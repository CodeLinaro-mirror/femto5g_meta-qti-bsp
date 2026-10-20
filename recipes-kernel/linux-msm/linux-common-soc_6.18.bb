inherit kernel

DESCRIPTION = "Linux Kernel"
LICENSE = "GPLv2.0-with-linux-syscall-note & BSD-3-Clause"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

COMPATIBLE_MACHINE = "pebble"

FILESEXTRAPATHS:prepend := "${KERNEL_PLATFORM_PATH}/:"

# soc-repo is fetched alongside common so KBUILD_EXT_PREFIX can point to it
SRC_URI = "file://common \
           file://soc-repo \
           file://qcom/opensource/devicetree \
           file://0001-kernel-common-soc-built-in.patch;patchdir=${WORKDIR}/common"

S = "${WORKDIR}/common"
B = "${WORKDIR}/out"
SOC_REPO = "${WORKDIR}/soc-repo"

PR = "r0"

DEPENDS += "virtual/kernel-toolchain-native rsync-native elfutils-native"

KERNEL_CC = "${STAGING_BINDIR_NATIVE}/clang/bin/clang \
             -target ${TARGET_ARCH}${TARGET_VENDOR}-${TARGET_OS}"

KERNEL_CONFIG = "generic_le_defconfig"

# Pass soc-repo path so common/Kbuild descends into it as in-tree.
# The symlink is created in do_configure so it is present during do_compile.
EXTRA_OEMAKE += "KBUILD_EXT_PREFIX=soc-repo-ext/"

# Kconfig system needs to source soc-repo symbols during olddefconfig/syncconfig.
# Points to staged tree (not raw soc-repo/) since Kconfig.ext is generated there.
EXTRA_OEMAKE += "KCONFIG_EXT_PREFIX=${WORKDIR}/soc-repo-staging/"

TARGET_CONFIG_FRAGMENTS ?= ""

KERNEL_CONFIG_FRAGMENTS:append = " ${@oe.utils.vartrue('DEBUG_BUILD', '${KERNEL_PLATFORM_PATH}/${KERNEL_SRC_TYPE}/arch/${ARCH}/configs/consolidate.fragment', '', d)}"

do_configure:prepend() {
    # Stage common kernel sources into soc-repo-staging/ and overlay soc-repo on top.
    # Mirrors what soc-modules does via STAGE_COMMON_SOURCES in its all target.
    oe_runmake -C ${SOC_REPO}/build \
        KERNEL_SRC=${STAGING_KERNEL_DIR} \
        KCONFIG_EXT_PREFIX=${SOC_REPO}/ \
        stage

    # Symlink the staged tree into $(srctree) so KBUILD_EXT_PREFIX=soc-repo-ext/
    # resolves correctly during do_compile ($(srctree) = ${STAGING_KERNEL_DIR}).
    ln -sfn ${WORKDIR}/soc-repo-staging ${STAGING_KERNEL_DIR}/soc-repo-ext


    KCONFIG_EXT_PREFIX="${WORKDIR}/soc-repo-staging/" \
        ${WORKDIR}/soc-repo-staging/flatten_kconfig.sh \
        ${WORKDIR}/soc-repo-staging/Kconfig.msm \
        > ${WORKDIR}/soc-repo-staging/Kconfig.ext

    cp "${KERNEL_PLATFORM_PATH}/${KERNEL_SRC_TYPE}/arch/${ARCH}/configs/${KERNEL_CONFIG}" ${B}/.config \
        || bbfatal "Missing defconfig"

    for f in ${KERNEL_CONFIG_FRAGMENTS}; do
        [ -e "$f" ] || bbfatal "Missing fragment $f"
        cat "$f" >> ${B}/.config
    done

    # Append soc/target-specific fragments
    for frag in ${TARGET_CONFIG_FRAGMENTS}; do
        cat "${WORKDIR}/soc-repo-staging/arch/${ARCH}/configs/${frag}" >> ${B}/.config
    done

    cp ${B}/.config ${B}/.preconfig
}

EXTRA_OEMAKE += "DTC="${B}/scripts/dtc/dtc""
EXTRA_OEMAKE += 'DTC_INCLUDE="${SOC_REPO}/scripts/dtc/include-prefixes/ ${STAGING_KERNEL_DIR}/scripts/dtc/include-prefixes/"'
EXTRA_OEMAKE += 'DTC_FLAGS+="-@"'

do_compile_dtb() {
    oe_runmake -C ${STAGING_KERNEL_DIR} O=${B} V=1 dtbs \
        dtstree=soc-repo-ext/arch/arm64/boot/dts/vendor
}
addtask compile_dtb after do_compile_kernelmodules before do_deploy

do_configure:append() {
    oe_runmake -C ${S} O=${B} savedefconfig
}

do_install:append() {
    mkdir -p ${STAGING_KERNEL_BUILDDIR}/lib/modules/${KERNEL_VERSION}
    for mod in $(find ${B}/soc-repo-ext -name '*.ko'); do
        if [ -f $mod ]; then
            install -m 0644 $mod \
                ${STAGING_KERNEL_BUILDDIR}/lib/modules/${KERNEL_VERSION}
        fi
    done

    rm -rf ${D}/lib/modules/${KERNEL_VERSION}/kernel/
}

do_deploy() {
    if [ -f ${D}/${KERNEL_IMAGEDEST}/-${KERNEL_VERSION} ]; then
        mv ${D}/${KERNEL_IMAGEDEST}/-${KERNEL_VERSION} \
           ${D}/${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE}-${KERNEL_VERSION}
    fi

    install -d ${DEPLOYDIR}
    install -m 0644 ${KERNEL_OUTPUT_DIR}/${KERNEL_IMAGETYPE} ${DEPLOYDIR}/${KERNEL_IMAGETYPE}
    install -m 0644 vmlinux ${DEPLOYDIR}
    install -m 0644 System.map ${DEPLOYDIR}

    install -d ${DEPLOYDIR}/kernel_modules
    for kmod in $(find ${B}/soc-repo-ext -name "*.ko") ; do
        install -m 0644 $kmod ${DEPLOYDIR}/kernel_modules
    done

    install -d ${DEPLOYDIR}/kernel_dtbs
    for dtbof in ${TARGET_DTBS}; do
        path=$(find -L ${WORKDIR} -name "$dtbof" -print -quit)
        if [ -n "$path" ]; then
            install -m 0644 "$path" "${DEPLOYDIR}/kernel_dtbs"
        else
            bbfatal "DTB $dtbof not found in build output"
        fi
    done
}
