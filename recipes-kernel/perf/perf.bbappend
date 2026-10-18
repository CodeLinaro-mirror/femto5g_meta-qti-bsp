PACKAGECONFIG[libtraceevent] = ",NO_LIBTRACEEVENT=1,libtraceevent"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append = " file://0001-libbpf-increase-probe_name-buffer-size-to-avoid-form.patch"

PERF_SRC:append = " include/uapi/asm-generic"

LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

# The Kalama 6.18 soc-repo exports the kernel sources below common/.
# The OE perf recipe expects the conventional kernel tree layout.
python copy_perf_source_from_kernel() {
    sources = (d.getVar("PERF_SRC") or "").split()
    src_dir = d.getVar("STAGING_KERNEL_DIR")
    if not os.path.exists(oe.path.join(src_dir, "tools", "perf")) and \
       os.path.exists(oe.path.join(src_dir, "common", "tools", "perf")):
        src_dir = oe.path.join(src_dir, "common")
    if not os.path.exists(oe.path.join(src_dir, "tools", "perf")):
        workspace_common = oe.path.join(d.getVar("WORKSPACE"),
                                        "kernel-6.18", "kernel_platform", "common")
        if os.path.exists(oe.path.join(workspace_common, "tools", "perf")):
            src_dir = workspace_common
    dest_dir = d.getVar("S")
    bb.utils.mkdirhier(dest_dir)
    bb.utils.prunedir(dest_dir)
    for source in sources:
        src = oe.path.join(src_dir, source)
        dest = oe.path.join(dest_dir, source)
        if not os.path.exists(src):
            bb.warn("Path does not exist: %s. Maybe PERF_SRC lists more files than this kernel provides." % src)
            continue
        if os.path.isdir(src):
            oe.path.copyhardlinktree(src, dest)
        else:
            bb.utils.mkdirhier(os.path.dirname(dest))
            bb.utils.copyfile(src, dest)
}

addtask do_fetch before do_configure
addtask do_unpack before do_configure after do_fetch

do_configure:prepend() {
    if patch -p1 --dry-run --silent -d ${S} < ${UNPACKDIR}/0001-libbpf-increase-probe_name-buffer-size-to-avoid-form.patch >/dev/null 2>&1; then
        bbnote "Applying libbpf snprintf patch for kernel ${PREFERRED_VERSION_linux-msm}"
        cd ${S}
        patch -p1 < ${UNPACKDIR}/0001-libbpf-increase-probe_name-buffer-size-to-avoid-form.patch || \
            (echo "Patch failed!" && exit 1)
    else
        bbnote "Skipping libbpf snprintf patch (already applied or not applicable for kernel ${PREFERRED_VERSION_linux-msm})"
    fi
}
