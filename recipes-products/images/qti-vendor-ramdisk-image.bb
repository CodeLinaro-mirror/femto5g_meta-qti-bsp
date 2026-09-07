# Provides packages required to build QTI vendor Ramdisk archive

LICENSE = "BSD-3-Clause"

inherit core-image

do_rootfs[nostamp] = "1"

# Ramdisk image generation doesn't need abl
EXTRA_IMAGEDEPENDS:remove = "edk2"

PACKAGE_INSTALL = "\
    gki-kernel-modules-first-stage \
    first-stage-scripts \
    ${@bb.utils.contains('MACHINE_FEATURES', 'dm-verity-initramfs-v2', 'cryptsetup verity-scripts lvm2-udevrules', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'qti-avb', 'cryptsetup avb-verity-scripts lvm2-udevrules', '', d)} \
"

IMAGE_FSTYPES = "${INITRAMFS_FSTYPES}"
IMAGE_NAME_SUFFIX = ""
IMAGE_FEATURES = ""
IMAGE_LINGUAS = ""
