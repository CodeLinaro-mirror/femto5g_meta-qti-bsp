# QTI Linux multimedia image file.
# Provides packages required to build an image with
# all multimedia support enabled.

inherit qimage populate_sdk_qti

IMAGE_FEATURES += "ssh-server-openssh"

CORE_IMAGE_EXTRA_INSTALL += "\
        glib-2.0 \
        kernel-modules \
        libdrm-tests \
        graphite-client \
        alsa-utils \
        packagegroup-android-utils \
        packagegroup-filesystem-utils \
        packagegroup-qti-audio \
        packagegroup-qti-pulseaudio \
        packagegroup-qti-bluetooth \
        packagegroup-qti-camera \
        packagegroup-qti-camera-kernel \
        packagegroup-qti-core \
        packagegroup-qti-data \
        packagegroup-qti-display \
        packagegroup-qti-dsp \
        packagegroup-qti-fastcv \
        packagegroup-qti-gst \
        packagegroup-qti-ml \
        packagegroup-qti-gfx \
        packagegroup-qti-qmmf \
        ${@bb.utils.contains('COMBINED_FEATURES', 'qti-security', 'packagegroup-qti-securemsm', '', d)} \
        ${@bb.utils.contains('MACHINE_FEATURES', 'qti-sensors', 'packagegroup-qti-sensors', '', d)} \
        ${@bb.utils.contains('MACHINE_FEATURES', 'qti-sensors-prop', 'packagegroup-qti-sensors-see', '', d)} \
        ${@bb.utils.contains('MACHINE_FEATURES', 'qti-sensors-prop', 'packagegroup-qti-test-sensors-see', '', d)} \
        ${@bb.utils.contains('MACHINE_FEATURES', 'qti-location', 'packagegroup-qti-location', '', d)} \
        packagegroup-qti-ss-mgr \
        packagegroup-qti-video \
        packagegroup-qti-wifi \
        packagegroup-startup-scripts \
        packagegroup-support-utils \
        systemd-machine-units \
        ${@bb.utils.contains('DISTRO_FEATURES','selinux', 'packagegroup-selinux-minimal', '', d)} \
        ${@bb.utils.contains('DISTRO_FEATURES','virtualization', 'packagegroup-qti-containers', '', d)} \
"

CORE_IMAGE_EXTRA_INSTALL:remove:qcm2290-mtp = "kernel-modules"
CORE_IMAGE_EXTRA_INSTALL:append:qcm2290-mtp = " gki-kernel-modules-second-stage"
CORE_IMAGE_EXTRA_INSTALL:append:qcm2290-mtp = " diag-router"
CORE_IMAGE_EXTRA_INSTALL:remove:qcs610-odk-64 = "kernel-modules"
CORE_IMAGE_EXTRA_INSTALL:append:qcs610-odk-64 = " gki-kernel-modules-second-stage"
CORE_IMAGE_EXTRA_INSTALL:append:qcs610-odk-64 = " diag-router"
CORE_IMAGE_EXTRA_INSTALL:append:qcs610-odk-64 = " packagegroup-qti-ppat"
CORE_IMAGE_EXTRA_INSTALL:remove:qcs610-odk-64 = "packagegroup-qti-display"
CORE_IMAGE_EXTRA_INSTALL:append:qcs610-odk-64 = " packagegroup-qcom-display"
CORE_IMAGE_EXTRA_INSTALL:remove:qcs610-odk-64 = "graphite-client"

CORE_IMAGE_EXTRA_INSTALL:remove:vienna = "kernel-modules"
CORE_IMAGE_EXTRA_INSTALL:remove:vienna = "packagegroup-qti-data"
CORE_IMAGE_EXTRA_INSTALL:remove:vienna = "graphite-client"
CORE_IMAGE_EXTRA_INSTALL:append:vienna = " packagegroup-qti-coproc"
CORE_IMAGE_EXTRA_INSTALL:append:vienna = " packagegroup-qti-wear-prop"
CORE_IMAGE_EXTRA_INSTALL:append:vienna = " gki-kernel-modules-second-stage"
CORE_IMAGE_EXTRA_INSTALL:append:vienna = " diag-router"
CORE_IMAGE_EXTRA_INSTALL:append:vienna = " packagegroup-qcom-sensors"
CORE_IMAGE_EXTRA_INSTALL:append:vienna = " packagegroup-qti-uwb"
CORE_IMAGE_EXTRA_INSTALL:append:vienna = " packagegroup-qti-perf"

CORE_IMAGE_EXTRA_INSTALL:remove:kera = "alsa-utils"
CORE_IMAGE_EXTRA_INSTALL:remove:kera = "kernel-modules"
CORE_IMAGE_EXTRA_INSTALL:remove:kera = "graphite-client"
CORE_IMAGE_EXTRA_INSTALL:remove:kera = "packagegroup-qti-test-sensors-see"
CORE_IMAGE_EXTRA_INSTALL:remove:kera = "packagegroup-qti-sensors-see"
CORE_IMAGE_EXTRA_INSTALL:remove:kera = "packagegroup-qti-sensors"
CORE_IMAGE_EXTRA_INSTALL:remove:kera = "packagegroup-qti-internal"
CORE_IMAGE_EXTRA_INSTALL:append:kera = " gki-kernel-modules-second-stage"
CORE_IMAGE_EXTRA_INSTALL:append:kera = " packagegroup-qti-core-prop"
CORE_IMAGE_EXTRA_INSTALL:append:kera = " packagegroup-qti-mmframeworks"
CORE_IMAGE_EXTRA_INSTALL:append:kera = " packagegroup-qti-ss-mgr"
CORE_IMAGE_EXTRA_INSTALL:append:kera = " packagegroup-qcom-sensors"
CORE_IMAGE_EXTRA_INSTALL:append:kera = " packagegroup-qti-touch"
CORE_IMAGE_EXTRA_INSTALL:append:kera = " packagegroup-qti-perf"
CORE_IMAGE_EXTRA_INSTALL:append:kera = " packagegroup-qti-nfc"

