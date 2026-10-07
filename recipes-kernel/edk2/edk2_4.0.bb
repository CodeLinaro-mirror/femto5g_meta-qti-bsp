inherit deploy nopackages
DESCRIPTION = "UEFI bootloader"
LICENSE = "BSD-2-Clause-Patent"
LIC_FILES_CHKSUM = " \
    file://${COMMON_LICENSE_DIR}/BSD-2-Clause-Patent;md5=0518d409dae93098cca8dfa932f3ab1b \
"
 
SIGNING_FUNCTION = "sectoolv2_sign_abl"
 
INHIBIT_DEFAULT_DEPS = "1"
FILESEXTRAPATHS:prepend := "${KERNEL_PREBUILT_PATH}/abl-userdebug:"
FILESEXTRAPATHS:prepend := "${WORKSPACE}/kernel-6.18/out/msm-kernel-kalama_le-debug_defconfig/dist/kalama-le_debug-defconfig_abl_unpacked/abl-userdebug:"
FILESEXTRAPATHS:prepend:pineapple := "${KERNEL_PREBUILT_PATH}/dist/abl-userdebug:"
FILESEXTRAPATHS:prepend:qcs610-odk-64 := "${KERNEL_PREBUILT_PATH}/dist/abl-userdebug:"
FILESEXTRAPATHS:prepend:kera := "${KERNEL_PREBUILT_PATH}/dist/abl-userdebug:"
FILESEXTRAPATHS:prepend:sun := "${KERNEL_PREBUILT_PATH}/dist/abl-userdebug:"
FILESEXTRAPATHS:prepend:vienna := "${KERNEL_PREBUILT_PATH}/dist/abl-userdebug:"
FILESEXTRAPATHS:prepend:alor := "${KERNEL_PREBUILT_PATH}/dist/abl-userdebug:"

SRC_URI = "file://unsigned_abl.elf"

do_install[noexec]="1"
do_configure[noexec]="1"
do_deploy() {
    install -m 644 ${WORKDIR}/abl.elf ${DEPLOYDIR}
}
do_deploy[dirs] = "${DEPLOYDIR}"
addtask deploy before do_build after do_install
