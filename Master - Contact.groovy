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
*  Version: 0.6.05
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
            
            
            if(settings['openAction'] == 'none') settings['openWait'] = false
            if(settings['closeAction'] == 'none') settings['closeWait'] = false
            if(settings['openAction'] == 'off' || settings['openAction'] == 'resume') settings['openLevel'] = false
            if(settings['openAction'] == 'off' || settings['openAction'] == 'resume') settings['openTemp'] = false
            if(settings['closeAction'] == 'off' || settings['closeAction'] == 'resume') settings['closeLevel'] = false
            if(settings['closeAction'] == 'off' || settings['closeAction'] == 'resume') settings['closeTemp'] = false
            if(settings['inputStartType'] == 'time') settings['inputStartSunriseType'] = false
            if(settings['inputStartType'] != 'time') settings['inputStartTime'] = false
            if(settings['inputStartSunriseType'] == 'at' || !settings['inputStartSunriseType']) settings['inputStartBefore'] = false
            if(settings['inputStopSunriseType'] == 'at' || !settings['inputStopSunriseType']) settings['inputStopBefore'] = false
            if(!settings['pushNotification'] && !settings['speech']) {
                settings['notificationOpenClose'] = false
                settings['personHome'] = false
                settings['personNotHome'] = false
            }
            
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
            plainOpenAction = getPlainAction(settings["openAction"])
            plainCloseAction = getPlainAction(settings["closeAction"])
            opening = getOpening()
            closing = getClosing()
            hiRezHue = parent.getHiRezHue()
            peopleError = compareDeviceLists(personHome,personNotHome)

            section(){
                displayNameOption()
                displayDevicesOption()
                displayDevicesTypes()
                displayOpenCloseDevicesOption()
                if(install) displayDisableOption()
                
            }

            displayOpenOptions()
            displayCloseOptions()
            displayBrightnessOption()
            if((!settings["openhue"] && !settings["openSat"]) || (!settings["closeHue"] && !settings["closeSat"])) displayTemperatureOption()
            if(!settings["openTemp"] || !settings["closeTemp"]) displayColorOption()
            displayChangeModeOption()
            displayAlertOptions()
            displayScheduleSection()
            displayIfModeOption()
            //displayIfPeopleOption()
        }
    }
}

def formComplete(){
    
    if((!app.label) ||
       (!contactDevice) ||
       (!deviceType) ||
       (!device) ||
       (!openAction) ||
       (!closeAction) ||
       (inputStartType == "time" && !inputStartTime) ||
       (inputStopType == "time" && !inputStopTime) ||
       ((inputStartType == "sunrise" || inputStartType == "sunset") && !inputStartSunriseType) ||
       ((inputStopType == "sunrise" || inputStopType == "sunset") && !inputStopSunriseType) ||
       ((inputStartSunriseType == "before" || inputStartSunriseType == "after") && !inputStartBefore) ||
       ((inputStopSunriseType == "before" || inputStopSunriseType == "after") && !inputStopBefore) ||
       (!validateOpenLevel()) || 
       (!validateCloseLevel()) ||
       (!validateOpenTemp()) || 
       (!validateCloseTemp()) ||  
       (!validateHue(openHue)) || 
       (!validateHue(closeHue)) || 
       (!validateSat(openSat)) ||
       (speechDevice && !speechopenAction == "none") ||
       (pushNotificationDevice && !pushNotification) ||
       ((settings['pushNotification'] || settings['speech']) && !settings['notificationOpenClose']) ||
       ((openAction == "none" && closeAction == "none" && !openLevel && !closeLevel && !openTemp && !closeTemmp && !openSat && !closeSat && !openHue && !closeHue && !openMode && !closeMode && !pushNotification) && !speech) ||
       (compareDeviceLists(personHome,personNotHome))) return false
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
        if(settings["deviceType"] == "lock") return "lock(s)"
        if(settings["deviceType"] == "light") return "light(s)"
        if(settings["deviceType"] == "switch") return "switch(es)"
    }
    
    if(deviceCount > 1) {
        if(settings["deviceType"] == "lock") return "locks"
        if(settings["deviceType"] == "light") return "lights"
        if(settings["deviceType"] == "switch")return "switches"
    }

    if(settings["deviceType"] == "lock") return "lock"
    if(settings["deviceType"] == "light") return "light"
    if(settings["deviceType"] == "switch") return "switch"
}

def getOpening(){
    if(settings["openAction"] == "off") return false
    if(settings["openAction"] == "resume") return false
    return true
}

def getClosing(){
    if(settings["closeAction"] == "off") return false
    if(settings["closeAction"] == "resume") return false
    return true
}

def validateOpenWait(){
    return validateWait(openWait)
}

def validateCloseWait(){
    return validateWait(closeWait)
}

def validateWait(time){
        if(!time) return true
        if(time > 7200) return false
    return true
}

def validateOpenLevel(){
    return parent.validateLevel(openLevel)
}

def validateCloseLevel(){
    return parent.validateLevel(closeLevel)
}

def validateOpenTemp(){
    return parent.validateTemp(openTemp)
}

def validateCloseTemp(){
    return parent.validateTemp(closeTemp)
}

def validateHue(value){
    return parent.validateHue(value)
}

def validateSat(value){
    return parent.validateSat(value)
}

def validateSunriseMinutes(time){
        if(!time) return true
        if(time > 719) return false
    return true
}
    
def displayAllErrors(){
    if(settings["open}Hue"] && settings["open}Hue"] > 100 && !hiRezHue) displayError("Hue can't be more then 100. Correct open hue.")
        if(settings["openHue"] && settings["openHue"] > 360 && hiRezHue) displayError("Hue can't be more then 360. Correct open hue.")
        if(settings["openSat"] && settings["openSat"] > 100) displayError("Saturation can't be more then 100. Correct open saturation.")
        if(settings["closeHue"] && settings["closeHue"] > 100 && !hiRezHue) displayError("Hue can't be more then 100. Correct close hue.")
        if(settings["closeHue"] && settings["closeHue"] > 360 && hiRezHue) displayError("Hue can't be more then 360. Correct close hue.")
        if(settings["closeSat"] && settings["closeSat"] > 100) displayError("Saturation can't be more then 100. Correct close saturation.")
}

def displayAllWarnings(){
            if(openWait > 1800) warningMessage("Wait time has been set to " + Math.round(openWait / 60) + " <i>minutes</i>. Is that correct?")
            if(closeWait > 1800) warningMessage("Wait time has been set to " + Math.round(closeWait / 60) + " <i>minutes</i>. Is that correct?")
}

def warningMessage(text){
    if(warning){
        warning = "$warning<br />$warningIcon $text"
    } else {
        warning = "<div style=\"background-color:LemonChiffon\">$warningIcon $text"
    }
}

def displayLabel(text = "Null", width = 12){
    paragraph("<div style=\"background-color:#DCDCDC\"><b> $text:</b></div>",width:width)
}

