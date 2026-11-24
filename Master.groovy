/***********************************************************************************************************************
*
*  Copyright (C) 2026 roguetech
*
*  License:
*  This program is free software: you can redistribute it and/or modify it under the terms of the GNU
*  General Public License as published by the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
*  implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
*  <http://www.gnu.org/licenses/> for more details.
*
*  Name: Master
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master.groovy
*  Version: 0.4.1.50
*
***********************************************************************************************************************/

// Add tooltips for use-cases with apps
// Add toggle to disable them
// Add "CONST"s as settings
// added storePicoSide
/***********************************************************************************************************************
*
*  Speech device is set as "speechSynthesis". The device capability must be set in the driver, or change this to
*  "voice".
*
***********************************************************************************************************************/

definition(
    name: 'Master',
    namespace: 'master',
    singleInstance: true,
    author: 'roguetech',
    description: 'Parent app for automation suite',
    category: 'Convenience',
    importUrl: 'https://raw.githubusercontent.com/roguetech2/hubitat/master/Master.groovy',
    iconUrl: 'http://cdn.device-icons.smartthings.com/home2-icn@2x.png',
    iconX2Url: 'http://cdn.device-icons.smartthings.com/home2-icn@2x.png'
)


infoIcon = '<img src="http://emily-john.love/icons/information.png" width=20 height=20>'
errorIcon = '<img src="http://emily-john.love/icons/error.png" width=20 height=20>'
warningIcon = '<img src="http://emily-john.love/icons/warning.png" width=20 height=20>'
moreOptions = ' (click for more options)'
expandText = ' (Click to expand/collapse)'

// logLevel sets number of log messages
// 0 for none
// 1 for errors only
// 2 for warnings + errors
// 3 for info + errors + warnings
// 4 for trace + info + errors + warnings
// 5 for debug + trace + info + errors + warnings
def getLogLevel(){
    return 2
}

def displayLabel(text, width = 12){
    if(!text) return
    paragraph('<div style="background-color:#DCDCDC"><b>' + text + '</b></div>', width:width)
}

def displayInfo(text, noDisplayIcon = null, width=12){
    if(!text) return
    if(noDisplayIcon) paragraph('<div style="background-color:AliceBlue">' + text + '</div>', width:width)
    if(!noDisplayIcon) paragraph('<div style="background-color:AliceBlue">' + infoIcon + ' ' + text + '</div>', width:width)
    helpTip = ''
}

def displayError(text, noDisplayIcon = null, width=12){
    if(!text) return
    if(noDisplayIcon) paragraph('<div style="background-color:Bisque">' + text + '</div>', width:width)
    if(!noDisplayIcon) paragraph('<div style="background-color:Bisque">' + errorIcon  + ' ' + text + '</div>', width:width)
    errorMessage = ''
}

def displayWarning(text, noDisplayIcon = null, width=12){
    if(!text) return
    if(noDisplayIcon) paragraph('<div style="background-color:LemonChiffon">' + text + '</div>', width:width)
    if(!noDisplayIcon) paragraph('<div style="background-color:LemonChiffon">' + warningIcon  + ' ' + text + '</div>', width:width)
    warningMessage = ''
}

def highlightText(text, width=12){
    if(!text) return
    return '<div style="background-color:Wheat">' + text + '</div>'
}

preferences {
    page(name: 'mainPage', install: true, uninstall: true){
        section(){
            getAppCounts()
            if(!atomicState?.masterInstalled) displayInfo ('Click "Done" to save.')

            if(!allDevices) displayAllDevicesOption()
            displayHiRezHue()
        }
        displayHideTips()
        displaySchedules()
        displaySensors()
        displayPicos()
        displayMagicCubes()
        displayContactSensors()
        displayPresence()
        if(allDevices) displayAllDevicesOption()
    }
}

def getAppCounts(){
    scheduleCount = 0
    presenceCount = 0
    picoCount = 0
    magicCubeCount = 0
    contactCount = 0
    sensorCount = 0
    childApps.each {Child->
        if(Child.label){
            if(Child.label.startsWith('Time - ')) scheduleCount++
            if(Child.label.startsWith('Presence - ')) presenceCount++
            if(Child.label.startsWith('Pico - ')) picoCount++
            if(Child.label.startsWith('MagicCube - ')) magicCubeCount++
            if(Child.label.startsWith('Contact - ')) contactCount++
            if(Child.label.startsWith('Sensor - ')) sensorCount++
        }
    }
}

def displayAllDevicesOption(){
    fieldName = 'allDevices'
    fieldTitle = 'Select all devices:'
    if(allDevices){
        section(){
            displayInfo('All devices should be selected. This is used to make the app suite of easier to use, but is not required. You can deselect any devices that are never used and/or any virtual devices.')
            input name: fieldName, title: fieldTitle, type: 'capability.*', multiple: true, filter:true, width:12, submitOnChange:true
        }
    }
    if(!allDevices) {
        
    input name: fieldName, title: fieldTitle, type: 'capability.*', multiple: true, filter:true, width:12, submitOnChange:true
    displayInfo('Select all devices ("Toggle All On"). This is used to make the app suite of easier to use, but is not required. You can deselect any devices that are never used and/or any virtual devices.')
    }
}

def displayHiRezHue(){
    input name: 'hiRezHue', type: 'bool', title: 'Enable Hue in degrees (0-360)', defaultValue: false, submitOnChange:true
    displayInfo('Select if light devices have been set to degrees. Leave unselected by default, but failure to match device settings may result in hue settings not working correctly.')
}

def displayHideTips(){
    section(){
        fieldName = 'hideTips'
        fieldTitle = 'Hide tips'
        input name: fieldName, title: fieldTitle, type: 'bool', submitOnChange:true
    }
}

def displaySchedules(){
    title = 'Click to add a <b>schedule</b>'
    if(scheduleCount != 0) title = 'Click to add or edit <b>schedules</b> (' + scheduleCount + ' total)'

        displayScheduleTip()
    section(hideable: true, hidden: true, title) {
        app(name: 'childApps', appName: 'Master - Time', namespace: 'master', title: 'New Schedule', multiple: true)
    }
}

def displayPicos(){
    section(){
        displayPicoTip()
        title = 'Click to add '
        if(picoCount == 0) title += 'a <b>Pico</b>'
        if(picoCount != 0) title += 'or edit <b>Picos</b> (' + picoCount + ' total)'
        section(hideable: true, hidden: true, title) {
            app(name: 'childApps', appName: 'Master - Pico', namespace: 'master', title: 'New Pico', multiple: true)
        }
    }
}

def displayMagicCubes(){
    section(){
        displayMagicCubeTip()
        title = 'Click to add '
        if(magicCubeCount == 0) title += 'a <b>MagicCube</b>'
        if(magicCubeCount != 0) title += 'or edit <b>MagicCubes</b> (' + magicCubeCount + ' total)'
        section(hideable: true, hidden: true, title) {
            app(name: 'childApps', appName: 'Master - MagicCube', namespace: 'master', title: 'New MagicCube', multiple: true)
        }
    }
}

def displaySensors(){
    section(){
        title = 'Click to add '
        if(sensorCount == 0) title += 'a <b>Sensor</b>'
        if(sensorCount != 0) title += 'or edit <b>sensors</b> (' + sensorCount + ' total)'
        section(hideable: true, hidden: true, title) {
            app(name: 'childApps', appName: 'Master - Sensor', namespace: 'master', title: 'New Sensor', multiple: true)
        }
    }
}
def displayContactSensors(){
    section(){
        title = 'Click to add '
        if(contactCount == 0) title += 'a <b>contact/door sensor</b>'
        if(contactCount != 0) title += "or edit <b>contact/door sensors</b> ($contactCount total)"
        section(hideable: true, hidden: true, title) {
            app(name: 'childApps', appName: 'Master - Contact', namespace: 'master', title: 'New Contact Sensor', multiple: true)
        }
    }
}

