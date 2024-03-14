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
*  Version: 0.4.1.5
* 
***********************************************************************************************************************/

// TO-DO: Allow actions per side (rather than flip90 or flip180)

definition(
    name: "Master - MagicCube",
    namespace: "master",
    author: "roguetech",
    description: "Control MagicCube",
    parent: "master:Master",
    category: "Convenience",
    importUrl: "https://raw.githubusercontent.com/roguetech2/hubitat/master/Master%20-%20MagicCube.groovy",
    iconUrl: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/mi_face_s.png",
    iconX2Url: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/mi_face.png"
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

preferences {
    page(name: "setup", install: true, uninstall: true) {
        // display Name
        if(!app.label){
            section(){
                displayNameOption()
            }
        } else {
            actionsMap = getActionsMap()
            section(){
                displayNameOption()
                displayMagicCubeOption()
                displayMultiDeviceOption()
                if(settings['multiDevice']) displayAdvancedActionsOption()
                displayDeviceOption()
            }
                displaySingleDeviceSetup()
                displayMultiDeviceSetup()
        
		}
    }
}

def getActionsMap(){
    actionsMap = [:]
    actionsMap['clockwise'] = [name:'rotate clockwise',action:'rotating clockwise',defaultType:'dimmer',advanced:false,active:true]
    actionsMap['counterClockwise'] = [name:'rotate counter-clockwise',action:'rotating counter-clockwise',defaultType:'dimmer',advanced:false,active:true]
    actionsMap['flip90'] = [name:'flip 90°',action:'flipping 90°',defaultType:'switch',advanced:false,active:true]
    actionsMap['flip180'] = [name:'flip 180°',action:'flipping 180°',defaultType:'switch',advanced:false,active:true]
    actionsMap['shake'] = [name:'shake',action:'shaking',defaultType:'switch',advanced:true,active:true]
    actionsMap['knock'] = [name:'knock',action:'knocking',defaultType:'switch',advanced:true,active:false]
    actionsMap['slide'] = [name:'slide',action:'sliding',defaultType:'dimmer',advanced:true,active:false]
    return actionsMap
}

def displayNameOption(){
    displayNameOptionComplete()
    displayNameOptionIncomplete()
}
def displayNameOptionComplete(){
    if(!app.label) return
    displayLabel('MagicCube name:',2)
    label title: '', required: false, width: 10,submitOnChange:true
}
def displayNameOptionIncomplete(){
    if(app.label) return
    fieldTitle = 'Set name for this MagicCube app:'
    displayLabel(highlightText(fieldTitle))
    label title: '', width:12, submitOnChange:true
    displayInfo('Name this MagicCube setup. Each MagicCube setup must have a unique name.')
}

def displayMagicCubeOption(){
    if(!app.label) return
    fieldName = 'buttonDevice'
    displayMagicCubeOptionComplete(fieldName)
    displayMagicCubeOptionIncomplete(fieldName)
}
def displayMagicCubeOptionComplete(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = 'MagicCube(s):'
    displayDeviceSelectionField(fieldName,fieldTitle,'capability.pushableButton',true)
}
def displayMagicCubeOptionIncomplete(fieldName){
    if(settings[fieldName]) return
    fieldTitle = 'Select MagicCube device(s) to setup:'
    displayDeviceSelectionField(fieldName,fieldTitle,'capability.pushableButton',true)
    displayInfo('Select which MagicCube(s) to control. You can select multiple MagicCubes devices.')
    
}

def displayMultiDeviceOption(){
    if(!settings['buttonDevice']) return
    fieldName = 'multiDevice'
    displayMultiDeviceOptionComplete(fieldName)
    displayMultiDeviceOptionIncomplete(fieldName)
}
def displayMultiDeviceOptionComplete(fieldName){
    if(!settings[fieldName]) return
    fieldTitleTrue = 'MagicCube actions unique per device'
    fieldTitleFalse = 'Click for buttons to do the same thing across all devices.'
    displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse)
}
def displayMultiDeviceOptionIncomplete(fieldName){
    if(settings[fieldName]) return
    fieldTitleTrue = 'MagicCube actions does the same thing for all devices'
    fieldTitleFalse = 'Click to assign devices for each action. (If only controlling one device, leave off.)'
    fieldTitle = 'MagicCube(s):'
    displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse)
}

