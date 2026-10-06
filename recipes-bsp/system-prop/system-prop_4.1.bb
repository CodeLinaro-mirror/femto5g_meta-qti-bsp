inherit systemd useradd

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI   = "file://${BASEMACHINE}/system.prop"
SRC_URI  += "file://persist-prop.sh"
SRC_URI  += "file://persist-prop.service"

DESCRIPTION = "Script to populate system properties"

LICENSE = "BSD-Source-Code"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/\
${LICENSE};md5=fe8b41221d7524c70688f7d059ff6d87"

USERADD_PACKAGES = "${PN}"

SYSTEMD_PACKAGES = "${@bb.utils.contains('DISTRO_FEATURES','systemd','${PN}','',d)}"
SYSTEMD_SERVICE:${PN} = "${@bb.utils.contains('DISTRO_FEATURES','systemd','persist-prop.service','',d)}"

do_compile() {
    # Remove empty lines and lines starting with '#'
    sed -e 's/#.*$//' -e '/^$/d' ${UNPACKDIR}/${BASEMACHINE}/system.prop > ${S}/build.prop
}

do_install() {
    install -d ${D}
    install -m 0644 ${S}/build.prop ${D}/build.prop
    if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        install -d ${D}${base_sbindir}
        install -m 0755 ${UNPACKDIR}/persist-prop.sh             ${D}${base_sbindir}/persist-prop.sh

        install -d ${D}${systemd_unitdir}/system
        install -d ${D}${systemd_unitdir}/system/multi-user.target.wants/

        install -m 0644 ${UNPACKDIR}/persist-prop.service             ${D}${systemd_unitdir}/system/persist-prop.service

        ln -sf ${systemd_unitdir}/system/persist-prop.service             ${D}${systemd_unitdir}/system/multi-user.target.wants/persist-prop.service
    fi
}

PACKAGES = "${PN}"
FILES:${PN} += "${base_sbindir}/"
FILES:${PN} += "/build.prop"
FILES:${PN} += "${systemd_unitdir}/"
