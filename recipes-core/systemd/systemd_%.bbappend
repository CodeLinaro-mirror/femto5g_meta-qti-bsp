#update SRC_URI to avoid fetcher failures
SRC_URI_remove = "git://github.com/systemd/systemd-stable.git;protocol=https;branch=${SRCBRANCH}"
SRC_URI_prepend = " git://git.codelinaro.org/clo/yocto-mirrors/github/systemd/systemd-stable.git;protocol=https;branch=${SRCBRANCH} "

#Add qti specific changes only when qt-disro is enabled.
QTI_SYSTEMD_INC = ""
QTI_SYSTEMD_INC_qti-distro-base = "${THISDIR}/qti-systemd.inc"
include ${QTI_SYSTEMD_INC}

do_install_append() {
    sed -i '/group:wheel/d' ${D}${exec_prefix}/lib/tmpfiles.d/systemd.conf
}
