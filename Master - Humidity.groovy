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
*  Name: Master - Sensor
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master%20-%20Sensor.groovy
*  Version: 0.4.3.0
*
***********************************************************************************************************************/

// Need to add lux, maybe energy/power
// And acceleration, wet/dry, motion, smoke, CO2? Or build separate app for binary states?

// Need to add allowing control of brightness/color/CT, and have time offsets for on/off
// Add state change option, with time delays, ala Contact

definition(
    name: "Master - Sensor",
    namespace: "master",
    author: "roguetech",
    description: "Sensors",
    parent: "master:Master",
    category: "Convenience",
    importUrl: "https://raw.githubusercontent.com/roguetech2/hubitat/master/Master%20-%20Sensor.groovy",
    iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@2x.png"
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

def highlightText(text){
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
            
            section(){
                sensorCount = getSensorCount()
                sensorAverage = getSensorAverage() 
                deviceCount = getDeviceCount()
                sensorPlural = getPluralSensor()
                devicePlural = getPluralDevice()
                unitType = getTypeUnit()
                unitText = getUnitText()
                deviceListMap = getDeviceListMap()
                if(deviceListMap?.(settings['sensorType'])) sensorFullName = deviceListMap[settings['sensorType']].toLowerCase() as String
                forwardDirection = getDirectionForward()    // over or under
                reverseDirection = getDirectionReverse()
                forwardDirection2 = getDirectionForward2()    // increase or decrease
                reverseDirection2 = getDirectionReverse2()

                displayNameOption()
                if(install) displayDisableOption()
                displayAdvancedOption()
                displaySensorTypeOption()
                displaySensorDeviceOption()
                displayDevicesTypesOption()
                displayDeviceOption()
            }
            displayThresholdOption()
            displayLevelDeltaOption()
            displayRunTimeOption()
            displayScheduleSection()
            displayPeopleOption()
            displayIfModeOption()
            section(){}
            displayActionOption()
            setLightOptions()
            displayChangeModeOption()
            displayAlertOptions()
            //displayControlDeviceOption()
        }
    }
}

def formComplete(){
    if(!app.label) return false
    if(!settings['sensorType']) return false
    if(!settings['sensor']) return false
    if(!settings['deviceType']) return false
    if(!settings['device']) return false

    return true
}

def displayNameOption(){
    if(app.label){
        displayLabel('Sensor app name',2)
        label title: '', required: false, width: 10,submitOnChange:true
    } else {
        displayLabel('Set name for this sensor app')
        label title: '', required: false, submitOnChange:true
        displayInfo('Name this sensor app. Each sensor app must have a unique name.')
    }
}

def displayDisableOption(){
    if(!install) return

    fieldName = 'disable'
    fieldTitle = '<b><font color="#000099">This ' + infoText + ' sensor is disabled.</font></b> Reenable it?'
    fieldTitle = 'This ' + infoText + ' sensor is enabled. Disable it?'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'bool', title: fieldTitle, submitOnChange:true
}

def displayAdvancedOption(){
// Only needs to show after selecting basics
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
    if(!settings['device']) return
    
    fieldName = 'advanced'
    fieldTitle = 'Display advanced options'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input(fieldName, 'bool', title: fieldTitle, submitOnChange:true)
}

def displaySensorTypeOption(){
    width = 10
    fieldName = 'sensorType'
    fieldTitle = 'Select type of ' + sensorPlural + ' to use:'
    if(settings[fieldName]) fieldTitle =  sensorPlural.capitalize() + ' type:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(!settings[fieldName]) {
        displayLabel(highlightText(fieldTitle))
        width = 12
    }
    input fieldName, 'enum', title: '', options: deviceListMap, multiple: false, width:width,submitOnChange:true
}

def displaySensorDeviceOption(){
    if(!settings['sensorType']) return
    if(settings['sensor'] && sensorCount == 0){
        String sensorName = settings['sensor'].label
        if(settings['sensor'] && sensorCount == 0) displayWarning(sensorName + ' does not have any ' + sensorFullName + ' reading. It is either inactive or misreports being a ' + sensorFullName + ' sensor.')
    }

    fieldName = 'sensor'
    fieldTitle = 'Select type of ' + sensorPlural + ' being used:'
    if(settings[fieldName]) fieldTitle = sensorPlural.capitalize() + ' to use:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings['sensorType'] == 'temperature') capability = 'capability.temperatureMeasurement'
    if(settings['sensorType'] == 'humidity') capability = 'capability.relativeHumidityMeasurement'
    if(settings['sensorType'] == 'illuminance') capability = 'capability.illuminanceMeasurement'
    if(settings['sensorType'] == 'energy') capability = 'capability.energyMeter'
    if(settings['sensorType'] == 'power') capability = 'capability.powerMeter'
    if(settings['sensorType'] == 'carbonDioxide') capability = 'capability.carbonDioxideMeasurement'
    if(!capability) return

        input fieldName, capability, title: fieldTitle, multiple: true, submitOnChange:true

    //helpTip = 'Select which ' + getDeviceTypeMap()[settings['sensorType']] + ' sensor(s) to use. Required.'
    if(sensorCount > 0) helpTip = sensorFullName.capitalize() + ' ' + sensorPlural + ' is currently at ' + sensorAverage + typeUnit + '.'
    if(settings['sensor'] && sensorCount > 1) helpTip += ' '
    if(sensorCount > 1) helpTip += 'Multiple sensors are averaged together.'
    displayInfo(helpTip)
}

def displayDevicesTypesOption(){
    if(!settings['sensorType']) return
    if(!settings['sensor']) return

    width = 10
    fieldName = 'deviceType'
    fieldTitle = 'Select type of ' + devicePlural + ' to control:'
    if(settings[fieldName]) fieldTitle =  devicePlural.capitalize() + ' type:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(!settings[fieldName]) {
        displayLabel(highlightText(fieldTitle))
        width = 12
    }
    input fieldName, 'enum', title: '', options: ['switch': 'All switches','light': 'Lights','color':'Color lights','lock': 'Lock(s)', 'fan': 'Fan(s)'], multiple: false, required: false, width:width, submitOnChange:true
}

def displayDeviceOption(){
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return

    fieldName = 'device'
    fieldTitle = devicePlural.capitalize() + ' being controlled:'
    if(!settings[fieldName]) fieldTitle = 'Select ' + devicePlural + ' to control:'
    fieldTitle = addFieldName(fieldTitle,fieldName)

    if(!settings['deviceType']) return
    if(settings['deviceType'] == 'switch') capability = 'capability.switch'
    if(settings['deviceType'] == 'light') capability = 'capability.switchLevel'
    if(settings['deviceType'] == 'color') capability = 'capability.colorControl'
    if(settings['deviceType'] == 'lock') capability = 'capability.lock'
    if(settings['deviceType'] == 'fan') capability = 'capability.fanControl'
    if(!capability) return
    if(!settings[fieldName]) paragraph(highlightText('Select which ' + pluralTitle + ' to control by ' + infoText + '. Required.'))
    input fieldName, capability, title: fieldTitle, multiple: true, submitOnChange:true
}

