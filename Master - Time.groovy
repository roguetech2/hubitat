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
*  Name: Master - Time
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master%20-%20Time.groovy
*  Version: 0.5.02
*
***********************************************************************************************************************/

definition(
    name: "Master - Time",
    namespace: "master",
    author: "roguetech",
    description: "Schedules, times and default settings",
    parent: "master:Master",
    category: "Convenience",
    importUrl: "https://raw.githubusercontent.com/roguetech2/hubitat/master/Master%20-%20Time.groovy",
    iconUrl: "http://cdn.device-icons.smartthings.com/Office/office6-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Office/office6-icn@2x.png"
)

// logLevel sets number of log messages
// 0 for none
// 1 for errors only
// 5 for all
def getLogLevel(){
    return 3
}

preferences {
    infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
    errorIcon = "<img src=\"http://emily-john.love/icons/error.png\" width=20 height=20>"
    warningIcon = "<img src=\"http://emily-john.love/icons/warning.png\" width=20 height=20>"

    // If we're missing a value, don't allow save
    if((!app.label) ||
       (!settings["timeDevice"]) ||
       !settings["timeOn"] ||
       !settings["inputStartType"] ||
       !settings["inputStopType"] || 
       (settings["inputStartType"] == "time" && !settings["inputStartTime"]) ||
       (settings["inputStopType"] == "time" && !settings["inputStopTime"]) ||
       ((settings["inputStartType"] == "sunrise" || settings["inputStartType"] == "sunset") && !settings["inputStartSunriseType"]) ||
       ((settings["inputStopType"] == "sunrise" || settings["inputStopType"] == "sunset") && !settings["inputStopSunriseType"]) ||
       ((settings["inputStartSunriseType"] == "before" || settings["inputStartSunriseType"] == "after") && !settings["inputStartBefore"]) ||
       ((settings["inputStopSunriseType"] == "before" || settings["inputStopSunriseType"] == "after") && !settings["inputStopBefore"]) ||
       (hueOn && hueOff && !hueDirection) ||
       (settings["levelOn"] > 100 || settings["levelOff"] >100 || (settings["tempOn"] && settings["tempOn"] < 1800) || settings["tempOn"] > 5400 || (tempOff && tempOff < 1800) || tempOff > 5400 || hueOn > 100 || hueOff > 100 || satOn > 100 || satOff > 100)) noInstall = true

    if(noInstall) {
        install = false
    } else {
        install = true
    }

    page(name: "setup", install: install, uninstall: true) {
        // display Name
        if(!app.label){
            section(){
                displayNameOption()
            }
        } else {
            section(){
                displayNameOption()
            displayDevicesOption()
                if(install) displayDisableOption()
            }
            if(timeDevice){
                if(!settings["inputStartType"]){
                    settings["inputStartTime"] = null
                    settings["inputStartSunriseType"] = null
                    settings["inputStartBefore"] = null
                }
                if(settings["inputStartType"] != "time") settings["inputStartTime"] = null
                if(settings["inputStartType"] != "sunrise" && settings["inputStartType"] != "sunset") {
                    settings["inputStartSunriseType"] = null
                    settings["inputStartBefore"] = null
                }  
                
                displayStartTimeSection()

                if(checkTimeComplete("start") && timeOn){
                    if(!settings["inputStopType"] || settings["inputStopType"] == "none"){
                        log.debug("true")
                        settings["inputStopTime"] = null
                        settings["inputStopSunriseType"] = null
                        settings["inputStopBefore"] = null
                        settings["levelOff"] = null
                        settings["tempOff"] = null
                        settings["hueOff"] = null
                        settings["satOff"] = null
                        settings["modeOff"] = null
                    }
                    if(settings["inputStopType"] != "time") settings["inputStopTime"] = null
                    if(settings["inputStopType"] != "sunrise" && settings["inputStopType"] != "sunset") {
                        settings["inputStopSunriseType"] = null
                        settings["inputStopBefore"] = null
                    }
                     
                    displayStopTimeSection()
                }

                if(checkTimeComplete("start") && checkTimeComplete("stop")){
                    displayBrightnessOption()
                    displayTemperatureOption()
                    displayColorOption()
                displayChangeModeOption()
                }
            }
        }
        section(){
            if(error) paragraph "$error</div>"
            if(warning) paragraph "$warning</div>"
        }
    }
}

/* ************************************************** */
/* TO-DO: Test for schedule spanning two days, but    */
/* scheduled for specific days;                       */
/* Warn that progressive changes may not work as      */
/* intended, and will not turn off on a non-scheduled */
/* day.                                               */
/* ************************************************** */
/* ************************************************** */
/* TO-DO: Add warning for if time span is small and   */
/* changes are large, where change will not be        */
/* smooth.                                            */
/* ************************************************** */


// Display functions

def errorMessage(text){
    if(error){
        error = "$error<br />$errorIcon $text"
    } else {
        error = "<div style=\"background-color:Bisque\">$errorIcon $text"
    }
}

def warningMessage(text){
    if(warning){
        warning = "$warning<br />$warningIcon $text"
    } else {
        warning = "<div style=\"background-color:LemonChiffon\">$warningIcon $text"
    }
}

def displayLabel(text = "Null", width = 12){
    paragraph("<div style=\"background-color:#DCDCDC\"><b> $text:</b></div>",width:width)
}

def displayInfo(text = "Null",noDisplayIcon = null){
    if(text == "Null") {
        paragraph "<div style=\"background-color:AliceBlue\"> </div>"
    } else {
        if(noDisplayIcon){
            paragraph "<div style=\"background-color:AliceBlue\"> &nbsp; &nbsp; $text</div>"
        } else {
            paragraph "<div style=\"background-color:AliceBlue\">$infoIcon $text</div>"
        }
    }
}

def displayNameOption(){
    if(app.label){
            displayLabel("Schedule name",2)

       // paragraph("Name of this schedule",width:2)
            label title: "", required: true, width: 10,submitOnChange:true
    } else {
            displayLabel("Set name for this schedule")
            label title: "", required: true, submitOnChange:true
            displayInfo("Name this schedule. Each schedule must have a unique name.")
    }
}

def displayDevicesOption(){
    if(timeDevice){
        timeDevice.each{
            if(count == 1) multipleDevices = true
            count = 1
        }
        if(multipleDevices){
            pluralInput = "Devices"
        } else {
            pluralInput = "Device"
        }
        input "timeDevice", "capability.switch", title: "$pluralInput:", multiple: true, submitOnChange:true
    } else {
        //displayLabel("Select which device(s) to schedule")
        input "timeDevice", "capability.switch", title: "Select device(s) to schedule:", multiple: true, submitOnChange:true
        displayInfo("Select which device(s) to schedule, either for controlling the device or setting default levels.")
    }
}

def displayDisableOption(){
    if(disable){
        input "disable", "bool", title: "<b><font color=\"#000099\">This schedule is disabled.</font></b> Reenable it?", submitOnChange:true
    } else {
        input "disable", "bool", title: "This schedule is enabled. Disable it?", submitOnChange:true
    }
}

def displayStartTimeSection(){
    // If all options entered
    if(checkTimeComplete("start") && settings["timeOn"]){
        varStartTime = getTimeVariables("start")
        
        if(settings["timeOn"] == "on"){
            title = "Starting: Turning on"
        } else if(settings["timeOn"] == "off"){
            title = "Starting: Turning off"
        } else if(settings["timeOn"] == "toggle"){
            title = "Starting: Toggling"
        } else {
            title = "Starting"
        }
        section(hideable: true, hidden: true, "$title $varStartTime"){
            displayStartTypeOption()

            // Display exact time option
            if(settings["inputStartType"] == "time"){
                displayTimeOption("start")
            } else {
                // Display sunrise/sunset type option (at/before/after)
                displaySunriseTypeOption("start")
                // Display sunrise/sunset offset
                if(inputStartSunriseType != "at"){
                    displaySunriseOffsetOption("start")
                }
            }
            displayActionOption("start")
        }
        // If missing option(s)
    } else {
        section(){
            displayStartTypeOption()

            // Display exact time option
            if(settings["inputStartType"] == "time"){
                displayTimeOption("start")
                // Display sunrise/sunset type option (at/before/after)
            } else if(settings["inputStartType"] == "sunrise" || settings["inputStartType"] == "sunset"){
                displaySunriseTypeOption("start")
                
                // Display sunrise/sunset offset
                if(inputStartSunriseType == "before" || inputStartSunriseType == "after"){
                    displaySunriseOffsetOption("start")
                }
            }
            if(checkTimeComplete("start")) displayActionOption("start")
        }
    }

    if(checkTimeComplete("start") && (!checkTimeComplete("stop") || settings["inputStopType"] == "none")){
        displayTimeDaysOption()
    }

    if(checkTimeComplete("start") && (!checkTimeComplete("stop") || settings["inputStopType"] == "none")){
        displayIfModeOption()
    }
}

