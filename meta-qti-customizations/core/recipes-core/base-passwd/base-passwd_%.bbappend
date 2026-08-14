FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "\
           file://add-diag-user.patch \
           file://add-sdcard-diag-groups.patch \
           file://add-reboot-daemon-group.patch \
           file://add-inet-group-tinyproxy.patch \
"

PR = "r1"
