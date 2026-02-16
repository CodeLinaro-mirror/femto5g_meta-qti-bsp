# QTI Linux robotics image file.
# Provides packages required to build an image with
# robotics features support.

inherit qimage populate_sdk_qti

IMAGE_FEATURES += "ssh-server-openssh"

ROS_FEATURES_PKG += "\
    ${@bb.utils.contains('BBFILE_COLLECTIONS', 'ros2-humble-layer', 'packagegroup-ros2-humble', '', d)} \
    ${@bb.utils.contains('BBFILE_COLLECTIONS', 'ros2-foxy-layer', 'packagegroup-ros2-foxy packagegroup-qti-ros2-node', '', d)} \
"

CORE_IMAGE_EXTRA_INSTALL += "\
        alsa-utils \
        can-utils \
        chronyc \
        glib-2.0 \
        gki-kernel-modules-second-stage \
        packagegroup-android-utils \
        packagegroup-filesystem-utils \
        packagegroup-qti-audio \
        packagegroup-qti-bluetooth \
        packagegroup-qti-camera \
        packagegroup-qti-camera-kernel \
        ${@bb.utils.contains('DISTRO_FEATURES','virtualization', 'packagegroup-qti-containers', '', d)} \
        packagegroup-qti-core \
        packagegroup-qti-core-prop \
        diag-router \
        packagegroup-qti-cvp \
        packagegroup-qti-eva \
        packagegroup-qti-data \
        packagegroup-qti-display \
        packagegroup-qti-touch \
        packagegroup-qti-dsp \
        packagegroup-qti-fastcv \
        packagegroup-mesa \
        packagegroup-qti-gfx \
        packagegroup-qti-gst \
        ${@bb.utils.contains('MACHINE_FEATURES', 'qti-location', 'packagegroup-qti-location', '', d)} \
        packagegroup-qti-ml \
        packagegroup-qti-mmframeworks \
        packagegroup-qti-pulseaudio \
        packagegroup-qti-qmmf \
        packagegroup-qti-robotics \
        packagegroup-qti-robos \
        packagegroup-qti-robos-addon \
        packagegroup-qti-securemsm \
        packagegroup-qti-ss-mgr \
        packagegroup-qti-test-sensors-see \
        packagegroup-qti-video \
        packagegroup-qti-wifi \
        ${@bb.utils.contains('DISTRO_FEATURES', 'ros2', '${ROS_FEATURES_PKG}', '', d)} \
        ${@bb.utils.contains('DISTRO_FEATURES', 'qirp-sdk', 'packagegroup-qti-qirp', '', d)} \
        packagegroup-startup-scripts \
        packagegroup-support-utils \
        ${@bb.utils.contains('DISTRO_FEATURES', 'qti-ib2c', 'qti-ib2c', '', d)} \
        ${@bb.utils.contains("COMBINED_FEATURES", "qti-uvc", "qti-umd-gadget", "", d)} \
        ${@bb.utils.contains("COMBINED_FEATURES", "qti-uvc", "qti-auto-framing-stabilization", "", d)} \
        systemd-machine-units \
        ${@bb.utils.contains('DISTRO_FEATURES','selinux', 'packagegroup-selinux-minimal', '', d)} \
        yavta \
        libdmabufheap \
        packagegroup-qti-sensors-ship \
        packagegroup-qti-sensors-see \
        packagegroup-qti-perf \
        tdk-chx01-get-data-app \
        tdk-thermistor-app \
        system-sample-apps \
        qti-c2-module \
"

CORE_IMAGE_EXTRA_INSTALL:append:pineapple = " packagegroup-qcom-sensors"
CORE_IMAGE_EXTRA_INSTALL:append:pineapple = " packagegroup-qti-ppat"
CORE_IMAGE_EXTRA_INSTALL:append:pineapple = " packagegroup-qcom-synx"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "alsa-utils"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "can-utils"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "chronyc"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "packagegroup-qti-cvp"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "packagegroup-qti-data"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "packagegroup-qti-mmframeworks"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "packagegroup-qti-robotics"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "packagegroup-qti-robos"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "packagegroup-qti-robos-addon"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "packagegroup-qti-test-sensors-see"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "yavta"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "packagegroup-qti-sensors-ship"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "packagegroup-qti-sensors-see"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "tdk-chx01-get-data-app"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "tdk-thermistor-app"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "system-sample-apps"

CORE_IMAGE_EXTRA_INSTALL:remove:kalama = "chronyc"
CORE_IMAGE_EXTRA_INSTALL:remove:kalama = "packagegroup-qti-ml"
CORE_IMAGE_EXTRA_INSTALL:remove:kalama = "packagegroup-qti-cvp"
CORE_IMAGE_EXTRA_INSTALL:remove:kalama = "packagegroup-qti-gst"
CORE_IMAGE_EXTRA_INSTALL:remove:kalama = "packagegroup-qti-robotics"
CORE_IMAGE_EXTRA_INSTALL:remove:kalama = "packagegroup-qti-test-sensors-see"
CORE_IMAGE_EXTRA_INSTALL:remove:kalama = "tdk-chx01-get-data-app"
CORE_IMAGE_EXTRA_INSTALL:remove:kalama = "tdk-thermistor-app"
CORE_IMAGE_EXTRA_INSTALL:remove:kalama = "packagegroup-qti-sensors-see"
CORE_IMAGE_EXTRA_INSTALL:append:kalama = " packagegroup-qti-qcawifi"
CORE_IMAGE_EXTRA_INSTALL:append:kalama = " tzdata tzcode"
CORE_IMAGE_EXTRA_INSTALL:append:kalama = " qcrosvm"
CORE_IMAGE_EXTRA_INSTALL:append:kalama = " vmsharememory-test"
# disable 'matter' package as default configuration
# CORE_IMAGE_EXTRA_INSTALL:append:kalama = " matter"
CORE_IMAGE_EXTRA_INSTALL:append:kalama = " i2c-tools"
CORE_IMAGE_EXTRA_INSTALL:append:kalama = " v4l-utils"
CORE_IMAGE_EXTRA_INSTALL:append:kalama = " yavta"
CORE_IMAGE_EXTRA_INSTALL:append:kalama = " lsof"

CORE_IMAGE_EXTRA_INSTALL:remove:qrb5165 = "packagegroup-qti-data"
CORE_IMAGE_EXTRA_INSTALL:remove:qrb5165 = "packagegroup-qti-eva"
CORE_IMAGE_EXTRA_INSTALL:remove:qrb5165 = "packagegroup-qti-ss-mgr"
CORE_IMAGE_EXTRA_INSTALL:remove:qrb5165 = "packagegroup-qti-touch"
CORE_IMAGE_EXTRA_INSTALL:remove:qrb5165 = "packagegroup-qti-perf"
CORE_IMAGE_EXTRA_INSTALL:remove:qrb5165 = "packagegroup-qti-sensors-ship"
CORE_IMAGE_EXTRA_INSTALL:remove:qrb5165 = "packagegroup-qti-robos-addon"
CORE_IMAGE_EXTRA_INSTALL:remove:qrb5165 = "system-sample-apps"
CORE_IMAGE_EXTRA_INSTALL:remove:qrb5165 = "tdk-chx01-get-data-app"
CORE_IMAGE_EXTRA_INSTALL:remove:qrb5165 = "tdk-thermistor-app"
