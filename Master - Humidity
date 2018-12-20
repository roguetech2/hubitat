/***********************************************************************************************************************
*
*  Copyright (C) 2018 roguetech
*  Derived from Craig Romei Copyright (C) 2018
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
*  Name: Master - Humidity
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master - Humidity.groovy
*  Version: 0.0.02
*
***********************************************************************************************************************/

definition(
    name: "Master - Humidity",
    namespace: "master",
    author: "roguetech",
    description: "Humidity Sensors",
    parent: "master:Master",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png"
)

preferences {
	infoIcon = "<img src=\"http://files.softicons.com/download/system-icons/windows-8-metro-invert-icons-by-dakirby309/ico/Folders%20&%20OS/Info.ico\" width=20 height=20>"
    page(name: "setup", install: true, uninstall: true) {
		
        section() {
			// Set disable all
			if(humidityDisableAll) {
				state.humidityDisable = true
			} else {
				state.humidityDisable = false
			}

			// If all disabled, force reenable
			if(state.humidityDisable){
				input "humidityDisableAll", "bool", title: "<b>All humidity sensors are disabled.</b> Reenable?", defaultValue: false, submitOnChange:true
			// If disabled, show only basic options
			} else if(humidityDisable){
				paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this humidity sensor routine:</b></div>"
				label title: "", required: true, submitOnChange:true
				if(!app.label){
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				} else if(app.label){
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select humidity sensor(s):</b></div>"
					input "humidityDevice", "capability.relativeHumidityMeasurement", title: "Humidity Sensor(s)?", multiple: true, required: true, submitOnChange:true
					input "humidityDisable", "bool", title: "<b><font color=\"#000099\">Humidity sensor is disabled.</font></b> Reenable it?", submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				}
			} else {
				paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this humidity sensor routine:</b></div>"
				label title: "", required: true, submitOnChange:true
				if(!app.label){
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				} else if(app.label){
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select humidity sensors for routine:</b></div>"
					input "humidityDevice", "capability.relativeHumidityMeasurement", title: "Humidity sensor(s)?", multiple: true, required: true, submitOnChange:true
					input "humidityDisable", "bool", title: "Disable this humidity sensor?", submitOnChange:true
					if(!humidityDevice){
						paragraph "<div style=\"background-color:BurlyWood\"> </div>"
					} else if(humidityDevice){
						paragraph "<div style=\"background-color:BurlyWood\"><b>Select which devices to control:</b></div>"
						input "switches", "capability.switch", title: "Fan (or other switch)?", multiple: true, required: false, submitOnChange:true
						if(!switches){
							paragraph "<div style=\"background-color:BurlyWood\"> </div>"
						} else if(switches){
							paragraph "<div style=\"background-color:BurlyWood\"><b>Select humidity thresholds:</b></div>"
							input "humidityIncreaseRate", "number", title: "Humidity Increase Rate :", required: true, defaultValue: 2, submitOnChange:true
        					input "humidityThreshold", "number", title: "Humidity Threshold (%):", required: false, defaultValue: 65, submitOnChange:true
							if(!humidityIncreaseRate || !humidityThreshold){
								paragraph "<div style=\"background-color:BurlyWood\"> </div>"
							} else if(humidityIncreaseRate && humidityThreshold){
								paragraph "<div style=\"background-color:BurlyWood\"><b>Select humidity cut-offs:</b></div>"
								input "humidityDropTimeout", "number", title: "How long after the humidity starts to drop should the fan turn off (minutes):", required: true, defaultValue:  10, submitOnChange:true
								input "humidityDropLimit", "number", title: "What percentage above the starting humidity before triggering the turn off delay:", required: true, defaultValue:  25, submitOnChange:true
								if(!humidityDropTimeout || !humidityDropLimit){
									paragraph "<div style=\"background-color:BurlyWood\"> </div>"
								} else if(humidityDropTimeout && humidityDropLimit){
									paragraph "<div style=\"background-color:BurlyWood\"><b>Select manual cut-offs:</b></div>"
									paragraph "When should the fan turn off when turned on manually?"
									input "manualControlMode", "enum", title: "Off After Manual-On?", required: true, options: ["Manually", "By Humidity", "After Set Time"], defaultValue: "After Set Time", submitOnChange:true
									paragraph "How many minutes until the fan is auto-turned-off?"
									input "manualOffMinutes", "number", title: "Auto Turn Off Time (minutes)?", required: false, defaultValue: 20, submitOnChange:true
								}
							}
						}
					}
				}
			}
		}
	}
}

