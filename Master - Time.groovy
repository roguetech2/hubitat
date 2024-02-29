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
*  Version: 0.7.2.14
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
        if(validateTimes('start') && validateTimes('stop') && getBaseStartStopTimes('start') == getBaseStartStopTimes('stop')) displayError('You can\'t have the same time to start and stop.')

        displayTypeOption('start')
        displayTimeOption('start')
        displaySunriseTypeOption('start')
        displayTypeOption('stop')
        displayTimeOption('stop')
        displaySunriseTypeOption('stop')
        displayDaysOption()
        displayMonthsOption()
        if(validateTimes('start') && validateTimes('stop') && getBaseStartStopTimes('start') > getBaseStartStopTimes('stop')) displayInfo('Stop time is before start time, which is perfectly fine, but stop time will be assumed to be the next day (i.e. the duration of this schedule will be approximately ' + Math.round((getBaseStartStopTimes('stop') + parent.CONSTDayInMilli() - getBaseStartStopTimes('start')) / parent.CONSTHourInMilli()) + ' hours).')
        if(validateTimes('start') && validateTimes('stop') && getBaseStartStopTimes('start') > getBaseStartStopTimes('stop') && settings['days']) displayInfo('Every schedule that starts will stop, even if the stop day falls on a day not permitted. For instance, if scheduled for only Mondays and the stop time falls on the next day, stop actions/levels will still be set.')
        if(validateTimes('start') && validateTimes('stop') && getBaseStartStopTimes('start') > getBaseStartStopTimes('stop') && settings['months']) displayInfo('Every schedule that starts will stop, even if the stop day is in a month not permitted. For instance, if scheduled starts on the last day of an allowed month and the stop time falls in the next month, stop actions/levels will still be set.')
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
    if(settings['stop_timeType'] == 'none') return sectionTitle + '<b>No end</b>'
    if(validateTimes('stop') && settings['stop_timeType'] != 'none') sectionTitle += '<b>Stopping: '
    if(settings['stop_timeType'] == 'time' && settings['stop_time']) sectionTitle += 'At ' + Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings['stop_time']).format('h:mm a', location.timeZone)
    if(settings['stop_timeType'] == 'time' && !settings['stop_time']) sectionTitle += 'At specific time '
    if(settings['stop_timeType'] == 'sunrise' || settings['stop_timeType'] == 'sunset'){
        if(!settings['stop_sunType']) sectionTitle += 'Based on ' + settings['stop_timeType']
        if(settings['stop_sunType'] == 'at') sectionTitle += 'At ' + settings['stop_timeType']
        if(settings['stop_sunOffset']) sectionTitle += settings['stop_sunOffset'] + ' minutes '
        if(settings['stop_sunType'] && settings['stop_sunType'] != 'at') sectionTitle += settings['stop_sunType'] + ' ' + settings['stop_timeType']
        if(validateTimes('stop') && settings['stop_timeType'] != 'none') sectionTitle += ' ' + getSunriseTime(settings['stop_timeType'],settings['stop_sunOffset'],settings['stop_sunType'])
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
    fieldList = ['time':'Start at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
    if(type == 'stop') fiedlList = ['none':'Don\'t stop','time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
    input fieldName, 'enum', title: fieldTitle, multiple: false, width: getTypeOptionWidth(type), options: fieldList, submitOnChange:true
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
        typeString = 'color temperature'
        typeUnit = 'K'
        typeRange = '1800 to 5400' + typeUnit
    }
    if(levelType == 'brightness'){
        if(!parent.validateLevel(settings['start_' + levelType])) displayError('Start ' + typeString + ' must be from ' + typeRange + '. Correct start ' + typeString + '.')
        if(!parent.validateLevel(settings['stop_' + levelType])) displayError('Stop ' + typeString + ' must be from ' + typeRange + '. Correct stop ' + typeString + '.')
        //Both entered
        message = typeString.capitalize() + ' is percentage from ' + typeRange + '. It will transition from ' + settings['start_' + levelType] + typeUnit + ' to ' + settings['stop_' + levelType] + typeUnit + ' ' + typeString + ' over the duration of the schedule.'
        //Neither entered
        if(!settings['start_' + levelType] && (!settings['stop_' + levelType] && settings['stop_timeType'] != 'none')) message = 'Enter the percentage of ' + typeString + ' from ' + typeRange + ' where 0' + typeUnit + ' is off, and 100' + typeUnit + ' is maximum.'
        //One entered
        if(!settings['start_' + levelType] && settings['stop_' + levelType]) typeString.capitalize() + ' is percentage from ' + typeRange + '. If entering both starting and stopping ' + typeString + ', it will transition from start level to ' + settings['stop_' + levelType] + typeUnit + ' over the duration of the schedule.'
        if(settings['start_' + levelType] && (!settings['stop_' + levelType] && settings['stop_timeType'] != 'none')) message = typeString.capitalize() + ' is percentage from ' + typeRange + '. If entering both starting and stopping ' + typeString + ', it will transition from ' + settings['start_' + levelType] + typeUnit + ' to stop level over the duration of the schedule.'
    }
    if(levelType == 'temp'){
        if(!parent.validateTemp(settings['start_' + levelType])) displayError('Start ' + typeString + ' must be from ' + typeRange + '. Correct start ' + typeString + '.')
        if(!parent.validateTemp(settings['stop_' + levelType])) displayError('Stop ' + typeString + ' must be from ' + typeRange + '. Correct stop ' + typeString + '.')
        //Both entered
        message = typeString.capitalize() + ' is from ' + typeRange + ' where lower is more yellow and higher is more blue. It will transition from ' + settings['start_' + levelType] + typeUnit + ' to ' + settings['start_' + levelType] + typeUnit + ' ' + typeString + ' over the duration of the schedule.'
        //Neither entered
        if(!settings['start_' + levelType] && (!settings['stop_' + levelType] && settings['stop_timeType'] != 'none')) message = typeString.capitalize() + ' is from ' + typeRange + ' where lower is more yellow and higher is more blue; 3000' + typeUnit + ' is warm white, 4000' + typeUnit + ' is cool white, and 5000' + typeUnit + ' is daylight.'
        //One entered
        if(!settings['start_' + levelType] && settings['stop_' + levelType]) message = typeString.capitalize() + ' is from ' + typeRange + ' where lower is more yellow and higher is more blue. If entering both starting and stopping ' + typeString + ', it will transition from start level to ' + settings['stop_' + levelType] + typeUnit + ' over the duration of the schedule.'
        if(settings['start_' + levelType] && (!settings['stop_' + levelType] && settings['stop_timeType'] != 'none')) message = typeString.capitalize() + ' is from ' + typeRange + ' where lower is more yellow and higher is more blue. If entering both starting and stopping ' + typeString + ', it will transition from ' + settings['start_' + levelType] + typeUnit + ' to stop level over the duration of the schedule.'
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
            if(validateTimes('stop') && settings['stop_timeType'] != 'none') message += ' If entering both starting and ending hue, it will transition from starting to ending hue for the duration of the schedule.'
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
            if(validateTimes('stop') && settings['stop_timeType'] != 'none') message += ' If entering both starting and ending saturation, it will transition from starting to ending saturation for the duration of the schedule.'
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
    if(validateTimes('stop') && settings['stop_timeType'] != 'none' && !settings['stop_action']) return

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
        
        if(validateTimes('stop') && settings['stop_timeType'] != 'none') message = 'Schedules will resume/pause based on people present/away. If requirements are met at start time, any stop actions/levels will be applied.'
        displayInfo(message)
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

        message = 'This will limit the schedule from running while Hubitat\'s Mode is as selected'
        if(settings[fieldName]) message = 'This will prevent the schedule from running unless Hubitat\'s Mode is ' + settings[fieldName]
        if(validateTimes('stop') && settings['stop_timeType'] != 'none') message += '. Schedules will resume/pause based on Mode. If the Mode matches at start time, any stop actions/levels will be applied'
        message += '.'
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
    putLog(890,'trace',app.label + ' initializing.')
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

    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))

    unschedule()
    setTime()
    clearScheduleFromTable()    // Clear schedule from table, to avoid stale settings

    if(settings['disable']) return

    subscribeDevices()
    setStartSchedule()

    if(parent.checkNowBetweenScheduledStartStopTimes(atomicState.startTime,atomicState.stopTime,app.label)) runDailyStartSchedule()

    putLog(917,'info',app.label + ' initialized.')
    return true
}

