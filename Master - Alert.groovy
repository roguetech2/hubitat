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
*  Name: Master - Alert
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master - Alert.groovy
*  Version: 0.0.01
*
***********************************************************************************************************************/

definition(
    name: "Master - Alert",
    namespace: "master",
    author: "roguetech",
    description: "SMS alerts for activity",
    parent: "master:Master",
    category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/mi_face_s.png",
    iconX2Url: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/mi_face.png"
)

preferences {
    page(name: "setup", install: false, uninstall: true, nextPage: "setup2") {
		section() {
			label title: "• <b><font color=\"#000099\">Assign a name:</font></b>", required: true
		}
		section("• <b><font color=\"#000099\">What to alert about?</font></b>") {
			input "alertSwitch", "capability.switch", title: "<b>Switches:</b>", multiple: true, required: false
			input "alertContact", "capability.contactSensor", title: "<b>Contact sensors:</b>", multiple: true, required: false
			
			input "alertMotion", "capability.motionSensor", title: "<b>Motion sensors:</b>", multiple: true, required: false

			input "alertLock", "capability.lock", title: "<b>Locks:</b>", multiple: true, required: false
		}
		section("• <b><font color=\"#000099\">Only alert if:</font></b>") {
			input "alertStart", "time", title: "<b>From Time</b> (12:00AM if all day; Optional)", required: true, width:6
			input "alertStop", "time", title: "<b>Until Time</b> (11:59PM for remaining day; Optional)", required: true, width:6
			input "alertDays", "enum", title: "<b>On these days:</b>", required: false, multiple: true, width:6, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"]
			input "alertStartIfMode", "mode", title: "<b>Only if Mode is already:</b> (Optional)", required: false, width:6

		}
	}
	page(name: "setup2", install: true, uninstall: true) {
		section("• <b><font color=\"#000099\">Switches - Alert if</font></b>") {
			if(alertSwitch){
				alertSwitch.each{
					input it.label, "enum", title: it.label, multiple: false, required: false, width: 6, options: ["Turned On", "Turned Off", "Either"]

				}
			}
		}
		
		section("• <b><font color=\"#000099\">Contact - Alert if</font></b>") {
			if(alertContact){
				alertContact.each{
					input it.label, "enum", title: it.label, multiple: false, required: false, width: 6, options: ["Opened", "Closed", "Either"]

				}
			}
			// don't need motion, since there's only one state
			/*
			count = 0
			isOdd = false
			if(alertMotion){
				alertMotion.each{
					count++
					input it.label, "enum", title: it.label, multiple: false, required: false, width: 6, options: ["Opened", "Closed", "Either"]

				}
			}
*/
		}
		
		section("• <b><font color=\"#000099\">Locks - Alert if</font></b>") {
			if(isOdd) paragraph "", width: 6
			if(alertLock){
				alertLock.each{
					input it.label, "enum", title: it.label, multiple: false, required: false, width: 6, options: ["Locked", "Unlocked", "Either"]

				}
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
	//if(timeOn == "Turn On" || timeOn == "Turn Off" || timeOn == "Toggle") runDaily(timeToday(timeStart, location.timeZone), runDaySchedule())
	//	if(timeOn == "Turn Off") runDaily(timeToday(timeStart, location.timeZone), parent.multiOff(timeDevice,appId))
	//	if(timeOn == "Toggle") runDaily(timeToday(timeStart, location.timeZone), parent.toggle(timeDevice,appId))
	//	if(timeLevelOn == "Dim" && !timeOff) runDaily(timeToday(timeStart, location.timeZone), parent.dim(timeDevice,appId))

	
}

def getDefaultLevel(device){
	//If no defult level
	if(!timeLevelOn) return
	if(timeDisable || state.timeDisableAll) return
	//If not mode
	if(timeStartIfMode){
		if(location.mode != timeStartIfMode) return
	}
	//If no device match
	match = false
	timeDevice.each{
		if(it.id == device.id)  match = true
	}
	if(timeLevelOn == device.currentLevel && !timeLevelOff) device.currentLevel
	if(match == false) return
	//If before start time
	if(timeStart){
		if(now() < timeToday(timeStart, location.timeZone).time) return
	}
	// If after time stop
	if(timeStop){
		if(now() > timeToday(timeStop, location.timeZone).time) return
	}
	//If not correct day
	if(timeDays){
		def df = new java.text.SimpleDateFormat("EEEE")
		df.setTimeZone(location.timeZone)
		def day = df.format(new Date())
		if(!timeDays.contains(day)) return
	}
	//If don't dim and is dimmer
	if(timeLevelIfLower == "Lower"){
		if(parent.stateOn(device) == true && device.currentLevel > timeLevelOn) device.currentLevel 
	}
	//If don't brighten and is brighter
	if(timeLevelIfLower == "Higher"){
		if(parent.stateOn(device) == true && device.currentLevel < timeLevelOn) return device.currentLevel
	}
	if(!timeStop) return timeLevelOn
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
				if(timeLevelIfLower == "Lower"){
					if(parent.stateOn(device) == true && device.currentLevel < newLevel) return device.currentLevel
				}
			if(timeLevelIfLower == "Higher"){
				if(parent.stateOn(device) == true && device.currentLevel > newLevel) return device.currentLevel
			}
			if(parent.isFan(device)) newLevel = roundFanLevel(newLevel)
			return newLevel
		}
	}

	//If don't dim and is dimmer
	if(timeLevelIfLower == "Lower"){
		if(parent.stateOn(device) == true && device.currentLevel > timeLevelOn) return device.currentLevel
	}
	newLevel = timeLevelOn
	if(parent.isFan(device)) newLevel = roundFanLevel(newLevel)
	return newLevel
}

def getDefaultTemp(device){
	//If no defult level
	if(!timeTempOn) return
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
	//If don't dim and is dimmer
	if(timeTempIfLower == "Lower"){
		if(parent.stateOn(device) == true && device.currentColorTemperature > timeTempOn) return device.currentColorTemperature
	}
	//If don't brighten and is brighter
	if(timeTempIfLower == "Higher"){
		if(parent.stateOn(device) == true && device.currentColorTemperature < timeTempOn) return device.currentColorTemperature
	}
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
		if(parent.stateOn(device) == true && device.currentColorTemperature > timeTempOn) return
	}
	//If don't brighten and is brighter
	if(timeTempIfLower == "Higher"){
		if(parent.stateOn(device) == true && device.currentColorTemperature < timeTempOn) return
	}

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
		if(it.id == device.id) firstStageSchedule()
    }
}