def installed() {
	logTrace("$app.label: installed")
	if(app.getLabel().length() < 8)  app.updateLabel("Humidity - " + app.getLabel())
	if(app.getLabel().substring(0,8) != "Humidity") app.updateLabel("Humidity - " + app.getLabel())
    initialize()
}

def updated() {
	logTrace("$app.label: updated")
    unsubscribe()
    initialize()
}

def initialize() {
	logTrace("$app.label: initialized")
	
	if(humidityDisable || state.humidityDisable) {
		unschedule()
		logTrace("$app.label: function initialize returning (humidity disabled)")
		return
	}

	state.overThreshold = false
	state.automaticallyTurnedOn = false
	state.turnOffLaterStarted = false
	subscribe(humiditySensor, "humidity", humidityHandler)
	subscribe(switches, "switch", switchHandler)
}

def humidityHandler(evt) {
	logTrace("$app.label: humidityHandler starting [evt:  $evt ($evt.value)]")

	if(humidityDisable || state.humidityDisable) {
		logTrace("$app.label: function humidityHandler returning (humidity disabled)")
		return
	}

	// Set state variables
	state.overThreshold = checkThreshold(evt)
	state.lastHumidityDate = state.currentHumidityDate
	if (state.currentHumidity) {
		state.lastHumidity = state.currentHumidity
	} else {
		state.lastHumidity = 100
	}
	if(!state.startingHumidity) state.startingHumidity = 100
	if(!state.highestHumidity) state.highestHumidity = 100
	state.currentHumidity = Double.parseDouble(evt.value.replace("%", ""))
	state.currentHumidityDate = evt.date.time
	state.humidityChangeRate = state.currentHumidity - state.lastHumidity
    if(state.currentHumidity > state.highestHumidity) state.highestHumidity = state.currentHumidity

	state.targetHumidity = state.startingHumidity + humidityDropLimit / 100 * (state.highestHumidity - state.startingHumidity)       

	logTrace("$app.label: function humidityHandler [lastHumidity = $state.lastHumidity]")
	logTrace("$app.label: function humidityHandler [lastHumidityDate = $state.lastHumidityDate]")
	logTrace("$app.label: function humidityHandler [currentHumidity = $state.currentHumidity]")
	logTrace("$app.label: function humidityHandler [currentHumidityDate = $state.currentHumidityDate]")
	logTrace("$app.label: function humidityHandler [startingHumidity = $state.startingHumidity]")
	logTrace("$app.label: function humidityHandler [highestHumidity = $state.highestHumidity]")
	logTrace("$app.label: function humidityHandler [humidityChangeRate = $state.humidityChangeRate.round(2)]")
	logTrace("$app.label: function humidityHandler [targetHumidity = $state.targetHumidity]")
    
	//if the humidity is high (or rising fast) and the fan is off, kick on the fan
    if (((state.humidityChangeRate > humidityIncreaseRate) || state.overThreshold) && !parent.stateOn(switches)) {
		state.automaticallyTurnedOn = true
		state.turnOffLaterStarted = false
        state.automaticallyTurnedOnAt = new Date().format("yyyy-MM-dd HH:mm")

		logTrace("$app.label: function humidityHandler (turning on; humidity increase)")
		parent.multiOn(switches)

        state.startingHumidity = state.lastHumidity
        state.highestHumidity = state.currentHumidity
		logTrace("$app.label: function humidityHandler exiting (turned on, humidity increase [startingHumidity = $state.startingHumidity; highestHumidity = $state.highestHumidity; targetHumidity = $state.targetHumidity]")


	// Turn off the fan when humidity returns to normal and it was kicked on by the humidity sensor
	} else if((state.automaticallyTurnedOn || manualControlMode == "By Humidity") && !state.turnOffLaterStarted) {    
        if(state.currentHumidity <= state.targetHumidity) {
            if(humidityDropTimeout == 0) {
				if(stateOn(switches)) {
					multiOff()
					logTrace("$app.label: function humidityHandler exiting (turning off, humidity decrease)")
				} else {
					logTrace("$app.label: function humidityHandler exiting (already off, humidity decrease)")
				}
            } else {
				state.turnOffLaterStarted = true
				runIn(60 * humidityDropTimeout.toInteger(), turnOffFanSwitchCheckHumidity)
				logTrace("$app.label: function humidityHandler scheduling fan off in $humidityDropTimeout minutes [turnOffLaterStarted = $state.turnOffLaterStarted]")
			}
		}
	}
}

