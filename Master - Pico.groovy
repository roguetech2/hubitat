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
*  Version: 0.5.06
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

// logLevel sets number of log messages
// 0 for none
// 1 for errors only
// 5 for all
def getLogLevel(){
    return 5
}

/* ************************************************************************ */
/* TO-DO: Add option for to set a Pico button to disable contact or         */
/* schedule?                                                                */
/* ************************************************************************ */
/* TO-DO: Add locks? Maybe like if turning on the porch light equals        */
/* unlock...?                                                               */
/* ************************************************************************ */
/* TO-DO: Add media device control. Volume increase/decrease, changing      */
/*  channels (skip, pause, next, etc?)                                      */
/* ************************************************************************ */
/* TO-DO: With enough options, add selection for which actions to display.  */
/* "On" and "off" are pretty universal, but could have a series of bool     */
/* choices for whether dimming, volume, lock, etc. are presented. Perhaps   */
/* even allow entering custom actions? Would there be any advantage over    */
/* rule machine or button maker or whatever??                               */
/* ************************************************************************ */
/* TO-DO: Add auto-pause/disabling of schedules when dimming and            */
/* and brightening. Will need to know id of device being dimmed, and set a  */
/* state variable. But the state variable will need to be global. Can that  */
/* be done? If setting in Master, will it apply to all child apps??         */
/* ************************************************************************ */
/* TO-DO: Add warnings for weird combinations of actions, like dimming and  */
/* turning on a device.                                                     */
/* ************************************************************************ */
preferences {
    infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
    warningIcon = "<img src=\"http://emily-john.love/icons/warning.png\" width=20 height=20>"
    errorIcon = "<img src=\"http://emily-john.love/icons/error.png\" width=20 height=20>"
    moreOptions = ' <font color="gray">(more options)</font>'
    if(!settings) settings = [:]

    install = testNoErrors()
    
    if(app.label && buttonDevice && numberOfButtons) {
        install = "true"
    } else {
        install = "false"
    }
    if(!multiDevice) {
        if(advancedSetup && !buttonPush1 && !buttonPush2 && !buttonPush3 && !buttonPush4 && !buttonPush5) install = false
        if(!advancedSetup && !controlDevice) {
            install = "false"
        } else {
            install = "true"
        }
    } else {
        install = "true"
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

                        if(numberOfButtons){
                            displayMultiDeviceOption()
                            displayAdvancedSetupOption()

                            if(controlDevice){
                                if(advancedSetup){
                                    getMultiDeviceInputOption()
                                } else {
                                    if(!replicateHold){
                                        paragraph "<div style=\"background-color:GhostWhite\"> Pushing Top button (\"On\") turns on.</div>"
                                        if(numberOfButtons == 4 || numberOfButtons == 5) paragraph "<div style=\"background-color:GhostWhite\"> Pushing \"Brighten\" button brightens.</div>"
                                        if(numberOfButtons == 5) paragraph "<div style=\"background-color:GhostWhite\"> Pushing Center button does <b>nothing</b>.</div>"
                                        if(numberOfButtons == 4 || numberOfButtons == 5) paragraph "<div style=\"background-color:GhostWhite\"> Pushing \"Dim\" dims.</div>"
                                        paragraph "<div style=\"background-color:GhostWhite\"> Pushing Bottom button (\"Off\") turns off.</div>"
                                    } else {
                                        paragraph "<div style=\"background-color:GhostWhite\"> Pushing or Holding Top button (\"On\") turns on.</div>"
                                        if(numberOfButtons == 4 || numberOfButtons == 5) paragraph "<div style=\"background-color:GhostWhite\"> Pushing or Holding \"Brighten\" button brightens.</div>"
                                        if(numberOfButtons == 5) paragraph "<div style=\"background-color:GhostWhite\"> Pushing or Holding Center button does <b>nothing</b>.</div>"
                                        if(numberOfButtons == 4 || numberOfButtons == 5) paragraph "<div style=\"background-color:GhostWhite\"> Pushing or Holding \"Dim\" dims.</div>"
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
                    if(app.label && buttonDevice && numberOfButtons) displayAdvancedSetupOption()
                    if(app.label && buttonDevice && numberOfButtons) displayMultiDeviceOption()

                    paragraph "<div style=\"background-color:BurlyWood\"><b> Select what to do for each Pico action:</b></div>"

                    /* ************************************************************************ */
                    /* TO-DO: Put these inputs in a function.                                   */
                    /* ************************************************************************ */
                    if(!replicateHold){
                        input "button_1_push_on", "capability.switch", title: "Top \"On\" button turns on?", multiple: true, submitOnChange:true
                        if(numberOfButtons == 4 || numberOfButtons == 5){
                            input "button_2_push_brighten", "capability.switchLevel", title: "\"Brighten\" button brightens?", multiple: true, submitOnChange:true
                        }
                        if(numberOfButtons == 5){
                            input "button_3_push_toggle", "capability.switch", title: "Center button toggles? (if on, turn off; if off, turn on)", multiple: true, submitOnChange:true
                        }
                        if(numberOfButtons == 4 || numberOfButtons == 5){
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
                        if(numberOfButtons == 4 || numberOfButtons == 5){
                            input "button_2_push_brighten", "capability.switchLevel", title: "Pushing \"Brighten\" button brightens?", multiple: true, submitOnChange:true
                        }
                        if(numberOfButtons == 5){
                            input "button_3_push_toggle", "capability.switch", title: "Pushing Center button toggles? (if on, turn off; if off, turn on)", multiple: true, submitOnChange:true
                        }
                        if(numberOfButtons == 4 || numberOfButtons == 5){
                            input "button_4_push_dim", "capability.switchLevel", title: "Pushing \"Dim\" button dims?", multiple: true, submitOnChange:true
                        }
                        input "button_5_push_off", "capability.switch", title: "Pushing Bottom (\"Off\") buttont turns off?", multiple: true, submitOnChange:true

                        if(!replicateHold){
                            input "replicateHold", "bool", title: "Replicating settings for Long Push. Click to customize Hold actions.", submitOnChange:true, defaultValue: false
                        } else {
                            input "replicateHold", "bool", title: "Holding Long Push options shown. Click to replicate from Push.", submitOnChange:true, defaultValue: false
                        }

                        input "button_1_hold_on", "capability.switch", title: "Holding Top \"On\" button turns on?", multiple: true, submitOnChange:true
                        if(numberOfButtons == 4 || numberOfButtons == 5){
                            input "button_2_hold_brighten", "capability.switchLevel", title: "Holding \"Brighten\" button brightens?", multiple: true, submitOnChange:true
                        }
                        if(numberOfButtons == 5){
                            input "button_3_hold_toggle", "capability.switch", title: "Holding Center button toggles? (if on, turn off; if off, turn on)", multiple: true, submitOnChange:true
                        }
                        if(numberOfButtons == 4 || numberOfButtons == 5){
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
                    if(app.label && buttonDevice && numberOfButtons) displayMultiDeviceOption()
                    paragraph "<div style=\"background-color:BurlyWood\"><b> Select what to do for each Pico action:</b></div>"
                }

                if(app.label && buttonDevice && numberOfButtons){
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
                    if((numberOfButtons == 4 || numberOfButtons == 5) && !error){
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
                    if(numberOfButtons == 5 && !error){
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
                    if((numberOfButtons == 4 || numberOfButtons == 5) && !error){
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
                        if((numberOfButtons == 4 || numberOfButtons == 5)  && !error){
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
                        if(numberOfButtons == 5 && !error){
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
                        if((numberOfButtons == 4 || numberOfButtons == 5)  && !error){
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
            displayScheduleSection()
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

def displayLabel(text = "Null", width = 12){
    paragraph("<div style=\"background-color:#DCDCDC\"><b> $text:</b></div>",width:width)
}

def displayInfo(text = ""){
    if(text == "") {
        paragraph "<div style=\"background-color:AliceBlue\"> </div>"
    } else {
        paragraph "<div style=\"background-color:AliceBlue\">$infoIcon $text</div>"
    }
}

def testNoErrors(){
        if((!app.label) ||
       (!buttonDevice) ||
       (advancedSetup && !buttonPush1 && !buttonPush2 && !buttonPush3 && !buttonPush4 && !buttonPush5) ||
           (!advancedSetup && !controlDevice) ||
       (inputStartType == "time" && !inputStartTime) ||
       (inputStopType == "time" && !inputStopTime) ||
       ((inputStartType == "sunrise" || inputStartType == "sunset") && !inputStartSunriseType) ||
       ((inputStopType == "sunrise" || inputStopType == "sunset") && !inputStopSunriseType) ||
       ((inputStartSunriseType == "before" || inputStartSunriseType == "after") && !inputStartBefore) ||
       ((inputStopSunriseType == "before" || inputStopSunriseType == "after") && !inputStopBefore)) return false
    return true
}

// Display functions
def getDeviceCount(device){
    if(!settings[buttonDevice]) return false
    return settings[buttonDevice].size()
}

def getPicoPlural(){
    if(!contactSensorCount) return "Pico(s)"
    if(contactSensorCount > 1) return "Picos"
    return "Pico"
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

/* ************************************************************************ */
/* TO-DO: Change it so different number of buttons can be used in one setup */
/* but add warnings when selecting to assign button number not on device,   */
/* as well as a general warning.                                            */
/* ************************************************************************ */
def displayPicoOption(){
    if(buttonDevice){
        buttonDevice.each{
            if(count == 1) multipleDevices = true
            count = 1
        }
        if(multipleDevices){
            pluralInput = "Picos"
        } else {
            pluralInput = "Pico"
        }
        input "buttonDevice", "capability.pushableButton", title: "$pluralInput:", multiple: true, submitOnChange:true
    } else {
        input "buttonDevice", "capability.pushableButton", title: "Select Pico device(s) :", defaultValue: 328, multiple: true, submitOnChange:true
        displayInfo("Select which Pico(s) to control. You can select multiple Pico devices, but all should have the same number of buttons.")
    }
}

/* ************************************************************************ */
/* TO-DO: Add errors in setup for defining actions to non-existent buttons. */
/* ************************************************************************ */
def displayPicoTypeOption(){
    //Get maximum number of buttons
    /* ************************************************************************ */
    /* TO-DO: Add error trap for if no numberOfButtons is set for device.       */
    /* ************************************************************************ */
    buttonDevice.each{
        if(it.currentValue("numberOfButtons")) {
            if(!numberOfButtons){
                numberOfButtons = it.currentValue("numberOfButtons")
            } else if(numberOfButtons < it.currentValue("numberOfButtons")) {
                numberOfButtons = it.currentValue("numberOfButtons")
            }
        } else {
            //display error
        }
    }
}

def displayAdvancedSetupOption(){
    displayLabel("Custom buttons and/or devices")
    if(advancedSetup){
        input "advancedSetup", "bool", title: "<b>Allowing custom button actions.</b> Click to use normal button actions.", submitOnChange:true
        if(numberOfButtons == 2) {
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
    if(numberOfButtons == 4 || numberOfButtons == 5) displaySelectActionsButtonOption(2,"\"Brighten\"")
    if(numberOfButtons == 5) displaySelectActionsButtonOption(3,"Center")
    if(numberOfButtons == 4 || numberOfButtons == 5) displaySelectActionsButtonOption(4,"\"Dim\"")
    displaySelectActionsButtonOption(5,"Bottom (\"Off\")")
    if(!replicateHold){
        input "replicateHold", "bool", title: "Replicating settings for Long Push. Click to customize Hold actions.", submitOnChange:true, defaultValue: false
    } else {
        input "replicateHold", "bool", title: "Long Push options shown. Click to replicate from Push.", submitOnChange:true, defaultValue: false

        displaySelectActionsButtonOption(1,"Top (\"On\")","hold")
        if(numberOfButtons == 4 || numberOfButtons == 5) displaySelectActionsButtonOption(2,"\"Brighten\"","hold")
        if(numberOfButtons == 5) displaySelectActionsButtonOption(3,"Center","hold")
        if(numberOfButtons == 4 || numberOfButtons == 5) displaySelectActionsButtonOption(4,"\"Dim\"","hold")
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
        switchType = "switchLevel"
    } else {
        switchType = "switch"
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

def displayScheduleSection(){
    if(!settings["buttonDevice"]) return

    helpTip = "Scheduling only applies with " + getPicoPlural() + ". To schedule the devices or default settings for them, use the Time app."  
    
    // If only days entered
    sectionTitle="<b>"
        List dayList=[]
        settings["days"].each{
            dayList.add(it)
        }
        dayText = dayList.join(", ")
    if(!settings["inputStartType"] && !settings["inputStopType"] && settings["days"]){
        sectionTitle += "Only on: " + dayText + "</b>" + moreOptions
        hidden = true
        // If only start time (and days) entered
    }  else if(checkTimeComplete("start") && settings["inputStartType"] && (!checkTimeComplete("stop") || !settings["inputStopType"])){
        sectionTitle = "Beginning at $varStartTime"
        if(settings["days"]) sectionTitle += " on: " + dayText
        if(settings["months"]) sectionTitle += "; in " + monthText
        sectionTitle += "</b>"
        hidden = false
        // If only stop time (and day) entered
    } else if(checkTimeComplete("stop") && settings["inputStopType"] && (!checkTimeComplete("start") || !settings["inputStartType"])){
        sectionTitle = "Ending at $varStopTime"
        if(settings["days"]) sectionTitle += " on: " + dayText
        if(settings["months"]) sectionTitle += "; in " + monthText
        sectionTitle += "</b>"
        hidden = false
        // If all options entered
    } else if(checkTimeComplete("start") && checkTimeComplete("stop") && settings["inputStartType"] && settings["inputStopType"]){
        varStartTime = getTimeVariables("start")
        varStopTime = getTimeVariables("stop")
        sectionTitle = "<b>Only if between $varStartTime and $varStopTime"
        if(settings["days"] && settings["months"]) {
            sectionTitle += "</b>"
        } else {
            sectionTitle += " on: " + dayText + "</b>"
            if(settings["months"]) sectionTitle += "; in " + monthText
            sectionTitle += "</b>"
        }
        hidden = true
        // If no options are entered
    } else {
        sectionTitle = "Click to set schedule (optional)"
        hidden = true
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        if(!settings["inputStartType"]) displayInfo(helpTip)
        displayStartTypeOption()

        // Display exact time option
        if(settings["inputStartType"] == "time"){
            displayTimeOption("start")
        } else if(settings["inputStartType"]){
            // Display sunrise/sunset type option (at/before/after)
            displaySunriseTypeOption("start")
            // Display sunrise/sunset offset
            if(inputStartSunriseType && inputStartSunriseType != "at") displaySunriseOffsetOption("start")
        }

        if(checkTimeComplete("start") && settings["inputStartType"]){
            displayStopTypeOption()

            // Display exact time option
            if(settings["inputStopType"] == "time"){
                displayTimeOption("stop")
            } else if(settings["inputStopType"]){
                // Display sunrise/sunset type option (at/before/after)
                displaySunriseTypeOption("stop")
                // Display sunrise/sunset offset
                if(inputStopSunriseType && inputStopSunriseType != "at") displaySunriseOffsetOption("stop")
            }
        }

        displayDaysOption(dayText)
        displayMonthsOption(monthText)


        displayInfo(message)
    }
}

def displayDaysOption(dayText){
    input "days", "enum", title: "On these days (defaults to all days)", multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"], submitOnChange:true

    return
}

def displayMonthsOption(monthText){
    input "months", "enum", title: "In these months (defaults to all months)", multiple: true, width: 12, options: ["1": "January", "2": "February", "3": "March", "4": "April", "5": "May", "6": "June", "7": "July", "8": "August", "9": "September", "10": "October", "11": "November", "12": "December"], submitOnChange:true

    return
}

def displayStartTypeOption(){
    if(!checkTimeComplete("start")  || !settings["inputStartType"]){
        displayLabel("Schedule starting time")
    } else {
        displayLabel("Schedule start")
    }
    if(!settings["inputStartType"]){
        width = 12
        input "inputStartType", "enum", title: "Start time (click to choose option):", multiple: false, width: width, options: ["time":"Start at specific time", "sunrise":"Sunrise (at, before or after)","sunset":"Sunset (at, before or after)" ], submitOnChange:true
        displayInfo("Select whether to enter a specific time, or have start time based on sunrise and sunset for the Hubitat location. Required field for a schedule.")
    } else {
        if(settings["inputStartType"] == "time" || !settings["inputStartSunriseType"] || settings["inputStartSunriseType"] == "at"){
            width = 6
        } else if(settings["inputStartSunriseType"]){
            width = 4
        }
        input "inputStartType", "enum", title: "Start time option:", multiple: false, width: width, options: ["time":"Start at specific time", "sunrise":"Sunrise (at, before or after)","sunset":"Sunset (at, before or after)" ], submitOnChange:true
    }
}

def displayStopTypeOption(){
    if(!checkTimeComplete("stop")){
        displayLabel("Schedule stopping time")
    } else {
        displayLabel("Schedule stop")
    }
    if(!settings["inputStopType"]){
        width = 12
        input "inputStopType", "enum", title: "Stop time (click to choose option):", multiple: false, width: width, options: ["time":"Stop at specific time", "sunrise":"Sunrise (at, before or after)","sunset":"Sunset (at, before or after)" ], submitOnChange:true
    } else {
        if(!settings["inputStopType"] || settings["inputStopType"] == "none"){
            width = 12
        } else if(settings["inputStopType"] == "time" || !settings["inputStopSunriseType"] || settings["inputStopSunriseType"] == "at"){
            width = 6
        } else if(inputStopSunriseType){
            width = 4
        }
        input "inputStopType", "enum", title: "Stop time option:", multiple: false, width: width, options: ["time":"Stop at specific time", "sunrise":"Sunrise (at, before or after)","sunset":"Sunset (at, before or after)" ], submitOnChange:true
    }
}

def displayTimeOption(lcType){
    ucType = lcType.capitalize()
    input "input${ucType}Time", "time", title: "$ucType time:", width: width, submitOnChange:true
    if(!settings["input${ucType}Time"]) displayInfo("Enter the time to $lcType the schedule in \"hh:mm AM/PM\" format. Required field.")
}

def displaySunriseTypeOption(lcType){
    if(!settings["input${ucType}SunriseType"] || settings["input${ucType}SunriseType"] == "at") {
        width = 6 
    } else {
        width = 4
    }
    // sunriseTime = getSunriseAndSunset()[settings["input${ucType}Type"]].format("hh:mm a")
    input "input${ucType}SunriseType", "enum", title: "At, before or after " + settings["input${ucType}Type"] + ":", multiple: false, width: width, options: ["at":"At " + settings["input${ucType}Type"], "before":"Before " + settings["input${ucType}Type"], "after":"After " + settings["input${ucType}Type"]], submitOnChange:true
    if(!settings["input${ucType}SunriseType"]) displayInfo("Select whether to start exactly at " + settings["input${ucType}Type"] + " (currently, $sunriseTime). To allow entering minutes prior to or after " + settings["input${ucType}Type"] + ", select \"Before " + settings["input${ucType}Type"] + "\" or \"After " + settings["input${ucType}Type"] + "\". Required field.")
}

def checkTimeComplete(lcType){
    ucType = lcType.capitalize()

    // If everything entered
    if((settings["input${ucType}Type"] == "time" && settings["input${ucType}Time"]) || 
       ((settings["input${ucType}Type"] == "sunrise" || settings["input${ucType}Type"] == "sunset") && settings["input${ucType}SunriseType"] == "at") || 
       ((settings["input${ucType}Type"] == "sunrise" || settings["input${ucType}Type"] == "sunset") && (settings["input${ucType}SunriseType"] == "before" || settings["input${ucType}SunriseType"] == "after") && (settings["input${ucType}Before"]))){
        return true
    } else if(!settings["input${ucType}Type"] && !settings["input${ucType}SunriseType"] && !settings["input${ucType}Before"]){
        return true
    } else {
        return false
    }
}

def getTimeVariables(lcType){
    ucType = lcType.capitalize()
    // If time, then set string to "[time]"
    if(settings["input${ucType}Type"] == "time"){
        return Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings["input${ucType}Time"]).format("h:mm a", location.timeZone)
        // If sunrise or sunset
    } else if((settings["input${ucType}Type"] == "sunrise" || settings["input${ucType}Type"] == "sunset")  && settings["input${ucType}SunriseType"]){
        if(settings["input${ucType}SunriseType"] == "at"){
            // Set string to "sun[rise/set] ([sunrise/set time])"
            return settings["input${ucType}Type"] + " (" + getSunriseAndSunset()[settings["input${ucType}Type"]].format("hh:mm a") + ")"
            // If before sunrise
        } else if(settings["input${ucType}Type"] == "sunrise" && settings["input${ucType}SunriseType"] == "before" && settings["input${ucType}Before"]){
            // Set string to "[number] minutes before sunrise ([time])
            if(settings["input${ucType}Before"]) return settings["input${ucType}Before"] + " minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset(sunriseOffset: (settings["input${ucType}Before"] * -1), sunsetOffset: 0)[settings["input${ucType}Type"]].format("hh:mm a") + ")"
            // If after sunrise
        } else if(settings["input${ucType}Type"] == "sunrise" && settings["input${ucType}SunriseType"] == "after" && settings["input${ucType}Before"]){
            // Set string to "[number] minutes after sunrise ([time])
            if(settings["input${ucType}Before"]) return settings["input${ucType}Before"] + " minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset(sunriseOffset: settings["input${ucType}Before"], sunsetOffset: 0)[settings["input${ucType}Type"]].format("hh:mm a") + ")"
            // If before sunset
        } else if(settings["input${ucType}Type"] == "sunset" && settings["input${ucType}SunriseType"] == "before" && settings["input${ucType}Before"]){
            // Set string to "[number] minutes before sunset ([time])
            if(settings["input${ucType}Before"]) return settings["input${ucType}Before"] + " minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: (settings["input${ucType}Before"] * -1))[settings["input${ucType}Type"]].format("hh:mm a") + ")"
            // If after sunrise
        } else if(settings["input${ucType}Type"] == "sunset" && settings["input${ucType}SunriseType"] == "after" && settings["input${ucType}Before"]){
            // Set string to "[number] minutes after sunset ([time])
            if(settings["input${ucType}Before"]) return settings["input${ucType}Before"] + " minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: settings["input${ucType}Before"])[settings["input${ucType}Type"]].format("hh:mm a") + ")"
        } else {
            return
        }
    } else {
        return
    }
}

def displaySunriseOffsetOption(lcType){
    ucType = lcType.capitalize()
    if(!settings["input${ucType}SunriseType"] || settings["input${ucType}SunriseType"] == "at") return

    if(settings["input${ucType}Before"] && settings["input${ucType}Before"] > 1441){
        // "Minues [before/after] [sunrise/set] is equal to "
        message = "Minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " is equal to "
        if(settings["input${ucType}Before"]  > 2881){
            // "X days"
            message += Math.floor(settings["input${ucType}Before"]  / 60 / 24) + " days."
        } else {
            message += "a day."
        }
        warningMessage(message)
    }
    input "input${ucType}Before", "number", title: "Minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + ":", width: 4, submitOnChange:true
    if(!settings["input${ucType}Before"]) displayInfo("Enter the number of minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " to start the schedule. Required field.")
}

def displayChangeModeOption(){
    if(!settings["contactDevice"] || !settings["deviceType"] || !settings["device"] || !settings["openAction"] || !settings["closeAction"]) return

    hidden = true
    if((settings["openMode"] || settings["closeMode"]) && (!settings["openMode"] || !settings["closeMode"])) hidden = false

    if(!settings["openMode"] && !settings["closeMode"]){
        sectionTitle = "Click to set Mode change (optional)"
    } else {
        sectionTitle = "<b>"
        if(settings["openMode"]) {
            sectionTitle = "On open, set Mode " + settings["openMode"]
            if(settings["closeMode"]) sectionTitle += "<br>"
        }

        if(settings["closeMode"]) {
            sectionTitle += "On close, set Mode " + settings["closeMode"]
        }
        sectionTitle += "</b>"
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        input "openMode", "mode", title: "Set Hubitat's \"Mode\" (on open)?", width: 6, submitOnChange:true
        input "closeMode", "mode", title: "Set Hubitat's \"Mode\" (on close)?", width: 6, submitOnChange:true
    }
}

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

def installed() {
    putLog(874,"trace", "Installed")
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    putLog(880,"trace","Updated")
    unsubscribe()
    initialize()
}

def initialize() {
    state.logLevel = getLogLevel()
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

    subscribe(buttonDevice, "pushed", buttonPushed)
    subscribe(buttonDevice, "hold", buttonPushed)
    subscribe(buttonDevice, "released", buttonReleased)
    
    setTime()
    
    putLog(892,"trace","Initialized")
}

def buttonPushed(evt){
    // If not correct day, return nulls
    if(!parent.nowInDayList(settings["days"],app.label)) return
    if(!parent.nowInMonthList(settings["months"],app.label)) return

    // if not between start and stop time, return nulls
    if(atomicState.stop && !parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return
    
    buttonNumber = evt.value
    numberOfButtons = evt.device.currentValue("numberOfButtons")

    // Needs to be state since we're passing back and forth to parent for progressive dim and brightening
    if(evt.name == "pushed") {
        atomicState.action = "push"
    } else {
        atomicState.action = "hold"
    }

    if(settings["${atomicState.action}Multiplier"]) settings["${atomicState.action}Multiplier"] = parent.validateMultiplier(settings["${atomicState.action}Multiplier"],app.label)

    // Treat 2nd button of 2-button Pico as "off" (eg button 5)
    if(buttonNumber == "2" && numberOfButtons == 2) buttonNumber = 5
    if(buttonNumber == "4" && numberOfButtons == 4) buttonNumber = 5
    if(buttonNumber == "3" && numberOfButtons == 4) buttonNumber = 4

    // There the action for either push or brighten, if we have only one variable for the Pico actions;
    // which isn't the case with "multiDevice"
    if(!multiDevice && !advancedSetup){
        switch(buttonNumber){
            case "1": switchAction = "on"
            break
            case "2": switchAction = "brighten"
            break
            case "4": switchAction = "dim"
            break
            case "5": switchAction = "off"
        }
    } else if(!multiDevice && (atomicState.action == "push" || (atomicState.action == "hold" && replicateHold))){
        switchAction = settings["buttonPush${buttonNumber}"]
    } else if(!multiDevice && atomicState.action == "hold" && !replicateHold){
        switchAction = settings["buttonHold${buttonNumber}"]
    } else if(!multiDevice && (atomicState.action == "push" || (atomicState.action == "hold" && replicateHold))){
        switchAction = settings["button_" + buttonNumber + "push"]
    }

    if(switchAction){
        switch(switchAction){
            case "on":
            parent.updateStateMulti(settings["controlDevice"],"on",app.label)

            parent.setStateMulti(settings["controlDevice"],app.label)
            message = "turn $switchAction"
            break

            case "brighten":
            if(atomicState.action == "push"){
                parent.updateStateMulti(settings["controlDevice"],"on",app.label)
                settings["controlDevice"].each{singleDevice->
                    setLevel = parent.nextLevel(singleDevice,"brighten",app.label)
                    if(setLevel){
                        defaults = ["level": ["startLevel": setLevel, "appId":"pico"]]
                        parent.updateLevelsSingle(singleDevice,defaults,app.label)
                    }
                }
                parent.setStateMulti(settings["controlDevice"],app.label)
            } else {
                holdBrighten(settings["controlDevice"],app.label)
            }

            message = "brighten"
            break

            case "toggle": 
            parent.updateStateMulti(controlDevice,switchAction,app.label)
            parent.setStateMulti(settings["controlDevice"],app.label)
            message = "toggle"
            break

            case "dim":
            if(atomicState.action == "push"){
                parent.updateStateMulti(settings["controlDevice"],"on",app.label)
                settings["controlDevice"].each{singleDevice->
                    setLevel = parent.nextLevel(singleDevice,"dim",app.label)
                    if(setLevel){
                        defaults = ["level": ["startLevel": setLevel, "appId":"pico"]]
                        parent.updateLevelsSingle(singleDevice,defaults,app.label)
                    }
                }
                parent.setStateMulti(settings["controlDevice"],app.label)
            } else {
                holdDim(settings["controlDevice"],app.label)
            }
            message = "dim"
            break

            case "off": 
            parent.updateStateMulti(settings["controlDevice"],switchAction,app.label)
            parent.setStateMulti(settings["controlDevice"],app.label)
            message = "turn $switchAction"
            break

            case "resume": 
            parent.updateLevelsMulti(settings["controlDevice"],["level":["time":"resume"],"temp":["time":"resume"],"hue":["time":"resume"],"sat":["time":"resume"]],app.label)
            message = "resume"
        }
        putLog(996,"trace","Button $buttonNumber of $buttonDevice $atomicState.action for $controlDevice to $message; default setup; ")
        // if(multiDevice && (!advancedSetup || advancedSetup))
    } else {
        if(atomicState.action == "push"){
            actionText = "pushed"
        } else {
            actionText = "hold"
        }

        if(settings["button_${buttonNumber}_${atomicState.action}_toggle"]) {
            putLog(1006,"trace","Button $buttonNumber of $buttonDevice $actionText for " + settings["button_${buttonNumber}_${atomicState.action}_toggle"] + "; remapped and advanced setup; toggling")
            parent.updateStateMulti(settings["button_${buttonNumber}_${atomicState.action}_toggle"],"toggle",app.label)
            parent.setStateMulti(settings["button_${buttonNumber}_${atomicState.action}_toggle"],app.label)

        }
        if(settings["button_${buttonNumber}_${atomicState.action}_on"]) {
            putLog(1012,"trace","Button $buttonNumber of $buttonDevice $actionText for " + settings["button_${buttonNumber}_${atomicState.action}_on"] +"; remapped and advanced setup; turning on")
            parent.updateStateMulti(settings["button_${buttonNumber}_${atomicState.action}_on"],"on",app.label)
            parent.setStateMulti(settings["button_${buttonNumber}_${atomicState.action}_on"],app.label)
        }
        if(settings["button_${buttonNumber}_${atomicState.action}_off"]){
            putLog(1017,"trace","Button $buttonNumber of $buttonDevice $actionText for " + settings["button_${buttonNumber}_${atomicState.action}_off"] + "; remapped and advanced setup; turning off")
            parent.updateStateMulti(settings["button_${buttonNumber}_${atomicState.action}_off"],"off",app.label)
            parent.setStateMulti(settings["button_${buttonNumber}_${atomicState.action}_off"],app.label)
        }
        if(settings["button_${buttonNumber}_${atomicState.action}_dim"]) {
            putLog(1022,"trace","Button $buttonNumber of $buttonDevice $actionText for " + settings["button_${buttonNumber}_${atomicState.action}_dim"] + "; remapped and advanced setup; dimming")
            if(atomicState.action == "push"){
                parent.updateStateMulti(settings["button_${buttonNumber}_${atomicState.action}_dim"],"on",app.label)
                settings["button_${buttonNumber}_${atomicState.action}_dim"].each{singleDevice->
                    setLevel = parent.nextLevel(singleDevice,"dim",app.label)
                    if(setLevel){
                        defaults = ["level": ["startLevel": setLevel, "appId":"pico"]]
                        parent.updateLevelsSingle(singleDevice,defaults,app.label)
                    }
                }
                parent.setStateMulti(settings["button_${buttonNumber}_${atomicState.action}_dim"],app.label)
            } else {
                holdDim(settings["button_${buttonNumber}_${atomicState.action}_dim"])
            }
        }
        if(settings["button_${buttonNumber}_${atomicState.action}_brighten"]) {
            putLog(1038,"trace","Button $buttonNumber of $buttonDevice $actionText for " + settings["button_${buttonNumber}_${atomicState.action}_brighten"] + "; remapped and advanced setup; brightening")
            if(atomicState.action == "push"){
                parent.updateStateMulti(settings["button_${buttonNumber}_${atomicState.action}_brighten"],"on",app.label)
                settings["button_${buttonNumber}_${atomicState.action}_brighten"].each{singleDevice->
                    setLevel = parent.nextLevel(singleDevice,"brighten",app.label)
                    if(setLevel){
                        defaults = ["level": ["startLevel": setLevel, "appId":"pico"]]
                        parent.updateLevelsSingle(singleDevice,defaults,app.label)
                    }
                }
                parent.setStateMulti(settings["button_${buttonNumber}_${atomicState.action}_brighten"],app.label)
            } else {
                holdBrighten(settings["button_${buttonNumber}_${atomicState.action}_brighten"])
            }
        }
        if(settings["button_${buttonNumber}_${atomicState.action}_resume"]) {
            putLog(1054,"trace","Button $buttonNumber of $buttonDevice $actionText for " + settings["button_${buttonNumber}_${atomicState.action}_resume"] + "; remapped and advanced setup; brightening")
            settings["button_${buttonNumber}_${atomicState.action}_resume"].each{singleDevice->
                // fix this
                if(!rescheduleIncrementalSingle(singleDevice,app.label)) parent.updateStateSingle(singleDevice,action,app.label)
            }
        }

    }
}



// place holder until I can redo my pico setups to not throw an error
def buttonHeld(evt){
}

def buttonReleased(evt){
    // If not correct day, return nulls
    if(!parent.nowInDayList(settings["days"],app.label)) return
    if(!parent.nowInMonthList(settings["months"],app.label)) return

    // if not between start and stop time, return nulls
    if(atomicState.stop && !parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return
    
    buttonNumber = evt.value
    numberOfButtons = evt.device.currentValue("numberOfButtons")

    if (buttonNumber == "2" || (buttonNumber == "4" && (numberOfButtons == 4 || numberOfButtons == 5)) || (buttonNumber == "1" && numberOfButtons == 2)){
        putLog(1073,"trace","Button $buttonNumber of $buttonDevice released, unscheduling all")
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
    } else if(atomicState.action == "hold" &&  holdMultiplier){
        return holdMultiplier
    } else if(atomicState.action == "hold" &&  !pushMultiplier){
        return pushMultiplier
    } else {
        return 1.2
    }
}

// counts number of steps for brighten and dim
// action = "dim" or "brighten"
def getSteps(level, action){
    if (action != "dim" && action != "brighten"){
        putLog(1100,"error","Invalid value for action \"$action\" sent to getSteps function")
        return false
    }

    def steps = 0

    // If as already level 1 and dimming or 100 and brightening
    if((action == "dim" && level < 2) || (action == "brighten" && level > 99)){
        steps = 0
    }

    //Just step through nextLevel until hit 1 or 100, and tally total times
    if (action == "dim"){
        if(parent.isNumeric(level)){
            while (level  > 1) {
                steps = steps + 1
                level = parent.nextLevel(level, action,app.getId())
            }
        }
    } else if(action == "brighten"){
        if(parent.isNumeric(level)){
            while (level  < 100) {
                steps = steps + 1
                level = parent.nextLevel(level, action,app.getId())
            }
        }
    }
    putLog(1127,"debug","Function getSteps returning $steps")
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
        putLog(1185,"trace","Function runSetProgressiveLevel returning (no matching device)")
        return
    }
    parent.setLevelSingle(defaults,device,app.label)
    // Why reschedule?
    //parent.reschedule(device)
}

def holdDim(device){
    // This needs to use the table data
    def level = getLevel(device)

    device.each{singleDevice->
        if(parent.isFan(singleDevice,app.label) == true){
            parent.dim("dim",singleDevice,app.id,app.label)
            // If dimming a light that's off, turn it on
            // setStateSingle does that by setting to 1% level, which is what we want
        } else if(!parent.isOn(singleDevice,app.label)){
            //parent.setLevelSingle(1,null,null,null,device,app.label)
            parent.updateStateSingle(singleDevice,"on",app.label)
        } else {
            if(level < 2){
                putLog(1207,"info","Can't dim $singleDevice; already 1%.")
            } else {
                def steps = getSteps(level, "dim")
                def newLevel

                for(def i = 1; i <= steps; i++) {
                    setLevel = parent.nextLevel(level, "dim",app.getId())
                    defaults = ["level": ["startLevel": setLevel, "appId":"pico"]]
                    parent.updateLevelsSingle(device,app.label)
                    runInMillis(i*750,runSetProgressiveLevel, [overwrite: false, data: [device: it.id, level: setLevel]])
                    level = setLevel
                }
            }
        }
    }
    parent.setStateMulti(device,app.label)
}

def holdBrighten(device){
    def level = getLevel(device)

    device.each{singleDevice->
        if(parent.isFan(singleDevice,app.label)){
            parent.dim("brighten",singleDevice,"pico",app.label)
            // If brightening a light that's off, turn it on at 1%
        } else if(!parent.isOn(it,app.label)){
            parent.updateStateSingle(singleDevice,"on",app.label)\
            reschedule(it)
        } else {
            if(level > 99){
                putLog(1237,"info","Can't brighten $it; already 100%.")
            } else {
                def steps = getSteps(level, "brighten")
                def newLevel

                for(def i = 1; i <= steps; i++) {
                    setLevel = parent.nextLevel(level, "brighten",app.getId())
                    defaults = ["level": ["startLevel": setLevel, "appId":"pico"]]
                    runInMillis(i*750,runSetProgressiveLevel, [overwrite: false, data: [device: it.id, level: setLevel]])
                    level = setLevel
                }
            }
        }
    }
    parent.setStateMulti(device,app.label)
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

// Scheduled funtion to reset the value of deviceChange
// Must be in every app using MultiOn
def resetStateDeviceChange(){
    atomicState.deviceChange = null
    return
}
def checkLog(type = null){
    if(!state.logLevel) getLogLevel()
    switch(type) {
        case "error":
        if(state.logLevel > 0) return true
        break
        case "warn":
        if(state.logLevel > 1) return true
        break
        case "info":
        if(state.logLevel > 2) return true
        break
        case "trace":
        if(state.logLevel > 3) return true
        break
        case "debug":
        if(state.logLevel == 5) return true
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,type = "trace",message = null){
    if(!checkLog(type)) return
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
