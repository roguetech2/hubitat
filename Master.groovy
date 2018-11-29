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
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master.groovy
*  Version: 0.1.11
*
***********************************************************************************************************************/

definition(
    name: "Master",
    namespace: "master",
    singleInstance: true,
    author: "roguetech",
    description: "Parent app for automation suite",
    category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/mi_face_s.png",
    iconX2Url: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/mi_face.png"
)

preferences {
	page(name: "mainPage")
	
}

def mainPage() {
    return dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
        if(!state.masterInstalled) {
            section("Click Done.") {
            }

        } else {
		if(showPresences){
			section(""){
				input "colorLights", "capability.switchLevel", title: "Select all color lights:", multiple: true, required: false, submitOnChange:true
				input "people", "capability.presenceSensor", title: "Select all people:", multiple: true, required: false, submitOnChange:true
			}
			if(showSchedules){
				section("Scheduled settings:") {
					app(name: "childApps", appName: "Master - Time", namespace: "master", title: "New Schedule", multiple: true)
				}
			}
		}
		if(showPresences){
			section("Presence settings:") {
				app(name: "childApps", appName: "Master - Presence", namespace: "master", title: "New Presence", multiple: true)
			}
		}
		if(showPicos){
			section("Picos:") {
				app(name: "childApps", appName: "Master - Pico", namespace: "master", title: "New Pico", multiple: true)
			}
		}
		if(showMagicCubes){
			section("MagicCubes:") {
				app(name: "childApps", appName: "Master - MagicCube", namespace: "master", title: "New MagicCube", multiple: true)
			}
		}
		if(showContacts){
			section("Contact sensors:") {
				app(name: "childApps", appName: "Master - Contact", namespace: "master", title: "New Contact Sensor", multiple: true)
			}
		}
		section(){
			if(!showSchedules){
				input "showSchedules", "bool", title: "Schedule app hidden. Show?", submitOnChange:true
			} else {
				input "showSchedules", "bool", title: "Hide schedule app?", submitOnChange:true
			}
			if(!showPresences){
				input "showPresences", "bool", title: "Presence app hidden. Show?", submitOnChange:true
			} else {
				input "showPresences", "bool", title: "Hide presence app?", submitOnChange:true
			}
			if(!showPicos){
				input "showPicos", "bool", title: "Pico app hidden. Show?", submitOnChange:true
			} else {
				input "showPicos", "bool", title: "Hide Pico app?", submitOnChange:true
			}
			if(!showMagicCubes){
				input "showMagicCubes", "bool", title: "MagicCube app hidden. Show?", submitOnChange:true
			} else {
				input "showMagicCubes", "bool", title: "Hide MagicCube app?", submitOnChange:true
			}
			if(!showContacts){
				input "showContacts", "bool", title: "Contact app hidden. Show?", submitOnChange:true
			} else {
				input "showContacts", "bool", title: "Hide contact app?", submitOnChange:true
			}
			paragraph "Note: Hiding apps does not disable any existing saved child apps; it only hides them from this list."
		}
        }
    }
}

def installed() {
	logTrace("$app.label, app.getId(): installed")
	state.masterInstalled = true
	initialize()
}

def updated() {
	logTrace("$app.label, app.getId(): updated")
	initialize()
}

def initialize() {
	logTrace("$app.label, app.getId(): initialized")
	state.debug = true
}

// Functions for turning on lights/fans

