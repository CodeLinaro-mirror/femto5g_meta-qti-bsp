inherit native

DESCRIPTION = "Tool from Android to validate and merge dtbo files before creating dtbo.img"

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=550794465ba0ec5312d6919e203a55f9"

BBCLASSEXTEND = "native"
DEPENDS = "${@bb.utils.contains('MACHINE_USES_KERNEL_PREBUILTS', 'True', '', 'flex-native bison-native', d)}"

FILESEXTRAPATHS:prepend := "${KERNEL_PREBUILT_PATH}:"
FILESEXTRAPATHS:prepend := "${KERNEL_PLATFORM_PATH}:"
FILESEXTRAPATHS:prepend := "${@bb.utils.contains('MACHINE_USES_KERNEL_PREBUILTS', 'True', '', '${KERNEL_PLATFORM_PATH}/external:', d)}"

KERNEL_BUILD_TOOLS_ARCH ?= "linux-x86"

SRC_URI = "${@bb.utils.contains('MACHINE_USES_KERNEL_PREBUILTS', 'True', \
            'file://host', 'file://qcom-dtc/', d)}"
SRC_URI += "file://build/ \
            file://prebuilts/kernel-build-tools/${KERNEL_BUILD_TOOLS_ARCH}"

S = "${@bb.utils.contains('MACHINE_USES_KERNEL_PREBUILTS', 'True', \
            '${WORKDIR}/host', '${WORKDIR}/qcom-dtc', d)}"

EXTRA_OEMAKE = "${@bb.utils.contains('MACHINE_USES_KERNEL_PREBUILTS', 'True', '', 'PKG_CONFIG=false', d)}"
do_compile() {
    if [ "${MACHINE_USES_KERNEL_PREBUILTS}" = "True" ]; then
        bbnote "Skipping compile: using prebuilts"
    else
        oe_runmake -C ${S}
    fi
}

BIN_PATH = "${@bb.utils.contains('MACHINE_USES_KERNEL_PREBUILTS', 'True', '${S}/bin', '${S}', d)}"
LIB_PATH = "${@bb.utils.contains('MACHINE_USES_KERNEL_PREBUILTS', 'True', '${S}/lib', '${S}/libfdt', d)}"
do_install() {
    # Install  bin
    install -d ${D}${bindir}/merge_dtbs/
    if [ -e ${S}/bin/merge_dtbs.py ]; then
        install -m 0755 ${S}/bin/merge_dtbs.py ${D}${bindir}/merge_dtbs/
    else
        install -m 0755 ${S}/../build/kernel/android/merge_dtbs.py ${D}${bindir}/merge_dtbs/
    fi
    if [ -e ${S}/bin/ufdt_apply_overlay ]; then
        install -m 0755 ${S}/bin/ufdt_apply_overlay  ${D}${bindir}/merge_dtbs/
    else
        install -m 0755 ${S}/../prebuilts/kernel-build-tools/${KERNEL_BUILD_TOOLS_ARCH}/bin/ufdt_apply_overlay  ${D}${bindir}/merge_dtbs/
    fi

    install -m 0755 ${BIN_PATH}/fdtget  ${D}${bindir}/merge_dtbs/
    install -m 0755 ${BIN_PATH}/fdtoverlaymerge  ${D}${bindir}/merge_dtbs/
    install -m 0755 ${BIN_PATH}/fdtdump  ${D}${bindir}/merge_dtbs/
    install -m 0755 ${BIN_PATH}/fdtoverlay  ${D}${bindir}/merge_dtbs/
    install -m 0755 ${BIN_PATH}/fdtput ${D}${bindir}/merge_dtbs/
    install -d ${D}${bindir}/merge_dtbs/lib/
    install -m 0644 ${LIB_PATH}/* ${D}${bindir}/merge_dtbs/lib/

    create_wrapper ${D}${bindir}/merge_dtbs/merge_dtbs.py \
        LD_LIBRARY_PATH=${STAGING_BINDIR_NATIVE}/merge_dtbs/lib:$LD_LIBRARY_PATH \
        PATH=${STAGING_BINDIR_NATIVE}/merge_dtbs/:$PATH

}