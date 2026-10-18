PROVIDES = "virtual/mesa"
PACKAGE_ARCH = "${TUNE_PKGARCH}"

PACKAGECONFIG = "expat zlib xmlconfig gallium gallium-llvm opengl egl gles"

EXTRA_OEMESON += "--libdir=lib/aarch64-linux-gnu"

SYSROOT_DIRS_IGNORE += " ${includedir} "
DEV_PKG_DEPENDENCY = ""

# Blank every component FILES that points at the plain ${libdir} so
# nothing from Mesa lands where adreno/gbm already own files.
FILES:mesa-megadriver = ""
FILES:mesa-vulkan-drivers = ""
FILES:libegl-mesa = ""
FILES:libgbm = ""
FILES:libgallium = ""
FILES:libgles1-mesa = ""
FILES:libgles2-mesa = ""
FILES:libgl-mesa = ""
FILES:libglx-mesa = ""
FILES:libopencl-mesa = ""
FILES:libglapi = ""
FILES:libteflon = ""
FILES:libegl-mesa-dev = ""
FILES:libgbm-dev = ""
FILES:libgl-mesa-dev = ""
FILES:libglapi-dev = ""
FILES:libgles1-mesa-dev = ""
FILES:libgles2-mesa-dev = ""
FILES:${PN}-dev = ""

# Catch-all: everything Mesa actually built now lives under the
# relocated libdir; bundle it into the one package packagegroup-mesa
# already RDEPENDS on.
FILES:${PN} = "${libdir}/aarch64-linux-gnu/* ${libdir}/libglapi.so.* ${datadir}/drirc.d ${datadir}/glvnd/egl_vendor.d"
FILES:${PN}-tools = "${bindir} ${datadir}"
ALLOW_EMPTY:${PN}-tools = "1"

INSANE_SKIP:${PN} += "installed-vs-shipped dev-deps dev-so arch"

# Everything else moved to the relocated libdir above, but libglapi is an
# internal ABI shim some mesa-adjacent consumers probe for at the plain
# libdir path (matches the proven LE1.0 bbappend) - keep one real soname
# symlink there without hardcoding the exact micro version.
do_install:append() {
    if ${@bb.utils.contains('DISTRO_FEATURES', 'opengl', 'true', 'false', d)}; then
        install -d ${D}${libdir}
        libglapi_so=$(cd ${D}${libdir}/aarch64-linux-gnu && ls libglapi.so.*.*.* 2>/dev/null | head -n1)
        if [ -n "$libglapi_so" ]; then
            ln -sf aarch64-linux-gnu/$libglapi_so ${D}${libdir}/libglapi.so.0
        fi
    fi
}
