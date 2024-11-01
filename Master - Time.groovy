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
*  Version: 0.7.2.34
*
***********************************************************************************************************************/

// TO-DO: Allow selecting rooms??

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
// 2 for warnings + errors
// 3 for info + errors + warnings
// 4 for trace + info + errors + warnings
// 5 for debug + trace + info + errors + warnings
def getLogLevel(){
    return 5
}

setUILinks()

preferences {
    if(!settings) settings = [:]
    install = formComplete()
    thisType = 'schedule'
    thisDescription = 'schedule'        // Used with schedule, people, ifMode
    thisDescriptionPlural = 'schedule'
    
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

            peopleError = compareDeviceLists(personHome,personNotHome)
            
            section(){
                displayNameOption()
                displayAdvancedOption()
                displayControlDeviceOption()
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
    if(!settings['controlDevice']) return false
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

def getDeviceCount(device){
    if(!device) return 0
    return device.size()
}

def checkIfStopTimeEntered(){
    if(settings['stop_timeType'] == 'none') return false
    if(!validateTimes('stop')) return false
    if(!settings['stop_timeType']) return false
    return true
}

def displayScheduleSection(){
    if(!settings['controlDevice']) return
    
    hidden = true
    if(!settings['stop_timeType']) hidden = false
    if(!validateTimes('start')) hidden = false
    if(!validateTimes('stop')) hidden = false
    if(!settings['start_timeType']) hidden = false
    if(parent.getDateProcessingErrors(app.id)) hidden = false
    section(hideable: true, hidden: hidden, getTimeSectionTitle()){
        displayTimeTypeOption('start')
        displayTimeOption('start')
        displaySunriseTypeOption('start')
        displayTimeTypeOption('stop')
        displayTimeOption('stop')
        displaySunriseTypeOption('stop')

// This was made using using getBaseStartStopTimes for time, rather than getBaseStartStopDateTime for date/time
        if(validateTimes('start') && checkIfStopTimeEntered() && getBaseStartStopDateTime('start') > getBaseStartStopDateTime('stop')) displayInfo('Stop time is before start time, which is perfectly fine, but stop time will be assumed to be the next day (i.e. the duration of this schedule will be approximately ' + Math.round((getBaseStartStopDateTime('stop') + parent.CONSTDayInMilli() - getBaseStartStopDateTime('start')) / parent.CONSTHourInMilli()) + ' hours).')
        if(validateTimes('start') && checkIfStopTimeEntered() && getBaseStartStopDateTime('start') > getBaseStartStopDateTime('stop') && settings['days']) displayInfo('Every schedule that starts will stop, even if the stop day falls on a day not permitted. For instance, if scheduled for only Mondays and the stop time falls on the next day, stop actions/levels will still be set.')
    }
}

def displayActionOption(){
    if(!settings['controlDevice']) return
    if(!settings['start_timeType']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return

// This was made using using getBaseStartStopTimes for time, rather than getBaseStartStopDateTime for date/time
    if(settings['start_timeType'] && validateTimes('start') && validateTimes('stop') && getBaseStartStopDateTime('start') == getBaseStartStopDateTime('stop') && settings['stop_action'] != 'none') errorMessage = 'Having the same time for start and stop makes it a daily schedule means it never stops, it just starts over, so you can\'t have a stop action.'

    hidden = true
    if(!settings['start_action']) hidden = false
    if(!settings['stop_action']) hidden = false
    if(settings['start_action'] == 'none' && (settings['stop_action'] == 'none' || settings['stop_timeType'] == 'none')) hidden = true
    if(errorMessage) hidden = false

    section(hideable: true, hidden: hidden, getActionSectionTitle()){
        displayError(errorMessage)
        displayActionField('start')
        displayActionField('stop')
    }
}

def getActionSectionTitle(){
    if(!settings['start_action'] && !settings['stop_action']) return '<b>Set action to perform on start</b>'
   // if(settings['start_action'] == 'none' && (settings['stop_action'] == 'none' || settings['stop_timeType'] == 'none')) return 'Doing nothing'
    sectionTitle = ''
    if(settings['start_action']) sectionTitle = '<b>When starting: ' + plainStartAction.capitalize() + '</b>'
    if(!settings['stop_action']) return sectionTitle
    if(settings['stop_action'] && checkIfStopTimeEntered()) sectionTitle += '<br><b>When stopping: ' + plainStopAction.capitalize() + '</b>'
    return sectionTitle
}

def displayActionField(startType){
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(startType == 'stop' && !checkIfStopTimeEntered()) return

    fieldName = startType + '_action'
    displayActionFieldComplete(startType,fieldName)
    displayActionFieldIncomplete(startType,fieldName)
 }
 def displayActionFieldComplete(startType,fieldName){
    if(!settings[fieldName]) return
    fieldTitle = 'On ' + startType
    fieldOptions = ['none': 'Don\'t turn on or off (leave as is)','on': 'Turn On', 'off': 'Turn Off', 'toggle': 'Toggle']
    displaySelectField(fieldName,fieldTitle,fieldOptions,false,required = true, '')

 }
 def displayActionFieldIncomplete(startType,fieldName){
    if(settings[fieldName]) return
    app.removeSetting(fieldName)
    // if no action on start, default to none at stop (then call displayActionFieldComplete)
   // if(startType == 'stop' && settings['start_action'] == 'none' && !settings[fieldName]) app.updateSetting(fieldName, [type: 'string', value: 'none'])
    devicesText = 'device'
    if(settings['controlDevice'].size() > 1) devicesText = 'devices'
    fieldTitle = 'When starting, do what with the ' + devicesText + ' (click to select)?'
    if(startType == 'stop') fieldTitle = 'When stopping, do what with the ' + devicesText + ' (click to select)?'
    fieldOptions = ['none': 'Don\'t turn on or off (leave as is)','on': 'Turn On', 'off': 'Turn Off', 'toggle': 'Toggle']
    displaySelectField(fieldName,fieldTitle,fieldOptions,false,required = true, '')
 }

def displayLevelsOption(levelType){
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(!settings['start_action']) return
    if(!settings['stop_action']) return
    if(settings['start_action'] == 'off') return
    
    hidden = true
    if(settings['start_' + levelType] && (!settings['stop_' + levelType] && checkIfStopTimeEntered())) hidden = false
    if(!settings['start_' + levelType] && (settings['stop_' + levelType] && checkIfStopTimeEntered())) hidden = false
    if(settings['start_' + levelType] && settings['start_' + levelType] == settings['stop_' + levelType]) hidden = false
    
    if(levelType == 'brightness') {
        if(!parent.checkIsDimmableMulti(settings['controlDevice'])) return
        if(!parent.validateLevel(settings['start_' + levelType])) hidden = false
        if(!parent.validateLevel(settings['stop_' + levelType])) hidden = false
        typeString = 'brightness'
        typeUnit = '%'
    }
    if(levelType == 'temp') {
        if(!parent.checkIsTempMulti(settings['controlDevice'])) return
        if(!parent.validateTemp(settings['start_' + levelType])) hidden = false
        if(!parent.validateTemp(settings['stop_' + levelType])) hidden = false
        typeString = 'temperature color'
        typeUnit = 'K'
    }
       
    sectionTitle = ''
    if(!settings['start_' + levelType] && !settings['stop_' + levelType]) sectionTitle = 'Click to set ' + typeString + ' (optional)'
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
    if(startType == 'stop' && !checkIfStopTimeEntered()) return
    if(startType == 'stop' && !settings['start_' + levelType]) return
    if(!settings['start_action']) return
    if(!settings['stop_action']) return
    
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
    if(!checkIfStopTimeEntered()) fieldWidth = 12
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
        if(!settings['start_' + levelType] && (!settings['stop_' + levelType] && checkIfStopTimeEntered())) message = 'Enter the percentage of ' + typeString + ' from ' + typeRange + ' where 0' + typeUnit + ' is off, and 100' + typeUnit + ' is maximum.'
        //One entered
        if(!settings['start_' + levelType] && settings['stop_' + levelType]) typeString.capitalize() + ' is percentage from ' + typeRange + '. If entering both starting and stopping ' + typeString + ', it will transition from start level to ' + settings['stop_' + levelType] + typeUnit + ' over the duration of the schedule.'
        if(settings['start_' + levelType] && (!settings['stop_' + levelType] && checkIfStopTimeEntered())) message = typeString.capitalize() + ' is percentage from ' + typeRange + '. If entering both starting and stopping ' + typeString + ', it will transition from ' + settings['start_' + levelType] + typeUnit + ' to stop level over the duration of the schedule.'
    }
    if(levelType == 'temp'){
        if(!parent.validateTemp(settings['start_' + levelType])) displayError('Start ' + typeString + ' must be from ' + typeRange + '. Correct start ' + typeString + '.')
        if(!parent.validateTemp(settings['stop_' + levelType])) displayError('Stop ' + typeString + ' must be from ' + typeRange + '. Correct stop ' + typeString + '.')
        //Both entered
        message = typeString.capitalize() + ' is from ' + typeRange + ' where lower is more yellow and higher is more blue. It will transition from ' + settings['start_' + levelType] + typeUnit + ' to ' + settings['start_' + levelType] + typeUnit + ' ' + typeString + ' over the duration of the schedule.'
        //Neither entered
        if(!settings['start_' + levelType] && (!settings['stop_' + levelType] && checkIfStopTimeEntered())) message = typeString.capitalize() + ' is from ' + typeRange + ' where lower is more yellow and higher is more blue; 3000' + typeUnit + ' is warm white, 4000' + typeUnit + ' is cool white, and 5000' + typeUnit + ' is daylight.'
        //One entered
        if(!settings['start_' + levelType] && settings['stop_' + levelType]) message = typeString.capitalize() + ' is from ' + typeRange + ' where lower is more yellow and higher is more blue. If entering both starting and stopping ' + typeString + ', it will transition from start level to ' + settings['stop_' + levelType] + typeUnit + ' over the duration of the schedule.'
        if(settings['start_' + levelType] && (!settings['stop_' + levelType] && checkIfStopTimeEntered())) message = typeString.capitalize() + ' is from ' + typeRange + ' where lower is more yellow and higher is more blue. If entering both starting and stopping ' + typeString + ', it will transition from ' + settings['start_' + levelType] + typeUnit + ' to stop level over the duration of the schedule.'
    }
    return message
}

def displayColorOption(){
    if(!parent.checkIsColorMulti(settings['controlDevice'])) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(!settings['start_action']) return
    if(!settings['stop_action']) return
    if(settings['start_action'] == 'off') return
    if(!checkIfStopTimeEntered()) settings['stop_hue'] = null
    if(!checkIfStopTimeEntered()) settings['stop_sat'] = null

    typeUnit = '°'

    hidden = true
    if(!validateColor()) hidden = false
    if(settings['start_hue'] && (!settings['stop_hue'] && checkIfStopTimeEntered())) hidden = false
    if(settings['start_sat'] && (!settings['stop_sat'] && checkIfStopTimeEntered())) hidden = false
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
            if(validateTimes('stop') && checkIfStopTimeEntered()) message += ' If entering both starting and ending hue, it will transition from starting to ending hue for the duration of the schedule.'
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
            if(validateTimes('stop') && checkIfStopTimeEntered()) message += ' If entering both starting and ending saturation, it will transition from starting to ending saturation for the duration of the schedule.'
            message += ' Optional.'
        }
        displayInfo(message)
    }
}

