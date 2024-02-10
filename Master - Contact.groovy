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
*  Name: Master - Contact
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master%20-%20Contact.groovy
*  Version: 0.7.1
* 
***********************************************************************************************************************/

definition(
    name: "Master - Contact",
    namespace: "master",
    author: "roguetech",
    description: "Door Sensors",
    parent: "master:Master",
    category: "Convenience",
    importUrl: "https://raw.githubusercontent.com/roguetech2/hubitat/master/Master%20-%20Contact.groovy",
    iconUrl: "http://cdn.device-icons.smartthings.com/locks/lock/unlocked@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/locks/lock/unlocked@2x.png"
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
    install = formComplete()

    page(name: "setup", install: install, uninstall: true) {
        // display Name
        if(!app.label){
            section(){
                displayNameOption()
            }
        } else {
            if(!settings) settings = [:]
            
            // Set variables
            singleContact = "contact/door sensor"
            contactCount = getDeviceCount(sensor)
            deviceCount = getDeviceCount(device)
            pluralContact = getContactSensorPlural()
            contactIndefiniteArticle = getPluralIndefiniteArticle(contactCount)
            contactIndefiniteArticleExtended =  getPluralIndefiniteArticle(contactCount,true)
            deviceIndefiniteArticle = getPluralIndefiniteArticle(deviceCount)
            deviceIndefiniteArticleExtended =  getPluralIndefiniteArticle(deviceCount,true)
            pluralDevice = getDevicePlural()
            singleDevice = settings["deviceType"]
            plainOpenAction = getPlainAction(settings["open_action"])
            plainCloseAction = getPlainAction(settings["closed_action"])
            onAtOpen = getOnAtOpen()
            onAtClose = getOnAtClose()
            hiRezHue = parent.getHiRezHue()
            peopleError = compareDeviceLists(personHome,personNotHome)

            section(){
                displayNameOption()
            input 'pico', 'capability.pushableButton', title: 'Pico', multiple: true, submitOnChange:true
                displayDevicesOption()
                displayDevicesTypes()
                displayOpenCloseDevicesOption()
                if(install) displayDisableOption()
            }

            displayOpenOption()
            displayCloseOption()
            displayBrightnessOption()
            if((!settings["openhue"] && !settings["open_sat"]) || (!settings["closed_hue"] && !settings["closed_sat"])) displayTemperatureOption()
            if(!settings["open_temp"] || !settings["closed_temp"]) displayColorOption()
            displayChangeModeOption()
            displayAlertOptions()
            displayPeopleOption()
            displayScheduleSection()
            displayIfModeOption()
        }
    }
}

def formComplete(){
    if(!app.label) return false
    if(!sensor) return false
    if(!deviceType) return false
    if(!device) return false
    if(!open_action) return false
    if(!closed_action) return false
    if(!validateTimes('start')) return false
    if(!validateTimes('stop')) return false
    if(!validateOpenLevel()) return false
    if(!validateCloseLevel()) return false
    if(!validateOpenTemp()) return false
    if(!validateCloseTemp()) return false
    if(!validateHue(open_hue)) return false
    if(!validateHue(closed_hue)) return false
    if(!validateSat(open_sat)) return false
    if(speechDevice && !speechOpenAction == 'none') return false
    if(pushNotificationDevice && !pushNotification) return false
    if((settings['pushNotification'] || settings['speech']) && !settings['notificationOpenClose']) return false
    if((open_action == 'none' && closed_action == 'none' && !open_level && !closed_level && !open_temp && !closed_temp && !open_sat && !closed_sat && !open_hue && !closed_hue && !open_mode && !closed_mode && !pushNotification) && !speech) return false
    if(compareDeviceLists(personHome,personNotHome)) return false
    
    return true
}

// Display functions
def getDeviceCount(device){
    if(!device) return 0
    return device.size()
}

def getContactSensorPlural(){
    if(!contactCount) return singleContact + "(s)"
    if(contactCount > 1) return singleContact + "s"
    return singleContact
}

def getDevicePlural(){
    if(!deviceCount) {
        if(settings['deviceType'] == 'lock') return 'lock(s)'
        if(settings['deviceType'] == 'light') return 'light(s)'
        if(settings['deviceType'] == 'switch') return 'switch(es)'
        if(settings['deviceType'] == 'fan') return 'fan(s)'
    }
    
    if(deviceCount > 1) {
        if(settings['deviceType'] == 'lock') return 'locks'
        if(settings['deviceType'] == 'light') return 'lights'
        if(settings['deviceType'] == 'switch')return 'switches'
        if(settings['deviceType'] == 'fan')return 'fans'
    }

    if(settings['deviceType'] == 'lock') return 'lock'
    if(settings['deviceType'] == 'light') return 'light'
    if(settings['deviceType'] == 'switch') return 'switch'
    if(settings['deviceType'] == 'fan') return 'fan'
}

