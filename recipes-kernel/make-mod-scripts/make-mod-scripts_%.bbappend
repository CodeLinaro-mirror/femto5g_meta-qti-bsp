
DEPENDS += "elfutils-native"

KERNEL_USE_PREBUILTS = "${@d.getVar('MACHINE_USES_KERNEL_PREBUILTS') or "False"}"

do_configure[depends] += "${@oe.utils.conditional('KERNEL_USE_PREBUILTS', 'True', 'virtual/kernel:do_prebuilt_shared_workdir', '',d)}"

python() {
    if d.getVar('USE_CLANG', True) == "True":
        # Set KERNEL_CC to clang
        d.setVar('KERNEL_CC', '${STAGING_BINDIR_NATIVE}/clang/bin/clang -target ${TARGET_ARCH}${TARGET_VENDOR}-${TARGET_OS}')
        d.setVar('KERNEL_LD', '${STAGING_BINDIR_NATIVE}/clang/bin/ld.lld')
}
