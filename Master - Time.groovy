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
*  Version: 0.3.15
*
***********************************************************************************************************************/

definition(
    name: "Master - Time",
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
            if(disableAll){
                input "disableAll", "bool", title: "<b>All schedules are disabled.</b> Reenable?", defaultValue: false, submitOnChange:true
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
                input "disable", "bool", title: "<b><font color=\"#000099\">Schedule is disabled.</font></b> Reenable it?", submitOnChange:true
            }

            //if not disabled, then show everything
            if(!state.disable && !disable ){
                displayNameOption()
                //if no label, stop
                if(app.label){
                    displayDevicesOption()
                    //if no devices, stop
                    if(timeDevice){
                        input "disable", "bool", title: "Schedule is enabled. Disable it?", submitOnChange:true
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
                            inputStartBefore = null
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
                                    inputStopTime = null
                                    inputStopSunriseType = null
                                    inputStopBefore = null
                                }

                                if(checkStopTimeEntered() && inputStopType != "None"){
                                    varStopTime = getStopTimeVariables()
                                    input "timeOff", "enum", title: "Turn devices on or off ($varStopTime)?", multiple: false, required: false, width: 12, options: ["None": "Don't turn on or off (leave as is)","On": "Turn On", "Off": "Turn Off", "Toggle": "Toggle (if on, turn off, and if off, turn on)"], submitOnChange:true
                                }

                                if(inputStopType == "None" || timeOff){
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
        if(inputStopType == "Time" && inputStopTime) return true
        if((inputStopType == "Sunrise" || inputStopType == "Sunset") && inputStopSunriseType == "At") return true
        if((inputStopType == "Sunrise" || inputStopType == "Sunset") && (inputStopSunriseType == "Before" || inputStopSunriseType == "After") && inputStopBefore) return true
    }
}

def displayStopTimeTypeOption(){
    displayLabel("Stop time")
    if(!inputStopType || inputStopType == "None"){
        width = 12
    } else if(inputStopType == "Time" || !inputStopSunriseType || inputStopSunriseType == "At"){
        width = 6
    } else if(inputStopSunriseType){
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
    if(inputStopSunriseType == "Before"){
        input "inputStopBefore", "number", title: "Minutes before sunrise:", required: false, width: 4, submitOnChange:true
    } else if(inputStopSunriseType == "After"){
        input "inputStopBefore", "number", title: "Minutes after sunrise:", required: false, width: 4, submitOnChange:true
    }
    if(inputStopBefore){
        if(inputStopBefore > 1441){
            message = "Minutes "
            if(inputStopSunriseType == "Before"){
                message = message + "before sunrise is "
            } else if (inputStopSunriseType == "After"){
                message = message + "after sunrise is "
            }
            if(inputStopBefore > 2881){
                message = message + Math.floor(inputStartBefore / 60 / 24) + " days"
            } else {
                message = message + "a day"
            }
            message = message + ". That may not work right."
            errorMessage(message)
        }
    }
}

def displayStopSunsetOption(){
    if(!inputStopSunriseType || inputStopSunriseType == "At"){
        width = 6
    } else {
        width = 4
    }
    input "inputStopSunriseType", "enum", title: "At, before or after sunset:", required: false, multiple: false, width: width, options: ["At":"At sunset", "Before":"Before sunset", "After":"After sunset"], submitOnChange:true
    if(inputStopSunriseType == "Before"){
        input "inputStopBefore", "number", title: "Minutes before sunset:", required: false, width: 4, submitOnChange:true
    } else if(inputStopSunriseType == "After"){
        input "inputStopBefore", "number", title: "Minutes after sunset:", required: false, width: 4, submitOnChange:true
    }
}

def getStartTimeVariables(){
    if(inputStartType == "Time" && inputStartTime){
        return "at " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", inputStartTime).format("h:mm a", location.timeZone)
    } else if(inputStartType == "Sunrise"){
            if(inputStartSunriseType == "At"){
                return "at sunrise"
            } else if(inputStartSunriseType == "Before" && inputStartBefore){
                if(inputStartBefore) return "$inputStartBefore minutes before sunrise"
            } else if(inputStartSunriseType == "After" && inputStartBefore){
                if(inputStartBefore) return "$inputStartBefore minutes after sunrise"
            } else {
                return
            }
    } else if(inputStartType == "Sunset" && inputStartSunriseType){
            if(inputStartSunriseType == "At"){
                return "at sunset"
            } else if(inputStartSunriseType == "Before" && inputStartBefore){
                return "$inputStartBefore minutes before sunset"
            } else if(inputStartSunriseType == "After" && inputStartBefore){
                return "$inputStartBefore minutes after sunset"
            } else {
                return
            }
    }
    if(inputStartBefore && inputStartBefore > 1441){
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
            parent.errorMessage(message)
    }
}

def getStopTimeVariables(){
    if(inputStopType == "Time" && inputStopTime){
        return "at " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", inputStopTime).format("h:mm a", location.timeZone)
    } else if(inputStopType == "Sunrise"){
            if(inputStopSunriseType == "At"){
                return "at sunrise"
            } else if(inputStopSunriseType == "Before" && inputStopBefore){
                return "$inputStopBefore minutes before sunrise"
            } else if(inputStopSunriseType == "After" && inputStopBefore){
                return "$inputStopBefore minutes after sunrise"
            } else {
                return
            }
    } else if(inputStopType == "Sunset"){
            if(inputStopSunriseType == "At"){
                return "at sunset"
            } else if(inputStopSunriseType == "Before" && inputStopBefore){
                return "$inputStopBefore minutes before sunset"
            } else if(inputStopSunriseType == "After" && inputStopBefore){
                return "$inputStopBefore minutes after sunset"
            } else {
                return
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
}

def displayBrightnessOption(){    
    if(levelEnable){
        if(inputStopType == "None"){
            displayLabel("Enter default brightness")
            levelOff = null
            input "levelOn", "number", title: "Set brightness ($varStartTime)?", required: false, width: 12, submitOnChange:true
        } else {
            displayLabel("Enter beginning and ending brightness")
            if(levelOn){
                input "levelOn", "number", title: "Beginning brightness ($varStartTime)?", required: false, width: 6, submitOnChange:true
                input "levelOff", "number", title: "and ending brightness ($varStartTime)? (Optional)", required: false, width: 6, submitOnChange:true
            } else {
                input "levelOn", "number", title: "Beginning brightness ($varStartTime)?", required: false, width: 12, submitOnChange:true
            }
        }
        displayInfo("Percentage from 1 to 100.")
    }
    if(levelOn > 100) errorMessage("Beginning brightness can't be more than 100. Correct before saving.")
    if(levelOff > 100) errorMessage("Ending brightness can't be more than 100. Correct before saving.")
}

def displayTemperatureOption(){
    if(tempEnable){
        if(inputStopType == "None"){
            displayLabel("Enter default color temperature")
            tempOff = null
            input "tempOn", "number", title: "Set color temperature ($varStartTime)?", required: false, width: 12, submitOnChange:true
        } else {
            displayLabel("Enter beginning and ending color temperature")
            if(tempOn){
                input "tempOn", "number", title: "Beginning color temperature ($varStartTime)?", required: false, width: 6, submitOnChange:true
                input "tempOff", "number", title: "and ending color temperature ($varStopTime)? (Optional)", required: false, width: 6, submitOnChange:true
            } else {
                input "tempOn", "number", title: "Beginning color temperature ($varStartTime)?", required: false, width: 12, submitOnChange:true
            }
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
        if(inputStopType == "None"){
            displayLabel("Enter default hue and saturation temperature")
            hueOff = null
            satOff = null
            input "hueOn", "number", title: "Set hue ($varStartTime)? (Optional)", required: false, width: 6, submitOnChange:true
            input "satOn", "number", title: "Set saturation ($varStartTime)? (Optional)", required: false, width: 6, submitOnChange:true
            displayInfo("Hue is the shade of color. Number from 1 to 100. Red is 1 or 100. Yellow is 11. Green is 26. Blue is 66. Purple is 73.")
            displayInfo("Saturation is the amount of color. Percent from 1 to 100, where 1 is hardly any and 100 is maximum amount.")
        } else {
            displayLabel("Enter beginning and ending hue and saturation")
            if(hueOn){
                if(hueOff){
                    width = 4
                } else {
                    width = 6
                }
                input "hueOn", "number", title: "Beginning hue ($varStartTime)? (Optional)", required: false, width: width, submitOnChange:true
                input "hueOff", "number", title: "and ending hue ($varStopTime)? (Optional)", required: false, width: width, submitOnChange:true
            } else {
                input "hueOn", "number", title: "Beginning hue ($varStartTime)? (Optional)", required: false, width: 12, submitOnChange:true
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
                input "satOn", "number", title: "Beginning saturation ($varStartTime)? (Optional)", required: false, width: 6, submitOnChange:true
                input "satOff", "number", title: "and ending saturation ($varStopTime)? (Optional)", required: false, width: 6, submitOnChange:true
            } else {
                input "satOn", "number", title: "Beginning saturation ($varStartTime)? (Optional)", required: false, width: 12, submitOnChange:true
            }
            displayInfo("Saturation is the amount of color. Percent from 1 to 100, where 1 is hardly any and 100 is maximum amount.")
        }
    }
    if(hueOn > 100) errorMessage("Beginning hue can't be more than 100. Correct before saving.")
    if(hueOff && hueOff > 100) errorMessage("Ending hue can't be more than 100. Correct before saving.")
    if(satOn > 100) errorMessage("Beginning saturation can't be more than 100. Correct before saving.")
    if(satOff && satOff > 100) errorMessage("Ending saturation can't be more than 100. Correct before saving.")
}

def displayModeOption(){
    if(!checkStopTimeEntered()) return
    if(modeEnable){
        if(inputStopType == "None"){
            displayLabel("Change Mode $varStartTime")
            input "modeChangeOn", "mode", title: "Set Mode (at $varStartTime)?", required: false, width: 6, submitOnChange:true
            input "ifMode", "mode", title: "Only run if Mode is already? (Optional)", required: false, width: 12
        } else {
            displayLabel("Change Mode $varStartTime and/or $varStopTime")
            input "modeChangeOn", "mode", title: "Change Mode (at $varStartTime)?", required: false, width: 12, submitOnChange:true
// ifmode is supposed to be "If Mode X, then allow run", not "only change mode if Mode is X" - i think?
            input "ifMode", "mode", title: "Only run if Mode is already? (Optional)", required: false, width: 12
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

/* ************************************************** */
/*                                                    */
/* End display functions.                             */
/*                                                    */
/* ************************************************** */

def installed() {
	logTrace(631, "Installed")
	app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
	initialize()
}

def updated() {
	logTrace(637,"Updated")
	initialize()
}

def initialize() {
	app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

	unschedule()

	// Clean up settings values
	if(inputStartType != "Sunrise" && inputStartType != "Sunset") {
		settings.inputStartSunriseType = null
		settings.inputStartBefore = null
	}
	if(inputStartSunriseType == "At") settings.inputStartBefore = null
	if(inputStopType != "Sunrise" && inputStopType != "Sunset") {
		settings.inputStopSunriseType = null
		settings.inputStopBefore = null
	}
	if(inputStopSunriseType == "At") settings.inputStopBefore = null
	if(inputStopType == "None"){
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
		if(!setStartStopTime("Start")) return false
		setStartStopTime("Stop")
        state.totalSeconds = Math.floor((Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.stop).time - Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.start).time) / 1000)
		setWeekDays()

		initializeSchedules()
	}
	logTrace(690,"Initialized")
}

def setStartStopTime(type = "Start"){
    if(type == "Start") state.start = null
	if(type == "Stop") state.stop = null

	if(type != "Start" && type != "Stop"){
		logTrace(698,"ERROR: Invalid variable passed to setStartStopTime")
		return
	}

	// If no stop time, exit
	if(type == "Stop" && (!inputStopType || inputStopType == "None")) return

    if(settings["input${type}Type"] == "Time"){
		value = settings["input${type}Time"]
	} else if(settings["input${type}Type"] == "Sunrise"){
		value = (settings["input${type}SunriseType"] == "Before" ? parent.getSunrise(settings["input${type}Before"] * -1,app.label) : parent.getSunrise(settings["input${type}Before"],app.label))
	} else if(settings["input${type}Type"] == "Sunset"){
		value = (settings["input${type}SunriseType"] == "Before" ? parent.getSunset(settings["input${type}Before"] * -1,app.label) : parent.getSunset(settings["input${type}Before"],app.label))
	} else {
		logTrace(712,"ERROR: input" + type + "Type set to " + settings["input${type}Type"])
		return
	}

	if(type == "Stop"){
		if(timeToday(state.start, location.timeZone).time > timeToday(value, location.timeZone).time) value = parent.getTomorrow(value,app.label)
	}
	logTrace(719,"$type time set as " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", value).format("h:mma MMM dd, yyyy", location.timeZone))
	if(type == "Start") state.start = value
	if(type == "Stop") state.stop = value
	return true
}

// Returns array for level, temp, hue and sat
// Should NOT return true or false; always return defaults array
// Array should return text "Null" for null values
def getDefaultLevel(device){
	// Set map with fake values
	defaults=[level:'Null',temp:'Null',hue:'Null',sat:'Null']

	// If no device match, return nulls
	timeDevice.findAll( {it.id == device.id} ).each {
		//logTrace(734,"getDefaultLevel matched device $device and $it")
		match = true
	}
	if(!match) return defaults

	// If there's a matching device, check and set state variables
	if(!state.start) {
		if(!setStartStopTime("Start")) return defaults
	}
	if(!state.stop) setStartStopTime("Stop")
	if(!state.totalSeconds) setTotalSeconds()

	// if no start levels, return nulls
	if(!levelOn && !tempOn && !hueOn && !satOn){
		logTrace(748,"No starting levels set for $device")
		return defaults
	}

	// If disabled, return nulls
	if(disable || state.disable) {
		logTrace(754,"Default level for $device null, schedule disabled")
		return defaults
	}

	// If mode set and node doesn't match, return nulls
	if(ifMode){
		if(location.mode != ifMode) {
			logTrace(761,"Default level for $device null, mode $ifMode")
			return defaults
		}
	}

	// If not correct day, return nulls
	if(timeDays && !parent.todayInDayList(timeDays,app.label)) return defaults

	// If there's a stop time with stop settings (possible, since could be changing mode)
	if(state.stop && (levelOff || tempOff || hueOff || satOff)){
		// if not between start and stop time, return nulls
		if(!parent.timeBetween(state.start, state.stop, app.label)) return defaults

		elapsedPercent = getElapsedPercent()
		if(!elapsedPercent) {
			logTrace(776,"ERROR: Unable to calculate elapsed time with start \"$state.start\" and stop \"$state.stop\"")
			return defaults
		}

		// If only level on, use on value
		if(levelOn && (!state.stop || !levelOff)) {
			defaults.put("level",levelOn)
		// Otherwise, calculate proportiant level to elapsed time
		} else if(levelOn){
			if(levelOff > levelOn){
				defaults.put("level", (levelOff - levelOn) * elapsedPercent + levelOn as int)
			} else {
				defaults.put("level", levelOn - (levelOn - levelOff) * elapsedPercent as int)
			}
		}

		// Calculate temp same as level
		if(tempOn && (!state.stop || !tempOff)){
			defaults.put("temp", tempOn)
		} else if(tempOn){
		        if(tempOff > tempOn){
		            defaults.put("temp", (tempOff - tempOn) * elapsedPercent + tempOn as int)
		        } else {
		            defaults.put("temp", tempOn - (tempOn - tempOff) * elapsedPercent as int)
		        }
		}

		// Calculate hue, using "direction"
		if(hueOn && (!state.stop || !hueOff)) {
			defaults.put("hue", hueOn)
		} else if(hueOn){
			// hueOn=25, hueOff=75, going 25, 26...74, 75
			if(hueOff > hueOn && hueDirection == "Forward"){
				defaults.put("hue", (hueOff - hueOn) * elapsedPercent + hueOn as int)
			// hueOn=25, hueOff=75, going 25, 24 ... 2, 1, 100, 99 ... 76, 75
			} else if(hueOff > hueOn && hueDirection == "Reverse"){
				defaults.put("hue", hueOn - (100 - hueOff + hueOn)  * elapsedPercent as int)
				if(defaults.hue < 1) defaults.put("hue", defaults.hue + 100)
			//hueOn=75, hueOff=25, going 75, 76, 77 ... 99, 100, 1, 2 ... 24, 25
			} else if(hueOff < hueOn && hueDirection == "Forward"){
				defaults.put("hue", (100 - hueOn + hueOff)  * elapsedPercent + hueOn as int)
				if(defaults.hue > 100) defaults = [hue: defaults.hue - 100]
			//hueOn=75, hueOff=25, going 75, 74 ... 26, 25
			} else if(hueOff < hueOn && hueDirection == "Reverse"){
				defaults.put("hue", hueOn - (hueOn - hueOff) * elapsedPercent as int)
			}
		}

		// Calculate Sat same as level
		if(satOn && (!state.stop || !satOff)) {
			defaults.put("sat", satOn)
		} else if(satOn) {
			if(satOff > satOn){
				defaults.put("sat", (satOff - satOn) * elapsedPercent + satOn as int)
			} else {
				defaults.put("sat", (100 - satOn + satOff) * elapsedPercent + satOn as int)
				if(defaults.sat > 100) defaults.put("sat", defaults.sat - 100)
			}
		}

	// If there's no stop time or no ending levels, use start levels
	// This prevents having to calc elapsedTime when it's not needed
	} else if(!state.stop || (!levelOff && !tempOff && !hueOff && !satOff)){
		if(levelOn) defaults.put("level",levelOn)
		if(tempOn) defaults.put("temp", tempOn)
		if(hueOn) defaults.put("hue", hueOn)
		if(satOn) defaults.put("sat", satOn)
	}

	// Round potential fan level
	if(parent.isFan(device,app.label) && defaults.level != "Null") defaults.put("level",roundFanLevel(defaults.level))

	logTrace(848,"Default levels $defaults for $device")
	return defaults
}

// Schedule initializer
// Called from initialize, runDayOnSchedule, and runDayOffSchedule
def initializeSchedules(){
	// Clear existing schedules
	unschedule()

	// If disabled, return null
	if(state.disable) {
		logTrace(860,"initializeSchedules returning; schedule disabled")
		return
	}

	// First, schedule dayOn, either every day or with specific days
	hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.start).format('HH').toInteger()
	minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.start).format('mm').toInteger()
    
	if(state.weekDays) {
		logTrace(869,"Scheduling runDayOnSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.start).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours ? * $state.weekDays)")
		schedule("0 " + minutes + " " + hours + " ? * " + state.weekDays, runDayOnSchedule)
	} else {
		logTrace(872,"Scheduling runDayOnSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.start).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours * * ?)")
		schedule("0 " + minutes + " " + hours + " * * ?", runDayOnSchedule)
	}

	// Second, schedule dayOff, either every day or with specific days
	if(state.stop){
		hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.stop).format('HH').toInteger()
		minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.stop).format('mm').toInteger()
		if(state.weekDays) {
			logTrace(881,"Scheduling runDayOffSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.stop).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours ? * $state.weekDays)")
			schedule("0 " + minutes + " " + hours + " ? * " + state.weekDays, runDayOffSchedule)
		} else {
			logTrace(884,"Scheduling runDayOffSchedule " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.stop).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours * * ?)")
			schedule("0 " + minutes + " " + hours + " * * ?", runDayOffSchedule)
		}
	}

	// Third, immediatly run incremental (which will self-reschedule thereafter)
	if(state.stop && parent.timeBetween(state.start, state.stop, app.label)) {
		if((levelOn && levelOff) || (tempOn && tempOff) || (hueOn && hueOff) || (satOn && satOff))
			incrementalSchedule()
	}
}

// Sets the schedule for runIncrementalSchedule
// Called from initializeSchedules and parent.reschedule
def incrementalSchedule(){
	//Since could be called by parent, get start and stop times
	if(!state.start) {
		if(!setStartStopTime("Start")) return
	}
	// Stop time required for incremental schedule; otherwise, there are no increments
	if(!state.stop) {
		if(!setStartStopTime("Stop")) return
	}

	// If disabled, return null
	if(disable || state.disable) {
		// logTrace(910,"Function incrementalSchedule returning; schedule disabled")
		return
	}

	// Check if correct day
	if(timeDays && !parent.todayInDayList(timeDays,app.label)) return

	// Check if correct mode
	if(ifMode && location.mode != ifMode) {
		// logTrace(919,"incrementalSchedule returning, mode $ifMode")
		return
	}

	// If between start and stop time (if start time after stop time, then if after start time)
	if(parent.timeBetween(state.start, state.stop, app.label)){
		// Check if device(s) are on
		if(!parent.multiStateOn(timeDevice)){
			// logTrace(927,"Since $timeDevice is off, stopping recurring schedules")
			return
		}

		// Run first iteration now
		runIncrementalSchedule()
//TO-DO: Figure out how long between each change, and schedule for that duration, rather than every X seconds.
//TO-DO: Add state variable setting for minimum duration
//TO-DO: Add warning on setup page if minimum duration is too low (override it?)
		runIn(20,incrementalSchedule)
		// logTrace(937,"Scheduling incrementalSchedule for 20 seconds")
		return true
	} else {
		// logTrace(940,"Schedule ended; now after $state.stop")
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
				// Only change temp by increments of 3
				if(defaults.temp - currentTemp > 3 || defaults.temp - currentTemp < -3) parent.singleTemp(it,defaults.temp,app.label)
			}

			// If either Hue or Sat, but not both, set the other to current
			if(defaults.hue != "Null" || defaults.sat != "Null") parent.singleColor(it,defaults.hue,defaults.sat,app.label)
		}
	}
}

// Performs actual changes at time set with timeOn
// Called only by schedule set in incrementalSchedule
def runDayOnSchedule(){
	if(disable || state.disable) return

	// if mode doesn't match, return
	if(ifMode && location.mode != ifMode) return

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
	// If no start time, exit
	if(stop.stop) return

	if(disable || state.disable) return

	// if mode return
	if(ifMode && location.mode != ifMode) return
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
	logTrace(1047,"weekDaysToNum returning $dayString")
	state.weekDays = dayString
	return true
}

// Calculates duration of the schedule in seconds, and sets as state.totalSeconds
// Only called by initialize
def setTotalSeconds(){
	if(!state.start || !state.stop) {
		state.totalSeconds = null
		return false
	}

	// Calculate duration of schedule


    logTrace(1063,"Schedule total seconds is $totalTime")
	state.totalSeconds = Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.stop).time - Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.start).time / 1000
	return true
}

// Returns percentage of schedule that has elapsed
// Only called by getDefaultLevel
def getElapsedPercent(){
    if(!state.totalSeconds) return false

	// If not between start and stop time, exit
    if(!parent.timeBetween(state.start, state.stop, app.label)) return false

    elapsedPercent = Math.floor((new Date().time - Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.start).time) / 1000)
    
	//Divide for percentage of time expired (avoid div/0 error)
    if(elapsedPercent < 1){
		elapsedPercent = 0
	} else {
		elapsedPercent = Math.floor(elapsedPercent / state.totalSeconds * 100)
	}
    logTrace(1084,"$elapsedPercent% has elapsed in the schedule")
    return elapsedPercent / 100
}

def logTrace(lineNumber,message = null){
    if(message) {
	    log.trace "$app.label (line $lineNumber) -- $message"
    } else {
        log.trace "$app.label (line $lineNumber)"
    }
}
