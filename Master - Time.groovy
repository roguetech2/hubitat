/***********************************************************************************************************************
*
*  Copyright (C) 2018 roguetech
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
*  Version: 0.3.7
*
***********************************************************************************************************************/

definition(
    name: "Master - Time2",
    namespace: "master",
    author: "roguetech",
    description: "Schedules, times and default settings",
    parent: "master:Master",
    category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/mi_face_s.png",
    iconX2Url: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/mi_face.png"
)

preferences {
    
	infoIcon = "<img src=\"http://files.softicons.com/download/toolbar-icons/fatcow-hosting-icons-by-fatcow/png/16/information.png\" width=20 height=20>"
	errorIcon = "<img src=\"http://files.softicons.com/download/toolbar-icons/fatcow-hosting-icons-by-fatcow/png/16/error.png\" width=20 height=20>"

    page(name: "setup", install: true, uninstall: true) {
        section() {
            // If all disabled, force reenable
            if(state.disableAll){
                input "disableAll", "bool", title: "<b>All schedules are disabled.</b> Reenable?", defaultValue: false, submitOnChange:true
                state.disable = true
            }

            // if app disabled, display Name and Devices
            if(!state.disable && state.disable){
                // display Name
                displayNameOption()
                // if Name entered, display Devices
                if(app.label){
                    displayLabel("Select which devices to schedule")
                    displayDevicesOption()
                }
                input "disable", "bool", title: "<b><font color=\"#000099\">Schedule is disabled.</font></b> Reenable it?", submitOnChange:true
            }

            //if not disabled, then show everything
            if(!state.disable && !state.disable ){
                displayNameOption()
                //if no label, stop
                if(app.label){
                    displayDevicesOption()
                    //if no devices, stop
                    if(timeDevice){
                        
                        displayStartTimeTypeOption()
                        if(inputStartType == "Time"){
                            displayStartTimeOption()
                        } else if(inputStartType == "Sunrise"){
                            displayStartSunriseOption()
                        } else if(inputStartType == "Sunset"){
                            displayStartSunsetOption()
                        } else {
                            inputStartTime = null
                            inputStartSunriseType = null
                            inputStartSunsetType = null
                        }
                        // if not start time entered, stop
                        if(checkStartTimeEntered()){

                            varStartTime = getStartTimeVariables()
                            
                            input "timeOn", "enum", title: "Turn devices on or off ($varStartTime)?", multiple: false, required: false, width: 12, options: ["None": "Don't turn on or off (leave as is)","On": "Turn On", "Off": "Turn Off", "Toggle": "Toggle (if on, turn off, and if off, turn on)"], submitOnChange:true
                            if(timeOn){
                                
// TO-DO: Add option for on or not on holidays
                                displayStopTimeTypeOption()
                                if(inputStopType == "Time"){
                                    displayStopTimeOption()
                                } else if(inputStopType == "Sunrise"){
                                    displayStopSunriseOption()
                                } else if(inputStopType == "Sunset"){
                                    displayStopSunsetOption()
                                } else {
                                    inputStopType = null
                                    inputStopSunriseType = null
                                    inputStopBefore = null
                                }
                                if(checkStopTimeEntered() && inputStopType){
                                    varStopTime = getStopTimeVariables()
                                    input "timeOff", "enum", title: "Turn devices on or off ($varStopTime)?", multiple: false, required: false, width: 12, options: ["None": "Don't turn on or off (leave as is)","On": "Turn On", "Off": "Turn Off", "Toggle": "Toggle (if on, turn off, and if off, turn on)"], submitOnChange:true
                                    if(timeOff){
                                        displayBinaryOptions()
                                        displayBrightnessOption()
                                        if(!colorEnable) displayTemperatureOption()
                                        if(!tempEnable) displayColorOption()
                                        displayModeOption()
                                    }
                                    if(!error) input "timeDisableAll", "bool", title: "Disable <b>ALL</b> schedules?", defaultValue: false, submitOnChange:true
                                }
                            }
                        }
                    }
                }
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
/* changes are large, where changewill not be smooth. */
/* ************************************************** */


// Display functions

def errorMessage(text){
    if(error){
        error = error + "<br />$errorIcon $text"
    } else {
        error = "<div style=\"background-color:Bisque\">$errorIcon $text"
    }
}

def displayLabel(text = "Null"){
    if(text == "Null") {
        paragraph "<div style=\"background-color:BurlyWood\"> </div>"
    } else {
        paragraph "<div style=\"background-color:BurlyWood\"><b> $text:</b></div>"
    }
}

def displayInfo(text = "Null"){
    if(text == "Null") {
        paragraph "<div style=\"background-color:AliceBlue\"> </div>"
    } else {
        paragraph "<div style=\"background-color:AliceBlue\">$infoIcon $text</div>"
    }
}

def displayNameOption(){
    displayLabel(text="Set name for this schedule")
	label title: "", required: true, submitOnChange:true
/* ************************************************** */
/* TO-DO: Test the name is unique; otherwise          */
/* rescheduling won't work, since we use "childLabel" */
/* variable.                                          */
/* ************************************************** */
}

def displayDevicesOption(){
    displayLabel("Select which devices to schedule")
    input "timeDevice", "capability.switch", title: "Device(s)?", multiple: true, required: true, submitOnChange:true
}

def displayStartTimeTypeOption(){
    displayLabel("Start time")

    input "timeDays", "enum", title: "On these days (Optional):", required: false, multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"], submitOnChange:true
    if(!inputStartType){
        width = 12
    } else if(inputStartType == "Time" || !inputStartSunriseType || inputStartSunriseType == "At"){
        width = 6
    } else if(inputStartSunriseType){
        width = 4
    }
    input "inputStartType", "enum", title: "Start Time:", required: false, multiple: false, width: width, options: ["Time":"Start at specific time", "Sunrise":"Sunrise (at, before or after)","Sunset":"Sunset (at, before or after)" ], submitOnChange:true
}

def displayStartTimeOption(){
    input "inputStartTime", "time", title: "Start time", required: false, width: 6, submitOnChange:true
}

def displayStartSunriseOption(){
    if(!inputStartSunriseType || inputStartSunriseType == "At") {
        width = 6 
    } else {
        width = 4
    }
    input "inputStartSunriseType", "enum", title: "At, before or after sunrise:", required: false, multiple: false, width: width, options: ["At":"At sunrise", "Before":"Before sunrise", "After":"After sunrise"], submitOnChange:true
    if(inputStartSunriseType == "Before"){
        input "inputStartBefore", "number", title: "Minutes before sunrise:", required: false, width: 4, submitOnChange:true
    } else if(inputStartSunriseType == "After"){
        input "inputStartBefore", "number", title: "Minutes after sunrise:", required: false, width: 4, submitOnChange:true
    }
}

def displayStartSunsetOption(){
    if(!inputStartSunriseType || inputStartSunriseType == "At") {
        width = 6
    } else {
        width = 4
    }
    input "inputStartSunriseType", "enum", title: "At, before or after sunset:", required: false, multiple: false, width: width, options: ["At":"At sunset", "Before":"Before sunset", "After":"After sunset"], submitOnChange:true
    if(inputStartSunriseType == "Before"){
        input "inputStartBefore", "number", title: "Minutes before sunset:", required: false, width: 4, submitOnChange:true
    } else if(inputStartSunriseType == "After"){
        input "inputStartBefore", "number", title: "Minutes after sunset:", required: false, width: 4, submitOnChange:true
    }
}

def checkStartTimeEntered(){
    //check if proper start time has been entered
    if(inputStartType){
        if(inputStartType == "Time" && inputStartTime) return true
        if(inputStartType == "Sunrise" && inputStartSunriseType == "At") return true
        if(inputStartType == "Sunrise" && (inputStartSunriseType == "Before" || inputStartSunriseType == "After") && inputStartBefore) return true
        if(inputStartType == "Sunset" && inputStartSunriseType == "At") return true
        if(inputStartType == "Sunset" && (inputStartSunriseType == "Before" || inputStartSunriseType == "After") && inputStartBefore) return true
    }
}

def checkStopTimeEntered(){
    //check if proper start time has been entered
    if(inputStopType){
        if(inputStopType == "None") return true
        if(inputStopType == "Time" && inputStopType) return true
        if(inputStopType == "Sunrise" && inputStopSunriseType == "At") return true
        if(inputStopType == "Sunrise" && (inputStopSunriseType == "Before" || inputStopSunriseType == "After") && inputStopBefore) return true
        if(inputStopType == "Sunset" && inputStopSunsetType == "At") return true
        if(inputStopType == "Sunset" && (inputStopSunsetType == "Before" || inputStopSunsetType == "After") && inputStopBefore) return true
    }
}

def displayStopTimeTypeOption(){
    displayLabel("Stop time")
    if(!inputStopType || inputStopType == "None"){
        width = 12
    } else if(inputStopType == "Time" || !inputStopSunriseType || inputStopSunriseType == "At"){
        width = 6
    } else if(inputStopSunriseType || inputStopSunsetType){
        width = 4
    }
    input "inputStopType", "enum", title: "Stop Time:", required: false, multiple: false, width: width, options: ["None":"Don't stop", "Time":"Stop at specific time", "Sunrise":"Sunrise (at, before or after)","Sunset":"Sunset (at, before or after)" ], submitOnChange:true
}

def displayStopTimeOption(){
    input "inputStopTime", "time", title: "Stop time", required: false, width: 6, submitOnChange:true
}

def displayStopSunriseOption(){
    if(!inputStopSunriseType || inputStopSunriseType == "At"){
        width = 6
    } else {
        width = 4
    }
    input "inputStopSunriseType", "enum", title: "At, before or after sunrise:", required: false, multiple: false, width: width, options: ["At":"At sunrise", "Before":"Before sunrise", "After":"After sunrise"], submitOnChange:true
    if(inputStopSunriseType == "Before" || inputStopSunriseType == "After")
        input "inputStopBefore", "number", title: "Minutes before sunrise", required: false, width: 4, submitOnChange:true
}

def displayStopSunsetOption(){
    if(!inputStopSunriseType || inputStopSunriseType == "At"){
        width = 6
    } else {
        width = 4
    }
    input "inputStopSunriseType", "enum", title: "At, before or after sunset:", required: false, multiple: false, width: width, options: ["At":"At sunset", "Before":"Before sunset", "After":"After sunset"], submitOnChange:true
    if(inputStopSunriseType == "Before" || inputStopSunriseType == "After")
        input "inputStopBefore", "number", title: "Minutes before sunset", required: false, width: 4, submitOnChange:true
}

def getStartTimeVariables(){
    if(inputStartType == "Time" && inputStartTime){
        return "at " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", inputStartTime).format("h:mm a", location.timeZone)
    } else if(inputStartType == "Sunrise"){
        if(inputStartSunriseType && inputStartBefore){
            if(inputStartSunriseType == "At"){
                return "at sunrise"
            } else if(inputStartSunriseType == "Before"){
                return "$inputStartBefore minutes before sunrise"
            } else if(inputStartSunriseType == "After"){
                return "$inputStartBefore minutes after sunrise"
            }
        }
    } else if(inputStartType == "Sunset"){
        if(inputStartSunriseType){
            if(inputStartSunriseType == "At"){
                return "at sunset"
            } else if(inputStartSunriseType == "Before"){
                return "$inputStartBefore minutes before sunset"
            } else if(inputStartSunriseType == "After"){
                return "$inputStartBefore minutes after sunset"
            }
        }
    }
    if(inputStartBefore){
        if(inputStartBefore > 1441){
            message = "Minutes "
            if(inputStartSunriseType == "Before"){
                message = message + "before "
            } else if (inputStartSunriseType == "After"){

                message = message + "after "
            }
            if(inputStartType == "Sunrise"){
                message = message + "sunrise"
            } else if(inputStartType == "Sunset"){
                message = message + "sunset"
            }
            if(inputStartBefore > 2881){
                message = message + Math.floor(inputStartBefore / 60 / 24) + " days."
            } else {
                message = message + "a day."
            }
            errorMessage(message)
        }
    }
}

def getStopTimeVariables(){
    if(inputStopType == "Time" && inputStopTime){
        return "at " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", inputStopTime).format("h:mm a", location.timeZone)
    } else if(inputStopType == "Sunrise"){
        if(inputStopSunriseType && inputStopBefore){
            if(inputStopSunriseType == "At"){
                return "at sunrise"
            } else if(inputStopSunriseType == "Before"){
                return "$inputStopBefore minutes before sunrise"
            } else if(inputStopSunriseType == "After"){
                return "$inputStopBefore minutes after sunrise"
            }
        }
    } else if(inputStopType == "Sunset"){
        if(inputStopSunriseType){
            if(inputStopSunriseType == "At"){
                return "at sunset"
            } else if(inputStopSunriseType == "Before"){
                return "$inputStopBefore minutes before sunset"
            } else if(inputStopSunriseType == "After"){
                return "$inputStopBefore minutes after sunset"
            }
        }
    }
    if(inputStopBefore){
        if(inputStopBefore > 1441){
            message = "Minutes "
            if(inputStopSunriseType == "Before"){
                message = message + "before "
            } else if (inputStopSunriseType == "After"){

                message = message + "after "
            }
            if(inputStopType == "Sunrise"){
                message = message + "sunrise"
            } else if(inputStopType == "Sunset"){
                message = message + "sunset"
            }
            if(inputStopBefore > 2881){
                message = message + Math.floor(inputStartBefore / 60 / 24) + " days."
            } else {
                message = message + "a day."
            }
            errorMessage(message)
        }
    }
}

def displayBinaryOptions(){
    if(!levelEnable){
        input "levelEnable", "bool", title: "<b>Don't change brightness.</b> Click to change.", submitOnChange:true
        levelOn = null
        levelOff = null
    } else {
        input "levelEnable", "bool", title: "<b>Change brightness.</b> Click to change.", submitOnChange:true
    }
    if(!tempEnable && !colorEnable){
        input "tempEnable", "bool", title: "<b>Don't change temperature color.</b> Click to change.", submitOnChange:true
        tempOn = null
        tempOff = null
        colorOn = null
        colorOff = null
    } else if(!colorEnable){
        input "tempEnable", "bool", title: "<b>Change temperature color.</b> Click to change.", submitOnChange:true
        colorOn = null
        colorOff = null
    } else if(colorEnable){
        paragraph " &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Don't change temperature color."
        tempOn = null
        tempOff = null
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
}

def displayBrightnessOption(){
    if(levelEnable){
        if(checkStopTimeEntered && inputStopType != "None"){
            displayLabel("Enter beginning and ending brightness")
        } else {
            displayLabel("Enter default brightness")
            levelOff = null
        }
        if(levelOn){
            input "levelOn", "number", title: "Beginning brightness ($varStartTime)?", required: false, width: 6, submitOnChange:true
            input "levelOff", "number", title: "and ending brightness ($varStartTime)?", required: false, width: 6, submitOnChange:true
        } else {
            input "levelOn", "number", title: "Beginning brightness ($varStartTime)?", required: false, width: 12, submitOnChange:true
        }
        displayInfo("Percentage from 1 to 100.")
    }
    if(levelOn > 100) errorMessage("Beginning brightness can't be more than 100. Correct before saving.")
    if(levelOff > 100) errorMessage("Ending brightness can't be more than 100. Correct before saving.")
}

def displayTemperatureOption(){
    if(tempEnable){
        if(checkStopTimeEntered && inputStopType != "None"){
            displayLabel("Enter beginning and ending color temperature")
        } else {
            displayLabel("Enter default color temperature")
            tempOff = null
        }
        if(tempOn){
            input "tempOn", "number", title: "Beginning color temperature ($varStartTime)?", required: false, width: 6, submitOnChange:true
            input "tempOff", "number", title: "and ending color temperature ($varStopTime)?", required: false, width: 6, submitOnChange:true
        } else {
            input "tempOn", "number", title: "Default color temperature ($varStartTime)?", required: false, width: 12, submitOnChange:true
        }
        displayInfo("Number from 1800 (warm) to 5400 (cold).")
    }
    if(tempOn > 5400) errorMessage("Beginning color temperature can't be more than 5,400. Correct before saving.")
    if(tempOn && tempOn < 1800) errorMessage("Beginning color temperature can't be less than 1,800. Correct before saving.")
    if(tempOff > 5400) errorMessage("Ending color temperature can't be more than 1,800. Correct before saving.")
    if(tempOff && tempOff < 1800) errorMessage("Ending color temperature can't be less than 1,800. Correct before saving.")
}

def displayColorOption(){
    if(colorEnable){
        if(checkStopTimeEntered && inputStopType != "None"){
            displayLabel("Enter beginning and ending hue and saturation")
        } else {
            displayLabel("Enter default hue and saturation temperature")
            hueOff = null
            satOff
        }
        if(hueOn){
            if(hueOff){
                width = 4
            } else {
                width = 6
            }
            input "hueOn", "number", title: "Beginning hue ($varStartTime)?", required: false, width: width, submitOnChange:true
            input "hueOff", "number", title: "and ending hue ($varStopTime)?", required: false, width: width, submitOnChange:true
        } else {
            input "hueOn", "number", title: "Beginning hue ($varStartTime)?", required: false, width: 12, submitOnChange:true
        }
        if(hueOn && hueOff){
            if(hueOn < hueOff){
                forwardSequence = "25, 26, 27  ... 73, 74, 75"
                reverseSequence = "25, 24, 23 ... 2, 1, 100, 99 ... 77, 76, 75"
            } else {
                forwardSequence = "75, 76, 77 ... 99, 100, 1, 2 ... 23, 24, 25"
                reverseSequence = "75, 74, 73 ... 27, 26, 25"
            }
            input "hueDirection", "enum", title: "Which order to change hue?", required: false, width: 4, submitOnChange:true, options: ["Forward": forwardSequence, "Reverse": reverseSequence]

        }
        displayInfo("Hue is the shade of color. Number from 1 to 100. Red is 1 or 100. Yellow is 11. Green is 26. Blue is 66. Purple is 73.")
        if(satOn){
            input "satOn", "number", title: "Beginning saturation ($varStartTime)?", required: false, width: 6, submitOnChange:true
            input "satOff", "number", title: "and ending saturation ($varStopTime)?", required: false, width: 6, submitOnChange:true
        } else {
            input "satOn", "number", title: "Beginning saturation ($varStartTime)?", required: false, width: 12, submitOnChange:true
        }
        displayInfo("Saturation is the amount of color. Percent from 1 to 100, where 1 is hardly any and 100 is maximum amount.")
    }
    if(hueOn > 100) errorMessage("Beginning hue can't be more than 100. Correct before saving.")
    if(hueOff && hueOff > 100) errorMessage("Ending hue can't be more than 100. Correct before saving.")
    if(satOn > 100) errorMessage("Beginning saturation can't be more than 100. Correct before saving.")
    if(satOff && satOff > 100) errorMessage("Ending saturation can't be more than 100. Correct before saving.")
}

def displayModeOption(){
    if(modeEnable){
        if(checkStopTimeEntered && inputStopType != "None"){
            displayLabel("Change Mode $varStartTime and/or $varStopTime")
            input "modeChangeOn", "mode", title: "<b>Change Mode $varStartTime to?</b> (Optional)", required: false, width: 12, submitOnChange:true
// ifmode is supposed to be "If Mode X, then allow run", not "only change mode if Mode is X" - i think?
            input "ifMode", "mode", title: "<b>Only if Mode is already:</b> (Optional)", required: false, width: 12
        } else {
            input "modeChangeOn", "mode", title: "<b>Change Mode $varStartTime to?</b> (Optional)", required: false, width: 6, submitOnChange:true
            input "modeChangeOff", "mode", title: "<b>Change Mode $varStopTime to?</b> (Optional)", required: false, width: 6, submitOnChange:true
            input "ifMode", "mode", title: "<b>Only if Mode is already:</b> (Optional)", required: false, width: 12
        }
    }
}

/* def buildMessage(){
    // doesnt work with before/after sunrise/sunset
    // maybe say "null" if no device has been selected yet
    // build message
    if(timeDevice && !varStartTime){
        message = "With $timeDevice"
    } else if(varStartTime){
        message = "$varStartTime"
        if(timeDays) message = "$message on $timeDays"
        if(ifMode) message = "$message, if mode is $ifMode"
        if(modeChangeOn) {
            if(ifMode) message = "$message, then set mode to $modeChangeOn"
            if(!ifMode) message = "$message, set mode to $modeChangeOn"
        }
        if(timeOn == "On") {
            if(modeChangeOn) message = "$message, and turn on $timeDevice"
            if(!modeChangeOn) message = "$message, turn on $timeDevice"
        } else if(!timeOn){
            if(modeChangeOn) message = "$message, and if $timeDevice is on"
            if(!modeChangeOn) message = "$message, if $timeDevice is on"
        }
        if(levelOn){
            if(timeOn) message = "$message and set level to $levelOn"
            if(!timeOn) message = "$message, set level to $levelOn"
        }
        if(tempOn){
            if(timeOn) message = "$message with temperature $tempOn"
            if(!timeOn && levelOn && !hueOn && !satOn) message = "$message and temperature to $tempOn"
            if(!timeOn && levelOn && (hueOn || satOn)) message = "$message, temperature to $tempOn"
            if(!timeOn && !levelOn) message = "$message, then set temperature to $tempOn"
        }
        if(hueOn){
            if(timeOn && (levelOn || tempOn)) message = "$message, and hue1 $hueOn"
            if(timeOn && !levelOn && !tempOn && satOn) message = "$message, hue2 $hueOn"
            if(timeOn && !levelOn && !tempOn && !satOn) message = "$message, and hue3 $hueOn"
            if(!timeOn && (levelOn || tempOn)) message = "$message, hue4 $tempOn"
            if(!timeOn && !levelOn && !tempOn) message = "$message, then set hue5 to $hueOn"
        }
        if(satOn){
            if(levelOn || tempOn || hueOn) message = "$message, and saturation $hueOn"
            if(!levelOn && !tempOn && !satOn) message = "$message, then set saturation to $hueOn"
        }
        if(varStopTime && (levelOff || tempOff || hueOff || satOff || modeChangeOff)){
            message = "$message, then"
            if(levelOff && !tempOff && !hueOff && !satOff){
                if(levelOff < levelOn) message = "$message dim to $levelOff until $varStopTime"
                if(levelOff > levelOn) message = "$message brigten to $levelOff until $varStopTime"
            } else if(levelOff || tempOff || hueOff || satOff){
                message = "$message change "
                if(levelOff) message = "$message level to $levelOff"
                if(tempOff){
                    if(!levelOff) message = "$message temperature to $tempOff"
                    if(levelOff && !hueOff && !satOff) message = "$message and temperature to $tempOff"
                    if(levelOff && (hueOff || satOff)) message = "$message, temperature to $tempOff"
                }
                if(hueOff){
                    if(!levelOff && !tempOff) message = "$message hue to $hueOff"
                    if((levelOff || tempOff) && satOff) message = "$message and hue to $hueOff"
                    if((levelOff || tempOff) && !satOff) message = "$message, hue to $hueOff"
                }
                message = "$message until $varStopTime"
            }
            if(inputStopOff && (levelOff || tempOff || hueOff || satOff) && !modeChangeOff) message = "$message, and $inputStopOff"
            if(inputStopOff && (levelOff || tempOff || hueOff || satOff) && modeChangeOff) message = "$message, $inputStopOff"
            if(inputStopOff && !levelOff && !tempOff && !hueOff && !satOff) message = "$message, then $inputStopOff"
            if(modeChangeOff && (levelOff || tempOff || hueOff || satOff)) message = "$message, and set mode to $modeChangeOff"
            if(modeChangeOff && !levelOff && !tempOff && !hueOff && !satOff) message = "$message, then set mode to $modeChangeOff"
        }


        // toggle and time off
    }
    if(!varStartTime || $timeDevice || (varStartTime && !levelOn && !hueOn && !satOn)){
        message = "$message ..."
    } else {
        message = "$message."
    }
} */


/* ********************** */
/* End display functions. */
/* ********************** */


def installed() {
	logTrace(603, "Installed")

    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
	initialize()
}

def updated() {
	logTrace(610,"Updated")
	initialize()
}

def initialize() {
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
	unschedule()
	if(disableAll || disable) {
		if(disableAll) state.disable = true
	} else {
		state.disable = false
        timeStart = getStartTime()
        timeStop = getStopTime()
		if(timeStart) initializeSchedules()
	}
	logTrace(625,"Initialized")
}

def getStartTime(){
	if(!inputStartType) return false
    if(inputStartType == "None") return false
    if(inputStartType == "Time"){
        if(!inputStartTime) {
            logTrace(633,"ERROR: inputStartType set to Time, but no inputStartTime entered")
            return false
        } else {
            value = inputStartTime
        }
    } else if(inputStartType == "Sunrise"){
        if(!inputStartSunriseType) {
            logTrace(640,"ERROR: inputStartType set as Sunrise, but no inputStartSunriseType selected")
            return false
        } else {
            value = (inputStartSunriseType == "Before" ? parent.getSunrise(inputStartBefore * -1,app.label) : parent.getSunrise(inputStartBefore,app.label))
        }
    } else if(inputStartType == "Sunset"){
        if(!inputStartSunriseType) {
            logTrace(647,"ERROR: inputStartType set as Sunset, but no inputStartSunriseType selected")
            return false
        } else {
            value = (inputStartSunsetType == "Before" ? parent.getSunset(inputStartBefore * -1,app.label) : parent.getSunset(inputStartBefore,app.label))
        }
    } else {
        return false
    }
    logTrace(655,"Start time set as " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", value).format("h:mma MMM dd, yyyy", location.timeZone))
    return value
}

def getStopTime(){
	if(!inputStopType) return false
    if(inputStopType == "None") return false
    if(inputStopType == "Time"){
        if(!inputStopTime) {
            logTrace(664,"ERROR: inputStopType set to Time, but no inputStopTime entered")
            return false
        } else {
            value = inputStopTime
        }
    } else if(inputStopType == "Sunrise"){
        if(!inputStopSunriseType) {
            logTrace(671,"ERROR: inputStopType set as Sunrise, but no inputStopSunriseType selected")
            return false
        } else {
            value = (inputStopSunriseType == "Before" ? parent.getSunrise(inputStopBefore * -1,app.label) : parent.getSunrise(inputStopBefore,app.label))
        }
    } else if(inputStopType == "Sunset"){
        if(!inputStopSunriseType) {
            logTrace(678,"ERROR: inputStopType set as Sunset, but no inputStopSunriseType selected")
            return false
        } else {
            value = (inputStopSunriseType == "Before" ? parent.getSunset(inputStopBefore * -1,app.label) : parent.getSunset(inputStopBefore,app.label))
        }
    } else {
        return false
    }
    // If timeStop before timeStart, add a day
    if(timeToday(timeStart, location.timeZone).time > timeToday(value, location.timeZone).time) value = parent.getTomorrow(value,app.label)
    logTrace(688,"Stop time set as " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", value).format("h:mma MMM dd, yyyy", location.timeZone))
    return value
}

def dimSpeed(){
	if(settings.multiplier != null){
		logTrace(694,"dimSpeed set to $settings.multiplier")
		return settings.multiplier
	}else{
		logTrace(697,"dimSpeed set to 1.2")
		return 1.2
	}
}

def getDefaultLevel(device){
	// Set map with fake values
	defaults=[level:'Null',temp:'Null',hue:'Null',sat:'Null']

	// If no device match, return null
	timeDevice.findAll( {it.id == device.id} ).each {
		logTrace(708,"getDefaultLevel matched device $device and $it")
		match = true
	}
	if(!match) return defaults

	// if no start levels, return nulls
    if(!levelOn && !tempOn && !hueOn && !satOn){
		logTrace(715,"Returning null as start level for $device")
		return defaults
	}

	// if not between start and stop time, return nulls
	if(timeStop && !parent.timeBetween(timeStart, timeStop, app.label)) return defaults

	// If disabled, return nulls
	if(disable || state.disableAll) {
		logTrace(724,"Default level for $device null, schedule disabled")
		return defaults
	}

	// If mode set and node doesn't match, return nulls
	if(ifMode){
		if(location.mode != ifMode) {
			logTrace(731,"Default level for $device null, mode $ifMode")
			return defaults
		}
	}

	// If not correct day, return nulls
	if(timeDays && !parent.todayInDayList(timeDays,app.label)) return defaults

	// Get current level
	currentLevel = device.currentLevel
	currentTemp = device.currentColorTemperature
	currentHue = device.currentHue
	currentSat = device.currentSaturation
    
    if(levelEnable && levelOn && (!timeStop || !levelOff || levelOn == levelOff)) defaults.put("level",levelOn)
    if(tempEnable && tempOn && (!timeStop || !tempOff || tempOn == tempOff)) defaults.put("temp", tempOn)
    if(colorEnable && hueOn && (!timeStop || !hueOff || hueOn == hueOff)) defaults.put("hue", hueOn)
    if(colorEnable && satOn && (!timeStop || !satOff || satOn == satOff)) defaults.put("sat", satOn)
    
    // If there's no stop time, or
    // There's no starting levels, or
    // There's no ending levels, or
    // No changes are enabled, then we're done
    if(!timeStop || ((!levelEnable || !levelOn || !levelOff || levelOn == levelOff) && (!tempEnable || !tempOn || !tempOff || tempOn == tempOff) && (!colorEnable && (!hueOn || !hueOff || hueOn == hueOff) && (!satOn || !satOff || satOn == satOff)))){
        // First, correct potential fan level (will correct below, so do it within if statement)
        if(parent.isFan(device,app.label) && defaults.level != "Null") defaults.put("level",roundFanLevel(defaults.level))

		logTrace(758,"Default level $defaults for $device")
        return defaults
    }
   
    // Calculate percent of schedule elapsed
    totalHours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).hours - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).hours
    totalMinutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).minutes - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).minutes
    totalSeconds = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).seconds - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).seconds + totalMinutes * 60 + totalHours * 60 * 60
    //Second calculate the amount of time elapsed from the beginning of the schedule (in seconds)
    elapsedHours = new Date().hours - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).hours
    elapsedMinutes = new Date().minutes - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).minutes
    elapsedSeconds = new Date().seconds - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).seconds + elapsedMinutes * 60 + elapsedHours * 60 * 60
    //Divide for percentage of time expired (avoid div/0 error)
    if(elapsedSeconds == 0){
        percentTimeExpired = 0
    } else {
        percentTimeExpired = elapsedSeconds / totalSeconds
    }

    // Calculate proportiant level
    if(levelEnable && levelOff && levelOn && levelOn != levelOff) {
        if(levelOff > levelOn){
            defaults.put("level", (levelOff - levelOn) * percentTimeExpired + levelOn as int)
        } else {
            defaults.put("level", levelOn - (levelOn - levelOff) * percentTimeExpired as int)
        }
    }

    //Calculate proportiant temp
    if(tempEnable && tempOff && tempOn && tempOn != tempOff) {
        if(tempOff > tempOn){
            defaults.put("temp", (tempOff - tempOn) * percentTimeExpired + tempOn as int)
        } else {
            defaults.put("temp", tempOn - (tempOn - tempOff) * percentTimeExpired as int)
        }
    }

    //Calculate proportiant hue
    if(colorEnable && hueOff && hueOn && hueOn != hueOff) {
        // hueOn=25, hueOff=75, going 25, 26...74, 75
        if(hueOff > hueOn && hueDirection == "Forward"){
            defaults.put("hue", (hueOff - hueOn) * percentTimeExpired + hueOn as int)
            // hueOn=25, hueOff=75, going 25, 24 ... 2, 1, 100, 99 ... 76, 75
        } else if(hueOff > hueOn && hueDirection == "Reverse"){
            defaults.put("hue", hueOn - (100 - hueOff + hueOn)  * percentTimeExpired as int)
            if(defaults.hue < 1) defaults.put("hue", defaults.hue + 100)
            //hueOn=75, hueOff=25, going 75, 76, 77 ... 99, 100, 1, 2 ... 24, 25
        } else if(hueOff < hueOn && hueDirection == "Forward"){
            defaults.put("hue", (100 - hueOn + hueOff)  * percentTimeExpired + hueOn as int)
            if(defaults.hue > 100) defaults = [hue: defaults.hue - 100]
            //hueOn=75, hueOff=25, going 75, 74 ... 26, 25
        } else if(hueOff < hueOn && hueDirection == "Reverse"){
            defaults.put("hue", hueOn - (hueOn - hueOff) * percentTimeExpired as int)
        }
    }

    //Calculate proportiant sat
    if(colorEnable && satOff && satOn && satOn != satOff) {
        if(satOff > satOn){
            defaults.put("sat", (satOff - satOn) * percentTimeExpired + satOn as int)
        } else {
            defaults.put("sat", (100 - satOn + satOff) * percentTimeExpired + satOn as int)
            if(defaults.sat > 100) defaults.put("sat", defaults.sat - 100)
        }
    }

    //Correct potential fan level
    if(parent.isFan(device,app.label) && defaults.level != "Null") defaults.put("level",roundFanLevel(defaults.level))
    
    logTrace(827,"Default levels $defaults for $device")
    return defaults
}

