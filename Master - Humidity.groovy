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
*  <http://www.gnu.org/licenses/> for more details.
*
*  Name: Master - Humidity
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master%20-%20Humidity.groovy
*  Version: 0.1.30
*
***********************************************************************************************************************/

definition(
    name: "Master - Humidity",
    namespace: "master",
    author: "roguetech",
    description: "Humidity Sensors",
    parent: "master:Master",
    category: "Convenience",
    importUrl: "https://raw.githubusercontent.com/roguetech2/hubitat/master/Master%20-%20Humidity.groovy",
    iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@2x.png"
)

// logLevel sets number of log messages
// 0 for none
// 1 for errors only
// 5 for all
def getLogLevel(){
    return 5
}

preferences {
	infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
	errorIcon = "<img src=\"http://emily-john.love/icons/error.png\" width=20 height=20>"
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
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select trigger humidity sensor(s):</b></div>"
					input "humidityDevice", "capability.relativeHumidityMeasurement", title: "Humidity sensor(s)?", multiple: true, required: true, submitOnChange:true
					if(humidityDevice){
						// If more than one device, average them?
						numDevices = humidityDevice.size()
						if(numDevices > 1)  {
							if(!humidityDeviceAverage) {
								input "humidityDeviceAverage", "bool", title: "<b>Averaging humidity sensors.</b> Click to not average.", submitOnChange:true
								paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Humidity sensor levels will be averaged together for better accuracy. Click to use all of the humidity sensor levels independantly.</div>"
							} else {
								input "humidityDeviceAverage", "bool", title: "<b>Not averaging humidity sensors.</b> Click to average.", submitOnChange:true
								paragraph "<div style=\"background-color:AliceBlue\">$infoIcon All of the humidity sensor levels will be used independantly. Click to average humidity sensors together for better accuracy.</div>"
							}
						}
					}
					input "humidityDisable", "bool", title: "<b><font color=\"#000099\">Humidity sensor is disabled.</font></b> Reenable it?", submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				}
			} else {
				paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this humidity sensor routine:</b></div>"
				label title: "", required: true, submitOnChange:true
				if(!app.label){
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				} else if(app.label){
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select trigger humidity sensor(s):</b></div>"
					input "humidityDevice", "capability.relativeHumidityMeasurement", title: "Humidity sensor(s)?", multiple: true, required: true, submitOnChange:true
					if(humidityDevice){
						currentHumidity = averageHumidity(humidityDevice)
						// If more than one device, average them?
						numDevices = humidityDevice.size()
						if(numDevices > 1)  {
							if(!humidityDeviceAverage) {
								input "humidityDeviceAverage", "bool", title: "<b>Averaging humidity sensors.</b> Click to not average.", submitOnChange:true
								paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Humidity sensor levels will be averaged together for better accuracy. Click to use all of the humidity sensor levels independantly.</div>"
							} else {
								input "humidityDeviceAverage", "bool", title: "<b>Not averaging humidity sensors.</b> Click to average.", submitOnChange:true
								paragraph "<div style=\"background-color:AliceBlue\">$infoIcon All of the humidity sensor levels will be used independantly. Click to average humidity sensors together for better accuracy.</div>"
							}
						}
					}
					input "humidityDisable", "bool", title: "Disable this humidity sensor?", submitOnChange:true
					if(!humidityDevice){
						paragraph "<div style=\"background-color:BurlyWood\"> </div>"
					} else if(humidityDevice){
						paragraph "<div style=\"background-color:BurlyWood\"><b> Select which device(s) to control:</b></div>"
						input "switches", "capability.switch", title: "Fan (or other switch)?", multiple: true, required: true, submitOnChange:true
						if(!switches){
							paragraph "<div style=\"background-color:BurlyWood\"> </div>"
						} else if(switches){
							varStartNumber = 0
							if(humidityControlStartDifference) varStartNumber++
							if(humidityStartThreshold) varStartNumber++
							if(humidityIncreaseRate) varStartNumber++
							varStopNumber = 0
							if(humidityControlStopDifference) varStopNumber++
							if(humidityStopThreshold) varStopNumber++
							if(humidityStopDecrease) varStopNumber++
							if(humidityStopMinutes) varStopNumber++
							
							paragraph "<div style=\"background-color:BurlyWood\"><b> Select which the control sensor(s):</b></div>"
							if(!humidityControlEnable){
								input "humidityControlEnable", "bool", title: "<b>Using control humidity sensor(s).</b> Click to <b>not</b> use control humidity sensor(s).", submitOnChange:true
								input "humidityControlDevice", "capability.relativeHumidityMeasurement", title: "Control humidity sensor(s)?", multiple: true, required: true, submitOnChange:true
								if(humidityControlDevice){
									// Check if control device is a primary device
									error = compareDeviceLists(humidityDevice,humidityControlDevice)
									if(error)
										paragraph "<div style=\"background-color:Bisque\">$errorIcon Control sensors can't be include a primary sensor.</div>"
								} else {
									paragraph "<div style=\"background-color:BurlyWood\"> </div>"
								}
							} else {
								humidityControlDevice = null
								humidityControlStartDifference = null
								humidityControlStopDifference = null
								input "humidityControlEnable", "bool", title: "<b>Not using control humidity sensor(s).</b> Click to select control humidity sensor(s).", submitOnChange:true
							}
						}
					}
					if(humidityControlEnable || (!humidityControlEnable && humidityControlDevice)){
						if(varStartNumber != 1 && !error){
							paragraph "<div style=\"background-color:BurlyWood\"><b> Conditions to start fan:</b></div>", width: 6
						} else if(!error) {
							paragraph "<div style=\"background-color:BurlyWood\"><b> Condition to start fan:</b></div>", width: 6
						}
						if(varStopNumber != 1 && !error){
							paragraph "<div style=\"background-color:BurlyWood\"><b> Conditions to stop fan:</b></div>", width: 6
						} else if(!error) {
							paragraph "<div style=\"background-color:BurlyWood\"><b> Condition to stop fan:</b></div>", width: 6
						}

						if(!humidityControlEnable && humidityControlDevice && !error){
							// Check if multiple controls (if so, say we're averaging)
							numControls = humidityControlDevice.size()
							// Get current humidity level of control(s)
							currentControlHumidity = averageHumidity(humidityControlDevice)
							if(numControls > 1){
								input "humidityControlStartDifference", "number", title: "Percent difference from the average of control devices?", required: false, width: 6, submitOnChange:true
							} else {
								input "humidityControlStartDifference", "number", title: "Percent difference from the control device?", required: false, width: 6, submitOnChange:true
							}
							if(numControls > 1){
								input "humidityControlStopDifference", "number", title: "Percent difference from the average of control devices?", default: humidityControlStartDifference, width: 6, required: false, submitOnChange:true
							} else {
								input "humidityControlStopDifference", "number", title: "Percent difference from the control device?", default: humidityControlStartDifference, width: 6, required: false, submitOnChange:true
							}
							if(humidityControlStartDifference > 100 || humidityControlStopDifference > 100) {
								error = true
								paragraph "<div style=\"background-color:Bisque\">$errorIcon Percent difference must be less than 100.</div>"
							} else if(humidityControlStartDifference == 0 || humidityControlStopDifference == 0){
								error = true
								paragraph "<div style=\"background-color:Bisque\">$errorIcon Percent difference can't equal zero.</div>"
							} else if((humidityControlStartDifference && humidityControlStartDifference < 0) || (humidityControlStopDifference && humidityControlStopDifference < 0)){
								error = true
								paragraph "<div style=\"background-color:Bisque\">$errorIcon Percent difference can't be less than zero.</div>"
							}

							// Build message with current levels
							varMessage = "<div style=\"background-color:AliceBlue\">$infoIcon These uses relative differences. For instance, the control sensor"
							if(numControls > 1) varMessage += "s"
							varMessage += " currently show"
							if(numControls == 1) varMessage += "s"
							varMessage += " $currentControlHumidity% humidity, "
							if(!humidityControlStartDifference) {
								varMessage += "so a value of 40 would require the primary sensor to be at " + ((100 - currentControlHumidity) * 40 / 100 + currentControlHumidity) + "% (rather than $currentControlHumidity + 40 = " + (currentControlHumidity + 40) + "%)."
							} else {
								varMessage += "so this requires the primary sensor to be at " + ((100 - currentControlHumidity) * humidityControlStartDifference / 100 + currentControlHumidity) + "% (rather than $currentControlHumidity + $humidityControlStartDifference = " + (currentControlHumidity + humidityControlStartDifference) + "%)."
							}
							varMessage += " Just FYI, primary sensor"
							if(numDevices > 1) {
								varMessage += "s current average is"
							} else {
								varMessage += " is currently"
							}
							varMessage += " $currentHumidity%.</div>" 
							if(!error) paragraph varMessage
						}
								
						if(!humidityControlEnable && !humidityControlDevice && !error){
							paragraph "<div style=\"background-color:BurlyWood\"> </div>"
						} else if((humidityControlEnable || humidityControlDevice) && !error){
							input "humidityStartThreshold", "number", title: "Absolute humidity level? (Currently at $currentHumidity%.)", required: false, width: 6, submitOnChange:true
							input "humidityStopThreshold", "number", title: "Absolute humidity level?", required: false, width: 6, submitOnChange:true

							if(humidityStartThreshold > 100 || humidityStopThreshold > 100) {
								error = true
								paragraph "<div style=\"background-color:Bisque\">$errorIcon Absolute humidity level must be less than 100.</div>"
							} else if(humidityStartThreshold == 0 || humidityStopThreshold == 0){
								error = true
								paragraph "<div style=\"background-color:Bisque\">$errorIcon Absolute humidity level can't equal zero.</div>"
							} else if((humidityStartThreshold && humidityStartThreshold < 0) || (humidityStopThreshold && humidityStopThreshold < 0)){
								error = true
								paragraph "<div style=\"background-color:Bisque\">$errorIcon Absolute humidity level can't be less than zero (use whole numbers).</div>"
							}

							if(!error) {
								input "humidityIncreaseRate", "number", title: "Humidity increase?", required: false, width: 6, submitOnChange:true
									input "humidityStopDecrease", "number", title: "Percent above the starting humidity?", required: false, width: 6, submitOnChange:true
								if(humidityIncreaseRate > 100) {
									error = true
									paragraph "<div style=\"background-color:Bisque\">$errorIcon Humidity increase rate must be less than 100.</div>"
								} else if(humidityIncreaseRate == 0){
									error = true
									paragraph "<div style=\"background-color:Bisque\">$errorIcon Humidity increase rate can't equal zero.</div>"
								} else if(humidityIncreaseRate && humidityIncreaseRate < 0){
									error = true
									paragraph "<div style=\"background-color:Bisque\">$errorIcon Humidity increase rate can't be less than zero (use whole numbers).</div>"
								}
								if(!error) paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Humidity increase is the difference between two sensor updates, so depends on polling rate.</div>", width: 6
								}
							if(!error){
								input "humidityStopMinutes", "number", title: "After minutes?", required: false, width: 6, submitOnChange:true
								if(humidityStopMinutes == 0){
									error = true
									paragraph "<div style=\"background-color:Bisque\">$errorIcon Minutes can't equal zero.</div>"
								} else if(humidityStopMinutes && humidityStopMinutes < 0){
									error = true
									paragraph "<div style=\"background-color:Bisque\">$errorIcon Minutes can't be less than zero.</div>"
								}
							}

							if(varStartNumber > 1 && !error) {
								if(!multiStartTrigger){
									input "multiStartTrigger", "bool", title: "<b>Requiring <i>any</i> one of the conditions.</b> Click to require all conditions.", width: 6, submitOnChange:true
								} else if(multiStartTrigger){
									input "multiStartTrigger", "bool", title: "<b>Requiring <i>all</i> of the conditions.</b> Click to require any one of the conditions.", width: 6, submitOnChange:true
								}
							} else if(!error){
								paragraph "", width: 6
							}
							if(varStopNumber > 1 && !error) {
								if(!multiStopTrigger){
									input "multiStopTrigger", "bool", title: "<b>Requiring <i>any</i> one of the conditions.</b> Click to require all conditions.", width: 6, submitOnChange:true
								} else if(multiStopTrigger){
									input "multiStopTrigger", "bool", title: "<b>Requiring <i>all</i> of the conditions.</b> Click to require any one of the conditions.", width: 6, submitOnChange:true
								}
							} else if(!error){
								paragraph "", width: 6
							}
							if(varStartNumber > 1 && multiStartTrigger && !error) {
								// Build message stating all criteria
								varMessage = "Current humidity is " + Math.round(currentHumidity) + ". The fan will turn on if the humidity"
								if(humidityIncreaseRate)
									varMessage += " rises by $humidityIncreaseRate% (eg to " + Math.round((currentHumidity * humidityIncreaseRate / 100) + currentHumidity) + ")"
								if(humidityStartThreshold){
									if(humidityIncreaseRate){
										varMessage += " while being at least $humidityStartThreshold"
										if(currentHumidity < humidityStartThreshold){
											varMessage += " (eg " + Math.round(humidityStartThreshold - currentHumidity) + " higher than now)"
										} else {
											varMessage += " (eg " + Math.round(currentHumidity - humidityStartThreshold) + " lower than now)"
										}
									} else {
										varMessage += " is at least $humidityStartThreshold"
										if(currentHumidity < humidityStartThreshold){
											varMessage += " (eg " + Math.round(humidityStartThreshold - currentHumidity) + " higher than now)"
										} else {
											varMessage += " (eg " + Math.round(currentHumidity - humidityStartThreshold) + " lower than now)"
										}
									}
								}
								if(humidityControlStartDifference && humidityControlDevice)
									varMessage += ", while being no more than $humidityControlStartDifference% higher than $humidityControlDevice (eg " + Math.round(getRelativePercentage(currentControlHumidity,humidityControlStartDifference)) + ")"
								varMessage += ". All conditions must be matched."
								paragraph "<div style=\"background-color:AliceBlue\">$infoIcon $varMessage</div>", width: 6
							} else {
								paragraph "", width: 6
							}

							if(varStopNumber > 1 && multiStopTrigger && !error){
								varMessage = ""
								if(!multiStartTrigger) varMessage = "Current humidity is " + Math.round(currentHumidity) +". "
								varMessage += "The fan will turn off only if humidity is "
								if(humidityStopThreshold) {
									varMessage += "at most $humidityStopThreshold"
									if(currentHumidity < humidityStopThreshold){
										varMessage += " (eg " + Math.round(humidityStopThreshold - currentHumidity) + " higher than now)"
									} else {
										varMessage += " (eg " + Math.round(currentHumidity - humidityStopThreshold) + " lower than now)"
									}
								}
								if((!(humidityControlStopDifference && humidityControlDevice) && !humidityStopDecrease) || (!humidityStopDecrease && !humidityStopMinutes)) {
									varMessage += ", and "
								} else {
									varMessage += ", "
								}
								if(humidityControlStopDifference && humidityControlDevice) {
									varMessage += "no more than $humidityControlStopDifference% higher than $humidityControlDevice (eg " + Math.round(getRelativePercentage(currentControlHumidity,humidityControlStopDifference)) + ")"
									if((humidityStopDecrease && !humidityStopMinutes) || (!humidityStopDecrease && humidityStopMinutes)) {
									   varMessage += ", and "
									} else if(humidityStopDecrease && humidityStopMinutes) {
									   varMessage += ", "
								   }
								}
								if(humidityStopDecrease) {
								   varMessage += "no more than $humidityStopDecrease% of starting level (eg " + Math.round((currentHumidity * humidityStopDecrease / 100 + currentHumidity)) + ")"
								   if(humidityStopMinutes){
									   varMessage += ", and "
								   }
								}
								if(humidityStopMinutes) varMessage += "after at least $humidityStopMinutes minutes"
								varMessage += ". All conditions must be matched."
								paragraph "<div style=\"background-color:AliceBlue\">$infoIcon $varMessage</div>", width: 6
							} else {
								paragraph "", width: 6
							}

							if(!humidityIncreaseRate && !humidityStartThreshold && !humidityControlStartDifference && !error){
								paragraph "<div style=\"background-color:BurlyWood\"> </div>"
							} else if((humidityIncreaseRate || humidityStartThreshold || humidityControlStartDifference) && !error){
								if((humidityControlStopDifference || humidityStopThreshold || humidityStopDecrease) && !error){
									paragraph "<div style=\"background-color:BurlyWood\"> </div>"
									input "humidityWaitMinutes", "number", title: "Minutes to delay turning off (to prevent \"cycling\" on and off)?", required: false, submitOnChange:true
									if(humidityWaitMinutes == 0){
										error = true
										paragraph "<div style=\"background-color:Bisque\">$errorIcon Minutes can't equal zero.</div>"
									} else if(humidityWaitMinutes && humidityWaitMinutes < 0){
										error = true
										paragraph "<div style=\"background-color:Bisque\">$errorIcon Minutes can't be less than zero.</div>"
									}
								}

								//input "humidityDropTimeout", "number", title: "After minutes after reaching $humidityDropLimit% of starting humidity?", required: true, submitOnChange:true
								if(varStopNumber == 0 && !error){
									paragraph "<div style=\"background-color:BurlyWood\"> </div>"
								} else if(varStopNumber > 1 && !error){
									paragraph "<div style=\"background-color:BurlyWood\"><b> Condition to stop fan manually turned on:</b></div>"
									if(!humidityControlEnable && humidityControlDevice && humidityControlStopDifference)
										input "humidityControlStopDifferenceManual", "bool", title: "Require humidity to be $humidityControlStopDifference% over $humidityControlDevice?", submitOnChange:true
									if(humidityStopThreshold)
										input "humidityStopThresholdManual", "bool", title: "Require humidity to be below $humidityStopThreshold?", submitOnChange:true
									if(humidityStopMinutes)
										input "humidityStopMinutesManual", "bool", title: "Require $humidityStopMinutes mintues to have passed?", submitOnChange:true
									paragraph "<div style=\"background-color:AliceBlue\">$infoIcon These options also apply if fan is turned on in any other way than by this rule-set (eg a schedule). Options combine; if more than one is selected, the fan will only be turned off if all the conditions are true.</div>"
/* ************************************************** */
/* TO-DO: Add control to stopping manual on.          */
/* ************************************************** */
								}
/* ************************************************** */
/* TO-DO: Fix "finished" bar at bottom of page.       */
/* ************************************************** */
							}
						}
					}
				}
			}
		}
	}
}


