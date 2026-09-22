SUMMARY = "QVconv Plugin for GStreamer"
DESCRIPTION = "QTI color converter plugin for GStreamer, convert color formats between UYVY, NV12_UBWC, NV12, BGR and RGBA"
HOMEPAGE = "https://git.codelinaro.org/"
SECTION = "multimedia"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${QTI_LICENSE_DIR}/${LICENSE};md5=b796c0007db682166a1721da80267bb2"

DEPENDS += "\
    adreno \
    glib-2.0 \
    gstreamer1.0 \
    gstreamer1.0-plugins-base \
    ${@bb.utils.contains('MACHINE_FEATURES', 'qti-umd', '', 'videodlkm displaydlkm', d)} \
    virtual/kernel-headers \
    virtual/libc \
"

DEPENDS:remove:quin-gvm-gen4 = "displaydlkm"
DEPENDS:remove:qtiquingvm8295 = "displaydlkm"

SRC_URI = "${PATH_TO_REPO}/gstreamer/gst-plugins-qti-oss/.git;protocol=${PROTO};destsuffix=gstreamer/gst-plugins-qti-oss;usehead=1"
SRCREV = "${AUTOREV}"
S = "${WORKDIR}/gstreamer/gst-plugins-qti-oss/gst-plugin-qvconv"

#inherit python3native to export related STAGING ENVs
inherit meson pkgconfig qprebuilt python3native

CXXFLAGS += "\
    -I${STAGING_INCDIR} \
    -I${STAGING_INCDIR}/glib-2.0 \
    -I${STAGING_LIBDIR}/glib-2.0/include \
    -I${STAGING_INCDIR}/c++ \
    -I${STAGING_INCDIR}/c++/${TARGET_SYS} \
    -I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel} \
    -I${STAGING_INCDIR}/mm-core \
    -I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel}/display \
"

CXXFLAGS:remove:quin-gvm-gen4 = "-I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel}/display"
CXXFLAGS:remove:qtiquingvm8295 = "-I${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel}/display"

EXTRA_OEMESON += "\
    ${@bb.utils.contains('MACHINE_FEATURES', 'qti-umd', '-Duseumd=true', '', d)} \
"

EXTRA_OEMESON:append:quin-gvm-gen4 = " -Dmmmcolorfmt=false"
EXTRA_OEMESON:append:qtiquingvm8295 = " -Dmmmcolorfmt=false"

FILES:${PN} += "${libdir}/gstreamer-1.0/*.so"
