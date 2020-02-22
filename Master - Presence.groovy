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
*  Version: 0.1.38
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

// logLevel sets number of log messages
// 0 for none
// 1 for errors only
// 5 for all
def getLogLevel(){
    return 5
}

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
	if(checkLog(a="trace")) putLog(228,"Installed",a)
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
	initialize()
}

def updated() {
	if(checkLog(a="trace")) putLog(234,"Updated",a)
	unsubscribe()
	initialize()
}

def initialize() {
	if(checkLog(a="trace")) putLog(240,"Initialized",a)

    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

	subscribe(person, "presence", presenceHandler)
}

def presenceHandler(evt) {
	if(checkLog(a="debug")) putLog(248,"$evt.displayName status changed to $evt.value",a)
	
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
			if(checkLog(a="trace")) putLog(271,"Presence handler doing nothing; occupied status is $occupied instead of $occupiedHome",a)
			return
		}
	}
		
	// If mode set and node doesn't match, return null
	if(ifMode && location.mode != ifMode) {
		if(checkLog(a="trace")) putLog(278,"Presence handler doing nothing; $location.mode is not $ifMode",a)
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
		if(checkLog(a="trace")) putLog(293,"Presence handler doing nothing; not correct day",a)
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
			if(checkLog(a="info")) putLog(307299, "SMS no longer supported until updates complete. $evt.displayName's arrival at $now.",a)
		} else {
			//parent.sendText(phone,"$evt.displayName left the house at $now.",app.label)
			if(checkLog(a="info")) putLog(310,"SMS no longer supported until updates complete. $evt.displayName's departure at $now.",a)
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
        if(checkLog(a="error")) putLog(453,"Invalid deviceAction \"$deviceAction\" sent to setStateMulti",a)
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
        if(checkLog(a="debug")) putLog(482,"Device id's turned on are $atomicState.deviceChange",a)
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
        if(checkLog(a="debug")) putLog(511,"Device id's toggled on are $atomicState.deviceChange",a)
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
                if(checkLog(a="debug")) putLog(538,"Scheduled defaults are $defaults",a)

                defaults = getOverrideLevels(defaults,appAction)
                if(checkLog(a="debug")) putLog(541,"With " + app.label + " overrides, using $defaults",a)

                // Skipping getting overall defaults, since we're resuming a schedule or exiting;
                // rather keep things the same level rather than an arbitrary default, and
                // if we got default, we'd not turn it off

                parent.setLevelSingle(defaults.level,defaults.temp,defaults.hue,defaults.sat,it,app.label)
                // Set default level
                if(!defaults){
                    if(checkLog(a="trace")) putLog(550,"No schedule to resume for $it; turning off",a)
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

    if(checkLog(a="debug")) putLog(595,logMessage,a)
    parent.setLevelSingle(defaults.level,defaults.temp,defaults.hue,defaults.sat,singleDevice,app.label)
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
    message = (message ? " -- $message" : "")
    if(lineNumber) message = "(line $lineNumber)$message"
    message = "$app.label $message"
    if(type == "error") message = "<font color=\"red\">$message</font>"
    if(type == "warn") message = "<font color=\"yellow\">$message</font>"
    switch(type) {
        case "error":
        log.error(message)
        return true
        case "warn":
        log.warn(message)
        return true
        case "info":
        log.info(message)
        return true
        case "trace":
        log.trace(message)
        return true
        case "debug":
        log.debug(message)
        return true
    }
    return
}
