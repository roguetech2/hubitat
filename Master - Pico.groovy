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
*f
*  You should have received a copy of the GNU General Public License along with this program.
*  If not, see <http://www.gnu.org/licenses/>.
*
*  Name: Master
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master - Pico.groovy
*  Version: 0.3.06
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
/* ************************************************** */
/* TO-DO: Add optiobn to disable contact or schedule? */
/* ************************************************** */
/* ************************************************** */
/* TO-DO: Have Pico disable and/or pause schedules,   */
/* eg if schedule is brightening, and Pico dims,      */
/* disable schedule; if schedule is dimming and PIco  */
/* dims lower than schedule is at, pause schedule.    */
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
							error = parent.compareDeviceLists(button_1_push_off,button_1_push_on,app.label)
							if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn on and off the same device.</div>"
						}
					} else {
						input "button_1_push_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
					}

					if(button_1_push_toggle && !error) {
						input "button_1_push_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
						// Can't toggle and turn on/off
						if(button_1_push_on || button_1_push_off){
							error = parent.compareDeviceLists(button_1_push_on,button_1_push_toggle,app.label)
							error = parent.compareDeviceLists(button_1_push_off,button_1_push_toggle,app.label)
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
								error = parent.compareDeviceLists(button_2_push_toggle,button_2_push_off,app.label)
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
								error = parent.compareDeviceLists(button_1_hold_off,button_1_hold_on,app.label)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to turn on and off the same device.</div>"
							}
						} else {
							input "button_1_hold_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}

						if(button_1_hold_toggle && !error) {
							input "button_1_hold_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
							// Can't toggle and turn on/off
							if(button_1_hold_on){
								error = parent.compareDeviceLists(button_1_hold_on,button_1_hold_toggle,app.label)
								if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to toggle as well as turn on the same device.</div>"
							}
							if(button_1_hold_off){
								error = parent.compareDeviceLists(button_1_hold_off,button_1_hold_toggle,app.label)
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
									error = parent.compareDeviceLists(button_2_hold_toggle,button_2_hold_on,app.label)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to toggle as well as turn on or off the same device.</div>"
								}
							} else {
								input "button_2_hold_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_2_hold_off && !error) {
								input "button_2_hold_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
								if(button_2_hold_toggle){
									error = parent.compareDeviceLists(button_2_hold_toggle,button_2_hold_off,app.label)
									if(error) paragraph "<div style=\"background-color:Bisque\">$errorIcon Can't set same button to toggle and off the same device.</div>"
								}
								if(button_2_hold_on){
									error = parent.compareDeviceLists(button_2_hold_toggle,button_2_hold_off,app.label)
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
	logTrace(840. "Installed")
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
	logTrace(846,"Updated")
    unsubscribe()
    initialize()
}

def initialize() {
	logTrace(852,"Initialized")

    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

	subscribe(buttonDevice, "pushed", buttonPushed)
	subscribe(buttonDevice, "held", buttonHeld)
	subscribe(buttonDevice, "released", buttonReleased)
}

def buttonPushed(evt){

    def buttonNumber = evt.value
    def colorSwitch
    def whiteSwitch

    if(pushMultiplier) pushMultiplier = parent.validateMultiplier(pushMultiplier,app.label)

	// Treat 2nd button of 2-button Pico as "off" (eg button 5)
	if(buttonNumber == "2" &&  numButton == "2 button") buttonNumber = 5

	// Simple setup
	if(!multiDevice && !advancedSetup){
		switch(buttonNumber){
			case "1": parent.multiOn(controlDevice,app.label)
                logTrace(876,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; default setup; turning on")
				break
			case "2": parent.brighten(controlDevice,app.getId())
                logTrace(879,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; default setup; brightening")
				break
			case "3": parent.toggle(controlDevice,app.label)
                logTrace(882,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; default setup; toggling")
				break
			case "4": parent.dim(controlDevice,app.getId())
                logTrace(885,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; default setup; dimming")
				break
			case "5": parent.multiOff(controlDevice,app.label)
                logTrace(888,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; default setup; turning off")
		}
	} else if(!multiDevice && advanceSetup){
			switch(buttonPush${buttonNumber}){
				case "on": parent.multiOn(controlDevice,app.label)
                    logTrace(893,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; advanced setup; turning on")
					break
				case "brighten": parent.brighten(controlDevice,app.getId())
                    logTrace(896,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; advanced setup; brightening")
					break
				case "toggle": parent.toggle(controlDevice,app.label)
                    logTrace(899,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; advanced setup; toggling")
					break
				case "dim": parent.dim(controlDevice,app.getId())
                    logTrace(902,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; advanced setup; dimming")
					break
				case "off": parent.multiOff(controlDevice,app.label)
                    logTrace(905,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; advanced setup; turning off")
			}
	} else if(multiDevice && advanceSetup){

        if(button_${buttonNumber}_push_toggle != null) {
                    logTrace(910,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; remapped and advanced setup; toggling")
            if (settings.color == "Separate"){
                toggleSeparate(button_${buttonNumber}_push_toggle)
            } else {
                parent.toggle(button_${buttonNumber}_push_toggle,app.label)
            }
        }
        if(button_${buttonNumber}_push_on) {
                    logTrace(918,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; remapped and advanced setup; turning on")
            parent.multiOn(button_${buttonNumber}_push_on,app.label)
        }
        if(button_${buttonNumber}_push_off){
                    logTrace(922,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; remapped and advanced setup; turning off")
            parent.multiOff(button_${buttonNumber}_push_off,app.label)
        }
        if(button_${buttonNumber}_push_dim) {
                    logTrace(926,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; remapped and advanced setup; dimming")
            parent.dim(button_${buttonNumber}_push_dim,app.getId())
        }
        if(button_${buttonNumber}_push_brighten) {
                    logTrace(930,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; remapped and advanced setup; brightening")
            parent.brighten(button_${buttonNumber}_push_brighten,app.getId())
        }
}

def buttonHeld(evt){

    def buttonNumber = evt.value
    def colorSwitch
    def whiteSwitch

	// Treat 2nd button of 2-button Pico as "off" (eg button 5)
	if(buttonNumber == "2" &&  numButton == "2 button") buttonNumber = 5

    if(holdMultiplier) holdMultiplier = parent.validateMultiplier(holdMultiplier,app.label)

// TO DO - see if these can be moved so doesn't need to process at every button click

// We are missing multiDevice + advancedSetup (minus replicateHold)!!
//But, why does multiDevice lead to multiOn/Off??

	if(!multiDevice && !advancedSetup && !replicateHold){
		switch(buttonNumber){
			case "1": parent.multiOn(controlDevice,app.label)
                    logTrace(954,"Button $buttonNumber of $buttonDevice held for $controlDevice; simple setup; turning on")
				break
			case "2": holdBrighten(controlDevice,app.label)
                    logTrace(957,"Button $buttonNumber of $buttonDevice held for $controlDevice; simple setup; brightening")
				break
			case "3": parent.toggle(controlDevice,app.label)
                    logTrace(960,"Button $buttonNumber of $buttonDevice held for $controlDevice; simple setup; toggling")
				break
			case "4": holdDim(controlDevice,app.label)
                    logTrace(963,"Button $buttonNumber of $buttonDevice held for $controlDevice; simple setup; dimming")
				break
			case "5": parent.multiOff(controlDevice,app.label)
                    logTrace(966,"Button $buttonNumber of $buttonDevice held for $controlDevice; simple setup; turning off")
		}
	} else if(!multiDevice && advanceSetup && !replicateHold){
			switch(buttonPush${buttonNumber}){
				case "on": parent.multiOn(controlDevice,app.label)
                    logTrace(971,"Button $buttonNumber of $buttonDevice held for $controlDevice; advanced setup; turning on")
					break
				case "brighten": holdBrighten(controlDevice,app.label)
                    logTrace(974,"Button $buttonNumber of $buttonDevice held for $controlDevice; advanced setup; brightening")
					break
				case "toggle": parent.toggle(controlDevice,app.label)
                    logTrace(977,"Button $buttonNumber of $buttonDevice held for $controlDevice; advanced setup; toggling")
					break
				case "dim": holdDim(controlDevice,app.label)
                    logTrace(980,"Button $buttonNumber of $buttonDevice held for $controlDevice; advanced setup; dimming")
					break
				case "off": parent.multiOff(controlDevice,app.label)
                    logTrace(983,"Button $buttonNumber of $buttonDevice held for $controlDevice; advanced setup; turning off")
			}
	} else if(!multiDevice && advanceSetup && replicateHold){
			switch(buttonHold${buttonNumber}){
				case "on": parent.multiOn(controlDevice,app.label)
                    logTrace(988,"Button $buttonNumber of $buttonDevice held for $controlDevice; advanced setup; turning on")
					break
				case "brighten": holdBrighten(controlDevice,app.label)
                    logTrace(991,"Button $buttonNumber of $buttonDevice held for $controlDevice; advanced setup; brightening")
					break
				case "toggle": parent.toggle(controlDevice,app.label)
                    logTrace(994,"Button $buttonNumber of $buttonDevice held for $controlDevice; advanced setup; toggling")
					break
				case "dim": holdDim(controlDevice,app.label)
                    logTrace(997,"Button $buttonNumber of $buttonDevice held for $controlDevice; advanced setup; dimming")
					break
				case "off": parent.multiOff(controlDevice,app.label)
                    logTrace(1000,"Button $buttonNumber of $buttonDevice held for $controlDevice; advanced setup; turning off")
			}
	} else if(multiDevice && advanceSetup && replicateHold){
		if(button_${buttonNumber}_hold_toggle != null) {
			logTrace(1004,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; remapped and advanced setup; toggling")
			if (settings.color == "Separate"){
				toggleSeparate(button_${buttonNumber}_push_toggle)
			} else {
				parent.toggle(button_${buttonNumber}_push_toggle,app.label)
			}
		}
		if(button_${buttonNumber}_hold_on) {
			logTrace(1012,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; remapped and advanced setup; turning on")
			parent.multiOn(button_${buttonNumber}_hold_on,app.label)
		}
		if(button_${buttonNumber}_hold_off) {
			logTrace(1016,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; remapped and advanced setup; turning off")
			parent.multiOff(button_${buttonNumber}_hold_off,app.label)
		}
		if(button_${buttonNumber}_hold_dim) {
			logTrace(1020,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; remapped and advanced setup; dimming")
			holdDim(button_${buttonNumber}_hold_dim)
		}
		if(button_${buttonNumber}_hold_brighten) {
			logTrace(1024,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; remapped and advanced setup; brightening")
			holdBrighten(button_${buttonNumber}_hold_brighten)
		}
	}
}

def buttonReleased(evt){
	def buttonNumber = evt.value
	if (buttonNumber == "2" || (buttonNumber == "4" && (settings.numButton == "4 button" || settings.numButton == "5 button")) || (buttonNumber == "1" && settings.numButton == "2 button")){
		logTrace(1034,"Button $buttonNumber of $buttonDevice released, unscheduling all")
		unschedule()
	}
}


//What's the difference between multiplier, pushMultiplier and holdMultiplier?!
def dimSpeed(){
	if(settings.multiplier != null){
		logTrace(1040,"function dimSpeed returning $pushMultiplier")
		return pushMultiplier
	} else {
		logTrace(1043,"function dimSpeed returning 1.2")
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
def getSteps(level, action){
	logTrace("$app.label: function getSteps starting [level: $level, action: $action]")
	def steps = 0

	if (action != "dim" && action != "brighten"){
		logTrace("$app.label: function getSteps returning null (invalid action")
		return false
	}

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
	logTrace(1089,"Function getSteps returning $steps")
	return steps
}

def setSubscribeLevel(data){

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
		logTrace(1144,"Function setSubscribeLevel returning (no matching device)")
		return
	}
	level = data.level as int
	parent.setToLevel(device,level,app.label)
	reschedule(it)
}

def toggleSeparate(device){
	device.each{
		if(it.currentValue("hue") && it.currentValue("switch") == "on") {
			colorSwitch = "on"
		} else if(!it.currentValue("hue") && it.currentValue("switch") == "on") {
			whiteSwitch = "on"
		}
	}
	// color on, white on, turn off color
	if (colorSwitch == "on" && whiteSwitch == "on"){
		parent.multiOff(device,app.label)
	// color on, white off; turn white on
	} else if (colorSwitch == "on" && whiteSwitch != "on"){
		parent.multiOn(device,app.label)
	//color off, white on; turn off white and turn on color
	} else if (colorSwitch != "on" && whiteSwitch == "on"){
		parent.multiOff(device,"white",app.label)
		parent.multiOn(device,app.label)
	// both off; turn color on
	} else if (colorSwitch != "on" && whiteSwitch != "on"){
		parent.multiOn(device,app.label)
	}
}

def holdDim(device){
    def level = getLevel(device)
	
    device.each{
        if(parent.isFan(it,app.label) == true){
            parent.dim(it,app.getId())
        } else if(!parent.stateOn(it,app.label)){
            parent.setToLevel(it,1,app.label)
			parent.reschedule(it,app.label)
        } else {
            if(level < 2){
                log.Trace("Can't dim $it; already 1%.")
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
            parent.brighten(it,app.label)
        } else if(!parent.stateOn(it,app.label)){
            parent.setToLevel(it,1,app.label)
			reschedule(it)
        } else {
            if(level > 99){
                logTrace(1660,"Pico: Can't brighten $it; already 100%.")
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

def logTrace(lineNumber,message){
	log.trace "$app.label (line $lineNumber) -- $message"
}