def compareDeviceLists(values,compare){
    // Be sure we have original button and comparison button values (odds are, we don't)
    // eg if(!button_1_push_on)
    if(!settings["button_" + values[0] + "_" + values[2] + "_" + values[1]]) return
    if(!settings["button_" + values[0] + "_" + values[2] + "_" + compare]) return
    if(error) return

    settings["button_" + values[0] + "_" + values[2] + "_" + values[1]].each{first->
        settings["button_" + values[0] + "_" + values[2] + "_" + compare].each{second->
            if(first.id == second.id) {
                if(compare == "on" || compare == "off"){
                    text1 = "turn $compare"
                } else {
                    text1 = compare
                }
                if(values[1] == "on" || values[1] == "off"){
                    text2 = "turn " + values[1]
                } else {
                    text2 = values[1]
                }
                returnText = "Can't set same button to $text1 and $text2 the same device."
            }
        }
    }
    return returnText
}
/*
Input fields:

humidityDisableAll - bool
	disables all humidity routines
humidityDevice - device - required - multiple
	primary humidity sensor
humidityDeviceAverage - bool
	false = average
	true = do not average
humidityDisable - bool
	disables this humidity routine
switches - device - required - multiple
	fan switch to be turned on/off
humidityControlEnable - bool
	shows options for a "control" sensor
	false = using control device
	true = not using control device
humidityControlDevice - device
	"control" device. Only appears if humidityControlEnable, but could be set regardless. Must be combined with humidityControlStartDifference
humidityControlStartDifference - number - required
	difference between primary and control sensors. Only appears if humidityControlEnable, but could be set regardless. Must be combined with humidityControlDevice
humidityStartThreshold - number
	absolute sensor value to turn on
humidityIncreaseRate - number
	amount of change with sensor to turn on
multiStartTrigger - bool
	indicates to use humidityControlStartDifference, humidityStartThreshold, AND/OR humidityIncreaseRate
	true = and
	false = or
humidityControlStopDifference - number
	percent difference of sensor compared to control sensor to turn off
humidityStopThreshold - number
	absolute sensor value to turn off
humidityStopDecrease - number
	percent over starting point to turn off
humidityWaitMinutes - number
	minutes after humidityControlStopDifference, humidityStopThreshold, and/or humidityStopDecrease to wait
humidityStopMinutes - number
	minutes of run time to turn off
multiStopTrigger - bool
	indicates to use humidityControlStopDifference, humidityStopThreshold, humidityStopDecrease AND/OR humidityStopMinutes
	true = and
	false = or
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
	if(checkLog(a="trace")) putLog(428,"Installed",a)
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
	if(checkLog(a="trace")) putLog(434,"Updated",a)
	unsubscribe()
	initialize()
}

def initialize() {
	if(checkLog(a="trace")) putLog(440,"Initialized",a)

    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

	if(humidityDisable || state.humidityDisable) {
		unschedule()
		return
	}

	subscribe(humidityDevice, "humidity", humidityHandler)
/* ************************************************** */
/* TO-DO: Make increase based on time, rather than    */
/* polling rate. Until then, don't subscribe to       */
/* control device, since it would mess up state       */
/* vairables. But it will increase responsiveness.    */
/* ************************************************** */
	// subscribe(humidityControlDevice, "humidity", humidityHandler)
	subscribe(switches, "switch", switchHandler)
}

