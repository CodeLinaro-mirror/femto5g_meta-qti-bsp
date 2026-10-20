DESCRIPTION = "AOSP host-side OTA signing tool (signapk) from OTA/build"
# SignApk source is pure Apache-2.0. BouncyCastle (folded into the fat jar at
# package time) is licensed and tracked by bouncycastle-native via the DEPENDS chain.
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/Apache-2.0;md5=89aea4e17d99a7cacdbeed46a0096b10"

# This recipe owns one source repo only: OTA/build tools/signapk (SignApk).
# OTA/build is repo-manifest managed and already checked out under ${WORKSPACE};
# reference its Java sources via FILESEXTRAPATHS rather than a duplicate git
# SRC_URI revision, so the manifest stays the single source of revision truth.
FILESEXTRAPATHS:prepend := "${WORKSPACE}/OTA/build/tools/signapk/src/com/android/signapk/:"

SRC_URI = " \
    file://SignApk.java \
    file://ApkSignerV2.java \
    file://Pair.java \
    file://ZipUtils.java \
"

# BouncyCastle is built once by bouncycastle-native and staged into the native
# sysroot; consume it here on the javac classpath and fold its classes into the
# output jar so signapk.jar stays a standalone fat jar runnable with `java -jar`.
DEPENDS += "bouncycastle-native"
BC_JAR = "${STAGING_LIBDIR_NATIVE}/bcprov_bcpkix_jar.jar"

BUILD_DIR = "${WORKDIR}/build"

inherit deploy native

do_configure[noexec] = "1"

do_compile() {
    rm -rf ${BUILD_DIR}
    mkdir -p ${BUILD_DIR}/classes ${BUILD_DIR}/libs
    mkdir -p ${BUILD_DIR}/signapk-src/com/android/signapk

    cp ${WORKDIR}/SignApk.java     ${BUILD_DIR}/signapk-src/com/android/signapk/
    cp ${WORKDIR}/ApkSignerV2.java ${BUILD_DIR}/signapk-src/com/android/signapk/
    cp ${WORKDIR}/Pair.java        ${BUILD_DIR}/signapk-src/com/android/signapk/
    cp ${WORKDIR}/ZipUtils.java    ${BUILD_DIR}/signapk-src/com/android/signapk/

    # Conscrypt is an APK-v2-signing accelerator (BoringSSL JCE provider), not
    # packaged in this Yocto env and not required for whole-file (-w) OTA signing,
    # which only uses BouncyCastle for PKCS#7 SignedData. Strip the Conscrypt-only
    # refs so SignApk falls back to the JDK default SecurityProvider.
    sed -i '/org.conscrypt.OpenSSLProvider/d' ${BUILD_DIR}/signapk-src/com/android/signapk/SignApk.java
    sed -i '/new OpenSSLProvider()/d'         ${BUILD_DIR}/signapk-src/com/android/signapk/SignApk.java

    # Fold BouncyCastle classes into the same classes dir so the jar is a fat jar.
    ( cd ${BUILD_DIR}/classes && /usr/bin/jar -xf ${BC_JAR} )

    find ${BUILD_DIR}/signapk-src -name "*.java" > ${BUILD_DIR}/signapk_java_source_list
    /usr/bin/javac -J-Xmx1024M -Xmaxerrs 9999999 -encoding UTF-8 -g \
        -classpath ${BC_JAR} \
        -d ${BUILD_DIR}/classes \
        @${BUILD_DIR}/signapk_java_source_list
    echo "Main-Class: com.android.signapk.SignApk" > ${BUILD_DIR}/SignApk.mf
    /usr/bin/jar -cfm ${BUILD_DIR}/libs/signapk.jar ${BUILD_DIR}/SignApk.mf -C ${BUILD_DIR}/classes .
}

do_install() {
    install -d ${D}${bindir}
    install -m 644 ${BUILD_DIR}/libs/signapk.jar ${D}${bindir}/signapk.jar
}

do_deploy() {
    install -d ${DEPLOYDIR}/ota-scripts/framework
    install -m 644 ${BUILD_DIR}/libs/signapk.jar \
        ${DEPLOYDIR}/ota-scripts/framework/signapk.jar
}
addtask deploy after do_install before do_build
