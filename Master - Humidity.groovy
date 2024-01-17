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
*  Version: 0.3.04
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
moreOptions = ' <font color="grey">(click for more options)</font>'

// logLevel sets number of log messages
// 0 for none
// 1 for errors only
// 5 for all
def getLogLevel(){
    return 4
}
def getMaxTemp(){
    return 250
}
def getMinTemp(){
    return -50
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
            if(humidityActive) averageHumidity = Math.round(averageHumidity(settings['humiditySensor'],settings['tempSensor']))
            if(humidityActive) averageHumidityText = averageHumidity + "%"
            if(tempActive) averageTemp = Math.round(averageTemp(settings['tempSensor'],settings['humiditySensor']))
            if(tempActive) averageTempText = averageTemp + '°' + getTemperatureScale()
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
            displayLevelOption('humidity')
            displayLevelOption('temp')
            displayHumidityRelativeChangeOption()
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
    if(!humiditySensor) return false
    if(!device) return false
    if((humidityControlSensor || tempControlSensor) && (!controlStartDifference && !controlStopDifference)) return false
    if(controlStartDifference > 100) return false
    if(controlStopDifference > 100) return false
    if(humidityStartThreshold > 101) return false
    if(humidityStopThreshold > 101) return false
    if(humidityStartPercent > 100) return false
    if(humidityStartPercent == 0) return false
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
    log.debug 'value ' + value + ' < 1 = ' + (value < 1)
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
                if(settings['humidityStartPercent']) count++
                        return count
}

def getCountFieldsStop(){
    count = 0
    if(settings['controlStopDifference']) count++
    if(settings['humidityStopThreshold']) count++
    if(settings['tempStartThreshold']) count++
    if(settings['humidityStopPercent']) count++
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
        displayLabel('Humidity/Temperature name',3)
        label title: '', required: true, width: 9,submitOnChange:true
    } else {
        displayLabel('Set name for this humidity/temperature')
        label title: '', required: true, submitOnChange:true
        displayInfo('Name this humidity/temperature sensor app. Each humidity/temperature sensor app must have a unique name.')
    }
    return
}

def displayDeviceTypeOption(){
    fieldName = 'sensorType'
    fieldTitle = addFieldName('Use humidity and/or temperature?',fieldName)
    input fieldName, 'enum', title: fieldTitle, options: ['humidityOnly':'Humidity only','both': 'Humidity and temperature', 'tempOnly':'Temperature only'], multiple: false, submitOnChange:true
}

def displayHumidityDeviceOption(){
    if(!settings['sensorType']) return
    if(settings['sensorType'] == 'tempOnly') return
    
    if(duplicateSensors && settings['sensorType'] == 'both') displayWarning('Dual-capability sensors do not need to be selected separately as humidity and temperature (but it won\'t hurt anything).')

    fieldName = 'humiditySensor'
    fieldTitle = 'Select ' + pluralHumidity
    if(settings['sensorType'] == 'both') fieldTitle += ' (optional)'
    if(settings['sensorType'] != 'both') fieldTitle += ' (required)'
    fieldTitle = addFieldName(fieldTitle,fieldName)

    input fieldName, 'capability.relativeHumidityMeasurement', title: fieldTitle, multiple: true, submitOnChange:true

    if(settings['sensorType'] == 'both') helpTip = 'Select which humidity sensor(s) for which to set actions. Humidity or temperature sensor is required.'
    if(settings['sensorType'] == 'humidityOnly') helpTip = 'Select which humidity sensor(s) for which to set actions. Required.'
    if(!settings['humiditySensor']) displayInfo(helpTip)
        if(!tempActive) displayAverageHumidityOption()
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
    displayAverageHumidityOption()
    displayAverageTempOption()
}

def displayAverageHumidityOption(){
    if(!humidityActive) return
    if(humiditySensorCount <= 1) return
    
    fieldName = 'humiditySensorAverage'
    fieldTitle = '<b>Independant humidity sensors.</b> Click to average them together.'
    if(settings['humiditySensorAverage'] == null || settings['humiditySensorAverage']) fieldTitle = '<b>Averaging humidity sensors.</b> Click to use them independatly.'
    fieldTitle = addFieldName(fieldTitle,fieldName)

    input fieldName, 'bool', title: fieldTitle, defaultValue: true, submitOnChange:true
        if(!settings['device']){
            message = 'Humidity sensor will be treated as if one device by averaging them together for better accuracy. Click to use all of the humidity sensor levels independantly.'
            if(!settings['humiditySensorAverage']) message = 'All of the humidity sensor levels will be used independantly. If any one meets the criteria, it will trigger the event. Click to average humidity sensors together for better accuracy.'
            displayInfo(message)
        }
}