def displayActionOption(){
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
    if(!settings['device']) return

    hidden = true
    sectionTitle = 'Click to set start/stop action(s)</b> (Optional)'
    if(settings['startAction']) {
        sectionTitle = '<b>On start: ' + getPlainAction(settings['startAction']).capitalize() + '</b>'
        if(settings['startWait'] && settings['startWait'] > 0)  sectionTitle += settings['startWait'] + ' minutes after start: ' +  getPlainAction(settings['startAction']).capitalize() + '</b>'
    }
    if(settings['startAction'] && settings['stopAction']) sectionTitle += '<br>'
    if(settings['stopAction']) {
        sectionTitle += '<b>On stop: ' +  getPlainAction(settings['stopAction']).capitalize() + '</b>'
        if(settings['stopWait'] && settings['stopWait'] > 0)  sectionTitle += settings['stopWait'] + ' minutes after stop: ' +  getPlainAction(settings['stopAction']).capitalize() + '</b>'
    }
    if(settings['startAction'] && !settings['stopAction']) sectionTitle += moreOptions
    if(!settings['startAction'] && settings['stopAction']) sectionTitle += moreOptions
    section(hideable: true, hidden: hidden, sectionTitle){
        displayActionFields('start')
        displayActionFields('stop')
    }

}
def displayActionFields(type){
    width = 10
    fieldName = type + 'Action'
    fieldTitle = type.capitalize() + ' action:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    displayLabel(fieldTitle,2)
    actionMap = ['none': 'Do Nothing (leave as is)','on': 'Turn On', 'off': 'Turn Off', 'toggle': 'Toggle', 'resume':'Resume Schedule (or turn off)']
    if(settings['deviceType'] == 'lock') actionMap = ['none': 'Don\'t lock or unlock','lock': 'Lock', 'unlock': 'Unlock']

    input type + 'Action', 'enum', title: '', multiple: false, width: width, options: actionMap, submitOnChange:true
    if(!settings['startAction'] && !settings['stopAction']) displayInfo('Select what action to take when the ' + sensorFullName + ' reading meets the requirement (as "start"), or no longer meets it ("stop"). You will enter the start and stop actions below.')

    displayActionWaitOption(type)

}

def displayActionWaitOption(type){
    if(!settings[type + 'Action']) return
    if(settings[type + 'Action'] == 'none') return
    if(!settings['advanced']) return

    displayError(getMinutesValidationError(settings[type + 'Wait']))

    width = 10
    fieldName = type + 'Wait'
    fieldTitle = 'Wait minutes: (Optional)'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    displayLabel(fieldTitle,2)
 
    input fieldName, 'number', title: '', defaultValue: false, width:width, submitOnChange:true
    if(type == 'stop') displayInfo('This sets it to wait before performing the start or stop action.')
}

def displayStopActionOption(){
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
    if(!settings['device']) return

    width = 10
    fieldName = 'stopAction'
    fieldTitle = 'Stop action:'
    if(!settings[fieldName]) fieldTitle = 'Select stop action to take:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    actionMap = ['none': 'Do nothing (leave as is)','on': 'Turn On', 'off': 'Turn Off', 'toggle': 'Toggle', 'resume':'Resume Schedule (or turn off)']
    if(settings["deviceType"] == 'lock') actionMap = ['none': 'Don\'t lock or unlock','lock': 'Lock', 'unlock': 'Unlock']
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(!settings[fieldName]) {
        displayLabel(highlightText(fieldTitle))
        width = 12
    }
    input "stopAction", "enum", title: '', multiple: false, width: width, options: actionMap, submitOnChange:true

    //if(settings['levelThreshold']) displayInfo('The ' + settings['deviceType'] + ' will ' + actionMap[settings['startAction']] + ' after ' + sensorFullName + ' goes ' + forwardDirection + ' ' + settings['levelThreshold'] + unitType + ', and then ' + actionMap[settings['stopAction']] + ' when back ' + reverseDirection + ' ' + settings['levelThreshold'] + unitType + '.')
    if(!settings['stopAction']) displayInfo('Select what action to take when the ' + sensorFullName + ' reading no longer meets the requirement (for instance if you select being above 50' + typeUnit + ', it would "start" above 50' + typeUnit + ' and then "stop" below 50' + typeUnit + ').')
    
}

def displayThresholdOption(){
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
    if(!settings['device']) return
    
    hidden = true
    if(!settings['levelThreshold']) sectionTitle = 'Click to set threshold</b> (Optional)'
    if(settings['levelThreshold']) sectionTitle = '<b>Run while: ' + sensorFullName.capitalize() + ' ' + forwardDirection + ' ' + settings['levelThreshold'] + typeUnit + '</b>'

    section(hideable: true, hidden: hidden, sectionTitle){
        //display error if !validate
    
        if(settings['levelThreshold']) displayDirectionOption('threshold')

        width = 10
        fieldName = 'levelThreshold'
        fieldTitle = 'Threshold: '
        fieldTitle = addFieldName(fieldTitle,fieldName)
        if(settings[fieldName]) displayLabel(fieldTitle,2)
        if(!settings[fieldName]) {
            displayLabel(fieldTitle)
            width = 12
        }

        input fieldName, 'number', title: '', width:width,submitOnChange:true
    }
}

