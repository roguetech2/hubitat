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
*  for more details.
*
*  You should have received a copy of the GNU General Public License along with this program.
*  If not, see <http://www.gnu.org/licenses/>.
*
*  Name: Master
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master - Time.groovy
*  Version: 0.3.3
*
***********************************************************************************************************************/

/* ************************************************** */
/* TO-DO: Fix schedule not triggering (if already on? */
/* ************************************************** */
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
                        if(timeStartType == "Time"){
                            displayStartTimeOption()
                        } else if(timeStartType == "Sunrise"){
                            displayStartSunriseOption()
                        } else if(timeStartType == "Sunset"){
                            displayStartSunsetOption()
                        } else {
                            timeStart = null
                            startSunriseType = null
                            startSunsetType = null
                        }
                        // if not start time entered, stop
                        if(checkStartTimeEntered()){

                            varStartTime = getStartTimeVariables()
                            
                            input "timeOn", "enum", title: "Turn devices on or off ($varStartTime)?", multiple: false, required: false, width: 12, options: ["None": "Don't turn on or off (leave as is)","On": "Turn On", "Off": "Turn Off", "Toggle": "Toggle (if on, turn off, and if off, turn on)"], submitOnChange:true
                            if(timeOn){
                                
                                // TO-DO: Add option for on or not on holidays
                                displayStopTimeTypeOption()
                                if(timeStopType == "Time"){
                                    displayStopTimeOption()
                                } else if(timeStopType == "Sunrise"){
                                    displayStopSunriseOption()
                                } else if(timeStopType == "Sunset"){
                                    displayStopSunsetOption()
                                } else {
                                    timeStop = null
                                    stopSunriseType = null
                                    stopSunsetType = null
                                }
                                if(checkStopTimeEntered() && timeStopType){
                                    if((timeStopType == "Time" && timeStop) || ((timeStopType == "Sunrise" || timeStopType == "Sunset") && (stopSunriseType == "At" || timeStopOffset))){
                                        varStopTime = getStopTimeVariables()
                                        input "timeOff", "enum", title: "Turn devices on or off ($varStopTime)?", multiple: false, required: false, width: 12, options: ["None": "Don't turn on or off (leave as is)","On": "Turn On", "Off": "Turn Off", "Toggle": "Toggle (if on, turn off, and if off, turn on)"], submitOnChange:true
                                    }
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
            }
        }
    }
}


// Display functions

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
}

def displayDevicesOption(){
    displayLabel("Select which devices to schedule")
    input "timeDevice", "capability.switch", title: "Device(s)?", multiple: true, required: true, submitOnChange:true
}

def displayStartTimeTypeOption(){
    displayLabel("Start time")
    if(timeStartType != "Sunrise") {
        startSunriseType = null
        timeStartBeforeSunrise = null
        timeStartAfterSunrise = null
    }
    if(timeStartType != "Sunset") {
        startSunsetType = null
        timeStartBeforeSunset = null
        timeStartAfterSunset = null
    }
    input "timeDays", "enum", title: "On these days (Optional):", required: false, multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"], submitOnChange:true
    if(!timeStartType){
        width = 12
    } else if(timeStartType == "Time" || (!startSunriseType && !startSunsetType) || startSunriseType == "At" || startSunsetType == "At"){
        width = 6
    } else if(startSunriseType || startSunsetType){
        width = 4
    }
    input "timeStartType", "enum", title: "Start Time:", required: false, multiple: false, width: width, options: ["Time":"Start at specific time", "Sunrise":"Sunrise (at, before or after)","Sunset":"Sunset (at, before or after)" ], submitOnChange:true
}

def displayStartTimeOption(){
    input "timeStart", "time", title: "Start time", required: false, width: 6, submitOnChange:true
}

