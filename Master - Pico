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
*  Name: Master
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master - Pico.groovy
*  Version: 0.0.01
*
***********************************************************************************************************************/

definition(
    name: "Master - Pico",
    namespace: "master",
    author: "roguetech",
    description: "Pico and Caseta switches",
    parent: "master:Master",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png"
)

preferences {
    page(name: "setup", install: false, uninstall: true, nextPage: "setup2") {
        section() {
            label title: "<b>Assign a name:</b>", required: false, width: 6
            
            input "numButton", "enum", title: "<b>Type of Pico</b>", multiple: false, required: true, width: 6,
                options: ["2 button", "4 button", "5 button"]
			paragraph "<font color=\"#000099\"><b>Select which Pico(s).</b></font>"
            input "buttonDevice", "capability.pushableButton", title: "Pico Device", multiple: true, required: true
        }
    }
    page(name: "setup2", install: true, uninstall: true){
        section("Instructions") {
            paragraph "For each action, select which lights or fans to turn on, turn off, toggle, dim/slow, and/or brighten/speed up. Do not have an action both turn on and off the same light/fan (use Toggle). Do not have an action both dim/slow and brighten/speed up the same light/fan."
        }
        if(buttonDevice) {
            section("• <font color=\"#000099\"><b>Push</b> - $buttonDevice</font>"){
            }
        } else {
            section("• <font color=\"#000099\"><b>Push</b></font>"){
            }
        }
        section(hideable: true, hidden: true, "\"On\" Button") {
            input "button_1_push_on", "capability.switch", title: "Turn On", multiple: true, required: false
            input "button_1_push_off", "capability.switch", title: "Turn Off", multiple: true, required: false
            input "button_1_push_toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "button_1_push_dim", "capability.switchLevel", title: "Dim", multiple: true, required: false
            input "button_1_push_brighten", "capability.switchLevel", title: "Brighten", multiple: true, required: false
        }
        if(numButton == "4 button" || numButton == "5 button"){
            section(hideable: true, hidden: true, "\"Brighten\" Button") {
                input "button_2_push_brighten", "capability.switchLevel", title: "Brighten", multiple: true, required: false
                input "button_2_push_toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
                input "button_2_push_on", "capability.switch", title: "Turn On", multiple: true, required: false
                input "button_2_push_off", "capability.switch", title: "Turn Off", multiple: true, required: false
                input "button_2_push_dim", "capability.switchLevel", title: "Dim", multiple: true, required: false

            }
        }
        if(numButton == "5 button"){
            section(hideable: true, hidden: true, "\"Middle\" Button") {
                input "button_3_push_toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
                input "button_3_push_on", "capability.switch", title: "Turn On", multiple: true, required: false
                input "button_3_push_off", "capability.switch", title: "Turn Off", multiple: true, required: false
                input "button_3_push_dim", "capability.switchLevel", title: "Dim", multiple: true, required: false
                input "button_3_push_brighten", "capability.switchLevel", title: "Brighten", multiple: true, required: false
            }
        }
        if(numButton == "4 button" || numButton == "5 button"){
            section(hideable: true, hidden: true, "\"Dim\" Button") {
                input "button_4_push_dim", "capability.switchLevel", title: "Dim", multiple: true, required: false
                input "button_4_push_toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
                input "button_4_push_on", "capability.switch", title: "Turn On", multiple: true, required: false
                input "button_4_push_off", "capability.switch", title: "Turn Off", multiple: true, required: false
                input "button_4_push_brighten", "capability.switchLevel", title: "Brighten", multiple: true, required: false
            }
        }

        section(hideable: true, hidden: true, "\"Off\" Button") {
            input "button_5_push_off", "capability.switch", title: "Turn Off", multiple: true, required: false
            input "button_5_push_toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "button_5_push_on", "capability.switch", title: "Turn On", multiple: true, required: false
            input "button_5_push_dim", "capability.switchLevel", title: "Dim", multiple: true, required: false
            input "button_5_push_brighten", "capability.switchLevel", title: "Brighten", multiple: true, required: false
        }
        if(buttonDevice) {
            section("• <font color=\"#000099\"><b>Long Push/Hold</b> - $buttonDevice</font>"){
            }
        } else {
            section("• <b><font color=\"#000099\">Long Push/Hold</font></b>"){
            }
        }
        section(hideable: true, hidden: true, "\"On\" Button") {
            input "button_1_hold_on", "capability.switch", title: "Turn On", multiple: true, required: false
            input "button_1_hold_toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "button_1_hold_off", "capability.switch", title: "Turn Off", multiple: true, required: false
            input "button_1_hold_dim", "capability.switchLevel", title: "Dim", multiple: true, required: false
            input "button_1_hold_brighten", "capability.switchLevel", title: "Brighten", multiple: true, required: false
        }
        if(numButton == "4 button" || numButton == "5 button"){
            section(hideable: true, hidden: true, "\"Brighten\" Button") {
                input "button_2_hold_brighten", "capability.switchLevel", title: "Brighten", multiple: true, required: false
                input "button_2_hold_toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
                input "button_2_hold_on", "capability.switch", title: "Turn On", multiple: true, required: false
                input "button_2_hold_off", "capability.switch", title: "Turn Off", multiple: true, required: false
                input "button_2_hold_dim", "capability.switchLevel", title: "Dim", multiple: true, required: false
            }
        }
        if(numButton == "5 button"){
            section(hideable: true, hidden: true, "\"Middle\" Button") {
                input "button_3_hold_toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
                input "button_3_hold_on", "capability.switch", title: "Turn On", multiple: true, required: false
                input "button_3_hold_off", "capability.switch", title: "Turn Off", multiple: true, required: false
                input "button_3_hold_dim", "capability.switchLevel", title: "Dim", multiple: true, required: false
                input "button_3_hold_brighten", "capability.switchLevel", title: "Brighten", multiple: true, required: false
            }
        }
        if(numButton == "4 button" || numButton == "5 button"){
            section(hideable: true, hidden: true, "\"Dim\" Button") {
                input "button_4_hold_dim", "capability.switchLevel", title: "Dim", multiple: true, required: false
                input "button_4_hold_toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
                input "button_4_hold_on", "capability.switch", title: "Turn On", multiple: true, required: false
                input "button_4_hold_off", "capability.switch", title: "Turn Off", multiple: true, required: false
                input "button_4_hold_brighten", "capability.switchLevel", title: "Brighten", multiple: true, required: false
            }
        }
        section(hideable: true, hidden: true, "\"Off\" Button") {
            input "button_5_hold_off", "capability.switch", title: "Turn Off", multiple: true, required: false
            input "button_5_hold_toggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "button_5_hold_on", "capability.switch", title: "Turn On", multiple: true, required: false
            input "button_5_hold_dim", "capability.switchLevel", title: "Dim", multiple: true, required: false
            input "button_5_hold_brighten", "capability.switchLevel", title: "Brighten", multiple: true, required: false
        }
        section() {
            input "pushMultiplier", "decimal", required: false, title: "<b>Push mulitplier.</b> (Optional. Default 1.2.)", width: 6
            input "holdMultiplier", "decimal", required: false, title: "<b>Hold mulitplier.</b> (Optional. Default 1.4.)", width: 6
            paragraph "Multiplier/divider for dimming and brightening, from 1.01 to 99. For instance, 2.0 doubles the brightness each time (eg from 25% to 50%, then 100%)."
        }
    }

}

