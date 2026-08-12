FILESBBAPPENDPATH := "${THISDIR}"
FILESEXTRAPATHS =. "${FILESBBAPPENDPATH}/${BP}:${FILESBBAPPENDPATH}/${BPN}:"

# Add glib-2.0 dependency to support g_strlcat
DEPENDS += "glib-2.0"

SRC_URI:append = " \
    file://0033-systemd-Make-root-s-home-directory-configurable-2.patch \
    file://0001-systemd-avoid-active-seat-change-to-NULL.patch \
    file://60-misc.rules \
    file://0001-systemd-config-linger-for-root-user.patch \
    file://0035-systemd-Change-job-print-method-to-Normal-way.patch \
"

SRC_URI:append:quin-gvm-gen4-5 = " file://70-vblk.rules"

SRC_URI:append:gvm-gen5 = " file://60-vblk.rules"

SRC_URI:append:gvm-gen4-5 = " file://60-vblk.rules \
                              file://80-vblk.rules \
"

SRC_URI:append:sa81x5 = " file://0001-systemd-add-slotselect-support-in-fstab.patch"

SRC_URI:append = " ${@bb.utils.contains("PREFERRED_VERSION_linux-msm", "6.1", "file://platform_load.conf", "", d)}"

SRC_URI:append = " ${@bb.utils.contains("PREFERRED_VERSION_linux-msm", "6.12", "file://linux-msm-6.12_modules_load.conf", "", d)}"

# Remove backlight - Loads/Saves Screen Backlight Brightness, not required.
PACKAGECONFIG:remove = "backlight "

#Disable systemd-timesyncd which not used in project.
PACKAGECONFIG:remove = "timesyncd "

# Use glib-2.0 for g_strlcat
CFLAGS:append = " \
    -fPIC \
    -DUSE_GLIB \
    -I${STAGING_INCDIR}/glib-2.0 \
    -I${STAGING_LIBDIR}/glib-2.0/include \
    -I${STAGING_LIBDIR}/glib-2.0/glib \
"

LDFLAGS:append = " -lglib-2.0"

# In aarch64 targets systemd is not booting with -finline-functions -finline-limit=64 optimizations
# So temporarily revert to default optimizations for systemd.
FULL_OPTIMIZATION = "-O2 -fexpensive-optimizations -frename-registers -fomit-frame-pointer -ftree-vectorize"

do_install:append:sa81x5() {
    # workaroud for suspend, suspend is not allowd in systemd-sleep, LA will trigger suspend
    if ${@bb.utils.contains('DISTRO_FEATURES', 'qti-lxc', 'true', 'false', d)}; then
        sed -i 's/#AllowSuspend=yes/AllowSuspend=no/' ${D}${sysconfdir}/systemd/sleep.conf
    fi
}

do_install:append () {
    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-umd', 'true', 'false', d)}; then
        echo "DefaultLimitNOFILE=infinity" >> ${D}${sysconfdir}/systemd/system.conf
        echo "DefaultLimitMSGQUEUE=infinity" >> ${D}${sysconfdir}/systemd/system.conf
    fi

    # Use kernel rules for network iface name
    sed -i  's/^NamePolicy.*/NamePolicy=kernel/g' ${D}${systemd_unitdir}/network/99-default.link

    #Remove privatetmp=true from hostname service
    sed -i  '/^PrivateTmp.*/d' ${D}${systemd_system_unitdir}/systemd-hostnamed.service

    # Remove orignal 60-persistent-v4l.rules which is not applicable for QTI video
    rm ${D}${nonarch_base_libdir}/udev/rules.d/60-persistent-v4l.rules

    # Add platform_load.conf to /etc/modules-load.d/, systemd will load modules in this file.
    if ${@bb.utils.contains("PREFERRED_VERSION_linux-msm", "6.1", "true", "false", d)}; then
        install -m 0664 ${WORKDIR}/platform_load.conf ${D}${sysconfdir}/modules-load.d/
    fi

    if ${@bb.utils.contains("PREFERRED_VERSION_linux-msm", "6.12", "true", "false", d)}; then
        install -m 0664 ${WORKDIR}/linux-msm-6.12_modules_load.conf ${D}${sysconfdir}/modules-load.d/
    fi
}

do_install:append:quin-gvm-gen4-5() {
    install -m 0644 ${WORKDIR}/70-vblk.rules ${D}${sysconfdir}/udev/rules.d/
}

do_install:append:gvm-gen5() {
    install -m 0644 ${WORKDIR}/60-vblk.rules ${D}${sysconfdir}/udev/rules.d/
}

do_install:append:gvm-gen4-5() {
    install -m 0644 ${WORKDIR}/60-vblk.rules ${D}${sysconfdir}/udev/rules.d/
    install -m 0644 ${WORKDIR}/80-vblk.rules ${D}${sysconfdir}/udev/rules.d/
}
