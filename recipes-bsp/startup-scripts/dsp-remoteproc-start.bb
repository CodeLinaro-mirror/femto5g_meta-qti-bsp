DESCRIPTION = "Start up service for dsp remoteproc"
HOMEPAGE    = "http://codelinaro.org"
LICENSE     = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-qti-bsp/files/common-licenses/${LICENSE};md5=3771d4920bd6cdb8cbdf1e8344489ee0"

SRC_URI  = "file://dsp_remoteproc.service"
SRC_URI += "file://dsp_remoteproc_start.sh"

inherit systemd

do_install() {
    install -m 0644 -D ${WORKDIR}/dsp_remoteproc.service   ${D}${systemd_unitdir}/system/dsp_remoteproc.service
    install -m 0755 -D ${WORKDIR}/dsp_remoteproc_start.sh  ${D}${libexecdir}/dsp_remoteproc_start.sh
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

SYSTEMD_SERVICE:${PN} = "dsp_remoteproc.service"

FILES:${PN} += "${systemd_unitdir}/system"
FILES:${PN} += "${libexecdir}"
