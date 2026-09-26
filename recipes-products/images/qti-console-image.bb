# QTI Linux minimal boot image file.
# Provides packages required to build an image with
# boot to console and wifi support.

inherit qimage


# use DISTRO_EXTRA_RDEPENDS = "list of packages"
# in distro conf file. These listed packages are specific to distro
# use MACHINE_EXTRA_RDEPENDS = "list of packages"
# these packages are complementary to image and specific to machine.
# specify IMAGE_FEATURES += "ssh-server-openssh" to bring in
#    packagegroup-core-ssh-openssh -> openssh

IMAGE_FEATURES += "ssh-server-openssh"
DEPENDS += "ext4-utils-native"

CORE_IMAGE_EXTRA_INSTALL += "\
              glib-2.0 \
              kernel-modules \
              packagegroup-android-utils \
              packagegroup-filesystem-utils \
              packagegroup-qti-core \
              packagegroup-qti-dsp \
              packagegroup-qti-ss-mgr \
              packagegroup-startup-scripts \
              packagegroup-support-utils \
              systemd-machine-units \
              ${@bb.utils.contains('DISTRO_FEATURES','selinux', 'packagegroup-selinux-minimal', '', d)} \
"

# Skip OTA/recovery EXT4 generation for this image
do_recovery_ext4[noexec] = "1"

# Skip OTA and recovery generation for noselinux image build
do_recovery_ext4[noexec] = "1"
do_gen_ota_full_zip_ext4[noexec] = "1"
