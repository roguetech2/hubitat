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
*  Version: 0.3.06
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
				input "contact", "capability.contactSensor", title: "Contact Sensor(s)", multiple: true, required: true
				input "contactDisable", "bool", title: "<b><font color=\"#000099\">This contact sensor is disabled.</font></b> Reenable it?", submitOnChange:true
			} else {
				paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this contact sensor routine:</b></div>"
				label title: "", required: true, submitOnChange:true
				if(!app.label){
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				} else if(app.label){
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select door sensors for routine:</b></div>"
					input "contact", "capability.contactSensor", title: "Contact sensor(s)?", multiple: true, required: true, submitOnChange:true
					input "contactDisable", "bool", title: "Disable this contact sensor?", submitOnChange:true
					if(!contact){
						paragraph "<div style=\"background-color:BurlyWood\"> </div>"
					} else if(contact){
						paragraph "<div style=\"background-color:BurlyWood\"><b>Select which devices to control:</b></div>"
						if(!switches && !locks) input "noDevice", "bool", title: "<b>No devices.</b> Click to continue if ONLY setting mode, or sending text alert.", defaultValue: false, submitOnChange:true
						if(!noDevice) input "switches", "capability.switchLevel", title: "Lights/switches?", multiple: true, required: false, submitOnChange:true
						if(!noDevice) input "locks", "capability.lock", title: "Locks?", multiple: true, required: false, submitOnChange:true
						if((!switches && !locks) || noDevice) {
							paragraph "<div style=\"background-color:BurlyWood\"> </div>"
						} else if((switches || locks) && !noDevice) {
							paragraph "<div style=\"background-color:BurlyWood\"><b> Select what to do with switches or locks:</b></div>"
							if(switches) {
								input "actionOpenSwitches", "enum", title: "What to do with lights/switches on open? (Optional)", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
								input "actionCloseSwitches", "enum", title: "What to do with lights/switches on close? (Optional)", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
							}
							if(locks) {
								input "actionOpenLocks", "enum", title: "What to do with locks on open? (Optional)", required: false, multiple: false, width: 6, options: ["Unlock":"Unlock", "lock":"Lock"], submitOnChange:true
								input "actionCloseLocks", "enum", title: "What to do with locks on close? (Optional)", required: false, multiple: false, width: 6, options: ["Unlock":"Unlock", "lock":"Lock"], submitOnChange:true
							}
							input "mode", "mode", title: "Change Mode? (Optional)", required: false, submitOnChange:true
							input "phone", "phone", title: "Number to text alert? (Optional)", required: false, submitOnChange:true
							if(!actionOpenSwitches && !actionCloseSwitches && !actionOpenLocks && !actionCloseLocks && !mode && !phone) {
								paragraph "<div style=\"background-color:BurlyWood\"> </div>"
							} else {
								paragraph "<div style=\"background-color:BurlyWood\"><b> Set wait time before setting lights:</b></div>"
								if(!openWait && !closeWait) input "noWaitTime", "bool", title: "<b>No pause.</b> Click to continue to set schedule and mode.", defaultValue: false, submitOnChange:true
								if(!noWaitTime){
									if(!noWaitTime) input "openWait", "number", title: "Wait seconds for opening action.", defaultValue: false, width: 6, submitOnChange:true
									if(!noWaitTime) input "closeWait", "number", title: "Wait seconds for closing action.", defaultValue: false, width: 6, submitOnChange:true
								}

								if(!openWait && !closeWait && !noWaitTime){
									paragraph "<div style=\"background-color:BurlyWood\"> </div>"
								} else {
									paragraph "<div style=\"background-color:BurlyWood\"><b> Select time or mode (Optional):</b></div>"
									if(!timeStartSunrise && !timeStartSundown){
										if(timeStop){
											input "timeStart", "time", title: "Between start time", required: false, width: 6, submitOnChange:true
										} else {
											input "timeStart", "time", title: "Between start time (Optional)", required: false, width: 6, submitOnChange:true
										}
									} else if(timeStartSunrise) {
										paragraph "Between sunrise", width: 6
									} else if(timeStartSundown){
										paragraph "Between sundown", width: 6
									}
									if(!timeStopSunrise && !timeStopSundown){
										if(timeStart){
											input "timeStop", "time", title: "and stop time", required: false, width: 6, submitOnChange:true
										} else {
											input "timeStop", "time", title: "and stop time (Optional)", required: false, width: 6, submitOnChange:true
										}
									} else if(timeStopSunrise){
										paragraph "and sunrise", width: 6
									} else if(timeStopSundown){
										paragraph "and sundown", width: 6
									}
									if(!timeStartSundown){
										input "timeStartSunrise", "bool", title: "Start at sunrise?", width: 6, submitOnChange:true
									} else {
										paragraph " ", width: 6
									}
									if(!timeStopSundown) {
										input "timeStopSunrise", "bool", title: "Stop at sunrise?", width: 6, submitOnChange:true
									} else {
										paragraph " ", width: 6
									}
									if(!timeStartSunrise){
										input "timeStartSundown", "bool", title: "Start at sundown?", width: 6, submitOnChange:true
									} else {
										paragraph " ", width: 6
									}
									if(!timeStopSunrise){
										input "timeStopSundown", "bool", title: "Stop at sundown?", width: 6, submitOnChange:true
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
	if(app.getLabel().length() < 7)  app.updateLabel("Contact - " + app.getLabel())
    if(app.getLabel().substring(0,7) != "Contact") app.updateLabel("Contact - " + app.getLabel())
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
    log.debug "Contact initialized"
	unschedule(scheduleOpen)
	unschedule(scheduleClose)
	
	if(!contactDisable && !state.contactDisableAll) {
		subscribe(contactDevice, "contact.open", contactOpen)
		subscribe(contactDevice, "contact.closed", contactClosed)
	}
}

def contactOpen(evt){
	if(contactDisable || state.contactDisableAll) return
	def appId = app.getId()

	// If presence is disabled, return null
	if(state.presenceDisable || presenceDisable) return
	
	// If mode set and node doesn't match, return null
	if(ifMode && location.mode != ifMode) return

	if(timeStartSunrise) timeStart = parent.getSunrise()
	if(timeStartSundown) timeStart = parent.getSundown()
	if(timeStopSunrise) timeStop = parent.getSunrise()
	if(timeStopSundown) timeStop = parent.getSundown()

	// if not between start and stop time
	if(timeStop){
		if(!parent.timeBetween(timeStart, timeStop)) return
	}
	
	// If not correct day, return null
	if(timeDays && !parent.todayInDayList(timeDays)) return

    log.debug "Contact: $evt.displayName contact sensor $evt.value"

	// Unschedule pevious events
	unschedule(scheduleOpen)
	unschedule(scheduleClose)
	
	// Text first (just in case there's an error later)
	/* ********************************** */
	/* TO DO: Instead of throwing error   */
	/* here, validate number on setup     */
	/* Also add to Master - Contact       */
	/* ********************************** */
	if(phone){
		def now = new Date()
		now = now.format("h:mm a", location.timeZone)
		if(evt.value == "present"){
			if(parent.sendText(phone,"$evt.displayName arrived at the house $now.")){
				log.debug "Sent SMS for $evt.displayName's arrival at $now."
			} else {
				log.debug "Error sending SMS for $evt.displayName's arrival at $now."
			}
		} else {
			if(parent.sendText(phone,"$evt.displayName left the house at $now.")){
				log.debug "Sent SMS for $evt.displayName's departure at $now."
			} else {
				log.debug "Error sending SMS for $evt.displayName's departure at $now."
			}
		}
	}

	// Set mode
	if(mode) parent.changeMode(mode, appId)

	// Schedule open events
	if(openWait) {
		runIn(openWait,scheduleOpen)
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
		/* ***************************************** */
		/* TO DO: Build lock code                    */
		/* ***************************************** */
	}
}

def contactClosed(evt){
	if(contactDisable || state.contactDisableAll) return
	def appId = app.getId()
	
	// If presence is disabled, return null
	if(state.presenceDisable || presenceDisable) return
	
	// If mode set and node doesn't match, return null
	if(ifMode && location.mode != ifMode) return

	if(timeStartSunrise) timeStart = parent.getSunrise()
	if(timeStartSundown) timeStart = parent.getSundown()
	if(timeStopSunrise) timeStop = parent.getSunrise()
	if(timeStopSundown) timeStop = parent.getSundown()

	// if not between start and stop times
	if(!parent.timeBetween(timeStart, timeStop)) return

	// If not correct day, return null
	if(timeDays && !parent.todayInDayList(timeDays)) return

    log.debug "Contact: $evt.displayName contact sensor $evt.value"

	// Unschedule pevious close events
	unschedule(scheduleClose)

	// Schedule open events
	if(closeWait) {
		runIn(closeWait,scheduleClose)
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
		/* ***************************************** */
		/* TO DO: Build lock code                    */
		/* ***************************************** */
	}
}

def scheduleOpen(){
	def appId = app.getId()

	if(switches) {
		if(actionOpenSwitches == "on") {
			parent.multiOn(switches,appId)
		} else if(actionOpenSwitches == "off"){
			parent.multiOff(switches,appId)
		} else if(actionOpenSwitches == "toggle"){
			parent.toggle(switches,appId)
		}
	}
	/* ***************************************** */
	/* TO DO: Build lock code                    */
	/* ***************************************** */
}

def scheduleClose(){
	def appId = app.getId()

	if(switches) {
		if(actionCloseSwitches == "on") {
			parent.multiOn(switches,appId)
		} else if(actionCloseSwitches == "off"){
			parent.multiOff(switches,appId)
		} else if(actionCloseSwitches == "toggle"){
			parent.toggle(switches,appId)
		}
	}
	/* ***************************************** */
	/* TO DO: Build lock code                    */
	/* ***************************************** */
}