def displayDeviceOption(){
    if(!settings['buttonDevice']) return
    if(settings['multiDevice']) return
    fieldName = 'controlDevice'
    displayDeviceOptionComplete(fieldName)
    displayDeviceOptionInomplete(fieldName)
}
def displayDeviceOptionComplete(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = 'Device(s) to control:'
    displayDeviceSelectionField(fieldName,fieldTitle,'capability.switch',true)
}
def displayDeviceOptionInomplete(fieldName){
    if(settings[fieldName]) return
    fieldTitle = 'Select which device(s) to control with the MagicCube:'
    displayDeviceSelectionField(fieldName,fieldTitle,'capability.switch',true)
    
}

def displayAdvancedActionsOption(){
    if(!settings['buttonDevice']) return
    fieldName = 'advancedSetup'
    displayAdvancedActionsOptionComplete(fieldName)
    displayAdvancedActionsOptionIncomplete(fieldName)
    if(settings['multiDevice']) displayInfo('Select each MagicCube action, and assign devices for the outcome.')
}
def displayAdvancedActionsOptionComplete(fieldName){
    if(!settings[fieldName]) return
    fieldTitleTrue = 'Advanced actions.'
    fieldTitleFalse = 'Click to hide slide, knock and shake, and less used functions.'
    displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse)
}
def displayAdvancedActionsOptionIncomplete(fieldName){
    if(settings[fieldName]) return
    fieldTitleTrue = 'Simple actions.'
    fieldTitleFalse = 'Click to show less used functions like "shake".'
    displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse)
}

def displaySingleDeviceSetup(){
    if(settings['multiDevice']) return
    if(!settings['controlDevice']) return
    
// set title as settings, and moreOptions
    sectionTitle = ''
    actionsMap.each{key,value->
        if(settings[key] && value['active']) {
            if(settings['advancedSetup'] || !value['advanced']) {
            if(sectionTitle) sectionTitle += '<br>'
            sectionTitle += '<b>' + value['name'].capitalize() + ': ' + settings[key].capitalize() + '</b>'
            }
        }
    }
    if(!sectionTitle) sectionTitle = 'Select what to do for each MagicCube action' + expandText
    section(hideable: true, hidden: false, sectionTitle){
        displayAdvancedActionsOption()
        actionsMap.each{key,value->
            displaySingleIndividualDeviceOption(key)
        }
        displayDimmingProgressionOption()
    }
}
def displaySingleIndividualDeviceOption(type){
    fieldName = type
    if(!actionsMap[fieldName].'active') return
    if(actionsMap[fieldName].'advanced' && !settings['advancedSetup']) return
    fieldTitle = 'When <b>' + actionsMap[fieldName].'name' + '</b>?'
    if(settings[fieldName]) fieldTitle = '<b>' + actionsMap[fieldName].'name'.capitalize() + '</b>:'
    fieldOptions = getActionsEnum('dimmer')
    displaySelectField(fieldName,fieldTitle,fieldOptions,false,false)
}

def displayMultiDeviceSetup(){
    if(!settings['multiDevice']) return
    if(!settings['controlDevice']) return

    actionsMap.each{key,value->
        displayMultiIndividualDeviceOption(key)
    }
    section(){
        displayDimmingProgressionOption()
    }
}
def displayMultiIndividualDeviceOption(type){
    fieldName = type
    if(!actionsMap[fieldName].'active') return
    if(actionsMap[fieldName].'advanced' && !settings['advancedSetup']) return
    section(hideable: true, hidden: true, actionsMap[fieldName].'action'.capitalize() + expandText) {
        getButton(fieldName,actionsMap[fieldName].defaultType)
    }
}

