include dbus.inc

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI += "file://extra-users.conf"
SRC_URI += "file://dbus.conf"
SRC_URI += "file://extra-users-reboot.conf"

INITSCRIPT_NAME = "dbus-1"
INITSCRIPT_PARAMS = "start 98 5 3 2 . stop 02 0 1 6 ."

GROUPADD_PARAM:${PN} = "-r netdev"

INSANE_SKIP:${PN}:qcm2290-mtp += " installed-vs-shipped"
INSANE_SKIP:${PN}:qcm4325-mtp += " installed-vs-shipped"
FILES:${PN}:qcm2290-mtp += "${base_libdir_native}/systemd/system/dbus.service.d"
FILES:${PN}:qcm4325-mtp += "${base_libdir_native}/systemd/system/dbus.service.d"

do_install:append() {
   install -d ${D}/${datadir}/dbus-1/system.d/
   install -m 0644 ${WORKDIR}/extra-users.conf -D ${D}${datadir}/dbus-1/system.d/extra-users.conf
   if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
      install -d ${D}${systemd_unitdir}/system/sockets.target.wants
      ln -fs ../dbus.service ${D}${systemd_system_unitdir}/sockets.target.wants/dbus.service
      rm -rf ${D}${systemd_system_unitdir}/multi-user.target.wants/dbus.service
      if [ "${BASEMACHINE}" = "qcm2290-mtp" -o "${BASEMACHINE}" = "qcm4325-mtp" ]; then
            install -d ${D}/${base_libdir_native}/systemd/system/dbus.service.d
            install -m 0644 ${WORKDIR}/dbus.conf ${D}/${base_libdir_native}/systemd/system/dbus.service.d/dbus.conf
      else
            install -d ${D}/${base_libdir}/systemd/system/dbus.service.d
            install -m 0644 ${WORKDIR}/dbus.conf ${D}/${base_libdir}/systemd/system/dbus.service.d/dbus.conf
      fi
      install -m 0644 ${WORKDIR}/extra-users-reboot.conf -D ${D}${datadir}/dbus-1/system.d/extra-users-reboot.conf
   fi
}

