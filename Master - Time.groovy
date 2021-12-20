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
*  Name: Master - Time
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master%20-%20Time.groovy
*  Version: 0.6.10
*
***********************************************************************************************************************/

definition(
    name: 'Master - Time',
    namespace: 'master',
    author: 'roguetech',
    description: 'Schedules, times and default settings',
    parent: 'master:Master',
    category: 'Convenience',
    importUrl: 'https://raw.githubusercontent.com/roguetech2/hubitat/master/Master%20-%20Time.groovy',
    iconUrl: 'http://cdn.device-icons.smartthings.com/Office/office6-icn@2x.png',
    iconX2Url: 'http://cdn.device-icons.smartthings.com/Office/office6-icn@2x.png'
)

// logLevel sets number of log messages
// 0 for none
// 1 for errors only
// 5 for all
def getLogLevel(){
    return 5
}

preferences {
    infoIcon = '<img src="http://emily-john.love/icons/information.png" width=20 height=20>'
    errorIcon = '<img src="http://emily-john.love/icons/error.png" width=20 height=20>'
    warningIcon = '<img src="http://emily-john.love/icons/warning.png" width=20 height=20>'
    moreOptions = ' <font color="gray">(more options)</font>'

    install = formComplete()
    page(name: 'setup', install: install, uninstall: true) {
        // display Name
        if(!app.label){
            section(){
                displayNameOption()
            }
        } else {
            if(!settings) settings = [:]

            deviceCount = getDeviceCount(device)
            peopleError = compareDeviceLists(personHome,personNotHome)
            plainStartAction = getPlainAction(settings['start_action'])
            plainStopAction = getPlainAction(settings['stop_action'])
            pluralDevice = getDevicePlural()

            if(!settings['deviceType']) settings['device'] = null
            if(!settings['device']) {
                settings['start_timeType'] = null
                settings['start_timeType'] = null
            }
            if(settings['start_action'] == 'off') {
                settings['start_level'] = null
                settings['start_temp'] = null
                settings['start_hue'] = null
                settings['start_sat'] = null
            }
            if(!settings['stop_action']) {
                settings['stop_level'] = null
                settings['stop_temp'] = null
                settings['stop_hue'] = null
                settings['stop_sat'] = null
            }
            if(!settings['start_timeType']) {
                settings['start_sunType'] = null
                settings['stop_timeType'] = null
            }
            if(settings['start_timeType'] != 'time') settings['start_time'] = null
            if(settings['start_timeType'] == 'time') settings['start_sunType'] = null
            if(!settings['start_sunType']) settings['start_sunOffset'] = null
            if(settings['start_sunType'] == 'at') settings['start_sunOffset'] = null
            startTimeComplete = checkTimeComplete('start')
            if(!startTimeComplete) settings['stop_timeType'] = null
            if(!settings['stop_timeType']) settings['stop_sunType'] = null
            if(settings['stop_timeType'] != 'time') settings['stop_time'] = null
            if(settings['stop_timeType'] == 'time') settings['stop_sunType'] = null
            if(settings['stop_sunType'] != 'before' && settings['stop_sunType'] != 'after') settings['stop_sunOffset'] = null
            if(settings['stop_sunType'] == 'at') settings['stop_sunOffset'] = null
            stopTimeComplete = checkTimeComplete('stop')
            if(!settings['stop_timeType'] || !stopTimeComplete) {
                settings['months'] = null
                settings['days'] = null
                settings['start_level'] = null
                settings['stop_level'] = null
                settings['start_temp'] = null
                settings['stop_temp'] = null
                settings['start_hue'] = null
                settings['stop_hue'] = null
                settings['start_sat'] = null
                settings['stop_sat'] = null
                settings['start_action'] = null
                settings['stop_action'] = null
                settings['stop_time'] = null
                settings['stop_sunType'] = null
                settings['stop_sunOffset'] = null
                settings['personHome'] = null
                settings['personNotHome'] = null
            }
            if(settings['stop_timeType'] == 'none') settings['stop_action'] = null
            if(!settings['start_hue']) settings['hueDirection'] = null
            if(!settings['stop_hue']) settings['hueDirection'] = null

            peopleError = compareDeviceLists(personHome,personNotHome)
            
            
            section(){
                displayNameOption()
                displayDevicesTypes()
                displayDevicesOption()
                if(install) displayDisableOption()
            }
            
            displayScheduleSection()
            displayActionOption()
            displayBrightnessOption()
            displayTemperatureOption()
            displayColorOption()
            displayChangeModeOption()
            displayPeopleOption()
            displayIfModeOption()
        }
    }
}

/* ************************************************** */
/* TO-DO: Test for schedule spanning two days, but    */
/* scheduled for specific days;                       */
/* Warn that progressive changes may not work as      */
/* intended, and will not turn off on a non-scheduled */
/* day.                                               */
/* ************************************************** */
/* ************************************************** */
/* TO-DO: Add warning for if time span is small and   */
/* changes are large, where change will not be        */
/* smooth.                                            */
/* ************************************************** */


// Display functions
def formComplete(){
    if(!app.label) return false
    if(!device) return false
    if(!start_action) return false
    if(!stop_action && stop_timeType != 'none') return false
    if(!start_timeType) return false
    if(!stop_timeType) return false
    if(start_timeType == 'time' && !start_time) return false
    if(stop_timeType == 'time' && !stop_time) return false
    if((start_timeType == 'sunrise' || start_timeType == 'sunset') && !start_sunType) return false
    if((stop_timeType == 'sunrise' || stop_timeType == 'sunset') && !stop_sunType) return false
    if((start_sunType == 'before' || start_sunType == 'after') && !start_sunOffset) return false
    if((stop_sunType == 'before' || stop_sunType == 'after') && !stop_sunOffset) return false
    if(!validateStartLevel()) return false
    if(!validateStopLevel()) return false
    if(!validateStartTemp()) return false
    if(!validateStopTemp()) return false
    if(!validateHue(start_hue)) return false
    if(!validateHue(stop_hue)) return false
    if(!validateSat(start_sat)) return false
    if(!validateSat(stop_sat)) return false
    if(start_hue && stop_hue && !hueDirection) return false
    return true
}

def validateStartLevel(){
    return parent.validateLevel(start_level)
}

def validateStopLevel(){
    return parent.validateLevel(stop_level)
}

def validateStartTemp(){
    return parent.validateTemp(start_temp)
}

