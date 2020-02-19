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
*  Version: 0.2.11
* 
***********************************************************************************************************************/

definition(
    name: "Master - MagicCube",
    namespace: "master",
    author: "roguetech",
    description: "Control MagicCube",
    parent: "master:Master",
    category: "Convenience",
    importUrl: "https://raw.githubusercontent.com/roguetech2/hubitat/master/Master%20-%20MagicCube.groovy",
    iconUrl: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/mi_face_s.png",
    iconX2Url: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/mi_face.png"
)

preferences {
    infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
    warningIcon = "<img src=\"http://emily-john.love/icons/warning.png\" width=20 height=20>"
    errorIcon = "<img src=\"http://emily-john.love/icons/error.png\" width=20 height=20>"
    page(name: "setup", install: true, uninstall: true) {
		section() {
			displayNameOption()
			if(app.label){
				displayCubeOption()
				if(buttonDevice){
					paragraph "$infoIcon For each action, select which lights or switches to turn on, turn off, toggle, dim/slow, and/or brighten/speed up."
					if(!advancedSetup){
						input "advancedSetup", "bool", title: "<b>Simple actions.</b> Click to show advanced actions.", defaultValue: false, submitOnChange:true
						displayInfo("Hides less common options, such as knock, or using slide to turn a device on. Also hides the option to resume a schedule. If you're not seeing an option you want, click this.")
					} else {
						input "advancedSetup", "bool", title: "<b>Advanced actions.</b> Click to hide advanced actions.", defaultValue: false, submitOnChange:true
					}
					if(!multiDevice){
						input "multiDevice", "bool", title: "Multi-control: <b>Controls one set of light(s)/switch(es).</b> Click for MagicCube to independently control different sets of lights/switches (eg a light and a fan).", defaultValue: false, submitOnChange:true
						displayInfo("Use this option if you only want to assign different devices to different MagicCube actions, for instance, to have 90° flip and a 180° flip to turn on </i>different</i> lights.")
						input "controlDevice", "capability.switch", title: "Device(s) to control", multiple: true, required: true, submitOnChange:true
					} else {
						input "multiDevice", "bool", title: "Mutli-control: <b>Independently control different sets of lights/switches.</b> Click for MagicCube to control only one set of lights/switches.", defaultValue: true, submitOnChange:true
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
							input "flip90", "enum", title: "When <b>flipping 90°</b>, what to do with lights/switches?", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true
						} else {
							input "flip90", "enum", title: "When <b>flipping 90°</b>, what to do with lights/switches?", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
						}
						if(advancedSetup){
							input "flip180", "enum", title: "When <b>flipping 180°</b>, what to do with lights/switches?", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true
						} else {
							input "flip180", "enum", title: "When <b>flipping 180°</b>, what to do with lights/switches?", required: false, multiple: false, width: 6, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
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

						if(clockwise == "dim" || clockwise == "brighten" || flip90 == "dim" || flip90 == "brighten" || flip180 == "dim" || flip180 == "brighten" || shake == "dim" || shake == "brighten" || knock == "dim" || knock == "brighten"){
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
                                button = [1,"on"]
                                getAdvancedSwitchInput(button)

                                button = [1,"off"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"on"))

                                button = [1,"toggle"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"on"))
                                errorMessage(compareDeviceLists(button,"off"))

				if(advancedSetup){
                                    button = [1,"resume"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"on"))
                                    errorMessage(compareDeviceLists(button,"off"))
                                    errorMessage(compareDeviceLists(button,"toggle"))

                                    button = [1,"dim"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"off"))

                                    button = [1,"brighten"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"off"))
                                    errorMessage(compareDeviceLists(button,"dim"))
				}
			}
			
			section(hideable: true, hidden: true, "Rotating clockwise <font color=\"gray\">(Click to expand/collapse)</font>") {
                                button = [6,"brighten"]
                                getAdvancedSwitchInput(button)

                                button = [6,"dim"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"brighten"))

                                button = [6,"toggle"]
                                getAdvancedSwitchInput(button)

				if(advancedSetup){
                                    button = [6,"on"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"toggle"))

                                    button = [6,"off"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"toggle"))
                                    errorMessage(compareDeviceLists(button,"on"))

                                    button = [6,"resume"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"toggle"))
                                    errorMessage(compareDeviceLists(button,"on"))
                                    errorMessage(compareDeviceLists(button,"off"))
				}
			}
			section(hideable: true, hidden: true, "Rotating counter clockwise <font color=\"gray\">(Click to expand/collapse)</font>") {
                                button = [7,"dim"]
                                getAdvancedSwitchInput(button)

                                button = [7,"brighten"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"dim"))

                                button = [7,"toggle"]
                                getAdvancedSwitchInput(button)

				if(advancedSetup){
                                    button = [7,"on"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"toggle"))

                                    button = [7,"off"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"dim"))
                                    errorMessage(compareDeviceLists(button,"brighten"))
                                    errorMessage(compareDeviceLists(button,"toggle"))
                                    errorMessage(compareDeviceLists(button,"on"))

                                    button = [7,"resume"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"toggle"))
                                    errorMessage(compareDeviceLists(button,"on"))
                                    errorMessage(compareDeviceLists(button,"off"))
				}
			}
			section(hideable: true, hidden: true, "90° flipping <font color=\"gray\">(Click to expand/collapse)</font>") {
                                button = [2,"on"]
                                getAdvancedSwitchInput(button)

                                button = [2,"off"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"on"))

                                button = [2,"toggle"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"on"))
                                errorMessage(compareDeviceLists(button,"off"))

				if(advancedSetup){
                                    button = [2,"resume"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"toggle"))
                                    errorMessage(compareDeviceLists(button,"on"))
                                    errorMessage(compareDeviceLists(button,"off"))

                                    button = [2,"dim"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"off"))

                                    button = [2,"brighten"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"off"))
                                    errorMessage(compareDeviceLists(button,"dim"))
				}
			}
			section(hideable: true, hidden: true, "180° flipping <font color=\"gray\">(Click to expand/collapse)</font>") {
                                button = [3,"on"]
                                getAdvancedSwitchInput(button)

                                button = [3,"off"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"on"))

                                button = [3,"toggle"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"on"))
                                errorMessage(compareDeviceLists(button,"off"))

				if(advancedSetup){
                                    button = [3,"resume"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"on"))
                                    errorMessage(compareDeviceLists(button,"off"))
                                    errorMessage(compareDeviceLists(button,"toggle"))

                                    button = [3,"dim"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"off"))

                                    button = [3,"brighten"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"off"))
                                    errorMessage(compareDeviceLists(button,"dim"))
				}
			}
			if(advancedSetup){
				section(hideable: true, hidden: true, "Sliding <font color=\"gray\">(Click to expand/collapse)</font>") {
                                    button = [4,"dim"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"off"))

                                    button = [4,"brighten"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"off"))
                                    errorMessage(compareDeviceLists(button,"dim"))

                                    button = [4,"on"]
                                    getAdvancedSwitchInput(button)

                                    button = [4,"off"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"on"))

                                    button = [4,"toggle"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"on"))
                                    errorMessage(compareDeviceLists(button,"off"))

                                    button = [4,"resume"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"on"))
                                    errorMessage(compareDeviceLists(button,"off"))
                                    errorMessage(compareDeviceLists(button,"toggle"))
				}
			}
			if(advancedSetup){
				section(hideable: true, hidden: true, "Knocking <font color=\"gray\">(Click to expand/collapse)</font>") {
                                    button = [5,"toggle"]
                                    getAdvancedSwitchInput(button)

                                    button = [5,"on"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"toggle"))

                                    button = [5,"off"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"toggle"))
                                    errorMessage(compareDeviceLists(button,"on"))

                                    button = [5,"resume"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"on"))
                                    errorMessage(compareDeviceLists(button,"off"))
                                    errorMessage(compareDeviceLists(button,"toggle"))

                                    button = [5,"dim"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"off"))

                                    button = [5,"brighten"]
                                    getAdvancedSwitchInput(button)
                                    errorMessage(compareDeviceLists(button,"off"))
                                    errorMessage(compareDeviceLists(button,"dim"))
				}
			}
			if(button_1_dim || button_1_brighten || button_2_dim || button_2_brighten || button_3_dim || button_3_brighten || button_4_dim || button_4_brighten || button_5_dim || button_5_brighten || button_6_dim || button_6_brighten || button_7_dim || button_7_brighten){
				section(){
					displayMultiplierOption()
				}
			}
