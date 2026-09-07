SUMMARY = "AVB Verity Setup Service"
DESCRIPTION = "Installs and enables a systemd service for AVB verity setup"

LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-qti-bsp/files/common-licenses/\
${LICENSE};md5=3771d4920bd6cdb8cbdf1e8344489ee0"

PACKAGE_ARCH = "${MACHINE_ARCH}"

SRC_URI += "file://avb-veritysetup.service.in \
            file://avb-init-verity.sh"

#inherit systemd
REQUIRED_DISTRO_FEATURES = "systemd"

do_configure[noexec] = "1"

do_compile[dirs] = "${WORKDIR}"
do_compile() {

devdiskstr="dev-disk-by\\\x2dpartlabel-"

    vdlkmdevice="${devdiskstr}vendor_dlkm.device"
    systemdevice="${devdiskstr}system.device"

    if [[ "${MACHINE_FEATURES}" =~ .*qti-ab-boot* ]]; then
        vdlkmdevice="${devdiskstr}vendor_dlkm_a.device ${devdiskstr}vendor_dlkm_b.device"
        systemdevice="${devdiskstr}system_a.device ${devdiskstr}system_b.device"
    fi
    # Replace placeholders in the service template
    sed -e "s#@DEVICE@#$systemdevice#g; s#@MAPDEVICE@#system#g;" \
        avb-veritysetup.service.in >avb-veritysetup-system.service

    sed -e "s#@DEVICE@#$vdlkmdevice#g; s#@MAPDEVICE@#vendor_dlkm#g;" \
        avb-veritysetup.service.in >avb-veritysetup-vendor-dlkm.service
}

do_install() {
    install -d ${D}/verity/
    install -d ${D}${systemd_unitdir}/system/
    install -d ${D}${systemd_unitdir}/system/sysinit.target.wants/

    # Install the script
    install -m 0755 ${WORKDIR}/avb-init-verity.sh ${D}/verity/avb-init-verity.sh

    # Install the service template
    install -m 0644 ${WORKDIR}/avb-veritysetup-system.service \
        ${D}${systemd_unitdir}/system/avb-veritysetup-system@.service

    install -m 0644 ${WORKDIR}/avb-veritysetup-vendor-dlkm.service \
            ${D}${systemd_unitdir}/system/avb-veritysetup-vendor-dlkm@.service

    # Enable the root instance of the service
    ln -sf ${systemd_unitdir}/system/avb-veritysetup-system@.service \
        ${D}${systemd_unitdir}/system/sysinit.target.wants/avb-veritysetup-system@root.service

    ln -sf ${systemd_unitdir}/system/avb-veritysetup-vendor-dlkm@.service \
         ${D}${systemd_unitdir}/system/sysinit.target.wants/avb-veritysetup-vendor-dlkm@vdlkm.service

}

FILES:${PN} += " /verity/* ${systemd_unitdir}/system/* "