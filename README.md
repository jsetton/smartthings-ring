# SmartThings Ring Devices

## Overview
Get motion/doorbell events and control lights/siren on Ring devices via SmartThings.

## Installation Instructions:

1. Create an account and/or log into the [SmartThings Developers Web IDE](https://graph.api.smartthings.com/)
1. Click on "My Device Handlers" from the navigation menu.
1. Click on "Settings" from the menu and add my GitHub Repository to your account
    * Owner: jsetton
    * Name: smartthings-ring
    * Branch: master
1. Click on "Update From Repo" from the menu
1. Select "smartthings-ring (master)" from the list
1. Select all of the Device Handlers
1. Check the "Publish" check box and click "Execute Update"
1. Click on "My SmartApps" from the navigation menu.
1. Repeat the same steps to add SmartApp.
1. Once done, go to your SmartThings app, add the Ring Device Manager SmartApp and follow instructions.

### Notes:

* Each device tiles order is configurable, if applicable, in the device settings.
* Battery level will be displayed on supported devices.
* The motion alerts can be delayed up to a minute (ST scheduling limitation) and are only retrieved when enabled on the Ring device.
