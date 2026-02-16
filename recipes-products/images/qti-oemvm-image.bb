inherit qimage ${@bb.utils.contains('MACHINE_FEATURES', 'dm-verity-initramfs', 'qramdisk', 'qcpioimage', d)}

DEPENDS += " virtual/kernel"

ENABLE_SECUREMSM = "${@d.getVar('MACHINE_SUPPORTS_SECUREMSM') or "True"}"
ENABLE_MINK = "${@d.getVar('MACHINE_SUPPORTS_MINK') or "True"}"
ENABLE_VSOCK_AGENT = "${@d.getVar('MACHINE_SUPPORTS_VSOCK_AGENT') or "False"}"

CORE_IMAGE_EXTRA_INSTALL += " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'selinux', 'packagegroup-selinux-minimal', '', d)} \
    post-boot \
    sdcard-scripts-automount \
    e2fsprogs-mke2fs \
    ${@oe.utils.conditional('ENABLE_VSOCK_AGENT', 'True', 'vsock-guest-agent-oemvm', '', d)} \
"
CORE_IMAGE_EXTRA_INSTALL += " ${@oe.utils.conditional('ENABLE_SECUREMSM', 'True', 'packagegroup-qti-securemsm-oemvm', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@oe.utils.conditional('ENABLE_MINK', 'True', 'packagegroup-qti-mink-oemvm', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@bb.utils.contains('MACHINE_FEATURES', 'qti-vm-persist', 'packagegroup-qti-encryption', '', d)}"

#Exclude packages
PACKAGE_EXCLUDE += "readline"
ROOTFS_POSTPROCESS_COMMAND:remove = " do_fsconfig;"
USE_DEPMOD = "0"

do_gen_partition_bin[noexec] = "1"

IMAGE_FEATURES[validitems] += "vm oemvm"
IMAGE_FEATURES += "vm oemvm"

do_compose_vmimage[recrdeptask] = "do_ramdisk_create"
do_compose_vmimage[recrdeptask] += "do_merge_dtbs"
do_compose_vmimage[recrdeptask] += "do_extracpio_create"

do_makesystem:prepend() {
      rm -rf ${IMAGE_ROOTFS_EXT4}/usr/lib/python3.10
      rm -rf ${IMAGE_ROOTFS_EXT4}/usr/lib/modules/smmu_proxy.ko
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/systemd/system/smmu_proxy.service
}
