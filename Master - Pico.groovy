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
*  Version: 0.6.2.15
*
***********************************************************************************************************************/

// To-do: Change "Push" to pushed, and "Hold" to held
// To-do: Add double-push
// To-do: Add Held + Released ?
// To-do: Add locks (and change device type selection to multi)

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
    return 4
}

def displayLabel(text, width = 12){
    if(!text) return
    paragraph('<div style="background-color:#DCDCDC"><b>' + text + '</b></div>',width:width)
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

preferences {
    if(!settings) settings = [:]
    if(device) numberOfButtons = getButtonNumbers()
    buttonMap = buildButtonMap()
    actionMap = buildActionMap()
    resetDevices()
    anyErrors = checkAnyErrors()
    install = formComplete()
    appDescription = 'Pico'        // Used with schedule, people, ifMode

    page(name: 'setup', install: install, uninstall: true) {
        if(!app.label){
            section(){
                displayNameOption()
            }
        }
        if(app.label){
            processDates()
            section(){
                displayNameOption()
                displayPicoOption()
                displayAdvancedOption()
                displayControlDeviceTypeOption()
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
    if(!settings['device']) return false
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
    anyErrors = false
        fieldOptions = [:]
        actionMap.each{it->
            fieldOptions[it.'action'] = it.'actionText'.capitalize()
    }
    for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
        if(anyErrors) continue
        if(!checkIfShowButton(buttonNumber)) continue
        
        actionMap.any{it->
            setErrors = displayCustomizeActionsAndDevicesErrors(buttonNumber,fieldOptions, it.'action', 'push')
            if(!anyErrors && setErrors) {
                anyErrors = true
                return true
            }
        }
        fieldOptions = setActionsPerButton(buttonNumber,'hold')
        actionMap.any{it->
            setErrors = displayCustomizeActionsAndDevicesErrors(buttonNumber,fieldOptions, it.'action', 'hold')
            if(!anyErrors && setErrors) {
                anyErrors = true
                return true
            }
        }
    }
    return anyErrors
}

def displayNameOption(){
    displayNameOptionComplete()
    displayNameOptionIncomplete()
}
def displayNameOptionComplete(){
    if(!app.label) return
    displayLabel('Button controller name:',2)
    label title: '', required: false, width: 10,submitOnChange:true
}
def displayNameOptionIncomplete(){
    if(app.label) return
    fieldTitle = 'Set name for this button setup:'
    displayLabel(highlightText(fieldTitle))
    label title: '', width:12, submitOnChange:true
    displayInfo('Name this button setup. Each button setup must have a unique name.')
}

def displayPicoOption(){
    if(!app.label) return
    fieldName = 'device'
    resetPicoDevices(fieldName)
    fieldOptions = picoOptionProcessParentDeviceList()
    if(fieldOptions) fieldName += 'Id'
    displayPicoOptionComplete(fieldName,fieldOptions)
    displayPicoOptionIncomplete(fieldName,fieldOptions)
    if(settings['device'] && !numberOfButtons) {
        if(settings['device'].size() == 1) displayWarning('The ' + settings['device'] + ' does not have the attribute numberOfButtons. Check to make sure it is a Caseta Lutron switch or Caseta Pico. This app will assume it has 5-buttons, and <i>should</i> otherwise work.')
        if(settings['device'].size() > 1) displayWarning('At least one of the selected controllers does not have the attribute numberOfButtons. Check to make sure they are Lutron Caseta switches or Picos. This app will assume it has 5-buttons, and <i>should</i> otherwise work.')
    }

// Need warning for if Picos with different # buttons, but maybe not flag thosee without numberOfButtons (ie straight average won't work)
}
def displayPicoOptionComplete(fieldName,fieldOptions){
// If just one Pico, add it
    if(!settings[fieldName]) return
    fieldTitle = 'Pico controller:'
    if(settings[fieldName].size() > 1) fieldTitle = 'Pico controllers:'
    if(fieldOptions) displaySelectField(fieldName, fieldTitle, fieldOptions, true, true)
    if(!fieldOptions) displayDeviceSelectField(fieldName, fieldTitle, 'capability.pushableButton', true)
}
def displayPicoOptionIncomplete(fieldName,fieldOptions){
    if(settings[fieldName]) return
    displayInfo('Select which Lutron Caseta(s) and/or Pico(s) to control. You can select multiple devices, but all should have the same number of buttons.')
    if(fieldOptions) displayInfo('If you don\'t see the device you want, make sure you have it selected in the Master app.')
    fieldTitle = 'Select button device(s) to setup:'
    if(fieldOptions) displaySelectField(fieldName, fieldTitle, fieldOptions, true, true)
    if(!fieldOptions) displayDeviceSelectField(fieldName, fieldTitle, 'capability.pushableButton', true)
}
def picoOptionProcessParentDeviceList(){
    parentDeviceList = parent.getDeviceList()
    if(!parentDeviceList) return
    picoList = [:]
    fullList = [:]
    parentDeviceList.each{singleDevice->
        if(singleDevice.hasCapability('PushableButton')){
            if(singleDevice.currentValue('numberOfButtons') != 2 && singleDevice.currentValue('numberOfButtons') != 4 && singleDevice.currentValue('numberOfButtons') != 5) return
            //picoMatch = checkDeviceMatchesPico(singleDevice)        // This would do keyword match, and sort them to the top
            if(picoMatch) picoList.put([singleDevice.'id',singleDevice.'label'])
            if(!picoMatch) fullList.put([singleDevice.'id',singleDevice.'label'])
        }
    }
    return fullList.sort{it.value.toLowerCase()}
}
// DISABLED
def checkDeviceMatchesPico(singleDevice){
    if(!singleDevice) return
    if(singleDevice.label.toUpperCase().contains('PICO')) return true
    if(singleDevice.name.toUpperCase().contains('PICO')) return true
    if(singleDevice.label.toUpperCase().contains('CASETA')) return true
    if(singleDevice.name.toUpperCase().contains('CASETA')) return true
    if(singleDevice.label.toUpperCase().contains('LUTRON')) return true
    if(singleDevice.name.toUpperCase().contains('LUTRON')) return true
}

def displayAdvancedOption(){
    if(settings['disable']) return
    if(anyErrors) return
    fieldName = 'advancedSetup'
    if(!settings['device'] && !settings[fieldName]) return
    if(!settings['customActionsSetup'] && !settings[fieldName]) return
    if(settings['customActionsSetup'] == 'automap' && !settings['controlDevice']) return
    if(settings['customActionsSetup'] == 'actions' && !settings['controlDevice']) return
    
    displayAdvancedOptionEnabled(fieldName)
    displayAdvancedOptionDisabled(fieldName)
}
def displayAdvancedOptionEnabled(fieldName){
    if(!settings[fieldName]) return
    fieldTitleTrue = 'Showing advanced options.'
    displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse, false)
}
def displayAdvancedOptionDisabled(fieldName){
    if(settings[fieldName]) return
    fieldTitleTrue = 'Keeping it simple.'
    fieldTitleFalse = 'Click to show advanced options.'
    displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse, false)
}

def displayControlDeviceTypeOption(){
    if(!settings['device']) return
    if(settings['controlDevice']) return
    fieldName = 'controlDeviceType'
    fieldOptions = ['switch':'All switches','switchLevel':'Only lights','colorMode':'Only color lights']
    displayControlDeviceTypeOptionComplete(fieldName, fieldOptions)
    displayControlDeviceTypeOptionIncomplete(fieldName, fieldOptions)
}
def displayControlDeviceTypeOptionComplete(fieldName, fieldOptions){
    if(!settings[fieldName]) return
    fieldTitle = 'Type of devices to control:'
    displaySelectField(fieldName,fieldTitle,fieldOptions,false,true)
}
def displayControlDeviceTypeOptionIncomplete(fieldName, fieldOptions){
    if(settings[fieldName]) return
    fieldTitle = 'Select the type of device(s) to control with the Pico:'
    if(settings['device'].size() > 1) fieldTitle = 'Select the type of device(s) to control with the Picos:'
    displaySelectField(fieldName,fieldTitle,fieldOptions,false,true)
    displayInfo('This just filters the device selection option shown in the next step. If in doubt, select "All switches."')
}

def displayControlDeviceOption(){
    if(!settings['device']) return
    if(anyErrors) return
    fieldName = 'controlDevice'
    if(!settings[fieldName] && !settings['controlDeviceType']) return
    displayControlDeviceOptionComplete(fieldName)
    displayControlDeviceOptionIncomplete(fieldName)
}
def displayControlDeviceOptionComplete(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = 'Device to control:'
    if(settings['controlDevice'].size() > 1) fieldTitle = 'Devices to control:'
    capabilitiesType = 'capability.' + settings['controlDeviceType']
    if(!settings['controlDeviceType']) capabilitiesType = 'capability.switch'

    displayDeviceSelectField(fieldName,fieldTitle,capabilitiesType,true)
}
def displayControlDeviceOptionIncomplete(fieldName){
    if(settings[fieldName]) return
    fieldTitle = 'Select all device(s) to control with the Pico:'
    if(settings['device'].size() > 1) fieldTitle = 'Select all device(s) to control with the Picos:'
    displayDeviceSelectField(fieldName,fieldTitle,'capability.' + settings['controlDeviceType'],true)
}

def displayCustomActionsOption(){
    if(!settings['device']) return
    if(!settings['controlDevice']) return
    if(!numberOfButtons) return
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
    if(!settings['device']) return
    if(!numberOfButtons) return
    if(settings['customActionsSetup'] != 'automap') return
    if(!settings['controlDevice']) return
    if(settings['disable']) return
    
    sectionTitle = '<b>Automapped buttons</b>'
    if(setFset) sectionTitle = '<b>Automapped buttons for Push and Hold</b>'
    
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
            if(!setHold) pushAutoMappingText += ' ' + buttonMap[i]['fullName'] + ' &nbsp; :: &nbsp; ' + actionMapItem['descriptionActive']
            if(setHold) {
                pushAutoMappingText += ' Push ' + buttonMap[i]['fullName'] + ' &nbsp; :: &nbsp; ' + actionMapItem['descriptionActive']
                if(i != 0) holdAutoMappingText += '<br>'
                holdAutoMappingText += ' Long pushing ' + buttonMap[i]['fullName'] + ' &nbsp; :: &nbsp; ' + actionMapItem['descriptionActive']
            }
        }
        paragraph '<div style="background-color:GhostWhite">' + pushAutoMappingText + '</div><p><div style="background-color:GhostWhite">' + holdAutoMappingText + '</div>'
        displaySetHoldOption()
        if(!settings['advancedSetup']) displayInfo('Select Advanced Setup for more options.')
    }
}

