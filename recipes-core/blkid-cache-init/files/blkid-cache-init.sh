#!/bin/sh
# Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause-Clear

# Pre-populate blkid cache for root device to prevent slow mount on ramdisk-less boots.
mm=$(awk '$5 == "/" {print $3; exit}' /proc/self/mountinfo)
[ -z "$mm" ] && exit 0

uevent="/sys/dev/block/$mm/uevent"
[ -f "$uevent" ] || exit 0

d=$(grep "^DEVNAME="  "$uevent" | cut -d= -f2-)
p=$(grep "^PARTNAME=" "$uevent" | cut -d= -f2-)
q=$(grep "^PARTUUID=" "$uevent" | cut -d= -f2-)

# No PARTNAME means root is a dm device (verity enabled) - nothing to do
[ -z "$p" ] && exit 0

# Read filesystem type dynamically from /proc/mounts
t=$(awk '$2 == "/" {print $3; exit}' /proc/mounts)
[ -z "$t" ] && t="ext4"

a=$(echo "$mm" | cut -d: -f1)
b=$(echo "$mm" | cut -d: -f2)
n=$(printf "0x%08x" $((a * 256 + b)))

mkdir -p /run/blkid
printf '<device DEVNO="%s" TIME="%s" PARTLABEL="%s" PARTUUID="%s" TYPE="%s">/dev/%s</device>\n' \
    "$n" "$(date +%s)" "$p" "$q" "$t" "$d" > /run/blkid/blkid.tab
