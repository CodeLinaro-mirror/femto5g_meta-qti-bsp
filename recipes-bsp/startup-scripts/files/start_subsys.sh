#!/bin/sh
# Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted (subject to the limitations in the
# disclaimer below) provided that the following conditions are met:
#
#    * Redistributions of source code must retain the above copyright
#      notice, this list of conditions and the following disclaimer.
#
#    * Redistributions in binary form must reproduce the above
#      copyright notice, this list of conditions and the following
#      disclaimer in the documentation and/or other materials provided
#      with the distribution.
#
#    * Neither the name of Qualcomm Technologies, Inc. nor the names of its
#      contributors may be used to endorse or promote products derived
#      from this software without specific prior written permission.
#
# NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE
# GRANTED BY THIS LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
# HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
# IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
# ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
# GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
# IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
# OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
# IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#
# LE (systemd) subsystem bring-up: soccp / adsp / cdsp via remoteproc sysfs.
# POSIX-sh compatible (busybox ash friendly): no bash-only [[ ]] constructs.

# Wait up to 10s for a directory to exist
wait_for_dir() {
    dir_path="$1"
    label="${2:-$1}"

    i=0
    while [ "$i" -lt 10 ]; do
        if [ -d "$dir_path" ]; then
            echo "[OK] Dir present: $label"
            return 0
        fi
        i=$((i + 1))
        sleep 1
    done

    echo "[TIMEOUT] Dir not found after 10s: $label"
    return 1
}

# Wait up to 10s for a file to exist
wait_for_file() {
    file_path="$1"
    label="${2:-$1}"

    i=0
    while [ "$i" -lt 10 ]; do
        if [ -e "$file_path" ]; then
            echo "[OK] File present: $label"
            return 0
        fi
        i=$((i + 1))
        sleep 1
    done

    echo "[TIMEOUT] File not found after 10s: $label"
    return 1
}

# Ensure remoteproc class exists before anything else
rproc_class="/sys/class/remoteproc"
wait_for_dir "$rproc_class" "remoteproc class" || exit 1

# Ensure at least one remoteprocN directory exists.
# Use a glob that won't error when no match; test via 'set --' trick
# (POSIX-friendly approach to check if any remoteproc* directories are present).
# Retry up to 10 times (1s sleep) until at least one remoteproc* dir appears.
set -- "$rproc_class"/remoteproc*
attempt=0
while [ "$1" = "$rproc_class/remoteproc*" ] && [ "$attempt" -lt 10 ]; do
    attempt=$((attempt + 1))
    echo "[RETRY $attempt/10] No remoteproc* directories yet under $rproc_class; waiting 1s."
    sleep 1
    set -- "$rproc_class"/remoteproc*  # re-expand the glob on each iteration
done

if [ "$1" = "$rproc_class/remoteproc*" ]; then
    echo "[TIMEOUT] No remoteproc* directories found under $rproc_class after 10s"
    exit 1
fi

# wait for the first remoteprocN dir
first_rproc_dir="$1"
wait_for_dir "$first_rproc_dir" "first remoteprocN dir" || exit 1

# remoteproc device directories for soccp/adsp/cdsp by name matching
soccp_path_file="$(grep -rl 'soccp' "$rproc_class"/remoteproc*/name 2>/dev/null | head -n1)"
[ -n "$soccp_path_file" ] && soccp_path_file="$(dirname "$soccp_path_file")"
adsp_path_file="$(grep -rl 'adsp' "$rproc_class"/remoteproc*/name 2>/dev/null | head -n1)"
[ -n "$adsp_path_file" ] && adsp_path_file="$(dirname "$adsp_path_file")"
cdsp_path_file="$(grep -rl 'cdsp' "$rproc_class"/remoteproc*/name 2>/dev/null | head -n1)"
[ -n "$cdsp_path_file" ] && cdsp_path_file="$(dirname "$cdsp_path_file")"

# Boot mode (Android-style property service may be absent on LE; guard it).
if command -v getprop >/dev/null 2>&1; then
    boot_mode_sh="$(getprop ro.boot.mode 2>/dev/null)"
else
    boot_mode_sh=""
fi
echo "Subsystem starting in mode: $boot_mode_sh"

# remoteproc state file paths
soccp_state_file=""
adsp_state_file=""
cdsp_state_file=""

[ -n "$soccp_path_file" ] && [ -d "$soccp_path_file" ] && soccp_state_file="$soccp_path_file/state"
[ -n "$adsp_path_file" ] && [ -d "$adsp_path_file" ] && adsp_state_file="$adsp_path_file/state"
[ -n "$cdsp_path_file" ] && [ -d "$cdsp_path_file" ] && cdsp_state_file="$cdsp_path_file/state"

# Wait for all state files to appear in parallel
soccp_pid=""
adsp_pid=""
cdsp_pid=""

[ -n "$soccp_state_file" ] && { wait_for_file "$soccp_state_file" "soccp state file" & soccp_pid=$!; }
[ -n "$adsp_state_file"  ] && { wait_for_file "$adsp_state_file"  "adsp state file"  & adsp_pid=$!; }
[ -n "$cdsp_state_file"  ] && { wait_for_file "$cdsp_state_file"  "cdsp state file"  & cdsp_pid=$!; }

wait_result=0
[ -n "$soccp_pid" ] && { wait "$soccp_pid" || wait_result=1; }
[ -n "$adsp_pid"  ] && { wait "$adsp_pid"  || wait_result=1; }
[ -n "$cdsp_pid"  ] && { wait "$cdsp_pid"  || wait_result=1; }
[ "$wait_result" -ne 0 ] && exit 1

# Read current states
soccp_state=""
adsp_state=""
cdsp_state=""
[ -n "$soccp_state_file" ] && soccp_state="$(cat "$soccp_state_file" 2>/dev/null)"
[ -n "$adsp_state_file"  ] && adsp_state="$(cat "$adsp_state_file"  2>/dev/null)"
[ -n "$cdsp_state_file"  ] && cdsp_state="$(cat "$cdsp_state_file"  2>/dev/null)"

# Start subsystems in parallel
if [ "$soccp_state" != "running" ] && [ "$soccp_state" != "attached" ]; then
    echo "Requesting soccp start"
    echo "start" > "$soccp_state_file" &
fi

# Skip ADSP/CDSP in charger mode
if [ "$boot_mode_sh" != "charger" ]; then
    if [ "$adsp_state" != "running" ]; then
        echo "Requesting adsp start"
        echo "start" > "$adsp_state_file" &
    fi

    if [ "$cdsp_state" != "running" ]; then
        echo "Requesting cdsp start"
        echo "start" > "$cdsp_state_file" &
    fi
fi
wait
