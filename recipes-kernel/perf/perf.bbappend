PACKAGECONFIG[libtraceevent] = ",NO_LIBTRACEEVENT=1,libtraceevent"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = "file://0001-libbpf-increase-probe_name-buffer-size-to-avoid-form.patch"

PERF_SRC:append = " include/uapi/asm-generic"

addtask do_fetch before do_configure
addtask do_unpack before do_configure after do_fetch

do_configure:prepend() {
    if patch -p1 --dry-run --silent -d ${S} < ${WORKDIR}/0001-libbpf-increase-probe_name-buffer-size-to-avoid-form.patch >/dev/null 2>&1; then
        bbnote "Applying libbpf snprintf patch for kernel ${PREFERRED_VERSION_linux-msm}"
        cd ${S}
        patch -p1 < ${WORKDIR}/0001-libbpf-increase-probe_name-buffer-size-to-avoid-form.patch || \
            (echo "Patch failed!" && exit 1)
    else
        bbnote "Skipping libbpf snprintf patch (already applied or not applicable for kernel ${PREFERRED_VERSION_linux-msm})"
    fi
}

do_populate_lic[noexec] = "1"