def displayTextField(fieldName,fieldTitle,type,required = true){
    width = 10
    if(!settings[fieldName]) width = 12
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(required && !settings[fieldName]) displayLabel(highlightText(fieldTitle))
    if(!required && !settings[fieldName]) displayLabel(fieldTitle)
    input name: fieldName, type: fieldType, title: fieldTitle, width:width, submitOnChange:true
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
def displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse,required = true){
    if(required) fieldTitle = highlightText(fieldTitleTrue) + ' ' + fieldTitleFalse
    if(!required) fieldTitle = fieldTitleTrue + '<br> ' + fieldTitleFalse
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input name: fieldName, type: 'bool', title:fieldTitle, submitOnChange:true
}
def displayDeviceSelectionField(fieldName,fieldTitle,capability,multiple){
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input name: fieldName, type: capability, title:fieldTitle, multiple: multiple, submitOnChange:true
}


def getActionsEnum(type){
    sliderAdvancedOptions = ['brighten':'Brighten','dim':'Dim','on':'Turn on', 'off':'Turn off', 'toggle':'Toggle']
    switchAdvancedOptions = ['on':'Turn on', 'off':'Turn off', 'toggle':'Toggle','dim':'Dim', 'brighten':'Brighten']
    sliderSimpleOptions = ['brighten':'Brighten','dim':'Dim', 'toggle':'Toggle']
    switchSimpleOptions = ['on':'Turn on', 'off':'Turn off', 'toggle':'Toggle']

    if(type == 'dimmer'){
        if(settings['advancedSetup']) return sliderAdvancedOptions
        return sliderSimpleOptions
    }
    if(settings['advancedSetup']) return switchAdvancedOptions
    return switchSimpleOptions
}

def getButton(buttonAction,type){
    if(type == 'dimmer'){
        button = [buttonAction,'brighten']
        getAdvancedSwitchInput(button)

        button = [buttonAction,'dim']
        getAdvancedSwitchInput(button)
        displayError(compareDeviceLists(button,'brighten'))

        button = [buttonAction,'toggle']
        getAdvancedSwitchInput(button)

        if(settings['advancedSetup']){
            button = [buttonAction,'on']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'toggle'))

            button = [buttonAction,"off"]
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'brighten'))
            displayError(compareDeviceLists(button,'dim'))
            displayError(compareDeviceLists(button,'toggle'))
            displayError(compareDeviceLists(button,'on'))

            button = [buttonAction,'resume']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'brighten'))
            displayError(compareDeviceLists(button,'dim'))
            displayError(compareDeviceLists(button,'toggle'))
            displayError(compareDeviceLists(button,'on'))
            displayError(compareDeviceLists(button,'off'))
        }
    }
    if(type == 'switch'){
        button = [buttonAction,'on']
        getAdvancedSwitchInput(button)

        button = [buttonAction,'off']
        getAdvancedSwitchInput(button)
        displayError(compareDeviceLists(button,'on'))

        button = [buttonAction,'toggle']
        getAdvancedSwitchInput(button)
        displayError(compareDeviceLists(button,'on'))
        displayError(compareDeviceLists(button,'off'))

        button = [buttonAction,'resume']
        getAdvancedSwitchInput(button)
        displayError(compareDeviceLists(button,'on'))
        displayError(compareDeviceLists(button,'off'))
        displayError(compareDeviceLists(button,'toggle'))

        if(settings['advancedSetup']){
            button = [buttonAction,'dim']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'off'))
            displayError(compareDeviceLists(button,'resume'))

            button = [buttonAction,'brighten']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'off'))
            displayError(compareDeviceLists(button,'resume'))
            displayError(compareDeviceLists(button,'dim'))
        }
    }
}

