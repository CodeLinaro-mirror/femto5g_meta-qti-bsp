# Provides packages required to build
# QTI Generic Linux image.

QTI_GENERIC_IMAGE_SDK_CLASS = "populate_sdk_ext"
QTI_GENERIC_IMAGE_SDK_CLASS:qcs610-odk-64 = "populate_sdk_qti"

inherit qimage ${QTI_GENERIC_IMAGE_SDK_CLASS}

IMAGE_FEATURES += "ssh-server-openssh"

# This image doesn't support abl generation
EXTRA_IMAGEDEPENDS:remove = "edk2"
EXTRA_IMAGEDEPENDS:remove = "lk"

KERN_MODS ?= "kernel-modules"
KERN_MODS:waipio = "gki-kernel-modules-second-stage"
KERN_MODS:seraph = "gki-kernel-modules-second-stage"

CORE_IMAGE_EXTRA_INSTALL += "\
        glib-2.0 \
        ${KERN_MODS} \
        packagegroup-android-utils-base \
        packagegroup-filesystem-utils-base \
        packagegroup-startup-scripts-base \
        systemd-machine-units \
        ${@bb.utils.contains('DISTRO_FEATURES','selinux', 'packagegroup-selinux-minimal', '', d)} \
	${@bb.utils.contains('MACHINE_FEATURES', 'nand-boot', 'mtd-utils-ubifs', '', d)} \
"
# Install display packages
CORE_IMAGE_EXTRA_INSTALL += " \
            libdrm \
            wayland \
            "

# Force default usb composition to 4EE7 as this image can't
# support diag, which is expected by all other compositions.
USBCOMPOSITION:forcevariable = "4EE7"

python copy_buildsystem:append() {
    if d.getVar('MACHINE') != 'qcs610-odk-64':
        # Create src directory in extensible SDK to copy the project sources
        bb.utils.mkdirhier(baseoutpath + '/src')
        # Enable the use of WORKSPACE variable on an extensible SDK
        with open(baseoutpath + '/conf/bblayers.conf', 'a') as f:
            f.write('WORKSPACE = "$' + '{TOPDIR}/src"\n')
}
