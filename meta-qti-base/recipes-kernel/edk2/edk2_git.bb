SUMMARY = "edk2"
DESCRIPTION = "UEFI bootloader"
HOMEPAGE = "https://git.codelinaro.org"
LICENSE = "BSD-2-Clause & BSD-3-Clause"
LIC_FILES_CHKSUM = "\
    file://${COMMON_LICENSE_DIR}/BSD-2-Clause;md5=cb641bc04cda31daea161b1bc15da69f \
    file://${COMMON_LICENSE_DIR}/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9 \
"
PROVIDES = "virtual/bootloader"

PR = "r1"
PV = "3.0"

SRC_URI = "${PATH_TO_REPO}/bootable/bootloader/edk2/.git;protocol=${PROTO};destsuffix=bootable/bootloader/edk2;usehead=1"

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/bootable/bootloader/edk2"

inherit deploy

TOOLCHAIN = "clang"

EARLY_ETH = "${@bb.utils.contains('DISTRO_FEATURES', 'qti-early-eth', '1', '0', d)}"
HIBERNATION = "${@bb.utils.contains('COMBINED_FEATURES', 'hibernation', '1', '0', d)}"
DISABLE_NONBOOTDEVICE_ENABLED ?= "0"
DISABLE_NONBOOTDEVICE_ENABLED:sa6155 = "1"
LOAD_KM_SET_ROT ?= "0"
LOAD_KM_SET_ROT:sa8775 = "1"
LOAD_KM_SET_ROT:sa7255 = "1"
LOAD_KM_SET_ROT:sa8797 = "1"
SCMI_UPDATES_NEEDED ?= "0"
SCMI_UPDATES_NEEDED:sa7255 = "1"
SCMI_UPDATES_NEEDED:sa8775 = "1"
SCMI_UPDATES_NEEDED:sa8797 = "1"
PVM_SKIP_DTBO = "${@bb.utils.contains('MACHINE_FEATURES', 'dt-overlay', '0', '1', d)}"

EXTRA_OEMAKE = "'CLANG_BIN=${STAGING_BINDIR_NATIVE}/' \
                'CLANG_PREFIX=${STAGING_BINDIR_NATIVE}/${TARGET_SYS}/${TARGET_PREFIX}' \
                'TARGET_ARCHITECTURE=${TARGET_ARCH}'\
                'BUILDDIR=${S}'\
                'BOOTLOADER_OUT=${S}/out'\
                'ENABLE_LE_VARIANT=true'\
                'HIBERNATION_SUPPORT=${HIBERNATION}'\
                'VERIFIED_BOOT_LE=0'\
                'VERITY_LE=0'\
                'LOAD_KM_AND_SET_ROT=${LOAD_KM_SET_ROT}'\
                'INIT_BIN_LE=\"/sbin/init\"'\
                'EDK_TOOLS_PATH=${S}/BaseTools'\
                'EARLY_ETH_ENABLED=${EARLY_ETH}'\
                'EARLY_ETH_AS_DLKM=1' \
                'UBSAN_UEFI_GCC_FLAG_ALIGNMENT=-Wno-misleading-indentation' \
                'SUPPORT_DISABLE_NON_BOOTDEVICE=${DISABLE_NONBOOTDEVICE_ENABLED}' \
                'TARGET_BOARD_TYPE_AUTO=1' \
                'SCMI_UPDATES_NEEDED=${SCMI_UPDATES_NEEDED}' \
                'PVM_SKIP_DTBO=${PVM_SKIP_DTBO}' \
                ${@bb.utils.contains('DISTRO_FEATURES', 'qti-avb', 'VERIFIED_BOOT_ENABLED=1', '', d)} \
                ${@bb.utils.contains('DISTRO_FEATURES', 'qti-avb', 'VERIFIED_BOOT_2=1', '', d)} "

EXTRA_OEMAKE:append:sa8775 = " 'SUPPORT_AB_BOOT_LXC=1' \
                               'ENABLE_LV_ATOMIC_AB=1' \
                               'ENABLE_SAIL_FLASHING=1' \
                               'BOOTIMAGE_LOAD_VERIFY_IN_PARALLEL=1' \
                               'ENABLE_SAIL_BOOT=1' \
                               'STORE_GPT_ATTR_IN_RECOVERYINFO=1' "

EXTRA_OEMAKE:append:sa8797 = " 'SUPPORT_AB_BOOT_LXC=1' \
                               'AB_RETRYCOUNT_DISABLE=1' \
                               'ENABLE_LV_ATOMIC_AB=1' "

EXTRA_OEMAKE:append:sa7255 = " 'SUPPORT_AB_BOOT_LXC=1' \
                               'AB_RETRYCOUNT_DISABLE=1' \
                               'ENABLE_LV_ATOMIC_AB=1' \
                               'ENABLE_SAIL_FLASHING=1' \
                               'BOOTIMAGE_LOAD_VERIFY_IN_PARALLEL=1' \
                               'ENABLE_SAIL_BOOT=1' \
                               'EMMC_MULTI_LUN_SUPPORT=1' "

do_configure[noexec] = "1"
do_compile () {
    export BUILD_CC=${STAGING_BINDIR_NATIVE}/clang
    export BUILD_CXX=${STAGING_BINDIR_NATIVE}/clang++
    if ${@bb.utils.contains('MACHINE_FEATURES', 'goldcore-boot', 'true', 'false', d)}; then
        export LINUX_BOOT_CPU_SELECTION_ENABLED=1
        export TARGET_LINUX_BOOT_CPU_ID=7
    fi
    oe_runmake -f makefile all
}
do_install() {
    install -d ${D}/boot
}

do_deploy() {
    if [ -f ${D}/boot/${PRODUCT}-abl.elf ]; then
      install -m 0644 ${D}/boot/${PRODUCT}-abl.elf ${DEPLOYDIR}
    else
      install -m 0644 ${S}/../abl.elf ${DEPLOYDIR}/unsigned_abl.elf
    fi
}
do_deploy[dirs] = "${S} ${DEPLOYDIR}"
do_deploy[nostamp] = "1"

addtask deploy before do_build after do_install

INCSUFFIX = "${@bb.utils.contains('QTI_BASE_PROP', "Y", 'edk2', 'none',d)}"
include ${INCSUFFIX}.inc

BUILD_OS = "linux"

INSANE_SKIP:${PN} = "arch"

PACKAGE_STRIP = "no"
PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES:${PN} += "/boot"
FILES:${PN}-dbg += "/boot/.debug"

#set PARALLEL_MAKE to 1 to avoid edk2 parallel build issue
PARALLEL_MAKE = "-j 1"
