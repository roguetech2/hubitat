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
*  Version: 0.4.3.7
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

setUILinks()

preferences {
    if(!settings) settings = [:]
    install = formComplete()
    thisType = 'sensor'
    thisDescription = 'sensor'        // Used with schedule, people, ifMode
    
    page(name: "setup", install: install, uninstall: true) {
        // display Name
        if(!app.label){
            section(){
                displayNameOption()
            }
        } else {
            if(!state.sensorsMap) state.sensorsMap = buildSensorMap()
            sensorsMap = state.sensorsMap
            if(!state.capabilitiesList) {
                capabilitiesList = parent.getInstalledCapabilitiesList(sensorsMap).sort()
            }
            state.capabilitiesList = capabilitiesList
            state.sensorTypeMap = getSensorsMapEntry(sensorsMap)
            allDeviceOptions = parent.getDeviceList()
            //controllerDeviceOptions = controllerOptionProcessParentDeviceList()
            sensorCount = getSensorCount()
            sensorAverage = getSensorAverage() 
            deviceCount = getDeviceCount()
            sensorPlural = getPluralSensor()
            devicePlural = getPluralDevice()
            forwardDirection = getDirectionForward()    // over or under
            reverseDirection = getDirectionReverse()
            forwardDirection2 = getDirectionForward2()    // increase or decrease
            reverseDirection2 = getDirectionReverse2()
            startText = getStartText()
            stopText = getStopText()
            section(){
                displayNameOption('sensor')
                displayAdvancedOption()
                displaySensorTypeOption()
                displaySensorDeviceOption()
                //displayDevicesTypesOption()
                displayControlDeviceOption()
                //displayDeviceOption()
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
        }
    }
}

def formComplete(){
    if(!app.label) return false
    if(!settings['sensorType']) return false
    if(!settings['sensor']) return false
    if(!settings['deviceType']) return false
    if(!settings['controlDevice']) return false
    if(settings['startDelay'] && settings['stopDelay'] && settings['startDelay'] > settings['stopDelay']) return false

    return true
}

def displaySensorTypeOption(){
    if(capabilitiesList) sensorCapabilitiesList = buildSensorTypeOptionsMap(capabilitiesList)
    if(!capabilitiesList) sensorCapabilitiesList = 'capabilities.*'
    fieldName = 'sensorType'
    displaySensorTypeOptionComplete(fieldName, sensorCapabilitiesList)
    displaySensorTypeOptionIncomplete(fieldName, sensorCapabilitiesList)
}
def displaySensorTypeOptionComplete(fieldName, capabilitiesList){
    if(!settings[fieldName]) return
    width = 10
    fieldTitle = 'Sensor type:'
    //displayLabel(highlightText(fieldTitle))
    displaySelectField(fieldName, fieldTitle, capabilitiesList, false, true)
    if(!state.sensorTypeMap) displayError('Something went wrong finding the select sensor type - ' + settings[fieldName] + ' in the supported list.')
}

def displaySensorTypeOptionIncomplete(fieldName, capabilitiesList){
    if(settings[fieldName]) return
    width = 12
    fieldTitle = 'Select which type of sensor:'
    //displayLabel(fieldTitle,2)
    displaySelectField(fieldName, fieldTitle, capabilitiesList, false, true)
    advancedMessage = ' Show Advanced Options for more.'
    if(sensorCapabilitiesList == 'capabilities.*') displayWarning('Select what type of sensor to use. All supported sensor types listed. ' + advancedMessage + 'To list only what you have available, update the device list in the Master app.')
    if(sensorCapabilitiesList != 'capabilities.*' && !settings['advanced']) displayInfo('Select what type of sensor to use. ' + advancedMessage)
    if(sensorCapabilitiesList != 'capabilities.*' && settings['advanced']) displayInfo('Select what type of sensor to use. If you don\'t see a sensor type you expect, you may need to update the device list in the Master app.')

}

def displaySensorDeviceOption(){
    if(!state.sensorTypeMap) return
    if(settings['sensor'] && sensorCount == 0){
        String sensorName = settings['sensor'].label
        if(settings['sensor'] && sensorCount == 0) displayWarning(sensorName + ' does not have any ' + state.sensorTypeMap.name + ' reading. It is either inactive or misreports being a ' + state.sensorTypeMap.name + ' sensor.')
    }

    fieldName = 'sensor'
    fieldTitle = 'Select type of ' + sensorPlural + ' being used:'
    if(settings[fieldName]) fieldTitle = sensorPlural.capitalize() + ' to use:'
    fieldTitle = addFieldName(fieldTitle,fieldName)

    capability = 'capability.' + settings['sensorType']
    input name: fieldName, type: capability, multiple: true, title: fieldTitle, submitOnChange: true

    if(capabilitiesList) {
        count = 0
        capabilitiesList.each{capabilityValue ->
            if(capabilitiesList.size() == 1) capabilitiesString += capabilitiesList[count]
            if(capabilitiesList.size() != 1 && count == capabilitiesList.size() - 1) capabilitiesString += ', and ' + capabilitiesList[count]   // last
            if(capabilitiesList.size() != 1 && count != 0 && count != capabilitiesList.size() - 1) capabilitiesString += ', ' + capabilitiesList[count]    // not first or last
            if(capabilitiesList.size() != 1 && count == 0) capabilitiesString += capabilitiesList[count]  // first

            count++
        }
    }
// test, if multiple selected, any common capabilities? If not, error.
    //helpTip = 'Select which ' + getDeviceTypeMap()[settings['sensorType']] + ' sensor(s) to use. Required.'
    if(!sensorAverage) return

    helpTip = state.sensorTypeMap.name + ' ' + sensorPlural + ' is currently at ' + sensorAverage + state.sensorTypeMap.unitType + '.'
    if(settings['sensor'] && sensorCount > 1) helpTip += ' '
    if(sensorCount > 1) helpTip += 'Multiple sensors are averaged together.'
    displayInfo(helpTip)
}

def displayDevicesTypesOption(){
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!state.sensorTypeMap) return

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
// Need to check any devices of each type exist, and build list
    input fieldName, 'enum', title: '', options: ['switch': 'All switches','light': 'Lights','color':'Color lights','lock': 'Lock(s)', 'fan': 'Fan(s)'], multiple: false, required: false, width:width, submitOnChange:true
}

