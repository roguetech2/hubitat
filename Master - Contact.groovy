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
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master - Contact.groovy
*  Version: 0.3.18
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

preferences {
	infoIcon = "<img src=\"http://files.softicons.com/download/system-icons/windows-8-metro-invert-icons-by-dakirby309/ico/Folders%20&%20OS/Info.ico\" width=20 height=20>"
    page(name: "setup", install: true, uninstall: true) {
		
        section() {
			// Set disable all
			if(timeDisableAll) {
				state.contactDisable = true
			} else {
				state.contactDisable = false
			}
			
			// If all disabled, force reenable
			if(state.timeDisable){
				input "contactDisableAll", "bool", title: "All contact sensors are disabled. Reenable?", defaultValue: false, submitOnChange:true
			} else if(contactDisable){
				paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this presence routine:</b></div>"
				label title: "", required: true
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
						if(!switches && !locks) input "noDevice", "bool", title: "<b>No devices.</b> Click to continue if ONLY setting mode, or sending text alert.", defaultValue: false, submitOnChange:true
						if(!noDevice) input "switches", "capability.switchLevel", title: "Lights/switches?", multiple: true, required: false, submitOnChange:true
						if(!noDevice) input "locks", "capability.lock", title: "Locks?", multiple: true, required: false, submitOnChange:true
						if((!switches && !locks) && !noDevice) {
							paragraph "<div style=\"background-color:BurlyWood\"> </div>"
						} else if((switches || locks) || noDevice) {
							paragraph "<div style=\"background-color:BurlyWood\"><b> Select what to do with switches or locks:</b></div>"
							if(switches) {
								input "actionOpenSwitches", "enum", title: "What to do with lights/switches on open? (Optional)", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
								input "actionCloseSwitches", "enum", title: "What to do with lights/switches on close? (Optional)", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
							}
							if(locks) {
								input "actionOpenLocks", "enum", title: "What to do with locks on open? (Optional)", required: false, multiple: false, width: 6, options: ["lock":"Lock", "unlock":"Unlock"], submitOnChange:true
								input "actionCloseLocks", "enum", title: "What to do with locks on close? (Optional)", required: false, multiple: false, width: 6, options: ["lock":"Lock", "unlock":"Unlock"], submitOnChange:true
							}
							input "mode", "mode", title: "Change Mode? (Optional)", required: false, submitOnChange:true
							input "phone", "phone", title: "Number to text alert? (Optional)", required: false, submitOnChange:true
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
	logTrace("$app.label: installed")
	if(app.getLabel().length() < 7)  app.updateLabel("Contact - " + app.getLabel())
	if(app.getLabel().substring(0,7) != "Contact") app.updateLabel("Contact - " + app.getLabel())
	initialize()
}

def updated() {
	logTrace("$app.label: updated")
	unsubscribe()
	initialize()
}

def initialize() {
	logTrace("$app.label: initialized")
	unschedule()
	
	// If date/time for last SMS not set, initialize it to 5 minutes ago
	// Allows an SMS immediately
	if(!state.contactLastSms) state.contactLastSms = new Date().getTime() - 360000

	if(!contactDisable && !state.contactDisableAll) {
		subscribe(contactDevice, "contact.open", contactChange)
		subscribe(contactDevice, "contact.closed", contactChange)
	}
}

def contactChange(evt){
	def appId = app.getId()
	logTrace("$app.label: function contactChange started [evt: $evt ($evt.value)]")
	if(contactDisable || state.contactDisableAll) {
		logTrace("$app.label: function contactChange  returning (contact disabled)")
		return
	}
	
	// If mode set and node doesn't match, return null
	if(ifMode && location.mode != ifMode) {
		logTrace("$app.label, $appId: function contactChange returning (mode doesn't match)")
		return
	}

	// Set timeStart and timeStop, if sunrise or sunset
	if(timeStartSunrise) timeStart = parent.getSunrise(timeStartOffset,timeStartOffsetNegative)
	if(timeStartSunset) timeStart = parent.getSunset(timeStartOffset,timeStartOffsetNegative)
	if(timeStopSunrise) timeStop = parent.getSunrise(timeStopOffset,timeStopOffsetNegative)
	if(timeStopSunset) timeStop = parent.getSunset(timeStopOffset,timeStopOffsetNegative)

	// if not between start and stop time
	if(timeStop){
		if(!parent.timeBetween(timeStart, timeStop)) {
			logTrace("$app.label: function contactChange returning (not between start time and stop time)")
			return
		}
	}
	
	// If not correct day, return null
	if(timeDays && !parent.todayInDayList(timeDays)) {
		logTrace("$app.label: function contactChange returning (not correct day)")
		return
	}

	// Unschedule pevious events
	
	// New open event resets delayed action
	// New close event won't override open
	if(evt.value == "open"){
		logTrace("$app.label: function contactChange unscheduling all")
		unschedule()
	} else {
		logTrace("$app.label: function contactChange unscheduling scheduleClose")
		unschedule(scheduleClose)
	}
	
	// Text first (just in case there's an error later)
/* ************************************************** */
/* TO DO: Instead of throwing error (in Master),      */
/* validate number on setup.                          */
/* ************************************************** */
	if(phone){
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
				if(parent.sendText(phone,"$evt.displayName was opened at $now.")){
					log.info "Sent SMS for $evt.displayName opening at $now."
				} else {
					logTrace("$app.label: function contactChange failed to send SMS for $evt.displayName opening")
				}
			} else {
				if(parent.sendText(phone,"$evt.displayName was closed at $now.")){
					log.info "Sent SMS for $evt.displayName closed at $now."
				} else {
					logTrace("$app.label: function contactChange failed to send SMS for $evt.displayName closing")
				}
			}
		} else {
			log.info("$evt.displayName was closed at $now. SMS not sent due to only being $seconds since last SMS.")
		}
	}

	// Set mode
	if(mode) parent.changeMode(mode, appId)

	// Perform open events (for switches and locks)
	if(evt.value == "open"){
		// Schedule delay
		if(openWait) {
			logTrace("$app.label: function contactChange scheduling scheduleOpen in $openWait seconds")
			runIn(openWait,scheduleOpen)
		// Otherwise perform immediately
		} else {
			if(switches) {
				if(actionOpenSwitches == "on") {
					parent.multiOn(switches,appId)
				} else if(actionOpenSwitches == "off"){
					parent.multiOff(switches,appId)
				} else if(actionOpenSwitches == "toggle"){
					parent.toggle(switches,appId)
				}
			}
			if(locks){
				if(actionOpenLocks == "lock"){
					parent.multiLock(locks,appId)
				} else if(actionOpenLocks == "unlock"){
					parent.multiUnlock(locks,appId)
				}
			}
		}

	// Perform close events (for switches and locks)
	} else {
		// Schedule delay
		if(closeWait) {
			logTrace("$app.label: function contactChange scheduling scheduleClose in $closeWait seconds")
			runIn(closeWait,scheduleClose)
		// Otherwise perform immediately
		} else {
			if(switches) {
				if(actionCloseSwitches == "on") {
					parent.multiOn(switches,appId)
				} else if(actionCloseSwitches == "off"){
					parent.multiOff(switches,appId)
				} else if(actionCloseSwitches == "toggle"){
					parent.toggle(switches,appId)
				}
			}
			if(locks){
				if(actionCloseLocks == "lock"){
					parent.multiLock(locks,appId)
				} else if(actionCloseLocks == "unlock"){
					parent.multiUnlock(locks,appId)
				}
			}
		}
	}
	logTrace("$app.label: function contactChange exiting")
}

def scheduleOpen(){
	def appId = app.getId()
	logTrace("$app.label: function scheduleOpen started")

	if(switches) {
		if(actionOpenSwitches == "on") {
			parent.multiOn(switches,appId)
		} else if(actionOpenSwitches == "off"){
			parent.multiOff(switches,appId)
		} else if(actionOpenSwitches == "toggle"){
			parent.toggle(switches,appId)
		}
	}
	if(locks){
		if(actionOpenLocks == "lock"){
			parent.multiLock(locks,appId)
		} else if(actionOpenLocks == "unlock"){
			parent.multiUnlock(locks,appId)
		}
	}
	logTrace("$app.label: function scheduleOpen exiting")
}

def scheduleClose(){
	def appId = app.getId()
	logTrace("$app.label: function scheduleClose started")

	if(switches) {
		if(actionCloseSwitches == "on") {
			parent.multiOn(switches,appId)
		} else if(actionCloseSwitches == "off"){
			parent.multiOff(switches,appId)
		} else if(actionCloseSwitches == "toggle"){
			parent.toggle(switches,appId)
		}
	}
	if(locks){
		if(actionCloseLocks == "lock"){
			parent.multiLock(locks,appId)
		} else if(actionCloseLocks == "unlock"){
			parent.multiUnlock(locks,appId)
		}
	}
	logTrace("$app.label: function scheduleClose exiting")
}

def logTrace(message){
	//log.trace message
}
