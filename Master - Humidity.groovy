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
*  Name: Master - Humidity
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master%20-%20Humidity.groovy
*  Version: 0.4.2.3
*
***********************************************************************************************************************/

definition(
    name: "Master - Humidity",
    namespace: "master",
    author: "roguetech",
    description: "Humidity Sensors",
    parent: "master:Master",
    category: "Convenience",
    importUrl: "https://raw.githubusercontent.com/roguetech2/hubitat/master/Master%20-%20Humidity.groovy",
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

def getMaxTemp(){
    return 250
}
def getMinTemp(){
    return -50
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
            deviceCount = getDeviceCount(settings['device'])
            getSensorType()
            infoText = getSensorText()
            pluralControl = getPluralControlSensor()
            pluralHumidity = getPluralHumiditySensor()
            pluralTemp = getPluralTempSensor()
            pluralFan = getPluralFan()
            controlInfoText  = getControlSensorText()
            duplicateSensors = compareDeviceLists(settings['humiditySensor'],settings['tempSensor'])
            duplicateControls = compareDeviceLists(settings['humidityControlSensor'],settings['tempControlSensor'])
            if(humidityActive) humidityAverage = humidityAverage(settings['humiditySensor'],settings['tempSensor'])
            if(humidityActive) humidityAverage = humidityAverage + "%"
            if(tempActive) tempAverage = tempAverage(settings['tempSensor'],settings['humiditySensor'])
            if(tempActive) tempAverage = tempAverage + '°' + getTemperatureScale()
            peopleError = compareDeviceLists(personHome,personNotHome)

            section(){
                displayNameOption()
                displayDeviceTypeOption()
                if(install) displayDisableOption()
                displayHumidityDeviceOption()
                displayTempDeviceOption()
                displayDeviceOption()
                displayMultiTrigger()
            }
            displayControlDeviceOption()
            displayAbsoluteThresholdOption('humidity')
            displayAbsoluteThresholdOption('temp')
            displayLevelRelativeChangeOption('humidity')
            displayLevelRelativeChangeOption('temp')
            displayRunTimeOption()
            displayAlertOptions()
            displayPeopleOption()
            displayScheduleSection()
            displayIfModeOption()
        }
    }
}

def formComplete(){
    if(!app.label) return false
    if(settings['sensorType'] == 'humidityOnly' && !settings['humiditySensor']) return false
    if(settings['sensorType'] == 'tempOnly' && !settings['tempSensor']) return false
    if(settings['sensorType'] == 'both' && !settings['tempSensor'] && !settings['humiditySensor']) return false
    if(!device) return false
    if((humidityControlSensor || tempControlSensor) && (!controlStartDifference && !controlStopDifference)) return false
    if(controlStartDifference > 100) return false
    if(controlStopDifference > 100) return false
    if(humidityStartThreshold > 101) return false
    if(humidityStopThreshold > 101) return false
    if(humidityStartDelta > 100) return false
    if(humidityStartDelta == 0) return false
    if(tempStartDelta > 100) return false
    if(tempStartDelta == 0) return false
    if(humidityIncreaseRate && humidityIncreaseRate < 0) return false
    if(runTimeMinimum && runTimeMaximum && runTimeMinimum >= runTimeMaximum) return false
    if(compareDeviceLists(humiditySensor,humidityControlSensor)) return false
    if(compareDeviceLists(tempSensor,tempControlSensor)) return false
    if(settings['tempStartThreshold'] && settings['tempStartThreshold'] == settings['tempStopThreshold']) return false
    if(settings['humidityStartThreshold'] && settings['humidityStartThreshold'] == settings['humidityStopThreshold']) return false
    if(start_timeType == "time" && !start_time) return false
    if(stop_timeType == "time" && !stop_time) return false
    if((start_timeType == "sunrise" || start_timeType == "sunset") && !start_sunType) return false
    if((stop_timeType == "sunrise" || stop_timeType == "sunset") && !stop_sunType) return false
    if((start_sunType == "before" || start_sunType == "after") && !start_sunOffset) return false
    if((stop_sunType == "before" || stop_sunType == "after") && !stop_sunOffset) return false
    if(start_timeType && !stop_timeType) return false
    if(!start_timeType && stop_timeType) return false
    if(settings['pushNotificationDevice'] && settings['pushNotification'] && !settings['notificationStartStop']) return false
    if(settings['speechDevice'] && settings['speech'] && !settings['notificationStartStop']) return false
    return true
}

def validatePercent(value){
    return parent.validateLevel(value,app.label)
}


def validateHumidity(value){
    if(value == 0) return 'Minimum humidity is 1.'
    if(!value) return
    if(value > 100) return 'Humidity cannot be more than 100.'
}

def validateTemp(value){
    if(!value) return
    if(validateMinTemp(value)) return true
    if(validateMaxTemp(value)) return true
}

def validateMaxTemp(value){
    if(!value) return
    if(value > getMaxTemp()) return 'Temperature must be less than ' + getMaxTemp() + typeUnit + '.'
}

def validateMinTemp(value){
    if(!value) return
    if(value < getMinTemp()) return 'Temperature must be greater than ' + getMinTemp() + typeUnit + '.'
}

def validateMinutes(value){
    if(value == 0) return 'Minimum minutes is 1.'
    if(!value) return
    if(value > 1440) return 'Maximum minutes is 1,440 (24 hours).'
    if(value < 1) return 'Minimum minutes is 1.'
}

def getHumiditySensorCount(){
    if(!settings['sensorType']) return 0
    if(settings['sensorType'] == 'tempOnly') return 0
    if(settings['sensorType'] == 'humidityOnly') {
        if(settings['humiditySensor']) return settings['humiditySensor'].size()
        return 0
    }
    
    deviceCount = 0
    if(settings['humiditySensor']) deviceCount = settings['humiditySensor'].size()

    if(settings['tempSensor']){
        settings['tempSensor'].each{singleDevice->
            if(singleDevice.hasCapability('RelativeHumidityMeasurement')) deviceCount++
                }
    }
    return deviceCount
}

def getHumidityControlSensorCount(){
    if(!settings['sensorType']) return 0
    if(settings['sensorType'] == 'tempOnly') return 0
    if(settings['sensorType'] == 'humidityOnly') {
        if(settings['humidityControlSensor']) return settings['humidityControlSensor'].size()
        return 0
    }
    
    deviceCount = 0
    if(settings['humidityControlSensor']) deviceCount = settings['humidityControlSensor'].size()

    if(settings['tempControlSensor']){
        settings['tempControlSensor'].each{singleDevice->
            if(singleDevice.hasCapability('RelativeHumidityMeasurement')) deviceCount++
                }
    }
    return deviceCount
}

def getTempSensorCount(){
    if(!settings['sensorType']) return 0
    if(settings['sensorType'] == 'humidityOnly') return 0
    if(settings['sensorType'] == 'tempOnly') {
        if(settings['tempSensor']) return settings['tempSensor'].size()
        return 0
    }
    
    deviceCount = 0
    if(settings['tempSensor']) deviceCount = settings['tempSensor'].size()

    if(settings['humiditySensor']){
        settings['humiditySensor'].each{singleDevice->
            if(singleDevice.hasCapability('TemperatureMeasurement')) deviceCount++
                }
    }
    return deviceCount
}

def getTempControlSensorCount(){
    if(!settings['sensorType']) return 0
    if(settings['sensorType'] == 'humidityOnly') return 0
    if(settings['sensorType'] == 'tempOnly') {
        if(settings['tempControlSensor']) return settings['tempControlSensor'].size()
        return 0
    }
    
    deviceCount = 0
    if(settings['tempControlSensor']) deviceCount = settings['tempControlSensor'].size()

    if(settings['humidityControlSensor']){
        settings['humidityControlSensor'].each{singleDevice->
            if(singleDevice.hasCapability('TemperatureMeasurement')) deviceCount++
                }
    }
    return deviceCount
}

