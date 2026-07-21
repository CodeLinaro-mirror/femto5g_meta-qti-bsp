# Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
# SPDX-License-Identifier: BSD-3-Clause-Clear

CONFLICT_MACHINE_FEATURES += " dm-verity-bootloader dm-verity-none dm-verity-initramfs dm-verity-initramfs-v2 dm-verity-initramfs-v3"

BLOCK_SIZE = "4096"
SECTOR_SIZE = "512"


populate_verity_cpio_cmdline () {
    # Re-parse the values that have already been generated in system_verity_metadata.txt
    # to avoid duplicate verity metadata creation.
    data_blocks=`awk -F ':' '{ if ($1 == "Data blocks") print $2 }' ${WORKDIR}/system_verity_metadata.txt | sed "s/^[ 	]*//"`
    data_sectors=`expr ${BLOCK_SIZE} / ${SECTOR_SIZE} \* ${data_blocks}`
    fec_offset=`awk -F ':' '{ if ($1 == "fec_offset") print $2 }' ${WORKDIR}/system_verity_metadata.txt | sed "s/^[ 	]*//"`
    fec_start_block=`expr ${fec_offset} / ${BLOCK_SIZE}`
    root_hash=`awk -F ':' '{ if ($1 == "Root hash") print $2 }' ${WORKDIR}/system_verity_metadata.txt | sed "s/^[ 	]*//"`

    # Write verity params for ABL to parse at runtime via GetLEVerityCmdLine().
    # Format: verity="<size_in_sectors> <data_blocks> <root_hash> <fec_start_block>"
    # ABL reads this, looks up partition index at runtime, and injects dm-mod.create= into kernel cmdline.
    echo -ne "verity=\"${data_sectors} ${data_blocks} ${root_hash} ${fec_start_block}\"" > ${WORKDIR}/verity-cmdline
}
do_makesystem[postfuncs] += "populate_verity_cpio_cmdline"


##### Generate boot.img with verity cmdline injected ######
BOOTIMGDEPLOYDIR = "${WORKDIR}/deploy-${PN}-bootimage-complete"

INITRAMFS_IMAGE ?= ''
RAMDISK ?= "${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE}-${MACHINE}.${INITRAMFS_FSTYPES}"
VRAMDISK = "${DEPLOY_DIR_IMAGE}/${VENDOR_INITRAMFS_IMAGE}-${MACHINE}.${INITRAMFS_FSTYPES}"

def get_ramdisk_path(d):
    if os.path.exists(d.getVar('RAMDISK')):
        return '%s' %(d.getVar('RAMDISK'))
    return '/dev/null'

RAMDISK_PATH = "${@get_ramdisk_path(d)}"

MKBOOTUTIL = '${@oe.utils.conditional("PREFERRED_PROVIDER_virtual/mkbootimg", "mkbootimg-gki", "scripts/mkbootimg.py", "mkbootimg", d)}'

python do_makeboot () {
    import subprocess, os

    # If qcpioimage is inherited (CPIODIR is set), let ABL handle verity cmdline
    verity_args = ''
    if not d.getVar('CPIODIR'):
        verity_cmdline_file = d.getVar('WORKDIR') + '/verity-cmdline'
        if os.path.exists(verity_cmdline_file):
            with open(verity_cmdline_file, 'r') as f:
                verity_args = ' ' + f.read().strip()

    # Set cmdline with verity args appended
    kernel_cmd = d.getVar('KERNEL_CMD_PARAMS', True) + verity_args

    mkboot_bin_path = d.getVar('STAGING_BINDIR_NATIVE', True) + '/' + d.getVar('MKBOOTUTIL')

    # Build args list to avoid shell quote interpretation
    args = [mkboot_bin_path, '--kernel', d.getVar('DEPLOY_DIR_IMAGE', True) + '/' + d.getVar('KERNEL_IMAGETYPE', True)]

    # cmdline arg
    if ((int(d.getVar('BOOT_HEADER_VERSION') or '0') < 3) or (d.getVar('SKIP_VENDOR_BOOT') or 'True') == 'True'):
        args += ['--cmdline', kernel_cmd]
    else:
        args += ['--vendor_cmdline', kernel_cmd]

    args += ['--pagesize', d.getVar('PAGE_SIZE', True)]
    args += ['--base', d.getVar('KERNEL_BASE', True)]
    args += ['--ramdisk', d.getVar('RAMDISK_PATH')]
    args += ['--ramdisk_offset', '0x0']

    if bb.utils.contains('MACHINE_FEATURES', 'nand-boot', True, False, d):
        args += ['--tags-addr', d.getVar('KERNEL_TAGS_OFFSET')]
    if (int(d.getVar('BOOT_HEADER_VERSION') or '0') >= 2):
        args += ['--header_version', d.getVar('BOOT_HEADER_VERSION')]
        args += ['--dtb', d.getVar('DEPLOY_DIR_IMAGE', True) + '/DTOverlays/dtb.img']

    if ((int(d.getVar('BOOT_HEADER_VERSION') or '0') >= 3) and (d.getVar('SKIP_VENDOR_BOOT') or 'True') == 'False'):
        args += ['--vendor_ramdisk', d.getVar('VRAMDISK')]
        args += ['--vendor_boot', d.getVar('VBOOTIMAGE_TARGET')]

    args += ['--output', d.getVar('BOOTIMAGE_TARGET', True)]

    bb.debug(1, 'dm-verity-cpio-cmdline do_makeboot cmd: %s' % ' '.join(args))
    try:
        ret = subprocess.check_output(args)
    except subprocess.CalledProcessError as e:
        bb.error('dm-verity-cpio-cmdline do_makeboot failed: %s' % str(e))
}
do_makeboot[dirs]      = "${BOOTIMGDEPLOYDIR}/${IMAGE_BASENAME}"
do_makeboot[depends] += "virtual/kernel:do_deploy virtual/mkbootimg-native:do_populate_sysroot"
SSTATETASKS += "do_makeboot"
SSTATE_SKIP_CREATION_task-makeboot = '1'
do_makeboot[sstate-inputdirs] = "${BOOTIMGDEPLOYDIR}"
do_makeboot[sstate-outputdirs] = "${DEPLOY_DIR_IMAGE}"
do_makeboot[stamp-extra-info] = "${MACHINE_ARCH}"

python do_makeboot_setscene () {
    sstate_setscene(d)
}
addtask do_makeboot_setscene
addtask do_makeboot after do_makesystem before do_image_complete

IMAGE_INSTALL:append = " blkid-cache-init"
