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
*  Version: 0.6.2.23
*
***********************************************************************************************************************/

// To-do: Change "Push" to pushed, and "Hold" to held
// To-do: Add double-push
// To-do: Add Held + Released ?
// To-do: Add locks (and change device type selection to multi)
// TO-DO: Allow selecting rooms??
// TO-DO: Add support for Hubitat Package Manager? https://community.hubitat.com/t/beta-hubitat-package-manager/38016

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
// 2 for warnings + errors
// 3 for info + errors + warnings
// 4 for trace + info + errors + warnings
// 5 for debug + trace + info + errors + warnings
def getLogLevel(){
    return 4
}

preferences {
    if(!settings) settings = [:]
    numberOfButtons = getButtonNumbers()
    buttonMap = buildButtonMap()
    actionMap = buildActionMap()
    resetDevices()
    anyErrors = checkAnyErrors()
    install = formComplete()
    thisType = 'pico'
    thisDescription = 'Pico'        // Used with schedule, people, ifMode
    thisDescriptionPlural = 'Picos'

    page(name: 'setup', install: install, uninstall: true) {
        
        if(!app.label){
            section(){
                displayNameOption()
            }
        }
        if(app.label){
            setUILinks()
            // Sometimes UI doesn refresh with controller
            // getDeviceList and controllerOptionProcessParentDeviceList should be called after resetDevices, but
            // Hubitat is a stupid piece of shit that arbitrarily does not allow calling parent prior to page.
            allDeviceOptions = parent.getDeviceList()
            controllerDeviceOptions = controllerOptionProcessParentDeviceList('pushableButton')
            if(allDeviceOptions && !controllerDeviceOptions) {
                section(){
                    displayError('You don\'t have any Caseta or Pico devices selected in the Master app. Update the device selection in the Master app to include Caseta(s)/Pico(s).')
                    displayInfo('Caseta/Pico devices are identified by having a "pushableButton" setting, and 2, 4 or 5 buttons. If you have a Caseta/Picos installed, check the device status page.') 
                }
            }
            processDates()
            section(){
                displayNameOption()
                displayControllerOption()
                displayAdvancedOption()
                displayControlDeviceOption()
                displayCustomActionsOption()
            }
            displayAutoMappingMessage()
            displayDefineActions()
            displayCustomizeActionsAndDevices()
            displayDimmingProgressionOption()
            displayScheduleSection()
            displayPeopleOption()
            displayIfModeOption()
        }

    }
}

def formComplete(){
    if(!app.label) return false
    if(!settings['controllerDevice']) return false
    if(!numberOfButtons) return false
    if(!controlDevice) return false
    if(!checkAnyDeviceSet()) return false
    if(inputStartType == 'time' && !inputStartTime) return false
    if(inputStopType == 'time' && !inputStopTime) return false
    if((inputStartType == 'sunrise' || inputStartType == 'sunset') && !inputStartSunriseType) return false
    if((inputStopType == 'sunrise' || inputStopType == 'sunset') && !inputStopSunriseType) return false
    if((inputStartSunriseType == 'before' || inputStartSunriseType == 'after') && !inputStartBefore) return false
    if((inputStopSunriseType == 'before' || inputStopSunriseType == 'after') && !inputStopBefore) return false
    if(anyErrors) return false
    return true
}

def checkAnyErrors(){
    if(settings['customActionsSetup'] != 'actionsAndDevices') return false
    anyErrors = false
    fieldOptions = [:]
    actionMap.each{it->
        fieldOptions[it.'action'] = it.'actionText'
    }
    for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
        if(!checkIfShowButton(buttonNumber)) continue
        
        for(int actionMapItem = 0; actionMapItem < actionMap.size(); actionMapItem++){
        //actionMap.any{it->
            anyErrors = displayCustomizeActionsAndDevicesErrors(buttonNumber,fieldOptions, actionMap[actionMapItem].'action', 'push')
            if(anyErrors) break
            anyErrors = displayCustomizeActionsAndDevicesErrors(buttonNumber,fieldOptions, actionMap[actionMapItem].'action', 'hold')
            if(anyErrors) break
        }
        if(anyErrors) break
    }
    return anyErrors
}

def controllerOptionProcessParentDeviceListMatch(singleDevice){
    if(!singleDevice.currentValue('numberOfButtons')) return false
    if(singleDevice.currentValue('numberOfButtons') == 2) return true
    if(singleDevice.currentValue('numberOfButtons') == 4) return true
    if(singleDevice.currentValue('numberOfButtons') == 5) return true
    
}

def displayCustomActionsOption(){
    if(!settings['controllerDevice']) return
    if(!settings['controlDevice']) return
    if(!numberOfButtons) {
        if(!state.hubitatBug) displayWarning('Please refresh the page. It failed to auto-refresh due to a known bug in Hubitat.')
        if(state.hubitatBug) displayWarning('Turns out, something is wrong with ' + settings['controllerDevice'] + '.')
        state.hubitatBug = true
        return
    }
    state.remove('hubitatBug')
    if(anyErrors) return
    fieldName = 'customActionsSetup'
    options = ['automap':'Automap actions','actions':'Assign action and devices to each button','actionsAndDevices':'Customize actions and devices for each button']
    if(settings['controlDevice'].size() == 1 && settings[fieldName] != 'actionsAndDevices') options = ['automap':'Automap actions','actions':'Assign action and devices to each button']
    displayCustomActionsOptionComplete(fieldName,options)
    displayCustomActionsOptionIncomplete(fieldName,options)
    devicesText = 'device'
    if(settings['controlDevice'].size() > 1) devicesText = 'devices'
    if(settings['customActionsSetup'] == 'automap') displayInfo('This option will automap what each button does (click "Automapped buttons" below for details), and will be applied to the ' + devicesText + ' selected.')
    if(settings['customActionsSetup'] == 'actions' && settings['controlDevice'].size() == 1) displayInfo('This option allows setting what each button does.')
    if(settings['customActionsSetup'] == 'actions' && settings['controlDevice'].size() > 1) displayInfo('This option allows setting what each button does, then assign ' + devicesText + ' to it.')
    if(settings['customActionsSetup'] == 'actionsAndDevices') displayInfo('This option allows setting what each button does, and which devices for each button/action.')
}
def displayCustomActionsOptionComplete(fieldName,options){
    if(!settings[fieldName]) return
    fieldTitle = 'Selection type:'
    displaySelectField(fieldName,fieldTitle,options,false,true)
}
def displayCustomActionsOptionIncomplete(fieldName,options){
    if(settings[fieldName]) return
    fieldTitle = 'Select how to assign devices and buttons actions:'
    displaySelectField(fieldName,fieldTitle,options,false,true)
}

def displayAutoMappingMessage(){
    if(!settings['controllerDevice']) return
    if(!numberOfButtons) return
    if(settings['customActionsSetup'] != 'automap') return
    if(!settings['controlDevice']) return
    
    sectionTitle = '<b>Automapped buttons</b>'
    if(setHold) sectionTitle = '<b>Automapped buttons for Push and Hold</b>'
    
    section(hideable: true, hidden: true, sectionTitle + expandText) {
        pushAutoMappingText = ''
        holdAutoMappingText = ''
        displayInfo('To change these, select "Assign actions to each button" as Selection Type.')
        for(int i = 0; i < buttonMap.size(); i++) {
            showButton = checkIfShowButton(i)
            if(!showButton) continue
            actionMapItem = getActionFromButtonNumber(i)
            if(i != 0) pushAutoMappingText += '<br>'
// Need to add devices on initialize
            //app.updateSetting('button_' + (i + 1) + '_push',['type':'text','value':actionMapItem['action']])        // Set the button; uses stupid Hubitat way to access the settings dictionary
            if(!setHold) pushAutoMappingText += ' ' + buttonMap[i]['fullName'].capitalize() + ' &nbsp; :: &nbsp; ' + actionMapItem['descriptionActive']
            if(setHold) {
                pushAutoMappingText += ' Push ' + buttonMap[i]['fullName'].capitalize() + ' &nbsp; :: &nbsp; ' + actionMapItem['descriptionActive']
                if(i != 0) holdAutoMappingText += '<br>'
                holdAutoMappingText += ' Long pushing ' + buttonMap[i]['fullName'].capitalize() + ' &nbsp; :: &nbsp; ' + actionMapItem['descriptionActive']
            }
        }
        paragraph '<div style="background-color:GhostWhite">' + pushAutoMappingText + '</div><p><div style="background-color:GhostWhite">' + holdAutoMappingText + '</div>'
        displaySetHoldOption()
        if(!settings['advancedSetup']) displayInfo('Select Advanced Setup for more options.')
    }
}

