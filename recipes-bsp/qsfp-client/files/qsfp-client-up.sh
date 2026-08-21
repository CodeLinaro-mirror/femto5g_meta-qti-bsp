#!/bin/sh
# ---------------------------------------------------------------------------
# Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause-Clear
# ---------------------------------------------------------------------------

set -u

log()  { echo "[+] $*"; }
warn() { echo "[!] $*" >&2; }
die()  { echo "[E] $*" >&2; exit 1; }

CONF="${CONF:-/etc/qsfp-client.conf}"
[ -r "$CONF" ] || die "config file not found: ${CONF}"
. "$CONF"

ALL_LANE_MODES="1x100GBASE_R2 1x100GBASE_R4 2x50GBASE_R 1x50GBASE_R \
1x50GBASE_R2 1x40GBASE_R4 4x25GBASE_R 1x25GBASE_R 4x10GBASE_R 1x10GBASE_R"

require() {
	command -v "$1" >/dev/null 2>&1 || die "missing required tool: $1"
}

iface_exists() {
	ip link show "$IFACE" >/dev/null 2>&1
}

release_from_bridge() {
	master=$(ip -o link show "$IFACE" 2>/dev/null \
	           | sed -n 's/.* master \([^ ]*\) .*/\1/p')

	if [ -n "${master:-}" ]; then
		log "releasing ${IFACE} from bridge ${master}"
		ip link set "$IFACE" nomaster \
		  || die "failed to release ${IFACE} from ${master}"
	else
		log "${IFACE} is not a bridge port"
	fi

	if [ -n "${BRIDGE}" ] && ip link show "$BRIDGE" >/dev/null 2>&1; then
		if ip -o addr show dev "$BRIDGE" 2>/dev/null | grep -q "${ADDR%%/*}"; then
			log "removing duplicate ${ADDR} from ${BRIDGE}"
			ip addr del "$ADDR" dev "$BRIDGE" 2>/dev/null || true
		fi
	fi
}

set_lane_mode() {
	command -v ethtool >/dev/null 2>&1 || return 0
	ethtool --show-priv-flags "$IFACE" >/dev/null 2>&1 || {
		warn "driver exposes no private flags - skipping lane mode"
		return 0
	}

	log "selecting lane mode ${LANE_MODE} (all others off)"

	ethtool --set-priv-flags "$IFACE" "$LANE_MODE" on 2>/dev/null \
	  || warn "could not enable ${LANE_MODE}"

	for m in $ALL_LANE_MODES; do
		[ "$m" = "$LANE_MODE" ] && continue
		ethtool --set-priv-flags "$IFACE" "$m" off 2>/dev/null || true
	done

	ethtool --set-priv-flags "$IFACE" "$LANE_MODE" on 2>/dev/null || true

	cur=$(ethtool --show-priv-flags "$IFACE" 2>/dev/null \
	        | awk -v m="$LANE_MODE" '$1==m { print $NF }')
	if [ "${cur:-off}" != "on" ]; then
		warn "${LANE_MODE} did not stick (reads '${cur:-unknown}')"
	fi
}

set_link_params() {
	command -v ethtool >/dev/null 2>&1 || return 0

	log "forcing ${SPEED}Mb/s full duplex, autoneg off"
	ethtool -s "$IFACE" autoneg off speed "$SPEED" duplex full 2>/dev/null \
	  || warn "link settings rejected (may already be applied)"

	if [ -n "${FEC}" ]; then
		log "setting FEC encoding ${FEC}"
		ethtool --set-fec "$IFACE" encoding "$FEC" 2>/dev/null \
		  || warn "FEC ${FEC} not accepted - driver may negotiate it itself"
	fi
}

relax_filters() {
	for k in rp_filter arp_ignore arp_filter; do
		for s in all "$IFACE"; do
			f="/proc/sys/net/ipv4/conf/$s/$k"
			[ -w "$f" ] && echo 0 > "$f" 2>/dev/null || true
		done
	done
	[ -w /proc/sys/net/ipv4/icmp_echo_ignore_all ] &&
		echo 0 > /proc/sys/net/ipv4/icmp_echo_ignore_all 2>/dev/null || true
}

