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
*  Version: 0.4.3.17
*
***********************************************************************************************************************/

// TO-DO: Allow showing/hiding info tips. Add a button showing where info tips are at. onClick, set a settings variable to show. Use a div span in the tip. On save, remove the settings variables.
// Should all settings['controllerDevice'] be made controller device, and settings['comparisonDevice'], because Hubitat is retarded and doesn't update the settings variable with app.updateSettings?

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
    //tooltipNumber = 0
    if(!app.label) install = false
    if(app.label){
        install = formComplete()
    }
    thisType = 'sensor'
    thisDescription = 'sensor'
    thisDescriptionPlural = 'sensors'
    page(name: "setup", install: install, uninstall: true) {
        if(!app.label){
            section(){
                displayNameOption()
            }
        } else {
            if(settings['relativeMinutes'] == 0) app.removeSetting('relativeMinutes')
            if(settings['direction'] == null) app.updateSetting('direction', [type: 'bool', value: 'true'])
            allDeviceOptions = parent.getDeviceList()
            controllerDeviceTypeList = getAvailableCapabilitiesList()
            controllerDeviceOptions = controllerOptionProcessParentDeviceList(settings['controllerType'])
            sensorCount = getSensorCount()        // Should be above formComplete, for sensorCount == 0
            sensorAverage = getControllerSensorAverage(sensorCount) 
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
                displayNameOption()
                displayAdvancedOption()
                displayControllerTypeOption()
                displaySensorControllerDeviceOption()        // Not shared function
                displayControlDeviceOption()
            }
            displayThresholdOption()
            displayLevelDeltaOption()
            displayComparisonOption()
            displayRunTimeOption()
            displayMultipleConditions()
            displayScheduleSection()
            displayDaysAndDatesSection()
            displayPeopleOption()
            displayIfModeOption()
            section(){}
            displayActionOption()
            displayDelayOption()
            setLightOptions()
            displayChangeModeOption()
            displayAlertOptions()
        }
    }
}

def formComplete(){
    sensorsMap = buildSensorMap()
    sensorMapEntry = getSensorsMapEntry(sensorsMap)
    if(!app.label) return false
    if(!settings['controllerType']) return false
    if(!settings['controllerDevice']) return false
    if(!settings['controlDevice']) return false
  //  if(settings['startDelay'] && settings['stopDelay'] && settings['startDelay'] > settings['stopDelay']) return false
    if(getThresholdOptionErrors('levelThreshold')) return false
    if(getLevelDeltaOptionErrors('levelDelta')) return false

    if(settings['comparisonDevice'] && !settings['levelComparison']) return false
    if(!settings['comparisonDevice'] && settings['levelComparison']) return false
    if(getMinutesError(settings['relativeMinutes'])) return false
    if(getRunTimeMaximumError('runTimeMinimum')) return false
    if(getRunTimeMaximumError('runTimeMaximum')) return false
    if(!validateLevel(settings['startBrightness'])) return false
    if(!validateTemp(settings['startColorTemperature'])) return false
    if(!validateHue(settings['startHue'])) return false
    if(!validateLevel(settings['startSat'])) return false
    if(peopleError) return false
    return true
}

def clearSettings(){
    if(settings['levelDelta'] == 0) app.removeSetting('levelDelta')
    if(settings['stopDelay'] == 0) app.removeSetting('stopDelay')
    if(settings['relativeMinutes'] == 0) app.removeSetting('relativeMinutes')
    if(settings['runTimeMinimum'] == 0) app.removeSetting('runTimeMinimum')
    if(settings['runTimeMaximum'] == 0) app.removeSetting('runTimeMaximum')
}
def displayControllerTypeOption(){
    if(settings['controllerTypeButtonValue'] == null && allDeviceOptions) atomicState.'controllerTypeButtonValue' = true
    fieldName = 'controllerType'
    displayControllerTypeOptionComplete(fieldName, controllerDeviceTypeList)
    displayControllerTypeOptionIncomplete(fieldName, controllerDeviceTypeList)
}
def displayControllerTypeOptionComplete(fieldName, controllerDeviceTypeList){
    if(!settings[fieldName]) return
    fieldTitle = 'Sensor type:'
    if(allDeviceOptions) displaySelectField(fieldName, fieldTitle, controllerDeviceTypeList, false, true,'controllerTypeButton')
    if(!allDeviceOptions) displaySelectField(fieldName, fieldTitle, controllerDeviceTypeList, false, true)
    if(!sensorMapEntry) displayError('Something went wrong finding the select sensor type - ' + settings[fieldName] + ' in the supported list.')
}
def displayControllerTypeOptionIncomplete(fieldName, controllerDeviceTypeList){
    if(settings[fieldName]) return
    
    if(!controllerDeviceTypeList){
        app.updateSetting('controllerTypeButtonValue', [type: 'bool', value: 'false'])
        controllerDeviceTypeList = getAvailableCapabilitiesList()
        displayWarning('No sensor types available with filtering. Filtering disabled.')
    }
    fieldTitle = 'Select which type of sensor:'
    if(allDeviceOptions) displaySelectField(fieldName, fieldTitle, controllerDeviceTypeList, false, true,'controllerTypeButton')
    if(!allDeviceOptions) displaySelectField(fieldName, fieldTitle, controllerDeviceTypeList, false, true)
    if(!state['controllerTypeButtonValue'] && settings['advancedSetup']) infoMessage = 'All supported sensor types listed.'
    if(!state['controllerTypeButtonValue'] && !settings['advancedSetup']) infoMessage = 'Show Advanced Options for more options.'
    if(state['controllerTypeButtonValue'] && !settings['advancedSetup']) infoMessage = 'Show Advanced Options for more options. Filtering to only show capabilities of devices on your system, of those selected in Master app.'
    if(state['controllerTypeButtonValue'] && settings['advancedSetup']) infoMessage = 'Filtering to only show capabilities of devices on your system, of those selected in Master app.'
    if(!allDeviceOptions && !settings['advancedSetup']) infoMessage = 'Show Advanced Options for more options.'
    if(!allDeviceOptions && settings['advancedSetup']) infoMessage = ''
    displayInfo(infoMessage)
}

def controllerOptionProcessParentDeviceListMatch(singleDevice){
    if(singleDevice.hasCapability(settings['controllerType'].capitalize())) return true
}

def displaySensorControllerDeviceOption(){
    if(!sensorMapEntry) return
    fieldName = 'controllerDevice'
    if(settings[fieldName]){
        settings[fieldName].each{singleDevice->
            if(!checkDeviceAbility(singleDevice)) {
                displayWarning(getDeviceName(singleDevice) + ' does not have any ' + sensorMapEntry.'name' + ' reading.')
            }
        }
    }
    displaySensorControllerDeviceOptionComplete(fieldName)
    displaySensorControllerDeviceOptionIncomplete(fieldName)
}
def displaySensorControllerDeviceOptionComplete(fieldName){
    fieldName = 'controllerDevice'
    if(!settings[fieldName]) return
    displayControllerOption()
    if(sensorCount == 1) helpTip = sensorMapEntry.'name' + ' is currently ' + sensorAverage + sensorMapEntry.'unitType' + '.'
    
    if(sensorCount > 1 && sensorMapEntry.'type' == 'bool') displayInfo('Any one sensor will start/stop routine.')
    if(sensorCount > 1 && sensorMapEntry.'type' != 'bool') {
// if averageButtonValue
        if(!settings['advancedSetup'] && !state['averageButtonValue']) helpTip = 'Average (of ' + sensorCount + ' devices) is currently ' + sensorAverage + sensorMapEntry.'unitType' + '.'
        if(settings['advancedSetup'] && !state['averageButtonValue']) helpTip = 'Using average (currently ' + sensorAverage + sensorMapEntry.'unitType' + ') of ' + sensorCount + ' devices.'
        if(settings['advancedSetup'] || state['averageButtonValue']){
            displayFilterButton('averageButton')
// Should stopping routine be limited to the same sensor that started it??
            if(state['averageButtonValue']) helpTip = 'Any one sensor can start or stop the routine (current average is ' + sensorAverage + sensorMapEntry.'unitType' + ').'
       
            displayInfo(helpTip,false,11)
        }
        if(!settings['advancedSetup'] && !state['averageButtonValue']) displayInfo(helpTip)
    }
}
def displaySensorControllerDeviceOptionIncomplete(fieldName){
    fieldName = 'controllerDevice'
    if(settings[fieldName]) return
    displayControllerOption()
    if(settings['advancedSetup']) displayInfo('Select all sensor devices to be used (including any you may want to use as a comparison device).')
    if(!settings['advancedSetup']) displayInfo('Select all sensor devices to be used. Multiple sensors will be averaged together by default.')
}

// Move delay to it's own section, to apply to everything (Mode, notifications, etc.)
// Needs to be advanced option
def displayDelayOption(){
    if(!settings['controllerType']) return
    if(!controllerDevice) return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup'] && !settings['startDelay'] && !settings['stopDelay']) return
    if(!sensorMapEntry) return
    if(sensorCount == 0) return

    hidden = true
    if(settings['startDelay'] == settings['stopDelay'] && (settings['startDelay'] || settings['stopDelay'])) hidden = false
    if(settings['startDelay'] > settings['stopDelay']) hidden = false
    sectionTitle = 'Click to set start and stop delay time</b> (Optional)'
    if(settings['stopDelay']) sectionTitle = ''
    if(settings['startDelay']) sectionTitle = '<b>Wait ' + settings['startDelay'] + ' minutes to ' + getPlainAction(settings['startAction']) + '</b>'
    if(settings['startDelay'] && settings['stopDelay']) sectionTitle += '\n'
    if(settings['stopDelay'])  sectionTitle += '<b>Wait ' + settings['stopDelay'] + ' minutes to ' + getPlainAction(settings['stopAction']) + '</b>'

    if(settings['startDelay'] && !settings['stopDelay']) sectionTitle += moreOptions
    if(!settings['startDelay'] && settings['stopDelay']) sectionTitle += moreOptions
    section(hideable: true, hidden: hidden, sectionTitle){
        if(settings['startDelay'] || settings['stopDelay']){
            if(settings['startDelay'] == settings['stopDelay']) displayWarning('Since start and stop delay times are the same, the stop delay time will automatically be delayed a fraction of a second to ensure stop occurs after start.')
            warningText = 'Having a stop delay shorter than the start delay may result in unpredicatable results, including a stop action preceding the start action, or a stop action occuring without any start action.'
            if((settings['startDelay'] && settings['stopDelay']) && settings['startDelay'] > settings['stopDelay']) displayWarning(warningText)
            if(settings['startDelay'] && !settings['stopDelay']) displayWarning(warningText)
            warningText = ''
        }
        displayActionDelayOptionFields('start')
        displayActionDelayOptionFields('stop')
    }
}

