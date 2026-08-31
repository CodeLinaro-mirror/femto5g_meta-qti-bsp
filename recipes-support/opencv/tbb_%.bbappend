# Patch TBB GNU compiler settings to skip -flto on aarch64
#
EXTRA_OECMAKE:append:aarch64 = " -DTBB_ENABLE_IPO=OFF "
FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI:append = " file://0001-meta-qti-bsp-fix-tbb-compile-issues-in-perf-build-on.patch \
"
