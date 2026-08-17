inherit kernel cml1

DESCRIPTION = "CAF Linux Kernel"
LICENSE = "GPLv2.0-with-linux-syscall-note"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

COMPATIBLE_MACHINE = "echo"

FILESEXTRAPATHS:prepend := "${WORKSPACE}/kernel-6.18/kernel_platform/:"

SRC_URI = "file://kernel"

S = "${WORKDIR}/kernel"
PR = "r0"

DEPENDS += "rsync-native"
DEPENDS:append:aarch64 = " libgcc"


#Add DTC_FLAGS to compile DTB with symbols.
KERNEL_DTC_FLAGS += "-@"

KERNEL_CONFIG_FRAGMENTS:append:echo = " \
    ${S}/arch/arm64/configs/vendor/echo-mbb.config \
    ${S}/arch/arm64/configs/vendor/echo-cpe.config \
    ${S}/arch/arm64/configs/vendor/echo-rdkb.config \
    ${S}/arch/arm64/configs/vendor/echo-cpe-plus.config \
    ${S}/arch/arm64/configs/vendor/echo-mbb-plus.config \
    ${S}/arch/arm64/configs/vendor/echo-rdkb-plus.config"
KERNEL_CONFIG_FRAGMENTS:append:echo = " ${@oe.utils.vartrue('DEBUG_BUILD', '${S}/arch/arm64/configs/vendor/echo-debug.config', '', d)}"

SRC_URI += " \
          ${@bb.utils.contains('DISTRO_FEATURES', 'apparmor', 'file://apparmor.cfg', '', d)} \
          "

do_configure:prepend() {
    if [ ! -f "${S}/arch/${ARCH}/configs/${KERNEL_CONFIG}" ]; then
        bbfatal "KERNEL_CONFIG '${KERNEL_CONFIG}' was specified, but not present in the source tree"
    else
        cp '${S}/arch/${ARCH}/configs/${KERNEL_CONFIG}' '${B}/.config'
    fi

    if [ ! -z "${KERNEL_CONFIG_FRAGMENTS}" ]
    then
        for f in ${KERNEL_CONFIG_FRAGMENTS}
        do
            # Check if the config fragment was copied into the WORKDIR from
            # the OE meta data
            if [ ! -e "$f" ]
            then
                echo "Could not find kernel config fragment $f"
                exit 1
            fi
        done

        # Now that all the fragments are located merge them.
        ( cd ${WORKDIR} && ${S}/scripts/kconfig/merge_config.sh -m -r -y -O ${B} ${B}/.config ${KERNEL_CONFIG_FRAGMENTS}  ${@" ".join(find_cfgs(d))} 1>&2 )
    fi

    # generate pair of private/public keys for module signing
    mkdir -p certs
    openssl req -new -nodes -utf8 -sha256 -days 36500 -batch -x509 \
        -config ${STAGING_KERNEL_DIR}/certs/default_x509.genkey -outform PEM -out ${B}/certs/signing_key.pem \
        -keyout ${B}/certs/signing_key.pem

    if ${@bb.utils.contains('MACHINE_FEATURES', 'dm-verity-none', 'false', 'true', d)} ; then
        # generate verity root hash signing keys
        openssl req -new -nodes -utf8 -newkey rsa:4096 -days 36500 -batch \
            -x509 -config ${STAGING_KERNEL_DIR}/certs/default_x509.genkey -outform PEM -out ${B}/certs/verity_cert.pem \
            -keyout ${B}/certs/verity_key.pem

        ${STAGING_KERNEL_DIR}/scripts/config --file ${B}/.config \
	    --set-str CONFIG_MODULE_SIG_KEY "${B}/certs/signing_key.pem" \
	    --set-str CONFIG_SYSTEM_TRUSTED_KEYS "${B}/certs/verity_cert.pem"
    fi
}

do_configure:append() {
    oe_runmake -C ${S} O=${B} savedefconfig && cp ${B}/defconfig ${WORKDIR}/defconfig.saved
}

# append DTB
# msm kernel trees have a special treatment for DTS, and both arm and
# arm64 DTS are located in arch/arm64/boot/dts/qcom folder, which
# confuses kernel-devicetree class, so we can't use it. Instead let's
# make sure that we generate all DTBs using the kernel 'dtbs' target,
# then we can append the DTBs that we need for $MACHINE.
KERNEL_EXTRA_ARGS += "dtbs"
KERNEL_EXTRA_ARGS += "DTC_EXT=${STAGING_DIR_NATIVE}/usr/bin/dtc/bin/dtc DTC_FLAGS+='${KERNEL_DTC_FLAGS}'"

do_install:append() {
    mkdir -p ${STAGING_KERNEL_BUILDDIR}/lib/modules/${KERNEL_VERSION}

    # Copy the modules to the staging directory
    for mod in $(find . -name '*.ko'); do
        if [ -f $mod ]; then
            install -m 0644 $mod ${STAGING_KERNEL_BUILDDIR}/lib/modules/${KERNEL_VERSION}
        fi
    done
    rm -rf ${D}/lib/modules/${KERNEL_VERSION}/kernel/

    if ${@bb.utils.contains('MACHINE_FEATURES', 'dm-verity-none', 'false', 'true', d)} ; then

        install -d ${STAGING_KERNEL_BUILDDIR}/kernel-certs
        install -m 0644 ${B}/certs/signing_key.pem ${STAGING_KERNEL_BUILDDIR}/kernel-certs/
        install -m 0644 ${B}/certs/verity_key.pem ${STAGING_KERNEL_BUILDDIR}/kernel-certs/
        install -m 0644 ${B}/certs/verity_cert.pem ${STAGING_KERNEL_BUILDDIR}/kernel-certs/
    fi
}

do_deploy() {
    if [ -f ${D}/${KERNEL_IMAGEDEST}/-${KERNEL_VERSION} ]; then
        mv ${D}/${KERNEL_IMAGEDEST}/-${KERNEL_VERSION} ${D}/${KERNEL_IMAGEDEST}/${KERNEL_IMAGETYPE}-${KERNEL_VERSION}
    fi

    # Copy vmlinux and zImage into deplydir for boot.img creation
    install -d ${DEPLOYDIR}
    install -m 0644 ${KERNEL_OUTPUT_DIR}/${KERNEL_IMAGETYPE} ${DEPLOYDIR}/${KERNEL_IMAGETYPE}
    install -m 0644 vmlinux ${DEPLOYDIR}
    install -m 0644 System.map ${DEPLOYDIR}

    install -d ${DEPLOYDIR}/kernel_dtbs
    for dtbs in ${KERNEL_DTB_NAMES}; do
        for dtbf in $(find . -name "$dtbs") ; do
            install -m 0644 "$dtbf" "${DEPLOYDIR}/kernel_dtbs"
        done
    done

    install -d ${DEPLOYDIR}/kernel_modules
    for kmod in $(find . -name "*.ko") ; do
        install -m 0644 $kmod ${DEPLOYDIR}/kernel_modules
    done

}