def getCountFieldsStart(){
    count = 0
    if(settings['controlStartDifference']) count++
        if(settings['humidityStartThreshold']) count++
            if(settings['tempStartThreshold']) count++
                if(settings['humidityStartDelta']) count++
                    if(settings['tempStartDelta']) count++
                        return count
}

def getCountFieldsStop(){
    count = 0
    if(settings['controlStopDifference']) count++
    if(settings['humidityStopThreshold']) count++
    if(settings['tempStartThreshold']) count++
    if(settings['humidityStopDelta']) count++
    if(settings['tempStopDelta']) count++
    if(settings['tempStopPercent']) count++
                        return count
}

def getDeviceCount(device){
    if(!settings['device']) return 0
    return settings['device'].size()
}

def getControlDeviceCount(){
    if(!settings['humidityControlSensor'] && !settings['tempControlSensor']) return 0
    
    if(settings['humidityControlSensor'] && !settings['tempControlSensor']) return settings['humidityControlSensor'].size()
    if(!settings['humidityControlSensor'] && settings['tempControlSensor']) return settings['tempControlSensor'].size()
    return settings['tempControlSensor'].size() + settings['humidityControlSensor'].size()
}

def getPluralControlSensor(){
    count = getControlDeviceCount()
    if(!count) return 'sensor(s)'
    if(count > 1) return 'sensors'
    return 'sensor'
}
       
def getPluralTempSensor(){
    if(!tempSensorCount) return 'temperature sensor(s)'
    if(tempSensorCount > 1) return 'temperature sensors'
    return 'temperature sensor'
}
def getPluralHumiditySensor(){
    if(!humiditySensorCount) return 'humidity sensor(s)'
    if(humiditySensorCount > 1) return 'humidity sensors'
    return 'humidity sensor'
}

def getPluralFan(){
    if(!deviceCount) return 'fan(s)'
    if(deviceCount > 1) return 'fans'
    return 'fan'
}

def getSensorType(){
    humiditySensorCount = getHumiditySensorCount()
    tempSensorCount = getTempSensorCount()

    if((settings['sensorType'] == 'humidityOnly' || settings['sensorType'] == 'both') && humiditySensorCount > 0) humidityActive = true
    if((settings['sensorType'] == 'tempOnly' || settings['sensorType'] == 'both') && tempSensorCount > 0) tempActive = true
}

def getSensorText(){
    if(humidityActive && tempActive) return 'humidity/temperature'
    if(humidityActive) return 'humidity'
    if(tempActive) return 'temperature'
}

def getControlSensorText(){
    humidityControlSensorCount = getHumidityControlSensorCount()
    tempControlSensorCount = getTempControlSensorCount()
    
    if(humidityControlSensorCount > 0 && tempControlSensorCount > 0) return 'humidity/temperature'
    if(humidityControlSensorCount > 0) return 'humidity'
    if(tempControlSensorCount > 0) return 'temperature'
}

def displayNameOption(){
    if(app.label){
        displayLabel('Humidity/Temperature name',2)
        label title: '', required: false, width: 10,submitOnChange:true
    } else {
        displayLabel('Set name for this humidity/temperature')
        label title: '', required: false, submitOnChange:true
        displayInfo('Name this humidity/temperature sensor app. Each humidity/temperature sensor app must have a unique name.')
    }
}

def displayDeviceTypeOption(){
    fieldName = 'sensorType'
    fieldTitle = addFieldName('Use humidity and/or temperature?',fieldName)
    input fieldName, 'enum', title: fieldTitle, options: ['humidityOnly':'Humidity only','both': 'Humidity and temperature', 'tempOnly':'Temperature only'], multiple: false, submitOnChange:true
}

def displayHumidityDeviceOption(){
    if(!settings['sensorType']) return
    if(settings['sensorType'] == 'tempOnly') return

    fieldName = 'humiditySensor'
    fieldTitle = 'Select ' + pluralHumidity
    if(settings['sensorType'] == 'both') fieldTitle += ' (optional)'
    if(settings['sensorType'] != 'both') fieldTitle += ' (required)'
    fieldTitle = addFieldName(fieldTitle,fieldName)

    input fieldName, 'capability.relativeHumidityMeasurement', title: fieldTitle, multiple: true, submitOnChange:true

    if(settings['sensorType'] == 'both') helpTip = 'Select which humidity sensor(s) for which to set actions. Humidity or temperature sensor is required.'
    if(settings['sensorType'] == 'humidityOnly') helpTip = 'Select which humidity sensor(s) for which to set actions. Required.'
    if(!settings['humiditySensor']) displayInfo(helpTip)
    
    if(!tempActive) displayHumidityAverageOption()
}

def displayTempDeviceOption(){
    if(!settings['sensorType']) return
    if(settings['sensorType'] == 'humidityOnly') return

    fieldName = 'tempSensor'
    fieldTitle = 'Select ' + pluralTemp
    if(settings['sensorType'] == 'both') fieldTitle = fieldTitle + ' (optional)'
    if(settings['sensorType'] != 'both') fieldTitle = fieldTitle + ' (required)'
    fieldTitle = addFieldName(fieldTitle,fieldName)

    input fieldName, 'capability.temperatureMeasurement', title: fieldTitle, multiple: true, submitOnChange:true

    if(settings['sensorType'] == 'both') helpTip = 'Select which temperature sensor(s) for which to set actions. Humidity or temperature sensor is required.'
    if(settings['sensorType'] == 'tempOnly') helpTip = 'Select which temperature sensor(s) for which to set actions. Required.'
    if(!settings['tempSensor']) displayInfo(helpTip)

    if(duplicateSensors && settings['sensorType'] == 'both'){
        warningMessage = 'The same device is used for both humidity and temperature. This isn\'t neccesary.'
        if(getHumiditySensorCount() > 2) warningMessage = 'The same device is used for both humidity and temperature. This will result in humidity/temperature being incorrect due to being an average.'
        displayWarning(warningMessage)
    }
    displayHumidityAverageOption()
    displayTempAverageOption()
}

def displayHumidityAverageOption(){
    if(!humidityActive) return
    if(humiditySensorCount <= 1) return
    
    fieldName = 'humiditySensorAverage'
    fieldTitle = '<b>independent humidity sensors.</b> Click to average them together.'
    if(settings['humiditySensorAverage'] == null || settings['humiditySensorAverage']) fieldTitle = '<b>Averaging humidity sensors.</b> Click to use them independently.'
    fieldTitle = addFieldName(fieldTitle,fieldName)

    input fieldName, 'bool', title: fieldTitle, defaultValue: true, submitOnChange:true
        if(!settings['device']){
            message = 'Humidity sensor will be treated as if one device by averaging them together for better accuracy. Click to use all of the humidity sensor levels independently.'
            if(!settings['humiditySensorAverage']) message = 'All of the humidity sensor levels will be used independently. If any one meets the criteria, it will trigger the event. Click to average humidity sensors together for better accuracy.'
            displayInfo(message)
        }
}

def displayTempAverageOption(){
    if(!tempActive) return
    if(tempSensorCount <= 1) return
    
    fieldName = 'tempSensorAverage'
    fieldTitle = '<b>independent temperature sensors.</b> Click to average them together.'
    if(settings['tempSensorAverage'] == null || settings['tempSensorAverage']) fieldTitle = '<b>Averaging temperature sensors.</b> Click to use them independently.'
    fieldTitle = addFieldName(fieldTitle,fieldName)

    input fieldName, 'bool', title: fieldTitle, defaultValue: true, submitOnChange:true
    if(!settings['device']){
            message = 'Temperature sensor will be treated as if one device by averaging them together for better accuracy. Click to use all of the temperature sensor levels independently.'
            if(!settings['tempSensorAverage']) message = 'All of the temperature sensor levels will be used independently. If any one meets the criteria, it will trigger the event. Click to average temperature sensors together for better accuracy.'
        }
}