def displayAverageTempOption(){
    if(!tempActive) return
    if(tempSensorCount <= 1) return
    
    fieldName = 'tempSensorAverage'
    fieldTitle = '<b>Independant temperature sensors.</b> Click to average them together.'
    if(settings['tempSensorAverage'] == null || settings['tempSensorAverage']) fieldTitle = '<b>Averaging temperature sensors.</b> Click to use them independantly.'
    fieldTitle = addFieldName(fieldTitle,fieldName)

    input fieldName, 'bool', title: fieldTitle, defaultValue: true, submitOnChange:true
    if(!settings['device']){
            message = 'Temperature sensor will be treated as if one device by averaging them together for better accuracy. Click to use all of the temperature sensor levels independantly.'
            if(!settings['tempSensorAverage']) message = 'All of the temperature sensor levels will be used independantly. If any one meets the criteria, it will trigger the event. Click to average temperature sensors together for better accuracy.'
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
        if(duplicateDevice) displayError('The control ' + pluralControl + ' can\'t be include a primary sensor.')

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
        if(settings['controlStartDifference']) humidityOn = Math.round(averageHumidity * settings['controlStartDifference'] / 100) + averageHumidity + '%'
        if(settings['controlStopDifference']) humidityOff = Math.round(averageHumidity * settings['controlStopDifference'] / 100) + averageHumidity+ '%'
    }
    if(tempActive) {
        if(settings['controlStartDifference']) tempOn = Math.round(averageTemp * settings['controlStartDifference'] / 100) + averageTemp + '°'
        if(settings['controlStopDifference']) tempOff = Math.round(averageTemp * settings['controlStopDifference'] / 100) + averageTemp + '°'
    }

    if(humidityActive && tempActive) helpTip = 'Control ' + pluralControl + ' is currently at ' + averageHumidityText + ' humidity and ' + averageTempText + ' , so the ' + pluralFan + ' will turn'
    if(humidityActive && !tempActive) helpTip = 'Control ' + pluralControl + ' is currently at ' + averageHumidityText + ' humidity, so the ' + pluralFan + ' will turn'
    if(!humidityActive && tempActive) helpTip = 'Control ' + pluralControl + ' is currently at ' + averageTempText + ', so the ' + pluralFan + ' will turn'

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

def displayLevelOption(type){
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
    if(settings[type + 'StartThreshold'] && !settings[type + 'StopThreshold']) hidden = false
    if(!settings[type + 'StartThreshold'] && settings[type + 'StopThreshold']) hidden = false
    if(type == 'humidity' && validateHumidity(settings[type + 'StartThreshold'])) hidden = false
    if(type == 'humidity' && validateHumidity(settings[type + 'StopThreshold'])) hidden = false
    if(type == 'temp' && validateTemp(settings[type + 'StartThreshold'])) hidden = false
    if(type == 'temp' && validateTemp(settings[type + 'StopThreshold'])) hidden = false
    if(settings[type + 'StartThreshold'] && settings[type + 'StartThreshold'] == settings[type + 'StopThreshold']) hidden = false

    sectionTitle = ''
    
    if(settings[type + 'StartThreshold'] && settings[type + 'StopThreshold']){
        if(settings[type + 'StartThreshold'] < settings[type + 'StopThreshold']){
            settings[type + 'Over'] = true
        }
        if(settings[type + 'StartThreshold'] > settings[type + 'StopThreshold']) settings[type + 'Over'] = false
    }
    
    startDirection = 'under'
    stopDirection = 'over'
    if(settings[type + 'Over']){
        startDirection = 'over'
        stopDirection = 'under'
    }
    if(!settings[type + 'StartThreshold'] && !settings[type + 'StopThreshold']) sectionTitle = 'Click to set absolute ' + type + ' (optional)'
    if(settings[type + 'StartThreshold'] && settings[type + 'Over']) sectionTitle = '<b>Start: ' + typeString.capitalize() + ' ' + startDirection + ' ' + settings[type + 'StartThreshold'] + typeUnit + '</b>'
    if(settings[type + 'StartThreshold'] && !settings[type + 'Over']) sectionTitle = '<b>Start: ' + typeString.capitalize() + ' ' + startDirection + ' ' + settings[type + 'StartThreshold'] + typeUnit + '</b>'
    if(settings[type + 'StartThreshold'] && settings[type + 'StartThreshold']) sectionTitle += '<br>'
    if(settings[type + 'StopThreshold'] && settings[type + 'Over']) sectionTitle += '<b>Stop: ' + typeString.capitalize() + ' ' + stopDirection + ' ' + settings[type + 'StopThreshold'] + typeUnit + '</b>'
    if(settings[type + 'StopThreshold'] && !settings[type + 'Over']) sectionTitle += '<b>Stop: ' + typeString.capitalize() + ' ' + stopDirection + ' ' + settings[type + 'StopThreshold'] + typeUnit + '</b>'
    if(settings[type + 'StartThreshold'] && !settings[type + 'StopThreshold']) sectionTitle += moreOptions
    if(!settings[type + 'StartThreshold'] && settings[type + 'StopThreshold']) sectionTitle += moreOptions

    section(hideable: true, hidden: hidden, sectionTitle){
        if(settings[type + 'StartThreshold'] && settings[type + 'StartThreshold'] == settings[type + 'StopThreshold'])  displayError('The starting and stopping levels can\'t be the same, since it would turn on and off at the same time.')
        if(type == 'humidity'){
            displayError(validateHumidity(settings[type + 'StopThreshold']))
            displayError(validateHumidity(settings[type + 'StartThreshold']))
        }
        if(type == 'temp'){
            displayError(validateMinTemp(settings[type + 'StopThreshold']))
            displayError(validateMaxTemp(settings[type + 'StopThreshold']))
            displayError(validateMinTemp(settings[type + 'StartThreshold']))
            displayError(validateMaxTemp(settings[type + 'StartThreshold']))
        }
        if(settings[type + 'StartThreshold'] && settings[type + 'StopThreshold'] && settings[type + 'Over']) displayWarning('Start is lower than stop. Assuming it should start when ' + typeString + ' is less than ' + settings[type + 'StartThreshold'] + typeUnit + ' and continue to run until it increases to ' + settings[type + 'StopThreshold']  + typeUnit + '.')
        
        displayHumidityTempOver(type, typeString, typeUnit)
        displayHumidityTempStartThreshold(type, typeString, typeUnit)
        displayHumidityTempStopThreshold(type, typeString, typeUnit)

        if(!settings[type + 'StartThreshold'] && !settings[type + 'StopThreshold']) helpTip = 'Enter ' + typeString + typeUnit + ' at which to turn and/or turn off on the ' + pluralFan + '.'
        displayInfo(helpTip)
    
        displayHumidityTempStopThresholdManual(type, typeString, typeUnit)
    }
}

def displayHumidityTempStartThreshold(type, typeString, typeUnit){
    if(type == 'humidity'){
        if(!validateHumidity(settings[type + 'StartThreshold']) && validateHumidity(settings[type + 'StopThreshold'])) return
    }
    if(type == 'temp'){
        if(!validateTemp(settings[type + 'StartThreshold']) && validateTemp(settings[type + 'StopThreshold'])) return
    }
    fieldName = type + 'StartThreshold'
    fieldTitle = ''
    fieldTitle = addFieldName(fieldTitle,fieldName)
    displayLabel('To start ' + pluralFan)
    input fieldName, 'number', title: fieldTitle, required: false, submitOnChange:true
}

def displayHumidityTempStopThreshold(type, typeString, typeUnit){
    if(type == 'humidity'){
        if(validateHumidity(settings[type + 'StartThreshold']) && !validateHumidity(settings[type + 'StopThreshold'])) return
    }
    if(type == 'temp'){
        if(validateTemp(settings[type + 'StartThreshold']) && !validateTemp(settings[type + 'StopThreshold'])) return
    }
    fieldName = type + 'StopThreshold'
    fieldTitle = ''
    fieldTitle = addFieldName(fieldTitle,fieldName)
    displayLabel('To stop ' + pluralFan)
    input fieldName, 'number', title: fieldTitle, required: false, submitOnChange:true
}

def displayHumidityTempStopThresholdManual(type, typeString, typeUnit){
    if(type == 'humidity'){
        if(validateHumidity(settings[type + 'StartThreshold'])) return
        if(validateHumidity(settings[type + 'StopThreshold'])) return
    }
    if(type == 'temp'){
        if(validateTemp(settings[type + 'StartThreshold'])) return
        if(validateTemp(settings[type + 'StopThreshold'])) return
    }
    if(!settings[type + 'StopThreshold']) return
    fieldName = type + 'StopThresholdManual'
    fieldTitle = 'If manually turned on, don\'t turn off with ' + typeString + ' level.'
    if(settings[type + 'StopThresholdManual']){
        fieldTitle = 'If manually turned on, turn off if ' + settings[type + 'StopThreshold'] + typeUnit + ' humidity.'
        if(type == 'temp') fieldTitle = 'If manually turned on, turn off if ' + settings[type + 'StopThreshold'] + typeUnit + '.'
        helpTip = 'If manually turned on, the ' + pluralFan + ' will turn off if ' + typeString + ' is ' + settings[type + 'StopThreshold'] + typeUnit + ' (even if it is already under ' + settings[type + 'StopThreshold'] + typeUnit + ' when turned on). Click to not turn off based on ' + typeString + '.'
    }
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'bool', title: fieldTitle, submitOnChange:true
    displayInfo(helpTip)
}

def displayHumidityTempOver(type, typeString, typeUnit){
    if(type == 'humidity'){
        if(validateHumidity(settings[type + 'StartThreshold'])) return
        if(validateHumidity(settings[type + 'StopThreshold'])) return
    }
    if(type == 'temp'){
        if(validateTemp(settings[type + 'StartThreshold'])) return
        if(validateTemp(settings[type + 'StopThreshold'])) return
    }
    if((settings[type + 'StartThreshold'] && settings[type + 'StopThreshold']) || (!settings[type + 'StartThreshold'] && !settings[type + 'StopThreshold'])) return
    fieldName = type + 'Over'
    fieldTitle = '<b>Start if ' + typeString + ' is over ' + settings[type + 'StartThreshold'] + '.</b> Click to start is ' + typeString + ' is under ' + settings[type + 'StartThreshold'] + typeUnit + '.'
    if(settings[type + 'StopThreshold']) fieldTitle = '<b>Stop if ' + typeString + ' is under ' + settings[type + 'StopThreshold'] + typeUnit + '.</b> Click to stop is ' + typeString + ' is over ' + settings[type + 'StopThreshold'] + typeUnit + '.'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'bool', title: fieldTitle, required: false, default: true, submitOnChange:true
}

def displayHumidityRelativeChangeOption(){
    if(!settings['device']) return
    if(!humidityActive) return

    if(settings['humidityStartPercent']) humidityOn = Math.round(averageHumidity) + settings['humidityStartPercent'] + '%'
    if(settings['humidityStopPercent']) humidityOff = Math.round(averageHumidity) + settings['humidityStopPercent'] + '%'

    minutes = settings['relativeMinutes']
    if(!settings['relativeMinutes']) minutes = 'the specified number of'
    
    hidden = true
    if(settings['relativeMinutes'] != 5 && !settings['humidityStartPercent']) hidden = false
    if(validateMinutes(settings['relativeMinutes'])) hidden = false
    if(settings['humidityStartPercent'] && settings['humidityStopPercent'] && settings['humidityStopPercent'] > settings['humidityStartPercent']) hidden = false
    if(settings['humidityStartPercent'] && settings['humidityStopPercent'] == settings['humidityStartPercent']) hidden = false

    sectionTitle = 'Click to set humidity increase (optional)'
    //if(!settings['humidityStartPercent'])
    if(!settings['humidityStartPercent'] && !settings['humidityStopPercent']) sectionTitle = 'Click to set humidity increase (optional)'
    startIncrease = 'increase'
    startLevel = settings['humidityStartPercent']
    if(settings['humidityStartPercent'] && settings['humidityStartPercent'] < 0) {
        startIncrease = 'decrease'
        startLevel = settings['humidityStartPercent'] * -1
    }
    stopIncrease = 'above'
    stopLevel = settings['humidityStopPercent']
    if(settings['humidityStopPercent'] && settings['humidityStopPercent'] < 0) {
        stopIncrease = 'below'
        stopLevel = settings['humidityStopPercent'] * -1
    }
    if(settings['humidityStartPercent'] && settings['relativeMinutes']) sectionTitle = '<b>Start: Humidity ' + startIncrease + ' of ' + startLevel + '% within ' + minutes + ' min.</b>'
    if(settings['humidityStartPercent'] && !settings['relativeMinutes']) sectionTitle = '<b>Start: Humidity  ' + startIncrease + '  of ' + startLevel + '%</b>'
    if(settings['humidityStartPercent'] && settings['humidityStopPercent']) sectionTitle += '<br>'
    //if(settings['humidityStopPercent'] && !settings['humidityStartPercent'] && settings['relativeMinutes']) sectionTitle += '<b>Stop: Humidity ' + settings['humidityStopPercent'] + '% above start level within ' + minutes + ' min.</b>'
    if(settings['humidityStartPercent'] && settings['humidityStopPercent']) sectionTitle += '<b>Stop: Humidity ' + stopLevel + '% ' + stopIncrease + ' start level</b>'
    
    section(hideable: true, hidden: hidden, sectionTitle){
        if(settings['humidityStartPercent'] && settings['humidityStopPercent'] == settings['humidityStartPercent']) displayError('The starting and stopping levels can be the same, since it would turn on and off at the same time.')
        //Validate humidity (as percent change)?
        displayError(validateMinutes(settings['relativeMinutes']))
        
        displayRelativeMinutes()
        displayStartPercent()
        displayStopPercent()
            
        log.debug settings['humidityStartPercent'] + ' ' + settings['humidityStopPercent']
        if(!settings['humidityStartPercent'] && !settings['humidityStopPercent']) displayInfo('Enter percentage of humidity increase within ' + minutes + ' minutes to start the ' + pluralFan + ', relative to original level.')

        if(settings['humidityStartPercent'] && settings['humidityStopPercent']) helpTip = 'The ' + pluralHumidity + ' is currently at ' + averageHumidityText + ', so it would turn the ' + pluralFan + ' on if, within ' + minutes + ' minutes, it were to ' + startIncrease + ' to ' + humidityOn + ', and turn off only when at ' + humidityOff + '.'
        if(settings['humidityStartPercent'] && !settings['humidityStopPercent']) helpTip = 'The ' + pluralHumidity + ' is currently at ' + averageHumidityText + ', so it would turn turn the ' + pluralFan + ' on if, within ' + minutes + ' minutes, it were to ' + startIncrease + ' to ' + humidityOn + '.'
        displayInfo(helpTip)
    }
}

def displayRelativeMinutes(){
    fieldName = 'relativeMinutes'
    fieldTitle = 'Minutes between change'
    if(settings[fieldName] != 5) fieldTitle = 'Minutes between change (default 5)'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    input fieldName, 'number', title: fieldTitle, required: false, submitOnChange:true, defaultValue: 5
}

def displayStartPercent(){
    if(validateMinutes(settings['relativeMinutes'])) return
    fieldName = 'humidityStartPercent'
    fieldTitle = 'Humidity increase'
    fieldTitle = addFieldName(fieldTitle,fieldName)
    displayLabel('To start ' + pluralFan)
    input fieldName, 'decimal', title: fieldTitle, required: false, submitOnChange:true
}

def displayStopPercent(){
    if(!settings['humidityStartPercent']) return
    if(validateMinutes(settings['relativeMinutes'])) return
    fieldName = 'humidityStopPercent'
    fieldTitle = 'Percent above starting humidity (where zero (0) is the initial level)'
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

def displayScheduleSection(){
    if(!getSensorSet()) return
    if(!settings['device']) return
    helpTip = 'Scheduling only applies with this automation for ' + settings['device'] + '. To schedule the devices or default settings for them, use the Time app.'  

    // If only days entered
    sectionTitle='<b>'
    List dayList=[]
    settings['days'].each{
        dayList.add(it)
    }
    dayText = dayList.join(', ')
    if(!settings['inputStartType'] && !settings['inputStopType'] && settings['days']){
        sectionTitle += 'Only on: ' + dayText + '</b>' + moreOptions
        hidden = true
        // If only start time (and days) entered
    }  else if(checkTimeComplete('start') && settings['inputStartType'] && (!checkTimeComplete('stop') || !settings['inputStopType'])){
        sectionTitle = 'Beginning at ' + varStartTime
        if(settings['days']) sectionTitle += ' on: ' + dayText
        if(settings['months']) sectionTitle += '; in ' + monthText
        sectionTitle += '</b>'
        hidden = false
        // If only stop time (and day) entered
    } else if(checkTimeComplete('stop') && settings['inputStopType'] && (!checkTimeComplete('start') || !settings['inputStartType'])){
        sectionTitle = 'Ending at ' + varStopTime
        if(settings['days']) sectionTitle += ' on: ' + dayText
        if(settings['months']) sectionTitle += '; in ' + monthText
        sectionTitle += '</b>'
        hidden = false
        // If all options entered
    } else if(checkTimeComplete('start') && checkTimeComplete('stop') && settings['inputStartType'] && settings['inputStopType']){
        varStartTime = getTimeVariables('start')
        varStopTime = getTimeVariables('stop')
        sectionTitle = '<b>Only if between ' + varStartTime + ' and ' + varStopTime
        if(settings['days'] && settings['months']) {
            sectionTitle += '</b>'
        } else {
            sectionTitle += ' on: ' + dayText + '</b>'
            if(settings['months']) sectionTitle += '; in ' + monthText
            sectionTitle += '</b>'
        }
        hidden = true
        // If no options are entered
    } else {
        sectionTitle = 'Click to set schedule (optional)'
        hidden = true
    }

    section(hideable: true, hidden: hidden, sectionTitle){
        if(!settings['inputStartType']) displayInfo(helpTip)
        displayStartTypeOption()

        // Display exact time option
        if(settings['inputStartType'] == 'time'){
            displayTimeOption('start')
        } else if(settings['inputStartType']){
            // Display sunrise/sunset type option (at/before/after)
            displaySunriseTypeOption('start')
            // Display sunrise/sunset offset
            if(inputStartSunriseType && inputStartSunriseType != 'at') displaySunriseOffsetOption('start')
        }

        if(checkTimeComplete('start') && settings['inputStartType']){
            displayStopTypeOption()

            // Display exact time option
            if(settings['inputStopType'] == 'time'){
                displayTimeOption('stop')
            } else if(settings['inputStopType']){
                // Display sunrise/sunset type option (at/before/after)
                displaySunriseTypeOption('stop')
                // Display sunrise/sunset offset
                if(inputStopSunriseType && inputStopSunriseType != 'at') displaySunriseOffsetOption('stop')
            }
        }

        displayDaysOption(dayText)
        displayMonthsOption(monthText)

        displayInfo(message)
    }
}

def displayDaysOption(dayText){
    fieldName = 'days'
    fieldTitle = 'On these days (defaults to all days)'
    input fieldName, 'enum', title: addFieldName(fieldTitle,fieldName), multiple: true, width: 12, options: ['Monday': 'Monday', 'Tuesday': 'Tuesday', 'Wednesday': 'Wednesday', 'Thursday': 'Thursday', 'Friday': 'Friday', 'Saturday': 'Saturday', 'Sunday': 'Sunday'], submitOnChange:true

    return
}

def displayMonthsOption(monthText){
    fieldName = 'months'
    fieldTitle = 'In these months (defaults to all months)'
    input fieldName, 'enum', title: addFieldName(fieldTitle,fieldName), multiple: true, width: 12, options: ['1': 'January', '2': 'February', '3': 'March', '4': 'April', '5': 'May', '6': 'June', '7': 'July', '8': 'August', '9': 'September', '10': 'October', '11': 'November', '12': 'December'], submitOnChange:true
}

def displayStartTypeOption(){
    if(checkTimeComplete('start') && settings['inputStartType']) displayLabel('Schedule start')
    if(!checkTimeComplete('start')  || !settings['inputStartType']) displayLabel('Schedule starting time')
    fieldName = 'inputStartType'
    if(settings[fieldName]){
        fieldTitle = 'Start time option:'
        if(settings[fieldName] == 'time' || !settings['inputStartSunriseType'] || settings['inputStartSunriseType'] == 'at'){
            width = 6
        } else if(settings['inputStartSunriseType']){
            width = 4
        }
        input fieldName, 'enum', title: addFieldName(fieldTitle,fieldName), multiple: false, width: width, options: ['time':'Start at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)' ], submitOnChange:true
    }
    if(!settings[fieldName]){
        fieldTitle = 'Start time (click to choose option):'
        width = 12
        input fieldName, 'enum', title: addFieldName(fieldTitle,fieldName), multiple: false, width: width, options: ['time':'Start at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)' ], submitOnChange:true
        displayInfo('Select whether to enter a specific time, or have start time based on sunrise and sunset for the Hubitat location. Required field for a schedule.')
    }
}

def displayStopTypeOption(){
    if(!checkTimeComplete('stop')){
        displayLabel('Schedule stopping time')
    } else {
        displayLabel('Schedule stop')
    }
    fieldName = 'inputStopType'
    if(settings[fieldName]){
        fieldTitle = 'Stop time option:'
        if(!settings['inputStopType'] || settings['inputStopType'] == 'none'){
            width = 12
        } else if(settings['inputStopType'] == 'time' || !settings['inputStopSunriseType'] || settings['inputStopSunriseType'] == 'at'){
            width = 6
        } else if(inputStopSunriseType){
            width = 4
        }
        input fieldName, 'enum', title: addFieldName(fieldTitle,fieldName), multiple: false, width: width, options: ['time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)' ], submitOnChange:true
    }
    if(!settings[fieldName]){
        width = 12
        fieldTitle = 'Stop time (click to choose option):'
        input fieldName, 'enum', title: addFieldName(fieldTitle,fieldName), multiple: false, width: width, options: ['time':'Stop at specific time', 'sunrise':'Sunrise (at, before or after)','sunset':'Sunset (at, before or after)' ], submitOnChange:true
    }
}

def displayTimeOption(lcType){
    fieldName = 'input' + ucType + 'Time'
    fieldTitle = ucType + ' time:'
    ucType = lcType.capitalize()
    input fieldName, 'time', title: addFieldName(fieldTitle,fieldName), width: width, submitOnChange:true
    if(!settings[fieldName]) displayInfo('Enter the time to ' + lcType + ' the schedule in "hh:mm AM/PM" format. Required field.')
}

def displaySunriseTypeOption(lcType){
    if(!settings['input' + ucType + 'SunriseType'] || settings['input' + ucType + 'SunriseType'] == 'at') {
        width = 6 
    } else {
        width = 4
    }
    // sunriseTime = getSunriseAndSunset()[settings['input' + ucType + 'Type']].format('hh:mm a')
    fieldName = 'input' + ucType + 'SunriseType'
    fieldTitle = 'At, before or after ' + settings['input' + ucType + 'Type'] + ':'
    input fieldName, 'enum', title: addFieldName(fieldTitle,fieldName), multiple: false, width: width, options: ['at':'At ' + settings['input' + ucType + 'Type'], 'before':'Before ' + settings['input' + ucType + 'Type'], 'after':'After ' + settings['input' + ucType + 'Type']], submitOnChange:true
    if(!settings[fieldName]) displayInfo('Select whether to start exactly at ' + settings['input' + ucType + 'Type'] + ' (currently, ' + sunriseTime + '). To allow entering minutes prior to or after ' + settings['input' + ucType + 'Type'] + ', select "Before ' + settings['input' + ucType + 'Type'] + '" or "After ' + settings['input' + ucType + 'Type'] + '". Required field.')
}

def checkTimeComplete(lcType){
    ucType = lcType.capitalize()

    // If everything entered
    if((settings['input' + ucType + 'Type'] == 'time' && settings['input' + ucType + 'Type']) || 
       ((settings['input' + ucType + 'Type'] == 'sunrise' || settings['input' + ucType + 'Type'] == 'sunset') && settings['input' + ucType + 'SunriseType'] == 'at') || 
       ((settings['input' + ucType + 'Type'] == 'sunrise' || settings['input' + ucType + 'Type'] == 'sunset') && (settings['input' + ucType + 'SunriseType'] == 'before' || settings['input' + ucType + 'SunriseType'] == 'after') && (settings['input' + ucType + 'Before']))){
        return true
    } else if(!settings['input' + ucType + 'Type'] && !settings['input' + ucType + 'SunriseType'] && !settings['input' + ucType + 'Before']){
        return true
    } else {
        return false
    }
}

def getTimeVariables(lcType){
    ucType = lcType.capitalize()
    type = settings['input' + ucType + 'Type']
    sunriseType = settings['input' + ucType + 'SunriseType']
    before = settings['input' + ucType + 'Before']
    // If time, then set string to "[time]"
    if(type == 'time'){
        return Date.parse("yyyy-MM-dd'T'HH:mm:ss", settings['input' + ucType + 'Time']).format('h:mm a', location.timeZone)
        // If sunrise or sunset
    } else if((type == 'sunrise' || type == 'sunset')  && sunriseType){
        if(sunriseType == 'at'){
            // Set string to "sun[rise/set] ([sunrise/set time])"
            return type + ' (' + getSunriseAndSunset()[type].format('hh:mm a') + ')'
            // If before sunrise
        } else if(type == 'sunrise' && sunriseType == 'before' && before){
            // Set string to "[number] minutes before sunrise ([time])
            if(before) return before + ' minutes ' + sunriseType + ' ' + type + ' (' + getSunriseAndSunset(sunriseOffset: (before * -1), sunsetOffset: 0)[type].format('hh:mm a') + ')'
            // If after sunrise
        } else if(type== 'sunrise' && sunriseType == 'after' && before){
            // Set string to "[number] minutes after sunrise ([time])
            if(before) return before + ' minutes ' + sunriseType + ' ' + type + ' (' + getSunriseAndSunset(sunriseOffset: before, sunsetOffset: 0)[type].format('hh:mm a') + ')'
            // If before sunset
        } else if(type == 'sunset' && sunriseType == 'before' && before){
            // Set string to "[number] minutes before sunset ([time])
            if(before) return before + ' minutes ' + sunriseType + ' ' + type + ' (' + getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: (before * -1))[type].format('hh:mm a') + ')'
            // If after sunrise
        } else if(type == 'sunset' && sunriseType == 'after' && before){
            // Set string to "[number] minutes after sunset ([time])
            if(before) return before + ' minutes ' + sunriseType + ' ' + type + ' (' + getSunriseAndSunset(sunriseOffset: 0, sunsetOffset: before)[type].format('hh:mm a') + ')'
        }
    }
}

def displaySunriseOffsetOption(lcType){
    ucType = lcType.capitalize()
    type = settings['input' + ucType + 'Type']
    sunriseType = settings['input' + ucType + 'SunriseType']
    before = settings['input' + ucType + 'Before']
    if(!sunriseType || sunriseType == 'at') return

    if(before && before > 1441){
        // "Minues [before/after] [sunrise/set] is equal to "
        message = 'Minutes ' + sunriseType + ' ' + type + ' is equal to '
        if(before  > 2881){
            // "X days"
            message += Math.floor(before  / 60 / 24) + " days."
        } else {
            message += 'a day.'
        }
        warningMessage(message)
    }
    fieldName = 'input' + ucType + 'Before'
    fieldTitle = 'Minutes ' + sunriseType + ' ' + type + ':'
    input fieldName, 'number', title: addFieldName(fieldTitle,fieldName), width: 4, submitOnChange:true
    if(!settings[fieldName]) displayInfo('Enter the number of minutes ' + sunriseType + ' ' + type + ' to start the schedule. Required field.')
}

def getSunriseTime(type,sunOffset,sunriseType){
    if(type == 'sunrise' && sunriseType == 'before' && sunOffset) return '(' + new Date(parent.getSunrise(sunOffset * -1)).format('hh:mm a') + ')'
    if(type == 'sunrise' && sunriseType == 'after' && sunOffset) return '(' + new Date(parent.getSunrise(sunOffset)).format('hh:mm a') + ')'
    if(type == 'sunset' && sunriseType == 'before' && sunOffset) return '(' + new Date(parent.getSunset(sunOffset * -1)).format('hh:mm a') + ')'
    if(type == 'sunset' && sunriseType == 'after' && sunOffset) return '(' + new Date(parent.getSunset(sunOffset)).format('hh:mm a') + ')'
    if(type == 'sunrise' && sunriseType == 'at') return '(' + new Date(parent.getSunrise(0)).format('hh:mm a') + ')'
    if(type == 'sunset' && sunriseType == 'at') return '(' + new Date(parent.getSunset(0)).format('hh:mm a') + ')'
    
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

/*
Input fields:

humiditySensor - device - required - multiple
	primary humidity sensor
humiditySensorAverage - bool
	false = average
disable - bool
	disables this humidity routine
switches - device - required - multiple
	fan switch to be turned on/off
humidityControlEnable - bool
	shows options for a "control" sensor
	false = using control device
	true = not using control device
humidityControlDevice - device
	"control" device. Only appears if humidityControlEnable, but could be set regardless. Must be combined with controlStartDifference
controlStartDifference - number - required
	difference between primary and control sensors. Only appears if humidityControlEnable, but could be set regardless. Must be combined with humidityControlDevice
humidityStartThreshold - number
	absolute sensor value to turn on
humidityIncreaseRate - number
	amount of change with sensor to turn on
multiStartTrigger - bool
	indicates to use controlStartDifference, humidityStartThreshold, AND/OR humidityIncreaseRate
	true = and
	false = or
controlStopDifference - number
	percent difference of sensor compared to control sensor to turn off
humidityStopThreshold - number
	absolute sensor value to turn off
humidityStopPercent - number
	percent over starting point to turn off
humidityWaitMinutes - number
	minutes after controlStopDifference, humidityStopThreshold, and/or humidityStopPercent to wait
runTimeMaximum - number
	minutes of run time to turn off
multiStopTrigger - bool
	indicates to use controlStopDifference, humidityStopThreshold, humidityStopPercent AND/OR runTimeMaximum
	true = and
	false = or
humidityDropTimeout - number
	NOT USED
controlStopDifferenceManual - bool
	if manually on, indicates to use controlStopDifference to turn off
humidityStopThresholdManual - bool
	if manually on, indicates to use humidityStopThreshold to turn off
runTimeMaximumManual - bool
	if manually on, indicates to use runTimeMaximum to turn off
*/

def installed() {
    state.logLevel = getLogLevel()
    
    putLog(1376,'trace','Installed')
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))
    initialize()
}

