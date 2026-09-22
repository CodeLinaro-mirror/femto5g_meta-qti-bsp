do_install:append() {
        rm ${D}${systemd_unitdir}/network/80-wired.network
        # Disable ForwardToSyslog from systemd-conf.bb for better performance
        [ -f ${D}${systemd_unitdir}/journald.conf.d/00-systemd-conf.conf ] && \
                sed -i "/ForwardToSyslog=yes/d" ${D}${systemd_unitdir}/journald.conf.d/00-systemd-conf.conf

        [ -f ${D}${systemd_unitdir}/journald.conf.d/00-systemd-conf.conf ] && \
                echo "Compress=no" >> ${D}${systemd_unitdir}/journald.conf.d/00-systemd-conf.conf

        [ -f ${D}${systemd_unitdir}/journald.conf.d/00-systemd-conf.conf ] && \
                echo "Audit=no" >> ${D}${systemd_unitdir}/journald.conf.d/00-systemd-conf.conf
}

