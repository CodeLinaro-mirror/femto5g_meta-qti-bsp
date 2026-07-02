SUMMARY = "Display devicetree"
DESCRIPTION = "Build display devicetree to dtbo"
HOMEPAGE = "http://support.cdmatech.com"
LICENSE = "BSD-3-Clause & (GPL-2.0-only | BSD-2-Clause)"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6 \
                    file://${COREBASE}/meta/files/common-licenses/BSD-2-Clause;md5=cb641bc04cda31daea161b1bc15da69f \
                    file://${COREBASE}/meta/files/common-licenses/BSD-3-Clause;md5=550794465ba0ec5312d6919e203a55f9"
DEPENDS += "displaydlkm"

SRC_URI = "${PATH_TO_REPO}/vendor/qcom/opensource/display-devicetree/.git;protocol=${PROTO};destsuffix=vendor/qcom/opensource/display-devicetree;usehead=1"

SRCREV = "${AUTOREV}"

S = "${WORKDIR}/vendor/qcom/opensource/display-devicetree"

EXT_MODULE = "vendor/qcom/opensource/display-devicetree"

inherit qti-techpack

do_compile:prepend:gvm-gen5(){
    export GEN5_LVGVM=y
}
do_configure[noexec] = "1"

TECHPACK_MODULE_OUT = "${WORKDIR}/display-devicetree"

TECHPACK_DTBS:quin-gvm-gen4-5 = "\
                 display/quin-vm-display.dtbo \
"
TECHPACK_DTBS:gvm-gen4-5 = "\
                 display/quin-vm-display.dtbo \
"
TECHPACK_DTBS:gvm-gen5 = "\
                 display/nordy-sde-hwvirt-lvgvm.dtbo \
                 display/nordy-sde-hwvirt-quin-lvgvm.dtbo \
"