def displayHueDirection(){
    if(!settings['start_hue']) return
    if(!settings['stop_hue']) return
    if(!checkIfStopTimeEntered()) settings['hueDirection'] = null

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
    if(!settings['controlDevice']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(!settings['start_action']) return
    if(!settings['stop_action']) return
    if(!settings['advancedSetup']) return

    hidden = true
    if(settings['startMode'] && (!settings['stopMode'] && checkIfStopTimeEntered())) hidden = false
    if(!settings['startMode'] && settings['stopMode']) hidden = false

    width = 12
    if(checkIfStopTimeEntered()) width = 6
    
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
    if(startType == 'stop' && !checkIfStopTimeEntered()) return
    fieldName = startType + 'Mode'
    fieldTitle = 'Set Hubitat\'s "Mode" on ' + startType + '?'
    if(settings['startMode']) fieldTitle = 'Set Hubitat\'s "Mode" on ' + startType + ':'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    fieldWitch = 6
    if(!checkIfStopTimeEntered()) fieldWidth = 12
    input fieldName, 'mode', title: fieldTitle, width: 6, submitOnChange:true
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
    putLog(544,'info','^')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    subscribeDevices()

    resume()
    
    putLog(550,'info','¬')
    return true
}
def resume() {
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))

    setControlDeviceId()
    unschedule()
    clearScheduleFromTable()    // Clear schedule from master table, to avoid stale settings

    subscribeDevices()
    
    setScheduleTime()

    if(getBaseStartStopDateTime('start') && getBaseStartStopDateTime('stop')){
        if(getBaseStartStopDateTime('start') < now() && getBaseStartStopDateTime('stop') > now()) runStartSchedule = true
        if(getBaseStartStopDateTime('start') < now() && getBaseStartStopDateTime('stop') < getBaseStartStopDateTime('start')) runStartSchedule = true
        if(getBaseStartStopDateTime('start') > now() && getBaseStartStopDateTime('stop') > now() && getBaseStartStopDateTime('start') > getBaseStartStopDateTime('stop')) runStartSchedule = true
    }
    if(!runStartSchedule) {
        setStartSchedule()
        parent.setDeviceMulti(settings['controlDevice'],app.id)    // If schedule was running, reset device
    }
    if(runStartSchedule) runDailyStartSchedule()
}

def handleStateChange(event){
    parent.updateTableCapturedState(event.device,event.value,app.id)
}

// this does level, temp, hue, and sat
def handleBrightnessChange(event){
    parent.updateTableCapturedLevel(event.device,'brightness',app.id)
}

// This needs rewrite
def handleTempChange(event){
    parent.updateTableCapturedLevel(event.device,'colorTemperature',app.id)
}

// This needs rewrite
def handleHueChange(event){
    return    // Captures pico hue change as being a "manual" change and updates (but doesn't seem to hurt anything)
    parent.updateTableCapturedLevel(event.device,'hue',app.id)
}

def handleSatChange(event){
    return    // Captures pico hue change as being a "manual" change and updates (but doesn't seem to hurt anything)
    parent.updateTableCapturedLevel(event.device,'saturation',app.id)
}

// Creates the schedule for start and stop
def setStartSchedule(){
    timeMillis = getBaseStartStopDateTime('start') - now()
    if(timeMillis < 0) timeMillis += parent.CONSTDayInMilli() + 5000   // Add seconds to allow stop schedule(s) to run; not sure about this
    parent.scheduleChildEvent(timeMillis,'','runDailyStartSchedule','',app.id)
    
    return true
}

def setStopSchedule(){
    if(!getBaseStartStopDateTime('stop')) return
    
    timeMillis = getBaseStartStopDateTime('stop') - now()
    if(timeMillis < 0) timeMillis += parent.CONSTDayInMilli()
    parent.scheduleChildEvent(timeMillis,'','runDailyStopSchedule','',app.id)

    return true
}

// Performs actual changes at time set with start_action
// Called only by schedule set in incrementalSchedule
def runDailyStartSchedule(){
    putLog(623,'info','^')

    setStartSchedule()        // Reschedule
    setStopSchedule()    // Need to do this for first time it runs (could be in initialize instead)

    if(!checkIncludeDates()) return

    clearScheduleFromTable() // clear out any "manual overrides"

    if(checkIfIncremental()){
        setScheduleTime()
        parent.scheduleChildEvent(atomicState.baseFrequency,'','runIncrementalSchedule','',app.id)
    }

    if(atomicState.stopTime){
        if(!getEnvironmentActive()) {
            atomicState.activeAtStart = false
            return
        }
        atomicState.activeAtStart = true
    }

    brightnessMap = getLevelMap('brightness','start')
    tempMap = getLevelMap('temp','start')
    hueMap = getLevelMap('hue','start')
    satMap = getLevelMap('sat','start')
    scheduleMap = parent.addMaps(brightnessMap, tempMap, hueMap, satMap,'',app.id)

    settings['controlDevice'].each{singleDevice->
        stateMap = parent.getStateMapSingle(singleDevice,settings['start_action'],app.id)          // Needs singleDevice for toggle
        parent.mergeMapToTable('schedule',singleDevice.id,scheduleMap,app.id)
        parent.mergeMapToTable('state',singleDevice.id,stateMap,app.id)
        putLog(655,'debug','[' + singleDevice + '] schedule Start ' + scheduleMap)
    }
    parent.setDeviceMulti(settings['controlDevice'],app.id)

    if(!atomicState.stopTime) atomicState.remove('startTime')
    putLog(60,'info','¬')
}

