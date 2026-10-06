inherit pkgconfig autotools native

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/\
${LICENSE};md5=89aea4e17d99a7cacdbeed46a0096b10"
HOMEPAGE = "https://www.codeaurora.org/gitweb/quic/la?p=platform/bootable/recovery.git"

PR = "r1"

DEPENDS += "bzip2-native zlib-native"
RDEPENDS:${PN} = "zlib"

FILESEXTRAPATHS:prepend := "${WORKSPACE_SOURCES}/OTA/recovery/:"
SRC_URI = "file://applypatch"

S = "${UNPACKDIR}/applypatch"

do_compile:prepend() {
    # libbz2.so is available, but libbz2.a is not.
    # Remove forced static linking from the generated makefiles.
    sed -i 's/-all-static//g' \
        ${S}/Makefile \
        ${S}/Makefile.in \
        ${S}/Makefile.am 2>/dev/null || true
}
