SUMMARY = "Package suite with debugging tools from OE and QTI"

inherit packagegroup

PACKAGES =  "\
              packagegroup-qti-debug-tools \
            "

# Add debug support packages to RDEPENDS list for a debug build.
# Remote debugging can be carried out(through adb port forwarding)
# on target gdb takes up considerable storage.
# Avoid gdb on target.
RDEPENDS:packagegroup-qti-debug-tools = " \
            gdbserver \
            strace \
            ${@bb.utils.contains_any('MACHINE', 'trustedvm-v6 trustedvm-v5 trustedvm-v4', '', 'valgrind', d)} \
            systemd-analyze \
            procrank \
            ${@bb.utils.contains_any('MACHINE', 'trustedvm-v6 trustedvm-v5 trustedvm-v4 trustedvm-v3', '', 'perf', d)} \
            atrace \
        "
