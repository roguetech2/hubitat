/***********************************************************************************************************************
*
*  Copyright (C) 2020 roguetech
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
*  Version: 0.2.01
*
***********************************************************************************************************************/

/***********************************************************************************************************************
*
*  Speech device is set as "speechSynthesis". The device capability must be set in the driver, or change this to
*  "voice".
*
***********************************************************************************************************************/

definition(
    name: "Master",
    namespace: "master",
    singleInstance: true,
    author: "roguetech",
    description: "Parent app for automation suite",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/home2-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/home2-icn@2x.png"
)

preferences {
    page(name: "mainPage")

}

def mainPage() {
    infoIcon = "<img src=\"http://files.softicons.com/download/system-icons/windows-8-metro-invert-icons-by-dakirby309/ico/Folders%20&%20OS/Info.ico\" width=20 height=20>"
    return dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
        if(!state.masterInstalled) {
            section("Click Done.") {
            }

        } else {
            if(showPresences){
                section(""){
                    input "colorLights", "capability.switchLevel", title: "Select all color lights:", multiple: true, required: false, submitOnChange:true
                    paragraph "<div style=\"background-color:AliceBlue\">$infoIcon These color lights will be used for flashing alerts.</div>"
                    input "people", "capability.presenceSensor", title: "Select all people:", multiple: true, required: false, submitOnChange:true
                    paragraph "<div style=\"background-color:AliceBlue\">$infoIcon These people will be used for \"anyone\" and \"everyone\" conditions in any presence-based condition, and allow birthday options.</div>"
                    input "notificationDevice", "capability.speechSynthesis", title: "Select notification device(s):", multiple: false, required: false, submitOnChange:true
                    paragraph "<div style=\"background-color:AliceBlue\">$infoIcon This will be used for voice alerts.</div>"
                    input "sensors", "capability.battery", title: "Select all sensors:", multiple: true, required: false, submitOnChange:true
                    input "phone", "phone", title: "Number to text alert? (Optional)", required: false, submitOnChange:true
                    paragraph "<div style=\"background-color:AliceBlue\">$infoIcon These will be periodically tested to make sure they are reporting properly. Enter phone number for text notice.</div>"

                }
                /* ********************************************* */
                /* TO-DO: Finish BDay code. Not complete.        */
                /* Try to move it to child app, using this code: */
                /* https://community.hubitat.com/t/parent-function-to-return-settings-state-data-to-child-app/2261/3 */
                /* ********************************************* */
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
                if(showSchedules){
                    section("Scheduled settings:") {
                        app(name: "childApps", appName: "Master - Time", namespace: "master", title: "New Schedule", multiple: true)
                    }
                }           
            }
            if(showPresences){
                section("Presence settings:") {
                    app(name: "childApps", appName: "Master - Presence", namespace: "master", title: "New Presence", multiple: true)
                }
            }
            if(showPicos){
                section("Picos:") {
                    app(name: "childApps", appName: "Master - Pico", namespace: "master", title: "New Pico", multiple: true)
                }
            }
            if(showMagicCubes){
                section("MagicCubes:") {
                    app(name: "childApps", appName: "Master - MagicCube", namespace: "master", title: "New MagicCube", multiple: true)
                }
            }
            if(showContacts){
                section("Contact sensors:") {
                    app(name: "childApps", appName: "Master - Contact", namespace: "master", title: "New Contact Sensor", multiple: true)
                }
            }
            if(showHumidity){
                section("Humidity sensors:") {
                    app(name: "childApps", appName: "Master - Humidity", namespace: "master", title: "New Humidity Sensor", multiple: true)
                }
            }
            if(showWasher){
                section("Washer-dryer sensors:") {
                    app(name: "childApps", appName: "Master - Washer-Dryer", namespace: "master", title: "New Washer-Dryer Sensor", multiple: true)
                }
            }
            section(){
                if(!showSchedules){
                    input "showSchedules", "bool", title: "Schedule app hidden. Show?", submitOnChange:true
                } else {
                    input "showSchedules", "bool", title: "Hide schedule app?", submitOnChange:true
                }
                if(!showPresences){
                    input "showPresences", "bool", title: "Presence app hidden. Show?", submitOnChange:true
                } else {
                    input "showPresences", "bool", title: "Hide presence app?", submitOnChange:true
                }
                if(!showPicos){
                    input "showPicos", "bool", title: "Pico app hidden. Show?", submitOnChange:true
                } else {
                    input "showPicos", "bool", title: "Hide Pico app?", submitOnChange:true
                }
                if(!showMagicCubes){
                    input "showMagicCubes", "bool", title: "MagicCube app hidden. Show?", submitOnChange:true
                } else {
                    input "showMagicCubes", "bool", title: "Hide MagicCube app?", submitOnChange:true
                }
                if(!showContacts){
                    input "showContacts", "bool", title: "Contact app hidden. Show?", submitOnChange:true
                } else {
                    input "showContacts", "bool", title: "Hide contact app?", submitOnChange:true
                }
                if(!showHumidity){
                    input "showHumidity", "bool", title: "Humidity app hidden. Show?", submitOnChange:true
                } else {
                    input "showHumidity", "bool", title: "Hide humidity app?", submitOnChange:true
                }
                if(!showWasher){
                    input "showWasher", "bool", title: "Washer-dryer app hidden. Show?", submitOnChange:true
                } else {
                    input "showWasher", "bool", title: "Hide washer-dryer app?", submitOnChange:true
                }
            }
        }
    }
}

