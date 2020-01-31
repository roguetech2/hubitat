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
*  Version: 0.4.06
*
***********************************************************************************************************************/

definition(
    name: "Master - Time",
    namespace: "master",
    author: "roguetech",
    description: "Schedules, times and default settings",
    parent: "master:Master",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Office/office6-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Office/office6-icn@2x.png"
)

preferences {
	infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
	errorIcon = "<img src=\"http://emily-john.love/icons/error.png\" width=20 height=20>"

	// If we're missing a value, don't allow save
    	if((!timeDevice) ||
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
	(modeEnable && !modeChangeOn && !modeChangeOff)) noInstall = true

	if(noInstall) {
		install = false
	} else {
		install = true
	}

    page(name: "setup", install: install, uninstall: true) {
        section() {
            // If all disabled, force reenable
            if(disableAll){
                input "disableAll", "bool", title: "<b>All schedules are disabled.</b> Reenable?", submitOnChange:true
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
    displayLabel("Set name for this schedule")
	label title: "", required: true, submitOnChange:true
    if(!app.label) displayInfo("Name this schedule. Each schedule must have a unique name.")
/* ************************************************** */
/* TO-DO: Test the name is unique; otherwise          */
/* rescheduling won't work, since we use "childLabel" */
/* variable.                                          */
/* ************************************************** */
}

def displayDevicesOption(){
	displayLabel("Select which devices to schedule")
	input "timeDevice", "capability.switch", title: "Device(s)?", multiple: true, required: true, submitOnChange:true
	if(!timeDevice) displayInfo("Select which device(s) to control.")
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
		errorMessage(message)
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
	if(!inputStopType) displayInfo("If wanting to perform actions when ending, or to have the Device(s) setting change over time, select whether to stop at a specific time, or stop based on sunrise and sunset for the Hubitat location. If only triggering actions at start time, then select \"None\" to proceed. Required field.")
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
		errorMessage(message)
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
            parent.errorMessage(message)
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
			displayInfo("Enter the percentage of brightness when turning on $timeOn, from 1 to 100. Required field (or unselect \"Change brightness\").")
		} else {
			displayInfo("Brightness is percentage from 1 to 100.")
		}
        } else {
            displayLabel("Enter beginning and/or ending brightness")
                input "levelOn", "number", title: "Beginning brightness ($varStartTime)?", width: 6, submitOnChange:true
                input "levelOff", "number", title: "and ending brightness ($varStopTime)?", width: 6, submitOnChange:true
		if(!levelOn && !levelOff) {
			displayInfo("Enter the percentage of brightness when turning on $timeOn, from 1 to 100, when starting and/or ending the schedule. If entering both starting and ending brightness, it will transition from beginning to ending brightness for the duration of the schedule. Either starting or ending brightness is required (or unselect \"Change brightness\").")
		} else if(!levelOn || !levelOff){
			displayInfo("Enter the percentage of brightness when turning on $timeOn, from 1 to 100. If entering both starting and ending brightness, it will transition from beginning to ending brightness for the duration of the schedule.")
		} else {
			displayInfo("Brightness is percentage from 1 to 100.")
		}
		if(!levelOn && levelOff && timeOff == "off") errorMessage("With no beginning brightness while setting Device(s) to turn off, setting an ending brightness won't do anything.")
        }

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
            input "modeChangeOn", "mode", title: "Set Mode (at $varStartTime)?", width: 6, submitOnChange:true
            input "ifMode", "mode", title: "Only run if Mode is already?", width: 12
        } else {
            displayLabel("Change Mode $varStartTime and/or $varStopTime")
            input "modeChangeOn", "mode", title: "Change Mode (at $varStartTime)?", width: 12, submitOnChange:true
// ifmode is supposed to be "If Mode X, then allow run", not "only change mode if Mode is X" - i think?
            input "ifMode", "mode", title: "Only run if Mode is already?", width: 12
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
	logTrace(608, "Installed")
	app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
	initialize()
}

def updated() {
	logTrace(614,"Updated")
	initialize()
}

def initialize() {
	app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

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
        if(state.stop) state.totalSeconds = Math.floor((Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.stop).time - Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.start).time) / 1000)
		setWeekDays()

		initializeSchedules()
	}
	logTrace(666,"Initialized")
}

def setTime (){
    if(!setStartStopTime("Start")) return
    if(!setStartStopTime("Stop")) return
    setTotalSeconds()    
}

