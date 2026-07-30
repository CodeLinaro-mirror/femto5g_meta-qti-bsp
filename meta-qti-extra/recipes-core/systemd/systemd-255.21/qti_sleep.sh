#!/bin/sh
# Copyright (c) 2020, The Linux Foundation. All rights reserved.

# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are
# met:
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above
#       copyright notice, this list of conditions and the following
#       disclaimer in the documentation and/or other materials provided
#       with the distribution.
#     * Neither the name of The Linux Foundation nor the names of its
#       contributors may be used to endorse or promote products derived
#       from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
# ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
# BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
# BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
# WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
# OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
# IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

mode_file_path="/var/usb"
usb_mode_file="usb_mode.txt"
usb_dev_path="/sys/devices/platform/soc"

usb_xhci_file="usb_xhci.txt"
usb_xhci_dev_path="/sys/bus/platform/drivers/xhci-hcd/"

case $1/$2 in
  pre/*)
    echo "Entering into $2..."

    systemctl stop loc_launcher.service
    systemctl stop location_hal_daemon.service
    systemctl stop audiod.service
    if [ $2 = "hibernate" ]; then
        echo 0 > /sys/kernel/boot_adsp/boot
    fi

    # disable wlan interfaces
    echo "Disable WLAN interfaces"
    for iface in `iw dev | grep Interface | awk '{print $2}'`
    do
        ifconfig $iface down
    done
    sleep 1

    # disable BT as hsuart could block suspend
    systemctl stop synergy.service

    # save all usb mode and xhci instances to file
    if [ ! -d "$mode_file_path" ]; then
        mkdir -p $mode_file_path
        touch $mode_file_path/$usb_mode_file
        touch $mode_file_path/$usb_xhci_file
    else
        if [ ! -f "$mode_file_path/$usb_mode_file" ]; then
            touch $mode_file_path/$usb_mode_file
        else
            sed -i '1,$d' $mode_file_path/$usb_mode_file
        fi
        if [ ! -f "$mode_file_path/$usb_xhci_file" ]; then
            touch $mode_file_path/$usb_xhci_file
        else
            sed -i '1,$d' $mode_file_path/$usb_xhci_file
        fi
    fi

    # Unbind the xhci for the usb qcom controller
    echo "Destroy USB host instances"
    for dev in `ls $usb_xhci_dev_path`
    do
        echo $dev >> "$mode_file_path/$usb_xhci_file"
        echo $dev > $usb_xhci_dev_path/unbind
    done

    echo "Destroy USB device instances"
    # Disable the ssusb and hsusb for the msm usb controllers
    for dev in `ls $usb_dev_path | grep 'susb$'`
    do
        usb_mode=`cat $usb_dev_path/$dev/mode`
        echo $dev=$usb_mode >> "$mode_file_path/$usb_mode_file"
        echo none > $usb_dev_path/$dev/mode
    done

    # Add delay to allow usb instance tear down for msm usb controllers
    sleep 2
    ;;
  post/*)
    echo "Exiting from $2..."

    echo "Enable USB device instances"
    if [ ! -f "$mode_file_path/$usb_mode_file" ]; then
        echo "USB mode recover failed for $usb_mode_file dose not exist."
    else
        for line in `cat $mode_file_path/$usb_mode_file`
        do
            dev=`echo $line | awk -F '[=]' '{print $1}'`
            usb_mode=`echo $line | awk -F '[=]' '{print $2}'`
            echo $usb_mode > $usb_dev_path/$dev/mode
        done
    fi

    echo "Enable USB Host insances"
   if [ ! -f "$mode_file_path/$usb_xhci_file" ]; then
        echo "USB xhci recover failed for $usb_xhci_file dose not exist."
    else
        for line in `cat $mode_file_path/$usb_xhci_file`
        do
            usb_xhci=`echo $line`
            echo $usb_xhci > $usb_xhci_dev_path/bind
        done
    fi

    systemctl restart synergy.service

    # Enable primary interface wlan0 to restore wlan, other interfaces
    # shall be enabled on demand.
    echo "Enable WLAN interfaces"
    ifconfig wlan0 up

    if [ $2 = "hibernate" ]; then
        echo 1 > /sys/kernel/boot_adsp/boot
    fi
    systemctl restart loc_launcher.service
    systemctl restart location_hal_daemon.service
    systemctl restart audiod.service
    ;;
esac
