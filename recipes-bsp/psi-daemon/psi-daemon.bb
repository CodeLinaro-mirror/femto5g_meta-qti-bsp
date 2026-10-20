inherit autotools-brokensep pkgconfig

DESCRIPTION      = "psi-daemon"
HOMEPAGE         = "www.codelinaro.org"
LICENSE          = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM += "file://${COREBASE}/meta-qti-bsp/files/common-licenses/\
${LICENSE};md5=3771d4920bd6cdb8cbdf1e8344489ee0"

#FIXME seems not needed
# PR = "r1"

FILESPATH =+ "${WORKSPACE}/vendor/qcom/opensource/:"
SRC_URI   = "file://psi_daemon"
# FIXME - seems above line copies this also
# SRC_URI  += "file://psi_daemon/psi_daemon.service"

# Default search path is ${WORKDIR}/psi-daemon-1.0. Change to match SRC_URI
S = "${WORKDIR}/psi_daemon"
DEPENDS += "virtual/kernel libcutils liblog libutils libbase linux-msm-headers"

# To get kernel headers for compilation
do_configure[depends] += "virtual/kernel:do_shared_workdir"

EXTRA_OECONF = " --with-sanitized-headers=${STAGING_INCDIR}/linux-msm/usr/include \
                 --disable-static "

FILES:${PN} += "${systemd_unitdir}/*"

do_install:append() {
    # Start psi_daemon service prior to multi-user.target
    install -d ${D}${systemd_unitdir}/system/multi-user.target.wants
    ln -sf ${systemd_unitdir}/system/psi_daemon.service ${D}${systemd_unitdir}/system/multi-user.target.wants/psi_daemon.service

    install -m 0644 ${S}/psi_daemon.service -D ${D}${systemd_unitdir}/system/psi_daemon.service
}

PACKAGE_ARCH = "${MACHINE_ARCH}"
