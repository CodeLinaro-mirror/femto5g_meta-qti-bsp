# Kalama requires the CLO libxcrypt source instead of Wrynose OE-Core 4.5.2.
SRC_URI:remove = "git://github.com/besser82/libxcrypt.git;branch=master;protocol=https;tag=v4.5.2"

SRC_URI:prepend = "${CLO_LE_GIT}/libxcrypt.git;branch=besser82/develop;protocol=https "