def updated() {
    state.logLevel = getLogLevel()
    putLog(1383,'trace','Updated')
    unsubscribe()
    initialize()
}

def initialize() {
    state.logLevel = getLogLevel()
    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

    // If date/time for last notification not set, initialize it to 5 minutes ago
    if(!state.contactLastNotification) state.contactLastNotification = new Date().getTime() - 360000

    if(settings['disable']) {
        state.disable = true
        return
    }
    state.disable = false
    
	unschedule()
    
    getSensorType()
    state.humidityActive = humidityActive
    state.tempActive = tempActive
    if(!humidityActive && !tempActive) return
    
    setTime()
    if(parent.isAnyOnMulti(settings['device'])) scheduleMaximum()
       
    if(humidityActive) subscribe(settings['humiditySensor'], 'humidity', humidityHandler)
    if(humidityActive && settings['humidityControlSensor']) subscribe(settings['humidityControlSensor'], 'humidity', temperatureHandler)
    if(tempActive) subscribe(settings['tempSensor'], 'temperature', temperatureHandler)
    if(tempActive && settings['tempControlSensor']) subscribe(settings['tempControlSensor'], 'temperature', temperatureHandler)
    subscribe(settings['device'], 'switch', switchHandler)
    
    putLog(1417,'trace','Initialized')
}

