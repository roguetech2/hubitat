/***********************************************************************************************************************
*
*  Copyright (C) 2020 roguetech
*
*  License:
*  This program is free software: you can redistribute it and/or modify it under the terms of the GNU
*  General Public License as published by the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
*  implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
*  <http://www.gnu.org/licenses/> for more details.
*
*  Name: Master - MagicCube
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master%20-%20MagicCube.groovy
*  Version: 0.2.10
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
	infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
	errorIcon = "<img src=\"http://emily-john.love/icons/error.png\" width=20 height=20>"
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
					paragraph "$infoIcon For each action, select which lights or switches to turn on, turn off, toggle, dim/slow, and/or brighten/speed up. Do not have an action both turn on and off the same light/switch (use Toggle). Do not have an action both dim/slow and brighten/speed up the same light/fan."
					if(!advancedSetup){
						input "advancedSetup", "bool", title: "<b>Simple actions.</b> Click to show advanced actions.", defaultValue: false, submitOnChange:true
					} else {
						input "advancedSetup", "bool", title: "<b>Advanced actions.</b> Click to hide advanced actions.", defaultValue: false, submitOnChange:true
					}
					if(!multiDevice){
						input "multiDevice", "bool", title: "Mutli-control: <b>Controls one set of light(s)/switch(es).</b> Click for MagicCube to independantly control different sets of lights/switches (eg a light and a fan).", defaultValue: false, submitOnChange:true
						paragraph "$infoIcon Use this option if you only want to control one light or set of lights. Change this option if, for instance, you want to control some lights with a 90° flip, and <i>different lights</i> with a 180° flip."
						input "controlDevice", "capability.switch", title: "Device(s) to control", multiple: true, required: true, submitOnChange:true
					} else {
						input "multiDevice", "bool", title: "Mutli-control: <b>Independantly control different sets of lights/switches.</b> Click for MagicCube to control only one set of lights/switches.", defaultValue: true, submitOnChange:true
						paragraph "$infoIcon Use this option if you only want to control one light or set of lights. Change this option if, for instance, you want to control some lights with a 90° flip, and <i>different lights</i> with a 180° flip."
					}
					if(advancedSetup){
						paragraph "$infoIcon <b>Pro-tip</b>: Profiles for Multi-control enabled and disabled are stored separatly, allowing toggling between two different setups. To do this, set the options both with Multi-control disabled and enabled."
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
							paragraph "$infoIcon Multiplier/divider for dimming and brightening, from 1.01 to 99, where higher is faster. For instance, a value of 2 would double (eg from 25% to 50%, then 100%), whereas a value of 1.5 would increase by half each time (eg from 25% to 38% to 57%)."
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
					input "button_3_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
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
					paragraph "$infoIcon Multiplier/divider for dimming and brightening, from 1.01 to 99, where higher is faster. For instance, a value of 2 would double (eg from 25% to 50%, then 100%), whereas a value of 1.5 would increase by half each time (eg from 25% to 38% to 57%)."
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
    logTrace(336,"Installed","trace")
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    logTrace(342,"Updated","trace")
    unsubscribe()
    initialize()
}

def initialize() {
    logTrace(348,"Initialized","trace")

    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

    subscribe(buttonDevice, "pushed.1", buttonEvent)
    subscribe(buttonDevice, "pushed.2", buttonEvent)
    subscribe(buttonDevice, "pushed.3", buttonEvent)
    subscribe(buttonDevice, "pushed.5", buttonEvent)
    subscribe(buttonDevice, "pushed.6", buttonEvent)
    subscribe(buttonDevice, "pushed.7", buttonEvent)
}

def buttonEvent(evt){
    atomicState.buttonNumber = evt.value

    logTrace(363,"$evt.displayName $evt.value","info")
    if(pushMultiplier) pushMultiplier = parent.validateMultiplier(pushMultiplier,app.label)
    if(holdMultiplier) holdMultiplier = parent.validateMultiplier(holdMultiplier,app.label)

    // Set device if using simple setup
    if(!multiDevice){
        if((atomicState.buttonNumber == 1 && shake && shake == "on") ||
           (atomicState.buttonNumber == 2 && f90 && f90 == "on") ||
           (atomicState.buttonNumber == 3 && f180 && f180 == "on") ||
           (atomicState.buttonNumber == 4 && slide && slide == "on") ||
           (atomicState.buttonNumber == 5 && knock && knock == "on") ||
           (atomicState.buttonNumber == 6 && clockwise && clockwise == "on") ||
           (atomicState.buttonNumber == 7 && counterClockwise && counterClockwise == "on")){
        multiOn("on",controlDevice)
        } else if((atomicState.buttonNumber == 1 && shake && shake == "off") ||
           (atomicState.buttonNumber == 2 && f90 && f90 == "off") ||
           (atomicState.buttonNumber == 3 && f180 && f180 == "off") ||
           (atomicState.buttonNumber == 4 && slide && slide == "off") ||
           (atomicState.buttonNumber == 5 && knock && knock == "off") ||
           (atomicState.buttonNumber == 6 && clockwise && clockwise == "off") ||
           (atomicState.buttonNumber == 7 && counterClockwise && counterClockwise == "off")){
            multiOn("off",controlDevice)
            } else if((atomicState.buttonNumber == 1 && shake && shake == "dim") ||
           (atomicState.buttonNumber == 2 && f90 && f90 == "dim") ||
           (atomicState.buttonNumber == 3 && f180 && f180 == "dim") ||
           (atomicState.buttonNumber == 4 && slide && slide == "dim") ||
           (atomicState.buttonNumber == 5 && knock && knock == "dim") ||
           (atomicState.buttonNumber == 6 && clockwise && clockwise == "dim") ||
           (atomicState.buttonNumber == 7 && counterClockwise && counterClockwise == "dim")){
            parent.dim("dim",controlDevice,app.getId())
             } else if((atomicState.buttonNumber == 1 && shake && shake == "brighten") ||
           (atomicState.buttonNumber == 2 && f90 && f90 == "brighten") ||
           (atomicState.buttonNumber == 3 && f180 && f180 == "brighten") ||
           (atomicState.buttonNumber == 4 && slide && slide == "brighten") ||
           (atomicState.buttonNumber == 5 && knock && knock == "brighten") ||
           (atomicState.buttonNumber == 6 && clockwise && clockwise == "brighten") ||
           (atomicState.buttonNumber == 7 && counterClockwise && counterClockwise == "brighten")){
            parent.dim("brighten",controlDevice,app.getId())
             } else if((atomicState.buttonNumber == 1 && shake && shake == "toggle") ||
           (atomicState.buttonNumber == 2 && f90 && f90 == "toggle") ||
           (atomicState.buttonNumber == 3 && f180 && f180 == "toggle") ||
           (atomicState.buttonNumber == 4 && slide && slide == "toggle") ||
           (atomicState.buttonNumber == 5 && knock && knock == "toggle") ||
           (atomicState.buttonNumber == 6 && clockwise && clockwise == "toggle") ||
           (atomicState.buttonNumber == 7 && counterClockwise && counterClockwise == "toggle")){
            multiOn("toggle",controlDevice)
        } else {
            logTrace(410,"No action defined for $atomicState.buttonNumber of $evt.displayName","trace")
        }
    } else {
        if(settings["button_${atomicState.buttonNumber}_on"]) multiOn("on",settings["button_${atomicState.buttonNumber}_on"])
        if(settings["button_${atomicState.buttonNumber}_off"]) multiOn("off",settings["button_${atomicState.buttonNumber}_off"])
        if(settings["button_${atomicState.buttonNumber}_dim"]) parent.dim("dim",settings["button_${atomicState.buttonNumber}_dim"],app.getId())
        if(settings["button_${atomicState.buttonNumber}_brighten"]) parent.dim("brighten",settings["button_${atomicState.buttonNumber}_brighten"],app.getId())
        if(settings["button_${atomicState.buttonNumber}_toggle"]) multiOn("toggle",settings["button_${atomicState.buttonNumber}_toggle"])
        if(!button_1_toggle && !button_1_on && !button_1_off && !button_1_dim && !button_1_brighten){
            logTrace(419,"No action defined for $atomicState.buttonNumber of $evt.displayName","trace")
        }
    }
}

def getDevice(deviceId){
// I'm betting this doesn't work right.
        if(multiDevice){
        if(settings["button_${atomicState.buttonNumber}_on"]) {
            device = settings["button_${atomicState.buttonNumber}_on"]
        } else if(settings["button_${atomicState.buttonNumber}_off"]) {
            device = settings["button_${atomicState.buttonNumber}_off"]
        } else if(settings["button_${atomicState.buttonNumber}_dim"]) {
            device = settings["button_${atomicState.buttonNumber}_dim"] 
        } else if(settings["button_${atomicState.buttonNumber}_brighten"]) {
            device = settings["button_${atomicState.buttonNumber}_brighten"] 
        } else if(settings["button_${atomicState.buttonNumber}_toggle"]) {
            device = settings["button_${atomicState.buttonNumber}_toggle"] 
        }
    } else {
        if((shake && atomicState.buttonNumber == 1) ||  
           (f90 && atomicState.buttonNumber == 2) || 
           (f180 && atomicState.buttonNumber == 3) || 
           (slide && atomicState.buttonNumber == 4) || 
           (knock && atomicState.buttonNumber == 5) || 
           (clockwise && atomicState.buttonNumber == 6) || 
           (counterClockwise && atomicState.buttonNumber == 7))
            device = controlDevice
    }
    return device
}

def multiOn(action,device){
    if(!action || (action != "on" && action != "off" && action != "toggle")) {
        logTrace(453,"Invalid action \"$action\" sent to multiOn", "error")
        return
    }

    device.each{
        if(action == "toggle"){
            if(parent.isOn(it,childLabel)){
                newAction = "off"
            } else {
                newAction = "on"
            }
        } else {
            newAction = action
        }
        parent.singleOn(newAction,it,childLabel)
        data = [deviceId: it.id, action: newAction, getLevel: true, childLabel: app.label]
        parent.runRetrySchedule(data)
    }
}


//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def logTrace(lineNumber,message = null, type = "trace"){
    message = (message ? " -- $message" : "")
    if(lineNumber) message = "(line $lineNumber)$message"
    message = "$app.label $message"
    switch(type) {
        case "error":
        log.error message
        break
        case "warn":
        log.warn message
        break
        case "info":
        log.info message
        break
        case "debug":
        //log.debug message
        break
        case "trace":
        log.trace message
    }
}