def humidityHandler(evt) {

	if(humidityDisable || state.humidityDisable) return

	// Set state variables
	state.lastHumidityDate = state.currentHumidityDate
	if (state.currentHumidity) {
		state.lastHumidity = state.currentHumidity
	} else {
		state.lastHumidity = 75
	}
	if(!state.startingHumidity) state.startingHumidity = 100
	// Get average humidity (or not)
	if(humidityDeviceAverage){
		state.currentHumidity = Double.parseDouble(averageHumidity(humidityDevice).replace("%", ""))
	} else {
		state.currentHumidity = Double.parseDouble(evt.value.replace("%", ""))
	}
	state.currentHumidityDate = evt.date.time
	state.humidityChangeRate = state.currentHumidity - state.lastHumidity

	// Average control humidity devices
	if(humidityControlDevice)
		state.controlHumidity = averageHumidity(humidityControlDevice)

    if(checkLog(a="debug")){
        putLog(486,"function humidityHandler [lastHumidity = $state.lastHumidity]",a)
        putLog(487,"function humidityHandler [lastHumidityDate = $state.lastHumidityDate]",a)
        putLog(488,"function humidityHandler [currentHumidityDate = $state.currentHumidityDate]",a)
        putLog(489,"function humidityHandler [startingHumidity = $state.startingHumidity]",a)
        putLog(490,"function humidityHandler [humidityChangeRate = $state.humidityChangeRate]",a)
    }

	if(checkLog(a="trace")) putLog(493,"Current humidity of $evt.displayName is $state.currentHumidity; auto on is $state.automaticallyTurnedOn",a)
	fanIsOn = parent.isOnMulti(switches,app.label)

	// If the fan is auto-on, turn it off? (Or need to schedule off?)
	if(fanIsOn && state.automaticallyTurnedOn){
		// Get status for whether criteria to turn off have been matched
		turnFanOff = checkOffCriteria()

		// If in cool-down period, nothing to do
		if(state.shortScheduledOff){
			return
		} else {
			// Schedule timed off - should have already happen when turned on
			if(!turnFanOff && humidityStopMinutes && humidityStopMinutes > 0 && !state.scheduleStarted){
				if(checkLog(a="trace")) putLog(507,"Scheduling check-in to turn off in $humidityWaitMinutes minutes",a)
				state.scheduleStarted = true
				runIn(60 * humidityWaitMinutes.toInteger(), scheduleTurnOff, [overwrite: false])
				return
			}

			// See if we should turn the fan OFF (if auto on)
			// If timer, then don't schedule cool-down period
			if(turnFanOff && !humidityWaitMinutes){
				setStateMulti("off",switches)
				return
			// Otherwise, schedule cool-down if not already scheduled
			} else if(turnFanOff && humidityWaitMinutes && humidityWaitMinutes > 0 && !state.shortScheduledOff){
				runIn(60 * humidityWaitMinutes.toInteger(), scheduleTurnOff, [overwrite: false])
				state.shortScheduledOff = true
				if(checkLog(a="trace")) putLog(522,"Scheduling check-in to turn off in $humidityWaitMinutes minutes",a)
				return
			// Unless there isn't a cool-down, in which case just turn off
			} else if(turnFanOff && !humidityWaitMinutes){
				setStateMulti("off",switches)
				return
			}
		}
	
	// If fan is manual-on, turn it off?
	} else if(fanIsOn && !state.automaticallyTurnedOn){
		// Get status for whether criteria to turn off have been matched
		turnFanOff = checkOffCriteria()
		// Schedule timed off - should have already happened when turned on
		if(!turnFanOff && humidityStopMinutes && humidityStopMinutes > 0 && !state.scheduleStarted){
			if(checkLog(a="trace")) putLog(537,"Scheduling check-in to turn off in $humidityWaitMinutes minutes (manual on).",a)
			state.scheduleStarted = true
			runIn(60 * humidityWaitMinutes.toInteger(), scheduleTurnOff, [overwrite: false])
			return
		// If all criteria matched, turn off
		} else if(turnOffFan){
			setStateMulti("off",switches)
			return
		}

	// If fan is off, turn it on?
	} else if(!fanIsOn){
		// Turning on
		if(checkOnCriteria()){
			setStateMulti("on",switches)
			state.automaticallyTurnedOn = true
			state.humidityStartTime = now()
			state.startingHumidity = state.lastHumidity

			// Set schedule for turn off (setting after turned on, to keep it more accurate)
			if(humidityStopMinutes && humidityStopMinutes > 0){
				if(checkLog(a="trace")) putLog(558,"Scheduling check-in to turn off in $humidityStopMinutes minutes",a)
				state.scheduleStarted = true
				runIn(60 * humidityStopMinutes.toInteger(), scheduleTurnOff, [overwrite: false])
			}
		}
	}
}

