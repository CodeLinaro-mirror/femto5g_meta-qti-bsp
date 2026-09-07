HOMEPAGE = "https://github.com/qualcomm/userspace-resource-manager-extensions"
SUMMARY = "Plugins for extending and customizing URM functionality"
DESCRIPTION = "The URM Extensions framework provides a mechanism for customizing \
URM behaviour, for example: Add new custom resources and signals without modifying \
the core URM codebase, Override default resource handlers with custom implementations, \
Provide target-specific configurations for different hardware platforms. \
The URM Extensions framework allows for extending URM functionality through a clean plugin architecture."
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=2998c54c288b081076c9af987bdf4838"

SRC_URI = "git://github.com/qualcomm/userspace-resource-manager-extensions.git;protocol=https;branch=main;"
SRCREV = "706c825eab86aa28b22021e4f2e336eef6d9876c"

S = "${WORKDIR}/git"
inherit cmake
DEPENDS += " userspace-resource-manager"
FILES:${PN}-dev += "${libdir}/urm/libUrmPlugin.so"
FILES:${PN} += " \
    ${libdir}/urm/libUrmPlugin.so* \
"
