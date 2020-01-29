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
*  Name: Master - Contact
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master%20-%20Contact.groovy
*  Version: 0.3.27
* 
***********************************************************************************************************************/

definition(
    name: "Master - Contact",
    namespace: "master",
    author: "roguetech",
    description: "Door Sensors",
    parent: "master:Master",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/locks/lock/unlocked@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/locks/lock/unlocked@2x.png"
)

/* ************************************************** */
/* TO-DO: Add error messages (and change info icon    */
/* (see humidity).                                    */
/* ************************************************** */ 
preferences {
	infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
	errorIcon = "<img src=\"http://emily-john.love/icons/error.png\" width=20 height=20>"
    page(name: "setup", install: true, uninstall: true) {

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
                input "disable", "bool", title: "<b><font color=\"#000099\">This contact sensor is disabled.</font></b> Reenable it?", submitOnChange:true
            }

            //if not disabled, then show everything
            if(!state.disable && !disable ){
                displayNameOption()
                //if no label, stop
                if(app.label){
                    displayDevicesOption()
                    if(contactDevice){
                        //if no devices, stop
                        input "disable", "bool", title: "This contact sensor is enabled. Disable it?", submitOnChange:true
                        displayOpenDevices()
                        if(openSwitch) displayOpenSwitchOptions()
                        //change from "!openLock" to "openLock"
                        if(!openLock) displayOpenLockOptions()
                            displayCloseDevices()
                        if(closeSwitch) displayCloseSwitchOptions()
                        displayBinaryOptions()
                        displayBrightnessOption()
                        displayTempOption()
                        displayColorOption()
                        
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
                            
                            input "timeOn", "enum", title: "Turn devices on or off ($varStartTime)?", multiple: false, width: 12, options: ["None": "Don't turn on or off (leave as is)","On": "Turn On", "Off": "Turn Off", "Toggle": "Toggle (if on, turn off, and if off, turn on)"], submitOnChange:true
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
                                    input "timeOff", "enum", title: "Turn devices on or off ($varStopTime)?", multiple: false, width: 12, options: ["None": "Don't turn on or off (leave as is)","On": "Turn On", "Off": "Turn Off", "Toggle": "Toggle (if on, turn off, and if off, turn on)"], submitOnChange:true
                                }
                            }
                        }
                        displayWaitOptions()
                        displayAlertOptions()
// Send SMS and/or speakc (depending who is home)
// Set active time
// Change Mode
// Open/close delay
// if Mode
                        
                    }
                    paragraph error
                if(!error) input "disableAll", "bool", title: "Disable <b>ALL</b> schedules?", submitOnChange:true
                }
            }
        }

    }
}

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
    displayLabel("Set name for this contact sensor routine")
	label title: "", required: true, submitOnChange:true
/* ************************************************** */
/* TO-DO: Test the name is unique; otherwise          */
/* rescheduling won't work, since we use "childLabel" */
/* variable.                                          */
/* ************************************************** */
}

def displayDevicesOption(){
    displayLabel("Select which devices to schedule")
    input "contactDevice", "capability.contactSensor", title: "Contact Sensor(s)", multiple: true, required: true, submitOnChange:true
	if(!contactDevice) displayInfo("Select the contact sensor(s) (doors/windows) for which to set actions.")
}

def displayOpenDevices(){
    if((openLock && openLockAction != "none") || (openSwitch && openSwitchAction == "none" && !openSwitchSettings)){
        displayLabel("Select which lights/devices to control when closed")
    } else {
        displayLabel("When opened")
    }

    input "openSwitch", "capability.switchLevel", title: "Lights/switches to control when opened (optional)", multiple: true, submitOnChange:true
    input "openLock", "capability.lock", title: "Locks to control when opened (optional)", multiple: true, submitOnChange:true
	if(!openSwitch && !openLock){
		displayInfo("Select which switches/lights and/or locks to control when the contact (door/window) is opened. This will allow turning on, turning off, toggling, and/or setting levels for lights/switches, and/or locking or unlocking.")
	} else if(!openLock){
		displayInfo("Select lock(s) in order to lock or unlock them when contact is opened. Optional field.")
	} else if(!openSwitch){
		displayInfo("Select switches/lights in order to turn on, turn off, toggle, and/or set levels for them when contact is opened. Optional field.")
	}
}

