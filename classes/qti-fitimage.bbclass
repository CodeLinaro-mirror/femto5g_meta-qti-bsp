# Generates a FIT image (boot.itb) containing the kernel, ramdisk and all
# DTBs.
#
# The images{} section (kernel/dtb/ramdisk nodes) is emitted by functions
# vendored verbatim from poky/meta/classes/kernel-fitimage.bbclass The one 
# exception is fitimage_emit_section_dtb, which gets a 4th "description" 
# argument added (kernel-fitimage.bbclass hardcodes "Flattened Device Tree 
# blob" for every DTB; by passing an arg, we can add friendly per-board 
# description instead).
#
# kernel-fitimage.bbclass itself is NOT inherited here: its task wiring
# (do_assemble_fitimage, kernel_do_deploy:append, do_kernel_generate_rsa_keys)
# and its DTB auto-discovery (KERNEL_DEVICETREE/EXTERNAL_KERNEL_DEVICETREE,
# resolved from the kernel recipe's own build dir) assume the DTB set is final
# before the kernel's own do_deploy runs. For some targets it isn't the case: 
# the techpack dtbo overlay merge (do_merge_techpack_dtbos in qimage.bbclass) 
# is an image-level task that runs after virtual/kernel:do_deploy, so the final 
# DTB set only exists once that has completed.
#
# kernel-uboot/uboot-sign are NOT inherited either, even though that's where
# these variable defaults normally come from: uboot-sign.bbclass pulls in
# uboot-config.bbclass, which unconditionally requires UBOOT_MACHINE or
# UBOOT_CONFIG to be set in the machine conf, or it raises bb.parse.SkipRecipe
# - dropping the *entire* recipe from the build. 
# Declare the same defaults directly instead - identical values

FIT_HASH_ALG ?= "sha256"
FIT_SIGN_ALG ?= "rsa2048"
FIT_PAD_ALG ?= "pkcs-1.5"
FIT_SIGN_INDIVIDUAL ?= "0"
UBOOT_SIGN_ENABLE ?= "0"
UBOOT_MKIMAGE_KERNEL_TYPE ?= "kernel"

FITIMAGE_TARGET ?= "boot.itb"
FITIMAGE_ITS = "${WORKDIR}/fit-${MACHINE}.its"
FIT_DESC ?= "FIT image for ${MACHINE} (kernel + ramdisk + all DTBs)"

#
# Emit the fitImage ITS header
# (vendored verbatim from poky/meta/classes/kernel-fitimage.bbclass)
#
fitimage_emit_fit_header() {
	cat << EOF >> $1
/dts-v1/;

/ {
        description = "${FIT_DESC}";
        #address-cells = <1>;
EOF
}

#
# Emit the fitImage section bits
# (vendored verbatim from poky/meta/classes/kernel-fitimage.bbclass)
#
fitimage_emit_section_maint() {
	case $2 in
	imagestart)
		cat << EOF >> $1

        images {
EOF
	;;
	confstart)
		cat << EOF >> $1

        configurations {
EOF
	;;
	sectend)
		cat << EOF >> $1
	};
EOF
	;;
	fitend)
		cat << EOF >> $1
};
EOF
	;;
	esac
}