def getOnAtOpen(){
    if(settings['open_action'] == 'off') return false
    if(settings['open_action'] == 'resume') return false
    return true
}

def getOnAtClose(){
    if(settings['closed_action'] == 'off') return false
    if(settings['closed_action'] == 'resume') return false
    return true
}

def validateOpenWait(){
    return validateWait(open_wait)
}

def validateCloseWait(){
    return validateWait(closed_wait)
}

def validateWait(time){
        if(!time) return true
        if(time > 7200) return false
    return true
}

def validateOpenLevel(){
    return parent.validateLevel(open_level)
}

def validateCloseLevel(){
    return parent.validateLevel(closed_level)
}

def validateOpenTemp(){
    return parent.validateTemp(open_temp)
}

def validateCloseTemp(){
    return parent.validateTemp(closed_temp)
}

def validateHue(value){
    return parent.validateHue(value)
}

def validateSat(value){
    return parent.validateSat(value)
}

def validateSunMinutes(time){
        if(!time) return true
        if(time > 719) return false
    return true
}

def displayNameOption(){
    if(app.label){
        displayLabel('Contact/door name',2)
        label title: '', required: false, width: 10,submitOnChange:true
    } else {
        displayLabel('Set name for this contact/door')
        label title: '', required: false, submitOnChange:true
        displayInfo('Name this contact/door sensor app. Each contact/door sensor app must have a unique name.')
    }
}

def displayDevicesOption(){
    if(settings['sensor']){
        text = pluralContact.capitalize()
        input 'sensor', 'capability.contactSensor', title: "$text:", multiple: true, submitOnChange:true
        return
    }
    input 'sensor', 'capability.contactSensor', title: "Select $pluralContact:", multiple: true, submitOnChange:true
    displayInfo("Select which $pluralContact for which to set actions.")
}

def displayDevicesTypes(){
    if(!settings['sensor']) return
        deviceText = 'device(s)'
    if(deviceCount == 1) deviceText = 'device'
    if(deviceCount > 1) deviceText = 'devices'
    inputTitle = "Type of $deviceText to control:"
    if(!settings['deviceType']) inputTitle = highlightText('Which type of device(s) to control (click to select one)?')
    input 'deviceType', 'enum', title: inputTitle, options: ['lock': 'Lock(s)','light': 'Light(s)', 'switch': 'Switch(es)', 'fan': 'Fan(s)'], multiple: false, required: false, submitOnChange:true
    if(!settings['deviceType']) displayInfo('Light(s) allows selecting dimmable switches. Switch(es) include all lights (and fans).')
}

def displayOpenCloseDevicesOption(){
    if(!settings['sensor']) return
    if(!settings['deviceType']) return

    if(settings['deviceType'] == 'lock'){
        capability = 'capability.lock'
    } else if(settings['deviceType'] == 'light'){
        capability = 'capability.switchLevel'
    } else if(settings['deviceType'] == 'switch'){
        capability = 'capability.switch'
    } else if(settings['deviceType'] == 'fan'){
        capability = 'capability.fanControl'
    }

    inputTitle = pluralDevice.capitalize() + ' to control (click to select)?'
    if(settings['device']) inputTitle = pluralDevice.capitalize() + ' being controlled:'
    input 'device', "$capability", title: inputTitle, multiple: true, submitOnChange:true
}

def displayDisableOption(){
    if(settings['disable']){
        input 'disable', 'bool', title: "<b><font color=\"#000099\">This $singleContact app is disabled.</font></b> Reenable it?", submitOnChange:true
        return
    }
    input 'disable', 'bool', title: "This $singleContact app is enabled. Disable it?", submitOnChange:true
}

