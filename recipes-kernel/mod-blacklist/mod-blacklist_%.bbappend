KERNEL_PREBUILT_PATH:kalama = "${WORKSPACE}/kernel-6.18/kernel_platform/out"
KERNEL_PREBUILT_DISTDIR:kalama = "${KERNEL_PREBUILT_PATH}/${KERNEL_DEFCONFIG}/dist"
KERNEL_OUT_PATH:kalama = "${KERNEL_PREBUILT_PATH}/${KERNEL_DEFCONFIG}"

#FILESEXTRAPATHS:prepend:kalama := "${TMPDIR}/work/${MACHINE}-oe-linux/linux-soc-repo/6.18/sources/kernel-6.18/kernel_platform/soc-repo/modules-lists:"

FILESEXTRAPATHS:prepend:kalama := "${WORKSPACE}/kernel-6.18/kernel_platform/soc-repo/modules-lists:"