def displayOpenSwitchOptions(){
    if(!openSwitch) return
    input "openSwitchAction", "enum", title: "Turn lights/switches on or off when opened?", multiple: false, width: 12, options: ["none": "Don't turn on or off (leave as is)","on":"Turn on", "off":"Turn off", "resume": "Resume schedule (if none, turn off)", "toggle":"Toggle"], submitOnChange:true
	if(!openSwitchAction) {
		displayInfo("Set whether to turn on or off, or toggle $timeDevice, when contact is opened. If it should not turn on, turn off, or toggle, then select \"Don't\". Toggle turns on devices that are off, and turns off devices that are on. \"Resume schedule\" will enable any schedule active when the contact sensor opens, and turn off if there are no active schedules. If there is not any active schedule for the device(s), they will turn off. In other words, it will restore their default state or turn them off. To resume active schedules without turning off, select \"Don't\". Required field.")
	} else if(openSwitchAction == "resume") {
		displayInfo("Active schedules will be enabled when the contact sensor opens, and turn off if there are no active schedules. If there is not any active schedule for the device(s), they will turn off. To resume active schedules without turning off, select \"Don't\".")
	} 
}

def displayOpenLockOptions(){
    if(!openLock) return
    input "openLockAction", "enum", title: "Lock or unlock when opened?", multiple: false, width: 12, options: ["none": "Don't lock or unlock (leave as is)","lock":"Lock", "unlock":"Unlock"], submitOnChange:true
	if(!openLockAction) displayInfo("Set whether to lock or unlock the lock when the contact is opened. Required field.")
}

def displayCloseDevices(){
	displayLabel("When closed")
	input "closeSwitchDifferent", "bool", title: "Control same lights and locks when closed. Click to change.", width: 12, submitOnChange:true
	if(closeSwitchDifferent){
		input "closeSwitch", "capability.switchLevel", title: "Lights/switches to control when closed", multiple: true, submitOnChange:true
		input "closeLock", "capability.lock", title: "Locks to control when closed", multiple: true, submitOnChange:true
		if(!openSwitch && !openLock){
			displayInfo("Select which switches/lights and/or locks to control when the contact is closed. This will allow turning on, turning off, toggling, and/or setting levels for lights/switches, and/or locking or unlocking.")
		} else if(!openLock){
			displayInfo("Select lock(s) in order to lock or unlock them when contact is closed. Optional field.")
		} else if(!openSwitch){
			displayInfo("Select switches/lights in order to turn on, turn off, toggle, and/or set levels for them when contact is closed. Optional field.")
		}
	} else {
		if(!closeSwitchDifferent) displayInfo("Change this option to allow control any <i>different</i> switch(es) or lock(s) when closing than when opening the contact.")
	}
}

def displayCloseSwitchOptions(){
    if(!closeSwitch) return
    input "closeSwitchAction", "enum", title: "Turn lights/switches on or off when closed?", multiple: false, width: 12, options: ["none": "Don't turn on or off (but resume schedule)","on":"Turn on", "off":"Turn off", "resume": "Resume schedule (if none, turn off)", "toggle":"Toggle"], submitOnChange:true
	if(!closeSwitchAction) {
		displayInfo("Set whether to turn on or off, or toggle $timeDevice, when closing the contact. If it should not turn on, turn off, or toggle, then select \"Don't\". Toggle turns on devices that are off, and turns off devices that are on. \"Resume schedule\" will enable any schedule active when the contact sensor opens, and turn off if there are no active schedules. If there is not any active schedule for the device(s), they will turn off. In other words, it will restore their default state or turn them off. To resume active schedules without turning off, select \"Don't\". Required field.")
	} else if(closeSwitchAction == "none"){
		displayInfo("Not turning on or off will resume schedule(s), even if the schedule was overriden when opened.")
	} else if(closeSwitchAction == "resume") {
		displayInfo("Active schedules will be enabled when the contact sensor opens, and turn off if there are no active schedules. If there is not any active schedule for the device(s), they will turn off. To resume active schedules without turning off, select \"Don't\".")
	}
}

def displayCloseLockOptions(){
    if(!closeLock) return
    displayLabel("Locks when closed")
    input "closeLockAction", "enum", title: "Lock or unlock when closed?", multiple: false, width: 6, options: ["none": "Don't lock or unlock (leave as is)","lock":"Lock", "unlock":"Unlock"], submitOnChange:true
	if(!closeLockAction) displayInfo("Set whether to lock or unlock the lock when the contact is opened. Required field.")
}

