HOMEPAGE = "https://github.com/qualcomm/userspace-resource-manager"
SUMMARY = "Userspace daemon for dynamic System resource management"
DESCRIPTION = "Userspace Resource Manager(URM) is a lightweight userspace \
daemon that monitors system resources and enforces policies using \
Linux kernel interfaces such as cgroups and sysfs."

LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=2998c54c288b081076c9af987bdf4838"

SRC_URI = "git://github.com/qualcomm/userspace-resource-manager.git;protocol=https;branch=main"
SRCREV = "a69a01c3cb1a56942a57718a6fe66880317adb7b"

S = "${WORKDIR}/git"

inherit cmake pkgconfig systemd

DEPENDS += "libyaml"

CXXFLAGS:append = " -fpermissive"

PACKAGECONFIG ??= "\
    classifier \
    extensions \
    ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'state-detector systemd', '', d)} \
    tests \
"

PACKAGECONFIG[classifier] = "-DBUILD_CLASSIFIER=ON,-DBUILD_CLASSIFIER=OFF"
PACKAGECONFIG[extensions] = "-DBUILD_EXTENSIONS=ON,-DBUILD_EXTENSIONS=OFF"
PACKAGECONFIG[state-detector] = "-DBUILD_STATE_DETECTOR=ON,-DBUILD_STATE_DETECTOR=OFF"
PACKAGECONFIG[systemd] = ",,systemd"
PACKAGECONFIG[tests] = "-DBUILD_TESTS=ON,-DBUILD_TESTS=OFF"

SYSTEMD_SERVICE:${PN} = "urm.service"
FILES:${PN}-dev += " \
    ${libdir}/urm/libUrmTestPlugin.so \
    ${libdir}/urm/libUrmPlugin.so \
"

FILES:${PN} += "${sysconfdir}/urm/*"

PACKAGE_BEFORE_PN += "${PN}-extensions"
FILES:${PN}-extensions += " \
    ${sysconfdir}/urm/target/* \
    ${libexecdir}/urm/initscripts/post_boot/* \
    ${libdir}/urm/libUrmPlugin.so* \
"

PACKAGE_BEFORE_PN += "${PN}-tests"
FILES:${PN}-tests += " \
    ${datadir}/urm/tests/configs/* \
    ${localstatedir}/lib/urm/tests/nodes/* \
    ${bindir}/UrmComponentTests \
    ${bindir}/UrmIntegrationTests \
    ${libdir}/libRestuneTestUtils.so* \
    ${libdir}/urm/libUrmTestPlugin.so* \
"

RRECOMMENDS:${PN} += "${PN}-extensions"
