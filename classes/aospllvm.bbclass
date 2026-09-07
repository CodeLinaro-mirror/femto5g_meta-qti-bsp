# Path to the clang binaries (installed by kernel-toolchain-native)
KERNEL_TOOLCHAIN_BIN ?= "${STAGING_BINDIR_NATIVE}/clang/bin"

# Override the necessary toolchain environment variables.
CC:toolchain-aospllvm  = "${KERNEL_TOOLCHAIN_BIN}/clang -target ${TARGET_SYS} ${HOST_CC_ARCH}${TOOLCHAIN_OPTIONS}"
CXX:toolchain-aospllvm = "${KERNEL_TOOLCHAIN_BIN}/clang++ -target ${TARGET_SYS} ${HOST_CC_ARCH}${TOOLCHAIN_OPTIONS}"
CPP:toolchain-aospllvm = "${KERNEL_TOOLCHAIN_BIN}/clang -target ${TARGET_SYS} -E ${TOOLCHAIN_OPTIONS} ${HOST_CC_ARCH}"
CCLD:toolchain-aospllvm = "${KERNEL_TOOLCHAIN_BIN}/clang ${HOST_CC_ARCH}${TOOLCHAIN_OPTIONS}"

# For cmake
OECMAKE_C_COMPILER:toolchain-aospllvm   = "${KERNEL_TOOLCHAIN_BIN}/clang"
OECMAKE_CXX_COMPILER:toolchain-aospllvm = "${KERNEL_TOOLCHAIN_BIN}/clang++"
OECMAKE_C_FLAGS:toolchain-aospllvm     += " -target ${TARGET_SYS} ${HOST_CC_ARCH} ${TOOLCHAIN_OPTIONS} ${CFLAGS}"
OECMAKE_CXX_FLAGS:toolchain-aospllvm   += " -target ${TARGET_SYS} ${HOST_CC_ARCH} ${TOOLCHAIN_OPTIONS} ${CXXFLAGS}"
OECMAKE_EXTRA_ROOT_PATH:toolchain-aospllvm = "${STAGING_BINDIR_TOOLCHAIN}"

THUMB_TUNE_CCARGS:remove:toolchain-aospllvm = "-mthumb-interwork"
TUNE_CCARGS:remove:toolchain-aospllvm = "-meb"
TUNE_CCARGS:remove:toolchain-aospllvm = "-mel"
TUNE_CCARGS:append:toolchain-aospllvm = " -D__extern_always_inline=inline  -Wno-error=unused-command-line-argument -Qunused-arguments"

# Remove unsupported compiler flags
SELECTED_OPTIMIZATION:remove:toolchain-aospllvm = "-fexpensive-optimizations"
SELECTED_OPTIMIZATION:remove:toolchain-aospllvm = "-frename-registers"
SELECTED_OPTIMIZATION:remove:toolchain-aospllvm = "-finline-functions"
SELECTED_OPTIMIZATION:remove:toolchain-aospllvm = "-finline-limit=64"
SELECTED_OPTIMIZATION:remove:toolchain-aospllvm = "-Wno-error=maybe-uninitialized"

# Remove GCC-specific flags that are not supported by Clang
DEBUG_PREFIX_MAP:remove:toolchain-aospllvm = "-fcanon-prefix-map"
TARGET_CC_ARCH:remove:toolchain-aospllvm = "-fcanon-prefix-map"

# Suppress some common build warnings
SELECTED_OPTIMIZATION:append:toolchain-aospllvm = " -Wno-error=uninitialized"
SELECTED_OPTIMIZATION:append:toolchain-aospllvm = " -Wno-unused-variable"
SELECTED_OPTIMIZATION:append:toolchain-aospllvm = " -Wno-unused-private-field"
#SELECTED_OPTIMIZATION:append:toolchain-aospllvm = " -Wno-error=undefined-optimized"

# choose between 'gcc' and 'sdllvm' and 'aospllvm' for toolchain. Default is gcc
TOOLCHAIN ??= "gcc"

TOOLCHAIN:class-native = "gcc"
TOOLCHAIN:class-nativesdk = "gcc"
TOOLCHAIN:class-cross-canadian = "gcc"
TOOLCHAIN:class-crosssdk = "gcc"
TOOLCHAIN:class-cross = "gcc"

# Add toolchain to pkg overrides.
OVERRIDES =. "${@['', 'toolchain-${TOOLCHAIN}:']['${TOOLCHAIN}' != '']}"
OVERRIDES[vardepsexclude] += "TOOLCHAIN"

BASEDEPENDS:append:toolchain-aospllvm:class-target = " kernel-toolchain-native"

QTI_LLVM_VARIANT = "aospllvm"