def displayDeviceOption(){
    if(!getSensorSet()) return
    
    fieldName = 'device'
    if(deviceCount > 1) pluralTitle = 'Fans (or switches)'
    if(deviceCount == 1)  pluralTitle = 'Fan (or switch)'
    if(!settings[fieldName]) pluralTitle = 'Fan(s) or switch(es)'
    fieldTitle = pluralTitle + ' being controlled:'
    if(!settings[fieldName]) fieldTitle = pluralTitle + ' to control?'
    fieldTitle = addFieldName(fieldTitle,fieldName)

    input fieldName, 'capability.switch', title: fieldTitle, multiple: true, submitOnChange:true
    if(!settings[fieldName])  displayInfo('Select which ' + pluralTitle + ' to control by ' + infoText + '. Required.')
}

def displayDisableOption(){
    fieldName = 'disable'
    fieldTitle = '<b><font color="#000099">This ' + infoText + ' sensor is disabled.</font></b> Reenable it?'
    fieldTitle = 'This ' + infoText + ' sensor is enabled. Disable it?'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'bool', title: fieldTitle, submitOnChange:true
}

def displayMultiTrigger(){
    startFieldCount = getCountFieldsStart()
    stopFieldCount = getCountFieldsStop()
    if(startFieldCount < 2 && stopFieldCount < 2) return
    fieldName = 'multiStopTrigger'
    if(startFieldCount > 1) {
        fieldTitle = 'Turn on with any condition. Click to require all conditions.'
        if(settings[fieldName]) fieldTitle = 'Turn on with all conditions. Click to only require any one condition.'
    }
    if(stopFieldCount > 1){
        fieldTitle = 'Turn off with any condition. Click to require all conditions.'
        if(settings[fieldName]) fieldTitle = 'Turn off with all conditions. Click to only require any one condition.'
    }
    if(startFieldCount > 1 && stopFieldCount > 1) {
        fieldTitle = 'Turn on or off with any condition. Click to require all conditions.'
        if(settings[fieldName]) fieldTitle = 'Turn on or off with all conditions. Click to only require any one condition.'
    }
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'bool', title: fieldTitle, submitOnChange:true
    if(settings['runTimeMinimum'] || settings['runTimeMaximum'] || settings['start_timeType'] || settings['ifMode'] || settings['personHome'] || settings['personNotHome']) displayInfo('Minimum and maximum time always apply, as do people, scheduling, and Mode settings.')
}

def displayControlDeviceOption(){
    if(!settings['device']) return

    duplicateDevice = compareDeviceLists(settings['humiditySensor'],settings['humidityControlSensor'])

    hidden = true
    if(settings['humidityControlSensor'] && (!settings['controlStartDifference'] && !settings['controlStopDifference'])) hidden = false
    if(duplicateDevice) hidden = false
    if(settings['controlStartDifference'] && validateHumidity(settings['controlStartDifference'])) hidden = false
    if(settings['controlStopDifference'] && validateHumidity(settings['controlStopDifference'])) hidden = false
    if(settings['controlStartDifference'] < settings['controlStopDifference']) hidden = false
    if(settings['controlStartDifference'] && settings['controlStartDifference'] == settings['controlStopDifference']) hidden = false
    //if(settings['controlStopDifference']) || (!settings["humidityControlSensor"] && !settings["controlStartDifference"] && !settings["controlStopDifference"])) hidden = true

    sectionTitle = ''
    if(!settings['humidityControlSensor']) sectionTitle = 'Select "control" sensor(s) (optional)'
    
    if(settings['humidityControlSensor']){
        if(settings['controlStartDifference']) sectionTitle += '<b>Start: Control ' + pluralControl + settings['controlStartDifference'] + '%</b>'
        if(settings['controlStartDifference'] && settings['controlStopDifference']) sectionTitle += '<br>'
        if(!settings['controlStartDifference'] || !settings['controlStopDifference']) sectionTitle += moreOptions

        if(settings['controlStopDifference']) sectionTitle += '<b>Stop: Control ' + pluralControl + settings['controlStopDifference'] + '%</b>'
        if(!settings['controlStartDifference']) sectionTitle = '<b>Control Device:</b>'
    }
    section(hideable: true, hidden: hidden, sectionTitle){
        if(!duplicateDevice) duplicateDevice = compareDeviceLists(settings['tempSensor'],settings['tempControlSensor'])
        if(duplicateDevice) displayError('The control device is a comparision sensor, so can not be the same as the primary sensor.')

        displayHumidityControlSensor()
        displayTempControlSensor()

        if(!duplicateDevice && (settings['humidityControlSensor'] || settings['tempControlSensor'])){
            displayError(validateHumidity(settings['controlStartDifference']))
            displayError(validateHumidity(settings['controlStopDifference']))
            if(settings['controlStartDifference'] < settings['controlStopDifference']) displayWarning('Start level is less than stop level. Unless controlling a humidifier and/or heater, this is probably wrong.')
            if(settings['controlStartDifference'] && settings['controlStartDifference'] == settings['controlStopDifference']) displayError('The starting and stopping levels can\'t be the same, since it would turn on and off at the same time.')
            
            displayControlStartDifference()
            displayControlStopDifference()
            displayControlHelpTip()
            displayControlStopDifferenceManual()
        }
    }
}

def displayHumidityControlSensor(){
    fieldName = 'humidityControlSensor'
    fieldTitle = 'Control ' + pluralHumidity
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(humidityActive) input fieldName, 'capability.relativeHumidityMeasurement', title: fieldTitle, multiple: true, submitOnChange:true
    if(humidityActive && tempActive) {
        message = 'Dual-capability sensors do not need to be selected separately as humidity and temperature (but it won\'t hurt anything).'
        if(duplicateControls) {
            displayWarning(message)
        } else {
            displayInfo(message)
        }
    }
}

def displayTempControlSensor(){
    fieldName = 'tempControlSensor'
    fieldTitle = 'Control ' + pluralTemp
    fieldTitle = addFieldName(fieldTitle,fieldName)
    if(tempActive) input fieldName, 'capability.temperatureMeasurement', title: fieldTitle, multiple: true, submitOnChange:true
    if(settings['sensorType'] == 'humidityOnly') sensor1 = settings['humiditySensor']
    if(settings['sensorType'] == 'tempOnly') sensor1 = settings['tempSensor']
    if(settings['sensorType'] == 'both') {
        if(settings['humiditySensor']) sensor1 = settings['humiditySensor']
        if(!settings['humiditySensor']) sensor1 = ''
        if(settings['tempSensor']) sensor1 += settings['tempSensor']
    }
    sensor2 = 'the Control Sensor'
    if(settings['sensorType'] == 'humidityOnly' && settings['humidityControlSensor']) sensor2 = settings['humidityControlSensor']
    if(settings['sensorType'] == 'tempOnly' && settings['tempControlSensor']) sensor2 = settings['tempControlSensor']
    if(settings['sensorType'] == 'both' && (settings['humidityControlSensor'] || settings['tempControlSensor'])) {
            if(settings['humidityControlSensor'])  sensor2 = settings['humidityControlSensor'] 
            if(!settings['humidityControlSensor'])  sensor2 = ''
            if(settings['tempControlSensor']) sensor2 += settings['tempControlSensor']
    }
    sensorAmt = '10% higher/lower'
    if(settings['sensorType'] == 'tempOnly') sensorAmt = '10°' + getTemperatureScale() + ' (or 10%) higher/lower'
    if(settings['sensorType'] == 'both') sensorAmt = 'higher/lower'
    
    message = 'The control device is a comparision sensor. For instance, it allows triggering an event if ' + sensor1 + ' is ' + sensorAmt + ' than ' + sensor2 + '.'
    displayInfo(message)
}

