IMAGE_INSTALL += "\
    ${@bb.utils.contains('PREFERRED_VERSION_linux-msm', '6.12', 'virtio-camera', '', d)} \
    "

IMAGE_INSTALL:append = " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'opengl', \
        'mesa-megadriver libgallium libegl-mesa libgles2-mesa libgbm', '', d)} \
"
