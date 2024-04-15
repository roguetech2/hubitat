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
*  Version: 0.7.2.22
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
filterYesIcon = '<img src="http://emily-john.love/icons/filterEnable.png" width=20 height=20>'
filterNoIcon = '<img src="http://emily-john.love/icons/filterDisable.png" width=20 height=20>'
filterLightIcon = '<img src="http://emily-john.love/icons/light.png" width=20 height=20>'
filterColorIcon = '<img src="http://emily-john.love/icons/color.png" width=20 height=20>'
filterSwitchIcon = '<img src="http://emily-john.love/icons/switch.png" width=20 height=20>'
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
    return 4
}

def displayLabel(text, width = 12){
    if(!text) return
    paragraph('<div style="background-color:#DCDCDC"><b>' + text + '</b></div>',width:width)
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
    if(!noDisplayIcon) paragraph('<div style="background-color:LemonChiffon">' + warningIcon  + ' ' + text + '</div>',width:width)
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
    page(name: 'setup', install: install, uninstall: true) {
        if(!app.label){
            section(){
                displayNameOption()
            }
        } else {
            if(!settings) settings = [:]

            processDates()

            deviceCount = getDeviceCount(device)
            peopleError = compareDeviceLists(personHome,personNotHome)
            plainStartAction = getPlainAction(settings['start_action'])
            plainStopAction = getPlainAction(settings['stop_action'])
            pluralDevice = getDevicePlural()

            peopleError = compareDeviceLists(personHome,personNotHome)
            
            section(){
                displayNameOption()
                displayDevicesTypes()
                displayControlDeviceOption()
                displayDisableOption()
            }
            
            displayScheduleSection()
            displayDaysAndDatesSection()
            section(){}
            displayActionOption()
            displayLevelsOption('brightness')
            displayLevelsOption('temp')
            displayColorOption()
            displayChangeModeOption()
            section(){}
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

def displayNameOption(){
    displayNameOptionComplete()
    displayNameOptionIncomplete()
}
def displayNameOptionComplete(){
    if(!app.label) return
    displayLabel('Schedule name:',2)
    label title: '', required: false, width: 10,submitOnChange:true
}
def displayNameOptionIncomplete(){
    if(app.label) return
    fieldTitle = 'Set name for this schedule:'
    displayLabel(highlightText(fieldTitle))
    label title: '', width:12, submitOnChange:true
    displayInfo('Name this schedule. Each schedule must have a unique name.')
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
        if(settings['deviceType'] == 'fan') return 'fan(s)'
        if(settings['deviceType'] == 'switch') {
            if(settings['controlButtonValue'] == 1) return 'switch(es)'
            if(settings['controlButtonValue'] == 2) return 'light(s)'
            if(settings['controlButtonValue'] == 2) return 'color light(s)'
        }
    }
    
    if(deviceCount > 1) {
        if(settings['deviceType'] == 'lock') return 'locks'
        if(settings['deviceType'] == 'fan') return 'fans'
        if(settings['deviceType'] == 'switch') {
            if(settings['controlButtonValue'] == 1) return 'switches'
            if(settings['controlButtonValue'] == 2) return 'lights'
            if(settings['controlButtonValue'] == 2) return 'color lights'
        }
    }

    if(settings['deviceType'] == 'lock') return 'lock'
    if(settings['deviceType'] == 'fan') return 'fan'
    if(settings['deviceType'] == 'switch') {
        if(settings['controlButtonValue'] == 1) return 'switch'
        if(settings['controlButtonValue'] == 2) return 'light'
        if(settings['controlButtonValue'] == 2) return 'color light'
    }
}
/*
def displayDevicesOption(){
    if(!settings['deviceType']) return

    capability = 'capability.switch'
    if(settings['deviceType'] == 'light') capability = 'capability.switchLevel'
    if(settings['deviceType'] == 'fan') capability = 'capability.fanControl'
    if(settings['deviceType'] == 'lock') capability = 'capability.lock'

    fieldName = 'device'
    fieldTitle = pluralDevice.capitalize() + ' to schedule (click to select)?'
    if(settings[fieldName]) fieldTitle = pluralDevice.capitalize() + ' being scheduled:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    fieldCapability = 'capability.switch'
    if(settings['deviceType'] == 'light') fieldCapability = 'capability.switchLevel'
    if(settings['deviceType'] == 'fan') fieldCapability = 'capability.fanControl'
    if(settings['deviceType'] == 'lock') fieldCapability = 'capability.lock'
    input fieldName, fieldCapability, title: fieldTitle, multiple: true, submitOnChange:true
}
*/

def displayControlDeviceOption(){
    if(!settings['deviceType']) return
    fieldName = 'device'
    capabilitiesType = 'switch'
    if(settings['deviceType'] == 'fan') fieldCapability = 'fanControl'
    if(settings['deviceType'] == 'lock') fieldCapability = 'lock'
    if(settings['controlButtonValue'] == 1) capabilitiesType = 'switchLevel'
    if(settings['controlButtonValue'] == 2) capabilitiesType = 'colorMode'
    displayControlDeviceOptionComplete(fieldName,capabilitiesType)
    displayControlDeviceOptionIncomplete(fieldName,capabilitiesType)
}
def displayControlDeviceOptionComplete(fieldName,capabilitiesType){
    if(!settings[fieldName]) return
    fieldTitle = 'Device to control:'
    if(settings[fieldName].size() > 1) fieldTitle = 'Devices to control:'

    if(settings['deviceType'] != 'switch') displayDeviceSelectField(fieldName,fieldTitle,'capability.' + capabilitiesType,true)
    if(settings['deviceType'] == 'switch') displayDeviceSelectField(fieldName,fieldTitle,'capability.' + capabilitiesType,true, 'controlButton')
}
def displayControlDeviceOptionIncomplete(fieldName,capabilitiesType){
    if(settings[fieldName]) return
    fieldTitle = 'Select all device(s) to control with the MagicCube:'
    if(settings['device'].size() > 1) fieldTitle = 'Select all device(s) to control with the MagicCubes:'
    if(settings['deviceType'] != 'switch') displayDeviceSelectField(fieldName,fieldTitle,'capability.' + capabilitiesType,true)
    if(settings['deviceType'] == 'switch') displayDeviceSelectField(fieldName,fieldTitle,'capability.' + capabilitiesType,true, 'controlButton')
}

def displayDisableOption(){
    if(!install) return
    fieldName = 'disable'
    fieldTitle = 'This schedule is disabled. Reenable it?'
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
    if((settings[type + '_timeType'] == 'sunrise' || settings[type + '_timeType'] == 'sunset') && (settings[type + '_sunType'] == 'before' || settings[type + '_sunType'] == 'after') && !settings[type + '_sunOffset']) return false
    if(!validateSunriseMinutes(type)) return false
    return true
}

def validateSunriseMinutes(type){
    if(!settings[type + '_sunOffset']) return true
    if(settings[type + '_sunOffset'] > 719) return false
    return true
}

def checkIfStopTimeEnetered(){
    if(settings['stop_timeType'] == 'none') return false
    if(!validateTimes('stop')) return false
    if(!settings['stop_timeType']) return false
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
        if(validateTimes('start') && checkIfStopTimeEnetered() && getBaseStartStopTimes('start') > getBaseStartStopTimes('stop')) displayInfo('Stop time is before start time, which is perfectly fine, but stop time will be assumed to be the next day (i.e. the duration of this schedule will be approximately ' + Math.round((getBaseStartStopTimes('stop') + parent.CONSTDayInMilli() - getBaseStartStopTimes('start')) / parent.CONSTHourInMilli()) + ' hours).')
        if(validateTimes('start') && checkIfStopTimeEnetered() && getBaseStartStopTimes('start') > getBaseStartStopTimes('stop') && settings['days']) displayInfo('Every schedule that starts will stop, even if the stop day falls on a day not permitted. For instance, if scheduled for only Mondays and the stop time falls on the next day, stop actions/levels will still be set.')
        if(validateTimes('start') && checkIfStopTimeEnetered() && getBaseStartStopTimes('start') > getBaseStartStopTimes('stop')) displayInfo('Every schedule that starts will stop, even if the stop day is in a month not permitted. For instance, if scheduled starts on the last day of an allowed month and the stop time falls in the next month, stop actions/levels will still be set.')
    }
}

def getTimeSectionTitle(){
    if(!settings['start_timeType'] && !settings['stop_timeType'] && !settings['days']) return 'Click to set times (optional)'

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

    if(settings['start_timeType'] && settings['days']) sectionTitle += ' on: ' + displayTextField
    if(settings['start_timeType']) sectionTitle += '</b>'
    if(!settings['days']) sectionTitle += moreOptions
    
    if(!settings['start_timeType'] && !settings['stop_timeType']) return sectionTitle

    sectionTitle += '</br>'
    if(!checkIfStopTimeEnetered()) return sectionTitle + '<b>No end</b>'
    if(checkIfStopTimeEnetered()) sectionTitle += '<b>Stopping: '
    if(settings['stop_timeType'] == 'time' && settings['stop_time']) sectionTitle += 'At ' + Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings['stop_time']).format('h:mm a', location.timeZone)
    if(settings['stop_timeType'] == 'time' && !settings['stop_time']) sectionTitle += 'At specific time '
    if(settings['stop_timeType'] == 'sunrise' || settings['stop_timeType'] == 'sunset'){
        if(!settings['stop_sunType']) sectionTitle += 'Based on ' + settings['stop_timeType']
        if(settings['stop_sunType'] == 'at') sectionTitle += 'At ' + settings['stop_timeType']
        if(settings['stop_sunOffset']) sectionTitle += settings['stop_sunOffset'] + ' minutes '
        if(settings['stop_sunType'] && settings['stop_sunType'] != 'at') sectionTitle += settings['stop_sunType'] + ' ' + settings['stop_timeType']
        if(checkIfStopTimeEnetered()) sectionTitle += ' ' + getSunriseTime(settings['stop_timeType'],settings['stop_sunOffset'],settings['stop_sunType'])
    }

    if(settings['start_timeType']) return sectionTitle + '</b>'
}