def installed() {
    logTrace(173,"Installed","trace")
    state.masterInstalled = true
    initialize()
}

def updated() {
    logTrace(179,"Updated","trace")
    initialize()
}

def initialize() {
    logTrace(184,"Initialized","trace")
}

// Returns app name with app title prepended
def appendAppTitle(appName,appTitle){
    //Compare length of name (eg "test") to appTitle length minus 6 (eg "Master - Time - " minus "Master - "; "Time - " is min length)
    if(appName.length() < appTitle.length() - 6){
        return appTitle.substring(9,appTitle.length()) + " - " + appName
    }

    //Compare first part of name (eg "Testing") to middle part of appTitle (eg "Master - Time" minus "Master - " plus " - ")
    if(appName.substring(0,appTitle.length() - 9) != appTitle.substring(9,appTitle.length())){
        return appTitle.substring(9,appTitle.length()) + " - " + appName
    }
    return appName
}

// Turns a single switch on or off
// action = "on" or "off"
// device is single device object
def setSingleState(action, device,childLabel = "Master"){
    if(action == "on"){
        device.on()
    } else if(action == "off"){
        device.off()
    } else {
        logTrace(210,"Invalid value for action \"$action\" sent to singleOn function","error",childLabel)
        return
    }
    logTrace(213,"Turned $action $device","info",childLabel)
}

// Lock or unlock a group of locks
def multiLock(action,device, childLabel = "Master"){
    device.each{
        singleLock(action,it,childLabel)
    }
}

// Lock or unlock a single lock
def singleLock(action,device, childLabel = "Master"){
    if(action == "lock"){
        device.lock()
    } else if(action == "unlock"){
        device.unlock()
    } else {
        logTrace(230,"Invalid value for action \"$action\" sent to singleLock function","error",childLabel)
    }
    logTrace(232,action + "ed $device","info",childLabel)
}

/*
Restructure the app to:
Use looped pause of, say, max 20 50ms increments, until on
get rid of runRetrySchedule
Add function setMultiLevels(multiDevice, childLabel)
setMultiLevels
[if on] getDefaults (each)
[if on] setSingleLevel (each)
*/

def setMultiLevels(multiDevice, childLabel = "Master"){
    // Loop through devices
    multiDevice.each{
        // If individual device is on, then...
        if(isOn(it)){
            defaults = getSingleDefaultLevel(it,childLabel)
            if(defaults) setSingleLevel(defaults.level,defaults.temp,defaults.hue,defaults.sat,it,childLabel)
        }
    }
}

