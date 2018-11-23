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
*  Version: 0.3.04
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
					input "timeDisableAll", "bool", title: "<b>All schedules are disabled.</b> Reenable?", defaultValue: false, submitOnChange:true
				// If schedule disabled, show only basic options
				} else if(timeDisable){
					paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this schedule:</b></div>"
					label title: "Schedule name?", required: true, submitOnChange:true
					if(app.label){
						paragraph "<div style=\"background-color:BurlyWood\"><b> Select which devices to schedule:</b></div>"
						input "timeDevice", "capability.switch", title: "Device(s)?", multiple: true, required: true, submitOnChange:true
						input "timeDisable", "bool", title: "<b><font color=\"#000099\">Schedule is disabled.</font></b> Reenable it?", submitOnChange:true
						// If no devices selected, don't show anything else (except disabling)
						if(timeDevice){
							paragraph "<div style=\"background-color:BurlyWood\"><b> Select time or mode (Optional):</b></div>"
							if(timeStop){
								input "timeStart", "time", title: "Between start time (12:00AM if all day)", required: false, width: 6, submitOnChange:true
							} else {
								input "timeStart", "time", title: "Between start time (12:00AM if all day; Optional)", required: false, width: 6, submitOnChange:true
							}
							if(timeStart){
								input "timeStop", "time", title: "and stop time (11:59PM for remaining day)", required: false, width: 6, submitOnChange:true
							} else {
								input "timeStop", "time", title: "and stop time (11:59PM for remaining day; Optional)", required: false, width: 6, submitOnChange:true
							}
							input "timeDays", "enum", title: "On these days (Optional):", required: false, multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"]
						}
					}
					
					input "timeDisableAll", "bool", title: "Disable <b>ALL</b> schedules?", defaultValue: false, submitOnChange:true
				// If not disabled, show all options
				} else {
					paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this schedule:</b></div>"
					label title: "Schedule name?", required: true, submitOnChange:true
					if(!app.label){
						paragraph "<div style=\"background-color:BurlyWood\"> </div>"
					} else if(app.label){
						paragraph "<div style=\"background-color:BurlyWood\"><b> Select which devices to schedule:</b></div>"
						input "timeDevice", "capability.switch", title: "Device(s)?", multiple: true, required: true, submitOnChange:true
						input "timeDisable", "bool", title: "Disable this schedule?", defaultValue: false, submitOnChange:true
						if(!timeDevice){
							paragraph "<div style=\"background-color:BurlyWood\"> </div>"
						// If no devices selected, don't show anything else (except disabling)
						} else if(timeDevice){
							paragraph "<div style=\"background-color:BurlyWood\"><b> Enter time or mode (Optional):</b></div>"
							if(timeStop){
								input "timeStart", "time", title: "Between start time (12:00AM if all day)", required: false, width: 6, submitOnChange:true
							} else {
								input "timeStart", "time", title: "Between start time (12:00AM if all day; Optional)", required: false, width: 6, submitOnChange:true
							}
							if(timeStart){
								input "timeStop", "time", title: "and stop time (11:59PM for remaining day)", required: false, width: 6, submitOnChange:true
							} else {
								input "timeStop", "time", title: "and stop time (11:59PM for remaining day; Optional)", required: false, width: 6, submitOnChange:true
							}
							input "timeDays", "enum", title: "On these days (Optional):", required: false, multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"]
							if(!timeStart) {
								paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				
							// If no start time, don't show any start or stop options
							} else if(timeStart){
								// Start On/Off/Toggle
								paragraph "<div style=\"background-color:BurlyWood\"><b> Select whether to turn on or off:</b></div>"
								input "timeOnOffDisable", "bool", title: "<b>Don't turn on or off</b> (leave them as-is). Click to continue setting level, colors and mode.", submitOnChange:true
								if(!timeOnOffDisable) input "timeOn", "enum", title: "Turn on or off devices at " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a", location.timeZone) + "? (Optional)", multiple: false, required: false, width: 6, options: ["Turn On", "Turn Off", "Toggle"]

								// Stop On/Off/Toggle: Only show if Stop Time entered
								if(!timeOnOffDisable) {
									if(timeStop){
										input "timeOff", "enum", title: "Turn on or off devices at " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mm a", location.timeZone) + "? (Optional)", multiple: false, required: false, width: 6, options: ["Turn On", "Turn Off", "Toggle"]
									} else {
										paragraph "Set stop time for options", width: 6
									}
								}

								if(!timeOnOffDisable && !timeOn && !timeOff){
									paragraph "<div style=\"background-color:BurlyWood\"> </div>"
								} else if(timeOnOffDisable || timeOn || timeOff){
									if(timeStop){
										paragraph "<div style=\"background-color:BurlyWood\"><b> Enter beginning and ending brightness:</b></div>"
									} else {
										paragraph "<div style=\"background-color:BurlyWood\"><b> Enter default brightness for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a", location.timeZone) + ":</b></div>"
									}
									input "timeLevelDisable", "bool", title: "<b>Don't change brightness.</b> Click to continue setting colors and mode.", submitOnChange:true
									// Start Level
									if(!timeLevelDisable){
										if(timeStop){
											input "timeLevelOn", "number", title: "Beginning brightness at " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a", location.timeZone) + "? (Optional: 1-100; Default 100)", required: false, width: 6, submitOnChange:true
										} else {
											input "timeLevelOn", "number", title: "At " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a", location.timeZone) + ", default brightness? (Optional: 1-100; Default 100)", required: false, width: 6, submitOnChange:true
										}

										// Stop Level: Only show if Stop Time entered
										if(timeStop){
											input "timeLevelOff", "number", title: "until " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mm a", location.timeZone) + ", change brightness to? (Optional: 1-100)", required: false, width: 6
										} else {
											paragraph "Set stop time for options", width: 6
										}
										
										input "timeLevelIfLower", "enum", title: "Don't change Level if light it's already dimmer or brighter? (Optional)", multiple: false, required: false, options: ["Lower":"Brighter", "Higher":"Dimmer"]
									}
									if(!timeLevelOn && !timeLevelDisable){
										paragraph "<div style=\"background-color:BurlyWood\"> </div>"
									} else if(timeLevelOn || timeLevelDisable){
										if(timeStop){
											paragraph "<div style=\"background-color:BurlyWood\"><b> Enter beginning and ending temperature color:</b></div>"
										} else {
											paragraph "<div style=\"background-color:BurlyWood\"><b> Enter default temperature color for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a", location.timeZone) + ":</b></div>"
										}
										input "timeTempDisable", "bool", title: "<b>Don't change temperature color.</b> Click to continue setting colors and mode.", submitOnChange:true
										paragraph "Temperature color is a range from about 2200 to about 4500, where lower numbers are more \"warm\" and orange, and higher numbers are more \"cool\" and blue."
										if(!timeTempDisable){
											// Start Temp
											if(timeStop){
												input "timeTempOn", "number", title: "Beginning temperature at " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a", location.timeZone) + "? (Optional, default 3400)", required: false, width: 6, submitOnChange:true
											} else {
												input "timeTempOn", "number", title: "At " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a", location.timeZone) + ", default temperature? (Optional, default 3400)", required: false, width: 6, submitOnChange:true
											}

											// Stop Level: Only show if Stop Time entered
											if(timeStop){
												input "timeTempOff", "number", title: "until " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mm a", location.timeZone) + ", change temperature to? (Optional: 2200-4500)", required: false, width: 6
											} else {
												paragraph "Set stop time for options", width: 6
											}
											
											input "timeTempIfLower", "enum", title: "Don't change temperature color if light is already warmer or cooler? (Optional)", multiple: false, required: false, options: ["Lower":"Warmer", "Higher":"Cooler"]
										}
										if(!timeTempOn && !timeTempDisable){
											paragraph "<div style=\"background-color:BurlyWood\"> </div>"
										} else if(timeTempOn || timeTempDisable){
											if(timeStop){
												paragraph "<div style=\"background-color:BurlyWood\"><b> Enter beginning and ending color hue and saturation:</b></div>"
											} else {
												paragraph "<div style=\"background-color:BurlyWood\"><b> Enter default color hue and saturation for " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a", location.timeZone) + ":</b></div>"
											}
											input "timeHueDisable", "bool", title: "<b>Don't change color.</b> Click to continue setting mode.", submitOnChange:true
											if(timeHueDisable){
												paragraph "Color change allows setting hue and saturation for custom colors."
											} else if(!timeHueDisable){
												paragraph "Color hue is a \"wheel\" of colors starting at red (1), and going through green (33) and blue (66), then back to red again (100). Color saturation is a range from 1 to 100 of \"amount\" of hue color, from lighter colors to deeper colors. If hue is set, saturation is required."
												// Start Hue
												if(timeStop){
													input "timeHueOn", "number", title: "Beginning hue at " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a", location.timeZone) + "? (Optional)", required: false, width: 6, submitOnChange:true
												} else {
													input "timeHueOn", "number", title: "At " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a", location.timeZone) + ", default hue? (Optional)", required: false, width: 6, submitOnChange:true
												}

												// Stop Level: Only show if Stop Time entered
												if(timeStop){
													input "timeHueOff", "number", title: "until " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mm a", location.timeZone) + ", change hue to? (Optional)", required: false, width: 6
												} else {
													paragraph "Set stop time for options", width: 6
												}

												// Start Sat
												input "timeSatOn", "number", title: "Saturation?", required: false, width: 6, submitOnChange:true

												// Stop Saturation: Only show if Stop Time entered
												if(timeStop){
													input "timeSatOff", "number", title: "Saturation?", required: false, width: 6
												} else {
													paragraph "", width: 6
												}
											}

											// If color entry is disable, or not entered correcly
											if(!timeHueDisable && !timeSatOn && !timeHueOn || ((timeSatOff && !timeHueOff) && (timeHueOff && !timeSatOff))){
												paragraph "<div style=\"background-color:BurlyWood\"> </div>"
											} else {
												if(timeStop){
													paragraph "<div style=\"background-color:BurlyWood\"><b> Change Mode at " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a", location.timeZone) + " and/or " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mm a", location.timeZone) + ".</b></div>"
												} else {
													paragraph "<div style=\"background-color:BurlyWood\"><b> Change Mode at " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a", location.timeZone) + ".</b></div>"
												}

												// Change Mode on Start
												input "timeModeChangeOn", "mode", title: "<b>At " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a", location.timeZone) + ", change Mode to?</b> (Optional)", required: false, width: 6

												// Change Mode on Stop: Only show if Time Stop entered
												if(timeStop){
													input "timeModeChangeOff", "mode", title: "<b>At " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mm a", location.timeZone) + ", change Mode to?</b> (Optional)", required: false, width: 6
												} else {
													paragraph "", width: 6
												}
												
												// Start only if Mode
												input "timeStartIfMode", "mode", title: "<b>Only if Mode is already:</b> (Optional)", required: false, width: 12
												paragraph "<div style=\"background-color:LightCyan\"><b> Click \"Done\" to continue.</b></div>"
											}
										}
									}
									paragraph "Options can be combined. To have a default brightness of 50% after 9pm, set start time and start level (but do not have turn on). To have device turn on at 7am and gradually brighten for a half hour from 1% to 100%, set start time of 7am, stop time of 7:30am, and at start time turn on with level of 1, and a stop time of 7:30a, with a level of 100."
								}
							}
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