def displayPresence(){
    section(){
        title = 'Click to add '
        if(presenceCount == 0) {
            title += 'a <b>presence</b> setting'
        } else {
            title += "or edit <b>presence</b> settings ($presenceCount total)"
        }
        section(hideable: true, hidden: true, title) {
            app(name: 'childApps', appName: 'Master - Presence', namespace: 'master', title: 'New Presence', multiple: true)
        }
    }
}

/*
if(notificationDevice && people){
section(""){
def i = 0
people.each{
i++
paragraph "<div style=\"background-color:BurlyWood\"><b> Sing Happy Birthday for $it:</b></div>"
if(i == 1) paragraph "This sets Google Home to sing Happy Birthday on the individual's birthday between the times entered, a few minutes after they (or a few minutes after the start time, if they're already home). It can be set to wait for everyone to be home before singing."
if(!settings["happyBDay$i"]){
input "happyBDay${i}", "bool", title: "Do not sing Happy Birthday for $it? Click to sing.", submitOnChange:true
} else {
input "happyBDay${i}", "bool", title: "Sing Happy Birthday for $it? Click to not sing.", submitOnChange:true
}
if(settings["happyBDay$i"]){
input "dateBDay${i}", "date", title: "Enter birthday date for $it."
if(!settings["bDayEveryone$i"]){
input "bDayEveryone${i}", "bool", title: "Sing regardless of who else is home. Click to only sing with everyone.", submitOnChange:true
} else {
input "bDayEveryone${i}", "bool", title: "Only sing when everyone is home. Click to sing regardless who's home.", submitOnChange:true
}
input "timeStart", "time", title: "Only sing between start time", required: true, width: 6, submitOnChange:true
input "timeStop", "time", title: "and stop time", required: true, width: 6, submitOnChange:true
}
}

}
}
*/


/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

def installed() {
    atomicState.masterInstalled = true
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
}

// Returns app name with app title prepended
def appendAppTitle(appName, appTitle){
    //Compare length of name (eg "test") to appTitle length minus 6 (eg "Master - Time - " minus "Master - "; "Time - " is min length)
    if(appName.length() < appTitle.length() - 6){
        returnValue = appTitle.substring(9, appTitle.length()) + ' - ' + appName
        //Compare first part of name (eg "Testing") to middle part of appTitle (eg "Master - Time" minus "Master - " plus " - ")
    } else if(appName.substring(0, appTitle.length() - 9) != appTitle.substring(9, appTitle.length())){
        returnValue = appTitle.substring(9, appTitle.length()) + ' - ' + appName
    } else {
        returnValue = appName
    }

    return returnValue
}

// Returns true if level value is either valid or null
def validateLevel(value, appId = app.id){
    if(!value) return true
    value = value as int
        if(value < 1) return false
        if(value > 100) return false
    return true
}

// Returns true if temp value is either valid or null
def validateTemp(value, appId = app.id){
    if(!value) return true
        value = value as int
            if(value < 1800) return false
            if(value > 6500) return false
            return true
}


// Returns true if temp value is either valid or null
def validateHue(value, appId = app.id){
    if(!value) return true
    value = value as int
    if(value < 1) return false

    if(value > 360) return false
    return true
}

// Returns true if temp value is either valid or null
def validateSat(value, appId = app.id){
    if(!value) return true
    value = value as int
        if(value < 1) return false
        if(value > 100) return false
        return true
}

def validateMultiplier(value, appId = app.id){
    if(value){
        if(value < 1 || value > 100){
            putLog(357, 'error', "ERROR: Multiplier $value is not valid", appId)
            return
        }
    }
    return value
}













































def checkIsDimmable(singleDevice, appId = app.id){
    if(singleDevice) return singleDevice.hasCapability('SwitchLevel')
}

def checkIsTemp(singleDevice, appId = app.id){
    if(singleDevice) return singleDevice.hasCapability('ColorTemperature')
}

def checkIsColor(singleDevice, appId = app.id){
    if(singleDevice) return singleDevice.hasCapability('ColorMode')
}
// Used by child apps to determine if UI shows dim/brighten, temp or color options
def checkIsDimmableMulti(multiDevice, appId = app.id){
    if(!multiDevice) return
    multiDevice.any{singleDevice->
        if(checkIsDimmable(singleDevice, appId)) return true
    }
}
def checkIsTempMulti(multiDevice, appId = app.id){
    if(!multiDevice) return
    multiDevice.any{singleDevice->
        if(checkIsTemp(singleDevice, appId)) return true
    }
}
def checkIsColorMulti(multiDevice, appId = app.id){
    if(!multiDevice) return
    multiDevice.any{singleDevice->
        if(checkIsColor(singleDevice, appId)) return true
    }
}

def checkIsFan(singleDevice, appId = app.id){
    if(singleDevice) return singleDevice.hasCapability('FanControl')
}
// Test state of a single switch
def checkIsOn(singleDevice, appId = app.id){
    // If no deviceState, set it
    stateValue = atomicState.'state'?."${singleDevice.id}"
    if(stateValue == 'on') return true
    if(stateValue == 'off') return false 
    if(singleDevice.currentValue('switch') == 'on') return true
}

// Used by Sensor
// Test state of a group of switches
// Return true if any are on
def checkAnyOnMulti(multiDevice, appId = app.id){
    if(!multiDevice) return
    multiDevice.any{singleDevice->
        if(checkIsOn(singleDevice, appId)) return true
    }
}

def checkAnyPeopleHome(multiDevice, appId = app.id){
    if(!multiDevice) return true
    multiDevice.any{singleDevice->
        if(singleDevice.currentPresence == 'present') return true
    }
}

def checkAllPeopleNotHome(multiDevice, appId = app.id){
    if(!multiDevice) return true
    returnValue = true
    multiDevice.any{it ->
        if(it.currentPresence == 'present') returnValue = false
    }
    return returnValue
}
// hiRezHue is currently boolean global option in master
// It should allow setting it for specific devices
// Possibly could automate:
//  Loop through all devices used in any schedule, store device.currentHue
//  Store current state (on/off) of device
//  Set device currentHue to 360
//  Check if devic.currentHue reports 100
//  Set device hiRezHue
//  Set device currentHue to stored value
//  Set device state to stored value
def settingHiRezHue(appId = app.id){
    return settings['hiRezHue']
}

// Minimum incremental cycle for an "active" schedule (people home, right mode, device is on)
// Must be greater than CONSTDeviceActionDelayMillis * 4, or else app won't complete fast enough
def CONSTScheduleMinimumActiveFrequencyMilli(){
    return 8000
}

// Minimum incremental cycle for an "active" schedule (people not home, wrong mode, device is off)
// Should be longer than CONSTScheduleMinimumActiveFrequencyMilli
// Must be greater than CONSTDeviceActionDelayMillis * 4, or else app won't complete fast enough
def CONSTScheduleMinimumInactiveFrequencyMilli(){
    return 30000
}

def CONSTScheduleMaximumIncrements(){
    return 200
}

def CONSTDayInMilli(){
    return 86400000
}

// Used for notifications, for some reason
def CONSTHourInMilli(){
    return 3600000
}

def CONSTMinuteInMilli(){
    return 60000
}

def CONSTDeviceActionDelayMillis(){
    return 200
}

def CONSTProgressiveDimmingDelayTimeMillis(){
    return 750
}

def CONSTPushDimmingTotalSteps(){
    return 10
}

def CONSTHoldDimmingTotalSteps(){
    return 15
}
                                                                                                       
def CONSTDeviceDefaultBrightness(){
    return 100
}

def CONSTDeviceDefaultTemp(){
    return 3250
}

def getTimeOfDayInMillis(time, appId = app.id){
    if(!time) return
    timeAtMidnight = new Date(time).clearTime().getTime()
    return time - timeAtMidnight
}

def getDatetimeFromTimeInMillis(time, appId = app.id){
    if(!time) return
    timeAtMidnight = new Date(now()).clearTime().getTime()
    return time + timeAtMidnight
}

