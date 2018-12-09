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
*  Version: 0.3.21
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
	infoIcon = "<img src=\"http://files.softicons.com/download/system-icons/windows-8-metro-invert-icons-by-dakirby309/ico/Folders%20&%20OS/Info.ico\" width=20 height=20>"

	if(timeStart && !timeStartSunrise && !timeStartsunset){
		varStartTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format("h:mm a", location.timeZone)
	} else if(timeStartSunrise || timeStartsunset){
		if(timeStartOffset && timeStartOffsetNegative){
			varStartTime = "$timeStartOffset minutes after"
		} else if(timeStartOffset){
			varStartTime = "$timeStartOffset minutes before"
		}
		if(varStartTime) {
			if(timeStartSunrise){
				varStartTime = "$varStartTime sunrise"
			} else if(timeStartsunset){
				varStartTime = "$varStartTime sunset"
			}
		} else {
			if(timeStartSunrise){
				varStartTime = "sunrise"
			} else if(timeStartsunset){
				varStartTime = "sunset"
			}
		}	
	}

	if(timeStop && !timeStopSunrise && !timeStopsunset){
		varStopTime = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format("h:mm a", location.timeZone)
	} else if(timeStopSunrise || timeStopsunset){
		if(timeStopOffset && timeStopOffsetNegative){
			varStopTime = "$timeStopOffset minutes after"
		} else if(timeStopOffset){
			varStopTime = "$timeStopOffset minutes before"
		}
		if(varStopTime){
			if(timeStopSunrise){
				varStopTime = "$varStopTime sunrise"
			} else if(timeStopsunset){
				varStopTime = "$varStopTime sunset"
			}
		} else {
			if(timeStopSunrise){
				varStopTime = "sunrise"
			} else if(timeStopsunset){
				varStopTime = "sunset"
			}
		}
	}

	// build message
	if(timeDevice && !varStartTime){
		message = "With $timeDevice"
	} else if(varStartTime){
		message = "At $varStartTime"
		if(timeDays) message = "$message on $timeDays"
		if(ifMode) message = "$message, if mode is $ifMode"
		if(timeModeChangeOn) {
			if(ifMode) message = "$message, then set mode to $timeModeChangeOn"
			if(!ifMode) message = "$message, set mode to $timeModeChangeOn"
		}
		if(timeOn == "Turn On") {
			if(timeModeChangeOn) message = "$message, and turn on $timeDevice"
			if(!timeModeChangeOn) message = "$message, turn on  $timeDevice"
		} else if(!timeOn){
			if(timeModeChangeOn) message = "$message, and if $timeDevice is on"
			if(!timeModeChangeOn) message = "$message, if $timeDevice is on"
		}
		if(timeLevelOn){
			if(timeOn) message = "$message to level $timeLevelOn"
			if(!timeOn) message = "$message, set level to $timeLevelOn"
		}
		if(timeTempOn){
			if(timeOn) message = "$message with temperature $timeTempOn"
			if(!timeOn && timeLevelOn && !timeHueOn && !timeSatOn) message = "$message and temperature to $timeTempOn"
			if(!timeOn && timeLevelOn && (timeHueOn || timeSatOn)) message = "$message, temperature to $timeTempOn"
			if(!timeOn && !timeLevelOn) message = "$message, then set temperature to $timeTempOn"
		}
		if(timeHueOn){
			if(timeOn && (timeLevelOn || timeTempOn)) message = "$message, hue $timeHueOn"
			if(timeOn && !timeLevelOn && !timeTempOn && timeSatOn) message = "$message, hue $timeHueOn"
			if(timeOn && !timeLevelOn && !timeTempOn && !timeSatOn) message = "$message, and hue $timeHueOn"
			if(!timeOn && (timeLevelOn || timeTempOn)) message = "$message, hue $timeTempOn"
			if(!timeOn && !timeLevelOn && !timeTempOn) message = "$message, then set hue to $timeHueOn"
		}
		if(timeSatOn){
			if(timeLevelOn || timeTempOn || timeHueOn) message = "$message, and saturation $timeHueOn"
			if(!timeLevelOn && !timeTempOn && !timeSatOn) message = "$message, then set saturation to $timeHueOn"
		}
		if(varStopTime && (timeLevelOff || timeTempOff || timeHueOff || timeSatOff || timeModeChangeOff)){
			message = "$message, then"
			if(timeLevelOff && !timeTempOff && !timeHueOff && !timeSatOff){
				if(timeLevelOff < timeLevelOn) message = "$message dim to $timeLevelOff until $varStopTime"
				if(timeLevelOff > timeLevelOn) message = "$message brigten to $timeLevelOff until $varStopTime"
			} else if(timeLevelOff || timeTempOff || timeHueOff || timeSatOff){
				message = "$message change "
				if(timeLevelOff) message = "$message level to $timeLevelOff"
				if(timeTempOff){
					if(!timeLevelOff) message = "$message temperature to $timeTempOff"
					if(timeLevelOff && !timeHueOff && !timeSatOff) message = "$message and temperature to $timeTempOff"
					if(timeLevelOff && (timeHueOff || timeSatOff)) message = "$message, temperature to $timeTempOff"
				}
				if(timeHueOff){
					if(!timeLevelOff && !timeTempOff) message = "$message hue to $timeHueOff"
					if((timeLevelOff || timeTempOff) && timeSatOff) message = "$message and hue to $timeHueOff"
					if((timeLevelOff || timeTempOff) && !timeSatOff) message = "$message, hue to $timeHueOff"
				}
				message = "$message until $varStopTime"
			}
			if(timeStopOff && (timeLevelOff || timeTempOff || timeHueOff || timeSatOff) && !timeModeChangeOff) message = "$message, and $timeStopOff"
			if(timeStopOff && (timeLevelOff || timeTempOff || timeHueOff || timeSatOff) && timeModeChangeOff) message = "$message, $timeStopOff"
			if(timeStopOff && !timeLevelOff && !timeTempOff && !timeHueOff && !timeSatOff) message = "$message, then $timeStopOff"
			if(timeModeChangeOff && (timeLevelOff || timeTempOff || timeHueOff || timeSatOff)) message = "$message, and set mode to $timeModeChangeOff"
			if(timeModeChangeOff && !timeLevelOff && !timeTempOff && !timeHueOff && !timeSatOff) message = "$message, then set mode to $timeModeChangeOff"
		}
		
				
				
				
// toggle and time off
	}
	if(!varStartTime || $timeDevice || (varStartTime && !timeLevelOn && !timeHueOn && !timeSatOn)){
		message = "$message ..."
	} else {
		message = "$message."
	}

    page(name: "setup", install: true, uninstall: true) {
        section() {

				// Set disable all
				if(timeDisableAll) {
					state.timeDisable = true
				} else {
					state.timeDisable = false
				}

				// If all disabled, force reenable
				if(state.timeDisable){
					input "timeDisableAll", "bool", title: "<b>All schedules are disabled.</b> Reenable?", defaultValue: false, submitOnChange:true
				// If schedule disabled, show only basic options
				} else if(timeDisable){
					paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this schedule:</b></div>"
					label title: "Schedule name?", required: true, submitOnChange:true
					if(app.label){
						paragraph "<div style=\"background-color:BurlyWood\"><b> Select which devices to schedule:</b></div>"
						input "timeDevice", "capability.switch", title: "Device(s)?", multiple: true, required: true, submitOnChange:true
						input "timeDisable", "bool", title: "<b><font color=\"#000099\">Schedule is disabled.</font></b> Reenable it?", submitOnChange:true
						// If no devices selected, don't show anything else (except disabling)
						if(timeDevice){
							paragraph "<div style=\"background-color:BurlyWood\"><b> Select time or mode (Optional):</b></div>"
							if(!timeStartSunrise && !timeStartSet){
								if(timeStop){
									input "timeStart", "time", title: "Between start time", required: false, width: 6, submitOnChange:true
								} else {
									input "timeStart", "time", title: "Between start time (Optional)", required: false, width: 6, submitOnChange:true
								}
							} else if(timeStartSunrise) {
								paragraph "Between sunrise", width: 6
							} else if(timeStartSet){
								paragraph "Between sunset", width: 6
							}
							if(!timeStopSunrise && !timeStopsunset){
								if(timeStart){
									input "timeStop", "time", title: "and stop time", required: false, width: 6, submitOnChange:true
								} else {
									input "timeStop", "time", title: "and stop time (Optional)", required: false, width: 6, submitOnChange:true
								}
							} else if(timeStopSunrise){
								paragraph "and sunrise", width: 6
							} else if(timeStopsunset){
								paragraph "and sunset", width: 6
							}
							if(!timeStartsunset){
								input "timeStartSunrise", "bool", title: "Start at sunrise?", width: 6, submitOnChange:true
							} else {
								paragraph " ", width: 6
							}
							if(!timeStopsunset) {
								input "timeStopSunrise", "bool", title: "Stop at sunrise?", width: 6, submitOnChange:true
							} else {
								paragraph " ", width: 6
							}
							if(!timeStartSunrise){
								input "timeStartsunset", "bool", title: "Start at sunset?", width: 6, submitOnChange:true
							} else {
								paragraph " ", width: 6
							}
							if(!timeStopSunrise){
								input "timeStopsunset", "bool", title: "Stop at sunset?", width: 6, submitOnChange:true
							} else {
								paragraph " ", width: 6
							}
							input "timeDays", "enum", title: "On these days (Optional):", required: false, multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"], submitOnChange:true
						}
					}
					
					input "timeDisableAll", "bool", title: "Disable <b>ALL</b> schedules?", defaultValue: false, submitOnChange:true
				// If not disabled, show all options
				} else {
					paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this schedule:</b></div>"
					label title: "Schedule name?", required: true, submitOnChange:true
					if(!app.label){
						paragraph "<div style=\"background-color:BurlyWood\"> </div>"
					} else if(app.label){
						paragraph "<div style=\"background-color:BurlyWood\"><b> Select which devices to schedule:</b></div>"
						input "timeDevice", "capability.switch", title: "Device(s)?", multiple: true, required: true, submitOnChange:true
						input "timeDisable", "bool", title: "Disable this schedule?", defaultValue: false, submitOnChange:true
						if(!timeDevice){
							paragraph "<div style=\"background-color:BurlyWood\"> </div>"
						// If no devices selected, don't show anything else (except disabling)
						} else if(timeDevice){
							paragraph "<div style=\"background-color:BurlyWood\"><b> Enter time:</b></div>"
							if(!timeStartSunrise && !timeStartsunset){
								if(timeStop){
									paragraph "Between start time", width: 6
									//input "timeStart", "time", title: "Between start time", required: false, width: 6, submitOnChange:true
								} else {
									paragraph "Between start time (optional)", width: 6
									//input "timeStart", "time", title: "Between start time (Optional)", required: false, width: 6, submitOnChange:true
								}
							} else if(timeStartSunrise) {
								if(timeStartOffsetNegative) {
									input "timeStartOffsetNegative", "bool", title: "Minutes <b>after</b> sunrise (optional)", required: false, width: 6, submitOnChange:true
								} else {
									input "timeStartOffsetNegative", "bool", title: "Minutes <b>before</b> sunrise (optional)", required: false, width: 6, submitOnChange:true
								}
								//input "timeStartOffset", "number", title: "Between minutes before or after sunrise (optional)", required: false, width: 6, submitOnChange:true
							} else if(timeStartsunset){
								if(timeStartOffsetNegative) {
									input "timeStartOffsetNegative", "bool", title: "Minutes <b>after</b> sunset (optional)", required: false, width: 6, submitOnChange:true
								} else {
									input "timeStartOffsetNegative", "bool", title: "Minutes <b>before</b> sunset (optional)", required: false, width: 6, submitOnChange:true
								}
								//input "timeStartOffset", "number", title: "Between minutes before or after sunset (optional)", required: false, width: 6, submitOnChange:true
							}
							
							if(!timeStopSunrise && !timeStopsunset){
								if(timeStart){
									paragraph "and stop time", width: 6
									//input "timeStop", "time", title: "and stop time", required: false, width: 6, submitOnChange:true
								} else {
									paragraph "and stop time (optional)", width: 6
									//input "timeStop", "time", title: "and stop time (Optional)", required: false, width: 6, submitOnChange:true
								}
							} else if(timeStopSunrise){
								if(timeStopOffsetNegative) {
									input "timeStopOffsetNegative", "bool", title: "and minutes <b>after</b> sunrise (optional)", required: false, width: 6, submitOnChange:true
								} else {
									input "timeStopOffsetNegative", "bool", title: "and minutes <b>before</b> sunrise (optional)", required: false, width: 6, submitOnChange:true
								}
								//input "timeStopOffset", "number", title: "and minutes before or after sunrise (optional)", required: false, width: 6, submitOnChange:true
							} else if(timeStopsunset){
								if(timeStopOffsetNegative) {
									input "timeStopOffsetNegative", "bool", title: "and minutes <b>after</b> sunset (optional)", required: false, width: 6, submitOnChange:true
								} else {
									input "timeStopOffsetNegative", "bool", title: "and minutes <b>before</b> sunset (optional)", required: false, width: 6, submitOnChange:true
								}
								//input "timeStopOffset", "number", title: "and minutes before or after sunset (optional)", required: false, width: 6, submitOnChange:true
							}
							if(!timeStartSunrise && !timeStartsunset){
								input "timeStart", "time", title: "", required: false, width: 6, submitOnChange:true
							} else if(timeStartSunrise || timeStartsunset){
								input "timeStartOffset", "number", title: "", required: false, width: 6, submitOnChange:true
							}
							
							if(!timeStopSunrise && !timeStopsunset){
								input "timeStop", "time", title: "", required: false, width: 6, submitOnChange:true
							} else if(timeStopSunrise || timeStopsunset){
								input "timeStopOffset", "number", title: "", required: false, width: 6, submitOnChange:true
							}
							
							if(!timeStartsunset){
								input "timeStartSunrise", "bool", title: "Start at sunrise?", width: 6, submitOnChange:true
							} else {
								paragraph " ", width: 6
							}
							if(!timeStopsunset) {
								input "timeStopSunrise", "bool", title: "Stop at sunrise?", width: 6, submitOnChange:true
							} else {
								paragraph " ", width: 6
							}
							if(!timeStartSunrise){
								input "timeStartsunset", "bool", title: "Start at sunset?", width: 6, submitOnChange:true
							} else {
								paragraph " ", width: 6
							}
							if(!timeStopSunrise){
								input "timeStopsunset", "bool", title: "Stop at sunset?", width: 6, submitOnChange:true
							} else {
								paragraph " ", width: 6
							}
							input "timeDays", "enum", title: "On these days (Optional):", required: false, multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"], submitOnChange:true
							if(!timeStart && !timeStartSunrise && !timeStartsunset) {
								paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				
							// If no start time, don't show any start or stop options
							} else if(timeStart || timeStartSunrise || timeStartsunset){
								// Start On/Off/Toggle
								paragraph "<div style=\"background-color:BurlyWood\"><b> Select whether to turn on or off:</b></div>"
								if(!timeOnOffDisable) {
									input "timeOnOffDisable", "bool", title: "<b>Don't turn on or off</b> (leave them as-is). Click to continue setting level, colors and mode.", submitOnChange:true
									input "timeOn", "enum", title: "Turn on or off devices at $varStartTime? (Optional)", multiple: false, required: false, width: 6, options: ["Turn On", "Turn Off", "Toggle"], submitOnChange:true
								} else {
									input "timeOnOffDisable", "bool", title: "<b>Don't turn on or off</b> (leave them as-is).", submitOnChange:true
								}

								// Stop On/Off/Toggle: Only show if Stop Time entered
								if(!timeOnOffDisable) {
									if(timeStop || timeStopSunrise || timeStopsunset){
										input "timeOff", "enum", title: "Turn on or off devices at $varStopTime? (Optional)", multiple: false, required: false, width: 6, options: ["Turn On", "Turn Off", "Toggle"]
									} else {
										paragraph "Set stop time for options", width: 6
									}
								}

								if(!timeOnOffDisable && !timeOn && !timeOff){
									paragraph "<div style=\"background-color:BurlyWood\"> </div>"
								} else if(timeOnOffDisable || timeOn || timeOff){
									if(timeStop || timeStopSunrise || timeStopsunset){
										paragraph "<div style=\"background-color:BurlyWood\"><b> Enter beginning and ending brightness:</b></div>"
									} else {
										paragraph "<div style=\"background-color:BurlyWood\"><b> Enter default brightness for $varStartTime:</b></div>"
									}
									// Start Level
									if(timeLevelDisable){
										input "timeLevelDisable", "bool", title: "<b>Don't change brightness.</b>", submitOnChange:true
									} else if(!timeLevelDisable){
										input "timeLevelDisable", "bool", title: "<b>Don't change brightness.</b> Click to continue setting colors and mode.", submitOnChange:true
										if(timeStop || timeStopSunrise || timeStopsunset){
											input "timeLevelOn", "number", title: "Beginning brightness at $varStartTime? (Optional: 1-100; Default 100)", required: false, width: 6, submitOnChange:true
										} else {
											input "timeLevelOn", "number", title: "At $varStartTime, default brightness? (Optional: 1-100; Default 100)", required: false, width: 6, submitOnChange:true
										}

										// Stop Level: Only show if Stop Time entered
										if(timeStop || timeStopSunrise || timeStopsunset){
											input "timeLevelOff", "number", title: "until $varStopTime, change brightness to? (Optional: 1-100)", required: false, width: 6, submitOnChange:true
										} else {
											paragraph "Set stop time for options", width: 6
										}
										if(timeLevelOff){
											if(!timeLevelPico){
												input "timeLevelPico", "bool", title: "<b>Not allowing manual override.</b> Click to enable.", submitOnChange:true
												paragraph "<div style=\"background-color:AliceBlue\">$infoIcon This prevents a Pico or MagicCube from overriding an ongoing schedule.</div>"
											} else {
												input "timeLevelPico", "bool", title: "<b>Allowing manual override.</b> Click to disable.", submitOnChange:true
												paragraph "<div style=\"background-color:AliceBlue\">$infoIcon This allows a Pico or MagicCube to override and suspend an ongoing schedule.</div>"
											}
											input "timeLevelIfLower", "enum", title: "Don't change Level if light is already dimmer or brighter? (Optional)", multiple: false, required: false, options: ["Lower":"Brighter", "Higher":"Dimmer"], submitOnChange:true
											if(timeLevelOff){
												if(timeLevelIfLower == "Lower"){
													paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Determines whether a schedule will begin if the light is brighter. It can also be used in conjunction with Manual Override, to determine if the schedule should resume if it catches up with level the light is set at.</div>"
												} else {
													paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Determines whether a schedule will begin if the light is dimmer. It can also be used in conjunction with Manual Override, to determine if the schedule should resume if it catches up with level the light is set at.</div>"
												}
											} else {
												if(timeLevelIfLower == "Lower"){
													paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Determines whether a schedule will begin if the light is brighter. Note: It can also be used in conjunction with Manual Override.</div>"
												} else {
													paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Determines whether a schedule will begin if the light is dimmer. Note: It can also be used in conjunction with Manual Override.</div>"
												}
											}
										}
										
									}
									if(!timeLevelOn && !timeLevelDisable){
										paragraph "<div style=\"background-color:BurlyWood\"> </div>"
									} else if(timeLevelOn || timeLevelDisable){
										if(timeStop || timeStopSunrise || timeStopsunset){
											paragraph "<div style=\"background-color:BurlyWood\"><b> Enter beginning and ending temperature color:</b></div>"
										} else {
											paragraph "<div style=\"background-color:BurlyWood\"><b> Enter default temperature color for $varStartTime:</b></div>"
										}
										if(!timeTempDisable){
											input "timeTempDisable", "bool", title: "<b>Don't change temperature color.</b> Click to continue setting colors and mode.", submitOnChange:true
										} else {
											input "timeTempDisable", "bool", title: "<b>Don't change temperature color.</b>", submitOnChange:true
										}
											
										paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Temperature color is a range from about 2200 to about 4500, where lower numbers are more \"warm\" and orange, and higher numbers are more \"cool\" and blue.</div>"
										if(!timeTempDisable){
											// Start Temp
											if(timeStop || timeStopSunrise || timeStopsunset){
												input "timeTempOn", "number", title: "Beginning temperature at $varStartTime? (Optional, default 3400)", required: false, width: 6, submitOnChange:true
											} else {
												input "timeTempOn", "number", title: "At $varStartTime, default temperature? (Optional, default 3400)", required: false, width: 6, submitOnChange:true
											}

											// Stop Level: Only show if Stop Time entered
											if(timeStop || timeStopSunrise || timeStopsunset){
												input "timeTempOff", "number", title: "until $varStopTime, change temperature to? (Optional: 2200-4500)", required: false, width: 6
											} else {
												paragraph "Set stop time for options", width: 6
											}
											
											//input "timeTempIfLower", "enum", title: "Don't change temperature color if light is already warmer or cooler? (Optional)", multiple: false, required: false, options: ["Lower":"Warmer", "Higher":"Cooler"]
										}
/* ************************************************** */
/* To-DO - Use color picker instead of hue and sat.   */
/* Only for beginning color - need hue and sat to     */
/* know how to change it over time.                   */
/* ************************************************** */
										if(!timeTempOn && !timeTempDisable){
											paragraph "<div style=\"background-color:BurlyWood\"> </div>"
										} else if(timeTempOn || timeTempDisable){
											if(timeStop || timeStopSunrise || timeStopsunset){
												paragraph "<div style=\"background-color:BurlyWood\"><b> Enter beginning and ending color hue and saturation:</b></div>"
											} else {
												paragraph "<div style=\"background-color:BurlyWood\"><b> Enter default color hue and saturation for $varStartTime:</b></div>"
											}
											
											if(timeHueDisable){
												input "timeHueDisable", "bool", title: "<b>Don't change color.</b>", submitOnChange:true
												paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Color change allows setting hue and saturation for custom colors.</div>"
											} else if(!timeHueDisable){
												input "timeHueDisable", "bool", title: "<b>Don't change color.</b>", submitOnChange:true
												paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Color hue is a \"wheel\" of colors starting at red (1), and going through green (33) and blue (66), then back to red again (100). Color saturation is a range from 1 to 100 of \"amount\" of hue color, from lighter colors to deeper colors. If hue is set, saturation is required.</div>"
												// Start Hue
												if(timeStop || timeStopSunrise || timeStopsunset){
													input "timeHueOn", "number", title: "Beginning hue at $varStartTime? (Optional)", required: false, width: 6, submitOnChange:true
												} else {
													input "timeHueOn", "number", title: "At $varStartTime, default hue? (Optional)", required: false, width: 6, submitOnChange:true
												}

												// Stop Level: Only show if Stop Time entered
												if(timeStop || timeStopSunrise || timeStopSunset){
													input "timeHueOff", "number", title: "until $varStopTime, change hue to? (Optional)", required: false, width: 6, submitOnChange:true
												} else {
													paragraph "Set stop time for options", width: 6
												}

												// Start Sat
												input "timeSatOn", "number", title: "Saturation?", required: false, width: 6, submitOnChange:true

												// Stop Saturation: Only show if Stop Time entered
												if(timeStop || timeStopSunrise || timeStopSunset){
													input "timeSatOff", "number", title: "Saturation?", required: false, width: 6
												} else {
													paragraph "", width: 6
												}
											}

											// If color entry is disable, or not entered correcly
											if(!timeHueDisable && !timeSatOn && !timeHueOn || ((timeSatOff && !timeHueOff) && (timeHueOff && !timeSatOff))){
												paragraph "<div style=\"background-color:BurlyWood\"> </div>"
											} else {
												if(timeStop || timeStopSunrise || timeStopSunset){
													paragraph "<div style=\"background-color:BurlyWood\"><b> Change Mode at $varStartTime and/or $varTimeStop.</b></div>"
												} else {
													paragraph "<div style=\"background-color:BurlyWood\"><b> Change Mode at $varStartTime.</b></div>"
												}

												// Change Mode on Start
												input "timeModeChangeOn", "mode", title: "<b>At $varStartTime, change Mode to?</b> (Optional)", required: false, width: 6, submitOnChange:true

												// Change Mode on Stop: Only show if Time Stop entered
												if(timeStop || timeStopSunrise || timeStopSunset){
													input "timeModeChangeOff", "mode", title: "<b>At $varStopTime, change Mode to?</b> (Optional)", required: false, width: 6, submitOnChange:true
												} else {
													paragraph "", width: 6
												}
												
												// Start only if Mode
												input "ifMode", "mode", title: "<b>Only if Mode is already:</b> (Optional)", required: false, width: 12
												paragraph "<div style=\"background-color:LightCyan\"><b> Click \"Done\" to continue.</b></div>"
											}
										}
									}
									paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Options can be combined. To have a default brightness of 50% after 9pm, set start time and start level (but do not have turn on). To have device turn on at 7am and gradually brighten for a half hour from 1% to 100%, set start time of 7am, stop time of 7:30am, and at start time turn on with level of 1, and a stop time of 7:30a, with a level of 100.</div>"
								}
							}
						}
					}
					
					paragraph "<div style=\"background-color:AliceBlue\">$infoIcon $message</div>"
				input "timeDisableAll", "bool", title: "Disable <b>ALL</b> schedules?", defaultValue: false, submitOnChange:true
				}
			
		}
    }
}