def displayStopTimeSection(){
    // If all options entered
    if(checkTimeComplete("stop")){
        varStopTime = getTimeVariables("stop")
        if(settings["inputStopType"] == "none"){
            title = "Not stopping"
        } else if(settings["timeOff"] == "on"){
            title = "Stopping: Turning on $varStopTime"
        } else if(settings["timeOff"] == "off"){
            title = "Stopping: Turning off $varStopTime"
        } else if(settings["timeOff"] == "toggle"){
            title = "Stopping: Toggling $varStopTime"
        } else {
            title = "Stopping $varStopTime"
        }
        
        section(hideable: true, hidden: true, "$title"){
            displayStopTypeOption()
            // Display exact time option
            if(settings["inputStopType"] == "time"){
                displayTimeOption("stop")
            } else if(settings["inputStopType"] == "sunrise" || settings["inputStopType"] == "sunset"){
                // Display sunrise/sunset type option (at/before/after)
                displaySunriseTypeOption("stop")
                // Display sunrise/sunset offset
                if(inputStopSunriseType != "at"){
                    displaySunriseOffsetOption("stop")
                }
            }
            if(settings["inputStopType"] != "none") displayActionOption("stop")
        }
        // If missing option(s)
    } else {
        section(){
            displayStopTypeOption()

            // Display exact time option
            if(settings["inputStopType"] == "time"){
               displayTimeOption("stop")
                // Display sunrise/sunset type option (at/before/after)
            } else if(settings["inputStopType"] == "sunrise" || settings["inputStopType"] == "sunset"){
                displaySunriseTypeOption("stop")
                // Display sunrise/sunset offset
                if(inputStopSunriseType == "before" || inputStopSunriseType == "after"){
                   displaySunriseOffsetOption("stop")
                }
            }
            if(checkTimeComplete("stop") && settings["inputStopType"] != "none") displayActionOption("stop")
        }
    }
        if(checkTimeComplete("stop") && settings["inputStopType"] != "none"){
        displayTimeDaysOption()
    }
    if(checkTimeComplete("stop") && settings["inputStopType"] != "none"){
        displayIfModeOption()
    }
}

def displayStartTypeOption(){
    if(!checkTimeComplete("start")){
        displayLabel("Schedule starting time")
    } else {
        displayLabel("Schedule start")
    }
    if(!settings["inputStartType"]){
        width = 12
        input "inputStartType", "enum", title: "Start time (click to choose option):", multiple: false, width: width, options: ["time":"Start at specific time", "sunrise":"Sunrise (at, before or after)","sunset":"Sunset (at, before or after)" ], submitOnChange:true
        displayInfo("Select whether to enter a specific time, or have start time based on sunrise and sunset for the Hubitat location. Required field.")
    } else {
        if(settings["inputStartType"] == "time" || !settings["inputStartSunriseType"] || settings["inputStartSunriseType"] == "at"){
            width = 6
        } else if(inputStartSunriseType){
            width = 4
        }
        input "inputStartType", "enum", title: "Start time option:", multiple: false, width: width, options: ["time":"Start at specific time", "sunrise":"Sunrise (at, before or after)","sunset":"Sunset (at, before or after)" ], submitOnChange:true
    }
}

def displayStopTypeOption(){
    if(!checkTimeComplete("stop")){
        displayLabel("Schedule stopping time")
    } else {
        displayLabel("Schedule stop")
    }
    if(!settings["inputStopType"]){
        width = 12
        input "inputStopType", "enum", title: "Stop time (click to choose option):", multiple: false, width: width, options: ["none":"Don't stop", "time":"Stop at specific time", "sunrise":"Sunrise (at, before or after)","sunset":"Sunset (at, before or after)" ], submitOnChange:true
        displayInfo("Select \"Don't stop\" only if you only want to $message $varStartTime. Select to enter a stop time or use sunrise/sunset if you want to:")
        displayInfo("• Set the device(s) turn on, turn off, or toggle at the end of the schedule, or","none")
        displayInfo("• Set the device(s) to a default level throughout a portion of the day, or","none")
        displayInfo("• Set the device(s) to transition levels over time.","none")
    } else {
        if(!settings["inputStopType"] || settings["inputStopType"] == "none"){
            width = 12
        } else if(settings["inputStopType"] == "time" || !settings["inputStopSunriseType"] || settings["inputStopSunriseType"] == "at"){
            width = 6
        } else if(inputStopSunriseType){
            width = 4
        }
        input "inputStopType", "enum", title: "Stop time option:", multiple: false, width: width, options: ["none":"Don't stop", "time":"Stop at specific time", "sunrise":"Sunrise (at, before or after)","sunset":"Sunset (at, before or after)" ], submitOnChange:true
    }
}

def displayTimeOption(lcType){
    ucType = lcType.capitalize()
    input "input${ucType}Time", "time", title: "$ucType time:", width: width, submitOnChange:true
    if(!settings["input${ucType}Time"]) displayInfo("Enter the time to $lcType the schedule in \"hh:mm AM/PM\" format. Required field.")
}

def displaySunriseTypeOption(lcType){
    if(!settings["input${ucType}SunriseType"] || settings["input${ucType}SunriseType"] == "at") {
        width = 6 
    } else {
        width = 4
    }
    sunriseTime = getSunriseAndSunset()[settings["input${ucType}Type"]].format("hh:mm a")
    input "input${ucType}SunriseType", "enum", title: "At, before or after " + settings["input${ucType}Type"] + ":", multiple: false, width: width, options: ["at":"At " + settings["input${ucType}Type"], "before":"Before " + settings["input${ucType}Type"], "after":"After " + settings["input${ucType}Type"]], submitOnChange:true
    if(!settings["input${ucType}SunriseType"]) displayInfo("Select whether to start exactly at " + settings["input${ucType}Type"] + " (currently, $sunriseTime). To allow entering minutes prior to or after " + settings["input${ucType}Type"] + ", select \"Before " + settings["input${ucType}Type"] + "\" or \"After " + settings["input${ucType}Type"] + "\". Required field.")
}

def displayActionOption(lcType){
    ucType = lcType.capitalize()
    if(lcType == "start") {
        varName = "timeOn"
    } else {
        varName = "timeOff"
    }
    
    varStartTime = getTimeVariables(lcType)
    if(!checkTimeComplete(lcType)) return
            timeDevice.each{
            if(count == 1) multipleDevices = true
            count = 1
        }
        if(multipleDevices){
            plural = "devices"
        } else {
            plural = "device"
        }
    if(settings["timeOn"]){
        
    input varName, "enum", title: "Turn devices on or off?", multiple: false, width: 12, options: ["none": "Don't turn on or off (leave as is)","on": "Turn On", "off": "Turn Off", "toggle": "Toggle"], submitOnChange:true
    } else {
        input varName, "enum", title: "Turn devices on or off?", multiple: false, width: 12, options: ["none": "Don't turn on or off (leave as is)","on": "Turn On", "off": "Turn Off", "toggle": "Toggle"], submitOnChange:true
    }
    if(lcType == "stop") lcType = "stopp"
    if(!settings[varName]) displayInfo("Set whether to turn on or off, or toggle Device(s), when ${lcType}ing the schedule. Select \"Don't\" to not have it turn on, turn off, or toggle. Toggle turns on devices that are off, and turns off devices that are on. Required field.")
}

