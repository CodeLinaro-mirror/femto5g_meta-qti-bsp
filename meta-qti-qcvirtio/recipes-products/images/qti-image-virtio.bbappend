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

# --- WIC single-disk image generation ---
# libsparse-native provides simg2img for sparse-to-raw conversion.
do_image_wic[depends] += "libsparse-native:do_populate_sysroot"
do_image_wic[depends] += "qti-image-virtio:do_image_ext4"
do_image_wic[depends] += "systemd-boot:do_deploy"
do_image_wic[depends] += "early-ramdisk-image:do_image_complete"

# Generate a single GPT disk image for visor.
IMAGE_FSTYPES:append:gvm-gen4-5-virtio = " wic"
WKS_FILE:gvm-gen4-5-virtio = "qti-lv-single-disk.wks"
INITRAMFS_IMAGE:gvm-gen4-5-virtio = "early-ramdisk-image"
INITRAMFS_FSTYPES:gvm-gen4-5-virtio = "cpio.lz4"

# Patch fstab in IMAGE_ROOTFS before WIC assembles the rootfs partition,
# then restore the original after. This ensures:
#   - WIC rootfs partition gets vda4/vda5/vda6 (correct for vSoC visor)
#   - ext4 rootfs (built before WIC) keeps vdb/vdc/vde (correct for soc)
# NOTE: do_image_wic:prepend() is silently dropped (task generated via d.setVar).
#       Use prefuncs instead — same mechanism as set_image_size.
patch_fstab_for_wic() {
    if [ "${MACHINE}" = "gvm-gen4-5-virtio" ]; then
        # Guard against double-patching on rebuild after failure
        if grep -q '/dev/vda4' ${IMAGE_ROOTFS}/etc/fstab; then
            bbnote "fstab already patched for WIC, skipping"
            return 0
        fi
        cp ${IMAGE_ROOTFS}/etc/fstab ${IMAGE_ROOTFS}/etc/fstab.soc
        sed -i 's|/dev/vdb|/dev/vda4|g' ${IMAGE_ROOTFS}/etc/fstab
        sed -i 's|/dev/vdc|/dev/vda5|g' ${IMAGE_ROOTFS}/etc/fstab
        sed -i 's|/dev/vde|/dev/vda6|g' ${IMAGE_ROOTFS}/etc/fstab
    fi
}
do_image_wic[prefuncs] += "${@bb.utils.contains('MACHINE', 'gvm-gen4-5-virtio', 'patch_fstab_for_wic', '', d)}"

do_image_wic:append:gvm-gen4-5-virtio() {
    # Restore original fstab after WIC assembly — keeps soc ext4 untouched
    if [ -f "${IMAGE_ROOTFS}/etc/fstab.soc" ]; then
        mv ${IMAGE_ROOTFS}/etc/fstab.soc ${IMAGE_ROOTFS}/etc/fstab
    fi
    # Clean up intermediate raw images
    rm -f "${DEPLOY_DIR_IMAGE}/usrfs-raw.ext4"
    rm -f "${DEPLOY_DIR_IMAGE}/persist-raw.ext4"
}

# Convert Android sparse usrfs/persist images to raw ext4 before WIC assembly.
# The WIC rawcopy plugin requires raw (non-sparse) ext4.
convert_sparse_to_raw() {
    if [ "${MACHINE}" != "gvm-gen4-5-virtio" ]; then
        return 0
    fi

    SIMG2IMG="${STAGING_BINDIR_NATIVE}/simg2img"

    if [ ! -x "${SIMG2IMG}" ]; then
        bbfatal "simg2img not found at ${SIMG2IMG}"
    fi

    SPARSE_USRFS=$(find "${DEPLOY_DIR_IMAGE}" -maxdepth 1 -name '*usrfs*.ext4' ! -name '*-raw.ext4' -print -quit)
    SPARSE_PERSIST=$(find "${DEPLOY_DIR_IMAGE}" -maxdepth 1 -name '*persist*.ext4' ! -name '*-raw.ext4' -print -quit)
    RAW_USRFS="${DEPLOY_DIR_IMAGE}/usrfs-raw.ext4"
    RAW_PERSIST="${DEPLOY_DIR_IMAGE}/persist-raw.ext4"

    # Remove conversion outputs when the WIC task exits, including failures.
    trap 'rm -f "${RAW_USRFS}" "${RAW_PERSIST}"' EXIT

    if [ -z "${SPARSE_USRFS}" ] || [ ! -f "${SPARSE_USRFS}" ]; then
        bbfatal "Sparse usrfs image not found in ${DEPLOY_DIR_IMAGE}"
    fi
    if [ -z "${SPARSE_PERSIST}" ] || [ ! -f "${SPARSE_PERSIST}" ]; then
        bbfatal "Sparse persist image not found in ${DEPLOY_DIR_IMAGE}"
    fi

    bbnote "Converting $(basename "${SPARSE_USRFS}") to raw ext4"
    "${SIMG2IMG}" "${SPARSE_USRFS}" "${RAW_USRFS}" || bbfatal "simg2img failed for usrfs"

    bbnote "Converting $(basename "${SPARSE_PERSIST}") to raw ext4"
    "${SIMG2IMG}" "${SPARSE_PERSIST}" "${RAW_PERSIST}" || bbfatal "simg2img failed for persist"

    bbnote "Sparse-to-raw conversion complete"
}
do_image_wic[prefuncs] += "${@bb.utils.contains('MACHINE', 'gvm-gen4-5-virtio', 'convert_sparse_to_raw', '', d)}"