def dimSpeed(){
    if(settings.multiplier != null){
        return settings.pushMultiplier
    }else{
        return 1.2
    }
}

def holdDimSpeed(){
    if(settings.multiplier != null){
        return settings.holdMultiplier
    }else{
        return 1.4
    }
}

def installed() {
	if(app.getLabel().length() < 4)  app.updateLabel("Pico - " + app.getLabel())
    if(app.getLabel().substring(0,4) != "Pico") app.updateLabel("Pico - " + app.getLabel())
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    log.debug "Pico initialized"
    subscribe(buttonDevice, "pushed", buttonPushed)
    subscribe(buttonDevice, "held", buttonHeld)
    subscribe(buttonDevice, "released", buttonReleased)
}

def buttonPushed(evt){
    def appId = app.getId()
    def buttonNumber = evt.value
    def colorSwitch
    def whiteSwitch
    def between1 = false
    def between2 = false
    def between3 = false

    log.info "Pico: $evt.displayName button $buttonNumber $evt.name."
    if(pushMultiplier) pushMultiplier = parent.validateMultiplier(pushMultiplier,appId)
    if(holdMultiplier) holdMultiplier = parent.validateMultiplier(holdMultiplier,appId)

    if(buttonNumber == "1"){
        if(button_1_push_toggle != null) {
            if (settings.color == "Separate"){
                toggleSeparate(button_1_push_toggle)
            } else {
                parent.toggle(button_1_push_toggle,appId)
            }
        }
        if(button_1_push_on) parent.multiOn(button_1_push_on,appId)
        if(button_1_push_off) parent.multiOff(button_1_push_off,appId)
		if(button_1_push_dim) parent.dim(button_1_push_dim,appId)
        if(button_1_push_brighten) parent.brighten(button_1_push_brighten,appId)
        if(!button_1_push_toggle && !button_1_push_on && !button_1_push_off && !button_1_push_dim && !button_1_push_brighten){
            log.info "Pico: No action defined for button $buttonNumber of $displayName."
        }
    }

    if(buttonNumber == "2" && (numButton == "4 button" || numButton == "5 button")){
        if(button_2_push_toggle != null) {
            if (settings.color == "Separate"){
                toggleSeparate(button_2_push_toggle)
            } else {
                parent.toggle(button_2_push_toggle,appId)
            }
        }
        if(button_2_push_on) parent.multiOn(button_2_push_on,appId)
        if(button_2_push_off) parent.multiOff(button_2_push_off,appId)
		if(button_2_push_dim) parent.dim(button_2_push_dim,appId)
        if(button_2_push_brighten) parent.brighten(button_2_push_brighten,appId)
        if(!button_2_push_toggle && !button_2_push_on && !button_2_push_off && !button_2_push_dim && !button_2_push_brighten){
            log.info "Pico: No action defined for button $buttonNumber of $displayName."
        }
    }

    if(buttonNumber == "3"){
        if(button_3_push_toggle != null) {
            if (settings.color == "Separate"){
                toggleSeparate(button_3_push_toggle)
            } else {
                parent.toggle(button_3_push_toggle,appId)
            }
        }
        if(button_3_push_on) parent.multiOn(button_3_push_on,appId)
        if(button_3_push_off) parent.multiOff(button_3_push_off,appId)
        if(button_3_push_dim) parent.dim(button_3_push_dim,appId)
        if(button_3_push_brighten) parent.brighten(button_3_push_brighten,appId)
        if(!button_3_push_toggle && !button_3_push_on && !button_3_push_off && !button_3_push_dim && !button_3_push_brighten){
            log.info "Pico: No action defined for button $buttonNumber of $displayName."
        }
    }

    if(buttonNumber == "4"){
        if(button_4_push_toggle != null) {
            if (settings.color == "Separate"){
                toggleSeparate(button_4_push_toggle)
            } else {
                parent.toggle(button_4_push_toggle,appId)
            }
        }
        if(button_4_push_on) parent.multiOn(button_4_push_on,appId)
        if(button_4_push_off) parent.multiOff(button_4_push_off,appId)
        if(button_4_push_dim) parent.dim(button_4_push_dim,appId)
        if(button_4_push_brighten) parent.brighten(button_4_push_brighten,appId)
        if(!button_4_push_toggle && button_4_push_on && !button_4_push_off && !button_4_push_dim && !button_4_push_brighten){
            log.info "Pico: No action defined for button $buttonNumber of $displayName."
        }
    }
    if(buttonNumber == "5" || (buttonNumber == "2" &&  numButton == "2 button")){
        if(button_5_push_toggle != null) {
            if (settings.color == "Separate"){
                toggleSeparate(button_5_push_toggle)
            } else {
                parent.toggle(button_5_push_toggle,appId)
            }
        }
        if(button_5_push_on) parent.multiOn(button_5_push_on,appId)
        if(button_5_push_off) parent.multiOff(button_5_push_off,appId)
        if(button_5_push_dim) parent.dim(button_5_push_dim,appId)
        if(button_5_push_brighten) parent.brighten(button_5_push_brighten,appId)
        if(!button_5_push_toggle && !button_5_push_on && !button_5_push_off && !button_5_push_dim && !button_5_push_brighten){
            log.info "Pico: No action defined for button $buttonNumber of $displayName."
        }
    }
    if(fromTime1 != null && toTime1 != null){
        between1 = timeOfDayIsBetween(timeToday(fromTime1, location.timeZone), timeToday(toTime1, location.timeZone), new Date(), location.timeZone)
    }
    if(fromTime2 != null && toTime2 != null){
        between2 = timeOfDayIsBetween(timeToday(fromTime2, location.timeZone), timeToday(toTime2, location.timeZone), new Date(), location.timeZone)
    }
    if(fromTime3 != null && toTime3 != null){
        between3 = timeOfDayIsBetween(timeToday(fromTime3, location.timeZone), timeToday(toTime3, location.timeZone), new Date(), location.timeZone)
    }
}

