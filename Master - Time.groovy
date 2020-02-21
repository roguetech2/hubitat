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
*  Version: 0.4.26
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

preferences {
    infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
    errorIcon = "<img src=\"http://emily-john.love/icons/error.png\" width=20 height=20>"
    warningIcon = "<img src=\"http://emily-john.love/icons/warning.png\" width=20 height=20>"

    // If we're missing a value, don't allow save
    if((!app.label) ||
       (!timeDevice) ||
       (!timeOn || !inputStartType) ||
       (inputStartType == "time" && !inputStartTime) ||
       (inputStopType == "time" && !inputStopTime) ||
       ((inputStartType == "sunrise" || inputStartType == "sunset") && !inputStartSunriseType) ||
       ((inputStopType == "sunrise" || inputStopType == "sunset") && !inputStopSunriseType) ||
       ((inputStartSunriseType == "before" || inputStartSunriseType == "after") && !inputStartBefore) ||
       ((inputStopSunriseType == "before" || inputStopSunriseType == "after") && !inputStopBefore) ||
       (levelEnable && !levelOn && !levelOff) ||
       (tempEnable && !tempOn && !tempOff) ||
       (colorEnable && !hueOn && !hueOff && !satOn && !satOff) ||
       (colorEnable && hueOn && hueOff && !hueDirection) ||
       (modeEnable && !modeChangeOn && !modeChangeOff) ||
       (levelOn > 100 || levelOff >100 || (tempOn && tempOn < 1800) || tempOn > 5400 || (tempOff && tempOff < 1800) || tempOff > 5400 || hueOn > 100 || hueOff > 100 || satOn > 100 || satOff > 100)) noInstall = true

    if(noInstall) {
        install = false
    } else {
        install = true
    }

    page(name: "setup", install: install, uninstall: true) {
        section() {
            // If all disabled, force reenable
            if(disableAll){
                input "disableAll", "bool", title: "<b><i>ALL</i> schedules are disabled.</b> Reenable?", submitOnChange:true
                state.disable = true
            }

            // if app disabled, display Name and Devices
            if(!state.disable && disable){
                // display Name
                displayNameOption()
                // if Name entered, display Devices
                if(app.label){
                    displayDevicesOption()
                }
                input "disable", "bool", title: "<b><font color=\"#000099\">This Schedule is disabled.</font></b> Reenable it?", submitOnChange:true
            }

            //if not disabled, then show everything
            if(!state.disable && !disable ){
                displayNameOption()
                //if no label, stop
                if(app.label){
                    displayDevicesOption()
                    //if no devices, stop
                    if(timeDevice){
                        if(!noInstall) input "disable", "bool", title: "This Schedule is enabled. Disable it?", submitOnChange:true

                        displayStartTimeTypeOption()
                        if(inputStartType == "time"){
                            displayStartTimeOption()
                        } else if(inputStartType == "sunrise"){
                            displayStartSunriseSunsetOption()
                        } else if(inputStartType == "sunset"){
                            displayStartSunriseSunsetOption()
                        } else {
                            inputStartTime = null
                            inputStartSunriseType = null
                            inputStartBefore = null
                        }
                        // if not start time entered, stop
                        if(checkStartTimeEntered()){

                            varStartTime = getStartTimeVariables()
                            displayTimeOnOption()

                            if(timeOn){
                                // TO-DO: Add option for on or not on holidays
                                displayStopTimeTypeOption()
                                if(inputStopType == "time"){
                                    displayStopTimeOption()
                                } else if(inputStopType == "sunrise"){
                                    displayStopSunriseSunsetOption()
                                } else if(inputStopType == "sunset"){
                                    displayStopSunriseSunsetOption()
                                } else {
                                    inputStopTime = null
                                    inputStopSunriseType = null
                                    inputStopBefore = null
                                }

                                if(checkStopTimeEntered() && inputStopType != "none"){
                                    varStopTime = getStopTimeVariables()
                                    displayTimeOffOption()
                                }

                                if(inputStopType == "none" || timeOff){
                                    displayBinaryOptions()
                                    displayBrightnessOption()
                                    if(!colorEnable) displayTemperatureOption()
                                    if(!tempEnable) displayColorOption()
                                    displayModeOption()
                                }
                                if(!error) input "disableAll", "bool", title: "Disable <b>ALL</b> schedules?", submitOnChange:true
                            }
                        }
                    }
                }
                if(warning) paragraph "$warning</div>"
                if(error) paragraph "$error</div>"
            }
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

def displayLabel(text = "Null"){
    if(text == "Null") {
        paragraph "<div style=\"background-color:BurlyWood\"> </div>"
    } else {
        paragraph "<div style=\"background-color:BurlyWood\"><b> $text:</b></div>"
    }
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
    displayLabel("Set name for this schedule")
    label title: "", required: true, submitOnChange:true
    if(!app.label) displayInfo("Name this schedule. Each schedule must have a unique name.")
    /* ************************************************** */
    /* TO-DO: Test the name is unique; otherwise          */
    /* rescheduling won't work, since we use "childLabel" */
    /* variable. No clue how to do that, since it         */
    /* doesn't seem can call a parent function in setup.  */
    /* ************************************************** */
}

def displayDevicesOption(){
    displayLabel("Select which device(s) to schedule")
    input "timeDevice", "capability.switch", title: "Device(s)?", multiple: true, required: true, submitOnChange:true
    if(!timeDevice) displayInfo("Select which device(s) to schedule, either for controlling the device or setting default levels.")
}

def displayTimeOnOption(){
    input "timeOn", "enum", title: "Turn devices on or off ($varStartTime)?", multiple: false, width: 12, options: ["none": "Don't turn on or off (leave as is)","on": "Turn On", "off": "Turn Off", "toggle": "Toggle"], submitOnChange:true
    if(!timeOn) displayInfo("Set whether to turn on or off, or toggle Device(s), when starting the schedule. Select \"Don't\" to not have it turn on, turn off, or toggle. Toggle turns on devices that are off, and turns off devices that are on. Required field.")
}

def displayTimeOffOption(){
    input "timeOff", "enum", title: "Turn devices on or off ($varStopTime)?", multiple: false, width: 12, options: ["none": "Don't turn on or off (leave as is)","on": "Turn On", "off": "Turn Off", "toggle": "Toggle"], submitOnChange:true
    if(!timeOff) displayInfo("Set whether to turn on or off, or toggle Device(s), when ending the schedule. If it should not turn on, turn off, or toggle, then select \"Don't\". Toggle turns on devices that are off, and turns off devices that are on. Required field.")
}

def displayStartTimeTypeOption(){
    displayLabel("Start time")

    input "timeDays", "enum", title: "On these days (defaults to all days)", multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"], submitOnChange:true
    if(!timeDays && !inputStartType) {
        displayInfo("Select which day(s) on which to schedule. Applies only to starting the schedule; schedules can always end on any day. Optional field; if none are selected, schedule will default to every day.")
    } else if(!timeDays){
        displayInfo("Select which day(s) on which to schedule. Optional field; if none are selected, schedule will default to every day.")
    }
    if(!inputStartType){
        width = 12
    } else if(inputStartType == "time" || !inputStartSunriseType || inputStartSunriseType == "at"){
        width = 6
    } else if(inputStartSunriseType){
        width = 4
    }
    input "inputStartType", "enum", title: "Start Time Select:", multiple: false, width: width, options: ["time":"Start at specific time", "sunrise":"Sunrise (at, before or after)","sunset":"Sunset (at, before or after)" ], submitOnChange:true
    if(!inputStartType) displayInfo("Select whether to enter a specific time, or have start time based on sunrise and sunset for the Hubitat location. Required field.")
}

def displayStartTimeOption(){
    if(inputStartType != "time") return
    input "inputStartTime", "time", title: "Start time", width: 6, submitOnChange:true
    if(!inputStartTime) displayInfo("Enter the time to start the schedule in \"hh:mm AM/PM\" format. Required field.")
}

def displayStartSunriseSunsetOption(){
    if(!inputStartSunriseType || inputStartSunriseType == "at") {
        width = 6 
    } else {
        width = 4
    }
    input "inputStartSunriseType", "enum", title: "At, before or after $inputStartType:", multiple: false, width: width, options: ["at":"At $inputStartType", "before":"Before $inputStartType", "after":"After $inputStartType"], submitOnChange:true
    if(!inputStartSunriseType) displayInfo("Select whether to start exactly at $inputStartType. To allow entering minutes prior to or after $inputStartType, select \"Before $inputStartType\" or \"After $inputStartType\". Required field.")
    if(inputStartSunriseType == "before" || inputStartSunriseType == "after"){
        input "inputStartBefore", "number", title: "Minutes $inputStartSunriseType $inputStartType:", width: 4, submitOnChange:true
        if(!inputStartBefore) displayInfo("Enter the number of minutes $inputStartSunriseType $inputStartType to start the schedule. Required field.")
        // Check if we can get sunrise/set times for info messages
    }
    if(inputStartBefore && inputStartBefore > 1441){
        message = "Minutes $inputStartSunriseType $inputStartType is equal to "
        if(inputStartBefore > 2881){
            message = message + Math.floor(inputStartBefore / 60 / 24) + " days"
        } else {
            message = message + "a day"
        }
        message = message + ". That may not work right."
        warningMessage(message)
    }
}

def checkStartTimeEntered(){
    //check if proper start time has been entered
    if(inputStartType){
        if(inputStartType == "time" && inputStartTime) return true
        if(inputStartType == "sunrise" && inputStartSunriseType == "at") return true
        if(inputStartType == "sunrise" && (inputStartSunriseType == "before" || inputStartSunriseType == "after") && inputStartBefore) return true
        if(inputStartType == "sunset" && inputStartSunriseType == "at") return true
        if(inputStartType == "sunset" && (inputStartSunriseType == "before" || inputStartSunriseType == "after") && inputStartBefore) return true
    }
}

def checkStopTimeEntered(){
    //check if proper start time has been entered
    if(inputStopType){
        if(inputStopType == "none") return true
        if(inputStopType == "time" && inputStopTime) return true
        if((inputStopType == "sunrise" || inputStopType == "sunset") && inputStopSunriseType == "at") return true
        if((inputStopType == "sunrise" || inputStopType == "sunset") && (inputStopSunriseType == "before" || inputStopSunriseType == "after") && inputStopBefore) return true
    }
}

def displayStopTimeTypeOption(){
    displayLabel("Stop time")
    if(!inputStopType || inputStopType == "none"){
        width = 12
    } else if(inputStopType == "time" || !inputStopSunriseType || inputStopSunriseType == "at"){
        width = 6
    } else if(inputStopSunriseType){
        width = 4
    }
    input "inputStopType", "enum", title: "Stop Time:", multiple: false, width: width, options: ["none":"Don't stop", "time":"Stop at specific time", "sunrise":"Sunrise (at, before or after)","sunset":"Sunset (at, before or after)" ], submitOnChange:true
    if(!inputStopType) {
        if(timeOn == "on" || timeOn == "off"){
            message = "turn the device(s) $timeOn"
        } else if(timeOn == "toggle"){
            message = "toggle the device(s)"
        } else if(timeOn == "none"){
            message = "change the device(s)'s level"
        }
        displayInfo("Select \"Don't stop\" only if you only want to $message $varStartTime. Select to enter a stop time or use sunrise/sunset if you want to:")
        displayInfo("• Set the device(s) turn on, turn off, or toggle at the end of the schedule, or","none")
        displayInfo("• Set the device(s) to a default level throughout a portion of the day, or","none")
        displayInfo("• Set the device(s) to transition levels over time.","none")
    }
}

def displayStopTimeOption(){
    input "inputStopTime", "time", title: "Stop time", width: 6, submitOnChange:true
    if(!inputStopTime) displayInfo("Enter the time to start the schedule in \"hh:mm AM/PM\" format. Required field.")
}

def displayStopSunriseSunsetOption(){
    if(!inputStopSunriseType || inputStopSunriseType == "at"){
        width = 6
    } else {
        width = 4
    }
    input "inputStopSunriseType", "enum", title: "At, before or after $inputStopType:", multiple: false, width: width, options: ["at":"At $inputStopType", "before":"Before $inputStopType", "after":"After $inputStopType"], submitOnChange:true

    if(!inputStopSunriseType) {
        displayInfo("Select whether to stop exactly at $inputStopType. To allow entering minutes prior to or after $inputStopType, select \"Before $inputStopType\" or \"After $inputStopType\". Required field.")
    } else if(inputStopSunriseType == "before"){
        input "inputStopBefore", "number", title: "Minutes before $inputStopType:", width: 4, submitOnChange:true
    } else if(inputStopSunriseType == "after"){
        input "inputStopBefore", "number", title: "Minutes after $inputStopType:", width: 4, submitOnChange:true
    }
    if(inputStopSunriseType && !inputStopBefore) displayInfo("Enter the number of minutes $inputStopSunriseType $inputStopType to start the schedule. Required field.")
    if(inputStopBefore && inputStopBefore > 1441){
        message = "Minutes $inputStopSunriseType $inputStopType is equal to "
        if(inputStopBefore > 2881){
            message = message + Math.floor(inputStopBefore / 60 / 24) + " days"
        } else {
            message = message + "a day"
        }
        message = message + ". That may not work right."
        warningMessage(message)
    }
}

def getStartTimeVariables(){
    if(inputStartType == "time"){
        return "at " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", inputStartTime).format("h:mm a", location.timeZone)
    } else if(inputStartType == "sunrise"){
        if(inputStartSunriseType == "at"){
            return "at sunrise"
        } else if(inputStartSunriseType == "before" && inputStartBefore){
            if(inputStartBefore) return "$inputStartBefore minutes before sunrise"
        } else if(inputStartSunriseType == "after" && inputStartBefore){
            if(inputStartBefore) return "$inputStartBefore minutes after sunrise"
        } else {
            return
        }
    } else if(inputStartType == "sunset" && inputStartSunriseType){
        if(inputStartSunriseType == "at"){
            return "at sunset"
        } else if(inputStartBefore){
            return "$inputStartBefore minutes $inputStartSunriseType sunset"
        }
    }
    if(inputStartBefore && inputStartBefore > 1441){
        message = "Minutes "
        if(inputStartSunriseType == "before" || inputStartSunriseType == "after"){
            message = message + inputStartSunriseType + " "
        }
        if(inputStartType == "sunrise" || inputStartType == "sunset"){
            message = message + inputStartType
        }
        if(inputStartBefore > 2881){
            message = message + Math.floor(inputStartBefore / 60 / 24) + " days."
        } else {
            message = message + "a day."
        }
        warningMessage(message)
    }
}

def getStopTimeVariables(){
    if(inputStopType == "time"){
        return "at " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", inputStopTime).format("h:mm a", location.timeZone)
    } else if(inputStopType == "sunrise"){
        if(inputStopSunriseType == "at"){
            return "at sunrise"
        } else if(inputStopBefore){
            return "$inputStopBefore minutes $inputStopSunriseType sunrise"
        }
    } else if(inputStopType == "sunset"){
        if(inputStopSunriseType == "at"){
            return "at sunset"
        } else if(inputStopBefore){
            return "$inputStopBefore minutes $inputStopSunriseType sunset"
        }
    }
}

def displayBinaryOptions(){
    // If not stop time, clear binary options
    if(!checkStopTimeEntered()) {
        levelEnable = null
        tempEnable = null
        colorEnable = null
        modeEnable = null
    }

    //These need to be moved, either to the top of the UI, and/or to Install, Update and Initialize
    // If change level isn't selected, clear levels
    if(!levelEnable){
        levelOn = null
        levelOff = null
    }
    // If change temp isn't selected, clear temps
    if(!tempEnable){
        tempOn = null
        tempOff = null
        // If change temp is selected, don't allow change color to be selected
    } else {
        colorEnable = null
    }
    // If change color isn't selected, clear colors
    if(!colorEnable){
        hueOn = null
        hueOff = null
        satOn = null
        satOff = null
    }

    // If stop time not entered, don't display binary options
    if(!checkStopTimeEntered()) return

    if(!levelEnable){
        input "levelEnable", "bool", title: "<b>Don't change brightness.</b> Click to change.", submitOnChange:true
    } else {
        input "levelEnable", "bool", title: "<b>Change brightness.</b> Click to change.", submitOnChange:true
    }
    //Should allow both, for settings one value at start and a different at stop
    if(!tempEnable && !colorEnable){
        input "tempEnable", "bool", title: "<b>Don't change temperature color.</b> Click to change.", submitOnChange:true
    } else if(!colorEnable){
        input "tempEnable", "bool", title: "<b>Change temperature color.</b> Click to change.", submitOnChange:true
    } else if(colorEnable){
        paragraph " &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Don't change temperature color."
    }
    if(!colorEnable && !tempEnable){
        input "colorEnable", "bool", title: "<b>Don't change color.</b> Click to change.", submitOnChange:true
    } else if(!tempEnable){
        input "colorEnable", "bool", title: "<b>Change color.</b> Click to change.", submitOnChange:true
    } else if(tempEnable){
        paragraph " &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Don't change color."
    }
    if(!modeEnable){
        input "modeEnable", "bool", title: "<b>Don't change Mode.</b> Click to change.", submitOnChange:true
    } else {
        input "modeEnable", "bool", title: "<b>Change Mode.</b> Click to change.", submitOnChange:true
    }
    if(!levelEnable && !tempEnable && !colorEnable && !modeEnable) displayInfo("Select which option(s) to change, which will allow entering values to set at the start and/or end of the schedule. All are optional.")
}

def displayBrightnessOption(){    
    if(!levelEnable) return
    if(inputStopType == "none"){
        //Should allow setting level, temp and color when schedule ends, even if not turning the device on or off
        displayLabel("Enter default brightness")
        levelOff = null
        input "levelOn", "number", title: "Set brightness ($varStartTime)?", width: 12, submitOnChange:true
        if(!levelOn) {
            displayInfo("Enter the percentage of brightness when turning on, from 1 to 100. Required field (or unselect \"Change brightness\").")
        } else {
            displayInfo("Brightness is percentage from 1 to 100.")
        }
    } else {
        displayLabel("Enter beginning and/or ending brightness")
        input "levelOn", "number", title: "Beginning brightness ($varStartTime)?", width: 6, submitOnChange:true
        input "levelOff", "number", title: "and ending brightness ($varStopTime)?", width: 6, submitOnChange:true
        if(!levelOn && !levelOff) {
            displayInfo("Enter the percentage of brightness when turning on, from 1 to 100, when starting and/or ending the schedule. If entering both starting and ending brightness, it will transition from beginning to ending brightness for the duration of the schedule. Either starting or ending brightness is required (or unselect \"Change brightness\").")
        } else if(!levelOn || !levelOff){
            displayInfo("Enter the percentage of brightness when turning on, from 1 to 100. If entering both starting and ending brightness, it will transition from beginning to ending brightness for the duration of the schedule.")
        } else {
            displayInfo("Brightness is percentage from 1 to 100.")
        }
        if(!levelOn && levelOff && timeOff == "off") warningMessage("With no beginning brightness while setting Device(s) to turn off, setting an ending brightness won't do anything.")
    }

    if(levelOn && levelOn == levelOff) warningMessage("Beginning and ending brightness are both set to $levelOn. This won't hurt anything, but the Stop brightness setting won't actually <i>do</i> anything.")
    if(levelOn > 100) errorMessage("Brightness is percentage from 1 to 100. Correct beginning brightness.")
    if(levelOff > 100) errorMessage("Brightness is percentage from 1 to 100. Correct ending brightness.")
}

def displayTemperatureOption(){
    if(tempEnable){
        if(inputStopType == "none"){
            displayLabel("Enter default color temperature")
            tempOff = null
            input "tempOn", "number", title: "Set color temperature ($varStartTime)?", width: 12, submitOnChange:true
            if(!tempOn) displayInfo("Temperature color is in Kelvin from 1800 to 5400, when starting and/or ending the schedule. Lower values have more red, while higher values have more blue, where daylight is 5000, cool white is 4000, and warm white is 3000. Required field (or unselect \"Change temperature\").")
        } else {
            displayLabel("Enter beginning and/or ending color temperature")
            input "tempOn", "number", title: "Beginning color temperature ($varStartTime)?", width: 6, submitOnChange:true
            input "tempOff", "number", title: "and ending color temperature ($varStopTime)?", width: 6, submitOnChange:true
            if(!tempOn && !tempOff) {
                displayInfo("Temperature color is in Kelvin from 1800 to 5400, when starting and/or ending the schedule. Lower values have more red, while higher values have more blue, where daylight is 5000, cool white is 4000, and warm white is 3000. If entering both starting and ending temperature, it will transition from beginning to ending temperature for the duration of the schedule. Either starting or ending temperature is required (or unselect \"Change temperature\").")
            } else if(!tempOn || !tempOff){
                displayInfo("Temperature color is from 1800 to 5400, where daylight is 5000, cool white is 4000, and warm white is 3000. If entering both starting and ending temperature, it will transition from beginning to ending temperature for the duration of the schedule.")
            } else {
                displayInfo("Temperature color is from 1800 to 5400, where daylight is 5000, cool white is 4000, and warm white is 3000.")
            }
        }
    }
    if(tempOn && tempOn == tempOff) warningMessage("Beginning and ending color temperature are both set to $tempOn. This won't hurt anything, but the Stop color temperature setting won't actually <i>do</i> anything.")
    if(tempOn && (tempOn < 1800 || tempOn > 5400)) errorMessage("Temperature color is from 1800 to 5400, where daylight is 5000, warm white is 3000, and cool white is 4000. Correct beginning temperature.")
    if(tempOff && (tempOff < 1800 || tempOff > 5400)) errorMessage("Temperature color is from 1800 to 5400, where daylight is 5000, warm white is 3000, and cool white is 4000. Correct ending temperature.")
}

def displayColorOption(){
    if(!colorEnable) return
    if(inputStopType == "none"){
        displayLabel("Enter default hue and saturation")
        hueOff = null
        satOff = null
        input "hueOn", "number", title: "Set hue ($varStartTime)?", width: 6, submitOnChange:true
        input "satOn", "number", title: "Set saturation ($varStartTime)?", width: 6, submitOnChange:true
        displayInfo("Hue is the shade of color, from 1 to 100, when starting and/or ending the schedule. Red is 1 or 100, yellow is 11, green is 26, blue is 66, and purple is 73.")
        displayInfo("Saturation is the percentage depth of color, from 1 to 100, where 1 is hardly any color tint and 100 is full color. Either hue or saturation is required (or unselect \"Change color\").")
    } else {
        displayLabel("Enter beginning and ending hue and saturation")

        if(hueOn && hueOff){
            if(hueOn < hueOff){
                forwardSequence = "25, 26, 27  ... 73, 74, 75"
                reverseSequence = "25, 24, 23 ... 2, 1, 100, 99 ... 77, 76, 75"
            } else {
                forwardSequence = "75, 76, 77 ... 99, 100, 1, 2 ... 23, 24, 25"
                reverseSequence = "75, 74, 73 ... 27, 26, 25"
            }
            input "hueOn", "number", title: "Beginning hue ($varStartTime)?", width: 4, submitOnChange:true
            input "hueOff", "number", title: "and ending hue ($varStopTime)?", width: 4, submitOnChange:true
            input "hueDirection", "enum", title: "Which order to change hue?", width: 4, submitOnChange:true, options: ["Forward": forwardSequence, "Reverse": reverseSequence]
        } else {
            input "hueOn", "number", title: "Beginning hue ($varStartTime)?", width: 6, submitOnChange:true
            input "hueOff", "number", title: "and ending hue ($varStopTime)?", width: 6, submitOnChange:true
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

        input "satOn", "number", title: "Beginning saturation ($varStartTime)?", width: 6, submitOnChange:true
        input "satOff", "number", title: "and ending saturation ($varStopTime)?", width: 6, submitOnChange:true
        if(!satOn && !satOff){
            displayInfo("Saturation is the percentage amount of color tint displayed, from 1 to 100, when starting and/or ending the schedule. 1 is hardly any color tint and 100 is full color. If entering both starting and ending saturation, it will transition from beginning to ending saturation for the duration of the schedule. Optional field.")
        } else if(!satOn || !satOff){
            displayInfo("Saturation is the percentage amount of color tint displayed, from 1 to 100, where 1 is hardly any color tint and 100 is full color. If entering both starting and ending saturation, it will transition from beginning to ending saturation for the duration of the schedule. Optional field.")
        } else {
            displayInfo("Saturation is the percentage amount of color tint displayed, from 1 to 100, where 1 is hardly any color tint and 100 is full color.")
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
    if(!checkStopTimeEntered()) return
    if(modeEnable){
        if(inputStopType == "none"){
            displayLabel("Change Mode $varStartTime")
            input "modeChangeOn", "mode", title: "Set Hubitat's \"Mode\" ($varStartTime)?", width: 12, submitOnChange:true
        } else {
            displayLabel("Change Mode $varStartTime and/or $varStopTime")
            input "modeChangeOn", "mode", title: "Set Hubitat's \"Mode\" ($varStartTime)?", width: 6, submitOnChange:true
            input "modeChangeOff", "mode", title: "Set Hubitat's \"Mode\" ($varStopTime)?", width: 6, submitOnChange:true
        }
    }
    input "ifMode", "mode", title: "Only run if Mode is already?", width: 12, submitOnChange:true
    if(ifMode){
        displayInfo("This will limit the schedule from running unless Hubitat's Mode is $ifMode.")
    } else {
        displayInfo("This will limit the schedule from running unless Hubitat's Mode is as selected. Optional field.")
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
levelEnable - bool - Flag to display level options
tempEnable - bool - Flag to display temp options
colorEnable - bool - Flag to display color options
modeEnable - bool - Flag to display mode options
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

/* ************************************************** */
/*                                                    */
/* End display functions.                             */
/*                                                    */
/* ************************************************** */

def installed() {
    logTrace(646, "Installed")
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    logTrace(652,"Updated")
    initialize()
}

def initialize() {
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    subscribe(timeDevice, "switch.on", handleStateChange)

    unschedule()

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
    if(!levelEnable) settings.levelOn = null
    if(!tempEnable) settings.tempOn = null
    if(!colorEnable){
        settings.hueOn = null
        settings.satOn = null
    }
    if(!modeEnable) settings.modeChangeOn = null
    if(!levelOn || levelOn == levelOff) settings.levelOff = null
    if(!tempOn || tempOn == tempOff) settings.tempOff = null
    if(!hueOn || hueOn == hueOff) settings.hueOff = null
    if(!satOn || satOn == satOff) settings.satOff = null

    if(disable || disableAll) {
        state.disable = true
    } else {
        state.disable = false
        // Set start time, stop time, and total seconds
        if(!setTime()) return false
        setWeekDays()

        setDailySchedules()
        setIncrementalSchedule()
    }
    logTrace(705,"Initialized")
    return true
}

def setDailySchedules(){
    unschedule(runDailySchedule)
    // Schedule dailyStart, either every day or with specific days
    startHours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.start).format('HH').toInteger()
    startMinutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.start).format('mm').toInteger()
    if(atomicState.stop){
        stopHours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.stop).format('HH').toInteger()
        stopMinutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.stop).format('mm').toInteger()
    }

    if(state.weekDays) {
        days = "? * $state.weekDays"
    } else {
        days = "* * ?"
    }

    // Schedule start
    schedule("0 $startMinutes $startHours $days", runDailySchedule, [overwrite: false, data: [action: "start"]])

    if(atomicState.stop){
        logTrace(729,"Scheduling runDailySchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.start).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $startMinutes $startHours ? *$days) and " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.stop).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $stopMinutes $stopHours * * ?)","debug")
        // Schedule stop
        schedule("0 $stopMinutes $stopHours $days", runDailySchedule, [overwrite: false, data: [action: "stop"]])
    } else {
        logTrace(733,"Scheduling runDailySchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.start).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $startMinutes $startHours ? *$days)","debug")
    }
    return true
}

// Performs actual changes for incremental schedule
// Called only by schedule set in incrementalSchedule
def runIncrementalSchedule(){
    unschedule(runIncrementalSchedule)

    if(!getScheduleActive()) {
        logTrace(744,"Schedule doesn't require active updates, exiting runIncrementalSchedule","trace")
        return
    }

    // If nothing to do, exit
    if((!levelOn || !levelOff) && (!tempOn || !tempOff) && (!hueOn || !hueOff) && (!satOn || !satOff)) return

    // If device(s) not on, exit
    if(!parent.isOnMulti(timeDevice)){
        logTrace(753,"Since $timeDevice is off, stopping recurring schedules","debug")
        return
    }

    // Set levels
    /* ************************************************************************ */
    /* TO-DO: Just use setLevelSingle here. I don't think ...Multi is used      */
    /* anywhere else, and we aren't getting any "default" levels within         */
    /* incremental, so wtf is the point of going to parent?                     */
    /* ************************************************************************ */
    parent.setLevelsMulti(timeDevice,app.label)

    // Reschedule itself
    setIncrementalSchedule()
    return true
}

def setIncrementalSchedule(){
    unschedule(runIncrementalSchedule)
    runIn(20,runIncrementalSchedule)
    logTrace(773,"Scheduling incremental for 20 seconds","debug")
}

// Performs actual changes at time set with timeOn
// Called only by schedule set in incrementalSchedule
def runDailySchedule(data){
    setDailySchedules()
    // If not valid data variable passed, throw error and exit
    if(data.action != "start" && data.action != "stop"){
        logTrace(782,"Invalid value for action \"$data.action\" sent to runDailySchedule function","error")
    }

    // Set time state variables
    if(!setTime()) return

    // If disabled, exit
    if(disable || state.disable) return

    // If not correct day, exit
    if(timeDays && !parent.todayInDayList(timeDays,app.label)) {
        logTrace(793,"Daily schedule ran on off day; that shouldn't happen","error")
        return
    }

    // If not correct mode, reschuexit
    if(ifMode && location.mode != ifMode) {
        return
    }

    // Set start mode
    if(modeChangeOn && data.action == "start") setLocationMode(modeChangeOn)
    // Set stop mode
    if(modeChangeOff && data.action == "stop") setLocationMode(modeChangeOff)

    if(data.action == "start"){
        // Let the catch-all "setStateMulti" handle everything
        // getDefaults will handle starting levels
        setStateMulti(timeOn,timeDevice)
    } else if(data.action == "stop"){
        setStateMulti(timeOff,timeDevice)

        // If ending the schedule, then need to set off levels since they won't be captured
        // in the getDefaults routine
        if(levelOff || tempOff || hueOff || satOff){
            timeDevice.each{
                if(isOn(it)){
                    if(!levelOff) levelOff = ""
                    if(!tempOff) tempOff = ""
                    if(!hueOff) hueOff = ""
                    if(!satOff) satOff = ""
                    // If there's a starting level, temp, hue or sat, this will cause levels to
                    // set a second time, but... it's once a day. Think it's better safe than sorry.
                    parent.setSingleLevel(levelOff, tempOff, hueOff, satOff, it,app.label)
                }
            }
        }
    }
    return true
}

// Returns array for level, temp, hue and sat
// Should NOT return true or false; always return defaults array
// Array should return text "Null" for null values
def getDefaultLevel(device,appLabel){
    // If no device match, return nulls
    timeDevice.findAll( {it.id == device.id} ).each {
        logTrace(839,"getDefaultLevel matched device $device","debug")
        match = true
    }
    if(!match) return

    if(!getScheduleActive()) return

    // If we need elapsed, then get it
    if((levelOn && levelOff) || (tempOn && tempOff) || (hueOn && hueOff) || (satOn && satOff)){
        elapsedFraction = getElapsedFraction()

        if(!elapsedFraction) {
            logTrace(848,"Unable to calculate elapsed time with start \"$atomicState.start\" and stop \"$atomicState.stop\"","error")
            return
        }
    }

    if(!levelOn && !tempOn && !hueOn && !satOn) return

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
    } else if(levelOn && !levelOff && appLabel == app.label){
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
    } else if(tempOn && !tempOff && appLabel == app.label){
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
    } else if(hueOn && !hueOff && appLabel == app.label){
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
    } else if(satOn && !satOff && appLabel == app.label){
        defaults.put("sat",satOn)
    }

    logTrace(923,"Returning levels $defaults for $device","debug")
    return defaults
}

// Captures device state changes to set levels and start incremental schedule
// Needs to find whether an app triggered it; if so, app may want to override schedule
def handleStateChange(event){
    // This function should be the same as "resume" portion of setStateMulti, except
    // no override levels, and not turning off if no level
    // If an app requested the state change, then exit
    if(parent.getStateRequest(event.device,app.label)) {
        logTrace(934,"Device state changed by an app; exiting handleStateChange","debug")
        return
    }
    logTrace(937,"Device $event.device turned on outside of app; caught by handleStateChange","debug")

    // If defaults, then there's an active schedule
    // So use it for if overriding/reenabling
    defaults = parent.getScheduleDefaultSingle(event.device,app.label)

    // Set default levels, for level and temp, if no scheduled defaults
    defaults = parent.getDefaultSingle(defaults,app.label)

    // Set default level
    parent.setLevelSingle(defaults.level,defaults.temp,defaults.hue,defaults.sat,event.device,,app.label)
    logTrace(929,"Set levels $defaults for $event.device, which was turned on outside of the app","debug")

    // if toggling on, reschedule incremental
    parent.rescheduleIncrementalSingle(event.device,app.label)
    return
}

def setTime(){
    if(!setStartStopTime("Start")) return
    setStartStopTime("Stop") 

    setTotalSeconds()
    return true
}

// Sets atomicState.start and atomicState.stop variables
// Requires type value of "Start" or "Stop" (must be capitalized to match setting variables)
def setStartStopTime(type){
    if(type != "Start" && type != "Stop") {
        logTrace(967,"Invalid value for type \"$type\" sent to setStartStopTime function","error")
        return
    }

    if(type == "Start") atomicState.start = null
    if(type == "Stop") atomicState.stop = null

    // If no stop time, exit
    if(type == "Stop" && (!inputStopType || inputStopType == "none")) return true

    if(settings["input${type}Type"] == "time"){
        value = settings["input${type}Time"]
    } else if(settings["input${type}Type"] == "sunrise"){
        value = (settings["input${type}SunriseType"] == "before" ? parent.getSunrise(settings["input${type}Before"] * -1,app.label) : parent.getSunrise(settings["input${type}Before"],app.label))
    } else if(settings["input${type}Type"] == "sunset"){
        value = (settings["input${type}SunriseType"] == "before" ? parent.getSunset(settings["input${type}Before"] * -1,app.label) : parent.getSunset(settings["input${type}Before"],app.label))
    } else {
        logTrace(984,"input" + type + "Type set to " + settings["input${type}Type"],"error")
        return
    }

    if(type == "Stop"){
        if(timeToday(atomicState.start, location.timeZone).time > timeToday(value, location.timeZone).time) value = parent.getTomorrow(value,app.label)
    }

    logTrace(992,"$type time set as " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", value).format("h:mma MMM dd, yyyy", location.timeZone),"trace")
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
    logTrace(1031,"weekDaysToNum returning $dayString","debug")
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
    logTrace(1046,"Schedule total seconds is $atomicState.totalSeconds","trace")
    return true
}

// Returns percentage of schedule that has elapsed
// Only called by getDefaultLevel
def getElapsedFraction(){
    // If not between start and stop time, exit
    if(!atomicState.stop || !parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return false

    elapsedSeconds = Math.floor((new Date().time - Date.parse("yyyy-MM-dd'T'HH:mm:ss", atomicState.start).time) / 1000)
    logTrace(1057,"$elapsedSeconds seconds of the schedule has elpased.","debug")
    //Divide for percentage of time expired (avoid div/0 error)
    if(elapsedSeconds < 1){
        elapsedFraction = 1 / atomicState.totalSeconds * 1000 / 1000
    } else {
        elapsedFraction = elapsedSeconds / atomicState.totalSeconds * 1000 / 1000
    }

    if(elapsedFraction > 1) {
        logTrace(1066,"Over 100% of the schedule has elapsed, so start or stop time hasn't updated correctly.","error")
        return
    }

    logTrace(1070,Math.floor(elapsedFraction * 100) + "% has elapsed in the schedule","debug")
    return elapsedFraction
}

// Used by Master to check whether to reschedule incremental
// type = "start" or "stop"
def getTimeVariable(){
    time = [atomicState.start,atomicState.stop]
    return time
}

def getScheduleActive(){
    // If disabled, return false
    if(disable || state.disable) return

    // If not correct day, return false
    if(timeDays && !parent.todayInDayList(timeDays,app.label)) return

    // If mode isn't correct, return false
    if(ifMode && location.mode != ifMode) return

    // If no start or stop time, return false
    if(!atomicState.start || !atomicState.stop) return

    // If no start levels, return false
    if(!levelOn && !tempOn && !hueOn && !satOn) return false

    // If not between start and stop time, return false
    if(!parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return

    return true
}

// If deviceChange exists, adds deviceId to it; otherwise, creates deviceChange with deviceId
// Used to track if app turned on device when schedule captures a device state changing to on
def addStateDeviceChange(singleDeviceId){
    if(atomicState.deviceChange) {
        atomicState.deviceChange = "$atomicState.deviceChange:$singleDeviceId:"
    } else {
        atomicState.deviceChange = ":$singleDeviceId:"
    }
}


// Gets levels as set for the app
// Function must be included in all apps that use MultiOn
def getOverrideLevels(defaults,appAction = null){
    return defaults        
}

// Returns the value of deviceChange
// Used by schedule when a device state changes to on, to check if an app did it
// Function must be in every app
def getStateDeviceChange(singleDeviceId){
    if(atomicState.deviceChange){
        return atomicState.deviceChange.indexOf(":$singleDeviceId:")
    } else {
        return false
    }
}

// Scheduled funtion to reset the value of deviceChange
def resetStateDeviceChange(){
    atomicState.deviceChange = null
    return
}

// Schedules don't use dim, brighten, or resume
// This is a bit of a mess, but.... 
def setStateMulti(deviceAction,device,appAction = null){
    if(!deviceAction || (deviceAction != "on" && deviceAction != "off" && deviceAction != "toggle" && deviceAction != "none")) {
        logTrace(1133,"Invalid deviceAction \"$deviceAction\" sent to setStateMulti","error")
        return
    }

    if(deviceAction == "off"){
        // Turn off devices
        parent.setStateMulti("off",device,app.label)
        return true
    }

    if(deviceAction == "on"){
        // Add device ids to deviceChange, so schedule knows it was turned on by an app
        device.each{
            addStateDeviceChange(it.id)
            // Time to schedule resetting deviceChange should match total time of waitStateChange
            // Might be best to put this in a state variable in Initialize, as a setting?
            runIn(2,resetStateDeviceChange)
        }
        logTrace(1151,"Device id's turned on are $atomicState.deviceChange","debug")

        // Turn on devices
        parent.setStateMulti("on",device,app.label)
        // Get and set defaults levels for each device
        device.each{
            setStateOnSingle(it,appAction)
        }
        return true
    }

    if(deviceAction == "toggle"){
        // Create toggleOnDevice variable, used to track which devices are being turned on
        toggleOnDevice = []
        // Set count variable, used for toggleOnDevice
        count = 0
        device.each{
            count = count + 1
            // Get original state
            deviceState = parent.isOn(it)
            // If toggling to off
            if(parent.isOn(it)){
                parent.setStateSingle("off",it,app.label)
                // Else if toggling on
            } else {
                // When turning on, add device id to deviceChange, so schedule knows it was turned on by an app
                addStateDeviceChange(it.id)
                runIn(1,resetStateDeviceChange)
                parent.setStateSingle("on",it,app.label)
                // Create list of devices toggled on
                // This lets us turn all of them on, then loop again so when setting levels, it won't wait for each individual device to respond
                toggleOnDevice.add(count)
            }
        }
        logTrace(1189,"Device id's turned on are $atomicState.deviceChange","debug")
        // Create newCount variable, which is compared to the [old]count variable
        // Used to identify which lights were turned on in the last loop
        newCount = 0
        device.each{
            newCount = newCount + 1
            // If turning on, set default levels and over-ride with any contact levels
            // If newCount is contained in the list of [old]count, then we toggled on
            if(toggleOnDevice.contains(newCount)){
                setStateOnSingle(it,appAction)
            }
        }
        return true
    }

    if(deviceAction == "resume"){
        device.each{
            // If turning on, set default levels and over-ride with any contact levels
            if(deviceAction == "resume"){
                // If defaults, then there's an active schedule
                // So use it for if overriding/reenabling
                defaults = parent.getScheduleDefaultSingle(it,app.label)
                logTrace(1211,"Scheduled defaults are $defaults","debug")

                defaults = getOverrideLevels(defaults,appAction)
                logTrace(1214,"With " + app.label + " overrides, using $defaults","debug")

                // Skipping getting overall defaults, since we're resuming a schedule or exiting;
                // rather keep things the same level rather than an arbitrary default, and
                // if we got default, we'd not turn it off

                parent.setLevelSingle(defaults.level,defaults.temp,defaults.hue,defaults.sat,it,app.label)
                // Set default level
                if(!defaults){
                    logTrace(1223,"No schedule to resume for $it; turning off","trace")
                    parent.setStateSingle("off",it,app.label)
                } else {
                    parent.rescheduleIncrementalSingle(it,app.label)
                }
            }
        }
        return true
    }

    if(deviceAction == "none"){
        // If doing nothing, reschedule incremental changes (to reset any overriding of schedules)
        // I think this is the only place we use ...Multi, prolly not enough to justify a separate function
        parent.rescheduleIncrementalMulti(device,app.label)
        return true
    }
}

// Handles turning on a single device and setting levels
def setStateOnSingle(singleDevice,appAction = null){
    // If defaults, then there's an active schedule
    // So use it for if overriding/reenabling
    // In scheduler app, this gets defaults for any *other* schedule
    defaults = parent.getScheduleDefaultSingle(singleDevice,app.label)
    logMessage = defaults ? "$singleDevice scheduled for $defaults" : "$singleDevice has no scheduled default levels"

    // If there are defaults, then there's a schedule (the results are corrupted below)
    if(defaults) parent.rescheduleIncrementalSingle(singleDevice,app.label)

    // This does nothing in Time, or other app that has no levels
    defaults = getOverrideLevels(defaults,appAction)
    logMessage += defaults ? ", controller overrides of $defaults": ", no controller overrides"

    // Set default levels, for level and temp, if no scheduled defaults (don't need to do for "resume")
    defaults = parent.getDefaultSingle(defaults,app.label)
    logMessage += ", so with generic defaults $defaults"

    logTrace(1260,logMessage,"debug")
    parent.setLevelSingle(defaults.level,defaults.temp,defaults.hue,defaults.sat,singleDevice,app.label)
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def logTrace(lineNumber,message = null, type = "trace"){
    // Uncomment return for no logging at all
    // return

    // logLevel sets number of log messages
    // 1 for least (errors only)
    // 5 for most (all)
    logLevel = 5

    message = (message ? " -- $message" : "")
    if(lineNumber) message = "(line $lineNumber)$message"
    message = "$app.label $message"
    switch(type) {
        case "error":
        log.error message
        break
        case "warn":
        if(logLevel > 1) log.warn message
        break
        case "info":
        if(logLevel > 2) log.info message
        break
        case "trace":
        if(logLevel > 3) log.trace message
        break
        case "debug":
        if(loglevel == 5) log.debug message
    }
    return true
}