def displayDefineActions(){
    if(settings['customActionsSetup'] != 'actions') return
    if(!settings['controllerDevice']) return
    if(!settings['controlDevice']) return
    if(!numberOfButtons) return
    section(hideable: true, hidden: false, getDefineActionsSectionTitle('push')) {
        if(settings['controlDevice'].size() > 1){
            warningValue = getDefineActionsWarningValue('push')
            if(warningValue) displayWarning('Select the device(s) for ' + warningValue + '.')
            if(!checkAnyDeviceSet()) {
                if(!warningValue) {
                    displayInfo('If you want assign different actions to individual buttons, select "Customize actions and devices for each button" as Selection Type.')
                    displayInfo('Select the button action, then select device(s).')
                }
            }
        }
        for(int i = 0; i < buttonMap.size(); i++){
            if(!checkIfShowButton(i)) continue
            if(!settings['button_' + (i + 1) + '_' + pushType]) continue
            if(settings['button_' + (i + 1) + '_' + pushType] != 'setColor' && settings['button_' + (i + 1) + '_' + pushType] != 'cycleColor') {
                app.removeSetting('button_' + (i + 1) + '_' + pushType + '_color')
                app.removeSetting('button_' + (i + 1) + '_' + pushType + '_colorCount')
                continue
            }
            if(settings['button_' + (i + 1) + '_' + pushType] == 'setColor' && !settings['button_' + (i + 1) + '_' + pushType + '_color']) infoText = 'Use the color picker to select what color. Note that the brightness value will not be used, only hue and saturation. (Default red.)'
            if(settings['button_' + (i + 1) + '_' + pushType] == 'cycleColor' && !settings['button_' + (i + 1) + '_' + pushType + '_colorCount']) infoText = 'Enter the number of colors to cycle through (default is 6 for primary and secondary colors). Note that saturation will be 100%, and brightness based on defaults.'
            if(infoText){
                displayInfo(infoText)
                break
            }
        }
        for(int i = 0; i < buttonMap.size(); i++){
            if(!checkIfShowButton(i)) continue
            displayDefineActionsButton(i,'push')
        }
        if(!setHold) displaySetHoldOption()
        if(!settings['advancedSetup']) displayInfo('Select Advanced Setup for more options.')
    }
    if(setHold){
        section(hideable: true, hidden: false, getDefineActionsSectionTitle('hold')) {
            for(int i = 0; i < buttonMap.size(); i++){
                if(!checkIfShowButton(i)) continue
                displayDefineActionsButton(i, 'hold')
            }
            displaySetHoldOption()
            if(!settings['advancedSetup']) displayInfo('Select Advanced Setup for more options.')
        }
    }
}
def getDefineActionsWarningValue(pushType){
    returnValue = ''
    for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
        if(returnValue) continue
        if(settings['button_' + buttonNumber + '_' + pushType] && !settings['button_' + buttonNumber + '_' + pushType + '_' + settings['button_' + buttonNumber + '_' + pushType]]){
            actionMap.find{it->
                if(it.'action' == settings['button_' + buttonNumber + '_' + pushType]){
                    returnValue = it.'description'
                }
            }
        }
    }
    return returnValue
}
def getDefineActionsSectionTitle(pushType){
    sectionTitle = ''
    for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
        if(!checkIfShowButton(buttonNumber)) continue
        if(!settings['button_' + (buttonNumber + 1) + '_' + pushType]) continue
        if(sectionTitle) sectionTitle += '\n'
        actionMap.find{it->
            if(it.'action' == settings['button_' + (buttonNumber + 1) + '_' + pushType]) buttonActionDescription = it.'descriptionActive'
        }
        sectionTitle += buttonMap[buttonNumber]['fullName'].capitalize() + ': ' + buttonActionDescription + ''
    }
    pushText = 'pushing'
    if(pushType == 'hold') pushText = 'pushing and holding'
    if(sectionTitle && setHold) sectionTitle = 'When ' + pushText + ':' + '\n\n' + sectionTitle + '\n' + expandText + ''
    if(!sectionTitle) sectionTitle = 'Select device(s) for each/any action:' + expandText
    return sectionTitle
}
def displayDefineActionsButton(buttonNumber,pushType){
    fieldName = 'button_' + (buttonNumber + 1) + '_' + pushType
    fieldTitle = buttonMap[buttonNumber]['fullName'].capitalize() + ' action:'
    fieldOptions = setActionsPerButton(buttonNumber,pushType,settings[fieldName], true)
    width = 12
    if(settings[fieldName] && settings[fieldName] == 'setColor') width = 11
    if(settings[fieldName] && settings[fieldName] == 'cycleColor') width = 11
    displaySelectField(fieldName,fieldTitle,fieldOptions,false,false,'',width)
    displaySetColorOption(buttonNumber,'setColor')
    displayCycleColorOption(buttonNumber,'cycleColor')
    displayDefineActionsDevice(buttonNumber,fieldOptions,pushType) 
}
def displayDefineActionsDevice(buttonNumber,fieldOptions,pushType){
    buttonAction = settings['button_' + (buttonNumber + 1) + '_' + pushType]
    if(!buttonAction) return
    actionMap.find{it->
        if(it.'action' == buttonAction) fieldText = it.'description'
    }
    if(settings['controlDevice'].size() == 1) {
        app.updateSetting('button_' + (buttonNumber + 1) + '_' + pushType + '_' + buttonAction,['type':'capability.switch','value':settings['controlDevice']])        // Set the button; uses stupid Hubitat way to access the settings dictionary
        return
    }
    fieldName = 'buttonId_' + (buttonNumber + 1) + '_' + pushType + '_' + buttonAction
    fieldTitle = ' '
    if(!settings[fieldName]) fieldTitle = 'Device to ' + fieldText + ':'
    deviceOptions = [:]
    settings['controlDevice'].each{it->
        deviceOptions.put([it.'id',it.'label'])
    }
    newVar = []

    displaySelectField(fieldName,fieldTitle,deviceOptions,true,true)
}

def displayCustomizeActionsAndDevices(){
    if(settings['customActionsSetup'] != 'actionsAndDevices') return
    if(!settings['controlDevice']) return
    if(!settings['controllerDevice']) return
    if(!numberOfButtons) return
    
    displayCustomizeActionsAndDevicesSections('push')
    displayCustomizeActionsAndDevicesSections('hold')

    section(){
        displaySetHoldOption()
    }
}
def displayCustomizeActionsAndDevicesSections(pushType) {
    if(pushType == 'hold' && !setHold) return
    if(setHold){
        section(){
            if(pushTye == 'push') paragraph('<b>Push:</b>')
            if(pushTye == 'hold') paragraph('<b>Push and hold:</b>')
        }
    }
    for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
        sectionLabel = ''
        if(!checkIfShowButton(buttonNumber)) continue
        fieldOptions = setActionsPerButton(buttonNumber,pushType)
        hidden = false
        setErrors = ''
        for(int actionMapItem = 0; actionMapItem < actionMap.size(); actionMapItem++){ 
            if(settings['button_' + (buttonNumber + 1) + '_' + pushType + '_' + actionMap[actionMapItem].'action']) {
                if(sectionLabel) sectionLabel += '\n'
                sectionLabel += '  ' + actionMap[actionMapItem].'descriptionActive' + ' ' + settings['button_' + (buttonNumber + 1) + '_' + pushType + '_' + actionMap[actionMapItem].'action']
                hidden = true
                
                setErrors = displayCustomizeActionsAndDevicesErrors(buttonNumber,fieldOptions, actionMap[actionMapItem].'action', pushType)
                if(setErrors) hidden = false
            }
        }
        if(anyErrors && !setErrors) hidden = true
        if(sectionLabel) sectionLabel = '\n' + sectionLabel
        sectionLabel = buttonMap[buttonNumber].'fullName'.capitalize() + expandText + sectionLabel
        section(hideable: true, hidden: hidden, sectionLabel) {
            displayCustomizeActionsAndDevicesButtons(buttonNumber,fieldOptions, pushType)
            if(!settings['advancedSetup']) displayInfo('Select Advanced Setup for more options.')
        }
    }
}
def displayCustomizeActionsAndDevicesButtons(buttonNumber, fieldOptions, pushType) {
    deviceOptions = [:]
    settings['controlDevice'].each{it->
        deviceOptions.put(it.'id',it.'label')
    }
    deviceOptions = deviceOptions.sort()        // If deviceOptions customized per field (removing contradictory ones, or moving selected to top), move to within displayDefineActionsAndDeviceField
    fieldOptions.eachWithIndex{fieldAction, fieldOptionNumber ->
        actionMap.each{it->
            if(it.'action' == fieldAction.key){
                displayDefineActionsAndDeviceField(buttonNumber, it, pushType, deviceOptions)
                if(anyErrors) displayError(displayCustomizeActionsAndDevicesErrors(buttonNumber,fieldOptions, fieldAction.key,pushType))
            }
        }
    }
}

def displayDefineActionsAndDeviceField(buttonNumber, actionLine, pushType,deviceOptions){
    fieldName = 'buttonId_' + (buttonNumber + 1) + '_' + pushType + '_' + actionLine.'action'
    
    fieldTitle = '<b>' + actionLine.'descriptionActive' + '</b>:'
    if(!settings[fieldName]) fieldTitle = actionLine.'description' + ' <font color="gray">(Select devices)</font>'

    width = 12
    if(settings[fieldName] && actionLine.'action' == 'setColor') width = 11
    if(settings[fieldName] && actionLine.'action' == 'cycleColor') width = 11
    displaySelectField(fieldName,fieldTitle,deviceOptions,true,false,'',width)
    displaySetColorOption(buttonNumber,actionLine.'action')
    displayCycleColorOption(buttonNumber,actionLine.'action')
}

def displayCustomizeActionsAndDevicesErrors(buttonNumber,fieldOptions, action, pushType){
    if(customActionsSetup == 'automap') return
    if(!fieldOptions) return
    firstActionNumber = fieldOptions.findIndexOf{it.key==action}
    if(firstActionNumber == 0) return
    errorMessage = ''
    firstDeviceName = 'button_' + (buttonNumber + 1) + '_' + pushType + '_' + action
    if(!settings[firstDeviceName]) return
    fieldOptions.find{it->
        if(it.key == action) return
        secondDeviceName = 'button_' + (buttonNumber + 1) + '_' + pushType + '_' + it.key
        if(compareDeviceLists(settings[firstDeviceName],settings[secondDeviceName],action,it.key)) {
            if(errorMessage) errorMessage += '\n'
            errorMessage += 'Can\'t set the same device to both ' + action + ' and ' + it.key + ' with the same button.'
        }
    }
    return errorMessage
}

def displaySetColorOption(buttonNumber, buttonAction){
    if(buttonAction != 'setColor') {
      //  app.removeSetting('button_' + (buttonNumber + 1) + '_' + pushType]
        return
    }
    fieldSet = false
    if(settings['button_' + (buttonNumber + 1) + '_' + pushType] == buttonAction) fieldSet = true
    if(settings['button_' + (buttonNumber + 1) + '_' + pushType + '_' + buttonAction]) fieldSet = true
    if(fieldSet) input name: 'button_' + (buttonNumber + 1) + '_' + pushType + '_color', type: 'color', title:'',width: 1,submitOnChange:true
    
    infoText = 'Use the color picker. If you want a light to be set to a specific color whenever turned on, using scheduler is preferred. Note: If device is already set to color, it will toggle back to white.'
    if(fieldSet) {
        if(!settings['button_' + (buttonNumber + 1) + '_' + pushType + '_color']) displayInfo(infoText)
        if(settings['button_' + (buttonNumber + 1) + '_' + pushType + '_color'] == '#000000') displayInfo(infoText)
    }
}
def displayCycleColorOption(buttonNumber, buttonAction){
    if(buttonAction != 'cycleColor') return
    fieldSet = false
    if(settings['button_' + (buttonNumber + 1) + '_' + pushType] == buttonAction) fieldSet = true
    if(settings['button_' + (buttonNumber + 1) + '_' + pushType + '_' + buttonAction]) fieldSet = true
    if(fieldSet) input name: 'button_' + (buttonNumber + 1) + '_' + pushType + '_colorCount', type: 'number', title:'',width: 1,submitOnChange:true
}

