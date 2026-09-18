DESCRIPTION = "Systemd machine units for ext4 image"
include qti-systemd-machine-units.inc

SRC_URI += " file://dash.mount"
SRC_URI += " file://dsp.mount"
SRC_URI += " file://media-ram.mount"
SRC_URI += " file://post_hibernate.sh"
SRC_URI += " file://pre_hibernate.sh"
SRC_URI += " file://proc-bus-usb.mount"
SRC_URI += " file://var-volatile.mount"
SRC_URI += " file://bt_firmware.mount"
SRC_URI += " file://firmware.mount"
SRC_URI += " file://overlay.mount"
SRC_URI += " file://persist.mount"
SRC_URI += " ${@bb.utils.contains('MACHINE_FEATURES', 'var-persist', 'file://var-persist.mount', '', d)}"
SRC_URI += " file://systemrw.mount"
SRC_URI += " file://overlay-etc.mount"
SRC_URI += " file://overlay-data.mount"
SRC_URI += " file://overlay-cache.mount"
SRC_URI += " file://overlay-workdir.sh"
SRC_URI += " file://overlay-workdir.service"
SRC_URI += " file://overlay-workdir-with-fde.service"
SRC_URI += " file://bt_firmware-mount.service"
SRC_URI += " file://cache.mount"
SRC_URI += " file://nvram.mount"
SRC_URI += " file://rdklogs.mount"
SRC_URI += " file://data.mount"
SRC_URI += " file://dsp-mount.service"
SRC_URI += " file://firmware-mount.service"
SRC_URI += " file://overlay-usr-share.mount"
SRC_URI += " file://set-slotsuffix.service"
SRC_URI += " file://systemrw.conf"
SRC_URI += " file://systemrw.mount"
SRC_URI += " file://qti-mount-generator"

SRC_URI:echo = " file://persist.mount \
                 file://systemrw.mount \
                 file://cache.mount \
                 file://nvram.mount \
                 file://rdklogs.mount \
                 file://data.mount \
                 file://var-volatile.mount \
                 file://set-slotsuffix.service \
                 file://systemrw.conf \
                 file://dsp-mount.service \
                 file://firmware-mount.service \
                 file://var-persist.mount"

IMAGETYPE = "ext4"

do_install[prefuncs] += " ${@bb.utils.contains('DISTRO_FEATURES', 'selinux', '', 'fix_sepolicies', d)}"
do_install[prefuncs] += " ${@bb.utils.contains('MACHINE_FEATURES', 'qti-ab-boot', 'fix_mount_services', '', d)}"

do_install:append () {

    if ${@bb.utils.contains('MACHINE_MNT_POINTS', '$userfsdatadir', 'true', 'false', d)}; then
        # Run fsck at boot
        install -d 0644 ${D}${systemd_unitdir}/system/local-fs-pre.target.requires
        ln -sf ${systemd_unitdir}/system/systemd-fsck@.service \
            ${D}${systemd_unitdir}/system/local-fs-pre.target.requires/systemd-fsck@dev-disk-by\\x2dpartlabel-userdata.service
    fi

    if ${@bb.utils.contains('COMBINED_FEATURES', 'qti-ab-boot', 'true', 'false', d)}; then
        install -m 0644 ${S}/set-slotsuffix.service ${D}${systemd_unitdir}/system
    fi

}

SYSTEMD_SERVICE:${PN} += "${@bb.utils.contains('COMBINED_FEATURES','qti-ab-boot',' set-slotsuffix.service','',d)}"
