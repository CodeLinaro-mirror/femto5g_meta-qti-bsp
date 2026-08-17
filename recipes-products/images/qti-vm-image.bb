inherit qimage populate_sdk_qti_prop ${@bb.utils.contains('MACHINE_FEATURES', 'dm-verity-initramfs', 'qramdisk', 'qcpioimage', d)}

DEPENDS += " virtual/kernel"

ENABLE_DISPLAY = "${@d.getVar('MACHINE_SUPPORTS_DISPLAY') or "True"}"
ENABLE_TOUCH = "${@d.getVar('MACHINE_SUPPORTS_TOUCH') or "True"}"
ENABLE_SECUREMSM = "${@d.getVar('MACHINE_SUPPORTS_SECUREMSM') or "True"}"
ENABLE_MINK = "${@d.getVar('MACHINE_SUPPORTS_MINK') or "True"}"

CORE_IMAGE_EXTRA_INSTALL += " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'selinux', 'packagegroup-selinux-minimal', '', d)} \
    post-boot \
    sdcard-scripts-automount \
    e2fsprogs-mke2fs \
    procrank \
"

CORE_IMAGE_EXTRA_INSTALL += " ${@bb.utils.contains('MACHINE_FEATURES', 'qti-vm-idv', 'libturbojpeg', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@bb.utils.contains('MACHINE_FEATURES', 'qti-vm-persist', 'packagegroup-qti-encryption', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@oe.utils.conditional('ENABLE_DISPLAY', 'True', 'packagegroup-qti-display', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@oe.utils.conditional('ENABLE_TOUCH', 'True', 'packagegroup-qti-touch', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@oe.utils.conditional('ENABLE_SECUREMSM', 'True', 'packagegroup-qti-securemsm', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@oe.utils.conditional('ENABLE_MINK', 'True', 'packagegroup-qti-mink', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@bb.utils.contains_any('MACHINE', 'trustedvm-v3 trustedvm-v4 trustedvm-v2-2', 'dsp-devicetree', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@bb.utils.contains_any('MACHINE', 'trustedvm-v2 trustedvm-v2-1 trustedvm-v3 trustedvm-v4 trustedvm-v2-2 trustedvm-neo', 'fastrpc-kernel', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@bb.utils.contains('MACHINE_FEATURES', 'vm-dynamic-memresize', 'psi-daemon', '', d)}"

#Exclude packages
PACKAGE_EXCLUDE += "readline"
ROOTFS_POSTPROCESS_COMMAND:remove = " do_fsconfig;"
USE_DEPMOD = "0"

do_gen_partition_bin[noexec] = "1"

IMAGE_FEATURES[validitems] += "vm"
IMAGE_FEATURES += "vm"

do_compose_vmimage[recrdeptask] = "do_ramdisk_create"
do_compose_vmimage[recrdeptask] += "do_merge_dtbs"
do_compose_vmimage[recrdeptask] += "do_extracpio_create"

do_makesystem:prepend() {
	rm -rf ${IMAGE_ROOTFS_EXT4}/usr/lib/python3.10
}