def displayControlStartDifference(){
    if(!settings['device']) return
    if(!humidityActive) return
    
    displayLabel('To start ' + pluralControl)
    fieldName = 'controlStartDifference'
    fieldTitle = 'Percent ' + controlInfoText + ' over control ' + pluralControl + ' to start:'
    if(!settings[fieldName]) fieldTitle = 'Percent ' + controlInfoText + ' over control sensor(s) to start?'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'number', title: fieldTitle, submitOnChange:true
}

def displayControlStopDifference(){
    displayLabel('To stop ' + pluralControl)
    fieldName = 'controlStopDifference'
    fieldTitle = 'Percent ' + controlInfoText + ' over control ' + pluralControl + ' to stop:'
    if(!settings[fieldName]) fieldTitle = 'Percent ' + controlInfoText + ' over control sensor(s) to stop?'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'number', title: fieldTitle, submitOnChange:true
}

def displayControlStopDifferenceManual(){
    if(!settings['controlStopDifference']) return
    fieldName = 'controlStopDifferenceManual'
    fieldTitle = 'If manually turned on, don/'t turn off based on control ' + pluralControl.'
    if(settings[fieldName]){
        fieldTitle = 'If manually turned on, turn off if ' + settings['controlStopDifference'] + '% over control ' + pluralControl + '.'
        helpTip = 'If manually turned on, the ' + pluralFan + ' will turn off if ' + controlInfoText + ' is ' + settings['controlStopDifference'] + '% over control device (even if it is already within ' + settings['controlStopDifference'] + '% when turned on). Click to not turn off based on control ' + pluralControl + '.'
    }
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'bool', title: fieldTitle, submitOnChange:true
    displayInfo(helpTip)
}

def displayControlHelpTip(){
    if(!settings['controlStartDifference'] && !settings['controlStopDifference']) return
    if(humidityActive) {
        if(settings['controlStartDifference']) humidityOn = Math.round(humidityAverage * settings['controlStartDifference'] / 100) + humidityAverage + '%'
        if(settings['controlStopDifference']) humidityOff = Math.round(humidityAverage * settings['controlStopDifference'] / 100) + humidityAverage+ '%'
    }
    if(tempActive) {
        if(settings['controlStartDifference']) tempOn = Math.round(tempAverage * settings['controlStartDifference'] / 100) + tempAverage + '°'
        if(settings['controlStopDifference']) tempOff = Math.round(tempAverage * settings['controlStopDifference'] / 100) + tempAverage + '°'
    }

    if(humidityActive && tempActive) helpTip = 'Control ' + pluralControl + ' is currently at ' + humidityAverage + ' humidity and ' + tempAverageText + ' , so the ' + pluralFan + ' will turn'
    if(humidityActive && !tempActive) helpTip = 'Control ' + pluralControl + ' is currently at ' + humidityAverage + ' humidity, so the ' + pluralFan + ' will turn'
    if(!humidityActive && tempActive) helpTip = 'Control ' + pluralControl + ' is currently at ' + tempAverageText + ', so the ' + pluralFan + ' will turn'

    if(settings['controlStartDifference'] && !validateHumidity(settings['controlStartDifference'])){
        if(humidityActive && tempActive)  helpTip += ' on with ' + humidityOn + ' humidity and ' + tempOn + getTemperatureScale()
        if(humidityActive && !tempActive)  helpTip += ' on with ' + humidityOn + ' humidity'
        if(!humidityActive && tempActive)  helpTip += ' on with ' + tempOn + getTemperatureScale()
    }
    if(settings['controlStartDifference'] && !validateHumidity(settings['controlStartDifference']) && settings['controlStopDifference'] && !validateHumidity(settings['controlStopDifference'])){
        if(humidityActive && tempActive) helpTip += ', and turn off with ' + humidityOff + ' humidity and ' + tempOff + getTemperatureScale()
        if(humidityActive && !tempActive) helpTip += ', and turn off with ' + humidityOff + ' humidity'
        if(humidityActive && !tempActive) helpTip += ', and turn off with ' + tempOff + ' humidity' + getTemperatureScale()
    }
    if((!settings['controlStartDifference'] || validateHumidity(settings['controlStartDifference'])) && settings['controlStopDifference'] && !validateHumidity(settings['controlStopDifference'])){
        if(humidityActive && tempActive)  helpTip += ' off with ' + humidityOff + ' humidity and ' + tempOff + getTemperatureScale()
        if(humidityActive && !tempActive)  helpTip += ' off with ' + humidityOff + ' humidity'
        if(!humidityActive && tempActive)  helpTip += ' off with ' + tempOff + getTemperatureScale()
    }
    helpTip += '.'
    if(helpTip) displayInfo(helpTip)
}

def displayAbsoluteThresholdOption(type){
    if(!settings['device']) return
    if(type == 'humidity' && !humidityActive) return
    if(type == 'temp' && !tempActive) return
    
    typeString = 'humidity'
    typeUnit = '%'
    if(type == 'temp') {
        typeString = 'temperature'
        typeUnit = '°' + getTemperatureScale()
    }

    hidden = true
    if(type == 'humidity' && validateHumidity(settings[type + 'StartThreshold'])) hidden = false
    if(type == 'temp' && validateTemp(settings[type + 'StartThreshold'])) hidden = false

    sectionTitle = ''
    
    direction = 'under'
    directionReverse = 'over'
    if(settings[type + 'Direction']){
        direction = 'over'
        directionReverse = 'under'
    }
    if(!settings[type + 'StartThreshold'] && !settings[type + 'StopThreshold']) sectionTitle = 'Click to set threshold for absolute ' + type + ' (optional)'
    if(settings[type + 'StartThreshold'] && settings[type + 'Direction']) sectionTitle = '<b>Run while: ' + typeString.capitalize() + ' ' + direction + ' ' + settings[type + 'StartThreshold'] + typeUnit + '</b>'
    if(settings[type + 'StartThreshold'] && !settings[type + 'Direction']) sectionTitle = '<b>Run while: ' + typeString.capitalize() + ' ' + direction + ' ' + settings[type + 'StartThreshold'] + typeUnit + '</b>'

    section(hideable: true, hidden: hidden, sectionTitle){
        if(type == 'humidity') displayError(validateHumidity(settings[type + 'StartThreshold']))
        if(type == 'temp'){
            displayError(validateMinTemp(settings[type + 'StartThreshold']))
            displayError(validateMaxTemp(settings[type + 'StartThreshold']))
        }
        
        displayDirection(type, typeString, typeUnit, 'threshold')
        displayAbsoluteThresholdField(type, typeString, typeUnit)

        if(settings[type + 'StartThreshold']) {
            helpTip = 'Device will turn on if ' + direction + ' ' + settings[type + 'StartThreshold'] + typeUnit + ' and not turn off until ' + directionReverse + ' ' + settings[type + 'StartThreshold'] + typeUnit
            if(settings['runTimeMaximum']) helpTip += ' (or after the maximum run time of ' + settings['runTimeMaximum'] + ' minutes have elapsed)'
            helpTip += '.'
        }
        if(!settings[type + 'StartThreshold']) helpTip = 'Enter ' + typeString + typeUnit + ' at which to turn on the ' + pluralFan + '. It will run until lower than threshold (or enter maximum run time below).'
        if(type == 'humidity') helpTip += ' (Current ' + typeString + ' is ' + humidityAverage(settings['humiditySensor'],settings['tempSensor']) + '.)'
        if(type == 'temp') helpTip += ' (Current ' + typeString + ' is ' + tempAverage(settings['humiditySensor'],settings['tempSensor']) + typeUnit + '.)'
        displayInfo(helpTip)
        displayError(errorMessage)
    
        displayAbsoluteThresholdManualStop(type, typeString, typeUnit)
    }
}

def displayAbsoluteThresholdField(type, typeString, typeUnit){
    if(type == 'humidity'){
        if(validateHumidity(settings[type + 'StartThreshold'])) return
    }
    if(type == 'temp'){
        if(validateTemp(settings[type + 'StartThreshold'])) return
    }
    fieldName = type + 'StartThreshold'
    fieldTitle = ''
    fieldTitle = addFieldName(fieldTitle,fieldName)
    displayLabel('To start ' + pluralFan)
    input fieldName, 'number', title: fieldTitle, submitOnChange:true
}