def switchHandler(evt) {
	if(humidityDisable || state.humidityDisable) return

	if(evt.value == "on"){
		// Set scheduled turn off
		if(!state.automaticallyTurnedOn && humidityStopMinutesManual && humidityStopMinutes && humidityStopMinutes > 0) {
			if(checkLog(a="trace")) putLog(572,"Scheduling fan off in $humidityStopMinutes minutes (manually turned on)",a)
			runIn(60 * humidityStopMinutes.toInteger(), scheduleTurnOff)
			return
		}
	} else if(evt.value == "off") {
		state.shortScheduledOff = false
		state.scheduleStarted = false
		state.automaticallyTurnedOn = false
		if(checkLog(a="debug")) putLog(580,"Resetting values to off",a)
	}		   
}

def scheduleTurnOff() {
	// Even if criteria is no longer matched, reset cool-down schedule
	// Do not reset state.scheduleStarted, even if expired (if schedule triggers, then it will be reset with multiOff function)
	state.shortScheduledOff = false

	// If nothing on, do nothing
	// Should we (allow) turn off just in case?
	if(!parent.isOnMulti(switches,app.label)) return

	// Get status for whether criteria to turn off are still matched
	turnFanOff = checkOffCriteria()

	if(turnFanOff) setStateMulti("off",switches) // state variables are reset in multiOff
}