def humidityHandler(event) {
    if(!state.humidityActive) return
    updateStatus()
}

def temperatureHandler(event) {
    if(!state.tempActive) return
    updateStatus()
}

def checkControlStartDifference(){
    if(!settings['controlStartDifference']) return

    if(averageHumidity > averageControlhumidity + settings['controlStartDifference']) return true
}

def checkControlStopDifference(){
    if(!settings['controlStopDifference']) return
    if(state.manualOn && !settings['controlStopDifferenceManual']) return

    if(averageHumidity < averageControlhumidity + settings['controlStopDifference']) return true
}

def checkHumidityStartThreshold(){
    if(!state.humidityActive) return
    if(!settings['humidityStartThreshold']) return
    if(settings['humidityOver'] && averageHumidity < settings['humidityStartThreshold']) return true
    if(!settings['humidityOver'] && averageHumidity > settings['humidityStartThreshold']) return true
}

def checkHumidityStopThreshold(){
    if(!state.humidityActive) return
    if(!settings['humidityStopThreshold']) return
    if(state.manualOn && !settings['humidityStopThresholdManual']) return
    if(settings['humidityOver'] && averageHumidity > settings['humidityStopThreshold']) return true
    if(!settings['humidityOver'] && averageHumidity < settings['humidityStopThreshold']) return true
}

