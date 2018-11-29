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
*  Version: 0.3.03
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
						input "numButton", "enum", title: "Pico buttons?", multiple: false, required: true, options: ["2 button", "4 button", "5 button"], submitOnChange:true
						if(!numButton){
							paragraph "<div style=\"background-color:BurlyWood\"> </div>"
						} else if(numButton){
							paragraph "For each action, select which lights or switches to turn on, turn off, toggle, dim/slow, and/or brighten/speed up. Do not have an action both turn on and off the same light/switch (use Toggle). Do not have an action both dim/slow and brighten/speed up the same light/fan."
							if(!advancedSetup){
								input "advancedSetup", "bool", title: "<b>Simple actions.</b> Click to show advanced options.", defaultValue: false, submitOnChange:true
							} else {
								input "advancedSetup", "bool", title: "<b>Advanced actions.</b> Clck to hide advanced options.", defaultValue: false, submitOnChange:true
							}

							input "multiDevice", "bool", title: "Mutli-control: <b>Controls one set of light(s)/switch(es).</b> Click for Pico to independantly control different sets of lights/switches (eg a light and a fan).", defaultValue: false, submitOnChange:true
							paragraph "Use this option if you only want to control one light or set of lights. Change this option if, for instance, you want to turn on some lights, and brighten/dim <i>different lights</i>."
							input "controlDevice", "capability.switch", title: "Device(s) to control", multiple: true, required: true, submitOnChange:true

							if(advancedSetup){
								paragraph "<b>Pro-tip</b>: Profiles for Multi-control enabled and disabled are stored separatly, allowing toggling between two different setups. To do this, set the options both with Multi-control disabled and enabled."
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
											input "buttonPush2", "enum", title: "ushing \"Brighten\" button?", required: false, multiple: false, options: ["brighten":"Brighten","dim":"Dim", "on":"Turn on", "off":"Turn off", "toggle":"Toggle"], submitOnChange:true
										if(numButton == "5 button") input "buttonPush3", "enum", title: "ushing Center button?", required: false, multiple: false, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true
										if(numButton == "4 button" || numButton == "5 button") input "buttonPush4", "enum", title: "ushing \"Dim\" button?", required: false, multiple: false, options: ["on":"Turn on", "off":"Turn off", "toggle":"Toggle","dim":"Dim", "brighten":"Brighten"], submitOnChange:true
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
									paragraph "Multiplier/divider for dimming and brightening, from 1.01 to 99, where higher is faster. For instance, a value of 2 would double (eg from 25% to 50%, then 100%), whereas a value of 1.5 would increase by half each time (eg from 25% to 38% to 57%)."
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
					paragraph "<div style=\"background-color:BurlyWood\"><b>Set type of Pico:</b></div>"
					input "numButton", "enum", title: "Pico buttons?", multiple: false, required: true, options: ["2 button", "4 button", "5 button"], submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				}
			} else if(app.label && buttonDevice && numButton && !advancedSetup){
				section() {
					paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this Pico setup:</b></div>"
					label title: "", required: true, submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select Pico device to setup:</b></div>"
					input "buttonDevice", "capability.pushableButton", title: "Pico(s)?", multiple: true, required: true, submitOnChange:true
					input "numButton", "enum", title: "<b>Type of Pico</b>", multiple: false, required: true, options: ["2 button", "4 button", "5 button"], submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"

					paragraph "For each action, select which lights or switches to turn on, turn off, toggle, dim/slow, and/or brighten/speed up. Do not have an action both turn on and off the same light/switch (use Toggle). Do not have an action both dim/slow and brighten/speed up the same light/fan."
					input "advancedSetup", "bool", title: "<b>Simple actions.</b> Click to show advanced actiions.", defaultValue: false, submitOnChange:true
					input "multiDevice", "bool", title: "Multi-control: <b>Independantly control different sets of lights/switches.</b> Click for Pico to control only one set of lights/switches.", defaultValue: true, submitOnChange:true
					paragraph "Use this option if you only want to control one light or set of lights. Change this option if, for instance, you want to turn on some lights, and brighten/dim <i>different lights</i>."

					paragraph "<div style=\"background-color:BurlyWood\"><b> Select what to do for each Pico action:</b></div>"

					paragraph "To set different functions to each button, change to Advanced actions by clicking \"Simple actions\"."
					if(!replicateHold){
						input "button_1_push_on", "capability.switch", title: "Top \"On\" button turns on?", multiple: true, required: false, submitOnChange:true
						if(numButton == "4 button" || numButton == "5 button"){
							input "button_2_push_brighten", "capability.switchLevel", title: "\"Brighten\" button brigtens?", multiple: true, required: false, submitOnChange:true
						}
						if(numButton == "5 button"){
							input "button_3_push_toggle", "capability.switch", title: "Center button toggles? (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
						}
						if(numButton == "4 button" || numButton == "5 button"){
							input "button_4_push_dim", "capability.switchLevel", title: "\"Dim\" button dims?", multiple: true, required: false, submitOnChange:true
						}
						input "button_5_push_off", "capability.switch", title: "Bottom (\"Off\") buttont turns off?", multiple: true, required: false, submitOnChange:true

						if(!replicateHold){
							input "replicateHold", "bool", title: "Replicating settings for Long Push. Click to customize Hold actions.", submitOnChange:true, defaultValue: false
						} else {
							input "replicateHold", "bool", title: "Long Push options shown. Click to replicate from Push.", submitOnChange:true, defaultValue: false
						}

					} else if(replicateHold){
						input "button_1_push_on", "capability.switch", title: "Pushing Top \"On\" button turns on?", multiple: true, required: false, submitOnChange:true
						if(numButton == "4 button" || numButton == "5 button"){
							input "button_2_push_brighten", "capability.switchLevel", title: "Pushing \"Brighten\" button brigtens?", multiple: true, required: false, submitOnChange:true
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
							input "button_2_hold_brighten", "capability.switchLevel", title: "Holding \"Brighten\" button brigtens?", multiple: true, required: false, submitOnChange:true
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
						paragraph "Multiplier/divider for dimming and brightening, from 1.01 to 99, where higher is faster. For instance, a value of 2 would double (eg from 25% to 50%, then 100%), whereas a value of 1.5 would increase by half each time (eg from 25% to 38% to 57%)."
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
					input "numButton", "enum", title: "<b>Type of Pico</b>", multiple: false, required: true, options: ["2 button", "4 button", "5 button"], submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"

					paragraph "For each action, select which lights or switches to turn on, turn off, toggle, dim/slow, and/or brighten/speed up. Do not have an action both turn on and off the same light/switch (use Toggle). Do not have an action both dim/slow and brighten/speed up the same light/fan."
					input "advancedSetup", "bool", title: "<b>Simple setup.</b> Click to show advanced actions.", defaultValue: false, submitOnChange:true
					input "multiDevice", "bool", title: "Mutli-control: <b>Independantly control different sets of lights/switches.</b> Click for Pico to control only one set of lights/switches.", defaultValue: true, submitOnChange:true
					paragraph "Use this option if you only want to control one light or set of lights. Change this option if, for instance, you want to turn on some lights, and brighten/dim <i>different lights</i>."

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
					} else {
						input "button_1_push_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
					}
					if(button_1_push_toggle) {
						input "button_1_push_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
					} else {
						input "button_1_push_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
					}
					if(button_1_push_dim) {
						input "button_1_push_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
					} else {
						input "button_1_push_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
					}
					if(button_1_push_brighten) {
						input "button_1_push_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
					} else {
						input "button_1_push_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
					}
				}
				if(numButton == "4 button" || numButton == "5 button"){
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
						} else {
							input "button_2_push_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_2_push_off) {
							input "button_2_push_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_2_push_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_2_push_dim) {
							input "button_2_push_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_2_push_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
					}
				}
				if(numButton == "5 button"){
					section(hideable: true, hidden: true, "Middle Button <font color=\"gray\">(Click to expand/collapse)</font>") {
						if(button_3_push_toggle) {
							input "button_3_push_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_3_push_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_3_push_on) {
							input "button_3_push_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_3_push_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_3_push_off) {
							input "button_3_push_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_3_push_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_3_push_dim) {
							input "button_3_push_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_3_push_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_3_push_brighten) {
							input "button_3_push_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_3_push_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
					}
				}
				if(numButton == "4 button" || numButton == "5 button"){
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
						} else {
							input "button_4_push_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_4_push_off) {
							input "button_4_push_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_4_push_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_4_push_brighten) {
							input "button_4_push_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_4_push_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
					}
				}

				section(hideable: true, hidden: true, "Bottom Button (\"Off\") <font color=\"gray\">(Click to expand/collapse)</font>") {
					if(button_5_push_off) {
						input "button_5_push_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false
					} else {
						input "button_5_push_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false
					}
					if(button_5_push_toggle) {
						input "button_5_push_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false
					} else {
						input "button_5_push_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false
					}
					if(button_5_push_on) {
						input "button_5_push_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false
					} else {
						input "button_5_push_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false
					}
					if(button_5_push_dim) {
						input "button_5_push_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
					} else {
						input "button_5_push_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
					}
					if(button_5_push_brighten) {
						input "button_5_push_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
					} else {
						input "button_5_push_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
					}
				}

				
				if(!replicateHold){
					section(){
						input "replicateHold", "bool", title: "Replicating settings for Long Push. Click to customize.", submitOnChange:true, defaultValue: false
					}
				} else {
					section(){
						input "replicateHold", "bool", title: "Long Push options shown. Click to replicate from Push.", submitOnChange:true, defaultValue: false
					}


// Advanced Hold

					section(hideable: true, hidden: true, "Top Button (\"On\") <font color=\"gray\">(Click to expand/collapse)</font>") {
						if(button_1_hold_on) {
							input "button_1_hold_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_1_hold_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_1_hold_toggle) {
							input "button_1_hold_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_1_hold_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_1_hold_off) {
							input "button_1_hold_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_1_hold_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_1_hold_dim) {
							input "button_1_hold_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_1_hold_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_1_hold_brighten) {
							input "button_1_hold_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_1_hold_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
					}
					if(numButton == "4 button" || numButton == "5 button"){
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
							} else {
								input "button_2_hold_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_2_hold_off) {
								input "button_2_hold_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
							} else {
								input "button_2_hold_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_2_hold_dim) {
								input "button_2_hold_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
							} else {
								input "button_2_hold_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
						}
					}
					if(numButton == "5 button"){
						section(hideable: true, hidden: true, "Middle Button <font color=\"gray\">(Click to expand/collapse)</font>") {
							if(button_3_hold_toggle) {
								input "button_3_hold_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
							} else {
								input "button_3_hold_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_3_hold_on) {
								input "button_3_hold_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
							} else {
								input "button_3_hold_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_3_hold_off) {
								input "button_3_hold_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
							} else {
								input "button_3_hold_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_3_hold_dim) {
								input "button_3_hold_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
							} else {
								input "button_3_hold_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_3_hold_brighten) {
								input "button_3_hold_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
							} else {
								input "button_3_hold_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
						}
					}
					if(numButton == "4 button" || numButton == "5 button"){
						section(hideable: true, hidden: true, "\"Dim\" Button <font color=\"gray\">(Click to expand/collapse)</font>") {
							if(button_4_hold_dim) {
								input "button_4_hold_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
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
							} else {
								input "button_4_hold_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_4_hold_off) {
								input "button_4_hold_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
							} else {
								input "button_4_hold_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
							if(button_4_hold_brighten) {
								input "button_4_hold_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
							} else {
								input "button_4_hold_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
							}
						}
					}
					section(hideable: true, hidden: true, "Bottom Button (\"Off\") <font color=\"gray\">(Click to expand/collapse)</font>") {
						if(button_5_hold_off) {
							input "button_5_hold_off", "capability.switch", title: "<b>Turns Off</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_5_hold_off", "capability.switch", title: "Turns Off <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_5_hold_toggle) {
							input "button_5_hold_toggle", "capability.switch", title: "<b>Toggles</b> (if on, turn off; if off, turn on)", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_5_hold_toggle", "capability.switch", title: "Toggles (if on, turn off; if off, turn on) <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_5_hold_on) {
							input "button_5_hold_on", "capability.switch", title: "<b>Turns On</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_5_hold_on", "capability.switch", title: "Turns On <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_5_hold_dim) {
							input "button_5_hold_dim", "capability.switchLevel", title: "<b>Dims</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_5_hold_dim", "capability.switchLevel", title: "Dims <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
						if(button_5_hold_brighten) {
							input "button_5_hold_brighten", "capability.switchLevel", title: "<b>Brightens</b>", multiple: true, required: false, submitOnChange:true
						} else {
							input "button_5_hold_brighten", "capability.switchLevel", title: "Brightens <font color=\"gray\">(Select devices)</font>", multiple: true, required: false, submitOnChange:true
						}
					}
				}


				if(button_1_push_dim || button_1_push_brighten || button_2_push_dim || button_2_push_brighten || button_3_push_dim || button_3_push_brighten || button_4_push_dim || button_4_push_brighten || button_5_push_dim || button_5_push_brighten || button_1_hold_dim || button_1_hold_brighten || button_2_hold_dim || button_2_hold_brighten || button_3_hold_dim || button_3_hold_brighten || button_4_hold_dim || button_4_hold_brighten || button_5_hold_dim || button_5_hold_brighten){
					section(){
						paragraph "<div style=\"background-color:BurlyWood\"><b> Set dim and brighten speed:</b></div>"
						paragraph "Multiplier/divider for dimming and brightening, from 1.01 to 99, where higher is faster. For instance, a value of 2 would double (eg from 25% to 50%, then 100%), whereas a value of 1.5 would increase by half each time (eg from 25% to 38% to 57%)."
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
				if(button_1_push_on || button_1_push_off || button_1_push_dim || button_1_push_brighten || button_1_push_toggle || button_2_push_on || button_2_push_off || button_2_push_dim || button_2_push_brighten || button_2_push_toggle || button_3_push_on || button_3_push_off || button_3_push_dim || button_3_push_brighten || button_3_push_toggle || button_4_push_on || button_4_push_off || button_4_push_dim || button_4_push_brighten || button_4_push_toggle || button_5_push_on || button_5_push_off || button_5_push_dim || button_5_push_brighten || button_5_push_toggle || button_1_hold_on || button_1_hold_off || button_1_hold_dim || button_1_hold_brighten || button_1_hold_toggle || button_2_hold_on || button_2_hold_off || button_2_hold_dim || button_2_hold_brighten || button_2_hold_toggle || button_3_hold_on || button_3_hold_off || button_3_hold_dim || button_3_hold_brighten || button_3_hold_toggle || button_4_hold_on || button_4_hold_off || button_4_hold_dim || button_4_hold_brighten || button_4_hold_toggle || button_5_hold_on || button_5_hold_off || button_5_hold_dim || button_5_hold_brighten || button_5_hold_toggle){
					section(){
						paragraph "<div style=\"background-color:LightCyan\"><b> Click \"Done\" to continue.</b></div>"
					}
				} else {
					section(){
						paragraph "<div style=\"background-color:MistyRose\"><b> No actions selected. Do NOT save.</b></div>"
					}
				}
			}
		}
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

/* ************************************************** */
/* TO DO - see if these can be moved so doesn't need  */
/* to precess at every button click. If in            */
/* Initialize function, will variables carry over?    */
/* Or need to set them as "settings."?                */
/* ************************************************** */
	if(!multiDevice && !advancedSetup){
		button_1_push_on = controlDevice
		if(numButton == "4 button" || numButton == "5 button") button_2_push_brighten = controlDevice
		if(numButton == "5 button") button_3_push_toggle = controlDevice
		if(numButton == "4 button" || numButton == "5 button") button_4_push_dim = controlDevice
		button_5_push_off = controlDevice
	} else if(!multiDevice && advanceSetup){
		if(buttonPush1 && buttonPush1 == "on") button_1_push_on = controlDevice
		if(buttonPush1 && buttonPush1 == "off") button_1_push_off = controlDevice
		if(buttonPush1 && buttonPush1 == "dim") button_1_push_dim = controlDevice
		if(buttonPush1 && buttonPush1 == "brighten") button_1_push_brighten = controlDevice
		if(buttonPush1 && buttonPush1 == "toggle") button_1_push_toggle = controlDevice
		if(numButton == "4 button" || numButton == "5 button"){
			if(buttonPush2 && buttonPush2 == "on") button_2_push_on = controlDevice
			if(buttonPush2 && buttonPush2 == "off") button_2_push_off = controlDevice
			if(buttonPush2 && buttonPush2 == "dim") button_2_push_dim = controlDevice
			if(buttonPush2 && buttonPush2 == "brighten") button_2_push_brighten = controlDevice
			if(buttonPush2 && buttonPush2 == "toggle") button_2_push_toggle = controlDevice
		}
		if(numButton == "5 button"){
			if(buttonPush3 && buttonPush3 == "on") button_3_push_on = controlDevice
			if(buttonPush3 && buttonPush3 == "off") button_3_push_off = controlDevice
			if(buttonPush3 && buttonPush3 == "dim") button_3_push_dim = controlDevice
			if(buttonPush3 && buttonPush3 == "brighten") button_3_push_brighten = controlDevice
			if(buttonPush3 && buttonPush3 == "toggle") button_3_push_toggle = controlDevice
		}
		if(numButton == "4 button" || numButton == "5 button"){
			if(buttonPush4 && buttonPush4 == "on") button_4_push_on = controlDevice
			if(buttonPush4 && buttonPush4 == "off") button_4_push_off = controlDevice
			if(buttonPush4 && buttonPush4 == "dim") button_4_push_dim = controlDevice
			if(buttonPush4 && buttonPush4 == "brighten") button_4_push_brighten = controlDevice
			if(buttonPush4 && buttonPush4 == "toggle") button_4_push_toggle = controlDevice
		}
		if(buttonPush5 && buttonPush5 == "on") button_5_push_on = controlDevice
		if(buttonPush5 && buttonPush5 == "off") button_5_push_off = controlDevice
		if(buttonPush5 && buttonPush5 == "dim") button_5_push_dim = controlDevice
		if(buttonPush5 && buttonPush5 == "brighten") button_5_push_brighten = controlDevice
		if(buttonPush5 && buttonPush5 == "toggle") button_5_push_toggle = controlDevice
	}
		

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

// TO DO - see if these can be moved so doesn't need to precess at every button click
	if(!multiDevice && !advancedSetup && !replicateHold){
		button_1_hold_on = controlDevice
		if(numButton == "4 button" || numButton == "5 button") button_2_hold_brighten = controlDevice
		if(numButton == "5 button") button_3_hold_toggle = controlDevice
		if(numButton == "4 button" || numButton == "5 button") button_4_hold_dim = controlDevice
		button_5_hold_off = controlDevice
	} else if(!multiDevice && advanceSetup && !replicateHold){
		if(buttonPush1 && buttonPush1 == "on") button_1_hold_on = controlDevice
		if(buttonPush1 && buttonPush1 == "off") button_1_hold_off = controlDevice
		if(buttonPush1 && buttonPush1 == "dim") button_1_hold_dim = controlDevice
		if(buttonPush1 && buttonPush1 == "brighten") button_1_hold_brighten = controlDevice
		if(buttonPush1 && buttonPush1 == "toggle") button_1_hold_toggle = controlDevice
		if(numButton == "4 button" || numButton == "5 button"){
			if(buttonPush2 && buttonPush2 == "on") button_2_hold_on = controlDevice
			if(buttonPush2 && buttonPush2 == "off") button_2_hold_off = controlDevice
			if(buttonPush2 && buttonPush2 == "dim") button_2_hold_dim = controlDevice
			if(buttonPush2 && buttonPush2 == "brighten") button_2_hold_brighten = controlDevice
			if(buttonPush2 && buttonPush2 == "toggle") button_2_hold_toggle = controlDevice
		}
		if(numButton == "5 button"){
			if(buttonPush3 && buttonPush3 == "on") button_3_hold_on = controlDevice
			if(buttonPush3 && buttonPush3 == "off") button_3_hold_off = controlDevice
			if(buttonPush3 && buttonPush3 == "dim") button_3_hold_dim = controlDevice
			if(buttonPush3 && buttonPush3 == "brighten") button_3_hold_brighten = controlDevice
			if(buttonPush3 && buttonPush3 == "toggle") button_3_hold_toggle = controlDevice
		}
		if(numButton == "4 button" || numButton == "5 button"){
			if(buttonPush4 && buttonPush4 == "on") button_4_hold_on = controlDevice
			if(buttonPush4 && buttonPush4 == "off") button_4_hold_off = controlDevice
			if(buttonPush4 && buttonPush4 == "dim") button_4_hold_dim = controlDevice
			if(buttonPush4 && buttonPush4 == "brighten") button_4_hold_brighten = controlDevice
			if(buttonPush4 && buttonPush4 == "toggle") button_4_hold_toggle = controlDevice
		}
		if(buttonPush5 && buttonPush5 == "on") button_5_hold_on = controlDevice
		if(buttonPush5 && buttonPush5 == "off") button_5_holdhold_off = controlDevice
		if(buttonPush5 && buttonPush5 == "dim") button_5_hold_dim = controlDevice
		if(buttonPush5 && buttonPush5 == "brighten") button_5_hold_brighten = controlDevice
		if(buttonPush5 && buttonPush5 == "toggle") button_5_hold_toggle = controlDevice
	} else if(!multiDevice && advanceSetup && replicateHold){
		if(buttonHold1 && buttonHold1 == "on") button_1_hold_on = controlDevice
		if(buttonHold1 && buttonHold1 == "off") button_1_hold_off = controlDevice
		if(buttonHold1 && buttonHold1 == "dim") button_1_hold_dim = controlDevice
		if(buttonHold1 && buttonHold1 == "brighten") button_1_hold_brighten = controlDevice
		if(buttonHold1 && buttonHold1 == "toggle") button_1_hold_toggle = controlDevice
		if(numButton == "4 button" || numButton == "5 button"){
			if(buttonHold2 && buttonHold2 == "on") button_2_hold_on = controlDevice
			if(buttonHold2 && buttonHold2 == "off") button_2_hold_off = controlDevice
			if(buttonHold2 && buttonHold2 == "dim") button_2_hold_dim = controlDevice
			if(buttonHold2 && buttonHold2 == "brighten") button_2_hold_brighten = controlDevice
			if(buttonHold2 && buttonHold2 == "toggle") button_2_hold_toggle = controlDevice
		}
		if(numButton == "5 button"){
			if(buttonHold3 && buttonHold3 == "on") button_3_hold_on = controlDevice
			if(buttonHold3 && buttonHold3 == "off") button_3_hold_off = controlDevice
			if(buttonHold3 && buttonHold3 == "dim") button_3_hold_dim = controlDevice
			if(buttonHold3 && buttonHold3 == "brighten") button_3_hold_brighten = controlDevice
			if(buttonHold3 && buttonHold3 == "toggle") button_3_hold_toggle = controlDevice
		}
		if(numButton == "4 button" || numButton == "5 button"){
			if(buttonHold4 && buttonHold4 == "on") button_4_hold_on = controlDevice
			if(buttonHold4 && buttonHold4 == "off") button_4_hold_off = controlDevice
			if(buttonHold4 && buttonHold4 == "dim") button_4_hold_dim = controlDevice
			if(buttonHold4 && buttonHold4 == "brighten") button_4_hold_brighten = controlDevice
			if(buttonHold4 && buttonHold4 == "toggle") button_4_hold_toggle = controlDevice
		}
		if(buttonHold5 && buttonHold5 == "on") button_5_hold_on = controlDevice
		if(buttonHold5 && buttonHold5 == "off") button_5_hold_off = controlDevice
		if(buttonHold5 && buttonHold5 == "dim") button_5_hold_dim = controlDevice
		if(buttonHold5 && buttonHold5 == "brighten") button_5_hold_brighten = controlDevice
		if(buttonHold5 && buttonHold5 == "toggle") button_5_hold_toggle = controlDevice
	} else if(multiDevice && !replicateHold){
		if(button_1_push_on) button_1_hold_on = button_1_push_on
		if(button_2_push_on) button_2_hold_on = button_2_push_on
		if(button_3_push_on) button_3_hold_on = button_3_push_on
		if(button_4_push_on) button_4_hold_on = button_4_push_on
		if(button_5_push_on) button_5_hold_on = button_5_push_on
		
		if(button_1_push_brighten) button_1_hold_brighten = button_1_push_brighten
		if(button_2_push_brighten) button_2_hold_brighten = button_2_push_brighten
		if(button_3_push_brighten) button_3_hold_brighten = button_3_push_brighten
		if(button_4_push_brighten) button_4_hold_brighten = button_4_push_brighten
		if(button_5_push_brighten) button_5_hold_brighten = button_5_push_brighten

		if(button_1_push_toggle) button_1_hold_toggle = button_1_push_toggle
		if(button_2_push_toggle) button_2_hold_toggle = button_2_push_toggle
		if(button_3_push_toggle) button_3_hold_toggle = button_3_push_toggle
		if(button_4_push_toggle) button_4_hold_toggle = button_4_push_toggle
		if(button_5_push_toggle) button_5_hold_toggle = button_5_push_toggle
		
		if(button_1_push_dim) button_1_hold_dim = button_1_push_dim
		if(button_2_push_dim) button_2_hold_dim = button_2_push_dim
		if(button_3_push_dim) button_3_hold_dim = button_3_push_dim
		if(button_4_push_dim) button_4_hold_dim = button_4_push_dim
		if(button_5_push_dim) button_5_hold_dim = button_5_push_dim
		
		if(button_1_push_off) button_1_hold_off = button_1_push_off
		if(button_2_push_off) button_2_hold_off = button_2_push_off
		if(button_3_push_off) button_3_hold_off = button_3_push_off
		if(button_4_push_off) button_4_hold_off = button_4_push_off
		if(button_5_push_off) button_5_hold_off = button_5_push_off
	}

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

def logTrace(message){
	if(state.debug) log.trace message
}
