# The BSP provides libuv 1.10.1, while CMake 4.3 requires system libuv >= 1.28.
# Use CMake's bundled libuv for the target build, matching cmake-native.
EXTRA_OECMAKE:append = " -DCMAKE_USE_SYSTEM_LIBRARY_LIBUV=0"
