#Add qti specific changes only when qt-disro is enabled.
QTI_SYSTEMD_INC = ""
QTI_SYSTEMD_INC:qti-distro-base = "${THISDIR}/qti-systemd.inc"
include ${QTI_SYSTEMD_INC}

ILESEXTRAPATHS:prepend := "${THISDIR}/systemd:"

SRC_URI += " \
    file://tmpfiles-setup-dev-override.conf \
    file://0001-Skip-Selinux-Check.patch \
"


do_install:append() {
    install -d ${D}${sysconfdir}/systemd/system/systemd-tmpfiles-setup-dev.service.d
    install -m 0644 ${WORKDIR}/tmpfiles-setup-dev-override.conf \
        ${D}${sysconfdir}/systemd/system/systemd-tmpfiles-setup-dev.service.d/override.conf


    sed -i '/group:wheel/d' ${D}${exec_prefix}/lib/tmpfiles.d/systemd.conf
    if ${@bb.utils.contains_any('MACHINE_FEATURES', 'qti-vm', 'true', 'false', d)}; then
        sed -i 's/#children_max=/children_max=2/' ${D}/etc/udev/udev.conf
    fi
}

do_install:append:mdm9607() {
   #  Mask journaling services by default.
   #  'systemctl unmask' can be used on device to enable them if needed.
   ln -sf /dev/null ${D}${systemd_unitdir}/system/systemd-journald.service
   ln -sf /dev/null ${D}${systemd_unitdir}/system/sysinit.target.wants/systemd-journal-flush.service
   ln -sf /dev/null ${D}${systemd_unitdir}/system/sysinit.target.wants/systemd-journal-catalog-update.service
}
