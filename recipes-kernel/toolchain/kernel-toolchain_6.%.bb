DESCRIPTION = "Clang based toolchain to compile QTI kernel"
LICENSE = "Apache-2.0 & MIT & NCSA"
LIC_FILES_CHKSUM = "file://NOTICE;md5=eeec5cfa0edfb54bfdba757236c7b531"

PROVIDES = "virtual/kernel-toolchain-native"

KERNEL_VERSION = "${@d.getVar('VM_KERNEL_VERSION')}"

CLANG_VERSION ?= "${@'r522817' if d.getVar('KERNEL_VERSION') == '6.7' else 'r510928'}"
CLANG_VERSION:trustedvm-v2 = "clang-r487747c"

FILESEXTRAPATHS:prepend := "${KERNEL_PLATFORM_PATH}/prebuilts/clang/host/linux-x86/:"
SRC_URI    = "file://clang-${CLANG_VERSION}"

S = "${WORKDIR}/clang-${CLANG_VERSION}"
INHIBIT_SYSROOT_STRIP = "1"
# clang lib don't include  debug symbol
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
# clang prebuilts *.so are private to clang and should not enter global shlib
# avoid cause Multiple shlib providers for libc++.so
EXCLUDE_FROM_SHLIBS = "1"
do_compile[noexec] = "1"
do_configure[noexec] = "1"

BBCLASSEXTEND = " native nativesdk"

do_install() {
    install -d ${D}/${bindir}/clang/
    install -d ${D}/${bindir}/clang/bin/
    cp -rf ${S}/bin/* ${D}/${bindir}/clang/bin/
    install -d ${D}/${bindir}/clang/lib/
    cp -rf ${S}/lib/* ${D}/${bindir}/clang/lib/
}
