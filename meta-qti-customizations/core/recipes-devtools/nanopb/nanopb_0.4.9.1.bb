DESCRIPTION="Protocol Buffers with small code size"
LICENSE="Zlib"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=9db4b73a55a3994384112efcdb37c01f"

DEPENDS = "protobuf-native"

SRC_URI = "git://github.com/nanopb/nanopb.git;nobranch=1;protocol=https"
SRCREV = "cad3c18ef15a663e30e3e43e3a752b66378adec1"

S = "${WORKDIR}/git"

inherit cmake python3native

# Override Python install dir to avoid CMake using the absolute sysroot path
EXTRA_OECMAKE += "-Dnanopb_PYTHON_INSTDIR_OVERRIDE=${PYTHON_SITEPACKAGES_DIR}"

do_install:append() {
    # Install C source files to nanopb/ subdirectory (not installed by CMake)
    install -d ${D}${includedir}/nanopb/
    install -m 0644 ${S}/pb_common.c ${D}${includedir}/nanopb/
    install -m 0644 ${S}/pb_decode.c ${D}${includedir}/nanopb/
    install -m 0644 ${S}/pb_encode.c ${D}${includedir}/nanopb/
}

FILES:${PN} += "${PYTHON_SITEPACKAGES_DIR}"
FILES:${PN}-dev += "${libdir}/cmake/${BPN}"

RDEPENDS:${PN} += "\
   ${PYTHON_PN}-protobuf \
   protobuf-compiler \
"

BBCLASSEXTEND = "native nativesdk"
