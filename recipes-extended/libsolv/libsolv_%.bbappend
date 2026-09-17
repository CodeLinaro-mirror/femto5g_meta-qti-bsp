#Package is fetch from the CLO

SRC_URI:remove = "git://github.com/openSUSE/libsolv.git;branch=master;protocol=https;tag=0.7.36"
SRC_URI:prepend = " ${CLO_LE_GIT}/libsolv.git;protocol=https;branch=libsolv/master "