/* ************************************************************************ */
/* TO-DO: Check if this is the right place for error messages.              */
/* ************************************************************************ */
                    section(){
                        if(error) paragraph "$error</div>"
                    }
		}
    }
}

def errorMessage(text){
    if(!text) return false
    if(error){
        error += "<br />$errorIcon $text"
    } else {
        error = "<div style=\"background-color:Bisque\">$errorIcon $text"
    }
}

def displayLabel(text){
    if(!text) {
        paragraph "<div style=\"background-color:BurlyWood\"> </div>"
    } else {
        paragraph "<div style=\"background-color:BurlyWood\"><b> $text:</b></div>"
    }
}

def displayInfo(text = ""){
    if(text == "") {
        paragraph "<div style=\"background-color:AliceBlue\"> </div>"
    } else {
        paragraph "<div style=\"background-color:AliceBlue\">$infoIcon $text</div>"
    }
}

def displayNameOption(){
    displayLabel("Set name for this MagicCube setup")
    label title: "", required: true, submitOnChange:true
    if(!app.label) displayInfo("Name this MagicCube setup. Each MagicCube setup must have a unique name.")
/* ************************************************************************ */
/* TO-DO: Need to test if the app name is unique. BUT we can't call the     */
/* parent app during setup. So we need to test this during initialize...    */
/* and what? Auto-rename it? Maybe better than it not working right.        */
/* Alternatively, we could switch everything over to app.id o.o             */
/* ************************************************************************ */
}

