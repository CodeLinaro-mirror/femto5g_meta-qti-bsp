FILESEXTRAPATHS:prepend := "${THISDIR}/systemd:"

SRC_URI += "file://sysctl-coredump.conf"
SRC_URI += "file://limits-coredump.conf"

# Modify default CONFFILES as per machine needs

# Don't install coredump.conf for user builds.
COREDUMP = "1"
COREDUMP:qti-distro-user = ""
no_logs_to_console = "${@d.getVar('NO_SYS_JOURNAL_LOGS_TO_CONSOLE')}"

KMSGLOGS ?= "0"
KMSGLOGS:qti-distro-perf ?= "1"

SYSTEMD_COREDUMP_PATH ?= "${userfsdatadir}/coredump"

do_install:append() {
   if [ "${COREDUMP}" == "1" ]; then
       sed -i "s#@COREDUMP_PATH@#${SYSTEMD_COREDUMP_PATH}#" ${WORKDIR}/sysctl-coredump.conf

       install -m 0644 ${WORKDIR}/sysctl-coredump.conf -D ${D}${sysconfdir}/sysctl.d/sys-coredump.conf
       install -m 0644 ${WORKDIR}/limits-coredump.conf -D ${D}${sysconfdir}/security/limits.d/sys-coredump.conf

       #create coredump folder if needed
       install -m 0666 -d ${D}${SYSTEMD_COREDUMP_PATH}
   else
       rm -f ${D}${sysconfdir}/systemd/coredump.conf
   fi

   if ${@bb.utils.contains_any('MACHINE_FEATURES', 'qti-vm qti-vm-guest', 'true', 'false', d)}; then
      sed -i -e 's/.*RuntimeMaxUse.*/RuntimeMaxUse=1M/' ${D}${systemd_unitdir}/journald.conf.d/00-${PN}.conf
   fi
}

FILES:${PN} += "${sysconfdir}/sysctl.d/* ${sysconfdir}/security/limits.d/* ${SYSTEMD_COREDUMP_PATH}"

# journald.conf
do_install:append() {
   if [ "${no_logs_to_console}" != "1" ]; then
       # Redirect journal logs to console.
       sed -i '$aForwardToConsole=yes' ${D}${systemd_unitdir}/journald.conf.d/00-${PN}.conf
       sed -i '$aTTYPath=/dev/ttyMSM0' ${D}${systemd_unitdir}/journald.conf.d/00-${PN}.conf
       sed -i '$aMaxLevelConsole=warning' ${D}${systemd_unitdir}/journald.conf.d/00-${PN}.conf
       sed -i '$aReadKMsg=yes' ${D}${systemd_unitdir}/journald.conf.d/00-${PN}.conf
   fi
  
   if [ "${KMSGLOGS}" != "0" ]; then
       sed -i '$aForwardToKMsg=yes' ${D}${systemd_unitdir}/journald.conf.d/00-${PN}.conf
       sed -i '$aMaxLevelKMsg=debug' ${D}${systemd_unitdir}/journald.conf.d/00-${PN}.conf
   fi
}

# logind.conf
do_install:append() {
    # Ignore PowerKey
    sed -i '$aHandlePowerKey=ignore' ${D}${systemd_unitdir}/logind.conf.d/00-${PN}.conf
}

# system.conf
do_install:append() {
   if [ "${no_logs_to_console}" != "1" ]; then
       # Redirect system logs to both console and syslog.
       sed -i '$aStandardOutput=syslog+console' ${D}${systemd_unitdir}/system.conf.d/00-${PN}.conf
       sed -i '$aTTYPath=/dev/ttyMSM0' ${D}${systemd_unitdir}/system.conf.d/00-${PN}.conf
       sed -i '$aLogTarget=console' ${D}${systemd_unitdir}/system.conf.d/00-${PN}.conf
   else
       # Set LogTarget as syslog
       sed -i '$aLogTarget=syslog' ${D}${systemd_unitdir}/system.conf.d/00-${PN}.conf
   fi
   if [ "${KMSGLOGS}" != "0" ]; then
       sed -i '$aLogTarget=journal-or-kmsg' ${D}${systemd_unitdir}/system.conf.d/00-${PN}.conf
       sed -i '$aLogLevel=info' ${D}${systemd_unitdir}/system.conf.d/00-${PN}.conf
       sed -i '/LogTarget=syslog/d' ${D}${systemd_unitdir}/system.conf.d/00-${PN}.conf
   fi
}

# user.conf
do_install:append() {
}