def displayStartSunriseOption(){
    if(!startSunriseType || startSunriseType == "At") {
        width = 6 
    } else {
        width = 4
    }
    input "startSunriseType", "enum", title: "At, before or after sunrise:", required: false, multiple: false, width: width, options: ["At":"At sunrise", "Before":"Before sunrise", "After":"After sunrise"], submitOnChange:true
    if(startSunriseType == "Before"){
        input "timeStartBeforeSunrise", "number", title: "Minutes before sunrise:", required: false, width: 4, submitOnChange:true
        // set timeoffset
        timeStartOffset = timeStartBeforeSunrise
        timeStartOffsetNegative = true
    } else if(startSunriseType == "After"){
        input "timeStartAfterSunrise", "number", title: "Minutes after sunrise:", required: false, width: 4, submitOnChange:true
        //set timeoffset
        timeStartOffset = timeStartAfterSunrise
    }
}

def displayStartSunsetOption(){
    if(!startSunsetType || startSunsetType == "At") {
        width = 6
    } else {
        width = 4
    }
    input "startSunsetType", "enum", title: "At, before or after sunset:", required: false, multiple: false, width: width, options: ["At":"At sunset", "Before":"Before sunset", "After":"After sunset"], submitOnChange:true
    if(startSunsetType == "Before"){
        input "startBeforeSunset", "number", title: "Minutes before sunset:", required: false, width: 4, submitOnChange:true
        // set timeoffset
        timeStartOffset = startBeforeSunset
        timeStartOffsetNegative = true
    } else if(startSunsetType == "After"){
        input "startAfterSunset", "number", title: "Minutes after sunset:", required: false, width: 4, submitOnChange:true
        //set timeoffset
        timeStartOffset = startAfterSunset
    }
}

def checkStartTimeEntered(){
    //check if proper start time has been entered
    if(timeStartType){
        if(timeStartType == "Time" && timeStart) return true
        if(timeStartType == "Sunrise"){
            if(startSunriseType){
                if(startSunriseType == "At"){
                    return true
                } else if(timeStartOffset){
                    return true
                }
            }
        } else if(timeStartType == "Sunset"){
            if(startSunsetType){
                if(startSunsetType == "At"){
                    return true
                } else if(timeStartOffset){
                    return true
               }
            }
        }
    }
}

def checkStopTimeEntered(){
    //check if proper start time has been entered
    if(timeStopType){
        if(timeStopType == "None") return true
        if(timeStopType == "Time" && timeStop) return true
        if(timeStopType == "Sunrise"){
            if(stopSunriseType){
                if(stopSunriseType == "At"){
                    return true
                } else if(timeStopOffset){
                    return true
                }
            }
        } else if(timeStopType == "Sunset"){
            if(stopSunsetType){
                if(stopSunsetType == "At"){
                    return true
                } else if(timeStopOffset){
                    return true
               }
            }
        }
    }
}

def displayStopTimeTypeOption(){
    displayLabel("Stop time")
    if(timeStopType != "Sunrise") {
        stopSunriseType = null
        timeStopBeforeSunrise = null
        timeStopAfterSunrise = null
    }
    if(timeStartType != "Sunset") {
        stopSunsetType = null
        timeStopBeforeSunset = null
        timeStopAfterSunset = null
    }
    if(!timeStopType || timeStopType == "None"){
        width = 12
    } else if(timeStopType == "Time" || (!stopSunriseType && !stopSunsetType) || stopSunriseType == "At" || stopSunsetType == "At"){
        width = 6
    } else if(stopSunriseType || stopSunsetType){
        width = 4
    }
    input "timeStopType", "enum", title: "Stop Time:", required: false, multiple: false, width: width, options: ["None":"Don't stop", "Time":"Stop at specific time", "Sunrise":"Sunrise (at, before or after)","Sunset":"Sunset (at, before or after)" ], submitOnChange:true
}

def displayStopTimeOption(){
    input "timeStop", "time", title: "Stop time", required: false, width: 6, submitOnChange:true
}