// Schedule initializer
//Called from initialize, runDayOnSchedule, and runDayOffSchedule
def initializeSchedules(){
	unschedule()

	if(!timeStart) timeStart = getStartTime()
	if(!timeStop) timeStop = getStopTime()

	// If disabled, return null
	if(disable || state.disableAll) {
		logTrace(841,"initializeSchedules returning; schedule disabled")
		return
	}

	//First, schedule dayOn, either every day or with specific days
    	if(timeDays) weekDays = weekDaysToNum()
	hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
	minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
	if(weekDays) {
		logTrace(850,"Scheduling runDayOnSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours ? * $weekDays)")
		schedule("0 " + minutes + " " + hours + " ? * " + weekDays, runDayOnSchedule)
	} else {
		logTrace(853,"Scheduling runDayOnSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours * * ?)")
		schedule("0 " + minutes + " " + hours + " * * ?", runDayOnSchedule)
	}

	//Second, schedule dayOff, either every day or with specific days
	if(timeStop){
		hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('HH').toInteger()
		minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('mm').toInteger()
		//Already set weekDays above
		if(weekDays) {
			logTrace(863,"Scheduling runDayOffSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours ? * $weekDays)")
			schedule("0 " + minutes + " " + hours + " ? * " + weekDays, runDayOffSchedule)
		} else {
			logTrace(866,"Scheduling runDayOffSchedule " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours * * ?)")
			schedule("0 " + minutes + " " + hours + " * * ?", runDayOffSchedule)
		}
	}

	//Third, immediatly run incremental (which will self-reschedule thereafter)
	if(timeStop && parent.timeBetween(timeStart, timeStop, app.label)) {
		if((levelOn && levelOff && levelOn != levelOff) || (tempOn && tempOff && tempOn != tempOff) || (hueOn && hueOff && hueOn != hueOff) || (satOn && satOff && satOn != satOff))
			incrementalSchedule()
	}
}