def displayInfo(text = "Null",noDisplayIcon = null){
    if(text == "Null") {
        paragraph "<div style=\"background-color:AliceBlue\"> </div>"
    } else {
        if(noDisplayIcon){
            paragraph "<div style=\"background-color:AliceBlue\"> &nbsp; &nbsp; $text</div>"
        } else {
            paragraph "<div style=\"background-color:AliceBlue\">$infoIcon $text</div>"
        }
    }
}

def displayError(text){
    paragraph "<div style=\"background-color:Bisque\">$errorIcon $text</div>"
}

def displayWarning(text){
    paragraph "<div style=\"background-color:LemonChiffon\">$warningIcon $text</div>"
}

def displayNameOption(){
    if(app.label){
        displayLabel("Contact/door name",2)
        label title: "", required: true, width: 10,submitOnChange:true
    } else {
        displayLabel("Set name for this contact/door")
        label title: "", required: true, submitOnChange:true
        displayInfo("Name this contact/door sensor app. Each contact/door sensor app must have a unique name.")
    }
}

def displayDevicesOption(){
    if(settings["contactDevice"]){
        text = pluralContact.capitalize()
        input "contactDevice", "capability.contactSensor", title: "$text:", multiple: true, submitOnChange:true
    } else {
        input "contactDevice", "capability.contactSensor", title: "Select $pluralContact:", multiple: true, submitOnChange:true
        displayInfo("Select which $pluralContact for which to set actions.")
    }
}

def displayDevicesTypes(){
    if(!settings["contactDevice"]) return false
    input "deviceType", "enum", title: "Which type of device(s) to control (select one; required)", options: ["lock": "Lock(s)","light": "Light(s)", "switch": "Switch(es)"], multiple: false, required: true, submitOnChange:true
    if(!settings["deviceType"]) displayInfo("Light(s) allows selecting dimmable switches. Switch(es) include all lights. To control both locks and switches/lights, create a separate rule-set for each.")
}

def displayOpenCloseDevicesOption(){
    if(!settings["contactDevice"]) return
    if(!settings["deviceType"]) return

    if(settings["deviceType"] == "lock"){
        capability = "capability.lock"
    } else if(settings["deviceType"] == "light"){
        capability = "capability.switchLevel"
    } else if(settings["deviceType"] == "switch"){
        capability = "capability.switch"
    }

    if(settings["device"]) {
        input "device", "$capability", title: pluralDevice.capitalize() + " being controlled:", multiple: true, submitOnChange:true
    } else {
        input "device", "$capability", title: pluralDevice.capitalize() + " to control?", multiple: true, submitOnChange:true
        displayInfo("Select which $pluralDevice to control when the $pluralContact is opened. Required.")
    }

}

def displayDisableOption(){
    if(settings["disable"]){
        input "disable", "bool", title: "<b><font color=\"#000099\">This $singleContact app is disabled.</font></b> Reenable it?", submitOnChange:true
    } else {
        input "disable", "bool", title: "This $singleContact app is enabled. Disable it?", submitOnChange:true
    }
}

def displayOpenOptions(){
    if(!settings["contactDevice"]) return
    if(!settings["device"]) return

    hideable = false
    hidden = false
    sectionTitle = ''
    if(settings["openAction"]){
        if(settings["openWait"] && settings["openWait"] > 0) {
            sectionTitle = "<b>When opened: " + plainOpenAction.capitalize() + " after " + settings["openWait"] + " seconds</b>"
        } else if(settings["openAction"] != "none"){
            sectionTitle = "<b>When opened: " + plainOpenAction.capitalize() + "</b>" + moreOptions
        } else {
            sectionTitle = "<b>When opened: " + plainOpenAction.capitalize() + "</b>"
        }
        hideable = true
        hidden = true
    }
    if(!validateOpenWait()) hidden = false

    section(hideable: hideable, hidden: hidden, sectionTitle){
        if(!validateOpenWait()) displayWarning("Open wait time is " + Math.round(settings["openWait"] / 3600) + " hours (" + settings["openWait"] + " seconds). That's probably wrong.")
        if(settings["deviceType"] == "lock"){
            input "openAction", "enum", title: "When opened, lock or unlock?", multiple: false, width: 12, options: ["none": "Don't lock or unlock","lock": "Lock", "unlock": "Unlock"], submitOnChange:true
        } else {
            input "openAction", "enum", title: "When opened, do what with the $pluralDevice?", multiple: false, width: 12, options: ["none": "Don't turn on or off (leave as is)","on": "Turn On", "off": "Turn Off", "toggle": "Toggle", "resume":"Resume Schedule (or turn off)"], submitOnChange:true
            if(settings["openAction"]){
                if(deviceCount > 1) message = "Set whether to turn on, turn off, toggle, or resume schedule when $contactIndefiniteArticle $singleContact is opened. Select \"Don't\" to do nothing when opened. Required field."
                if(deviceCount <= 1) message = "Set whether to turn on, turn off, toggle, or resume schedule when $contactIndefiniteArticle $singleContact is opened. Select \"Don't\" to do nothing when opened. Required field."
            } else {
                if(deviceCount > 1) message = "Set whether to turn on, turn off, toggle, or resume schedule when $contactIndefiniteArticle $singleContact is opened. Select \"Don't\" to control other options (like setting Mode), or to do nothing when opened. Toggle will change the $pluralDevice from off to on and vice versa. Resume schedule will restart any schedule(s) for the $pluralDevice; if there are no active schedules, the $pluralDevice will turn off. Required field."
                if(deviceCount <= 1) message = "Set whether to turn on, turn off, toggle, or resume schedule when $contactIndefiniteArticle $singleContact is opened. Select \"Don't\" to control other options (like setting Mode), or to do nothing when opened. Toggle will change the $pluralDevice from off to on and vice versa. Resume schedule will restart any schedule(s) for the $pluralDevice; if there are no active schedules, the $pluralDevice will turn off. Required field."
            }
            displayInfo(message)
        }

        if(settings["openAction"] && settings["openAction"] != "none"){
            input "openWait", "number", title: "Wait seconds after open to $plainOpenAction $pluralDevice. (Optional)", defaultValue: false, submitOnChange:true

            if(settings["openWait"]) message = "If $contactIndefiniteArticleExtended $pluralContact is closed within " + settings['openWait'] + " seconds, the $pluralDevice will not $plainOpenAction. Instead, it will only $plainCloseAction with being closed."
            if(!settings["openWait"]) message = "If $contactIndefiniteArticleExtended $pluralContact is closed before time expires, the $pluralDevice will not $plainOpenAction. Instead, it will only $plainCloseAction with being closed."

            if(validateOpenWait()) {
                displayInfo(message)
            } else {
                displayWarning(message)
            }
        }
    }
}

