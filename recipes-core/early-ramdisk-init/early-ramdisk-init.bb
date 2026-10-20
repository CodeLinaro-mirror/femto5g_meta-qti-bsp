SUMMARY = "early-ramdisk-init for load kernel modules and start rootfs init"
HOMEPAGE = "https://git.codelinaro.org"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=7a434440b651f4a472ca93716d01033a"

SRC_URI = "file://early-ramdisk-init"

SRCREV = "${AUTOREV}"
DEPENDS += "kmod util-linux"
S = "${WORKDIR}/early-ramdisk-init"

inherit autotools

EXTRA_OECONF += "--bindir=${base_sbindir} --sbindir=${base_sbindir}"

CFLAGS += '-DLOG_DIR=\\"/boot/early-ramdisk\\"'
CFLAGS += "${@bb.utils.contains('DISTRO_FEATURES', 'early_init', '-DEARLY_INIT', '', d)}"

TARGET_PATH_NAME ?= "${MACHINE}"

do_install:append() {
    install -d ${D}/dev
    install -d ${D}/sys
    install -d ${D}/etc
    install -d ${D}/proc
    install -d ${D}/boot/early-ramdisk
    install -d ${D}/realroot
    install -d ${D}/etc/modules-load.f
    touch ${D}/init
    install -m 0755 ${S}/conf/${TARGET_PATH_NAME}/*.conf -D ${D}/etc/modules-load.f/
}

FILES:${PN} += "\
         init \
         dev/ \
         sys/ \
         etc/ \
         proc/ \
         boot/early-ramdisk \
         realroot/ \
         etc/modules-load.f/* \
"
