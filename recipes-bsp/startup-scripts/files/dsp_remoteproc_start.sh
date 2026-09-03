#!/bin/sh
# Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause-Clear
#
# Start the ADSP and CDSP subsystems through the remoteproc sysfs interface.
# Needed on alor because the companion kernel change clears auto_boot for
# alor_adsp_resource / alor_cdsp_resource, so the kernel no longer boots them
# at probe time.

RPROC_CLASS="/sys/class/remoteproc"
# Space separated; relies on word splitting in the for loop below.
SUBSYS_LIST="adsp cdsp"

# How long to wait for asynchronously created sysfs nodes: 100 x 0.1s = 10s.
MAX_ATTEMPTS=100
POLL_INTERVAL=0.1

# Shell convention: 0 means success, non-zero means failure. Named so that the
# returns below read as intent rather than as magic numbers.
SUCCESS=0
FAILURE=1

# Poll for a directory to appear.
wait_for_dir() {
    dir_path="$1"
    attempt=0
    while [ "$attempt" -lt "$MAX_ATTEMPTS" ]; do
        if [ -d "$dir_path" ]; then
            echo "[OK] Dir present: $dir_path"
            return "$SUCCESS"
        fi
        attempt=$((attempt + 1))
        sleep "$POLL_INTERVAL"
    done
    echo "[TIMEOUT] Dir not found after ${MAX_ATTEMPTS} x ${POLL_INTERVAL}s: $dir_path"
    return "$FAILURE"
}

# Poll for a file to appear.
wait_for_file() {
    file_path="$1"
    attempt=0
    while [ "$attempt" -lt "$MAX_ATTEMPTS" ]; do
        if [ -e "$file_path" ]; then
            echo "[OK] File present: $file_path"
            return "$SUCCESS"
        fi
        attempt=$((attempt + 1))
        sleep "$POLL_INTERVAL"
    done
    echo "[TIMEOUT] File not found after ${MAX_ATTEMPTS} x ${POLL_INTERVAL}s: $file_path"
    return "$FAILURE"
}

# Poll for one subsystem's remoteproc node and echo its directory path.
#
# Matches the sysfs "name" attribute by its "remoteproc-<subsys>" suffix only.
# The full name is "<base-addr>.remoteproc-<subsys>" and the base address differs
# per SoC, which is why this script matches only the suffix rather than the
# full name (avoiding the per-machine address patching used in cdsp-start.bb).
#
# Re-globs on every attempt because remoteproc devices register asynchronously
# and their probe may be deferred.
wait_for_subsys_dir() {
    # Deliberately not named "subsys": that is the caller's loop variable, and
    # this only happens to be safe today because the call is wrapped in $(...).
    subsys_name="$1"
    attempt=0
    while [ "$attempt" -lt "$MAX_ATTEMPTS" ]; do
        for dir in "$RPROC_CLASS"/remoteproc*/; do
            [ -r "$dir/name" ] || continue
            case "$(cat "$dir/name")" in
            *remoteproc-"$subsys_name")
                echo "${dir%/}"   # strip the trailing slash left by the glob
                return "$SUCCESS"
                ;;
            esac
        done
        attempt=$((attempt + 1))
        sleep "$POLL_INTERVAL"
    done
    return "$FAILURE"
}

# Bring up one subsystem. Returns FAILURE if it could not be started.
start_subsys() {
    # Distinct name from the caller's loop variable: this function runs in the
    # current shell, so a shared name would silently overwrite it.
    target="$1"

    subsys_dir="$(wait_for_subsys_dir "$target")"
    if [ -z "$subsys_dir" ]; then
        echo "[TIMEOUT] Node not found after ${MAX_ATTEMPTS} x ${POLL_INTERVAL}s: remoteproc-$target"
        return "$FAILURE"
    fi
    echo "[OK] Node present: $target ($subsys_dir)"

    state_file="$subsys_dir/state"
    wait_for_file "$state_file" || return "$FAILURE"

    # Writing "start" to an already running subsystem is not rejected: it only
    # bumps rproc->power, which unbalances the refcount so that a later "stop"
    # no longer shuts the subsystem down. So always check the state first.
    state="$(cat "$state_file" 2>/dev/null)"
    if [ "$state" = "running" ] || [ "$state" = "attached" ]; then
        echo "$target already $state, nothing to do"
        return "$SUCCESS"
    fi

    echo "Requesting $target start"
    if ! echo start > "$state_file"; then
        echo "[ERROR] Could not write start to $state_file"
        return "$FAILURE"
    fi
    return "$SUCCESS"
}

# The class directory only exists once the remoteproc framework is up, and
# everything below depends on it.
wait_for_dir "$RPROC_CLASS" || exit "$FAILURE"

failed_count=0
for subsys in $SUBSYS_LIST; do
    start_subsys "$subsys" || failed_count=$((failed_count + 1))
done

# Report failures to systemd. Without this the script would exit with the status
# of the loop's last command, which hides an earlier subsystem having failed.
if [ "$failed_count" -gt 0 ]; then
    echo "[ERROR] $failed_count subsystem(s) failed to start"
    exit "$FAILURE"
fi

exit "$SUCCESS"
