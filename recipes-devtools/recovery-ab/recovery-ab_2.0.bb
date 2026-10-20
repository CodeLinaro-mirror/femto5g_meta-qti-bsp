inherit autotools-brokensep pkgconfig systemd

DESCRIPTION = "Recovery bootloader"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/\
${LICENSE};md5=89aea4e17d99a7cacdbeed46a0096b10"
HOMEPAGE = "https://www.codeaurora.org/gitweb/quic/la?p=platform/bootable/recovery.git"

DEPENDS += "glib-2.0 ext4-utils oem-recovery adbd libbase libsparse libmincrypt bzip2 bison-native openssl openssl-native"
DEPENDS += "${@bb.utils.contains('OTA_WHOLE_FILE_SIGN', 'true', 'releasetools-native dumpkey-native', '', d)}"
DEPENDS += " ${@bb.utils.contains('COMBINED_FEATURES', 'qti-ab-boot', 'abctl', '', d)}"

RDEPENDS:${PN} += "zlib attr"
RDEPENDS:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'ota-package-verification', 'openssl', '', d)}"
RDEPENDS:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'ota-package-verification', 'openssl-bin', '', d)}"

FILESEXTRAPATHS:prepend := "${WORKSPACE}:"

SRC_URI = "file://OTA/recovery/"
SRC_URI += "file://fstab_AB"
SRC_URI += "file://fstab_AB_cache_ext4"
SRC_URI += "file://update_engine.service"
SRC_URI += "file://ota_overlayfs_decouple"

S = "${WORKDIR}/OTA/recovery"

EXTRA_OECONF = "--with-glib --with-sanitized-headers=${STAGING_KERNEL_BUILDDIR}/usr/include \
                --with-core-headers=${STAGING_INCDIR}"
EXTRA_OECONF:append = "${@bb.utils.contains('MACHINE_FEATURES', 'ota-package-verification', 'TARGET_SUPPORTS_OTA_VERIFICATION=true', '', d)}"
EXTRA_OECONF:append = "${@bb.utils.contains('OTA_WHOLE_FILE_SIGN', 'true', 'TARGET_SUPPORTS_OTA_WHOLE_FILE_SIGN=true', '', d)}"
CFLAGS += "-lsparse -llog"
PARALLEL_MAKE = ""

EXTRA_OECONF:append = " ${@bb.utils.contains('DISTRO_FEATURES', 'qti-ab-boot', 'TARGET_SUPPORTS_AB=true', '', d)}"
EXTRA_OECONF:append = " ${@bb.utils.contains('MACHINE_FEATURES', 'qti-abc-boot', 'TARGET_SUPPORTS_ABC=true', '', d)}"

FILES:${PN}  = "${bindir} ${libdir} ${systemd_unitdir} ${includedir} /res /cache"
SYSTEMD_SERVICE:${PN} = " ${@bb.utils.contains('DISTRO_FEATURES', 'qti-ab-boot', bb.utils.contains('COMBINED_FEATURES', 'qti-nad-core', '', 'update_engine.service', d), '', d)}"

RM_WORK_EXCLUDE += "${PN}"
INITSCRIPT_NAME = "update_engine"
INITSCRIPT_PARAMS = "defaults"
generate_public_key() {
    mkdir -p ${TMPDIR}/deploy/images/${MACHINE}/ota-scripts
    openssl pkcs8 -inform DER -nocrypt -in ${WORKSPACE}/OTA/build/target/product/security/testkey.pk8 -out ${TMPDIR}/deploy/images/${MACHINE}/ota-scripts/private.pem
    openssl rsa -in ${TMPDIR}/deploy/images/${MACHINE}/ota-scripts/private.pem -outform PEM -pubout > ${WORKDIR}/public.pem
}

generate_recovery_keys() {
    java -jar ${DEPLOY_DIR_IMAGE}/ota-scripts/framework/dumpkey.jar \
        ${DEPLOY_DIR_IMAGE}/ota-scripts/security/testkey.x509.pem \
        > ${WORKDIR}/keys
}

do_install[prefuncs] += "${@bb.utils.contains('MACHINE_FEATURES', 'ota-package-verification', 'generate_public_key', '', d)}"
do_install[depends] += "${@bb.utils.contains('OTA_WHOLE_FILE_SIGN', 'true', 'releasetools-native:do_deploy dumpkey-native:do_deploy', '', d)}"
do_install[prefuncs] += "${@bb.utils.contains('OTA_WHOLE_FILE_SIGN', 'true', 'generate_recovery_keys', '', d)}"

do_install:append() {
        install -d ${D}/res/

        install -d ${D}/${base_bindir}
        install -m 0755 ${WORKDIR}/ota_overlayfs_decouple -D ${D}${base_bindir}/ota_overlayfs_decouple

        if ${@bb.utils.contains('COMBINED_FEATURES', 'qti-nad-core', 'false', 'true', d)}; then
            install -d ${D}/cache/recovery
        fi
        if ${@bb.utils.contains('IMAGE_FSTYPES', 'ext4', 'true', 'false', d)}; then
            if ${@bb.utils.contains_any('MACHINE_MNT_POINTS', '/overlay', 'true', 'false', d)}; then
                install -m 0755 ${WORKDIR}/fstab_AB -D ${D}/res/recovery_volume_config
            elif ${@bb.utils.contains_any('MACHINE_MNT_POINTS', '/cache', 'true', 'false', d)}; then
                install -m 0755 ${WORKDIR}/fstab_AB_cache_ext4 -D ${D}/res/recovery_volume_config
            fi
        fi

        install -d ${D}${systemd_unitdir}/system/

        if ${@bb.utils.contains('DISTRO_FEATURES', 'qti-ab-boot', bb.utils.contains('COMBINED_FEATURES', 'qti-nad-core', 'false', 'true', d), 'false', d)}; then
            install -m 0644 ${WORKDIR}/update_engine.service -D \
                     ${D}${systemd_unitdir}/system/update_engine.service
        fi
        if ${@bb.utils.contains('MACHINE_FEATURES', 'ota-package-verification', 'true', 'false', d)}; then
            install -m 0755 ${WORKDIR}/public.pem -D ${D}/res/public.pem
        fi
        if ${@bb.utils.contains('OTA_WHOLE_FILE_SIGN', 'true', 'true', 'false', d)}; then
            install -m 0644 ${WORKDIR}/keys -D ${D}/res/keys
        fi
}
