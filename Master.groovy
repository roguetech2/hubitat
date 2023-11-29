/***********************************************************************************************************************
*
*  Copyright (C) 2022 roguetech
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
*  Version: 0.3.10
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

preferences {
    page(name: 'mainPage')
}

// logLevel sets number of log messages
// 0 for none
// 1 for errors only
// 5 for all
def getLogLevel(){
    return 5
}

def mainPage() {
    infoIcon = '<img src="http://emily-john.love/icons/information.png" width=20 height=20>'
    errorIcon = '<img src="http://emily-john.love/icons/error.png" width=20 height=20>'
    moreOptions = " <font color='grey'>(click for more options)</font>"
    
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

def displayInfo(text = 'Null',noDisplayIcon = null){
    if(text == 'Null') {
        paragraph '<div style="background-color:AliceBlue"> </div>'
    } else {
        if(noDisplayIcon){
            paragraph "<div style=\"background-color:AliceBlue\"> &nbsp; &nbsp; $text</div>"
        } else {
            paragraph "<div style=\"background-color:AliceBlue\">$infoIcon $text</div>"
        }
    }
}

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
        putLog(269,'error',"Invalid value for action \"$action\" sent to singleLock function",childLabel)
    }
    putLog(271,'info',action + "ed $singleDevice",childLabel)
}

/*
// Waits in 10ms increments for devices to change states to on, for a total of 2 seconds
// This should only be called after all devices have been set on
// Must return true or false
def waitStateChange(action, singleDevice, childLabel = "Master"){
    for (loopCount = 0; loopCount < 200; loopCount++) {
        deviceState = singleDevice.currentValue("switch",true)
        // Retry turning on/off? What's the performance hit??
        setStateSingle(action,singleDevice,childLabel)
        // Don't use isOn, because it uses cached state
        if((action == "on" && deviceState == "off") || (action == "off" && deviceState == "on")) {
            pause(10)
        } else {
            loopCount = 201
        }
    }
    if((action == "on" && deviceState == "on") || (action == "off" && deviceState == "off")) {
        putLog(251,"debug","Waited " + (loopCount * 10) + " milliseconds for $singleDevice to turn on",childLabel)
        return true
    } else {
        putLog(254,"error","$singleDevice refused to turn $action in " + (loopCount * 10) + " milliseconds",childLabel)
        return
    }
}
*/


// deprecated function?
// If not, need to set priorLevel, and non-fan code is borked
def dim(action, multiDevice, appId, childLabel='Master'){
    if(action != 'dim' && action != 'brighten'){
        putLog(305,'error',"Invalid value for action \"$action\" sent to dim function",childLabel)
        return
    }

    multiDevice.each{singleDevice->
        defaults = [:]
        if(isOn(singleDevice,childLabel)){
            if(isFan(singleDevice,childLabel)){
                if(action == 'brighten'){
                    if(atomicState."deviceData${singleDevice.id}".'level'.'startLevel' == 'medium' || atomicState."deviceData${singleDevice.id}".'level'.'startLevel' == 'medium-high'){
                        atomicState."deviceData${singleDevice.id}".'startLevel' = 'high'
                    } else if(atomicState."deviceData${singleDevice.id}".'level'.'startLevel' == 'low' || atomicState."deviceData${singleDevice.id}".'level'.'startLevel' == 'medium-low'){
                        atomicState."deviceData${singleDevice.id}".'level'.'startLevel' = 'medium'
                    }
                } else if(action == 'dim'){
                    if(atomicState."deviceData${singleDevice.id}".'level'.'startLevel' == 'high' || atomicState."deviceData${singleDevice.id}".'level'.'startLevel' == 'medium-high'){
                        atomicState."deviceData${singleDevice.id}".'startLevel' = 'medium'
                    } else if(atomicState."deviceData${singleDevice.id}".'level'.'startLevel' == 'medium' || atomicState."deviceData${singleDevice.id}".'level'.'startLevel' == 'medium-low'){
                        atomicState."deviceData${singleDevice.id}".'level'.'startLevel' = 'low'
                    }
                }
            } else if(isDimmable(singleDevice,childLabel)){
                defaults.'startLevel' = nextLevel(singleDevice, action, childLabel)
            }
            atomicState."deviceData${singleDevice.id}".'level'.'stopLevel' = null
            atomicState."deviceData${singleDevice.id}".'level'.'totalSeconds' = null
            atomicState."deviceData${singleDevice.id}".'level'.'startSeconds' = null
            atomicState."deviceData${singleDevice.id}".'level'.'appId' = appId
            atomicState."deviceData${singleDevice.id}".'level'.'pro' = appId
        }
    }
    //updateDeviceMulti(multiDevice, appLabel)
    
    return
}


// Determine next dim or brighten level based on "multiplier
// action = "dim" or "brighten"
def nextLevel(singleDevice, action, childLabel='Master'){
    if(isFan(singleDevice,childLabel)){
        level = atomicState."deviceData${singleDevice.id}"?.'level'?.'startLevel' ? atomicState."deviceData${singleDevice.id}".'level'.'startLevel' : singleDevice.currentSpeed
        if(action == 'brighten'){
            if(!level || level == 0 || level == 'off'){
                updateStateSingle(singleDevice,'on',childLabel)
                newLevel = 'low'
            } else if(level == 'low' || level == 'medium-low') {
                newLevel = 'medium'
            } else if(level == 'medium' || level == 'medium-high'){
                newLevel = 'high'
            }
        } else if(action == 'dim'){
            if(level){
                if(level == 'high' || level == 'medium-high') {
                    newLevel = 'medium'
                } else if(level == 'medium' || level == 'medium-low'){
                    newLevel = 'low'
                }
            }
        }
        return newLevel
    }
                                                         
    level = atomicState."deviceData${singleDevice.id}"?.'level'?.'startLevel' ? atomicState."deviceData${singleDevice.id}".'level'.'startLevel' : singleDevice.currentLevel
    
    if(childLabel != 'Master'){
        childApps.each {Child->
            if(childLabel && Child.getLabel() == childLabel) dimSpeed = Child.getDimSpeed()
        }
        if(!dimSpeed){
            dimSpeed = 1.2
            putLog(376,'error','ERROR: Failed to find dimSpeed in function nextLevel',childLabel)
        }
    }
    if (action != 'dim' && action != 'brighten'){
        putLog(380,'error',"ERROR: Invalid value for action \"$action\" sent to nextLevel function",childLabel)
        return false
    }
    def newLevel = level as int
        if(level < 1) level = 1
        if(level > 100) level = 100
        if((action == 'dim' && level > 1) || (action ==  'brighten' && level < 100)){
            if (action == 'dim'){
                newLevel = convertToInteger(Math.round(level / dimSpeed))
                // make sure it changed by at least 1
                if (newLevel == level) newLevel = newLevel - 1
            } else if (action == 'brighten'){
                newLevel = convertToInteger(Math.round(level * dimSpeed))
                // make sure it changed by at least 1
                if (newLevel == level) newLevel = newLevel + 1
            }
        }

    if(newLevel > 100) newLevel = 100
    if(newLevel < 1) newLevel = 1

    return newLevel
}

