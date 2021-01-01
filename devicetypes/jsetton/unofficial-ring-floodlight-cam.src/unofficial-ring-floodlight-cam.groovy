/**
 *  Unofficial Ring Floodlight Cam
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
    name: "Unofficial Ring Floodlight Cam",
    namespace: "jsetton",
    author: "Jeremy Setton",
    ocfDeviceType: "oic.d.camera"
  ) {
    capability "Alarm"
    capability "Health Check"
    capability "Motion Sensor"
    capability "Refresh"
    capability "Switch"
    capability "Sensor"

    command "lightOff"
    command "lightOn"
    command "sirenOff"
    command "sirenOn"
    command "sirenConfirm"
    command "updateDeviceStatus", ["string"]
    command "updateHealthStatus", ["string"]

    attribute "firstStatus",  "string"
    attribute "secondStatus", "string"
    attribute "thirdStatus",  "string"
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
    input "thirdCapability", "enum",
      title: "Third Capability",
      options: capabilityOptions.collect { it.name },
      defaultValue: capabilityMap.findResult { it.name == "thirdCapability" ? it.defaultValue : null },
      required: false
  }

  tiles(scale: 2) {
    multiAttributeTile(name:"firstTile", type: "generic", width: 6, height: 4) {
      tileAttribute ("device.firstStatus", key: "PRIMARY_CONTROL") {
        attributeState "motionInactive",
          label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
        attributeState "motionActive",
          label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
        attributeState "switchOff",
          label:'off', action:"lightOn", icon:"st.Lighting.light15", backgroundColor:"#ffffff", nextState:"switchTurningOn"
        attributeState "switchOn",
          label:'on', action:"lightOff", icon:"st.Lighting.light15", backgroundColor:"#00a0dc", nextState:"switchTurningOff"
        attributeState "switchTurningOff",
          label:'turning off', icon:"st.Lighting.light15", backgroundColor:"#ffffff"
        attributeState "switchTurningOn",
          label:'turning on', icon:"st.Lighting.light15", backgroundColor:"#00a0dc"
        attributeState "switchUnavailable",
          label:'unavailable', icon:"st.Lighting.light15", backgroundColor:"#ffffff"
        attributeState "alarmOff",
          label:'off', action:"sirenConfirm", icon:"st.alarm.beep.beep", backgroundColor:"#ffffff"
        attributeState "alarmSiren",
          label:'on', action:'sirenOff', icon:"st.alarm.beep.beep", backgroundColor:"#e86d13", nextState:"alarmTurningOff"
        attributeState "alarmConfirm",
          label:'Are you sure?', action:"sirenOn", icon:"st.alarm.beep.beep", backgroundColor:"#ffffff", nextState:"alarmTurningOn"
        attributeState "alarmTurningOff",
          label:'turning off', icon:"st.alarm.beep.beep", backgroundColor:"#ffffff"
        attributeState "alarmTurningOn",
          label:'turning on', icon:"st.alarm.beep.beep", backgroundColor:"#e86d13"
        attributeState "alarmUnavailable",
          label:'unavailable', icon:"st.alarm.beep.beep", backgroundColor:"#ffffff"
      }
    }

    standardTile("secondTile", "device.secondStatus", decoration: "flat", width: 2, height: 2) {
      state "motionInactive",
        label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
      state "motionActive",
        label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
      state "switchOff",
        label:'off', action:"lightOn", icon:"st.Lighting.light15", backgroundColor:"#ffffff", nextState:"switchTurningOn"
      state "switchOn",
        label:'on', action:"lightOff", icon:"st.Lighting.light15", backgroundColor:"#00a0dc", nextState:"switchTurningOff"
      state "switchTurningOff",
        label:'turning\noff', icon:"st.Lighting.light15", backgroundColor:"#ffffff"
      state "switchTurningOn",
        label:'turning\non', icon:"st.Lighting.light15", backgroundColor:"#00a0dc"
      state "switchUnavailable",
        label:'unavailable', icon:"st.Lighting.light15", backgroundColor:"#ffffff"
      state "alarmOff",
        label:'off', action:"sirenConfirm", icon:"st.alarm.beep.beep", backgroundColor:"#ffffff"
      state "alarmSiren",
        label:'on', action:'sirenOff', icon:"st.alarm.beep.beep", backgroundColor:"#e86d13", nextState:"alarmTurningOff"
      state "alarmConfirm",
        label:'Are you sure?', action:"sirenOn", icon:"st.alarm.beep.beep", backgroundColor:"#ffffff", nextState:"alarmTurningOn"
      state "alarmTurningOff",
        label:'turning\noff', icon:"st.alarm.beep.beep", backgroundColor:"#ffffff"
      state "alarmTurningOn",
        label:'turning\non', icon:"st.alarm.beep.beep", backgroundColor:"#e86d13"
      state "alarmUnavailable",
        label:'unavailable', icon:"st.alarm.beep.beep", backgroundColor:"#ffffff"
    }

    standardTile("thirdTile", "device.thirdStatus", decoration: "flat", width: 2, height: 2) {
      state "motionInactive",
        label:'no motion', icon:"st.motion.motion.inactive", backgroundColor:"#ffffff"
      state "motionActive",
        label:'motion', icon:"st.motion.motion.active", backgroundColor:"#00a0dc"
      state "switchOff",
        label:'off', action:"lightOn", icon:"st.Lighting.light15", backgroundColor:"#ffffff", nextState:"switchTurningOn"
      state "switchOn",
        label:'on', action:"lightOff", icon:"st.Lighting.light15", backgroundColor:"#00a0dc", nextState:"switchTurningOff"
      state "switchTurningOff",
        label:'turning\noff', icon:"st.Lighting.light15", backgroundColor:"#ffffff"
      state "switchTurningOn",
        label:'turning\non', icon:"st.Lighting.light15", backgroundColor:"#00a0dc"
      state "switchUnavailable",
        label:'unavailable', icon:"st.Lighting.light15", backgroundColor:"#ffffff"
      state "alarmOff",
        label:'off', action:"sirenConfirm", icon:"st.alarm.beep.beep", backgroundColor:"#ffffff"
      state "alarmSiren",
        label:'on', action:'sirenOff', icon:"st.alarm.beep.beep", backgroundColor:"#e86d13", nextState:"alarmTurningOff"
      state "alarmConfirm",
        label:'Are you sure?', action:"sirenOn", icon:"st.alarm.beep.beep", backgroundColor:"#ffffff", nextState:"alarmTurningOn"
      state "alarmTurningOff",
        label:'turning\noff', icon:"st.alarm.beep.beep", backgroundColor:"#ffffff"
      state "alarmTurningOn",
        label:'turning\non', icon:"st.alarm.beep.beep", backgroundColor:"#e86d13"
      state "alarmUnavailable",
        label:'unavailable', icon:"st.alarm.beep.beep", backgroundColor:"#ffffff"
    }

    standardTile("refresh", "device.switch", decoration: "flat", width: 2, height: 2) {
      state "refresh",
        action: "refresh.refresh", icon: "st.secondary.refresh"
    }

    main "firstTile"
    details(["firstTile", "secondTile", "thirdTile", "refresh"])
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

def off() {
  log.debug "Executing 'off'"
  lightOff()
  sirenOff()
}

def on() {
  log.debug "Executing 'on'"
  lightOn()
}

def strobe() {
  log.debug "Executing 'strobe'"
  lightOn()
}

def siren() {
  log.debug "Executing 'siren'"
  sirenOn()
}

def both() {
  log.debug "Executing 'both'"
  lightOn()
  sirenOn()
}

def lightOff() {
  log.debug "Executing 'lightOff'"
  parent?.sendDeviceCommand(device, "floodlight_light_off")
}

def lightOn() {
  log.debug "Executing 'lightOn'"
  parent?.sendDeviceCommand(device, "floodlight_light_on")
}

def sirenOff() {
  log.debug "Executing 'sirenOff'"
  parent?.sendDeviceCommand(device, "siren_off")
}

def sirenOn() {
  log.debug "Executing 'sirenOn'"
  parent?.sendDeviceCommand(device, "siren_on")
}

def sirenConfirm() {
  log.debug "Executing 'sirenConfirm'"
  sendCustomAttributeEvent(name: "alarm", value: "confirm", displayed: false)
  state.lastSirenConfirm = now()
  // reset confirmation if not acknowledged within 10 seconds
  runIn(10, sirenConfirmReset)
}

def sirenConfirmReset() {
  log.debug "Executing 'sirenConfirmReset'"
  if (device.currentValue("alarm") == "confirm")
    sendCustomAttributeEvent(name: "alarm", value: "off", displayed: false)
}

def updateDeviceStatus(properties) {
  log.debug "Executing 'updateDeviceStatus' [properties: '${properties}']"
  def status = [
    light: properties.led_status?: "unavailable",
    siren: properties.siren_status ? properties.siren_status.seconds_remaining?.toInteger() > 0 ? "siren" :
      isSirenConfirm ? "confirm" : "off" : "unavailable"
  ]
  // light
  sendCustomAttributeEvent(name: "switch", value: status.light)
  // siren
  sendCustomAttributeEvent(name: "alarm", value: status.siren, displayed: device.currentValue("alarm") != "confirm")
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
private isSirenConfirm() {
  return device.currentValue("alarm") == "confirm" && state.lastSirenConfirm > now() - 10000
}

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
    case "alarm":   return "siren is ${value == "siren" ? "on" : value}"
    case "motion":  return "motion ${value == "active" ? "detected" : "cleared"}"
    case "switch":  return "light is ${value}"
    default:        return "${name} is ${value}"
  }
}

private static getCapabilityMap() {
  [
    ["name": "firstCapability",  "attribute": "firstStatus",  "defaultValue": "Motion Detector"],
    ["name": "secondCapability", "attribute": "secondStatus", "defaultValue": "Light"          ],
    ["name": "thirdCapability",  "attribute": "thirdStatus",  "defaultValue": "Siren"          ]
  ]
}

private static getCapabilityOptions() {
  [
    ["name": "Motion Detector", "capability": "motion", "defaultValue": "inactive"   ],
    ["name": "Light",           "capability": "switch", "defaultValue": "unavailable"],
    ["name": "Siren",           "capability": "alarm",  "defaultValue": "unavailable"]
  ]
}