def displayCubeOption(){
    displayLabel("Select MagicCube device(s) to setup")
    input "buttonDevice", "capability.pushableButton", title: "MagicCube(s)?", multiple: true, submitOnChange:true
    if(!buttonDevice) displayInfo("Select which MagicCube(s) to control. You can select multiple MagicCubes devices.")
}

// values sent as list
// values.0 = number
// values.1 = on/off/toggle/dim/brighten
// populated = value of the input
def getAdvancedSwitchInput(values,populated = null){
    if(error) return
    text = ""
    if(populated) text += "<b>"
    if(values[1] == "on"){
        text += "Turns On"
    } else if(values[1] == "off"){
        text += "Turns Off"
    } else if(values[1] == "toggle"){
        text += "Toggles (if on, turn off; if off, turn on)"
    } else if(values[1] == "dim"){
        text += "Dims"
    } else if(values[1] == "brighten"){
        text += "Brightens"
    } else if(values[1] == "resume"){
        text += "Resume schedule(s) (if none, turn off)"
    }	
    if(populated) {
        text += "</b>"
    } else {
        text += " <font color=\"gray\">(Select devices)</font>"
    }
    if(values[1] == "dim" || values[1] == "brighten"){
        switchType = "switch"
    } else {
        switchType = "switchLevel"
    }
    input "button_" + values[0] + "_" + values[1], "capability.$switchType", title: "$text", multiple: true, width: 6, submitOnChange:true
}
def displayMultiplierOption(){
    displayLabel("Set dim and brighten speed")
    displayMultiplierMessage()
    if(button_1_hold_dim || button_1_hold_brighten || button_2_hold_dim || button_2_hold_brighten || button_3_hold_dim || button_3_hold_brighten || button_4_hold_dim || button_4_hold_brighten || button_5_hold_dim || button_5_hold_brighten){
        input "multiplier", "decimal", title: "Multiplier? (Optional. Default 1.2.)", width: 6
    } else {
        input "multiplier", "decimal", title: "Multiplier? (Optional. Default 1.2.)", width: 12
    }
}