def buttonHeld(evt){
    def appId = app.getId()
    def buttonNumber = evt.value
    def colorSwitch
    def whiteSwitch

    log.info "Pico: $evt.displayName button $buttonNumber $evt.name."
    if(pushMultiplier) pushMultiplier = parent.validateMultiplier(pushMultiplier,appId)
    if(holdMultiplier) holdMultiplier = parent.validateMultiplier(holdMultiplier,appId)

    if(buttonNumber == "1"){
        if(button_1_hold_toggle != null) {
            if (settings.color == "Separate"){
                toggleSeparate(button_1_push_toggle)
            } else {
                parent.toggle(button_1_push_toggle,appId)
            }
        }
        if(button_1_hold_on) parent.multiOn(button_1_hold_on,appId)
        if(button_1_hold_off) parent.multiOff(button_1_hold_off,appId)
        if(button_1_hold_dim) holdDim(button_1_hold_dim)
        if(button_1_hold_brighten) holdBrighten(button_1_hold_brighten)
        if(!button_1_hold_toggle && !button_1_hold_on && !button_1_hold_off && !button_1_hold_dim && !button_1_hold_brighten){
            log.info "Pico: No action defined for button $buttonNumber of $evt.displayName."
        }
    }

    if(buttonNumber == "2"){
        if(button_2_hold_toggle != null) {
            if (settings.color == "Separate"){
                toggleSeparate(button_2_push_toggle)
            } else {
                parent.toggle(button_2_push_toggle,appId)
            }
        }
        if(button_2_hold_on) parent.multiOn(button_2_hold_on,appId)
        if(button_2_hold_off) multiOff(button_2_hold_off,appId)
        if(button_2_hold_dim) holdDim(button_2_hold_dim)
        if(button_2_hold_brighten) holdBrighten(button_2_hold_brighten)
        if(!button_2_hold_toggle && !button_2_hold_on && !button_2_hold_off && !button_2_hold_dim && !button_2_hold_brighten){
            log.info "Pico: No action defined for button $buttonNumber of $displayName."
        }
    }

    if(buttonNumber == "3"){
        if(button_3_hold_toggle != null) {
            if (settings.color == "Separate"){
                toggleSeparate(button_3_push_toggle)
            } else {
                parent.toggle(button_3_push_toggle,appId)
            }
        }
        if(button_3_hold_on) parent.multiOn(button_3_hold_on,appId)
        if(button_3_hold_off) parent.multiOff(button_3_hold_off,appId)
        if(button_3_hold_dim) holdDim(button_3_hold_dim)
        if(button_3_hold_brighten) holdBrighten(button_3_hold_brighten)
        if(!button_3_hold_toggle && !button_3_hold_on && !button_3_hold_off && !button_3_hold_dim && !button_3_hold_brighten){
            log.info "Pico: No action defined for button $buttonNumber of $displayName."
        }
    }

    if(buttonNumber == "4"){
        if(button_4_hold_toggle != null) {
            if (settings.color == "Separate"){
                toggleSeparate(button_4_push_toggle)
            } else {
                parent.toggle(button_4_push_toggle,appId)
            }
        }
        if(button_4_hold_on) parent.multiOn(button_4_hold_on,appId)
        if(button_4_hold_off) parent.multiOff(button_4_hold_off,appId)
        if(button_4_hold_dim) holdDim(button_4_hold_dim)
        if(button_4_hold_brighten) holdBrighten(button_4_hold_brighten)
        if(!button_4_hold_toggle && !button_4_hold_on && !button_4_hold_off && !button_4_hold_dim && !button_4_hold_brighten){
            log.info "Pico: No action defined for button $buttonNumber of $displayName."
        }
    }

    if(buttonNumber == "5"){
        if(button_5_hold_toggle != null) {
            if (settings.color == "Separate"){
                toggleSeparate(button_5_push_toggle)
            } else {
                parent.toggle(button_5_push_toggle,appId)
            }
        }
        if(button_5_hold_on) parent.multiOn(button_5_hold_on,appId)
        if(button_5_hold_off) parent.multiOff(button_5_hold_off,appId)
        if(button_5_hold_dim) holdDim(button_5_hold_dim)
        if(button_5_hold_brighten) holdBrighten(button_5_hold_brighten)
        if(!button_5_hold_toggle && !button_5_hold_on && !button_5_hold_off && !button_5_hold_dim && !button_5_hold_brighten){
            log.info "Pico: No action defined for button $buttonNumber of $displayName."
        }
    }
}

