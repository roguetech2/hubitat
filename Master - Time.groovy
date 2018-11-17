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
*  Version: 0.1.01
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
	/*
    page(name: "setup", install: true, uninstall: true) {
		option = 2
		if(option == 1){
			section() {
				label title: "• <b><font color=\"#000099\">Assign a name:</font></b>", required: true
			}
			section("• <b><font color=\"#000099\">With what to do things?</font></b>") {
				input "timeDevice", "capability.switch", title: "<b>Devices to control:</b>", multiple: true, required: true
				paragraph "Options can be combined. To have a default brightness of 50% after 9pm, set start time and start level (but do not have turn on). To have device turn on at 7am and gradually brighten for a half hour from 1% to 100%, set start time of 7am, stop time of 7:30am, and at start time turn on with level of 1, and a stop time of 7:30a, with a level of 100."
			}
			section("• <b><font color=\"#000099\">When to do things?</font></b>") {
				input "timeStart", "time", title: "<b>Start Time</b> (12:00AM if all day; Optional)", required: true, width:6
				input "timeStop", "time", title: "<b>Stop Time</b> (11:59PM for remaining day; Optional)", required: false, width:6
				input "timeDays", "enum", title: "<b>On these days:</b>", required: false, multiple: true, width:6, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"]
				input "timeStartIfMode", "mode", title: "<b>Only if Mode is already:</b> (Optional)", required: false, width:6

			}
			section("• <b><font color=\"#000099\">What to do at start time?</font></b>") {
				input "timeOn", "enum", title: "<b>Turn On/Off?</b> (Optional)", multiple: false, required: false, width:6, options: ["Turn On", "Turn Off", "Toggle"]
				input "timeModeChangeOn", "mode", title: "<b>Change Mode?</b> (Optional)", required: false, width:6
				input "timeLevelOn", "number", title: "<b>Set brightness level to:</b> (Only if on; Optional: 1-100)", required: false, width:6
				input "timeTempOn", "number", title: "<b>Set temperature color to:</b> (Only if on; Optional: 2200-4500)", required: false, width:6
				input "timeLevelIfLower", "enum", title: "<b>Don't change Level if already:</b> (Optional)", multiple: false, required: false, width:6, options: ["Lower", "Brighter"]
				input "timeTempIfLower", "enum", title: "<b>Don't change temperature color if already:</b> (Optional)", multiple: false, required: false, width:6, options: ["Lower", "Cooler"]

			}
			section("• <b><font color=\"#000099\">What to do at stop time? (Optional)</font></b>") {
				input "timeOff", "enum", title: "<b>Turn On/Off?</b> (Optional)", multiple: false, required: false, width:6, options: ["Turn On", "Turn Off", "Toggle"]
				input "timeModeChangeOff", "mode", title: "<b>Change Mode?</b> (Optional)", required: false, width:6
				input "timeLevelOff", "number", title: "<b>Set brightness level to:</b> (Optional: 1-100)", required: false, width:6
				input "timeTempOff", "number", title: "<b>Set temperature color to:</b> (Optional: 2200-4500)", required: false, width:6
			}
		} else {
		*/

		
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
							paragraph "<td><center><b><font color=\"#000099\">• Stopping •</font></b></center>", width: 6
						} else {
							paragraph "", width: 6
						}
						input "timeStart", "time", title: "<b>Start Time</b> (12:00AM if all day; Optional)", required: true, width: 6,enable:false
						// If time start not entered, don't show stop time
						if(timeStart){
							input "timeStop", "time", title: "<b>Stop Time</b> (11:59PM for remaining day; Optional)", required: false, width: 6
						} else {
							paragraph "", width: 6
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
							paragraph "", width: 6
						}

						// Start Time field
						input "timeStart", "time", title: "<b>Start Time</b> (12:00AM if all day; Optional)", required: true, width: 6, submitOnChange:true

						// Stop Time field: Only show if start time entered
						if(timeStart){
							input "timeStop", "time", title: "<b>Stop Time</b> (11:59PM for remaining day; Optional)", required: false, width: 6, submitOnChange:true
						} else {
							paragraph "", width: 6
						}

						// Days
						input "timeDays", "enum", title: "<b>On these days</b> (Optional):", required: false, multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"]

						// If no start time, don't show any start or stop options
						if(timeStart){
							// Start On/Off/Toggle
							input "timeOn", "enum", title: "<b>Turn device(s) on/off at start time?</b> (Optional)", multiple: false, required: false, width: 6, options: ["Turn On", "Turn Off", "Toggle"]

							// Stop On/Off/Toggle: Only show if Stop Time entered
							if(timeStop){
								input "timeOff", "enum", title: "<b><b>Turn device(s) on/off at stop time?</b> (Optional)", multiple: false, required: false, width: 6, options: ["Turn On", "Turn Off", "Toggle"]
							} else {
								paragraph "", width: 6
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

def dimSpeed(){
    if(settings.multiplier != null){
        return settings.multiplier
    }else{
        return 1.2
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

def getDefaultLevel(device){
	// *******************************************************************************
	// ** TO DO: Merge level,temp, hue and sat into one function (and return array) **
	// *******************************************************************************

	// If no default level, return null
	if(!timeLevelOn) return
	
	// If disabled, return null
	if(timeDisable || state.timeDisableAll) return
	
	// If no device match, return null
	match = false
	timeDevice.each{
		if(it.id == device.id)  match = true
	}
	if(match == false) return

	// If mode set and node doesn't match, return null
	if(timeStartIfMode){
		if(location.mode != timeStartIfMode) return
	}

	// If before start time, return null
	if(timeStart){
		if(now() < timeToday(timeStart, location.timeZone).time) return
	}

	// If after time stop, return null
	if(timeStop){
		if(now() > timeToday(timeStop, location.timeZone).time) return
	}

	// If not correct day, return null
	if(timeDays){
		def df = new java.text.SimpleDateFormat("EEEE")
		df.setTimeZone(location.timeZone)
		def day = df.format(new Date())
		if(!timeDays.contains(day)) return
	}

	// Get current level
	currentLevel = device.currentLevel

	// If no stop time or no start level, return start level
	if(!timeStop || !timeLevelOff) {
		// If start level is too dim, and set not to dim, return current level
		if(timeLevelIfLower == "Lower"){
			if(parent.stateOn(device) == true && currentLevel < timeLevelOn) return currentLevel
		}
		// If start level is too bright, and set not to brighten, return current level
		if(timeLevelIfLower == "Higher"){
			if(parent.stateOn(device) == true && currentLevel > timeLevelOn) return currentLevel
		}
		// Otherwise, return start level
		return timeLevelOn
	}

	// If default level is the current level, return current level
	if(timeLevelOn == currentLevel && !timeLevelOff) currentLevel

	// If there's a stop time and stop level, and after start time
	if(timeStop && timeLevelOff){
		if(timeStart){
			// Calculate proportion of time already passed from start time to endtime
			hours1 = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
			minutes1 = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
			seconds1 = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
			hours2 = new Date().format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
			minutes2 = new Date().format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
			seconds2 = new Date().format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
			// Calculate new level
			newLevel = (timeLevelOff - timeLevelOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + timeLevelOn as int

			// If new level is too dim, and set not to dim, return current level
			if(timeLevelIfLower == "Lower"){
				if(parent.stateOn(device) == true && currentLevel < newLevel) return currentLevel
			}
			// If new level is too bright, and set not to brighten, return current level
			if(timeLevelIfLower == "Higher"){
				if(parent.stateOn(device) == true && currentLevel > newLevel) return currentLevel
			}
			
			// If device is a fan, round it to a third
			if(parent.isFan(device)) newLevel = roundFanLevel(newLevel)
			
			// Return the new level
			return newLevel
		}
	}

	// Should be all the options, but let's return current level just in case, and log an error
	log.debug "Time: No default level match found for $device."
	return currentLevel
}

def getDefaultTemp(device){
	// If no default temp, return null
	if(!timeTempOn) return
	
	// If disabled, return null
	if(timeDisable || state.timeDisableAll) return

	// If no device match, return null
	match = false
	timeDevice.each{
		if(it.id == device.id) match = true
	}
	if(match == false) return

	// If mode set and node doesn't match, return null
	if(timeStartIfMode){
		if(location.mode != timeStartIfMode) return
	}
	
	// If before start time, return null
	if(timeStart){
		if(now() < timeToday(timeStart, location.timeZone).time) return
	}

	// If after time stop, return null
	if(timeStop){
		if(now() > timeToday(timeStop, location.timeZone).time) return
	}
	
	// If not correct day, return null
	if(timeDays){
		def df = new java.text.SimpleDateFormat("EEEE")
		df.setTimeZone(location.timeZone)
		def day = df.format(new Date())
		if(!timeDays.contains(day)) return
	}
	
	// Get current temp
	currentTemp = device.currentColorTemperature
	
	//If no stop time or no start level, return start level
	if(!timeStop || !timeTempOff) {
		// If start temp is too low, and set not to lower, return current temp
		if(timeLevelIfLower == "Lower"){
			if(parent.stateOn(device) == true && currentTemp < timeTempOn) return currentTemp
		}
		// If start temp is too high, and set not to raise, return current temp
		if(timeLevelIfLower == "Higher"){
			if(parent.stateOn(device) == true && currentTemp > timeTempOn) return currentTemp
		}
		// Otherwise, return start level
		return timeTempOn
	}
	
	// If default temp is the current temp, return current temp
	if(timeLevelOn == currentTemp && !timeLevelOff) currentTemp

	//If default temp is the current level (plus or minus four, because temp isn't exact)
	if(timeTempOn > currentTemp - 4 &&  timeTempOn < currentTemp + 4 && !timeTempOff) return currentTemp

	// If there's a stop time and stop temp, and after start time
	if(timeStop && timeTempOff){
		if(timeStart){
			// Calculate proportion of time already passed from start time to endtime
			hours1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
			minutes1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
			seconds1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
			hours2 = new Date().format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
			minutes2=new Date().format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
			seconds2=new Date().format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
			// Calculate new temp
			newTemp = (timeTempOff - timeTempOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + timeTempOn as int

			// If new level is too low, and set not to lower, return current temp
			if(timeTempIfLower == "Lower"){
				if(parent.stateOn(device) == true && currentTemp < newTemp) return currentTemp
			}
			// If new level is too high, and set not to raise, return current temp
			if(timeTempIfLower == "Higher"){
				if(parent.stateOn(device) == true && currentTemp > newTemp) return currentTemp
			}
			
			// Return the new level
			return newTemp
		}
	}

	// Should be all the options, but let's return current temp just in case, and log an error
	log.debug "Time: No default temp match found for $device."
	return currentTemp
}

def getDefaultHue(device){
	// If no default hue, return null
	if(!timeHueOn) return
	
	// If disabled, return null
	if(timeDisable || state.timeDisableAll) return
	
	// If no device match, return null
	match = false
	timeDevice.each{
		if(it.id == device.id)  match = true
	}
	if(match == false) return
	
	// If mode set and node doesn't match, return null
	if(timeStartIfMode){
		if(location.mode != timeStartIfMode) return
	}
	
	// If before start time, return null
	if(timeStart){
		if(now() < timeToday(timeStart, location.timeZone).time) return
	}

	// If after time stop, return null
	if(timeStop){
		if(now() > timeToday(timeStop, location.timeZone).time) return
	}
	
	// If not correct day, return null
	if(timeDays){
		def df = new java.text.SimpleDateFormat("EEEE")
		df.setTimeZone(location.timeZone)
		def day = df.format(new Date())
		if(!timeDays.contains(day)) return
	}
	
	// Get current level
	currentHue = device.currentHue
	
	// If no stop time or no stop level, return start hue
	if(!timeStop || !timeHueOff) {
		// Otherwise, return start hue
		return timeHueOn
	}

	// If default hue is the current hue, return current hue
	if(timeHueOn == currentHue && !timeHueOff) currentHue
	
	// If there's a stop time and stop level, and after start time
	if(timeStop && timeHueOff){
		if(timeStart){
			// Calculate proportion of time already passed from start time to endtime
			hours1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
			minutes1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
			seconds1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
			hours2 = new Date().format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
			minutes2=new Date().format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
			seconds2=new Date().format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
			// Calculate new hue and return
			newTemp = (timeHueOff - timeHueOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + timeHueOn as int
			return newHue
		}
	}

	// Should be all the options, but let's return current hue just in case, and log an error
	log.debug "Time: No default hue match found for $device."
	return currentHue
}


def getDefaultSat(device){
	// If no default level, return null
	if(!timeSatOn) return
	
	// If disabled, return null
	if(timeDisable || state.timeDisableAll) return
	
	// If no device match, return null
	match = false
	timeDevice.each{
		if(it.id == device.id)  match = true
	}
	if(match == false) return

	// If mode set and node doesn't match, return null
	if(timeStartIfMode){
		if(location.mode != timeStartIfMode) return
	}
	
	// If before start time, return null
	if(timeStart){
		if(now() < timeToday(timeStart, location.timeZone).time) return
	}
	
	// If after time stop, return null
	if(timeStop){
		if(now() > timeToday(timeStop, location.timeZone).time) return
	}
	
	// If not correct day, return null
	if(timeDays){
		def df = new java.text.SimpleDateFormat("EEEE")
		df.setTimeZone(location.timeZone)
		def day = df.format(new Date())
		if(!timeDays.contains(day)) return
	}
	
	// Get current sat
	currentSat = device.currentSaturation
	
	// If no stop time or no start sat, return start sat
	if(!timeStop || !timeSatOff) {
		return timeSatOn
	}
	
	// If default sat is the current sat, return current sat
	if(timeSatOn == currentSat && !timeLevelOff) currentSat
	
	// If there's a stop time and stop sat, and after start time
	if(timeStop && timeSatOff){
		if(timeStart){
			// Calculate proportion of time already passed from start time to endtime
			hours1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
			minutes1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
			seconds1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
			hours2 = new Date().format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
			minutes2=new Date().format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
			seconds2=new Date().format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
			// Calculate new and return new sat
			newTemp = (timeSatOff - timeSatOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + timeSatOn as int
			return newTemp
		}
	}

	// Should be all the options, but let's return current sat just in case, and log an error
	log.debug "Time: No default saturation match found for $device."
	return currentSat
}

// Set on/off day schedule event
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
	timeDevice.each{
		log.debug "time.reshedule - device id = $device.id (matching on $it.id)"
		if(it.id == device.id) firstStageSchedule()
    }
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
	unschedule(secondStageSchedule)
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

// run scheduled level/temp
def runMultiSchedule(){
	if(timeDisable || state.timeDisableAll) return
	if(!timeStop) return
	// If nothing is on, exit
	if(!parent.multiStateOn(timeDevice)) return
	
	if(timeStartIfMode){
		if(location.mode != timeStartIfMode) return
	}
	
					log.debug "temp0.1"
	// If nothing to do, exit
	if((!timeLevelOn && !timeTempOn && !timeHueOn && !timeSatOn) || timeOn == "Turn Off") return
	
					log.debug "temp0.2"
	// If after time stop
    if(timeStop){
        if(now() > timeToday(timeStop, location.timeZone).time) return
    }
	
	if(timeDays){
		// If not correct day, exit
		def df = new java.text.SimpleDateFormat("EEEE")
		df.setTimeZone(location.timeZone)
		def day = df.format(new Date())
		if(!timeDays.contains(day)) return
	}
	
	timeDevice.each{
		if(parent.stateOn(it) == true){
			if(timeLevelOn && parent.isDimmable(it)){
				defaultLevel = getDefaultLevel(it)
					if(defaultLevel != it.currentLevel) {
					//mode
						/*
					//If don't dim and is dimmer
					if(timeLevelIfLower == "Lower"){
						if(it.currentLevel > timeLevelOn) return 
					}
					//If don't brighten and is brighter
					if(timeLevelIfLower == "Higher"){
						if(it.currentLevel < timeLevelOn) return
					}
*/
					if(timeLevelIfLower == "Lower"){
						if(it.currentLevel < defaultLevel) return 
					}
					if(timeLevelIfLower == "Higher"){
						if(it.currentLevel > defaultLevel) return
					}
					parent.setToLevel(it,defaultLevel,app.getId())
				}
			}
			if(timeTempOn && parent.isTemp(it)){
				defaultTemp = getDefaultTemp(it)
				
				if(defaultTemp - it.currentColorTemperature > 4 || defaultTemp - it.currentColorTemperature < -4) {
					//mode
					/*
					//If don't dim and is dimmer
					if(timeTempIfLower == "Lower"){
						if(it.currentColorTemperature < timeTempOn) return 
					}
					//If don't brighten and is brighter
					if(timeTempIfLower == "Higher"){
						if(it.currentColorTemperature > timeTempOn) return
					}
*/
					if(timeTempIfLower == "Lower"){
						if(it.currentColorTemperature < defaultTemp) return 
					}
					if(timeTempIfLower == "Higher"){
						if(it.currentColorTemperature > defaultTemp) return
					}
					parent.singleTemp(it,defaultTemp,app.getId())
				}
			}
			
			if(timeHueOn && parent.isColor(it)){
				defaultHue = getDefaultHue(it)
				parent.singleTemp(it,defaultTemp,app.getId())
			}
			if(timeHueOn && parent.isColor(it)){
				defaultSat = getDefaultSat(it)
			}
			if(timeHueOn || timeSatOn) parent.singleColor(it,defaultHue,defaultSat,app.getId())
		}
	}
}

def runDayOnSchedule(){
	if(timeDisable || state.timeDisableAll) return
	if(timeModeChangeOn) setLocationMode(timeModeChangeOn)
	if(timeOn != "Turn On" && timeOn != "Turn Off" && timeOn != "Toggle") return
	// if mode return
	if(timeOn == "Turn On"){
		parent.multiOn(timeDevice,app.getId())
	} else if(timeOn == "Turn Off"){
		parent.multiOff(timeDevice,app.getId())
	} else if(timeOn == "Toggle"){
		parent.toggle(timeDevice,app.getId())
	}
	//firstStageSchedule()
}

def runDayOffSchedule(){
	if(timeDisable || state.timeDisableAll) return
	if(timeModeChangeOff) setLocationMode(timeModeChangeOff)
	if(timeOff != "Turn On" && timeOff != "Turn Off" && timeOff != "Toggle") return
	// if mode return
	if(timeOff == "Turn On"){
	   parent.multiOn(timeDevice,app.getId())
	} else if(timeOff == "Turn Off"){
	   parent.multiOff(timeDevice,app.getId())
	} else if(timeOff == "Toggle"){
	   parent.toggle(timeDevice,app.getId())
	}
	//firstStageSchedule()
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