def displayTimeDaysOption(){
    if(settings["timeDays"]){
        sectionText = "Only on: $settings.timeDays"
        helpTip = "This will limit the schedule from running unless it's $settings.timeDays."
    } else {
        sectionText = "<b>Click to select on which days</b> (optional)"
        helpTip = "Select which day(s) on which to schedule. Applies to both beginning and ending of the schedule. If none are selected, schedule will default to every day."
    }

    section(hideable: true, hidden: true, sectionText){
        input "timeDays", "enum", title: "On these days (defaults to all days)", multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"], submitOnChange:true
        displayInfo(helpTip)
    }
}

def displayIfModeOption(){
    if(settings["ifMode"]){
        sectionText = "Only with Mode: $settings.ifMode"
        helpTip = "This will limit the schedule from running unless Hubitat's Mode is $settings.ifMode."
    } else {
        sectionText = "<b>Click to select with what Mode</b> (optional)"
        helpTip = "This will limit the schedule from running unless Hubitat's Mode is as selected."
    }

    section(hideable: true, hidden: true, sectionText){
        input "ifMode", "mode", title: "Only run if Mode is already?", width: 12, submitOnChange:true
        displayInfo(helpTip)
    }
}
  
def getTimeVariables(lcType){
    ucType = lcType.capitalize()
    // If time, then set string to "at [time]"
    if(settings["input${ucType}Type"] == "time"){
        return "at " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings["input${ucType}Time"]).format("h:mm a", location.timeZone)
        // If sunrise or sunset
    } else if((settings["input${ucType}Type"] == "sunrise" || settings["input${ucType}Type"] == "sunset")  && settings["input${ucType}SunriseType"]){
        if(settings["input${ucType}SunriseType"] == "at"){
            // Set string to "at sun[rise/set] ([sunrise/set time])"
            return "at " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset()[settings["input${ucType}Type"]].format("hh:mm a") + ")"
            // If before sunrise
        } else if(settings["input${ucType}Type"] == "sunrise" && settings["input${ucType}SunriseType"] == "before" && settings["input${ucType}Before"]){
            // Set string to "[number] minutes before sunrise ([time])
            if(settings["input${ucType}Before"]) return settings["input${ucType}Before"] + " minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset(sunriseOffset: (settings["input${ucType}Before"] * -1), sunsetOffset: 0)[settings["input${ucType}Type"]].format("hh:mm a") + ")"
            // If after sunrise
        } else if(settings["input${ucType}Type"] == "sunrise" && settings["input${ucType}SunriseType"] == "after" && settings["input${ucType}Before"]){
            // Set string to "[number] minutes after sunrise ([time])
            if(settings["input${ucType}Before"]) return settings["input${ucType}Before"] + " minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset(sunriseOffset: settings["input${ucType}Before"], sunsetOffset: 0)[settings["input${ucType}Type"]].format("hh:mm a") + ")"
            // If before sunset
        } else if(settings["input${ucType}Type"] == "sunset" && settings["input${ucType}SunriseType"] == "before" && settings["input${ucType}Before"]){
            // Set string to "[number] minutes before sunset ([time])
            if(settings["input${ucType}Before"]) return settings["input${ucType}Before"] + " minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: (settings["input${ucType}Before"] * -1))[settings["input${ucType}Type"]].format("hh:mm a") + ")"
            // If after sunrise
        } else if(settings["input${ucType}Type"] == "sunset" && settings["input${ucType}SunriseType"] == "after" && settings["input${ucType}Before"]){
            // Set string to "[number] minutes after sunset ([time])
            if(settings["input${ucType}Before"]) return settings["input${ucType}Before"] + " minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: settings["input${ucType}Before"])[settings["input${ucType}Type"]].format("hh:mm a") + ")"
        } else {
            return
        }
    } else {
        return
    }
}

