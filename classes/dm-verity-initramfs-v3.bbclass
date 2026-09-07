#Copyright (c) 2024 Qualcomm Innovation Center, Inc. All rights reserved.
#SPDX-License-Identifier: BSD-3-Clause-Clear

inherit dm-verity-none

# Ensure kernel work-shared artifacts (verity_cert.pem/verity_key.pem) are
# fully staged before image makesystem tasks attempt dm-verity v3 signing.
python () {
    if d.getVarFlag('do_makesystem', 'task') is not None:
        d.appendVarFlag('do_makesystem', 'depends', ' virtual/kernel:do_deploy')
    if d.getVarFlag('do_makesystem_sqsh', 'task') is not None:
        d.appendVarFlag('do_makesystem_sqsh', 'depends', ' virtual/kernel:do_deploy')
}
