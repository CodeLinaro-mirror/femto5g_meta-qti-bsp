inherit pkgconfig qprebuilt autotools-brokensep

DESCRIPTION = "Sensing Hub Library"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause-Clear;md5=7a434440b651f4a472ca93716d01033a"

DEPENDS += "protobuf"
DEPENDS += "protobuf-native"

FILESPATH =+ "${WORKSPACE}/vendor/qcom/opensource/:"
SRC_URI  = "file://sensing-hub/"
S = "${WORKDIR}/sensing-hub"

#Disable the split of debug information into -dbg files
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

#Skips check for .so symlinks
INSANE_SKIP:${PN} = "dev-so"

FILES:${PN}  = "${includedir}/*"
FILES:${PN} += "${libdir}/*"
FILES:${PN} += "${bindir}/*"
FILES:${PN} += "${sysconfdir}/sensors/proto/*"
FILES:${PN}-dev = "${libdir}/*.la ${includedir}"

SOLIBS = ".so"
FILES:SOLIBSDEV = ""

PACKAGE_ARCH = "${MACHINE_ARCH}"
