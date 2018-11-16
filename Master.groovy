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
*
***********************************************************************************************************************/

definition(
    name: "Master",
    namespace: "master",
    singleInstance: true,
    author: "roguetech",
    description: "Pico and Caseta switches",
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
            section("Scheduled settings:") {
                app(name: "childApps", appName: "Master - Time", namespace: "master", title: "New Schedule", multiple: true)
            }
            section("Picos:") {
                app(name: "childApps", appName: "Master - Pico", namespace: "master", title: "New Mapping", multiple: true)
            }
            section("MagicCubes:") {
                app(name: "childApps", appName: "Master - MagicCube", namespace: "master", title: "New Mapping", multiple: true)
            }
            section("Contact sensors:") {
                app(name: "childApps", appName: "Master - Contact", namespace: "master", title: "New Contact Sensor", multiple: true)
            }
            section("Alerts settings:") {
                app(name: "childApps", appName: "Master - Alert", namespace: "master", title: "New Alert", multiple: true)
            }
        }
    }
}

def installed() {
    state.masterInstalled = true
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {

}

def setToLevel(device,level,child=""){
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
}

// action = "dim" or "brighten"
def nextLevel(level, action, childId="Master"){
    if(childId != "Master"){
        childApps.each {Child->
            if(Child.getId() == childId) dimSpeed = Child.dimSpeed()
        }
        if(!dimSpeed){
            dimSpeed = 1.2
            log.debug "Master: Unrecognized child - $child."
        }
    }
    if (action != "dim" && action != "brighten"){
        child = getAppLabel(childId)
        log.debug "$child: Invalid action with nextLevel"
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
    return newLevel
}

def stateOn(device){
    if(device.currentValue("switch") == "on") return true
}

def multiStateOn(device){
	multiState = false
	device.each{
		if(stateOn(it) == true) multiState = true
	}
	return multiState
}

def singleOn(device,child = "Master"){
    device.on()
    log.info "$child: Turned on $device."
}

def singleOff(device,child = "Master"){
    device.off()
    log.info "$child: Turned off $device."
}

def multiOn(device,childId="Master"){
    device.each{
		log.debug "mutliOn - $device: 1"
		// Using temp vars since each app will overwrite with null
        childApps.each {Child->
            if(Child.label.substring(0,4) == "Time") {
				log.debug "Master - childId = $childId (matching on $Child.id)"
                if(isDimmable(it)) tempDefaultLevel = Child.getDefaultLevel(it)
				if(tempDefaultLevel) defaultLevel = tempDefaultLevel
                if(isTemp(it)) tempDefaultTemp = Child.getDefaultTemp(it)
				if(tempDefaultTemp) defaultTemp = tempDefaultTemp
				log.debug "mutliOn - defaultLevel = $defaultLevel"
				if(isColor(it)){
					tempDefaultHue = Child.getDefaultHue(it)
					if(tempDefaultHue) defaultHue = tempDefaultHue
				}
				if(isColor(it)){
					tempDefaultSat = Child.getDefaultSat(it)
					if(tempDefaultSat) defaultSat = tempDefaultSat
				}
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
			log.debug "mutliOn - $device: 2"
            if(isFan(it)){
			log.debug "mutliOn - $device: 3"
                reschedule(it)
                setToLevel(it,roundFanLevel(defaultLevel),childId)
            } else {
			log.debug "mutliOn - $device: 4"
                reschedule(it)
            	setToLevel(it,defaultLevel,childId)
            }
        } else {
			log.debug "mutliOn - $device: 5"
            reschedule(it)
            singleOn(it,childId)
        }
        if(defaultTemp && isTemp(it)) singleTemp(it,defaultTemp,childId)
		if(defaultHue && defaultSat && isColor(it)) singleColor(it,defaultHue,defaultSat,childId)
    }
}

def multiOff(device,childId="Master"){
    device.each{
		if(stateOn(it)){
			singleOff(it,childId)
			reschedule(it)
		}
    }
}

def toggle(device,childId="Master"){
    device.each{
		// Using temp vars since each app will overwrite with null
        childApps.each {Child->
            if(Child.label.substring(0,4) == "Time") {
				log.debug "Master - childId = $childId (matching on $Child.id)"
                if(isDimmable(it)) tempDefaultLevel = Child.getDefaultLevel(it)
				if(tempDefaultLevel) defaultLevel = tempDefaultLevel
                if(isTemp(it)) tempDefaultTemp = Child.getDefaultTemp(it)
				if(tempDefaultTemp) defaultTemp = tempDefaultTemp
				log.debug "mutliOn - defaultLevel = $defaultLevel"
				if(isColor(it)){
					tempDefaultHue = Child.getDefaultHue(it)
					if(tempDefaultHue) defaultHue = tempDefaultHue
				}
				if(isColor(it)){
					tempDefaultSat = Child.getDefaultSat(it)
					if(tempDefaultSat) defaultSat = tempDefaultSat
				}
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
        if (!stateOn(it)){
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
}

def dim(device,childId="Master"){
	deviceChange = false
    device.each{
        if(isDimmable(it)){
            if(isFan(it) == true){
                if(!stateOn(it)){
                    singleOn(it,childId)
                    setToLevel(it,100,childId)
					reschedule(it)
                } else {
                    if(roundFanLevel(it.currentLevel) == 34){
                        singleOff(it,childId)
                    	deviceChange = true
                    } else {
                        setToLevel(it,roundFanLevel(it.currentLevel - 33),childId)
                    }
                }
            } else if(isFan(it) != true){
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
}

def brighten(device,childId="Master"){
	deviceChange = false
    device.each{
        if(isDimmable(it)){
            if(isFan(it) == true){
                if(!stateOn(it)){
                    singleOn(it,childId)
                    setToLevel(it,34,childId)
					reschedule(it)
                } else {
                    if(roundFanLevel(it.currentLevel) == 100){
                        singleOff(it,childId)
						deviceChange = true
                    } else {
                        setToLevel(it,roundFanLevel(it.currentLevel + 33))
                    }
                }
            } else if(isFan(it) != true){
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
}

def singleTemp(device, temp,childId="Master"){
    child = getAppLabel(childId)
    if(!isTemp(device)) return
    device.setColorTemperature(temp as int)
    log.info "$child: $device temperature set to (about) $temp."
}

def singleColor(device, hue, sat, childId="Master"){
    child = getAppLabel(childId)
    if(!isTemp(device)) return
	newValue = [hue: hue, saturation: sat]
    device.setColor(newValue)
    log.info "$child: $device color hue to $hue; saturation to $sat."
}

def validateTemp(value, childId="Master"){
        if(value){
           value = value as int   
            if(value < 2200 || value > 6500){
                child = getAppLabel(childId)
                value = null
                log.debug "$child: Default Temp Color is invalid."
            }
        }
    return value
}

def validateLevel(value, childId="Master"){
	if(value){
		value = value as int 
		if(value < 1 || value > 100){
			child = getAppLabel(childId)
			value = null
			log.debug "$child: Default Level is invalid."
		}
	}
	return value
}

def validateMultiplier(value, childId="Master"){
    if(value != null){
        if(value < 1 || value > 100){
            child = getAppLabel(childId)
            value = null
            log.debug "$child: Multiplier is invalid."
        }
    }
    return value
}

def isDimmable(device){
    def deviceCapability
    device.capabilities.each {
        deviceCapability += it.name
    }
    return deviceCapability.contains("SwitchLevel")
}

def isTemp(device){
   def deviceCapability
    device.capabilities.each {
        deviceCapability += it.name
    }
    return deviceCapability.contains("ColorTemperature")
}

def isColor(device){
    def deviceCapability
    device.capabilities.each {
        deviceCapability += it.name
    }
    return deviceCapability.contains("ColorMode")
}

def isFan(device){
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

def flashGreen(device){

    if(!isColor(device)) return
	newValue = [hue: 33, saturation: 100]
    device.setColor(newValue)
    pause(750)
	newValue = [hue: 66, saturation: 0]
    device.setColor(newValue)
	log.debug "true"
}

def roundFanLevel(level){
    if(isFan(device)) {
    	return Math.round(level / 33) * 33 + 1
    } else {
        return level
    }
}

def getAppLabel(childId){
    childApps.each { 
        if(it.getId() == childId) child = it.label
    }
    if(child) {
        return child
    } else {
        return childId
    }
}

def reschedule(device){
	childApps.each {Child->
		if(Child.label.substring(0,4) == "Time") {
			Child.reschedule(device)
		}
	}
}