// Allows pausing scheduling for a minute
def firstStageSchedule(){
	unschedule(firstStageSchedule)
	unschedule(secondStageSchedule)
	if(timeDisable || state.timeDisableAll) return
	runIn(30,secondStageSchedule)
}

//settings up schedules for level/temp
def secondStageSchedule(){
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
	
	// If nothing to do, exit
	if((!timeLevelOn && !timeTempOn && !timeHueOn && !timeSatOn) || timeOn == "Turn Off") return
	
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
					//If don't dim and is dimmer
					if(timeLevelIfLower == "Lower"){
						if(it.currentLevel > timeLevelOn) return 
					}
					//If don't brighten and is brighter
					if(timeLevelIfLower == "Higher"){
						if(it.currentLevel < timeLevelOn) return
					}
					if(timeLevelIfLower == "Lower"){
						if(it.currentLevel > defaultLevel) return 
					}
					if(timeLevelIfLower == "Higher"){
						if(it.currentLevel < defaultLevel) return
					}
					parent.setToLevel(it,defaultLevel,app.getId())
				}
			}
			if(timeTempOn && parent.isTemp(it)){
				defaultTemp = getDefaultTemp(it)
				if(defaultTemp - it.currentColorTemperature > 4 || defaultTemp - it.currentColorTemperature < -4) {
					//mode
					//If don't dim and is dimmer
					if(timeTempIfLower == "Lower"){
						if(it.currentColorTemperature > timeTempOn) return 
					}
					//If don't brighten and is brighter
					if(timeTempIfLower == "Higher"){
						if(it.currentColorTemperature < timeTempOn) return
					}
					if(timeTempIfLower == "Lower"){
						if(it.currentTemp > defaultTemp) return 
					}
					if(timeTempIfLower == "Higher"){
						if(it.currentTemp < defaultTemp) return
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
		if(it == "Monday") dayString += "1"
		if(it == "Tuesday") {
			if(dayString) dayString += ","
			dayString += "2"
		}
		if(it == "Wednesday") {
			if(dayString) dayString += ","
			dayString += "3"
		}
		if(it == "Thursday") {
			if(dayString) dayString += ","
			dayString += "4"
		}
		if(it == "Friday") {
			if(dayString) dayString += ","
			dayString += "5"
		}
		if(it == "Saturday") {
			if(dayString) dayString += ","
			dayString += "6"
		}
		if(it == "Sunday") {
			if(dayString) dayString += ","
			dayString += "7"
		}
	}
	return dayString
}
