DESCRIPTION = "AOSP host-side OTA key tool (dumpkey) from OTA/system/core"
# DumpPublicKey source is pure Apache-2.0. BouncyCastle (folded into the fat jar
# at package time) is licensed and tracked by bouncycastle-native via DEPENDS.
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

# This recipe owns one source repo only: OTA/system/core libmincrypt/tools
# (DumpPublicKey). OTA/system/core is repo-manifest managed and already checked
# out under ${WORKSPACE}; reference its Java source via FILESEXTRAPATHS rather
# than a duplicate git SRC_URI revision, so the manifest stays the single source
# of revision truth.
FILESEXTRAPATHS:prepend := "${WORKSPACE}/OTA/system/core/libmincrypt/tools/:"

SRC_URI = " \
    file://DumpPublicKey.java \
"

# BouncyCastle is built once by bouncycastle-native and staged into the native
# sysroot; consume it here on the javac classpath and fold its classes into the
# output jar so dumpkey.jar stays a standalone fat jar runnable with `java -jar`.
DEPENDS += "bouncycastle-native"
BC_JAR = "${STAGING_LIBDIR_NATIVE}/bcprov_bcpkix_jar.jar"

BUILD_DIR = "${WORKDIR}/build"

inherit deploy native

do_configure[noexec] = "1"

do_compile() {
    rm -rf ${BUILD_DIR}
    mkdir -p ${BUILD_DIR}/classes ${BUILD_DIR}/libs
    mkdir -p ${BUILD_DIR}/dumpkey-src/com/android/dumpkey

    cp ${WORKDIR}/DumpPublicKey.java ${BUILD_DIR}/dumpkey-src/com/android/dumpkey/

    # Fold BouncyCastle classes in so the jar is a standalone fat jar.
    ( cd ${BUILD_DIR}/classes && /usr/bin/jar -xf ${BC_JAR} )

    /usr/bin/javac -J-Xmx1024M -Xmaxerrs 9999999 -encoding UTF-8 -g \
        -classpath ${BC_JAR} \
        -d ${BUILD_DIR}/classes \
        ${BUILD_DIR}/dumpkey-src/com/android/dumpkey/DumpPublicKey.java
    echo "Main-Class: com.android.dumpkey.DumpPublicKey" > ${BUILD_DIR}/DumpPublicKey.mf
    /usr/bin/jar -cfm ${BUILD_DIR}/libs/dumpkey.jar ${BUILD_DIR}/DumpPublicKey.mf -C ${BUILD_DIR}/classes .
}

do_install() {
    install -d ${D}${bindir}
    install -m 644 ${BUILD_DIR}/libs/dumpkey.jar ${D}${bindir}/dumpkey.jar
}

do_deploy() {
    install -d ${DEPLOYDIR}/ota-scripts/framework
    install -m 644 ${BUILD_DIR}/libs/dumpkey.jar \
        ${DEPLOYDIR}/ota-scripts/framework/dumpkey.jar
}
addtask deploy after do_install before do_build