// Returns app name with app title prepended
def appendChildAppTitle(appName, appTitle){
    //Compare length of name (eg "test") to appTitle length minus 6 (eg "Master - Time - " minus "Master - "; "Time - " is min length)
    if(appName.length() < appTitle.length() - 6) return appTitle.substring(9, appTitle.length()) + ' - ' + appName
    //Compare first part of name (eg "Testing") to middle part of appTitle (eg "Master - Time" minus "Master - " plus " - ")
    if(appName.substring(0, appTitle.length() - 9) != appTitle.substring(9, appTitle.length())) return appTitle.substring(9, appTitle.length()) + ' - ' + appName
    return appName
}

def getDeviceList(){
    return allDevices
}




// ***************************************************************************** 
// * Perform actions                                                           *
// *****************************************************************************




// Should be added to Table
// Lock or unlock a group of locks
def setLockMulti(multiDevice, action, appId = app.id){
    if(!multiDevice) return
    multiDevice.each{singleDevice->
        setLockSingle(singleDevice, action, appId)
    }
}

// Lock or unlock a single lock
def setLockSingle(singleDevice, action, appId = app.id){
    if(action == 'lock') singleDevice.lock()
    if(action == 'unlock') singleDevice.unlock()
    putLog(592, 'info', '[' + singleDevice + '] ' + action + 'ed ', appId)
}

// setDeviceMulti passes to both setDeviceLevelMulti and setDeviceStateMulti
// setDeviceLevelMulti hands off to setDeviceLevelSingle
// setDeviceLevelMulti could be put inside setDeviceMulti
// but otherwise, setDeviceLevelMulti is needed separate from setDeviceLevelSingle for pausing per setting
def setDeviceMulti(multiDevice, appType = 'manual', appId = app.id){
    if(!multiDevice) return

    setDeviceLevelMulti(multiDevice, 'brightness', appId)
    setDeviceLevelMulti(multiDevice, 'sat', appId)
    setDeviceLevelMulti(multiDevice, 'hue', appId)
    setDeviceLevelMulti(multiDevice, 'temp', appId)
    setDeviceStateMulti(multiDevice, appType, appId)
}

def setDeviceSingle(singleDevice, appType = 'manual', appId = app.id){
    if(!singleDevice) return

    // brightness
    def newLevel = getDeviceLevelSingleChange(singleDevice, 'brightness', appId)
    if(newLevel){
        pauseActions()
        setDeviceLevelSingle(singleDevice, 'brightness', newLevel, appId)
    }
    // saturation
    newLevel = getDeviceLevelSingleChange(singleDevice, 'sat', appId)
    if(newLevel){
        pauseActions()
        setDeviceLevelSingle(singleDevice, 'sat', newLevel, appId)
    }
    // hue
    newLevel = getDeviceLevelSingleChange(singleDevice, 'hue', appId)
    if(newLevel){
        pauseActions()
        setDeviceLevelSingle(singleDevice, 'hue', newLevel, appId)
    }
    // temperature
    newLevel = getDeviceLevelSingleChange(singleDevice, 'temp', appId)
    if(newLevel){
        pauseActions()
        setDeviceLevelSingle(singleDevice, 'temp', newLevel, appId)
    }
    if(getDeviceStateChangeSingle(singleDevice, appId)){
        pauseActions()
        setDeviceStateSingle(singleDevice, appType, appId)
    }
}

def setDeviceLevelMulti(multiDevice, type, appId = app.id){
    multiDevice.each{singleDevice->
        def newLevel = getDeviceLevelSingleChange(singleDevice, type, appId)
        if(newLevel){
            pauseActions()
            setDeviceLevelSingle(singleDevice, type, newLevel, appId)
        }
    }
}

// If any, return value of new level
// Used to determine if a change is occuring for pausing execution
def getDeviceLevelSingleChange(singleDevice, type, appId = app.id){
    if(!singleDevice) return
    
    def stateValue = atomicState.'state'?."${singleDevice.id}"
    if(!stateValue) return       // Should set state to current device state?
    if(stateValue == 'off') return
    
    if(type == 'brightness' && !checkIsDimmable(singleDevice, appId)) return
    if(type == 'temp' && !checkIsTemp(singleDevice, appId)) return
    if(type == 'hue' && !checkIsColor(singleDevice, appId)) return
    if(type == 'sat' && !checkIsColor(singleDevice, appId)) return

    if(type == 'temp'){
        if(atomicState.'nonSchedule'?."${singleDevice.id}"?.'hue') return
        if(atomicState.'nonSchedule'?."${singleDevice.id}"?.'sat') return
        if(atomicState.'schedule'?."${singleDevice.id}"?.'hue') return
        if(atomicState.'schedule'?."${singleDevice.id}"?.'sat') return
    }
    
    // Need to somehow get the default into nonSchedule? Otherwise, it gets set on capture
    newLevel = getCurrentLevelFromTableOrDefault(singleDevice, type, appId)
    
    
    if(!newLevel) return
    
    if(singleDevice.currentValue('switch') == 'on'){
        if(!checkLevelDifferent(singleDevice, type, newLevel, appId)) return
    }
    return newLevel
}

def setDeviceLevelSingle(singleDevice, type, newLevel, appId = app.id){
    if(type == 'brightness'){
        checkFan = checkIsFan(singleDevice, appId)
        if(checkFan) singleDevice.setSpeed(newLevel)        // Need to see about computing fan speed based on newLevel using device.supportedFanSpeeds
        if(!checkFan) singleDevice.setLevel(newLevel)
    }
    if(type == 'temp') singleDevice.setColorTemperature(newLevel)
    if(type == 'hue') singleDevice.setHue(convertHueValueToDefault(newLevel, appId))
    if(type == 'sat') singleDevice.setSaturation(newLevel)
    
    putLog(695, 'info', '[' + singleDevice + '] ' + type + ' ' + newLevel, appId)
    return true
}

def setDeviceStateMulti(multiDevice, appType = 'manual', appId = app.id){
    multiDevice.each{singleDevice->
        if(getDeviceStateChangeSingle(singleDevice, appId)){
            pauseActions()
            setDeviceStateSingle(singleDevice, appType, appId)
        }
    }
}

def getDeviceStateChangeSingle(singleDevice, appId = app.id){
    def stateValue = atomicState.'state'?."${singleDevice.id}"
    if(!stateValue) return
    if(singleDevice.currentValue('switch') == stateValue) return
    return true
}

def setDeviceStateSingle(singleDevice, appType = 'manual', appId = app.id){
    def stateValue = atomicState.'state'?."${singleDevice.id}"
    if(stateValue == 'on') singleDevice.on()
    if(stateValue == 'off') {
        singleDevice.off()
        // clear state settings
        clearTableByDeviceAndAppId(singleDevice.id, 'nonSchedule', appId)
       // clearNonScheduleDeviceSetting(singleDevice.id, appId)
    }
    
    setDeviceLastChangedApp(singleDevice.id, appType, appId)
    putLog(726, 'info', '[' + singleDevice + '] ' + stateValue, appId)
    return true
}

def storeCubeSide(singleDeviceId, side, appId = app.id){
    // Need to read in whole variable, and resave the entire thing, because of some hubitat stupiditiness
    def stateValue = atomicState?.cube
    if(!stateValue) stateValue = [:]
    stateValue."${singleDeviceId}" = side
    atomicState.'cube' = stateValue
}

def getCubeSide(singleDeviceId, appId = app.id){
    // Need to read in whole variable, and resave the entire thing, because of some hubitat stupiditiness
    return convertToInteger(atomicState?.'cube'?."${singleDeviceId}")

}
def changeMode(mode, appId = app.id){
    if(location.mode == mode) return
    def message = 'Changed Mode from ' + location.mode + ' to ' + mode + '.'
    pauseActions()
    setLocationMode(mode)
    putLog(748, 'debug', message, appId)
}

