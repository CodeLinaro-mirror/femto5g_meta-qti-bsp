#!/bin/sh
#
# Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause-Clear
#
#
# modem-mount.sh: Mount modem firmware partition and trigger slot switch or EDL
# on mount failure for eMMC-based targets.
#
# Use cases handled:
#   Trial boot active, single slot corrupt    -> fall back to other slot after 7 trials
#   Trial boot active, both slots corrupt     -> enter EDL after 7+7 trials
#   Recovery boot active, single slot corrupt -> mark unbootable, reboot to other slot
#   Recovery boot active, both slots corrupt  -> enter EDL
#
# Trial boot vs recovery boot detection:
#   abctl --print_rci prints trial_boot_info.trial_boot_enable.
#   Value 1 = trial boot active (bootloader manages retry count).
#   Value 0 = recovery boot (HLOS marks slot unbootable immediately).
#   The recoveryinfo partition path is /dev/block/bootdevice/by-name/recoveryinfo.
#
# SLOT_SUFFIX is injected into this script's environment by systemd via
# PassEnvironment=SLOT_SUFFIX in firmware-mount.service. It is set in the
# systemd global environment by set-slotsuffix.service which runs:
#   systemctl set-environment SLOT_SUFFIX="$(getslotsuffix)"

FIRMWARE_DIR="/firmware"
MODEM_DEV="/dev/disk/by-partlabel/modem${SLOT_SUFFIX}"

# SELinux context option is stripped at build time by fix_sepolicies:echo for
# this target (SELinux disabled). Mount with base options only.
MOUNT_OPTS="noexec,nodev,ro"

SLOT_A="_a"
SLOT_B="_b"
SLOT_NUM_A=0
SLOT_NUM_B=1

log()
{
    echo "modem-mount: $1" > /dev/kmsg
}

get_trialboot_enable()
{
    # --print_rci prints "trial_boot_info.trial_boot_enable: <value>"
    # Returns 1 if trial boot is enabled, 0 if recovery boot, empty on error.
    abctl --print_rci 2>/dev/null | grep "trial_boot_enable" | awk '{print $NF}'
}

fail_reboot()
{
    TRIAL_BOOT_ENABLE=$(get_trialboot_enable)
    log "Trial boot enable: $TRIAL_BOOT_ENABLE for slot $SLOT_SUFFIX"

    if [ "$TRIAL_BOOT_ENABLE" = "1" ]; then
        # Trial boot active: the bootloader owns retry counting.
        # It increments trial_boot_failed_attempts on every boot and retries
        # the same slot until trial_boot_max_attempts (7) is reached.
        # HLOS must NOT mark the slot unbootable here — doing so would cause
        # the bootloader to switch slots immediately on the first failure
        # instead of retrying 7 times.
        # After 7 failures the bootloader switches to the other slot (or enters
        # EDL if both slots are exhausted).
        log "Trial boot active: rebooting, bootloader will retry slot $SLOT_SUFFIX"
    else
        # Recovery boot active: HLOS marks the slot unbootable immediately.
        # Bootloader picks the other slot on next boot. If that also fails
        # this script runs again, marks that slot unbootable too, and the
        # bootloader enters EDL when both slots are marked DONT_USE.
        log "Recovery boot active: marking slot $SLOT_SUFFIX unbootable"
        if [ "$SLOT_SUFFIX" = "$SLOT_A" ]; then
            abctl --set_unbootable $SLOT_NUM_A
        else
            abctl --set_unbootable $SLOT_NUM_B
        fi
    fi

    log "Rebooting after mount failure for slot $SLOT_SUFFIX"
    reboot -f
    # reboot -f should not return. If it does the system cannot reboot —
    # log the failure explicitly so it is visible in post-mortem debugging.
    log "ERROR: reboot -f failed for slot $SLOT_SUFFIX, system may be in an unrecoverable state"
    exit 1
}

# Validate that SLOT_SUFFIX was injected by set-slotsuffix.service.
if [ -z "$SLOT_SUFFIX" ]; then
    log "ERROR: SLOT_SUFFIX not set, cannot determine modem partition"
    exit 1
fi

log "Current slot: $SLOT_SUFFIX, modem device: $MODEM_DEV"

# /firmware may already be mounted.
if [ -d "$FIRMWARE_DIR/image" ]; then
    log "/firmware already mounted, skipping"
    exit 0
fi

# Attempt to mount the modem firmware partition.
mount -o "$MOUNT_OPTS" -t vfat "$MODEM_DEV" "$FIRMWARE_DIR"
MOUNT_ST=$?

if [ "$MOUNT_ST" != "0" ]; then
    log "Mount of $MODEM_DEV failed (status $MOUNT_ST)"
    fail_reboot
    # fail_reboot calls reboot -f and only returns if reboot itself failed.
    # The error is already logged inside fail_reboot.
    exit 1
fi

log "Mounted $MODEM_DEV on $FIRMWARE_DIR successfully"

exit 0