def setStartStopTime(type = "Start"){
    if(type == "Start") state.start = null
	if(type == "Stop") state.stop = null

	// If no stop time, exit
	if(type == "Stop" && (!inputStopType || inputStopType == "none")) return true

    if(settings["input${type}Type"] == "time"){
		value = settings["input${type}Time"]
	} else if(settings["input${type}Type"] == "sunrise"){
		value = (settings["input${type}SunriseType"] == "before" ? parent.getSunrise(settings["input${type}Before"] * -1,app.label) : parent.getSunrise(settings["input${type}Before"],app.label))
	} else if(settings["input${type}Type"] == "sunset"){
		value = (settings["input${type}SunriseType"] == "before" ? parent.getSunset(settings["input${type}Before"] * -1,app.label) : parent.getSunset(settings["input${type}Before"],app.label))
	} else {
		logTrace(689,"ERROR: input" + type + "Type set to " + settings["input${type}Type"])
		return
	}

	if(type == "Stop"){
		if(timeToday(state.start, location.timeZone).time > timeToday(value, location.timeZone).time) value = parent.getTomorrow(value,app.label)
	}
	logTrace(696,"$type time set as " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", value).format("h:mma MMM dd, yyyy", location.timeZone))
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
		//logTrace(711,"getDefaultLevel matched device $device and $it")
		match = true
	}
	if(!match) return defaults

	// If there's a matching device, check and set state variables
           if(state.stop && Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.stop).time < new Date().time) setTime()
        if(!state.start) return defaults

	// if no start levels, return nulls
	if(!levelOn && !tempOn && !hueOn && !satOn){
		logTrace(722,"No starting levels set for $device")
		return defaults
	}

	// If disabled, return nulls
	if(disable || state.disable) {
		logTrace(728,"Default level for $device null, schedule disabled")
		return defaults
	}

	// If mode set and node doesn't match, return nulls
	if(ifMode){
		if(location.mode != ifMode) {
			logTrace(735,"Default level for $device null, mode $ifMode")
			return defaults
		}
	}

	// If not correct day, return nulls
	if(timeDays && !parent.todayInDayList(timeDays,app.label)) return defaults

    // if not between start and stop time, return nulls
    if(state.stop && !parent.timeBetween(state.start, state.stop, app.label)) return defaults

    // If we need elapsed, then get it
    if((levelOn && levelOff) || (tempOn && tempOff) || (hueOn && hueOff) || (satOn && satOff)){
        elapsedFraction = getElapsedFraction()

		if(!elapsedFraction) {
			logTrace(751,"ERROR: Unable to calculate elapsed time with start \"$state.start\" and stop \"$state.stop\"")
			return defaults
		}
    }
        
    if(levelOn && levelOff){
        if(levelOff > levelOn){
            defaults.put("level", (levelOff - levelOn) * elapsedFraction + levelOn as int)
        } else {
            defaults.put("level", levelOn - (levelOn - levelOff) * elapsedFraction as int)
        }
    } else if(levelOn && !levelOff){
        defaults.put("level",levelOn)
    }

    if(tempOn && tempOff){
        if(tempOff > tempOn){
            defaults.put("temp", (tempOff - tempOn) * elapsedFraction + tempOn as int)
        } else {
            defaults.put("temp", tempOn - (tempOn - tempOff) * elapsedFraction as int)
        }
    } else if(tempOn && !tempOff){
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
    } else if(hueOn && !hueOff){
        defaults.put("hue",hueOn)
    }
    
    
    if(satOn && satOff){
        if(satOff > satOn){
            defaults.put("sat", (satOff - satOn) * elapsedFraction + satOn as int)
        } else {
            defaults.put("sat", satOn - (satOn - satOff) * elapsedFraction as int)
        }
    } else if(satOn && !satOff){
        defaults.put("sat",satOn)
    }

	// Round potential fan level
	if(parent.isFan(device,app.label) && defaults.level != "Null") defaults.put("level",roundFanLevel(defaults.level))

	logTrace(810,"Default levels $defaults for $device")
	return defaults
}

