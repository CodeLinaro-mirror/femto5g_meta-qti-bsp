inherit native python3native

DESCRIPTION = "Tool from Android to validate and merge dtbo files before creating dtbo.img"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/\
${LICENSE};md5=89aea4e17d99a7cacdbeed46a0096b10"

BBCLASSEXTEND = "native"

DEFAULT_PREFERENCE = "-1"

DEPENDS += "dtc-native"

FILESEXTRAPATHS:prepend := "${KERNEL_PLATFORM_PATH}:"

SRC_URI += "file://build/kernel/android/merge_dtbs.py"
SRC_URI += "file://build/kernel/build-tools/path/linux-x86/ufdt_apply_overlay"

S = "${WORKDIR}/dtc"

do_install() {
    # Install  bin
    install -d ${D}${bindir}/merge_dtbs/
    install -m 0755 ${S}/../build/kernel/android/merge_dtbs.py ${D}${bindir}/merge_dtbs/
    install -m 0755 ${S}/../build/kernel/build-tools/path/linux-x86/ufdt_apply_overlay ${D}${bindir}/merge_dtbs/

    # Install fdt tools from dtc-native
    install -m 0755 ${STAGING_BINDIR_NATIVE}/fdtget ${D}${bindir}/merge_dtbs/
    install -m 0755 ${STAGING_BINDIR_NATIVE}/fdtput ${D}${bindir}/merge_dtbs/
    install -m 0755 ${STAGING_BINDIR_NATIVE}/fdtoverlay ${D}${bindir}/merge_dtbs/
    install -m 0755 ${STAGING_BINDIR_NATIVE}/fdtdump ${D}${bindir}/merge_dtbs/

    create_wrapper ${D}${bindir}/merge_dtbs/merge_dtbs.py \
        LD_LIBRARY_PATH=${STAGING_BINDIR_NATIVE}/merge_dtbs/lib:$LD_LIBRARY_PATH \
        PATH=${STAGING_BINDIR_NATIVE}/merge_dtbs/:$PATH

}
