SUMMARY = "QTI headless image"
DESCRIPTION = "QTI headless image for basic platform support without Multimedia/Connectivity modules"
LICENSE = "BSD-3-Clause-Clear"

DEPENDS += "ext4-utils-native mkbootimg-native"

inherit core-image
# Introducing selinux-image.bbclass is to label selinux contexts when build.
inherit ${@bb.utils.contains('DISTRO_FEATURES', 'selinux', 'selinux-image', '', d)}

# Set up for handling the generation of the /usr image
# partition...
require recipes-products/images/automotive-usr-image.inc

# Set up for handling the generation of the /persist image
require recipes-products/images/automotive-persist-image.inc

IMAGE_ROOTFS_SIZE = "716800"

IMAGE_INSTALL += "\
    platformdlkm \
    system-core-adbd \
    system-core-leprop \
    system-core-post-boot \
    system-core-usb \
    system-prop \
    bridge-utils \
    ${@bb.utils.contains('MACHINE_FEATURES', 'qti-hypervisor', 'setup-network', '', d)} \
    net-tools \
    iproute2 \
    iproute2-ss \
    procps \
    iproute2-tc \
    vlan \
    xinetd \
    tcp-wrappers \
    libcap \
    libcap-bin \
    attr \
    abctl \
    ab-status-updater \
    ${@bb.utils.contains('PREFERRED_VERSION_linux-msm', '6.12', 'kernel-modules', '', d)} \
    ${@bb.utils.contains('PREFERRED_PROVIDER_virtual/kernel', 'linux-ack', 'soc-modules', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'qti-security', 'securemsmdlkm', '', d)}  \
    ${@bb.utils.contains('DISTRO_FEATURES', 'qti-security', bb.utils.contains_any('PREFERRED_VERSION_linux-msm', '5.15 6.1', '', 'securemsm-devicetree', d), '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'qti-fde', 'enable-fde', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'selinux', 'packagegroup-selinux-minimal packagegroup-selinux-policycoreutils checkpolicy secilc auditd selinux-relabelvar selinux-relabeldata', '', d)} \
"

IMAGE_INSTALL:append:quin-tgvm-gen4-5 = " ${@bb.utils.contains('DISTRO_FEATURES', 'qti-security', 'optee-libckteec', '', d)}"

IMAGE_LINGUAS = ""

EXTRA_IMAGECMD:ext4 = "-i 4096 -b 4096"

remove_data_bind() {
    sed -i '/^\/data/d' ${IMAGE_ROOTFS}/etc/fstab
}

ROOTFS_POSTPROCESS_COMMAND:append = " remove_data_bind;"

# Makes image suitable for development (e.g. enable ssh for login, allows root logins and logins without passwords by ssh)
IMAGE_FEATURES:append = " ${@bb.utils.contains('VARIANT', 'debug', 'debug-tweaks ssh-server-openssh', '', d)}"

INCOMPATIBLE_LICENSE = "GPL-3.0* LGPL-3.0* AGPL-3.0*"
