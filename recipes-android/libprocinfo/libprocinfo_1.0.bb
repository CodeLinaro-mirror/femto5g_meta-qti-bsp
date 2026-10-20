
DESCRIPTION = "libprocinfo utility"
HOMEPAGE = "http://developer.android.com/"

LICENSE  = "Apache-2.0 & BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/\
Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

LIC_FILES_CHKSUM += "file://${COREBASE}/meta-qti-bsp/files/common-licenses/\
BSD-3-Clause-Clear;md5=3771d4920bd6cdb8cbdf1e8344489ee0"

DEPENDS = "libbase libcutils libutils gtest"

S = "${WORKDIR}/git"
# matches with android11-mainline-release release
SRCREV= "86e630123f122225313adc8b02008210c0c62d28"

SRC_URI = "${CLO_LA_GIT}/platform/system/libprocinfo;name=native;protocol=https;branch=aosp-new/android12-gsi;destsuffix=git \
	   file://libprocinfo-port.patch;patchdir=${S} \
          "

CPPFLAGS += "-I${S}/include -std=c++17 -Wno-error=pointer-to-int-cast"
LDFLAGS += "-lpthread -lbase -llog -lutils -lcutils -L${S}"

do_compile() {
	 ${CXX} ${S}/process.cpp ${CFLAGS} ${CPPFLAGS} ${LDFLAGS} --shared -fPIC -o libprocinfo.so
	 ${CXX} ${S}/process_test.cpp ${S}/process_map_test.cpp ${CFLAGS} ${CPPFLAGS} ${LDFLAGS} -lbase -lgtest_main -lpthread -lgtest -lprocinfo -o libprocinfo_test
}

do_install() {
     install -d ${D}${bindir}
     install -d ${D}${libdir}
     install -d ${D}${includedir}
     install -d ${D}${includedir}/procinfo/
     install -m 0755 libprocinfo_test ${D}${bindir}
     install -m 0755 libprocinfo.so   ${D}${libdir}
     install -m 0755 ${S}/include/procinfo/process.h ${D}${includedir}/procinfo/process.h
     install -m 0755 ${S}/include/procinfo/process_map.h ${D}${includedir}/procinfo/process_map.h
}

FILES:${PN} = "${libdir}/libprocinfo.so \
	       ${bindir}/libprocinfo_test \
		"
FILES:${PN}-dev += "${libdir}/liblibmctp.so \
	            ${bindir}/libprocinfo_test \
		   "

FILES:${PN}-dev = "${includedir}"
#PACKAGES = "${PN}"
PACKAGE_ARCH = "${MACHINE_ARCH}"


