# Limit packages need to be included as part of the default package.
# To add more extend bindir_progs, base_bindir_progs by referring
# original recipe. Accordingly update alternatives to avoid pkg warnings

CUSTOMIZE_COREUTILS_COMMANDS = "${@oe.utils.conditional('TOYBOX_RAMDISK', 'True', 'False', 'True', d)}"

python () {
    if d.getVar("CUSTOMIZE_COREUTILS_COMMANDS") == "True":
        d.setVar("bindir_progs", "chcon")
        d.setVar("base_bindir_progs", "cp")
}

ALTERNATIVE:${PN} = "${bindir_progs} ${base_bindir_progs} ${sbindir_progs}"
ALTERNATIVE:${PN}-doc = ""

PACKAGE_PREPROCESS_FUNCS += "${@oe.utils.conditional('CUSTOMIZE_COREUTILS_COMMANDS', 'True', 'remove_extra_progs', '', d)}"
remove_extra_progs() {
    cd ${PKGD}${bindir}

    if ${@bb.utils.contains('DISTRO_FEATURES','usrmerge','true','false',d)}; then
       find . -type f ! -name '${bindir_progs}.${BPN}' -a ! -name '${base_bindir_progs}.${BPN}'  -delete
    else
       find . -type f ! -name '${bindir_progs}.${BPN}' -delete

       cd ${PKGD}${base_bindir}
       find . -type f ! -name '${base_bindir_progs}.${BPN}' -delete
    fi
}

ALTERNATIVE:${PN}-doc += "base32.1"

ALTERNATIVE_LINK_NAME[base32] = "${base_bindir}/base32"
ALTERNATIVE_TARGET[base32] = "${bindir}/base32.${BPN}"
ALTERNATIVE_LINK_NAME[base32.1] = "${mandir}/man1/base32.1"

do_install:append() {
    # Rename only if the file (or symlink) exists
    if [ -e "${D}${bindir}/base32" ]; then
        mv ${D}${bindir}/base32 ${D}${bindir}/base32.${BPN}
    fi
}
