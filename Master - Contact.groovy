/***********************************************************************************************************************
*
*  Copyright (C) 2020 roguetech
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
*  Version: 0.6.04
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

    // Check for errors, and if so prevent saving
    /// Can't use settings variable before page setup (or functions that use it) (?)
    if((!app.label) ||
       (!contactDevice) ||
       (!deviceType) ||
       (!device) ||
       (inputStartType == "time" && !inputStartTime) ||
       (inputStopType == "time" && !inputStopTime) ||
       ((inputStartType == "sunrise" || inputStartType == "sunset") && !inputStartSunriseType) ||
       ((inputStopType == "sunrise" || inputStopType == "sunset") && !inputStopSunriseType) ||
       ((inputStartSunriseType == "before" || inputStartSunriseType == "after") && !inputStartBefore) ||
       ((inputStopSunriseType == "before" || inputStopSunriseType == "after") && !inputStopBefore) ||
       (openLevel > 100 || closeLevel >100 || (openTemp && openTemp < 1800) || openTemp > 6500 || (closeTemp && closeTemp < 1800) || closeTemp > 6500 || openHue > 100 || closeHue > 100 || openSat > 100 || closeSat > 100) ||
       (speechDevice && !speech) ||
       (pushNotificationDevice && !pushNotification)) noInstall = true

    if(noInstall) {
        allowInstall = false
    } else {
        allowInstall = true
    }

    page(name: "setup", install: allowInstall, uninstall: true) {
        // display Name
        if(!app.label){
            section(){
                displayNameOption()
            }
        } else {
            section(){
                displayNameOption()
                displayDevicesOption()
                displayDevicesTypes()
                displayOpenCloseDevicesOption()
                if(allowInstall) displayDisableOption()
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
            section(){
                if(error) paragraph "$error</div>"
                if(warning) paragraph "$warning</div>"
            }
        }
    }
}

// Display functions


def errorMessage(text){
    if(error){
        error = "$error<br />$errorIcon $text"
    } else {
        error = "<div style=\"background-color:Bisque\">$errorIcon $text"
    }
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
        multipleDevices = false
        count = settings["contactDevice"].size()
        if(count > 1) multipleDevices = true
        if(multipleDevices){
            pluralInput = "Contact/door sensors"
        } else {
            pluralInput = "Contact/door sensor"
        }
        input "contactDevice", "capability.contactSensor", title: "$pluralInput:", multiple: true, submitOnChange:true
    } else {
        input "contactDevice", "capability.contactSensor", title: "Select contact/door sensor(s):", multiple: true, submitOnChange:true
        displayInfo("Select which contact/door sensor(s) for which to set actions.")
    }
}

def displayDevicesTypes(){
    if(!settings["contactDevice"]) return false
    input "deviceType", "enum", title: "Which type of device(s) to control (select one; required)", options: ["lock": "Locks","light": "Lights", "switch": "Switches"], multiple: false, required: true, submitOnChange:true
    if(!settings["deviceType"]) displayInfo("If selecting lights, only dimmable devices will be displayed, but switches include lights. To control both locks and switches/lights, create a separate rule-set for each.")
}

def displayOpenCloseDevicesOption(){
    if(!settings["contactDevice"] || !settings["deviceType"]) return

    multipleDevices = false
    count = 0
    if(settings["device"]){
        settings["device"].each{
            if(count == 1) multipleDevices = true
            count = 1
        }
    }

    if(settings["deviceType"] == "lock"){
        capability = "capability.lock"
        plural = "Locks"
        pluralLc = "locks"
        pluralOptional = "Lock(s)"
    } else if(settings["deviceType"] == "light"){
        capability = "capability.switchLevel"
        plural = "Lights"
        pluralLc = "lights"
        pluralOptional = "Light(s)"
    } else if(settings["deviceType"] == "switch"){
        capability = "capability.switch"
        plural = "Switches"
        pluralLc = "switches"
        pluralOptional = "Switch(es)"
    }

    if(settings["device"]) {
        input "device", "$capability", title: "$plural being controlled:", multiple: true, submitOnChange:true
    } else {
        input "device", "$capability", title: "$pluralOptional to control?", multiple: true, submitOnChange:true
        displayInfo("Select which $pluralLc to control when the contact/door is opened. Required.")
    }

}

def displayDisableOption(){
    if(settings["disable"]){
        input "disable", "bool", title: "<b><font color=\"#000099\">This contact/door sensor is disabled.</font></b> Reenable it?", submitOnChange:true
    } else {
        input "disable", "bool", title: "This contact/door sensor is enabled. Disable it?", submitOnChange:true
    }
}

def displayOpenOptions(){
    if(!settings["contactDevice"] || !settings["device"]) return

    count = 0
    multipleDevices = false
    settings["device"].each{
        if(count == 1) multipleDevices = true
        count = 1
    }

    if(settings["deviceType"] == "lock"){
        deviceString = "lock"
    } else if(settings["deviceType"] == "switch"){
        deviceString = "switch"
    } else if(settings["deviceType"] == "light"){
        deviceString = "light"
    }
    if(multipleDevices){
        if(settings["deviceType"] == "lock" || settings["deviceType"] == "light"){
            deviceString = deviceString + "s"
        } else {
            deviceString = deviceString + "es"
        }
    }


    multipleContacts = false
    count = 0
    settings["contactDevice"].each{
        if(count == 1) multipleContacts = true
        count = 1
    }

    if(multipleContacts){
        contactPlural = "contact/door sensors"
    } else {
        contactPlural = "contact/door sensor"
    }

    if(settings["openAction"]){

        sectionTitle = "<b>When opened: "

        if(settings["openAction"] == "none"){
            action = "Do nothing"
        } else if(settings["openAction"] == "on"){
            action =  "Turn on"
        } else if(settings["openAction"] == "off"){
            action =  "Turn off"
        } else if(settings["openAction"] == "toggle"){
            action =  "Toggle"
        } else if(settings["openAction"] == "resume"){
            action =  "Resume Schedule (or turn off)"
        } else if(settings["openAction"] == "lock"){
            action =  "Lock"
        } else if(settings["openAction"] == "unlock"){
            action =  "Unlock"
        }
        sectionTitle += action

        if(settings["openWait"] && settings["openWait"] > 0) {
            sectionTitle += " after " + settings["openWait"] + " seconds</b>"
        } else {
            sectionTitle += "</b>" + moreOptions
        }

        section(hideable: true, hidden: true, sectionTitle){
            if(settings["deviceType"] == "lock"){
                input "openAction", "enum", title: "When opened, lock or unlock?", multiple: false, width: 12, options: ["none": "Don't lock or unlock","lock": "Lock", "unlock": "Unlock"], submitOnChange:true
            } else {
                input "openAction", "enum", title: "When opened, turn $deviceString on or off?", multiple: false, width: 12, options: ["none": "Don't turn on or off (leave as is)","on": "Turn On", "off": "Turn Off", "toggle": "Toggle", "resume":"Resume Schedule (or turn off)"], submitOnChange:true
            }
            if(settings["openAction"] == "resume") {
                message = "If "
                if(multipleDevices){
                    message += "a $deviceString"
                } else {
                    message += "the $deviceString"
                }
                message += " has an active schedule, the schedule will be enabled when "
                if(multipleContacts){
                    message += "one of the contact/door sensors"
                } else {
                    message += "the contact/door sensor"
                }
                message += " opens. If there are no active schedules, the $deviceString will be turned off. To resume active schedules without turning off, select \"Don't\"."

                displayInfo(message)
            }
            if(settings["openAction"] != "none"){
                input "openWait", "number", title: "Wait seconds after open to $action $deviceString. (Optional)", defaultValue: false, submitOnChange:true

                message = "If device is closed "
                if(settings["openWait"]) {
                    message += "within " + settings["openWait"] + " seconds"
                } else {
                    message += "before time expires"
                }
                message += ", $deviceString will not $action. Instead, it will only "
                if(settings["closeAction"]){

                    if(settings["closeAction"] == "on" || settings["closeAction"] == "off") message += "turn "
                    message += settings["closeAction"] + " with being closed."
                } else {
                    message += "perform action for being closed."
                }
                displayInfo(message)
                if(openWait > 1800) errorMessage("Wait time has been set to " + Math.round(openWait / 60) + " <i>minutes</i>. Is that correct?")
            }
        }
    } else {
        section(){
            if(settings["deviceType"] == "lock"){
                input "openAction", "enum", title: "When opened, lock or unlock?", multiple: false, width: 12, options: ["none": "Don't lock or unlock","lock": "Lock", "unlock": "Unlock"], submitOnChange:true
            } else {
                input "openAction", "enum", title: "When opened, turn $deviceString on or off?", multiple: false, width: 12, options: ["none": "Don't turn on or off (leave as is)","on": "Turn On", "off": "Turn Off", "toggle": "Toggle", "resume":"Resume Schedule (or turn off)"], submitOnChange:true
                message = "Set whether to turn on or off, toggle, or resume schedule when "

                if(multipleContacts) {
                    message += "a "
                } else {
                    message += "the "
                }
                message += "$contactPlural is opened. Select \"Don't\" to control other options, or for opening to do nothing. Toggle will turn the $deviceString on if "
                if(multipleSwitches) {
                    message += "they are off, and turns them off they're on."
                } else {
                    message += "it is off, and turns it off if it's on."
                }
                message += " Required field."
                displayInfo(message)
            }
            input "openWait", "number", title: "Wait seconds after open to take action with $deviceString. (Optional)", defaultValue: false, submitOnChange:true

            message = "If device is closed "
            if(settings["openWait"]) {
                message += "within " + settings["openWait"] + " seconds"
            } else {
                message += "before time expires"
            }
            message += ", $deviceString will not do open action. Instead, it will only "
            if(settings["closeAction"]){
                if(settings["closeAction"] == "on" || settings["closeAction"] == "off") message += "turn "
                message += settings["closeAction"] + " with being closed."
            } else {
                message += "perform action for being closed."
            }
            displayInfo(message)
            if(openWait > 1800) errorMessage("Wait time has been set to " + Math.round(openWait / 60) + " <i>minutes</i>. Is that correct?")
        }
    }
}


def displayCloseOptions(){
    if(!settings["contactDevice"] || !settings["deviceType"] || !settings["device"] || !settings["openAction"]) return

    count = 0
    multipleDevices = false
    settings["device"].each{
        if(count == 1) multipleDevices = true
        count = 1
    }

    if(settings["deviceType"] == "lock"){
        deviceString = "lock"
    } else if(settings["deviceType"] == "switch"){
        deviceString = "switch"
    } else if(settings["deviceType"] == "light"){
        deviceString = "light"
    }
    if(multipleDevices){
        if(settings["deviceType"] == "lock" || settings["deviceType"] == "light"){
            deviceString = deviceString + "s"
        } else {
            deviceString = deviceString + "es"
        }
    }


    multipleContacts = false
    count = 0
    settings["contactDevice"].each{
        if(count == 1) multipleContacts = true
        count = 1
    }

    if(multipleContacts){
        contactPlural = "contact/door sensors"
    } else {
        contactPlural = "contact/door sensor"
    }

    if(settings["closeAction"]){
        sectionTitle = "<b>When closed: "
        if(settings["closeAction"] == "none"){
            action = "Do nothing"
        } else if(settings["closeAction"] == "on"){
            action =  "Turn on"
        } else if(settings["closeAction"] == "off"){
            action =  "Turn off"
        } else if(settings["closeAction"] == "toggle"){
            action =  "Toggle"
        } else if(settings["closeAction"] == "resume"){
            action =  "Resume Schedule (or turn off)"
        } else if(settings["closeAction"] == "lock"){
            action =  "Lock"
        } else if(settings["closeAction"] == "unlock"){
            action =  "Unlock"
        }
        sectionTitle += action

        if(settings["closeWait"] && settings["closeWait"] > 0) {
            sectionTitle += " after " + settings["closeWait"] + " seconds</b>"
        } else {
            sectionTitle += "</b>" + moreOptions
        }

        section(hideable: true, hidden: true, sectionTitle){
            if(settings["deviceType"] == "lock"){
                input "closeAction", "enum", title: "When closed, lock or unlock?", multiple: false, width: 12, options: ["none": "Don't lock or unlock","lock": "Lock", "unlock": "Unlock"], submitOnChange:true
            } else {
                input "closeAction", "enum", title: "When closed, turn $deviceString on or off?", multiple: false, width: 12, options: ["none": "Don't turn on or off (leave as is)","on": "Turn On", "off": "Turn Off", "toggle": "Toggle", "resume":"Resume Schedule (or turn off)"], submitOnChange:true
            }
            if(settings["closeAction"] == "resume") {
                message = "If "
                if(multipleDevices){
                    message += "a $deviceString"
                } else {
                    message += "the $deviceString"
                }
                message += " has an active schedule, the schedule will be enabled when "
                if(multipleContacts){
                    message += "one of the contact/door sensors"
                } else {
                    message += "the contact/door sensor"
                }
                message += " closes. If there are no active schedules, the $deviceString will be turned off. To resume active schedules without turning off, select \"Don't\"."

                displayInfo(message)
            }
            if(settings["closeAction"] != "none"){
                input "closeWait", "number", title: "Wait seconds after close to $action $deviceString. (Optional)", defaultValue: false, submitOnChange:true

                message = "If device is opened "
                if(settings["closeWait"]) {
                    message += "within " + settings["closeWait"] + " seconds"
                } else {
                    message += "before time expires"
                }
                message += ", $deviceString will not $action. Instead, it will only "
                if(settings["openAction"]){

                    if(settings["openAction"] == "on" || settings["openAction"] == "off") message += "turn "
                    message += settings["openAction"] + " with being opened again."
                } else {
                    message += "perform action for being opened."
                }
                displayInfo(message)
                if(openWait > 1800) errorMessage("Wait time has been set to " + Math.round(openWait / 60) + " <i>minutes</i>. Is that correct?")
            }
        }
    } else {
        section(){
            if(settings["deviceType"] == "lock"){
                input "closeAction", "enum", title: "When closed, lock or unlock?", multiple: false, width: 12, options: ["none": "Don't lock or unlock","lock": "Lock", "unlock": "Unlock"], submitOnChange:true
            } else {
                input "closeAction", "enum", title: "When closed, turn $deviceString on or off?", multiple: false, width: 12, options: ["none": "Don't turn on or off (leave as is)","on": "Turn On", "off": "Turn Off", "toggle": "Toggle", "resume":"Resume Schedule (or turn off)"], submitOnChange:true
                message = "Set whether to turn on or off, toggle, or resume schedule when "

                if(multipleContacts) {
                    message += "a "
                } else {
                    message += "the "
                }
                message += "$contactPlural is closed. Select \"Don't\" to control other options, or for opening to do nothing. Toggle will turn the $deviceString on if "
                if(multipleSwitches) {
                    message += "they are off, and turns them off they're on."
                } else {
                    message += "it is off, and turns it off if it's on."
                }
                message += " Required field."
                displayInfo(message)
            }
            if(settings["closeAction"] != "none"){
                input "closeWait", "number", title: "Wait seconds after close to take action with $deviceString. (Optional)", defaultValue: false, submitOnChange:true

                message = "If device is opened "
                if(settings["closeWait"]) {
                    message += "within " + settings["closeWait"] + " seconds"
                } else {
                    message += "before time expires"
                }
                message += ", $deviceString will not do close action. Instead, it will only "
                if(settings["openAction"] == "on" || settings["openAction"] == "off") message += "turn "
                message += settings["openAction"] + " with being opened again."
                displayInfo(message)
                if(openWait > 1800) errorMessage("Wait time has been set to " + Math.round(openWait / 60) + " <i>minutes</i>. Is that correct?")
            }
        }
    }
}

def displayBrightnessOption(){
    if(!settings["contactDevice"] || !settings["deviceType"] || !settings["device"] || !settings["openAction"] || !settings["closeAction"]) return
    if((settings["closeAction"] == "off" || settings["closeAction"] == "resume") && (settings["openAction"] == "off" || settings["openAction"] == "resume")) return
    if(settings["deviceType"] != "light") return

    hidden = true
    // If error, unhide
    if(settings["openLevel"] > 100 || settings["closeLevel"] > 100) hidden = false
    // If open or close settings but not both, unhide
    if((settings["openLevel"] && !settings["closeLevel"]) || (!settings["openLevel"] && settings["closeLevel"])) hidden = false


    if(settings["closeAction"] == "off" || settings["closeAction"] == "resume" || settings["openAction"] == "off" || settings["openAction"] == "resume") {
        width = 12
    } else {
        width = 6
    }

    if(!settings["openLevel"] && !settings["closeLevel"]){
        sectionTitle = "Click to set brightness (optional)"
    } else {
        sectionTitle = "<b>"
        if(settings["openLevel"]){
            sectionTitle += "On open, set brightness to " + settings["openLevel"] + "%"
            if(settings["closeLevel"]) sectionTitle += "<br>"
        }
        if(settings["closeLevel"]){
            sectionTitle += "On close, set brightness to " + settings["closeLevel"] + "%"
        }
        sectionTitle += "</b>"
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        if(settings["openAction"] != "off" && settings["openAction"] != "resume"){
            input "openLevel", "number", title: "Set brightness on open?", width: width, submitOnChange:true
        }

        if(settings["closeAction"] != "off" && settings["closeAction"] != "resume"){
            input "closeLevel", "number", title: "Set brightness on close?", width: width, submitOnChange:true
        }
        if(width == 6) {
            displayInfo("Enter the percentage of brightness of light, from 1 to 100, when opening and/or closing the contact/door. Optional.")
        } else if(settings["openAction"] != "off" && settings["openAction"] != "resume"){
            displayInfo("Enter the percentage of brightness of light, from 1 to 100, when opening the contact/door. Cannot set brightness when turning off or resuming schedule. Optional.")
        } else if(settings["closeAction"] != "off" && settings["closeAction"] != "resume"){
            displayInfo("Enter the percentage of brightness of light, from 1 to 100, when closing the contact/door. Cannot set brightness when turning off or resuming schedule. Optional.")
        }

        if(settings["openLevel"] > 100) errorMessage("Brightness is percentage from 1 to 100. Correct open brightness.")
        if(settings["closeLevel"] > 100) errorMessage("Brightness is percentage from 1 to 100. Correct close brightness.")
    }
}


def displayTemperatureOption(){
    if(!settings["contactDevice"] || !settings["deviceType"] || !settings["device"] || !settings["openAction"] || !settings["closeAction"]) return
    if((settings["closeAction"] == "off" || settings["closeAction"] == "resume") && (settings["openAction"] == "off" || settings["openAction"] == "resume")) return
    if(settings["deviceType"] != "light") return

    hidden = true
    // If error, unhide
    if((settings["openTemp"] && (settings["openTemp"] < 1800 || settings["openTemp"] > 5400)) || (settings["closeTemp"] && (settings["closeTemp"] < 1800 || settings["closeTemp"] > 5400))) hidden = false
    // if open or close settings but not both then unhide
    if((settings["openTemp"] && !settings["closeTemp"]) || (!settings["openTemp"] && settings["closeTemp"])) hidden = false

    if(settings["closeAction"] == "off" || settings["closeAction"] == "resume" || settings["openAction"] == "off" || settings["openAction"] == "resume") {
        width = 12
    } else {
        width = 6
    }

    if(!settings["openTemp"] && !settings["closeTemp"]){
        sectionTitle = "Click to set temperature color (optional)"
    } else {
        sectionTitle = "<b>"
        if(settings["openTemp"]){
            sectionTitle = "On open, set temperature color to " + settings["openTemp"] + "K"
            if(settings["closeTemp"]) sectionTitle += "<br>"
        }
        if(settings["closeTemp"]){
            sectionTitle += "On close, set temperature color to "+ settings["closeTemp"] + "K"
        }
        sectionTitle += "</b>"
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        if(settings["openAction"] != "off" && settings["openAction"] != "resume"){
            input "openTemp", "number", title: "Set temperature color on open?", width: width, submitOnChange:true
        }

        if(settings["closeAction"] != "off" && settings["closeAction"] != "resume"){
            input "closeTemp", "number", title: "Set temperature color on close?", width: width, submitOnChange:true
        }

        if(!settings["openTemp"] && !settings["closeTemp"]) {
            displayInfo("Temperature color is in Kelvin from 1800 to 6500, but range spported by bulbs vary. Lower values have more red, while higher values have more blue, where daylight is 5000, cool white is 4000, and warm white is 3000. Optional.")
        } else {
            displayInfo("Temperature color is from 1800 to 6500, where daylight is 5000, cool white is 4000, and warm white is 3000. Optional.")
        }

        if(settings["openTemp"] && (settings["openTemp"] < 1800 || settings["openTemp"] > 6500)) errorMessage("Temperature color is from 1800 to 5400, where daylight is 5000, warm white is 3000, and cool white is 4000. Correct open temperature.")
        if(settings["closeTemp"] && (settings["closeTemp"] < 1800 || settings["closeTemp"] > 6500)) errorMessage("Temperature color is from 1800 to 5400, where daylight is 5000, warm white is 3000, and cool white is 4000. Correct close temperature.")

    }
}

def displayColorOption(){
    if(!settings["contactDevice"] || !settings["deviceType"] || !settings["device"] || !settings["openAction"] || !settings["closeAction"]) return
    if((settings["closeAction"] == "off" || settings["closeAction"] == "resume") && (settings["openAction"] == "off" || settings["openAction"] == "resume")) return
    if(settings["deviceType"] != "light") return
    
    hiRezHue = parent.getHiRezHue()
    unit = hiRezHue ? "°" : "%"

    hidden = true
    // If error, unhide
    if((settings["openHue"] && settings["openHue"] > 100) || (settings["openSat"] && settings["openSat"] > 100) || (settings["closeHue"] && settings["closeHue"] > 100) || (settings["closeSat"] && settings["closeSat"] > 100)) hidden = false
    // If missing any one value, unhide
    if((settings["openHue"] || settings["openSat"] || settings["closeHue"] || settings["closeSat"]) &&
       (!settings["openHue"] || !settings["openSat"] || !settings["closeHue"] || !settings["closeSat"])) hidden = false

    if(!settings["openHue"] && !settings["closeHue"] && !settings["openSat"] && !settings["closeSat"]){
        sectionTitle = "Click to set color (hue and/or saturation) (optional)"
    } else {
        sectionTitle = "<b>"
        // If just open color
        if(settings["openHue"]) {
            sectionTitle = "On open, set hue to " + settings["openHue"] + "$unit"
            if(settings["openSat"]) sectionTitle += " and sat to " + settings["openSat"] + "%"
        } else if(settings["openSat"]) {
            sectionTitle = "On open, set saturation " + settings["openSat"] + "%"
        }
        if((settings["openHue"] || settings["openSat"]) && (settings["closeHue"] || settings["closeSat"])) {
            sectionTitle += "<br>"
        }
        if(settings["closeHue"]) {
            sectionTitle += "On close, set hue to " + settings["closeHue"] + "$unit"
            if(settings["closeSat"]) sectionTitle += " and sat to " + settings["closeSat"] + "%"
        } else if(settings["closeSat"]) {
            sectionTitle += "On close, set saturation " + settings["closeSat"] + "%"
        }
        sectionTitle += "<b>"
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        if(settings["openAction"] != "off" && settings["openAction"] != "resume"){
            input "openHue", "number", title: "Set hue on open?", width: 6, submitOnChange:true
            input "openSat", "number", title: "Set saturation on open?", width: 6, submitOnChange:true
        }

        if(settings["closeAction"] != "off" && settings["closeAction"] != "resume"){
            input "closeHue", "number", title: "Set hue on close?", width: 6, submitOnChange:true
            input "closeSat", "number", title: "Set saturation on close?", width: 6, submitOnChange:true
        }
        if(hiRezHue){
            displayInfo("Hue is degrees around a color wheel, where red is 0 (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270 (may vary by device). Optional")
        } else {
            displayInfo("Hue is percent around a color wheel, where red is 0 (and 100). Orange = 8; yellow = 16; green = 33; turquiose = 50; blue = 66; purple = 79 (may vary by device). Optional")
        }
        displayInfo("Saturation is the percentage depth of color, from 1 to 100, where 1 is hardly any color tint and 100 is full color. Optional.")


        if(settings["openHue"] > 100 && !hiRezHue) errorMessage("Hue can't be more then 100. Correct open hue.")
        if(settings["openHue"] > 360 && hiRezHue) errorMessage("Hue can't be more then 360. Correct open hue.")
        if(settings["openSat"] > 100) errorMessage("Saturation can't be more then 100. Correct open saturation.")
        if(settings["closeHue"] > 100 && !hiRezHue) errorMessage("Hue can't be more then 100. Correct close hue.")
        if(settings["closeHue"] > 360 && hiRezHue) errorMessage("Hue can't be more then 360. Correct close hue.")
        if(settings["closeSat"] > 100) errorMessage("Saturation can't be more then 100. Correct close saturation.")
    }
}

def displayScheduleSection(){
    if(!settings["contactDevice"] || !settings["deviceType"] || !settings["device"] || !settings["openAction"] || !settings["closeAction"]) return

    helpTip = "Scheduling only applies with "
    if(multipleContacts) {
        helpTip += "these contact sensors"
                         } else {
        helpTip += "this contact sensor"
    }
    if(multipleDevices){
        helpTip += ". To schedule the devices or default settings for them, use the Time app."
    } else {
        helpTip += ". To schedule the device or default settings for it, use the Time app."
    }
        
    
    // If only days entered
    sectionTitle="<b>"
        List dayList=[]
        settings["days"].each{
            dayList.add(it)
        }
        dayText = dayList.join(", ")
    if(!settings["inputStartType"] && !settings["inputStopType"] && settings["days"]){
        sectionTitle += "Only on: " + dayText + "</b>" + moreOptions
        hidden = true
        // If only start time (and days) entered
    }  else if(checkTimeComplete("start") && settings["inputStartType"] && (!checkTimeComplete("stop") || !settings["inputStopType"])){
        sectionTitle = "Beginning at $varStartTime"
        if(settings["days"]) sectionTitle += " on: " + dayText
        if(settings["months"]) sectionTitle += "; in " + monthText
        sectionTitle += "</b>"
        hidden = false
        // If only stop time (and day) entered
    } else if(checkTimeComplete("stop") && settings["inputStopType"] && (!checkTimeComplete("start") || !settings["inputStartType"])){
        sectionTitle = "Ending at $varStopTime"
        if(settings["days"]) sectionTitle += " on: " + dayText
        if(settings["months"]) sectionTitle += "; in " + monthText
        sectionTitle += "</b>"
        hidden = false
        // If all options entered
    } else if(checkTimeComplete("start") && checkTimeComplete("stop") && settings["inputStartType"] && settings["inputStopType"]){
        varStartTime = getTimeVariables("start")
        varStopTime = getTimeVariables("stop")
        sectionTitle = "<b>Only if between $varStartTime and $varStopTime"
        if(settings["days"] && settings["months"]) {
            sectionTitle += "</b>"
        } else {
            sectionTitle += " on: " + dayText + "</b>"
            if(settings["months"]) sectionTitle += "; in " + monthText
            sectionTitle += "</b>"
        }
        hidden = true
        // If no options are entered
    } else {
        sectionTitle = "Click to set schedule (optional)"
        hidden = true
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        displayInfo(helpTip)
        displayStartTypeOption()

        // Display exact time option
        if(settings["inputStartType"] == "time"){
            displayTimeOption("start")
        } else if(settings["inputStartType"]){
            // Display sunrise/sunset type option (at/before/after)
            displaySunriseTypeOption("start")
            displaySunriseOffsetOption("start")
            // Display sunrise/sunset offset
            if(inputStartSunriseType && inputStartSunriseType != "at") displaySunriseOffsetOption("start")
        }

        if(checkTimeComplete("start") && settings["inputStartType"]){
            displayStopTypeOption()

            // Display exact time option
            if(settings["inputStopType"] == "time"){
                displayTimeOption("stop")
            } else if(settings["inputStopType"]){
                // Display sunrise/sunset type option (at/before/after)
                displaySunriseTypeOption("stop")
                displaySunriseOffsetOption("stop")
                // Display sunrise/sunset offset
                if(inputStartSunriseType != "at") displaySunriseOffsetOption("stop")
            }
        }

        displayDaysOption(dayText)
        displayMonthsOption(monthText)


        displayInfo(message)
    }
}

def displayDaysOption(dayText){
    input "days", "enum", title: "On these days (defaults to all days)", multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"], submitOnChange:true

    return
}

def displayMonthsOption(monthText){
    input "months", "enum", title: "In these months (defaults to all months)", multiple: true, width: 12, options: ["1": "January", "2": "February", "3": "March", "4": "April", "5": "May", "6": "June", "7": "July", "8": "August", "9": "September", "10": "October", "11": "November", "12": "December"], submitOnChange:true

    return
}

def displayStartTypeOption(){
    if(!checkTimeComplete("start")  || !settings["inputStartType"]){
        displayLabel("Schedule starting time")
    } else {
        displayLabel("Schedule start")
    }
    if(!settings["inputStartType"]){
        width = 12
        input "inputStartType", "enum", title: "Start time (click to choose option):", multiple: false, width: width, options: ["time":"Start at specific time", "sunrise":"Sunrise (at, before or after)","sunset":"Sunset (at, before or after)" ], submitOnChange:true
        displayInfo("Select whether to enter a specific time, or have start time based on sunrise and sunset for the Hubitat location. Required field for a schedule.")
    } else {
        if(settings["inputStartType"] == "time" || !settings["inputStartSunriseType"] || settings["inputStartSunriseType"] == "at"){
            width = 6
        } else if(settings["inputStartSunriseType"]){
            width = 4
        }
        input "inputStartType", "enum", title: "Start time option:", multiple: false, width: width, options: ["time":"Start at specific time", "sunrise":"Sunrise (at, before or after)","sunset":"Sunset (at, before or after)" ], submitOnChange:true
    }
}

def displayStopTypeOption(){
    if(!checkTimeComplete("stop")){
        displayLabel("Schedule stopping time")
    } else {
        displayLabel("Schedule stop")
    }
    if(!settings["inputStopType"]){
        width = 12
        input "inputStopType", "enum", title: "Stop time (click to choose option):", multiple: false, width: width, options: ["time":"Stop at specific time", "sunrise":"Sunrise (at, before or after)","sunset":"Sunset (at, before or after)" ], submitOnChange:true
    } else {
        if(!settings["inputStopType"] || settings["inputStopType"] == "none"){
            width = 12
        } else if(settings["inputStopType"] == "time" || !settings["inputStopSunriseType"] || settings["inputStopSunriseType"] == "at"){
            width = 6
        } else if(inputStopSunriseType){
            width = 4
        }
        input "inputStopType", "enum", title: "Stop time option:", multiple: false, width: width, options: ["time":"Stop at specific time", "sunrise":"Sunrise (at, before or after)","sunset":"Sunset (at, before or after)" ], submitOnChange:true
    }
}

def displayTimeOption(lcType){
    ucType = lcType.capitalize()
    input "input${ucType}Time", "time", title: "$ucType time:", width: width, submitOnChange:true
    if(!settings["input${ucType}Time"]) displayInfo("Enter the time to $lcType the schedule in \"hh:mm AM/PM\" format. Required field.")
}

def displaySunriseTypeOption(lcType){
    if(!settings["input${ucType}SunriseType"] || settings["input${ucType}SunriseType"] == "at") {
        width = 6 
    } else {
        width = 4
    }
    // sunriseTime = getSunriseAndSunset()[settings["input${ucType}Type"]].format("hh:mm a")
    input "input${ucType}SunriseType", "enum", title: "At, before or after " + settings["input${ucType}Type"] + ":", multiple: false, width: width, options: ["at":"At " + settings["input${ucType}Type"], "before":"Before " + settings["input${ucType}Type"], "after":"After " + settings["input${ucType}Type"]], submitOnChange:true
    if(!settings["input${ucType}SunriseType"]) displayInfo("Select whether to start exactly at " + settings["input${ucType}Type"] + " (currently, $sunriseTime). To allow entering minutes prior to or after " + settings["input${ucType}Type"] + ", select \"Before " + settings["input${ucType}Type"] + "\" or \"After " + settings["input${ucType}Type"] + "\". Required field.")
}

def checkTimeComplete(lcType){
    ucType = lcType.capitalize()

    // If everything entered
    if((settings["input${ucType}Type"] == "time" && settings["input${ucType}Time"]) || 
       ((settings["input${ucType}Type"] == "sunrise" || settings["input${ucType}Type"] == "sunset") && settings["input${ucType}SunriseType"] == "at") || 
       ((settings["input${ucType}Type"] == "sunrise" || settings["input${ucType}Type"] == "sunset") && (settings["input${ucType}SunriseType"] == "before" || settings["input${ucType}SunriseType"] == "after") && (settings["input${ucType}Before"]))){
        return true
    } else if(!settings["input${ucType}Type"] && !settings["input${ucType}SunriseType"] && !settings["input${ucType}Before"]){
        return true
    } else {
        return false
    }
}

def getTimeVariables(lcType){
    ucType = lcType.capitalize()
    // If time, then set string to "[time]"
    if(settings["input${ucType}Type"] == "time"){
        return Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings["input${ucType}Time"]).format("h:mm a", location.timeZone)
        // If sunrise or sunset
    } else if((settings["input${ucType}Type"] == "sunrise" || settings["input${ucType}Type"] == "sunset")  && settings["input${ucType}SunriseType"]){
        if(settings["input${ucType}SunriseType"] == "at"){
            // Set string to "sun[rise/set] ([sunrise/set time])"
            return settings["input${ucType}Type"] + " (" + getSunriseAndSunset()[settings["input${ucType}Type"]].format("hh:mm a") + ")"
            // If before sunrise
        } else if(settings["input${ucType}Type"] == "sunrise" && settings["input${ucType}SunriseType"] == "before" && settings["input${ucType}Before"]){
            // Set string to "[number] minutes before sunrise ([time])
            if(settings["input${ucType}Before"]) return settings["input${ucType}Before"] + " minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset(sunriseOffset: (settings["input${ucType}Before"] * -1), sunsetOffset: 0)[settings["input${ucType}Type"]].format("hh:mm a") + ")"
            // If after sunrise
        } else if(settings["input${ucType}Type"] == "sunrise" && settings["input${ucType}SunriseType"] == "after" && settings["input${ucType}Before"]){
            // Set string to "[number] minutes after sunrise ([time])
            if(settings["input${ucType}Before"]) return settings["input${ucType}Before"] + " minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset(sunriseOffset: settings["input${ucType}Before"], sunsetOffset: 0)[settings["input${ucType}Type"]].format("hh:mm a") + ")"
            // If before sunset
        } else if(settings["input${ucType}Type"] == "sunset" && settings["input${ucType}SunriseType"] == "before" && settings["input${ucType}Before"]){
            // Set string to "[number] minutes before sunset ([time])
            if(settings["input${ucType}Before"]) return settings["input${ucType}Before"] + " minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: (settings["input${ucType}Before"] * -1))[settings["input${ucType}Type"]].format("hh:mm a") + ")"
            // If after sunrise
        } else if(settings["input${ucType}Type"] == "sunset" && settings["input${ucType}SunriseType"] == "after" && settings["input${ucType}Before"]){
            // Set string to "[number] minutes after sunset ([time])
            if(settings["input${ucType}Before"]) return settings["input${ucType}Before"] + " minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " (" + getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: settings["input${ucType}Before"])[settings["input${ucType}Type"]].format("hh:mm a") + ")"
        } else {
            return
        }
    } else {
        return
    }
}

def displaySunriseOffsetOption(lcType){
    ucType = lcType.capitalize()
    if(!settings["input${ucType}SunriseType"] || settings["input${ucType}SunriseType"] == "at") return

    if(settings["input${ucType}Before"] && settings["input${ucType}Before"] > 1441){
        // "Minues [before/after] [sunrise/set] is equal to "
        message = "Minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " is equal to "
        if(settings["input${ucType}Before"]  > 2881){
            // "X days"
            message += Math.floor(settings["input${ucType}Before"]  / 60 / 24) + " days."
        } else {
            message += "a day."
        }
        warningMessage(message)
    }
    input "input${ucType}Before", "number", title: "Minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + ":", width: 4, submitOnChange:true
    if(!settings["input${ucType}Before"]) displayInfo("Enter the number of minutes " + settings["input${ucType}SunriseType"] + " " + settings["input${ucType}Type"] + " to start the schedule. Required field.")
}

def displayChangeModeOption(){
    if(!settings["contactDevice"] || !settings["deviceType"] || !settings["device"] || !settings["openAction"] || !settings["closeAction"]) return

    hidden = true
    if((settings["openMode"] || settings["closeMode"]) && (!settings["openMode"] || !settings["closeMode"])) hidden = false

    if(!settings["openMode"] && !settings["closeMode"]){
        sectionTitle = "Click to set Mode change (optional)"
    } else {
        sectionTitle = "<b>"
        if(settings["openMode"]) {
            sectionTitle = "On open, set Mode " + settings["openMode"]
            if(settings["closeMode"]) sectionTitle += "<br>"
        }

        if(settings["closeMode"]) {
            sectionTitle += "On close, set Mode " + settings["closeMode"]
        }
        sectionTitle += "</b>"
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        input "openMode", "mode", title: "Set Hubitat's \"Mode\" (on open)?", width: 6, submitOnChange:true
        input "closeMode", "mode", title: "Set Hubitat's \"Mode\" (on close)?", width: 6, submitOnChange:true
    }
}

def displayIfModeOption(){
    if(!settings["contactDevice"] || !settings["deviceType"] || !settings["device"] || !settings["openAction"] || !settings["closeAction"]) return
    if(settings["openAction"] == "none" && settings["closeAction"] == "none") return
    multipleContacts = false
    count = 0
    settings["contactDevice"].each{
        if(count == 1) multipleContacts = true
        count = 1
    }

    if(settings["ifMode"]){
        sectionTitle = "<b>Only with Mode: $settings.ifMode</b>"
    } else {
        sectionTitle = "Click to select with what Mode (optional)"
    }
    section(hideable: true, hidden: true, sectionTitle){
        input "ifMode", "mode", title: "Only run if Mode is already?", width: 12, submitOnChange:true

        message = "This will limit the contact/door sensor"
        if(multipleContacts) message += "s"
        message += " from running to only when Hubitat's Mode is "
        if(settings["ifMode"]) {
            message += settings["ifMode"]
        } else {
            message += "as selected."
        }

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
    if(!settings["contactDevice"] || !settings["deviceType"] || !settings["device"] || !settings["openAction"] || !settings["closeAction"]) return
    if(!parent.pushNotificationDevice && !parent.speechDevice) return

    hidden = true

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
            settings["pushNotificationDevice"] = [:]
            settings["pushNotificationDevice"][pushDeviceName] = pushDeviceId
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
            settings["speechDevice"] = [:]
            settings["speechDevice"][speechDeviceName] = speechDeviceId
        }
    }

    sectionTitle = ""
    if(notificationOpenClose) {
        action = "<b>On close"
    } else {
        action = "<b>On open"
    }

    if(!settings["speech"] && !settings["pushNotification"]){
        sectionTitle = "Click to set notifications (optional)"
    } else if((settings["pushNotification"] && !settings["pushNotificationDevice"] && countPushDevices > 1) ||
              (settings["speech"] && !settings["speechDevice"] && countSpeechDevices > 1) ||
              (settings["pushNotificationDevice"] && countPushDevices > 1 && !settings["pushNotification"]) ||
              (settings["speechDevice"] && countSpeechDevices > 1 && !settings["speech"])) {
        sectionTitle = "Click to set notifications (optional)"
        hidden = false
        // Notification entered
    } else {
        if(settings["pushNotificationDevice"] && settings["pushNotification"]){
            sectionTitle = "$action, send push notification"
            if(settings["speechDevice"] && settings["speech"]) sectionTitle += "<br>"
        }
        if(settings["speechDevice"] && settings["speech"]){
            sectionTitle = "$action, text-to-speech announcement"
        }
        sectionTitle += "</b>"
        if(!settings["speech"] || !settings["pushNotification"] || !settings["personHome"] || !settings["personNotHome"])
        sectionTitle += moreOptions
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        if(parent.pushNotificationDevice){
            if(countPushDevices > 1)
            input "pushNotificationDevice", "enum", title: "Push notification device(s)?", options: state.pushFilteredList, multiple: true, submitOnChange: true

            input "pushNotification", "text", title: "Text of push notification to send?", submitOnChange:true
            if(countPushDevices == 1) displayInfo("Push notifications will use the device \"$pushDeviceName\". To use other(s), add it in the Master app.")
        }

        if(parent.speechDevice){
            if(countSpeechDevices > 1) 
            input "speechDevice", "enum", title: "Text-to-speech device to use", options: state.speechFilteredList, multiple: true, submitOnChange: true

            input "speech", "text", title: "Text-to-speech announcement?", submitOnChange:true
            if(countSpeechDevices == 1) displayInfo("Text-to-speech will use the device \"$speechDeviceName\". To use other(s), add it in the Master app.")
        }

        if((parent.pushNotificationDevice && settings["pushNotification"]) || (parent.speechDevice && settings["speech"])){
            if(settings["notificationOpenClose"]){
                input "notificationOpenClose", "bool", title: "Speak when <b>closed</b>. Click for opened.", submitOnChange:true
            } else {
                input "notificationOpenClose", "bool", title: "Speak when <b>opened</b>. Click for closed.", submitOnChange:true
            }
        }

        if((settings["pushNotificationDevice"] && settings["pushNotification"]) || (settings["speechDevice"] && settings["speech"])){
            input "personHome", "capability.presenceSensor", title: "Only alert if any of these people are home", multiple: true, submitOnChange:true
            input "personNotHome", "capability.presenceSensor", title: "Only alert if none of these people are home", multiple: true, submitOnChange:true
        }

    }
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
    putLog(1141,"trace","Installed")
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    state.logLevel = getLogLevel()
    putLog(1148,"trace","Updated")
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

    if(settings["disable"] || disableAll) {
        state.disable = true
        return
    } else {
        state.disable = false
    }

    if(!settings["disable"] && !state.disable) {
        subscribe(contactDevice, "contact.open", contactChange)
        subscribe(contactDevice, "contact.closed", contactChange)            
    }

    putLog(1179,"trace","Initialized")
}

// Temp function; can be removed from production
def getScheduleActive(){
    return "testing"
}
def contactChange(evt){
    if(settings["disable"] || state.disable) return

    putLog(1189,"debug","Contact sensor $evt.displayName $evt.value")

    // If mode set and node doesn't match, return nulls
    if(settings["ifMode"]){
        if(location.mode != settings["ifMode"]) {
            putLog(1194,"trace","Contact disabled, requires mode $ifMode")
            return defaults
        }
    }

    // If not correct day, return nulls
    if(!parent.nowInDayList(settings["days"],app.label)) return
    if(!parent.nowInMonthList(settings["months"],app.label)) return

    if(settings["inputStartType"] && settings["inputStopType"]) {
        setTime()
    } else {
        atomicState.stop = null
        atomicState.start = null
    }

    // if not between start and stop time, return nulls
    if(atomicState.stop && !parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return

    // Unschedule pevious events

    // If opened a second time, it will reset delayed action
    // If closed a second time, it won't override open
    if(evt.value == "open"){
        unschedule()
    } else {
        unschedule(runScheduleClose)
    }

    // Perform open events (for switches and locks)
    if(evt.value == "open"){
        // Schedule delay
        if(settings["openWait"]) {
            putLog(1226,"trace","Scheduling runScheduleOpen in "+ settings["openWait"] + " seconds")
            runIn(settings["openWait"],runScheduleOpen)
            // Otherwise perform immediately
        } else {
            runScheduleOpen()
        }

        // Perform close events (for switches and locks)
    } else if(evt.value == "closed"){
        // Schedule delay
        if(settings["closeWait"]) {
            putLog(1237,"trace","Scheduling runScheduleClose in " + settings["closeWait"] + " seconds")
            runIn(settings["closeWait"],runScheduleClose)
            // Otherwise perform immediately
        } else {
            runScheduleClose()
        }
    }

    // Check if people are home (home1 and home2 should be true)
    // If conditions are satified, set home to true
    if(settings["personHome"]){
        home1 = false
        settings["personHome"].each{
            if(it.currentPresence == "present") home1 = true
        }
    } else {
        home1 = true
    }
    if(settings["personNotHome"]){
        home2 = true
        settings["personNotHome"].each{
            if(it.currentPresence == "present") home2 = false
        }
    } else {
        home2 = true
    }
    if(home1 && home2) home = true

    // Text first (just in case there's an error later)
    if(settings["pushNotificationDevice"] && home && ((!settings["notificationOpenClose"] && evt.value == "open") || (settings["notificationOpenClose"] && evt.value == "closed"))){
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

            if(evt.value == "open") {
                eventName = "opened"
            } else {
                eventName = evt.value
            }
            settings["pushNotificationDevice"].each{
                parent.sendPushNotification(it,"$evt.displayName was $eventName at " + now.format("h:mm a", location.timeZone),app.label)
            }
            putLog(1291,"info","Sent push notice for $evt.displayName $eventName at " + now.format("h:mm a", location.timeZone) + ".")
        } else {
            putLog(1293,"info","Did not send push notice for $evt.displayName $evt.value due to notification sent $seconds ago.")
        }
    }

    // Give voice alert
    if(settings["speech"] && home && ((!settings["notificationOpenClose"] && evt.value == "open") || (settings["notificationOpenClose"] && evt.value == "closed"))) {
        /* ************************************************************************ */
        /* TO-DO: Add option to override speech cooldown period? (Maybe in Master?) */
        /* Same with presence app.                                                  */
        /* ************************************************************************ */
        settings["speech"].each{
            parent.speakSingle(settings["speech"],it,app.label)
        }
    }

    // Set mode
    if(settings["openMode"] && evt.value == "open"){
        parent.changeMode(settings["openMode"],app.label)
    } else if(settings["closeMode"] && evt.value == "closed"){
        parent.changeMode(settings["closeMode"],app.label)
    }

}

