inherit kernel

DESCRIPTION = "CAF Linux Kernel"
LICENSE = "GPLv2.0-with-linux-syscall-note"

COMPATIBLE_MACHINE = "trustedvm-v5|trustedvm-v4|vienna|alor|pebble"

FILESEXTRAPATHS:prepend := "${WORKSPACE}:"
FILESEXTRAPATHS:prepend := "${WORKSPACE}:${KERNEL_PREBUILT_PATH}:"

SRC_URI = "file://kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform/soc-repo \
           file://kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform/common \
           file://dist \
           "

S = "${WORKDIR}/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform/soc-repo"
S:vienna = "${WORKDIR}/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform/common"
S:alor = "${WORKDIR}/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform/common"
S:pebble = "${WORKDIR}/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform/common"
PR = "r0"
DEPENDS += "virtual/kernel-toolchain-native virtual/dtc-native ${@bb.utils.contains('DDK_BUILD', 'true','', 'rsync-native', d)} mod-signing-keys"

LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

KERNEL_USE_PREBUILTS = "${@d.getVar('MACHINE_USES_KERNEL_PREBUILTS') or "False"}"

# Don't set any version extention on debug build
LINUX_VERSION_EXTENSION ?= "-perf"
LINUX_VERSION_EXTENSION_qti-distro-debug = ""


do_prebuilt_configure[cleandirs] += "${B}"
do_prebuilt_configure() {
    cd ${KERNEL_PREBUILT_DISTDIR}

    install -d ${B}/include/config
    install -d ${B}/include/generated
    install -d ${B}/scripts
    install -d ${B}/certs
    # Some of the artifacts needed for module compilation are present under
    # soc-repo path, for now copy them for this path to avoid build failures.
    # Ask prebuilt providers to make these available in KERNEL_PREBUILT_DISTDIR.
    install -m 0644 ../${KERNEL_TYPE}/.config ${B}
    install -m 0644 ../${KERNEL_TYPE}/Module.symvers ${B}
    install -m 0644 ../${KERNEL_TYPE}/signing_key.pem ${B}/certs/signing_key.pem
    if ${@bb.utils.contains('DISTRO_FEATURES', 'dm-verity', bb.utils.contains('MACHINE_FEATURES', 'dm-verity-initramfs-v3', 'true', 'false', d), 'false', d)}; then
        install -m 0644 ../${KERNEL_TYPE}/verity_cert.pem ${B}/certs/verity_cert.pem
        install -m 0644 ../${KERNEL_TYPE}/verity_key.pem ${B}/certs/verity_key.pem
    fi
    if [ -f module.lds ]; then
    install -m 0644 module.lds ${B}/scripts/module.lds
    fi
    if [ -f utsrelease.h ]; then
    install -m 0644 utsrelease.h ${B}/include/generated
    fi
    # update paths of signature checking certificates to reflect current host
    sed -i -e '/CONFIG_MODULE_SIG_KEY[ =]/d' ${B}/.config
    echo "CONFIG_MODULE_SIG_KEY="\"${STAGING_DIR_TARGET}/kernel-certs/signing_key.pem\" >> ${B}/.config
    sed -i -e '/CONFIG_SYSTEM_TRUSTED_KEYS[ =]/d' ${B}/.config
    echo "CONFIG_SYSTEM_TRUSTED_KEYS="\"${STAGING_DIR_TARGET}/kernel-certs/verity_cert.pem\" >> ${B}/.config

    install -m 0644 vmlinux ${B}
    install -m 0644 System.map ${B}

    for dtbf in ${KERNEL_DTB_NAMES}; do
        install -m 0644 $dtbf ${B}
    done
    for dtbof in $(find . -name "*.dtbo") ; do
        install -m 0644 $dtbof ${B}
    done
}

do_prebuilt_shared_workdir[cleandirs] += " ${STAGING_KERNEL_BUILDDIR}"
#do_prebuilt_shared_workdir[nostamp] = "1"
do_prebuilt_shared_workdir() {
    cd ${B}

    kerneldir=${STAGING_KERNEL_BUILDDIR}
    install -d $kerneldir

    #
    # Store the kernel version in sysroots for module-base.bbclass
    #

    echo "${KERNEL_VERSION}" > $kerneldir/${KERNEL_PACKAGE_NAME}-abiversion

    # Copy files required for module builds
    install -m 0644 System.map $kerneldir/System.map-${KERNEL_VERSION}
    [ -e Module.symvers ] && install -m 0644 Module.symvers $kerneldir/
    mkdir -p $kerneldir/include/config
    mkdir -p $kerneldir/scripts
    mkdir -p $kerneldir/include/generated
    if [ -e "${B}/scripts/module.lds" ]; then
        install -m 0644 ${B}/scripts/module.lds ${STAGING_KERNEL_BUILDDIR}/scripts/module.lds
    fi

    if [ -e "${KERNEL_PREBUILT_PATH}/host/unifdef" ]; then
        cp -rp ${KERNEL_PREBUILT_PATH}/host/unifdef $kerneldir/scripts/
        cp -rp ${KERNEL_PLATFORM_PATH}/soc-repo/scripts/headers_install.sh $kerneldir/
        sed -i 's|scripts/unifdef|${STAGING_KERNEL_BUILDDIR}/scripts/unifdef|g' ${STAGING_KERNEL_BUILDDIR}/headers_install.sh
    fi
}