def displayActionDelayOptionFields(type){
    fieldName = type + 'Delay'
    displayActionDelayOptionCompleted(fieldName,type)
    displayActionDelayOptionIncompleted(fieldName,type)

}
def displayActionDelayOptionCompleted(fieldName,type){
    if(!settings[fieldName]) return
    fieldTitle = type.capitalize() + ' delay minutes:'
    displayError(getMinutesError(settings[fieldName]))
    if(type == 'start' && settings['runTimeMinimum']) {
        if(settings[fieldName] < settings['runTimeMinimum']) displayWaring('The start delay is less then minimum run time. Delay time and minimum run time are from the start trigger, so minimum run time should include delay time.')
    }
    if(settings['runTimeMaximum'] && settings[fieldName] >= settings['runTimeMaximum']) displayWarning('The start action will never occur, because the delay time exceeds the maximum runtime (' + settings['runTimeMaximum'] + ' minutes).')
    displayTextField(fieldName,fieldTitle,'number',false)
}
def displayActionDelayOptionIncompleted(fieldName,type){
    if(settings[fieldName]) return
    helpTip = ''
    if(type == 'start') helpTip = 'Applies to all actions (Mode, notifications, etc.).'
    displayInfo(helpTip)
    fieldTitle = 'Wait time in minutes before performing ' + type + ' actions: (Optional)'
    displayTextField(fieldName,fieldTitle,'number',false)
    if(type == 'stop' && settings['startDelay']) {
        activationText = ''
        if(sensorMapEntry.'type' == 'bool') activationText = '(' + sensorMapEntry.'start' + ')'
        helpTip = 'Note that if the sensor status changes within the delay period, the start/stop action will be ignored (that is, if after starting, the sensor routines toggles back to stop within the start delay period, the delayed start action would be cancelled).'
        displayInfo(helpTip)
    }
}
// Move delay to it's own section, to apply to everything (Mode, notifications, etc.)
// Needs to be advanced option

// Set error message if nothing (including Mode and Notifications)?
def displayActionOption(){
    if(!settings['controllerType']) return
    if(!controllerDevice) return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(!settings['controlDevice']) return
    if(!sensorMapEntry) return
    if(sensorCount == 0) return

    hidden = true
    
    sectionTitle = 'Click to set start and stop action(s)</b> (Optional)'
    if(sensorMapEntry.'type' == 'bool'){
        startText = 'When ' + sensorMapEntry.'attribute' + ' ' + sensorMapEntry.'start' + ': '
        stopText = 'When ' + sensorMapEntry.'attribute' + ' ' + sensorMapEntry.'stop' + ': '
    }
    if(sensorMapEntry.'type' != 'bool'){
        startText = 'Start action: '
        stopText = 'Stop action: '
    }

    if(settings['stopAction']) sectionTitle = ''
    if(settings['startAction']) sectionTitle = '<b>' + startText + getPlainAction(settings['startAction']).capitalize() + '</b>'
    if(settings['startAction'] && settings['stopAction']) sectionTitle += '\n'
    if(settings['stopAction']) sectionTitle += '<b>' + stopText +  getPlainAction(settings['stopAction']).capitalize() + '</b>'

    if(settings['startAction'] && !settings['stopAction']) sectionTitle += moreOptions
    if(!settings['startAction'] && settings['stopAction']) sectionTitle += moreOptions
    section(hideable: true, hidden: hidden, sectionTitle){
        fieldOptions = getActionOptionsList()
        displayActionOptionFields('start',fieldOptions)
        displayActionOptionFields('stop',fieldOptions)
    }
}

def getActionOptionsList(){
    return ['on': 'Turn On', 'off': 'Turn Off', 'toggle': 'Toggle', 'resume':'Resume Schedule (or turn off)']
}

def displayActionOptionFields(type,fieldOptions){
    fieldName = type + 'Action'
    displayActionOptionCompleted(fieldName,type,fieldOptions)
    displayActionOptionIncompleted(fieldName,type,fieldOptions)
}
def displayActionOptionCompleted(fieldName,type,fieldOptions){
    if(!settings[fieldName]) return
    if(type == 'start') typeText = startText
    if(type == 'stop') typeText = stopText
    fieldTitle = typeText.capitalize() + ' action:'
    displaySelectField(fieldName,fieldTitle,fieldOptions,false,true)
}
def displayActionOptionIncompleted(fieldName,type,fieldOptions){
    if(settings[fieldName]) return
    if(type == 'start') typeText = startText
    if(type == 'stop') typeText = stopText
    fieldTitle = 'Action to take on control device on ' + typeText + ':'
    displaySelectField(fieldName,fieldTitle,fieldOptions,false,true)
    if(sensorMapEntry.'type' == 'bool') infoStartText = sensorMapEntry.'start'
    if(sensorMapEntry.'type' != 'bool') infoStartText = 'start'
    if(!settings['startAction'] && !settings['stopAction']) displayInfo('Select what action to take when the ' + sensorMapEntry.'name' + ' reading meets the requirement (as "' + infoStartText + '"), or no longer meets it ("stop"). You will enter the start and stop actions below.')

}

def displayMultipleConditions(){
    if(!settings['controllerType']) return
    if(!controllerDevice) return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(!settings['controlDevice']) return
    if(!sensorMapEntry) return
    if(sensorMapEntry.'type' == 'bool') return
    triggerOptions = 0
    if(settings['levelThreshold']) triggerOptions++
    if(settings['levelDelta']) triggerOptions++
    if(settings['levelComparison']) triggerOptions++
    if(triggerOptions < 2) return

    if(state['multipleOptionsButtonValue']) conjunctionText = ' AND '
    if(!state['multipleOptionsButtonValue']) conjunctionText = ' OR '
    if(infoLevelsText && settings['levelDelta'] && !state['multipleOptionsButtonValue']) infoLevelsText += ' OR '
    if(settings['levelThreshold'] && settings['direction']) infoLevelsText = forwardDirection + ' ' + settings['levelThreshold'] + sensorMapEntry.'unitType'
    if(settings['levelThreshold'] && !settings['direction']) infoLevelsText = reverseDirection + ' ' + settings['levelThreshold'] + sensorMapEntry.'unitType'
    if(infoLevelsText && settings['levelDelta']) infoLevelsText += conjunctionText
    if(settings['levelDelta'] && settings['direction']) infoLevelsText += forwardDirection2 + ' ' + settings['levelDelta'] + sensorMapEntry.'unitType'
    if(settings['levelDelta'] && !settings['direction']) infoLevelsText += reverseDirection2 + ' ' + settings['levelDelta'] + sensorMapEntry.'unitType'
    if(infoLevelsText && settings['levelComparison']) infoLevelsText += conjunctionText
    if(settings['levelComparison'] && settings['direction']) infoLevelsText += settings['levelComparison'] + sensorMapEntry.'unitType' + ' ' + forwardDirection + ' ' + settings['comparisonDevice']
    if(settings['levelComparison'] && !settings['direction']) infoLevelsText += settings['levelComparison'] + sensorMapEntry.'unitType' + ' ' + reverseDirection + ' ' + settings['comparisonDevice']
    
    section(){
        if(!state['multipleOptionsButtonValue']) infoMessage = 'Any of the conditions will start the routine (' + infoLevelsText + ').'
        if(!state['multipleOptionsButtonValue'] && !settings['advancedSetup']) infoMessage += ' Select advanced options to change.'
        if(settings['advancedSetup'] || state['multipleOptionsButtonValue']) {
            displayFilterButton('multipleOptionsButton')
            if(state['multipleOptionsButtonValue']) infoMessage = 'All of the conditions are required to start the routine (' + infoLevelsText + ').'
            displayInfo(infoMessage,false,11)
        }
        if(!settings['advancedSetup'] && !state['multipleOptionsButtonValue']) displayInfo(infoMessage)
    }
}

def displayThresholdOption(){
    if(!settings['controllerType']) return
    if(!controllerDevice) return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(!settings['controlDevice']) return
    if(!sensorMapEntry) return
    if(sensorMapEntry.'type' == 'bool') return
    if(sensorCount == 0) return
    
    hidden = true
    if(getThresholdOptionErrors('levelThreshold')) hidden = false
    if(!settings['levelThreshold']) sectionTitle = 'Click to set ' + sensorMapEntry.'name' + ' threshold</b> (Optional)'
    if(settings['levelThreshold']) sectionTitle = '<b>Run while: ' + sensorMapEntry.'name' + ' ' + forwardDirection + ' ' + settings['levelThreshold'] + sensorMapEntry.'unitType' + '</b>'

    section(hideable: true, hidden: hidden, sectionTitle){
        //display error if !validate
        fieldName = 'levelThreshold'
        displayError(getThresholdOptionErrors(fieldName))
        if(settings[fieldName]) displayDirectionOption('threshold','direction')
        displayThresholdOptionComplete(fieldName)
        displayThresholdOptionIncomplete(fieldName)
    }
}
def displayThresholdOptionComplete(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = 'Threshold:'
  
    displayTextField(fieldName,fieldTitle,'number',false)
}
def displayThresholdOptionIncomplete(fieldName){
    if(settings[fieldName]) return
    fieldTitle = 'Start threshold level:'
    directionText = 'less'
    if(settings['direction']) directionText = 'greater'
    unitsText = ''
    if(sensorMapEntry.'unitTypeText') unitsText = ' (in ' + sensorMapEntry.'unitTypeText' + ')'
    displayInfo('Set the ' + sensorMapEntry.'name' + ' level' + unitsText + ' to start. It will run until it is ' + directionText + ' than the threshold level.')
    displayTextField(fieldName,fieldTitle,'number',false)
}
def getThresholdOptionErrors(fieldName){
    if(!settings[fieldName]) return
    if(sensorMapEntry.'type' == 'bool') return
    if(settings[fieldName] < sensorMapEntry.'start' || settings[fieldName] > sensorMapEntry.'stop') return 'The ' + sensorMapEntry.'name' + ' change must be between ' + sensorMapEntry.'start' + ' and ' + sensorMapEntry.'stop' + '.'
}