def displayCloseOptions(){
    if(!settings['contactDevice']) return
    if(!settings['deviceType']) return
    if(!settings['device']) return
    if(!settings['openAction']) return

    hidden = true
        hideable = true
    if(!validateCloseWait()) hidden = false
    if(!settings['closeAction']) hideable = false
    
    sectionTitle = ''
    if(settings['closeAction']){
        if(settings['closeWait'] && settings['closeWait'] > 0) {
            sectionTitle = "<b>When closed: " + plainCloseAction.capitalize() + " after " + settings['closeWait'] + " seconds</b>"
        } else if(settings['closeAction'] != 'none'){
            sectionTitle = "<b>When closed: " + plainCloseAction.capitalize() + "</b>$moreOptions"
        } else {
            sectionTitle = "<b>When closed: " + plainCloseAction.capitalize() + "</b>"
        }
    }

    section(hideable: hideable, hidden: hidden, sectionTitle){
        if(!validateCloseWait()) displayWarning("Close wait time is " + Math.round(settings['closeWait'] / 3600) + " hours (" + settings['closeWait'] + " seconds). That's probably wrong.")
        if(settings['deviceType'] == 'lock'){
            input 'closeAction', 'enum', title: 'When closed, lock or unlock?', multiple: false, width: 12, options: ['none': 'Don\'t lock or unlock','lock': 'Lock', 'unlock': 'Unlock'], submitOnChange:true
        } else {
            input 'closeAction', 'enum', title: "When closed, do what with the $pluralDevice?", multiple: false, width: 12, options: ['none': 'Don\'t turn on or off (leave as is)','on': 'Turn On', 'off': 'Turn Off', 'toggle': 'Toggle', 'resume':'Resume Schedule (or turn off)'], submitOnChange:true
            if(deviceCount > 1) message = "Set whether to turn on, turn off, toggle, or resume schedule when $contactIndefiniteArticle $singleContact is closed. Select \"Don't\" to do nothing when closed. Required field."
            if(deviceCount <= 1) message = "Set whether to turn on, turn off, toggle, or resume schedule when $contactIndefiniteArticle $singleContact is closed. Select \"Don't\" to do nothing when closed. Required field."
            displayInfo(message)
        }
        
        if(settings['closeAction'] && settings['closeAction'] != 'none'){
            input 'closeWait', 'number', title: "Wait seconds after close to $plainCloseAction $pluralDevice. (Optional)", defaultValue: false, submitOnChange:true

            if(settings['closeWait']) message = "If $contactIndefiniteArticleExtended $pluralContact is opened within " + settings['closeWait'] + " seconds, the $pluralDevice will not $plainCloseAction. Instead, it will only $plainOpenAction with being opened."
            if(!settings['closeWait']) message = "If $contactIndefiniteArticleExtended $pluralContact is opened before time expires, the $pluralDevice will not $plainCloseAction. Instead, it will only $plainOpenAction with being opened."

            if(validateOpenWait()) {
                displayInfo(message)
            } else {
                displayWarning(message)
            }
        }
    }
}

def displayBrightnessOption(){
    if(!settings['contactDevice']) return
    if(!settings['deviceType']) return
    if(settings['deviceType'] != 'light') return
    if(!settings['device']) return
    if(!settings['openAction']) return
    if(!settings['closeAction']) return
    if((settings['closeAction'] == 'off' || settings['closeAction'] == 'resume') && (settings['openAction'] == 'off' || settings['openAction'] == 'resume')) return
    
    if(!opening && !closing) return

    hidden = true
    if(!validateOpenLevel()) hidden = false
    if(!validateCloseLevel()) hidden = false
    if(settings['openLevel'] && !settings['closeLevel'] && closing) hidden = false
    if(settings['closeLevel'] && !settings['openLevel'] && opening) hidden = false

    width = 12
    if(opening && closing) width = 6

    sectionTitle = ''
    if(!settings['openLevel'] && !settings['closeLevel']){
        sectionTitle = 'Click to set brightness (optional)'
    } else {
        if(settings['openLevel']) sectionTitle = '<b>On open, set brightness to ' + settings['openLevel'] + '%</b>'
        if(settings['openLevel'] && settings['closeLevel']) sectionTitle += '</br>'
        if(settings['closeLevel']) sectionTitle += '<b>On close, set brightness to ' + settings['closeLevel'] + '%</b>'
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        if(!validateOpenLevel()) displayError('Open level must be from 1 to 100. Correct open brightness.')
        if(!validateCloseLevel()) displayError('Close level must be from 1 to 100. Correct close brightness.')
        
        if(opening) input 'openLevel', 'number', title: 'Set brightness on open?', width: width, submitOnChange:true
        if(closing) input 'closeLevel', 'number', title: 'Set brightness on close?', width: width, submitOnChange:true

        if(opening && !closing) message = "Enter the percentage of brightness of light, from 1 to 100, when opening $contactIndefiniteArticleExtended $pluralContact. Cannot set brightness when turning off or resuming schedule. Optional."
        if(!opening && closing) message = "Enter the percentage of brightness of light, from 1 to 100, when closing $contactIndefiniteArticleExtended $pluralContact. Cannot set brightness when turning off or resuming schedule. Optional."
        if(opening && closing) message = "Enter the percentage of brightness of light, from 1 to 100, when opening and/or closing $contactIndefiniteArticleExtended $pluralContact. Optional."
        if(!validateOpenLevel() || !validateCloseLevel()) {
            displayError(message)
        } else {
            displayInfo(message)
        }
    }
}

def displayTemperatureOption(){
    if(!settings['contactDevice']) return
    if(!settings['deviceType']) return
    if(settings['deviceType'] != 'light') return
    if(!settings['device']) return
    if(!settings['openAction']) return
    if(!settings['closeAction']) return
    if((settings['closeAction'] == 'off' || settings['closeAction'] == 'resume') && (settings['openAction'] == 'off' || settings['openAction'] == 'resume')) return

    if(!opening && !closing) return
    
    hidden = true
    if(!validateOpenTemp()) hidden = false
    if(!validateCloseTemp()) hidden = false
    if(settings['openTemp'] && !settings['closeTemp'] && closing) hidden = false
    if(settings['closeTemp'] && !settings['openTemp'] && opening) hidden = false
    
    width = 12
    if(opening && closing) width = 6

    sectionTitle = ''
    if(!settings['openTemp'] && !settings['closeTemp']){
        sectionTitle = 'Click to set temperature color (optional)'
    } else {
        if(settings['openTemp']) sectionTitle = "<b>On open, set temperature color to " + settings['openTemp'] + "K</b>"
        if(settings['openTemp'] && settings['closeTemp']) sectionTitle += '<br>'
        if(settings['closeTemp']) sectionTitle += "<b>On close, set temperature color to "+ settings['closeTemp'] + "K</b>"
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        if(!validateOpenTemp()) displayError('Open temperature color must be from 1800 to 6500. Correct open temperature.')
        if(!validateCloseTemp()) displayError('Close temperature color must be from 1800 to 6500. Correct close temperature.')

        if(settings['openAction'] != 'off' && settings['openAction'] != 'resume') input 'openTemp', 'number', title: 'Set temperature color on open?', width: width, submitOnChange:true
        if(settings['closeAction'] != 'off' && settings['closeAction'] != 'resume') input 'closeTemp', 'number', title: 'Set temperature color on close?', width: width, submitOnChange:true

        message = 'Temperature color is from 1800 to 6500, where daylight is 5000, cool white is 4000, and warm white is 3000. Optional.'
        if(!settings['openTemp'] && !settings['closeTemp']) message = 'Temperature color is in Kelvin from 1800 to 6500, but range supported by bulbs vary. Lower values have more red, while higher values have more blue, where daylight is 5000, cool white is 4000, and warm white is 3000. Optional.'
        if(!validateOpenTemp() || !validateCloseTemp()) {
            displayError(message)
        } else {
            displayInfo(message)
        }
    }
}