def displayOpenOption(){
    if(!settings["device"]) return

    hideable = true
    hidden = true
    if(!settings["open_action"]) hideable = false
    
    sectionTitle = ''
    if(settings["open_action"]){
        if(settings["open_wait"] && settings["open_wait"] > 0) {
            sectionTitle = "<b>When opened: " + plainOpenAction.capitalize() + " after " + settings["open_wait"] + " seconds</b>"
        } else if(settings["open_action"] != "none"){
            sectionTitle = "<b>When opened: " + plainOpenAction.capitalize() + "</b>" + moreOptions
        } else {
            sectionTitle = "<b>When opened: " + plainOpenAction.capitalize() + "</b>"
        }
    }
    if(!validateOpenWait()) hidden = false
    
        if(settings['open_action']) inputTitle = 'When opened:'
    if(!settings['open_action'] && settings['deviceType'] == 'lock') inputTitle = 'When opened, lock or unlock (click to select)?'
    if(!settings['open_action'] && settings['deviceType'] != 'lock') inputTitle = "When opened, do what with the $pluralDevice (click to select)?"
    if(!settings['open_action']) inputTitle = highlightText(inputTitle)

    section(hideable: hideable, hidden: hidden, sectionTitle){
        if(!validateOpenWait()) displayWarning("Open wait time is " + Math.round(settings["open_wait"] / 3600) + " hours (" + settings["open_wait"] + " seconds). That's probably wrong.")
        if(settings["deviceType"] == "lock"){
            input "open_action", "enum", title: inputTitle, multiple: false, width: 12, options: ["none": "Don't lock or unlock","lock": "Lock", "unlock": "Unlock"], submitOnChange:true
        } else {
            input "open_action", "enum", title: inputTitle, multiple: false, width: 12, options: ["none": "Don't turn on or off (leave as is)","on": "Turn On", "off": "Turn Off", "toggle": "Toggle", "resume":"Resume Schedule (or turn off)"], submitOnChange:true
            if(settings["open_action"]){
                if(deviceCount > 1) message = "Set whether to turn on, turn off, toggle, or resume schedule when $contactIndefiniteArticle $singleContact is opened. Select \"Don't\" to do nothing when opened. Required."
                if(deviceCount <= 1) message = "Set whether to turn on, turn off, toggle, or resume schedule when $contactIndefiniteArticle $singleContact is opened. Select \"Don't\" to do nothing when opened. Required."
            } else {
                if(deviceCount > 1) message = "Set whether to turn on, turn off, toggle, or resume schedule when $contactIndefiniteArticle $singleContact is opened. Select \"Don't\" to control other options (like setting Mode), or to do nothing when opened. Toggle will change the $pluralDevice from off to on and vice versa. Resume schedule will restart any schedule(s) for the $pluralDevice; if there are no active schedules, the $pluralDevice will turn off. Required field."
                if(deviceCount <= 1) message = "Set whether to turn on, turn off, toggle, or resume schedule when $contactIndefiniteArticle $singleContact is opened. Select \"Don't\" to control other options (like setting Mode), or to do nothing when opened. Toggle will change the $pluralDevice from off to on and vice versa. Resume schedule will restart any schedule(s) for the $pluralDevice; if there are no active schedules, the $pluralDevice will turn off. Required field."
            }
            displayInfo(message)
        }

        if(settings["open_action"] && settings["open_action"] != "none"){
            inputTitle = "Seconds after open to $plainOpenAction $pluralDevice? (Optional.)"
            if(settings['open_wait']) inputTitle = "Seconds after open to $plainOpenAction $pluralDevice:"
            input "open_wait", "number", title: inputTitle, defaultValue: false, submitOnChange:true

            if(settings["open_wait"]) message = "If $contactIndefiniteArticleExtended $pluralContact is closed within " + settings['open_wait'] + " seconds, the $pluralDevice will not $plainOpenAction. Instead, it will only $plainCloseAction with being closed."
            if(!settings["open_wait"]) message = "If $contactIndefiniteArticleExtended $pluralContact is closed before time expires, the $pluralDevice will not $plainOpenAction. Instead, it will only $plainCloseAction with being closed."

            if(validateOpenWait()) {
                displayInfo(message)
            } else {
                displayWarning(message)
            }
        }
    }
}

def displayCloseOption(){
    //Put in notice that close will run even if disabled
    if(!settings['open_action']) return

    hidden = true
        hideable = true
    if(!validateCloseWait()) hidden = false
    if(!settings['closed_action']) hideable = false
    
    sectionTitle = ''
    if(settings['closed_action']){
        if(settings['closed_wait'] && settings['closed_wait'] > 0) {
            sectionTitle = "<b>When closed: " + plainCloseAction.capitalize() + " after " + settings['closed_wait'] + " seconds</b>"
        } else if(settings['closed_action'] != 'none'){
            sectionTitle = "<b>When closed: " + plainCloseAction.capitalize() + "</b>$moreOptions"
        } else {
            sectionTitle = "<b>When closed: " + plainCloseAction.capitalize() + "</b>"
        }
    }
    
    if(settings['closed_action']) inputTitle = 'When closed:'
    if(!settings['closed_action'] && settings['deviceType'] == 'lock') inputTitle = 'When closed, lock or unlock (click to select)?'
    if(!settings['closed_action'] && settings['deviceType'] != 'lock') inputTitle = "When closed, do what with the $pluralDevice (click to select)?"
    if(!settings['closed_action']) inputTitle = highlightText(inputTitle)

    section(hideable: hideable, hidden: hidden, sectionTitle){
        if(!validateCloseWait()) displayWarning("Close wait time is " + Math.round(settings['closed_wait'] / 3600) + " hours (" + settings['closed_wait'] + " seconds). That's probably wrong.")
        if(settings['deviceType'] == 'lock'){
            input 'closed_action', 'enum', title: inputTitle, multiple: false, width: 12, options: ['none': 'Don\'t lock or unlock','lock': 'Lock', 'unlock': 'Unlock'], submitOnChange:true
        } else {
            input 'closed_action', 'enum', title: inputTitle, multiple: false, width: 12, options: ['none': 'Don\'t turn on or off (leave as is)','on': 'Turn On', 'off': 'Turn Off', 'toggle': 'Toggle', 'resume':'Resume Schedule (or turn off)'], submitOnChange:true
            if(deviceCount > 1) message = "Set whether to turn on, turn off, toggle, or resume schedule when $contactIndefiniteArticle $singleContact is closed. Select \"Don't\" to do nothing when closed. Required."
            if(deviceCount <= 1) message = "Set whether to turn on, turn off, toggle, or resume schedule when $contactIndefiniteArticle $singleContact is closed. Select \"Don't\" to do nothing when closed. Required."
            displayInfo(message)
        }
        
        if(settings['closed_action'] && settings['closed_action'] != 'none'){
            inputTitle = "Seconds after open to $plainOpenAction $pluralDevice? (Optional.)"
            if(settings['closed_wait']) inputTitle = "Seconds after open to $plainOpenAction $pluralDevice:"
            input "closed_wait", "number", title: inputTitle, defaultValue: false, submitOnChange:true

            if(settings['closed_wait']) message = "If $contactIndefiniteArticleExtended $pluralContact is opened within " + settings['closed_wait'] + " seconds, the $pluralDevice will not $plainCloseAction. Instead, it will only $plainOpenAction with being opened."
            if(!settings['closed_wait']) message = "If $contactIndefiniteArticleExtended $pluralContact is opened before time expires, the $pluralDevice will not $plainCloseAction. Instead, it will only $plainOpenAction with being opened."

            if(validateOpenWait()) {
                displayInfo(message)
            } else {
                displayWarning(message)
            }
        }
    }
}

