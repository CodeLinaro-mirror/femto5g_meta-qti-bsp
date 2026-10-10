FILESEXTRAPATHS:prepend:echo := "${THISDIR}/files:"

SRC_URI:append:echo = " file://0001-pseudo-avoid-openat2-via-syscall.patch"