def displaySetHoldOption(){
    if(!settings['controllerDevice']) return
    if(!numberOfButtons) return
    if(!settings['advancedSetup'] && !settings['setHold']) return
    fieldName = 'setHold'
    holdAvailable = false
    settings['controllerDevice'].each{singleDevice->
        if(singleDevice.currentHeld) holdAvailable = true
    }
    if(!holdAvailable) {
        if(settings['controllerDevice'].size() == 1) displayInfo('The Pico ' + settings['controllerDevice'] + ' does not appear to support Long Press. To set Long Press actions, change the device type from "Lutron Fast Pico."')
        if(settings['controllerDevice'].size() > 1) displayInfo('The Picos do not appear to support Long Press. To set Long Press actions, change the device type from "Lutron Fast Pico."')
        return
    }
    displaySetHoldOptionEnabled(fieldName)
    displaySetHoldOptionDisabled(fieldName)
}
def displaySetHoldOptionEnabled(fieldName){
    if(!settings[fieldName]) return
    fieldTitleTrue = 'Automapping Long Push to the same things. Click to disable.'
    if(customActionsSetup != 'automap') fieldTitleTrue = 'Click to not set Long Push options.'
    fieldTitleFalse = ''
    displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse, false,true)
}
def displaySetHoldOptionDisabled(fieldName){
    if(settings[fieldName]) return
    fieldTitleTrue = 'Click to automap Long Push to the same things.'
    if(customActionsSetup != 'automap') fieldTitleTrue = 'Click to set Long Push options.'
    fieldTitleFalse = ''
    displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse, false,true)
}

def displayDimmingProgressionOption(){
    if(!settings['advancedSetup']) return
    if(!checkAnyDeviceSet()) return
    if(anyErrors) return
    dimmingSet = false
    if(customActionsSetup == 'automap') dimmingSet = true
    if(customActionsSetup == 'actions' || customActionsSetup == 'actionsAndDevices'){
        for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
// Replace with actionMap type = 'dim'
            if(settings['button_' + (buttonNumber + 1) + '_push'] == 'brighten' && settings['button_' + (buttonNumber + 1) + '_push_brighten']) dimmingSet = true
            if(settings['button_' + (buttonNumber + 1) + '_push'] == 'dim' && settings['button_' + (buttonNumber + 1) + '_push_dim']) dimmingSet = true
            if(settings['button_' + (buttonNumber + 1) + '_hold'] == 'brighten' && settings['button_' + (buttonNumber + 1) + '_hold_brighten']) dimmingSet = true
            if(settings['button_' + (buttonNumber + 1) + '_hold'] == 'dim' && settings['button_' + (buttonNumber + 1) + '_hold' + '_dim']) dimmingSet = true
        }
    }
    if(!dimmingSet) return
    sectionTitle = 'Click to set dimming steps (Optional)'
    if(settings['pushedDimmingProgressionSteps']) sectionTitle = 'Push dimming steps: ' + settings['pushedDimmingProgressionSteps']
    if(settings['heldDimmingProgressionSteps']) {
        if(sectionTitle) sectionTitle += '\n'
        sectionTitle = 'Push and hold dimming steps: ' + settings['heldDimmingProgressionSteps']
    }
    if(!sectionTitle) sectionTitle = '<b>Set dimming steps:</b>'
    
    section(hideable: true, hidden: true, sectionTitle + expandText) {
        infoTip = 'Number of steps it takes to brighten (or dim) from 1 to 100%.'
        if(!settings['pushedDimmingProgressionSteps'] && !settings['heldDimmingProgressionSteps']) infoTip = 'This is the number of button pushes it takes to brighten (or dim) from 1 to 100% brightness. It uses a geometric progression. For instance, with 10 steps, pressing the brighten button would go from 1% to 2%, and then to 4, 7, 11, 17, 25, 36, 52, 74, and finally 100%.'
        displayInfo(infoTip)
        fieldName = 'pushedDimmingProgressionSteps'
        displayDimmingProgressionOptionComplete(fieldName, 'push')
        displayDimmingProgressionOptionIncomplete(fieldName, 'push')
        fieldName = 'heldDimmingProgressionSteps'
        displayDimmingProgressionOptionComplete(fieldName, 'hold')
        displayDimmingProgressionOptionIncomplete(fieldName, 'hold')
    }
}
def displayDimmingProgressionOptionComplete(fieldName, pushType){
    if(!settings[fieldName]) return
    if(pushType == 'hold' && !setHold) return
    
    dimmingSet = false
    if(customActionsSetup == 'automap') dimmingSet = true
    if(customActionsSetup == 'actions' || customActionsSetup == 'actionsAndDevices'){
        for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
// Replace with actionMap type = 'dim'
            if(settings['button_' + (buttonNumber + 1) + '_' + pushType] == 'brighten' && settings['button_' + (buttonNumber + 1) + '_' + pushType + '_brighten']) dimmingSet = true
            if(settings['button_' + (buttonNumber + 1) + '_' + pushType] == 'dim' && settings['button_' + (buttonNumber + 1) + '_' + pushType + '_dim']) dimmingSet = true
        }
    }
    if(!dimmingSet) return
    fieldTitle = 'Dimming steps for Push:'
    if(pushType == 'hold') fieldTitle = 'Dimming steps for Long Push:'

    displayTextField(fieldName,fieldTitle,'number',false)
}
def displayDimmingProgressionOptionIncomplete(fieldName, pushType){
    if(settings[fieldName]) return
    if(pushType == 'hold' && !setHold) return
    dimmingSet = false
    if(customActionsSetup == 'automap') dimmingSet = true
    if(customActionsSetup == 'actions'){
        for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
// Replace with actionMap type = 'dim'
            if(settings['button_' + (buttonNumber + 1) + '_' + pushType] == 'brighten' && settings['button_' + (buttonNumber + 1) + '_' + pushType + '_brighten']) dimmingSet = true
            if(settings['button_' + (buttonNumber + 1) + '_' + pushType] == 'dim' && settings['button_' + (buttonNumber + 1) + '_' + pushType + '_dim']) dimmingSet = true
        }
    }
// This doesn't look right
    if(customActionsSetup == 'actionsAndDevices'){
        for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
            actionMap.each{it->
                if(actionMap.'type' == 'dim' && settings['button_' + (buttonNumber + 1) + '_' + pushType + '_' + it.'action']) dimmingSet = true
            }
        }
    }
    if(!dimmingSet) return
    fieldTitle = 'Enter dimming steps for Push (optional, default 8):'
    if(pushType == 'hold') fieldTitle = 'Enter dimming steps for Long Push (optional, default 20):'
    displayTextField(fieldName,fieldTitle,'number',false)
// Need to figure out how to display this for either Push or Hold, not both
}

def displayScheduleSection(){
    if(!settings['controllerDevice']) return
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup']) return
    if(!checkAnyDeviceSet()) return
    if(anyErrors) return
    
    section(){}
    
    hidden = true
    if(settings['start_timeType'] && !settings['stop_timeType']) hidden = false
    if(settings['start_time'] && settings['start_time'] == settings['stop_time']) hidden = false
    if(!validateTimes('start')) hidden = false
    if(!validateTimes('stop')) hidden = false

    section(hideable: true, hidden: hidden, getTimeSectionTitle()){
        if(!settings['start_timeType'] && validateTimes('start') && validateTimes('stop') && !settings['days']  && !settings['includeDates'] && !settings['excludeDates']) displayInfo('This will limit when this ' + appDescription + ' is active. You can create another ' + appDescription + ' "app" to do something else for opposite times/days.')
        if(settings['start_time'] && settings['start_time'] == settings['stop_time']) displayError('You can\'t have the same time to start and stop.')

        displayTimeTypeOption('start')
        displayTimeOption('start')
        displaySunriseTypeOption('start')
        displayTimeTypeOption('stop')
        displayTimeOption('stop')
        displaySunriseTypeOption('stop')
        displayDaysOption()
        displayDatesOptions()
    }
}

// Returns true if showing button (ie 2 button has no Middle button)
def checkIfShowButton(number){
    if(numberOfButtons == 2 && number == 1) return false
    if(numberOfButtons == 2 && number == 2) return false
    if(numberOfButtons == 2 && number == 3) return false
    if(numberOfButtons == 4 && number == 2) return false
    if(!parent.checkIsColorMulti(settings['controlDevice']) && buttonMap[number].'type' == 'dim') return false
    return true
}
                                     
def getButtonNumbers(){
    if(!settings['controllerDevice']) return
    settings['controllerDevice'].each{
        if(!it.currentValue('numberOfButtons')) numberOfButtons = 5
        if(it.currentValue('numberOfButtons')) {
            if(numberOfButtons){
                if(numberOfButtons < it.currentValue('numberOfButtons')) numberOfButtons = it.currentValue('numberOfButtons').toInteger()
            }
            if(!numberOfButtons) numberOfButtons = it.currentValue('numberOfButtons').toInteger()
        }
    }
    return numberOfButtons
}

def checkDefineActionButNoDevice(){
    for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
        if(settings['button_' + (buttonNumber + 1) + '_push']){
            if(!settings['button_' + (buttonNumber + 1) + '_push_' + settings['button_' + (buttonNumber + 1) + '_push']]) return settings['button_' + (buttonNumber + 1) + '_push']
        }
        if(settings['button_' + (buttonNumber + 1) + '_hold']){
            if(settings['button_' + (buttonNumber + 1) + '_hold_' + settings['button_' + (buttonNumber + 1) + '_hold']]) return settings['button_' + (buttonNumber + 1) + '_hold']
        }
    }
    return false
}
                                     
def compareDeviceLists(firstDeviceMulti,secondDeviceMulti,firstAction,secondAction){
    if(!firstDeviceMulti) return
    if(!secondDeviceMulti) return
    if(firstAction == 'on' && !['off', 'toggle'].contains(secondAction)) return
    if(firstAction == 'off' && !['on', 'resume','setColor','cycleColor'].contains(secondAction)) return
    if(firstAction == 'toggle' && !['off', 'on'].contains(secondAction)) return
    if(firstAction == 'dim' && !['brighten', 'off', 'resume'].contains(secondAction)) return
    if(firstAction == 'brighten' && !['dim', 'off', 'resume'].contains(secondAction)) return
    if(firstAction == 'resume' && !['dim', 'brighten', 'off'].contains(secondAction)) return
    if(firstAction == 'setColor' && !['off'].contains(secondAction)) return
    if(firstAction == 'cycleColor' && !['off'].contains(secondAction)) return
    returnValue = false
    firstDeviceMulti.each{firstDevice->
        secondDeviceMulti.each{secondDevice->
            if(firstDevice.id == secondDevice.id){
                returnValue = true
            }
        }
        if(returnValue) return returnValue
    }
    return returnValue
}