// Send SMS text message to $phone with $message
//SMS IS NO LONGER SUPPORTED
// Needs to be rewritten fro whatever supported method(s)
/*
def sendPushNotification(phone, message, appId = app.id){
    def now = new Date().getTime()
    def seconds = (now - atomicState.contactLastNotification) / 1000
    if(seconds < 361) {
        putLog(759, 'info', 'Did not send push notice for ' + evt.displayName + ' ' + evt.value + 'due to notification sent ' + seconds + ' ago.', appId)
        return
    }
    pauseActions()
    atomicState.contactLastNotification = now
    speechDevice.find{it ->
        if(it.id == deviceId) {
            if(it.deviceNotification(message)) putLog(766, 'debug', 'Sent phone message to ' + phone + ' "' + message + '"', appId)
        }
    }
}

def sendVoiceNotification(deviceId, message, appId = app.id){
    if(!deviceId)  return
    pauseActions()
    speechDevice.find{it ->
        if(it.id == deviceId) {
            if(it.speak(message)) putLog(776, 'debug', 'Played voice message on ' + deviceId + ' "' + message + '"', appId)
        }
    }
}
*/

def getCurrentLevelFromTableOrDefault(singleDevice, type, appId = app.id){
    def levelValue = getCurrentLevelFromTable(singleDevice.id, type, appId)
    if(type == 'brightness' && checkIsFan(singleDevice, appId)) return levelValue       // Fans do not use default levels
    if(!levelValue) levelValue = getDeviceLevelDefault(type, appId)
    if(type == 'hue') return levelValue
    if(type == 'sat'){
        if(!levelValue){
            if(getCurrentLevelFromTable(singleDevice.id, 'hue', appId)){
                levelValue = 100
            }
        }
    }
    return levelValue
}

def getCurrentLevelFromTable(singleDeviceId, type, appId = app.id){
    def nonScheduleValue = atomicState?.'nonSchedule'?."${singleDeviceId}"?."${type}"
    if(nonScheduleValue) return nonScheduleValue
    def scheduleValue = atomicState?.'schedule'?."${singleDeviceId}"?."${type}"?.'currentLevel'
    if(scheduleValue) return scheduleValue
}

// Call when writting to the device
def convertHueValueToDefault(hueValue, appId = app.id){
    if(!hueValue) return
    if(settings['hiRezHue']) return hueValue
    return Math.round(hueValue / 3.6) as Integer
}

// Call when reading from the device
def convertHueValueFromDefault(hueValue, appId = app.id){
    if(!hueValue) return
    if(settings['hiRezHue']) return hueValue
    return Math.round(hueValue * 3.6) as Integer
}

def getDeviceLevelDefault(type, appId = app.id){
    if(type == 'brightness') return CONSTDeviceDefaultBrightness()
    if(type == 'temp') return CONSTDeviceDefaultTemp()
    if(type == 'hue') return
    if(type == 'sat') return
}

def setDeviceLastChangedApp(singleDeviceId, appType, appId = app.id){
    def tempMap = [:]
    if(atomicState?.'stateChangeApp') tempMap = atomicState?.'stateChangeApp'
    tempMap."${singleDeviceId}" = appType
    atomicState.'stateChangeApp' = tempMap
}

def getDeviceLastChangedApp(singleDeviceId, appId = app.id){
    changedAppId = atomicState?.'stateChangeApp'?."${singleDeviceId}"
    if(changedAppId) return changedAppId
}

def getCurrentLevelFromDevice(singleDevice, type, appId = app.id){
    if(type == 'brightness') {
        if(checkIsFan(singleDevice)) return singleDevice.currentSpeed
        return singleDevice.currentLevel as Integer
    }
    if(type == 'temp' && singleDevice.currentColorMode != 'RGB') return singleDevice.currentColorTemperature as Integer
    if(type == 'hue' && singleDevice.currentColorMode == 'RGB') {
        def hueLevel = singleDevice.currentHue
        if(hueLevel == 0) hueLevel = 1
        return convertHueValueFromDefault(hueLevel, appId) as Integer
    }
    if(type == 'sat' && singleDevice.currentColorMode == 'RGB'){
        def satLevel = singleDevice.currentSat
        if(satLevel == 0) satLevel = 1
        return satLevel as Integer
    }
}

// If newLevel is different from oldLevel, return true
// If newLevel is null, return false
def checkLevelDifferent(singleDevice, type, newLevel, appId = app.id){
    if(!newLevel) return
    def oldLevel = getCurrentLevelFromDevice(singleDevice, type, appId)     // This should only happen with temp in color mode, or color in CT mode
    if(!oldLevel) return true
    if(type == 'temp'){
        if(Math.abs(newLevel - oldLevel) < 25) return false
    }
    if((type == 'hue' || type == 'sat') && singleDevice.currentColorMode != 'RGB') return true
    if(type == 'temp' && singleDevice.currentColorMode == 'RGB') return true
    if(newLevel != oldLevel) return true
}

// Do reverse, and wrap around
// Not used
def _computeIncrementalHueRange(singleDeviceId, appId = app.id){
    if(atomicState.'schedule'."${singleDeviceId}".'hue'?.'hueDirection' == 'reverse') return 360 - Math.abs(atomicState.'schedule'."${singleDeviceId}".'hue'.'stopLevel' + atomicState.'schedule'."${singleDeviceId}".'hue'.'startLevel')
    if(atomicState.'schedule'."${singleDeviceId}".'hue'?.'hueDirection' != 'reverse') return Math.abs(atomicState.'schedule'."${singleDeviceId}".'hue'.'stopLevel' - atomicState.'schedule'."${singleDeviceId}".'hue'.'startLevel')
}

def resumeDeviceScheduleMulti(multiDevice, appId = app.id){
    if(!multiDevice) return
    multiDevice.each{singleDevice->
        clearNonScheduleDeviceSetting(singleDevice.id, appId)
    }
}

def clearNonScheduleDeviceSetting(singleDeviceId, appId = app.id){
    if(!singleDeviceId) return
    if(!atomicState.'nonSchedule'?."${singleDeviceId}") return

    def newVar = atomicState.'nonSchedule'
    def toRemove = []
    newVar?."${singleDeviceId}".each{key, value -> 
        toRemove.add(key)
    }
    toRemove.each{newVar."${singleDeviceId}".remove(it)}
    atomicState.'nonSchedule' = newVar
}

def getStateMapSingle(singleDevice, action, appId = app.id){
    if(!action) return
    if(!singleDevice) return
    if(action != 'on' && action != 'off' && action != 'toggle') return
    
    if(action == 'toggle'){
        action = 'on'
        def stateValue = atomicState?.'state'?."${singleDevice.id}"
        if(!stateValue) {
            if(singleDevice.currentValue('switch') == 'on') action = 'off'
        }
        if(stateValue == 'on') action = 'off'
    }
    return action    // appId used by sensor
}

def getNonScheduleLevelMap(type, level, appId = app.id){
    if(!type) return
    if(!level) return
    if(!appId) return
    if(type != 'brightness' && type != 'temp' && type != 'hue' && type != 'sat') return
    
    return [(type):level]
}
def getScheduleLevelMap(type, level, appId = app.id){
    if(!type) return
    if(!level) return
    if(!appId) return
    if(type != 'brightness' && type != 'temp' && type != 'hue' && type != 'sat') return
      
    return [(type):['currentLevel':level, 'appId':appId]]
}

def updateTableCapturedState(singleDevice, action, appType = 'manual', appId = app.id){
    def stateValue = atomicState?.'state'?."${singleDevice.id}"
    if(stateValue == action) return
    if(action != 'on' && action != 'off') return
    putLog(933, 'trace', '[' + singleDevice + '] captured state ' + action + ' (table was ' + stateValue + '; actually was ' + singleDevice.currentState + ')', appId)
    def stateMap = getStateMapSingle(singleDevice, action, app.id)
    mergeMapToTable(singleDevice.id, 'state', stateMap, appId)
    if(action == 'on') setDeviceSingle(singleDevice, appType, appId)    // With device on, set levels
}


