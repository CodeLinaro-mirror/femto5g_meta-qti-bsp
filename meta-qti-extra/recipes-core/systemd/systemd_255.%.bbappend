FILESBBAPPENDPATH_EXTRA := "${THISDIR}"
FILESEXTRAPATHS:prepend = "${FILESBBAPPENDPATH_EXTRA}/${BP}:"

SRC_URI:append = " \
             ${@bb.utils.contains('MACHINE_FEATURES', 'qti-hypervisor', '', 'file://0001-systemd-assign-prime-core-to-manager_dispatch_load_q.patch', d)} \
             ${@bb.utils.contains('DISTRO_FEATURES', 'early_init', 'file://0034-systemd-add-handover-support-for-early-service.patch', '', d)} \
             file://power-switch.rules \
             file://qti_sleep.sh \
             ${@bb.utils.contains('MACHINE_FEATURES', 'qti-hypervisor', 'file://0001-systemd-sleep-change-suspend-state-list.patch', '', d)} \
             ${@bb.utils.contains('MACHINE_FEATURES', 'deepsleep', 'file://0002-systemd-add-deepsleep-support.patch', '', d)} \
"

do_install:append() {
    install -d ${D}/${base_libdir}/systemd/system-sleep
    install -m 0755 ${WORKDIR}/qti_sleep.sh -D ${D}/${base_libdir}/systemd/system-sleep/qti_sleep.sh

    if ${@bb.utils.contains('DISTRO_FEATURES', 'volatiled-var', 'true', 'false', d)}; then
        sed -i -e 's|^mode_file_path=.*|mode_file_path="/persist/usb"|' ${D}/${base_libdir}/systemd/system-sleep/qti_sleep.sh
    fi
}
