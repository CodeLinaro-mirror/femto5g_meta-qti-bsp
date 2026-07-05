# Support systemd-user-session
DISTRO_FEATURES:append = " pam"

# Video
DISTRO_FEATURES:append:sa6155 = " qti-carcamsrc qti-omx"
DISTRO_FEATURES:append:sa81x5-rt = " qti-carcamsrc qti-omx"
DISTRO_FEATURES:append:sa81x5 = " qti-carcamsrc qti-fastcv qti-omx"
DISTRO_FEATURES:append:sa8295 = " qti-codec2"
DISTRO_FEATURES:append:qtiquingvm = " qti-omx"
DISTRO_FEATURES:append:qtiquingvm8295 = " qti-codec2 qti-gstdeinterlace qti-gstqvrate qti-gstqvais"
DISTRO_FEATURES:append:quin-gvm-gen4 = " qti-codec2 qti-gstdeinterlace qti-gstqvrate qti-gstqvais"
DISTRO_FEATURES:append:quin-gvm-gen4-5 = " qti-codec2 qti-gstdeinterlace qti-gstqcarcamsrc"
DISTRO_FEATURES:append:gvm-gen4-5 = " qti-codec2 qti-gstdeinterlace"

# Vnw
DISTRO_FEATURES:append:qtiquingvm = " qti-gstopenavb"
DISTRO_FEATURES:append:qtiquingvm8295 = " qti-gstopenavb"
DISTRO_FEATURES:append:quin-gvm-gen4 = " qti-gstopenavb"
DISTRO_FEATURES:append:quin-gvm-lemans = " qti-gstopenavb"
DISTRO_FEATURES:append:quin-gvm-monaco = " qti-gstopenavb"

# Camera
DISTRO_FEATURES:append:quin-gvm-lemans = " qti-qcx"
DISTRO_FEATURES:append:quin-gvm-monaco = " qti-qcx"
DISTRO_FEATURES:append:quin-gvm-gen4-5 = " qti-qcx"
DISTRO_FEATURES:append:gvm-gen4-5 = " qti-qcx"
DISTRO_FEATURES:append:gvm-gen5 = " qti-qcx"

# Display
DISTRO_FEATURES:append = " wayland"


# Use synergy, disable bluez
DISTRO_FEATURES_BACKFILL_CONSIDERED += "bluez5 bluetooth"

#Enable persist-image
DISTRO_FEATURES:append = " persist-image "

DISTRO_FEATURES:append = " qti-early-eth"
#DISTRO_FEATURES:append = " early_init"
#DISTRO_FEATURES:append = " early_userspace"


DISTRO_FEATURES:append = " hibernation"

# Use systemd init manager.
DISTRO_FEATURES:append = " systemd ${@bb.utils.contains('LAYERSERIES_CORENAMES', 'scarthgap', 'usrmerge', '', d)}"
DISTRO_FEATURES_BACKFILL_CONSIDERED += "sysvinit"

DISTRO_FEATURES:append = " vulkan"

# WLAN
DISTRO_FEATURES:append = " qti-wlan wifi"

# BT
DISTRO_FEATURES:append:qtiquingvm8295 = " qti-bluetooth"
DISTRO_FEATURES:append:quin-gvm-gen4 = " qti-bluetooth"
DISTRO_FEATURES:append:quin-gvm-gen4-2 = " qti-bluetooth"
DISTRO_FEATURES:append:quin-gvm-lemans = " qti-bluetooth"
DISTRO_FEATURES:append:quin-gvm-gen4-5 = " qti-bluetooth"
DISTRO_FEATURES:append:gvm-gen4-5 = " qti-bluetooth"
DISTRO_FEATURES:append:quin-gvm-monaco = " qti-bluetooth"

# SELinux
DISTRO_FEATURES:append:sa8775 = " selinux"
DISTRO_FEATURES:append:gvm-gen4-5 = " selinux"
DISTRO_FEATURES:append:quin-gvm-gen4-5 = " selinux"
DISTRO_FEATURES_NATIVE:append:sa8775 = " selinux"
DISTRO_FEATURES_NATIVE:append:gvm-gen4-5 = " selinux"
DISTRO_FEATURES_NATIVE:append:quin-gvm-gen4-5 = " selinux"

# Preferred refpolicy
PREFERRED_PROVIDER_virtual/refpolicy:sa8775 = "selinux-policy"
PREFERRED_PROVIDER_virtual/refpolicy:gvm-gen4-5 = "selinux-policy"
PREFERRED_PROVIDER_virtual/refpolicy:quin-gvm-gen4-5 = "selinux-policy"
DEFAULT_ENFORCING:sa8775 = "enforcing"
DEFAULT_ENFORCING:gvm-gen4-5 = "enforcing"
DEFAULT_ENFORCING:quin-gvm-gen4-5 = "enforcing"

# volatile /var
DISTRO_FEATURES:append:quin-gvm-gen4-5 = " volatiled-var"