def checkOnCriteria(){
	// Check if current humidity is higher than control device + humidityControlStartDifference
	if(humidityControlStartDifference && humidityControlDevice){
		percentThreshold = getRelativePercentage(state.controlHumidity,humidityControlStartDifference)
		if(state.currentHumidity > percentThreshold){
			// Set flag for one of several conditions being met
			if(multiStartTrigger) {
				if(checkLog(a="debug")) putLog(606,"One condition for turning on met (current: $state.currentHumidity; start difference: $humidityControlStartDifference; threshold: $percentThreshold)",a)
				turnFanOn = true
			// Only condition met
			} else {
				if(checkLog(a="debug")) putLog(610,"Condition for turning on met (current: $state.currentHumidity; start difference: $humidityControlStartDifference; threshold: $percentThreshold)",a)
				return true
			}
		//Conditions not met
		} else if(state.currentHumidity <= percentThreshold && multiStartTrigger){
			return false
		}
	}

	// Check if current humidity is higher than start threshold
	if(humidityStartThreshold){
		if(state.currentHumidity > humidityStartThreshold) {
			// Set flag for one of several conditions being met
			if(multiStartTrigger) {
				if(checkLog(a="debug")) putLog(624,"One condition for turning on met (current: $state.currentHumidity; start threshold: $humidityStartThreshold)",a)
				turnFanOn = true
			// Only condition met
			} else {
				if(checkLog(a="debug")) putLog(628,"Condition for turning on met (current: $state.currentHumidity; start threshold: $humidityStartThreshold)",a)
				return true
			}
		//Conditions not met
		} else if(state.currentHumidity <= humidityStartThreshold && multiStartTrigger) {
			return false
		}
	}
	
	// Check increase rate from last update
	if(humidityIncreaseRate){
		if(humidityIncreaseRate > state.humidityChangeRate) {
			// Set flag for one of several conditions being met
			if(multiStartTrigger) {
				if(checkLog(a="debug")) putLog(642,"One condition for turning on met (change rate: $state.humidityChangeRate; threshold: $humidityIncreaseRate)",a)
				turnFanOn = true
			// Only condition met
			} else {
				if(checkLog(a="debug")) putLog(646,"Condition for turning on met (change rate: $state.humidityChangeRate; threshold: $humidityIncreaseRate)",a)
				return true
			}
		//Conditions not met
		} else if(humidityIncreaseRate <= state.humidityChangeRate) {
			return false
		}
	}

	if(turnFanOn) {
		if(checkLog(a="trace")) putLog(656,"All conditions matched; turning fan on",a)
	} else {
		if(checkLog(a="error")) putLog(658,"All conditions for turn on should have been tested",a)
	}
	return turnFanOn
}