def displayLevelDeltaOption(){
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
    if(!settings['device']) return

    minutes = settings['relativeMinutes']
    if(!settings['relativeMinutes']) minutes = 'the specified number of'

    hidden = true
    if(settings['relativeMinutes'] != 5 && !settings['levelDelta']) hidden = false
    if(!validateMinutes(settings['relativeMinutes'])) hidden = false
    sectionTitle = 'Click to set amount ' + forwardDirection2 + ' over time (optional)'
    if(settings['levelDelta']) sectionTitle = '<b>Run after: ' + sensorFullName.capitalize() + ' ' + forwardDirection2 + 's ' + settings['levelDelta'] + typeUnit + ' in ' + minutes + ' min.</b>'

    section(hideable: true, hidden: hidden, sectionTitle){
        //display error if !validate
        if(settings['levelDelta']) displayDirectionOption('delta')
        displayError(getMinutesValidationError(settings['relativeMinutes']))
        displayDeltaMinutes()

        width = 10
        fieldName = 'levelDelta'
        fieldTitle = sensorFullName.capitalize() + ' change'
        fieldTitle = addFieldName(fieldTitle,fieldName)
        if(settings[fieldName]) displayLabel(fieldTitle,2)
        if(!settings[fieldName]) {
            displayLabel(fieldTitle)
            width = 12
        }
        input fieldName, 'decimal', title: '', required: false, width:width,submitOnChange:true

        if(settings['levelDelta']) {
            if(settings['direction']) startLevel = sensorAverage + settings['levelDelta']
            if(!settings['direction']) startLevel = sensorAverage - settings['levelDelta']
            if(settings['direction']) stopLevel = sensorAverage + Math.round(settings['levelDelta'] / 10)
            if(!settings['direction']) stopLevel = sensorAverage - Math.round(settings['levelDelta'] / 10)
            helpTip = 'The ' + sensorPlural + ' is currently at ' + sensorAverage + typeUnit + ', so it would turn the ' + devicePlural + ' on if, within ' + minutes + ' minutes, it were to ' + forwardDirection2 + ' to ' + startLevel + typeUnit + ' (and turn off only when back to at least ' + stopLevel + typeUnit + ').'
        }
        if(!settings['levelDelta']) displayInfo('Enter the number of ' + unitText + ' for ' + sensorFullName + ' must ' + forwardDirection2 + ' within ' + minutes + ' minutes to start the ' + devicePlural + ', relative to original level (currently ' + sensorAverage + unitType + '). (It will continue to run until back within 10% of the original value.)')

        displayInfo(helpTip)
        if(settings['levelDelta']){
         //   if(settings['humidityDirection'] && (settings['humidityDelta'] + humidityAverage > 100)) warnMessage = 'The current humidity is ' + humidityAverage + humidityTypeUnit + ' so an increase of ' + settings['humidityDelta'] + humidityTypeUnit + ' (to ' + (settings['humidityDelta'] + humidityAverage) + humidityTypeUnit + ') is not possible with the current conditions.'
         //   if(!settings['humidityDirection'] && (humidityAverage - settings['humidityDelta'] < 0)) warnMessage = 'The current humidity is ' + humidityAverage + humidityTypeUnit + ' so a decrease of ' + settings['humidityDelta'] + humidityTypeUnit + ' (to ' + (humidityAverage - settings['humidityDelta']) + humidityTypeUnit + ') is not possible with the current conditions.'
        }
        displayWarning(warnMessage)
    }
}

def displayDirectionOption(type){
    fieldName = 'direction'
    if(type ==  'threshold') fieldTitle = '<b>Start if ' + sensorFullName + ' is ' + forwardDirection + ' ' + settings['levelThreshold'] + typeUnit + '.</b> Click for ' + reverseDirection + '.'
    
    if(type ==  'delta') fieldTitle = '<b>Start if ' + sensorFullName + ' ' + forwardDirection2 + 's ' + settings['levelDelta'] + typeUnit + ' in ' + minutes + ' minutes.</b> Click for ' + reverseDirection2 + 's.'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'bool', title: fieldTitle, submitOnChange:true
}

def displayDeltaMinutes(){
    width = 10
    fieldName = 'relativeMinutes'
    fieldTitle = 'Interval (minutes)'
    if(settings[fieldName] == 5 && !settings['levelDelta']) fieldTitle = 'Minutes between change (default 5)'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName] != 5 || settings['levelDelta']) displayLabel(fieldTitle,2)
    if(settings[fieldName] == 5 && !settings['levelDelta']) {
        displayLabel(fieldTitle)
        width = 12
    }
    input fieldName, 'number', title: '', required: false, submitOnChange:true, width:width, defaultValue: 5
}

def displayChangeModeOption(){
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
    if(!settings['device']) return

    hidden = true
    if(settings['startMode'] || settings['stopMode']) hidden = false
    if(settings['startMode'] && settings['stopMode']) hidden = true

    sectionTitle = 'Click to set Mode change (optional)'
    if(settings['startMode']) sectionTitle = '<b>On start: Set Mode to ' + settings['startMode'] + '</b>'
    if(settings['startMode'] && settings['stopMode']) sectionTitle += '<br>'
    if(settings['stopMode']) sectionTitle += '<b>On stop: Set Mode to ' + settings['stopMode'] + '</b>'
    if(settings['startMode'] && !settings['stopMode']) sectionTitle += moreOptions
    if(!settings['startMode'] && settings['stopMode']) sectionTitle += moreOptions
    section(hideable: true, hidden: hidden, sectionTitle){
        input 'startMode', 'mode', title: 'Set Hubitat\'s "Mode" on start?', width: 6, submitOnChange:true
        input 'stopMode', 'mode', title: 'Set Hubitat\'s "Mode" on stop?', width: 6, submitOnChange:true
    }
}

def displayRunTimeOption(){
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
    if(!settings['device']) return
    
    hidden = true
    if(settings['runTimeMinimum'] && settings['runTimeMaximum'] && settings['runTimeMinimum'] >= settings['runTimeMaximum']) hidden = false
    if(settings['runTimeMinimum'] && !settings['runTimeMaximum']) hidden = false
    if(!settings['runTimeMinimum'] && settings['runTimeMaximum']) hidden = false
    if(!validateMinutes(settings['runTimeMinimum'])) hidden = false
    if(!validateMinutes(settings['runTimeMaximum'])) hidden = false

    sectionTitle = ''
    if(!settings['runTimeMinimum'] && !settings['runTimeMaximum']) sectionTitle = 'Click to set run time (optional)'
    if(settings['runTimeMinimum']) sectionTitle =  '<b>Minimum run time: ' + settings['runTimeMinimum'] + ' min.</b>'
    if(settings['runTimeMinimum'] && settings['runTimeMaximum']) sectionTitle += '<br>'
    if(settings['runTimeMaximum']) sectionTitle += '<b>Maximum run time: ' + settings['runTimeMaximum'] + ' min.</b>'
    if(settings['runTimeMinimum'] && !settings['runTimeMaximum']) sectionTitle += moreOptions
    if(!settings['runTimeMinimum'] && settings['runTimeMaximum']) sectionTitle += moreOptions

    section(hideable: true, hidden: hidden, sectionTitle){
        if(settings['runTimeMinimum'] && settings['runTimeMaximum'] && settings['runTimeMinimum'] > settings['runTimeMaximum']) displayError('Minimum run time must be greater than maximum run time.')
        displayError(getMinutesValidationError(settings['runTimeMinimum']))
        displayError(getMinutesValidationError(settings['runTimeMaximum']))
        if(settings['runTimeMinimum'] && settings['runTimeMaximum'] && settings['runTimeMinimum'] == settings['runTimeMaximum']) displayWarning('If is not recommended to have equal maximum and minimum run time. It will turn off after ' + settings['runTimeMinimum'] + ' minutes regardless of any other settings, but setting maximum run time without any other start and stop settings would accomplish the same thing.')

        displayRunTimeMinimum()
        displayRunTimeMaximum()
        //displayRunTimeMaximumManual()
    }
}

