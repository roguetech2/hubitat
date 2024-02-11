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
*  Name: Master - Time
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master%20-%20Time.groovy
*  Version: 0.7.1.3
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
    infoIcon = '<img src="http://emily-john.love/icons/information.png" width=20 height=20>'
    errorIcon = '<img src="http://emily-john.love/icons/error.png" width=20 height=20>'
    warningIcon = '<img src="http://emily-john.love/icons/warning.png" width=20 height=20>'
    moreOptions = ' (click for more options)'
    expandText = ' (Click to expand/collapse)'

    install = formComplete()
    page(name: 'setup', install: install, uninstall: true) {
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

            peopleError = compareDeviceLists(personHome,personNotHome)
            
            section(){
                displayNameOption()
                displayDevicesTypes()
                displayDevicesOption()
                displayDisableOption()
            }
            
            displayScheduleSection()
            displayActionOption()
            displayLevelsOption('brightness')
            displayLevelsOption('temp')
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
    if(!settings['device']) return false
    if(!settings['start_action']) return false
    if(!settings['stop_action'] && settings['stop_timeType'] != 'none') return false
    if(!validateTimes('start')) return false
    if(!validateTimes('stop')) return false
    if(!validateLevels('start')) return false
    if(!validateLevels('stop')) return false
    if(settings['start_hue'] && settings['stop_hue'] && !settings['hueDirection']) return false
    if(compareDeviceLists(personHome,personNotHome)) return false
    return true
}

def validateLevels(type){
    if(!parent.validateLevel(settings[type + '_brightness'])) return false
    if(!parent.validateTemp(settings[type + '_temp'])) return false
    if(!parent.validateHue(settings[type + '_hue'])) return false
    if(!parent.validateSat(settings[type + '_sat'])) return false
    return true
}

def validateColor(){
    if(!settings['start_hue'] && settings['stop_hue']) return false
    if(!settings['start_sat'] && settings['stop_sat']) return false
    if(settings['start_hue'] && settings['start_hue'] == settings['stop_hue']) return false
    if(settings['start_sat'] && settings['start_sat'] == settings['stop_sat']) return false
    if(settings['start_hue'] && settings['stop_hue'] && !settings['hueDirection']) return false
    if(!parent.validateHue(settings['start_hue'])) return false
    if(!parent.validateHue(settings['stop_hue'])) return false
    if(!parent.validateSat(settings['start_sat'])) return false
    if(!parent.validateSat(settings['stop_sat'])) return false
    return true
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
        if(settings['deviceType'] == 'switch') return 'switches'
        if(settings['deviceType'] == 'fan') return 'fans'
    }

    if(settings['deviceType'] == 'lock') return 'lock'
    if(settings['deviceType'] == 'light') return 'light'
    if(settings['deviceType'] == 'switch') return 'switch'
    if(settings['deviceType'] == 'fan') return 'fan'
}

def displayNameOption(){
    labelText = 'Schedule name'
    labelWidth = 2
    if(!app.label) {
        labelText = 'Set name for this schedule'
        labelWidth = 10
    }
    displayLabel(labelText,labelWidth)
    if(app.label) label title: '', required: false, width: labelWidth,submitOnChange:true
    if(!app.label) {
        label title: '', required: false, width: labelWidth, submitOnChange:true
        displayInfo('Name this schedule. Each schedule must have a unique name.')
    }
}

def displayDevicesTypes(){
    fieldName = 'deviceType'
    
    deviceText = 'device(s)'
    if(deviceCount == 1) deviceText = 'device'
    if(deviceCount && deviceCount > 1) deviceText = 'devices'
    fieldTitle = 'Type of ' + deviceText + ' to schedule:'
    if(!settings[fieldName]) fieldTitle = highlightText('Which type of ' + deviceText + ' to schedule (click to select one)?')
    fieldTitle = addFieldName(fieldTitle,fieldName)
    
    input fieldName, 'enum', title: fieldTitle, options: ['lock': 'Lock(s)','light': 'Light(s)', 'switch': 'Switch(es)', 'fan': 'Fan(s)'], multiple: false, submitOnChange:true
    if(!settings[fieldName]) displayInfo('Light(s) allows selecting dimmable switches. Switch(es) include all lights (and fans).')
}

def displayDevicesOption(){
    if(!settings['deviceType']) return

    capability = 'capability.switchLevel'
    if(settings['deviceType'] == 'light') capability = 'capability.switchLevel'
    if(settings['deviceType'] == 'fan') capability = 'capability.fanControl'
    if(settings['deviceType'] == 'lock') capability = 'capability.lock'

    fieldName = 'device'
    fieldTitle = pluralDevice.capitalize() + ' to schedule (click to select)?'
    if(settings[fieldName]) fieldTitle = pluralDevice.capitalize() + ' being scheduled:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    fieldCapability = 'capability.switchLevel'
    if(settings['deviceType'] == 'light') fieldCapability = 'capability.switchLevel'
    if(settings['deviceType'] == 'fan') fieldCapability = 'capability.fanControl'
    if(settings['deviceType'] == 'lock') fieldCapability = 'capability.lock'
    input fieldName, fieldCapability, title: fieldTitle, multiple: true, submitOnChange:true
}

def displayDisableOption(){
    if(!install) return
    fieldName = 'disable'
    fieldTitle = fieldTitle = 'This schedule is disabled. Reenable it?'
    if(!settings[fieldName]) fieldTitle = 'This schedule is enabled. Disable it?'
    if(settings[fieldName]) displayError(fieldTitle,'True')
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'bool', title: fieldTitle, submitOnChange:true
}