def displayAbsoluteThresholdManualStop(type, typeString, typeUnit){
    if(!settings[type + 'StartThreshold']) return
    if(type == 'humidity'){
        if(validateHumidity(settings[type + 'StartThreshold'])) return
    }
    if(type == 'temp'){
        if(validateTemp(settings[type + 'StartThreshold'])) return
    }
    //if(!settings[type + 'StopThreshold']) return
    
    direction = 'over'
    directionReverse = 'under'
    if(settings[type + 'Direction']){
        direction = 'under'
        directionReverse = 'over'
    }
    fieldName = type + 'AbsoluteThresholdManualStop'
    fieldTitle = 'If manually turned on, ignore this threshold - do not turn off when ' + direction + ' ' + settings[type + 'StartThreshold'] + typeUnit + '.'
    if(settings[type + 'AbsoluteThresholdManualStop']){
        fieldTitle = 'If manually turned on, turn off if ' + direction + ' ' + settings[type + 'StartThreshold'] + typeUnit + '.'
        helpTip = 'If manually turned on, the ' + pluralFan + ' will turn off if ' + typeString + ' is ' + direction + ' ' + settings[type + 'StartThreshold'] + typeUnit + ' (if it is already under, it will immediately turn off). Click to not turn off based on ' + typeString + '.'
    }
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'bool', title: fieldTitle, submitOnChange:true
    displayInfo(helpTip)
}

def displayDirection(type, typeString, typeUnit, field){
    if(!settings[type + 'StartThreshold']) return
    fieldName = type + 'Direction'
    if(field == 'threshold'){
        if(type == 'humidity'){
            if(validateHumidity(settings[type + 'StartThreshold'])) return
        }
        if(type == 'temp'){
            if(validateTemp(settings[type + 'StartThreshold'])) return
        }
        startDirection = 'lower'
        if(settings[fieldName]) startDirection = 'higher'
        fieldTitle = '<b>Start if ' + typeString + ' is ' + startDirection + ' than value entered.</b> Click to reverse.'
    }
    if(field == 'delta'){
        startDirection = 'decreases'
        if(settings[fieldName]) startDirection = 'increases'
        fieldTitle = '<b>Start if ' + typeString + ' ' + startDirection + ' value entered.</b> Click to reverse.'
    }
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'bool', title: fieldTitle, submitOnChange:true
}

def displayLevelRelativeChangeOption(type){
    if(!settings['device']) return
    if(type == 'humidity' && !humidityActive) return
    if(type == 'temp' && !tempActive) return

    typeString = 'humidity'
    typeUnit = '%'
    if(type == 'temp') {
        typeString = 'temperature'
        typeUnit = '°' + getTemperatureScale()
    }

    minutes = settings['relativeMinutes']
    if(!settings['relativeMinutes']) minutes = 'the specified number of'

    hidden = true
    if(settings['relativeMinutes'] != 5 && !settings[type + 'StartDelta']) hidden = false
    if(validateMinutes(settings['relativeMinutes'])) hidden = false

    startIncrease = 'decrease'
    if(settings[type + 'Direction']) startIncrease = 'increase'

    sectionTitle = 'Click to set ' + typeString + ' ' + startIncrease + ' (optional)'
    if(!settings[type + 'StartDelta']) sectionTitle = 'Click to set ' + typeString + ' ' + startIncrease + ' (optional)'
    if(settings[type + 'StartDelta'] && settings['relativeMinutes']) sectionTitle = '<b>Run after: ' + typeString.capitalize() + ' ' + startIncrease + 's ' + settings[type + 'StartDelta'] + typeUnit + ' in ' + minutes + ' min.</b>'
    if(settings[type + 'StartDelta'] && !settings['relativeMinutes']) sectionTitle = '<b>Run after: ' + typeString.capitalize() + '  ' + startIncrease + 's ' + settings[type + 'StartDelta'] + typeUnit + '</b>'

    section(hideable: true, hidden: hidden, sectionTitle){
        if(type == 'humidity') currentLevel = humidityAverage
        if(type == 'temp') currentLevel = tempAverage
        //Validate humidity (as percent change)?
        displayError(validateMinutes(settings['relativeMinutes']))

        displayDirection(type, typeString, typeUnit,'delta')
        displayRelativeMinutes()
        displayDeltaAmountField(type)

        if(!settings[type + 'StartDelta']) displayInfo('Enter percentage of ' + typeString + ' ' + startIncrease + ' within ' + minutes + ' minutes to start the ' + pluralFan + ', relative to original level. (It will continue to run until back to original value.)')

        if(settings[type + 'StartDelta']) helpTip = 'The ' + pluralHumidity + ' is currently at ' + currentLevel + typeUnit + ', so it would turn the ' + pluralFan + ' on if, within ' + minutes + ' minutes, it were to ' + startIncrease + ' to ' + (currentLevel + settings[type + 'StartDelta']) + typeUnit + ' (and turn off only when back to ' + currentLevel + typeUnit + ').'
        displayInfo(helpTip)
        if(type == 'humidity' && settings[type + 'StartDelta']){
         //   if(settings['humidityDirection'] && (settings[type + 'StartDelta'] + humidityAverage > 100)) warnMessage = 'The current humidity is ' + humidityAverage + typeUnit + ' so an increase of ' + settings[type + 'StartDelta'] + typeUnit + ' (to ' + (settings[type + 'StartDelta'] + humidityAverage) + typeUnit + ') is not possible with the current conditions.'
         //   if(!settings['humidityDirection'] && (humidityAverage - settings[type + 'StartDelta'] < 0)) warnMessage = 'The current humidity is ' + humidityAverage + typeUnit + ' so a decrease of ' + settings[type + 'StartDelta'] + typeUnit + ' (to ' + (humidityAverage - settings[type + 'StartDelta']) + typeUnit + ') is not possible with the current conditions.'
        }
        displayWarning(warnMessage)
    }
}

def displayRelativeMinutes(){
    fieldName = 'relativeMinutes'
    fieldTitle = 'Interval (in minutes)'
    if(settings[fieldName] != 5) fieldTitle = 'Minutes between change (default 5)'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'number', title: fieldTitle, required: false, submitOnChange:true, defaultValue: 5
}

def displayDeltaAmountField(type){
    if(validateMinutes(settings['relativeMinutes'])) return

    direction = 'decrease'
    if(settings[type + 'Direction']) direction = 'increase'

    fieldName = type + 'StartDelta'
    fieldTitle = type.capitalize() + ' ' + startIncrease
    if(type == 'temp') fieldTitle = 'Temperature ' + startIncrease
    fieldTitle = addFieldName(fieldTitle,fieldName)
    displayLabel('To start ' + pluralFan)
    input fieldName, 'decimal', title: fieldTitle, required: false, submitOnChange:true
}

def displayHumidityStopDelta(){
    if(!settings['humidityStartDelta']) return
    if(validateMinutes(settings['relativeMinutes'])) return
    fieldName = 'humidityStopDelta'
    fieldTitle = 'Percent above starting percent (where zero (0) is the initial level)'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    displayLabel('To stop ' + pluralFan)
    input fieldName, 'decimal', title: fieldTitle, required: false, submitOnChange:true
}

def displayTempStartDelta(){
    if(validateMinutes(settings['relativeMinutes'])) return
    fieldName = 'tempStartDelta'
    fieldTitle = 'Temperature increase'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    displayLabel('To start ' + pluralFan)
    input fieldName, 'decimal', title: fieldTitle, required: false, submitOnChange:true
}

