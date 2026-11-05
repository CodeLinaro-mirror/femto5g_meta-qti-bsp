SUMMARY = "U-Boot boot script for QTI FIT (boot.itb) boot on echo"
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM += "file://${COREBASE}/meta-qti-bsp/files/common-licenses/\
BSD-3-Clause-Clear;md5=3771d4920bd6cdb8cbdf1e8344489ee0"

DEPENDS = "u-boot-mkimage-native"
SRC_URI = "file://boot.cmd.in"
INHIBIT_DEFAULT_DEPS = "1"

inherit kernel-arch deploy nopackages

PACKAGE_ARCH = "${MACHINE_ARCH}"
S = "${UNPACKDIR}"

# Must match qti-fitimage.bbclass's FITIMAGE_TARGET default - this recipe
# doesn't inherit that class, so the filename contract is kept in sync here.
FITIMAGE_TARGET ?= "boot.itb"

do_compile() {
	sed -e "s|@KERNEL_CMDLINE@|${KERNEL_CMD_PARAMS}|g" \
	    -e "s|@FITIMAGE_TARGET@|${FITIMAGE_TARGET}|g" \
	    boot.cmd.in > boot.cmd
	"${STAGING_BINDIR_NATIVE}/mkimage" -A ${UBOOT_ARCH} -T script -C none \
	    -n "Boot script" -d boot.cmd boot.scr
}
do_compile[depends] += "u-boot-mkimage-native:do_populate_sysroot"

do_install[noexec] = "1"

do_deploy() {
	install -d ${DEPLOYDIR}
	install -m 0644 boot.scr ${DEPLOYDIR}/boot.scr
}
addtask do_deploy after do_compile before do_build
