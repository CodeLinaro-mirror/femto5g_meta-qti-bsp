#!/bin/sh
#Copyright (c) Qualcomm Innovation Center, Inc. All rights reserved.
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

if [[ "$SLOT_SUFFIX" == "_a" ]]; then
    if [[ -e "${DEVICE}" ]]; then
        # bare system exists: system_a/_b+system present, or non-A/B — use bare system
        SLOT_SUFFIX=""
    elif [[ ! -e "${DEVICE}${SLOT_SUFFIX}" ]]; then
        # system_a not found either, fall back to bare system
        SLOT_SUFFIX=""
    fi
    # system_a exists and bare system absent: true A/B slot A, keep _a
elif [[ "$SLOT_SUFFIX" == "_b" ]]; then
    if [[ -e "${DEVICE}_a" && -e "${DEVICE}" ]]; then
        # system_a + bare system both exist: bare system is canonical, use it
        SLOT_SUFFIX=""
    fi
    # system_a absent or bare system absent: true A/B slot B, keep _b
fi

if [ -f /verity/$MAPDEV.env ]; then
   source /verity/$MAPDEV.env
   /usr/sbin/veritysetup open $DEVICE$SLOT_SUFFIX $MAPDEV \
       $DEVICE$SLOT_SUFFIX $VERITY_ROOT_HASH --salt $VERITY_SALT \
       --hash-offset $VERITY_HASH_OFFSET --data-blocks $VERITY_DATA_BLOCKS \
       --fec-device $DEVICE$SLOT_SUFFIX --fec-offset $VERITY_FEC_OFFSET \
       --fec-roots $VERITY_FEC_ROOTS --root-hash-signature=/verity/"$MAPDEV".sig \
       --restart-on-corruption

   if [ $? -ne 0 ]; then
      echo "Veritysetup failed."
      echo "Rebooting the device."
      systemctl reboot "dm-verity device corrupted" -f
   fi

   # veritysetup doesn't create symlink to /dev/dm-X as expected by udev, do it explicitly
   dm_node=""
   for d in /sys/block/dm-*; do
       [ -r "$d/dm/name" ] || continue
       name="$(cat "$d/dm/name" 2>/dev/null)"
       if [ "$name" = "$MAPDEV" ]; then
           dm_node="$(basename "$d")"   # e.g. dm-1
           break
       fi
   done

   if [ -n "$dm_node" ]; then
       /bin/ln -sf "../$dm_node" "/dev/mapper/$MAPDEV"
   fi

   echo "/dev/mapper/$MAPDEV ready"
else
   echo "/verity/$MAPDEV.env not found. Exiting..."
   systemctl reboot "dm-verity device corrupted" -f
fi