// Performs actual changes at time set with start_action
// Called only by schedule set in incrementalSchedule
def runDailyStopSchedule(){
    putLog(666,'info','^')

    unschedule('runIncrementalSchedule')    //This doesn't seem to work
    setStopSchedule()        // Don't run Start Schedule, because we don't want to trigger setScheduleTime within dailyStopSchedule - if we do, it must be prior to removing state vars
    
    activeAtStart = atomicState.activeAtStart
    atomicState.remove('startTime')        // Clear out schedule times (can changed with sunrise/sunset)
    atomicState.remove('stopTime')
    atomicState.remove('stopDateTime')
    atomicState.remove('totalTime')
    atomicState.remove('baseFrequency')
    atomicState.remove('activeAtStart')
    
    clearScheduleFromTable()        // Clear schedule from master table
    
    if(!activeAtStart) return
    brightnessMap = getLevelMap('brightness','stop')
    tempMap = getLevelMap('temp','stop')
    hueMap = getLevelMap('hue','stop')
    satMap = getLevelMap('sat','stop')
    scheduleMap = parent.addMaps(brightnessMap, tempMap, hueMap, satMap,'',app.id)
    settings['controlDevice'].each{singleDevice->
        stateMap = parent.getStateMapSingle(singleDevice.id,settings['stop_action'],app.id)          // Needs singleDevice for toggle
        parent.mergeMapToTable('schedule',singleDevice.id,scheduleMap,app.id)
        parent.mergeMapToTable('state',singleDevice.id,stateMap,app.id)
        if(scheduleMap.size() > 0) putLog(691,'debug','[' + singleDevice + '] schedule Stop ' + scheduleMap)
    }
    parent.setDeviceMulti(settings['controlDevice'],app.id)
    putLog(694,'info','¬')
}

// Is unscheduled from runDailyStopSchedule
def runIncrementalSchedule(){
    if(!atomicState.stopDateTime) return
    if(atomicState.stopDateTime < now()) return        // This should never happen; add error? Reset state vars?

    if(!getEnvironmentActive()) {
        // Remove table entries, to be re-added if schedule becomes active again
        clearScheduleFromTable()
        parent.scheduleChildEvent(parent.CONSTScheduleMinimumInactiveFrequencyMilli(),'','runIncrementalSchedule','',app.id)        // Reschedule (if not active)
        return
    }
    
    putLog(709,'info','^')
    allDevicesOff = true           // True is to set time as CONSTScheduleMinimumInactiveFrequencyMilli; not using checkAnyOnMulti because already looping devices
    anyDevicesChanged = false       // True is to set devices
    settings['controlDevice'].each{singleDevice->
        if(parent.checkIsOn(singleDevice,app.id)) allDevicesOff = false
        brightnessMap = getIncrementalMaps(singleDevice,'brightness')
        tempMap = getIncrementalMaps(singleDevice,'temp')
        hueMap = getIncrementalMaps(singleDevice,'hue')
        satMap = getIncrementalMaps(singleDevice,'sat')
        incrementalMap = parent.addMaps(brightnessMap, tempMap, hueMap, satMap,'',app.id)
        if(incrementalMap) {
            putLog(720,'debug','[' + singleDevice + '] incremental ' + incrementalMap)
            anyDevicesChanged = true
            parent.mergeMapToTable('schedule',singleDevice.id, incrementalMap,app.id)
        }
        if(!incrementalMap) putLog(724,'debug','[' + singleDevice + '] incremental no changes.')
    }
    
    timeMillis = atomicState.baseFrequency
    if(allDevicesOff && timeMillis < parent.CONSTScheduleMinimumInactiveFrequencyMilli()) timeMillis = parent.CONSTScheduleMinimumInactiveFrequencyMilli()
    
    parent.scheduleChildEvent(timeMillis, '', 'runIncrementalSchedule', '', app.id)        // Reschedule
    
    if(anyDevicesChanged) parent.setDeviceMulti(settings['controlDevice'], app.id)
    putLog(733,'info','¬')
}

def getLevelMap(type,startStop){
    if(!settings[startStop + '_' + type]) return
    if(!settings[startStop + '_' + type] == 0) return

    return parent.getLevelMap(type,settings[startStop + '_' + type],app.id)
}

def getIncrementalMaps(singleDevice,type){
    if(parent.getAppIdForDeviceFromTable(singleDevice.id,type,app.id) == 'manual') return
    
    newLevel = getIncrementalLevelSingle(singleDevice, type)
    return parent.getLevelMap(type, newLevel, app.id)
}

def subscribeDevices(){
    unsubscribe()
    subscribe(settings['controlDevice'], 'switch', handleStateChange)
    subscribe(settings['controlDevice'], 'hue', handleHueChange)
    subscribe(settings['controlDevice'], 'saturation', handleSatChange)
    subscribe(settings['controlDevice'], 'colorTemperature', handleTempChange)
    subscribe(settings['controlDevice'], 'level', handleBrightnessChange)
    subscribe(settings['controlDevice'], 'speed', handleBrightnessChange)
    subscribe(location, 'systemStart', handleSystemBoot)
    subscribe(location,'timeZone',handleTimezone)
    return
}

def clearScheduleFromTable(){
    if(settings['stop_time'] == settings['start_time']) return    // Prevents lights returning to default settings (and flickering)
    putLog(765,'debug','Removing scheduled settings from master device table.')
    settings['controlDevice'].each{singleDevice->
        parent.clearTableDevice('schedule', singleDevice.id,appId)
        clearTableKey(singleDevice.id,'brightness')
        clearTableKey(singleDevice.id,'temp')
        clearTableKey(singleDevice.id,'hue')
        clearTableKey(singleDevice.id,'sat')
    }
}

def clearTableKey(singleDeviceId,type){
    if(!singleDeviceId) return
    if(!type) return
    levelAppId = parent.getAppIdForDeviceFromTable(singleDeviceId,type,app.id)
    if(levelAppId){
        if(levelAppId == app.id) clearKey = true
        levelTime = parent.getTimeForDeviceFromTable(singleDeviceId,type,app.id)
        // Clear any "manual overrides" set prior to schedule
        // Perhaps should only be done with settings used by this schedule
        // However, runIncremental does not check times (to maximize run speed)
        if(levelAppId == 'manual') {
            if(levelTime < parent.getDatetimeFromTimeInMillis(atomicState.startTime,app.id)) clearKey = true
        }
        if(levelAppId != app.id){
            if(levelTime + parent.CONSTDayInMilli() < now()) clearKey = true    // If not this schedule but not today, prune it
        }
    }
    clearKey = true
    if(clearKey) parent.clearTableKey('schedule',singleDeviceId,type,app.id)
}

// Only thing really neede is:
// 1) If schedule is not running, remove itself from Table
// 2) Run Daily Start Schedule and let it deal with restarting a schedule
def systemBootActivate(){
    initialize()
}

// type expects 'brightness', 'temp', 'hue', 'sat'
// With Hue, checks reverse
def getIncrementalLevelSingle(singleDevice,type){
    if(!singleDevice) return
    if(!type) return
    if(!atomicState.startTime) return
    if(!atomicState.stopTime) return
    if(!atomicState.totalTime) return
    if(!settings['start_' + type]) return
    if(!settings['stop_' + type]) return

    // need to check if time was before schedule started
    //if(parent.getAppIdForDeviceFromTable(singleDevice,type,app.id) != app.id) return

    elapsedMillis = parent.getTimeOfDayInMillis(now(),app.id) - atomicState.startTime
    percentComplete = Math.round((elapsedMillis / atomicState.totalTime) * 1000) / 1000
    forward = false
    if(settings['start_' + type] < settings['stop_' + type]) forward = true
    
    if(type == 'hue' && settings['hueDirection'] == 'reverse') forward = false
    if(type == 'hue' && settings['hueDirection'] == 'forward') forward = true

    //forward or reverse, except hue going past 360
    totalRange = Math.abs(settings['start_' + type] - settings['stop_' + type])
    
    if(type == 'hue'){
        if(settings['hueDirection'] == 'reverse' && settings['start_' + type] > settings['stop_' + type]) totalRange = (360 - settings['stop_' + type]) + settings['start_' + type]
        if(settings['hueDirection'] == 'forward' && settings['start_' + type] > settings['stop_' + type]) totalRange = (360 - settings['start_' + type]) + settings['stop_' + type]
        if(settings['hueDirection'] == 'reverse' && settings['start_' + type] < settings['stop_' + type]) totalRange = (360 - settings['start_' + type]) + settings['stop_' + type]
    }
    
    if(forward) resultLevel = Math.round(totalRange * percentComplete + settings['start_' + type])
    if(!forward) resultLevel = Math.round(settings['start_' + type] - totalRange * percentComplete)
    if(type == 'hue'){
        if(resultLevel < 0) resultLevel = 360 + resultLevel
        if(resultLevel > 360) resultLevel = resultLevel - 360
    }
    
    return resultLevel
}