do_prebuilt_install[dirs] = "${B}"
do_prebuilt_install[cleandirs] += "${D}"
fakeroot do_prebuilt_install() {
    cd ${B}

    #
    # Install various kernel output (zImage, map file, config, module support files)
    # From prebuilt paths
    #
    install -d ${D}/${KERNEL_IMAGEDEST}

    install -d ${D}${sysconfdir}/modules-load.d
    install -d ${D}${sysconfdir}/modprobe.d

    # Copied files may cause host contamination due to invalid UID. Change ownership to root.
    find ${D} -name '*' -exec chown -h root:root {} \;
}

# Must be ran no earlier than after do_kernel_checkout or else Makefile won't be in ${S}/Makefile
PREBUILT_DISCARDED_TASKS += "\
    do_configure \
    do_compile \
    do_kernel_link_images \
    do_compile_kernelmodules \
    do_shared_workdir \
    do_install \
    split_kernel_module_packages \
"
python () {
    if d.getVar('KERNEL_USE_PREBUILTS') == 'True':
        for task in d.getVar('PREBUILT_DISCARDED_TASKS').split():
            d.setVarFlag(task, 'noexec', '1')
        bb.build.addtask('do_prebuilt_configure', 'do_configure', 'do_unpack', d)
        bb.build.addtask('do_prebuilt_install', 'do_install', 'do_compile', d)
        bb.build.addtask('do_prebuilt_shared_workdir', 'do_compile_kernelmodules', 'do_compile', d)
}



# Path for dtbo generation is kernel version dependent.
DTBO_SRC_PATH ?= "${STAGING_KERNEL_BUILDDIR}/arch/${ARCH}/boot/dts/qcom/"

do_deploy() {
    #if [ -f ${D}/${KERNEL_IMAGEDEST}/-${KERNEL_VERSION} ]; then
    #    mv ${D}/${KERNEL_IMAGEDEST}/-${KERNEL_VERSION} ${D}/${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE}-${KERNEL_VERSION}
    #fi

    # Copy vmlinux and zImage into deplydir for boot.img creation
    install -d ${DEPLOYDIR}
    install -m 0644 ${KERNEL_PREBUILT_DISTDIR}/Image ${DEPLOYDIR}/${KERNEL_IMAGETYPE}
    if [ -f ${KERNEL_PREBUILT_DISTDIR}/unstripped_modules.tar.gz ]; then
        install -m 0644 ${KERNEL_PREBUILT_DISTDIR}/unstripped_modules.tar.gz ${DEPLOYDIR}/unstripped_modules.tar.gz
        tar -xvf ${KERNEL_PREBUILT_DISTDIR}/unstripped_modules.tar.gz -C ${DEPLOYDIR}/
    fi
    install -m 0644 vmlinux ${DEPLOYDIR}
    install -m 0644 System.map ${DEPLOYDIR}

    install -d ${DEPLOYDIR}/kernel_dtbs
    cd ${KERNEL_PREBUILT_DISTDIR}/
    for dtbf in ${KERNEL_DTB_NAMES}; do
        install -m 0644 $dtbf ${DEPLOYDIR}/kernel_dtbs
    done

    # Check if KERNEL_DTBO_NAMES is empty or unset
    if [ -z "${KERNEL_DTBO_NAMES}" ]; then
        # If empty, fallback to installing ALL found .dtbo files
        for dtbof in $(find . -name "*.dtbo"); do
            install -m 0644 $dtbof ${DEPLOYDIR}/kernel_dtbs
        done
    else
        # If set, strictly match the names in the list
        for dtbof_name in ${KERNEL_DTBO_NAMES}; do
            # Find specific files by name (handling potential duplicates in subdirs)
            for dtbof_path in $(find . -name "$dtbof_name"); do
                install -m 0644 $dtbof_path ${DEPLOYDIR}/kernel_dtbs
            done
        done
    fi

    install -d ${DEPLOYDIR}/kernel_modules
    cd ${KERNEL_PREBUILT_DISTDIR}
    for kmod in $(find . -name "*.ko") ; do
        install -m 0644 $kmod ${DEPLOYDIR}/kernel_modules
    done

}

# Put the zImage in the kernel-dev pkg
INSANE_SKIP:${PN} += "installed-vs-shipped"