def displayRunTimeMinimum(){
    if(validateMinutes(settings['runTimeMinimum']) && !validateMinutes(settings['runTimeMaximum'])) return
    width = 10
    fieldName = 'runTimeMinimum'
    fieldTitle = 'Minimum minutes:'
    if(!settings[fieldName]) fieldTitle = 'Minimum run time (in minutes)'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(!settings[fieldName]) {
        displayLabel(fieldTitle)
        width = 12
    }
    input fieldName, 'number', title: '', required: false, width:width, submitOnChange:true
    message = 'Number of minutes it must run before stopping regardless of ' + sensorFullName + ' level, to prevent "cycling".'
    if(settings['startWait'] && !settings['stopWait']) message += ' Note that the start wait time will not affect the minimum run time.'
    if(settings['startWait'] && settings['stopWait']) message += ' Note that neither the start nor stop wait time will not affect the minimum run time.'
    if(!settings['startWait'] && settings['stopWait']) message += ' Note that neither the start nor stop wait time will not affect the minimum run time.'
    if(!settings[fieldName]) displayInfo(message)
}

def displayRunTimeMaximum(){
    if(!validateMinutes(settings['runTimeMinimum'])) return
    width = 10
    fieldName = 'runTimeMaximum'
    fieldTitle = 'Maximum minutes:'
    if(!settings[fieldName]) fieldTitle = 'Maximum run time (in minutes)'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(!settings[fieldName]) {
        displayLabel(fieldTitle)
        width = 12
    }
    input fieldName, 'number', title: '', required: false, width:width, submitOnChange:true
    message = 'Number of minutes to run after which it will stop regardless of ' + sensorFullName + ' level. (Will not start again for the same duration.)'
    if(settings['startWait'] && !settings['stopWait']) message += ' Note that the start wait time will not affect the maximum run time.'
    if(settings['startWait'] && settings['stopWait']) message += ' Note that neither the start nor stop wait time will not affect the maximum run time.'
    if(!settings['startWait'] && settings['stopWait']) message += ' Note that neither the start nor stop wait time will not affect the maximum run time.'
    if(!settings[fieldName]) displayInfo(message)
}

def displayScheduleSection(){
    if(!settings['advanced']) return
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
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
        if(settings['start_time'] && settings['start_time'] == settings['stop_time']) displayError('You can\'t have the same time to begin and end.')

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
    if(!settings['start_timeType'] && !settings['stop_timeType'] && !settings['days'] && !settings['months']) return 'Click to constrain by schedule (optional)'

    if(settings['start_timeType']) sectionTitle = '<b>Only between: '
    if(settings['start_timeType'] == 'time' && settings['start_time']) sectionTitle += Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings['start_time']).format('h:mm a', location.timeZone)
    if(settings['start_timeType'] == 'time' && !settings['start_time']) sectionTitle += 'Specific time '
    if(settings['start_timeType'] == 'sunrise' || settings['start_timeType'] == 'sunset'){
        if(!settings['start_sunType']) sectionTitle += 'Based on ' + settings['start_timeType']
        if(settings['start_sunType'] == 'at') sectionTitle += settings['start_timeType'].capitalize()
        if(settings['start_sunOffset']) sectionTitle += settings['start_sunOffset'] + ' minutes '
        if(settings['start_sunType'] && settings['start_sunType'] != 'at') sectionTitle += settings['start_sunType'].capitalize() + ' ' + settings['start_timeType']
        if(validateTimes('start')) sectionTitle += ' ' + getSunriseTime(settings['start_timeType'],settings['start_sunOffset'],settings['start_sunType'])
    }

    if(settings['start_timeType'] && settings['days']) sectionTitle += ' on: ' + dayText
    if(settings['start_timeType'] && settings['months'] && settings['days']) sectionTitle += ';'
    if(settings['start_timeType'] && settings['months']) sectionTitle += ' in ' + monthText
    if(settings['start_timeType']) sectionTitle += '</b>'
    if(settings['start_timeType'] || settings['stop_timeType'] || settings['days'] || settings['months']) {
        if(!settings['start_timeType'] || !settings['stop_timeType'] || !settings['days'] || !settings['months']) sectionTitle += moreOptions
    }
    if(!settings['start_timeType'] && !settings['stop_timeType']) return sectionTitle

    sectionTitle += '</br>'
    if(settings['stop_timeType']) sectionTitle += '<b>And: '
    if(settings['stop_timeType'] == 'time' && settings['stop_time']) sectionTitle += Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings['stop_time']).format('h:mm a', location.timeZone)
    if(settings['stop_timeType'] == 'time' && !settings['stop_time']) sectionTitle += 'Specific time '
    if(settings['stop_timeType'] == 'sunrise' || settings['stop_timeType'] == 'sunset'){
        if(!settings['stop_sunType']) sectionTitle += 'Based on ' + settings['stop_timeType']
        if(settings['stop_sunType'] == 'at') sectionTitle += settings['stop_timeType'].capitalize()
        if(settings['stop_sunOffset']) sectionTitle += settings['stop_sunOffset'] + ' minutes '
        if(settings['stop_sunType'] && settings['stop_sunType'] != 'at') sectionTitle += settings['stop_sunType'].capitalize() + ' ' + settings['stop_timeType']
        if(stopTimeComplete) sectionTitle += ' ' + getSunriseTime(settings['stop_timeType'],settings['stop_sunOffset'],settings['stop_sunType'])
    }

    if(settings['start_timeType']) return sectionTitle + '</b>'
}

