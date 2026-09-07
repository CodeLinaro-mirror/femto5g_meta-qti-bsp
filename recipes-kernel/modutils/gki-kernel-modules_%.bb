inherit linux-kernel-base

SUMMARY = "Linux Kernel prebuilt modules"
DESCRIPTION = "Installs boot critical kernel modules into images. \
These modules are auto-loaded by systemd at boot"
LICENSE = "GPLv2.0-with-linux-syscall-note"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-qti-bsp/files/common-licenses/\
${LICENSE};md5=8afb6abdac9a14cb18a0d6c9c151e9b4"

KERNEL_SRC_TYPE ?= "msm-kernel"

# KERNEL_SRC_TYPE can be 'soc-repo' or 'msm-kernel' as per the kernel platform
FILESEXTRAPATHS:prepend := "${KERNEL_PREBUILT_PATH}:${KERNEL_PLATFORM_PATH}/${KERNEL_SRC_TYPE}:"
SRC_URI = "${@bb.utils.contains('MACHINE_USES_KERNEL_PREBUILTS', 'True', ' file://dist', '', d)}"
SRC_URI  +=  "file://${KERNEL_MODULES_LIST}"
SRC_URI  +=  "file://linkmodulesload.service"
SRC_URI:remove:qcs40x = "file://${KERNEL_MODULES_LIST}"

S  =  "${WORKDIR}/dist"

EXISTING_MODULES = "${@bb.utils.contains('MACHINE_USES_KERNEL_PREBUILTS', 'True', \
    '*.ko', \
    '${STAGING_KERNEL_BUILDDIR}/lib/modules/${KERNEL_VERSION}/*.ko', d)}"

do_compile[depends] += "${@bb.utils.contains('MACHINE_USES_KERNEL_PREBUILTS', 'True', '', \
    'linux-common-soc:do_install' if d.getVar('MACHINE') == 'pebble' else 'soc-modules:do_install', d)}"
do_compile () {
    existing_modules=$(ls ${EXISTING_MODULES} 2> /dev/null || true)

    if [ ! -f ${WORKDIR}/${KERNEL_MODULES_LIST} ]; then
        bbfatal "Kernel modules list file ${KERNEL_MODULES_LIST} not found"
    fi
    first_mod_list=$(cat ${WORKDIR}/${KERNEL_MODULES_LIST} | sed -e '/^ *#/d;/^ *$/d')

    # Check if any modules were found after filtering
    if [ -z "$first_mod_list" ]; then
        bbwarn "No modules found in ${KERNEL_MODULES_LIST} after filtering comments and empty lines"
    fi

    # Check if any modules exist
    if [ -z "$existing_modules" ]; then
        bbwarn "No kernel modules (*.ko) found in the working directory"
    fi

    # generate conf file for 1st/2nd stage module
    touch firstmods.conf
    touch secondmods.conf

    for module in ${existing_modules}; do
        # Use word boundaries to ensure exact module name matching
        echo ${first_mod_list} | grep -qw $(basename ${module} .ko) && is_1st_ko="True" || is_1st_ko="False"
        if [ "${is_1st_ko}" == "True" ]; then
            echo "$(basename ${module} .ko)" >> firstmods.conf
        else
            echo "$(basename ${module} .ko)" >> secondmods.conf
        fi
    done
}

KERNEL_VERSION = "${@get_kernelversion_file("${STAGING_KERNEL_BUILDDIR}")}"
do_install[depends] += "${@bb.utils.contains('MACHINE_USES_KERNEL_PREBUILTS', 'True', 'virtual/kernel:do_prebuilt_shared_workdir', '', d)}"
do_install() {
    # Install modules
    mkdir -p ${D}${base_libdir}/modules/${KERNEL_VERSION}
    if ls ${EXISTING_MODULES} 1> /dev/null 2>&1; then
        for mod in ${EXISTING_MODULES}; do
            install -m 0644 $mod ${D}${base_libdir}/modules/${KERNEL_VERSION}
        done
    else
        bbwarn "No kernel modules (*.ko) found to install"
    fi
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
    import os
    import bb

    pn = d.getVar('PN')

    f_conf = os.path.join(d.getVar('D'), 'etc/modules-load.d', '00-firstmods.conf')
    s_conf = os.path.join(d.getVar('D'), 'etc/modules-load.d', '00-secondmods.conf')

    f_mods = [ '/etc/modules-load.d/00-firstmods.conf', '${base_libdir}/modules/*/modules.load' ]
    if os.path.exists(f_conf):
        with open(f_conf) as f:
            lines = f.readlines()
            for line in lines:
                if line.startswith('#'):
                    continue
                f_mods += [ '${base_libdir}/modules/*/' + line.rstrip() + '.ko' ]
    else:
        bb.warn("First modules conf file not found at %s" % f_conf)
    d.setVar('FILES:' + pn + '-first-stage', " ".join(f_mods))

    s_mods = [ '/etc/modules-load.d/00-secondmods.conf' ]
    if os.path.exists(s_conf):
        with open(s_conf) as f:
            lines = f.readlines()
            for line in lines:
                if line.startswith('#'):
                    continue
                s_mods += [ '${base_libdir}/modules/*/' + line.rstrip() + '.ko' ]
    else:
        bb.warn("Second modules conf file not found at %s" % s_conf)
    d.setVar('FILES:' + pn + '-second-stage', " ".join(s_mods))
}

PACKAGE_PREPROCESS_FUNCS += "get_files_pn_from_conf "