def checkOffCriteria(){
	// Check if current humidity is less than control device + humidityControlStopDifference
	if(humidityControlStopDifference && humidityControlDevice && (state.automaticallyTurnedOn || humidityControlStopDifferenceManual)){
		percentThreshold = getRelativePercentage(state.controlHumidity,humidityControlStopDifference)
		if(state.currentHumidity < percentThreshold){
			// Set flag for one of several conditions being met
			if(multiStopTrigger) {
				if(checkLog(a="debug")) putLog(670,"One condition for turning off met (current: $state.currentHumidity; stop difference: $humidityControlStopDifference; threshold: $percentThreshold)",a)
				turnFanOff = true
			// Only condition met
			} else {
				if(checkLog(a="debug")) putLog(674,"Condition for turning off met (current: $state.currentHumidity; stop difference: $humidityControlStopDifference; threshold: $percentThreshold)",a)
				return true
			}
		// Only condition not met
		} else if(state.currentHumidity >= percentThreshold && multiStopTrigger){
			return false
		}
	}

	// Check if current humidity is less than stop threshold
	if(humidityStopThreshold){
		if(state.currentHumidity < humidityStopThreshold) {
			// Set flag for one of several conditions being met
			if(multiStopTrigger) {
				if(checkLog(a="debug")) putLog(688,"One condition for turning off met (current: $state.currentHumidity; stop threshold: $humidityStopThreshold; threshold: $percentThreshold)",a)
				turnFanOff = true
			// Only condition met
			} else {
				if(checkLog(a="debug")) putLog(692,"Condition for turning off met (current: $state.currentHumidity; stop threshold: $humidityStopThreshold; threshold: $percentThreshold)",a)
				return true
			}
		// Only condition not met
		} else if(state.currentHumidity >= humidityStopThreshold && multiStopTrigger) {
			return false
		}
	}
	
	// Check if current humidity is starting + difference
	if(humidityStopDecrease && state.automaticallyTurnedOn){
		percentThreshold = state.startingHumidity * humidityStopDecrease / 100 + state.startingHumidity
		if(state.currentHumidity < percentThreshold){
			// Set flag for one of several conditions being met
			if(multiStopTrigger) {
				if(checkLog(a="debug")) putLog(707,"One condition for turning off met (current: $state.currentHumidity; stop decrease: $humidityStopDecrease; threshold: $percentThreshold)",a)
				turnFanOff = true
			// Only condition met
			} else {
				if(checkLog(a="debug")) putLog(711,"Condition for turning off met (current: $state.currentHumidity; stop decrease: $humidityStopDecrease; threshold: $percentThreshold)",a)
				return true
			}
		// Only condition not met
		} else if(state.currentHumidity >= percentThreshold && multiStopTrigger){
			return false
		}
	}

	// Check if current time is after end time
	if(humidityStopMinutes && (state.automaticallyTurnedOn || humidityStopMinutesManual)){
		time = now()
		stop = state.humidityStartTime + (humidityStopMinutes * 60000)
		if(time > stop){
			// Set flag for one of several conditions being met
			if(multiStopTrigger) {
                message = "One condition for turning off met (time now: $time; stop time: $stop)"
				turnFanOff = true
			// Shouldn't happen, since schedule should have executed
			} else {
                message = "Condition for turning off met (time now: $time; stop time: $stop)"
				return true
			}
            if(checkLog(a="debug")) putLog(734,message,a)
		// Only condition not met
		} else if(time <= stop && multiStopTrigger){
			return false
		}
	}

	if(turnFanOff) {
		if(checkLog(a="trace")) putLog(742,"All conditions matched; turning fan off",a)
	} else {
		if(checkLog(a="error")) putLog(744,"All conditions for turn off should have been tested",a)
	}
	return turnFanOff
}