def displayTypeOption(type){
    if(type == 'stop' && (!settings['start_timeType'] || !validateTimes('start'))) return
    
    ingText = 'Begin'
    if(type == 'stop') ingText = 'End'
    displayLabel(ingText + 'ing time')

    if(!validateSunriseMinutes(type)) displayWarning('Time ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType'] + ' is ' + (Math.round(settings[type + '_sunOffset']) / 60) + ' hours. That\'s probably wrong.')
    
    fieldName = type + '_timeType'
    fieldTitle = ingText.capitalize() + ' option'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    fieldList = ['time':'Start at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
    if(type == 'stop') fieldList = ['time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
    input fieldName, 'enum', title: fieldTitle, multiple: false, width: getTypeOptionWidth(type), options: fieldList, submitOnChange:true
    if(!settings['start_timeType'] && type == 'start') displayInfo('Select whether to enter a specific time, or have begin time based on sunrise and sunset for the Hubitat location. Required.')
}

def displayTimeOption(type){
    if(type == 'stop' && !validateTimes('start')) return
    if(settings[type + '_timeType'] != 'time') return
    
    fieldName = type + '_time'
    fieldTitle = type.capitalize() + ' time:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'time', title: fieldTitle, width: getTypeOptionWidth(type), submitOnChange:true
    if(!settings[fieldName]) displayInfo('Enter the time to ' + type + ' the schedule in "hh:mm AM/PM" format. Required.')
}

def getTypeOptionWidth(type){
    if(!settings[type + '_timeType']) return 12
    if(settings[type + '_sunType'] && settings[type + '_sunType'] != 'at') return 4
    return 6
}

def displaySunriseTypeOption(type){
    if(!settings[type + '_timeType']) return
    if(settings[type + '_timeType'] == 'time') return
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

def displayIfModeOption(){
    if(!settings['advanced']) return
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
    if(!settings['device']) return

    sectionTitle = 'Click to select with what Mode (optional)'
    if(settings['ifMode']) sectionTitle = '<b>Only with Mode: ' + settings['ifMode'] + '</b>'

    section(hideable: true, hidden: true, sectionTitle){
        input 'ifMode', 'mode', title: 'Only run if Mode is already?', width: 12, submitOnChange:true

        message = 'This will limit the ' + pluralContact + ' from running to only when Hubitat\'s Mode is as selected.'
        if(settings['ifMode']) message = 'This will limit the ' + pluralContact + ' from running to only when Hubitat\'s Mode is ' + settings['ifMode'] + '.'

        displayInfo(message)
    }
}

def setLightOptions(){
    if(!settings['advanced']) return
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
    if(!settings['device']) return
    if(settings['deviceType'] != 'light' && settings['deviceType'] != 'color') return

    sectionTitle = getLightOptionsSectionTitle()
    // brightness, color and temp levels section title

    hidden = true
    //if !validate brightness, color or temp, hidden = false
    section(hideable: true, hidden: hidden, sectionTitle){
        // display error if !validate
        displayBrightnessOption()
        displayColorTemperatureOption()
        displayHueOption()
        displaySatOption()
    }
}

def getLightOptionsSectionTitle(){
    sectionTitle = 'Click to set light options (optional)'
    if(settings['startBrightness']) sectionTitle = '<b>On start: Set brightness to ' + settings['startBrightness'] + '%</b>'
    if(settings['startColorTemperature']) {
        if(settings['startBrightness']) sectionTitle += '<br>'
        sectionTitle = '<b>On start: Set color brightness to ' + settings['startColorTemperature'] + 'K</b>'
    }
    if(settings['startHue']) {
        if(settings['startBrightness']) sectionTitle += '<br>'
        sectionTitle = '<b>On start: Set hue to ' + settings['startHue'] + '°</b>'
    }
    if(settings['startSat']) {
        if(settings['startBrightness'] || settings['startHue']) sectionTitle += '<br>'
        sectionTitle = '<b>On start: Set saturation to ' + settings['startSat'] + '%</b>'
    }
    if(settings['startBrightness'] || settings['startColorTemperature'] || settings['startHue'] || settings['startSat']) {
        if(!settings['startBrightness'] || (!settings['startColorTemperature'] || (!settings['startHue'] && !settings['startSat']))) sectionTitle += moreOptions
    }
    return sectionTitle
}

def displayBrightnessOption(){
        width = 10
        fieldName = 'startBrightness'
        fieldTitle = 'Start brightness:'
        fieldTitle = addFieldName(fieldTitle,fieldName)
        if(settings[fieldName]) displayLabel(fieldTitle,2)
        if(!settings[fieldName]) {
            displayLabel(fieldTitle)
            width = 12
        }
        input fieldName, 'number', title: '', required: false, width:width,submitOnChange:true
}

def displayColorTemperatureOption(){
    if(settings['deviceType'] != 'color') return

    width = 10
    fieldName = 'startColorTemperature'
    fieldTitle = 'Start color temperature:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(!settings[fieldName]) {
        displayLabel(fieldTitle)
        width = 12
    }
    input fieldName, 'number', title: '', required: false, width:width,submitOnChange:true
    displayInfo('Only color temperature or hue/saturation can be set, not both. Color temperature is from 1800 to 5400 where lower is more yellow and higher is more blue; 3000 is warm white, 4000 is cool white, and 5000 is daylight.')
}

def displayHueOption(){
    if(settings['deviceType'] != 'color') return
    if(settings['startColorTemperature']) return

    width = 10
    fieldName = 'startHue'
    fieldTitle = 'Start hue:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(!settings[fieldName]) {
        displayLabel(fieldTitle)
        width = 12
    }
    input fieldName, 'number', title: '', required: false, width:width,submitOnChange:true

    displayInfo('Hue is degrees from 1 to 360 around a color wheel, where red is 1 (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270 (may vary by device).')
}

def displaySatOption(){
    if(settings['deviceType'] != 'color') return
    if(settings['startColorTemperature']) return

    width = 10
    fieldName = 'startHue'
    fieldTitle = 'Start saturation:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(settings[fieldName]) displayLabel(fieldTitle,2)
    if(!settings[fieldName]) {
        displayLabel(fieldTitle)
        width = 12
    }
    input fieldName, 'number', title: '', required: false, width:width,submitOnChange:true
    displayInfo('Saturation is the percent of color, as opposed to white. Lower numbers will appear more washed out, and higher numbers more vibrant.')
}

def displayAlertOptions(){
    if(!settings['advanced']) return
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
    if(!settings['device']) return
    if(!parent.pushNotificationDevice && !parent.speechDevice) return

    hidden = true
    if(settings['pushNotification']) hidden = false
    if(settings['speech']) hidden = false
    if((settings['pushNotification'] || settings['speech']) && !settings['notificationStartStop']) hidden = false

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
    
    sectionTitle = ''
    if(settings['notificationStartStop']) sectionTitle = '<b>On ' + settings['notificationOpenClose'] + ', '
    if(settings['notificationStartStop'] == 'both') sectionTitle = '<b>On start and stop, '

    if(settings['pushNotification'] && settings['speech']) sectionTitle += 'send notification and speak text</b>'
    if(settings['pushNotification'] && !settings['speech']) sectionTitle += 'send notification</b>'
    if(!settings['pushNotification'] && settings['speech']) sectionTitle += 'speak text</b>'
    if(!settings['notificationStartStop']) sectionTitle = '<b>' + sectionTitle.capitalize()
    if(settings['pushNotification'] && !settings['speech']) sectionTitle += moreOptions
    if(!settings['pushNotification'] && settings['speech']) sectionTitle += moreOptions
    
    if(!settings['speech'] && !settings['pushNotification']) sectionTitle = 'Click to send notifications (optional)'

    section(hideable: true, hidden: hidden, sectionTitle){
        if(parent.pushNotificationDevice){
            if(countPushDevices > 1) input 'pushNotificationDevice', 'enum', title: 'Push notification device(s)?', options: state.pushFilteredList, multiple: true, submitOnChange: true

            input 'pushNotification', 'text', title: 'Text of push notification to send?', submitOnChange:true
            if(countPushDevices == 1) displayInfo('Push notifications will use the device ' + pushDeviceName+ '. To use other(s), add it in the Master app.')
        }

        if(parent.speechDevice){
            if(countSpeechDevices > 1) input 'speechDevice', 'enum', title: 'Text-to-speech device to use', options: state.speechFilteredList, multiple: true, submitOnChange: true

            input 'speech', 'text', title: 'Text-to-speech announcement?', submitOnChange:true
            if(countSpeechDevices == 1) displayInfo('Text-to-speech will use the device ' + speechDeviceName + '. To use other(s), add it in the Master app.')
        }
        if(settings['pushNotification'] && settings['speech']) action = 'Notice and speak'
        if(settings['pushNotification'] && !settings['speech']) action = 'Push notice'
        if(!settings['pushNotification'] && settings['speech']) action = 'Speak'

        sectionTitle = action + ' on start or stop? (Required)'
        
        if((settings['pushNotificationDevice'] && settings['pushNotification']) || (settings['speechDevice'] && settings['speech'])) input 'notificationStartStop', 'enum', title: sectionTitle, multiple: false, width: 12, options: ['start': 'Start', 'stop': 'Stop','both': 'Both start and stop'], submitOnChange:true
    }
}

