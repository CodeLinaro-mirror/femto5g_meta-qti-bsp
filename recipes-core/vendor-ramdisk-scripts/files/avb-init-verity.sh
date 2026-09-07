#!/bin/sh
#Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
#SPDX-License-Identifier: BSD-3-Clause-Clear

set -u

MAPDEV=""
DEVICE_LABEL=""

while [ $# -gt 0 ]; do
    case "$1" in
        -p)
            [ $# -ge 2 ] || fail_reboot "Missing argument for -p"
            MAPDEV="$2"
            shift 2
            ;;
        -d)
            [ $# -ge 2 ] || fail_reboot "Missing argument for -d"
            DEVICE_LABEL="$2"
            shift 2
            ;;
        *)
            fail_reboot "Unknown argument: $1"
            ;;
    esac
done

[ -n "$MAPDEV" ] || fail_reboot "MAPDEV is empty."
[ -n "$DEVICE_LABEL" ] || fail_reboot "DEVICE label is empty."

SLOT_SUFFIX=$(/bin/echo $SLOT_SUFFIX)
DEVICE="/dev/disk/by-partlabel/${DEVICE_LABEL}${SLOT_SUFFIX}"
ENV_FILE="/verity/${MAPDEV}.env"

echo "MAPDEV=$MAPDEV"
echo "DEVICE_LABEL=$DEVICE_LABEL"
echo "SLOT_SUFFIX=$SLOT_SUFFIX"
echo "DEVICE=$DEVICE"
echo "ENV_FILE=$ENV_FILE"

[ -f "$ENV_FILE" ] || fail_reboot "$ENV_FILE not found."
[ -e "$DEVICE" ] || fail_reboot "Device $DEVICE does not exist or access denied."

. "$ENV_FILE"

: "${VERITY_ROOT_HASH:?VERITY_ROOT_HASH missing}"
: "${VERITY_SALT:?VERITY_SALT missing}"
: "${VERITY_HASH_ALGORITHAM:?VERITY_HASH_ALGORITHAM missing}"
: "${VERITY_HASH_OFFSET:?VERITY_HASH_OFFSET missing}"
: "${VERITY_DATA_BLOCK_SIZE:?VERITY_DATA_BLOCK_SIZE missing}"
: "${VERITY_DATA_BLOCKS:?VERITY_DATA_BLOCKS missing}"
: "${VERITY_HASH_BLOCK_SIZE:?VERITY_HASH_BLOCK_SIZE missing}"
: "${VERITY_FEC_OFFSET:?VERITY_FEC_OFFSET missing}"
: "${VERITY_FEC_ROOTS:?VERITY_FEC_ROOTS missing}"

echo "Opening dm-verity mapper $MAPDEV on $DEVICE"

/usr/sbin/veritysetup open "$DEVICE" "$MAPDEV" \
    "$DEVICE" "$VERITY_ROOT_HASH" \
    --salt="$VERITY_SALT" \
    --hash="$VERITY_HASH_ALGORITHAM" \
    --hash-offset="$VERITY_HASH_OFFSET" \
    --data-block-size="$VERITY_DATA_BLOCK_SIZE" \
    --data-blocks="$VERITY_DATA_BLOCKS" \
    --hash-block-size="$VERITY_HASH_BLOCK_SIZE" \
    --fec-device="$DEVICE" \
    --fec-offset="$VERITY_FEC_OFFSET" \
    --fec-roots="$VERITY_FEC_ROOTS" \
    --no-superblock \
    --restart-on-corruption

if [ $? -ne 0 ]; then
    fail_reboot "Veritysetup failed."
fi

echo "verity setup is successful"

if [ ! -b "/dev/mapper/$MAPDEV" ]; then
    echo "/dev/mapper/$MAPDEV not created by udev, trying to create deterministic symlink."

    DM_TARGET="$(dmsetup info -c --noheadings -o blkdevname "$MAPDEV" 2>/dev/null | awk '{print $1}')"

    if [ -n "$DM_TARGET" ] && [ -b "/dev/$DM_TARGET" ]; then
        mkdir -p /dev/mapper
        ln -sf "../$DM_TARGET" "/dev/mapper/$MAPDEV" || fail_reboot "Failed to create /dev/mapper/$MAPDEV symlink."
    else
        fail_reboot "Unable to find dm device for mapper $MAPDEV."
    fi
fi

[ -b "/dev/mapper/$MAPDEV" ] || fail_reboot "/dev/mapper/$MAPDEV is not a block device."

echo "/dev/mapper/$MAPDEV ready"
exit 0
