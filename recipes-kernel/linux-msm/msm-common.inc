MSM_KERNEL_VERSION ?= "6.12"

PREFERRED_PROVIDER_virtual/mkbootimg = "mkbootimg-gki"
PREFERRED_PROVIDER_virtual/mkdtimg   = "mkdtimg-gki"

PREFERRED_PROVIDER_virtual/kernel = "${@'linux-msm' if d.getVar('MSM_KERNEL_VERSION') in ['5.15', '6.1', '6.6'] else 'linux-soc-repo'}"

PREFERRED_VERSION_linux-msm ?= "${MSM_KERNEL_VERSION}"
PREFERRED_VERSION_linux-soc-repo ?= "${MSM_KERNEL_VERSION}"

PREFERRED_VERSION_PREFERRED_PROVIDER_virtual/kernel ?= "${MSM_KERNEL_VERSION}"
PREFERRED_VERSION_linux-msm-headers ?= "${MSM_KERNEL_VERSION}"
PREFERRED_VERSION_kernel-toolchain ?= "${MSM_KERNEL_VERSION}"
PREFERRED_VERSION_kernel-toolchain-native ?= "${MSM_KERNEL_VERSION}"
PREFERRED_VERSION_mkbootimg-gki ?= "${MSM_KERNEL_VERSION}"
PREFERRED_VERSION_mkbootimg-gki-native ?= "${MSM_KERNEL_VERSION}"
PREFERRED_VERSION_mod-signing-keys ?= "${MSM_KERNEL_VERSION}"
PREFERRED_VERSION_dtc-android-build ?= "1.6.0"

#### KERNEL ####
MACHINE_USES_KERNEL_PREBUILTS = "True"
KERNEL_VARIANT ??= ""
KERNEL_VARIANT:qti-distro-debug ?= "debug-defconfig"
KERNEL_VARIANT:qti-distro-perf ?= "defconfig"
BUILD_VARIANT:qti-distro-debug ?= "debug_defconfig"
BUILD_VARIANT:qti-distro-perf ?= "defconfig"

KERNEL_SRC_TYPE = "${@'msm-kernel' if d.getVar('MSM_KERNEL_VERSION') in ['5.15', '6.1', '6.6'] else 'soc-repo'}"
KERNEL_TYPE ?= "dist"

#### Initramfs configuration ####
INITRAMFS_IMAGE ?= "qti-ramdisk-image"
INITRAMFS_FSTYPES ?= "cpio.lz4"
VENDOR_INITRAMFS_IMAGE ?= "qti-vendor-ramdisk-image"

#### bootloader configuration ####
EXTRA_IMAGEDEPENDS += "edk2"
PREFERRED_VERSION_edk2 ?= "4.0"

#### KP build paths
KERNEL_DEFCONFIG ?= "msm-kernel-${BASEMACHINE}_le-${BUILD_VARIANT}"
KERNEL_PLATFORM_PATH = "${WORKSPACE}/kernel-${MSM_KERNEL_VERSION}/kernel_platform"
KERNEL_PREBUILT_PATH = "${WORKSPACE}/kernel-${MSM_KERNEL_VERSION}/out/${KERNEL_DEFCONFIG}"
KERNEL_PREBUILT_DISTDIR = "${KERNEL_PREBUILT_PATH}/dist"
KERNEL_OUT_PATH = "${KERNEL_PREBUILT_PATH}"

#### DDK compilation support ####
DDK_BUILD ?= "${@'true' if d.getVar('MSM_KERNEL_VERSION') not in ['5.15', '6.1', '6.6'] else ''}"
TARGET_BOARD_PLATFORM ?= "${BASEMACHINE}-le"

#### others ####
PAGE_SIZE ?= '4096'
SDCARD_DEVICE ?= "mmcblk1p1"
MACHINE_SUPPORTS_DTBO ?= "True"
BUILD_WITH_TECHPACKS ?= "1"
SKIP_VENDOR_BOOT ?= "False"
