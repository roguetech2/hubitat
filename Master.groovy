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
*  Version: 0.1.24
*
***********************************************************************************************************************/

/***********************************************************************************************************************
*
*  Speech device is set as "speechSynthesis". The device capability must be set in the driver, or change this to
*  "voice".
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
	infoIcon = "<img src=\"http://files.softicons.com/download/system-icons/windows-8-metro-invert-icons-by-dakirby309/ico/Folders%20&%20OS/Info.ico\" width=20 height=20>"
    return dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
        if(!state.masterInstalled) {
            section("Click Done.") {
            }

        } else {
			if(showPresences){
				section(""){
					input "colorLights", "capability.switchLevel", title: "Select all color lights:", multiple: true, required: false, submitOnChange:true
					paragraph "<div style=\"background-color:AliceBlue\">$infoIcon These color lights will be used for flashing alerts.</div>"
					input "people", "capability.presenceSensor", title: "Select all people:", multiple: true, required: false, submitOnChange:true
					paragraph "<div style=\"background-color:AliceBlue\">$infoIcon These people will be used for \"anyone\" and \"everyone\" conditions in any presence-based condition.</div>"
					input "notificationDevice", "capability.speechSynthesis", title: "Select notification device(s):", multiple: false, required: false, submitOnChange:true
					paragraph "<div style=\"background-color:AliceBlue\">$infoIcon This will be used for voice alerts.</div>"
					input "sensors", "capability.battery", title: "Select all sensors:", multiple: true, required: false, submitOnChange:true
					input "phone", "phone", title: "Number to text alert? (Optional)", required: false, submitOnChange:true
					paragraph "<div style=\"background-color:AliceBlue\">$infoIcon These will be periodically tested to make sure they are reporting properly. Enter phone number for text notice.</div>"

				}
/* ********************************************* */
/* TO-DO: Finish BDay code. Not complete.        */
/* ********************************************* */
				if(notificationDevice && people){
					section(""){
						def i = 0
						people.each{
							i++
							paragraph "<div style=\"background-color:BurlyWood\"><b> Sing Happy Birthday for $it:</b></div>"
							if(i == 1) paragraph "This sets Google Home to sing Happy Birthday on the individual's birthday between the times entered, a few minutes after they (or a few minutes after the start time, if they're already home). It can be set to wait for everyone to be home before singing."
							if(!settings["happyBDay$i"]){
								input "happyBDay${i}", "bool", title: "Do not sing Happy Birthday for $it? Click to sing.", submitOnChange:true
							} else {
								input "happyBDay${i}", "bool", title: "Sing Happy Birthday for $it? Click to not sing.", submitOnChange:true
							}
							if(settings["happyBDay$i"]){
								input "dateBDay${i}", "date", title: "Enter birthday date for $it.", submitOnChange:true
								if(!settings["bDayEveryone$i"]){
									input "bDayEveryone${i}", "bool", title: "Sing regardless of who else is home. Click to only sing with everyone.", submitOnChange:true
								} else {
									input "bDayEveryone${i}", "bool", title: "Only sing when everyone is home. Click to sing regardless who's home.", submitOnChange:true
								}
								input "timeStart", "time", title: "Only sing between start time", required: true, width: 6, submitOnChange:true
								input "timeStop", "time", title: "and stop time", required: true, width: 6, submitOnChange:true
							}
						}
	
					}
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
			if(showHumidity){
				section("Humidity sensors:") {
					app(name: "childApps", appName: "Master - Humidity", namespace: "master", title: "New Humidity Sensor", multiple: true)
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
				if(!showHumidity){
					input "showHumidity", "bool", title: "Humidity app hidden. Show?", submitOnChange:true
				} else {
					input "showHumidity", "bool", title: "Hide humidity app?", submitOnChange:true
				}
			}
        }
    }
}

def installed() {
	logTrace("$app.label: installed")
	state.masterInstalled = true
	initialize()
}

def updated() {
	logTrace("$app.label: updated")
	initialize()
}

def initialize() {
	test()
						i=1
log.debug settings["happyBDay$i"]
	log.debug "here" + happyBDay$i
	
						i=2
						log.debug settings["happyBDay$i"].toString()
						i=3
						log.debug settings["happyBDay$i"].toString()
	logTrace("$app.label: initialized")
}

// Functions for turning on lights/fans

