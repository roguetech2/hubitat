/***********************************************************************************************************************
*
*  Copyright (C) 2024 roguetech
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
*  Version: 0.4.1.27
*
***********************************************************************************************************************/

// Add tooltips for use caseswith apps
// Add toggle to disable them

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
    return 5
}

def displayLabel(text, width = 12){
    if(!text) return
    paragraph('<div style="background-color:#DCDCDC"><b>' + text + '</b></div>',width:width)
}

def displayInfo(text,noDisplayIcon = null, width=12){
    if(!text) return
    if(noDisplayIcon) paragraph('<div style="background-color:AliceBlue">' + text + '</div>',width:width)
    if(!noDisplayIcon) paragraph('<div style="background-color:AliceBlue">' + infoIcon + ' ' + text + '</div>',width:width)
    helpTip = ''
}

def displayError(text,noDisplayIcon = null, width=12){
    if(!text) return
    if(noDisplayIcon) paragraph('<div style="background-color:Bisque">' + text + '</div>',width:width)
    if(!noDisplayIcon) paragraph('<div style="background-color:Bisque">' + errorIcon  + ' ' + text + '</div>',width:width)
    errorMessage = ''
}

def displayWarning(text,noDisplayIcon = null, width=12){
    if(!text) return
    if(noDisplayIcon) paragraph('<div style="background-color:LemonChiffon">' + text + '</div>',width:width)
    if(noDisplayIcon) paragraph('<div style="background-color:LemonChiffon">' + warningIcon  + ' ' + text + '</div>',width:width)
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
        if(Child.label.startsWith('Time - ')) scheduleCount++
        if(Child.label.startsWith('Presence - ')) presenceCount++
        if(Child.label.startsWith('Pico - ')) picoCount++
        if(Child.label.startsWith('MagicCube - ')) magicCubeCount++
        if(Child.label.startsWith('Contact - ')) contactCount++
        if(Child.label.startsWith('Sensor - ')) sensorCount++
    }
}