def displayStopSunriseOption(){
    if(!stopSunriseType || stopSunriseType == "At"){
        width = 6
    } else {
        width = 4
    }
    input "stopSunriseType", "enum", title: "At, before or after sunrise:", required: false, multiple: false, width: width, options: ["At":"At sunrise", "Before":"Before sunrise", "After":"After sunrise"], submitOnChange:true
    if(stopSunriseType == "Before"){
        input "stopBeforeSunrise", "number", title: "Minutes before sunrise", required: false, width: 4, submitOnChange:true
        // set timeoffset
        timeStopOffset = stopBeforeSunrise
        timeStopOffsetNegative = true
    } else if(stopSunriseType == "After"){
        input "stopAfterSunrise", "number", title: "Minutes after sunrise", required: false, width: 4, submitOnChange:true
        //set timeoffset
        timeStopOffset = stopAfterSunrise
    }
}

def displayStopSunsetOption(){
    if(!stopSunsetType || stopSunsetType == "At"){
        width = 6
    } else {
        width = 4
    }
    input "stopSunsetType", "enum", title: "At, before or after sunset:", required: false, multiple: false, width: width, options: ["At":"At sunset", "Before":"Before sunset", "After":"After sunset"], submitOnChange:true
    if(stopSunsetType == "Before"){
        input "stopBeforeSunset", "number", title: "Minutes before sunset", required: false, width: 4, submitOnChange:true
        // set timeoffset
        timeStopOffset = stopBeforeSunset
        timeStopOffsetNegative = true
    } else if(stopSunsetType == "After"){
        input "stopAfterSunset", "number", title: "Minutes after sunset", required: false, width: 4, submitOnChange:true
        //set timeoffset
        timeStopOffset = stopAfterSunset
    }
}

def getStartTimeVariables(){
    if(timeStartType == "Time" && timeStart){
        return "at " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a", location.timeZone)
    } else if(timeStartType == "Sunrise"){
        if(startSunriseType){
            if(startSunriseType == "At"){
                return "at sunrise"
            } else if(startSunriseType == "Before"){
                return "$timeStartBeforeSunrise minutes before sunrise"
            } else if(startSunriseType == "After"){
                return "$timeStartAfterSunrise minutes after sunrise"
            }
        }
    } else if(timeStartType == "Sunset"){
        if(startSunsetType){
            if(startSunsetType == "At"){
                return "at sunset"
            } else if(startSunsetType == "Before"){
                return "$startBeforeSunset minutes before sunset"
            } else if(startSunsetType == "After"){
                return "$startAfterSunset minutes after sunset"
            }
        }
    }
}

