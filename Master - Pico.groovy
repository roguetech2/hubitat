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
*  Version: 0.3.05
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

/* ************************************************** */
/* TO-DO: Add error messages (and change info icon    */
/* (see humidity).                                    */
/* ************************************************** */
preferences {
	infoIcon = "<img src=\"http://files.softicons.com/download/toolbar-icons/fatcow-hosting-icons-by-fatcow/png/32/information.png\" width=20 height=20>"
	errorIcon = "<img src=\"http://files.softicons.com/download/toolbar-icons/fatcow-hosting-icons-by-fatcow/png/32/error.png\" width=20 height=20>"
    page(name: "setup", install: true, uninstall: true) {
		if(!multiDevice){
			section() {
				paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this Pico setup:</b></div>"
				label title: "", required: true, submitOnChange:true
				if(!app.label){
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				} else if(app.label){
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select Pico device to setup:</b></div>"
					input "buttonDevice", "capability.pushableButton", title: "Pico(s)?", multiple: true, required: true, submitOnChange:true
					if(!buttonDevice){
						paragraph "<div style=\"background-color:BurlyWood\"> </div>"
					} else if(buttonDevice){
						paragraph "<div style=\"background-color:BurlyWood\"><b> Set type of Pico:</b></div>"
						input "numButton", "enum", title: "<b>Type of Pico</b>", multiple: false, required: true, options: ["2 button", "4 button", "5 button"], submitOnChange:true
						if(!numButton){
							paragraph "<div style=\"background-color:BurlyWood\"> </div>"
						} else if(numButton){
							paragraph "<div style=\"background-color:BurlyWood\"><b> Advanced options: </b></div>"
							paragraph "$infoIcon For each action, select which lights or switches to turn on, turn off, toggle, dim/slow, and/or brighten/speed up. Do not have an action both turn on and off the same light/switch (use Toggle). Do not have an action both dim/slow and brighten/speed up the same light/fan."
							if(!advancedSetup){
								input "advancedSetup", "bool", title: "<b>Using normal button actions.</b> Click to map buttons to other actions.", defaultValue: false, submitOnChange:true
							} else {
								input "advancedSetup", "bool", title: "<b>Allowing mapping buttons to custom actions.</b> Clck to use normal button actions.", defaultValue: false, submitOnChange:true
							}

							input "multiDevice", "bool", title: "Mutli-control: <b>Controls one set of light(s)/switch(es).</b> Click for Pico to independantly control different sets of lights/switches (eg a light and a fan).", defaultValue: false, submitOnChange:true
							paragraph "$infoIcon Use this option if you only want to control one light or set of lights. Change this option if, for instance, you want to turn on some lights, and brighten/dim <i>different lights</i>."

/* ************************************************** */
/* TO-DO: Add locks? Any reason someone might want to */
/* use a Pico to lock a door?? (Ditto for             */
/* MagicCubes.)                                       */
/* ************************************************** */
							input "controlDevice", "capability.switch", title: "Device(s) to control", multiple: true, required: true, submitOnChange:true

							if(advancedSetup){
								paragraph "$infoIcon <b>Pro-tip</b>: Profiles for Multi-control enabled and disabled are stored separatly, allowing toggling between two different setups. To do this, set the options both with Multi-control disabled and enabled."
							}
							if(!controlDevice){
								paragraph "<div style=\"background-color:BurlyWood\"> </div>"
							} else if(controlDevice){
								if(advancedSetup){
									if(!replicateHold){
										paragraph "<div style=\"background-color:BurlyWood\"><b> Select what to do for each Pico action:</b></div>"
										input "buttonPush1", "enum", title: "With Top (\"On\") button?", required: false, multiple: false, options: ["brighten":"Brighten","dim":"Dim","on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
										if(numButton == "4 button" || numButton == "5 button") 
											input "buttonPush2", "enum", title: "With \"Brighten\" button?", required: false, multiple: false, options: ["brighten":"Brighten","dim":"Dim", "on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
										if(numButton == "5 button") input "buttonPush3", "enum", title: "With Center button?", required: false, multiple: false, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true
										if(numButton == "4 button" || numButton == "5 button") input "buttonPush4", "enum", title: "With \"Dim\" button?", required: false, multiple: false, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true
										input "buttonPush5", "enum", title: "With Bottom (\"Off\") button?", required: false, multiple: false, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true
										input "replicateHold", "bool", title: "Replicating settings for Long Push. Click to customize Hold actions.", submitOnChange:true, defaultValue: false
									} else {
										paragraph "<div style=\"background-color:BurlyWood\"><b> Select what to do for each Pico action:</b></div>"
										input "buttonPush1", "enum", title: "Pushing Top (\"On\") button?", required: false, multiple: false, options: ["brighten":"Brighten","dim":"Dim","on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
										if(numButton == "4 button" || numButton == "5 button") 
											input "buttonPush2", "enum", title: "Pushing \"Brighten\" button?", required: false, multiple: false, options: ["brighten":"Brighten","dim":"Dim", "on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
										if(numButton == "5 button") input "buttonPush3", "enum", title: "Pushing Center button?", required: false, multiple: false, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true
										if(numButton == "4 button" || numButton == "5 button") input "buttonPush4", "enum", title: "Pushing \"Dim\" button?", required: false, multiple: false, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true
										input "buttonPush5", "enum", title: "Pushing Bottom (\"Off\") button?", required: false, multiple: false, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true

										input "replicateHold", "bool", title: "Long Push options shown. Click to replicate from Push.", submitOnChange:true, defaultValue: false

										input "buttonHold1", "enum", title: "Holding Top (\"On\") button?", required: false, multiple: false, options: ["brighten":"Brighten","dim":"Dim","on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
										if(numButton == "4 button" || numButton == "5 button") 
											input "buttonHold2", "enum", title: "Holding \"Brighten\" button?", required: false, multiple: false, options: ["brighten":"Brighten","dim":"Dim", "on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
										if(numButton == "5 button") input "buttonHold3", "enum", title: "Holding Center button?", required: false, multiple: false, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true
										if(numButton == "4 button" || numButton == "5 button") input "buttonHold4", "enum", title: "Holding \"Dim\" button?", required: false, multiple: false, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true
										input "buttonHold5", "enum", title: "Holding Bottom (\"Off\") button?", required: false, multiple: false, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true

									}
								} else {
									paragraph "To set different functions to each button, change to Advanced actions by clicking \"Simple actions\"."
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

								if(!advancedSetup || buttonPush1 == "dim" || buttonPush1 == "brighten" || buttonPush2 == "dim" || buttonPush2 == "brighten" || buttonPush3 == "dim" || buttonPush3 == "brighten" || buttonPush4 == "dim" || buttonPush4 == "brighten" || buttonPush5 == "dim" || buttonPush5 == "brighten" || buttonHold1 == "dim" || buttonHold1 == "brighten" || buttonHold2 == "dim" || buttonHold2 == "brighten" || buttonHold3 == "dim" || buttonHold3 == "brighten" || buttonHold4 == "dim" || buttonHold4 == "brighten" || buttonHold5 == "dim" || buttonHold5 == "brighten"){
									paragraph "<div style=\"background-color:BurlyWood\"><b> Set dim and brighten speed:</b></div>"
									paragraph "$infoIcon Multiplier/divider for dimming and brightening, from 1.01 to 99, where higher is faster. For instance, a value of 2 would double (eg from 25% to 50%, then 100%), whereas a value of 1.5 would increase by half each time (eg from 25% to 38% to 57%)."
									input "multiplier", "decimal", required: false, title: "Mulitplier? (Optional. Default 1.2.)", width: 6
								}
								if(!advancedSetup || buttonPush1 == "on" || buttonPush1 == "off" || buttonPush1 == "dim" || buttonPush1 == "brighten" || buttonPush1 == "toggle" || buttonPush2 == "on" || buttonPush2 == "off" || buttonPush3 == "dim" || buttonPush3 == "brighten" || buttonPush3 == "toggle" || buttonPush4 == "on" || buttonPush4 == "off" || buttonPush4 == "dim" || buttonPush4 == "brighten" || buttonPush4 == "toggle" || buttonPush5 == "on" || buttonPush5 == "off" || buttonPush5 == "dim" || buttonPush5 == "brighten" || buttonPush5 == "toggle" || buttonHold1 == "on" || buttonHold1 == "off" || buttonHold1 == "dim" || buttonHold1 == "brighten" || buttonHold1 == "toggle" || buttonHold2 == "on" || buttonHold2 == "off" || buttonHold3 == "dim" || buttonHold3 == "brighten" || buttonHold3 == "toggle" || buttonHold4 == "on" || buttonHold4 == "off" || buttonHold4 == "dim" || buttonHold4 == "brighten" || buttonHold4 == "toggle" || buttonHold5 == "on" || buttonHold5 == "off" || buttonHold5 == "dim" || buttonHold5 == "brighten" || buttonHold5 == "toggle"){ 
									paragraph "<div style=\"background-color:LightCyan\"><b> Click \"Done\" to continue.</b></div>"
								} else {
									paragraph "<div style=\"background-color:MistyRose\"><b> No actions selected. Do NOT save.</b></div>"
								}
							}
						}
					}
				}

			}
		} else if(multiDevice){

//Multi device select

			if(!app.label){
				section() {
					paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this Pico setup:</b></div>"
					label title: "", required: true, submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				}
			} else if(app.label && !buttonDevice){
				section() {
					paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this Pico setup:</b></div>"
					label title: "", required: true, submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select Pico device to setup:</b></div>"
					input "buttonDevice", "capability.pushableButton", title: "Pico(s)?", multiple: true, required: true, submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				}
			} else if(app.label && buttonDevice && !numButton){
				section() {
					paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this Pico setup:</b></div>"
					label title: "", required: true, submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select Pico device to setup:</b></div>"
					input "buttonDevice", "capability.pushableButton", title: "Pico(s)?", multiple: true, required: true, submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"><b> Set type of Pico:</b></div>"
					input "numButton", "enum", title: "Pico buttons?", multiple: false, required: true, options: ["2 button", "4 button", "5 button"], submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				}
			} else if(app.label && buttonDevice && numButton && !advancedSetup){
				section() {
					paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this Pico setup:</b></div>"
					label title: "", required: true, submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select Pico device to setup:</b></div>"
					input "buttonDevice", "capability.pushableButton", title: "Pico(s)?", multiple: true, required: true, submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"><b> Set type of Pico:</b></div>"
					input "numButton", "enum", title: "<b>Type of Pico</b>", multiple: false, required: true, options: ["2 button", "4 button", "5 button"], submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"><b> Advanced options: </b></div>"

					paragraph "$infoIcon For each action, select which lights or switches to turn on, turn off, toggle, dim/slow, and/or brighten/speed up. Do not have an action both turn on and off the same light/switch (use Toggle). Do not have an action both dim/slow and brighten/speed up the same light/fan."
					input "advancedSetup", "bool", title: "<b>Using normal button actions.</b> Click to map buttons to other actions.", defaultValue: false, submitOnChange:true
					input "multiDevice", "bool", title: "Multi-control: <b>Independantly control different sets of lights/switches.</b> Click for Pico to control only one set of lights/switches.", defaultValue: true, submitOnChange:true
					paragraph "$infoIcon Use this option if you only want to control one light or set of lights. Change this option if, for instance, you want to turn on some lights, and brighten/dim <i>different lights</i>."

					paragraph "<div style=\"background-color:BurlyWood\"><b> Select what to do for each Pico action:</b></div>"

					paragraph "$infoIcon To set different functions to each button, change to Advanced actions by clicking \"Simple actions\"."
					if(!replicateHold){
						input "button_1_push_on", "capability.switch", title: "Top \"On\" button turns on?", multiple: true, required: false, submitOnChange:true
						if(numButton == "4 button" || numButton == "5 button"){
							input "button_2_push_brighten", "capability.switchLevel", title: "\"Brighten\" button brightens?", multiple: true, required: false, submitOnChange:true
						}
						if(numButton == "5 button"){
							input "button_3_push_toggle", "capability.switch", title: "Center button toggles? (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
						}
						if(numButton == "4 button" || numButton == "5 button"){
							input "button_4_push_dim", "capability.switchLevel", title: "\"Dim\" button dims?", multiple: true, required: false, submitOnChange:true
						}
						if(!error) input "button_5_push_off", "capability.switch", title: "Bottom (\"Off\") buttont turns off?", multiple: true, required: false, submitOnChange:true

						if(!replicateHold){
							input "replicateHold", "bool", title: "Replicating settings for Long Push. Click to customize Hold actions.", submitOnChange:true, defaultValue: false
						} else {
							input "replicateHold", "bool", title: "Long Push options shown. Click to replicate from Push.", submitOnChange:true, defaultValue: false
						}

					} else if(replicateHold){
						input "button_1_push_on", "capability.switch", title: "Pushing Top \"On\" button turns on?", multiple: true, required: false, submitOnChange:true
						if(numButton == "4 button" || numButton == "5 button"){
							input "button_2_push_brighten", "capability.switchLevel", title: "Pushing \"Brighten\" button brightens?", multiple: true, required: false, submitOnChange:true
						}
						if(numButton == "5 button"){
							input "button_3_push_toggle", "capability.switch", title: "Pushing Center button toggles? (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
						}
						if(numButton == "4 button" || numButton == "5 button"){
							input "button_4_push_dim", "capability.switchLevel", title: "Pushing \"Dim\" button dims?", multiple: true, required: false, submitOnChange:true
						}
						input "button_5_push_off", "capability.switch", title: "Pushing Bottom (\"Off\") buttont turns off?", multiple: true, required: false, submitOnChange:true

						if(!replicateHold){
							input "replicateHold", "bool", title: "Replicating settings for Long Push. Click to customize Hold actions.", submitOnChange:true, defaultValue: false
						} else {
							input "replicateHold", "bool", title: "Holding Long Push options shown. Click to replicate from Push.", submitOnChange:true, defaultValue: false
						}
						
						input "button_1_hold_on", "capability.switch", title: "Holding Top \"On\" button turns on?", multiple: true, required: false, submitOnChange:true
						if(numButton == "4 button" || numButton == "5 button"){
							input "button_2_hold_brighten", "capability.switchLevel", title: "Holding \"Brighten\" button brightens?", multiple: true, required: false, submitOnChange:true
						}
						if(numButton == "5 button"){
							input "button_3_hold_toggle", "capability.switch", title: "Holding Center button toggles? (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
						}
						if(numButton == "4 button" || numButton == "5 button"){
							input "button_4_hold_dim", "capability.switchLevel", title: "Holding \"Dim\" button dims?", multiple: true, required: false, submitOnChange:true
						}
						input "button_5_hold_off", "capability.switch", title: "Holding Bottom (\"Off\") buttont turns off?", multiple: true, required: false, submitOnChange:true
					}

					
					if(button_2_push_brighten || button_4_push_dim || button_2_hold_brighten || button_4_hold_dim){
						paragraph "<div style=\"background-color:BurlyWood\"><b> Set dim and brighten speed:</b></div>"
						paragraph "$infoIcon Multiplier/divider for dimming and brightening, from 1.01 to 99, where higher is faster. For instance, a value of 2 would double (eg from 25% to 50%, then 100%), whereas a value of 1.5 would increase by half each time (eg from 25% to 38% to 57%)."
						input "multiplier", "decimal", required: false, title: "Mulitplier? (Optional. Default 1.2.)", width: 6
					}
					if(button_1_push_on || button_2_push_brighten || button_4_push_toggle || button_4_push_dim || button_5_push_off || button_1_hold_on || button_2_hold_brighten || button_4_hold_toggle || button_4_hold_dim || button_5_hold_off){
						paragraph "<div style=\"background-color:LightCyan\"><b> Click \"Done\" to continue.</b></div>"
					} else {
						paragraph "<div style=\"background-color:MistyRose\"><b> No actions selected. Do NOT save.</b></div>"
					}
				}
			} else if(app.label && buttonDevice && numButton && advancedSetup){
				section() {
					paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this Pico setup:</b></div>"
					label title: "", required: true, submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select Pico device to setup:</b></div>"
					input "buttonDevice", "capability.pushableButton", title: "Pico(s)?", multiple: true, required: true, submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"><b> Set type of Pico:</b></div>"
					input "numButton", "enum", title: "<b>Type of Pico</b>", multiple: false, required: true, options: ["2 button", "4 button", "5 button"], submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"><b> Advanced options: </b></div>"

					paragraph "$infoIcon For each action, select which lights or switches to turn on, turn off, toggle, dim/slow, and/or brighten/speed up. Do not have an action both turn on and off the same light/switch (use Toggle). Do not have an action both dim/slow and brighten/speed up the same light/fan."
					input "advancedSetup", "bool", title: "<b>Allowing mapping buttons to custom actions.</b> Clck to use normal button actions.", defaultValue: false, submitOnChange:true
					input "multiDevice", "bool", title: "Mutli-control: <b>Independantly control different sets of lights/switches.</b> Click for Pico to control only one set of lights/switches.", defaultValue: true, submitOnChange:true
					paragraph "$infoIcon Use this option if you only want to control one light or set of lights. Change this option if, for instance, you want to turn on some lights, and brighten/dim <i>different lights</i>."

					paragraph "<div style=\"background-color:BurlyWood\"><b> Select what to do for each Pico action:</b></div>"
				}
					
				section(hideable: true, hidden: true, "Top button (\"On\") <font color=\"gray\">(Click to expand/collapse)</font>") {
					if(button_1_push_on) {
						input "button_1_push_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
					} else {
						input "button_1_push_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
					}
					if(button_1_push_off) {
						input "button_1_push_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
						// Can't turn on and off
						if(button_1_push_on){
							error = parent.compareDeviceLists(button_1_push_off,button_1_push_on)
							if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn on and off the same device.</div>"
						}
					} else {
						input "button_1_push_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
					}

					if(button_1_push_toggle && !error) {
						input "button_1_push_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
						// Can't toggle and turn on/off
						if(button_1_push_on || button_1_push_off){
							error = parent.compareDeviceLists(button_1_push_on,button_1_push_toggle)
							error = parent.compareDeviceLists(button_1_push_off,button_1_push_toggle)
							if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to toggle as well as turn on or off the same device.</div>"
						}
					} else if(!error) {
						input "button_1_push_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
					}
					
					if(button_1_push_dim && !error) {
						input "button_1_push_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
						// Can't dim and turn off
						if(button_1_push_off){
							error = compareDeviceList(button_1_push_off,button_1_push_dim)
							if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn off and dim the same device.</div>"
						}
					} else if(!error) {
						input "button_1_push_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
					}
					

					if(button_1_push_brighten && !error) {
						input "button_1_push_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
						// Can't brighten and turn off
						if(button_1_push_off){
							error = compareDeviceList(button_1_push_off,button_1_push_brighten)
							if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn off and brighten the same device.</div>"
						}
					} else if(!error) {
						input "button_1_push_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
					}
				}
				if((numButton == "4 button" || numButton == "5 button")  && !error){
					section(hideable: true, hidden: true, "\"Brighten\" Button <font color=\"gray\">(Click to expand/collapse)</font>") {
						if(button_2_push_brighten) {
							input "button_2_push_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_2_push_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_2_push_toggle) {
							input "button_2_push_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_2_push_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_2_push_on) {
							input "button_2_push_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
							if(button_2_push_toggle){
								error = parent.compareDeviceLists(button_2_push_toggle,button_2_push_on)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to toggle as well as turn on or off the same device.</div>"
							}
						} else {
							input "button_2_push_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_2_push_off && !error) {
							input "button_2_push_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
							if(button_2_push_toggle){
								error = parent.compareDeviceLists(button_2_push_toggle,button_2_push_off)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to toggle and off the same device.</div>"
							}
							if(button_2_push_on){
								error = parent.compareDeviceLists(button_2_push_toggle,button_2_push_off)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn on and off the same device.</div>"
							}
						} else if(!error) {
							input "button_2_push_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_2_push_dim && !error) {
							input "button_2_push_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
							if(button_2_push_off){
								error = compareDeviceList(button_2_push_off,button_2_push_dim)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn off and dim the same device.</div>"
							}
							if(button_2_push_brighten){
								error = compareDeviceList(button_2_push_brighten,button_2_push_dim)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to brighten and dim the same device.</div>"
							}
						} else if(!error) {
							input "button_2_push_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
					}
				}
				if(numButton == "5 button" && !error){
					section(hideable: true, hidden: true, "Middle Button <font color=\"gray\">(Click to expand/collapse)</font>") {
						if(button_3_push_toggle) {
							input "button_3_push_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_3_push_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_3_push_on) {
							input "button_3_push_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
							if(button_3_push_toggle){
								error = compareDeviceList(button_3_push_toggle,button_3_push_on)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to toggle and turn on the same device.</div>"
							}
						} else {
							input "button_3_push_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_3_push_off && !error) {
							input "button_3_push_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
							if(button_3_push_off){
								error = compareDeviceList(button_3_push_off,button_3_push_on)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn on and off the same device.</div>"
							}
						} else if(!error) {
							input "button_3_push_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_3_push_dim && !error) {
							input "button_3_push_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
							if(button_3_push_off){
								error = compareDeviceList(button_3_push_off,button_3_push_dim)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to dim and off the same device.</div>"
							}
						} else if(!error) {
							input "button_3_push_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_3_push_brighten && !error) {
							input "button_3_push_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
							if(button_3_push_off){
								error = compareDeviceList(button_3_push_off,button_3_push_brighten)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to brighten and off the same device.</div>"
							}
							if(button_3_push_dim){
								error = compareDeviceList(button_3_push_dim,button_3_push_brighten)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to dim and brighten the same device.</div>"
							}
						} else if(!error) {
							input "button_3_push_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
					}
				}
				if((numButton == "4 button" || numButton == "5 button")  && !error){
					section(hideable: true, hidden: true, "\"Dim\" Button <font color=\"gray\">(Click to expand/collapse)</font>") {
						if(button_4_push_dim) {
							input "button_4_push_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:truee
						} else {
							input "button_4_push_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_4_push_toggle) {
							input "button_4_push_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_4_push_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_4_push_on) {
							input "button_4_push_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
							if(button_3_push_toggle){
								error = compareDeviceList(button_4_push_toggle,button_3_push_on)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to toggle and turn on the same device.</div>"
							}
						} else {
							input "button_4_push_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_4_push_off && !error) {
							input "button_4_push_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
							if(button_4_push_toggle){
								error = compareDeviceList(button_4_push_toggle,button_4_push_off)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to toggle and turn off the same device.</div>"
							}
							if(button_4_push_on){
								error = compareDeviceList(button_4_push_toggle,button_4_push_off)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to on and turn off the same device.</div>"
							}
						} else if(!error) {
							input "button_4_push_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_4_push_brighten && !error) {
							input "button_4_push_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
							if(button_4_push_dim){
								error = compareDeviceList(button_4_push_dim,button_4_push_brighten)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to dim and brighten the same device.</div>"
							}
							if(button_4_push_off){
								error = compareDeviceList(button_4_push_dim,button_4_push_brighten)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to brighten and turn off the same device.</div>"
							}
						} else if(!error) {
							input "button_4_push_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
					}
				}

				if(!error){
					section(hideable: true, hidden: true, "Bottom Button (\"Off\") <font color=\"gray\">(Click to expand/collapse)</font>") {
						if(button_5_push_off) {
							input "button_5_push_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false
						} else {
							input "button_5_push_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false
						}
						if(button_5_push_toggle) {
							input "button_5_push_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false
							if(button_5_push_off){
								error = compareDeviceList(button_5_push_toggle,button_5_push_off)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn off and toggle the same device.</div>"
							}
						} else {
							input "button_5_push_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false
						}
						if(button_5_push_on && !error) {
							input "button_5_push_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false
							if(button_5_push_off){
								error = compareDeviceList(button_5_push_toggle,button_5_push_off)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn on and turn off the same device.</div>"
							}
							if(button_5_push_toggle){
								error = compareDeviceList(button_5_push_off,button_5_push_on)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn on and turn off the same device.</div>"
							}
						} else if(!error) {
							input "button_5_push_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false
						}
						if(button_5_push_dim && !error) {
							input "button_5_push_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
							if(button_5_push_off){
								error = compareDeviceList(button_5_push_dim,button_5_push_off)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to dim and turn off the same device.</div>"
							}
						} else if(!error) {
							input "button_5_push_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_5_push_brighten && !error) {
							input "button_5_push_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
							if(button_5_push_off){
								error = compareDeviceList(button_5_push_brighten,button_5_push_off)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to brighten and turn off the same device.</div>"
							}
							if(button_5_push_dim){
								error = compareDeviceList(button_5_push_brighten,button_5_push_dim)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to dim and brighten the same device.</div>"
							}
						} else if(!error) {
							input "button_5_push_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
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
							input "button_1_hold_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_1_hold_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_1_hold_off) {
							input "button_1_hold_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
							// Can't turn on and off
							if(button_1_hold_on){
								error = parent.compareDeviceLists(button_1_hold_off,button_1_hold_on)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn on and off the same device.</div>"
							}
						} else {
							input "button_1_hold_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}

						if(button_1_hold_toggle && !error) {
							input "button_1_hold_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
							// Can't toggle and turn on/off
							if(button_1_hold_on){
								error = parent.compareDeviceLists(button_1_hold_on,button_1_hold_toggle)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to toggle as well as turn on the same device.</div>"
							}
							if(button_1_hold_off){
								error = parent.compareDeviceLists(button_1_hold_off,button_1_hold_toggle)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to toggle as well as turn ogg the same device.</div>"
							}
						} else if(!error) {
							input "button_1_hold_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}

						if(button_1_hold_dim && !error) {
							input "button_1_hold_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
							// Can't dim and turn off
							if(button_1_hold_off){
								error = compareDeviceList(button_1_hold_off,button_1_hold_dim)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn off and dim the same device.</div>"
							}
						} else if(!error) {
							input "button_1_hold_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}


						if(button_1_hold_brighten && !error) {
							input "button_1_hold_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
							// Can't brighten and turn off
							if(button_1_hold_off){
								error = compareDeviceList(button_1_hold_off,button_1_hold_brighten)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn off and brighten the same device.</div>"
							}
						} else if(!error) {
							input "button_1_hold_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
					}
					if((numButton == "4 button" || numButton == "5 button")  && !error){
						section(hideable: true, hidden: true, "\"Brighten\" Button <font color=\"gray\">(Click to expand/collapse)</font>") {
							if(button_2_hold_brighten) {
								input "button_2_hold_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
							} else {
								input "button_2_hold_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_2_hold_toggle) {
								input "button_2_hold_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
							} else {
								input "button_2_hold_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_2_hold_on) {
								input "button_2_hold_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
								if(button_2_hold_toggle){
									error = parent.compareDeviceLists(button_2_hold_toggle,button_2_hold_on)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to toggle as well as turn on or off the same device.</div>"
								}
							} else {
								input "button_2_hold_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_2_hold_off && !error) {
								input "button_2_hold_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
								if(button_2_hold_toggle){
									error = parent.compareDeviceLists(button_2_hold_toggle,button_2_hold_off)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to toggle and off the same device.</div>"
								}
								if(button_2_hold_on){
									error = parent.compareDeviceLists(button_2_hold_toggle,button_2_hold_off)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn on and off the same device.</div>"
								}
							} else if(!error) {
								input "button_2_hold_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_2_hold_dim && !error) {
								input "button_2_hold_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
								if(button_2_hold_off){
									error = compareDeviceList(button_2_hold_off,button_2_hold_dim)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn off and dim the same device.</div>"
								}
								if(button_2_hold_brighten){
									error = compareDeviceList(button_2_hold_brighten,button_2_hold_dim)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to brighten and dim the same device.</div>"
								}
							} else if(!error) {
								input "button_2_hold_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
						}
					}
					if(numButton == "5 button" && !error){
						section(hideable: true, hidden: true, "Middle Button <font color=\"gray\">(Click to expand/collapse)</font>") {
							if(button_3_hold_toggle) {
								input "button_3_hold_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
							} else {
								input "button_3_hold_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_3_hold_on) {
								input "button_3_hold_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
								if(button_3_hold_toggle){
									error = compareDeviceList(button_3_hold_toggle,button_3_hold_on)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to toggle and turn on the same device.</div>"
								}
							} else {
								input "button_3_hold_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_3_hold_off && !error) {
								input "button_3_push_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
								if(button_3_hold_off){
									error = compareDeviceList(button_3_hold_off,button_3_hold_on)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn on and off the same device.</div>"
								}
							} else if(!error) {
								input "button_3_hold_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_3_hold_dim && !error) {
								input "button_3_hold_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
								if(button_3_hold_off){
									error = compareDeviceList(button_3_hold_off,button_3_hold_dim)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to dim and off the same device.</div>"
								}
							} else if(!error) {
								input "button_3_hold_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_3_hold_brighten && !error) {
								input "button_3_hold_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
								if(button_3_hold_off){
									error = compareDeviceList(button_3_hold_off,button_3_hold_brighten)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to brighten and off the same device.</div>"
								}
								if(button_3_hold_dim){
									error = compareDeviceList(button_3_hold_dim,button_3_hold_brighten)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to dim and brighten the same device.</div>"
								}
							} else if(!error) {
								input "button_3_hold_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
						}
					}
					if((numButton == "4 button" || numButton == "5 button")  && !error){
						section(hideable: true, hidden: true, "\"Dim\" Button <font color=\"gray\">(Click to expand/collapse)</font>") {
							if(button_4_hold_dim) {
								input "button_4_hold_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:truee
							} else {
								input "button_4_hold_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_4_hold_toggle) {
								input "button_4_hold_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
							} else {
								input "button_4_hold_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_4_hold_on) {
								input "button_4_hold_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
								if(button_3_hold_toggle){
									error = compareDeviceList(button_4_hold_toggle,button_3_hold_on)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to toggle and turn on the same device.</div>"
								}
							} else {
								input "button_4_hold_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_4_hold_off && !error) {
								input "button_4_hold_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
								if(button_4_hold_toggle){
									error = compareDeviceList(button_4_hold_toggle,button_4_hold_off)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to toggle and turn off the same device.</div>"
								}
								if(button_4_hold_on){
									error = compareDeviceList(button_4_hold_toggle,button_4_hold_off)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to on and turn off the same device.</div>"
								}
							} else if(!error) {
								input "button_4_hold_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_4_hold_brighten && !error) {
								input "button_4_hold_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
								if(button_4_hold_dim){
									error = compareDeviceList(button_4_hold_dim,button_4_hold_brighten)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to dim and brighten the same device.</div>"
								}
								if(button_4_hold_off){
									error = compareDeviceList(button_4_hold_dim,button_4_hold_brighten)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to brighten and turn off the same device.</div>"
								}
							} else if(!error) {
								input "button_4_hold_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
						}
					}

					if(!error){
						section(hideable: true, hidden: true, "Bottom Button (\"Off\") <font color=\"gray\">(Click to expand/collapse)</font>") {
							if(button_5_hold_off) {
								input "button_5_hold_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false
							} else {
								input "button_5_hold_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false
							}
							if(button_5_hold_toggle) {
								input "button_5_hold_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false
								if(button_5_hold_off){
									error = compareDeviceList(button_5_hold_toggle,button_5_hold_off)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn off and toggle the same device.</div>"
								}
							} else {
								input "button_5_hold_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false
							}
							if(button_5_hold_on && !error) {
								input "button_5_hold_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false
								if(button_5_hold_off){
									error = compareDeviceList(button_5_hold_toggle,button_5_hold_off)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn on and turn off the same device.</div>"
								}
								if(button_5_hold_toggle){
									error = compareDeviceList(button_5_hold_off,button_5_hold_on)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn on and turn off the same device.</div>"
								}
							} else if(!error) {
								input "button_5_hold_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false
							}
							if(button_5_hold_dim && !error) {
								input "button_5_hold_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
								if(button_5_hold_off){
									error = compareDeviceList(button_5_hold_dim,button_5_hold_off)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to dim and turn off the same device.</div>"
								}
							} else if(!error) {
								input "button_5_hold_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_5_hold_brighten && !error) {
								input "button_5_hold_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
								if(button_5_hold_off){
									error = compareDeviceList(button_5_hold_brighten,button_5_hold_off)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to brighten and turn off the same device.</div>"
								}
								if(button_5_hold_dim){
									error = compareDeviceList(button_5_hold_brighten,button_5_hold_dim)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to dim and brighten the same device.</div>"
								}
							} else if(!error) {
								input "button_5_hold_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
						}
					}
				}


				if(!error && (button_1_push_dim || button_1_push_brighten || button_2_push_dim || button_2_push_brighten || button_3_push_dim || button_3_push_brighten || button_4_push_dim || button_4_push_brighten || button_5_push_dim || button_5_push_brighten || button_1_hold_dim || button_1_hold_brighten || button_2_hold_dim || button_2_hold_brighten || button_3_hold_dim || button_3_hold_brighten || button_4_hold_dim || button_4_hold_brighten || button_5_hold_dim || button_5_hold_brighten)){
					section(){
						paragraph "<div style=\"background-color:BurlyWood\"><b> Set dim and brighten speed:</b></div>"
						paragraph "$infoIcon Multiplier/divider for dimming and brightening, from 1.01 to 99, where higher is faster. For instance, a value of 2 would double (eg from 25% to 50%, then 100%), whereas a value of 1.5 would increase by half each time (eg from 25% to 38% to 57%)."
						if(button_1_push_dim || button_1_push_brighten || button_2_push_dim || button_2_push_brighten || button_3_push_dim || button_3_push_brighten || button_4_push_dim || button_4_push_brighten || button_5_push_dim || button_5_push_brighten){
							input "pushMultiplier", "decimal", required: false, title: "<b>Push mulitplier.</b> (Optional. Default 1.2.)", width: 6
						} else {
							paragraph "", width: 6
						}
						if(button_1_hold_dim || button_1_hold_brighten || button_2_hold_dim || button_2_hold_brighten || button_3_hold_dim || button_3_hold_brighten || button_4_hold_dim || button_4_hold_brighten || button_5_hold_dim || button_5_hold_brighten){
						input "holdMultiplier", "decimal", required: false, title: "<b>Hold mulitplier.</b> (Optional. Default 1.4.)", width: 6
						} else {
							paragraph "", width: 6
						}
					}
				}
				if(!error && (button_1_push_on || button_1_push_off || button_1_push_dim || button_1_push_brighten || button_1_push_toggle || button_2_push_on || button_2_push_off || button_2_push_dim || button_2_push_brighten || button_2_push_toggle || button_3_push_on || button_3_push_off || button_3_push_dim || button_3_push_brighten || button_3_push_toggle || button_4_push_on || button_4_push_off || button_4_push_dim || button_4_push_brighten || button_4_push_toggle || button_5_push_on || button_5_push_off || button_5_push_dim || button_5_push_brighten || button_5_push_toggle || button_1_hold_on || button_1_hold_off || button_1_hold_dim || button_1_hold_brighten || button_1_hold_toggle || button_2_hold_on || button_2_hold_off || button_2_hold_dim || button_2_hold_brighten || button_2_hold_toggle || button_3_hold_on || button_3_hold_off || button_3_hold_dim || button_3_hold_brighten || button_3_hold_toggle || button_4_hold_on || button_4_hold_off || button_4_hold_dim || button_4_hold_brighten || button_4_hold_toggle || button_5_hold_on || button_5_hold_off || button_5_hold_dim || button_5_hold_brighten || button_5_hold_toggle)){
					section(){
						paragraph "<div style=\"background-color:LightCyan\"><b> Click \"Done\" to continue.</b></div>"
					}
				} else if(!error) {
					section(){
						paragraph "<div style=\"background-color:MistyRose\"><b> No actions selected. Do NOT save.</b></div>"
					}
				}
			}
		}
	}
}


def installed() {
	logTrace("$app.label: installed")
	if(app.getLabel().length() < 4)  app.updateLabel("Pico - " + app.getLabel())
    if(app.getLabel().substring(0,4) != "Pico") app.updateLabel("Pico - " + app.getLabel())
    initialize()
}

def updated() {
	logTrace("$app.label: updated")
    unsubscribe()
    initialize()
}

def initialize() {
	logTrace("$app.label: initialized")

	subscribe(buttonDevice, "pushed", buttonPushed)
	subscribe(buttonDevice, "held", buttonHeld)
	subscribe(buttonDevice, "released", buttonReleased)
}

def buttonPushed(evt){
	logTrace("$app.label: function buttonPushed starting [$evt.value]")
    def appId = app.getId()
    def buttonNumber = evt.value
    def colorSwitch
    def whiteSwitch

    if(pushMultiplier) pushMultiplier = parent.validateMultiplier(pushMultiplier,appId)
	
	// Simple setup
	if(!multiDevice && !advancedSetup){
		switch(buttonNumber){
			case "1": parent.multiOn(controlDevice,appId)
				break
			case "2": parent.brighten(controlDevice,appId)
				break
			case "3": parent.toggle(controlDevice,appId)
				break
			case "4": parent.dim(controlDevice,appId)
				break
			case "5": parent.multiOff(controlDevice,appId)
		}
		logTrace("$app.label: function buttonPushed exiting (no multiDevice and no advanced setup)")
		return
	}
	
	if(!multiDevice && advanceSetup){
		if(buttonNumber == "1"){
			switch(buttonPush1){
				case "on": parent.multiOn(controlDevice,appId)
					break
				case "brighten": parent.brighten(controlDevice,appId)
					break
				case "toggle": parent.toggle(controlDevice,appId)
					break
				case "dim": parent.dim(controlDevice,appId)
					break
				case "off": parent.multiOff(controlDevice,appId)
			}
		} else if(buttonNumber == "2"){
			switch(buttonPush2){
				case "on": parent.multiOn(controlDevice,appId)
					break
				case "brighten": parent.brighten(controlDevice,appId)
					break
				case "toggle": parent.toggle(controlDevice,appId)
					break
				case "dim": parent.dim(controlDevice,appId)
					break
				case "off": parent.multiOff(controlDevice,appId)
			}
		} else if(buttonNumber == "3"){
			switch(buttonPush2){
				case "on": parent.multiOn(controlDevice,appId)
					break
				case "brighten": parent.brighten(controlDevice,appId)
					break
				case "toggle": parent.toggle(controlDevice,appId)
					break
				case "dim": parent.dim(controlDevice,appId)
					break
				case "off": parent.multiOff(controlDevice,appId)
			}
		} else if(buttonNumber == "4"){
			switch(buttonPush2){
				case "on": parent.multiOn(controlDevice,appId)
					break
				case "brighten": parent.brighten(controlDevice,appId)
					break
				case "toggle": parent.toggle(controlDevice,appId)
					break
				case "dim": parent.dim(controlDevice,appId)
					break
				case "off": parent.multiOff(controlDevice,appId)
			}
		} else if(buttonNumber == "5"){
			switch(buttonPush2){
				case "on": parent.multiOn(controlDevice,appId)
					break
				case "brighten": parent.brighten(controlDevice,appId)
					break
				case "toggle": parent.toggle(controlDevice,appId)
					break
				case "dim": parent.dim(controlDevice,appId)
					break
				case "off": parent.multiOff(controlDevice,appId)
			}
		}
		logTrace("$app.label: function buttonPushed exiting (multiDevice and no advanced setup)")
		return
	}

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
		if(button_1_push_dim) parent.dim(button_1_push_dim,appId,manualOverride=true)
        if(button_1_push_brighten) parent.brighten(button_1_push_brighten,appId,manualOverride=true)
		logTrace("$app.label: function buttonPushed exiting (buttonNumber 1)")
		return
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
		if(button_2_push_dim) parent.dim(button_2_push_dim,appId,manualOverride=true)
        if(button_2_push_brighten) parent.brighten(button_2_push_brighten,appId,manualOverride=true)
		logTrace("$app.label: function buttonPushed exiting (buttonNumber 2)")
		return
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
        if(button_3_push_dim) parent.dim(button_3_push_dim,appId,manualOverride=true)
        if(button_3_push_brighten) parent.brighten(button_3_push_brighten,appId,manualOverride=true)
		logTrace("$app.label: function buttonPushed exiting (buttonNumber 3)")
		return
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
        if(button_4_push_dim) parent.dim(button_4_push_dim,appId,manualOverride=true)
        if(button_4_push_brighten) parent.brighten(button_4_push_brighten,appId,manualOverride=true)
		logTrace("$app.label: function buttonPushed exiting (buttonNumber 4)")
		return
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
        if(button_5_push_dim) parent.dim(button_5_push_dim,appId,manualOverride=true)
        if(button_5_push_brighten) parent.brighten(button_5_push_brighten,appId,manualOverride=true)
		logTrace("$app.label: function buttonPushed exiting (buttonNumber 5)")
		return
    }
	logTrace("$app.label: function buttonPushed exiting")
}

def buttonHeld(evt){
	logTrace("$app.label: function buttonHeld starting [$evt.value]")
    def appId = app.getId()
    def buttonNumber = evt.value
    def colorSwitch
    def whiteSwitch

    if(holdMultiplier) holdMultiplier = parent.validateMultiplier(holdMultiplier,appId)

// TO DO - see if these can be moved so doesn't need to precess at every button click
	if(!multiDevice && !advancedSetup && !replicateHold){
		switch(buttonNumber){
			case "1": parent.multiOn(controlDevice,appId)
				break
			case "2": parent.holdBrighten(controlDevice,appId)
				break
			case "3": parent.toggle(controlDevice,appId)
				break
			case "4": parent.holdDim(controlDevice,appId)
				break
			case "5": parent.multiOff(controlDevice,appId)
		}
		logTrace("$app.label: function buttonHeld exiting (not multiDevice, not advancedSetup and not replicateHold)")
		return
	}

	if(!multiDevice && advanceSetup && !replicateHold){
		if(buttonNumber == "1"){
			switch(buttonPush1){
				case "on": parent.multiOn(controlDevice,appId)
					break
				case "brighten": parent.holdBrighten(controlDevice,appId)
					break
				case "toggle": parent.toggle(controlDevice,appId)
					break
				case "dim": parent.holdDim(controlDevice,appId)
					break
				case "off": parent.multiOff(controlDevice,appId)
			}
		} else if(buttonNumber == "2"){
			switch(buttonPush2){
				case "on": parent.multiOn(controlDevice,appId)
					break
				case "brighten": parent.holdBrighten(controlDevice,appId)
					break
				case "toggle": parent.toggle(controlDevice,appId)
					break
				case "dim": parent.holdDim(controlDevice,appId)
					break
				case "off": parent.multiOff(controlDevice,appId)
			}
		} else if(buttonNumber == "3"){
			switch(buttonPush3){
				case "on": parent.multiOn(controlDevice,appId)
					break
				case "brighten": parent.holdBrighten(controlDevice,appId)
					break
				case "toggle": parent.toggle(controlDevice,appId)
					break
				case "dim": parent.holdDim(controlDevice,appId)
					break
				case "off": parent.multiOff(controlDevice,appId)
			}
		} else if(buttonNumber == "4"){
			switch(buttonPush4){
				case "on": parent.multiOn(controlDevice,appId)
					break
				case "brighten": parent.holdBrighten(controlDevice,appId)
					break
				case "toggle": parent.toggle(controlDevice,appId)
					break
				case "dim": parent.holdDim(controlDevice,appId)
					break
				case "off": parent.multiOff(controlDevice,appId)
			}
		} else if(buttonNumber == "5"){
			switch(buttonPush5){
				case "on": parent.multiOn(controlDevice,appId)
					break
				case "brighten": parent.holdBrighten(controlDevice,appId)
					break
				case "toggle": parent.toggle(controlDevice,appId)
					break
				case "dim": parent.holdDim(controlDevice,appId)
					break
				case "off": parent.multiOff(controlDevice,appId)
			}
		}
		logTrace("$app.label: function buttonHeld exiting (not multiDevice, advancedSetup and not replicateHold)")
		return
	}

	if(!multiDevice && advanceSetup && replicateHold){
		if(buttonNumber == "1"){
			switch(buttonHold1){
				case "on": parent.multiOn(controlDevice,appId)
					break
				case "brighten": parent.holdBrighten(controlDevice,appId)
					break
				case "toggle": parent.toggle(controlDevice,appId)
					break
				case "dim": parent.holdDim(controlDevice,appId)
					break
				case "off": parent.multiOff(controlDevice,appId)
			}
		} else if(buttonNumber == "2"){
			switch(buttonHold2){
				case "on": parent.multiOn(controlDevice,appId)
					break
				case "brighten": parent.holdBrighten(controlDevice,appId)
					break
				case "toggle": parent.toggle(controlDevice,appId)
					break
				case "dim": parent.holdDim(controlDevice,appId)
					break
				case "off": parent.multiOff(controlDevice,appId)
			}
		} else if(buttonNumber == "3"){
			switch(buttonHold3){
				case "on": parent.multiOn(controlDevice,appId)
					break
				case "brighten": parent.holdBrighten(controlDevice,appId)
					break
				case "toggle": parent.toggle(controlDevice,appId)
					break
				case "dim": parent.holdDim(controlDevice,appId)
					break
				case "off": parent.multiOff(controlDevice,appId)
			}
		} else if(buttonNumber == "4"){
			switch(buttonHold4){
				case "on": parent.multiOn(controlDevice,appId)
					break
				case "brighten": parent.holdBrighten(controlDevice,appId)
					break
				case "toggle": parent.toggle(controlDevice,appId)
					break
				case "dim": parent.holdDim(controlDevice,appId)
					break
				case "off": parent.multiOff(controlDevice,appId)
			}
		} else if(buttonNumber == "5"){
			switch(buttonHold5){
				case "on": parent.multiOn(controlDevice,appId)
					break
				case "brighten": parent.holdBrighten(controlDevice,appId)
					break
				case "toggle": parent.toggle(controlDevice,appId)
					break
				case "dim": parent.holdDim(controlDevice,appId)
					break
				case "off": parent.multiOff(controlDevice,appId)
			}
		}
		logTrace("$app.label: function buttonHeld exiting (not multiDevice, advancedSetup and replicateHold)")
		return
	}


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
		logTrace("$app.label: function buttonHeld exiting (button number 1)")
		return
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
		logTrace("$app.label: function buttonHeld exiting (button number 2)")
		return
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
		logTrace("$app.label: function buttonHeld exiting (button number 3)")
		return
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
		logTrace("$app.label: function buttonHeld exiting (button number 4)")
		return
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
		logTrace("$app.label: function buttonHeld exiting (button number 5)")
		return
    }
	logTrace("$app.label: function buttonHeld exiting")
}

def buttonReleased(evt){
	logTrace("$app.label: function buttonReleased starting [$evt.value]")
	def buttonNumber = evt.value
	if (buttonNumber == "2" || (buttonNumber == "4" && (settings.numButton == "4 button" || settings.numButton == "5 button")) || (buttonNumber == "1" && settings.numButton == "2 button")){
		logTrace("$app.label: function buttonHeld unscheduling all")
		unschedule()
	}
	logTrace("$app.label: function buttonHeld exiting")
}

def dimSpeed(){
	logTrace("$app.label: function dimSpeed starting")
	if(settings.multiplier != null){
		logTrace("$app.label: function dimSpeed returning $pushMultiplier")
		return pushMultiplier
	} else {
		logTrace("$app.label: function dimSpeed returning 1.2")
		return 1.2
	}
}

def holdDimSpeed(){
	logTrace("$app.label: function holdDimSpeed starting")
	if(settings.multiplier != null){
		logTrace("$app.label: function holdDimSpeed returning $holdMultiplier")
		return holdMultiplier
	} else {
		logTrace("$app.label: function holdDimSpeed returning 1.4")
		return 1.4
	}
}

// counts number of steps for brighten and dim
// action = "dim" or "brighten"
def getSteps(lvl, action){
	logTrace("$app.label: function getSteps starting [lvl: $lvl, action: $action]")
	def steps = 0

	if (action != "dim" && action != "brighten"){
		logTrace("$app.label: function getSteps returning null (invalid action")
		return false
	}
	// If as already level 1 and dimming or 100 and brightening
	if((action == "dim" && lvl < 2) || (action == "brighten" && lvl>99)){
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
	logTrace("$app.label: function getSteps returning $steps")
	return steps
}

def setSubscribeLevel(data){
	logTrace("$app.label: function setSubscribeLevel returning [date: $data]")
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
		logTrace("$app.label: function setSubscribeLevel returning (no matching device)")
		return
	}
	level = data.level as int
	parent.setToLevel(device,level,app.getId())
	reschedule(it,mannualOverride=true)
	logTrace("$app.label: function setSubscribeLevel returning (no matching device)")
}

def toggleSeparate(device){
	logTrace("$app.label: function setSubscribeLevel returning (no matching device)")
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
			parent.reschedule(it,mannualOverride=true)
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
			reschedule(it,mannualOverride=true)
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

def logTrace(message){
	//log.trace(message)
}