// Called from setScheduleTime, so don't use atomicState.startTime or stoopTime
def checkIfIncremental(){
    if(!settings['start_timeType']) return
    if(!settings['stop_timeType']) return
    if(settings['start_brightness'] && settings['stop_brightness'] && settings['start_brightness'] > 0  && settings['stop_brightness'] > 0) return true
    if(settings['start_temp'] && settings['stop_temp'] && settings['start_temp'] > 0 && settings['stop_temp'] > 0) return true
    if(settings['start_hue'] && settings['stop_hue'] && settings['start_hue'] > 0 && settings['stop_hue'] > 0) return true
    if(settings['start_sat'] && settings['stop_sat'] && settings['start_sat'] > 0 && settings['stop_sat'] > 0) return true
    return false
}

def setControlDeviceId(){
    parent.clearScheduleDevicesValue(app.id)
    if(!settings['stop_timeType'] || settings['stop_timeType'] == 'none') return
    // If no light settings
    if((!settings['start_brightness'] || settings['start_brightness'] == 0) || (!settings['stop_brightness'] || settings['stop_brightness'] == 0) && (!settings['start_temp'] || settings['start_temp'] == 0) || (!settings['stop_temp'] || settings['stop_temp'] == 0) && (!settings['start_hue'] || settings['start_hue'] == 0) || (!settings['stop_hue'] || settings['stop_hue'] == 0) && (!settings['start_sat'] || settings['start_sat'] == 0) || (!settings['stop_sat'] || settings['stop_sat'] == 0)) return
    
    settings['controlDevice'].each{singleDevice->
        parent.addScheduleDevices(singleDevice.id, app.id)
    }
}

def getLevels(singleDeviceId){
    if(!state.controlDeviceId.contains(singleDeviceId)) return
    if(!getEnvironmentActive) return
    // check days
    
    //currentTime is not set
    if(state.startTime > currentTime){
        if(state.stopTime < currentTime) return
        if(state.startTime < state.stopTime) return
    } else {
        if(state.startTime > state.stopTime) return
    }
    if(!settings['start_brightness']) brightnessMap = getLevelMap('brightness',settings['stop_brightness'])
    if(!settings['start_temp']) tempMap = getLevelMap('temp',settings['stop_temp'])
    if(!settings['start_hue']) hueMap = getLevelMap('hue',settings['stop_hue'])
    if(!settings['start_sat']) satMap = getLevelMap('sat',settings['stop_sat'])
    
    return parent.addMaps(brightnessMap, tempMap, hueMap, satMap,'',app.id)
}

def getEnvironmentActive(){
    if(settings['ifMode'] && location.mode != settings['ifMode']) return
    
    if(!parent.checkPeopleHome(settings['personHome'],app.id)) return
    if(!parent.checkNoPeopleHome(settings['personNotHome'],app.id)) return
    return true
}

def getCurrentlyActive(){
    if(!atomicState.stopTime) return        // No stop time

    nowTime = parent.getTimeOfDayInMillis(now(),app.id)
    startTime = atomicState.startTime
    stopTime = atomicState.stopTime

    if(startTime < nowTime){
        if(stopTime < nowTime && startTime < stopTime) return       // Not at this time
    }
    if(startTime > nowTime){
        if(stopTime < nowTime) return       // Not at this time
        if(startTime < stopTime) return       // Not at this time
    }

    if(!checkIncludeDates()) return
    if(!getEnvironmentActive()) return
    return true
}

// If incremenatal, sets persistent variables for:
// startTime = time of day (no date) for start
// stopTime = time of day (no date) for stop
// stopDateTime = date and time for stop
// totalTime = duration of schedule in milliseconds
// baseFrequency = incermental schedule frequency in milliseconds
def setScheduleTime(){
    atomicState.remove('startTime')
    atomicState.remove('stopTime')
    atomicState.remove('stopDateTime')
    atomicState.remove('totalTime')
    atomicState.remove('baseFrequency')
    
    startDateTime = getBaseStartStopDateTime('start')
    stopDateTime = getBaseStartStopDateTime('stop')
    
    if(!startDateTime) return
    
    startTime = parent.getTimeOfDayInMillis(startDateTime,app.id)
    atomicState.startTime = startTime

    if(stopDateTime){
        stopTime = parent.getTimeOfDayInMillis(stopDateTime,app.id)
        if(stopTime == 0) stopTime += 1                         // If midnight, don't have zero to prevent false null checks.
        if(startTime == stopTime) stopTime += 1 // Not sure this is actually neccesary
        atomicState.stopDateTime = stopDateTime
        atomicState.stopTime = stopTime
    }
    
    if(!checkIfIncremental()) return
    
    totalTime = stopTime - startTime
    if(totalTime < 1) totalTime += parent.CONSTDayInMilli()
    baseFrequency = Math.round(totalTime / parent.CONSTScheduleMaximumIncrements())
    if(baseFrequency < parent.CONSTScheduleMinimumActiveFrequencyMilli()) baseFrequency = parent.CONSTScheduleMinimumActiveFrequencyMilli()
    
    atomicState.totalTime = totalTime
    atomicState.baseFrequency = baseFrequency
}

// Called from parent.scheduleChildEvent
def setScheduleFromParent(timeMillis,scheduleFunction,scheduleParameters = null){
    runInMillis(timeMillis,scheduleFunction,scheduleParameters)
}

/* ************************************************************************ */
/*                                                                          */
/*                         Begin common functions.                          */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                         Begin common functions.                          */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                         Begin common functions.                          */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                         Begin common functions.                          */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                         Begin common functions.                          */
/*                                                                          */
/* ************************************************************************ */
// Copy/paste between all child apps

// Not used by schedule or sensor (yet)
// Needs to be moved back into non-shared
def buildActionMap(thisType){
    if(thisType == 'schedule') return
    if(thisType == 'sensor') return

    return [['action':'on','actionText':'turn on','descriptionActive':'Turns on', 'description': 'Turn on','type':'on', 'defaultButton':1,'advanced':false],
        ['action':'off', 'actionText':'turn off','descriptionActive':'Turns off', 'description': 'Turn off', 'type':'on', 'defaultButton':5, 'advanced':false],
        ['action':'brighten', 'actionText':'brighten','descriptionActive':'Brightens', 'description': 'Brighten', 'type':'dim', 'defaultButton':2, 'advanced':false],
        ['action':'dim', 'actionText':'dim','descriptionActive':'Dims', 'description': 'Dim', 'type':'dim', 'defaultButton':4, 'advanced':false],
        ['action':'toggle', 'actionText':'toggle','descriptionActive':'Toggles', 'description': 'Toggle', 'type':'other', 'defaultButton':3, 'advanced':false],
        ['action':'resume', 'actionText':'resume schedule','descriptionActive':'Resumes schedule (if none, turn off)', 'description': 'Resume schedule (if none, turn off)', 'type':'other', 'advanced':true],
        ['action':'setColor', 'actionText':'set color','descriptionActive':'Sets color', 'description': 'Set color', 'type':'other', 'advanced':true],
        ['action':'cycleColor', 'actionText':'cycle through colors','descriptionActive':'Cycles through colors', 'description': 'Cycle through colors', 'type':'other', 'advanced':true]]
}
                                     
def setUILinks(){
    infoIcon = '<img src="http://emily-john.love/icons/information.png" width=20 height=20>'
    errorIcon = '<img src="http://emily-john.love/icons/error.png" width=20 height=20>'
    warningIcon = '<img src="http://emily-john.love/icons/warning.png" width=20 height=20>'
    filterYesIcon = '<img src="http://emily-john.love/icons/filterEnable.png" width=20 height=20>'
    filterNoIcon = '<img src="http://emily-john.love/icons/filterDisable.png" width=20 height=20>'
    filterLightIcon = '<img src="http://emily-john.love/icons/light.png" width=20 height=20>'
    filterColorIcon = '<img src="http://emily-john.love/icons/color.png" width=20 height=20>'
    filterSwitchIcon = '<img src="http://emily-john.love/icons/switch.png" width=20 height=20>'
    filterMergeIcon = '<img src="http://emily-john.love/icons/merge.png" width=20 height=20>'
    filterNoMergeIcon = '<img src="http://emily-john.love/icons/noMerge.png" width=20 height=20>'
    moreOptions = ' (click for more options)'
    expandText = ' (Click to expand/collapse)'
}

