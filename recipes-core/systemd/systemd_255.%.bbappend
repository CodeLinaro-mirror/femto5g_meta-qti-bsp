FILESEXTRAPATHS:prepend := "${THISDIR}/systemd_255:"

SRC_URI += "file://Disable-unused-mount-points.patch"
SRC_URI += "file://sd-bus-Allow-extra-users-to-communicate.patch"
SRC_URI += "file://fstab-generator-Honor-verity-enabled-cmdline.patch"

GROUPADD_PARAM:${PN}:prepend = "-f -g 1000 system; "