#
# Emit the fitImage ITS kernel section
# (vendored from poky/meta/classes/kernel-fitimage.bbclass, with one addition:
# when UBOOT_MKIMAGE_KERNEL_TYPE is "kernel_noload", the load/entry properties
# are omitted - upstream always emits them regardless of type
#
# $1 ... .its filename
# $2 ... Image counter
# $3 ... Path to kernel image
# $4 ... Compression type
fitimage_emit_section_kernel() {

	kernel_csum="${FIT_HASH_ALG}"
	kernel_sign_algo="${FIT_SIGN_ALG}"
	kernel_sign_keyname="${UBOOT_SIGN_IMG_KEYNAME}"

	cat << EOF >> $1
                kernel-$2 {
                        description = "Linux kernel";
                        data = /incbin/("$3");
                        type = "${UBOOT_MKIMAGE_KERNEL_TYPE}";
                        arch = "${UBOOT_ARCH}";
                        os = "linux";
                        compression = "$4";
EOF

	if [ "${UBOOT_MKIMAGE_KERNEL_TYPE}" != "kernel_noload" ]; then
		ENTRYPOINT="${UBOOT_ENTRYPOINT}"
		if [ -n "${UBOOT_ENTRYSYMBOL}" ]; then
			ENTRYPOINT=`${HOST_PREFIX}nm vmlinux | \
				awk '$3=="${UBOOT_ENTRYSYMBOL}" {print "0x"$1;exit}'`
		fi
		cat << EOF >> $1
                        load = <${UBOOT_LOADADDRESS}>;
                        entry = <$ENTRYPOINT>;
EOF
	fi

	cat << EOF >> $1
                        hash-1 {
                                algo = "$kernel_csum";
                        };
                };
EOF

	if [ "${UBOOT_SIGN_ENABLE}" = "1" -a "${FIT_SIGN_INDIVIDUAL}" = "1" -a -n "$kernel_sign_keyname" ] ; then
		sed -i '$ d' $1
		cat << EOF >> $1
                        signature-1 {
                                algo = "$kernel_csum,$kernel_sign_algo";
                                key-name-hint = "$kernel_sign_keyname";
                        };
                };
EOF
	fi
}

#
# Emit the fitImage ITS DTB section
# (vendored from poky/meta/classes/kernel-fitimage.bbclass, with one addition:
# a 4th "description" argument, since upstream hardcodes a generic
# "Flattened Device Tree blob" description for every DTB)
#
# $1 ... .its filename
# $2 ... Image counter
# $3 ... Path to DTB image
# $4 ... Description (friendly per-board text)
fitimage_emit_section_dtb() {

	dtb_csum="${FIT_HASH_ALG}"
	dtb_sign_algo="${FIT_SIGN_ALG}"
	dtb_sign_keyname="${UBOOT_SIGN_IMG_KEYNAME}"
	dtb_description="${4:-Flattened Device Tree blob}"

	dtb_loadline=""
	dtb_ext=${DTB##*.}
	if [ "${dtb_ext}" = "dtbo" ]; then
		if [ -n "${UBOOT_DTBO_LOADADDRESS}" ]; then
			dtb_loadline="load = <${UBOOT_DTBO_LOADADDRESS}>;"
		fi
	elif [ -n "${UBOOT_DTB_LOADADDRESS}" ]; then
		dtb_loadline="load = <${UBOOT_DTB_LOADADDRESS}>;"
	fi
	cat << EOF >> $1
                fdt-$2 {
                        description = "$dtb_description";
                        data = /incbin/("$3");
                        type = "flat_dt";
                        arch = "${UBOOT_ARCH}";
                        compression = "none";
                        $dtb_loadline
                        hash-1 {
                                algo = "$dtb_csum";
                        };
                };
EOF

	if [ "${UBOOT_SIGN_ENABLE}" = "1" -a "${FIT_SIGN_INDIVIDUAL}" = "1" -a -n "$dtb_sign_keyname" ] ; then
		sed -i '$ d' $1
		cat << EOF >> $1
                        signature-1 {
                                algo = "$dtb_csum,$dtb_sign_algo";
                                key-name-hint = "$dtb_sign_keyname";
                        };
                };
EOF
	fi
}

#
# Emit the fitImage ITS ramdisk section
# (vendored verbatim from poky/meta/classes/kernel-fitimage.bbclass)
#
# $1 ... .its filename
# $2 ... Image counter
# $3 ... Path to ramdisk image
fitimage_emit_section_ramdisk() {

	ramdisk_csum="${FIT_HASH_ALG}"
	ramdisk_sign_algo="${FIT_SIGN_ALG}"
	ramdisk_sign_keyname="${UBOOT_SIGN_IMG_KEYNAME}"
	ramdisk_loadline=""
	ramdisk_entryline=""

	if [ -n "${UBOOT_RD_LOADADDRESS}" ]; then
		ramdisk_loadline="load = <${UBOOT_RD_LOADADDRESS}>;"
	fi
	if [ -n "${UBOOT_RD_ENTRYPOINT}" ]; then
		ramdisk_entryline="entry = <${UBOOT_RD_ENTRYPOINT}>;"
	fi

	cat << EOF >> $1
                ramdisk-$2 {
                        description = "${INITRAMFS_IMAGE}";
                        data = /incbin/("$3");
                        type = "ramdisk";
                        arch = "${UBOOT_ARCH}";
                        os = "linux";
                        compression = "none";
                        $ramdisk_loadline
                        $ramdisk_entryline
                        hash-1 {
                                algo = "$ramdisk_csum";
                        };
                };
EOF

	if [ "${UBOOT_SIGN_ENABLE}" = "1" -a "${FIT_SIGN_INDIVIDUAL}" = "1" -a -n "$ramdisk_sign_keyname" ] ; then
		sed -i '$ d' $1
		cat << EOF >> $1
                        signature-1 {
                                algo = "$ramdisk_csum,$ramdisk_sign_algo";
                                key-name-hint = "$ramdisk_sign_keyname";
                        };
                };