def displayAllDevicesOption(){
    fieldName = 'allDevices'
    fieldTitle = 'Select all devices:'
    if(allDevices){
        section(){
    displayInfo('All devices should be selected. This is used to make the app suite of easier to use, but is not required. You can deselect any devices that are never used and/or any virtual devices.')
    input name: fieldName, title: fieldTitle, type: 'capability.*', multiple: true, filter:true,width:12,submitOnChange:true
        }
    }
    if(!allDevices) {
        
    input name: fieldName, title: fieldTitle, type: 'capability.*', multiple: true, filter:true,width:12,submitOnChange:true
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

def displayScheduleTip(){
    if(hideTips) return
    section(){
        scheduleText = 'Schedules allow performing actions on switches and lights (e.g. turn on, off, dim, set color, etc.) at specific times, \
and will run daily. Devices can be set to progressively change by setting both start and stop values for brightness or color. If a \
device is turned off, the schedule continues to run, so <i>when it is</i> turned on, it will do so at the correct level(s). You \
can also select specific days and months, and other factors like Mode. Schedules also support sunrise and sunset. Use cases:<br>\
        • Set a porch light to turn on at sunset and off at sunrise<br>\
        • Set room lights to turn on and get dimmer as it gets later in the evening<br>\
        • Set room lights to be dimmer as it gets later in the evening, when and if they are turned on<br>\
        • Set lights to turn off at night<br>\
        • Set a bedroom light to turn on in the morning, on weekdays (and progressively brighten for a couple minutes)<br>\
        • Set irrigation (or pool) controls for different times based on the season (by setting a schedule for each season, limited to those months)<br>\
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
    atomicState.logLevel = getLogLevel()
    atomicState.masterInstalled = true
    initialize()
}

def updated() {
    atomicState.logLevel = getLogLevel()
    initialize()
}

def initialize() {
    _getTable()
    atomicState.logLevel = getLogLevel()
}

// Returns app name with app title prepended
def appendAppTitle(appName,appTitle){
    //Compare length of name (eg "test") to appTitle length minus 6 (eg "Master - Time - " minus "Master - "; "Time - " is min length)
    if(appName.length() < appTitle.length() - 6){
        returnValue = appTitle.substring(9,appTitle.length()) + ' - ' + appName
        //Compare first part of name (eg "Testing") to middle part of appTitle (eg "Master - Time" minus "Master - " plus " - ")
    } else if(appName.substring(0,appTitle.length() - 9) != appTitle.substring(9,appTitle.length())){
        returnValue = appTitle.substring(9,appTitle.length()) + ' - ' + appName
    } else {
        returnValue = appName
    }

    return returnValue
}

// Lock or unlock a group of locks
def multiLock(action, multiDevice, childLabel = 'Master'){
    multiDevice.each{
        singleLock(action,it,childLabel)
    }
}

// Lock or unlock a single lock
def singleLock(action, singleDevice, childLabel = 'Master'){
    if(action == 'lock'){
        singleDevice.lock()
    } else if(action == 'unlock'){
        singleDevice.unlock()
    } else {
        putLog(391,'error','Invalid value for action "' + action + '" sent to singleLock function',childLabel,'True')
    }
    putLog(393,'info',action + 'ed ' + singleDevice,childLabel,'True')
}


// Returns true if level value is either valid or null
def validateLevel(value, childLabel='Master'){
    if(!value) return true
    value = value as int
        if(value < 1) return false
        if(value > 100) return false
    return true
}

// Returns true if temp value is either valid or null
def validateTemp(value, childLabel='Master'){
    if(!value) return true
        value = value as int
            if(value < 1800) return false
            if(value > 6500) return false
            return true
}


// Returns true if temp value is either valid or null
def validateHue(value, childLabel='Master'){
    if(!value) return true
    value = value as int
    if(value < 1) return false

    if(value > 360) return false
    return true
}

// Returns true if temp value is either valid or null
def validateSat(value, childLabel='Master'){
    if(!value) return true
    value = value as int
        if(value < 1) return false
        if(value > 100) return false
        return true
}

def validateMultiplier(value, childLabel='Master'){
    if(value){
        if(value < 1 || value > 100){
            putLog(438,'error',"ERROR: Multiplier $value is not valid",childLabel,True)
            return
        }
    }
    return value
}

/* ************************************************************************ */
/* TO-DO: Clean it up, and schedule it every day, with an alert.            */
/* ************************************************************************ */
def test(){
    // Get current time
    varNow = now()
    sensors.each{
        // lastCheckin only available for Xiaomi drivers
        // Could use it.lastUpdate, but maybe it's just not used much
        // So ... maybe subscribe and create state variable??
        if(it.currentLastCheckin){
            // Covert lastCheckin to Unix epoch timestamp
            //		long epoch = it.currentLastCheckin.getTime()

            diff = (varNow - it.currentLastCheckin.toBigInteger()) / 1000 / 60 / 60
            if(diff > 24) {
                numDays = Math.round(diff / 24)
                if(!phone){
                    if($numDays > 1){
                        log.debug "$it hasn't done anything in $numDays days."
                    } else {
                        log.debug "$it hasn't done anything in $numDays day."
                    }
                } else {
                    if($numDays > 1){
                        sendText(phone,"$it hasn't done anything in $numDay days.")
                    } else {
                        sendText(phone,"$it hasn't done anything in $numDay day.")
                    }
                }
            }
        } else {
            log.debug "$it not tested."
        }
    }
}











































def checkIsDimmable(singleDevice, childLabel='Master'){
    if(singleDevice) return singleDevice.hasCapability('SwitchLevel')
}

def checkIsTemp(singleDevice, childLabel='Master'){
    if(singleDevice) return singleDevice.hasCapability('ColorTemperature')
}

def checkIsColor(singleDevice, childLabel='Master'){
    if(singleDevice) return singleDevice.hasCapability('ColorMode')
}

def checkIsFan(singleDevice, childLabel='Master'){
    if(singleDevice) return singleDevice.hasCapability('FanControl')
}
// Test state of a single switch
def checkIsOn(singleDevice,childLabel='Master'){
    // If no deviceState, set it
    if(atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state' == 'on') return true
    if(atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state' == 'off') return false 
    if(singleDevice.currentValue('switch') == 'on') return true
}

// Used by Sensor
// Test state of a group of switches
// Return true if any are on
def checkAnyOnMulti(multiDevice,childLabel='Master'){
    if(!multiDevice) return
    returnStatus = false
    multiDevice.each{singleDevice->
        if(checkIsOn(singleDevice,childLabel)) returnStatus = true
    }
    return returnStatus
}

def checkPeopleHome(multiDevice,childLabel = 'Master'){
    if(!multiDevice) return true
    peopleHome = true
    multiDevice.each{it ->
        if(it.currentPresence != 'present') peopleHome = false
    }
    return peopleHome
}

def checkNoPeopleHome(multiDevice,childLabel = 'Master'){
    if(!multiDevice) return true
    nooneHome = true
    multiDevice.each{it ->
        if(it.currentPresence == 'present') nooneHome = false
    }
    return nooneHome
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
def settingHiRezHue(){
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

def CONSTDeviceActionDelayMillis(childLabel = 'Master'){
    returnValue = 200
    putLog(622,'debug','Pausing for ' + returnValue + 'ms.',childLabel,'True')
    return returnValue
}

def CONSTProgressiveDimmingDelayTimeMillis(){
    return 750
}
def CONSTDeviceDefaultBrightness(){
    return 100
}

def CONSTDeviceDefaultTemp(){
    return 3250
}

def getTimeOfDayInMillis(time,childLabel = 'Master'){
    if(!time) return
    timeAtMidnight = new Date(time).clearTime().getTime()
    return time - timeAtMidnight
}

def getDatetimeFromTimeInMillis(time,childLabel = 'Master'){
    if(!time) return
    timeAtMidnight = new Date(now()).clearTime().getTime()
    return time + timeAtMidnight
}

// Returns app name with app title prepended
def appendChildAppTitle(appName,appTitle){
    //Compare length of name (eg "test") to appTitle length minus 6 (eg "Master - Time - " minus "Master - "; "Time - " is min length)
    if(appName.length() < appTitle.length() - 6) return appTitle.substring(9,appTitle.length()) + ' - ' + appName
    //Compare first part of name (eg "Testing") to middle part of appTitle (eg "Master - Time" minus "Master - " plus " - ")
    if(appName.substring(0,appTitle.length() - 9) != appTitle.substring(9,appTitle.length())) return appTitle.substring(9,appTitle.length()) + ' - ' + appName
    return appName
}

def getAppIdForDeviceFromTable(singleDeviceId,type,childLabel = 'Master'){
    if(!singleDeviceId) return
    return atomicState.'devices'?."${singleDeviceId}"?."${type}"?.'appId'
}

def getTimeForDeviceFromTable(singleDeviceId,type,childLabel = 'Master'){
    if(!singleDeviceId) return
    return atomicState.'devices'?."${singleDeviceId}"?."${type}"?.'time'
}

def _getChildAppIdFromLabel(childLabel = 'Master'){
    if(childLabel == 'Master') return
    childApps.find { Child ->
        if(Child.label == childLabel) returnValue = Child.id
    }
    return returnValue
}

// Creates a list of devices capabilities for the sensor app
// by matching the abilities of any device in the allDevices setting
// against a list of all supported sensor types provided in matchMap
// matchMap is a list of maps [[attribute: 'motion', name: 'Motion Sensor'], [attribute: 'contact', name: 'Contact Sensor']] etc (with other key:values)
// It could return attribute:name pairs
def getInstalledCapabilitiesList(fullList){
    if(!allDevices) return

    deviceCount = 0
    matchList = []
    completedList = []
    allDevices.each {singleDevice ->
        deviceMatch = false
        singleDevice.capabilities.each {capability ->   // Capabilities are capitalized for the device, NOT in the fullList
            if(!completedList.contains(capability.name)) {
                fullList.find { it ->
                    if (it.capability == capability.name.uncapitalize()) {
                        matchList.add(it.capability)
                    }
                }
                completedList.add(capability.name)
            }
        }
        deviceCount++
    }
    return matchList
}

// Returns true if today is in $days map
// Returns true for null values
def checkNowInDayList(days,childLabel='Master'){
    if(!days) return true
    
    dateFormat = new java.text.SimpleDateFormat('EEEE')
    dateFormat.setTimeZone(location.timeZone)
    dayToday = dateFormat.format(new Date())
    if(days.contains(dayToday)) return true
}

// Returns true if today is in $months map
// Returns true for null values
def checkNowInMonthList(months,childLabel='Master'){
    if(!months) return true

    monthToday = new Date().getMonth() + 1
    if(months.contains(monthToday.toString())) return true
}

// Checks if current datetime is beteeen two datetimes
def checkNowBetweenScheduledStartStopTimes(startTime, stopTime,childLabel='Master'){
    if(!startTime) return
    if(!stopTime) return
    if(startTime > stopTime) {
        if(now() < startTime) {
            startTime -= CONSTDayInMilli()
        } else {
            stopTime += CONSTDayInMilli()
        }
    }
 
    if(now() < startTime) return
    if(now() > stopTime) return

    return true
}

// Returns true if now is between two times
// Returns true for null values
def checkNowBetweenTimes(timeStart, timeStop,childLabel='Master'){
    if(!timeStart) return
    if(!timeStop) return

    varNow = now()
    if(varNow < timeStart) return
    if(varNow > timeStop) return

    return true
}

def checkTempWithinVariance(originalTemp, newTemp, colorMode, childLabel='Master'){
    if(!originalTemp) return
    if(!newTemp) return
    if(colorMode == 'RGB') return
    if(originalTemp == newTemp) return true
    tempDifference = Math.abs(originalTemp - newTemp)
    tolerance = originalTemp / 100
    if (tempDifference < tolerance) return true
}

def getPrintDateTimeFormat(datetime){
    return new Date(datetime).format('h:mma MMM dd, yyyy', location.timeZone)
}

def getSunriseSunset(type, childLabel='Master'){
    if(!type) return
    if(type == 'sunrise') return getSunrise('',childLabel)
    if(type == 'sunset') return getSunset('',childLabel)
}

// Returns date/time of sunrise in format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
// negative is true of false
def getSunrise(offset = false, childLabel='Master'){
    if(offset) return getSunriseAndSunset().sunrise.getTime() + (offset * CONSTMinuteInMilli())
    if(!offset) return getSunriseAndSunset().sunrise.getTime()
}

// Returns date/time of sunset in format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
def getSunset(offset = false,childLabel='Master'){
    if(offset) return getSunriseAndSunset().sunset.getTime() + (offset * CONSTMinuteInMilli())
    if(!offset) return getSunriseAndSunset().sunset.getTime()
}

def checkToday(time,childLabel = 'Master'){
    if(time + CONSTDayInMilli() > now()) return true
}

def resumeDeviceScheduleMulti(multiDevice,childLabel='Master'){
    if(!multiDevice) return
    multiDevice.each{singleDevice->
        // Needs to just set appId to... scheduleId or null?
        _resumeDeviceScheduleSingle(singleDevice,childLabel)
    }
}

// Should be added to Table
// Lock or unlock a group of locks
def setLockMulti(multiDevice, action, childLabel = 'Master'){
    if(!multiDevice) return
    multiDevice.each{singleDevice->
        setLockSingle(singleDevice,action,childLabel)
    }
}

// Lock or unlock a single lock
def _setLockSingle(singleDevice, action, childLabel = 'Master'){
    if(action == 'lock') singleDevice.lock()
    if(action == 'unlock') singleDevice.unlock()
    putLog(813,'info',action + 'ed ' + singleDevice,childLabel,'True')
}

// Sets devices to match state
def setDeviceMulti(multiDevice,childLabel = 'Master'){
    if(!multiDevice) return
    anyChange = false
    // Separate device loops for if there are multiple devices, don't pause per device, pause per action
    multiDevice.each{singleDevice->
        deviceChange = setDeviceBrightnessSingle(singleDevice,childLabel)
        if(!anyChange) anyChange = deviceChange
    }
    if(anyChange) pauseExecution(CONSTDeviceActionDelayMillis(childLabel))
    
    anyChange = false
    multiDevice.each{singleDevice->
        deviceChange = setDeviceTempSingle(singleDevice,childLabel)
        if(!anyChange) anyChange = deviceChange
    }
    if(anyChange) pauseExecution(CONSTDeviceActionDelayMillis(childLabel))
    
    anyChange = false
    multiDevice.each{singleDevice->
        deviceChange = setDeviceHueSingle(singleDevice,childLabel)
        if(!anyChange) anyChange = deviceChange
    }
    if(anyChange) pauseExecution(CONSTDeviceActionDelayMillis(childLabel))
    
    anyChange = false
    multiDevice.each{singleDevice->
        deviceChange = setDeviceSatSingle(singleDevice,childLabel)
        if(!anyChange) anyChange = deviceChange
    }

    if(anyChange) pauseExecution(CONSTDeviceActionDelayMillis(childLabel))
    multiDevice.each{singleDevice->
        setDeviceStateSingle(singleDevice,childLabel)
    }
}

// Sets devices to match state
def setDeviceSingle(singleDevice,childLabel = 'Master'){
    if(!singleDevice) return
    if(!atomicState.'devices'?."${singleDevice.id}") return

    deviceChange = setDeviceBrightnessSingle(singleDevice,childLabel)
    if(deviceChange) pauseExecution(CONSTDeviceActionDelayMillis(childLabel))
    deviceChange = false

    deviceChange = setDeviceTempSingle(singleDevice,childLabel)
    if(deviceChange) pauseExecution(CONSTDeviceActionDelayMillis(childLabel))
    deviceChange = false

    deviceChange = setDeviceHueSingle(singleDevice,childLabel)
    if(deviceChange) pauseExecution(CONSTDeviceActionDelayMillis(childLabel))
    deviceChange = false

    deviceChange = setDeviceSatSingle(singleDevice,childLabel)
    if(deviceChange) pauseExecution(CONSTDeviceActionDelayMillis(childLabel))

    setDeviceStateSingle(singleDevice,childLabel)
}

def setDeviceBrightnessSingle(singleDevice, childLabel = 'Master'){
    if(!singleDevice) return
    if(!checkIsDimmable(singleDevice,childLabel)) return

    if(!atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state') return       // Should set state to current device state?
    if(atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state' == 'off') return

    newLevel = atomicState.'devices'?."${singleDevice.id}"?.'brightness'?.'currentLevel'
    if(atomicState.'devices'?."${singleDevice.id}"?.'brightness'?.'stopTime'){
        if(atomicState.'devices'?."${singleDevice.id}"?.'brightness'?.'stopTime' < now()) newLevel = null    //If from expired schedule, it doesn't count
    }
    
    if(!newLevel) {
        putLog(889,'Using default brightness of ' + CONSTDeviceDefaultBrightness() + ' for ' + singleDevice,childLabel,'True')
        newLevel = CONSTDeviceDefaultBrightness()
    }
    oldLevel = _getDeviceCurrentLevel(singleDevice,'brightness',childLabel)
    if(newLevel == oldLevel) return

    if(checkIsFan(singleDevice)) singleDevice.setSpeed(newLevel)
    if(!checkIsFan(singleDevice)) singleDevice.setLevel(newLevel)
    putLog(896,'info','Set brightenss of ' + singleDevice + ' to ' + newLevel + ' (from ' + oldLevel + ')',childLabel,'True')
    return true
}

// Need to confirm colorMode?
def setDeviceTempSingle(singleDevice,childLabel = 'Master'){
    if(!singleDevice) return
    if(!checkIsTemp(singleDevice,childLabel)) return
    
    if(!atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state') return
    if(atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state' == 'off') return

    if(atomicState.'devices'?."${singleDevice.id}"?.'hue'?.'currentLevel' && atomicState.'devices'?."${singleDevice.id}"?.'hue'?.'stopTime' > now()) return
    if(atomicState.'devices'?."${singleDevice.id}"?.'sat'?.'currentLevel' && atomicState.'devices'?."${singleDevice.id}"?.'sat'?.'stopTime' > now()) return
    
    newLevel = atomicState.'devices'?."${singleDevice.id}"?.'temp'?.'currentLevel'
    
    if(atomicState.'devices'?."${singleDevice.id}"?.'temp'?.'stopTime'){
        if(atomicState.'devices'?."${singleDevice.id}"?.'temp'?.'stopTime' < now()) newLevel = null    //If from expired schedule, it doesn't count
    }
    if(!newLevel) {
        putLog(918,'Using default color temperature of ' + CONSTDeviceDefaultTemp() + ' for ' + singleDevice,childLabel,'True')
        newLevel = CONSTDeviceDefaultTemp()
    }
    
    if(checkTempWithinVariance(_getDeviceCurrentLevel(singleDevice,'temp',childLabel),newLevel,singleDevice.currentColorMode,childLabel) && singleDevice.currentColorMode == 'CT') return

    singleDevice.setColorTemperature(newLevel)
    putLog(925,'info','Set temperature of ' + singleDevice + ' to ' + newLevel + ' (from ' + oldLevel + ')',childLabel,'True')
    return true
}

// Need to confirm colorMode?
def setDeviceHueSingle(singleDevice,childLabel = 'Master'){
    if(!singleDevice) return
    if(!checkIsColor(singleDevice,childLabel)) return

    if(!atomicState.'devices'?."${singleDevice.id}"?.'hue'?.'currentLevel') return
    if(atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state' == 'off') return
    
    if(atomicState.'devices'?."${singleDevice.id}"?.'hue'?.'stopTime'){
        if(atomicState.'devices'?."${singleDevice.id}"?.'hue'?.'stopTime' < now()) return    //If from expired schedule, it doesn't count
    }

    newLevel = atomicState.'devices'?."${singleDevice.id}"?.'hue'?.'currentLevel'
    
    if(!settings['hiRezHue']) newLevel = Math.round(newLevel / 3.6)
    if(newLevel == _getDeviceCurrentLevel(singleDevice,'hue',childLabel) && singleDevice.currentColorMode == 'RGB') return

    singleDevice.setHue(newLevel)
    putLog(946,'info','Set hue of ' + singleDevice + ' to ' + newLevel + ' (from ' +  _getDeviceCurrentLevel(singleDevice,'hue',childLabel) + ')',childLabel,'True')
    return true
}

// Need to confirm colorMode?
def setDeviceSatSingle(singleDevice,childLabel = 'Master'){
    if(!singleDevice) return
    if(!checkIsColor(singleDevice,childLabel)) return

    if(!atomicState.'devices'?."${singleDevice.id}"?.'sat'?.'currentLevel') return
    if(atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state' == 'off') return

    if(atomicState.'devices'?."${singleDevice.id}"?.'sat'?.'time'){
        if(atomicState.'devices'?."${singleDevice.id}"?.'sat'?.'time' + CONSTDayInMilli() < now()) return    //If from yesterday, it doesn't count
    }
    
    if(atomicState.'devices'?."${singleDevice.id}"?.'sat'?.'stopTime'){
        if(atomicState.'devices'?."${singleDevice.id}"?.'sat'?.'stopTime' < now()) return    //If from expired schedule, it doesn't count
    }
    
    newLevel = atomicState.'devices'?."${singleDevice.id}"?.'sat'?.'currentLevel'
    
    if(newLevel == _getDeviceCurrentLevel(singleDevice,'sat',childLabel) && singleDevice.currentColorMode == 'RGB') return

    singleDevice.setSaturation(newLevel)
    putLog(972,'info','Set saturation of ' + singleDevice + ' to ' + newLevel + ' (from ' +  _getDeviceCurrentLevel(singleDevice,'sat',childLabel) + ')',childLabel,'True')
    return true
}

def getStateChangeAppId(singleDevice, appId, childLabel = 'Master'){
    if(!singleDevice) return
    if(!appId) return
    return atomicState.'devices'?."${singleDevice.id}"?.'state'?.'appId'
}

def setDeviceStateSingle(singleDevice,childLabel = 'Master'){
    if(!singleDevice) return
    if(!atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state') return
    if(singleDevice.currentValue('switch') == atomicState.'devices'."${singleDevice.id}".'state'.'state') return
    
    if(atomicState.'devices'."${singleDevice.id}".'state'.'state' == 'on') {
        singleDevice.on()
        putLog(989,'info','Turned on ' + singleDevice,childLabel,'True')
        return true
    }
    if(atomicState.'devices'."${singleDevice.id}".'state'.'state' == 'off') {
        singleDevice.off()
        putLog(994,'info','Turned off ' + singleDevice,childLabel,'True')
        return true
    }
}

def _getDeviceCurrentLevel(singleDevice,type,childLabel = 'Master'){
    if(type == 'brightness') {
        if(checkIsFan(singleDevice)) return singleDevice.currentSpeed
        if(checkIsFan(singleDevice)) return singleDevice.currentLevel as Integer
    }
    if(type == 'temp') return singleDevice.currentColorTemperature as Integer
    if(type == 'hue') return singleDevice.currentHue as Integer
    if(type == 'sat') return singleDevice.currentSaturation as Integer
}

// Do reverse, and wrap around
def _computeIncrementalHueRange(singleDevice,childLabel = 'Master'){
    if(atomicState.'devices'."${singleDevice.id}".'hue'?.'hueDirection' == 'reverse') return 360 - Math.abs(atomicState.'devices'."${singleDevice.id}".'hue'.'stopLevel' + atomicState.'devices'."${singleDevice.id}".'hue'.'startLevel')
    if(atomicState.'devices'."${singleDevice.id}".'hue'?.'hueDirection' != 'reverse') return Math.abs(atomicState.'devices'."${singleDevice.id}".'hue'.'stopLevel' - atomicState.'devices'."${singleDevice.id}".'hue'.'startLevel')
}

def getStateMapSingle(singleDevice,action,appId,childLabel = 'Master'){
    if(!action) return
    if(!singleDevice) return
    if(action != 'on' && action != 'off' && action != 'toggle') return
    if(action == 'toggle'){
        action = 'on'
        if(!atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state') {
            if(singleDevice.currentValue('switch') == 'on') action = 'off'
        }
        if(atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state' == 'on') action = 'off'
    }
    return ['state':['state':action,'time':now(),'appId':appId]]    // appId used by sensor
}

def getLevelMap(type,level,appId,stopTime = '', childLabel = 'Master'){
    if(!type) return
    if(!level) return
    if(!appId) return
    if(type != 'brightness' && type != 'temp' && type != 'hue' && type != 'sat') return
    
    if(stopTime) return [(type):['currentLevel':level,'time':now(),'appId':appId,'stopTime':stopTime,'appType':_getAppTypeFromId(appId,childLabel)]]    
    if(!stopTime) return [(type):['currentLevel':level,'time':now(),'appId':appId,'appType':_getAppTypeFromId(appId,childLabel)]]
}

def _getAppTypeFromId(appId,childLabel = 'Master'){
    if(!appId) return 'manual'
    childApps.find { Child ->
        if (Child.id == appId) {
            // These should be pared down to just 'manual', 'time' and maybe 'auto'
            // Leaving for testing
            if(Child.label.startsWith('Time - ')) returnValue = 'time'
            if(Child.label.startsWith('Presence - ')) returnValue = 'presence'
            if(Child.label.startsWith('Pico - ')) returnValue = 'pico'
            if(Child.label.startsWith('MagicCube - ')) returnValue = 'magicCube'
            if(Child.label.startsWith('Contact - ')) returnValue = 'contact'
            if(Child.label.startsWith('Sensor - ')) returnValue = 'sensor'
        }
    }
    if(!returnValue) return 'manual'
    return returnValue
}

def updateTableCapturedState(singleDevice,action,childLabel = 'Master'){
    if(atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state' == action) return
    putLog(1059, 'Captured state change for ' + singleDevice + ' to turn ' + action + ' (table was ' + atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state' + '; actually was ' + singleDevice.currentState + ')',childLabel,'True')
    stateMap = getStateMapSingle(singleDevice,action,'manual',childLabel)
    mergeMapToTable(singleDevice.id,stateMap,childLabel)
    if(action == 'on') setDeviceSingle(singleDevice,childLabel)    // With device on, set levels

}

def updateTableCapturedLevel(singleDevice,type,childLabel = 'Master'){
    if(!atomicState.'devices'?."${singleDevice.id}"?."${type}"?.'currentLevel') return
    if(atomicState.'devices'?."${singleDevice.id}"?."${type}"?.'currentLevel' == _getDeviceCurrentLevel(singleDevice,type,childLabel)) return
    if(type == 'hue' && !settings['hiRezHue']) {
        if(Math.round(atomicState.'devices'?."${singleDevice.id}"?."${type}"?.'currentLevel' * 3.6) == _getDeviceCurrentLevel(singleDevice,type,childLabel)) return
    }
    
    currentLevel = _getDeviceCurrentLevel(singleDevice,type,childLabel)
    putLog(1074,'trace','Captured manual ' + type + ' change for ' + singleDevice + ' to turn ' + currentLevel + ' (table was ' + atomicState.'devices'?."${singleDevice.id}"?."${type}"?.'currentLevel' + ')',childLabel,'True')
    levelMap = getLevelMap(type,currentLevel,'manual','',childLabel)
    mergeMapToTable(singleDevice.id,levelMap,childLabel)
}

def _getNextLevelDimmable(singleDevice, action, childLabel='Master'){
    if(checkIsFan(singleDevice,childLabel)) return _getNextLevelFan(singleDevice,action,childLabel)
    if(!checkIsDimmable(singleDevice,childLabel)) return
    if(action != 'dim' && action != 'brighten') return
    if(atomicState.'devices'."${singleDevice.id}"?.'brightness'?.'currentLevel') level = atomicState.'devices'."${singleDevice.id}".'brightness'.'currentLevel'
    if(!atomicState.'devices'."${singleDevice.id}"?.'brightness'?.'currentLevel') level = _getDeviceCurrentLevel(singleDevice,'brightness',childLabel)

    newLevel = level
    if(level < 1) level = 1
    if(level > 100) level = 100
    if(action == 'dim' && level < 2) return
    if(action ==  'brighten' && level > 99) return

    childApps.find {Child->
        if(Child.id == appId) dimSpeed = Child.getDimSpeed()
    }
    if(!dimSpeed){
        dimSpeed = 1.2
    }

    oldLevel = level
    if (action == 'dim'){
        newLevel = Math.round(convertToInteger(level) / dimSpeed)
        if (newLevel == level) newLevel = newLevel - 1
        if(newLevel < 1) newLevel = 1
    }
    if (action == 'brighten'){
        newLevel = Math.round(convertToInteger(level) * dimSpeed)
        if (newLevel == level) newLevel = newLevel + 1
        if(newLevel > 100) newLevel = 100
    }

    return newLevel
}

def _getNextLevelFan(singleDevice, action, childLabel='Master'){
    if(!checkIsFan(singleDevice,childLabel)) return
    if(action != 'dim' && action != 'brighten') return
    if(atomicState.'devices'."${singleDevice.id}"?.'brightness'?.'currentLevel') level = atomicState.'devices'."${singleDevice.id}".'brightness'.'currentLevel'
    if(level && level.isNumber()) level = _getDeviceCurrentLevel(singleDevice,'brightness',childLabel) // Should convert ranges to speeds, but this is easier
    if(!atomicState.'devices'."${singleDevice.id}"?.'brightness'?.'currentLevel') level = _getDeviceCurrentLevel(singleDevice,'brightness',childLabel)

    if(action == 'brighten'){
        if(level == 'low' || level == 'medium-low') return 'medium'
        if(level == 'medium' || level == 'medium-high') return 'high'
        if(!level || level == 'off') return 'low'
    }
    if(!level) return
    if(level == 'high') return 'medium'
    if(level == 'medium-high') return 'medium'
    if(level == 'medium') return 'low'
    if(level == 'medium-low') return 'low'
}

// Sets cron string based on:
//      timeMillis = time from now in milliseconds, or
//      timeValue = time in timestamp
// Checks child app getDisabled
// If active, passes cronString, functionName, and formated parameters back to child app, to set the schedule
def scheduleChildEvent(timeMillis = '',timeValue = '',functionName,parameters,appId){
    // noPerformDisableCheck is reversed, so that null does not equal False
    // true = not check, False = check
    if(!appId) return
    if(!timeMillis && !timeValue) return
    if(timeMillis < 0) {
        putLog(1144,'warn','scheduleChildEvent given negative timeMillis from appId ' + appId + ' (' + functionName + ' timeMillis = ' + timeMillis + ')',childLabel,'True')
        return
    }
    if(timeValue) {
        def currentTimeMillis = now()
        def targetTimeMillis = new Date(timeValue).time
        timeMillis = targetTimeMillis - currentTimeMillis
        if(timeMillis < 0) timeMillis += CONSTDayInMilli()
    }
    if(parameters) parametersMap = [data:[' + parameters + ']]

    childApps.find {Child->
        if(Child.id == appId) {
                if(!functionName) {
                    putLog(1158,'warn','scheduleChildEvent given null for functionName from appId ' + appId + ' (timeMillis = ' + timeMillis + ', timeValue = ' + TimeValue + ')',Child.label,'True')
                    return
                }
                Child.setScheduleFromParent(timeMillis,functionName,parametersMap)
                if(parameters) parameters = ' (with parameters: ' + parameters + ')'
                putLog(1157,'debug','Scheduled ' + functionName + parameters + ' for ' + new Date(timeMillis + now()).format('hh:mma MM/dd ') + ' (in ' + Math.round(timeMillis / 1000) + ' seconds)',Child.label,'True')
        }
    }
}

def changeMode(mode, childLabel = 'Master'){
    if(location.mode == mode) return
    message = 'Changed Mode from ' + oldMode + ' to '
    setLocationMode(mode)
    putLog(1172,'debug',message + mode,childLabel,'True')
}

// Send SMS text message to $phone with $message
//SMS IS NO LONGER SUPPORTED
def sendPushNotification(phone, message, childLabel = 'Master'){
    def now = new Date()getTime()
    seconds = (now - atomicState.contactLastNotification) / 1000
    if(seconds < 361) {
        putLog(1181,'info','Did not send push notice for ' + evt.displayName + ' ' + evt.value + 'due to notification sent ' + seconds + ' ago.',childLabel,'True')
        return
    }

    atomicState.contactLastNotification = now
    speechDevice.find{it ->
        if(it.id == deviceId) {
            if(it.deviceNotification(message)) putLog(1188,'debug','Sent phone message to ' + phone + ' "' + message + '"',childLabel,'True')
        }
    }
}

def sendVoiceNotification(deviceId,message, childLabel='Master'){
    if(!deviceId)  return
    speechDevice.find{it ->
        if(it.id == deviceId) {
            if(it.speak(text)) putLog(1197,'debug','Played voice message on ' + deviceId + ' "' + message + '"',childLabel,'True')
        }
    }
}

def addMaps(map1, map2 , map3 = '', map4 = '', map5 = ''){
    returnMap = [:]
    if(map1) returnMap += map1
    if(map2) returnMap += map2
    if(map3) returnMap += map3
    if(map4) returnMap += map4
    if(map5) returnMap += map5
    return returnMap
}

//THIS CAN BE REWRITTEN TO OVERWRITE WITH SUBKEYS FROM NEW MAP

// newMap in format of [[type:'brightness/temp/sat/hus',time:datetime as long,stopTime:datetime as long,addId:nnnn,appType:string],ETC]
def mergeMapToTable(singleDeviceId, newMap, childLabel = 'Master'){
    if(!singleDeviceId) return
    if(!newMap) return
    
    if(atomicState.'devices') mainMap = atomicState.'devices'
    if(!atomicState.'devices') mainMap = [:]

    if(!mainMap?."${singleDeviceId}") mainMap[(singleDeviceId)] = [:]
    newMap.each{newKey,newValue->
            mainMap."${singleDeviceId}"."${newKey}" = newValue
    }
    if(mainMap?."${singleDeviceId}") atomicState.'devices' =  mainMap
}

def clearTableKey(singleDeviceId,type, childLabel = 'Master'){
    if(!singleDeviceId) return
    if(!atomicState.'devices'?."${singleDeviceId}"?."${type}") return

    tempMap = atomicState.'devices'
    tempMap."${singleDeviceId}".remove(type)
    atomicState.'devices' = tempMap
    return true
}

def convertToInteger(value, childLabel = 'Master'){
    if(value instanceof Integer) return value
    if(value instanceof Long) return value
    if(value instanceof String && value.isInteger()) return value.toInteger()
    return
}

def checkLog(type,logLevel){
    switch(type) {
        case 'error':
        if(atomicState.logLevel > 0) return true
        break
        case 'warn':
        if(atomicState.logLevel > 1) return true
        break
        case 'info':
        if(atomicState.logLevel > 2) return true
        break
        case 'trace':
        if(atomicState.logLevel > 3) return true
        case 'debug':
        if(atomicState.logLevel > 4) return true
        break
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber, type = 'trace', message, childLabel = 'Master', app = False,logLevel = null){
    if(!app && !logLevel) return
    if(app) logLevel = getLogLevel()
    if(!checkLog(type,logLevel)) return
    if(!app && !type) return
    errorText = ''
    if(type == 'error') errorText = '<font color="red">'
    if(type == 'warn') errorText = '<font color="brown">'
    appText = ''
    if(app) appText = '[Master] '
    if(childLabel != 'Master') appText += '[' + childLabel + '] '
    if(lineNumber) lineText = '(line ' + lineNumber + ') '
    if(message) messageText = '-- ' + message
    logMessage = errorText + appText + lineText  + messageText
    if(type == 'error' || type == 'warn') logMessage += '</font>'

    switch(type) {
        case 'error':
        log.error(logMessage)
        return
        case 'warn':
        log.warn(logMessage)
        return
        case 'info':
        log.info(logMessage)
        return
        case 'debug':
        log.debug(logMessage)
        return
        case 'trace':
        log.trace(logMessage)
    }
}
