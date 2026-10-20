SUMMARY = "Qualcomm soc-repo kernel modules"
DESCRIPTION = "Out-of-tree qualcomm kernel modules from soc-repo"
LICENSE = "GPLv2.0-with-linux-syscall-note & BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${SOC_REPO}/COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

inherit module

DEPENDS += "virtual/kernel virtual/dtc-native elfutils-native"
do_deploy[depends] += "virtual/kernel:do_deploy"

FILESEXTRAPATHS:prepend := "${KERNEL_PLATFORM_PATH}/:"

SRC_URI = "file://soc-repo \
           file://qcom/opensource/devicetree"

PROVIDES= "kernel-module-soc-repo"
S = "${WORKDIR}/soc-repo/build"
SOC_REPO = "${WORKDIR}/soc-repo"

KERNEL_BUILD_DIR = "${STAGING_KERNEL_BUILDDIR}"

KERNEL_CC = "${STAGING_BINDIR_NATIVE}/clang/bin/clang \
             -target ${TARGET_ARCH}${TARGET_VENDOR}-${TARGET_OS}"

EXTRA_OEMAKE += "KCONFIG_EXT_PREFIX=${SOC_REPO}/"
EXTRA_OEMAKE += ' CC="${KERNEL_CC}" LD="${KERNEL_LD}" OBJCOPY="${KERNEL_OBJCOPY}" STRIP="${KERNEL_STRIP}"'
EXTRA_OEMAKE += ' HOSTCC="${BUILD_CC}" HOSTCFLAGS="${BUILD_CFLAGS}" HOSTLDFLAGS="${BUILD_LDFLAGS}" HOSTCPP="${BUILD_CPP}"'
EXTRA_OEMAKE += ' HOSTCXX="${BUILD_CXX}" HOSTCXXFLAGS="${BUILD_CXXFLAGS}"'

TARGET_CONFIG_FRAGMENTS ?= ""

do_configure:append() {
    KCONFIG_EXT_PREFIX="${SOC_REPO}/" ${SOC_REPO}/flatten_kconfig.sh ${SOC_REPO}/Kconfig.msm > ${SOC_REPO}/Kconfig.ext

    for frag in ${TARGET_CONFIG_FRAGMENTS}; do
        cat "${SOC_REPO}/arch/arm64/configs/${frag}" >> "${KERNEL_BUILD_DIR}/.config"
    done

    oe_runmake -C ${KERNEL_BUILD_DIR} \
        KCONFIG_EXT_PREFIX=${SOC_REPO}/ \
        olddefconfig syncconfig
}

EXTRA_OEMAKE += "DTC="${STAGING_KERNEL_BUILDDIR}/scripts/dtc/dtc""
EXTRA_OEMAKE += 'DTC_INCLUDE="${SOC_REPO}/scripts/dtc/include-prefixes/ ${STAGING_KERNEL_DIR}/scripts/dtc/include-prefixes/"'

do_install() {
    mkdir -p ${STAGING_KERNEL_BUILDDIR}/lib/modules/${KERNEL_VERSION}

    for mod in $(find ${WORKDIR} -name '*.ko'); do
        if [ -f $mod ]; then
            install -m 0644 $mod \
                ${STAGING_KERNEL_BUILDDIR}/lib/modules/${KERNEL_VERSION}
        fi
    done

    rm -rf ${D}/lib/modules/${KERNEL_VERSION}/updates/

    # Expose soc-repo symbols for techpacks
    install -m 0755 ${B}/Module.symvers -D ${D}${includedir}/kernel-module-soc-repo/Module.symvers

    # Install soc-repo include headers for dependent recipes.
    install -d ${D}${includedir}/soc-repo/
    cp -rp ${SOC_REPO}/include ${D}${includedir}/soc-repo/
}

do_deploy() {
    install -d ${DEPLOY_DIR_IMAGE}/kernel_dtbs

    for dtbof in ${TARGET_DTBS}; do
        path=$(find ${WORKDIR} -name "$dtbof" -print -quit)
        if [ -n "$path" ]; then
            install -m 0644 "$path" "${DEPLOY_DIR_IMAGE}/kernel_dtbs"
        else
            bbfatal "DTB $dtbof not found in build output"
        fi
    done
}

addtask deploy after do_install

