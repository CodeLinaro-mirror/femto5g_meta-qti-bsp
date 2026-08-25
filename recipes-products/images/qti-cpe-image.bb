# QTI Linux cpe image file.
# Provides packages required to build an cpe image with
# boot to console with connectivity support.

inherit qimage ${@bb.utils.contains_any('MACHINE_FEATURES', 'dm-verity-initramfs-v2 dm-verity-initramfs', 'qramdisk', '', d)}

#IMAGE_FEATURES += "nand2x"
IMAGE_FEATURES += "read-only-rootfs"

# gluebi is read only and prevents debugging/experimentation. Only enable in user variant
IMAGE_FEATURES:append:qti-distro-base-user = " gluebi"

IMAGE_INSTALL:append = "${@bb.utils.contains('DISTRO_FEATURES', 'apparmor', ' apparmor rdk-apparmor-profiles ', '', d)}"

IMAGE_INSTALL:append = "\
${@bb.utils.contains('BBFILE_COLLECTIONS', 'qti-rdkb', 'packagegroup-rdkb', '', d)} \
"

CORE_IMAGE_EXTRA_INSTALL += "\
                glib-2.0 \
                kernel-modules \
                coreutils \
                powerapp \
                powerapp-powerconfig \
                powerapp-reboot \
                powerapp-shutdown \
                systemd-machine-units \
                packagegroup-qti-core \
                packagegroup-startup-scripts \
                packagegroup-android-utils-base \
                packagegroup-filesystem-utils-base \
                packagegroup-startup-scripts-base \
                packagegroup-qti-ss-mgr \
                packagegroup-support-utils \
                packagegroup-qti-fastrpc \
                packagegroup-qti-data \
                packagegroup-qcom-securemsm \
		packagegroup-open-mac80211 \
                ${@bb.utils.contains('MACHINE_FEATURES', 'qti-ssdk', "packagegroup-qti-ssdk", "", d)} \
                ${@bb.utils.contains('MACHINE_FEATURES', 'qti-location', 'packagegroup-qti-location', '', d)} \
                ${@bb.utils.contains('BBFILE_COLLECTIONS', 'qti-internal', 'packagegroup-qti-internal', '', d)} \
                ${@bb.utils.contains('BBFILE_COLLECTIONS', 'cta', 'packagegroup-qti-cta', '', d)} \
"

IMAGE_INSTALL:append = " libatomic"

do_cleanup_sepolicy() {

        policy_version=33
        policy_type=mls
        policy_dir=${IMAGE_ROOTFS}/etc/selinux/${policy_type}/policy
        recovery_policy=${policy_dir}/recovery.policy.${policy_version}
        if [ -f ${recovery_policy} ]; then
                rm ${recovery_policy}
        fi
}

ROOTFS_POSTPROCESS_COMMAND += "${@bb.utils.contains('DISTRO_FEATURES', 'selinux', 'do_cleanup_sepolicy;', '', d)}"

#Install bash
CORE_IMAGE_EXTRA_INSTALL += "bash"

#Install Audio packagegroup
CORE_IMAGE_EXTRA_INSTALL += "packagegroup-qcom-audio"
