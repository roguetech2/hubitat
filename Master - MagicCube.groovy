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
*  Name: Master - MagicCube
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master%20-%20MagicCube.groovy
*  Version: 0.4.2.4
* 
***********************************************************************************************************************/

// TO-DO: Allow actions per side (rather than flip90 or flip180)
// TO-DO: Allow selecting rooms??
// TO-DO: Add support for Hubitat Package Manager? https://community.hubitat.com/t/beta-hubitat-package-manager/38016

definition(
    name: 'Master - MagicCube',
    namespace: 'master',
    author: 'roguetech',
    description: 'MagicCubes',
    parent: 'master:Master',
    category: 'Convenience',
    importUrl: 'https://raw.githubusercontent.com/roguetech2/hubitat/master/Master%20-%20MagicCube.groovy',
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
    buttonMap = buildButtonMap()
    actionMap = buildActionMap()
    resetDevices()
    anyErrors = checkAnyErrors()
    install = formComplete()
    thisType = 'cube'
    thisDescription = 'MagicCube'        // Used with schedule, people, ifMode

    page(name: 'setup', install: install, uninstall: true) {
        if(!app.label){
            section(){
                displayNameOption()
            }
        }
        if(app.label){
setUILinks()
            allDeviceOptions = parent.getDeviceList()
            controllerDeviceOptions = controllerOptionProcessParentDeviceList()
            if(allDeviceOptions && !controllerDeviceOptions) {
                section(){
                    displayError('You don\'t have any MagicCube devices selected in the Master app. Update the device selection in the Master app to include MagicCube(s).')
                    displayInfo('MagicCube devices are identified by having a "sideUp" setting. If you have a MagicCube installed, check the device status page under "Current States" (right side). If there is no "sideUp" value, and it\'s a new MagicCube, try flipping it (then refresh this page). Otherwise, check to make sure the driver is "Aqara Cube T1 Pro".') 
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
            anyErrors = displayCustomizeActionsAndDevicesErrors(buttonNumber,fieldOptions, actionMap[actionMapItem].'action')
            if(anyErrors) break
            anyErrors = displayCustomizeActionsAndDevicesErrors(buttonNumber,fieldOptions, actionMap[actionMapItem].'action')
            if(anyErrors) break
        }
        if(anyErrors) break
    }
    return anyErrors
}

def controllerOptionProcessParentDeviceListMatch(singleDevice){
            if(singleDevice.hasAttribute('sideUp')) return true
}


def displayCustomActionsOption(){
    if(!settings['device']) return
    if(!settings['controlDevice']) return
    if(anyErrors) return
    fieldName = 'customActionsSetup'
    options = ['actions':'Assign action and devices to each button','actionsAndDevices':'Customize actions and devices for each button']
    if(settings['controlDevice'].size() == 1 && settings[fieldName] != 'actionsAndDevices') {
        app.updateSetting('customActionsSetup', [type: 'enum', value: 'actions'])
        return
    }
    displayCustomActionsOptionComplete(fieldName,options)
    displayCustomActionsOptionIncomplete(fieldName,options)
    devicesText = 'device'
    if(settings['controlDevice'].size() > 1) devicesText = 'devices'
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

def displayDefineActions(){
    if(settings['customActionsSetup'] != 'actions') return
    if(!settings['controlDevice']) return
    section(hideable: true, hidden: false, getDefineActionsSectionTitle()) {
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
            displayDefineActionsButton(i)
        }
        if(!settings['advancedSetup']) displayInfo('Select Advanced Setup for more options.')
    }
}
def getDefineActionsWarningValue(){
    returnValue = ''
    for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
        if(returnValue) continue
        if(settings['button_' + buttonNumber] && !settings['button_' + buttonNumber + '_' + settings['button_' + buttonNumber]]){
            actionMap.find{it->
                if(it.'action' == settings['button_' + buttonNumber]){
                    returnValue = it.'description'
                }
            }
        }
    }
    return returnValue
}
def getDefineActionsSectionTitle(){
    sectionTitle = ''
    for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
        if(!checkIfShowButton(buttonNumber)) continue
        if(!settings['button_' + (buttonNumber + 1)]) continue
        if(sectionTitle) sectionTitle += '\n'
        //sectionTitle += buttonMap[i]['fullName'] + ': ' + getActionFromButtonNumber(i)['descriptionActive'] + ''
        actionMap.find{it->
            if(it.'action' == settings['button_' + (buttonNumber + 1)]) actionText = it.'descriptionActive'
        }
        if(settings['controlDevice'].size() > 1) sectionDevice = settings['button_' + (buttonNumber + 1) + '_' + settings['button_' + (buttonNumber + 1)]]
        sectionTitle += buttonMap[buttonNumber]['fullName'].capitalize() + ': ' + actionText.capitalize() + ' ' + sectionDevice
    }
    if(!sectionTitle) sectionTitle = 'Select device(s) for each/any action:' + expandText
    return sectionTitle
}
def displayDefineActionsButton(buttonNumber){
    fieldName = 'button_' + (buttonNumber + 1)
    fieldTitle = buttonMap[buttonNumber]['fullName'].capitalize() + ' action:'
    fieldOptions = setActionsPerButton(buttonNumber,settings[fieldName], true)
    displaySelectField(fieldName,fieldTitle,fieldOptions,false,false)
    displayDefineActionsDevice(buttonNumber,fieldOptions) 
}
def displayDefineActionsDevice(buttonNumber,fieldOptions){
    buttonAction = settings['button_' + (buttonNumber + 1)]
    if(!buttonAction) return
    if(!settings['controlDevice']) return
    actionMap.find{it->
        if(it.'action' == buttonAction) fieldText = it.'description'
    }
    if(settings['controlDevice'].size() == 1) return
    fieldName = 'buttonId_' + (buttonNumber + 1) + '_' + buttonAction
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
    
    displayCustomizeActionsAndDevicesSections()
}
def displayCustomizeActionsAndDevicesSections() {
    for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
        sectionLabel = ''
        if(!checkIfShowButton(buttonNumber)) continue
        fieldOptions = setActionsPerButton(buttonNumber)
        hidden = false
        setErrors = ''
        for(int actionMapItem = 0; actionMapItem < actionMap.size(); actionMapItem++){ 
            if(settings['button_' + (buttonNumber + 1) + '_' + actionMap[actionMapItem].'action']) {
                if(sectionLabel) sectionLabel += '\n'
                sectionLabel += '  ' + actionMap[actionMapItem].'descriptionActive' + ' ' + settings['button_' + (buttonNumber + 1) + '_' + actionMap[actionMapItem].'action']
                hidden = true
                
                setErrors = displayCustomizeActionsAndDevicesErrors(buttonNumber,fieldOptions, actionMap[actionMapItem].'action')
                if(setErrors) hidden = false
            }
        }
        if(anyErrors && !setErrors) hidden = true
        if(sectionLabel) sectionLabel = '\n' + sectionLabel
        sectionLabel = buttonMap[buttonNumber].'fullName'.capitalize() + expandText + sectionLabel
        section(hideable: true, hidden: hidden, sectionLabel) {
            displayCustomizeActionsAndDevicesButtons(buttonNumber,fieldOptions)
        }
    }
}
def displayCustomizeActionsAndDevicesButtons(buttonNumber, fieldOptions) {
    fieldOptions.eachWithIndex{fieldAction, fieldOptionNumber ->
        actionMap.each{it->
            if(it.'action' == fieldAction.key){
                displayDefineActionsAndDeviceField(buttonNumber, it, true)
                if(anyErrors) displayError(displayCustomizeActionsAndDevicesErrors(buttonNumber,fieldOptions, fieldAction.key))
            }
        }
    }
}

