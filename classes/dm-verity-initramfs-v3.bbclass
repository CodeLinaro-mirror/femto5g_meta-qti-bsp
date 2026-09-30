#Copyright (c) 2024 Qualcomm Innovation Center, Inc. All rights reserved.
#SPDX-License-Identifier: BSD-3-Clause-Clear

inherit dm-verity-none

# Use the verity signing key/cert directly from the prebuilt kernel output
# instead of the kernel work-shared folder, so image signing does not depend
# on kernel tasks.
VERITY_KEYS_DIR = "${KERNEL_PREBUILT_PATH}/${KERNEL_TYPE}"

# Re-sign the image when the key or cert changes
do_makesystem[file-checksums] += "${VERITY_KEYS_DIR}/verity_key.pem:True ${VERITY_KEYS_DIR}/verity_cert.pem:True"
do_makesystem_sqsh[file-checksums] += "${VERITY_KEYS_DIR}/verity_key.pem:True ${VERITY_KEYS_DIR}/verity_cert.pem:True"
