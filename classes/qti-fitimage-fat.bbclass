#Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
#SPDX-License-Identifier: BSD-3-Clause-Clear
# Packages boot.itb (qti-fitimage.bbclass) and boot.scr (u-boot-scr-qti-fit
# recipe) into a FAT-formatted container image, so a bootloader can read both
# off a plain VFAT boot partition.
#
# boot.itb lives at ${DEPLOY_DIR_IMAGE}/${IMAGE_BASENAME}/ (set by
# qti-fitimage.bbclass's do_make_fitimage). boot.scr lives at the top level
# of ${DEPLOY_DIR_IMAGE} instead - it comes from the standalone
# u-boot-scr-qti-fit recipe's do_deploy, which (like any non-image recipe
# using deploy.bbclass) deploys directly into DEPLOY_DIR_IMAGE, not into an
# image-specific subdirectory. Both paths are read explicitly below rather
# than assuming the two files sit side by side.

BOOTFAT_TARGET ?= "boot.vfat"
BOOTFAT_VOLUME_ID ?= "BOOT"
BOOTFAT_EXTRA_SPACE ?= "1024"

do_make_bootfat() {
	itb="${DEPLOY_DIR_IMAGE}/${IMAGE_BASENAME}/${FITIMAGE_TARGET}"
	scr="${DEPLOY_DIR_IMAGE}/boot.scr"
	out="${DEPLOY_DIR_IMAGE}/${IMAGE_BASENAME}/${BOOTFAT_TARGET}"

	if [ ! -e "$itb" ]; then
		bbfatal "do_make_bootfat: $itb not found"
	fi
	if [ ! -e "$scr" ]; then
		bbfatal "do_make_bootfat: $scr not found"
	fi

	itb_size=$(stat -c%s "$itb")
	scr_size=$(stat -c%s "$scr")
	total_kb=$(expr $(expr $(expr $itb_size + $scr_size) / 1024) + ${BOOTFAT_EXTRA_SPACE})
	# Round up to the next whole MB, since mkfs.vfat sizing wants a clean value.
	size_kb=$(expr $(expr $(expr $total_kb + 1023) / 1024) \* 1024)

	rm -f "$out"
	"${STAGING_SBINDIR_NATIVE}/mkfs.vfat" -C "$out" "$size_kb" -n "${BOOTFAT_VOLUME_ID}"
	MTOOLS_SKIP_CHECK=1 "${STAGING_BINDIR_NATIVE}/mcopy" -i "$out" "$itb" ::/
	MTOOLS_SKIP_CHECK=1 "${STAGING_BINDIR_NATIVE}/mcopy" -i "$out" "$scr" ::/
}
do_make_bootfat[nostamp] = "1"
do_make_bootfat[dirs] = "${DEPLOY_DIR_IMAGE}/${IMAGE_BASENAME}"
do_make_bootfat[depends] += " dosfstools-native:do_populate_sysroot mtools-native:do_populate_sysroot u-boot-scr-qti-fit:do_deploy virtual/kernel:do_deploy"

# do_make_fitimage (qti-fitimage.bbclass) is unconditionally present in the
# task graph, ordering after it.
python () {
    bb.build.addtask('do_make_bootfat', 'do_image', 'do_make_fitimage', d)
}