def handleStateChange(event){
    parent.updateTableCapturedState(event.device,event.value,app.label)
}

// this does level, temp, hue, and sat
def handleBrightnessChange(event){
    parent.updateTableCapturedLevel(event.device,'brightness',app.label)
}

// This needs rewrite
def handleTempChange(event){
    parent.updateTableCapturedLevel(event.device,'colorTemperature',app.label)
}

// This needs rewrite
def handleHueChange(event){
    parent.updateTableCapturedLevel(event.device,'hue',app.label)
}

def handleSatChange(event){
    parent.updateTableCapturedLevel(event.device,'saturation',app.label)
}

// Creates the schedule for start and stop
def setStartSchedule(){
    timeMillis = getBaseStartStopTimes('start') - now()
    if(timeMillis < 0) timeMillis += parent.CONSTDayInMilli()
    parent.scheduleChildEvent(timeMillis,'','runDailyStartSchedule','',app.id)
    
    return true
}

def setStopSchedule(){
    if(!atomicState.stopTime) return
    timeMillis = getBaseStartStopTimes('stop') - now()
    if(timeMillis < 0) timeMillis += parent.CONSTDayInMilli()
    parent.scheduleChildEvent(timeMillis,'','runDailyStopSchedule','',app.id)

    return true
}

// Performs actual changes at time set with start_action
// Called only by schedule set in incrementalSchedule
def runDailyStartSchedule(){
    putLog(965,'info',app.label + ' schedule has started.')
    if(settings['disabled']) return
    
    if(!parent.checkNowInDayList(settings['days'],app.Label)) {
        setStartSchedule()
        return
    }
    if(!parent.checkNowInMonthList(settings['months'],app.Label)) {
        setStartSchedule()
        return
    }

    setStopSchedule()
    clearScheduleFromTable() // clear out any "manual overrides"
    
    if(settings['start_brightness'] && settings['stop_brightness']) runIncremental = true
    if(settings['start_temp'] && settings['stop_temp']) runIncremental = true
    if(settings['start_hue'] && settings['stop_hue']) runIncremental = true
    if(settings['start_sat'] && settings['stop_sat']) runIncremental = true
    if(runIncremental) {
        setTime()
        if(atomicState.startTime < atomicState.stopTime) scheduleFrequency = Math.round(Math.abs(atomicState.stopTime - atomicState.startTime) / parent.CONSTScheduleMaximumIncrements())
        if(atomicState.startTime > atomicState.stopTime) scheduleFrequency = Math.round(Math.abs((atomicState.stopTime + parent.CONSTDayInMilli()) - atomicState.startTime) / parent.CONSTScheduleMaximumIncrements())
        if(scheduleFrequency < parent.CONSTScheduleMinimumInactiveFrequencyMilli()) scheduleFrequency = parent.CONSTScheduleMinimumActiveFrequencyMilli()
        atomicState.scheduleFrequency = scheduleFrequency
        parent.scheduleChildEvent(scheduleFrequency,'','runIncrementalSchedule','',app.id)
    }

    if(!getActive()) {
        atomicState.startDisabled = true
        return
    }
    atomicState.startDisabled = false

    brightnessMap = getLevelMap('brightness',settings['start_brightness'])
    tempMap = getLevelMap('temp',settings['start_temp'])
    hueMap = getLevelMap('hue',settings['start_hue'])
    satMap = getLevelMap('sat',settings['start_sat'])
    scheduleMap = parent.addMaps(brightnessMap, tempMap, hueMap, satMap)

    settings['device'].each{singleDevice->
        stateMap = parent.getStateMapSingle(singleDevice,settings['start_action'],app.id,app.label)          // Needs singleDevice for toggle
        fullMap = parent.addMaps(scheduleMap, stateMap)
        parent.mergeMapToTable(singleDevice.id,fullMap,app.label)
        putLog(1009,'debug','Performing start action(s) for ' + singleDevice + ' as ' + fullMap + '.')
    }
    parent.setDeviceMulti(settings['device'],app.label)
}