EOF
	fi
}

#
# Emit the fitImage ITS configuration section for one DTB, using a
# qcom,board-id-based description instead of upstream's generic
# "Linux kernel, FDT blob, ramdisk" style description, and no hash node
# (not needed here).
#
# $1 ... .its filename
# $2 ... node id (matches the fdt-$2/kernel-1/ramdisk-1 ids used above)
# $3 ... description (e.g. "qcom,board-id=0x1010022,0x401")
fitimage_emit_section_config_boardid() {
	its_file="$1"
	node_id="$2"
	description="$3"

	cat << EOF >> $its_file
                conf-$node_id {
                        description = "$description";
                        kernel = "kernel-1";
                        ramdisk = "ramdisk-1";
                        fdt = "fdt-$node_id";
                };
EOF
}

do_make_fitimage () {
	its="${FITIMAGE_ITS}"
	rm -f "$its"

	# When BUILD_WITH_TECHPACKS=="1", DEPLOY_DIR_IMAGE/DTOverlays holds the
	# merged (base + techpack overlay) DTB set populated by
	# do_merge_techpack_dtbos (qimage.bbclass) - the same set dtb.img is
	# built from. Otherwise that task never runs and DTOverlays never gets
	# created, so fall back to the base kernel DTBs directly, which are
	# unconditionally deployed by virtual/kernel:do_deploy regardless of
	# BUILD_WITH_TECHPACKS.
	if [ "${BUILD_WITH_TECHPACKS}" = "1" ]; then
		dtbodir="${DEPLOY_DIR_IMAGE}/DTOverlays"
	else
		dtbodir="${DEPLOY_DIR_IMAGE}/kernel_dtbs"
	fi
	kernel_path="${DEPLOY_DIR_IMAGE}/${KERNEL_IMAGETYPE}"
	ramdisk_path="${DEPLOY_DIR_IMAGE}/${INITRAMFS_IMAGE}-${MACHINE}.${INITRAMFS_FSTYPES}"
	fdtget_bin="${STAGING_BINDIR_NATIVE}/fdtget"
	mkimage_bin="${STAGING_BINDIR_NATIVE}/mkimage"

	if [ ! -e "$kernel_path" ]; then
		bbfatal "do_make_fitimage: kernel image not found at $kernel_path"
	fi
	if [ ! -e "$ramdisk_path" ]; then
		bbfatal "do_make_fitimage: ramdisk not found at $ramdisk_path"
	fi

	dtbs=$(find "$dtbodir" -maxdepth 1 -name '*.dtb' 2>/dev/null | sort)
	if [ -z "$dtbs" ]; then
		bbfatal "do_make_fitimage: no DTBs found under $dtbodir"
	fi

	# KERNEL_DTB_NAMES (e.g. "echo.dtb echo-*-emmc.dtb") is shared by every
	# MACHINE variant (echo, echo-min, echo-ab, ...) - its first token is the
	# canonical base DTB, independent of MACHINE.
	default_dtb_name=$(echo "${KERNEL_DTB_NAMES}" | awk '{print $1}')
	[ -z "$default_dtb_name" ] && default_dtb_name="${MACHINE}.dtb"

	fitimage_emit_fit_header "$its"
	fitimage_emit_section_maint "$its" imagestart

	fitimage_emit_section_kernel "$its" 1 "$kernel_path" none

	first_node=""
	default_node=""
	conf_lines=""

	for dtb_path in $dtbs; do
		dtb_basename=$(basename "$dtb_path")
		base_noext=${dtb_basename%.dtb}
		friendly_base=${base_noext%-emmc}
		node_name=$(echo "$base_noext" | sed 's/[^a-zA-Z0-9_-]/-/g')
		fdt_description=$(echo "$friendly_base" | tr '-' ' ' | tr '[:lower:]' '[:upper:]')
		fdt_description="$fdt_description devicetree"

		# fdtget -tx prints raw hex digits per cell with no "0x" prefix
		# (e.g. "10021 0") - prefix each cell with "0x" and join with commas.
		board_id=$("$fdtget_bin" -tx "$dtb_path" / qcom,board-id 2>/dev/null | \
			awk '{out=""; for (i=1;i<=NF;i++) out=out (i>1?",":"") "0x" $i; print out}')
		if [ -n "$board_id" ]; then
			conf_description="qcom,board-id=$board_id"
		else
			conf_description="$dtb_basename"
		fi

		# fitimage_emit_section_dtb reads the ambient $DTB (not an arg) to
		# tell dtb from dtbo - matches the contract set up by
		# kernel-fitimage.bbclass's own caller (fitimage_assemble).
		DTB="$dtb_basename"
		fitimage_emit_section_dtb "$its" "$node_name" "$dtb_path" "$fdt_description"

		[ -z "$first_node" ] && first_node="$node_name"
		[ "$dtb_basename" = "$default_dtb_name" ] && default_node="$node_name"

		conf_lines="$conf_lines
$node_name|$conf_description"
	done

	[ -z "$default_node" ] && default_node="$first_node"

	fitimage_emit_section_ramdisk "$its" 1 "$ramdisk_path"

	fitimage_emit_section_maint "$its" sectend
	fitimage_emit_section_maint "$its" confstart

	# "default" must be emitted before any conf-* subnode - dtc requires
	# properties to precede subnodes within a node. Writing it inline per-DTB
	# (interleaved with the subnode loop below) would violate that whenever
	# the default DTB isn't first in sort order (e.g. "-" sorts before "."
	# in ASCII, so "echo-*-emmc.dtb" sorts before "echo.dtb").
	cat << EOF >> "$its"
                default = "conf-$default_node";
EOF

	echo "$conf_lines" | while IFS='|' read -r node_name conf_description; do
		[ -z "$node_name" ] && continue
		fitimage_emit_section_config_boardid "$its" "$node_name" "$conf_description"
	done

	fitimage_emit_section_maint "$its" sectend
	fitimage_emit_section_maint "$its" fitend

	bbdebug 1 "do_make_fitimage: assembling ${DEPLOY_DIR_IMAGE}/${IMAGE_BASENAME}/${FITIMAGE_TARGET} from $its"
	"$mkimage_bin" -f "$its" "${DEPLOY_DIR_IMAGE}/${IMAGE_BASENAME}/${FITIMAGE_TARGET}"

	# Keep the generated .its alongside boot.itb for inspection/debugging -
	# it otherwise only lives in WORKDIR and is lost on rm_work/do_clean.
	install -m 0644 "$its" "${DEPLOY_DIR_IMAGE}/${IMAGE_BASENAME}/${FITIMAGE_TARGET}.its"
}
do_make_fitimage[nostamp] = "1"
do_make_fitimage[dirs] = "${DEPLOY_DIR_IMAGE}/${IMAGE_BASENAME}"
do_make_fitimage[depends] += " u-boot-mkimage-native:do_populate_sysroot dtc-native:do_populate_sysroot virtual/kernel:do_deploy"

# Order after do_merge_techpack_dtbos when it exists (BUILD_WITH_TECHPACKS=="1"
# - DTOverlays is the DTB source in that case); otherwise after do_rootfs,
# since do_make_fitimage falls back to reading DEPLOY_DIR_IMAGE/kernel_dtbs
# directly (populated unconditionally by virtual/kernel:do_deploy).
python () {
    if (d.getVar("BUILD_WITH_TECHPACKS") or "0") == "1":
        bb.build.addtask('do_make_fitimage', 'do_image', 'do_merge_techpack_dtbos', d)
    else:
        bb.build.addtask('do_make_fitimage', 'do_image', 'do_rootfs', d)
}
