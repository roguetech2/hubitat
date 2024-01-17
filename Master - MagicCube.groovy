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
*  Version: 0.3.02
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
moreOptions = ' <font color="grey">(click for more options)</font>'
expandText = ' (Click to expand/collapse)'

// logLevel sets number of log messages
// 0 for none
// 1 for errors only
// 5 for all
def getLogLevel(){
    return 5
}

def displayLabel(text, width = 12){
    if(!text) return
    paragraph('<div style="background-color:#DCDCDC"><b>' + text + ':</b></div>',width:width)
}

def displayInfo(text,noDisplayIcon = null){
    if(!text) return
    paragraph '<div style="background-color:AliceBlue">' + infoIcon + ' ' + text + '</div>'
    helpTip = ''
}

def displayError(text){
    if(!text) return
    paragraph '<div style="background-color:Bisque">' + errorIcon  + ' ' + text + '</div>'
    errorMessage = ''
}

def displayWarning(text){
    if(!text) return
    paragraph '<div style="background-color:LemonChiffon">' + warningIcon  + ' ' + text + '</div>'
    warningMessage = ''
}

def highlightText(text){
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
                getButton(6,'dimmer')
            }
            section(hideable: true, hidden: true, 'Rotating counter clockwise' + expandText) {
                getButton(7,'dimmer')
            }
            section(hideable: true, hidden: true, '90° flipping' + expandText) {
                getButton(2,'switch')
            }
            section(hideable: true, hidden: true, '180° flipping' + expandText) {
                getButton(3,'switch')
            }

            if(advancedSetup){
                section(hideable: true, hidden: true, 'Shaking' + expandText) {
                    getButton(1,'switch')
                }
                section(hideable: true, hidden: true, 'Sliding' + expandText) {
                    getButton(4,'dimmer')
                }
                section(hideable: true, hidden: true, 'Knocking' + expandText) {
                    getButton(5,'switch')
                }
            }
			if(button_1_dim || button_1_brighten || button_2_dim || button_2_brighten || button_3_dim || button_3_brighten || button_4_dim || button_4_brighten || button_5_dim || button_5_brighten || button_6_dim || button_6_brighten || button_7_dim || button_7_brighten){
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
    displayLabel('Set name for this MagicCube setup')
    label title: '', required: true, submitOnChange:true
    if(!app.label) displayInfo('Name this MagicCube setup. Each MagicCube setup must have a unique name.')
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

def getButton(buttonNumber,type){
    if(type == 'dimmer'){
        button = [buttonNumber,'brighten']
        getAdvancedSwitchInput(button)

        button = [buttonNumber,'dim']
        getAdvancedSwitchInput(button)
        displayError(compareDeviceLists(button,'brighten'))

        button = [buttonNumber,'toggle']
        getAdvancedSwitchInput(button)

        if(settings['advancedSetup']){
            button = [buttonNumber,'on']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'toggle'))

            button = [buttonNumber,"off"]
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'brighten'))
            displayError(compareDeviceLists(button,'dim'))
            displayError(compareDeviceLists(button,'toggle'))
            displayError(compareDeviceLists(button,'on'))

            button = [buttonNumber,'resume']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'brighten'))
            displayError(compareDeviceLists(button,'dim'))
            displayError(compareDeviceLists(button,'toggle'))
            displayError(compareDeviceLists(button,'on'))
            displayError(compareDeviceLists(button,'off'))
        }
    }
    if(type == 'switch'){
        button = [buttonNumber,'on']
        getAdvancedSwitchInput(button)

        button = [buttonNumber,'off']
        getAdvancedSwitchInput(button)
        displayError(compareDeviceLists(button,'on'))

        button = [buttonNumber,'toggle']
        getAdvancedSwitchInput(button)
        displayError(compareDeviceLists(button,'on'))
        displayError(compareDeviceLists(button,'off'))

        button = [buttonNumber,'resume']
        getAdvancedSwitchInput(button)
        displayError(compareDeviceLists(button,'on'))
        displayError(compareDeviceLists(button,'off'))
        displayError(compareDeviceLists(button,'toggle'))

        if(settings['advancedSetup']){
            button = [buttonNumber,'dim']
            getAdvancedSwitchInput(button)
            displayError(compareDeviceLists(button,'off'))
            displayError(compareDeviceLists(button,'resume'))

            button = [buttonNumber,'brighten']
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

def getDimSpeed(){
    if(multiplier){
        return multiplier
    } else {
        return 1.2
    }
}

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

def installed() {
    putLog(404,'trace','Installed')
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    putLog(410,'trace','Updated')
    unsubscribe()
    initialize()
}

def initialize() {
    putLog(416,'trace','Initialized')

    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

    subscribe(buttonDevice, "pushed.1", buttonEvent)
    subscribe(buttonDevice, "pushed.2", buttonEvent)
    subscribe(buttonDevice, "pushed.3", buttonEvent)
    subscribe(buttonDevice, "pushed.4", buttonEvent)
    subscribe(buttonDevice, "pushed.5", buttonEvent)
    subscribe(buttonDevice, "pushed.6", buttonEvent)
    
    //doubleTapped, release and hold used by kkossev's driver
    subscribe(buttonDevice, "doubleTapped.1", buttonEvent)
    subscribe(buttonDevice, "doubleTapped.2", buttonEvent)
    subscribe(buttonDevice, "doubleTapped.3", buttonEvent)
    subscribe(buttonDevice, "doubleTapped.4", buttonEvent)
    subscribe(buttonDevice, "doubleTapped.5", buttonEvent)
    subscribe(buttonDevice, "doubleTapped.6", buttonEvent)
    
    subscribe(buttonDevice, "release.1", buttonEvent)
    subscribe(buttonDevice, "release.2", buttonEvent)
    subscribe(buttonDevice, "release.3", buttonEvent)
    subscribe(buttonDevice, "release.4", buttonEvent)
    subscribe(buttonDevice, "release.5", buttonEvent)
    subscribe(buttonDevice, "release.6", buttonEvent)
    
    subscribe(buttonDevice, "hold.1", buttonEvent)
    subscribe(buttonDevice, "hold.2", buttonEvent)
    subscribe(buttonDevice, "hold.3", buttonEvent)
    subscribe(buttonDevice, "hold.4", buttonEvent)
    subscribe(buttonDevice, "hold.5", buttonEvent)
    subscribe(buttonDevice, "hold.6", buttonEvent)
}

def buttonEvent(evt){
    convertDriver(evt)

    // Set device if using simple setup
    if(!multiDevice){
        def switchButtonsInput = [:]
        def switchButtonsOutput = [:]
        switchButtonInput.put('input','shake')
        switchButtonInput.put('input','flip90')
        switchButtonInput.put('input','flip180')
        switchButtonInput.put('input','slide')    // Slide isn't used, but needs to be in the loop, maybe?
        switchButtonInput.put('input','knock')    // Knock isn't used, but needs to be in the loop, maybe?
        switchButtonInput.put('input','clockwise')
        switchButtonInput.put('input','counterClockwise')
        switchButtonOutput.put('output','on')
        switchButtonOutput.put('output','off')
        switchButtonOutput.put('output','dim')
        switchButtonOutput.put('output','brighten')
        switchButtonOutput.put('output','toggle')
        switchButtonOutput.put('output','resume')

        // Loop through button types ("shake", "flip90", etc)
        switchButtonInput.each{input->
            // Loop through actions ("on", "off", etc)
            switchButtonOutput.each{output->
                // Check if button type is set to action (eg "if($shake == 'on')")
                if(settings[input.input] && settings[input.input] == output.output){
                    // on, off, and toggle use multiOn(); dim and brighten use dim()
                    if(output.output == 'on' || output.output == 'off' || output.output == 'toggle') parent.updateStateMulti(controlDevice,output.output,app.label)
                    if(output.output == 'dim' || output.output == 'brighten') {
                        parent.updateStateMulti(controlDevice,'on',app.label)
                        controlDevice.each{singleDevice->
                            setLevel = parent.nextLevel(singleDevice,output.output,app.label)
                            if(setLevel) defaults = ['level': ['startLevel': setLevel, 'appId':'magiccube']]
                        }
                        parent.updateLevelsMulti(device,defaults,app.label)
                    }
                    if(output.output == 'resume') resume(controlDevice,app.label)
                    result = true
                }
            }
        }

        if(!result) putLog(538,'trace','No action defined for ' + atomicState.buttonNumber + ' of ' + evt.displayName)
    } else {
        device = settings['button_' + atomicState.buttonNumber + '_on']
        if(device) {
            putLog(542,'trace','Turning ' + device + ' on')
            parent.updateStateMulti(device,'on',app.label)
            parent.setStateMulti(device,app.label)
        }
        device = settings['button_' + atomicState.buttonNumber + '_off']
        if(device) {
            putLog(548,'trace','Turning ' + device + ' off')
            parent.updateStateMulti(device,'off',app.label)
            parent.setStateMulti(device,app.label)
        }
        device = settings['button_' + atomicState.buttonNumber + '_dim']
        if(device) {
            putLog(554,'trace','Dimming ' + device)
            parent.updateStateMulti(device,'on',app.label)
            device.each{singleDevice->
                setLevel = parent.nextLevel(singleDevice,'dim',app.label)
                if(setLevel) defaults = ['level': ['startLevel': setLevel, 'appId':'magiccube']]
            }
            parent.updateLevelsMulti(device,defaults,app.label)
        }
        device = settings['button_' + atomicState.buttonNumber + '_brighten']
        if(device)  {
            putLog(564,'trace','Brightening ' + devicedevice)
            parent.updateStateMulti(device,'on',app.label)
            device.each{singleDevice->
                setLevel = parent.nextLevel(singleDevice,'brighten',app.label)
                if(setLevel) defaults = ['level': ['startLevel': setLevel, 'appId':'magiccube']]
            }
            parent.updateLevelsMulti(device,defaults,app.label)
        }
        device = settings['button_' + atomicState.buttonNumber + '_toggle']
        if(device) {
            putLog(574,'trace','Toggling ' + device)
            parent.updateStateMulti(device,'toggle',app.label)
            parent.setStateMulti(device,app.label)
        }
        device = settings['button_' + atomicState.buttonNumber + '_resume']
        if(device) resume(device)
        if(!settings['button_' + atomicState.buttonNumber + '_toggle'] && !settings['button_' + atomicState.buttonNumber + '_on'] && !settings['button_' + atomicState.buttonNumber + '_off'] && !settings['button_' + atomicState.buttonNumber + '_dim'] && !settings['button_' + atomicState.buttonNumber + '_brighten'] && !settings['button_' + atomicState.buttonNumber + '_resume']){  
            putLog(581,'trace','No action defined for ' + atomicState.buttonNumber + ' of ' + evt.displayName)
        }
    }
}

def resume(device){
    // checkActiveSchedule doesn't exist
    activeSchedule = parent.checkActiveScheduleMulti(device,app.label)
    if(activeSchedule) {
        if(device) putLog(590,'trace','Resuming ' + device)
        parent.updateLevelsMulti(device,['level':['time':'resume'],'temp':['time':'resume'],'hue':['time':'resume'],'sat':['time':'resume']],app.label)
        // This isn't right - therre's no "action" (also in pico)
        if(!parent.rescheduleIncrementalMulti(device,app.label)) parent.updateStateSingle(singleDevice,action,app.label)
    }
    if(!activeSchedule) parent.updateStateMulti(device,'off',app.label)
    parent.setStateMulti(device,app.label)
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
    if(!atomicState.priorSide) putLog(610,'warn','Prior button not known. If this is not the first run of the app, this indicates a problem.')

    //flip180
    if(evt.name == 'pushed'){
        if(evt.value == '1' && atomicState.priorSide == '6') actionNumber = 3
        if(evt.value == '2' && atomicState.priorSide == '5') actionNumber = 3
        if(evt.value == '3' && atomicState.priorSide == '4') actionNumber = 3
        if(evt.value == '4' && atomicState.priorSide == '3') actionNumber = 3
        if(evt.value == '5' && atomicState.priorSide == '2') actionNumber = 3
        if(evt.value == '6' && atomicState.priorSide == '1') actionNumber = 3
        if(actionNumber == 3) putLog(620,'trace','' + buttonDevice + ' action captured as flip180.')
    }
    //flip90
    if(evt.name == 'pushed'){
        if(actionNumber != 3) {
            actionNumber = 2
            putLog(626,'trace','' + buttonDevice + ' action captured as flip90.')
        }
    }
    
    //shake
    if(evt.name == 'doubleTap'){
        actionNumber = 1
        putLog(633,'trace','' + buttonDevice + ' action captured as shake.')
    }
    
    //clockwise
    if(evt.name == 'release'){
        actionNumber = 6
        putLog(639,'trace','' + buttonDevice + ' action captured as [rotate] clockwise.')
    }
    
    //clockwise
    if(evt.name == 'hold'){
        actionNumber = 7
        putLog(645,'trace','' + buttonDevice + ' action captured as [rotate] counter-clockwise.')
    }

    atomicState.priorSide = evt.value 
    atomicState.buttonNumber = actionNumber
}

def checkLog(type = null){
    switch(type) {
        case 'error':
        if(getLogLevel() > 0) return true
        break
        case 'warn':
        if(getLogLevel() > 1) return true
        break
        case 'info':
        if(getLogLevel() > 2) return true
        break
        case 'trace':
        if(getLogLevel() > 3) return true
        break
        case 'debug':
        if(getLogLevel() == 5) return true
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,type = 'trace',message = null){
    if(!checkLog(type)) return
    return parent.putLog(lineNumber,type,message,app.label)
}