def displayTempStopDelta(){
    if(!settings['tempStartDelta']) return
    if(validateMinutes(settings['relativeMinutes'])) return
    fieldName = 'tempStopDelta'
    fieldTitle = 'Degrees above starting temperature (where zero (0) is the initial level)'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    displayLabel('To stop ' + pluralFan)
    input fieldName, 'decimal', title: fieldTitle, required: false, submitOnChange:true
}

def displayRunTimeOption(){
    if(!settings['device']) return
    if(!humidityActive && !tempActive) return
    
    hidden = true
    if(settings['runTimeMinimum'] && settings['runTimeMaximum'] && settings['runTimeMinimum'] >= settings['runTimeMaximum']) hidden = false
    if(settings['runTimeMinimum'] && !settings['runTimeMaximum']) hidden = false
    if(!settings['runTimeMinimum'] && settings['runTimeMaximum']) hidden = false
    if(validateMinutes(settings['runTimeMinimum'])) hidden = false
    if(validateMinutes(settings['runTimeMaximum'])) hidden = false

    sectionTitle = ''
    if(!settings['runTimeMinimum'] && !settings['runTimeMaximum']) sectionTitle = 'Click to set run time (optional)'
    if(settings['runTimeMinimum']) sectionTitle =  '<b>Minimum run time: ' + settings['runTimeMinimum'] + ' min.</b>'
    if(settings['runTimeMinimum'] && settings['runTimeMaximum']) sectionTitle += '<br>'
    if(settings['runTimeMaximum']) sectionTitle += '<b>Maximum run time: ' + settings['runTimeMaximum'] + ' min.</b>'
    if(settings['runTimeMinimum'] && !settings['runTimeMaximum']) sectionTitle += moreOptions
    if(!settings['runTimeMinimum'] && settings['runTimeMaximum']) sectionTitle += moreOptions

    section(hideable: true, hidden: hidden, sectionTitle){
        if(settings['runTimeMinimum'] && settings['runTimeMaximum'] && settings['runTimeMinimum'] > settings['runTimeMaximum']) displayError('Minimum run time must be greater than maximum run time.')
        displayError(validateMinutes(settings['runTimeMinimum']))
        displayError(validateMinutes(settings['runTimeMaximum']))
        if(settings['runTimeMinimum'] && settings['runTimeMaximum'] && settings['runTimeMinimum'] == settings['runTimeMaximum']) displayWarning('If is not recommended to have equal maximum and minimum run time. It will turn off after ' + settings['runTimeMinimum'] + ' minutes regardless of any other settings, but setting maximum run time without any other start and stop settings would accomplish the same thing.')

        displayRunTimeMinimum()
        displayRunTimeMaximum()
        displayRunTimeMaximumManual()
    }
}

def displayRunTimeMinimum(){
    if(!validateMinutes(settings['runTimeMinimum']) && validateMinutes(settings['runTimeMaximum'])) return
    fieldName = 'runTimeMinimum'
    fieldTitle = ''
    fieldTitle = addFieldName(fieldTitle,fieldName) 
    displayLabel('Minimum run time (in minutes)')
    input fieldName, 'number', title: fieldTitle, required: false, submitOnChange:true
    if(!settings[fieldName]) displayInfo('Number of minimum minutes before turning off regardless of ' + getSensorText() + ' level, to prevent "cycling".')
}

def displayRunTimeMaximum(){
    if(validateMinutes(settings['runTimeMinimum']) && !validateMinutes(settings['runTimeMaximum'])) return
    fieldName = 'runTimeMaximum'
    fieldTitle = ''
    fieldTitle = addFieldName(fieldTitle,fieldName) 
    displayLabel('Maximum run time')
    input fieldName, 'number', title: fieldTitle, required: false, submitOnChange:true
    if(!settings[fieldName]) displayInfo('Number of maximum minutes to run after which it will turn off regardless of ' + getSensorText() + ' level. (Will not turn on again for the same duration.)')
}

def displayRunTimeMaximumManual(){
    if(!settings['runTimeMaximum']) return
    if(validateMinutes(settings['runTimeMinimum'])) return
    if(validateMinutes(settings['runTimeMaximum'])) return
    fieldName = 'runTimeMaximumManual'
    fieldTitle = 'If manually turned on, turn the ' + pluralFan + ' off after ' + settings['runTimeMaximum'] + ' minutes.'
    fieldTitle = addFieldName(fieldTitle,fieldName) 
    input fieldName, 'bool', defaultValue : true, title: fieldTitle, submitOnChange:true
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
    
    hidden = true
    if(settings['start_timeType'] && !settings['stop_timeType']) hidden = false
    if(settings['disable']) hidden = true
    if(settings['start_time'] && settings['start_time'] == settings['stop_time']) hidden = false
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
    if(settings['start_timeType'] == 't ime' && settings['start_time']) sectionTitle += 'At ' + Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings['start_time']).format('h:mm a', location.timeZone)
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
    if(type == 'stop') fielList = ['time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)']
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

def displayIfModeOption(){
    if(!settings['device']) return
    if(!humidityActive && !tempActive) return

    sectionTitle = 'Click to select with what Mode (optional)'
    if(settings['ifMode']) sectionTitle = '<b>Only with Mode: ' + settings['ifMode'] + '</b>'

    section(hideable: true, hidden: true, sectionTitle){
        input 'ifMode', 'mode', title: 'Only run if Mode is already?', width: 12, submitOnChange:true

        message = 'This will limit the ' + pluralContact + ' from running to only when Hubitat\'s Mode is as selected.'
        if(settings['ifMode']) message = 'This will limit the ' + pluralContact + ' from running to only when Hubitat\'s Mode is ' + settings['ifMode'] + '.'

        displayInfo(message)
    }
}