def buttonReleased(evt){
    def buttonNumber = evt.value
    if (buttonNumber == "2" || (buttonNumber == "4" && (settings.numButton == "4 button" || settings.numButton == "5 button")) || (buttonNumber == "1" && settings.numButton == "2 button")){
        unschedule()
    }
}

// counts number of steps for brighten and dim
// action = "dim" or "brighten"
def getSteps(lvl, action){
    def steps = 0

    if (action != "dim" && action != "brighten"){
        log.debug "Pico: Invalid action with getSteps"
        return false
    }
    if(action == "dim" && lvl < 2){
        steps = 0
    } else if (action == "brighten" && lvl>99){
        steps = 0
    }
	
    if (action == "dim"){
        while (lvl  > 1) {
            steps = steps + 1
            lvl = parent.nextLevel(lvl, action,app.getId())
        }
    } else if(action == "brighten"){
        while (lvl  < 100) {
            steps = steps + 1
            lvl = parent.nextLevel(lvl, action,app.getId())
        }
    }
    return steps
}

def setSubscribeLevel(data){
	def appId = app.getId()
	button_1_hold_dim.each{
        if (it.id == data.device) device = it
    }
    if (device == null){
        button_1_hold_brighten.each{
            if (it.id == data.device) device = it
        }
    }
    if (device == null){
        button_2_hold_dim.each{
            if (it.id == data.device) device = it
        }
    }
    if (device == null){
        button_2_hold_brighten.each{
            if (it.id == data.device) device = it
        }
    }
    if (device == null){
        button_3_hold_dim.each{
            if (it.id == data.device) device = it
        }
    }
    if (device == null){
        button_3_hold_brighten.each{
            if (it.id == data.device) device = it
        }
    }
    if (device == null){
        button_4_hold_dim.each{
            if (it.id == data.device) device = it
        }
    }
    if (device == null){
        button_4_hold_brighten.each{
            if (it.id == data.device) device = it
        }
    }
    if (device == null){
        button_5_hold_dim.each{
            if (it.id == data.device) device = it
        }
    }
    if (device == null){
        button_5_hold_brighten.each{
            if (it.id == data.device) device = it
        }
    }
	if(device == null) {
		log.debug "Pico: Error finding device id \"$data.device\" in setSubscribeLevel."
		return
	}
	level = data.level as int
    parent.setToLevel(device,level,app.getId())
}

