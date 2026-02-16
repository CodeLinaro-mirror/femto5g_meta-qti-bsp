FILESEXTRAPATHS:prepend:cinder := "${THISDIR}/files:"
FILESEXTRAPATHS:prepend:sdxlemur := "${THISDIR}/files:"
FILESEXTRAPATHS:prepend:sa525m := "${THISDIR}/files:"
FILESEXTRAPATHS:prepend:sa510m := "${THISDIR}/files:"
FILESEXTRAPATHS:prepend:mdm9607 := "${THISDIR}/files:"
FILESEXTRAPATHS:prepend:sa415m := "${THISDIR}/files:"

REQUIRED_DISTRO_FEATURES = ""
SRC_URI += "\
    ${@bb.utils.contains('MACHINE_MNT_POINTS', '/systemrw', 'file://mount-copybind', '', d)} \
    ${@bb.utils.contains('MACHINE_MNT_POINTS', '/systemrw', 'file://umount-copybind', '', d)} \
    ${@bb.utils.contains('MACHINE_MNT_POINTS', '/systemrw', 'file://volatile-binds.service.in', '', d)} \
"
do_compile:append:mdm9607() {
    if  [ -e var-volatile-lib.service ]; then
        # As systemd-logind need /var/lib, ensure that this service runs
        # after the volatile /var/lib is mounted.
        sed -i -e "/^Before=/s/\$/ systemd-logind.service/" \
          -e "/^WantedBy=/s/\$/ systemd-logind.service/" \
          var-volatile-lib.service
    fi
}

do_install:append () {
    if ${@bb.utils.contains('MACHINE_MNT_POINTS', '/systemrw', 'true', 'false', d)}; then
        install -d ${D}${base_sbindir}
        install -m 0755 mount-copybind ${D}${base_sbindir}/
        if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
            install -d ${D}${systemd_unitdir}/system
            for service in ${SYSTEMD_SERVICE:${PN}}; do
                install -m 0644 $service ${D}${systemd_unitdir}/system/
                install -m 0755 umount-copybind ${D}${base_sbindir}/
            done
        fi
    fi
}

VOLATILE_BINDS_sdxlemur = "\
/systemrw/adb_devid  /etc/adb_devid\n\
/systemrw/build.prop /etc/build.prop\n\
/systemrw/data /etc/data/\n\
/systemrw/data/adpl /etc/data/adpl/\n\
/systemrw/data/usb /etc/data/usb/\n\
/systemrw/data/miniupnpd /etc/data/miniupnpd/\n\
/systemrw/data/ipa /etc/data/ipa/\n\
/systemrw/rt_tables /etc/data/iproute2/rt_tables\n\
/systemrw/boot_hsusb_comp /etc/usb/boot_hsusb_comp\n\
/systemrw/boot_hsic_comp /etc/usb/boot_hsic_comp\n\
/systemrw/misc/wifi /etc/misc/wifi/\n\
/systemrw/bluetooth /etc/bluetooth/\n\
/systemrw/allplay /etc/allplay/\n\
"

VOLATILE_BINDS_sa2150p-nand = "\
/systemrw/adb_devid  /etc/adb_devid\n\
/systemrw/build.prop /etc/build.prop\n\
/systemrw/data /etc/data/\n\
/systemrw/data/adpl /etc/data/adpl/\n\
/systemrw/data/usb /etc/data/usb/\n\
/systemrw/data/miniupnpd /etc/data/miniupnpd/\n\
/systemrw/data/ipa /etc/data/ipa/\n\
/systemrw/rt_tables /etc/data/iproute2/rt_tables\n\
/systemrw/boot_hsusb_comp /etc/usb/boot_hsusb_comp\n\
/systemrw/boot_hsic_comp /etc/usb/boot_hsic_comp\n\
/systemrw/misc/wifi /etc/misc/wifi/\n\
/systemrw/tel.conf /etc/tel.conf\n\
/systemrw/systemd/network/ethernet.network /etc/systemd/network/ethernet.network\n\
/systemrw/netmgrd/config /etc/netmgrd/config\n\
/systemrw/config.ini /etc/initscripts/config.ini\n\
/systemrw/modem-monitor-usb.conf /etc/modem-monitor-usb.conf\n\
/systemrw/modem-monitor-pcie.conf /etc/modem-monitor-pcie.conf\n\
/systemrw/modem-monitor-eth.conf /etc/modem-monitor-eth.conf\n\
/systemrw/power_state.conf /etc/power_state.conf\n\
/systemrw/enable /etc/cv2x/enable\n\
"
VOLATILE_BINDS_sa410m = "\
/systemrw/adb_devid  /etc/adb_devid\n\
/systemrw/data /etc/data/\n\
/systemrw/data/adpl /etc/data/adpl/\n\
/systemrw/data/usb /etc/data/usb/\n\
/systemrw/data/ipa /etc/data/ipa/\n\
/systemrw/rt_tables /etc/data/iproute2/rt_tables\n\
/systemrw/boot_hsusb_comp /etc/usb/boot_hsusb_comp\n\
/systemrw/boot_hsic_comp /etc/usb/boot_hsic_comp\n\
"

VOLATILE_BINDS_sa515m = "\
/systemrw/adb_devid  /etc/adb_devid\n\
/systemrw/data /etc/data/\n\
/systemrw/data/adpl /etc/data/adpl/\n\
/systemrw/data/usb /etc/data/usb/\n\
/systemrw/data/ipa /etc/data/ipa/\n\
/systemrw/rt_tables /etc/data/iproute2/rt_tables\n\
/systemrw/boot_hsusb_comp /etc/usb/boot_hsusb_comp\n\
/systemrw/boot_hsic_comp /etc/usb/boot_hsic_comp\n\
"
