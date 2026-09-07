inherit qimage ${@bb.utils.contains('MACHINE_FEATURES', 'dm-verity-initramfs', 'qramdisk', 'qcpioimage', d)}

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
    bash \
    procrank \
    vm-time-init \
"

DEPENDS += " ${@bb.utils.contains_any('MACHINE', 'trustedvm-v4 trustedvm-v5', 'dsp-devicetree', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@bb.utils.contains('MACHINE_FEATURES', 'qti-vm-idv', 'libturbojpeg', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@bb.utils.contains('MACHINE_FEATURES', 'qti-vm-persist', 'packagegroup-qti-encryption', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@oe.utils.conditional('ENABLE_DISPLAY', 'True', 'packagegroup-qti-display', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@oe.utils.conditional('ENABLE_TOUCH', 'True', 'packagegroup-qti-touch', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@oe.utils.conditional('ENABLE_SECUREMSM', 'True', 'packagegroup-qti-securemsm', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@oe.utils.conditional('ENABLE_MINK', 'True', 'packagegroup-qti-mink', '', d)}"
#CORE_IMAGE_EXTRA_INSTALL += " ${@bb.utils.contains_any('MACHINE', 'trustedvm-v3 trustedvm-v4', 'dsp-devicetree', '', d)}"
CORE_IMAGE_EXTRA_INSTALL += " ${@bb.utils.contains_any('MACHINE', 'trustedvm-v2  trustedvm-v3 trustedvm-v4 trustedvm-v5', 'fastrpc-kernel', '', d)}"
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
	rm -rf ${IMAGE_ROOTFS_EXT4}/usr/lib/python3.12
      rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/50-binder.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/50-log.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/99-gpiochardev.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/ashmem.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/automountsdcard.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/ion.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/mmc-rpmb.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/mtpserver.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/set-mhi-nodes.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/set-usb-nodes.rules

      #rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/fastrpc-adsp.rules
      #rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/fastrpc-cdsp.rules
      #rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/fastrpc.rules
      #rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/kgsl.rules
      #rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/kmem.rules
      #rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/platform-internal.rules
      #rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/platform.rules
      #rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/touchscreen.rules
      #rm -rf ${IMAGE_ROOTFS_EXT4}/${sysconfdir}/udev/rules.d/tui.rules

      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/rules.d10-dm.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/11-dm-lvm.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/13-dm-disk.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/60-cdrom_id.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/60-dmi-id.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/60-evdev.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/60-fido-id.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/60-infiniband.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/60-input-id.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/60-persistent-alsa.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/60-persistent-input.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/60-persistent-storage-mtd.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/60-persistent-storage.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/60-persistent-storage-tape.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/60-sensor.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/60-serial.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/64-btrfs.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/69-dm-lvm.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/70-camera.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/70-joystick.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/70-memory.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/70-mouse.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/70-touchpad.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/75-net-description.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/75-probe_mtd.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/78-sound-card.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/80-drivers.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/80-net-setup-link.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/81-net-dhcp.rules
      rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/90-iocost.rules

      #rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/50-udev-default.rules
      #rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/60-autosuspend.rules
      #rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/60-drm.rules
      #rm -rf ${IMAGE_ROOTFS_EXT4}/lib/udev/rules.d/60-block.rules
}

