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
*  Version: 0.0.03
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
		
		section() {
            label title: "• <b><font color=\"#000099\">Assign a name:</font></b>", required: true
        }
        section("• <b><font color=\"#000099\">With what to do things?</font></b>") {
            input "timeDevice", "capability.switch", title: "<b>Devices timeDeviceto control:</b>", multiple: true, required: true
			input "timeDisable", "enum", title: "Disable this schedule:", required: false, options: ["Yes"]
			paragraph "<center><b><font color=\"#000099\">• Starting •</font></b></center>", width: 6
			paragraph "<td><center><b><font color=\"#000099\">• Stopping •</font></b></center>", width: 6
            input "timeStart", "time", title: "<b>Start Time</b> (12:00AM if all day; Optional)", required: true, width: 6
            input "timeStop", "time", title: "<b>Stop Time</b> (11:59PM for remaining day; Optional)", required: false, width: 6
            input "timeDays", "enum", title: "<b>On these days</b> (Optional):", required: false, multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"]

            input "timeOn", "enum", title: "<b>Turn On/Off at start?</b> (Optional)", multiple: false, required: false, width: 6, options: ["Turn On", "Turn Off", "Toggle"]
            input "timeOff", "enum", title: "<b>Turn On/Off at stop?</b> (Optional)", multiple: false, required: false, width: 6, options: ["Turn On", "Turn Off", "Toggle"]
            input "timeLevelOn", "number", title: "<b>Starting default brightness:</b> (Optional: 1-100; Default 100)", required: false, width: 6
            input "timeLevelOff", "number", title: "<b>Ending default brightness:</b> (Optional: 1-100)", required: false, width: 6
            input "timeTempOn", "number", title: "<b>Starting temperature color:</b> (Optional: 2200-4500; Default 100)", required: false, width: 6
            input "timeTempOff", "number", title: "<b>Ending temperature color:</b> (Optional: 2200-4500)", required: false, width: 6
            input "timeHueOn", "number", title: "<b>Starting color hue:</b> (Optional: 0-100; 1/100 = red, 33 = green, 66 = blue)", required: false, width: 6
            input "timeHueOff", "number", title: "<b>Ending color hue:</b> (Optional: 0-100)", required: false, width: 6
            input "timeSatOn", "number", title: "<b>Starting color saturation:</b> (Optional: 0-100; Default 100)", required: false, width: 6
            input "timeSatOff", "number", title: "<b>Ending color saturation:</b> (Optional: 0-100)", required: false, width: 6
            input "timeModeChangeOn", "mode", title: "<b>Change Mode at start?</b> (Optional)", required: false, width: 6
            input "timeModeChangeOff", "mode", title: "<b>Change Mode at stop?</b> (Optional)", required: false, width: 6
		}
		section(){
			
			input "timeStartIfMode", "mode", title: "<b>Only if Mode is already:</b> (Optional)", required: false, width: 12
            input "timeLevelIfLower", "enum", title: "<b>Don't change Level if already:</b> (Optional)", multiple: false, required: false, width: 6, options: ["Lower", "Higher"]
            input "timeTempIfLower", "enum", title: "<b>Don't change temperature color if already:</b> (Optional)", multiple: false, required: false, width: 6, options: ["Lower", "Higher"]
			
            paragraph "Options can be combined. To have a default brightness of 50% after 9pm, set start time and start level (but do not have turn on). To have device turn on at 7am and gradually brighten for a half hour from 1% to 100%, set start time of 7am, stop time of 7:30am, and at start time turn on with level of 1, and a stop time of 7:30a, with a level of 100."
			
			input "timeDisableAll", "enum", title: "Disable ALL schedules:", required: false, options: ["Yes"]

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
	//If no defult level
	if(!timeLevelOn) return
	if(state.debug) log.debug "getDefaultLevel - $device: 1"
	//If disabled
	if(timeDisable || state.timeDisableAll) return
	if(state.debug) log.debug "getDefaultLevel - $device: 2"
	//If no device match
	match = false
	timeDevice.each{
		if(state.debug) log.debug "getDefaultLevel - device = $device.id (testing $it.id): 2"
		if(it.id == device.id)  match = true
	}
	if(match == false) return
	if(state.debug) log.debug "getDefaultLevel - $device: 3"
	//If not mode
	if(timeStartIfMode){
		if(state.debug) log.debug "getDefaultLevel: 3.1"
		if(location.mode != timeStartIfMode) return
	}
	if(state.debug) log.debug "getDefaultLevel - $device: 4"
	//If before start time
	if(timeStart){
		if(state.debug) log.debug "getDefaultLevel: 4.1"
		if(now() < timeToday(timeStart, location.timeZone).time) return
	}
	if(state.debug) log.debug "getDefaultLevel - $device: 5"
	// If after time stop
	if(timeStop){
		if(state.debug) log.debug "getDefaultLevel - $device: 5.1"
		if(now() > timeToday(timeStop, location.timeZone).time) return
	}
	//If not correct day
	if(timeDays){
		def df = new java.text.SimpleDateFormat("EEEE")
		df.setTimeZone(location.timeZone)
		def day = df.format(new Date())
		if(!timeDays.contains(day)) return
	}
	if(state.debug) log.debug "getDefaultLevel - $device: 6"
	//If no stop time, return start level
	if(!timeStop) return timeLevelOn
	if(state.debug) log.debug "getDefaultLevel - $device: 7"
	//If default level is the current level
	if(timeLevelOn == device.currentLevel && !timeLevelOff) device.currentLevel
	if(state.debug) log.debug "getDefaultLevel - $device: 8"
	//If don't dim and is dimmer
	/*
	if(timeLevelIfLower == "Lower"){
	if(state.debug) log.debug "getDefaultLevel - $device: 8.1"
		if(parent.stateOn(device) == true && device.currentLevel > timeLevelOn) device.currentLevel 
	}
	//If don't brighten and is brighter
	if(timeLevelIfLower == "Higher"){
	if(state.debug) log.debug "getDefaultLevel - $device: 8.2"
		if(parent.stateOn(device) == true && device.currentLevel < timeLevelOn) return device.currentLevel
	}
*/
	if(state.debug) log.debug "getDefaultLevel - $device: 9"
	// If after time stop
	if(timeStop && timeLevelOff){
		if(timeStart){
			//calculate proportion of dimming
			hours1 = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
			minutes1 = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
			seconds1 = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
			hours2 = new Date().format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
			minutes2 = new Date().format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
			seconds2 = new Date().format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
			newLevel = (timeLevelOff - timeLevelOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + timeLevelOn as int
			if(state.debug) log.debug "getDefaultLevel - $device: 10"
				if(timeLevelIfLower == "Lower"){
					if(state.debug) log.debug "getDefaultLevel - $device: 10.1"
					if(parent.stateOn(device) == true && device.currentLevel < newLevel) return device.currentLevel
				}
			if(timeLevelIfLower == "Higher"){
				if(state.debug) log.debug "getDefaultLevel - $device: 10.2"
				if(parent.stateOn(device) == true && device.currentLevel > newLevel) return device.currentLevel
			}
			if(parent.isFan(device)) newLevel = roundFanLevel(newLevel)
			if(state.debug) log.debug "getDefaultLevel - $device: 10.3"
			return newLevel
		}
	}

	//If don't dim and is dimmer
	if(timeLevelIfLower == "Lower"){
		if(state.debug) log.debug "getDefaultLevel - $device: 11.1"
		if(parent.stateOn(device) == true && device.currentLevel > timeLevelOn) return device.currentLevel
	}
	
	if(state.debug) log.debug "getDefaultLevel - $device: 11.2"
	newLevel = timeLevelOn
	if(parent.isFan(device)) newLevel = roundFanLevel(newLevel)
	return newLevel
}

def getDefaultTemp(device){
	//If no defult level
	if(!timeTempOn) return
	if(timeDisable || state.timeDisableAll) return
	//If no device match
	match = false
	timeDevice.each{
		if(it.id == device.id) match = true
	}
	if(match == false) return
	//If not mode
	if(timeStartIfMode){
		if(location.mode != timeStartIfMode) return
	}
	//If before start time
	if(timeStart){
		if(now() < timeToday(timeStart, location.timeZone).time) return
	}
	// If after time stop
	if(timeStop){
		if(now() > timeToday(timeStop, location.timeZone).time) return
	}
	if(timeDays){
		//If not correct day
		def df = new java.text.SimpleDateFormat("EEEE")
		df.setTimeZone(location.timeZone)
		def day = df.format(new Date())
		if(!timeDays.contains(day)) return
	}
	//If no stop time, return start temp
	if(!timeStop) return timeTempOn
	//If default temp is the current level
	if(timeTempOn > device.currentColorTemperature - 4 &&  timeTempOn < device.currentColorTemperature + 4 && !timeTempOff) return device.currentColorTemperature
	/*
	//If don't dim and is dimmer
	if(timeTempIfLower == "Lower"){
		if(parent.stateOn(device) == true && device.currentColorTemperature < timeTempOn) return device.currentColorTemperature
	}
	log.debug "8"
	//If don't brighten and is brighter
	if(timeTempIfLower == "Higher"){
		if(parent.stateOn(device) == true && device.currentColorTemperature > timeTempOn) return device.currentColorTemperature
	}
*/
	if(!timeStop) return timeTempOn
	// If after time stop
	if(timeStop && timeTempOff){
		if(timeStart){
			//calculate proportion of dimming
			hours1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
			minutes1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
			seconds1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
			hours2 = new Date().format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
			minutes2=new Date().format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
			seconds2=new Date().format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
			newTemp = (timeTempOff - timeTempOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + timeTempOn as int
				if(timeTempIfLower == "Lower"){
					if(parent.stateOn(device) == true && device.currentColorTemperature < newTemp) return device.currentColorTemperature
				}
			if(timeTempIfLower == "Higher"){
				if(parent.stateOn(device) == true && device.currentColorTemperature > newTemp) return device.currentColorTemperature
			}
			return newTemp
		}
	}

	//If don't dim and is dimmer
	if(timeTempIfLower == "Lower"){
		if(parent.stateOn(device) == true && device.currentColorTemperature > timeTempOn) return device.currentColorTemperature
	}
	//If don't brighten and is brighter
	if(timeTempIfLower == "Higher"){
		if(parent.stateOn(device) == true && device.currentColorTemperature < timeTempOn) return device.currentColorTemperature
	}

	log.debug "19"
	return timeTempOn
}

def getDefaultHue(device){
	//If no defult level
	if(!timeHueOn) return
	if(timeDisable || state.timeDisableAll) return
	//If not mode
	if(timeStartIfMode){
		if(location.mode != timeStartIfMode) return
	}
	//If no device match
	match = false
	timeDevice.each{
		if(it.id == device.id) match = true
	}
	if(match == false) return
	//If before start time
	if(timeStart){
		if(now() < timeToday(timeStart, location.timeZone).time) return
	}
	// If after time stop
	if(timeStop){
		if(now() > timeToday(timeStop, location.timeZone).time) return
	}
	if(timeDays){
		//If not correct day
		def df = new java.text.SimpleDateFormat("EEEE")
		df.setTimeZone(location.timeZone)
		def day = df.format(new Date())
		if(!timeDays.contains(day)) return
	}
	if(!timeStop) return timeHueOn
	// If after time stop
	if(timeStop && timeHueOff){
		if(timeStart){
			//calculate proportion of dimming
			hours1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
			minutes1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
			seconds1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
			hours2 = new Date().format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
			minutes2=new Date().format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
			seconds2=new Date().format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
			newTemp = (timeHueOff - timeHueOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + timeHueOn as int
			return newHue
		}
	}

	return timeHueOn
}


def getDefaultSat(device){
	//If no defult level
	if(!timeSatOn) return
	if(timeDisable || state.timeDisableAll) return
	//If not mode
	if(timeStartIfMode){
		if(location.mode != timeStartIfMode) return
	}
	//If no device match
	match = false
	timeDevice.each{
		if(it.id == device.id) match = true
	}
	if(match == false) return
	//If before start time
	if(timeStart){
		if(now() < timeToday(timeStart, location.timeZone).time) return
	}
	// If after time stop
	if(timeStop){
		if(now() > timeToday(timeStop, location.timeZone).time) return
	}
	if(timeDays){
		//If not correct day
		def df = new java.text.SimpleDateFormat("EEEE")
		df.setTimeZone(location.timeZone)
		def day = df.format(new Date())
		if(!timeDays.contains(day)) return
	}
	if(!timeStop) return timeSatOn
	// If after time stop
	if(timeStop && timeSatOff){
		if(timeStart){
			//calculate proportion of dimming
			hours1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
			minutes1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
			seconds1=Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
			hours2 = new Date().format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
			minutes2=new Date().format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
			seconds2=new Date().format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
			newTemp = (timeSatOff - timeSatOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + timeSatOn as int
			return newTemp
		}
	}

	return timeSatOn
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