// Returns an ordered map of actions per button
def setActionsPerButton(buttonNumber,pushType,fieldValue = false, unique = false){
    fieldOptions = [:]
    fieldOptions = setActionsPerButtonPreset(fieldOptions,fieldValue)
    fieldOptions = setActionsPerButtonDefault(fieldOptions,fieldValue,pushType,buttonNumber)
    fieldOptions = setActionsPerButtonType(fieldOptions,fieldValue,pushType,buttonNumber)
    fieldOptions = setActionsPerButtonOther(fieldOptions,fieldValue,pushType,buttonNumber)
    
    if(!settings['advancedSetup']) {        // Remove any already picked
        for(int i = 0; i < buttonMap.size(); i++){
            if(i != buttonNumber){
                actionMap.each{it->
                    if(settings['button_' + (i + 1) + '_' + pushType] == it.'action') fieldOptions.remove(it.'action')
                }
            }
        }
    }
    return fieldOptions
}
// Return actionMap line of what it's set to
def setActionsPerButtonPreset(fieldOptions,fieldValue){
    if(!fieldValue) return
    actionMap.find{it->      // 1) What fieldValue is
        if(it.'action' == fieldValue) {
            fieldOptions[it.'action'] = it.'actionText'.capitalize()
        }
    }
    return fieldOptions
}
def setActionsPerButtonDefault(fieldOptions,fieldValue,pushType,buttonNumber){
    if(!fieldOptions) fieldOptions = [:]
    it = getActionFromButtonNumber(buttonNumber)      // 2) Default
    if(setActionsPerButtonProcess(fieldOptions,it,fieldValue,pushType,buttonNumber)) fieldOptions[it.action] = it.'actionText'.capitalize()
    return fieldOptions
}
def setActionsPerButtonType(fieldOptions,fieldValue,pushType,buttonNumber){
    if(!fieldOptions) fieldOptions = [:]
    actionMap.findAll{it->      // 3) Correct "type"
        if(it.'type' == buttonMap[buttonNumber].'type') {
            if(setActionsPerButtonProcess(fieldOptions,it,fieldValue,pushType,buttonNumber)) fieldOptions[it.action] = it.'actionText'.capitalize()
        }
    }
    return fieldOptions
}
def setActionsPerButtonOther(fieldOptions,fieldValue,pushType,buttonNumber){
    actionMap.findAll{it->      // 4) The rest
        if(it.'type' != buttonMap[buttonNumber]['type'] && it.'defaultButton' != buttonNumber + 1) {
            if(setActionsPerButtonProcess(fieldOptions,it,fieldValue,pushType,buttonNumber)) fieldOptions[it.action] = it.'actionText'.capitalize()
        }
    }
    return fieldOptions
}
// Returns false if not showing action option
def setActionsPerButtonProcess(fieldOptions,actionMapLine,fieldValue,pushType,buttonNumber){
    if(!actionMapLine) return
    if(actionMapLine?.'advanced' && !advancedSetup){
        if(!settings['buttonId_' + (buttonNumber + 1) + '_' + pushType + '_' + actionMapLine.'action'] || fieldValue) return
                     }
    if(actionMapLine.'actionText' == fieldValue) return

    return true
}

def checkAnyDeviceSet(){
    if(customActionsSetup == 'automap') return true

    for(int pushActionLoop = 0; pushActionLoop < 2; pushActionLoop++){        // Repeat for push/hold
        pushType = 'push'
        if(pushActionLoop == 1) pushType = 'hold'
        for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
            if(!checkIfShowButton(buttonNumber)) continue
            for(int actionNumber = 0; actionNumber < actionMap.size(); actionNumber++){
                if(settings['button_' + (buttonNumber + 1) + '_' + pushType] == actionMap[actionNumber].'action' && settings['controlDevice'].size() == 1) return true
                if(settings['button_' + (buttonNumber + 1) + '_' + pushType + '_' + actionMap[actionNumber].'action']) return true
                return true
            }
        }
    }
    return false
}

def resetControllerDevices(deviceName){
    if(!deviceName) return
    app.removeSetting(deviceName)
    setDeviceById(deviceName + 'Id', deviceName,'pushableButton')
}
def setDeviceById(deviceIdName, deviceName,capability){
    if(!settings[deviceIdName]) return
    if(!(parentDeviceList = parent.getDeviceList())) return
    newVar = []
    settings[deviceIdName].each{deviceId->
        parentDeviceList.find{singleDevice->
            if(singleDevice.id == deviceId){
                newVar.add(singleDevice)
            }
        }
    }
    app.updateSetting(deviceName, [type: 'capability.' + capability, value: newVar])
}

def resetDevices(){
    for(int pushActionLoop = 0; pushActionLoop < 2; pushActionLoop++){        // Repeat for push/hold
        pushType = 'push'
        if(pushActionLoop == 1) pushType = 'hold'
        for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
            for(int actionMapItem = 0; actionMapItem < actionMap.size(); actionMapItem++){
                buttonAction = actionMap[actionMapItem].'action'
            //actionMap.each{it->
                app.removeSetting('button_' + (buttonNumber + 1) + '_' + pushType + '_' + buttonAction)

                if(!settings['controlDevice']) continue
                if(!checkIfShowButton(buttonNumber)) continue
                if(customActionsSetup == 'automap' && actionMap[actionMapItem].'defaultButton' == (buttonNumber + 1) && (setHold || pushType == 'push')){
                    app.updateSetting('button_' + (buttonNumber + 1) + '_' + pushType + '_' + buttonAction, [type: "capability.switch", value: settings['controlDevice']])
                }
                if(settings['button_' + (buttonNumber + 1) + '_' + pushType] == actionMap[actionMapItem].'action' && settings['controlDevice'].size() == 1){
                    app.updateSetting('button_' + (buttonNumber + 1) + '_' + pushType + '_' + buttonAction, [type: "capability.switch", value: settings['controlDevice']])
                }
                if(settings['button_' + (buttonNumber + 1) + '_' + pushType] != actionMap[actionMapItem].'action') app.removeSetting('buttonId_' + (buttonNumber + 1) + '_' + pushType + '_' + buttonAction)
                setDeviceById('buttonId_' + (buttonNumber + 1) + '_' + pushType + '_' + buttonAction, 'button_' + (buttonNumber + 1) + '_' + pushType + '_' + buttonAction,'switch')
            }
        }
    }
}

def buildButtonMap(){
    return [['fullName':'top ("On") button', 'shortName':'top', 'type':'on'],
    ['fullName':'up button', 'shortName':'up', 'type':'dim'],
        ['fullName':'middle button', 'shortName':'middle', 'type':'on'],
        ['fullName':'down button', 'shortName':'down', 'type':'dim'],
        ['fullName':'bottom ("Off") button', 'shortName':'bottom', 'type':'on']]
}

def getActionFromButtonNumber(buttonNumber){
     actionMap.find{it->
           if(it.'defaultButton' == (buttonNumber + 1)) returnValue = it
     }
     returnValue
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
    putLog(821,'trace', 'Installed')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    putLog(827,'trace','Updated')
    unsubscribe()
    initialize()
}

def initialize() {
    putLog(833,'trace','Initializing')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))

    subscribe(settings['controllerDevice'], 'pushed', buttonPushed)
    subscribe(settings['controllerDevice'], 'held', buttonPushed)
    subscribe(settings['controllerDevice'], 'released', buttonReleased)
    
    dimValue = 8
    if(settings['pushedDimmingProgressionSteps']) dimValue = settings['pushedDimmingProgressionSteps']
    atomicState.pushedDimmingProgressionFactor = parent.computeOptiomalGeometricProgressionFactor(dimValue)
    dimValue = 20
    if(settings['heldDimmingProgressionSteps']) pushedValue = settings['heldDimmingProgressionSteps']
    atomicState.heldDimmingProgressionFactor = parent.computeOptiomalGeometricProgressionFactor(dimValue)
    putLog(846,'info','Brightening/dimming progression factor set: push ' + atomicState.pushedDimmingProgressionFactor + '; held = ' + atomicState.heldDimmingProgressionFactor + '.')

    setTime()

    putLog(850,'trace','Initialized')
}

def buttonPushed(evt){
// check mode and people
    // If not correct day, return nulls
    if(!checkIncludeDates()) return
    // if not between start and stop time, return nulls
    if(atomicState.stop && !parent.checkNowBetweenTimes(atomicState.start, atomicState.stop, app.label)) return
    if(!getActive()) return

    buttonNumber = assignButtonNumber(evt.value.toInteger())

    // Needs to be state since we're passing back and forth to parent for progressive dim and brightening
    if(evt.name == 'pushed') actionType = 'push'
    if(evt.name == 'held') actionType = 'hold'

    //action = settings['button_' + buttonNumber + '_' + actionType]
    
    putLog(869,'trace',actionType.capitalize() + ' button ' + buttonNumber + ' of ' + device)

    if(!actionMap) switchActions = buildActionMap()

    switchActions.each { switchAction ->
    
        device = settings['button_' + buttonNumber + '_' + actionType + '_' + switchAction.'action']
     //   if(!device) device = settings['controlDevice']
        device.each{singleDevice->
            if(actionType == 'push') level = parent._getNextLevelDimmable(singleDevice, switchAction.'action', atomicState.pushedDimmingProgressionFactor, app.label)

            brightnessMap = parent.getLevelMap('brightness',level,app.id,'',childLabel)         // dim, brighten
            
            setColorMap = getSetColorMap(switchAction, buttonNumber, actionType, singleDevice)
            setCycleMap = getCycleColorMap(switchAction, buttonNumber, actionType, singleDevice)
            
            if(switchAction.'action' == 'on' || switchAction.'action' == 'off' || switchAction.'action' == 'toggle') stateMap = parent.getStateMapSingle(singleDevice,switchAction.'action',app.id,app.label)       // on, off, toggle
            if(switchAction.'action' == 'level') stateMap = parent.getStateMapSingle(singleDevice,'on',app.id,app.label)
            fullMap = parent.addMaps(stateMap, brightnessMap,setColorMap,setCycleMap)
            if(fullMap) putLog(888,'trace','Updating settings for ' + singleDevice + ' to ' + fullMap)
            parent.mergeMapToTable(singleDevice.id,fullMap,app.label)
        }
        if(actionType == 'resume') parent.resumeDeviceScheduleMulti(device,app.label)       //??? this function needs to be rewritten, I think
        if(actionType == 'hold') holdNextLevelMulti(device,switchAction.'action')

        parent.setDeviceMulti(device,app.label)
    }
}

