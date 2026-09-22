SUMMARY = "Video external buffer pool sink plugin for GStreamer"
DESCRIPTION = "Gstreamer video sink plugin to provide external buffer pool"
HOMEPAGE = "https://git.codelinaro.org/"
SECTION = "multimedia"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${QTI_LICENSE_DIR}/${LICENSE};md5=b796c0007db682166a1721da80267bb2"

DEPENDS += "\
    displaydlkm \
    display-commonsys-intf-linux \
    gbm \
    gbm-headers \
    glib-2.0 \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    ${@bb.utils.contains('MACHINE_FEATURES', 'qti-umd', '', 'videodlkm', d)} \
    virtual/kernel-headers \
"

DEPENDS:remove:quin-gvm-gen4 = "displaydlkm"
DEPENDS:remove:qtiquingvm8295 = "displaydlkm"

SRC_URI = "${PATH_TO_REPO}/gstreamer/gst-plugins-qti-oss/.git;protocol=${PROTO};destsuffix=gstreamer/gst-plugins-qti-oss;usehead=1"
SRCREV = "${AUTOREV}"
S = "${WORKDIR}/gstreamer/gst-plugins-qti-oss/gst-plugin-extpoolsink"

inherit meson pkgconfig

CFLAGS += "\
    -I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel} \
    -I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel}/display \
"

CFLAGS:remove:quin-gvm-gen4 = "-I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel}/display"
CFLAGS:remove:qtiquingvm8295 = "-I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel}/display"

EXTRA_OEMESON:append = " \
    ${@bb.utils.contains('MACHINE_FEATURES', 'qti-umd', '-Duseumd=true', '', d)} \
"

EXTRA_OEMESON:append:quin-gvm-gen4 = " -Dmmmcolorfmt=false"
EXTRA_OEMESON:append:qtiquingvm8295 = " -Dmmmcolorfmt=false"

SOLIBS = ".so"
FILES_SOLIBSDEV = ""

FILES:${PN} += "${libdir}/gstreamer-1.0/*.so"
