/**
 *  Ring Device Manager
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
definition(
  name: "Ring Device Manager",
  namespace: "jsetton",
  author: "Jeremy Setton",
  description: "Manage Ring devices",
  category: "Safety & Security",
  iconUrl: "${iconUrl}/ring.png",
  iconX2Url: "${iconUrl}/ring@2x.png",
  iconX3Url: "${iconUrl}/ring@3x.png"
)

preferences {
  page(name: "prefDevices")
  page(name: "prefCredentials")
}

// Preference pages
def prefDevices() {
  if (!login())
    return prefCredentials()

  def options = deviceList.collect { [(it.id): "${it.label} (${it.model.name})"] }
  if (options) {
    return dynamicPage(
      name: "prefDevices",
      title: "Device List",
      install: true,
      uninstall: true
    ) {
      section("Select which devices to include") {
        input "selectedDevices", "enum",
          required: false,
          multiple: true,
          options: options
      }
    }
  } else {
    return dynamicPage(
      name: "prefDevices",
      title: "Error",
      install: false,
      uninstall: true
    ) {
      section {
        paragraph "Please check your Ring account has linked devices.",
          title: "No Device Found",
          image: "${iconUrl}/caution.png"
      }
    }
  }
}

def prefCredentials() {
  def isInstalled = username && password
  return dynamicPage(
    name: "prefCredentials",
    title: "Connect to Ring",
    nextPage: "prefDevices",
    install: false,
    uninstall: isInstalled
  ) {
    if (isInstalled) {
      section {
        paragraph "Please check the credentials you entered below.",
          title: "Login Failed",
          image: "${iconUrl}/caution.png"
      }
    }

    section("Login Credentials") {
      input "username", "email",
        title: "Email",
        description: "Ring account email",
        required: true
      input "password", "password",
        title: "Password",
        description: "Ring account password",
        required: true
    }
  }
}

def installed() {
  log.debug "Installed with settings: ${settings}"
  initialize()
}

def updated() {
  log.debug "Updated with settings: ${settings}"
  unschedule()
  initialize()
}

def initialize() {
  log.debug "Initializing..."
  // create selected child devices if not already initialized
  deviceList.findAll { selectedDevices.contains(it.id) && !getChildDevice(it.id) }.each { device ->
    try {
      addChildDevice(device.model.handler, device.id, null, [
        name: device.model.name,
        label: device.label,
        data: device.model.data,
        completedSetup: true
      ])
    } catch (e) {
      log.error "addChildDevice Error: $e"
    }
  }
  // remove unselected child devices
  allChildDevices.findAll { !selectedDevices.contains(it.deviceNetworkId) }.each { child ->
    deleteChildDevice(child.deviceNetworkId)
  }
  // schedule refresh to run every minute
  runEvery1Minute(refresh)
  // trigger refresh
  refresh()
}

// Refresh data
def refresh() {
  if (allChildDevices.size() > 0) {
    log.info "Refreshing data..."
    // update active alerts
    updateActiveAlerts()
    // update all devices status
    updateDeviceStatus()
  }
}

def sendDeviceCommand(childDevice, command) {
  log.info "sendDeviceCommand: ${childDevice.deviceNetworkId} ${command}"
  apiRequest("${apiDoorbotsEndpoint}/${childDevice.deviceNetworkId}/${command}", "PUT")
  runIn(2, refresh)
}

def uninstallDevice(childDevice) {
  log.info "uninstallDevice: ${childDevice.deviceNetworkId}"
  // remove uninstalled child device from selected devices setting
  if (settings.selectedDevices) {
    app.updateSetting('selectedDevices',
      settings.selectedDevices.findAll { it != childDevice.deviceNetworkId })
  }
}

// Private methods
private getDeviceList() {
  def deviceList = []
  apiRequest(apiDevicesEndpoint, "GET") { resp ->
    if (resp?.status == 200) {
      resp.data.values().flatten().each { device ->
        def deviceModel = getDeviceModel(device.kind, device.ring_cam_setup_flow)
        if (deviceModel) {
          deviceList.add([
            id: device.id.toString(),
            label: device.description,
            model: deviceModel
          ])
        } else {
          log.debug "Device model not defined: " +
            "id=${device.id} kind=${device.kind} flow=${device.ring_cam_setup_flow}"
        }
      }
    }
  }
  log.debug "Device list: ${deviceList}"
  return deviceList
}

private updateActiveAlerts() {
  apiRequest(apiActiveAlertsEndpoint, "GET") { resp ->
    if (resp?.status == 200) {
      resp.data.each { alert ->
        log.debug "Received Alert: ${alert}"
        def child = getChildDevice(alert.doorbot_id.toString())
        if (child) {
          switch (alert.kind) {
            case "ding":
              if (child.capabilities.find { it.name == "Button" }) {
                log.debug "Creating ding event: ${child.deviceNetworkId}"
                child.ding()
              }
              break
            case "motion":
              if (child.capabilities.find { it.name == "Motion Sensor" }) {
                log.debug "Creating motion event: ${child.deviceNetworkId}"
                child.active()
              }
              break
          }
        }
      }
    }
  }
}

private updateDeviceStatus() {
  apiRequest(apiDevicesEndpoint, "GET") { resp ->
    if (resp?.status == 200) {
      log.debug "Got data: ${resp.data}"
      allChildDevices.each { child ->
        def online = resp.data.values().flatten().any { device ->
          if (child.deviceNetworkId == device.id.toString()) {
            log.debug "Updating device status: ${child.deviceNetworkId}"
            child.updateDeviceStatus(device)
            return true
          }
        }
        log.debug "Updating health status: ${child.deviceNetworkId}"
        child.updateHealthStatus(online ? "online" : "offline")
      }
    }
  }
}

private login() {
  if (!settings.username || !settings.password)
    return false

  if (!state.session?.accessToken || state.session?.expiration <= now()) {
    log.debug "Getting oauth token..."
    def params = [
      uri: oauthUri,
      path: oauthTokenEndpoint,
      headers: headers,
      requestContentType: "application/json",
      body: (!state.session?.accessToken || !state.session?.refreshToken) ? [
        // New request
        "client_id": clientId,
        "grant_type": "password",
        "scope": "client",
        "username": settings.username,
        "password": settings.password
      ] : [
        // Refresh request
        "client_id": clientId,
        "grant_type": "refresh_token",
        "scope": "client",
        "refresh_token": state.session.refreshToken
      ]
    ]

    httpRequest(params, "POST") { resp ->
      state.session = resp?.status != 200 ? null : [
        accessToken: resp.data.access_token,
        refreshToken: resp.data.refresh_token,
        expiration: now() + resp.data.expires_in.toInteger() * 1000
      ]
    }
  }

  return state.session?.accessToken
}

private apiRequest(endpoint, method = "GET", callback = {}) {
  def params = [
    uri: apiUri,
    path: endpoint,
    headers: headers + [
      "Authorization": "Bearer ${state.session?.accessToken}"
    ],
    requestContentType: "application/json",
    query: [
      "api_version": apiVersion
    ]
  ]

  if (login()) {
    log.debug "API ${method.toUpperCase()} Request: ${params}"
    httpRequest(params, method) { resp -> callback(resp) }
  }
}

private httpRequest(params = [], method = "GET", callback = {}) {
  try {
    "http${method.toLowerCase().capitalize()}"(params) { resp ->
      log.debug "${method.toUpperCase()} Response code: ${resp.status}"
      log.debug "Response data: ${resp.data}"
      callback(resp)
    }
  } catch (e) {
    log.error "HTTP ${method.toUpperCase()} Exception: ${e}"
    callback(null)
  }
}

// Static values
private static getApiUri() {
  "https://api.ring.com"
}

private static getApiActiveAlertsEndpoint() {
  "/clients_api/dings/active"
}

private static getApiDevicesEndpoint() {
  "/clients_api/ring_devices"
}

private static getApiDoorbotsEndpoint() {
  "/clients_api/doorbots"
}

private static getApiVersion() {
  "11"
}

private static getOauthUri() {
  "https://oauth.ring.com"
}

private static getOauthTokenEndpoint() {
  "/oauth/token"
}

private static getClientId() {
  "ring_official_android"
}

private static getHeaders() {
  [
    "User-Agent": "android:com.ringapp:3.10.1(1)",
    "X-Api-Lang": "en",
    "Accept-Encoding": "gzip"
  ]
}

private static getDeviceModel(kind, flow) {
  switch (kind) {
    // Doorbells
    case "doorbot":
    case "doorbell":
    case "doorbell_v3":
      return [
        name: "Doorbell",
        handler: "Ring Video Doorbell"
      ]
    case "doorbell_v4":
    case "doorbell_v5":
      return [
        name: "Doorbell 2",
        handler: "Ring Video Doorbell"
      ]
    case "doorbell_portal":
      return [
        name: "Peephole Cam",
        handler: "Ring Video Doorbell"
      ]
    case "lpd_v1":
    case "lpd_v2":
      return [
        name: "Doorbell Pro",
        handler: "Ring Video Doorbell",
        data: [wired: true]
      ]
    case "jobx_v1":
      return [
        name: "Doorbell Elite",
        handler: "Ring Video Doorbell",
        data: [wired: true]
      ]
    // Security cams
    case "hp_cam_v1":
    case "floodlight_v2":
      return [
        name: "Floodlight Cam",
        handler: "Ring Floodlight Cam"
      ]
    case "hp_cam_v2":
    case "spotlightw_v2":
      return [
        name: "Spotlight Cam ${flow == "mount" ? "Mount" : "Wired"}",
        handler: "Ring Spotlight Cam",
        data: [wired: true]
      ]
    case "stickup_cams_v4":
      return [
        name: "Spotlight Cam ${flow == "solar" ? "Solar" : "Battery"}",
        handler: "Ring Spotlight Cam"
      ]
    case "stickup_cam":
    case "stickup_cam_v3":
      return [
        name: "Stick Up Cam",
        handler: "Ring Stick Up V1 Cam"
      ]
    case "cocoa_camera":
    case "stickup_cam_lunar":
      return [
        name: "Stick Up Cam Battery",
        handler: "Ring Stick Up V2 Cam"
      ]
    case "stickup_cam_elite":
      return [
        name: "Stick Up Cam Wired",
        handler: "Ring Stick Up V2 Cam",
        data: [wired: true]
      ]
    case "stickup_cam_mini":
      return [
        name: "Indoor Cam",
        handler: "Ring Stick Up V2 Cam",
        data: [wired: true]
      ]
  }
}

private static getIconUrl() {
  "https://raw.githubusercontent.com/jsetton/smartthings-ring/master/icons"
}
