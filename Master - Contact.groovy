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
*  Version: 0.3.01
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
									if(timeStop){
										input "timeStart", "time", title: "Between start time (12:00AM if all day)", required: false, width: 6, submitOnChange:true
									} else {
										input "timeStart", "time", title: "Between start time (12:00AM if all day; Optional)", required: false, width: 6, submitOnChange:true
									}
									if(timeStart){
										input "timeStop", "time", title: "and stop time (11:59PM for remaining day)", required: false, width: 6, submitOnChange:true
									} else {
										input "timeStop", "time", title: "and stop time (11:59PM for remaining day; Optional)", required: false, width: 6, submitOnChange:true
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

	// If before start time, return null
	if(timeStart){
		if(now() < timeToday(timeStart, location.timeZone).time) return defaults
	}

	// If after time stop, return null
	if(timeStop){
		if(now() > timeToday(timeStop, location.timeZone).time) return defaults
	}
	
	// If not correct day, return null
	if(timeDays){
		def df = new java.text.SimpleDateFormat("EEEE")
		df.setTimeZone(location.timeZone)
		def day = df.format(new Date())
		if(!timeDays.contains(day)) return null
	}

    log.debug "Contact: $evt.displayName contact sensor $evt.value"

	// Unschedule pevious events
	unschedule(scheduleOpen)
	unschedule(scheduleClose)

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

	// If before start time, return null
	if(timeStart){
		if(now() < timeToday(timeStart, location.timeZone).time) return defaults
	}

	// If after time stop, return null
	if(timeStop){
		if(now() > timeToday(timeStop, location.timeZone).time) return defaults
	}
	
	// If not correct day, return null
	if(timeDays){
		def df = new java.text.SimpleDateFormat("EEEE")
		df.setTimeZone(location.timeZone)
		def day = df.format(new Date())
		if(!timeDays.contains(day)) return null
	}

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