// Sets level, temp, hue, and/or sat
// Validates level, temp, hue and sat
// Returns true if it does anything
def setSingleLevel(level,temp,hue,sat,device,childLabel = "Master"){
    if(level == "") level = null
    if(temp == "") temp = null
    if(hue == "") hue = null
    if(sat == "") sat = null

    // If no device, throw error and exit
    if(!device){
        logTrace(267,"Null device sent to setSingleLevel","error",childLabel)
        return
    }

    // If invalid error, throw error
    if(level && !validateLevel(level,childLabel)){
        logTrace(273,"Invalid value for level \"$level\" sent to setSingleLevel function","error",childLabel)
        level = null
    }

    // If invalid temp, throw error
    if(temp && !validateTemp(temp,childLabel)){
        logTrace(279,"Invalid value for temp \"$temp\" sent to setSingleLevel funuction","error",childLabel)
        temp = null
    }

    // If invalid hue or sat, throw error
    if((hue || sat) && !validateHueSat(hue,sat,childLabel)){
        logTrace(285,"Invalid value for hue \"$hue\" or sat \"$sat\" sent to setSingleLevel function","error",childLabel)
        hue = null
        sat = null
    }

    // If no changes, exit
    if(!level && !temp && !hue && !sat){
        logTrace(292,"No valid changes sent to setSingleLevel","error",childLabel)
        return
    }

    // If not on, exit
    // This should be checked prior to setting level
    // Maybe want to allow it, and have the device do whatever it does when setting level while off...?
    if(!isOn(device,childLabel)) return

    message = "Set "
    if(level){
        if(device.currentLevel != level && isDimmable(device,childLabel)){
            if(isFan(device,childLabel)) level = roundFanLevel(level,childLabel)

            device.setLevel(level)
            message = message + "level: $level; "
        }	
    }
    isColor = isColor(device,childLabel)

    if(temp && isColor){
        if((device.currentColorTemperature && ((temp - device.currentColorTemperature) > 3 || (temp - device.currentColorTemperature) < -3)) || !device.currentColorTemperature){
            device.setColorTemperature(temp as int)
            message = message + "temp: $temp; "
        }
    }
    // Need to compare to current temp, hue and/or sat
    if(hue || sat){
        if(isColor){
            device.setColor([hue: hue, saturation: sat])
            message = message + "hue: $hue; sat: $sat; "
        }
    }
    if(message != "Set ") logTrace(325,"$message device $device","debug",childLabel)
    return true
}

def getSingleDefaultLevel(singleDevice, childLabel = "Master"){
    // Loop through all the apps...
    childApps.each { Child ->
        // Checking schedules...
        if(Child.label.substring(0,7) == "Time - ") {
            // As long as not all the levels are already set...
            if(!defaultLevel || !defaultTemp || !defaultHue || !defaultSat) {
                // Get their default levels
                defaults = Child.getDefaultLevel(singleDevice)

                if(defaults.level != "") defaultLevel = defaults.level
                if(defaults.temp != "") defaultTemp = defaults.temp
                if(defaults.hue != "") defaultHue = defaults.hue
                if(defaults.sat != "") defaultSat = defaults.sat
                if(defaults.level != "" || defaults.temp != "" || defaults.hue != "" || defaults.sat != "")
                logTrace(344,"Default levels of $defaults found for $it with $Child.label","trace",childLabel)
            }
        }

    }
    // And set the level
    if(defaultLevel || defaultTemp || defaultHue || defaultSat) {
        defaults = [level: defaultLevel,temp: defaultTemp, hue: defaultHue,sat: defaultSat]
        return defaults
    } else {
        return false
    }
}

