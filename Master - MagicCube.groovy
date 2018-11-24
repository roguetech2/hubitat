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
*  Version: 0.2.01
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
			paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this MagicCube setup:</b></div>"
			label title: "", required: true, submitOnChange:true
			if(!app.label){
				paragraph "<div style=\"background-color:BurlyWood\"> </div>"
			} else if(app.label){
				paragraph "<div style=\"background-color:BurlyWood\"><b> Select MagicCubic device to setup:</b></div>"
				input "buttonDevice", "capability.pushableButton", title: "MagicCube(s)?", multiple: true, required: true, submitOnChange:true
				if(!buttonDevice){
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				} else if(buttonDevice){
					paragraph "For each action, select which lights or fans to turn on, turn off, toggle, dim/slow, and/or brighten/speed up. Do not have an action both turn on and off the same light/fan (use Toggle). Do not have an action both dim/slow and brighten/speed up the same light/fan."
					if(!advancedSetup){
						input "advancedSetup", "bool", title: "<b>Simple setup.</b> Click to show advanced options.", defaultValue: false, submitOnChange:true
					} else {
						input "advancedSetup", "bool", title: "<b>Advanced setup.</b> Clck to hide advanced options.", defaultValue: false, submitOnChange:true
					}
					if(!multiDevice){
						input "multiDevice", "bool", title: "Mutli-control: <b>Controls one set of light(s)/switch(es).</b> Click for MagicCube to independantly control different sets of lights/switches.", defaultValue: false, submitOnChange:true
						paragraph "Use this option if you only want to control one light or set of lights. Change this option if, for instance, you want to control some lights with a 90° flip, and <i>different lights</i> with a 180° flip."
						input "controlDevice", "capability.switch", title: "Device(s) to control", multiple: true, required: true, submitOnChange:true
					} else {
						input "multiDevice", "bool", title: "Mutli-control: <b>Independantly control different sets of lights/switches.</b> Click for MagicCube to control only one set of lights/switches.", defaultValue: true, submitOnChange:true
						paragraph "Use this option if you only want to control one light or set of lights. Change this option if, for instance, you want to control some lights with a 90° flip, and <i>different lights</i> with a 180° flip."
					}
					if(advancedSetup){
						paragraph "<b>Pro-tip</b>: Profiles for Multi-control enabled and disabled are stored separatly, allowing toggling between two different setups. To do this, set the options both with Multi-control disabled and enabled."
					}
					if(!multiDevice && !controlDevice){
						paragraph "<div style=\"background-color:BurlyWood\"> </div>"
					} else if(!multiDevice && controlDevice){
						paragraph "<div style=\"background-color:BurlyWood\"><b> Select what to do for each MagicCube action:</b></div>"
						if(advancedSetup){
							input "clockwise", "enum", title: "When <b>rotating clockwise</b>, what to do with lights/switches?", required: false, multiple: false, width: 6, options: ["brighten":"Brighten","dim":"Dim","on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
						} else {
							input "clockwise", "enum", title: "When <b>rotating clockwise</b>, what to do with lights/switches?", required: false, multiple: false, width: 6, options: ["brighten":"Brighten", ,"dim":"Dim", "toggle":"Toggle"], submitOnChange:true
						}
						if(advancedSetup){
							input "counterClockwise", "enum", title: "When <b>rotating counter-clockwise</b>, what to do with lights/switches?", required: false, multiple: false, width: 6, options: ["brighten":"Brighten","dim":"Dim", "on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
						} else {
							input "counterClockwise", "enum", title: "When <b>rotating counter-clockwise</b>, what to do with lights/switches?", required: false, multiple: false, width: 6, options: ["brighten":"Brighten","dim":"Dim", "toggle":"Toggle"], submitOnChange:true
						}
						if(advancedSetup){
							input "f90", "enum", title: "When <b>flipping 90°</b>, what to do with lights/switches?", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true
						} else {
							input "f90", "enum", title: "When <b>flipping 90°</b>, what to do with lights/switches?", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
						}
						if(advancedSetup){
							input "f180", "enum", title: "When <b>flipping 180°</b>, what to do with lights/switches?", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true
						} else {
							input "f180", "enum", title: "When <b>flipping 180°</b>, what to do with lights/switches?", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
						}
						if(advancedSetup){
							input "shake", "enum", title: "When <b>shaking</b>, what to do with lights/switches?", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true
						} else {
							input "shake", "enum", title: "When <b>shaking</b>, what to do with lights/switches?", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
						}
						if(advancedSetup){
							input "knock", "enum", title: "When <b>knocking</b>, what to do with lights/switches?", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true
							input "slide", "enum", title: "When <b>sliding</b>, what to do with lights/switches?", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true
						}

						if(clockwise == "dim" || clockwise == "brighten" || f90 == "dim" || f90 == "brighten" || f180 == "dim" || f180 == "brighten" || shake == "dim" || shake == "brighten" || knock == "dim" || knock == "brighten"){
							paragraph "<div style=\"background-color:BurlyWood\"><b> Set dim and brighten speed:</b></div>"
							paragraph "Multiplier/divider for dimming and brightening, from 1.01 to 99, where higher is faster. For instance, a value of 2 would double (eg from 25% to 50%, then 100%), whereas a value of 1.5 would increase by half each time (eg from 25% to 38% to 57%)."
							input "multiplier", "decimal", required: false, title: "Mulitplier? (Optional. Default 1.2.)", width: 6
						}
						if(button_1_push_on || button_1_push_off || button_1_push_dim || button_1_push_brighten || button_1_push_toggle || button_2_push_on || button_2_push_off || button_2_push_dim || button_2_push_brighten || button_2_push_toggle || button_3_push_on || button_3_push_off || button_3_push_dim || button_3_push_brighten || button_3_push_toggle || button_4_push_on || button_4_push_off || button_4_push_dim || button_4_push_brighten || button_4_push_toggle || button_5_push_on || button_5_push_off || button_5_push_dim || button_5_push_brighten || button_5_push_toggle || button_1_hold_on || button_1_hold_off || button_1_hold_dim || button_1_hold_brighten || button_1_hold_toggle || button_2_hold_on || button_2_hold_off || button_2_hold_dim || button_2_hold_brighten || button_2_hold_toggle || button_3_hold_on || button_3_hold_off || button_3_hold_dim || button_3_hold_brighten || button_3_hold_toggle || button_4_hold_on || button_4_hold_off || button_4_hold_dim || button_4_hold_brighten || button_4_hold_toggle || button_5_hold_on || button_5_hold_off || button_5_hold_dim || button_5_hold_brighten || button_5_hold_toggle){
							paragraph "<div style=\"background-color:LightCyan\"><b> Click \"Done\" to continue.</b></div>"
						}
					}
				}
			}			
		
		}
		if(app.label && buttonDevice && multiDevice){
			section(hideable: true, hidden: true, "Shaking <font color=\"gray\">(Click to expand/collapse)</font>") {
				log.debug controlDevice
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
				if(button_1_toggle){
					input "button_1_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)</b>", multiple: true, required: false, submitOnChange:true
				} else{
					input "button_1_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(advancedSetup){
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
			}
			
			section(hideable: true, hidden: true, "Rotating clockwise <font color=\"gray\">(Click to expand/collapse)</font>") {
				if(button_6_brighten){
					input "button_6_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_6_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_6_dim){
					input "button_6_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_6_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(button_6_toggle){
					input "button_6_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_6_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(advancedSetup){
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
				}
			}
			section(hideable: true, hidden: true, "Rotating counter clockwise <font color=\"gray\">(Click to expand/collapse)</font>") {
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
				if(button_7_toggle){
					input "button_7_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_7_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(advancedSetup){
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
				}
			}
			section(hideable: true, hidden: true, "90° flipping <font color=\"gray\">(Click to expand/collapse)</font>") {
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
				if(button_2_toggle){
					input "button_2_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_2_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(advancedSetup){
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
			}
			section(hideable: true, hidden: true, "180° flipping <font color=\"gray\">(Click to expand/collapse)</font>") {
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
				if(button_3_toggle){
					input "button_3_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
				} else {
					input "button_3_toggle", "capability.switch", title: "<b>Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
				}
				if(advancedSetup){
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
			}
			if(advancedSetup){
				section(hideable: true, hidden: true, "Sliding <font color=\"gray\">(Click to expand/collapse)</font>") {
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
					if(button_4_toggle){
						input "button_4_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
					} else {
						input "button_4_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
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
			}
			if(advancedSetup){
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
			}
			if(button_1_dim || button_1_brighten || button_2_dim || button_2_brighten || button_3_dim || button_3_brighten || button_4_dim || button_4_brighten || button_5_dim || button_5_brighten || button_6_dim || button_6_brighten || button_7_dim || button_7_brighten){
				section(){
					paragraph "<div style=\"background-color:BurlyWood\"><b> Set dim and brighten speed:</b></div>"
					paragraph "Multiplier/divider for dimming and brightening, from 1.01 to 99, where higher is faster. For instance, a value of 2 would double (eg from 25% to 50%, then 100%), whereas a value of 1.5 would increase by half each time (eg from 25% to 38% to 57%)."
					input "multiplier", "decimal", required: false, title: "Mulitplier? (Optional. Default 1.2.)", width: 6
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

	// Set device if using simple setup
	if(!multiDevice){
		if(shake && shake == "on") button_1_on = controlDevice
		if(shake && shake == "off") button_1_off = controlDevice
		if(shake && shake == "dim") button_1_dim = controlDevice
		if(shake && shake == "brighten") button_1_brighten = controlDevice
		if(shake && shake == "toggle") button_1_toggle = controlDevice
	}

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

	// Set device if using simple setup
	if(!multiDevice){
		if(f90 && f90 == "on") button_2_on = controlDevice
		if(f90 && f90 == "off") button_2_off = controlDevice
		if(f90 && f90 == "dim") button_2_dim = controlDevice
		if(f90 && f90 == "brighten") button_2_brighten = controlDevice
		if(f90 && f90 == "toggle") button_2_toggle = controlDevice
	}

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

	// Set device if using simple setup
	if(!multiDevice){
		if(f180 && f180 == "on") button_3_on = controlDevice
		if(f180 && f180 == "off") button_3_off = controlDevice
		if(f180 && f180 == "dim") button_3_dim = controlDevice
		if(f180 && f180 == "brighten") button_3_brighten = controlDevice
		if(f180 && f180 == "toggle") button_3_toggle = controlDevice
	}

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

	// Set device if using simple setup
	if(!multiDevice){
		if(slide && slide == "on") button_4_on = controlDevice
		if(slide && slide == "off") button_4_off = controlDevice
		if(slide && slide == "dim") button_4_dim = controlDevice
		if(slide && slide == "brighten") button_4_brighten = controlDevice
		if(slide && slide == "toggle") button_4_toggle = controlDevice
	}

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

	// Set device if using simple setup
	if(!multiDevice){
		if(knock && knock == "on") button_5_on = controlDevice
		if(knock && knock == "off") button_5_off = controlDevice
		if(knock && knock == "dim") button_5_dim = controlDevice
		if(knock && knock == "brighten") button_5_brighten = controlDevice
		if(knock && knock == "toggle") button_5_toggle = controlDevice
	}

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
	
	if(!multiDevice){
		if(clockwise && clockwise == "on") button_6_on = controlDevice
		if(clockwise && clockwise == "off") button_6_off = controlDevice
		if(clockwise && clockwise == "dim") button_6_dim = controlDevice
		if(clockwise && clockwise == "brighten") button_6_brighten = controlDevice
		if(clockwise && clockwise == "toggle") button_6_toggle = controlDevice
	}

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

	// Set device if using simple setup
	if(!multiDevice){
		if(counterClockwise && counterClockwise == "on") button_7_on = controlDevice
		if(counterClockwise && counterClockwise == "off") button_7_off = controlDevice
		if(counterClockwise && counterClockwise == "dim") button_7_dim = controlDevice
		if(counterClockwise && counterClockwise == "brighten") button_7_brighten = controlDevice
		if(counterClockwise && counterClockwise == "toggle") button_7_toggle = controlDevice
	}

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
