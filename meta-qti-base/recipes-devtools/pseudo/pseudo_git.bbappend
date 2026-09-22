FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append:quin-gvm-gen4 = " file://0001-pseudo-avoid-openat2-via-syscall.patch"
SRC_URI:append:qtiquingvm8295 = " file://0001-pseudo-avoid-openat2-via-syscall.patch"
