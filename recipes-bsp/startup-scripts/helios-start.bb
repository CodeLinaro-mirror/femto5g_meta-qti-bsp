DESCRIPTION = "Start up service for helios"
HOMEPAGE    = "http://codelinaro.org"
LICENSE     = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-qti-bsp/files/common-licenses/${LICENSE};md5=3771d4920bd6cdb8cbdf1e8344489ee0"

SRC_URI  = "file://helios_start.service"

inherit systemd

do_install() {
    install -m 0644 -D ${WORKDIR}/helios_start.service  ${D}${systemd_unitdir}/system/helios_start.service
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

SYSTEMD_SERVICE:${PN} = "helios_start.service"

FILES:${PN} += "${systemd_unitdir}/system"