def displayDeviceOption(){
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
    if(!state.sensorTypeMap) return

    fieldName = 'controlDevice'
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

// Move delay to it's own section, to apply to everything (Mode, notifications, etc.)
def displayActionOption(){
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
    if(!settings['controlDevice']) return
    if(!state.sensorTypeMap) return

    hidden = true
    if(settings['startDelay'] == settings['stopDelay']) hidden = false
    if(settings['startDelay'] > settings['stopDelay']) hidden = false
    sectionTitle = 'Click to set ' + startText + ' and ' + stopText + ' action(s)</b> (Optional)'
    if(settings['startAction']) {
        startSectionTitle = '<b>On ' + startText + ': ' + getPlainAction(settings['startAction']).capitalize() + '</b>'
        if(settings['startDelay'] && settings['startDelay'] > 0) startSectionTitle = ' <b>' + settings['startDelay'] + ' minutes after ' + startText + ': ' +  getPlainAction(settings['startAction']).capitalize() + '</b>'
    }
    if(settings['startAction'] && settings['stopAction']) startSectionTitle += '</b><br>'
    if(settings['stopAction']) {
        stopSectionTitle += '<b>On ' + stopText + ': ' +  getPlainAction(settings['stopAction']).capitalize() + '</b>'
        if(settings['stopDelay'] && settings['stopDelay'] > 0)  stopSectionTitle = ' <b>' + settings['stopDelay'] + ' minutes after ' + stopText + ': ' +  getPlainAction(settings['stopAction']).capitalize() + '</b>'
    }
    sectionTitle = startSectionTitle + stopSectionTitle
    if(settings['startAction'] && !settings['stopAction']) sectionTitle += moreOptions
    if(!settings['startAction'] && settings['stopAction']) sectionTitle += moreOptions
    section(hideable: true, hidden: hidden, sectionTitle){
        if(settings['startDelay'] == settings['stopDelay']) displayWarning('The Start Delay and Stop Delay times are set as the same. The Stop action will be automatically delayed a few seconds longer to prevent Start and Stop events from conflicting.')
        if(settings['startDelay'] > settings['stopDelay']) displayError('Start Delay must be equal to or less than Stop Delay. This is to prevent them from conflicting, if Stop activates within the ' + (settings['startDelay'] - settings['stopDelay']) + ' minutes() of potential overlap with Start.')
        displayActionFields('start')
        displayActionFields('stop')
    }

}
def displayActionFields(type){
    width = 10
    if(type == 'start') typeText = startText
    if(type == 'stop') typeText = stopText
    fieldName = type + 'Action'
    fieldTitle = typeText.capitalize() + ' action:'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    displayLabel(fieldTitle,2)
    actionMap = ['none': 'Do Nothing (leave as is)','on': 'Turn On', 'off': 'Turn Off', 'toggle': 'Toggle', 'resume':'Resume Schedule (or turn off)']
    if(settings['deviceType'] == 'lock') actionMap = ['none': 'Don\'t lock or unlock','lock': 'Lock', 'unlock': 'Unlock']

    input type + 'Action', 'enum', title: '', multiple: false, width: width, options: actionMap, submitOnChange:true
    if(state.sensorTypeMap.type == 'bool') infoStartText = state.sensorTypeMap.start
    if(state.sensorTypeMap.type != 'bool') infoStartText = 'start'
    if(!settings['startAction'] && !settings['stopAction']) displayInfo('Select what action to take when the ' + state.sensorTypeMap.name + ' reading meets the requirement (as "' + infoStartText + '"), or no longer meets it ("stop"). You will enter the start and stop actions below.')

    displayActionDelayOption(type)

}

def displayActionDelayOption(type){
    if(!settings[type + 'Action']) return
    if(settings[type + 'Action'] == 'none') return
    if(!settings['advanced']) return

    displayError(getMinutesValidationError(settings[type + 'Delay']))

    width = 10
    fieldName = type + 'Delay'
    fieldTitle = 'Delay minutes: (Optional)'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    displayLabel(fieldTitle,2)
 
    input fieldName, 'number', title: '', defaultValue: false, width:width, submitOnChange:true
    if(type == 'stop') {
        helpTip = 'This sets it to wait before performing the ' + startText + ' or ' + stopText + ' action.'
        if(settings['startDelay'] || settings['stopDelay']) helpTip += ' Note that if the device activates (' + state.sensorTypeMap.start + ') or re-activates within the Stop Delay time, the Stop Delay will be reset.<br>1) If a sensor activates and then deactivates within the Start Delay period, the deactivation will be ignored. (The Stop conditions will be rechecked when the Start Delay expires, without the sensor needing to update. So, the Stop Actions may <i>immediately</i> follow the Start Actions.)<br>2) If a sensor deactivates (stops) and then reactivates (starts again) within the Stop Delay period, then a) If allowed by Run Times (not Delay Times), a new Start event will be triggered, b) If Wait Times prevent a new Start event, then Stop Delay timer will continue (every Start event should allow an Stop event, with the assumption of a sensor providing only one update per Start/Stop event).' // conditions performed in handleSensorUpdate
        //if(state.sensorTypeMap.type == 'range' && settings['stopDelay']) helpTip += ' Note that if the device re-activates (the Start condition(s) are met) within the Stop Delay time, the Stop sequence will be aborted.'
        displayInfo(helpTip)
    }
}

def displayThresholdOption(){
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
    if(!settings['controlDevice']) return
    if(!state.sensorTypeMap) return
    if(state.sensorTypeMap.type == 'bool') return
    
    hidden = true
    if(!settings['levelThreshold']) sectionTitle = 'Click to set threshold</b> (Optional)'
    if(settings['levelThreshold']) sectionTitle = '<b>Run while: ' + state.sensorTypeMap.name + ' ' + forwardDirection + ' ' + settings['levelThreshold'] + state.sensorTypeMap.unitType + '</b>'

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
    if(!settings['controlDevice']) return
    if(!state.sensorTypeMap) return
    if(state.sensorTypeMap.type == 'bool') return

    minutes = settings['relativeMinutes']
    if(!settings['relativeMinutes']) minutes = 'the specified number of'

    hidden = true
    if(settings['relativeMinutes'] != 5 && !settings['levelDelta']) hidden = false
    if(!validateMinutes(settings['relativeMinutes'])) hidden = false
    sectionTitle = 'Click to set amount ' + forwardDirection2 + ' over time (optional)'
    if(settings['levelDelta']) sectionTitle = '<b>Run after: ' + state.sensorTypeMap.name + ' ' + forwardDirection2 + 's ' + settings['levelDelta'] + state.sensorTypeMap.unitType + ' in ' + minutes + ' min.</b>'

    section(hideable: true, hidden: hidden, sectionTitle){
        //display error if !validate
        if(settings['levelDelta']) displayDirectionOption('delta')
        displayError(getMinutesValidationError(settings['relativeMinutes']))
        displayDeltaMinutes()

        width = 10
        fieldName = 'levelDelta'
        fieldTitle = state.sensorTypeMap.name + ' change'
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
            helpTip = 'The ' + sensorPlural + ' is currently at ' + sensorAverage + state.sensorTypeMap.unitType + ', so it would turn the ' + devicePlural + ' on if, within ' + minutes + ' minutes, it were to ' + forwardDirection2 + ' to ' + startLevel + state.sensorTypeMap.unitType + ' (and turn off only when back to at least ' + stopLevel + state.sensorTypeMap.unitType + ').'
        }
        if(!settings['levelDelta']) displayInfo('Enter the number of ' + state.sensorTypeMap.unitType + ' for ' + state.sensorTypeMap.name + ' must ' + forwardDirection2 + ' within ' + minutes + ' minutes to start the ' + devicePlural + ', relative to original level (currently ' + sensorAverage + state.sensorTypeMap.unitType + '). (It will continue to run until back within 10% of the original value.)')

        displayInfo(helpTip)
        if(settings['levelDelta']){
         //   if(settings['direction'] && (settings['levelDelta'] + sensorAverage > 100)) warnMessage = 'The current humidity is ' + sensorAverage + state.sensorTypeMap.unitType + ' so an increase of ' + settings['levelDelta'] + state.sensorTypeMap.unitType + ' (to ' + (settings['levelDelta'] + sensorAverage) + state.sensorTypeMap.unitType + ') is not possible with the current conditions.'
         //   if(!settings['direction'] && (sensorAverage - settings['levelDelta'] < 0)) warnMessage = 'The current humidity is ' + sensorAverage + state.sensorTypeMap.unitType + ' so a decrease of ' + settings['levelDelta'] + state.sensorTypeMap.unitType + ' (to ' + (sensorAverage - settings['levelDelta']) + state.sensorTypeMap.unitType + ') is not possible with the current conditions.'
        }
        displayWarning(warnMessage)
    }
}

