FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
QCVIRTIO_LINUX_ACK_FILES := "${THISDIR}/files/ack"

SRC_URI:append = " file://ack/virtio_camera.fragment"

do_shared_workdir[file-checksums] += "${QCVIRTIO_LINUX_ACK_FILES}/virtio_camera.fragment:True"
