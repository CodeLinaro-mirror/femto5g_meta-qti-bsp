SUMMARY = "gst video codec test applications"
DESCRIPTION = "gst video codec test applications which used to verify some encoder/decoder features"
HOMEPAGE = "https://git.codelinaro.org"
SECTION = "multimedia"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${QTI_LICENSE_DIR}/${LICENSE};md5=b796c0007db682166a1721da80267bb2"

DEPENDS += "\
    glib-2.0 \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
"

SRC_URI = "${PATH_TO_REPO}/gstreamer/gst-plugins-qti-oss/.git;protocol=${PROTO};destsuffix=gstreamer/gst-plugins-qti-oss;usehead=1"
SRCREV = "${AUTOREV}"
S = "${WORKDIR}/gstreamer/gst-plugins-qti-oss/gst-qv-codec-test"

inherit pkgconfig meson

CFLAGS += "\
    -I${STAGING_INCDIR}/glib-2.0 \
    -I${STAGING_LIBDIR}/glib-2.0/include \
    -I${STAGING_INCDIR}/glib-2.0/glib \
    -I${STAGING_INCDIR}/gstreamer-1.0 \
"