def combineMaps(map1, map2, map3 = '', map4 = '', map5 = '', appId = app.id){
    def returnMap = [:]
    if(map1) returnMap += map1
    if(map2) returnMap += map2
    if(map3) returnMap += map3
    if(map4) returnMap += map4
    if(map5) returnMap += map5
    return returnMap
}

//THIS CAN BE REWRITTEN TO OVERWRITE WITH SUBKEYS FROM NEW MAP

// newMap in format of [[type:'brightness/temp/sat/hue', time:datetime as long, stopTime:datetime as long, addId:nnnn, appType:string], ETC]
// Needs to rewrite atomicState dueto Hubitat fuckery
def mergeMapToTable(singleDeviceId, oldTableName, newMap, appId = app.id){
    if(!singleDeviceId) return
    if(!newMap) return
    
    if(oldTableName != 'state' && oldTableName != 'nonSchedule' && oldTableName != 'schedule' && oldTableName != 'incremental') {
        putLog(959, 'error', 'Invalid table name ' + oldTableName + ' sent to mergeMapToTable (ignore if new install)', appId)
        return
    }

    def newTableName = oldTableName
    if(oldTableName == 'incremental') newTableName = 'schedule'

    def tempMap = [:]
    if(atomicState?."${newTableName}") tempMap = atomicState?."${newTableName}"

    if(newTableName == 'state') {
        tempMap."${singleDeviceId}" = newMap
        atomicState."${newTableName}" =  tempMap
        return true
    }
    if(!tempMap?."${singleDeviceId}") tempMap[(singleDeviceId)] = [:]
    newMap.each{newKey, newValue->
        tempMap."${singleDeviceId}"."${newKey}" = newValue
        if(oldTableName == 'schedule') clearTableByDeviceAndType(singleDeviceId, 'nonSchedule', newKey, appId)    //On schedule start, clear non-schedule
    }
    atomicState."${newTableName}" = tempMap
}

//Never used
def clearTableByDeviceAndTypeAndAppId(singleDeviceId, tableName, type, appId = app.id){
    if(!singleDeviceId) return
    if(!atomicState?."${tableName}"?."${singleDeviceId}"?."${type}") return

    newVar = atomicState."${tableName}"
    if(!newVar) newVar = [:]
    toRemove = []
    newVar."${singleDeviceId}".each{key, value ->
        if(tableName == 'nonSchedule') toRemove.add(key)
        if(tableName == 'schedule') {
            if(value?.'appId' == appId && key == type) {
                toRemove.add(key)
            }
        }
    }

    toRemove.each{newVar."${singleDeviceId}".remove(it)}
    atomicState."${tableName}" = newVar.findAll{ it.value!=[:] }
}

def clearTableByDeviceAndAppId(singleDeviceId, tableName, appId = app.id){
    if(!singleDeviceId) return
    if(!atomicState?."${tableName}"?."${singleDeviceId}") return
    // if schedule, need to loop through and delete only itself (appId)
    def newVar = atomicState?."${tableName}"
    if(!newVar) newVar = [:]
    if(tableName == 'schedule') {
        toRemove = []
        newVar?."${singleDeviceId}".each{key, value -> 
            if(value?.'appId'){
                if(value?.'appId' == appId) {
                    toRemove.add(key)
                }
            }
        }
        toRemove.each{newVar."${singleDeviceId}".remove(it)}
        atomicState.'schedule' = newVar.findAll{ it.value!=[:] }
        return true
    }

    newVar.remove(singleDeviceId)
    atomicState."${tableName}" = newVar.findAll{ it.value!=[:] }
    return true
}

def clearTableByDeviceAndType(singleDeviceId, tableName, type, appId = app.id){
    if(!singleDeviceId) return
    if(!atomicState?."${tableName}"?."${singleDeviceId}"?."${type}") return
    def newVar = atomicState?."${tableName}"
    if(!newVar) newVar = [:]
    newVar."${singleDeviceId}".remove(type)
    atomicState."${tableName}" = newVar.findAll{ it.value!=[:] }
    return true
}

def clearTableByAppId(tableName, appId = app.id){
    newVar = atomicState?."${tableName}"
    if(!newVar) newVar = [:]
    newVar?.each{key, value ->      // Devices
        toRemove = []
        newVar?."${key}".each{key2, value2 -> // Levels
            if(value2?.'appId'){
                if(value2?.'appId' == appId) {
                    toRemove.add(key2)
                }
            }
        }
        toRemove.each{newVar."${key}".remove(it)}
    }
    atomicState."${tableName}" = newVar.findAll{ it.value!=[:] }
    return true
}

def updateTableCapturedLevel(singleDeviceId, type, newLevel, appId = app.id){
    if(!newLevel) return
    def tableLevel = atomicState?.'nonSchedule'?."${singleDeviceId}"?."${type}"
    if(!tableLevel) tableLevel = atomicState?.'schedule'?."${singleDeviceId}"?."${type}"?.'currentLevel'

    if(!tableLevel){      // Exclude defaults (or maybe capture them as nonSchedule? Or have a separate table for them?)
        if(type == 'brightness') tableLevel = CONSTDeviceDefaultBrightness()
        if(type == 'temp') tableLevel = CONSTDeviceDefaultTemp()
        if(type == 'sat') tableLevel = 100
    }
    if(newLevel == tableLevel) return
    if(tableLevel) {
        if(type == 'temp'){
            if(Math.abs(newLevel - tableLevel) < 25) return
        }
        if(type == 'hue'){
            if(Math.abs(newLevel - tableLevel) < 4) return
        }
        putLog(1074, 'trace', '[Device Id: ' + singleDeviceId + '] captured ' + type + ' to ' + newLevel + ' (current/table/default was ' + tableLevel + ')', appId)
    }
    
    if((type == 'hue' || type == 'sat') && newLevel == 0) newLevel = 1
    def levelMap = getNonScheduleLevelMap(type, newLevel, appId)
    mergeMapToTable(singleDeviceId, 'nonSchedule', levelMap, appId)
}

def _getNextLevelDimmable(singleDevice, action, dimFactor = 1.61, appId = app.id){
    if(action != 'dim' && action != 'brighten') return
    if(!dimFactor) dimFactor = 1.61

    if(checkIsFan(singleDevice, appId)) return _getNextLevelFan(singleDevice, action, appId)

    if(!checkIsDimmable(singleDevice, appId)) return
    def oldLevel = getCurrentLevelFromTable(singleDevice.id, 'brightness', appId)
    if(!oldLevel && checkIsOn(singleDevice, appId)) oldLevel = getCurrentLevelFromDevice(singleDevice, 'brightness', appId)
    if(!oldLevel){
        if(action == 'brighten') return 1
        if(action == 'dim') return 100
    }
    if (action == 'dim'){
        newLevel = Math.round(convertToInteger(oldLevel) / dimFactor)
        if (newLevel == oldLevel) newLevel -= 1
        if(newLevel < 1) newLevel = 1
    }
    if (action == 'brighten'){
        newLevel = Math.round(convertToInteger(oldLevel) * dimFactor)
        if (newLevel == oldLevel) newLevel++
        if(newLevel > 100) newLevel = 100
    }

    return newLevel
}

def _getNextLevelFan(singleDevice, action, appId = app.id){
    if(!checkIsFan(singleDevice, appId)) return
    if(action != 'dim' && action != 'brighten') return
    def brightnessValue = atomicState?.'schedule'?."${singleDevice.id}"?.'brightness'?.'currentLevel'
    if(brightnessValue) level = brightnessValue
    if(!brightnessValue) level = getCurrentLevelFromDevice(singleDevice, 'brightness', appId)

    if(action == 'brighten'){
        if(level == 'off') return 'low'
        if(level == 'high') return 'off'
        if(level == 'low' || level == 'medium-low') return 'medium'
        if(level == 'medium' || level == 'medium-high') return 'high'
        if(!level || level == 'off') return 'low'
    }
    if(!level) return
    if(level == 'off') return 'high'
    if(level == 'low') return 'off'
    if(level == 'high') return 'medium'
    if(level == 'medium-high') return 'medium'
    if(level == 'medium') return 'low'
    if(level == 'medium-low') return 'low'
}

