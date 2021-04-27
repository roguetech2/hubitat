/***********************************************************************************************************************
*
*  Copyright (C) 2021 roguetech
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
*  Version: 0.6.09
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

/* ************************************************************************ */
/* TO-DO: Change icon from lock to something  that resembles a door I guess.*/
/* ************************************************************************ */

// logLevel sets number of log messages
// 0 for none
// 1 for errors only
// 5 for all
def getLogLevel(){
    return 5
}

preferences {
    infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
    errorIcon = "<img src=\"http://emily-john.love/icons/error.png\" width=20 height=20>"
    warningIcon = "<img src=\"http://emily-john.love/icons/warning.png\" width=20 height=20>"
    moreOptions = " <font color=\"grey\">(click for more options)</font>"

    install = formComplete()

    page(name: "setup", install: install, uninstall: true) {
        // display Name
        if(!app.label){
            section(){
                displayNameOption()
            }
        } else {
            if(!settings) settings = [:]
            
            // Clear settings with pre-requisites
            if(!settings['contactDevice']) settings['deviceType'] = null
            if(!settings['deviceType']) settings['device'] = null
            if(!settings['device']) settings['open_action'] = null
            if(!settings['open_action']) {
                settings['open_wait'] = null
                settings['close_action'] = null
                settings['open_level'] = null
                settings['open_temp'] = null
                settings['open_hue'] = null
                settings['open_sat'] = null
                settings['start_timeType'] = null
            }
            if(!settings['close_action']) {
                settings['close_wait'] = null
                settings['close_level'] = null
                settings['close_temp'] = null
                settings['close_hue'] = null
                settings['close_sat'] = null
                settings['start_timeType'] = null
            }
           if(!settings['start_timeType']) {
                settings['start_sunType'] = null
                settings['stop_timeType'] = null
            }
            if(settings['start_timeType'] != 'time') settings['start_time'] = null
            if(settings['start_timeType'] == 'time') settings['start_sunType'] = null
            if(settings['start_sunType'] != 'before' && settings['start_sunType'] != 'after') settings['start_sunOffset'] = null
            if(settings['start_sunType'] == 'at') settings['start_sunOffset'] = null
            if(!settings['start_timeType']) {
                settings['start_time'] = null
                settings['start_sunType'] = null
                settings['start_sunOffset'] = null
            }
            startTimeComplete = checkTimeComplete('start')
            if(!startTimeComplete) settings['stop_timeType'] = null
            if(!settings['stop_timeType']) settings['stop_sunType'] = null
            if(settings['stop_timeType'] != 'time') settings['stop_time'] = null
            if(settings['stop_timeType'] == 'time') settings['stop_sunType'] = null
            if(settings['stop_sunType'] != 'before' && settings['stop_sunType'] != 'after') settings['stop_sunOffset'] = null
            if(settings['stop_sunType'] == 'at') settings['stop_sunOffset'] = null
            if(!settings['stop_timeType']) {
                settings['stop_time'] = null
                settings['stop_sunType'] = null
                settings['stop_sunOffset'] = null
            }
            if(!settings['open_hue']) settings['hueDirection'] = null
            if(!settings['close_hue']) settings['hueDirection'] = null
            if(!settings['pushNotification'] && !settings['speech']) settings['notificationOpenClose'] = null
            
            stopTimeComplete = checkTimeComplete('stop')
            
            // Set variables
            singleContact = "contact/door sensor"
            contactCount = getDeviceCount(contactDevice)
            deviceCount = getDeviceCount(device)
            pluralContact = getContactSensorPlural()
            contactIndefiniteArticle = getPluralIndefiniteArticle(contactCount)
            contactIndefiniteArticleExtended =  getPluralIndefiniteArticle(contactCount,true)
            deviceIndefiniteArticle = getPluralIndefiniteArticle(deviceCount)
            deviceIndefiniteArticleExtended =  getPluralIndefiniteArticle(deviceCount,true)
            pluralDevice = getDevicePlural()
            singleDevice = settings["deviceType"]
            plainOpenAction = getPlainAction(settings["open_action"])
            plainCloseAction = getPlainAction(settings["close_action"])
            onAtOpen = getOnAtOpen()
            onAtClose = getOnAtClose()
            hiRezHue = parent.getHiRezHue()
            peopleError = compareDeviceLists(personHome,personNotHome)

            section(){
                displayNameOption()
                displayDevicesOption()
                displayDevicesTypes()
                displayOpenCloseDevicesOption()
                if(install) displayDisableOption()
            }

            displayOpenOption()
            displayCloseOption()
            displayBrightnessOption()
            if((!settings["openhue"] && !settings["open_sat"]) || (!settings["close_hue"] && !settings["close_sat"])) displayTemperatureOption()
            if(!settings["open_temp"] || !settings["close_temp"]) displayColorOption()
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
    if(!contactDevice) return false
    if(!deviceType) return false
    if(!device) return false
    if(!open_action) return false
    if(!close_action) return false
    if(start_timeType == "time" && !start_time) return false
    if(stop_timeType == "time" && !stop_time) return false
    if((start_timeType == "sunrise" || start_timeType == "sunset") && !start_sunType) return false
    if((stop_timeType == "sunrise" || stop_timeType == "sunset") && !stop_sunType) return false
    if((start_sunType == "before" || start_sunType == "after") && !start_sunOffset) return false
    if((stop_sunType == "before" || stop_sunType == "after") && !stop_sunOffset) return false
    if(start_timeType && !stop_timeType) return false
    if(!start_timeType && stop_timeType) return false
    if(!validateOpenLevel()) return false
    if(!validateCloseLevel()) return false
    if(!validateOpenTemp()) return false
    if(!validateCloseTemp()) return false
    if(!validateHue(open_hue)) return false
    if(!validateHue(close_hue)) return false
    if(!validateSat(open_sat)) return false
    if(speechDevice && !speechOpenAction == 'none') return false
    if(pushNotificationDevice && !pushNotification) return false
    if((settings['pushNotification'] || settings['speech']) && !settings['notificationOpenClose']) return false
    if((open_action == 'none' && close_action == 'none' && !open_level && !close_level && !open_temp && !close_temp && !open_sat && !close_sat && !open_hue && !close_hue && !open_mode && !close_mode && !pushNotification) && !speech) return false
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
    if(settings['close_action'] == 'off') return false
    if(settings['close_action'] == 'resume') return false
    return true
}

def validateOpenWait(){
    return validateWait(open_wait)
}

def validateCloseWait(){
    return validateWait(close_wait)
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
    return parent.validateLevel(close_level)
}

def validateOpenTemp(){
    return parent.validateTemp(open_temp)
}

def validateCloseTemp(){
    return parent.validateTemp(close_temp)
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

def displayLabel(text, width = 12){
    paragraph("<div style=\"background-color:#DCDCDC\"><b> $text:</b></div>",width:width)
}

def displayInfo(text,noDisplayIcon = null){
    paragraph "<div style=\"background-color:AliceBlue\">$infoIcon $text</div>"
}

def displayError(text){
    paragraph "<div style=\"background-color:Bisque\">$errorIcon $text</div>"
}

def displayWarning(text){
    paragraph "<div style=\"background-color:LemonChiffon\">$warningIcon $text</div>"
}

def highlightText(text){
    return "<div style=\"background-color:Wheat\">$text</div>"
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
    if(settings['contactDevice']){
        text = pluralContact.capitalize()
        input 'contactDevice', 'capability.contactSensor', title: "$text:", multiple: true, submitOnChange:true
        return
    }
    input 'contactDevice', 'capability.contactSensor', title: "Select $pluralContact:", multiple: true, submitOnChange:true
    displayInfo("Select which $pluralContact for which to set actions.")
}

def displayDevicesTypes(){
    if(!settings['contactDevice']) return
        deviceText = 'device(s)'
    if(deviceCount == 1) deviceText = 'device'
    if(deviceCount > 1) deviceText = 'devices'
    inputTitle = "Type of $deviceText to control:"
    if(!settings['deviceType']) inputTitle = highlightText('Which type of device(s) to control (click to select one)?')
    input 'deviceType', 'enum', title: inputTitle, options: ['lock': 'Lock(s)','light': 'Light(s)', 'switch': 'Switch(es)', 'fan': 'Fan(s)'], multiple: false, required: false, submitOnChange:true
    if(!settings['deviceType']) displayInfo('Light(s) allows selecting dimmable switches. Switch(es) include all lights (and fans).')
}

def displayOpenCloseDevicesOption(){
    if(!settings['contactDevice']) return
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
    if(!settings['open_action']) return

    hidden = true
        hideable = true
    if(!validateCloseWait()) hidden = false
    if(!settings['close_action']) hideable = false
    
    sectionTitle = ''
    if(settings['close_action']){
        if(settings['close_wait'] && settings['close_wait'] > 0) {
            sectionTitle = "<b>When closed: " + plainCloseAction.capitalize() + " after " + settings['close_wait'] + " seconds</b>"
        } else if(settings['close_action'] != 'none'){
            sectionTitle = "<b>When closed: " + plainCloseAction.capitalize() + "</b>$moreOptions"
        } else {
            sectionTitle = "<b>When closed: " + plainCloseAction.capitalize() + "</b>"
        }
    }
    
    if(settings['close_action']) inputTitle = 'When closed:'
    if(!settings['close_action'] && settings['deviceType'] == 'lock') inputTitle = 'When closed, lock or unlock (click to select)?'
    if(!settings['close_action'] && settings['deviceType'] != 'lock') inputTitle = "When closed, do what with the $pluralDevice (click to select)?"
    if(!settings['close_action']) inputTitle = highlightText(inputTitle)

    section(hideable: hideable, hidden: hidden, sectionTitle){
        if(!validateCloseWait()) displayWarning("Close wait time is " + Math.round(settings['close_wait'] / 3600) + " hours (" + settings['close_wait'] + " seconds). That's probably wrong.")
        if(settings['deviceType'] == 'lock'){
            input 'close_action', 'enum', title: inputTitle, multiple: false, width: 12, options: ['none': 'Don\'t lock or unlock','lock': 'Lock', 'unlock': 'Unlock'], submitOnChange:true
        } else {
            input 'close_action', 'enum', title: inputTitle, multiple: false, width: 12, options: ['none': 'Don\'t turn on or off (leave as is)','on': 'Turn On', 'off': 'Turn Off', 'toggle': 'Toggle', 'resume':'Resume Schedule (or turn off)'], submitOnChange:true
            if(deviceCount > 1) message = "Set whether to turn on, turn off, toggle, or resume schedule when $contactIndefiniteArticle $singleContact is closed. Select \"Don't\" to do nothing when closed. Required."
            if(deviceCount <= 1) message = "Set whether to turn on, turn off, toggle, or resume schedule when $contactIndefiniteArticle $singleContact is closed. Select \"Don't\" to do nothing when closed. Required."
            displayInfo(message)
        }
        
        if(settings['close_action'] && settings['close_action'] != 'none'){
            inputTitle = "Seconds after open to $plainOpenAction $pluralDevice? (Optional.)"
            if(settings['close_wait']) inputTitle = "Seconds after open to $plainOpenAction $pluralDevice:"
            input "close_wait", "number", title: inputTitle, defaultValue: false, submitOnChange:true

            if(settings['close_wait']) message = "If $contactIndefiniteArticleExtended $pluralContact is opened within " + settings['close_wait'] + " seconds, the $pluralDevice will not $plainCloseAction. Instead, it will only $plainOpenAction with being opened."
            if(!settings['close_wait']) message = "If $contactIndefiniteArticleExtended $pluralContact is opened before time expires, the $pluralDevice will not $plainCloseAction. Instead, it will only $plainOpenAction with being opened."

            if(validateOpenWait()) {
                displayInfo(message)
            } else {
                displayWarning(message)
            }
        }
    }
}

def displayBrightnessOption(){
    if(!settings['close_action']) return
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
    if(!settings['open_level'] && !settings['close_level']) sectionTitle = 'Click to set brightness (optional)'
    if(settings['open_level'] && (!settings['close_level'] || settings['open_level'] == settings['close_level'])) sectionTitle = '<b>On open, set brightness to ' + settings['open_level'] + '%</b>'
    if(settings['close_level'] && !settings['open_level']) sectionTitle += '<br>'
    if(settings['close_level'] && !settings['open_level']) sectionTitle += '<b>On close, set brightness to ' + settings['close_level'] + '%</b>'
 
    section(hideable: true, hidden: hidden, sectionTitle){
        if(!validateOpenLevel()) displayError('Open level must be from 1 to 100. Correct start brightness.')
        if(!validateCloseLevel()) displayError('Close level must be from 1 to 100. Correct stop brightness.')

        inputTitle = 'Set brightness on open?'
        if(settings['open_level']) inputTitle = 'Set brightness on open:'
        if(onAtOpen) input 'open_level', 'number', title: inputTitle, width: width, submitOnChange:true
        inputTitle = 'Set brightness on close?'
        if(settings['close_level']) inputTitle = 'Set brightness on open:'
        if(settings['stop_timeType'] &&  onAtClose) input 'close_level', 'number', title: inputTitle, width: width, submitOnChange:true

        message = 'Brightness is percentage from 1 to 100.'
        if(!validateOpenLevel() || !validateCloseLevel()) {
            displayError(message)
        } else {
            displayInfo(message)
        }
    }
}

def displayTemperatureOption(){
    if(!settings['close_action']) return
    if(settings['deviceType'] != 'light') return
    if(!onAtOpen) return
    if(!onAtClose) return

    hidden = true
    if(!validateOpenTemp()) hidden = false
    if(!validateCloseTemp()) hidden = false
    if(settings['open_temp'] && !settings['close_temp'] && !settings['stop_timeType']) hidden = false
    if(settings['close_temp'] && !settings['open_temp']) hidden = false
    if(settings['open_temp'] && settings['open_temp'] == settings['close_temp']) hidden = false
    if(settings['open_temp'] && (settings['open_hue'] || settings['open_sat'])){
        hidden = false
        colorWarning = true
    }
    if(settings['close_temp'] && (settings['close_hue'] || settings['close_sat'])){
        hidden = false
        colorWarning = true
    }

    width = 12
    if(onAtOpen && onAtClose) width = 6

    sectionTitle = ''
    if(!settings['open_temp'] && !settings['close_level']) sectionTitle = 'Click to set temperature color (optional)'
    if(settings['open_temp'] && (!settings['close_level'] || settings['close_temp'] == settings['close_level'])) sectionTitle = '<b>On open, set temperature color to ' + settings['open_temp'] + 'K</b>'
    if(settings['close_temp'] && !settings['open_temp']) sectionTitle += '<br>'
    if(settings['close_temp'] && !settings['open_temp']) sectionTitle += '<b>On close, set temperature color to ' + settings['close_temp'] + 'K</b>'

    section(hideable: true, hidden: hidden, sectionTitle){
        if(!validateOpenTemp()) displayError('Start temperature color must be from 1800 to 5400. Correct start temperature color.')
        if(!validateCloseTemp()) displayError('Stop temperature color must be from 1800 to 5400. Correct stop temperature color.')
        if(colorWarning) displayWarning('Color options have been entered that conflict with temperature color. Color will take precedence over temperature color.')

        inputTitle = 'Set temperature color on open?'
        if(settings['open_temp']) inputTitle = 'Set temperature color on open:'
        input 'open_temp', 'number', title: inputTitle, width: width, submitOnChange:true
        inputTitle = 'Set temperature color on close?'
        if(settings['close_temp']) inputTitle = 'Set temperature color on open:'
        if(settings['stop_timeType'] &&  settings['close_action'] != 'off') input 'close_temp', 'number', title: inputTitle, width: width, submitOnChange:true

        message = 'Temperature color is from 1800 to 5400, where daylight is 5000, cool white is 4000, and warm white is 3000.'
        if(!settings['open_temp'] && !settings['close_temp']) message = 'Temperature color is in Kelvin from 1800 to 5400. Lower values have more red, while higher values have more blue, where daylight is 5000, cool white is 4000, and warm white is 3000. (Optional.)'
        if(!validateOpenTemp() || !validateCloseTemp()) {
            displayError(message)
        } else {
            displayInfo(message)
        }
    }
}

def displayColorOption(){
    if(!settings['close_action']) return
    if(settings['deviceType'] != 'light') return
    if(!onAtOpen) return
    if(!onAtClose) return
    
    unit = hiRezHue ? '°' : '%'

    validateOpenHue = validateHue(settings['open_hue'])
    validateCloseHue = validateHue(settings['close_hue'])
    validateOpenSat = validateSat(settings['open_sat'])
    validateCloseSat = validateSat(settings['close_sat'])
    
    hidden = true
    if(!validateOpenHue) hidden = false
    if(!validateCloseHue) hidden = false
    if(!validateOpenSat) hidden = false
    if(!validateCloseSat) hidden = false
    if(settings['open_hue'] && !settings['close_hue'] && onAtClose) hidden = false
    if(!settings['open_hue'] && settings['close_hue'] && onAtClose) hidden = false
    if(settings['open_sat'] && !settings['close_sat'] && onAtOpen) hidden = false
    if(!settings['open_sat'] && settings['close_sat'] && onAtOpen) hidden = false
    
    width = 12
    if(onAtOpen && onAtClose) width = 6

    sectionTitle = ''
    if(!settings['open_hue'] && !settings['close_hue'] && !settings['open_sat'] && !settings['close_sat']){
        sectionTitle = 'Click to set color (hue and/or saturation) (optional)'
    } else {
        if(settings['open_hue'] && settings['open_sat']) sectionTitle = "<b>On open, set hue to " + settings['open_hue'] + "$unit and sat to " + settings['open_sat'] + "%</b>"
        if(settings['open_hue'] && !settings['open_sat']) sectionTitle = "<b>On open, set hue to " + settings['open_hue'] + "$unit</b>"
        if(!settings['open_hue'] && settings['open_sat']) sectionTitle = "<b>On open, set saturation " + settings['open_sat'] + "%</b>"
        
        if(!settings['open_hue'] || !settings['close_hue'] || !settings['open_sat'] || !settings['close_sat']) sectionTitle += moreOptions
        
        if((settings['open_hue'] || settings['open_sat']) && (settings['close_hue'] || settings['close_sat'])) sectionTitle += '<br>'
        
        if(settings['close_hue'] && settings['close_sat']) sectionTitle += "<b>On close, set hue to " + settings["close_hue"] + "$unit and sat to " + settings["close_sat"] + "%</b>"
        if(settings['close_hue'] && !settings['close_sat']) sectionTitle += "<b>On close, set hue to " + settings["close_hue"] + "$unit</b>"
        if(!settings['close_hue'] && settings['close_sat']) sectionTitle += "<b>On close, set saturation " + settings["close_sat"] + "%</b>"
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
        if(onAtClose) input 'close_hue', 'number', title: 'Set hue on close?', width: width, submitOnChange:true

        message = 'Hue is percent around a color wheel, where red is 0 (and 100). Orange = 8; yellow = 16; green = 33; turquiose = 50; blue = 66; purple = 79 (may vary by device). Optional.'
        if(hiRezHue) message = 'Hue is degrees around a color wheel, where red is 0 (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270 (may vary by device). Optional.'
        if(validateOpenHue && validateCloseHue) {
            displayInfo(message)
        } else {
            displayError(message)
        }
        
        if(onAtOpen) input 'open_sat', 'number', title: 'Set saturation on open?', width: width, submitOnChange:true
        if(onAtClose) input 'close_sat', 'number', title: 'Set saturation on close?', width: width, submitOnChange:true

        message = 'Saturation is the percentage depth of color, from 1 to 100, where 1 is hardly any color tint and 100 is full color. Optional.'
        if(validateOpenSat && validateCloseSat) {
            displayInfo(message)
        } else {
            displayError(message)
        }
    }
}

def displayScheduleSection(){
    if(!settings['close_action']) return
    
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
    
    //Date.parse( 'MM', "$month" ).format( 'MMMM' )
    hidden = true
    if(settings['start_timeType'] && !settings['stop_timeType']) hidden = false
    if(!settings['start_timeType'] && settings['stop_timeType']) hidden = false
    if(!startTimeComplete) hidden = false
    if(!stopTimeComplete) hidden = false
    if(!validateSunMinutes(settings['start_sunOffset'])) hidden = false
    if(!validateSunMinutes(settings['stop_sunOffset'])) hidden = false
    if(settings['start_timeType'] == 'time' && settings['start_time'] == settings['stop_time']) hidden = false
    
    sectionTitle = ''
    if(!settings['start_timeType'] && !settings['stop_timeType'] && !settings['days'] && !settings['months']) sectionTitle = 'Click to set schedule (optional)'
    
    if(settings['start_timeType']) sectionTitle += '<b>Starting: '
    if(settings['start_timeType'] == 'time' && settings['start_time']) sectionTitle += 'At ' + Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings['start_time']).format('h:mm a', location.timeZone)
    if(settings['start_timeType'] == 'time' && !settings['start_time']) sectionTitle += 'At specific time '
    if(settings['start_timeType'] == 'sunrise' || settings['start_timeType'] == 'sunset'){
        if(!settings['start_sunType']) sectionTitle += 'Based on ' + settings['start_timeType']
        if(settings['start_sunType'] == 'at') sectionTitle += 'At ' + settings['start_timeType']
        if(settings['start_sunOffset']) sectionTitle += ' ' + settings['start_sunOffset'] + ' minutes '
        if(settings['start_sunType'] && settings['start_sunType'] != 'at') sectionTitle += settings['start_sunType'] + ' ' + settings['start_timeType']
        if(startTimeComplete) sectionTitle += ' ' + getSunriseTime(settings['start_timeType'],settings['start_sunOffset'],settings['start_sunType'])
    }

    if(settings['start_timeType'] && settings['days']) sectionTitle += " on: $dayText"
    if(!settings['start_timeType'] && settings['days']) sectionTitle += "<b>On: $dayText"
    if(settings['months'] && settings['days']) sectionTitle += ';'
    if(settings['days'] && settings['months']) sectionTitle += " in $monthText"
    if(!settings['days'] && settings['months']) sectionTitle += "<b>In $monthText"
    if(settings['start_timeType']) sectionTitle += '</b>'
    if(!settings['days'] || !settings['months']) sectionTitle += moreOptions
    
    if(settings['start_timeType'] && settings['stop_timeType']) sectionTitle += '</br>'
    if(settings['stop_timeType']) sectionTitle += '<b>Stopping: '
    if(settings['stop_timeType'] == 'time' && settings['stop_time']) sectionTitle += 'At ' + Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings['stop_time']).format('h:mm a', location.timeZone)
    if(settings['stop_timeType'] == 'time' && !settings['stop_time']) sectionTitle += 'At specific time '
    if(settings['stop_timeType'] == 'sunrise' || settings['stop_timeType'] == 'sunset'){
        if(!settings['stop_sunType']) sectionTitle += 'Based on ' + settings['stop_timeType']
        if(settings['stop_sunType'] == 'at') sectionTitle += 'At ' + settings['stop_timeType']
        if(settings['stop_sunOffset']) sectionTitle += ' ' + settings['stop_sunOffset'] + ' minutes '
        if(settings['stop_sunType'] && settings['stop_sunType'] != 'at') sectionTitle += settings['stop_sunType'] + ' ' + settings['stop_timeType']
        if(stopTimeComplete) sectionTitle += ' ' + getSunriseTime(settings['stop_timeType'],settings['stop_sunOffset'],settings['stop_sunType'])
    }
    
    if(settings['start_timeType']) sectionTitle += '</b>'

    section(hideable: true, hidden: hidden, sectionTitle){
        if(settings['start_time'] && settings['start_time'] == settings['stop_time']) displayError('You can\'t have the same time to start and stop.')
        if(contactCount > 1 && deviceCount > 1) message = "Scheduling only applies with these $pluralContact. To schedule the devices or default settings for them, use the Time app."
        if(contactCount > 1 && deviceCount == 1) message = "Scheduling only applies with these $pluralContact. To schedule the device or default settings for it, use the Time app."
        if(contactCount == 1 && deviceCount > 1) message = "Scheduling only applies with this $pluralContact. To schedule the devices or default settings for them, use the Time app."
        if(contactCount == 1 && deviceCount == 1) message = "Scheduling only applies with this $pluralContact. To schedule the device or default settings for it, use the Time app."
        displayInfo(message)
        displayStartTypeOption()

        // Display exact time option
        if(settings['start_timeType'] == 'time'){
            displayTimeOption('start')
        } else if(settings['start_timeType']){
            // Display sunrise/sunset type option (at/before/after)
            displaySunriseTypeOption('start')
            // Display sunrise/sunset offset
            if(settings['start_sunType'] && settings['start_sunType'] != 'at') displaySunriseOffsetOption('start')
        }

        if(startTimeComplete && settings['start_timeType']){
            displayStopTypeOption()

            // Display exact time option
            if(settings['stop_timeType'] == 'time'){
                displayTimeOption('stop')
            } else if(settings['stop_timeType']){
                // Display sunrise/sunset type option (at/before/after)
                displaySunriseTypeOption('stop')
                // Display sunrise/sunset offset
                if(settings['stop_sunType'] && settings['stop_sunType'] != 'at') displaySunriseOffsetOption('stop')
            }
        }

        displayDaysOption()
        displayMonthsOption()
    }
}

def displayDaysOption(){
    if(settings['start_timeType'] && !settings['stop_timeType']) return
    if(!settings['start_timeType'] && settings['stop_timeType']) return
    if(!startTimeComplete || !stopTimeComplete) return

    inputTitle = 'On these days (optional; defaults to all days):'
    if(!settings['days']) inputTitle = 'On which days (optional; defaults to all days)?'
    input 'days', 'enum', title: inputTitle, multiple: true, width: 12, options: ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday'], submitOnChange:true

    return
}

def displayMonthsOption(){
    if(settings['start_timeType'] && !settings['stop_timeType']) return
    if(!settings['start_timeType'] && settings['stop_timeType']) return
    if(!startTimeComplete || !stopTimeComplete) return
    inputTitle = 'In these months (optional; defaults to all months):'
    if(!settings['months']) inputTitle = 'In which months (optional; defaults to all months)?'

    input 'months', 'enum', title: inputTitle, multiple: true, width: 12, options: ['1': 'January', '2': 'February', '3': 'March', '4': 'April', '5': 'May', '6': 'June', '7': 'July', '8': 'August', '9': 'September', '10': 'October', '11': 'November', '12': 'December'], submitOnChange:true

    return
}

def displayStartTypeOption(){
    if(!startTimeComplete || !settings['start_timeType']){
        displayLabel('Schedule starting time')
    } else {
        displayLabel('Schedule start')
    }
    
    if(settings['start_sunOffset'] && !validateSunMinutes(settings['start_sunOffset'])) displayWarning('Time ' + settings['start_sunType'] + ' ' + settings['start_timeType'] + ' is ' + (Math.round(settings['start_sunOffset']) / 60) + ' hours. That\'s probably wrong.')

    if(!settings['start_timeType']){
        width = 12
        inputTitle = highlightText('Start time (click to select)?')
        input 'start_timeType', 'enum', title: inputTitle, multiple: false, width: width, options: ['time':'Start at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)' ], submitOnChange:true
        displayInfo('Select whether to enter a specific time, or have start time based on sunrise and sunset for the Hubitat location. Required.')
    } else {
        if(settings['start_timeType'] == 'time' || !settings['start_sunType'] || settings['start_sunType'] == 'at'){
            width = 6
        } else if(settings['start_sunType']){
            width = 4
        }
        input 'start_timeType', 'enum', title: 'Start time option:', multiple: false, width: width, options: ['time':'Start at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)'], submitOnChange:true
    }
}

def displayStopTypeOption(){
    if(!stopTimeComplete){
        displayLabel('Schedule stopping time')
    } else {
        displayLabel('Schedule stop')
    }
    if(settings['stop_sunOffset'] && !validateSunMinutes(settings['stop_sunOffset'])) displayWarning('Time ' + settings['stop_sunType'] + ' ' + settings['stop_timeType'] + ' is ' + (Math.round(settings['stop_sunOffset']) / 60) + ' hours. That\'s probably wrong.')

    if(!settings['stop_timeType']){
        width = 12
        inputTitle = highlightText('Stop time (click to select)?')
        input 'stop_timeType', 'enum', title: inputTitle, multiple: false, width: width, options: ['time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)'], submitOnChange:true
    } else {
        if(!settings['stop_timeType'] || settings['stop_timeType'] == 'none'){
            width = 12
        } else if(settings['stop_timeType'] == 'time' || !settings['stop_sunType'] || settings['stop_sunType'] == 'at'){
            width = 6
        } else if(stop_sunType){
            width = 4
        }
        input 'stop_timeType', 'enum', title: 'Stop time option:', multiple: false, width: width, options: ['time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)'], submitOnChange:true
    }
}

def displayTimeOption(type){
    ucType = type.capitalize()
    inputTitle = type.capitalize() + ' time:'
    if(!settings["${type}_time"]) inputTitle = highlightText(type.capitalize() + ' time?')
    input "${type}_time", 'time', title: inputTitle, width: width, submitOnChange:true
    if(!settings["${type}_time"]) displayInfo("Enter the time to $type the schedule in \"hh:mm AM/PM\" format. Required.")
}

def displaySunriseTypeOption(type){
    if(!settings["${type}_sunType"] || settings["${type}_sunType"] == 'at') {
        width = 6 
    } else {
        width = 4
    }
    sunTime = getSunriseAndSunset()[settings["${type}_timeType"]].format('hh:mm a')
    inputTitle = "At, before or after " + settings["${type}_timeType"] + " ($sunTime):"
    if(!settings["${type}_sunType"]) inputTitle = highlightText("At, before or after " + settings["${type}_timeType"] + " ($sunTime)?")
    input "${type}_sunType", 'enum', title: inputTitle, multiple: false, width: width, options: ['at':'At ' + settings["${type}_timeType"], 'before':'Before ' + settings["${type}_timeType"], 'after':'After ' + settings["${type}_timeType"]], submitOnChange:true
    if(!settings["${type}_sunType"]) displayInfo("Select whether to start exactly at " + settings["${type}_timeType"] + " (currently, $sunTime). To allow entering minutes prior to or after " + settings["${type}_timeType"] + ", select \"Before " + settings["${type}_timeType"] + "\" or \"After " + settings["${type}_timeType"] + "\". Required.")
}

def checkTimeComplete(type){
    if(!settings["${type}_timeType"]) return true
    if(settings["${type}_timeType"] == 'time' && !settings["${type}_time"]) return false
    if((settings["${type}_timeType"] == 'sunrise' || settings["${type}_timeType"] == 'sunset') && !settings["${type}_sunType"]) return false
    if((settings["${type}_timeType"] == 'sunrise' || settings["${type}_timeType"] == 'sunset') && settings["${type}_sunType"] != 'at' && !settings["${type}_sunOffset"]) return false

    return true
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
    if(!settings["${type}_sunType"] || settings["${type}_sunType"] == 'at') return

    inputTitle = 'Minutes ' + settings["${type}_sunType"] + ' ' + settings["${type}_timeType"] + '?'
    if(settings["${type}_sunOffset"]) inputTitle = 'Minutes ' + settings["${type}_sunType"] + ' ' + settings["${type}_timeType"] + ':'
    input "${type}_sunOffset", 'number', title: inputTitle, width: 4, submitOnChange:true
    
    if(!settings["${type}_sunOffset"] || !validateSunMinutes(settings["${type}_sunOffset"])) message = "Enter the number of minutes " + settings["${type}_sunType"] + " " + settings["${type}_timeType"] + " to start the schedule. Required."
    if(!settings["${type}_sunOffset"]) {
        displayInfo(message)
    } else if(!validateSunMinutes(settings["${type}_sunOffset"])){
        displayWarning(message)
    }
}

def displayChangeModeOption(){
    if(!settings['close_action']) return

    hidden = true
    if((settings['open_mode'] || settings['close_mode']) && (!settings['open_mode'] || !settings['close_mode'])) hidden = false

    
    sectionTitle = ''
    if(!settings['open_mode'] && !settings['close_mode']){
        sectionTitle = 'Click to set Mode change (optional)'
    } else {
        if(settings['open_mode']) sectionTitle = '<b>On open, set Mode ' + settings['open_mode'] + '</b>'
        if(settings['open_mode'] && settings['close_mode']) sectionTitle += '<br>'
        if(settings['close_mode']) sectionTitle += '<b>On close, set Mode ' + settings['close_mode'] + '</b>'
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        input 'open_mode', 'mode', title: 'Set Hubitat\'s "Mode" on open?', width: 6, submitOnChange:true
        input 'close_mode', 'mode', title: 'Set Hubitat\'s "Mode" on close?', width: 6, submitOnChange:true
    }
}

def displayIfModeOption(){
    if(!settings['close_action']) return

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
    if(!settings['close_action']) return
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
        
        if((settings['pushNotificationDevice'] && settings['pushNotification']) || (settings['speechDevice'] && settings['speech'])) input 'notificationOpenClose', 'enum', title: sectionTitle, multiple: false, width: 12, options: ['open': 'Open', 'close': 'Close','both': 'Both open and close'], submitOnChange:true
    }
}