def displayBinaryOptions(){
//These need to be moved, either to the top of the UI, and/or to Install, Update and Initialize
    // If change level isn't selected, clear levels
    if(!levelEnable){
        levelOpen = null
        levelClose = null
    }

    // If change temp isn't selected, clear temps
    if(!tempEnable){
        tempOpen = null
        tempClose = null
    // If change temp is selected, don't allow change color to be selected
    } else {
        colorEnable = null
    }

    // If change color isn't selected, clear colors
    if(!colorEnable){
        hueOpen = null
        hueClose = null
        satOpen = null
        satClose = null
    }

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
//Should allow both, for settings one value at open and a different at close
    if(!colorEnable && !tempEnable){
        input "colorEnable", "bool", title: "<b>Don't change color.</b> Click to change.", submitOnChange:true
    } else if(!tempEnable){
        input "colorEnable", "bool", title: "<b>Change color.</b> Click to change.", submitOnChange:true
    } else if(tempEnable){
        paragraph " &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; Don't change color."
    }
    if(delayEnable){
        input "delayEnable", "bool", title: "<b>Delay actions when opening or closing.</b> Click to change.", submitOnChange:true
    } else {
        input "delayEnable", "bool", title: "<b>Don't delay actions when opening or closing.</b> Click to change.", submitOnChange:true
    }
    if(scheduleEnable){
        input "scheduleEnable", "bool", title: "<b>Set schedule.</b> Click to change.", submitOnChange:true
    } else {
        input "scheduleEnable", "bool", title: "<b>Do not set schedule.</b> Click to change.", submitOnChange:true
    }
    if(alertEnable){
        input "alertEnable", "bool", title: "<b>Send alert.</b> Click to change.", submitOnChange:true
    } else {
        input "alertEnable", "bool", title: "<b>Do not send alert (text or voice).</b> Click to change.", submitOnChange:true
    }
    if(!modeEnable){
        input "modeEnable", "bool", title: "<b>Don't change Mode.</b> Click to change.", submitOnChange:true
    } else {
        input "modeEnable", "bool", title: "<b>Change Mode.</b> Click to change.", submitOnChange:true
    }
	if(!levelEnable && !tempEnable && !colorEnable && !scheduleEnable && !alertEnable && !modeEnable) displayInfo("Select which option(s) to change, which will allow entering values to set when contact is opened and/or closed. All are optional.")
}

def displayBrightnessOption(){
    if(!levelEnable) return

	input "openLevel", "number", title: "Brightness when opened?", width: width, submitOnChange:true
	input "closeLevel", "number", title: "Brightness when closed?", width: 6, submitOnChange:true
	if(!openLevel && !closeLevel) displayInfo("Enter the percentage of brightness to set lights/switches, from 1 to 100, when contact opens and/or closes. Either opening or closing brightness is required (or unselect \"Change brightness\").")

	if(openLevel > 100) errorMessage("Brightness is percentage from 1 to 100. Correct opening brightness.")
        if(closeLevel > 100) errorMessage("Brightness is percentage from 1 to 100. Correct closing brightness.")
}

def displayTempOption(){
    if(!tempEnable) return

	input "openTemp", "number", title: "Color temperature when opened?", width: width, submitOnChange:true
	input "closeTemp", "number", title: "Color temperature when closed?", width: 6, submitOnChange:true
	if(!openLevel && !closeLevel) {
		displayInfo("Temperature color is in Kelvin from 1800 to 5400, to set lights/switches when contact opens and/or closes. Lower values have more red, while higher values have more blue, where daylight is 5000, cool white is 4000, and warm white is 3000. Either opening or closing temperature is required (or unselect \"Change temperature\").")
	} else {
		displayInfo("Temperature color is from 1800 to 5400, where daylight is 5000, warm white is 3000, and cool white is 4000.")
	}

	if(openTemp < 1800 || openTemp > 5400) errorMessage("Temperature color is from 1800 to 5400, where daylight is 5000, warm white is 3000, and cool white is 4000. Correct opening temperature.")
	if(closeTemp <  1800 || closeTemp > 5400) errorMessage("Temperature color is from 1800 to 5400, where daylight is 5000, warm white is 3000, and cool white is 4000. Correct opening temperature.")
}

