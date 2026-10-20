DESCRIPTION = "Tool used for creating boot image"
LICENSE = "Apache-2.0 & MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10 \
                    file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

PROVIDES = "virtual/mkbootimg"

BBCLASSEXTEND = "native"

# Add Python dependency for the scripts
RDEPENDS:${PN}-native += "python3-core"

FILESEXTRAPATHS:prepend := "${KERNEL_PLATFORM_PATH}/prebuilts/kernel-build-tools/${KERNEL_BUILD_TOOLS_ARCH}/:"
FILESEXTRAPATHS:prepend := "${KERNEL_PLATFORM_PATH}/bootable/libbootloader/gbl/libbootimg/mkbootimg/:"
SRC_URI = "file://bin/mkbootimg"
SRC_URI += "file://include/bootimg"
SRC_URI += "file://lib64/libc_musl.so"
S = "${WORKDIR}"
do_compile[noexec] = "1"
do_configure[noexec] = "1"

do_install:class-native () {
    install -d ${D}${bindir}
    cp ${S}/bin/mkbootimg ${D}${bindir}/
    cp -r ${S}/lib64 ${D}${bindir}/
}

do_install:class-target() {
    if [ -f ${S}/include/bootimg/bootimg.h ]; then
        install -d ${D}${includedir}
        install ${S}/include/bootimg/bootimg.h ${D}${includedir}/bootimg.h
    else
        bbwarn "bootimg.h file not found, header will not be installed"
    fi
}
