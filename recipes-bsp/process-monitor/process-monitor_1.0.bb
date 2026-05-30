inherit autotools systemd

LICENSE          = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-qti-bsp/files/common-licenses/\
${LICENSE};md5=3771d4920bd6cdb8cbdf1e8344489ee0"

DESCRIPTION = "process monitor"

S = "${WORKDIR}/${PN}"
FILESEXTRAPATHS:prepend := "${WORKSPACE}/QPlatformUtils:"
SRC_URI += "file://process-monitor"

DEPENDS += "systemd jsoncpp faultmgr"
RDEPENDS:${PN} += "faultmgr bash"

CPPFLAGS += "${@oe.utils.conditional('DEBUG_BUILD', '1', '-D_DEBUG', '',d)}"

SYSTEMD_SERVICE:${PN}  = "process-monitor.service"
SYSTEMD_AUTO_ENABLE    = "enable"

do_install:append() {
    
    # Install the systemd unit
    install -m 0644 ${S}/process-monitor.service -D ${D}${systemd_unitdir}/system/process-monitor.service

    
    # Deploy JSON configuration
    if [ -f ${S}/conf/process_monitor-${MACHINE}.json ]; then
        install -m 0644 ${S}/conf/process_monitor-${MACHINE}.json -D ${D}${sysconfdir}/process_monitor-${MACHINE}.json
    fi

    # Deploy NTN JSON configuration
    if [ -f ${S}/conf/process_monitor_ntn-${MACHINE}.json ]; then
        install -m 0644 ${S}/conf/process_monitor_ntn-${MACHINE}.json -D ${D}${sysconfdir}/process_monitor_ntn-${MACHINE}.json
    fi
    
    # Deploy D-Bus policy
    install -d ${D}${sysconfdir}/dbus-1/system.d
    install -m 0644 ${S}/conf/process_monitor-dbus.conf ${D}${sysconfdir}/dbus-1/system.d/

}

FILES:${PN} += " \
  ${bindir}/process-monitor \
  ${systemd_unitdir}/system/process-monitor.service \
  ${sysconfdir}/process_monitor-${MACHINE}.json \
  ${sysconfdir}/process_monitor_ntn-${MACHINE}.json \
  ${sysconfdir}/dbus-1/system.d/process_monitor-dbus.conf \
"