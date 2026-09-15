SUMMARY = "Secure video playback demo for GStreamer"
DESCRIPTION = "Secure video playback demo for GStreamer, supporting secure input buffer sharing."
HOMEPAGE = "https://git.codelinaro.org"
SECTION = "multimedia"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://secure-gst-codec2.c;beginline=1;endline=33;md5=8d64263c4896d8aca24635fd53968e67"

DEPENDS += "\
    glib-2.0 \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    virtual/kernel-headers \
"

PR = "r1"

SRC_URI = "${PATH_TO_REPO}/gstreamer/gst-plugins-qti-oss/.git;protocol=${PROTO};destsuffix=gstreamer/gst-plugins-qti-oss;usehead=1"
SRCREV = "${AUTOREV}"
S = "${WORKDIR}/gstreamer/gst-plugins-qti-oss/secure-app"

inherit autotools-brokensep pkgconfig

CFLAGS += "\
    -I${STAGING_INCDIR}/glib-2.0 \
    -I${STAGING_INCDIR}/../lib/glib-2.0/include \
    -I${STAGING_INCDIR}/glib-2.0/glib \
    -I${STAGING_INCDIR}/gstreamer-1.0 \
    -I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel} \
"
