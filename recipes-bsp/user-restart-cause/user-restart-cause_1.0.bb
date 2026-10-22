SUMMARY = "Report a user-initiated restart cause to Fault Manager"
DESCRIPTION = "Systemd reboot-target service that reports USER_INITIATED and the restart timestamp to Fault Manager"
LICENSE          = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-qti-bsp/files/common-licenses/\
${LICENSE};md5=3771d4920bd6cdb8cbdf1e8344489ee0"

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI = "file://user-restart-cause.service"

S = "${WORKDIR}"

inherit systemd

RDEPENDS_${PN} = "coreutils faultdbg"

SYSTEMD_SERVICE_${PN} = "user-restart-cause.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install() {
    install -d ${D}${systemd_system_unitdir}
    install -m 0644 ${WORKDIR}/user-restart-cause.service \
        ${D}${systemd_system_unitdir}/user-restart-cause.service
}

FILES_${PN} += "${systemd_system_unitdir}/user-restart-cause.service"
