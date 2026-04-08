FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
SRC_URI:append = " \
             ${@bb.utils.contains('PREFERRED_VERSION_linux-msm', '5.15', 'file://0032-systemd-add-bootkpi-marker-for-user-session.patch', '', d)} \
             ${@bb.utils.contains('DISTRO_FEATURES', 'early_init', 'file://0034-systemd-add-handover-support-for-early-service.patch', '', d)} \
             file://power-switch.rules \
             file://0001-journald-disable-audit-support-completely-from-the-j.patch \
             file://0036-systemd-journald-optimize-kmsg-reading-performance.patch \
             ${@bb.utils.contains_any('MACHINE_FEATURES', 'qti-umd', '', 'file://qti_sleep.sh', d)} \
             ${@bb.utils.contains('DISTRO_FEATURES', 'qti-rumi', 'file://0031-udev-trigger-only-enable-must-part-while-leave-other.patch', '', d)} \
             ${@bb.utils.contains_any('MACHINE_FEATURES', 'qti-hypervisor qti-gunyah', 'file://0001-systemd-sleep-change-suspend-state-list.patch', '', d)} \
             ${@bb.utils.contains('MACHINE_FEATURES', 'deepsleep', 'file://0002-systemd-add-deepsleep-support.patch', '', d)} \
             ${@bb.utils.contains('MACHINE_FEATURES', 'qti-umd', 'file://0001-systemd-shutdown-shorten-file-sync-timeout.patch', '', d)} \
             ${@bb.utils.contains('MACHINE_FEATURES', 'qti-umd', 'file://0001-systemd-Remove-systemd-watchdog-ping-condition.patch', '', d)} \
             ${@bb.utils.contains('MACHINE_FEATURES', 'qti-umd', 'file://0035-systemd-Make-systemd-init-run-in-high-priority.patch', '', d)} \
             ${@bb.utils.contains('MACHINE_FEATURES', 'qti-umd', 'file://0037-systemd-Add-wdt_ping-in-dispatch_runqueue.patch', '', d)} \
             ${@bb.utils.contains('MACHINE_FEATURES', 'qti-umd', bb.utils.contains('MACHINE_FEATURES', 'qti-gunyah', '', 'file://qti_lxc_umd_sleep.sh', d), '', d)} \
             ${@bb.utils.contains('MACHINE_FEATURES', 'qti-gunyah', 'file://0001-modules-load-implement-parallel-module-loading.patch', '', d)}"

SRC_URI:append:sa8775 = " \
             ${@bb.utils.contains('MACHINE_FEATURES', 'early-ramdisk-init', 'file://0001-change-systemd-modules-load-service-type-to-simple.patch', '', d)}"

do_install:append() {
   if ${@bb.utils.contains_any('MACHINE_FEATURES', 'qti-umd', 'false', 'true', d)} ; then
      install -d ${D}/${base_libdir}/systemd/system-sleep
      install -m 0755 ${WORKDIR}/qti_sleep.sh -D ${D}/${base_libdir}/systemd/system-sleep/qti_sleep.sh
   fi

   if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-umd', 'true', 'false', d)} ; then
      if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-gunyah', 'false', 'true', d)} ; then
         install -m 0755 ${WORKDIR}/qti_lxc_umd_sleep.sh -D ${D}/${base_libdir}/systemd/system-sleep/qti_lxc_umd_sleep.sh
      fi
   fi
}
