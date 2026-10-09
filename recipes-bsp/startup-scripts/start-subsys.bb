DESCRIPTION = "Start up service for subsystems (soccp/adsp/cdsp) via remoteproc"
HOMEPAGE    = "http://codelinaro.org"
LICENSE     = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-qti-bsp/files/common-licenses/${LICENSE};md5=3771d4920bd6cdb8cbdf1e8344489ee0"

SRC_URI  = "file://start_subsys.service \
            file://start_subsys.sh"

inherit systemd

do_install() {
    install -m 0755 -D ${WORKDIR}/start_subsys.sh        ${D}${bindir}/start_subsys.sh
    install -m 0644 -D ${WORKDIR}/start_subsys.service   ${D}${systemd_unitdir}/system/start_subsys.service
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

SYSTEMD_SERVICE:${PN} = "start_subsys.service"

FILES:${PN} += "${systemd_unitdir}/system"
