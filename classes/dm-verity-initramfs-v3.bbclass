#Copyright (c) 2024 Qualcomm Innovation Center, Inc. All rights reserved.
#SPDX-License-Identifier: BSD-3-Clause-Clear

inherit dm-verity-none

# Ensure kernel work-shared artifacts (verity_cert.pem/verity_key.pem) are
# fully staged before image makesystem tasks attempt dm-verity v3 signing.
python () {
    def _is_true(val):
        return val in ('True', '1', 'yes', 'true')
    use_prebuilts = _is_true(d.getVar('KERNEL_USE_PREBUILTS')) or \
                    _is_true(d.getVar('MACHINE_USES_KERNEL_PREBUILTS'))
    kernel_task = 'do_prebuilt_shared_workdir' if use_prebuilts else 'do_shared_workdir'
    if d.getVarFlag('do_makesystem', 'task') is not None:
        d.appendVarFlag('do_makesystem', 'depends', ' virtual/kernel:' + kernel_task)
    if d.getVarFlag('do_makesystem_sqsh', 'task') is not None:
        d.appendVarFlag('do_makesystem_sqsh', 'depends', ' virtual/kernel:' + kernel_task)
}