// Turn on a group of switches
def multiOn(device,childId="Master"){
	logTrace("$app.label (140): function multiOn starting [device: $device; childId: $childId]")
	device.each{
		logTrace("$app.label (142): function multiOn starting device $device loop")
		// Using temp vars since each app will overwrite with null
		// Need to clear between devices
		def defaultLevel
		def defaultTemp
		def defaultHue
		def defaultSat
		childApps.each {Child->
			if(Child.label.substring(0,4) == "Time") {
				// defaults will return map with level, temp, hue and sat - populated with value of "Null" for null
				// Skip if all possibile values have been gotten
				if(!defaultLevel || !defaultTemp || !defaultHue || !defaultSat) {
					defaults = Child.getDefaultLevel(it)

					if(defaults.level != "Null") defaultLevel = defaults.level
					if(defaults.temp != "Null") defaultTemp = defaults.temp
					if(defaults.hue != "Null") defaultHue = defaults.hue
					if(defaults.sat != "Null") defaultSat = defaults.sat
					logTrace("$app.label (155): function multiOn found defaultLevel $defaults for $device.name")
				}
			}
		}
		if(!defaultLevel) defaultLevel = 100
		if(defaultHue && !defaultSat){
			defaultSat = 100
		} else if(!defaultHue && defaultSat){
			defaultHue = false
			defaultSat = false
		}
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
	logTrace("$app.label (181): function multiOn exiting")
}

// Turn on a single switch
def singleOn(device,child = "Master"){
	logTrace("$app.label (186): function singleOn starting [device: $device; child: $child]")
	device.on()
	log.info "Turned on $device."
	logTrace("$app.label (189): function singleOn exiting")
}

// Functions for turning off switches

// Turn off a group of switches
def multiOff(device,childId="Master"){
	logTrace("$app.label (196): function multiOff starting [device: $device; child: $childId]")
	device.each{
		if(stateOn(it)){
			singleOff(it,childId)
			reschedule(it)
		}
	}
	logTrace("$app.label (203): function multiOff exiting")
}

// Turn off a single switch
def singleOff(device,child = "Master"){
	logTrace("$app.label (208): function singleOff starting [device: $device; child: $child]")
	device.off()
	log.info "Turned off $device."
	logTrace("$app.label (211): function singleOff exiting")
}

// Toggle a group of switches
def toggle(device,childId="Master"){
	logTrace("$app.label: function toggle starting [device: $device; childId: $childId]")
	device.each{
		if(!stateOn(it)){
			// Using temp vars since each app will overwrite with null
			childApps.each {Child->
				if(Child.label.substring(0,4) == "Time") {
					defaults = Child.getDefaultLevel(it)
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
	logTrace("$app.label: function toggle exiting")
}

// Dim a group of dimmers
def dim(device,childId="Master",manualOverride=false){
	logTrace("$app.label: function dim starting [device: $device; childId: $childId]")
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
					} else {
						setToLevel(it,roundFanLevel(it.currentLevel - 33),childId)
						if(manualOverride) reschedule(it,manualOverride)
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
					if(manualOverride) reschedule(it,manualOverride)
				}
			}
		}
	}
	logTrace("$app.label: function dim exiting")
}

// Brighten a group of dimmers
def brighten(device,childId="Master",manualOverride=false){
	logTrace("$app.label: function brighten starting [device: $device; childId: $childId]")
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
						if(manualOverride) reschedule(it,manualOverride)
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
					if(manualOverride) reschedule(it,manualOverride)
				}
			}
		}
	}
	logTrace("$app.label: function brighten exiting")
}

// Set level (brighten or dim) a single dimmer
def setToLevel(device,level,child="Master"){
	logTrace("$app.label: function setToLevel starting [device: $device; level: $level; child: $child]")
	if(device.currentLevel != level || !stateOn(device)){
		logTrace("$app.label: function setToLevel set $device to $level")
		device.setLevel(level)
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
		logTrace("$app.label: function setToLevel exiting")
	} else {
		logTrace("$app.label: function setToLevel exiting (level hasn't changed)")
	}
}

// Lock/unlock functions

// Lock a group of locks
def multiLock(device, childId = "Master"){
	logTrace("$app.label: function multiLock starting [device: $device, childId: $childId]")
	device.each{
		singleLock(it,childId)
	}
	logTrace("$app.label: function multiLock exiting")
}