def displayDirectionOption(type){
    fieldName = 'direction'
    if(type ==  'threshold') fieldTitle = '<b>Start if ' + state.sensorTypeMap.name + ' is ' + forwardDirection + ' ' + settings['levelThreshold'] + state.sensorTypeMap.unitType + '.</b> Click for ' + reverseDirection + '.'
    
    if(type ==  'delta') fieldTitle = '<b>Start if ' + state.sensorTypeMap.name + ' ' + forwardDirection2 + 's ' + settings['levelDelta'] + state.sensorTypeMap.unitType + ' in ' + minutes + ' minutes.</b> Click for ' + reverseDirection2 + 's.'
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
    if(!settings['controlDevice']) return
    if(!state.sensorTypeMap) return

    hidden = true
    if(settings['startMode'] || settings['stopMode']) hidden = false
    if(settings['startMode'] && settings['stopMode']) hidden = true

    sectionTitle = 'Click to set Mode change (optional)'
    if(settings['startMode']) sectionTitle = '<b>On ' + startText + ': Set Mode to ' + settings['startMode'] + '</b>'
    if(settings['startMode'] && settings['stopMode']) sectionTitle += '<br>'
    if(settings['stopMode']) sectionTitle += '<b>On stop: Set Mode to ' + settings['stopMode'] + '</b>'
    if(settings['startMode'] && !settings['stopMode']) sectionTitle += moreOptions
    if(!settings['startMode'] && settings['stopMode']) sectionTitle += moreOptions
    section(hideable: true, hidden: hidden, sectionTitle){
        input 'startMode', 'mode', title: 'Set Hubitat\'s "Mode" on ' + startText + '?', width: 6, submitOnChange:true
        input 'stopMode', 'mode', title: 'Set Hubitat\'s "Mode" on ' + stopText + '?', width: 6, submitOnChange:true
    }
}

def displayRunTimeOption(){
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
    if(!settings['controlDevice']) return
    if(!state.sensorTypeMap) return
    
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
    message = 'Number of minutes it must run before stopping regardless of ' + state.sensorTypeMap.name + ', to prevent "cycling".'
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
    message = 'Number of minutes to run after which it will stop regardless of ' + state.sensorTypeMap.name + '. (Will not start again for the same duration.)'
    if(settings['startWait'] && !settings['stopWait']) message += ' Note that the start wait time will not affect the maximum run time.'
    if(settings['startWait'] && settings['stopWait']) message += ' Note that neither the start nor stop wait time will not affect the maximum run time.'
    if(!settings['startWait'] && settings['stopWait']) message += ' Note that neither the start nor stop wait time will not affect the maximum run time.'
    if(!settings[fieldName]) displayInfo(message)
}

def displayScheduleSection(){
    //if(!settings['device']) return
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup']) return
    if(!checkAnyDeviceSet()) return
    if(anyErrors) return
    
    section(){}
    
    hidden = true
    if(settings['start_timeType'] && !settings['stop_timeType']) hidden = false
    if(settings['start_time'] && settings['start_time'] == settings['stop_time']) hidden = false
    if(!validateTimes('start')) hidden = false
    if(!validateTimes('stop')) hidden = false

    section(hideable: true, hidden: hidden, getTimeSectionTitle()){
        if(!settings['start_timeType'] && validateTimes('start') && validateTimes('stop') && !settings['days']  && !settings['includeDates'] && !settings['excludeDates']) displayInfo('This will limit when this ' + thisDescription + ' is active. You can create another ' + thisDescription + ' "app" to do something else for opposite times/days.')
        if(settings['start_time'] && settings['start_time'] == settings['stop_time']) displayError('You can\'t have the same time to start and stop.')

        displayTimeTypeOption('start')
        displayTimeOption('start')
        displaySunriseTypeOption('start')
        displayTimeTypeOption('stop')
        displayTimeOption('stop')
        displaySunriseTypeOption('stop')
        displayDaysOption()
        displayDatesOptions()
    }
}

