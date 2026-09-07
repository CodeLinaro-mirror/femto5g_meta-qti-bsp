SUMMARY = "Startup script and systemd service files for graphics dynamic loading"

LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM += "file://${COREBASE}/meta/files/common-licenses/\
BSD-3-Clause-Clear;md5=7a434440b651f4a472ca93716d01033a"

S = "${WORKDIR}"
SRC_URI = " file://init_qti_graphics.service \
            file://init_qti_graphics"

PACKAGE_ARCH = "${MACHINE_ARCH}"
do_install() {
   if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
       install -m 0755 ${S}/init_qti_graphics -D ${D}${sbindir}/initscripts/init_qti_graphics
       install -d ${D}/etc/systemd/system/
       install -m 0755 ${S}/init_qti_graphics.service -D ${D}${sysconfdir}/systemd/system/init_qti_graphics.service
       install -d ${D}/etc/systemd/system/multi-user.target.wants
       ln -sf /etc/systemd/system/init_qti_graphics.service ${D}/etc/systemd/system/multi-user.target.wants/init_qti_graphics.service
   else
       install -d ${D}/${sysconfdir}/init.d
       install -m755 ${S}/init_qti_graphics ${D}/${sysconfdir}/init.d/init_qti_graphics
   fi
}
SYSTEMD_SERVICE:${PN} = "init_qti_graphics.service"