def displayLabel(text, width = 12){
    if(!text) return
    paragraph('<div style="background-color:#DCDCDC"><b>' + text + '</b></div>',width:width)
}


def displayInfo(text,noDisplayIcon = null, width=12){
    if(!text) return
    //tooltipName = 'tooltip' + tooltipNumber
    //tooltipComputedValueName = 'tooltipValue' + tooltipNumber
    //paragraph('<div onclick="var ' + tooltipName + ' = document.getElementById(\'' + tooltipName + '\'); var ' + tooltipComputedValueName + ' = window.getComputedStyle(' + tooltipName + '); if (' + tooltipComputedValueName + '.display === \'block\') { ' + tooltipName + '.style.display = \'none\'; } else { ' + tooltipName + '.style.display = \'block\'; ' + tooltipName + '.innerText = \'' + text + '\'; }">' + infoIcon + '</div><div id="' + tooltipName + '" style="display: none; background-color:AliceBlue"></div>', width:width)

    //tooltipNumber++
    if(noDisplayIcon) paragraph('<div style="background-color:AliceBlue">' + text + '</div>',width:width)
    if(!noDisplayIcon) paragraph('<div style="background-color:AliceBlue">' + infoIcon + ' ' + text + '</div>',width:width)
    text = ''
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
                                     
def displayNameOption(){
    displayNameOptionComplete()
    displayNameOptionIncomplete()
}
def displayNameOptionComplete(){
    if(!app.label) return
    displayLabel(thisDescription.capitalize() + ' name:',2)
    label title: '', required: false, width: 10,submitOnChange:true
}
def displayNameOptionIncomplete(){
    if(app.label) return
    fieldTitle = 'Set name for this ' + thisDescription + ' setup:'
    displayLabel(highlightText(fieldTitle))
    label title: '', width:12, submitOnChange:true
    displayInfo('Name this ' + thisDescription + ' setup. Each ' + thisDescription + ' setup must have a unique name.')
}

//So many custom options per app.... Either pass params, or move to non-shared
def displayControllerOption(){
    if(thisType == 'schedule') return
    if(thisType == 'sensor' && !sensorMapEntry) return
    if(!app.label) return
    fieldName = 'controllerDevice'
    resetControllerDevices(fieldName)
    fieldOptions = ''
    if(state['controllerButtonValue'] == null) state['controllerButtonValue'] = true
    if(state['controllerButtonValue']){
        fieldOptions = controllerDeviceOptions
        if(parent.getDeviceList(app.id) && !fieldOptions) return
    }
    if(fieldOptions) {
        fieldName += 'Id'
        newValue = []
        if(fieldOptions.size() == 1) {
            app.updateSetting(fieldName, [type: 'enum', value: fieldOptions.keySet()])
            return
        }
    }
    
    displayControllerOptionComplete(fieldName,fieldOptions)
    displayControllerOptionIncomplete(fieldName,fieldOptions)
}
def displayControllerOptionComplete(fieldName,fieldOptions){
    if(!settings[fieldName]) return
    if(thisType == 'cube') fieldTitle = 'MagicCube'
    if(thisType == 'pico') fieldTitle = 'Pico controller'
    if(thisType == 'sensor')fieldTitle = 'Sensor'
    if(settings[fieldName].size() > 1) fieldTitle += 's:'
    capability = 'capability.pushableButton'
    if(thisType == 'sensor') capability = 'capability.' + sensorMapEntry.'capability'
    if(fieldOptions) displaySelectField(fieldName, fieldTitle, fieldOptions, true, true, 'controllerButton')
    if(!fieldOptions) displayDeviceSelectField(fieldName, fieldTitle, capability, true, 'controllerButton')
}
def displayControllerOptionIncomplete(fieldName,fieldOptions){
    if(settings[fieldName]) return
    if(thisType == 'pico') displayInfo('Select which Lutron Caseta(s) and/or Pico(s) to control. You can select multiple devices, but all should have the same number of buttons.')
    if(fieldOptions) displayInfo('If you don\'t see the device you want, make sure you have it selected in the Master app.')
    if(thisType == 'pico') fieldTitle = 'Select button device(s) to setup:'
    if(thisType == 'cube') fieldTitle = 'Select MagicCube(s) to setup:'
    if(thisType == 'sensor') fieldTitle = 'Select sensor(s) to setup:'
    capability = 'capability.pushableButton'
    if(thisType == 'sensor') capability = 'capability.' + sensorMapEntry.'capability'
    if(fieldOptions) displaySelectField(fieldName, fieldTitle, fieldOptions, true, true, 'controllerButton')
    if(!fieldOptions) displayDeviceSelectField(fieldName, fieldTitle, capability, true, 'controllerButton')
}
def controllerOptionProcessParentDeviceList(capability){
    if(!allDeviceOptions) return
    if(!capability) return
    controllerList = [:]
    //fullList = [:]
    allDeviceOptions.each{singleDevice->
        if(singleDevice.hasCapability(capability.capitalize())){
            controllerMatch = controllerOptionProcessParentDeviceListMatch(singleDevice)
            if(controllerMatch) controllerList.put([singleDevice.'id',getDeviceName(singleDevice)])
            //if(!controllerMatch) fullList.put([singleDevice.'id',getDeviceName(singleDevice)])
        }
    }
    if(settings['controllerDeviceId']){
        settings['controllerDeviceId'].each{controllerDeviceIdValue->
            if(!controllerList.containsKey(controllerDeviceIdValue)){
                controllerList.put([controllerDeviceIdValue,'test'])
               }
               }
    }
    if(!controllerList) return
    return controllerList.sort{it.value.toLowerCase()}
}

def displayAdvancedOption(){
    if((thisType == 'pico' || thisType == 'magicCube') && !settings['controllerDevice']) return
    if(thisType != 'sensor' && !settings['controlDevice']) return
    if(anyErrors) return        // Only pico or cube
    fieldName = 'advancedSetup'
    if((thisType != 'schedule' && thisType != 'sensor') && !controllerDevice && !settings[fieldName]) return        // schedule doesn't have controllerDevice, and sensor needs advanced before controllerDevice
    if(!settings['customActionsSetup'] && !settings[fieldName] && (thisType == 'pico' || thisType == 'cube')) return
    if(thisType != 'sensor' && !settings['controlDevice']) return
    if(thisType == 'schedule'){
        if(!validateTimes('start')) return
        if(!validateTimes('stop')) return
        if(!settings['start_action']) return
        if(!settings['stop_action']) return
    }
    
    displayAdvancedOptionEnabled(fieldName)
    displayAdvancedOptionDisabled(fieldName)
}
def displayAdvancedOptionEnabled(fieldName){
    if(!settings[fieldName]) return
    fieldTitleTrue = 'Showing advanced setup.'
    displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse, false)
}
def displayAdvancedOptionDisabled(fieldName){
    if(settings[fieldName]) return
    fieldTitleTrue = 'Keeping it simple.'
    fieldTitleFalse = 'Click to show advanced setup.'
    displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse, false)
}

def displayControlDeviceOption(){
    if(thisType == 'sensor' && !settings['controllerType']) return
    if(thisType != 'schedule' && !controllerDevice) return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(anyErrors) return
    fieldName = 'controlDevice'
    displayControlDeviceOptionComplete(fieldName)
    displayControlDeviceOptionIncomplete(fieldName)
    if(thisType == 'sensor' && sensorMapEntry.'type' == 'bool') displayInfo('Note the sensor routine starts with "' + sensorMapEntry.'start' + '" and stops with "' + sensorMapEntry.'stop' + '". It will only run if the prior instance has stopped.')
}
def displayControlDeviceOptionComplete(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = 'Device to control:'
    if(settings[fieldName].size() > 1) fieldTitle = 'Devices to control:'
    capabilitiesType = 'switch'
    if(state['controlButtonValue'] == 1) capabilitiesType = 'switchLevel'
    if(state['controlButtonValue'] == 2) capabilitiesType = 'colorMode'

    displayDeviceSelectField(fieldName,fieldTitle,'capability.' + capabilitiesType,true, 'controlButton')
}
def displayControlDeviceOptionIncomplete(fieldName){
    if(settings[fieldName]) return
    fieldTitle = 'Select all device(s) to control with the ' + thisDescription + ':'
    if(thisType == 'sensor' && settings['controllerDevice'].size() == 1) fieldTitle = 'Select all device(s) to control with the sensor:'
    if(thisType == 'sensor' && settings['controllerDevice'].size() > 1) fieldTitle = 'Select all device(s) to control with the sensors:'
    capabilitiesType = 'switch'
    if(state['controlButtonValue'] == 1) capabilitiesType = 'switchLevel'
    if(state['controlButtonValue'] == 2) capabilitiesType = 'colorMode'

    displayDeviceSelectField(fieldName,fieldTitle,'capability.' + capabilitiesType,true, 'controlButton')
}

