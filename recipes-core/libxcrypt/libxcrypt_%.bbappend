#libxcrypt package is fetching from codelinaro
SRC_URI:remove = " git://github.com/besser82/libxcrypt.git;branch=${SRCBRANCH};protocol=https "
SRC_URI:prepend = " ${CLO_LE_GIT}/libxcrypt.git;branch=${SRCBRANCH};protocol=https "
SRCBRANCH = "besser82/develop"
