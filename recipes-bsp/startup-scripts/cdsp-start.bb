DESCRIPTION = "Start up service for cdsp"
HOMEPAGE    = "http://codelinaro.org"
LICENSE     = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-qti-bsp/files/common-licenses/${LICENSE};md5=3771d4920bd6cdb8cbdf1e8344489ee0"

SRC_URI  = "file://cdsp_start.service"

inherit systemd

do_install() {
    install -m 0644 -D ${WORKDIR}/cdsp_start.service  ${D}${systemd_unitdir}/system/cdsp_start.service
}

do_install:append:seraph() {
    sed -i 's|ExecStart=.*|ExecStart=/bin/sh -c '\''for d in /sys/class/remoteproc/remoteproc*/; do if [ "$(cat "$d/name")" == "32300000.remoteproc-cdsp" ]; then echo start > "$d/state"; fi; done'\''|' \
        ${D}${systemd_unitdir}/system/cdsp_start.service
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

SYSTEMD_SERVICE:${PN} = "cdsp_start.service"
SYSTEMD_SERVICE:${PN}:vienna = ""

FILES:${PN} += "${systemd_unitdir}/system"
