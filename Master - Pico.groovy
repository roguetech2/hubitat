/***********************************************************************************************************************
*
*  Copyright (C) 2024 roguetech
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
*  Version: 0.5.12
*
***********************************************************************************************************************/

// To-do: Change "Push" to pushed, and "Hold" to held
// To-do: Add double-push
// To-do: Add Held + Released ?
// To-do: Add presense ?

definition(
    name: 'Master - Pico',
    namespace: 'master',
    author: 'roguetech',
    description: 'Pico and Caseta switches',
    parent: 'master:Master',
    category: 'Convenience',
    importUrl: 'https://raw.githubusercontent.com/roguetech2/hubitat/master/Master%20-%20Pico.groovy',
    iconUrl: 'http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png',
    iconX2Url: 'http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png'
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
    infoIcon = '<img src="http://emily-john.love/icons/information.png" width=20 height=20>'
    warningIcon = '<img src="http://emily-john.love/icons/warning.png" width=20 height=20>'
    errorIcon = '<img src="http://emily-john.love/icons/error.png" width=20 height=20>'
    moreOptions = ' <font color="gray">(more options)</font>'
    if(!settings) settings = [:]

    numberOfButtons = getButtonNumbers()
    install = formComplete()

    page(name: 'setup', install: install, uninstall: true) {
        // display Name
        if(!app.label){
            section(){
                displayNameOption()
            }
        } else {
            //if(buttonDevice) numberOfButtons = getButtonNumbers()
            // Multi + advanced needs section closed
            if(settings['multiDevice'] && settings['customActionsSetup']){
                section(){
                    displayNameOption()
                    displayPicoOption()
                    if(numberOfButtons) {
                        displayMultiDeviceOption()
                        displayCustomActionsOption()
                    }
                    paragraph '<div style="background-color:BurlyWood"><b> Select what to do for each Pico action:</b></div>'
                }
                displayMultiDeviceAdvanced()
                displayScheduleSection()
            }
            if(!settings['multiDevice'] || !settings['customActionsSetup']){
                section(){
                    displayNameOption()
                    displayPicoOption()
                    if(numberOfButtons) displayMultiDeviceOption()
                    if(numberOfButtons && !settings['multiDevice']) {
                        if(controlDevice) displayCustomActionsOption()
                        displaySingleDevice()
                    }
                    if(numberOfButtons && settings['multiDevice']) {
                        displayCustomActionsOption()
                        displayMultiDeviceSimple()
                    }
                }
                displayScheduleSection()
            }
        }
    }
}

def displaySingleDevice(){
    if(!numberOfButtons) return
    if(!controlDevice) return

    if(settings['customActionsSetup']) displaySingleDeviceAdvanced()
    if(!settings['customActionsSetup']) displaySingleDeviceSimple()

    if(!settings['customActionsSetup']){
        displayMultiplierOption()
        return
    }
    if(button_1_push == 'dim' || button_2_push == 'dim' || button_3_push == 'dim' || button_4_push == 'dim' || button_5_push == 'dim'){
        displayMultiplierOption()
        return
    }
    if(button_1_hold == 'dim' || button_2_hold == 'dim' || button_3_hold == 'dim' || button_4_hold == 'dim' || button_5_hold == 'dim'){
        displayMultiplierOption()
        return
    }
    if(button_1_push == 'brighten' || button_2_push == 'brighten' || button_3_push == 'brighten' || button_4_push == 'brighten' || button_5_push == 'brighten'){
        displayMultiplierOption()
        return
    }
    if(button_1_hold == 'brighten' || button_2_hold == 'brighten' || button_3_hold == 'brighten' || button_4_hold == 'brighten' || button_5_hold == 'brighten'){
        displayMultiplierOption()
        return
    }
}

def displaySingleDeviceSimple(){
     if(replicateHold){
        paragraph '<div style="background-color:GhostWhite"> Pushing or Holding Top button ("On") turns on.</div>'
        if(numberOfButtons == 4 || numberOfButtons == 5) paragraph '<div style="background-color:GhostWhite"> Pushing or Holding "Brighten" button brightens.</div>'
        if(numberOfButtons == 5) paragraph '<div style="background-color:GhostWhite"> Pushing or Holding Center button does <b>nothing</b>.</div>'
        if(numberOfButtons == 4 || numberOfButtons == 5) paragraph '<div style="background-color:GhostWhite"> Pushing or Holding "Dim" dims.</div>'
        paragraph '<div style="background-color:GhostWhite"> Pushing or Holding Bottom button ("Off") turns off.</div>'
    }
    if(!replicateHold){
        paragraph '<div style="background-color:GhostWhite"> Pushing Top button ("On") turns on.</div>'
        if(numberOfButtons == 4 || numberOfButtons == 5) paragraph '<div style="background-color:GhostWhite"> Pushing "Brighten" button brightens.</div>'
        if(numberOfButtons == 5) paragraph '<div style="background-color:GhostWhite"> Pushing Center button does <b>nothing</b>.</div>'
        if(numberOfButtons == 4 || numberOfButtons == 5) paragraph '<div style="background-color:GhostWhite"> Pushing "Dim" dims.</div>'
        paragraph '<div style="background-color:GhostWhite"> Pushing Bottom button ("Off") turns off.</div>'
    }
}

def displaySingleDeviceAdvanced(){
    displayLabel('Select what to do for each Pico action')
    displaySelectActionsButtonOption(1,'Top ("On")')
    if(numberOfButtons == 4 || numberOfButtons == 5) displaySelectActionsButtonOption(2,'brighten')
    if(numberOfButtons == 5) displaySelectActionsButtonOption(3,'Center')
    if(numberOfButtons == 4 || numberOfButtons == 5) displaySelectActionsButtonOption(4,'dim')
    displaySelectActionsButtonOption(5,'Bottom ("Off")')
    if(button_1_push || button_2_push || button_3_push || button_4_push || button_5_push){
        if(replicateHold){
            input 'replicateHold', 'bool', title: addFieldName('Long Push options shown. Click to replicate from Push.','replicateHold'), submitOnChange:true, defaultValue: false

            displaySelectActionsButtonOption(1,'Top ("On")','hold')
            if(numberOfButtons == 4 || numberOfButtons == 5) displaySelectActionsButtonOption(2,'brighten','hold')
            if(numberOfButtons == 5) displaySelectActionsButtonOption(3,'Center','hold')
            if(numberOfButtons == 4 || numberOfButtons == 5) displaySelectActionsButtonOption(4,'dim','hold')
            displaySelectActionsButtonOption(5,'Bottom ("Off")','hold')
        }
        if(!replicateHold) input 'replicateHold', 'bool', title: addFieldName('Replicating settings for Long Push. Click to customize Hold actions.','replicateHold'), submitOnChange:true, defaultValue: false
    }
}

