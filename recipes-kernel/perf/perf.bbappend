PACKAGECONFIG[libtraceevent] = ",NO_LIBTRACEEVENT=1,libtraceevent"

EXTRA_OEMAKE:append:aarch64 = " EXTRA_CFLAGS='-D__BITS_PER_LONG=64' "