def checkTempStartThreshold(){
    if(!state.tempActive) return
    if(!settings['tempStartThreshold']) return
    if(settings['tempOver'] && averageTemp > settings['tempStartThreshold']) return true
    if(!settings['tempOver'] && averageTemp < settings['tempStartThreshold']) return true
}

def checkTempStopThreshold(){
    if(!state.tempActive) return
    if(!settings['tempStopThreshold']) return
    if(state.manualOn && !settings['tempStopThresholdManual']) return

    if(settings['tempOver'] && averageTemp < settings['tempStopThreshold']) return true
    if(!settings['tempOver'] && averageTemp > settings['tempStopThreshold']) return true
}

def checkHumidityStartPercent(){
    if(!state.humidityActive) return
    if(!settings['humidityStartPercent']) return
    if(!settings['relativeMinutes']) return
    if(!state.startHumidity) return
    
    if(averageHumidity > state.startHumidity + settings['humidityStartPercent']) return true
}

def checkHumidityStopPercent(){
    if(!settings['humidityStopPercent']) return
    if(!settings['relativeMinutes']) return
    if(!state.startHumidity) return
    
    if(averageHumidity < state.startHumidity + settings['humidityStopPercent']) return true
}

def checkRunTimeMinimum(){
    if(!settings['runTimeMinimum']) return
    if(!state.startTime) return
    
    if(now - state.startTime > settings['runTimeMinimum'] * 60000) return true
}