def runScheduleOpen(){
    if(settings["disable"] || state.disable) return

    if(settings["deviceType"] == "switch" || settings["deviceType"] == "light") {
        defaults = [:]

        if(settings["openAction"] == "on" || settings["openAction"] == "off" || settings["openAction"] == "toggle") parent.updateStateMulti(settings["device"],settings["openAction"],app.label)
        if(settings["openLevel"]) defaults."level" = ["startLevel":settings["openLevel"],"appId":"contact"]
        if(settings["openTemp"]) defaults."temp" = ["startLevel":settings["openTemp"],"appId":"contact"]
        if(settings["openHue"]) defaults."hue" = ["startLevel":settings["openHue"],"appId":"contact"]
        if(settings["openSat"]) defaults."sat" = ["startLevel":settings["openSat"],"appId":"contact"]
        if(settings["openLevel"] || settings["openTemp"] || settings["openHue"] || settings["openSat"]) parent.updateLevelsMulti(settings["device"],defaults,app.label)
        if(settings["openAction"] == "on" || settings["openAction"] == "off" || settings["openAction"] == "toggle" || settings["openLevel"] || settings["openTemp"] || settings["openHue"] || settings["openSat"]) parent.setStateMulti(settings["device"],app.label)
        
        // No option to schedule locks (yet); move out of if statement if feature is added
        if(settings["openAction"] == "resume"){
            defaults = ["level":["time":"resume","appId":"contact"],
                "temp":["time":"resume","appId":"contact"],
                "hue":["time":"resume","appId":"contact"],
               "sat":["time":"resume","appId":"contact"]]
         parent.updateLevelsMulti(settings["device"],defaults,app.label)
        }
    } else if(settings["deviceType"] == "lock") {
        parent.multiLock(settings["openAction"],settings["device"],app.label)
    }
}