def installed() {
	varStartTime = Math.abs(timeStartOffest)
	logTrace("$app.label: installed")
	if(app.getLabel().length() < 7)  app.updateLabel("Time - " + app.getLabel())
	if(app.getLabel().substring(0,7) != "Time - ") app.updateLabel("Time - " + app.getLabel())
	initialize()
}

def updated() {
	logTrace("$app.label: updated")
	initialize()
}

def initialize() {
	logTrace("$app.label: initializing")
	if(app.getLabel().substring(0,7) != "Time - ") app.updateLabel("Time - " + app.getLabel())
	def appId = app.getId()
	logTrace("$app.label: function initialize unschedule all")
	unschedule()
	if(timeDisableAll || timeDisable) {
		if(timeDisableAll) state.timeDisable = true
		logTrace("$app.label (540): function exiting (schedule disabled)")
	} else {
		state.timeDisable = false
		logTrace("$app.label (543): function passing to initializeSchedules")
		initializeSchedules()
	}
	logTrace("$app.label: initialized")
}

def dimSpeed(){
	logTrace("$app.label (550): function dimSpeed started")
	if(settings.multiplier != null){
		logTrace("$app.label (552): function dimSpeed returning $settings.multiplier (user defined dimSpeed)")
		return settings.multiplier
	}else{
		logTrace("$app.label (555): function dimSpeed returning 1.2 (default dimSpeed)")
		return 1.2
	}
}