// Turn on a group of switches
def multiOn(device,childId="Master"){
	logTrace("$app.label, app.getId(): function multiOn starting [device: $device; childId: $childId]")
	device.each{
		// Using temp vars since each app will overwrite with null
		childApps.each {Child->
			if(Child.label.substring(0,4) == "Time") {
				// defaults will return map with level, temp, hue and sat - populated with value of "Null" for null
				defaults = Child.getDefaultLevel(it)

				if(isDimmable(it) && defaults.level != "Null") defaultLevel = defaults.level
				if(isTemp(it) && defaults.temp != "Null") defaultTemp = defaults.temp
				if(isColor(it)){
					if(defaults.hue != "Null") defaultHue = defaults.hue
					if(defaults.sat != "Null") defaultSat = defaults.sat
				}

				logTrace("$app.label, app.getId(): function multiOn found defaultLevel $defaults for $device.name")
				if(defaultHue && !defaultSat){
					defaultSat = 100
				} else if(!defaultHue && defaultSat){
					defaultHue = false
					defaultSat = false
				}
			}
		}
		if(!defaultLevel) defaultLevel = 100
		if(!defaultTemp) defaultTemp = 3400
		if(isDimmable(it)){
			if(isFan(it)){
				reschedule(it)
				setToLevel(it,roundFanLevel(defaultLevel),childId)
			} else {
				reschedule(it)
				setToLevel(it,defaultLevel,childId)
			}
		} else {
			reschedule(it)
			singleOn(it,childId)
		}
		if(defaultTemp && isTemp(it)) singleTemp(it,defaultTemp,childId)
		if(defaultHue && defaultSat && isColor(it)) singleColor(it,defaultHue,defaultSat,childId)
	}
	logTrace("$app.label, app.getId(): function multiOn exiting")
}

// Turn on a single switch
def singleOn(device,child = "Master"){
	logTrace("$app.label, app.getId(): function singleOn starting [device: $device; child: $child]")
	device.on()
	log.info "Turned on $device."
	logTrace("$app.label, app.getId(): function singleOn exiting")
}

// Functions for turning off switches

// Turn off a group of switches
def multiOff(device,childId="Master"){
	logTrace("$app.label, app.getId(): function multiOff starting [device: $device; child: $childId]")
	device.each{
		if(stateOn(it)){
			singleOff(it,childId)
			reschedule(it)
		}
	}
	logTrace("$app.label, app.getId(): function multiOff exiting")
}

// Turn off a single switch
def singleOff(device,child = "Master"){
	logTrace("$app.label, app.getId(): function singleOff starting [device: $device; child: $child]")
	device.off()
	log.info "Turned off $device."
	logTrace("$app.label, app.getId(): function singleOff exiting")
}

// Toggle a group of switches
def toggle(device,childId="Master"){
	logTrace("$app.label, app.getId(): function toggle starting [device: $device; childId: $childId]")
	device.each{
		if(!stateOn(it)){
			// Using temp vars since each app will overwrite with null
			childApps.each {Child->
				if(Child.label.substring(0,4) == "Time") {
					defaults = Child.getDefaultLevel(it)
					log.debug "Master - childId = $childId (matching on $Child.id) with device = $it.id"
					if(isDimmable(it) && defaults.level != "Null") defaultLevel = defaults.level
					if(isTemp(it) && defaults.temp != "Null") defaultTemp = defaults.temp
					if(isColor(it) && defaults.hue != "Null") defaultHue = defaults.hue
					if(isColor(it) && defaults.sat != "Null") defaultSat = defaults.sat
					if(defaultHue && !defaultSat){
						defaultSat = 100
					} else if(!defaultHue && defaultSat){
						defaultHue = false
						defaultSat = false
					}
				}
			}
			if(!defaultLevel) defaultLevel = 100
			if(!defaultTemp) defaultTemp = 3400

			if(isDimmable(it)){
				setToLevel(it,defaultLevel,childId)
			} else {
				singleOn(it,defaultLevel,childId)
			}
			if(defaultTemp && isTemp(it)) singleTemp(it,defaultTemp,childId)
			if(defaultHue && defaultSat && isColor(it)) singleColor(it,defaultHue,defaultSat,childId)
		} else {
			singleOff(it,childId)
		}
		reschedule(it)
	}
	logTrace("$app.label, app.getId(): function toggle exiting")
}