def displayPeopleOption(){
    if(!settings['close_action']) return

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

/*
disable - bool - Flag to disable this single contact
contactDevice - capability.contactSensor - Contact sensor being monitored
deviceType - Sets whether to expect lights (switchLevel), switches, or locks
device - Device(s) being controlled (opened or closed); may be switch, switchLevel, or lock
open_action - enum (none, on, off, resume, toggle, lock, or unlock) - Action to perform on device when opened
close_action - enum (none, on, off, resume, toggle, lock, unlock) - Action to perform on device when closed
open_level - number (1-100) - Level to set openSwitch when opened
close_level - number (1-100) - Level to set closeSwitch when closed
open_temp - number (1800-5400) - Temperature to set openSwitch when opened
close_temp - number (1800-5400) - Temperature to set closeSwitch when closed
open_hue - number (1-100) - Hue to set openSwitch when opened
close_hue - number (1-100) - Hue to set closeSwitch when closed
open_sat - number (1-100) - Hue to set openSwitch when opened
close_sat - number (1-100) - Hue to set closeSwitch when closed
days - enum (Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday) - Days on which contact will run. Only displays if scheduleEnable = true
start_timeType - enum (time, sunrise, sunset) - Sets whether start time is a specific time, or based on sunrise or sunset. Only displays if scheduleEnable = true
start_time - time - Start Time (only displays when scheduleEnable = true and start_timeType = "time")
start_sunType - enum (at, before, after) - Sets whether start time is sunrise/sunset time, or uses positive or negative offset (only displays if scheduleEnable = true, and start_timeType = "sunrise" or "sunset")
start_sunOffset - number - Number of minutes before/after sunrise/sunset for start time (only displays if scheduleEnable = true, start_timeType = "sunrise" or "sunset", and start_sunType = "before" or "after")
stop_timeType - enum (time, sunrise, sunset) - Sets whether stop time is a specific time, or based on sunrise or sunset. (Only displays if scheduleEnable = true)
stop_time - time - Stop Time (only displays when scheduleEnable = true and stop_timeType = "time")
stop_sunType - enum (at, before, after) - Sets whether start time is sunrise/sunset time, or uses positive or negative offset (only displays if scheduleEnable = true, and start_timeType = "sunrise" or "sunset")
stop_sunOffset - number - Number of minutes before/after sunrise/sunset for stop time (only displays if scheduleEnable = true, start_timeType = "sunrise" or "sunset", and start_sunType = "before" or "bfter")
open_wait - number - Minutes to delay open action(s). 
close_wait - number - Minutes to delay close action(s). 
pushNotificationDevice - Device(s) for push notifications
pushNotification - text - Text to push
speechDevice - Device(s) for speech
speech - text - Text to speak.
notificationOpenClose - bool - Switch push notification and speech when door opened, or closed.
open_mode - bool - Switch to change mode when door opened, or closed.
close_mode - bool - Switch to change mode when door opened, or closed. 
personHome - capability.presenseSensor - Persons any of who must be home for contact to run. 
personNotHome - capability.presenseSensor - Persons all of who must not be home for contact to run.
*/

/* ************************************************************************ */
/*                                                                          */
/*                          End display functions.                          */
/*                                                                          */
/* ************************************************************************ */

def installed() {
    state.logLevel = getLogLevel()
    putLog(1074,'trace','Installed')
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    state.logLevel = getLogLevel()
    putLog(1081,'trace','Updated')
    unsubscribe()
    initialize()
}

def initialize() {
    state.logLevel = getLogLevel()
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    
/* ************************************************************************ */
/* TO-DO: Add routine to remove devices from table if removed as a device   */
/* controlled by a contact sensor.                                          */
/* ************************************************************************ */ 

    unschedule()

    // If date/time for last notification not set, initialize it to 5 minutes ago
    if(!state.contactLastNotification) state.contactLastNotification = new Date().getTime() - 360000

    if(settings['disable']) {
        state.disable = true
        return
    }
    state.disable = false

    if(!settings['disable'] && !state.disable) {
        subscribe(contactDevice, 'contact.open', contactChange)
        subscribe(contactDevice, 'contact.closed', contactChange)            
    }
    
    setTime()

    putLog(1113,'trace','Initialized')
}

def contactChange(evt){
    if(state.disable) return

    putLog(1115,'debug',"Contact sensor $evt.displayName $evt.value")

    // If mode set and node doesn't match, return nulls
    if(settings['ifMode'] && location.mode != settings['ifMode']) {
        putLog(1123,'trace',"Contact disabled, requires mode $ifMode")
        return
    }

    // If not correct day, return nulls
    if(!parent.nowInDayList(settings['days'],app.label)) return
    if(!parent.nowInMonthList(settings['months'],app.label)) return

    // if not between start and stop time, return nulls
    if(!parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return

    if(!parent.getPeopleHome(settings['personHome'],app.label)) return
    if(!parent.getNooneHome(settings['personNotHome'],app.label)) return

    // Unschedule pevious events

    // If opened a second time, it will reset delayed action
    // If closed a second time, it won't override open
    if(evt.value == 'open'){
        unschedule()
    } else {
        unschedule(runScheduleClose)
    }

    // Perform open events (for switches and locks)
    if(evt.value == 'open'){
        // Schedule delay
        if(settings['open_wait']) {
            putLog(1151,'trace','Scheduling runScheduleOpen in ' + settings['open_wait'] + ' seconds')
            runIn(settings['open_wait'],runScheduleOpen)
            // Otherwise perform immediately
        } else {
            runScheduleOpen()
        }

        // Perform close events (for switches and locks)
    } else if(evt.value == 'closed'){
        // Schedule delay
        if(settings['close_wait']) {
            putLog(1162,'trace','Scheduling runScheduleClose in ' + settings['close_wait'] + ' seconds')
            runIn(settings['close_wait'],runScheduleClose)
            // Otherwise perform immediately
        } else {
            runScheduleClose()
        }
    }

    // Text first (just in case there's an error later)
    // Need to move to a function
    if(settings['pushNotificationDevice'] && (settings['notificationOpenClose'] == 'both' || (settings['notificationOpenClose'] == 'open' && evt.value == 'open') || (settings['notificationOpenClose'] == 'close' && evt.value == 'closed'))){
        def now = new Date()getTime()

        //if last text was sent less than 5 minutes ago, don't send
        /* ************************************************************************ */
        /* TO-DO: Add option to override text cooldown period? (Maybe in Master?)   */
        /* Same with presence app.                                                  */
        /* ************************************************************************ */
        // Compute seconds from last notification
        seconds = (now - state.contactLastNotification) / 1000

        // Convert date to friendly format for log
        if(seconds > 360){
            state.contactLastNotification = now

            if(evt.value == 'open') {
                eventName = 'opened'
            } else {
                eventName = evt.value
            }
            settings['pushNotificationDevice'].each{
                parent.sendPushNotification(it,"$evt.displayName was $eventName at " + now.format('h:mm a', location.timeZone),app.label)
            }
            putLog(1195,'info',"Sent push notice for $evt.displayName $eventName at " + now.format('h:mm a', location.timeZone) + ".")
        } else {
            putLog(1197,'info',"Did not send push notice for $evt.displayName $evt.value due to notification sent $seconds ago.")
        }
    }

    // Give voice alert
    if(settings['speech'] && ((!settings['notificationOpenClose'] && evt.value == 'open') || (settings['notificationOpenClose'] && evt.value == 'closed'))) {
        /* ************************************************************************ */
        /* TO-DO: Add option to override speech cooldown period? (Maybe in Master?) */
        /* Same with presence app.                                                  */
        /* ************************************************************************ */
        settings['speech'].each{
            parent.speakSingle(settings['speech'],it,app.label)
        }
    }

    // Set mode
    if(settings['open_mode'] && evt.value == 'open'){
        parent.changeMode(settings['open_mode'],app.label)
    } else if(settings['close_mode'] && evt.value == 'closed'){
        parent.changeMode(settings['close_mode'],app.label)
    }

}

def runScheduleOpen(){
    if(settings['disable'] || state.disable) return
    
    if(settings['ifMode'] && location.mode != settings['ifMode']) {
        putLog(1225,'trace',"Contact disabled, requires mode $ifMode")
        return
    }

    if(!parent.getPeopleHome(settings['personHome'],app.label)) return
    if(!parent.getNooneHome(settings['personNotHome'],app.label)) return

    if(settings['deviceType'] == 'switch' || settings['deviceType'] == 'light') {
        defaults = [:]

        if(settings['open_action'] == 'on' || settings['open_action'] == 'off' || settings['open_action'] == 'toggle') parent.updateStateMulti(settings['device'],settings['open_action'],app.label)
        if(settings['open_level']) defaults.'level' = ['startLevel':settings['open_level'],'appId':'contact']
        if(settings['open_temp']) defaults.'temp' = ['startLevel':settings['open_temp'],'appId':'contact']
        if(settings['open_hue']) defaults.'hue' = ['startLevel':settings['open_hue'],'appId':'contact']
        if(settings['open_sat']) defaults.'sat' = ['startLevel':settings['open_sat'],'appId':'contact']
        if(settings['open_level'] || settings['open_temp'] || settings['open_hue'] || settings['open_sat']) parent.updateLevelsMulti(settings['device'],defaults,app.label)
        if(settings['open_action'] == 'on' || settings['open_action'] == 'off' || settings['open_action'] == 'toggle' || settings['open_level'] || settings['open_temp'] || settings['open_hue'] || settings['open_sat']) parent.setStateMulti(settings['device'],app.label)
        
        // No option to schedule locks (yet); move out of if statement if feature is added
        if(settings['open_action'] == 'resume'){
            defaults = ['level':['time':'resume','appId':'contact'],
                'temp':['time':'resume','appId':'contact'],
                'hue':['time':'resume','appId':'contact'],
                'sat':['time':'resume','appId':'contact']]
         parent.updateLevelsMulti(settings['device'],defaults,app.label)
        }
    } else if(settings['deviceType'] == 'lock') {
        parent.multiLock(settings['open_action'],settings['device'],app.label)
    }
}

def runScheduleClose(){
    if(settings['disable'] || state.disable) return
    
        if(settings['ifMode'] && location.mode != settings['ifMode']) {
        putLog(1260,'trace',"Contact disabled, requires mode $ifMode")
        return
    }

    if(!parent.getPeopleHome(settings['personHome'],app.label)) return
    if(!parent.getNooneHome(settings['personNotHome'],app.label)) return

    if(deviceType == 'switch' || deviceType == 'light') {
        defaults = [:]

        if(settings['close_action'] == 'on' || settings['close_action'] == 'off' || settings['close_action'] == 'toggle') parent.updateStateMulti(settings['device'],settings['close_action'],app.label)
        if(settings['close_level']) defaults.'level' = ['startLevel':settings['close_level'],'appId':'contact']
        if(settings['close_temp']) defaults.'temp' = ['startLevel':settings['close_temp'],'appId':'contact']
        if(settings['close_hue']) defaults.'hue' = ['startLevel':settings['close_hue'],'appId':'contact']
        if(settings['close_sat']) defaults.'sat' = ['startLevel':settings['close_sat'],'appId':'contact']
        if(settings['close_level'] || settings['close_temp'] || settings['close_hue'] || settings['close_sat']) parent.updateLevelsMulti(settings['device'],defaults,app.label)
        if(settings['close_action'] == 'on' || settings['close_action'] == 'off' || settings['close_action'] == 'toggle' || settings['close_level'] || settings['close_temp'] || settings['close_hue'] || settings['close_sat']) parent.setStateMulti(settings['device'],app.label)

        if(settings['close_action'] == 'resume') {
                defaults = ['level':['time':'stop','appId':app.id],
                'temp':['time':'stop','appId':app.id],
                'hue':['time':'stop','appId':app.id],
                'sat':['time':'stop','appId':app.id]]
            parent.updateLevelsMulti(settings['device'],defaults,app.label)
        }
    } else if(settings['deviceType'] == 'lock') {
        parent.multiLock(settings['close_action'],settings['device'],app.label)
    }
}

def setTime(){
    if(setStartTime()) {
        setStopTime()
        return true
    }
    return false
}

def setStartTime(){
    if(!settings['start_timeType']) {
        atomicState.start = null
        return
    }

    setTime = setStartStopTime('start')

    if(setTime){
        atomicState.start = setTime
        putLog(1308,'info','Start time set to ' + parent.normalPrintDateTime(setTime))
        return true
    }
}

def setStopTime(){
    if(!settings['start_timeType'] || settings['stop_timeType'] == 'none') {
        atomicState.stop = null
        return
    }
    setTime = setStartStopTime('stop')
    if(setTime){ 
        if(atomicState.start > setTime) setTime = parent.getTomorrow(setTime,app.label)
        atomicState.stop = setTime
        putLog(1322,'info','Stop time set to ' + parent.normalPrintDateTime(setTime))
    }
    return
}

// Sets atomicState.start and atomicState.stop variables
// Requires type value of "start" or "stop" (must be capitalized to match setting variables)
def setStartStopTime(type){
    if(settings["${type}_timeType"] == 'time') return Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSSZ", settings["${type}_time"]).getTime()
    if(settings["${type}_timeType"] == 'sunrise') return (settings["${type}_sunType"] == 'before' ? parent.getSunrise(settings["${type}_sunOffset"] * -1,app.label) : parent.getSunrise(settings["${type}_sunOffset"],app.label))
    if(settings["${type}_timeType"] == 'sunset') return (settings["${type}_sunType"] == 'before' ? parent.getSunset(settings["${type}_sunOffset"] * -1,app.label) : parent.getSunset(settings["${type}_sunOffset"],app.label))
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
    if(type == 'error') logMessage = '<font color="red">'
    if(type == 'warn') logMessage = '<font color="brown">'
    logMessage += "$app.label "
    if(lineNumber) logMessage += "(line $lineNumber) "
    if(message) logMessage += "-- $message"
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
    }
}
