inherit kernel-arch pkgconfig multilib_header

SUMMARY = "MSM Linux Kernel Headers"
DESCRIPTION = "Installs MSM kernel headers required to build userspace. \
These headers are installed in ${includedir}/linux-msm path."

LICENSE = "GPLv2.0-with-linux-syscall-note"
LIC_FILES_CHKSUM = "file://COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

PROVIDES = "linux-msm-headers"

COMPATIBLE_MACHINE = "echo" 

FILESEXTRAPATHS:prepend := "${WORKSPACE}/kernel-6.18/kernel_platform/:"

SRC_URI = "file://kernel"

S = "${WORKDIR}/kernel"

DEPENDS += "rsync-native"


do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install () {
    cd ${B}
    headerdir=${B}/headers
    kerneldir=${D}${includedir}/linux-msm
    install -d $kerneldir

    # Install all headers inside B and copy only required ones to D
    oe_runmake_call -C ${B} ARCH=${ARCH} headers_install O=$headerdir

    if [ -d $headerdir/include/generated ]; then
        mkdir -p $kerneldir/include/generated/
        cp -fR $headerdir/include/generated/* $kerneldir/include/generated/
    fi

    if [ -d $headerdir/arch/${ARCH}/include/generated ]; then
        mkdir -p $kerneldir/arch/${ARCH}/include/generated/
        cp -fR $headerdir/arch/${ARCH}/include/generated/* $kerneldir/arch/${ARCH}/include/generated/
    fi

    if [ -d $headerdir/${includedir} ]; then
        mkdir -p $kerneldir/${includedir}
        cp -fR $headerdir/${includedir}/* $kerneldir/${includedir}
    fi

    # Remove ..install.cmd and .install
    find $kerneldir/${includedir} -name ..install.cmd | xargs rm -f
    find $kerneldir/${includedir} -name .install | xargs rm -f
}

# kernel headers are generally machine specific
PACKAGE_ARCH = "${MACHINE_ARCH}"

# Allow to build empty main package, to include -dev package into the SDK
ALLOW_EMPTY:${PN} = "1"

FILES:${PN}-dev += "linux-msm/*"

INHIBIT_DEFAULT_DEPS = "1"