// Changes mode to value of $mode
/* ************************************************************************ */
/* TO DO: Trigger reschedule of incremental so that any schedules suspended */
/* due to mode will restart. Need to enforce getting the device id from the */
/* requesting child app. Somehow.                                           */
/* ************************************************************************ */
def changeMode(mode, childLabel = 'Master', device = ''){
    if(location.mode == mode) return
    oldMode = location.mode
    setLocationMode(mode)
    putLog(414,'info',"Changed Mode from $oldMode to $mode",childLabel)
}

// Send SMS text message to $phone with $message
//SMS IS NO LONGER SUPPORTED
def sendPushNotification(phone, message, childLabel = 'Master'){
    putLog(420,'info',"Sent \"$message\" to $phone",childLabel)
    phone.deviceNotification(message)
    return true
}

// Test state of a group of switches
// Return true if any are on
def isOnMulti(multiDevice,childLabel='Master'){
    multiState = false
    multiDevice.each{singleDevice->
        if(isOn(singleDevice,childLabel)) return true
    }
    return false
}

// Test state of a single switch
def isOn(singleDevice,childLabel='Master'){
    // If no deviceState, set it
    if(!atomicState."deviceState${singleDevice.id}"){
        time = new Date().time
        atomicState."deviceState${singleDevice.id}" = ['state':singleDevice.currentValue('switch'),'time':time]
    }
    if(atomicState."deviceState${singleDevice.id}".'state' == 'on') return true
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
            putLog(494,'error',"ERROR: Multiplier $value is not valid",childLabel)
            return
        }
    }
    return value
}

// Validation functions for device capabilities

def isDimmable(singleDevice, childLabel='Master'){
    if(singleDevice) returnValue = singleDevice.hasCapability('SwitchLevel')
    return returnValue
}

def isTemp(singleDevice, childLabel='Master'){
    if(singleDevice) returnValue = singleDevice.hasCapability('ColorTemperature')
    return returnValue
}

def isColor(singleDevice, childLabel='Master'){
    if(singleDevice) returnValue = singleDevice.hasCapability('ColorMode')
    return returnValue
}

def isFan(singleDevice, childLabel='Master'){
    if(singleDevice) returnValue = singleDevice.hasCapability('FanControl')
    return returnValue
}

// Round fan level to high, medium or low
// Returns rounded value
// Only used when cycling down (other, cycle command is used)
def roundFanLevel(level, childLabel='Master'){
    if(level > 66){
        value = 'high'
    } else if (level < 66  && level > 33){
        value = 'medium'
    } else if (level > 0 && level < 33){
        value = 'low'
    }
    return value
}


// deprecated function?
def rescheduleIncrementalMulti(multiDevice,childLabel='Master'){
    multiDevice.each{singleDevice->
        rescheduleIncrementalSingle(singleDevice,childLabel)
    }
    return
}

// deprecated function?
def rescheduleIncrementalSingle(singleDevice,childLabel='Master'){
// Fix this to look for appIds in the table
    childApps.each {Child->
        if(Child.label.substring(0,7) == 'Time - ') {
            Child.timeDevice.each {ChildDevice->
                if(singleDevice.id == ChildDevice.id){
                    // If schedule is't active, runIncrementalSchedule will kick it out
                Child.runIncrementalSchedule()
                    match = true
                }
            }
        }
    }

    return match
}

// deprecated function?
def descheduleIncrementalMulti(multiDevice,childLabel='Master'){
    multiDevice.each{singleDevice->
        descheduleIncrementalSingle(singleDevice,childLabel)
    }
    return
}

// deprecated function?
def descheduleIncrementalSingle(singleDevice,childLabel='Master'){
    childApps.each {Child->
        if(Child.label.substring(0,7) == 'Time - ') {
            Child.timeDevice.each {ChildDevice->
                if(singleDevice.id == ChildDevice.id){
                    Child.deschedule()
                }
            }
        }
    }
    return
}

// Returns date one day ahead of $date
// Expects and returns format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
def getTomorrow(date,childLabel='Master'){
    returnValue = date + CONSTDayInMilli()
    //returnValue =  Date.parse("yyyy-MM-dd'T'HH:mm:ss", date).plus(30)
    return returnValue
}


// Returns date/time of sunrise in format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
// negative is true of false
def getSunrise(offset, childLabel='Master'){
    value = getSunriseAndSunset().sunrise.getTime()
    if(offset){
        value = value + offset * CONSTMinuteInMilli()
    }
    return value
}

// Returns date/time of sunset in format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
def getSunset(offset = false,childLabel='Master'){
    value = getSunriseAndSunset().sunset.getTime()
    if(offset){
        value = value + offset * CONSTMinuteInMilli()
    }
    return value
}

// Returns true if today is in $days map
def nowInDayList(days,childLabel='Master'){
    if(!days)  return true

    def df = new java.text.SimpleDateFormat('EEEE')
    df.setTimeZone(location.timeZone)
    def day = df.format(new Date())
    if(days.contains(day)) return true

    return false
}

// Returns true if today is in $months map
def nowInMonthList(months,childLabel='Master'){
    if(!months) return true

    month = new Date().getMonth() + 1
    if(months.contains(month.toString())) return true

    return false
}

// Returns true if now is between two dates
def timeBetween(timeStart, timeStop,childLabel='Master'){
    if(!timeStart) return true
    if(!timeStop) return true

    varNow = now()
    if(varNow > timeStart && varNow < timeStop){
        returnValue = true
    }

    return returnValue
}

