# QTI Linux minimal boot image file.
# Provides packages required to build an image with
# boot to console

inherit qimage

IMAGE_FEATURES += "read-only-rootfs persist-volume"

MLIBPREFIX ?= ""

CORE_IMAGE_EXTRA_INSTALL += "\
              ${MLIBPREFIX}glib-2.0 \
              ${MLIBPREFIX}kernel-modules \
              ${MLIBPREFIX}systemd-machine-units \
              ${MLIBPREFIX}packagegroup-android-utils \
              ${MLIBPREFIX}packagegroup-startup-scripts \
"

IMAGE_FEATURES:remove:qcm2290-mtp = "persist-volume"
IMAGE_FEATURES:remove:qcs610-odk-64 = "persist-volume"

CORE_IMAGE_EXTRA_INSTALL:remove:qcm2290-mtp = "kernel-modules"
CORE_IMAGE_EXTRA_INSTALL:append:qcm2290-mtp = " gki-kernel-modules-second-stage"

CORE_IMAGE_EXTRA_INSTALL:remove:qcs610-odk-64 = "kernel-modules"
CORE_IMAGE_EXTRA_INSTALL:append:qcs610-odk-64 = " gki-kernel-modules-second-stage"

IMAGE_FEATURES:remove:qcm4325-mtp = "persist-volume"
IMAGE_FEATURES:remove:qcm4325-mtp = "read-only-rootfs"

CORE_IMAGE_EXTRA_INSTALL:remove:qcm4325-mtp = "kernel-modules"
CORE_IMAGE_EXTRA_INSTALL:append:qcm4325-mtp = " gki-kernel-modules-second-stage"
CORE_IMAGE_EXTRA_INSTALL:append:qcm4325-mtp = " diag-router"