def setLightOptions(){
    if(!settings['advanced']) return
    if(!settings['sensorType']) return
    if(!settings['sensor']) return
    if(!settings['deviceType']) return
    if(!settings['controlDevice']) return
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
    if(settings['startBrightness']) sectionTitle = '<b>On ' + startText + ': Set brightness to ' + settings['startBrightness'] + '%</b>'
    if(settings['startColorTemperature']) {
        if(settings['startBrightness']) sectionTitle += '<br>'
        sectionTitle = '<b>On ' + startText + ': Set color brightness to ' + settings['startColorTemperature'] + 'K</b>'
    }
    if(settings['startHue']) {
        if(settings['startBrightness']) sectionTitle += '<br>'
        sectionTitle = '<b>On ' + startText + ': Set hue to ' + settings['startHue'] + '°</b>'
    }
    if(settings['startSat']) {
        if(settings['startBrightness'] || settings['startHue']) sectionTitle += '<br>'
        sectionTitle = '<b>On ' + startText + ': Set saturation to ' + settings['startSat'] + '%</b>'
    }
    if(settings['startBrightness'] || settings['startColorTemperature'] || settings['startHue'] || settings['startSat']) {
        if(!settings['startBrightness'] || (!settings['startColorTemperature'] || (!settings['startHue'] && !settings['startSat']))) sectionTitle += moreOptions
    }
    return sectionTitle
}

