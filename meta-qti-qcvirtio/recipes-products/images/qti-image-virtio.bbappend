IMAGE_INSTALL += "libdrm-tests"

IMAGE_INSTALL += "\
    ${@bb.utils.contains('PREFERRED_VERSION_linux-msm', '6.12', 'virtio-camera', '', d)} \
    "

IMAGE_INSTALL:append = " \
    ${@bb.utils.contains('DISTRO_FEATURES', 'opengl', \
        'mesa-megadriver libgallium libegl-mesa libgles2-mesa libgbm', '', d)} \
"

IMAGE_INSTALL += "gstreamer1.0 gstreamer1.0-plugins-base gstreamer1.0-plugins-good gstreamer1.0-plugins-bad"

IMAGE_INSTALL:append:gvm-gen4-5-virtio = " weston weston-init weston-examples libgallium"
