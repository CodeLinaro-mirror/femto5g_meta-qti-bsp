inherit core-image

DEPENDS += "${@bb.utils.contains('DISTRO_FEATURES', 'qti-avb', 'avbtool-native', '', d)}"

# AVB operates on one filesystem image even when multiple image types are built.
def get_avb_image_fstype(d):
    image_fstypes = (d.getVar('IMAGE_FSTYPES') or '').split()
    if not image_fstypes:
        bb.fatal("IMAGE_FSTYPES is not set; cannot determine AVB image filesystem type")
    if 'ext4' in image_fstypes:
        return 'ext4'
    return image_fstypes[0]

QTI_AVB_IMAGE_FSTYPE := "${@get_avb_image_fstype(d)}"

do_make_avb_image(){
    if ${@bb.utils.contains('DISTRO_FEATURES', 'qti-avb', 'true', 'false', d)}; then
        # In qti-image-headless.bb, need to set IMAGE_ROOTFS_ZIZE to 700M, set the value of the judgment of 500M to skip redefinition.
        if [ "${IMAGE_ROOTFS_SIZE}" -lt "524288" ]; then
            # core minimal image define the IMAGE_ROOTFS_SIZE to 8192, no way to calculate
            # an appropriate partition size for hashtree footer on top of rootfs image.
            rootfs_size_kb=1572864
        else
            rootfs_size_kb=${IMAGE_ROOTFS_SIZE}
        fi

        rootfs_size=$(expr $rootfs_size_kb \* 1024)
        overhead_size_kb=$(expr $rootfs_size_kb / 5)
        overhead_size=$(expr $overhead_size_kb \* 1024)

        if [ "$(expr $overhead_size % 4096)" != "0" ]; then
            overhead_size=$(expr $(expr 4096 - $(expr $overhead_size % 4096)) + $overhead_size)
        fi
        rootfs_partition_size=$(expr $rootfs_size + $overhead_size)

        if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-hypervisor', 'true', 'false', d)}; then
            #For lvgvm avb2.0, add hashtree for system image and generate vbmeta.img.
            avbtool add_hashtree_footer \
                --image ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}.${QTI_AVB_IMAGE_FSTYPE} \
                --partition_name system \
                --partition_size ${rootfs_partition_size} \
                --hash_algorithm sha256 \
                --do_not_generate_fec
            avbtool make_vbmeta_image \
                --include_descriptors_from_image ${DEPLOY_DIR_IMAGE}/${BOOTIMAGE_TARGET} \
                --include_descriptors_from_image ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}.${QTI_AVB_IMAGE_FSTYPE} \
                --setup_rootfs_from_kernel ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}.${QTI_AVB_IMAGE_FSTYPE} \
                --algorithm SHA256_RSA4096 \
                --key ${STAGING_DIR_NATIVE}${sysconfdir}/signing_tools/sigkeys/vbgvm_private_key_4096.pem \
                --rollback_index 0 \
                --output ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}-vbmeta.img
            # Workaround, to keep two vbmeta images here with different vbmeta name.
            install -m 644 ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}-vbmeta.img ${DEPLOY_DIR_IMAGE}/${PRODUCT}-vbmeta.img
            install -m 644 ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}-vbmeta.img ${DEPLOY_DIR_IMAGE}/vbmeta.img
        else
            #For lv avb2.0, add hashtree for system image and generate vbmeta.img.
            avbtool add_hashtree_footer \
                --image ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}.${QTI_AVB_IMAGE_FSTYPE} \
                --partition_size ${rootfs_partition_size} \
                --partition_name system  \
                --hash_algorithm sha256 \
                --key ${STAGING_DIR_NATIVE}${sysconfdir}/signing_tools/sigkeys/testkey_rsa4096.pem \
                --rollback_index 0 \
                --do_not_generate_fec
            if [ -f ${DEPLOY_DIR_IMAGE}/${PRODUCT}-vendor_boot.img ]; then
               avbtool make_vbmeta_image \
                   --include_descriptors_from_image ${DEPLOY_DIR_IMAGE}/${BOOTIMAGE_TARGET} \
                   --include_descriptors_from_image ${DEPLOY_DIR_IMAGE}/${PRODUCT}-dtbo.img \
                   --include_descriptors_from_image ${DEPLOY_DIR_IMAGE}/${PRODUCT}-vendor_boot.img \
                   --include_descriptors_from_image ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}.${QTI_AVB_IMAGE_FSTYPE} \
                   --setup_rootfs_from_kernel ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}.${QTI_AVB_IMAGE_FSTYPE} \
                   --algorithm SHA256_RSA4096 \
                   --key ${STAGING_DIR_NATIVE}${sysconfdir}/signing_tools/sigkeys/testkey_rsa4096.pem \
                   --rollback_index 0 \
                   --prop "com.android.build.boot.security_patch:${@time.strftime('%Y-%m-%d',time.gmtime())}" \
                   --prop "com.android.build.boot.os_version:${@time.strftime('%Y-%m-%d',time.gmtime())}" \
                   --output ${DEPLOY_DIR_IMAGE}/${VBMETAIMAGE_TARGET}
            else
               avbtool make_vbmeta_image \
                   --include_descriptors_from_image ${DEPLOY_DIR_IMAGE}/${BOOTIMAGE_TARGET} \
                   ${@bb.utils.contains('MACHINE_FEATURES', 'dt-overlay', '--include_descriptors_from_image ${DEPLOY_DIR_IMAGE}/${PRODUCT}-dtbo.img', '', d)} \
                   --include_descriptors_from_image ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}.${QTI_AVB_IMAGE_FSTYPE} \
                   --setup_rootfs_from_kernel ${DEPLOY_DIR_IMAGE}/${IMAGE_LINK_NAME}.${QTI_AVB_IMAGE_FSTYPE} \
                   --algorithm SHA256_RSA4096 \
                   --key ${STAGING_DIR_NATIVE}${sysconfdir}/signing_tools/sigkeys/testkey_rsa4096.pem \
                   --rollback_index 0 \
                   --prop "com.android.build.boot.security_patch:${@time.strftime('%Y-%m-%d',time.gmtime())}" \
                   --prop "com.android.build.boot.os_version:${@time.strftime('%Y-%m-%d',time.gmtime())}" \
                   --output ${DEPLOY_DIR_IMAGE}/${VBMETAIMAGE_TARGET}
            fi
        fi
    fi
}

addtask do_make_avb_image after do_image_complete before do_build