def displayPeopleOption(){
    if(!settings['advanced']) return
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
    if(!settings['device']) return

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
    if(settings['personHome']) sectionTitle = '<b>With: ' + withPeople + '</b>'
    if(settings['personHome'] && settings['personNotHome']) sectionTitle += '<br>'
    if(settings['personNotHome']) sectionTitle += '<b>Without: ' + withoutPeople + '</b>'

    section(hideable: true, hidden: hidden, sectionTitle){
        if(peopleError) displayError('You can\'t include and exclude the same person.')

        input 'personHome', 'capability.presenceSensor', title: 'Only if any of these people are home (Optional)', multiple: true, submitOnChange:true
        input 'personNotHome', 'capability.presenceSensor', title: 'Only if all these people are NOT home (Optional)', multiple: true, submitOnChange:true
    }
}

// Capitlized for the sensor selection list; then set to lowercase for display
def getDeviceListMap(){
    return ['temperature':'Temperature','humidity':'Humidity','illuminance':'Illuminance','energy':'Energy','power':'Power','carbonDioxide':'Carbon dioxide']
}

def getTypeUnit(){
    if(settings['sensorType'] == 'temperature') return '°' + getTemperatureScale()
    if(settings['sensorType'] == 'humidity') return '%'
    if(settings['sensorType'] == 'illuminance') return ' lux'
    if(settings['sensorType'] == 'energy') return ' kWh'
    if(settings['sensorType'] == 'power') return ' W'
    if(settings['sensorType'] == 'carbonDioxide') return ' ppm'
}

def getUnitText(){
    if(settings['sensorType'] == 'temperature') return 'degrees'
    if(settings['sensorType'] == 'humidity') return 'percent'
    if(settings['sensorType'] == 'illuminance') return 'lux'
    if(settings['sensorType'] == 'energy') return 'kilowatt hours'
    if(settings['sensorType'] == 'power') return 'Watts'
    if(settings['sensorType'] == 'carbonDioxide') return 'parts per million'
}

def getSensorCount(){
    if(!settings['sensor']) return
    count = 0
    settings['sensor'].each{singleDevice->
        if(settings['sensorType'] == 'temperature' && singleDevice.currentTemperature) count++
        if(settings['sensorType'] == 'humidity' && singleDevice.currentHumidity) count++
        if(settings['sensorType'] == 'illuminance' && singleDevice.currentIlluminance) count++
        if(settings['sensorType'] == 'energy' && singleDevice.currentEnergy) count++
        if(settings['sensorType'] == 'power' && singleDevice.currentPower) count++
        if(settings['sensorType'] == 'carbonDioxide' && singleDevice.currentCarbonDioxide) count++
        }
    return count
}

def getDeviceCount(){
    if(!settings['device']) return
    count = 0
    settings['device'].each{singleDevice->
        count++
    }
    return count
}

def getPluralSensor(){
    if(!getSensorCount()) return 'sensor(s)'
    if(getSensorCount() > 1) return 'sensors'
    return 'sensor'
}

def getPluralDevice(){
    if(!getDeviceCount()) return 'device(s)'
    if(getDeviceCount() > 1) return 'devices'
    return 'device'
}

def getSensorAverage(){
    if(!settings['sensor']) return
    if(sensorCount == 0) return

    count = 0
    total = 0
    settings['sensor'].each{singleDevice->
        if(settings['sensorType'] == 'temperature') total += singleDevice.currentTemperature
        if(settings['sensorType'] == 'humidity') total += singleDevice.currentHumidity
        if(settings['sensorType'] == 'illuminance') total += singleDevice.currentIlluminance
        if(settings['sensorType'] == 'energy') total += singleDevice.currentEnergy
        if(settings['sensorType'] == 'power') total += singleDevice.currentPower
        if(settings['sensorType'] == 'carbonDioxide') total += singleDevice.currentCarbonDioxide
    }
    return Math.round(total / sensorCount)
}

def getDirectionForward(){
    if(settings['direction']) return 'over'
    return 'under'
}

def getDirectionReverse(){
    if(settings['direction']) return 'under'
    return 'over'
}

def getDirectionForward2(){
    if(settings['direction']) return 'increase'
    return 'decrease'
}