// Finds the smallest factor (0.01 resolution, between 1.00 and 2.00) that takes a value from 1 to 100
// in exactly numberOfSteps steps. If no factor gives an exact match, returns the closest one.
// Returns null if numberOfSteps is outside the achievable range.
// [Claude Sonnet 5.5]
def computeOptiomalGeometricProgressionFactor(numberOfSteps, appId = app.id){
    if(!numberOfSteps) return
    numberOfSteps = numberOfSteps as Integer

    def maxSteps = computeGeometricProgressionSteps(1.0)   // slowest progression (+1 each step)
    def minSteps = computeGeometricProgressionSteps(2.0)   // fastest progression (doubling)
    if(numberOfSteps > maxSteps || numberOfSteps < minSteps) return

    def bestFactor = 1.0
    def bestDiff = null
    // Integer loop (hundredths) avoids floating point drift from repeated += 0.01
    for(int i = 100; i <= 200; i++){
        def factor = i / 100.0
        def steps = computeGeometricProgressionSteps(factor)
        if(steps == numberOfSteps) return factor as double
        def diff = Math.abs(steps - numberOfSteps)
        if(bestDiff == null || diff < bestDiff){
            bestDiff = diff
            bestFactor = factor
        }
    }
    return bestFactor as double
}

// computes the number of steps to get from 1 to 100 by x * 1.y
// Returns null if factor is outside 1..2
// [Claude Sonnet 5.5]
def computeGeometricProgressionSteps(factor, appId = app.id){
    if(factor == null) return
    double dFactor = factor as double
    if(dFactor < 1 || dFactor > 2) return
    long priorStep = 1
    int count = 0
    while (priorStep < 100) {
        long thisStep = Math.round(dFactor * priorStep)
        if(thisStep > priorStep) {
            priorStep = thisStep
        } else {
            priorStep++   // factor too small to move the rounded value, so advance by 1
        }
        count++
    }
    return count
}

// Sets cron string based on:
//      timeMillis = time from now in milliseconds, or
//      timeValue = time in timestamp
// Checks child app getDisabled
// If active, passes cronString, functionName, and formated parameters back to child app, to set the schedule
def scheduleChildEvent(timeMillis = 0, timeValue = '', functionName, parameters, appId = app.id){
    // noPerformDisableCheck is reversed, so that null does not equal False
    // true = not check, False = check
    if(!appId) return
    if(!timeMillis && !timeValue) return
    if(!parameters) parameters = ''
    if(timeMillis && timeMillis < 0) {
        putLog(1193, 'warn', 'scheduleChildEvent given negative timeMillis from appId ' + appId + ' (' + functionName + ' timeMillis = ' + timeMillis + ')', appId)
        return
    }

    def currentTimeMillis = ''
    def targetTimeMillis = ''
    def parametersMap = [:]

    if(timeValue) {
        currentTimeMillis = now()
        targetTimeMillis = new Date(timeValue).time
        timeMillis = targetTimeMillis - currentTimeMillis
        if(timeMillis < 0) timeMillis += CONSTDayInMilli()
    }

    if(parameters) parametersMap = ['data':parameters]

    childApps.find {Child->
        if(Child.id == appId) {
                if(!functionName) {
                    putLog(1213, 'warn', 'scheduleChildEvent given null for functionName from appId ' + appId + ' (timeMillis = ' + timeMillis + ', timeValue = ' + timeValue + ')', appId)
                    return
                }
                Child.setScheduleFromParent(timeMillis, functionName, parametersMap)
                putLog(1217, 'debug', 'Scheduled ' + functionName + '(' + parameters + ')' + ' for ' + new Date(timeMillis + now()).format('hh:mma MM/dd ') + ' (in ' + Math.round(timeMillis / 1000) + ' seconds)', appId)
        }
    }
}

def convertToInteger(value, appId = app.id){
    if(value instanceof String && value.isInteger()) return value.toInteger()
    return value
}

// pauseActions is called AFTER a device change is made
// Maybe this should be per device, not per app/action - but tracking that would take too much computing time
def pauseActions(appId = app.id){
    lastAction = atomicState.'lastActionTime'
    if(!lastAction) lastAction = 0
    if((lastAction + CONSTDeviceActionDelayMillis()) < now()) {
        atomicState.'lastActionTime' = now()
        return        // No delay required
    }

    delayTime = CONSTDeviceActionDelayMillis() - (now() - lastAction)
    putLog(1238, 'debug', 'Pausing execution: ' + delayTime + 'ms', appId)
    
    pauseExecution(delayTime)
    atomicState.'lastActionTime' = now()
}

// ***************************************************************************** 
// *                                                                           *
// *                                                                           *
// * Date functions                                                            *
// *                                                                           *
// *                                                                           *
// *****************************************************************************

def getCalendar(year, month, day, timeZone) {
    if(month < 1 || month > 12) return null

    def daysInMonth = [
        31,
        isLeapYear(year) ? 29 : 28,
        31, 30, 31, 30,
        31, 31, 30, 31, 30, 31
    ]

    if(day < 1 || day > daysInMonth[month - 1]) return null

    Calendar cal = Calendar.getInstance(timeZone)
    cal.clear()
    cal.setLenient(false)
    cal.set(year, month - 1, day)

    return cal
}

def getCurrentYear(timeZone) {
    Calendar cal = Calendar.getInstance(timeZone)
    cal.setTimeInMillis(now())
    return cal.get(Calendar.YEAR)
}

def isLeapYear(year) {
    return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
}

def getCalendarYearDay(year, day, timeZone) {
    def daysInYear = isLeapYear(year) ? 366 : 365

    if(day < 1 || day > daysInYear) return null

    Calendar cal = Calendar.getInstance(timeZone)
    cal.clear()
    cal.setLenient(false)
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.DAY_OF_YEAR, day)

    return cal
}

def getCalendarWeekday(Calendar cal, timeZone) {
    if(!cal) return null

    def format = new java.text.SimpleDateFormat('EEEE')
    format.setTimeZone(timeZone)

    return format.format(cal.getTime())
}

def getCalendarDateString(Calendar cal, timeZone) {
    if(!cal) return null

    def format = new java.text.SimpleDateFormat('MM/dd/yyyy')
    format.setTimeZone(timeZone)

    return format.format(cal.getTime())
}

def getDateParts(dateString) {
    if(!dateString) return null

    def value = dateString.toString().trim().replace('\\', '/')

    // Julian date
    if(value ==~ /\d{1,3}/) return [julian: convertToInteger(value)]

    if(!(value ==~ /[\d\/.]+/)) return null

    def parts = value.split('[/.]', -1)

    if((parts.size() != 2 && parts.size() != 3) || parts.any { !(it ==~ /\d{1,4}/) }) {
        atomicState.dateErrors += 'Warning: Invalid date "' + dateString + '".<br>'
        return null
    }

    def month
    def day

    month = convertToInteger(parts[0])
    day = convertToInteger(parts[1])
    if(value.contains('.')) {
        day = convertToInteger(parts[0])
        month = convertToInteger(parts[1])
    }

    def year = ''
    if(parts.size() == 3) year = convertToInteger(parts[2])
    if(year.toString().length() != 0 && year.toString().length() != 2 && year.toString().length() != 4) {
        atomicState.dateErrors += 'Warning: Invalid year with "' + dateString + '".<br>'
        return null
    }
    if(year.toString().length() == 2) year += 2000

    return [month: month, day: day, year: year]
}

