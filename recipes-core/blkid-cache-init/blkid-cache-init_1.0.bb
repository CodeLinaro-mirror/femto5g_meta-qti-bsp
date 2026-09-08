SUMMARY = "Pre-populate blkid cache for root device at boot"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause-Clear;md5=7a434440b651f4a472ca93716d01033a"

inherit systemd

SRC_URI = "file://blkid-cache-init.sh \
           file://blkid-cache-init.service"

SYSTEMD_SERVICE:${PN} = "blkid-cache-init.service"
SYSTEMD_AUTO_ENABLE = "enable"

do_install() {
    install -d ${D}${libexecdir}
    install -m 0755 ${WORKDIR}/blkid-cache-init.sh \
        ${D}${libexecdir}/blkid-cache-init.sh

    install -d ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/blkid-cache-init.service \
        ${D}${systemd_unitdir}/system/blkid-cache-init.service
}

FILES:${PN} = "${libexecdir}/blkid-cache-init.sh \
               ${systemd_unitdir}/system/blkid-cache-init.service"

PACKAGE_ARCH = "${MACHINE_ARCH}"
