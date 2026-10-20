DESCRIPTION = "Command-line tool for working with Android Verified Boot images"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=f0f3a517d46b5f0ca048b58f503b6dc1"

BBCLASSEXTEND =+ "native"

PREMIRRORS = ""

SRCREV = "b27a9aa53d2db7720e4d88d6950dcd979da97de3"

SRC_URI = "git://github.com/AndroidBootloader/platform_external_avb.git;branch=master;protocol=https"
S = "${WORKDIR}/git"
SRC_URI[sha256sum] = "34e87265772d7409c6cc422af6da9604b333b2f06d5bf9120e5e34cc0db94723"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${S}/avbtool.py ${D}${bindir}/avbtool.py
    install -d ${D}${sysconfdir}
    install -d ${D}${sysconfdir}/avb
    install -d ${D}${sysconfdir}/avb/sigkeys
    install -m 0755 ${S}/test/data/testkey_rsa4096.pem ${D}${sysconfdir}/avb/sigkeys/testkey_rsa4096.pem
}