def displayMultiDeviceSimple(){
    paragraph '<div style="background-color:BurlyWood"><b> Select what to do for each Pico action:</b></div>'

    /* ************************************************************************ */
    /* TO-DO: Put these inputs in a function.                                   */
    /* ************************************************************************ */
    if(!replicateHold){
        input 'button_1_push_on', 'capability.switch', title: addFieldName('Top "On" button turns on?','button_1_push_on'), multiple: true, submitOnChange:true
        if(numberOfButtons == 4 || numberOfButtons == 5){
            input 'button_2_push_brighten', 'capability.switchLevel', title: addFieldName('"Brighten" button brightens?','button_2_push_brighten'), multiple: true, submitOnChange:true
        }
        if(numberOfButtons == 5){
            input 'button_3_push_toggle', 'capability.switch', title: addFieldName('Center button toggles? (if on, turn off; if off, turn on)','button_3_push_toggle'), multiple: true, submitOnChange:true
        }
        if(numberOfButtons == 4 || numberOfButtons == 5){
            input 'button_4_push_dim', 'capability.switchLevel', title: addFieldName('"Dim" button dims?','button_4_push_dim'), multiple: true, submitOnChange:true
        }
        input 'button_5_push_off', 'capability.switch', title: addFieldName('Bottom ("Off") button turns off?','button_5_push_off'), multiple: true, submitOnChange:true

        if(!replicateHold){
            input 'replicateHold', 'bool', title: addFieldName('Replicating settings for Long Push. Click to customize Hold actions.','replicateHold'), submitOnChange:true, defaultValue: false
        } else {
            input 'replicateHold', 'bool', title: addFieldName('Long Push options shown. Click to replicate from Push.','replicateHold'), submitOnChange:true, defaultValue: false
        }

    } else if(replicateHold){
        input 'button_1_push_on', 'capability.switch', title: addFieldName('Pushing Top "On" button turns on?','button_1_push_on'), multiple: true, submitOnChange:true
        if(numberOfButtons == 4 || numberOfButtons == 5){
            input 'button_2_push_brighten', 'capability.switchLevel', title: addFieldName('Pushing "Brighten" button brightens?','button_2_push_brighten'), multiple: true, submitOnChange:true
        }
        if(numberOfButtons == 5){
            input 'button_3_push_toggle', 'capability.switch', title: addFieldName('Pushing Center button toggles? (if on, turn off; if off, turn on)','button_3_push_toggle'), multiple: true, submitOnChange:true
        }
        if(numberOfButtons == 4 || numberOfButtons == 5){
            input 'button_4_push_dim', 'capability.switchLevel', title: addFieldName('Pushing "Dim" button dims?','button_4_push_dim'), multiple: true, submitOnChange:true
        }
        input 'button_5_push_off', 'capability.switch', title: addFieldName('Pushing Bottom ("Off") buttont turns off?','button_5_push_off'), multiple: true, submitOnChange:true

        if(!replicateHold){
            input 'replicateHold', 'bool', title: addFieldName('Replicating settings for Long Push. Click to customize Hold actions.','replicateHold'), submitOnChange:true, defaultValue: false
        } else {
            input 'replicateHold', 'bool', title: addFieldName('Holding Long Push options shown. Click to replicate from Push.','replicateHold'), submitOnChange:true, defaultValue: false
        }

        input 'button_1_hold_on', 'capability.switch', title: addFieldName('Holding Top "On" button turns on?','button_1_hold_on'), multiple: true, submitOnChange:true
        if(numberOfButtons == 4 || numberOfButtons == 5){
            input 'button_2_hold_brighten', 'capability.switchLevel', title: addFieldName('Holding "Brighten" button brightens?','button_2_hold_brighten'), multiple: true, submitOnChange:true
        }
        if(numberOfButtons == 5){
            input 'button_3_hold_toggle', 'capability.switch', title: addFieldName('Holding Center button toggles? (if on, turn off; if off, turn on)','button_3_hold_toggle'), multiple: true, submitOnChange:true
        }
        if(numberOfButtons == 4 || numberOfButtons == 5){
            input 'button_4_hold_dim', 'capability.switchLevel', title: addFieldName('Holding "Dim" button dims?','button_4_hold_dim'), multiple: true, submitOnChange:true
        }
        input 'button_5_hold_off', 'capability.switch', title: addFieldName('Holding Bottom ("Off") button turns off?','button_5_hold_off'), multiple: true, submitOnChange:true
    }

    if(button_2_push_brighten || button_4_push_dim || button_2_hold_brighten || button_4_hold_dim){
        displayMultiplierOption()
    }
}

