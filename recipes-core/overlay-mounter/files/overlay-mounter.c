// Copyright (c) Qualcomm Technologies, Inc. and/or its subsidiaries.
// SPDX-License-Identifier: BSD-3-Clause-Clear
//
// overlay-mounter: mount overlayfs on /data or /etc using a dedicated
// SELinux domain (overlay_mounter_t) so that the stashed credential used
// by the kernel for the overlayfs second-check is not the generic mount_t.

#define _GNU_SOURCE
#include <stdio.h>
#include <string.h>
#include <sys/mount.h>

int main(int argc, char *argv[])
{
    const char *opts;

    if (argc != 2) {
        fprintf(stderr, "Usage: overlay-mounter {data|etc|cache}\n");
        return 1;
    }

    if (strcmp(argv[1], "data") == 0) {
        opts = "lowerdir=/data,upperdir=/overlay/data,"
               "workdir=/overlay/.data-work,"
               "rootcontext=system_u:object_r:data_t:s0";
        if (mount("overlay", "/data", "overlay", 0, opts) != 0) {
            perror("mount /data");
            return 1;
        }
    } else if (strcmp(argv[1], "etc") == 0) {
        opts = "lowerdir=/etc,upperdir=/overlay/etc,"
               "workdir=/overlay/.etc-work,"
               "rootcontext=system_u:object_r:etc_t:s0";
        if (mount("overlay", "/etc", "overlay", 0, opts) != 0) {
            perror("mount /etc");
            return 1;
        }
    } else if (strcmp(argv[1], "cache") == 0) {
        opts = "lowerdir=/cache,upperdir=/overlay/cache,"
               "workdir=/overlay/.cache-work,"
               "rootcontext=system_u:object_r:cache_t:s0";
        if (mount("overlay", "/cache", "overlay", 0, opts) != 0) {
            perror("mount /cache");
            return 1;
        }
    } else {
        fprintf(stderr, "overlay-mounter: unknown target '%s'\n", argv[1]);
        return 1;
    }

    return 0;
}