def displayDefineActions(){
    if(settings['customActionsSetup'] != 'actions') return
    if(!settings['controlDevice']) return
    if(settings['disable']) return
    section(hideable: true, hidden: false, getDefineActionsSectionTitle('push')) {
        if(settings['controlDevice'].size() > 1){
            warningValue = getDefineActionsWarningValue()
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
def getDefineActionsWarningValue(){
    returnValue = ''
    for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
        if(returnValue) continue
        if(settings['button_' + buttonNumber + '_push'] && !settings['button_' + buttonNumber + '_push_' + settings['button_' + buttonNumber + '_push']]){
            actionMap.find{it->
                if(it.'action' == settings['button_' + buttonNumber + '_push']){
                    returnValue = it.'description'
                }
            }
        }
        if(settings['button_' + buttonNumber + '_hold'] && !settings['button_' + buttonNumber + '_hold_' + settings['button_' + buttonNumber + '_hold']]){
            actionMap.find{it->
                if(it.'action' == settings['button_' + buttonNumber + '_hold']){
                    returnValue = it.'description'
                }
            }
        }
    }
    return returnValue
}
def getDefineActionsSectionTitle(pushType){
    sectionTitle = ''
    for(int i = 0; i < buttonMap.size(); i++){
        if(!checkIfShowButton(i)) continue
        if(!settings['button_' + (i + 1) + '_' + pushType]) continue
        if(sectionTitle) sectionTitle += '\n'
        sectionTitle += buttonMap[i]['fullName'] + ': ' + getActionFromButtonNumber(i)['descriptionActive'] + ''
    }
    pushText = 'pushing'
    if(pushType == 'hold') pushText = 'pushing and holding'
    if(sectionTitle && setHold) sectionTitle = 'When ' + pushText + ':' + '\n\n' + sectionTitle + '\n' + expandText + ''
    if(!sectionTitle) sectionTitle = 'Select device(s) for each/any action:' + expandText
    return sectionTitle
}
def displayDefineActionsButton(buttonNumber,pushType){
    fieldName = 'button_' + (buttonNumber + 1) + '_' + pushType
    fieldTitle = buttonMap[buttonNumber]['fullName'] + ' action:'
    fieldOptions = setActionsPerButton(buttonNumber,pushType,settings[fieldName], true)
    displaySelectField(fieldName,fieldTitle,fieldOptions,false,false)
    displayDefineActionsDevice(buttonNumber,fieldOptions,pushType) 
}
def displayDefineActionsDevice(buttonNumber,fieldOptions,pushType){
    buttonAction = settings['button_' + (buttonNumber + 1) + '_' + pushType]
    if(!buttonAction) return
    if(!settings['controlDevice']) return
    actionMap.find{it->
        if(it.'action' == buttonAction) fieldText = it.'description'
    }
    if(settings['controlDevice'].size() == 1) return
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
    if(!settings['device']) return
    if(settings['disable']) return
    
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
        actionMap.any{it->
            setErrors = displayCustomizeActionsAndDevicesErrors(buttonNumber,fieldOptions, it.'action', pushType)
            if(setErrors) {
                hidden = false
                return true            // break
            }
            if(anyErrors) {
                hidden = true
                return
            }
            if(settings['button_' + (buttonNumber + 1) + '_' + pushType + '_' + it.'action']) {
                if(sectionLabel) sectionLabel += '\n'
                sectionLabel += it.'descriptionActive' + ' ' + settings['button_' + (buttonNumber + 1) + '_' + pushType + '_' + it.'action']
                hidden = true
                return true            // break
            }
        }
        if(sectionLabel) sectionLabel = '\n' + sectionLabel
        sectionLabel = buttonMap[buttonNumber].'fullName' + expandText + sectionLabel
        section(hideable: true, hidden: hidden, sectionLabel) {
            displayCustomizeActionsAndDevicesButtons(buttonNumber,fieldOptions, pushType)
        }
    }
}
def displayCustomizeActionsAndDevicesButtons(buttonNumber, fieldOptions, pushType) {
    fieldOptions.eachWithIndex{fieldAction, fieldOptionNumber ->
        actionMap.each{it->
            if(it.'action' == fieldAction.key){
                displayDefineActionsAndDeviceField(buttonNumber, it, pushType, true)
                if(anyErrors) displayError(displayCustomizeActionsAndDevicesErrors(buttonNumber,fieldOptions, fieldAction.key,pushType))
            }
        }
    }
}

def displayDefineActionsAndDeviceField(buttonNumber, actionLine, pushType,populated = null){
    fieldName = 'buttonId_' + (buttonNumber + 1) + '_' + pushType + '_' + actionLine.'action'
    fieldTitle = '<b>' + actionLine.'descriptionActive' + '</b>:'
    if(!settings[fieldName]) fieldTitle = actionLine.'description' + ' <font color="gray">(Select devices)</font>'

    deviceOptions = [:]
    settings['controlDevice'].each{it->
        deviceOptions.put([it.'id',it.'label'])
    }
    displaySelectField(fieldName,fieldTitle,deviceOptions,true,false)
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

def displaySetHoldOption(){
    if(!settings['device']) return
    if(!numberOfButtons) return
    if(!settings['advancedSetup'] && !settings['setHold']) return
    if(settings['disable']) return
    fieldName = 'setHold'
    holdAvailable = false
    settings['device'].each{singleDevice->
        if(singleDevice.currentHeld) holdAvailable = true
    }
    if(!holdAvailable) {
        if(settings['device'].size() == 1) displayInfo('The Pico ' + settings['device'] + ' does not appear to support Long Press. To set Long Press actions, change the device type from "Lutron Fast Pico."')
        if(settings['device'].size() > 1) displayInfo('The Picos do not appear to support Long Press. To set Long Press actions, change the device type from "Lutron Fast Pico."')
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
        if(!settings['pushedDimmingProgressionSteps'] && !settings['pushedDimmingProgressionSteps']) infoTip = 'This is the number of button pushes it takes to brighten (or dim) from 1 to 100% brightness. It uses a geometric progression. For instance, with 10 steps, pressing the brighten button would go from 1% to 2%, and then to 4, 7, 11, 17, 25, 36, 52, 74, and finally 100%.'
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
        for(int i = 0; i < buttonMap.size(); i++){
// Replace with actionMap type = 'dim'
            if(settings['button_' + (i + 1) + '_' + pushType] == 'brighten' && settings['button_' + (i + 1) + '_' + pushType + '_brighten']) dimmingSet = true
            if(settings['button_' + (i + 1) + '_' + pushType] == 'dim' && settings['button_' + (i + 1) + '_' + pushType + '_dim']) dimmingSet = true
        }
    }
    if(customActionsSetup == 'actionsAndDevices'){
        for(int i = 0; i < buttonMap.size(); i++){
            actionMap.each{it->
                if(actionMap.'type' == 'dim' && settings['button_' + (i + 1) + '_' + pushType + '_' + it.'action']) dimmingSet = true
            }
        }
    }
    if(!dimmingSet) return
    fieldTitle = 'Enter dimming steps for Push (optional, default 8):'
    if(pushType == 'hold') fieldTitle = 'Enter dimming steps for Long Push (optional, default 20):'
    displayTextField(fieldName,fieldTitle,'number',false)
// Need to figure out how to display this for either Push or Hold, not both
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
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup']) return
    if(!checkAnyDeviceSet()) return
    if(anyErrors) return
    
    section(){}
    
    hidden = true
    if(settings['start_timeType'] && !settings['stop_timeType']) hidden = false
    if(settings['disable']) hidden = true
    if(settings['start_time'] && settings['start_time'] == settings['stop_time']) hidden = false
    if(!validateTimes('start')) hidden = false
    if(!validateTimes('stop')) hidden = false

    section(hideable: true, hidden: hidden, getTimeSectionTitle()){
        if(!settings['start_timeType'] && validateTimes('start') && validateTimes('stop') && !settings['days']  && !settings['includeDates'] && !settings['excludeDates']) displayInfo('This will limit when this ' + appDescription + ' is active. You can create another ' + appDescription + ' "app" to do something else for opposite times/days.')
        if(settings['start_time'] && settings['start_time'] == settings['stop_time']) displayError('You can\'t have the same time to start and stop.')

        displayTypeOption('start')
        displayTimeOption('start')
        displaySunriseTypeOption('start')
        displayTypeOption('stop')
        displayTimeOption('stop')
        displaySunriseTypeOption('stop')
        displayDaysOption()
        displayDatesOptions()
    }
}

def getTimeSectionTitle(){
    if(!settings['start_timeType'] && !settings['stop_timeType'] && !settings['days']) return 'Click to set with schedule (optional)'

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

    List dayList=[]
    settings['days'].each{
        dayList.add(it)
    }
    dayText = dayList.join(', ')
    
    if(settings['start_timeType'] && settings['days']) sectionTitle += ' on: ' + dayText
    if(settings['start_timeType']) sectionTitle += '</b>'
    if(!settings['days']) sectionTitle += moreOptions
    
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
    if(type == 'stop' && (!settings['start_timeType'] || !validateTimes('start'))) return
    
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
    fieldList = ['time':'Start at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
    if(type == 'stop') fieldList = ['none':'Don\'t stop','time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
    input fieldName, 'enum', title: fieldTitle, multiple: false, width: getTypeOptionWidth(type), options: fieldList, submitOnChange:true
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
    timeUnits = 'minutes'
    if(advancedSetup) timeUnits = 'seconds'
    fieldTitle = timeUnits.capitalize() + ' ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType'] + ':'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'number', title: fieldTitle, width: getTypeOptionWidth(type), submitOnChange:true

    message = 'Enter the number of ' + timeUnits + ' ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType'] + ' to start the schedule. Required.'
    if(!settings[type + '_sunOffset']) displayInfo(message)
    if(!validateSunriseMinutes(type)) displayWarning(message)
}

def displayDaysOption(){
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return

    fieldName = 'days'
    fieldTitle = 'On these days (optional; defaults to all days):'
    if(!settings[fieldName]) fieldTitle = 'On which days (optional; defaults to all days)?'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    options = ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday']
    displaySelectField(fieldName,fieldTitle,options,true,false)
    //input fieldName, 'enum', title: fieldTitle, multiple: true, width: 12, options: ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday'], submitOnChange:true
}

def displayDatesOptions(){
    displayIncludeDates()
    displayExcludeDates()
}

def displayIncludeDates(){
    displayWarning(parent.getDateProcessingErrors(app.id))
    displayInfo(dateList)
    fieldName = 'includeDates'
    fieldTitle = 'Only on dates:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, "textarea", title: fieldTitle, submitOnChange:true
}

def displayExcludeDates(){
    fieldName = 'excludeDates'
    fieldTitle = 'Not on dates:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
input fieldName, "textarea", title: fieldTitle, submitOnChange:true
    deviceText = 'it'
    if(settings['device'].size() > 1) deviceText = 'them'
    infoTip = 'Enter which date(s) to restrict or exclude this ' + appDescription + ' routine. "Only on dates" are when this ' + appDescription + ' will work, for instance if you want ' + deviceText + ' to do a specific thing on Christmas. \
"Not on" dates are when this ' + appDescription + ' will not apply, for instance to set ' + deviceText + ' to do something any other day. Rules:\n\
	• Year is optional, but would only apply to that <i>one day</i>. If no year is entered, it will repeat annually. \
<i>Example: "12/25/' + (new Date(now()).format('yyyy').toInteger() - 1) + '" will never occur in the future, because that\'s how time works.</i>\n\
	• Enter dates as month/day ("mm/dd") format, or day.month ("dd.mm"). You can also use Julian days of the year as a 3-digit number ("ddd"). \
<i>Example: Christmas could be entered as 12/25, 25.12 or 359 [the latter only true for non-leap years, otherwise 360].</i>\n\
	• Separate multiple dates with a comma (or semicolon). \
<i>Example: "12/25, 1/1" is Christmas and New Year\'s Day.</i>\n\
	• Use a hyphen to indicate a range of dates. \
<i>Example: "12/25-1/6" are the 12 days of Christmas.</i>\n\
    	• The "days" options above will combine with the dates. \
<i>Example: Selecting Monday and entering "12/25" as an "only on" date would only allow the ' + appDescription + ' if Christmas is on a Monday.</i>\n\
	• You can mix and match formats (even tho you probably shouldn\'t), and individual dates with ranges. And the order doesn\'t matter. \
<i>Example: "001, 31.10, 12/25/' + (new Date(now()).format('yy').toInteger()) + '-12/31/' + (new Date(now()).format('yyyy').toInteger()) + '" is every Halloween, Christmas to New Years\' Eve of ' + (new Date(now()).format('yyyy').toInteger()) + ', and every New Years\' Day.</i>\n\
	• If a date falls within both "only on" and "not on", it will be treated as "not on".\n\
	• If any date within a date range is invalid, the entire date range will be ignored. <i>Example: 02/01-02/29 would only be used on a Leap Year (to do all of February including 2/29, enter "2/1-2/28, 2/29").</i>'

    displayInfo(infoTip)
}

def displayIfModeOption(){
    if(!settings['device']) return
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(!checkAnyDeviceSet()) return
    if(anyErrors) return
    
    fieldName = 'ifMode'
    sectionTitle = 'Click to select with what Mode (optional)'
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
    displayInfo('This will limit the ' + appDescription + ' from being active to only when Hubitat\'s Mode is as selected. You can create another ' + appDescription + ' "app" to do something else for other Modes.')
    fieldTitle = 'Only when the Mode is:'
    displayModeSelectField(fieldName,fieldTitle,options,true,false)
}

def displayPeopleOption(){
// Use devices selected in Master app
// Add check for if no presense devices
    if(!settings['device']) return
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(!checkAnyDeviceSet()) return
    if(anyErrors) return

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
    
    if(!settings['personHome'] && !settings['personNotHome']) sectionTitle = 'Click to select with people (optional)'
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
    }
}
def displayPersonHomeComplete(fieldName, fieldCapability){
    if(!settings[fieldName]) return
    fieldTitle = 'Only if home:'
    displayDeviceSelectField(fieldName,fieldTitle,fieldCapability,true)
}
def displayPersonHomeIncomplete(fieldName, fieldCapability){
    if(settings[fieldName]) return
    if(!settings['personNotHome']) displayInfo('This will limit the ' + appDescription + ' from being active to only when those selected are home and/or away. They can be combined (as if Person A is home AND Person B is away). You can create another ' + appDescription + ' "app" to do something else for the opposite.')
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

// Returns true if showing button (ie 2 button has not Middle button)
def checkIfShowButton(number){
    if(number == 0) return true
    if(number == 1 && numberOfButtons > 3) return true
    if(number == 2 && numberOfButtons == 5) return true
    if(number == 3 && numberOfButtons > 3) return true
    if(number == 4) return true
}
                                     
def getButtonNumbers(){
    if(!settings['device']) return
    settings['device'].each{
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
        if(settings['button_' + buttonNumber + '_push']){
            if(!settings['button_' + buttonNumber + '_push_' + settings['button_' + buttonNumber + '_push']]) return settings['button_' + buttonNumber + '_push']
        }
        if(settings['button_' + buttonNumber + '_hold']){
            if(settings['button_' + buttonNumber + '_hold_' + settings['button_' + buttonNumber + '_hold']]) return settings['button_' + buttonNumber + '_hold']
        }
    }
    return false
}
                                     
def compareDeviceLists(firstDeviceMulti,secondDeviceMulti,firstAction,secondAction){
    if(!firstDeviceMulti) return
    if(!secondDeviceMulti) return

    if(firstAction == 'on' && !['off', 'toggle'].contains(secondAction)) return
    if(firstAction == 'off' && !['on', 'resume'].contains(secondAction)) return
    if(firstAction == 'toggle' && !['off', 'on'].contains(secondAction)) return
    if(firstAction == 'dim' && !['brighten', 'off', 'resume'].contains(secondAction)) return
    if(firstAction == 'brighten' && !['dim', 'off', 'resume'].contains(secondAction)) return
    if(firstAction == 'resume' && !['dim', 'brighten', 'off'].contains(secondAction)) return
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
    fieldOptions = setActionsPerButtonDefault(fieldOptions,fieldValue,buttonNumber)
    fieldOptions = setActionsPerButtonType(fieldOptions,fieldValue,buttonNumber)
    fieldOptions = setActionsPerButtonOther(fieldOptions,fieldValue,buttonNumber)
    
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
def setActionsPerButtonDefault(fieldOptions,fieldValue,buttonNumber){
    if(!fieldOptions) fieldOptions = [:]
    it = getActionFromButtonNumber(buttonNumber)      // 2) Default
    if(setActionsPerButtonProcess(fieldOptions,it,fieldValue)) fieldOptions[it.action] = it.'actionText'.capitalize()
    return fieldOptions
}
def setActionsPerButtonType(fieldOptions,fieldValue,buttonNumber){
    if(!fieldOptions) fieldOptions = [:]
    actionMap.findAll{it->      // 3) Correct "type"
        if(it.'type' == buttonMap[buttonNumber].'type') {
            if(setActionsPerButtonProcess(fieldOptions,it,fieldValue)) fieldOptions[it.action] = it.'actionText'.capitalize()
        }
    }
    return fieldOptions
}
def setActionsPerButtonOther(fieldOptions,fieldValue,buttonNumber){
    actionMap.findAll{it->      // 4) The rest
        if(it.'type' != buttonMap[buttonNumber]['type'] && it.'defaultButton' != buttonNumber + 1) {
            if(setActionsPerButtonProcess(fieldOptions,it,fieldValue)) fieldOptions[it.action] = it.'actionText'.capitalize()
        }
    }
    return fieldOptions
}
def setActionsPerButtonProcess(fieldOptions,actionMapLine,fieldValue){
    if(!actionMapLine) return
    if(actionMapLine?.'advanced' && !advancedSetup) return
    if(actionMapLine.'actionText' == fieldValue) return

    return true
}

def checkAnyDeviceSet(){
    if(customActionsSetup == 'automap') return true
    returnValue = false
    for(int pushActionLoop = 0; pushActionLoop < 2; pushActionLoop++){        // Repeat for push/hold
        pushType = 'push'
        if(pushActionLoop == 1) pushType = 'hold'
        for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
            if(!checkIfShowButton(buttonNumber)) continue
            actionMap.any{it->
                if(settings['button_' + (buttonNumber + 1) + '_' + pushType] == it.'action' && settings['controlDevice'].size() == 1) returnValue = true
                if(settings['buttonId_' + (buttonNumber + 1) + '_' + pushType + '_' + it.'action']) returnValue = true
                return true
            }
        }
    }
    return returnValue
}

def resetPicoDevices(deviceName){
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
            actionMap.each{it->
                app.removeSetting('button_' + (buttonNumber + 1) + '_' + pushType + '_' + it.'action')
                if(customActionsSetup == 'automap') return true
                if(!checkIfShowButton(buttonNumber)) return true
                if(settings['button_' + (buttonNumber + 1) + '_' + pushType] == it.'action' && settings['controlDevice'].size() == 1){
                    app.updateSetting('button_' + (buttonNumber + 1) + '_' + pushType + '_' + it.'action', [type: "capability.switch", value: settings['controlDevice']])
                }
                setDeviceById('buttonId_' + (buttonNumber + 1) + '_' + pushType + '_' + it.'action', 'button_' + (buttonNumber + 1) + '_' + pushType + '_' + it.'action','capability.switch')
            }
        }
    }
}