//Returns true if condition is met
def checkRunTimeMaximum(){
    if(!settings['runTimeMaximum']) return true
    if(!state.startTime) return true
    
    if(now - state.startTime > settings['runTimeMaximum'] * 60000){
        putLog(1504,'trace','Maximum runtime exceeded.')
        return true
    }
}


def checkMinimumWaitTime(){
    if(!settings['runTimeMaximum']) return true
    if(!state.stopTime) return true
    
    if(now - state.stopTime > settings['runTimeMaximum'] * 60000){
        putLog(1515,'trace','Minimum wait time exceeded.')
        return true
    }
}

def checkOnConditions(){
    if(!checkMinimumWaitTime()) return
    if(settings['multiStartTrigger']) {
        allOnConditions = checkAllOnConditions()
        putLog(1524,'trace','All on conditions is ' + allOnConditions)
    }
    if(!settings['multiStartTrigger']) {
        anyOnConditions = checkAnyOnConditions()
        putLog(1528,'trace','Any on condition is ' + anyOnConditions)
            return anyOnConditions
    }
}

def checkOffConditions(){
    if(!checkRunTimeMaximum()) return
    
    if(settings['multiStopTrigger']) {
        allOffConditions = checkAllOffConditions()
        putLog(1538,'trace','All off conditions is ' + allOffConditions)
        return allOffConditions
    }
    if(!settings['multiStopTrigger']) {
        anyOffConditions = checkAnyOffConditions()
        putLog(1543,'trace','Any off conditions is ' + anyOffConditions)
        return anyOffConditions
    }
}

