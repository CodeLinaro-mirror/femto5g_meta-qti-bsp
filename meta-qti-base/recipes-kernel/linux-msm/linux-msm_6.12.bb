SUMMARY = "CLO Linux Kernel"
HOMEPAGE = "https://git.codelinaro.org"
LICENSE = "GPLv2.0-with-linux-syscall-note"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

inherit kernel

COMPATIBLE_MACHINE = "quin-gvm-lemans|quin-gvm-monaco|gvm-gen4-5|quin-gvm-gen4-5|gvm-gen5"

FILESPATH =+ "${SRC_DIR_ROOT}/kernel:"

DEPENDS += "python3-native bison-native"
DEPENDS += "elfutils-native kern-tools-native mkbootimg-native mkdtimg-native openssl-native pahole-native rsync-native signing-keys"

SRC_URI = "file://kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform/common"

S = "${WORKDIR}/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform/common"

KERNEL_BUILD_VARIANT = "${@bb.utils.contains_any('VARIANT', 'perf user', '', 'debug-', d)}"
KERNEL_OUT_VARIANT = "${@bb.utils.contains_any('VARIANT', 'perf user', '', 'debug_', d)}"
BZ_PREBUILT_ROOT = "${SRC_DIR_ROOT}/kernel/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform"

do_compile[noexec] = "1"
do_kernel_link_images[noexec] = "1"
do_compile_kernelmodules[noexec] = "1"

do_configure () {
    # Try sandbox_stash path first (full Bazel build with sandbox enabled).
    # Fall back to execroot path when sandbox_stash is absent (e.g. mainline
    # builds where sandbox is disabled or stash was cleaned).
    MODULES_PREPARE_SRC="${BZ_PREBUILT_ROOT}/bazel-cache/bazel-cache/*/sandbox/sandbox_stash/ModulesPrepare/*/execroot/_main/out/*/common/."
    KBUILD_MIXED_TREE_SRC="${BZ_PREBUILT_ROOT}/bazel-cache/bazel-cache/*/sandbox/sandbox_stash/KernelBuild/*/execroot/_main/bazel-out/k8-fastbuild/bin/soc-repo/autogvm_${KERNEL_BUILD_VARIANT}defconfig_dtb_build_kbuild_mixed_tree/*"

    if ls ${MODULES_PREPARE_SRC} > /dev/null 2>&1; then
        cp -fpPR ${MODULES_PREPARE_SRC} ${B}
        cp ${KBUILD_MIXED_TREE_SRC} ${B}
    else
        # sandbox_stash not available: use execroot directly.
        # autogvm_*defconfig_dtb_build/ has: .config, include/config/kernel.release
        # autogvm_*defconfig_dtb_build_kbuild_mixed_tree/ has: gen_init_cpio, scripts/
        # Copy both; kbuild_mixed_tree is copied last so its files take precedence.
        EXECROOT="${BZ_PREBUILT_ROOT}/bazel-cache/bazel-cache/*/execroot/_main/bazel-out/k8-fastbuild/bin/soc-repo"
        cp -fpPR ${EXECROOT}/autogvm_${KERNEL_BUILD_VARIANT}defconfig_dtb_build/. ${B}
        cp -fpPR ${EXECROOT}/autogvm_${KERNEL_BUILD_VARIANT}defconfig_dtb_build_kbuild_mixed_tree/. ${B}
    fi

    install -d ${B}/arch/${ARCH}/boot/
    mv ${B}/Image ${B}/arch/${ARCH}/boot/
    mv ${B}/gen_init_cpio ${B}/usr
    ln -sf ${B}/vmlinux ${B}/arch/${ARCH}/boot/vmlinux
}