// Lock a single lock
def singleLock(device, childId = "Master"){
	logTrace("$app.label: function singleLock starting [device: $device, childId: $childId]")
	child = getAppLabel(childId)
	device.lock()
	log.info "$child: $child locked $device."
	logTrace("$app.label: function singleLock exiting")
}

// Unlock a group of locks
def multiUnlock(device, childId = "Master"){
	logTrace("$app.label: function multiUnlock starting [device: $device, childId: $childId]")
	device.each{
		singleUnlock(it,childId)
	}
	logTrace("$app.label: function multiUnlock exiting")
}

// Unlock a single lock
def singleUnlock(device, childId = "Master"){
	logTrace("$app.label: function singleUnlock starting [device: $device, childId: $childId]")
	child = getAppLabel(childId)
	device.unlock()
	log.info "$child: $child unlocked $device."
	logTrace("$app.label: function singleUnlock exiting")
}

// Set temperature color of single device
def singleTemp(device, temp,childId="Master"){
	logTrace("$app.label: function singleTemp starting [device: $device; temp:$temp; childId: $childId]")
	child = getAppLabel(childId)
	if(!isTemp(device)) {
		logTrace("$app.label: function singleTemp returning (device is not Temp)")
		return
	}
	device.setColorTemperature(temp as int)
	log.info "Set $device temperature color to $temp."
	logTrace("$app.label: function singleTemp exiting")
}


// Set color (hue and saturation) of single device
def singleColor(device, hue, sat, childId="Master"){
	logTrace("$app.label: function singleColor starting [device: $device, hue: $hue, sat: $sat, childId: $childId]")
	child = getAppLabel(childId)
	if(!isColor(device)) return
	newValue = [hue: hue, saturation: sat]
	device.setColor(newValue)
	log.info "Set $device color to hue $hue and satuartion $sat."
	logTrace("$app.label: function singleColor exiting")
}

