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
*  Version: 0.5.2
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
/* TO-DO: Change icon from to to something  that resembles a door I guess.  */
/* ************************************************************************ */
/* ************************************************************************ */
/* TO-DO: Finish error messages.                                            */
/* ************************************************************************ */

// logLevel sets number of log messages
// 0 for none
// 1 for errors only
// 5 for all
def getLogLevel(){
    return 5
}


//
//
//
// errors aren't displaying
//
//
//

preferences {
    infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
    errorIcon = "<img src=\"http://emily-john.love/icons/error.png\" width=20 height=20>"
    warningIcon = "<img src=\"http://emily-john.love/icons/warning.png\" width=20 height=20>"

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
            displayDaysOption()
            displayIfModeOption()
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
        count = 0
        settings["contactDevice"].each{
            if(count == 1) multipleDevices = true
            count = 1
        }
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
    if(disable){
        input "disable", "bool", title: "<b><font color=\"#000099\">This schedule is disabled.</font></b> Reenable it?", submitOnChange:true
    } else {
        input "disable", "bool", title: "This schedule is enabled. Disable it? (You must click \"Done\" for change to take affect.)", submitOnChange:true
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

        title = "When opened: "

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
        title = title + action

        if(settings["openWait"] && settings["openWait"] > 0) title = title + " after " + settings["openWait"] + " seconds"
        section(hideable: true, hidden: true, "$title"){
            if(settings["deviceType"] == "lock"){
                input "openAction", "enum", title: "When opened, lock or unlock?", multiple: false, width: 12, options: ["none": "Don't lock or unlock","lock": "Lock", "unlock": "Unlock"], submitOnChange:true
            } else {
                input "openAction", "enum", title: "When opened, turn $deviceString on or off?", multiple: false, width: 12, options: ["none": "Don't turn on or off (leave as is)","on": "Turn On", "off": "Turn Off", "toggle": "Toggle", "resume":"Resume Schedule (or turn off)"], submitOnChange:true
            }
            if(settings["openAction"] == "resume") {
                message = "If "
                if(multipleDevices){
                    message = message + "a $deviceString"
                } else {
                    message = message + "the $deviceString"
                }
                message = message + " has an active schedule, the schedule will be enabled when "
                if(multipleContacts){
                    message = message + "one of the contact/door sensors"
                } else {
                    message = message + "the contact/door sensor"
                }
                message = message + " opens. If there are no active schedules, the $deviceString will be turned off. To resume active schedules without turning off, select \"Don't\"."

                displayInfo(message)
            }
            if(settings["openAction"] != "none"){
                input "openWait", "number", title: "Wait seconds after open to $action $deviceString. (Optional)", defaultValue: false, submitOnChange:true

                message = "If device is closed "
                if(settings["openWait"]) {
                    message = message + "within " + settings["openWait"] + " seconds"
                } else {
                    message = message + "before time expires"
                }
                message = message + ", $deviceString will not $action. Instead, it will only "
                if(settings["closeAction"]){

                    if(settings["closeAction"] == "on" || settings["closeAction"] == "off") message = message + "turn "
                    message = message + settings["closeAction"] + " with being closed."
                } else {
                    message = message + "perform action for being closed."
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
                    message = message + "a "
                } else {
                    message = message + "the "
                }
                message = message + "$contactPlural is opened. If the $deviceString should not turn on, turn off, or toggle, then select \"Don't\". Toggle will turn the $deviceString on if "
                if(multipleSwitches) {
                    message = message + "they are off, and turns them off they're on."
                } else {
                    message = message + "it is off, and turns it off if it's on."
                }
                message = message + " Required field."
                displayInfo(message)
            }
            input "openWait", "number", title: "Wait seconds after open to take action with $deviceString. (Optional)", defaultValue: false, submitOnChange:true

            message = "If device is closed "
            if(settings["openWait"]) {
                message = message + "within " + settings["openWait"] + " seconds"
            } else {
                message = message + "before time expires"
            }
            message = message + ", $deviceString will not do open action. Instead, it will only "
            if(settings["closeAction"]){
                if(settings["closeAction"] == "on" || settings["closeAction"] == "off") message = message + "turn "
                message = message + settings["closeAction"] + " with being closed."
            } else {
                message = message + "perform action for being closed."
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
        title = "When closed: "
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
        title = title + action

        if(settings["closeWait"] && settings["closeWait"] > 0) title = title + " after " + settings["closeWait"] + " seconds"

        section(hideable: true, hidden: true, "$title"){
            if(settings["deviceType"] == "lock"){
                input "closeAction", "enum", title: "When closed, lock or unlock?", multiple: false, width: 12, options: ["none": "Don't lock or unlock","lock": "Lock", "unlock": "Unlock"], submitOnChange:true
            } else {
                input "closeAction", "enum", title: "When closed, turn $deviceString on or off?", multiple: false, width: 12, options: ["none": "Don't turn on or off (leave as is)","on": "Turn On", "off": "Turn Off", "toggle": "Toggle", "resume":"Resume Schedule (or turn off)"], submitOnChange:true
            }
            if(settings["closeAction"] == "resume") {
                message = "If "
                if(multipleDevices){
                    message = message + "a $deviceString"
                } else {
                    message = message + "the $deviceString"
                }
                message = message + " has an active schedule, the schedule will be enabled when "
                if(multipleContacts){
                    message = message + "one of the contact/door sensors"
                } else {
                    message = message + "the contact/door sensor"
                }
                message = message + " closes. If there are no active schedules, the $deviceString will be turned off. To resume active schedules without turning off, select \"Don't\"."

                displayInfo(message)
            }
            if(settings["closeAction"] != "none"){
                input "closeWait", "number", title: "Wait seconds after close to $action $deviceString. (Optional)", defaultValue: false, submitOnChange:true

                message = "If device is opened "
                if(settings["closeWait"]) {
                    message = message + "within " + settings["closeWait"] + " seconds"
                } else {
                    message = message + "before time expires"
                }
                message = message + ", $deviceString will not $action. Instead, it will only "
                if(settings["openAction"]){

                    if(settings["openAction"] == "on" || settings["openAction"] == "off") message = message + "turn "
                    message = message + settings["openAction"] + " with being opened again."
                } else {
                    message = message + "perform action for being opened."
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
                    message = message + "a "
                } else {
                    message = message + "the "
                }
                message = message + "$contactPlural is closed. If the $deviceString should not turn on, turn off, or toggle, then select \"Don't\". Toggle will turn the $deviceString on if "
                if(multipleSwitches) {
                    message = message + "they are off, and turns them off they're on."
                } else {
                    message = message + "it is off, and turns it off if it's on."
                }
                message = message + " Required field."
                displayInfo(message)
            }
            if(settings["closeAction"] != "none"){
                input "closeWait", "number", title: "Wait seconds after close to take action with $deviceString. (Optional)", defaultValue: false, submitOnChange:true

                message = "If device is opened "
                if(settings["closeWait"]) {
                    message = message + "within " + settings["closeWait"] + " seconds"
                } else {
                    message = message + "before time expires"
                }
                message = message + ", $deviceString will not do close action. Instead, it will only "
                if(settings["openAction"] == "on" || settings["openAction"] == "off") message = message + "turn "
                message = message + settings["openAction"] + " with being opened again."
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
        sectionTitle = "<b>Click to set brightness</b> (optional)"
    } else {
        sectionTitle = ""
        if(settings["openLevel"]){
            sectionTitle = "On open, set brightness to " + settings["openLevel"] + "%"
            if(settings["closeLevel"]) sectionTitle = sectionTitle + "<br>"
        }
        if(settings["closeLevel"]){
            sectionTitle = sectionTitle + "On close, set brightness to " + settings["closeLevel"] + "%"
        }
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
        sectionTitle = "<b>Click to set temperature color</b> (optional)"
    } else {
        sectionTitle = ""
        if(settings["openTemp"]){
            sectionTitle = "On open, set temperature color to " + settings["openTemp"] + "K"
            if(settings["closeTemp"]) sectionTitle = sectionTitle + "<br>"
        }
        if(settings["closeTemp"]){
            sectionTitle = sectionTitle + "On close, set temperature color to "+ settings["closeTemp"] + "K"
        }
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

    hidden = true
    // If error, unhide
    if((settings["openHue"] && settings["openHue"] > 100) || (settings["openSat"] && settings["openSat"] > 100) || (settings["closeHue"] && settings["closeHue"] > 100) || (settings["closeSat"] && settings["closeSat"] > 100)) hidden = false
    // If missing any one value, unhide
    if((settings["openHue"] || settings["openSat"] || settings["closeHue"] || settings["closeSat"]) &&
       (!settings["openHue"] || !settings["openSat"] || !settings["closeHue"] || !settings["closeSat"])) hidden = false

    if(settings["closeAction"] == "off" || settings["closeAction"] == "resume" || settings["openAction"] == "off" || settings["openAction"] == "resume") {
        width = 12
    } else {
        width = 6
    }

    if(!settings["openHue"] && !settings["closeHue"] && !settings["openSat"] && !settings["closeSat"]){
        sectionTitle = "<b>Click to set color (hue and/or saturation)</b> (optional)"
    } else {
        sectionTitle = ""
        // If just open color
        if(settings["openHue"]) {
            sectionTitle = "On open, set hue to " + settings["openHue"] + "%"
            if(settings["openSat"]) sectionTitle = sectionTitle + " and sat to " + settings["openSat"] + "%"
        } else if(settings["openSat"]) {
            sectionTitle = "On open, set saturation " + settings["openSat"] + "%"
        }
        if((settings["openHue"] || settings["openSat"]) && (settings["closeHue"] || settings["closeSat"])) {
            sectionTitle = sectionTitle + "<br>"
        }
        if(settings["closeHue"]) {
            sectionTitle = sectionTitle + "On close, set hue to " + settings["closeHue"] + "%"
            if(settings["closeSat"]) sectionTitle = sectionTitle + " and sat to " + settings["closeSat"] + "%"
        } else if(settings["closeSat"]) {
            sectionTitle = sectionTitle + "On close, set saturation " + settings["closeSat"] + "%"
        }
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        if(settings["openAction"] != "off" && settings["openAction"] != "resume"){
            input "openHue", "number", title: "Set hue on open?", width: width, submitOnChange:true
        }

        if(settings["openAction"] != "off" && settings["openAction"] != "resume"){
            input "openSat", "number", title: "Set saturation on open?", width: width, submitOnChange:true
        }

        if(settings["closeAction"] != "off" && settings["closeAction"] != "resume"){
            input "closeHue", "number", title: "Set hue on close?", width: width, submitOnChange:true
        }

        if(settings["closeAction"] != "off" && settings["closeAction"] != "resume"){
            input "closeSat", "number", title: "Set saturation on close?", width: width, submitOnChange:true
        }
        displayInfo("Hue is percent around a color wheel, where red is 0 (and 100). Orange = 8; yellow = 16; green = 33; 50 = turquiose; blue = 66; purple = 79. Optional")
        displayInfo("Saturation is the percentage depth of color, from 1 to 100, where 1 is hardly any color tint and 100 is full color. Optional.")


        if(settings["openHue"] > 100) errorMessage("Hue can't be more then 100. Correct open hue.")
        if(settings["openSat"] > 100) errorMessage("Saturation can't be more then 100. Correct open saturation.")
        if(settings["closeHue"] > 100) errorMessage("Hue can't be more then 100. Correct close hue.")
        if(settings["closeSat"] > 100) errorMessage("Saturation can't be more then 100. Correct close saturation.")
    }
}

def displayScheduleSection(){
    if(!settings["contactDevice"] || !settings["deviceType"] || !settings["device"] || !settings["openAction"] || !settings["closeAction"]) return

    // If all options, display full title
    if(checkTimeComplete("start") && checkTimeComplete("stop") && settings["inputStartType"] && settings["inputStopType"]){
        varStartTime = getTimeVariables("start")
        varStopTime = getTimeVariables("stop")
        title = "Only if between $varStartTime and $varStopTime"
        hidden = true
        // If no options, display generic title
    } else if(checkTimeComplete("start") && checkTimeComplete("stop") && !settings["inputStartType"] && !settings["inputStopType"]){
        title = "<b>Click to set schedule</b> (optional)"
        hidden = true
        // If partial options, display partial title
    } else if(!checkTimeComplete("start") || !checkTimeComplete("stop") || !settings["inputStartType"] || !settings["inputStopType"]){
        if(checkTimeComplete("start") && settings["inputStartType"]){
            varStartTime = getTimeVariables("start")
            title = "Beginning at $varStartTime"
        } else if(checkTimeComplete("stop") && settings["inputStopType"]){
            varStopTime = getTimeVariables("stop")
            title = "Ending at $varStopTime"
        } else {
            title = "Set schedule"
        }
        hidden = false
    }

    section(hideable: true, hidden: hidden, title){
        displayStartTypeOption()

        // Display exact time option
        if(settings["inputStartType"] == "time"){
            displayTimeOption("start")
        } else if(settings["inputStartType"]){
            // Display sunrise/sunset type option (at/before/after)
            displaySunriseTypeOption("start")
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
                // Display sunrise/sunset offset
                if(inputStartSunriseType != "at") displaySunriseOffsetOption("stop")
            }
        }
    }
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
        sectionTitle = "<b>Click to set Mode change</b> (optional)"
    } else {
        sectionTitle = ""
        if(settings["openMode"]) {
            sectionTitle = "On open, set Mode " + settings["openMode"]
            if(settings["closeMode"]) sectionTitle = sectionTitle + "<br>"
        }

        if(settings["closeMode"]) {
            sectionTitle = sectionTitle + "On close, set Mode " + settings["closeMode"]
        }
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
        sectionTitle = "Only with Mode: $settings.ifMode"
    } else {
        sectionTitle = "<b>Click to select with what Mode</b> (optional)"
    }
    section(hideable: true, hidden: true, sectionTitle){
        input "ifMode", "mode", title: "Only run if Mode is already?", width: 12, submitOnChange:true

        message = "This will limit the contact/door sensor"
        if(multipleContacts) message = message + "s"
        message = message + " from running to only when Hubitat's Mode is "
        if(settings["ifMode"]) {
            message = message + settings["ifMode"]
        } else {
            message = message + "as selected."
        }

        displayInfo(message)
    }
}

def displayDaysOption(){
    if(!settings["contactDevice"] || !settings["deviceType"] || !settings["device"] || !settings["openAction"] || !settings["closeAction"]) return
    if(settings["openAction"] == "none" && settings["closeAction"] == "none") return
    multipleContacts = false
    count = 0
    settings["contactDevice"].each{
        if(count == 1) multipleContacts = true
        count = 1
    }

    if(settings["timeDays"]){
        sectionTitle = "Only on: $settings.timeDays"
    } else {
        sectionTitle = "<b>Click to select on which days</b> (optional)"
    }
    section(hideable: true, hidden: true, sectionTitle){
        input "timeDays", "enum", title: "On these days (defaults to all days)", multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"], submitOnChange:true


        message = "This will limit the contact/door sensor"
        if(multipleContacts) message = message + "s"
        message = message + " from running only on "
        if(settings["timeDays"]) {
            message = message + settings["timeDays"]
        } else {
            message = message + "the day(s) selected."
        }

        displayInfo(message)
    }
}

def displayAlertOptions(){
    if(!settings["contactDevice"] || !settings["deviceType"] || !settings["device"] || !settings["openAction"] || !settings["closeAction"]) return
    if(!parent.pushNotification && !parent.speech) return

    hidden = true
    if((settings["pushNotificationDevice"] && !settings["pushNotification"]) || (settings["speechDevice"] && !settings["speech"])) hidden = false

    sectionTitle = ""
    if(notificationOpenClose) {
        action = "On close"
    } else {
        action = "On open"
    }
    if((!settings["pushNotificationDevice"] && !settings["speechDevice"] || (settings["pushNotificationDevice"] && !settings["pushNotification"]) || (settings["speechDevice"] && !settings["speech"]))){
        sectionTitle = "<b>Click to set notifications</b> (optional)"
    } else {
        if(settings["pushNotificationDevice"]){
            sectionTitle = "$action, send push notification"
            if(settings["speechDevice"]) sectionTitle = sectionTitle + "<br>" 
        }
        if(settings["speechDevice"]){
            sectionTitle = "$action, text-to-speech announcement"
        }
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        if(parent.pushNotificationDevice){
            state.filteredList = [:]
            count = 0
            deviceName = ""
            parent.pushNotificationDevice.each{
                deviceName = "${it.label ?: it.name}"
                deviceId = it.id
                state.filteredList[it.id] = deviceName
                count++
                    }
            if(count > 1){
                input "pushNotificationDevice", "enum", title: "Push notification device(s)?", options: state.filteredList, multiple: true, submitOnChange: true
            } else {
                settings["pushNotificationDevice"] = deviceId
            }
            input "pushNotification", "text", title: "Text notification to send?", submitOnChange:true
            if(count == 1) displayInfo("Push notifications will use the device \"$deviceName\". To use another, add it in the Master app.")
        }

        if(parent.speech){
            state.filteredList = [:]
            count = 0
            deviceName = ""
            parent.speechDevice.each{
                deviceName = "${it.label ?: it.name}"
                deviceId = it.id
                state.filteredList[it.id] = deviceName
                count++
                    }
            if(count > 1) {
                input "speechDevice", "enum", title: "Text-to-speech device to use", options: state.filteredList, multiple: true, submitOnChange: true
            } else {
                settings["speechDevice"] = deviceId
            }
            input "speechDevice", "text", title: "Text-to-speech announcement?", submitOnChange:true
            if(count == 1) displayInfo("Text-to-speech will use the device \"$deviceName\". To use another, add it in the Master app.")
        }

        if((pushNotificationDevice && pushNotification) || (speechDevice && speech)){
            titleText = ""
            if(pushNotificationDevice && pushNotification) {
                titleText = "Send notification"
                if(speechDevice && speech) titleText = titleText + " and speak"
            } else {
                titleText = "Speak"
            }
            titleText = titleText + "when "
            if(notificationOpenClose){
                titleText = titleText + "<b>closed</b>. Click for opened."
            } else {
                titleText = titleText + "<b>opened</b>. Click for closed."
            }

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
timeDays - enum (Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday) - Days on which contact will run. Only displays if scheduleEnable = true
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
    if(checkLog(a="trace")) putLog(1062,"Installed",a)
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    state.logLevel = getLogLevel()
    if(checkLog(a="trace")) putLog(1069,"Updated",a)
    unsubscribe()
    initialize()
}

def initialize() {
    state.logLevel = getLogLevel()
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

    unschedule()

    // If date/time for last notification not set, initialize it to 5 minutes ago
    if(!state.contactLastNotification) state.contactLastNotification = new Date().getTime() - 360000

    if(disable || disableAll) {
        state.disable = true
        return
    } else {
        state.disable = false
    }

    if(!disable && !state.disable) {
        subscribe(contactDevice, "contact.open", contactChange)
        subscribe(contactDevice, "contact.closed", contactChange)            
    }

    if(checkLog(a="trace")) putLog(1096,"Initialized",a)
}

def contactChange(evt){
    if(disable || state.disable) return

    if(checkLog(a="debug")) putLog(1102,"Contact sensor $evt.displayName $evt.value",a)

    // If mode set and node doesn't match, return nulls
    if(settings["ifMode"]){
        if(location.mode != settings["ifMode"]) {
            if(checkLog(a="trace")) putLog(1107,"Contact disabled, requires mode $ifMode",a)
            return defaults
        }
    }

    // If not correct day, return nulls
    if(timeDays && !parent.todayInDayList(timeDays,app.label)) return

    if(settings["inputStartType"] && settings["inputStopType"]) {
        setTime()
    } else {
        atomicState.stop = null
        atomicState.start = null
    }

    // if not between start and stop time, return nulls
    if(atomicState.stop && !parent.timeBetween(state.start, atomicState.stop, app.label)) return

    // Unschedule pevious events

    // If opened a second time, it will reset delayed action
    // If closed a second time, it won't override open
    if(evt.value == "open"){
        unschedule()
    } else {
        unschedule(runScheduleClose)
    }

    // Check if people are home (home1 and home2 should be true)
    // If conditions are satified, set home to true
    if(personHome){
        home1 = false
        personHome.each{
            if(it.currentPresence == "present") home1 = true
        }
    } else {
        home1 = true
    }
    if(personNotHome){
        home2 = true
        personNotHome.each{
            if(it.currentPresence == "present") home2 = false
        }
    } else {
        home2 = true
    }
    if(home1 && home2) home = true

    // Text first (just in case there's an error later)
    if(settings["pushNotificationDevice"] && home && ((!settings["notificationOpenClose"] && evt.value == "open") || (settings["notificationOpenClose"] && evt.value == "closed"))){
        // Only if correct people are home/not home
        def now = new Date()

        //if last text was sent less than 5 minutes ago, don't send
        /* ************************************************************************ */
        /* TO-DO: Add option to override text cooldown period? (Maybe in Master?)   */
        /* Same with presence app.                                                  */
        /* ************************************************************************ */
        // Compute seconds from last notification
        seconds = (now.getTime()  - state.contactLastNotification) / 1000

        // Convert date to friendly format for log
        now = now.format("h:mm a", location.timeZone)
        if(seconds > 360){
            state.contactLastNotification = new Date().getTime()

            if(evt.value == "open") {
                eventName = "opened"
            } else {
                eventName = evt.value
            }
            parent.sendText(pushNotificationDevice,"$evt.displayName was $eventName at $now.",app.label)
            if(checkLog(a="info")) putLog(1169,"Sent push notice for $evt.displayName $eventName at $now.",a)
        } else {
            if(checkLog(a="info")) putLog(1171,"Did not send push notice for $evt.displayName $eventName due to notification sent $seconds ago.",a)
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

    // Perform open events (for switches and locks)
    if(evt.value == "open"){
        // Schedule delay
        if(openWait) {
            if(checkLog(a="trace")) putLog(1191,"Scheduling runScheduleOpen in $openWait seconds",a)
            runIn(openWait,runScheduleOpen)
            // Otherwise perform immediately
        } else {
            // Need to add level, temp and color!!
            // Need to add resume
            // It will get defaults, even if it's supposed to override
            if(deviceType == "switch" || deviceType == "light") {
                setStateMulti(openAction,device,"open")
            } else if(deviceType == "lock") {
                parent.multiLock(openAction,device,app.label)
            }
        }

        // Perform close events (for switches and locks)
    } else {
        // Schedule delay
        if(closeWait) {
            if(checkLog(a="trace")) putLog(1206,"Scheduling runScheduleClose in $closeWait seconds",a)
            runIn(closeWait,runScheduleClose)
            // Otherwise perform immediately
        } else {
            if(deviceType == "switch" || deviceType == "light") {
                setStateMulti(closeAction,device,"close")
            } else if(deviceType == "lock"){
                parent.multiLock(closeLockAction,device,app.label)
            }
        }
    }
}

def runScheduleOpen(){
    if(disable || state.disable) return

    if(deviceType == "switch" || deviceType == "light") {
        setStateMulti(openAction,device,"open")
    } else if(deviceType == "lock") {
        parent.multiLock(openAction,device,app.label)
    }
}

def runScheduleClose(){
    if(disable || state.disable) return

    if(deviceType == "switch" || deviceType == "light") {
        setStateMulti(closeAction,device,"close")
    } else if(deviceType == "lock"){
        parent.multiLock(closeAction,device,app.label)
    }
}

def setTime (){
    if(!setStartStopTime("start")) return
    if(!setStartStopTime("stop")) return 
}

// Sets atomicState.start and atomicState.stop variables
// Requires type value of "start" or "stop" (must be capitalized to match setting variables)
def setStartStopTime(type){
    if(type != "start" && type != "stop") {
        if(checkLog(a="error")) putLog(1269,"Invalid value for type \"$type\" sent to setStartStopTime function",a)
        return
    }

    if(type == "start") {
        atomicState.start = null
        type = "Start"
    } else if(type == "stop") {
        atomicState.stop = null
        type = "Stop"
    }

    // If no stop time, exit
    if(type == "Stop" && (!inputStopType || inputStopType == "none")) return true

    if(settings["input${type}Type"] == "time"){
        value = settings["input${type}Time"]
    } else if(settings["input${type}Type"] == "sunrise"){
        value = (settings["input${type}SunriseType"] == "before" ? parent.getSunrise(settings["input${type}Before"] * -1,app.label) : parent.getSunrise(settings["input${type}Before"],app.label))
    } else if(settings["input${type}Type"] == "sunset"){
        value = (settings["input${type}SunriseType"] == "before" ? parent.getSunset(settings["input${type}Before"] * -1,app.label) : parent.getSunset(settings["input${type}Before"],app.label))
    } else {
        if(checkLog(a="error")) putLog(1291,"input" + type + "Type set to " + settings["input${type}Type"],a)
        return
    }

    if(type == "Stop"){
        if(timeToday(state.start, location.timeZone).time > timeToday(value, location.timeZone).time) value = parent.getTomorrow(value,app.label)
    }
    if(checkLog(a="trace")) putLog(1298,"$type time set as " + Date.parse("yyyy-MM-dd'T'HH:mm:ss", value).format("h:mma MMM dd, yyyy", location.timeZone),a)
    if(type == "Start") atomicState.start = value
    if(type == "Stop") atomicState.stop = value
    return true
}

// If deviceChange exists, adds deviceId to it; otherwise, creates deviceChange with deviceId
// Delineate values with colons on each side - must match getStateDeviceChange
// Used to track if app turned on device when schedule captures a device state changing to on
// Must be included in all apps using MultiOn
def addDeviceStateChange(singleDeviceId){
    if(atomicState.deviceChange) {
        atomicState.deviceChange += ":$singleDeviceId:"
    } else {
        atomicState.deviceChange = ":$singleDeviceId:"
    }
    return
}

// Gets levels as set for the app
// Function must be included in all apps that use MultiOn
def getOverrideLevels(defaults,appAction = null){
    if(!defaults && (settings[appAction + "Level"] || settings[appAction + "Temp"] || settings[appAction + "Hue"] || settings[appAction + "Sat"])) defaults = [:]
    if(settings[appAction + "Level"]) defaults.put("level",settings[appAction + "Level"])
    if(settings[appAction + "Temp"]) defaults.put("temp",settings[appAction + "Temp"])
    if(settings[appAction + "Hue"]) defaults.put("hue",settings[appAction + "Hue"])
    if(settings[appAction + "Sat"]) defaults.put("sat",settings[appAction + "Sat"])
    return defaults       
}

// Returns the value of deviceChange
// Used by schedule when a device state changes to on, to check if an app did it
// It should only persist as long as it takes for the scheduler to capture and
// process both state change request and state change subscription
// Function must be in every app
def getStateDeviceChange(singleDeviceId){
    if(atomicState.deviceChange){
        value = atomicState.deviceChange.indexOf(":$singleDeviceId:")
        // Reset it when it's used, to try and avoid race conditions with multiple fast button clicks
        resetStateDeviceChange()
        return value
    } else {
        return false
    }
}

// Scheduled funtion to reset the value of deviceChange
// Must be in every app using MultiOn
def resetStateDeviceChange(){
    atomicState.deviceChange = null
    return
}

// This is a bit of a mess, but.... 
def setStateMulti(deviceAction,device,appAction = null){
    if(!deviceAction || (deviceAction != "on" && deviceAction != "off" && deviceAction != "toggle" && deviceAction != "resume" && deviceAction != "none")) {
        if(checkLog(a="error")) putLog(1354,"Invalid deviceAction \"$deviceAction\" sent to setStateMulti",a)
        return
    }

    // Time in which to allow Hubitat to process sensor change (eg Pico, contact, etc.)
    // as well as the scheduler to process any state change generated by the sensor
    // What's a realistic number to use if someone has a lot of devices attached to a lot 
    // of Picos with a lot of schedules?
    stateDeviceChangeResetMillis = 500

    if(deviceAction == "off"){
        // Reset device change, since we know the last event from this device didn't turn anything on
        resetStateDeviceChange()
        // Turn off devices
        parent.setStateMulti("off",device,app.label)
        return true
    }

    if(deviceAction == "on"){
        // Turn on devices
        parent.setStateMulti("on",device,app.label)
        // Get and set defaults levels for each device
        device.each{
            // Add device ids to deviceChange, so schedule knows it was turned on by an app
            // Needs to be done before turning the device on.
            addDeviceStateChange(it.id)
            // Set scheduled levels, default levels, and/or [this child-app's] levels
            getAndSetSingleLevels(it,appAction)
        }
        if(checkLog(a="debug")) putLog(1383,"Device id's turned on are $atomicState.deviceChange",a)
        // Schedule deviceChange reset
        runInMillis(stateDeviceChangeResetMillis,resetStateDeviceChange)
        return true
    }

    if(deviceAction == "toggle"){
        // Create toggleOnDevice list, used to track which devices are being toggled on
        toggleOnDevice = []
        // Set count variable, used for toggleOnDevice
        count = 0
        device.each{
            // Start count at 1; doesn't matter, so long as it matches newCount below
            count = count + 1
            // If toggling to off
            if(parent.isOn(it)){
                parent.setStateSingle("off",it,app.label)
                // Else if toggling on
            } else {
                // When turning on, add device ids to deviceChange, so schedule knows it was turned on by an app
                // Needs to be done before turning the device on.
                addDeviceStateChange(it.id)
                // Turn the device on
                parent.setStateSingle("on",it,app.label)
                // Add device to toggleOnDevice list so when we loop again to set levels, we know whether we
                // just turned it on or not (without knowing how long the device may take to respond)
                toggleOnDevice.add(count)
            }
        }
        if(checkLog(a="debug")) putLog(1412,"Device id's toggled on are $atomicState.deviceChange",a)
        // Create newCount variable, which is compared to the [old]count variable
        // Used to identify which lights were turned on in the last loop
        newCount = 0
        device.each{
            // Start newCount at 1 like count above
            newCount = newCount + 1
            // If turning on, set scheduled levels, default levels, and/or [this child-app's] levels
            // If newCount is contained in the list of [old]count, then we toggled on
            if(toggleOnDevice.contains(newCount)){
                getAndSetSingleLevels(it,appAction)
            }
        }
        // Schedule deviceChange reset
        runInMillis(stateDeviceChangeResetMillis,resetStateDeviceChange)
        return true
    }

    if(deviceAction == "resume"){
        // Reset device change, since we know the last event from this device didn't turn anything on
        resetStateDeviceChange()
        device.each{
            // If turning on, set default levels and over-ride with any contact levels
            if(deviceAction == "resume"){
                // If defaults, then there's an active schedule
                // So use it for if overriding/reenabling
                defaults = parent.getScheduleDefaultSingle(it,app.label)
                if(checkLog(a="debug")) putLog(1439,"Scheduled defaults are $defaults",a)

                defaults = getOverrideLevels(defaults,appAction)
                if(checkLog(a="debug")) putLog(1442,"With " + app.label + " overrides, using $defaults",a)

                // Skipping getting overall defaults, since we're resuming a schedule or exiting;
                // rather keep things the same level rather than an arbitrary default, and
                // if we got default, we'd not turn it off

                parent.setLevelSingle(defaults,it,app.label)
                // Set default level
                if(!defaults){
                    if(checkLog(a="trace")) putLog(1451,"No schedule to resume for $it; turning off",a)
                    parent.setStateSingle("off",it,app.label)
                } else {
                    parent.rescheduleIncrementalSingle(it,app.label)
                }
            }
        }
        return true
    }

    if(deviceAction == "none"){
        // Reset device change, since we know the last event from this device didn't turn anything on
        resetStateDeviceChange()
        // If doing nothing, reschedule incremental changes (to reset any overriding of schedules)
        // I think this is the only place we use ...Multi, prolly not enough to justify a separate function
        parent.rescheduleIncrementalMulti(device,app.label)
        return true
    }
}

// Handles turning on a single device and setting levels
// Only called by (child app) multiOn
// appAction is for "open/close", "push/hold", etc., so the child app knows which
// levels to apply for which device/action
def getAndSetSingleLevels(singleDevice,appAction = null){
    // If defaults, then there's an active schedule
    // So use it for if overriding/reenabling
    // In scheduler app, this gets defaults for any *other* schedule
    defaults = parent.getScheduleDefaultSingle(singleDevice,app.label)
    logMessage = defaults ? "$singleDevice scheduled for $defaults" : "$singleDevice has no scheduled default levels"

    // If there are defaults, then there's an active schedule so reschedule it (the results are corrupted below).
    // We could do this for the matching schedules within its own getDefaultLevel(), but that would
    // probably result in incremental schedules rescheduling themselves over and over again. And if we
    // excluded schedules from rescheduling, then daily schedules wouldn't do this.
    if(defaults) parent.rescheduleIncrementalSingle(singleDevice,app.label)

    // This does nothing in Time, or other app that has no levels, getOverrideLevels will immediately exit
    defaults = getOverrideLevels(defaults,appAction)
    logMessage += defaults ? ", controller overrides of $defaults": ", no controller overrides"

    // Set default levels, for level and temp, if no scheduled defaults (don't need to do for "resume")
    defaults = parent.getDefaultSingle(defaults,app.label)
    logMessage += ", so with generic defaults $defaults"

    if(checkLog(a="debug")) putLog(1496,logMessage,a)
    parent.setLevelSingle(defaults,singleDevice,app.label)
    return
}

def checkLog(type = null){
    if(!state.logLevel) getLogLevel()
    switch(type) {
        case "error":
        if(state.logLevel > 0) return "error"
        break
        case "warn":
        if(state.logLevel > 1) return "warn"
        break
        case "info":
        if(state.logLevel > 2) return "info"
        break
        case "trace":
        if(state.logLevel > 3) return "trace"
        break
        case "debug":
        if(state.logLevel == 5) return "debug"
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,message = null,type = "trace"){
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