def runScheduleClose(){
    if(settings["disable"] || state.disable) return

    if(deviceType == "switch" || deviceType == "light") {
        defaults = [:]

        if(settings["closeAction"] == "on" || settings["closeAction"] == "off" || settings["closeAction"] == "toggle") parent.updateStateMulti(settings["device"],settings["closeAction"],app.label)
        if(settings["closeLevel"]) defaults."level" = ["startLevel":settings["closeLevel"],"appId":"contact"]
        if(settings["closeTemp"]) defaults."temp" = ["startLevel":settings["closeTemp"],"appId":"contact"]
        if(settings["closeHue"]) defaults."hue" = ["startLevel":settings["closenHue"],"appId":"contact"]
        if(settings["closeSat"]) defaults."sat" = ["startLevel":settings["closeSat"],"appId":"contact"]
        if(settings["closeLevel"] || settings["closeTemp"] || settings["closeHue"] || settings["closeSat"]) parent.updateLevelsMulti(settings["device"],defaults,app.label)
        if(settings["closeAction"] == "on" || settings["closeAction"] == "off" || settings["closeAction"] == "toggle" || settings["closeLevel"] || settings["closeTemp"] || settings["closeHue"] || settings["closeSat"]) parent.setStateMulti(settings["device"],app.label)

        if(settings["closeAction"] == "resume") {
                defaults = ["level":["time":"stop","appId":app.id],
                "temp":["time":"stop","appId":app.id],
                "hue":["time":"stop","appId":app.id],
               "sat":["time":"stop","appId":app.id]]
            parent.updateLevelsMulti(settings["device"],defaults,app.label)
        }
    } else if(settings["deviceType"] == "lock") {
        parent.multiLock(settings["closeAction"],settings["device"],app.label)
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
    if(!settings["inputStartType"]) return
    setTime = setStartStopTime("Start") // Capitalized because used for dynamic variable
    if(setTime){
        atomicState.start = setTime
        putLog(1382,"info","Start time set to " + parent.normalPrintDateTime(setTime))
        return true
    }
}

def setStopTime(){
    if(!settings["inputStartType"] || settings["inputStopType"] == "none") return
    setTime = setStartStopTime("Stop") // Capitalized because used for dynamic variable
    if(setTime){ 
        if(atomicState.start > setTime) setTime = parent.getTomorrow(setTime,app.label)
        atomicState.stop = setTime
        putLog(1393,"info","Stop time set to " + parent.normalPrintDateTime(setTime))
    }
    return
}

// Sets atomicState.start and atomicState.stop variables
// Requires type value of "start" or "stop" (must be capitalized to match setting variables)
def setStartStopTime(type){
    if(settings["input${type}Type"] == "time"){
        returnValue = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSSZ", settings["input${type}Time"]).getTime()
    } else if(settings["input${type}Type"] == "sunrise"){
        returnValue = (settings["input${type}SunriseType"] == "before" ? parent.getSunrise(settings["input${type}Before"] * -1,app.label) : parent.getSunrise(settings["input${type}Before"],app.label))
    } else if(settings["input${type}Type"] == "sunset"){
        returnValue = (settings["input${type}SunriseType"] == "before" ? parent.getSunset(settings["input${type}Before"] * -1,app.label) : parent.getSunset(settings["input${type}Before"],app.label))
    }
    
    return returnValue
}

def checkLog(type = null){
    if(!state.logLevel) getLogLevel()
    switch(type) {
        case "error":
        if(state.logLevel > 0) return true
        break
        case "warn":
        if(state.logLevel > 1) return true
        break
        case "info":
        if(state.logLevel > 2) return true
        break
        case "trace":
        if(state.logLevel > 3) return true
        break
        case "debug":
        if(state.logLevel == 5) return true
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,type = "trace",message = null){
    if(!checkLog(type)) return
    logMessage = ""
    if(type == "error") logMessage += "<font color=\"red\">"
    if(type == "warn") logMessage += "<font color=\"brown\">"
    logMessage += "$app.label "
    if(lineNumber) logMessage += "(line $lineNumber) "
    if(message) logMessage += "-- $message"
    if(type == "error" || type == "warn") logMessage += "</font>"
    switch(type) {
        case "error":
        log.error(logMessage)
        return true
        case "warn":
        log.warn(logMessage)
        return true
        case "info":
        log.info(logMessage)
        return true
        case "trace":
        log.trace(logMessage)
        return true
        case "debug":
        log.debug(logMessage)
        return true
    }
    return
}