def buildButtonMap(){
    return [['fullName':'Top ("On") button', 'shortName':'top', 'type':'on'],
    ['fullName':'Up button', 'shortName':'up', 'type':'dim'],
        ['fullName':'Middle button', 'shortName':'middle', 'type':'on'],
        ['fullName':'Down button', 'shortName':'down', 'type':'dim'],
        ['fullName':'Bottom ("Off") button', 'shortName':'bottom', 'type':'on']]
}
def buildActionMap(){
    return [['action':'on','actionText':'turn on','descriptionActive':'Turns on', 'description': 'Turn on','type':'on','defaultButton':1, 'advanced':false],
        ['action':'off', 'actionText':'turn off','descriptionActive':'Turns off', 'description': 'Turn off', 'type':'on', 'defaultButton':5, 'advanced':false],
        ['action':'brighten', 'actionText':'brighten','descriptionActive':'Brightens', 'description': 'Brighten', 'type':'dim', 'defaultButton':2, 'advanced':false],
        ['action':'dim', 'actionText':'dim','descriptionActive':'Dims', 'description': 'Dim', 'type':'dim', 'defaultButton':4, 'advanced':false],
        ['action':'toggle', 'actionText':'toggle','descriptionActive':'Toggles', 'description': 'Toggle', 'type':'other', 'defaultButton':3, 'advanced':false],
        ['action':'resume', 'actionText':'resume schedule','descriptionActive':'Resumes schedule (if none, turn off)', 'description': 'Resume schedule (if none, turn off)', 'type':'other', 'defaultButton':'', 'advanced':true]]
}