def displayIfModeOption(){
    if(!controllerDevice && thisType != 'schedule') return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if((thisType == 'pico' || thisType == 'cube') && !checkAnyDeviceSet()) return
    if(anyErrors) return
    if(thisType == 'sensor' && sensorCount == 0) return        // Need to add 'anyErrors' to sensors
    
    fieldName = 'ifMode'
    sectionTitle = 'Click to select with what Mode (Optional)'
    if(settings[fieldName]) sectionTitle = '<b>Only with Mode: ' + settings[fieldName] + '</b>'

    section(hideable: true, hidden: true, sectionTitle){
        displayIfModeFieldComplete(fieldName)
        displayIfModeFieldIncomplete(fieldName)
    }
}
def displayIfModeFieldComplete(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = 'Only with Mode:'
    displayModeSelectField(fieldName,fieldTitle,options,true,false)
}
def displayIfModeFieldIncomplete(fieldName){
    if(settings[fieldName]) return
    displayInfo('This will limit the ' + thisDescription + ' from being active to only when Hubitat\'s Mode is as selected. You can create another ' + thisDescription + ' "app" to do something else for other Modes.')
    fieldTitle = 'Only when the Mode is:'
    displayModeSelectField(fieldName,fieldTitle,options,true,false)
}

def displayPeopleOption(){
// Use devices selected in Master app
// Add check for if no presense devices
    if(!controllerDevice && thisType != 'schedule') return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if((thisType == 'pico' || thisType == 'cube') && !checkAnyDeviceSet()) return
    if(anyErrors) return
    if(thisType == 'sensor' && sensorCount == 0) return        // Need to add 'anyErrors' to sensors

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
    
    if(!settings['personHome'] && !settings['personNotHome']) sectionTitle = 'Click to select with people (Optional)'
    if(settings['personHome']) sectionTitle = '<b>Only if home: ' + withPeople + '</b>'
    if(settings['personHome'] && settings['personNotHome']) sectionTitle += '<br>'
    if(settings['personNotHome']) sectionTitle += '<b>Only if away: ' + withoutPeople + '</b>'

    section(hideable: true, hidden: hidden, sectionTitle){
        if(peopleError) displayError('You can\'t include and exclude the same person.')
        fieldName = 'personHome'
        displayPersonHomeComplete(fieldName, 'capability.presenceSensor')
        displayPersonHomeIncomplete(fieldName, 'capability.presenceSensor')
        fieldName = 'personNotHome'
        displayPersonNotHomeComplete(fieldName, 'capability.presenceSensor')
        displayPersonNotHomeIncomplete(fieldName, 'capability.presenceSensor')
        if(thisType == 'schedule') displayInfo('Schedules will resume/pause based on people present/away. If requirements are met at start time, any stop actions/levels will be applied.')
    }
}
def displayPersonHomeComplete(fieldName, fieldCapability){
    if(!settings[fieldName]) return
    fieldTitle = 'Only if home:'
    displayDeviceSelectField(fieldName,fieldTitle,fieldCapability,true)
}
def displayPersonHomeIncomplete(fieldName, fieldCapability){
    if(settings[fieldName]) return
    if(!settings['personNotHome']) displayInfo('This will limit the ' + thisDescription + ' from being active to only when those selected are home and/or away. They can be combined (as if Person A is home AND Person B is away). You can create another ' + thisDescription + ' "app" to do something else for the opposite.')
    fieldTitle = 'Only if any of these people are home (Optional)'
    displayDeviceSelectField(fieldName,fieldTitle,fieldCapability,true)
}
def displayPersonNotHomeComplete(fieldName, fieldCapability){
    if(!settings[fieldName]) return
    fieldTitle = 'Only if away:'
    displayDeviceSelectField(fieldName,fieldTitle,fieldCapability,true)
}
def displayPersonNotHomeIncomplete(fieldName, fieldCapability){
    if(settings[fieldName]) return
    fieldTitle = 'Only if any of these people are not home (Optional)'
    displayDeviceSelectField(fieldName,fieldTitle,fieldCapability,true)
}

def validateTimes(type){
    if(type == 'stop' && settings['start_timeType'] && !settings['stop_timeType']) return false
    if(thisType == 'schedule' && type == 'stop' && settings['stop_timeType'] == 'none') return true
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

def getTimeSectionTitle(){
    sectionTitle = ''
    if(!settings['start_timeType'] && !settings['stop_timeType']) return 'Click to set with schedule (Optional)'
    if(settings['start_timeType']) sectionTitle = '<b>Starting: ' + getTimeSectionStartStopTitle('start') + '</b>'
    if(settings['start_timeType'] && settings['stop_timeType']) sectionTitle += '\n'
    if(settings['stop_timeType']) sectionTitle += '<b>Stopping: ' + getTimeSectionStartStopTitle('stop') + '</b>'
    if(thisType == 'schedule' && settings['start_time'] && settings['stop_time'] && settings['start_time'] == settings['stop_time']) sectionTitle = '<b>Always</b>'
    return sectionTitle
}
def getTimeSectionStartStopTitle(type){
    sectionTitle = ''
    if(type == 'stop' && settings[type + '_timeType'] == 'none') return 'No end'
    if(settings[type + '_timeType'] == 'time') sectionTitle += 'At '
    if(settings[type + '_timeType'] == 'time' && !settings[type + '_time']) sectionTitle += 'specific time'
    if(settings[type + '_timeType'] == 'time' && settings[type + '_time']) sectionTitle += Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings[type + '_time']).format('h:mm a', location.timeZone)
    if(settings[type + '_timeType'] == 'sunrise' || settings[type + '_timeType'] == 'sunset'){
        if(!settings[type + '_sunType']) sectionTitle += 'Based on ' + settings[type + '_timeType']
        if(settings[type + '_sunType'] == 'at') sectionTitle += 'At ' + settings[type + '_timeType']
        if(settings[type + '_sunType'] && settings[type + '_sunType'] != 'at' && !settings[type + '_sunOffset']) sectionTitle += settings[type + '_sunType'].capitalize() + ' ' + settings[type + '_timeType']
        if(settings[type + '_sunType'] && settings[type + '_sunType'] != 'at' && settings[type + '_sunOffset']) sectionTitle += settings[type + '_sunOffset'] + ' minutes ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType']
        if(settings[type + '_sunType'] && settings[type + '_sunType'] != 'at' && settings[type + '_sunOffset'] && validateTimes(type)) sectionTitle += ' ' + getSunriseTime(settings[type + '_timeType'],settings[type + '_sunOffset'],settings[type + '_sunType'])
    }
    return sectionTitle
}