do_shared_workdir[cleandirs] += "${STAGING_KERNEL_BUILDDIR}"
do_shared_workdir () {
    cd ${B}
    kerneldir=${STAGING_KERNEL_BUILDDIR}
    install -d $kerneldir

    echo "${KERNEL_VERSION}" > $kerneldir/${KERNEL_PACKAGE_NAME}-abiversion

    install -m 0644 System.map $kerneldir/System.map-${KERNEL_VERSION}
    [ -e Module.symvers ] && install -m 0644 Module.symvers $kerneldir/
    install -m 0644 .config $kerneldir/
    mkdir -p $kerneldir/include/config
    mkdir -p $kerneldir/scripts
    install -m 0644 include/config/kernel.release $kerneldir/include/config/kernel.release
    if [ -e "${B}/scripts/module.lds" ]; then
        install -m 0644 ${B}/scripts/module.lds ${STAGING_KERNEL_BUILDDIR}/scripts/module.lds
    fi

    fdtoverlaymerge_bin=$(find ${BZ_PREBUILT_ROOT}/out -path "*/host/bin/fdtoverlaymerge" -type f | head -n 1)
    if [ -n "${fdtoverlaymerge_bin}" ]; then
        install -d $kerneldir/bin
        install -m 0755 ${fdtoverlaymerge_bin} ${kerneldir}/bin/
    else
        bbwarn "fdtoverlaymerge not found under ${BZ_PREBUILT_ROOT}/out"
    fi
}

do_install() {
    install -d ${D}/${KERNEL_IMAGEDEST}
    install -d ${D}/boot
    for imageType in ${KERNEL_IMAGETYPES} ; do
        install -m 0644 ${KERNEL_OUTPUT_DIR}/${imageType} ${D}/${KERNEL_IMAGEDEST}/${imageType}-${KERNEL_VERSION}
        if [ "${KERNEL_PACKAGE_NAME}" = "kernel" ]; then
            ln -sf ${imageType}-${KERNEL_VERSION} ${D}/${KERNEL_IMAGEDEST}/${imageType}
        fi
    done
    install -m 0644 ${B}/System.map ${D}/boot/System.map-${KERNEL_VERSION}
    install -m 0644 ${B}/.config ${D}/boot/config-${KERNEL_VERSION}
    install -m 0644 ${B}/vmlinux ${D}/boot/vmlinux-${KERNEL_VERSION}
    [ -e Module.symvers ] && install -m 0644 Module.symvers ${D}/boot/Module.symvers-${KERNEL_VERSION}
    install -d ${D}${sysconfdir}/modules-load.d
    install -d ${D}${sysconfdir}/modprobe.d

    install -d ${D}/${nonarch_base_libdir}/modules/${KERNEL_VERSION}
    install ${BZ_PREBUILT_ROOT}/out/msm-kernel-autogvm-${KERNEL_OUT_VARIANT}defconfig/dist/*.ko ${D}/${nonarch_base_libdir}/modules/${KERNEL_VERSION}

    install -m 0644 ${B}/modules.builtin ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}
    install -m 0644 ${B}/modules.builtin.modinfo ${D}${nonarch_base_libdir}/modules/${KERNEL_VERSION}

    find ${D} -name '*' -exec chown -h root:root {} \;
}

do_deploy () {
    if [ -f ${D}/${KERNEL_IMAGEDEST}/-${KERNEL_VERSION} ]; then
        mv ${D}/${KERNEL_IMAGEDEST}/-${KERNEL_VERSION} ${D}/${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE}-${KERNEL_VERSION}
    fi

    install -d ${DEPLOYDIR}
    install -m 0644 ${KERNEL_OUTPUT_DIR}/${KERNEL_IMAGETYPE} ${DEPLOYDIR}/${KERNEL_IMAGETYPE}
    install -m 0644 vmlinux ${DEPLOYDIR}
    install -m 0644 System.map ${DEPLOYDIR}

    install -d ${DEPLOYDIR}/build-artifacts/dtb/
    install -m 0644 ${BZ_PREBUILT_ROOT}/out/msm-kernel-autogvm-${KERNEL_OUT_VARIANT}defconfig/dist/*.dtb ${DEPLOYDIR}/build-artifacts/dtb/

    if ${@bb.utils.contains('MACHINE_FEATURES', 'dt-overlay', 'true', 'false', d)}; then
        install -d ${DEPLOYDIR}/build-artifacts/dtbo/
        install -m 0644 ${BZ_PREBUILT_ROOT}/out/msm-kernel-autogvm-${KERNEL_OUT_VARIANT}defconfig/dist/*.dtbo ${DEPLOYDIR}/build-artifacts/dtbo/
    fi
}

PACKAGES:remove = "${KERNEL_PACKAGE_NAME}-devicetree"

FILES:${KERNEL_PACKAGE_NAME}-dev += "/${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE}-${KERNEL_VERSION}"
