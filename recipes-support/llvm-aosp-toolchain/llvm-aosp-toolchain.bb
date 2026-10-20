DESCRIPTION = "AOSP clang/llvm compiler"
LICENSE = "Apache-2.0 WITH LLVM-exception & MIT & NCSA"

# Allow machine/distro to select a different prebuilt clang drop.
AOSP_LLVM_VERSION ?= "clang-r584948"

FILESEXTRAPATHS:prepend := "${WORKSPACE}/prebuilts/clang/host/linux-x86:"

SRC_URI = "file://${AOSP_LLVM_VERSION}"
S = "${WORKDIR}/${AOSP_LLVM_VERSION}"

INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    install -d ${D}${bindir}/llvm-aosp-toolchain/bin/
    for file in `cd ${S}/bin && find -L . -type f`; do
        install -m 0755 ${S}/bin/$file ${D}${bindir}/llvm-aosp-toolchain/bin
    done
    install -d ${D}${bindir}/llvm-aosp-toolchain/lib/
    cd ${S}/lib
    for file in `find -L . -type f`; do
        install -D -m 0644 $file ${D}${bindir}/llvm-aosp-toolchain/lib/$file
    done
}

RDEPENDS:${PN}:class-nativesdk += " libgcc zlib "

BBCLASSEXTEND = " native nativesdk"