def getDirectionReverse2(){
    if(settings['direction']) return 'decrease'
    return 'increase'
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

def validateMinutes(value){
    if(!getMinutesValidationError(value)) return true
}

def getMinutesValidationError(value){
    if(value == 0) return
    if(!value) return
    if(value > 1440) return 'Maximum minutes is 1,440 (24 hours).'
    if(value < 1) return 'Minimum minutes is 1.'
}

def validateTimes(type){
    if(!settings[type + '_timeType']) return false
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
    putLog(1137,'trace','Installed')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    putLog(1143,'trace','Updated')
    unsubscribe()
    initialize()
}

def initialize() {
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    setTime()
// If manual on, need to reset maximum runtime schedule
	unschedule()  

    // If date/time for last notification not set, initialize it to 5 minutes ago
    if(!atomicState.contactLastNotification) atomicState.contactLastNotification = new Date().getTime() - parent.CONSTHourInMilli() //Wtf?
    
    setTime()
    scheduleMaximumRunTime()
       
    subscribe(settings['sensor'], settings['sensorType'], handleSensorUpdate)
    
    if(settings['deviceType'] == 'switch') subscribe(settings['device'], 'switch', handleStateChange)
    if(settings['deviceType'] == 'light') subscribe(settings['device'], 'switch', handleStateChange)
    if(settings['deviceType'] == 'color') subscribe(settings['device'], 'switch', handleStateChange)
    if(settings['deviceType'] == 'lock') subscribe(settings['device'], 'lock', handleStateChange)
    if(settings['deviceType'] == 'fan') subscribe(settings['device'], 'switch', handleStateChange)
    
    putLog(1174,'trace','Initialized')
}

def handleSensorUpdate(event) {
    updateStatus()
}

def updateStatus(){
    atomicState.sensorAverage = getSensorAverage()
    //atomicState.controlSensorAverage = 

    updateSensorDeltaArray()
    
    turnOn()
    turnOff()
}

def handleStateChange(event) {
    lastChangeAddId = parent.getStateChangeAppId(event.device,app.id,app.label)
    currentState = parent.checkIsOn(event.device,app.label)
    if(lastChangeAddId == app.id && currentState == event.value) return
    parent.updateTableCapturedState(event.device,event.value,app.label)
    atomicState.startTime = now()
    if(event.value == 'on') scheduleMaximumRunTime()	
    if(event.value == 'off') atomicState.startTime = null
}

def turnOn(){
    if(atomicState.startTime) return // Already on
    if(getDisabled()) return
    atomicState.sensorStart = atomicState.sensorAverage
    if(!checkOnConditions()) return
    
    atomicState.startTime = now()
    atomicState.stopTime = null
    
    if(checkOffConditions()) {
        atomicState.startTime = null
        putLog(1213,'error','Both on and off conditions met.')
        return
    }
    
    atomicState.startTime = now()
    atomicState.stopTime = null
// Set locks
// Set levels
    if(settings['startAction'] == 'on' || settings['startAction'] == 'off' || settings['startAction'] == 'toggle') {
        settings['device'].each{singleDevice->
    // set levels
            stateMap = parent.getStateMapSingle(singleDevice,settings['startAction'],app.id,app.label)
            parent.mergeMapToTable(singleDevice.id,stateMap,app.label)
        }
        putLog(1227,'warn','Turning devices on.')
        parent.setDeviceMulti(settings['device'],app.label)
    }
    unschedule()
    scheduleMaximumRunTime()
// set Mode
    //parent.sendPushNotification('',app.label + ' turned on at ' + now.format('h:mm a', location.timeZone),app.label)
    //parent.sendVoiceNotification(settings['speechDevice'],settings['speech'],app.label)
}

def turnOff(){
    log.debug '1 turnOff'
    if(!atomicState.startTime) return // Already off
    log.debug '2 turnOff'
    if(getDisabled()) return
    log.debug '3 turnOff'
    if(!checkOffConditions()) return
    log.debug '4 turnOff'
    if(checkOnConditions()) return
    log.debug '5 turnOff'

    if(!checkRunTimeMinimum()) {
        scheduleMinimumRunTime()
        return
    }

// check minimum runtime (remove check for checkOffConditions) then schedule for remaining time
    atomicState.startTime = null
    atomicState.stopTime = now()
// set locks
// set levels
    if(settings['stopAction'] == 'on' || settings['stopAction'] == 'off' || settings['stopAction'] == 'toggle') {
        settings['device'].each{singleDevice->
            stateMap = parent.getStateMapSingle(singleDevice,settings['stopAction'],app.id,app.label)
            parent.mergeMapToTable(singleDevice.id,stateMap,app.label)
        }
        parent.setDeviceMulti(settings['device'],app.label)
    }
    unschedule()
// Set mode
}

def updateSensorDeltaArray(){
    if(!atomicState.sensorChanges){
        atomicState.sensorChanges = ['1':[time:now(),sensor:atomicState.sensorAverage]]
        return
    }
    itemCount = 0
    newArray = [:]
    timeLimit = now() - (settings['relativeMinutes'] * parent.CONSTMinuteInMilli())
    atomicState.sensorChanges.each{
        if(it.value.time > timeLimit) {
            itemCount++
            newArray[itemCount]  = [time:it.value.time,sensor:it.value.sensor]
        } else {
            if(!earliestTime) {
                earliestTime =  it.value.time
                earliestValue = it.value.sensor
            } else if(earliestValue && earliestValue < it.value.time)  {
                earliestTime =  it.value.time
                earliestValue = it.value.sensor
            }
        }
    }
    if(earliestValue){
        if(!itemCount) itemCount = 0
        itemCount++
            newArray[itemCount] = [time:earliestTime,sensor:earliestValue]
    }
    itemCount++
    newArray[itemCount]  = [time:now(),sensor:atomicState.sensorAverage]
    atomicState.sensorChanges = newArray
}

def checkOnConditions(){
    if(!checkMinimumWaitTime()) return
// check people
// check mode
// check time
    //if(settings['multiStartTrigger']) {
    //    allOnConditions = checkAllOnConditions()
    //    putLog(1297,'trace','All on conditions is ' + allOnConditions)
    //    return allOnConditions
    //}
    anyOnConditions = checkAnyOnConditions()
    putLog(1301,'trace','Any on condition is ' + anyOnConditions)
    return anyOnConditions
}

def checkOffConditions(){
// check people
// check mode
// check time
log.debug '1 checkOffConditions'
    if(checkRunTimeMaximum()) return true
    
    //if(settings['multiStopTrigger']) {
    //    allOffConditions = checkAllOffConditions()
     //   putLog(1314,'trace','All off conditions is ' + allOffConditions)
     //   return allOffConditions
    //}
    anyOffConditions = checkAnyOffConditions()
    putLog(1318,'trace','Any off conditions is ' + anyOffConditions)
    return anyOffConditions
}

def checkAnyOnConditions(){
    log.debug '1 checkAnyOnConditions'
    if(checkControlDifference()) return true
    log.debug '2 checkAnyOnConditions'
    if(checkThreshold()) return true
    log.debug '3 checkAnyOnConditions'
    if(checkDelta()) return true
    log.debug '4 checkAnyOnConditions'
    return false    // used for log
}


// controlStopManual is not checked

def checkAnyOffConditions(){
    if(checkControlStopDifference()) return true
    if(checkStopThreshold()) return true
    if(checkStopDelta()) return true
    return false
}

def checkControlDifference(){
    if(!settings['controltDifference']) return
    if(atomicState.sensorAverage > atomicState.controlSensorAverage + settings['controlDifference']) return true
}

def checkControlStopDifference(){
    if(!settings['controlStopDifference']) return
    if(!settings['controlStopDifferenceManual']) return
    if(atomicState.sensorAverage < atomicState.controlSensorAverage + settings['controlStopDifference']) return true
}
    
//With stop, need to test this if state.appId matches - but that is per device
//Need to make testing each criteria per device?!
//if(!settings['humidityAbsoluteThresholdManualStop']) return

// Need to check if we use independent/average as set by [type]SensorAverage

def checkThreshold(){
    if(!settings['levelThreshold']) return
    if(settings['direction'] && atomicState['sensorAverage'] > settings['levelThreshold']) return true
    if(!settings['direction'] && atomicState['sensorAverage'] < settings['levelThreshold']) return true
}

def checkStopThreshold(){
    if(!settings['levelThreshold']) return
    if(settings['direction'] && atomicState['sensorAverage'] < settings['levelThreshold']) return true
    if(!settings['direction'] && atomicState['sensorAverage'] > settings['levelThreshold']) return true
}

// Need to add fudge factor for Delta - if goes up 10 degrees, stop after lowering it 8

def checkDelta(){
    log.debug '1 checkDelta'
    if(!settings['levelDelta']) return
    log.debug '2 checkDelta'
    if(!settings['relativeMinutes']) return
    log.debug '3 checkDelta'
    if(!atomicState['sensorAverage']) return
    log.debug '4 checkDelta'
    
    startDelta = false
    atomicState['sensorChanges'].each {
        if(settings['direction']) {
            difference = atomicState['sensorAverage'] - it.value['sensor']
            if(difference >= settings['levelDelta']) startDelta = true
        }
        if(!settings['direction']) {
            difference = it.value['sensor'] - atomicState['sensorAverage']
            if(difference >= settings['levelDelta']) startDelta = true
        }
    }
    return startDelta
}

def checkStopDelta(type){    
    if(!settings['levelDelta']) return
    if(!settings['relativeMinutes']) return
    if(!atomicState['sensorAverage']) return

    if(settings['direction']){
        if(atomicState['sensorAverage'] <= atomicState['sensorStart'] - settings['levelDelta']) return true
    }
    if(!settings['direction']){
        if(atomicState['sensorAverage'] >= atomicState['sensorStart'] + settings['levelDelta']) return true
    }
}

def checkRunTimeMinimum(){
    if(!settings['runTimeMinimum']) return true
    if(!state.startTime) return true //??
    if((now() - atomicState.startTime) > settings['runTimeMinimum'] * parent.CONSTMinuteInMilli()) return true
}

//Returns true if condition is met
def checkRunTimeMaximum(){
    if(!settings['runTimeMaximum']) return true
    if(!atomicState.startTime) return true
    
    if(now() - atomicState.startTime > settings['runTimeMaximum'] * parent.CONSTMinuteInMilli()){
        putLog(1437,'trace','Maximum runtime exceeded.')
        return true
    }
}

def checkMinimumWaitTime(){
    if(!settings['runTimeMinimum']) return true
    if(!atomicState.stopTime) return true
    
    elapsedTime = now() - atomicState.stopTime

    if(elapsedTime < settings['runTimeMinimum'] * parent.CONSTMinuteInMilli()) return
    putLog(1449,'trace','Minimum wait time exceeded.')
    return true
}

def scheduleMaximumRunTime(){
    if(!settings['runTimeMaximum']) return
    if(!parent.checkAnyOnMulti(settings['device'])) return
    unschedule('scheduleMaximumRunTime')
    
    timeMillis = settings['runTimeMaximum'] * parent.CONSTMinuteInMilli()
    
    parent.scheduleChildEvent(timeMillis,'','turnOff','',app.id)
}

def scheduleMinimumRunTime(){
    if(!settings['runTimeMinimum']) return
    unschedule('scheduleMinimumRunTime')
    
    timeMillis = (atomicState.startTime + (settings['runTimeMinimum'] * parent.CONSTMinuteInMilli())) - now()
    
    parent.scheduleChildEvent(timeMillis,'','turnOff','',app.id)
}

def performMaximumRunTime(){     // is called from maximumTime schedule, if parameters added, need to update scheduleMaximumRunTime
    atomicState.sensorAverage = sensorAverage()
    //atomicState.sensorControlAverage = 

    updateSensorDeltaArray()
    // Why not just call turnOff()?
    if(settings['stopAction'] == 'on' || settings['stopAction'] == 'off' || settings['stopAction'] == 'toggle') {
        settings['device'].each{singleDevice->
            stateMap = parent.getStateMapSingle(singleDevice,settings['stopAction'],app.id,app.label)
            parent.mergeMapToTable(singleDevice.id,stateMap,app.label)
        }
        parent.setDeviceMulti(settings['device'],app.label)
    }
}

def setScheduleFromParent(timeMillis,scheduleFunction,scheduleParameters = null){
    runInMillis(timeMillis,scheduleFunction,scheduleParameters)
}

def setTime(){
    if(!settings['start_timeType']) return
    if(!settings['stop_timeType']) return
    if(settings['start_timeType'] == 'time') atomicState.scheduleStartTime = timeToday(settings['start_time']).getTime()
    if(settings['stop_timeType'] == 'time') atomicState.scheduleStartTime = timeToday(settings['stop_time']).getTime()
    if(settings['start_timeType'] == 'sunrise') atomicState.scheduleStartTime = (settings['start_sunType'] == 'before' ? parent.getSunrise(settings['start_sunOffset'] * -1,app.label) : parent.getSunrise(settings['start_sunOffset'],app.label))
    if(settings['stop_timeType'] == 'sunrise') atomicState.scheduleStopTime = (settings['stop_sunType'] == 'before' ? parent.getSunrise(settings['stop_sunOffset'] * -1,app.label) : parent.getSunrise(settings['stop_sunOffset'],app.label))
    if(settings['start_timeType'] == 'sunset') atomicState.scheduleStartTime = (settings['start_sunType'] == 'before' ? parent.getSunset(settings['start_sunOffset'] * -1,app.label) : parent.getSunset(settings['start_sunOffset'],app.label))
    if(settings['stop_timeType'] == 'sunset') atomicState.scheduleStopTime = (settings['stop_sunType'] == 'before' ? parent.getSunset(settings['stop_sunOffset'] * -1,app.label) : parent.getSunset(settings['stop_sunOffset'],app.label))
    
    if(settings['start_timeType'] != 'time' || settings['stop_timeType'] != 'time'){
        unschedule('setTime')
        timeMillis = now() + parent.CONSTDayInMilli()
        parent.scheduleChildEvent(timeMillis,'','setTime','',app.id)
        putLog(1496,'info','Scheduling update subrise/sunset start and/or stop time(s).')
    }
    return true
}

// Return true if disabled
def getDisabled(){
    if(settings['disable']) return true

    if(settings['ifMode'] && location.mode != settings['ifMode']) return true
    
    if(atomicState.scheduleStartTime && atomicState.scheduleStopTime){
        if(!parent.checkNowBetweenTimes(atomicState.scheduleStartTime, atomicState.scheduleStopTime, app.label)) return true
    }

    if(settings['personHome']){
        if(!parent.checkPeopleHome(settings['personHome'],app.label)) return true
    }
    if(settings['personNotHome']){
        if(!parent.checkNoPeopleHome(settings['personNotHome'],app.label)) return true
    }

    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,type = 'trace',message = null){
    return parent.putLog(lineNumber,type,message,app.label,,getLogLevel())
}