def displayBrightnessOption(){
    if(!settings['closed_action']) return
    if(settings['deviceType'] != 'light') return
    if(!onAtOpen) return
    if(!onAtClose) return

    hidden = true
    if(!validateOpenLevel()) hidden = false
    if(!validateCloseLevel()) hidden = false
    if(settings['open_level'] && !settings['stop_level'] && !settings['stop_timeType']) hidden = false
    if(settings['stop_level'] && !settings['open_level']) hidden = false
    if(settings['open_level'] && settings['open_level'] == settings['stop_level']) hidden = false

    width = 12
    if(onAtOpen && onAtClose) width = 6

    sectionTitle = ''
    if(!settings['open_level'] && !settings['closed_level']) sectionTitle = 'Click to set brightness (optional)'
    if(settings['open_level'] && (!settings['closed_level'] || settings['open_level'] == settings['closed_level'])) sectionTitle = '<b>On open, set brightness to ' + settings['open_level'] + '%</b>'
    if(settings['closed_level'] && !settings['open_level']) sectionTitle += '<br>'
    if(settings['closed_level'] && !settings['open_level']) sectionTitle += '<b>On close, set brightness to ' + settings['closed_level'] + '%</b>'
 
    section(hideable: true, hidden: hidden, sectionTitle){
        if(!validateOpenLevel()) displayError('Open level must be from 1 to 100. Correct start brightness.')
        if(!validateCloseLevel()) displayError('Close level must be from 1 to 100. Correct stop brightness.')

        inputTitle = 'Set brightness on open?'
        if(settings['open_level']) inputTitle = 'Set brightness on open:'
        if(onAtOpen) input 'open_level', 'number', title: inputTitle, width: width, submitOnChange:true
        inputTitle = 'Set brightness on close?'
        if(settings['closed_level']) inputTitle = 'Set brightness on open:'
        if(settings['stop_timeType'] &&  onAtClose) input 'closed_level', 'number', title: inputTitle, width: width, submitOnChange:true

        message = 'Brightness is percentage from 1 to 100.'
        if(!validateOpenLevel() || !validateCloseLevel()) {
            displayError(message)
        } else {
            displayInfo(message)
        }
    }
}

