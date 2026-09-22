SUMMARY = "Gstreamer appsrc video playback sample"
DESCRIPTION = "Gstreamer appsrc video playback sample, supporting h264, h265, vp8, vp9"
HOMEPAGE = "https://git.codelinaro.org"
SECTION = "multimedia"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${QTI_LICENSE_DIR}/${LICENSE};md5=b796c0007db682166a1721da80267bb2"

DEPENDS += "\
    glib-2.0 \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    virtual/kernel-headers \
"

PR = "r1"

SRC_URI = "${PATH_TO_REPO}/gstreamer/gst-plugins-qti-oss/.git;protocol=${PROTO};destsuffix=gstreamer/gst-plugins-qti-oss;usehead=1"
SRCREV = "${AUTOREV}"
S = "${WORKDIR}/gstreamer/gst-plugins-qti-oss/gst-qappsrc-sample"

inherit autotools-brokensep pkgconfig

CFLAGS += "\
    -I${STAGING_INCDIR}/glib-2.0 \
    -I${STAGING_INCDIR}/../lib/glib-2.0/include \
    -I${STAGING_INCDIR}/glib-2.0/glib \
    -I${STAGING_INCDIR}/gstreamer-1.0 \
    -I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel} \
    -I${STAGING_INCDIR}/ion_headers \
    -I${STAGING_INCDIR}/mm-core/ \
"