def getDefaultLevel(device){
	logTrace("$app.label (561): function getDefaultLevel started  [device: $device]")
	// Set map with fake values
	defaults=[level:'Null',temp:'Null',hue:'Null',sat:'Null']

	// If no device match, return null
	timeDevice.findAll( {it.id == device.id} ).each {
		logTrace("$app.label (567): [device: $device] function getDefaultLevel matched device $device and $it")
		match = true
	}
	if(!match) {
		logTrace("$app.label (571) [$device]: function getDefaultLevel returning null (no matching device)")
		return defaults
	}

	// if no start time
	if(!timeStart && !timeStartSunrise && !timeStartSunset){
		logTrace("$app.label (577): [device: $device] function getDefaultLevel returning null (no start time)")
		return defaults
	}

	// if no start levels
	if(!timeLevelOn && !timeTempOn && !timeHueOn && !timeSatOn){
		logTrace("$app.label (583): [device: $device] function getDefaultLevel returning null (no start levels)")
		return defaults
	}

	// Set timeStart and timeStop, if sunrise or sunset
	if(timeStartSunrise) timeStart = parent.getSunrise(timeStartOffset,timeStartOffsetNegative)
	if(timeStartSunset) timeStart = parent.getSunset(timeStartOffset,timeStartOffsetNegative)
	if(timeStopSunrise) timeStop = parent.getSunrise(timeStopOffset,timeStopOffsetNegative)
	if(timeStopSunset) timeStop = parent.getSunset(timeStopOffset,timeStopOffsetNegative)

	// if not between start and stop time
	if(timeStop && !parent.timeBetween(timeStart, timeStop)) {
		logTrace("$app.label (599) [$device]: function getDefaultLevel returning null (not between start $timeStart and stop $timeStop)")
		return defaults
	}

	// If disabled, return null
	if(timeDisable || state.timeDisableAll) {
		logTrace("$app.label (605) [$device]: function getDefaultLevel returning null (schedule disabled)")
		return defaults
	}

	// If mode set and node doesn't match, return null
	if(ifMode){
		if(location.mode != ifMode) {
			logTrace("$app.label (612) [$device]: function getDefaultLevel returning null (mode $ifMode doesn't match)")
			return defaults
		}
	}

	// If not correct day, return null
	if(timeDays && !parent.todayInDayList(timeDays)) {
		logTrace("$app.label (619) [$device]: function getDefaultLevel returning null (no scheduled day)")
		return defaults
	}

	// Get current level
	currentLevel = device.currentLevel
	currentTemp = device.currentColorTemperature
	currentHue = device.currentHue
	currentSat = device.currentSaturation

	// If no stop time, return start level
	if(!timeStop) {
		if(timeLevelOn){
			defaults = [level: timeLevelOn]
			// If start level is too dim, and set not to dim, return current level
			if(timeLevelIfLower){
				if(timeLevelIfLower == "Lower"){
					if(parent.stateOn(device) && currentLevel < timeLevelOn) defaults = [level: currentLevel]
				// If start level is too bright, and set not to brighten, return current level
				} else if(timeLevelIfLower == "Higher"){
					if(parent.stateOn(device) && currentLevel > timeLevelOn) defaults = [level: currentLevel]
				}
			}
		}

		if(timeTempOn){
			defaults = [temp: timeTempOn]
			// If start temp is too low, and set not to go lower, return current level
			if(timeTempIfLower){
				if(timeTempIfLower == "Lower"){
					if(parent.stateOn(device) && currentTemp < timeTempOn) defaults = [temp: currentTemp]
				// If start temp is too high, and set not to go higher, return current level
				} else if(timeTempIfLower == "Higher"){
					if(parent.stateOn(device) && currentTemp > timeTempOn) defaults = [temp: currentTemp]
				}
			}
		}
		if(timeHueOn) defaults = [hue: timeHueOn]
		if(timeSatOn) defaults = [sat: timeSatOn]
		logTrace("$app.label (658) [$device]: function getDefaultLevel returning $defaults")
		return defaults
	}

	// If there's a stop time and stop level, and after start time
	if(timeStart && timeStop){
		// If timeStop before timeStart, add a day
		if(timeToday(timeStop, location.timeZone).time < timeToday(timeStart, location.timeZone).time) {
			newTimeStop = parent.getTomorrow(timeStop)
		} else {
			newTimeStop = timeStop
		}
	
		// Calculate proportion of time already passed from start time to endtime
		hours1 = Date.parse("yyyy-MM-dd'T'HH:mm:ss", newTimeStop).format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
		minutes1 = Date.parse("yyyy-MM-dd'T'HH:mm:ss", newTimeStop).format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
		seconds1 = Date.parse("yyyy-MM-dd'T'HH:mm:ss", newTimeStop).format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
		hours2 = new Date().format('HH').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
		minutes2 = new Date().format('mm').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
		seconds2 = new Date().format('ss').toInteger() - Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('ss').toInteger()
		// Calculate new level
		if(timeLevelOff && timeLevelOn) {
			newLevel = (timeLevelOff - timeLevelOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + timeLevelOn as int
		} else if(timeLevelOn) {
			newLevel = timeLevelOn
		}
		if(timeTempOff && timeTempOn) {
			newTemp = (timeTempOff - timeTempOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + timeTempOn as int
		} else if(timeTempOn){
			newTemp = timeTempOn
		}

		if(timeHueOff && timeHueOn) {
			newHue = (timeHueOff - timeHueOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + timeHueOn as int
		} else if(timeHueOn){
			newHue = timeHueOn
		}
		if(timeSatOff && timeSatOn) {
			newSat = (timeSatOff - timeSatOn) * ((seconds2 + minutes2 * 60 + hours2 * 60 * 60) / (seconds1 + minutes1 * 60 + hours1 * 60 * 60)) + timeSatOn as int
		} else if (timeSatOn){
			newSat = timeSatOn
		}

		if(newLevel && defaults.level == "Null"){
			// If new level is too dim, and set not to dim, return current level
			if(timeLevelIfLower){
				if(timeLevelIfLower == "Lower"){
					logTrace("$app.label (705) [$device]: function getDefaultLevel using current level (already dimmer)")
					if(parent.stateOn(device) && currentLevel > newLevel) defaults.put("level",currentLevel)
				}
				// If new level is too bright, and set not to brighten, return current level
				if(timeLevelIfLower == "Higher"){
					logTrace("$app.label (710) [$device]: function getDefaultLevel using current level (already brighter)")
					if(parent.stateOn(device) && currentLevel < newLevel) defaults.put("level",currentLevel)
				}
			}
		}

		if(defaults.level == "Null" && newLevel) defaults.put("level",newLevel)
		if(parent.isFan(device) && defaults.level != "Null") defaults.put("level",roundFanLevel(defaults.level))

		// Set temp
		if(newTemp && defaults.temp == "Null"){
			// If new level is too low, and set not to go lower, return current level
			if(timeTempIfLower){
				if(timeTempIfLower == "Lower"){
					if(parent.stateOn(device) && currentTemp < newTemp) defaults.put("temp",currentTemp)
				}
				// If new level is too high, and set not to go higher, return current level
				if(timeTempIfLower == "Higher"){
					if(parent.stateOn(device) && currentTemp > newTemp) defaults.put("temp",currentTemp)
				}
			}
		}
		if(defaults.temp == "Null" && newTemp) defaults.put("temp",newTemp)
		// Set hue
		if(defaults.hue == "Null" && newHue) defaults.put("hue",newHue)
		// Set sat
		if(defaults.sat == "Null" && newSat) defaults.put("sat",newSat)
	}

	// Should be all the options, but let's return current level just in case, and log an error
	if(defaults.level == "Null") log.debug "Time: No default level match found for $device."

	logTrace("$app.label (742) [$device]: function getDefaultLevel returning $defaults")
	return defaults
}

// Schedule initializer
def initializeSchedules(){
	logTrace("$app.label (748): function initializeSchedules started")
	unschedule()
	
	// If disabled, return null
	if(timeDisable || state.timeDisableAll) {
		logTrace("$app.label (753): function initializeSchedules returning null (schedule disabled)")
		return
	}

	// Set timeStart and timeStop, if sunrise or sunset
	if(timeStartSunrise) timeStart = parent.getSunrise(timeStartOffset,timeStartOffsetNegative)
	if(timeStartSunset) timeStart = parent.getSunset(timeStartOffset,timeStartOffsetNegative)
	if(timeStopSunrise) timeStop = parent.getSunrise(timeStopOffset,timeStopOffsetNegative)
	if(timeStopSunset) timeStop = parent.getSunset(timeStopOffset,timeStopOffsetNegative)

	// if no start time
	if(!timeStart && !timeStartSunrise && !timeStartSunset){
		logTrace("$app.label (765): [device: $device] function initializeSchedules returning null (no start time)")
		return
	}

	// Immediately start incremental schedules
	// If incremental
	if(timeStop || timeStopSunrise || timeStopSunset){
		// Check if any incremental changes to make
		if((timeLevelOn && timeLevelOff) || (timeTempOn && timeTempOff) || (timeHueOn && timeHueOff) || (timeSatOn && timeSatOff)){
			// IncrementalSchedule does all data checks, so just run it
			logTrace("$app.label (775): function initializeSchedules passing to incrementalSchedule")
			incrementalSchedule()
		}
	}

	// Get start time cron data
	weekDays = weekDaysToNum()
	hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('HH').toInteger()
	minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStart).format('mm').toInteger()
	
	// Schedule next day incrementals, if no start action to be scheduled 
	if(timeOn != "Turn On" && timeOn != "Turn Off" && timeOn != "Toggle" && !timeModeChangeOn) {
		if(weekDays) {
			logTrace("$app.label (788): function initializeSchedules scheduling runDayOnSchedule (0 $minutes $hours ? * $weekDays)")
			schedule("0 " + minutes + " " + hours + " ? * " + weekDays, incrementalSchedule)
		} else {
			logTrace("$app.label (791): function initializeSchedules scheduling incrementalSchedule (0 $minutes $hours * * ?)")
			schedule("0 " + minutes + " " + hours + " * * ?", incrementalSchedule)
		}
	// Schedule next day's starting on/off/toggle
	} else if(timeOn == "Turn On" || timeOn == "Turn Off" || timeOn == "Toggle" || timeModeChangeOn){
		if(weekDays) {
			logTrace("$app.label (797): function initializeSchedules scheduling runDayOnSchedule (0 $minutes $hours ? * $weekDays)")
			schedule("0 " + minutes + " " + hours + " ? * " + weekDays, runDayOnSchedule)
		} else {
			logTrace("$app.label (800): function initializeSchedules scheduling runDayOnSchedule (0 $minutes $hours * * ?)")
			schedule("0 " + minutes + " " + hours + " * * ?", runDayOnSchedule)
		}
	}

	// Schedule next day's ending on/off/toggle														  
	if(timeOff == "Turn On" || timeOff == "Turn Off" || timeOff == "Toggle" || timeModeChangeOff){
		if(timeStop){
			// Increment time stop by a day if before start time
			if(timeToday(timeStop, location.timeZone).time < timeToday(timeStart, location.timeZone).time) timeStop = parent.getTomorrow(timeStop)
			hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('HH').toInteger()
			minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('mm').toInteger()
			if(weekDays) {
				logTrace("$app.label (813): function initializeSchedules scheduling runDayOffSchedule (0 $minutes $hours ? * $weekDays)")
				schedule("0 " + minutes + " " + hours + " ? * " + weekDays, runDayOffSchedule, [overwrite: false])
			}else {
				logTrace("$app.label (816): function initializeSchedules scheduling runDayOffSchedule (0 $minutes $hours * * ?)")
				schedule("0 " + minutes + " " + hours + " * * ?", runDayOffSchedule, [overwrite: false])
			}
		}
	}
	logTrace("$app.label (821): function initializeSchedules exiting")
}

//settings up schedules for level/temp
def incrementalSchedule(device = "Null",manualOverride=false){
	logTrace("$app.label (826): [device: $device] function incrementalSchedule started")
	// If disabled, return null
	if(timeDisable || state.timeDisableAll) {
		logTrace("$app.label (829): function incrementalSchedule returning null (schedule disabled)")
		return
	}

	if(timeLevelPico && manualOverride && (!timeOff && !timeModeChangeOff && !timeLevelOff)){
		logTrace("$app.label (834): [device: $device] function incrementalSchedule exiting (manual override)")
		return
	}

	// If nothing is on, return null
	if(!parent.multiStateOn(timeDevice)) {
		logTrace("$app.label (840): function incrementalSchedule returning null (device not on)")
		return
	}

	// Check if correct day and time just so we don't keep running forever
	if(timeDays && !parent.todayInDayList(timeDays)) {
		logTrace("$app.label (846): function incrementalSchedule returning null (not scheduled day)")
		return
	}

	// If mode set and node doesn't match, return null
	if(ifMode && location.mode != ifMode) {
		logTrace("$app.label (852): function incrementalSchedule returning null (mode $ifMode doesn't match)")
		return
	}
log.debug "855 $timeStartOffset"
	// Set timeStart and timeStop, if sunrise or sunset
	if(timeStartSunrise) timeStart = parent.getSunrise(timeStartOffset,timeStartOffsetNegative)
	if(timeStartSunset) timeStart = parent.getSunset(timeStartOffset,timeStartOffsetNegative)
	if(timeStopSunrise) timeStop = parent.getSunrise(timeStopOffset,timeStopOffsetNegative)
	if(timeStopSunset) timeStop = parent.getSunset(timeStopOffset,timeStopOffsetNegative)

	// If between start and stop time (if start time after stop time, then if after start time)
	if(parent.timeBetween(timeStart, timeStop)){
		// If Pico override, return null
		if(timeLevelPico && manualOverride && timeLevelPico) {
			logTrace("$app.label (866): [device: $device] function incrementalSchedule unsecheduling updates and exiting (manual override)")
			unschedule(incrementalSchedule)
			return
		}
		// Run first iteration now
		runIncrementalSchedule()
		runIn(20,incrementalSchedule)
		logTrace("$app.label (873): function incrementalSchedule scheduling itself")
		log.info "Time: Scheduling update for 20 seconds for $timeDevice."
	} else {
		logTrace("$app.label (876): function incrementalSchedule returning null (not between start $timeStart and stop time $timeStop)")
		return
	}
}

// run scheduled level/temp incremental changes
// scheduled function called from incrementalSchedule
def runIncrementalSchedule(){
	logTrace("$app.label: function runIncrementalSchedule started")
	// Loop through devices
	timeDevice.each{
		// Ignore devices that aren't on
		if(parent.stateOn(it)){
			// Set level
			defaults = getDefaultLevel(it)

			if(timeLevelOn && parent.isDimmable(it) && defaults.level != "Null"){
				if(defaults) parent.setToLevel(it,defaults.level,app.getId())
			}
			// Set temp
			if(timeTempOn && parent.isTemp(it) && defaults.temp != "Null"){
				currentTemp = it.currentColorTemperature
				if(defaults.temp){
					if(defaults.temp - currentTemp > 3 || defaults.temp - currentTemp < -3) {
						parent.singleTemp(it,defaults.temp,app.getId())
					}
				}
			}

			// If either Hue or Sat, but not both, set the other to current
			if(defaults.hue != "Null" || defaults.sat != "Null") {
				parent.singleColor(it,defaults.hue,defaults.sat,app.getId())
			}
		}
	}
	logTrace("$app.label: function runIncrementalSchedule exiting")
}

//Scheduled function called from setDaySchedule
def runDayOnSchedule(){
	logTrace("$app.label: function runDayOnSchedule started")
	if(timeDisable || state.timeDisableAll) {
		logTrace("$app.label: function runDayOnSchedule returning null (schedule disabled)")
		return
	}

	// if mode doesn't match, return
	if(ifMode){
		if(location.mode != ifMode) {
			logTrace("$app.label: function runDayOnSchedule returning null (mode doesn't match)")
			return
		}
	}
	if(timeModeChangeOn) setLocationMode(timeModeChangeOn)
	if(timeOn == "Turn On"){
		parent.multiOn(timeDevice,app.getId())
	} else if(timeOn == "Turn Off"){
		parent.multiOff(timeDevice,app.getId())
	} else if(timeOn == "Toggle"){
		parent.toggle(timeDevice,app.getId())
	}
	logTrace("$app.label: function runDayOffSchedule exiting to initializeSchedules")
	initializeSchedules()
}

//Scheduled function called from setDaySchedule
def runDayOffSchedule(){
	logTrace("$app.label: function runDayOffSchedule started")
	if(timeDisable || state.timeDisableAll) {
		logTrace("$app.label: function runDayOffSchedule returning null (schedule disabled)")
		return
	}

	// if mode return
	if(ifMode){
		if(location.mode != ifMode) {
			logTrace("$app.label: function runDayOffSchedule returning null (mode doesn't match)")
			return
		}
	}
	if(timeModeChangeOff) setLocationMode(timeModeChangeOff)
	if(timeOff == "Turn On"){
	   parent.multiOn(timeDevice,app.getId())
	} else if(timeOff == "Turn Off"){
	   parent.multiOff(timeDevice,app.getId())
	} else if(timeOff == "Toggle"){
	   parent.toggle(timeDevice,app.getId())
	}
	logTrace("$app.label: function runDayOffSchedule exiting to initializeSchedules")
	initializeSchedules()
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
	logTrace("$app.label: function weekDaysToNum returning $dayString")
	return dayString
}

def logTrace(message){
	log.trace message
}
