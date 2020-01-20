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
*  for more details.
*
*  You should have received a copy of the GNU General Public License along with this program.
*  If not, see <http://www.gnu.org/licenses/>.
*
*  Name: Master
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master.groovy
*  Version: 0.1.31
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
					paragraph "<div style=\"background-color:AliceBlue\">$infoIcon These people will be used for \"anyone\" and \"everyone\" conditions in any presence-based condition, and allow birthday options.</div>"
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
								input "dateBDay${i}", "date", title: "Enter birthday date for $it."
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
                
                
                				if(showSchedules2){
					section("Scheduled settings:") {
						app(name: "childApps", appName: "Master - Time2", namespace: "master", title: "New Schedule", multiple: true)
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
			if(showWasher){
				section("Washer-dryer sensors:") {
					app(name: "childApps", appName: "Master - Washer-Dryer", namespace: "master", title: "New Washer-Dryer Sensor", multiple: true)
				}
			}
			if(showBirthday){
				section("Birthdays:") {
					app(name: "childApps", appName: "Master - Birthday", namespace: "master", title: "New Birthday", multiple: true)
				}
			}
			section(){
                if(!showSchedules){
                    input "showSchedules", "bool", title: "Schedule app hidden. Show?", submitOnChange:true
                } else {
                    input "showSchedules", "bool", title: "Hide schedule app?", submitOnChange:true
                }

                if(!showSchedules2){
                    input "showSchedules2", "bool", title: "Schedule app hidden. Show?", submitOnChange:true
                } else {
                    input "showSchedules2", "bool", title: "Hide schedule app?", submitOnChange:true
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
                if(!showWasher){
                    input "showWasher", "bool", title: "Washer-dryer app hidden. Show?", submitOnChange:true
                } else {
                    input "showWasher", "bool", title: "Hide washer-dryer app?", submitOnChange:true
				}
                if(!showBirthday){
                    input "showBirthday", "bool", title: "Birthday app hidden. Show?", submitOnChange:true
                } else {
                    input "showBirthday", "bool", title: "Hide Birthday app?", submitOnChange:true
				}
			}
        }
    }
}

def installed() {
	logTrace(189,"Installed")
	state.masterInstalled = true
	initialize()
}

def updated() {
	logTrace(195,"Updated")
	initialize()
}

def initialize() {
	//test()

	logTrace(202,"Initialized")
}

def appendAppTitle(appName,appTitle){
    //Compare length of name (eg "test") to appTitle length minus 6 (eg "Master - Time - " minus "Master - "; "Time - " is min length)
    if(appName.length() < appTitle.length() - 6){
        return appTitle.substring(9,appTitle.length()) + " - " + appName
    }

    //Compare first part of name (eg "Testing") to middle part of appTitle (eg "Master - Time" minus "Master - " plus " - ")
    if(appName.substring(0,appTitle.length() - 9) != appTitle.substring(9,appTitle.length())){
        return appTitle.substring(9,appTitle.length()) + " - " + appName
    }
    return appName
}

// Functions for turning on lights/fans

// Turn on a group of switches
def multiOn(device,childLabel="Master"){
	device.each{
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
                    if(defaults.level != "Null" || defaults.temp != "Null" || defaults.hue != "Null" || defaults.sat != "Null"){
                        logTrace(247,"Default levels of $defaults found for $it with $Child.label",childLabel)
                    }
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

        singleOn(it,childLabel)
        reschedule(it,childLabel)
		if(isDimmable(it,childLabel)){
            pause(250)
			if(isFan(it,childLabel)){
				setToLevel(it,roundFanLevel(defaultLevel,childLabel),childLabel)
			} else {
				setToLevel(it,defaultLevel,childLabel)
			}
		}
        // Check if it turned on, else retry
        pause(250)
        if(!stateOn(it,childLabel)){
            logTrace(267,"$device didn't turn on; trying again",childLabel)
            singleOff(it,childLabel)
            pause(250)
            singleOn(it,childLabel)
        }
		if(defaultTemp && isTemp(it,childLabel)) singleTemp(it,defaultTemp,childLabel)
		if(defaultHue && defaultSat && isColor(it,childLabel)) singleColor(it,defaultHue,defaultSat,childLabel)
	}
}