def displayAlertOptions(){
    if(!settings['device']) return
    if(!humidityActive && !tempActive) return
    if(!parent.pushNotificationDevice && !parent.speechDevice) return

    hidden = true
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

    if(settings['pushNotification'] && settings['speech']) sectionTitle += "send notification and speak text</b>"
    if(settings['pushNotification'] && !settings['speech']) sectionTitle += "send notification</b>"
    if(!settings['pushNotification'] && settings['speech']) sectionTitle += "speak text</b>"
    if(!settings['notificationStartStop']) sectionTitle = '<b>' + sectionTitle.capitalize()
    if(!settings['pushNotification'] || !settings['speech']) sectionTitle += moreOptions
    
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
    if(!settings['device']) return
    if(!humidityActive && !tempActive) return

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

def getSensorSet(){
    if(settings['sensorType'] == 'humidityOnly' && settings['humiditySensor']) return true
    if(settings['sensorType'] == 'tempOnly' && settings['tempSensor']) return true
    if(settings['sensorType'] == 'both' && settings['humiditySensor']) return true
    if(settings['sensorType'] == 'both' && settings['tempSensor']) return true
}

def compareDeviceLists(firstDevices,secondDevices){
    if(!firstDevices) return
    if(!secondDevices) return
    returnValue = null

    firstDevices.each{first->
        secondDevices.each{second->
            if(first.id == second.id) {
                returnValue = true
            }
        }
    }
    return returnValue
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
    putLog(1329,'trace','Installed')
    app.updateLabel(parent.appendChildAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    putLog(1335,'trace','Updated')
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
    
    getSensorType()
// Pretty sure humidityActive isn't set
    atomicState.humidityActive = humidityActive
    atomicState.tempActive = tempActive
    if(!humidityActive && !tempActive) return
    
    setTime()
    scheduleMaximumRunTime()
       
    if(humidityActive) subscribe(settings['humiditySensor'], 'humidity', handleSensorUpdate)
    if(humidityActive && settings['humidityControlSensor']) subscribe(settings['humidityControlSensor'], 'humidity', temperatureHandler)
    if(tempActive) subscribe(settings['tempSensor'], 'temperature', handleSensorUpdate)
    if(tempActive && settings['tempControlSensor']) subscribe(settings['tempControlSensor'], 'temperature', temperatureHandler)
    subscribe(settings['device'], 'switch', handleStateChange)
    
    putLog(1364,'trace','Initialized')
}

def handleSensorUpdate(event) {
    if(!atomicState.humidityActive) return
    updateStatus()
}

def updateStatus(){
    atomicState.humidityAverage = humidityAverage(settings['humiditySensor'],settings['tempSensor'])
    atomicState.tempAverage = tempAverage(settings['humiditySensor'],settings['tempSensor'])
    atomicState.humidityControlAverage = humidityAverage(settings['humidityControlSensor'],settings['tempControlSensor'])
    atomicState.tempControlAverage = tempAverage(settings['humidityControlSensor'],settings['tempControlSensor'])

    updateRelativeHumidityArray()
    updateRelativeTempArray()
    
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
    atomicState.humidityStart = atomicState.humidityAverage
    atomicState.tempStart = atomicState.tempAverage
    if(!checkOnConditions()) return
    if(checkOffConditions()) {
        atomicState.startTime = null
        putLog(1403,'warn','Both on and off conditions met.')
        return
    }
    
    atomicState.startTime = now()
    atomicState.stopTime = null
    
    settings['device'].each{singleDevice->
        stateMap = parent.getStateMapSingle(singleDevice,'on',app.id,app.label)
        parent.mergeMapToTable(singleDevice.id,stateMap,app.label)
    }
    putLog(1414,'warn','Turning devices on.')
    parent.setDeviceMulti(settings['device'],app.label)
    unschedule()
    scheduleMaximumRunTime()

    //parent.sendPushNotification('',app.label + ' turned on at ' + now.format('h:mm a', location.timeZone),app.label)
    //parent.sendVoiceNotification(settings['speechDevice'],settings['speech'],app.label)
}

def turnOff(){
    if(!atomicState.startTime) return // Already off
    if(getDisabled()) return
    if(!checkOffConditions()) return
    if(checkOnConditions()) return

    atomicState.startTime = null
    atomicState.stopTime = now()
    
    settings['device'].each{singleDevice->
        stateMap = parent.getStateMapSingle(singleDevice,'off',app.id,app.label)
        parent.mergeMapToTable(singleDevice.id,stateMap,app.label)
    }
    parent.setDeviceMulti(settings['device'],app.label)
    unschedule()
}

//Averages humidity level of all "humidity" and "temperature" sensors
def humidityAverage(humidityDevice, tempDevice){
    if(settings['sensorType'] == 'humidityOnly') tempDevice = null
    if(settings['sensorType'] == 'tempOnly') humidityDevice = null
    if(!humidityDevice && !tempDevice) return
    humidity = 0
    deviceCount = 0
    if(humidityDevice){
        humidityDevice.each {singleDevice->
            if(checkIsHumidityDevice(singleDevice)) {
                humidity += singleDevice.currentHumidity
                deviceCount++
                    }
        }
    }
    if(tempDevice){        // We should exclude those that are (selected as) both humidity and temp, but there's a UI warning for it
        tempDevice.each {singleDevice->
            if(checkIsHumidityDevice(singleDevice)) {
                humidity += singleDevice.currentHumidity
                deviceCount++
                }
        }
    }

    if(deviceCount > 0) return Math.round(humidity / deviceCount)
    return 0
}

def tempAverage(humidityDevice, tempDevice){
    if(settings['sensorType'] == 'tempOnly') humidityDevice = null
    if(settings['sensorType'] == 'humidityOnly') tempDevice = null
    if(!humidityDevice && !tempDevice) return

    temp = 0
    deviceCount = 0
    if(humidityDevice){
        humidityDevice.each {singleDevice->
            if(checkIsTemperatureDevice(singleDevice)) {
                temp += singleDevice.currentTemperature
                deviceCount++
                    }
        }
    }
    if(tempDevice){        // We should exclude those that are (selected as) both humidity and temp, but there's a UI warning for it
        tempDevice.each {singleDevice->
            if(checkIsTemperatureDevice(singleDevice)) {
                temp += singleDevice.currentTemperature
                deviceCount++
                    }
        }
    }
    if(deviceCount > 0) return Math.round(temp / deviceCount)
    return 0
}

def updateRelativeHumidityArray(){
    if(!atomicState.humidityActive) return
    if(!atomicState.humidityChanges){
        atomicState.humidityChanges = ['1':[time:now(),humidity:atomicState.humidityAverage]]
        return
    }
    itemCount = 0
    newArray = [:]
    timeLimit = now() - (settings['relativeMinutes'] * parent.CONSTMinuteInMilli())
    atomicState.humidityChanges.each{
        if(it.value.time > timeLimit) {
            itemCount++
            newArray[itemCount]  = [time:it.value.time,humidity:it.value.humidity]
        } else {
            if(!earliestTime) {
                earliestTime =  it.value.time
                earliestValue = it.value.humidity
            } else if(earliestValue && earliestValue < it.value.time)  {
                earliestTime =  it.value.time
                earliestValue = it.value.humidity
            }
        }
    }
    if(earliestValue){
        if(!itemCount) itemCount = 0
        itemCount++
            newArray[itemCount] = [time:earliestTime,humidity:earliestValue]
    }
    itemCount++
    newArray[itemCount]  = [time:now(),humidity:atomicState.humidityAverage]
    atomicState.humidityChanges = newArray
}

def updateRelativeTempArray(){
    if(!atomicState.tempActive) return
    if(!atomicState.tempChanges){
        atomicState.tempChanges = ['1':[time:now(),temp:atomicState.tempAverage]]
        return
    }
    itemCount = 0
    newArray = [:]
    timeLimit = now() - (settings['relativeMinutes'] * parent.CONSTMinuteInMilli())
    atomicState.tempChanges.each{
        if(it.value.time > timeLimit) {
            itemCount++
                newArray[itemCount]  = [time:it.value.time,temp:it.value.temp]
        } else {
            if(!earliestTime) {
                earliestTime =  it.value.time
                earliestValue = it.value.temp
            } else if(earliestValue && earliestValue < it.value.time)  {
                earliestTime =  it.value.time
                earliestValue = it.value.temp
            }
        }
    }
    if(earliestValue){
        if(!itemCount) itemCount = 0
        itemCount++
            newArray[itemCount] = [time:earliestTime,temp:earliestValue]
    }
    itemCount++
    newArray[itemCount]  = [time:now(),temp:atomicState.tempAverage]
    atomicState.tempChanges = newArray
}

def checkOnConditions(){
    if(!checkMinimumWaitTime()) return
    
    if(settings['multiStartTrigger']) {
        allOnConditions = checkAllOnConditions()
        putLog(1566,'trace','All on conditions is ' + allOnConditions)
        return allOnConditions
    }
    anyOnConditions = checkAnyOnConditions()
    putLog(1570,'trace','Any on condition is ' + anyOnConditions)
    return anyOnConditions
}

def checkOffConditions(){
    if(!checkRunTimeMinimum()) return
    if(checkRunTimeMaximum()) return true
    
    if(settings['multiStopTrigger']) {
        allOffConditions = checkAllOffConditions()
        putLog(1580,'trace','All off conditions is ' + allOffConditions)
        return allOffConditions
    }
    if(!settings['multiStopTrigger']) {
        anyOffConditions = checkAnyOffConditions()
        putLog(1585,'trace','Any off conditions is ' + anyOffConditions)
        return anyOffConditions
    }
}

def checkAnyOnConditions(){
    if(checkControlStartDifference()) return true
    if(checkStartThreshold('humidity')) return true
    if(checkStartThreshold('temp')) return true
    if(checkStartDelta('humidity')) return true
    if(checkStartDelta('temp')) return true
    return false    // used for log
}

def checkAllOnConditions(){
    if(settings['controlStartDifference'] && !checkControlStartDifference()) return false
    if(settings['humidityStartThreshold'] && !checkStartThreshold('humidity')) return false
    if(settings['humidityStartDelta'] && !checkStartDelta('humidity')) return false
    if(settings['tempStartDelta'] && !checkStartDelta('temp')) return false
    if(settings['tempStartThreshold'] && !checkStartThreshold('temp')) return false
    
    return true
}

def checkAnyOffConditions(){
    if(checkControlStopDifference()) return true
    if(checkStopThreshold('humidity')) return true
    if(checkStopDelta('humidity')) return true
    if(checkStopDelta('temp')) return true
    if(checkStopThreshold('temp')) return true
    return false
}

def checkAllOffConditions(){
    if(settings['controlStopDifference'] && !checkControlStopDifference()) return false
    if(settings['humidityStartThreshold'] && !checkStopThreshold('humidity')) return false
    if(settings['humidityDelta'] && !checkStopDelta('humidity')) return false
    if(settings['tempThreshold'] && !checkStopThreshold('temp')) return false
    if(settings['tempDelta'] && !checkStopDelta('temp')) return false
}

def checkControlStartDifference(){
    if(!settings['controlStartDifference']) return
    if(atomicState.humidityAverage > atomicState.humidityControlAverage + settings['controlStartDifference']) return true
}

def checkControlStopDifference(){
    if(!settings['controlStopDifference']) return
    if(!settings['controlStopDifferenceManual']) return
    if(atomicState.humidityAverage < atomicState.humidityControlAverage + settings['controlStopDifference']) return true
}
    
//With stop, need to test this if state.appId matches - but that is per device
//Need to make testing each criteria per device?!
//if(!settings['humidityAbsoluteThresholdManualStop']) return

def checkStartThreshold(type){
    if(type != 'humidity' && type != 'temp') return
    if(!atomicState[type + 'Active']) return
    if(!settings[type + 'StartThreshold']) return
    if(settings[type + 'Direction'] && atomicState[type + 'Average'] > settings[type + 'StartThreshold']) return true
    if(!settings[type + 'Direction'] && atomicState[type + 'Average'] < settings[type + 'StartThreshold']) return true
}

def checkStopThreshold(type){
    if(type != 'humidity' && type != 'temp') return
    if(!atomicState[type + 'Active']) return
    if(!settings[type + 'StartThreshold']) return
    if(settings[type + 'Direction'] && atomicState[type + 'Average'] < settings[type + 'StartThreshold']) return true
    if(!settings[type + 'Direction'] && atomicState[type + 'Average'] > settings[type + 'StartThreshold']) return true
}

// start/stopHumdity/Temp are never set
def checkStartDelta(type){
    if(!atomicState[type + 'Active']) return
    if(type != 'humidity' && type != 'temp') return
    if(!settings[type + 'StartDelta']) return
    if(!settings['relativeMinutes']) return
    if(!atomicState[type + 'Humidity']) return
    
    startDelta = false
    atomicState[type + 'Changes'].each {
        if(settings[type + 'Direction']) {
            difference = atomicState[type + 'average'] - it.value[type]
            if(difference >= settings[type + 'StartDelta']) startDelta = true
        }
        if(!settings[type + 'Direction']) {
            difference = it.value.humidity - atomicState[type + 'average']
            if(difference >= settings[type + 'StartDelta']) startDelta = true
        }
    }
    return startDelta
}

def checkStopDelta(type){   
    if(type != 'humidity' && type != 'temp') return     
    if(!settings[type + 'Delta']) return
    if(!settings['relativeMinutes']) return
    if(!atomicState[type + 'Humidity']) return

    if(settings[type + 'Direction']){
        if(atomicState[type + 'Average'] <= atomicState[type + 'Start'] - settings[type + 'Delta']) return true
    }
    if(!settings[type + 'Direction']){
        if(atomicState[type + 'Average'] >= atomicState[type + 'Start'] + settings[type + 'Delta']) return true
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
        putLog(1701,'trace','Maximum runtime exceeded.')
        return true
    }
}

def checkMinimumWaitTime(){
    if(!settings['runTimeMinimum']) return true
    if(!atomicState.stopTime) return true
    
    elapsedTime = now() - atomicState.stopTime

    if(elapsedTime < settings['runTimeMinimum'] * parent.CONSTMinuteInMilli()) return
    putLog(1713,'trace','Minimum wait time exceeded.')
    return true
}

def checkIsHumidityDevice(singleDevice){
    if(singleDevice.hasCapability("RelativeHumidityMeasurement")) return true
    return
}

def checkIsTemperatureDevice(singleDevice){
    if(singleDevice.hasCapability("TemperatureMeasurement")) return true
    return
}

def scheduleMaximumRunTime(){
    if(!settings['runTimeMaximum']) return
    if(!parent.checkAnyOnMulti(settings['device'])) return
    unschedule()
    
    timeMillis = settings['runTimeMaximum'] * parent.CONSTMinuteInMilli()
    
    parent.scheduleChildEvent(timeMillis,'','performMaximumRunTime','',app.id)
}

def performMaximumRunTime(){     // is called from maximumTime schedule, if parameters added, need to update scheduleMaximumRunTime
    atomicState.humidityAverage = humidityAverage(settings['humiditySensor'],settings['tempSensor'])
    atomicState.tempAverage = tempAverage(settings['humiditySensor'],settings['tempSensor'])
    atomicState.humidityControlAverage = humidityAverage(settings['humidityControlSensor'],settings['tempControlSensor'])
    atomicState.tempControlAverage = tempAverage(settings['humidityControlSensor'],settings['tempControlSensor'])

    updateRelativeHumidityArray()
    settings['device'].each{singleDevice->
        stateMap = parent.getStateMapSingle(singleDevice,'off',app.id,app.label)
        parent.mergeMapToTable(singleDevice.id,stateMap,app.label)
    }
    parent.setDeviceMulti(settings['device'],app.label)
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
        putLog(1769,'info','Scheduling update subrise/sunset start and/or stop time(s).')
    }
    return true
}

// DEPRECATED
def setStartTime(){
    if(!settings['start_timeType']) return
    if(atomicState.start && parent.checkToday(atomicState.start,app.label)) return
    setTime = setStartStopTime('start')
    if(setTime > now()) setTime -= parent.CONSTDayInMilli() // We shouldn't have to do this, it should be in setStartStopTime to get the right time to begin with
    if(!parent.checkToday(setTime)) setTime += parent.CONSTDayInMilli() // We shouldn't have to do this, it should be in setStartStopTime to get the right time to begin with
    atomicState.start  = setTime
    putLog(1782,'info','Start time set to ' + parent.getPrintDateTimeFormat(setTime))
    return true
}

// DEPRECATED
def setStopTime(){
    if(!settings['stop_timeType'] || settings['stop_timeType'] == 'none') return
    if(atomicState.stop > atomicState.start) return
    setTime = setStartStopTime('stop')
    if(setTime < atomicState.start) setTime += parent.CONSTDayInMilli()
    atomicState.stop = setTime
    putLog(1793,'info','Stop time set to ' + parent.getPrintDateTimeFormat(setTime))
    return true
}

// DEPRECATED
// Sets atomicState.start and atomicState.stop variables
// Requires type value of "start" or "stop" (must be capitalized to match setting variables)
def setStartStopTime(type){
    if(settings[type + '_timeType'] == 'time') return Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSSZ", settings[type + '_time']).getTime()
    if(settings[type + '_timeType'] == 'time') return Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSSZ", settings[type + '_time']).getTime()
    if(settings[type + '_timeType'] == 'sunrise') return (settings[type + '_sunType'] == 'before' ? parent.getSunrise(settings[type + '_sunOffset'] * -1,app.label) : parent.getSunrise(settings[type + '_sunOffset'],app.label))
    if(settings[type + '_timeType'] == 'sunset') return (settings[type + '_sunType'] == 'before' ? parent.getSunset(settings[type + '_sunOffset'] * -1,app.label) : parent.getSunset(settings[type + '_sunOffset'],app.label))
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
