# Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause-Clear


VBMETAIMAGE_TARGET ?= "vbmeta.img"

DEPENDS +=  "avbtool-native"
AVBSIGN_KEY = "${STAGING_DIR_NATIVE}${sysconfdir}/avb/sigkeys/testkey_rsa4096.pem"

# Function to calculate partition size
calculate_partition_size() {
    image_file=$1

    # Roundoff to next multiple of 4kb
    round_to=4096
    # Set 80KB for avbtool footer
    padding=81920

    if [ "$2" == "--logical" ]; then
        total_blocks=$(file "$image_file" | awk -F'Total of | 4096-byte' '{if (NF>1) print $2}' | awk '{print $1}')
        size_bytes=$(expr $total_blocks \* 4096)
    else
        size_bytes=$(stat -c%s "$image_file")
    fi

    temp_size=$(expr $size_bytes + $round_to - 1)
    rounded_size=$(expr $temp_size / $round_to \* $round_to)
    output=$(expr $rounded_size + $padding)
    echo $output
}

# Function to sign boot, vendor_boot, dtbo, vendor_dlkm images using avbtool.
avbsign_images[dirs] = "${DEPLOY_DIR_IMAGE}/${IMAGE_BASENAME}"
avbsign_images() {

    #sign boot image
    avbtool.py add_hash_footer --image ${BOOTIMAGE_TARGET} \
                               --partition_name boot \
                               --key ${AVBSIGN_KEY} \
                               --algorithm SHA256_RSA4096 \
                               --partition_size $(calculate_partition_size "boot.img") \
                               --prop com.android.build.boot.os_version:4.0.26 \
                               --prop com.android.build.boot.security_patch:2025-05-01 \
                               --rollback_index 0

    #sign vendor boot image
    avbtool.py add_hash_footer --image ${VBOOTIMAGE_TARGET} \
                               --partition_name vendor_boot \
                               --key ${AVBSIGN_KEY} \
                               --algorithm SHA256_RSA4096 \
                               --partition_size $(calculate_partition_size "vendor_boot.img") \
                               --prop com.android.build.vendor_boot.os_version:4.0.26 \
                               --prop com.android.build.vendor_boot.security_patch:2025-05-01 \
                               --rollback_index 0

    # sign dtbo image
    avbtool.py add_hash_footer --image ${DTBOIMAGE_TARGET} \
                               --partition_name dtbo \
                               --key ${AVBSIGN_KEY} \
                               --algorithm SHA256_RSA4096 \
                               --partition_size $(calculate_partition_size "dtbo.img") \
                               --prop com.android.build.dtbo.os_version:4.0.26 \
                               --prop com.android.build.dtbo.security_patch:2025-05-01 \
                               --rollback_index 0

}

# Generate vbmeta and vbmeta_system images.
do_makevbmeta_images[dirs] = "${DEPLOY_DIR_IMAGE}/${IMAGE_BASENAME}"
do_makevbmeta_images[prefuncs] += " avbsign_images"
do_makevbmeta_images() {
    # vbmeta image
    avbtool.py make_vbmeta_image --output ${VBMETAIMAGE_TARGET} \
                                 --key ${AVBSIGN_KEY} \
                                 --algorithm SHA256_RSA4096 \
                                 --include_descriptors_from_image ${BOOTIMAGE_TARGET} \
                                 --include_descriptors_from_image ${VBOOTIMAGE_TARGET} \
                                 --include_descriptors_from_image ${DTBOIMAGE_TARGET} \
                                 --include_descriptors_from_image ${VDLKMIMAGE_TARGET} \
                                 --include_descriptors_from_image ${SYSTEMIMAGE_TARGET}


}
addtask makevbmeta_images after do_image_complete before do_build