def displayColorOption(){
    if(!settings['contactDevice']) return
    if(!settings['deviceType']) return
    if(settings['deviceType'] != 'light') return
    if(!settings['device']) return
    if(!settings['openAction']) return
    if(!settings['closeAction']) return
    if((settings['closeAction'] == 'off' || settings['closeAction'] == 'resume') && (settings['openAction'] == 'off' || settings['openAction'] == 'resume')) return
    
    unit = hiRezHue ? '°' : '%'

    validateOpenHue = validateHue(settings['openHue'])
    validateCloseHue = validateHue(settings['closeHue'])
    validateOpenSat = validateSat(settings['openSat'])
    validateCloseSat = validateSat(settings['closeSat'])
    
    hidden = true
    if(!validateOpenHue) hidden = false
    if(!validateCloseHue) hidden = false
    if(!validateOpenSat) hidden = false
    if(!validateCloseSat) hidden = false
    if(settings['openHue'] && !settings['closeHue'] && closing) hidden = false
    if(!settings['openHue'] && settings['closeHue'] && closing) hidden = false
    if(settings['openSat'] && !settings['closeSat'] && opening) hidden = false
    if(!settings['openSat'] && settings['closeSat'] && opening) hidden = false
    
    width = 12
    if(opening && closing) width = 6

    sectionTitle = ''
    if(!settings['openHue'] && !settings['closeHue'] && !settings['openSat'] && !settings['closeSat']){
        sectionTitle = 'Click to set color (hue and/or saturation) (optional)'
    } else {
        if(settings['openHue'] && settings['openSat']) sectionTitle = "<b>On open, set hue to " + settings['openHue'] + "$unit and sat to " + settings['openSat'] + "%</b>"
        if(settings['openHue'] && !settings['openSat']) sectionTitle = "<b>On open, set hue to " + settings['openHue'] + "$unit</b>"
        if(!settings['openHue'] && settings['openSat']) sectionTitle = "<b>On open, set saturation " + settings['openSat'] + "%</b>"
        
        if(!settings['openHue'] || !settings['closeHue'] || !settings['openSat'] || !settings['closeSat']) sectionTitle += moreOptions
        
        if((settings['openHue'] || settings['openSat']) && (settings['closeHue'] || settings['closeSat'])) sectionTitle += '<br>'
        
        if(settings['closeHue'] && settings['closeSat']) sectionTitle += "<b>On close, set hue to " + settings["closeHue"] + "$unit and sat to " + settings["closeSat"] + "%</b>"
        if(settings['closeHue'] && !settings['closeSat']) sectionTitle += "<b>On close, set hue to " + settings["closeHue"] + "$unit</b>"
        if(!settings['closeHue'] && settings['closeSat']) sectionTitle += "<b>On close, set saturation " + settings["closeSat"] + "%</b>"
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
        
        if(opening) input 'openHue', 'number', title: 'Set hue on open?', width: width, submitOnChange:true
        if(closing) input 'closeHue', 'number', title: 'Set hue on close?', width: width, submitOnChange:true

        message = 'Hue is percent around a color wheel, where red is 0 (and 100). Orange = 8; yellow = 16; green = 33; turquiose = 50; blue = 66; purple = 79 (may vary by device). Optional.'
        if(hiRezHue) message = 'Hue is degrees around a color wheel, where red is 0 (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270 (may vary by device). Optional.'
        if(validateOpenHue && validateCloseHue) {
            displayInfo(message)
        } else {
            displayError(message)
        }
        
        if(opening) input 'openSat', 'number', title: 'Set saturation on open?', width: width, submitOnChange:true
        if(closing) input 'closeSat', 'number', title: 'Set saturation on close?', width: width, submitOnChange:true

        message = 'Saturation is the percentage depth of color, from 1 to 100, where 1 is hardly any color tint and 100 is full color. Optional.'
        if(validateOpenSat && validateCloseSat) {
            displayInfo(message)
        } else {
            displayError(message)
        }
    }
}

def displayScheduleSection(){
    if(!settings['contactDevice']) return
    if(!settings['deviceType']) return
    if(!settings['device']) return
    if(!settings['openAction']) return
    if(!settings['closeAction']) return
    
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
    
    //Date.parse( 'MM', "$month" ).format( 'MMMM' )
    if(!checkTimeComplete('start') && (settings['inputStartType'] || settings['inputStartType'])) hidden = false
    if(!checkTimeComplete('stop') && (settings['inputStartType'] || settings['inputStartType'])) hidden = false
    if(!validateSunriseMinutes(settings['inputStartBefore']) || !validateSunriseMinutes(settings['inputStopBefore'])) hidden = false
    if(settings['inputStartTime'] && settings['inputStartTime'] == settings['inputStopTime']) hidden = false
    
    sectionTitle = ''
    if(!settings['inputStartType'] && !settings['inputStopType'] && !settings['days'] && !settings['months']) sectionTitle = 'Click to set schedule (optional)'
    
    if(settings['inputStartType']) sectionTitle += '<b>Starting: '
    if(settings['inputStartType'] == 'time' && settings['inputStartTime']) sectionTitle += 'At ' + Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings['inputStartTime']).format('h:mm a', location.timeZone)
    if(settings['inputStartType'] == 'time' && !settings['inputStartTime']) sectionTitle += 'At specific time '
    if(settings['inputStartType'] == 'sunrise' || settings['inputStartType'] == 'sunset'){
        if(!settings['inputStartSunriseType']) sectionTitle += 'Based on ' + settings['inputStartType']
        if(settings['inputStartSunriseType'] == 'at') sectionTitle += 'At ' + settings['inputStartType']
        if(settings['inputStartBefore']) sectionTitle += ' ' + settings['inputStartBefore'] + ' minutes '
        if(settings['inputStartSunriseType'] && settings['inputStartSunriseType'] != 'at') sectionTitle += settings['inputStartSunriseType'] + ' ' + settings['inputStartType']
        if(checkTimeComplete('start')) sectionTitle += ' ' + getSunriseTime(settings['inputStartType'],settings['inputStartBefore'],settings['inputStartSunriseType'])
    }
    if(settings['inputStartType'] && settings['days']) sectionTitle += " on: $dayText"
    if(settings['inputStartType'] && settings['months'] && settings['days']) sectionTitle += ';'
    if(settings['inputStartType'] && settings['months']) sectionTitle += " in $monthText"
    if(settings['inputStartType']) sectionTitle += '</b>'
    if(!settings['days'] || !settings['months']) sectionTitle += moreOptions
    
    if(settings['inputStartType'] && settings['inputStopType']) sectionTitle += '</br>'
    if(settings['inputStopType']) sectionTitle += '<b>Stopping: '
    if(settings['inputStopType'] == 'time' && settings['inputStopTime']) sectionTitle += 'At ' + Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings['inputStopTime']).format('h:mm a', location.timeZone)
    if(settings['inputStopType'] == 'time' && !settings['inputStopTime']) sectionTitle += 'At specific time '
    if(settings['inputStopType'] == 'sunrise' || settings['inputStopType'] == 'sunset'){
        if(!settings['inputStopSunriseType']) sectionTitle += 'Based on ' + settings['inputStopType']
        if(settings['inputStopSunriseType'] == 'at') sectionTitle += 'At ' + settings['inputStopType']
        if(settings['inputStopBefore']) sectionTitle += ' ' + settings['inputStopBefore'] + ' minutes '
        if(settings['inputStopSunriseType'] && settings['inputStopSunriseType'] != 'at') sectionTitle += settings['inputStopSunriseType'] + ' ' + settings['inputStopType']
        if(checkTimeComplete('stop')) sectionTitle += ' ' + getSunriseTime(settings['inputStopType'],settings['inputStopBefore'],settings['inputStopSunriseType'])
    }
    
    if(settings['inputStartType']) sectionTitle += '</b>'

    section(hideable: true, hidden: hidden, sectionTitle){
        if(settings['inputStartTime'] && settings['inputStartTime'] == settings['inputStopTime']) displayError('You can\'t have the same time to start and stop.')
        if(contactCount > 1 && deviceCount > 1) message = "Scheduling only applies with these $pluralContact. To schedule the devices or default settings for them, use the Time app."
        if(contactCount > 1 && deviceCount == 1) message = "Scheduling only applies with these $pluralContact. To schedule the device or default settings for it, use the Time app."
        if(contactCount == 1 && deviceCount > 1) message = "Scheduling only applies with this $pluralContact. To schedule the devices or default settings for them, use the Time app."
        if(contactCount == 1 && deviceCount == 1) message = "Scheduling only applies with this $pluralContact. To schedule the device or default settings for it, use the Time app."
        displayInfo(message)
        displayStartTypeOption()

        // Display exact time option
        if(settings['inputStartType'] == 'time'){
            displayTimeOption('start')
        } else if(settings['inputStartType']){
            // Display sunrise/sunset type option (at/before/after)
            displaySunriseTypeOption('start')
            // Display sunrise/sunset offset
            if(inputStartSunriseType && inputStartSunriseType != 'at') displaySunriseOffsetOption('start')
        }

        if(checkTimeComplete('start') && settings['inputStartType']){
            displayStopTypeOption()

            // Display exact time option
            if(settings['inputStopType'] == 'time'){
                displayTimeOption('stop')
            } else if(settings['inputStopType']){
                // Display sunrise/sunset type option (at/before/after)
                displaySunriseTypeOption('stop')
                // Display sunrise/sunset offset
                if(inputStopSunriseType && inputStopSunriseType != 'at') displaySunriseOffsetOption('stop')
            }
        }

        displayDaysOption()
        displayMonthsOption()
    }
}

