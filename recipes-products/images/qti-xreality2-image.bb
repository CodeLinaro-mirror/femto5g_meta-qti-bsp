# Provides packages required to build
# QTI Linux eXtended Reality image with splitxr support.

require qti-xreality2-base-image.bb

CORE_IMAGE_EXTRA_INSTALL:remove:seraph = " \
    packagegroup-qti-bluetooth \
    packagegroup-qti-audio \
    packagegroup-qti-video \
    packagegroup-qti-display \
    packagegroup-qti-camera \
    packagegroup-qti-gst \
    packagegroup-qti-eva \
    packagegroup-qti-ppat \
    packagegroup-qti-securemsm \
    packagegroup-qti-mmframeworks \
    packagegroup-qti-sensors-see \
    packagegroup-qti-test-sensors-see \
    packagegroup-qti-qesdk-core \
    packagegroup-qti-dcf-lib \
    packagegroup-qti-dcf-hal \
    libmeminfo \
    kernel-modules \
"

CORE_IMAGE_EXTRA_INSTALL:append:seraph = " \
    userspace-resource-manager \
    userspace-resource-manager-extensions \
"