// Performs actual changes at time set with start_action
// Called only by schedule set in incrementalSchedule
def runDailyStopSchedule(){
    putLog(1017,'info',app.label + ' schedule has ended.')

    unschedule('runIncrementalSchedule')    //This doesn't seem to work
    setStartSchedule()
    atomicState.scheduleFrequency = null

    if(settings['disabled']) return
    if(atomicState.startDisabled) return
    
    clearScheduleFromTable()    // Remove start/incremental table entries - all that should be left after schedule ends is stop settings (with a stopTime)
    
    if(!settings['start_brightness']) brightnessMap = getLevelMap('brightness',settings['stop_brightness'])
    if(!settings['start_temp']) tempMap = getLevelMap('temp',settings['stop_temp'])
    if(!settings['start_hue']) hueMap = getLevelMap('hue',settings['stop_hue'])
    if(!settings['start_sat']) satMap = getLevelMap('sat',settings['stop_sat'])
    scheduleMap = parent.addMaps(brightnessMap, tempMap, hueMap, satMap)
    settings['device'].each{singleDevice->
        stateMap = parent.getStateMapSingle(singleDevice.id,settings['stop_action'],app.id,app.label)          // Needs singleDevice for toggle
        fullMap = parent.addMaps(scheduleMap, stateMap)
        parent.mergeMapToTable(singleDevice.id,fullMap,app.label)
        putLog(1037,'debug','Performing stop action(s) for ' + singleDevice + ' as ' + fullMap + '.')
    }
    parent.setDeviceMulti(settings['device'],app.label)
    atomicState.startTime = null        // Set to null to prevent runIncremental from running
    atomicState.stopTime = null
}

