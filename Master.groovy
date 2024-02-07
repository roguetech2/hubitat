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
*  Version: 0.4.01
*
***********************************************************************************************************************/

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
    paragraph('<div style="background-color:#DCDCDC"><b>' + text + ':</b></div>',width:width)
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

def addFieldName(text,fieldName){
    if(!fieldName) return
    if(getLogLevel() != 5) return text
    return text + ' [' + fieldName + ']'
}

preferences {
    page(name: 'mainPage')
}

def mainPage() {
    
    return dynamicPage(name: 'mainPage', title: '', install: true, uninstall: true) {
        if(!atomicState.masterInstalled) {
            section('Click Done.') {
            }

        } else {
            if(presenceDevice || pushNotificationDevice || speechDevice || hiRezHue || colorStaging) hidden = true
            title = 'Device Settings'
            if(hidden && (!presenceDevice || !pushNotificationDevice || !speechDevice)) title += moreOptions
                section(hideable: true, hidden: hidden, title) {
                    input 'presenceDevice', 'capability.presenceSensor', title: 'Select all people:', multiple: true, required: false, submitOnChange:true
                    paragraph "<div style=\"background-color:AliceBlue\">$infoIcon These people will be used for \"anyone\" and \"everyone\" conditions in any presence-based condition, and allow birthday options.</div>"
                    input 'pushNotificationDevice', 'capability.notification', title: 'Select push notification device(s):', multiple: true, required: false, submitOnChange:true

                    input 'speechDevice', 'capability.speechSynthesis', title: 'Select text-to-speech notification device(s):', multiple: true, required: false, submitOnChange:true
                    paragraph "<div style=\"background-color:AliceBlue\">$infoIcon This will be used for voice alerts.</div>"
                    input name: 'hiRezHue', type: 'bool', title: 'Enable Hue in degrees (0-360)', defaultValue: false, submitOnChange:true
                    displayInfo('Select if light devices have been set to degrees. Leave unselected by default, but failure to match device settings may result in hue settings not working correctly.')
                    input name: 'colorStaging', type: 'bool', title: 'Enable color pre-staging', defaultValue: false, submitOnChange:true
                    displayInfo('Select if light devices have been set for color pre-staging. This allows the color of lights to be set prior to turning on, but this app can handle that internally. Leave unselected by default, but failure to match device settings may result in devices not turning on correctly.')
                    
                }
            section(){}

                    /*
// Used for flash-alerts; nothing else
input "colorLights", "capability.switchLevel", title: "Select all color lights:", multiple: true, required: false, submitOnChange:true
paragraph "<div style=\"background-color:AliceBlue\">$infoIcon These color lights will be used for flashing alerts.</div>"
*/


                }
                /* ********************************************* */
                /* TO-DO: Finish BDay code. Not complete.        */
                /* Try to move it to child app, using this code: */
                /* https://community.hubitat.com/t/parent-function-to-return-settings-state-data-to-child-app/2261/3 */
                /* ********************************************* */
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

        scheduleCount = 0
        presenceCount = 0
        picoCount = 0
        magicCubeCount = 0
        contactCount = 0
        humidityCount = 0
        childApps.each {Child->
            if(Child.label.length() > 6 && Child.label.substring(0,7) == 'Time - ') {
                scheduleCount++
                    } else if(Child.label.length() > 10 && Child.label.substring(0,11) == 'Presence - ') {
                presenceCount++
                    } else if(Child.label.length() > 6 && Child.label.substring(0,7) == 'Pico - ') {
                picoCount++
                    } else if(Child.label.length() > 11 && Child.label.substring(0,12) == 'MagicCube - ') {
                magicCubeCount++
                    } else if(Child.label.length() > 9 && Child.label.substring(0,10) == 'Contact - ') {
                contactCount++
                    } else if(Child.label.length() > 10 && Child.label.substring(0,11) == 'Humidity - ') {
                humidityCount++
                    }
        }

                title = 'Click to add '
                if(scheduleCount == 0) {
                    title += 'a <b>schedule</b>'
                } else {
                    title += "or edit <b>schedules</b> ($scheduleCount total)"
                }
                section(hideable: true, hidden: true, title) {
                    app(name: 'childApps', appName: 'Master - Time', namespace: 'master', title: 'New Schedule', multiple: true)
                }

                title = 'Click to add '
                if(presenceCount == 0) {
                    title += 'a <b>presence</b> setting'
                } else {
                    title += "or edit <b>presence</b> settings ($presenceCount total)"
                }
                section(hideable: true, hidden: true, title) {
                    app(name: 'childApps', appName: 'Master - Presence', namespace: 'master', title: 'New Presence', multiple: true)
                }

                title = 'Click to add '
                if(picoCount == 0) {
                    title += 'a <b>Pico</b>'
                } else {
                    title += "or edit <b>Picos</b> ($picoCount total)"
                }
                section(hideable: true, hidden: true, title) {
                    app(name: 'childApps', appName: 'Master - Pico', namespace: 'master', title: 'New Pico', multiple: true)
                }

                title = 'Click to add '
                if(magicCubeCount == 0) {
                    title += 'a <b>MagicCube</b>'
                } else {
                    title += "or edit <b>MagicCubes</b> ($magicCubeCount total)"
                }
                section(hideable: true, hidden: true, title) {
                    app(name: 'childApps', appName: 'Master - MagicCube', namespace: 'master', title: 'New MagicCube', multiple: true)
                }

                title = 'Click to add '
                if(contactCount == 0) {
                    title += 'a <b>contact/door sensor</b>'
                } else {
                    title += "or edit <b>contact/door sensors</b> ($contactCount total)"
                }
                section(hideable: true, hidden: true, title) {
                    app(name: 'childApps', appName: 'Master - Contact', namespace: 'master', title: 'New Contact Sensor', multiple: true)
                }

                title = 'Click to add '
                if(humidityCount == 0) {
                    title += 'a <b>humidity sensor</b>'
                } else {
                    title += "or edit <b>humidity sensors</b> ($humidityCount total)"
                }
                section(hideable: true, hidden: true, title) {
                    app(name: 'childApps', appName: 'Master - Humidity', namespace: 'master', title: 'New Humidity Sensor', multiple: true)
                }

    }
}

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
        putLog(331,'error','Invalid value for action "' + action + '" sent to singleLock function',childLabel,'True')
    }
    putLog(333,'info',action + 'ed ' + singleDevice,childLabel,'True')
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
def validateHue(value, hiRezHue, childLabel='Master'){
    if(!value) return true
    value = value as int
    if(value < 1) return false

    if(hiRezHue && value > 360) return false
    if(!hiRezHue && value > 100) return false
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

// Returns true if neither hue nor sat are invalid
// Returns true if both are null values
def validateHueSat(hue,sat, childLabel='Master'){
    if(hue && (hue < 1 || hue > 100)) return
    if(sat && (sat < 1 || sat > 100)) return
    return true
}

def validateMultiplier(value, childLabel='Master'){
    if(value){
        if(value < 1 || value > 100){
            putLog(386,'error',"ERROR: Multiplier $value is not valid",childLabel,True)
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

// NOT USED
// Test state of a group of switches
// Return true if any are on
def _checkAllOnMulti(multiDevice,childLabel='Master'){
    if(!multiDevice) return
    returnStatus = true
    multiDevice.each{singleDevice->
        if(!checkIsOn(singleDevice,childLabel)) returnStatus = false
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
def settingHiRezHue(multiDevice,childLabel = 'Master'){
    return settings['hiRezHue']
}

// Should (really must) be greater than CONSTDeviceActionDelayMillis * 4
def CONSTScheduleActiveFrequencyMilli(childLabel = 'Master'){
    return 2000
}

def CONSTScheduleInactiveFrequencyMilli(childLabel = 'Master'){
    return 30000
}

def CONSTDayInMilli(){
    return 86400000
}

// NOT USED
def CONSTHourInMilli(){
    return 3600000
}

def CONSTMinuteInMilli(){
    return 60000
}

def CONSTDeviceActionDelayMillis(){
    return 500
}

// Returns app name with app title prepended
def appendChildAppTitle(appName,appTitle){
    //Compare length of name (eg "test") to appTitle length minus 6 (eg "Master - Time - " minus "Master - "; "Time - " is min length)
    if(appName.length() < appTitle.length() - 6) return appTitle.substring(9,appTitle.length()) + ' - ' + appName
    //Compare first part of name (eg "Testing") to middle part of appTitle (eg "Master - Time" minus "Master - " plus " - ")
    if(appName.substring(0,appTitle.length() - 9) != appTitle.substring(9,appTitle.length())) return appTitle.substring(9,appTitle.length()) + ' - ' + appName
    return appName
}

def _getChildAppIdFromLabel(childLabel = 'Master'){
    if(childLabel == 'Master') return
    childApps.find { Child ->
        if(Child.label == childLabel) returnValue = Child.id
    }
    return returnValue
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

// Returns true if now is between two times
// Need to allow checking if time spans midnight
// Returns true for null values
def checkNowBetweenTimes(timeStart, timeStop,childLabel='Master'){
    if(!timeStart) return true
    if(!timeStop) return true

    varNow = now()
    if(varNow < timeStart) return false
    if(varNow > timeStop) return false

    return true
}

def getPrintDateTimeFormat(datetime){
    return new Date(datetime).format('h:mma MMM dd, yyyy', location.timeZone)
}

// Only needed to set Start Time
// Must be within CONSTDayInMilli to now
// If abs(dateValue - now() < CONSTDayInMilli then valid
// If stop time has passed, incremented in (schedule) setStopTime
def setTimeAsIn24Hours(dateValue,childLabel='Master'){
    timeFromNow = now() - dateValue
    if(Math.abs(timeFromNow) < CONSTDayInMilli()) return dateValue
        while (Math.abs(timeFromNow) > CONSTDayInMilli()) {
            if(timeFromNow < 0) dateValue += CONSTDayInMilli()
            if(timeFromNow > 0) dateValue -= CONSTDayInMilli()
            timeFromNow = dateValue - now()
        }
    return timeFromNow
}

def getSunriseSunset(type, childLabel='Master'){
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

//NOT USED
// Used for "resume" function
def _rescheduleIncrementalMulti(multiDevice,childLabel='Master'){
    multiDevice.each{singleDevice->
        _rescheduleIncrementalSingle(singleDevice,childLabel)
    }
    return
}

def resumeDeviceScheduleMulti(multiDevice,childLabel='Master'){
    if(!multiDevice) return
    multiDevice.each{singleDevice->
        _resumeDeviceScheduleSingle(singleDevice,childLabel)
    }
}

//DEPRICATED?
def _resumeDeviceScheduleSingle(singleDevice,childLabel='Master'){
    if(!singleDevice) return
    childApps.each { Child ->   // COuld be multiple sechedules per device
        if(Child.label.startsWith('Time - ')){
            Child.getSetting('device').find{childDevice->
                if(singleDevice.id == childDevice.id){
                    Child.setDailySchedules()
                    putLog(690,'info','Resuming schedule for ' + singleDevice + ' (' + Child.label + ')',childLabel,True)
                }
            }
            /*
            Child.find { ChildDevice ->
                if(singleDevice.id == ChildDevice.id){
            log.debug 'singleDevice.id ' + singleDevice.id + ' ChildDevice: ' + Child.label + ' ' + ChildDevice.id + ' ' + (singleDevice.id == ChildDevice.id)

                    Child.setDailySchedules()
                    putLog(690,'info','Resuming schedule for ' + singleDevice + ' (' + ChildDevice.label + ')',childLabel,True)
                }
            }
            */
        }
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
    putLog(710,'info',action + 'ed ' + singleDevice,childLabel,'True')
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
    if(anyChange) pauseExecution(CONSTDeviceActionDelayMillis())
    anyChange = false
    multiDevice.each{singleDevice->
        deviceChange = setDeviceTempSingle(singleDevice,childLabel)
        if(!anyChange) anyChange = deviceChange
    }
    if(anyChange) pauseExecution(CONSTDeviceActionDelayMillis())
    anyChange = false
    multiDevice.each{singleDevice->
        deviceChange = setDeviceHueSingle(singleDevice,childLabel)
        if(!anyChange) anyChange = deviceChange
    }
    if(anyChange) pauseExecution(CONSTDeviceActionDelayMillis())
    anyChange = false
    multiDevice.each{singleDevice->
        deviceChange = setDeviceSatSingle(singleDevice,childLabel)
        if(!anyChange) anyChange = deviceChange
    }
    if(anyChange) pauseExecution(CONSTDeviceActionDelayMillis())
    multiDevice.each{singleDevice->
        setDeviceStateSingle(singleDevice,childLabel)
    }
}
// Sets devices to match state
def setDeviceSingle(multiDevice,childLabel = 'Master'){
    if(!multiDevice) return
    if(!atomicState.'devices'?."${singleDevice.id}") return

    deviceChange = setDeviceBrightnessSingle(singleDevice,childLabel)
    if(deviceChange) pauseExecution(CONSTDeviceActionDelayMillis())
    deviceChange = false

    deviceChange = setDeviceTempSingle(singleDevice,childLabel)
    if(deviceChange) pauseExecution(CONSTDeviceActionDelayMillis())
    deviceChange = false

    deviceChange = setDeviceHueSingle(singleDevice,childLabel)
    if(deviceChange) pauseExecution(CONSTDeviceActionDelayMillis())
    deviceChange = false

    deviceChange = setDeviceSatSingle(singleDevice,childLabel)
    if(deviceChange) pauseExecution(CONSTDeviceActionDelayMillis())

    setDeviceStateSingle(singleDevice,childLabel)
}

def setDeviceBrightnessSingle(singleDevice,childLabel = 'Master'){
    if(!singleDevice) return
    if(!atomicState.'devices'?."${singleDevice.id}"?.'brightness'?.'currentLevel') return
    if(atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state' == 'off') return
    if(!checkIsDimmable(singleDevice,childLabel)) return
    newLevel = atomicState.'devices'?."${singleDevice.id}"?.'brightness'?.'currentLevel'
    if(_getDeviceCurrentLevel(singleDevice,action,childLabel) == newLevel) return
    singleDevice.setLevel(newLevel)
    putLog(757,'info','Set brightenss of ' + singleDevice + ' to ' + newLevel,childLabel,'True')
    return true
}
// Need to confirm colorMode
def setDeviceTempSingle(singleDevice,childLabel = 'Master'){
    if(!singleDevice) return
    if(!atomicState.'devices'?."${singleDevice.id}"?.'temp'?.'currentLevel') return
    if(atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state' == 'off') return
    if(!checkIsTemp(singleDevice,childLabel)) return
    newLevel = atomicState.'devices'?."${singleDevice.id}"?.'temp'?.'currentLevel'
    oldLevel = _getDeviceCurrentLevel(singleDevice,action,childLabel)
    if(oldLevel == newLevel) return
    tempDifference = Math.abs(newLevel - oldLevel)
    tolerance = oldLevel * 0.05
    if (tempDifference < tolerance) return
    singleDevice.setColorTemperature(newLevel)
    putLog(757,'info','Set color temperature of ' + singleDevice + ' to ' + newLevel,childLabel,'True')
    return true
}

// Need to confirm colorMode
def setDeviceHueSingle(singleDevice,childLabel = 'Master'){
    if(!singleDevice) return
    if(!atomicState.'devices'?."${singleDevice.id}"?.'hue'?.'currentLevel') return
    if(atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state' == 'off') return
    if(!checkIsDimmable(singleDevice,childLabel)) return
    if(settings['hiRezHue']) newLevel = atomicState.'devices'."${singleDevice.id}".'hue'.'currentLevel'
    if(!settings['hiRezHue']) newLevel = Math.round(atomicState.'devices'."${singleDevice.id}".'hue'.'currentLevel' / 3.6)
    log.debug atomicState.'devices'."${singleDevice.id}"
    if(_getDeviceCurrentLevel(singleDevice,action,childLabel) == newLevel) return
    singleDevice.setHue(newLevel)
    putLog(757,'info','Set hue of ' + singleDevice + ' to ' + newLevel,childLabel,'True')
    return true
}
def setDeviceSatSingle(singleDevice,childLabel = 'Master'){
    if(!singleDevice) return
    if(!atomicState.'devices'?."${singleDevice.id}"?.'sat'?.'currentLevel') return
    if(atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state' == 'off') return
    if(!checkIsDimmable(singleDevice,childLabel)) return
    newLevel = atomicState.'devices'?."${singleDevice.id}"?.'sat'?.'currentLevel'
    if(_getDeviceCurrentLevel(singleDevice,action,childLabel) == newLevel) return
    singleDevice.setSaturation(newLevel)
    putLog(757,'info','Set saturation of ' + singleDevice + ' to ' + newLevel,childLabel,'True')
    return true
}
def setDeviceStateSingle(singleDevice,childLabel = 'Master'){
    if(!singleDevice) return
    if(!atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state') return
    if(atomicState.'devices'."${singleDevice.id}".'state'.'state' == 'on') {
        if(singleDevice.currentValue('switch') == 'on') return
        singleDevice.on()
        putLog(757,'info','Turned on ' + singleDevice,childLabel,'True')
        return true
    }
    if(atomicState.'devices'."${singleDevice.id}".'state'.'state' == 'off') {
        if(singleDevice.currentValue('switch') == 'off') return
        singleDevice.off()
        putLog(757,'info','Turned off ' + singleDevice,childLabel,'True')
        return true
    }
}
DEPRICATED
// Sets device to match state
def setOldDeviceSingle(singleDevice,childLabel = 'Master'){
    if(!singleDevice) return
    if(!atomicState.'devices'?."${singleDevice.id}") return
    if(atomicState.'devices'."${singleDevice.id}"?.'state'?.'state' == 'on') {
        //Convert to runInMillis? Would need to get device from app from appId
        if(atomicState.'devices'."${singleDevice.id}"?.'brightness'?.'currentLevel'){
            newLevel = atomicState.'devices'."${singleDevice.id}".'brightness'.'currentLevel'
            if(performDeviceAction(singleDevice,'brightness',newLevel,childLabel)) pauseExecution(CONSTDeviceActionDelayMillis())
        }
        if(atomicState.'devices'."${singleDevice.id}"?.'temp'?.'currentLevel'){
            currentValue = _getDeviceCurrentLevel(singleDevice,'temp',childLabel)
            newLevel = atomicState.'devices'."${singleDevice.id}".'temp'.'currentLevel'
            tempDifference = Math.abs(newLevel - currentValue)
            tolerance = currentValue * 0.05
            if (tempDifference > tolerance) {  // device level isn't "actually" changing
                if(performDeviceAction(singleDevice,'temp',newLevel,childLabel)) pauseExecution(CONSTDeviceActionDelayMillis())
            }
        }
        if(atomicState.'devices'."${singleDevice.id}"?.'hue'?.'currentLevel'){
            newLevel = atomicState.'devices'."${singleDevice.id}".'hue'.'currentLevel'
            if(performDeviceAction(singleDevice,'hue',newLevel,childLabel)) pauseExecution(CONSTDeviceActionDelayMillis())
        }
        if(atomicState.'devices'."${singleDevice.id}"?.'sat'?.'currentLevel'){
            newLevel = atomicState.'devices'."${singleDevice.id}".'sat'.'currentLevel'
            if(performDeviceAction(singleDevice,'sat',newLevel,childLabel)) pauseExecution(CONSTDeviceActionDelayMillis())
        }
    }
    if(atomicState.'devices'."${singleDevice.id}"?.'state'?.'state'){

        newLevel = atomicState.'devices'."${singleDevice.id}".'state'.'state'
        performDeviceAction(singleDevice,'state',newLevel,childLabel)
    }
}

// action expects 'state', 'brightness', 'temp', 'hue', 'sat', or 'color'
def performDeviceAction(singleDevice,action,value,childLabel = 'Master'){
    if(!singleDevice) return
    if(!atomicState.'devices'?."${singleDevice.id}") {
        putLog(757,'info','No table entry for ' + singleDevice,childLabel,'True')
        return
    }
    if(action == 'state') {
        if(value == 'on') {
            if(singleDevice.currentValue('switch') == 'on') return
            if(!atomicState.'devices'?."${singleDevice.id}"?.'state'.'state') return
            singleDevice.on()
            putLog(757,'info','Turned on ' + singleDevice,childLabel,'True')
            return true
        }
        if(value == 'off') {
            if(singleDevice.currentValue('switch') == 'off') return
            if(!atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state') return
            singleDevice.off()
            putLog(757,'info','Turned off ' + singleDevice,childLabel,'True')
            return true
        }
    }
    if(action == 'brightness') {
        if(!checkIsDimmable(singleDevice,childLabel)) return
        if(!atomicState.'devices'?."${singleDevice.id}"?.'brightness'?.'currentLevel') return
        if(_getDeviceCurrentLevel(singleDevice,action,childLabel) == value) return
        singleDevice.setLevel(value)
        putLog(757,'info','Set brightenss of ' + singleDevice + ' to ' + value,childLabel,'True')
        return true
    }
    if(action == 'temp') {
        if(!checkIsTemp(singleDevice,childLabel)) return
            if(!atomicState.'devices'?."${singleDevice.id}"?.'temp'?.'currentLevel') return
            if(_getDeviceCurrentLevel(singleDevice,action,childLabel) == value) return // check variance
            singleDevice.setColorTemperature(value)
        putLog(757,'info','Set color temperature of ' + singleDevice + ' to ' + value,childLabel,'True')
            return true
    }
    if(action == 'hue') {
        if(!checkIsColor(singleDevice,childLabel)) return
        if(!atomicState.'devices'?."${singleDevice.id}"?.'hue'?.'currentLevel') return
        if(_getDeviceCurrentLevel(singleDevice,action,childLabel) == value) return
        singleDevice.setHue(value)
        putLog(757,'info','Set hue of ' + singleDevice + ' to ' + value,childLabel,'True')
        return true
    }
    if(action == 'sat') {
        if(!checkIsColor(singleDevice,childLabel)) return
        if(!atomicState.'devices'?."${singleDevice.id}"?.'sat'?.'currentLevel') return
        if(_getDeviceCurrentLevel(singleDevice,action,childLabel = 'Master') == value) return
        singleDevice.setSaturation(value)
        putLog(757,'info','Set saturation of ' + singleDevice + ' to ' + value,childLabel,'True')
        return true
    }
    if(action == 'color') {
        if(!checkIsColor(singleDevice,childLabel)) return
        singleDevice.setColor(value)
        return true
    }
    putLog(797,'error','Invalid action for ' + singleDevice + ' of ' + action + ' with value ' + value + ' sent to performDeviceAction',childLabel,'True')
}

// DEPRECATED
// Turns on off based on devices Table
def _setDeviceStateSingle(singleDevice,childLabel = 'Master'){
    if(!singleDevice) return
    if(!atomicState.'devices'?."${singleDevice.id}"?.'state') return
    if(singleDevice.currentValue('switch') == atomicState.'devices'."${singleDevice.id}".'state'.'state') return
    if(atomicState.'devices'."${singleDevice.id}".'state'.'state' == 'on') {
        putLog(731,'info','Turned on ' + singleDevice + (singleDevice.on()),childLabel,'True')
        performDeviceAction(singleDevice,'on',null,childLabel)
        singleDevice.on()
        return true
    }
    if(atomicState.'devices'."${singleDevice.id}".'state'.'state' == 'off') {
        putLog(736,'info','Turned off ' + singleDevice,childLabel,'True')
        
        performDeviceAction(singleDevice,'on',null,childLabel)
        return true
    }
}


//DEPRICATED?
// type expects 'brightness' or 'temp'
// Sets table.priorLevel to device.currentLevel
// Sets table.time to now()
// Sets device to currentLevel
// hue and sat are _setDeviceColorLevelsSingle

// Need to change this to use option Level paramter of color map ([hue:, sat:, level:])
def _setDeviceLevelsSingle(singleDevice,type,childLabel='Master'){
    if(!singleDevice) return
    if(atomicState.'devices'."${singleDevice.id}"?.'state'?.'state' != 'on') return
    
    if(type == 'color') return _setDeviceColorLevelsSingle(singleDevice,type,childLabel)   // split out for 'color' being 'hue' and 'sat'
    
    if(!atomicState.'devices'?."${singleDevice.id}"?."${type}"?.'currentLevel') return

    if(type == 'brightness' && !checkIsDimmable(singleDevice,childLabel)) return
    if(type == 'temp' && !checkIsTemp(singleDevice,childLabel)) return
    currentValue = _getDeviceCurrentLevel(singleDevice,type,childLabel)
    newLevel = atomicState.'devices'."${singleDevice.id}"."${type}".'currentLevel'
    if(newLevel == currentValue) return  // device level isn't changing
    if(type == 'temp'){
        tempDifference = Math.abs(newLevel - currentValue)
        tolerance = currentValue * 0.05
        if (tempDifference <= tolerance) return  // device level isn't changing
    }
    atomicState.'devices'."${singleDevice.id}"?."${type}"?.'priorLevel' = currentValue
    atomicState.'devices'."${singleDevice.id}"?."${type}"?.'time' = now()
    if(type == 'brightness') singleDevice.setLevel(newLevel)
    if(type == 'temp') {
        singleDevice.setColorTemperature(newLevel)
        //if(singleDevice.currentColorMode == 'RGB') singleDevice.currentColorMode == 'CT'    // Probably need a pause here? But not sure if it's automatic
    }
    putLog(777,'info','Set ' + type + ' of ' + singleDevice + ' to ' + atomicState.'devices'."${singleDevice.id}"."${type}".'currentLevel',childLabel,'True')

    return true
}

//DEPRECATED?
// type expects 'color' (for consistency, and could loopback into _setDeviceLevelsSingle)
// Sets table.priorLevel to device.currentLevel
// Sets table.time to now()
// Sets device to currentLevel
def _setDeviceColorLevelsSingle(singleDevice,type,childLabel='Master'){
    if(type != 'color') return
    if(!singleDevice) return
    if(!checkIsColor(singleDevice,childLabel)) return

    hueLevel = atomicState.'devices'."${singleDevice.id}"?.'hue'?.'currentLevel'
    if(hueLevel && hueLevel != _getDeviceCurrentLevel(singleDevice,'hue',childLabel)) hueChange = true  // device level is changing
    if(!hueLevel) hueLevel = _getDeviceCurrentLevel(singleDevice,'hue',childLabel)
    
    satLevel = atomicState.'devices'."${singleDevice.id}"?.'sat'?.'currentLevel'
    if(satLevel && satLevel != _getDeviceCurrentLevel(singleDevice,'sat',childLabel)) satChange = true  // device level is changing
    if(!satLevel) satLevel = _getDeviceCurrentLevel(singleDevice,'sat',childLabel)
    
    if(!hueChange && !satChange) return
    
    colorMap = ['hue':hueLevel,'saturation':satLevel]
    singleDevice.setColor(colorMap)

    //if(singleDevice.currentColorMode == 'CT') singleDevice.currentColorMode == 'RGB'    // Probably need a pause here? But not sure if it's automatic

    putLog(819,'info','Set color of ' + singleDevice + ' to ' + colorMap,childLabel,'True')
    return true
}          

def _getDeviceCurrentLevel(singleDevice,type,childLabel = 'Master'){
    if(type == 'brightness') return singleDevice.currentLevel as Integer
    if(type == 'temp') return singleDevice.currentColorTemperature as Integer
    if(type == 'hue') return singleDevice.currentHue as Integer
    if(type == 'sat') return singleDevice.currentSaturation as Integer
}

// type expects 'brightness', 'temp', 'hue', 'sat'
// Updates state to current scheduled settings
// Need to check if manual
def updateTableIncrementalMulti(multiDevice,type,childLabel = 'Master'){
    if(!multiDevice) return
    multiDevice.each{singleDevice->
        updateTableIncrementalSingle(singleDevice,type,childLabel)
    }
    //setDeviceMultiState(multiDevice,childLabel)
}

// type expects 'brightness', 'temp', 'hue', 'sat'
def updateTableIncrementalSingle(singleDevice, type, childLabel = 'Master') {
    if (!singleDevice) return
    if (!atomicState?.'devices'?."${singleDevice.id}"?."${type}"?.'startTime') return
    if (!atomicState?.'devices'?."${singleDevice.id}"?."${type}"?.'stopTime') return
    //if (!state?.'devices'?."${singleDevice.id}"?."${type}"?.'totalSeconds') return
    if (!atomicState?.'devices'?."${singleDevice.id}"?."${type}"?.'startLevel') return
    if (!atomicState?.'devices'?."${singleDevice.id}"?."${type}"?.'stopLevel') return

    // Needs to check if manual
    // Check if the schedule is active (wrong mode or disabled)
    childApps.each { Child ->
        if (Child.id == atomicState.'devices'."${singleDevice.id}"."${type}".'appId'.toInteger()) levelScheduleActive = Child.getScheduleActive()
    }
    if (!levelScheduleActive) {
        putLog(857, 'warn', "$singleDevice ${type} data = " + atomicState.'devices'."${singleDevice.id}"."${type}" + " for schedule id " + atomicState.'devices'."${singleDevice.id}"."${type}".'appId' + " but schedule isn't active", childLabel, true)
        return
    }
    incrementLevel = _computeIncrementalLevelSingle(singleDevice,type,childLabel)
    if(incrementLevel) atomicState.'devices'."${singleDevice.id}"."${type}".'currentLevel' = incrementLevel
    return true
}
//DEPRECATED
// type expects 'brightness', 'temp', 'hue', 'sat'
// With Hue, checks reverse
def _computeIncrementalLevelSingle(singleDevice,type,childLabel = 'Master'){
    if(atomicState.'devices'."${singleDevice.id}"."${type}".'stopTime' == atomicState.'devices'."${singleDevice.id}"."${type}".'startTime') return

    if(atomicState.'devices'?."${singleDevice.id}"?."${type}"?.'appType' != 'time') return    // Checks for manual override
    // If no seconds elapsed
    //if (currentSeconds == atomicState.'devices'."${singleDevice.id}"."${type}".'startSeconds') resultLevel = atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel'

    // Calculate dynamic value
    elapsedSeconds = Math.round((now() - atomicState.'devices'."${singleDevice.id}"."${type}".'startTime') / 1000)
    totalSeconds = Math.round((atomicState.'devices'."${singleDevice.id}"."${type}".'stopTime' - atomicState.'devices'."${singleDevice.id}"."${type}".'startTime') / 1000)
    if (elapsedSeconds == 0) elapsedSeconds = 1
    //remainingSeconds = Math.round((atomicState.'devices'."${singleDevice.id}"."${type}".'stopTime' - atomicState.'devices'."${singleDevice.id}"."${type}".'startTime') / 1000)
    percentComplete = elapsedSeconds / totalSeconds
    if(type == 'hue') levelRange = _computeIncrementalHueRange(singleDevice,childLabel)
    if(type != 'hue') levelRange = Math.abs(atomicState.'devices'."${singleDevice.id}"."${type}".'stopLevel' - atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel')
    remainingLevel = Math.round(levelRange * percentComplete)
    if (totalSeconds - elapsedSeconds < CONSTScheduleActiveFrequencyMilli(childLabel) / 1000) {
        atomicState.'devices'."${singleDevice.id}"."${type}".'startTime' = null
        atomicState.'devices'."${singleDevice.id}"."${type}".'stopTime' = null
        //atomicState.'devices'."${singleDevice.id}"."${type}".'totalSeconds' = null
        atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel' = null
        atomicState.'devices'."${singleDevice.id}"."${type}".'stopLevel' = null
        if(atomicState.'devices'."${singleDevice.id}"."${type}"?.'reverse') atomicState.'devices'."${singleDevice.id}"."${type}"?.'reverse' = null
        // Deschedule and reschedule incremental
        return
    }
    if(atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel' < atomicState.'devices'."${singleDevice.id}"."${type}".'stopLevel') forward = True
    if(atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel' > atomicState.'devices'."${singleDevice.id}"."${type}".'stopLevel') forward = False
    if(atomicState.'devices'."${singleDevice.id}"."${type}"?.'hueDirection' == 'reverse') forward = !forward
    if(atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel' < atomicState.'devices'."${singleDevice.id}"."${type}".'stopLevel') resultLevel = atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel' + remainingLevel
    if(atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel' > atomicState.'devices'."${singleDevice.id}"."${type}".'stopLevel') resultLevel = atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel' - remainingLevel
    return resultLevel
}
// type expects 'brightness', 'temp', 'hue', 'sat'
// With Hue, checks reverse
def getIncrementalLevelSingle(singleDevice,type,childLabel = 'Master'){
    if(!atomicState.'devices'?."${singleDevice.id}"?."${type}"?.'startTime') return
    if(!atomicState.'devices'?."${singleDevice.id}"?."${type}"?.'stopTime') return
    if(!atomicState.'devices'?."${singleDevice.id}"?."${type}"?.'startLevel') return
    if(!atomicState.'devices'?."${singleDevice.id}"?."${type}"?.'stopLevel') return
    if(atomicState.'devices'."${singleDevice.id}"?."${type}"?.'stopTime' == atomicState.'devices'."${singleDevice.id}"."${type}".'startTime') return
    if(!atomicState.'devices'."${singleDevice.id}"?."${type}"?.'startLevel' == atomicState.'devices'."${singleDevice.id}"."${type}".'stopLevel') return

    if(atomicState.'devices'?."${singleDevice.id}"?."${type}"?.'appType' != 'time') return    // Checks for manual override
    // If no seconds elapsed
    //if (currentSeconds == atomicState.'devices'."${singleDevice.id}"."${type}".'startSeconds') resultLevel = atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel'

    // Calculate dynamic value
    elapsedSeconds = Math.round((now() - atomicState.'devices'."${singleDevice.id}"."${type}".'startTime') / 1000)
    
    totalSeconds = Math.round((atomicState.'devices'."${singleDevice.id}"."${type}".'stopTime' - atomicState.'devices'."${singleDevice.id}"."${type}".'startTime') / 1000)
    
    if (elapsedSeconds == 0) elapsedSeconds = 1
    //remainingSeconds = Math.round((atomicState.'devices'."${singleDevice.id}"."${type}".'stopTime' - atomicState.'devices'."${singleDevice.id}"."${type}".'startTime') / 1000)
    percentComplete = elapsedSeconds / totalSeconds
    if(type == 'hue') levelRange = _computeIncrementalHueRange(singleDevice,childLabel)
    if(type != 'hue') levelRange = Math.abs(atomicState.'devices'."${singleDevice.id}"."${type}".'stopLevel' - atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel')
    
    remainingLevel = Math.round(levelRange * percentComplete)
    /* Stupid place for this
    if (totalSeconds - elapsedSeconds < CONSTScheduleActiveFrequencyMilli(childLabel) / 1000) {
        
        atomicState.'devices'."${singleDevice.id}"."${type}".'startTime' = null
        atomicState.'devices'."${singleDevice.id}"."${type}".'stopTime' = null
        //atomicState.'devices'."${singleDevice.id}"."${type}".'totalSeconds' = null
        atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel' = null
        atomicState.'devices'."${singleDevice.id}"."${type}".'stopLevel' = null
        if(atomicState.'devices'."${singleDevice.id}"."${type}"?.'reverse') atomicState.'devices'."${singleDevice.id}"."${type}"?.'reverse' = null
        // Deschedule and reschedule incremental
        return
    }
    */
    if(atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel' < atomicState.'devices'."${singleDevice.id}"."${type}".'stopLevel') forward = True
    if(atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel' > atomicState.'devices'."${singleDevice.id}"."${type}".'stopLevel') forward = False
    if(atomicState.'devices'."${singleDevice.id}"."${type}"?.'hueDirection' == 'reverse') forward = !forward
    if(atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel' < atomicState.'devices'."${singleDevice.id}"."${type}".'stopLevel') resultLevel = atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel' + remainingLevel
    if(atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel' > atomicState.'devices'."${singleDevice.id}"."${type}".'stopLevel') resultLevel = atomicState.'devices'."${singleDevice.id}"."${type}".'startLevel' - remainingLevel
    return resultLevel
}
// Do reverse, and wrap around
def _computeIncrementalHueRange(singleDevice,childLabel = 'Master'){
    if(atomicState.'devices'."${singleDevice.id}".'hue'?.'hueDirection' == 'reverse') return 360 - Math.abs(atomicState.'devices'."${singleDevice.id}".'hue'.'stopLevel' + atomicState.'devices'."${singleDevice.id}".'hue'.'startLevel')
    if(atomicState.'devices'."${singleDevice.id}".'hue'?.'hueDirection' != 'reverse') return Math.abs(atomicState.'devices'."${singleDevice.id}".'hue'.'stopLevel' - atomicState.'devices'."${singleDevice.id}".'hue'.'startLevel')
}

// CAN BE REMOVED
def logDeviceTable(multiDevice,line){
    multiDevice.each{singleDevice->
   // log.debug line + ' logDeviceTable ' + atomicState.'devices'?."${singleDevice.id}"
    }
}

// DEPRICATED
// type expects 'brightness', 'temp', 'hue', 'sat'
def buildIncrementalStartDefaults(type, multiDevice, startTime, stopTime, startLevel, stopLevel, hueDirection = null, appId){
    if(!multiDevice) return
    if(!startTime) return
    //if(!stopTime) return
    if(!type) return
    if(!startLevel) return
    if(!stopLevel) return

    if(!atomicState.'devices') atomicState.'devices' = [:]

    rightMap = [:]
    rightMap."${type}" = [:]
    rightMap."${type}".'startLevel' = startLevel
    if(stopLevel) rightMap."${type}".'stopLevel' = stopLevel
    rightMap."${type}".'appId' = appId
    rightMap."${type}".'appType' = _getAppTypeFromId(appId,childLabel)
    rightMap."${type}".'currentLevel' = startLevel
    if(hueDirection) rightMap."${type}".'hueDirection' = hueDirection

    //startHours = new Date(startTime).format('HH').toInteger()
    //startMinutes = new Date(startTime).format('mm').toInteger()
    //startSeconds = startHours * 3600 + startMinutes * 60
    //totalSeconds = (stopTime - startTime) / 1000

    //rightMap."${type}".'startSeconds' = startSeconds
    //rightMap."${type}".'totalSeconds' = totalSeconds
    rightMap."${type}".'startTime' = startTime
    if(stopTime) rightMap."${type}".'stopTime' = stopTime
    multiDevice.each{singleDevice->
        
        if(!atomicState.'devices'?."${singleDevice.id}") atomicState.'devices'."${singleDevice.id}" = [:]
        
        rightMap."${type}".'priorLevel' = _getDeviceCurrentLevel(singleDevice,type)
        leftMap = atomicState.'devices'."${singleDevice.id}"
        mergeMaps(rightMap, singleDevice)
    }
    return true
}

// DEPRECATED
//Rename these back to "updateTable"?
def buildStateMapMulti(multiDevice,action,childLabel = 'Master'){
    if(!multiDevice) return
    multiDevice.each{singleDevice->
        _buildStateMapSingle(singleDevice,action,childLabel)
    }
}
def getStateMapSingle(singleDevice,action,appId,childLabel = 'Master'){
    if(!action) return
    if(!singleDevice) return
    if(!appId) return
    if(atomicState?.'devices'?."${singleDevice.id}"?.'state' == action) return  // Do we care if it's another app?
    if(action == 'toggle'){
        if(!atomicState?.'devices'?."${singleDevice.id}"?.'state') action = 'on'
        if(atomicState.'devices'."${singleDevice.id}".'state' == 'on') action = 'off'
        if(atomicState.'devices'."${singleDevice.id}".'state' == 'off') action = 'on'
    }
    return ['state':['state': action,'time':now()]]
}
def getLevelMapSingle(type,level,appId,childLabel = 'Master'){
    if(!type) return
    if(!level) return
    if(!appId) return
    
    return ["${type}":['currentLevel': level,'time':now(),'appId':appId,'appType':_getAppTypeFromId(appId,childLabel)]]     // priorLevel now? Prolly should
}

//DEPRICATED
def _buildStateMapSingle(singleDevice,action,childLabel = 'Master'){
    if(!singleDevice) return
    if(!atomicState?.'devices'?."${singleDevice.id}") atomicState?.'devices'?."${singleDevice.id}" = [:]
    if(!atomicState?.'devices'?."${singleDevice.id}"?.'state') atomicState?.'devices'?."${singleDevice.id}"?.'state' = [:]
    if(atomicState?.'devices'?."${singleDevice.id}"?.'state' == action) return
    if(action == 'toggle'){
        if(!atomicState?.'devices'?."${singleDevice.id}"?.'state') action = 'on'
        if(atomicState.'devices'."${singleDevice.id}".'state' == 'on') action = 'off'
        if(atomicState.'devices'."${singleDevice.id}".'state' == 'off') action = 'on'
    }
    map = ['state':['state': action]]
    _updateTableSingle(singleDevice, map,childLabel)
}

//DEPRICATED
//Used with Contact
def updateTableMulti(multiDevice,childLabel = 'Master'){
    multiDevice.each{singleDevice->
        _updateTableSingle(singleDevice,childLabel)
    }
}

// DEPRECATED
def _updateTableSingle(singleDevice,rightMap,childLabel = 'Master'){
    if(!singleDevice) return
    if(!atomicState.'devices') atomicState.'devices' = [:]
    if(!atomicState.'devices'?."${singleDevice.id}") atomicState.'devices'."${singleDevice.id}" = [:]
 
    appId = _getChildAppIdFromLabel(childLabel)
    time = new Date().time
    if(rightMap?.'brightness') rightMap = _addAppTimeToMap(rightMap,'brightness',childLabel)
    if(rightMap?.'temp') rightMap = _addAppTimeToMap(rightMap,'temp',childLabel)
    if(rightMap?.'hue') rightMap = _addAppTimeToMap(rightMap,'hue',childLabel)
    if(rightMap?.'sat') rightMap = _addAppTimeToMap(rightMap,'sat',childLabel)
    if(rightMap?.'state') rightMap = _addAppTimeToMap(rightMap,'state',childLabel)

    //log.debug '_updateTableSingle ' + rightMap
    mergeMaps(rightMap,singleDevice)
    //atomicState.'devices'."${singleDevice.id}" = atomicState.'devices'."${singleDevice.id}" + defaults
}

// DEPRECATED
def _addAppTimeToMap(rightMap,entry,childLabel){
    appId = _getChildAppIdFromLabel(childLabel)
    if (appId) rightMap."${entry}" += ['appId':appId]
    if (!appId) rightMap."${entry}" += ['appId':'manual']
    rightMap."${entry}" += ['appType':_getAppTypeFromId(appId,childLabel)]
    rightMap."${entry}" += ['time':now()]
    return rightMap
}

// DEPRECATED
// type expects 'level', 'temp', 'hue', 'sat'
def updateTableLevelMulti(multiDevice, type, level, childLabel='Master'){
    if(!multiDevice) return
    if(!level) return
    if(!type) return
    multiDevice.each{singleDevice->
        updateTableLevelSingle(singleDevice, type, level, childLabel)
    }
}

// DEPRECATED
// type expects 'level', 'temp', 'hue', 'sat'
def updateTableLevelSingle(singleDevice, type, level, childLabel='Master'){
    if(!singleDevice) return
    if(!level) return
    if(!type) return

    if(type == 'hue'){
        if(!settingsHiRezHue) level = Math.round(convertToInteger(level) * 3.6)   // Convert to store in table; should be the only place needed for conversion
    }
    rightMap = ["${type}":['currentLevel': level]]
    _updateTableSingle(singleDevice, rightMap, childLabel)
    //_updateTableSingle(singleDevice, map, childLabel)
}
//def updateTableTempSingle(singleDevice, level, childLabel='Master'){
//    if(!singleDevice) return
//    if(!level) return
    
//    _updateTableSingle(singleDevice, ['temp':['level': level]],childLabel)
//}
//def updateTableHueSingle(singleDevice, level, childLabel='Master'){
//    if(!singleDevice) return
//    if(!level) return
//    _updateTableSingle(singleDevice, ['hue':['level': level]],childLabel)
//}
//def updateTableSatSingle(singleDevice, level, childLabel='Master'){
//    if(!singleDevice) return
//    if(!level) return
//    _updateTableSingle(singleDevice, ['sat':['level': level]],childLabel)
//}

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
            if(Child.label.startsWith('Humidity - ')) returnValue = 'humidity'
        }
    }
    if(!returnValue) return 'manual'
    return returnValue
}

def updateTableNextLevelMulti(multiDevice, action, childLabel='Master'){
    if(action != 'dim' && action != 'brighten') return
    if(!multiDevice) return
    multiDevice.each{singleDevice->
        _updateTableNextLevelSingle(singleDevice, action, childLabel)
    }
}

// DEPRECATED?
// THIS DOES NOT UPDATE THE LEVEL
// Determine next dim or brighten level based on "multiplier
// action = "dim" or "brighten"
def _updateTableNextLevelSingle(singleDevice, action, childLabel='Master'){
    if(action != 'dim' && action != 'brighten') return
    if(!isDimmable(singleDevice,childLabel)) return
    
    appId = _getChildAppIdFromLabel(childLabel)
    if(!appId) return

    if(action == 'brighten' && !atomicState.'devices'."${singleDevice.id}"?.'brightness'?.'currentLevel' == 100) return
    if(action == 'brighten' && !atomicState.'devices'."${singleDevice.id}"?.'brightness'?.'currentLevel' == 'high') return

    if(action == 'dim' && !atomicState.'devices'."${singleDevice.id}"?.'brightness'?.'currentLevel' == 100) return
    if(action == 'dim' && !atomicState.'devices'."${singleDevice.id}"?.'brightness'?.'currentLevel' == 'low') return

    newLevel = _getNextLevelFan(singleDevice, action, childLabel)
    if(!newLevel) newLevel = _getNextLevelDimmable(singleDevice, action, childLabel)
                                                         
    if(atomicState.'devices'."${singleDevice.id}"?.'brightness'?.'currentLevel') level = atomicState.'devices'."${singleDevice.id}".'brightness'.'currentLevel'
    if(!atomicState.'devices'."${singleDevice.id}"?.'brightness'?.'currentLevel') _getDeviceCurrentLevel(singleDevice,'level',childLabel)
    return true
}

def updateTableCapturedState(singleDevice,action,appId,childLabel = 'Master'){
    if(!atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state') return
    if(atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state' == action) return
    putLog(1176, 'Captured state change for ' + singleDevice + ' to turn ' + action + ' (table was ' + atomicState.'devices'?."${singleDevice.id}"?.'state'?.'state' + '; actually was ' + singleDevice.currentState + ')',childLabel,'True')
    //_buildStateMapSingle(singleDevice,action,childLabel)    // With on/off, we don't care about appId, and don't need to remove schedule stuff
}

def updateTableCapturedLevel(singleDevice,type,value,appId,childLabel = 'Master'){
    //value = convertToInteger(event.value)
    if(!atomicState.'devices'?."${singleDevice.id}"?."${type}"?.'currentLevel') return
    if(atomicState.'devices'?."${singleDevice.id}"?."${type}"?.'currentLevel' == convertToInteger(value)) return
    
    //updateTableLevelSingle(singleDevice,'brightness',value,'manual')
    // Delete startLevel, etc., of schedule? or only rely on 'manual'?
    currentLevel = _getDeviceCurrentLevel(singleDevice,type,childLabel)
    putLog(1188,'trace','Captured manual ' + type + ' change for ' + singleDevice + ' to turn ' + value + ' (table was ' + atomicState.'devices'?."${singleDevice.id}"?."${type}"?.'currentLevel' + '; actually was ' + currentLevel + ')',childLabel,'True')
    //updateTableLevelSingle(singleDevice,'brightness',value,'manual')
    //_updateTableSingle(singleDevice, map)
}

def _getNextLevelFan(singleDevice, action, childLabel='Master'){
    if(!checkIsFan(singleDevice,childLabel)) return
    if(action != 'dim' && action != 'brighten') return
    if(atomicState.'devices'."${singleDevice.id}"?.'brightness'?.'currentLevel') level = atomicState.'devices'."${singleDevice.id}".'brightness'.'currentLevel'
    if(!atomicState.'devices'."${singleDevice.id}"?.'brightness'?.'currentLevel') level = singleDevice.currentSpeed

    if(action == 'brighten'){
        if(level == 'low' || level == 'medium-low') return 'medium'
        if(level == 'medium' || level == 'medium-high') return 'high'
        if(!level || level == 0 || level == 'off') return 'low'
    }
    if(!level) return
    if(level == 'high') return 'medium'
    if(level == 'medium-high') return 'medium'
    if(level == 'medium') return 'low'
    if(level == 'medium-low') return 'low'
}

def _getNextLevelDimmable(singleDevice, action, childLabel='Master'){
    if(checkIsFan(singleDevice,childLabel)) return
    if(!isDimmable(singleDevice,childLabel)) return
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
        putLog(1229,'error','ERROR: Failed to find dimSpeed in function getNextLevel with ' + appId,childLabel,True)
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
                    putLog(1269,'warn','scheduleChildEvent given null for functionName from appId ' + appId + ' (timeMillis = ' + timeMillis + ', timeValue = ' + TimeValue + ')',Child.label,'True')
                    return
                }
                Child.setScheduleFromParent(timeMillis,functionName,parametersMap)
                if(parameters) parameters = ' (with parameters: ' + parameters + ')'
                putLog(1274,'debug','Scheduled ' + functionName + parameters + ' for ' + Math.round(timeMillis / 1000) + ' seconds from now',Child.label,'True')
        }
    }
}

def changeMode(mode, childLabel = 'Master'){
    if(location.mode == mode) return
    message = 'Changed Mode from ' + oldMode + ' to '
    setLocationMode(mode)
    putLog(1284,'debug',message + mode,childLabel,'True')
}

// Send SMS text message to $phone with $message
//SMS IS NO LONGER SUPPORTED
def sendPushNotification(phone, message, childLabel = 'Master'){
    def now = new Date()getTime()
    seconds = (now - atomicState.contactLastNotification) / 1000
    if(seconds < 361) {
        putLog(1293,'info','Did not send push notice for ' + evt.displayName + ' ' + evt.value + 'due to notification sent ' + seconds + ' ago.',childLabel,'True')
        return
    }

    atomicState.contactLastNotification = now
    speechDevice.find{it ->
        if(it.id == deviceId) {
            if(it.deviceNotification(message)) putLog(1300,'debug','Sent phone message to ' + phone + ' "' + message + '"',childLabel,'True')
        }
    }
}

def sendVoiceNotification(deviceId,message, childLabel='Master'){
    if(!deviceId)  return
    speechDevice.find{it ->
        if(it.id == deviceId) {
            if(it.speak(text)) putLog(1309,'debug','Played voice message on ' + deviceId + ' "' + message + '"',childLabel,'True')
        }
    }
}


//Merges maps as left to right, where left takes precedence
def mergeTwoMaps(Map leftMap, Map rightMap){
    //deviceId = convertToInteger(deviceId)
    if(!leftMap) return rightMap
    if(!rightMap) return leftMap
    
    newMap = [:]
    (leftMap.entrySet() + rightMap.entrySet()).each { entry -> 
        newMap[entry.key] = newMap.containsKey(entry.key) ? [:] << newMap[entry.key] << entry.value : entry.value 
    }
  
    return newMap
}

// Deletes and replaces an atomicState key with a new map
// Replaces an atomicState key with a new map, preserving subkeys not in common
def mergeMapToTableWithPreserve(singleDevice, newNewMap){
    newMap = [:]
    newMap = newNewMap
        log.debug '1 mergeMapToTableWithPreserve ' + newMap
    if(!singleDevice) return
        log.debug '2 mergeMapToTableWithPreserve'
    if(!newMap) return
        log.debug '3 mergeMapToTableWithPreserve'
    if(!atomicState.'devices') atomicState.'devices' = [:]
    if(!atomicState.'devices'?."${singleDevice.id}") atomicState.'devices'."${singleDevice.id}" = [:]

    //leftMap = atomicState.'devices'."${singleDevice.id}"
    mainMap = atomicState.'devices'
    log.debug '4 mergeMapToTableWithPreserve'
    if(newMap?.'state') mainMap."${singleDevice.id}"?.'state' = newMap.'state'
    if(newMap?.'brightness') {
        log.debug '5 mergeMapToTableWithPreserve'
        if(mainMap."${singleDevice.id}"?.'brightness'){
        log.debug '6 mergeMapToTableWithPreserve'
            if(newMap?.'brightness'?.'currentLevel') mainMap."${singleDevice.id}".'brightness'.'currentLevel' = newMap.'brightness'.'currentLevel'
            if(newMap?.'brightness'?.'startTime') mainMap."${singleDevice.id}".'brightness'.'startTime' = newMap.'brightness'.'startTime'
            if(newMap?.'brightness'?.'stopTime') mainMap."${singleDevice.id}".'brightness'.'stopTime' = newMap.'brightness'.'stopTime'
            if(newMap?.'brightness'?.'startLevel') mainMap."${singleDevice.id}".'brightness'.'startLevel' = newMap.'brightness'.'startLevel'
            if(newMap?.'brightness'?.'stopLevel') mainMap."${singleDevice.id}".'brightness'.'stopLevel' = newMap.'brightness'.'stopLevel'
            if(newMap?.'brightness'?.'appId') mainMap."${singleDevice.id}".'brightness'.'appId' = newMap.'brightness'.'appId'
            if(newMap?.'brightness'?.'appType') mainMap."${singleDevice.id}".'brightness'.'appType' = newMap.'brightness'.'appType'
            if(newMap?.'brightness'?.'time') mainMap."${singleDevice.id}".'brightness'.'time' = newMap.'brightness'.'time'
        }
        if(!mainMap."${singleDevice.id}"?.'brightness') mainMap."${singleDevice.id}"?.'brightness' = newMap.'brightness'
    }
    if(newMap?.'temp') {
        log.debug '7 mergeMapToTableWithPreserve'
        if(mainMap."${singleDevice.id}"?.'temp'){
        log.debug '8 mergeMapToTableWithPreserve'
            if(newMap?.'temp'?.'currentLevel') mainMap."${singleDevice.id}".'temp'.'currentLevel' = newMap.'temp'.'currentLevel'
            if(newMap?.'temp'?.'startTime') mainMap."${singleDevice.id}".'temp'.'startTime' = newMap.'temp'.'startTime'
            if(newMap?.'temp'?.'stopTime') mainMap."${singleDevice.id}".'temp'.'stopTime' = newMap.'temp'.'stopTime'
            if(newMap?.'temp'?.'startLevel') mainMap."${singleDevice.id}".'temp'.'startLevel' = newMap.'temp'.'startLevel'
            if(newMap?.'temp'?.'stopLevel') mainMap."${singleDevice.id}".'temp'.'stopLevel' = newMap.'temp'.'stopLevel'
            if(newMap?.'temp'?.'appId') mainMap."${singleDevice.id}".'temp'.'appId' = newMap.'temp'.'appId'
            if(newMap?.'temp'?.'appType') mainMap."${singleDevice.id}".'temp'.'appType' = newMap.'temp'.'appType'
            if(newMap?.'temp'?.'time') mainMap."${singleDevice.id}".'temp'.'time' = newMap.'temp'.'time'
        }
        if(!mainMap."${singleDevice.id}"?.'temp') mainMap."${singleDevice.id}".'temp' = newMap.'temp'
    }
    if(newMap.'hue') {
        log.debug '9 mergeMapToTableWithPreserve'
        if(mainMap."${singleDevice.id}"?.'hue'){
        log.debug '10 mergeMapToTableWithPreserve'
            if(newMap?.'hue'?.'currentLevel') mainMap."${singleDevice.id}".'hue'.'currentLevel' = newMap.'hue'.'currentLevel'
            log.debug 'newMap 1 ' + newMap
            if(newMap?.'hue'?.'startTime') mainMap."${singleDevice.id}".'hue'.'startTime' = newMap.'hue'.'startTime'
            if(newMap?.'hue'?.'stopTime') mainMap."${singleDevice.id}".'hue'.'stopTime' = newMap.'hue'.'stopTime'
            if(newMap?.'hue'?.'startLevel') mainMap."${singleDevice.id}".'hue'.'startLevel' = newMap.'hue'.'startLevel'
            if(newMap?.'hue'?.'stopLevel') mainMap."${singleDevice.id}".'hue'.'stopLevel' = newMap.'hue'.'stopLevel'
            if(newMap?.'hue'?.'reverse') mainMap."${singleDevice.id}".'hue'.'reverse' = newMap.'hue'.'reverse'
            if(newMap?.'hue'?.'appId') mainMap."${singleDevice.id}".'hue'.'appId' = newMap.'hue'.'appId'
            if(newMap?.'hue'?.'appType') mainMap."${singleDevice.id}".'hue'.'appType' = newMap.'hue'.'appType'
            if(newMap?.'hue'?.'time') mainMap."${singleDevice.id}".'hue'.'time' = newMap.'hue'.'time'
        }
        if(!mainMap."${singleDevice.id}"?.'hue') mainMap."${singleDevice.id}"?.'hue' = newMap.'hue'
    }
    if(newMap?.'sat') {
        log.debug '11 mergeMapToTableWithPreserve'
        if(mainMap."${singleDevice.id}"?.'sat'){
        log.debug '12 mergeMapToTableWithPreserve'
            if(newMap?.'sat'?.'currentLevel') mainMap."${singleDevice.id}".'sat'.'currentLevel' = newMap.'sat'.'currentLevel'
            if(newMap?.'sat'?.'startTime') mainMap."${singleDevice.id}".'sat'.'startTime' = newMap.'sat'.'startTime'
            if(newMap?.'sat'?.'stopTime') mainMap."${singleDevice.id}".'sat'.'stopTime' = newMap.'sat'.'stopTime'
            if(newMap?.'sat'?.'startLevel') mainMap."${singleDevice.id}".'sat'.'startLevel' = newMap.'sat'.'startLevel'
            if(newMap?.'sat'?.'stopLevel') mainMap."${singleDevice.id}".'sat'.'stopLevel' = newMap.'sat'.'stopLevel'
            if(newMap?.'sat'?.'appId') mainMap."${singleDevice.id}".'sat'.'appId' = newMap.'sat'.'appId'
            if(newMap?.'sat'?.'appType') mainMap."${singleDevice.id}".'sat'.'appType' = newMap.'sat'.'appType'
            if(newMap?.'sat'?.'time') mainMap."${singleDevice.id}".'sat'.'time' = newMap.'sat'.'time'
        }
        if(!mainMap."${singleDevice.id}"?.'sat') mainMap."${singleDevice.id}".'sat' = newMap.'temp'
    }
    log.debug 'mainMap ' + mainMap
    atomicState.'devices' =  mainMap

/*
newMap = [:]
log.debug 'leftMap ' + leftMap
log.debug 'rightMap ' + rightMap

rightMap.each{rightKey,rightValue->
    if(!leftMap?."${rightKey}"){
        newMap["${rightKey}"] = rightValue
    }
    if(leftMap?."${rightKey}"){
        newMap."${rightKey}" = [:]
        rightMap."${rightKey}".each {rightSubkey,rightSubvalue->
        log.debug 'rightSubkey ' + rightSubkey
            newMap."${rightKey}"."${rightSubkey}" = rightSubvalue
            log.debug 'newMap 0 ' + newMap
        }
        log.debug 'newMap 1 ' + newMap
        
        leftMap["${rightKey}"].each { leftSubkey, leftSubvalue ->
            if(!newMap."${rightKey}"?."${leftSubkey}"){
                newMap."${rightKey}"."${leftSubkey}" = leftSubvalue
            }
        }
        log.debug 'newMap 2 ' + newMap
    }
}
leftMap.each { leftKey, leftValue ->
    if(!newMap?."${leftKey}"){
        newMap["${leftKey}"] = leftValue
    }
}

        log.debug 'newMap 3 ' + newMap
   // newMap = [:]
   // (leftMap.entrySet() + rightMap.entrySet()).each { entry -> 
   //     newMap[entry.key] = newMap.containsKey(entry.key) ? [:] << newMap[entry.key] << entry.value : entry.value 
   // }
    deviceMap = [:]
    deviceMap."${singleDevice.id}" = newMap
    
    fullMap = [:]
    fullMap = atomicState.'devices' + deviceMap
    atomicState.'devices' =  fullMap
    */
}

// Deletes and replaces an atomicState key with a new map
def mergeMapToTableWithoutPreserve(singleDevice, Map rightMap){
    if(!singleDevice) return
    if(!rightMap) return
    if(!atomicState.'devices') atomicState.'devices' = [:]
    if(!atomicState.'devices'?."${singleDevice.id}") atomicState.'devices'."${singleDevice.id}" = [:]

    newMap = atomicState.'devices'."${singleDevice.id}"
    newMap << rightMap //??
  
    deviceMap = [:]
    deviceMap."${singleDevice.id}" = newMap
    fullMap = [:]
    fullMap = atomicState.'devices' + deviceMap
    atomicState.'devices' =  fullMap
}

//DEPRECATED
//Merges maps as left to right, where left takes precedence
def mergeMaps(Map rightMap, singleDevice, leftMap = null){
    //deviceId = convertToInteger(deviceId)
    if(!singleDevice) return
    if(!rightMap) return
    
    if(!atomicState.'devices') {
        atomicState.'devices' = [:]
        atomicState.'devices'."${singleDevice.id}" = [:]
    }
    leftMap = atomicState.'devices'."${singleDevice.id}"

    
    //old code - this needs to be the whole thing, not just deviceId portion
  //  if(!atomicState.'devices'?."${deviceId}"){
   //     atomicState.'devices'."${deviceId}" = rightMap
    //    return
    //}
    
   // return newRightMap.inject(leftMap.clone()) { map, entry ->
   // if(entry.key in map && map[entry.key] != null && entry.value != null) {
   //     if(map[entry.key] instanceof Map && entry.value instanceof Map) {
   //         map[entry.key] = mergeMaps(entry.value, singleDevice, map[entry.key])
   //     } else {
   //         map[entry.key] = entry.value
   //     }
   // }
//}
    //leftMap = [:]
    newMap = [:]
    (leftMap.entrySet() + rightMap.entrySet()).each { entry -> 
        newMap[entry.key] = newMap.containsKey(entry.key) ? [:] << newMap[entry.key] << entry.value : entry.value 
    }
  
    
    deviceMap = [:]
    deviceMap."${singleDevice.id}" = newMap
    fullMap = [:]
    fullMap = atomicState.'devices' + deviceMap
    atomicState.'devices' =  fullMap
}


// Replaces an atomicState key with a new map, preserving subkeys not in common
def updateTableFromSettings(singleDevice, appId) {
    if(!atomicState.'devices'?."${singleDevice.id}") atomicState.'devices'."${singleDevice.id}" = [:]
    def rightMap = [:]
    child.find { Child ->
        if(Child.id == appId) {
            rightMap['brightness'] = Child.buildSubMap('brightness')
            rightMap['temp'] = Child.buildSubMap('temp')
            rightMap['hue'] = Child.buildSubMap('hue')
            rightMap['sat'] = Child.buildSubMap('sat')
        }
    }
    //leftMap = atomicState.'devices'."${singleDevice.id}"
    mergeMaps(rightMap, singleDevice) 
}


def removeScheduleFromTable(appId){
    if(!appId) return
    Child.find { Child ->
        if(Child.id == appId) {
            multiDevice = Child.getDevices()
            multiDevice.each{singleDevice->
                if(leftMap?."${singleDevice.id}"){
                    deviceMap = atomicState.'devices'."${singleDevice.id}"
                    if(deviceMap?.'brightness'?.appId == appId || deviceMap?.'brightness'?.appType != 'schedule'){
                        //deviceMap?.'level'?.remove('startSeconds')
                        //deviceMap?.'level'?.remove('stopSeconds')
                        //deviceMap?.'level'?.remove('totalSeconds')
                        deviceMap?.'brightness'?.remove('startTime')
                        deviceMap?.'brightness'?.remove('stopTime')
                        deviceMap?.'brightness'?.remove('startLevel')
                        deviceMap?.'brightness'?.remove('stopLevel')
                    }
                    if(deviceMap?.'temp'?.appId == appId || deviceMap?.'temp'?.appType != 'schedule'){
                        deviceMap?.'temp'?.remove('startTime')
                        deviceMap?.'temp'?.remove('stopTime')
                        deviceMap?.'temp'?.remove('startLevel')
                        deviceMap?.'temp'?.remove('stopLevel')
                    }
                    if(deviceMap?.'hue'?.appId == appId || deviceMap?.'hue'?.appType != 'schedule'){
                        deviceMap?.'hue'?.remove('startTime')
                        deviceMap?.'hue'?.remove('stopTime')
                        deviceMap?.'hue'?.remove('startLevel')
                        deviceMap?.'hue'?.remove('stopLevel')
                    }
                    if(deviceMap?.'sat'?.appId == appId || deviceMap?.'sat'?.appType != 'schedule'){
                        deviceMap?.'sat'?.remove('startTime')
                        deviceMap?.'sat'?.remove('stopTime')
                        deviceMap?.'sat'?.remove('startLevel')
                        deviceMap?.'sat'?.remove('stopLevel')
                    }
                    atomicState."${singleDevice.id}" = deviceMap
                }
            }
        }
    }
    return leftMap
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