// Dim a group of dimmers
def dim(device,childId="Master"){
	logTrace("$app.label, app.getId(): function dim starting [device: $device; childId: $childId]")
	deviceChange = false
	device.each{
		if(isDimmable(it)){
			if(isFan(it)){
				// If not fan is not on, turn it on by setting level 100
				if(!stateOn(it)){
					setToLevel(it,100,childId)
					reschedule(it)
				} else {
					// If fan is on low, turn it off
					if(roundFanLevel(it.currentLevel) == 34){
						singleOff(it,childId)
						reschedule(it)
					} else {
						setToLevel(it,roundFanLevel(it.currentLevel - 33),childId)
					}
				}
			} else if(!isFan(it)){
				// If not light is not on, then turn it on by setting level 1
				if (!stateOn(it)){
					setToLevel(it,1,childId)
					reschedule(it)
				} else if(it.currentLevel == 1){
					flashGreen(it)
					child = getAppLabel(childId)
					log.info "$child: $device is already at 1%; can't dim."
				} else {
					newLevel = nextLevel(it.currentLevel, "dim", childId)
					setToLevel(it,newLevel,childId)
				}
			}
		}
	}
	logTrace("$app.label, app.getId(): function dim exiting")
}

// Brighten a group of dimmers
def brighten(device,childId="Master"){
	logTrace("$app.label, app.getId(): function brighten starting [device: $device; childId: $childId]")
	device.each{
		if(isDimmable(it)){
			if(isFan(it)){
				// If fan is not on, turn it on by setting level to 34
				if(!stateOn(it)){
					singleOn(it,childId)
					setToLevel(it,34,childId)
					reschedule(it)
				} else {
					// If fan is on high, turn it off
					if(roundFanLevel(it.currentLevel) == 100){
						singleOff(it,childId)
						reschedule(it)
					} else {
						setToLevel(it,roundFanLevel(it.currentLevel + 33))
					}
				}
			} else if(!isFan(it)){
				// If light is not on, turn it on by setting level to 1
				if (!stateOn(it)){
					setToLevel(it,1,childId)
					reschedule(it)
				} else if(it.currentLevel == 100){
					child = getAppLabel(childId)
					flashGreen(it)
					log.info "$child: $device is already at 100%; can't brighten."
				} else {
					newLevel = nextLevel(it.currentLevel, "brighten",childId)
					setToLevel(it,newLevel,childId)
				}
			}
		}
	}
	logTrace("$app.label, app.getId(): function brighten exiting")
}

// Set level (brighten or dim) a single dimmer
def setToLevel(device,level,child=""){
	logTrace("$app.label, app.getId(): function setToLevel starting [device: $device; level: $level; child: $child]")
	device.setLevel(level)
	if(child == "") child = "Master"
	// output to log with fan "high", "medium" or "low"
	if(isFan(device) == true){
		if(level == 99 | level == 100){
			log.info "$child: Set level of $device to high."
		} else if (level == 66 || level == 67){
			log.info "$child: Set level of $device to medium."
		} else if (level == 33 || level == 34){
			log.info "$child: Set level of $device to low."
		} else {
			log.info "$child: Set level of $device to $level."
		}
	} else {
		log.info "$child: Set level of $device to $level."
	}
	logTrace("$app.label, app.getId(): function setToLevel exiting")
}

// Lock/unlock functions

// Lock a group of locks
def multiLock(device, childId = "Master"){
	logTrace("$app.label, app.getId(): function multiLock starting [device: $device, childId: $childId]")
	device.each{
		singleLock(it,childId)
	}
	logTrace("$app.label, app.getId(): function multiLock exiting")
}

// Lock a single lock
def singleLock(device, childId = "Master"){
	logTrace("$app.label, app.getId(): function singleLock starting [device: $device, childId: $childId]")
	child = getAppLabel(childId)
	device.lock()
	log.info "$child: $child locked $device."
	logTrace("$app.label, app.getId(): function singleLock exiting")
}

// Unlock a group of locks
def multiUnlock(device, childId = "Master"){
	logTrace("$app.label, app.getId(): function multiUnlock starting [device: $device, childId: $childId]")
	device.each{
		singleUnlock(it,childId)
	}
	logTrace("$app.label, app.getId(): function multiUnlock exiting")
}

// Unlock a single lock
def singleUnlock(device, childId = "Master"){
	logTrace("$app.label, app.getId(): function singleUnlock starting [device: $device, childId: $childId]")
	child = getAppLabel(childId)
	device.unlock()
	log.info "$child: $child unlocked $device."
	logTrace("$app.label, app.getId(): function singleUnlock exiting")
}

