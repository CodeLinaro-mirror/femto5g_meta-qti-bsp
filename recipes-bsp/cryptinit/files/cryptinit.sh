#!/bin/sh
# Copyright (c) 2022 Qualcomm Innovation Center, Inc. All rights reserved.
# SPDX-License-Identifier: BSD-3-Clause-Clear

counter=0
while [ ! -e /dev/mapper/persist ]; do
    counter=$((counter + 1))
    sleep 0.1
    if [ $counter -gt 100 ]; then
        echo "/dev/mapper/persist not found after 10 seconds"
        exit 1
    fi
done
counter=$((counter * 100))
echo "/dev/mapper/persist ready, time = $counter ms"

# Restrict maximum IO through the dm-crypt and dm-integrity drivers
#  to reduce memory consumption.
function throttle_block_device {
    echo "throttling /sys/block/$1"
    if [ -e "/sys/block/$1" ]; then
        echo 0 > "/sys/block/$1/queue/rotational"
        echo 128 > "/sys/block/$1/queue/read_ahead_kb"
        # dm-# entries do not have nr_requests
    else
        echo "/sys/block/$1 does not exist on bootup"
    fi
}

crypt_blk="$(dmsetup info persist -c --noheadings -o blkdevname)"
if [ $? -eq 0 ]; then
    throttle_block_device "$crypt_blk"
fi
integ_blk="$(dmsetup info persist_dif -c --noheadings -o blkdevname)"
if [ $? -eq 0 ]; then
    throttle_block_device "$integ_blk"
fi

blkid /dev/mapper/persist | grep "/dev/mapper/persist"
persistfmt=$?
if [ -b "/dev/mapper/persist" -a $persistfmt -ne 0 ]; then
    mkfs.ext4 /dev/mapper/persist
    sync
else
    echo "persist already formatted, directly mounting"
fi
mount -t ext4 /dev/mapper/persist /persist -o rootcontext=system_u:object_r:persist_t:s0

if [ ! -d "/persist/display" ]; then
    mkdir -p /persist/display/
    chown -R system:system /persist/display
    chmod 770 /persist/display
    restorecon /persist/display
else
    echo "/persist/display already created"
fi

if [ ! -d "/persist/c2pa" ]; then
    mkdir -p /persist/c2pa/
    chown -R system:system /persist/c2pa
    chmod 770 /persist/c2pa
    restorecon /persist/c2pa
else
    echo "/persist/c2pa already created"
fi
