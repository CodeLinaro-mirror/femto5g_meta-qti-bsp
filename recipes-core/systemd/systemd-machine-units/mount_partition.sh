#!/bin/sh
# Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause-Clear
#
# Mount a partition by its partlabel name.
# Usage: mount_partition.sh <partlabel> [slot_suffix]
#
# Examples:
#   mount_partition.sh modem
#   mount_partition.sh dsp _a
#   mount_partition.sh bluetooth _b

PARTLABEL="$1"
SLOT_SUFFIX="${2:-}"
EXTRA_CMD=""

if [ -z "$PARTLABEL" ]; then
    echo "Usage: $0 <partlabel> [slot_suffix]" >&2
    exit 1
fi

# Partition-to-mountpoint and filesystem-type/options table.
# Format: mountpoint|fstype|options|[extra_cmd]
case "$PARTLABEL" in
    modem)
        MOUNTPOINT="/firmware"
        FSTYPE="vfat"
        OPTIONS="noexec,nodev,ro,context=system_u:object_r:firmware_t:s0"
        ;;
    bluetooth)
        MOUNTPOINT="/bt_firmware"
        FSTYPE="vfat"
        OPTIONS="noexec,nodev,ro,context=system_u:object_r:firmware_t:s0"
        ;;
    dsp)
        MOUNTPOINT="/dsp"
        FSTYPE="ext4"
        OPTIONS="noatime,data=ordered,noauto_da_alloc,discard,ro,noexec,nodev,nosuid,context=system_u:object_r:adsprpcd_t:s0"
        ;;
    persist)
        MOUNTPOINT="/persist"
        FSTYPE="ext4"
        OPTIONS="noatime,data=ordered,noauto_da_alloc,discard,noexec,nodev,nosuid"
        ;;
    vendor_dlkm)
        MOUNTPOINT="/lib/modules"
        FSTYPE="ext4"
        OPTIONS="noexec,nodev,ro,context=system_u:object_r:lib_t:s0"
        EXTRA_CMD="/bin/ln -sf /lib/modules/modules-load.d /run/modules-load.d"
        ;;
    overlay)
        # Physical partition is 'userdata'; logical name passed as 'overlay'.
        DEVICE_PARTLABEL="userdata"
        MOUNTPOINT="/overlay"
        FSTYPE="ext4"
        OPTIONS="noatime,nosuid,nodev,barrier=1,data=ordered,noauto_da_alloc,discard,noexec,rootcontext=system_u:object_r:overlay_t:s0,inlinecrypt"
        EXTRA_CMD="/sbin/create-overlay-workdirs && \
          mount -t overlay overlay -o lowerdir=/data,upperdir=/overlay/data,workdir=/overlay/.data-work,rootcontext=system_u:object_r:data_t:s0 /data && \
          mount -t overlay overlay -o lowerdir=/etc,upperdir=/overlay/etc,workdir=/overlay/.etc-work,rootcontext=system_u:object_r:etc_t:s0 /etc && \
          mount -t overlay overlay -o lowerdir=/cache,upperdir=/overlay/cache,workdir=/overlay/.cache-work,rootcontext=system_u:object_r:cache_t:s0 /cache"
        ;;
    *)
        echo "Unknown partition '$PARTLABEL'. Supported: modem, bluetooth, dsp, persist, vendor_dlkm, overlay" >&2
        exit 1
        ;;
esac

DEVICE="/dev/disk/by-partlabel/${DEVICE_PARTLABEL:-${PARTLABEL}}${SLOT_SUFFIX}"

# Wait up to 3 s (60 × 0.05 s) for the device to appear.
i=0
while [ $i -lt 60 ]; do
    test -e "$DEVICE" && break
    sleep 0.05
    i=$((i + 1))
done

if [ ! -e "$DEVICE" ]; then
    echo "Device not found: $DEVICE" >&2
    exit 1
fi

# Skip if mountpoint already in use.
if mountpoint -q "$MOUNTPOINT" 2>/dev/null; then
    echo "Mountpoint $MOUNTPOINT is not empty, skipping." >&2
    exit 0
fi

mkdir -p "$MOUNTPOINT"

echo "Mounting $DEVICE -> $MOUNTPOINT ($FSTYPE, $OPTIONS)"
/bin/mount -o "$OPTIONS" -t "$FSTYPE" "$DEVICE" "$MOUNTPOINT" || exit 1

if [ -n "$EXTRA_CMD" ]; then
    eval "$EXTRA_CMD"
fi

echo "Done."