def displayDefineActionsAndDeviceField(buttonNumber, actionLine,populated = null){
    fieldName = 'buttonId_' + (buttonNumber + 1) + '_' + actionLine.'action'
    fieldTitle = '<b>' + actionLine.'descriptionActive' + '</b>:'
    if(!settings[fieldName]) fieldTitle = actionLine.'description' + ' <font color="gray">(Select devices)</font>'

    deviceOptions = [:]
    settings['controlDevice'].each{it->
        deviceOptions.put([it.'id',it.'label'])
    }
    displaySelectField(fieldName,fieldTitle,deviceOptions,true,false)
}

def displayCustomizeActionsAndDevicesErrors(buttonNumber,fieldOptions, action){
    if(!fieldOptions) return
    firstActionNumber = fieldOptions.findIndexOf{it.key==action}
    if(firstActionNumber == 0) return
    errorMessage = ''
    firstDeviceName = 'button_' + (buttonNumber + 1) + '_' + action
    if(!settings[firstDeviceName]) return
    fieldOptions.find{it->
        if(it.key == action) return
        secondDeviceName = 'button_' + (buttonNumber + 1) + '_' + it.key
        if(compareDeviceLists(settings[firstDeviceName],settings[secondDeviceName],action,it.key)) {
            if(errorMessage) errorMessage += '\n'
            errorMessage += 'Can\'t set the same device to both ' + action + ' and ' + it.key + ' with the same button.'
        }
    }
    return errorMessage
}

