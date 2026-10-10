FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}-${PV}:"

SRC_URI += "\
           file://aupathfix.patch \
"

# Locate the BSP patch for Kalama, including native and SDK variants.
FILESEXTRAPATHS:append := "${@'${QTIBASE}/recipes-devtools/${BPN}/${BPN}-%:' if d.getVar('MACHINE') == 'kalama' else ''}"
