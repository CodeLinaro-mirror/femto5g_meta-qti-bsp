inherit autotools

DESCRIPTION = "Android Verified Boot Library"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=f0f3a517d46b5f0ca048b58f503b6dc1"

SRCREV = "b27a9aa53d2db7720e4d88d6950dcd979da97de3"

SRC_URI = "git://github.com/AndroidBootloader/platform_external_avb.git;branch=master;protocol=https"
SRC_URI += "file://0001-Add-autotool-make-files-for-libavb.patch"

S = "${WORKDIR}/git"

FILES:${PN} += "${base_libdir}/*"