/* ************************************************************************ */
/*                                                                          */
/* End display functions.                                                   */
/*                                                                          */
/* ************************************************************************ */

def installed() {
    logTrace(421,"Installed","trace")
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    logTrace(427,"Updated","trace")
    unsubscribe()
    initialize()
}

def initialize() {
    logTrace(433,"Initialized","trace")

    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

    subscribe(buttonDevice, "pushed.1", buttonEvent)
    subscribe(buttonDevice, "pushed.2", buttonEvent)
    subscribe(buttonDevice, "pushed.3", buttonEvent)
    subscribe(buttonDevice, "pushed.5", buttonEvent)
    subscribe(buttonDevice, "pushed.6", buttonEvent)
    subscribe(buttonDevice, "pushed.7", buttonEvent)
}

def buttonEvent(evt){
    buttonNumber = evt.value

    logTrace(448,"$evt.displayName $evt.value","info")
    if(multiplier) multiplier = parent.validateMultiplier(multiplier,app.label)

/* ************************************************************************ */
/* TO-DO: Clean this shit up (if we can)!                                   */
/* ************************************************************************ */
    // Set device if using simple setup
    if(!multiDevice){
        if((buttonNumber == 1 && shake && shake == "on") ||
           (buttonNumber == 2 && flip90 && flip90 == "on") ||
           (buttonNumber == 3 && flip180 && flip180 == "on") ||
           (buttonNumber == 4 && slide && slide == "on") ||
           (buttonNumber == 5 && knock && knock == "on") ||
           (buttonNumber == 6 && clockwise && clockwise == "on") ||
           (buttonNumber == 7 && counterClockwise && counterClockwise == "on")){
        multiOn("on",controlDevice)
        } else if((buttonNumber == 1 && shake && shake == "off") ||
           (buttonNumber == 2 && flip90 && flip90 == "off") ||
           (buttonNumber == 3 && flip180 && flip180 == "off") ||
           (buttonNumber == 4 && slide && slide == "off") ||
           (buttonNumber == 5 && knock && knock == "off") ||
           (buttonNumber == 6 && clockwise && clockwise == "off") ||
           (buttonNumber == 7 && counterClockwise && counterClockwise == "off")){
            multiOn("off",controlDevice)
            } else if((buttonNumber == 1 && shake && shake == "dim") ||
           (buttonNumber == 2 && flip90 && flip90 == "dim") ||
           (buttonNumber == 3 && flip180 && flip180 == "dim") ||
           (buttonNumber == 4 && slide && slide == "dim") ||
           (buttonNumber == 5 && knock && knock == "dim") ||
           (buttonNumber == 6 && clockwise && clockwise == "dim") ||
           (buttonNumber == 7 && counterClockwise && counterClockwise == "dim")){
            parent.dim("dim",controlDevice,app.getId())
             } else if((buttonNumber == 1 && shake && shake == "brighten") ||
           (buttonNumber == 2 && flip90 && flip90 == "brighten") ||
           (buttonNumber == 3 && flip180 && flip180 == "brighten") ||
           (buttonNumber == 4 && slide && slide == "brighten") ||
           (buttonNumber == 5 && knock && knock == "brighten") ||
           (buttonNumber == 6 && clockwise && clockwise == "brighten") ||
           (buttonNumber == 7 && counterClockwise && counterClockwise == "brighten")){
            parent.dim("brighten",controlDevice,app.getId())
             } else if((buttonNumber == 1 && shake && shake == "toggle") ||
           (buttonNumber == 2 && flip90 && flip90 == "toggle") ||
           (buttonNumber == 3 && flip180 && flip180 == "toggle") ||
           (buttonNumber == 4 && slide && slide == "toggle") ||
           (buttonNumber == 5 && knock && knock == "toggle") ||
           (buttonNumber == 6 && clockwise && clockwise == "toggle") ||
           (buttonNumber == 7 && counterClockwise && counterClockwise == "toggle")){
            multiOn("toggle",controlDevice)
        } else {
            logTrace(497,"No action defined for $buttonNumber of $evt.displayName","trace")
        }
    } else {
        if(settings["button_${buttonNumber}_on"]) multiOn("on",settings["button_${buttonNumber}_on"])
        if(settings["button_${buttonNumber}_off"]) multiOn("off",settings["button_${buttonNumber}_off"])
        if(settings["button_${buttonNumber}_dim"]) parent.dim("dim",settings["button_${buttonNumber}_dim"],app.getId())
        if(settings["button_${buttonNumber}_brighten"]) parent.dim("brighten",settings["button_${buttonNumber}_brighten"],app.getId())
        if(settings["button_${buttonNumber}_toggle"]) multiOn("toggle",settings["button_${buttonNumber}_toggle"])
        if(!button_1_toggle && !button_1_on && !button_1_off && !button_1_dim && !button_1_brighten){
            logTrace(506,"No action defined for $buttonNumber of $evt.displayName","trace")
        }
    }
}

