inherit pkgconfig autotools-brokensep systemd

DESCRIPTION = "Sensing-hub APIs Library"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/BSD-3-Clause-Clear;md5=7a434440b651f4a472ca93716d01033a"

DEPENDS = "syslog-plumber"
DEPENDS += "protobuf"
DEPENDS += "protobuf-native"
DEPENDS:remove:alor = " syslog-plumber"
DEPENDS:remove:seraph = " syslog-plumber"
DEPENDS:append:seraph = " nanopb-runtime nanopb-generator-native glib-2.0"
DEPENDS:remove:pebble = " syslog-plumber"
DEPENDS:append:pebble = " nanopb-runtime nanopb-generator-native glib-2.0"

FILESPATH =+ "${WORKSPACE}:"
SRC_URI  = "file://sensors/sensing-hub/"
S = "${WORKDIR}/sensors/sensing-hub"

SRC_URI:alor  = "file://vendor/qcom/opensource/sensing-hub/"
S:alor = "${WORKDIR}/vendor/qcom/opensource/sensing-hub"
SRC_URI:pebble  = "file://vendor/qcom/opensource/sensing-hub/"
S:pebble = "${WORKDIR}/vendor/qcom/opensource/sensing-hub"

SRC_URI:seraph  = "file://vendor/qcom/opensource/sensing-hub/"
S:seraph = "${WORKDIR}/vendor/qcom/opensource/sensing-hub"

CXXFLAGS:append:seraph = " ${DEBUG_PREFIX_MAP}"

EXTRA_OECONF += " --with-systemd"

#Disable the split of debug information into -dbg files
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"

do_compile:prepend() {
    export LD_LIBRARY_PATH="${STAGING_DIR_NATIVE}/usr/lib/x86_64-linux-gnu:${LD_LIBRARY_PATH}"
}

#Skips check for .so symlinks
INSANE_SKIP:${PN} = "dev-so"

# need to export these variables for python-config to work
FILES:${PN} = "${includedir}/*"
FILES:${PN} += "/usr/lib/*"
FILES:${PN} += "/usr/bin/*"
FILES:${PN}-dev  = "${libdir}/*.la ${includedir}"
FILES:${PN} += "${systemd_unitdir}/system/"
FILES:${PN} += "/etc/sensors/*"

PACKAGE_ARCH    ?= "${TUNE_PKGARCH}"
