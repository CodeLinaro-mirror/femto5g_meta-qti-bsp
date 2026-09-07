# Provides packages required to build
# QTI Ramdisk archive with systemd as init

LICENSE = "BSD-3-Clause"

# Ramdisk image generation doesn't need abl
EXTRA_IMAGEDEPENDS:remove = "edk2"

PACKAGE_INSTALL = "\
    adbd \
    usb-composition \
    usb-composition-usbd \
    ${VIRTUAL-RUNTIME_base-utils} \
    ext4-utils \
    ${@d.getVar('kern_mods')} \
    fsmgr \
    first-stage-scripts-init \
    gki-kernel-modules-linkmodulesload \
    glib-2.0 \
    glibc \
    initrd-release \
    libbase \
    libcutils \
    libgcc \
    liblog \
    logwrapper \
    packagegroup-core-boot \
    ${@bb.utils.contains('DISTRO_FEATURES','full-disk-encryption', 'securemsmdlkm', '', d)} \
    udev \
    ${@bb.utils.contains('DISTRO_FEATURES', 'selinux', 'libselinux libpcre', '', d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'dm-verity', bb.utils.contains('MACHINE_FEATURES', 'dm-verity-initramfs-v3', 'cryptsetup verity-scripts lvm2-udevrules', '', d), '', d)} \
"

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES}"
IMAGE_NAME_SUFFIX = ""
IMAGE_FEATURES = ""
IMAGE_LINGUAS = ""

# Set default target to initrd.target
SYSTEMD_DEFAULT_TARGET = "initrd.target"

inherit core-image

#do_rootfs[nostamp] = "1"  # workaround for linux-soc-repo nostamp (fixed)

# Add dependency on vendor ramdisk
python () {
    if ((int(d.getVar("BOOT_HEADER_VERSION") or "0") >= 3) and (d.getVar("SKIP_VENDOR_BOOT") or "True") == "False"):
        d.setVar("kern_mods", "")
        d.appendVarFlag('do_image', 'depends', ' ${VENDOR_INITRAMFS_IMAGE}:do_image_complete')
    else:
        d.setVar("kern_mods", "gki-kernel-modules-first-stage")
}

COMMON_REMOVE_PACKAGES = "\
    adbd \
    usb-composition \
    usb-composition-usbd \
    first-stage-scripts-init \
"

PACKAGE_INSTALL:remove:pineapple = "${COMMON_REMOVE_PACKAGES}"
PACKAGE_INSTALL:remove:sun = "${COMMON_REMOVE_PACKAGES}"
PACKAGE_INSTALL:remove:kera = "${COMMON_REMOVE_PACKAGES}"
PACKAGE_INSTALL:remove:alor = "${COMMON_REMOVE_PACKAGES}"
PACKAGE_INSTALL:remove:vienna = "${COMMON_REMOVE_PACKAGES}"
