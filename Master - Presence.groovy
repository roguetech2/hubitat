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
*  Version: 0.1.05
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
					paragraph "<div style=\"background-color:BurlyWood\"><b> </b></div>"
				} else if(app.label){
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
								if(!switches && !locks)	input "noDevice", "bool", title: "<b>No devices.</b> Click to continue if ONLY flashing color lights, setting mode, sending text alert.", defaultValue: false, submitOnChange:true
								if(!noDevice) input "switches", "capability.switchLevel", title: "Lights and switches?", multiple: true, required: false, submitOnChange:true
								if(!noDevice) input "locks", "capability.lock", title: "Locks?", multiple: true, required: false, submitOnChange:true
								if(!switches && !locks && !noDevice)	{
									paragraph "<div style=\"background-color:BurlyWood\"> </div>"
								} else if(switches || locks || noDevice){
									paragraph "<div style=\"background-color:BurlyWood\"><b> Select what to do:</b></div>"
									if(switches) input "actionSwitches", "enum", title: "What to do with lights/switches? (Optional)", required: false, multiple: false, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
									if(locks) input "actionLocks", "enum", title: "What to do with locks? (Optional)", required: false, multiple: false, options: ["Unlock":"Unlock", "lock":"Lock"], submitOnChange:true
									if(flashColor) {
										input "flashColor", "color", title: "Flash all color lights? (Optional)", required: false, default: flashColor
									} else {
										input "flashColor", "color", title: "Flash all color lights? (Optional)", required: false
									}
									input "mode", "mode", title: "Change Mode? (Optional)", required: false, submitOnChange:true
									input "phone", "phone", title: "Number to text alert? (Optional)", required: false, submitOnChange:true
									if(!actionSwitches && !actionLocks && !mode && !phone && !flashColor) {
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
									}
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

def installed() {
	if(app.getLabel().length() < 11)  app.updateLabel("Presence - " + app.getLabel())
    if(app.getLabel().substring(0,11) != "Presence - ") app.updateLabel("Presence - " + app.getLabel())
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
	//TO DO: Add days to schedule
	def appId = app.getId()
    def person = evt.value

log.debug "name: $evt.name value: $evt.value diplayname: $evt.displayName evt: $evt"
	log.debug phone
	// If presence is disabled, return null
	if(state.presenceDisable || presenceDisable) return
	
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

	// Text first (just in case there's an error later)
	/* ********************************** */
	/* TO DO: Instead of throwing error   */
	/* here, valid number on setup        */
	/* Also add to Master - Contact       */
	/* ********************************** */
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
			log.debug "Error sneding SMS for $evt.displayName's departure at $now."
		}
	}

	// Set mode
	if(mode) parent.changeMode(mode, childId)
	
	// Turn on/off lights
	if(switches){
		if(actionSwitches == "on") {
			parent.multiOn(actionSwitches, childId)
		} else if(actionSwtiches == "off"){
			parent.multiOff(actionSwitches, childId)
		} else if(actionSwitches == "toggle"){
			parent.toggle(actionSwitches, childId)
		}
	}

	// Lock/unlock doors
	/* ***************************************** */
	/* TO DO: Build lock code                    */
	/* ***************************************** */

	// Flash alert
	/* ********************************** */
	/* TO DO: Need to create multi-level  */
	/* map to store current values        */
	/* ********************************** */
	/*
	if(flashColor) {
		colorMap = convertRgbToHsl(flashColor)
		hue = colorMap.hue
		sat = colorMap.sat
		level = colorMap.level
		parent.colorlights.each{

		currentHue = it.currentHue
		currentSat = it.currentSaturation
		currentLevel = it.currentLevel
		newValue = [hue: hue, saturation: sat]
		it.setColor(newValue)
	}
	pause(750)
*/
		// loop through multi-level map to restore old values.		
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