def displayLevelDeltaOption(){
    if(!settings['controllerType']) return
    if(!controllerDevice) return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(!settings['controlDevice']) return
    if(!sensorMapEntry) return
    if(sensorMapEntry.'type' == 'bool') return
    if(sensorCount == 0) return

    hidden = true
    fieldName = 'levelDelta'
    errorMessage = getLevelDeltaOptionErrors(fieldName)
    if(errorMessages) hidden = false
    if(settings['relativeMinutes'] && !settings[fieldName]) hidden = false
    if(getMinutesError(settings['relativeMinutes'])) hidden = false
    sectionTitle = 'Click to set ' + sensorMapEntry.'name' + ' ' + forwardDirection2 + ' over time (Optional)'
    minutes = settings['relativeMinutes']
    if(!settings['relativeMinutes']) minutes = '5'
    if(settings[fieldName]) sectionTitle = '<b>Run after: ' + sensorMapEntry.'name' + ' ' + forwardDirection2 + 's ' + settings[fieldName] + sensorMapEntry.'unitType' + ' in ' + minutes + ' min.</b>'
    if(settings['advancedSetup'] && settings[fieldName] && !settings['relativeMinutes']) sectionTitle += moreOptions
    section(hideable: true, hidden: hidden, sectionTitle){
        displayError(errorMessage)
        displayLevelDeltaOptionComplete(fieldName)
        displayLevelDeltaOptionIncomplete(fieldName)
    }
}
def displayLevelDeltaOptionComplete(fieldName){
    if(!settings[fieldName]) return
    displayDirectionOption('delta','direction')
    displayDeltaMinutes('relativeMinutes')
    fieldTitle = sensorMapEntry.'name' + ' change:'
    displayTextField(fieldName,fieldTitle,'decimal',true)
    if(sensorAverage){
        if(settings['direction']) startLevel = sensorAverage + settings[fieldName]
        if(!settings['direction']) startLevel = sensorAverage - settings[fieldName]
        if(settings['direction']) stopLevel = sensorAverage + Math.round(settings[fieldName] / 10)
        if(!settings['direction']) stopLevel = sensorAverage - Math.round(settings[fieldName] / 10)
        pluralText = sensorPlural + ' is'
        if(sensorCount > 1) pluralText = sensorPlural + ' are'
        helpTip = 'The ' + pluralText + ' currently at ' + sensorAverage + sensorMapEntry.'unitType' + ', so it would turn the ' + devicePlural + ' on if, within ' + minutes + ' minutes, ' + sensorMapEntry.'name' + ' were to ' + forwardDirection2 + ' to ' + startLevel + sensorMapEntry.'unitType' + ' (and turn off only when back to at least ' + stopLevel + sensorMapEntry.'unitType' + ').'
    }
    displayInfo(helpTip)
    if(!settings['advancedSetup']) displayInfo('Select Advanced Setup for more options.')
    //   if(settings['direction'] && (settings['levelDelta'] + sensorAverage > 100)) warnMessage = 'The current humidity is ' + sensorAverage + sensorMapEntry.'unitType' + ' so an increase of ' + settings['levelDelta'] + sensorMapEntry.'unitType' + ' (to ' + (settings['levelDelta'] + sensorAverage) + sensorMapEntry.'unitType' + ') is not possible with the current conditions.'
    //   if(!settings['direction'] && (sensorAverage - settings['levelDelta'] < 0)) warnMessage = 'The current humidity is ' + sensorAverage + sensorMapEntry.'unitType' + ' so a decrease of ' + settings['levelDelta'] + sensorMapEntry.'unitType' + ' (to ' + (sensorAverage - settings['levelDelta']) + sensorMapEntry.'unitType' + ') is not possible with the current conditions.'
    displayWarning(warnMessage)
}
def displayLevelDeltaOptionIncomplete(fieldName){
    if(settings[fieldName]) return
    displayError(getMinutesError(settings['relativeMinutes']))
    displayDeltaMinutes('relativeMinutes')
    sensorText = sensorMapEntry.'name' + ' (' + sensorMapEntry.'unitTypeText' + ')'
    if(!sensorMapEntry.'unitType') sensorText = sensorMapEntry.'name'
    fieldTitle = sensorText + ' change within ' + settings['relativeMinutes'] + ' minutes:'
    if(!settings['relativeMinutes']) fieldTitle = sensorText + ' ' + forwardDirection2 + ' within 5 minutes:'
    displayTextField(fieldName,fieldTitle,'decimal',true)
    displayInfo('It will continue to run until back within 10% of the original value.')
    if(!settings['advancedSetup']) displayInfo('Select Advanced Setup for more options.')
}
def getLevelDeltaOptionErrors(fieldName){
    if(!settings[fieldName]) return
    if(sensorMapEntry.'type' == 'bool') return
    if(settings[fieldName] >= (sensorMapEntry.'stop' - sensorMapEntry.'start')) return 'The ' + sensorMapEntry.'name' + ' change must be less than ' + (sensorMapEntry.'stop' - sensorMapEntry.'start') + '.'
}

def displayComparisonOption(){
    resetComparisonDevices('comparisonDevice')
    if(!settings['controllerType']) return
    if(!controllerDevice) return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup'] && !settings['comparisonDevice'] && !settings['levelComparison']) return
    if(!sensorMapEntry) return
    
    if(!app.label) return
    if(settings['controllerDevice'].size() < 2) return
    fieldName = 'comparisonDevice'
    
    fieldOptions = getComparisonDeviceList()
    if(!fieldOptions) {
        putLog(494,'error','Failed building comparison device list (comparisonOptionProcessControlDeviceList).')
        return
    }
    if(fieldOptions) {
        fieldName += 'Id'
    }

    hidden = true
    if(settings['comparisonDevice'] && !settings['levelComparison'] && sensorMapEntry.'type' != 'bool') hidden = false
    if(!settings['comparisonDevice'] && settings['levelComparison'] && sensorMapEntry.'type' != 'bool') hidden = false
    //if(getThresholdOptionErrors('levelThreshold')) hidden = false
    section(hideable: true, hidden: hidden, getComparisonOptionTitle()){
        if(sensorCount == 0) displayError('You can\'t select all devices as comparison devices. Leave at least one unselected (below) to be the primary sensor which will trigger action(s).')
        //display error if !validate
        //displayError(getThresholdOptionErrors(fieldName))
        displayComparisonDeviceOptionComplete(fieldName,fieldOptions)
        displayComparisonDeviceOptionIncomplete(fieldName,fieldOptions)
        fieldName = 'levelComparison'
        if(settings[fieldName] && sensorMapEntry.'type' == 'bool') displayDirectionOption('comparison','direction')
        //displayError(getThresholdOptionErrors(fieldName))
        displayComparisonLevelOptionComplete(fieldName)
        displayComparisonLevelOptionIncomplete(fieldName)
        displayInfo(getComparisonOptionHelptip())
    }
}
def displayComparisonDeviceOptionComplete(fieldName,fieldOptions){
    if(!settings[fieldName]) return
    fieldTitle = 'Control device:'
    displaySelectField(fieldName,fieldTitle,fieldOptions, true, true)
    deviceText = 'sensor is'
}
def displayComparisonDeviceOptionIncomplete(fieldName,fieldOptions){
    if(settings[fieldName]) return
    fieldTitle = 'Select the control sensor (that the primary sensor will be compared to):'
    displaySelectField(fieldName,fieldTitle,fieldOptions, true, true)
    displayInfo('If the sensor you\'d like as control sensor ins\'t listed, add it as a ' + sensorMapEntry.'name' + ' Sensor above.')
}
def getComparisonOptionTitle(){
    if(!settings['levelComparison'] && !settings['comparisonDevice']) return 'Click to set compare to a control sensor (Optional)'
    controllerText = 'sensor(s)'
    if(settings['controllerDevice']) controllerText = 'sensor'
    if(sensorCount > 1) controllerText = 'sensors'
    comparisonText = 'control sensor(s)'
    if(settings['comparisonDevice']) comparisonText = 'control sensor'
    if(settings['comparisonDevice'].size() > 1) comparisonText = 'control sensors'
    levelText = ''
    if(settings['levelComparison']) levelText = settings['levelComparison'] + sensorMapEntry.'unitType' + ' '
    if(sensorMapEntry.'type' == 'bool') directionText = 'different from'
    if(settings['direction'] && sensorMapEntry.'type' == 'range') directionText = 'over'
    if(!settings['direction'] && sensorMapEntry.'type' == 'range') directionText = 'under'

    return '<b>Run while:</b> Primary ' + controllerText + ' is <b>' + levelText + directionText + '</b> ' + comparisonText + '</b>'
}
def getComparisonOptionHelptip(){
    if(!sensorMapEntry) return
    if(!comparisonDevice) return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    controllerText = 'Primary sensor is'
    if(sensorCount > 1) controllerText = 'Primary sensors are'
    comparisonText = 'Control sensor is'
    if(settings['comparisonDevice'].size() > 1) comparisonText = 'Control sensors are'
    if(sensorMapEntry.'type' == 'bool'){
        if(sensorCount == 0) return
        comparisonDeviceText = ''
        settings['comparisonDevice'].each{singleDevice->
            deviceState = singleDevice.('current' + sensorMapEntry.'attribute'.capitalize())
            if(comparisonDeviceText && comparisonDeviceText != deviceState) comparisonDeviceText = 'both ' + comparisonDeviceText + ' and ' + deviceState
            if(!comparisonDeviceText) comparisonDeviceText = deviceState
        }
        controllerDeviceText = ''
        settings['controllerDevice'].each{singleDevice->
            if(!settings['comparisonDeviceId'].contains(singleDevice.id)){
                deviceState = singleDevice.('current' + sensorMapEntry.'attribute'.capitalize())
                if(controllerDeviceText && controllerDeviceText == deviceState) controllerDeviceText = 'both ' + controllerDeviceText + ' and ' + deviceState
                if(!controllerDeviceText) controllerDeviceText = deviceState
            }
        }
        return comparisonText + ' ' + comparisonDeviceText + '. ' + controllerText + ' ' + controllerDeviceText + '.'
    }
    
    return comparisonText + ' average ' + getComparisonSensorAverage() + sensorMapEntry.'unitType' + '. ' + controllerText + ' average ' + sensorAverage + sensorMapEntry.'unitType' + '.'
}
def displayComparisonLevelOptionComplete(fieldName){
    if(!settings[fieldName]) return
    if(!settings['comparisonDevice']) return
    if(sensorMapEntry.'type' == 'bool') return
    displayDirectionOption('comparison','direction')
    if(settings['direction']) fieldTitle = sensorMapEntry.'unitType' + ' over:'
    if(!settings['direction']) fieldTitle = sensorMapEntry.'unitType' + ' under:'
  
    displayTextField(fieldName,fieldTitle,'number',false)
}
def displayComparisonLevelOptionIncomplete(fieldName){
    if(settings[fieldName]) return
    if(!settings['comparisonDevice']) return
    if(sensorMapEntry.'type' == 'bool') return
    if(settings['direction']) fieldTitle = 'Value over ' + settings['comparisonDevice'] + ' to start:'
    if(!settings['direction']) fieldTitle = 'Value under ' + settings['comparisonDevice'] + ' to start:'
    if(settings['comparisonDevice']) displayTextField(fieldName,fieldTitle,'number',true)
    if(!settings['comparisonDevice']) displayTextField(fieldName,fieldTitle,'number',false)
}
//def getThresholdOptionErrors(fieldName){
//    if(!settings[fieldName]) return
//    if(settings[fieldName] < sensorMapEntry.'start' || settings[fieldName] > sensorMapEntry.'stop') return 'The ' + sensorMapEntry.'name' + ' change must be between ' + sensorMapEntry.'start' + ' and ' + sensorMapEntry.'stop' + '.'
//}
def getComparisonDeviceList(){
    if(!settings['controllerDevice']) return
    deviceList = [:]
    settings['controllerDevice'].each{singleDevice->
        deviceList.put([singleDevice.'id',getDeviceName(singleDevice)])
    }
    return deviceList.sort{it.value.toLowerCase()}
}