def setCalendarYear(Calendar source, year, timeZone) {
    if(!source) return null

    return getCalendar(
        year,
        source.get(Calendar.MONTH) + 1,
        source.get(Calendar.DAY_OF_MONTH),
        timeZone
    )
}

def processDates(includeDates, excludeDates, days, retrieveErrors = true, appId = app.id) {
    timeZone = location.timeZone
    currentYear = getCurrentYear(timeZone)

    def yearEnd = getCalendar(currentYear, 12, 31, timeZone)
    def daysInYear = yearEnd.get(Calendar.DAY_OF_YEAR)

    def includeList = []
    def excludeList = []

    atomicState.dateErrors = ''

    includeDates = cleanDateString(includeDates)
    excludeDates = cleanDateString(excludeDates)

    // No include dates means all days are initially included
    if(!includeDates) {
        includeList.addAll(getDaysInRange(getCalendar(currentYear, 1, 1, timeZone), getCalendar(currentYear, 12, 31, timeZone), currentYear))
    }

    // Process included dates
    if(includeDates) {
        def dateStringsList = includeDates.split(/[,;]/).findAll { it }

        dateStringsList.each { dateString ->
            if(dateString.contains('-')) {
                def rangeList = processDateStringRange(dateString, currentYear, timeZone, appId)
                if(rangeList) includeList.addAll(rangeList)
            }
            if(!dateString.contains('-')) {
                def cal = processDateStringIndividual(dateString, currentYear, timeZone, appId)

                if(cal && cal.get(Calendar.YEAR) == currentYear) {
                    includeList.add(cal.get(Calendar.DAY_OF_YEAR))
                }
            }
        }
    }

    // Process excluded dates
    if(excludeDates) {
        def dateStringsList = excludeDates.split(/[,;]/).findAll { it }

        dateStringsList.each { dateString ->
            if(dateString.contains('-')) {
                def rangeList = processDateStringRange( dateString, currentYear, timeZone, appId)
                if(rangeList) excludeList.addAll(rangeList)
            }
            if(!dateString.contains('-')) {
                def cal = processDateStringIndividual(dateString, currentYear, timeZone, appId)

                if(cal && cal.get(Calendar.YEAR) == currentYear) {
                    excludeList.add(cal.get(Calendar.DAY_OF_YEAR))
                }
            }
        }
    }

    // Days of the week
    if(days && !includeDates) {
        def daysList = []
        includeList = includeList.findAll{ day ->
            if(checkDateInDayList(day, days, currentYear, timeZone)) {
                excludeList.add(day)
            }
        }
    }
    includeList = includeList.findAll { !excludeList.contains(it) }
    finalList = includeList.unique().sort()
    

    if(!retrieveErrors) atomicState.remove('dateErrors')

    if(finalList.size() == daysInYear) return

    return finalList
}

def processDateStringIndividual(dateString, currentYear, timeZone, appId = app.id) {
    def parts = getDateParts(dateString)
    if(parts == null) return null

    if(parts.julian != null) {
        def cal = getCalendarYearDay(currentYear, parts.julian, timeZone)

        if(cal) return cal
        atomicState.dateErrors += 'Warning: Invalid Julian date "' + dateString + '".<br>'

        return
    }

    if(parts.year && parts.year != currentYear) return

    def cal = getCalendar(currentYear, parts.month, parts.day, timeZone)

    if(cal) return cal
    atomicState.dateErrors += 'Warning: Invalid date "' + dateString + '".<br>'
}

def processDateStringRange(dateString, currentYear, timeZone, appId = app.id) {
    def dates = dateString.split('-', -1)

    if(dates.size() != 2) {
        atomicState.dateErrors += 'Error: Invalid date range "' + dateString + '".<br>'
        return null
    }

    def firstParts = getDateParts(dates[0])
    def secondParts = getDateParts(dates[1])
    
    if(firstParts == null) return null
    if(secondParts == null) return null
    def listOfDays = []
    def firstDate
    def secondDate

    if(firstParts.julian && firstParts.month) {
        atomicState.dateErrors += 'Error: You can mix regular dates and julian days within the same date range in "' + dateString +'".<br>'
        return null
    }
    if(secondParts.julian && secondParts.month) {
        atomicState.dateErrors += 'Error: You can mix regular dates and julian days within the same date range in "' + dateString +'".<br>'
        return null
    }

    if(firstParts.julian || secondParts.julian) {
        def yearEnd = getCalendar(currentYear, 12, 31, timeZone)
        def daysInYear = yearEnd.get(Calendar.DAY_OF_YEAR)
        if(firstParts.julian > secondParts.julian) {
            listOfDays.addAll(getDaysInJulianRange(firstParts.julian, daysInYear))
            listOfDays.addAll(getDaysInJulianRange(1, secondParts.julian))
        }
        if(firstParts.julian <= secondParts.julian) listOfDays.addAll(getDaysInJulianRange(firstParts.julian, secondParts.julian))
        return listOfDays
    }

    if(firstParts.year && !secondParts.year) {
        atomicState.dateErrors += 'Error: Invalid date in range "' + dateString + '".<br>'
        return null
    }
    // Reverse date range
    if(firstParts.year && secondParts.year && firstParts.year > secondParts.year) {
        atomicState.dateErrors += 'Error: Invalid date range "' + dateString + '".<br>'
        return null
    }
    // Future range
    if(firstParts.year && firstParts.year > currentYear) return null
    // Past range
    if(secondParts.year && secondParts.year < currentYear) return null
    // No years, cross 12/31, getDaysInRange for both parts of year 
    if(!firstParts.year && !secondParts.year && (firstParts.month > secondParts.month || (firstParts.month == secondParts.month && firstParts.day > secondParts.day))){
        // end of year, first-12/31
        firstDate = getCalendar(currentYear, firstParts.month, firstParts.day, timeZone)
        secondDate = getCalendar(currentYear, 12, 31, timeZone)
        listOfDays.addAll(getDaysInRange(firstDate, secondDate, currentYear))

        // start of year, 1/1-second
        firstDate = getCalendar(currentYear, 1, 1, timeZone)
        secondDate = getCalendar(currentYear, secondParts.month, secondParts.day, timeZone)
        listOfDays.addAll(getDaysInRange(firstDate, secondDate, currentYear))

        return listOfDays
    }
    // Use Jan 1 for prior year start
    if(firstParts.year && firstParts.year < currentYear) {
        firstParts.day = 1
        firstParts.month = 1
    }
    // Use Dec 31 for future year end
    if(secondParts.year && secondParts.year > currentYear) {
        secondParts.day = 31
        secondParts.month = 12
    }

    // Construct and validate dates using their assigned years
    firstDate = getCalendar(currentYear, firstParts.month, firstParts.day, timeZone)
    secondDate = getCalendar(currentYear, secondParts.month, secondParts.day, timeZone)

    return getDaysInRange(firstDate, secondDate, currentYear)
}

def getDaysInRange(Calendar firstDate, Calendar secondDate, currentYear) {
    if(!firstDate) return null
    if(!secondDate) return null
    def cal = firstDate.clone() as Calendar
    def listOfDays = []

    while(!cal.after(secondDate)) {
        if(cal.get(Calendar.YEAR) == currentYear) {
            listOfDays << cal.get(Calendar.DAY_OF_YEAR)
        }
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }

    return listOfDays
}

def getDaysInJulianRange(firstDate, secondDate) {
    def listOfDays = []
    for(int i = firstDate; i <= secondDate; i++){
        listOfDays.add(i)
    }
    return listOfDays
}

def checkDateInDayList(dateValue, days, currentYear, timeZone) {
    if(dateValue == null) return false
    if(!days) return true

    def cal = getCalendarYearDay(currentYear, dateValue, timeZone)
    if(!cal) return false

    def weekday = getCalendarWeekday(cal, timeZone)

    return days.contains(weekday)
}

