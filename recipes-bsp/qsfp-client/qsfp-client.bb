SUMMARY = "QSFP28 25G client port bring-up service"
DESCRIPTION = "Systemd oneshot service and helper script to bring up the QSFP28 \
25G client port against the LX2160A ClearFog-CX peer, tunable via /etc/qsfp-client.conf."
LICENSE          = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-qti-bsp/files/common-licenses/\
${LICENSE};md5=3771d4920bd6cdb8cbdf1e8344489ee0"

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

SRC_URI  = "file://qsfp-client-up.sh"
SRC_URI += "file://qsfp-client.service"
SRC_URI += "file://qsfp-client.conf"

S = "${WORKDIR}"

inherit systemd

SYSTEMD_PACKAGES = "${@bb.utils.contains('DISTRO_FEATURES','systemd','${PN}','',d)}"
SYSTEMD_SERVICE_${PN} = "${@bb.utils.contains('DISTRO_FEATURES','systemd','qsfp-client.service','',d)}"

RDEPENDS_${PN} += "iproute2 ethtool"

do_install() {
    install -m 0755 ${WORKDIR}/qsfp-client-up.sh -D ${D}${sbindir}/qsfp-client-up.sh

    install -d ${D}${sysconfdir}
    install -m 0644 ${WORKDIR}/qsfp-client.conf ${D}${sysconfdir}/qsfp-client.conf

    if ${@bb.utils.contains('DISTRO_FEATURES','systemd','true','false',d)}; then
        install -d ${D}${systemd_unitdir}/system
        install -m 0644 ${WORKDIR}/qsfp-client.service ${D}${systemd_unitdir}/system/
    fi
}

PACKAGES = "${PN}"
FILES_${PN} += "${sbindir}/qsfp-client-up.sh"
FILES_${PN} += "${systemd_unitdir}/"
FILES_${PN} += "${sysconfdir}/qsfp-client.conf"