def displayColorOption(){
    if(!colorEnable) return

	input "openHue", "number", title: "Hue when opened?", width: 6, submitOnChange:true
    	input "closeHue", "number", title: "Hue when closed?", width: 6, submitOnChange:true
	input "openSat", "number", title: "Saturation when opened?", width: 6, submitOnChange:true
    	input "closeSat", "number", title: "Saturation when closed?", width: 6, submitOnChange:true
	if(!openHue && !closeHue){
		displayInfo("Hue is the shade of color, from 1 to 100, to set lights/switches when contact opens and/or closes. Red is 1 or 100, yellow is 11, green is 26, blue is 66, and purple is 73. Optional fields.")
	} else {
		displayInfo("Hue is the shade of color, from 1 to 100. Red is 1 or 100, yellow is 11, green is 26, blue is 66, and purple is 73.")
	}
	if(!openSat && !closeSat) displayInfo("Saturation is the percentage amount of color tint displayed, from 1 to 100, when contact opens and/or closes. 1 is hardly any color tint and 100 is full color. Optional fields.")

	if(openHue > 100) errorMessage("Hue is from 1 to 100. Correct opening hue.")
	if(closeHue > 100) errorMessage("Hue is from 1 to 100. Correct closing hue.")
	if(openSat > 100) errorMessage("Saturation is from 1 to 100. Correct opening saturation.")
	if(closeSat > 100) errorMessage("Saturation is from 1 to 100. Correct closing saturation.")
}




// pick up here with display messages




def displayStartTimeTypeOption(){
    if(!scheduleEnable) return
    displayLabel("Start time")

    input "timeDays", "enum", title: "On these days (defaults to all days)", multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"], submitOnChange:true
    if(!inputStartType){
        width = 12
    } else if(inputStartType == "Time" || !inputStartSunriseType || inputStartSunriseType == "At"){
        width = 6
    } else if(inputStartSunriseType){
        width = 4
    }
    input "inputStartType", "enum", title: "Start Time:", multiple: false, width: width, options: ["Time":"Start at specific time", "Sunrise":"Sunrise (at, before or after)","Sunset":"Sunset (at, before or after)" ], submitOnChange:true
}

def displayStartTimeOption(){
        if(!scheduleEnable) return
    input "inputStartTime", "time", title: "Start time", width: 6, submitOnChange:true
}

def displayStartSunriseOption(){
        if(!scheduleEnable) return
    if(!inputStartSunriseType || inputStartSunriseType == "At") {
        width = 6 
    } else {
        width = 4
    }
    input "inputStartSunriseType", "enum", title: "At, before or after sunrise:", multiple: false, width: width, options: ["At":"At sunrise", "Before":"Before sunrise", "After":"After sunrise"], submitOnChange:true
    if(inputStartSunriseType == "Before"){
        input "inputStartBefore", "number", title: "Minutes before sunrise:", width: 4, submitOnChange:true
    } else if(inputStartSunriseType == "After"){
        input "inputStartBefore", "number", title: "Minutes after sunrise:", width: 4, submitOnChange:true
    }
}

def displayStartSunsetOption(){
        if(!scheduleEnable) return
    if(!inputStartSunriseType || inputStartSunriseType == "At") {
        width = 6
    } else {
        width = 4
    }
    input "inputStartSunriseType", "enum", title: "At, before or after sunset:", multiple: false, width: width, options: ["At":"At sunset", "Before":"Before sunset", "After":"After sunset"], submitOnChange:true
    if(inputStartSunriseType == "Before"){
        input "inputStartBefore", "number", title: "Minutes before sunset:", width: 4, submitOnChange:true
    } else if(inputStartSunriseType == "After"){
        input "inputStartBefore", "number", title: "Minutes after sunset:", width: 4, submitOnChange:true
    }
}

def checkStartTimeEntered(){
        if(!scheduleEnable) return
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
        if(!scheduleEnable) return
    //check if proper start time has been entered
    if(inputStopType){
        if(inputStopType == "None") return true
        if(inputStopType == "Time" && inputStopTime) return true
        if((inputStopType == "Sunrise" || inputStopType == "Sunset") && inputStopSunriseType == "At") return true
        if((inputStopType == "Sunrise" || inputStopType == "Sunset") && (inputStopSunriseType == "Before" || inputStopSunriseType == "After") && inputStopBefore) return true
    }
}

