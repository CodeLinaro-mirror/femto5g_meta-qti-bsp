do_populate_kernel_header_artifacts() {
    mkdir -p ${B}/headers

    if [ ! -d "${UNPACKDIR}/usr" ]; then
        bbfatal "Expected unpacked UAPI headers at ${UNPACKDIR}/usr, but the directory is missing"
    fi

    cp -a ${UNPACKDIR}/usr "${B}/headers/"
}