// Set temperature color of single device
def singleTemp(device, temp,childId="Master"){
	logTrace("$app.label, app.getId(): function singleTemp starting [device: $device; childId: $childId]")
	child = getAppLabel(childId)
	if(!isTemp(device)) {
		logTrace("$app.label, app.getId(): function singleTemp returning (device is not Temp)")
		return
	}
	device.setColorTemperature(temp as int)
	log.info "Set $device temperature color to $temp."
	logTrace("$app.label, app.getId(): function singleTemp exiting")
}


// Set color (hue and saturation) of single device
def singleColor(device, hue, sat, childId="Master"){
	logTrace("$app.label, app.getId(): function singleColor starting [device: $device, hue: $hue, sat: $sat, childId: $childId]")
	child = getAppLabel(childId)
	if(!isTemp(device)) return
	newValue = [hue: hue, saturation: sat]
	device.setColor(newValue)
	log.info "Set $device color to hue $hue and satuartion $sat."
	logTrace("$app.label, app.getId(): function singleColor exiting")
}

// Determine next dim or brighten level based on "multiplier
// action = "dim" or "brighten"
def nextLevel(level, action, childId="Master"){
	logTrace("$app.label, app.getId(): function setToLevel starting [level: $level; action: $action; childId: $childId]")
	if(childId != "Master"){
		childApps.each {Child->
			if(Child.getId() == childId) dimSpeed = Child.dimSpeed()
		}
		if(!dimSpeed){
			dimSpeed = 1.2
			logTrace("$app.label, app.getId(): function setToLevel - error: failed to find dimSpeed")
		}
	}
	if (action != "dim" && action != "brighten"){
		child = getAppLabel(childId)
		logTrace("$app.label, app.getId(): function setToLevel - error: invalid action ($action); exiting")
		return false
	}
	def newLevel = level as int
	if(level < 1) level = 1
	if(level > 100) level = 100
	if((action == "dim" && level > 1) || (action ==  "brighten" && level < 100)){
		if (action == "dim"){
			newLevel = Math.round(level / dimSpeed)
			// make sure it changed by at least 1
			if (newLevel == level) newLevel = newLevel - 1
		} else if (action == "brighten"){
			newLevel = Math.round(level * dimSpeed)
			// make sure it changed by at least 1
			if (newLevel == level) newLevel = newLevel + 1
		}
	}
	if(newLevel > 100) newLevel = 100
	if(newLevel < 1) newLevel = 1
	logTrace("$app.label, app.getId(): function setToLevel returning $newLevel")
	return newLevel
}

// Changes mode to value of $mode
/* *************************************** */
/* TO DO: Trigger reschedule so that any   */
/* schedules suspended due to mode will    */
/* restart. Need to get the device id from */
/* the requesting child app... somehow.    */
/* *************************************** */
def changeMode(mode, childId = "Master", device = "Null") {
	logTrace("$app.label, app.getId(): function changeMode starting [mode: $mode, childId: $childId]")
	if(location.mode == mode) {
		logTrace("$app.label, app.getId(): function changeMode returning (mode already $mode)")
		return
	}
	child = getAppLabel(childId)
	oldMode = location.mode
	setLocationMode(mode)
	
	log.info "$child: Changed Mode from $oldMode to $mode."
	logTrace("$app.label, app.getId(): function changeMode exiting")
}

// Send SMS text message to $phone with $message
def sendText(phone, message){
	logTrace("$app.label, app.getId(): function sendText starting [phone: $phone, message: $message]")
	//Normalize phone number
	phone = phone.replaceAll(" ","");
	phone = phone.replaceAll("\\(","");
	phone = phone.replaceAll("\\)","");
	phone = phone.replaceAll("-","");
	phone = phone.replaceAll("\\.","");
	phone = phone.replaceAll("\\+","");
	if(!phone.isNumber()) return false
	if(phone.length() == 10) {
		phone = "+1" + phone
	} else if(phone.length() == 9 && phone.substring(0,1) == "1") {
		phone = "+" + phone
	}
	log.info "Sent text to $phone for \"$message\"
	sendSms(phone,message)
	logTrace("$app.label, app.getId(): function sendText exiting")
	return true
}

