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
*  Version: 0.1.02
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
							if(!humidityControlEnable){
								humidityControlStartDifference = null
								humidityControlDevice = null
							}
							varStartNumber = 0
							if(humidityControlDevice && humidityControlStartDifference) varStartNumber++
							if(humidityStartThreshold) varStartNumber++
							if(humidityIncreaseRate) varStartNumber++
							if(varStartNumber != 1){
								paragraph "<div style=\"background-color:BurlyWood\"><b> Conditions to start fan:</b></div>"
							} else {
								paragraph "<div style=\"background-color:BurlyWood\"><b> Condition to start fan:</b></div>"
							}
							if(!humidityControlEnable){
								input "humidityControlEnable", "bool", title: "Click to select control humidity sensor(s) to compare to.", submitOnChange:true
							} else {
								input "humidityControlEnable", "bool", title: "Click to <b>not</b> compare to control humidity sensor(s).", submitOnChange:true
							}
							if(humidityControlEnable){
								input "humidityControlDevice", "capability.relativeHumidityMeasurement", title: "Control humidity sensor(s)?", multiple: true, required: true, submitOnChange:true
								if(humidityControlDevice){
									// Check if multiple controls (if so, say we're averaging)
									numControls = humidityControlDevice.size()
									// Get current humidity level of control(s)
									if(humidityControlDevice){
										currentControlHumidity = averageHumidity(humidityControlDevice)
										if(numControls > 1){
											input "humidityControlStartDifference", "number", title: "Percent difference from the average of control devices $humidityControlDevice?", required: true, submitOnChange:true
										} else {
											input "humidityControlStartDifference", "number", title: "Percent difference from the control device $humidityControlDevice?", required: true, submitOnChange:true
										}
									} else {
										input "humidityControlStartDifference", "number", title: "Percent difference from control?", required: true, submitOnChange:true
									}
									// Build message with current levels
									varMessage = "<div style=\"background-color:AliceBlue\">$infoIcon This uses relative differences. For instance, the control sensor"
									if(numControls > 1) varMessage += "s"
									varMessage += " currently show"
									if(numControls == 1) varMessage += "s"
									varMessage += " $currentControlHumidity% humidity, "
									if(!humidityControlStartDifference) {
										varMessage += "so a value of 40 would require the primary sensor to be at " + ((100 - currentControlHumidity) * 40 / 100 + currentControlHumidity) + "% or more (rather than $currentControlHumidity + 40 = " + (currentControlHumidity + 40) + "%)."
									} else {
										varMessage += "so this requires the primary sensor to be at " + ((100 - currentControlHumidity) * humidityControlStartDifference / 100 + currentControlHumidity) + "% or more (rather than $currentControlHumidity + $humidityControlStartDifference = " + (currentControlHumidity + humidityControlStartDifference) + "%)."
									}
									varMessage += " Just FYI, primary sensor"
									if(numDevices > 1) {
										varMessage += "s current average is"
									} else {
										varMessage += " is currently"
									}
									varMessage += " $currentHumidity%.</div>" 
									paragraph varMessage
								}
							}
							if(humidityControlEnable && (!humidityControlDevice || !humidityControlStartDifference)){
								paragraph "<div style=\"background-color:BurlyWood\"> </div>"
							} else if(!humidityControlEnable || (humidityControlDevice && humidityControlStartDifference)){
        						input "humidityStartThreshold", "number", title: "Absolute humidity level? (Currently at $currentHumidity%.)", required: false, submitOnChange:true
/* ************************************************** */
/* TO-DO: Make increase based on time, rather than    */
/* polling rate.                                      */
/* ************************************************** */
								input "humidityIncreaseRate", "number", title: "Humidity increase?", required: false, submitOnChange:true
								paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Humidity increase is the difference between two sensor updates, so depends on polling rate.</div>"
								if(varStartNumber > 1) {
									if(!multiStartTrigger){
										input "multiStartTrigger", "bool", title: "<b>Requiring <i>any</i> one of the conditions.</b> Click to require all conditions.", submitOnChange:true
									
									} else if(multiStartTrigger){
										input "multiStartTrigger", "bool", title: "<b>Requiring <i>all</i> of the conditions.</b> Click to require any one of the conditions.", submitOnChange:true
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
										paragraph "<div style=\"background-color:AliceBlue\">$infoIcon $varMessage</div>"
									}
								}
								if(!humidityIncreaseRate && !humidityStartThreshold && !humidityControlStartDifference){
									paragraph "<div style=\"background-color:BurlyWood\"> </div>"
								} else if(humidityIncreaseRate || humidityStartThreshold || humidityControlStartDifference){
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
									if(humidityControlEnable && humidityControlDevice && humidityControlStartDifference){
										if(numControls > 1){
											input "humidityControlStopDifference", "number", title: "Percent difference from the average of control devices $humidityControlDevice?", default: humidityControlStartDifference, required: true, submitOnChange:true
										} else {
											input "humidityControlStopDifference", "number", title: "Percent difference from the control device $humidityControlDevice?", default: humidityControlStartDifference, required: true, submitOnChange:true
										}
									}
									if(humidityStartThreshold){
										input "humidityStopThreshold", "number", title: "Absolute humidity level?", required: false, submitOnChange:true
										paragraph "<div style=\"background-color:AliceBlue\">$infoIcon You <i>can</i> make this less than starting threshold of $humidityStartThreshold, but it's not recommended.</div>"
									}
									input "humidityStopDecrease", "number", title: "Percent above the starting humidity?", required: false, submitOnChange:true
									paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Enter zero for humidity to be completely normalized, but only with caution. If the ambiant humidity level rises, it may never reach the starting humidity.</div>"

									input "humidityStopMinutes", "number", title: "After minutes?", required: false, submitOnChange:true
									if(varStopNumber > 1){
										if(!multiStopTrigger){
											input "multiStopTrigger", "bool", title: "<b>Requiring <i>any</i> one of the conditions.</b> Click to require all conditions.", submitOnChange:true
										} else if(multiStopTrigger){
											input "multiStopTrigger", "bool", title: "<b>Requiring <i>all</i> of the conditions.</b> Click to require any one of the conditions.", submitOnChange:true
											varMessage = "Current humidity is " + Math.round(currentHumidity) +". The fan will turn off only if humidity is "
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
											paragraph "<div style=\"background-color:AliceBlue\">$infoIcon $varMessage</div>"
										}
									}
									
									
									if(humidityControlStopDifference || humidityStopThreshold || humidityStopDecrease){
										paragraph "<div style=\"background-color:BurlyWood\"> </div>"
										input "humidityWaitMinutes", "number", title: "Minutes to delay turning off (to prevent \"cycling\" on and off)?", required: false, submitOnChange:true
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

	subscribe(humidityDevice, "humidity", humidityHandler)
	// Uncomment this when add changeRate time frame, instead of using polling rate
	// Otherwise, it'll throw off all the state variables
	// subscribe(humidityControlDevice, "humidity", humidityHandler)
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

	logTrace("$app.label: function humidityHandler [lastHumidity = $state.lastHumidity]")
	logTrace("$app.label: function humidityHandler [lastHumidityDate = $state.lastHumidityDate]")
	logTrace("$app.label: function humidityHandler [currentHumidity = $state.currentHumidity]")
	logTrace("$app.label: function humidityHandler [currentHumidityDate = $state.currentHumidityDate]")
	logTrace("$app.label: function humidityHandler [startingHumidity = $state.startingHumidity]")
	logTrace("$app.label: function humidityHandler [humidityChangeRate = $state.humidityChangeRate]")
	logTrace("$app.label: function humidityHandler [automaticallyTurnedOn = $state.automaticallyTurnedOn]")
	fanIsOn = parent.multiStateOn(switches)
	turnFanOff = false
	
	// See if we should turn the fan OFF (if auto on)
	if(fanIsOn && state.automaticallyTurnedOn){
		// Check if control device is lower than humidityControlStopDifference
		if(humidityControlStopDifference && humidityControlDevice){
			percentThreshold = getRelativePercentage(state.controlHumidity,humidityControlStopDifference)
			if(state.currentHumidity <= percentThreshold  && !multiStopTrigger) {
				if(humidityWaitMinutes){
					logTrace("$app.label: function humidityHandler turning off (currentHumidity: $state.currentHumidity; humidityControlDevice: $controlHumidity; humidityControlStopDifference: $humidityControlStopDifference%; target: $percentThreshold)")
					multiOff()
					return
				} else {
					logTrace("$app.label: function humidityHandler scheduling off in $humidityWaitMinutes.")
					runIn(60 * humidityWaitMinutes.toInteger(), scheduleTurnOff)
					return
				}
			} else if(state.currentHumidity <= percentThreshold  && multiStopTrigger){
				turnFanOff = true
			} else if(state.currentHumidity > percentThreshold && multiStopTrigger){
				turnFanOff = false
			}
		}

		// Check if less than stop threshold
		if(humidityStopThreshold){
			if(state.currentHumidity <= humidityStopThreshold && !multiStopTrigger) {
				if(humidityWaitMinutes){
					logTrace("$app.label: function humidityHandler turning off (currentHumidity: $state.currentHumidity; humidityStopThreshold: $humidityStopThreshold)")
					multiOff()
					return
				} else {
					logTrace("$app.label: function humidityHandler scheduling off in $humidityWaitMinutes.")
					runIn(60 * humidityWaitMinutes.toInteger(), scheduleTurnOff)
					return
				}
			} else if(state.currentHumidity <= humidityStopThreshold && multiStopTrigger){
				turnFanOff = true
			} else if(state.currentHumidity > humidityStopThreshold && multiStopTrigger){
				turnFanOff = false
			}				
		}
		
		// Check if percent of starting
		if(humidityStopDecrease){
			percentThreshold = state.startingHumidity * humidityStopDecrease / 100 + state.startingHumidity
			if(state.currentHumidity <= percentThreshold && !multiStopTrigger){
				if(humidityWaitMinutes){
					logTrace("$app.label: function humidityHandler turning off (currentHumidity: $state.currentHumidity; startingHumidity = $state.startingHumidity; humidityStopDecrease = $humidityStopDecrease%; target = $percentThreshold)")
					multiOff()
					return
				} else {
					logTrace("$app.label: function humidityHandler scheduling off in $humidityWaitMinutes.")
					runIn(60 * humidityWaitMinutes.toInteger(), scheduleTurnOff)
					return
				}
			} else if(state.currentHumidity <= percentThreshold && multiStopTrigger){
				turnFanOff = true
			} else if(state.currentHumidity > percentThreshold && multiStopTrigger){
				turnFanOff = false
			}
		}

		// If no scheduled time, and turning off regardless, then turn off
		if((turnFanOff && multiStopTrigger) || (!humidityStopMinutes && turnFanOff && multiStopTrigger)){
			if(humidityWaitMinutes){
				logTrace("$app.label: function humidityHandler turning off (multiple conditions)")
				multiOff()
				return
			} else {
				logTrace("$app.label: function humidityHandler scheduling off in $humidityWaitMinutes.")
				runIn(60 * humidityWaitMinutes.toInteger(), scheduleTurnOff)
				return
			}

		// Check if need schedule and if so, set it
		// if time has already passed, ignore it
		// otherwise schedule it
		} else if(humidityStopMinutes && !state.scheduleStarted){
			time = now()
			stop = state.humidityStartTime + (humidityStopMinutes * 60000)
			// If schedule time has passed
			if(time > stop){
				if(turnFanOff || multiStopTrigger){
					if(humidityWaitMinutes){
						logTrace("$app.label: function humidityHandler turning off (multiple conditions)")
						multiOff()
						return
					} else {
						logTrace("$app.label: function humidityHandler scheduling off in $humidityWaitMinutes.")
						runIn(60 * humidityWaitMinutes.toInteger(), scheduleTurnOff)
						return
					}
				}
			} else {
				state.scheduleStarted = true
				logTrace("$app.label: function humidityHandler scheduling turn off (for $humidityStopMinutes minutes)")
				runIn(60 * humidityStopMinutes.toInteger(), scheduleTurnOff)
			}
		}
			


	// See if we should turn the fan OFF (if manual on)
	} else if(fanIsOn && state.automaticallyTurnedOn == false){
		turnFanOff = true
		if(humidityControlStopDifferenceManual && humidityControlStopDifference && humidityControlDevice){
			percentThreshold = getRelativePercentage(state.controlHumidity,humidityControlStopDifference)
			if(percentThreshold > state.currentHumidity) turnFanOff = false
		}
		if(humidityStopThresholdManual && humidityStopThreshold > state.currentHumidity) turnFanOff = false
		if(turnFanOff) {
			if(humidityWaitMinutes){
				logTrace("$app.label: function humidityHandler turning off (manually turned on)")
				multiOff()
				return
			} else {
				logTrace("$app.label: function humidityHandler scheduling off in $humidityWaitMinutes.")
				runIn(60 * humidityWaitMinutes.toInteger(), scheduleTurnOff)
				return
			}
		}
	}

	// If fan is on, skip turning it on
	// If only one of multiple switches is on, must have been manually turned on (or some manually turned off), so we'll just reset
	if(!fanIsOn){
		// Check if ALL conditions are true
		if(multiStartTrigger){
			turnFanOn = true
			// Compare it to the "control" device (checking for negative result)
			if(humidityControlDevice && humidityControlStartDifference){
				percentThreshold = getRelativePercentage(state.controlHumidity,humidityControlStartDifference)
				if(state.controlHumidity + humidityControlStartDifference > state.currentHumidity) {
					turnFanOn = false
					logTrace("$app.label: function humidityHandler (not turning on because control device + humidityControlStartDifference $humidityControlStartDifference [$percentThreshold] > current humidity $state.currentHumidity)")
				}
			}
			// Check if over the base threshold (checking for negative result)
			if(humidityStartThreshold &&  humidityStartThreshold > state.currentHumidity) {
				turnFanOn = false
				logTrace("$app.label: function humidityHandler (not turning on because humidityStartThreshold $humidityStartThreshold > current humidity $state.currentHumidity)")
			}
			// Check if amount increase from last update (checking for negative result)
			if(humidityIncreaseRate && humidityIncreaseRate > state.humidityChangeRate) {
				turnFanOn = false
				logTrace("$app.label: function humidityHandler (not turning on because humidityIncreaseRate $humidityIncreaseRate > humidityChangeRate $state.humidityChangeRate)")
			}
		// Check if ANY condition is true
		} else {
			turnFanOn = false
			// Compare it to the "control" device
			if(humidityControlDevice && humidityControlStartDifference){
				percentThreshold = getRelativePercentage(state.controlHumidity,humidityControlStartDifference)
				if(percentThreshold <= state.currentHumidity) {
					turnFanOn = true
					logTrace("$app.label: function humidityHandler (turning on - currentHumidity $humidityControlDevice.currentHumidity + humidityControlStartDifference $humidityControlStartDifference [$percentThreshold] <= currentHumidity $state.currentHumidity)")
				} else {
					logTrace("$app.label: function humidityHandler (not match for on - currentHumidity $humidityControlDevice.currentHumidity + humidityControlStartDifference $humidityControlStartDifference [$percentThreshold] <= currentHumidity $state.currentHumidity)")
				}
			}

			// Check if over the base threshold 
			if(humidityStartThreshold &&  humidityStartThreshold <= state.currentHumidity) {
				turnFanOn = true
				logTrace("$app.label: function humidityHandler (turning on - humidityStartThreshold $humidityStartThreshold <= currentHumidity $state.currentHumidity)")
			} else {
				logTrace("$app.label: function humidityHandler (not match for on - humidityStartThreshold $humidityStartThreshold > currentHumidity $state.currentHumidity)")
			}
			// Check if amount increase from last update
			if(humidityIncreaseRate && humidityIncreaseRate <= state.humidityChangeRate) {
				turnFanOn = true
				logTrace("$app.label: function humidityHandler (turning on - humidityIncreaseRate $humidityIncreaseRate  <= humidityChangeRate $state.humidityChangeRate)")
			} else {
				logTrace("$app.label: function humidityHandler (not match for on - humidityIncreaseRate $humidityIncreaseRate  > humidityChangeRate $state.humidityChangeRate)")
			}
		}

		// Turning on
		if(turnFanOn){
			if(humidityStopMinutes && humidityStopMinutes > 0){
				logTrace("$app.label: function humidityHandler scheduling turn off (for $humidityStopMinutes minutes)")
				runIn(60 * humidityStopMinutes.toInteger(), scheduleTurnOff)
			}
			state.automaticallyTurnedOn = true
			parent.multiOn(switches)
			state.humidityStartTime = now()
			state.startingHumidity = state.lastHumidity
			logTrace("$app.label: function humidityHandler exiting (turned on, humidity increase [startingHumidity = $state.startingHumidity])")
		}

		// If nothing is on, then we can exit
		if(!turnFanOn) {
			logTrace("$app.label: function humidityHandler exiting (no switches on to turn off)")
			return
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
		if(!state.automaticallyTurnedOn && humidityStopMinutesManual && humidityStopMinutes && humidityStopMinutes > 0) {
			logTrace("$app.label: function switchHandler scheduling fan off in $humidityStopMinutes minutes (switched turned on)")
			runIn(60 * humidityStopMinutes.toInteger(), multiOff)
			return
		}
	} else if(evt.value == "off") {
		logTrace("$app.label: function switchHandler exiting (switched turned off)")
		state.automaticallyTurnedOn = false
		logTrace("$app.label: function switchHandler setting automaticallyTurnedOn to false")
	}		   
}

def scheduleTurnOff() {
	logTrace("$app.label: scheduleTurnOff starting")

	state.scheduleStarted = false
	
	fanIsOn = parent.multiStateOn(switches)
	if(!fanIsOn) return

	turnFanOff = false
	
	// Check if control device is lower than humidityControlStopDifference
	if(humidityControlStopDifference && humidityControlDevice){
		percentThreshold = getRelativePercentage(state.controlHumidity,humidityControlStopDifference)
		if(percentThreshold >= state.currentHumidity && multiStopTrigger){
			turnFanOff = true
		} else if(percentThreshold < state.currentHumidity && multiStopTrigger){
			turnFanOff = false
		}
	}

	// Check if less than stop threshold
	if(humidityStopThreshold){
		if(humidityStopThreshold >= state.currentHumidity && multiStopTrigger){
			turnFanOff = true
		} else if(humidityStopThreshold < state.currentHumidity && multiStopTrigger){
			turnFanOff = false
		}				
	}

	// Check if percent of starting
	if(humidityStopDecrease){
		if(state.startingHumidity * humidityStopDecrease + state.startingHumidity >= state.currentHumidity && multiStopTrigger){
			turnFanOff = true
		} else if(state.startingHumidity * humidityStopDecrease + state.startingHumidity < state.currentHumidity && multiStopTrigger){
			turnFanOff = false
		}
	}

	if(!multiStopTrigger || turnFanOff){
		parent.multiOff(switches)
		logTrace("$app.label: function scheduleTurnOff turning off")
        state.automaticallyTurnedOn = false
	}
}

def multiOff() {
	logTrace("$app.label: multiOff starting")
	if(parent.multiStateOn(switches)){
		unschedule()
		parent.multiOff(switches)
		
		logTrace("$app.label: function multiOff setting automaticallyTurnedOn to false")
        state.automaticallyTurnedOn = false
	}
	logTrace("$app.label: multiOff exiting")
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

def logTrace(message) {
	//log.trace message
}