def displayDimmingProgressionOption(){
    if(!settings['advancedSetup']) return
    if(!checkAnyDeviceSet()) return
    if(anyErrors) return
    dimmingSet = false
    if(customActionsSetup == 'actions' || customActionsSetup == 'actionsAndDevices'){
        for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
// Replace with actionMap type = 'dim'
            if(settings['button_' + (buttonNumber + 1)] == 'brighten' && settings['button_' + (buttonNumber + 1) + '_brighten']) dimmingSet = true
            if(settings['button_' + (buttonNumber + 1)] == 'dim' && settings['button_' + (buttonNumber + 1) + '_dim']) dimmingSet = true
        }
    }
    if(!dimmingSet) return
    sectionTitle = 'Click to set dimming steps (Optional)'
    if(settings['dimmingProgressionSteps']) sectionTitle = 'Dimming steps: ' + settings['dimmingProgressionSteps']
    if(!sectionTitle) sectionTitle = '<b>Set dimming steps:</b>'
    
    section(hideable: true, hidden: true, sectionTitle + expandText) {
        infoTip = 'Number of steps it takes to brighten (or dim) from 1 to 100%.'
        if(!settings['dimmingProgressionSteps']) infoTip = 'This is the number of steps it takes to brighten (or dim) from 1 to 100% brightness. It uses a geometric progression. For instance, with 10 steps, pressing the brighten button would go from 1% to 2%, and then to 4, 7, 11, 17, 25, 36, 52, 74, and finally 100%.'
        displayInfo(infoTip)
        fieldName = 'dimmingProgressionSteps'
        displayDimmingProgressionOptionComplete(fieldName)
        displayDimmingProgressionOptionIncomplete(fieldName)
    }
}
def displayDimmingProgressionOptionComplete(fieldName){
    if(!settings[fieldName]) return
    
    dimmingSet = false
    for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
// Replace with actionMap type = 'dim'
        if(settings['button_' + (buttonNumber + 1)] == 'brighten' && settings['button_' + (buttonNumber + 1) + '_brighten']) dimmingSet = true
        if(settings['button_' + (buttonNumber + 1)] == 'dim' && settings['button_' + (buttonNumber + 1) + '_dim']) dimmingSet = true
    }
    if(!dimmingSet) return
    fieldTitle = 'Dimming steps:'

    displayTextField(fieldName,fieldTitle,'number',false)
}
def displayDimmingProgressionOptionIncomplete(fieldName){
    if(settings[fieldName]) return
    dimmingSet = false
        for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
// Replace with actionMap type = 'dim'
            if(settings['button_' + (buttonNumber + 1)] == 'brighten' && settings['button_' + (buttonNumber + 1) + '_brighten']) dimmingSet = true
            if(settings['button_' + (buttonNumber + 1)] == 'dim' && settings['button_' + (buttonNumber + 1) + '_dim']) dimmingSet = true
        }
    if(!dimmingSet) return
    fieldTitle = 'Enter dimming steps (optional, default 8):'
    displayTextField(fieldName,fieldTitle,'number',false)
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
    if(settings['start_time'] && settings['start_time'] == settings['stop_time']) hidden = false
    if(!validateTimes('start')) hidden = false
    if(!validateTimes('stop')) hidden = false

    section(hideable: true, hidden: hidden, getTimeSectionTitle()){
        if(!settings['start_timeType'] && validateTimes('start') && validateTimes('stop') && !settings['days']  && !settings['includeDates'] && !settings['excludeDates']) displayInfo('This will limit when this ' + thisDescription + ' is active. You can create another ' + thisDescription + ' "app" to do something else for opposite times/days.')
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

// Returns true if showing button (ie 2 button has not Middle button)
def checkIfShowButton(number){
    if(buttonMap[number].'advanced' && !settings['advancedSetup']) return false
    if(!parent.checkIsColorMulti(settings['controlDevice']) && buttonMap[number].'type' == 'dim') return false
    return true
}

def checkDefineActionButNoDevice(){
    for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
        if(settings['button_' + (buttonNumber + 1)]){
            if(!settings['button_' + (buttonNumber + 1) + '_' + settings['button_' + (buttonNumber + 1)]]) return settings['button_' + (buttonNumber + 1)]
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
def setActionsPerButton(buttonNumber,fieldValue = false, unique = false){
    fieldOptions = [:]
    fieldOptions = setActionsPerButtonPreset(fieldOptions,fieldValue)
    //fieldOptions = setActionsPerButtonDefault(fieldOptions,fieldValue,buttonNumber)
    fieldOptions = setActionsPerButtonType(fieldOptions,fieldValue,buttonNumber)
    fieldOptions = setActionsPerButtonOther(fieldOptions,fieldValue,buttonNumber)
    
    if(!settings['advancedSetup']) {        // Remove any already picked
        for(int i = 0; i < buttonMap.size(); i++){
            if(i != buttonNumber){
                actionMap.each{it->
                    if(settings['button_' + (i + 1)] == it.'action') fieldOptions.remove(it.'action')
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
        if(it.'type' != buttonMap[buttonNumber]['type']) {
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
    for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
        if(!checkIfShowButton(buttonNumber)) continue
        for(int actionNumber = 0; actionNumber < actionMap.size(); actionNumber++){
            if(settings['button_' + (buttonNumber + 1)] == actionMap[actionNumber].'action' && settings['controlDevice'].size() == 1) return true
            if(settings['button_' + (buttonNumber + 1) + '_' + actionMap[actionNumber].'action']) return true
            return true
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
        for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
            for(int actionMapItem = 0; actionMapItem < actionMap.size(); actionMapItem++){
                app.removeSetting('button_' + (buttonNumber + 1) + '_' + actionMap[actionMapItem].'action')
                if(!settings['controlDevice']) continue
                if(!checkIfShowButton(buttonNumber)) continue
                if(settings['button_' + (buttonNumber + 1)] == actionMap[actionMapItem].'action' && settings['controlDevice'].size() == 1){
                    app.updateSetting('button_' + (buttonNumber + 1) + '_' + actionMap[actionMapItem].'action', [type: "capability.switch", value: settings['controlDevice']])
                }
                if(settings['button_' + (buttonNumber + 1)] != actionMap[actionMapItem].'action') app.removeSetting('buttonId_' + (buttonNumber + 1) + '_' + actionMap[actionMapItem].'action')
                setDeviceById('buttonId_' + (buttonNumber + 1) + '_' + actionMap[actionMapItem].'action', 'button_' + (buttonNumber + 1) + '_' + actionMap[actionMapItem].'action','switch')
            }
        }
}
def buildButtonMap(){
    return [['fullName':'flip 90°', 'shortName':'flip90', 'type':'on','eventType':'pushed','advanced':false],
            ['fullName':'flip 180°', 'shortName':'flip180', 'type':'on','eventType':'pushed','advanced':false],
            ['fullName':'rotate clockwise', 'shortName':'clockwise', 'type':'dim','eventType':'released','advanced':false],
    ['fullName':'rotate counter-clockwise', 'shortName':'counterClockwise', 'type':'dim','eventType':'held','advanced':false],
        ['fullName':'shake', 'shortName':'shake', 'type':'on','eventType':'doubleTapped','advanced':true]]
}

def getActionFromButtonNumber(buttonNumber){
     actionMap.find{it->
           if(it.'defaultButton' == (buttonNumber + 1)) returnValue = it
     }
     returnValue
}


/* ************************************************************************ */
/*                                                                          */
/*                                 End UI.                                  */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                                 End UI.                                  */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                                 End UI.                                  */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                                 End UI.                                  */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                                 End UI.                                  */
/*                                                                          */
/* ************************************************************************ */

def installed() {
    putLog(581,'trace','Installed')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    putLog(587,'trace','Updated')
    unsubscribe()
    initialize()
}

def initialize() {
    putLog(593,'trace','Initializing')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))

    subscribe(device, "pushed.1", buttonEvent)
    subscribe(device, "pushed.2", buttonEvent)
    subscribe(device, "pushed.3", buttonEvent)
    subscribe(device, "pushed.4", buttonEvent)
    subscribe(device, "pushed.5", buttonEvent)
    subscribe(device, "pushed.6", buttonEvent)
    
    //doubleTapped, release and hold used by kkossev's driver
    subscribe(device, "doubleTapped", buttonEvent)
    subscribe(device, "released", buttonEvent)
    subscribe(device, "held", buttonEvent)
        
    dimValue = 8
    if(settings['pushedDimmingProgressionSteps']) dimValue = settings['pushedDimmingProgressionSteps']
    atomicState.pushedDimmingProgressionFactor = parent.computeOptiomalGeometricProgressionFactor(dimValue)
    dimValue = 20
    if(settings['heldDimmingProgressionSteps']) pushedValue = settings['heldDimmingProgressionSteps']
    atomicState.heldDimmingProgressionFactor = parent.computeOptiomalGeometricProgressionFactor(dimValue)
    putLog(614,'info','Brightening/dimming progression factor set: push ' + atomicState.pushedDimmingProgressionFactor + '; held = ' + atomicState.heldDimmingProgressionFactor + '.')

    setTime()

    putLog(618,'trace','Initialized')
}

def buttonEvent(evt){
    // If not correct day, return nulls
    if(!checkIncludeDates()) return
    // if not between start and stop time, return nulls
    if(atomicState.stop && !parent.checkNowBetweenTimes(atomicState.start, atomicState.stop, app.label)) return
    if(!getActive()) return

    buttonNumber = convertDriver(evt)     // Sets atomicState.buttonNumber to action corresponding to cubeActions
    actionMap = buildActionMap()

    for(int actionMapItem = 0; actionMapItem < actionMap.size(); actionMapItem++){
        if(!settings['button_' + (buttonNumber + 1) + '_' + actionMap[actionMapItem].'action']) continue
        putLog(633,'debug','' + settings['button_' + (buttonNumber + 1) + '_' + actionMap[actionMapItem].'action'] + ' action captured as ' + actionMap[actionMapItem].'action' + ' (event = ' + evt.name + '; side = ' + evt.value + ').')
        doActions(settings['button_' + (buttonNumber + 1) + '_' + actionMap[actionMapItem].'action'],actionMap[actionMapItem].'action')
    }
}

def doActions(device,action){
    if(!device) return
    putLog(640,'trace','Set ' + device + ' as ' + action)
    
    device.each{singleDevice->
        if(action == 'dim' || action == 'brighten') level = parent._getNextLevelDimmable(singleDevice, action, app.label)
        levelMap = parent.getLevelMap('brightness',level,app.id,'',childLabel)         // dim, brighten

        stateMap = parent.getStateMapSingle(singleDevice,action,app.id,app.label)       // on, off, toggle
        if(level) stateMap = parent.getStateMapSingle(singleDevice,'on',app.id,app.label)
        
        fullMap = parent.addMaps(stateMap,levelMap)
        if(fullMap) putLog(650,'trace','Updating settings for ' + singleDevice + ' to ' + fullMap)
        parent.mergeMapToTable(singleDevice.id,fullMap,app.label)
    }
    if(action == 'resume') parent.resumeDeviceScheduleMulti(device,app.label)
    parent.setDeviceMulti(device,app.label)
}

// Sets atomicState.buttonNumber to string of action
// Currently converts from kkossev's T1 driver to match veeceeoh's Aqara Mi driver
// Is there a way to determine which driver is used?
// (1) shaking
// (2) 90flip
// (3) 180flip
// (4) slide
// (5) knock
// (6) clockwise
// (7) counterclockwise
def convertDriver(evt){
   // cubeActions = ['shake', 'flip90', 'flip180', 'slide', 'knock', 'clockwise', 'counterClockwise'] // Need to put this in the UI, should be state variable
    if(!atomicState.priorSide) putLog(669,'warn','Prior button not known. If this is not the first run of the app, this indicates a problem.')

// Could be mutliple priors sices (if multiple cubes) - need to make it a map
    priorSide = atomicState.priorSide
    atomicState.priorSide = evt.value

    buttonMap = buildButtonMap()
    for(int buttonNumber = 0; buttonNumber < buttonMap.size(); buttonNumber++){
        if(evt.name == buttonMap[buttonNumber].'eventType'){
            if(evt.name != 'pushed') return buttonNumber
        }
        if(evt.value == '1' && priorSide == '6') return 1
        if(evt.value == '2' && priorSide == '5') return 1
        if(evt.value == '3' && priorSide == '4') return 1
        if(evt.value == '4' && priorSide == '3') return 1
        if(evt.value == '5' && priorSide == '2') return 1
        if(evt.value == '6' && priorSide == '1') return 1
        return 0
    }
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
def buildActionMap(){
    return [['action':'on','actionText':'turn on','descriptionActive':'Turns on', 'description': 'Turn on','type':'on', 'advanced':false],
        ['action':'off', 'actionText':'turn off','descriptionActive':'Turns off', 'description': 'Turn off', 'type':'on', 'advanced':false],
        ['action':'brighten', 'actionText':'brighten','descriptionActive':'Brightens', 'description': 'Brighten', 'type':'dim', 'advanced':false],
        ['action':'dim', 'actionText':'dim','descriptionActive':'Dims', 'description': 'Dim', 'type':'dim', 'advanced':false],
        ['action':'toggle', 'actionText':'toggle','descriptionActive':'Toggles', 'description': 'Toggle', 'type':'other', 'advanced':false],
        ['action':'resume', 'actionText':'resume schedule','descriptionActive':'Resumes schedule (if none, turn off)', 'description': 'Resume schedule (if none, turn off)', 'type':'other', 'advanced':true]]
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
    moreOptions = ' (click for more options)'
    expandText = ' (Click to expand/collapse)'
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

// Not used by sensor app (yet)
def displayControllerOption(){
    if(thisApp == 'schedule') return
    if(!app.label) return
    fieldName = 'device'
    resetControllerDevices(fieldName)
    fieldOptions = ''
    if(settings['controllerButtonValue'] == null) app.updateSetting('controllerButtonValue', [type: 'bool', value: 'true'])
    if(settings['controllerButtonValue']){
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
    if(thisType == 'cube'){
        fieldTitle = 'MagicCube:'
        if(settings[fieldName].size() > 1) fieldTitle = 'MagicCubes:'
    }
    if(thisType == 'pico'){
        fieldTitle = 'Pico controller:'
        if(settings[fieldName].size() > 1) fieldTitle = 'Pico controllers:'
    }

    if(fieldOptions) displaySelectField(fieldName, fieldTitle, fieldOptions, true, true, 'controllerButton')
    if(!fieldOptions) displayDeviceSelectField(fieldName, fieldTitle, 'capability.pushableButton', true, 'controllerButton')
}
def displayControllerOptionIncomplete(fieldName,fieldOptions){
    if(settings[fieldName]) return
            if(thisType == 'pico') displayInfo('Select which Lutron Caseta(s) and/or Pico(s) to control. You can select multiple devices, but all should have the same number of buttons.')
    if(fieldOptions) displayInfo('If you don\'t see the device you want, make sure you have it selected in the Master app.')
            if(thisType == 'pico') fieldTitle = 'Select button device(s) to setup:'
            if(thisType == 'cube') fieldTitle = 'Select MagicCube(s) to setup:'
    if(fieldOptions) displaySelectField(fieldName, fieldTitle, fieldOptions, true, true, 'controllerButton')
    if(!fieldOptions) displayDeviceSelectField(fieldName, fieldTitle, 'capability.pushableButton', true, 'controllerButton')
}
def controllerOptionProcessParentDeviceList(){
    if(!allDeviceOptions) return
    controllerList = [:]
    fullList = [:]
    allDeviceOptions.each{singleDevice->
        if(singleDevice.hasCapability('PushableButton')){
            controllerMatch = controllerOptionProcessParentDeviceListMatch(singleDevice)
            if(controllerMatch) controllerList.put([singleDevice.'id',singleDevice.'label'])
            if(!controllerMatch) fullList.put([singleDevice.'id',singleDevice.'label'])
        }
    }
    return fullList.sort{it.value.toLowerCase()}
}

def displayAdvancedOption(){
    if(anyErrors) return        // Only pico or cube
    fieldName = 'advancedSetup'
    if((thisType != 'schedule' && thisType != 'sensor') && !settings['controllerDevice'] && !settings[fieldName]) return        // schedule doesn't have controllerDevice, and sensor needs advanced before controllerDevice
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
    fieldTitleTrue = 'Showing advanced options.'
    displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse, false)
}
def displayAdvancedOptionDisabled(fieldName){
    if(settings[fieldName]) return
    fieldTitleTrue = 'Keeping it simple.'
    fieldTitleFalse = 'Click to show advanced options.'
    displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse, false)
}

def displayControlDeviceOption(){
    if(thisType == 'sensor' && !settings['controllerType']) return
    if(thisType != 'schedule' && !settings['controllerDevice']) return
    if(anyErrors) return
    fieldName = 'controlDevice'
    displayControlDeviceOptionComplete(fieldName)
    displayControlDeviceOptionIncomplete(fieldName)
}
def displayControlDeviceOptionComplete(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = 'Device to control:'
    if(settings[fieldName].size() > 1) fieldTitle = 'Devices to control:'
    capabilitiesType = 'switch'
    if(settings['controlButtonValue'] == 1) capabilitiesType = 'switchLevel'
    if(settings['controlButtonValue'] == 2) capabilitiesType = 'colorMode'

    displayDeviceSelectField(fieldName,fieldTitle,'capability.' + capabilitiesType,true, 'controlButton')
}
def displayControlDeviceOptionIncomplete(fieldName){
    if(settings[fieldName]) return
    fieldTitle = 'Select all device(s) to control with the ' + thisDescription + ':'
    if(thisType == 'sensor' && settings['controllerDevice'].size() == 1) fieldTitle = 'Select all device(s) to control with the sensor:'
    if(thisType == 'sensor' && settings['controllerDevice'].size() > 1) fieldTitle = 'Select all device(s) to control with the sensors:'
    capabilitiesType = 'switch'
    if(settings['controlButtonValue'] == 1) capabilitiesType = 'switchLevel'
    if(settings['controlButtonValue'] == 2) capabilitiesType = 'colorMode'

    displayDeviceSelectField(fieldName,fieldTitle,'capability.' + capabilitiesType,true, 'controlButton')
}

def displayIfModeOption(){
    if(!settings['controllerDevice'] && thisType != 'schedule') return
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if((thisType == 'pico' || thisType == 'cube') && !checkAnyDeviceSet()) return
    if(anyErrors) return
    
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
    if(!settings['controllerDevice'] && thisType != 'schedule') return
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if((thisType == 'pico' || thisType == 'cube') && !checkAnyDeviceSet()) return
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

// Need to clean up/fix this
def getTimeSectionTitle(){
    if(!settings['start_timeType'] && !settings['stop_timeType'] && !settings['days']) return 'Click to set with schedule (Optional)'

    if(settings['start_timeType']) sectionTitle = '<b>Starting: '
    if(!settings['start_timeType'] && (settings['days'] || settings['includeDates'] || settings['excludeDates'])) sectionTitle = 'On: '
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
    
    if(settings['days']) sectionTitle += dayText
    if(settings['includeDates']) sectionTitle += ' +[included dates]'
    if(settings['excludeDates']) sectionTitle += ' +[excluded dates]'
    if(settings['start_timeType']) sectionTitle += '</b>'
    if(!settings['days'] || !settings['includeDates'] || !settings['excludeDates']) sectionTitle += moreOptions
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

def displayTimeTypeOption(type){
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
    if(advancedSetup) timeUnits = 'seconds'
    fieldTitle = timeUnits.capitalize() + ' ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType'] + ':'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'number', title: fieldTitle, width: getTypeOptionWidth(type), submitOnChange:true

    message = 'Enter the number of ' + timeUnits + ' ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType'] + ' for schedule to ' + type + '. Required.'
    if(!settings[type + '_sunOffset']) displayInfo(message)
    if(!validateSunriseMinutes(type)) displayWarning(message)
}

def displayDaysOption(){
    if(thisType == 'schedule' && !settings['start_timeType']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return

    fieldName = 'days'
    fieldTitle = 'On these days (Optional; defaults to all days):'
    if(!settings[fieldName]) fieldTitle = 'On which days (Optional; defaults to all days)?'
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
    if(thisType == 'schedule') fieldTitle = 'Dates on which to run ("include"):'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, "textarea", title: fieldTitle, submitOnChange:true
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
def displaySelectField(fieldName,fieldTitle,options,multiple = false,required = true, button = ''){
    width = 12
    if(!settings[fieldName] && button) width = 11
    if(settings[fieldName]){
        width = 10
        if(button) width = 9
    }
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
          if(!settings[buttonValue + 'Value']){
              app.updateSetting(buttonValue + 'Value', [type: 'bool', value: 'true'])
              break
          }
              app.updateSetting(buttonValue + 'Value', [type: 'bool', value: 'false'])
          break
          case 'controlButton':
          if(!settings[buttonValue + 'Value']){
              app.updateSetting(buttonValue + 'Value', [type: 'number', value: 1])
              break
          }
          if(settings[buttonValue + 'Value'] == 1){
              app.updateSetting(buttonValue + 'Value', [type: 'number', value: 2])
              break
          }
          app.removeSetting(buttonValue + 'Value')
          break
          case 'controllerTypeButton':        //used only by sensor app
          if(!settings[buttonValue + 'Value']){
              app.updateSetting(buttonValue + 'Value', [type: 'bool', value: 'true'])
              break
          }
              app.updateSetting(buttonValue + 'Value', [type: 'bool', value: 'false'])
          break
      }
}

def displayFilterButton(buttonName){
    if(buttonName == 'controllerButton'){
        if(settings[buttonName + 'Value']) {
            input buttonName, 'button', title: filterYesIcon + ' Filter', width:1
        }
        if(!settings[buttonName + 'Value']){
            input buttonName, 'button', title: filterNoIcon + ' Filter', width:1
        }
        return
    }
    if(buttonName == 'controlButton'){
        if(!settings[buttonName + 'Value']) {
            input buttonName, 'button', title: filterSwitchIcon + ' Filter', width:1
        }
        if(settings[buttonName + 'Value'] == 1){
            input buttonName, 'button', title: filterLightIcon + ' Filter', width:1
        }
        if(settings[buttonName + 'Value'] == 2){
            input buttonName, 'button', title: filterColorIcon + ' Filter', width:1
        }
    }
    if(buttonName == 'controllerTypeButton'){
        if(settings[buttonName + 'Value']) {
            input buttonName, 'button', title: filterYesIcon + ' Filter', width:1
        }
        if(!settings[buttonName + 'Value']){
            input buttonName, 'button', title: filterNoIcon + ' Filter', width:1
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
    if(!settings['start_timeType']) return
    if(!settings['stop_timeType']) return
    if(settings['start_timeType'] == 'time') atomicState.scheduleStartTime = timeToday(settings['start_time']).getTime()
    if(settings['stop_timeType'] == 'time') atomicState.scheduleStartTime = timeToday(settings['stop_time']).getTime()
    if(settings['start_timeType'] == 'sunrise') atomicState.scheduleStartTime = (settings['start_sunType'] == 'before' ? parent.getSunrise(settings['start_sunOffset'] * -1,app.label) : parent.getSunrise(settings['start_sunOffset'],app.label))
    if(settings['stop_timeType'] == 'sunrise') atomicState.scheduleStopTime = (settings['stop_sunType'] == 'before' ? parent.getSunrise(settings['stop_sunOffset'] * -1,app.label) : parent.getSunrise(settings['stop_sunOffset'],app.label))
    if(settings['start_timeType'] == 'sunset') atomicState.scheduleStartTime = (settings['start_sunType'] == 'before' ? parent.getSunset(settings['start_sunOffset'] * -1,app.label) : parent.getSunset(settings['start_sunOffset'],app.label))
    if(settings['stop_timeType'] == 'sunset') atomicState.scheduleStopTime = (settings['stop_sunType'] == 'before' ? parent.getSunset(settings['stop_sunOffset'] * -1,app.label) : parent.getSunset(settings['stop_sunOffset'],app.label))
    
    if(settings['start_timeType'] != 'time' || settings['stop_timeType'] != 'time'){
        unschedule('setTime')
        timeMillis = now() + parent.CONSTDayInMilli()
        parent.scheduleChildEvent(timeMillis,'','setTime','',app.id)
        putLog(1355,'info','Scheduling update subrise/sunset start and/or stop time(s).')
    }
    return true
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

def displayExcludeDates(){
    fieldName = 'excludeDates'
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
