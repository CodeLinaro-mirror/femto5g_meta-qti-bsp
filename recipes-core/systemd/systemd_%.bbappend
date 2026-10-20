#Add qti specific changes only when qt-disro is enabled.
QTI_SYSTEMD_INC = ""
QTI_SYSTEMD_INC:qti-distro-base = "${THISDIR}/qti-systemd.inc"
include ${QTI_SYSTEMD_INC}

SRC_URI:append = " file://platform.rules"
SRC_URI:append = " file://59-ota-optimization.rules"

do_install:append() {
    sed -i '/group:wheel/d' ${D}${exec_prefix}/lib/tmpfiles.d/systemd.conf
    if ${@bb.utils.contains_any('MACHINE_FEATURES', 'qti-vm', 'true', 'false', d)}; then
        sed -i 's/#children_max=/children_max=2/' ${D}/etc/udev/udev.conf
    fi
    install -m 0644 ${WORKDIR}/platform.rules -D ${D}${sysconfdir}/udev/rules.d/platform.rules
}

do_install:append:mdm9607() {
   #  Mask journaling services by default.
   #  'systemctl unmask' can be used on device to enable them if needed.
   ln -sf /dev/null ${D}${systemd_unitdir}/system/systemd-journald.service
   ln -sf /dev/null ${D}${systemd_unitdir}/system/sysinit.target.wants/systemd-journal-flush.service
   ln -sf /dev/null ${D}${systemd_unitdir}/system/sysinit.target.wants/systemd-journal-catalog-update.service
}

do_install:append:qcs610-odk-64() {
   #  For QCS610-odk-64 Oneshot should be replace as simple
   #  Boot performance improvement for Talos SP 
    sed -i 's/^Type=oneshot$/Type=simple/' ${D}${systemd_unitdir}/system/systemd-modules-load.service
}

do_install:append:vienna() {
   # For Vienna include OTA optimization udev rules
   # to optimize udev worker threads during OTA update
   # and reduce shell reboot time
   install -m 0644 ${WORKDIR}/59-ota-optimization.rules -D ${D}/etc/udev/rules.d/59-ota-optimization.rules

   # Update persistent-storage.rules to check if OTA_ACTIVE
   # flag is set, directly move to "persistent_storage_end"
   # lable
   sed -i '/ACTION=="remove", GOTO="persistent_storage_end"/i ENV{OTA_ACTIVE}=="1", GOTO="persistent_storage_end"' ${D}/${base_libdir}/udev/rules.d/60-persistent-storage.rules
}
