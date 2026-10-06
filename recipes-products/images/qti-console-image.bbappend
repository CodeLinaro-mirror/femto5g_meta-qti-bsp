# Reduced console image: do not use Android fs_config host tooling.
DEPENDS:remove = "fsconfig-native"
DEPENDS:remove = "fsconfig-native bsdiff-native releasetools-native fec-native"
# Reduced console image: no Android recovery/OTA flow.
RDEPENDS:${PN}:remove = "recovery-ab"
IMAGE_INSTALL:remove = "recovery-ab"

# Reduced console image: exclude Android utility packagegroup and FastRPC.
IMAGE_INSTALL:remove = "packagegroup-android-utils packagegroup-qti-bsp"
RDEPENDS:${PN}:remove = "packagegroup-android-utils packagegroup-qti-bsp"

# Reduced console: omit Android/QTI filesystem utilities.
IMAGE_INSTALL:remove = "packagegroup-filesystem-utils"
RDEPENDS:${PN}:remove = "packagegroup-filesystem-utils"
CORE_IMAGE_EXTRA_INSTALL:remove = "packagegroup-filesystem-utils"

# Reduced console: exclude DSP service stack.
IMAGE_INSTALL:remove = "packagegroup-qti-dsp"
CORE_IMAGE_EXTRA_INSTALL:remove = "packagegroup-qti-dsp"
RDEPENDS:${PN}:remove = "packagegroup-qti-dsp"

# mkdtimg-gki-native stages the utility below mkdtboimg/bin.
MKDTUTIL = "mkdtboimg/bin/mkdtimg"