// Test state of a group of switches
// Return true if any are on
def multiStateOn(device){
	logTrace("$app.label, app.getId(): function multiStateOn starting [device: $device]")
	multiState = false
	device.each{
		if(stateOn(it) == true) multiState = true
	}
	logTrace("$app.label, app.getId(): function multiStateOn returning $multiState")
	return multiState
}

// Test state of a single switch
def stateOn(device){
	logTrace("$app.label, app.getId(): function stateOn starting")
	if(device.currentValue("switch") == "on") {
		logTrace("$app.label, app.getId(): function stateOn returning true")
		return true
	}
	logTrace("$app.label, app.getId(): function stateOn exiting")
}

// Validation functions

def validateTemp(value, childId="Master"){
	logTrace("$app.label, app.getId(): function validateTemp starting [value: $value, childId: $childId]")
	if(value){
		value = value as int   
		if(value < 2200) {
			value = 2200
		} else if(value > 6500){
			value = 6500
		} else {
			child = getAppLabel(childId)
			value = null
			log.debug "$child: Default Temp Color is invalid."
		}
	}
	logTrace("$app.label, app.getId(): function validateTemp returning $value")
	return value
}

def validateLevel(value, childId="Master"){
	logTrace("$app.label, app.getId(): function validateLevel starting [value: $value, childId: $childId]")
	if(value){
		value = value as int 
		if(value < 1 || value > 100){
			child = getAppLabel(childId)
			value = null
			log.debug "$child: Default Level is invalid."
		}
	}
	logTrace("$app.label, app.getId(): function validateTemp returning $value")
	return value
}

def validateMultiplier(value, childId="Master"){
	logTrace("$app.label, app.getId(): function validateMultiplier starting [value: $value, childId: $childId]")
	if(value != null){
		if(value < 1 || value > 100){
			child = getAppLabel(childId)
			value = null
			log.debug "$child: Multiplier is invalid."
		}
	}
	logTrace("$app.label, app.getId(): function validateMultiplier returning $value")
	return value
}

// Validation functions for device capabilities

def isDimmable(device){
	logTrace("$app.label, app.getId(): function isDimmable starting [device: $device]")
	def deviceCapability
	device.capabilities.each {
		deviceCapability += it.name
	}
	value = deviceCapability.contains("SwitchLevel")
	logTrace("$app.label, app.getId(): function isDimmable returning $value")
	return value
}

def isTemp(device){
	logTrace("$app.label, app.getId(): function isTemp starting [device: $device]")
	def deviceCapability
	device.capabilities.each {
		deviceCapability += it.name
	}
	value = deviceCapability.contains("ColorTemperature")
	logTrace("$app.label, app.getId(): function isDimmable returning $value")
	return value
}

def isColor(device){
	logTrace("$app.label, app.getId(): function isColor starting [device: $device]")
	def deviceCapability
	device.capabilities.each {
		deviceCapability += it.name
	}
	value = deviceCapability.contains("ColorMode")
	logTrace("$app.label, app.getId(): function isDimmable returning $value")
	return value
}

def isFan(device){
	logTrace("$app.label, app.getId(): function isColor starting [device: $device]")
	def deviceCapability
	device.capabilities.each {
		deviceCapability += it.name
	}
	if(deviceCapability.contains("Actuator") == true && device.name.contains("Fan") == true){
		logTrace("$app.label, app.getId(): function isFan returning true")
		return true
	} else {
		logTrace("$app.label, app.getId(): function isFan returning false")
		return false
	}
}

// Flash lights green
// Used by brighten function to indicate lights at 100%
/* ********************************************* */
/* TO-DO: Should add option to change color      */
/* and/or disable                                */
/* ********************************************* */
def flashGreen(device){
	logTrace("$app.label, app.getId(): function flashGreen starting [device: $device]")
	currentHue = device.currentHue
	currentSat = device.currentSaturation
	if(!isColor(device)) {
		logTrace("$app.label, app.getId(): function flashGreen returning (not color device)")
		return
	}
	newValue = [hue: 33, saturation: 100]
	singleColor(33,100)
	pause(750)
	newValue = [hue: currentHue, saturation: currentSat]
	singleColor(currentHue, currentSat)
	logTrace("$app.label, app.getId(): function flashGreen exiting")
}