def getSetColorMap(switchAction, buttonNumber, actionType, singleDevice){
    if(switchAction.'action' != 'setColor') return
    currentHue = parent._getCurrentLevel(singleDevice.id,'hue')
    currentSat = parent._getCurrentLevel(singleDevice.id,'sat')
    hueValue = getHueFromHex(buttonNumber, actionType, singleDevice)
    satValue = getSatFromHex(buttonNumber, actionType, singleDevice)
    if(currentHue == hueValue && currentSat == satValue)  {
        parent.clearTableKey(singleDevice.id,'hue')
        parent.clearTableKey(singleDevice.id,'sat')
        return
    }
    if(hueValue) hueMap = parent.getLevelMap('hue',hueValue,app.id,'',childLabel)
    if(satValue) satMap = parent.getLevelMap('sat',satValue,app.id,'',childLabel)
    stateMap = parent.getStateMapSingle(singleDevice,'on',app.id,app.label)
    return parent.addMaps(stateMap, hueMap, satMap)
}
def getHueFromHex(buttonNumber, pushType, singleDevice){
    if(!parent.checkIsColor(singleDevice)) return
    colorHex = settings['button_' + buttonNumber + '_' + pushType + '_color']
    if(!colorHex) colorHex = '#FF0000'
    colorRgb = hubitat.helper.ColorUtils.hexToRGB(colorHex)
    colorHsv = hubitat.helper.ColorUtils.rgbToHSV(colorRgb)
    returnValue = Math.round(colorHsv[0] * 3.6)
    if(returnValue == 0) returnValue = 1
    return returnValue
}
def getSatFromHex(buttonNumber, pushType, singleDevice){
    if(!parent.checkIsColor(singleDevice)) return
    colorHex = settings['button_' + buttonNumber + '_' + pushType + '_color']
    if(!colorHex) colorHex = '#FF0000'
    colorRgb = hubitat.helper.ColorUtils.hexToRGB(colorHex)
    colorHsv = hubitat.helper.ColorUtils.rgbToHSV(colorRgb)
    return Math.round(colorHsv[1])
}

def getCycleColorMap(switchAction, buttonNumber, actionType, singleDevice){
    if(switchAction.'action' != 'cycleColor') return
    nextColor = getCycleColorValue(buttonNumber, actionType, singleDevice)
    if(!nextColor) {
        parent.clearTableKey(singleDevice.id,'hue')
        parent.clearTableKey(singleDevice.id,'sat')
        return
    }
    hueMap = parent.getLevelMap('hue',nextColor,app.id,'',childLabel)
    satMap = parent.getLevelMap('sat',100,app.id,'',childLabel)
    stateMap = parent.getStateMapSingle(singleDevice,'on',app.id,app.label)
    return parent.addMaps(stateMap, hueMap, satMap)
}
def getCycleColorValue(buttonNumber, pushType, singleDevice){
    if(!singleDevice) return
    if(!parent.checkIsColor(singleDevice)) return
    colorCount = settings['button_' + buttonNumber + '_' + pushType + '_colorCount']
    if(!colorCount) colorCount = 6
    firstColor = Math.round(360 / (colorCount * 2))
    lastColor = 360 - Math.round(360 / (colorCount * 2))
    currentColor = parent._getCurrentLevel(singleDevice.id,'hue')
    //if(!hiRezHue) currentColor = Math.round(currentColor * 3.6)
    if(!parent.checkIsOn(singleDevice)) return firstColor
    if(!currentColor) return firstColor
    if(currentColor >= lastColor){
        // set white/resume schedule
        return false
    }
    lastValue = Math.round(360 / (colorCount * 2))
    while(lastValue < 360){
        if((currentColor + 2) < lastValue) return lastValue
        lastValue += Math.round(360 / colorCount)
    }
}

// place holder until I can redo my pico setups to not throw an error
def buttonHeld(evt){
}

def buttonReleased(evt){
    buttonNumber = assignButtonNumber(evt.value.toInteger())

    putLog(975,'trace','Button ' + buttonNumber + ' of ' + device + ' released, unscheduling all')
    unschedule()
}

def assignButtonNumber(originalButton){
    numberOfButtons = getButtonNumbers()
    // Treat 2nd button of 2-button Pico as "off" (eg button 5)
    if(originalButton == 2 && numberOfButtons == 2) return 5
    if(originalButton == 4 && numberOfButtons == 4) return 5
    if(originalButton == 3 && numberOfButtons == 4) return 4
    return originalButton
}

// This is the schedule function that sets the level for progressive dimming
def runSetProgressiveLevel(data){
    if(!getSetProgressiveLevelDevice(data.device, data.action)) {
        putLog(991,'trace','Function runSetProgressiveLevel returning (no matching device)')
        return
    }
    holdNextLevelSingle(singleDevice,action)
}

def getSetProgressiveLevelDevice(deviceId, action){
    for(int i = 0; i < switchActions.size(); i++) {
        if(action == 'dim'){
            settings['button_' + (i + 1) + '_hold_dim'].each{
                if (it.id == deviceId) returnValue = it
            }
        }
        if(action == 'brighten'){
            settings['button_' + (i + 1) + '_hold_brighten'].each{
                if (it.id == deviceId) returnValue = it
            }
        }
    }
    return returnValue
}
// Has to be in child app for schedule
def holdNextLevelMulti(multiDevice,action){
    if(action != 'dim' && action != 'brighten') return

    device.each{singleDevice->
        holdNextLevelSingle(singleDevice,action)
    }
}

// Has to be in child app for schedule
def holdNextLevelSingle(singleDevice,action){
    if(!parent.checkIsDimmable(singleDevice,app.label)) return
    level = parent._getNextLevelDimmable(singleDevice, action, atomicState.heldDimmingProgressionFactor, app.label)
    if(!level) return
    levelMap = parent.getLevelMap(type,level,app.id,'',childLabel)         // dim, brighten
    parent.mergeMapToTable(singleDevice.id,levelMap,app.label)
    
    parameters = [device: singleDevice.id, action: action]
    parent.scheduleChildEvent(parent.CONSTProgressiveDimmingDelayTimeMillis(),'','runSetProgressiveLevel',parameters,app.id)
}

def getDevices(){
    return settings['controlDevice']
}

// Return true if disabled
def getActive(){
    if(settings['ifMode'] && location.mode != settings['ifMode']) return
    
    if(atomicState.scheduleStartTime && atomicState.scheduleStopTime){
        if(!parent.checkNowBetweenTimes(atomicState.scheduleStartTime, atomicState.scheduleStopTime, app.label)) return
    }

    if(settings['personHome']){
        if(!parent.checkPeopleHome(settings['personHome'],app.label)) return
    }
    if(settings['personNotHome']){
        if(!parent.checkNoPeopleHome(settings['personNotHome'],app.label)) return
    }

    return true
}

/* ************************************************************************ */
/*                                                                          */
/*                         Begin common functions.                          */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                         Begin common functions.                          */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                         Begin common functions.                          */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                         Begin common functions.                          */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                         Begin common functions.                          */
/*                                                                          */
/* ************************************************************************ */
// Copy/paste between all child apps

// Not used by schedule or sensor (yet)
// Needs to be moved back into non-shared
def buildActionMap(){
    if(thisType == 'schedule') return
    if(thisType == 'sensor') return
    return [['action':'on','actionText':'turn on','descriptionActive':'Turns on', 'description': 'Turn on','type':'on', 'defaultButton':1,'advanced':false],
        ['action':'off', 'actionText':'turn off','descriptionActive':'Turns off', 'description': 'Turn off', 'type':'on', 'defaultButton':5, 'advanced':false],
        ['action':'brighten', 'actionText':'brighten','descriptionActive':'Brightens', 'description': 'Brighten', 'type':'dim', 'defaultButton':2, 'advanced':false],
        ['action':'dim', 'actionText':'dim','descriptionActive':'Dims', 'description': 'Dim', 'type':'dim', 'defaultButton':4, 'advanced':false],
        ['action':'toggle', 'actionText':'toggle','descriptionActive':'Toggles', 'description': 'Toggle', 'type':'other', 'defaultButton':3, 'advanced':false],
        ['action':'resume', 'actionText':'resume schedule','descriptionActive':'Resumes schedule (if none, turn off)', 'description': 'Resume schedule (if none, turn off)', 'type':'other', 'advanced':true],
        ['action':'setColor', 'actionText':'set color','descriptionActive':'Sets color', 'description': 'Set color', 'type':'other', 'advanced':true],
        ['action':'cycleColor', 'actionText':'cycle through colors','descriptionActive':'Cycles through colors', 'description': 'Cycle through colors', 'type':'other', 'advanced':true]]
}
                                     
def setUILinks(){
    infoIcon = '<img src="http://emily-john.love/icons/information.png" width=20 height=20>'
    errorIcon = '<img src="http://emily-john.love/icons/error.png" width=20 height=20>'
    warningIcon = '<img src="http://emily-john.love/icons/warning.png" width=20 height=20>'
    filterYesIcon = '<img src="http://emily-john.love/icons/filterEnable.png" width=20 height=20>'
    filterNoIcon = '<img src="http://emily-john.love/icons/filterDisable.png" width=20 height=20>'
    filterLightIcon = '<img src="http://emily-john.love/icons/light.png" width=20 height=20>'
    filterColorIcon = '<img src="http://emily-john.love/icons/color.png" width=20 height=20>'
    filterSwitchIcon = '<img src="http://emily-john.love/icons/switch.png" width=20 height=20>'
    filterMergeIcon = '<img src="http://emily-john.love/icons/merge.png" width=20 height=20>'
    filterNoMergeIcon = '<img src="http://emily-john.love/icons/noMerge.png" width=20 height=20>'
    moreOptions = ' (click for more options)'
    expandText = ' (Click to expand/collapse)'
}

def displayLabel(text, width = 12){
    if(!text) return
    paragraph('<div style="background-color:#DCDCDC"><b>' + text + '</b></div>',width:width)
}


