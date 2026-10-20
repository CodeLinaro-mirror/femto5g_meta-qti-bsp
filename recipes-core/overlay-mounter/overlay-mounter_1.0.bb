SUMMARY = "Dedicated overlay filesystem mounter"
DESCRIPTION = "Small helper binary that mounts overlayfs on /data or /etc \
using a dedicated SELinux domain (overlay_mounter_t), replacing the use of \
the generic mount_t as the stashed mounter credential."
LICENSE = "BSD-3-Clause-Clear"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=7a434440b651f4a472ca93716d01033a"

SRC_URI = "file://overlay-mounter.c"

S = "${WORKDIR}"

COMPATIBLE_MACHINE = "alor|vienna|pebble|seraph"

do_compile() {
    ${CC} ${CFLAGS} ${LDFLAGS} -o overlay-mounter ${WORKDIR}/overlay-mounter.c
}

do_install() {
    install -d ${D}${libexecdir}
    install -m 0755 ${B}/overlay-mounter ${D}${libexecdir}/overlay-mounter
}

FILES:${PN} = "${libexecdir}/overlay-mounter"