def getActionFromButtonNumber(buttonNumber){
     actionMap.find{it->
           if(it.'defaultButton' == (buttonNumber + 1)) returnValue = it
     }
     returnValue
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
def displaySelectField(fieldName,fieldTitle,options,multiple = false,required = true){
    width = 10
    if(!settings[fieldName]) width = 12
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(required && !settings[fieldName]) displayLabel(highlightText(fieldTitle))
    if(!required && !settings[fieldName]) displayLabel(fieldTitle)
    input name: fieldName, type: 'enum', title: '', options: options, width:width, multiple: multiple, submitOnChange:true
}
def displayDeviceSelectField(fieldName,fieldTitle,capability,multiple){
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input name: fieldName, type: capability, title:fieldTitle, multiple: multiple, submitOnChange:true
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
    putLog(1281,'trace', 'Installed')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    putLog(1287,'trace','Updated')
    unsubscribe()
    initialize()
}

def initialize() {
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))

setControlDevice()
    subscribe(settings['device'], 'pushed', buttonPushed)
    subscribe(settings['device'],, 'held', buttonPushed)
    subscribe(settings['device'],, 'released', buttonReleased)
    
    dimValue = 8
    if(settings['pushedDimmingProgressionSteps']) dimValue = settings['pushedDimmingProgressionSteps']
    atomicState.pushedDimmingProgressionFactor = parent.computeOptiomalGeometricProgressionFactor(dimValue)
    dimValue = 20
    if(settings['heldDimmingProgressionSteps']) pushedValue = settings['heldDimmingProgressionSteps']
    atomicState.heldDimmingProgressionFactor = parent.computeOptiomalGeometricProgressionFactor(dimValue)
    putLog(1306,'info','Brightening/dimming progression factor set: push ' + atomicState.pushedDimmingProgressionFactor + '; held = ' + atomicState.heldDimmingProgressionFactor + '.')

    setTime()

    putLog(1328,'trace','Initialized')
}

