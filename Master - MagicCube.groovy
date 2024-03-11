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
*  Version: 0.4.1.4
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

preferences {
    page(name: "setup", install: true, uninstall: true) {
        // display Name
        if(!app.label){
            section(){
                displayNameOption()
            }
        } else {
            section(){
                displayNameOption()
                displayMagicCubeOption()
                displayMultiDeviceOption()
                if(settings['multiDevice']) displayAdvancedOptions()
                displayDeviceOption()
                displaySingleDeviceSetup()
            }
        }
        if(app.label && buttonDevice && multiDevice){

            section(hideable: true, hidden: true, 'Rotating clockwise' + expandText) {
                getButton('clockwise','dimmer')
            }
            section(hideable: true, hidden: true, 'Rotating counter clockwise' + expandText) {
                getButton('counterClockwise','dimmer')
            }
            section(hideable: true, hidden: true, '90° flipping' + expandText) {
                getButton('flip90','switch')
            }
            section(hideable: true, hidden: true, '180° flipping' + expandText) {
                getButton('flip180','switch')
            }

            if(advancedSetup){
                section(hideable: true, hidden: true, 'Shaking' + expandText) {
                    getButton('shake','switch')
                }
                //section(hideable: true, hidden: true, 'Sliding' + expandText) {
                //    getButton('slide','dimmer')
                //}
                //section(hideable: true, hidden: true, 'Knocking' + expandText) {
                //    getButton('knock','switch')
                //}
            }
			if(button_clockwise_dim || button_clockwise_brighten || button_counterClockwise_dim || button_counterClockwise_brighten || button_flip90_dim || button_flip90_brighten || button_flip180_dim || button_flip180_brighten || button_shake_dim || button_shake_brighten || button_slide_dim || button_slide_brighten || button_knock_dim || button_knock_brighten){
				section(){
					paragraph "<div style=\"background-color:BurlyWood\"><b> Set dim and brighten speed:</b></div>"
					paragraph "$infoIcon Multiplier/divider for dimming and brightening, from 1.01 to 99, where higher is faster. For instance, a value of 2 would double (eg from 25% to 50%, then 100%), whereas a value of 1.5 would increase by half each time (eg from 25% to 38% to 57%)."
					input "multiplier", "decimal", required: false, title: "Mulitplier? (Optional. Default 1.2.)", width: 6
				}
			}
		}
    }
}

def displayNameOption(){
    if(app.label){
        displayLabel('MagicCube setup name',2)
        label title: '', required: false, width: 10,submitOnChange:true
    } else {
        displayLabel('Set name for this MagicCube setup')
        label title: '', required: false, submitOnChange:true
        displayInfo('Name this MagicCube setup. Each MagicCube setup must have a unique name.')
    }
}

def displayMagicCubeOption(){
    if(!app.label) return
    displayLabel('Select MagicCube device(s) to setup')

    fieldName = 'buttonDevice'
    fieldTitle = 'MagicCube(s)?'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'capability.pushableButton', title: fieldTitle, multiple: true, submitOnChange:true
    if(!settings[fieldName]) displayInfo('Select which MagicCube(s) to control. You can select multiple MagicCubes devices.')
}

def displayMultiDeviceOption(){
    if(!settings['buttonDevice']) return
    fieldName = 'multiDevice'
    fieldTitle = highlightText('MagicCube actions do same thing for all devices') + ' Click to do different things with different devices. (If only controlling one device, leave off.)'
    if(!settings[fieldName]) highlightText('MagicCube actions unique per device') + '  Click for buttons to do the same thing across all devices.'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'bool', title: fieldTitle, submitOnChange:true
}

def displayAdvancedOptions(){
    if(!settings['buttonDevice']) return
    fieldName = 'advancedSetup'
    //fieldTitle = '<b>Advanced actions.</b> Click to hide slide, knock and shake, and less used functions.'
    fieldTitle = '<b>Advanced actions.</b> Click to hide  less used functions like "shake".'
    //if(!settings[fieldName]) fieldTitle = '<b>Simple actions.</b> Click to show slide, knock and shake, and less used functions.'
    if(!settings[fieldName]) fieldTitle = '<b>Simple actions.</b> Click to show less used functions like "shake".'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'bool', title: fieldTitle, submitOnChange:true
    if(settings['multiDevice']) displayInfo('Select each MagicCube action, and assign devices for the outcome.')
}