def displayDirectionOption(type,fieldName){
    if(type ==  'threshold') fieldTitle = 'Start if ' + sensorMapEntry.'name' + ' is <b>' + forwardDirection + '</b> ' + settings['levelThreshold'] + sensorMapEntry.'unitType' + '. Click for ' + reverseDirection + '.'
    if(type ==  'delta') fieldTitle = 'Start if ' + sensorMapEntry.'name' + ' <b>' + forwardDirection2 + 's</b> ' + settings['levelDelta'] + sensorMapEntry.'unitType' + ' in ' + minutes + ' minutes. Click for ' + reverseDirection2 + 's.'
    if(type ==  'comparison') fieldTitle = 'Start if [insert primary] is  ' + settings['levelComparison'] + sensorMapEntry.'unitType' + ' <b>' + forwardDirection + '</b> ' + settings['comparisonDevice'] + '. Click for ' + reverseDirection + '.'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'bool', title: fieldTitle, submitOnChange:true
}

def displayDeltaMinutes(fieldName){
    if(!settings['advancedSetup'] && !settings[fieldName]) return
    displayError(getMinutesError(settings['relativeMinutes']))
    displayDeltaMinutesComplete(fieldName)
    displayDeltaMinutesIncomplete(fieldName)
}
def displayDeltaMinutesComplete(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = 'Interval (minutes):'
    displayTextField(fieldName,fieldTitle,'number',false)
}
def displayDeltaMinutesIncomplete(fieldName){
    if(settings[fieldName]) return
    fieldTitle = 'Minutes between change (Optional: default 5)'
    displayTextField(fieldName,fieldTitle,'number',false)
}

def displayChangeModeOption(){
    if(!settings['controllerType']) return
    if(!controllerDevice) return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(!settings['controlDevice']) return
    if(!sensorMapEntry) return
    if(sensorCount == 0) return

    hidden = true
    if(settings['startMode'] || settings['stopMode']) hidden = false
    if(settings['startMode'] && settings['stopMode']) hidden = true

    sectionTitle = 'Click to set Mode change (Optional)'
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
    if(!settings['controllerType']) return
    if(!controllerDevice) return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(!settings['controlDevice']) return
    if(!sensorMapEntry) return
    if(sensorCount == 0) return
    
    hidden = true
    if(settings['runTimeMinimum'] && settings['runTimeMaximum'] && settings['runTimeMinimum'] >= settings['runTimeMaximum']) hidden = false
    if(settings['runTimeMinimum'] && !settings['runTimeMaximum']) hidden = false
    if(!settings['runTimeMinimum'] && settings['runTimeMaximum']) hidden = false
    if(getMinutesError(settings['runTimeMinimum'])) hidden = false
    if(getMinutesError(settings['runTimeMaximum'])) hidden = false

    sectionTitle = ''
    if(!settings['runTimeMinimum'] && !settings['runTimeMaximum']) sectionTitle = 'Click to set run time (Optional)'
    if(settings['runTimeMinimum']) sectionTitle =  '<b>Minimum run time: ' + settings['runTimeMinimum'] + ' min.</b>'
    if(settings['runTimeMinimum'] && settings['runTimeMaximum']) sectionTitle += '<br>'
    if(settings['runTimeMaximum']) sectionTitle += '<b>Maximum run time: ' + settings['runTimeMaximum'] + ' min.</b>'
    if(settings['runTimeMinimum'] && !settings['runTimeMaximum']) sectionTitle += moreOptions
    if(!settings['runTimeMinimum'] && settings['runTimeMaximum']) sectionTitle += moreOptions

    section(hideable: true, hidden: hidden, sectionTitle){
        displayError(getRunTimeMaximumError('runTimeMinimum'))
        displayError(getRunTimeMaximumError('runTimeMaximum'))
        if(settings['runTimeMinimum'] && settings['runTimeMaximum'] && settings['runTimeMinimum'] == settings['runTimeMaximum']) displayWarning('Having the same maximum and minimum run time will force it to stop at ' + settings['runTimeMinimum'] + ' minutes. Setting just maximum run time (with no other requirements to stop) would accomplish the same thing (or, set a delay time).')

        displayRunTimeMinimum()
        displayRunTimeMaximum()
        //displayRunTimeMaximumManual()
    }
}

def displayRunTimeMinimum(){
    if(!getMinutesError(settings['runTimeMinimum']) && getMinutesError(settings['runTimeMaximum'])) return
    fieldName = 'runTimeMinimum'
    displayRunTimeMinimumCompleted(fieldName)
    displayRunTimeMinimumIncompleted(fieldName)
}
def displayRunTimeMinimumCompleted(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = 'Minimum minutes:'
    displayTextField(fieldName,fieldTitle,'number',false)
}
def displayRunTimeMinimumIncompleted(fieldName){
    if(settings[fieldName]) return
    fieldTitle = 'Run for at least (minutes):'
    displayTextField(fieldName,fieldTitle,'number',false)
    message = 'Number of minutes it must run before stopping regardless of ' + sensorMapEntry.'name' + ', to prevent "cycling".'
    displayInfo(message)
    message = ''
    if(settings['startDelay'] && !settings['stopDelay']) message += ' Note that the start delay time will not affect the maximum run time (that is, the minimum run time is from when the start conditions are met, not when the start actions are performed.)'
    if(settings['startDelay'] && settings['stopDelay']) message += ' Note that minimum run time is not affected by start and stop delay times (that is, the minimum run time is from when the start/stop conditions are met, not when the start/stop actions are performed.)'
    if(!settings['startDelay'] && settings['stopDelay']) message += ' Note that stop delay time will not affect the maximum run time (that is, the minimum run time is from when the stop conditions are met, not when the stop actions are performed.)'
    displayInfo(message)
}

def displayRunTimeMaximum(){
    if(getMinutesError(settings['runTimeMinimum'])) return
    fieldName = 'runTimeMaximum'
    displayRunTimeMaximumCompleted(fieldName)
    displayRunTimeMaximumIncompleted(fieldName)
}
def displayRunTimeMaximumCompleted(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = 'Maximum minutes:'
    if(settings['startDelay'] && settings['startDelay'] >= settings[fieldName]) displayWarning('The start action will never occur, because the delay time (' + settings['startDelay'] + ' minutes) exceeds the maximum runtime (' + settings[fieldName] + ' minutes).')
    displayTextField(fieldName,fieldTitle,'number',false)
    message = ''
    if(settings['startDelay'] && !settings['stopDelay']) message += ' Note that the start delay time will not be affected by the maximum run time.'
    if(settings['startDelay'] && settings['stopDelay']) message += ' Note that minimum run time is not affected by start and stop delay times.'
    if(!settings['startDelay'] && settings['stopDelay']) message += ' Note that stop delay time will not be affected by the maximum run time.'
    displayInfo(message)
}
def displayRunTimeMaximumIncompleted(fieldName){
    if(settings[fieldName]) return
    fieldTitle = 'Run for no more than (minutes):'
    displayTextField(fieldName,fieldTitle,'number',false)
    message = 'Number of minutes to run after which it will stop regardless of ' + sensorMapEntry.'name' + '. (Will not start again for the same duration.)'
    displayInfo(message)
    message = ''
    if(settings['startDelay'] && !settings['stopDelay']) message += ' Note that the start delay time will not affect the maximum run time.'
    if(settings['startDelay'] && settings['stopDelay']) message += ' Note that minimum run time is not affected by start and stop delay times.'
    if(!settings['startDelay'] && settings['stopDelay']) message += ' Note that stop delay time will not affect the maximum run time.'
    displayInfo(message)
}
def getRunTimeMaximumError(fieldName){
    if(!settings[fieldName]) return
    if(getMinutesError(settings['fieldName'])) return getMinutesError(settings['fieldName'])
    if(fieldName == 'runTimeMinimum' && settings['runTimeMaximum'] && settings['runTimeMinimum'] > settings['runTimeMaximum']) return 'Minimum run time must be greater than maximum run time.'
}