def displayInfo(text,noDisplayIcon = null, width=12){
    if(!text) return
    //tooltipName = 'tooltip' + tooltipNumber
    //tooltipComputedValueName = 'tooltipValue' + tooltipNumber
    //paragraph('<div onclick="var ' + tooltipName + ' = document.getElementById(\'' + tooltipName + '\'); var ' + tooltipComputedValueName + ' = window.getComputedStyle(' + tooltipName + '); if (' + tooltipComputedValueName + '.display === \'block\') { ' + tooltipName + '.style.display = \'none\'; } else { ' + tooltipName + '.style.display = \'block\'; ' + tooltipName + '.innerText = \'' + text + '\'; }">' + infoIcon + '</div><div id="' + tooltipName + '" style="display: none; background-color:AliceBlue"></div>', width:width)

    //tooltipNumber++
    if(noDisplayIcon) paragraph('<div style="background-color:AliceBlue">' + text + '</div>',width:width)
    if(!noDisplayIcon) paragraph('<div style="background-color:AliceBlue">' + infoIcon + ' ' + text + '</div>',width:width)
    text = ''
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
    if(!noDisplayIcon) paragraph('<div style="background-color:LemonChiffon">' + warningIcon  + ' ' + text + '</div>',width:width)
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
                                     
def displayNameOption(){
    displayNameOptionComplete()
    displayNameOptionIncomplete()
}
def displayNameOptionComplete(){
    if(!app.label) return
    displayLabel(thisDescription.capitalize() + ' name:',2)
    label title: '', required: false, width: 10,submitOnChange:true
}
def displayNameOptionIncomplete(){
    if(app.label) return
    fieldTitle = 'Set name for this ' + thisDescription + ' setup:'
    displayLabel(highlightText(fieldTitle))
    label title: '', width:12, submitOnChange:true
    displayInfo('Name this ' + thisDescription + ' setup. Each ' + thisDescription + ' setup must have a unique name.')
}

//So many custom options per app.... Either pass params, or move to non-shared
def displayControllerOption(){
    if(thisType == 'schedule') return
    if(thisType == 'sensor' && !sensorMapEntry) return
    if(!app.label) return
    fieldName = 'controllerDevice'
    resetControllerDevices(fieldName)
    fieldOptions = ''
    if(state['controllerButtonValue'] == null) state['controllerButtonValue'] = true
    if(state['controllerButtonValue']){
        fieldOptions = controllerDeviceOptions
        if(parent.getDeviceList() && !fieldOptions) return
    }
    if(fieldOptions) {
        fieldName += 'Id'
        newValue = []
        if(fieldOptions.size() == 1) {
            app.updateSetting(fieldName, [type: 'enum', value: fieldOptions.keySet()])
            return
        }
    }
    
    displayControllerOptionComplete(fieldName,fieldOptions)
    displayControllerOptionIncomplete(fieldName,fieldOptions)
}
def displayControllerOptionComplete(fieldName,fieldOptions){
    if(!settings[fieldName]) return
    if(thisType == 'cube') fieldTitle = 'MagicCube'
    if(thisType == 'pico') fieldTitle = 'Pico controller'
    if(thisType == 'sensor')fieldTitle = 'Sensor'
    if(settings[fieldName].size() > 1) fieldTitle += 's:'
    capability = 'capability.pushableButton'
    if(thisType == 'sensor') capability = 'capability.' + sensorMapEntry.'capability'
    if(fieldOptions) displaySelectField(fieldName, fieldTitle, fieldOptions, true, true, 'controllerButton')
    if(!fieldOptions) displayDeviceSelectField(fieldName, fieldTitle, capability, true, 'controllerButton')
}
def displayControllerOptionIncomplete(fieldName,fieldOptions){
    if(settings[fieldName]) return
    if(thisType == 'pico') displayInfo('Select which Lutron Caseta(s) and/or Pico(s) to control. You can select multiple devices, but all should have the same number of buttons.')
    if(fieldOptions) displayInfo('If you don\'t see the device you want, make sure you have it selected in the Master app.')
    if(thisType == 'pico') fieldTitle = 'Select button device(s) to setup:'
    if(thisType == 'cube') fieldTitle = 'Select MagicCube(s) to setup:'
    if(thisType == 'sensor') fieldTitle = 'Select sensor(s) to setup:'
    capability = 'capability.pushableButton'
    if(thisType == 'sensor') capability = 'capability.' + sensorMapEntry.'capability'
    if(fieldOptions) displaySelectField(fieldName, fieldTitle, fieldOptions, true, true, 'controllerButton')
    if(!fieldOptions) displayDeviceSelectField(fieldName, fieldTitle, capability, true, 'controllerButton')
}
def controllerOptionProcessParentDeviceList(capability){
    if(!allDeviceOptions) return
    if(!capability) return
    controllerList = [:]
    //fullList = [:]
    allDeviceOptions.each{singleDevice->
        if(singleDevice.hasCapability(capability.capitalize())){
            controllerMatch = controllerOptionProcessParentDeviceListMatch(singleDevice)
            if(controllerMatch) controllerList.put([singleDevice.'id',getDeviceName(singleDevice)])
            //if(!controllerMatch) fullList.put([singleDevice.'id',getDeviceName(singleDevice)])
        }
    }
    if(settings['controllerDeviceId']){
        settings['controllerDeviceId'].each{controllerDeviceIdValue->
            if(!controllerList.containsKey(controllerDeviceIdValue)){
                controllerList.put([controllerDeviceIdValue,'test'])
               }
               }
    }
    if(!controllerList) return
    return controllerList.sort{it.value.toLowerCase()}
}

def displayAdvancedOption(){
    if((thisType == 'pico' || thisType == 'magicCube') && !settings['controllerDevice']) return
    if(thisType != 'sensor' && !settings['controlDevice']) return
    if(anyErrors) return        // Only pico or cube
    fieldName = 'advancedSetup'
    if((thisType != 'schedule' && thisType != 'sensor') && !controllerDevice && !settings[fieldName]) return        // schedule doesn't have controllerDevice, and sensor needs advanced before controllerDevice
    if(!settings['customActionsSetup'] && !settings[fieldName] && (thisType == 'pico' || thisType == 'cube')) return
    if(thisType != 'sensor' && !settings['controlDevice']) return
    if(thisType == 'schedule'){
        if(!validateTimes('start')) return
        if(!validateTimes('stop')) return
        if(!settings['start_action']) return
        if(!settings['stop_action']) return
    }
    
    displayAdvancedOptionEnabled(fieldName)
    displayAdvancedOptionDisabled(fieldName)
}
def displayAdvancedOptionEnabled(fieldName){
    if(!settings[fieldName]) return
    fieldTitleTrue = 'Showing advanced setup.'
    displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse, false)
}
def displayAdvancedOptionDisabled(fieldName){
    if(settings[fieldName]) return
    fieldTitleTrue = 'Keeping it simple.'
    fieldTitleFalse = 'Click to show advanced setup.'
    displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse, false)
}

def displayControlDeviceOption(){
    if(thisType == 'sensor' && !settings['controllerType']) return
    if(thisType != 'schedule' && !controllerDevice) return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(anyErrors) return
    fieldName = 'controlDevice'
    displayControlDeviceOptionComplete(fieldName)
    displayControlDeviceOptionIncomplete(fieldName)
    if(thisType == 'sensor' && sensorMapEntry.'type' == 'bool') displayInfo('Note the sensor routine starts with "' + sensorMapEntry.'start' + '" and stops with "' + sensorMapEntry.'stop' + '". It will only run if the prior instance has stopped.')
}
def displayControlDeviceOptionComplete(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = 'Device to control:'
    if(settings[fieldName].size() > 1) fieldTitle = 'Devices to control:'
    capabilitiesType = 'switch'
    if(state['controlButtonValue'] == 1) capabilitiesType = 'switchLevel'
    if(state['controlButtonValue'] == 2) capabilitiesType = 'colorMode'

    displayDeviceSelectField(fieldName,fieldTitle,'capability.' + capabilitiesType,true, 'controlButton')
}
def displayControlDeviceOptionIncomplete(fieldName){
    if(settings[fieldName]) return
    fieldTitle = 'Select all device(s) to control with the ' + thisDescription + ':'
    if(thisType == 'sensor' && settings['controllerDevice'].size() == 1) fieldTitle = 'Select all device(s) to control with the sensor:'
    if(thisType == 'sensor' && settings['controllerDevice'].size() > 1) fieldTitle = 'Select all device(s) to control with the sensors:'
    capabilitiesType = 'switch'
    if(state['controlButtonValue'] == 1) capabilitiesType = 'switchLevel'
    if(state['controlButtonValue'] == 2) capabilitiesType = 'colorMode'

    displayDeviceSelectField(fieldName,fieldTitle,'capability.' + capabilitiesType,true, 'controlButton')
}

def displayIfModeOption(){
    if(!controllerDevice && thisType != 'schedule') return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if((thisType == 'pico' || thisType == 'cube') && !checkAnyDeviceSet()) return
    if(anyErrors) return
    if(thisType == 'sensor' && sensorCount == 0) return        // Need to add 'anyErrors' to sensors
    
    fieldName = 'ifMode'
    sectionTitle = 'Click to select with what Mode (Optional)'
    if(settings[fieldName]) sectionTitle = '<b>Only with Mode: ' + settings[fieldName] + '</b>'

    section(hideable: true, hidden: true, sectionTitle){
        displayIfModeFieldComplete(fieldName)
        displayIfModeFieldIncomplete(fieldName)
    }
}
def displayIfModeFieldComplete(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = 'Only with Mode:'
    displayModeSelectField(fieldName,fieldTitle,options,true,false)
}
def displayIfModeFieldIncomplete(fieldName){
    if(settings[fieldName]) return
    displayInfo('This will limit the ' + thisDescription + ' from being active to only when Hubitat\'s Mode is as selected. You can create another ' + thisDescription + ' "app" to do something else for other Modes.')
    fieldTitle = 'Only when the Mode is:'
    displayModeSelectField(fieldName,fieldTitle,options,true,false)
}

def displayPeopleOption(){
// Use devices selected in Master app
// Add check for if no presense devices
    if(!controllerDevice && thisType != 'schedule') return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if((thisType == 'pico' || thisType == 'cube') && !checkAnyDeviceSet()) return
    if(anyErrors) return
    if(thisType == 'sensor' && sensorCount == 0) return        // Need to add 'anyErrors' to sensors

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
    
    if(!settings['personHome'] && !settings['personNotHome']) sectionTitle = 'Click to select with people (Optional)'
    if(settings['personHome']) sectionTitle = '<b>Only if home: ' + withPeople + '</b>'
    if(settings['personHome'] && settings['personNotHome']) sectionTitle += '<br>'
    if(settings['personNotHome']) sectionTitle += '<b>Only if away: ' + withoutPeople + '</b>'

    section(hideable: true, hidden: hidden, sectionTitle){
        if(peopleError) displayError('You can\'t include and exclude the same person.')
        fieldName = 'personHome'
        displayPersonHomeComplete(fieldName, 'capability.presenceSensor')
        displayPersonHomeIncomplete(fieldName, 'capability.presenceSensor')
        fieldName = 'personNotHome'
        displayPersonNotHomeComplete(fieldName, 'capability.presenceSensor')
        displayPersonNotHomeIncomplete(fieldName, 'capability.presenceSensor')
        if(thisType == 'schedule') displayInfo('Schedules will resume/pause based on people present/away. If requirements are met at start time, any stop actions/levels will be applied.')
    }
}
def displayPersonHomeComplete(fieldName, fieldCapability){
    if(!settings[fieldName]) return
    fieldTitle = 'Only if home:'
    displayDeviceSelectField(fieldName,fieldTitle,fieldCapability,true)
}
def displayPersonHomeIncomplete(fieldName, fieldCapability){
    if(settings[fieldName]) return
    if(!settings['personNotHome']) displayInfo('This will limit the ' + thisDescription + ' from being active to only when those selected are home and/or away. They can be combined (as if Person A is home AND Person B is away). You can create another ' + thisDescription + ' "app" to do something else for the opposite.')
    fieldTitle = 'Only if any of these people are home (Optional)'
    displayDeviceSelectField(fieldName,fieldTitle,fieldCapability,true)
}
def displayPersonNotHomeComplete(fieldName, fieldCapability){
    if(!settings[fieldName]) return
    fieldTitle = 'Only if away:'
    displayDeviceSelectField(fieldName,fieldTitle,fieldCapability,true)
}
def displayPersonNotHomeIncomplete(fieldName, fieldCapability){
    if(settings[fieldName]) return
    fieldTitle = 'Only if any of these people are not home (Optional)'
    displayDeviceSelectField(fieldName,fieldTitle,fieldCapability,true)
}

