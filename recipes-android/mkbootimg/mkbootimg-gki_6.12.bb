DESCRIPTION = "Tool used for creating boot image"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/${LICENSE};md5=89aea4e17d99a7cacdbeed46a0096b10"

PROVIDES = "virtual/mkbootimg"

BBCLASSEXTEND = "native"

# Add Python dependency for the scripts
RDEPENDS:${PN}-native += "python3-core"

FILESEXTRAPATHS:prepend := "${WORKSPACE}/kernel-${MSM_KERNEL_VERSION}/kernel_platform/tools/mkbootimg/:"
SRC_URI = "file://mkbootimg.py"
SRC_URI += "file://include"
SRC_URI += "file://gki/generate_gki_certificate.py"

S = "${WORKDIR}"
do_compile[noexec] = "1"
do_configure[noexec] = "1"

do_install:class-native () {
    install -d ${D}${bindir}/scripts/
    cp ${S}/mkbootimg.py ${D}${bindir}/scripts/
    install -d ${D}${bindir}/scripts/gki/
    cp ${S}/gki/generate_gki_certificate.py ${D}${bindir}/scripts/gki/
}

do_install:class-target() {
    if [ -f ${S}/include/bootimg/bootimg.h ]; then
        install -d ${D}${includedir}
        install ${S}/include/bootimg/bootimg.h ${D}${includedir}/bootimg.h
    else
        bbwarn "bootimg.h file not found, header will not be installed"
    fi
}