def displayScheduleSection(){
    if(!settings['controllerType']) return
    if(!controllerDevice) return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(!settings['controlDevice']) return
    if(!settings['advancedSetup'] && !settings['start_timeType'] && !settings['stop_timeType']) return
    if(sensorCount == 0) return
    
    section(){}
    
    hidden = true
    if(settings['start_timeType'] && !settings['stop_timeType']) hidden = false
    if(settings['start_time'] && settings['start_time'] == settings['stop_time']) hidden = false
    if(!validateTimes('start')) hidden = false
    if(!validateTimes('stop')) hidden = false

    section(hideable: true, hidden: hidden, getTimeSectionTitle()){
        if(!settings['start_timeType'] && validateTimes('start') && validateTimes('stop') && !settings['days']  && !settings['includeDates'] && !settings['excludeDates']) displayInfo('This will limit when this ' + thisDescription + ' is active. For instance, to set a door contact sensor to turn on a light only at night, you could set start time as sunset and stop time as sunrise.')
        if(settings['start_time'] && settings['start_time'] == settings['stop_time']) displayWarning('There is no reason for having start and stop time the same; ' + thisDescription + ' apps are active all the time by default.')

        displayTimeTypeOption('start')
        displayTimeOption('start')
        displaySunriseTypeOption('start')
        displayTimeTypeOption('stop')
        displayTimeOption('stop')
        displaySunriseTypeOption('stop')
    }
}

def setLightOptions(){
    if(!settings['advancedSetup'] && !settings['startBrightness'] && !settings['stopBrightness'] && !settings['startColorTemperature'] && !settings['stopColorTemperature'] && !settings['startHue'] && !settings['stopHue'] && !settings['startSat'] && !settings['stopSat']) return
    if(!settings['controllerType']) return
    if(!controllerDevice) return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(!settings['controlDevice']) return
    if(!parent.checkIsDimmableMulti(settings['controlDevice'],app.id)) return
    if(sensorCount == 0) return

    sectionTitle = getLightOptionsSectionTitle()
    // brightness, color and temp levels section title

    hidden = true
    //if !validate brightness, color or temp, hidden = false
    section(hideable: true, hidden: hidden, sectionTitle){
        if(!validateLevel(settings['startBrightness'])) displayError('Brightness must be between 1 and 100.')
        if(!validateTemp(settings['startColorTemperature'])) displayError('Color temperature must be between 1800 and 5400.')
        if(!validateHue(settings['startHue'])) displayError('Color temperature must be between 1 and 360.')
        if(!validateLevel(settings['startSat'])) displayError('Saturation must be between 1 and 100.')

        // display error if !validate
        displayBrightnessOption()
        displayColorTemperatureOption()
        displayHueOption()
        displaySatOption()
    }
}

def getLightOptionsSectionTitle(){
    sectionTitle = 'Click to set light options (Optional)'
    if(settings['startBrightness']) sectionTitle = '<b>On ' + startText + ': Set brightness to ' + settings['startBrightness'] + '%</b>'
    if(settings['startColorTemperature']) {
        if(settings['startBrightness']) sectionTitle += '<br>'
        sectionTitle = '<b>On ' + startText + ': Set color temperature to ' + settings['startColorTemperature'] + 'K</b>'
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
        if(!settings['startBrightness'] || (!settings['startColorTemperature'] && (!settings['startHue'] || !settings['startSat']))) sectionTitle += moreOptions
    }
    return sectionTitle
}

def displayBrightnessOption(){
    if(!settings['advancedSetup'] && !settings['startBrightness']) return
    if(!parent.checkIsDimmableMulti(settings['controlDevice'],app.id)) return
    fieldName = 'startBrightness'
    displayBrightnessOptionCompleted(fieldName)
    displayBrightnessOptionIncompleted(fieldName)
}
def displayBrightnessOptionCompleted(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = startText.capitalize() + ' brightness:'
    displayTextField(fieldName,fieldTitle,'number',false)
}
def displayBrightnessOptionIncompleted(fieldName){
    if(settings[fieldName]) return
    fieldTitle = 'Set brightness on ' + startText.capitalize() + ':'
    displayTextField(fieldName,fieldTitle,'number',false)
}

def displayColorTemperatureOption(){
    if(!settings['advancedSetup'] && !settings['startColorTemperature']) return
    if(!parent.checkIsTempMulti(settings['controlDevice'],app.id)) return
    fieldName = 'startColorTemperature'
    displayColorTemperatureOptionCompleted(fieldName)
    displayColorTemperatureOptionIncompleted(fieldName)

}
def displayColorTemperatureOptionCompleted(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = startText.capitalize() + ' color temperature:'
    displayTextField(fieldName,fieldTitle,'number',false)
    displayInfo('Only color temperature or hue/saturation can be set. 3000 is warm white, 4000 is cool white, and 5000 is daylight.')
}
def displayColorTemperatureOptionIncompleted(fieldName){
    if(settings[fieldName]) return
    fieldTitle = 'Set color temperature on ' + startText.capitalize() + ':'
    displayTextField(fieldName,fieldTitle,'number',false)
    displayInfo('Only color temperature or hue/saturation can be set, not both. Color temperature is from 1800 to 5400 where lower is more yellow and higher is more blue; 3000 is warm white, 4000 is cool white, and 5000 is daylight.')
}

def displayHueOption(){
    if(!settings['advancedSetup'] && !settings['startHue'] && !settings['startSat']) return
    if(settings['startColorTemperature']) return
    if(!parent.checkIsColorMulti(settings['controlDevice'],app.id)) return
    fieldName = 'startHue'
    displayHueOptionCompleted(fieldName)
    displayHueOptionIncompleted(fieldName)

}
def displayHueOptionCompleted(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = startText.capitalize() + ' hue:'
    displayTextField(fieldName,fieldTitle,'number',false)
    displayInfo('Red = 1 (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270.')
}
def displayHueOptionIncompleted(fieldName){
    if(settings[fieldName]) return
    fieldTitle = 'Set hue on ' + startText.capitalize() + ':'
    displayTextField(fieldName,fieldTitle,'number',false)
    
    displayInfo('Hue is degrees from 1 to 360 around a color wheel, where red is 1 (and 360). Orange = 29; yellow = 58; green = 94; turquiose = 180; blue = 240; purple = 270 (may vary by device).')
}

def displaySatOption(){
    if(!settings['advancedSetup'] && !settings['startHue'] && !settings['stopHue'] && !settings['startSat'] && !settings['stopSat']) return
    if(settings['startColorTemperature']) return
    if(!parent.checkIsColorMulti(settings['controlDevice'],app.id)) return
    fieldName = 'startSat'
    displaySatOptionCompleted(fieldName)
    displaySatOptionIncompleted(fieldName)
}
def displaySatOptionCompleted(fieldName){
    if(!settings[fieldName]) return
    fieldTitle = startText.capitalize() + ' saturation:'
    displayTextField(fieldName,fieldTitle,'number',false)
}
def displaySatOptionIncompleted(fieldName){
    if(settings[fieldName]) return
    fieldTitle = 'Set saturation on ' + startText.capitalize() + ':'
    displayTextField(fieldName,fieldTitle,'number',false)
    displayInfo('Saturation is the percent of color, as opposed to white. Lower numbers will appear more washed out, and higher numbers more vibrant.')
}

