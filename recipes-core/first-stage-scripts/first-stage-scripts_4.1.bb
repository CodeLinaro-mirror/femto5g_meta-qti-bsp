SUMMARY = "Scripts to mimic GKI vendor RAM Disk"
DESCRIPTION = "first_stage.sh contains vendor initializations"

LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-qti-bsp/files/common-licenses/\
${LICENSE};md5=3771d4920bd6cdb8cbdf1e8344489ee0"

SRC_URI  +=  "file://run-firststage-script.service"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install () {
  install -m 755 /dev/null ${D}/first_stage.sh
  install -d ${D}${systemd_unitdir}/system/
  install -m 0644 ${WORKDIR}/run-firststage-script.service ${D}${systemd_unitdir}/system/run-firststage-script.service
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
PACKAGES = "${PN} ${PN}-init"
FILES:${PN}      += " /first_stage.sh "
FILES:${PN}-init += " ${systemd_unitdir}/system/ "

inherit systemd

SYSTEMD_PACKAGES = "${PN}-init"
SYSTEMD_SERVICE:${PN}-init = "run-firststage-script.service"