def averageHumidity(device){
	humidity = 0
	device.each {
		humidity += it.currentHumidity
	}
	humidity = humidity / device.size()
	return humidity
}

def getRelativePercentage(base,percent){
	return (100 - base) * percent / 100 + base
}

// If deviceChange exists, adds deviceId to it; otherwise, creates deviceChange with deviceId
// Delineate values with colons on each side - must match getStateDeviceChange
// Used to track if app turned on device when schedule captures a device state changing to on
// Must be included in all apps using MultiOn
def addDeviceStateChange(singleDeviceId){
    if(atomicState.deviceChange) {
        if(!atomicState.deviceChange.contains(":$singleDeviceId:")) atomicState.deviceChange += ":$singleDeviceId:"
    } else {
        atomicState.deviceChange = ":$singleDeviceId:"
    }
    return
}

// Gets levels as set for the app
// Function must be included in all apps that use MultiOn
def getOverrideLevels(defaults,appAction = null){
    if(!defaults && (settings[appAction + "Level"] || settings[appAction + "Temp"] || settings[appAction + "Hue"] || settings[appAction + "Sat"])) defaults = [:]
    if(settings[appAction + "Level"]) defaults.put("level",settings[appAction + "Level"])
    if(settings[appAction + "Temp"]) defaults.put("temp",settings[appAction + "Temp"])
    if(settings[appAction + "Hue"]) defaults.put("hue",settings[appAction + "Hue"])
    if(settings[appAction + "Sat"]) defaults.put("sat",settings[appAction + "Sat"])
    return defaults       
}

// Returns the value of deviceChange
// Used by schedule when a device state changes to on, to check if an app did it
// It should only persist as long as it takes for the scheduler to capture and
// process both state change request and state change subscription
// Function must be in every app
def getStateDeviceChange(singleDeviceId){
    if(atomicState.deviceChange){
        value = atomicState.deviceChange.indexOf(":$singleDeviceId:")
        // Reset it when it's used, to try and avoid race conditions with multiple fast button clicks
        resetStateDeviceChange()
        return value
    } else {
        return false
    }
}

// Scheduled funtion to reset the value of deviceChange
// Must be in every app using MultiOn
def resetStateDeviceChange(){
    atomicState.deviceChange = null
    return
}