// Is unscheduled from runDailyStopSchedule
def runIncrementalSchedule(){
    if(settings['disabled']) return

    if(!atomicState.startTime) return
    if(!atomicState.stopTime) return
    if(!parent.checkNowBetweenScheduledStartStopTimes(atomicState.startTime,atomicState.stopTime,app.label)) return  // Unscheduled from runDailyStopSchedule

    if(!getActive()) {
        // Remove table entries, to be re-added if schedule becomes active again
        clearScheduleFromTable()
        parent.scheduleChildEvent(parent.CONSTScheduleMinimumInactiveFrequencyMilli(),'','runIncrementalSchedule','',app.id)
        return
    }
    
    timeMillis = atomicState.scheduleFrequency
    
    anyDevicesChanged = false       // True is to remain active
    settings['device'].each{singleDevice->
        brightnessMap = getIncrementalMaps(singleDevice,'brightness')
        tempMap = getIncrementalMaps(singleDevice,'temp')
        hueMap = getIncrementalMaps(singleDevice,'hue')
        satMap = getIncrementalMaps(singleDevice,'sat')
        incrementalMap = parent.addMaps(brightnessMap, tempMap, hueMap, satMap)
        if(incrementalMap) {
            putLog(1069,'debug','Incremental schedule for ' + singleDevice + ' settings are ' + incrementalMap)
            anyDevicesChanged = true
            parent.mergeMapToTable(singleDevice.id, levelMap)
        }
        if(!incrementalMap) putLog(1073,'debug','Incremental schedule for ' + singleDevice + ' has no changes.')
    }
    if(anyDevicesChanged) parent.setDeviceMulti(settings['device'], app.label)

    if(anyDevicesChanged) {
        if(timeMillis < parent.CONSTScheduleMinimumActiveFrequencyMilli()) timeMillis = parent.CONSTScheduleMinimumActiveFrequencyMilli()
    }
    parent.scheduleChildEvent(timeMillis, '', 'runIncrementalSchedule', '', app.id)
}

def getLevelMap(type,level){
    if(atomicState.stopTime) {
        stopTime = parent.getDatetimeFromTimeInMillis(atomicState.stopTime, app.label)
        if(stopTime < now()) stopTime += parent.CONSTDayInMilli()
    }
    return parent.getLevelMap(type,level,app.id,stopTime,app.label)
}

def getIncrementalMaps(singleDevice,type){
    if(parent.getAppIdForDeviceFromTable(singleDevice.id,type,app.label) == 'manual') return
    
    newLevel = getIncrementalLevelSingle(singleDevice, type)
    
    stopTime = parent.getDatetimeFromTimeInMillis(atomicState.stopTime, app.label)
    if(stopTime < now()) stopTime += parent.CONSTDayInMilli()
    return parent.getLevelMap(type, newLevel, app.id,stopTime, app.label)
}

def subscribeDevices(){
    unsubscribe()
    subscribe(settings['device'], 'switch', handleStateChange)
    subscribe(settings['device'], 'hue', handleHueChange)
    subscribe(settings['device'], 'saturation', handleSatChange)
    subscribe(settings['device'], 'colorTemperature', handleTempChange)
    subscribe(settings['device'], 'level', handleBrightnessChange)
    subscribe(settings['device'], 'speed', handleBrightnessChange)
    subscribe(location, 'systemStart', handleSystemBoot)
    subscribe(location,'timeZone',handleTimezone)
    return
}

def clearScheduleFromTable(){
    settings['device'].each{singleDevice->
        clearTableKey(singleDevice.id,'brightness')
        clearTableKey(singleDevice.id,'temp')
        clearTableKey(singleDevice.id,'hue')
        clearTableKey(singleDevice.id,'sat')
    }
}

