# Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause-Clear

## Generate nvram image

# Convert human readable partition sizes into bytes
NVRAM_IMAGE_ROOTFS_SIZE  = "${@get_size_in_bytes(d.getVar('NVRAM_SIZE_EXT4') or '50MiB')}"

NVRAMIMAGE_TARGET ?= "nvram.img"
NVRAMIMAGE_MAP_TARGET ?= "nvram.map"

do_makenvram[dirs] = "${IMGDEPLOYDIR}/${IMAGE_BASENAME}"

do_makenvram() {
    if [ ! -d "${IMAGE_ROOTFS}/nvram" ]; then
    bbwarn "nvram directory does not exist in rootfs, creating empty image"
    mkdir -p "${IMAGE_ROOTFS}/nvram"
    fi

    make_ext4fs -B ${IMGDEPLOYDIR}/${IMAGE_BASENAME}/${NVRAMIMAGE_MAP_TARGET} \
                -s -l ${NVRAM_IMAGE_ROOTFS_SIZE} \
                ${IMGDEPLOYDIR}/${IMAGE_BASENAME}/${NVRAMIMAGE_TARGET} \
                ${IMAGE_ROOTFS}/nvram || bbfatal "make_ext4fs failed to generate nvram image"
}


# It must be before do_makesystem to remove contents from /nvram in rootfs
addtask do_makenvram after do_image before do_makesystem

