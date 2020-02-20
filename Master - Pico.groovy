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
*  Version: 0.4.19
*
***********************************************************************************************************************/
//Select device to control, then switch to "map buttons to other actions" = 500 error
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

/* ************************************************************************ */
/* TO-DO: BUG! During setup, if a device is selected for all buttons, then  */
/* switch to "map "map buttons to other actions", it causes a 500 error.    */
/* ************************************************************************ */
/* TO-DO: Add option for to set a Pico button to disable contact or         */
/* schedule?                                                                */
/* ************************************************************************ */
/* ************************************************************************ */
/* TO-DO: Add locks? Maybe like if turning on the porch light equals        */
/* unlock...?                                                               */
/* ************************************************************************ */
/* ************************************************************************ */
/* TO-DO: Add media device control. Volume increase/decrease, changing      */
/*  channels (skip, pause, next, etc?)                                      */
/* ************************************************************************ */
/* ************************************************************************ */
/* TO-DO: With growing number of options, add selection for which actions   */
/* to display. Perhaps even allow entering custom actions? Would there be   */
/* any advantage over rule machine or button maker or whatever??            */
/* ************************************************************************ */
/* ************************************************************************ */
/* TO-DO: Add auto-pause/disabling of schedules when dimming and            */
/* and brightening. Will need to know id of device being dimmed, and set a  */
/* state variable. But the state variable will need to be global. Can that  */
/* be done? If setting in Master, will it apply to all child apps??         */
/* ************************************************************************ */
preferences {
    infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
    warningIcon = "<img src=\"http://emily-john.love/icons/warning.png\" width=20 height=20>"
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
                                displayMultiplierOption()
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

                    /* ************************************************************************ */
                    /* TO-DO: Put these inputs in a function.                                   */
                    /* ************************************************************************ */
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
                        input "button_5_hold_off", "capability.switch", title: "Holding Bottom (\"Off\") button turns off?", multiple: true, submitOnChange:true
                    }

                    if(button_2_push_brighten || button_4_push_dim || button_2_hold_brighten || button_4_hold_dim){
                        displayMultiplierOption()
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
                    if(!button_1_push_on && !button_1_push_off && !button_1_push_dim && !button_1_push_brighten && !button_1_push_toggle && !button_1_push_resume) {
                        hidden = true
                        text = "None selected - Click to expand"
                    } else {
                        hidden = false
                        text = "Click to expand/collapse"
                    }

                    /* ************************************************************************ */
                    /* TO-DO: Add warning messages for if dim/brightening and also turning on,  */
                    /* toggling or resuming the same device (remove error for dim/brighten with */
                    /* resume). Add same to MagicCube.                                          */
                    /* ************************************************************************ */
                    section(hideable: true, hidden: hidden, "Top button (\"On\") <font color=\"gray\">($text)</font>") {
                        button = [1,"on","push"]
                        getAdvancedSwitchInput(button)

                        button = [1,"off","push"]
                        getAdvancedSwitchInput(button)
                        errorMessage(compareDeviceLists(button,"on"))

                        button = [1,"resume","push"]
                        getAdvancedSwitchInput(button)
                        errorMessage(compareDeviceLists(button,"on"))
                        errorMessage(compareDeviceLists(button,"off"))

                        button = [1,"toggle","push"]
                        getAdvancedSwitchInput(button)
                        errorMessage(compareDeviceLists(button,"on"))
                        errorMessage(compareDeviceLists(button,"off"))
                        errorMessage(compareDeviceLists(button,"resume"))

                        button = [1,"dim","push"]
                        getAdvancedSwitchInput(button)
                        errorMessage(compareDeviceLists(button,"off"))
                        errorMessage(compareDeviceLists(button,"resume"))

                        button = [1,"brighten","push"]
                        getAdvancedSwitchInput(button)
                        errorMessage(compareDeviceLists(button,"off"))
                        errorMessage(compareDeviceLists(button,"dim"))
                        errorMessage(compareDeviceLists(button,"resume"))
                    }
                    //paragraph error
                    if((numButton == "4 button" || numButton == "5 button") && !error){
                        if(!button_2_push_on && !button_2_push_off && !button_2_push_dim && !button_2_push_brighten && !button_2_push_toggle && !button_2_push_resume) {
                            hidden = true
                            text = "None selected - Click to expand"
                        } else {
                            hidden = false
                            text = "Click to expand/collapse"
                        }

                        section(hideable: true, hidden: hidden, "\"Brighten\" Button <font color=\"gray\">($text)</font>") {
                            button = [2,"brighten","push"]
                            getAdvancedSwitchInput(button)

                            button = [2,"dim","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"off"))
                            errorMessage(compareDeviceLists(button,"brighten"))

                            button = [2,"on","push"]
                            getAdvancedSwitchInput(button)

                            button = [2,"off","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"brighten"))
                            errorMessage(compareDeviceLists(button,"dim"))
                            errorMessage(compareDeviceLists(button,"on"))

                            button = [2,"toggle","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"on"))
                            errorMessage(compareDeviceLists(button,"off"))

                            button = [2,"resume","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"brighten"))
                            errorMessage(compareDeviceLists(button,"dim"))
                            errorMessage(compareDeviceLists(button,"on"))
                            errorMessage(compareDeviceLists(button,"off"))
                            errorMessage(compareDeviceLists(button,"toggle"))
                        }
                    }
                    if(numButton == "5 button" && !error){
                        if(!button_3_push_on && !button_3_push_off && !button_3_push_dim && !button_3_push_brighten && !button_3_push_toggle && !button_3_push_resume) {
                            hidden = true
                            text = "None selected - Click to expand"
                        } else {
                            hidden = false
                            text = "Click to expand/collapse"
                        }
                        section(hideable: true, hidden: hidden, "Middle Button <font color=\"gray\">($text)</font>") {
                            button = [3,"toggle","push"]
                            getAdvancedSwitchInput(button)

                            button = [3,"resume","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"toggle"))

                            button = [3,"on","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"toggle"))
                            errorMessage(compareDeviceLists(button,"resume"))

                            button = [3,"off","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"on"))
                            errorMessage(compareDeviceLists(button,"toggle"))
                            errorMessage(compareDeviceLists(button,"resume"))

                            button = [3,"dim","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"off"))
                            errorMessage(compareDeviceLists(button,"resume"))

                            button = [3,"brighten","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"off"))
                            errorMessage(compareDeviceLists(button,"dim"))
                            errorMessage(compareDeviceLists(button,"resume"))
                        }
                    }
                    if((numButton == "4 button" || numButton == "5 button") && !error){
                        if(!button_4_push_on && !button_4_push_off && !button_4_push_dim && !button_4_push_brighten && !button_4_push_toggle && !button_4_push_resume) {
                            hidden = true
                            text = "None selected - Click to expand"
                        } else {
                            hidden = false
                            text = "Click to expand/collapse"
                        }
                        section(hideable: true, hidden: hidden, "\"Dim\" Button <font color=\"gray\">($text)</font>") {
                            button = [4,"dim","push"]
                            getAdvancedSwitchInput(button)

                            button = [4,"brighten","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"dim"))

                            button = [4,"on","push"]
                            getAdvancedSwitchInput(button)

                            button = [4,"off","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"dim"))
                            errorMessage(compareDeviceLists(button,"brighten"))
                            errorMessage(compareDeviceLists(button,"on"))

                            button = [4,"toggle","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"on"))
                            errorMessage(compareDeviceLists(button,"off"))

                            button = [4,"resume","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"dim"))
                            errorMessage(compareDeviceLists(button,"brighten"))
                            errorMessage(compareDeviceLists(button,"on"))
                            errorMessage(compareDeviceLists(button,"off"))
                            errorMessage(compareDeviceLists(button,"toggle"))
                        }
                    }

                    if(!error){
                        if(!button_5_push_on && !button_5_push_off && !button_5_push_dim && !button_5_push_brighten && !button_5_push_toggle && !button_5_push_resume) {
                            hidden = true
                            text = "None selected - Click to expand"
                        } else {
                            hidden = false
                            text = "Click to expand/collapse"
                        }
                        section(hideable: true, hidden: hidden, "Bottom Button (\"Off\") <font color=\"gray\">($text)</font>") {
                            button = [5,"off","push"]
                            getAdvancedSwitchInput(button)

                            button = [5,"resume","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"off"))

                            button = [5,"on","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"off"))
                            errorMessage(compareDeviceLists(button,"resume"))

                            button = [5,"toggle","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"off"))
                            errorMessage(compareDeviceLists(button,"on"))
                            errorMessage(compareDeviceLists(button,"resume"))

                            button = [5,"dim","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"off"))
                            errorMessage(compareDeviceLists(button,"resume"))

                            button = [5,"brighten","push"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"off"))
                            errorMessage(compareDeviceLists(button,"dim"))
                            errorMessage(compareDeviceLists(button,"resume"))
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
                        if(!button_1_hold_on && !button_1_hold_off && !button_1_hold_dim && !button_1_hold_brighten && !button_1_hold_toggle && !button_1_hold_resume) {
                            hidden = true
                            text = "None selected - Click to expand"
                        } else {
                            hidden = false
                            text = "Click to expand/collapse"
                        }
                        section(hideable: true, hidden: hidden, "Top button (\"On\") <font color=\"gray\">($text)</font>") {
                            button = [1,"on","hold"]
                            getAdvancedSwitchInput(button)

                            button = [1,"off","hold"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"on"))

                            button = [1,"resume","hold"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"on"))
                            errorMessage(compareDeviceLists(button,"off"))

                            button = [1,"toggle","hold"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"on"))
                            errorMessage(compareDeviceLists(button,"off"))
                            errorMessage(compareDeviceLists(button,"resume"))

                            button = [1,"dim","hold"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"off"))
                            errorMessage(compareDeviceLists(button,"resume"))

                            button = [1,"brighten","hold"]
                            getAdvancedSwitchInput(button)
                            errorMessage(compareDeviceLists(button,"off"))
                            errorMessage(compareDeviceLists(button,"dim"))
                            errorMessage(compareDeviceLists(button,"resume"))
                        }
                        if((numButton == "4 button" || numButton == "5 button")  && !error){
                            if(!button_2_hold_on && !button_2_hold_off && !button_2_hold_dim && !button_2_hold_brighten && !button_2_hold_toggle && !button_2_hold_resume) {
                                hidden = true
                                text = "None selected - Click to expand"
                            } else {
                                hidden = false
                                text = "Click to expand/collapse"
                            }
                            section(hideable: true, hidden: hidden, "\"Brighten\" Button <font color=\"gray\">($text)</font>") {
                                button = [2,"brighten","hold"]
                                getAdvancedSwitchInput(button)

                                button = [2,"dim","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"off"))
                                errorMessage(compareDeviceLists(button,"brighten"))

                                button = [2,"on","hold"]
                                getAdvancedSwitchInput(button)

                                button = [2,"off","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"brighten"))
                                errorMessage(compareDeviceLists(button,"dim"))
                                errorMessage(compareDeviceLists(button,"on"))

                                button = [2,"toggle","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"on"))
                                errorMessage(compareDeviceLists(button,"off"))

                                button = [2,"resume","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"brighten"))
                                errorMessage(compareDeviceLists(button,"dim"))
                                errorMessage(compareDeviceLists(button,"on"))
                                errorMessage(compareDeviceLists(button,"off"))
                                errorMessage(compareDeviceLists(button,"toggle"))
                            }
                        }
                        if(numButton == "5 button" && !error){
                            if(!button_3_hold_on && !button_3_hold_off && !button_3_hold_dim && !button_3_hold_brighten && !button_3_hold_toggle && !button_3_hold_resume) {
                                hidden = true
                                text = "None selected - Click to expand"
                            } else {
                                hidden = false
                                text = "Click to expand/collapse"
                            }
                            section(hideable: true, hidden: hidden, "Middle Button <font color=\"gray\">($text)</font>") {
                                button = [3,"toggle","hold"]
                                getAdvancedSwitchInput(button)

                                button = [3,"resume","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"toggle"))

                                button = [3,"on","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"toggle"))
                                errorMessage(compareDeviceLists(button,"resume"))

                                button = [3,"off","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"on"))
                                errorMessage(compareDeviceLists(button,"toggle"))
                                errorMessage(compareDeviceLists(button,"resume"))

                                button = [3,"dim","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"off"))
                                errorMessage(compareDeviceLists(button,"resume"))

                                button = [3,"brighten","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"off"))
                                errorMessage(compareDeviceLists(button,"dim"))
                                errorMessage(compareDeviceLists(button,"resume"))
                            }
                        }
                        if((numButton == "4 button" || numButton == "5 button")  && !error){
                            if(!button_4_hold_on && !button_4_hold_off && !button_4_hold_dim && !button_4_hold_brighten && !button_4_hold_toggle && !button_4_hold_resume) {
                                hidden = true
                                text = "None selected - Click to expand"
                            } else {
                                hidden = false
                                text = "Click to expand/collapse"
                            }

                            section(hideable: true, hidden: hidden, "\"Dim\" Button <font color=\"gray\">($text)</font>") {
                                button = [4,"dim","hold"]
                                getAdvancedSwitchInput(button)

                                button = [4,"brighten","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"dim"))

                                button = [4,"on","hold"]
                                getAdvancedSwitchInput(button)

                                button = [4,"off","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"dim"))
                                errorMessage(compareDeviceLists(button,"brighten"))
                                errorMessage(compareDeviceLists(button,"on"))

                                button = [4,"toggle","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"on"))
                                errorMessage(compareDeviceLists(button,"off"))

                                button = [4,"resume","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"dim"))
                                errorMessage(compareDeviceLists(button,"brighten"))
                                errorMessage(compareDeviceLists(button,"on"))
                                errorMessage(compareDeviceLists(button,"off"))
                                errorMessage(compareDeviceLists(button,"toggle"))
                            }
                        }

                        if(!error){
                            if(!button_5_hold_on && !button_5_hold_off && !button_5_hold_dim && !button_5_hold_brighten && !button_5_hold_toggle && !button_5_hold_resume) {
                                hidden = true
                                text = "None selected - Click to expand"
                            } else {
                                hidden = false
                                text = "Click to expand/collapse"
                            }
                            section(hideable: true, hidden: hidden, "Bottom Button (\"Off\") <font color=\"gray\">($text)</font>") {
                                button = [5,"off","hold"]
                                getAdvancedSwitchInput(button)

                                button = [5,"resume","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"off"))

                                button = [5,"on","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"off"))
                                errorMessage(compareDeviceLists(button,"resume"))

                                button = [5,"toggle","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"off"))
                                errorMessage(compareDeviceLists(button,"on"))
                                errorMessage(compareDeviceLists(button,"resume"))

                                button = [5,"dim","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"off"))
                                errorMessage(compareDeviceLists(button,"resume"))

                                button = [5,"brighten","hold"]
                                getAdvancedSwitchInput(button)
                                errorMessage(compareDeviceLists(button,"off"))
                                errorMessage(compareDeviceLists(button,"dim"))
                                errorMessage(compareDeviceLists(button,"resume"))
                            }
                        }
                    }

                    if(!error && (button_1_push_dim || button_1_push_brighten || button_2_push_dim || button_2_push_brighten || button_3_push_dim || button_3_push_brighten || button_4_push_dim || button_4_push_brighten || button_5_push_dim || button_5_push_brighten || button_1_hold_dim || button_1_hold_brighten || button_2_hold_dim || button_2_hold_brighten || button_3_hold_dim || button_3_hold_brighten || button_4_hold_dim || button_4_hold_brighten || button_5_hold_dim || button_5_hold_brighten)){
                        section(){
                            displayMultiplierOption(true)
                        }
                    }
                    section(){
                        if(error) paragraph "$error</div>"
                    }
                }
            }
        }
    }
}

// Display functions

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
    displayLabel("Set name for this Pico setup")
    label title: "", required: true, submitOnChange:true
    if(!app.label) displayInfo("Name this Pico setup. Each Pico setup must have a unique name.")
    /* ************************************************************************ */
    /* TO-DO: Need to test if the app name is unique. BUT we can't call the     */
    /* parent app during setup. So we need to test this during initialize...    */
    /* and what? Auto-rename it? Maybe better than it not working right.        */
    /* Alternatively, we could switch everything over to app.id o.o             */
    /* ************************************************************************ */
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


def getMultiDeviceInputOption(){
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

// values sent as list
// values.0 = number
// values.1 = on/off/toggle/dim/brighten
// values.2 = push/hold
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
    input "button_" + values[0] + "_" + values[2] + "_" + values[1], "capability.$switchType", title: "$text", multiple: true, submitOnChange:true
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

/* ************************************************************************ */
/* TO-DO: Convert from a "multiplier" to using a "percentage", for user     */
/* ease.                                                                    */
/* ************************************************************************ */
def displayMultiplierOption(hold = false){
    displayLabel("Set dim and brighten speed")
    displayMultiplierMessage()
    if(hold){
        if(button_1_push_dim || button_1_push_brighten || button_2_push_dim || button_2_push_brighten || button_3_push_dim || button_3_push_brighten || button_4_push_dim || button_4_push_brighten || button_5_push_dim || button_5_push_brighten){
            if(button_1_hold_dim || button_1_hold_brighten || button_2_hold_dim || button_2_hold_brighten || button_3_hold_dim || button_3_hold_brighten || button_4_hold_dim || button_4_hold_brighten || button_5_hold_dim || button_5_hold_brighten){
                input "pushMultiplier", "decimal", title: "<b>Push multiplier.</b> (Optional. Default 1.2.)", width: 6
                input "holdMultiplier", "decimal", title: "<b>Hold multiplier.</b> (Optional. Default 1.4.)", width: 6
            } else {
                input "pushMultiplier", "decimal", title: "<b>Push multiplier.</b> (Optional. Default 1.2.)", width: 12
            }
        } else if(button_1_hold_dim || button_1_hold_brighten || button_2_hold_dim || button_2_hold_brighten || button_3_hold_dim || button_3_hold_brighten || button_4_hold_dim || button_4_hold_brighten || button_5_hold_dim || button_5_hold_brighten){
            input "holdMultiplier", "decimal", title: "<b>Hold multiplier.</b> (Optional. Default 1.4.)", width: 12
        }
    } else {
        if(button_1_hold_dim || button_1_hold_brighten || button_2_hold_dim || button_2_hold_brighten || button_3_hold_dim || button_3_hold_brighten || button_4_hold_dim || button_4_hold_brighten || button_5_hold_dim || button_5_hold_brighten){
            input "pushMultiplier", "decimal", title: "Multiplier? (Optional. Default 1.2.)", width: 6
        } else {
            input "pushMultiplier", "decimal", title: "Multiplier? (Optional. Default 1.2.)", width: 12
        }
    }
}

def displayMultiplierMessage(){
    displayInfo("Multiplier/divider for dimming and brightening, from 1.01 to 99, where higher is faster. For instance, a value of 2 would double (eg from 25% to 50%, then 100%), whereas a value of 1.5 would increase by half each time (eg from 25% to 38% to 57%).")
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

/* ************************************************************************ */
/*                                                                          */
/* End display functions.                                                   */
/*                                                                          */
/* ************************************************************************ */

def installed() {
    logTrace(830, "Installed","trace")
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    logTrace(836,"Updated","trace")
    unsubscribe()
    initialize()
}

def initialize() {
    logTrace(842,"Initialized","trace")

    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

    subscribe(buttonDevice, "pushed", buttonPushed)
    subscribe(buttonDevice, "held", buttonPushed)
    subscribe(buttonDevice, "released", buttonReleased)

    // Treat 2nd button of 2-button Pico as "off" (eg button 5)
}

def buttonPushed(evt){
    /* ************************************************************************ */
    /* TO-DO: Merge buttonPushed and buttonHeld functions. The only differences */
    /* are the variable names, dim/brighten functions, and log messages         */
    /* ************************************************************************ */
    buttonNumber = evt.value

    // Needs to be state since we're passing back and forth to parent for progressive dim and brightening
    if(evt.name == "pushed") {
        atomicState.action = "push"
    } else {
        atomicState.action = "held"
    }
    def colorSwitch
    def whiteSwitch

    if(settings["${atomicState.action}Multiplier"]) settings["${atomicState.action}Multiplier"] = parent.validateMultiplier(settings["${atomicState.action}Multiplier"],app.label)

    // Treat 2nd button of 2-button Pico as "off" (eg button 5)
    if(buttonNumber == "2" &&  numButton == "2 button") buttonNumber = 5

    // Simple setup
    if(!multiDevice && !advancedSetup){
        switchButtonNumber = buttonNumber
    } else if(!multiDevice && advancedSetup && !replicateHold){
        switchButtonNumber = settings["buttonPush${buttonNumber}"]
    } else if(!multiDevice && advancedSetup && replicateHold && atomicState.action == "held"){
        switchButtonNumber = settings["buttonHold${buttonNumber}"]
    }
    if(switchButtonNumber){
        switch(switchButtonNumber){
            case "1": multiOn("on",controlDevice)
            message = "turning on"
            break
            case "2": 
            if(atomicState.action == "push"){
                parent.dim("brighten",controlDevice,app.label)
            } else {
                holdBrighten(controlDevice,app.label)
            }
            message = "brightening"
            break
            case "3": multiOn("toggle",controlDevice)
            message = "toggling"
            break
            case "4": 
            if(atomicState.action == "push"){
                parent.dim("dim",controlDevice,app.label)
            } else {
                holdDim(controlDevice,app.label)
            }
            message = "dimming"
            break
            case "5": multiOn("off",controlDevice)
            message = "turning off"
        }
        logTrace(909,"Button $buttonNumber of $buttonDevice pushed for $controlDevice; default setup; $atomicState.action","trace")
        // if(multiDevice && (!advancedSetup || advancedSetup))
    } else {
        if(atomicState.action_toggle == "push"){
            actionText = "pushed"
        } else {
            actionText = "held"
        }
        if(settings["button_${buttonNumber}_${atomicState.action_toggle}"] != null) {
            logTrace(918,"Button $buttonNumber of $buttonDevice $actionText for " + settings["button_${buttonNumber}_${atomicState.action_toggle}_toggle"] + "; remapped and advanced setup; toggling","trace")
            if (settings.color == "Separate"){
                toggleSeparate(settings["button_${buttonNumber}_${atomicState.action_toggle}_toggle"])
            } else {
                multiOn("toggle",settings["button_${buttonNumber}_${atomicState.action_toggle}_toggle"])
            }
        }
        if(settings["button_${buttonNumber}_${atomicState.action_toggle}_on"]) {
            logTrace(926,"Button $buttonNumber of $buttonDevice $actionText for " + settings["button_${buttonNumber}_${atomicState.action_toggle}_on"] +"; remapped and advanced setup; turning on","trace")
            multiOn("on",settings["button_${buttonNumber}_${atomicState.action_toggle}_on"])
        }
        if(settings["button_${buttonNumber}_${atomicState.action_toggle}_off"]){
            logTrace(930,"Button $buttonNumber of $buttonDevice $actionText for " + settings["button_${buttonNumber}_${atomicState.action_toggle}_off"] + "; remapped and advanced setup; turning off","trace")
            multiOn("off",settings["button_${buttonNumber}_${atomicState.action_toggle}_off"])
        }
        if(settings["button_${buttonNumber}_${atomicState.action_toggle}_dim"]) {
            logTrace(934,"Button $buttonNumber of $buttonDevice $actionText for " + settings["button_${buttonNumber}_${atomicState.action_toggle}_dim"] + "; remapped and advanced setup; dimming","trace")
            if(atomicState.action_toggle == "push"){
                parent.dim("dim",settings["button_${buttonNumber}_push_dim"],app.label)
            } else {
                holdDim(settings["button_${buttonNumber}_hold_dim"])
            }
        }
        if(settings["button_${buttonNumber}_${atomicState.action_toggle}_brighten"]) {
            logTrace(942,"Button $buttonNumber of $buttonDevice $actionText for " + settings["button_${buttonNumber}_${atomicState.action_toggle}_brighten"] + "; remapped and advanced setup; brightening","trace")
            if(atomicState.action_toggle == "push"){
                parent.dim("brighten",settings["button_${buttonNumber}_${atomicState.action_toggle}_brighten"],app.label)
            } else {
                holdBrighten(settings["button_${buttonNumber}_hold_brighten"])
            }
        }
        if(settings["button_${buttonNumber}_${atomicState.action_toggle}_resume"]) {
            logTrace(950,"Button $buttonNumber of $buttonDevice $actionText for " + settings["button_${buttonNumber}_${atomicState.action_toggle}_resume"] + "; remapped and advanced setup; brightening","trace")
            multiOn("resume",settings["button_${buttonNumber}_${atomicState.action_toggle}_resume"])
        }
    }
}

def buttonReleased(evt){
    buttonNumber = evt.value
    if (buttonNumber == "2" || (buttonNumber == "4" && (settings.numButton == "4 button" || settings.numButton == "5 button")) || (buttonNumber == "1" && settings.numButton == "2 button")){
        logTrace(959,"Button $buttonNumber of $buttonDevice released, unscheduling all","trace")
        unschedule()
    }
}

/* ************************************************************************ */
/* TO-DO: Retest "multiplier" functionality (also in MagicCube). Make sure  */
/* it's implemented in the UI, and it carries through in the logic. Also    */
/* rename the variables. "Multiplier" is just stupid. And change it to a    */
/* "percentage" for user ease.                                              */
/* ************************************************************************ */
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
        logTrace(986,"Invalid value for action \"$action\" sent to getSteps function","error")
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
    logTrace(1009,"Function getSteps returning $steps","debug")
    return steps
}

// This is the schedule function that sets the level for progressive dimming
def runSetProgressiveLevel(data){
    /* ************************************************************************ */
    /* TO-DO: Use a 1 to 5 loop here                                            */
    /* ************************************************************************ */
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
        logTrace(1067,"Function runSetProgressiveLevel returning (no matching device)","trace")
        return
    }
    parent.setLevelSingle(data.level,null,null,null,device,app.label)
    // Why reschedule?
    //parent.reschedule(device)
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
            // If dimming a light that's off, turn it on
            // setStateSingle does that by setting to 1% level, which is what we want
        } else if(!parent.isOn(it,app.label)){
            //parent.setLevelSingle(1,null,null,null,device,app.label)
            parent.setStateSingle("on",it,childLabel)
            // Since we're turning it on, reschedule it
            parent.reschedule(it,app.label)
        } else {
            if(level < 2){
                logTrace(1114,"Can't dim $it; already 1%.","info")
            } else {
                def steps = getSteps(level, "dim")
                def newLevel

                for(def i = 1; i <= steps; i++) {
                    newLevel = parent.nextLevel(level, "dim",app.getId())
                    runInMillis(i*750,runSetProgressiveLevel, [overwrite: false, data: [device: it.id, level: newLevel]])
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
            // If brightening a light that's off, turn it on at 1%
        } else if(!parent.isOn(it,app.label)){
            //parent.setLevelSingle(1,null,null,null,device,app.label)
            parent.setStateSingle("on",it,childLabel)
            reschedule(it)
        } else {
            if(level > 99){
                logTrace(1142,"Can't brighten $it; already 100%.","info")
            } else {
                def steps = getSteps(level, "brighten")
                def newLevel

                for(def i = 1; i <= steps; i++) {
                    newLevel = parent.nextLevel(level, "brighten",app.getId())
                    runInMillis(i*750,runSetProgressiveLevel, [overwrite: false, data: [device: it.id, level: newLevel]])
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

// Gets app levels, either user defined (currently, contacts) or per-app defaults (currently, none of them)
// Function must be included in all apps that use MultiOn
def getOverrideLevels(defaults,appAction = null){
    /* ************************************************************************ */
    /* TO-DO: Add brightness, temp, etc., options into UI.                      */
    /* ************************************************************************ */
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
        logTrace(1213,"Invalid deviceAction \"$deviceAction\" sent to multiOn","error")
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
        logTrace(1231,"Device id's turned on are $atomicState.deviceChange","debug")

        // Turn on devices
        parent.setStateMulti("on",device,app.label)
        // Get and set defaults levels for each device
        device.each{
            // If defaults, then there's an active schedule
            // So use it for if overriding/reenabling
            defaults = parent.getScheduleDefaultSingle(it,app.label)
            logMessage = defaults ? "Device is scheduled for $defaults" : "Device has no scheduled default levels"
            logTrace(1241,logMessage,"debug")

            defaults = getOverrideLevels(defaults,appAction)
            logMessage = defaults ? "With " + app.label + " overrides, using $defaults": "With no override levels"
            logTrace(1245,logMessage,"debug")

            // Set default levels, for level and temp, if no scheduled defaults (don't need to do for "resume")
            defaults = parent.getDefaultSingle(defaults,app.label)
            logTrace(1249,"With generic defaults, using $defaults","debug")
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
        logTrace(1278,"Device id's turned on are $atomicState.deviceChange","debug")
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
                logTrace(1314,"Scheduled defaults are $defaults","debug")

                defaults = getOverrideLevels(defaults,appAction)
                logTrace(1317,"With " + app.label + " overrides, using $defaults","debug")

                parent.setLevelSingle(defaults.level,defaults.temp,defaults.hue,defaults.sat,it,app.label)
                // Set default level
                if(!defaults){
                    logTrace(1322,"No schedule to resume for $it; turning off","trace")
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