configure_ip() {
	log "configuring ${IFACE} = ${ADDR} (mtu ${MTU})"
	ip addr flush dev "$IFACE" 2>/dev/null || true
	ip link set "$IFACE" mtu "$MTU" || die "failed to set mtu ${MTU}"
	ip addr add "$ADDR" dev "$IFACE" || die "failed to add ${ADDR}"

	net=$(echo "$ADDR" | cut -d/ -f1 | cut -d. -f1-3)
	ip route show default 2>/dev/null | grep -q "via ${net}\." && {
		log "removing bogus default route on ${net}.0/24"
		ip route del default dev "$IFACE" 2>/dev/null || true
	}
}

wait_for_carrier() {
	log "waiting up to ${LINK_TIMEOUT}s for carrier on ${IFACE}"
	i=0
	while [ "$i" -lt "$LINK_TIMEOUT" ]; do
		if ip -o link show "$IFACE" 2>/dev/null | grep -q 'LOWER_UP'; then
			log "carrier up after ${i}s"
			return 0
		fi
		i=$((i + 1))
		sleep 1
	done
	warn "no carrier after ${LINK_TIMEOUT}s"
	return 1
}

show_status() {
	echo "--- ${IFACE} ---"
	ip -br addr show "$IFACE" 2>/dev/null
	ip -o link show "$IFACE" 2>/dev/null | grep -q ' master ' &&
		warn "${IFACE} is STILL a bridge port - traffic will be consumed by the bridge"

	if command -v ethtool >/dev/null 2>&1; then
		ethtool "$IFACE" 2>/dev/null \
		  | grep -E 'Speed|Duplex|Auto-negotiation|Link detected'
		ethtool --show-priv-flags "$IFACE" 2>/dev/null \
		  | grep -iE '25GBASE|10GBASE' || true
		ethtool --show-fec "$IFACE" 2>/dev/null | grep -i 'Active' || true
	fi

	echo "--- route to ${PEER} ---"
	ip route get "$PEER" 2>/dev/null || true

	dup=$(ip -o addr show 2>/dev/null | grep -c "${ADDR%%/*}")
	[ "${dup:-0}" -gt 1 ] &&
		warn "${ADDR%%/*} is configured on more than one device"
	return 0
}

verify() {
	command -v ping >/dev/null 2>&1 || return 0
	log "pinging peer ${PEER}"
	if ping -c 3 -W 2 "$PEER" >/dev/null 2>&1; then
		log "PASS: ${PEER} is reachable"
		return 0
	fi
	warn "FAIL: ${PEER} not reachable"
	warn "check on the CX: ip -br addr, ethtool <if> | grep Link, and that"
	warn "its MTU is ${MTU} and address is ${PEER}"
	return 1
}

do_up() {
	iface_exists || die "interface ${IFACE} not found"

	relax_filters
	release_from_bridge

	log "bringing ${IFACE} down to apply link settings"
	ip link set "$IFACE" down
	sleep 1

	set_lane_mode
	set_link_params

	ip link set "$IFACE" up
	wait_for_carrier || true

	if ip -o link show "$IFACE" 2>/dev/null | grep -q ' master '; then
		warn "${IFACE} was re-enslaved after link up - releasing again"
		ip link set "$IFACE" nomaster || true
	fi

	configure_ip
	sleep 2

	show_status
	verify
}

do_down() {
	iface_exists || die "interface ${IFACE} not found"
	log "taking ${IFACE} down"
	ip addr flush dev "$IFACE" 2>/dev/null || true
	ip link set "$IFACE" down
}

require ip

case "${1:-up}" in
up)     do_up ;;
down)   do_down ;;
status) iface_exists || die "interface ${IFACE} not found"; show_status ;;
*)      echo "Usage: $0 {up|down|status}" >&2; exit 2 ;;
esac