def validateTimes(type){
    if(type == 'stop' && settings['start_timeType'] && !settings['stop_timeType']) return false
    if(thisType == 'schedule' && type == 'stop' && settings['stop_timeType'] == 'none') return true
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

def getTimeSectionTitle(){
    sectionTitle = ''
    if(!settings['start_timeType'] && !settings['stop_timeType']) return 'Click to set with schedule (Optional)'
    if(settings['start_timeType']) sectionTitle = '<b>Starting: ' + getTimeSectionStartStopTitle('start') + '</b>'
    if(settings['start_timeType'] && settings['stop_timeType']) sectionTitle += '\n'
    if(settings['stop_timeType']) sectionTitle += '<b>Stopping: ' + getTimeSectionStartStopTitle('stop') + '</b>'
    if(thisType == 'schedule' && settings['start_time'] == settings['stop_time']) sectionTitle = '<b>Always</b>'
    return sectionTitle
}
def getTimeSectionStartStopTitle(type){
    sectionTitle = ''
    if(type == 'stop' && !settings[type + '_timeType'] && !settings[type + '_timeType']) return 'No end'
    if(settings[type + '_timeType'] == 'time') sectionTitle += 'At '
    if(settings[type + '_timeType'] == 'time' && !settings[type + '_time']) sectionTitle += 'specific time'
    if(settings[type + '_timeType'] == 'time' && settings[type + '_time']) sectionTitle += Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings[type + '_time']).format('h:mm a', location.timeZone)
    if(settings[type + '_timeType'] == 'sunrise' || settings[type + '_timeType'] == 'sunset'){
        if(!settings[type + '_sunType']) sectionTitle += 'Based on ' + settings[type + '_timeType']
        if(settings[type + '_sunType'] == 'at') sectionTitle += 'At ' + settings[type + '_timeType']
        if(settings[type + '_sunType'] && settings[type + '_sunType'] != 'at' && !settings[type + '_sunOffset']) sectionTitle += settings[type + '_sunType'].capitalize() + ' ' + settings[type + '_timeType']
        if(settings[type + '_sunType'] && settings[type + '_sunType'] != 'at' && settings[type + '_sunOffset']) sectionTitle += settings[type + '_sunOffset'] + ' minutes ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType']
        if(settings[type + '_sunType'] && settings[type + '_sunType'] != 'at' && settings[type + '_sunOffset'] && validateTimes(type)) sectionTitle += ' ' + getSunriseTime(settings[type + '_timeType'],settings[type + '_sunOffset'],settings[type + '_sunType'])
    }
    return sectionTitle
}

def displayTimeTypeOption(type){
    if(type == 'stop' && (!settings['start_timeType'] || !validateTimes('start'))) return
    ingText = type
    if(type == 'stop') ingText = 'stopp'
    
    labelText = 'Schedule ' + type
    if(validateTimes('start')) labelText = ''
    if(type == 'start' && (!validateTimes('start') || !settings[type + '_timeType'])) labelText = ''
    if(!validateTimes('start') || !settings[type + '_timeType']) labelText = 'Schedule ' + ingText + 'ing time'
    
    if(labelText) displayLabel(labelText)

    if(!validateSunriseMinutes(type)) displayWarning('Time ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType'] + ' is ' + (Math.round(settings[type + '_sunOffset']) / 60) + ' hours. That\'s probably wrong.')
    
    fieldName = type + '_timeType'
    fieldTitle = type.capitalize() + ' time:'
    if(!settings[type + '_timeType']){
        fieldTitle = type.capitalize() + ' time?'
        if(type == 'stop' && thisType == 'schedule') fieldTitle += ' (Select "Don\'t stop" for none)'
        if(type == 'stop' && thisType != 'schedule') fieldTitle += ' (Required with Start time.)'
        highlightText(fieldTitle)
    }
    fieldTitle = addFieldName(fieldTitle,fieldName)
    fieldList = ['time':'Start at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
    if(type == 'stop' && thisType == 'schedule') fieldList = ['none':'Don\'t stop','time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
    if(type == 'stop' && thisType != 'schedule') fieldList = ['time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
    input fieldName, 'enum', title: fieldTitle, multiple: false, width: getTypeOptionWidth(type), options: fieldList, submitOnChange:true
    if(!settings['start_timeType']) displayInfo('Select whether to enter a specific time, or have start time based on sunrise and sunset for the Hubitat location. Required.')
}

def displayTimeOption(type){
    if(type == 'stop' && !validateTimes('start')) return
    if(type == 'stop' && settings['stop_timeType'] == 'none') return
    if(settings[type + '_timeType'] != 'time') return
    if(type == 'stop' && (!settings['start_timeType'] || !validateTimes('start'))) return

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
    
    if(!settings[fieldName]) displayInfo('Select whether scheduling starts exactly at ' + settings[type + '_timeType'] + ' (currently ' + sunTime + '). To allow entering minutes prior to or after ' + settings[type + '_timeType'] + ', select "Before ' + settings[type + '_timeType'] + '" or "After ' + settings[type + '_timeType'] + '". Required.')
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
    timeUnits = 'minutes'
    fieldTitle = timeUnits.capitalize() + ' ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType'] + ':'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'number', title: fieldTitle, width: getTypeOptionWidth(type), submitOnChange:true

    message = 'Enter the number of ' + timeUnits + ' ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType'] + ' for schedule to ' + type + '. Required.'
    if(!settings[type + '_sunOffset']) displayInfo(message)
    if(!validateSunriseMinutes(type)) displayWarning(message)
}

