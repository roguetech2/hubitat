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
*  Name: Master - Presence
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master%20-%20Presence.groovy
*  Version: 0.1.35
*
***********************************************************************************************************************/


/* ********************************************* */
/* TO-DO: Add option for window of arrival of    */
/* other people for doing stuff "alone".         */
/* ********************************************* */

definition(
    name: "Master - Presence",
    namespace: "master",
    author: "roguetech",
    description: "Presence - Arriving and Leaving",
    parent: "master:Master",
    category: "Convenience",
    importUrl: "https://raw.githubusercontent.com/roguetech2/hubitat/master/Master%20-%20Presence.groovy",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png"
)

/* ************************************************** */
/* TO-DO: Add error messages (and change info icon    */
/* (see humidity).                                    */
/* ************************************************** */ 
preferences {
	infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
	errorIcon = "<img src=\"http://emily-john.love/icons/error.png\" width=20 height=20>"
	page(name: "setup", install: true, uninstall: true) {
		section() {
			// Set disable all
			if(presenceDisableAll) {
				state.presenceDisable = true
			} else {
				state.presenceDisable = false
			}

			// If all disabled, force reenable
			if(state.presenceDisable){
				input "presenceDisableAll", "bool", title: "<b>All schedules are disabled.</b> Reenable?", defaultValue: false, submitOnChange:true
			} else if(presenceDisable){
				paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this presence routine:</b></div>"
				label title: "Routine name?", required: true, submitOnChange:true
				if(app.label){
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select which people:</b></div>"
					input "person", "capability.presenceSensor", title: "Person/people", multiple: true, required: true, submitOnChange:true
				}
				input "presenceDisable", "bool", title: "<b><font color=\"#000099\">Presence is disabled.</font></b> Reenable it?", submitOnChange:true
				input "presenceDisableAll", "bool", title: "Disable <b>ALL</b> presence routines?", defaultValue: false, submitOnChange:true

			} else {
				paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this presence routine:</b></div>"
				label title: "", required: true, submitOnChange:true
				if(!app.label){
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				} else if(app.label){
					input "presenceDisable", "bool", title: "Disable this presence?", submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select people for this routine:</b></div>"
					input "person", "capability.presenceSensor", title: "Person/people", multiple: true, required: true, submitOnChange:true
					if(person && person.size() > 1 && arrivingDeparting != "both"){
						paragraph("<div style=\"background-color:BurlyWood\"><b> Select if all or any of the people:</b></div>")
						if(!peopleAll){
							input "peopleAll", "bool", title: "Any of these people. Click to change.", submitOnChange:true
						} else {
							input "peopleAll", "bool", title: "All of these people. Click to change.", submitOnChange:true
						}
					}
					if(!person){
						paragraph "<div style=\"background-color:BurlyWood\"> </div>"
					} else if(person){
						paragraph("<div style=\"background-color:BurlyWood\"><b> Select if with arrivals or depatures (or both):</b></div>")
						input "arrivingDeparting", "enum", title: "Arriving or Departing?", required: true, multiple: false, width: 12, options: ["present":"Arriving", "not present":"Departing", "both":"Both arriving and departing"], submitOnChange:true
						if(!arrivingDeparting) {
							paragraph "<div style=\"background-color:BurlyWood\"> </div>"
						} else if(arrivingDeparting){
							paragraph "<div style=\"background-color:BurlyWood\"><b> Select if with arrivals to empty or occupied house (or either):</b></div>"
							input "occupiedHome", "enum", title: "Empty or occupied?", required: true, multiple: false, width: 12, options: ["unoccupied":"Only if home is otherwise unoccupied", "occupied":"Only if home is already occupied", "both":"Either empty or occupied"], submitOnChange:true
							if(!occupiedHome) {
								paragraph "<div style=\"background-color:BurlyWood\"> </div>"
							} else if(occupiedHome){
								paragraph "<div style=\"background-color:BurlyWood\"><b> Select which devices:</b></div>"
								if(!switches && !locks)	input "noDevice", "bool", title: "<b>No devices.</b> Click to continue if ONLY setting mode, or sending text alert.", defaultValue: false, submitOnChange:true
								if(!noDevice) input "switches", "capability.switchLevel", title: "Lights and switches?", multiple: true, required: false, submitOnChange:true
								if(!noDevice) input "locks", "capability.lock", title: "Locks?", multiple: true, required: false, submitOnChange:true
								if(!switches && !locks && !noDevice) {
									paragraph "<div style=\"background-color:BurlyWood\"> </div>"
								} else if((switches || locks) && !noDevice){
									paragraph "<div style=\"background-color:BurlyWood\"><b> Select what to do:</b></div>"
									if(switches) input "actionSwitches", "enum", title: "What to do with lights/switches? (Optional)", required: false, multiple: false, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
									if(locks) input "actionLocks", "enum", title: "What to do with locks? (Optional)", required: false, multiple: false, options: ["lock":"Lock", "unlock":"Unlock", ], submitOnChange:true
									if((switches && !actionSwitches) || (locks && !actionLocks)){
										paragraph "<div style=\"background-color:BurlyWood\">1 </div>"
									}
								}
									if(((switches && actionSwitches) || (locks && actionLocks)) || noDevice){
										paragraph "<div style=\"background-color:BurlyWood\"><b> Miscellaneous actions (Optional):</b></div>"
										input "noFlashColor", "bool", title: "Flash all color lights?", defaultValue: false, submitOnChange:true
										if(noFlashColor){
											if(flashColor) {
												input "flashColor", "color", title: "Flash all color lights?", required: false, default: flashColor
											} else {
												input "flashColor", "color", title: "Flash all color lights?", required: false
											}
										}
										input "mode", "mode", title: "Change Mode? (Optional)", required: false, submitOnChange:true
										input "phone", "phone", title: "Number to text alert? (Optional)", required: false, submitOnChange:true
										if(parent.notificationDevice) input "speakText", "text", title: "Voice notification text? (Optional)", required: false, submitOnChange:true
										if(((!switches && !locks) && !noDevice) || (!actionSwitches && !actionLocks && !mode && !phone && (!flashColor || !noFlashColor) && !speakText)) {
											paragraph "<div style=\"background-color:BurlyWood\"> </div>"
										} else {
											paragraph "<div style=\"background-color:BurlyWood\"><b> Select time or mode (Optional):</b></div>"
											paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Schedule and mode restriction applies to all actions.</div>"
											if(!timeStartSunrise && !timeStartSunset){
												if(timeStop){
													paragraph "Between start time", width: 6
													//input "timeStart", "time", title: "Between start time", required: false, width: 6, submitOnChange:true
												} else {
													paragraph "Between start time (optional)", width: 6
													//input "timeStart", "time", title: "Between start time (Optional)", required: false, width: 6, submitOnChange:true
												}
											} else if(timeStartSunrise) {
												if(timeStartOffsetNegative) {
													input "timeStartOffsetNegative", "bool", title: "Minutes <b>after</b> sunrise (click to change)", required: false, width: 6, submitOnChange:true
												} else {
													input "timeStartOffsetNegative", "bool", title: "Minutes <b>before</b> sunrise (click to change)", required: false, width: 6, submitOnChange:true
												}
											} else if(timeStartSunset){
												if(timeStartOffsetNegative) {
													input "timeStartOffsetNegative", "bool", title: "Minutes <b>after</b> sunset (click to change)", required: false, width: 6, submitOnChange:true
												} else {
													input "timeStartOffsetNegative", "bool", title: "Minutes <b>before</b> sunset (click to change)", required: false, width: 6, submitOnChange:true
												}
											}

											if(!timeStopSunrise && !timeStopSunset){
												if(timeStart){
													paragraph "and stop time", width: 6
													//input "timeStop", "time", title: "and stop time", required: false, width: 6, submitOnChange:true
												} else {
													paragraph "and stop time (optional)", width: 6
													//input "timeStop", "time", title: "and stop time (Optional)", required: false, width: 6, submitOnChange:true
												}
											} else if(timeStopSunrise){
												if(timeStopOffsetNegative) {
													input "timeStopOffsetNegative", "bool", title: "and minutes <b>after</b> sunrise (click to change)", required: false, width: 6, submitOnChange:true
												} else {
													input "timeStopOffsetNegative", "bool", title: "and minutes <b>before</b> sunrise (click to change)", required: false, width: 6, submitOnChange:true
												}
											} else if(timeStopSunset){
												if(timeStopOffsetNegative) {
													input "timeStopOffsetNegative", "bool", title: "and minutes <b>after</b> sunset (click to change)", required: false, width: 6, submitOnChange:true
												} else {
													input "timeStopOffsetNegative", "bool", title: "and minutes <b>before</b> sunset (click to change)", required: false, width: 6, submitOnChange:true
												}
											}
											if(!timeStartSunrise && !timeStartSunset){
												input "timeStart", "time", title: "", required: false, width: 6, submitOnChange:true
											} else if(timeStartSunrise || timeStartSunset){
												input "timeStartOffset", "number", title: "", required: false, width: 6, submitOnChange:true
											}

											if(!timeStopSunrise && !timeStopSunset){
												input "timeStop", "time", title: "", required: false, width: 6, submitOnChange:true
											} else if(timeStopSunrise || timeStopSunset){
												input "timeStopOffset", "number", title: "", required: false, width: 6, submitOnChange:true
											}

											if(!timeStartSunset){
												input "timeStartSunrise", "bool", title: "Start at sunrise?", width: 6, submitOnChange:true
											} else {
												paragraph " ", width: 6
											}
											if(!timeStopSunset) {
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
											input "timeDays", "enum", title: "On these days: (Optional):", required: false, multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"]
											input "ifMode", "mode", title: "Only if the Mode is already: (Optional)", required: false, width: 12
										}
								}
							}
						}
					}
					paragraph " "
					input "presenceDisableAll", "bool", title: "Disable <b>ALL</b> presence routines?", defaultValue: false, submitOnChange:true
				}
			}
		}
	}
}

def installed() {
	logTrace(219,"Installed","trace")
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
	initialize()
}

def updated() {
	logTrace(225,"Updated","trace")
	unsubscribe()
	initialize()
}

def initialize() {
	logTrace(231,"Initialized","trace")

    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

	subscribe(person, "presence", presenceHandler)
}

def presenceHandler(evt) {
	logTrace(239,"$evt.displayName status changed to $evt.value","debug")
	
	// If presence is disabled, return null
	if(state.presenceDisable || presenceDisable) return
	
	// If arrival or departure doesn't match, return null
	if(arrivingDeparting){
		if(evt.value != arrivingDeparting && arrivingDeparting != "both") return
	}
	
	// If occupied or unoccupied doesn't match, return null
	// Will need to change this is add an "any/all" option
	if(occupiedHome != "both"){
		occupied = false
		// Set occupied flag for all people but don't count the person in question
		parent.people.each{all->
			person.each{
				if(all.id != it.id && all.currentPresence == "present") {
					occupied = true
				}
			}
		}
		if((occupiedHome == "unoccupied" && occupied) || (occupiedHome == "occupied" && !occupied)) {
			logTrace(262,"Presence handler doing nothing; occupied status is $occupied instead of $occupiedHome","trace")
			return
		}
	}
		
	// If mode set and node doesn't match, return null
	if(ifMode && location.mode != ifMode) {
		logTrace(269,"Presence handler doing nothing; $location.mode is not $ifMode","trace")
		return
	}
	
	// Set timeStart and timeStop, if sunrise or sunset
	if(timeStartSunrise) timeStart = parent.getSunrise(timeStartOffset,timeStartOffsetNegative,app.label)
	if(timeStartSunset) timeStart = parent.getSunset(timeStartOffset,timeStartOffsetNegative,app.label)
	if(timeStopSunrise) timeStop = parent.getSunrise(timeStopOffset,timeStopOffsetNegative,app.label)
	if(timeStopSunset) timeStop = parent.getSunset(timeStopOffset,timeStopOffsetNegative,app.label)

	// if not bewteen start and stop times
	if(timeStart && timeStop && !parent.timeBetween(timeStart, timeStop,app.label)) return

	// If not correct day, return null
	if(timeDays && !parent.todayInDayList(timeDays,app.label)) {
		logTrace(284,"Presence handler doing nothing; not correct day","trace")
		return
	}

	// Text first (just in case there's an error later)
/* ************************************************** */
/* TO DO: Instead of throwing error here, validate    */
/* number on setup.                                   */
/* ************************************************** */
	if(phone){
		def now = new Date()
		now = now.format("h:mm a", location.timeZone)
		if(evt.value == "present"){
			//parent.sendText(phone,"$evt.displayName arrived at the house $now.",app.label)
			logTrace(298, "SMS no longer supported until updates complete. $evt.displayName's arrival at $now.","info")
		} else {
			//parent.sendText(phone,"$evt.displayName left the house at $now.",app.label)
			logTrace("SMS no longer supported until updates complete. $evt.displayName's departure at $now.","info")
		}
	}

	// Send voice notification
// Needs to be changed and/or expanded to push notifications
//input "notificationDevice", "capability.notification", multiple: false, required: false
// notificationDevice.deviceNotification("Here is the message!")
// https://community.hubitat.com/t/coding-help-for-new-he-user/14925/2
	if(speakText) parent.speak(speakText,app.label)

	// Set mode
	if(mode) parent.changeMode(mode,app.label)
	// Turn on/off lights
	if(switches) setStateMulti(actionSwitches,switches)

	// Lock/unlock doors
	if(locks) parent.multiLock(actionLocks,locks,app.label)

	// Flash alert
	if(flashColor && noFlashColor) {
		colorMap = convertRgbToHsl(flashColor)
		hue = colorMap.hue
		sat = colorMap.sat
		level = colorMap.level
		def current = [:]

		// Loop through color lights to set color
		parent.colorLights.each{
			current[it.id] = [temp: it.currentValue("colorTemperature"), level: it.currentLevel, state: it.currentValue("switch")]
			if(it.currentValue("switch") == "off"){
				it.setLevel(1)
			} else {
				it.setLevel(100)
			}
			newValue = [hue: hue, saturation: sat]
			it.setColor(newValue)
		}
		pause(750)

		// loop through again to restore old values from map
		parent.colorLights.each{
			it.setLevel(current."${it.id}".level)
			it.setColorTemperature(current."${it.id}".temp)
			if(current."${it.id}".state == "off") it.off()
		}
	}
}

def convertRgbToHsl(color){
	/* ************************************* */
	/* TO DO: Can we convert it once         */
	/* and store in hidden input variable??  */
	/* ************************************* */

	// Split the hex string in three
	red = color.substring(1,3)
	blue = color.substring(3,5)
	green = color.substring(5,7)
		
	// Convert from hex to decimal (true decimals, as 0 to 1)
	red = Integer.valueOf(red, 16) / 255;
	blue = Integer.valueOf(blue, 16) / 255;
	green = Integer.valueOf(green, 16) / 255;
	
	// Create map, to use min and max
	def colorMap = ["red":red, "blue":blue, "green":green]
	
	// Get min and max values
	min = colorMap.values().min()
	max = colorMap.values().max()
	
	// Compute hue value
	if(red == max) {
		hue = (green - blue) / (max - min)
	} else if (green == max){
		hue = 2 + (blue - red) / (max - min)
	} else {
		hue = 4 + (red - green) / (max - min)
	}
	
	// Compute sat value
	if(level > 0.5) {
		sat = (max - min) / (max + min)
	} else {
		sat = (max - min) / (2 - max - min)
	}
	
	// Compute level
	level = min + max / 2
	
	// Convert to final values
	hue = Math.round(hue * 100)
	sat = Math.round(sat * 100)
	level = Math.round(level * 100)

	// Store final values in map, and return
	def hsl = ["hue":hue, "sat":sat, "level":level]

	return hsl
	
}

// Gets levels as set for the app
// Function must be included in all apps that use MultiOn
def getOverrideLevels(defaults,appAction = null){
    // Need to add levels
    return defaults       
}

// If deviceChange exists, adds deviceId to it; otherwise, creates deviceChange with deviceId
// Used to track if app turned on device when schedule captures a device state changing to on
// Must be included in all apps using MultiOn
def addStateDeviceChange(singleDeviceId){
    if(atomicState.deviceChange) {
        atomicState.deviceChange = "$atomicState.deviceChange:$singleDeviceId:"
    } else {
        atomicState.deviceChange = ":$singleDeviceId:"
    }
}

// Returns the value of deviceChange
// Used by schedule when a device state changes to on, to check if an app did it
// Function must be in every app
def getStateDeviceChange(singleDeviceId){
    if(atomicState.deviceChange){
        return atomicState.deviceChange.indexOf(":$singleDeviceId:")
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

// Presense does not use "resume" or "none" (at least, not yet)
// This is a bit of a mess, but.... 
def setStateMulti(deviceAction,device,appAction = null){
    if(!deviceAction || (deviceAction != "on" && deviceAction != "off" && deviceAction != "toggle")) {
        logTrace(1133,"Invalid deviceAction \"$deviceAction\" sent to setStateMulti","error")
        return
    }

    if(deviceAction == "off"){
        // Turn off devices
        parent.setStateMulti("off",device,app.label)
        return true
    }

    if(deviceAction == "on"){
        // Add device ids to deviceChange, so schedule knows it was turned on by an app
        device.each{
            addStateDeviceChange(it.id)
            // Time to schedule resetting deviceChange should match total time of waitStateChange
            // Might be best to put this in a state variable in Initialize, as a setting?
            runIn(2,resetStateDeviceChange)
        }
        logTrace(1151,"Device id's turned on are $atomicState.deviceChange","debug")

        // Turn on devices
        parent.setStateMulti("on",device,app.label)
        // Get and set defaults levels for each device
        device.each{
            setStateOnSingle(it,appAction)
        }
        return true
    }

    if(deviceAction == "toggle"){
        // Create toggleOnDevice variable, used to track which devices are being turned on
        toggleOnDevice = []
        // Set count variable, used for toggleOnDevice
        count = 0
        device.each{
            count = count + 1
            // Get original state
            deviceState = parent.isOn(it)
            // If toggling to off
            if(parent.isOn(it)){
                parent.setStateSingle("off",it,app.label)
                // Else if toggling on
            } else {
                // When turning on, add device id to deviceChange, so schedule knows it was turned on by an app
                addStateDeviceChange(it.id)
                runIn(2,resetStateDeviceChange)
                parent.setStateSingle("on",it,app.label)
                // Create list of devices toggled on
                // This lets us turn all of them on, then loop again so when setting levels, it won't wait for each individual device to respond
                toggleOnDevice.add(count)
            }
        }
        logTrace(1189,"Device id's turned on are $atomicState.deviceChange","debug")
        // Create newCount variable, which is compared to the [old]count variable
        // Used to identify which lights were turned on in the last loop
        newCount = 0
        device.each{
            newCount = newCount + 1
            // If turning on, set default levels and over-ride with any contact levels
            // If newCount is contained in the list of [old]count, then we toggled on
            if(toggleOnDevice.contains(newCount)){
                setStateOnSingle(it,appAction)
            }
        }
        return true
    }

    if(deviceAction == "resume"){
        device.each{
            // If turning on, set default levels and over-ride with any contact levels
            if(deviceAction == "resume"){
                // If defaults, then there's an active schedule
                // So use it for if overriding/reenabling
                defaults = parent.getScheduleDefaultSingle(it,app.label)
                logTrace(1211,"Scheduled defaults are $defaults","debug")

                defaults = getOverrideLevels(defaults,appAction)
                logTrace(1214,"With " + app.label + " overrides, using $defaults","debug")

                // Skipping getting overall defaults, since we're resuming a schedule or exiting;
                // rather keep things the same level rather than an arbitrary default, and
                // if we got default, we'd not turn it off

                parent.setLevelSingle(defaults.level,defaults.temp,defaults.hue,defaults.sat,it,app.label)
                // Set default level
                if(!defaults){
                    logTrace(1223,"No schedule to resume for $it; turning off","trace")
                    parent.setStateSingle("off",it,app.label)
                } else {
                    parent.rescheduleIncrementalSingle(it,app.label)
                }
            }
        }
        return true
    }

    if(deviceAction == "none"){
        // If doing nothing, reschedule incremental changes (to reset any overriding of schedules)
        // I think this is the only place we use ...Multi, prolly not enough to justify a separate function
        parent.rescheduleIncrementalMulti(device,app.label)
        return true
    }
}

// Handles turning on a single device and setting levels
def setStateOnSingle(singleDevice,appAction = null){
    // If defaults, then there's an active schedule
    // So use it for if overriding/reenabling
    // In scheduler app, this gets defaults for any *other* schedule
    defaults = parent.getScheduleDefaultSingle(singleDevice,app.label)
    logMessage = defaults ? "$singleDevice scheduled for $defaults" : "$singleDevice has no scheduled default levels"

    // If there are defaults, then there's a schedule (the results are corrupted below)
    if(defaults) parent.rescheduleIncrementalSingle(singleDevice,app.label)

    // This does nothing in Time, or other app that has no levels
    defaults = getOverrideLevels(defaults,appAction)
    logMessage += defaults ? ", controller overrides of $defaults": ", no controller overrides"

    // Set default levels, for level and temp, if no scheduled defaults (don't need to do for "resume")
    defaults = parent.getDefaultSingle(defaults,app.label)
    logMessage += ", so with generic defaults $defaults"

    logTrace(1260,logMessage,"debug")
    parent.setLevelSingle(defaults.level,defaults.temp,defaults.hue,defaults.sat,singleDevice,app.label)
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def logTrace(lineNumber,message = null, type = "trace"){
    // Uncomment return for no logging at all
    // return

    // logLevel sets number of log messages
    // 1 for least (errors only)
    // 5 for most (all)
    logLevel = 5

    message = (message ? " -- $message" : "")
    if(lineNumber) message = "(line $lineNumber)$message"
    message = "$app.label $message"
    switch(type) {
        case "error":
        log.error message
        break
        case "warn":
        if(logLevel > 1) log.warn message
        break
        case "info":
        if(logLevel > 2) log.info message
        break
        case "trace":
        if(logLevel > 3) log.trace message
        break
        case "debug":
        if(loglevel == 5) log.debug message
    }
    return true
}