def dim(action,device,childLabel="Master"){
    if(action != "dim" && action != "brighten"){
        logTrace(360,"Invalid value for action \"$action\" sent to dim function","error",childLabel)
        return
    }

    device.each{
        if(isDimmable(it,childLabel)){
            // dimming a fan that's on (decrease by 25)
            if(action == "dim" && isFan(it,childLabel) && isOn(it,childLabel)){
                levelValue = it.currentLevel - 25
                // brightening a fan that's on (increase by 25)
            } else if(action == "bright" && isFan(it,childLabel) && isOn(it,childLabel)){
                levelValue = it.currentLevel + 25
                // dimming or brightening non-fan that's on (use nextLevel)
            } else if(!isFan(it,childLabel) && isOn(it,childLabel)){
                levelValue = nextLevel(it.currentLevel, action, childLabel)
                // dimming or brightening (anything) when off (turn on and set to level 1)
            } else if(!isOn(it,childLabel)){
                singleOn("on",it,childLabel)
                levelValue = 1
            }
            if(levelValue) {
                if(isFan(it,childLabel)) roundFanLevel(levelValue,childLabel)
                singleLevels(levelValue,null,null,null,it,childLabel)
                logTrace(383,"Set level of $it to $levelValue","info",childLabel)
            }
        }
    }
}


// Determine next dim or brighten level based on "multiplier
// action = "dim" or "brighten"
def nextLevel(level, action, childLabel="Master"){
    childId = getAppId(childLabel)

    if(childLabel != "Master"){
        childApps.each {Child->
            if(childId && Child.getId() == childId) dimSpeed = Child.getDimSpeed()
        }
        if(!dimSpeed){
            dimSpeed = 1.2
            logTrace(401,"Failed to find dimSpeed in function nextLevel","error",childLabel)
        }
    }
    if (action != "dim" && action != "brighten"){
        logTrace(405,"Invalid value for action \"$action\" sent to nextLevel function","error",childLabel)
        return false
    }
    def newLevel = level as int
        if(level < 1) level = 1
        if(level > 100) level = 100
        if((action == "dim" && level > 1) || (action ==  "brighten" && level < 100)){
            if (action == "dim"){
                newLevel = Math.round(level / dimSpeed)
                // make sure it changed by at least 1
                if (newLevel == level) newLevel = newLevel - 1
            } else if (action == "brighten"){
                newLevel = Math.round(level * dimSpeed)
                // make sure it changed by at least 1
                if (newLevel == level) newLevel = newLevel + 1
            }
        }
    if(newLevel > 100) newLevel = 100
    if(newLevel < 1) newLevel = 1

    return newLevel
}

// Changes mode to value of $mode
/* ************************************************** */
/* TO DO: Trigger reschedule so that any schedules    */
/* suspended due to mode will restart. Need to get    */
/* the device id from the requesting child app...     */
/* somehow.                                           */
/* ************************************************** */
def changeMode(mode, childLabel = "Master", device = "") {
    if(location.mode == mode) return
    oldMode = location.mode
    setLocationMode(mode)
    logTrace(439,"Changed Mode from $oldMode to $mode","info",childLabel)
}

// Send SMS text message to $phone with $message
//SMS IS NO LONGER SUPPORTED
def sendText(phone, message,childLabel){
    //Normalize phone number
    phone = phone.replaceAll(" ","");
    phone = phone.replaceAll("\\(","");
    phone = phone.replaceAll("\\)","");
    phone = phone.replaceAll("-","");
    phone = phone.replaceAll("\\.","");
    phone = phone.replaceAll("\\+","");
    if(!phone.isNumber()) {
        logTrace(453,"Phone number $phone is not valid (message \"$message\" not sent)","error",childLabel)
        return false
    }
    if(phone.length() == 10) {
        phone = "+1" + phone
    } else if(phone.length() == 9 && phone.substring(0,1) == "1") {
        phone = "+" + phone
    }
    logTrace(461,"Sent \"$message\" to $phone","info",childLabel)
    sendSms(phone,message)
    return true
}

