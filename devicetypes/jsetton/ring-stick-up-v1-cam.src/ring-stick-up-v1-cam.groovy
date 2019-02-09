/**
 *  Ring Stick Up V1 Cam
 *
 *  Author:
 *      Jeremy Setton (jsetton)
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
  definition (
    name: "Ring Stick Up V1 Cam",
    namespace: "jsetton",
    author: "Jeremy Setton"
  ) {
    capability "Battery"
    capability "Health Check"
    capability "Motion Sensor"
    capability "Refresh"
    capability "Sensor"

    command "updateDeviceStatus", ["string"]
    command "updateHealthStatus", ["string"]
  }

  simulator {}

  preferences {}

  tiles(scale: 2) {
    multiAttributeTile(name:"motion", type: "generic", width: 6, height: 4) {
      tileAttribute ("device.motion", key: "PRIMARY_CONTROL") {
        attributeState "inactive",
          label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
        attributeState "active",
          label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
      }
      tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
        attributeState "battery",
          label:'Battery: ${currentValue}%', unit:""
        attributeState "999",
          label:''  // unavailable
      }
    }

    standardTile("refresh", "device.switch", decoration: "flat", width: 2, height: 2) {
      state "refresh",
        action: "refresh.refresh", icon: "st.secondary.refresh"
    }

    main "motion"
    details(["motion", "refresh"])
  }
}

def installed() {
  log.debug "Executing 'installed'"
  initialize()
}

def updated() {
  log.debug "Executing 'updated'"
  initialize()
}

def uninstalled() {
  log.debug "Executing 'uninstalled'"
  parent?.uninstallDevice(device)
}

def initialize() {
  log.debug "Executing 'initialize'"
  updateHealthStatus("online")
  sendEvent(
    name: "DeviceWatch-Enroll",
    value: [protocol: "cloud", scheme:"untracked"].encodeAsJson(),
    displayed: false
  )
}

def parse(String description) {
  log.debug "Parsing '${description}'"
}

def refresh() {
  log.debug "Executing 'refresh'"
  parent?.refresh()
}

// Command handlers
def active() {
  log.debug "Executing 'active'"
  sendEvent(name: "motion", value: "active", descriptionText: "motion detected")
  // reset motion event after 30 seconds
  runIn(30, inactive)
}

def inactive() {
  log.debug "Executing 'inactive'"
  sendEvent(name: "motion", value: "inactive", descriptionText: "motion cleared")
}

def updateDeviceStatus(properties) {
  def status = [
    battery: properties.battery_life?: properties.battery_life_2?: 999,
  ]
  // battery
  sendEvent(name: "battery", value: status.battery, unit: "%", displayed: status.battery != 999)
}

def updateHealthStatus(status) {
  log.debug "Executing 'updateHealthStatus' [status: '${status}']"
  if (status in ["online", "offline"]) {
    sendEvent(name: "healthStatus", value: status)
    sendEvent(name: "DeviceWatch-DeviceStatus", value: status)
  }
}
