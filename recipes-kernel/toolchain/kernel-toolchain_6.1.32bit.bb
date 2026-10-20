DESCRIPTION = "Clang based 32 bit toolchain to compile QTI kernel"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://NOTICE;md5=eeec5cfa0edfb54bfdba757236c7b531"

PROVIDES = "virtual/kernel-toolchain-native"

FILESEXTRAPATHS:prepend := "${WORKSPACE}/kernel-6.1/kernel_platform/prebuilts/clang/host/linux-x86/:"
SRC_URI    = "file://clang-${CLANG_VERSION}"
CLANG_VERSION = "r487747c"

S = "${WORKDIR}/clang-${CLANG_VERSION}"
INHIBIT_SYSROOT_STRIP = "1"
do_compile[noexec] = "1"
do_configure[noexec] = "1"

BBCLASSEXTEND = " native"

do_install() {
    install -d ${D}/${bindir}/clang/
    install -d ${D}/${bindir}/clang/bin/
    cp -rf ${S}/bin/* ${D}/${bindir}/clang/bin/
    install -d ${D}/${bindir}/clang/lib/
    cp -rf ${S}/lib/* ${D}/${bindir}/clang/lib/
}