def displaySunriseOffsetOption(lcType){
    ucType = lcType.capitalize()
    if(settings["input${ucType}Before"] && settings["input${ucType}Before"] > 1441){
        // "Minues [before/after] [sunrise/set] is equal to "
        message = "Minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " is equal to "
        if(settings["input${ucType}Before"]  > 2881){
            // "X days"
            message += Math.floor(settings["input${ucType}Before"]  / 60 / 24) + " days."
        } else {
            message += "a day."
        }
        warningMessage(message)
    }
    input "input${ucType}Before", "number", title: "Minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + ":", width: 4, submitOnChange:true
    if(!settings["input${ucType}Before"]) displayInfo("Enter the number of minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " to start the schedule. Required field.")
}

def checkTimeComplete(lcType){
    ucType = lcType.capitalize()
    if(lcType == "stop" && settings["inputStopType"] == "none") return true
    if((settings["input${ucType}Type"] == "time" && settings["input${ucType}Time"]) || 
       ((settings["input${ucType}Type"] == "sunrise" || settings["input${ucType}Type"] == "sunset") && settings["input${ucType}SunriseType"] == "at") || 
       ((settings["input${ucType}Type"] == "sunrise" || settings["input${ucType}Type"] == "sunset") && (settings["input${ucType}SunriseType"] == "before" || settings["input${ucType}SunriseType"] == "after") && (settings["input${ucType}Before"]))){
        return true
    } else {
        return false
    }
}

def displayBrightnessOption(){
    hidden = true
    // If just start or stop level, then don't hide
    if((settings["levelOn"] && !settings["levelOff"] && settings["inputStopType"] != "none") || (settings["levelOff"] && !settings["levelOn"])) hidden = false
 
    if(!settings["levelOn"] && (!settings["levelOff"] || settings["inputStopType"] == "none")){
        sectionTitle = "<b>Click to schedule brightness</b> (optional)"
    } else {
        // If just start level
        if(settings["levelOn"] && !settings["levelOff"]) {
            sectionTitle = "Start brightness $settings.levelOn%"
        // Both start and stop levels
        } else if(settings["levelOn"] && settings["levelOff"]) {
            sectionTitle = "Transitioning brightness from $settings.levelOn% to $settings.levelOff%"
        // Just stop level
        } else if(!settings["levelOn"] && settings["levelOff"]){
            sectionTitle = "Stop brightness $settings.levelOff%"
        }
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        if(settings["inputStopType"] == "none"){
            //Should allow setting level, temp and color when schedule ends, even if not turning the device on or off
            input "levelOn", "number", title: "Set brightness ($varStartTime)?", width: 12, submitOnChange:true
            if(!settings["levelOn"]) {
                displayInfo("Enter the percentage of brightness when turning on, from 1 to 100. Required field (or unselect \"Change brightness\").")
            } else {
                displayInfo("Brightness is percentage from 1 to 100.")
            }
        } else {
            displayLabel("Enter beginning and/or ending brightness")
            input "levelOn", "number", title: "Beginning brightness ($varStartTime)?", width: 6, submitOnChange:true
            input "levelOff", "number", title: "and ending brightness ($varStopTime)?", width: 6, submitOnChange:true
            if(!settings["levelOn"] && !settings["levelOff"]) {
                displayInfo("Enter the percentage of brightness when turning on, from 1 to 100, when starting and/or ending the schedule. If entering both starting and ending brightness, it will transition from beginning to ending brightness for the duration of the schedule. Either starting or ending brightness is required (or unselect \"Change brightness\").")
            } else if(!settings["levelOn"] || !settings["levelOff"]){
                displayInfo("Enter the percentage of brightness when turning on, from 1 to 100. If entering both starting and ending brightness, it will transition from beginning to ending brightness for the duration of the schedule.")
            } else {
                displayInfo("Brightness is percentage from 1 to 100.")
            }
        }

    }
    if(!settings["levelOn"] && levelOff && timeOff == "off") warningMessage("With no beginning brightness while setting Device(s) to turn off, setting an ending brightness won't do anything.")
    if(settings["levelOn"] && settings["levelOn"] == settings["levelOff"]) warningMessage("Beginning and ending brightness are both set to $settings['levelOn']. This won't hurt anything, but the Stop brightness setting won't actually <i>do</i> anything.")
    if(settings["levelOn"] > 100) errorMessage("Brightness is percentage from 1 to 100. Correct beginning brightness.")
    if(settings["levelOff"] > 100) errorMessage("Brightness is percentage from 1 to 100. Correct ending brightness.")
}

def displayTemperatureOption(){
    hidden = true
    // If just start or stop level, then don't hide
    if((settings["tempOn"] && !settings["tempOff"] && settings["inputStopType"] != "none") || (settings["tempOff"] && !settings["tempOn"])) hidden = false
 
    if(!settings["tempOn"] && (!settings["tempOff"] || settings["inputStopType"] == "none")){
        sectionTitle = "<b>Click to schedule temperature color</b> (optional)"
    } else {
        // If just start level
        if(settings["tempOn"] && !settings["tempOff"]) {
            sectionTitle = "Start temperature color " + settings["tempOn"] + "K"
        // Both start and stop levels
        } else if(settings["tempOn"] && settings["tempOff"]) {
            sectionTitle = "Transitioning temperature color from " + settings["tempOn"] + "K to " + settings["tempOff"] + "K"
        // Just stop level
        } else if(settings["tempOff"]){
            sectionTitle = "Stop temperature color " + settings["tempOff"] + "K"
        }
    }
   
    section(hideable: true, hidden: hidden, sectionTitle){
        if(settings["inputStopType"] == "none"){
            //Should allow setting level, temp and color when schedule ends, even if not turning the device on or off
            displayLabel("Enter default color temperature")
            input "tempOn", "number", title: "Set color temperature?", width: 12, submitOnChange:true
            if(!settings["tempOn"]) {
                displayInfo("Temperature color is in Kelvin from 1800 to 5400, when starting and/or ending the schedule. Lower values have more red, while higher values have more blue, where daylight is 5000, cool white is 4000, and warm white is 3000. Required field (or unselect \"Change temperature\").")
            } else {
                displayInfo("Temperature color is from 1800 to 5400, where daylight is 5000, cool white is 4000, and warm white is 3000.")
            }
        } else {
            displayLabel("Enter beginning and/or ending temperature color")
            input "tempOn", "number", title: "Beginning temperature color?", width: 6, submitOnChange:true
            input "tempOff", "number", title: "and ending temperature color?", width: 6, submitOnChange:true
            if(!settings["tempOn"] && !settings["tempOff"]) {
                displayInfo("Temperature color is in Kelvin from 1800 to 5400, when starting and/or ending the schedule. Lower values have more red, while higher values have more blue, where daylight is 5000, cool white is 4000, and warm white is 3000. If entering both starting and ending temperature, it will transition from beginning to ending temperature for the duration of the schedule. Either starting or ending temperature is required (or unselect \"Change temperature\").")
            } else if(!settings["tempOn"] || !settings["tempOff"]){
                displayInfo("Temperature color is from 1800 to 5400, where daylight is 5000, cool white is 4000, and warm white is 3000. If entering both starting and ending temperature, it will transition from beginning to ending temperature for the duration of the schedule.")
            } else {
                displayInfo("Temperature color is from 1800 to 5400, where daylight is 5000, cool white is 4000, and warm white is 3000.")
            }
        }

    }
    if(!settings["tempOn"] && settings["tempOff"] && settings["tempOff"]== "off") warningMessage("With no beginning color temperature while setting Device(s) to turn off, setting an ending temperature color won't do anything.")
    if(settings["tempOn"] && settings["tempOn"] == settings["tempOff"]) warningMessage("Beginning and ending color temperature are both set to $settings.tempOn. This won't hurt anything, but the Stop color temperature setting won't actually <i>do</i> anything.")
    if(settings["tempOn"] && (settings["tempOn"] < 1800 || settings["tempOn"] > 5400)) errorMessage("Temperature color is from 1800 to 5400, where daylight is 5000, warm white is 3000, and cool white is 4000. Correct beginning temperature.")
    if(settings["tempOff"] && (settings["tempOff"] < 1800 || settings["tempOff"] > 5400)) errorMessage("Temperature color is from 1800 to 5400, where daylight is 5000, warm white is 3000, and cool white is 4000. Correct ending temperature.")
}

def displayColorOption(){
    hidden = false
    // If nothing entered, hide
    if(!settings["hueOn"] && !settings["hueOff"]  && !settings["satOn"] && !settings["satOff"]) hidden = true
    // If everything enetered, hide
    if(settings["hueOn"] && ((settings["hueOff"] && settings["hueDirection"]) || settings["inputStopType"] == "none") && settings["satOn"] && (settings["satOff"] || settings["inputStopType"] == "none")) hidden = true
    
    if(!settings["hueOn"] && !settings["satOn"] && ((!settings["hueOff"] && !settings["satOff"]) || settings["inputStopType"] == "none")){
        hueTitle = "<b>Click to schedule color (hue and/or saturation)</b> (optional)"
    } else {
        // If just start level
        if(settings["hueOn"] && !settings["hueOff"]) {
            hueTitle = "Start hue " + settings["hueOn"]
            // Both start and stop levels
        } else if(settings["hueOn"] && settings["hueOff"]) {
            hueTitle = "Transitioning hue from " + settings["hueOn"] + " to " + settings["hueOff"]
            if(settings["hueDirection"] == "reverse") hueTitle += " (in reverse order)"
            // Just stop level
        } else if(settings["hueOff"]){
            hueTitle = "Stop hue " + settings["hueOff"]
        }
        // If just start level
        if(settings["satOn"] && (!settings["satOff"] && settings["inputStopType"] != "none")) {
            satTitle = "Start sat " + settings["satOn"] + "%"
            // Both start and stop levels
        } else if(settings["satOn"] && settings["satOff"]) {
            satTitle = "Transitioning sat from " + settings["satOn"] + "% to " + settings["satOff"] + "%"
            // Just stop level
        } else if(settings["satOff"]){
            satTitle = "Stop hue " + settings["satOff"] + "%"
        }
    }
    if(hueTitle && satTitle){
        sectionTitle = "$hueTitle; $satTitle"
    } else if(hueTitle){
        sectionTitle = hueTitle
    } else if(satTitle){
        sectionTitle = satTitle
    }
    
    section(hideable: true, hidden: hidden, sectionTitle){
        if(settings["inputStopType"] == "none"){
            //Should allow setting level, temp and color when schedule ends, even if not turning the device on or off
            displayLabel("Enter default hue and/or color saturation")
            input "hueOn", "number", title: "Set hue?", width: 6, submitOnChange:true
            input "satOn", "number", title: "Set color saturation?", width: 6, submitOnChange:true
            displayInfo("Hue is the shade of color, from 1 to 100, when starting and/or ending the schedule. Red is 1 or 100, yellow is 11, green is 26, blue is 66, and purple is 73.")
            displayInfo("Saturation is the percentage depth of color, from 1 to 100, where 1 is hardly any color tint and 100 is full color. Either hue or saturation is required (or unselect \"Change color\").")
        } else {
            displayLabel("Enter beginning and/or ending hue")
            if(settings["hueOn"] && settings["hueOff"]){
                if(settings["hueOn"] < settings["hueOff"]){
                    forwardSequence = "25, 26, 27  ... 73, 74, 75"
                    reverseSequence = "25, 24, 23 ... 2, 1, 100, 99 ... 77, 76, 75"
                } else {
                    forwardSequence = "75, 76, 77 ... 99, 100, 1, 2 ... 23, 24, 25"
                    reverseSequence = "75, 74, 73 ... 27, 26, 25"
                }
                input "hueOn", "number", title: "Beginning hue?", width: 4, submitOnChange:true
                input "hueOff", "number", title: "and ending hue?", width: 4, submitOnChange:true
                input "hueDirection", "enum", title: "Which order to change hue?", width: 4, submitOnChange:true, options: ["forward": forwardSequence, "reverse": reverseSequence]
            } else {
                input "hueOn", "number", title: "Beginning hue?", width: 6, submitOnChange:true
                input "hueOff", "number", title: "and ending hue?", width: 6, submitOnChange:true
            }
        if(hueOn && hueOff && hueDirection){
            displayInfo("Hue is the shade of color, when starting and/or ending the schedule. Red is 1 or 100, yellow is 11, green is 26, blue is 66, and purple is 73.")
        } else if(hueOn && hueOff && !hueDirection){
            displayInfo("Hue is the shade of color. Red is 1 or 100, yellow is 11, green is 26, blue is 66, and purple is 73. It will transition from beginning to ending temperature for the duration of the schedule. For \"direction\", if for instances, a start value of 1 and end value of 26 is entered, allows for chosing whether it would change from red to yellow then blue, or from red to purple, blue, then green.")
        } else if(!hueOn || !hueOff){
            displayInfo("Hue is the shade of color. Red is 1 or 100, yellow is 11, green is 26, blue is 66, and purple is 73. If entering both starting and ending hue, it will transition from beginning to ending temperature for the duration of the schedule.")
        } else if(!hueOn && !hueOff){
            displayInfo("Hue is the shade of color, from 1 to 100, when starting and/or ending the schedule. Red is 1 or 100, yellow is 11, green is 26, blue is 66, and purple is 73. If entering both starting and ending hue, it will transition from beginning to ending temperature for the duration of the schedule. Optional field.")
        }
            displayLabel("Enter beginning and/or color saturation")
            input "satOn", "number", title: "Beginning color saturation?", width: 6, submitOnChange:true
            input "satOff", "number", title: "and ending color saturation?", width: 6, submitOnChange:true
        if(!satOn && !satOff){
            displayInfo("Saturation is the percentage amount of color tint displayed, from 1 to 100, when starting and/or ending the schedule. 1 is hardly any color tint and 100 is full color. If entering both starting and ending saturation, it will transition from beginning to ending saturation for the duration of the schedule. Optional field.")
        } else if(!satOn || !satOff){
            displayInfo("Saturation is the percentage amount of color tint displayed, from 1 to 100, where 1 is hardly any color tint and 100 is full color. If entering both starting and ending saturation, it will transition from beginning to ending saturation for the duration of the schedule. Optional field.")
        } else {
            displayInfo("Saturation is the percentage amount of color tint displayed, from 1 to 100, where 1 is hardly any color tint and 100 is full color.")
        }
        }

    }

    if(hueOn && hueOn == hueOff) warningMessage("Beginning and ending hue are both set to $hueOn. This won't hurt anything, but the Stop hue setting won't actually <i>do</i> anything.")
    if(satOn && satOn == satOff) warningMessage("Beginning and ending saturation are both set to $satOn. This won't hurt anything, but the Stop saturation setting won't actually <i>do</i> anything.")
    if(hueOn > 100) errorMessage("Beginning hue can't be more than 100. Correct before saving.")
    if(hueOff && hueOff > 100) errorMessage("Ending hue can't be more than 100. Correct before saving.")
    if(satOn > 100) errorMessage("Beginning saturation can't be more than 100. Correct before saving.")
    if(satOff && satOff > 100) errorMessage("Ending saturation can't be more than 100. Correct before saving.")
}

def displayModeOption(){
    section(){
        if(inputStopType == "none"){
            displayLabel("Change Mode $varStartTime")
            input "modeChangeOn", "mode", title: "Set Hubitat's \"Mode\" ($varStartTime)?", width: 12, submitOnChange:true
        } else {
            displayLabel("Change Mode $varStartTime and/or $varStopTime")
            input "modeChangeOn", "mode", title: "Set Hubitat's \"Mode\" ($varStartTime)?", width: 6, submitOnChange:true
            input "modeChangeOff", "mode", title: "Set Hubitat's \"Mode\" ($varStopTime)?", width: 6, submitOnChange:true
        }
    }
}

def displayChangeModeOption(){
        hidden = true
    // If just start or stop level, then don't hide
    if((settings["modeChangeOn"] && !settings["modeChangeOff"] && settings["inputStopType"] != "none") || (settings["modeChangeOn"] && !settings["modeChangeOff"])) hidden = false
 
    if(!settings["modeChangeOn"] && (!settings["modeChangeOff"] || settings["inputStopType"] == "none")){
        sectionTitle = "<b>Click to change Modes</b> (optional)"
    } else {
        // If just start level
        if(settings["modeChangeOn"] && (!settings["modeChangeOff"] && settings["inputStopType"] != "none")) {
            sectionTitle = "Start Mode " + settings["modeChangeOn"]
        // Both start and stop levels
        } else if(settings["modeChangeOn"] && settings["modeChangeOff"]) {
            sectionTitle = "Start Mode " + settings["modeChangeOn"] + "; stop Mode " + settings["modeChangeOff"]
        // Just stop level
        } else if(settings["modeChangeOff"]){
            sectionTitle = "Stop Mode " + settings["modeChangeOff"]
        }
    }
   
    section(hideable: true, hidden: hidden, sectionTitle){

        if(inputStopType == "none"){
            displayLabel("Change Mode $varStartTime")
            input "modeChangeOn", "mode", title: "Set Hubitat's \"Mode\" ($varStartTime)?", width: 12, submitOnChange:true
        } else {
            displayLabel("Change Mode $varStartTime and/or $varStopTime")
            input "modeChangeOn", "mode", title: "Set Hubitat's \"Mode\" ($varStartTime)?", width: 6, submitOnChange:true
            input "modeChangeOff", "mode", title: "Set Hubitat's \"Mode\" ($varStopTime)?", width: 6, submitOnChange:true
        }
    }
}


/*
disableAll - bool - Flag to disable all schedules
disable - bool - Flag to disable this single schedule
timeOn - enum (none, on, off, toggle) - What to do with timeDevice at starting time
timeOff - enum (none, on, off, toggle) - What to do with timeDevice at stopping time
timeDevice - capability.switch - Device(s) being scheduled
timeDays - enum (Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday) - Day(s) of the week schedule will run
inputStartType - enum (time, sunrise, sunset) - Sets whether start time is a specific time, or based on sunrise or sunset
inputStartTime - time - Start Time (only displays when inputStartType = "time")
inputStartSunriseType - enum (at, before, after) - Sets whether start time is sunrise/sunset time, or uses positive or negative offset (only displays if inputStartType = "sunrise" or "sunset")
inputStartBefore - number (1-) - Number of minutes before/after sunrise/sunset (only displays if inputStartType = "sunrise" or "sunset" and inputStartSunriseType = "before" or "after")
inputStopType - emum (none, time, sunrise, sunset) - Sets whether there is a stop time, and whether it is a specific time, or based on sunrise or sunset
inputStopTime - time - Stop Time (only displays when inputStopType = "time")
inputStopSunriseType - enum (at, before, after) - Sets whether stop time is sunrise/sunset time, or uses positive or negative offset (only displays if inputStartType = "sunrise" or "sunset")
inputStopBefore - number (1-) - Number of minutes before/after sunrise/sunset (only displays if inputStartType = "sunrise" or "sunset" and inputStartSunriseType = "before" or "after")
levelOn - number (1-100) - Level to set at start time
levelOff - number (1-100) - Level to set at stop time
tempOn - number (1800-5400) - Temperature to set at start time
tempOff - number (1800-5400) - Temperature to set at stop time
hueOn - number (1-100) - Hue to set at start time
hueOff - number (1-100) - Hue to set at stop time
hueDirection - enum (Forward, Reverse) - "Direction" in which to change hue; only displays if hueOn and hueOff have values
satOn - number (1-100) - Sat to set at start time
satOff - number (1-100) - Sat to set at stop time
modeChangeOn - mode - Mode to set at start time
//modeChangeOff - mode - Mode to set at stop time [doesn't currently exist]
ifMode - mode - Mode system must have for schedule to run
*/

/* ************************************************************************ */
/*                                                                          */
/*                          End display functions.                          */
/*                                                                          */
/* ************************************************************************ */

def installed() {
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
    state.logLevel = getLogLevel()
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    subscribe(timeDevice, "switch.on", handleStateChange)

    unschedule()

    if(disable) {
        state.disable = true
        return
    } else {
        state.disable = false
    }

    // Clean up settings values
    if(inputStartType != "sunrise" && inputStartType != "sunset") {
        settings.inputStartSunriseType = null
        settings.inputStartBefore = null
    }
    if(inputStartSunriseType == "at") settings.inputStartBefore = null
    if(inputStopType != "sunrise" && inputStopType != "sunset") {
        settings.inputStopSunriseType = null
        settings.inputStopBefore = null
    }
    if(inputStopSunriseType == "at") settings.inputStopBefore = null
    if(inputStopType == "none"){
        settings.timeOff = null
        settings.inputStopSunriseType = null
        settings.inputStopBefore = null
        settings.levelOff = null
        settings.tempOff = null
        settings.hueOff = null
        settings.satOff = null
    }
    if(!levelOn || levelOn == levelOff) settings.levelOff = null
    if(!tempOn || tempOn == tempOff) settings.tempOff = null
    if(!hueOn || hueOn == hueOff) settings.hueOff = null
    if(!satOn || satOn == satOff) settings.satOff = null

    // Set start time, stop time, and total seconds
    if(!setTime()) return false
    setWeekDays()

    setDailySchedules()
    setIncrementalSchedule()

    return true
}

def setDailySchedules(type = null){
    if(type != "stop") {
        unschedule(runDailyStartSchedule)
    } else if(type != "start"){
        unschedule(runDailyStopSchedule)
    }
    // Schedule dailyStart, either every day or with specific days
    if(type != "stop"){
        startHours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.start).format('HH').toInteger()
        startMinutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.start).format('mm').toInteger()
    }
    if(type != "start" && atomicState.stop){
        stopHours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.stop).format('HH').toInteger()
        stopMinutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.stop).format('mm').toInteger()
    }

    if(state.weekDays) {
        days = "? * $state.weekDays"
    } else {
        days = "* * ?"
    }

    // Schedule start
    if(type != "stop") {
        scheduleString = "0 $startMinutes $startHours $days"
        runIn(1,setStartSchedule, [data: [scheduleString: scheduleString]])
    }

    if((type != "start") && atomicState.stop){
        scheduleString = "0 $stopMinutes $stopHours $days"
        runIn(1,setStopSchedule, [data: [scheduleString: scheduleString]])
    }
    return true
}