//Sets schedule for runIncrementalSchedule
//Called from initializeSchedules and parent.reschedule
def incrementalSchedule(){
    if(!timeStart) timeStart = getStartTime()
    if(!timeStop) timeStop = getStopTime()

	// If disabled, return null
	if(disable || state.disableAll) {
		logTrace(886,"Function incrementalSchedule returning; schedule disabled")
		return
	}

	if(!parent.multiStateOn(timeDevice)){
		logTrace(891,"Since $timeDevice is off, stopping recurring schedules")
		return
	}

	// Check if correct day and time just so we don't keep running forever
	if(timeDays && !parent.todayInDayList(timeDays,app.label)) return

	// If mode set and node doesn't match, return null
	if(ifMode && location.mode != ifMode) {
		logTrace(900,"incrementalSchedule returning, mode $ifMode")
		return
	}

	// If between start and stop time (if start time after stop time, then if after start time)
	if(parent.timeBetween(timeStart, timeStop, app.label)){        
		// Run first iteration now
		runIncrementalSchedule()
//TO-DO: Figure out how long between each change, and schedule for that duration, rather than every X seconds.
//TO-DO: Add state variable setting for minimum duration
//TO-DO: Add warning on setup page if minimum duration is too low (override it?)
		runIn(20,incrementalSchedule)
		logTrace(912,"Scheduling incrementalSchedule for 20 seconds")
		return true
	} else {
		logTrace(915,"Schedule ended; now after $timeStop")
	}
}

