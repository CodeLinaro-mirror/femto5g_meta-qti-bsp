# Convert human readable partition sizes into bytes
SYSTEM_IMAGE_ROOTFS_SIZE   = "${@get_size_in_bytes(d.getVar('SYSTEM_SIZE_EXT4') or '256MB')}"

# support veritysetup tools.
DEPENDS += "cryptsetup-native"
DEPENDS += "fec-native avbtool-native"

# if A/B support is supported, generate OTA pkg by default.
GENERATE_AB_OTA_PACKAGE ?= "${@bb.utils.contains('COMBINED_FEATURES', 'qti-ab-boot', '1', '', d)}"

# List all mount points
MNT_POINTS = "${MACHINE_MNT_POINTS} ${GENERATED_MACHINE_MNT_POINTS}"

QIMGEXT4CLASSES  = ""
QIMGEXT4CLASSES += "${@bb.utils.contains('GENERATE_AB_OTA_PACKAGE', '1', 'ab-ota-ext4', '', d)}"
QIMGEXT4CLASSES += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-recovery', 'ota-ext4', '', d)}"
QIMGEXT4CLASSES += "${@bb.utils.contains('MNT_POINTS', '/cache', 'qimage-cache-ext4', '', d)}"
QIMGEXT4CLASSES += "${@bb.utils.contains('MNT_POINTS', '/persist', 'qimage-persist-ext4', '', d)}"
QIMGEXT4CLASSES += "${@bb.utils.contains('MNT_POINTS', '/systemrw', 'qimage-systemrw-ext4', '', d)}"
QIMGEXT4CLASSES += "${@bb.utils.contains('MNT_POINTS', '/lib/modules', 'qimage-vdlkm-ext4', '', d)}"
QIMGEXT4CLASSES += "${@bb.utils.contains('MNT_POINTS', '/nvram', 'qimage-nvram-ext4', '', d)}"

inherit ${QIMGEXT4CLASSES}

CORE_IMAGE_EXTRA_INSTALL += "${@bb.utils.contains('COMBINED_FEATURES', 'qti-ab-boot', ' recovery-ab', '', d)}"

CORE_IMAGE_EXTRA_INSTALL += "systemd-machine-units-ext4"

do_image_ext4[noexec] = "1"
# Default Image names
SYSTEMIMAGE_TARGET ?= "system.img"
SYSTEMIMAGE_UNSPARSE_TARGET ?= "system.img.unsparse"
SYSTEMIMAGE_MAP_TARGET ?= "system.map"
VDLKMIMAGE_TARGET ?= "vendor_dlkm.img"
VDLKMIMAGE_UNSPARSE_TARGET ?= "vendor_dlkm.img.unspase"
VDLKMIMAGE_MAP_TARGET ?= "vendor_dlkm.map"

# Ensure SELinux file context variable is defined
SELINUX_FILE_CONTEXTS ?= ""
SELINUX_IMG_S = "${@['-S ${SELINUX_FILE_CONTEXTS}', ''][d.getVar('SELINUX_FILE_CONTEXTS') == '']}"
IMAGE_EXT4_SELINUX_OPTIONS = "${@bb.utils.contains('DISTRO_FEATURES', 'selinux', '${SELINUX_IMG_S}', '', d)}"

ROOTFS_POSTPROCESS_COMMAND += "do_fsconfig;"
ROOTFS_POSTPROCESS_COMMAND += "${@bb.utils.contains('MNT_POINTS', 'overlay', 'gen_overlayfs;', '', d)}"

gen_overlayfs() {
    mkdir -p ${IMAGE_ROOTFS}/overlay
    mkdir -p ${IMAGE_ROOTFS}/overlay/etc
    mkdir -p ${IMAGE_ROOTFS}/overlay/.etc-work
    mkdir -p ${IMAGE_ROOTFS}/overlay/data
    mkdir -p ${IMAGE_ROOTFS}/overlay/.data-work
    mkdir -p ${IMAGE_ROOTFS}/overlay/cache
    mkdir -p ${IMAGE_ROOTFS}/overlay/.cache-work
}

do_fsconfig() {
 chmod go-r ${IMAGE_ROOTFS}/etc/passwd || :
 chmod -R o-rwx ${IMAGE_ROOTFS}/etc/init.d/ || :
}

do_fsconfig:append:qti-distro-user() {
 rm ${IMAGE_ROOTFS}/lib/systemd/system/sys-kernel-debug.mount
}

