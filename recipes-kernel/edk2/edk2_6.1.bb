inherit deploy python3native
DESCRIPTION = "UEFI bootloader"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/\
${LICENSE};md5=550794465ba0ec5312d6919e203a55f9"

BUILD_OS = "linux"

DEPENDS += "util-linux-native"

PACKAGE_ARCH = "${MACHINE_ARCH}"
FILESEXTRAPATHS:prepend := "${WORKSPACE}/kernel-6.1/kernel_platform/bootable/bootloader/:"

SRC_URI = "file://edk2"
S         =  "${WORKDIR}/edk2"

INSANE_SKIP:${PN} = "arch"

VBLE = "${@bb.utils.contains('DISTRO_FEATURES', 'qti-vble','1', '0', d)}"

AVB = "${@bb.utils.contains('MACHINE_FEATURES', 'qti-avb','1', '0', d)}"

VERITY_ENABLED = "${@bb.utils.contains('DISTRO_FEATURES', 'dm-verity', bb.utils.contains('MACHINE_FEATURES', 'dm-verity-bootloader', '1', '0', d), '0', d)}"

EARLY_ETH = "${@bb.utils.contains('DISTRO_FEATURES', 'early-eth', '1', '0', d)}"

SYSTEMD_BOOTSLOT_ENABLED = "${@bb.utils.contains('COMBINED_FEATURES', 'qti-ab-boot','1', '0', d)}"

DM_MOD_FOR_KERNEL5_4 = "${@d.getVar('DM_MOD_FOR_KERNEL') or "0"}"

EARLY_USB_INIT = "${@bb.utils.contains('DISTRO_FEATURES', 'qti-earlyusb', '1', '0', d)}"

TARGET_HIBERNATION_INSECURE_ENABLE = "${@bb.utils.contains('HIBERNATION_INSECURE_ENABLE', 'True', 'true', 'false', d)}"

TARGET_HIBERNATION_NO_AES = "${@bb.utils.contains('HIBERNATION_NO_AES', 'True', '1', '0', d)}"

HIBERNATION_PARTITION = "${@d.getVar('HIBERNATION_PARTITION_NAME') or 'none'}"

TARGET_HIBERNATION_TZ_ENC = "${@bb.utils.contains('HIBERNATION_TZ_ENC', 'True', '1', '0', d)}"

EXTRA_OEMAKE = " \
    'TARGET_ARCHITECTURE=${TARGET_ARCH}' \
    'BUILDDIR=${B}' \
    'BOOTLOADER_OUT=${B}/out' \
    'ENABLE_LE_VARIANT=true' \
    'ENABLE_SYSTEMD_BOOTSLOT=${SYSTEMD_BOOTSLOT_ENABLED}'\
    'ENABLE_DM_MOD_FOR_KERNEL5_4=${DM_MOD_FOR_KERNEL5_4}'\
    'VERIFIED_BOOT_ENABLED=${AVB}' \
    'VERIFIED_BOOT_LE=${VBLE}' \
    'VERITY_LE=${VERITY_ENABLED}' \
    'EDK_TOOLS_PATH=${S}/BaseTools' \
    'EARLY_ETH_ENABLED=${EARLY_ETH}' \
    'OVERRIDE_ABL_LOAD_ADDRESS=${ABL_LOAD_ADDRESS}' \
    'HIBERNATION_SUPPORT_INSECURE=${TARGET_HIBERNATION_INSECURE_ENABLE}' \
    'TARGET_SUPPORTS_EARLY_USB_INIT=${EARLY_USB_INIT}' \
    'HIBERNATION_SUPPORT_NO_AES=${TARGET_HIBERNATION_NO_AES}' \
    'HIBERNATION_PARTITION_NAME=${HIBERNATION_PARTITION}' \
    'HIBERNATION_TZ_ENCRYPTION=${TARGET_HIBERNATION_TZ_ENC}' \
"
# Nested quotes and escape characters as per CLANG needs.
EXTRA_OEMAKE += "INIT_BIN_LE="\"/sbin/init\"""

EXTRA_OEMAKE:append:qcs40x = " 'DISABLE_PARALLEL_DOWNLOAD_FLASH=1'"
NAND_SQUASHFS_SUPPORT = "${@bb.utils.contains('DISTRO_FEATURES', 'nand-squashfs', '1', '0', d)}"
EXTRA_OEMAKE:append = " 'NAND_SQUASHFS_SUPPORT=${NAND_SQUASHFS_SUPPORT}'"
EXTRA_OEMAKE:append:qti-distro-base-user = " 'VERITY_LE_USE_EXT4_GLUEBI=1'"

do_compile () {
    export BUILD_CC="clang"
    export CC=${STAGING_BINDIR_NATIVE}/clang/bin/clang
    export CXX=${STAGING_BINDIR_NATIVE}/clang/bin/clang++
    export LD=${STAGING_BINDIR_NATIVE}/clang/bin/clang/bin/ld.lld
    export AR=${BUILD_AR}
    export PATH="${STAGING_BINDIR_NATIVE}/clang/bin/:${PATH}"
    oe_runmake -f makefile all
}

do_install[noexec]="1"
do_configure[noexec]="1"

do_deploy() {
    install -m 644 ${WORKDIR}/abl.elf ${DEPLOYDIR}
}

do_deploy[dirs] = "${S} ${DEPLOYDIR}"
addtask deploy before do_build after do_install

PACKAGE_STRIP = "no"