def displayTemperatureOption(){
    if(!settings['closed_action']) return
    if(settings['deviceType'] != 'light') return
    if(!onAtOpen) return
    if(!onAtClose) return

    hidden = true
    if(!validateOpenTemp()) hidden = false
    if(!validateCloseTemp()) hidden = false
    if(settings['open_temp'] && !settings['closed_temp'] && !settings['stop_timeType']) hidden = false
    if(settings['closed_temp'] && !settings['open_temp']) hidden = false
    if(settings['open_temp'] && settings['open_temp'] == settings['closed_temp']) hidden = false
    if(settings['open_temp'] && (settings['open_hue'] || settings['open_sat'])){
        hidden = false
        colorWarning = true
    }
    if(settings['closed_temp'] && (settings['closed_hue'] || settings['closed_sat'])){
        hidden = false
        colorWarning = true
    }

    width = 12
    if(onAtOpen && onAtClose) width = 6

    sectionTitle = ''
    if(!settings['open_temp'] && !settings['closed_level']) sectionTitle = 'Click to set temperature color (optional)'
    if(settings['open_temp'] && (!settings['closed_level'] || settings['closed_temp'] == settings['closed_level'])) sectionTitle = '<b>On open, set temperature color to ' + settings['open_temp'] + 'K</b>'
    if(settings['closed_temp'] && !settings['open_temp']) sectionTitle += '<br>'
    if(settings['closed_temp'] && !settings['open_temp']) sectionTitle += '<b>On close, set temperature color to ' + settings['closed_temp'] + 'K</b>'

    section(hideable: true, hidden: hidden, sectionTitle){
        if(!validateOpenTemp()) displayError('Start temperature color must be from 1800 to 5400. Correct start temperature color.')
        if(!validateCloseTemp()) displayError('Stop temperature color must be from 1800 to 5400. Correct stop temperature color.')
        if(colorWarning) displayWarning('Color options have been entered that conflict with temperature color. Color will take precedence over temperature color.')

        inputTitle = 'Set temperature color on open?'
        if(settings['open_temp']) inputTitle = 'Set temperature color on open:'
        input 'open_temp', 'number', title: inputTitle, width: width, submitOnChange:true
        inputTitle = 'Set temperature color on close?'
        if(settings['closed_temp']) inputTitle = 'Set temperature color on open:'
        if(settings['stop_timeType'] &&  settings['closed_action'] != 'off') input 'closed_temp', 'number', title: inputTitle, width: width, submitOnChange:true

        message = 'Temperature color is from 1800 to 5400, where daylight is 5000, cool white is 4000, and warm white is 3000.'
        if(!settings['open_temp'] && !settings['closed_temp']) message = 'Temperature color is in Kelvin from 1800 to 5400. Lower values have more red, while higher values have more blue, where daylight is 5000, cool white is 4000, and warm white is 3000. (Optional.)'
        if(!validateOpenTemp() || !validateCloseTemp()) {
            displayError(message)
        } else {
            displayInfo(message)
        }
    }
}