// Test state of a group of switches
// Return true if any are on
def isMultiOn(multiDevice,childLabel="Master"){
    multiState = false
    multiDevice.each{
        if(isOn(it,childLabel)) value = true
    }
    logTrace(472,"Function isMultiOn returning $value","debug",childLabel)
    return value
}

// Test state of a single switch
def isOn(singleDevice,childLabel="Master"){
    if(singleDevice.currentValue("switch") == "on") return true
}

// Returns true if level value is either valid or null
def validateLevel(value, childLabel="Master"){
    if(value){
        value = value as int 
            if(value < 1 || value > 100) return false
            }
    return true
}

// Returns true if temp value is either valid or null
def validateTemp(value, childLabel="Master"){
    if(value){
        value = value as int   
            if(value < 2200 || value > 6500) return false
            }
    return true
}

// Returns true if neither hue nor sat are invalid
// Returns true if both are null values
def validateHueSat(hue,sat, childLabel="Master"){
    if(hue && (hue < 1 || hue > 100)) return
    if(sat && (sat < 1 || sat > 100)) return
    return true
}

def validateMultiplier(value, childLabel="Master"){
    if(value){
        if(value < 1 || value > 100){
            logTrace(511,"Multiplier $value is not valid","error",childLabel)
            return
        }
    }
    return value
}

// Validation functions for device capabilities

def isDimmable(device, childLabel="Master"){
    def deviceCapability
    device.capabilities.each {
        deviceCapability += it.name
    }
    value = deviceCapability.contains("SwitchLevel")
    return value
}

def isTemp(device, childLabel="Master"){
    def deviceCapability
    device.capabilities.each {
        deviceCapability += it.name
    }
    value = deviceCapability.contains("ColorTemperature")
    return value
}

def isColor(device, childLabel="Master"){
    def deviceCapability
    device.capabilities.each {
        deviceCapability += it.name
    }
    value = deviceCapability.contains("ColorMode")
    return value
}

def isFan(device, childLabel="Master"){
    def deviceCapability
    device.capabilities.each {
        deviceCapability += it.name
    }
    if(deviceCapability.contains("Actuator") == true && device.name.contains("Fan") == true){
        return true
    } else {
        return false
    }
}

// Round fan level to high, medium or low
// Returns rounded value
def roundFanLevel(level, childLabel="Master"){
    if(level > 66){
        value = 75
    } else if (level < 66  && level > 33){
        value = 50
    } else if (level > 0 && level < 33){
        value = 25
    }
    logTrace(569,"Setting level for fan to $value from $level","info",childLabel)
    return value
}

// Returns app id from app label
def getAppId(childLabel){
    childApps.each { 
        if(it.label == childLabel) child = it.id
    }
    //Return the label; if no label, return the id
    if(child) return child
}

def rescheduleAll(device,childLabel="Master"){
    childApps.each {Child->
        /* ************************************************** */
        /* TO-DO: We need to test this! If we use a Pico, it  */
        /* should reschedule all of them.                     */
        /* If a schedule changes something *that* schedule    */
        /* should NOT be rescheduled (Time app handles it).   */
        /* ************************************************** */
        if(Child.label.substring(0,7) == "Time - " && Child.label != childLabel) {
            Child.timeDevice.each {
                if(device.id == it.id) match = true
            }
            if(match == true) {
                Child.setDailySchedules()
                Child.runIncrementalSchedule()
                logTrace(596,"Rescheduling $device with $Child.label","trace",childLabel)
                match = null
            }
        }
    }
}

def rescheduleDaily(device,childLabel="Master"){
    childApps.each {Child->
        /* ************************************************** */
        /* TO-DO: We need to test this! If we use a Pico, it  */
        /* should reschedule all of them.                     */
        /* If a schedule changes something *that* schedule    */
        /* should NOT be rescheduled (Time app handles it).   */
        /* ************************************************** */
        if(Child.label.substring(0,7) == "Time - " && Child.label != childLabel) {
            Child.timeDevice.each {
                if(device.id == it.id) match = true
            }
            if(match == true) {
                Child.setDailySchedules()
                logTrace(617,"Rescheduling $device with $Child.label","trace",childLabel)
                match = null
            }
        }
    }
}


