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
*  Version: 0.3.01
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
    page(name: "setup", install: true, uninstall: true) {
        section() {

				// Set disable all
				if(timeDisableAll) {
					state.timeDisable = true
				} else {
					state.timeDisable = false
				}

				// If all disabled, force reenable
				if(state.timeDisable){
					input "timeDisableAll", "bool", title: "All schedules are disabled. Reenable?", defaultValue: false, submitOnChange:true
				// If schedule disabled, show only basic options
				} else if(timeDisable){
					label title: "• <b><font color=\"#000099\">Assign a name:</font></b>", required: true
					paragraph "• <b><font color=\"#000099\">Device(s) to control?</font></b>"
					input "timeDevice", "capability.switch", title: "<b>Device(s):</b>", multiple: true, required: true, submitOnChange:true
					input "timeDisable", "bool", title: "<b><font color=\"#000099\">This schedule is disabled.</font></b> Reenable it?", submitOnChange:true
					// If no devices selected, don't show anything else (except disabling)
					if(timeDevice){
						paragraph "<center><b><font color=\"#000099\">• Starting •</font></b></center>", width: 6
						// If time start not entered, don't show stop time
						if(timeStart){
							paragraph "<center><b><font color=\"#000099\">• Stopping •</font></b></center>", width: 6
						} else {
							paragraph "<font color=\"LightGray\">Stopping</font>", width: 6
						}
						input "timeStart", "time", title: "<b>Start Time</b> (12:00AM if all day; Optional)", required: true, width: 6,enable:false
						// If time start not entered, don't show stop time
						if(timeStart){
							input "timeStop", "time", title: "<b>Stop Time</b> (11:59PM for remaining day; Optional)", required: false, width: 6
						} else {
							paragraph "<font color=\"LightGray\">Stop Time</font>", width: 6
						}
						input "timeDays", "enum", title: "<b>On these days</b> (Optional):", required: false, multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"]
					}
					
					input "timeDisableAll", "bool", title: "Disable <b>ALL</b> schedules?", defaultValue: false, submitOnChange:true
				// If not disabled, show all options
				} else {
					label title: "• <b><font color=\"#000099\">Assign a name:</font></b>", required: true
					paragraph "• <b><font color=\"#000099\">Device(s) to control?</font></b>"
					input "timeDevice", "capability.switch", title: "<b>Device(s):</b>", multiple: true, required: true, submitOnChange:true
					input "timeDisable", "bool", title: "Disable this schedule?", defaultValue: false, submitOnChange:true
					// If no devices selected, don't show anything else (except disabling)
					if(timeDevice){
						paragraph "<center><b><font color=\"#000099\">• Starting •</font></b></center>", width: 6

						// Stop column label: If no start time, don't show
						if(timeStart){
							paragraph "<td><center><b><font color=\"#000099\">• Stopping •</font></b></center>", width: 6
						} else {
							paragraph "<font color=\"LightGray\">Stopping (set Start Time)</font>", width: 6
						}

						// Start Time field
						input "timeStart", "time", title: "<b>Start Time</b> (12:00AM if all day; Optional)", required: true, width: 6, submitOnChange:true

						// Stop Time field: Only show if start time entered
						if(timeStart){
							input "timeStop", "time", title: "<b>Stop Time</b> (11:59PM for remaining day; Optional)", required: false, width: 6, submitOnChange:true
						} else {
							paragraph "<font color=\"LightGray\">Stop Time</font>", width: 6
						}

						// Days
						input "timeDays", "enum", title: "<b>On these days</b> (Optional):", required: false, multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"]

						// If no start time, don't show any start or stop options
						if(timeStart){
							// Start On/Off/Toggle
							input "timeOn", "enum", title: "<b>Turn device(s) on/off at start time?</b> (Optional)", multiple: false, required: false, width: 6, options: ["Turn On", "Turn Off", "Toggle"]

							// Stop On/Off/Toggle: Only show if Stop Time entered
							if(timeStop){
								input "timeOff", "enum", title: "<b>Turn device(s) on/off at stop time?</b> (Optional)", multiple: false, required: false, width: 6, options: ["Turn On", "Turn Off", "Toggle"]
							} else {
								paragraph "<font color=\"LightGray\">Set Stop Time for options</font>", width: 6
							}

							// Start Level
							input "timeLevelOn", "number", title: "<b>Starting default brightness:</b> (Optional: 1-100; Default 100)", required: false, width: 6, submitOnChange:true

							// Stop Level: Only show if Stop Time entered
							if(timeStop){
								input "timeLevelOff", "number", title: "<b>Ending default brightness:</b> (Optional: 1-100)", required: false, width: 6
							} else {
								paragraph "", width: 6
							}

							// Start Temp
							input "timeTempOn", "number", title: "<b>Starting temperature color:</b> (Optional: 2200-4500; Default 100)", required: false, width: 6, submitOnChange:true

							// Stop Temp: Only show if Stop Time entered
							if(timeStop){
								input "timeTempOff", "number", title: "<b>Ending temperature color:</b> (Optional: 2200-4500)", required: false, width: 6
							} else {
								paragraph "", width: 6
							}

							// Start Hue
							input "timeHueOn", "number", title: "<b>Starting color hue:</b> (Optional: 0-100; 1/100 = red, 33 = green, 66 = blue)", required: false, width: 6

							// Stop Hue: Only show if Stop Time entered
							if(timeStop){
								input "timeHueOff", "number", title: "<b>Ending color hue:</b> (Optional: 0-100)", required: false, width: 6
							} else {
								paragraph "", width: 6
							}

							//Start Saturation
							input "timeSatOn", "number", title: "<b>Starting color saturation:</b> (Optional: 0-100; Default 100)", required: false, width: 6

							// Stop Saturation: Only show if Stop Time entered
							if(timeStop){
								input "timeSatOff", "number", title: "<b>Ending color saturation:</b> (Optional: 0-100)", required: false, width: 6
							} else {
								paragraph "", width: 6
							}

							// Change Mode on Start
							input "timeModeChangeOn", "mode", title: "<b>Change Mode at start?</b> (Optional)", required: false, width: 6

							// Change Mode on Stop: Only show if Time Stop entered
							if(timeStop){
								input "timeModeChangeOff", "mode", title: "<b>Change Mode at stop?</b> (Optional)", required: false, width: 6
							} else {
								paragraph "", width: 6
							}

							// Start only if Mode
							input "timeStartIfMode", "mode", title: "<b>Only if Mode is already:</b> (Optional)", required: false, width: 12
							if(timeLevelOn){
								input "timeLevelIfLower", "enum", title: "<b>Don't change Level if already:</b> (Optional)", multiple: false, required: false, width: 6, options: ["Lower", "Higher"]
							} else {
								paragraph "", width: 6
							}
							if(timeTempOn){
								input "timeTempIfLower", "enum", title: "<b>Don't change temperature color if already:</b> (Optional)", multiple: false, required: false, width: 6, options: ["Lower", "Higher"]
							} else {
								paragraph "", width: 6
							}
							paragraph "Options can be combined. To have a default brightness of 50% after 9pm, set start time and start level (but do not have turn on). To have device turn on at 7am and gradually brighten for a half hour from 1% to 100%, set start time of 7am, stop time of 7:30am, and at start time turn on with level of 1, and a stop time of 7:30a, with a level of 100."

						}
					}

				
				input "timeDisableAll", "bool", title: "Disable <b>ALL</b> schedules?", defaultValue: false, submitOnChange:true
				}
			
		}
    }
}