def setStartSchedule(data){
    schedule(data.scheduleString, runDailyStartSchedule)
    if(checkLog(a="debug")) putLog(889,"Scheduling runDailyStartSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.start).format("h:mma MMM dd, yyyy", location.timeZone) + " ($data.scheduleString)",a)
}

def setStopSchedule(data){
    schedule(data.scheduleString, runDailyStopSchedule)
    if(checkLog(a="debug")) putLog(894,"Scheduling runDailyStopSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.stop).format("h:mma MMM dd, yyyy", location.timeZone) + " ($data.scheduleString)",a)
}

// Performs actual changes for incremental schedule
// Called only by schedule set in incrementalSchedule
def runIncrementalSchedule(){
    if(state.disable) return

    // If nothing to do, exit
    if((!levelOn || !levelOff) && (!tempOn || !tempOff) && (!hueOn || !hueOff) && (!satOn || !satOff)) return

    // If device(s) not on, exit
    if(!parent.isOnMulti(timeDevice)){
        if(checkLog(a="debug")) putLog(907,"Since $timeDevice is off, stopping recurring schedules",a)
        return
    }

    // Set levels
    timeDevice.each{
        // If individual device is on, then...
        if(parent.isOn(it,app.label)){
            defaults = parent.getScheduleDefaultSingle(it,,app.label)
            if(defaults) parent.setLevelSingle(defaults,it,,app.label)
        }
    }

    // Reschedule itself
    setIncrementalSchedule()
    return true
}

def setIncrementalSchedule(){
    unschedule(runIncrementalSchedule)
    if(!getScheduleActive()) {
        return
    } else {
        if(checkLog(a="debug")) putLog(930,"Scheduling incremental for 20 seconds",a)
        runIn(20,runIncrementalSchedule)
    }
}

// Performs actual changes at time set with timeOn
// Called only by schedule set in incrementalSchedule
def runDailyStartSchedule(){
    if(state.disable) return
    if(!timeOn)

    // Set time state variables
    if(!setTime()) return
    setDailySchedules("start")

    // If not correct day, exit
    if(timeDays && !parent.todayInDayList(timeDays,app.label)) return

    // If not correct mode, reschuexit
    if(ifMode && location.mode != ifMode) return

    // Set start mode
    if(modeChangeOn) setLocationMode(modeChangeOn)

    if(timeOn) setStateMulti(timeOn,timeDevice)
    return true
}

// Performs actual changes at time set with timeOn
// Called only by schedule set in incrementalSchedule
def runDailyStopSchedule(){
    if(state.disable) return

    // Set time state variables
    if(!setTime()) return
    setDailySchedules("stop")

    // Do not test for day when stopping
    // if(timeDays && !parent.todayInDayList(timeDays,app.label)) return

    // If not correct mode, reschuexit
    if(ifMode && location.mode != ifMode) return

    // Set stop mode
    if(modeChangeOff && data.action == "stop") setLocationMode(modeChangeOff)

    if(timeOff){
        setStateMulti(timeOff,timeDevice)
        // If ending the schedule, then need to set off levels since they won't be captured
        // in the getDefaults routine
        if(levelOff || tempOff || hueOff || satOff){
            timeDevice.each{
                if(isOn(it)){
                    defaults = [:]
                    if(levelOff) defaults.put("level",levelOff)
                    if(tempOff) defaults.put("temp",tempOff)
                    if(hueOff) defaults.put("hue",hueOff)
                    if(satOff) defaults.put("sat",satOff)
                    // If there's a starting level, temp, hue or sat, this will cause levels to
                    // set a second time, but... it's once a day. Think it's better safe than sorry.
                    parent.setSingleLevel(defaults, it,app.label)
                }
            }
        }
    }
    return true
}

// Returns array for level, temp, hue and sat
// Should NOT return true or false; always return defaults array
// Array should return text "Null" for null values
def getDefaultLevel(singleDevice, appLabel){
    // If no device match, exit
    def result = timeDevice.id.find { it == singleDevice.id}
    if(!result) return
    message = "Schedule has device $singleDevice"


    // If schedule isn't active, return null
    if(!getScheduleNotInactive()) {
        if(checkLog(a="debug")) putLog(1009,"$message but isn't active",a)
        return
    }

    // If schedule doesn't establish a "defualt", exit
    // Don't exit for no stop levels, unless it's not called from daily schedule
    if(!levelOn && !tempOn && !hueOn && !satOn) {
        if(checkLog(a="debug")) putLog(1016,"$message but has no levels",a)
        return
    }

    // If we need elapsed, then get it
    // (Start time and level alone do not establish a "default")
    if((levelOn && levelOff) || (tempOn && tempOff) || (hueOn && hueOff) || (satOn && satOff)){
        elapsedFraction = getElapsedFraction()

        if(!elapsedFraction) {
            if(checkLog(a="error")) putLog(1026,"Unable to calculate elapsed time with start \"$atomicState.start\" and stop \"$atomicState.stop\"",a)
            return
        }
    }
    if(checkLog(a="debug")) putLog(1030,message,a)
    // Initialize defaults map
    defaults = [:]

    if(levelOn && levelOff){
        if(levelOff > levelOn){
            defaults.put("level", (levelOff - levelOn) * elapsedFraction + levelOn as int)
        } else {
            defaults.put("level", levelOn - (levelOn - levelOff) * elapsedFraction as int)
        }
        // Just start level doesn't establish a "default"
        // However, if *this* schedule triggers through runDaily, then we need to capture start level
        // It does not do anything with "start" levels
    } else if(levelOn && atomicState.stop){
        defaults.put("level",levelOn)
    } else if(levelOn && !atomicState.stop && appLabel == app.label){
        defaults.put("level",levelOn)
    }

    if(tempOn && tempOff){
        if(tempOff > tempOn){
            defaults.put("temp", (tempOff - tempOn) * elapsedFraction + tempOn as int)
        } else {
            defaults.put("temp", tempOn - (tempOn - tempOff) * elapsedFraction as int)
        }
        // Just start level doesn't establish a "default"
        // However, if *this* schedule triggers through runDaily, then we need to capture start level
        // It does not do anything with "start" levels
    } else if(tempOn && atomicState.stop){
        defaults.put("temp",tempOn)
    } else if(tempOn && !atomicState.stop && appLabel == app.label){
        defaults.put("temp",tempOn)
    }

    if(hueOn && hueOff){
        // hueOn=25, hueOff=75, going 25, 26...74, 75
        if(hueOff > hueOn && hueDirection == "Forward"){
            defaults.put("hue", (hueOff - hueOn) * elapsedFraction + hueOn as int)
            // hueOn=25, hueOff=75, going 25, 24 ... 2, 1, 100, 99 ... 76, 75
        } else if(hueOff > hueOn && hueDirection == "Reverse"){
            defaults.put("hue", hueOn - (100 - hueOff + hueOn)  * elapsedFraction as int)
            if(defaults.hue < 1) defaults.put("hue", defaults.hue + 100)
            //hueOn=75, hueOff=25, going 75, 76, 77 ... 99, 100, 1, 2 ... 24, 25
        } else if(hueOff < hueOn && hueDirection == "Forward"){
            defaults.put("hue", (100 - hueOn + hueOff)  * elapsedFraction + hueOn as int)
            if(defaults.hue > 100) defaults = [hue: defaults.hue - 100]
            //hueOn=75, hueOff=25, going 75, 74 ... 26, 25
        } else if(hueOff < hueOn && hueDirection == "Reverse"){
            defaults.put("hue", hueOn - (hueOn - hueOff) * elapsedFraction as int)
        }
        // Just start level doesn't establish a "default"
        // However, if *this* schedule triggers through runDaily, then we need to capture start level
        // It does not do anything with "start" levels
    } else if(hueOn && atomicState.stop){
        defaults.put("hue",hueOn)
    } else if(hueOn && !atomicState.stop && appLabel == app.label){
        defaults.put("hue",hueOn)
    }

    if(satOn && satOff){
        if(satOff > satOn){
            defaults.put("sat", (satOff - satOn) * elapsedFraction + satOn as int)
        } else {
            defaults.put("sat", satOn - (satOn - satOff) * elapsedFraction as int)
        }
        // Just start level doesn't establish a "default"
        // However, if *this* schedule triggers through runDaily, then we need to capture start level
    } else if(satOn && atomicState.stop){
        defaults.put("sat",satOn)
    } else if(satOn && !atomicState.stop && appLabel == app.label){
        defaults.put("sat",satOn)
    }

    // Avoid returning an empty set
    if(!defaults.level && !defaults.temp && !defaults.sat && !defaults.hue) {
        if(checkLog(a="error")) putLog(1105,"getDefaultLevel failed to capture default levels",a)
        return
    }

    return defaults
}

// Captures device state changes to set levels and start incremental schedule
// Needs to find whether an app triggered it; if so, app may want to override schedule
def handleStateChange(event){
    // This function should be the same as "resume" portion of setStateMulti, except
    // no override levels, and not turning off if no level
    // If an app requested the state change, then exit
    if(parent.getStateRequest(event.device,app.label)) {
        if(checkLog(a="debug")) putLog(1119,"Device state $event.device changed by an app; exiting handleStateChange",a)
        return
    } else {
        if(checkLog(a="debug")) putLog(1122,"Device $event.device turned on outside of app; caught by handleStateChange",a)
    }

    // If defaults, then there's an active schedule
    // So use it for if overriding/reenabling
    defaults = parent.getScheduleDefaultSingle(event.device,app.label)

    // Set default levels, for level and temp, if no scheduled defaults
    defaults = parent.getDefaultSingle(defaults,app.label)

    // Set default level
    parent.setLevelSingle(defaults,event.device,app.label)
    if(checkLog(a="debug")) putLog(1134,"Set levels $defaults for $event.device, which was turned on outside of the app",a)

    // if toggling on, reschedule incremental
    parent.rescheduleIncrementalSingle(event.device,app.label)
    
    return
}

def setTime(){
    if(setStartStopTime("start")) {
    setStartStopTime("stop") 
    if(!atomicState.totalSeconds || (inputStartType == "sunrise" || inputStartType == "sunset" || inputStopType == "sunrise" || inputStopType == "sunset"))
    setTotalSeconds()
    returnValue = true
    }
    return returnValue
}

// Sets atomicState.start and atomicState.stop variables
// Requires type value of "start" or "stop" (must be capitalized to match setting variables)
def setStartStopTime(type){
    if(type != "start" && type != "stop") {
        if(checkLog(a="error")) putLog(1156,"Invalid value for type \"$type\" sent to setStartStopTime function",a)
        return
    }

    // Change to uppercase to match input strings (eg "inputStartTime")
    /*
if(type == "start") {
atomicState.start = null
type = "Start"
} else if(type == "stop") {
atomicState.stop = null
type = "Stop"
}
*/
    type = type.capitalize()

    // If no stop time, exit
    if(type == "Stop" && (!inputStopType || inputStopType == "none")) return true

    if(settings["input${type}Type"] == "time"){
        value = settings["input${type}Time"]
    } else if(settings["input${type}Type"] == "sunrise"){
        value = (settings["input${type}SunriseType"] == "before" ? parent.getSunrise(settings["input${type}Before"] * -1,app.label) : parent.getSunrise(settings["input${type}Before"],app.label))
    } else if(settings["input${type}Type"] == "sunset"){
        value = (settings["input${type}SunriseType"] == "before" ? parent.getSunset(settings["input${type}Before"] * -1,app.label) : parent.getSunset(settings["input${type}Before"],app.label))
    } else {
        if(checkLog(a="error")) putLog(1182,"input" + type + "Type set to " + settings["input${type}Type"],a)
        return
    }

    if(type == "Stop"){
        if(timeToday(atomicState.start, location.timeZone).time > timeToday(value, location.timeZone).time) value = parent.getTomorrow(value,app.label)
    }

    if(type == "Start") atomicState.start = value
    if(type == "Stop") atomicState.stop = value
    
    return true
}

// Converts full text weekdays as string to be used by cron schedule, and sets as state.weekDays
// Only called from initialize
def setWeekDays(){
    state.weekDays = null
    if(!timeDays) return
    dayString = ""
    timeDays.each{
        if(it == "Monday") dayString += "MON"
        if(it == "Tuesday") {
            if(dayString) dayString += ","
            dayString += "TUE"
        }
        if(it == "Wednesday") {
            if(dayString) dayString += ","
            dayString += "WED"
        }
        if(it == "Thursday") {
            if(dayString) dayString += ","
            dayString += "THU"
        }
        if(it == "Friday") {
            if(dayString) dayString += ","
            dayString += "FRI"
        }
        if(it == "Saturday") {
            if(dayString) dayString += ","
            dayString += "SAT"
        }
        if(it == "Sunday") {
            if(dayString) dayString += ","
            dayString += "SUN"
        }
    }
    state.weekDays = dayString
    return true
}

// Calculates duration of the schedule in seconds, and sets as atomicState.totalSeconds
// Only called by initialize
def setTotalSeconds(){
    if(!atomicState.start || !atomicState.stop) {
        atomicState.totalSeconds = null
        return false
    }

    // Calculate duration of schedule
    atomicState.totalSeconds = Math.floor((Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.stop).time - Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.start).time) / 1000)
    return true
}

