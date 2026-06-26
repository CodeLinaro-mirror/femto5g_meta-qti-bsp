SUMMARY = "virtio video driver"
DESCRIPTION = "Recipe to build virtio video driver module provides BE communication"
HOMEPAGE = "https://git.codelinaro.org"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=801f80980d171dd6425610833a22dbe6"

SRC_URI = "${PATH_TO_REPO}/vendor/qcom/opensource/virtio-video/.git;protocol=${PROTO};destsuffix=vendor/qcom/opensource/virtio-video;usehead=1"
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/vendor/qcom/opensource/virtio-video"

EXT_MODULE = "vendor/qcom/opensource/virtio-video"

TECHPACK_MODULE_OUT = "${S}"
TECHPACK_MODULES = "msm_virtio_video.ko"

TECHPACK_MAKE_ARGS = "\
    VIDEO_ROOT=${S} \
    BOARD_PLATFORM=gen5 \
    ENABLE_HYP=true \
    MODNAME=msm_virtio_video \
"

inherit qti-techpack

do_configure[depends] += "virtual/kernel:do_shared_workdir"

# copy vidc_hw_virt.h into the kernel source tree for this and downstream videodlkm build
do_configure:prepend() {
    install -D -m 0644 \
        "${S}/include/vidc_hw_virt.h" \
        "${STAGING_KERNEL_DIR}/include/vidc_hw_virt.h"
}

FILES:${PN} += "${nonarch_base_libdir}/modules/${KERNEL_VERSION}/extra/msm_virtio_video.ko"

RPROVIDES:${PN} += "kernel-module-msm-virtio-video-${KERNEL_VERSION}"