CORE_IMAGE_EXTRA_INSTALL:remove:sun = "kernel-modules"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-data"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-ss-mgr"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "graphite-client"
CORE_IMAGE_EXTRA_INSTALL:append:sun = " packagegroup-mesa"
CORE_IMAGE_EXTRA_INSTALL:append:sun = " packagegroup-qcom-sensors"
CORE_IMAGE_EXTRA_INSTALL:append:sun = " gki-kernel-modules-second-stage"
CORE_IMAGE_EXTRA_INSTALL:append:sun = " packagegroup-qti-core-prop"
CORE_IMAGE_EXTRA_INSTALL:append:sun = " packagegroup-qti-mmframeworks"
CORE_IMAGE_EXTRA_INSTALL:append:sun = " packagegroup-qti-eva"
CORE_IMAGE_EXTRA_INSTALL:append:sun = " packagegroup-qti-touch"
CORE_IMAGE_EXTRA_INSTALL:append:sun = " packagegroup-qti-perf"
CORE_IMAGE_EXTRA_INSTALL:append:sun = " packagegroup-qti-qcawifi"

CORE_IMAGE_EXTRA_INSTALL:remove:alor = "alsa-utils"
CORE_IMAGE_EXTRA_INSTALL:remove:alor = "kernel-modules"
CORE_IMAGE_EXTRA_INSTALL:remove:alor = "libdrm-tests"
CORE_IMAGE_EXTRA_INSTALL:remove:alor = "graphite-client"
CORE_IMAGE_EXTRA_INSTALL:remove:alor = "packagegroup-qti-data"
CORE_IMAGE_EXTRA_INSTALL:remove:alor = "packagegroup-qti-test-sensors-see"
CORE_IMAGE_EXTRA_INSTALL:remove:alor = "packagegroup-qti-sensors-see"
CORE_IMAGE_EXTRA_INSTALL:remove:alor = "packagegroup-qti-sensors"
CORE_IMAGE_EXTRA_INSTALL:remove:alor = "packagegroup-qti-internal"
CORE_IMAGE_EXTRA_INSTALL:append:alor = " gki-kernel-modules-second-stage"
CORE_IMAGE_EXTRA_INSTALL:append:alor = " packagegroup-qti-perf"
CORE_IMAGE_EXTRA_INSTALL:append:alor = " packagegroup-mesa"
CORE_IMAGE_EXTRA_INSTALL:append:alor = " packagegroup-qti-touch"
CORE_IMAGE_EXTRA_INSTALL:append:alor = " packagegroup-qcom-sensors"
CORE_IMAGE_EXTRA_INSTALL:append:alor = " packagegroup-qti-mmframeworks"
CORE_IMAGE_EXTRA_INSTALL:append:alor = " packagegroup-qti-eva"
CORE_IMAGE_EXTRA_INSTALL:append:alor = " libsv-headers"
CORE_IMAGE_EXTRA_INSTALL:append:alor = " packagegroup-qti-ppat"

CORE_IMAGE_EXTRA_INSTALL:remove:pebble = "kernel-modules"
CORE_IMAGE_EXTRA_INSTALL:remove:pebble = "libdrm-tests"
CORE_IMAGE_EXTRA_INSTALL:remove:pebble = "graphite-client"
CORE_IMAGE_EXTRA_INSTALL:remove:pebble = "alsa-utils"
CORE_IMAGE_EXTRA_INSTALL:remove:pebble = "packagegroup-qti-bluetooth"
CORE_IMAGE_EXTRA_INSTALL:remove:pebble = "packagegroup-qti-data"
CORE_IMAGE_EXTRA_INSTALL:remove:pebble = "packagegroup-qti-display"
CORE_IMAGE_EXTRA_INSTALL:remove:pebble = "packagegroup-qti-dsp"
CORE_IMAGE_EXTRA_INSTALL:remove:pebble = "packagegroup-qti-gfx"
CORE_IMAGE_EXTRA_INSTALL:remove:pebble = "packagegroup-qti-video"
CORE_IMAGE_EXTRA_INSTALL:remove:pebble = "packagegroup-qti-wifi"
CORE_IMAGE_EXTRA_INSTALL:append:pebble = " gki-kernel-modules-second-stage"
CORE_IMAGE_EXTRA_INSTALL:append:pebble = " packagegroup-qti-perf"
CORE_IMAGE_EXTRA_INSTALL:append:pebble = " packagegroup-qti-eva"
CORE_IMAGE_EXTRA_INSTALL:append:pebble = " libsv-headers"
CORE_IMAGE_EXTRA_INSTALL:append:pebble = " mmrm-kernel"
CORE_IMAGE_EXTRA_INSTALL:append:pebble = " sv-noship"
CORE_IMAGE_EXTRA_INSTALL:append:pebble = " packagegroup-qti-mmframeworks"
