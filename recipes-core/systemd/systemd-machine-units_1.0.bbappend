inherit qimage-utils

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI:append = " file://media-card.mount"
SRC_URI:append = " file://media-ram.mount"
SRC_URI:append = " file://var-volatile.mount"
SRC_URI:append = " file://proc-bus-usb.mount"
SRC_URI:append = " file://dash.mount"

SRC_URI:append:batcam = " file://pre_hibernate.sh"
SRC_URI:append:batcam = " file://post_hibernate.sh"

SRC_URI:append:vienna = " \
    file://factory_reset.service \
    file://factory_reset.sh \
    file://wlan_pre_load.service \
    file://msm_serial_init.service \
    file://mount_partition.sh \
    file://early-mount.service \
    file://shutdown_perf.service \
    file://shutdown_perf.sh \
"
USERDATA_IMAGE_SIZE = "${@get_size_in_bytes(d.getVar('USERDATA_SIZE') or '1GB')}"

do_install:append:vienna() {
    install -d ${D}${bindir}
    install -m 0755 ${WORKDIR}/factory_reset.sh ${D}${bindir}/factory_reset.sh

    sed -i "s|__USERDATA_IMAGE_SIZE__|${USERDATA_IMAGE_SIZE}|g" ${D}${bindir}/factory_reset.sh

    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${WORKDIR}/factory_reset.service ${D}${systemd_unitdir}/system/

    install -d ${D}${systemd_unitdir}/system/local-fs.target.wants
    ln -sf ../factory_reset.service \
        ${D}${systemd_unitdir}/system/local-fs.target.wants/factory_reset.service

    install -m 0644 ${WORKDIR}/wlan_pre_load.service ${D}${systemd_unitdir}/system/
    ln -sf ../wlan_pre_load.service \
        ${D}${systemd_unitdir}/system/local-fs.target.wants/wlan_pre_load.service

    install -m 0644 ${WORKDIR}/msm_serial_init.service ${D}${systemd_unitdir}/system/
    ln -sf ../msm_serial_init.service \
        ${D}${systemd_unitdir}/system/local-fs.target.wants/msm_serial_init.service

    install -d ${D}${base_sbindir}
    install -m 0755 ${WORKDIR}/mount_partition.sh ${D}${base_sbindir}/mount_partition

    install -m 0644 ${WORKDIR}/early-mount.service ${D}${systemd_unitdir}/system/
    ln -sf ../early-mount.service \
        ${D}${systemd_unitdir}/system/local-fs.target.wants/early-mount.service

    install -m 0755 ${WORKDIR}/shutdown_perf.sh ${D}${bindir}/shutdown_perf.sh

    install -m 0644 ${WORKDIR}/shutdown_perf.service ${D}${systemd_unitdir}/system/
    install -d ${D}${systemd_unitdir}/system/poweroff.target.wants
    ln -sf ../shutdown_perf.service \
        ${D}${systemd_unitdir}/system/poweroff.target.wants/shutdown_perf.service
}

# Various mount related files assume selinux support by default.
# Explicitly remove sepolicy entries when selinux is not present.
fix_sepolicies () {
    sed -i "s#,rootcontext=system_u:object_r:var_t:s0##g"  ${WORKDIR}/var-volatile.mount
}
do_install[prefuncs] += " ${@bb.utils.contains('DISTRO_FEATURES', 'selinux', '', 'fix_sepolicies', d)}"

# Install var-volatile.mount for tmpfs
do_install:append () {
    install -d 0644 ${D}${systemd_unitdir}/system
    install -d 0644 ${D}${systemd_unitdir}/system/local-fs.target.wants
    install -m 0644 ${WORKDIR}/var-volatile.mount \
            ${D}${systemd_unitdir}/system/var-volatile.mount

    ln -sf ${systemd_unitdir}/system/var-volatile.mount \
           ${D}${systemd_unitdir}/system/local-fs.target.wants/var-volatile.mount
}

# Scripts for pre and post hibernate functions
do_install:append:batcam () {
   install -d ${D}${systemd_unitdir}/system-sleep/
   install -m 0755 ${WORKDIR}/pre_hibernate.sh -D ${D}${systemd_unitdir}/system-sleep/pre_hibernate.sh
   install -m 0755 ${WORKDIR}/post_hibernate.sh -D ${D}${systemd_unitdir}/system-sleep/post_hibernate.sh
}

FILES:${PN} += " ${systemd_unitdir}/*"
