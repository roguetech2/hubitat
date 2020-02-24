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
*  Version: 0.2.20
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
    importUrl: "https://raw.githubusercontent.com/roguetech2/hubitat/master/Master.groovy",
    iconUrl: "http://cdn.device-icons.smartthings.com/home2-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/home2-icn@2x.png"
)

preferences {
    page(name: "mainPage")
}

// logLevel sets number of log messages
// 0 for none
// 1 for errors only
// 5 for all
def getLogLevel(){
    return 5
}

def mainPage() {
    infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
    errorIcon = "<img src=\"http://emily-john.love/icons/error.png\" width=20 height=20>"
    return dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
        if(!state.masterInstalled) {
            section("Click Done.") {
            }

        } else {
            if(showPresences){
                section(""){
                    /*
// Used for flash-alerts; nothing else
input "colorLights", "capability.switchLevel", title: "Select all color lights:", multiple: true, required: false, submitOnChange:true
paragraph "<div style=\"background-color:AliceBlue\">$infoIcon These color lights will be used for flashing alerts.</div>"
*/
                    input "people", "capability.presenceSensor", title: "Select all people:", multiple: true, required: false, submitOnChange:true
                    paragraph "<div style=\"background-color:AliceBlue\">$infoIcon These people will be used for \"anyone\" and \"everyone\" conditions in any presence-based condition, and allow birthday options.</div>"
                    input "notificationDevice", "capability.speechSynthesis", title: "Select notification device(s):", multiple: false, required: false, submitOnChange:true
                    paragraph "<div style=\"background-color:AliceBlue\">$infoIcon This will be used for voice alerts.</div>"
                    //input "sensors", "capability.battery", title: "Select all sensors:", multiple: true, required: false, submitOnChange:true
                    //paragraph "<div style=\"background-color:AliceBlue\">$infoIcon These will be periodically tested to make sure they are reporting properly. Enter phone number for text notice.</div>"
                    input "phone", "phone", title: "Number to text alert? (Optional)", required: false, submitOnChange:true
                    paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Text alerts do not currently work. Will be fixed soon.</div>"

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
            }
            if(showSchedules){
                section("Scheduled settings:") {
                    app(name: "childApps", appName: "Master - Time", namespace: "master", title: "New Schedule", multiple: true)
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
    state.logLevel = getLogLevel()
    if(checkLog(a="trace")) putLog(189,"Installed",a)
    state.masterInstalled = true
    initialize()
}

def updated() {
    state.logLevel = getLogLevel()
    if(checkLog(a="trace")) putLog(196,"Updated",a)
    initialize()
}

def initialize() {
    state.logLevel = getLogLevel()
    if(checkLog(a="trace")) putLog(202,"Initialized",a)
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

// Turns multiple switches on or off, then waits for all of them to be on
// action = "on" or "off"
def setStateMulti(action, multiDevice,childLabel = "Master"){
    if(action != "on" && action != "off"){
        if(checkLog(a="error")) putLog(223,"Invalid value for action \"$action\" sent to setStateMulti function",a,childLabel)
        return
    }
    multiDevice.each{
        if(action == "on"){
            it.on()
        } else if(action == "off"){
            it.off()
        }
    }
    if(checkLog(a="info")) putLog(233,"Turned $action $multiDevice",a,childLabel)
}

// Turns a single switch on or off
// action = "on" or "off"
def setStateSingle(action, singleDevice,childLabel = "Master"){
    if(action == "on"){
        if(!isFan(singleDevice) && isDimmable(singleDevice)){
            singleDevice.setLevel(1)
        } else {
            singleDevice.on()
        }
    } else if(action == "off"){
        singleDevice.off()
    } else {
        if(checkLog(a="error")) putLog(248,"Invalid value for action \"$action\" sent to setStateSingle function",a,childLabel)
        return
    }
    if(checkLog(a="info")) putLog(251,"Turned $action $singleDevice",a,childLabel)
}

// Lock or unlock a group of locks
def multiLock(action,multiDevice, childLabel = "Master"){
    multiDevice.each{
        singleLock(action,it,childLabel)
    }
}

// Lock or unlock a single lock
def singleLock(action,singleDevice, childLabel = "Master"){
    if(action == "lock"){
        singleDevice.lock()
    } else if(action == "unlock"){
        singleDevice.unlock()
    } else {
        if(checkLog(a="error")) putLog(268,"Invalid value for action \"$action\" sent to singleLock function",a,childLabel)
    }
    if(checkLog(a="info")) putLog(270,action + "ed $singleDevice",a,childLabel)
}

// Sets level, temp, hue, and/or sat
// Validates level, temp, hue and sat
// Returns true if it does anything
def setLevelSingle(defaults,singleDevice,childLabel = "Master"){
    // If no device, throw error and exit
    if(!singleDevice){
        if(checkLog(a="error")) putLog(279,"Null singleDevice sent to setLevelSingle",a,childLabel)
        return
    }

    // If no changes, exit
    if(!defaults){
        // Shouldn't happen, since there are system level defaults
        if(checkLog(a="error")) putLog(286,"No valid changes sent to setLevelSingle",a,childLabel)
        return
    }

    // If invalid error, throw error
    if(defaults.level && !validateLevel(defaults.level,childLabel)){
        if(checkLog(a="error")) putLog(292,"Invalid value for level \"$defaults.level\" sent to setLevelSingle function",a,childLabel)
        defaults.level = null
    }

    // If invalid temp, throw error
    if(defaults.temp && !validateTemp(defaults.temp,childLabel)){
        if(checkLog(a="error")) putLog(298,"Invalid value for temp \"$defaults.temp\" sent to setLevelSingle funuction",a,childLabel)
        defaults.temp = null
    }

    // If invalid hue or sat, throw error
    if((defaults.hue || defaults.sat) && !validateHueSat(defaults.hue,defaults.sat,childLabel)){
        if(checkLog(a="error")) putLog(304,"Invalid value for hue \"$defaults.hue\" or sat \"$defaults.sat\" sent to setLevelSingle function",a,childLabel)
        defaults.hue = null
        defaults.sat = null
    }

    // If not on, wait for it to turn on
    // If refuses to turn on, exit
    // Must override cached value
    if(singleDevice.currentValue("switch",true) != "on") {
        if(!waitStateChange("on",singleDevice,childLabel)) return
    }

    /* ************************************************************************ */
    /* TO-DO: Test whether overriding cached value of currentValue refreshes    */
    /* the cache. If so, we can check isOn() above, and override cache in       */
    /* waitStateChange to keep things more consistent                           */
    /* ************************************************************************ */
    /* TO-DO: Test whether we have any race condition or dropped commands below.*/
    /* Do we need to pause between setting level, temp, or color? (The device   */
    /* must be on, per previous statement.)                                     */
    /* ************************************************************************ */
    message = "Set "
    if(defaults.level){
        if(singleDevice.currentLevel != defaults.level && isDimmable(singleDevice,childLabel)){
            if(isFan(singleDevice,childLabel)) defaults.level = roundFanLevel(defaults.level,childLabel)

            singleDevice.setLevel(defaults.level as int)
            message += "level: $defaults.level; "
        }	
    }

    // Color hue takes precendence over temp, but...
    // if sat with no hue, then obviously temp should take precedence
    if(defaults.hue && defaults.temp){
        defaults.temp = null
    } else if(defaults.sat && defaults.temp){
        defaults.sat = null
    }
    if(defaults.temp && defaults.temp != singleDevice.currentColorTemperature && isTemp(singleDevice,childLabel)){
        singleDevice.setColorTemperature(defaults.temp as int)
        message += "temp: $defaults.temp; "
    }
    // Need to compare to current temp, hue and/or sat
    if((defaults.hue || defaults.sat) && isColor(singleDevice,childLabel)){
        // Only update what's needed
        // treat it like a null if current hue/sat being equal to change value 
        if(defaults.sat && (!defaults.hue || defaults.hue == singleDevice.currentHue) && defaults.sat != singleDevice.currentSaturation) {
            // Defaults to existing sat - should we default to 100%?
            singleDevice.setColor([saturation: defaults.sat])
            message += "sat: " + defaults.sat + "; "
        } else if((!defaults.sat || defaults.sat == singleDevice.currentSaturation) && defaults.hue != singleDevice.currentHue){
            singleDevice.setColor([hue: defaults.hue])
            message += "hue: $defaults.hue; "
        } else if(defaults.hue && defaults.sat && defaults.sat != singleDevice.currentSaturation && defaults.hue != singleDevice.currentHue){
            singleDevice.setColor([hue: defaults.hue, saturation: defaults.sat])
            message += "hue: $defaults.hue; sat: $defaults.sat; "
        }
    }
    if(message != "Set " && checkLog(a="trace")) putLog(362,"$message of $singleDevice",a,childLabel)
    return true
}

// Gets levels as set for the child app
def getOverrideLevels(defaults,appAction = null,childLabel = "Master"){
    if(!defaults && (settings[appAction + "Level"] || settings[appAction + "Temp"] || settings[appAction + "Hue"] || settings[appAction + "Sat"])) defaults = [:]
    if(settings[appAction + "Level"]) defaults.put("level",settings[appAction + "Level"])
    if(settings[appAction + "Temp"]) defaults.put("temp",settings[appAction + "Temp"])
    if(settings[appAction + "Hue"]) defaults.put("hue",settings[appAction + "Hue"])
    if(settings[appAction + "Sat"]) defaults.put("sat",settings[appAction + "Sat"])
    return defaults       
}

// Handles turning on a single device and setting levels
// Only called by (child app) multiOn
// appAction is for "open/close", "push/hold", etc., so the child app knows which
// levels to apply for which device/action
def getAndSetSingleLevels(singleDevice,appAction = null,childLabel = "Master"){
    // If defaults, then there's an active schedule
    // So use it for if overriding/reenabling
    // In scheduler app, this gets defaults for any *other* schedule
    defaults = getScheduleDefaultSingle(singleDevice,childLabel)
    logMessage = defaults ? "$singleDevice scheduled for $defaults" : "$singleDevice has no scheduled default levels"

    // If there are defaults, then there's an active schedule so reschedule it (the results are corrupted below).
    // We could do this for the matching schedules within its own getDefaultLevel(), but that would
    // probably result in incremental schedules rescheduling themselves over and over again. And if we
    // excluded schedules from rescheduling, then daily schedules wouldn't do this.
    if(defaults) rescheduleIncrementalSingle(singleDevice,childLabel)

    // This does nothing in Time, or other app that has no levels, getOverrideLevels will immediately exit
    defaults = getOverrideLevels(defaults,appAction,childLabel)
    logMessage += defaults ? ", controller overrides of $defaults": ", no controller overrides"

    // Set default levels, for level and temp, if no scheduled defaults (don't need to do for "resume")
    defaults = getDefaultSingle(defaults,childLabel)
    logMessage += ", so with generic defaults $defaults"

    if(checkLog(a="debug")) putLog(401,logMessage,a,childLabel)
    setLevelSingle(defaults,singleDevice,childLabel)
    return
}

// Waits in 10ms increments for devices to change states to on, for a total of 2 seconds
// This should only be called after all devices have been set on
def waitStateChange(action,singleDevice,childLabel = "Master"){
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
        if(checkLog(a="debug")) putLog(421,"Waited " + (i * 10) + " milliseconds for $singleDevice to turn on",a,childLabel)
        return true
    } else {
        if(checkLog(a="error")) putLog(424,"$singleDevice refused to turn $action in " + (i * 10) + " milliseconds",a,childLabel)
        return
    }
}

// This gets all scheduled levels for the device; if none, should return null
// defaults map is generated in getDefaultLevel
// Goes through schedules, and calls child getDefaultLevel, which attempts to match on singleDevice
def getScheduleDefaultSingle(singleDevice, childLabel = "Master"){
    // Loop through all the apps...
    childApps.each { Child ->
        // Checking schedules...
        if(Child.label.substring(0,7) == "Time - ") {
            // As long as not all the levels are already set...
            if(!defaultLevel || !defaultTemp || !defaultHue || !defaultSat) {
                // Get their default levels
                defaults = Child.getDefaultLevel(singleDevice,childLabel)
                if(defaults){
                    if(defaults.level) defaultLevel = defaults.level
                    if(defaults.temp) defaultTemp = defaults.temp
                    if(defaults.hue) defaultHue = defaults.hue
                    if(defaults.sat) defaultSat = defaults.sat
                    if(defaults.level || defaults.temp || defaults.hue || defaults.sat)
                    if(checkLog(a="debug")) putLog(447,"Default levels of $defaults found for $singleDevice with $Child.label",a,childLabel)
                }
            }
        }
    }
    // And set the level
    if(defaultLevel || defaultTemp || defaultHue || defaultSat) {
        // Reset defaults to repopulate, just in case something weird happens
        defaults = [:]
        if(defaultLevel) defaults.put("level",defaultLevel)
        if(defaultTemp) defaults.put("temp",defaultTemp)
        if(defaultHue) defaults.put("hue",defaultHue)
        if(defaultSat) defaults.put("sat",defaultSat)
        return defaults
    } else {
        return
    }
}

// Sets default level if none is set
// Default level of 100, and default temp of 3500 used
def getDefaultSingle(defaults, childLabel = "Master"){
    if(!defaults) defaults = [:]
    if(!defaults.level || defaults.level == "") defaults.put("level",100)
    if(!defaults.temp || defaults.temp == "") defaults.put("temp",3500)
    return defaults
}

// Gets value of true from each Pico, MagicCube, etc., which has turned on
// singleDevice within the last second or so
def getStateRequest(singleDevice,childLabel){
    // Loop through all the apps...
    childApps.each { Child ->
        // Can't break from .each loop; also tried .any and .find
        if(!value && Child.getStateDeviceChange(singleDevice.id)) value = true
    }
    return value
}

def dim(action,device,childLabel="Master"){
    if(action != "dim" && action != "brighten"){
        if(checkLog(a="error")) putLog(488,"Invalid value for action \"$action\" sent to dim function",a,childLabel)
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
                setLevelSingle(levelValue,"","","",it,childLabel)
                if(checkLog(a="info")) putLog(511,"Set level of $it to $levelValue",a,childLabel)
            }
        }
    }
}


// Determine next dim or brighten level based on "multiplier
// action = "dim" or "brighten"
def nextLevel(level, action, childLabel="Master"){
    if(childLabel != "Master"){
        childApps.each {Child->
            if(childLabel && Child.getLabel() == childLabel) dimSpeed = Child.getDimSpeed()
        }
        if(!dimSpeed){
            dimSpeed = 1.2
            if(checkLog(a="error")) putLog(527,"Failed to find dimSpeed in function nextLevel",a,childLabel)
        }
    }
    if (action != "dim" && action != "brighten"){
        if(checkLog(a="error")) putLog(531,"Invalid value for action \"$action\" sent to nextLevel function",a,childLabel)
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
/* ************************************************************************ */
/* TO DO: Trigger reschedule of incremental so that any schedules suspended */
/* due to mode will restart. Need to enforce getting the device id from the */
/* requesting child app. Somehow.                                           */
/* ************************************************************************ */
def changeMode(mode, childLabel = "Master", device = "") {
    if(location.mode == mode) return
    oldMode = location.mode
    setLocationMode(mode)
    if(checkLog(a="info")) putLog(564,"Changed Mode from $oldMode to $mode",a,childLabel)
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
        if(checkLog(a="error")) putLog(578,"Phone number $phone is not valid (message \"$message\" not sent)",a,childLabel)
        return false
    }
    if(phone.length() == 10) {
        phone = "+1" + phone
    } else if(phone.length() == 9 && phone.substring(0,1) == "1") {
        phone = "+" + phone
    }
    if(checkLog(a="info")) putLog(586,"Sent \"$message\" to $phone",a,childLabel)
    sendSms(phone,message)
    return true
}

// Test state of a group of switches
// Return true if any are on
def isOnMulti(multiDevice,childLabel="Master"){
    multiState = false
    multiDevice.each{
        if(!value && isOn(it,childLabel)) value = true
    }
    if(checkLog(a="debug")) putLog(598,"Function isOnMulti returning $value",a,childLabel)
    return value
}

// Test state of a single switch
def isOn(singleDevice,childLabel="Master"){
    if(singleDevice.currentValue("switch") == "on") return true 
    return false
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
            if(checkLog(a="error")) putLog(637,"Multiplier $value is not valid",a,childLabel)
            return
        }
    }
    return value
}

// Validation functions for device capabilities

/*
def isDimmable(device, childLabel="Master"){
    def deviceCapability
    device.capabilities.each {
        deviceCapability += it.name
    }
    value = deviceCapability.contains("SwitchLevel")
    return value
}
*/
def isDimmable(device, childLabel="Master"){
    if(!device) return false
    return device.hasCapability("SwitchLevel")
}

/*
def isTemp(device, childLabel="Master"){
    def deviceCapability
    device.capabilities.each {
        deviceCapability += it.name
    }
    value = deviceCapability.contains("ColorTemperature")
    return value
}
*/
def isTemp(device, childLabel="Master"){
    if(!device) return false
    return device.hasCapability("ColorTemperature")
}

/*
def isColor(device, childLabel="Master"){
    def deviceCapability
    device.capabilities.each {
        deviceCapability += it.name
    }
    value = deviceCapability.contains("ColorMode")
    return value
}
*/
def isColor(device, childLabel="Master"){
    if(!device) return false
    return device.hasCapability("ColorMode")
}

/*
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
*/
def isFan(device, childLabel="Master"){
    if(!device) return false
    return device.hasCapability("FanControl")
}

// Round fan level to high, medium or low
// Returns rounded value
/* ************************************************************************ */
/* TO-DO: Switch to using cycleUp and cycleDown                             */
/* ************************************************************************ */
def roundFanLevel(level, childLabel="Master"){
    if(level > 66){
        value = 75
    } else if (level < 66  && level > 33){
        value = 50
    } else if (level > 0 && level < 33){
        value = 25
    }
    if(checkLog(a="info")) putLog(722,"Setting level for fan to $value from $level",a,childLabel)
    return value
}

def rescheduleIncrementalMulti(multiDevice,childLabel="Master"){
    multiDevice.each{singleDevice->
        rescheduleIncrementalSingle(singleDevice,childLabel)
    }
}

def rescheduleIncrementalSingle(singleDevice,childLabel="Master"){
    childApps.each {Child->
        if(Child.label.substring(0,7) == "Time - " && Child.label != childLabel) {
            Child.timeDevice.each {ChildDevice->
                if(singleDevice.id == ChildDevice.id){
                    // Check if it's between schedule start and stop time
                    // This would be done by setIncrementalSchedule. The only major advantage of
                    // doing it here, with getTimeVariable function, is to prevent extra log messages
                    time = Child.getTimeVariable()
                    if(timeBetween(time[0], time[1],childLabel)) match = true
                }
            }
            if(match == true) {
                if(checkLog(a="trace")) putLog(745,"Rescheduling incremental $singleDevice with $Child.label",a,childLabel)
                Child.setIncrementalSchedule()
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
        if(checkLog(a="error")) putLog(798,"Function timeBetween returning false (no start time)",a,childLabel)
        return
    }
    if(!timeStop) {
        if(checkLog(a="trace")) putLog(802,"Function timeBetween returning false (no stop time)",a,childLabel)
        return
    }

    //This might work
    //if(timeStart.before(now()) && timeStop.after(now()))

    varNow = now()
    if(timeToday(timeStart, location.timeZone).time > timeToday(timeStop, location.timeZone).time) {
        if(varNow > timeToday(timeStart, location.timeZone).time || varNow < timeToday(timeStop, location.timeZone).time){
            if(checkLog(a="debug")) putLog(812,"Time is between " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mma MMM dd, yyyy", location.timeZone) + " and " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mma MMM dd, yyyy", location.timeZone),a,childLabel)
            return true
        }
    }
    if(varNow > timeToday(timeStart, location.timeZone).time && varNow < timeToday(timeStop, location.timeZone).time) {
        if(checkLog(a="debug")) putLog(817,"Time is between " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mma MMM dd, yyyy", location.timeZone) + " and " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mma MMM dd, yyyy", location.timeZone),a,childLabel)
        return true
    }
}

def speak(text,childLabel="Master"){
    if(!notificationDevice) {
        if(checkLog(a="warn")) putLog(824,"No speech device for \"$text\"",a,childLabel)
        return
    }
    notificationDevice.speak(text)
    if(checkLog(a="info")) putLog(828,"Sending speech \"$text\"",a,childLabel)
    return true
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

def checkLog(type = null){
    if(!state.logLevel) getLogLevel()
    switch(type) {
        case "error":
        if(state.logLevel > 0) return "error"
        break
        case "warn":
        if(state.logLevel > 1) return "warn"
        break
        case "info":
        if(state.logLevel > 2) return "info"
        break
        case "trace":
        if(state.logLevel > 3) return "trace"
        break
        case "debug":
        if(state.logLevel == 5) return "debug"
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,message = null,type = "trace", childLabel = "Master"){
    logMessage = ""
    if(type == "error") logMessage += "<font color=\"red\">"
    if(type == "warn") logMessage += "<font color=\"brown\">"
    logMessage += "Master "
    if(lineNumber) logMessage += "(line $lineNumber) "
    if(childLabel != "Master") logMessage += "[$childLabel] "
    if(message) logMessage += "-- $message"
    if(type == "error" || type == "warn") logMessage += "</font>"

    switch(type) {
        case "error":
        log.error(logMessage)
        return true
        case "warn":
        log.warn(logMessage)
        return true
        case "info":
        log.info(logMessage)
        return true
        case "trace":
        log.trace(logMessage)
        return true
        case "debug":
        log.debug(logMessage)
        return true
    }
    return
}