// Determine next dim or brighten level based on "multiplier
// action = "dim" or "brighten"
def nextLevel(level, action, childId="Master"){
	logTrace("$app.label: function setToLevel starting [level: $level; action: $action; childId: $childId]")
	if(childId != "Master"){
		childApps.each {Child->
			if(Child.getId() == childId) dimSpeed = Child.dimSpeed()
		}
		if(!dimSpeed){
			dimSpeed = 1.2
			logTrace("$app.label: function setToLevel - error: failed to find dimSpeed")
		}
	}
	if (action != "dim" && action != "brighten"){
		child = getAppLabel(childId)
		logTrace("$app.label: function setToLevel - error: invalid action ($action); exiting")
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
	logTrace("$app.label: function setToLevel returning $newLevel")
	return newLevel
}

// Changes mode to value of $mode
/* ************************************************** */
/* TO DO: Trigger reschedule so that any schedules    */
/* suspended due to mode will restart. Need to get    */
/* the device id from the requesting child app...     */
/* somehow.                                           */
/* ************************************************** */
def changeMode(mode, childId = "Master", device = "Null") {
	logTrace("$app.label: function changeMode starting [mode: $mode, childId: $childId]")
	if(location.mode == mode) {
		logTrace("$app.label: function changeMode returning (mode already $mode)")
		return
	}
	child = getAppLabel(childId)
	oldMode = location.mode
	setLocationMode(mode)
	
	log.info "$child: Changed Mode from $oldMode to $mode."
	logTrace("$app.label: function changeMode exiting")
}

// Send SMS text message to $phone with $message
def sendText(phone, message){
	logTrace("$app.label: function sendText starting [phone: $phone, message: $message]")
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
	log.info "Sent text to $phone for \"$message\""
	sendSms(phone,message)
	logTrace("$app.label: function sendText exiting")
	return true
}

// Test state of a group of switches
// Return true if any are on
def multiStateOn(device){
	logTrace("$app.label: function multiStateOn starting [device: $device]")
	multiState = false
	device.each{
		if(stateOn(it) == true) multiState = true
	}
	logTrace("$app.label: function multiStateOn returning $multiState")
	return multiState
}

// Test state of a single switch
def stateOn(device){
	logTrace("$app.label: function stateOn starting")
	if(device.currentValue("switch") == "on") {
		logTrace("$app.label: function stateOn returning true")
		return true
	}
	logTrace("$app.label: function stateOn exiting")
}

// Validation functions

def validateTemp(value, childId="Master"){
	logTrace("$app.label: function validateTemp starting [value: $value, childId: $childId]")
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
	logTrace("$app.label: function validateTemp returning $value")
	return value
}

def validateLevel(value, childId="Master"){
	logTrace("$app.label: function validateLevel starting [value: $value, childId: $childId]")
	if(value){
		value = value as int 
		if(value < 1 || value > 100){
			child = getAppLabel(childId)
			value = null
			log.debug "$child: Default Level is invalid."
		}
	}
	logTrace("$app.label: function validateTemp returning $value")
	return value
}

def validateMultiplier(value, childId="Master"){
	logTrace("$app.label: function validateMultiplier starting [value: $value, childId: $childId]")
	if(value != null){
		if(value < 1 || value > 100){
			child = getAppLabel(childId)
			value = null
			log.debug "$child: Multiplier is invalid."
		}
	}
	logTrace("$app.label: function validateMultiplier returning $value")
	return value
}

// Validation functions for device capabilities

def isDimmable(device){
	logTrace("$app.label (569): function isDimmable starting [device: $device]")
	def deviceCapability
	device.capabilities.each {
		deviceCapability += it.name
	}
	value = deviceCapability.contains("SwitchLevel")
	logTrace("$app.label (575): function isDimmable returning $value")
	return value
}

def isTemp(device){
	logTrace("$app.label (580): function isTemp starting [device: $device]")
	def deviceCapability
	device.capabilities.each {
		deviceCapability += it.name
	}
	value = deviceCapability.contains("ColorTemperature")
	logTrace("$app.label (586): function isTemp returning $value")
	return value
}

def isColor(device){
	logTrace("$app.label: function isColor starting [device: $device]")
	def deviceCapability
	device.capabilities.each {
		deviceCapability += it.name
	}
	value = deviceCapability.contains("ColorMode")
	logTrace("$app.label: function isColor returning $value")
	return value
}

def isFan(device){
	logTrace("$app.label: function isFan starting [device: $device]")
	def deviceCapability
	device.capabilities.each {
		deviceCapability += it.name
	}
	if(deviceCapability.contains("Actuator") == true && device.name.contains("Fan") == true){
		logTrace("$app.label: function isFan returning true")
		return true
	} else {
		logTrace("$app.label: function isFan returning false")
		return false
	}
}

// Flash lights green
// Used by brighten function to indicate lights at 100%
/* ************************************************** */
/* TO-DO: Should add option to change color and/or    */
/* disable                                            */
/* ************************************************** */

/* ************************************************** */
/* Disabling feature - doesn't work reliably.         */
/* ************************************************** */
def flashGreen(device){
	/*
	logTrace("$app.label: function flashGreen starting [device: $device]")
	currentHue = device.currentHue
	currentSat = device.currentSaturation
	if(!isColor(device)) {
		logTrace("$app.label: function flashGreen returning (not color device)")
		return
	}
	newValue = [hue: 33, saturation: 100]
	singleColor(device,33,100)
	pause(750)
	singleColor(device,currentHue, currentSat)
	logTrace("$app.label: function flashGreen exiting")
*/
}

// Round fan level to high, medium or low
// Returns rounded value
def roundFanLevel(level){
	logTrace("$app.label: function roundFan starting [level: $level]")
	value = Math.round(level / 33) * 33 + 1
	logTrace("$app.label: function roundFan returning $value")
	return value
}

// Returns app label from app id
def getAppLabel(childId){
	logTrace("$app.label: function getAppLabel starting [childId: $childId]")
    childApps.each { 
        if(it.getId() == childId) child = it.label
    }
    if(child) {
	logTrace("$app.label: function getAppLabel returning $child")
        return child
    } else {
	logTrace("$app.label: function getAppLabel returning $childId")
        return childId
    }
}

def reschedule(device,pico=false){
	logTrace("$app.label: function reschedule starting [device: $device]")
	childApps.each {Child->
		if(Child.label.substring(0,4) == "Time") {
			Child.incrementalSchedule(device,pico)
		}
	}
	logTrace("$app.label: function reschedule exiting")
}


// Returns date one day ahead of $date
// Expects and returns format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
def getTomorrow(date){
	logTrace("$app.label: function getTomorrow starting [date: $date]")
	day = date.substring(8,10).toInteger() + 1
	day = String.format("%02d",day)
	value = date.substring(0,8) + day.toString() + date.substring(10,28)
	logTrace("$app.label: function getTomorrow returning $value")
	return value
}

// Returns date/time of sunrise in format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
// negative is true of false
def getSunrise(offset, negative=false){
	logTrace("$app.label: function getSunrise starting")
	if(offset){
		if(negative){
			def offsetRiseAndSet = getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: offset * -1)
			value = offsetRiseAndSet.sunrise
		} else {
			def offsetRiseAndSet = getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: offset)
			value = offsetRiseAndSet.sunrise
		}
		value = value.format("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ")
	} else {
		value = getSunriseAndSunset().sunrise.format("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ")
	}
	logTrace("$app.label: function getSunrise returning $value")
	return value
}