def displayDimmingProgressionOption(hold = false){
    if(!button_clockwise_dim && !button_counterClockwise_brighten && !button_flip90_brighten && !button_flip180_brighten && !button_shake_brighten && !button_clockwise_dim && 
        !button_counterClockwise_dim && !button_flip90_dim && !button_flip180_dim && !button_shake_dim && clockwise != 'brighten' && counterClockwise != 'brighten' && flip90 != 'brighten' &&
        flip180 != 'brighten' && shake != 'brighten' && clockwise != 'dim' && counterClockwise != 'dim' && flip90 != 'dim' && flip180 != 'dim' && shake != 'dim') return

    width = 10
    fieldName = 'pushedDimmingProgressionSteps'
    fieldTitle = 'Enter dimming steps for pushed (optional, default 8):'
    if(settings[fieldName]) fieldTitle = 'Pushed dimming steps:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(!settings[fieldName]) {
        displayLabel(highlightText(fieldTitle))
        width = 12
    }
    input fieldName, 'number', title: '', width:width,submitOnChange:true

    displayInfo('This sets how many steps it takes to brighten from 1 to 100% (or vice versa), using a geometric progression. For instance, 10 steps would be: 2, 4, 7, 11, 17, 25, 36, 52, 74, 100.')
}

// values sent as list
// values.0 = number
// values.1 = on/off/toggle/dim/brighten
// populated = value of the input
def getAdvancedSwitchInput(values,populated = null){
    if(error) return
    fieldName = 'button_' + values[0] + '_' + values[2] + '_' + values[1]
    fieldTitle = ''
    if(populated) text += '<b>'
    if(values[1] == 'on') fieldTitle += 'Turns On'
    if(values[1] == 'off') fieldTitle += 'Turns Off'
    if(values[1] == 'toggle') fieldTitle += 'Toggles (if on, turn off; if off, turn on)'
    if(values[1] == 'dim') fieldTitle += 'Dims'
    if(values[1] == 'brighten') fieldTitle += 'Brightens'
    if(values[1] == 'resume') fieldTitle += 'Resume schedule(s) (if none, turn off)'

    if(populated) fieldTitle += '</b>'
    if(!populated) fieldTitle += ' <font color="gray">(Select devices)</font>'

    if(values[1] == 'dim' || values[1] == 'brighten') switchType = 'switchLevel'
    if(values[1] != 'dim' && values[1] != 'brighten') switchType = 'switch'
    input fieldName, 'capability.' + switchType, title: addFieldName(fieldTitle,fieldName), multiple: true, submitOnChange:true
}

def compareDeviceLists(values,compare){
    // Be sure we have original button and comparison button values (odds are, we don't)
    // eg if(!button_1_push_on)
    if(!settings['button_' + values[0] + '_' + values[2] + '_' + values[1]]) return
    if(!settings['button_' + values[0] + '_' + values[2] + '_' + compare]) return

    settings['button_' + values[0] + '_' + values[2] + '_' + values[1]].each{first->
        settings['button_' + values[0] + '_' + values[2] + '_' + compare].each{second->
            if(first.id == second.id) {
                if(compare == 'on' || compare == 'off') text1 = 'turn ' + compare
                if(compare != 'on' && compare != 'off') text1 = compare
                if(values[1] == 'on' || values[1] == 'off') text2 = 'turn ' + values[1]
                if(values[1] != 'on' && values[1] != 'off') text2 = values[1]
                returnText = 'Can\'t set same button to ' + text1 + ' and ' + text2 + ' the same device.'
            }
        }
    }
    return returnText
}

/* ************************************************************************ */
/*                                                                          */
/*                          End display functions.                          */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                          End display functions.                          */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                          End display functions.                          */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                          End display functions.                          */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                          End display functions.                          */
/*                                                                          */
/* ************************************************************************ */

def installed() {
    putLog(477,'trace','Installed')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    putLog(483,'trace','Updated')
    unsubscribe()
    initialize()
}

def initialize() {
    putLog(489,'trace','Initialized')

    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))

    subscribe(buttonDevice, "pushed.1", buttonEvent)
    subscribe(buttonDevice, "pushed.2", buttonEvent)
    subscribe(buttonDevice, "pushed.3", buttonEvent)
    subscribe(buttonDevice, "pushed.4", buttonEvent)
    subscribe(buttonDevice, "pushed.5", buttonEvent)
    subscribe(buttonDevice, "pushed.6", buttonEvent)
    
    //doubleTapped, release and hold used by kkossev's driver
    subscribe(buttonDevice, "doubleTapped", buttonEvent)
    
    subscribe(buttonDevice, "released", buttonEvent)
    
    subscribe(buttonDevice, "held", buttonEvent)
}

