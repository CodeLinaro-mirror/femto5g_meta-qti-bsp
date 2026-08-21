# soc-modules_6.12.bbappend
#
# Extends soc-modules_6.12.bb for gvm-gen4-5-virtio:
#
# Patch: wire drivers/Makefile and drivers/virtio/Makefile for
# virtio-gpu OOT Kbuild (applied to soc-repo at fetch time).
#
# copy_common_sources:append: copy virtio-gpu sources from ACK
# common/ into soc-repo at configure time:
#   - virtio_dma_buf.c (VIRTIO_DMA_SHARED_BUFFER dependency)
#   - drivers/gpu/drm/virtio/ (virtio-gpu.ko sources + Makefile)

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = " file://ack/0001-soc-repo-virtio-gpu-wire-OOT-Kbuild-Makefile.patch"

copy_common_sources:append() {
    COMMON="${WORKDIR}/kernel/kernel-${PV}/kernel_platform/common"

    cp -f "${COMMON}/drivers/virtio/virtio_dma_buf.c" \
          "${SOC_REPO}/drivers/virtio/"

    install -d "${SOC_REPO}/drivers/gpu/drm/virtio"
    cp -rf "${COMMON}/drivers/gpu/drm/virtio/." \
           "${SOC_REPO}/drivers/gpu/drm/virtio"
}
