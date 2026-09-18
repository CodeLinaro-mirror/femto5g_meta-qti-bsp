SUMMARY = "Basic programs and scripts required by LE system"
DESCRIPTION = "Package group to bring in all basic packages for LE system"
LICENSE = "BSD-3-Clause"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

PROVIDES = "${PACKAGES}"

USB_SUPPORT = "${@d.getVar('MACHINE_SUPPORTS_USB') or "True"}"
DISABLE_USBD_SUPPORT = "${@d.getVar('USB_PERIPHERAL_ONLY_MODE') or "False"}"

USB_AUTOSUSPEND_SUPPORT = "${@d.getVar('MACHINE_SUPPORTS_USB_AUTOSUSPEND') or "True"}"
PROPERTIES_SUPPORT = "${@d.getVar('MACHINE_SUPPORTS_ANDROID_PROPERTIES') or "True"}"

PACKAGES = ' \
    packagegroup-android-utils-base \
    packagegroup-filesystem-utils-base \
    packagegroup-startup-scripts-base \
    packagegroup-support-utils-base \
    '
ENABLE_ADB ?= "True"
ENABLE_ADB:qti-distro-base-user ?= "False"

# Android Core Image and Debugging utilities
RDEPENDS:packagegroup-android-utils-base = "\
    ${@oe.utils.conditional('ENABLE_ADB', 'True', 'adbd', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'qti-sdx', '', 'leproperties', d)} \
    logcat \
    logd \
    libsync \
    ${@oe.utils.conditional('PROPERTIES_SUPPORT', 'True', 'system-prop', '', d)} \
    "

ADDON_SCRIPTS ?= ""
ADDON_SCRIPTS:neo = "helios-start"

# Startup scripts needed during device bootup
RDEPENDS:packagegroup-startup-scripts-base = "\
    ${@bb.utils.contains('COMBINED_FEATURES', 'qti-ab-boot', 'ab-slot-util', '', d)} \
    ${@oe.utils.conditional('USB_SUPPORT', 'True', 'usb-composition', '', d)} \
    ${@oe.utils.conditional('USB_AUTOSUSPEND_SUPPORT', 'True', oe.utils.conditional('DISABLE_USBD_SUPPORT', 'True', '', 'usb-composition-usbd', d), '', d)} \
    post-boot \
    ${@oe.utils.conditional('BASEMACHINE', 'echo', '', 'sdcard-scripts-automount', d)} \
    ${ADDON_SCRIPTS} \
    mod-blacklist \
    "

RDEPENDS:packagegroup-support-utils-base = "\
    libinput \
    libinput-bin \
    libnl \
    libxml2 \
    "

EXFAT_SUPPORT ?= "False"
EXFAT_SUPPORT:kalama = "True"
EXFAT_SUPPORT:pineapple = "True"

RDEPENDS:packagegroup-filesystem-utils-base = "\
    e2fsprogs \
    e2fsprogs-e2fsck \
    e2fsprogs-mke2fs \
    e2fsprogs-resize2fs \
    e2fsprogs-tune2fs \
    ${@oe.utils.conditional('EXFAT_SUPPORT', 'True', 'exfat-utils', '', d)} \
    "