def displayAlertOptions(){
    if(!settings['advancedSetup']) return
    if(!settings['controllerType']) return
    if(!controllerDevice) return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(!settings['controlDevice']) return
    if(!parent.pushNotificationDevice && !parent.speechDevice) return
    if(sensorCount == 0) return

    hidden = true
    if(settings['pushNotification']) hidden = false
    if(settings['speech']) hidden = false
    if((settings['pushNotification'] || settings['speech']) && !settings['notificationStartStop']) hidden = false

    // Get push notification device(s) from parent (if applicable)
    if(parent.pushNotificationDevice){
        state.pushFilteredList = [:]
        countPushDevices = 0
        parent.pushNotificationDevice.each{
            pushDeviceId = it.id
            state.pushFilteredList[pushDeviceId] = getDeviceName(it)
            countPushDevices++
                }
        if(countPushDevices == 1) {
            settings['pushNotificationDevice'] = [:]
            settings['pushNotificationDevice'][getDeviceName(it)] = pushDeviceId
        }
    }

    // Get speech device(s) from parent (if applicable)
    if(parent.speechDevice){
        state.speechFilteredList = [:]
        countSpeechDevices = 0
        parent.speechDevice.each{
            speechDeviceId = it.id
            state.speechFilteredList[speechDeviceId] = getDeviceName(it)
            countSpeechDevices++
                }
        if(countSpeechDevices == 1) {
            settings['speechDevice'] = [:]
            settings['speechDevice'][getDeviceName(it)] = speechDeviceId
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
    
    if(!settings['speech'] && !settings['pushNotification']) sectionTitle = 'Click to send notifications (Optional)'

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

def resetControllerDevices(deviceName){
    if(!deviceName) return
    app.removeSetting(deviceName)
    setDeviceById(deviceName + 'Id', deviceName,sensorMapEntry.'capability')
}
def setDeviceById(deviceIdName, deviceName,capability){
//This doesn't work for comparison device
    if(!settings[deviceIdName]) return
    if(!(parentDeviceList = parent.getDeviceList())) return
    newVar = []
    settings[deviceIdName].each{deviceId->
        parentDeviceList.find{singleDevice->
            if(singleDevice.id == deviceId){
                newVar.add(singleDevice)
            }
        }
    }
    app.updateSetting(deviceName, [type: 'capability.' + capability, value: newVar])
}
def resetComparisonDevices(deviceName){
    if(!deviceName) return
    app.removeSetting(deviceName)
    setComparisonDeviceById(deviceName + 'Id', deviceName,settings['controllerType'])
}
def setComparisonDeviceById(deviceIdName, deviceName,capability){
//This doesn't work for comparison device
    if(!settings[deviceIdName]) return
    if(!settings['controllerDevice']) return
    newVar = []
    settings[deviceIdName].each{deviceId->
        settings['controllerDevice'].find{singleDevice->
            if(singleDevice.id == deviceId){
                newVar.add(singleDevice)
            }
        }
    }
    app.updateSetting(deviceName, [type: 'capability.' + capability, value: newVar])
}

def buildSensorMap(){
    // get network status attribute(s)
    return [[name:'Acceleration',attribute:'acceleration',capability:'accelerationSensor',type:'bool',start:'active',stop:'inactive',advanced:false],
            [name:'Air Quality',attribute:'airQualityIndex',capability:'airQuality',type:'range',start:0,stop:500,unitType:'ppm',unitTypeText:'parts per million',advanced:true],
            [name:'Battery',attribute:'battery',capability:'battery',type:'range',start:0,stop:100,unitType:'%',unitTypeText:'percent',advanced:true],
            [name:'Beacon',attribute:'presence',capability:'beacon',type:'bool',start:'present',stop:'not present',advanced:true],
            [name:'Carbon Dioxide',attribute:'carbonDioxide',capability:'carbonDioxideMeasurement',type:'range',start:0,stop:1000000,unitType:'ppm',unitTypeText:'parts per million',advanced:true],
            [name:'Carbon Monoxide',attribute:'carbonMonoxide',capability:'carbonMonoxideDetector',type:'bool',start:'clear',stop:'detected',advanced:true],
            [name:'Contact (door/window)',attribute:'contact',capability:'contactSensor',type:'bool',start:'open',stop:'closed',advanced:false],
            [name:'Current',attribute:'amperage',capability:'currentMeter',type:'range',start:0,stop:1000,unitType:'a',unitTypeText:'amps',advanced:true],
            [name:'Door Control',attribute:'door',capability:'doorControl',type:'bool',start:'open',stop:'closed',advanced:true],
            [name:'Energy',attribute:'energy',capability:'EnergyMeter',type:'range',start:0,stop:100000,unitType:'KWh',unitTypeText:'Kilowatt hours',advanced:true],
            [name:'Flow Rate',attribute:'rate',capability:'liquidFlowRate',type:'range',start:0,stop:500,unitType:'gpm or lpm',unitTypeText:'gallons/liters per minute',advanced:true],
            [name:'Garage Door Control',attribute:'door',capability:'garageDoorControl',type:'bool',start:'open',stop:'closed',advanced:true],
            [name:'Natural Gas',attribute:'naturalGas',capability:'gasDetector',type:'bool',start:'clear',stop:'detected',advanced:true],
            [name:'Humidity',attribute:'humidity',capability:'relativeHumidityMeasurement',type:'range',start:0,stop:100,unitType:'%',unitTypeText:'percent',advanced:false],
            [name:'Illuminance',attribute:'illuminance',capability:'illuminanceMeasurement',type:'range',start:0,stop:5000,unitType:'lux',unitTypeText:'lux',advanced:false],
            [name:'Motion',attribute:'motion',capability:'motionSensor',type:'bool',start:'active',stop:'inactive',advanced:false],
            [name:'Moisture',attribute:'moisture',capability:'waterSensor',type:'bool',start:'wet',stop:'dry',advanced:false],
            [name:'Network Status',attribute:'online',capability:'networkStatus',type:'bool',start:'online',stop:'offline',advanced:false],
            [name:'pH',attribute:'pH',capability:'pHMeasurement',type:'range',start:0,stop:14,unitType:'',unitTypeText:'',advanced:true],
            [name:'Power',attribute:'power',capability:'powerMeter',type:'range',start:0,stop:10000,unitType:'W',unitTypeText:'Watts',advanced:true],
            [name:'Presence',attribute:'presence',capability:'presenceSensor',type:'bool',start:'present',stop:'not present',advanced:false,advanced:true],
            [name:'Pressure',attribute:'pressure',capability:'pressureMeasurement',type:'range',start:0,stop:100000,unitType:'Pa',unitTypeText:'Pascals',advanced:true],
            [name:'Shock',attribute:'shock',capability:'shockSensor',type:'bool',start:'clear',stop:'detected',advanced:true],
            [name:'Signal Strength (lqi)',attribute:'lqi',capability:'signalStrength',type:'range',start:0,stop:255,advanced:true],
            [name:'Signal Strength (rssi)',attribute:'rssi',capability:'signalStrength',type:'range',start:0,stop:255,advanced:true],
            [name:'Sleep',attribute:'sleeping',capability:'sleepSensor',type:'bool',start:'sleeping',stop:'not sleeping',advanced:true],
            [name:'Smoke',attribute:'smoke',capability:'smokeDetector',type:'bool',start:'clear',stop:'detected',advanced:true],
            [name:'Sound',attribute:'sound',capability:'soundSensor',type:'bool',start:'clear',stop:'detected',advanced:true],
            [name:'Sound Volume',attribute:'soundPressureLevel',capability:'soundPressureLevel',type:'range',start:0,stop:150,unitType:'dB',unitTypeText:'decibels',advanced:true],
            [name:'Steps',attribute:'steps',capability:'stepSensor',type:'range',start:0,stop:100000,unitType:'steps',unitTypeText:'steps',advanced:true],
            [name:'Switch',attribute:'switch',capability:'switch',type:'bool',start:'on',stop:'off',advanced:false],
            [name:'Temperature',attribute:'temperature',capability:'temperatureMeasurement',type:'range',start:-100,stop:250,unitType:'°' + location.temperatureScale,unitTypeText:'degrees ' + location.temperatureScale,advanced:false],
            [name:'Ultraviolet Index',attribute:'ultravioletIndex',capability:'ultravioletIndex',type:'range',start:1,stop:11,unitType:'',unitTypeText:'',advanced:true],
            [name:'Valve',attribute:'valve',capability:'valve',type:'bool',start:'open',stop:'closed',advanced:true],
            [name:'Voltage',attribute:'voltage',capability:'voltageMeasurement',type:'range',start:0,stop:1000,unitType:'v',unitTypeText:'volts',advanced:true],
            [name:'Window Blind (position)',attribute:'position',capability:'windowShade',type:'range',start:0,stop:100,unitType:'%',unitTypeText:'percent',advanced:true],
            [name:'Window Blind',attribute:'windowBlind',capability:'windowShade',type:'bool',start:'open',stop:'closed',advanced:true],
            [name:'Window Shade (position)',attribute:'position',capability:'windowShade',type:'range',start:0,stop:100,unitType:'%',unitTypeText:'percent',advanced:true],
            [name:'Window Shade',attribute:'windowShade',capability:'windowShade',type:'bool',start:'open',stop:'closed',advanced:true]]
}

// Creates a list of devices capabilities 
// by matching the abilities of any device in the allDevices setting
// against a list of all supported sensor types provided in sensorsMap
// sensorsMap is a list of maps [[attribute: 'motion', name: 'Motion Sensor'], [attribute: 'contact', name: 'Contact Sensor']] etc (with other key:values)
// It could return attribute:name pairs
def getAvailableCapabilitiesList(){
    matchList = [:]
    notMatchList = []

    if(!state['controllerTypeButtonValue']){    // If filtering disabled, add all sensor types
        for(int sensorsMapLoop = 0; sensorsMapLoop < sensorsMap.size();sensorsMapLoop++){
            if(matchList.containsKey(sensorsMap[sensorsMapLoop].capability)) continue
            matchList[sensorsMap[sensorsMapLoop].capability] = buildAvailableCapabilitiesListEntry(sensorsMap[sensorsMapLoop])
        }
        return returnAvailableCapabilitiesList(matchList)
    }

    allDeviceOptions.each {singleDevice ->
        for(int capabilitiesLoop = 0; capabilitiesLoop < singleDevice.capabilities.size();capabilitiesLoop++){
            if(notMatchList.contains(singleDevice.capabilities[capabilitiesLoop].name)) continue        // Uncapitalizing adds significant processing time, so check notMatch
            capabilityName = singleDevice.capabilities[capabilitiesLoop].name.uncapitalize()
            if(matchList.containsKey(capabilityName)) continue
            sensorsMapEntry = sensorsMap.find{it.capability == capabilityName}
            if(!sensorsMapEntry) {
                notMatchList += singleDevice.capabilities[capabilitiesLoop].name
                continue
        }
            if(!advancedSetup && sensorsMapEntry.advanced) continue
            matchList[capabilityName] = buildAvailableCapabilitiesListEntry(sensorsMapEntry)
        }
    }
    return returnAvailableCapabilitiesList(matchList)
}
def returnAvailableCapabilitiesList(matchList){
    if(!matchList) return
    if(!settings['controllerType'] && matchList.containsKey(settings['controllerType'])) matchList[settings['controllerType']] = sensorsMap.find{it.capability == settings['controllerType']}.name // Add the current controller type (mostly if advanced options changed)
    return matchList.sort { it.value.toLowerCase() }
}
def buildAvailableCapabilitiesListEntry(sensorsMapEntry){
    if(sensorsMapEntry.type == 'bool') return sensorsMapEntry.name + ' (' + sensorsMapEntry.start + '/' + sensorsMapEntry.stop + ')'
    if(sensorsMapEntry.unitType) return sensorsMapEntry.name + ' (' + sensorsMapEntry.unitTypeText + ')'
    if(sensorsMapEntry.type != 'bool' && !sensorsMapEntry.unitType) return sensorsMapEntry.name
}

def getSensorsMapEntry(sensorsMap){
    if(!controllerType) return
    if(!sensorsMap) return
    sensorMapEntry = [:]
    sensorsMap.find{sensorLine->
        if(sensorLine.capability == controllerType) return sensorLine
    }
}

def buildControllerTypeOptionsMap(listMap){
    if(!sensorsMap) return
    newMap = [:]
    listMap.each{capabilityOption->
        sensorsMap.find { it ->
            if (it.capability == capabilityOption) {
                if(settings['advancedSetup'] || it.'advanced') {
                    newMap[it.capability] = it.name
                }
            }
        }
    }
    return newMap
}

def getStartText(){
    if(!sensorMapEntry) return
    if(sensorMapEntry.'type' == 'bool') return 'start (' + sensorMapEntry.'start' + ')'
    return 'start'
}

def getStopText(){
    if(!sensorMapEntry) return
    if(sensorMapEntry.'type' == 'bool') return 'stop (' + sensorMapEntry.'stop' + ')'
    return 'stop'

}

// Gets count by attribute (ie active)
def getSensorCount(){
    if(!controllerDevice) return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(!sensorMapEntry && !atomicState.sensorMapEntry) return
    if(atomicState.sensorMapEntry && !sensorMapEntry) sensorMapEntry = atomicState.sensorMapEntry
    count = 0

    settings['controllerDevice'].each{singleDevice->
        attributeString = 'current' + sensorMapEntry.'attribute'.capitalize()
        if(checkDeviceAbility(singleDevice) && !(settings['comparisonDeviceId'] && settings['comparisonDeviceId'].contains(singleDevice.id.toString()))) count++
    }
    return count
}

def checkDeviceAbility(singleDevice){
    if(!singleDevice) return false
    if(!sensorMapEntry.'attribute') return false
    attributeString = 'current' + sensorMapEntry.'attribute'.capitalize()
    if(singleDevice?."${attributeString}") return true
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
    if(!settings['controllerDevice']) return 'sensor(s)'
    if(settings['controllerDevice'].size() > 1) return 'sensors'
    return 'sensor'
}

def getPluralDevice(){
    if(!getDeviceCount()) return 'device(s)'
    if(getDeviceCount() > 1) return 'devices'
    return 'device'
}

def getControllerSensorAverage(sensorCount){
    if(!sensorMapEntry && !atomicState.sensorMapEntry) return
    if(!controllerDevice) return        // Can't be settings because... Hubitat is retarded and doesn't update the settings variable with app.updateSettings
    if(sensorCount == 0) return

    if(atomicState.sensorMapEntry && !sensorMapEntry) sensorMapEntry = atomicState.sensorMapEntry
    if(sensorMapEntry.'type' == 'bool') return
    total = 0
    attributeString = 'current' + sensorMapEntry.'attribute'.capitalize()
    
    settings['controllerDevice'].each{singleDevice->
        if(!(settings['comparisonDeviceId'] && settings['comparisonDeviceId'].contains(singleDevice.id.toString()))) {
            if(checkDeviceAbility(singleDevice)) total += singleDevice."${attributeString}"
        }
    }
    return Math.round(total / sensorCount)
}

def getComparisonSensorAverage(){
    if(!sensorMapEntry && !atomicState.sensorMapEntry) return
    if(!settings['comparisonDevice']) return

    if(atomicState.sensorMapEntry && !sensorMapEntry) sensorMapEntry = atomicState.sensorMapEntry
    if(sensorMapEntry.'type' == 'bool') return
    total = 0
    attributeString = 'current' + sensorMapEntry.'attribute'.capitalize()
    settings['comparisonDevice'].each{singleDevice->
        total += singleDevice."${attributeString}"
    }
    return Math.round(total / settings['comparisonDevice'].size())
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
    putLog(1294,'trace','Installed')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    putLog(1300,'trace','Updated')
    unsubscribe()
    initialize()
}

def initialize() {
    putLog(1306,'trace','^')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    atomicState.remove('startTime')
    atomicState.remove('startLevel')
    atomicState.remove('stopTime')
    atomicState.remove('startDelayActive')
    atomicState.remove('priorValue')
    atomicState.remove('sensorChanges') // Needs to be removed if changing the number of sensors

    if(settings['levelDelta']) {
        if(settings['relativeMinutes']) atomicState.relativeMinutes = settings['relativeMinutes']
        if(!settings['relativeMinutes']) atomicState.relativeMinutes = 5
    }
	unschedule()        // Reset here or in updated? Would installed also do updated?

    if(!atomicState.contactLastNotification) atomicState.contactLastNotification = new Date().getTime() - parent.CONSTHourInMilli() // Why set this? Just test if blank when sending.
    
    sensorsMap = buildSensorMap()
    atomicState.sensorMapEntry = getSensorsMapEntry(sensorsMap)

    if(atomicState.sensorMapEntry.'type' == 'bool') {
        subscribe(settings['controllerDevice'], atomicState.sensorMapEntry.'attribute' + '.' + atomicState.sensorMapEntry.'start', handleSensorUpdate)
        subscribe(settings['controllerDevice'], atomicState.sensorMapEntry.'attribute' + '.' + atomicState.sensorMapEntry.'stop', handleSensorUpdate)
    }
    if(atomicState.sensorMapEntry.'type' == 'range') subscribe(settings['controllerDevice'], atomicState.sensorMapEntry.'attribute', handleSensorUpdate)
    
    setTime()
    
    subscribe(settings['controlDevice'], 'switch', handleStateChange)
    
    putLog(1336,'trace','¬')
}

// Somewhat duplicated in handleScheduleStart and handleScheduleStop
def handleSensorUpdate(event) {
    
    putLog(1342,'info','^')
    unschedule('handleScheduleStop')
    unschedule('handleScheduleStart')
    
    if(!state.sensorMapEntry.'type') {
        putLog(1347,'error','No sensor type defined, which means the setup is somehow incorrect. Try resaving it.')
        return
    }
    
    if(settings['comparisonDeviceId'] && settings['comparisonDeviceId'].contains(event.device.id.toString())) return
    if(state.sensorMapEntry.'type' == 'range'){
        if(!state['averageButtonValue']) atomicState.sensorAverage = getControllerSensorAverage(getSensorCount())
        if(state['averageButtonValue']) atomicState.sensorAverage = event.value.toInteger()
        updateSensorDeltaArray(event.device.id)
        if(settings['comparisonDevice']) atomicState.comparisonSensorAverage = getComparisonSensorAverage()
    }
    
    startConditionsMet = checkStartOrStopCondititions('start',event.device)
    stopConditionsMet = checkStartOrStopCondititions('stop',event.device)
    
    if(startConditionsMet && stopConditionsMet) {
        if(atomicState.startTime) startConditionsMet = false
        if(!atomicState.startTime) stopConditionsMet = false
    }
    
    if(startConditionsMet) {
        putLog(1368,'trace','Start conditions met.')
        performStart(event.value,event.device.id)
    }
    if(stopConditionsMet) {
        putLog(1372,'trace','Stop conditions met.')
        performStop(event.device.id)
    }
    
    putLog(1376,'info','^')
}

// With start, returns true to start, false to not start
// With stop, returns false to stop, true to not stop
def checkStartOrStopCondititions(startType,singleDevice){
    if(!singleDevice) return    // log error
    levelValue = singleDevice.('current' + state.sensorMapEntry.'attribute'.capitalize())
    if(state.sensorMapEntry.'type' == 'bool') {
        if(levelValue == state.sensorMapEntry."${startType}") return true
        return false
    }
    if(!settings['multipleOptionsButtonValue']){
        if(checkThresholdValue(startType)) return true
        if(checkDeltaValue(startType,deviceId,levelValue)) return true
        if(checkControlValue(startType)) return true
        return false
    }
    if(checkThresholdValue(startType) && checkDeltaValue(startType,levelValue) && checkDeltaValue(startType)) true
}
def checkThresholdValue(startType){
    if(settings['multipleOptionsButtonValue'] && !settings['levelThreshold']) return true
    if(!settings['levelThreshold']) return false
    
    if(settings['direction'] && atomicState.sensorAverage > settings['levelThreshold']) returnValue = true
    if(!settings['direction'] && atomicState.sensorAverage < settings['levelThreshold']) returnValue = true
    if(startType == 'start') return returnValue
    if(startType == 'stop') return !returnValue
}
def checkDeltaValue(startType,deviceId,levelValue){
    if(settings['multipleOptionsButtonValue'] && !settings['levelDelta']) return true
    if(!settings['levelDelta']) return false
    
    if(startType == 'stop'){
// Need to add fudge factor for Delta - if goes up 10 degrees, stop after lowering it 8
        if(settings['direction'] && levelValue <= atomicState.startLevel) return true
        if(!settings['direction'] && levelValue >= atomicState.startLevel) return true
        return false
    }
    //if startType == 'start'
    if(!atomicState.relativeMinutes) return
    if(!atomicState.sensorAverage) return
    startDelta = false
    if(state['averageButtonValue'] && settings['controllerDevice'].size() > 1) sensorChangeList = atomicState.sensorChanges[deviceId]
    if(!state['averageButtonValue'] || settings['controllerDevice'].size() == 1) sensorChangeList = atomicState.sensorChanges
    
    if(!sensorChangeList) return
    sensorChangeList.reverseEach{
        if(!returnValue){
            if(settings['direction']) difference = atomicState.sensorAverage - it.value['sensor']
            if(!settings['direction']) difference = it.value['sensor'] - atomicState.sensorAverage
            if(difference > settings['levelDelta']){
                atomicState.priorValue = it.value['sensor']
                returnValue = true
            }
        }
    }
    if(returnValue){
        if(state['averageButtonValue'] && settings['controllerDevice'].size() > 1) {
            newArray = atomicState.sensorChanges
            newArray.remove(singleDeviceId)
            atomicState.sensorChanges = newArray        // Convoluted bullshit because Hubitat doesn't properly support dictionaries in state variables
        }
        if(!state['averageButtonValue'] || settings['controllerDevice'].size() == 1) atomicState.remove('sensorChanges')
    }
    return returnValue
}
def checkControlValue(startType){
    if(settings['multipleOptionsButtonValue'] && !settings['levelComparison']) return true
    if(!settings['levelComparison']) return false
    
    if(settings['direction'] && atomicState.sensorAverage > atomicState.comparisonSensorAverage + settings['levelComparison']) returnValue = true
    if(!settings['direction'] && atomicState.sensorAverage < atomicState.comparisonSensorAverage - settings['levelComparison']) returnValue = true
    if(startType == 'start') return returnValue
    if(startType == 'stop') return !returnValue
}
def performStart(levelValue,deviceId){
    if(!getActive()) return
    if(atomicState.startTime) return        // May need to come up with a solution for if only starting?
    if(!state.sensorMapEntry) return       // log error...?
    if(!checkMinimumWaitTime()){
        scheduleMinimumWaitTime(deviceId)
        return
    }

    unschedule('performStopAction')
    atomicState.remove('stopTime')
    atomicState.startTime = now()
    if(state.sensorMapEntry.'type' == 'range' && settings['levelDelta']) atomicState.startLevel = levelValue
    if(state.sensorMapEntry.'type' == 'range') atomicState.sensorStart = atomicState.sensorAverage
    
    if(!scheduleDelay('start',deviceId)) performStartActions(levelValue)
}
// Called from performStart and scheduleDelay
def performStartActions(levelValue){
    scheduleMaximumRunTime()
    unschedule('performStopAction')
    atomicState.remove('stopTime')
    atomicState.startTime = now()
    if(state.sensorMapEntry.'type' == 'range') atomicState.startLevel = levelValue
    if(state.sensorMapEntry.'type' == 'range') atomicState.sensorStart = atomicState.sensorAverage
// Mode Change
// Notifications
    if(settings['startAction'] == 'resume')  {
        putLog(1480,'trace','[' + singleDevice + '] attempting schedule resume')
        parent.resumeDeviceScheduleMulti(settings['controlDevice'],app.id)
        return
    }
    if(settings['stopAction'] != 'on' && settings['stopAction'] != 'off' && settings['stopAction'] != 'toggle') return
    settings['controlDevice'].each{singleDevice->
        // set levels
        stateMap = parent.getStateMapSingle(singleDevice,settings['startAction'],app.id)
        parent.mergeMapToTable('state',singleDevice.id,stateMap,app.id)
        putLog(1489,'info','[' + singleDevice + '] sensor Start ' + stateMap)
    }
    parent.setDeviceMulti(settings['controlDevice'],app.id)
}
// Called from scheduleMaximumRunTime?
def performStop(deviceId){
    if(!atomicState.startTime) return        // Leave this for when scheduled (if it changes prior to schedule triggering)

    if(!checkMinimumRunTime()){
        scheduleMinimumRunTime(deviceId)
        return
    }
    if(!scheduleDelay('stop')) performStopActions()
}
// Called from performStop and scheduleDelay
def performStopActions(levelValue = ''){        // levelValue is sent by scheduleDelay (for caompatibility with 'start')
    atomicState.remove('startTime')
    atomicState.remove('startLevel')
    atomicState.remove('sensorStart')
    atomicState.remove('stopDelayActive')
    
    unschedule('scheduleDelay')
    unschedule('performStartAction')
    atomicState.stopTime = now()
// Mode Change
// Notifications
    if(settings['stopAction'] != 'on' && settings['stopAction'] != 'off' && settings['stopAction'] != 'toggle') return
    settings['controlDevice'].each{singleDevice->
        stateMap = parent.getStateMapSingle(singleDevice,settings['stopAction'],app.id)
        parent.mergeMapToTable('state',singleDevice.id,stateMap,app.id)
        putLog(1519,'info','[' + singleDevice + '] sensorStop ' + stateMap)
    }
    parent.setDeviceMulti(settings['controlDevice'],app.id)
}

// Called with minimumWaitTime
def handleScheduleStart(deviceId){
    settings['controllerDevice'].find{it->
        if(it.id == deviceId) singleDevice = it
    }
    if(!singleDevice) {
        // log error
        return
    }
    levelValue = singleDevice.'current' + state.sensorMapEntry.'attribute'.capitalize()
    if(!checkStartOrStopCondititions('start',singleDevice)) return
    performStartAction(levelValue)
}
// Called with minimumRunTime (if stop is triggered beforehand) and maximumRunTime
def handleScheduleStop(deviceId){
    settings['controllerDevice'].find{it->
        if(it.id == deviceId) singleDevice = it
    }
    if(!singleDevice) {
        // log error
        return
    }
    levelValue = singleDevice.'current' + state.sensorMapEntry.'attribute'.capitalize()
    if(!checkStartOrStopCondititions('stop',singleDevice)) return
    performStopAction(levelValue)
}

// Called when control device changes
def handleStateChange(event) {
    return
    lastChangeAddId = parent.getStateChangeAppId(event.device,app.id)
    currentState = parent.checkIsOn(event.device,app.id)
    if(lastChangeAddId == app.id && (currentState && event.value == 'on' || !currentState && event.value == 'off')) return
    
    parent.updateTableCapturedState(event.device,event.value,app.id)
    atomicState.startTime = now()
    if(event.value == 'on') scheduleMaximumRunTime()	
    if(event.value == 'off') atomicState.remove("startTime")
    
}

def updateSensorDeltaArray(singleDeviceId){
    backupArray = [:]
    //atomicState.remove('sensorChanges')
    if(!settings['levelDelta']) {
        atomicState.remove('sensorChanges')
        return
    }
    if(state['averageButtonValue'] && settings['controllerDevice'].size() > 1) {
        if(!atomicState.sensorChanges) atomicState.sensorChanges = [:]
        if(atomicState.sensorChanges[singleDeviceId]) sensorChanges = atomicState.sensorChanges[singleDeviceId]
    }
    if(!state['averageButtonValue'] || settings['controllerDevice'].size() == 1) sensorChanges = atomicState.sensorChanges
    atomicState.remove('sensorChanges')

    itemCount = 0
    newArray = [:]
    timeLimit = now() - (atomicState.relativeMinutes * parent.CONSTMinuteInMilli())
    sensorChanges.each{
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
    newArray[itemCount] = [time:now(),sensor:atomicState.sensorAverage]
    if(state['averageButtonValue'] && settings['controllerDevice'].size() > 1) {
        if(atomicState.sensorChanges) backupArray = atomicState.sensorChanges
        backupArray[singleDeviceId] = newArray
        atomicState.sensorChanges = backupArray        // Convoluted bullshit because Hubitat doesn't properly support dictionaries in state variables
    }
    if(!state['averageButtonValue'] || settings['controllerDevice'].size() == 1) atomicState.sensorChanges = newArray
}

def checkMinimumRunTime(){
    if(!settings['runTimeMinimum']) return true
    if(!state.startTime) return true //??
    if((now() - atomicState.startTime) > settings['runTimeMinimum'] * parent.CONSTMinuteInMilli()) return true
}

def checkMinimumWaitTime(){
// Need a way to check is already set
    if(!settings['runTimeMinimum']) return true
    if(!atomicState.stopTime) return true
    
    elapsedTime = now() - atomicState.stopTime

    if(elapsedTime < settings['runTimeMinimum'] * parent.CONSTMinuteInMilli()) return
    putLog(1625,'trace','Minimum wait time exceeded.')
    return true
}

// Schedule to turn off immdiately when timer ends
def scheduleMaximumRunTime(){
    if(!settings['runTimeMaximum']) return
    if(!atomicState.startTime) return
    unschedule('performStop')
    
    timeMillis = (atomicState.startTime + (settings['runTimeMaximum'] * parent.CONSTMinuteInMilli())) - now()
    putLog(1636,'trace','Scheduled off in ' + timeMillis + 'ms as max runtime')
    parent.scheduleChildEvent(timeMillis,'','performStop','',app.id)
}

// Schedule to check if should stop when timer expires (rather than wait for a sensor update)
def scheduleMinimumRunTime(deviceId){
    if(!settings['runTimeMinimum']) return true
    unschedule('performStopAction')
    
    timeMillis = (atomicState.startTime + (settings['runTimeMinimum'] * parent.CONSTMinuteInMilli())) - now()
    if(timeMillis < 0) return true
    parent.scheduleChildEvent(timeMillis,'','handleScheduleStop',deviceId,app.id)
}

// Schedule to check if should (re)start when timer expires (rather than wait for a sensor update)
// Just reinitialize? Do we need averages from prior to a wait time?
def scheduleMinimumWaitTime(deviceId){
    if(!atomicState.startTime) return
    if(!settings['runTimeMaximum']) return true
    unschedule('handleScheduleStop')    // ?? Not 100% sure on this
    
    timeMillis = (atomicState.startTime + (settings['runTimeMaximum'] * parent.CONSTMinuteInMilli())) - now()
    if(timeMillis < 0) return true

    parent.scheduleChildEvent(timeMillis,'','handleScheduleStart',deviceId,app.id)
}

def scheduleDelay(type,deviceId = ''){
    if(!settings[type + 'Delay']) return
    timeMillis = settings[type + 'Delay'] * parent.CONSTMinuteInMilli()
    if(type == 'stop' && settings['startDelay'] == settings['stopDelay']) timeMillis += 500        // Add delay for if start and stop trigger at the same "minute" (if allowing second units, change this to a fraction of a second)
    if(type == 'start') parent.scheduleChildEvent(timeMillis,'','perform' + type.capitalize() + 'Actions',deviceId,app.id)
    if(type == 'stop') parent.scheduleChildEvent(timeMillis,'','perform' + type.capitalize() + 'Actions','',app.id)
    putLog(1669,'trace','Delaying ' + type + ' action ' + settings[type + 'Delay'] + ' minutes')
    //atomicState[type + 'DelayActive'] = true
    return true
}

def setScheduleFromParent(timeMillis,scheduleFunction,scheduleParameters = null){
    runInMillis(timeMillis,scheduleFunction,scheduleParameters)
}

// Return true if disabled
def getActive(){
    if(settings['ifMode'] && location.mode != settings['ifMode']) return
    
    if(atomicState.scheduleStartTime && atomicState.scheduleStopTime){
        if(!parent.checkNowBetweenTimes(atomicState.scheduleStartTime, atomicState.scheduleStopTime,app.id)) return
    }

    if(settings['personHome']){
        if(!parent.checkPeopleHome(settings['personHome'],app.id)) return
    }
    if(settings['personNotHome']){
        if(!parent.checkNoPeopleHome(settings['personNotHome'],app.id)) return
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
        putLog(2489,'error','Schedule error with starting time.')
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