// Performs actual changes for incremental schedule
// Called only by schedule set in incrementalSchedule
def runIncrementalSchedule(){
	// Loop through devices
	timeDevice.each{
		// Ignore devices that aren't on
		if(parent.stateOn(it,app.label)){
			// Set level
			defaults = getDefaultLevel(it)

			if(defaults.level != "Null") parent.setToLevel(it,defaults.level,app.label)

			// Set temp
			if(defaults.temp != "Null"){
				currentTemp = it.currentColorTemperature
				if(defaults.temp - currentTemp > 3 || defaults.temp - currentTemp < -3) parent.singleTemp(it,defaults.temp,app.label)
			}

			// If either Hue or Sat, but not both, set the other to current
			if(defaults.hue != "Null" || defaults.sat != "Null") parent.singleColor(it,defaults.hue,defaults.sat,app.label)
		}
	}
}

//Returns true if schedule turns on
//used for when Pico resumes schedule
def returnTimeOn(){
    if(timeOn) return true
}

// Performs actual changes at time set with timeOn
// Called only by schedule set in incrementalSchedule
def runDayOnSchedule(){
	if(disable || state.disableAll) return

	// if mode doesn't match, return
	if(ifMode){
		if(location.mode != ifMode) return
	}
	if(modeChangeOn) setLocationMode(modeChangeOn)
	if(timeOn == "On"){
		parent.multiOn(timeDevice,app.label)
	} else if(timeOn == "Off"){
		parent.multiOff(timeDevice,app.label)
	} else if(timeOn == "Toggle"){
		parent.toggle(timeDevice,app.label)
	//Set initial levels, at beginning of schedule if it's not turning on/off
	} else {
		// Don't need to set levels, since initializeSchedules will do that
	}
	//Reschedule everything
	initializeSchedules()
}

// Performs actual changes at time set with timeOff
// Called only by schedule set in incrementalSchedule
def runDayOffSchedule(){
	if(timeDisable || state.disableAll) return

	// if mode return
	if(ifMode){
		if(location.mode != ifMode) return
	}
	if(modeChangeOff) setLocationMode(modeChangeOff)
	if(timeOff == "On"){
	   parent.multiOn(timeDevice,app.label)
	} else if(timeOff == "Off"){
	   parent.multiOff(timeDevice,app.label)
	} else if(timeOff == "Toggle"){
	   parent.toggle(timeDevice,app.label)
	}
	//Reschedule everything
	initializeSchedules()
}

def weekDaysToNum(){
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
	logTrace(1023,"weekDaysToNum returning $dayString")
	return dayString
}

def logTrace(lineNumber,message = null){
    if(message) {
	    log.trace "$app.label (line $lineNumber) -- $message"
    } else {
        log.trace "$app.label (line $lineNumber)"
    }
}