################################################
### Generate system.img #####
################################################
SPARSE_SYSTEMIMAGE_FLAG = "${@bb.utils.contains('IMAGE_FEATURES', 'vm', '', '-s', d)}"
IMAGE_ROOTFS_EXT4 = "${WORKDIR}/rootfs-ext4"

MACHINE_FSCONFIG_CONF_SEARCH_PATH ?= "${@':'.join('%s/conf/machine/fsconfig' % p for p in '${BBPATH}'.split(':'))}}"
MACHINE_FSCONFIG_CONF_FULL_PATH = "${@machine_search(d.getVar('MACHINE_FSCONFIG_CONF'), d.getVar('MACHINE_FSCONFIG_CONF_SEARCH_PATH')) or ''}"

create_symlink_systemd_ext4_mount_rootfs() {
    # Symlink ext4 mount files to systemd targets
    for entry in ${MACHINE_MNT_POINTS}; do
        mountname="${entry:1}"
        # Replace "/" with "-" for systemd to understand mount unit.
        mountname=${mountname//'/'/"-"}

	if { [[ "$mountname" == "firmware" || "$mountname" == "bt_firmware" || "$mountname" == "dsp" ]] && \
	     [[ "${COMBINED_FEATURES}" =~ .*qti-ab-boot.* ]]; } || \
	   { [[ "$mountname" == "dsp" || "$mountname" == "firmware" ]] && [[ "${MACHINE_FEATURES}" =~ mount-dsp-firmware ]]; }; then
            cp ${IMAGE_ROOTFS_EXT4}/lib/systemd/system/${mountname}-mount-ext4.service ${IMAGE_ROOTFS_EXT4}/lib/systemd/system/${mountname}-mount.service
            ln -sf ${systemd_unitdir}/system/${mountname}-mount.service ${IMAGE_ROOTFS_EXT4}/lib/systemd/system/local-fs.target.requires/${mountname}-mount.service
        else
            cp ${IMAGE_ROOTFS_EXT4}/lib/systemd/system/${mountname}-ext4.mount  ${IMAGE_ROOTFS_EXT4}/lib/systemd/system/${mountname}.mount
            if [[ "$mountname" == "$userfsdatadir" ]] ; then
                ln -sf ${systemd_unitdir}/system/${mountname}.mount ${IMAGE_ROOTFS_EXT4}/lib/systemd/system/local-fs.target.wants/${mountname}.mount
            elif [[ "$mountname" == "cache" ]] ; then
                ln -sf ${systemd_unitdir}/system/${mountname}.mount ${IMAGE_ROOTFS_EXT4}/lib/systemd/system/multi-user.target.wants/${mountname}.mount
            elif [[ "$mountname" == "persist" ]] ; then
                ln -sf ${systemd_unitdir}/system/${mountname}.mount ${IMAGE_ROOTFS_EXT4}/lib/systemd/system/local-fs.target.requires/${mountname}.mount
                if [[ "${MACHINE_FEATURES}" =~ var-persist ]] ; then
                    ln -sf ${systemd_unitdir}/system/var-persist.mount ${IMAGE_ROOTFS_EXT4}/lib/systemd/system/local-fs.target.wants/var-persist.mount
                fi
            elif [[ "$mountname" == "overlay" ]] ; then
                if ${@bb.utils.contains('DISTRO_FEATURES', 'full-disk-encryption', 'false', 'true', d)} ; then
                   ln -sf ${systemd_unitdir}/system/${mountname}.mount ${IMAGE_ROOTFS_EXT4}/lib/systemd/system/local-fs.target.requires/${mountname}.mount
                fi
            else
                ln -sf ${systemd_unitdir}/system/${mountname}.mount ${IMAGE_ROOTFS_EXT4}/lib/systemd/system/local-fs.target.requires/${mountname}.mount
            fi
        fi
    done
   # Remove generator binaries and ensure that we don't rely on generators for mount or service files.
   rm -rf ${IMAGE_ROOTFS_EXT4}/lib/systemd/system-generators/systemd-debug-generator
   rm -rf ${IMAGE_ROOTFS_EXT4}/lib/systemd/system-generators/systemd-fstab-generator
   rm -rf ${IMAGE_ROOTFS_EXT4}/lib/systemd/system-generators/systemd-gpt-auto-generator
   rm -rf ${IMAGE_ROOTFS_EXT4}/lib/systemd/system-generators/systemd-hibernate-resume-generator
   rm -rf ${IMAGE_ROOTFS_EXT4}/lib/systemd/system-generators/systemd-rc-local-generator
   rm -rf ${IMAGE_ROOTFS_EXT4}/lib/systemd/system-generators/systemd-system-update-generator
   rm -rf ${IMAGE_ROOTFS_EXT4}/lib/systemd/system-generators/systemd-sysv-generator
}

create_rootfs_ext4[cleandirs] = "${IMAGE_ROOTFS_EXT4}"
python create_rootfs_ext4 () {
    src_dir = d.getVar("IMAGE_ROOTFS")
    dest_dir = d.getVar("IMAGE_ROOTFS_EXT4")
    if os.path.isdir(src_dir):
        oe.path.copyhardlinktree(src_dir, dest_dir)
    else:
        bb.error("rootfs is not generated")
}

do_makesystem[prefuncs] += "create_rootfs_ext4"
do_makesystem[prefuncs] += "create_symlink_systemd_ext4_mount_rootfs"

# To successfully flash generated images on device, need to ensure combined size of
# unsparsed image and verity fec metadata is with in actual rootfs size. As there is
# no easy way to know the best fit values upfront, images and fec metadata are repeatedly
# generated by reducing size by 1% every time till a suitable image is avilable.
adjust_system_size_for_verity() {
    #set -x
    adjustedSize=$(echo ${SYSTEM_IMAGE_ROOTFS_SIZE} |egrep -o '^[0-9]+')
    percent=$(echo ${1} |egrep -o '^[0-9]+')
    adjustedSize=`expr $adjustedSize \* $percent`
    adjustedSize=`expr $adjustedSize / 100`

    #Align to 4096 block size
    adjustedSize=`expr $adjustedSize + 4095`
    adjustedSize=`expr $adjustedSize / 4096`
    adjustedSize=`expr $adjustedSize \* 4096`

    echo "$adjustedSize"
}

#$1 is path of system image.
get_system_verity_metdata_info(){
    VERITY_SALT="aee087a5be3b982978c923f566a94613496b417f2af592639bc80d141e34dfe7"
    FEC_ROOTS="2"

    # Remove files from previous run
    rm -f ${WORKDIR}/system.verityhash
    rm -f ${WORKDIR}/system.verityfec
    rm -f ${WORKDIR}/system_verity_metadata.txt

    veritysetup format $1 \
        ${WORKDIR}/system.verityhash \
        --fec-device ${WORKDIR}/system.verityfec \
        --fec-roots ${FEC_ROOTS} \
        --salt ${VERITY_SALT} > ${WORKDIR}/system_verity_metadata.txt
}

# Applicable only for (DISTRO_FEATURES->dm-verity and MACHINE_FEATURES->dm-verity-initramfs-v3)
# Where, Verity metadata and root signature are saved into the image itself, instead in ramfs
# $1 is path of system image.
# This function creates a 4096-byte block containing Verity metadata and a signature.
# The structure of the block is as follows:
# -------------------------------------
# |          root.env                 |
# |-----------------------------------|
# |          reserved bytes           |
# |-----------------------------------|
# |          root.sig                 |
# |-----------------------------------|
# |          reserved bytes           |
# -------------------------------------
# Total: 4096 bytes
#
# This block is then added to the last block of system.img.
# system.img:
# -------------------------------------
# |          Total Size               |
# |                                   |
# |                                   |
# |                                   |
# |                                   |
# |                                   |
# |                                   |
# |-----------------------------------|
# |  Last 4096 bytes                  |
# | (root.env + root.sig)             |
# -------------------------------------

add_verity_v3metadata() {
    local ImgPath=$1

    VERITY_SALT="aee087a5be3b982978c923f566a94613496b417f2af592639bc80d141e34dfe7"
    BLOCK_SIZE="4096"
    FEC_ROOTS="2"

    # Compute for system
            root_hash=`awk -F ':' '{ if ($1 == "Root hash") print $2 }' ${WORKDIR}/system_verity_metadata.txt | sed "s/^[ \t]*//"`
            data_blocks=`awk -F ':' '{ if ($1 == "Data blocks") print $2 }' ${WORKDIR}/system_verity_metadata.txt  |  sed "s/^[ \t]*//"`
            fec_offset=`awk -F ':' '{ if ($1 == "fec_offset") print $2 }' ${WORKDIR}/system_verity_metadata.txt  |  sed "s/^[ \t]*//"`
            hash_offset=`expr $data_blocks \* 4096`

    cat > ${WORKDIR}/root.env <<EOF
VERITY_DATA_BLOCKS=${data_blocks}
VERITY_HASH_OFFSET=${hash_offset}
VERITY_FEC_OFFSET=${fec_offset}
VERITY_FEC_ROOTS=${FEC_ROOTS}
VERITY_SALT=${VERITY_SALT}
VERITY_ROOT_HASH=${root_hash}
EOF

    # Sign the root hash
    echo -n "${root_hash}" > ${WORKDIR}/roothash.txt

    openssl smime -sign -nocerts -noattr -binary -in ${WORKDIR}/roothash.txt \
        -inkey ${STAGING_KERNEL_BUILDDIR}/certs/verity_key.pem -signer \
        ${STAGING_KERNEL_BUILDDIR}/certs/verity_cert.pem -outform der -out ${WORKDIR}/root.sig

    # Get the sizes of root.env and root.sig
    metadata_size=$(stat -c%s "${WORKDIR}/root.env")
    root_sig_size=$(stat -c%s "${WORKDIR}/root.sig")

    # Create a temporary file to hold the combined metadata and signature
    temp_file=$(mktemp)

    # Write root.env to the first part of the temporary file
    dd if=${WORKDIR}/root.env of=$temp_file bs=1 count=$metadata_size conv=notrunc

    # Calculate padding needed after root.env to align to 2048 bytes
    metadata_padding=`expr 2048 - $metadata_size`

    # Add padding after root.env
    dd if=/dev/zero bs=1 count=$metadata_padding >> $temp_file

    # Write root.sig to the next part of the temporary file
    dd if=${WORKDIR}/root.sig of=$temp_file bs=1 seek=2048 count=$root_sig_size conv=notrunc

    # Calculate the remaining padding to make the total size 4096 bytes
    remaining_padding=`expr 4096 - 2048 - $root_sig_size`

    # Add remaining padding to the temporary file
    dd if=/dev/zero bs=1 count=$remaining_padding >> $temp_file

    # Define the total size in blocks (assuming 4096 bytes per block)
    total_size_in_blocks=`expr ${SYSTEM_IMAGE_ROOTFS_SIZE} / 4096`

    # Calculate the offset for the last block
    offset=`expr $total_size_in_blocks - 1`

    # Append the temporary file to the image at the calculated offset
    dd if=$temp_file of=${ImgPath} bs=4096 seek=$offset conv=notrunc

    # Clean up the temporary file
    rm $temp_file
}

do_makesystem() {
    # Empty the folders that have seperate mount points
    # so that they doesn't end up in system image as well
    for entry in ${MNT_POINTS}; do
        mountname="${entry:1}"
        rm -rf ${IMAGE_ROOTFS_EXT4}/$mountname/*
        echo "Cleared... ${IMAGE_ROOTFS_EXT4}/$mountname/"
    done

    cp ${MACHINE_FSCONFIG_CONF_FULL_PATH} ${WORKDIR}/rootfs-fsconfig.conf
    invalid_image=0

    if ${@bb.utils.contains('MACHINE_FEATURES', 'dm-verity-none', 'false', 'true', d)} ; then
    # Generate unsparsed image, append hash and fec data to the image and then sparse the image
        for count in {99..1}
        do
            adjustedSystemSize=$(echo $(adjust_system_size_for_verity "${count}" ))
            echo adjustedSystemSize: $adjustedSystemSize
            ImgPath="${IMGDEPLOYDIR}/${IMAGE_BASENAME}/${SYSTEMIMAGE_UNSPARSE_TARGET}"
            make_ext4fs -C ${WORKDIR}/rootfs-fsconfig.conf \
                        -B ${IMGDEPLOYDIR}/${IMAGE_BASENAME}/${SYSTEMIMAGE_MAP_TARGET} \
                        -a / -b 4096 -l ${adjustedSystemSize} \
                        ${IMAGE_EXT4_SELINUX_OPTIONS} \
                        ${ImgPath} ${IMAGE_ROOTFS_EXT4} /dev/null || invalid_image=1

            if [ $invalid_image -eq 1 ]; then
                echo "Unsparse image generation failed...exiting."
                break
            fi

            # Get verity metadata for generated image.
            get_system_verity_metdata_info "${ImgPath}"

            # Append hash and fec data to the image
            cat ${WORKDIR}/system.verityhash >> ${ImgPath}
            cat ${WORKDIR}/system.verityfec >> ${ImgPath}

            # Check if size is within the range
            systemSize=`wc -c ${ImgPath} | awk '{print $1}'`
            if [ "$systemSize" -gt "${SYSTEM_IMAGE_ROOTFS_SIZE}" ]; then
                echo "Size mismatch ($systemSize Vs ${SYSTEM_IMAGE_ROOTFS_SIZE})...recreating unsparse image."
                continue
            fi

            # Calculate offset
            hash_offset=$adjustedSystemSize
            hash_size=`wc -c ${WORKDIR}/system.verityhash | awk '{print $1}'`
            fec_offset=`expr ${hash_offset} + ${hash_size}`
            echo "fec_offset:$fec_offset" >> ${WORKDIR}/system_verity_metadata.txt
            echo "Calculated fec offset: $fec_offset"

            if ${@bb.utils.contains('DISTRO_FEATURES', 'dm-verity', bb.utils.contains('MACHINE_FEATURES', 'dm-verity-initramfs-v3', 'true', 'false', d), 'false', d)}; then
                add_verity_v3metadata "${ImgPath}"
            fi

            # Convert to sparse image
            sparseImgPath="${IMGDEPLOYDIR}/${IMAGE_BASENAME}/${SYSTEMIMAGE_TARGET}"
            img2simg ${ImgPath} ${sparseImgPath}
            break
        done
    elif ${@bb.utils.contains('MACHINE_FEATURES', 'qti-avb', bb.utils.contains('MACHINE_FEATURES', 'dm-verity-none', 'true', 'false', d), 'false', d)} ; then

        adjustedSystemSize=$(avbtool.py add_hashtree_footer --partition_size ${SYSTEM_IMAGE_ROOTFS_SIZE}  --calc_max_image_size)
        echo adjustedSystemSize: $adjustedSystemSize
        ImgPath="${IMGDEPLOYDIR}/${IMAGE_BASENAME}/${SYSTEMIMAGE_UNSPARSE_TARGET}"
        make_ext4fs -C ${WORKDIR}/rootfs-fsconfig.conf \
                    -B ${IMGDEPLOYDIR}/${IMAGE_BASENAME}/${SYSTEMIMAGE_MAP_TARGET} \
                    -a / -b 4096 -l ${adjustedSystemSize} \
                    ${IMAGE_EXT4_SELINUX_OPTIONS} \
                    ${ImgPath} ${IMAGE_ROOTFS_EXT4} /dev/null || invalid_image=1

        if [ $invalid_image -eq 1 ]; then
            echo "Unsparse image generation failed...exiting."
        fi

        VERITY_SALT="aee087a5be3b982978c923f566a94613496b417f2af592639bc80d141e34dfe7"

        avbtool.py add_hashtree_footer \
        --image ${IMGDEPLOYDIR}/${IMAGE_BASENAME}/${SYSTEMIMAGE_UNSPARSE_TARGET} \
        --partition_size  ${SYSTEM_IMAGE_ROOTFS_SIZE} \
        --partition_name system \
        --hash_algorithm sha256 \
        --fec_num_roots 2 \
        --salt ${VERITY_SALT} \
        --key ${STAGING_DIR_NATIVE}${bindir}/SecImage/sigkeys/testkey_rsa4096.pem \
        --rollback_index 0


        # Convert to sparse image
        sparseImgPath="${IMGDEPLOYDIR}/${IMAGE_BASENAME}/${SYSTEMIMAGE_TARGET}"
        img2simg ${ImgPath} ${sparseImgPath}
    else
    # Directly generate sparsed image
            sparseImgPath="${IMGDEPLOYDIR}/${IMAGE_BASENAME}/${SYSTEMIMAGE_TARGET}"
            make_ext4fs -s -C ${WORKDIR}/rootfs-fsconfig.conf \
                        -B ${IMGDEPLOYDIR}/${IMAGE_BASENAME}/${SYSTEMIMAGE_MAP_TARGET} \
                        -a / -b 4096 -l ${SYSTEM_IMAGE_ROOTFS_SIZE} \
                        ${IMAGE_EXT4_SELINUX_OPTIONS} \
                        ${sparseImgPath} ${IMAGE_ROOTFS_EXT4} /dev/null || invalid_image=1
            if [ $invalid_image -eq 1 ]; then
                bbfatal "make_ext4fs failed to generate sparse system image"
            fi
    fi

        echo "image is good to use..."
}
addtask do_makesystem after do_image before do_image_complete