def displayColorOption(){
    if(!settings['closed_action']) return
    if(settings['deviceType'] != 'light') return
    if(!onAtOpen) return
    if(!onAtClose) return
    
    unit = hiRezHue ? '°' : '%'

    validateOpenHue = validateHue(settings['open_hue'])
    validateCloseHue = validateHue(settings['closed_hue'])
    validateOpenSat = validateSat(settings['open_sat'])
    validateCloseSat = validateSat(settings['closed_sat'])
    
    hidden = true
    if(!validateOpenHue) hidden = false
    if(!validateCloseHue) hidden = false
    if(!validateOpenSat) hidden = false
    if(!validateCloseSat) hidden = false
    if(settings['open_hue'] && !settings['closed_hue'] && onAtClose) hidden = false
    if(!settings['open_hue'] && settings['closed_hue'] && onAtClose) hidden = false
    if(settings['open_sat'] && !settings['closed_sat'] && onAtOpen) hidden = false
    if(!settings['open_sat'] && settings['closed_sat'] && onAtOpen) hidden = false
    
    width = 12
    if(onAtOpen && onAtClose) width = 6

    sectionTitle = ''
    if(!settings['open_hue'] && !settings['closed_hue'] && !settings['open_sat'] && !settings['closed_sat']){
        sectionTitle = 'Click to set color (hue and/or saturation) (optional)'
    } else {
        if(settings['open_hue'] && settings['open_sat']) sectionTitle = "<b>On open, set hue to " + settings['open_hue'] + "$unit and sat to " + settings['open_sat'] + "%</b>"
        if(settings['open_hue'] && !settings['open_sat']) sectionTitle = "<b>On open, set hue to " + settings['open_hue'] + "$unit</b>"
        if(!settings['open_hue'] && settings['open_sat']) sectionTitle = "<b>On open, set saturation " + settings['open_sat'] + "%</b>"
        
        if(!settings['open_hue'] || !settings['closed_hue'] || !settings['open_sat'] || !settings['closed_sat']) sectionTitle += moreOptions
        
        if((settings['open_hue'] || settings['open_sat']) && (settings['closed_hue'] || settings['closed_sat'])) sectionTitle += '<br>'
        
        if(settings['closed_hue'] && settings['closed_sat']) sectionTitle += "<b>On close, set hue to " + settings["closed_hue"] + "$unit and sat to " + settings["closed_sat"] + "%</b>"
        if(settings['closed_hue'] && !settings['closed_sat']) sectionTitle += "<b>On close, set hue to " + settings["closed_hue"] + "$unit</b>"
        if(!settings['closed_hue'] && settings['closed_sat']) sectionTitle += "<b>On close, set saturation " + settings["closed_sat"] + "%</b>"
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        if(!validateOpenHue && hiRezHue) displayError('Open hue must be from 1 to 360. Correct open hue.')
        if(!validateOpenHue && !hiRezHue) displayError('Open hue must be from 1 to 100. Correct open hue.')
        if(!validateCloseHue && hiRezHue) displayError('Close hue must be from 1 to 360. Correct close hue.')
        if(!validateCloseHue && !hiRezHue) displayError('Close hue must be from 1 to 100. Correct close hue.')
        if(!validateOpenSat && hiRezHue) displayError('Open saturation must be from 1 to 360. Correct open saturation.')
        if(!validateOpenSat && !hiRezHue) displayError('Open saturation must be from 1 to 100. Correct open saturation.')
        if(!validateCloseSat && hiRezHue) displayError('Close saturation must be from 1 to 360. Correct close saturation.')
        if(!validateCloseSat && !hiRezHue) displayError('Close saturation must be from 1 to 100. Correct close saturation.')
        if(!validateCloseTemp()) displayError('Close temperature color must be from 1800 to 6500. Correct close temperature.')
        
        if(onAtOpen) input 'open_hue', 'number', title: 'Set hue on open?', width: width, submitOnChange:true
        if(onAtClose) input 'closed_hue', 'number', title: 'Set hue on close?', width: width, submitOnChange:true

        message = 'Hue is percent around a color wheel, where red is 0 (and 100). Orange = 8; yellow = 16; green = 33; turquiose = 50; blue = 66; purple = 79 (may vary by device). Optional.'
        if(hiRezHue) message = 'Hue is degrees around a color wheel, where red is 0 (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270 (may vary by device). Optional.'
        if(validateOpenHue && validateCloseHue) {
            displayInfo(message)
        } else {
            displayError(message)
        }
        
        if(onAtOpen) input 'open_sat', 'number', title: 'Set saturation on open?', width: width, submitOnChange:true
        if(onAtClose) input 'closed_sat', 'number', title: 'Set saturation on close?', width: width, submitOnChange:true

        message = 'Saturation is the percentage depth of color, from 1 to 100, where 1 is hardly any color tint and 100 is full color. Optional.'
        if(validateOpenSat && validateCloseSat) {
            displayInfo(message)
        } else {
            displayError(message)
        }
    }
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

def displayChangeModeOption(){
    if(!settings['closed_action']) return

    hidden = true
    if((settings['open_mode'] || settings['closed_mode']) && (!settings['open_mode'] || !settings['closed_mode'])) hidden = false

    
    sectionTitle = ''
    if(!settings['open_mode'] && !settings['closed_mode']){
        sectionTitle = 'Click to set Mode change (optional)'
    } else {
        if(settings['open_mode']) sectionTitle = '<b>On open, set Mode ' + settings['open_mode'] + '</b>'
        if(settings['open_mode'] && settings['closed_mode']) sectionTitle += '<br>'
        if(settings['closed_mode']) sectionTitle += '<b>On close, set Mode ' + settings['closed_mode'] + '</b>'
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        input 'open_mode', 'mode', title: 'Set Hubitat\'s "Mode" on open?', width: 6, submitOnChange:true
        input 'closed_mode', 'mode', title: 'Set Hubitat\'s "Mode" on close?', width: 6, submitOnChange:true
    }
}

def displayIfModeOption(){
    if(!settings['closed_action']) return

    sectionTitle = 'Click to select with what Mode (optional)'
    if(settings['ifMode']) sectionTitle = '<b>Only with Mode: ' + settings['ifMode'] + '</b>'

    section(hideable: true, hidden: true, sectionTitle){
        input 'ifMode', 'mode', title: 'Only run if Mode is already?', width: 12, submitOnChange:true

        message = "This will limit the $pluralContact from running to only when Hubitat\'s Mode is as selected."
        if(settings['ifMode']) message = "This will limit the $pluralContact from running to only when Hubitat\'s Mode is " + settings['ifMode'] + "."

        displayInfo(message)
    }
}

def displayAlertOptions(){
    if(!settings['closed_action']) return
    if(!parent.pushNotificationDevice && !parent.speechDevice) return

    hidden = true
    if((settings['pushNotification'] || settings['speech']) && !settings['notificationOpenClose']) hidden = false

    // Get push notification device(s) from parent (if applicable)
    if(parent.pushNotificationDevice){
        state.pushFilteredList = [:]
        countPushDevices = 0
        parent.pushNotificationDevice.each{
            pushDeviceName = "${it.label ?: it.name}"
            pushDeviceId = it.id
            state.pushFilteredList[pushDeviceId] = pushDeviceName
            countPushDevices++
                }
        if(countPushDevices == 1) {
            settings['pushNotificationDevice'] = [:]
            settings['pushNotificationDevice'][pushDeviceName] = pushDeviceId
        }
    }

    // Get speech device(s) from parent (if applicable)
    if(parent.speechDevice){
        state.speechFilteredList = [:]
        countSpeechDevices = 0
        parent.speechDevice.each{
            speechDeviceName = "${it.label ?: it.name}"
            speechDeviceId = it.id
            state.speechFilteredList[speechDeviceId] = speechDeviceName
            countSpeechDevices++
                }
        if(countSpeechDevices == 1) {
            settings['speechDevice'] = [:]
            settings['speechDevice'][speechDeviceName] = speechDeviceId
        }
    }
    
    if(settings['notificationOpenClose']) sectionTitle = '<b>On ' + settings['notificationOpenClose'] + ', '
    if(settings['notificationOpenClose'] == 'both') sectionTitle = '<b>On open and close, '

    if(settings['pushNotification'] && settings['speech']) sectionTitle += "send notification and speak text</b>"
    if(settings['pushNotification'] && !settings['speech']) sectionTitle += "send notification</b>"
    if(!settings['pushNotification'] && settings['speech']) sectionTitle += "speak text</b>"
    if(!settings['notificationOpenClose']) sectionTitle = '<b>' + sectionTitle.capitalize()
    if(!settings['pushNotification'] || !settings['speech']) sectionTitle += moreOptions
    
    if(!settings['speech'] && !settings['pushNotification']) sectionTitle = 'Click to send notifications (optional)'

    section(hideable: true, hidden: hidden, sectionTitle){
        if(parent.pushNotificationDevice){
            if(countPushDevices > 1) input 'pushNotificationDevice', 'enum', title: 'Push notification device(s)?', options: state.pushFilteredList, multiple: true, submitOnChange: true

            input 'pushNotification', 'text', title: 'Text of push notification to send?', submitOnChange:true
            if(countPushDevices == 1) displayInfo("Push notifications will use the device \"$pushDeviceName\". To use other(s), add it in the Master app.")
        }

        if(parent.speechDevice){
            if(countSpeechDevices > 1) input 'speechDevice', 'enum', title: 'Text-to-speech device to use', options: state.speechFilteredList, multiple: true, submitOnChange: true

            input 'speech', 'text', title: 'Text-to-speech announcement?', submitOnChange:true
            if(countSpeechDevices == 1) displayInfo("Text-to-speech will use the device \"$speechDeviceName\". To use other(s), add it in the Master app.")
        }
        if(settings['pushNotification'] && settings['speech']) action = 'Notice and speak'
        if(settings['pushNotification'] && !settings['speech']) action = 'Push notice'
        if(!settings['pushNotification'] && settings['speech']) action = 'Speak'

        sectionTitle = "$action on open or close? (Required)"
        
        if((settings['pushNotificationDevice'] && settings['pushNotification']) || (settings['speechDevice'] && settings['speech'])) input 'notificationOpenClose', 'enum', title: sectionTitle, multiple: false, width: 12, options: ['open': 'Open', 'closed': 'Close','both': 'Both open and close'], submitOnChange:true
    }
}

def displayPeopleOption(){
    if(!settings['closed_action']) return

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
    if(settings['personHome']) sectionTitle = "<b>With: $withPeople</b>"
    if(settings['personHome'] && settings['personNotHome']) sectionTitle += '<br>'
    if(settings['personNotHome']) sectionTitle += "<b>Without: $withoutPeople</b>"

    section(hideable: true, hidden: hidden, sectionTitle){
        if(peopleError) displayError('You can\'t include and exclude the same person.')

        input 'personHome', 'capability.presenceSensor', title: 'Only if any of these people are home (Optional)', multiple: true, submitOnChange:true
        input 'personNotHome', 'capability.presenceSensor', title: 'Only if all these people are NOT home (Optional)', multiple: true, submitOnChange:true
    }
}

def getPluralIndefiniteArticle(pluralCount, extended = false){
    if(!pluralCount) return ''
    if(pluralCount == 1) return 'the'
    if(pluralCount > 1 && !extended) return 'a'
    if(pluralCount > 1 && extended) return 'one of the'
}

def getPlainAction(action){
    if(!action) return 'perform action'
    if(action == 'none') return 'do nothing'
    if(action == 'on') return 'turn on'
    if(action == 'off') return 'turn off'
    if(action == 'toggle') return 'toggle'
    if(action == 'resume') return 'resume schedule'
    if(action == 'lock') return 'lock'
    if(action == 'unlock') return 'unlock'
}

def compareDeviceLists(list1,list2){
   list1.each{first->
        list2.each{second->
            if(first.id == second.id) returnValue = true
        }
    }
    return returnValue
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
    state.logLevel = getLogLevel()
    putLog(1003,'trace','Installed')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    state.logLevel = getLogLevel()
    putLog(1010,'trace','Updated')
    unsubscribe()
    initialize()
}

def initialize() {
    state.logLevel = getLogLevel()
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    atomicState.start = null
    atomicState.stop = null
    setTime()
    unschedule()

    if(settings['disable']) state.disable = true
    if(!settings['disable']) state.disable = false

    if(getDisabled()) return
    
    // If date/time for last notification not set, initialize it to 5 minutes ago
    if(!state.contactLastNotification) state.contactLastNotification = new Date().getTime() - 360000

    subscribe(pico, 'pushed', contactChange)
    subscribe(sensor, 'contact.open', contactChange)
    subscribe(sensor, 'contact.closed', contactChange)            
    
    putLog(1035,'trace','Initialized')
}

def contactChange(evt){
    setTime()
    if(getDisabled()) return
    putLog(1041,'debug','Contact sensor ' + evt.displayName + ' ' + evt.value)
    

    // If opened a second time, it will reset delayed action
    // If closed a second time, it won't override open
    if(evt.value == 'open') unschedule()
    if(evt.value != 'open') unschedule(runScheduleClosed)

    // Schedule delay
    if(settings[evt.value + '_wait']){
        if(evt.value == 'open') functionName = 'runScheduleOpen'
        if(evt.value == 'closed') functionName = 'runScheduleClosed'
        timeMillis = settings[evt.value + '_wait'] * 1000
        parent.scheduleChildEvent(timeMillis,'',functionName,'',app.id)
    }

    if(!settings[evt.value + '_wait']) performUpdates(evt.value)

    // Set mode
    if(settings[evt.value + '_mode']) parent.changeMode(settings[evt.value + '_mode'],app.label)
}

def performUpdates(action){
    if(settings['deviceType'] == 'lock') {
        parent.setLockMulti(settings['device'],settings[action + '_action'],app.label)
        return
    }
    settings['device'].each{singleDevice->
        stateMap = parent.getStateMapSingle(singleDevice,settings[action + '_action'],app.id,app.label)       // on, off, toggle
        parent.mergeMapToTableWithPreserve(singleDevice,stateMap,app.label)

        levelMap = parent.getLevelMap(type,settings[action + '_brightness'],app.id,childLabel)
        parent.mergeMapToTableWithPreserve(singleDevice,levelMap,app.label)
        levelMap = parent.getLevelMap(type,settings[action + '_temp'],app.id,childLabel)
        parent.mergeMapToTableWithPreserve(singleDevice,levelMap,app.label)
        levelMap = parent.getLevelMap(type,settings[action + '_hue'],app.id,childLabel)
        parent.mergeMapToTableWithPreserve(singleDevice,levelMap,app.label)
        levelMap = parent.getLevelMap(type,settings[action + '_sat'],app.id,childLabel)
        parent.mergeMapToTableWithPreserve(singleDevice,levelMap,app.label)
    }

    if(settings[action + '_action'] == 'resume') parent.resumeDeviceScheduleMulti(settings['device'],app.label)

    parent.setDeviceMulti(settings['device'],app.label)
    
    sendPushNotification(action)
    sendVoiceAlert(action)
    if(settings[action + '_mode']) parent.changeMode(settings[action + '_mode'],app.label)
}

def sendPushNotification(action){
    if(!settings['pushNotificationDevice']) return
    if(settings['notificationOpenClose'] != 'both' && (settings['notificationOpenClose'] != 'open' ||action != 'open')) return
    if(settings['notificationOpenClose'] != 'both' && (settings['notificationOpenClose'] != 'closed' || action != 'closed')) return

    eventName = action
    if(action == 'open') eventName = 'opened'

    parent.sendPushNotification(settings['pushNotificationDevice'],evt.displayName + ' was ' + eventName + ' at ' + now.format('h:mm a', location.timeZone),app.label)
}

def sendVoiceAlert(action){
    if(!settings['speech']) return
    if(settings['notificationOpenClose'] != 'both' && (settings['notificationOpenClose'] != 'open' || action != 'open')) return
    if(settings['notificationOpenClose'] != 'both' && (settings['notificationOpenClose'] != 'closed' ||action != 'closed')) return

    parent.sendVoiceNotification(settings['speechDevice'],settings['speech'],app.label)
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
    putLog(1123,'info','Start time set to ' + parent.getPrintDateTimeFormat(setTime))
    return true
}

def setStopTime(){
    if(!settings['stop_timeType'] || settings['stop_timeType'] == 'none') return
    if(atomicState.stop > atomicState.start) return
    setTime = setStartStopTime('stop')
    if(setTime < atomicState.start) setTime += parent.CONSTDayInMilli()
    atomicState.stop  = setTime
    putLog(1133,'info','Stop time set to ' + parent.getPrintDateTimeFormat(setTime))
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

// Need for if descheduling Closed
def runScheduleOpen(){
    if(getDisabled()) return
    performUpdates('open')
}
def runScheduleClosed(){
    performUpdates('closed')
}

// Called from parent.scheduleChildEvent
def setScheduleFromParent(timeMillis,scheduleFunction,scheduleParameters = null){
    runInMillis(timeMillis,scheduleFunction,scheduleParameters)
}

def getDisabled(){
    // If disabled, return true
    if(state.disable) return true
    // If mode isn't correct, return false
    if(settings['ifMode'] && location.mode != settings['ifMode']) return true
    if(!parent.checkNowBetweenTimes(atomicState.start, atomicState.stop, app.label)) return true
    // days and months
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
