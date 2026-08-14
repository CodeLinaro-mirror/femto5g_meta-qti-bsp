SUMMARY = "QTI codec2 Plugin for GStreamer"
DESCRIPTION = "Gstreamer H/W decoder and encoder plugins based on codec2 APIs"
HOMEPAGE = "https://git.codelinaro.org/"
SECTION = "multimedia"
LICENSE = "BSD-3-Clause & BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://NOTICE;md5=e45e8fc7bdec198bec59831b0d7d16f7"

DEPENDS += "\
    codec2 \
    glib-2.0 \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    libdrm \
    libxml2 \
    media-codec2 \
    media-external \
    videodlkm \
    virtual/kernel-headers \
    binder \
    codec2-service \
    displaydlkm \
"

SRC_URI = "${PATH_TO_REPO}/gstreamer/gst-plugins-qti-oss/.git;protocol=${PROTO};destsuffix=gstreamer/gst-plugins-qti-oss;usehead=1"
SRCREV = "${AUTOREV}"
S = "${WORKDIR}/gstreamer/gst-plugins-qti-oss/gst-plugin-codec2"

inherit meson pkgconfig
CFLAGS += "\
    -I${STAGING_INCDIR}/c++ \
    -I${STAGING_INCDIR}/c++/${TARGET_SYS} \
    -I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel}/vidc \
    -I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel} \
    -I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel}/display \
"

CXXFLAGS += "\
    -I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel}/vidc \
    -I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel} \
    -I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel}/display \
"

EXTRA_OEMESON += "\
    -Dusedmaheap=true \
    -Dagl-c2service=true \
    -Dav1-dec=enabled \
    -Dmpeg2-dec=enabled \
    -Dqprange_option=op1 \
    -Dreport_frame_qp_option=op1 \
"

PACKAGE_ARCH ?= "${MACHINE_ARCH}"

SOLIBS = ".so"
FILES_SOLIBSDEV = ""

FILES:${PN} += "${libdir}/gstreamer-1.0/*.so"

TOOLCHAIN = "clang"
