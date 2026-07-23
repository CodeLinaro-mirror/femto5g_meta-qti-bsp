SUMMARY = "QCVirtIO camera device driver"
DESCRIPTION = "Recipe to build virtio camera driver module"
HOMEPAGE = "https://git.codelinaro.org"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=801f80980d171dd6425610833a22dbe6"

SRC_URI = "${PATH_TO_REPO}/vendor/qcom/opensource/virtio-camera-kernel/.git;protocol=${PROTO};destsuffix=vendor/qcom/opensource/virtio-camera-kernel;usehead=1"
SRCREV = "${AUTOREV}"

COMPATIBLE_MACHINE = "gvm-gen4-5-virtio"

S = "${WORKDIR}/vendor/qcom/opensource/virtio-camera-kernel"

PACKAGE_ARCH = "${MACHINE_ARCH}"

EXT_MODULE = "vendor/qcom/opensource/virtio-camera-kernel"

TECHPACK_MODULES = "virtio_camera.ko"
TECHPACK_MAKE_ARGS = "MODNAME=virtio_camera"

inherit qti-techpack

KERNEL_MODULE_AUTOLOAD += "virtio_camera"
