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
    test ! -x /sbin/restorecon || /sbin/restorecon /persist/display
else
    echo "/persist/display already created"
fi

if [ ! -d "/persist/c2pa" ]; then
    mkdir -p /persist/c2pa/
    chown -R system:system /persist/c2pa
    chmod 770 /persist/c2pa
    test ! -x /sbin/restorecon || /sbin/restorecon /persist/c2pa
else
    echo "/persist/c2pa already created"
fi
