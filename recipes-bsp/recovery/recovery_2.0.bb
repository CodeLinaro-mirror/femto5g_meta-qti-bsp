inherit autotools-brokensep pkgconfig

DESCRIPTION = "Recovery bootloader"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/\
${LICENSE};md5=89aea4e17d99a7cacdbeed46a0096b10"
HOMEPAGE = "https://www.codeaurora.org/gitweb/quic/la?p=platform/bootable/recovery.git"

DEPENDS += "glib-2.0 mtd-utils oem-recovery adbd libbase libsparse libmincrypt bzip2 bison-native openssl openssl-native"
RDEPENDS:${PN} = "zlib"

FILESEXTRAPATHS:prepend := "${WORKSPACE}:"
SRC_URI = "file://OTA/recovery/"
SRC_URI += "file://recovery.service"
S = "${WORKDIR}/OTA/recovery"

generate_public_key() {
     mkdir -p ${DEPLOY_DIR_IMAGE}/ota-scripts
     openssl pkcs8 -inform DER -nocrypt -in ${WORKSPACE}/OTA/build/target/product/security/testkey.pk8 -out ${DEPLOY_DIR_IMAGE}/ota-scripts/private.pem
     openssl rsa -in ${DEPLOY_DIR_IMAGE}/ota-scripts/private.pem -outform PEM -pubout > ${WORKDIR}/public.pem
}

do_install[prefuncs] += "${@bb.utils.contains('MACHINE_FEATURES', 'ota-package-verification', 'generate_public_key', '', d)}"

EXTRA_OECONF = "--with-glib --with-sanitized-headers=${STAGING_KERNEL_BUILDDIR}/usr/include \
                --with-core-headers=${STAGING_INCDIR}"
EXTRA_OECONF += "${@bb.utils.contains('DISTRO_FEATURES', 'ota-package-verification', 'TARGET_SUPPORTS_OTA_VERIFICATION=true', '', d)}"
EXTRA_OECONF:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'dm-verity', 'TARGET_SUPPORTS_NAND_DM_VERITY=true', '', d)}"

# echo machine uses a kernel-based flag requiring this compile-time flag for usb
CPPFLAGS:append:echo = " -DQCLINUX_KERNEL_BASED_TARGETS"

PACKAGECONFIG ??= " ${@bb.utils.filter('DISTRO_FEATURES', 'selinux', d)} \
                  "
PACKAGECONFIG[selinux] = "--enable-selinux,--disable-selinux,libselinux"

CFLAGS += "-lsparse -llog"

PARALLEL_MAKE = ""

FILES:${PN}  = "${bindir} ${libdir} ${includedir} ${systemd_unitdir} /res /cache /data"

RM_WORK_EXCLUDE += "${PN}"

do_install:append() {
    install -d ${D}${systemd_unitdir}/system/
    install -d ${D}${systemd_unitdir}/system/multi-user.target.wants/
    install -m 0644 ${WORKDIR}/recovery.service -D ${D}${systemd_unitdir}/system/recovery.service
    ln -sf ${systemd_unitdir}/system/recovery.service ${D}${systemd_unitdir}/system/multi-user.target.wants/recovery.service
    if ${@bb.utils.contains('MACHINE_FEATURES', 'ota-package-verification', 'true', 'false', d)}; then
            install -m 0755 ${WORKDIR}/public.pem -D ${D}/res/public.pem
    fi
}