// Returns percentage of schedule that has elapsed
// Only called by getDefaultLevel
def getElapsedFraction(){
    // If not between start and stop time, exit
    if(!atomicState.stop || !parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return false

    elapsedSeconds = Math.floor((new Date().time - Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.start).time) / 1000)
    if(checkLog(a="debug")) putLog(1253,"$elapsedSeconds seconds of the schedule has elpased.",a)
    //Divide for percentage of time expired (avoid div/0 error)
    if(elapsedSeconds < 1){
        elapsedFraction = 1 / atomicState.totalSeconds * 1000 / 1000
    } else {
        elapsedFraction = elapsedSeconds / atomicState.totalSeconds * 1000 / 1000
    }

    if(elapsedFraction > 1) {
        if(checkLog(a="error")) putLog(1262,"Over 100% of the schedule has elapsed, so start or stop time hasn't updated correctly.",a)
        return
    }

    return elapsedFraction
}

// Used by Master to check whether to reschedule incremental
// type = "start" or "stop"
def getTimeVariable(){
    time = [atomicState.start,atomicState.stop]
    return time
}

// Returns true is schedule is not inactive, and allows for no stop time
// Used by getDefaultLevel
def getScheduleNotInactive(){
    // If disabled, return false
    if(state.disable) return

    // If not correct day, return false
    if(timeDays && !parent.todayInDayList(timeDays,app.label)) return

    // If mode isn't correct, return false
    if(ifMode && location.mode != ifMode) return

    // If no start or stop time, return false
    if(!atomicState.start) return

    // If no start levels, return false
    if(!levelOn && !tempOn && !hueOn && !satOn) return false

    // If not between start and stop time, return false
    if(atomicState.stop && !parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return

    return true
}

// Returns true if schedule is active, with stop time
// Used by incremental schedule
def getScheduleActive(){
    if(getScheduleNotInactive() && atomicState.stop) return true
    return
}

/* ************************************************************************ */
/*                                                                          */
/*                  Begin (mostly) universal functions.                     */
/*                 Most or all could be moved to Master.                    */
/*                                                                          */
/* ************************************************************************ */

// If deviceChange exists, adds deviceId to it; otherwise, creates deviceChange with deviceId
// Delineate values with colons on each side - must match getStateDeviceChange
// Used to track if app turned on device when schedule captures a device state changing to on
// Must be included in all apps using MultiOn
def addDeviceStateChange(singleDeviceId){
    if(atomicState.deviceChange) {
        atomicState.deviceChange += ":$singleDeviceId:"
    } else {
        atomicState.deviceChange = ":$singleDeviceId:"
    }
    return
}

// Returns the value of deviceChange
// Used by schedule when a device state changes to on, to check if an app did it
// It should only persist as long as it takes for the scheduler to capture and
// process both state change request and state change subscription
// Function must be in every app
def getStateDeviceChange(singleDeviceId){
    if(atomicState.deviceChange){
        value = atomicState.deviceChange.contains(":$singleDeviceId:") 
        // Reset it when it's used, to try and avoid race conditions with multiple fast button clicks
        resetStateDeviceChange()
        // Allows for using either .contains or .indexof; if .contains works, remove != -1
    }
}

// Scheduled funtion to reset the value of deviceChange
// Must be in every app using MultiOn
def resetStateDeviceChange(){
    atomicState.deviceChange = null
    return
}

// Schedules don't use resume
// This is a bit of a mess, but.... 
def setStateMulti(deviceAction,device,appAction = null){
    if(!deviceAction || (deviceAction != "on" && deviceAction != "off" && deviceAction != "toggle" && deviceAction != "none")) {
        if(checkLog(a="error")) putLog(1352,"Invalid deviceAction \"$deviceAction\" sent to setStateMulti",a)
        return
    }

    // Time in which to allow Hubitat to process sensor change (eg Pico, contact, etc.)
    // as well as the scheduler to process any state change generated by the sensor, after
    // which the requesting child-app will "forget" it's the one to have requested any
    // level changes and the schedule not see a state change was from child-app.
    // What's a realistic number to use if someone has a lot of devices attached to a lot 
    // of Picos with a lot of schedules? Probably could be as low as 100 or 250.
    stateDeviceChangeResetMillis = 1000

    if(deviceAction == "off"){
        // Reset device change, since we know the last event from this device didn't turn anything on
        resetStateDeviceChange()
        // Turn off devices
        parent.setStateMulti("off",device,app.label)
        returnValue = true
    } else if(deviceAction == "on"){
        // Get list of all devices to be turned on (for schedule overriding)
        device.each{
            // Add device ids to deviceChange, so schedule knows it was turned on by an app
            // Needs to be done before turning the device on.
            addDeviceStateChange(it.id)
        }

        // Turn on devices
        parent.setStateMulti("on",device,app.label)

        // Then set the levels
        device.each{
            // Set scheduled levels, default levels, and/or [this child-app's] levels
            parent.getAndSetSingleLevels(it,appAction,app.label)
        }
        if(checkLog(a="debug")) putLog(1386,"Device id's turned on are $atomicState.deviceChange",a)
        // Schedule deviceChange reset
        runInMillis(stateDeviceChangeResetMillis,resetStateDeviceChange)
        returnValue = true
    } else if(deviceAction == "toggle"){
        // Create toggleOnDevice list, used to track which devices are being toggled on
        toggleOnDevice = []
        // Set count variable, used for toggleOnDevice
        count = 0
        device.each{
            // Start count at 1; doesn't matter, so long as it matches newCount below
            count = count + 1
            // If toggling to off
            if(parent.isOn(it)){
                parent.setStateSingle("off",it,app.label)
                // Else if toggling on
            } else {
                // When turning on, add device ids to deviceChange, so schedule knows it was turned on by an app
                // Needs to be done before turning the device on.
                addDeviceStateChange(it.id)
                // Turn the device on
                parent.setStateSingle("on",it,app.label)
                // Add device to toggleOnDevice list so when we loop again to set levels, we know whether we
                // just turned it on or not (without knowing how long the device may take to respond)
                toggleOnDevice.add(count)
            }
        }
        if(checkLog(a="debug")) putLog(1413,"Device id's toggled on are $atomicState.deviceChange",a)
        // Create newCount variable, which is compared to the [old]count variable
        // Used to identify which lights were turned on in the last loop
        newCount = 0
        device.each{
            // Start newCount at 1 like count above
            newCount = newCount + 1
            // If turning on, set scheduled levels, default levels, and/or [this child-app's] levels
            // If newCount is contained in the list of [old]count, then we toggled on
            if(toggleOnDevice.contains(newCount)){
                parent.getAndSetSingleLevels(it,appAction,app.label)
            }
        }
        // Schedule deviceChange reset
        runInMillis(stateDeviceChangeResetMillis,resetStateDeviceChange)
        returnValue = true
    } else if(deviceAction == "resume"){
        // Reset device change, since we know the last event from this device didn't turn anything on
        resetStateDeviceChange()
        device.each{
            // If defaults, then there's an active schedule
            // So use it for if overriding/reenabling
            defaults = parent.getScheduleDefaultSingle(it,app.label)
            logMessage = defaults ? "$singleDevice scheduled for $defaults" : "$singleDevice has no scheduled default levels"

            // If there are defaults, then there's an active schedule so reschedule it (the results are corrupted below).
            // We could do this for the matching schedules within its own getDefaultLevel(), but that would
            // probably result in incremental schedules rescheduling themselves over and over again. And if we
            // excluded schedules from rescheduling, then daily schedules wouldn't do this.
            if(defaults) parent.rescheduleIncrementalSingle(it,app.label)

            defaults = parent.getOverrideLevels(defaults,appAction, app.label)
            logMessage += defaults ? ", controller overrides of $defaults": ", no controller overrides"

            // Skipping getting overall defaults, since we're resuming a schedule or exiting;
            // rather keep things the same level rather than an arbitrary default, and
            // if we got default, we'd not turn it off

            if(defaults){
                if(checkLog(a="debug")) putLog(1452,logMessage,a)
                parent.setLevelSingle(defaults,it,app.label)
                // Set default level
            } else {
                parent.setStateSingle("off",it,app.label)
            }
        }
        returnValue = true
    } else if(deviceAction == "none"){
        // Reset device change, since we know the last event from this device didn't turn anything on
        resetStateDeviceChange()
        // If doing nothing, reschedule incremental changes (to reset any overriding of schedules)
        // I think this is the only place we use ...Multi, prolly not enough to justify a separate function
        parent.rescheduleIncrementalMulti(device,app.label)
        returnValue = true
    }
    return returnValue
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
        case "debug":
        if(state.logLevel == 5) return "debug"
        break
        case "trace":
        if(state.logLevel > 3) return "trace"
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,message = null,type = "trace"){
    logMessage = ""
    if(type == "error") logMessage += "<font color=\"red\">"
    if(type == "warn") logMessage += "<font color=\"brown\">"
    logMessage += "$app.label "
    if(lineNumber) logMessage += "(line $lineNumber) "
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
        case "debug":
        log.debug(logMessage)
        return true
        case "trace":
        log.trace(logMessage)
        return true
    }
    return
}