// Turn on a single switch
def singleOn(device,childLabel = "Master"){
	device.on()
	logTrace(280,"Turned on $device",childLabel)
}

// Functions for turning off switches

// Turn off a group of switches
def multiOff(device,childLabel="Master"){
	device.each{
        singleOff(it,childLabel)
        reschedule(it,childLabel)
		// Check if it turned on, else retry
        pause(250)
        if(stateOn(it,childLabel)){
               logTrace(293, "$device didn't turn off; trying again",childLabel)
               singleOn(it,childLabel)
               pause(250)
               singleOff(it,childLabel)
        }
	}
}

// Turn off a single switch
def singleOff(device,childLabel = "Master"){
	device.off()
	logTrace(304,"Turned off $device",childLabel)
}

// Toggle a group of switches
def toggle(device,childLabel="Master"){
	device.each{
		if(!stateOn(it,childLabel)){
			// Using temp vars since each app will overwrite with null
			childApps.each {Child->
				if(Child.label.substring(0,4) == "Time") {
					defaults = Child.getDefaultLevel(it)
					if(defaults.level != "Null") defaultLevel = defaults.level
					if(defaults.temp != "Null") defaultTemp = defaults.temp
					if(defaults.hue != "Null") defaultHue = defaults.hue
					if(defaults.sat != "Null") defaultSat = defaults.sat
					if(defaultHue && !defaultSat){
						defaultSat = 100
					} else if(!defaultHue && defaultSat){
						defaultHue = false
						defaultSat = false
					}
				}
			}
			if(!defaultLevel) defaultLevel = 100
			if(isFan(it)) defaultLevel = roundFanLevel(defaultLevel,childLabel)
			if(isDimmable(it)){
				setToLevel(it,defaultLevel,childLabel)
			} else {
				singleOn(it,defaultLevel,childLabel)
			}
			
			if(defaultTemp) singleTemp(it,defaultTemp,childLabel)
			if(defaultHue && defaultSat) singleColor(it,defaultHue,defaultSat,childLabel)
		} else {
			singleOff(it,childLabel)
		}
		reschedule(it,childLabel)
	}
}

// Dim a group of dimmers
def dim(device,childId="Master"){
	childLabel = getAppLabel(childId)
	deviceChange = false
	device.each{
		if(isDimmable(it,childLabel)){
			if(isFan(it,childLabel)){
				// If not fan is not on, turn it on by setting level 100
				if(!stateOn(it,childLabel)){
					setToLevel(it,75,childLabel)
					reschedule(it,childLabel)
				} else {
					// If fan is on low, turn it off
					if(roundFanLevel(it.currentLevel,childLabel) == 25){
						singleOff(it,childLabel)
					} else {
						setToLevel(it,roundFanLevel(it.currentLevel - 25,childLabel),childLabel)
						reschedule(it,childLabel)
					}
				}
			} else if(!isFan(it,childLabel)){
				// If not light is not on, then turn it on by setting level 1
				if (!stateOn(it,childLabel)){
					setToLevel(it,1,childLabel)
					reschedule(it,childLabel)
				} else if(it.currentLevel == 1){
					logTrace(371,"Can't dim $device; already at 1%",childLabel)
				} else {
					newLevel = nextLevel(it.currentLevel, "dim", childLabel)
					setToLevel(it,newLevel,childLabel)
				}
			}
		}
	}
}