def checkAllOnConditions(){
    //False value used for log
    if(settings['controlStartDifference'] && !checkControlStartDifference()) return false
    if(settings['humidityStartThreshold'] && !checkHumidityStartThreshold()) return false
    if(settings['humidityStartPercent'] && !checkHumidityStartPercent()) return false
    if(settings['tempStartThreshold'] && !checkTempStartThreshold()) return false
    
    return true
}

def checkAllOffConditions(){
    //False value used for log
    if(checkRunTimeMinimum()) return false
    if(settings['controlStopDifference'] && !checkControlStopDifference()) return false
    if(settings['humidityStopThreshold'] && !checkHumidityStopThreshold()) return false
    if(settings['humidityStopPercent'] && !checkHumidityStopPercent()) return false
    if(settings['tempStopThreshold'] && !checkTempStopThreshold()) return false
    
    if(checkRunTimeMinimum()) return true
}

def checkAnyOnConditions(){
    if(checkControlStartDifference()) return true
    if(checkHumidityStartThreshold()) return true
    if(checkHumidityStartPercent()) return true
    if(checkTempStartThreshold()) return true
    return false    // used for log
}

def checkAnyOffConditions(){
    if(checkRunTimeMinimum()) return false
    if(checkControlStopDifference()) return true
    if(checkHumidityStopThreshold()) return true
    if(checkHumidityStopPercent()) return true
    if(checkTempStopThreshold()) return true
    return false    // used for log
}

def scheduleTurnOff() {
    if(state.manualOn && !settings['runTimeMaximumManual']) return
    state.manualOn = false
    if(!parent.isAnyOnMulti(settings['device'],app.label)) return
    //check minimum run time
    //clear any (all) schedules
    state.startTime = null
    state.stopTime = new Date().getTime()
    parent.updateStateMulti(settings['device'],'off',app.label)
    parent.setStateMulti(settings['device'],app.label)
    putLog(1596,'debug','Turning off ' + settings['device'] + ' from schedule.')
}