def displayDaysOption(){
    input 'days', 'enum', title: 'On these days (defaults to all days)', multiple: true, width: 12, options: ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday'], submitOnChange:true

    return
}

def displayMonthsOption(){
    input 'months', 'enum', title: 'In these months (defaults to all months)', multiple: true, width: 12, options: ['1': 'January', '2': 'February', '3': 'March', '4': 'April', '5': 'May', '6': 'June', '7': 'July', '8': 'August', '9': 'September', '10': 'October', '11': 'November', '12': 'December'], submitOnChange:true

    return
}

def displayStartTypeOption(){
    if(!checkTimeComplete('start')  || !settings['inputStartType']){
        displayLabel('Schedule starting time')
    } else {
        displayLabel('Schedule start')
    }

    if(settings['inputStartBefore'] && !validateSunriseMinutes(settings['inputStartBefore'])) displayWarning('Time ' + settings["input${ucType}SunriseType"] + ' ' + settings["input${ucType}Type"] + ' is ' + (Math.round(settings["input${ucType}Before"]) / 60) + ' hours. That\'s probably wrong.')

    if(!settings['inputStartType']){
        width = 12
        input 'inputStartType', 'enum', title: 'Start time (click to choose option):', multiple: false, width: width, options: ['time':'Start at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)' ], submitOnChange:true
        displayInfo('Select whether to enter a specific time, or have start time based on sunrise and sunset for the Hubitat location. Required field for a schedule.')
    } else {
        if(settings['inputStartType'] == 'time' || !settings['inputStartSunriseType'] || settings['inputStartSunriseType'] == 'at'){
            width = 6
        } else if(settings['inputStartSunriseType']){
            width = 4
        }
        input 'inputStartType', 'enum', title: 'Start time option:', multiple: false, width: width, options: ['time':'Start at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)'], submitOnChange:true
    }
}

def displayStopTypeOption(){
    if(!checkTimeComplete('stop')){
        displayLabel('Schedule stopping time')
    } else {
        displayLabel('Schedule stop')
    }
    if(settings['inputStopBefore'] && !validateSunriseMinutes(settings['inputStopBefore'])) displayWarning('Time ' + settings["input${ucType}SunriseType"] + ' ' + settings["input${ucType}Type"] + ' is ' + (Math.round(settings["input${ucType}Before"]) / 60) + ' hours. That\'s probably wrong.')

    if(!settings['inputStopType']){
        width = 12
        input 'inputStopType', 'enum', title: 'Stop time (click to choose option):', multiple: false, width: width, options: ['time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)'], submitOnChange:true
    } else {
        if(!settings['inputStopType'] || settings['inputStopType'] == 'none'){
            width = 12
        } else if(settings['inputStopType'] == 'time' || !settings['inputStopSunriseType'] || settings['inputStopSunriseType'] == 'at'){
            width = 6
        } else if(inputStopSunriseType){
            width = 4
        }
        input 'inputStopType', 'enum', title: 'Stop time option:', multiple: false, width: width, options: ['time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)'], submitOnChange:true
    }
}

def displayTimeOption(lcType){
    ucType = lcType.capitalize()
    input "input${ucType}Time", 'time', title: "$ucType time:", width: width, submitOnChange:true
    if(!settings["input${ucType}Time"]) displayInfo("Enter the time to $lcType the schedule in \"hh:mm AM/PM\" format. Required field.")
}

def displaySunriseTypeOption(lcType){
    if(!settings["input${ucType}SunriseType"] || settings["input${ucType}SunriseType"] == 'at') {
        width = 6 
    } else {
        width = 4
    }
    // sunriseTime = getSunriseAndSunset()[settings["input${ucType}Type"]].format("hh:mm a")
    input "input${ucType}SunriseType", 'enum', title: "At, before or after " + settings["input${ucType}Type"] + ":", multiple: false, width: width, options: ["at":"At " + settings["input${ucType}Type"], "before":"Before " + settings["input${ucType}Type"], "after":"After " + settings["input${ucType}Type"]], submitOnChange:true
    if(!settings["input${ucType}SunriseType"]) displayInfo("Select whether to start exactly at " + settings["input${ucType}Type"] + " (currently, $sunriseTime). To allow entering minutes prior to or after " + settings["input${ucType}Type"] + ", select \"Before " + settings["input${ucType}Type"] + "\" or \"After " + settings["input${ucType}Type"] + "\". Required field.")
}

