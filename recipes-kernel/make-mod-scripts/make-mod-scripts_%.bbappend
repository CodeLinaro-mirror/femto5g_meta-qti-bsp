
DEPENDS += "elfutils-native"

KERNEL_USE_PREBUILTS = "${@d.getVar('MACHINE_USES_KERNEL_PREBUILTS') or "False"}"

do_configure[depends] += "${@oe.utils.conditional('KERNEL_USE_PREBUILTS', 'True', 'virtual/kernel:do_prebuilt_shared_workdir', '',d)}"

python() {
    if d.getVar('USE_CLANG', True) == "True":
        # Set KERNEL_CC to clang
        d.setVar('KERNEL_CC', '${STAGING_BINDIR_NATIVE}/clang/bin/clang -target ${TARGET_ARCH}${TARGET_VENDOR}-${TARGET_OS}')
        d.setVar('KERNEL_LD', '${STAGING_BINDIR_NATIVE}/clang/bin/ld.lld')
}

qti_fix_unused_ksyms_whitelist() {
    kernel_config="${STAGING_KERNEL_BUILDDIR}/.config"

    if [ ! -f "${kernel_config}" ]; then
        bbfatal "Kernel config not found: ${kernel_config}"
    fi

    current_whitelist="$(sed -n 's/^CONFIG_UNUSED_KSYMS_WHITELIST="\([^"]*\)"/\1/p' "${kernel_config}" | head -n1)"

    if [ -z "${current_whitelist}" ] || [ -f "${current_whitelist}" ]; then
        return 0
    fi

    fix_abi_symbollist="${KERNEL_PREBUILT_PATH}/gki_kernel/common/abi_symbollist.raw"

    sed -i "s#^CONFIG_UNUSED_KSYMS_WHITELIST=.*#CONFIG_UNUSED_KSYMS_WHITELIST=\"${fix_abi_symbollist}\"#" "${kernel_config}"

    rm -f "${STAGING_KERNEL_BUILDDIR}/include/config/auto.conf"
    rm -f "${STAGING_KERNEL_BUILDDIR}/include/generated/autoconf.h"
    rm -f "${STAGING_KERNEL_BUILDDIR}/include/generated/autoksyms.h"

}

do_configure:prepend() {
    qti_fix_unused_ksyms_whitelist
}
