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
*  Name: Master - Contact
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master%20-%20Contact.groovy
*  Version: 0.3.24
* 
***********************************************************************************************************************/

definition(
    name: "Master - Contact",
    namespace: "master",
    author: "roguetech",
    description: "Door Sensors",
    parent: "master:Master",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png"
)

/* ************************************************** */
/* TO-DO: Add error messages (and change info icon    */
/* (see humidity).                                    */
/* ************************************************** */ 
preferences {
	infoIcon = "<img src=\"http://files.softicons.com/download/toolbar-icons/fatcow-hosting-icons-by-fatcow/png/16/information.png\" width=20 height=20>"
	
	errorIcon = "<img src=\"http://files.softicons.com/download/toolbar-icons/fatcow-hosting-icons-by-fatcow/png/16/error.png\" width=20 height=20>"
    page(name: "setup", install: true, uninstall: true) {
		
        section() {
			// Set disable all
			if(contactDisableAll) {
				state.contactDisable = true
			} else {
				state.contactDisable = false
			}
			
			// If all disabled, force reenable
			if(state.contactDisable){
				input "contactDisableAll", "bool", title: "All contact sensors are disabled. Reenable?", defaultValue: false, submitOnChange:true
			} else if(contactDisable){
				paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this contact sensor routine:</b></div>"
				label title: "", required: true, submitOnChange:true
				paragraph "<font color=\"#000099\"><b>Select which sensor(s):</b></font>"
				input "contactDevice", "capability.contactSensor", title: "Contact Sensor(s)", multiple: true, required: true
				input "contactDisable", "bool", title: "<b><font color=\"#000099\">This contact sensor is disabled.</font></b> Reenable it?", submitOnChange:true
			} else {
				paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this contact sensor routine:</b></div>"
				label title: "", required: true, submitOnChange:true
				if(!app.label){
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				} else if(app.label){
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select door sensors for routine:</b></div>"
					input "contactDevice", "capability.contactSensor", title: "Contact sensor(s)?", multiple: true, required: true, submitOnChange:true
					input "contactDisable", "bool", title: "Disable this contact sensor?", submitOnChange:true
					if(!contactDevice){
						paragraph "<div style=\"background-color:BurlyWood\"> </div>"
					} else if(contactDevice){
						paragraph "<div style=\"background-color:BurlyWood\"><b>Select which devices to control:</b></div>"
						if(!switches && !locks) input "noDevice", "bool", title: "<b>No devices.</b> Click to continue if ONLY setting mode, sending text alert or making voice notification.", defaultValue: false, submitOnChange:true
						if(!noDevice) input "switches", "capability.switchLevel", title: "Lights/switches?", multiple: true, required: false, submitOnChange:true
						if(!noDevice) input "locks", "capability.lock", title: "Locks?", multiple: true, required: false, submitOnChange:true
						if((!switches && !locks) && !noDevice) {
							paragraph "<div style=\"background-color:BurlyWood\"> </div>"
						} else if((switches || locks) || noDevice) {
							paragraph "<div style=\"background-color:BurlyWood\"><b> Select what to do with switches, locks or voice alert:</b></div>"
							if(switches) {
								input "actionOpenSwitches", "enum", title: "What to do with lights/switches on open? (Optional)", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
								input "actionCloseSwitches", "enum", title: "What to do with lights/switches on close? (Optional)", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
							}

							if(switches){
								input "contactLevelOpen", "number", title: "Set brightness at open? (Only if on - Optional: 1-100; Default 100)", required: false, width: 6, submitOnChange:true

								// Close Level: Only show if Stop Time entered
								input "contactLevelClose", "number", title: "Set brighteness at close? (Only if on - Optional: 1-100)", required: false, width: 6, submitOnChange:true

								if(contactLevelOpen > 100){
									error = 1
									paragraph "<div style=\"background-color:Bisque\">$errorIcon Level can't be more than 100.</div>"
								}
								if(contactLevelClose > 100){
									error = 1
									paragraph "<div style=\"background-color:Bisque\">$errorIcon Level can't be more than 100.</div>"
								}

								//Open Temp
								if(!contactHueOpen){
									input "contactTempOpen", "number", title: "Set temperature at open? (Only if on - Optional, default 3400)", required: false, width: 6, submitOnChange:true
								} else if(!contactHueClose){
									paragraph "", width: 6
								}

								// Close Temp
								if(!contactHueClose){
									input "contactTempClose", "number", title: "Set temperature at close? (Only if on - Optional, default 3400)", required: false, width: 6, submitOnChange:true
								} else if(!contactHueOpen){
									paragraph "", width: 6
								}


								if(contactTempOpen && contactTempOpen < 1800){
									error = 1
									paragraph "<div style=\"background-color:Bisque\">$errorIcon Temperature can't be less than 1800.</div>"
								}
								if(contactTempOpen > 5400){
									error = 1
									paragraph "<div style=\"background-color:Bisque\">$errorIcon Temperature can't be more than 5400.</div>"
								}
								if(contactTempClose && contactTempClose < 1800){
									error = 1
									paragraph "<div style=\"background-color:Bisque\">$errorIcon Temperature can't be less than 1800.</div>"
								}
								if(contactTempClose > 5400){
									error = 1
									paragraph "<div style=\"background-color:Bisque\">$errorIcon Temperature can't be more than 5400.</div>"
								}

								// Open Hue
								if(!contactTempOpen){
									input "contactHueOpen", "number", title: "Set color hue at open? (Only if on - Optional)", required: false, width: 6, submitOnChange:true
								} else if(!contactTempClose){
									paragraph "", width:6
								}

								// Close Hue
								if(!contactTempClose){
									input "contactHueClose", "number", title: "Set color hue at close? (Only if on - Optional)", required: false, width: 6, submitOnChange:true
								} else if(!contactTempOpen){
									paragraph "", width: 6
								}

								if(contactHueOpen > 100){
									error = 1
									paragraph "<div style=\"background-color:Bisque\">$errorIcon Hue can't be more than 100.</div>"
								}
								if(contactHueClose > 5400){
									error = 1
									paragraph "<div style=\"background-color:Bisque\">$errorIcon Hue can't be more than 100.</div>"
								}

								//Open Saturation
								if(contactHueOpen){
									input "contactSatOpen", "number", title: "Set saturation on open?  (Only if on - Optional)", required: false, width: 6, submitOnChange:true
								} else if(contactHueClose){
									paragraph "", width: 6
								}
								// Close Saturation
								if(contactHueClose){
									input "contactSatClose", "number", title: "Set saturation on close?  (Only if on - Optional)", required: false, width: 6, submitOnChange:true
								} else if(contactHueOpen){
									paragraph "", width: 6
								}

								if(contactSatOpen > 100){
									error = 1
									paragraph "<div style=\"background-color:Bisque\">$errorIcon Saturation can't be more than 100.</div>"
								}
								if(contactSatclose > 5400){
									error = 1
									paragraph "<div style=\"background-color:Bisque\">$errorIcon Saturation can't be more than 100.</div>"
								}
							}
							

							if(locks) {
								input "actionOpenLocks", "enum", title: "What to do with locks on open? (Optional)", required: false, multiple: false, width: 6, options: ["lock":"Lock", "unlock":"Unlock"], submitOnChange:true
								input "actionCloseLocks", "enum", title: "What to do with locks on close? (Optional)", required: false, multiple: false, width: 6, options: ["lock":"Lock", "unlock":"Unlock"], submitOnChange:true
							}

							
							
							
					
							
							
							
							
							
							input "mode", "mode", title: "Change Mode? (Optional)", required: false, submitOnChange:true
							input "phone", "phone", title: "Number to text alert? (Optional)", required: false, submitOnChange:true
							if(parent.notificationDevice) input "speakText", "text", title: "Voice notification text? (Optional)", required: false, submitOnChange:true
							if(mode || phone || speakText){
								if(openOrClose){
									input "openOrClose", "bool", title: "When <b>opened</b>, change mode, text and/or notification. Click for when closed.", defaultValue: false, submitOnChange:true
								} else {
									input "openOrClose", "bool", title: "When <b>closed</b>, change mode, text and/or notification. Click for on opened.", defaultValue: false, submitOnChange:true
								}
							}
							if(phone || speakText){
								input "personHome", "capability.presenceSensor", title: "Only alert if any of these people are home (optional)", multiple: true, required: false, submitOnChange:true
								input "personNotHome", "capability.presenceSensor", title: "Only alert if none of these people are home (optional)", multiple: true, required: false, submitOnChange:true
							}
							if(!actionOpenSwitches && !actionCloseSwitches && !actionOpenLocks && !actionCloseLocks && !mode && !phone) {
								paragraph "<div style=\"background-color:BurlyWood\"> </div>"
							} else {
								if(!noDevice) paragraph "<div style=\"background-color:BurlyWood\"><b> Set wait time before setting lights:</b></div>"
								if(!openWait && !closeWait && !noDevice) input "noWaitTime", "bool", title: "<b>No pause.</b> Click to continue to set schedule and mode.", defaultValue: false, submitOnChange:true
								if(!noWaitTime && !noDevice){
									if(!noWaitTime) input "openWait", "number", title: "Wait seconds for opening action.", defaultValue: false, width: 6, submitOnChange:true
									if(!noWaitTime) input "closeWait", "number", title: "Wait seconds for closing action.", defaultValue: false, width: 6, submitOnChange:true
									paragraph "<div style=\"background-color:AliceBlue\">$infoIcon Wait time applies to turning lights/switches and locks, not to Mode change or text alerts.</div>"
								}

								if(!openWait && !closeWait && !noWaitTime && !noDevice){
									paragraph "<div style=\"background-color:BurlyWood\"> </div>"
								} else {
									paragraph "<div style=\"background-color:BurlyWood\"><b> Select time or mode (Optional):</b></div>"
									if(!timeStartSunrise && !timeStartSunset){
								if(timeStop){
									paragraph "Between start time", width: 6
								} else {
									paragraph "Between start time (optional)", width: 6
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
								} else {
									paragraph "and stop time (optional)", width: 6
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
								input "timeStartSunset", "bool", title: "Start at sunset?", width: 6, submitOnChange:true
							} else {
								paragraph " ", width: 6
							}
							if(!timeStopSunrise){
								input "timeStopSunset", "bool", title: "Stop at sunset?", width: 6, submitOnChange:true
							} else {
								paragraph " ", width: 6
							}
									input "timeDays", "enum", title: "On these days: (Optional):", required: false, multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"]
									input "ifMode", "mode", title: "Only if the Mode is already: (Optional)", required: false, width: 12
									paragraph "<div style=\"background-color:LightCyan\"><b> Click \"Done\" to continue.</b></div>"
								}
							}
						}
						input "contactDisableAll", "bool", title: "Disable <b>ALL</b> contact sensors?", defaultValue: false, submitOnChange:true
					}
				}
			}
		}
    }

}

def installed() {
	logTrace("$app.label (line 310) -- Installed")
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
	initialize()
}

def updated() {
	logTrace("$app.label (line 316) -- Updated")
	unsubscribe()
	initialize()
}

def initialize() {
	logTrace("$app.label (line 322) -- Initialized")
	unschedule()
    
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    
	// If date/time for last SMS not set, initialize it to 5 minutes ago
	// Allows an SMS immediately
	if(!state.contactLastSms) state.contactLastSms = new Date().getTime() - 360000

	if(!contactDisable && !state.contactDisable) {
		subscribe(contactDevice, "contact.open", contactChange)
		subscribe(contactDevice, "contact.closed", contactChange)
	}
}

def contactChange(evt){
	if(contactDisable || state.contactDisable) return

	logTrace("$app.label (line 340) -- Contact sensor $evt.displayName $evt.value")
	
	// If mode set and node doesn't match, return null
	if(ifMode && location.mode != ifMode) {
		logTrace("$app.label (line 342) -- Contact sensor not triggered; mode location.mode doesn't match ifMode")
		return
	}

	// Set timeStart and timeStop, if sunrise or sunset
	if(timeStartSunrise) timeStart = parent.getSunrise(timeStartOffset,timeStartOffsetNegative,app.label)
	if(timeStartSunset) timeStart = parent.getSunset(timeStartOffset,timeStartOffsetNegative,app.label)
	if(timeStopSunrise) timeStop = parent.getSunrise(timeStopOffset,timeStopOffsetNegative,app.label)
	if(timeStopSunset) timeStop = parent.getSunset(timeStopOffset,timeStopOffsetNegative,app.label)

	// if not between start and stop time
	if(timeStop){
		if(!parent.timeBetween(timeStart, timeStop,app.label)) return
	}
	
	// If not correct day, return null
	if(timeDays && !parent.todayInDayList(timeDays,app.label)) {
		logTrace("$app.label (line 359) -- Contact sensor not triggered; not correct day")
		return
	}

	// Unschedule pevious events
	
	// New open event resets delayed action
	// New close event won't override open
	if(evt.value == "open"){
		unschedule()
	} else {
		unschedule(scheduleClose)
	}

	// Check if people are home (home1 and home2 should be true
	if(personHome){
		home1 = false
		personHome.each{
			if(it.currentPresence == "present") home1 = true
		}
	}
	if(personNotHome){
		home2 = true
		personNotHome.each{
			if(it.currentPresence == "present") home2 = false
		}
	}

	// Text first (just in case there's an error later)
/* ************************************************** */
/* TO DO: Instead of throwing error (in Master),      */
/* validate number on setup.                          */
/* ************************************************** */
	if(phone && ((openOrClose && evt.value == "open") || (!openOrClose && evt.value == "closed"))){
		// Only if correct people are home/not home
		if((personHome && personNotHome && home1 && home2) || (personHome && !personNotHome && home1) || (!personHome && personNotHome && home2) || (!personHome && !personNotHome)){	
			def now = new Date()

			//if last text was sent less than 5 minutes ago, don't send
/* ************************************************** */
/* TO-DO: Add option to override text cooldown        */
/* period? (Maybe in Master?) Migrate new code to     */
/* presence app.                                      */
/* ************************************************** */
			// Compute seconds from last sms
			seconds = (now.getTime()  - state.contactLastSms) / 1000

			// Convert date to friendly format for log
			now = now.format("h:mm a", location.timeZone)
			if(seconds > 360){
				state.contactLastSms = new Date().getTime()

					if(evt.value == "open"){
						parent.sendText(phone,"$evt.displayName was opened at $now.",app.label)
						log.info "$app.label -- Sent SMS for $evt.displayName opened at $now."
					} else {
						parent.sendText(phone,"$evt.displayName was closed at $now.",app.label)
						log.info "$app.label -- Sent SMS for $evt.displayName closed at $now."
					}
			} else {
				log.info("app.label -- $evt.displayName was closed at $now. SMS not sent due to only being $seconds since last SMS.")
			}
		}
	}

	// Give voice alert
	if(speakText && ((openOrClose && evt.value == "open") || (!openOrClose && evt.value == "closed"))) {
		// Only if correct people are home/not home
		if((personHome && personNotHome && home1 && home2) || (personHome && !personNotHome && home1) || (!personHome && personNotHome && home2) || (!personHome && !personNotHome)){	
			parent.speak(speakText,app.label)
		}
	}

	// Set mode
	if(mode && ((openOrClose && evt.value == "open") || (!openOrClose && evt.value == "closed"))) parent.changeMode(mode,app.label)

	// Perform open events (for switches and locks)
	if(evt.value == "open"){
		// Schedule delay
		if(openWait) {
			logTrace("$app.label (line 437) -- Scheduling scheduleOpen in $openWait seconds")
			runIn(openWait,scheduleOpen)
		// Otherwise perform immediately
		} else {
			if(switches) {
				if(actionOpenSwitches == "on") {
					parent.multiOn(switches,app.label)
				} else if(actionOpenSwitches == "off"){
					parent.multiOff(switches,app.label)
				} else if(actionOpenSwitches == "toggle"){
					parent.toggle(switches,app.label)
				}
			}
			if(locks){
				if(actionOpenLocks == "lock"){
					parent.multiLock(locks,app.label)
				} else if(actionOpenLocks == "unlock"){
					parent.multiUnlock(locks,app.label)
				}
			}
		}

	// Perform close events (for switches and locks)
	} else {
		// Schedule delay
		if(closeWait) {
			logTrace("$app.label (line 463) -- Scheduling scheduleClose in $closeWait seconds")
			runIn(closeWait,scheduleClose)
		// Otherwise perform immediately
		} else {
			if(switches) {
				if(actionCloseSwitches == "on") {
					parent.multiOn(switches,app.label)
				} else if(actionCloseSwitches == "off"){
					parent.multiOff(switches,app.label)
				} else if(actionCloseSwitches == "toggle"){
					parent.toggle(switches,app.label)
				}
			}
			if(locks){
				if(actionCloseLocks == "lock"){
					parent.multiLock(locks,app.label)
				} else if(actionCloseLocks == "unlock"){
					parent.multiUnlock(locks,app.label)
				}
			}
		}
	}
}

def scheduleOpen(){
	if(contactDisable || state.contactDisable) return

	if(switches) {
		if(actionOpenSwitches == "on") {
			parent.multiOn(switches,app.label)
		} else if(actionOpenSwitches == "off"){
			parent.multiOff(switches,app.label)
		} else if(actionOpenSwitches == "toggle"){
			parent.toggle(switches,app.label)
		}
	}
	if(locks){
		if(actionOpenLocks == "lock"){
			parent.multiLock(locks,app.label)
		} else if(actionOpenLocks == "unlock"){
			parent.multiUnlock(locks,app.label)
		}
	}
}

def scheduleClose(){
	if(contactDisable || state.contactDisable) return

	if(switches) {
		if(actionCloseSwitches == "on") {
			parent.multiOn(switches,app.label)
		} else if(actionCloseSwitches == "off"){
			parent.multiOff(switches,app.label)
		} else if(actionCloseSwitches == "toggle"){
			parent.toggle(switches,app.label)
		}
	}
	if(locks){
		if(actionCloseLocks == "lock"){
			parent.multiLock(locks,app.label)
		} else if(actionCloseLocks == "unlock"){
			parent.multiUnlock(locks,app.label)
		}
	}
}

def logTrace(message){
	//log.trace message
}