def checkTimeComplete(lcType){
    ucType = lcType.capitalize()

    // If everything entered
    if((settings["input${ucType}Type"] == 'time' && settings["input${ucType}Time"]) || 
       ((settings["input${ucType}Type"] == 'sunrise' || settings["input${ucType}Type"] == 'sunset') && settings["input${ucType}SunriseType"] == 'at') || 
       ((settings["input${ucType}Type"] == 'sunrise' || settings["input${ucType}Type"] == 'sunset') && (settings["input${ucType}SunriseType"] == 'before' || settings["input${ucType}SunriseType"] == 'after') && (settings["input${ucType}Before"]))){
        return true
    } else if(!settings["input${ucType}Type"] && !settings["input${ucType}SunriseType"] && !settings["input${ucType}Before"]){
        return true
    } else {
        return false
    }
}

def getSunriseTime(type,before,sunriseType){
    // If before sunrise, set string to "[number] minutes before sunrise ([time])"
    if(type == 'sunrise' && sunriseType == 'before' && before) return '(' + new Date(parent.getSunrise(before * -1)).format('hh:mm a') + ')'
    // If after sunrise, set string to "[number] minutes after sunrise ([time])"
    if(type == 'sunrise' && sunriseType == 'after' && before) return "(" + new Date(parent.getSunrise(before)).format('hh:mm a') + ")"
    // If before sunset, set string to "[number] minutes before sunset ([time])"
    if(type == 'sunset' && sunriseType == 'before' && before) return "(" + new Date(parent.getSunset(before * -1)).format('hh:mm a') + ")"
    // If after sunrise, set string to "[number] minutes after sunset ([time])"
    if(type == 'sunset' && sunriseType == 'after' && before) return "(" + new Date(parent.getSunset(before)).format('hh:mm a') + ")"
    if(type == 'sunrise' && sunriseType == 'at') return "(" + new Date(parent.getSunrise(0)).format('hh:mm a') + ")"
    if(type == 'sunset' && sunriseType == 'at') return "(" + new Date(parent.getSunset(0)).format('hh:mm a') + ")"
    
}

def getTimeVariables(lcType){
    ucType = lcType.capitalize()
    // If time, then set string to "[time]"
    if(settings["input${ucType}Type"] == 'time') return Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings["input${ucType}Time"]).format('h:mm a', location.timeZone)
        // If sunrise or sunset
    if(settings["input${ucType}SunriseType"]){
        // Set string to "sun[rise/set] ([sunrise/set time])"
        if(settings["input${ucType}SunriseType"] == 'at') return settings["input${ucType}Type"] + " (" + getSunriseAndSunset()[settings["input${ucType}Type"]].format('hh:mm a') + ")"
            // If before sunrise, set string to "[number] minutes before sunrise ([time])"
        if(settings["input${ucType}Type"] == 'sunrise' && settings["input${ucType}SunriseType"] == 'before' && settings["input${ucType}Before"]) return settings["input${ucType}Before"] + " minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset(sunriseOffset: (settings["input${ucType}Before"] * -1), sunsetOffset: 0)[settings["input${ucType}Type"]].format('hh:mm a') + ")"
            // If after sunrise, set string to "[number] minutes after sunrise ([time])"
        if(settings["input${ucType}Type"] == 'sunrise' && settings["input${ucType}SunriseType"] == 'after' && settings["input${ucType}Before"]) return settings["input${ucType}Before"] + " minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset(sunriseOffset: settings["input${ucType}Before"], sunsetOffset: 0)[settings["input${ucType}Type"]].format('hh:mm a') + ")"
        // If before sunset, set string to "[number] minutes before sunset ([time])"
        if(settings["input${ucType}Type"] == 'sunset' && settings["input${ucType}SunriseType"] == 'before' && settings["input${ucType}Before"]) return settings["input${ucType}Before"] + " minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: (settings["input${ucType}Before"] * -1))[settings["input${ucType}Type"]].format('hh:mm a') + ")"
        // If after sunrise, set string to "[number] minutes after sunset ([time])"
        if(settings["input${ucType}Type"] == 'sunset' && settings["input${ucType}SunriseType"] == 'after' && settings["input${ucType}Before"]) return settings["input${ucType}Before"] + " minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: settings["input${ucType}Before"])[settings["input${ucType}Type"]].format('hh:mm a') + ")"
    }
    
    return
}

def displaySunriseOffsetOption(lcType){
    ucType = lcType.capitalize()
    if(!settings["input${ucType}SunriseType"] || settings["input${ucType}SunriseType"] == 'at') return

    //displayWarning(settings["input${ucType}Before"]) / 60)
    input "input${ucType}Before", 'number', title: "Minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + ":", width: 4, submitOnChange:true
    
    if(!settings["input${ucType}Before"] || !validateSunriseMinutes(settings["input${ucType}Before"])) message = "Enter the number of minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " to start the schedule. Required field."
    if(!settings["input${ucType}Before"]) {
        displayInfo(message)
    } else if(!validateSunriseMinutes(settings["input${ucType}Before"])){
        displayWarning(message)
    }
}

def displayChangeModeOption(){
    if(!settings['contactDevice'] || !settings['deviceType'] || !settings['device'] || !settings['openAction'] || !settings['closeAction']) return

    hidden = true
    if((settings['openMode'] || settings['closeMode']) && (!settings['openMode'] || !settings['closeMode'])) hidden = false

    
    sectionTitle = ''
    if(!settings['openMode'] && !settings['closeMode']){
        sectionTitle = 'Click to set Mode change (optional)'
    } else {
        if(settings['openMode']) sectionTitle = '<b>On open, set Mode ' + settings['openMode'] + '</b>'
        if(settings['openMode'] && settings['closeMode']) sectionTitle += '<br>'
        if(settings['closeMode']) sectionTitle += '<b>On close, set Mode ' + settings['closeMode'] + '</b>'
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        input 'openMode', 'mode', title: 'Set Hubitat\'s "Mode" on open?', width: 6, submitOnChange:true
        input 'closeMode', 'mode', title: 'Set Hubitat\'s "Mode" on close?', width: 6, submitOnChange:true
    }
}

def displayIfModeOption(){
    if(!settings['contactDevice']) return
    if(!settings['deviceType']) return
    if(!settings['device']) return
    if(!settings['openAction']) return
    if(!settings['closeAction']) return
    if(settings['openAction'] == 'none' && settings['closeAction'] == 'none') return

    sectionTitle = 'Click to select with what Mode (optional)'
    if(settings['ifMode']) sectionTitle = '<b>Only with Mode: ' + settings['ifMode'] + '</b>'

    section(hideable: true, hidden: true, sectionTitle){
        input 'ifMode', 'mode', title: 'Only run if Mode is already?', width: 12, submitOnChange:true

        message = "This will limit the $pluralContact from running to only when Hubitat\'s Mode is as selected."
        if(settings['ifMode']) message = "This will limit the $pluralContact from running to only when Hubitat\'s Mode is " + settings['ifMode'] + "."

        displayInfo(message)
    }
}