def displayTimeTypeOption(type){
    if(type == 'stop' && (!settings['start_timeType'] || !validateTimes('start'))) return
    ingText = type
    if(type == 'stop') ingText = 'stopp'
    
    labelText = 'Schedule ' + type
    if(validateTimes('start')) labelText = ''
    if(type == 'start' && (!validateTimes('start') || !settings[type + '_timeType'])) labelText = ''
    if(!validateTimes('start') || !settings[type + '_timeType']) labelText = 'Schedule ' + ingText + 'ing time'
    
    if(labelText) displayLabel(labelText)

    if(!validateSunriseMinutes(type)) displayWarning('Time ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType'] + ' is ' + (Math.round(settings[type + '_sunOffset']) / 60) + ' hours. That\'s probably wrong.')
    
    fieldName = type + '_timeType'
    fieldTitle = type.capitalize() + ' time:'
    if(!settings[type + '_timeType']){
        fieldTitle = type.capitalize() + ' time?'
        if(type == 'stop' && thisType == 'schedule') fieldTitle += ' (Select "Don\'t stop" for none)'
        if(type == 'stop' && thisType != 'schedule') fieldTitle += ' (Required with Start time.)'
        highlightText(fieldTitle)
    }
    fieldTitle = addFieldName(fieldTitle,fieldName)
    fieldList = ['time':'Start at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
    if(type == 'stop' && thisType == 'schedule') fieldList = ['none':'Don\'t stop','time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
    if(type == 'stop' && thisType != 'schedule') fieldList = ['time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
    input fieldName, 'enum', title: fieldTitle, multiple: false, width: getTypeOptionWidth(type), options: fieldList, submitOnChange:true
    if(!settings['start_timeType']) displayInfo('Select whether to enter a specific time, or have start time based on sunrise and sunset for the Hubitat location. Required.')
}

def displayTimeOption(type){
    if(type == 'stop' && !validateTimes('start')) return
    if(type == 'stop' && settings['stop_timeType'] == 'none') return
    if(settings[type + '_timeType'] != 'time') return
    if(type == 'stop' && (!settings['start_timeType'] || !validateTimes('start'))) return

    fieldName = type + '_time'
    fieldTitle = type.capitalize() + ' time:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(!settings[fieldName]) fieldTitle = highlightText(fieldTitle)
    input fieldName, 'time', title: fieldTitle, width: getTypeOptionWidth(type), submitOnChange:true
    // Add separate alert for if noon or midnight are actually entered
    if(!settings[fieldName]) displayInfo('Enter the time to ' + type + ' the schedule in "hh:mm AM/PM" format. Midnight is "am", and noon is "pm". Required.')
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
    
    if(!settings[fieldName]) displayInfo('Select whether scheduling starts exactly at ' + settings[type + '_timeType'] + ' (currently ' + sunTime + '). To allow entering minutes prior to or after ' + settings[type + '_timeType'] + ', select "Before ' + settings[type + '_timeType'] + '" or "After ' + settings[type + '_timeType'] + '". Required.')
    displaySunriseOffsetOption(type)
}

def getSunriseTime(type,sunOffset,sunriseType){
    if(type == 'sunrise' && sunriseType == 'before' && sunOffset) return '(' + new Date(parent.getSunrise((sunOffset * -1),app.id)).format('hh:mm a') + ')'
    if(type == 'sunrise' && sunriseType == 'after' && sunOffset) return '(' + new Date(parent.getSunrise(sunOffset,app.id)).format('hh:mm a') + ')'
    if(type == 'sunset' && sunriseType == 'before' && sunOffset) return '(' + new Date(parent.getSunset((sunOffset * -1),app.id)).format('hh:mm a') + ')'
    if(type == 'sunset' && sunriseType == 'after' && sunOffset) return '(' + new Date(parent.getSunset(sunOffset)).format('hh:mm a') + ')'
    if(type == 'sunrise' && sunriseType == 'at') return '(' + new Date(parent.getSunrise(0,app.id)).format('hh:mm a') + ')'
    if(type == 'sunset' && sunriseType == 'at') return '(' + new Date(parent.getSunset(0,app.id)).format('hh:mm a') + ')'   
}

def displaySunriseOffsetOption(type){
    if(type == 'stop' && !validateTimes('start')) return
    if(type == 'stop' && settings['stop_timeType'] == 'none') return
    if(!settings[type + '_sunType']) return
    if(settings[type + '_sunType'] == 'at') return

    fieldName = type + '_sunOffset'
    timeUnits = 'minutes'
    fieldTitle = timeUnits.capitalize() + ' ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType'] + ':'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'number', title: fieldTitle, width: getTypeOptionWidth(type), submitOnChange:true

    message = 'Enter the number of ' + timeUnits + ' ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType'] + ' for schedule to ' + type + '. Required.'
    if(!settings[type + '_sunOffset']) displayInfo(message)
    if(!validateSunriseMinutes(type)) displayWarning(message)
}

