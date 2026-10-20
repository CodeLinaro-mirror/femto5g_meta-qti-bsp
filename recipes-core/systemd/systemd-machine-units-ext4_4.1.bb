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
SRC_URI += " file://vendor-bt_firmware.mount"
SRC_URI += " file://vendor-soccp_firmware.mount"
SRC_URI += " file://firmware.mount"
SRC_URI += " file://overlay.mount"
SRC_URI += " file://persist.mount"
SRC_URI += " file://systemrw.mount"
SRC_URI += " file://overlay-etc.mount"
SRC_URI += " file://overlay-data.mount"
SRC_URI += " file://overlay-cache.mount"
SRC_URI += " file://overlay-workdir.sh"
SRC_URI += " file://overlay-workdir.service"
SRC_URI += " file://overlay-workdir-with-fde.service"

OVERLAY_MOUNTER_MACHINES = "alor vienna pebble seraph"

SRC_URI:append = "${@bb.utils.contains_any('BASEMACHINE', \
    d.getVar('OVERLAY_MOUNTER_MACHINES'), \
    ' file://overlay-data-mounter.service \
      file://overlay-etc-mounter.service \
      file://overlay-cache-mounter.service', \
    '', d)}"

SRC_URI += " file://bt_firmware-mount.service"
SRC_URI += " file://vendor-bt_firmware-mount.service"
SRC_URI += " file://vendor-soccp_firmware-mount.service"
SRC_URI += " file://cache.mount"
SRC_URI += " file://data.mount"
SRC_URI += " file://dsp-mount.service"
SRC_URI += " file://firmware-mount.service"
SRC_URI += " file://overlay-usr-share.mount"
SRC_URI += " file://set-slotsuffix.service"
SRC_URI += " file://systemrw.conf"
SRC_URI += " file://systemrw.mount"
SRC_URI += " file://qti-mount-generator"

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

# [ alor vienna pebble ]: replace generic automount .mount units for /data and /etc with
# dedicated overlay-mounter service units that use overlay_mounter_t domain
# so the stashed credential for the overlayfs second-check is not mount_t.
do_install:append() {
    if echo "${OVERLAY_MOUNTER_MACHINES}" | grep -qw "${BASEMACHINE}"; then
       # Remove the automount-based .mount units for data, etc and cache installed
       # by add_overlay_mount_files(); they are replaced by explicit service units.
       rm -f ${D}${systemd_unitdir}/system/data.mount
       rm -f ${D}${systemd_unitdir}/system/etc.mount
       rm -f ${D}${systemd_unitdir}/system/cache.mount
       rm -f ${D}${systemd_unitdir}/system/local-fs.target.wants/data.mount
       rm -f ${D}${systemd_unitdir}/system/local-fs.target.wants/etc.mount
       rm -f ${D}${systemd_unitdir}/system/local-fs.target.wants/cache.mount

       # Install dedicated mounter service units
       install -m 0644 ${WORKDIR}/overlay-data-mounter.service \
           ${D}${systemd_unitdir}/system/overlay-data-mounter.service
       install -m 0644 ${WORKDIR}/overlay-etc-mounter.service \
           ${D}${systemd_unitdir}/system/overlay-etc-mounter.service
       install -m 0644 ${WORKDIR}/overlay-cache-mounter.service \
           ${D}${systemd_unitdir}/system/overlay-cache-mounter.service

       # Enable them via local-fs.target.wants
       ln -sf ${systemd_unitdir}/system/overlay-data-mounter.service \
           ${D}${systemd_unitdir}/system/local-fs.target.wants/overlay-data-mounter.service
       ln -sf ${systemd_unitdir}/system/overlay-etc-mounter.service \
           ${D}${systemd_unitdir}/system/local-fs.target.wants/overlay-etc-mounter.service
       ln -sf ${systemd_unitdir}/system/overlay-cache-mounter.service \
           ${D}${systemd_unitdir}/system/local-fs.target.wants/overlay-cache-mounter.service
    fi
}

SYSTEMD_SERVICE:${PN}:append:alor = " overlay-data-mounter.service overlay-etc-mounter.service overlay-cache-mounter.service"
SYSTEMD_SERVICE:${PN}:append:vienna = " overlay-data-mounter.service overlay-etc-mounter.service overlay-cache-mounter.service"
SYSTEMD_SERVICE:${PN}:append:pebble = " overlay-data-mounter.service overlay-etc-mounter.service overlay-cache-mounter.service"
SYSTEMD_SERVICE:${PN}:append:seraph = " overlay-data-mounter.service overlay-etc-mounter.service overlay-cache-mounter.service"

RDEPENDS:${PN}:append:alor = " overlay-mounter"
RDEPENDS:${PN}:append:vienna = " overlay-mounter"
RDEPENDS:${PN}:append:pebble = " overlay-mounter"
RDEPENDS:${PN}:append:seraph = " overlay-mounter"
