# Must exist before: useradd -u 1000 -N -g system system
GROUPADD_PARAM:${PN} += " -f -g 1000 system"