/*
// Presumably if no one is home, the contact sensor wouldn't change
// Don't see the point of requiring everyone to be home
def displayIfPeopleOption(){
    if(!settings["contactDevice"] || !settings["deviceType"] || !settings["device"] || !settings["openAction"] || !settings["closeAction"]) return
    if(!parent.getPresenceDevice(app.label)) return
    if(settings["openAction"] == "none" && settings["closeAction"] == "none") return
    multipleContacts = false
    count = 0
    settings["contactDevice"].each{
        if(count == 1) multipleContacts = true
        count = 1
    }

    if(settings["ifPeople"]){
        sectionTitle = "<b>Only with people: $settings.ifPeople</b>"
    } else {
        sectionTitle = "Click to select with which people (optional)"
    }
    section(hideable: true, hidden: true, sectionTitle){
        input "ifPeople", "enum", title: "Only run if people are present?", width: 12, options: ["everyone":"Everyone present","noone":"Noone present"],submitOnChange:true

        message = "This will limit the contact/door sensor"
        if(multipleContacts) message += "s"
        message += " from running to only when "
        if(settings["ifPeople"]) {
            message += settings["ifPeople"] + " is home"
        } else {
            message += "everyone is home or not"
        }
        message += ". Presence devices are set in the Master app."

        displayInfo(message)
    }
}
*/

def displayAlertOptions(){
    if(!settings['contactDevice']) return
    if(!settings['deviceType']) return
    if(!settings['device']) return
    if(!settings['openAction']) return
    if(!settings['closeAction']) return
    if(!parent.pushNotificationDevice && !parent.speechDevice) return

    hidden = true
    if((settings['pushNotification'] || settings['speech']) && !settings['notificationOpenClose']) hidden = false
    if(peopleError) hidden = false

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
    if((settings['pushNotification'] || settings['speech']) && !settings['personHome'] && !settings['personNotHome']) sectionTitle += moreOptions
    
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
        
        if((settings['pushNotificationDevice'] && settings['pushNotification']) || (settings['speechDevice'] && settings['speech'])){
            input 'notificationOpenClose', 'enum', title: sectionTitle, multiple: false, width: 12, options: ['open': 'Open', 'close': 'Close','both': 'Both open and close'], submitOnChange:true

            if(peopleError) displayError('You can\'t include and exclude the same person.')
            input 'personHome', 'capability.presenceSensor', title: 'Only alert if any of these people are home (Optional)', multiple: true, submitOnChange:true
            input 'personNotHome', 'capability.presenceSensor', title: 'Only alert if none of these people are home (Optional)', multiple: true, submitOnChange:true
            // Move these options to overall?
        }
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
openAction - enum (none, on, off, resume, toggle, lock, or unlock) - Action to perform on device when opened
closeAction - enum (none, on, off, resume, toggle, lock, unlock) - Action to perform on device when closed
openLevel - number (1-100) - Level to set openSwitch when opened
closeLevel - number (1-100) - Level to set closeSwitch when closed
openTemp - number (1800-5400) - Temperature to set openSwitch when opened
closeTemp - number (1800-5400) - Temperature to set closeSwitch when closed
openHue - number (1-100) - Hue to set openSwitch when opened
closeHue - number (1-100) - Hue to set closeSwitch when closed
openSat - number (1-100) - Hue to set openSwitch when opened
closeSat - number (1-100) - Hue to set closeSwitch when closed
days - enum (Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday) - Days on which contact will run. Only displays if scheduleEnable = true
inputStartType - enum (time, sunrise, sunset) - Sets whether start time is a specific time, or based on sunrise or sunset. Only displays if scheduleEnable = true
inputStartTime - time - Start Time (only displays when scheduleEnable = true and inputStartType = "time")
inputStartSunriseType - enum (at, before, after) - Sets whether start time is sunrise/sunset time, or uses positive or negative offset (only displays if scheduleEnable = true, and inputStartType = "sunrise" or "sunset")
inputStartBefore - number - Number of minutes before/after sunrise/sunset for start time (only displays if scheduleEnable = true, inputStartType = "sunrise" or "sunset", and inputStartSunriseType = "before" or "after")
inputStopType - enum (time, sunrise, sunset) - Sets whether stop time is a specific time, or based on sunrise or sunset. (Only displays if scheduleEnable = true)
inputStopTime - time - Stop Time (only displays when scheduleEnable = true and inputStopType = "time")
inputStopSunriseType - enum (at, before, after) - Sets whether start time is sunrise/sunset time, or uses positive or negative offset (only displays if scheduleEnable = true, and inputStartType = "sunrise" or "sunset")
inputStopBefore - number - Number of minutes before/after sunrise/sunset for stop time (only displays if scheduleEnable = true, inputStartType = "sunrise" or "sunset", and inputStartSunriseType = "before" or "bfter")
openWait - number - Minutes to delay open action(s). 
closeWait - number - Minutes to delay close action(s). 
pushNotificationDevice - Device(s) for push notifications
pushNotification - text - Text to push
speechDevice - Device(s) for speech
speech - text - Text to speak.
notificationOpenClose - bool - Switch push notification and speech when door opened, or closed.
openMode - bool - Switch to change mode when door opened, or closed.
closeMode - bool - Switch to change mode when door opened, or closed. 
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
    putLog(1085,'trace','Installed')
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    state.logLevel = getLogLevel()
    putLog(1092,'trace','Updated')
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

    if(settings['disable'] || disableAll) {
        state.disable = true
        return
    } else {
        state.disable = false
    }

    if(!settings['disable'] && !state.disable) {
        subscribe(contactDevice, 'contact.open', contactChange)
        subscribe(contactDevice, 'contact.closed', contactChange)            
    }
    
    setTime()

    putLog(1125,'trace','Initialized')
}