// Brighten a group of dimmers
def brighten(device,childId="Master"){
	childLabel = getAppLabel(childId)
	device.each{
		if(isDimmable(it,childLabel)){
			if(isFan(it,childLabel)){
				// If fan is not on, turn it on by setting level to 25 (low)
				if(!stateOn(it,childLabel)){
					singleOn(it,childLabel)
					setToLevel(it,25,childLabel)
					reschedule(it,childLabel)
				} else {
					// If fan is on high, turn it off
					if(roundFanLevel(it.currentLevel,childLabel) == 75){
						singleOff(it,childLabel)
						reschedule(it,childLabel)
					} else {
						setToLevel(it,roundFanLevel(it.currentLevel + 25,childLabel),childLabel)
						reschedule(it,childLabel)
					}
				}
			} else if(!isFan(it,childLabel)){
				// If light is not on, turn it on by setting level to 1
				if (!stateOn(it,childLabel)){
					setToLevel(it,25,childLabel)
					reschedule(it,childLabel)
				} else if(it.currentLevel == 100){
					logTrace(410,"Can't brighten $device; already at 100%",childLabel)
				} else {
					newLevel = nextLevel(it.currentLevel, "brighten",childId)
					setToLevel(it,newLevel,childLabel)
				}
			}
		}
	}
}

// Set level (brighten or dim) a single dimmer
def setToLevel(device,level,childLabel="Master"){
	if(device.currentLevel != level || !stateOn(device,childLabel)){
		logTrace(424,"Set $device to $level",childLabel)
		device.setLevel(level)
		// output to log with fan "high", "medium" or "low"
		if(isFan(device,childLabel) == true){
			if(level == 99 | level == 100){
                logTrace(429,"Set $device to high",childLabel)
			} else if (level == 66 || level == 67){
                logTrace(431,"Set $device to medium",childLabel)
			} else if (level == 33 || level == 34){
                logTrace(433,"Set $device to low",childLabel)
			} else {
                logTrace(435,"Set $device to $level",childLabel)
			}
		} else {
            logTrace(438,"Set $device to $level",childLabel)
		}
	}
}

// Lock/unlock functions

// Lock a group of locks
def multiLock(device, childLabel = "Master"){
	device.each{
		singleLock(it,childLabel)
	}
}

// Lock a single lock
def singleLock(device, childLabel = "Master"){
	device.lock()
	logTrace(455,"Locked $device",childLabel)
}

// Unlock a group of locks
def multiUnlock(device, childLabel = "Master"){
	device.each{
		singleUnlock(it,childLabel)
	}
}

// Unlock a single lock
def singleUnlock(device, childLabel = "Master"){
	device.unlock()
	logTrace(468,"Unlocked $device",childLabel)
}

// Set temperature color of single device
def singleTemp(device, temp,childLabel="Master"){
	if(!isTemp(device,childLabel)) return
	device.setColorTemperature(temp as int)
	logTrace(475,"Set temperature color of $device to $temp",childLabel)
}


// Set color (hue and saturation) of single device
def singleColor(device, hue, sat, childLabel="Master"){
	if(!isColor(device,childLabel)) return
    if(hue != "Null" && sat != "Null"){
	    newValue = [hue: hue, saturation: sat]
        message = "hue $hue and saturation to $sat"
    } else if(hue != "Null"){
         newValue = [hue: hue]
        message = "hue $hue"
    } else if(sat != "Null"){
        newValue = [saturation: sat]
        message = "saturation to $sat"
    }
    if(newValue){
        device.setColor(newValue)
	    logTrace(494,"Set color of $device to $message",childLabel)
    }
}

