inherit linux-kernel-base

SUMMARY = "Linux Kernel prebuilt modules"
DESCRIPTION = "Installs boot critical kernel modules into images. \
These modules are auto-loaded by systemd at boot"
LICENSE = "GPLv2.0-with-linux-syscall-note"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-qti-bsp/files/common-licenses/\
${LICENSE};md5=8afb6abdac9a14cb18a0d6c9c151e9b4"

DEPENDS = "virtual/kernel"

DEFAULT_PREFERENCE = "-1"

FILESEXTRAPATHS:prepend := "${KERNEL_PLATFORM_PATH}/kernel:"
#SRC_URI  +=  "file://${KERNEL_MODULES_LIST}"
SRC_URI  +=  "file://linkmodulesload.service"

S  =  "${WORKDIR}/dist"

#do_compile[depends] += "virtual/kernel:do_deploy"

do_compile () {
    existing_modules=$(ls ${STAGING_KERNEL_BUILDDIR}/lib/modules/${KERNEL_VERSION}/*.ko 2> /dev/null || true)
    #existing_modules=$(find ${STAGING_KERNEL_BUILDDIR}/lib/modules/${KERNEL_VERSION}/ -name *.ko)
    first_mod_list=$(cat ${WORKDIR}/${KERNEL_MODULES_LIST} | sed -e '/^ *#/d;/^ *$/d')

    # generate conf file for 1st/2nd stage module
    touch firstmods.conf
    touch secondmods.conf

    find ${STAGING_KERNEL_BUILDDIR} -name '*.ko' 2> /dev/null | while read -r module; do
        if echo "${first_mod_list}" | grep -q "$(basename ${module})"; then
            is_1st_ko="True"
        else
            is_1st_ko="False"
        fi

        if [ "${is_1st_ko}" == "True" ]; then
            echo "$(basename ${module} .ko)" >> firstmods.conf
        else
            echo "$(basename ${module} .ko)" >> secondmods.conf
        fi
    done
}

KERNEL_VERSION = "${@get_kernelversion_file("${STAGING_KERNEL_BUILDDIR}")}"
do_install() {
    # Install modules
    mkdir -p ${D}${base_libdir}/modules/${KERNEL_VERSION}
    for mod in $(find ${STAGING_KERNEL_BUILDDIR} -name *.ko); do
        if [ -f $mod ]; then
            install -m 0644 $mod ${D}${base_libdir}/modules/${KERNEL_VERSION}
        fi
    done
    # Create empty modules.load as a place holder to mimic Android GKI ramdisk
    touch ${D}${base_libdir}/modules/${KERNEL_VERSION}/modules.load

    # Install systemd configuration file for auto load
    install -d ${D}${sysconfdir}/modules-load.d/
    install -m 0644 firstmods.conf ${D}${sysconfdir}/modules-load.d/00-firstmods.conf
    install -m 0644 secondmods.conf ${D}${sysconfdir}/modules-load.d/00-secondmods.conf

    install -d ${D}${systemd_unitdir}/system/
    install -m 0644 ${WORKDIR}/linkmodulesload.service ${D}${systemd_unitdir}/system/linkmodulesload.service
}

ALLOW_EMPTY:${PN} = "1"

PACKAGE_ARCH = "${MACHINE_ARCH}"
PACKAGES = "${PN}-first-stage ${PN}-second-stage ${PN}-linkmodulesload ${PN}-dbg"
FILES:${PN}-linkmodulesload += "${systemd_unitdir}/system/linkmodulesload.service"
FILES:${PN}-dbg += "${base_libdir}/modules/${KERNEL_VERSION}/.debug"

inherit systemd

SYSTEMD_PACKAGES = "${PN}-linkmodulesload"
SYSTEMD_SERVICE:${PN}-linkmodulesload = "linkmodulesload.service"

python get_files_pn_from_conf() {
    pn = d.getVar('PN')

    f_conf = os.path.join(d.getVar('D'), 'etc/modules-load.d', '00-firstmods.conf')
    s_conf = os.path.join(d.getVar('D'), 'etc/modules-load.d', '00-secondmods.conf')

    f_mods = [ '/etc/modules-load.d/00-firstmods.conf', '${base_libdir}/modules/*/modules.load' ]
    with open(f_conf) as f:
        lines = f.readlines()
        for line in lines:
            if line.startswith('#'):
                continue
            f_mods += [ '${base_libdir}/modules/*/' + line.rstrip() + '.ko' ]
    d.setVar('FILES:' + pn + '-first-stage', " ".join(f_mods))

    s_mods = [ '/etc/modules-load.d/00-secondmods.conf' ]
    with open(s_conf) as f:
        lines = f.readlines()
        for line in lines:
            if line.startswith('#'):
                continue
            s_mods += [ '${base_libdir}/modules/*/' + line.rstrip() + '.ko' ]
    d.setVar('FILES:' + pn + '-second-stage', " ".join(s_mods))
}

PACKAGE_PREPROCESS_FUNCS += "get_files_pn_from_conf "