def displayMultiDeviceAdvanced(){
    hidden = true
    expandText = '(None selected - Click to expand)'
    if(button_1_push_on || button_1_push_off || button_1_push_dim || button_1_push_brighten || button_1_push_toggle || button_1_push_resume) {
        hidden = false
        expandText = '(Click to expand/collapse)'
    }

    section(hideable: true, hidden: hidden, 'Top button ("On") ' + expandText) {
        button = [1,'on','push']
        getAdvancedSwitchInput(button)

        button = [1,'off','push']
        getAdvancedSwitchInput(button)
        displayError(compareDeviceLists(button,'on'))

        button = [1,'resume','push']
        getAdvancedSwitchInput(button)
        displayError(compareDeviceLists(button,'on'))
        displayError(compareDeviceLists(button,'off'))

        button = [1,'toggle','push']
        getAdvancedSwitchInput(button)
        displayError(compareDeviceLists(button,'on'))
        displayError(compareDeviceLists(button,'off'))
        displayError(compareDeviceLists(button,'resume'))

        button = [1,'dim','push']
        getAdvancedSwitchInput(button)
        displayError(compareDeviceLists(button,'off'))
        displayError(compareDeviceLists(button,'resume'))

        button = [1,'brighten','push']
        getAdvancedSwitchInput(button)
        displayError(compareDeviceLists(button,'off'))
        displayError(compareDeviceLists(button,'dim'))
        displayError(compareDeviceLists(button,'resume'))
    }
    //paragraph error
    if((numberOfButtons == 4 || numberOfButtons == 5) && !error){
        hidden = true
        expandText = 'None selected - Click to expand'
        if(button_2_push_on || button_2_push_off || button_2_push_dim || button_2_push_brighten || button_2_push_toggle || button_2_push_resume) {
            hidden = false
            expandText = 'Click to expand/collapse'
        }

        section(hideable: true, hidden: hidden, '"Brighten" Button (' + expandText + ')') {
            button = [2,'brighten','push']
            getAdvancedSwitchInput(button)

            button = [2,'dim','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'off'))
            displayError(compareDeviceLists(button,'brighten'))

            button = [2,'on','push']
            getAdvancedSwitchInput(button)

            button = [2,'off','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'brighten'))
            displayError(compareDeviceLists(button,'dim'))
            displayError(compareDeviceLists(button,'on'))

            button = [2,'toggle','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'on'))
            displayError(compareDeviceLists(button,'off'))

            button = [2,'resume','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'brighten'))
            displayError(compareDeviceLists(button,'dim'))
            displayError(compareDeviceLists(button,'on'))
            displayError(compareDeviceLists(button,'off'))
            displayError(compareDeviceLists(button,'toggle'))
        }
    }
    if(numberOfButtons == 5 && !error){
        hidden = true
        expandText = 'None selected - Click to expand'
        if(button_3_push_on || button_3_push_off || button_3_push_dim || button_3_push_brighten || button_3_push_toggle || button_3_push_resume) {
            hidden = false
            expandText = 'Click to expand/collapse'
        }
        section(hideable: true, hidden: hidden, 'Middle Button (' + expandText + ')') {
            button = [3,'toggle','push']
            getAdvancedSwitchInput(button)

            button = [3,'resume','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'toggle'))

            button = [3,'on','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'toggle'))
            displayError(compareDeviceLists(button,'resume'))

            button = [3,'off','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'on'))
            displayError(compareDeviceLists(button,'toggle'))
            displayError(compareDeviceLists(button,'resume'))

            button = [3,'dim','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'off'))
            displayError(compareDeviceLists(button,'resume'))

            button = [3,'brighten','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'off'))
            displayError(compareDeviceLists(button,'dim'))
            displayError(compareDeviceLists(button,'resume'))
        }
    }
    if((numberOfButtons == 4 || numberOfButtons == 5) && !error){
        hidden = true
        expandText = 'None selected - Click to expand'
        if(button_4_push_on || button_4_push_off || button_4_push_dim || button_4_push_brighten || button_4_push_toggle || button_4_push_resume) {
            hidden = false
            expandText = 'Click to expand/collapse'
        }
        section(hideable: true, hidden: hidden, '"Dim" Button (' + expandText + ')') {
            button = [4,'dim','push']
            getAdvancedSwitchInput(button)

            button = [4,'brighten','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'dim'))

            button = [4,'on','push']
            getAdvancedSwitchInput(button)

            button = [4,'off','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'dim'))
            displayError(compareDeviceLists(button,'brighten'))
            displayError(compareDeviceLists(button,'on'))

            button = [4,'toggle','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'on'))
            displayError(compareDeviceLists(button,'off'))

            button = [4,'resume','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'dim'))
            displayError(compareDeviceLists(button,'brighten'))
            displayError(compareDeviceLists(button,'on'))
            displayError(compareDeviceLists(button,'off'))
            displayError(compareDeviceLists(button,'resume'))
        }
    }

    if(!error){
        hidden = true
        expandText = 'None selected - Click to expand'
        if(button_5_push_on || button_5_push_off || button_5_push_dim || button_5_push_brighten || button_5_push_toggle || button_5_push_resume) {
            hidden = false
            expandText = 'Click to expand/collapse'
        }
        section(hideable: true, hidden: hidden, 'Bottom Button ("Off") (' + expandText + ')') {
            button = [5,'off','push']
            getAdvancedSwitchInput(button)

            button = [5,'on','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'off'))

            button = [5,'resume','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'on'))
            displayError(compareDeviceLists(button,'off'))

            button = [5,'toggle','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'off'))
            displayError(compareDeviceLists(button,'on'))
            displayError(compareDeviceLists(button,'resume'))

            button = [5,'dim','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'off'))
            displayError(compareDeviceLists(button,'resume'))

            button = [5,'brighten','push']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'off'))
            displayError(compareDeviceLists(button,'dim'))
            displayError(compareDeviceLists(button,'resume'))
        }
    }
    fieldName = 'replicateHold'
    if(!replicateHold && !error){
        fieldTitle = 'Replicating settings for Long Push. Click to customize.'
        section(){
            input fieldName, 'bool', title: addFieldName(fieldTitle,fieldName), submitOnChange:true, defaultValue: false
        }
    } else if(!error) {
        fieldTitle = 'Long Push options shown. Click to replicate from Push.'
        section(){
            input fieldName, 'bool', title: addFieldName(fieldTitle,fieldName), submitOnChange:true, defaultValue: false
        }

        // Advanced Hold
        hidden = true
        expandText = 'None selected - Click to expand'
        if(button_1_hold_on || !button_1_hold_off || button_1_hold_dim || button_1_hold_brighten || button_1_hold_toggle || button_1_hold_resume) {
            hidden = false
            expandText = 'Click to expand/collapse'
        }
        section(hideable: true, hidden: hidden, 'Top button ("On") (' + expandText + ')') {
            button = [1,'on','hold']
            getAdvancedSwitchInput(button)

            button = [1,'off','hold']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'on'))

            button = [1,'resume','hold']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'on'))
            displayError(compareDeviceLists(button,'off'))

            button = [1,'toggle','hold']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'on'))
            displayError(compareDeviceLists(button,'off'))
            displayError(compareDeviceLists(button,'resume'))

            button = [1,'dim','hold']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'off'))
            displayError(compareDeviceLists(button,'resume'))

            button = [1,'brighten','hold']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'off'))
            displayError(compareDeviceLists(button,'dim'))
            displayError(compareDeviceLists(button,'resume'))
        }
        if((numberOfButtons == 4 || numberOfButtons == 5)  && !error){
            hidden = true
            expandText = 'None selected - Click to expand'
            if(button_2_hold_on || button_2_hold_off || button_2_hold_dim || button_2_hold_brighten || button_2_hold_toggle || button_2_hold_resume) {
                hidden = false
                expandText = 'Click to expand/collapse'
            }
            section(hideable: true, hidden: hidden, '"Brighten" Button (' + expandText + ')') {
                button = [2,'brighten','hold']
                getAdvancedSwitchInput(button)

                button = [2,'dim','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'off'))
                displayError(compareDeviceLists(button,'brighten'))

                button = [2,'on','hold']
                getAdvancedSwitchInput(button)

                button = [2,'off','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'brighten'))
                displayError(compareDeviceLists(button,'dim'))
                displayError(compareDeviceLists(button,'on'))

                button = [2,'resume','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'on'))
                displayError(compareDeviceLists(button,'off'))

                button = [2,'toggle','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'brighten'))
                displayError(compareDeviceLists(button,'dim'))
                displayError(compareDeviceLists(button,'on'))
                displayError(compareDeviceLists(button,'off'))
                displayError(compareDeviceLists(button,'resume'))
            }
        }
        if(numberOfButtons == 5 && !error){
            hidden = true
            expandText = 'None selected - Click to expand'
            if(button_3_hold_on || !button_3_hold_off || button_3_hold_dim || button_3_hold_brighten || button_3_hold_toggle || button_3_hold_resume) {
                hidden = false
                expandText = 'Click to expand/collapse'
            }
            section(hideable: true, hidden: hidden, 'Middle Button (' + expandText + ')') {
                button = [3,'resume','hold']
                getAdvancedSwitchInput(button)

                button = [3,'toggle','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'resume'))

                button = [3,'on','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'resume'))
                displayError(compareDeviceLists(button,'toggle'))

                button = [3,'off','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'resume'))
                displayError(compareDeviceLists(button,'toggle'))
                displayError(compareDeviceLists(button,'on'))

                button = [3,'dim','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'resume'))
                displayError(compareDeviceLists(button,'toggle'))
                displayError(compareDeviceLists(button,'on'))
                displayError(compareDeviceLists(button,'off'))

                button = [3,'brighten','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'resume'))
                displayError(compareDeviceLists(button,'toggle'))
                displayError(compareDeviceLists(button,'on'))
                displayError(compareDeviceLists(button,'off'))
                displayError(compareDeviceLists(button,'dim'))
            }
        }
        if((numberOfButtons == 4 || numberOfButtons == 5)  && !error){
            hidden = true
            expandText = 'None selected - Click to expand'
            if(button_4_hold_on || !button_4_hold_off || button_4_hold_dim || button_4_hold_brighten || button_4_hold_toggle || button_4_hold_resume) {
                hidden = false
                expandText = 'Click to expand/collapse'
            }

            section(hideable: true, hidden: hidden, '"Dim" Button (' + expandText + ')') {
                button = [4,'dim','hold']
                getAdvancedSwitchInput(button)

                button = [4,'brighten','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'dim'))

                button = [4,'on','hold']
                getAdvancedSwitchInput(button)

                button = [4,'off','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'dim'))
                displayError(compareDeviceLists(button,'brighten'))
                displayError(compareDeviceLists(button,'on'))

                button = [4,'resume','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'on'))
                displayError(compareDeviceLists(button,'off'))

                button = [4,'toggle','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'dim'))
                displayError(compareDeviceLists(button,'brighten'))
                displayError(compareDeviceLists(button,'on'))
                displayError(compareDeviceLists(button,'off'))
                displayError(compareDeviceLists(button,'resume'))
            }
        }

        if(!error){
            hidden = true
            expandText = 'None selected - Click to expand'
            if(button_5_hold_on || button_5_hold_off || button_5_hold_dim || button_5_hold_brighten || button_5_hold_toggle || button_5_hold_resume) {
                hidden = false
                expandText = 'Click to expand/collapse'
            }
            section(hideable: true, hidden: hidden, 'Bottom Button ("Off") (' + expandText + ')') {
                button = [5,'off','hold']
                getAdvancedSwitchInput(button)

                button = [5,'resume','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'off'))

                button = [5,'on','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'off'))
                displayError(compareDeviceLists(button,'resume'))

                button = [5,'toggle','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'off'))
                displayError(compareDeviceLists(button,'on'))
                displayError(compareDeviceLists(button,'resume'))

                button = [5,'dim','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'off'))
                displayError(compareDeviceLists(button,'resume'))

                button = [5,'brighten','hold']
                getAdvancedSwitchInput(button)
                displayError(compareDeviceLists(button,'off'))
                displayError(compareDeviceLists(button,'dim'))
                displayError(compareDeviceLists(button,'resume'))
            }
        }
    }

    if(!error && (button_1_push_dim || button_1_push_brighten || button_2_push_dim || button_2_push_brighten || button_3_push_dim || button_3_push_brighten || button_4_push_dim || button_4_push_brighten || button_5_push_dim || button_5_push_brighten || button_1_hold_dim || button_1_hold_brighten || button_2_hold_dim || button_2_hold_brighten || button_3_hold_dim || button_3_hold_brighten || button_4_hold_dim || button_4_hold_brighten || button_5_hold_dim || button_5_hold_brighten)){
        section(){
            displayMultiplierOption(true)
        }
    }
    section(){
        if(error) paragraph error + '</div>'
    }
}
// Display functions


def displayLabel(text, width = 12){
    if(!text) return
    paragraph('<div style="background-color:#DCDCDC"><b>' + text + ':</b></div>',width:width)
}

def displayInfo(text,noDisplayIcon = null){
    if(!text) return
    paragraph '<div style="background-color:AliceBlue">' + infoIcon + ' ' + text + '</div>'
}

def displayError(text){
    if(!text) return
    paragraph '<div style="background-color:Bisque">' + errorIcon  + ' ' + text + '</div>'
}

def displayWarning(text){
    if(!text) return
    paragraph '<div style="background-color:LemonChiffon">' + warningIcon  + ' ' + text + '</div>'
}

def highlightText(text){
    if(!text) return
    return '<div style="background-color:Wheat">' + text + '</div>'
}

def formComplete(){
    if(!app.label) return false
    if(!buttonDevice) return false
    if(!numberOfButtons) return false
    if(!settings['multiDevice']) {
        if(settings['customActionsSetup'] && !button_1_push && !button_2_push && !button_3_push && !button_4_push && !button_5_push) return false
        if(!settings['customActionsSetup'] && !controlDevice) return false
    }
    if(inputStartType == 'time' && !inputStartTime) return false
    if(inputStopType == 'time' && !inputStopTime) return false
    if((inputStartType == 'sunrise' || inputStartType == 'sunset') && !inputStartSunriseType) return false
    if((inputStopType == 'sunrise' || inputStopType == 'sunset') && !inputStopSunriseType) return false
    if((inputStartSunriseType == 'before' || inputStartSunriseType == 'after') && !inputStartBefore) return false
    if((inputStopSunriseType == 'before' || inputStopSunriseType == 'after') && !inputStopBefore) return false

    return true
}

// Display functions
def getDeviceCount(device){
    if(!settings[buttonDevice]) return false
    return settings[buttonDevice].size()
}

def getPicoPlural(){
    if(!contactSensorCount) return 'Pico(s)'
    if(contactSensorCount > 1) return 'Picos'
    return 'Pico'
}

def displayNameOption(){
    if(app.label){
        displayLabel('Pico name',2)
        label title: '', required: false, width: 10,submitOnChange:true
    } else {
        displayLabel('Set name for this Pico setup')
        label title: '', required: false, submitOnChange:true
        displayInfo('Name this schedule. Each schedule must have a unique name.')
    }
}

/* ************************************************************************ */
/* TO-DO: Change it so different number of buttons can be used in one setup */
/* but add warnings when selecting to assign button number not on device,   */
/* as well as a general warning.                                            */
/* ************************************************************************ */
def displayPicoOption(){
    fieldName = 'buttonDevice'
    if(settings[fieldName]){
        settings[fieldName].each{
            if(count == 1) multipleDevices = true
            count = 1
        }
        fieldTitle = 'Pico'
        if(settings['multipleDevices']) fieldTitle += 's'
        fieldTitle += ':'
        input fieldName, 'capability.pushableButton', title: addFieldName(fieldTitle,fieldName), multiple: true, submitOnChange:true
    }
    if(!settings[fieldName]){
        fieldTitle = 'Select Pico device(s) (click to select):'
        input fieldName, 'capability.pushableButton', title: addFieldName(fieldTitle,fieldName), defaultValue: 328, multiple: true, submitOnChange:true
        displayInfo('Select which Pico(s) to control. You can select multiple Pico devices, but all should have the same number of buttons.')
    }
}

/* ************************************************************************ */
/* TO-DO: Add errors in setup for defining actions to non-existent buttons. */
/* ************************************************************************ */
def getButtonNumbers(){
    if(!buttonDevice) return
    //Get maximum number of buttons
    /* ************************************************************************ */
    /* TO-DO: Add error trap for if no numberOfButtons is set for device.       */
    /* ************************************************************************ */
    buttonDevice.each{
        if(it.currentValue('numberOfButtons')) {
            if(numberOfButtons){
                if(numberOfButtons < it.currentValue('numberOfButtons')) numberOfButtons = it.currentValue('numberOfButtons')
            }
            if(!numberOfButtons) numberOfButtons = it.currentValue('numberOfButtons')
            return numberOfButtons
        }
        //display error
    }
}

def displayCustomActionsOption(){
    fieldName = 'customActionsSetup'
    if(settings[fieldName]) fieldTitle = highlightText('Allowing custom button actions.') + ' Click to auto-set buttons.'
    if(!settings[fieldName]) fieldTitle = highlightText('Auto-setting buttons.') + ' Click to map buttons to other actions.'
    input fieldName, 'bool', title: addFieldName(fieldTitle,fieldName), submitOnChange:true
}

def displayMultiDeviceOption(){
    fieldName = 'multiDevice'
    if(settings[fieldName]){
        fieldTitle = highlightText('Buttons unique per device') + ' Click for buttons to do the same thing across all devices. (If only controlling one device, leave off.)'
        input fieldName, 'bool', title: addFieldName(fieldTitle,fieldName), submitOnChange:true
        //input fieldName, 'bool', title: '<b>Allow different lights for different buttons.</b> Click for all buttons controlling same light(s).', submitOnChange:true
        //displayInfo('Assign light(s)/switch(es) to each button. Click for the buttons to do the same thing for all devices.')
    }
    if(!settings[fieldName]){
        fieldTitle = highlightText('Buttons do same thing for all devices [multiDevice]')
        input fieldName, 'bool', title: addFieldName(fieldTitle,fieldName), submitOnChange:true
        //input fieldName, 'bool', title: '<b>Same lights for all buttons.</b> Click to assign different device(s) to different buttons.', submitOnChange:true
        //displayInfo('The buttons will do the same thing for all devices. Click to assign light(s)/switch(es) to each button.')
        fieldName = 'controlDevice'
        fieldTitle = 'Device(s) to control <font color="gray">(or change option above to assign different buttons and/or actions to multiple devices)</font>'
        input fieldName, 'capability.switch', title: addFieldName(fieldTitle,fieldName), multiple: true, submitOnChange:true
    }
}



// values sent as list
// values.0 = number
// values.1 = on/off/toggle/dim/brighten
// values.2 = push/hold
// populated = value of the input
def getAdvancedSwitchInput(values,populated = null){
    if(error) return
    fieldName = 'button_' + values[0] + '_' + values[2] + '_' + values[1]
    fieldTitle = ''
    if(populated) text += '<b>'
    if(values[1] == 'on'){
        fieldTitle += 'Turns On'
    } else if(values[1] == 'off'){
        fieldTitle += 'Turns Off'
    } else if(values[1] == 'toggle'){
        fieldTitle += 'Toggles (if on, turn off; if off, turn on)'
    } else if(values[1] == 'dim'){
        fieldTitle += 'Dims'
    } else if(values[1] == 'brighten'){
        fieldTitle += 'Brightens'
    } else if(values[1] == 'resume'){
        fieldTitle += 'Resume schedule(s) (if none, turn off)'
    }
    if(populated) {
        fieldTitle += '</b>'
    } else {
        fieldTitle += ' <font color="gray">(Select devices)</font>'
    }
    if(values[1] == 'dim' || values[1] == 'brighten'){
        switchType = 'switchLevel'
    } else {
        switchType = 'switch'
    }
    input fieldName, 'capability.' + switchType, title: addFieldName(fieldTitle,fieldName), multiple: true, submitOnChange:true
}

def displaySelectActionsButtonOption(number,text,action = 'push'){
    button = action
    fieldName = 'button_' + number + '_' + button
    fieldTitle = action.capitalize() + 'ing ' + text
    if(settings['replicateHold']) {
        button = 'push'
        fieldTitle = 'With '
    }
    fieldTitle += ' button?'
    input fieldName, 'enum', title: addFieldName(fieldTitle,fieldName), multiple: false, options: ['brighten':'brighten','dim':'dim','on':'Turn on', 'off':'Turn off', 'resume': 'Resume schedule (if none, turn off)', 'resume':'resume'], submitOnChange:true
}

/* ************************************************************************ */
/* TO-DO: Convert from a "multiplier" to using a "percentage", for user     */
/* ease.                                                                    */
/* ************************************************************************ */
def displayMultiplierOption(hold = false){
    displayLabel('Set dim and brighten speed')
    displayMultiplierMessage()
    pushedFieldName = 'pushMultiplier'
    pushedFieldTitle = '<b>Push multiplier.</b> (Optional. Default 1.2.)'
    heldFieldName = 'holdMultiplier'
    heldFieldTitle = '<b>Hold multiplier.</b> (Optional. Default 1.4.)'
    if(hold){
        if(button_1_push_dim || button_1_push_brighten || button_2_push_dim || button_2_push_brighten || button_3_push_dim || button_3_push_brighten || button_4_push_dim || button_4_push_brighten || button_5_push_dim || button_5_push_brighten){
            if(button_1_hold_dim || button_1_hold_brighten || button_2_hold_dim || button_2_hold_brighten || button_3_hold_dim || button_3_hold_brighten || button_4_hold_dim || button_4_hold_brighten || button_5_hold_dim || button_5_hold_brighten){
                input pushedFieldName, 'decimal', title: addFieldName(pushedFieldTitle,pushedFieldName), width: 6
                input heldFieldName, 'decimal', title: addFieldName(heldFieldTitle,heldFieldName), width: 6
            } else {
                input pushedFieldName, 'decimal', title: addFieldName(pushedFieldTitle,pushedFieldName), width: 12
            }
        } else if(button_1_hold_dim || button_1_hold_brighten || button_2_hold_dim || button_2_hold_brighten || button_3_hold_dim || button_3_hold_brighten || button_4_hold_dim || button_4_hold_brighten || button_5_hold_dim || button_5_hold_brighten){
            input heldFieldName, 'decimal', title: addFieldName(heldFieldTitle,heldFieldName), width: 12
        }
    } else {
        pushedFieldTitle = 'Multiplier? (Optional. Default 1.2.)'
        if(button_1_hold_dim || button_1_hold_brighten || button_2_hold_dim || button_2_hold_brighten || button_3_hold_dim || button_3_hold_brighten || button_4_hold_dim || button_4_hold_brighten || button_5_hold_dim || button_5_hold_brighten){
            input pushedFieldName, 'decimal', title: addFieldName(pushedFieldTitle,pushedFieldName), width: 6
        } else {
            input pushedFieldName, 'decimal', title: addFieldName(pushedFieldTitle,pushedFieldName), width: 12
        }
    }
}

def displayMultiplierMessage(){
    displayInfo('Multiplier/divider for dimming and brightening, from 1.01 to 99, where higher is faster. For instance, a value of 2 would double (eg from 25% to 50%, then 100%), whereas a value of 1.5 would increase by half each time (eg from 25% to 38% to 57%).')
}

def compareDeviceLists(values,compare){
    // Be sure we have original button and comparison button values (odds are, we don't)
    // eg if(!button_1_push_on)
    if(!settings['button_' + values[0] + '_' + values[2] + '_' + values[1]]) return
    if(!settings['button_' + values[0] + '_' + values[2] + '_' + compare]) return

    settings['button_' + values[0] + '_' + values[2] + '_' + values[1]].each{first->
        settings['button_' + values[0] + '_' + values[2] + '_' + compare].each{second->
            if(first.id == second.id) {
                if(compare == 'on' || compare == 'off'){
                    text1 = 'turn ' + compare
                } else {
                    text1 = compare
                }
                if(values[1] == 'on' || values[1] == 'off'){
                    text2 = 'turn ' + values[1]
                } else {
                    text2 = values[1]
                }
                returnText = 'Can\'t set same button to ' + text1 + ' and ' + text2 + ' the same device.'
            }
        }
    }
    return returnText
}

def displayScheduleSection(){
    if(!settings['buttonDevice']) return

    helpTip = 'Scheduling only applies with ' + getPicoPlural() + '. To schedule the devices or default settings for them, use the Time app.'  

    // If only days entered
    sectionTitle='<b>'
    List dayList=[]
    settings['days'].each{
        dayList.add(it)
    }
    dayText = dayList.join(', ')
    if(!settings['inputStartType'] && !settings['inputStopType'] && settings['days']){
        sectionTitle += 'Only on: ' + dayText + '</b>' + moreOptions
        hidden = true
        // If only start time (and days) entered
    }  else if(checkTimeComplete('start') && settings['inputStartType'] && (!checkTimeComplete('stop') || !settings['inputStopType'])){
        sectionTitle = 'Beginning at ' + varStartTime
        if(settings['days']) sectionTitle += ' on: ' + dayText
        if(settings['months']) sectionTitle += '; in ' + monthText
        sectionTitle += '</b>'
        hidden = false
        // If only stop time (and day) entered
    } else if(checkTimeComplete('stop') && settings['inputStopType'] && (!checkTimeComplete('start') || !settings['inputStartType'])){
        sectionTitle = 'Ending at ' + varStopTime
        if(settings['days']) sectionTitle += ' on: ' + dayText
        if(settings['months']) sectionTitle += '; in ' + monthText
        sectionTitle += '</b>'
        hidden = false
        // If all options entered
    } else if(checkTimeComplete('start') && checkTimeComplete('stop') && settings['inputStartType'] && settings['inputStopType']){
        varStartTime = getTimeVariables('start')
        varStopTime = getTimeVariables('stop')
        sectionTitle = '<b>Only if between ' + varStartTime + ' and ' + varStopTime
        if(settings['days'] && settings['months']) {
            sectionTitle += '</b>'
        } else {
            sectionTitle += ' on: ' + dayText + '</b>'
            if(settings['months']) sectionTitle += '; in ' + monthText
            sectionTitle += '</b>'
        }
        hidden = true
        // If no options are entered
    } else {
        sectionTitle = 'Click to set schedule (optional)'
        hidden = true
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        if(!settings['inputStartType']) displayInfo(helpTip)
        displayStartTypeOption()

        // Display exact time option
        if(settings['inputStartType'] == 'time'){
            displayTimeOption('start')
        } else if(settings['inputStartType']){
            // Display sunrise/sunset type option (at/before/after)
            displaySunriseTypeOption('start')
            // Display sunrise/sunset offset
            if(inputStartSunriseType && inputStartSunriseType != 'at') displaySunriseOffsetOption('start')
        }

        if(checkTimeComplete('start') && settings['inputStartType']){
            displayStopTypeOption()

            // Display exact time option
            if(settings['inputStopType'] == 'time'){
                displayTimeOption('stop')
            } else if(settings['inputStopType']){
                // Display sunrise/sunset type option (at/before/after)
                displaySunriseTypeOption('stop')
                // Display sunrise/sunset offset
                if(inputStopSunriseType && inputStopSunriseType != 'at') displaySunriseOffsetOption('stop')
            }
        }

        displayDaysOption(dayText)
        displayMonthsOption(monthText)

        displayInfo(message)
    }
}

def displayDaysOption(dayText){
    fieldName = 'days'
    fieldTitle = 'On these days (defaults to all days)'
    input fieldName, 'enum', title: addFieldName(fieldTitle,fieldName), multiple: true, width: 12, options: ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday'], submitOnChange:true

    return
}

def displayMonthsOption(monthText){
    fieldName = 'months'
    fieldTitle = 'In these months (defaults to all months)'
    input fieldName, 'enum', title: addFieldName(fieldTitle,fieldName), multiple: true, width: 12, options: ['1': 'January', '2': 'February', '3': 'March', '4': 'April', '5': 'May', '6': 'June', '7': 'July', '8': 'August', '9': 'September', '10': 'October', '11': 'November', '12': 'December'], submitOnChange:true
}

def displayStartTypeOption(){
    if(checkTimeComplete('start') && settings['inputStartType']) displayLabel('Schedule start')
    if(!checkTimeComplete('start')  || !settings['inputStartType']) displayLabel('Schedule starting time')
    fieldName = 'inputStartType'
    if(settings[fieldName]){
        fieldTitle = 'Start time option:'
        if(settings[fieldName] == 'time' || !settings['inputStartSunriseType'] || settings['inputStartSunriseType'] == 'at'){
            width = 6
        } else if(settings['inputStartSunriseType']){
            width = 4
        }
        input fieldName, 'enum', title: addFieldName(fieldTitle,fieldName), multiple: false, width: width, options: ['time':'Start at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)' ], submitOnChange:true
    }
    if(!settings[fieldName]){
        fieldTitle = 'Start time (click to choose option):'
        width = 12
        input fieldName, 'enum', title: addFieldName(fieldTitle,fieldName), multiple: false, width: width, options: ['time':'Start at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)' ], submitOnChange:true
        displayInfo('Select whether to enter a specific time, or have start time based on sunrise and sunset for the Hubitat location. Required field for a schedule.')
    }
}

def displayStopTypeOption(){
    if(!checkTimeComplete('stop')){
        displayLabel('Schedule stopping time')
    } else {
        displayLabel('Schedule stop')
    }
    fieldName = 'inputStopType'
    if(settings[fieldName]){
        fieldTitle = 'Stop time option:'
        if(!settings['inputStopType'] || settings['inputStopType'] == 'none'){
            width = 12
        } else if(settings['inputStopType'] == 'time' || !settings['inputStopSunriseType'] || settings['inputStopSunriseType'] == 'at'){
            width = 6
        } else if(inputStopSunriseType){
            width = 4
        }
        input fieldName, 'enum', title: addFieldName(fieldTitle,fieldName), multiple: false, width: width, options: ['time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)' ], submitOnChange:true
    }
    if(!settings[fieldName]){
        width = 12
        fieldTitle = 'Stop time (click to choose option):'
        input fieldName, 'enum', title: addFieldName(fieldTitle,fieldName), multiple: false, width: width, options: ['time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)' ], submitOnChange:true
    }
}

def displayTimeOption(lcType){
    fieldName = 'input' + ucType + 'Time'
    fieldTitle = ucType + ' time:'
    ucType = lcType.capitalize()
    input fieldName, 'time', title: addFieldName(fieldTitle,fieldName), width: width, submitOnChange:true
    if(!settings[fieldName]) displayInfo('Enter the time to ' + lcType + ' the schedule in "hh:mm AM/PM" format. Required field.')
}

def displaySunriseTypeOption(lcType){
    if(!settings["input${ucType}SunriseType"] || settings["input${ucType}SunriseType"] == 'at') {
        width = 6 
    } else {
        width = 4
    }
    // sunriseTime = getSunriseAndSunset()[settings["input${ucType}Type"]].format('hh:mm a')
    fieldName = 'input' + ucType + 'SunriseType'
    fieldTitle = 'At, before or after ' + settings['input' + ucType + 'Type'] + ':'
    input fieldName, 'enum', title: addFieldName(fieldTitle,fieldName), multiple: false, width: width, options: ['at':'At ' + settings['input' + ucType + 'Type'], 'before':'Before ' + settings['input' + ucType + 'Type'], 'after':'After ' + settings['input' + ucType + 'Type']], submitOnChange:true
    if(!settings[fieldName]) displayInfo('Select whether to start exactly at ' + settings['input' + ucType + 'Type'] + ' (currently, ' + sunriseTime + '). To allow entering minutes prior to or after ' + settings['input' + ucType + 'Type'] + ', select "Before ' + settings['input' + ucType + 'Type'] + '" or "After ' + settings['input' + ucType + 'Type'] + '". Required field.')
}

def checkTimeComplete(lcType){
    ucType = lcType.capitalize()

    // If everything entered
    if((settings['input' + ucType + 'Type'] == 'time' && settings['input' + ucType + 'Type']) || 
       ((settings['input' + ucType + 'Type'] == 'sunrise' || settings['input' + ucType + 'Type'] == 'sunset') && settings['input' + ucType + 'SunriseType'] == 'at') || 
       ((settings['input' + ucType + 'Type'] == 'sunrise' || settings['input' + ucType + 'Type'] == 'sunset') && (settings['input' + ucType + 'SunriseType'] == 'before' || settings['input' + ucType + 'SunriseType'] == 'after') && (settings['input' + ucType + 'Before']))){
        return true
    } else if(!settings['input' + ucType + 'Type'] && !settings['input' + ucType + 'SunriseType'] && !settings['input' + ucType + 'Before']){
        return true
    } else {
        return false
    }
}

def getTimeVariables(lcType){
    ucType = lcType.capitalize()
    type = settings['input' + ucType + 'Type']
    sunriseType = settings['input' + ucType + 'SunriseType']
    before = settings['input' + ucType + 'Before']
    // If time, then set string to "[time]"
    if(type == 'time'){
        return Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings['input' + ucType + 'Time']).format('h:mm a', location.timeZone)
        // If sunrise or sunset
    } else if((type == 'sunrise' || type == 'sunset')  && sunriseType){
        if(sunriseType == 'at'){
            // Set string to "sun[rise/set] ([sunrise/set time])"
            return type + ' (' + getSunriseAndSunset()[type].format('hh:mm a') + ')'
            // If before sunrise
        } else if(type == 'sunrise' && sunriseType == 'before' && before){
            // Set string to "[number] minutes before sunrise ([time])
            if(before) return before + ' minutes ' + sunriseType + ' ' + type + ' (' + getSunriseAndSunset(sunriseOffset: (before * -1), sunsetOffset: 0)[type].format('hh:mm a') + ')'
            // If after sunrise
        } else if(type== 'sunrise' && sunriseType == 'after' && before){
            // Set string to "[number] minutes after sunrise ([time])
            if(before) return before + ' minutes ' + sunriseType + ' ' + type + ' (' + getSunriseAndSunset(sunriseOffset: before, sunsetOffset: 0)[type].format('hh:mm a') + ')'
            // If before sunset
        } else if(type == 'sunset' && sunriseType == 'before' && before){
            // Set string to "[number] minutes before sunset ([time])
            if(before) return before + ' minutes ' + sunriseType + ' ' + type + ' (' + getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: (before * -1))[type].format('hh:mm a') + ')'
            // If after sunrise
        } else if(type == 'sunset' && sunriseType == 'after' && before){
            // Set string to "[number] minutes after sunset ([time])
            if(before) return before + ' minutes ' + sunriseType + ' ' + type + ' (' + getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: before)[type].format('hh:mm a') + ')'
        }
    }
}

def displaySunriseOffsetOption(lcType){
    ucType = lcType.capitalize()
    type = settings['input' + ucType + 'Type']
    sunriseType = settings['input' + ucType + 'SunriseType']
    before = settings['input' + ucType + 'Before']
    if(!sunriseType || sunriseType == 'at') return

    if(before && before > 1441){
        // "Minues [before/after] [sunrise/set] is equal to "
        message = 'Minutes ' + sunriseType + ' ' + type + ' is equal to '
        if(before  > 2881){
            // "X days"
            message += Math.floor(before  / 60 / 24) + " days."
        } else {
            message += 'a day.'
        }
        warningMessage(message)
    }
    fieldName = 'input' + ucType + 'Before'
    fieldTitle = 'Minutes ' + sunriseType + ' ' + type + ':'
    input fieldName, 'number', title: addFieldName(fieldTitle,fieldName), width: 4, submitOnChange:true
    if(!settings[fieldName]) displayInfo('Enter the number of minutes ' + sunriseType + ' ' + type + ' to start the schedule. Required field.')
}

def displayChangeModeOption(){
    if(!settings['contactDevice'] || !settings['deviceType'] || !settings['device'] || !settings['openAction'] || !settings['closeAction']) return

    hidden = true
    if((settings['openMode'] || settings['closeMode']) && (!settings['openMode'] || !settings['closeMode'])) hidden = false

    if(!settings['openMode'] && !settings['closeMode']){
        sectionTitle = 'Click to set Mode change (optional)'
    } else {
        sectionTitle = '<b>'
        if(settings['openMode']) {
            sectionTitle = 'On open, set Mode ' + settings['openMode']
            if(settings['closeMode']) sectionTitle += '<br>'
        }

        if(settings['closeMode']) {
            sectionTitle += 'On close, set Mode ' + settings['closeMode']
        }
        sectionTitle += '</b>'
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        input 'openMode', 'mode', title: 'Set Hubitat\'s "Mode" (on open)?', width: 6, submitOnChange:true
        input 'closeMode', 'mode', title: 'Set Hubitat\'s "Mode" (on close)?', width: 6, submitOnChange:true
    }
}

def addFieldName(text,fieldName){
    if(!fieldName) return
    if(getLogLevel() != 5) return text
    return text + ' [' + fieldName + ']'
}

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

def installed() {
    putLog(1191,'trace', 'Installed')
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    putLog(1197,'trace','Updated')
    unsubscribe()
    initialize()
}

def initialize() {
    state.logLevel = getLogLevel()
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

    subscribe(buttonDevice, 'pushed', buttonPushed)
    subscribe(buttonDevice, 'held', buttonPushed)
    subscribe(buttonDevice, 'released', buttonReleased)

    setTime()

    putLog(1212,'trace','Initialized')
}

def buttonPushed(evt){
    // If not correct day, return nulls
    if(!parent.nowInDayList(settings['days'],app.label)) return
    if(!parent.nowInMonthList(settings['months'],app.label)) return

    // if not between start and stop time, return nulls
    if(atomicState.stop && !parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return

    buttonNumber = evt.value.toInteger()
    numberOfButtons = evt.device.currentValue('numberOfButtons')
    // Treat 2nd button of 2-button Pico as "off" (eg button 5)
    if(buttonNumber == 2 && numberOfButtons == 2) buttonNumber = 5
    if(buttonNumber == 4 && numberOfButtons == 4) buttonNumber = 5
    if(buttonNumber == 3 && numberOfButtons == 4) buttonNumber = 4

    // Needs to be state since we're passing back and forth to parent for progressive dim and brightening
    if(evt.name == 'pushed') atomicState.action = 'push'
    if(evt.name == 'held') atomicState.action = 'hold'
    
    putLog(1234,'trace',atomicState.action.capitalize() + ' button ' + buttonNumber + ' of ' + buttonDevice)
    // Turn on
    switchAction = 'on'
    if(settings['multiDevice']) device = settings['button_' + buttonNumber + '_' + atomicState.action + '_' + switchAction]
    if(!settings['multiDevice']){
        if(settings['customActionsSetup'] && settings['button_' + buttonNumber + '_' + atomicState.action] == switchAction) device = settings['controlDevice']
        if(!settings['customActionsSetup'] && buttonNumber == 1) device = settings['controlDevice']
    }
    parent.updateStateMulti(device,switchAction,app.label)
    parent.setStateMulti(device,app.label)
    if(device) putLog(1244,'trace','Turning on ' + device)
    
    // Turn off
    switchAction = 'off'
    device = ''
    if(settings['multiDevice']) device = settings['button_' + buttonNumber + '_' + atomicState.action + '_' + switchAction]
    if(!settings['multiDevice']){
        if(settings['customActionsSetup'] && settings['button_' + buttonNumber + '_' + atomicState.action] == switchAction) device = settings['controlDevice']
        if(!settings['customActionsSetup'] && buttonNumber == 5) device = settings['controlDevice']
    }
    parent.updateStateMulti(device,switchAction,app.label)
    parent.setStateMulti(device,app.label)
    if(device) putLog(1256,'trace','Turning off ' + device)
    
    // Toggle
    switchAction = 'toggle'
    device = ''
    if(settings['multiDevice']) device = settings['button_' + buttonNumber + '_' + atomicState.action + '_' + switchAction]
    if(!settings['multiDevice']){
        if(settings['customActionsSetup'] && settings['button_' + buttonNumber + '_' + atomicState.action] == switchAction) device = settings['controlDevice']
    }
    parent.updateStateMulti(device,switchAction,app.label)
    parent.setStateMulti(device,app.label)
    if(device) putLog(1267,'trace','Toggling ' + device)
    
    // Resume
    switchAction = 'resume'
    device = ''
    if(settings['multiDevice']) device = settings['button_' + buttonNumber + '_' + atomicState.action + '_' + switchAction]
    if(!settings['multiDevice']){
        if(settings['customActionsSetup'] && settings['button_' + buttonNumber + '_' + atomicState.action] == switchAction) device = settings['controlDevice']
    }
    // checkActiveSchedule doesn't exist
    activeSchedule = parent.checkActiveScheduleMulti(device,app.label)
    if(activeSchedule) {
        parent.updateLevelsMulti(device,['level':['time':'resume'],'temp':['time':'resume'],'hue':['time':'resume'],'sat':['time':'resume']],app.label)
        // This probably isn't right
        if(!rescheduleIncrementalMulti(device,app.label)) parent.updateStateSingle(singleDevice,action,app.label)
    }
    if(!activeSchedule) parent.updateStateMulti(device,'off',app.label)
    parent.setStateMulti(device,app.label)
    if(device) putLog(1285,'trace','Resuming ' + device)
    
    // Brighten
    switchAction = 'brighten'
    device = ''
    if(settings['multiDevice']) device = settings['button_' + buttonNumber + '_' + atomicState.action + '_' + switchAction]
    if(!settings['multiDevice']){
        if(settings['customActionsSetup'] && settings['button_' + buttonNumber + '_' + atomicState.action] == switchAction) device = settings['controlDevice']
        if(!settings['customActionsSetup'] && buttonNumber == 2) device = settings['controlDevice']
    }
    if(atomicState.action == 'push'){
        parent.updateStateMulti(device,'on',app.label)
        device.each{singleDevice->
            setLevel = parent.nextLevel(singleDevice,switchAction,app.label)
            if(setLevel) defaults = ['level': ['startLevel': setLevel, 'appId':'pico']]
        }
        parent.updateLevelsMulti(device,defaults,app.label)
    }
    if(atomicState.action == 'hold') holdDim(device,app.label)
    parent.setStateMulti(device,app.label)
    if(device) putLog(1305,'trace','Brightening ' + device)
    
    // Dim
    switchAction = 'dim'
    device = ''
    if(settings['multiDevice']) device = settings['button_' + buttonNumber + '_' + atomicState.action + '_' + switchAction]
    if(!settings['multiDevice']){
        if(settings['customActionsSetup'] && settings['button_' + buttonNumber + '_' + atomicState.action] == switchAction) device = settings['controlDevice']
        if(!settings['customActionsSetup'] && buttonNumber == 4) device = settings['controlDevice']
    }
    if(atomicState.action == 'push'){
        parent.updateStateMulti(device,'on',app.label)
        device.each{singleDevice->
            setLevel = parent.nextLevel(singleDevice,switchAction,app.label)
            if(setLevel) defaults = ['level': ['startLevel': setLevel, 'appId':'pico']]
        }
        parent.updateLevelsMulti(device,defaults,app.label)
    }
    if(atomicState.action == 'hold') holdDim(device,app.label)
    parent.setStateMulti(device,app.label)
    if(device) putLog(1325,'trace','Dimming ' + device)
}

// place holder until I can redo my pico setups to not throw an error
def buttonHeld(evt){
}

def buttonReleased(evt){
    // If not correct day, return nulls
    if(!parent.nowInDayList(settings['days'],app.label)) return
    if(!parent.nowInMonthList(settings['months'],app.label)) return

    // if not between start and stop time, return nulls
    if(atomicState.stop && !parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return

    buttonNumber = evt.value.toInteger()
    numberOfButtons = evt.device.currentValue('numberOfButtons')

    if (buttonNumber == 2 || (buttonNumber == 4 && (numberOfButtons == 4 || numberOfButtons == 5)) || (buttonNumber == 1 && numberOfButtons == 2)){
        putLog(1344,'trace',"Button $buttonNumber of $buttonDevice released, unscheduling all")
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
    if(atomicState.action == 'push' &&  pushMultiplier){
        return pushMultiplier
    } else if(atomicState.action == 'hold' &&  holdMultiplier){
        return holdMultiplier
    } else if(atomicState.action == 'hold' &&  !pushMultiplier){
        return pushMultiplier
    } else {
        return 1.2
    }
}

// counts number of steps for brighten and dim
// action = 'dim' or 'brighten'
def getSteps(level, action){
    if (action != 'dim' && action != 'brighten'){
        putLog(1371,'error','Invalid value for action "' + action + '" sent to getSteps function')
        return false
    }

    def steps = 0

    // If as already level 1 and dimming or 100 and brightening
    if((action == 'dim' && level < 2) || (action == 'brighten' && level > 99)){
        steps = 0
    }

    //Just step through nextLevel until hit 1 or 100, and tally total times
    if (action == 'dim'){
        if(parent.isNumeric(level)){
            while (level  > 1) {
                steps = steps + 1
                level = parent.nextLevel(level, action,app.getId())
            }
        }
    } else if(action == 'brighten'){
        if(parent.isNumeric(level)){
            while (level  < 100) {
                steps = steps + 1
                level = parent.nextLevel(level, action,app.getId())
            }
        }
    }
    putLog(1398,'debug','Function getSteps returning ' + steps)
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
        putLog(1456,'trace','Function runSetProgressiveLevel returning (no matching device)')
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
            parent.dim('dim',singleDevice,app.id,app.label)
            // If dimming a light that's off, turn it on
            // setStateSingle does that by setting to 1% level, which is what we want
        } else if(!parent.isOn(singleDevice,app.label)){
            //parent.setLevelSingle(1,null,null,null,device,app.label)
            parent.updateStateSingle(singleDevice,'on',app.label)
        } else {
            if(level < 2){
                putLog(1478,'info','Can\'t dim ' + singleDevice + '; already 1%.')
            } else {
                def steps = getSteps(level, 'dim')
                def newLevel

                for(def i = 1; i <= steps; i++) {
                    setLevel = parent.nextLevel(level, 'dim',app.getId())
                    defaults = ['level': ['startLevel': setLevel, 'appId':'pico']]
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
            parent.dim('brighten',singleDevice,'pico',app.label)
            // If brightening a light that's off, turn it on at 1%
        } else if(!parent.isOn(it,app.label)){
            parent.updateStateSingle(singleDevice,'on',app.label)\
            reschedule(it)
        } else {
            if(level > 99){
                putLog(1508,'info','Can\'t brighten ' + it + '; already 100%.')
            } else {
                def steps = getSteps(level, 'brighten')
                def newLevel

                for(def i = 1; i <= steps; i++) {
                    setLevel = parent.nextLevel(level, 'brighten',app.getId())
                    defaults = ['level': ['startLevel': setLevel, 'appId':'pico']]
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
        case 'error':
        if(state.logLevel > 0) return true
        break
        case 'warn':
        if(state.logLevel > 1) return true
        break
        case 'info':
        if(state.logLevel > 2) return true
        break
        case 'trace':
        if(state.logLevel > 3) return true
        break
        case 'debug':
        if(state.logLevel == 5) return true
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,type = 'trace',message = null){
    if(!checkLog(type)) return
    logMessage = ''
    if(type == 'error') logMessage += '<font color="red">'
    if(type == 'warn') logMessage += '<font color="brown">'
    logMessage += app.label + ' '
    if(lineNumber) logMessage += '(line ' + lineNumber + ') '
    if(message) logMessage += '-- ' + message
    if(type == 'error' || type == 'warn') logMessage += '</font>'
    switch(type) {
        case 'error':
        log.error(logMessage)
        return true
        case 'warn':
        log.warn(logMessage)
        return true
        case 'info':
        log.info(logMessage)
        return true
        case 'trace':
        log.trace(logMessage)
        return true
        case 'debug':
        log.debug(logMessage)
        return true
    }
    return
}