def buttonPushed(evt){
    // If not correct day, return nulls
    if(!checkIncludeDates()) return

    // if not between start and stop time, return nulls
    if(atomicState.stop && !parent.checkNowBetweenTimes(atomicState.start, atomicState.stop, app.label)) return

    buttonNumber = assignButtonNumber(evt.value.toInteger())

    // Needs to be state since we're passing back and forth to parent for progressive dim and brightening
    if(evt.name == 'pushed') action = 'push'
    if(evt.name == 'held') action = 'hold'
    
    putLog(1326,'trace',action.capitalize() + ' button ' + buttonNumber + ' of ' + device)

    if(!actionMap) switchActions = buildActionMap()

    switchActions.each { switchAction ->
        device = settings['button_' + buttonNumber + '_' + action + '_' + switchAction.'action']
        device.each{singleDevice->
            
            // need to get nextLevel here
            if(action == 'push') level = parent._getNextLevelDimmable(singleDevice, switchAction.'action', atomicState.pushedDimmingProgressionFactor, app.label)
            levelMap = parent.getLevelMap('brightness',level,app.id,'',childLabel)         // dim, brighten
            
            stateMap = parent.getStateMapSingle(singleDevice,switchAction.'action',app.id,app.label)       // on, off, toggle
            if(level) stateMap = parent.getStateMapSingle(singleDevice,'on',app.id,app.label)

            fullMap = parent.addMaps(stateMap, levelMap)
            if(fullMap) putLog(1342,'trace','Updating settings for ' + singleDevice + ' to ' + fullMap)
            parent.mergeMapToTable(singleDevice.id,fullMap,app.label)
        }
        if(action == 'resume') parent.resumeDeviceScheduleMulti(device,app.label)       //??? this function needs to be rewritten, I think
        if(action == 'hold') holdNextLevelMulti(device,switchAction.'action')

        parent.setDeviceMulti(device,app.label)
    }
}

