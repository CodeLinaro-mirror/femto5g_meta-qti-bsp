SUMMARY = "Qualcomm FDE userspace client"
DESCRIPTION = "Full Disk Encryption userspace tools"
HOMEPAGE = "https://git.codelinaro.org"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause-Clear;md5=7a434440b651f4a472ca93716d01033a"

DEPENDS = "curl glib-2.0 e2fsprogs"

PR = "r1"

SRC_URI = "${PATH_TO_REPO}/vendor/qcom/opensource/sec-modules/fde_utils/.git;protocol=${PROTO};destsuffix=/vendor/qcom/opensource/sec-modules/fde_utils;usehead=1"
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/vendor/qcom/opensource/sec-modules/fde_utils"

inherit autotools-brokensep pkgconfig

EXTRA_OECONF += "\
    --with-glib \
"

FILES:${PN} += "${bindir}/fde_utils"

RDEPENDS:${PN} += "\
    e2fsprogs-mke2fs lvm2 \
 "
