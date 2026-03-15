SUMMARY = "QTI package group for security"
DESCRIPTION = "This is the minimal set of packages required for linux userspace security utilities."

PACKAGE_ARCH = "${TUNE_PKGARCH}"

inherit packagegroup

PACKAGES = "\
    packagegroup-qti-security \
"

ALLOW_EMPTY:${PN} = "1"

RDEPENDS:${PN} += "\
    libcap \
    libcap-bin \
    attr \
    ${@bb.utils.contains_any("PREFERRED_VERSION_linux-msm", "5.15 6.1", "securemsmdlkm", "", d)} \
    ${@bb.utils.contains('DISTRO_FEATURES', 'qti-fde', 'enable-fde', '', d)} \
"

# sa8650-adas: do not auto-enable FDE, only provide fde tools
RDEPENDS:${PN}:remove:sa8650-adas = "enable-fde"
RDEPENDS:${PN}:append:sa8650-adas = " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'qti-fde', 'fde-utils', '', d)} \
"

RDEPENDS:${PN}:append:sa8775-flex = " optee-libckteec"
RDEPENDS:${PN}:append:gh-gvm-lemans = " optee-libckteec"
