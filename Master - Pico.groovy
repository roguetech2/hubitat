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
*  Version: 0.6.2.7
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

infoIcon = '<img src="http://emily-john.love/icons/information.png" width=20 height=20>'
errorIcon = '<img src="http://emily-john.love/icons/error.png" width=20 height=20>'
warningIcon = '<img src="http://emily-john.love/icons/warning.png" width=20 height=20>'
moreOptions = ' (click for more options)'
expandText = ' (Click to expand/collapse)'

// logLevel sets number of log messages
// 0 for none
// 1 for errors only
// 2 for warnings + errors
// 3 for info + errors + warnings
// 4 for trace + info + errors + warnings
// 5 for debug + trace + info + errors + warnings
def getLogLevel(){
    return 5
}

def displayLabel(text, width = 12){
    if(!text) return
    paragraph('<div style="background-color:#DCDCDC"><b>' + text + ':</b></div>',width:width)
}

def displayInfo(text,noDisplayIcon = null, width=12){
    if(!text) return
    if(noDisplayIcon) paragraph('<div style="background-color:AliceBlue">' + text + '</div>',width:width)
    if(!noDisplayIcon) paragraph('<div style="background-color:AliceBlue">' + infoIcon + ' ' + text + '</div>',width:width)
    helpTip = ''
}

def displayError(text,noDisplayIcon = null, width=12){
    if(!text) return
    if(noDisplayIcon) paragraph('<div style="background-color:Bisque">' + text + '</div>',width:width)
    if(!noDisplayIcon) paragraph('<div style="background-color:Bisque">' + errorIcon  + ' ' + text + '</div>',width:width)
    errorMessage = ''
}

def displayWarning(text,noDisplayIcon = null, width=12){
    if(!text) return
    if(noDisplayIcon) paragraph('<div style="background-color:LemonChiffon">' + text + '</div>',width:width)
    if(noDisplayIcon) paragraph('<div style="background-color:LemonChiffon">' + warningIcon  + ' ' + text + '</div>',width:width)
    warningMessage = ''
}

def highlightText(text, width=12){
    if(!text) return
    return '<div style="background-color:Wheat">' + text + '</div>'
}

def addFieldName(text,fieldName){
    if(!fieldName) return
    if(getLogLevel() != 5) return text
    return text + ' [' + fieldName + ']'
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
            //if(device) numberOfButtons = getButtonNumbers()
            // Multi + advanced needs section closed
            if(settings['multiDevice'] && settings['customActionsSetup']){
                section(){
                    displayNameOption()
                    displayPicoOption()
                    displayMultiDeviceOption()
                    displayCustomActionsOption()
                    if(settings['device']) paragraph '<div style="background-color:BurlyWood"><b> Select what to do for each Pico action:</b></div>'
                }
                displayMultiDeviceAdvanced()
            }
            if(!settings['multiDevice'] || !settings['customActionsSetup']){
                section(){
                    displayNameOption()
                    displayPicoOption()
                    displayMultiDeviceOption()
                    if(!settings['multiDevice']) {
                        if(controlDevice) displayCustomActionsOption()
                        displaySingleDevice()
                    }
                    if(numberOfButtons && settings['multiDevice']) {
                        displayCustomActionsOption()
                        displayMultiDeviceSimple()
                    }
                }
            }
            displayScheduleSection()
            displayPeopleOption()
            displayIfModeOption()
        }
    }
}

