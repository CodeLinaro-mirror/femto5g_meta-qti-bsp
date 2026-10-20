DESCRIPTION = "This package contains some simple command line tools to help using Linux spidev devices"
HOMEPAGE = "https://github.com/cpb-/spi-tools"
AUTHOR = "Christophe BLAESS"
LICENSE="GPL-2.0-only"
LIC_FILES_CHKSUM = "file://LICENSE;md5=8c16666ae6c159876a0ba63099614381"

S = "${WORKDIR}/git"

SRC_URI = "git://github.com/cpb-/spi-tools.git;protocol=https;branch=master"
SRC_URI:append = " \
                    file://0001-Allow-CFLAGS-to-be-passed-to-.-configure.patch \
                  "

SRCREV = "03405ab45884e4264dfa0371c032b2baaeeaaa98"

inherit autotools
