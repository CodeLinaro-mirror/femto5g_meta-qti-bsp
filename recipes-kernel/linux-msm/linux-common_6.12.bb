inherit kernel

DESCRIPTION = "Linux Kernel"
LICENSE = "GPLv2.0-with-linux-syscall-note"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

COMPATIBLE_MACHINE = "seraph"

FILESEXTRAPATHS:prepend := "${KERNEL_PLATFORM_PATH}/:"

SRC_URI = "file://common"

S = "${WORKDIR}/common"
B = "${WORKDIR}/out"

PR = "r0"

DEPENDS += "virtual/kernel-toolchain-native rsync-native elfutils-native"

KERNEL_CC = "${STAGING_BINDIR_NATIVE}/clang/bin/clang \
             -target ${TARGET_ARCH}${TARGET_VENDOR}-${TARGET_OS}"

KERNEL_CONFIG = "generic_le_defconfig"

KERNEL_CONFIG_FRAGMENTS:append = " ${@oe.utils.vartrue('DEBUG_BUILD', '${KERNEL_PLATFORM_PATH}/${KERNEL_SRC_TYPE}/arch/${ARCH}/configs/consolidate.fragment', '', d)}"

do_configure:prepend() {
    cp "${KERNEL_PLATFORM_PATH}/${KERNEL_SRC_TYPE}/arch/${ARCH}/configs/${KERNEL_CONFIG}" ${B}/.config \
        || bbfatal "Missing defconfig"

    for f in ${KERNEL_CONFIG_FRAGMENTS}; do
        [ -e "$f" ] || bbfatal "Missing fragment $f"
        cat "$f" >> ${B}/.config
    done
    cp ${B}/.config ${B}/.preconfig
}

do_configure:append() {
    oe_runmake -C ${S} O=${B} savedefconfig
}

do_install:append() {
    mkdir -p ${STAGING_KERNEL_BUILDDIR}/lib/modules/${KERNEL_VERSION}

    for mod in $(find . -name '*.ko'); do
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
    for kmod in $(find . -name "*.ko") ; do
        install -m 0644 $kmod ${DEPLOYDIR}/kernel_modules
    done
}
