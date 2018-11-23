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
*  Name: Master - Presence
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master - Presence.groovy
*  Version: 0.1.01
*
***********************************************************************************************************************/

definition(
    name: "Master - Presence",
    namespace: "master",
    author: "roguetech",
    description: "Presence - Arriving and Leaving",
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
				state.timeDisable = true
			} else {
				state.timeDisable = false
			}

			// If all disabled, force reenable
			if(state.timeDisable){
				input "timeDisableAll", "bool", title: "All schedules are disabled. Reenable?", defaultValue: false, submitOnChange:true
			} else if(presenceDisable){
				paragraph "<div style=\"background-color:BurlyWood\"><b> Select name for this presence routine:</b></div>"
				label title: "Routine name?", required: true, submitOnChange:true
				paragraph "<div style=\"background-color:BurlyWood\"><b> Select which people:</b></div>"
				input "person", "capability.presenceSensor", title: "Person/people", multiple: true, required: true, submitOnChange:true
				if(presenceDisable){
					input "presenceDisable", "bool", title: "<b><font color=\"#000099\">Presence is disabled.</font></b> Reenable it?", submitOnChange:true
				} else {
					input "presenceDisable", "bool", title: "Disable this presence?", submitOnChange:true
				}
				input "timeDisableAll", "bool", title: "Disable <b>ALL</b> presence routines?", defaultValue: false, submitOnChange:true

			} else {
				paragraph "<div style=\"background-color:BurlyWood\"><b> Select name for this presence routine:</b></div>"
				label title: "Routine name?", required: true, submitOnChange:true
				if(app.label){
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select everyone in the house:</b></div>"
					input "everyone", "capability.presenceSensor", title: "Everyone (check all):", multiple: true, required: true, submitOnChange:true
					if(everyone){
						paragraph "<div style=\"background-color:BurlyWood\"><b> Select people for this routine:</b></div>"
						input "person", "capability.presenceSensor", title: "Person/people", multiple: true, required: true, submitOnChange:true
						input "presenceDisable", "bool", title: "Disable this presence?", submitOnChange:true
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
									if(!switches && !locks)	input "noDevice", "bool", title: "<b>No devices.</b> Click to continue if only setting mode or sending text alert.", defaultValue: false, submitOnChange:true
									if(!noDevice) input "switches", "capability.switchLevel", title: "Lights and switches?", multiple: true, required: false, submitOnChange:true
									if(!noDevice) input "locks", "capability.lock", title: "Locks?", multiple: true, required: false, submitOnChange:true
									if(!switches && !locks && !noDevice)	{
										paragraph "<div style=\"background-color:BurlyWood\"> </div>"
									} else if(switches || locks || noDevice){
										paragraph "<div style=\"background-color:BurlyWood\"><b> Select what to do:</b></div>"
										if(switches) input "actionSwitches", "enum", title: "What to do with lights/switches? (optional)", required: false, multiple: false, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
										if(switches) {
											if(flashColor) {
												input "flashColor", "color", title: "Flash all color lights? (optional)", required: false, default: flashColor
											} else {
												input "flashColor", "color", title: "Flash all color lights? (optional)", required: false
											}
										}
										if(locks) input "actionLocks", "enum", title: "What to do with locks? (optional)", required: false, multiple: false, options: ["Unlock":"Unlock", "lock":"Lock"], submitOnChange:true
										input "mode", "mode", title: "Set Mode? (optional)", required: false, submitOnChange:true
										input "phone", "phone", title: "Number to text alert? (optional)", required: false, submitOnChange:true
										if(!actionSwitches && !actionLocks && !mode && !phone && !flashColor) {
											paragraph "<div style=\"background-color:BurlyWood\"> </div>"
										} else {
											paragraph "<div style=\"background-color:BurlyWood\"><b> Select time or mode (optional):</b></div>"
											if(timeStop){
												input "timeStart", "time", title: "Between start time (12:00AM if all day)", required: false, width: 6, submitOnChange:true
											} else {
												input "timeStart", "time", title: "Between start time (12:00AM if all day; Optional)", required: false, width: 6, submitOnChange:true
											}
											if(timeStart){
												input "timeStop", "time", title: "and stop time (11:59PM for remaining day)", required: true, width: 6, submitOnChange:true
											} else {
												input "timeStop", "time", title: "and stop time (11:59PM for remaining day; Optional)", required: false, width: 6, submitOnChange:true
											}
											input "ifMode", "mode", title: "Only if the Mode is already:</b> (Optional)", required: false, width: 12
										}
									}
								}
							}
						}
					}
				}
				paragraph " "
				input "timeDisableAll", "bool", title: "Disable <b>ALL</b> presence routines?", defaultValue: false, submitOnChange:true
			}
		}
	}
}

def installed() {
	if(app.getLabel().length() < 8)  app.updateLabel("Presence - " + app.getLabel())
    if(app.getLabel().substring(0,8) != "Presence") app.updateLabel("Presence - " + app.getLabel())
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    log.debug "Presence initialized"
    subscribe(person, "presence", presenceHandler)
}

def presenceHandler(evt) {
	def appId = app.getId()
    def person = evt.value

	if(state.disable || presenceDisable) return
	
	// If arrival or departure doesn't match, return null
	if( arrivingDeparting){
		if(evt.value != arrivingDeparting && arrivingDeparting != "both") return
	}
		
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

	// If occupied or unoccupied doesn't match, return null
	if(occupiedHome != "both"){
		occupied = false
		everyone.each{
			if(it == "present") occupied = true
		}
		if((occupiedHome == "unoccupied" && occupied) || (occupidHome == "occupied" && !occupied)) return
	}

	// Text first (just in case error)
	/* ***************************************** */
	/* TO DO                                     */
	/* ***************************************** */
	
	// Set mode
	if(mode) parent.changeMode(mode)
	
	// Turn on/off lights
	if(switches){
		if(actionSwitches == "on") {
			parent.multiOn(actionSwitches)
		} else if(actionSwtiches == "off"){
			parent.multiOff(actionSwitches)
		} else if(actionSwitches == "toggle"){
			parent.toggle(actionSwitches)
		}
	}

	// Lock/unlock doors
	/* ***************************************** */
	/* TO DO                                     */
	/* ***************************************** */

	// Flash alert
	/* ***************************************** */
	/* TO DO                                     */
	/* ***************************************** */
	if(flashColor) log.debug flashColor
}