/* ************************************************************************ */
/* TO-DO: Retest "multiplier" functionality (also in Pico). Make sure       */
/* it's implemented in the UI, and it carries through in the logic. Also    */
/* rename the variables. "Multiplier" is just stupid.                       */
/* ************************************************************************ */
def getDimSpeed(){
    if(multiplier){
        return multiplier
    } else {
        return 1.2
    }
}

// Gets levels as set for the app
// Function must be included in all apps that use MultiOn
def getOverrideLevels(defaults,appAction = null){
    // Need to add levels
    return defaults       
}

// If deviceChange exists, adds deviceId to it; otherwise, creates deviceChange with deviceId
// Used to track if app turned on device when schedule captures a device state changing to on
// Must be included in all apps using MultiOn
def addStateDeviceChange(singleDeviceId){
    if(atomicState.deviceChange) {
        atomicState.deviceChange = "$atomicState.deviceChange:$singleDeviceId:"
    } else {
        atomicState.deviceChange = ":$singleDeviceId:"
    }
}

// Returns the value of deviceChange
// Used by schedule when a device state changes to on, to check if an app did it
// Function must be in every app
def getStateDeviceChange(singleDeviceId){
    if(atomicState.deviceChange){
        return atomicState.deviceChange.indexOf(":$singleDeviceId:")
    } else {
        return false
    }
}

// Scheduled funtion to reset the value of deviceChange
// Must be in every app using MultiOn
def resetStateDeviceChange(){
    atomicState.deviceChange = null
    return
}