def updateRelativeHumidityArray(){
    if(!state.humidityActive) return
    timeNow = new Date().getTime()
    timeLimit = timeNow - settings['relativeMinutes'] * 60000
    if(!state.humidityChanges){
        state.humidityChanges = ['1':[time:timeNow,humidity:averageHumidity]]
        return
    }
    itemCount = 0
    newArray = [:]
    state.humidityChanges.each{
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
        newArray[itemCount]  = [time:timeNow,humidity:averageHumidity]
    state.humidityChanges = newArray
}

def scheduleMaximum(){
    if(!settings['runTimeMaximum']) return
    unschedule()
    // Need to check if already scheduled
    runIn(60 * settings['runTimeMaximum'].toInteger(), updateStatus)
    putLog(1638,'trace', 'Scheduling turn off in ' + settings['runTimeMaximum'] + ' minutes.')
}

def updateStatus(){
    if(settings['disable']) return

    // If mode set and node doesn't match, return nulls
    if(settings['ifMode'] && location.mode != settings['ifMode']) {
        putLog(1646,'trace','Humidity/temp disabled, requires mode ' + ifMode)
        return
    }
    
    // If not correct day, return nulls
    if(!parent.nowInDayList(settings['days'],app.label)) return
    if(!parent.nowInMonthList(settings['months'],app.label)) return

    // if not between start and stop time, return nulls
    if(!parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return

    if(!parent.getPeopleHome(settings['personHome'],app.label)) return
    if(!parent.getNooneHome(settings['personNotHome'],app.label)) return
    
    averageHumidity = averageHumidity(settings['humiditySensor'],settings['tempSensor'])
    averageTemp = averageTemp(settings['humiditySensor'],settings['tempSensor'])
    averageControlHumidity = averageHumidity(settings['humidityControlSensor'],settings['tempControlSensor'])
    averageControlTemp = averageTemp(settings['humidityControlSensor'],settings['tempControlSensor'])

    updateRelativeHumidityArray()
    
    turnOnStatus = checkOnConditions()
    turnOffStatus = checkOffConditions()

    if(turnOnStatus && turnOffStatus){
        putLog(1671,'warn','Both on and off conditions met.')
        return
    }
    if(turnOnStatus && !turnOffStatus) turnOn()
    if(turnOffStatus && !turnOnStatus) turnOff()
}

def turnOn(){
    if(parent.isAllOnMulti(settings['device'],app.label)) return
    if(!state.manualOn) return
    unschedule()
    state.manualOn = false
    state.startTime = new Date().getTime()
    state.stopTime = null
    scheduleMaximum()
    parent.updateStateMulti(settings['device'],'on',app.label)
    parent.setStateMulti(settings['device'],app.label)
    putLog(1688,'debug','Turning on ' + settings['device'] + '.')
    sendNotice()
}

def turnOff(){
    if(state.manualOn) return
    if(!parent.isAnyOnMulti(settings['device'],app.label)) return
    state.manualOn = false
    state.startTime = null
    state.stopTime = new Date().getTime()
    unschedule()
    parent.updateStateMulti(settings['device'],'off',app.label)
    parent.setStateMulti(settings["device"],app.label)
    putLog(1701,'debug','Turning off ' + settings['device'] + '.')
}


def sendNotice(){
    if(settings['pushNotificationDevice'] && settings['pushNotification']) sendPushNotice()
    if(settings['speechDevice'] && settings['speech']) speakNotice()
}

def sendPushNotice(){
    def now = new Date()getTime()

    //if last text was sent less than 5 minutes ago, don't send
    /* ************************************************************************ */
    /* TO-DO: Add option to override text cooldown period? (Maybe in Master?)   */
    /* Same with presence app.                                                  */
    /* ************************************************************************ */
    // Compute seconds from last notification
    seconds = (now - state.contactLastNotification) / 1000

    // Convert date to friendly format for log
    if(seconds > 360){
        state.contactLastNotification = now

        settings['pushNotificationDevice'].each{
            parent.sendPushNotification(it,evt.displayName + ' was ' + eventName + ' at ' + now.format('h:mm a', location.timeZone),app.label)
        }
        putLog(1728,'info','Sent push notice for ' + evt.displayName + ' ' + eventName + ' at ' + now.format('h:mm a', location.timeZone) + '.')
    } else {
        putLog(1730,'info','Did not send push notice for ' + evt.displayName + ' ' + evt.value + ' due to notification sent ' + seconds + 's ago.')
    }
}

def speakNotice(){
    settings['speech'].each{
        parent.speakSingle(settings['speech'],it,app.label)
    }
}

def isHumidityDevice(singleDevice){
    if(singleDevice.hasCapability("RelativeHumidityMeasurement")) return true
    return
}

def isTemperatureDevice(singleDevice){
    if(singleDevice.hasCapability("TemperatureMeasurement")) return true
    return
}

def switchHandler(evt) {
    if(evt.value == 'off') return
    if(settings['disable']) return

    // If mode set and node doesn't match, return nulls
    if(settings['ifMode'] && location.mode != settings['ifMode']) {
        putLog(1756,'trace','Humidity/temp disabled, requires mode ' + ifMode)
        return
    }
    
    // If not correct day, return nulls
    if(!parent.nowInDayList(settings['days'],app.label)) return
    if(!parent.nowInMonthList(settings['months'],app.label)) return

    // if not between start and stop time, return nulls
    if(!parent.timeBetween(atomicState.start, atomicState.stop, app.label)) return

    if(!parent.getPeopleHome(settings['personHome'],app.label)) return
    if(!parent.getNooneHome(settings['personNotHome'],app.label)) return
    
    scheduleMaximum()	   
}

def averageHumidity(firstMultiDevice, secondMultiDevice = null){
    //if(!state.humidityActive) return
    if(!firstMultiDevice && !secondMultiDevice) return
    
    humidity = 0
    deviceCount = 0
    if(firstMultiDevice){
    firstMultiDevice.each {singleDevice->
        if(isHumidityDevice(singleDevice)) humidity += singleDevice.currentHumidity
        deviceCount++
            }
    }
    if(secondMultiDevice){
        secondMultiDevice.each {singleDevice->
        if(isHumidityDevice(singleDevice)) humidity += singleDevice.currentHumidity
        deviceCount++
            }
    }
    
    if(deviceCount > 0)  return humidity / deviceCount
    return 0
}

def averageTemp(firstMultiDevice, secondMultiDevice = null){
    //if(!state.tempActive) return
    if(!firstMultiDevice && !secondMultiDevice) return

    temp = 0
    deviceCount = 0
    if(firstMultiDevice){
        firstMultiDevice.each {singleDevice->
            if(isTemperatureDevice(singleDevice)) {
                temp += singleDevice.currentTemperature
                deviceCount++
                    }
                }
    }
    if(secondMultiDevice){
        secondMultiDevice.each {singleDevice->
            if(isTemperatureDevice(singleDevice)) {
                temp += singleDevice.currentTemperature
                deviceCount++
                    }
                }
    }
    if(deviceCount > 0) return temp / deviceCount
    return 0
}

def setTime(){
    if(setStartTime()) {
        setStopTime()
        return true
    }
    return false
}

def setStartTime(){
    if(!settings['start_timeType']) {
        atomicState.start = null
        return
    }
    setTime = setStartStopTime('start')
    if(setTime){
        atomicState.start = setTime
        putLog(1838,'info','Start time set to ' + parent.normalPrintDateTime(setTime))
        return true
    }
}

def setStopTime(){
    if(!settings['start_timeType'] || settings['stop_timeType'] == 'none') {
        atomicState.stop = null
        return
    }
    setTime = setStartStopTime('stop')
    if(setTime){ 
        if(atomicState.start > setTime) setTime = parent.getTomorrow(setTime,app.label)
        atomicState.stop = setTime
        putLog(1852,'info','Stop time set to ' + parent.normalPrintDateTime(setTime))
    }
    return
}

// Sets atomicState.start and atomicState.stop variables
// Requires type value of "start" or "stop" (must be capitalized to match setting variables)
def setStartStopTime(type){
    if(settings['input' + type + 'Type'] == 'time') return Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSSZ", settings['input' + type + 'Type']).getTime()
    if(settings['input' + type + 'Type'] == 'sunrise') return (settings['input' + type + 'SunriseType'] == 'before' ? parent.getSunrise(settings['input' + type + 'Before'] * -1,app.label) : parent.getSunrise(settings['input' + type + 'Before'],app.label))
    if(settings['input' + type + 'Type'] == 'sunset')return (settings['input' + type + 'SunriseType'] == 'before' ? parent.getSunset(settings['input' + type + 'Before'] * -1,app.label) : parent.getSunset(settings['input' + type + 'Before'],app.label))
}

def checkLog(type = null){
    switch(type) {
        case 'error':
        if(getLogLevel() > 0) return true
        break
        case 'warn':
        if(getLogLevel() > 1) return true
        break
        case 'info':
        if(getLogLevel() > 2) return true
        break
        case 'trace':
        if(getLogLevel() > 3) return true
        break
        case 'debug':
        if(getLogLevel() == 5) return true
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,type = 'trace',message = null){
    if(!checkLog(type)) return
    return parent.putLog(lineNumber,type,message,app.label)
}
