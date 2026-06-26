# BBMASK drops some recipe that checksum file not found
BBMASK:append = " ${MISSING_RECIPE_SRCLIST}"
MISSING_RECIPE_SRCLIST:append:quin-gvm-gen4 = " ${MISSING_RECIPE_SRCLIST_BASELINE_LV050}"
MISSING_RECIPE_SRCLIST:append:qtiquingvm8295 = " ${MISSING_RECIPE_SRCLIST_BASELINE_LV050}"
MISSING_RECIPE_SRCLIST:append:quin-gvm-gen4-5 = " ${MISSING_RECIPE_SRCLIST_BASELINE_LV051}"
MISSING_RECIPE_SRCLIST:append:gvm-gen4-5 = " ${MISSING_RECIPE_SRCLIST_BASELINE_LV051}"
MISSING_RECIPE_SRCLIST:append:gvm-gen5 = " ${MISSING_RECIPE_SRCLIST_BASELINE_LV052}"

# unsed bbappend
BBMASK:append = " \
meta-qti-bsp/meta-qti-extra/recipes-devtools/gtest/googletest_git.bbappend \
meta-qti-bsp/meta-qti-base/recipes-multimedia/ffmpeg/ffmpeg_5.0.%.bbappend \
meta-qti-bsp/meta-qti-base/recipes-graphics/wayland/weston_10.0.%.bbappend \
"

# Missing recipes globally
MISSING_RECIPE_SRCLIST += "\
meta-qti-bsp/meta-qti-base/recipes-bsp/abctl/abctl_1.0.bb \
meta-qti-bsp/meta-qti-base/recipes-bsp/libgpt/libgpt_1.0.bb \
meta-qti-bsp/meta-qti-extra/recipes-test/virt-spmi-ut/virt-spmi-ut.bb \
meta-qti-bsp/meta-qti-extra/recipes-test/virt-reg-ut/virt-reg-ut.bb \
meta-qti-bsp/meta-qti-extra/recipes-test/virtio-i2c-test/virtio-i2c-test.bb \
meta-qti-bsp/meta-qti-extra/recipes-test/virt-fastrpc-ut/virt-fastrpc-ut_git.bb \
meta-qti-bsp/meta-qti-extra/recipes-test/virt-clk-ut/virt-clk-ut.bb \
meta-qti-bsp/meta-qti-extra/recipes-test/khab-test/khab-unit-test.bb \
meta-qti-bsp/meta-qti-base/recipes-virt/vhost-user-scmi/vhost-user-scmi_git.bb \
meta-qti-bsp/meta-qti-base/recipes-virt/vhost-user-q/vhost-user-q_git.bb \
meta-qti-bsp/meta-qti-base/recipes-virt/vhost-user-lib/vhost-user-lib_git.bb \
meta-qti-bsp/meta-qti-base/recipes-virt/vhost-device/vhost-device-i2c_0.1.0.bb \
meta-qti-bsp/meta-qti-base/recipes-virt/safelinux-system-cfg/safelinux-system-cfg_git.bb \
meta-qti-bsp/meta-qti-base/recipes-virt/qcrosvm/qcrosvm_git.bb \
meta-qti-bsp/meta-qti-base/recipes-virt/msmhab/msmhab_git.bb \
meta-qti-bsp/meta-qti-base/recipes-virt/hyp-udmabuf/hyp-udmabuf_git.bb \
meta-qti-bsp/meta-qti-base/recipes-virt/gunyah-drivers/gunyah-drivers_git.bb \
meta-qti-bsp/meta-qti-base/recipes-security/security-modules/safelinux-sec-modules_git.bb \
meta-qti-bsp/meta-qti-base/recipes-safelinux-dbg-modules/safelinux-dbg-modules/safelinux-dbg-modules_git.bb \
meta-qti-bsp/meta-qti-base/recipes-multimedia/virtio-video/virtio-video-header_git.bb \
meta-qti-bsp/meta-qti-base/recipes-multimedia/media/media_0.1.bb \
meta-qti-bsp/meta-qti-base/recipes-kiumd/safelinux-cfg-modules/safelinux-cfg-modules_git.bb \
meta-qti-bsp/meta-qti-base/recipes-kiumd/kiumd-headers/kiumd-headers_git.bb \
meta-qti-bsp/meta-qti-base/recipes-kiumd/gvm-net-config/gvm-net-config_git.bb \
meta-qti-bsp/meta-qti-base/recipes-kiumd/dspfirmware-mount/dspfirmware-mount_git.bb \
meta-qti-bsp/meta-qti-base/recipes-kernel/oot-dtbo/oot-dtbo.bb \
meta-qti-bsp/meta-qti-base/recipes-kernel/edk2/edk2_git.bb \
meta-qti-bsp/meta-qti-base/recipes-data/emac-dwc-eqos/emac-dwc-eqos_git.bb \
meta-qti-bsp/meta-qti-base/recipes-data/aurix-can/aurix-can_git.bb \
meta-qti-bsp/meta-qti-base/recipes-core/reboot-daemon/reboot-daemon_2.0.bb \
meta-qti-bsp/meta-qti-base/recipes-core/bootkpi-logging/bootkpi-logging.bb \
meta-qti-bsp/meta-qti-base/recipes-camera/cameradlkm/cameradlkm_0.2.bb \
meta-qti-bsp/meta-qti-base/recipes-audio/audiolite-dlkm/audiolite-dlkm_git.bb \
meta-qti-bsp/meta-qti-base/recipes-audio/audiolite-dlkm/audiolite-devicetree_git.bb \
meta-qti-bsp/meta-qti-base/recipes-kernel/linux-msm/linux-msm_5.15.bb \
meta-qti-bsp/meta-qti-extra/recipes-location/ \
"

