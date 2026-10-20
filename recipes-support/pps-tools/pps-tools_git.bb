SUMMARY = "User-space tools for LinuxPPS"

LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://COPYING;md5=751419260aa954499f7abaabaa882bbe"

PV = "0.0.0+git${SRCPV}"
SRCREV = "0deb9c7e135e9380a6d09e9d2e938a146bb698c8"
SRC_URI = "git://github.com/ago/pps-tools.git;branch=master;protocol=https"

S = "${WORKDIR}/git"

do_install() {
        install -d ${D}${bindir} ${D}${includedir} \
                   ${D}${includedir}/sys
        oe_runmake 'DESTDIR=${D}' install
}