def checkNowInDayList(days, timeZone = location.timeZone) {
    if(!days) return true

    def cal = Calendar.getInstance(timeZone)
    cal.setTimeInMillis(now())

    return days.contains(getCalendarWeekday(cal, timeZone))
}

def cleanDateString(dateString) {
    if(!dateString) return null

    return dateString.toString()
        .replaceAll('\\\\', '/')
        .replaceAll('\\s', '')
}

def getDateProcessingErrors(appId = app.id) {
    def returnValue = atomicState.dateErrors
    atomicState.remove('dateErrors')
    return returnValue
}

// Returns true if now is between two times
// Returns true for null values
def checkNowBetweenTimes(startTime, stopTime, appId = app.id){
    if(startTime == null) return true
    if(stopTime == null) return true

    currentTime = getTimeOfDayInMillis(now(), appId)

    if(startTime == stopTime) return true

    if(startTime < currentTime && stopTime > currentTime) return true
    if(startTime < currentTime && stopTime < startTime) return true
    if(startTime > currentTime && stopTime > currentTime && startTime > stopTime) return true
}

def getPrintDateTimeFormat(datetime, appId = app.id){
    return new Date(datetime).format('h:mma MMM dd, yyyy', location.timeZone)
}

def getSunriseSunset(type, appId = app.id){
    if(!type) return
    if(type == 'sunrise') return getSunrise('', appId)
    if(type == 'sunset') return getSunset('', appId)
}

// Returns date/time of sunrise in format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
// negative is true of false
def getSunrise(offset = false, appId = app.id){
    if(offset) return getSunriseAndSunset().sunrise.getTime() + (offset * CONSTMinuteInMilli())
    if(!offset) return getSunriseAndSunset().sunrise.getTime()
}

// Returns date/time of sunset in format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
def getSunset(offset = false, appId = app.id){
    if(offset) return getSunriseAndSunset().sunset.getTime() + (offset * CONSTMinuteInMilli())
    if(!offset) return getSunriseAndSunset().sunset.getTime()
}

def checkToday(time, appId = app.id){
    if(time + CONSTDayInMilli() > now()) return true
}

def displayScheduleTip(){
    if(hideTips) return
    section(){
        scheduleText = 'Schedules allow performing actions on switches and lights (e.g. turn on, off, dim, set color, etc.) at specific times, \
and will run daily. Devices can be set to progressively change by setting both start and stop values for brightness or color. If a \
device is turned off, the schedule continues to run, so <i>when it is</i> turned on, it will do so at the correct level(s). You \
can also select specific weekdays and dates, and other factors like Mode. Schedules also support sunrise and sunset. Use cases:<br>\
        • Set a porch light to turn on at sunset and off at sunrise<br>\
        • Set room lights to turn on and get dimmer as it gets later in the evening<br>\
        • Set room lights to be dimmer as it gets later in the evening, when and if they are turned on<br>\
        • Set lights to turn off at night<br>\
        • Set a bedroom light to turn on in the morning, on weekdays (and progressively brighten for a couple minutes)<br>\
        • Set irrigation (or pool) controls for different times based on the season (by setting a schedule for each season, limited to those dates)<br>\
        • Turn off all devices when everyone leaves, within a certain time (or any time, by setting start time the same as stop time)<br>\
        • Turn on certain devices when someone arrives, within a certain time (or any time, by setting start time the same as stop time)<br>\
        • Set a color of a light based on who is home, based on the time (e.g. if a child is not home an hour after sunset, set a light to red)<br>\
        • Send a notification based on who is home (e.g. a child arriving/departing, during a certain time, or in general).'
        displayInfo(scheduleText)
    }
}

def displayPicoTip(){
    if(hideTips) return
    section(){
        picoText = 'The Pico app is designed for Lutron Caseta switches and Pico remotes. It may or may not work for other 2, 4 or 5 button devices. \
Actions include on, off, toggle, dim, brighten, and "resume schedule" (more on that below). It supports push, and hold (no double tap yet). \
It allows quickly setting up remotes to work (select a Pico, set a device, and save), or more advanced options of defining specific actions for each button, \
and/or defining different devices per button. It allows scheduling, such that a remote can do different things at different times, as well as presence. \
The "resume" option resets back to a schedule. For instance, if the brightness set by a schedule is overridden, selecting resume has the schedule take \
back over. Use cases:<br>\
        • Control a light or group of lights<br>\
        • Control any (smart) switched device (i. e. blinds, irrigation, fans, etc.)<br>\
        • With 5-button Picos, set center button to toggle the ceiling fan (and other buttons control the lights)<br>\
        • Using a 2 button Pico, control two bedside lamps by setting both as "toggle"<br>\
        • When turning on a bedroom lamp, turn off the living room (or porch) light<br>\
        • Set push to control one light, but hold to control lights in an adjacent room<br>\
        • Set push to dim, but hold to progressively dim<br>\
        • Set light switch to send a notification during the day as a honeypot for would be intruders (notifications aren\'t yet a feature, but soon)<br>\
        • Set switches for a pool pump or irrigation for pool cleaner/landscaper for their time and day, but repurpose the switch for outdoor lighting at other times<br>\
        • Schedule a porch light to be dim, but set the "on" button to set it to full brightness, and "off" button has it go back to dim per the schedule.'
        displayInfo(picoText)
    }
}

def displayMagicCubeTip(){
    if(hideTips) return
    section(){
        magicCubeText = 'MagicCubes are sold by Xiaomi under the Aqara brand. They are, aptly, cubes (and perhaps magic). Actions supported are flip 90, flip 180, shake, \
rotate left, and rotate right. (Support for side up number to be added.) Each action can be set to control different devices and/or perform different actions. Actions include on, off, dim, \
brighten, and resum schedule. The "resume" option grants control back to a schedule if the schedule is overridden (see Pico app note for more info).\
Use cases:<br>\
        • Control a light by flipping the Cube.<br>\
        • Control multiple devices, such as having a lamp toggle with flip 90, another lamp toggle with flip 180, and a ceiling fan toggle with shake.<br>\
        • When turning on a bedroom lamp, turn off the living room (or porch) light<br>\
        • Control light brightness with rotate right (brighten) and left (dim) (like a volume knob)<br>\
        • Control a (smart) media player volume... like a volume knob, as well as power, skip track, etc.<br>\
        • Train a pet to flip it to indicate to be let outside (notifications not yet supported).'
        displayInfo(magicCubeText)
    }
}
                                                                                                       
def checkLog(type, logLevel){
    switch(type) {
        case 'error':
        if(logLevel > 0) return true
        break
        case 'warn':
        if(logLevel > 1) return true
        break
        case 'info':
        if(logLevel > 2) return true
        break
        case 'trace':
        if(logLevel > 3) return true
        case 'debug':
        if(logLevel > 4) return true
        break
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber, type = 'trace', message, appId = app.id, isChild = null){
    logLevel = 5
    if(appId == app.id) logLevel = getLogLevel()
    if(appId != app.id){
        appLabel = 'new app'
        if(getChildAppById(appId)) appLabel = (getChildAppById(appId)).label
        if(getChildAppById(appId)) logLevel = (getChildAppById(appId)).getLogLevel()
    }
    
    if(!checkLog(type, logLevel)) return
    if(isChild)  text = '[' + appLabel + ' :: ' + lineNumber + '] -- ' + message
    if(!isChild && appId != app.id) text = '[' + appLabel + '] [Master :: ' + lineNumber + '] -- ' + message        // Within parent, but called from app
    if(!isChild && appId == app.id) text = '[Master :: ' + lineNumber + '] -- ' + message        // Within parent, and not called from app

    if(type == 'error') text = '<font color="red">' + text + '</font>'
    if(type == 'warn') text = '<font color="brown">' + text + '</font>'

    switch(type) {
        case 'error':
        log.error(text)
        return
        case 'warn':
        log.warn(text)
        return
        case 'info':
        log.info(text)
        return
        case 'debug':
        log.debug(text)
        return
        case 'trace':
        log.trace(text)
    }
}
