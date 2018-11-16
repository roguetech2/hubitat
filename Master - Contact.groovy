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
*  Version: 0.0.01
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
            label title: "<b>Assign a name:</b>", required: true
			paragraph "<font color=\"#000099\"><b>Select which sensor(s).</b></font>"
            input "contactDevice", "capability.contactSensor", title: "Contact Sensor", multiple: true, required: true
        }
		section("• <font color=\"#000099\"><b>When opened</b></font>"){
			input "contactSetModeOpen", "mode", title: "Set Mode to:", required: false
			input "contactOpen1On", "capability.switch", title: "Turn On", multiple: true, required: false
            input "contactOpen1Off", "capability.switch", title: "Turn Off", multiple: true, required: false
			input "contactOpen1Toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
			input "contactOpenWait", "number", required: false, title: "<b>Then after seconds</b> (Optional. Default 0.)"
			input "contactOpen2On", "capability.switch", title: "Turn On", multiple: true, required: false
            input "contactOpen2Off", "capability.switch", title: "Turn Off", multiple: true, required: false
			input "contactOpen2Toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
        }
        section("• <font color=\"#000099\"><b>When closed</b></font>"){
			input "contactSetModeClose", "mode", title: "Set Mode to:", required: false
			input "contactClose1On", "capability.switch", title: "Turn On", multiple: true, required: false
            input "contactClose1Off", "capability.switch", title: "Turn Off", multiple: true, required: false
			input "contactClose1Toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
			input "contactCloseWait", "number", required: false, title: "<b>Then after seconds</b> (Optional. Default 0.)"
			input "contactClose2On", "capability.switch", title: "Turn On", multiple: true, required: false
            input "contactClose2Off", "capability.switch", title: "Turn Off", multiple: true, required: false
			input "contactClose2Toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
        }
		section("• <font color=\"#000099\"><b>Only if:</b></font>"){
			input "contactIfMode", "mode", title: "<b>Mode is already:</b> (Optional)", required: false, width: 12
			input "contactStart", "time", title: "<b>Time is between</b> (12:00AM if all day; Optional)", required: true, width: 6
			input "contactStop", "time", title: "<b>and</b> (11:59PM for remaining day; Optional)", required: false, width: 6
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
    subscribe(contactDevice, "contact.open", contactOpen)
    subscribe(contactDevice, "contact.closed", contactClosed)
}

def contactOpen(evt){
	unschedule(scheduleOpen)
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
				if(contactOpen1On) parent.multiOn(contactOpen1On)
				if(contactOpen1Off) parent.multiOff(contactOpen1Off)
				if(contactOpen1Toggle) parent.toggle(contactOpen1Toggle)
				if(contactSetModeOpen) setLocationMode(contactSetModeOpen)
			}
		}
	}
}

def contactClosed(evt){
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
				if(contactClose1On) parent.multiOn(contactClose1On)
				if(contactClose1Off) parent.multiOff(contactClosen1Off)
				if(contactClose1Toggle) parent.toggle(contactClose1Toggle)
				if(contactSetModeClose) setLocationMode(contactSetModeClose)
			}
		}
	}
}

def scheduleOpen(){
	if(timeStartIfMode && location.mode != timeStartIfMode) return
	if(contactOpen2On) parent.multiOn(contactOpen2On)
	if(contactOpen2Off) parent.multiOff(contactOpen2Off)
	if(contactOpen2Toggle) parent.toggle(contactOpen2Toggle)
	if(contactSetModeOpen) setLocationMode(contactSetModeOpen)
}

def scheduleClose(){
	if(timeStartIfMode && location.mode != timeStartIfMode) return
	if(contactClose2On) parent.multiOn(contactClose2On)
	if(contactClose2Off) parent.multiOff(contactClose2Off)
	if(contactClose2Toggle) parent.toggle(contactClose2Toggle)
	if(contactSetModeClose) setLocationMode(contactSetModeClose)
}