// Schedule initializer
// Called from initialize, runDayOnSchedule, and runDayOffSchedule
def initializeSchedules(){
	// Clear existing schedules
	unschedule()

	// If disabled, return null
	if(state.disable) {
		logTrace(822,"initializeSchedules returning; schedule disabled")
		return
	}

	// First, schedule dayOn, either every day or with specific days
	hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.start).format('HH').toInteger()
	minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.start).format('mm').toInteger()
    
	if(state.weekDays) {
		logTrace(831,"Scheduling runDayOnSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.start).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours ? * $state.weekDays)")
		schedule("0 " + minutes + " " + hours + " ? * " + state.weekDays, runDayOnSchedule)
	} else {
		logTrace(834,"Scheduling runDayOnSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.start).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours * * ?)")
		schedule("0 " + minutes + " " + hours + " * * ?", runDayOnSchedule)
	}

	// Second, schedule dayOff, either every day or with specific days
	if(state.stop){
		hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.stop).format('HH').toInteger()
		minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.stop).format('mm').toInteger()
		if(state.weekDays) {
			logTrace(843,"Scheduling runDayOffSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.stop).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours ? * $state.weekDays)")
			schedule("0 " + minutes + " " + hours + " ? * " + state.weekDays, runDayOffSchedule)
		} else {
			logTrace(846,"Scheduling runDayOffSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.stop).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours * * ?)")
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
	// If disabled, return null
	if(disable || state.disable) {
		// logTrace(863,"Function incrementalSchedule returning; schedule disabled")
		return
	}

	// Check if correct day
	if(timeDays && !parent.todayInDayList(timeDays,app.label)) return

	// Check if correct mode
	if(ifMode && location.mode != ifMode) {
		// logTrace(872,"incrementalSchedule returning, mode $ifMode")
		return
    }

    if(state.stop && Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.stop).time < new Date().time) setTime()
    if(!state.start || !state.stop) return

    // If between start and stop time (if start time after stop time, then if after start time)
    if(parent.timeBetween(state.start, state.stop, app.label)){
        // Check if device(s) are on
		if(!parent.multiStateOn(timeDevice)){
			// logTrace(883,"Since $timeDevice is off, stopping recurring schedules")
			return
		}

		// Run first iteration now
		runIncrementalSchedule()
        
//TO-DO: Figure out how long between each change, and schedule for that duration, rather than every X seconds.
//TO-DO: Add state variable setting for minimum duration
//TO-DO: Add warning on setup page if minimum duration is too low (override it?)
		runIn(20,incrementalSchedule)
		// logTrace(894,"Scheduling incrementalSchedule for 20 seconds")
		return true
	} else {
		// logTrace(897,"Schedule ended; now after $state.stop")
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

	// Check if correct day
	if(timeDays && !parent.todayInDayList(timeDays,app.label)) return

	// Check if correct mode
	if(ifMode && location.mode != ifMode) {
		// logTrace(935,"runDayOnSchedule returning, mode $ifMode")
		return
	}

	// if mode doesn't match, return
	if(ifMode && location.mode != ifMode) return

    if(!setTime()) return
    
	if(modeChangeOn) setLocationMode(modeChangeOn)
	if(timeOn == "on"){
		parent.multiOn(timeDevice,app.label)
	} else if(timeOn == "off"){
		parent.multiOff(timeDevice,app.label)
	} else if(timeOn == "toggle"){
		parent.toggle(timeDevice,app.label)
	}
    
	//  initializeSchedules sets levels
  
	//Reschedule everything
	initializeSchedules()
}

// Performs actual changes at time set with timeOff
// Called only by schedule set in incrementalSchedule
def runDayOffSchedule(){
	// If no start time, exit
	if(state.stop) return

	if(disable || state.disable) return

    if(!setStartStopTime()) return

	// if mode return
	if(ifMode && location.mode != ifMode) return
	if(modeChangeOff) setLocationMode(modeChangeOff)
	if(timeOff == "on"){
	   parent.multiOn(timeDevice,app.label)
	} else if(timeOff == "off"){
	   parent.multiOff(timeDevice,app.label)
	} else if(timeOff == "toggle"){
	   parent.toggle(timeDevice,app.label)
	}
    
    if(levelOff || tempOff || hueOff || satOff){
        // Loop through devices
        timeDevice.each{
            if(levelOff) parent.setToLevel(it,levelOff,app.label)
            if(tempOff) parent.singleTemp(it,tempOff,app.label)
            if(hueOff || satOff) singleColor(it, hueOff, satOff)
        }
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
	logTrace(1025,"weekDaysToNum returning $dayString")
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
	state.totalSeconds = Math.floor((Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.stop).time - Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.start).time) / 1000)
	logTrace(1040,"Schedule total seconds is $state.totalSeconds")
	return true
}

// Returns percentage of schedule that has elapsed
// Only called by getDefaultLevel
def getElapsedFraction(){
	if(!state.totalSeconds) return false

	// If not between start and stop time, exit
	if(!parent.timeBetween(state.start, state.stop, app.label)) return false

	elapsedSeconds = Math.floor((new Date().time - Date.parse("yyyy-MM-dd'T'HH:mm:ss", state.start).time) / 1000)

	//Divide for percentage of time expired (avoid div/0 error)
	if(elapsedSeconds < 1){
		elapsedFraction = 0
	} else {
		elapsedFraction = Math.floor(elapsedSeconds / state.totalSeconds * 100) / 100
	}

    if(elapsedFraction > 1 && !setTime()) return

	logTrace(1063,elapsedFraction * 100 + "% has elapsed in the schedule")
	return elapsedFraction
}

def getDevice(deviceId){
    timeDevice.each{
        if(it.id == deviceId){
            return it
        }
    }
}

def logTrace(lineNumber,message = null){
    if(message) {
	    log.trace "$app.label (line $lineNumber) -- $message"
    } else {
        log.trace "$app.label (line $lineNumber)"
    }
}