def speakSingle(text,deviceId, childLabel='Master'){
    if(!deviceId) {
        putLog(651,'warn',"No speech device for \"$text\"",childLabel)
        return
    }
    speechDevice.each{
        if(it.id == deviceId){
            it.speak(text)
            putLog(657,'info',"Sending speech \"$text\" to device $it",childLabel)
            return true
        }
    }
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

// Correct order for children to invoke state and settings is:
// First, updateState
// Second, updateLevels
// Third, setState
// Fourth, setLevels
// UpdateDeviceTable checks the expected state of the device; setState uses deviceTable for initial level; 
// setLevels sets all levels (brightness,temp, hue & sat), so even if it's set when turning on, it needs to be run
def updateStateMulti(multiDevice,action,childLabel = 'Master'){
    if(!multiDevice || !action) return
    
    if(action != 'on' && action != 'off' && action != 'toggle') return

    multiDevice.each{singleDevice->
        updateStateSingle(singleDevice,action,childLabel)
    }
    return
}

def updateStateSingle(singleDevice,action,childLabel = 'Master'){
    time = new Date().time
    if(atomicState?."deviceState${singleDevice.id}" && atomicState."deviceState${singleDevice.id}".'state'){
// Is this what is messing up toggle?
        //if(atomicState."deviceState${singleDevice.id}".'state' == action){
        //    return
        //} else if(action != 'toggle'){
        if(action != 'toggle'){
            atomicState."deviceState${singleDevice.id}" = ['state':action,'time':time]
        } else if(action == 'toggle'){
            if(atomicState."deviceState${singleDevice.id}".'state' == 'on') {
                atomicState."deviceState${singleDevice.id}" = ['state':'off','time':time]
            } else if(atomicState."deviceState${singleDevice.id}".'state' == 'off') {
                atomicState."deviceState${singleDevice.id}" = ['state':'on','time':time]
            }
        }      
    } else {
        if(action!= 'toggle'){
            atomicState."deviceState${singleDevice.id}" = ['state':action,'time':time]
        } else {
            atomicState."deviceState${singleDevice.id}" = ['state':'on','time':time]
        }
    }
    return
}

def updateLevelsMulti(multiDevice, defaults,childLabel = 'Master'){
    // If table gets out of sync, or structure is change (eg with an update), uncomment line below to reset
    //atomicState.deviceData = null
    multiDevice.each{singleDevice->
        updateLevelsSingle(singleDevice, defaults,childLabel)
    }
    return
}

def updateLevelsSingle(singleDevice, defaults,childLabel = 'Master'){
    time = new Date().time

    if(atomicState."deviceData${singleDevice.id}") currentDeviceData = atomicState."deviceData${singleDevice.id}"
    if(!atomicState."deviceData${singleDevice.id}") currentDeviceData = [:]
    
    if((defaults?.'hue' || defaults?.'sat') && currentDeviceData.'temp') currentDeviceData.remove('temp')
        
        // If appId = not #, use it
        // If appId = # and time = "start, use it
        // If appId = # and time = "stop"
            // If startLevel
                // If prior appId # != appId
                    // Do nothing
                // If prior appId # = appId
                    // set stopLevel as startLevel, appId = "manual"
            // If not startLevel
                // set stopLevel as startLevel, appId = "manual"
        
        // If manual change, pico, etc.
            // Always use it
        // If starting schedule
            // Always use it
        // If stopping schedule
            // If no prior setting
                // Use stop as manual
            // If prior manual change
                   // Ignore it
            // If current schedule
                // If progressive change (shouldn't happen from schedule app)
                    // Clear it
                // If stop setting
                    // Use stop as manual
                // Blank
                    // Clear it
            // If different schedule
                // Ignore it
        // If resuming schedule
            // Clear it

    if(isDimmable(singleDevice)){
        if((!currentDeviceData.'level' && defaults?.'level' && !defaults.'level'?.'startLevel' && defaults.'level'?.'stopLevel') || 
           (currentDeviceData.'level' && defaults.'level' && isNumeric(defaults.'level'.'appId') && defaults.'level'.'time' == 'stop' && defaults.'level'.'startLevel' && isNumeric(currentDeviceData.'level'.'appId') && (currentDeviceData.'level'.'appId' == defaults.'level'.'appId' || !currentDeviceData.'level'.'stopLevel')) ||
           (currentDeviceData.'level' && defaults.'level' && isNumeric(defaults.'level'.'appId') && defaults.'level'.'time' == 'stop' && !defaults.'level'.'startLevel' && !isNumeric(currentDeviceData.'level'.'appId'))){
            currentDeviceData.'level' = ['startLevel':defaults.'level'.'stopLevel','appId':'manual','time':time]
        } else if((!currentDeviceData.'level' && defaults?.'level'?.'startLevel') ||
                  (currentDeviceData.'level' && defaults.'level' && isNumeric(defaults.'level'.'appId') && defaults.'level'?.'time' == 'start') ||
                  (currentDeviceData.'level' && defaults.'level' && !isNumeric(defaults.'level'.'appId'))){
            if(currentDeviceData.'level'?.'currentLevel') {
                defaults.'level'.'priorLevel' = currentDeviceData.'level'.'currentLevel'
                // setting both "prior" and "current" level the same, because we haven't actually updated the level yet
                defaults.'level'.'currentLevel' = currentDeviceData.'level'.'currentLevel'
            } else {
                if(isFan(singleDevice)){
                    defaults.'level'.'priorLevel' = singleDevice.currentSpeed
                } else {
                    defaults.'level'.'priorLevel' = convertToInteger(singleDevice.currentLevel,childLabel)
                }
                if(currentDeviceData?.'level'?.'startLevel') defaults.'level'.'currentLevel' = currentDeviceData.'level'.'startLevel'
            }
            currentDeviceData.'level' = defaults.'level'
            currentDeviceData.'level'.'time' = time
        } else if(currentDeviceData.'level' && defaults?.'level' && (defaults.'level'.'time' == 'resume' || (defaults.'level'.'time' == 'stop' && !defaults.'level'.'stopLevel' && currentDeviceData.'level'.'appId' == defaults.'level'.'appId'))){
            currentDeviceData.remove('level')
        }
    }
    
    if(isColor(singleDevice)){
        if((!currentDeviceData.'temp' && defaults?.'temp' && !defaults.'temp'?.'startLevel' && defaults.'temp'?.'stopLevel') || 
           (currentDeviceData.'temp' && defaults.'temp' && isNumeric(defaults.'temp'.'appId') && defaults.'temp'.'time' == 'stop' && defaults.'temp'.'startLevel' && isNumeric(currentDeviceData.'temp'.'appId') && (currentDeviceData.'temp'.'appId' == defaults.'temp'.'appId' || !currentDeviceData.'temp'.'stopLevel')) ||
           (currentDeviceData.'temp' && defaults.'temp' && isNumeric(defaults.'temp'.'appId') && defaults.'temp'.'time' == 'stop' && !defaults.'temp'.'startLevel' && !isNumeric(currentDeviceData.'temp'.'appId'))){
            currentDeviceData.'temp' = ['startLevel':defaults.'temp'.'stopLevel','appId':'manual','time':time]
        } else if((!currentDeviceData.'temp' && defaults?.'temp'?.'startLevel') ||
                  (currentDeviceData.'temp' && defaults.'temp' && isNumeric(defaults.'temp'.'appId') && defaults.'temp'?.'time' == 'start') ||
                  (currentDeviceData.'temp' && defaults.'temp' && !isNumeric(defaults.'temp'.'appId'))){
            if(currentDeviceData.'temp'?.'currentLevel') {
                defaults.'temp'.'priorLevel' = currentDeviceData.'temp'.'currentLevel'
                // setting both "prior" and "current" level the same, because we haven't actually updated the level yet
                defaults.'temp'.'currentLevel' = currentDeviceData.'temp'.'currentLevel'
            } else {
                defaults.'temp'.'priorLevel' = convertToInteger(singleDevice.currentTemperatureColor)
                if(currentDeviceData?.'temp'?.'startLevel') defaults.'temp'.'currentLevel' = currentDeviceData.'temp'.'startLevel'
            }
            currentDeviceData.'temp' = defaults.'temp'
            currentDeviceData.'temp'.'time' = time
        } else if(currentDeviceData.'temp' && defaults.'temp' && (defaults.'temp'.'time' == 'resume' || (defaults.'temp'.'time' == 'stop' && !defaults.'temp'.'stopLevel' && currentDeviceData.'temp'.'appId' == defaults.'temp'.'appId'))){
            currentDeviceData.remove('temp')
        }
    
        if((!currentDeviceData.'hue' && defaults?.'hue' && !defaults.'hue'?.'startLevel' && defaults.'hue'?.'stopLevel') || 
           (currentDeviceData.'hue' && defaults.'hue' && isNumeric(defaults.'hue'.'appId') && defaults.'hue'.'time' == 'stop' && defaults.'hue'.'startLevel' && isNumeric(currentDeviceData.'hue'.'appId') && (currentDeviceData.'hue'.'appId' == defaults.'hue'.'appId' || !currentDeviceData.'hue'.'stopLevel')) ||
           (currentDeviceData.'hue' && defaults.'hue' && isNumeric(defaults.'hue'.'appId') && defaults.'hue'.'time' == 'stop' && !defaults.'hue'.'startLevel' && !isNumeric(currentDeviceData.'hue'.'appId'))){
            currentDeviceData.'hue' = ['startLevel':defaults.'hue'.'stopLevel','appId':'manual','time':time]
        } else if((!currentDeviceData.'hue' && defaults?.'hue'?.'startLevel') ||
                  (currentDeviceData.'hue' && defaults.'hue' && isNumeric(defaults.'hue'.'appId') && defaults.'hue'?.'time' == 'start') ||
                  (currentDeviceData.'hue' && defaults.'hue' && !isNumeric(defaults.'hue'.'appId'))){
            if(currentDeviceData.'hue'?.'currentLevel') {
                defaults.'hue'.'priorLevel' = currentDeviceData.'hue'.'currentLevel'
                // setting both "prior" and "current" level the same, because we haven't actually updated the level yet
                defaults.'hue'.'currentLevel' = currentDeviceData.'hue'.'currentLevel'
            } else {
                defaults.'hue'.'priorLevel' = convertToInteger(singleDevice.currentHue)
                if(currentDeviceData?.'hue'?.'startLevel') defaults.'hue'.'currentLevel' = currentDeviceData.'hue'.'startLevel'
            }
            currentDeviceData.'hue' = defaults.'hue'
            currentDeviceData.'hue'.'time' = time
        } else if(currentDeviceData.'hue' && defaults.'hue' && (defaults.'hue'.'time' == 'resume' || (defaults.'hue'.'time' == 'stop' && !defaults.'hue'.'stopLevel' && currentDeviceData.'hue'.'appId' == defaults.'hue'.'appId'))){
            currentDeviceData.remove('hue')
        }
        if((!currentDeviceData.'sat' && defaults?.'sat' && !defaults.'sat'?.'startLevel' && defaults.'sat'?.'stopLevel') || 
           (currentDeviceData.'sat' && defaults.'sat' && isNumeric(defaults.'sat'.'appId') && defaults.'sat'.'time' == 'stop' && defaults.'sat'.'startLevel' && isNumeric(currentDeviceData.'sat'.'appId') && (currentDeviceData.'sat'.'appId' == defaults.'sat'.'appId' || !currentDeviceData.'sat'.'stopLevel')) ||
           (currentDeviceData.'sat' && defaults.'sat' && isNumeric(defaults.'sat'.'appId') && defaults.'sat'.'time' == 'stop' && !defaults.'sat'.'startLevel' && !isNumeric(currentDeviceData.'sat'.'appId'))){
            currentDeviceData.'sat' = ['startLevel':defaults.'sat'.'stopLevel','appId':'manual','time':time]
        } else if((!currentDeviceData.'sat' && defaults?.'sat'?.'startLevel') ||
                  (currentDeviceData.'sat' && defaults.'sat' && isNumeric(defaults.'sat'.'appId') && defaults.'sat'?.'time' == 'start') ||
                  (currentDeviceData.'sat' && defaults.'sat' && !isNumeric(defaults.'sat'.'appId'))){
            if(currentDeviceData.'sat'?.'currentLevel') {
                defaults.'sat'.'priorLevel' = currentDeviceData.'sat'.'currentLevel'
                // setting both "prior" and "current" level the same, because we haven't actually updated the level yet
                defaults.'sat'.'currentLevel' = currentDeviceData.'sat'.'currentLevel'
            } else {
                defaults.'sat'.'priorLevel' = convertToInteger(singleDevice.currentSaturation)
                if(currentDeviceData?.'sat'?.'startLevel') defaults.'sat'.'currentLevel' = currentDeviceData.'sat'.'startLevel'
            }
            currentDeviceData.'sat' = defaults.'sat'
            currentDeviceData.'sat'.'time' = time
        } else if(currentDeviceData.'sat' && defaults.'sat' && (defaults.'sat'.'time' == 'resume' || (defaults.'sat'.'time' == 'stop' && !defaults.'sat'.'stopLevel' && currentDeviceData.'sat'.'appId' == defaults.'sat'.'appId'))){
            currentDeviceData.remove('sat')
        }
    }

    atomicState."deviceData${singleDevice.id}" = currentDeviceData
    return
}

def setStateMulti(multiDevice,childLabel = 'Master'){
    // Set current time (used for progressive changes)
    currentTime = new Date()
    //currentTimeSeconds = new Date().time
    atomicState.currentSeconds = currentTime.format('HH').toInteger() * 3600 + currentTime.format('mm').toInteger() * 60 + currentTime.format('ss').toInteger()
            
    multiDevice.each{singleDevice->
        setStateSingle(singleDevice,childLabel)
    }
    atomicState.currentSeconds = null
}

def setStateSingleOff(singleDevice,childLabel = 'Master'){
    if(atomicState."deviceData${singleDevice.id}"){
        currentDeviceData = atomicState."deviceData${singleDevice.id}"
        if(currentDeviceData?.'level'?.'appId' && !isNumeric(atomicState."deviceData${singleDevice.id}".'level'.'appId')) currentDeviceData.remove('level')
        if(currentDeviceData?.'temp'?.'appId' && !isNumeric(atomicState."deviceData${singleDevice.id}".'temp'.'appId')) currentDeviceData.remove('temp')
        if(currentDeviceData?.'hue'?.'appId' && !isNumeric(atomicState."deviceData${singleDevice.id}".'hue'.'appId')) currentDeviceData.remove('hue')
        if(currentDeviceData?.'sat'?.'appId' && !isNumeric(atomicState."deviceData${singleDevice.id}".'sat'.'appId')) currentDeviceData.remove('sat')
        atomicState."deviceData${singleDevice.id}" = currentDeviceData
    }

    // this turns off during an incremental schedule
    if(isFan(singleDevice,childLabel)) singleDevice.setSpeed('off')
    if(!isFan(singleDevice,childLabel)) singleDevice.off()
}

def setStateSingleFan(singleDevice,defaults,childLabel = 'Master'){
    if(!isFan(singleDevice,childLabel)) return
    
    handleFanWithStartLevel(singleDevice,defaults,childLabel)
    handleFanWithoutStartLevel(singleDevice,defaults,childLabel)
    return true
}

def handleFanWithStartLevel(singleDevice,defaults,childLabel = 'Master'){
    if(!defaults?.'level'?.'startLevel') return
    
    time = new Date().time
    defaults.'level'.'currentLevel' = defaults.'level'.'startLevel'
    defaults.'level'.'priorLevel' = atomicState."deviceData${singleDevice.id}"?.'level'?.'currentLevel' ? atomicState."deviceData${singleDevice.id}".'level'.'currentLevel' : singleDevice.currentSpeed
    defaults.'level'.'time' = time
    if(singleDevice.currentSpeed == atomicState."deviceData${singleDevice.id}".'level'.'startLevel'){
        singleDevice.setSpeed('on')
        putLog(932,'debug','Turning ' + singleDevice + ' on',childLabel)
    }
    if(singleDevice.currentSpeed != defaults.'level'.'startLevel'){
        singleDevice.setSpeed(atomicState."deviceData${singleDevice.id}".'level'.'startLevel')
        putLog(936,'debug','Setting ' + singleDevice + ' to ' + defaults.'level'.'startLevel',childLabel)
    }
    if(defaults.'level'?.'time' == 'stop') defaults.remove('level')
}
def handleFanWithoutStartLevel(singleDevice,defaults,childLabel = 'Master'){
    if(defaults?.'level'?.'startLevel') return
    
    if(singleDevice.currentSpeed == 'high'){
        singleDevice.setSpeed('on')
        putLog(945,'debug','Turning ' + singleDevice + ' on',childLabel)
        return true
    }
    singleDevice.setSpeed('high')
    putLog(949,'debug','Setting ' + singleDevice + ' to high',childLabel)
}

def setStateSingleDimmable(singleDevice,defaults,childLabel = 'Master'){
    if(isFan(singleDevice,childLabel)) return
    if(isColor(singleDevice,childLabel)) return
    if(!isDimmable(singleDevice,childLabel)) return

    handleDimmableWithStartLevel(singleDevice,defaults,childLabel)
    handleDimmableWithoutStartLevel(singleDevice,defaults,childLabel)
    return true
}
def handleDimmableWithStartLevel(singleDevice,defaults,childLabel = 'Master'){
    if(!defaults?.'level'?.'startLevel') return

    time = new Date().time
    defaults.'level'.'currentLevel' = defaults.'level'.'startLevel'
    defaults.'level'.'priorLevel' = atomicState."deviceData${singleDevice.id}"?.'level'?.'currentLevel' ? atomicState."deviceData${singleDevice.id}".'level'.'currentLevel' : singleDevice.currentLevel
    defaults.'level'.'time' = time
    if(singleDevice.currentLevel == defaults.'level'.'startLevel'){
        singleDevice.on()
        putLog(970,'debug','Turning ' + singleDevice + ' on',childLabel)
    }
    if(singleDevice.currentLevel != defaults.'level'.'startLevel'){
        singleDevice.setLevel(defaults.'level'.'startLevel')
        putLog(974,'debug','Setting ' + singleDevice + ' to ' + defaults.'level'.'startLevel',childLabel)
    }
    if(defaults.'level'?.'time' == 'stop') defaults.remove('level')
}
def handleDimmableWithoutStartLevel(singleDevice,defaults,childLabel = 'Master'){
    if(defaults?.'level'?.'startLevel') return
    if(singleDevice.currentLevel == 100){
        singleDevice.on()
        putLog(982,'debug','Turning ' + singleDevice + ' on',childLabel)
        return
    }
    singleDevice.setLevel(100)
    putLog(986,'debug','Setting ' + singleDevice + ' to 100%',childLabel)
}

def setStateSingleColor(singleDevice,defaults,childLabel = 'Master'){
    if(!isColor(singleDevice,childLabel)) return

    if(!handleColorWithoutStartLevel(singleDevice,defaults,childLabel)){
        time = new Date().time
        // Set current time (used for progressive changes)
        // Set as atomicState in setStateMulti
        if(!atomicState.currentSeconds){
            // Set current time (used for progressive changes)
            currentTime = new Date()
            //currentTimeSeconds = new Date().time
            currentSeconds = currentTime.format('HH').toInteger() * 3600 + currentTime.format('mm').toInteger() * 60 + currentTime.format('ss')
        } else {
            currentSeconds = atomicState.currentSeconds
        }

        // Get start level
        // Should be dimmable, since it must be color, but check anyways
        if(defaults.'level' && isDimmable(singleDevice,childLabel)){
            // If start and stop level, and level set by a schedule
            if(defaults.'level'.'startLevel' && isNumeric(defaults.'level'.'appId')){
                // Check if the schedule is active (wrong mode or disabled)
                childApps.each { Child ->
                    if(convertToInteger(Child.id) == defaults.'level'.'appId') levelScheduleActive = Child.getScheduleActive()
                }
                // If schedule is active
                if(levelScheduleActive){
                    if(defaults.'level'.'stopLevel'){
                        // If no seconds elapsed
                        if(currentSeconds == defaults.'level'.'startSeconds'){
                            levelStart = defaults.'level'.'startLevel'
                        } else {
                            // Calculate dynamic value
                            levelPercent = (currentSeconds - defaults.'level'.'startSeconds') / defaults.'level'.'totalSeconds'
                            if(defaults.'level'.'startLevel' < defaults.'level'.'stopLevel'){
                                levelStart = convertToInteger((defaults.'level'.'startLevel' + Math.round((defaults.'level'.'stopLevel' - defaults.'level'.'startLevel') * levelPercent)))
                            } else {
                                levelStart = convertToInteger((defaults.'level'.'startLevel' - Math.round((defaults.'level'.'startLevel' - defaults.'level'.'stopLevel') * levelPercent)))
                            }
                        }
                    } else {
                        levelStart = defaults.'level'.'startLevel'
                    }
                }
                if(!levelStart) putLog(1033,'warn',"$singleDevice level data = " + defaults.'level' + " for schedule id " + defaults.'level'.'appId' + " but schedule isn't active",childLabel)
                // If just start level
            } else if(defaults.'level'.'startLevel'){
                levelStart = defaults.'level'.'startLevel'
            }

            // Set currentLevel
            if(defaults.'level'?.'time' == 'stop'){
                defaults.remove('level')
            } else if(levelStart) {
                if(levelStart == singleDevice.currentLevel) {
                    levelStart = null
                } else {
                    levelStart = levelStart.toFloat().toInteger()
                    defaults.'level'.'currentLevel' = levelStart
                    defaults.'level'.'priorLevel' = atomicState."deviceData${singleDevice.id}"?.'level'?.'currentLevel' ? atomicState."deviceData${singleDevice.id}".'level'.'currentLevel' : singleDevice.currentLevel
                    defaults.'level'.'time' = time
                }
            }
        }

        // Set hue and/or sat

        // Get start hue
        if(defaults.'hue'){
            // If start and stop level, and hue set by a schedule
            if(defaults.'hue'.'startLevel' && isNumeric(defaults.'hue'.'appId')){
                // If same progressive schedule as level, we know it's active and elapsed percent
                if(defaults.'hue'.'appId' == defaults?.'level'?.'appId'){
                    hueScheduleActive = levelScheduleActive
                    huePercent = levelPercent
                } else {
                    // Check if the schedule is active (wrong mode or disabled)
                    childApps.each { Child ->
                        if(Child.id == defaults.'hue'.'appId'.toInteger()) hueScheduleActive = Child.getScheduleActive()
                    }
                }
                // If schedule is active
                if(hueScheduleActive){
                    if(defaults.'hue'.'stopLevel'){
                        // If no seconds elapsed
                        if(currentSeconds == defaults.'hue'.'startSeconds'){
                            hueStart = defaults.'hue'.'startLevel'
                        } else {
                            // Calculate dynamic value
                            if(!huePercent) huePercent = (currentSeconds - defaults.'hue'.'startSeconds') / defaults.'hue'.'totalSeconds'
                            if(defaults.'hue'.'stopLevel' > defaults.'hue'.'startLevel' && defaults.'hue'.'direction' == 'forward'){
                                // hueOn=25, hueOff=75, going 25, 26...74, 75
                                hueStart = defaults.'hue'.'startLevel' + Math.round((defaults.'hue'.'stopLevel' - defaults.'hue'.'startLevel') * huePercent)
                                // hueOn=25, hueOff=75, going 25, 24 ... 2, 1, 100, 99 ... 76, 75
                            } else if(defaults.'hue'.'stopLevel' > defaults.'hue'.'startLevel' && defaults.'hue'.'direction' == 'reverse'){
                                hueStart = defaults.'hue'.'startLevel' - Math.round((100 - defaults.'hue'.'stopLevel' + defaults.'hue'.'startLevel') * huePercent)
                                if(hueStart <1) hueStart += 100
                                //hueOn=75, hueOff=25, going 75, 76, 77 ... 99, 100, 1, 2 ... 24, 25
                            } else if(defaults.'hue'.'stopLevel' < defaults.'hue'.'startLevel' && defaults.'hue'.'direction' == 'forward'){
                                hueStart = defaults.'hue'.'startLevel' + Math.round((defaults.'hue'.'stopLevel' + 100 - defaults.'hue'.'startLevel') * huePercent)
                                if(hueStart > 100) hueStart = hueStart - 100
                                //hueOn=75, hueOff=25, going 75, 74 ... 26, 25
                            } else if(defaults.'hue'.'stopLevel' < defaults.'hue'.'startLevel' && defaults.'hue'.'direction' == 'reverse'){
                                hueStart = defaults.'hue'.'startLevel' -  Math.round((defaults.'hue'.'startLevel' - defaults.'hue'.'stopLevel') * huePercent)
                            }
                        }
                    } else {
                        hueStart = defaults.'hue'.'startLevel'
                    }
                }
                if(!hueStart) putLog(1099,'warn',"$singleDevice hue data = " + defaults.'hue' + " for schedule id " + defaults.'hue'.'appId' + " but schedule isn't active",childLabel)
                // If just start hue
            } else if(defaults.'hue'.'startLevel'){
                hueStart = defaults.'hue'.'startLevel'
            } else if(!isNumeric(defaults.'hue'.'appId')){
                putLog(1104,'error',"ERROR: $singleDevice hue data = " + defaults + "; Hue node without start hue",childLabel)
            }

            // Set currentHue
            if(defaults.'hue'?.'time' == 'stop'){
                defaults.remove('hue')
            } else if(hueStart) {
                if(hueStart == singleDevice.currentHue && singleDevice.currentColorMode == 'RGB') {
                    hueStart = null
                } else {
                    defaults.'hue'.'currentLevel' = hueStart
                    defaults.'hue'.'priorLevel' = atomicState."deviceData${singleDevice.id}"?.'hue'?.'currentLevel' ? atomicState."deviceData${singleDevice.id}".'hue'.'currentLevel' : singleDevice.currentHue
                    defaults.'hue'.'time' = time
                }
            }
        }

        // Get start sat
        if(defaults.'sat'){
            // If start and stop level, and level set by a schedule
            if(defaults.'sat'.'startLevel' && isNumeric(defaults.'sat'.'appId')){
                // If same progressive schedule as hue, we know it's active and elapsed percent
                if(defaults.'sat'.'appId' == defaults?.'hue'?.'appId'){
                    satScheduleActive = hueScheduleActive
                    satPercent = huePercent
                } else {
                    // Check if the schedule is active (wrong mode or disabled)
                    childApps.each { Child ->
                        if(Child.id == defaults.'sat'.'appId'.toInteger()) satScheduleActive = Child.getScheduleActive()
                    }
                }
                // If schedule is active
                if(satScheduleActive){
                    if(defaults.'sat'.'stopLevel'){
                        // If no seconds elapsed
                        if(currentSeconds == defaults.'sat'.'startSeconds'){
                            satStart = defaults.'sat'.'startLevel'
                        } else {
                            // Calculate dynamic value
                            if(!satPercent) satPercent = (currentSeconds - defaults.'sat'.'startSeconds') / defaults.'sat'.'totalSeconds'
                            if(defaults.'sat'.'startLevel' > defaults.'sat'.'stopLevel'){
                                // start=75, stop=25 - 75 - (75 - 25) * .25 = 62.5
                                satStart = convertToInteger(defaults.'sat'.'startLevel' - Math.round((defaults.'sat'.'startLevel' - defaults.'sat'.'stopLevel') * satPercent))
                                if(satStart > 100) satStart = satStart - 100
                            } else {
                                // start=25,stop=75 - 25 + (75 - 25) * .25 = 37.5
                                satStart = convertToInteger(defaults.'sat'.'startLevel' + Math.round((defaults.'sat'.'stopLevel' - defaults.'sat'.'startLevel') * satPercent))
                            }
                        }
                    } else {
                        satStart = defaults.'sat'.'startLevel'
                    }
                }
                if(!satStart) putLog(1157,'warn',"$singleDevice sat data = " + defaults.'sat' + " for schedule id " + defaults.'sat'.'appId' + " but schedule isn't active",childLabel)
                // If just start sat
            } else if(defaults.'sat'.'startLevel'){
                satStart = defaults.'sat'.'startLevel'
            } else if(!isNumeric(defaults.'sat'.'appId')){
                putLog(1162,'error',"ERROR: $singleDevice sat data = $defaults; Sat node without start sat",childLabel)
            }

            // Set currentSat
            if(defaults.'sat'?.'time' == 'stop'){
                defaults.remove('sat')
            } else if(satStart) {
                if(satStart == singleDevice.currentSaturation && singleDevice.currentColorMode == 'RGB') {
                    satStart = null
                } else {
                    defaults.'sat'.'currentLevel' = satStart
                    defaults.'sat'.'priorLevel' = atomicState."deviceData${singleDevice.id}"?.'sat'?.'currentLevel' ? atomicState."deviceData${singleDevice.id}".'sat'.'currentLevel' : singleDevice.currentSaturation
                    defaults.'sat'.'time' = time
                }
            }
        }

        // Get temp
        // Hue && sat take priority over temp
        if(!hueStart && !satStart && defaults.'temp'){
            // If start and stop level, and level set by a schedule
            if(defaults.'temp'.'startLevel' && isNumeric(defaults.'temp'.'appId')){
                // If same progressive schedule as level, we know it's active and elapsed percent
                if(defaults.'temp'.'appId' == defaults?.'level'?.'appId'){
                    tempScheduleActive = levelScheduleActive
                    tempPercent = levelPercent
                } else {
                    // Check if the schedule is active (wrong mode or disabled)
                    childApps.each { Child ->
                        if(Child.id == defaults.'temp'.'appId'.toInteger()) tempScheduleActive = Child.getScheduleActive()
                    }
                }
                // If schedule is active
                if(tempScheduleActive){
                    if(defaults.'temp'.'stopLevel'){
                        // If no seconds elapsed
                        if(currentSeconds == defaults.'temp'.'startSeconds'){
                            tempStart = defaults.'temp'.'startLevel'
                        } else {
                            // Calculate dynamic value
                            if(!tempPercent) tempPercent = (currentSeconds - defaults.'temp'.'startSeconds') / defaults.'temp'.'totalSeconds'
                            if(defaults.'temp'.'startLevel' < defaults.'temp'.'stopLevel'){
                                tempStart = convertToInteger((defaults.'temp'.'startLevel' + Math.round((defaults.'temp'.'stopLevel' - defaults.'temp'.'startLevel') * tempPercent)))
                            } else {
                                tempStart = convertToInteger((defaults.'temp'.'startLevel' - Math.round((defaults.'temp'.'startLevel' - defaults.'temp'.'stopLevel') * tempPercent)))
                            }
                        }
                    } else {
                        tempStart = defaults.'temp'.'startLevel'
                    }
                }
                if(!tempStart) putLog(1213,'warn',"$singleDevice temp data = " + defaults.'temp' + " for schedule id " + defaults.'temp'.'appId' + " but schedule isn't active",childLabel)
                // If just start temp
            } else if(defaults.'temp'.'startLevel'){
                tempStart = defaults.'temp'.'startLevel'
            } else if(isOn(singleDevice,childLabel) && singleDevice.currentSwitch != 'on'){
                tempStart = 3500
            }

            // Set currentTemp
            if(defaults.'temp'?.'time' == 'stop'){
                defaults.remove('temp')
            } else if(tempStart) {
                // Allow temp to be within 5% margin
                maxValue = tempStart / 20
                if(tempStart > singleDevice.currentColorTemperature - maxValue && tempStart < singleDevice.currentColorTemperature + maxValue  && singleDevice.currentColorMode == 'CT') {
                    tempStart = null
                } else {
                    tempStart = tempStart.toFloat().toInteger()
                    defaults.'temp'.'currentLevel' = tempStart
                    defaults.'temp'.'priorLevel' = atomicState."deviceData${singleDevice.id}"?.'temp'?.'currentLevel' ? atomicState."deviceData${singleDevice.id}".'temp'.'currentLevel' : singleDevice.currentColorTemperature
                    defaults.'temp'.'time' = time
                }
            }
        }

        // Update the device table
        atomicState."deviceData${singleDevice.id}" = defaults

        //Turn it on in fewest possible steps
        if(hueStart || satStart){
            colorMap = [:]
            unit = hiRezHue ? '°' : '%'
            message = ''
            if(levelStart) {
                message = "brightness to $levelStart% - "
                colorMap.'level' = levelStart
            }
            if(hueStart) message += "hue to $hueStart% - "
            if(satStart) message += "sat to $satStart$unit"

            colorMap.'hue' = hueStart ? hueStart : singleDevice.currentHue
            colorMap.'saturation' = satStart ? satStart : singleDevice.currentSaturation

            singleDevice.setColor(colorMap)
            putLog(1257,'info',"Set $singleDevice $message",childLabel)
            if(settings['colorStaging']){
                pauseExecution(200)
                singleDevice.on()
                putLog(1261,'info',"Set $singleDevice on",childLabel)
            }
            putLog(1263,message,'info',childLabel)
        } else if(levelStart && isDimmable(singleDevice,childLabel)){
            if(tempStart){
                singleDevice.setColorTemperature(tempStart)
                putLog(1267,'info',"Set $singleDevice temperature color set to " + tempStart + "K",childLabel)
                pauseExecution(200)
            }
            if(singleDevice.currentLevel != levelStart){
                singleDevice.setLevel(levelStart)
                putLog(1272,'info',"Set $singleDevice brightness to $levelStart%",childLabel)
            } else {
                singleDevice.on()
                putLog(1275,'info',"Turned $singleDevice on",childLabel)
            }
        } else if(tempStart && isColor(singleDevice,childLabel)){
            singleDevice.setColorTemperature(tempStart)
            putLog(1279,'info',"Set $singleDevice temperature color set to " + tempStart + "K",childLabel)
            if(settings['colorStaging']){
                pauseExecution(200)
                singleDevice.on()
                putLog(1283,'info',"Set $singleDevice on",childLabel)
            }
        } else {
            if(singleDevice.currentValue('switch') != 'on') putLog(1286,'info',"Set $singleDevice on",childLabel)
            singleDevice.on()
        }
    }
    return true
}

def handleColorWithoutStartLevel(singleDevice,defaults,childLabel = 'Master'){
    if(defaults?.'level') return
    if(defaults?.'temp') return
    if(defaults?.'hue') return
    if(defaults?.'sat') return
    if(singleDevice.currentSwitch == 'on') return

    if(singleDevice.currentLevel == 100){
        singleDevice.on()
        putLog(1302,'debug','Turning ' + singleDevice + 'on',childLabel)
    }
    if(singleDevice.currentLevel != 100){
        singleDevice.setLevel(100)
        putLog(1306,'debug','Turning ' + singleDevice + 'on by setting to 100%',childLabel)
    }
    if(singleDevice.currentColorMode == 'RGB'){
        pauseExecution(200)
        singleDevice.currentColorMode == 'CT'
        putLog(1311,'debug','Set color mode to "CT"',childLabel)
    }
    //this needs a fudge check
    if(singleDevice.currentTemperatureColor != 3500){
        pauseExecution(200)
        singleDevice.setColorTemperature(3500)
        putLog(1321,'debug','Set ' + singleDevice + ' temperature color to 3500K',childLabel)
    }

    return true
}

def setStateSingle(singleDevice,childLabel = 'Master'){
    if(!atomicState."deviceState${singleDevice.id}") return
    
    // If turning off device, do it, and exit
    if(atomicState."deviceState${singleDevice.id}".'state' == 'off'){
        setStateSingleOff(singleDevice,childLabel)
        //atomicState."deviceState${singleDevice.id}" = ['time':time] 'Not sure why we did this, but it wipes the state, only stores time
        putLog(1330,'debug',"Turning $singleDevice off",childLabel)
        return
    }

    if(atomicState."deviceData${singleDevice.id}") defaults = atomicState."deviceData${singleDevice.id}"
    
    if(setStateSingleFan(singleDevice,defaults,childLabel)) atomicState."deviceData${singleDevice.id}" = defaults
    if(setStateSingleDimmable(singleDevice,defaults,childLabel)) atomicState."deviceData${singleDevice.id}" = defaults
    if(!setStateSingleColor(singleDevice,defaults,childLabel){
        if(singleDevice.currentValue('switch') != 'on') putLog(1339,'info',"Set $singleDevice on",childLabel)
        singleDevice.on()
    }
    return
}
      def setColorMode

// Determines whether there will be an incremental change
// Hopefully shaves off a few milliseconds except in rare case when there is a change
def checkIncrementalUpdates(multiDevice,defaults,childLabel = 'Master'){
    multiDevice.each{singleDevice->
        if((defaults.'level' && defaults.'level'.'time' == 'increment') || (defaults.'temp' && defaults.'temp'.'time' == 'increment') || (defaults.'sat' && defaults.'sat'.'time' == 'increment') || (defaults.'hue' && defaults.'hue'.'time' == 'increment')){
            if(atomicState."deviceData${singleDevice.id}"){
                if((!defaults.'level' || (atomicState."deviceData${singleDevice.id}".'level' && atomicState."deviceData${singleDevice.id}".'level'.'startLevel')) &&
                   (!defaults.'temp' || (!atomicState."deviceData${singleDevice.id}".'temp' && atomicState."deviceData${singleDevice.id}".'temp'.'startLevel')) &&
                   (!defaults.'hue' || (atomicState."deviceData${singleDevice.id}".'hue' && atomicState."deviceData${singleDevice.id}".'hue'.'startLevel')) &&
                   (!defaults.'sat' || (atomicState."deviceData${singleDevice.id}".'sat' && atomicState."deviceData${singleDevice.id}".'sat'.'startLevel'))){
                    returnValue = false
                } else {
                    returnValue = true
                }
            } else {
                returnValue = true
            }
        } else {
            returnValue = true
        }
    }
    return returnValue
}

def CONSTDayInMilli(){
    return 86400000
}

def CONSTHourInMilli(){
    return 3600000
}

def CONSTMinuteInMilli(){
    return 60000
}

def convertToInteger(value, childLabel = 'Master'){
    if(value instanceof Integer) return value
    if(value instanceof Long) return value
    if(value instanceof String && value.isInteger()) return value.toInteger()
    return
}

def convertToString(value, childLabel = 'Master'){
    if(!value) return
    if(value instanceof String) return value
    return value.toString()
}

def getPeopleHome(mutlidevice,childLabel = 'Master'){
    peopleHome = true
    if(settings['personHome']){
        settings['personHome'].each{
            if(it.currentPresence != 'present') peopleHome = false
        }
    }
    return peopleHome
}

def getNooneHome(mutlidevice,childLabel = 'Master'){
    nooneHome = true
    if(settings['personHome']){
        settings['personHome'].each{
            if(it.currentPresence == 'present') nooneHome = false
        }
    }
    return nooneHome
}

def getLastLevelChange(singleDevice,childLabel = 'Master'){
    if(!isOn(singleDevice,childLabel)) return
    deviceId = singleDevice.id.toInteger()
    if(!atomicState."deviceData${deviceId}") return
    if(!atomicState."deviceData${deviceId}".'level') return
    time = new Date().time
    timeDifference = time - atomicState."deviceData${deviceId}".'level'.'time'

    returnValue = ['timeDifference':timeDifference,'currentLevel':atomicState."deviceData${deviceId}".'level'.'currentLevel','priorLevel': atomicState."deviceData${deviceId}".'level'.'priorLevel','appId':atomicState."deviceData${deviceId}".'level'.'appId']

    return returnValue
}

def getLastStateChange(singleDevice,childLabel = 'Master'){
    if(!atomicState."deviceState${singleDevice.id}" || atomicState."deviceState${singleDevice.id}".'state' == 'off') return
    time = new Date().time
    timeDifference = time - atomicState."deviceState${singleDevice.id}".'time'
    return timeDifference
}

def getLastHueChange(singleDevice,childLabel = "Master"){
    if(!isOn(singleDevice,childLabel)) return
    deviceId = singleDevice.id.toInteger()
    if(!atomicState."deviceData${deviceId}") return
    if(!atomicState."deviceData${deviceId}".'hue') return
    time = new Date().time
    timeDifference = time - atomicState."deviceData${deviceId}".'hue'.'time'
    return ['timeDifference':timeDifference,'currentLevel':atomicState."deviceData${deviceId}".'hue'.'currentLevel','priorLevel': atomicState."deviceData${deviceId}".'hue'.'priorLevel','appId':atomicState."deviceData${deviceId}".'hue'.'appId']
}

def getLastSatChange(singleDevice,childLabel = 'Master'){
    if(!isOn(singleDevice,childLabel)) return
    deviceId = singleDevice.id.toInteger()
    if(!atomicState."deviceData${deviceId}") return
    if(!atomicState."deviceData${deviceId}".'sat') return
    time = new Date().time
    timeDifference = time - atomicState."deviceData${deviceId}".'sat'.'time'
    return ['timeDifference':timeDifference,'currentLevel':atomicState."deviceData${deviceId}".'sat'.'currentLevel','priorLevel': atomicState."deviceData${deviceId}".'sat'.'priorLevel','appId':atomicState."deviceData${deviceId}".'sat'.'appId']
}

def getLastTempChange(singleDevice,childLabel = 'Master'){
    if(!isOn(singleDevice,childLabel)) return
    deviceId = singleDevice.id.toInteger()
    if(!atomicState."deviceData${deviceId}") return
    if(!atomicState."deviceData${deviceId}".'temp') return
    time = new Date().time
    timeDifference = time - atomicState."deviceData${deviceId}".'temp'.'time'
    return ['timeDifference':timeDifference,'currentLevel':atomicState."deviceData${deviceId}".'temp'.'currentLevel','priorLevel': atomicState."deviceData${deviceId}".'temp'.'priorLevel','appId':atomicState."deviceData${deviceId}".'temp'.'appId']
}

def getPresenceDevice(childLabel = 'Master'){
    if(settings['presenceDevice']) return settings['presenceDevice']
    return false
}

def isNumeric(strNum) {
    if (strNum == null) {
        return false;
    }
    try {
        strNum as Integer
        return true
    }catch (e) {
        return false
    }
}

def normalPrintDateTime(datetime){
    return new Date(datetime).format('h:mma MMM dd, yyyy', location.timeZone)
}

def checkLog(type = null){
    if(!state.logLevel) getLogLevel()
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
        case 'debug':
        if(state.logLevel == 5) return true
        break
        case 'trace':
        if(state.logLevel > 3) return true
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber, type = 'trace', message = null, childLabel = 'Master'){
    if(!checkLog(type)) return
    errorText = ''
    if(type == 'error') errorText = '<font color="red">'
    if(type == 'warn') errorText = '<font color="brown">'
    if(lineNumber) lineText = "(line $lineNumber) "
    if(childLabel != 'Master') appText = "[$childLabel] "
    if(message) messageText = "-- $message"
    logMessage = errorText + "Master $app.label $lineText $appText $messageText"
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
