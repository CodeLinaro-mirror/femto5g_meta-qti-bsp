FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
# Binutils 2.46.x already carries the bundled-zlib fix for CVE-2023-45853.
# Applying the older backport duplicates the checks and fails patch-fuzz QA.
SRC_URI += "file://CVE-2021-3998.patch"
