SUMMARY = "QTI FastRPC package group"
DESCRIPTION = "Installs Qualcomm FastRPC libraries, services, and tests"
LICENSE = "BSD-3-Clause-Clear"

inherit packagegroup

RDEPENDS:${PN} = "\
    fastrpc \
"