// Determine next dim or brighten level based on "multiplier
// action = "dim" or "brighten"
def nextLevel(level, action, childId="Master"){
	childLabel = getAppLabel(childId)
	if(childId != "Master"){
		childApps.each {Child->
			if(Child.getId() == childId) dimSpeed = Child.dimSpeed()
		}
		if(!dimSpeed){
			dimSpeed = 1.2
			logTrace(504,"ERROR: Failed to find dimSpeed in function nextLevel",childLabel)
		}
	}
	if (action != "dim" && action != "brighten"){
		logTrace(508,"ERROR: Invalid action of $action in function nextLevel",childLabel)
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
	logTrace(527,"Function nextLevel returning $newLevel",childLabel)
	return newLevel
}

// Changes mode to value of $mode
/* ************************************************** */
/* TO DO: Trigger reschedule so that any schedules    */
/* suspended due to mode will restart. Need to get    */
/* the device id from the requesting child app...     */
/* somehow.                                           */
/* ************************************************** */
def changeMode(mode, childLabel = "Master", device = "Null") {
    if(location.mode == mode) return
    oldMode = location.mode
    setLocationMode(mode)
    logTrace(546,"Changed Mode from $oldMode to $mode",childLabel)
}

// Send SMS text message to $phone with $message
def sendText(phone, message,childLabel){
    //Normalize phone number
    phone = phone.replaceAll(" ","");
    phone = phone.replaceAll("\\(","");
    phone = phone.replaceAll("\\)","");
    phone = phone.replaceAll("-","");
    phone = phone.replaceAll("\\.","");
    phone = phone.replaceAll("\\+","");
    if(!phone.isNumber()) {
        return false
        logTrace(560,"Phone number $phone is not valid (message \"$message\" not sent)",childLabel)
    }
    if(phone.length() == 10) {
        phone = "+1" + phone
    } else if(phone.length() == 9 && phone.substring(0,1) == "1") {
        phone = "+" + phone
    }
    logTrace(567,"Sent \"$message\" to $phone",childLabel)
    sendSms(phone,message)
    return true
}

// Test state of a group of switches
// Return true if any are on
def multiStateOn(device,childLabel="Master"){
    multiState = false
    device.each{
        if(stateOn(it,childLabel) == true) multiState = true
    }
    //logTrace(579,"Function multiStateOn returning $multiState for $device",childLabel)
    return multiState
}

// Test state of a single switch
def stateOn(device,childLabel="Master"){
    if(device.currentValue("switch") == "on") return true
}

// Validation functions

def validateTemp(value, childLabel="Master"){
    if(value){
        value = value as int   
            if(value < 2200) {
                value = 2200
            } else if(value > 6500){
                value = 6500
            } else {
                logTrace(598,"Default temperature of $value is not valid",childLabel)
                value = null
            }
    }
    return value
}

def validateLevel(value, childLabel="Master"){
    if(value){
        value = value as int 
            if(value < 1 || value > 100){
                logTrace(609,"ERROR: Default level of $value is not valid",childLabel)
                value = null
            }
    }
    return value
}

def validateMultiplier(value, childLabel="Master"){
    if(value != null){
        if(value < 1 || value > 100){
            logTrace(619,"ERROR: Multiplier $value is not valid",childLabel)
            value = null
        }
    }
    logTrace(623,"Multiplier set to $value",childLabel)
    return value
}

// Validation functions for device capabilities

def isDimmable(device, childLabel="Master"){
    def deviceCapability
    device.capabilities.each {
        deviceCapability += it.name
    }
    value = deviceCapability.contains("SwitchLevel")
    return value
}

def isTemp(device, childLabel="Master"){
    def deviceCapability
    device.capabilities.each {
        deviceCapability += it.name
    }
    value = deviceCapability.contains("ColorTemperature")
    return value
}

def isColor(device, childLabel="Master"){
    def deviceCapability
    device.capabilities.each {
        deviceCapability += it.name
    }
    value = deviceCapability.contains("ColorMode")
    return value
}

def isFan(device, childLabel="Master"){
    def deviceCapability
    device.capabilities.each {
        deviceCapability += it.name
    }
    if(deviceCapability.contains("Actuator") == true && device.name.contains("Fan") == true){
        return true
    } else {
        return false
    }
}

// Round fan level to high, medium or low
// Returns rounded value
def roundFanLevel(level, childLabel="Master"){
    if(level > 66){
        value = 75
    } else if (level < 66  && level > 33){
        value = 50
    } else if (level > 0 && level < 33){
        value =25
    }
    logTrace(705,"Setting level for fan to $value from $level",childLabel)
    return value
}

// Returns app label from app id
def getAppLabel(childId){
    childApps.each { 
        if(it.getId() == childId) child = it.label
    }
    //Return the label; if no label, return the id
    if(child) return child
    return childId
}

def reschedule(device,childLabel="Master"){
    logTrace(720,"Rescheduling $device",childLabel)
    childApps.each {Child->
/* ************************************************** */
/* TO-DO: We need to test this! If we use a Pico, it  */
/* should reschedule all of them.                     */
/* If a schedule changes something *that* schedule    */
/* should NOT be rescheduled (Time app handles it).   */
/* ************************************************** */
        if(Child.label.substring(0,4) == "Time" && Child.label != childLabel) {
            incrementalSchedule = Child.incrementalSchedule()
        }
    }
}


// Returns date one day ahead of $date
// Expects and returns format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
def getTomorrow(date,childLabel="Master"){
    day = date.substring(8,10).toInteger() + 1
    day = String.format("%02d",day)
    value = date.substring(0,8) + day.toString() + date.substring(10,28)
    return value
}

// Returns date/time of sunrise in format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
// negative is true of false
def getSunrise(offset, childLabel="Master"){
    if(offset){
        value = getSunriseAndSunset(sunriseOffset: offset, sunsetOffset: 0).sunrise
        value = value.format("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ")
    } else {
        value = getSunriseAndSunset().sunrise.format("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ")
    }
    return value
}

// Returns date/time of sunset in format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
def getSunset(offset = false,childLabel="Master"){
    if(offset){
        value = getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: offset).sunset
        value = value.format("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ")
    } else {
        value = getSunriseAndSunset().sunset.format("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ")
    }
    return value
}

