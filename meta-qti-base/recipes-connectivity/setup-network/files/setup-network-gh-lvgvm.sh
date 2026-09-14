#!/bin/sh
# Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause-Clear

CMDLINE_PATH=/proc/cmdline

AGL1_IFACE_ARRAY="eth0"
AGL2_IFACE_ARRAY="eth0"

AGL1_IFACE_ARRAY1="eth1"
AGL1_IFACE_ARRAY2="eth2"

get_gvm_version() {
    cmdline_value=$(cat "$CMDLINE_PATH")
    system_name_value=$(echo "$cmdline_value" | sed -n 's/.*system_name=\([^ ]*\).*/\1/p')
    echo "system_name_value=${system_name_value}!"
    case "$system_name_value" in
        lv) return 1 ;;
        tgvm) return 2 ;;
        *) return 0 ;;
    esac
}

check_all_interfaces_up() {
    iface_arr="$1"
    for iface_name in $iface_arr; do
        iface_cnt=$(ifconfig -a | grep "$iface_name" | wc -l)
        if [ "$iface_cnt" -ne 1 ]; then
            echo " WARN : $iface_name is not Ready"
            return 0
        fi
    done
    return 1
}

check_dns_conf() {
    echo "DNS resolv.conf file is present. "
    for i in $(seq 1 10); do
        if [ -e /etc/resolv.conf ]; then
            echo "DNS resolv.conf file is present. "
            return 0
        fi
        sleep 0.5    # 500ms
    done
    echo " WARN : resolv.conf file is not present, DNS not working !"
    return 1
}

setup_network_agl_vm_1() {
    echo "update eth0 MAC address"
    ip link set dev eth0 down
    ip link set dev eth0 address fe:bb:9a:58:aa:01
    ip link set dev eth0 up

    if [ -e /vendor/persist/enable_dhcp ]; then
        echo "Start DHCP."
        check_dns_conf
        udhcpc -i eth0 -b
        echo "Start DHCP complete."
    else
        echo "Assign Static IP Address for eth0"
        ifconfig eth0 192.168.1.4 up
        echo "Setup route"
        ip route add default dev eth0 via 192.168.1.10 table default
    fi

    echo "Create vlan"
    ip link add link eth0 name eth0.4 type vlan id 4
    ifconfig eth0.4 192.168.4.2 up

    echo "Enable forwarding"
    sysctl -w net.ipv4.conf.all.forwarding=1

    echo "Apply GVM perf network tuning"
    sysctl -w net.core.wmem_max=67108864
    sysctl -w net.core.rmem_max=33554432
    sysctl -w net.ipv4.udp_mem="786432 1048576 16777216"
    sysctl -w net.ipv4.tcp_mem="786432 1048576 16777216"
    sysctl -w net.core.rmem_default=16777216
    sysctl -w net.core.optmem_max=25165824
    sysctl -w net.ipv4.tcp_rmem="8192 87380 16777216"
    sysctl -w net.ipv4.tcp_wmem="8192 87380 16777216"
    sysctl -w net.core.somaxconn=10000
    sysctl -w net.core.netdev_max_backlog=10000
    sysctl -w net.ipv4.ipfrag_high_thresh=50000000
    sysctl -w net.ipv6.ip6frag_high_thresh=50000000
    sysctl -w net.ipv4.ipfrag_time=2
    sysctl -w net.ipv6.ip6frag_time=2

    sysctl -p
}

setup_network_agl_vm_2() {
    echo "update eth0 MAC address"
    ip link set dev eth0 down
    ip link set dev eth0 address fe:bb:9a:58:aa:02
    ip link set dev eth0 up

    echo "Assign IP address"
    ifconfig eth0 192.168.1.8 up

    echo "Setup route"
    ip route add default dev eth0 via 192.168.1.10

    echo "Create vlan"
    ip link add link eth0 name eth0.4 type vlan id 4
    ifconfig eth0.4 192.168.4.3 up

    echo "Enable forwarding"
    sysctl -w net.ipv4.conf.all.forwarding=1
    sysctl -p
}

get_gvm_version
gvm_version=$?

ietf1_configured=false
ietf2_configured=false
ietf3_configured=false

for i in $(seq 1 10); do
    if [ "$gvm_version" -eq 2 ]; then
        check_all_interfaces_up "$AGL2_IFACE_ARRAY"
        iface_is_up=$?
        echo "current interface status is ${iface_is_up}"
        if [ "$iface_is_up" -eq 1 ]; then
            setup_network_agl_vm_2
            break
        else
            echo " ERR : Ethernet Interfaces are not Ready !!!"
        fi
    else
        if [ "$ietf1_configured" = true ] && [ "$ietf2_configured" = true ] && [ "$ietf3_configured" = true ]; then
            echo " eth0, eth1, and eth2 are all configured."
            break
        fi

        check_all_interfaces_up "$AGL1_IFACE_ARRAY"
        iface1_is_up=$?
        echo "current interface status is ${iface1_is_up}"
        if [ "$iface1_is_up" -eq 1 ] && [ "$ietf1_configured" = false ]; then
            setup_network_agl_vm_1
            ietf1_configured=true
        else
            echo " ERR : Ethernet Interfaces(eth0) are not Ready or already configured !!!"
        fi

        check_all_interfaces_up "$AGL1_IFACE_ARRAY1"
        iface2_is_up=$?
        echo "current interface status is ${iface2_is_up}"
        if [ "$iface2_is_up" -eq 1 ] && [ "$ietf2_configured" = false ]; then
            echo "Qti-base:Assign Static IP Address for eth1"
            ifconfig eth1 192.168.6.2 up
            ietf2_configured=true
        else
            echo " ERR : Ethernet Interfaces(eth1) are not Ready or already configured !!!"
        fi

        check_all_interfaces_up "$AGL1_IFACE_ARRAY2"
        iface3_is_up=$?
        echo "current interface status is ${iface3_is_up}"
        if [ "$iface3_is_up" -eq 1 ] && [ "$ietf3_configured" = false ]; then
            echo "Qti-base:Assign Static IP Address for eth2"
            ifconfig eth2 192.168.7.2 up
            ietf3_configured=true
        else
            echo " ERR : Ethernet Interfaces(eth2) are not Ready or already configured !!!"
        fi
    fi
    sleep 2
done
