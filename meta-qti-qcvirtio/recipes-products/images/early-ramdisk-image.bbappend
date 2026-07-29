# Append virtio-gpu and its virtio_dma_buf dependency to the early-ramdisk
# image for gvm-gen4-5-virtio. Both modules must be present in the ramdisk
# so the GPU is available before rootfs mount.
#
# Note: EARLY_RAMDISK_KERNEL_MODULES in conf/machine/ cannot be used here
# because BitBake loads only the first machine conf match from BBPATH, and
# meta-qti-headless precedes meta-qti-qcvirtio. A bbappend on IMAGE_INSTALL
# is the correct mechanism for a secondary layer.

KERNEL_VERSION = "${@oe.utils.read_file('${STAGING_KERNEL_BUILDDIR}/kernel-abiversion')}"

IMAGE_INSTALL:append:gvm-gen4-5-virtio = " \
    kernel-module-virtio-dma-buf-${KERNEL_VERSION} \
    kernel-module-virtio-gpu-${KERNEL_VERSION} \
"
