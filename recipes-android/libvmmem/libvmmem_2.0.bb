inherit autotools-brokensep pkgconfig qprebuilt

DESCRIPTION      = "Build Android libvmmem for LE"
HOMEPAGE         = "http://support.cdmatech.com"
LICENSE          = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/\
${LICENSE};md5=7a434440b651f4a472ca93716d01033a"

PR = "r1"

FILESEXTRAPATHS:prepend := "${WORKSPACE}/system/memory/:"
SRC_URI   = "file://libvmmem"

S = "${WORKDIR}/libvmmem"
DEPENDS += "liblog libbase linux-msm-headers glib-2.0"

EXTRA_OECONF = " --with-sanitized-headers=${STAGING_INCDIR}/linux-msm/usr/include \
                 --disable-static "
EXTRA_OECONF += " --with-glib"
EXTRA_OECONF:remove:echo = "--with-glib"

PACKAGES +="${PN}-test-bin"

FILES:${PN}     = "${libdir}/* ${sysconfdir}/*"
FILES:${PN}-test-bin = "${base_bindir}/*"

PACKAGE_ARCH = "${MACHINE_ARCH}"
