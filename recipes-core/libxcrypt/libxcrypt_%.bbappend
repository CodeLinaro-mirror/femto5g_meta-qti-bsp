# The Wrynose OE-Core libxcrypt 4.5.2 source cannot be used with the
# Kalama CLO layer stack because it does not contain the Kalama-specific
# compatibility changes required by the target. Use the CLO libxcrypt
# fork from the besser82/develop branch instead.
SRC_URI:remove = "git://github.com/besser82/libxcrypt.git;branch=master;protocol=https;tag=v${PV}"

SRC_URI:prepend = "${CLO_LE_GIT}/libxcrypt.git;branch=besser82/develop;protocol=https "