// Returns true if today is in $days map
def todayInDayList(days,childLabel="Master"){
    if(!days) return false
    def df = new java.text.SimpleDateFormat("EEEE")
    df.setTimeZone(location.timeZone)
    def day = df.format(new Date())
    if(days.contains(day)) {
        //logTrace(786,"Today is in $days",childLabel)
        return true
    } else {
        //logTrace(789,"Today is not in $days",childLabel)
    }
}

// Returns true if now is between two dates
def timeBetween(timeStart, timeStop,childLabel="Master"){
    if(!timeStart) {
        logTrace(796,"ERROR: Function timeBetween returning false (no start time)",childLabel)
        return false
    }
    if(!timeStop) {
        logTrace(800,"Function timeBetween returning false (no stop time)",childLabel)
        return false
    }
  
    //This might work
    //if(timeStart.before(now()) && timeStop.after(now()))
    
    varNow = now()
    if(timeToday(timeStart, location.timeZone).time > timeToday(timeStop, location.timeZone).time) {
        if(varNow > timeToday(timeStart, location.timeZone).time || varNow < timeToday(timeStop, location.timeZone).time){
            //logTrace(810,"Time is between " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mma MMM dd, yyyy", location.timeZone) + " and " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mma MMM dd, yyyy", location.timeZone),childLabel)
            return true
        }
    }
    if(varNow > timeToday(timeStart, location.timeZone).time && varNow < timeToday(timeStop, location.timeZone).time) {
        //logTrace(815,"Time is between " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mma MMM dd, yyyy", location.timeZone) + " and " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mma MMM dd, yyyy", location.timeZone),childLabel)
        return true
    }
}

def speak(text,childLabel="Master"){
    if(!notificationDevice) {
        logTrace(822,"ERROR: No speech device for \"$text\"",childLabel)
        return
    }
    notificationDevice.speak(text)
    logTrace(826,"Sending speech \"$text\"",childLabel)
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

// Returns true if secondDevice contains anything in firstDevice
def compareDeviceLists(firstDevice,secondDevice){
    list = []
    firstDevice.each{
        list.add(it.id)
    }
    secondDevice.each{
        if(list.contains(it.id)) return true
    }
    return false
}

def logTrace(lineNumber,message,childLabel = null){
    if(childLabel && childLabel != "Master"){
	    log.trace "$app.label (line $lineNumber) [$childLabel] -- $message"
    } else {
	    log.trace "$app.label (line $lineNumber) -- $message"
    }
}
