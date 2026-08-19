SYSTEMD_SERVICE:${PN}:gvm-gen4-5-virtio = "weston.service"

do_install:append:gvm-gen4-5-virtio() {
    cat > ${D}${systemd_system_unitdir}/weston.service <<'EOF'
[Unit]
Description=Weston Wayland compositor on virtio-gpu
Requires=systemd-user-sessions.service
After=dbus.socket systemd-udevd.service systemd-user-sessions.service

[Service]
Type=simple
User=root
Environment="XDG_RUNTIME_DIR=/run/weston"
Environment="XDG_CACHE_HOME=/tmp/mesa-cache"
Environment="LIBSEAT_BACKEND=noop"
ExecStartPre=/bin/mkdir -p /run/weston
ExecStartPre=/bin/chmod 700 /run/weston
ExecStartPre=/bin/mkdir -p /tmp/mesa-cache
ExecStartPre=/bin/sh -c 'i=0; while [ $i -lt 20 ]; do [ -e /dev/dri/card0 ] && exit 0; i=$((i + 1)); sleep 1; done; exit 1'
ExecStart=/usr/bin/weston --backend=drm-backend.so --drm-device=card0 --renderer=gl --continue-without-input --idle-time=0 --socket=wayland-0 --log=/tmp/weston.log
Restart=always
RestartSec=2
StartLimitIntervalSec=100
StartLimitBurst=5

[Install]
WantedBy=multi-user.target
EOF
}
