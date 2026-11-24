/***********************************************************************************************************************
*
*  Copyright (C) 2026 roguetech
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
*  Version: 0.4.2.20
* 
***********************************************************************************************************************/

// TO-DO: Allow actions per side (rather than flip90 or flip180)
// TO-DO: Allow selecting rooms??
// TO-DO: Add support for Hubitat Package Manager? https://community.hubitat.com/t/beta-hubitat-package-manager/38016
// added parent.storeCubeSide(singleDevice, buttonNumber, app.id)
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
    return 2
}

preferences {
    if(!settings) settings = [:]
    buttonMap = buildButtonMap()
    actionMap = buildActionMap('cube')
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
            controllerDeviceOptions = controllerOptionProcessParentDeviceList('pushableButton')
            if(allDeviceOptions && !controllerDeviceOptions) {
                section(){
                    displayError('You don\'t have any MagicCube devices selected in the Master app. Update the device selection in the Master app to include MagicCube(s).')
                    displayInfo('MagicCube devices are identified by having a "sideUp" setting. If you have a MagicCube installed, check the device status page under "Current States" (right side). If there is no "sideUp" value, and it\'s a new MagicCube, try flipping it (then refresh this page). Otherwise, check to make sure the driver is "Aqara Cube T1 Pro".') 
                }
            }
            
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
    if(!settings['controllerDevice']) return false
    if(!controlDevice) return false
    if(!checkAnyDeviceSet()) return false
    if(timeTypeStart == 'time' && !timeStart) return false
    if(timeTypeStop == 'time' && !timeStop) return false
    if((timeTypeStart == 'sunrise' || timeTypeStart == 'sunset') && !timeSunTypeStart) return false
    if((timeTypeStop == 'sunrise' || timeTypeStop == 'sunset') && !timeSunTypeStop) return false
    if((timeSunTypeStart == 'before' || timeSunTypeStart == 'after') && !timeSunOffsetStart) return false
    if((timeSunTypeStop == 'before' || timeSunTypeStop == 'after') && !timeSunOffsetStop) return false
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
    for(int buttonNumber = 1; buttonNumber < buttonMap.size() + 1; buttonNumber++){
        if(!checkIfShowButton(buttonNumber)) continue
        
        for(int actionMapItem = 0; actionMapItem < actionMap.size(); actionMapItem++){
        //actionMap.any{it->
            anyErrors = displayCustomizeActionsAndDevicesErrors(buttonNumber, fieldOptions, actionMap[actionMapItem].'action')
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
    if(!settings['controllerDevice']) return
    if(!settings['controlDevice']) return
    if(anyErrors) return
    fieldName = 'customActionsSetup'
    options = ['actions':'Assign action and devices to each button','actionsAndDevices':'Customize actions and devices for each button']
    if(settings['controlDevice'].size() == 1 && settings[fieldName] != 'actionsAndDevices') {
        app.updateSetting('customActionsSetup', [type: 'enum', value: 'actions'])
        return
    }
    displayCustomActionsOptionComplete(fieldName, options)
    displayCustomActionsOptionIncomplete(fieldName, options)
    devicesText = 'device'
    if(settings['controlDevice'].size() > 1) devicesText = 'devices'
    if(settings['customActionsSetup'] == 'actions' && settings['controlDevice'].size() == 1) displayInfo('This option allows setting what each button does.')
    if(settings['customActionsSetup'] == 'actions' && settings['controlDevice'].size() > 1) displayInfo('This option allows setting what each button does, then assign ' + devicesText + ' to it.')
    if(settings['customActionsSetup'] == 'actionsAndDevices') displayInfo('This option allows setting what each button does, and which devices for each button/action.')
}
def displayCustomActionsOptionComplete(fieldName, options){
    if(!settings[fieldName]) return
    fieldTitle = 'Selection type:'
    displaySelectField(fieldName, fieldTitle, options, false, true)
}
def displayCustomActionsOptionIncomplete(fieldName, options){
    if(settings[fieldName]) return
    fieldTitle = 'Select how to assign devices and buttons actions:'
    displaySelectField(fieldName, fieldTitle, options, false, true)
}

def displayDefineActions(){
    if(settings['customActionsSetup'] != 'actions') return
    if(!settings['controllerDevice']) return
    if(!settings['controlDevice']) return
    section(hideable: true, hidden: false, 'Button actions: ' + getDefineActionsSectionTitle()) {
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
        for(int buttonNumber = 1; buttonNumber < buttonMap.size() + 1; buttonNumber++){
            if(!checkIfShowButton(buttonNumber)) continue
            if(!settings['button_' + buttonNumber]) continue
            if(settings['button_' + buttonNumber] != 'setColor' && settings['button_' + buttonNumber] != 'cycleColor') {
                app.removeSetting('button_' + buttonNumber + '_color')
                app.removeSetting('button_' + buttonNumber + '_colorCount')
                continue
            }
            if(settings['button_' + buttonNumber] == 'setColor' && !settings['button_' + buttonNumber + '_color']) infoText = 'Use the color picker to select what color. Note that the brightness value will not be used, only hue and saturation. (Default red.)'
            if(settings['button_' + buttonNumber] == 'cycleColor' && !settings['button_' + buttonNumber + '_colorCount']) infoText = 'Enter the number of colors to cycle through (default is 6 for primary and secondary colors). Note that saturation will be 100%, and brightness based on defaults.'
            if(infoText){
                displayInfo(infoText)
                break
            }
        }
        for(int buttonNumber = 1; buttonNumber < buttonMap.size() + 1; buttonNumber++){
            if(!checkIfShowButton(buttonNumber)) continue
            displayDefineActionsButton(buttonNumber)
        }
        if(!settings['advancedSetup']) displayInfo('Select Advanced Setup for more options.')
    }
}
def getDefineActionsWarningValue(){
    returnValue = ''
    for(int buttonNumber = 1; buttonNumber < buttonMap.size() + 1; buttonNumber++){
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
    for(int buttonNumber = 1; buttonNumber < buttonMap.size() + 1; buttonNumber++){
        if(!checkIfShowButton(buttonNumber)) continue
        if(!settings['button_' + buttonNumber]) continue
        if(sectionTitle) sectionTitle += '\n'
        //sectionTitle += buttonMap[i]['fullName'] + ': ' + getActionFromButtonNumber(i)['descriptionActive'] + ''
        actionMap.find{it->
            if(it.'action' == settings['button_' + buttonNumber]) actionText = it.'descriptionActive'
        }
        sectionDevice = settings['button_' + buttonNumber]
        if(settings['controlDevice'].size() > 1) sectionDevice = settings['button_' + buttonNumber + '_' + settings['button_' + buttonNumber]]
        sectionTitle += buttonMap[buttonNumber - 1]['fullName'].capitalize() + ': ' + actionText.capitalize() + ' ' + sectionDevice
    }
    if(!sectionTitle) sectionTitle = 'Select device(s) for each/any action:' + expandText
    return sectionTitle
}
def displayDefineActionsButton(buttonNumber){
    fieldName = 'button_' + buttonNumber
    fieldTitle = buttonMap[buttonNumber - 1]['fullName'].capitalize() + ' action:'
    fieldOptions = setActionsPerButton(buttonNumber, settings[fieldName], true)
    width = 12
    if(settings[fieldName] && settings[fieldName] == 'setBrightness') width = 11
    if(settings[fieldName] && settings[fieldName] == 'setTemp') width = 11
    if(settings[fieldName] && settings[fieldName] == 'setColor') width = 11
    if(settings[fieldName] && settings[fieldName] == 'cycleColor') width = 11
    displaySelectField(fieldName, fieldTitle, fieldOptions, false, false, '', width)
    displaysetBrightnessOption(buttonNumber, 'setBrightness')
    displaySetTempOption(buttonNumber, 'setTemp')
    displaySetColorOption(buttonNumber, 'setColor')
    displayCycleColorOption(buttonNumber, 'cycleColor')
    displayDefineActionsDevice(buttonNumber, fieldOptions) 
}
def displayDefineActionsDevice(buttonNumber, fieldOptions){
    buttonAction = settings['button_' + buttonNumber]
    if(!buttonAction) return
    if(!settings['controlDevice']) return
    actionMap.find{it->
        if(it.'action' == buttonAction) fieldText = it.'description'
    }
    if(settings['controlDevice'].size() == 1) return
    fieldName = 'buttonId_' + buttonNumber + '_' + buttonAction
    fieldTitle = ' '
    if(!settings[fieldName]) fieldTitle = 'Device to ' + fieldText + ':'
    deviceOptions = [:]
    settings['controlDevice'].each{it->
        deviceOptions[it.'id'] = it.'label'
    }
    newVar = []

    displaySelectField(fieldName, fieldTitle, deviceOptions, true, true)
}

def displayCustomizeActionsAndDevices(){
    if(settings['customActionsSetup'] != 'actionsAndDevices') return
    if(!settings['controlDevice']) return
    if(!settings['controllerDevice']) return
    
    displayCustomizeActionsAndDevicesSections()
}

def displayCustomizeActionsAndDevicesSections() {
    def setErrors = ''

    for(int buttonNumber = 1; buttonNumber < buttonMap.size() + 1; buttonNumber++){
        sectionLabel = ''
        if(!checkIfShowButton(buttonNumber)) continue
        fieldOptions = setActionsPerButton(buttonNumber)
        hidden = true
        if(buttonNumber == 1) hidden = false		// Don't hide flip 90 if empty
        
        for(int actionMapItem = 0; actionMapItem < actionMap.size(); actionMapItem++){
            if(settings['button_' + buttonNumber + '_' + actionMap[actionMapItem].'action']) {
                if(sectionLabel) sectionLabel += '\n'
                sectionLabel += '  ' + actionMap[actionMapItem].'descriptionActive'
                if(actionMap[actionMapItem].'action' == 'setBrightness' && settings['button_' + buttonNumber + '_level']) sectionLabel += ' to ' + settings['button_' + buttonNumber + '_level'] + '%'
                if(actionMap[actionMapItem].'action' == 'setTemp' && settings['button_' + buttonNumber + '_temp']) sectionLabel += ' to ' + settings['button_' + buttonNumber + '_temp'] + 'K'
                sectionLabel += ' ' + settings['button_' + buttonNumber + '_' + actionMap[actionMapItem].'action']
                hidden = true
                
                setErrors = displayCustomizeActionsAndDevicesErrors(buttonNumber, fieldOptions, actionMap[actionMapItem].'action')
                if(setErrors) hidden = false
                
                if(actionMap[actionMapItem].'action' == 'setBrightness' && !settings['button_' + buttonNumber + '_level']) hidden = false
                if(actionMap[actionMapItem].'action' == 'setTemp' && !settings['button_' + buttonNumber + '_temp']) hidden = false
                if(actionMap[actionMapItem].'action' == 'setColor' && !settings['button_' + buttonNumber + '_color']) hidden = false
                if(actionMap[actionMapItem].'action' == 'cycleColor' && !settings['button_' + buttonNumber + '_colorCount']) hidden = false
            }
        }
        if(anyErrors && !setErrors) hidden = true
        if(sectionLabel) sectionLabel = '\n' + sectionLabel
        sectionLabel = buttonMap[buttonNumber - 1].'fullName'.capitalize() + expandText + sectionLabel
        section(hideable: true, hidden: hidden, sectionLabel) {
            displayCustomizeActionsAndDevicesButtons(buttonNumber, fieldOptions)
            if(!settings['advancedSetup']) displayInfo('Select Advanced Setup for more options.')
        }
    }
}
def displayCustomizeActionsAndDevicesButtons(buttonNumber, fieldOptions) {
    deviceOptions = [:]
    settings['controlDevice'].each{it->
        deviceOptions.put([it.'id', it.'label'])    // This confirmed works
    }
    deviceOptions = deviceOptions.sort()        // If deviceOptions customized per field (removing contradictory ones, or moving selected to top), move to within displayDefineActionsAndDeviceField
    fieldOptions.eachWithIndex{fieldAction, fieldOptionNumber ->
        actionMap.each{it->
            if(it.'action' == fieldAction.key){
                displayDefineActionsAndDeviceField(buttonNumber, it, deviceOptions)
                if(anyErrors) displayError(displayCustomizeActionsAndDevicesErrors(buttonNumber, fieldOptions, fieldAction.key))
            }
        }
    }
}

def displayDefineActionsAndDeviceField(buttonNumber, actionLine, deviceOptions){
    fieldName = 'buttonId_' + buttonNumber + '_' + actionLine.'action'
    fieldTitle = '<b>' + actionLine.'descriptionActive' + '</b>:'
    if(!settings[fieldName]) fieldTitle = actionLine.'description' + ' <font color="gray">(Select devices)</font>'
    
    width = 12
    if(settings[fieldName] && actionLine.'action' == 'setBrightness') width = 11
    if(settings[fieldName] && actionLine.'action' == 'setTemp') width = 11
    if(settings[fieldName] && actionLine.'action' == 'setColor') width = 11
    if(settings[fieldName] && actionLine.'action' == 'cycleColor') width = 11
    displaySelectField(fieldName, fieldTitle, deviceOptions, true, false, '', width)

    displaysetBrightnessOption(buttonNumber, actionLine.'action')
    displaySetTempOption(buttonNumber, actionLine.'action')
    displaySetColorOption(buttonNumber, actionLine.'action')
    displayCycleColorOption(buttonNumber, actionLine.'action')
}

def displayCustomizeActionsAndDevicesErrors(buttonNumber, fieldOptions, action){
    if(!fieldOptions) return
    firstActionNumber = fieldOptions.findIndexOf{it.key==action}
    if(firstActionNumber == 0) return
    errorMessage = ''
    firstDeviceName = 'button_' + buttonNumber + '_' + action
    if(!settings[firstDeviceName]) return
    fieldOptions.findAll{it->
        if(it.key == action) return
        secondDeviceName = 'button_' + buttonNumber + '_' + it.key
        if(checkDeviceConflict(settings[firstDeviceName], settings[secondDeviceName], action, it.key)) {
            if(errorMessage) errorMessage += '\n'
            errorMessage += 'Can\'t set the same device to both ' + action + ' and ' + it.key + ' with the same button.'
        }
    }
    return errorMessage
}

def displaysetBrightnessOption(buttonNumber, buttonAction){
    if(buttonAction != 'setBrightness') {
      //  app.removeSetting('button_' + buttonNumber]
        return
    }
    if(settings['button_' + buttonNumber + '_level'] == 0)  app.removeSetting('button_' + buttonNumber + '_level')
                                                                                                            
    fieldSet = false
    if(settings['button_' + buttonNumber] == buttonAction) fieldSet = true
    if(settings['button_' + buttonNumber + '_' + buttonAction]) fieldSet = true
    if(fieldSet) input name: 'button_' + buttonNumber + '_level', type: 'number', title:'', width: 1, submitOnChange:true
    
    infoText = 'Specify brightness level (percentage).'
    if(fieldSet) {
        if(!settings['button_' + buttonNumber + '_level']) displayInfo(infoText)
        if(settings['button_' + buttonNumber + '_level'] == 0) displayInfo(infoText)
    }
}

def displaySetTempOption(buttonNumber, buttonAction){
    if(buttonAction != 'setTemp') {
      //  app.removeSetting('button_' + buttonNumber]
        return
    }
    if(settings['button_' + buttonNumber + '_temp'] == 0) app.removeSetting('button_' + buttonNumber + '_temp')
                                                                                                            
    fieldSet = false
    if(settings['button_' + buttonNumber] == buttonAction) fieldSet = true
    if(settings['button_' + buttonNumber + '_' + buttonAction]) fieldSet = true
    if(fieldSet) input name: 'button_' + buttonNumber  + '_temp', type: 'number', title:'', width: 1, submitOnChange:true
    
    infoText = 'Specify color tempertaure level (1800 to 6500, lower is more yellow).'
    if(fieldSet) {
        if(!settings['button_' + buttonNumber + '_temp']) displayInfo(infoText)
        if(settings['button_' + buttonNumber + '_temp'] == 0) displayInfo(infoText)
    }
}

def displaySetColorOption(buttonNumber, buttonAction){
    if(buttonAction != 'setColor') {
      //  app.removeSetting('button_' + buttonNumber]
        return
    }
    if(settings['button_' + buttonNumber + '_color'] == '#000000')  app.removeSetting('button_' + buttonNumber + '_color')
                                                                                                            
    fieldSet = false
    if(settings['button_' + buttonNumber] == buttonAction) fieldSet = true
    if(settings['button_' + buttonNumber + '_' + buttonAction]) fieldSet = true
    if(fieldSet) input name: 'button_' + buttonNumber + '_color', type: 'color', title:'', width: 1, submitOnChange:true
    
    infoText = 'Use the color picker. If you want a light to be set to a specific color whenever turned on, using scheduler is preferred. Note: If device is already set to color, it will toggle back to white.'
    if(fieldSet) {
        if(!settings['button_' + buttonNumber + '_color']) displayInfo(infoText)
        if(settings['button_' + buttonNumber + '_color'] == '#000000') displayInfo(infoText)
    }
}
def displayCycleColorOption(buttonNumber, buttonAction){
    if(buttonAction != 'cycleColor') return
    fieldSet = false
    if(settings['button_' + buttonNumber] == buttonAction) fieldSet = true
    if(settings['button_' + buttonNumber + '_' + buttonAction]) fieldSet = true
    if(fieldSet) input name: 'button_' + buttonNumber + '_colorCount', type: 'number', title:'', width: 1, submitOnChange:true
    infoText = 'Specify the number of colors (ie 3 for primary, 6 for secondary, 12 for tertiary, etc.).'
    if(fieldSet) {
        if(!settings['button_' + buttonNumber + '_colorCount']) displayInfo(infoText)
        if(settings['button_' + buttonNumber + '_colorCount'] == 0) displayInfo(infoText)
    }
}
def displayDimmingProgressionOption(){
    if(!settings['advancedSetup']) return
    if(!checkAnyDeviceSet()) return
    if(anyErrors) return
    dimmingSet = false
    if(customActionsSetup == 'actions' || customActionsSetup == 'actionsAndDevices'){
        for(int buttonNumber = 1; buttonNumber < buttonMap.size() + 1; buttonNumber++){
// Replace with actionMap type = 'dim'
            if(settings['button_' + buttonNumber] == 'brighten' && settings['button_' + buttonNumber + '_brighten']) dimmingSet = true
            if(settings['button_' + buttonNumber] == 'dim' && settings['button_' + buttonNumber + '_dim']) dimmingSet = true
        }
    }
    if(!dimmingSet) return
    sectionTitle = 'Click to set dimming steps (Optional)'
    if(settings['dimmingProgressionSteps']) sectionTitle = 'Dimming steps: ' + settings['dimmingProgressionSteps']
    if(!sectionTitle) sectionTitle = '<b>Set dimming steps:</b>'
    
    section(hideable: true, hidden: true, sectionTitle + expandText) {
        infoTip = 'Number of steps it takes to brighten (or dim) from 1 to 100%.'
        if(!settings['dimmingProgressionSteps']) infoTip = 'This is the number of steps it takes to brighten (or dim) from 1 to 100% brightness. It uses a geometric progression. For instance, with 10 steps, pressing the brighten button would go from 1% to 2%, and then to 4, 7, 11, 17, 25, 36, 52, 74, and finally 100%.'
        if(settings['dimmingProgressionSteps'] > 30) displayError('Maximum dimming steps is 30.')
        if(settings['dimmingProgressionSteps'] && settings['dimmingProgressionSteps'] < 1) displayError('Must be at least 1.')
        displayInfo(infoTip)
        fieldName = 'dimmingProgressionSteps'
        displayDimmingProgressionOptionComplete(fieldName)
        displayDimmingProgressionOptionIncomplete(fieldName)
    }
}
def displayDimmingProgressionOptionComplete(fieldName){
    if(!settings[fieldName]) return
    
    dimmingSet = false
    for(int buttonNumber = 1; buttonNumber < buttonMap.size() + 1; buttonNumber++){
// Replace with actionMap type = 'dim'
        if(settings['button_' + buttonNumber] == 'brighten' && settings['button_' + buttonNumber + '_brighten']) dimmingSet = true
        if(settings['button_' + buttonNumber] == 'dim' && settings['button_' + buttonNumber + '_dim']) dimmingSet = true
    }
    if(!dimmingSet) return
    fieldTitle = 'Dimming steps:'

    displayTextField(fieldName, fieldTitle, 'number', false)
}
def displayDimmingProgressionOptionIncomplete(fieldName){
    if(settings[fieldName]) return
    dimmingSet = false
        for(int buttonNumber = 1; buttonNumber < buttonMap.size() + 1; buttonNumber++){
// Replace with actionMap type = 'dim'
            if(settings['button_' + buttonNumber] == 'brighten' && settings['button_' + buttonNumber + '_brighten']) dimmingSet = true
            if(settings['button_' + buttonNumber] == 'dim' && settings['button_' + buttonNumber + '_dim']) dimmingSet = true
        }
    if(!dimmingSet) return
    fieldTitle = 'Enter dimming steps (optional, default ' + parent.CONSTPushDimmingTotalSteps() + '):'
    displayTextField(fieldName, fieldTitle, 'number', false)
}

def displayScheduleSection(){
    if(!settings['controllerDevice']) return
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup']) return
    if(!checkAnyDeviceSet()) return
    if(anyErrors) return
    
    section(){}
    
    hidden = true
    if(settings['timeTypeStart'] && !settings['timeTypeStop']) hidden = false
    if(settings['timeStart'] && settings['timeStart'] == settings['timeStop']) hidden = false
    if(!validateTimes('start')) hidden = false
    if(!validateTimes('stop')) hidden = false

    section(hideable: true, hidden: hidden, getTimeSectionTitle()){
        if(!settings['timeTypeStart'] && validateTimes('start') && validateTimes('stop') && !settings['days']  && !settings['includeDates'] && !settings['excludeDates']) displayInfo('This will limit when this ' + thisDescription + ' is active. You can create another ' + thisDescription + ' "app" to do something else for opposite times/days.')
        if(settings['timeStart'] && settings['timeStart'] == settings['timeStop']) displayError('You can\'t have the same time to start and stop.')

        displayTimeTypeOption('start')
        displayTimeOption('start')
        displaySunriseTypeOption('start')
        displayTimeTypeOption('stop')
        displayTimeOption('stop')
        displaySunriseTypeOption('stop')
        displayDaysOption()
        displayDatesOptions(thisType)
    }
}

// Returns true if showing button (ie 2 button has not Middle button)
def checkIfShowButton(number){
    if(buttonMap[number - 1].'advanced' && !settings['advancedSetup']) return false
    if(!parent.checkIsDimmableMulti(settings['controlDevice']) && buttonMap[number].'type' == 'dim') return false
    return true
}

def checkDefineActionButNoDevice(){
    for(int buttonNumber = 1; buttonNumber < buttonMap.size() + 1; buttonNumber++){
        if(settings['button_' + buttonNumber]){
            if(!settings['button_' + buttonNumber + '_' + settings['button_' + buttonNumber]]) return settings['button_' + buttonNumber]
        }
    }
    return false
}
                                     
def checkDeviceConflict(firstDeviceMulti, secondDeviceMulti, firstAction, secondAction){
    if(!firstDeviceMulti) return
    if(!secondDeviceMulti) return
    // This should use action.type, rather than defining conflicts
    if(firstAction == 'on' && !['off', 'toggle'].contains(secondAction)) return
    if(firstAction == 'off' && !['on', 'resume', 'setBrightness', 'setTemp', 'setColor', 'cycleColor'].contains(secondAction)) return
    if(firstAction == 'toggle' && !['off', 'on'].contains(secondAction)) return
    if(firstAction == 'dim' && !['brighten', 'off', 'resume', 'setBrightness'].contains(secondAction)) return
    if(firstAction == 'brighten' && !['dim', 'off', 'resume', 'setBrightness'].contains(secondAction)) return
    if(firstAction == 'resume' && !['dim', 'brighten', 'off'].contains(secondAction)) return
    if(firstAction == 'setBrightness' && !['off', 'dim', 'brighten'].contains(secondAction)) return
    if(firstAction == 'setTemp' && !['off', 'setColor', 'cycleColor'].contains(secondAction)) return
    if(firstAction == 'setColor' && !['off', 'setTemp', 'cycleColor'].contains(secondAction)) return
    if(firstAction == 'cycleColor' && !['off', 'setTemp', 'setColor'].contains(secondAction)) return
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

def compareDeviceLists(multiDevice1, multiDevice2){
    multiDevice1?.any{first->
        multiDevice2?.any{second->
            if(first.id == second.id) return true
        }
    }
}

// Returns an ordered map of actions per button
def setActionsPerButton(buttonNumber, fieldValue = false, unique = false){
    fieldOptions = [:]
    fieldOptions = setActionsPerButtonPreset(fieldOptions, fieldValue)
    //fieldOptions = setActionsPerButtonDefault(fieldOptions, fieldValue, buttonNumber)
    fieldOptions = setActionsPerButtonType(fieldOptions, fieldValue, buttonNumber)
    fieldOptions = setActionsPerButtonOther(fieldOptions, fieldValue, buttonNumber)
    
    if(!settings['advancedSetup']) {        // Remove any already picked
        for(int i = 1; i < buttonMap.size() + 1; i++){
            if(i != buttonNumber){
                actionMap.each{it->
                    if(settings['button_' + i] == it.'action') fieldOptions.remove(it.'action')
                }
            }
        }
    }
    return fieldOptions
}
// Return actionMap line of what it's set to
def setActionsPerButtonPreset(fieldOptions, fieldValue){
    if(!fieldValue) return
    actionMap.find{it->      // 1) What fieldValue is
        if(it.'action' == fieldValue) {
            fieldOptions[it.'action'] = it.'actionText'.capitalize()
        }
    }
    return fieldOptions
}

def setActionsPerButtonType(fieldOptions, fieldValue, buttonNumber){
    if(!fieldOptions) fieldOptions = [:]
    actionMap.findAll{it->      // 3) Correct "type"
        if(it.'type' == buttonMap[buttonNumber - 1].'type') {
            if(setActionsPerButtonProcess(fieldOptions, it, fieldValue)) fieldOptions[it.action] = it.'actionText'.capitalize()
        }
    }
    return fieldOptions
}
def setActionsPerButtonOther(fieldOptions, fieldValue, buttonNumber){
    actionMap.findAll{it->      // 4) The rest
        if(it.'type' != buttonMap[buttonNumber - 1]['type']) {
            if(setActionsPerButtonProcess(fieldOptions, it, fieldValue)) fieldOptions[it.action] = it.'actionText'.capitalize()
        }
    }
    return fieldOptions
}
def setActionsPerButtonProcess(fieldOptions, actionMapLine, fieldValue){
    if(!actionMapLine) return
    if(actionMapLine?.'advanced' && !advancedSetup) return
    if(actionMapLine.'actionText' == fieldValue) return

    return true
}

def checkAnyDeviceSet(){
    for(int buttonNumber = 1; buttonNumber < buttonMap.size() + 1; buttonNumber++){
        if(!checkIfShowButton(buttonNumber)) continue
        for(int actionNumber = 0; actionNumber < actionMap.size(); actionNumber++){
            if(settings['button_' + buttonNumber] == actionMap[actionNumber].'action' && settings['controlDevice'].size() == 1) return true
            if(settings['button_' + buttonNumber + '_' + actionMap[actionNumber].'action']) return true
        }
    }
    return false
}

def resetControllerDevices(deviceName){
    if(!deviceName) return
    app.removeSetting(deviceName)
    setDeviceById(deviceName + 'Id', deviceName, 'pushableButton')
}
def setDeviceById(deviceIdName, deviceName, capability){
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
        for(int buttonNumber = 1; buttonNumber < buttonMap.size() + 1; buttonNumber++){
            for(int actionMapItem = 0; actionMapItem < actionMap.size(); actionMapItem++){
                app.removeSetting('button_' + buttonNumber + '_' + actionMap[actionMapItem].'action')
                if(!settings['controlDevice']) continue
                if(!checkIfShowButton(buttonNumber)) continue
                if(settings['button_' + buttonNumber] == actionMap[actionMapItem].'action' && settings['controlDevice'].size() == 1){
                    app.updateSetting('button_' + buttonNumber + '_' + actionMap[actionMapItem].'action', [type: "capability.switch", value: settings['controlDevice']])
                }
                if(settings['button_' + buttonNumber] != actionMap[actionMapItem].'action') app.removeSetting('buttonId_' + buttonNumber + '_' + actionMap[actionMapItem].'action')
                setDeviceById('buttonId_' + buttonNumber + '_' + actionMap[actionMapItem].'action', 'button_' + buttonNumber + '_' + actionMap[actionMapItem].'action','switch')
            }
        }
}
def buildButtonMap(){
    return [['fullName':'flip 90°', 'shortName':'flip90', 'type':'on', 'eventType':'push', 'advanced':false],
            ['fullName':'flip 180°', 'shortName':'flip180', 'type':'on', 'eventType':'push', 'advanced':false],
            ['fullName':'rotate clockwise', 'shortName':'clockwise', 'type':'dim', 'eventType':'released', 'advanced':false],
            ['fullName':'rotate counter-clockwise', 'shortName':'counterClockwise', 'type':'dim', 'eventType':'held', 'advanced':false],
            ['fullName':'shake', 'shortName':'shake', 'type':'on', 'eventType':'doubleTapped', 'advanced':true]]
}

def getActionFromButtonNumber(buttonNumber){
     actionMap.find{it->
           if(it.'defaultButton' == buttonNumber) returnValue = it
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
    putLog(710, 'trace', 'Installed')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(), app.getName()))
    initialize()
}

def updated() {
    putLog(716, 'trace', 'Updated')
    unsubscribe()
    initialize()
}

def initialize() {
    putLog(722, 'trace', '^ initialize ^')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(), app.getName()))

    subscribe(controllerDevice, "pushed.1", buttonEvent)
    subscribe(controllerDevice, "pushed.2", buttonEvent)
    subscribe(controllerDevice, "pushed.3", buttonEvent)
    subscribe(controllerDevice, "pushed.4", buttonEvent)
    subscribe(controllerDevice, "pushed.5", buttonEvent)
    subscribe(controllerDevice, "pushed.6", buttonEvent)
    
    //doubleTapped, release and hold used by kkossev's driver
    subscribe(controllerDevice, "doubleTapped", buttonEvent)
    subscribe(controllerDevice, "released", buttonEvent)
    subscribe(controllerDevice, "held", buttonEvent)
        
    dimValue = parent.CONSTPushDimmingTotalSteps()
    if(settings['dimmingProgressionSteps']) dimValue = settings['dimmingProgressionSteps']
    atomicState.dimmingProgressionFactor = parent.computeOptiomalGeometricProgressionFactor(dimValue, app.id)
    putLog(740, 'info', 'Brightening/dimming factor: ' + atomicState.dimmingProgressionFactor + '.')

    setScheduleProcessDates()
    
    processDates()
    setTime()

    putLog(747, 'trace', '¬ initialize ¬')
}

def buttonEvent(evt){
    putLog(751, 'info', '^')

    buttonNumber = convertDriver(evt, parent.getCubeSide(evt.device.id, app.id))
    buttonNumber.each{         // create false loop, from which to exit with "return", but still perform storeCubeSide; is there a less stupid way to do it?

    if(buttonNumber == false) return
    
    // If not correct day, return nulls
    if(!checkIncludeDates()) return
    // if not between start and stop time, return nulls
    if(!parent.checkNowBetweenTimes(atomicState.timestampStart, atomicState.timestampStop, app.id)) return
    if(!getEnvironmentActive()) return

    if(!actionMap) actionMap = buildActionMap('cube')

    actionMap.each{switchAction ->
        device = settings['button_' + buttonNumber + '_' + switchAction.'action']
        if(!device) return
  
        device.each{singleDevice->
            def tempMap
            def brightness
            def stateValue
            brightness = parent._getNextLevelDimmable(singleDevice, switchAction.'action', atomicState.dimmingProgressionFactor, app.id)
            if(switchAction.'action' == 'setBrightness')  brightness = settings['button_' + buttonNumber + '_level']
            brightnessMap = parent.getNonScheduleLevelMap('brightness', brightness, app.id)         // dim, brighten
            if(switchAction.'action' == 'setTemp')  tempMap = ['temp':settings['button_' + buttonNumber + '_temp']]
            setColorMap = getColorMap(singleDevice, switchAction.'action', buttonNumber)
            setCycleMap = getCycleColorMap(singleDevice, switchAction.'action', buttonNumber)
            fullMap = parent.combineMaps(brightnessMap, tempMap, setColorMap, setCycleMap, '', app.id)
            stateValue =  performToggle(singleDevice, switchAction.'action')
            if(brightnessMap) stateValue = 'on'

            if(fullMap) putLog(784, 'trace', '[' + singleDevice + '] settings ' + fullMap)
            parent.mergeMapToTable(singleDevice.id, 'state', stateValue, app.id)
            parent.mergeMapToTable(singleDevice.id, 'nonSchedule', fullMap, app.id)
        }
        if(switchAction.'action' == 'resume') parent.resumeDeviceScheduleMulti(device, app.id)       //??? this function needs to be rewritten, I think

        parent.setDeviceMulti(device, 'cube', app.id)
    }
    }
    putLog(793, 'info', '¬')
    storeCubeSide(evt.device, evt.value.toInteger())
}

def storeCubeSide(singleDevice, priorSide){
    parent.storeCubeSide(singleDevice.id, priorSide, app.id)
}

// Sets atomicState.buttonNumber to string of action
// Currently converts from kkossev's T1 driver to match veeceeoh's Aqara Mi driver
// Is there a way to determine which driver is used?
// (0) 90flip
// (1) 180flip
// (2) clockwise
// (3) counterclockwise
// (4) shake
def convertDriver(evt, priorSide){
   // cubeActions = ['shake', 'flip90', 'flip180', 'slide', 'knock', 'clockwise', 'counterClockwise'] // Need to put this in the UI, should be state variable
    if(!priorSide) {
        putLog(812, 'warn', 'Prior button not known. If this is not the first run of the app, this indicates a problem.')
    	return false
    }
    
    eventValue = evt.value.toInteger()
    if(evt.name == 'pushed'){
        if(eventValue == 1 && priorSide == 6) return 2
        if(eventValue == 2 && priorSide == 5) return 2
        if(eventValue == 3 && priorSide == 4) return 2
        if(eventValue == 4 && priorSide == 3) return 2
        if(eventValue == 5 && priorSide == 2) return 2
        if(eventValue == 6 && priorSide == 1) return 2
        if(eventValue != priorSide) return 1
    }
    buttonMap = buildButtonMap()
    for(int buttonNumber = 1; buttonNumber < buttonMap.size() + 1; buttonNumber++){
        if(evt.name == buttonMap[buttonNumber - 1].'eventType') return buttonNumber
    }
    return false
}

def getColorMap(singleDevice, switchAction, buttonNumber){
    if(switchAction != 'setColor') return
    hueValue = getHueFromHex(singleDevice, buttonNumber)
    satValue = getSatFromHex(singleDevice, buttonNumber)
    if(!hueValue && !satValue) return
    def hueMap = parent.getNonScheduleLevelMap('hue', hueValue, app.id)
    def satMap = parent.getNonScheduleLevelMap('sat', satValue, app.id)
    parent.mergeMapToTable(singleDevice.id, 'state', 'on', app.id)
    return parent.combineMaps(hueMap, satMap, '', '', '', app.id)
}
def getHueFromHex(singleDevice, buttonNumber){
    if(!parent.checkIsColor(singleDevice, app.id)) return
    colorHex = settings['button_' + buttonNumber + '_color']
    if(!colorHex) colorHex = '#FF0000'
    colorRgb = hubitat.helper.ColorUtils.hexToRGB(colorHex)
    colorHsv = hubitat.helper.ColorUtils.rgbToHSV(colorRgb)
    def returnValue = Math.round(colorHsv[0] * 3.6)
    if(returnValue == 0) returnValue = 1
    return returnValue
}
def getSatFromHex(singleDevice, buttonNumber){
    if(!parent.checkIsColor(singleDevice, app.id)) return
    colorHex = settings['button_' + buttonNumber + '_color']
    if(!colorHex) colorHex = '#FF0000'
    colorRgb = hubitat.helper.ColorUtils.hexToRGB(colorHex)
    colorHsv = hubitat.helper.ColorUtils.rgbToHSV(colorRgb)
    return Math.round(colorHsv[1])
}

// This is fucked up - returning map, and merging maps
// SHould just merge it here?
def getCycleColorMap(singleDevice, switchAction, buttonNumber){
    if(switchAction != 'cycleColor') return
    nextColor = getCycleColorValue(singleDevice, buttonNumber)
    if(!nextColor) {
        parent.clearTableByDeviceAndType(singleDevice.id, 'nonSchedule', 'hue', app.id)
        parent.clearTableByDeviceAndType(singleDevice.id, 'nonSchedule', 'sat', app.id)
        return
    }
    def hueMap = parent.getNonScheduleLevelMap('hue', nextColor, app.id)
    def satMap = parent.getNonScheduleLevelMap('sat',100, app.id)
    parent.mergeMapToTable(singleDevice.id, 'state', 'on', app.id)
    return parent.combineMaps(hueMap, satMap, '', '', '', app.id)
}
def getCycleColorValue(singleDevice, buttonNumber){
    if(!singleDevice) return
    if(!parent.checkIsColor(singleDevice, app.id)) return
    colorCount = settings['button_' + buttonNumber + '_colorCount']
    if(!colorCount) colorCount = 6
    firstColor = Math.round(360 / (colorCount * 2))
    lastColor = 360 - Math.round(360 / (colorCount * 2))
    currentColor = parent.getCurrentLevelFromTable(singleDevice.id, 'hue', app.id)
    //if(!hiRezHue) currentColor = Math.round(currentColor * 3.6)
    if(!parent.checkIsOn(singleDevice, app.id)) return firstColor
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

// Return true if disabled
def getEnvironmentActive(){
    if(settings['ifMode'] && !settings['ifMode'].contains(location.mode)) return
    
    if(atomicState.timestampStart && atomicState.timestampStop){
        if(!parent.checkNowBetweenTimes(atomicState.timestampStart, atomicState.timestampStop, app.id)) return
    }

    if(settings['personHome']){
        if(!parent.checkAnyPeopleHome(settings['personHome'], app.id)) return
    }
    if(settings['personNotHome']){
        if(!parent.checkAllPeopleNotHome(settings['personNotHome'], app.id)) return
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
def buildActionMap(thisType){
    if(thisType == 'schedule') return
    if(thisType == 'sensor') return

    return [['action':'on', 'actionText':'turn on', 'descriptionActive':'Turns on', 'description': 'Turn on', 'type':'on', 'defaultButton':1, 'advanced':false],
        ['action':'off', 'actionText':'turn off', 'descriptionActive':'Turns off', 'description': 'Turn off', 'type':'on', 'defaultButton':5, 'advanced':false],
        ['action':'brighten', 'actionText':'brighten', 'descriptionActive':'Brightens', 'description': 'Brighten', 'type':'dim', 'defaultButton':2, 'advanced':false],
        ['action':'dim', 'actionText':'dim', 'descriptionActive':'Dims', 'description': 'Dim', 'type':'dim', 'defaultButton':4, 'advanced':false],
        ['action':'toggle', 'actionText':'toggle', 'descriptionActive':'Toggles', 'description': 'Toggle', 'type':'other', 'defaultButton':3, 'advanced':false],
        ['action':'resume', 'actionText':'resume schedule', 'descriptionActive':'Resumes schedule (if none, turn off)', 'description': 'Resume schedule (if none, turn off)', 'type':'other', 'advanced':true],
        ['action':'setBrightness', 'actionText':'set brightness', 'descriptionActive':'Sets brightness', 'description': 'Set brightness', 'type':'other', 'advanced':true],
        ['action':'setTemp', 'actionText':'set color temperature', 'descriptionActive':'Sets color temperature', 'description': 'Set color temperature', 'type':'other', 'advanced':true],
        ['action':'setColor', 'actionText':'set color', 'descriptionActive':'Sets color', 'description': 'Set color', 'type':'other', 'advanced':true],
        ['action':'cycleColor', 'actionText':'cycle through colors', 'descriptionActive':'Cycles through colors', 'description': 'Cycle through colors', 'type':'other', 'advanced':true]]
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
    paragraph('<div style="background-color:#DCDCDC"><b>' + text + '</b></div>', width:width)
}


def displayInfo(text, noDisplayIcon = null, width=12){
    if(!text) return
    //tooltipName = 'tooltip' + tooltipNumber
    //tooltipComputedValueName = 'tooltipValue' + tooltipNumber
    //paragraph('<div onclick="var ' + tooltipName + ' = document.getElementById(\'' + tooltipName + '\'); var ' + tooltipComputedValueName + ' = window.getComputedStyle(' + tooltipName + '); if (' + tooltipComputedValueName + '.display === \'block\') { ' + tooltipName + '.style.display = \'none\'; } else { ' + tooltipName + '.style.display = \'block\'; ' + tooltipName + '.innerText = \'' + text + '\'; }">' + infoIcon + '</div><div id="' + tooltipName + '" style="display: none; background-color:AliceBlue"></div>', width:width)

    //tooltipNumber++
    if(noDisplayIcon) paragraph('<div style="background-color:AliceBlue">' + text + '</div>', width:width)
    if(!noDisplayIcon) paragraph('<div style="background-color:AliceBlue">' + infoIcon + ' ' + text + '</div>', width:width)
    text = ''
}

def displayError(text, noDisplayIcon = null, width=12){
    if(!text) return
    if(noDisplayIcon) paragraph('<div style="background-color:Bisque">' + text + '</div>', width:width)
    if(!noDisplayIcon) paragraph('<div style="background-color:Bisque">' + errorIcon  + ' ' + text + '</div>', width:width)
    errorMessage = ''
}

def displayWarning(text, noDisplayIcon = null, width=12){
    if(!text) return
    if(noDisplayIcon) paragraph('<div style="background-color:LemonChiffon">' + text + '</div>', width:width)
    if(!noDisplayIcon) paragraph('<div style="background-color:LemonChiffon">' + warningIcon  + ' ' + text + '</div>', width:width)
    warningMessage = ''
}

def highlightText(text, width=12){
    if(!text) return
    return '<div style="background-color:Wheat">' + text + '</div>'
}

def addFieldName(text, fieldName){
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
    label title: '', required: false, width: 10, submitOnChange:true
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
    
    displayControllerOptionComplete(fieldName, fieldOptions)
    displayControllerOptionIncomplete(fieldName, fieldOptions)
}
def displayControllerOptionComplete(fieldName, fieldOptions){
    if(!settings[fieldName]) return
    if(thisType == 'cube') fieldTitle = 'MagicCube'
    if(thisType == 'pico') fieldTitle = 'Pico controller'
    if(thisType == 'sensor') fieldTitle = 'Sensor'
    if(settings[fieldName].size() > 1) fieldTitle += 's:'
    capability = 'capability.pushableButton'
    if(thisType == 'sensor') capability = 'capability.' + sensorMapEntry.'capability'
    if(fieldOptions) displaySelectField(fieldName, fieldTitle, fieldOptions, true, true, 'controllerButton')
    if(!fieldOptions) displayDeviceSelectField(fieldName, fieldTitle, capability, true, 'controllerButton')
}
def displayControllerOptionIncomplete(fieldName, fieldOptions){
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
            if(controllerMatch) controllerList.put([singleDevice.'id', getDeviceName(singleDevice)])    // This confirmed works
            //if(!controllerMatch) fullList.put([singleDevice.'id', getDeviceName(singleDevice)])
        }
    }
    if(settings['controllerDeviceId']){
        settings['controllerDeviceId'].each{controllerDeviceIdValue->
            if(!controllerList.containsKey(controllerDeviceIdValue)){
                controllerList.put([controllerDeviceIdValue, 'test'])
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
        if(!settings['actionStart']) return
        if(!settings['actionStop']) return
    }
    
    displayAdvancedOptionEnabled(fieldName)
    displayAdvancedOptionDisabled(fieldName)
}
def displayAdvancedOptionEnabled(fieldName){
    if(!settings[fieldName]) return
    def fieldTitleTrue = 'Showing advanced setup.'
    displayBoolField(fieldName, fieldTitleTrue, fieldTitleFalse, false)
}
def displayAdvancedOptionDisabled(fieldName){
    if(settings[fieldName]) return
    def fieldTitleTrue = 'Keeping it simple.'
    def fieldTitleFalse = 'Click to show advanced setup.'
    displayBoolField(fieldName, fieldTitleTrue, fieldTitleFalse, false)
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

    displayDeviceSelectField(fieldName, fieldTitle, 'capability.' + capabilitiesType, true, 'controlButton')
}
def displayControlDeviceOptionIncomplete(fieldName){
    if(settings[fieldName]) return
    fieldTitle = 'Select all device(s) to control with the ' + thisDescription + ':'
    if(thisType == 'sensor' && settings['controllerDevice'].size() == 1) fieldTitle = 'Select all device(s) to control with the sensor:'
    if(thisType == 'sensor' && settings['controllerDevice'].size() > 1) fieldTitle = 'Select all device(s) to control with the sensors:'
    capabilitiesType = 'switch'
    if(state['controlButtonValue'] == 1) capabilitiesType = 'switchLevel'
    if(state['controlButtonValue'] == 2) capabilitiesType = 'colorMode'

    displayDeviceSelectField(fieldName, fieldTitle, 'capability.' + capabilitiesType, true, 'controlButton')
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
    displayModeSelectField(fieldName, fieldTitle, true, false)
}
def displayIfModeFieldIncomplete(fieldName){
    if(settings[fieldName]) return
    displayInfo('This will limit the ' + thisDescription + ' from being active to only when Hubitat\'s Mode is as selected. You can create another ' + thisDescription + ' "app" to do something else for other Modes.')
    fieldTitle = 'Only when the Mode is:'
    displayModeSelectField(fieldName, fieldTitle, true, false)
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
    peopleError = compareDeviceLists(personHome, personNotHome)
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
    displayDeviceSelectField(fieldName, fieldTitle, fieldCapability, true)
}
def displayPersonHomeIncomplete(fieldName, fieldCapability){
    if(settings[fieldName]) return
    if(!settings['personNotHome']) displayInfo('This will limit the ' + thisDescription + ' from being active to only when those selected are home and/or away. They can be combined (as if Person A is home AND Person B is away). You can create another ' + thisDescription + ' "app" to do something else for the opposite.')
    fieldTitle = 'Only if any of these people are home (Optional)'
    displayDeviceSelectField(fieldName, fieldTitle, fieldCapability, true)
}
def displayPersonNotHomeComplete(fieldName, fieldCapability){
    if(!settings[fieldName]) return
    fieldTitle = 'Only if away:'
    displayDeviceSelectField(fieldName, fieldTitle, fieldCapability, true)
}
def displayPersonNotHomeIncomplete(fieldName, fieldCapability){
    if(settings[fieldName]) return
    fieldTitle = 'Only if all of these people are not home (Optional)'
    displayDeviceSelectField(fieldName, fieldTitle, fieldCapability, true)
}

def validateTimes(type){
    if(type == 'stop' && settings['timeTypeStart'] && !settings['timeTypeStop']) return false
    if(thisType == 'schedule' && type == 'stop' && settings['timeTypeStop'] == 'none') return true
    if(settings['timeType' + type.capitalize()] == 'time' && !settings['time' + type.capitalize()]) return false
    if(settings['timeType' + type.capitalize()] == 'sunrise' && !settings['timeSunType' + type.capitalize()]) return false
    if(settings['timeType' + type.capitalize()] == 'sunset' && !settings['timeSunType' + type.capitalize()]) return false
    if(settings['timeSunType' + type.capitalize()] == 'before' && !settings['timeSunOffset' + type.capitalize()]) return false
    if(settings['timeSunType' + type.capitalize()] == 'after' && !settings['timeSunOffset' + type.capitalize()]) return false
    if(!validateSunriseMinutes(type)) return false
    return true
}

def validateSunriseMinutes(type){
    if(!settings['timeSunOffset' + type.capitalize()]) return true
    // If sunrise/set offset is negative, make positive and invert before/after
    if(settings['timeSunOffset' + type.capitalize()] < 0){
        if(settings['timeSunType' + type.capitalize()] == 'before') {
            app.updateSetting('timeSunType' + type.capitalize(), [type:'enum', value:'after'])
        } else if(settings['timeSunType' + type.capitalize()] == 'after'){
            app.updateSetting('timeSunType' + type.capitalize(), [type:'enum', value:'before'])
        }
        app.updateSetting('timeSunOffset' + type.capitalize(), [type:'number', value:settings['timeSunOffset' + type.capitalize()] * -1])
    }
    if(settings['timeSunOffset' + type.capitalize()] > 719) return false
    return true
}

def getTimeSectionTitle(){
    sectionTitle = ''
    if(!settings['timeTypeStart'] && !settings['timeTypeStop']) return 'Click to set with schedule (Optional)'
    if(settings['timeTypeStart']) sectionTitle = '<b>Starting: ' + getTimeSectionStartStopTitle('start') + '</b>'
    if(settings['timeTypeStart'] && settings['timeTypeStop']) sectionTitle += '\n'
    if(settings['timeTypeStop']) sectionTitle += '<b>Stopping: ' + getTimeSectionStartStopTitle('stop') + '</b>'
    if(thisType == 'schedule' && settings['timeStart'] && settings['timeStop'] && settings['timeStart'] == settings['timeStop']) sectionTitle = '<b>Always</b>'
    return sectionTitle
}
def getTimeSectionStartStopTitle(type){
    sectionTitle = ''
    if(type == 'stop' && settings['timeType' + type.capitalize()] == 'none') return 'No end'
    if(settings['timeType' + type.capitalize()] == 'time') sectionTitle += 'At '
    if(settings['timeType' + type.capitalize()] == 'time' && !settings['time' + type.capitalize()]) sectionTitle += 'specific time'
    if(settings['timeType' + type.capitalize()] == 'time' && settings['time' + type.capitalize()]) sectionTitle += Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings['time' + type.capitalize()]).format('h:mm a', location.timeZone)
    if(settings['timeType' + type.capitalize()] == 'sunrise' || settings['timeType' + type.capitalize()] == 'sunset'){
        if(!settings['timeSunType' + type.capitalize()]) sectionTitle += 'Based on ' + settings['timeType' + type.capitalize()]
        if(settings['timeSunType' + type.capitalize()] == 'at') sectionTitle += 'At ' + settings['timeType' + type.capitalize()]
        if(settings['timeSunType' + type.capitalize()] && settings['timeSunType' + type.capitalize()] != 'at' && !settings['timeSunOffset' + type.capitalize()]) sectionTitle += settings['timeSunType' + type.capitalize()].capitalize() + ' ' + settings['timeType' + type.capitalize()]
        if(settings['timeSunType' + type.capitalize()] && settings['timeSunType' + type.capitalize()] != 'at' && settings['timeSunOffset' + type.capitalize()]) sectionTitle += settings['timeSunOffset' + type.capitalize()] + ' minutes ' + settings['timeSunType' + type.capitalize()] + ' ' + settings['timeType' + type.capitalize()]
        if(settings['timeSunType' + type.capitalize()] && settings['timeSunType' + type.capitalize()] != 'at' && settings['timeSunOffset' + type.capitalize()] && validateTimes(type)) sectionTitle += ' ' + getSunriseTime(settings['timeType' + type.capitalize()], settings['timeSunOffset' + type.capitalize()], settings['timeSunType' + type.capitalize()])
    }
    return sectionTitle
}

def displayTimeTypeOption(type){
    if(type == 'stop' && (!settings['timeTypeStart'] || !validateTimes('start'))) return
    ingText = type
    if(type == 'stop') ingText = 'stopp'
    
    labelText = 'Schedule ' + type
    if(validateTimes('start')) labelText = ''
    if(type == 'start' && (!validateTimes('start') || !settings['timeType' + type.capitalize()])) labelText = ''
    if(!validateTimes('start') || !settings['timeType' + type.capitalize()]) labelText = 'Schedule ' + ingText + 'ing time'
    
    if(labelText) displayLabel(labelText)

    if(!validateSunriseMinutes(type)) displayWarning('Time ' + settings['timeSunType' + type.capitalize()] + ' ' + settings['timeType' + type.capitalize()] + ' is ' + (Math.round(settings['timeSunOffset' + type.capitalize()] / 60)) + ' hours. That\'s probably wrong.')
    
    fieldName = 'timeType' + type.capitalize()
    fieldTitle = type.capitalize() + ' time:'
    if(!settings[fieldName]){
        fieldTitle = type.capitalize() + ' time?'
        if(type == 'stop' && thisType == 'schedule') fieldTitle += ' (Select "Don\'t stop" for none)'
        if(type == 'stop' && thisType != 'schedule') fieldTitle += ' (Required with Start time.)'
        fieldTitle = highlightText(fieldTitle)
    }
    fieldTitle = addFieldName(fieldTitle, fieldName)
    fieldList = ['time':'Start at specific time', 'sunrise':'Sunrise (at, before or after)', 'sunset':'Sunset (at, before or after)']
    if(type == 'stop' && thisType == 'schedule') fieldList = ['none':'Don\'t stop', 'time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)', 'sunset':'Sunset (at, before or after)']
    if(type == 'stop' && thisType != 'schedule') fieldList = ['time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)', 'sunset':'Sunset (at, before or after)']
    input fieldName, 'enum', title: fieldTitle, multiple: false, width: getTypeOptionWidth(type), options: fieldList, submitOnChange:true
    if(!settings['timeTypeStart']) displayInfo('Select whether to enter a specific time, or have start time based on sunrise and sunset for the Hubitat location. Required.')
}

def displayTimeOption(type){
    if(type == 'stop' && !validateTimes('start')) return
    if(type == 'stop' && settings['timeTypeStop'] == 'none') return
    if(settings['timeType' + type.capitalize()] != 'time') return
    if(type == 'stop' && (!settings['timeTypeStart'] || !validateTimes('start'))) return

    fieldName = 'time' + type.capitalize()
    fieldTitle = type.capitalize() + ' time:'
    fieldTitle = addFieldName(fieldTitle, fieldName)
    if(!settings[fieldName]) fieldTitle = highlightText(fieldTitle)
    input fieldName, 'time', title: fieldTitle, width: getTypeOptionWidth(type), submitOnChange:true
    // Add separate alert for if noon or midnight are actually entered
    if(!settings[fieldName]) displayInfo('Enter the time to ' + type + ' the schedule in "hh:mm AM/PM" format. Midnight is "am", and noon is "pm". Required.')
}
                                     
def getTypeOptionWidth(type){
    if(!settings['timeType' + type.capitalize()]) return 12
    if(type == 'stop' && settings['timeType' + type.capitalize()] == 'none') return 12
    if(settings['timeSunType' + type.capitalize()] && settings['timeSunType' + type.capitalize()] != 'at') return 4
    return 6
}

def displaySunriseTypeOption(type){
    if(!settings['timeType' + type.capitalize()]) return
    if(settings['timeType' + type.capitalize()] == 'time') return
    if(type == 'stop' && settings['timeTypeStop'] == 'none') return
    if(type == 'stop' && !validateTimes('start')) return
    if(settings['timeType' + type.capitalize()] != 'sunrise' && settings['timeType' + type.capitalize()] != 'sunset') return
    
    sunTime = getSunriseAndSunset()[settings['timeType' + type.capitalize()]].format('hh:mm a')

    fieldName = 'timeSunType' + type.capitalize()
    fieldTitle = 'At, before or after ' + settings['timeType' + type.capitalize()] + ' (' + sunTime + '):'
    fieldTitle = addFieldName(fieldTitle, fieldName)
    if(!settings[fieldName]) fieldTitle = highlightText(fieldTitle)
    input fieldName, 'enum', title: fieldTitle, multiple: false, width: getTypeOptionWidth(type), options: ['at':'At ' + settings['timeType' + type.capitalize()], 'before':'Before ' + settings['timeType' + type.capitalize()], 'after':'After ' + settings['timeType' + type.capitalize()]], submitOnChange:true
    
    if(!settings[fieldName]) displayInfo('Select whether scheduling starts exactly at ' + settings['timeType' + type.capitalize()] + ' (currently ' + sunTime + '). To allow entering minutes prior to or after ' + settings['timeType' + type.capitalize()] + ', select "Before ' + settings['timeType' + type.capitalize()] + '" or "After ' + settings['timeType' + type.capitalize()] + '". Required.')
    displaySunriseOffsetOption(type)
}

def getSunriseTime(type, sunOffset, sunriseType){
    if(type == 'sunrise' && sunriseType == 'before' && sunOffset) return '(' + new Date(parent.getSunrise((sunOffset * -1), app.id)).format('hh:mm a', location.timeZone) + ')'
    if(type == 'sunrise' && sunriseType == 'after' && sunOffset) return '(' + new Date(parent.getSunrise(sunOffset, app.id)).format('hh:mm a', location.timeZone) + ')'
    if(type == 'sunset' && sunriseType == 'before' && sunOffset) return '(' + new Date(parent.getSunset((sunOffset * -1), app.id)).format('hh:mm a', location.timeZone) + ')'
    if(type == 'sunset' && sunriseType == 'after' && sunOffset) return '(' + new Date(parent.getSunset(sunOffset, app.id)).format('hh:mm a', location.timeZone) + ')'
    if(type == 'sunrise' && sunriseType == 'at') return '(' + new Date(parent.getSunrise(0, app.id)).format('hh:mm a', location.timeZone) + ')'
    if(type == 'sunset' && sunriseType == 'at') return '(' + new Date(parent.getSunset(0, app.id)).format('hh:mm a', location.timeZone) + ')'   
}

def displaySunriseOffsetOption(type){
    if(type == 'stop' && !validateTimes('start')) return
    if(type == 'stop' && settings['timeTypeStop'] == 'none') return
    if(!settings['timeSunType' + type.capitalize()]) return
    if(settings['timeSunType' + type.capitalize()] == 'at') return

    fieldName = 'timeSunOffset' + type.capitalize()
    timeUnits = 'minutes'
    fieldTitle = timeUnits.capitalize() + ' ' + settings['timeSunType' + type.capitalize()] + ' ' + settings['timeType' + type.capitalize()] + ':'
    fieldTitle = addFieldName(fieldTitle, fieldName)
    input fieldName, 'number', title: fieldTitle, width: getTypeOptionWidth(type), submitOnChange:true

    message = 'Enter the number of ' + timeUnits + ' ' + settings['timeSunType' + type.capitalize()] + ' ' + settings['timeType' + type.capitalize()] + ' for schedule to ' + type + '. Required.'
    if(!settings[fieldName]) displayInfo(message)
    if(!validateSunriseMinutes(type)) displayWarning(message)
}

def displayDaysAndDatesSection(thisType){
    if(thisType != 'schedule' && !settings['controlDevice']) return
    if(!settings['controlDevice']) return
    if(!settings['timeTypeStart']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(!settings['advancedSetup'] && !settings['days'] && !settings['includeDates'] && !settings['excludeDates']) return
    hidden = true
    
    processDates(true)
	message = parent.getDateProcessingErrors(app.id)
    if(message) hidden = false

    sectionTitle = '<b>Days/Dates:'
    if(!settings['days'] && !settings['includeDates'] && !settings['excludeDates']) sectionTitle = 'Click to set days/dates (optional)'
    List dayList=[]
    settings['days'].each{
        dayList.add(it)
    }
    dayText = dayList.join(', ')
    if(settings['days']) sectionTitle += ' ' + dayText
    if(settings['includeDates']) sectionTitle += ' +[included dates]'
    if(settings['excludeDates']) sectionTitle += ' +[excluded dates]'
    if(settings['days'] || settings['includeDates'] || settings['excludeDates']) sectionTitle += '</b>'
    if((!settings['days'] || !settings['includeDates'] || !settings['excludeDates']) && (settings['days'] || settings['includeDates'] || settings['excludeDates'])) sectionTitle += moreOptions
    section(hideable: true, hidden: hidden, sectionTitle){
        if(message) displayWarning(message)
        displayDaysOption()
        displayIncludeDates()
        displayExcludeDates(thisType)
    }
}

def displayDaysOption(){
    if(thisType == 'schedule' && !settings['timeTypeStart']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return

    fieldName = 'days'
    if(!settings['advancedSetup'] && !settings[fieldName]) return
    fieldTitle = 'On these days (Optional; defaults to all days):'
    if(!settings[fieldName]) fieldTitle = 'On which days (Optional; defaults to all days)?'
    options = ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday']
    displaySelectField(fieldName, fieldTitle, options, true, false)
    //input fieldName, 'enum', title: fieldTitle, multiple: true, width: 12, options: ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday'], submitOnChange:true
}

def displayDatesOptions(thisType){
    displayIncludeDates()
    displayExcludeDates(thisType)
}

def displayIncludeDates(){
    if(!settings['advancedSetup'] && !settings['includeDates']) return
    displayWarning(parent.getDateProcessingErrors(app.id))
    fieldName = 'includeDates'
    fieldTitle = 'Only on dates:'
    if(thisType == 'schedule') fieldTitle = 'Dates on which to run ("include"):'
    fieldTitle = addFieldName(fieldTitle, fieldName)
    input fieldName, "textarea", title: fieldTitle, submitOnChange:true
}

def displayTextField(fieldName, fieldTitle, fieldType, required = true){
    width = 10
    if(!settings[fieldName]) width = 12
    fieldTitle = addFieldName(fieldTitle, fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(required && !settings[fieldName]) displayLabel(highlightText(fieldTitle))
    if(!required && !settings[fieldName]) displayLabel(fieldTitle)
    input name: fieldName, type: fieldType, title: '', width:width, submitOnChange:true
}
def displayBoolField(fieldName, fieldTitleTrue, fieldTitleFalse, required = true, defaultValue = false){
    if(!fieldTitleTrue) fieldTitleTrue = ''
    if(!fieldTitleFalse) fieldTitleFalse = ''
    if(required && fieldTitleTrue) fieldTitleTrue = highlightText(fieldTitleTrue)
    if(fieldTitleTrue) fieldTitle = fieldTitleTrue
    if(required) fieldTitle += ' ' + fieldTitleFalse
    if(!required) fieldTitle += '<br> ' + fieldTitleFalse
    fieldTitle = addFieldName(fieldTitle, fieldName)
    input name: fieldName, type: 'bool', title:fieldTitle, defaultValue:defaultValue, submitOnChange:true
}
def displaySelectField(fieldName, fieldTitle, options, multiple = false, required = true, button = '', width = 12){
    if(button) width--
    if(settings[fieldName]) width = width - 2
    fieldTitle = addFieldName(fieldTitle, fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(required && !settings[fieldName]) displayLabel(highlightText(fieldTitle))
    if(!required && !settings[fieldName]) displayLabel(fieldTitle)
    input name: fieldName, type: 'enum', title: '', options: options, width:width, multiple: multiple, submitOnChange:true
    if(button) displayFilterButton(button)
}
def displayDeviceSelectField(fieldName, fieldTitle, capability, multiple, button = ''){
    width = 12
    if(button) width = 11
    fieldTitle = addFieldName(fieldTitle, fieldName)
    input name: fieldName, type: capability, title:fieldTitle, multiple: multiple, submitOnChange:true, width: width
    if(button) displayFilterButton(button)
}
def displayModeSelectField(fieldName, fieldTitle, multiple = false, required = true){
    width = 10
    if(!settings[fieldName]) width = 12
    fieldTitle = addFieldName(fieldTitle, fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(required && !settings[fieldName]) displayLabel(highlightText(fieldTitle))
    if(!required && !settings[fieldName]) displayLabel(fieldTitle)
    input name: fieldName, type: 'mode', title: '', width:width, multiple: multiple, submitOnChange:true
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
//Used in UI tooltip example getCurrentYear(location.timeZone)
def getNextYearWithMondayChristmas(currentYear) {
    def mondayChristmas = false
    while (!mondayChristmas) {
        currentYear++
        christmas = parent.getCalendar(currentYear, 12, 25, location.timeZone)
        if (parent.getCalendarWeekday(christmas, location.timeZone) == 'Monday') mondayChristmas = true
        
    }
    return currentYear
}

// Returns 'start' or 'stop' time (of day) in millis
// Must be converted with getDatetimeFromTimeInMillis if compared to now()
def getBaseStartStopDateTime(type){
    if(type == 'stop' && !settings['timeTypeStop']) return
    if(type == 'stop' && settings['timeTypeStop'] == 'none') return
    if(type == 'start' && !settings['timeTypeStart']) return
    if(settings['timeType' + type.capitalize()] == 'time') {
        if(!settings['time' + type.capitalize()]) return
        return timeToday(settings['time' + type.capitalize()]).getTime()
    }
    if(!settings['timeSunType' + type.capitalize()]) return

    if(settings['timeType' + type.capitalize()] == 'sunrise') {
        if(settings['timeSunType' + type.capitalize()] == 'before') return parent.getSunrise(settings['timeSunOffset' + type.capitalize()] * -1, app.id)
        if(settings['timeSunType' + type.capitalize()] == 'after') return parent.getSunrise(settings['timeSunOffset' + type.capitalize()], app.id)
        if(settings['timeSunType' + type.capitalize()] == 'at') return parent.getSunrise(0, app.id)
    }
    if(settings['timeType' + type.capitalize()] == 'sunset') {
        if(settings['timeSunType' + type.capitalize()] == 'before') return parent.getSunset((settings['timeSunOffset' + type.capitalize()] * -1), app.id)
        if(settings['timeSunType' + type.capitalize()] == 'after') return parent.getSunset(settings['timeSunOffset' + type.capitalize()], app.id)
        if(settings['timeSunType' + type.capitalize()] == 'at') return parent.getSunset(0, app.id)
    }
}

// Date map is [yyyy:[ddd, ddd, ddd......]]
def checkIncludeDates(){
    if(!atomicState?.includeDates) return true
    currentYear = parent.getCurrentYear(location.timeZone)
    
    if(!atomicState.includeDates?."${currentYear}") processDates()        // If a new year
    
    if(atomicState.includeDates."${currentYear}".contains(new Date(now()).format('D', location.timeZone).toInteger())) return true
}

def processDates(retrieveErrors = false){
    atomicState.remove('includeDates')
    if(!settings['days'] && !settings['includeDates'] && !settings['excludeDates']) return
    currentYear = new Date().format('yyyy', location.timeZone).toInteger()
    includeDatesValue = settings['includeDates']
    if(!settings['includeDates'] && (settings['days'] || settings['excludeDates'])) includeDatesValue = ''
    dateMap = parent.processDates(includeDatesValue, settings['excludeDates'], settings['days'], retrieveErrors, app.id)
    if(dateMap == null) return
    atomicState.'includeDates' = [(currentYear):dateMap]
}

// Not used with scheduler app
// If time, sets persistent variables for:
// timestampStart = time of day (no date) for start
// timestampStop = time of day (no date) for stop
def setTime(){
    atomicState.remove('timestampStart')
    atomicState.remove('timestampStop')
    if(!settings['timeTypeStart']) return
    if(!settings['timeTypeStop']) return
    
    // Not sure is resyncing timestampStart and timestampStop is neccesary if set a specific times (not sunrise/set)
    parent.scheduleChildEvent(parent.CONSTDayInMilli(), '', 'setTime', '', app.id)    // Schedule setting time every for each day, to keep sunrise/set accurate
    
    timestampStart = parent.getTimeOfDayInMillis(getBaseStartStopDateTime('start'), app.id)
    if(timestampStart == null) {		// Use null for midnight = 0
        putLog(1693, 'error', 'Schedule error with starting time.')
        return
    }

    timestampStop = parent.getTimeOfDayInMillis(getBaseStartStopDateTime('stop'), app.id)

    if(timestampStop == 0) timestampStop += 1                         // If midnight, don't have zero to prevent false null checks.
    
    atomicState.timestampStart = timestampStart
    
    if(!timestampStop) return
    atomicState.timestampStop = timestampStop
}

def setScheduleProcessDates(){
    if(!settings['includeDates'] && !settings['excludeDates'] && !settings['days']) return
    nextYearTimestamp = java.time.Year.now().plusYears(1).atDay(1).atStartOfDay(location.timeZone.toZoneId()).toInstant().toEpochMilli() + 1
    parent.scheduleChildEvent('', nextYearTimestamp, 'processDates', '', app.id)
    putLog(1711, 'trace', 'Scheduled updating days, include dates and/or exclude dates for next year: ' + nextYearTimestamp)
}

// Called from parent.scheduleChildEvent
def setScheduleFromParent(timeMillis, scheduleFunction, scheduleParameters = null){
    if(!scheduleParameters) scheduleParameters = [:]
    runInMillis(timeMillis, scheduleFunction, scheduleParameters)
}

def getDeviceName(singleDevice){
    if(!singleDevice) return
    if(singleDevice.label) return singleDevice.label
    return singleDevice.name
}

def performToggle(singleDevice, action){
    if(action != 'on' && action != 'off' && action != 'toggle') return
    if(action != 'toggle') return action
    if(parent.checkIsOn(singleDevice, app.id)) return 'off'
    return 'on'
}

def displayExcludeDates(appType){
    fieldName = 'excludeDates'
    if(!settings['advancedSetup'] && !settings[fieldName]) return
    fieldTitle = 'Not on dates:'
    if(appType == 'schedule') fieldTitle = 'Dates on which to <u>not</u> run ("exclude"):'
    fieldTitle = addFieldName(fieldTitle, fieldName)
    input fieldName, "textarea", title: fieldTitle, submitOnChange:true
    deviceText = 'the device'
    if(thisType == 'schedule' && settings['controlDevice'].size() > 1) deviceText = 'the devices'
    if(thisType != 'schedule' && settings['controlDevice'].size() > 1) deviceText = 'the devices'
    
    currentYear = parent.getCurrentYear(location.timeZone)
    christmasMondayYear = getNextYearWithMondayChristmas(currentYear)
    infoTip = 'Enter which date(s) to allow or restrict this ' + thisDescription + ' routine. "Only on dates" are when this ' + thisDescription + ' will work. \
"Not on" dates are when this ' + thisDescription + ' will not apply. Rules:\n\
	• Year is optional, but would only apply to that <i>one day</i>. If no year is entered, it will repeat annually. \
<i>Example: "12/25/' + (currentYear - 1) + '" will never occur in the future, because that\'s how time works.</i>\n\
	• Use month/day ("mm/dd") format, or day.month ("dd.mm"). You can also use Julian days of the year as a 3-digit number ("ddd"). \
<i>Example: Christmas could be entered as "12/25", "25.12" or "359" (the latter only true for non-leap years, otherwise "360").</i>\n\
	• Separate multiple dates with a comma (or semicolon). \
<i>Example: "12/25, 1/1" is Christmas and New Year\'s Day.</i>\n\
	• Use a hyphen to indicate a range of dates. \
<i>Example: "12/25-1/6" are the 12 days of Christmas.</i>\n\
    	• The "days" options above will restrict include the dates. \
<i>Example: Selecting Monday and entering "12/25" as an "only on" date would only allow activation on 12/25/' + christmasMondayYear + ', 12/25/' + getNextYearWithMondayChristmas((christmasMondayYear + 1)) + ', etc. when Christmas is on a Monday.</i>\n\
	• You can mix and match formats (even tho you probably shouldn\'t), and individual dates with ranges. And the order doesn\'t matter. \
<i>Example: "001, 31.10, 12/25/' + (currentYear.toString().substring(2)) + '-12/31/' + (currentYear) + '" is every Halloween, Christmas to New Years\' Eve of ' + (currentYear) + ', and every New Years\' Day.</i>\n\
	• If a date falls within both "only on" and "not on", it will be treated as "not on".</i>'

    displayInfo(infoTip)
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber, type = 'trace', message = null){
    appId = app.id

    return parent.putLog(lineNumber, type, message, app.id, 'True')
}
