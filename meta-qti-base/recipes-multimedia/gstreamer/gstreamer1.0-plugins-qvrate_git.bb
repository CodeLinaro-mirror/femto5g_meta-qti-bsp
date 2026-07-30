SUMMARY = "QTI Video frame rate convert plugin for GStreamer"
DESCRIPTION = "Gstreamer video frame rate convert plugin based on VPP hardware frame rate convert"
HOMEPAGE = "https://git.codelinaro.org/"
SECTION = "multimedia"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${QTI_LICENSE_DIR}/${LICENSE};md5=b796c0007db682166a1721da80267bb2"

DEPENDS += "\
    display-commonsys-intf-linux \
    gbm \
    gbm-headers \
    glib-2.0 \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    hyp-vpp \
    videodlkm \
    virtual/kernel-headers \
    vpp-headers \
"

SRC_URI = "${PATH_TO_REPO}/gstreamer/gst-plugins-qti-oss/.git;protocol=${PROTO};destsuffix=gstreamer/gst-plugins-qti-oss;usehead=1"
SRCREV = "${AUTOREV}"
S = "${WORKDIR}/gstreamer/gst-plugins-qti-oss/gst-plugin-qvrate"

inherit meson pkgconfig

CFLAGS += "-I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel}"

EXTRA_OEMESON:append:quin-gvm-gen4 = " -Dmmmcolorfmt=false"
EXTRA_OEMESON:append:qtiquingvm8295 = " -Dmmmcolorfmt=false"

SOLIBS = ".so"
FILES_SOLIBSDEV = ""

FILES:${PN} += "${libdir}/gstreamer-1.0/*.so"
