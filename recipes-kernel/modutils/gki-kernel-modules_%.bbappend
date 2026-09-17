KERNEL_PREBUILT_PATH:kalama = "${WORKSPACE}/src/kernel-6.18/kernel_platform/out/msm-kernel-kalama_le-debug_defconfig"
KERNEL_PREBUILT_DISTDIR:kalama = "${KERNEL_PREBUILT_PATH}/dist"
KERNEL_OUT_PATH:kalama = "${KERNEL_PREBUILT_PATH}"
FILESEXTRAPATHS:prepend:kalama := "${KERNEL_PLATFORM_PATH}/${KERNEL_SRC_TYPE}/modules-lists:"
