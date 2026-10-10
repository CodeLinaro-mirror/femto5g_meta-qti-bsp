inherit allarch nopackages

SUMMARY = "Installs Linux kernel signing keys"
LICENSE = "GPLv2.0-with-linux-syscall-note"
LIC_FILES_CHKSUM = "file://${QTI_METADATA_ROOT}/meta-qti-bsp/files/common-licenses/\
${LICENSE};md5=8afb6abdac9a14cb18a0d6c9c151e9b4"

# Ensure PACKAGE_ARCH is set to all even when multilib is enabled.
PACKAGE_ARCH = "all"

# Kalama kernel artifacts are exported beside kernel_platform.
KERNEL_PREBUILT_PATH:kalama = "${WORKSPACE}/kernel-${MSM_KERNEL_VERSION}/out/${KERNEL_DEFCONFIG}"

FILESEXTRAPATHS:prepend := "${KERNEL_PREBUILT_PATH}:"
SRC_URI   =  "file://${KERNEL_TYPE}/"
# Fetch only the key and preserve its directory once, including with BSP appends.
SRC_URI:kalama = "file://${KERNEL_TYPE}/signing_key.pem"
S = "${UNPACKDIR}"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    # Copy kernel certs
    install -d ${D}/kernel-certs
    install -m 0644 ${S}/${KERNEL_TYPE}/signing_key.pem ${D}/kernel-certs/
}

SYSROOT_DIRS += "/kernel-certs"
