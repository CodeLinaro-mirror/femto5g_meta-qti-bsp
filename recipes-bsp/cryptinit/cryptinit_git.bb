DESCRIPTION = "DM-Crypt Initialization"
HOMEPAGE         = "https://git.codelinaro.org/"
LICENSE     = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-qti-bsp/files/common-licenses/${LICENSE};md5=3771d4920bd6cdb8cbdf1e8344489ee0"

PR = "r1"

inherit pkgconfig

FILESPATH =+ "${WORKSPACE}/files:"
SRC_URI = " \
    file://cryptinit.service \
    file://cryptinit.sh \
    file://cryptshutdown.sh \
"

S = "${WORKDIR}"

do_unpack[deptask] = "do_populate_sysroot"

FILES:${PN} = "${bindir}/* ${systemd_unitdir}/system"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system/
    install -d ${D}${systemd_unitdir}/system/multi-user.target.wants/
    install -m 0755 ${S}/cryptinit.sh -D ${D}/${bindir}/cryptinit.sh
    install -m 0755 ${S}/cryptshutdown.sh -D ${D}/${bindir}/cryptshutdown.sh
    install -m 0644 ${S}/cryptinit.service -D ${D}${systemd_unitdir}/system/cryptinit.service
    ln -sf ${systemd_unitdir}/system/cryptinit.service ${D}${systemd_unitdir}/system/multi-user.target.wants/cryptinit.service
}
