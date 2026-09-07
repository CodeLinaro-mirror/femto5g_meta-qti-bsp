#!/bin/sh
#Copyright (c) 2024 Qualcomm Innovation Center, Inc. All rights reserved.
#SPDX-License-Identifier: BSD-3-Clause-Clear

SLOT_SUFFIX=$(/bin/echo $SLOT_SUFFIX)

MAPDEV=""
DEVICE=""
if [[ "$1" == "-p" ]]; then
    MAPDEV=$2
    shift; shift
fi

if [[ "$1" == "-d" ]]; then
    DEVICE=$2
    DEVICE="/dev/disk/by-partlabel/$2"
    shift; shift
fi

#get the first four bytes of the image header.
#Compare with "sqsh" in little endian to know whether sqaushfs image.
#68 - h, 73 - s, 71 - q, 73 - s
dd if=$DEVICE$SLOT_SUFFIX bs=1 count=4 2>/dev/null | hexdump -v -e '4/1 "%02x"' | grep -q '^68737173$' && echo "Valid Squashfs image" && Squashfs=1 || echo "Not a Squashfs image"

if [ "$Squashfs" == "1" ]; then
    sqshfs_size=$(dd if=$DEVICE$SLOT_SUFFIX bs=8 skip=5 count=1 2>/dev/null | od -An -td8)
    sqshfs_size_align=$(((sqshfs_size + 4095) / 4096 * 4096))
    verity_metadata_offset=`expr $sqshfs_size_align / 4096`
else
    size_in_block=$(echo $(($(blockdev --getsz $DEVICE$SLOT_SUFFIX) / 8)))
    verity_metadata_offset=$(($size_in_block - 1))
fi
# get Verity metadata and signature
# Block size is considered as 4096
# Half block size is considered as 2048
dd if="$DEVICE$SLOT_SUFFIX" bs=4096 count=1 skip="$verity_metadata_offset" | dd bs=1 count=2048 of="/tmp/$MAPDEV.env"
dd if="$DEVICE$SLOT_SUFFIX" bs=4096 count=1 skip="$verity_metadata_offset" | dd skip=2048 bs=1 count=2048 of="/tmp/$MAPDEV.sig"


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

    /usr/sbin/veritysetup open "$DEVICE$SLOT_SUFFIX" "$MAPDEV" \
        "$DEVICE$SLOT_SUFFIX" "$VERITY_ROOT_HASH" --salt "$VERITY_SALT" \
        --hash-offset "$VERITY_HASH_OFFSET" --data-blocks "$VERITY_DATA_BLOCKS" \
        --fec-device "$DEVICE$SLOT_SUFFIX" --fec-offset "$VERITY_FEC_OFFSET" \
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