def displayTypeOption(startType){
    if(startType == 'stop' && !validateTimes('start')) return
    
    ingText = startType
    if(startType == 'stop') ingText = 'stopp'
    
    labelText = 'Schedule ' + startType
    if(validateTimes('start')) labelText = ''
    if(startType == 'start' && !validateTimes('start') || !settings[startType + '_timeType']) labelText = ''
    if(!validateTimes('start') || !settings[startType + '_timeType']) labelText = 'Schedule ' + ingText + 'ing time'
    
    if(labelText) displayLabel(labelText)

    if(!validateSunriseMinutes(startType)) displayWarning('Time ' + settings[startType + '_sunType'] + ' ' + settings[startType + '_timeType'] + ' is ' + (Math.round(settings[startType + '_sunOffset']) / 60) + ' hours. That\'s probably wrong.')
    
    fieldName = startType + '_timeType'
    fieldTitle = startType.capitalize() + ' time option:'
    if(!settings[startType + '_timeType']){
        fieldTitle = startType.capitalize() + ' time?'
        if(startType == 'stop') fieldTitle += ' (Select "Don\'t stop" for none)'
        highlightText(fieldTitle)
    }
    fieldTitle = addFieldName(fieldTitle,fieldName)
    fieldList = ['time':'Start at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
    if(startType == 'stop') fieldList = ['none':'Don\'t stop','time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
    input fieldName, 'enum', title: fieldTitle, multiple: false, width: getTypeOptionWidth(startType), options: fieldList, submitOnChange:true
    if(!settings['start_timeType']) displayInfo('Select whether to enter a specific time, or have start time based on sunrise and sunset for the Hubitat location. Required.')
}

def displayTimeOption(startType){
    if(startType == 'stop' && !validateTimes('start')) return
    if(startType == 'stop' && settings['stop_timeType'] == 'none') return
    if(settings[startType + '_timeType'] != 'time') return
    
    fieldName = startType + '_time'
    fieldTitle = startType.capitalize() + ' time:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(!settings[fieldName]) fieldTitle = highlightText(fieldTitle)
    input fieldName, 'time', title: fieldTitle, width: getTypeOptionWidth(startType), submitOnChange:true
    if(!settings[fieldName]) displayInfo('Enter the time to ' + startType + ' the schedule in "hh:mm AM/PM" format. Required.')
}

def getTypeOptionWidth(startType){
    if(!settings[startType + '_timeType']) return 12
    if(startType == 'stop' && settings[startType + '_timeType'] == 'none') return 12
    if(settings[startType + '_timeType'] == 'time') return 6
    if(settings[startType + '_sunType'] && settings[startType + '_sunType'] == 'at') return 6
    return 4
}

def displaySunriseTypeOption(startType){
    if(!settings[startType + '_timeType']) return
    if(settings[startType + '_timeType'] == 'time') return
    if(startType == 'stop' && settings['stop_timeType'] == 'none') return
    if(startType == 'stop' && !validateTimes('start')) return
    if(settings[startType + '_timeType'] != 'sunrise' && settings[startType + '_timeType'] != 'sunset') return
    
    sunTime = getSunriseAndSunset()[settings[startType + '_timeType']].format('hh:mm a')

    fieldName = startType + '_sunType'
    fieldTitle = 'At, before or after ' + settings[startType + '_timeType'] + ' (' + sunTime + '):'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(!settings[fieldName]) fieldTitle = highlightText(fieldTitle)
    input fieldName, 'enum', title: fieldTitle, multiple: false, width: getTypeOptionWidth(startType), options: ['at':'At ' + settings[startType + '_timeType'], 'before':'Before ' + settings[startType + '_timeType'], 'after':'After ' + settings[startType + '_timeType']], submitOnChange:true
    
    if(!settings[fieldName]) displayInfo('Select whether to start exactly at ' + settings[startType + '_timeType'] + ' (currently ' + sunTime + '). To allow entering minutes prior to or after ' + settings[startType + '_timeType'] + ', select "Before ' + settings[startType + '_timeType'] + '" or "After ' + settings[startType + '_timeType'] + '". Required.')
    displaySunriseOffsetOption(startType)
}

def getSunriseTime(startType,sunOffset,sunriseType){
    if(startType == 'sunrise' && sunriseType == 'before' && sunOffset) return '(' + new Date(parent.getSunrise(sunOffset * -1)).format('hh:mm a') + ')'
    if(startType == 'sunrise' && sunriseType == 'after' && sunOffset) return '(' + new Date(parent.getSunrise(sunOffset)).format('hh:mm a') + ')'
    if(startType == 'sunset' && sunriseType == 'before' && sunOffset) return '(' + new Date(parent.getSunset(sunOffset * -1)).format('hh:mm a') + ')'
    if(startType == 'sunset' && sunriseType == 'after' && sunOffset) return '(' + new Date(parent.getSunset(sunOffset)).format('hh:mm a') + ')'
    if(startType == 'sunrise' && sunriseType == 'at') return '(' + new Date(parent.getSunrise(0)).format('hh:mm a') + ')'
    if(startType == 'sunset' && sunriseType == 'at') return '(' + new Date(parent.getSunset(0)).format('hh:mm a') + ')'   
}

def displaySunriseOffsetOption(startType){
    if(startType == 'stop' && !validateTimes('start')) return
    if(startType == 'stop' && settings['stop_timeType'] == 'none') return
    if(!settings[startType + '_sunType']) return
    if(settings[startType + '_sunType'] == 'at') return

    fieldName = startType + '_sunOffset'
    fieldTitle = 'Minutes ' + settings[startType + '_sunType'] + ' ' + settings[startType + '_timeType'] + ':'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'number', title: fieldTitle, width: getTypeOptionWidth(startType), submitOnChange:true
    
    message = 'Enter the number of minutes ' + settings[startType + '_sunType'] + ' ' + settings[startType + '_timeType'] + ' to start the schedule. Required.'
    if(!settings[startType + '_sunOffset']) displayInfo(message)
    if(!validateSunriseMinutes(startType)) displayWarning(message)
}

def displayDaysAndDatesSection(){

    if(!settings['start_timeType']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
        hidden = true

    if(!settings['stop_timeType']) hidden = false
    if(settings['start_time'] && settings['start_time'] == settings['stop_time']) hidden = false
    if(settings['disable']) hidden = true
    if(!validateTimes('start')) hidden = false
    if(!validateTimes('stop')) hidden = false

    sectionTitle = 'Click to set days/dates (optional)'
    section(hideable: true, hidden: hidden, sectionTitle){

    displayDaysOption()
    displayDatesOptions()
    }
}
def displayDaysOption(){
    
    fieldName = 'days'
    fieldTitle = 'On these days (optional; defaults to all days):'
    if(!settings[fieldName]) fieldTitle = 'On which days (optional; defaults to all days)?'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'enum', title: fieldTitle, multiple: true, width: 12, options: ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday'], submitOnChange:true
}

def displayDatesOptions(){
    if(!settings['start_timeType']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    displayIncludeDates()
    displayExcludeDates()
}

def displayIncludeDates(){
    displayWarning(parent.getDateProcessingErrors(app.id))
    displayInfo(dateList)
    fieldName = 'includeDates'
    fieldTitle = 'Dates on which to run ("include"):'
    fieldTitle = addFieldName(fieldTitle,fieldName)
input fieldName, "textarea", title: fieldTitle, submitOnChange:true

}

def displayExcludeDates(){
    fieldName = 'excludeDates'
    fieldTitle = 'Dates on which to <u>not</u> run ("exclude"):'
    fieldTitle = addFieldName(fieldTitle,fieldName)
input fieldName, "textarea", title: fieldTitle, submitOnChange:true
    infoTip = 'Enter which date(s) to limit or exclude. Included dates are when the routine will run, for instance only on certain holidays. Excluded dates will prevent it from running, for instance \
every day <i>except</i> holidays. Rules:\n\
	• Year is optional, but would only apply to that <i>one day</i>. If no year is entered, it will repeat annually. \
<i>Example: "12/25/' + (new Date(now()).format('yyyy').toInteger() - 1) + '" will never occur, because it already has.</i>\n\
	• Enter dates as month/day ("mm/dd") format, or day.month ("dd.mm"). You can also use Julian days of the year as a 3-digit number ("ddd"). \
<i>Example: Christmas can be entered as 12/25, 25.12 or 359 [the latter only true for non-leap years].</i>\n\
	• Separate multiple dates with a comma (or semicolon). \
<i>Example: "12/25, 1/1" is Christmas and New Year\'s Day.</i>\n\
	• Use a hyphen to indicate a range of dates. \
<i>Example: "12/25-1/6" are the 12 days of Christmas.</i>\n\
    	• The "days" options above will combine with the dates. \
<i>Example: Selecting Monday and entering "12/25" as an included date would only trigger if Christmas is on a Monday.</i>\n\
	• Leap years count. \
<i>Example: Entering "2/29" (or "366") would only trigger on leap years.</i>\n\
	• You can mix and match formats (even tho you probably shouldn\'t), and individual dates with rangesranges. And the order doesn\'t matter. \
<i>Example: "001, 31.10, 12/25/' + (new Date(now()).format('yy').toInteger()) + '-12/31/' + (new Date(now()).format('yyyy').toInteger()) + '".</i>\n\
	• If a date is listed as both Included and Excluded, it will be treated as Excluded.\n\
	• To do Feb. 29, ignore the warning (on non-leap years). To do all of Febraury, enter "2/1-2/28, 2/29".'

displayInfo(infoTip)
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
    if(settings['stop_action'] && checkIfStopTimeEnetered()) sectionTitle += '<br><b>When stopping: ' + plainStopAction.capitalize() + '</b>'
    return sectionTitle
}

def displayActionField(startType){
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(startType == 'stop' && !checkIfStopTimeEnetered()) return

    ingText = startType
    if(startType == 'stop') ingText = 'stopp'
    fieldName = startType + '_action'
    fieldTitle = 'When ' + ingText + 'ing:'
    if(!settings[fieldName] && settings['deviceType'] == 'lock') fieldTitle = 'When ' + ingText + 'ing, lock or unlock (click to select)?'
    if(!settings[fieldName] && settings['deviceType'] != 'lock') fieldTitle = 'When ' + ingText + 'ing, do what with the ' + pluralDevice + ' (click to select)?'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(!settings[fieldName]) fieldTitle = highlightText(fieldTitle)

    fieldOptions = ['none': 'Don\'t turn on or off (leave as is)','on': 'Turn On', 'off': 'Turn Off', 'toggle': 'Toggle']
    if(settings['deviceType'] == 'lock') fieldOptions = ['none': 'Don\'t lock or unlock','lock': 'Lock', 'unlock': 'Unlock']
    input fieldName, 'enum', title: fieldTitle, multiple: false, width: 12, options: fieldOptions, submitOnChange:true

    if(settings['deviceType'] != 'lock' && !settings[fieldName]) displayInfo('Set whether to turn on, turn off, or toggle the ' + pluralDevice + ' when the schedule ' + startType + 's. Select "Don\'t" to control other options (like setting Mode), or to do nothing when ' + ingText + 'ing. Toggle will change the ' + pluralDevice + ' from off to on and vice versa. Required.')
}

def displayLevelsOption(levelType){
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(!settings['start_action']) return
    if(settings['start_action'] == 'off') return

    hidden = true
    if(settings['start_' + levelType] && (!settings['stop_' + levelType] && checkIfStopTimeEnetered())) hidden = false
    if(!settings['start_' + levelType] && (settings['stop_' + levelType] &&checkIfStopTimeEnetered())) hidden = false
    if(settings['start_' + levelType] && settings['start_' + levelType] == settings['stop_' + levelType]) hidden = false
    if(settings['disable']) hidden = true
    
    if(levelType == 'brightness') {
        if(!parent.checkIsDimmableMulti(settings['device'])) return
        if(!parent.validateLevel(settings['start_' + levelType])) hidden = false
        if(!parent.validateLevel(settings['stop_' + levelType])) hidden = false
        typeString = 'brightness'
        typeUnit = '%'
    }
    if(levelType == 'temp') {
        if(!parent.checkIsTempMulti(settings['device'])) return
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
    if(!validateTimes(startType)) return
    if(startType == 'stop' && !checkIfStopTimeEnetered()) return
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
    if(!checkIfStopTimeEnetered()) fieldWidth = 12
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
        if(!settings['start_' + levelType] && (!settings['stop_' + levelType] && checkIfStopTimeEnetered())) message = 'Enter the percentage of ' + typeString + ' from ' + typeRange + ' where 0' + typeUnit + ' is off, and 100' + typeUnit + ' is maximum.'
        //One entered
        if(!settings['start_' + levelType] && settings['stop_' + levelType]) typeString.capitalize() + ' is percentage from ' + typeRange + '. If entering both starting and stopping ' + typeString + ', it will transition from start level to ' + settings['stop_' + levelType] + typeUnit + ' over the duration of the schedule.'
        if(settings['start_' + levelType] && (!settings['stop_' + levelType] && checkIfStopTimeEnetered())) message = typeString.capitalize() + ' is percentage from ' + typeRange + '. If entering both starting and stopping ' + typeString + ', it will transition from ' + settings['start_' + levelType] + typeUnit + ' to stop level over the duration of the schedule.'
    }
    if(levelType == 'temp'){
        if(!parent.validateTemp(settings['start_' + levelType])) displayError('Start ' + typeString + ' must be from ' + typeRange + '. Correct start ' + typeString + '.')
        if(!parent.validateTemp(settings['stop_' + levelType])) displayError('Stop ' + typeString + ' must be from ' + typeRange + '. Correct stop ' + typeString + '.')
        //Both entered
        message = typeString.capitalize() + ' is from ' + typeRange + ' where lower is more yellow and higher is more blue. It will transition from ' + settings['start_' + levelType] + typeUnit + ' to ' + settings['start_' + levelType] + typeUnit + ' ' + typeString + ' over the duration of the schedule.'
        //Neither entered
        if(!settings['start_' + levelType] && (!settings['stop_' + levelType] && checkIfStopTimeEnetered())) message = typeString.capitalize() + ' is from ' + typeRange + ' where lower is more yellow and higher is more blue; 3000' + typeUnit + ' is warm white, 4000' + typeUnit + ' is cool white, and 5000' + typeUnit + ' is daylight.'
        //One entered
        if(!settings['start_' + levelType] && settings['stop_' + levelType]) message = typeString.capitalize() + ' is from ' + typeRange + ' where lower is more yellow and higher is more blue. If entering both starting and stopping ' + typeString + ', it will transition from start level to ' + settings['stop_' + levelType] + typeUnit + ' over the duration of the schedule.'
        if(settings['start_' + levelType] && (!settings['stop_' + levelType] && checkIfStopTimeEnetered())) message = typeString.capitalize() + ' is from ' + typeRange + ' where lower is more yellow and higher is more blue. If entering both starting and stopping ' + typeString + ', it will transition from ' + settings['start_' + levelType] + typeUnit + ' to stop level over the duration of the schedule.'
    }
    return message
}

def displayColorOption(){
    if(!parent.checkIsColoreMulti(settings['device'])) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(!settings['start_action']) return
    if(settings['start_action'] == 'off') return
    if(!checkIfStopTimeEnetered()) settings['stop_hue'] = null
    if(!checkIfStopTimeEnetered()) settings['stop_sat'] = null

    typeUnit = '°'

    hidden = true
    if(!validateColor()) hidden = false
    if(settings['start_hue'] && (!settings['stop_hue'] && checkIfStopTimeEnetered())) hidden = false
    if(settings['start_sat'] && (!settings['stop_sat'] && checkIfStopTimeEnetered())) hidden = false
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
            if(validateTimes('stop') && checkIfStopTimeEnetered()) message += ' If entering both starting and ending hue, it will transition from starting to ending hue for the duration of the schedule.'
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
            if(validateTimes('stop') && checkIfStopTimeEnetered()) message += ' If entering both starting and ending saturation, it will transition from starting to ending saturation for the duration of the schedule.'
            message += ' Optional.'
        }
        displayInfo(message)
    }
}

def displayHueDirection(){
    if(!settings['start_hue']) return
    if(!settings['stop_hue']) return
    if(!checkIfStopTimeEnetered()) settings['hueDirection'] = null

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
    if(settings['startMode'] && (!settings['stopMode'] && checkIfStopTimeEnetered())) hidden = false
    if(!settings['startMode'] && settings['stopMode']) hidden = false
    if(settings['disable']) hidden = true

    width = 12
    if(checkIfStopTimeEnetered()) width = 6
    
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

def displayModeField(startType){
    if(startType == 'stop' && !checkIfStopTimeEnetered()) return
    fieldName = startType + 'Mode'
    fieldTitle = 'Set Hubitat\'s "Mode" on ' + startType + '?'
    if(settings['startMode']) fieldTitle = 'Set Hubitat\'s "Mode" on ' + startType + ':'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    fieldWitch = 6
    if(!checkIfStopTimeEnetered()) fieldWidth = 12
    input fieldName, 'mode', title: fieldTitle, width: 6, submitOnChange:true
}

def displayPeopleOption(){
    if(!settings['start_action']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(checkIfStopTimeEnetered() && !settings['stop_action']) return

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
        
        if(checkIfStopTimeEnetered()) message = 'Schedules will resume/pause based on people present/away. If requirements are met at start time, any stop actions/levels will be applied.'
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
        if(checkIfStopTimeEnetered()) message += '. Schedules will resume/pause based on Mode. If the Mode matches at start time, any stop actions/levels will be applied'
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

def displayTextField(fieldName,fieldTitle,fieldType,required = true){
    width = 10
    if(!settings[fieldName]) width = 12
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(required && !settings[fieldName]) displayLabel(highlightText(fieldTitle))
    if(!required && !settings[fieldName]) displayLabel(fieldTitle)
    input name: fieldName, type: fieldType, title: '', width:width, submitOnChange:true
}
def displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse,required = true,defaultValue = false){
    if(!fieldTitleTrue) fieldTitleTrue = ''
    if(!fieldTitleFalse) fieldTitleFalse = ''
    if(required && fieldTitleTrue) fieldTitleTrue = highlightText(fieldTitleTrue)
    if(fieldTitleTrue) fieldTitle = fieldTitleTrue
    if(required) fieldTitle += ' ' + fieldTitleFalse
    if(!required) fieldTitle += '<br> ' + fieldTitleFalse
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input name: fieldName, type: 'bool', title:fieldTitle, default:defaultValue,submitOnChange:true
}
def displaySelectField(fieldName,fieldTitle,options,multiple = false,required = true, button = ''){
    width = 12
    if(!settings[fieldName] && button) width = 11
    if(settings[fieldName]){
        width = 10
        if(button) width = 9
    }
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(required && !settings[fieldName]) displayLabel(highlightText(fieldTitle))
    if(!required && !settings[fieldName]) displayLabel(fieldTitle)
    input name: fieldName, type: 'enum', title: '', options: options, width:width, multiple: multiple, submitOnChange:true
    if(button) displayFilterButton(button)
}
def displayDeviceSelectField(fieldName,fieldTitle,capability,multiple, button = ''){
    width = 12
    if(button) width = 11
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input name: fieldName, type: capability, title:fieldTitle, multiple: multiple, submitOnChange:true, width: width
    if(button) displayFilterButton(button)
}
def displayModeSelectField(fieldName,fieldTitle,options,multiple = false,required = true){
    width = 10
    if(!settings[fieldName]) width = 12
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(required && !settings[fieldName]) displayLabel(highlightText(fieldTitle))
    if(!required && !settings[fieldName]) displayLabel(fieldTitle)
    input name: fieldName, type: 'mode', title: '', options: options, width:width, multiple: multiple, submitOnChange:true
}

def appButtonHandler(buttonValue){
      switch(buttonValue) {
          case 'controllerButton':
          if(!settings[buttonValue + 'Value']){
              app.updateSetting(buttonValue + 'Value', [type: 'bool', value: 'true'])
              break
          }
              app.updateSetting(buttonValue + 'Value', [type: 'bool', value: 'false'])
          break
          case 'controlButton':
          if(!settings[buttonValue + 'Value']){
              app.updateSetting(buttonValue + 'Value', [type: 'number', value: 1])
              break
          }
          if(settings[buttonValue + 'Value'] == 1){
              app.updateSetting(buttonValue + 'Value', [type: 'number', value: 2])
              break
          }
          app.removeSetting(buttonValue + 'Value')
          break
      }
}

def displayFilterButton(buttonName){
    if(buttonName == 'controllerButton'){
        if(settings[buttonName + 'Value']) {
            input buttonName, 'button', title: filterYesIcon + ' Filter', width:1
        }
        if(!settings[buttonName + 'Value']){
            input buttonName, 'button', title: filterNoIcon + ' Filter', width:1
        }
        return
    }
    if(buttonName == 'controlButton'){
        if(!settings[buttonName + 'Value']) {
            input buttonName, 'button', title: filterSwitchIcon + ' Filter', width:1
        }
        if(settings[buttonName + 'Value'] == 1){
            input buttonName, 'button', title: filterLightIcon + ' Filter', width:1
        }
        if(settings[buttonName + 'Value'] == 2){
            input buttonName, 'button', title: filterColorIcon + ' Filter', width:1
        }
    }
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
    putLog(1079,'trace',app.label + ' initializing.')
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
    startTime = parent.getDatetimeFromTimeInMillis(atomicState.startTime,app.label)
    stopTime = parent.getDatetimeFromTimeInMillis(atomicState.stopTime,app.label)
    if(stopTime && stopTime < startTime) stopTime += parent.CONSTDayInMilli()
    
    alreadyRunning = parent.checkNowBetweenScheduledStartStopTimes(startTime,stopTime,app.label)
    if(!alreadyRunning) setStartSchedule()
    if(alreadyRunning) runDailyStartSchedule()
    if(!alreadyRunning) setStopSchedule()

    putLog(1111,'info',app.label + ' initialized.')
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
    setTime()
    timeMillis = parent.getDatetimeFromTimeInMillis(atomicState.startTime) - now()
    if(timeMillis < 0) timeMillis += parent.CONSTDayInMilli() + 5000   // Add seconds to allow stop schedule(s) to run
    parent.scheduleChildEvent(timeMillis,'','runDailyStartSchedule','',app.id)
    
    return true
}

def setStopSchedule(){
    setTime()
    if(!atomicState.stopTime) return
    timeMillis = parent.getDatetimeFromTimeInMillis(atomicState.stopTime) - now()
    if(timeMillis < 0) timeMillis += parent.CONSTDayInMilli()
    parent.scheduleChildEvent(timeMillis,'','runDailyStopSchedule','',app.id)

    return true
}

// Performs actual changes at time set with start_action
// Called only by schedule set in incrementalSchedule
def runDailyStartSchedule(){
    if(settings['disabled']) return
    putLog(1062,'info',app.label + ' schedule has started.')
    
    setStartSchedule()
    setStopSchedule()
    
    if(!checkIncludeDates()) return

    setStartSchedule()
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
        putLog(1202,'debug','Performing start action(s) for ' + singleDevice + ' as ' + fullMap + '.')
    }
    parent.setDeviceMulti(settings['device'],app.label)
}

// Performs actual changes at time set with start_action
// Called only by schedule set in incrementalSchedule
def runDailyStopSchedule(){
    if(settings['disabled']) return
    putLog(1211,'info',app.label + ' schedule has ended.')

    unschedule('runIncrementalSchedule')    //This doesn't seem to work
    setStartSchedule()
    setStopSchedule()
    atomicState.remove('scheduleFrequency')

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
        putLog(1232,'debug','Performing stop action(s) for ' + singleDevice + ' as ' + fullMap + '.')
    }
    parent.setDeviceMulti(settings['device'],app.label)
    atomicState.remove('startTime')        // Cleared to prevent runIncremental from running
    atomicState.remove('stopTime')
    atomicState.stopDateTime = null
}

// Is unscheduled from runDailyStopSchedule
def runIncrementalSchedule(){
    if(settings['disabled']) return

    setStopDateTime()
    if(atomicState.stopDateTime < now()) return

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
            putLog(1264,'debug','Incremental schedule for ' + singleDevice + ' settings are ' + incrementalMap)
            anyDevicesChanged = true
            parent.mergeMapToTable(singleDevice.id, levelMap)
        }
        if(!incrementalMap) putLog(1268,'debug','Incremental schedule for ' + singleDevice + ' has no changes.')
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
    if(settings['stopTime'] == settings['startTime']) return    // Prevents lights returning to default settings (and flickering)
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
    atomicState.startTime = getBaseStartStopTimes('start')
    atomicState.stopTime = getBaseStartStopTimes('stop')
    if(atomicState.startTime == atomicState.stopTime) atomicState.stopTime -= 1 // If start and stop are the same, stop needs to be smaller, to be seen as next day
}
def setStopDateTime(){      // Should only be run from Incremental
    if(atomicState.startDateTime) return
    if(atomicState.stopDateTime) return
    startDateTime = parent.getDatetimeFromTimeInMillis(atomicState.startTime)
    stopDateTime = parent.getDatetimeFromTimeInMillis(atomicState.stopTime)
    if(stopDateTime < startDateTime) stopDateTime += parent.CONSTDayInMilli()
    atomicState.stopDateTime = stopDateTime
}

// Returns 'start' or 'stop' time (of day) in millis
// Must be converted with getDatetimeFromTimeInMillis if compared to now()
def getBaseStartStopTimes(type){
    if(type == 'stop' && settings['stop_timeType'] == 'none') return
    if(settings[type + '_timeType'] == 'time') {
        if(!settings[type + '_time']) return
        return parent.getTimeOfDayInMillis(timeToday(settings[type + '_time']).getTime()) + 1   // Add 1 so midnight isn't "empty" as zero
    }
    if(!settings[type + '_sunType']) return
    if(settings[type + '_timeType'] == 'sunrise') return parent.getTimeOfDayInMillis((settings[type + '_sunType'] == 'before' ? parent.getSunrise(settings[type + '_sunOffset'] * -1,app.label) : parent.getSunrise(settings[type + '_sunOffset'],app.label)))
    if(settings[type + '_timeType'] == 'sunset') return parent.getTimeOfDayInMillis((settings[type + '_sunType'] == 'before' ? parent.getSunset(settings[type + '_sunOffset'] * -1,app.label) : parent.getSunset(settings[type + '_sunOffset'],app.label)))
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

    // need to check if time was before schedule started
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
def processDates(){
    atomicState.remove('includeDates')
    if(!settings['days'] && !settings['includeDates'] && !settings['excludeDates']) return
    currentYear = new Date(now()).format('yyyy').toInteger()
    includeDatesValue = settings['includeDates']
    if(!settings['includeDates'] && (settings['days'] || settings['excludeDates'])) includeDatesValue = '1/1-12/31'
    atomicState.'includeDates' = [(currentYear):parent.processDates(settings['includeDates'], settings['excludeDates'], settings['days'], app.id, true)]
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

// Checks if schedule is to run or not
def checkIncludeDates(){
    if(!atomicState.includeDates) return true
    currentYear = new Date(now()).format('yyyy')
    if(!atomicState.includeDates[currentYear]) processDates()
    if(atomicState.includeDates[currentYear].contains(new Date(now()).format('D'))) return true
}
//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,type = 'trace',message = null){
    return parent.putLog(lineNumber,type,message,app.label,,getLogLevel())
}
