#!/bin/sh
#Copyright (c) 2024 Qualcomm Innovation Center, Inc. All rights reserved.
#SPDX-License-Identifier: BSD-3-Clause-Clear


MAPDEV=""
DEVICE=""

if [[ "$1" == "-p" ]]; then
    MAPDEV=$2
    shift; shift
fi

if [[ "$1" == "-d" ]]; then
    # v3 resolves the active system partition from kernel root=.
    # Keep consuming -d <name> for compatibility with veritysetup.service.in.
    DEVICE=$(awk '{for(n=1;n<=NF;n++) if ($n ~ "^root=") print $n}' /proc/cmdline | cut -d= -f2)
    shift; shift
fi

if [ -z "$DEVICE" ]; then
    echo "error: Unable to determine root device from /proc/cmdline. Rebooting." >/dev/kmsg
    systemctl reboot "dm-verity device not found" -f
    exit 1
fi

size_in_block=$(echo $(($(blockdev --getsz $DEVICE) / 8)))
verity_metadata_offset=$(($size_in_block - 1))

# get Verity metadata and signature
# Block size is considered as 4096
# Half block size is considered as 2048
dd if="$DEVICE" bs=4096 count=1 skip="$verity_metadata_offset" | dd bs=1 count=2048 of="/tmp/$MAPDEV.env"
dd if="$DEVICE" bs=4096 count=1 skip="$verity_metadata_offset" | dd skip=2048 bs=1 count=2048 of="/tmp/$MAPDEV.sig"

verity_setup() {
    local env_path=$1
    local sig_path=$2

# Extracts exactly the needed fields and sanitizes them (only allows digits/hex)
    VERITY_ROOT_HASH=$(awk -F= '$1=="VERITY_ROOT_HASH" {print $2; exit}' "$env_path" | tr -cd 'a-fA-F0-9')
    VERITY_SALT=$(awk -F= '$1=="VERITY_SALT" {print $2; exit}' "$env_path" | tr -cd 'a-fA-F0-9')
    VERITY_HASH_OFFSET=$(awk -F= '$1=="VERITY_HASH_OFFSET" {print $2; exit}' "$env_path" | tr -cd '0-9')
    VERITY_DATA_BLOCKS=$(awk -F= '$1=="VERITY_DATA_BLOCKS" {print $2; exit}' "$env_path" | tr -cd '0-9')
    VERITY_FEC_OFFSET=$(awk -F= '$1=="VERITY_FEC_OFFSET" {print $2; exit}' "$env_path" | tr -cd '0-9')
    VERITY_FEC_ROOTS=$(awk -F= '$1=="VERITY_FEC_ROOTS" {print $2; exit}' "$env_path" | tr -cd '0-9')

    if [ -z "$VERITY_ROOT_HASH" ] || [ -z "$VERITY_DATA_BLOCKS" ] || \
       [ -z "$VERITY_HASH_OFFSET" ] || [ -z "$VERITY_SALT" ] || \
       [ -z "$VERITY_FEC_OFFSET" ] || [ -z "$VERITY_FEC_ROOTS" ]; then
        echo "error: Missing or malformed Verity metadata. Rebooting." >/dev/kmsg
        systemctl reboot "dm-verity metadata invalid" -f
        sleep 5
        echo b > /proc/sysrq-trigger

exit 1
    fi

    /usr/sbin/veritysetup open "$DEVICE" "$MAPDEV" \
        "$DEVICE" "$VERITY_ROOT_HASH" --salt "$VERITY_SALT" \
        --hash-offset "$VERITY_HASH_OFFSET" --data-blocks "$VERITY_DATA_BLOCKS" \
        --fec-device "$DEVICE" --fec-offset "$VERITY_FEC_OFFSET" \
        --fec-roots "$VERITY_FEC_ROOTS" --root-hash-signature="$sig_path" \
        --restart-on-corruption

    if [ $? -ne 0 ]; then
        echo "error: Veritysetup failed. Rebooting the device" >/dev/kmsg
        systemctl reboot "dm-verity device corrupted" -f
    else
        echo "verity setup is successful" >/dev/kmsg
    fi

    # Create symlink to /dev/dm-X as expected by udev, do it explicitly
    /bin/ln -sf ../dm-0 /dev/mapper/"$MAPDEV"

    if [ $? -ne 0 ]; then
       echo "error: symlink to /dev/mapper/$MAPDEV has failed." >/dev/kmsg
    else
        echo "../dm-0 symlink to /dev/mapper/$MAPDEV is done" >/dev/kmsg
    fi
}

if [ -f /tmp/"$MAPDEV.env" ]; then
    echo -n "call verity_setup" >/dev/kmsg
    verity_setup "/tmp/$MAPDEV.env" "/tmp/$MAPDEV.sig"
else
    echo -n "error: $MAPDEV.env not found. Rebooting the device." >/dev/kmsg
    systemctl reboot "dm-verity device corrupted" -f
fi