def toggleSeparate(device){
	def appId = app.getId()
    device.each{
        if(it.currentValue("hue") && it.currentValue("switch") == "on") {
            colorSwitch = "on"
        } else if(!it.currentValue("hue") && it.currentValue("switch") == "on") {
            whiteSwitch = "on"
        }
    }
    // color on, white on, turn off color
    if (colorSwitch == "on" && whiteSwitch == "on"){
        parent.multiOff(device,appId)
        // color on, white off; turn white on
    } else if (colorSwitch == "on" && whiteSwitch != "on"){
        parent.multiOn(device,appId)
        //color off, white on; turn off white and turn on color
    } else if (colorSwitch != "on" && whiteSwitch == "on"){
        parent.multiOff(device,"white",appId)
        parent.multiOn(device,appId)
        // both off; turn color on
    } else if (colorSwitch != "on" && whiteSwitch != "on"){
        parent.multiOn(device,appId)
    }
}

def holdDim(dvce){
	def appId = app.getId()
    def lvl = getLevel(dvce)
	
    dvce.each{
        if(parent.isFan(it) == true){
            parent.dim(it,appId)
        } else if(!parent.stateOn(it)){
            parent.setToLevel(it,1,appId)
        } else {
            if(lvl < 2){
                log.info "Pico: Can't dim $it; already 1%."
				parent.flashGreen(it)
            } else {
                def steps = getSteps(lvl, "dim")
                def newLevel

                for(def i = 1; i <= steps; i++) {
                    newLevel = parent.nextLevel(lvl, "dim",appId)
                    runInMillis(i*750,setSubscribeLevel, [overwrite: false, data: [device: it.id, level: newLevel]])
                    lvl = newLevel
                }
            }
        }
    }
}

def holdBrighten(dvce){
	def appId = app.getId()
    def lvl = getLevel(dvce)

    dvce.each{
        if(parent.isFan(it) == true){
            parent.brighten(it,appId)
        } else if(!parent.stateOn(it)){
            parent.setToLevel(it,1,appId)
        } else {
            if(lvl > 99){
                log.info "Pico: Can't brighten $it; already 100%."
				parent.flashGreen(it)
            } else {
                def steps = getSteps(lvl, "brighten")
                def newLevel

                for(def i = 1; i <= steps; i++) {
                    newLevel = parent.nextLevel(lvl, "brighten",app.getId())
                    runInMillis(i*750,setSubscribeLevel, [overwrite: false, data: [device: it.id, level: newLevel]])
                    lvl = newLevel
                }
            }
        }
    }
}

// calculate average level of a group
def getLevel(device){
    def lvl = 0
    def count = 0
    device.each{
        if(parent.isFan(it) != true){
            lvl += it.currentLevel
            count++
        }
    }
    if(lvl>0) lvl = Math.round(lvl/count)
    if (lvl > 100) lvl = 100
    return lvl
}
