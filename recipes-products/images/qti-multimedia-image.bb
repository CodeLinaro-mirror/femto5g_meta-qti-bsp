# QTI Linux multimedia image file.
# Provides packages required to build an image with
# all multimedia support enabled.

inherit qimage

IMAGE_FEATURES += "ssh-server-openssh"

MLIBPREFIX ?= ""

CORE_IMAGE_EXTRA_INSTALL += "\
        ${MLIBPREFIX}glib-2.0 \
        ${MLIBPREFIX}kernel-modules \
        ${MLIBPREFIX}libdrm-tests \
        ${MLIBPREFIX}graphite-client \
        ${MLIBPREFIX}alsa-utils \
        ${MLIBPREFIX}packagegroup-android-utils \
        ${MLIBPREFIX}packagegroup-filesystem-utils \
        ${MLIBPREFIX}packagegroup-qti-audio \
        ${MLIBPREFIX}packagegroup-qti-pulseaudio \
        ${MLIBPREFIX}packagegroup-qti-bluetooth \
        ${MLIBPREFIX}packagegroup-qti-camera \
        ${MLIBPREFIX}packagegroup-qti-camera-kernel \
        ${MLIBPREFIX}packagegroup-qti-core \
        ${MLIBPREFIX}packagegroup-qti-data \
        ${MLIBPREFIX}packagegroup-qti-display \
        ${MLIBPREFIX}packagegroup-qti-dsp \
        ${MLIBPREFIX}packagegroup-qti-fastcv \
        ${MLIBPREFIX}packagegroup-qti-gst \
        ${MLIBPREFIX}packagegroup-qti-ml \
        ${MLIBPREFIX}packagegroup-qti-gfx \
        ${MLIBPREFIX}packagegroup-qti-qmmf \
        ${@bb.utils.contains('COMBINED_FEATURES', 'qti-security', '${MLIBPREFIX}packagegroup-qti-securemsm', '', d)} \
        ${@bb.utils.contains('MACHINE_FEATURES', 'qti-sensors', '${MLIBPREFIX}packagegroup-qti-sensors', '', d)} \
        ${@bb.utils.contains('MACHINE_FEATURES', 'qti-sensors-prop', '${MLIBPREFIX}packagegroup-qti-sensors-see', '', d)} \
        ${@bb.utils.contains('MACHINE_FEATURES', 'qti-sensors-prop', '${MLIBPREFIX}packagegroup-qti-test-sensors-see', '', d)} \
        ${@bb.utils.contains('MACHINE_FEATURES', 'qti-location', '${MLIBPREFIX}packagegroup-qti-location', '', d)} \
        ${MLIBPREFIX}packagegroup-qti-ss-mgr \
        ${MLIBPREFIX}packagegroup-qti-video \
        ${MLIBPREFIX}packagegroup-qti-wifi \
        ${MLIBPREFIX}packagegroup-startup-scripts \
        ${MLIBPREFIX}packagegroup-support-utils \
        ${MLIBPREFIX}systemd-machine-units \
        ${@bb.utils.contains('DISTRO_FEATURES','selinux', '${MLIBPREFIX}packagegroup-selinux-minimal', '', d)} \
"

CORE_IMAGE_EXTRA_INSTALL:remove:qcm2290-mtp = "kernel-modules"
CORE_IMAGE_EXTRA_INSTALL:remove:qcm2290-mtp = " ${MLIBPREFIX}graphite-client"
CORE_IMAGE_EXTRA_INSTALL:append:qcm2290-mtp = " gki-kernel-modules-second-stage"
CORE_IMAGE_EXTRA_INSTALL:append:qcm2290-mtp = " ${MLIBPREFIX}diag-router"
CORE_IMAGE_EXTRA_INSTALL:append:qcm2290-mtp = " ${MLIBPREFIX}packagegroup-qti-touch"

CORE_IMAGE_EXTRA_INSTALL:remove:qcm4325-mtp = "kernel-modules"
CORE_IMAGE_EXTRA_INSTALL:append:qcm4325-mtp = " gki-kernel-modules-second-stage"
CORE_IMAGE_EXTRA_INSTALL:append:qcm4325-mtp = " ${MLIBPREFIX}diag-router"
CORE_IMAGE_EXTRA_INSTALL:remove:qcm4325-mtp = "${MLIBPREFIX}graphite-client"
CORE_IMAGE_EXTRA_INSTALL:append:qcm4325-mtp = " ${MLIBPREFIX}packagegroup-qti-touch"