# Missing recipes in baseline LV.AU.0.5.0 (target for quin-gvm-gen4 & qtiquingvm8295)
MISSING_RECIPE_SRCLIST_BASELINE_LV050 += "\
meta-qti-bsp/meta-qti-base/recipes-security/securemsm-devicetree/securemsm-devicetree.bb \
meta-qti-bsp/meta-qti-base/recipes-kernel/linux-msm/linux-msm-headers_6.12.bb \
meta-qti-bsp/meta-qti-base/recipes-kernel/linux-msm/qcom-dtc-native.bb \
meta-qti-bsp/meta-qti-base/recipes-graphics/graphics-hgsl/graphics-hgsldlkm_git.bb \
meta-qti-bsp/meta-qti-base/recipes-wlan/wlan-devicetree \
meta-qti-bsp/meta-qti-base/recipes-data/dataeth-dlkm/dataeth-dlkm_git.bb \
meta-qti-bsp/meta-qti-extra/recipes-security/selinux-policy/selinux-policy.bb \
meta-qti-bsp/meta-qti-base/recipes-display/display-la/display-la-headers.bb \
meta-qti-bsp/meta-qti-base/recipes-display/display-devicetree/display-devicetree_1.0.bb \
meta-qti-bsp/meta-qti-base/recipes-graphics/display-intf/display-intf-headers.bb \
meta-qti-bsp/meta-qti-base/recipes-audio/msm-virtio-snd/msm-virtio-snd.bb \
meta-qti-bsp/meta-qti-base/recipes-display/displaydlkm/msm-ext-display_git.bb \
meta-qti-bsp/meta-qti-base/recipes-multimedia/virtio-video/virtio-video_git.bb \
"

# Missing recipes in baseline LV.AU.0.5.1 (target for quin-gvm-gen4-5 & gvm-gen4-5)
MISSING_RECIPE_SRCLIST_BASELINE_LV051 += "\
meta-qti-bsp/meta-qti-extra/recipes-containers/softwarecontainer/softwarecontainer_git.bb \
meta-qti-bsp/meta-qti-base/recipes-camera/cameradlkm/cameradlkm_0.1.bb \
meta-qti-bsp/meta-qti-base/recipes-kernel/linux-msm/linux-msm_6.1.bb \
meta-qti-bsp/meta-qti-base/recipes-data/avb-utils/avb-utils_1.0.bb \
meta-qti-bsp/meta-qti-base/recipes-display/display-la/display-la-headers.bb \
meta-qti-bsp/meta-qti-base/recipes-display/display-devicetree/display-devicetree_1.0.bb \
meta-qti-bsp/meta-qti-base/recipes-graphics/display-intf/display-intf-headers.bb \
meta-qti-bsp/meta-qti-base/recipes-audio/msm-virtio-snd/msm-virtio-snd.bb \
meta-qti-bsp/meta-qti-base/recipes-display/displaydlkm/msm-ext-display_git.bb \
meta-qti-bsp/meta-qti-base/recipes-multimedia/virtio-video/virtio-video_git.bb \
"

# Missing recipes in baseline LV.AU.0.5.2 (target for gvm-gen5)
MISSING_RECIPE_SRCLIST_BASELINE_LV052 += "\
meta-qti-bsp/meta-qti-extra/recipes-containers/softwarecontainer/softwarecontainer_git.bb \
meta-qti-bsp/meta-qti-base/recipes-camera/cameradlkm/cameradlkm_0.1.bb \
meta-qti-bsp/meta-qti-base/recipes-wlan \
meta-qti-bsp/meta-qti-base/recipes-data/dataeth-dlkm/dataeth-dlkm_git.bb \
meta-qti-bsp/meta-qti-base/recipes-data/eavb/eavb-fe-virtio_git.bb \
meta-qti-bsp/meta-qti-base/recipes-connectivity/bt-dlkm/bt-dlkm_git.bb \
meta-qti-bsp/meta-qti-extra/recipes-security/selinux-policy/selinux-policy.bb \
meta-qti-display \
meta-qti-display-internal \
"
