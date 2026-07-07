SUMMARY = "Multimedia libraries and SDK, QTI OpenMax IL implementation"
DESCRIPTION = "OpenMAX is a royalty-free, cross-platform API that provides comprehensive streaming media codec and application portability by enabling \
               accelerated multimedia components to be developed, integrated and programmed across multiple operating systems and silicon platforms. \
               OpenMAX IL serves as a low-level interface for video decoder/encoder used in embedded and/or mobile devices. It gives applications and media \
               frameworks the ability to interface with multimedia codecs and supporting components in a unified manner. This component implemented standard \
               OMX IL 1.1.2 interface with QTI extension. With those extension, application developer could take full advantage of hardware capability more easily. \
               Besides OMX IL implement, this component also provides a series of hardware accelerated 2d video conversion API, including color space convert, \
               scale and crop for video frame data. \
              "
HOMEPAGE = "https://git.codelinaro.org"
SECTION = "multimedia"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${WORKDIR}/hardware/qcom/media/NOTICE;md5=67f520c8e55cf33925e4d738d7f4a5f4"

DEPENDS += "\
    display-commonsys-intf-linux \
    gbm \
    gbm-headers \
    glib-2.0 \
    libcutils \
    libdrm \
    ${@bb.utils.contains("PREFERRED_VERSION_linux-msm", "5.15", 'libdmabufheap', "", d)} \
    libion \
    libutils \
    virtual/kernel-headers \
    media-plugin-headers \
    mm-video-noship \
    system-core-headers \
    ${@bb.utils.contains("PREFERRED_VERSION_linux-msm", "5.15", "videodlkm", "", d)} \
    virtual/egl \
    virtual/libc \
    wayland \
"

PR = "r1"

SRC_URI = "${PATH_TO_REPO}/hardware/qcom/media/.git;protocol=${PROTO};destsuffix=hardware/qcom/media;usehead=1"
SRCREV = "${AUTOREV}"
S = "${WORKDIR}/hardware/qcom/media"

inherit autotools systemd
SYSTEMD_SERVICE:${PN} = "${@bb.utils.contains('DISTRO_FEATURES','early_init','video_early_demo.service','',d)}"

EXTRA_OECONF:append = " \
    --with-sanitized-headers=${STAGING_INCDIR}/${PREFERRED_PROVIDER_virtual/kernel} \
    --with-cutils-headers=${STAGING_INCDIR}/cutils/ \
    --enable-use-glib='yes' \
    --enable-target-uses-ion='yes' \
    --enable-target-uses-gbm='yes' \
    --enable-target-uses-media-extensions='no' \
    --enable-build-mm-video='yes' \
    --enable-is-ubwc-supported='yes' \
    --enable-build-swcodec='yes' \
    --enable-target-output-deinterlaced='yes' \
    ${@bb.utils.contains('MACHINE_FEATURES', 'qti-hypervisor', ' --enable-target-hypervisor=yes', '', d)} \
    ${@bb.utils.contains('MACHINE_FEATURES', 'qti-direwolf-vpu', ' --enable-target-direwolf-vpu=yes', '', d)} \
    ${@bb.utils.contains("PREFERRED_VERSION_linux-msm", "5.15", '--enable-target-uses-dmabufheap=yes', "", d)} \
"

CPPFLAGS += "\
    -I${STAGING_INCDIR} \
    -I${STAGING_INCDIR}/drm \
    -I${STAGING_INCDIR}/EGL \
    -I${STAGING_INCDIR}/GLES2 \
    -I${STAGING_INCDIR}/glib-2.0 \
    -I${STAGING_LIBDIR}/glib-2.0/include \
    -I${STAGING_LIBDIR}/glib-2.0/glib \
    -I${STAGING_INCDIR}/c++ \
    -I${STAGING_INCDIR}/c++/${TARGET_SYS} \
    -I${STAGING_INCDIR}/ion_headers  \
    -I${STAGING_INCDIR}/disp-commonsys-intf/display \
    -I${STAGING_INCDIR}/mm-video/swvdec \
    -I${STAGING_INCDIR}/mm-video/swvenc \
    -include stdint.h \
    -Wno-format-truncation \
"

LDFLAGS += "\
    -lglib-2.0 \
    -lgbm \
    -ldrm \
    -lwayland-client \
    -lEGL \
"

do_install:append() {
   install -d ${D}${includedir}/mm-core
   install -m 0644 ${S}/mm-core/inc/*.h -D ${D}${includedir}/mm-core/
   install -d ${D}${includedir}/venc/inc
   install -m 0644 ${S}/mm-video-v4l2/vidc/venc/inc/omx_video_common.h -D ${D}${includedir}/venc/inc/
   install -m 0644 ${S}/mm-video-v4l2/vidc/venc/inc/omx_video_base.h -D ${D}${includedir}/venc/inc/

   install -d ${D}${includedir}/vdec/inc
   install -m 0644 ${S}/mm-video-v4l2/vidc/vdec/inc/*.h -D ${D}${includedir}/vdec/inc/

   install -m 0644 ${S}/mm-video-v4l2/vidc/common/inc/*.h -D ${D}${includedir}/
   install -d ${D}${includedir}/libstagefrighthw
   install -m 0644 ${S}/libstagefrighthw/QComOMXMetadata.h -D ${D}${includedir}/libstagefrighthw/
   install -m 0644 ${S}/libc2dcolorconvert/C2DColorConverter.h ${D}${includedir}/

   if ${@bb.utils.contains('DISTRO_FEATURES', 'early_init', 'true', 'false', d)}; then
       install -m 0777 ${THISDIR}/test_1080p.h264 -D ${D}${bindir}/test_1080p.h264
       install -m 0755 ${THISDIR}/video_dec_demo.sh -D ${D}${sbindir}/video_dec_demo.sh
       install -m 0644 ${THISDIR}/video_early_demo.service -D ${D}${systemd_system_unitdir}/video_early_demo.service
   fi
}

PACKAGE_ARCH = "${MACHINE_ARCH}"

FILES:${PN} += "\
    ${datadir}/* \
    ${systemd_system_unitdir}/*"

SOLIBS = ".so"
FILES_SOLIBSDEV = ""
