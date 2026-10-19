FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = " \
        file://autodev.sh \
        file://config \
        file://seccomp.conf \
        file://file_contexts \
        file://dbus_contexts \
        file://0001-fix-error-msg-Unsupported-config-key-lxc.seccomp.patch \
        file://0001-lxc-monitor-Add-watchdog-option.patch \
        file://0002-cgroup-hybrid-mode-refine.patch \
        file://0003-add-lxc-daemon-tool.patch \
        file://0004-fix-monitor-cgroup-leak.patch \
"

FILES:${PN} += "lxc/bin"
FILES:${PN} += "lxc/lxc-conf"
FILES:${PN} += "/vendor_early_services/vendor/vm-system"

EXTRA_OEMESON += "--default-library=static -Dc_link_args=-static"
EXTRA_OEMESON += "-Ddata-path=/vendor_early_services/vendor/vm-system/lxc/lxc-conf"
EXTRA_OEMESON += "-Druntime-path=/vendor_early_services/run/lxc/run"
EXTRA_OEMESON += "-Drootfs-mount-path=/vendor_early_services/vendor/vm-system"
CFLAGS:append = " -Wno-incompatible-pointer-types -Wno-stringop-overread"

do_configure:prepend() {
    sed -i 's/dependency(\x27threads\x27)/dependency(\x27threads\x27, static: true)/' ${S}/meson.build
    sed -i 's/dependency(\x27libseccomp\x27, required: false)/dependency(\x27libseccomp\x27, required: false, static: true)/' ${S}/meson.build
    sed -i 's/dependency(\x27libselinux\x27, required: false)/dependency(\x27libselinux\x27, required: false, static: true)/' ${S}/meson.build
    sed -i 's/dependency(\x27libapparmor\x27, required: false)/dependency(\x27libapparmor\x27, required: false, static: true)/' ${S}/meson.build
    sed -i 's/dependency(\x27openssl\x27, required: false)/dependency(\x27openssl\x27, required: false, static: true)/' ${S}/meson.build
    sed -i 's/dependency(\x27libcap\x27, required: false)/dependency(\x27libcap\x27, required: false, static: true)/' ${S}/meson.build
    sed -i 's/liblxc = shared_library/liblxc = static_library/' ${S}/meson.build
    sed -i '/version: liblxc_version/d' ${S}/meson.build
}

do_install:append() {
    for file in ${D}/usr/bin/*; do
        if [ -f "$file" ] && [[ $(basename "$file") == lxc* ]]; then
            file_output=$(file "$file")
            if [[ "$file_output" == *"ELF"* ]]; then
                ${STRIP} --strip-debug "$file"
            fi
        fi
    done

    install -d ${D}/lxc/lxc-conf/lv
    install -d ${D}/etc/selinux/mls/contexts/files
    install -d ${D}/etc/selinux/targeted/contexts/files
    mv ${D}/usr/bin ${D}/lxc
    cp ${WORKDIR}/config ${D}/lxc/lxc-conf/lv
    install -m 644 ${WORKDIR}/seccomp.conf ${D}/lxc/lxc-conf/lv
    install -m 755 ${WORKDIR}/autodev.sh ${D}/lxc/lxc-conf
    install -m 644 ${WORKDIR}/file_contexts ${D}/etc/selinux/mls/contexts/files
    install -m 644 ${WORKDIR}/file_contexts ${D}/etc/selinux/targeted/contexts/files
    install -m 644 ${WORKDIR}/dbus_contexts ${D}/etc/selinux/targeted/contexts

    # getenforce can't work if no /etc/selinux/config file
    touch ${D}/etc/selinux/config
}

RM_WORK_EXCLUDE += "${PN}"

