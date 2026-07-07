SUMMARY = "Enable FDE on data partition"
DESCRIPTION = "Partition name which wants to encrypt will send through hab channel to host and host will generate key and encrypt the partition"
HOMEPAGE = "http://git.codelinaro.org/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=550794465ba0ec5312d6919e203a55f9"

SRC_URI = "\
           file://enable-fde.sh \
           file://enable-fde.service \
"

inherit systemd useradd

SYSTEMD_SERVICE:${PN} = "enable-fde.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

USERADD_PACKAGES = "${PN}"
GROUPADD_PARAM:${PN} = "security"
USERADD_PARAM:${PN} = "--no-create-home -g security --shell /bin/false fde"

do_install:append () {
  install -d ${D}${systemd_system_unitdir}
  install -d ${D}${bindir}
  install -m 0755 ${WORKDIR}/enable-fde.sh ${D}${bindir}/enable-fde.sh
  install -m 0644 ${WORKDIR}/enable-fde.service ${D}${systemd_unitdir}/system/
  if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-hypervisor', 'true', 'false', d)}; then
      sed -i 's/Before=data.mount systemd-fsck@dev-vdb.service/Before=data.mount/' ${D}${systemd_system_unitdir}/enable-fde.service
  fi
}

RDEPENDS:${PN} += "e2fsprogs-mke2fs"