def displayDaysAndDatesSection(){
    if(thisType != 'schedule' && !settings['controlDevice']) return
    if(!settings['controlDevice']) return
    if(!settings['start_timeType']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(!settings['advancedSetup'] && !settings['days'] && !settings['includeDates'] && !settings['excludeDates']) return
    hidden = true

    defaultSectionTitle = '<b>Days/Dates:'
    if(!settings['days'] && !settings['includeDates'] && !settings['excludeDates']) sectionTitle = 'Click to set days/dates (optional)'
    List dayList=[]
    settings['days'].each{
        dayList.add(it)
    }
    dayText = dayList.join(', ')
    sectionTitle = ''
    if(settings['days']) sectionTitle += ' ' + dayText
    if(settings['includeDates']) sectionTitle += ' +[included dates]'
    if(settings['excludeDates']) sectionTitle += ' +[excluded dates]'
    if(settings['days'] || settings['includeDates'] || settings['excludeDates']) sectionTitle += '</b>'
    if((!settings['days'] || !settings['includeDates'] || !settings['excludeDates']) && (settings['days'] || settings['includeDates'] || settings['excludeDates'])) sectionTitle += moreOptions
    section(hideable: true, hidden: hidden, sectionTitle){

    displayDaysOption()
    displayIncludeDates()
    displayExcludeDates()
    }
}

def displayDaysOption(){
    if(thisType == 'schedule' && !settings['start_timeType']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return

    fieldName = 'days'
    if(!settings['advancedSetup'] && !settings[fieldName]) return
    fieldTitle = 'On these days (Optional; defaults to all days):'
    if(!settings[fieldName]) fieldTitle = 'On which days (Optional; defaults to all days)?'
    options = ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday']
    displaySelectField(fieldName,fieldTitle,options,true,false)
    //input fieldName, 'enum', title: fieldTitle, multiple: true, width: 12, options: ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday'], submitOnChange:true
}

def displayDatesOptions(){
    displayIncludeDates()
    displayExcludeDates()
}

def displayIncludeDates(){
    if(!settings['advancedSetup'] && !settings['includeDates']) return
    displayWarning(parent.getDateProcessingErrors(app.id))
    displayInfo(dateList)
    fieldName = 'includeDates'
    fieldTitle = 'Only on dates:'
    if(thisType == 'schedule') fieldTitle = 'Dates on which to run ("include"):'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, "textarea", title: fieldTitle, submitOnChange:true
}
 
// Returns true if level value is either valid or null
def validateLevel(value){
    if(!value) return true
    value = value as int
        if(value < 1) return false
        if(value > 100) return false
    return true
}

// Returns true if temp value is either valid or null
def validateTemp(value){
    if(!value) return true
        value = value as int
            if(value < 1800) return false
            if(value > 6500) return false
            return true
}


// Returns true if temp value is either valid or null
def validateHue(value){
    if(!value) return true
    value = value as int
    if(value < 1) return false

    if(value > 360) return false
    return true
}

def getMinutesError(value){
    if(!value) return
    if(value == 0) return
    if(value > 1440) return 'Maximum minutes is 1,440 (24 hours).'
    if(value < 1) return 'Minimum minutes is 1.'
}

def displayTextField(fieldName,fieldTitle,fieldType,required = true){
    width = 10
    if(!settings[fieldName]) width = 12
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(required && !settings[fieldName]) displayLabel(highlightText(fieldTitle))
    if(!required && !settings[fieldName]) displayLabel(fieldTitle)
    input name: fieldName, type: fieldType, title: '', width:width, submitOnChange:true
}
def displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse,required = true,defaultValue = false){
    if(!fieldTitleTrue) fieldTitleTrue = ''
    if(!fieldTitleFalse) fieldTitleFalse = ''
    if(required && fieldTitleTrue) fieldTitleTrue = highlightText(fieldTitleTrue)
    if(fieldTitleTrue) fieldTitle = fieldTitleTrue
    if(required) fieldTitle += ' ' + fieldTitleFalse
    if(!required) fieldTitle += '<br> ' + fieldTitleFalse
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input name: fieldName, type: 'bool', title:fieldTitle, default:defaultValue,submitOnChange:true
}
def displaySelectField(fieldName,fieldTitle,options,multiple = false,required = true, button = '', width = 12){
    if(button) width--
    if(settings[fieldName]) width = width - 2
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(required && !settings[fieldName]) displayLabel(highlightText(fieldTitle))
    if(!required && !settings[fieldName]) displayLabel(fieldTitle)
    input name: fieldName, type: 'enum', title: '', options: options, width:width, multiple: multiple, submitOnChange:true
    if(button) displayFilterButton(button)
}
def displayDeviceSelectField(fieldName,fieldTitle,capability,multiple, button = ''){
    width = 12
    if(button) width = 11
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input name: fieldName, type: capability, title:fieldTitle, multiple: multiple, submitOnChange:true, width: width
    if(button) displayFilterButton(button)
}
def displayModeSelectField(fieldName,fieldTitle,options,multiple = false,required = true){
    width = 10
    if(!settings[fieldName]) width = 12
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(required && !settings[fieldName]) displayLabel(highlightText(fieldTitle))
    if(!required && !settings[fieldName]) displayLabel(fieldTitle)
    input name: fieldName, type: 'mode', title: '', options: options, width:width, multiple: multiple, submitOnChange:true
}

def appButtonHandler(buttonValue){
      switch(buttonValue) {
          case 'controllerButton':
          if(!state[buttonValue + 'Value']){
              state[buttonValue + 'Value'] = true
              return
          }
              state[buttonValue + 'Value'] = false
          return
          case 'controlButton':
          if(!state[buttonValue + 'Value']){
              state[buttonValue + 'Value'] = 1
              return
          }
          if(state[buttonValue + 'Value'] == 1){
              state[buttonValue + 'Value'] = 2
              return
          }
              state.remove(buttonValue + 'Value')
          return
          case 'controllerTypeButton':        //used only by sensor app
          if(!state[buttonValue + 'Value']){
              state[buttonValue + 'Value'] = true
              return
          }
              state[buttonValue + 'Value'] = false
          return
          case 'averageButton':        //used only by sensor app
          if(!state[buttonValue + 'Value']){
              state[buttonValue + 'Value'] = true
              return
          }
              state[buttonValue + 'Value'] = false
          return
          case 'multipleOptionsButton':        //used only by sensor app
          if(!state[buttonValue + 'Value']){
              state[buttonValue + 'Value'] = true
              return
          }
              state[buttonValue + 'Value'] = false
          return
      }
}

def displayFilterButton(buttonName){
    if(buttonName == 'controllerButton'){
        if(state[buttonName + 'Value']) {
            input buttonName, 'button', title: filterYesIcon + ' Filter', width:1
        }
        if(!state[buttonName + 'Value']){
            input buttonName, 'button', title: filterNoIcon + ' Filter', width:1
        }
        return
    }
    if(buttonName == 'controlButton'){
        if(!state[buttonName + 'Value']) {
            input buttonName, 'button', title: filterSwitchIcon + ' Filter', width:1
        }
        if(state[buttonName + 'Value'] == 1){
            input buttonName, 'button', title: filterLightIcon + ' Filter', width:1
        }
        if(state[buttonName + 'Value'] == 2){
            input buttonName, 'button', title: filterColorIcon + ' Filter', width:1
        }
        return
    }
    if(buttonName == 'controllerTypeButton'){
        if(state[buttonName + 'Value']) {
            input buttonName, 'button', title: filterYesIcon + ' Filter', width:1
        }
        if(!state[buttonName + 'Value']){
            input buttonName, 'button', title: filterNoIcon + ' Filter', width:1
        }
        return
    }
    if(buttonName == 'averageButton'){
        if(state[buttonName + 'Value']) {
            input buttonName, 'button', title: filterNoMergeIcon, width:1
        }
        if(!state[buttonName + 'Value']){
            input buttonName, 'button', title: filterMergeIcon, width:1
        }
        return
    }
    if(buttonName == 'multipleOptionsButton'){
        if(state[buttonName + 'Value']) {
            input buttonName, 'button', title: filterMergeIcon, width:1
        }
        if(!state[buttonName + 'Value']){
            input buttonName, 'button', title: filterNoMergeIcon, width:1
        }
        return
    }
}
def getNextYearWithMondayChristmas(currentYear = null) {
    if(!currentYear) currentYear = new Date().format('yyyy').toInteger() - 1
    mondayChristmas = false
    while (!mondayChristmas) {
        currentYear++
        christmas = Date.parse('yyyyMMdd',currentYear + '1225')
        if (christmas.format('EEEE') == 'Monday') mondayChristmas = true
        
    }
    return currentYear
}

def setTime(){
    atomicState.remove('scheduleStartTime')
    atomicState.remove('scheduleStopTime')
    if(!settings['start_timeType']) return
    atomicState.scheduleStartTime = getBaseStartStopTimes('start')
    atomicState.scheduleStopTime = getBaseStartStopTimes('stop')
    
    if(atomicState.scheduleStartTime == atomicState.scheduleStopTime) atomicState.scheduleStopTime -= 1 // If start and stop are the same, stop needs to be smaller, to be seen as next day

}
// Returns 'start' or 'stop' time (of day) in millis
// Must be converted with getDatetimeFromTimeInMillis if compared to now()
def getBaseStartStopTimes(type){
    if(type == 'stop' && settings['stop_timeType'] == 'none') return
    if(type == 'stop' && !settings['stop_timeType']) return
    if(type == 'start' && !settings['start_timeType']) return
    if(settings[type + '_timeType'] == 'time') {
        if(!settings[type + '_time']) return
        return parent.getTimeOfDayInMillis(timeToday(settings[type + '_time']).getTime()) + 1   // Add 1 so midnight isn't "empty" as zero
    }
    if(!settings[type + '_sunType']) return
    if(settings[type + '_timeType'] == 'sunrise') return parent.getTimeOfDayInMillis((settings[type + '_sunType'] == 'before' ? parent.getSunrise(settings[type + '_sunOffset'] * -1,app.label) : parent.getSunrise(settings[type + '_sunOffset'],app.label)))
    if(settings[type + '_timeType'] == 'sunset') return parent.getTimeOfDayInMillis((settings[type + '_sunType'] == 'before' ? parent.getSunset(settings[type + '_sunOffset'] * -1,app.label) : parent.getSunset(settings[type + '_sunOffset'],app.label)))
}
def checkIncludeDates(){
    if(!atomicState?.includeDates) return true
    currentYear = new Date().format('yyyy').toInteger()
    if(!atomicState.'includeDates'?.currentYear) processDates()
    if(atomicState.includeDates.(currentYear.toInteger()).contains(new Date(now()).format('D'))) return true
}
def processDates(){
    atomicState.remove('includeDates')
    if(!settings['days'] && !settings['includeDates'] && !settings['excludeDates']) return
    currentYear = new Date().format('yyyy').toInteger()
    includeDatesValue = settings['includeDates']
    if(!settings['includeDates'] && (settings['days'] || settings['excludeDates'])) includeDatesValue = '1/1-12/31'
    atomicState.'includeDates' = [(currentYear):parent.processDates(settings['includeDates'], settings['excludeDates'], settings['days'], app.id, true)]
}

def getDeviceName(singleDevice){
    if(!singleDevice) return
    if(singleDevice.label) return singleDevice.label
    return singleDevice.name
}

def displayExcludeDates(){
    fieldName = 'excludeDates'
    if(!settings['advancedSetup'] && !settings[fieldName]) return
    fieldTitle = 'Not on dates:'
    if(appType == 'schedule') fieldTitle = 'Dates on which to <u>not</u> run ("exclude"):'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, "textarea", title: fieldTitle, submitOnChange:true
    deviceText = 'the device'
    if(thisType == 'schedule' && settings['controlDevice'].size() > 1) deviceText = 'the devices'
    if(thisType != 'schedule' && settings['controlDevice'].size() > 1) deviceText = 'the devices'
    currentYear = new Date(now()).format('yyyy').toInteger()
    christmasMondayYear = getNextYearWithMondayChristmas()
    infoTip = 'Enter which date(s) to restrict or exclude this ' + thisDescription + ' routine. "Only on dates" are when this ' + thisDescription + ' will work, for instance if you want ' + deviceText + ' to do a specific thing on Christmas. \
"Not on" dates are when this ' + thisDescription + ' will not apply, for instance to set ' + deviceText + ' to do something any other day. Rules:\n\
	• Year is optional, but would only apply to that <i>one day</i>. If no year is entered, it will repeat annually. \
<i>Example: "12/25/' + (new Date(now()).format('yyyy').toInteger() - 1) + '" will never occur in the future, because that\'s how time works.</i>\n\
	• Use month/day ("mm/dd") format, or day.month ("dd.mm"). You can also use Julian days of the year as a 3-digit number ("ddd"). \
<i>Example: Christmas could be entered as "12/25", "25.12" or "359" (the latter only true for non-leap years, otherwise "360").</i>\n\
	• Separate multiple dates with a comma (or semicolon). \
<i>Example: "12/25, 1/1" is Christmas and New Year\'s Day.</i>\n\
	• Use a hyphen to indicate a range of dates. \
<i>Example: "12/25-1/6" are the 12 days of Christmas.</i>\n\
    	• The "days" options above will combine with the dates. \
<i>Example: Selecting Monday and entering "12/25" as an "only on" date would only allow the ' + thisDescription + ' to activate on 12/25/' + christmasMondayYear + ', 12/25/' + getNextYearWithMondayChristmas((christmasMondayYear + 1)) + ', etc. when Christmas is on a Monday.</i>\n\
	• You can mix and match formats (even tho you probably shouldn\'t), and individual dates with ranges. And the order doesn\'t matter. \
<i>Example: "001, 31.10, 12/25/' + (new Date(now()).format('yy').toInteger()) + '-12/31/' + (new Date(now()).format('yyyy').toInteger()) + '" is every Halloween, Christmas to New Years\' Eve of ' + (new Date(now()).format('yyyy').toInteger()) + ', and every New Years\' Day.</i>\n\
	• If a date falls within both "only on" and "not on", it will be treated as "not on".\n\
	• If any date within a date range is invalid, the entire date range will be ignored. <i>Example: 02/01-02/29 would only be used on a Leap Year (to do all of February including 2/29, enter "2/1-2/28, 2/29").</i>'

    displayInfo(infoTip)
    
}
//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,type = 'trace',message = null){
    return parent.putLog(lineNumber,type,message,app.label,,getLogLevel())
}
