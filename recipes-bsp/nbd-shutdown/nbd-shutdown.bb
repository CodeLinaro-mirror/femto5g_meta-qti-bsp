LICENSE          = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-qti-bsp/files/common-licenses/\
${LICENSE};md5=3771d4920bd6cdb8cbdf1e8344489ee0"

DESCRIPTION = "Shutdown-time NBD teardown: flush/unmount data filesystems and \
disconnect nbd devices cleanly on network-boot targets"

FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI = "file://nbd-shutdown-teardown.service \
           file://nbd-data-flush \
           file://nbd-disconnect.shutdown"

S = "${WORKDIR}"

PR = "r2"
RDEPENDS_${PN} = "nbd-client"

do_install_append() {
	# Teardown oneshot unit (ExecStop runs the flush at shutdown) + static enable
	install -d ${D}${systemd_unitdir}/system/
	install -d ${D}${systemd_unitdir}/system/multi-user.target.wants/
	install -m 0644 ${S}/nbd-shutdown-teardown.service -D ${D}${systemd_unitdir}/system/nbd-shutdown-teardown.service
	ln -sf ${systemd_unitdir}/system/nbd-shutdown-teardown.service ${D}${systemd_unitdir}/system/multi-user.target.wants/nbd-shutdown-teardown.service

	# Flush helper invoked by the unit's ExecStop
	install -d ${D}${bindir}
	install -m 0755 ${S}/nbd-data-flush -D ${D}${bindir}/nbd-data-flush

	# systemd system-shutdown hook: nbd-client -d after final umount
	install -d ${D}${nonarch_libdir}/systemd/system-shutdown/
	install -m 0755 ${S}/nbd-disconnect.shutdown -D ${D}${nonarch_libdir}/systemd/system-shutdown/nbd-disconnect.shutdown
}

FILES_${PN} += "${systemd_unitdir}/system/"
FILES_${PN} += "${bindir}/nbd-data-flush"
FILES_${PN} += "${nonarch_libdir}/systemd/system-shutdown/"
