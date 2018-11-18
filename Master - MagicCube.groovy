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
*  Name: Master - MagicCube
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master - MagicCube.groovy
*  Version: 0.1.01
* 
***********************************************************************************************************************/

definition(
    name: "Master - MagicCube",
    namespace: "master",
    author: "roguetech",
    description: "Control MagicCube",
    parent: "master:Master",
    category: "Convenience",
    iconUrl: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/mi_face_s.png",
    iconX2Url: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/mi_face.png"
)

preferences {
    page(name: "setup", install: true, uninstall: true) {
        section() {
            label title: "<b>Assign a name:</b>", required: true
        }
        section() {
			paragraph "<font color=\"#000099\"><b>Select which MagicCube(s).</b></font>"
            input "buttonDevice", "capability.pushableButton", title: "The Cube", multiple: false, required: true, submitOnChange:true
        }

		if(buttonDevice){
			section("<div style=\"background-color:BurlyWood\"><b>Instructions</b>") {
				paragraph "For each action, select which lights or fans to turn on, turn off, toggle, dim/slow, and/or brighten/speed up. Do not have an action both turn on and off the same light/fan (use Toggle). Do not have an action both dim/slow and brighten/speed up the same light/fan."
			}
			section(hideable: true, hidden: true, "Shaking <font color=\"gray\">(Click to expand/collapse)</font>") {
				if(button_1_toggle){
					input "button_1_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)</b>", multiple: true, required: false, submitOnChange:true
				} else{
					input "button_1_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_1_on){
					input "button_1_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_1_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_1_off){
					input "button_1_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_1_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_1_dim){
					input "button_1_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_1_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_1_brighten){
					input "button_1_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_1_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
			}
			 section(hideable: true, hidden: true, "Sliding <font color=\"gray\">(Click to expand/collapse)</font>") {
				if(button_4_toggle){
					input "button_4_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_4_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_4_on){
					input "button_4_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_4_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_4_off){
					input "button_4_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_4_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_4_dim){
					input "button_4_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_4_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_4_brighten){
					input "button_4_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_4_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
			}
			section(hideable: true, hidden: true, "Rotating clockwise <font color=\"gray\">(Click to expand/collapse)</font>") {
				if(button_6_toggle){
					input "button_6_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_6_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_6_on){
					input "button_6_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_6_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_6_off){
					input "button_6_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_6_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_6_dim){
					input "button_6_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_6_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_6_brighten){
					input "button_6_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_6_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
			}
			section(hideable: true, hidden: true, "Rotating counter clockwise <font color=\"gray\">(Click to expand/collapse)</font>") {
				if(button_7_toggle){
					input "button_7_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_7_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_7_on){
					input "button_7_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_7_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_7_off){
					input "button_7_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_7_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_7_dim){
					input "button_7_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_7_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_7_brighten){
					input "button_7_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_7_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
			}
			section(hideable: true, hidden: true, "90° flipping <font color=\"gray\">(Click to expand/collapse)</font>") {
				if(button_2_toggle){
					input "button_2_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_2_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_2_on){
					input "button_2_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_2_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_2_off){
					input "button_2_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_2_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_2_dim){
					input "button_2_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_2_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_2_brightn){
					input "button_2_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_2_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
			}
			 section(hideable: true, hidden: true, "180° flipping <font color=\"gray\">(Click to expand/collapse)</font>") {
				if(button_3_toggle){
					input "button_3_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_3_toggle", "capability.switch", title: "<b>Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_3_on){
					input "button_3_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_3_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_3_off){
					input "button_3_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_3_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_3_dim){
					input "button_3_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_3_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_3_brighten){
					input "button_3_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_3_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
			}
			  section(hideable: true, hidden: true, "Knocking <font color=\"gray\">(Click to expand/collapse)</font>") {
				if(button_5_toggle){
					input "button_5_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_5_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_5_on){
					input "button_5_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_5_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_5_off){
					input "button_5_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_5_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_5_dim){
					input "button_5_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_5_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_5_brighten){
					input "button_5_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_5_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
			}
			if(button_1_dim || button_1_brighten || button_2_dim || button_2_brighten || button_3_dim || button_3_brighten || button_4_dim || button_4_brighten || button_5_dim || button_5_brighten || button_6_dim || button_6_brighten || button_7_dim || button_7_brighten){
				section(){
					input "multiplier", "decimal", required: false, title: "<b>Multiplier.</b> (Optional. Default 1.2.)"
					paragraph "Multiplier/divider for dimming and brightening, from 1.01 to 99. For instance, 2.0 doubles the brightness each time (eg from 25% to 50%, then 100%)."
				}
			}
		}
    }
}

def dimSpeed(){
    if(settings.multiplier != null){
        return settings.multiplier
    }else{
        return 1.2
    }
}

def installed() {
	if(app.getLabel().length() < 9)  app.updateLabel("MagicCube - " + app.getLabel())
    if(app.getLabel().substring(0,9) != "MagicCube") app.updateLabel("MagicCube - " + app.getLabel())
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    log.info "MagicCube initialized"
    subscribe(buttonDevice, "pushed.1", buttonEvent1)
    subscribe(buttonDevice, "pushed.2", buttonEvent2)
    subscribe(buttonDevice, "pushed.3", buttonEvent3)
    subscribe(buttonDevice, "pushed.5", buttonEvent5)
    subscribe(buttonDevice, "pushed.6", buttonEvent6)
    subscribe(buttonDevice, "pushed.7", buttonEvent7)
}

def buttonEvent1(evt){
    def appId = app.getId()
    def buttonNumber = evt.value

    log.info "MagicCube: $evt.displayName shaken."
    if(pushMultiplier) pushMultiplier = parent.validateMultiplier(pushMultiplier,appId)
    if(holdMultiplier) holdMultiplier = parent.validateMultiplier(holdMultiplier,appId)

    if(button_1_toggle) parent.toggle(button_1_toggle,appId)
    if(button_1_on) parent.multiOn(button_1_on,appId)
    if(button_1_off) parent.multiOff(button_1_off,appId)
    if(button_1_dim) parent.dim(button_1_dim,appId)
    if(button_1_brighten) parent.brighten(button_1_brighten,appId)
    if(!button_1_hold_toggle && !button_1_hold_on && !button_1_hold_off && !button_1_hold_dim && !button_1_hold_brighten){
        log.info "MagicCube: No action defined for shaking of $evt.displayName."
    }
}

def buttonEvent2(evt){
    def appId = app.getId()
    def buttonNumber = evt.value

    log.info "MagicCube: $evt.displayName flipped 90°."
    if(pushMultiplier) pushMultiplier = parent.validateMultiplier(pushMultiplier,appId)
    if(holdMultiplier) holdMultiplier = parent.validateMultiplier(holdMultiplier,appId)

    if(button_2_toggle) parent.toggle(button_2_toggle,appId)
    if(button_2_on) parent.multiOn(button_2_on,appId)
    if(button_2_off) parent.multiOff(button_2_off,appId)
    if(button_2_dim) parent.dim(button_2_dim,appId)
    if(button_2_brighten) parent.brighten(button_2_brighten,appId)
    if(!button_2_hold_toggle && !button_2_hold_on && !button_2_hold_off && !button_2_hold_dim && !button_2_hold_brighten){
        log.info "MagicCube: No action defined for flipping 90° of $evt.displayName."
    }
}

def buttonEvent3(evt){
    def appId = app.getId()
    def buttonNumber = evt.value

    log.info "MagicCube: $evt.displayName flipped 180°."
    if(pushMultiplier) pushMultiplier = parent.validateMultiplier(pushMultiplier,appId)
    if(holdMultiplier) holdMultiplier = parent.validateMultiplier(holdMultiplier,appId)

    if(button_3_toggle) parent.toggle(button_3_toggle,appId)
    if(button_3_on) parent.multiOn(button_3_on,appId)
    if(button_3_off) parent.multiOff(button_3_off,appId)
    if(button_3_dim) parent.dim(button_3_dim,appId)
    if(button_3_brighten) parent.brighten(button_3_brighten,appId)
    if(!button_3_hold_toggle && !button_3_hold_on && !button_3_hold_off && !button_3_hold_dim && !button_3_hold_brighten){
        log.info "MagicCube: No action defined for flipping 180° of $evt.displayName."
    }
}

def buttonEvent4(evt){
    def appId = app.getId()
    def buttonNumber = evt.value

    log.info "MagicCube: $evt.displayName slid."
    if(pushMultiplier) pushMultiplier = parent.validateMultiplier(pushMultiplier,appId)
    if(holdMultiplier) holdMultiplier = parent.validateMultiplier(holdMultiplier,appId)

    if(button_4_toggle) parent.toggle(button_4_toggle,appId)
    if(button_4_on) parent.multiOn(button_4_on,appId)
    if(button_4_off) parent.multiOff(button_4_off,appId)
    if(button_4_dim) parent.dim(button_4_dim,appId)
    if(button_4_brighten) parent.brighten(button_4_brighten,appId)
    if(!button_4_hold_toggle && !button_4_hold_on && !button_4_hold_off && !button_4_hold_dim && !button_4_hold_brighten){
        log.info "MagicCube: No action defined for sliding of $evt.displayName."
    }
}

def buttonEvent5(evt){
    def appId = app.getId()
    def buttonNumber = evt.value

    log.info "MagicCube: $evt.displayName knock."
    if(pushMultiplier) pushMultiplier = parent.validateMultiplier(pushMultiplier,appId)
    if(holdMultiplier) holdMultiplier = parent.validateMultiplier(holdMultiplier,appId)

    if(button_5_toggle) parent.toggle(button_5_toggle,appId)
    if(button_5_on) parent.multiOn(button_5_on,appId)
    if(button_5_off) parent.multiOff(button_5_off,appId)
    if(button_5_dim) parent.dim(button_5_dim,appId)
    if(button_5_brighten) parent.brighten(button_5_brighten,appId)
    if(!button_5_hold_toggle && !button_5_hold_on && !button_5_hold_off && !button_5_hold_dim && !button_5_hold_brighten){
        log.info "MagicCube: No action defined for knocking of $evt.displayName."
    }
}

def buttonEvent6(evt){
    def appId = app.getId()
    def buttonNumber = evt.value

    log.info "MagicCube: $evt.displayName rotated clockwise."
    if(pushMultiplier) pushMultiplier = parent.validateMultiplier(pushMultiplier,appId)
    if(holdMultiplier) holdMultiplier = parent.validateMultiplier(holdMultiplier,appId)

    if(button_6_toggle) parent.toggle(button_6_toggle,appId)
    if(button_6_on) parent.multiOn(button_6_on,appId)
    if(button_6_off) parent.multiOff(button_6_off,appId)
    if(button_6_dim) parent.dim(button_6_dim,appId)
    if(button_6_brighten) parent.brighten(button_6_brighten,appId)
    if(!button_6_hold_toggle && !button_6_hold_on && !button_6_hold_off && !button_6_hold_dim && !button_6_hold_brighten){
        log.info "MagicCube: No action defined for rotating clockwise of $evt.displayName."
    }
}

def buttonEvent7(evt){
    def appId = app.getId()
    def buttonNumber = evt.value

    log.info "MagicCube: $evt.displayName rotated counter-clockwise."
    if(pushMultiplier) pushMultiplier = parent.validateMultiplier(pushMultiplier,appId)
    if(holdMultiplier) holdMultiplier = parent.validateMultiplier(holdMultiplier,appId)

    if(button_7_toggle) parent.toggle(button_7_toggle,appId)
    if(button_7_on) parent.multiOn(button_7_on,appId)
    if(button_7_off) parent.multiOff(button_7_off,appId)
    if(button_7_dim) parent.dim(button_7_dim,appId)
    if(button_7_brighten) parent.brighten(button_7_brighten,appId)
    if(!button_7_hold_toggle && !button_7_hold_on && !button_7_hold_off && !button_7_hold_dim && !button_7_hold_brighten){
        log.info "MagicCube: No action defined for rotating counter-clockwise of $evt.displayName."
    }
}