// Returns date one day ahead of $date
// Expects and returns format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
def getTomorrow(date,childLabel="Master"){
    day = date.substring(8,10).toInteger() + 1
    day = String.format("%02d",day)
    value = date.substring(0,8) + day.toString() + date.substring(10,28)
    return value
}

// Returns date/time of sunrise in format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
// negative is true of false
def getSunrise(offset, childLabel="Master"){
    if(offset){
        value = getSunriseAndSunset(sunriseOffset: offset, sunsetOffset: 0).sunrise
        value = value.format("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ")
    } else {
        value = getSunriseAndSunset().sunrise.format("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ")
    }
    return value
}

// Returns date/time of sunset in format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
def getSunset(offset = false,childLabel="Master"){
    if(offset){
        value = getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: offset).sunset
        value = value.format("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ")
    } else {
        value = getSunriseAndSunset().sunset.format("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ")
    }
    return value
}

// Returns true if today is in $days map
def todayInDayList(days,childLabel="Master"){
    if(!days) return
    def df = new java.text.SimpleDateFormat("EEEE")
    df.setTimeZone(location.timeZone)
    def day = df.format(new Date())
    if(days.contains(day)) return true
}

// Returns true if now is between two dates
def timeBetween(timeStart, timeStop,childLabel="Master"){
    if(!timeStart) {
        logTrace(669,"Function timeBetween returning false (no start time)","error",childLabel)
        return
    }
    if(!timeStop) {
        logTrace(673,"Function timeBetween returning false (no stop time)","trace",childLabel)
        return
    }

    //This might work
    //if(timeStart.before(now()) && timeStop.after(now()))

    varNow = now()
    if(timeToday(timeStart, location.timeZone).time > timeToday(timeStop, location.timeZone).time) {
        if(varNow > timeToday(timeStart, location.timeZone).time || varNow < timeToday(timeStop, location.timeZone).time){
            logTrace(683,"Time is between " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mma MMM dd, yyyy", location.timeZone) + " and " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mma MMM dd, yyyy", location.timeZone),"debug",childLabel)
            return true
        }
    }
    if(varNow > timeToday(timeStart, location.timeZone).time && varNow < timeToday(timeStop, location.timeZone).time) {
        logTrace(688,"Time is between " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mma MMM dd, yyyy", location.timeZone) + " and " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mma MMM dd, yyyy", location.timeZone),"debug",childLabel)
        return true
    }
}

def speak(text,childLabel="Master"){
    if(!notificationDevice) {
        logTrace(695,"No speech device for \"$text\"","error",childLabel)
        return
    }
    notificationDevice.speak(text)
    logTrace(699,"Sending speech \"$text\"","info",childLabel)
}

/* ************************************************** */
/* TO-DO: Clean it up, and schedule it every day,     */
/* with an alert.                                     */
/* ************************************************** */
def test(){
    // Get current time
    varNow = now()
    sensors.each{
        // lastCheckin only availble for Xiaomi drivers
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

// Returns true if secondDevice contains anything in firstDevice
def compareDeviceLists(firstDevice,secondDevice){
    list = []
    firstDevice.each{
        list.add(it.id)
    }
    secondDevice.each{
        if(list.contains(it.id)) return true
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def logTrace(lineNumber,message = null, type = "trace",childLabel = "Master"){
    message = (message ? " -- $message" : "")
    if(childLabel != "Master") message = "[$childLabel]$message"
    if(lineNumber) message = "(line $lineNumber) $message"
    message = "Master $message"
    switch(type) {
        case "error":
        log.error message
        break
        case "warn":
        log.warn message
        break
        case "info":
        log.info message
        break
        case "debug":
        log.debug message
        break
        case "trace":
        log.trace message
    }
}
