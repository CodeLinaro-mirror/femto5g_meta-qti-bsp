SUMMARY = "Filesystem resize service"
DESCRIPTION = "A systemd service that runs on the first system boot \
               and expands the filesystem(s) to the size of underlying partition"
HOMEPAGE = "https://git.codelinaro.org/"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=550794465ba0ec5312d6919e203a55f9"

SRC_URI = "file://resize-userdata.service"

S = "${WORKDIR}"

inherit systemd

export localstatedir = "${@bb.utils.contains('DISTRO_FEATURES','volatiled-var','/persist','/var',d)}"

SYSTEMD_SERVICE:${PN} = "resize-userdata.service"
SYSTEMD_AUTO_ENABLE:${PN} = "enable"

do_install() {
    if ${@bb.utils.contains('IMAGE_FEATURES','read-only-rootfs','true','false',d)}; then
        if ${@bb.utils.contains('DISTRO_FEATURES','volatiled-var','true','false',d)}; then
            sed -i -e 's/^After=.*$/After=dev-disk-by\x2dpartlabel-userdata.device persist.mount/' ${S}/resize-userdata.service
        else
            sed -i -e 's/^After=.*$/After=dev-disk-by\x2dpartlabel-userdata.device var.mount/' ${S}/resize-userdata.service
        fi
        sed -i    '/ConditionPathExists=.*$/d' ${S}/resize-userdata.service
        sed -i    '/^Before=.*$/a\ConditionPathExists=\${localstatedir}\/lib\/need_resize' ${S}/resize-userdata.service
        sed -i -e 's/^ExecStartPost=.*$/ExecStartPost=\/bin\/rm -rf \${localstatedir}\/lib\/need_resize/' ${S}/resize-userdata.service
    fi
    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-hypervisor', 'true', 'false', d)}; then
        sed -i    's/dev-disk-by\\x2dpartlabel-userdata/dev-vdb/g' ${S}/resize-userdata.service
        sed -i    's/dev-disk-by-partlabel-userdata/dev-vdb/g' ${S}/resize-userdata.service
        sed -i    's/\/dev\/disk\/by-partlabel\/userdata/\/dev\/vdb/g' ${S}/resize-userdata.service
    fi
    install -d ${D}${systemd_unitdir}/system
    install -m 0644 ${S}/resize-userdata.service ${D}${systemd_unitdir}/system/resize-userdata.service
    install -d ${D}${localstatedir}/lib
    touch ${D}${localstatedir}/lib/need_resize
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES:${PN} += "\
    ${systemd_unitdir}/system/resize-userdata.service \
    ${localstatedir}/lib/need_resize \
"

RDEPENDS:${PN} += "e2fsprogs-resize2fs"