def displayDaysAndDatesSection(){
    if(thisType != 'schedule' && !settings['controlDevice']) return
    if(!settings['controlDevice']) return
    if(!settings['start_timeType']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if(!settings['advancedSetup'] && !settings['days'] && !settings['includeDates'] && !settings['excludeDates']) return
    hidden = true

    defaultSectionTitle = '<b>Days/Dates:'
    if(!settings['days'] && !settings['includeDates'] && !settings['excludeDates']) sectionTitle = 'Click to set days/dates (optional)'
    List dayList=[]
    settings['days'].each{
        dayList.add(it)
    }
    dayText = dayList.join(', ')
    sectionTitle = ''
    if(settings['days']) sectionTitle += ' ' + dayText
    if(settings['includeDates']) sectionTitle += ' +[included dates]'
    if(settings['excludeDates']) sectionTitle += ' +[excluded dates]'
    if(settings['days'] || settings['includeDates'] || settings['excludeDates']) sectionTitle += '</b>'
    if((!settings['days'] || !settings['includeDates'] || !settings['excludeDates']) && (settings['days'] || settings['includeDates'] || settings['excludeDates'])) sectionTitle += moreOptions
    section(hideable: true, hidden: hidden, sectionTitle){

    displayDaysOption()
    displayIncludeDates()
    displayExcludeDates()
    }
}

def displayDaysOption(){
    if(thisType == 'schedule' && !settings['start_timeType']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return

    fieldName = 'days'
    if(!settings['advancedSetup'] && !settings[fieldName]) return
    fieldTitle = 'On these days (Optional; defaults to all days):'
    if(!settings[fieldName]) fieldTitle = 'On which days (Optional; defaults to all days)?'
    options = ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday']
    displaySelectField(fieldName,fieldTitle,options,true,false)
    //input fieldName, 'enum', title: fieldTitle, multiple: true, width: 12, options: ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday'], submitOnChange:true
}

def displayDatesOptions(){
    displayIncludeDates()
    displayExcludeDates()
}

def displayIncludeDates(){
    if(!settings['advancedSetup'] && !settings['includeDates']) return
    displayWarning(parent.getDateProcessingErrors(app.id))
    displayInfo(dateList)
    fieldName = 'includeDates'
    fieldTitle = 'Only on dates:'
    if(thisType == 'schedule') fieldTitle = 'Dates on which to run ("include"):'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, "textarea", title: fieldTitle, submitOnChange:true
}
 
// Returns true if level value is either valid or null
def validateLevel(value){
    if(!value) return true
    value = value as int
        if(value < 1) return false
        if(value > 100) return false
    return true
}

// Returns true if temp value is either valid or null
def validateTemp(value){
    if(!value) return true
        value = value as int
            if(value < 1800) return false
            if(value > 6500) return false
            return true
}


// Returns true if temp value is either valid or null
def validateHue(value){
    if(!value) return true
    value = value as int
    if(value < 1) return false

    if(value > 360) return false
    return true
}

def getMinutesError(value){
    if(!value) return
    if(value == 0) return
    if(value > 1440) return 'Maximum minutes is 1,440 (24 hours).'
    if(value < 1) return 'Minimum minutes is 1.'
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
def displaySelectField(fieldName,fieldTitle,options,multiple = false,required = true, button = '', width = 12){
    if(button) width--
    if(settings[fieldName]) width = width - 2
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
          if(!state[buttonValue + 'Value']){
              state[buttonValue + 'Value'] = true
              return
          }
              state[buttonValue + 'Value'] = false
          return
          case 'controlButton':
          if(!state[buttonValue + 'Value']){
              state[buttonValue + 'Value'] = 1
              return
          }
          if(state[buttonValue + 'Value'] == 1){
              state[buttonValue + 'Value'] = 2
              return
          }
              state.remove(buttonValue + 'Value')
          return
          case 'controllerTypeButton':        //used only by sensor app
          if(!state[buttonValue + 'Value']){
              state[buttonValue + 'Value'] = true
              return
          }
              state[buttonValue + 'Value'] = false
          return
          case 'averageButton':        //used only by sensor app
          if(!state[buttonValue + 'Value']){
              state[buttonValue + 'Value'] = true
              return
          }
              state[buttonValue + 'Value'] = false
          return
          case 'multipleOptionsButton':        //used only by sensor app
          if(!state[buttonValue + 'Value']){
              state[buttonValue + 'Value'] = true
              return
          }
              state[buttonValue + 'Value'] = false
          return
      }
}

def displayFilterButton(buttonName){
    if(buttonName == 'controllerButton'){
        if(state[buttonName + 'Value']) {
            input buttonName, 'button', title: filterYesIcon + ' Filter', width:1
        }
        if(!state[buttonName + 'Value']){
            input buttonName, 'button', title: filterNoIcon + ' Filter', width:1
        }
        return
    }
    if(buttonName == 'controlButton'){
        if(!state[buttonName + 'Value']) {
            input buttonName, 'button', title: filterSwitchIcon + ' Filter', width:1
        }
        if(state[buttonName + 'Value'] == 1){
            input buttonName, 'button', title: filterLightIcon + ' Filter', width:1
        }
        if(state[buttonName + 'Value'] == 2){
            input buttonName, 'button', title: filterColorIcon + ' Filter', width:1
        }
        return
    }
    if(buttonName == 'controllerTypeButton'){
        if(state[buttonName + 'Value']) {
            input buttonName, 'button', title: filterYesIcon + ' Filter', width:1
        }
        if(!state[buttonName + 'Value']){
            input buttonName, 'button', title: filterNoIcon + ' Filter', width:1
        }
        return
    }
    if(buttonName == 'averageButton'){
        if(state[buttonName + 'Value']) {
            input buttonName, 'button', title: filterNoMergeIcon, width:1
        }
        if(!state[buttonName + 'Value']){
            input buttonName, 'button', title: filterMergeIcon, width:1
        }
        return
    }
    if(buttonName == 'multipleOptionsButton'){
        if(state[buttonName + 'Value']) {
            input buttonName, 'button', title: filterMergeIcon, width:1
        }
        if(!state[buttonName + 'Value']){
            input buttonName, 'button', title: filterNoMergeIcon, width:1
        }
        return
    }
}
//Used in UI tooltip example
def getNextYearWithMondayChristmas(currentYear = null) {
    if(!currentYear) currentYear = new Date().format('yyyy').toInteger() - 1
    mondayChristmas = false
    while (!mondayChristmas) {
        currentYear++
        christmas = Date.parse('yyyyMMdd',currentYear + '1225')
        if (christmas.format('EEEE') == 'Monday') mondayChristmas = true
        
    }
    return currentYear
}

// Returns 'start' or 'stop' time (of day) in millis
// Must be converted with getDatetimeFromTimeInMillis if compared to now()
def getBaseStartStopDateTime(type){
    if(type == 'stop' && !settings['stop_timeType']) return
    if(type == 'stop' && settings['stop_timeType'] == 'none') return
    if(type == 'start' && !settings['start_timeType']) return
    if(settings[type + '_timeType'] == 'time') {
        if(!settings[type + '_time']) return
        return timeToday(settings[type + '_time']).getTime()
    }
    if(!settings[type + '_sunType']) return

    if(settings[type + '_timeType'] == 'sunrise') {
        if(settings[type + '_sunType'] == 'before') returnValue = parent.getSunrise(settings[type + '_sunOffset'] * -1,app.id)
        if(settings[type + '_sunType'] == 'after') returnValue = parent.getSunrise(settings[type + '_sunOffset'],app.id)
    }
    if(settings[type + '_timeType'] == 'sunset') {
        if(settings[type + '_sunType'] == 'before') returnValue = parent.getSunset((settings[type + '_sunOffset'] * -1),app.id)
        if(settings[type + '_sunType'] == 'after') returnValue = parent.getSunset(settings[type + '_sunOffset'],app.id)
    }
    return returnValue
}
// Date map is [yyyy:[ddd,ddd,ddd......]]
def checkIncludeDates(){
    if(!atomicState?.includeDates) return true
    currentYear = new Date().format('yyyy').toInteger()
    
    if(!atomicState.'includeDates'?.currentYear) processDates()        // If a new year
    
    if(atomicState.includeDates.(currentYear.toInteger()).contains(new Date(now()).format('D').toInteger())) return true
}
def processDates(){
    atomicState.remove('includeDates')
    if(!settings['days'] && !settings['includeDates'] && !settings['excludeDates']) return
    currentYear = new Date().format('yyyy').toInteger()
    includeDatesValue = settings['includeDates']
    if(!settings['includeDates'] && (settings['days'] || settings['excludeDates'])) includeDatesValue = '1/1-12/31'
    atomicState.'includeDates' = [(currentYear):parent.processDates(settings['includeDates'], settings['excludeDates'], settings['days'], true, app.id)]
}

// Not used with scheduler app (except in UI)
// If time, sets persistent variables for:
// startTime = time of day (no date) for start
// stopTime = time of day (no date) for stop
// stopDateTime = date and time for stop
def setTime(){
    atomicState.remove('startTime')
    atomicState.remove('stopTime')
    atomicState.remove('stopDateTime')
    
    if(!settings['start_timeType']) return
    if(!settings['stop_timeType']) return
    startTime = parent.getTimeOfDayInMillis(getBaseStartStopDateTime('start'), app.id)
    if(!startTime) {
        putLog(1752,'error','Schedule error with starting time.')
        return
    }

    stopDateTime = getBaseStartStopDateTime('stop')
    stopTime = parent.getTimeOfDayInMillis(stopDateTime, app.id)
    if(stopTime == 0) stopTime += 1                         // If midnight, don't have zero to prevent false null checks.
    atomicState.startTime = startTime
    if(!stopTime) return
    if(startTime == stopTime) stopTime += 1 // Not sure this is actually neccesary

    atomicState.stopTime = stopTime
    atomicState.stopDateTime = stopDateTime
}

def getDeviceName(singleDevice){
    if(!singleDevice) return
    if(singleDevice.label) return singleDevice.label
    return singleDevice.name
}

def displayExcludeDates(){
    fieldName = 'excludeDates'
    if(!settings['advancedSetup'] && !settings[fieldName]) return
    fieldTitle = 'Not on dates:'
    if(appType == 'schedule') fieldTitle = 'Dates on which to <u>not</u> run ("exclude"):'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, "textarea", title: fieldTitle, submitOnChange:true
    deviceText = 'the device'
    if(thisType == 'schedule' && settings['controlDevice'].size() > 1) deviceText = 'the devices'
    if(thisType != 'schedule' && settings['controlDevice'].size() > 1) deviceText = 'the devices'
    currentYear = new Date(now()).format('yyyy').toInteger()
    christmasMondayYear = getNextYearWithMondayChristmas()
    infoTip = 'Enter which date(s) to restrict or exclude this ' + thisDescription + ' routine. "Only on dates" are when this ' + thisDescription + ' will work, for instance if you want ' + deviceText + ' to do a specific thing on Christmas. \
"Not on" dates are when this ' + thisDescription + ' will not apply, for instance to set ' + deviceText + ' to do something any other day. Rules:\n\
	• Year is optional, but would only apply to that <i>one day</i>. If no year is entered, it will repeat annually. \
<i>Example: "12/25/' + (new Date(now()).format('yyyy').toInteger() - 1) + '" will never occur in the future, because that\'s how time works.</i>\n\
	• Use month/day ("mm/dd") format, or day.month ("dd.mm"). You can also use Julian days of the year as a 3-digit number ("ddd"). \
<i>Example: Christmas could be entered as "12/25", "25.12" or "359" (the latter only true for non-leap years, otherwise "360").</i>\n\
	• Separate multiple dates with a comma (or semicolon). \
<i>Example: "12/25, 1/1" is Christmas and New Year\'s Day.</i>\n\
	• Use a hyphen to indicate a range of dates. \
<i>Example: "12/25-1/6" are the 12 days of Christmas.</i>\n\
    	• The "days" options above will combine with the dates. \
<i>Example: Selecting Monday and entering "12/25" as an "only on" date would only allow the ' + thisDescription + ' to activate on 12/25/' + christmasMondayYear + ', 12/25/' + getNextYearWithMondayChristmas((christmasMondayYear + 1)) + ', etc. when Christmas is on a Monday.</i>\n\
	• You can mix and match formats (even tho you probably shouldn\'t), and individual dates with ranges. And the order doesn\'t matter. \
<i>Example: "001, 31.10, 12/25/' + (new Date(now()).format('yy').toInteger()) + '-12/31/' + (new Date(now()).format('yyyy').toInteger()) + '" is every Halloween, Christmas to New Years\' Eve of ' + (new Date(now()).format('yyyy').toInteger()) + ', and every New Years\' Day.</i>\n\
	• If a date falls within both "only on" and "not on", it will be treated as "not on".\n\
	• If any date within a date range is invalid, the entire date range will be ignored. <i>Example: 02/01-02/29 would only be used on a Leap Year (to do all of February including 2/29, enter "2/1-2/28, 2/29").</i>'

    displayInfo(infoTip)
    
}
//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,type = 'trace',message = null){
    appId = app.id

    return parent.putLog(lineNumber,type,message,app.id,'True')
}