def installed() {
	if(app.getLabel().length() < 4)  app.updateLabel("Time - " + app.getLabel())
    if(app.getLabel().substring(0,4) != "Time") app.updateLabel("Time - " + app.getLabel())
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
	    if(app.getLabel().substring(0,4) != "Time") app.updateLabel("Time - " + app.getLabel())
    log.info "Time initialized"
	def appId = app.getId()
	unschedule(setDayOnSchedule)
	unschedule(setDayOffSchedule)
	unschedule(firstStageSchedule)
	unschedule(secondStageSchedule)
	if(timeDisableAll) {
		state.timeDisable = true
	} else {
		state.timeDisable = false
		if(!timeDisable) setDaySchedule()
	}
	state.debug = true
	//if(timeOn == "Turn On" || timeOn == "Turn Off" || timeOn == "Toggle") runDaily(timeToday(timeStart, location.timeZone), runDaySchedule())
	//	if(timeOn == "Turn Off") runDaily(timeToday(timeStart, location.timeZone), parent.multiOff(timeDevice,appId))
	//	if(timeOn == "Toggle") runDaily(timeToday(timeStart, location.timeZone), parent.toggle(timeDevice,appId))
	//	if(timeLevelOn == "Dim" && !timeOff) runDaily(timeToday(timeStart, location.timeZone), parent.dim(timeDevice,appId))

	
}

