SUMMARY = "video driver"
DESCRIPTION = "Recipe to build video driver module. The driver configures the vidc interfaces in the video subsystem."
HOMEPAGE = "https://git.codelinaro.org/"
LICENSE = "GPLv2.0-with-linux-syscall-note"
LIC_FILES_CHKSUM = "file://${QTI_LICENSE_DIR}/${LICENSE};md5=8afb6abdac9a14cb18a0d6c9c151e9b4"

DEPENDS += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-hypervisor', '', 'video-devicetree', d)}"
DEPENDS:gvm-gen5 += "video-devicetree virtio-video"

SRC_URI = "${PATH_TO_REPO}/vendor/qcom/opensource/video-driver/.git;protocol=${PROTO};destsuffix=vendor/qcom/opensource/video-driver;usehead=1"

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/vendor/qcom/opensource/video-driver"
EXT_MODULE = "vendor/qcom/opensource/video-driver"

TECHPACK_MODULE_OUT = "${@bb.utils.contains('MACHINE_FEATURES', 'qti-hypervisor', '', '${WORKDIR}/vendor/qcom/opensource/video-driver-out', d)}"
TECHPACK_MODULE_OUT:gvm-gen5 = "${S}"

TECHPACK_MODULES = "${@bb.utils.contains('MACHINE_FEATURES', 'qti-hypervisor', '', 'msm-vidc.ko', d)}"
TECHPACK_MODULES:gvm-gen5 = "msm_video.ko"

# export v4l2_vidc_extensions.h for QC2 HAL
TECHPACK_HEADERS = "${S}/include/uapi"

inherit qti-techpack

RDEPENDS:${PN}:gvm-gen5 += "kernel-module-msm-virtio-video-${KERNEL_VERSION}"

RPROVIDES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-hypervisor', '', 'kernel-module-msm-vidc-${KERNEL_VERSION}', d)}"
RPROVIDES:${PN}:gvm-gen5 += "kernel-module-msm-video-${KERNEL_VERSION}"

FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-hypervisor', '', '${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/*', d)}"
FILES:${PN}:gvm-gen5 += "${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/msm_video.ko"

ALLOW_EMPTY:${PN} = "1"
