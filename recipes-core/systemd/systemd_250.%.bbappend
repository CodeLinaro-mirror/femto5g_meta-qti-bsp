FILESEXTRAPATHS:prepend := "${THISDIR}/systemd_250:"

SRC_URI += "${@bb.utils.contains('DISTRO_FEATURES', 'apparmor', ' ', 'file://Disable-unused-mount-points.patch', d)}"
SRC_URI += "file://fstab-generator-Honor-verity-enabled-cmdline.patch"
SRC_URI += "file://sd-bus-Allow-extra-users-to-communicate.patch"
SRC_URI += "file://0001-cgroup-downgrade-warning-if-we-can-t-get-ID-off-cgro.patch"
