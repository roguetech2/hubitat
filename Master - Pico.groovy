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
*  Name: Master - Pico
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master%20-%20Pico.groovy
*  Version: 0.4.15
*
***********************************************************************************************************************/

definition(
    name: "Master - Pico",
    namespace: "master",
    author: "roguetech",
    description: "Pico and Caseta switches",
    parent: "master:Master",
    category: "Convenience",
    importUrl: "https://raw.githubusercontent.com/roguetech2/hubitat/master/Master%20-%20Pico.groovy",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png"
)

/* ************************************************** */
/* TO-DO: Add option to disable contact or schedule?  */
/* ************************************************** */
/* ************************************************** */
/* TO-DO: Have Pico disable and/or pause schedules,   */
/* eg if schedule is brightening, and Pico dims,      */
/* disable schedule; if schedule is dimming and PIco  */
/* dims lower than schedule is at, pause schedule.    */
/* ************************************************** */
preferences {
	infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
	errorIcon = "<img src=\"http://emily-john.love/icons/error.png\" width=20 height=20>"

    if(app.label && buttonDevice && numButton) install = true
    if(!multiDevice) {
        if(advancedSetup && !buttonPush1 && !buttonPush2 && !buttonPush3 && !buttonPush4 && !buttonPush5) install = false
        if(!advancedSetup && !controlDevice) install = false
    }
    //if(advancedSetup && !buttonPush1 && !buttonPush2 && !buttonPush3 && !buttonPush4 && !buttonPush5 == "on") install = false

    page(name: "setup", install: install, uninstall: true) {
		if(!multiDevice){
			section() {
				displayNameOption()
				if(app.label){
                    displayPicoOption()
					if(buttonDevice){
						displayPicoTypeOption()
						if(numButton){
							displayAdvancedSetupOption()
                            displayMultiDeviceOption()

							if(controlDevice){
								if(advancedSetup){
									displaySelectActionsOption()
								} else {
									if(!replicateHold){ 
										paragraph "<div style=\"background-color:GhostWhite\"> Pushing Top button (\"On\") turns on.</div>"
										if(numButton == "4 button" || numButton == "5 button") paragraph "<div style=\"background-color:GhostWhite\"> Pushing \"Brighten\" button brightens.</div>"
										if(numButton == "5 button") paragraph "<div style=\"background-color:GhostWhite\"> Pushing Center button does <b>nothing</b>.</div>"
										if(numButton == "4 button" || numButton == "5 button") paragraph "<div style=\"background-color:GhostWhite\"> Pushing \"Dim\" dims.</div>"
										paragraph "<div style=\"background-color:GhostWhite\"> Pushing Bottom button (\"Off\") turns off.</div>"
									} else {
										paragraph "<div style=\"background-color:GhostWhite\"> Pushing or Holding Top button (\"On\") turns on.</div>"
										if(numButton == "4 button" || numButton == "5 button") paragraph "<div style=\"background-color:GhostWhite\"> Pushing or Holding \"Brighten\" button brightens.</div>"
										if(numButton == "5 button") paragraph "<div style=\"background-color:GhostWhite\"> Pushing or Holding Center button does <b>nothing</b>.</div>"
										if(numButton == "4 button" || numButton == "5 button") paragraph "<div style=\"background-color:GhostWhite\"> Pushing or Holding \"Dim\" dims.</div>"
										paragraph "<div style=\"background-color:GhostWhite\"> Pushing or Holding Bottom button (\"Off\") turns off.</div>"
									}
								}

								if(!advancedSetup || buttonPush1 == "dim" || buttonPush1 == "brighten" || buttonPush2 == "dim" || buttonPush2 == "brighten" || buttonPush3 == "dim" || buttonPush3 == "brighten" || buttonPush4 == "dim" || buttonPush4 == "brighten" || buttonPush5 == "dim" || buttonPush5 == "brighten" || buttonHold1 == "dim" || buttonHold1 == "brighten" || buttonHold2 == "dim" || buttonHold2 == "brighten" || buttonHold3 == "dim" || buttonHold3 == "brighten" || buttonHold4 == "dim" || buttonHold4 == "brighten" || buttonHold5 == "dim" || buttonHold5 == "brighten")
									displayPushMultiplierOption()
							}
						}
					}
				}

			}
		} else if(multiDevice){

//Multi device select

			if(!advancedSetup){
                section() {
                    displayNameOption()
                    if(app.label) displayPicoOption()
                    if(app.label && buttonDevice) displayPicoTypeOption()
                    if(app.label && buttonDevice && numButton) displayAdvancedSetupOption()
                    if(app.label && buttonDevice && numButton) displayMultiDeviceOption()

                    paragraph "<div style=\"background-color:BurlyWood\"><b> Select what to do for each Pico action:</b></div>"

                    if(!replicateHold){
                        input "button_1_push_on", "capability.switch", title: "Top \"On\" button turns on?", multiple: true, submitOnChange:true
						if(numButton == "4 button" || numButton == "5 button"){
							input "button_2_push_brighten", "capability.switchLevel", title: "\"Brighten\" button brightens?", multiple: true, submitOnChange:true
						}
						if(numButton == "5 button"){
							input "button_3_push_toggle", "capability.switch", title: "Center button toggles? (if on, turn off; if off, turn on)", multiple: true, submitOnChange:true
						}
						if(numButton == "4 button" || numButton == "5 button"){
							input "button_4_push_dim", "capability.switchLevel", title: "\"Dim\" button dims?", multiple: true, submitOnChange:true
						}
						input "button_5_push_off", "capability.switch", title: "Bottom (\"Off\") button turns off?", multiple: true, submitOnChange:true

						if(!replicateHold){
							input "replicateHold", "bool", title: "Replicating settings for Long Push. Click to customize Hold actions.", submitOnChange:true, defaultValue: false
						} else {
							input "replicateHold", "bool", title: "Long Push options shown. Click to replicate from Push.", submitOnChange:true, defaultValue: false
						}

					} else if(replicateHold){
						input "button_1_push_on", "capability.switch", title: "Pushing Top \"On\" button turns on?", multiple: true, submitOnChange:true
						if(numButton == "4 button" || numButton == "5 button"){
							input "button_2_push_brighten", "capability.switchLevel", title: "Pushing \"Brighten\" button brightens?", multiple: true, submitOnChange:true
						}
						if(numButton == "5 button"){
							input "button_3_push_toggle", "capability.switch", title: "Pushing Center button toggles? (if on, turn off; if off, turn on)", multiple: true, submitOnChange:true
						}
						if(numButton == "4 button" || numButton == "5 button"){
							input "button_4_push_dim", "capability.switchLevel", title: "Pushing \"Dim\" button dims?", multiple: true, submitOnChange:true
						}
						input "button_5_push_off", "capability.switch", title: "Pushing Bottom (\"Off\") buttont turns off?", multiple: true, submitOnChange:true

						if(!replicateHold){
							input "replicateHold", "bool", title: "Replicating settings for Long Push. Click to customize Hold actions.", submitOnChange:true, defaultValue: false
						} else {
							input "replicateHold", "bool", title: "Holding Long Push options shown. Click to replicate from Push.", submitOnChange:true, defaultValue: false
						}
						
						input "button_1_hold_on", "capability.switch", title: "Holding Top \"On\" button turns on?", multiple: true, submitOnChange:true
						if(numButton == "4 button" || numButton == "5 button"){
							input "button_2_hold_brighten", "capability.switchLevel", title: "Holding \"Brighten\" button brightens?", multiple: true, submitOnChange:true
						}
						if(numButton == "5 button"){
							input "button_3_hold_toggle", "capability.switch", title: "Holding Center button toggles? (if on, turn off; if off, turn on)", multiple: true, submitOnChange:true
						}
						if(numButton == "4 button" || numButton == "5 button"){
							input "button_4_hold_dim", "capability.switchLevel", title: "Holding \"Dim\" button dims?", multiple: true, submitOnChange:true
						}
						input "button_5_hold_off", "capability.switch", title: "Holding Bottom (\"Off\") buttont turns off?", multiple: true, submitOnChange:true
					}

					if(button_2_push_brighten || button_4_push_dim || button_2_hold_brighten || button_4_hold_dim){
                        displayPushMultiplierOption()
					}
				}
            } else if(advancedSetup){
                section() {
                    displayNameOption()
                    displayPicoOption()
                    displayPicoTypeOption()
                    displayAdvancedSetupOption()
                    if(app.label && buttonDevice && numButton) displayMultiDeviceOption()
                    paragraph "<div style=\"background-color:BurlyWood\"><b> Select what to do for each Pico action:</b></div>"
                }

                if(app.label && buttonDevice && numButton){
                    section(hideable: true, hidden: true, "Top button (\"On\") <font color=\"gray\">(Click to expand/collapse)</font>") {
                        if(button_1_push_on) {
                            input "button_1_push_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, submitOnChange:true
                        } else {
                            input "button_1_push_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                        }
                        if(button_1_push_off) {
                            input "button_1_push_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, submitOnChange:true
                            // Can't turn on and off
                            if(button_1_push_on && parent.compareDeviceLists(button_1_push_off,button_1_push_on,app.label)) errorMessage("Can't set same button to turn on and off the same device")
                        } else {
                            input "button_1_push_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                        }
                        if(button_1_push_toggle && !error){
                            input "button_1_push_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, submitOnChange:true
                            // Can't toggle and turn on/off
                            if(button_1_push_on || button_1_push_off){
                                if((button_1_push_on || button_1_push_off) && (parent.compareDeviceLists(button_1_push_on,button_1_push_toggle,app.label) || parent.compareDeviceLists(button_1_push_off,button_1_push_toggle,app.label)))
                                errorMessage("Can't set same button to toggle as well as turn on or off the same device")
                            }
                        } else if(!error) {
                            input "button_1_push_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                        }
                        if(button_1_push_dim && !error){
                            input "button_1_push_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, submitOnChange:true
                            // Can't dim and turn off
                            if(button_1_push_off && compareDeviceList(button_1_push_off,button_1_push_dim)) errorMessage("Can't set same button to turn off and dim the same device")
                        } else if(!error) {
                            input "button_1_push_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                        }
                        if(button_1_push_brighten && !error) {
                            input "button_1_push_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, submitOnChange:true
                            // Can't brighten and turn off
                            if(button_1_push_off && compareDeviceList(button_1_push_off,button_1_push_brighten)) errorMessage("Can't set same button to turn off and brighten the same device")
                        } else if(!error) {
                            input "button_1_push_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                        }
                    }
                    if(numButton == "4 button" || numButton == "5 button"){
                        section(hideable: true, hidden: true, "\"Brighten\" Button <font color=\"gray\">(Click to expand/collapse)</font>") {
                            if(button_2_push_brighten) {
                                input "button_2_push_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, submitOnChange:true
                            } else {
                                input "button_2_push_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_2_push_toggle) {
                                input "button_2_push_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, submitOnChange:true
                            } else {
                                input "button_2_push_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_2_push_on) {
                                input "button_2_push_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, submitOnChange:true
                                if(button_2_push_toggle && parent.compareDeviceLists(button_2_push_toggle,button_2_push_on)) errorMessage("Can't set same button to toggle as well as turn on or off the same device")
                            } else {
                                input "button_2_push_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_2_push_off && !error){
                                input "button_2_push_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, submitOnChange:true
                                if(button_2_push_toggle && parent.compareDeviceLists(button_2_push_toggle,button_2_push_off)) errorMessage("Can't set same button to toggle and off the same device")
                                if(button_2_push_on && parent.compareDeviceLists(button_2_push_toggle,button_2_push_off,app.label)) errorMessage("Can't set same button to turn on and off the same device")
                            } else if(!error) {
                                input "button_2_push_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_2_push_dim && !error) {
                                input "button_2_push_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, submitOnChange:true
                                if(button_2_push_off && compareDeviceList(button_2_push_off,button_2_push_dim)) errorMessage("Can't set same button to turn off and dim the same device")
                                if(button_2_push_brighten && compareDeviceList(button_2_push_brighten,button_2_push_dim)) errorMessage("Can't set same button to brighten and dim the same device")
                            } else if(!error) {
                                input "button_2_push_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                        }
                    }
                    if(numButton == "5 button"){
                        section(hideable: true, hidden: true, "Middle Button <font color=\"gray\">(Click to expand/collapse)</font>") {
                            if(button_3_push_toggle) {
                                input "button_3_push_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, submitOnChange:true
                            } else {
                                input "button_3_push_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_3_push_on) {
                                input "button_3_push_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, submitOnChange:true
                                if(button_3_push_toggle && compareDeviceList(button_3_push_toggle,button_3_push_on)) errorMessage("Can't set same button to toggle and turn on the same device")
                            } else {
                                input "button_3_push_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_3_push_off && !error) {
                                input "button_3_push_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, submitOnChange:true
                                if(button_3_push_off && compareDeviceList(button_3_push_off,button_3_push_on)) errorMessage("Can't set same button to turn on and off the same device")
                            } else if(!error) {
                                input "button_3_push_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_3_push_dim && !error) {
                                input "button_3_push_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, submitOnChange:true
                                if(button_3_push_off && compareDeviceList(button_3_push_off,button_3_push_dim)) errorMessage("Can't set same button to dim and off the same device")
                            } else if(!error) {
                                input "button_3_push_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_3_push_brighten && !error) {
                                input "button_3_push_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, submitOnChange:true
                                if(button_3_push_off && compareDeviceList(button_3_push_off,button_3_push_brighten)) errorMessage("Can't set same button to brighten and off the same device")
                                if(button_3_push_dim && compareDeviceList(button_3_push_dim,button_3_push_brighten)) errorMessage("Can't set same button to dim and brighten the same device")
                            } else if(!error) {
                                input "button_3_push_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                        }
                    }
                    if(numButton == "4 button" || numButton == "5 button"){
                        section(hideable: true, hidden: true, "\"Dim\" Button <font color=\"gray\">(Click to expand/collapse)</font>") {
                            if(button_4_push_dim) {
                                input "button_4_push_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, submitOnChange:truee
                            } else {
                                input "button_4_push_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_4_push_toggle) {
                                input "button_4_push_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, submitOnChange:true
                            } else {
                                input "button_4_push_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_4_push_on && !error) {
                                input "button_4_push_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, submitOnChange:true
                                if(button_3_push_toggle && compareDeviceList(button_4_push_toggle,button_3_push_on)) errorMessage("Can't set same button to toggle and turn on the same device")
                            } else if(!error) {
                                input "button_4_push_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_4_push_off && !error) {
                                input "button_4_push_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, submitOnChange:true
                                if(button_4_push_toggle && compareDeviceList(button_4_push_toggle,button_4_push_off)) errorMessage("Can't set same button to toggle and turn off the same device")
                                if(button_4_push_on && compareDeviceList(button_4_push_toggle,button_4_push_off)) errorMessage("Can't set same button to on and turn off the same device")
                            } else if(!error) {
                                input "button_4_push_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_4_push_brighten && !error) {
                                input "button_4_push_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, submitOnChange:true
                                if(button_4_push_dim && compareDeviceList(button_4_push_dim,button_4_push_brighten)) errorMessage("Can't set same button to dim and brighten the same device")
                                if(button_4_push_off && compareDeviceList(button_4_push_dim,button_4_push_brighten)) errorMessage("Can't set same button to brighten and turn off the same device")
                            } else if(!error) {
                                input "button_4_push_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                        }
                    }

                    if(!error){
                        section(hideable: true, hidden: true, "Bottom Button (\"Off\") <font color=\"gray\">(Click to expand/collapse)</font>") {
                            if(button_5_push_off) {
                                input "button_5_push_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, submitOnChange:true
                            } else {
                                input "button_5_push_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_5_push_toggle) {
                                input "button_5_push_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, submitOnChange:true
                                if(button_5_push_off && compareDeviceList(button_5_push_toggle,button_5_push_off)) errorMessage("Can't set same button to turn off and toggle the same device")
                            } else {
                                input "button_5_push_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_5_push_on && !error) {
                                input "button_5_push_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, submitOnChange:true
                                if(button_5_push_off && compareDeviceList(button_5_push_toggle,button_5_push_off)) errorMessage("Can't set same button to turn on and turn off the same device")
                                if(button_5_push_toggle && compareDeviceList(button_5_push_off,button_5_push_on)) errorMessage("Can't set same button to turn on and turn off the same device")
                            } else if(!error) {
                                input "button_5_push_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_5_push_dim && !error) {
                                input "button_5_push_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, submitOnChange:true
                                if(button_5_push_off && compareDeviceList(button_5_push_dim,button_5_push_off)) errorMessage("Can't set same button to dim and turn off the same device")
                            } else if(!error) {
                                input "button_5_push_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_5_push_brighten && !error) {
                                input "button_5_push_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, submitOnChange:true
                                if(button_5_push_off && compareDeviceList(button_5_push_brighten,button_5_push_off)) errorMessage("Can't set same button to brighten and turn off the same device")
                                if(button_5_push_dim && compareDeviceList(button_5_push_brighten,button_5_push_dim)) errorMessage("Can't set same button to dim and brighten the same device")
                            } else if(!error) {
                                input "button_5_push_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                        }
                    }


                    if(!replicateHold && !error){
                        section(){
                            input "replicateHold", "bool", title: "Replicating settings for Long Push. Click to customize.", submitOnChange:true, defaultValue: false
                        }
                    } else if(!error) {
                        section(){
                            input "replicateHold", "bool", title: "Long Push options shown. Click to replicate from Push.", submitOnChange:true, defaultValue: false
                        }


                        // Advanced Hold

                        section(hideable: true, hidden: true, "Top button (\"On\") <font color=\"gray\">(Click to expand/collapse)</font>") {
                            if(button_1_hold_on) {
                                input "button_1_hold_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, submitOnChange:true
                            } else {
                                input "button_1_hold_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                            if(button_1_hold_off) {
                                input "button_1_hold_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, submitOnChange:true
                                // Can't turn on and off
                                if(button_1_hold_on && parent.compareDeviceLists(button_1_hold_off,button_1_hold_on,app.label)) errorMessage("Can't set same button to turn on and off the same device")
                            } else {
                                input "button_1_hold_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }

                            if(button_1_hold_toggle && !error) {
                                input "button_1_hold_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, submitOnChange:true
                                // Can't toggle and turn on/off
                                if(button_1_hold_on && parent.compareDeviceLists(button_1_hold_on,button_1_hold_toggle,app.label)) errorMessage("Can't set same button to toggle as well as turn on the same device")
                                if(button_1_hold_off && parent.compareDeviceLists(button_1_hold_off,button_1_hold_toggle,app.label)) errorMessage("Can't set same button to toggle as well as turn ogg the same device")
                            } else if(!error) {
                                input "button_1_hold_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }

                            if(button_1_hold_dim && !error) {
                                input "button_1_hold_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, submitOnChange:true
                                // Can't dim and turn off
                                if(button_1_hold_off && compareDeviceList(button_1_hold_off,button_1_hold_dim)) errorMessage("Can't set same button to turn off and dim the same device")
                            } else if(!error) {
                                input "button_1_hold_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }


                            if(button_1_hold_brighten && !error) {
                                input "button_1_hold_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, submitOnChange:true
                                // Can't brighten and turn off
                                if(button_1_hold_off && compareDeviceList(button_1_hold_off,button_1_hold_brighten)) errorMessage("Can't set same button to turn off and brighten the same device")
                            } else if(!error) {
                                input "button_1_hold_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                            }
                        }
                        if((numButton == "4 button" || numButton == "5 button")  && !error){
                            section(hideable: true, hidden: true, "\"Brighten\" Button <font color=\"gray\">(Click to expand/collapse)</font>") {
                                if(button_2_hold_brighten) {
                                    input "button_2_hold_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, submitOnChange:true
                                } else {
                                    input "button_2_hold_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                                if(button_2_hold_toggle) {
                                    input "button_2_hold_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, submitOnChange:true
                                } else {
                                    input "button_2_hold_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                                if(button_2_hold_on) {
                                    input "button_2_hold_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, submitOnChange:true
                                    if(button_2_hold_toggle && parent.compareDeviceLists(button_2_hold_toggle,button_2_hold_on,app.label)) errorMessage("Can't set same button to toggle as well as turn on or off the same device")
                                } else {
                                    input "button_2_hold_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                                if(button_2_hold_off && !error) {
                                    input "button_2_hold_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, submitOnChange:true
                                    if(button_2_hold_toggle && parent.compareDeviceLists(button_2_hold_toggle,button_2_hold_off,app.label)) errorMessage("Can't set same button to toggle and off the same device")
                                    if(button_2_hold_on && parent.compareDeviceLists(button_2_hold_toggle,button_2_hold_off,app.label)) errorMessage("Can't set same button to turn on and off the same device")
                                } else if(!error) {
                                    input "button_2_hold_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                                if(button_2_hold_dim && !error) {
                                    input "button_2_hold_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, submitOnChange:true
                                    if(button_2_hold_off && compareDeviceList(button_2_hold_off,button_2_hold_dim)) errorMessage("Can't set same button to turn off and dim the same device")
                                    if(button_2_hold_brighten && compareDeviceList(button_2_hold_brighten,button_2_hold_dim)) errorMessage("Can't set same button to brighten and dim the same device")
                                } else if(!error) {
                                    input "button_2_hold_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                            }
                        }
                        if(numButton == "5 button" && !error){
                            section(hideable: true, hidden: true, "Middle Button <font color=\"gray\">(Click to expand/collapse)</font>") {
                                if(button_3_hold_toggle) {
                                    input "button_3_hold_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, submitOnChange:true
                                } else {
                                    input "button_3_hold_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                                if(button_3_hold_on) {
                                    input "button_3_hold_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, submitOnChange:true
                                    if(button_3_hold_toggle && compareDeviceList(button_3_hold_toggle,button_3_hold_on)) errorMessage("Can't set same button to toggle and turn on the same device")
                                } else {
                                    input "button_3_hold_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                                if(button_3_hold_off && !error) {
                                    input "button_3_push_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, submitOnChange:true
                                    if(button_3_hold_off && compareDeviceList(button_3_hold_off,button_3_hold_on)) errorMessage("Can't set same button to turn on and off the same device")
                                } else if(!error) {
                                    input "button_3_hold_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                                if(button_3_hold_dim && !error) {
                                    input "button_3_hold_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, submitOnChange:true
                                    if(button_3_hold_off && compareDeviceList(button_3_hold_off,button_3_hold_dim)) errorMessage("Can't set same button to dim and off the same device")
                                } else if(!error) {
                                    input "button_3_hold_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                                if(button_3_hold_brighten && !error) {
                                    input "button_3_hold_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, submitOnChange:true
                                    if(button_3_hold_off && compareDeviceList(button_3_hold_off,button_3_hold_brighten)) errorMessage("Can't set same button to brighten and off the same device")
                                    if(button_3_hold_dim && compareDeviceList(button_3_hold_dim,button_3_hold_brighten)) errorMessage("Can't set same button to dim and brighten the same device")
                                } else if(!error) {
                                    input "button_3_hold_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                            }
                        }
                        if((numButton == "4 button" || numButton == "5 button")  && !error){
                            section(hideable: true, hidden: true, "\"Dim\" Button <font color=\"gray\">(Click to expand/collapse)</font>") {
                                if(button_4_hold_dim) {
                                    input "button_4_hold_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, submitOnChange:truee
                                } else {
                                    input "button_4_hold_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                                if(button_4_hold_toggle) {
                                    input "button_4_hold_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, submitOnChange:true
                                } else {
                                    input "button_4_hold_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                                if(button_4_hold_on) {
                                    input "button_4_hold_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, submitOnChange:true
                                    if(button_3_hold_toggle && compareDeviceList(button_4_hold_toggle,button_3_hold_on)) errorMessage("Can't set same button to toggle and turn on the same device")
                                } else {
                                    input "button_4_hold_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                                if(button_4_hold_off && !error) {
                                    input "button_4_hold_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, submitOnChange:true
                                    if(button_4_hold_toggle && compareDeviceList(button_4_hold_toggle,button_4_hold_off)) errorMessage("Can't set same button to toggle and turn off the same device")
                                    if(button_4_hold_on && compareDeviceList(button_4_hold_toggle,button_4_hold_off)) errorMessage("Can't set same button to on and turn off the same device")
                                } else if(!error) {
                                    input "button_4_hold_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                                if(button_4_hold_brighten && !error) {
                                    input "button_4_hold_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, submitOnChange:true
                                    if(button_4_hold_dim && compareDeviceList(button_4_hold_dim,button_4_hold_brighten)) errorMessage("Can't set same button to dim and brighten the same device")
                                    if(button_4_hold_off && compareDeviceList(button_4_hold_dim,button_4_hold_brighten)) errorMessage("Can't set same button to brighten and turn off the same device")
                                } else if(!error) {
                                    input "button_4_hold_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                            }
                        }

                        if(!error){
                            section(hideable: true, hidden: true, "Bottom Button (\"Off\") <font color=\"gray\">(Click to expand/collapse)</font>") {
                                if(button_5_hold_off) {
                                    input "button_5_hold_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, submitOnChange:true
                                } else {
                                    input "button_5_hold_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                                if(button_5_hold_toggle) {
                                    input "button_5_hold_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, submitOnChange:true
                                    if(button_5_hold_off && compareDeviceList(button_5_hold_toggle,button_5_hold_off)) errorMessage("Can't set same button to turn off and toggle the same device")
                                } else {
                                    input "button_5_hold_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                                if(button_5_hold_on && !error) {
                                    input "button_5_hold_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, submitOnChange:true
                                    if(button_5_hold_off && compareDeviceList(button_5_hold_toggle,button_5_hold_off)) errorMessage("Can't set same button to turn on and turn off the same device")
                                    if(button_5_hold_toggle && compareDeviceList(button_5_hold_off,button_5_hold_on)) errorMessage("Can't set same button to turn on and turn off the same device")
                                } else if(!error) {
                                    input "button_5_hold_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                                if(button_5_hold_dim && !error) {
                                    input "button_5_hold_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, submitOnChange:true
                                    if(button_5_hold_off && compareDeviceList(button_5_hold_dim,button_5_hold_off)) errorMessage("Can't set same button to dim and turn off the same device")
                                } else if(!error) {
                                    input "button_5_hold_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                                if(button_5_hold_brighten && !error) {
                                    input "button_5_hold_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, submitOnChange:true
                                    if(button_5_hold_off && compareDeviceList(button_5_hold_brighten,button_5_hold_off)) errorMessage("Can't set same button to brighten and turn off the same device")
                                    if(button_5_hold_dim && compareDeviceList(button_5_hold_brighten,button_5_hold_dim)) errorMessage("Can't set same button to dim and brighten the same device")
                                } else if(!error) {
                                    input "button_5_hold_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, submitOnChange:true
                                }
                            }
                        }
                    }

                    if(!error && (button_1_push_dim || button_1_push_brighten || button_2_push_dim || button_2_push_brighten || button_3_push_dim || button_3_push_brighten || button_4_push_dim || button_4_push_brighten || button_5_push_dim || button_5_push_brighten || button_1_hold_dim || button_1_hold_brighten || button_2_hold_dim || button_2_hold_brighten || button_3_hold_dim || button_3_hold_brighten || button_4_hold_dim || button_4_hold_brighten || button_5_hold_dim || button_5_hold_brighten)){
                        section(){
                            displayPushHoldMultiplierOption()
                        }
                    }
                }
			}
		}
	}
}

// Display functions

def errorMessage(text){
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
    displayLabel("Set name for this Pico setup")
	label title: "", required: true, submitOnChange:true
    if(!app.label) displayInfo("Name this Pico setup. Each pico setup must have a unique name.")
/* ************************************************** */
/* TO-DO: Test the name is unique; otherwise          */
/* rescheduling won't work, since we use "childLabel" */
/* variable.                                          */
/* ************************************************** */
}

def displayPicoOption(){
    displayLabel("Select Pico device(s) to setup")
    input "buttonDevice", "capability.pushableButton", title: "Pico(s)?", multiple: true, submitOnChange:true
    if(!buttonDevice) displayInfo("Select which Pico(s) to control. You can select multiple Pico devices, but all should have the same number of buttons.")
}

def displayPicoTypeOption(){
    displayLabel("Set type of Pico")
    input "numButton", "enum", title: "<b>Type of Pico</b>", multiple: false, options: ["2 button", "4 button", "5 button"], submitOnChange:true
    if(!numButton) displayInfo("Set how many buttons the Pico has.")
}

def displayAdvancedSetupOption(){
    displayLabel("Custom buttons and/or devices")
    if(advancedSetup){
        input "advancedSetup", "bool", title: "<b>Allowing custom button actions.</b> Click to use normal button actions.", submitOnChange:true
        if(numButton == "2 button") {
            displayInfo("Click to have the on button turn on and the off button to turn off.")
        } else {
            displayInfo("Click to have the buttons mapped to the normal function; on button turns on, up button brightens, etc.")
        }
    } else {
        input "advancedSetup", "bool", title: "<b>Using normal button actions.</b> Click to map buttons to other actions.", submitOnChange:true
        displayInfo("Click to choose what each button does.")
    }
}

def displayMultiDeviceOption(){
    if(multiDevice){
        input "multiDevice", "bool", title: "<b>Allow different lights for different buttons.</b> Click for all buttons controlling same light(s).", submitOnChange:true
        displayInfo("Assign light(s)/switch(es) to each button. Click for the buttons to do the same thing for all devices.")
    } else {
        input "multiDevice", "bool", title: "<b>Same lights for all buttons.</b> Click to assign different device(s) to different buttons.", submitOnChange:true
        displayInfo("The buttons will do the same thing for all devices. Click to assign light(s)/switch(es) to each button.")

        input "controlDevice", "capability.switch", title: "Device(s) to control", multiple: true, submitOnChange:true
    }
}

def displaySelectActionsOption(){
        displayLabel("Select what to do for each Pico action")
        displaySelectActionsButtonOption(1,"Top (\"On\")")
        if(numButton == "4 button" || numButton == "5 button") displaySelectActionsButtonOption(2,"\"Brighten\"")
        if(numButton == "5 button") displaySelectActionsButtonOption(3,"Center")
        if(numButton == "4 button" || numButton == "5 button") displaySelectActionsButtonOption(4,"\"Dim\"")
        displaySelectActionsButtonOption(5,"Bottom (\"Off\")")
    if(!replicateHold){
        input "replicateHold", "bool", title: "Replicating settings for Long Push. Click to customize Hold actions.", submitOnChange:true, defaultValue: false
    } else {
        input "replicateHold", "bool", title: "Long Push options shown. Click to replicate from Push.", submitOnChange:true, defaultValue: false

        displaySelectActionsButtonOption(1,"Top (\"On\")","hold")
        if(numButton == "4 button" || numButton == "5 button") displaySelectActionsButtonOption(2,"\"Brighten\"","hold")
        if(numButton == "5 button") displaySelectActionsButtonOption(3,"Center","hold")
        if(numButton == "4 button" || numButton == "5 button") displaySelectActionsButtonOption(4,"\"Dim\"","hold")
        displaySelectActionsButtonOption(5,"Bottom (\"Off\")","hold")
    }
}

def displaySelectActionsButtonOption(number,text,action = "push"){
    if(replicateHold && action == "push"){
        button = "Push"
        text = "Pushing $text"
    } else if(replicateHold && action == "hold"){
        button = "Hold"
        text = "Holding $text"
    } else if(!replicateHold){
        button = "Push"
        text = "With $text"
    }
    input "button${button}${number}", "enum", title: "$text button?", multiple: false, options: ["brighten":"Brighten","dim":"Dim","on":"Turn on", "off":"Turn off", "resume": "Resume schedule (if none, turn off)", "toggle":"Toggle"], submitOnChange:true
}

def displayPushMultiplierOption(){
    displayLabel("Set dim and brighten speed")
    displayMultiplierMessage()
    if(button_1_hold_dim || button_1_hold_brighten || button_2_hold_dim || button_2_hold_brighten || button_3_hold_dim || button_3_hold_brighten || button_4_hold_dim || button_4_hold_brighten || button_5_hold_dim || button_5_hold_brighten){
        input "multiplier", "decimal", title: "Mulitplier? (Optional. Default 1.2.)", width: 6
    } else {
        input "multiplier", "decimal", title: "Mulitplier? (Optional. Default 1.2.)", width: 12
    }
}

def displayPushHoldMultiplierOption(){
    displayLabel("Set dim and brighten speed")
    displayMultiplierMessage()
    if(button_1_push_dim || button_1_push_brighten || button_2_push_dim || button_2_push_brighten || button_3_push_dim || button_3_push_brighten || button_4_push_dim || button_4_push_brighten || button_5_push_dim || button_5_push_brighten){
        if(button_1_hold_dim || button_1_hold_brighten || button_2_hold_dim || button_2_hold_brighten || button_3_hold_dim || button_3_hold_brighten || button_4_hold_dim || button_4_hold_brighten || button_5_hold_dim || button_5_hold_brighten){
            input "pushMultiplier", "decimal", title: "<b>Push mulitplier.</b> (Optional. Default 1.2.)", width: 6
            input "holdMultiplier", "decimal", title: "<b>Hold mulitplier.</b> (Optional. Default 1.4.)", width: 6
        } else {
            input "pushMultiplier", "decimal", title: "<b>Push mulitplier.</b> (Optional. Default 1.2.)", width: 12
        }
        } else if(button_1_hold_dim || button_1_hold_brighten || button_2_hold_dim || button_2_hold_brighten || button_3_hold_dim || button_3_hold_brighten || button_4_hold_dim || button_4_hold_brighten || button_5_hold_dim || button_5_hold_brighten){
            input "holdMultiplier", "decimal", title: "<b>Hold mulitplier.</b> (Optional. Default 1.4.)", width: 12
        }
    }

def displayMultiplierMessage(){
    displayInfo("Multiplier/divider for dimming and brightening, from 1.01 to 99, where higher is faster. For instance, a value of 2 would double (eg from 25% to 50%, then 100%), whereas a value of 1.5 would increase by half each time (eg from 25% to 38% to 57%).")
}

def createDeviceList(device,list = []){
	device.each{
		list.add(it.id)
	}
	return list
}

def compareDeviceList(device,list){
	device.each{
		if(list.contains(it.id)) return true
	}
}

/* ************************************************** */
/*                                                    */
/* End display functions.                             */
/*                                                    */
/* ************************************************** */

def installed() {
	logTrace(711, "Installed","trace")
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
	logTrace(717,"Updated","trace")
    unsubscribe()
    initialize()
}

def initialize() {
	logTrace(723,"Initialized","trace")

    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

	subscribe(buttonDevice, "pushed", buttonPushed)
	subscribe(buttonDevice, "held", buttonHeld)
	subscribe(buttonDevice, "released", buttonReleased)
}

def buttonPushed(evt){
    atomicState.buttonNumber = evt.value
    atomicState.action = "push"
    def colorSwitch
    def whiteSwitch

    if(pushMultiplier) pushMultiplier = parent.validateMultiplier(pushMultiplier,app.label)

	// Treat 2nd button of 2-button Pico as "off" (eg button 5)
	if(atomicState.buttonNumber == "2" &&  numButton == "2 button") atomicState.buttonNumber = 5

	// Simple setup
	if(!multiDevice && !advancedSetup){
		switch(atomicState.buttonNumber){
			case "1": multiOn("on",controlDevice)
                message = "turning on"
				break
			case "2": parent.dim("brighten",controlDevice,app.label)
                message = "brightening"
				break
			case "3": multiOn("toggle",controlDevice)
                message = "toggling"
				break
			case "4": parent.dim("dim",controlDevice,app.label)
                message = "dimming"
				break
			case "5": multiOn("off",controlDevice)
                message = "turning off"
		}
        logTrace(761,"Button $atomicState.buttonNumber of $buttonDevice pushed for $controlDevice; default setup; $atomicState.action","trace")
	} else if(!multiDevice && advancedSetup){
			switch(settings["buttonPush${atomicState.buttonNumber}"]){
				case "on": multiOn("on",controlDevice)
                    message = "turning on"
					break
				case "brighten": parent.dim("brighten",controlDevice,app.label)
                    message = "brightening"
					break
				case "toggle": multiOn("toggle",controlDevice)
                    message = "toggling"
					break
				case "dim": parent.dim("dim",controlDevice,app.label)
                    message = "dimming"
					break
				case "off": multiOn("off",controlDevice)
                    message = "turning off"
			}
            logTrace(779,"Button $atomicState.buttonNumber of $buttonDevice pushed for $controlDevice; advanced setup; $message","trace")
    // if(multiDevice && (!advancedSetup || advancedSetup))
	} else {
        if(settings["button_${atomicState.buttonNumber}_push_toggle"] != null) {
            logTrace(783,"Button $atomicState.buttonNumber of $buttonDevice pushed for " + settings["button_${atomicState.buttonNumber}_push_toggle"] + "; remapped and advanced setup; toggling","trace")
            if (settings.color == "Separate"){
                toggleSeparate(settings["button_${atomicState.buttonNumber}_push_toggle"])
            } else {
                multiOn("toggle",settings["button_${atomicState.buttonNumber}_push_toggle"])
            }
        }
        if(settings["button_${atomicState.buttonNumber}_push_on"]) {
            logTrace(791,"Button $atomicState.buttonNumber of $buttonDevice pushed for " + settings["button_${atomicState.buttonNumber}_push_on"] +"; remapped and advanced setup; turning on","trace")
            multiOn("on",settings["button_${atomicState.buttonNumber}_push_on"])
        }
        if(settings["button_${atomicState.buttonNumber}_push_off"]){
            logTrace(795,"Button $atomicState.buttonNumber of $buttonDevice pushed for " + settings["button_${atomicState.buttonNumber}_push_off"] + "; remapped and advanced setup; turning off","trace")
            multiOn("off",settings["button_${atomicState.buttonNumber}_push_off"])
        }
        if(settings["button_${atomicState.buttonNumber}_push_dim"]) {
            logTrace(799,"Button $atomicState.buttonNumber of $buttonDevice pushed for " + settings["button_${atomicState.buttonNumber}_push_dim"] + "; remapped and advanced setup; dimming","trace")
            parent.dim("dim",settings["button_${atomicState.buttonNumber}_push_dim"],app.label)
        }
        if(settings["button_${atomicState.buttonNumber}_push_brighten"]) {
            logTrace(803,"Button $atomicState.buttonNumber of $buttonDevice pushed for " + settings["button_${atomicState.buttonNumber}_push_brighten"] + "; remapped and advanced setup; brightening","trace")
            parent.dim("brighten",settings["button_${atomicState.buttonNumber}_push_brighten"],app.label)
        }
    }
}

def buttonHeld(evt){

    atomicState.buttonNumber = evt.value
    atomicState.action = "held"
    def colorSwitch
    def whiteSwitch

	// Treat 2nd button of 2-button Pico as "off" (eg button 5)
	if(atomicState.buttonNumber == "2" &&  numButton == "2 button") atomicState.buttonNumber = 5

    if(holdMultiplier) holdMultiplier = parent.validateMultiplier(holdMultiplier,app.label)

// TO DO - see if these can be moved so doesn't need to process at every button click

// We are missing multiDevice + advancedSetup (minus replicateHold)!!
//But, why does multiDevice lead to multiOn/Off??

    if(!multiDevice && !advancedSetup && !replicateHold){
        switch(atomicState.buttonNumber){
            case "1": multiOn("on",controlDevice)
            action = "turning on"
            break
            case "2": holdBrighten(controlDevice,app.label)
            action = "brightening"
            break
            case "3": multiOn("toggle",controlDevice)
            action = "toggling"
            break
            case "4": holdDim(controlDevice,app.label)
            action = "dimming"
            break
            case "5": multiOn("off",controlDevice)
            action = "turning off"
        }
        logTrace(843,"Button $atomicState.buttonNumber of $buttonDevice held for $controlDevice; simple setup; $atomicState.action")
    } else if(!multiDevice && advancedSetup && !replicateHold){
        switch(settings["buttonPush${atomicState.buttonNumber}"]){
            case "on": multiOn("on",controlDevice)
            action = "turning on"
            break
            case "brighten": holdBrighten(controlDevice,app.label)
            action = "brightening"
            break
            case "toggle": multiOn("toggle",controlDevice)
            action = "toggling"
            break
            case "dim": holdDim(controlDevice,app.label)
            action = "dimming"
            break
            case "off": multiOn("off",controlDevice)
            action = "turning off"
        }
        logTrace(861,"Button $atomicState.buttonNumber of $buttonDevice held for $controlDevice; advanced setup; $atomicState.action")
    } else if(!multiDevice && advancedSetup && replicateHold){
        switch(settings["buttonHold${atomicState.buttonNumber}"]){
            case "on": multiOn("on",controlDevice)
            action = "turning on"
            break
            case "brighten": holdBrighten(controlDevice,app.label)
            action = "brightening"
            break
            case "toggle": multiOn("toggle",controlDevice)
            action = "toggling"
            break
            case "dim": holdDim(controlDevice,app.label)
            action = "dimming"
            break
            case "off": multiOn("off",controlDevice)
            action = "turning off"
        }
        logTrace(879,"Button $atomicState.buttonNumber of $buttonDevice held for $controlDevice; advanced setup; $atomicState.action","trace")
    } else {
		if(settings["button_${buttonNumber}_hold_toggle"] != null) {
			logTrace(882,"Button $atomicState.buttonNumber of $buttonDevice pushed for " + settings["button_${atomicState.buttonNumber}_push_toggle"] + "; remapped and advanced setup; toggling","trace")
			if (settings.color == "Separate"){
				toggleSeparate(settings["button_${atomicState.buttonNumber}_push_toggle"])
			} else {
				multiOn("toggle",settings["button_${atomicState.buttonNumber}_push_toggle"])
			}
		}
		if(settings["button_${atomicState.buttonNumber}_hold_on"]) {
			logTrace(890,"Button $atomicState.buttonNumber of $buttonDevice pushed for " + settings["button_${atomicState.buttonNumber}_hold_on"] + "; remapped and advanced setup; turning on","trace")
			multiOn("on",settings["button_${atomicState.buttonNumber}_hold_on"])
		}
		if(settings["button_${atomicState.buttonNumber}_hold_off"]) {
			logTrace(894,"Button $atomicState.buttonNumber of $buttonDevice pushed for " + settings["button_${atomicState.buttonNumber}_hold_off"] + "; remapped and advanced setup; turning off","trace")
			multiOn("off",settings["button_${atomicState.buttonNumber}_hold_off"])
		}
		if(settings["button_${atomicState.buttonNumber}_hold_dim"]) {
			logTrace(898,"Button $atomicState.buttonNumber of $buttonDevice pushed for " + settings["button_${atomicState.buttonNumber}_hold_dim"] + "; remapped and advanced setup; dimming","trace")
			holdDim(settings["button_${atomicState.buttonNumber}_hold_dim"])
		}
		if(settings["button_${atomicState.buttonNumber}_hold_brighten"]) {
			logTrace(902,"Button $atomicState.buttonNumber of $buttonDevice pushed for " + settings["button_${atomicState.buttonNumber}_hold_brighten"] + "; remapped and advanced setup; brightening","trace")
			holdBrighten(settings["button_${atomicState.buttonNumber}_hold_brighten"])
		}
	}
}

def buttonReleased(evt){
	atomicState.buttonNumber = evt.value
	if (atomicState.buttonNumber == "2" || (atomicState.buttonNumber == "4" && (settings.numButton == "4 button" || settings.numButton == "5 button")) || (atomicState.buttonNumber == "1" && settings.numButton == "2 button")){
		logTrace(911,"Button $atomicState.buttonNumber of $buttonDevice released, unscheduling all","trace")
		unschedule()
	}
}


//What's the difference between multiplier, pushMultiplier and holdMultiplier?!
def getDimSpeed(){
    if(atomicState.action == "push" &&  pushMultiplier){
		return pushMultiplier
    } else if(atomicState.action == "held" &&  holdMultiplier){
        return holdMultiplier
    } else if(atomicState.action == "held" &&  !pushMultiplier){
		return pushMultiplier
	} else {
		return 1.2
	}
}

// counts number of steps for brighten and dim
// action = "dim" or "brighten"
def getSteps(level, action){
	if (action != "dim" && action != "brighten"){
		logTrace(934,"Invalid value for action \"$action\" sent to getSteps function","error")
		return false
	}
    
	def steps = 0

	// If as already level 1 and dimming or 100 and brightening
	if((action == "dim" && level < 2) || (action == "brighten" && level > 99)){
		steps = 0
	}

	//Just step through nextLevel until hit 1 or 100, and tally total times
	if (action == "dim"){
		while (level  > 1) {
			steps = steps + 1
			level = parent.nextLevel(level, action,app.getId())
		}
	} else if(action == "brighten"){
		while (level  < 100) {
			steps = steps + 1
			level = parent.nextLevel(level, action,app.getId())
		}
	}
	logTrace(957,"Function getSteps returning $steps","debug")
	return steps
}

def setSubscribeLevel(data){
	button_1_hold_dim.each{
		if (it.id == data.device) device = it
	}
	if(!device){
		button_1_hold_brighten.each{
			if (it.id == data.device) device = it
		}
	}
	if(!device){
		button_2_hold_dim.each{
			if (it.id == data.device) device = it
		}
	}
	if(!device){
		button_2_hold_brighten.each{
			if (it.id == data.device) device = it
		}
	}
	if(!device){
		button_3_hold_dim.each{
			if (it.id == data.device) device = it
		}
	}
	if(!device){
		button_3_hold_brighten.each{
			if (it.id == data.device) device = it
		}
	}
	if(!device){
		button_4_hold_dim.each{
			if (it.id == data.device) device = it
		}
	}
	if(!device){
		button_4_hold_brighten.each{
			if (it.id == data.device) device = it
		}
	}
	if(!device){
		button_5_hold_dim.each{
			if (it.id == data.device) device = it
		}
	}
	if(!device){
		button_5_hold_brighten.each{
			if (it.id == data.device) device = it
		}
	}
	if(!device) {
		logTrace(1011,"Function setSubscribeLevel returning (no matching device)","trace")
		return
	}
	parent.singleLevels(data.level,null,null,null,device,app.label)
	reschedule(it)
}

def toggleSeparate(device){
	device.each{
		if(it.currentValue("hue") && parent.isOn(it)) {
			colorSwitch = "on"
		} else if(!it.currentValue("hue") && parent.isOn(it)) {
			whiteSwitch = "on"
		}
	}
	// color on, white on, turn off color
	if(colorSwitch == "on" && whiteSwitch == "on"){
		multiOn("off",device)
	// color on, white off; turn white on
	} else if(colorSwitch == "on" && whiteSwitch != "on"){
		multiOn("on",device)
	//color off, white on; turn off white and turn on color
	} else if(colorSwitch != "on" && whiteSwitch == "on"){
		multiOn("off",device,"white")
		multiOn("on",device)
	// both off; turn color on
	} else if(colorSwitch != "on" && whiteSwitch != "on"){
		multiOn("on",device)
	}
}

def holdDim(device){
    def level = getLevel(device)
	
    device.each{
        if(parent.isFan(it,app.label) == true){
            parent.dim(it,app.label)
        } else if(!parent.isOn(it,app.label)){
		parent.singleLevels(1,null,null,null,device,app.label)
            //parent.setToLevel(it,1,app.label)
			parent.reschedule(it,app.label)
        } else {
            if(level < 2){
                logTrace(1054,"Can't dim $it; already 1%.","info")
            } else {
                def steps = getSteps(level, "dim")
                def newLevel

                for(def i = 1; i <= steps; i++) {
                    newLevel = parent.nextLevel(level, "dim",app.getId())
                    runInMillis(i*750,setSubscribeLevel, [overwrite: false, data: [device: it.id, level: newLevel]])
                    level = newLevel
                }
            }
        }
    }
}

def holdBrighten(device){
    def level = getLevel(device)

    device.each{
        if(parent.isFan(it,app.label)){
            parent.dim("brighten",it,app.label)
        } else if(!parent.isOn(it,app.label)){
		    parent.singleLevels(1,null,null,null,device,app.label)
			reschedule(it)
        } else {
            if(level > 99){
                logTrace(1080,"Can't brighten $it; already 100%.","info")
            } else {
                def steps = getSteps(level, "brighten")
                def newLevel

                for(def i = 1; i <= steps; i++) {
                    newLevel = parent.nextLevel(level, "brighten",app.getId())
                    runInMillis(i*750,setSubscribeLevel, [overwrite: false, data: [device: it.id, level: newLevel]])
                    level = newLevel
                }
            }
        }
    }
}

// calculate average level of a group
def getLevel(device){
    def level = 0
    def count = 0
    device.each{
        if(parent.isFan(it,app.label) != true){
            level += it.currentLevel
            count++
        }
    }
    if(level>0) level = Math.round(level/count)
    if (level > 100) level = 100
    return level
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
        logTrace(1149,"Invalid deviceAction \"$deviceAction\" sent to multiOn","error")
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
        logTrace(1167,"Device id's turned on are $atomicState.deviceChange","debug")
        
        // Turn on devices
        parent.setStateMulti("on",device,app.label)
        // Get and set defaults levels for each device
        device.each{
            // If defaults, then there's an active schedule
            // So use it for if overriding/reenabling
            defaults = parent.getScheduleDefaultSingle(it,app.label)
            logMessage = defaults ? "Device is scheduled for $defaults" : "Device has no scheduled default levels"
            logTrace(1177,logMessage,"debug")

            defaults = getOverrideLevels(defaults,appAction)
            logMessage = defaults ? "With " + app.label + " overrides, using $defaults": "With no override levels" 
            logTrace(1181,logMessage,"debug")

            // Set default levels, for level and temp, if no scheduled defaults (don't need to do for "resume")
            defaults = parent.getDefaultSingle(defaults,app.label)
            logTrace(1185,"With generic defaults, using $defaults","debug")
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
        logTrace(1214,"Device id's turned on are $atomicState.deviceChange","debug")
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
                logTrace(1250,"Scheduled defaults are $defaults","debug")

                defaults = getOverrideLevels(defaults,appAction)
                logTrace(1253,"With " + app.label + " overrides, using $defaults","debug")
                
                parent.setLevelSingle(defaults.level,defaults.temp,defaults.hue,defaults.sat,it,app.label)
                // Set default level
                if(!defaults){
                    logTrace(1258,"No schedule to resume for $it; turning off","trace")
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
        if(logLevel > 3) log.debug message
        break
        case "debug":
        if(loglevel == 5) log.trace message
    }
    return true
}
