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
*  Version: 0.6.02
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
    return 5
}

preferences {
    infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
    errorIcon = "<img src=\"http://emily-john.love/icons/error.png\" width=20 height=20>"
    warningIcon = "<img src=\"http://emily-john.love/icons/warning.png\" width=20 height=20>"
    moreOptions = " <font color=\"gray\">(more options)</font>"

    // If we're missing a value, don't allow save
    if((!app.label) ||
       (!timeDevice) ||
       (!timeOn || !inputStartType) ||
       !inputStopType || 
       (inputStartType == "time" && !inputStartTime) ||
       (inputStopType == "time" && !inputStopTime) ||
       ((inputStartType == "sunrise" || inputStartType == "sunset") && !inputStartSunriseType) ||
       ((inputStopType == "sunrise" || inputStopType == "sunset") && !inputStopSunriseType) ||
       ((inputStartSunriseType == "before" || inputStartSunriseType == "after") && !inputStartBefore) ||
       ((inputStopSunriseType == "before" || inputStopSunriseType == "after") && !inputStopBefore) ||
       (hueOn && hueOff && !hueDirection) ||
       (levelOn > 100 || levelOff >100 || (tempOn && tempOn < 1800) || tempOn > 5400 || (tempOff && tempOff < 1800) || tempOff > 5400 || hueOn > 100 || hueOff > 100 || satOn > 100 || satOff > 100)) noInstall = true

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
                if(checkTimeComplete("stop")){
                    displayTimeDaysOption()
                    
                    displayBrightnessOption()
                    displayTemperatureOption()
                    displayColorOption()
                    
                    displayChangeModeOption()
                    displayIfModeOption()
                    displayIfPeopleOption()
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
        
        sectionTitle = "<b>Start: "
        if(settings["timeOn"] == "on"){
            sectionTitle += "Turn on"
        } else if(settings["timeOn"] == "off"){
            sectionTitle += "Turn off"
        } else if(settings["timeOn"] == "toggle"){
            sectionTitle += "Toggle"
        }
        sectionTitle += " " + varStartTime + "</b>"
        
        section(hideable: true, hidden: true, sectionTitle){
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
    return
}

def displayStopTimeSection(){
    // If all options entered
    if(checkTimeComplete("stop")){
        varStopTime = getTimeVariables("stop")
        sectionTitle = "<b>End: "
        if(settings["inputStopType"] == "none"){
            sectionTitle = "No end"
        } else {
            if(settings["timeOff"] == "on"){
                sectionTitle += "Turn on"
            } else if(settings["timeOff"] == "off"){
                sectionTitle += "Turn off"
            } else if(settings["timeOff"] == "toggle"){
                sectionTitle += "Toggle"
            }
            sectionTitle += " " + varStopTime + "</b>"
        }

        section(hideable: true, hidden: true, sectionTitle){
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
return
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
    return
}

def displayStopTypeOption(){
    if(!checkTimeComplete("stop")){
        displayLabel("Schedule ending time")
    } else {
        displayLabel("Schedule end")
    }
    if(!settings["inputStopType"]){
        width = 12
        input "inputStopType", "enum", title: "End time (click to choose option):", multiple: false, width: width, options: ["none":"No end", "time":"End at specific time", "sunrise":"Sunrise (at, before or after)","sunset":"Sunset (at, before or after)" ], submitOnChange:true
        displayInfo("Select \"No end\" only if you only want to $message $varStartTime. Select to enter an end time or use sunrise/sunset if you want to:")
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
        input "inputStopType", "enum", title: "End time option:", multiple: false, width: width, options: ["none":"No end", "time":"End at specific time", "sunrise":"Sunrise (at, before or after)","sunset":"Sunset (at, before or after)" ], submitOnChange:true
    }
    return
}

def displayTimeOption(lcType){
    ucType = lcType.capitalize()
    input "input${ucType}Time", "time", title: "$ucType time:", width: width, submitOnChange:true
    if(!settings["input${ucType}Time"]) displayInfo("Enter the time to $lcType the schedule in \"hh:mm AM/PM\" format. Required field.")
    return
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
    return
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
    if(!settings[varName]) displayInfo("Set whether to turn on or off, or toggle Device(s), when ${lcType}ing the schedule. Select \"Don't\" to not have it turn on, turn off, or toggle. Toggle turns on devices that are off, and turns off devices that are on. Required field.")
    return
}

def displayTimeDaysOption(){
    if(settings["timeDays"]){
        sectionText = "<b>Only on: $settings.timeDays</b>"
        helpTip = "This will limit the schedule from running unless it's $settings.timeDays."
    } else {
        sectionText = "Click to select on which days (optional)"
        helpTip = "Select which day(s) on which to schedule. Applies to both starting and ending of the schedule. If none are selected, schedule will default to every day."
    }

    section(hideable: true, hidden: true, sectionText){
        input "timeDays", "enum", title: "On these days (defaults to all days)", multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"], submitOnChange:true
        displayInfo(helpTip)
    }
    return
}

def displayIfModeOption(){
    if(settings["ifMode"]){
        sectionText = "<b>Only with Mode: $settings.ifMode</b>"
        helpTip = "This will limit the schedule from running to only when Hubitat's Mode is $settings.ifMode."
    } else {
        sectionText = "Click to select with what Mode (optional)"
        helpTip = "This will limit the schedule from running to only when Hubitat's Mode is as selected."
    }

    section(hideable: true, hidden: true, sectionText){
        input "ifMode", "mode", title: "Only run if Mode is already?", width: 12, submitOnChange:true
        displayInfo(helpTip)
    }
    return
}

def displayIfPeopleOption(){
    if(!parent.getPresenceDevice(app.label)) return

    if(settings["ifPeople"]){
        sectionTitle = "<b>Only with people: $settings.ifPeople</b>"
        helpTip = "This will limit the schedule from running to only when $settings.ifPeople is home. Presence devices are set in the Master app."
    } else {
        sectionTitle = "Click to select with which people (optional)"
        helpTip = "This will limit the schedule from running to only when everyone is home or not. Presence devices are set in the Master app."
    }
    section(hideable: true, hidden: true, sectionTitle){
        input "ifPeople", "enum", title: "Only run if people are present?", width: 12, options: ["everyone":"Everyone present","noone":"Noone present"],submitOnChange:true

        displayInfo(helpTip)
    }
    return
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
        sectionTitle = "Click to schedule brightness (optional)"
    } else {
        sectionTitle = "<b>"
        // If just start level
        if(settings["levelOn"] && !settings["levelOff"]) {
            sectionTitle += "Start: Brightness $settings.levelOn%</b>"
            if(settings["inputStopType"] != "none") sectionTitle += moreOptions
        // Both start and stop levels
        } else if(settings["levelOn"] && settings["levelOff"]) {
            sectionTitle += "Start to end: Change brightness from $settings.levelOn% to $settings.levelOff%</b>"
        // Just stop level
        } else if(!settings["levelOn"] && settings["levelOff"]){
            sectionTitle += "End: Brightness $settings.levelOff%</b>"
            if(settings["timeOn"] != "off") sectionTitle += moreOptions
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
            displayLabel("Enter starting and/or ending brightness")
            input "levelOn", "number", title: "Starting brightness ($varStartTime)?", width: 6, submitOnChange:true
            input "levelOff", "number", title: "and ending brightness ($varStopTime)?", width: 6, submitOnChange:true
            if(!settings["levelOn"] && !settings["levelOff"]) {
                displayInfo("Enter the percentage of brightness when turning on, from 1 to 100, when starting and/or ending the schedule. If entering both starting and ending brightness, it will transition from starting to ending brightness for the duration of the schedule. Either starting or ending brightness is required (or unselect \"Change brightness\").")
            } else if(!settings["levelOn"] || !settings["levelOff"]){
                displayInfo("Enter the percentage of brightness when turning on, from 1 to 100. If entering both starting and ending brightness, it will transition from starting to ending brightness for the duration of the schedule.")
            } else {
                displayInfo("Brightness is percentage from 1 to 100.")
            }
        }

    if(!settings["levelOn"] && settings["levelOff"] && settings["timeOff"] == "off") warningMessage("With no starting brightness while setting Device(s) to turn off, setting an ending brightness won't do anything.")
    if(settings["levelOn"] && settings["levelOn"] == settings["levelOff"]) warningMessage("Starting and ending brightness are both set to $settings['levelOn']. This won't hurt anything, but the Stop brightness setting won't actually <i>do</i> anything.")
    if(settings["levelOn"] > 100) errorMessage("Brightness is percentage from 1 to 100. Correct starting brightness.")
    if(settings["levelOff"] > 100) errorMessage("Brightness is percentage from 1 to 100. Correct ending brightness.")
    }
}

def displayTemperatureOption(){
    hidden = true
    // If just start or stop level, then don't hide
    if((settings["tempOn"] && !settings["tempOff"] && settings["inputStopType"] != "none") || (settings["tempOff"] && !settings["tempOn"])) hidden = false
 
    if(!settings["tempOn"] && (!settings["tempOff"] || settings["inputStopType"] == "none")){
        sectionTitle = "Click to schedule temperature color (optional)"
    } else {
        // If just start level
        sectionTitle = "<b>"
        if(settings["tempOn"] && !settings["tempOff"]) {
            sectionTitle += "Start: Temperature color " + settings["tempOn"] + "K</b>"
        // Both start and stop levels
        } else if(settings["tempOn"] && settings["tempOff"]) {
            sectionTitle += "Start to end: Change temperature color from " + settings["tempOn"] + "K to " + settings["tempOff"] + "K</b>"
        // Just stop level
        } else if(settings["tempOff"]){
            sectionTitle += "End: Temperature color " + settings["tempOff"] + "K</b>"
            if(settings["timeOn"] != "off") sectionTitle += moreOptions
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
            displayLabel("Enter starting and/or ending temperature color")
            input "tempOn", "number", title: "Starting temperature color?", width: 6, submitOnChange:true
            input "tempOff", "number", title: "and ending temperature color?", width: 6, submitOnChange:true
            if(!settings["tempOn"] && !settings["tempOff"]) {
                displayInfo("Temperature color is in Kelvin from 1800 to 5400, when starting and/or ending the schedule. Lower values have more red, while higher values have more blue, where daylight is 5000, cool white is 4000, and warm white is 3000. If entering both starting and ending temperature, it will transition from starting to ending temperature for the duration of the schedule. Either starting or ending temperature is required (or unselect \"Change temperature\").")
            } else if(!settings["tempOn"] || !settings["tempOff"]){
                displayInfo("Temperature color is from 1800 to 5400, where daylight is 5000, cool white is 4000, and warm white is 3000. If entering both starting and ending temperature, it will transition from starting to ending temperature for the duration of the schedule.")
            } else {
                displayInfo("Temperature color is from 1800 to 5400, where daylight is 5000, cool white is 4000, and warm white is 3000.")
            }
        }

    }
    if((settings["tempOn"] && (settings["hueOn"] || settings["satOn"])) || (settings["tempOff"] && (settings["hueOff"] || settings["satOff"]))) warningMessage("Temperature color and hue/saturation color are conflicting. Hue/saturation will take precedence over temperature color.")
    if(!settings["tempOn"] && settings["tempOff"] && settings["tempOff"]== "off") warningMessage("With no starting color temperature while setting Device(s) to turn off, setting an ending temperature color won't do anything.")
    if(settings["tempOn"] && settings["tempOn"] == settings["tempOff"]) warningMessage("Starting and ending color temperature are both set to $settings.tempOn. This won't hurt anything, but the end color temperature setting won't actually <i>do</i> anything.")
    if(settings["tempOn"] && (settings["tempOn"] < 1800 || settings["tempOn"] > 5400)) errorMessage("Temperature color is from 1800 to 5400, where daylight is 5000, warm white is 3000, and cool white is 4000. Correct starting temperature.")
    if(settings["tempOff"] && (settings["tempOff"] < 1800 || settings["tempOff"] > 5400)) errorMessage("Temperature color is from 1800 to 5400, where daylight is 5000, warm white is 3000, and cool white is 4000. Correct ending temperature.")
}

def displayColorOption(){
    hiRezHue = parent.getHiRezHue(settings["timeDevice"])
    unit = hiRezHue ? "°" : "%"

// allow for hiRezHue?
    hidden = false
    // If nothing entered, hide
    if(!settings["hueOn"] && !settings["hueOff"]  && !settings["satOn"] && !settings["satOff"]) hidden = true
    // If everything enetered, hide
    if(settings["hueOn"] && ((settings["hueOff"] && settings["hueDirection"]) || settings["inputStopType"] == "none") && settings["satOn"] && (settings["satOff"] || settings["inputStopType"] == "none")) hidden = true
    
    if(!settings["hueOn"] && !settings["satOn"] && ((!settings["hueOff"] && !settings["satOff"]) || settings["inputStopType"] == "none")){
        hueTitle = "Click to schedule color (hue and/or saturation) (optional)"
    } else {
        
        // If just start level
        if(settings["hueOn"] && !settings["hueOff"]) {
            hueTitle = "<b>Start: Hue " + settings["hueOn"] + "%</b>"
            // Both start and stop levels
        } else if(settings["hueOn"] && settings["hueOff"]) {
            if(settings["hueDirection"] == "reverse") {
                    hueTitle = "<b>Start to end: Change hue from " + settings["hueOn"] + "$unit to " + settings["hueOff"] + "$unit</b>"
            } else {
                
            hueTitle = "<b>Start to end: Change hue from " + settings["hueOn"] + "$unit to " + settings["hueOff"] + "$unit</b>"
            }
            if(settings["hueDirection"] == "reverse") hueTitle += " (in reverse order)"
            // Just stop level
        } else if(settings["hueOff"]){
            hueTitle = "<b>End: Hue " + settings["hueOff"] + "$unit</b>"
            if(settings["timeOn"] != "off") hueTitle += moreOptions
        }
        
        // If just start level
        if(settings["satOn"] && (!settings["satOff"] && settings["inputStopType"] != "none")) {
            satTitle = "<b>Start: Saturation " + settings["satOn"] + "%</b>"
            // Both start and stop levels
        } else if(settings["satOn"] && settings["satOff"]) {
            satTitle = "<b>Start to end: Change saturation from " + settings["satOn"] + "% to " + settings["satOff"] + "%</b>"
            // Just stop level
        } else if(settings["satOff"]){
            satTitle = "<b>End: Saturation " + settings["satOff"] + "%</b>"
            if(settings["timeOn"] != "off") satTitle += moreOptions
        }
    }
    if(hueTitle && satTitle){
        sectionTitle =hueTitle + "<br>" + satTitle
    } else if(hueTitle){
        sectionTitle = hueTitle
    } else if(satTitle){
        sectionTitle = satTitle
    }
    
    section(hideable: true, hidden: hidden, sectionTitle){
        if(settings["inputStopType"] == "none"){
            //Should allow setting level, temp and color when schedule ends, even if not turning the device on or off
            if(settings["timeOn"] != "off"){
                displayLabel("Enter default hue and/or color saturation")
                input "hueOn", "number", title: "Set hue?", width: 6, submitOnChange:true
                input "satOn", "number", title: "Set color saturation?", width: 6, submitOnChange:true
                if(hiRezHue){
                    displayInfo("Hue is the shade of color, from 1 to 360, when starting and/or ending the schedule. Red is 1 or 360, yellow is 60, green is 120, blue is 240, and purple is 270.")
                } else {
                    displayInfo("Hue is the shade of color, from 1 to 360, when starting and/or ending the schedule. Red is 1 or 100, yellow is 16, green is 33, blue is 66, and purple is 73.")
                }
                displayInfo("Saturation is the percentage depth of color, from 1 to 100, where 1 is hardly any color tint and 100 is full color..")
            }
        } else {
            if(settings["timeOn"] == "off"){
              displayLabel("Enter ending hue")
            } else {
              displayLabel("Enter starting and/or ending hue")
            }
            if(settings["hueOn"] && settings["hueOff"]){
                if(settings["hueOn"] < settings["hueOff"]){
                    if(hiRezHue){
                    forwardSequence = "90, 91, 92  ... 270, 271, 272"
                    reverseSequence = "90, 89, 88 ... 2, 1, 360, 359 ... 270, 269, 268"
                    } else {
                    forwardSequence = "25, 26, 27  ... 73, 74, 75"
                    reverseSequence = "25, 24, 23 ... 2, 1, 100, 99 ... 77, 76, 75"
                    }
                } else {
                    if(hiRezHue){
                    forwardSequence = "270, 271, 272 ... 359, 360, 1, 2 ... 90, 91, 92"
                    reverseSequence = "270, 269, 268 ... 75, 74, 73"
                    } else {
                    forwardSequence = "75, 76, 77 ... 99, 100, 1, 2 ... 23, 24, 25"
                    reverseSequence = "75, 74, 73 ... 27, 26, 25"
                    }
                }
                input "hueOn", "number", title: "Starting hue?", width: 4, submitOnChange:true
                input "hueOff", "number", title: "and ending hue?", width: 4, submitOnChange:true
                input "hueDirection", "enum", title: "Which order to change hue?", width: 4, submitOnChange:true, options: ["forward": forwardSequence, "reverse": reverseSequence]
            } else {
                if(settings["timeOn"] != "off") input "hueOn", "number", title: "Starting hue?", width: 6, submitOnChange:true
                input "hueOff", "number", title: "and ending hue?", width: 6, submitOnChange:true
            }
            if(hueOn && hueOff && hueDirection){
               if(hiRezHue){
                   message = "Red = 0 hue (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270 (may vary by device). Optional."
               } else {
                   message = "Red = 0 hue (and 100). Orange = 8; yellow = 16; green = 33; turquiose = 50; blue = 66; purple = 79 (may vary by device). Optional."
               }
            } else if(hueOn && hueOff && !hueDirection){
               if(hiRezHue){
                message = "Red = 0 hue (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270 (may vary by device). It will transition from starting to ending temperature for the duration of the schedule. For \"direction\", if for instances, a start value of 1 and end value of 26 is entered, allows for chosing whether it would change from red to yellow then blue, or from red to purple, blue, then green. Optional."
               } else {
                message = "Red = 0 hue (and 100). Orange = 8; yellow = 16; green = 33; turquiose = 50; blue = 66; purple = 79 (may vary by device). It will transition from starting to ending temperature for the duration of the schedule. For \"direction\", if for instances, a start value of 1 and end value of 26 is entered, allows for chosing whether it would change from red to yellow then blue, or from red to purple, blue, then green. Optional."
               }
                   
            } else if(!hueOn || !hueOff){
               if(hiRezHue){
                message = "Hue is degrees around a color wheel, where red is 0 (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270 (may vary by device)."
               } else {
                message = "Hue is percent around a color wheel, where red is 0 (and 100). Orange = 8; yellow = 16; green = 33; turquiose = 50; blue = 66; purple = 79 (may vary by device)."
               }
                if(settings["timeOn"] != "off") message += " If entering both starting and ending hue, it will transition from starting to ending temperature for the duration of the schedule."
                message += " Optional."
            } else if(!hueOn && !hueOff){
               if(hiRezHue){
                message = "Hue is degrees from 0 to 360 around a color wheel, where red is 0 (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270 (may vary by device)."
               } else {
                message = "Hue is percent from 0 to 100 around a color wheel, where red is 0 (and 100). Orange = 8; yellow = 16; green = 33; turquiose = 50; blue = 66; purple = 79 (may vary by device)."
               }
                if(settings["timeOn"] != "off") message += " If entering both starting and ending hue, it will transition from starting to ending temperature for the duration of the schedule."
                message += " Optional."
            }
            displayInfo(message)

            displayLabel("Enter starting and/or color saturation")
            input "satOn", "number", title: "Starting color saturation?", width: 6, submitOnChange:true
            input "satOff", "number", title: "and ending color saturation?", width: 6, submitOnChange:true
            if(!satOn && !satOff){
                message = "Saturation is the percentage amount of color tint displayed, from 1 to 100, when starting and/or ending the schedule. 1 is hardly any color tint and 100 is full color."
                if(settings["timeOn"] != "off") message += " If entering both starting and ending saturation, it will transition from starting to ending saturation for the duration of the schedule."
                message += " Optional."
            } else if(!satOn || !satOff){
                message = "Saturation is the percentage amount of color tint displayed, from 1 to 100, where 1 is hardly any color tint and 100 is full color."
                if(settings["timeOn"] != "off") message += " If entering both starting and ending saturation, it will transition from starting to ending saturation for the duration of the schedule."
                message += " Optional."
            } else {
                message = "Saturation is the percentage amount of color tint displayed, from 1 to 100, where 1 is hardly any color tint and 100 is full color. Optional."
            }
            displayInfo(message)
        }
    }

    if(hueOn && hueOn == hueOff) warningMessage("Starting and ending hue are both set to $hueOn. This won't hurt anything, but the end hue setting won't actually <i>do</i> anything.")
    if(satOn && satOn == satOff) warningMessage("Starting and ending saturation are both set to $satOn. This won't hurt anything, but the end saturation setting won't actually <i>do</i> anything.")
    if(hueOn > 100 && !hiRezHue) errorMessage("Starting hue can't be more than 100. Correct before saving.")
    if(hueOn > 360 && hiRezHue) errorMessage("Starting hue can't be more than 360. Correct before saving.")
    if(hueOff && hueOff > 100 && !hiRezHue) errorMessage("Ending hue can't be more than 360. Correct before saving.")
    if(hueOff && hueOff > 360 && hiRezHue) errorMessage("Ending hue can't be more than 360. Correct before saving.")
    if(satOn > 100) errorMessage("Starting saturation can't be more than 100. Correct before saving.")
    if(satOff && satOff > 100) errorMessage("Ending saturation can't be more than 100. Correct before saving.")
}

def displayChangeModeOption(){
    hidden = true
    // If just start or stop level, then don't hide
    if((settings["modeChangeOn"] && !settings["modeChangeOff"] && settings["inputStopType"] != "none") || (!settings["modeChangeOn"] && settings["modeChangeOff"])) hidden = false
 
    if(!settings["modeChangeOn"] && (!settings["modeChangeOff"] || settings["inputStopType"] == "none")){
        sectionTitle = "Click to schedule changing Modes (optional)"
    } else {
        // If just start level
        sectionTitle = ""
        if(settings["modeChangeOn"] && (!settings["modeChangeOff"] && settings["inputStopType"] != "none")) {
            sectionTitle = "<b>Start: Set Mode " + settings["modeChangeOn"]
            if(settings["modeChangeOff"]) sectionTitle += "<br>"
        }
        if(settings["modeChangeOff"]){
            sectionTitle += "<b>End: Set Mode " + settings["modeChangeOff"]
        }
        sectionTitle += "</b>"
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
hueDirection - enum (forward, reverse) - "Direction" in which to change hue; only displays if hueOn and hueOff have values
satOn - number (1-100) - Sat to set at start time
satOff - number (1-100) - Sat to set at stop time
modeChangeOn - mode - Mode to set at start time
//modeChangeOff - mode - Mode to set at stop time [doesn't currently exist]
ifMode - mode - Mode system must have for schedule to run
ifPeople - enum (everyone, noone) - who must be present for schedule to run
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
    // Need to deal with dim and brighten too (and even hue/sat)

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
    subscribeDevices()

    setDailySchedules()
    
    //Initialize deviceState variable
    parent.isOnMulti(settings["timeDevice"])
    
    atomicState.defaults = null
    defaults = getStartDefaults()
    if(defaults){
        if(defaults."level") defaults."level"."time" = "start"
        if(defaults."temp") defaults."temp"."time" = "start"
        if(defaults."sat") defaults."sat"."time" = "start"
        if(defaults."hue") defaults."hue"."time" = "start"
        parent.updateLevelsMulti(settings["timeDevice"],defaults,app.label)
        if(getScheduleActive()){
            parent.setStateMulti(settings["timeDevice"],app.label)
            runIncrementalSchedule()
        }
    }
    return true
}

def handleStateChange(event){
    if(parent.isOn(event.device,app.label) && event.value == "on") return
    if(!parent.isOn(event.device,app.label) && event.value == "off") return
    putLog(942,"trace","Captured manual state change for $event.device to turn $event.value")
    parent.updateStateSingle(event.device,event.value,app.label)

    return
}

def handleLevelChange(event){
    if(!parent.isOn(event.device)) return
    value = parent.convertToString(event.value)

    levelChange = parent.getLastLevelChange(event.device, app.label)
    if(levelChange){
        // We do need a time of 400 ms, for rapid button pushes
        if(levelChange."timeDifference" < 400) return
        if(levelChange."currentLevel".toString() == event.value) return
        defaults = ["level":["startLevel":event.value,"appId":"manual"]]
        parent.updateLevelsSingle(event.device,defaults,app.label)
        putLog(959,"warn","Captured manual change for $event.device level to $event.value% - last changed " + levelChange."timeDifference" + "ms")
    }
    return
}

def handleTempChange(event){
    if(!parent.isOn(event.device)) return
    value = parent.convertToInteger(event.value)
    if(!value) return
    tempChange = parent.getLastTempChange(event.device, app.label)
    // Temp can be different by + .5% (25 at 5000); always plus, never minus
    if(tempChange){
        if(tempChange."timeDifference".toInteger() < 400) return
        maxValue = (tempChange."currentLevel" / 200 + tempChange."currentLevel").toInteger()
        if(value + 1 > tempChange."currentLevel" && value < maxValue && event.device.colorMode == "CT") return

        defaults = ["temp":["startLevel":value,"appId":"manual"]]
        putLog(976,"warn","Captured manual temperature change for $event.device to temperature color " + value + "K - last changed " + tempChange."timeDifference" + "ms")
        parent.updateLevelsSingle(event.device,defaults,app.label)
    }
    return
}

def handleHueChange(event){
    if(!parent.isOn(event.device))
    value = parent.convertToInteger(event.value)
    if(!value) return
    
    hueChange = parent.getLastHueChange(event.device, app.label)
    if(hueChange){
        if(hueChange."timeDifference" < 400) return
        //if(hueChange."currentLevel".toString().toInteger() == event.value.toString().toInteger() || (hueChange."priorLevel".toString().toInteger() == event.value.toString().toInteger() && hueChange."timeDifference" < 1500)) return
        if(hueChange."currentLevel" == value && event.device.colorMode == "RGB") return
        defaults = ["hue":["startLevel":value,"priorLevel":event.device.currentHue,"appId":"manual"]]
        putLog(993,"warn","Captured manual change for $event.device to hue $value% - last changed " + hueChange."timeDifference" + "ms")
        parent.updateLevelsSingle(event.device,defaults,app.label)
    }
    return
}

def handleSatChange(event){
    if(!parent.isOn(event.device)) return
    value = parent.convertToInteger(event.value)
    if(!value) return

    satChange = parent.getLastSatChange(event.device, app.label)
    if(satChange){
        if(satChange."timeDifference" < 400) return
        //if(satChange."currentLevel".toString().toInteger() == event.value.toString().toInteger() || ((satChange."priorLevel".toString().toInteger() == event.value.toString().toInteger() || satChange."priorLevel" + 1 == event.value.toString().toInteger() || satChange."priorLevel" - 1 == event.value.toString().toInteger()) && satChange."timeDifference" < 1500)) return
        if(satChange."currentLevel".toInteger() == value && event.device.colorMode == "RGB") return
        defaults = ["sat":["startLevel":value,"appId":"manual"]]
        putLog(1010,"warn","Captured manual change for $event.device to saturation $value% - last changed " + satChange."timeDifference" + "ms")
        parent.updateLevelsSingle(event.device,defaults,app.label)
    }
    return
}

def setDailySchedules(type = null){
    // Set start time, stop time, and total seconds
    if(!setTime()) return false
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
        //schedule(scheduleString, runDailyStartSchedule)
        // Need to pause or else Hubitat may run runDailyStartSchedule immediately and cause a loop
        runIn(1,setStartSchedule, [data: ["scheduleString": scheduleString]])
        putLog(1046,"debug","Scheduling runDailyStartSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.start).format("h:mma MMM dd, yyyy", location.timeZone) + " ($scheduleString)")
    }

    if((type != "start") && atomicState.stop){
        scheduleString = "0 $stopMinutes $stopHours $days"
        // Need to pause or else Hubitat may run runDailyStopSchedule immediately and cause a loop
        runIn(1,setStopSchedule, [data: ["scheduleString": scheduleString]])
        //schedule(scheduleString, runDailyStopSchedule)
        putLog(1054,"debug","Scheduling runDailyStopSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.stop).format("h:mma MMM dd, yyyy", location.timeZone) + " ($scheduleString)")
    }
    return true
}

// Required for offsetting scheduling start and stop by a second, to prevent Hubitat running runDailyStartSchedule immediately
def setStartSchedule(data){
    schedule(data.scheduleString, runDailyStartSchedule)
    putLog(1062,"debug","Scheduling runDailyStartSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.start).format("h:mma MMM dd, yyyy", location.timeZone) + " ($data.scheduleString)")
}

def setStopSchedule(data){
    schedule(data.scheduleString, runDailyStopSchedule)
    putLog(1067,"debug","Scheduling runDailyStopSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.stop).format("h:mma MMM dd, yyyy", location.timeZone) + " ($data.scheduleString)")
}

// Performs actual changes at time set with timeOn
// Called only by schedule set in incrementalSchedule
def runDailyStartSchedule(){
    if(state.disable) return
    
    atomicState.defaults = null
    
    // Set time state variables
    if(!setTime()) return
    
    setDailySchedules("start")

    // If not correct day, exit
    if(settings["timeDays"] && !parent.todayInDayList(settings["timeDays"],app.label)) return
    
    if(settings["timeOn"] == "on" || settings["timeOn"] == "off" || settings["timeOn"] == "toggle") {
        parent.updateStateMulti(settings["timeDevice"],settings["timeOn"],app.label)
    }
    if(settings["levelOn"] || settings["tempOn"] || settings["hueOn"] || settings["satOn"]){
        defaults = getStartDefaults()
        if(defaults."level") defaults."level"."time" = "start"
        if(defaults."temp") defaults."temp"."time" = "start"
        if(defaults."sat") defaults."sat"."time" = "start"
        if(defaults."hue") defaults."hue"."time" = "start"
        parent.updateLevelsMulti(settings["timeDevice"],defaults,app.label)
    }
    
    // If not correct mode, exit
    if(settings["ifMode"] && location.mode != settings["ifMode"]) return
    
    if(settings["ifPeople"] && parent.getEveryonePresent(settings["ifPeople"], app.label)) return
    
    // Update and set state (when turning on, off, or toggling)
        parent.setStateMulti(settings["timeDevice"],app.label)

    // Set start mode
    if(settings["modeChangeOn"]) setLocationMode(settings["modeChangeOn"])

    runIn(15,runIncrementalSchedule)

    return
}

// Performs actual changes for incremental schedule
// Called only by schedule set in incrementalSchedule
def runIncrementalSchedule(){
    putLog(1116,"trace","runIncrementalSchedule starting")
    if(!getScheduleActive()) return
    if((settings["levelOn"] && settings["levelOff"]) || (settings["tempOn"] && settings["tempOff"]) || (settings["hueOn"] && settings["hueOff"]) || (settings["satOn"] && settings["satOff"])) {
        // If it's disabled, keep it active
        // Master will only update when appropriate
        if(state.disable) return
        // If mode isn't correct, return false
        if(settings["ifMode"] && location.mode != settings["ifMode"]) {
            runIn(30,runIncrementalSchedule)
            return
        }
        if(settings["ifPeople"] && parent.getEveryonePresent(settings["ifPeople"], app.label)) {
            runIn(60,runIncrementalSchedule)
            return true
        }

        parent.setStateMulti(settings["timeDevice"],app.label)

        // Reschedule itself
        runIn(15,runIncrementalSchedule)
        putLog(1136,"trace","runIncrementalSchedule exiting")
    }
    return true
}


// Performs actual changes at time set with timeOn
// Called only by schedule set in incrementalSchedule
def runDailyStopSchedule(){
    atomicState.defaults = null
    if(state.disable) return

    // Set time state variables
    if(!setTime()) return
    setDailySchedules("stop")

    // Do not test for day when stopping
    if(settings["timeDays"] && !parent.todayInDayList(settings["timeDays"],app.label)) return
    
        // Update and set state (when turning on, off, or toggling)
    if(settings["timeOff"] == "on" || settings["timeOff"] == "off" || settings["timeOff"] == "toggle") {
        parent.updateStateMulti(settings["timeDevice"],settings["timeOff"],app.label)
    }
    
    defaults = getStartDefaults("stop")

    // If level off value (either end level or progressive), use it
    if(settings["levelOff"]) {
        defaults."level"."time" = "stop"
    // If no end level but start level, clear the schedule
    } else if(settings["levelOn"]){
        defaults."level" = ["time":"stop","appId":app.id]
    }
    if(settings["tempOff"]) {
        defaults."temp"."time" = "stop"
    } else if(settings["tempOn"]){
        defaults."temp" = ["time":"stop","appId":app.id]
    }
    if(settings["hueOff"]) {
        defaults."hue"."time" = "stop"
    } else if(settings["hueOn"]){
        defaults."hue" = ["time":"stop","appId":app.id]
    }
    if(settings["satOff"]) {
        defaults."sat"."time" = "stop"
    } else if(settings["satOn"]){
        defaults."sat" = ["time":"stop","appId":app.id]
    }
    
    // Clear out schedule's levels
    parent.updateLevelsMulti(settings["timeDevice"],defaults,app.label)
    
    // If not correct mode, exit
    if(settings["ifMode"] && location.mode != settings["ifMode"]) return
    
    if(settings["ifPeople"] && parent.getEveryonePresent(settings["ifPeople"], app.label)) return
    
    // Update and set state (when turning on, off, or toggling)
    parent.setStateMulti(settings["timeDevice"],app.label)

    // Set stop mode
    if(settings["modeChangeOff"]) setLocationMode(settings["modeChangeOff"])

    return
}

def subscribeDevices(){
    subscribe(settings["timeDevice"], "switch", handleStateChange)
    subscribe(settings["timeDevice"], "hue", handleHueChange)
    subscribe(settings["timeDevice"], "saturation", handleSatChange)
    subscribe(settings["timeDevice"], "colorTemperature", handleTempChange)
    subscribe(settings["timeDevice"], "level", handleLevelChange)
    subscribe(settings["timeDevice"], "Speed", handleLevelChange)
    return
}

def setTime(){
    if(setStartStopTime("start")) {
    setStartStopTime("stop") 
    //if(!atomicState.totalSeconds || (inputStartType == "sunrise" || inputStartType == "sunset" || inputStopType == "sunrise" || inputStopType == "sunset"))
    //setTotalSeconds()
    returnValue = true
    }
    return returnValue
}

// Sets atomicState.start and atomicState.stop variables
// Requires type value of "start" or "stop" (must be capitalized to match setting variables)
def setStartStopTime(type){
    if(type != "start" && type != "stop") {
        putLog(1226,"error","Invalid value for type \"$type\" sent to setStartStopTime function")
        return
    }

    // Change to uppercase to match input strings (eg "inputStartTime")
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
        putLog(1243,"error","ERROR: input" + type + "Type set to " + settings["input${type}Type"])
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

// Used by Master to check whether to reschedule incremental
// type = "start" or "stop"
def getTimeVariable(){
    time = [atomicState.start,atomicState.stop]
    return time
}

// Returns true is schedule is not inactive, and allows for no stop time
// Used by getDefaultLevel
def getScheduleActive(){
    // If not correct day, return false
    if(timeDays && !parent.todayInDayList(timeDays,app.label)) return

    // If no start or stop time, return false
    if(!atomicState.start || !atomicState.stop) return

    // If not between start and stop time, return false
    if(!parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return

    return true
}

def getScheduleDisabled(){
    // If disabled, return true
    if(state.disable) return true

    // If mode isn't correct, return false
    if(settings["ifMode"] && location.mode != settings["ifMode"]) return true
    
    if(settings["ifPeople"] && parent.getEveryonePresent(settings["ifPeople"], app.label)) return true

    return false
}

def getStartDefaults(action = "start"){
    if(atomicState.defaults) return atomicState.defaults
    
    // If no settings, exit
    if(!settings["levelOn"] && !settings["levelOff"] && !settings["tempOn"] && !settings["tempOff"] && !settings["hueOn"] && !settings["hueOff"] && !settings["satOn"] && !settings["satOff"]) return
    
    // If not correct time, exit
    // Shouldn't happen
    if(action == "start" && !parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return
    
    defaults = [:]

    if(settings["levelOn"] || settings["levelOff"]) {
        defaults."level" = [:]
        if(settings["levelOn"]) defaults."level"."startLevel" = parent.convertToInteger(settings["levelOn"],app.label)
        if(settings["levelOff"]) defaults."level"."stopLevel" = parent.convertToInteger(settings["levelOff"],app.label)
        defaults."level"."appId" = app.id
    }
    if(settings["tempOn"] || settings["tempOff"]) {
        defaults."temp" = [:]
        if(settings["tempOn"]) defaults."temp"."startLevel" = parent.convertToInteger(settings["tempOn"],app.label)
        if(settings["tempOff"]) defaults."temp"."stopLevel" = parent.convertToInteger(settings["tempOff"],app.label)
        defaults."temp"."appId" = app.id
    }
    if(settings["hueOn"] || settings["hueOff"]) {
        defaults."hue" = [:]
        if(settings["hueOn"]) defaults."hue"."startLevel" = parent.convertToInteger(settings["hueOn"],app.label)
        if(settings["hueOff"]) defaults."hue"."stopLevel" = parent.convertToInteger(settings["hueOff"],app.label)
        defaults."hue"."appId" = app.id
    }
    if(settings["satOn"] || settings["satOff"]) {
        defaults."sat" = [:]
        if(settings["satOn"]) defaults."sat"."startLevel" = parent.convertToInteger(settings["satOn"],app.label)
        if(settings["satOff"]) defaults."sat"."stopLevel" = parent.convertToInteger(settings["satOff"],app.label)
        defaults."sat"."appId" = app.id
    }
    
    // If start and stop, compute seconds and set
    if(atomicState.start && atomicState.stop){
        startHours = parent.convertToInteger(Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.start).format('HH'),app.label)
        startMinutes = parent.convertToInteger(Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.start).format('mm'),app.label)
        startSeconds = parent.convertToInteger(Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.start).format('ss'),app.label)
        startSeconds = startHours * 60 * 60 + startMinutes * 60 + startSeconds
        totalSeconds = Math.floor((Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.stop).time - Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.start).time) / 1000)

        if(settings["levelOn"] && settings["levelOff"]){
            defaults."level"."startSeconds" = startSeconds
            defaults."level"."totalSeconds" = totalSeconds
        }
        if(settings["tempOn"] && settings["tempOff"]){
            defaults."temp"."startSeconds" = startSeconds
            defaults."temp"."totalSeconds" = totalSeconds
        }
        if(settings["hueOn"] && settings["hueOff"]){
            defaults."hue"."startSeconds" = startSeconds
            defaults."hue"."totalSeconds" = totalSeconds
            defaults."hue"."direction" = settings["hueDirection"]
        }
        if(settings["satOn"] && settings["satOff"]){
            defaults."sat"."startSeconds" = startSeconds
            defaults."sat"."totalSeconds" = totalSeconds
        }
    }
    atomicState.defaults = defaults
    return defaults
}

def checkLog(type = null){
    if(!state.logLevel) getLogLevel()
    switch(type) {
        case "error":
        if(state.logLevel > 0) return true
        break
        case "warn":
        if(state.logLevel > 1) return true
        break
        case "info":
        if(state.logLevel > 2) return true
        break
        case "debug":
        break
        case "trace":
        if(state.logLevel == 5) return true
        if(state.logLevel > 3) return true
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,type = "trace",message = null){
    if(!checkLog(type)) return
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