def getStopTimeVariables(){
    if(timeStopType == "Time" && timeStop){
        return "at " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mm a", location.timeZone)
    } else if(timeStopType == "Sunrise"){
        if(stopSunriseType){
            if(stopSunriseType == "At"){
                return "at sunrise"
            } else if(stopSunriseType == "Before"){
                return "$stopBeforeSunrise minutes before sunrise"
            } else if(stopSunriseType == "After"){
                return "$stopAfterSunrise minutes after sunrise"
            }
        }
    } else if(timeStopType == "Sunset"){
        if(stopSunsetType){
            if(stopSunsetType == "At"){
                return "at sunset"
            } else if(stopSunsetType == "Before"){
                return "$stopBeforeSunset minutes before sunset"
            } else if(stopSunsetType == "After"){
                return "$stopAfterSunset minutes after sunset"
            }
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
        if(checkStopTimeEntered && timeStopType != "None"){
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
}

def displayTemperatureOption(){
    if(tempEnable){
        if(checkStopTimeEntered && timeStopType != "None"){
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
}


def displayColorOption(){
    if(colorEnable){
        if(checkStopTimeEntered && timeStopType != "None"){
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
}

def displayModeOption(){
    if(modeEnable){
        if(checkStopTimeEntered && timeStopType != "None"){
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
            if(timeStopOff && (levelOff || tempOff || hueOff || satOff) && !modeChangeOff) message = "$message, and $timeStopOff"
            if(timeStopOff && (levelOff || tempOff || hueOff || satOff) && modeChangeOff) message = "$message, $timeStopOff"
            if(timeStopOff && !levelOff && !tempOff && !hueOff && !satOff) message = "$message, then $timeStopOff"
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
	varStartTime = Math.abs(timeStartOffest)
	logTrace("$app.label (line 604): installed")

    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
	initialize()
}

def updated() {
	logTrace("$app.label (line 611): updated")
	initialize()
}

def initialize() {
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
	unschedule()
	if(disableAll || disable) {
		if(disableAll) state.disable = true
	} else {
		state.disable = false
		initializeSchedules()
	}
	logTrace("$app.label (line 625): Initialized")
}

def dimSpeed(){
	if(settings.multiplier != null){
		return settings.multiplier
	}else{
		logTrace("$app.label (line 632): dimSpeed set to 1.2")
		return 1.2
	}
}

def getDefaultLevel(device){
	// Set map with fake values
	defaults=[level:'Null',temp:'Null',hue:'Null',sat:'Null']

	// If no device match, return null
	timeDevice.findAll( {it.id == device.id} ).each {
		logTrace("$app.label (line 647): getDefaultLevel matched device $device and $it")
		match = true
	}
	if(!match) return defaults

	// if no start time
	if(!timeStart && !timeStartSunrise && !timeStartSunset){
		logTrace("$app.label (line 654): Error: No start time for $device")
		return defaults
	}

	// if no start levels
	if(!levelOn && !tempOn && !hueOn && !satOn){
		logTrace("$app.label (line 660): Returning null as start level for $device")
		return defaults
	}

	// Set timeStart and timeStop, if sunrise or sunset
	if(timeStartSunrise) timeStart = parent.getSunrise(timeStartOffset,timeStartOffsetNegative,app.label)
	if(timeStartSunset) timeStart = parent.getSunset(timeStartOffset,timeStartOffsetNegative,app.label)
	if(timeStopSunrise) timeStop = parent.getSunrise(timeStopOffset,timeStopOffsetNegative,app.label)
	if(timeStopSunset) timeStop = parent.getSunset(timeStopOffset,timeStopOffsetNegative,app.label)

	// if not between start and stop time
	if(timeStop && !parent.timeBetween(timeStart, timeStop,app.label)) return defaults

	// If disabled, return null
	if(disable || state.disableAll) {
		logTrace("$app.label (line 675): Default level for $device null, schedule disabled")
		return defaults
	}

	// If mode set and node doesn't match, return null
	if(ifMode){
		if(location.mode != ifMode) {
			logTrace("$app.label (line 682): Default level for $device null, mode $ifMode")
			return defaults
		}
	}

	// If not correct day, return null
	if(timeDays && !parent.todayInDayList(timeDays,app.label)) return defaults

	// Get current level
	currentLevel = device.currentLevel
	currentTemp = device.currentColorTemperature
	currentHue = device.currentHue
	currentSat = device.currentSaturation

	// If no stop time, return start level
	if(!timeStop) {
		if(levelOn){
			defaults = [level: levelOn]
			// If start level is too dim, and set not to dim, return current level
			if(timeLevelIfLower){
				if(timeLevelIfLower == "Lower"){
					if(parent.stateOn(device,app.label) && currentLevel < levelOn) defaults = [level: currentLevel]
				// If start level is too bright, and set not to brighten, return current level
				} else if(timeLevelIfLower == "Higher"){
					if(parent.stateOn(device,app.label) && currentLevel > levelOn) defaults = [level: currentLevel]
				}
			}
		}

		if(tempOn){
			defaults = [temp: tempOn]
			// If start temp is too low, and set not to go lower, return current level
            if(timeTempIfLower){
                if(timeTempIfLower == "Lower"){
                    if(parent.stateOn(device,app.label) && currentTemp < tempOn) defaults = [temp: currentTemp]
                    // If start temp is too high, and set not to go higher, return current level
                } else if(timeTempIfLower == "Higher"){
                    if(parent.stateOn(device,app.label) && currentTemp > tempOn) defaults = [temp: currentTemp]
                }
            }
		}
		if(hueOn) defaults = [hue: hueOn]
		if(satOn) defaults = [sat: satOn]
		logTrace("$app.label (line 725): Default level $defaults for $device")
		return defaults
	}

	// If there's a stop time and stop level, and after start time
	if(timeStart && timeStop){
		// If timeStop before timeStart, add a day
		if(timeToday(timeStop, location.timeZone).time < timeToday(timeStart, location.timeZone).time) {
			newTimeStop = parent.getTomorrow(timeStop,app.label)
		} else {
			newTimeStop = timeStop
		}
	
		// Calculate proportion of time already passed from start time to endtime
		hours1 = Date.parse("yyyy-MM-dd'T'HH:mm:ss", newTimeStop).format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
		minutes1 = Date.parse("yyyy-MM-dd'T'HH:mm:ss", newTimeStop).format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
		seconds1 = Date.parse("yyyy-MM-dd'T'HH:mm:ss", newTimeStop).format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
		hours2 = new Date().format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
		minutes2 = new Date().format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
		seconds2 = new Date().format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
		// Calculate new level
		if(levelOff && levelOn) {
            if(levelOff > levelOn){
			newLevel = (levelOff - levelOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + levelOn as int
                } else {
                newLevel = levelOn - (levelOn - levelOff) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) as int
                    }
		} else if(levelOn) {
			newLevel = levelOn
		}
		if(tempOff && tempOn) {
            if(tempOff > tempOn){
			newTemp = (tempOff - tempOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + tempOn as int
                } else {
			newTemp = tempOn - (tempOn - tempOff) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) as int
                }
		} else if(tempOn){
			newTemp = tempOn
		}

        if(hueOff && hueOn) {
            // hueOn=25, hueOff=75, going 25, 26...74, 75
            if(hueOff > hueOn && hueDirection == "Forward"){
                newHue = (hueOff - hueOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + hueOn as int
            // hueOn=25, hueOff=75, going 25, 24 ... 2, 1, 100, 99 ... 76, 75
            } else if(hueOff > hueOn && hueDirection == "Reverse"){
                newHue = hueOn - (100 - hueOff + hueOn)  * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) as int
                if(newHue < 1) newHue = newHue + 100
            //hueOn=75, hueOff=25, going 75, 76, 77 ... 99, 100, 1, 2 ... 24, 25
            } else if(hueOff < hueOn && hueDirection == "Forward"){
                newHue = (100 - hueOn + hueOff)  * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + hueOn as int
                if(newHue > 100) newHue = newHue - 100
            //hueOn=75, hueOff=25, going 75, 74 ... 26, 25
            } else if(hueOff < hueOn && hueDirection == "Reverse"){
                newHue = hueOn - (hueOn - hueOff) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) as int
            }
		} else if(hueOn){
			newHue = hueOn
		}
        if(satOff && satOn) {
            if(satOff > satOn){
                newSat = (satOff - satOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + satOn as int
            } else {
                newSat = (100 - satOn + satOff) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + satOn as int
                if(newSat > 100) newSat = newSat - 100
           }
        } else if (satOn){
			newSat = satOn
		}

		if(newLevel && defaults.level == "Null"){
			// If new level is too dim, and set not to dim, return current level
			if(timeLevelIfLower){
				if(timeLevelIfLower == "Lower"){
					logTrace("$app.label (line 799): Default level for $device ignored; already dimmer")
					if(parent.stateOn(device,app.label) && currentLevel > newLevel) defaults.put("level",currentLevel)
				}
				// If new level is too bright, and set not to brighten, return current level
				if(timeLevelIfLower == "Higher"){
					logTrace("$app.label (line 804): Default level for $device ignored; already brighter")
					if(parent.stateOn(device,app.label) && currentLevel < newLevel) defaults.put("level",currentLevel)
				}
			}
		}

		if(defaults.level == "Null" && newLevel) defaults.put("level",newLevel)
		if(parent.isFan(device,app.label) && defaults.level != "Null") defaults.put("level",roundFanLevel(defaults.level))

		// Set temp
		if(newTemp && defaults.temp == "Null"){
			// If new level is too low, and set not to go lower, return current level
			if(timeTempIfLower){
				if(timeTempIfLower == "Lower"){
					if(parent.stateOn(device,app.label) && currentTemp < newTemp) defaults.put("temp",currentTemp)
				}
				// If new level is too high, and set not to go higher, return current level
				if(timeTempIfLower == "Higher"){
					if(parent.stateOn(device,app.label) && currentTemp > newTemp) defaults.put("temp",currentTemp)
				}
			}
		}
		if(defaults.temp == "Null" && newTemp) defaults.put("temp",newTemp)
		// Set hue
		if(defaults.hue == "Null" && newHue) defaults.put("hue",newHue)
		// Set sat
		if(defaults.sat == "Null" && newSat) defaults.put("sat",newSat)
	}

	// Should be all the options, but let's return current level just in case, and log an error
	if(defaults.level == "Null") logTrace("$app.label (line 834): Error: No default level match found for $device.")

	return defaults
}

// Schedule initializer
def initializeSchedules(){
	unschedule()
	
	// If disabled, return null
	if(disable || state.disableAll) {
		logTrace("$app.label (line 848): initializeSchedules returning; schedule disabled")
		return
	}

	// Set timeStart and timeStop, if sunrise or sunset
	if(timeStartSunrise) timeStart = parent.getSunrise(timeStartOffset,timeStartOffsetNegative,app.label)
	if(timeStartSunset) timeStart = parent.getSunset(timeStartOffset,timeStartOffsetNegative,app.label)
	if(timeStopSunrise) timeStop = parent.getSunrise(timeStopOffset,timeStopOffsetNegative,app.label)
	if(timeStopSunset) timeStop = parent.getSunset(timeStopOffset,timeStopOffsetNegative,app.label)

	// if no start time
	if(!timeStart && !timeStartSunrise && !timeStartSunset){
		logTrace("$app.label (line 860): Error: No start time entered")
		return
	}

	// Immediately start incremental schedules
	// If incremental
	if(timeStop || timeStopSunrise || timeStopSunset){
		// Check if any incremental changes to make
		if((levelOn && levelOff) || (tempOn && tempOff) || (hueOn && hueOff) || (satOn && satOff)){
			// IncrementalSchedule does all data checks, so just run it
			incrementalSchedule()
		}
	}

	// Get start time cron data
    if(timeDays) weekDays = weekDaysToNum()
	hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
	minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
	
	// Schedule next day incrementals, if no start action to be scheduled 
	if(timeOn != "On" && timeOn != "Off" && timeOn != "Toggle" && !modeChangeOn) {
		if(weekDays) {
			logTrace("$app.label (line 882): function initializeSchedules scheduling runDayOnSchedule (0 $minutes $hours ? * $weekDays)")
			schedule("0 " + minutes + " " + hours + " ? * " + weekDays, incrementalSchedule)
		} else {
			logTrace("$app.label (line 885): initializeSchedules scheduling incrementalSchedule (0 $minutes $hours * * ?)")
			schedule("0 " + minutes + " " + hours + " * * ?", incrementalSchedule)
		}
	// Schedule next day's starting on/off/toggle
	} else if(timeOn == "On" || timeOn == "Off" || timeOn == "Toggle" || modeChangeOn){
		if(weekDays) {
			logTrace("$app.label (line 891): initializeSchedules scheduling runDayOnSchedule (0 $minutes $hours ? * $weekDays)")
			schedule("0 " + minutes + " " + hours + " ? * " + weekDays, runDayOnSchedule)
		} else {
			logTrace("$app.label (line 894): initializeSchedules scheduling runDayOnSchedule (0 $minutes $hours * * ?)")
			schedule("0 " + minutes + " " + hours + " * * ?", runDayOnSchedule)
		}
	}

	// Schedule next day's ending on/off/toggle														  
	if(timeOff == "On" || timeOff == "Off" || timeOff == "Toggle" || modeChangeOff){
		if(timeStop){
			// Increment time stop by a day if before start time
			if(timeToday(timeStop, location.timeZone).time < timeToday(timeStart, location.timeZone).time) timeStop = parent.getTomorrow(timeStop,app.label)
			hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('HH').toInteger()
			minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('mm').toInteger()
			if(weekDays) {
				logTrace("$app.label (line 907): initializeSchedules scheduling runDayOffSchedule (0 $minutes $hours ? * $weekDays)")
				schedule("0 " + minutes + " " + hours + " ? * " + weekDays, runDayOffSchedule, [overwrite: false])
			}else {
				logTrace("$app.label (line 910): initializeSchedules scheduling runDayOffSchedule (0 $minutes $hours * * ?)")
				schedule("0 " + minutes + " " + hours + " * * ?", runDayOffSchedule, [overwrite: false])
			}
		}
	}
}

//settings up schedules for level/temp
def incrementalSchedule(device = "Null",manualOverride=false){
	// If disabled, return null
	if(disable || state.disableAll) {
		logTrace("$app.label (line 924): function incrementalSchedule returning; schedule disabled")
		return
	}

	// If no device match, return null
	if(device != "Null"){
		timeDevice.findAll( {it.id == device.id} ).each {
			logTrace("$app.label (line 931): Matched device $device and $it")
			match = true
		}
		if(!match) return
	}
	
	if(timeLevelPico && manualOverride && (!timeOff && !modeChangeOff && !levelOff)){
		logTrace("$app.label (line 938): incrementalSchedule exiting, manual override enabled for $device")
		return
	}

	// Check if correct day and time just so we don't keep running forever
	if(timeDays && !parent.todayInDayList(timeDays,app.label)) return

	// If mode set and node doesn't match, return null
	if(ifMode && location.mode != ifMode) {
		logTrace("$app.label (line 947): incrementalSchedule returning, mode $ifMode")
		return
	}

	// Set timeStart and timeStop, if sunrise or sunset
	if(timeStartSunrise) timeStart = parent.getSunrise(timeStartOffset,timeStartOffsetNegative,app.label)
	if(timeStartSunset) timeStart = parent.getSunset(timeStartOffset,timeStartOffsetNegative,app.label)
	if(timeStopSunrise) timeStop = parent.getSunrise(timeStopOffset,timeStopOffsetNegative,app.label)
	if(timeStopSunset) timeStop = parent.getSunset(timeStopOffset,timeStopOffsetNegative,app.label)

	// If between start and stop time (if start time after stop time, then if after start time)
	if(parent.timeBetween(timeStart, timeStop,app.label)){
		// If Pico override, return null
        if(timeLevelPico && manualOverride && timeLevelPico) {
            logTrace("$app.label (line 961): incrementalSchedule exiting, manual override enabled for $device")
			unschedule(incrementalSchedule)
			return
		}
		// Run first iteration now
		runIncrementalSchedule()
		runIn(20,incrementalSchedule)
		logTrace("$app.label (line 968): scheduling itself for 20 seconds")
        return true
	} else {
		return
	}
}

// run scheduled level/temp incremental changes
// scheduled function called from incrementalSchedule
def runIncrementalSchedule(){
	// Loop through devices
	timeDevice.each{
		// Ignore devices that aren't on
		if(parent.stateOn(it,app.label)){
			// Set level
			defaults = getDefaultLevel(it)

			if(levelOn && parent.isDimmable(it,app.label) && defaults.level != "Null"){
                if(defaults) parent.setToLevel(it,defaults.level,app.label)
			}
			// Set temp
			if(tempOn && parent.isTemp(it,app.label) && defaults.temp != "Null"){
				currentTemp = it.currentColorTemperature
				if(defaults.temp){
					if(defaults.temp - currentTemp > 3 || defaults.temp - currentTemp < -3) parent.singleTemp(it,defaults.temp,app.label)
				}
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


//Scheduled function called from setDaySchedule
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
	}
	initializeSchedules()
}

//Scheduled function called from setDaySchedule
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
	logTrace("$app.label (line 1085): weekDaysToNum returning $dayString")
	return dayString
}

def logTrace(message){
	log.trace message
}
