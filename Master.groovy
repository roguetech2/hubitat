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
        if(!state.masterInstalled) {
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
    state.logLevel = getLogLevel()
    state.masterInstalled = true
    initialize()
}

def updated() {
    state.logLevel = getLogLevel()
    initialize()
}

def initialize() {
    _getTable()
    state.logLevel = getLogLevel()
}

def getHiRezHue(multiDevice){
    // Can test hiRezHue by setting hue to 350, and testing if currentColorName == "Red"
    // Could have a "test" option, and loop through all child apps, all devices, and report
    return settings['hiRezHue']
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
def validateHue(value, childLabel='Master'){
    if(!value) return true
    value = value as int
        if(value < 1) return false
        if(getHiRezHue() && value > 360) return false
        if(!getHiRezHue() && value > 100) return false
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
    if(state.'devices'?."${singleDevice.id}"?.'state'?.'state' == 'on') return true
    if(state.'devices'?."${singleDevice.id}"?.'state'?.'state' == 'off') return false 
    if(singleDevice.currentValue('switch') == 'on') {
        _buildStateMapSingle(singleDevice,'on',childLabel)
        return true
    }
    _buildStateMapSingle(singleDevice,'off',childLabel)
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

def CONSTScheduleActiveFrequencyMilli(childLabel = 'Master'){
    return 8000
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

def _resumeDeviceScheduleSingle(singleDevice,childLabel='Master'){
    if(!singleDevice) return

    childApps.each { Child ->   // COuld be multiple sechedules per device
        if(Child.label.startsWith('Time - ')){
            Child.find { ChildDevice ->
                if(singleDevice.id == ChildDevice.id){
                    Child.setDailySchedules()
                    putLog(690,'info','Resuming schedule for ' + singleDevice + ' (' + ChildDevice.label + ')',childLabel,True)
                }
            }
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
    multiDevice.each{singleDevice->
        _setDeviceSingle(singleDevice,childLabel)
    }
}

// Sets device to match state
def _setDeviceSingle(singleDevice,childLabel = 'Master'){
    if(!singleDevice) return
    if(!state.'devices'?."${singleDevice.id}") return
    if(_setDeviceSingleLevels(singleDevice,'level',childLabel)) pauseExecution(200)
    if(_setDeviceSingleLevels(singleDevice,'temp',childLabel)) pauseExecution(200)
    if(_setDeviceSingleLevels(singleDevice,'color',childLabel)) pauseExecution(200)
    //if(_setDeviceSingleLevels(singleDevice,'sat',childLabel)) pauseExecution(200)
    _setDeviceSingleState(singleDevice,childLabel)
}

// Turns on off based on devices Table
def _setDeviceSingleState(singleDevice,childLabel = 'Master'){
    if(!singleDevice) return
    if(!state.'devices'?."${singleDevice.id}"?.'state') return
    if(singleDevice.currentValue('switch') != !state.'devices'."${singleDevice.id}".'state'.'state') putLog(736,'info','Turned ' + singleDevice + ' ' + state.'devices'."${singleDevice.id}".'state'.'state' ,childLabel,'True')
    if(state.'devices'."${singleDevice.id}".'state'.'state' == 'on') singleDevice.on()
    if(state.'devices'."${singleDevice.id}".'state'.'state' == 'off') singleDevice.off()
    return true
}

// Does brightness, temp, hue and sat
// Adds device level to state as priorLevel
// Sets current value in devices Table
// temp is handled by _setDeviceSingleTemp
// hue is handled by to _setDeviceSingleHue
def _setDeviceSingleLevels(singleDevice,type,childLabel='Master'){
    if(!singleDevice) return
    if(!state.'devices'."${singleDevice.id}"?."${type}") return
    if(type == 'level' && !checkIsDimmable(singleDevice,childLabel)) return
    if(type == 'temp' && !checkIsTemp(singleDevice,childLabel)) return
    if(type == 'color' && !checkIsColor(singleDevice,childLabel)) return
    //if(type == 'sat' && !checkIsColor(singleDevice,childLabel)) return
    if(!state.'devices'."${singleDevice.id}"?."${type}"?.'currentLevel') {
        putLog(755,'warning','WARNING: ' + type + '.currentLevel not set in Table, when updating device (_setDeviceSingleLevel)',childLabel,True)
        return
    }

    if(type == 'level') currentValue = singleDevice.currentLevel
    if(type == 'temp') return _setDeviceSingleTemp(singleDevice,childLabel) // Why returning it?
    // Hue and Sat set together with map ['hue':level,'saturation':level]
    if(type == 'color') return _setDeviceSingleColor(singleDevice,childLabel) // Why returning it?
    //if(type == 'sat') currentValue = singleDevice.currentSaturation
    if(state.'devices'."${singleDevice.id}"?."${type}"?.'currentLevel' == currentValue) return  // device level isn't changing
    state.'devices'."${singleDevice.id}"."${type}".'priorLevel' = currentValue
    if(type == 'level') singleDevice.setLevel(state.'devices'."${singleDevice.id}"."${type}".'currentLevel')
    //if(type == 'color') singleDevice.setColor(['hue':state.'devices'."${singleDevice.id}"."${type}".'currentLevel','saturation':state.'devices'."${singleDevice.id}"."${type}".'currentLevel'])
    //if(type == 'sat') singleDevice.currentSaturation = state.'devices'."${singleDevice.id}"."${type}".'currentLevel'
    putLog(769,'info','Set ' + type + ' of ' + singleDevice + ' to ' + state.'devices'."${singleDevice.id}"."${type}".'currentLevel',childLabel,'True')

    return true
}

// Sets Temp to current value in devices Table
// Provides a 5% tolerance for temp

// Add check for device.currentColorMode == 'CT vs device.currentColorMode == 'RGB'
def _setDeviceSingleTemp(singleDevice,childLabel='Master'){
    if(!singleDevice) return
    if(!checkIsTemp(singleDevice,childLabel)) return
    if(!state.'devices'?."${singleDevice.id}"?.'temp'?.'currentLevel') return
    tempDifference = Math.abs(state.'devices'."${singleDevice.id}".'temp'.'currentLevel' - singleDevice.currentColorTemperature as Integer)
    tolerance = singleDevice.currentColorTemperature * 0.05
    if (tempDifference <= tolerance) return  // device level isn't changing
    state.'devices'."${singleDevice.id}".'temp'.'priorLevel' = singleDevice.currentColorTemperature
    singleDevice.setColorTemperature(state.'devices'."${singleDevice.id}".'temp'.'currentLevel')
    putLog(787,'info','Set temp of ' + singleDevice + ' to ' + state.'devices'."${singleDevice.id}".'temp'.'currentLevel',childLabel,'True')

    return true
}

// Sets Hue to current value in devices Table
// If not hiRezHue (set by user in master):
// Converts from percent to degrees (which could result in value of zero), when setting state priorLevel
// Cpmverts from degrees to percent, when setting device currentLevel
def _setDeviceSingleColor(singleDevice,childLabel='Master'){
    if(!singleDevice) return
    if(!checkIsColor(singleDevice,childLabel)) return
    if(state.'devices'?."${singleDevice.id}"?.'hue'?.'currentLevel') return

    if(settingHiRezHue) {
        if(state.'devices'."${singleDevice.id}"?.'hue'?.'currentLevel' == singleDevice.currentHue) return  // device level isn't changing
        state.'devices'."${singleDevice.id}".'hue'.'priorLevel' = singleDevice.currentHue
        singleDevice.setColor(['hue':state.'devices'."${singleDevice.id}"."${type}".'currentLevel','saturation':state.'devices'."${singleDevice.id}"."${type}".'currentLevel'])
    }
    if(!settingHiRezHue) {
        newValue = Math.round(state.'devices'."${singleDevice.id}".'hue'.'currentLevel' / 3.6)
        if(newValue == 0) newValue = 100
        if(newValue == 101) newValue = 1
        if(newValue == singleDevice.currentHue) return  // device level isn't changing
        singleDevice.setColor(['hue':newValue,'saturation':state.'devices'."${singleDevice.id}"."${type}".'currentLevel'])
    }
    putLog(813,'info','Set hue of ' + singleDevice + ' to ' + state.'devices'."${singleDevice.id}".'hue'.'currentLevel',childLabel,'True')
    return true
}          

// Updates state to current scheduled settings
// Need to check if manual
def updateTableIncrementalMulti(multiDevice,type,childLabel = 'Master'){
    if(!multiDevice) return
    multiDevice.each{singleDevice->
        updateTableIncrementalSingle(singleDevice,type,childLabel)
    }
    state.currentSeconds = null
    //setDeviceMultiState(multiDevice,childLabel)
}

def updateTableIncrementalSingle(singleDevice, type, childLabel = 'Master') {
    if (!singleDevice) return
    if (!state?.'devices'?."${singleDevice.id}"?."${type}"?.'startTime') return
    if (!state?.'devices'?."${singleDevice.id}"?."${type}"?.'stopTime') return
    //if (!state?.'devices'?."${singleDevice.id}"?."${type}"?.'totalSeconds') return
    if (!state?.'devices'?."${singleDevice.id}"?."${type}"?.'startLevel') return
    if (!state?.'devices'?."${singleDevice.id}"?."${type}"?.'stopLevel') return

    // Needs to check if manual
    // Check if the schedule is active (wrong mode or disabled)
    childApps.each { Child ->
        if (Child.id == state.'devices'."${singleDevice.id}"."${type}".'appId'.toInteger()) levelScheduleActive = Child.getScheduleActive()
    }
    if (!levelScheduleActive) {
        putLog(842, 'warn', "$singleDevice ${type} data = " + state.'devices'."${singleDevice.id}"."${type}" + " for schedule id " + state.'devices'."${singleDevice.id}"."${type}".'appId' + " but schedule isn't active", childLabel, true)
        return
    }
    incrementLevel = _computeIncrementalLevelSingle(singleDevice,type,childLabel)
    if(incrementLevel) state.'devices'."${singleDevice.id}"."${type}".'currentLevel' = incrementLevel
    return true
}

// With Hue, checks reverse
def _computeIncrementalLevelSingle(singleDevice,type,childLabel = 'Master'){
    if(state.'devices'."${singleDevice.id}"."${type}".'stopTime' == state.'devices'."${singleDevice.id}"."${type}".'startTime') return

    if(state.'devices'?."${singleDevice.id}"?."${type}"?.'appType' != 'time') return    // Checks for manual override
    // If no seconds elapsed
    //if (currentSeconds == state.'devices'."${singleDevice.id}"."${type}".'startSeconds') resultLevel = state.'devices'."${singleDevice.id}"."${type}".'startLevel'

    // Calculate dynamic value
    elapsedSeconds = Math.round((now() - state.'devices'."${singleDevice.id}"."${type}".'startTime') / 1000)
    totalSeconds = Math.round((state.'devices'."${singleDevice.id}"."${type}".'stopTime' - state.'devices'."${singleDevice.id}"."${type}".'startTime') / 1000)
    if (elapsedSeconds == 0) elapsedSeconds = 1
    //remainingSeconds = Math.round((state.'devices'."${singleDevice.id}"."${type}".'stopTime' - state.'devices'."${singleDevice.id}"."${type}".'startTime') / 1000)
    percentComplete = elapsedSeconds / totalSeconds
    if(type == 'hue') levelRange = _computeIncrementalHueRange(singleDevice,childLabel)
    if(type != 'hue') levelRange = Math.abs(state.'devices'."${singleDevice.id}"."${type}".'stopLevel' - state.'devices'."${singleDevice.id}"."${type}".'startLevel')
    remainingLevel = Math.round(levelRange * percentComplete)
    if (totalSeconds - elapsedSeconds < CONSTScheduleActiveFrequencyMilli(childLabel) / 1000) {
        state.'devices'."${singleDevice.id}"."${type}".'startTime' = null
        state.'devices'."${singleDevice.id}"."${type}".'stopTime' = null
        //state.'devices'."${singleDevice.id}"."${type}".'totalSeconds' = null
        state.'devices'."${singleDevice.id}"."${type}".'startLevel' = null
        state.'devices'."${singleDevice.id}"."${type}".'stopLevel' = null
        if(state.'devices'."${singleDevice.id}"."${type}"?.'reverse') state.'devices'."${singleDevice.id}"."${type}"?.'reverse' = null
        // Deschedule and reschedule incremental
        return
    }
    if(state.'devices'."${singleDevice.id}"."${type}".'startLevel' < state.'devices'."${singleDevice.id}"."${type}".'stopLevel') forward = True
    if(state.'devices'."${singleDevice.id}"."${type}".'startLevel' > state.'devices'."${singleDevice.id}"."${type}".'stopLevel') forward = False
    if(state.'devices'."${singleDevice.id}"."${type}"?.'direction' == 'reverse') forward = !forward
    if(state.'devices'."${singleDevice.id}"."${type}".'startLevel' < state.'devices'."${singleDevice.id}"."${type}".'stopLevel') resultLevel = state.'devices'."${singleDevice.id}"."${type}".'startLevel' + remainingLevel
    if(state.'devices'."${singleDevice.id}"."${type}".'startLevel' > state.'devices'."${singleDevice.id}"."${type}".'stopLevel') resultLevel = state.'devices'."${singleDevice.id}"."${type}".'startLevel' - remainingLevel
    return resultLevel
}

// Do reverse, and wrap around
def _computeIncrementalHueRange(singleDevice,childLabel = 'Master'){
    if(state.'devices'."${singleDevice.id}".'hue'?.'direction' == 'reverse') return 360 - Math.abs(state.'devicedevicesData'."${singleDevice.id}"."${type}".'stopLevel' + state.'devices'."${singleDevice.id}"."${type}".'startLevel')
    if(state.'devices'."${singleDevice.id}".'hue'?.'direction' != 'reverse') return Math.abs(state.'devicedevicesData'."${singleDevice.id}"."${type}".'stopLevel' - state.'devices'."${singleDevice.id}"."${type}".'startLevel')
}

def buildIncrementalStartDefaults(type, multiDevice, startTime, stopTime, startLevel, stopLevel, direction, appId){
    if(!multiDevice) return
    if(!startTime) return
    //if(!stopTime) return
    if(!type) return
    if(!startLevel) return
    if(!stopLevel) return

    if(!state.'devices') state.'devices' = [:]

    rightMap = [:]
    rightMap."${type}" = [:]
    rightMap."${type}".'startLevel' = convertToInteger(startLevel)
    if(stopLevel) rightMap."${type}".'stopLevel' = convertToInteger(stopLevel)
    rightMap."${type}".'appId' = appId
    rightMap."${type}".'appType' = _getAppTypeFromId(appId,childLabel)
    rightMap."${type}".'currentLevel' = startLevel
    if(direction) rightMap."${type}".'direction' = direction

    //startHours = new Date(startTime).format('HH').toInteger()
    //startMinutes = new Date(startTime).format('mm').toInteger()
    //startSeconds = startHours * 3600 + startMinutes * 60
    //totalSeconds = (stopTime - startTime) / 1000

    //rightMap."${type}".'startSeconds' = startSeconds
    //rightMap."${type}".'totalSeconds' = totalSeconds
    rightMap."${type}".'startTime' = startTime
    if(stopTime) rightMap."${type}".'stopTime' = stopTime

    multiDevice.each{singleDevice->
        if(!state.'devices'?."${singleDevice.id}") {
            state.'devices'."${singleDevice.id}" = [:]
            state.'devices'."${type}"."${singleDevice.id}" = [:]
        }
        if(type == 'level') rightMap."${type}".'priorLevel' = singleDevice.currentLevel
        if(type == 'temp') rightMap."${type}".'priorLevel' = singleDevice.currentColorTemperature
        if(type == 'hue') rightMap."${type}".'priorLevel' = singleDevice.currentHue
        if(type == 'sat') rightMap."${type}".'priorLevel' = singleDevice.currentSaturation
        leftMap = state.'devices'."${singleDevice.id}"
        mergeMaps(leftMap,rightMap, singleDevice.id)
    }
    return true
}

//Rename these back to "updateTable"?

def buildStateMapMulti(multiDevice,action,childLabel = 'Master'){
    if(!multiDevice) return
    multiDevice.each{singleDevice->
        _buildStateMapSingle(singleDevice,action,childLabel)
    }
}
def _buildStateMapSingle(singleDevice,action,childLabel = 'Master'){
    if(!singleDevice) return
    if(state?.'devices'?."${singleDevice.id}"?.'state' == action) return
    if(action == 'toggle'){
        if(!state?.'devices'?."${singleDevice.id}"?.'state') action == 'on'
        if(state.'devices'."${singleDevice.id}".'state' == 'on') action == 'off'
        if(state.'devices'."${singleDevice.id}".'state' == 'off') action == 'on'
    }
    map = ['state':['state': action]]
    _updateTableSingle(singleDevice, map,childLabel)
}

//Used with Contact
def updateTableMulti(multiDevice,childLabel = 'Master'){
    multiDevice.each{singleDevice->
        _updateTableSingle(singleDevice,childLabel)
    }
}

def _updateTableSingle(singleDevice,rightMap,childLabel = 'Master'){
    if(!singleDevice) return
    if(!state.'devices') state.'devices' = [:]
    if(!state.'devices'?."${singleDevice.id}") state.'devices'."${singleDevice.id}" = [:]

    appId = _getChildAppIdFromLabel(childLabel)
    time = new Date().time
    rightMap.each { entry, value ->
        if(entry != 'level' && entry != 'temp' && entry != 'hue' && entry != 'sat' && entry != 'state'){
            putLog(971,'warn','Invalid Table key of "' + entry + '" created in _updateTableSingle, with value of "' + value + '" for ' + singleDevice + ' (' + singleDevice.id + '), ' + childLabel,childLabel,'True')
        }
        if (appId) rightMap."${entry}".'appId' = appId
        if (!appId) rightMap."${entry}".'appId' = 'manual'
        rightMap."${entry}".'appType' = _getAppTypeFromId(appId,childLabel)
        rightMap."${entry}".'time' = time
    }
    leftMap = state.'devices'."${singleDevice.id}"
    mergeMaps(leftMap,rightMap,singleDevice.id)
    //state.'devices'."${singleDevice.id}" = state.'devices'."${singleDevice.id}" + defaults
}

def updateTableLevelMulti(multiDevice, type, level, childLabel='Master'){
    if(!multiDevice) return
    if(!level) return
    if(!type) return
    multiDevice.each{singleDevice->
        updateTableLevelSingle(singleDevice, type, action, childLabel)
    }
}

def updateTableLevelSingle(singleDevice, type, level, childLabel='Master'){
    if(!singleDevice) return
    if(!level) return
    if(!type) return
    if(type != 'level' && type != 'temp' && type != 'hue' && type != 'sat') putLog(996,'warn','Invalid type of ' + type + ' with level of ' + level + ' sent to updateTableLevelSingle for ' + singleDevice + '  from ' + childLabel, appLabel,'True')
    
    map = [type:['level': level]]
    _updateTableSingle(singleDevice, map, childLabel)
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

// THIS DOES NOT UPDATE THE LEVEL
// Determine next dim or brighten level based on "multiplier
// action = "dim" or "brighten"
def _updateTableNextLevelSingle(singleDevice, action, childLabel='Master'){
    if(action != 'dim' && action != 'brighten') return
    if(!isDimmable(singleDevice,childLabel)) return
    
    appId = _getChildAppIdFromLabel(childLabel)
    if(!appId) return

    if(action == 'brighten' && !state.'devices'."${singleDevice.id}"?.'level'?.'currentLevel' == 100) return
    if(action == 'brighten' && !state.'devices'."${singleDevice.id}"?.'level'?.'currentLevel' == 'high') return

    if(action == 'dim' && !state.'devices'."${singleDevice.id}"?.'level'?.'currentLevel' == 100) return
    if(action == 'dim' && !state.'devices'."${singleDevice.id}"?.'level'?.'currentLevel' == 'low') return

    newLevel = _getNextLevelFan(singleDevice, action, childLabel)
    if(!newLevel) newLevel = _getNextLevelDimmable(singleDevice, action, childLabel)
                                                         
    if(state.'devices'."${singleDevice.id}"?.'level'?.'currentLevel') level = state.'devices'."${singleDevice.id}".'level'.'currentLevel'
    if(!state.'devices'."${singleDevice.id}"?.'level'?.'currentLevel') singleDevice.currentLevel
    return true
}

def updateTableCapturedState(singleDevice,action,appId,childLabel = 'Master'){
    if(state.'devices'."${singleDevice.id}"?.'state'?.'state' && state.'devices'."${singleDevice.id}".'state'.'state' == action) return
    if(state.'devices'?."${singleDevice.id}"?.'state'?.'appId' == appId) return
    // Need more checks here!
    _buildStateMapSingle(singleDevice,action,childLabel)
    putLog(1073,'trace','Captured manual state change for ' + singleDevice + ' to turn ' + action,childLabel,'True')
}
def updateTableCapturedLevel(singleDevice,type,value,appId,childLabel = 'Master'){
    //value = parent.convertToInteger(event.value)
    if(state.'devices'?."${singleDevice.id}"?."${type}"?.'currentLevel'){
        if(state.'devices'."${singleDevice.id}"."${type}".'currentLevel' == convertToInteger(value)) return
        map = ["${type}":['currentLevel':action,'priorLevel':state.'devices'."${singleDevice.id}".'level'.'currentLevel']]
    }
    if(!state.'devices'."${singleDevice.id}"?."${type}"?.'currentLevel') map = ["${type}":['currentLevel':value]]

    putLog(1083,'trace','Captured manual ' + type + ' change for ' + singleDevice + ' to turn ' + value,childLabel,'True')
    _updateTableSingle(singleDevice, map)
}

def _getNextLevelFan(singleDevice, action, childLabel='Master'){
    if(!checkIsFan(singleDevice,childLabel)) return
    if(action != 'dim' && action != 'brighten') return
    if(state.'devices'."${singleDevice.id}"?.level?.currentLevel) level = state.'devices'."${singleDevice.id}".'level'.'currentLevel'
    if(!state.'devices'."${singleDevice.id}"?.level?.currentLevel) level = singleDevice.currentSpeed

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
    if(state.'devices'."${singleDevice.id}"?.level?.currentLevel) level = state.'devices'."${singleDevice.id}".'level'.'currentLevel'
    if(!state.'devices'."${singleDevice.id}"?.level?.currentLevel) level = singleDevice.currentLevel

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
        putLog(1123,'error','ERROR: Failed to find dimSpeed in function getNextLevel with ' + appId,childLabel,True)
    }

    oldLevel = level
    if (action == 'dim'){
        newLevel = convertToInteger(Math.round(level / dimSpeed))
        if (newLevel == level) newLevel = newLevel - 1
        if(newLevel < 1) newLevel = 1
    }
    if (action == 'brighten'){
        newLevel = convertToInteger(Math.round(level * dimSpeed))
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
def scheduleChildEvent(timeMillis = '',timeValue = '',functionName,parameters,noPerformDisableCheck = False,appId){
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
            if(noPerformDisableCheck || (!noPerformDisableCheck && !Child.getDisabled())) {
                if(!functionName) {
                    putLog(1163,'warn','scheduleChildEvent given null for functionName from appId ' + appId + ' (timeMillis = ' + timeMillis + ', timeValue = ' + TimeValue + ')',Child.label,'True')
                    return
                }
                Child.setScheduleFromParent(timeMillis,functionName,parametersMap)
                if(parameters) parameters = ' (with parameters: ' + parameters + ')'
                putLog(1168,'debug','Scheduled ' + functionName + parameters + ' for ' + (now() + timeMillis),Child.label,'True')
            }
        }
    }
}

def changeMode(mode, childLabel = 'Master'){
    if(location.mode == mode) return
    message = 'Changed Mode from ' + oldMode + ' to '
    setLocationMode(mode)
    putLog(1178,'debug',message + mode,childLabel,'True')
}

// Send SMS text message to $phone with $message
//SMS IS NO LONGER SUPPORTED
def sendPushNotification(phone, message, childLabel = 'Master'){
    def now = new Date()getTime()
    seconds = (now - state.contactLastNotification) / 1000
    if(seconds < 361) {
        putLog(1187,'info','Did not send push notice for ' + evt.displayName + ' ' + evt.value + 'due to notification sent ' + seconds + ' ago.',childLabel,'True')
        return
    }

    state.contactLastNotification = now
    speechDevice.find{it ->
        if(it.id == deviceId) {
            if(it.deviceNotification(message)) putLog(1194,'debug','Sent phone message to ' + phone + ' "' + message + '"',childLabel,'True')
        }
    }
}

def sendVoiceNotification(deviceId,message, childLabel='Master'){
    if(!deviceId)  return
    speechDevice.find{it ->
        if(it.id == deviceId) {
            if(it.speak(text)) putLog(1203,'debug','Played voice message on ' + deviceId + ' "' + message + '"',childLabel,'True')
        }
    }
}

//Merges maps as left to right, where left takes precedence
def mergeMaps(Map leftMap, Map rightMap, deviceId){
    if(!deviceId) return
    if(!rightMap) return
    if(!state.'devices'?."${deviceId}"){
        state.'devices'."${deviceId}" = rightMap
        return
    }
    return rightMap.inject(leftMap.clone()) { map, entry ->
        if(map[entry.key] instanceof Map && entry.value instanceof Map) {

            map[entry.key] = mergeMaps(map[entry.key], entry.value, deviceId)
        } else {
            map[entry.key] = entry.value
        }
        state.'devices'."${deviceId}" =  map
    }
}

def updateTableFromSettings(singleDevice, appId) {
    if(!state.'devices'?."${singleDevice.id}") state.'devices'."${singleDevice.id}" = [:]
    def rightMap = [:]
    child.find { Child ->
        if(Child.id == appId) {
            rightMap['level'] = Child.buildSubMap('level')
            rightMap['temp'] = Child.buildSubMap('temp')
            rightMap['hue'] = Child.buildSubMap('hue')
            rightMap['sat'] = Child.buildSubMap('sat')
        }
    }
    leftMap = state.'devices'."${singleDevice.id}"
    mergeMaps(leftMap,rightMap, singleDevice.id) 
}


def removeScheduleFromTable(appId){
    if(!appId) return
    Child.find { Child ->
        if(Child.id == appId) {
            multiDevice = Child.getDevices()
            multiDevice.each{singleDevice->
                if(leftMap?."${singleDevice.id}"){
                    deviceMap = state.'devices'."${singleDevice.id}"
                    if(deviceMap?.'level'?.appId == appId || deviceMap?.'level'?.appType != 'schedule'){
                        //deviceMap?.'level'?.remove('startSeconds')
                        //deviceMap?.'level'?.remove('stopSeconds')
                        //deviceMap?.'level'?.remove('totalSeconds')
                        deviceMap?.'level'?.remove('startTime')
                        deviceMap?.'level'?.remove('stopTime')
                        deviceMap?.'level'?.remove('startLevel')
                        deviceMap?.'level'?.remove('stopLevel')
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
                    state."${singleDevice.id}" = deviceMap
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
        if(state.logLevel > 0) return true
        break
        case 'warn':
        if(state.logLevel > 1) return true
        break
        case 'info':
        if(state.logLevel > 2) return true
        break
        case 'trace':
        if(state.logLevel > 3) return true
        case 'debug':
        if(state.logLevel > 4) return true
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