// place holder until I can redo my pico setups to not throw an error
def buttonHeld(evt){
}

def buttonReleased(evt){
    buttonNumber = assignButtonNumber(evt.value.toInteger())

    putLog(1359,'trace','Button ' + buttonNumber + ' of ' + device + ' released, unscheduling all')
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

def setControlDevice(){
    if(settings['customActionsSetup'] == 'automap') {
        if(!actionMap) actionMap = buildActionMap()
        actionMap.each{it->
            if(it.'defaultButton') {
                app.updateSetting('button_' + it.'defaultButton' + '_push_' + it.'action', [type: "capability.switch", value: settings['controlDevice']])
                if(settings['setHold']) app.updateSetting('button_' + it.'defaultButton' + '_hold_' + it.'action', [type: "capability.switch", value: settings['controlDevice']])
            }
        }
    }
}

// This is the schedule function that sets the level for progressive dimming
def runSetProgressiveLevel(data){
    if(!getSetProgressiveLevelDevice(data.device, data.action)) {
        putLog(1387,'trace','Function runSetProgressiveLevel returning (no matching device)')
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
    putLog(1446,'info','Start time set to ' + parent.getPrintDateTimeFormat(setTime))
    return true
}

def setStopTime(){
    if(!settings['stop_timeType'] || settings['stop_timeType'] == 'none') return
    if(atomicState.stop > atomicState.start) return
    setTime = setStartStopTime('stop')
    if(setTime < atomicState.start) setTime += parent.CONSTDayInMilli()
    atomicState.stop  = setTime
    putLog(1456,'info','Stop time set to ' + parent.getPrintDateTimeFormat(setTime))
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

def checkIncludeDates(){
    if(!atomicState.includeDates) return true
    if(!atomicState.includeDates[now().format('yyyy')]) processDates()
    if(atomicState?.includeDates[now().format('yyyy')].contains(now().format('D'))) return true
}
def processDates(){
    atomicState.remove('includeDates')
    if(!settings['days'] && !settings['includeDates'] && !settings['excludeDates']) return
    currentYear = new Date(now()).format('yyyy').toInteger()
    includeDatesValue = settings['includeDates']
    if(!settings['includeDates'] && (settings['days'] || settings['excludeDates'])) includeDatesValue = '1/1-12/31'
    atomicState.'includeDates' = [(currentYear):parent.processDates(settings['includeDates'], settings['excludeDates'], settings['days'], app.id, true)]
}
//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,type = 'trace',message = null){
    return parent.putLog(lineNumber,type,message,app.label,,getLogLevel())
}