def displayDeviceOption(){
    if(!settings['buttonDevice']) return
    if(settings['multiDevice']) return
    fieldName = 'controlDevice'
    fieldTitle = 'Device(s) to control'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'capability.switch', title: fieldTitle, multiple: true, submitOnChange:true
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

def displaySingleDeviceSetup(){
    if(settings['multiDevice']) return
    if(!settings['controlDevice']) return
    
    displayAdvancedOptions()
    displayLabel('Select what to do for each MagicCube action')
// Need to add summary field titles
    fieldName = 'clockwise'
    fieldTitle = 'When <b>rotating clockwise</b>, what to do with lights/switches?'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    fieldOptions = getActionsEnum('dimmer')
    input fieldName, 'enum', title: fieldTitle, required: false, multiple: false, width: 6, options: fieldOptions, submitOnChange:true

    fieldName = 'counterClockwise'
    fieldTitle = 'When <b>rotating counter-clockwise</b>, what to do with lights/switches?'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'enum', title: fieldTitle, required: false, multiple: false, width: 6, options: fieldOptions, submitOnChange:true

    fieldName = 'flip90'
    fieldTitle = 'When <b>flipping 90°</b>, what to do with lights/switches?'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    fieldOptions = getActionsEnum('switch')
    input fieldName, 'enum', title: fieldTitle, required: false, multiple: false, width: 6, options: fieldOptions, submitOnChange:true

    fieldName = 'flip180'
    fieldTitle = 'When <b>flipping 180°</b>, what to do with lights/switches?'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    fieldOptions = getActionsEnum('switch')
    input fieldName, 'enum', title: fieldTitle, required: false, multiple: false, width: 6, options: fieldOptions, submitOnChange:true

    if(settings['advancedSetup']){
        fieldName = 'shake'
        fieldTitle = 'When <b>shaking</b>, what to do with lights/switches?'
        fieldTitle = addFieldName(fieldTitle,fieldName)
        fieldOptions = getActionsEnum('switch')
        input fieldName, 'enum', title: fieldTitle, required: false, multiple: false, width: 6, options: fieldOptions, submitOnChange:true

        /*
        fieldName = 'knock'
        fieldTitle = 'When <b>knocking</b>, what to do with lights/switches?'
        fieldTitle = addFieldName(fieldTitle,fieldName)
        fieldOptions = getActionsEnum('switch')
        input fieldName, 'enum', title: fieldTitle, required: false, multiple: false, width: 6, options: fieldOptions, submitOnChange:true
*/

        /*
        fieldName = 'slide'
        fieldTitle = 'When <b>sliding</b>, what to do with lights/switches?'
        fieldTitle = addFieldName(fieldTitle,fieldName)
        fieldOptions = getActionsEnum('dimmer')
        input fieldName, 'enum', title: fieldTitle, required: false, multiple: false, width: 6, options: fieldOptions, submitOnChange:true
*/
    }
    if(clockwise == "dim" || clockwise == "brighten" || flip90 == "dim" || flip90 == "brighten" || flip180 == "dim" || flip180 == "brighten" || shake == "dim" || shake == "brighten" || knock == "dim" || knock == "brighten"){
        paragraph "<div style=\"background-color:BurlyWood\"><b> Set dim and brighten speed:</b></div>"
        displayInfo('Multiplier/divider for dimming and brightening, from 1.01 to 99, where higher is faster. For instance, a value of 2 would double (eg from 25% to 50%, then 100%), whereas a value of 1.5 would increase by half each time (eg from 25% to 38% to 57%).')
        input "multiplier", "decimal", required: false, title: "Mulitplier? (Optional. Default 1.2.)", width: 6
    }
    if(button_1_push_on || button_1_push_off || button_1_push_dim || button_1_push_brighten || button_1_push_toggle || button_2_push_on || button_2_push_off || button_2_push_dim || button_2_push_brighten || button_2_push_toggle || button_3_push_on || button_3_push_off || button_3_push_dim || button_3_push_brighten || button_3_push_toggle || button_4_push_on || button_4_push_off || button_4_push_dim || button_4_push_brighten || button_4_push_toggle || button_5_push_on || button_5_push_off || button_5_push_dim || button_5_push_brighten || button_5_push_toggle || button_1_hold_on || button_1_hold_off || button_1_hold_dim || button_1_hold_brighten || button_1_hold_toggle || button_2_hold_on || button_2_hold_off || button_2_hold_dim || button_2_hold_brighten || button_2_hold_toggle || button_3_hold_on || button_3_hold_off || button_3_hold_dim || button_3_hold_brighten || button_3_hold_toggle || button_4_hold_on || button_4_hold_off || button_4_hold_dim || button_4_hold_brighten || button_4_hold_toggle || button_5_hold_on || button_5_hold_off || button_5_hold_dim || button_5_hold_brighten || button_5_hold_toggle){
        paragraph "<div style=\"background-color:LightCyan\"><b> Click \"Done\" to continue.</b></div>"
    }
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

// values sent as list
// values.0 = number
// values.1 = on/off/toggle/dim/brighten
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
    input "button_" + values[0] + "_" + values[1], "capability.$switchType", title: text, multiple: true, submitOnChange:true
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
    putLog(430,'trace','Installed')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    putLog(436,'trace','Updated')
    unsubscribe()
    initialize()
}

def initialize() {
    putLog(442,'trace','Initialized')

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
    putLog(486,'trace','Set ' + device + ' as ' + action)
    
    device.each{singleDevice->
        if(action == 'dim' || action == 'brighten') level = parent._getNextLevelDimmable(singleDevice, action, app.label)
        levelMap = parent.getLevelMap('brightness',level,app.id,'',childLabel)         // dim, brighten

        stateMap = parent.getStateMapSingle(singleDevice,action,app.id,app.label)       // on, off, toggle
        if(level) stateMap = parent.getStateMapSingle(singleDevice,'on',app.id,app.label)
        
        fullMap = parent.addMaps(stateMap,levelMap)
        if(fullMap) putLog(496,'trace','Updating settings for ' + singleDevice + ' to ' + fullMap)
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
    if(!atomicState.priorSide) putLog(515,'warn','Prior button not known. If this is not the first run of the app, this indicates a problem.')

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

    putLog(531,'debug','' + buttonDevice + ' action captured as ' + atomicState.actionType + ' (event = ' + evt.name + '; side = ' + evt.value + ').')
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