// Returns date/time of sunset in format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
def getSunset(offset = false, negative=false){
	logTrace("$app.label: function getSunset starting")
	if(offset){
		if(negative){
			def offsetRiseAndSet = getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: offset)
			value = offsetRiseAndSet.sunset
		} else {
			def offsetRiseAndSet = getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: offset * -1)
			value = offsetRiseAndSet.sunset
		}
		value = value.format("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ")
	} else {
		value = getSunriseAndSunset().sunset.format("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ")
	}
	logTrace("$app.label: function getSunset returning $value")
	return value
}

// Returns true if today is in $days map
def todayInDayList(days){
	logTrace("$app.label: function todayInDayList [days: $days]")
	if(!days) return false
	def df = new java.text.SimpleDateFormat("EEEE")
	df.setTimeZone(location.timeZone)
	def day = df.format(new Date())
	if(days.contains(day)) {
		logTrace("$app.label: function todayInDayList returning true")
		return true
	}
	logTrace("$app.label: function todayInDayList exiting")
}

// Returns true if now is between two dates
def timeBetween(timeStart, timeStop){
	logTrace("$app.label: function timeBetween [timeStart: $timeStart, timeStop: $timeStop]")
	if(!timeStart) {
		logTrace("$app.label: function timeBetween returning false (no start time)")
		return false
	}
	if(!timeStop) {
		logTrace("$app.label: function timeBetween returning false (no stop time)")
		return false
	}

	varNow = now()
	if(timeToday(timeStart, location.timeZone).time > timeToday(timeStop, location.timeZone).time) {
		if(varNow > timeToday(timeStart, location.timeZone).time || varNow < timeToday(timeStop, location.timeZone).time){
			logTrace("$app.label: function timeBetween returning true (time stop before time start)")
			return true
		}
	}
	if(varNow > timeToday(timeStart, location.timeZone).time && varNow < timeToday(timeStop, location.timeZone).time) {
		logTrace("$app.label: function timeBetween returning true")
		return true
	}
	logTrace("$app.label: function timeBetween exiting")
}

def speak(text){
	logTrace("$app.label: function speak [text: $text]")
	if(!notificationDevice) {
		logTrace("$app.label: function speak returning (no notification device)")
		return
	}
	notificationDevice.speak(text)
	logTrace("$app.label: function speak returning")
}

/* ************************************************** */
/* TO-DO: Clean it up, and schedule it every day,     */
/* with an alert.                                     */
/* ************************************************** */
def test(){
	// Get current time
	varNow = now()
	sensors.each{
		// lastCheckin only availble for Xiaomi drivers
		// Could use it.lastUpdate, but maybe it's just not used much
		// So ... maybe subscribe and create state variable??
		log.debug "here " + it.currentLastCheckin
		if(it.currentLastCheckin){
			// Covert lastCheckin to Unix epoch timestamp
	//		long epoch = it.currentLastCheckin.getTime()
			
			diff = (varNow - it.currentLastCheckin.toBigInteger()) / 1000 / 60 / 60
			if(diff > 24) {
				numDays = Math.round(diff / 24)
				if(!phone){
					if($numDays > 1){
						log.debug "$it hasn't done anything in $numDays days."
					} else {
						log.debug "$it hasn't done anything in $numDays day."
					}
				} else {
					if($numDays > 1){
						sendText(phone,"$it hasn't done anything in $numDay days.")
					} else {
						sendText(phone,"$it hasn't done anything in $numDay day.")
					}
				}
			}
		} else {
			log.debug "$it not tested."
		}
	}
}

def logTrace(message){
	//log.trace message
}
