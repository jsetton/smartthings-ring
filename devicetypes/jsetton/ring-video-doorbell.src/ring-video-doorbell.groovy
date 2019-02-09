/**
 *  Ring Video Doorbell
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
    name: "Ring Video Doorbell",
    namespace: "jsetton",
    author: "Jeremy Setton"
  ) {
    capability "Battery"
    capability "Button"
    capability "Health Check"
    capability "Motion Sensor"
    capability "Refresh"
    capability "Sensor"

    command "ding"
    command "updateDeviceStatus", ["string"]
    command "updateHealthStatus", ["string"]

    attribute "doorbell", "string"
    attribute "firstStatus",  "string"
    attribute "secondStatus", "string"
  }

  simulator {}

  preferences {
    input "firstCapability", "enum",
      title: "First Capability",
      options: capabilityOptions.collect { it.name },
      defaultValue: capabilityMap.findResult { it.name == "firstCapability" ? it.defaultValue : null },
      required: false
    input "secondCapability", "enum",
      title: "Second Capability",
      options: capabilityOptions.collect { it.name },
      defaultValue: capabilityMap.findResult { it.name == "secondCapability" ? it.defaultValue : null },
      required: false
  }

  tiles(scale: 2) {
    multiAttributeTile(name:"firstTile", type: "generic", width: 6, height: 4) {
      tileAttribute ("device.firstStatus", key: "PRIMARY_CONTROL") {
        attributeState "motionInactive",
          label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
        attributeState "motionActive",
          label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
        attributeState "doorbellReleased",
          label:'off', icon: "${iconUrl}/doorbell.png", backgroundColor:"#ffffff"
        attributeState "doorbellPushed",
          label:'ding!', icon: "${iconUrl}/doorbell.png", backgroundColor:"#00a0dc"
      }
      tileAttribute ("device.battery", key: "SECONDARY_CONTROL") {
        attributeState "battery",
          label:'Battery: ${currentValue}%', unit:""
        attributeState "999",
          label:''  // unavailable
      }
    }

    standardTile("secondTile", "device.secondStatus", decoration: "flat", width: 2, height: 2) {
      state "motionInactive",
        label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
      state "motionActive",
        label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
      state "doorbellReleased",
        label:'off', icon: "${iconUrl}/doorbell.png", backgroundColor:"#ffffff"
      state "doorbellPushed",
        label:'ding!', icon: "${iconUrl}/doorbell.png", backgroundColor:"#00a0dc"
    }

    standardTile("refresh", "device.switch", decoration: "flat", width: 2, height: 2) {
      state "refresh",
        action: "refresh.refresh", icon: "st.secondary.refresh"
    }

    main "firstTile"
    details(["firstTile", "secondTile", "refresh"])
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
  updateTilePreferences()
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
  sendCustomAttributeEvent(name: "motion", value: "active")
  // reset motion event after 30 seconds
  runIn(30, inactive)
}

def inactive() {
  log.debug "Executing 'inactive'"
  sendCustomAttributeEvent(name: "motion", value: "inactive")
}

def ding() {
  log.debug "Executing 'ding'"
  sendCustomAttributeEvent(name: "button", value: "pushed", displayed: false)
  sendCustomAttributeEvent(name: "doorbell", value: "pushed")
  // reset doorbell event after 5 seconds
  runIn(5, release)
}

def release() {
  log.debug "Executing 'release'"
  sendCustomAttributeEvent(name: "doorbell", value: "released", displayed: false)
}

def updateDeviceStatus(properties) {
  def status = [
    battery: getDataValue("wired") ? 999 : properties.battery_life?: properties.battery_life_2?: 999,
  ]
  // battery
  sendCustomAttributeEvent(name: "battery", value: status.battery, unit: "%", displayed: status.battery != 999)
}

def updateHealthStatus(status) {
  log.debug "Executing 'updateHealthStatus' [status: '${status}']"
  if (status in ["online", "offline"]) {
    sendEvent(name: "healthStatus", value: status)
    sendEvent(name: "DeviceWatch-DeviceStatus", value: status)
  }
}

def updateTilePreferences() {
  log.debug "Executing 'updateTilePreferences'"
  capabilityMap.each {
    def setting = getCapabilitySetting(it.name, it.defaultValue)
    if (setting) {
      sendCustomAttributeEvent(
        name: setting.capability,
        value: device.currentValue(setting.capability)?: setting.defaultValue,
        displayed: false
      )
    }
  }
}

// Private methods
private getCapabilityAttributes(capability) {
  return capabilityMap.findAll { capability == getCapabilitySetting(it.name, it.defaultValue)?.capability }.attribute
}

private getCapabilitySetting(name, fallback) {
  return capabilityOptions.find { it.name == (settings.get(name)?: fallback) }
}

private sendCustomAttributeEvent(properties) {
  if (properties?.name && properties?.value) {
    // send custom attributes event
    def attributes = getCapabilityAttributes(properties.name).eachWithIndex { name, index ->
      sendEvent(properties + [
        name: name,
        value: "${properties.name}${properties.value.capitalize()}",
        displayed: isStateChange(device, properties.name, properties.value.toString()) && index == 0 ?
          properties.displayed : false,
        descriptionText: properties.descriptionText?:
          "${device.displayName} ${getCapabilityDescription(properties.name, properties.value)}"
      ])
    }
    // send capability event
    sendEvent(properties + [
      displayed: !attributes ? properties.displayed : false
    ])
  }
}

// Static values
private static getCapabilityDescription(name, value) {
  switch (name) {
    case "doorbell": return "doorbell ${value == "pushed" ? "rang" : "cleared"}"
    case "motion":   return "motion ${value == "active" ? "detected" : "cleared"}"
    default:         return "${name} is ${value}"
  }
}

private static getCapabilityMap() {
  [
    ["name": "firstCapability",  "attribute": "firstStatus",  "defaultValue": "Motion Detector"],
    ["name": "secondCapability", "attribute": "secondStatus", "defaultValue": "Doorbell"       ]
  ]
}

private static getCapabilityOptions() {
  [
    ["name": "Motion Detector", "capability": "motion",   "defaultValue": "inactive"],
    ["name": "Doorbell",        "capability": "doorbell", "defaultValue": "released"]
  ]
}

private static getIconUrl() {
  "https://raw.githubusercontent.com/jsetton/smartthings-ring/master/icons"
}
