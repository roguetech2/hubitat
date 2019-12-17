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
*  Version: 0.3.5
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
                if(error) paragraph "$error</div>"
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
        input "startBeforeSunrise", "number", title: "Minutes before sunrise:", required: false, width: 4, submitOnChange:true
    } else if(startSunriseType == "After"){
        input "startAfterSunrise", "number", title: "Minutes after sunrise:", required: false, width: 4, submitOnChange:true
        //set timeoffset
        //timeStartOffset = timeStartAfterSunrise
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
    } else if(startSunsetType == "After"){
        input "startAfterSunset", "number", title: "Minutes after sunset:", required: false, width: 4, submitOnChange:true
    }
}

def checkStartTimeEntered(){
    //check if proper start time has been entered
    if(timeStartType){
        if(timeStartType == "Time" && timeStart) return true
        if(timeStartType == "Sunrise"){
            if(startSunriseType){
                if(startSunriseType == "At" || (startSunriseType == "Before" && startBeforeSunrise) || (startSunriseType == "After" && startAfterSunrise)){
                    return true
                }
            }
        } else if(timeStartType == "Sunset"){
            if(startSunsetType){
                if(startSunsetType == "At" || (startSunsetType == "Before" && startBeforeSunset) || (startSunsetType == "After" && startAfterSunset)){
                    return true
                }
            }
        }
    }
    return false
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
    if(startBeforeSunset){
        if(startBeforeSunset > 2881) {
            errorMessage("Minutes before sunset is set to $startBeforeSunset, which is over " + Math.floor(startBeforeSunset / 60 / 24) + " days.")
        } else if(startBeforeSunset > 1441) {
            errorMessage("Minutes before sunset is set to $startBeforeSunset, which is over a day.")
        }
        if(startBeforeSunrise > 2881) {
            errorMessage("Minutes before sunrise is set to $startBeforeSunrise, which is over " + Math.floor(startBeforeSunrise / 60 / 24) + " days.")
        } else if(startBeforeSunset > 1441) {
            errorMessage("Minutes before sunrise is set to $startBeforeSunrise, which is over a day.")
        }
    }
    if(startAfterSunset){
        if(startAfterSunset > 2881) {
            errorMessage("Minutes after sunset is set to $startBeforeSunset, which is over " + Math.floor(startAfterSunset / 60 / 24) + " days.")
        } else if(startBeforeSunset > 1441) {
            errorMessage("Minutes after sunset is set to $startBeforeSunset, which is over a day.")
        }
        if(startBeforeSunrise > 2881) {
            errorMessage("Minutes before sunrise is set to $startBeforeSunrise, which is over " + Math.floor(startBeforeSunrise / 60 / 24) + " days.")
        } else if(startBeforeSunset > 1441) {
            errorMessage("Minutes before sunrise is set to $startBeforeSunrise, which is over a day.")
        }
        if(startAfterSunrise > 2881) {
            errorMessage("Minutes after sunset is set to $startBeforeSunrise, which is over " + Math.floor(startAfterSunrise / 60 / 24) + " days.")
        } else if(startBeforeSunrise > 1441) {
            errorMessage("Minutes after sunset is set to $startBeforeSunrise, which is over a day.")
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
    if(stopBeforeSunset){
        if(stopBeforeSunset > 2881) {
            errorMessage("Minutes before sunset is set to $stopBeforeSunset, which is over " + Math.floor(stopBeforeSunset / 60 / 24) + " days. That's kinda dumb, and may not work right.")
        } else if(stopBeforeSunset > 1441) {
           errorMessage("Minutes before sunset is set to $stopBeforeSunset, which is over a day. That's kinda dumb, and may not work right.")
        }
        if(stopBeforeSunrise > 2881) {
            errorMessage("Minutes before sunrise is set to $stopBeforeSunrise, which is over " + Math.floor(stopBeforeSunrise / 60 / 24) + " days. That's kinda dumb, and may not work right.")
        } else if(stopBeforeSunset > 1441) {
            errorMessage("Minutes before sunrise is set to $stopBeforeSunrise, which is over a day. That's kinda dumb, and may not work right.")
        }
    }
    if(stopAfterSunset){
        if(stopAfterSunset > 2881) {
            errorMessage("Minutes after sunset is set to $stopBeforeSunset, which is over " + Math.floor(stopAfterSunset / 60 / 24) + " days. That's kinda dumb, and may not work right.")
        } else if(stopBeforeSunset > 1441) {
            errorMessage("Minutes after sunset is set to $stopBeforeSunset, which is over a day. That's kinda dumb, and may not work right.")
        }
        if(stopAfterSunrise > 2881) {
            errorMessage("Minutes after sunset is set to $stopBeforeSunrise, which is over " + Math.floor(stopAfterSunrise / 60 / 24) + " days. That's kinda dumb, and may not work right.")
        } else if(stopBeforeSunrise > 1441) {
            errorMessage("Minutes after sunset is set to $stopBeforeSunrise, which is over a day. That's kinda dumb, and may not work right.")
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
    if(levelOn > 100) errorMessage("Beginning brightness can't be more than 100. Correct before saving.")
    if(levelOff > 100) errorMessage("Ending brightness can't be more than 100. Correct before saving.")
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
    if(tempOn > 5400) errorMessage("Beginning color temperature can't be more than 5,400. Correct before saving.")
    if(tempOn && tempOn < 1800) errorMessage("Beginning color temperature can't be less than 1,800. Correct before saving.")
    if(tempOff > 5400) errorMessage("Ending color temperature can't be more than 1,800. Correct before saving.")
    if(tempOff && tempOff < 1800) errorMessage("Ending color temperature can't be less than 1,800. Correct before saving.")
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
    if(hueOn > 100) errorMessage("Beginning hue can't be more than 100. Correct before saving.")
    if(hueOff && hueOff > 100) errorMessage("Ending hue can't be more than 100. Correct before saving.")
    if(satOn > 100) errorMessage("Beginning saturation can't be more than 100. Correct before saving.")
    if(satOff && satOff > 100) errorMessage("Ending saturation can't be more than 100. Correct before saving.")
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
	logTrace(663,"Installed")

    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
	initialize()
}

def updated() {
	logTrace(670,"Updated")
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
	logTrace(685,"Initialized")
}

def getStartTime(){
    if(timeStartType == "None") return false
    if(timeStartType == "Time"){
        if(!timeStart) {
            logTrace(692,"ERROR: timeStartType set to Time, but no timeStart entered")
            return false
        } else {
            value = timeStart
        }
    } else if(timeStartType == "Sunrise"){
        if(!startSunriseType) {
            logTrace(699,"ERROR: timeStartType set as Sunrise, but no startSunriseType selected")
            return false
        } else {
            value = (startSunriseType == "Before" ? parent.getSunrise(startBeforeSunrise * -1,app.label) : parent.getSunrise(startAfterSunrise,app.label))
        }
    } else if(timeStartType == "Sunset"){
        if(!startSunsetType) {
            logTrace(706,"ERROR: timeStartType set as Sunset, but no startSunsetType selected")
            return false
        } else {
            value = (startSunsetType == "Before" ? parent.getSunset(startBeforeSunset * -1,app.label) : parent.getSunset(startAfterSunset,app.label))
        }
    } else {
        return false
    }
    logTrace(714,"Start time set as " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", value).format("h:mma MMM dd, yyyy", location.timeZone))
    return value
}

def getStopTime(){
    //Test if initialized; otherwise, app will crash for unknown reason
    if(timeStopType == "None") return false
    if(timeStopType == "Time"){
        if(!timeStop) {
            logTrace(723,"ERROR: timeStopType set to Time, but no timeStop entered")
            return false
        } else {
            value = timeStop
        }
    } else if(timeStopType == "Sunrise"){
        if(!stopSunriseType) {
            logTrace(730,"ERROR: timeStopType set as Sunrise, but no stopSunriseType selected")
            return false
        } else {
            value = (stopSunriseType == "Before" ? parent.getSunrise(stopBeforeSunrise * -1,app.label) : parent.getSunrise(stopAfterSunrise,app.label))
        }
    } else if(timeStopType == "Sunset"){
        if(!stopSunsetType) {
            logTrace(737,"ERROR: timeStopType set as Sunset, but no stopSunsetType selected")
            return false
        } else {
            value = (stopSunsetType == "Before" ? parent.getSunset(stopBeforeSunset * -1,app.label) : parent.getSunset(stopAfterSunset,app.label))
        }
    } else {
        return false
    }
    // If timeStop before timeStart, add a day
    if(timeToday(timeStart, location.timeZone).time > timeToday(timeStop, location.timeZone).time) value = parent.getTomorrow(value,app.label)
    logTrace(747,"Stop time set as " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", value).format("h:mma MMM dd, yyyy", location.timeZone))
    return value
}

def dimSpeed(){
	if(settings.multiplier != null){
		logTrace(753,"dimSpeed set to $settings.multiplier")
		return settings.multiplier
	}else{
		logTrace(756,"dimSpeed set to 1.2")
		return 1.2
	}
}

def getDefaultLevel(device){
	// Set map with fake values
	defaults=[level:'Null',temp:'Null',hue:'Null',sat:'Null']

	// If no device match, return null
	timeDevice.findAll( {it.id == device.id} ).each {
		logTrace(767,"getDefaultLevel matched device $device and $it")
		match = true
	}
	if(!match) return defaults

	// if no start levels, return nulls
	if((!levelOn || !levelOff || levelOn == levelOff) && (!tempOn || !tempOff || tempOn == tempOff) && (!hueOn || !hueOff || hueOn == hueOff) && (!satOn || !satOff || satOn == satOff)){
		logTrace(774,"Returning null as start level for $device")
		return defaults
	}

	// if not between start and stop time, return nulls
	if(timeStop && !parent.timeBetween(timeStart, timeStop, app.label)) return defaults

	// If disabled, return nulls
	if(disable || state.disableAll) {
		logTrace(783,"Default level for $device null, schedule disabled")
		return defaults
	}

	// If mode set and node doesn't match, return nulls
	if(ifMode){
		if(location.mode != ifMode) {
			logTrace(790,"Default level for $device null, mode $ifMode")
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

		logTrace(817,"Default level $defaults for $device")
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
    
    logTrace(886,"Default levels $defaults for $device")
    return defaults
}

// Schedule initializer
def initializeSchedules(){
	unschedule()

	// If disabled, return null
	if(disable || state.disableAll) {
		logTrace(896,"initializeSchedules returning; schedule disabled")
		return
	}

	// Immediately start incremental schedules
	// If incremental
	if(timeStop){
		// Check if any incremental changes to make; if so, just run incrementalSchedule for the first time
		if((levelOn && levelOff && levelOn != levelOff) || (tempOn && tempOff && tempOn != tempOff) || (hueOn && hueOff && hueOn != hueOff) || (satOn && satOff && satOn != satOff))
			incrementalSchedule()
	}

	// Get start time cron data
    if(timeDays) weekDays = weekDaysToNum()
	hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
	minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
	
	// Schedule next day incrementals, if no start action to be scheduled 
	if(timeOn != "On" && timeOn != "Off" && timeOn != "Toggle" && !modeChangeOn) {
        if((levelOn && levelOff && levelOn != levelOff) || (tempOn && tempOff && tempOn != tempOff) || (hueOn && hueOff && hueOn != hueOff) || (satOn && satOff && satOn != satOff)){
            if(weekDays) {
                logTrace(917,"Scheduling incrementalSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours ? * $weekDays)")
                schedule("0 " + minutes + " " + hours + " ? * " + weekDays, incrementalSchedule)
            } else {
                logTrace(920,"Scheduling incrementalSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours * * ?)")
                schedule("0 " + minutes + " " + hours + " * * ?", incrementalSchedule)
            }
        }
	// Schedule next day's starting on/off/toggle
	} else if(timeOn == "On" || timeOn == "Off" || timeOn == "Toggle" || modeChangeOn){
		if(weekDays) {
			logTrace(927,"Scheduling runDayOnSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours ? * $weekDays)")
			schedule("0 " + minutes + " " + hours + " ? * " + weekDays, runDayOnSchedule)
		} else {
			logTrace(932,"Scheduling runDayOnSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours * * ?)")
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
				logTrace(944,"Scheduling runDayOffSchedule for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours ? * $weekDays)")
				schedule("0 " + minutes + " " + hours + " ? * " + weekDays, runDayOffSchedule, [overwrite: false])
			}else {
				logTrace(947,"Scheduling runDayOffSchedule " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mma MMM dd, yyyy", location.timeZone) + " (0 $minutes $hours * * ?)")
				schedule("0 " + minutes + " " + hours + " * * ?", runDayOffSchedule, [overwrite: false])
			}
		}
	}
}

//settings up schedules for level/temp
def incrementalSchedule(device = "Null",manualOverride=false){
    if(!timeStart) timeStart = getStartTime()
    if(!timeStop) timeStop = getStopTime()
	// If disabled, return null
	if(disable || state.disableAll) {
		logTrace(960,"Function incrementalSchedule returning; schedule disabled")
		return
	}

	// If no device match, return null
	if(device != "Null"){
		timeDevice.findAll( {it.id == device.id} ).each {
            if(parent.stateOn(it,app.label)){
			    logTrace(968,"Matched device $device and $it")
			    match = true
            } else {
			    logTrace(971,"Matched device $device and $it, but device isn't on; no need for incremental schedule")
            }
		}
		if(!match) return
    } else {
        if(!parent.multiStateOn(timeDevice)){
		    logTrace(977,"Since $timeDevice is off, stopping recurring schedules")
            return
        }
    }

    if(timeLevelPico && manualOverride && (!timeOff && !modeChangeOff && !levelOff)){
        logTrace(983,"incrementalSchedule exiting, manual override enabled for $device")
        unschedule(incrementalSchedule)
        return
	}

	// Check if correct day and time just so we don't keep running forever
	if(timeDays && !parent.todayInDayList(timeDays,app.label)) return

	// If mode set and node doesn't match, return null
	if(ifMode && location.mode != ifMode) {
		logTrace(993,"incrementalSchedule returning, mode $ifMode")
		return
	}
    
    if((levelOn && levelOff && levelOn != levelOff) || (tempOn && tempOff && tempOn != tempOff) || (hueOn && hueOff && hueOn != hueOff) || (satOn && satOff && satOn != satOff))
        return

	// If between start and stop time (if start time after stop time, then if after start time)
    if(parent.timeBetween(timeStart, timeStop, app.label)){        
        // Run first iteration now
        runIncrementalSchedule()
        //TO-DO: Figure out how long between each change, and schedule for that duration, rather than every X seconds.
        runIn(20,incrementalSchedule)
        logTrace(1006,"Scheduling incrementalSchedule for 20 seconds")
        return true
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
	logTrace(1111,"weekDaysToNum returning $dayString")
	return dayString
}

def logTrace(lineNumber,message){
	log.trace "$app.label (line $lineNumber) -- $message"
}
