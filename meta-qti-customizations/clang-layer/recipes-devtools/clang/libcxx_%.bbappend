# LLVM CMake treats warning output during compiler-flag probes as a failed check.
# QTI debug builds pass GCC-only flags that clang warns about, which prevents
# libc++/libc++abi from detecting -nostdinc++ and lets libstdc++ wrapper headers
# leak into the libc++ build.
CFLAGS:append:toolchain-clang = " -Wno-unknown-warning-option -Wno-ignored-optimization-argument"
CXXFLAGS:append:toolchain-clang = " -Wno-unknown-warning-option -Wno-ignored-optimization-argument"
