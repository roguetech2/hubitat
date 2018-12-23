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
*  Version = "0.0.03"
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
						paragraph "<div style=\"background-color:BurlyWood\"><b> Select which devices to control:</b></div>"
						input "switches", "capability.switch", title: "Fan (or other switch)?", multiple: true, required: true, submitOnChange:true
						if(!switches){
							paragraph "<div style=\"background-color:BurlyWood\"> </div>"
						} else if(switches){
							varStartNumber = 0
							if(humidityControlDevice && humidityControlStartDifference) varStartNumber++
							if(humidityStartThreshold) varStartNumber++
							if(humidityIncreaseRate) varStartNumber++
							if(varStartNumber != 1){
								paragraph "<div style=\"background-color:BurlyWood\"><b> Conditions to start fan:</b></div>"
							} else {
								paragraph "<div style=\"background-color:BurlyWood\"><b> Condition to start fan:</b></div>"
							}
							input "humidityControlEnable", "bool", title: "Use control device?", submitOnChange:true
							if(humidityControlEnable){
								input "humidityControlDevice", "capability.relativeHumidityMeasurement", title: "Control humidity sensor(s)?", multiple: false, required: true, submitOnChange:true
								if(humidityControlDevice){
									input "humidityControlStartDifference", "number", title: "Percent difference from the control device $humidityControlDevice?", required: true, submitOnChange:true
								} else {
									input "humidityControlStartDifference", "number", title: "Percent difference from control?", required: true, submitOnChange:true
								}
							}
							if(humidityControlEnable && (!humidityControlDevice || !humidityControlStartDifference)){
								paragraph "<div style=\"background-color:BurlyWood\"> </div>"
							} else if(!humidityControlEnable || (humidityControlDevice && humidityControlStartDifference)){
        						input "humidityStartThreshold", "number", title: "Absolute humidity level?", required: false, submitOnChange:true
/* ************************************************** */
/* TO-DO: Make increase based on time, rather than    */
/* polling rate.                                      */
/* ************************************************** */
								input "humidityIncreaseRate", "number", title: "Humidity increase?", required: false, submitOnChange:true
								if(varStartNumber > 1) {
									input "multiStartTrigger", "enum", title: "Turn on if any condition is matched, or all conditions?", required: true, options: ["any":"Any of the conditions", "all":"All of the conditions"], defaultValue: "any", submitOnChange:true
									if(multiStartTrigger == "all"){
										varMessage = "You have indicated the fan should turn on only if the humidity "
										if(humidityIncreaseRate) varMessage += "rises by $humidityIncreaseRate"
										if(humidityStartThreshold){
											if(humidityIncreaseRate){
												varMessage += " to at least $humidityStartThreshold"
											} else {
												varMessage += " is at least $humidityStartThreshold"
											}
										}
										if(humidityControlStartDifference && humidityControlDevice)
											varMessage += ", while being no more than $humidityControlStartDifference% higher than $humidityControlDevice"
										varMessage += ". All conditions must be matched."
										paragraph "<div style=\"background-color:AliceBlue\">$infoIcon $varMessage</div>"
									}
								}
								if(!humidityIncreaseRate && !humidityThreshold && (varStartNumber > 1 && (multiStartTrigger != "any" && multiStartTrigger != "all"))){
									paragraph "<div style=\"background-color:BurlyWood\"> </div>"
								} else if(humidityIncreaseRate || humidityThreshold){
									varStopNumber = 0
									if(humidityControlDevice && humidityControlStopDifference) varStopNumber++
									if(humidityStopThreshold) varStopNumber++
									if(humidityStopDecrease) varStopNumber++
									if(humidityStopMinutes) varStopNumber++
									if(varStopNumber != 1){
										paragraph "<div style=\"background-color:BurlyWood\"><b> Conditions to stop fan:</b></div>"
									} else {
										paragraph "<div style=\"background-color:BurlyWood\"><b> Condition to stop fan:</b></div>"
									}
									if(humidityControlEnable && humidityControlDevice && humidityControlStopDifference){
										input "humidityControlStopDifference", "number", title: "Percent difference from the control device $humidityControlDevice?", required: false, submitOnChange:true
									}
									if(humidityStartThreshold){
										input "humidityStopThreshold", "number", title: "Absolute humidity level?", required: false, submitOnChange:true
										paragraph "<div style=\"background-color:AliceBlue\">$infoIcon You <i>can</i> make this less than starting threshold of $humidityStartThreshold, but it's not recommended.</div>"
									}
									input "humidityStopDecrease", "number", title: "Percent above the starting humidity?", required: false, submitOnChange:true
									paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Enter zero for humidity to be completely normalized, but only with caution. If the ambiant humidity level rises, it may never reach the starting humidity.</div>"
									input "humidityStopMinutes", "number", title: "After minutes?", required: false, submitOnChange:true
									if(varStopNumber > 1){
										input "multiStopTrigger", "enum", title: "Turn off if any condition is matched, or all conditions?", required: true, options: ["any":"Any of the conditions", "all":"All of the conditions"], defaultValue: "any", submitOnChange:true
/* ************************************************** */
/* TO-DO: Build string for what "all conditions"      */
/* actually are.                                      */
/* ************************************************** */
										if(multiStopTrigger == "all"){
											msgString = "You have indicated the fan should turn off only if humidity is "
											if(humidityStopMinutes) msgString += "at most $humidityStopThreshold"
											if((!(humidityControlStopDifference && humidityControlDevice) && !humidityStopDecrease) || (!humidityStopDecrease && !humidityStopMinutes)) {
												msgString += ", and "
											} else {
												msgString += ", "
											}
											if(humidityControlStopDifference && humidityControlDevice) {
												msgString += "no more than $humidityControlStopDifference% higher than $humidityControlDevice"
												if(!humidityStopDecrease && !humidityStopDecrease) {
												   msgString += "."
												} else if(!humidityStopDecrease || !humidityStopDecrease) {
												   msgString += ", and "
												} else {
												   msgString += ", "
											   }
											}
											if(humidityStopDecrease) {
											   msgString += "no more than $humidityStopDecrease% of starting level"
											   if(humidityStopMinutes){
												   msgString += ", and "
											   }
											}
											if(humidityStopMinutes) msgString += "after at least $humidityStopMinutes minutes"
											msgString += ". All conditions must be matched."
											paragraph "<div style=\"background-color:AliceBlue\">$infoIcon $msgString</div>"
										}
									}
									
										
									//input "humidityDropTimeout", "number", title: "After minutes after reaching $humidityDropLimit% of starting humidity?", required: true, submitOnChange:true
									if(varStopNumber == 0){
										paragraph "<div style=\"background-color:BurlyWood\"> </div>"
									} else if(varStopNumber > 1){
										paragraph "<div style=\"background-color:BurlyWood\"><b> Condition to stop fan manually turned on:</b></div>"
										if(humidityControlEnable && humidityControlDevice && humidityControlStopDifference)
											input "humidityControlStopDifferenceManual", "bool", title: "Require humidity to be $humidityControlStopDifference% over $humidityControlDevice?", submitOnChange:true
										if(humidityStopThreshold)
											input "humidityStopThresholdManual", "bool", title: "Require humidity to be below $humidityStopThreshold?", submitOnChange:true
										if(humidityStopMinutes)
											input "humidityStopMinutesManual", "bool", title: "Require $humidityStopMinutes mintues to have passed?", submitOnChange:true
										paragraph "<div style=\"background-color:AliceBlue\">$infoIcon These options also apply if fan is turned on in any other way than by this rule-set (eg a schedule). Options combine; if more than one is selected, the fan will only be turned off if all the conditions are true.</div>"
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

/*
Input fields:

humidityDisableAll - bool
	disables all humidity routines
humidityDevice - device - required - multiple
	primary humidity sensor
humidityDisable - bool
	disables this humidity routine
switches - device - required - multiple
	fan switch to be turned on/off
humidityControlEnable - bool
	shows options for a "control" sensor
humidityControlDevice - device
	"control" device. Only appears if humidityControlEnable, but could be set regardless. Must be combined with humidityControlStartDifference
humidityControlStartDifference - number - required
	difference between primary and control sensors. Only appears if humidityControlEnable, but could be set regardless. Must be combined with humidityControlDevice
humidityStartThreshold - number
	absolute sensor value to turn on
humidityIncreaseRate - number
	amount of change with sensor to turn on
multiStartTrigger - enum [any, all] 
	indicates to use humidityControlStartDifference, humidityStartThreshold, AND/OR humidityIncreaseRate
humidityControlStopDifference - number
	percent difference of sensor compared to control sensor to turn off
humidityStopThreshold - number
	absolute sensor value to turn off
humidityStopDecrease - number
	amount of change with sensor to turn off
humidityStopMinutes - number
	minutes of run time to turn off
multiStopTrigger - enum [any, all]
	indicates to use humidityControlStopDifference, humidityStopThreshold, humidityStopDecrease AND/OR humidityStopMinutes
humidityDropTimeout - number
	NOT USED
humidityControlStopDifferenceManual - bool
	if manually on, indicates to use humidityControlStopDifference to turn off
humidityStopThresholdManual - bool
	if manually on, indicates to use humidityStopThreshold to turn off
humidityStopMinutesManual - bool
	if manually on, indicates to use humidityStopMinutes to turn off
*/

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

	state.automaticallyTurnedOn = false
	state.turnOffLaterStarted = false
	subscribe(humiditySensor, "humidity", humidityHandler)
	subscribe(humidityControlDevice, "humidity", NeedFunctionForThis)
	subscribe(switches, "switch", switchHandler)
}

def humidityHandler(evt) {
	logTrace("$app.label: humidityHandler starting [evt:  $evt ($evt.value)]")

	if(humidityDisable || state.humidityDisable) {
		logTrace("$app.label: function humidityHandler returning (humidity disabled)")
		return
	}

	// Set state variables
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

	// Check if ALL conditions are true
	if(multiStartTrigger){
		fanOn = true
		// Compare it to the "control" device (checking for negative result)
		if(humidityControlDevice && humidityControlStartDifference){
			if(humidityControlDevice.currentHumidity + humidityControlStartDifference > state.currentHumidity) {
				fanOn = false
				logTrace("$app.label: function humidityHandler (not turning on becsause control device $humidityControlDevice.currentHumidity + humidityControlStartDifference $humidityControlStartDifference > current humidity $state.currentHumidity)")
			}	
		}
		// Check if over the base threshold (checking for negative result)
		if(humidityStartThreshold &&  humidityStartThreshold > state.currentHumidity) {
			fanOn = false
			logTrace("$app.label: function humidityHandler (not turning on becsause humidityStartThreshold $humidityStartThreshold > current humidity $state.currentHumidity)")
		}
		// Check if amount increase from last update (checking for negative result)
		if(humidityIncreaseRate && humidityIncreaseRate > state.humidityChangeRate) {
			fanOn = false
			logTrace("$app.label: function humidityHandler (not turning on becsause humidityIncreaseRate $humidityIncreaseRate > humidityChangeRate $state.humidityChangeRate)")
		}
	// Check if ANY condition is true
	} else {
		fanOn = false
		// Compare it to the "control" device
		if(humidityControlDevice && humidityControlStartDifference){
			if(humidityControlDevice.currentHumidity + humidityControlStartDifference <= state.currentHumidity) {
				fanOn = true
				logTrace("$app.label: function humidityHandler (turning on - currentHumidity $humidityControlDevice.currentHumidity + humidityControlStartDifference $humidityControlStartDifference <= currentHumidity $state.currentHumidity)")
			}
		}
		// Check if over the base threshold 
		if(humidityStartThreshold &&  humidityStartThreshold <= state.currentHumidity) {
			fanOn = true
			logTrace("$app.label: function humidityHandler (turning on - humidityStartThreshold $humidityStartThreshold <= currentHumidity $state.currentHumidity)")
		}
		// Check if amount increase from last update
		if(humidityIncreaseRate && humidityIncreaseRate <= state.humidityChangeRate) {
			fanOn = true
			logTrace("$app.label: function humidityHandler (turning on - humidityIncreaseRate $humidityIncreaseRate  <= humidityChangeRate $state.humidityChangeRate)")
		}
	}

	// Turning on
	if(fanOn && !state.TurnOffLaterStarted){
		if(humidityStopMinutes && humidityStopMinutes > 0){
			state.TurnOffLaterStarted = true
			logTrace("$app.label: function humidityHandler scheduling turn off (for $humidityStopMinutes minutes)")
			runIn(60 * humidityStopMinutes.toInteger(), TurnOffFanSwitchCheckHumidity)
		}
		
		parent.multiOn(switches)
		state.startingHumidity = state.lastHumidity
        state.highestHumidity = state.currentHumidity
		logTrace("$app.label: function humidityHandler exiting (turned on, humidity increase [startingHumidity = $state.startingHumidity; highestHumidity = $state.highestHumidity; targetHumidity = $state.targetHumidity]")
	}

	// If nothing is on, then we can exit
	if(!parent.multiStateOn(switches)) return

	// If off requires timer, let schedule handle it
	if(humidityStopMinutesManual && humidityStopMinutes && humidityStopMinutes > 0) return

	// If no conditions (other than timer), we can exit
	if(!humidityControlStopDifferenceManual && !humidityStopThresholdManual) return

	// If should turn off by all applicable conditions
	fanOff = true
	if(humidityControlStopDifferenceManual && humidityControlStopDifference && humidityControlDevice){
		if(humidityControlDevice.currentHumidity + humidityControlStopDifference > state.currentHumidity) fanOff = false
	}
	if(humidityStopThresholdManual && humidityStopThreshold > state.currentHumidity) fanOff = false
	if(fanOff) multiOff()
}

def switchHandler(evt) {
	logTrace("$app.label: switchHandler starting [evt:  $evt ($evt.value)]")

	if(humidityDisable || state.humidityDisable) {
		logTrace("$app.label: function switchHandler returning (humidity disabled)")
		return
	}

	if(evt.value == "on"){
		if(!state.automaticallyTurnedOn && humidityStopMinutesManual && humidityStopMinutes && humidityStopMinutes > 0) {
			logTrace("$app.label: function switchHandler scheduling fan off in $humidityStopMinutes minutes (switched turned on)")
			runIn(60 * humidityStopMinutes.toInteger(), multiOff)
			return
		}
	} else if(evt.value == "off") {
		logTrace("$app.label: function switchHandler exiting (switched turned off)")
		state.automaticallyTurnedOn = false
		state.turnOffLaterStarted = false
	}		   
}

def turnOffFanSwitchCheckHumidity() {
	logTrace("$app.label: turnOffFanSwitchCheckHumidity starting")

	// If nothing is on, then we can exit
	if(!multiStateOn(switches)) return

	// If should turn off by all applicable conditions
	fanOff = true
	if(humidityControlStopDifferenceManual && humidityControlStopDifference && humidityControlDevice){
		if(humidityControlDevice.currentHumidity + humidityControlStopDifference > state.currentHumidity) {
			logTrace("$app.label: function turnOffFanSwitchCheckHumidity not turning off (control device $humidityControlDevice.currentHumidity + humidityControlStopDifference $humidityControlStopDifference > currentHumidity $state.currentHumidity")
			fanOff = false
		}
	}
	if(humidityStopThresholdManual && humidityStopThreshold > state.currentHumidity) {
			logTrace("$app.label: function turnOffFanSwitchCheckHumidity not turning off (humidityStopThreshold $humidityStopThreshold > currentHumidity $state.currentHumidity)")
		fanOff = false
	}
	if(fanOff) {
		logTrace("$app.label: function turnOffFanSwitchCheckHumidity turning off")
		multiOff()
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

def logTrace(message) {
	//log.trace message
}
