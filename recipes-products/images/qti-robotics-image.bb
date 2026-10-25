# QTI Linux robotics image file.
# Provides packages required to build an image with
# robotics features support.

inherit qimage populate_sdk_qti

IMAGE_FEATURES += "ssh-server-openssh"

# Kalama 6.18 bring-up: keep WARN backtraces in the kernel log without flooding
# the 115200-baud console while the watchdog waits for CPU keep-alive responses.
# This is a diagnostic mitigation pending the legacy DT binding migration.
KERNEL_CMD_PARAMS:append:kalama = " loglevel=4 printk.console_no_auto_verbose=1 log_buf_len=4M"

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
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "packagegroup-qti-gfx"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "packagegroup-qti-mmframeworks"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "packagegroup-qti-robotics"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "packagegroup-qti-robos"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "packagegroup-qti-robos-addon"
CORE_IMAGE_EXTRA_INSTALL:remove:pineapple = "packagegroup-qti-securemsm"
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

CORE_IMAGE_EXTRA_INSTALL:remove:sun = "can-utils"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "chronyc"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-bluetooth"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-camera"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-camera-kernel"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-cvp"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-eva"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-data"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-display"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-mesa"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-gfx"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-gst"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-ml"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-pulseaudio"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-qmmf"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-robotics"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-robos"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-robos-addon"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-securemsm"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-ss-mgr"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-test-sensors-see"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-wifi"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-sensors-ship"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qti-sensors-see"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "tdk-chx01-get-data-app"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "tdk-thermistor-app"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "system-sample-apps"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "qti-c2-module"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "qti-umd-gadget"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "qti-auto-framing-stabilization"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-qcom-sensors"
CORE_IMAGE_EXTRA_INSTALL:remove:sun = "packagegroup-selinux-minimal"