// This is a bit of a mess, but.... 
def multiOn(deviceAction,device,appAction = null){
    if(!deviceAction || (deviceAction != "on" && deviceAction != "off" && deviceAction != "toggle" && deviceAction != "resume" && deviceAction != "none")) {
        logTrace(563,"Invalid deviceAction \"$deviceAction\" sent to multiOn","error")
        return
    }

    if(deviceAction == "off"){
        // Turn off devices
        parent.setStateMulti("off",device,app.label)
        return true
    }
    
    if(deviceAction == "on"){
         // Add device ids to deviceChange, so schedule knows it was turned on by an app
        device.each{
            addStateDeviceChange(it.id)
            // Time to schedule resetting deviceChange should match total time of waitStateChange
            // Might be best to put this in a state variable in Initialize, as a setting?
            runIn(2,resetStateDeviceChange)
        }
        logTrace(581,"Device id's turned on are $atomicState.deviceChange","debug")
        
        // Turn on devices
        parent.setStateMulti("on",device,app.label)
        // Get and set defaults levels for each device
        device.each{
            // If defaults, then there's an active schedule
            // So use it for if overriding/reenabling
            defaults = parent.getScheduleDefaultSingle(it,app.label)
            logMessage = defaults ? "Device is scheduled for $defaults" : "Device has no scheduled default levels"
            logTrace(591,logMessage,"debug")

            defaults = getOverrideLevels(defaults,appAction)
            logMessage = defaults ? "With " + app.label + " overrides, using $defaults": "With no override levels" 
            logTrace(595,logMessage,"debug")

            // Set default levels, for level and temp, if no scheduled defaults (don't need to do for "resume")
            defaults = parent.getDefaultSingle(defaults,app.label)
            logTrace(599,"With generic defaults, using $defaults","debug")
            parent.setLevelSingle(defaults.level,defaults.temp,defaults.hue,defaults.sat,it,app.label)
        }
        return true
    }

    if(deviceAction == "toggle"){
        // Create toggleOnDevice variable, used to track which devices are being turned on
        toggleOnDevice = []
        // Set count variable, used for toggleOnDevice
        count = 0
        device.each{
            count = count + 1
            // Get original state
            deviceState = parent.isOn(it)
            // If toggling to off
            if(deviceState){
                parent.setStateSingle("off",it,app.label)
                // Else if toggling on
            } else {
                // When turning on, add device id to deviceChange, so schedule knows it was turned on by an app
                addStateDeviceChange(it.id)
                runIn(1,resetStateDeviceChange)
                parent.setStateSingle("on",it,app.label)
                // Create list of devices toggled on
                // This lets us turn all of them on, then loop again so when setting levels, it won't wait for each individual device to respond
                toggleOnDevice.add(count)
            }
        }
        logTrace(628,"Device id's turned on are $atomicState.deviceChange","debug")
        // Create newCount variable, which is compared to the [old]count variable
        // Used to identify which lights were turned on in the last loop
        newCount = 0
        device.each{
            newCount = newCount + 1
            // If turning on, set default levels and over-ride with any contact levels
            if(toggleOnDevice.contains(newCount)){
                // If defaults, then there's an active schedule
                // So use it for if overriding/reenabling
                defaults = parent.getScheduleDefaultSingle(it,app.label)

                // Set default levels, for level and temp, if no scheduled defaults (don't need to do for "resume")
                defaults = parent.getDefaultSingle(defaults,app.label)

                // Set default level
                if(defaults){
                    parent.setLevelSingle(defaults.level,defaults.temp,defaults.hue,defaults.sat,it,app.label)
                } else {
                    parent.setStateSingle("off",it,app.label)
                }

                // If toggling on, reschedule incremental
                if(!deviceState) parent.rescheduleIncrementalSingle(it,app.label)
            }
        }
        return true
    }

    if(deviceAction == "resume"){
        device.each{
            // If turning on, set default levels and over-ride with any contact levels
            if(deviceAction == "resume"){
                // If defaults, then there's an active schedule
                // So use it for if overriding/reenabling
                defaults = parent.getScheduleDefaultSingle(it,app.label)
                logTrace(664,"Scheduled defaults are $defaults","debug")

                defaults = getOverrideLevels(defaults,appAction)
                logTrace(667,"With " + app.label + " overrides, using $defaults","debug")
                
                parent.setLevelSingle(defaults.level,defaults.temp,defaults.hue,defaults.sat,it,app.label)
                // Set default level
                if(!defaults){
                    logTrace(672,"No schedule to resume for $it; turning off","trace")
                    parent.setStateSingle("off",it,app.label)
                }

            }
        }
        return true
    }

    if(deviceAction == "none"){
        // If doing nothing, reschedule incremental changes (to reset any overriding of schedules)
        parent.rescheduleIncrementalMulti(device,app.label)
        return true
    }
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def logTrace(lineNumber,message = null, type = "trace"){
    // Uncomment return for no logging at all
    // return

    // logLevel sets number of log messages
    // 1 for least (errors only)
    // 5 for most (all)
    logLevel = 5

    message = (message ? " -- $message" : "")
    if(lineNumber) message = "(line $lineNumber)$message"
    message = "$app.label $message"
    switch(type) {
        case "error":
        log.error message
        break
        case "warn":
        if(logLevel > 1) log.warn message
        break
        case "info":
        if(logLevel > 2) log.info message
        break
        case "trace":
        if(logLevel > 3) log.trace message
        break
        case "debug":
        if(loglevel == 5) log.debug message
    }
    return true
}
