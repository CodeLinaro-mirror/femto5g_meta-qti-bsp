DESCRIPTION = "Systemd machine units for ubi image"
include qti-systemd-machine-units.inc

SRC_URI += " file://post_hibernate.sh"
SRC_URI += " file://pre_hibernate.sh"
SRC_URI += " file://proc-bus-usb.mount"
SRC_URI += " file://var-volatile.mount"
SRC_URI += " file://persist.mount"
SRC_URI += " file://cache.mount"
SRC_URI += " file://data.mount"
SRC_URI += " file://dsp-mount.service"
SRC_URI += " file://firmware-mount.service"
SRC_URI += " file://systemrw.conf"
SRC_URI += " file://systemrw.mount"
SRC_URI += " file://firmware-ubi-mount.sh"
SRC_URI += " file://bt_firmware-mount.service"
SRC_URI += " file://mountpartitions.rules"
SRC_URI += " file://bt_firmware-ubi-mount.sh"
SRC_URI += " file://ecm.rules"

IMAGETYPE = "ubi"

fix_sepolicies_ubi () {

    sed -i "s#,context=system_u:object_r:firmware_t:s0##g" ${WORKDIR}/firmware-mount.service
    sed -i "s#,context=system_u:object_r:firmware_t:s0##g" ${WORKDIR}/bt_firmware-mount.service
    sed -i "s#,context=system_u:object_r:adsprpcd_t:s0##g" ${WORKDIR}/dsp-mount.service
    sed -i "s#,rootcontext=system_u:object_r:data_t:s0##g" ${WORKDIR}/data.mount
    sed -i "s#,rootcontext=system_u:object_r:persist_t:s0##g" ${WORKDIR}/persist.mount
    sed -i "s#,rootcontext=system_u:object_r:system_data_t:s0##g" ${WORKDIR}/systemrw.mount
}

do_install[prefuncs] += " ${@bb.utils.contains('DISTRO_FEATURES', 'selinux', '', 'fix_sepolicies_ubi', d)}"

do_install:append () {

    add_ubi_scripts
    if ${@bb.utils.contains('DISTRO_FEATURES','nand-squashfs','true','false',d)}; then
        add_squashfs_scripts
    fi
    if ${@bb.utils.contains('MACHINE_FEATURES','tele-squashfs-ubi','true','false',d)}; then
        add_squashfs_scripts
    fi
    install -d 0644 ${D}${sysconfdir}/udev/rules.d
    install -m 0644 ${S}/mountpartitions.rules ${D}${sysconfdir}/udev/rules.d/mountpartitions
}

do_install:append:mdm9607() {
   install -d 0644 ${D}${sysconfdir}/udev/rules.d
   install -m 0744 ${S}/ecm.rules ${D}${sysconfdir}/udev/rules.d/ecm.rules
}

add_ubi_scripts () {
    for entry in ${MACHINE_MNT_POINTS}; do
        mountname="${entry:1}"
        if [ "$mountname" = "firmware" -o "$mountname" = "bt_firmware" -o "$mountname" = "dsp" -o "$mountname" = "vm-bootsys" -o "$mountname" = "lxcrootfs" ]; then
            install -m 0744 ${S}/${mountname}-ubi-mount.sh ${D}${sysconfdir}/initscripts/${mountname}-ubi-mount.sh
        fi

        if [ "$mountname" = "systemrw" ]; then
            install -m 0744 ${S}/systemrw.conf ${D}${systemd_system_unitdir}/systemrw-ubi.conf
        fi
    done
}

add_squashfs_scripts () {
    if ${@bb.utils.contains('MACHINE_FEATURES','tele-squashfs-ubi','true','false',d)}; then
        install -m 0544 ${S}/non-hlos-squash.sh ${D}${sysconfdir}/initscripts/non-hlos-squash.sh
        sed -i -e 's/FindAndMountUBI modem/FindAndMountUBIVol firmware/' ${D}${sysconfdir}/initscripts/non-hlos-squash.sh
    elif ${@bb.utils.contains('MACHINE_MNT_POINTS', '/firmware', 'true', 'false', d)}; then
        install -m 0544 ${S}/non-hlos-squash.sh ${D}${sysconfdir}/initscripts/firmware-ubi-mount.sh
    fi
}