def displayBrightnessOption(){
        width = 10
        fieldName = 'startBrightness'
        fieldTitle = startText.capitalize() + ' brightness:'
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
    fieldTitle = startText.capitalize() + ' color temperature:'
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
    fieldTitle = startText.capitalize() + ' hue:'
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
    fieldTitle = startText.capitalize() + ' saturation:'
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
    if(!settings['controlDevice']) return
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
    if(settings['notificationStartStop'] == 'both') sectionTitle = '<b>On ' + startText + ' and ' + stopText + ', '

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

def buildSensorMap(){
    newMap = []
    newMap.add([name:'Acceleration',attribute:'acceleration',capability:'accelerationSensor',type:'bool',start:'active',stop:'inactive'])
    if(settings['advanced']) newMap.add([name:'Air Quality',attribute:'airQualityIndex',capability:'airQuality',type:'range',start:0,stop:500,unitType:'ppm'])
    if(settings['advanced']) newMap.add([name:'Battery',attribute:'battery',capability:'battery',type:'range',start:0,stop:100,unitType:'%'])
    if(settings['advanced']) newMap.add([name:'Beacon',attribute:'presence',capability:'beacon',type:'bool',start:'present',stop:'not present'])
    if(settings['advanced']) newMap.add([name:'Carbon Dioxide',attribute:'carbonDioxide',capability:'carbonDioxideMeasurement',type:'range',start:0,stop:1000000,unitType:'ppm'])
    if(settings['advanced']) newMap.add([name:'Carbon Monoxide',attribute:'carbonMonoxide',capability:'carbonMonoxideDetector',type:'bool',start:'clear',stop:'detected'])
    newMap.add([name:'Contact (door/window)',attribute:'contact',capability:'contactSensor',type:'bool',start:'open',stop:'closed'])
    if(settings['advanced']) newMap.add([name:'Current',attribute:'amperage',capability:'currentMeter',type:'range',start:0,stop:1000,unitType:'amps'])
    if(settings['advanced']) newMap.add([name:'Door Control',attribute:'door',capability:'doorControl',type:'bool',start:'open',stop:'closed'])
    if(settings['advanced']) newMap.add([name:'Energy',attribute:'energy',capability:'EnergyMeter',type:'range',start:0,stop:100000,unitType:'KWh'])
    if(settings['advanced']) newMap.add([name:'Flow Rate',attribute:'rate',capability:'liquidFlowRate',type:'range',start:0,stop:500,unitType:'gpm or lpm'])
    if(settings['advanced']) newMap.add([name:'Garage Door Control',attribute:'door',capability:'garageDoorControl',type:'bool',start:'open',stop:'closed'])
    if(settings['advanced']) newMap.add([name:'Natural Gas',attribute:'naturalGas',capability:'gasDetector',type:'bool',start:'clear',stop:'detected'])
    newMap.add([name:'Humidity',attribute:'humidity',capability:'relativeHumidityMeasurement',type:'range',start:0,stop:100,unitType:'%'])
    newMap.add([name:'Illuminance',attribute:'illuminance',capability:'illuminanceMeasurement',type:'range',start:0,stop:100000,unitType:'lux']) //range?
    newMap.add([name:'Motion',attribute:'motion',capability:'motionSensor',type:'bool',start:'active',stop:'inactive'])
    newMap.add([name:'Moisture',attribute:'moisture',capability:'waterSensor',type:'bool',start:'wet',stop:'dry'])
    if(settings['advanced']) newMap.add([name:'pH',attribute:'pH',capability:'pHMeasurement',type:'range',start:0,stop:14,unitType:''])
    if(settings['advanced']) newMap.add([name:'Power',attribute:'power',capability:'powerMeter',type:'range',start:0,stop:10000,unitType:'Watts'])
    newMap.add([name:'Presence',attribute:'presence',capability:'presenceSensor',type:'bool',start:'present',stop:'not present'])
    if(settings['advanced']) newMap.add([name:'Pressure',attribute:'pressure',capability:'pressureMeasurement',type:'range',start:0,stop:100000,unitType:'Pa']) //range?
    if(settings['advanced']) newMap.add([name:'Shock',attribute:'shock',capability:'shockSensor',type:'bool',start:'clear',stop:'detected'])
    if(settings['advanced']) newMap.add([name:'Signal Strength (lqi)',attribute:'lqi',capability:'signalStrength',type:'range',start:0,stop:100]) //range?
    if(settings['advanced']) newMap.add([name:'Signal Strength (rssi)',attribute:'rssi',capability:'signalStrength',type:'range',start:0,stop:100]) //range?
    if(settings['advanced']) newMap.add([name:'Sleep',attribute:'sleeping',capability:'sleepSensor',type:'bool',start:'sleeping',stop:'not sleeping'])
    if(settings['advanced']) newMap.add([name:'Smoke',attribute:'smoke',capability:'smokeDetector',type:'bool',start:'clear',stop:'detected'])
    if(settings['advanced']) newMap.add([name:'Sound',attribute:'sound',capability:'soundSensor',type:'bool',start:'clear',stop:'detected'])
    if(settings['advanced']) newMap.add([name:'Sound Volume',attribute:'soundPressureLevel',capability:'soundPressureLevel',type:'range',start:0,stop:200,unitType:'dB'])
    if(settings['advanced']) newMap.add([name:'Steps',attribute:'steps',capability:'stepSensor',type:'range',start:0,stop:100000,unitType:'steps'])
    newMap.add([name:'Switch',attribute:'switch',capability:'switch',type:'bool',start:'on',stop:'off'])
    newMap.add([name:'Temperature',attribute:'temperature',capability:'temperatureMeasurement',type:'range',start:-100,stop:250,unitType:'°' + location.temperatureScale])
    if(settings['advanced']) newMap.add([name:'Ultraviolet Index',attribute:'ultravioletIndex',capability:'ultravioletIndex',type:'range',start:0,stop:100,unitType:'']) //range?
    if(settings['advanced']) newMap.add([name:'Valve',attribute:'valve',capability:'valve',type:'bool',start:'open',stop:'closed'])
    if(settings['advanced']) newMap.add([name:'Voltage',attribute:'voltage',capability:'voltageMeasurement',type:'range',start:0,stop:1000,unitType:'V'])
    if(settings['advanced']) newMap.add([name:'Window Blind (position)',attribute:'position',capability:'windowShade',type:'range',start:0,stop:100,unitType:'%'])
    if(settings['advanced']) newMap.add([name:'Window Blind',attribute:'windowBlind',capability:'windowShade',type:'bool',start:'open',stop:'closed'])
    if(settings['advanced']) newMap.add([name:'Window Shade (position)',attribute:'position',capability:'windowShade',type:'range',start:0,stop:100,unitType:'%'])
    if(settings['advanced']) newMap.add([name:'Window Shade',attribute:'windowShade',capability:'windowShade',type:'bool',start:'open',stop:'closed'])

    return newMap
}

def getSensorsMapEntry(sensorsMap){
    if(!settings['sensorType']) return
    if(!sensorsMap) return
    state.sensorTypeMap = [:]
    sensorsMap.find{sensorLine->
        if(sensorLine.capability == settings['sensorType']) return sensorLine
    }
}

def buildSensorTypeOptionsMap(listMap){
    if(!sensorsMap) return
    newMap = [:]
    listMap.each{capabilityOption->
        sensorsMap.find { it ->
            if (it.capability == capabilityOption) newMap[it.capability] = it.name
        }
    }
    return newMap
}

def getStartText(){
    if(!state.sensorTypeMap) return
    if(state.sensorTypeMap['type'] == 'bool') return 'start (' + state.sensorTypeMap['start'] + ')'
    return 'start'
}

def getStopText(){
    if(!state.sensorTypeMap) return
    if(state.sensorTypeMap['type'] == 'bool') return 'stop (' + state.sensorTypeMap['stop'] + ')'
    return 'stop'

}

def getSensorCount(){
    if(!settings['sensor']) return
    if(!state.sensorTypeMap) return
    count = 0

    settings['sensor'].each{singleDevice->
    attributeString = 'current' + state.sensorTypeMap['attribute'].capitalize()
    if(singleDevice."${attributeString}") count++
        }
    return count
}

def getDeviceCount(){
    if(!settings['controlDevice']) return
    count = 0
    settings['controlDevice'].each{singleDevice->
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
    if(!state.sensorTypeMap) return
    if(!settings['sensor']) return
    if(sensorCount == 0) return
    if(state.sensorTypeMap['type'] == 'bool') return

    total = 0
    attributeString = 'current' + state.sensorTypeMap['attribute'].capitalize()
    settings['sensor'].each{singleDevice->
        total += singleDevice."${attributeString}"
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
    putLog(918,'trace','Installed')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    putLog(924,'trace','Updated')
    unsubscribe()
    initialize()
}

def initialize() {
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    setTime()

	unschedule()        // Reset here or in updated? Would installed also do updated?
    atomicState.remove("startTime")
    atomicState.remove("stopTime")

    if(!atomicState.contactLastNotification) atomicState.contactLastNotification = new Date().getTime() - parent.CONSTHourInMilli() // Why set this? Just test if blank when sending.
    
    setTime()
    sensorsMap = buildSensorMap()
    state.sensorTypeMap = getSensorsMapEntry(sensorsMap)

    if(state.sensorTypeMap['type'] == 'bool') {
        subscribe(settings['sensor'], state.sensorTypeMap['attribute'] + '.' + state.sensorTypeMap['start'], handleSensorStart)
        subscribe(settings['sensor'], state.sensorTypeMap['attribute'] + '.' + state.sensorTypeMap['stop'], handleSensorStop)  
    }
    if(state.sensorTypeMap['type'] == 'range') subscribe(settings['sensor'], state.sensorTypeMap['attribute'], handleSensorUpdate)
    
    if(settings['deviceType'] == 'switch') subscribe(settings['controlDevice'], 'switch', handleStateChange)
    if(settings['deviceType'] == 'light') subscribe(settings['controlDevice'], 'switch', handleStateChange)
    if(settings['deviceType'] == 'color') subscribe(settings['controlDevice'], 'switch', handleStateChange)
    if(settings['deviceType'] == 'lock') subscribe(settings['controlDevice'], 'lock', handleStateChange)
    if(settings['deviceType'] == 'fan') subscribe(settings['controlDevice'], 'switch', handleStateChange)
    
    putLog(955,'trace','Initialized')
}

def handleSensorStart(event) {
    if(atomicState.startTime) return
    if(atomicState.startDelayActive) return
    if(!getActive()) return
    unschedule('performStopAction')
    putLog(963,'debug','Sensor is ' + event.value + '.')
    if(scheduleDelay('start')) return        // if repeadedly triggered (as Start), need to know if delayed schedule; maybe another state var?
    performStartAction()
}

def handleSensorStop(event) {
    if(!atomicState.startTime) return
    if(atomicState.stopDelayActive) return
    unschedule('performStartAction')
    putLog(972,'debug','Sensor is ' + event.value + '.')
    if(scheduleDelay('stop')) return        // if repeadedly triggered (as Stop), need to know if delayed schedule; maybe another state var?
    performStopAction()
}

def handleSensorUpdate(event) {
    atomicState.sensorAverage = getSensorAverage()
    updateSensorDeltaArray()
    if(!checkStartOptions()) return
    if(checkStopOptions()) {
        putLog(982,'error','Both start and stop conditions met.')
        return
    }
    startConditionsMet = checkStartOptions()
    stopConditionsMet = checkStartOptions()
    if(startConditionsMet && stopConditionsMet) {
        putLog(988,'error','Both start and stop conditions met.')
        return
    }
    if(startConditionsMet) {
        if(atomicState.startDelayActive) return
        unschedule('performStopAction')
        if(!scheduleDelay('start')) performStartAction()
        return
    }
    if(stopConditionsMet) {
        if(atomicState.stopDelayActive) return    // If startDelay... return? If it hasn't yet started but already stopped, pretend like it never happened, or ignore any stop until after the delay?
        unschedule('performStartAction')
        if(!scheduleDelay('stop')) performStopAction()
        return
    }
}

def handleStateChange(event) {
    return
    lastChangeAddId = parent.getStateChangeAppId(event.device,app.id,app.label)
    currentState = parent.checkIsOn(event.device,app.label)
    if(lastChangeAddId == app.id && (currentState && event.value == 'on' || !currentState && event.value == 'off')) return
    
    parent.updateTableCapturedState(event.device,event.value,app.label)
    atomicState.startTime = now()
    if(event.value == 'on') scheduleMaximumRunTime()	
    if(event.value == 'off') atomicState.remove("startTime")
    
}

def checkStartOptions(){
    if(atomicState.startTime) return // Already on - would we want it to reset??
    
    if(!getActive()) return
    
    if(state.sensorTypeMap['type'] == 'range') atomicState.sensorStart = atomicState.sensorAverage
    if(!checkStartLevelConditions()) return
    
    if(state.sensorTypeMap['type'] == 'range' && checkStopLevelConditions()) {
        atomicState.startTime = null
        atomicState.remove("startTime")    // Clear stopTime too?
        putLog(1029,'error','Both start and stop conditions met.')
        return
    }
    if(!checkMinimumWaitTime) {
        scheduleMinimumWaitTime()
        putLog(1034,'debug','Sensor would activate, but for minimum wait time from last execution.')
        return
    }
    return true
}

def checkStopOptions(){
    if(!atomicState.startTime) return // Already off
    
    if(!checkStopLevelConditions()) return
    if(state.sensorTypeMap['type'] == 'range' && checkStartLevelConditions()) return

    if(!checkMinimumRunTime()) {
        scheduleMinimumRunTime()
        return
    }

    return true
}

def performStartAction(){
    if(atomicState.startTime) return
    atomicState.startTime = now()
    atomicState.remove('stopTime')
    atomicState.remove('startDelayActive')
    if(settings['startAction'] != 'on' && settings['startAction'] != 'off' && settings['startAction'] != 'toggle') return    // still need to resume. And lock/unlock
    settings['controlDevice'].each{singleDevice->
        // set levels
        stateMap = parent.getStateMapSingle(singleDevice,settings['startAction'],app.id,app.label)
        parent.mergeMapToTable(singleDevice.id,stateMap,app.label)
        putLog(1064,'info','Setting ' + singleDevice + ' to ' + stateMap + ' as sensor Start.')
    }
    parent.setDeviceMulti(settings['controlDevice'],app.label)
    scheduleMaximumRunTime()
}

def performStopAction(){
    if(!atomicState.startTime) return
    atomicState.remove('startTime')
    atomicState.remove('stopDelayActive')
    atomicState.stopTime = now()
    if(settings['stopAction'] != 'on' && settings['stopAction'] != 'off' && settings['stopAction'] != 'toggle') return
    settings['controlDevice'].each{singleDevice->
        stateMap = parent.getStateMapSingle(singleDevice,settings['stopAction'],app.id,app.label)
        parent.mergeMapToTable(singleDevice.id,stateMap,app.label)
        putLog(1079,'info','Setting ' + singleDevice + ' to ' + stateMap + ' as sensor Stop.')
    }
    parent.setDeviceMulti(settings['controlDevice'],app.label)
}

def scheduleDelay(type){
    if(!settings[type + 'Delay']) return
    timeMillis = settings[type + 'Delay'] * parent.CONSTMinuteInMilli()
    if(type == 'stop' && settings['startDelay'] == settings['stopDelay']) timeMillis += 2000        // Add delay for if start and stop trigger at the same "minute" (if allowing second units, change this to a fraction of a second)
    parent.scheduleChildEvent(timeMillis,'','perform' + type.capitalize() + 'Action','',app.id)
    atomicState[type + 'DelayActive'] = true
    return true
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

def checkStartLevelConditions(){
    if(state.sensorTypeMap['type'] == 'bool') return true
    // no multi-conditions?
    if(checkControlDifference()) return true
    if(checkThreshold()) return true
    if(checkDelta()) return true
    return false    // used for log
}

// controlStopManual is not checked

def checkStopLevelConditions(){
    if(state.sensorTypeMap['type'] == 'bool') return true
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
    if(!settings['levelDelta']) return
    if(!settings['relativeMinutes']) return
    if(!atomicState['sensorAverage']) return
    
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

def checkMinimumRunTime(){
    if(!settings['runTimeMinimum']) return true
    if(!state.startTime) return true //??
    if((now() - atomicState.startTime) > settings['runTimeMinimum'] * parent.CONSTMinuteInMilli()) return true
}

def checkMinimumWaitTime(){
    if(!settings['runTimeMinimum']) return true
    if(!atomicState.stopTime) return true
    
    elapsedTime = now() - atomicState.stopTime

    if(elapsedTime < settings['runTimeMinimum'] * parent.CONSTMinuteInMilli()) return
    putLog(1220,'trace','Minimum wait time exceeded.')
    return true
}

def scheduleMaximumRunTime(){
    if(!settings['runTimeMaximum']) return
    if(!atomicState.startTime) return
    unschedule('scheduleMaximumRunTime')
    
    timeMillis = (atomicState.startTime + (settings['runTimeMaximum'] * parent.CONSTMinuteInMilli())) - now()
    parent.scheduleChildEvent(timeMillis,'','performStopActions','',app.id)
}

def scheduleMinimumRunTime(){
    if(!settings['runTimeMinimum']) return true
    unschedule('scheduleMinimumRunTime')
    
    timeMillis = (atomicState.startTime + (settings['runTimeMinimum'] * parent.CONSTMinuteInMilli())) - now()
    if(timeMillis < 0) return true
    parent.scheduleChildEvent(timeMillis,'','checkStartConditions','',app.id)
}

def scheduleMinimumWaitTime(){
    if(!settings['runTimeMinimum']) return true
    unschedule('scheduleMinimumRunTime')
    
    timeMillis = (atomicState.startTime + (settings['runTimeMaximum'] * parent.CONSTMinuteInMilli())) - now()
    if(timeMillis < 0) return true
    parent.scheduleChildEvent(timeMillis,'','checkStartConditions','',app.id)
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
        putLog(1269,'info','Scheduling update subrise/sunset start and/or stop time(s).')
    }
    return true
}

def checkIncludeDates(){
    if(!atomicState.includeDates) return true
    if(!atomicState.includeDates[now().format('yyyy')]) processDates()
    if(atomicState?.includeDates[now().format('yyyy')].contains(now().format('D'))) return true
}
def processDates(){
    atomicState.remove('includeDates')
    if(!settings['days'] && !settings['includeDates'] && !settings['excludeDates']) return
    currentYear = new Date(now()).format('yyyy').toInteger()
    includeDatesValue = settings['includeDates']
    if(!settings['includeDates'] && (settings['days'] || settings['excludeDates'])) includeDatesValue = '1/1-12/31'
    atomicState.'includeDates' = [(currentYear):parent.processDates(settings['includeDates'], settings['excludeDates'], settings['days'], app.id, true)]
}

// Return true if disabled
def getActive(){
    if(settings['ifMode'] && location.mode != settings['ifMode']) return
    
    if(atomicState.scheduleStartTime && atomicState.scheduleStopTime){
        if(!parent.checkNowBetweenTimes(atomicState.scheduleStartTime, atomicState.scheduleStopTime, app.label)) return
    }

    if(settings['personHome']){
        if(!parent.checkPeopleHome(settings['personHome'],app.label)) return
    }
    if(settings['personNotHome']){
        if(!parent.checkNoPeopleHome(settings['personNotHome'],app.label)) return
    }

    return true
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
def buildActionMap(){
    return [['action':'on','actionText':'turn on','descriptionActive':'Turns on', 'description': 'Turn on','type':'on', 'advanced':false],
        ['action':'off', 'actionText':'turn off','descriptionActive':'Turns off', 'description': 'Turn off', 'type':'on', 'advanced':false],
        ['action':'brighten', 'actionText':'brighten','descriptionActive':'Brightens', 'description': 'Brighten', 'type':'dim', 'advanced':false],
        ['action':'dim', 'actionText':'dim','descriptionActive':'Dims', 'description': 'Dim', 'type':'dim', 'advanced':false],
        ['action':'toggle', 'actionText':'toggle','descriptionActive':'Toggles', 'description': 'Toggle', 'type':'other', 'advanced':false],
        ['action':'resume', 'actionText':'resume schedule','descriptionActive':'Resumes schedule (if none, turn off)', 'description': 'Resume schedule (if none, turn off)', 'type':'other', 'advanced':true]]
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
    moreOptions = ' (click for more options)'
    expandText = ' (Click to expand/collapse)'
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
                                     
def displayNameOption(appName){
    displayNameOptionComplete(appName)
    displayNameOptionIncomplete(appName)
}
def displayNameOptionComplete(appName){
    if(!app.label) return
    displayLabel(appName.capitalize() + ' name:',2)
    label title: '', required: false, width: 10,submitOnChange:true
}
def displayNameOptionIncomplete(appName){
    if(app.label) return
    fieldTitle = 'Set name for this ' + appName + ' setup:'
    displayLabel(highlightText(fieldTitle))
    label title: '', width:12, submitOnChange:true
    displayInfo('Name this ' + appName + ' setup. Each ' + appName + ' setup must have a unique name.')
}

// Not used by sensor app (yet)
def displayControllerOption(){
    if(thisApp == 'schedule') return
    if(!app.label) return
    fieldName = 'device'
    resetControllerDevices(fieldName)
    fieldOptions = ''
    if(settings['controllerButtonValue'] == null) app.updateSetting('controllerButtonValue', [type: 'bool', value: 'true'])
    if(settings['controllerButtonValue']){
        fieldOptions = controllerDeviceOptions
        if(parent.getDeviceList() && !fieldOptions) return
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
    if(thisType == 'cube'){
        fieldTitle = 'MagicCube:'
        if(settings[fieldName].size() > 1) fieldTitle = 'MagicCubes:'
    }
    if(thisType == 'pico'){
        fieldTitle = 'Pico controller:'
        if(settings[fieldName].size() > 1) fieldTitle = 'Pico controllers:'
    }

    if(fieldOptions) displaySelectField(fieldName, fieldTitle, fieldOptions, true, true, 'controllerButton')
    if(!fieldOptions) displayDeviceSelectField(fieldName, fieldTitle, 'capability.pushableButton', true, 'controllerButton')
}
def displayControllerOptionIncomplete(fieldName,fieldOptions){
    if(settings[fieldName]) return
            if(thisType == 'pico') displayInfo('Select which Lutron Caseta(s) and/or Pico(s) to control. You can select multiple devices, but all should have the same number of buttons.')
    if(fieldOptions) displayInfo('If you don\'t see the device you want, make sure you have it selected in the Master app.')
            if(thisType == 'pico') fieldTitle = 'Select button device(s) to setup:'
            if(thisType == 'cube') fieldTitle = 'Select MagicCube(s) to setup:'
    if(fieldOptions) displaySelectField(fieldName, fieldTitle, fieldOptions, true, true, 'controllerButton')
    if(!fieldOptions) displayDeviceSelectField(fieldName, fieldTitle, 'capability.pushableButton', true, 'controllerButton')
}
def controllerOptionProcessParentDeviceList(){
    if(!allDeviceOptions) return
    controllerList = [:]
    fullList = [:]
    allDeviceOptions.each{singleDevice->
        if(singleDevice.hasCapability('PushableButton')){
            controllerMatch = controllerOptionProcessParentDeviceListMatch(singleDevice)
            if(controllerMatch) controllerList.put([singleDevice.'id',singleDevice.'label'])
            if(!controllerMatch) fullList.put([singleDevice.'id',singleDevice.'label'])
        }
    }
    return fullList.sort{it.value.toLowerCase()}
}

def displayAdvancedOption(){
    if(anyErrors) return        // Only pico or cube
    fieldName = 'advancedSetup'
    if(!settings['device'] && !settings[fieldName]) return
    if(!settings['customActionsSetup'] && !settings[fieldName] && (thisType == 'pico' || thisType == 'cube')) return
    if(!settings['controlDevice']) return
    
    displayAdvancedOptionEnabled(fieldName)
    displayAdvancedOptionDisabled(fieldName)
}
def displayAdvancedOptionEnabled(fieldName){
    if(!settings[fieldName]) return
    fieldTitleTrue = 'Showing advanced options.'
    displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse, false)
}
def displayAdvancedOptionDisabled(fieldName){
    if(settings[fieldName]) return
    fieldTitleTrue = 'Keeping it simple.'
    fieldTitleFalse = 'Click to show advanced options.'
    displayBoolField(fieldName,fieldTitleTrue,fieldTitleFalse, false)
}

def displayControlDeviceOption(){
    if(!settings['device'] && thisType != 'schedule') return
    if(anyErrors) return
    fieldName = 'controlDevice'
    displayControlDeviceOptionComplete(fieldName)
    displayControlDeviceOptionIncomplete(fieldName)
}
def displayControlDeviceOptionComplete(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = 'Device to control:'
    if(settings[fieldName].size() > 1) fieldTitle = 'Devices to control:'
    capabilitiesType = 'switch'
    if(settings['controlButtonValue'] == 1) capabilitiesType = 'switchLevel'
    if(settings['controlButtonValue'] == 2) capabilitiesType = 'colorMode'

    displayDeviceSelectField(fieldName,fieldTitle,'capability.' + capabilitiesType,true, 'controlButton')
}
def displayControlDeviceOptionIncomplete(fieldName){
    if(settings[fieldName]) return
    fieldTitle = 'Select all device(s) to control with the ' + thisDescription + ':'
    if(settings['device'].size() > 1) fieldTitle = 'Select all device(s) to control with the ' + thisDescription + ':'
    capabilitiesType = 'switch'
    if(settings['controlButtonValue'] == 1) capabilitiesType = 'switchLevel'
    if(settings['controlButtonValue'] == 2) capabilitiesType = 'colorMode'
    displayDeviceSelectField(fieldName,fieldTitle,'capability.' + capabilitiesType,true, 'controlButton')
}

def displayIfModeOption(){
    if(!settings['device'] && thisType != 'schedule') return
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if((thisType == 'pico' || thisType == 'cube') && !checkAnyDeviceSet()) return
    if(anyErrors) return
    
    fieldName = 'ifMode'
    sectionTitle = 'Click to select with what Mode (optional)'
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
    if(!settings['device'] && thisType != 'schedule') return
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return
    if((thisType == 'pico' || thisType == 'cube') && !checkAnyDeviceSet()) return
    if(anyErrors) return

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
    
    if(!settings['personHome'] && !settings['personNotHome']) sectionTitle = 'Click to select with people (optional)'
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
    if(settings['start_timeType'] && !settings['stop_timeType']) return false
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

def getTimeSectionTitle(){
    if(!settings['start_timeType'] && !settings['stop_timeType'] && !settings['days']) return 'Click to set with schedule (optional)'

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

    List dayList=[]
    settings['days'].each{
        dayList.add(it)
    }
    dayText = dayList.join(', ')
    
    if(settings['start_timeType'] && settings['days']) sectionTitle += ' on: ' + dayText
    if(settings['start_timeType']) sectionTitle += '</b>'
    if(!settings['days']) sectionTitle += moreOptions
    
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

def displayTimeTypeOption(type){
    if(type == 'stop' && (!settings['start_timeType'] || !validateTimes('start'))) return
    
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
    if(type == 'stop') fieldList = ['none':'Don\'t stop','time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
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
    
    if(!settings[fieldName]) displayInfo('Select whether scheduling starts exactly at ' + settings[type + '_timeType'] + ' (currently ' + sunTime + '). To allow entering minutes prior to or after ' + settings[type + '_timeType'] + ', select "Before ' + settings[type + '_timeType'] + '" or "After ' + settings[type + '_timeType'] + '". Required.')
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
    timeUnits = 'minutes'
    if(advancedSetup) timeUnits = 'seconds'
    fieldTitle = timeUnits.capitalize() + ' ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType'] + ':'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'number', title: fieldTitle, width: getTypeOptionWidth(type), submitOnChange:true

    message = 'Enter the number of ' + timeUnits + ' ' + settings[type + '_sunType'] + ' ' + settings[type + '_timeType'] + ' for schedule to ' + type + '. Required.'
    if(!settings[type + '_sunOffset']) displayInfo(message)
    if(!validateSunriseMinutes(type)) displayWarning(message)
}

def displayDaysOption(){
    if(thisType == 'schedule' && !settings['start_timeType']) return
    if(!validateTimes('start')) return
    if(!validateTimes('stop')) return

    fieldName = 'days'
    fieldTitle = 'On these days (optional; defaults to all days):'
    if(!settings[fieldName]) fieldTitle = 'On which days (optional; defaults to all days)?'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    options = ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday']
    displaySelectField(fieldName,fieldTitle,options,true,false)
    //input fieldName, 'enum', title: fieldTitle, multiple: true, width: 12, options: ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday'], submitOnChange:true
}

def displayDatesOptions(){
    displayIncludeDates()
    displayExcludeDates()
}

def displayIncludeDates(){
    displayWarning(parent.getDateProcessingErrors(app.id))
    displayInfo(dateList)
    fieldName = 'includeDates'
    fieldTitle = 'Only on dates:'
    if(thisType == 'schedule') fieldTitle = 'Dates on which to run ("include"):'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, "textarea", title: fieldTitle, submitOnChange:true
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
def displayExcludeDates(){
    fieldName = 'excludeDates'
    fieldTitle = 'Not on dates:'
    if(appType == 'schedule') fieldTitle = 'Dates on which to <u>not</u> run ("exclude"):'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, "textarea", title: fieldTitle, submitOnChange:true
    deviceText = 'it'
    if(settings['device'].size() > 1) deviceText = 'them'
    infoTip = 'Enter which date(s) to restrict or exclude this ' + thisDescription + ' routine. "Only on dates" are when this ' + thisDescription + ' will work, for instance if you want ' + deviceText + ' to do a specific thing on Christmas. \
"Not on" dates are when this ' + thisDescription + ' will not apply, for instance to set ' + deviceText + ' to do something any other day. Rules:\n\
	• Year is optional, but would only apply to that <i>one day</i>. If no year is entered, it will repeat annually. \
<i>Example: "12/25/' + (new Date(now()).format('yyyy').toInteger() - 1) + '" will never occur in the future, because that\'s how time works.</i>\n\
	• Enter dates as month/day ("mm/dd") format, or day.month ("dd.mm"). You can also use Julian days of the year as a 3-digit number ("ddd"). \
<i>Example: Christmas could be entered as 12/25, 25.12 or 359 [the latter only true for non-leap years, otherwise 360].</i>\n\
	• Separate multiple dates with a comma (or semicolon). \
<i>Example: "12/25, 1/1" is Christmas and New Year\'s Day.</i>\n\
	• Use a hyphen to indicate a range of dates. \
<i>Example: "12/25-1/6" are the 12 days of Christmas.</i>\n\
    	• The "days" options above will combine with the dates. \
<i>Example: Selecting Monday and entering "12/25" as an "only on" date would only allow the ' + thisDescription + ' if Christmas is on a Monday.</i>\n\
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
    return parent.putLog(lineNumber,type,message,app.label,,getLogLevel())
}