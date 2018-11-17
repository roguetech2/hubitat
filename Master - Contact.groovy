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
*  Version: 0.1.01
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
				label title: "<b>Assign a name:</b>", required: true
				paragraph "<font color=\"#000099\"><b>Select which sensor(s):</b></font>"
				input "contactDevice", "capability.contactSensor", title: "Contact Sensor(s)", multiple: true, required: true
				input "contactDisable", "bool", title: "<b><font color=\"#000099\">This contact sensor is disabled.</font></b> Reenable it?", submitOnChange:true
			} else {
			
				label title: "<b>Assign a name:</b>", required: true
				paragraph "<font color=\"#000099\"><b>Select which sensor(s):</b></font>"
				input "contactDevice", "capability.contactSensor", title: "Contact Sensor(s)", multiple: true, required: true
				input "contactDisable", "bool", title: "<b><font color=\"#000099\">Disable this contact sensor?</font></b>", submitOnChange:true

				if(contactDevice){
					paragraph "• <font color=\"#000099\"><b>When opened</b></font>"
					input "contactSetModeOpen", "mode", title: "Set Mode to:", required: false
					input "contactOpen1On", "capability.switch", title: "Turn On", multiple: true, required: false
					input "contactOpen1Off", "capability.switch", title: "Turn Off", multiple: true, required: false
					input "contactOpen1Toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
					input "contactOpenWait", "number", required: false, title: "<b>Then after seconds</b> (Optional. Default 0.)"
					input "contactOpen2On", "capability.switch", title: "Turn On", multiple: true, required: false
					input "contactOpen2Off", "capability.switch", title: "Turn Off", multiple: true, required: false
					input "contactOpen2Toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false

					paragraph "• <font color=\"#000099\"><b>When closed</b></font>"
					input "contactSetModeClose", "mode", title: "Set Mode to:", required: false
					input "contactClose1On", "capability.switch", title: "Turn On", multiple: true, required: false
					input "contactClose1Off", "capability.switch", title: "Turn Off", multiple: true, required: false
					input "contactClose1Toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
					input "contactCloseWait", "number", required: false, title: "<b>Then after seconds</b> (Optional. Default 0.)"
					input "contactClose2On", "capability.switch", title: "Turn On", multiple: true, required: false
					input "contactClose2Off", "capability.switch", title: "Turn Off", multiple: true, required: false
					input "contactClose2Toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
					paragraph " ", width: 12
					input "contactIfMode", "mode", title: "<b>Only if Mode is already:</b> (Optional)", required: false, width: 12
					paragraph "• <font color=\"#000099\"><b>Only if time is between:</b></font>"
					input "contactStart", "time", title: "<b>Start time</b> (12:00AM if all day; Optional)", required: true, width: 6
					input "contactStop", "time", title: "<b>Stop time</b> (11:59PM for remaining day; Optional)", required: false, width: 6
					input "contactDisableAll", "bool", title: "Disable <b>ALL</b> contact sensors?", defaultValue: false, submitOnChange:true
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
	unschedule(scheduleOpen)
	unschedule(scheduleClose)
    log.debug "Contact: $evt.displayName contact sensor $evt.value"

	if(contactOpenWait){
		if(!contactStart || now() > timeToday(contactStart, location.timeZone).time){
			if(!contactStop || now() < timeToday(contactStop, location.timeZone).time){
				runIn(contactOpenWait,scheduleOpen)
			}
		}
	}
	if(!timeStartIfMode || location.mode == timeStartIfMode) {
		if(!contactStart || now() > timeToday(contactStart, location.timeZone).time){
			if(!contactStop || now() < timeToday(contactStop, location.timeZone).time){
				if(contactOpen1On) parent.multiOn(contactOpen1On,appId)
				if(contactOpen1Off) parent.multiOff(contactOpen1Off,appId)
				if(contactOpen1Toggle) parent.toggle(contactOpen1Toggle,appId)
				if(contactSetModeOpen) setLocationMode(contactSetModeOpen,appId)
			}
		}
	}
}

def contactClosed(evt){
	if(contactDisable || state.contactDisableAll) return
	def appId = app.getId()
	unschedule(scheduleClose)
    log.debug "Contact: $evt.displayName contact sensor $evt.value"

	if(contactCloseWait){
		if(!contactStart || now() > timeToday(contactStart, location.timeZone).time){
			if(!contactStop || now() < timeToday(contactStop, location.timeZone).time){
				runIn(contactCloseWait,scheduleClose)
			}
		}
	}
	if(!timeStartIfMode || location.mode == timeStartIfMode) {
		if(!contactStart || now() > timeToday(contactStart, location.timeZone).time){
			if(!contactStop || now() < timeToday(contactStop, location.timeZone).time){
				if(contactClose1On) parent.multiOn(contactClose1On,appId)
				if(contactClose1Off) parent.multiOff(contactClosen1Off,appId)
				if(contactClose1Toggle) parent.toggle(contactClose1Toggle,appId)
				if(contactSetModeClose) setLocationMode(contactSetModeClose,appId)
			}
		}
	}
}

def scheduleOpen(){
	def appId = app.getId()
	if(timeStartIfMode && location.mode != timeStartIfMode) return
	if(contactOpen2On) parent.multiOn(contactOpen2On,appId)
	if(contactOpen2Off) parent.multiOff(contactOpen2Off,appId)
	if(contactOpen2Toggle) parent.toggle(contactOpen2Toggle,appId)
	if(contactSetModeOpen) setLocationMode(contactSetModeOpen,appId)
}

def scheduleClose(){
	def appId = app.getId()
	if(timeStartIfMode && location.mode != timeStartIfMode) return
	if(contactClose2On) parent.multiOn(contactClose2On,appId)
	if(contactClose2Off) parent.multiOff(contactClose2Off,appId)
	if(contactClose2Toggle) parent.toggle(contactClose2Toggle,appId)
	if(contactSetModeClose) setLocationMode(contactSetModeClose,appId)
}