def displaySingleDevice(){
    if(!numberOfButtons) return
    if(!controlDevice) return
    if(settings['multiDevice']) return

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
    if(!settings['device']) return
    hidden = true
    if(button_1_push_on || button_1_push_off || button_1_push_dim || button_1_push_brighten || button_1_push_toggle || button_1_push_resume) {
        hidden = false
    }

    section(hideable: true, hidden: hidden, 'Top button ("On")' + expandText) {
        buttonMap = ['on','off','resume','toggle','dim','brighten']
        displayMultiDeviceButtons(1,buttonMap)
    }
    //paragraph error
    if((numberOfButtons == 4 || numberOfButtons == 5) && !error){
        hidden = true
        if(button_2_push_on || button_2_push_off || button_2_push_dim || button_2_push_brighten || button_2_push_toggle || button_2_push_resume) {
            hidden = false
        }

        section(hideable: true, hidden: hidden, '"Brighten" Button' + expandText) {
            buttonMap = ['brighten','dim','on','off','toggle','resume']
            displayMultiDeviceButtons(2,buttonMap)
        }
    }
    if(numberOfButtons == 5 && !error){
        hidden = true
        if(button_3_push_on || button_3_push_off || button_3_push_dim || button_3_push_brighten || button_3_push_toggle || button_3_push_resume) {
            hidden = false
        }
        section(hideable: true, hidden: hidden, 'Middle Button' + expandText) {
            buttonMap = ['toggle','resume','on','off','dim','brighten']
            displayMultiDeviceButtons(3,buttonMap)
        }
    }
    if((numberOfButtons == 4 || numberOfButtons == 5) && !error){
        hidden = true
        if(button_4_push_on || button_4_push_off || button_4_push_dim || button_4_push_brighten || button_4_push_toggle || button_4_push_resume) {
            hidden = false
        }
        section(hideable: true, hidden: hidden, '"Dim" Button' + expandText) {
            buttonMap = ['dim','brighten','on','off','toggle','resume']
            displayMultiDeviceButtons(4,buttonMap)
        }
    }

    if(!error){
        hidden = true
        if(button_5_push_on || button_5_push_off || button_5_push_dim || button_5_push_brighten || button_5_push_toggle || button_5_push_resume) {
            hidden = false
        }
        section(hideable: true, hidden: hidden, 'Bottom Button ("Off")' + expandText) {
            buttonMap = ['off','on','resume','toggle','dim','brighten']
            displayMultiDeviceButtons(5,buttonMap)
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
        if(button_1_hold_on || !button_1_hold_off || button_1_hold_dim || button_1_hold_brighten || button_1_hold_toggle || button_1_hold_resume) {
            hidden = false
        }
        section(hideable: true, hidden: hidden, 'Top button ("On")' + expandText) {
            buttonMap = ['on','off','resume','toggle','dim','brighten']
            displayMultiDeviceButtons(1,buttonMap,'hold')
        }
        if((numberOfButtons == 4 || numberOfButtons == 5)  && !error){
            hidden = true
            if(button_2_hold_on || button_2_hold_off || button_2_hold_dim || button_2_hold_brighten || button_2_hold_toggle || button_2_hold_resume) {
                hidden = false
            }
            section(hideable: true, hidden: hidden, '"Brighten" Button' + expandText) {
                buttonMap = ['brighten','dim','on','off','resume','toggle']
                displayMultiDeviceButtons(2,buttonMap,'hold')
            }
        }
        if(numberOfButtons == 5 && !error){
            hidden = true
            if(button_3_hold_on || !button_3_hold_off || button_3_hold_dim || button_3_hold_brighten || button_3_hold_toggle || button_3_hold_resume) {
                hidden = false
            }
            section(hideable: true, hidden: hidden, 'Middle Button' + expandText) {
                buttonMap = ['resume','toggle','on','off','dim','brighten']
                displayMultiDeviceButtons(3,buttonMap,'hold')
            }
        }
        if((numberOfButtons == 4 || numberOfButtons == 5)  && !error){
            hidden = true
            if(button_4_hold_on || !button_4_hold_off || button_4_hold_dim || button_4_hold_brighten || button_4_hold_toggle || button_4_hold_resume) {
                hidden = false
            }

            section(hideable: true, hidden: hidden, '"Dim" Button' + expandText) {
                buttonMap = ['dim','brighten','on','off','resume','toggle']
                displayMultiDeviceButtons(4,buttonMap,'hold')
            }
        }

        if(!error){
            hidden = true
            if(button_5_hold_on || button_5_hold_off || button_5_hold_dim || button_5_hold_brighten || button_5_hold_toggle || button_5_hold_resume) {
                hidden = false
            }
            section(hideable: true, hidden: hidden, 'Bottom Button ("Off")' + expandText) {
                buttonMap = ['off','resume','on','toggle','dim','brighten']
                displayMultiDeviceButtons(5,buttonMap,'hold')
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

def formComplete(){
    if(!app.label) return false
    if(!settings['device']) return false
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
    if(!settings['device']) return false
    return settings['device'].size()
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
        displayInfo('Name this Pico. Each Pico app must have a unique name.')
    }
}

/* ************************************************************************ */
/* TO-DO: Change it so different number of buttons can be used in one setup */
/* but add warnings when selecting to assign button number not on device,   */
/* as well as a general warning.                                            */
/* ************************************************************************ */
def displayPicoOption(){
    fieldName = 'device'
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
    if(!settings['device']) return
    //Get maximum number of buttons
    /* ************************************************************************ */
    /* TO-DO: Add error trap for if no numberOfButtons is set for device.       */
    /* ************************************************************************ */
    settings['device'].each{
        if(it.currentValue('numberOfButtons')) {
            if(numberOfButtons){
                if(numberOfButtons < it.currentValue('numberOfButtons')) numberOfButtons = it.currentValue('numberOfButtons').toInteger()
            }
            if(!numberOfButtons) numberOfButtons = it.currentValue('numberOfButtons').toInteger()
        }
        //display error
    }
    return numberOfButtons
}

def displayCustomActionsOption(){
    if(!settings['device']) return 
    if(!numberOfButtons) return
    fieldName = 'customActionsSetup'
    if(settings[fieldName]) fieldTitle = highlightText('Allowing custom button actions.') + ' Click to auto-set buttons.'
    if(!settings[fieldName]) fieldTitle = highlightText('Auto-setting buttons.') + ' Click to map buttons to other actions.'
    input fieldName, 'bool', title: addFieldName(fieldTitle,fieldName), submitOnChange:true
}

def displayMultiDeviceOption(){
    if(!numberOfButtons) return
    fieldName = 'multiDevice'
    if(settings[fieldName]){
        fieldTitle = highlightText('Buttons unique per device') + ' Click for buttons to do the same thing across all devices. (If only controlling one device, leave off.)'
        fieldTitle = addFieldName(fieldTitle,fieldName)
        input fieldName, 'bool', title: fieldTitle, submitOnChange:true
        //input fieldName, 'bool', title: '<b>Allow different lights for different buttons.</b> Click for all buttons controlling same light(s).', submitOnChange:true
        //displayInfo('Assign light(s)/switch(es) to each button. Click for the buttons to do the same thing for all devices.')
    }
    if(!settings[fieldName]){
        fieldTitle = highlightText('Buttons do same thing for all devices')
        fieldTitle = addFieldName(fieldTitle,fieldName)
        input fieldName, 'bool', title: fieldTitle, submitOnChange:true
        //input fieldName, 'bool', title: '<b>Same lights for all buttons.</b> Click to assign different device(s) to different buttons.', submitOnChange:true
        //displayInfo('The buttons will do the same thing for all devices. Click to assign light(s)/switch(es) to each button.')
        fieldName = 'controlDevice'
        fieldTitle = 'Device(s) to control <font color="gray">(or change option above to assign different buttons and/or actions to multiple devices)</font>'
        fieldTitle = addFieldName(fieldTitle,fieldName)
        input fieldName, 'capability.switch', title: fieldTitle, multiple: true, submitOnChange:true
    }
}

def displayMultiDeviceButtons(buttonNumber, buttonMap, action = 'push') {
    for (mainLoopNumber in 0..4) {
        button = [buttonNumber, buttonMap[mainLoopNumber], action]
            getAdvancedSwitchInput(button)

        if(mainLoopNumber != 0){
            if (buttonMap[mainLoopNumber] == 'on') {
                for (errorLoopNumber in 0..mainLoopNumber - 1) {
                    if (buttonMap[errorLoopNumber] in ['off', 'resume', 'toggle']) {
                        displayError(compareDeviceLists(button, buttonMap[errorLoopNumber]))
                    }
                }
            }
            
            if (buttonMap[mainLoopNumber] in ['off', 'resume']) {
                for (errorLoopNumber in 0..mainLoopNumber - 1) {
                    displayError(compareDeviceLists(button, buttonMap[errorLoopNumber]))
                }
            }
            
            if (buttonMap[mainLoopNumber] == 'toggle') {
                for (errorLoopNumber in 0..mainLoopNumber - 1) {
                    if (buttonMap[errorLoopNumber] in ['off', 'on', 'toggle']) {
                        displayError(compareDeviceLists(button, buttonMap[errorLoopNumber]))
                    }
                }
            }
            
            if (buttonMap[mainLoopNumber] == 'dim') {
                for (errorLoopNumber in 0..mainLoopNumber - 1) {
                    if (buttonMap[errorLoopNumber] in ['brighten', 'off', 'toggle', 'resume']) {
                        displayError(compareDeviceLists(button, buttonMap[errorLoopNumber]))
                    }
                }
            }
            
            if (buttonMap[mainLoopNumber] == 'brighten') {
                for (errorLoopNumber in 0..mainLoopNumber - 1) {
                    if (buttonMap[errorLoopNumber] in ['dim', 'off', 'toggle', 'resume']) {
                        displayError(compareDeviceLists(button, buttonMap[errorLoopNumber]))
                    }
                }
            }
        }
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

def validateTimes(type){
    if(settings['start_timeType'] && !settings['stop_timeType']) return false
    if(type == 'stop' && settings['stop_timeType'] == 'none') return true
    if(settings[type + '_timeType'] == 'time' && !settings[type + '_time']) return false
    if(settings[type + '_timeType'] == 'sunrise' && !settings[type + '_sunType']) return false
    if(settings[type + '_timeType'] == 'sunset' && !settings[type + '_sunType']) return false
    if(settings[type + '_sunType'] == 'before' && !settings[type + '_sunOffset']) return false
    if(settings[type + '_sunType'] == 'after' && !settings[type + '_sunOffset']) return false
    if(!validateSunriseMinutes(type)) return false
    return true
}

def validateSunriseMinutes(type){
    if(!settings[type + '_sunOffset']) return true
    if(settings[type + '_sunOffset'] > 719) return false
    return true
}

def displayScheduleSection(){
    if(!settings['device']) return
    
    List dayList=[]
    settings['days'].each{
        dayList.add(it)
    }
    dayText = dayList.join(', ')
    List monthList=[]
    settings['months'].each{
        monthList.add(Date.parse('MM',it).format('MMMM'))

    }
    monthText = monthList.join(', ')
    
    hidden = true
    if(settings['start_timeType'] && !settings['stop_timeType']) hidden = false
    if(settings['disable']) hidden = true
    if(settings['start_time'] && settings['start_time'] == settings['stop_time']) hidden = false
    if(!validateTimes('start')) hidden = false
    if(!validateTimes('stop')) hidden = false

    section(hideable: true, hidden: hidden, getTimeSectionTitle()){
        if(settings['start_time'] && settings['start_time'] == settings['stop_time']) displayError('You can\'t have the same time to start and stop.')

        displayTypeOption('start')
        displayTimeOption('start')
        displaySunriseTypeOption('start')
        displayTypeOption('stop')
        displayTimeOption('stop')
        displaySunriseTypeOption('stop')
        displayDaysOption()
        displayMonthsOption()
    }
}

def getTimeSectionTitle(){
    if(!settings['start_timeType'] && !settings['stop_timeType'] && !settings['days'] && !settings['months']) return 'Click to set schedule (optional)'

    if(settings['start_timeType']) sectionTitle = '<b>Starting: '
    if(settings['start_timeType'] == 'time' && settings['start_time']) sectionTitle += 'At ' + Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings['start_time']).format('h:mm a', location.timeZone)
    if(settings['start_timeType'] == 'time' && !settings['start_time']) sectionTitle += 'At specific time '
    if(settings['start_timeType'] == 'sunrise' || settings['start_timeType'] == 'sunset'){
        if(!settings['start_sunType']) sectionTitle += 'Based on ' + settings['start_timeType']
        if(settings['start_sunType'] == 'at') sectionTitle += 'At ' + settings['start_timeType']
        if(settings['start_sunOffset']) sectionTitle += ' ' + settings['start_sunOffset'] + ' minutes '
        if(settings['start_sunType'] && settings['start_sunType'] != 'at') sectionTitle += settings['start_sunType'] + ' ' + settings['start_timeType']
        if(validateTimes('start')) sectionTitle += ' ' + getSunriseTime(settings['start_timeType'],settings['start_sunOffset'],settings['start_sunType'])
    }

    if(settings['start_timeType'] && settings['days']) sectionTitle += ' on: ' + dayText
    if(settings['start_timeType'] && settings['months'] && settings['days']) sectionTitle += ';'
    if(settings['start_timeType'] && settings['months']) sectionTitle += ' in ' + monthText
    if(settings['start_timeType']) sectionTitle += '</b>'
    if(!settings['days'] || !settings['months']) sectionTitle += moreOptions
    
    if(!settings['start_timeType'] && !settings['stop_timeType']) return sectionTitle

    sectionTitle += '</br>'
    if(settings['stop_timeType'] && settings['stop_timeType'] == 'none') return sectionTitle + '<b>No end</b>'
    if(settings['stop_timeType'] && settings['stop_timeType'] != 'none') sectionTitle += '<b>Stopping: '
    if(settings['stop_timeType'] == 'time' && settings['stop_time']) sectionTitle += 'At ' + Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings['stop_time']).format('h:mm a', location.timeZone)
    if(settings['stop_timeType'] == 'time' && !settings['stop_time']) sectionTitle += 'At specific time '
    if(settings['stop_timeType'] == 'sunrise' || settings['stop_timeType'] == 'sunset'){
        if(!settings['stop_sunType']) sectionTitle += 'Based on ' + settings['stop_timeType']
        if(settings['stop_sunType'] == 'at') sectionTitle += 'At ' + settings['stop_timeType']
        if(settings['stop_sunOffset']) sectionTitle += settings['stop_sunOffset'] + ' minutes '
        if(settings['stop_sunType'] && settings['stop_sunType'] != 'at') sectionTitle += settings['stop_sunType'] + ' ' + settings['stop_timeType']
        if(stopTimeComplete) sectionTitle += ' ' + getSunriseTime(settings['stop_timeType'],settings['stop_sunOffset'],settings['stop_sunType'])
    }

    if(settings['start_timeType']) return sectionTitle + '</b>'
}

def displayTypeOption(type){
    if(type == 'stop' && !validateTimes('start')) return
    
    ingText = type
    if(type == 'stop') ingText = 'stopp'
    
    labelText = 'Schedule ' + type
    if(validateTimes('start')) labelText = ''
    if(type == 'start' && !validateTimes('start') || !settings[type + '_timeType']) labelText = ''
    if(!validateTimes('start') || !settings[type + '_timeType']) labelText = 'Schedule ' + ingText + 'ing time'
    
    if(labelText) displayLabel(labelText)

    if(!validateSunriseMinutes(type)) displayWarning('Time ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType'] + ' is ' + (Math.round(settings[type + '_sunOffset']) / 60) + ' hours. That\'s probably wrong.')
    
    fieldName = type + '_timeType'
    fieldTitle = type.capitalize() + ' time option:'
    if(!settings[type + '_timeType']){
        fieldTitle = type.capitalize() + ' time?'
        if(type == 'stop') fieldTitle += ' (Select "Don\'t stop" for none)'
        highlightText(fieldTitle)
    }
    fieldTitle = addFieldName(fieldTitle,fieldName)
    fielList = ['time':'Start at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
    if(type == 'stop') fielList = ['none':'Don\'t stop','time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
    input fieldName, 'enum', title: fieldTitle, multiple: false, width: getTypeOptionWidth(type), options: fielList, submitOnChange:true
    if(!settings['start_timeType']) displayInfo('Select whether to enter a specific time, or have start time based on sunrise and sunset for the Hubitat location. Required.')
}

def displayTimeOption(type){
    if(type == 'stop' && !validateTimes('start')) return
    if(type == 'stop' && settings['stop_timeType'] == 'none') return
    if(settings[type + '_timeType'] != 'time') return
    
    fieldName = type + '_time'
    fieldTitle = type.capitalize() + ' time:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(!settings[fieldName]) fieldTitle = highlightText(fieldTitle)
    input fieldName, 'time', title: fieldTitle, width: getTypeOptionWidth(type), submitOnChange:true
    if(!settings[fieldName]) displayInfo('Enter the time to ' + type + ' the schedule in "hh:mm AM/PM" format. Required.')
}

def getTypeOptionWidth(type){
    if(!settings[type + '_timeType']) return 12
    if(type == 'stop' && settings[type + '_timeType'] == 'none') return 12
    if(settings[type + '_sunType'] && settings[type + '_sunType'] != 'at') return 4
    return 6
}

def displaySunriseTypeOption(type){
    if(!settings[type + '_timeType']) return
    if(settings[type + '_timeType'] == 'time') return
    if(type == 'stop' && settings['stop_timeType'] == 'none') return
    if(type == 'stop' && !validateTimes('start')) return
    if(settings[type + '_timeType'] != 'sunrise' && settings[type + '_timeType'] != 'sunset') return
    
    sunTime = getSunriseAndSunset()[settings[type + '_timeType']].format('hh:mm a')

    fieldName = type + '_sunType'
    fieldTitle = 'At, before or after ' + settings[type + '_timeType'] + ' (' + sunTime + '):'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(!settings[fieldName]) fieldTitle = highlightText(fieldTitle)
    input fieldName, 'enum', title: fieldTitle, multiple: false, width: getTypeOptionWidth(type), options: ['at':'At ' + settings[type + '_timeType'], 'before':'Before ' + settings[type + '_timeType'], 'after':'After ' + settings[type + '_timeType']], submitOnChange:true
    
    if(!settings[fieldName]) displayInfo('Select whether to start exactly at ' + settings[type + '_timeType'] + ' (currently ' + sunTime + '). To allow entering minutes prior to or after ' + settings[type + '_timeType'] + ', select "Before ' + settings[type + '_timeType'] + '" or "After ' + settings[type + '_timeType'] + '". Required.')
    displaySunriseOffsetOption(type)
}

def getSunriseTime(type,sunOffset,sunriseType){
    if(type == 'sunrise' && sunriseType == 'before' && sunOffset) return '(' + new Date(parent.getSunrise(sunOffset * -1)).format('hh:mm a') + ')'
    if(type == 'sunrise' && sunriseType == 'after' && sunOffset) return '(' + new Date(parent.getSunrise(sunOffset)).format('hh:mm a') + ')'
    if(type == 'sunset' && sunriseType == 'before' && sunOffset) return '(' + new Date(parent.getSunset(sunOffset * -1)).format('hh:mm a') + ')'
    if(type == 'sunset' && sunriseType == 'after' && sunOffset) return '(' + new Date(parent.getSunset(sunOffset)).format('hh:mm a') + ')'
    if(type == 'sunrise' && sunriseType == 'at') return '(' + new Date(parent.getSunrise(0)).format('hh:mm a') + ')'
    if(type == 'sunset' && sunriseType == 'at') return '(' + new Date(parent.getSunset(0)).format('hh:mm a') + ')'   
}

def displaySunriseOffsetOption(type){
    if(type == 'stop' && !validateTimes('start')) return
    if(type == 'stop' && settings['stop_timeType'] == 'none') return
    if(!settings[type + '_sunType']) return
    if(settings[type + '_sunType'] == 'at') return

    fieldName = type + '_sunOffset'
    fieldTitle = 'Minutes ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType'] + ':'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'number', title: fieldTitle, width: getTypeOptionWidth(type), submitOnChange:true
    
    message = 'Enter the number of minutes ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType'] + ' to start the schedule. Required.'
    if(!settings[type + '_sunOffset']) displayInfo(message)
    if(!validateSunriseMinutes(type)) displayWarning(message)
}

def displayDaysOption(){
    if(!settings['start_timeType']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    
    fieldName = 'days'
    fieldTitle = 'On these days (optional; defaults to all days):'
    if(!settings[fieldName]) fieldTitle = 'On which days (optional; defaults to all days)?'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'enum', title: fieldTitle, multiple: true, width: 12, options: ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday'], submitOnChange:true
}

def displayMonthsOption(){
    if(!settings['start_timeType']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    
    fieldName = 'months'
    fieldTitle = 'In these months (optional; defaults to all months):'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'enum', title: fieldTitle, multiple: true, width: 12, options: ['1': 'January', '2': 'February', '3': 'March', '4': 'April', '5': 'May', '6': 'June', '7': 'July', '8': 'August', '9': 'September', '10': 'October', '11': 'November', '12': 'December'], submitOnChange:true
}

def displayIfModeOption(){
    if(!settings['device']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return

    sectionTitle = 'Click to select with what Mode (optional)'
    if(settings['ifMode']) sectionTitle = '<b>Only with Mode: ' + settings['ifMode'] + '</b>'

    section(hideable: true, hidden: true, sectionTitle){
        input 'ifMode', 'mode', title: 'Only run if Mode is already?', width: 12, submitOnChange:true

        message = 'This will limit the ' + pluralContact + ' from running to only when Hubitat\'s Mode is as selected.'
        if(settings['ifMode']) message = 'This will limit the ' + pluralContact + ' from running to only when Hubitat\'s Mode is ' + settings['ifMode'] + '.'

        displayInfo(message)
    }
}

def displayPeopleOption(){
    if(!settings['device']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return

    List peopleList1=[]
    settings['personHome'].each{
        peopleList1.add(it)
    }
    withPeople = peopleList1.join(', ')
 
    List peopleList2 = []
    settings['personNotHome'].each{
        peopleList2.add(it)
    }
    withoutPeople = peopleList2.join(', ')
    
    hidden = true
    if(peopleError) hidden = false
    
    if(!settings['personHome'] && !settings['personNotHome']) sectionTitle = 'Click to select people (optional)'
    if(settings['personHome']) sectionTitle = '<b>With: ' + withPeople + '</b>'
    if(settings['personHome'] && settings['personNotHome']) sectionTitle += '<br>'
    if(settings['personNotHome']) sectionTitle += '<b>Without: ' + withoutPeople + '</b>'

    section(hideable: true, hidden: hidden, sectionTitle){
        if(peopleError) displayError('You can\'t include and exclude the same person.')

        input 'personHome', 'capability.presenceSensor', title: 'Only if any of these people are home (Optional)', multiple: true, submitOnChange:true
        input 'personNotHome', 'capability.presenceSensor', title: 'Only if all these people are NOT home (Optional)', multiple: true, submitOnChange:true
    }
}

def displayChangeModeOption(){
    if(!settings['device']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return

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

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

def installed() {
    putLog(1016,'trace', 'Installed')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    putLog(1022,'trace','Updated')
    unsubscribe()
    initialize()
}

def initialize() {
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))

    subscribe(settings['device'], 'pushed', buttonPushed)
    subscribe(settings['device'],, 'held', buttonPushed)
    subscribe(settings['device'],, 'released', buttonReleased)

    setTime()

    putLog(1036,'trace','Initialized')
}

def buttonPushed(evt){
    // If not correct day, return nulls
    if(!parent.checkNowInDayList(settings['days'],app.label)) return
    if(!parent.checkNowInMonthList(settings['months'],app.label)) return

    // if not between start and stop time, return nulls
    if(atomicState.stop && !parent.checkNowBetweenTimes(atomicState.start, atomicState.stop, app.label)) return

    buttonNumber = assignButtonNumber(evt.value.toInteger())

    // Needs to be state since we're passing back and forth to parent for progressive dim and brightening
    if(evt.name == 'pushed') atomicState.action = 'push'
    if(evt.name == 'held') atomicState.action = 'hold'
    
    putLog(1053,'trace',atomicState.action.capitalize() + ' button ' + buttonNumber + ' of ' + device)

    switchActions = ['on', 'brighten', 'dim', 'off', 'resume', 'toggle']

    switchActions.each { switchAction ->
        device = getControlDeviceFromButton(switchAction,buttonNumber)
        device.each{singleDevice->
            stateMap = parent.getStateMapSingle(singleDevice,switchAction,app.id,app.label)       // on, off, toggle
            
            // need to get nextLevel here
            if(atomicState.action == 'push') level = parent._getNextLevelDimmable(singleDevice, switchAction, app.label)
            levelMap = parent.getLevelMap(type,level,app.id,'',childLabel)         // dim, brighten
            fullMap = parent.addMaps(stateMap, levelMap)
            parent.mergeMapToTable(singleDevice.id,fullMap,app.label)
        }
        if(action == 'resume') parent.resumeDeviceScheduleMulti(device,app.label)       //??? this function needs to be rewritten, I think
        if(atomicState.action == 'hold') holdNextLevelMulti(device,switchAction)

        if(settings['multiDevice']) parent.setDeviceMulti(device,app.label)
        device = ''
    }
    
    if(!settings['multiDevice']) parent.setDeviceMulti(settings['controlDevice'],app.label)
}

// place holder until I can redo my pico setups to not throw an error
def buttonHeld(evt){
}

def buttonReleased(evt){
    buttonNumber = assignButtonNumber(evt.value.toInteger())
    log.debug buttonNumber

        putLog(1085,'trace','Button ' + buttonNumber + ' of ' + device + ' released, unscheduling all')
        unschedule()
}

def assignButtonNumber(originalButton){
    numberOfButtons = getButtonNumbers()
    log.debug numberOfButtons + ' ' + originalButton
    // Treat 2nd button of 2-button Pico as "off" (eg button 5)
    if(originalButton == 2 && numberOfButtons == 2) return 5
    if(originalButton == 4 && numberOfButtons == 4) return 5
    if(originalButton == 3 && numberOfButtons == 4) return 4
    return originalButton


}

def getControlDeviceFromButton(action,buttonNumber){
    switchActions = ['on', 'brighten', 'dim', 'off', 'resume', 'toggle']
    buttonNumbers = [1, 2, 4, 5, null, null]

    switchActionToButtonNumber = [:]

    for (int i = 0; i < switchActions.size(); i++) {
        switchActionToButtonNumber[switchActions[i]] = buttonNumbers[i]
    }
    if(settings['multiDevice']) return settings['button_' + buttonNumber + '_' + atomicState.action + '_' + action]
 
    if(settings['customActionsSetup'] && settings['button_' + buttonNumber + '_' + atomicState.action] == action) return settings['controlDevice']
    if(!switchActionToButtonNumber[action]) return
    if(!settings['customActionsSetup'] && buttonNumber == switchActionToButtonNumber[action]) return settings['controlDevice']
}
/* ************************************************************************ */
/* TO-DO: Retest "multiplier" functionality (also in MagicCube). Make sure  */
/* it's implemented in the UI, and it carries through in the logic. Also    */
/* rename the variables. "Multiplier" is just stupid. And change it to a    */
/* "percentage" for user ease.                                                */
/* ************************************************************************ */
def getDimSpeed(){
    if(atomicState.action == 'push') return pushMultiplier
    if(atomicState.action == 'hold' &&  holdMultiplier) return holdMultiplier
    if(atomicState.action == 'hold' &&  pushMultiplier) return pushMultiplier
}

// This is the schedule function that sets the level for progressive dimming
def runSetProgressiveLevel(data){
    if(!settings['multiDevice']) return settings['controlDevice']
    if(!getSetProgressiveLevelDevice(data.device, data.action)) {
        putLog(1129,'trace','Function runSetProgressiveLevel returning (no matching device)')
        return
    }
    holdNextLevelSingle(singleDevice,action)
}

def getSetProgressiveLevelDevice(deviceId, action){
    if(!settings['multiDevice']) return settings['controlDevice']
    for (int i = 1; i <= 5; i++) {
        if(action == 'dim'){
            button_${i}_hold_dim.each{
                if (it.id == deviceId) returnValue = it
            }
        }
        if(action == 'brighten'){
            button_${i}_hold_brighten.each{
                if (it.id == deviceId) returnValue = it
            }
        }
    }
    return returnValue
}
// Has to be in child app for schedule
def holdNextLevelMulti(multiDevice,action){
    if(!multiDevice) return
    if(action != 'dim' && action != 'brighten') return

    device.each{singleDevice->
        holdNextLevelSingle(singleDevice,action)
    }
}

// Has to be in child app for schedule
def holdNextLevelSingle(singleDevice,action){
    if(!parent.checkIsDimmable(singleDevice,app.label)) return
    level = parent._getNextLevelDimmable(singleDevice, action, app.label)
    if(!level) return
    levelMap = parent.getLevelMap(type,level,app.id,'',childLabel)         // dim, brighten
    parent.mergeMapToTable(singleDevice.id,levelMap,app.label)
    
    parameters = '[device: it.id, action: action]'
    parent.scheduleChildEvent(parent.CONSTProgressiveDimmingDelayTimeMillis(),'','runSetProgressiveLevel',parameters, '',app.id)
}

def getDevices(){
    return settings['controlDevice']
}

def setTime(){
    if(!setStartTime()) return
    setStopTime()
    return true
}

def setStartTime(){
    if(!settings['start_timeType']) return
    if(atomicState.start && parent.checkToday(atomicState.start,app.label)) return
    setTime = setStartStopTime('start')
    if(setTime > now()) setTime -= parent.CONSTDayInMilli() // We shouldn't have to do this, it should be in setStartStopTime to get the right time to begin with
    if(!parent.checkToday(setTime)) setTime += parent.CONSTDayInMilli() // We shouldn't have to do this, it should be in setStartStopTime to get the right time to begin with
    atomicState.start  = setTime
    putLog(1190,'info','Start time set to ' + parent.getPrintDateTimeFormat(setTime))
    return true
}

def setStopTime(){
    if(!settings['stop_timeType'] || settings['stop_timeType'] == 'none') return
    if(atomicState.stop > atomicState.start) return
    setTime = setStartStopTime('stop')
    if(setTime < atomicState.start) setTime += parent.CONSTDayInMilli()
    atomicState.stop  = setTime
    putLog(1200,'info','Stop time set to ' + parent.getPrintDateTimeFormat(setTime))
    return true
}

// Sets atomicState.start and atomicState.stop variables
// Requires type value of "start" or "stop" (must be capitalized to match setting variables)
def setStartStopTime(type){
    if(settings[type + '_timeType'] == 'time') return Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSSZ", settings[type + '_time']).getTime()
    if(settings[type + '_timeType'] == 'time') return Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSSZ", settings[type + '_time']).getTime()
    if(settings[type + '_timeType'] == 'sunrise') return (settings[type + '_sunType'] == 'before' ? parent.getSunrise(settings[type + '_sunOffset'] * -1,app.label) : parent.getSunrise(settings[type + '_sunOffset'],app.label))
    if(settings[type + '_timeType'] == 'sunset') return (settings[type + '_sunType'] == 'before' ? parent.getSunset(settings[type + '_sunOffset'] * -1,app.label) : parent.getSunset(settings[type + '_sunOffset'],app.label))
}

def getDisabled(){
    // If disabled, return true
    if(state.disable) return true

    // If mode isn't correct, return false
    if(settings['ifMode'] && location.mode != settings['ifMode']) return true
    if(!parent.checkNowBetweenTimes(atomicState.start, atomicState.stop, app.label)) return true

    if(!parent.checkPeopleHome(settings['personHome'],app.label)) return true
    if(!parent.checkNoPeopleHome(settings['personNotHome'],app.label)) return true

    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,type = 'trace',message = null){
    return parent.putLog(lineNumber,type,message,app.label,,getLogLevel())
}
