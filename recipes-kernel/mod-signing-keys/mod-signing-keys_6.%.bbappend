
LIC_FILES_CHKSUM = "file://${WORKSPACEROOT}/poky/meta-qti-bsp/files/common-licenses/GPLv2.0-with-linux-syscall-note;md5=8afb6abdac9a14cb18a0d6c9c151e9b4"
KERNEL_PREBUILT_PATH = "${WORKSPACE}/kernel-6.18/kernel_platform/out/msm-kernel-kalama_le-debug_defconfig"

FILESEXTRAPATHS:prepend := "${KERNEL_PREBUILT_PATH}:"
