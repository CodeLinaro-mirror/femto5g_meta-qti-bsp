# The packaged Clang distribution intentionally contains host x86/x86_64
# runtime objects and static archives in addition to AArch64 runtimes.


INSANE_SKIP:${PN} += "libdir arch staticdev dev-so file-rdeps"