def contactChange(evt){
    if(settings['disable'] || state.disable) return

    putLog(1129,'debug',"Contact sensor $evt.displayName $evt.value")

    // If mode set and node doesn't match, return nulls
    if(settings['ifMode']){
        if(location.mode != settings['ifMode']) {
            putLog(1136,'trace',"Contact disabled, requires mode $ifMode")
            return defaults
        }
    }

    // If not correct day, return nulls
    if(!parent.nowInDayList(settings['days'],app.label)) return
    if(!parent.nowInMonthList(settings['months'],app.label)) return

    // if not between start and stop time, return nulls
    if(atomicState.stop && !parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return

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
        if(settings['openWait']) {
            putLog(1162,'trace','Scheduling runScheduleOpen in ' + settings['openWait'] + ' seconds')
            runIn(settings['openWait'],runScheduleOpen)
            // Otherwise perform immediately
        } else {
            runScheduleOpen()
        }

        // Perform close events (for switches and locks)
    } else if(evt.value == 'closed'){
        // Schedule delay
        if(settings['closeWait']) {
            putLog(1173,'trace','Scheduling runScheduleClose in ' + settings['closeWait'] + ' seconds')
            runIn(settings['closeWait'],runScheduleClose)
            // Otherwise perform immediately
        } else {
            runScheduleClose()
        }
    }

    // Check if people are home (home1 and home2 should be true)
    // If conditions are satified, set home to true
    if(settings['personHome']){
        home1 = false
        settings['personHome'].each{
            if(it.currentPresence == 'present') home1 = true
        }
    } else {
        home1 = true
    }
    if(settings['personNotHome']){
        home2 = true
        settings['personNotHome'].each{
            if(it.currentPresence == 'present') home2 = false
        }
    } else {
        home2 = true
    }
    if(home1 && home2) home = true

    // Text first (just in case there's an error later)
    // Need to move to a function
    if(settings['pushNotificationDevice'] && home && (settings['notificationOpenClose'] == 'both' || (settings['notificationOpenClose'] == 'open' && evt.value == 'open') || (settings['notificationOpenClose'] == 'close' && evt.value == 'closed'))){
        // Only if correct people are home/not home
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
            putLog(1227,'info',"Sent push notice for $evt.displayName $eventName at " + now.format('h:mm a', location.timeZone) + ".")
        } else {
            putLog(1229,'info',"Did not send push notice for $evt.displayName $evt.value due to notification sent $seconds ago.")
        }
    }

    // Give voice alert
    if(settings['speech'] && home && ((!settings['notificationOpenClose'] && evt.value == 'open') || (settings['notificationOpenClose'] && evt.value == 'closed'))) {
        /* ************************************************************************ */
        /* TO-DO: Add option to override speech cooldown period? (Maybe in Master?) */
        /* Same with presence app.                                                  */
        /* ************************************************************************ */
        settings['speech'].each{
            parent.speakSingle(settings['speech'],it,app.label)
        }
    }

    // Set mode
    if(settings['openMode'] && evt.value == 'open'){
        parent.changeMode(settings['openMode'],app.label)
    } else if(settings['closeMode'] && evt.value == 'closed'){
        parent.changeMode(settings['closeMode'],app.label)
    }

}

def runScheduleOpen(){
    if(settings['disable'] || state.disable) return

    if(settings['deviceType'] == 'switch' || settings['deviceType'] == 'light') {
        defaults = [:]

        if(settings['openAction'] == 'on' || settings['openAction'] == 'off' || settings['openAction'] == 'toggle') parent.updateStateMulti(settings['device'],settings['openAction'],app.label)
        if(settings['openLevel']) defaults.'level' = ['startLevel':settings['openLevel'],'appId':'contact']
        if(settings['openTemp']) defaults.'temp' = ['startLevel':settings['openTemp'],'appId':'contact']
        if(settings['openHue']) defaults.'hue' = ['startLevel':settings['openHue'],'appId':'contact']
        if(settings['openSat']) defaults.'sat' = ['startLevel':settings['openSat'],'appId':'contact']
        if(settings['openLevel'] || settings['openTemp'] || settings['openHue'] || settings['openSat']) parent.updateLevelsMulti(settings['device'],defaults,app.label)
        if(settings['openAction'] == 'on' || settings['openAction'] == 'off' || settings['openAction'] == 'toggle' || settings['openLevel'] || settings['openTemp'] || settings['openHue'] || settings['openSat']) parent.setStateMulti(settings['device'],app.label)
        
        // No option to schedule locks (yet); move out of if statement if feature is added
        if(settings['openAction'] == 'resume'){
            defaults = ['level':['time':'resume','appId':'contact'],
                'temp':['time':'resume','appId':'contact'],
                'hue':['time':'resume','appId':'contact'],
                'sat':['time':'resume','appId':'contact']]
         parent.updateLevelsMulti(settings['device'],defaults,app.label)
        }
    } else if(settings['deviceType'] == 'lock') {
        parent.multiLock(settings['openAction'],settings['device'],app.label)
    }
}

def runScheduleClose(){
    if(settings['disable'] || state.disable) return

    if(deviceType == 'switch' || deviceType == 'light') {
        defaults = [:]

        if(settings['closeAction'] == 'on' || settings['closeAction'] == 'off' || settings['closeAction'] == 'toggle') parent.updateStateMulti(settings['device'],settings['closeAction'],app.label)
        if(settings['closeLevel']) defaults.'level' = ['startLevel':settings['closeLevel'],'appId':'contact']
        if(settings['closeTemp']) defaults.'temp' = ['startLevel':settings['closeTemp'],'appId':'contact']
        if(settings['closeHue']) defaults.'hue' = ['startLevel':settings['closeHue'],'appId':'contact']
        if(settings['closeSat']) defaults.'sat' = ['startLevel':settings['closeSat'],'appId':'contact']
        if(settings['closeLevel'] || settings['closeTemp'] || settings['closeHue'] || settings['closeSat']) parent.updateLevelsMulti(settings['device'],defaults,app.label)
        if(settings['closeAction'] == 'on' || settings['closeAction'] == 'off' || settings['closeAction'] == 'toggle' || settings['closeLevel'] || settings['closeTemp'] || settings['closeHue'] || settings['closeSat']) parent.setStateMulti(settings['device'],app.label)

        if(settings['closeAction'] == 'resume') {
                defaults = ['level':['time':'stop','appId':app.id],
                'temp':['time':'stop','appId':app.id],
                'hue':['time':'stop','appId':app.id],
                'sat':['time':'stop','appId':app.id]]
            parent.updateLevelsMulti(settings['device'],defaults,app.label)
        }
    } else if(settings['deviceType'] == 'lock') {
        parent.multiLock(settings['closeAction'],settings['device'],app.label)
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
    if(!settings['inputStartType']) {
        atomicState.start = null
        return
    }
    setTime = setStartStopTime('Start') // Capitalized because used for dynamic variable
    if(setTime){
        atomicState.start = setTime
        putLog(1322,'info','Start time set to ' + parent.normalPrintDateTime(setTime))
        return true
    }
}

def setStopTime(){
    if(!settings['inputStartType'] || settings['inputStopType'] == 'none') {
        atomicState.stop = null
        return
    }
    setTime = setStartStopTime('Stop') // Capitalized because used for dynamic variable
    if(setTime){ 
        if(atomicState.start > setTime) setTime = parent.getTomorrow(setTime,app.label)
        atomicState.stop = setTime
        putLog(1336,'info','Stop time set to ' + parent.normalPrintDateTime(setTime))
    }
    return
}

// Sets atomicState.start and atomicState.stop variables
// Requires type value of "start" or "stop" (must be capitalized to match setting variables)
def setStartStopTime(type){
    if(settings["input${type}Type"] == 'time'){
        returnValue = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSSZ", settings["input${type}Time"]).getTime()
    } else if(settings["input${type}Type"] == 'sunrise'){
        returnValue = (settings["input${type}SunriseType"] == 'before' ? parent.getSunrise(settings["input${type}Before"] * -1,app.label) : parent.getSunrise(settings["input${type}Before"],app.label))
    } else if(settings["input${type}Type"] == "sunset"){
        returnValue = (settings["input${type}SunriseType"] == 'before' ? parent.getSunset(settings["input${type}Before"] * -1,app.label) : parent.getSunset(settings["input${type}Before"],app.label))
    }
    
    return returnValue
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
    if(type == 'error') logMessage += '<font color="red">'
    if(type == 'warn') logMessage += '<font color="brown">'
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
        return true
    }
    return
}