def switchHandler(evt) {
	logTrace("$app.label: switchHandler starting [evt:  $evt ($evt.value)]")

	if(humidityDisable || state.humidityDisable) {
		logTrace("$app.label: function switchHandler returning (humidity disabled)")
		return
	}

	if(evt.value == "on"){
		if(!state.automaticallyTurnedOn && (manualControlMode == "After Set Time") && manualOffMinutes) {
			if(manualOffMinutes == 0) {
				multiOff()
				logTrace("$app.label: switchHandler exiting (turned off)")
				return
			} else {
				logTrace("$app.label: function switchHandler scheduling fan off in $manualOffMinutes minutes (switched turned on)")
				runIn(60 * manualOffMinutes.toInteger(), multiOff)
				return
			}
		}
	} else if(evt.value == "off") {
		logTrace("$app.label: function switchHandler exiting (switched turned off)")
		state.automaticallyTurnedOn = false
		state.turnOffLaterStarted = false
	}		   
}

def turnOffFanSwitchCheckHumidity() {
	logTrace("$app.label: turnOffFanSwitchCheckHumidity starting")

	if(humidityDisable || state.humidityDisable) {
		logTrace("$app.label: function turnOffFanSwitchCheckHumidity returning (humidity disabled)")
		return
	}

	if(parent.multiStateOn(switches)) {
		logTrace("$app.label: turnOffFanSwitchCheckHumidity [humidityChangeRate: $state.HumidityChangeRate]")
		if(state.currentHumidity > state.targetHumidity) {
			logTrace("$app.label: turnOffFanSwitchCheckHumidity exiting (humidityChangeRate: $state.HumidityChangeRate)")
			state.automaticallyTurnedOn = true
			state.automaticallyTurnedOnAt = now()
			state.turnOffLaterStarted = false
			return
        } else {
			logTrace("$app.label: turnOffFanSwitchCheckHumidity exiting (turning off)")
			multiOff()
		}
	}
}

def multiOff() {
	logTrace("$app.label: multiOff starting")
	if(parent.multiStateOn(switches)){
		parent.multiOff(switches)
        state.automaticallyTurnedOn = false
        state.turnOffLaterStarted = false
	}
	logTrace("$app.label: multiOff exiting")
}

def checkThreshhold(evt) {
	logTrace("$app.label: checkThrehold starting [evt:  $evt ($evt.value)]")
	double lastEvtValue = Double.parseDouble(evt.value.replace("%", ""))
	if(lastEvtValue >= humidityThreshold) {
		logTrace("$app.label: checkThrehold returning true (humidity: lastevtvalue; threshhold: humidityThreshold]")
		return true
	} else {
		logTrace("$app.label: checkThreshold returning false")
		return false
	}
}

def logTrace(message) {
	//log.trace message
}