def validateTimes(type){
    if(!settings[type + '_timeType']) return false
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
// Add dislaimer that schedules started will end, even if disabled?
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

    if(!settings['stop_timeType']) hidden = false
    if(settings['start_time'] && settings['start_time'] == settings['stop_time']) hidden = false
    if(settings['disable']) hidden = true
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

def displayActionOption(){
    if(!settings['start_timeType']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return

    hidden = true
    if(!settings['start_action']) hidden = false
    if(!settings['stop_action']) hidden = false
    if(settings['start_action'] == 'none' && (settings['stop_action'] == 'none' || settings['stop_timeType'] == 'none')) hidden = true

    section(hideable: true, hidden: hidden, getActionSectionTitle()){
        displayActionField('start')
        displayActionField('stop')
    }
}

def getActionSectionTitle(){
    if(!settings['start_action'] && !settings['stop_action']) return '<b>Click to set ' + pluralDevice.capitalize() + ' control<b>'
    if(settings['start_action'] == 'none' && (settings['stop_action'] == 'none' || settings['stop_timeType'] == 'none')) return 'Doing nothing'
    sectionTitle = ''
    if(settings['start_action']) sectionTitle = '<b>When starting: ' + plainStartAction.capitalize() + '</b>'
    if(!settings['stop_action']) return sectionTitle
    if(settings['stop_action']) sectionTitle += '<br><b>When stopping: ' + plainStopAction.capitalize() + '</b>'
    return sectionTitle
}

def displayActionField(type){
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(type == 'stop' && settings['stop_timeType'] == 'none') return

    ingText = type
    if(type == 'stop') ingText = 'stopp'
    fieldName = type + '_action'
    fieldTitle = 'When ' + ingText + 'ing:'
    if(!settings[fieldName] && settings['deviceType'] == 'lock') fieldTitle = 'When ' + ingText + 'ing, lock or unlock (click to select)?'
    if(!settings[fieldName] && settings['deviceType'] != 'lock') fieldTitle = 'When ' + ingText + 'ing, do what with the ' + pluralDevice + ' (click to select)?'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(!settings[fieldName]) fieldTitle = highlightText(fieldTitle)

    fieldOptions = ['none': 'Don\'t turn on or off (leave as is)','on': 'Turn On', 'off': 'Turn Off', 'toggle': 'Toggle']
    if(settings['deviceType'] == 'lock') fieldOptions = ['none': 'Don\'t lock or unlock','lock': 'Lock', 'unlock': 'Unlock']
    input fieldName, 'enum', title: fieldTitle, multiple: false, width: 12, options: fieldOptions, submitOnChange:true

    if(settings['deviceType'] != 'lock' && !settings[fieldName]) displayInfo('Set whether to turn on, turn off, or toggle the ' + pluralDevice + ' when the schedule ' + type + 's. Select "Don\'t" to control other options (like setting Mode), or to do nothing when ' + ingText + 'ing. Toggle will change the ' + pluralDevice + ' from off to on and vice versa. Required.')
}

def displayLevelsOption(levelType){
    if(settings['deviceType'] != 'light') return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(!settings['start_action']) return
    if(settings['start_action'] == 'off') return

    hidden = true
    if(settings['start_' + levelType] && (!settings['stop_' + levelType] && settings['stop_timeType'] && settings['stop_timeType'] != 'none')) hidden = false
    if(!settings['start_' + levelType] && settings['stop_' + levelType]) hidden = false
    if(settings['start_' + levelType] && settings['start_' + levelType] == settings['stop_' + levelType]) hidden = false
    if(settings['disable']) hidden = true
    
    if(levelType == 'brightness') {
        if(!parent.validateLevel(settings['start_' + levelType])) hidden = false
        if(!parent.validateLevel(settings['stop_' + levelType])) hidden = false
        typeString = 'brightness'
        typeUnit = '%'
    }
    if(levelType == 'temp') {
        if(!parent.validateTemp(settings['start_' + levelType])) hidden = false
        if(!parent.validateTemp(settings['stop_' + levelType])) hidden = false
        typeString = 'temperature color'
        typeUnit = 'K'
    }
       
    sectionTitle = ''
    if(!settings['start_' + levelType] && !settings['stop_' + levelType]) sectionTitle = 'Click to set ' + levelType + ' (optional)'
    if(settings['start_' + levelType] && (!settings['stop_' + levelType] || settings['start_' + levelType] == settings['stop_' + levelType])) sectionTitle = '<b>On start, set ' + typeString + ' to ' + settings['start_' + levelType] + typeUnit + '</b>'
    if(settings['stop_' + levelType] && !settings['start_' + levelType]) sectionTitle += '<b>On stop, set ' + typeString + ' to ' + settings['stop_' + levelType] + typeUnit + '</b>'
    if(settings['start_' + levelType] && settings['stop_' + levelType] && settings['start_' + levelType] != settings['stop_' + levelType]) sectionTitle = '<b>Start to end: Change ' + typeString + ' from ' + settings['start_' + levelType] + typeUnit + ' to ' + settings['stop_' + levelType] + typeUnit + '</b>'
 
    section(hideable: true, hidden: hidden, sectionTitle){

        if(settings['start_' + levelType] && settings['start_' + levelType] == settings['stop_' + levelType]) displayWarning('Starting and ending ' + typeString + ' are both set to ' + settings['start_' + levelType] + '. This won\'t hurt anything, but the Stop ' + settings['start_' + levelType] + ' setting won\'t actually <i>do</i> anything.')
        
        displayLevelsField(levelType,'start')
        displayLevelsField(levelType,'stop')
        displayInfo(getLevelsMessage(levelType))
    }
}

def displayLevelsField(levelType, startType){
    if(startType == 'stop' && !settings['stop_timeType']) return
    if(startType == 'stop' && settings['stop_timeType'] == 'none') return
    if(startType == 'stop' && !settings['start_' + levelType]) return
    
    if(startType == 'start' && 'start_action' == 'off'){
        paragraph('',width:6)
        return
    }
    typeString = 'brightness'
    if(levelType == 'temp') typeString = 'temperature color'
    if(levelType == 'hue') typeString = 'hue'
    if(levelType == 'sat') typeString = 'saturation'
    
    fieldName = startType + '_' + levelType
    unitString = ''
    if(levelType == 'hue') unitString = ' (in 360 degrees)'
    fieldTitle = 'Set ' + typeString + ' at start' + unitString + ':'
    if(startType == 'stop') fieldTitle = 'Transition to ' + typeString + ' at stop:'
    fieldWidth = 12
    if(settings['start_' + levelType]) fieldWidth = 6
    if(settings['stop_timeType'] == 'none') fieldWidth = 12
    if(levelType == 'hue' && settings['start_hue'] && settings['stop_hue']) fieldWidth = 4
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'number', title: fieldTitle, width: fieldWidth, submitOnChange:true
}

def getLevelsMessage(levelType){
    if(levelType == 'brightness') {
        typeString = 'brightness'
        typeUnit = '%'
        typeRange = '1 to 100' + typeUnit
    }
    if(levelType == 'temp') {
        typeString = 'temperature color'
        typeUnit = 'K'
        typeRange = '1800 to 5400' + typeUnit
    }
    if(levelType == 'brightness'){
        if(!parent.validateLevel(settings['start_' + levelType])) displayError('Start ' + typeString + ' must be from ' + typeRange + '. Correct start ' + typeString + '.')
        if(!parent.validateLevel(settings['stop_' + levelType])) displayError('Stop ' + typeString + ' must be from ' + typeRange + '. Correct stop ' + typeString + '.')
        //Both entered
        message = typeString.capitalize() + ' is percentage from ' + typeRange + '. It will transition from ' + settings['start_' + levelType] + typeUnit + ' to ' + settings['start_' + levelType] + typeUnit + ' ' + typeString + ' over the duration of the schedule.'
        //Neither entered
        if(!settings['start_' + levelType] && (!settings['stop_' + levelType] && settings['stop_timeType'] != 'none')) message = 'Enter the percentage of ' + typeString + ' when turning on, from ' + typeRange + ' where 0' + typeUnit + ' is off, and 100' + typeUnit + ' is maximum.'
        //One entered
        if(!settings['start_' + levelType] && settings['stop_' + levelType]) typeString.capitalize() + ' is percentage from ' + typeRange + '. If entering both starting and stopping ' + typeString + ', it will transition from ' + settings['start_' + levelType] + typeUnit + ' to stop amount over the duration of the schedule.'
        if(settings['start_' + levelType] && (!settings['stop_' + levelType] && settings['stop_timeType'] != 'none')) typeString.capitalize() + ' is percentage from ' + typeRange + '. If entering both starting and stopping ' + typeString + ', it will transition from ' + settings['start_' + levelType] + typeUnit + ' to stop amount over the duration of the schedule.'
    }
    if(levelType == 'temp'){
        if(!parent.validateTemp(settings['start_' + levelType])) displayError('Start ' + typeString + ' must be from ' + typeRange + '. Correct start ' + typeString + '.')
        if(!parent.validateTemp(settings['stop_' + levelType])) displayError('Stop ' + typeString + ' must be from ' + typeRange + '. Correct stop ' + typeString + '.')
        //Both entered
        message = typeString.capitalize() + ' is Kelvin from ' + typeRange + '. It will transition from ' + settings['start_' + levelType] + typeUnit + ' to ' + settings['start_' + levelType] + typeUnit + ' ' + typeString + ' over the duration of the schedule.'
        //Neither entered
        if(!settings['start_' + levelType] && (!settings['stop_' + levelType] && settings['stop_timeType'] != 'none')) message = 'Enter Kelvin of ' + typeString + ' when turning on, from ' + typeRange  + ' where 5000' + typeUnit + ' is daylight, 3000' + typeUnit + ' is cool white is 4000, and 3000' + typeUnit + ' is warm white.'
        //One entered
        if(!settings['start_' + levelType] && settings['stop_' + levelType]) message = typeString.capitalize() + ' is Kelvin from ' + typeRange + '. If entering both starting and stopping ' + typeString + ', it will transition from ' + settings['start_' + levelType] + typeUnit + ' to stop amount over the duration of the schedule.'
        if(settings['start_' + levelType] && (!settings['stop_' + levelType] && settings['stop_timeType'] != 'none')) message = typeString.capitalize() + ' is Kelvin from ' + typeRange + '. If entering both starting and stopping ' + typeString + ', it will transition from ' + settings['start_' + levelType] + typeUnit + ' to stop amount over the duration of the schedule.'
    }
    return message
}

def displayColorOption(){
    if(settings['deviceType'] != 'light') return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(!settings['start_action']) return
    if(settings['start_action'] == 'off') return

    typeUnit = '°'

    hidden = true
    if(!validateColor()) hidden = false
    if(settings['start_hue'] && (!settings['stop_hue'] && settings['stop_timeType'] || settings['stop_timeType'] != 'none')) hidden = false
    if(settings['start_sat'] && (!settings['stop_sat'] && settings['stop_timeType'] || settings['stop_timeType'] != 'none')) hidden = false
    if(settings['disable']) hidden = true
    if(!validateColor()) hidden = false

    section(hideable: true, hidden: hidden, getColorTitle()){
    
        if(!parent.validateHue(settings['start_hue'])) displayError('Start hue must be from 1 to 360. Correct start hue.')
        if(!parent.validateSat(settings['start_sat'])) displayError('Start saturation must be from 1 to 100. Correct start saturation.')
        if(!parent.validateHue(settings['stop_hue'])) displayError('Stop hue must be from 1 to 360. Correct stop hue.')
        if(!parent.validateSat(settings['stop_sat'])) displayError('Stop saturation must be from 1 to 100. Correct stop saturation.')
        
        if(settings['start_hue'] && settings['start_hue'] == settings['stop_hue']) displayWarning('Starting and ending hue are both set to ' + settings['start_hue'] + '. This won\'t hurt anything, but the Stop hue setting won\'t actually <i>do</i> anything.')
        if(settings['start_sat'] && settings['start_sat'] == settings['stop_sat']) displayWarning('Starting and ending saturation are both set to ' + settings['start_sat'] + '. This won\'t hurt anything, but the Stop saturation setting won\'t actually <i>do</i> anything.')

        displayLevelsField('hue', 'start')
        displayLevelsField('hue', 'stop')
        displayHueDirection()
        
        if(settings['start_hue'] && settings['stop_hue']){
            if(settings['hueDirection']) message = 'Red = 1 hue (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270 (may vary by device). Optional.'
            if(!settings['hueDirection']) message = 'Red = 1 hue (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270 (may vary by device). It will transition from starting to ending hue for the duration of the schedule. For "order", if for instance, a start value of 1 and stop value of 26 is entered, allows for chosing whether it would change from red to yellow then blue, or from red to purple, blue, then green. Optional.'
        }
        if(!settings['start_hue'] || !settings['stop_hue']){
            message = 'Hue is degrees from 1 to 360 around a color wheel, where red is 1 (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270 (may vary by device).'
            if(settings['stop_timeType'] && settings['stop_timeType'] != 'none') message += ' If entering both starting and ending hue, it will transition from starting to ending hue for the duration of the schedule.'
            message += ' Optional.'
        }
        if(parent.validateHue(settings['start_hue']) && parent.validateSat(settings['start_sat']) && parent.validateHue(settings['stop_hue']) && parent.validateSat(settings['stop_sat'])){
            displayInfo(message)
        } else {
            displayError(message)
        }
        displayLevelsField('sat', 'start')
        displayLevelsField('sat', 'stop')

        if(settings['start_hue'] && settings['stop_hue']) message = 'Saturation is the percentage amount of color tint displayed, from 1 to 100, where 1 is hardly any color tint and 100 is full color. If entering both starting and ending saturation, it will transition from starting to ending saturation for the duration of the schedule.'
        if(!settings['start_sat'] || !settings['stop_sat']){
            message = 'Saturation is the percentage amount of color tint displayed, from 1 to 100, where 1 is hardly any color tint and 100 is full color.'
            if(settings['stop_timeType'] && settings['stop_timeType'] != 'none') message += ' If entering both starting and ending saturation, it will transition from starting to ending saturation for the duration of the schedule.'
            message += ' Optional.'
        }
        displayInfo(message)
    }
}

def displayHueDirection(){
    if(!settings['start_hue']) return
    if(!settings['stop_hue']) return

    if(settings['start_hue'] < settings['stop_hue']){
        forwardSequence = '90, 91, 92  ... 270, 271, 272'
        reverseSequence = '90, 89, 88 ... 2, 1, 360, 359 ... 270, 269, 268'
    }
    if(settings['start_hue'] > settings['stop_hue']){
        forwardSequence = '270, 271, 272 ... 359, 360, 1, 2 ... 90, 91, 92'
        reverseSequence = '270, 269, 268 ... 75, 74, 73'
    }

    fieldName = 'hueDirection'
    fieldTitle = 'Order to change hue:'
    if(!settings[fieldName]) fieldTitle = 'Which order to change hue?'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(!settings[fieldName]) fieldTitle = highlightText(fieldTitle)
    input fieldName, 'enum', title: fieldTitle, width: 4, submitOnChange:true, options: ['forward': forwardSequence, 'reverse': reverseSequence]
}

def getColorTitle(){
    if(!settings['start_hue'] && !settings['stop_hue'] && !settings['start_sat'] && !settings['stop_sat'])  return 'Click to set color (hue and/or saturation) (optional)'
    typeUnit = '°'
    sectionTitle = ''
    
    if(settings['start_hue'] && !settings['stop_hue']) sectionTitle = '<b>On start, set hue to ' + settings['start_hue'] + typeUnit + '</b>'
    if(settings['stop_hue'] && !settings['start_hue']) sectionTitle = '<b>On stop, set hue to ' + settings['stop_hue'] + typeUnit + '</b>'
    if(settings['start_hue'] && settings['stop_hue']) sectionTitle = '<b>Start to end: Change hue from ' + settings['start_hue'] + typeUnit + ' to ' + settings['stop_hue'] + typeUnit + '</b>'
    if(settings['hueDirection'] == 'reverse') sectionTitle += '<b> (in reverse order)</b>'
    
    if(!settings['start_sat'] && !settings['stop_sat']) return sectionTitle + moreOptions
    
    if(settings['start_hue'] || settings['stop_hue']) sectionTitle += '<br>'
    if(settings['start_sat'] && !settings['stop_sat']) sectionTitle += '<b>On start, set saturation to ' + settings['start_sat'] + '%</b>'
    if(settings['stop_sat'] && !settings['start_sat']) sectionTitle += '<b>On stop, set saturation to ' + settings['stop_sat'] + '%</b>'
    if(settings['start_sat'] && settings['stop_sat']) sectionTitle += '<b>Start to end: Change saturation from ' + settings['start_sat'] + '% to ' + settings['stop_sat'] + '%</b>'

    if(!settings['start_sat'] || !settings['stop_sat']) return sectionTitle + moreOptions
    if(!validateColor()) return sectionTitle + moreOptions
    return sectionTitle
}

def displayChangeModeOption(){
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(!settings['start_action']) return

    hidden = true
    if(settings['startMode'] && (!settings['stopMode'] && settings['stop_timeType'] || settings['stop_timeType'] != 'none')) hidden = false
    if(!settings['startMode'] && settings['stopMode']) hidden = false
    if(settings['disable']) hidden = true

    width = 12
    if(settings['stop_timeType'] && settings['stop_timeType'] != 'none') width = 6
    
    sectionTitle = ''
    if(!settings['startMode'] && !settings['stopMode']) sectionTitle = 'Click to set Mode change (optional)'
    if(settings['startMode']) sectionTitle = '<b>On start, set Mode ' + settings['startMode'] + '</b>'
    if(settings['startMode'] && settings['stopMode']) sectionTitle += '<br>'
    if(settings['stopMode']) sectionTitle += '<b>On stop, set Mode ' + settings['stopMode'] + '</b>'

    section(hideable: true, hidden: hidden, sectionTitle){
        displayModeField('start')
        displayModeField('stop')
    }
}

def displayModeField(type){
    if(startType == 'stop' && settings['stop_timeType'] == 'none') return
    fieldName = type + 'Mode'
    fieldTitle = 'Set Hubitat\'s "Mode" on ' + type + '?'
    if(settings['startMode']) fieldTitle = 'Set Hubitat\'s "Mode" on ' + type + ':'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    fieldWitch = 6
    if(settings['stop_timeType'] == 'none') fieldWidth = 12
    input fieldName, 'mode', title: fieldTitle, width: 6, submitOnChange:true
}

def displayPeopleOption(){
    if(!settings['start_action']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
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
    if(settings['disable']) hidden = true
    
    if(!settings['personHome'] && !settings['personNotHome']) sectionTitle = 'Click to select people (optional)'
    if(settings['personHome']) sectionTitle = "<b>With: $withPeople</b>"
    if(settings['personHome'] && settings['personNotHome']) sectionTitle += '<br>'
    if(settings['personNotHome']) sectionTitle += "<b>Without: $withoutPeople</b>"

    section(hideable: true, hidden: hidden, sectionTitle){
        if(compareDeviceLists(personHome,personNotHome)) displayError('You can\'t include and exclude the same person.')
        fieldName = 'personHome'
        fieldTitle = 'Only if any of these people are home (Optional)'
        fieldTitle = addFieldName(fieldTitle,fieldName)
        input fieldName, 'capability.presenceSensor', title: fieldTitle, multiple: true, submitOnChange:true
        
        fieldName = 'personNotHome'
        fieldTitle = 'Only if all these people are NOT home (Optional)'
        fieldTitle = addFieldName(fieldTitle,fieldName)
        input fieldName, 'capability.presenceSensor', title: fieldTitle, multiple: true, submitOnChange:true
    }
}

def displayIfModeOption(){
    if(!settings['start_action']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return

    sectionTitle = 'Click to select with what Mode (optional)'
    if(settings['ifMode']) sectionTitle = '<b>Only with Mode: ' + settings['ifMode'] + '</b>'

    section(hideable: true, hidden: true, sectionTitle){
        fieldName = 'ifMode'
        fieldTitle = 'Only run if Mode?'
        if(settings[fieldName]) fieldTitle = 'Only run if Mode:'
        fieldTitle = addFieldName(fieldTitle,fieldName)
        input fieldName, 'mode', title: fieldTitle, width: 12, submitOnChange:true

        message = 'This will limit the schedule from running while Hubitat\'s Mode is as selected.'
        if(settings[fieldName]) message = 'This will limit the schedule from running while Hubitat\'s Mode is ' + settings[fieldName] + '.'
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
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
    if(settings['stop_brightness'] == 0) settings['stop_brightness'] = null
    if(settings['stop_temp'] == 0) settings['stop_temp'] = null
    if(settings['stop_hue'] == 0) settings['stop_hue'] = null
    if(settings['stop_sat'] == 0) settings['stop_sat'] = null
    
    if(settings['start_brightness']) settings['start_brightness'] = parent.convertToInteger(settings['start_brightness'])
    if(settings['stop_brightness']) settings['stop_brightness'] = parent.convertToInteger(settings['stop_brightness'])
    if(settings['start_temp']) settings['start_temp'] = parent.convertToInteger(settings['start_temp'])
    if(settings['stop_temp']) settings['stop_temp'] = parent.convertToInteger(settings['stop_temp'])
    if(settings['start_hue']) settings['start_hue'] = parent.convertToInteger(settings['start_hue'])
    if(settings['stop_hue']) settings['stop_hue'] = parent.convertToInteger(settings['stop_hue'])
    if(settings['start_sat']) settings['start_sat'] = parent.convertToInteger(settings['start_sat'])
    if(settings['stop_sat']) settings['stop_sat'] = parent.convertToInteger(settings['stop_sat'])

    putLog(896,'trace',app.label + ' initializing.')
    atomicState.logLevel = getLogLevel()
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    unschedule()
    atomicState.disable = false
    if(settings['disable']) {
        //parent.updateTableFromSettings(settings['device'], app.id) // Why? DOes it not update when the schedule starts?
        state.disable = true
    }
    if(atomicState.disable) {
        unschedule()
        parent.removeScheduleFromTable(app.id)
        return
    }

    subscribeDevices()

    parent.checkAnyOnMulti(settings['device'],app.label) //Initialize device into Table; we don't need the value of if "any on"

    setStartSchedule()
    putLog(916,'info',app.label + ' initialized.')
    return true
}

def handleStateChange(event){
    return
    parent.updateTableCapturedState(event.device,event.value,app.label)
}

// this does level, temp, hue, and sat
def handleBrightnessChange(event){
    return
    parent.updateTableCapturedLevel(event.device,'brightness',event.value,app.label)
}

// This needs rewrite
def handleTempChange(event){
    return
    parent.updateTableCapturedLevel(event.device,'temp',event.value,app.label)
    return
    //parent.updateTableCapturedBrightness(event.device,'temp',event.value,app.label)
    
    tempChange = parent.getLastTempChange(event.device, app.label)
    if(!tempChange) return
    if(tempChange.'appId' != app.id) return

    value = parent.convertToInteger(event.value)
    
    // Temp can be different by + .5% (25 at 5000); always plus, never minus
    if(event.device.currentColorMode == 'CT' && Math.round(tempChange.'currentLevel' / 255) == Math.round(value / 255)) return
    if(event.device.currentColorMode == 'CT' && Math.round(tempChange.'priorLevel' / 255) == Math.round(value / 255) && tempChange.'timeDifference' < 5000) return

    defaults = ['temp':['startLevel':value,'priorLevel':tempChange.'currentLevel','appId':'manual']]

    putLog(950,'warn','Captured manual temperature change for ' + event.device + ' to temperature color ' + value + 'K - last changed ' + tempChange.'timeDifference' + 'ms (to ' + tempChange.'currentLevel' + ')')
    parent.updateLevelsSingle(event.device,defaults,app.label)

    return
}

// This needs rewrite
def handleHueChange(event){
    return
    parent.updateTableCapturedLevel(event.device,'hue',event.value,app.label)
    return  //REMOVE THIS
    //parent.updateTableCapturedBrightness(event.device,'hue',event.value,app.label)
    hueChange = parent.getLastHueChange(event.device, app.label)
    if(!hueChange) return
    if(hueChange.'appId' != app.id) return

    value = parent.convertToInteger(event.value)
    
    if(hueChange.'currentLevel' == value && event.device.currentColorMode == 'RGB') return
    if(hueChange.'priorLevel' == value && hueChange.'timeDifference' < 5000 && event.device.currentColorMode == 'RGB') return

    defaults = ['hue':['startLevel':value,'priorLevel':hueChange.'currentLevel','appId':'manual']]

    putLog(973,'warn','Captured manual change for ' + event.device + ' to hue ' + value + '% - last changed ' + hueChange.'timeDifference' + 'ms (to ' + hueChange.'currentLevel' + ')')
    parent.updateLevelsSingle(event.device,defaults,app.label)

    return
}

def handleSatChange(event){
    return
    parent.updateTableCapturedLevel(event.device,'sat',event.value,app.label)
    return  //REMOVE THIS
    //parent.updateTableCapturedBrightness(event.device,'sat',event.value,app.label)
    satChange = parent.getLastSatChange(event.device, app.label)
    if(!satChange) return
    if(satChange.'appId' != app.id) return

    value = parent.convertToInteger(event.value)

    if(satChange.'currentLevel' == value && event.device.currentColorMode == 'RGB') return
    if(satChange.'priorLevel' == value && satChange.'timeDifference' < 5000 && event.device.currentColorMode == 'RGB') return

    defaults = ['sat':['startLevel':value,'priorLevel':event.device.currentSat,'appId':'manual']]
    putLog(994,'warn','Captured manual change for ' + event.device + ' to saturation ' + value + '% - last changed ' + satChange.'timeDifference' + 'ms (to ' + satChange.'currentLevel' + ')')
    parent.updateLevelsSingle(event.device,defaults,app.label)

    return
}

// Creates the schedule for start and stop
def setStartSchedule(){
    setTime()
    if(!atomicState.start) return
    unschedule()
    scheduleStart = atomicState.start
    if(scheduleStart < now()) scheduleStart += parent.CONSTDayInMilli()
    timeMillis = scheduleStart - now()
    parent.scheduleChildEvent(timeMillis,'','runDailyStartSchedule','',app.id)
    putLog(1009,'info','Set start schedule for ' + new Date(scheduleStart).format('HH:mm'))

    return true
}

def setStopSchedule(){
    if(!atomicState.stop) return
    timeMillis = atomicState.stop - now()
    if(timeMillis < 0) timeMillis = atomicState.stop + parent.CONSTDayInMilli() - now() // If timeMillis is in the past, just exit?
    parent.scheduleChildEvent(timeMillis,'','runDailyStopSchedule','',app.id)
    putLog(1019,'info','Set stop schedule for ' + new Date(atomicState.stop).format('HH:mm'))

}

// Performs actual changes at time set with start_action
// Called only by schedule set in incrementalSchedule
def runDailyStartSchedule(){
    if(atomicState.disabled) return
    if(atomicState.start && atomicState.stop){
        if(!parent.checkNowBetweenTimes(atomicState.start,atomicState.stop)){
            parent.scheduleChildEvent(atomicState.start + parent.CONSTDayInMilli(),'','runDailyStartSchedule','',app.id)
            return
        }
    }
    
    if(!parent.checkNowInDayList(settings['days'],app.Label)) {
        parent.scheduleChildEvent(atomicState.start + parent.CONSTDayInMilli(),'','runDailyStartSchedule','',app.id)
        return
    }
    if(!parent.checkNowInMonthList(settings['months'],app.Label)) {
        parent.scheduleChildEvent(atomicState.start + parent.CONSTDayInMilli(),'','runDailyStartSchedule','',app.id)
        return
    }

    atomicState.startDisabled = false
    scheduleBrightnessMap = getIncrementalScheduleMap('brightness')
    if(!getDisabled()) baseBrightnessMap = parent.getLevelMap('brightness',settings['start_brightness'],app.id,app.label)
    if(baseBrightnessMap) finalBrightnessMap = parent.mergeTwoMaps(baseBrightnessMap,scheduleBrightnessMap)
    if(!baseBrightnessMap && scheduleBrightnessMap) finalBrightnessMap = scheduleBrightnessMap
    
    scheduleTempMap = getIncrementalScheduleMap('temp')
    if(!getDisabled()) baseTempMap = parent.getLevelMap('temp',settings['start_temp'],app.id,app.label)
    if(baseTempMap) finalTempMap = parent.mergeTwoMaps(baseTempMap,scheduleTempMap)
    if(!baseTempMap && scheduleTempMap) finalTempMap = scheduleTempMap
    scheduleHueMap = getIncrementalScheduleMap('hue')
    if(!getDisabled()) baseHueMap = parent.getLevelMap('hue',settings['start_hue'],app.id,app.label) // hiRezHue needed here?
    if(baseHueMap) finalHueMap = parent.mergeTwoMaps(baseHueMap,scheduleHueMap)
    if(!baseHueMap && scheduleHueMap) finalHueMap = scheduleHueMap
    scheduleSatMap = getIncrementalScheduleMap('sat')
    if(!getDisabled()) baseSatMap = parent.getLevelMap('sat',settings['start_sat'],app.id,app.label)
    if(baseSatMap) finalSatMap = parent.mergeTwoMaps(baseSatMap,scheduleSatMap)
    if(!baseHueMap && scheduleHueMap) finalHueMap = scheduleHueMap

    if(scheduleBrightnessMap) runIncremental = true
    if(scheduleTempMap) runIncremental = true
    if(scheduleHueMap) runIncremental = true
    if(scheduleSatMap) runIncremental = true

    settings['device'].each{singleDevice->
        stateMap = parent.getStateMapSingle(singleDevice,settings['start_action'],app.id,app.label)          // Needs singleDevice for toggle
        
        parent.mergeMapToTableWithPreserve(singleDevice,stateMap,app.label)
        parent.mergeMapToTableWithPreserve(singleDevice,finalBrightnessMap,app.label)
        parent.mergeMapToTableWithPreserve(singleDevice,finalTempMap,app.label)
        parent.mergeMapToTableWithPreserve(singleDevice,finalHueMap,app.label)
        parent.mergeMapToTableWithPreserve(singleDevice,finalSatMap,app.label)
    }
    if(!getDisabled()) parent.setDeviceMulti(settings['device'],app.label)
    
    atomicState.start +=  parent.CONSTDayInMilli()
    newTime = atomicState.start - now()
    parent.scheduleChildEvent(newTime,'','runDailyStartSchedule','',app.id)
    setStopSchedule()
    //if(!runIncremental) parent.setDeviceMulti(settings['device'],app.label)
    if(runIncremental) parent.scheduleChildEvent(parent.CONSTScheduleMinimumActiveFrequencyMilli(),'','runIncrementalSchedule','',app.id)
    //if(!atomicState.stop) setDailySchedules()  // Reschedule next day; runDailyStopSchedule also reschedules, so on if start
}

// Performs actual changes at time set with start_action
// Called only by schedule set in incrementalSchedule
def runDailyStopSchedule(){
    if(!atomicState.stop) return
    if(atomicState.disabled) return
    unschedule(runIncrementalSchedule)

    brightnessMap = parent.getLevelMap('brightness',settings['stop_brightness'],app.id,app.label) // Should check if any level changes?
    tempMap = parent.getLevelMap('temp',settings['stop_temp'],app.id,app.label)
    hueMap = parent.getLevelMap('hue',settings['stop_hue'],app.id,app.label) // Do hiRezHue
    satMap = parent.getLevelMap('sat',settings['stop_sat'],app.id,app.label)
        
    settings['device'].each{singleDevice->
        stateMap = parent.getStateMapSingle(singleDevice,settings['stop_action'],app.id,app.label)          // Needs singleDevice for toggle
//strictly speaking, doesn't need WithoutPreserve, but that should be a bit more efficient?
//If no other apps need it, maybe deprecate it
        parent.mergeMapToTableWithoutPreserve(singleDevice,stateMap,app.label)
        parent.mergeMapToTableWithoutPreserve(singleDevice,brightnessMap,app.label)
        parent.mergeMapToTableWithoutPreserve(singleDevice,tempMap,app.label)
        parent.mergeMapToTableWithoutPreserve(singleDevice,hueMap,app.label)
        parent.mergeMapToTableWithoutPreserve(singleDevice,satMap,app.label)
    }

    parent.setDeviceMulti(settings['device'],app.label)
    
//make these all one function, unless we need individuals in other apps (maybe just 'state'?)
// removing keys will muck up the "manual change" capture process
// maybe change it so the key only has currentLevel, time and appId?
    settings['device'].each{singleDevice->
        parent.clearTableKey(singleDevice,'state',app.id,app.label)
        parent.clearTableKey(singleDevice,'brightness',app.id,app.label)
        parent.clearTableKey(singleDevice,'temp',app.id,app.label)
        parent.clearTableKey(singleDevice,'hue',app.id,app.label)
        parent.clearTableKey(singleDevice,'sat',app.id,app.label)
    }

    atomicState.start += parent.CONSTDayInMilli()
    newTime = atomicState.start - now()
    parent.scheduleChildEvent(newTime,'','runDailyStopSchedule','',app.id)
}

// Is unscheduled from runDailyStopSchedule
def runIncrementalSchedule(){
    if(!atomicState.start) return
    if(!atomicState.stop) return

    if(atomicState.stop < now()) return       // Shouldn't be neccesary; uncommented for testing
    if(atomicState.start < atomicState.stop){
        scheduleFrequency = Math.round(Math.abs(atomicState.stop - atomicState.start) / parent.CONSTScheduleMaximumIncrements())
    }
    if(atomicState.start > atomicState.stop){
        scheduleFrequency = Math.round(Math.abs((atomicState.stop + parent.CONSTDayInMilli()) - atomicState.start) / parent.CONSTScheduleMaximumIncrements())
    }
    timeMillis = scheduleFrequency
    if(scheduleFrequency < parent.CONSTScheduleMinimumInactiveFrequencyMilli()) timeMillis = parent.CONSTScheduleMinimumInactiveFrequencyMilli()

    if(getDisabled()){
        parent.scheduleChildEvent(timeMillis,'','runIncrementalSchedule','',app.id)
        return
    }
    activateScheduleFromManualOverride = false       // True is to remain active

    settings['device'].each{singleDevice->
        deviceChangedAny = false
        //This is where we should be checking manual overrides
        newLevel = parent.getIncrementalLevelSingle(singleDevice,'brightness',app.label)
        brightnessMap = parent.getLevelMap('brightness',newLevel,app.id,app.label)
        parent.mergeMapToTableWithPreserve(singleDevice,brightnessMap)
        if(brightnessMap) deviceChangedAny = true
        
        newLevel = parent.getIncrementalLevelSingle(singleDevice,'temp',app.label)
        tempMap = parent.getLevelMap('temp',newLevel,app.id,app.label)
        parent.mergeMapToTableWithPreserve(singleDevice,tempMap)
        if(tempMap) deviceChangedAny = true
        
        newLevel = parent.getIncrementalLevelSingle(singleDevice,'hue',app.label)
        hueMap = parent.getLevelMap('hue',newLevel,app.id,app.label)
        parent.mergeMapToTableWithPreserve(singleDevice,hueMap)
        if(hueMap) deviceChangedAny = true
        
        newLevel = parent.getIncrementalLevelSingle(singleDevice,'sat',app.label)
        satMap = parent.getLevelMap('sat',newLevel,app.id,app.label)
        parent.mergeMapToTableWithPreserve(singleDevice,satMap)
        if(satMap) deviceChangedAny = true

        if(deviceChangedAny) activateScheduleFromManualOverride = true
    }
    
    if(deviceChangedAny)  parent.setDeviceMulti(settings['device'],app.label)

    if(activateScheduleFromManualOverride) {
        if(scheduleFrequency < parent.CONSTScheduleMinimumActiveFrequencyMilli()) timeMillis = parent.CONSTScheduleMinimumActiveFrequencyMilli()
    }
    parent.scheduleChildEvent(timeMillis,'','runIncrementalSchedule','',app.id)
}

def subscribeDevices(){
    unsubscribe()
    //subscribe(settings['device'], 'switch', handleStateChange)
    subscribe(settings['device'], 'hue', handleHueChange)
    subscribe(settings['device'], 'saturation', handleSatChange)
    subscribe(settings['device'], 'colorTemperature', handleTempChange)
    subscribe(settings['device'], 'brightness', handleBrightnessChange)
    subscribe(settings['device'], 'speed', handleBrightnessChange)
    subscribe(location, 'systemStart', handleSystemBoot)
    subscribe(location,'timeZone',handleTimezone)
    return
}


// type expects 'brightness', 'temp', 'hue', 'sat'
def getIncrementalScheduleMap(type){
    if(!atomicState.start) return
    if(!atomicState.stop) return
    if(!settings['start_' + type]) return
    if(!settings['stop_' + type]) return

    scheduleMap = [:]
    scheduleMap."${type}" = [:]
    scheduleMap."${type}".'startLevel' = settings['start_' + type]
    scheduleMap."${type}".'stopLevel' = settings['stop_' + type]
    scheduleMap."${type}".'appId' = app.id
    scheduleMap."${type}".'appType' = 'time'
    scheduleMap."${type}".'currentLevel' = settings['start_' + type]
    scheduleMap."${type}".'startTime' = atomicState.start
    scheduleMap."${type}".'stopTime' = atomicState.stop
    if(hueDirection) scheduleMap."${type}".'hueDirection' = hueDirection
    return scheduleMap
}

// Only thing really neede is:
// 1) If schedule is not running, remove itself from Table
// 2) Run Daily Start Schedule and let it deal with restarting a schedule
def systemBootActivate(){
    if(atomicState.disable) return
    
    initialize()
}

def setTime(){      // Should NOT be run from Incremental
    if(!setStartTime()) return
    setStopTime()
    return true
}

def setStartTime(){
    //if(app.id == 2227) {
    //    atomicState.start = now() + 5000
    //    atomicState.stop = now() + 20000
    //    return
    //}
// REMOVE THESE

    if(!settings['start_timeType']) return
    // Gets start time caught up to today 
    // But if start and stop time have passed today, setStopTime will fix it
    setTime = parent.setTimeAsIn24Hours(setStartStopTime('start'))  
    atomicState.start  = setTime
    putLog(1245,'info','Start time set to ' + parent.getPrintDateTimeFormat(setTime))
    return true
}

def setStopTime(){
    if(!settings['stop_timeType'] || settings['stop_timeType'] == 'none') return
    setTime = setStartStopTime('stop')
    while (setTime < atomicState.start) {       // If stop time is behind start, increment by a day until it's after start (could be behind multiple days if disabled)
        setTime += parent.CONSTDayInMilli()
    }
    if(setTime < now()){        // Increment both start and stop if stop has passed today
        setTime += parent.CONSTDayInMilli()
        atomicState.start += parent.CONSTDayInMilli()
    }
    atomicState.stop  = setTime
    putLog(1260,'info','Stop time set to ' + parent.getPrintDateTimeFormat(setTime))
    return true
}

// Sets atomicState.start and atomicState.stop variables
// Requires type value of "start" or "stop" (must be capitalized to match setting variables)
def setStartStopTime(type){
    if(settings[type + '_timeType'] == 'time') return timeToday(settings[type + '_time']).getTime()
    if(settings[type + '_timeType'] == 'sunrise') return (settings[type + '_sunType'] == 'before' ? parent.getSunrise(settings[type + '_sunOffset'] * -1,app.label) : parent.getSunrise(settings[type + '_sunOffset'],app.label))
    if(settings[type + '_timeType'] == 'sunset') return (settings[type + '_sunType'] == 'before' ? parent.getSunset(settings[type + '_sunOffset'] * -1,app.label) : parent.getSunset(settings[type + '_sunOffset'],app.label))
}

// Returns true is schedule is not inactive, and allows for no stop time
// Used by getDefaultLevel
def getScheduleActive(){
    if(!parent.checkNowInDayList(settings['days'],app.label)) return
    if(!parent.checkNowInMonthList(settings['months'],app.label)) return
    if(!atomicState.start || !atomicState.stop) return
    if(!parent.checkNowBetweenTimes(atomicState.start, atomicState.stop, app.label)) return

    return true
}

def getDisabled(){
    // If disabled, return true
    if(atomicState.disable) return true

    // If mode isn't correct, return false
    if(settings['ifMode'] && location.mode != settings['ifMode']) return true
    
    if(!parent.checkPeopleHome(settings['personHome'],app.label)) return true
    if(!parent.checkNoPeopleHome(settings['personNotHome'],app.label)) return true

    return false
}

def getDevices(){
    return settings['device']
}

// Called from parent.scheduleChildEvent
def setScheduleFromParent(timeMillis,scheduleFunction,scheduleParameters = null){
    if(timeMillis < 1) {
        putLog(1303,'warning','Scheduled time ' + timeMillis + ' is not a positive number with ' + scheduleFunction)
        return
    }
    runInMillis(timeMillis,scheduleFunction,scheduleParameters)
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,type = 'trace',message = null){
    return parent.putLog(lineNumber,type,message,app.label,,getLogLevel())
}