// Round fan level to high, medium or low
// Returns rounded value
def roundFanLevel(level){
	logTrace("$app.label, app.getId(): function roundFan starting [level: $level]")
	if(isFan(device)) {
		value = Math.round(level / 33) * 33 + 1
		logTrace("$app.label, app.getId(): function roundFan returning $value")
		return value
	} else {
		logTrace("$app.label, app.getId(): function roundFan returning $level")
		return level
	}
}

// Returns app label from app id
def getAppLabel(childId){
	logTrace("$app.label, app.getId(): function getAppLabel starting [childId: $childId]")
    childApps.each { 
        if(it.getId() == childId) child = it.label
    }
    if(child) {
	logTrace("$app.label, app.getId(): function getAppLabel returning $child")
        return child
    } else {
	logTrace("$app.label, app.getId(): function getAppLabel returning $childId")
        return childId
    }
}

def reschedule(device){
	logTrace("$app.label, app.getId(): function reschedule starting [device: $device]")
	childApps.each {Child->
		if(Child.label.substring(0,4) == "Time") {
			Child.incrementalSchedule(device)
		}
	}
	logTrace("$app.label, app.getId(): function reschedule exiting")
}


// Returns date one day ahead of $date
// Expects and returns format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
def getTomorrow(date){
	logTrace("$app.label, app.getId(): function getTomorrow starting [date: $date]")
	day = date.substring(8,10).toInteger() + 1
	day = String.format("%02d",day)
	value = date.substring(0,8) + day.toString() + date.substring(10,28)
	logTrace("$app.label, app.getId(): function getTomorrow returning $value")
	return value
}

// Returns date/time of sunrise in format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
def getSunrise(){
	logTrace("$app.label, app.getId(): function getSunrise starting")
	value = getSunriseAndSunset().sunrise.format("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ")
	logTrace("$app.label, app.getId(): function getSunrise returning $value")
	return value
}

// Returns date/time of sunset in format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
def getSundown(){
	logTrace("$app.label, app.getId(): function getSundown starting")
	value = getSunriseAndSunset().sunset.format("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ")
	logTrace("$app.label, app.getId(): function getSunrise returning $value")
	return value
}

// Returns true if today is in $days map
def todayInDayList(days){
	logTrace("$app.label, app.getId(): function todayInDayList [days: $days]")
	if(!days) return false
	def df = new java.text.SimpleDateFormat("EEEE")
	df.setTimeZone(location.timeZone)
	def day = df.format(new Date())
	if(days.contains(day)) {
		logTrace("$app.label, app.getId(): function todayInDayList returning true")
		return true
	}
	logTrace("$app.label, app.getId(): function todayInDayList exiting")
}

// Returns true if now is between two dates
def timeBetween(timeStart, timeStop){
	logTrace("$app.label, app.getId(): function timeBetween [timeStart: $timeStart, timeStop: $timeStop]")
	if(!timeStart) {
		logTrace("$app.label, app.getId(): function timeBetween returning false (no start time)")
		return false
	}
	if(!timeStop) {
		logTrace("$app.label, app.getId(): function timeBetween returning false (no stop time)")
		return false
	}

	varNow = now()
	if(timeToday(timeStart, location.timeZone).time > timeToday(timeStop, location.timeZone).time) {
		if(varNow > timeToday(timeStart, location.timeZone).time || varNow < timeToday(timeStop, location.timeZone).time){
			logTrace("$app.label, app.getId(): function timeBetween returning true (time stop before time start)")
			return true
		}
	}
	if(varNow > timeToday(timeStart, location.timeZone).time && varNow < timeToday(timeStop, location.timeZone).time) {
		logTrace("$app.label, app.getId(): function timeBetween returning true")
		return true
	}
	logTrace("$app.label, app.getId(): function timeBetween exiting")

}

def logTrace(message){
	if(state.debug) log.trace message
}