def dimSpeed(){
    if(settings.multiplier != null){
        return settings.multiplier
    }else{
        return 1.2
    }
}

def getDefaultLevel(device){
	// *******************************************************************************
	// ** TO DO: Merge level,temp, hue and sat into one function (and return array) **
	// *******************************************************************************
	defaults=[level:'Null',temp:'Null',hue:'Null',sat:'Null']
	
	// If no device match, return null
	match = false
	timeDevice.each{
		if(it.id == device.id)  match = true
	}
	if(match == false) return defaults
	
	// Set map with fake values (must test if equals "a" as if null
	
	// If no default level, return null
	if(!timeLevelOn && !timeTempOn && !timeHueOn && !timeSatOn) return defaults
	
	// If disabled, return null
	if(timeDisable || state.timeDisableAll) return defaults
	
	// If no device match, return null
	match = false
	timeDevice.each{
		if(it.id == device.id)  match = true
	}
	if(match == false) return defaults
	

	// If mode set and node doesn't match, return null
	if(timeStartIfMode){
		if(location.mode != timeStartIfMode) return defaults
	}

	// If before start time, return null
	if(timeStart){
		if(now() < timeToday(timeStart, location.timeZone).time) return defaults
	}

	// If after time stop, return null
	if(timeStop){
		if(now() > timeToday(timeStop, location.timeZone).time) return defaults
	}

	// If not correct day, return null
	if(timeDays){
		def df = new java.text.SimpleDateFormat("EEEE")
		df.setTimeZone(location.timeZone)
		def day = df.format(new Date())
		if(!timeDays.contains(day)) return defaults
	}

	// Get current level
	currentLevel = device.currentLevel
	currentTemp = device.currentColorTemperature
	currentHue = device.currentHue
	currentSat = device.currentSaturation
	
	// If no stop time or no start level, return start level
	if(!timeStop) {
		if(timeLevelOn && !timeLevelOff){
			defaults = [level: timeLevelOn]
			// If start level is too dim, and set not to dim, return current level
			if(timeLevelIfLower){
				if(timeLevelIfLower == "Lower"){
					if(parent.stateOn(device) && currentLevel < timeLevelOn) defaults = [level: currentLevel]
				// If start level is too bright, and set not to brighten, return current level
				} else if(timeLevelIfLower == "Higher"){
					if(parent.stateOn(device) && currentLevel > timeLevelOn) defaults = [level: currentLevel]
				}
			}
		}

		if(timeTempOn && !timeTempOff){
			defaults = [temp: timeTempOn]
			// If start temp is too low, and set not to go lower, return current level
			if(timeTempIfLower){
				if(timeTempIfLower == "Lower"){
					if(parent.stateOn(device) && currentTemp < timeTempOn) defaults = [temp: currentTemp]
				// If start temp is too high, and set not to go higher, return current level
				} else if(timeTempIfLower == "Higher"){
					if(parent.stateOn(device) && currentTemp > timeTempOn) defaults = [temp: currentTemp]
				}
			}
		}
		if(timeHueOn && !timeHueOff){
			// Return start level
			if(timeHueOn) defaults = [hue: timeHueOn]
		}
		if(timeSatOn && !timeSatOff){
			// Return start level
			if(timeSatOn) defaults = [sat: timeSatOn]
		}
	}

	// If default level is the current level, return current level
	if(timeLevelOn && timeLevelOn == currentLevel && !timeLevelOff && !defaults.level) defaults = [level: currentLevel]
	if(timeTempOn && timeTempOn == currentTemp && !timeTempOff && !defaults.temp) defaults = [temp: currentTemp]
	if(timeHueOn && timeHueOn == currentHue && !timeHueOff && !defaults.hue) defaults = [hue: currentHue]
	if(timeSatOn && timeSatOn == currentSat && !timeSatOff && !defaults.sat) defaults = [sat: currentSat]

	// If there's a stop time and stop level, and after start time

	if(timeStart && timeStop){
		// Calculate proportion of time already passed from start time to endtime
		hours1 = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
		minutes1 = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
		seconds1 = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
		hours2 = new Date().format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
		minutes2 = new Date().format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
		seconds2 = new Date().format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
		// Calculate new level
		if(timeLevelOff && timeLevelOn) {
			newLevel = (timeLevelOff - timeLevelOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + timeLevelOn as int
		} else if(timeLevelOn) {
			newLevel = timeLevelOn
		}
		if(timeTempOff && timeTempOn) {
			newTemp = (timeTempOff - timeTempOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + timeTempOn as int
		} else if(timeTempOn){
			newTemp = timeTempOn
		}
		
		if(timeHueOff && timeHueOn) {
			newHue = (timeHueOff - timeHueOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + timeHueOn as int
		} else if(timeHueOn){
			newHue = timeHueOn
		}
		if(timeSatOff && timeSatOn) {
			newSat = (timeSatOff - timeSatOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + timeSatOn as int
		} else if (timeSatOn){
			newSat = timeSatOn
		}

		if(newLevel && defaults.level == "Null"){
			// If new level is too dim, and set not to dim, return current level
			if(timeLevelIfLower){
				if(timeLevelIfLower == "Lower"){
					if(parent.stateOn(device) && currentLevel < newLevel) defaults.put("level",currentLevel)
				}
				// If new level is too bright, and set not to brighten, return current level
				if(timeLevelIfLower == "Higher"){
					if(parent.stateOn(device) && currentLevel > newLevel) defaults.put("level",currentLevel)
				}
			}
		}

		if(defaults.level == "Null" && newLevel) defaults.put("level",newLevel)
		if(parent.isFan(device) && defaults.level != "Null") defaults.put("level",roundFanLevel(defaults.level))

		// Set temp
		if(newTemp && defaults.temp == "Null"){
			// If new level is too low, and set not to go lower, return current level
			if(timeTempIfLower){
				if(timeTempIfLower == "Lower"){
					if(parent.stateOn(device) && currentTemp < newTemp) defaults.put("temp",currentTemp)
				}
				// If new level is too high, and set not to go higher, return current level
				if(timeTempIfLower == "Higher"){
					if(parent.stateOn(device) && currentTemp > newTemp) defaults.put("temp",currentTemp)
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
	if(defaults.level == "Null") log.debug "Time: No default level match found for $device."
	return defaults
}

// New schedule initializer
// Not completed or implemented
// Will replace setDaySchedule (perhaps others)
// Fixes: wasn't creating day schedule for mode only change
//        Incorrectly creating increment schedule when nothing is on
//        Failing to create increment schedule immediatly (when initializing)
def initializeSchedules(){
	if(!timeStart) return
	
	// ****************************
	// TO-DO - make sure reenabling reschedules
	// *****************************
	
	// If disabled, return null
	if(timeDisable || state.timeDisableAll) return
	
	// Immediately start incremental schedules
	// If incremental
	if(timeStop){
		// If correct Mode
		if(timeStartIfMode && location.mode != timeStartIfMode){
			// If correct day
			def df = new java.text.SimpleDateFormat("EEEE")
			df.setTimeZone(location.timeZone)
			def day = df.format(new Date())
			if(timeDays && timeDays.contains(day)){
				// If between start and stop time
				if(now() > timeToday(timeStart, location.timeZone).time && now() < timeToday(timeStop, location.timeZone).time){
					// If anything is on
					if(parent.multiStateOn(timeDevice)){
						secondStageSchedule()
					}
				}
			}
		}
	}

	// Get start time cron data
	weekDays = weekDaysToNum()
	hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
	minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()

	// Schedule next day incrementals, if no start action to be scheduled 
	// Change to scheduling both - just skip secondStageSchedule
	// FIrst, check if a day schedule - if so, add 20 seconds
	// Otherwise, schedule without seconds
	if(timeOn != "Turn On" && timeOn != "Turn Off" && timeOn != "Toggle" && !timeModeChangeOn) {
		if(weekDays) {
			schedule("0 " + minutes + " " + hours + " ? * " + weekDays, secondStageSchedule)
		} else {
			schedule("0 " + minutes + " " + hours + " * * ?", secondStageSchedule)
		}
	// Schedule next day's starting on/off/toggle
	} else if(timeOn == "Turn On" || timeOn == "Turn Off" || timeOn == "Toggle" || timeModeChangeOn){
		if(weekDays) {
			schedule("0 " + minutes + " " + hours + " ? * " + weekDays, runDayOnSchedule)
		} else {
			schedule("0 " + minutes + " " + hours + " * * ?", runDayOnSchedule)
		}
	}
																									 
	// Schedule next day's ending on/off/toggle														  
	if(timeOff == "Turn On" || timeOff == "Turn Off" || timeOff == "Toggle" || timeModeChangeOff){
		if(timeStop){
			hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('HH').toInteger()
			minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('mm').toInteger()
			if(weekDays) {
				schedule("0 " + minutes + " " + hours + " ? * " + weekDays, runDayOffSchedule, [overwrite: false])
			}else {
				schedule("0 " + minutes + " " + hours + " * * ?", runDayOffSchedule, [overwrite: false])
			}
		}
	}															  
}

// Setup on/off day schedule event
// Called from initialize (then loops from runDayOnSchedule and runDayOffSchedule)
def setDaySchedule(){
	if(!timeStart) return
	if(timeDisable || state.timeDisableAll) return
	if(timeStop){
		if(timeToday(timeStop, location.timeZone).time < timeToday(timeStart, location.timeZone).time) {
			log.debug "Time: Stop time is before Start time. Cancelling schedule."
			return
		} else {
			if(now() > timeToday(timeStart, location.timeZone).time && now() < timeToday(timeStop, location.timeZone).time) secondStageSchedule()
		}
	}
	weekDays = weekDaysToNum()
	if(timeStart){
		hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
		minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
		// schedule starting "second stage" increment
		if(timeOn != "Turn On" && timeOn != "Turn Off" && timeOn != "Toggle" && timeOff != "Turn On" && timeOff != "Turn Off" && timeOff != "Toggle") {
			if(weekDays) {
				schedule("0 " + minutes + " " + hours + " ? * " + weekDays, secondStageSchedule)
			} else {
				schedule("0 " + minutes + " " + hours + " * * ?", secondStageSchedule)
			}
		// Schedule starting on/off/toggle
		} else if(timeOn == "Turn On" || timeOn == "Turn Off" || timeOn == "Toggle"){
			if(weekDays) {
				schedule("0 " + minutes + " " + hours + " ? * " + weekDays, runDayOnSchedule)
			} else {
				schedule("0 " + minutes + " " + hours + " * * ?", runDayOnSchedule)
			}
		}
		// schedule stopping on/off/toggle
		if(timeOff == "Turn On" || timeOff == "Turn Off" || timeOff == "Toggle"){
			if(timeStop){
				hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('HH').toInteger()
				minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('mm').toInteger()
				if(weekDays) {
					schedule("0 " + minutes + " " + hours + " ? * " + weekDays, runDayOffSchedule, [overwrite: false])
				}else {
					schedule("0 " + minutes + " " + hours + " * * ?", runDayOffSchedule, [overwrite: false])
				}
			}
		}

	}
}

def reschedule(device){
	match = false
	timeDevice.each{
		if(it.id == device.id) match = true
	}
	if(match) firstStageSchedule()
}

// Allows pausing scheduling for a minute
def firstStageSchedule(){
	unschedule(firstStageSchedule)
	unschedule(secondStageSchedule)
	if(timeDisable || state.timeDisableAll) return
	runIn(30,secondStageSchedule)
	log.info "Time: Pausing updates for 30 seconds for $timeDevice."
}

//settings up schedules for level/temp
def secondStageSchedule(){
	// Probably don't need this, but maybe possible to have multiple schedules??
	unschedule(secondStageSchedule)
	
	// If no default level, return null
	if(!timeLevelOn) return
	
	// If disabled, return null
	if(timeDisable || state.timeDisableAll) return
	if(!timeStop) return
	var = parent.multiStateOn(settings.timeDevice)
	if(!parent.multiStateOn(timeDevice)) return
	// If nothing to do, exit
	if((!timeLevelOn && !timeLevelOff) || (!timeTempOn && !timeTempOff) || timeOn == "Turn Off" || !timeStart || !timeStop) return

	// If before or after time frame
    if(now() < timeToday(timeStart, location.timeZone).time) return
    if(timeStop){
        if(now() > timeToday(timeStop, location.timeZone).time) return
    }
	runMultiSchedule()
	runIn(20,secondStageSchedule)
	
	log.info "Time: Scheduling update for 20 seconds for $timeDevice."
}

// run scheduled level/temp incremental changes
// scheduled function called from secondStageSchedule
def runMultiSchedule(){
	if(timeDisable || state.timeDisableAll) return
	
	// If no stop time, then don't need incremental changes
	if(!timeStop) return

	// If nothing is on, exit
	if(!parent.multiStateOn(timeDevice)) return
	
	// If mode set and node doesn't match, exit
	if(timeStartIfMode){
		if(location.mode != timeStartIfMode) return
	}
	
	// Loop through devices
	timeDevice.each{
		// Ignore devices that aren't on
		if(parent.stateOn(it)){
			// Set level
			if(timeLevelOn && parent.isDimmable(it)){
				currentLevel = it.currentlLevel
				defaultLevel = getDefaultLevel(it)
				if(defaultLevel){
					if(defaultLevel != currentLevel) {
						if(timeLevelIfLower == "Lower"){
							if(currentLevel < defaultLevel) return 
						}
						if(timeLevelIfLower == "Higher"){
							if(currentLevel > defaultLevel) return
						}
						parent.setToLevel(it,defaultLevel,app.getId())
					}
				}
			}
			// Set temp
			if(timeTempOn && parent.isTemp(it)){
				currentTemp = it.currentColorTemperature
				defaultTemp = getDefaultTemp(it)
				if(defaultTemp){
					if(defaultTemp - currentTemp > 4 || defaultTemp - currentTemp < -4) {
						if(timeTempIfLower == "Lower"){
							if(currentTemp < defaultTemp) return 
						}
						if(timeTempIfLower == "Higher"){
							if(currentTemp > defaultTemp) return
						}
						parent.singleTemp(it,defaultTemp,app.getId())
					}
				}
			}
			
			// Set hue and sat
			if(timeHueOn && parent.isColor(it)){
				defaultHue = getDefaultHue(it)
			}
			if(timeHueOn && parent.isColor(it)){
				defaultSat = getDefaultSat(it)
			}
			// If either Hue or Sat, but not both, set the other to current
			if(defaultHue || defaultSat) {
				if(!defaultHue) defaultHue = it.currentHue
				if(!defaultSat) defaultSat = it.currentSaturation
				parent.singleColor(it,defaultHue,defaultSat,app.getId())
			}
		}
	}
}

//Scheduled function called from setDaySchedule
def runDayOnSchedule(){
	if(timeDisable || state.timeDisableAll) return
	if(timeOn != "Turn On" && timeOn != "Turn Off" && timeOn != "Toggle") return
	// if mode return
	if(timeStartIfMode){
		if(location.mode != timeStartIfMode) return
	}
	if(timeModeChangeOn) setLocationMode(timeModeChangeOn)
	if(timeOn == "Turn On"){
		parent.multiOn(timeDevice,app.getId())
	} else if(timeOn == "Turn Off"){
		parent.multiOff(timeDevice,app.getId())
	} else if(timeOn == "Toggle"){
		parent.toggle(timeDevice,app.getId())
	}
	setDaySchedule()
}

//Scheduled function called from setDaySchedule
def runDayOffSchedule(){
	if(timeDisable || state.timeDisableAll) return
	if(timeModeChangeOff) setLocationMode(timeModeChangeOff)
	if(timeOff != "Turn On" && timeOff != "Turn Off" && timeOff != "Toggle") return
	// if mode return
	if(timeStartIfMode){
		if(location.mode != timeStartIfMode) return
	}
	if(timeOff == "Turn On"){
	   parent.multiOn(timeDevice,app.getId())
	} else if(timeOff == "Turn Off"){
	   parent.multiOff(timeDevice,app.getId())
	} else if(timeOff == "Toggle"){
	   parent.toggle(timeDevice,app.getId())
	}
	setDaySchedule()
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
	return dayString
}