def buttonEvent(evt){
    convertDriver(evt)     // Sets atomicState.buttonNumber to action corresponding to cubeActions

    if(multiDevice){
        doActions(settings['button_' + atomicState.actionType + '_on'],'on')
        doActions(settings['button_' + atomicState.actionType + '_off'],'off')
        doActions(settings['button_' + atomicState.actionType + '_toggle'],'toggle')
        doActions(settings['button_' + atomicState.actionType + '_dim'],'dim')
        doActions(settings['button_' + atomicState.actionType + '_brighten'],'brighten')
        doActions(settings['button_' + atomicState.actionType + '_resume'],'resume')
    }
    
    if(!multiDevice){
        if(settings[atomicState.actionType] == 'on') doActions(settings['controlDevice'],'on')
        if(settings[atomicState.actionType] == 'off') doActions(settings['controlDevice'],'off')
        if(settings[atomicState.actionType] == 'toggle') doActions(settings['controlDevice'],'toggle')
        if(settings[atomicState.actionType] == 'dim') doActions(settings['controlDevice'],'dim')
        if(settings[atomicState.actionType] == 'brighten') doActions(settings['controlDevice'],'brighten')
        if(settings[atomicState.actionType] == 'resume') doActions(settings['controlDevice'],'resume')
    }
    if(!multiDevice) parent.setDeviceMulti(settings['device'],app.label)
}

def doActions(device,action){
    if(!device) return
    putLog(533,'trace','Set ' + device + ' as ' + action)
    
    device.each{singleDevice->
        if(action == 'dim' || action == 'brighten') level = parent._getNextLevelDimmable(singleDevice, action, app.label)
        levelMap = parent.getLevelMap('brightness',level,app.id,'',childLabel)         // dim, brighten

        stateMap = parent.getStateMapSingle(singleDevice,action,app.id,app.label)       // on, off, toggle
        if(level) stateMap = parent.getStateMapSingle(singleDevice,'on',app.id,app.label)
        
        fullMap = parent.addMaps(stateMap,levelMap)
        if(fullMap) putLog(543,'trace','Updating settings for ' + singleDevice + ' to ' + fullMap)
        parent.mergeMapToTable(singleDevice.id,fullMap,app.label)
    }
    if(action == 'resume') parent.resumeDeviceScheduleMulti(device,app.label)
    if(multiDevice) parent.setDeviceMulti(device,app.label)
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
    cubeActions = ['shake', 'flip90', 'flip180', 'slide', 'knock', 'clockwise', 'counterClockwise'] // Need to put this in the UI, should be state variable
    if(!atomicState.priorSide) putLog(562,'warn','Prior button not known. If this is not the first run of the app, this indicates a problem.')

    //flip90 - look at which side it's going from and landing on
    if(evt.name == 'pushed'){
        atomicState.actionType = cubeActions[1]
        if(evt.value == '1' && atomicState.priorSide == '6') atomicState.actionType = cubeActions[2]
        if(evt.value == '2' && atomicState.priorSide == '5') atomicState.actionType = cubeActions[2]
        if(evt.value == '3' && atomicState.priorSide == '4') atomicState.actionType = cubeActions[2]
        if(evt.value == '4' && atomicState.priorSide == '3') atomicState.actionType = cubeActions[2]
        if(evt.value == '5' && atomicState.priorSide == '2') atomicState.actionType = cubeActions[2]
        if(evt.value == '6' && atomicState.priorSide == '1') atomicState.actionType = cubeActions[2]
    }
    if(evt.name == 'doubleTapped') atomicState.actionType = cubeActions[0]
    if(evt.name == 'released') atomicState.actionType = cubeActions[5]
    if(evt.name == 'held') atomicState.actionType = cubeActions[6]

    putLog(578,'debug','' + buttonDevice + ' action captured as ' + atomicState.actionType + ' (event = ' + evt.name + '; side = ' + evt.value + ').')
    atomicState.priorSide = evt.value
}

def getDimSpeed(){
    return pushMultiplier
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,type = 'trace',message = null){
    return parent.putLog(lineNumber,type,message,app.label,,getLogLevel())
}
