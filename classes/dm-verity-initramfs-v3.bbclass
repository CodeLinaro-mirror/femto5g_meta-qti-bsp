#Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
#SPDX-License-Identifier: BSD-3-Clause-Clear

inherit dm-verity-none

# Default verity signing keys to kernel work-shared directory.
# Machines using prebuilt kernels can override this in their machine conf.
VERITY_KEYS_DIR ?= "${STAGING_KERNEL_BUILDDIR}/certs"

# Re-sign the image when the key or cert changes
do_makesystem[file-checksums] += "${VERITY_KEYS_DIR}/verity_key.pem:True ${VERITY_KEYS_DIR}/verity_cert.pem:True"
do_makesystem_sqsh[file-checksums] += "${VERITY_KEYS_DIR}/verity_key.pem:True ${VERITY_KEYS_DIR}/verity_cert.pem:True"
