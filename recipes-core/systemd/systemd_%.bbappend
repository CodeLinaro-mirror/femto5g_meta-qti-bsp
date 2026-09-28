#Add qti specific changes only when qt-disro is enabled.
QTI_SYSTEMD_INC = ""
QTI_SYSTEMD_INC_qti-distro-base = "${THISDIR}/qti-systemd.inc"
include ${QTI_SYSTEMD_INC}
SRC_URI += "file://0001-fix-reset-arguments.patch"
SRC_URI += "${@bb.utils.contains('MACHINE', 'cinder', 'file://0002-shutdown-report-reset-reason-to-faultmgr.patch', '', d)}"
RDEPENDS_${PN} += "${@bb.utils.contains('MACHINE', 'cinder', 'faultdbg', '', d)}"

do_install_append() {
    sed -i '/group:wheel/d' ${D}${exec_prefix}/lib/tmpfiles.d/systemd.conf
    if ${@bb.utils.contains_any('MACHINE', 'cinder', 'true', 'false', d)}; then
        sed -i -e 's/^#RuntimeWatchdogSec=.*$/RuntimeWatchdogSec=30/g' ${D}${sysconfdir}/systemd/system.conf
        sed -i -e 's/^#RebootWatchdogSec=.*$/RebootWatchdogSec=120/g' ${D}${sysconfdir}/systemd/system.conf
    fi
}
