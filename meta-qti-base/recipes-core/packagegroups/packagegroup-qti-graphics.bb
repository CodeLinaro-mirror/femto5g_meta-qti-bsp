SUMMARY = "QTI package group for kgsl"
LICENSE = "BSD-3-Clause-Clear"

PACKAGE_ARCH = "${TUNE_PKGARCH}"

inherit packagegroup

PACKAGES = "\
    packagegroup-qti-graphics \
    "

ALLOW_EMPTY:${PN} = "1"

RDEPENDS:${PN} += "\
    vulkan-loader \
    "
#Add hgsl dependency on quin-gvm-gen4-5 and gvm-gen4-5
RDEPENDS:${PN}:quin-gvm-gen4-5 += "graphics-hgsldlkm"
RDEPENDS:${PN}:gvm-gen4-5 += "graphics-hgsldlkm"
RDEPENDS:${PN}:gvm-gen5 += "graphics-hgsldlkm"