def validateStopTemp(){
    return parent.validateTemp(stop_temp)
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

def displayLabel(text, width = 12){
    paragraph("<div style=\"background-color:#DCDCDC\"><b>$text:</b></div>",width:width)
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

// Display functions
def getDeviceCount(device){
    if(!device) return 0
    return device.size()
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

def displayNameOption(){
    if(app.label){
        displayLabel('Schedule name',2)
        label title: '', required: false, width: 10,submitOnChange:true
    } else {
        displayLabel('Set name for this schedule')
        label title: '', required: false, submitOnChange:true
        displayInfo('Name this schedule. Each schedule must have a unique name.')
    }
}

def displayDevicesTypes(){
    deviceText = 'device(s)'
    if(deviceCount == 1) deviceText = 'device'
    if(deviceCount > 1) deviceText = 'devices'
    inputTitle = "Type of $deviceText to schedule:"
    if(!settings['deviceType']) inputTitle = highlightText('Which type of device(s) to schedule (click to select one)?')
    input 'deviceType', 'enum', title: inputTitle, options: ['lock': 'Lock(s)','light': 'Light(s)', 'switch': 'Switch(es)', 'fan': 'Fan(s)'], multiple: false, required: false, submitOnChange:true
    if(!settings['deviceType']) displayInfo('Light(s) allows selecting dimmable switches. Switch(es) include all lights (and fans).')
}

def displayDevicesOption(){
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

    inputTitle = pluralDevice.capitalize() + ' to schedule (click to select)?'
    if(settings['device']) inputTitle = pluralDevice.capitalize() + ' being scheduled:'
    input 'device', capability, title: inputTitle, multiple: true, submitOnChange:true
}

def displayDisableOption(){
    if(disable){
        input 'disable', 'bool', title: '<b><font color="#000099">This schedule is disabled.</font></b> Reenable it?', submitOnChange:true
        return
    }
    input 'disable', 'bool', title: 'This schedule is enabled. Disable it?', submitOnChange:true
}

def displayActionOption(){
    if(!settings['start_timeType'] || !settings['stop_timeType']) return
    if(!startTimeComplete || !stopTimeComplete) return

    hideable = true
    hidden = true
    if(!settings['start_action'] || (!settings['stop_action'] && settings['stop_timeType'] != 'none')) hideable = false
    
    sectionTitle = ''
    if(hideable){
        if(settings['start_action']) sectionTitle = '<b>When starting: ' + plainStartAction.capitalize() + '</b>'
        if(settings['start_action'] && settings['stop_action']) sectionTitle += '<br>'
        if(settings['stop_action']) sectionTitle += '<b>When stopping: ' + plainStopAction.capitalize() + '</b>'
    }
    
    if(settings['start_action']) startInputTitle = 'When starting:'
    if(!settings['start_action'] && settings['deviceType'] == 'lock') startInputTitle = 'When starting, lock or unlock (click to select)?'
    if(!settings['start_action'] && settings['deviceType'] != 'lock') startInputTitle = "When starting, do what with the $pluralDevice (click to select)?"
    if(!settings['start_action']) startInputTitle = highlightText(startInputTitle)

    if(settings['stop_action']) stopInputTitle = 'When stopping:'
    if(!settings['stop_action'] && settings['deviceType'] == 'lock') stopInputTitle = 'When stopping, lock or unlock (click to select)?'
    if(!settings['stop_action'] && settings['deviceType'] != 'lock') stopInputTitle = "When stopping, do what with the $pluralDevice (click to select)?"
    if(!settings['stop_action']) stopInputTitle = highlightText(stopInputTitle)
    
    section(hideable: hideable, hidden: hidden, sectionTitle){
        if(settings['deviceType'] == 'lock'){
            input 'start_action', 'enum', title: startInputTitle, multiple: false, width: 12, options: ['none': 'Don\'t lock or unlock','lock': 'Lock', 'unlock': 'Unlock'], submitOnChange:true
        } else {
            input 'start_action', 'enum', title: startInputTitle, multiple: false, width: 12, options: ['none': 'Don\'t turn on or off (leave as is)','on': 'Turn On', 'off': 'Turn Off', 'toggle': 'Toggle'], submitOnChange:true
            if(!settings['start_action']) {
                message = "Set whether to turn on, turn off, or toggle the $pluralDevice when the schedule starts. Select \"Don't\" to control other options (like setting Mode), or to do nothing when starting. Toggle will change the $pluralDevice from off to on and vice versa. Required."
                displayInfo(message)
            }
        }
        if(settings['start_action'] && settings['stop_timeType'] != 'none'){
            if(settings['deviceType'] == 'lock'){
                input 'stop_action', 'enum', title: stopInputTitle, multiple: false, width: 12, options: ['none': 'Don\'t lock or unlock','lock': 'Lock', 'unlock': 'Unlock'], submitOnChange:true
            } else {
                input 'stop_action', 'enum', title: stopInputTitle, multiple: false, width: 12, options: ['none': 'Don\'t turn on or off (leave as is)','on': 'Turn On', 'off': 'Turn Off', 'toggle': 'Toggle'], submitOnChange:true
                message = 'Set whether to turn on, turn off, or toggle when the schedule stops. Select "Don\'t" to do nothing when closed. Required.'
                displayInfo(message)
            }
        }
    }
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
    hideable = true
    hidden = true

    if(!settings['start_timeType'])  hideable = false
    if(!settings['stop_timeType']) hideable = false
    if(!startTimeComplete) hideable = false
    if(!stopTimeComplete) hideable = false
    //if(settings['start_time'] && settings['start_time'] == settings['stop_time']) hideable = false
    
    //Date.parse( 'MM', "$month" ).format( 'MMMM' )
    if(!settings['start_timeType']) hidden = false
    if(!startTimeComplete) hidden = false
    if(!stopTimeComplete && (settings['start_timeType'] || settings['stop_timeType'])) hidden = false
    if(!validateSunriseMinutes(settings['start_sunOffset'])) hidden = false
    if(!validateSunriseMinutes(settings['stop_sunOffset'])) hidden = false
    if(settings['start_time'] && settings['start_time'] == settings['stop_time']) hidden = false
    
    sectionTitle = ''
    if(hideable){
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
        if(settings['start_timeType'] && settings['months'] && settings['days']) sectionTitle += ';'
        if(settings['start_timeType'] && settings['months']) sectionTitle += " in $monthText"
        if(settings['start_timeType']) sectionTitle += '</b>'
        if(!settings['days'] || !settings['months']) sectionTitle += moreOptions

        if(settings['start_timeType'] && settings['stop_timeType']) sectionTitle += '</br>'
        if(settings['stop_timeType'] && settings['stop_timeType'] != 'none') sectionTitle += '<b>Stopping: '
        if(settings['stop_timeType'] && settings['stop_timeType'] == 'none') sectionTitle += '<b>No end'
        if(settings['stop_timeType'] == 'time' && settings['stop_time']) sectionTitle += 'At ' + Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings['stop_time']).format('h:mm a', location.timeZone)
        if(settings['stop_timeType'] == 'time' && !settings['stop_time']) sectionTitle += 'At specific time '
        if(settings['stop_timeType'] == 'sunrise' || settings['stop_timeType'] == 'sunset'){
            if(!settings['stop_sunType']) sectionTitle += 'Based on ' + settings['stop_timeType']
            if(settings['stop_sunType'] == 'at') sectionTitle += 'At ' + settings['stop_timeType']
            if(settings['stop_sunOffset']) sectionTitle += settings['stop_sunOffset'] + ' minutes '
            if(settings['stop_sunType'] && settings['stop_sunType'] != 'at') sectionTitle += settings['stop_sunType'] + ' ' + settings['stop_timeType']
            if(stopTimeComplete) sectionTitle += ' ' + getSunriseTime(settings['stop_timeType'],settings['stop_sunOffset'],settings['stop_sunType'])
        }

        if(settings['start_timeType']) sectionTitle += '</b>'
    }

    section(hideable: hideable, hidden: hidden, sectionTitle){
        if(settings['start_time'] && settings['start_time'] == settings['stop_time']) displayError('You can\'t have the same time to start and stop.')

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
            } else if(settings['stop_timeType'] && settings['stop_timeType'] != 'none'){
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
    if(!settings['start_timeType'] || !settings['stop_timeType']) return
    if(!startTimeComplete || !stopTimeComplete) return
    inputTitle = 'On these days (optional; defaults to all days):'
    if(!settings['days']) inputTitle = 'On which days (optional; defaults to all days)?'
    input 'days', 'enum', title: inputTitle, multiple: true, width: 12, options: ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday'], submitOnChange:true

    return
}

def displayMonthsOption(){
    if(!settings['start_timeType'] || !settings['stop_timeType']) return
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
    
    if(settings['start_sunOffset'] && !validateSunriseMinutes(settings['start_sunOffset'])) displayWarning('Time ' + settings['start_sunType'] + ' ' + settings['start_timeType'] + ' is ' + (Math.round(settings['start_sunOffset']) / 60) + ' hours. That\'s probably wrong.')

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
    if(settings['stop_sunOffset'] && !validateSunriseMinutes(settings['stop_sunOffset'])) displayWarning('Time ' + settings['stop_sunType'] + ' ' + settings['stop_timeType'] + ' is ' + (Math.round(settings['stop_sunOffset']) / 60) + ' hours. That\'s probably wrong.')

    if(!settings['stop_timeType']){
        width = 12
        inputTitle = highlightText('Stop time (click to select)?')
        input 'stop_timeType', 'enum', title: inputTitle, multiple: false, width: width, options: ['none':'Don\'t stop','time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)'], submitOnChange:true
    } else {
        if(!settings['stop_timeType']){
            width = 12
        } else if(settings['stop_timeType'] == 'time' || !settings['stop_sunType'] || settings['stop_sunType'] == 'at'){
            width = 6
        } else if(stop_sunType){
            width = 4
        }
        input 'stop_timeType', 'enum', title: 'Stop time option:', multiple: false, width: width, options: ['none':'Don\'t stop','time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)'], submitOnChange:true
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
    if(settings["${type}_timeType"] == 'none') return true
    if(settings["${type}_timeType"] == 'time' && !settings["${type}_time"]) return false
    if((settings["${type}_timeType"] == 'sunrise' || settings["${type}_timeType"] == 'sunset') && !settings["${type}_sunType"]) return false
    if((settings["${type}_timeType"] == 'sunrise' || settings["${type}_timeType"] == 'sunset') && (settings["${type}_sunType"] && settings["${type}_sunType"] != 'at') && !settings["${type}_sunOffset"]) return false

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
    if(settings["${type}_sunOffset"]) 'Minutes ' + settings["${type}_sunType"] + ' ' + settings["${type}_timeType"] + ':'
    input "${type}_sunOffset", 'number', title: inputTitle, width: 4, submitOnChange:true
    
    if(!settings["${type}_sunOffset"] || !validateSunriseMinutes(settings["${type}_sunOffset"])) message = "Enter the number of minutes " + settings["${type}_sunType"] + " " + settings["${type}_timeType"] + " to start the schedule. Required."
    if(!settings["${type}_sunOffset"]) {
        displayInfo(message)
    } else if(!validateSunriseMinutes(settings["${type}_sunOffset"])){
        displayWarning(message)
    }
}

def displaySkipStartOption(){
    input 'skipStart', 'bool', defaultValue: true, title: 'Run when available?', submitOnChange:true
    if(skipStart){
    helpTip = 'If the scheduled time is missed such as due to Hubitat being offline, it will run when available'
        if(stopTime) helpTip += ' (if prior to Stop time)'
        helpTip += '. Click to run only at Start time.'
    } else {
        helpTip = 'If the scheduled time is missed such as due to Hubitat being offline, it will NOT run when available.'
    }
    displayInfo(helpTip)
}

def displayBrightnessOption(){
    if(settings['deviceType'] != 'light') return
    if(!settings['start_action']) return
    if(settings['stop_timeType'] && settings['stop_timeType'] != 'none' && !settings['stop_action']) return

    hidden = true
    if(!validateStartLevel()) hidden = false
    if(!validateStopLevel()) hidden = false
    if(settings['start_level'] && (!settings['stop_level'] && settings['stop_timeType'] && settings['stop_timeType'] != 'none')) hidden = false
    if(!settings['start_level'] && settings['stop_level']) hidden = false
    if(settings['start_level'] && settings['start_level'] == settings['stop_level']) hidden = false

    width = 12
    if(settings['stop_timeType'] && settings['stop_timeType'] != 'none') width = 6

    sectionTitle = ''
    if(!settings['start_level'] && !settings['stop_level']) sectionTitle = 'Click to set brightness (optional)'
    if(settings['start_level'] && (!settings['stop_level'] || settings['start_level'] == settings['stop_level'])) sectionTitle = '<b>On start, set brightness to ' + settings['start_level'] + '%</b>'
    if(settings['stop_level'] && !settings['start_level']) sectionTitle += '<b>On stop, set brightness to ' + settings['stop_level'] + '%</b>'
    if(settings['start_level'] && settings['stop_level'] && settings['start_level'] != settings['stop_level']) sectionTitle = "<b>Start to end: Change brightness from $settings.start_level% to $settings.stop_level%</b>"
 

    section(hideable: true, hidden: hidden, sectionTitle){
        if(!validateStartLevel()) displayError('Start level must be from 1 to 100. Correct start brightness.')
        if(!validateStopLevel()) displayError('Stop level must be from 1 to 100. Correct stop brightness.')
        if(settings['start_level'] && settings['start_level'] == settings['stop_level']) displayWarning('Starting and ending brightness are both set to ' + settings['start_level'] + '. This won\'t hurt anything, but the Stop brightness setting won\'t actually <i>do</i> anything.')

        inputTitle = 'Set brightness at start?'
        if(settings['start_level']) inputTitle = 'Set brightness at start:'
        input 'start_level', 'number', title: inputTitle, width: width, submitOnChange:true
        inputTitle = 'Set brightness at stop?'
        if(settings['stop_level']) inputTitle = 'Set brightness at stop:'
        if(settings['stop_timeType'] && settings['stop_timeType'] != 'none') input 'stop_level', 'number', title: inputTitle, width: width, submitOnChange:true

        message = 'Brightness is percentage from 1 to 100.'
        if(!settings['start_level'] && settings['stop_timeType'] == 'none') message = 'Enter the percentage of brightness when turning on, from 1 to 100.'
        if(!settings['start_level'] && !settings['stop_level']) message = 'Enter the percentage of brightness when turning on, from 1 to 100, when starting and/or ending the schedule. If entering both starting and ending brightness, it will transition from starting to ending brightness for the duration of the schedule. Either starting or ending brightness is required (or unselect "Change brightness").'
        if((!settings['start_level'] || !settings['stop_level']) && settings['stop_timeType'] != 'none') message = 'Enter the percentage of brightness when turning on, from 1 to 100. If entering both starting and ending brightness, it will transition from starting to ending brightness for the duration of the schedule.'

        if(!validateStartLevel() || !validateStopLevel()) {
            displayError(message)
        } else {
            displayInfo(message)
        }
    }
}

def displayTemperatureOption(){
    if(settings['deviceType'] != 'light') return
    if(!settings['start_action']) return
    if(settings['stop_timeType'] && settings['stop_timeType'] != 'none' && !settings['stop_action']) return

    hidden = true
    if(!validateStartTemp()) hidden = false
    if(!validateStopTemp()) hidden = false
    if(settings['start_level'] && (!settings['stop_level'] && settings['stop_timeType'] && settings['stop_timeType'] != 'none')) hidden = false
    if(!settings['start_temp'] && settings['stop_temp']) hidden = false
    if(settings['start_temp'] && settings['start_temp'] == settings['stop_temp']) hidden = false
    if(settings['start_temp'] && (settings['start_hue'] || settings['start_sat'])){
        hidden = false
        colorWarning = true
    }
    if(settings['stop_temp'] && (settings['stop_hue'] || settings['stop_sat'])){
        hidden = false
        colorWarning = true
    }

    width = 12
    if(settings['stop_timeType'] && settings['stop_timeType'] != 'none') width = 6

    sectionTitle = ''
    if(!settings['start_temp'] && !settings['stop_temp']) sectionTitle = 'Click to set temperature color (optional)'
    if(settings['start_temp'] && (!settings['stop_temp'] || settings['start_temp'] == settings['stop_temp'])) sectionTitle = '<b>On start, set temperature color to ' + settings['start_temp'] + 'K</b>'
    if(settings['stop_temp'] && !settings['start_temp']) sectionTitle += '<b>On stop, set temperature color to ' + settings['stop_temp'] + 'K</b>'
    if(settings['start_temp'] && settings['stop_temp'] && settings['start_temp'] != settings['stop_temp']) sectionTitle = '<b>Start to end: Change temperature color from ' + settings['start_temp'] + 'K to ' + settings['stop_temp'] + 'K</b>'

    section(hideable: true, hidden: hidden, sectionTitle){
        if(!validateStartTemp()) displayError('Start temperature color must be from 1800 to 5400. Correct start temperature color.')
        if(!validateStopTemp()) displayError('Stop temperature color must be from 1800 to 5400. Correct stop temperature color.')
        if(colorWarning) displayWarning('Color options have been entered that conflict with temperature color. Color will take precedence over temperature color.')
        if(settings['start_temp'] && settings['start_temp'] == settings['stop_temp']) displayWarning('Starting and ending temperature color are both set to ' + settings['start_temp'] + '. This won\'t hurt anything, but the Stop brightness setting won\'t actually <i>do</i> anything.')

        inputTitle = 'Set temperature color at start?'
        if(settings['start_temp']) inputTitle = 'Set temperature color at start:'
        input 'start_temp', 'number', title: inputTitle, width: width, submitOnChange:true
        inputTitle = 'Set temperature color at stop?'
        if(settings['stop_temp']) inputTitle = 'Set temperature color at start:'
        if(settings['stop_timeType'] && settings['stop_timeType'] != 'none') input 'stop_temp', 'number', title: inputTitle, width: width, submitOnChange:true

        message = 'Temperature color is from 1800 to 5400, where daylight is 5000, cool white is 4000, and warm white is 3000.'
        if(!settings['start_temp'] && !settings['stop_temp']) message = 'Temperature color is in Kelvin from 1800 to 5400, when starting and/or ending the schedule. Lower values have more red, while higher values have more blue, where daylight is 5000, cool white is 4000, and warm white is 3000. (Optional.)'
        if((!settings['start_temp'] || !settings['stop_temp']) && settings['stop_timeType'] != 'none') message = 'Temperature color is from 1800 to 5400, where daylight is 5000, cool white is 4000, and warm white is 3000. If entering both starting and ending temperature, it will transition from starting to ending temperature for the duration of the schedule.' 
        if(!validateStartTemp() || !validateStopTemp()) {
            displayError(message)
        } else {
            displayInfo(message)
        }
    }
}

def displayColorOption(){
    if(settings['deviceType'] != 'light') return
    if(!settings['start_action']) return
    if(settings['stop_timeType'] && settings['stop_timeType'] != 'none' && !settings['stop_action']) return

    validateStartHue = validateHue(settings['start_hue'])
    validateStopHue = validateHue(settings['stop_hue'])
    validateStartSat = validateSat(settings['start_sat'])
    validateStopSat = validateSat(settings['stop_sat'])

    unit = parent.getHiRezHue(settings['device']) ? 'Â°' : '%'
    
    hidden = true

    if(!validateStartHue) hidden = false
    if(!validateStopHue) hidden = false
    if(!validateStartSat) hidden = false
    if(!validateStopSat) hidden = false
    if(settings['start_hue'] && (!settings['stop_hue'] && settings['stop_timeType'] || settings['stop_timeType'] != 'none')) hidden = false
    if(!settings['start_hue'] && settings['stop_hue']) hidden = false
    if(settings['start_sat'] && (!settings['stop_sat'] && settings['stop_timeType'] || settings['stop_timeType'] != 'none')) hidden = false
    if(!settings['start_sat'] && settings['stop_sat']) hidden = false
    if(settings['start_hue'] && settings['start_hue'] == settings['stop_hue']) hidden = false
    if(settings['start_sat'] && settings['start_sat'] == settings['stop_sat']) hidden = false
    if(settings['start_hue'] && settings['stop_hue'] && !settings['hueDirection']) hidden = false

    hueWidth = 6
    satWidth = 12
    if(settings['stop_timeType'] && settings['stop_timeType'] != 'none') satWidth = 6
    if(!settings['stop_timeType'] || settings['stop_timeType'] == 'none') hueWidth = 12
    if(settings['start_hue'] && settings['stop_hue'] && settings['start_hue'] != settings['stop_hue']) hueWidth = 4

    sectionTitle = ''
    if(!settings['start_hue'] && !settings['stop_hue'] && !settings['start_sat'] && !settings['stop_hue']) {
        sectionTitle = 'Click to set color (hue and/or saturation) (optional)'
    } else {
        if(settings['start_hue'] && (!settings['stop_hue'] || settings['start_hue'] == settings['stop_hue'])) sectionTitle = '<b>On start, set hue to ' + settings['start_hue'] + "$unit</b>"
        if(settings['stop_hue'] && !settings['start_hue']) sectionTitle += '<b>On stop, set hue to ' + settings['stop_hue'] + "$unit</b>"
        if(settings['start_hue'] && settings['stop_hue'] && settings['start_hue'] != settings['stop_hue']) sectionTitle = '<b>Start to end: Change hue from ' + settings['start_hue'] + "$unit to " + settings['stop_hue'] + "$unit</b>"
        if(settings['hueDirection'] == 'reverse') sectionTitle += '<b> (in reverse order)</b>'
        if(!settings['start_hue'] || !settings['stop_hue'] || !settings['start_sat'] || !settings['stop_hue'] && sectionTitle) sectionTitle += moreOptions
        if((settings['start_hue'] || settings['stop_hue']) && (settings['start_sat'] || settings['stop_sat'])) sectionTitle += '<br>'
        if(settings['start_sat'] && (settings['start_sat'] == settings['stop_sat'])) sectionTitle += '<b>On start, set saturation to ' + settings['start_sat'] + '%</b>'
        if(settings['start_sat'] && !settings['stop_sat']) sectionTitle += '<b>On start, set saturation to ' + settings['start_sat'] + '%</b>'
        if(settings['stop_sat'] && !settings['start_sat']) sectionTitle += '<b>On stop, set saturation to ' + settings['stop_sat'] + '%</b>'
        if(settings['start_sat'] && settings['stop_sat'] && settings['start_sat'] != settings['stop_sat']) sectionTitle += '<b>Start to end: Change saturation from ' + settings['start_sat'] + '% to ' + settings['stop_sat'] + '%</b>'
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        if(!validateStartHue && unit == '%') displayError('Start hue must be from 1 to 100. Correct start hue.')
        if(!validateStartHue && unit == 'Â°') displayError('Start hue must be from 1 to 360. Correct start hue.')
        if(!validateStartSat) displayError('Start saturation must be from 1 to 100. Correct start saturation.')
        if(!validateStopHue && unit == '%') displayError('Stop hue must be from 1 to 100. Correct stop hue.')
        if(!validateStopHue && unit == 'Â°') displayError('Stop hue must be from 1 to 360. Correct stop hue.')
        if(!validateStopSat) displayError('Stop saturation must be from 1 to 100. Correct stop saturation.')
        
        if(settings['start_hue'] && settings['start_hue'] == settings['stop_hue']) displayWarning('Starting and ending hue are both set to ' + settings['start_hue'] + '. This won\'t hurt anything, but the Stop hue setting won\'t actually <i>do</i> anything.')
        if(settings['start_sat'] && settings['start_sat'] == settings['stop_sat']) displayWarning('Starting and ending saturation are both set to ' + settings['start_sat'] + '. This won\'t hurt anything, but the Stop saturation setting won\'t actually <i>do</i> anything.')

        
        inputTitle = 'Set hue at start?'
        if(settings['start_hue']) inputTitle = 'Set hue at start:'
        input 'start_hue', 'number', title: inputTitle, width: hueWidth, submitOnChange:true
        
        inputTitle = 'Set hue at stop?'
        if(settings['stop_hue']) inputTitle = 'Set hue at stop:'
        if(settings['stop_timeType'] && settings['stop_timeType'] != 'none') input 'stop_hue', 'number', title: inputTitle, width: hueWidth, submitOnChange:true
        
        if(settings['start_hue'] && settings['stop_hue']){
            if(hiRezHue && settings['start_hue'] < settings['stop_hue']){
                forwardSequence = '90, 91, 92  ... 270, 271, 272'
                reverseSequence = '90, 89, 88 ... 2, 1, 360, 359 ... 270, 269, 268'
            }
            if(!hiRezHue && settings['start_hue'] < settings['stop_hue']){
                forwardSequence = '25, 26, 27  ... 73, 74, 75'
                reverseSequence = '25, 24, 23 ... 2, 1, 100, 99 ... 77, 76, 75'
            }
            if(hiRezHue && settings['start_hue'] > settings['stop_hue']){
                forwardSequence = '270, 271, 272 ... 359, 360, 1, 2 ... 90, 91, 92'
                reverseSequence = '270, 269, 268 ... 75, 74, 73'
            }
            if(!hiRezHue && settings['start_hue'] > settings['stop_hue']){
                forwardSequence = '75, 76, 77 ... 99, 100, 1, 2 ... 23, 24, 25'
                reverseSequence = '75, 74, 73 ... 27, 26, 25'
            }
        
        inputTitle = highlightText('Which order to change hue?')
        if(settings['hueDirection']) inputTitle = 'Order to change hue:'
        input 'hueDirection', 'enum', title: inputTitle, width: hueWidth, submitOnChange:true, options: ['forward': forwardSequence, 'reverse': reverseSequence]
        }
        
        if(settings['start_hue'] && settings['stop_hue']){
            if(hiRezHue && settings['hueDirection']) message = 'Red = 0 hue (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270 (may vary by device). Optional.'
            if(!hiRezHue && settings['hueDirection']) message = 'Red = 0 hue (and 100). Orange = 8; yellow = 16; green = 33; turquiose = 50; blue = 66; purple = 79 (may vary by device). Optional.'
            if(hiRezHue && !settings['hueDirection']) message = 'Red = 0 hue (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270 (may vary by device). It will transition from starting to ending hue for the duration of the schedule. For "order", if for instance, a start value of 1 and stop value of 26 is entered, allows for chosing whether it would change from red to yellow then blue, or from red to purple, blue, then green. Optional.'
            if(!hiRezHue && !settings['hueDirection']) message = 'Red = 0 hue (and 100). Orange = 8; yellow = 16; green = 33; turquiose = 50; blue = 66; purple = 79 (may vary by device). It will transition from starting to ending hue for the duration of the schedule. For "order", if for instance, a start value of 1 and stop value of 26 is entered, allows for chosing whether it would change from red to yellow then blue, or from red to purple, blue, then green. Optional.'
        }
        if(!settings['start_hue'] || !settings['stop_hue']){
            message = 'Hue is percent from 0 to 100 around a color wheel, where red is 0 (and 100). Orange = 8; yellow = 16; green = 33; turquiose = 50; blue = 66; purple = 79 (may vary by device).'
            if(hiRezHue) message = 'Hue is degrees from 0 to 360 around a color wheel, where red is 0 (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270 (may vary by device).'
            if(settings['stop_timeType'] && settings['stop_timeType'] != 'none') message += ' If entering both starting and ending hue, it will transition from starting to ending hue for the duration of the schedule.'
            message += ' Optional.'
        }
        if(validateStartHue && validateStartSat && validateStopHue && validateStopSat){
            displayInfo(message)
        } else {
            displayError(message)
        }
        
        inputTitle = 'Set saturation at start?'
        if(settings['start_sat']) inputTitle = 'Set saturation at start:'
        input 'start_sat', 'number', title: inputTitle, width: satWidth, submitOnChange:true
        inputTitle = 'Set saturation at stop?'
        if(settings['stop_sat']) inputTitle = 'Set saturation at stop:'
        if(settings['stop_timeType'] && settings['stop_timeType'] != 'none') input 'stop_sat', 'number', title: inputTitle, width: satWidth, submitOnChange:true

        if(settings['start_hue'] && settings['stop_hue']) message = 'Saturation is the percentage amount of color tint displayed, from 1 to 100, where 1 is hardly any color tint and 100 is full color. If entering both starting and ending saturation, it will transition from starting to ending saturation for the duration of the schedule.'
        if(!settings['start_hue'] || !settings['stop_hue']){
            message = 'Saturation is the percentage amount of color tint displayed, from 1 to 100, where 1 is hardly any color tint and 100 is full color.'
            if(settings['stop_timeType'] && settings['stop_timeType'] != 'none') message += ' If entering both starting and ending saturation, it will transition from starting to ending saturation for the duration of the schedule.'
            message += ' Optional.'
        }

        if(!validateStartTemp() || !validateStopTemp()) {
            displayError(message)
        } else {
            displayInfo(message)
        }
    }
}

def displayChangeModeOption(){
    if(!settings['start_action']) return
    if(settings['stop_timeType'] && settings['stop_timeType'] != 'none' && !settings['stop_action']) return

    hidden = true
    if(settings['startMode'] && (!settings['stopMode'] && settings['stop_timeType'] || settings['stop_timeType'] != 'none')) hidden = false
    if(!settings['startMode'] && settings['stopMode']) hidden = false

    width = 12
    if(settings['stop_timeType'] && settings['stop_timeType'] != 'none') width = 6
    
    sectionTitle = ''
    if(!settings['startMode'] && !settings['stopMode']){
        sectionTitle = 'Click to set Mode change (optional)'
    } else {
        if(settings['startMode']) sectionTitle = '<b>On start, set Mode ' + settings['startMode'] + '</b>'
        if(settings['startMode'] && settings['stopMode']) sectionTitle += '<br>'
        if(settings['stopMode']) sectionTitle += '<b>On stop, set Mode ' + settings['stopMode'] + '</b>'
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        inputTitle = 'Set Hubitat\'s "Mode" on start?'
        if(settings['startMode']) inputTitle = 'Set Hubitat\'s "Mode" on start:'
        input 'startMode', 'mode', title: inputTitle, width: 6, submitOnChange:true
        inputTitle = 'Set Hubitat\'s "Mode" on stop?'
        if(settings['stopMode']) inputTitle = 'Set Hubitat\'s "Mode" on stop:'
        if(settings['stop_timeType'] && settings['stop_timeType'] != 'none') input 'stopMode', 'mode', title: inputTitle, width: width, submitOnChange:true
    }
}

def displayPeopleOption(){
    if(!settings['start_action']) return
    if(settings['stop_timeType'] && settings['stop_timeType'] != 'none' && !settings['stop_action']) return

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

def displayIfModeOption(){
    if(!settings['start_action']) return
    if(settings['stop_timeType'] && settings['stop_timeType'] != 'none' && !settings['stop_action']) return

    sectionTitle = 'Click to select with what Mode (optional)'
    if(settings['ifMode']) sectionTitle = '<b>Only with Mode: ' + settings['ifMode'] + '</b>'

    section(hideable: true, hidden: true, sectionTitle){
        inputTitle = 'Only run if Mode?'
        if(settings['ifMode']) inputTitle = 'Only run if Mode:'
        input 'ifMode', 'mode', title: inputTitle, width: 12, submitOnChange:true

        message = 'This will limit the schedule from running while Hubitat\'s Mode is as selected.'
        if(settings['ifMode']) message = 'This will limit the schedule from running while Hubitat\'s Mode is ' + settings['ifMode'] + '.'

        displayInfo(message)
    }
}

def compareDeviceLists(list1,list2){
   list1.each{first->
        list2.each{second->
            if(first.id == second.id) returnValue = true
        }
    }
    return returnValue
}

def getPlainAction(action){
    if(!action) return 'perform action'
    if(action == 'none') return 'do nothing'
    if(action == 'on') return 'turn on'
    if(action == 'off') return 'turn off'
    if(action == 'toggle') return 'toggle'
    if(action == 'lock') return 'lock'
    if(action == 'unlock') return 'unlock'
}

/*
disableAll - bool - Flag to disable all schedules
disable - bool - Flag to disable this single schedule
start_action - enum (none, on, off, toggle) - What to do with device at starting time
stop_action - enum (none, on, off, toggle) - What to do with device at stopping time
device - capability.switch - Device(s) being scheduled
days - enum (Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday) - Day(s) of the week schedule will run
months - enum (Jan, Feb, Mar, Apr, May, Jun, Jul, Aug, Sep, Oct, Nov, Dec) - Months(s) of the year schedule will run
start_timeType - enum (time, sunrise, sunset) - Sets whether start time is a specific time, or based on sunrise or sunset
start_time - time - Start Time (only displays when start_timeType = "time")
start_sunType - enum (at, before, after) - Sets whether start time is sunrise/sunset time, or uses positive or negative offset (only displays if start_timeType = "sunrise" or "sunset")
start_sunOffset - number (1-) - Number of minutes before/after sunrise/sunset (only displays if start_timeType = "sunrise" or "sunset" and start_sunType = "before" or "after")
stop_timeType - emum (none, time, sunrise, sunset) - Sets whether there is a stop time, and whether it is a specific time, or based on sunrise or sunset
stop_time - time - Stop Time (only displays when stop_timeType = "time")
stop_sunType - enum (at, before, after) - Sets whether stop time is sunrise/sunset time, or uses positive or negative offset (only displays if start_timeType = "sunrise" or "sunset")
stop_sunOffset - number (1-) - Number of minutes before/after sunrise/sunset (only displays if start_timeType = "sunrise" or "sunset" and start_sunType = "before" or "after")
start_level - number (1-100) - Level to set at start time
stop_level - number (1-100) - Level to set at stop time
start_temp - number (1800-5400) - Temperature to set at start time
stop_temp - number (1800-5400) - Temperature to set at stop time
start_hue - number (1-100) - Hue to set at start time
stop_hue - number (1-100) - Hue to set at stop time
hueDirection - enum (forward, reverse) - "Direction" in which to change hue; only displays if start_hue and stop_hue have values
start_sat - number (1-100) - Sat to set at start time
stop_sat - number (1-100) - Sat to set at stop time
modeChangeOn - mode - Mode to set at start time
//modeChangeOff - mode - Mode to set at stop time [doesn't currently exist]
ifMode - mode - Mode system must have for schedule to run
ifPeople - enum (everyone, noone) - who must be present for schedule to run
*/

/* ************************************************************************ */
/*                                                                          */
/*                          End display functions.                          */
/*                                                                          */
/* ************************************************************************ */

def installed() {
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
    state.logLevel = getLogLevel()
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    // Need to deal with dim and brighten too (and even hue/sat)

    unschedule()

    if(disable) {
        state.disable = true
        return
    } else {
        state.disable = false
    }

    // Clean up settings values
    if(start_timeType != 'sunrise' && start_timeType != 'sunset') {
        settings.start_sunType = null
        settings.start_sunOffset = null
    }
    if(start_sunType == 'at') settings.start_sunOffset = null
    if(stop_timeType != 'sunrise' && stop_timeType != 'sunset') {
        settings.stop_sunType = null
        settings.stop_sunOffset = null
    }
    if(stop_sunType == 'at') settings.stop_sunOffset = null
    if(stop_timeType == 'none'){
        settings.stop_action = null
        settings.stop_sunType = null
        settings.stop_sunOffset = null
        settings.stop_level = null
        settings.stop_temp = null
        settings.stop_hue = null
        settings.stop_sat = null
    }
    if(!start_level || start_level == stop_level) settings.stop_level = null
    if(!start_temp || start_temp == stop_temp) settings.stop_temp = null
    if(!start_hue || start_hue == stop_hue) settings.stop_hue = null
    if(!start_sat || start_sat == stop_sat) settings.stop_sat = null

    // Set start time, stop time, and total seconds
    //if(!setTime()) return false

    subscribeDevices()
    setDailySchedules()

    //Initialize deviceState variable
    parent.isOnMulti(settings['device'])

    atomicState.defaults = null
    defaults = getStartDefaults()

    if(defaults){
        if(defaults.'level') defaults.'level'.'time' = 'start'
        if(defaults.'temp') defaults.'temp'.'time' = 'start'
        if(defaults.'sat') defaults.'sat'.'time' = 'start'
        if(defaults.'hue') defaults.'hue'.'time' = 'start'

        parent.updateLevelsMulti(settings['device'],defaults,app.label)

    }
    
    // If defaults array required, thenwe need to create null values for if simply turning on/off
        if(getScheduleActive()){
            parent.setStateMulti(settings['device'],app.label)
            runIncrementalSchedule()
        }
    return true
}

def handleStateChange(event){
    //if(parent.isOn(event.device,app.label) && event.value == 'on') return
    //if(!parent.isOn(event.device,app.label) && event.value == 'off') return
    putLog(1039,'trace',"Captured manual state change for $event.device to turn $event.value")
    parent.updateStateSingle(event.device,event.value,app.label)

    return
}

def handleLevelChange(event){
    levelChange = parent.getLastLevelChange(event.device, app.label)
    if(!levelChange) return

    if(levelChange.'appId' != app.id) return
    // if appLabel = "Time", then return

    value = parent.convertToInteger(event.value)

    if(levelChange.'currentLevel' == value && event.device.currentColorMode == 'RGB') return
    if(levelChange.'priorLevel' == value && levelChange.'timeDifference' < 5000) return

    defaults = ['level':['startLevel':value,'priorLevel':levelChange.'currentLevel','appId':'manual']]

    parent.updateLevelsSingle(event.device,defaults,app.label)
    putLog(1060,'warn',"Captured manual change for $event.device level to $event.value% - last changed " + levelChange.'currentLevel' + " " + levelChange.'timeDifference' + "ms")

    return
}

def handleTempChange(event){
    tempChange = parent.getLastTempChange(event.device, app.label)
    if(!tempChange) return
    // Temp can be different by + .5% (25 at 5000); always plus, never minus
    if(hueChange.'appId' != app.id) return

    value = parent.convertToInteger(event.value)
    
    if(event.device.currentColorMode == 'CT' && Math.round(tempChange.'currentLevel' / 255) == Math.round(value / 255)) return
    if(event.device.currentColorMode == 'CT' && Math.round(tempChange.'priorLevel' / 255) == Math.round(value / 255) && tempChange.'timeDifference' < 5000) return

    defaults = ['temp':['startLevel':value,'priorLevel':tempChange.'currentLevel','appId':'manual']]

    putLog(1078,'warn',"Captured manual temperature change for $event.device to temperature color " + value + "K - last changed " + tempChange.'timeDifference' + "ms")
        parent.updateLevelsSingle(event.device,defaults,app.label)

    return
}

def handleHueChange(event){
    hueChange = parent.getLastHueChange(event.device, app.label)
    if(!hueChange) return
    if(hueChange.'appId' != app.id) return

    value = parent.convertToInteger(event.value)
    
    if(hueChange.'currentLevel' == value && event.device.currentColorMode == 'RGB') return
    if(hueChange.'priorLevel' == value && hueChange.'timeDifference' < 5000 && event.device.currentColorMode == 'RGB') return

    defaults = ['hue':['startLevel':value,'priorLevel':hueChange.'currentLevel','appId':'manual']]

    putLog(1096,'warn',"Captured manual change for $event.device to hue $value% - last changed " + hueChange.'timeDifference' + "ms")
    parent.updateLevelsSingle(event.device,defaults,app.label)

    return
}

def handleSatChange(event){
    satChange = parent.getLastSatChange(event.device, app.label)
    if(!satChange) return
    if(satChange.'appId' != app.id) return

    value = parent.convertToInteger(event.value)

    if(satChange.'currentLevel' == value && event.device.currentColorMode == 'RGB') return
    if(satChange.'priorLevel' == value && satChange.'timeDifference' < 5000 && event.device.currentColorMode == 'RGB') return

    defaults = ['sat':['startLevel':value,'priorLevel':event.device.currentSat,'appId':'manual']]
    putLog(1113,'warn',"Captured manual change for $event.device to saturation $value% - last changed " + satChange.'timeDifference' + "ms (to " + satChange.'currentLevel')
    parent.updateLevelsSingle(event.device,defaults,app.label)

    return
}

def setDailySchedules(type = null){
    // Set start time, stop time, and total seconds
    if(!setTime()) return false
    if(type != 'stop') {
        unschedule(runDailyStartSchedule)
    } else if(type != 'start'){
        unschedule(runDailyStopSchedule)
    }
    
    // Schedule dailyStart, either every day or with specific days
    if(type != 'stop'){
        startHours = new Date(atomicState.start).format('HH').toInteger()
        startMinutes = new Date(atomicState.start).format('mm').toInteger()
    }
    if(type != 'start' && atomicState.stop){
        stopHours = new Date(atomicState.stop).format('HH').toInteger()
        stopMinutes = new Date(atomicState.stop).format('mm').toInteger()
    }

    days = '* * ?'

    // Schedule start
    if(type != 'stop') {
        scheduleString = "0 $startMinutes $startHours $days"
        // Need to pause or else Hubitat may run runDailyStartSchedule immediately and cause a loop
        runIn(1,setStartSchedule, [data: ['scheduleString': scheduleString]])
        putLog(1145,'debug',"Scheduling runDailyStartSchedule for " + parent.normalPrintDateTime(atomicState.start) + " ($scheduleString)")
    }

    if((type != 'start') && atomicState.stop){
        scheduleString = "0 $stopMinutes $stopHours $days"
        // Need to pause or else Hubitat may run runDailyStopSchedule immediately and cause a loop
        runIn(1,setStopSchedule, [data: ['scheduleString': scheduleString]])
        putLog(1152,'debug',"Scheduling runDailyStopSchedule for " + parent.normalPrintDateTime(atomicState.stop) + " ($scheduleString)")
    }
    return true
}

// Required for offsetting scheduling start and stop by a second, to prevent Hubitat running runDailyStartSchedule immediately
def setStartSchedule(data){
    schedule(data.scheduleString, runDailyStartSchedule)
    putLog(1160,'debug',"Scheduling runDailyStartSchedule for " + parent.normalPrintDateTime(atomicState.start) + " ($data.scheduleString)")
}

def setStopSchedule(data){
    schedule(data.scheduleString, runDailyStopSchedule)
    putLog(1165,'debug',"Scheduling runDailyStopSchedule for " + parent.normalPrintDateTime(atomicState.stop) + " ($data.scheduleString)")
}

// Performs actual changes at time set with start_action
// Called only by schedule set in incrementalSchedule
def runDailyStartSchedule(){
    if(state.disable) return
    
    atomicState.defaults = null
    
    // Set time state variables
    if(!setTime()) return
    
    setDailySchedules('start')

    // If not correct day, exit
    if(!parent.nowInDayList(settings['days'],app.label)) return
    
    // If not correct month, exit
    if(!parent.nowInMonthList(settings['months'],app.label)) return
    
    
    if(settings['start_action'] == 'on' || settings['start_action'] == 'off' || settings['start_action'] == 'toggle') {
        parent.updateStateMulti(settings["device"],settings["start_action"],app.label)
    }
    if(settings['start_level'] || settings['start_temp'] || settings['start_hue'] || settings['start_sat']){
        defaults = getStartDefaults()
        if(defaults.'level') defaults.'level'.'time' = 'start'
        if(defaults.'temp') defaults.'temp'.'time' = 'start'
        if(defaults.'sat') defaults.'sat'.'time' = 'start'
        if(defaults.'hue') defaults.'hue'.'time' = 'start'
        parent.updateLevelsMulti(settings['device'],defaults,app.label)
    }
    
    // If not correct mode, exit
    if(settings['ifMode'] && location.mode != settings['ifMode']) return
    
    if(!parent.getPeopleHome(settings['personHome'],app.label)) return
    if(!parent.getNooneHome(settings['personNotHome'],app.label)) return
    
    // Update and set state (when turning on, off, or toggling)
        parent.setStateMulti(settings['device'],app.label)

    // Set start mode
    if(settings['startMode']) setLocationMode(settings['stopMode'])

    runIn(8,runIncrementalSchedule)

    return
}

// Performs actual changes for incremental schedule
// Called only by schedule set in incrementalSchedule
def runIncrementalSchedule(){
    putLog(1219,'trace','runIncrementalSchedule starting')
    if(!getScheduleActive()) return
    if((settings['start_level'] && settings['stop_level']) || (settings['start_temp'] && settings['stop_temp']) || (settings['start_hue'] && settings['stop_hue']) || (settings['start_sat'] && settings['stop_sat'])) {
        // If it's disabled, keep it active
        // Master will only update when appropriate
        if(state.disable) return
        
        if(!parent.isOnMulti(settings['device'],app.label)) return

        // If mode isn't correct, return false
        if(settings['ifMode'] && location.mode != settings['ifMode']) {
            runIn(60,runIncrementalSchedule)
            return
        }
        
        if(!parent.getPeopleHome(settings['personHome'],app.label) || !parent.getNooneHome(settings['personNotHome'],app.label)) {
            runIn(60,runIncrementalSchedule)
            return true
        }

        parent.setStateMulti(settings['device'],app.label)

        // Reschedule itself
        runIn(8,runIncrementalSchedule)
        putLog(1243,'trace','runIncrementalSchedule exiting')
    }
    return true
}


// Performs actual changes at time set with start_action
// Called only by schedule set in incrementalSchedule
def runDailyStopSchedule(){
    atomicState.defaults = null
    if(state.disable) return

    // Set time state variables
    if(!setTime()) return
    setDailySchedules('stop')

    if(!parent.nowInDayList(settings['days'],app.label)) return
    
    if(!parent.nowInMonthList(settings['months'],app.label)) return
    
        // Update and set state (when turning on, off, or toggling)
    if(settings['stop_action'] == 'on' || settings['stop_action'] == 'off' || settings['stop_action'] == 'toggle') {
        parent.updateStateMulti(settings['device'],settings['stop_action'],app.label)
    }
    
    defaults = getStartDefaults('stop')

    // If level off value (either end level or progressive), use it
    if(settings['stop_level']) {
        defaults.'level'.'time' = 'stop'
    // If no end level but start level, clear the schedule
    } else if(settings['start_level']){
        defaults.'level' = ['time':'stop','appId':app.id]
    }
    if(settings['stop_temp']) {
        defaults.'temp'.'time' = 'stop'
    } else if(settings['start_temp']){
        defaults.'temp' = ['time':'stop','appId':app.id]
    }
    if(settings['stop_hue']) {
        defaults.'hue'.'time' = 'stop'
    } else if(settings['start_hue']){
        defaults.'hue' = ['time':'stop','appId':app.id]
    }
    if(settings['stop_sat']) {
        defaults.'sat'.'time' = 'stop'
    } else if(settings['start_sat']){
        defaults.'sat' = ['time':'stop','appId':app.id]
    }
    
    // Clear out schedule's levels
    parent.updateLevelsMulti(settings['device'],defaults,app.label)
    
    // If not correct mode, exit
    if(settings['ifMode'] && location.mode != settings['ifMode']) return
    
    if(!parent.getPeopleHome(settings['personHome'],app.label)) return
    if(!parent.getNooneHome(settings['personNotHome'],app.label)) return
    
    // Update and set state (when turning on, off, or toggling)
    parent.setStateMulti(settings['device'],app.label)

    // Set stop mode
    if(settings['stopMode']) setLocationMode(settings['stopMode'])

    return
}

def subscribeDevices(){
    subscribe(settings['device'], 'switch', handleStateChange)
    subscribe(settings['device'], 'hue', handleHueChange)
    subscribe(settings['device'], 'saturation', handleSatChange)
    subscribe(settings['device'], 'colorTemperature', handleTempChange)
    subscribe(settings['device'], 'level', handleLevelChange)
    subscribe(settings['device'], 'speed', handleLevelChange)
    subscribe(location, 'systemStart', handleSystemBoot)
    subscribe(location,'timeZone',handleTimezone)
    return
}

def handleSystemBoot(evt){
     if(settings['skipStart']) systemBootActivate()

    initialize()
}

def systemBootActivate(){
    if(state.disable) return
    
    // need to exit if not with 1 hour of start time
    if((new Date().getTime() - atomicState.start) > parent.CONSTHourInMilli()) return

    atomicState.defaults = null

    // Set time state variables
    if(!setTime()) return
    if(atomicState.stop){
        if(!parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return
    }

    // If not correct day, exit
    if(!parent.nowInDayList(settings['days'],app.label)) return
    
    // If not correct month, exit
    if(!parent.nowInMonthList(settings['months'],app.label)) return

    if(settings['start_action'] == 'on' || settings['start_action'] == 'off' || settings['start_action'] == 'toggle') {
        parent.updateStateMulti(settings['device'],settings['start_action'],app.label)
    }
    if(settings['start_level'] || settings['start_temp'] || settings['start_hue'] || settings['start_sat']){
        defaults = getStartDefaults()
        if(defaults.'level') defaults.'level'.'time' = 'start'
        if(defaults.'temp') defaults.'temp'.'time' = 'start'
        if(defaults.'sat') defaults.'sat'.'time' = 'start'
        if(defaults.'hue') defaults.'hue'.'time' = 'start'
        parent.updateLevelsMulti(settings['device'],defaults,app.label)
    }

    // If not correct mode, exit
    if(settings['ifMode'] && location.mode != settings['ifMode']) return
    
    if(!parent.getPeopleHome(settings['personHome'],app.label)) return
    if(!parent.getNooneHome(settings['personNotHome'],app.label)) return

    // Update and set state (when turning on, off, or toggling)
    parent.setStateMulti(settings['device'],app.label)

    // Set start mode
    if(settings['startMode']) setLocationMode(settings['startMode'])

    runIn(8,runIncrementalSchedule)

    return
}

def setTime(){
    if(setStartTime()) {
        setStopTime()
        return true
    }
    return false
}

def setStartTime(){
    if(!settings['start_timeType']) return
    setTime = setStartStopTime('start')
    if(setTime){
        atomicState.start = setTime
        putLog(1391,'info','Start time set to ' + parent.normalPrintDateTime(setTime))
        return true
    }
}

def setStopTime(){
    if(!settings['start_timeType'] || settings['stop_timeType'] == 'none') return
    setTime = setStartStopTime('stop')
    if(setTime){ 
        if(atomicState.start > setTime) setTime = parent.getTomorrow(setTime,app.label)
        atomicState.stop = setTime
        putLog(1402,'info','Stop time set to ' + parent.normalPrintDateTime(setTime))
    }
    return
}

// Sets atomicState.start and atomicState.stop variables
def setStartStopTime(type){
    if(settings["${type}_timeType"] == 'time') return Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSSZ", settings["${type}_time"]).getTime()
    if(settings["${type}_timeType"] == 'sunrise') return (settings["${type}_sunType"] == 'before' ? parent.getSunrise(settings["${type}_sunOffset"] * -1,app.label) : parent.getSunrise(settings["${type}_sunOffset"],app.label))
    if(settings["${type}_timeType"] == 'sunset') return (settings["${type}_sunType"] == 'before' ? parent.getSunset(settings["${type}_sunOffset"] * -1,app.label) : parent.getSunset(settings["${type}_sunOffset"],app.label))
}

// Used by Master to check whether to reschedule incremental
def getTimeVariable(){
    time = [atomicState.start,atomicState.stop]
    return time
}

// Returns true is schedule is not inactive, and allows for no stop time
// Used by getDefaultLevel
def getScheduleActive(){
    // If not correct day, return false
    if(!parent.nowInDayList(settings['days'],app.label)) return
    
    // If not correct month, return false
    if(settings['months'] && !parent.nowInMonthList(settings['months'],app.label)) return

    // If no start or stop time, return false
    if(!atomicState.start || !atomicState.stop) return

    // If not between start and stop time, return false
    if(!parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return

    return true
}

def getScheduleDisabled(){
    // If disabled, return true
    if(state.disable) return true

    // If mode isn't correct, return false
    if(settings['ifMode'] && location.mode != settings['ifMode']) return true
    
    if(!parent.getPeopleHome(settings['personHome'],app.label)) return
    if(!parent.getNooneHome(settings['personNotHome'],app.label)) return

    return false
}

def getStartDefaults(action = 'start'){
    if(atomicState.defaults) return atomicState.defaults
    
    // If no settings, exit
    if(!settings['start_level'] && !settings['stop_level'] && !settings['start_temp'] && !settings['stop_temp'] && !settings['start_hue'] && !settings['stop_hue'] && !settings['start_sat'] && !settings['stop_sat']) return
    
    // If not correct time, exit
    // Shouldn't happen
    if(action == 'start' && !parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return
    
    defaults = [:]

    if(settings['start_level'] || settings['stop_level']) {
        defaults.'level' = [:]
        if(settings['start_level']) defaults.'level'.'startLevel' = parent.convertToInteger(settings['start_level'],app.label)
        if(settings['stop_level']) defaults.'level'.'stopLevel' = parent.convertToInteger(settings['stop_level'],app.label)
        defaults.'level'.'appId' = app.id
    }
    if(settings['start_temp'] || settings['stop_temp']) {
        defaults.'temp' = [:]
        if(settings['start_temp']) defaults.'temp'.'startLevel' = parent.convertToInteger(settings['start_temp'],app.label)
        if(settings['stop_temp']) defaults.'temp'.'stopLevel' = parent.convertToInteger(settings['stop_temp'],app.label)
        defaults.'temp'.'appId' = app.id
    }
    if(settings['start_hue'] || settings['stop_hue']) {
        defaults.'hue' = [:]
        if(settings['start_hue']) defaults.'hue'.'startLevel' = parent.convertToInteger(settings['start_hue'],app.label)
        if(settings['stop_hue']) defaults.'hue'.'stopLevel' = parent.convertToInteger(settings['stop_hue'],app.label)
        defaults.'hue'.'appId' = app.id
    }
    if(settings['start_sat'] || settings['stop_sat']) {
        defaults.'sat' = [:]
        if(settings['start_sat']) defaults.'sat'.'startLevel' = parent.convertToInteger(settings['start_sat'],app.label)
        if(settings['stop_sat']) defaults.'sat'.'stopLevel' = parent.convertToInteger(settings['stop_sat'],app.label)
        defaults.'sat'.'appId' = app.id
    }
    
    // If start and stop, compute seconds and set
    if(atomicState.start && atomicState.stop){
        startHours = new Date(atomicState.start).format('HH').toInteger()
        startMinutes = new Date(atomicState.start).format('mm').toInteger()
        startSeconds = startHours * 3600 + startMinutes * 60
        totalSeconds = (atomicState.stop - atomicState.start) / 1000

        if(settings['start_level'] && settings['stop_level']){
            defaults.'level'.'startSeconds' = startSeconds
            defaults.'level'.'totalSeconds' = totalSeconds
        }
        if(settings['start_temp'] && settings['stop_temp']){
            defaults.'temp'.'startSeconds' = startSeconds
            defaults.'temp'.'totalSeconds' = totalSeconds
        }
        if(settings['start_hue'] && settings['stop_hue']){
            defaults.'hue'.'startSeconds' = startSeconds
            defaults.'hue'.'totalSeconds' = totalSeconds
            defaults.'hue'.'direction' = settings['hueDirection']
        }
        if(settings['start_sat'] && settings['stop_sat']){
            defaults.'sat'.'startSeconds' = startSeconds
            defaults.'sat'.'totalSeconds' = totalSeconds
        }
    }

    atomicState.defaults = defaults
    return defaults
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
        case 'debug':
        break
        case 'trace':
        if(state.logLevel == 5) return true
        if(state.logLevel > 3) return true
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,type = 'trace',message = null){
    if(!checkLog(type)) return
    errorText = ''
    if(type == 'error') errorText = '<font color="red">'
    if(type == 'warn') errorText = '<font color="brown">'
    if(lineNumber) lineText = "(line $lineNumber) "
    if(message) messageText = "-- $message"
    
        logMessage = "$errorText$app.label $lineText $messageText"
    if(errorText) logMessage += '</font>'
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
        case 'debug':
        log.debug(logMessage)
        return true
        case 'trace':
        log.trace(logMessage)
        return true
    }
    return
}