// Humidity only uses on and off
// This is a bit of a mess, but.... 
def setStateMulti(deviceAction,device,appAction = null){
    if(!deviceAction || (deviceAction != "on" && deviceAction != "off")) {
        if(checkLog(a="error")) putLog(813,"Invalid deviceAction \"$deviceAction\" sent to setStateMulti",a)
        return
    }

    // Time in which to allow Hubitat to process sensor change (eg Pico, contact, etc.)
    // as well as the scheduler to process any state change generated by the sensor
    // What's a realistic number to use if someone has a lot of devices attached to a lot 
    // of Picos with a lot of schedules?
    stateDeviceChangeResetMillis = 500

    if(deviceAction == "off"){
        // Reset device change, since we know the last event from this device didn't turn anything on
        resetStateDeviceChange()
        // Turn off devices
        parent.setStateMulti("off",device,app.label)
        return true
    }

    if(deviceAction == "on"){
        // Turn on devices
        parent.setStateMulti("on",device,app.label)
        // Get and set defaults levels for each device
        device.each{
            // Add device ids to deviceChange, so schedule knows it was turned on by an app
            // Needs to be done before turning the device on.
            addDeviceStateChange(it.id)
            // Set scheduled levels, default levels, and/or [this child-app's] levels
            getAndSetSingleLevels(it,appAction)
        }
        if(checkLog(a="debug")) putLog(842,"Device id's turned on are $atomicState.deviceChange",a)
        // Schedule deviceChange reset
        runInMillis(stateDeviceChangeResetMillis,resetStateDeviceChange)
        return true
    }

    if(deviceAction == "toggle"){
        // Create toggleOnDevice list, used to track which devices are being toggled on
        toggleOnDevice = []
        // Set count variable, used for toggleOnDevice
        count = 0
        device.each{
            // Start count at 1; doesn't matter, so long as it matches newCount below
            count = count + 1
            // If toggling to off
            if(parent.isOn(it)){
                parent.setStateSingle("off",it,app.label)
                // Else if toggling on
            } else {
                // When turning on, add device ids to deviceChange, so schedule knows it was turned on by an app
                // Needs to be done before turning the device on.
                addDeviceStateChange(it.id)
                // Turn the device on
                parent.setStateSingle("on",it,app.label)
                // Add device to toggleOnDevice list so when we loop again to set levels, we know whether we
                // just turned it on or not (without knowing how long the device may take to respond)
                toggleOnDevice.add(count)
            }
        }
        if(checkLog(a="debug")) putLog(871,"Device id's toggled on are $atomicState.deviceChange",a)
        // Create newCount variable, which is compared to the [old]count variable
        // Used to identify which lights were turned on in the last loop
        newCount = 0
        device.each{
            // Start newCount at 1 like count above
            newCount = newCount + 1
            // If turning on, set scheduled levels, default levels, and/or [this child-app's] levels
            // If newCount is contained in the list of [old]count, then we toggled on
            if(toggleOnDevice.contains(newCount)){
                getAndSetSingleLevels(it,appAction)
            }
        }
        // Schedule deviceChange reset
        runInMillis(stateDeviceChangeResetMillis,resetStateDeviceChange)
        return true
    }

    if(deviceAction == "resume"){
        // Reset device change, since we know the last event from this device didn't turn anything on
        resetStateDeviceChange()
        device.each{
            // If turning on, set default levels and over-ride with any contact levels
            if(deviceAction == "resume"){
                // If defaults, then there's an active schedule
                // So use it for if overriding/reenabling
                defaults = parent.getScheduleDefaultSingle(it,app.label)
                if(checkLog(a="debug")) putLog(898,"Scheduled defaults are $defaults",a)

                defaults = getOverrideLevels(defaults,appAction)
                if(checkLog(a="debug")) putLog(901,"With " + app.label + " overrides, using $defaults",a)

                // Skipping getting overall defaults, since we're resuming a schedule or exiting;
                // rather keep things the same level rather than an arbitrary default, and
                // if we got default, we'd not turn it off

                parent.setLevelSingle(defaults,it,app.label)
                // Set default level
                if(!defaults){
                    if(checkLog(a="trace")) putLog(910,"No schedule to resume for $it; turning off",a)
                    parent.setStateSingle("off",it,app.label)
                } else {
                    parent.rescheduleIncrementalSingle(it,app.label)
                }
            }
        }
        return true
    }

    if(deviceAction == "none"){
        // Reset device change, since we know the last event from this device didn't turn anything on
        resetStateDeviceChange()
        // If doing nothing, reschedule incremental changes (to reset any overriding of schedules)
        // I think this is the only place we use ...Multi, prolly not enough to justify a separate function
        parent.rescheduleIncrementalMulti(device,app.label)
        return true
    }
}

// Handles turning on a single device and setting levels
// Only called by (child app) multiOn
// appAction is for "open/close", "push/hold", etc., so the child app knows which
// levels to apply for which device/action
def getAndSetSingleLevels(singleDevice,appAction = null){
    // If defaults, then there's an active schedule
    // So use it for if overriding/reenabling
    // In scheduler app, this gets defaults for any *other* schedule
    defaults = parent.getScheduleDefaultSingle(singleDevice,app.label)
    logMessage = defaults ? "$singleDevice scheduled for $defaults" : "$singleDevice has no scheduled default levels"

    // If there are defaults, then there's an active schedule so reschedule it (the results are corrupted below).
    // We could do this for the matching schedules within its own getDefaultLevel(), but that would
    // probably result in incremental schedules rescheduling themselves over and over again. And if we
    // excluded schedules from rescheduling, then daily schedules wouldn't do this.
    if(defaults) parent.rescheduleIncrementalSingle(singleDevice,app.label)

    // This does nothing in Time, or other app that has no levels, getOverrideLevels will immediately exit
    defaults = getOverrideLevels(defaults,appAction)
    logMessage += defaults ? ", controller overrides of $defaults": ", no controller overrides"

    // Set default levels, for level and temp, if no scheduled defaults (don't need to do for "resume")
    defaults = parent.getDefaultSingle(defaults,app.label)
    logMessage += ", so with generic defaults $defaults"

    if(checkLog(a="debug")) putLog(955,logMessage,a)
    parent.setLevelSingle(defaults,singleDevice,app.label)
    return
}

def checkLog(type = null){
    if(!state.logLevel) getLogLevel()
    switch(type) {
        case "error":
        if(state.logLevel > 0) return "error"
        break
        case "warn":
        if(state.logLevel > 1) return "warn"
        break
        case "info":
        if(state.logLevel > 2) return "info"
        break
        case "trace":
        if(state.logLevel > 3) return "trace"
        break
        case "debug":
        if(state.logLevel == 5) return "debug"
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,message = null,type = "trace"){
    logMessage = ""
    if(type == "error") logMessage += "<font color=\"red\">"
    if(type == "warn") logMessage += "<font color=\"brown\">"
    logMessage += "$app.label "
    if(lineNumber) logMessage += "(line $lineNumber) "
    if(message) logMessage += "-- $message"
    if(type == "error" || type == "warn") logMessage += "</font>"
    switch(type) {
        case "error":
        log.error(logMessage)
        return true
        case "warn":
        log.warn(logMessage)
        return true
        case "info":
        log.info(logMessage)
        return true
        case "trace":
        log.trace(logMessage)
        return true
        case "debug":
        log.debug(logMessage)
        return true
    }
    return
}