def clearTableKey(singleDeviceId,type){
    if(!singleDeviceId) return
    if(!type) return
    levelAppId = parent.getAppIdForDeviceFromTable(singleDeviceId,type,app.label)
    if(levelAppId){
        if(levelAppId == app.id) clearKey = true
        levelTime = parent.getTimeForDeviceFromTable(singleDeviceId,type,app.label)
        // Clear any "manual overrides" set prior to schedule
        // Perhaps should only be done with settings used by this schedule
        // However, runIncremental does not check times (to maximize run speed)
        if(levelAppId == 'manual') {
            if(levelTime < parent.getDatetimeFromTimeInMillis(atomicState.startTime,app.label)) clearKey = true
        }
        if(levelAppId != app.id){
            if(levelTime + parent.CONSTDayInMilli() < now()) clearKey = true    // If not this schedule but not today, prune it
        }
    }
    if(clearKey) parent.clearTableKey(singleDeviceId,type,app.label)
}

// Only thing really neede is:
// 1) If schedule is not running, remove itself from Table
// 2) Run Daily Start Schedule and let it deal with restarting a schedule
def systemBootActivate(){
    if(settings['disabled']) return
    
    initialize()
}

def setTime(){      // Should NOT be run from Incremental
    atomicState.startTime = parent.getTimeOfDayInMillis(getBaseStartStopTimes('start'),app.label)
    atomicState.stopTime = parent.getTimeOfDayInMillis(getBaseStartStopTimes('stop'),app.label)
    if(atomicState.startTime == atomicState.stopTime) atomicState.stopTime -= atomicState.stopTime
}

// Sets atomicState.start and atomicState.stop variables
// Requires type value of "start" or "stop" (must be capitalized to match setting variables)
def getBaseStartStopTimes(type){
    if(type == 'stop' && settings['stop_timeType'] == 'none') return
    if(settings[type + '_timeType'] == 'time') {
        if(!settings[type + '_time']) return
        return timeToday(settings[type + '_time']).getTime()
    }
    if(!settings[type + '_sunType']) return
    if(settings[type + '_timeType'] == 'sunrise') return (settings[type + '_sunType'] == 'before' ? parent.getSunrise(settings[type + '_sunOffset'] * -1,app.label) : parent.getSunrise(settings[type + '_sunOffset'],app.label))
    if(settings[type + '_timeType'] == 'sunset') return (settings[type + '_sunType'] == 'before' ? parent.getSunset(settings[type + '_sunOffset'] * -1,app.label) : parent.getSunset(settings[type + '_sunOffset'],app.label))
}

// type expects 'brightness', 'temp', 'hue', 'sat'
// With Hue, checks reverse
def getIncrementalLevelSingle(singleDevice,type){
    if(!singleDevice) return
    if(!type) return
    if(!atomicState.startTime) return
    if(!atomicState.stopTime) return
    if(!settings['start_' + type]) return
    if(!settings['stop_' + type]) return

    // need to check if time was before schedule started?
    //if(parent.getAppIdForDeviceFromTable(singleDevice,type,app.label) != app.id) return

    totalMillis = atomicState.stopTime - atomicState.startTime
    if(totalMillis < 0) totalMillis = atomicState.startTime + atomicState.stopTime  // Adjust for going past midnight
    elapsedMillis = parent.getTimeOfDayInMillis(now(),app.label) - atomicState.startTime
    percentComplete = elapsedMillis / totalMillis
    forward = false
    if(settings['start_' + type] < settings['stop_' + type]) forward = true
    if(type == 'hue' && settings['hueDirection'] == 'reverse') forward = !forward

    totalRange = Math.abs(settings['start_' + type] - settings['stop_' + type])
    if(type == 'hue'){
        if(!forward) totalRange = 360 - settings['stop_' + type] + settings['start_' + type]
    }
    if(forward) resultLevel = Math.round(totalRange * percentComplete + settings['start_' + type])
    if(!forward) resultLevel = Math.round(settings['start_' + type] - totalRange * percentComplete)
    if(type == 'hue'){
        if(resultLevel < 0) resultLevel = 360 + resultLevel
        if(resultLevel > 360) resultLevel = resultLevel - 360
    }
    
    return resultLevel
}

def getActive(){
    if(settings['disabled']) return
    if(settings['ifMode'] && location.mode != settings['ifMode']) return
    
    if(!parent.checkPeopleHome(settings['personHome'],app.label)) return
    if(!parent.checkNoPeopleHome(settings['personNotHome'],app.label)) return
    return true
}

// Called from parent.scheduleChildEvent
def setScheduleFromParent(timeMillis,scheduleFunction,scheduleParameters = null){
    runInMillis(timeMillis,scheduleFunction,scheduleParameters)
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,type = 'trace',message = null){
    return parent.putLog(lineNumber,type,message,app.label,,getLogLevel())
}
