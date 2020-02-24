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
*  Version: 0.2.25
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

// logLevel sets number of log messages
// 0 for none
// 1 for errors only
// 5 for all
def getLogLevel(){
    return 5
}

preferences {
    infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
    warningIcon = "<img src=\"http://emily-john.love/icons/warning.png\" width=20 height=20>"
    errorIcon = "<img src=\"http://emily-john.love/icons/error.png\" width=20 height=20>"
    page(name: "setup", install: true, uninstall: true) {
		section() {
			
                displayNameOption()
			if(app.label){
                    displayMagicCubeOption()
				if(buttonDevice){
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

                           button = [1,"resume"]
                           getAdvancedSwitchInput(button)
                           errorMessage(compareDeviceLists(button,"on"))
                           errorMessage(compareDeviceLists(button,"off"))
                           errorMessage(compareDeviceLists(button,"toggle"))

                           if(advancedSetup){
                               button = [1,"dim"]
                               getAdvancedSwitchInput(button)
                               errorMessage(compareDeviceLists(button,"off"))
                               errorMessage(compareDeviceLists(button,"resume"))

                               button = [1,"brighten"]
                               getAdvancedSwitchInput(button)
                               errorMessage(compareDeviceLists(button,"off"))
                               errorMessage(compareDeviceLists(button,"resume"))
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
                               errorMessage(compareDeviceLists(button,"brighten"))
                               errorMessage(compareDeviceLists(button,"dim"))
                               errorMessage(compareDeviceLists(button,"toggle"))
                               errorMessage(compareDeviceLists(button,"on"))

                               button = [6,"resume"]
                               getAdvancedSwitchInput(button)
                               errorMessage(compareDeviceLists(button,"brighten"))
                               errorMessage(compareDeviceLists(button,"dim"))
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
                               errorMessage(compareDeviceLists(button,"brighten"))
                               errorMessage(compareDeviceLists(button,"dim"))
                               errorMessage(compareDeviceLists(button,"toggle"))
                               errorMessage(compareDeviceLists(button,"on"))

                               button = [7,"resume"]
                               getAdvancedSwitchInput(button)
                               errorMessage(compareDeviceLists(button,"brighten"))
                               errorMessage(compareDeviceLists(button,"dim"))
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

                           button = [2,"resume"]
                           getAdvancedSwitchInput(button)
                           errorMessage(compareDeviceLists(button,"on"))
                           errorMessage(compareDeviceLists(button,"off"))
                           errorMessage(compareDeviceLists(button,"toggle"))

                           if(advancedSetup){
                               button = [2,"dim"]
                               getAdvancedSwitchInput(button)
                               errorMessage(compareDeviceLists(button,"off"))
                               errorMessage(compareDeviceLists(button,"resume"))

                               button = [2,"brighten"]
                               getAdvancedSwitchInput(button)
                               errorMessage(compareDeviceLists(button,"off"))
                               errorMessage(compareDeviceLists(button,"resume"))
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

                           button = [3,"resume"]
                           getAdvancedSwitchInput(button)
                           errorMessage(compareDeviceLists(button,"on"))
                           errorMessage(compareDeviceLists(button,"off"))
                           errorMessage(compareDeviceLists(button,"toggle"))

                           if(advancedSetup){
                               button = [3,"dim"]
                               getAdvancedSwitchInput(button)
                               errorMessage(compareDeviceLists(button,"off"))
                               errorMessage(compareDeviceLists(button,"resume"))

                               button = [3,"brighten"]
                               getAdvancedSwitchInput(button)
                               errorMessage(compareDeviceLists(button,"off"))
                               errorMessage(compareDeviceLists(button,"resume"))
                               errorMessage(compareDeviceLists(button,"dim"))
                           }
			}
			if(advancedSetup){
				section(hideable: true, hidden: true, "Sliding <font color=\"gray\">(Click to expand/collapse)</font>") {
                               button = [4,"dim"]
                               getAdvancedSwitchInput(button)

                               button = [4,"brighten"]
                               getAdvancedSwitchInput(button)

                               button = [4,"on"]
                               getAdvancedSwitchInput(button)

                               button = [4,"off"]
                               getAdvancedSwitchInput(button)
                               errorMessage(compareDeviceLists(button,"dim"))
                               errorMessage(compareDeviceLists(button,"brighten"))
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

				section(hideable: true, hidden: true, "Knocking <font color=\"gray\">(Click to expand/collapse)</font>") {
                               button = [5,"on"]
                               getAdvancedSwitchInput(button)

                               button = [5,"off"]
                               getAdvancedSwitchInput(button)
                               errorMessage(compareDeviceLists(button,"on"))

                               button = [5,"toggle"]
                               getAdvancedSwitchInput(button)
                               errorMessage(compareDeviceLists(button,"on"))
                               errorMessage(compareDeviceLists(button,"off"))

                               button = [5,"resume"]
                               getAdvancedSwitchInput(button)
                               errorMessage(compareDeviceLists(button,"on"))
                               errorMessage(compareDeviceLists(button,"off"))
                               errorMessage(compareDeviceLists(button,"toggle"))

                               button = [5,"dim"]
                               getAdvancedSwitchInput(button)
                               errorMessage(compareDeviceLists(button,"off"))
                               errorMessage(compareDeviceLists(button,"resume"))

                               button = [5,"brighten"]
                               getAdvancedSwitchInput(button)
                               errorMessage(compareDeviceLists(button,"off"))
                               errorMessage(compareDeviceLists(button,"resume"))
                               errorMessage(compareDeviceLists(button,"dim"))
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

def errorMessage(text){
    if(!text) return false
    if(error){
        error = error + "<br />$errorIcon $text"
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
    if(!app.label) displayInfo("Name this MagicCube setup. Each Pico setup must have a unique name.")
    /* ************************************************************************ */
    /* TO-DO: Need to test if the app name is unique. BUT we can't call the     */
    /* parent app during setup. So we need to test this during initialize...    */
    /* and what? Auto-rename it? Maybe better than it not working right.        */
    /* Alternatively, we could switch everything over to app.id o.o             */
    /* ************************************************************************ */
}

def displayMagicCubeOption(){
    displayLabel("Select MagicCube device(s) to setup")
/* ************************************************************************ */
/* TO-DO: Hypothetically, we could limit list to 7 (or however many with    */
/* depending whether people use exanded number) button devices              */
/* Could we use something from the driver? device.command = "flip90" or     */
/* some crap like that?                                                     */
/* ************************************************************************ */
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
    input "button_" + values[0] + "_" + values[1], "capability.$switchType", title: "$text", multiple: true, submitOnChange:true
}

def compareDeviceLists(values,compare){
    // Be sure we have original button and comparison button values (odds are, we don't)
    // eg if(!button_1_push_on)
    if(!settings["button_" + values[0] + "_" + values[2] + "_" + values[1]]) return
    if(!settings["button_" + values[0] + "_" + values[2] + "_" + compare]) return
    if(error) return

    settings["button_" + values[0] + "_" + values[2] + "_" + values[1]].each{first->
        settings["button_" + values[0] + "_" + values[2] + "_" + compare].each{second->
            if(first.id == second.id) {
                if(compare == "on" || compare == "off"){
                    text1 = "turn $compare"
                } else {
                    text1 = compare
                }
                if(values[1] == "on" || values[1] == "off"){
                    text2 = "turn " + values[1]
                } else {
                    text2 = values[1]
                }
                returnText = "Can't set same button to $text1 and $text2 the same device."
            }
        }
    }
    return returnText
}

def getDimSpeed(){
    if(multiplier){
        return multiplier
    } else {
        return 1.2
    }
}

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

def installed() {
    if(checkLog(a="trace")) putLog(344,"Installed",a)
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    if(checkLog(a="trace")) putLog(350,"Updated",a)
    unsubscribe()
    initialize()
}

def initialize() {
    if(checkLog(a="trace")) putLog(356,"Initialized",a)

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

    if(checkLog(a="info")) putLog(371,"$evt.displayName $evt.value",a)

    // Set device if using simple setup
    if(!multiDevice){
	def switchButtonsInput[:]
	def switchButtonsOutput[:]
	switchButtonInput.put("input":"shake")
	switchButtonInput.put("input":"flip90")
	switchButtonInput.put("input":"flip180")
	switchButtonInput.put("input":"slide")
	switchButtonInput.put("input":"knock")
	switchButtonInput.put("input":"clockwise")
	switchButtonInput.put("input":"counterClockwise")
	switchButtonOutput.put("output":"on")
	switchButtonOutput.put("output":"off")
	switchButtonOutput.put("output":"dim")
	switchButtonOutput.put("output":"brighten")
	switchButtonOutput.put("output":"toggle")

// Loop through button types ("shake", "flip90", etc)
switchButtonInput.each{input->
	// Loop through actions ("on", "off", etc)
	switchButtonOutput.each{output->
		// Check if button type is set to action (eg "if($shake == 'on')")
		if(settings[input.input] && settings[input.input] == output.output){
			// on, off, and toggle use multiOn(); dim and brighten use dim()
			if(output.output == "on" || output.output == "off" || output.output == "toggle") setStateMulti(output.output,controlDevice)
			if(output.output == "dim" || output.output == "brighten) parent.dim(output.output,controlDevice,app.label)
			result = true
		}
	}
}
if(!result && checkLog(a="trace")) putLog(418,"No action defined for $atomicState.buttonNumber of $evt.displayName",a)
/*
        if((atomicState.buttonNumber == 1 && shake && shake == "on") ||
           (atomicState.buttonNumber == 2 && flip90 && flip90 == "on") ||
           (atomicState.buttonNumber == 3 && flip180 && flip180 == "on") ||
           (atomicState.buttonNumber == 4 && slide && slide == "on") ||
           (atomicState.buttonNumber == 5 && knock && knock == "on") ||
           (atomicState.buttonNumber == 6 && clockwise && clockwise == "on") ||
           (atomicState.buttonNumber == 7 && counterClockwise && counterClockwise == "on")){
        setStateMulti("on",controlDevice)
        } else if((atomicState.buttonNumber == 1 && shake && shake == "off") ||
           (atomicState.buttonNumber == 2 && flip90 && flip90 == "off") ||
           (atomicState.buttonNumber == 3 && flip180 && flip180 == "off") ||
           (atomicState.buttonNumber == 4 && slide && slide == "off") ||
           (atomicState.buttonNumber == 5 && knock && knock == "off") ||
           (atomicState.buttonNumber == 6 && clockwise && clockwise == "off") ||
           (atomicState.buttonNumber == 7 && counterClockwise && counterClockwise == "off")){
            setStateMulti("off",controlDevice)
            } else if((atomicState.buttonNumber == 1 && shake && shake == "dim") ||
           (atomicState.buttonNumber == 2 && flip90 && flip90 == "dim") ||
           (atomicState.buttonNumber == 3 && flip180 && flip180 == "dim") ||
           (atomicState.buttonNumber == 4 && slide && slide == "dim") ||
           (atomicState.buttonNumber == 5 && knock && knock == "dim") ||
           (atomicState.buttonNumber == 6 && clockwise && clockwise == "dim") ||
           (atomicState.buttonNumber == 7 && counterClockwise && counterClockwise == "dim")){
            parent.dim("dim",controlDevice,app.getId())
             } else if((atomicState.buttonNumber == 1 && shake && shake == "brighten") ||
           (atomicState.buttonNumber == 2 && flip90 && flip90 == "brighten") ||
           (atomicState.buttonNumber == 3 && flip180 && flip180 == "brighten") ||
           (atomicState.buttonNumber == 4 && slide && slide == "brighten") ||
           (atomicState.buttonNumber == 5 && knock && knock == "brighten") ||
           (atomicState.buttonNumber == 6 && clockwise && clockwise == "brighten") ||
           (atomicState.buttonNumber == 7 && counterClockwise && counterClockwise == "brighten")){
            parent.dim("brighten",controlDevice,app.getId())
             } else if((atomicState.buttonNumber == 1 && shake && shake == "toggle") ||
           (atomicState.buttonNumber == 2 && flip90 && flip90 == "toggle") ||
           (atomicState.buttonNumber == 3 && flip180 && flip180 == "toggle") ||
           (atomicState.buttonNumber == 4 && slide && slide == "toggle") ||
           (atomicState.buttonNumber == 5 && knock && knock == "toggle") ||
           (atomicState.buttonNumber == 6 && clockwise && clockwise == "toggle") ||
           (atomicState.buttonNumber == 7 && counterClockwise && counterClockwise == "toggle")){
            setStateMulti("toggle",controlDevice)
        } else {
            if(checkLog(a="trace")) putLog(418,"No action defined for $atomicState.buttonNumber of $evt.displayName",a)
        }
*/
    } else {
        if(settings["button_${atomicState.buttonNumber}_on"]) setStateMulti("on",settings["button_${atomicState.buttonNumber}_on"])
        if(settings["button_${atomicState.buttonNumber}_off"]) setStateMulti("off",settings["button_${atomicState.buttonNumber}_off"])
        if(settings["button_${atomicState.buttonNumber}_dim"]) parent.dim("dim",settings["button_${atomicState.buttonNumber}_dim"],app.getId())
        if(settings["button_${atomicState.buttonNumber}_brighten"]) parent.dim("brighten",settings["button_${atomicState.buttonNumber}_brighten"],app.getId())
        if(settings["button_${atomicState.buttonNumber}_toggle"]) setStateMulti("toggle",settings["button_${atomicState.buttonNumber}_toggle"])
        if(!button_1_toggle && !button_1_on && !button_1_off && !button_1_dim && !button_1_brighten){
            if(checkLog(a="trace")) putLog(427,"No action defined for $atomicState.buttonNumber of $evt.displayName",a)
        }
    }
}

/* ************************************************************************ */
/*                                                                          */
/*                  Begin (mostly) universal functions.                     */
/*                 Most or all could be moved to Master.                    */
/*                                                                          */
/* ************************************************************************ */

// If deviceChange exists, adds deviceId to it; otherwise, creates deviceChange with deviceId
// Delineate values with colons on each side - must match getStateDeviceChange
// Used to track if app turned on device when schedule captures a device state changing to on
// Must be included in all apps using MultiOn
def addDeviceStateChange(singleDeviceId){
    if(atomicState.deviceChange) {
        atomicState.deviceChange += ":$singleDeviceId:"
    } else {
        atomicState.deviceChange = ":$singleDeviceId:"
    }
    return
}

// Returns the value of deviceChange
// Used by schedule when a device state changes to on, to check if an app did it
// It should only persist as long as it takes for the scheduler to capture and
// process both state change request and state change subscription
// Function must be in every app
def getStateDeviceChange(singleDeviceId){
    if(atomicState.deviceChange){
/* ************************************************************************ */
/* TO-DO: We don't need "indexOf", just .contains, but test first           */
/* ************************************************************************ */
        value = atomicState.deviceChange.indexOf(":$singleDeviceId:")
        // Reset it when it's used, to try and avoid race conditions with multiple fast button clicks
        resetStateDeviceChange()
        return value
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
def setStateMulti(deviceAction,device,appAction = null){
    if(!deviceAction || (deviceAction != "on" && deviceAction != "off" && deviceAction != "toggle" && deviceAction != "none")) {
        if(checkLog(a="error")) putLog(1216,"Invalid deviceAction \"$deviceAction\" sent to setStateMulti",a)
        return
    }

    // Time in which to allow Hubitat to process sensor change (eg Pico, contact, etc.)
    // as well as the scheduler to process any state change generated by the sensor, after
    // which the requesting child-app will "forget" it's the one to have requested any
    // level changes and the schedule not see a state change was from child-app.
    // What's a realistic number to use if someone has a lot of devices attached to a lot 
    // of Picos with a lot of schedules? Probably could be as low as 100 or 250.
    stateDeviceChangeResetMillis = 500

    if(deviceAction == "off"){
        // Reset device change, since we know the last event from this device didn't turn anything on
        resetStateDeviceChange()
        // Turn off devices
        parent.setStateMulti("off",device,app.label)
        return true
    }

    if(deviceAction == "on"){
        // Get list of all devices to be turned on (for schedule overriding)
        device.each{
            // Add device ids to deviceChange, so schedule knows it was turned on by an app
            // Needs to be done before turning the device on.
            addDeviceStateChange(it.id)
        }

        // Turn on devices
        parent.setStateMulti("on",device,app.label)

        // Then set the levels
        device.each{
            // Set scheduled levels, default levels, and/or [this child-app's] levels
            parent.getAndSetSingleLevels(it,appAction,app.label)
        }
        if(checkLog(a="debug")) putLog(1252,"Device id's turned on are $atomicState.deviceChange",a)
        // Schedule deviceChange reset
        runInMillis(stateDeviceChangeResetMillis,resetStateDeviceChange)
        return true
    }

    if(deviceAction == "toggle"){
        // Create toggleOnDevice list, used to track which devices are being toggled on
        toggleOnDevice = []
        // Set count variable, used for toggleOnDevice
        count = 0
        device.each{
            // Start count at 1; doesn't matter, so long as it matches newCount below
            count = count + 1
            // If toggling to off
            if(parent.isOn(it)){
                parent.setStateSingle("off",it,app.label)
                // Else if toggling on
            } else {
                // When turning on, add device ids to deviceChange, so schedule knows it was turned on by an app
                // Needs to be done before turning the device on.
                addDeviceStateChange(it.id)
                // Turn the device on
                parent.setStateSingle("on",it,app.label)
                // Add device to toggleOnDevice list so when we loop again to set levels, we know whether we
                // just turned it on or not (without knowing how long the device may take to respond)
                toggleOnDevice.add(count)
            }
        }
        if(checkLog(a="debug")) putLog(1281,"Device id's toggled on are $atomicState.deviceChange",a)
        // Create newCount variable, which is compared to the [old]count variable
        // Used to identify which lights were turned on in the last loop
        newCount = 0
        device.each{
            // Start newCount at 1 like count above
            newCount = newCount + 1
            // If turning on, set scheduled levels, default levels, and/or [this child-app's] levels
            // If newCount is contained in the list of [old]count, then we toggled on
            if(toggleOnDevice.contains(newCount)){
                parent.getAndSetSingleLevels(it,appAction,app.label)
            }
        }
        // Schedule deviceChange reset
        runInMillis(stateDeviceChangeResetMillis,resetStateDeviceChange)
        return true
    }

    if(deviceAction == "resume"){
        // Reset device change, since we know the last event from this device didn't turn anything on
        resetStateDeviceChange()
        device.each{
            // If defaults, then there's an active schedule
            // So use it for if overriding/reenabling
            defaults = parent.getScheduleDefaultSingle(it,app.label)
            logMessage = defaults ? "$singleDevice scheduled for $defaults" : "$singleDevice has no scheduled default levels"

            // If there are defaults, then there's an active schedule so reschedule it (the results are corrupted below).
            // We could do this for the matching schedules within its own getDefaultLevel(), but that would
            // probably result in incremental schedules rescheduling themselves over and over again. And if we
            // excluded schedules from rescheduling, then daily schedules wouldn't do this.
            if(defaults) parent.rescheduleIncrementalSingle(it,app.label)

            defaults = parent.getOverrideLevels(defaults,appAction, app.label)
            logMessage += defaults ? ", controller overrides of $defaults": ", no controller overrides"

            // Skipping getting overall defaults, since we're resuming a schedule or exiting;
            // rather keep things the same level rather than an arbitrary default, and
            // if we got default, we'd not turn it off

            if(defaults){
                if(checkLog(a="debug")) putLog(1322,logMessage,a)
                parent.setLevelSingle(defaults,it,app.label)
                // Set default level
            } else {
                if(checkLog(a="trace")) putLog(1326,"No schedule to resume for $it; turning off",a)
                parent.setStateSingle("off",it,app.label)
            }
        }
        return true
    }

    if(deviceAction == "none"){
        // Reset device change, since we know the last event from this device didn't turn anything on
        resetStateDeviceChange()
        // If doing nothing, reschedule incremental changes (to reset any overriding of schedules)
        // I think this is the only place we use ...Multi, prolly not enough to justify a separate function
        parent.rescheduleIncrementalMulti(device,app.label)
        return true
    }
}

def checkLog(type = null){
    if(!state.logLevel) getLogLevel()
    switch(type) {
        case "error":
        if(state.logLevel > 0) return "error"
        break
        case "warn":
        if(state.logLevel > 1) return "warn"
        break
        case "info":
        if(state.logLevel > 2) return "info"
        break
        case "trace":
        if(state.logLevel > 3) return "trace"
        break
        case "debug":
        if(state.logLevel == 5) return "debug"
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,message = null,type = "trace"){
    logMessage = ""
    if(type == "error") logMessage += "<font color=\"red\">"
    if(type == "warn") logMessage += "<font color=\"brown\">"
    logMessage += "$app.label "
    if(lineNumber) logMessage += "(line $lineNumber) "
    if(message) logMessage += "-- $message"
    if(type == "error" || type == "warn") logMessage += "</font>"
    switch(type) {
        case "error":
        log.error(logMessage)
        return true
        case "warn":
        log.warn(logMessage)
        return true
        case "info":
        log.info(logMessage)
        return true
        case "trace":
        log.trace(logMessage)
        return true
        case "debug":
        log.debug(logMessage)
        return true
    }
    return
}
