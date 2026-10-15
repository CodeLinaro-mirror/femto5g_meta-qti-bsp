EXTRA_OECONF:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'selinux', '--with-selinux', '', d)}"
BASEPRODUCT = "${@d.getVar('PRODUCT', False)}"

FILESEXTRAPATHS:prepend := "${THISDIR}/openssh:"
SRC_URI += "file://sshd_config"

do_install:append:drone () {
    sed -i -e 's:#PermitRootLogin yes:PermitRootLogin yes:' ${WORKDIR}/sshd_config ${D}${sysconfdir}/ssh/sshd_config
    sed -i -e 's:#PasswordAuthentication yes:PasswordAuthentication yes:' ${WORKDIR}/sshd_config ${D}${sysconfdir}/ssh/sshd_config
}

do_install:append:robot-som () {
    sed -i -e 's:#PasswordAuthentication yes:PasswordAuthentication yes:' ${WORKDIR}/sshd_config ${D}${sysconfdir}/ssh/sshd_config
    sed -i -e 's:#PermitRootLogin yes:PermitRootLogin yes:' ${WORKDIR}/sshd_config ${D}${sysconfdir}/ssh/sshd_config ${D}${sysconfdir}/ssh/sshd_config_readonly
    sed -i -e 's:#PasswordAuthentication yes:PasswordAuthentication yes:' ${WORKDIR}/sshd_config ${D}${sysconfdir}/ssh/sshd_config ${D}${sysconfdir}/ssh/sshd_config_readonly
    sed -i '$a    StrictHostKeyChecking no' ${WORKDIR}/ssh_config ${D}${sysconfdir}/ssh/ssh_config
    sed -i '$a    UserKnownHostsFile /dev/null' ${WORKDIR}/ssh_config ${D}${sysconfdir}/ssh/ssh_config
}

RDEPENDS:${PN} += "${PN}-sftp"
