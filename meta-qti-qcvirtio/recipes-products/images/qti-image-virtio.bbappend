IMAGE_INSTALL += "\
    ${@bb.utils.contains('PREFERRED_VERSION_linux-msm', '6.12', 'virtio-camera', '', d)} \
    "
