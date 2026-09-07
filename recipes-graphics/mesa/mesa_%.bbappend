FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

LIC_FILES_CHKSUM = "file://docs/license.rst;md5=63779ec98d78d823a9dc533a0735ef10"

PV = "24.0.7"

SRC_URI = "https://mesa.freedesktop.org/archive/mesa-${PV}.tar.xz \
           file://0001-meson.build-check-for-all-linux-host_os-combinations.patch \
           file://0001-meson-misdetects-64bit-atomics-on-mips-clang.patch \
           file://0001-gbm-make-mesa-gbm-compatable-to-qti-gbm.patch \
           file://0001-gallium-Fix-jit-compilation-issue.patch \
          "

SRC_URI[sha256sum] = "7454425f1ed4a6f1b5b107e1672b30c88b22ea0efea000ae2c7d96db93f6c26a"

PACKAGE_ARCH = "${TUNE_PKGARCH}"

PROVIDES = " \
            virtual/mesa \
           "

ANY_OF_DISTRO_FEATURES = "opengl vulkan"

PACKAGECONFIG += " \
                  gallium-llvm \
                  vulkan \
                 "
PACKAGECONFIG:remove = "zink"
PACKAGECONFIG:append:class-native = "gallium-llvm"

GALLIUMDRIVERS_LLVM = ",swrast"

PACKAGECONFIG[vulkan] = "-Dvulkan-drivers=${@strip_comma('${VULKAN_DRIVERS}')}, -Dvulkan-drivers='',glslang-native vulkan-loader vulkan-headers"

OPENCL_NATIVE = "${@bb.utils.contains('PACKAGECONFIG', 'freedreno', '-Dopencl-native=true', '', d)}"
PACKAGECONFIG[opencl] = "-Dgallium-opencl=icd -Dopencl-spirv=true ${OPENCL_NATIVE},-Dgallium-opencl=disabled -Dopencl-spirv=false,libclc spirv-tools"

PACKAGECONFIG[vulkan-beta] = "-Dvulkan-beta=true,-Dvulkan-beta=false"

VIDEO_CODECS = "vc1dec,h264dec,h264enc,h265dec,h265enc"
PACKAGECONFIG[video-codecs] = "-Dvideo-codecs=${@strip_comma('${VIDEO_CODECS}')}, -Dvideo-codecs=''"

PACKAGECONFIG[elf-tls] = ""
PACKAGECONFIG[xvmc] = ""

DEV_PKG_DEPENDENCY = ""

INSANE_SKIP:${PN} = "installed-vs-shipped dev-deps dev-so arch"

EXTRA_OEMESON += " \
                  --prefix=/usr \
                  --libdir=lib/aarch64-linux-gnu \
                 "
do_install:append(){
    if [ "${@bb.utils.contains('DISTRO_FEATURES', 'opengl', 'TRUE', 'FALSE', d)}" == "TRUE" ]; then
        install -d ${D}${libdir}/
        ln -sf aarch64-linux-gnu/libglapi.so.0.0.0  ${D}${libdir}/libglapi.so.0
    fi
}

FILES:mesa-megadriver = ""
FILES:mesa-vulkan-drivers = ""
FILES:${PN}-vdpau-drivers = ""
FILES:libegl-mesa = ""
FILES:libgbm = ""
FILES:libgles1-mesa = ""
FILES:libgles2-mesa = ""
FILES:libgl-mesa = ""
FILES:libglx-mesa = ""
FILES:libopencl-mesa = ""
FILES:libglapi = ""
FILES:libosmesa = ""
FILES:libxatracker = ""

FILES:${PN}-dev = ""
FILES:libegl-mesa-dev = ""
FILES:libgbm-dev = ""
FILES:libgl-mesa-dev = ""
FILES:libglx-mesa-dev = ""
FILES:libglapi-dev = ""
FILES:libgles1-mesa-dev = ""
FILES:libgles2-mesa-dev = ""
FILES:libgles3-mesa-dev = ""
FILES:libopencl-mesa-dev = ""
FILES:libosmesa-dev = ""
FILES:libxatracker-dev = " "

FILES:${PN} = "\
               ${libdir}/libglapi.so.0 \
               ${libdir}/aarch64-linux-gnu/* \
               /usr/share/vulkan/icd.d/* \
               /usr/share/drirc.d/* \
              "

SYSROOT_DIRS_IGNORE += " \
               ${includedir} \
              "

# catch all to get all the tools and data
FILES:${PN}-tools = "${bindir} ${datadir}"
ALLOW_EMPTY:${PN}-tools = "1"

