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

# Use the workspace layout:
#   vendor/ is below the workspace root
#   security/ is below src/


# Recipe-local ABL signing function.
# vendor/ is below the workspace root; security profiles are below src/.
edk2_sectoolv2_sign_abl() {
    sectools="${WORKSPACE}/vendor/qcom/proprietary/sectools/Linux/sectools"
    profiles_dir="${WORKSPACE}/security/securemsm/security_profiles"
    security_profile_xml="${profiles_dir}/${QSIGN_TARGET}_tz_security_profile.xml"

    if [ ! -f "${security_profile_xml}" ]; then
        security_profile_xml="${profiles_dir}/${QSIGN_TARGET}_security_profile.xml"
    fi

    if [ ! -x "${sectools}" ]; then
        echo "ERROR: Sectools executable not found:"
        echo "${sectools}"
        exit 1
    fi

    if [ ! -f "${security_profile_xml}" ]; then
        echo "ERROR: Security profile not found:"
        echo "${security_profile_xml}"
        echo "QSIGN_TARGET=${QSIGN_TARGET}"
        echo "Profiles directory:"
        echo "${profiles_dir}"
        ls -la "${profiles_dir}" 2>/dev/null || true
        exit 1
    fi

    echo "Using Sectools: ${sectools}"
    echo "Using security profile: ${security_profile_xml}"

    "${sectools}" secure-image \
        "${WORKDIR}/unsigned_abl.elf" \
        --image-id ABL \
        --security-profile "${security_profile_xml}" \
        --sign \
        --signing-mode TEST \
        --outfile "${WORKDIR}/abl.elf"
}

# Recipe-local override.
# vendor/ is below the workspace root.
# security profiles are below src/.
sectoolv2_sign_abl() {
    sectools="${WORKSPACE}/vendor/qcom/proprietary/sectools/Linux/sectools"
    profiles_dir="${WORKSPACE}/security/securemsm/security_profiles"

    security_profile_xml="${profiles_dir}/${QSIGN_TARGET}_tz_security_profile.xml"

    if [ ! -f "${security_profile_xml}" ]; then
        security_profile_xml="${profiles_dir}/${QSIGN_TARGET}_security_profile.xml"
    fi

    if [ ! -x "${sectools}" ]; then
        echo "ERROR: Sectools executable not found:"
        echo "${sectools}"
        exit 1
    fi

    if [ ! -f "${security_profile_xml}" ]; then
        echo "ERROR: Security profile not found:"
        echo "${security_profile_xml}"
        echo "QSIGN_TARGET=${QSIGN_TARGET}"
        echo "Profiles directory=${profiles_dir}"
        ls -la "${profiles_dir}" 2>/dev/null || true
        exit 1
    fi

    echo "Using Sectools: ${sectools}"
    echo "Using security profile: ${security_profile_xml}"

    "${sectools}" secure-image \
        "${WORKDIR}/unsigned_abl.elf" \
        --image-id ABL \
        --security-profile "${security_profile_xml}" \
        --sign \
        --signing-mode TEST \
        --outfile "${WORKDIR}/abl.elf"
}
