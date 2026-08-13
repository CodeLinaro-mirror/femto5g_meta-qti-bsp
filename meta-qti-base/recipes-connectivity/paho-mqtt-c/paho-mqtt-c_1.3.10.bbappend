# Append to paho-mqtt-c to add external-signing callback support for TA-backed mTLS.
# Patches are applied by the standard do_patch task (quilt, -p1 against ${S}).

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

CFLAGS:append  = " -Dstrlcpy=g_strlcpy -DUNIXSOCK -I${STAGING_INCDIR}/glib-2.0 -I${STAGING_LIBDIR}/glib-2.0/include"
LDFLAGS:append = " -Wl,--no-as-needed -lglib-2.0 -Wl,--as-needed"
DEPENDS += "glib-2.0"

SRC_URI += " \
    file://0001-paho-mqtt-c-add-ssl_external_sign_cb-to-MQTTClient_S.patch \
    file://0002-paho-mqtt-c-implement-external-signing-and-ssl_hands.patch \
    file://0003-paho-mqtt-c-add-SSLSocket-external-signing-key-and-d.patch \
    file://0004-paho-mqtt-c-add-Unix-domain-socket-support-to-Socket.patch \
"