def displayStopTimeTypeOption(){
        if(!scheduleEnable) return
    displayLabel("Stop time")
    if(!inputStopType || inputStopType == "None"){
        width = 12
    } else if(inputStopType == "Time" || !inputStopSunriseType || inputStopSunriseType == "At"){
        width = 6
    } else if(inputStopSunriseType){
        width = 4
    }
    input "inputStopType", "enum", title: "Stop Time:", multiple: false, width: width, options: ["Time":"Stop at specific time", "Sunrise":"Sunrise (at, before or after)","Sunset":"Sunset (at, before or after)" ], submitOnChange:true
//Must have a stop time - can't have open ended schedule. "None" is not allowed
}

def displayStopTimeOption(){
        if(!scheduleEnable) return
    input "inputStopTime", "time", title: "Stop time", width: 6, submitOnChange:true
}

def displayStopSunriseOption(){
        if(!scheduleEnable) return
    if(!inputStopSunriseType || inputStopSunriseType == "At"){
        width = 6
    } else {
        width = 4
    }
    input "inputStopSunriseType", "enum", title: "At, before or after sunrise:", multiple: false, width: width, options: ["At":"At sunrise", "Before":"Before sunrise", "After":"After sunrise"], submitOnChange:true
    if(inputStopSunriseType == "Before"){
        input "inputStopBefore", "number", title: "Minutes before sunrise:", width: 4, submitOnChange:true
    } else if(inputStopSunriseType == "After"){
        input "inputStopBefore", "number", title: "Minutes after sunrise:", width: 4, submitOnChange:true
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
        if(!scheduleEnable) return
    if(!inputStopSunriseType || inputStopSunriseType == "At"){
        width = 6
    } else {
        width = 4
    }
    input "inputStopSunriseType", "enum", title: "At, before or after sunset:", multiple: false, width: width, options: ["At":"At sunset", "Before":"Before sunset", "After":"After sunset"], submitOnChange:true
    if(inputStopSunriseType == "Before"){
        input "inputStopBefore", "number", title: "Minutes before sunset:", width: 4, submitOnChange:true
    } else if(inputStopSunriseType == "After"){
        input "inputStopBefore", "number", title: "Minutes after sunset:", width: 4, submitOnChange:true
    }
}

def getStartTimeVariables(){
        if(!scheduleEnable) return
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
        if(!scheduleEnable) return
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

def displayWaitOptions(){
    if(!delayEnable) return
    displayLabel("Delay start and/or stop actions")
    input "openWait", "number", title: "Wait seconds for opening action. (Optional)", defaultValue: false, width: 6, submitOnChange:true
	input "closeWait", "number", title: "Wait seconds for closing action. (Optional)", defaultValue: false, width: 6, submitOnChange:true
	if(openWait > 30) errorMessage("Wait time has been set to $openWait <i>minutes</i>. Is that correct?")
	if(closeWait > 30) errorMessage("Wait time has been set to $closeWait <i>minutes</i>. Is that correct?")
}

def displayAlertOptions(){
    if(!alertEnable) return
	if(parent.notificationDevice){
    		displayLabel("Alert by voice or text")
		width = 6
	} else {
		dispayLabel("Send text message")
		width = 12
	}
	if(
        input "phone", "phone", title: "Number to text alert? (Optional)", width: width, submitOnChange:true
	if(!validatePhone(phone)) errorMessage("Phone number must be nine digits.")
        if(width == 6) {
		input "speakText", "text", title: "Voice notification text? (Optional)", width: 6, submitOnChange:true
        	displayInfo("Voice message will be sent to \"Notification device(s)\" set in Master app.")
	}
	if(phone){
	        if(phoneOpenClose){
	            input "phoneOpenClose", "bool", title: "SMS when <b>closed</b>. Click for opened.", submitOnChange:true, width: width
	        } else {
	            input "phoneOpenClose", "bool", title: "SMS when <b>opened</b>. Click for closed.", submitOnChange:true, width: width
	        }
	}
	if(width == 6 && speakText){
		if(speakOpenClose){
		    input "speakOpenClose", "bool", title: "Speak when <b>closed</b>. Click for opened.", submitOnChange:true, width: 6
		} else {
		    input "speakOpenClose", "bool", title: "Speak when <b>opened</b>. Click for closed.", submitOnChange:true, width: 6
		}
	}
        if(width == 12) errorMessage("To have voice message will be sent, set \"Notification device(s)\" in Master app.")

// This should probably different functions
    input "modeEnable", "bool", title: "<b>Change Mode.</b> Click to change.", submitOnChange:true
// We're missing a mode selection option!!
    if(modeEnable && mode){
        if(modeOpenOrClose){
            input "modeOpenOrClose", "bool", title: "When <b>opened</b>, change mode, text and/or notification. Click for when closed.", submitOnChange:true
        } else {
            input "modeOpenOrClose", "bool", title: "When <b>closed</b>, change mode, text and/or notification. Click for on opened.", submitOnChange:true
        }
    }
    if(phone || speakText){
        input "personHome", "capability.presenceSensor", title: "Only alert if any of these people are home", multiple: true, submitOnChange:true
        input "personNotHome", "capability.presenceSensor", title: "Only alert if none of these people are home", multiple: true, submitOnChange:true
    }
}

/*
disableAll - bool - Flag to disable all contacts
disable - bool - Flag to disable this single contact
contactDevice - capability.contactSensor - Contact sensor being monitored
openSwitch - capability.switchLevel - Light device(s) being controlled (when opened, or when opened or closed, depending on closeSwitchDifferent)
openLock - capability.lock - Lock(s) being controlled
openSwitchAction - enum (none, on, off, resume, toggle) - Action to perform on openSwitch when opened
openLockAction - enum (none, lock, unlock) - Action to perform on openLock when opened
closeSwitchDifferent - bool - Flag to allow different devices for open and close
closeSwitch - capability.switchLevel - Light device(s) to control when closing. Does not display is closeSwitchDifferent does not equal true.
closeLock - capability.lock - Lock(s) being controlled when closing. Does not display is closeSwitchDifferent does not equal true.
closeSwitchAction - enum (none, on, off, resume, toggle) - Action to perform on closeSwitch when closed. "None" will resume any schedule disabled by open.
closeLockAction - enum (none, lock, unlock) - Action to perform on closeLock when closed
levelEnable - bool - Flag to enable options to change level
tempEnable - bool - Flag to enable options to change temperature
colorEnable - bool - Flag to enable options to change hue and saturation
delayEnable - bool - Flag to enable options to delay open/close actions
scheduleEnable - bool - Flag to enable options to set timeframe when contact performs actions
alertEnable - bool - Flag to enable options for SMS or speech
modeEnable - bool - Flag to enable option for changing mode
openLevel - number (1-100) - Level to set openSwitch when opened
closeLevel - number (1-100) - Level to set closeSwitch when closed
openTemp - number (1800-5400) - Temperature to set openSwitch when opened
closeTemp - number (1800-5400) - Temperature to set closeSwitch when closed
openHue - number (1-100) - Hue to set openSwitch when opened
closeHue - number (1-100) - Hue to set closeSwitch when closed
timeDays - enum (Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday) - Days on which contact will run. Only displays if scheduleEnable = true
inputStartType - enum (Time, Sunrise, Sunset) - Sets whether start time is a specific time, or based on sunrise or sunset. Only displays if scheduleEnable = true
inputStartTime - time - Start Time (only displays when scheduleEnable = true and inputStartType = "Time")
inputStartSunriseType - enum (At, Before, After) - Sets whether start time is sunrise/sunset time, or uses positive or negative offset (only displays if scheduleEnable = true, and inputStartType = "Sunrise" or "Sunset")
inputStartBefore - number - Number of minutes before/after sunrise/sunset for start time (only displays if scheduleEnable = true, inputStartType = "Sunrise" or "Sunset", and inputStartSunriseType = "Before" or "After")
inputStopType - enum (Time, Sunrise, Sunset) - Sets whether stop time is a specific time, or based on sunrise or sunset. (Only displays if scheduleEnable = true)
inputStopTime - time - Stop Time (only displays when scheduleEnable = true and inputStopType = "Time")
inputStopSunriseType - enum (At, Before, After) - Sets whether start time is sunrise/sunset time, or uses positive or negative offset (only displays if scheduleEnable = true, and inputStartType = "Sunrise" or "Sunset")
inputStopBefore - number - Number of minutes before/after sunrise/sunset for stop time (only displays if scheduleEnable = true, inputStartType = "Sunrise" or "Sunset", and inputStartSunriseType = "Before" or "After")
openWait - number - Minutes to delay open action(s). Only displays if delayEnable = true
closeWait - number - Minutes to delay close action(s). Only displays if delayEnable = true
phone - phone - Phone number for SMS. Only displays if alertEnable = true
speakText - text - Text to speak. Only displays if alertEnable = true
phoneOpenClose - bool - Switch to send alert when door opened, or closed. Only displays if alertEnable = true
speakOpenClose - bool - Switch to speak text when door opened, or closed. Only displays if alertEnable = true
modeOpenOrClose - bool - Switch to change mode when door opened, or closed. Only displays if modeEnable = true
personHome - capability.presenseSensor - Persons any of who must be home for contact to run. 
personNotHome - capability.presenseSensor - Persons all of who must not be home for contact to run.
*/

/* ************************************************** */
/*                                                    */
/* End display functions.                             */
/*                                                    */
/* ************************************************** */

def installed() {
	logTrace("$app.label (line 310) -- Installed")
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
	initialize()
}

def updated() {
	logTrace("$app.label (line 316) -- Updated")
	unsubscribe()
	initialize()
}

def initialize() {
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

	unschedule()
    
    
	// If date/time for last SMS not set, initialize it to 5 minutes ago
	// Allows an SMS immediately
	if(!state.contactLastSms) state.contactLastSms = new Date().getTime() - 360000
    
	if(disable || disableAll) {
		state.disable = true
        return
	} else {
		state.disable = false
    }
    
	if(!contactDisable && !state.contactDisable) {
		subscribe(contactDevice, "contact.open", contactChange)
		subscribe(contactDevice, "contact.closed", contactChange)
	}
}

def contactChange(evt){
	if(contactDisable || state.contactDisable) return

	logTrace("$app.label (line 340) -- Contact sensor $evt.displayName $evt.value")
	
	// If mode set and node doesn't match, return null
	if(ifMode && location.mode != ifMode) {
		logTrace("$app.label (line 342) -- Contact sensor not triggered; mode location.mode doesn't match ifMode")
		return
	}

	// Set timeStart and timeStop, if sunrise or sunset
	if(timeStartSunrise) timeStart = parent.getSunrise(timeStartOffset,timeStartOffsetNegative,app.label)
	if(timeStartSunset) timeStart = parent.getSunset(timeStartOffset,timeStartOffsetNegative,app.label)
	if(timeStopSunrise) timeStop = parent.getSunrise(timeStopOffset,timeStopOffsetNegative,app.label)
	if(timeStopSunset) timeStop = parent.getSunset(timeStopOffset,timeStopOffsetNegative,app.label)

	// if not between start and stop time
	if(timeStop){
		if(!parent.timeBetween(timeStart, timeStop,app.label)) return
	}
	
	// If not correct day, return null
	if(timeDays && !parent.todayInDayList(timeDays,app.label)) {
		logTrace("$app.label (line 359) -- Contact sensor not triggered; not correct day")
		return
	}

	// Unschedule pevious events
	
	// New open event resets delayed action
	// New close event won't override open
	if(evt.value == "open"){
		unschedule()
	} else {
		unschedule(scheduleClose)
	}

	// Check if people are home (home1 and home2 should be true
	if(personHome){
		home1 = false
		personHome.each{
			if(it.currentPresence == "present") home1 = true
		}
	}
	if(personNotHome){
		home2 = true
		personNotHome.each{
			if(it.currentPresence == "present") home2 = false
		}
	}

	// Text first (just in case there's an error later)
	if(phone && ((openOrClose && evt.value == "open") || (!openOrClose && evt.value == "closed"))){
		// Only if correct people are home/not home
		if((personHome && personNotHome && home1 && home2) || (personHome && !personNotHome && home1) || (!personHome && personNotHome && home2) || (!personHome && !personNotHome)){	
			def now = new Date()

			//if last text was sent less than 5 minutes ago, don't send
/* ************************************************** */
/* TO-DO: Add option to override text cooldown        */
/* period? (Maybe in Master?) Migrate new code to     */
/* presence app.                                      */
/* ************************************************** */
			// Compute seconds from last sms
			seconds = (now.getTime()  - state.contactLastSms) / 1000

			// Convert date to friendly format for log
			now = now.format("h:mm a", location.timeZone)
			if(seconds > 360){
				state.contactLastSms = new Date().getTime()

					if(evt.value == "open"){
						parent.sendText(phone,"$evt.displayName was opened at $now.",app.label)
						log.info "$app.label -- Sent SMS for $evt.displayName opened at $now."
					} else {
						parent.sendText(phone,"$evt.displayName was closed at $now.",app.label)
						log.info "$app.label -- Sent SMS for $evt.displayName closed at $now."
					}
			} else {
				log.info("app.label -- $evt.displayName was closed at $now. SMS not sent due to only being $seconds since last SMS.")
			}
		}
	}

	// Give voice alert
	if(speakText && ((openOrClose && evt.value == "open") || (!openOrClose && evt.value == "closed"))) {
		// Only if correct people are home/not home
		if((personHome && personNotHome && home1 && home2) || (personHome && !personNotHome && home1) || (!personHome && personNotHome && home2) || (!personHome && !personNotHome)){	
			parent.speak(speakText,app.label)
		}
	}

	// Set mode
	if(mode && ((openOrClose && evt.value == "open") || (!openOrClose && evt.value == "closed"))) parent.changeMode(mode,app.label)

	// Perform open events (for switches and locks)
	if(evt.value == "open"){
		// Schedule delay
		if(openWait) {
			logTrace("$app.label (line 437) -- Scheduling scheduleOpen in $openWait seconds")
			runIn(openWait,scheduleOpen)
		// Otherwise perform immediately
		} else {
			if(openSwitch) {
				if(openSwitchAction == "on") {
					parent.multiOn(openSwitch,app.label)
				} else if(openSwitchAction == "off"){
					parent.multiOff(openSwitch,app.label)
				} else if(openSwitchAction == "toggle"){
					parent.toggle(openSwitch,app.label)
				}
			}
			if(locks){
				if(actionOpenLock == "lock"){
					parent.multiLock(locks,app.label)
				} else if(actionOpenLock == "unlock"){
					parent.multiUnlock(locks,app.label)
				}
			}
		}

	// Perform close events (for switches and locks)
	} else {
		// Schedule delay
		if(closeWait) {
			logTrace("$app.label (line 463) -- Scheduling scheduleClose in $closeWait seconds")
			runIn(closeWait,scheduleClose)
		// Otherwise perform immediately
		} else {
			if(openSwitch) {
				if(closeSwitchAction == "on") {
					parent.multiOn(openSwitch,app.label)
				} else if(closeSwitchAction == "off"){
					parent.multiOff(openSwitch,app.label)
				} else if(closeSwitchAction == "toggle"){
					parent.toggle(openSwitch,app.label)
				}
			}
			if(locks){
				if(closeLockAction == "lock"){
					parent.multiLock(locks,app.label)
				} else if(closeLockAction == "unlock"){
					parent.multiUnlock(locks,app.label)
				}
			}
		}
	}
}

def scheduleOpen(){
	if(contactDisable || state.contactDisable) return

	if(openSwitch) {
		if(openSwitchAction == "on") {
			parent.multiOn(openSwitch,app.label)
		} else if(openSwitchAction == "off"){
			parent.multiOff(openSwitch,app.label)
		} else if(openSwitchAction == "toggle"){
			parent.toggle(openSwitch,app.label)
		}
	}
	if(locks){
		if(actionOpenLock == "lock"){
			parent.multiLock(locks,app.label)
		} else if(actionOpenLock == "unlock"){
			parent.multiUnlock(locks,app.label)
		}
	}
}

def scheduleClose(){
	if(contactDisable || state.contactDisable) return

	if(closeSwitch) {
		if(closeSwitchAction == "on") {
			parent.multiOn(closeSwitch,app.label)
		} else if(closeSwitchAction == "off"){
			parent.multiOff(closeSwitch,app.label)
		} else if(closeSwitchAction == "toggle"){
			parent.toggle(closeSwitch,app.label)
		}
	}
	if(locks){
		if(closeLockAction == "lock"){
			parent.multiLock(locks,app.label)
		} else if(closeLockAction == "unlock"){
			parent.multiUnlock(locks,app.label)
		}
	}
}

def validatePhone(phone){
	if(phone.length() == 9) return true
}

def getDevice(deviceId){
    openSwitch.each{
        if(it.id == deviceId){
            return it
        }
    }
    closeSwitch.each{
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
