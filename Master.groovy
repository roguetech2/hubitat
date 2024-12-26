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
*  Name: Master
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master.groovy
*  Version: 0.4.1.44
*
***********************************************************************************************************************/

// Add tooltips for use-cases with apps
// Add toggle to disable them
// Add "CONST"s as settings

/***********************************************************************************************************************
*
*  Speech device is set as "speechSynthesis". The device capability must be set in the driver, or change this to
*  "voice".
*
***********************************************************************************************************************/

definition(
    name: 'Master',
    namespace: 'master',
    singleInstance: true,
    author: 'roguetech',
    description: 'Parent app for automation suite',
    category: 'Convenience',
    importUrl: 'https://raw.githubusercontent.com/roguetech2/hubitat/master/Master.groovy',
    iconUrl: 'http://cdn.device-icons.smartthings.com/home2-icn@2x.png',
    iconX2Url: 'http://cdn.device-icons.smartthings.com/home2-icn@2x.png'
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

preferences {
    page(name: 'mainPage', install: true, uninstall: true){
        section(){
            getAppCounts()
            if(!atomicState?.masterInstalled) displayInfo ('Click "Done" to save.')

            if(!allDevices) displayAllDevicesOption()
            displayHiRezHue()
        }
        displayHideTips()
        displaySchedules()
        displaySensors()
        displayPicos()
        displayMagicCubes()
        displayContactSensors()
        displayPresence()
        if(allDevices) displayAllDevicesOption()
    }
}

def getAppCounts(){
    scheduleCount = 0
    presenceCount = 0
    picoCount = 0
    magicCubeCount = 0
    contactCount = 0
    sensorCount = 0
    childApps.each {Child->
        if(Child.label){
            if(Child.label.startsWith('Time - ')) scheduleCount++
            if(Child.label.startsWith('Presence - ')) presenceCount++
            if(Child.label.startsWith('Pico - ')) picoCount++
            if(Child.label.startsWith('MagicCube - ')) magicCubeCount++
            if(Child.label.startsWith('Contact - ')) contactCount++
            if(Child.label.startsWith('Sensor - ')) sensorCount++
        }
    }
}

def displayAllDevicesOption(){
    fieldName = 'allDevices'
    fieldTitle = 'Select all devices:'
    if(allDevices){
        section(){
            displayInfo('All devices should be selected. This is used to make the app suite of easier to use, but is not required. You can deselect any devices that are never used and/or any virtual devices.')
            input name: fieldName, title: fieldTitle, type: 'capability.*', multiple: true, filter:true,width:12,submitOnChange:true
        }
    }
    if(!allDevices) {
        
    input name: fieldName, title: fieldTitle, type: 'capability.*', multiple: true, filter:true,width:12,submitOnChange:true
    displayInfo('Select all devices ("Toggle All On"). This is used to make the app suite of easier to use, but is not required. You can deselect any devices that are never used and/or any virtual devices.')
    }
}

def displayHiRezHue(){
    input name: 'hiRezHue', type: 'bool', title: 'Enable Hue in degrees (0-360)', defaultValue: false, submitOnChange:true
    displayInfo('Select if light devices have been set to degrees. Leave unselected by default, but failure to match device settings may result in hue settings not working correctly.')
}

def displayHideTips(){
    section(){
        fieldName = 'hideTips'
        fieldTitle = 'Hide tips'
        input name: fieldName, title: fieldTitle, type: 'bool', submitOnChange:true
    }
}

def displaySchedules(){
    title = 'Click to add a <b>schedule</b>'
    if(scheduleCount != 0) title = 'Click to add or edit <b>schedules</b> (' + scheduleCount + ' total)'

        displayScheduleTip()
    section(hideable: true, hidden: true, title) {
        app(name: 'childApps', appName: 'Master - Time', namespace: 'master', title: 'New Schedule', multiple: true)
    }
}

def displayPicos(){
    section(){
        displayPicoTip()
        title = 'Click to add '
        if(picoCount == 0) title += 'a <b>Pico</b>'
        if(picoCount != 0) title += 'or edit <b>Picos</b> (' + picoCount + ' total)'
        section(hideable: true, hidden: true, title) {
            app(name: 'childApps', appName: 'Master - Pico', namespace: 'master', title: 'New Pico', multiple: true)
        }
    }
}

def displayMagicCubes(){
    section(){
        displayMagicCubeTip()
        title = 'Click to add '
        if(magicCubeCount == 0) title += 'a <b>MagicCube</b>'
        if(magicCubeCount != 0) title += 'or edit <b>MagicCubes</b> (' + magicCubeCount + ' total)'
        section(hideable: true, hidden: true, title) {
            app(name: 'childApps', appName: 'Master - MagicCube', namespace: 'master', title: 'New MagicCube', multiple: true)
        }
    }
}

def displaySensors(){
    section(){
        title = 'Click to add '
        if(sensorCount == 0) title += 'a <b>Sensor</b>'
        if(sensorCount != 0) title += 'or edit <b>sensors</b> (' + sensorCount + ' total)'
        section(hideable: true, hidden: true, title) {
            app(name: 'childApps', appName: 'Master - Sensor', namespace: 'master', title: 'New Sensor', multiple: true)
        }
    }
}
def displayContactSensors(){
    section(){
        title = 'Click to add '
        if(contactCount == 0) title += 'a <b>contact/door sensor</b>'
        if(contactCount != 0) title += "or edit <b>contact/door sensors</b> ($contactCount total)"
        section(hideable: true, hidden: true, title) {
            app(name: 'childApps', appName: 'Master - Contact', namespace: 'master', title: 'New Contact Sensor', multiple: true)
        }
    }
}

def displayPresence(){
    section(){
        title = 'Click to add '
        if(presenceCount == 0) {
            title += 'a <b>presence</b> setting'
        } else {
            title += "or edit <b>presence</b> settings ($presenceCount total)"
        }
        section(hideable: true, hidden: true, title) {
            app(name: 'childApps', appName: 'Master - Presence', namespace: 'master', title: 'New Presence', multiple: true)
        }
    }
}

/*
if(notificationDevice && people){
section(""){
def i = 0
people.each{
i++
paragraph "<div style=\"background-color:BurlyWood\"><b> Sing Happy Birthday for $it:</b></div>"
if(i == 1) paragraph "This sets Google Home to sing Happy Birthday on the individual's birthday between the times entered, a few minutes after they (or a few minutes after the start time, if they're already home). It can be set to wait for everyone to be home before singing."
if(!settings["happyBDay$i"]){
input "happyBDay${i}", "bool", title: "Do not sing Happy Birthday for $it? Click to sing.", submitOnChange:true
} else {
input "happyBDay${i}", "bool", title: "Sing Happy Birthday for $it? Click to not sing.", submitOnChange:true
}
if(settings["happyBDay$i"]){
input "dateBDay${i}", "date", title: "Enter birthday date for $it."
if(!settings["bDayEveryone$i"]){
input "bDayEveryone${i}", "bool", title: "Sing regardless of who else is home. Click to only sing with everyone.", submitOnChange:true
} else {
input "bDayEveryone${i}", "bool", title: "Only sing when everyone is home. Click to sing regardless who's home.", submitOnChange:true
}
input "timeStart", "time", title: "Only sing between start time", required: true, width: 6, submitOnChange:true
input "timeStop", "time", title: "and stop time", required: true, width: 6, submitOnChange:true
}
}

}
}
*/


/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

/* ************************************************************************ */
/*                                                                          */
/*                      End display functions.                              */
/*                                                                          */
/* ************************************************************************ */

def installed() {
    atomicState.masterInstalled = true
    initialize()
}

def updated() {
    initialize()
}

def initialize() {
}

// Returns app name with app title prepended
def appendAppTitle(appName,appTitle){
    //Compare length of name (eg "test") to appTitle length minus 6 (eg "Master - Time - " minus "Master - "; "Time - " is min length)
    if(appName.length() < appTitle.length() - 6){
        returnValue = appTitle.substring(9,appTitle.length()) + ' - ' + appName
        //Compare first part of name (eg "Testing") to middle part of appTitle (eg "Master - Time" minus "Master - " plus " - ")
    } else if(appName.substring(0,appTitle.length() - 9) != appTitle.substring(9,appTitle.length())){
        returnValue = appTitle.substring(9,appTitle.length()) + ' - ' + appName
    } else {
        returnValue = appName
    }

    return returnValue
}

// Lock or unlock a group of locks
def multiLock(action, multiDevice, appId = app.id){
    multiDevice.each{
        singleLock(action,it,appId)
    }
}

// Lock or unlock a single lock
def singleLock(action, singleDevice, appId = app.id){
    if(action == 'lock'){
        singleDevice.lock()
    } else if(action == 'unlock'){
        singleDevice.unlock()
    } else {
        putLog(330,'error','Invalid value for action "' + action + '" sent to singleLock function',appId)
    }
    putLog(332,'info',action + 'ed ' + singleDevice,appId)
}


// Returns true if level value is either valid or null
def validateLevel(value, appId = app.id){
    if(!value) return true
    value = value as int
        if(value < 1) return false
        if(value > 100) return false
    return true
}

// Returns true if temp value is either valid or null
def validateTemp(value, appId = app.id){
    if(!value) return true
        value = value as int
            if(value < 1800) return false
            if(value > 6500) return false
            return true
}


// Returns true if temp value is either valid or null
def validateHue(value, appId = app.id){
    if(!value) return true
    value = value as int
    if(value < 1) return false

    if(value > 360) return false
    return true
}

// Returns true if temp value is either valid or null
def validateSat(value, appId = app.id){
    if(!value) return true
    value = value as int
        if(value < 1) return false
        if(value > 100) return false
        return true
}

def validateMultiplier(value, appId = app.id){
    if(value){
        if(value < 1 || value > 100){
            putLog(377,'error',"ERROR: Multiplier $value is not valid",appId)
            return
        }
    }
    return value
}













































def checkIsDimmable(singleDevice, appId = app.id){
    if(singleDevice) return singleDevice.hasCapability('SwitchLevel')
}

def checkIsTemp(singleDevice, appId = app.id){
    if(singleDevice) return singleDevice.hasCapability('ColorTemperature')
}

def checkIsColor(singleDevice, appId = app.id){
    if(singleDevice) return singleDevice.hasCapability('ColorMode')
}
// Used by child apps to determine if UI shows dim/brighten, temp or color options
def checkIsDimmableMulti(multiDevice, appId = app.id){
    if(!multiDevice) return
    for(int i = 0; i < multiDevice.size(); i++){
        if(multiDevice[i].hasCapability('SwitchLevel')) return true
    }
}
def checkIsTempMulti(multiDevice, appId = app.id){
    if(!multiDevice) return
    for(int i = 0; i < multiDevice.size(); i++){
        if(multiDevice[i].hasCapability('ColorTemperature')) return true
    }
}
def checkIsColorMulti(multiDevice, appId = app.id){
    if(!multiDevice) return
    for(int i = 0; i < multiDevice.size(); i++){
        if(multiDevice[i].hasCapability('ColorMode')) return true
    }
}

def checkIsFan(singleDevice, appId = app.id){
    if(singleDevice) return singleDevice.hasCapability('FanControl')
}
// Test state of a single switch
def checkIsOn(singleDevice, appId = app.id){
    // If no deviceState, set it
    stateValue = atomicState.'state'?."${singleDevice.id}"
    if(stateValue == 'on') return true
    if(stateValue == 'off') return false 
    if(singleDevice.currentValue('switch') == 'on') return true
}

// Used by Sensor
// Test state of a group of switches
// Return true if any are on
def checkAnyOnMulti(multiDevice, appId = app.id){
    if(!multiDevice) return
    returnStatus = false
    multiDevice.each{singleDevice->
        if(checkIsOn(singleDevice,appId)) returnStatus = true
    }
    return returnStatus
}

def checkPeopleHome(multiDevice, appId = app.id){
    if(!multiDevice) return true
    peopleHome = true
    multiDevice.each{it ->
        if(it.currentPresence != 'present') peopleHome = false
    }
    return peopleHome
}

def checkNoPeopleHome(multiDevice, appId = app.id){
    if(!multiDevice) return true
    nooneHome = true
    multiDevice.each{it ->
        if(it.currentPresence == 'present') nooneHome = false
    }
    return nooneHome
}
// hiRezHue is currently boolean global option in master
// It should allow setting it for specific devices
// Possibly could automate:
//  Loop through all devices used in any schedule, store device.currentHue
//  Store current state (on/off) of device
//  Set device currentHue to 360
//  Check if devic.currentHue reports 100
//  Set device hiRezHue
//  Set device currentHue to stored value
//  Set device state to stored value
def settingHiRezHue(appId = app.id){
    return settings['hiRezHue']
}

// Minimum incremental cycle for an "active" schedule (people home, right mode, device is on)
// Must be greater than CONSTDeviceActionDelayMillis * 4, or else app won't complete fast enough
def CONSTScheduleMinimumActiveFrequencyMilli(){
    return 8000
}

// Minimum incremental cycle for an "active" schedule (people not home, wrong mode, device is off)
// Should be longer than CONSTScheduleMinimumActiveFrequencyMilli
// Must be greater than CONSTDeviceActionDelayMillis * 4, or else app won't complete fast enough
def CONSTScheduleMinimumInactiveFrequencyMilli(){
    return 30000
}

def CONSTScheduleMaximumIncrements(){
    return 200
}

def CONSTDayInMilli(){
    return 86400000
}

// Used for notifications, for some reason
def CONSTHourInMilli(){
    return 3600000
}

def CONSTMinuteInMilli(){
    return 60000
}

def CONSTDeviceActionDelayMillis(){
    return 200
}

def CONSTProgressiveDimmingDelayTimeMillis(){
    return 750
}

def CONSTPushDimmingTotalSteps(){
    return 10
}

def CONSTHoldDimmingTotalSteps(){
    return 15
}
                                                                                                       
def CONSTDeviceDefaultBrightness(){
    return 100
}

def CONSTDeviceDefaultTemp(){
    return 3250
}

def getTimeOfDayInMillis(time,appId = app.id){
    if(!time) return
    timeAtMidnight = new Date(time).clearTime().getTime()
    return time - timeAtMidnight
}

def getDatetimeFromTimeInMillis(time,appId = app.id){
    if(!time) return
    timeAtMidnight = new Date(now()).clearTime().getTime()
    return time + timeAtMidnight
}

// Returns app name with app title prepended
def appendChildAppTitle(appName,appTitle){
    //Compare length of name (eg "test") to appTitle length minus 6 (eg "Master - Time - " minus "Master - "; "Time - " is min length)
    if(appName.length() < appTitle.length() - 6) return appTitle.substring(9,appTitle.length()) + ' - ' + appName
    //Compare first part of name (eg "Testing") to middle part of appTitle (eg "Master - Time" minus "Master - " plus " - ")
    if(appName.substring(0,appTitle.length() - 9) != appTitle.substring(9,appTitle.length())) return appTitle.substring(9,appTitle.length()) + ' - ' + appName
    return appName
}

def getDeviceList(){
    return allDevices
}



// ***************************************************************************** 
// * Date functions                                                            *
// *****************************************************************************



def processDates(includeDates, excludeDates, days, processErrors = false, appId){
// Need to figure out how to have error message
    currentYear = new Date(now()).format('yyyy').toInteger()
    daysInYear = new Date(currentYear, 11, 31).format('D').toInteger()
    includeList = []
    excludeList = []

    if(!includeDates){
        for (int day = 1; day <= daysInYear; day++) {
            if(days){
                def date = new Date(currentYear, 0, day)
                def weekday = date.format('EEEE')
                if (days.contains(weekday)) includeList.add(day)
            }
            if(!days) includeList.add(day)
        }
       //if(includeList.size() == new Date(currentYear, 11, 31).format('D').toInteger()) return    // If all days, return
        //return includeList
    }
    includeDates = cleanDateString(includeDates)
    excludeDates = cleanDateString(excludeDates)
    
    if(includeDates){
        includeDateStrings = includeDates.split(",|;")
        includeDateStrings.each { dateString ->
            if(!dateString.contains('-')) {
                dateValue = processDateStringIndividual(dateString,appId, processErrors)
                if(!dateValue) return
                if (dateValue.format('yyyy') == String.valueOf(currentYear)) includeList.add(dateValue.format('D').toInteger())
            }
            if(dateString.contains('-')) includeList += processDateStringRange(dateString, appId, processErrors)
        }
    }
    if(excludeDates){
        excludeDateStrings = excludeDates.split(",|;")
        excludeDateStrings.each { dateString ->
            if(!dateString.contains('-')) {
                dateValue = processDateStringIndividual(dateString,appId, processErrors)
                if(dateValue.format('yyyy') == String.valueOf(currentYear)) excludeList.add(dateValue.format('D').toInteger())
            }
            if(dateString.contains('-')) excludeList += processDateStringRange(dateString, appId, processErrors)
        }
    }
    finalList = includeList.findAll { !excludeList.contains(it) }
    finalList = cleanCompleteDateList(finalList)
    if(finalList.size() == daysInYear) return    // If all days, return
    
    return finalList
}

def cleanDateString(dateString,appId = app.id){
    if(!dateString) return
    dateString = dateString.replaceAll("\\\\", "/")
    dateString = dateString.replaceAll("\\s","")
    return dateString
}

def processDateStringIndividual(dateString, processErrors,appId = app.id){
    if(dateString.contains('-')) return
    
    currentYear = new Date(now()).format('yyyy').toInteger()
    // Check for ddd format
    if (dateString.matches('\\d{1,3}')) {
        returnDate = Date.parse('D yyyy', dateString + ' ' + currentYear)
        if(dateString.toInteger() == returnDate.format('D').toInteger()) return returnDate
        if(processErrors) atomicState.'dateProcessingErrors' += 'WARNING: Invalid date dateString = ' + dateString + ' resolves to ' + dateValue + '<br>'
        return
    }
    if(!dateString.contains('/') && !dateString.contains('.')){
        if(processErrors) atomicState.'dateProcessingErrors' += 'WARNING: Invalid2 date "' + dateString + '".<br>'
        return
    }
    // Convert m/d, d.m/yy, etc to mm/dd/yyyy
    dateValueParts = dateString.split('/').toList()
    if(dateValueParts.size() > 3) {
        if(processErrors) atomicState.'dateProcessingErrors' += 'WARNING: Invalid3 date "' + dateString + '".<br>'
        return
    }
    if(dateValueParts.size() < 2) {
        if(processErrors) atomicState.'dateProcessingErrors' += 'WARNING: Invalid4 date "' + dateString + '".<br>'
        return
    }
    dateValueParts[0] = ('0' + dateValueParts[0])[-2..-1]
    dateValueParts[1] = ('0' + dateValueParts[1])[-2..-1]
    if (dateValueParts.size() > 2) dateValueParts[2] = dateValueParts[2][-4..-1]
    if(dateValueParts.size() < 3){
        [dateValueParts].flatten().findAll { it != null }       // Convert array to list
        dateValueParts.add(2)           // Add another element (do we need this with a list?)
        dateValueParts[2] = currentYear
    }
    if(dateString.contains('/')) returnDate = dateValueParts[0] + '/' + dateValueParts[1] + '/' + dateValueParts[2]
    if(dateString.contains('.')) returnDate = dateValueParts[1] + '/' + dateValueParts[0] + '/' + dateValueParts[2]

    if(returnDate == Date.parse('MM/dd/yyyy', returnDate).format('MM/dd/yyyy')) return Date.parse('MM/dd/yyyy', returnDate)
    if(processErrors) atomicState.'dateProcessingErrors' += 'WARNING: Invalid5 date "' + dateString + '".<br>'
    return returnDate
}

def processDateStringRange(dateString, processErrors,appId = app.id){
    if(!dateString.contains('-')) return
    
    dates = dateString.split('-')
    if(dates.size() != 2) {
        if(processErrors) atomicState.'dateProcessingErrors' += 'WARNING: Invalid1 date "' + dateString + '".<br>'
        return
    }

    listOfDays = []
    currentYear = new Date(now()).format('yyyy').toInteger()
    firstDate = processDateStringIndividual(dates[0], appId, processErrors)
    secondDate = processDateStringIndividual(dates[1], appId, processErrors)
    if(dates[0].length() < 6 && dates[1].length() < 6) {
        if(firstDate > secondDate) {
            numberDaysInYear = new Date(currentYear, 11, 31).format('D').toInteger()
            start = firstDate.format('D').toInteger()
            end = numberDaysInYear
            while (start <= end) {
                listOfDays.add(start)
                start++
            }
            start = 1
            end = secondDate.format('D').toInteger()
            while (start <= end) {
                listOfDays.add(start)
                start++
            }
            return listOfDays
        }
    }

    if(dates[0].length() > 5 && dates[1].length() < 6) {
        firstYear = processDateStringIndividual(dates[0], appId, processErrors).format('yyyy').toInteger()
        secondYear = processDateStringIndividual(dates[1], appId, processErrors).format('yyyy').toInteger()
        yearDifference = secondYear - firstYear
        firstDate = Date.parse('MM/dd/yyyy', firstDate.format('MM/dd/yyyy').replaceAll('\\d{4}$', (firstDate.format('yyyy').toInteger() + yearDifference).toString()))
    }
    if(dates[0].length() < 6 && dates[1].length() > 5) {
        firstYear = processDateStringIndividual(dates[0], appId, processErrors).format('yyyy').toInteger()
        secondYear = processDateStringIndividual(dates[1], appId, processErrors).format('yyyy').toInteger()
        yearDifference = firstYear - secondYear
        secondDate = Date.parse('MM/dd/yyyy', secondDate.format('MM/dd/yyyy').replaceAll('\\d{4}$', (secondDate.format('yyyy').toInteger() + yearDifference).toString()))
    }
    if(firstDate > secondDate){
        if(processErrors) state.dateProcessingErrors += 'Error: firstDate is greater than secondDate with "' + dateString + '".</br>'
        return
    }
    // here, change years if dates[0].length() < 6 && dates[1].length() < 6 and firstDate > secondDate
    while (firstDate <= secondDate) {
        if(firstDate.format('yyyy').toInteger() == currentYear) listOfDays.add(firstDate.format('D').toInteger())
        firstDate = firstDate.plus(1)
    }
    
    return listOfDays
}

def getDateProcessingErrors(appId = app.id){
    returnValue = atomicState.'dateProcessingErrors'     // Need to add appId to the atomicState
    atomicState.remove('dateProcessingErrors')
    return returnValue
}

def checkDateInDayList(dateValue,appId = app.id){
    if(!settings['days']) return true
    
    dateFormat = new java.text.SimpleDateFormat('EEEE')
    dateFormat.setTimeZone(location.timeZone)
    testDay = dateFormat.format(Date.parse('D', dateValue.toString()))
    if(settings['days'].contains(testDay)) return true
}

def cleanCompleteDateList(dateList, appId = app.id){
    if(!dateList) return
    filteredDateList = []
    dateList.each { value ->
        if(value){
            if (checkDateInDayList(value)) filteredDateList.add(value)
        }
    }
    if(!filteredDateList) return
    dateList = filteredDateList.unique().sort()
    return dateList
}

// Returns true if today is in $days map
// Returns true for null values
def checkNowInDayList(days,appId = app.id){
    if(!days) return true
    
    dateFormat = new java.text.SimpleDateFormat('EEEE')
    dateFormat.setTimeZone(location.timeZone)
    dayToday = dateFormat.format(new Date())
    if(days.contains(dayToday)) return true
}

// Returns true if now is between two times
// Returns true for null values
def checkNowBetweenTimes(startTime, stopTime,appId = app.id){
    if(!startTime) return true
    if(!stopTime) return true

    currentTime = getTimeOfDayInMillis(now(),appId)

    if(startTime == stopTime) return true

    if(startTime < currentTime && stopTime > currentTime) return true
    if(startTime < currentTime && stopTime < startTime) return true
    if(startTime > currentTime && stopTime > currentTime && startTime > stopTime) return true
}

def getPrintDateTimeFormat(datetime, appId = app.id){
    return new Date(datetime).format('h:mma MMM dd, yyyy', location.timeZone)
}

def getSunriseSunset(type, appId = app.id){
    if(!type) return
    if(type == 'sunrise') return getSunrise('',appId)
    if(type == 'sunset') return getSunset('',appId)
}

// Returns date/time of sunrise in format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
// negative is true of false
def getSunrise(offset = false, appId = app.id){
    if(offset) return getSunriseAndSunset().sunrise.getTime() + (offset * CONSTMinuteInMilli())
    if(!offset) return getSunriseAndSunset().sunrise.getTime()
}

// Returns date/time of sunset in format of yyyy-MM-dd'T'HH:mm:ss.SSSZZZZ
def getSunset(offset = false,appId = app.id){
    if(offset) return getSunriseAndSunset().sunset.getTime() + (offset * CONSTMinuteInMilli())
    if(!offset) return getSunriseAndSunset().sunset.getTime()
}

def checkToday(time,appId = app.id){
    if(time + CONSTDayInMilli() > now()) return true
}



// ***************************************************************************** 
// * Perform actions                                                           *
// *****************************************************************************




// Should be added to Table
// Lock or unlock a group of locks
def setLockMulti(multiDevice, action, appId = app.id){
    if(!multiDevice) return
    multiDevice.each{singleDevice->
        setLockSingle(singleDevice,action,appId)
    }
}

// Lock or unlock a single lock
def _setLockSingle(singleDevice, action, appId = app.id){
    if(action == 'lock') singleDevice.lock()
    if(action == 'unlock') singleDevice.unlock()
    putLog(859,'info','[' + singleDevice + '] ' + action + 'ed ',appId)
}

// setDeviceMulti passes to both setDeviceLevelMulti and setDeviceStateMulti
// setDeviceLevelMulti hands off to setDeviceLevelSingle
// setDeviceLevelMulti could be put inside setDeviceMulti
// but otherwise, setDeviceLevelMulti is needed separate from setDeviceLevelSingle for pausing per setting
def setDeviceMulti(multiDevice, appType = 'manual', appId = app.id){
    if(!multiDevice) return
    
    setDeviceLevelMulti('brightness',multiDevice,appId)
    setDeviceLevelMulti('sat',multiDevice,appId)
    setDeviceLevelMulti('hue',multiDevice,appId)
    setDeviceLevelMulti('temp',multiDevice,appId)
    setDeviceStateMulti(multiDevice, appType,appId)
}

def setDeviceSingle(singleDevice,appId = app.id){
    if(!singleDevice) return

    newLevel = getDeviceLevelSingleChange('brightness',singleDevice,appId)
    if(newLevel){
        pauseActions()
        setDeviceLevelSingle('brightness',singleDevice,newLevel,appId)
    }
    newLevel = getDeviceLevelSingleChange('sat',singleDevice,appId)
    if(newLevel){
        pauseActions()
        setDeviceLevelSingle('sat',singleDevice,appId)
    }
    newLevel = getDeviceLevelSingleChange('hue',singleDevice,appId)
    if(newLevel){
        pauseActions()
        setDeviceLevelSingle('hue',singleDevice,appId)
    }
    newLevel = getDeviceLevelSingleChange('temp',singleDevice,appId)
    if(newLevel){
        pauseActions()
        setDeviceLevelSingle('temp',singleDevice,newLevel,appId)
    }
    if(getDeviceStateChangeSingle(singleDevice, appId)){
        pauseActions()
        setDeviceStateSingle(singleDevice, appType,appId)
    }
}

def setDeviceLevelMulti(type, multiDevice, appId = app.id){
    multiDevice.each{singleDevice->
        newLevel = getDeviceLevelSingleChange(type,singleDevice,appId)
        if(newLevel){
            pauseActions()
            setDeviceLevelSingle(type,singleDevice,newLevel,appId)
        }
    }
}

// If any, return value of new level
// Used to determine if a change is occuring for pausing execution
def getDeviceLevelSingleChange(type, singleDevice,appId = app.id){
    if(!singleDevice) return
    
    stateValue = atomicState.'state'?."${singleDevice.id}"
    if(!stateValue) return       // Should set state to current device state?
    if(stateValue == 'off') return
    
    if(type == 'brightness' && !checkIsDimmable(singleDevice,appId)) return
    if(type == 'temp' && !checkIsTemp(singleDevice,appId)) return
    if(type == 'hue' && !checkIsColor(singleDevice,appId)) return
    if(type == 'sat' && !checkIsColor(singleDevice,appId)) return

    if(type == 'temp'){
        if(atomicState.'nonSchedule'?."${singleDevice.id}"?.'hue') return
        if(atomicState.'nonSchedule'?."${singleDevice.id}"?.'sat') return
        if(atomicState.'schedule'?."${singleDevice.id}"?.'hue') return
        if(atomicState.'schedule'?."${singleDevice.id}"?.'sat') return
    }
    
    // Need to somehow get the default into nonSchedule? Otherwise, it gets set on capture
    newLevel = getCurrentLevelFromTableOrDefault(type, singleDevice, appId)
    if(!newLevel) return
    
    if(singleDevice.currentValue('switch') == 'on'){
        if(!checkLevelDifferent(type, newLevel,singleDevice,appId)) return
    }
    return newLevel
}

def setDeviceLevelSingle(type, singleDevice, newLevel,appId = app.id){
    if(type == 'brightness'){
        checkFan = checkIsFan(singleDevice,appId)
        if(checkFan) singleDevice.setSpeed(newLevel)
        if(!checkFan) singleDevice.setLevel(newLevel)
    }
    if(type == 'temp') singleDevice.setColorTemperature(newLevel)
    if(type == 'hue') singleDevice.setHue(newLevel)
    if(type == 'sat') singleDevice.setSaturation(newLevel)
    
    putLog(956,'info','[' + singleDevice + '] ' + type + ' ' + newLevel, appId)
    return true
}

def setDeviceStateMulti(multiDevice, appType = 'manual', appId = app.id){
    multiDevice.each{singleDevice->
        if(getDeviceStateChangeSingle(singleDevice,appId)){
            pauseActions()
            setDeviceStateSingle(singleDevice,appType,appId)
        }
    }
}

def getDeviceStateChangeSingle(singleDevice, appId = app.id){
    stateValue = atomicState.'state'?."${singleDevice.id}"
    if(!stateValue) return
    if(singleDevice.currentValue('switch') == stateValue) return
    return true
}

def setDeviceStateSingle(singleDevice, appType = 'manual',appId = app.id){
    newState = atomicState.'state'."${singleDevice.id}"
    if(newState == 'on') singleDevice.on()
    if(newState == 'off') {
        singleDevice.off()
        // clear state settings
        clearTableByDeviceAndAppId('nonSchedule', singleDevice.id,appId)
       // clearNonScheduleDeviceSetting(singleDevice.id,appId)
    }
    
    setDeviceLastChangedApp(singleDevice.id, appType, appId)
    putLog(987,'info','[' + singleDevice + '] ' + newState,appId)
    return true
}

def changeMode(mode, appId = app.id){
    if(location.mode == mode) return
    message = 'Changed Mode from ' + oldMode + ' to ' + mode + '.'
    pauseActions()
    setLocationMode(mode)
    putLog(996,'debug',message + mode,appId)
}

// Send SMS text message to $phone with $message
//SMS IS NO LONGER SUPPORTED
def sendPushNotification(phone, message, appId = app.id){
    def now = new Date().getTime()
    seconds = (now - atomicState.contactLastNotification) / 1000
    if(seconds < 361) {
        putLog(1005,'info','Did not send push notice for ' + evt.displayName + ' ' + evt.value + 'due to notification sent ' + seconds + ' ago.',appId)
        return
    }
    pauseActions()
    atomicState.contactLastNotification = now
    speechDevice.find{it ->
        if(it.id == deviceId) {
            if(it.deviceNotification(message)) putLog(1012,'debug','Sent phone message to ' + phone + ' "' + message + '"',appId)
        }
    }
}

def sendVoiceNotification(deviceId,message, appId = app.id){
    if(!deviceId)  return
    pauseActions()
    speechDevice.find{it ->
        if(it.id == deviceId) {
            if(it.speak(text)) putLog(1022,'debug','Played voice message on ' + deviceId + ' "' + message + '"',appId)
        }
    }
}

def getCurrentLevelFromTableOrDefault(type, singleDevice, appId = app.id){
    levelValue = getCurrentLevelFromTable(type, singleDevice.id, appId)
    if(type == 'brightness' && checkIsFan(singleDevice,appId)) return        // Fans do not use default levels
    if(!levelValue) levelValue = getDeviceLevelDefault(type,appId)
    if(type == 'hue'){
        if(levelValue){
            if(!settings['hiRezHue']) levelValue = Math.round(levelValue / 3.6) as Integer
        }
    }
    if(type == 'sat'){
        if(!levelValue){
            if(getCurrentLevelFromTable('hue',singleDevice.id,appId)){
                levelValue = 100
            }
        }
    }
    return levelValue
}
def getCurrentLevelFromTable(type, singleDeviceId, appId = app.id){
    nonScheduleValue = atomicState?.'nonSchedule'?."${singleDeviceId}"?."${type}"
    if(nonScheduleValue) return nonScheduleValue
    scheduleValue = atomicState?.'schedule'?."${singleDeviceId}"?."${type}"?.'currentLevel'
    if(scheduleValue) return scheduleValue
}

def getDeviceLevelDefault(type, appId = app.id){
    if(type == 'brightness') return CONSTDeviceDefaultBrightness()
    if(type == 'temp') return CONSTDeviceDefaultTemp()
    if(type == 'hue') return
    if(type == 'sat') return
}

def setDeviceLastChangedApp(singleDeviceId, appType, appId = app.id){
    tempMap = atomicState?.'stateChangeApp'
    if(!tempMap) tempMap = [:]
    tempMap."${singleDeviceId}" = appType
    atomicState.'stateChangeApp' = tempMap
}

def getDeviceLastChangedApp(singleDeviceId, appId = app.id){
    changedAppId = atomicState?.'stateChangeApp'?."${singleDeviceId}"
    if(changedAppId) return changedAppId
}

def getCurrentLevelFromDevice(type, singleDevice, appId = app.id){
    if(type == 'brightness') {
        if(checkIsFan(singleDevice)) return singleDevice.currentSpeed
        return singleDevice.currentLevel as Integer
    }
    if(type == 'temp' && singleDevice.currentColorMode != 'RGB') return singleDevice.currentColorTemperature as Integer
    if(type == 'hue' && singleDevice.currentColorMode == 'RGB') return convertHueValue(type,singleDevice.currentHue,appId) as Integer
    if(type == 'sat' && singleDevice.currentColorMode == 'RGB') return singleDevice.currentSaturation as Integer
}

def convertLevelValue(type,levelValue,appId = app.id){
    if(!levelValue) return
    return convertHueValue(type,levelValue,appId)
}

def convertHueValue(type, hueValue, appId = app.id){
    if(type != 'hue') return hueValue
    if(!hueValue) return
    if(settings['hiRezHue']) return hueValue
    if(!settings['hiRezHue']) return Math.round(hueValue * 3.6) as Integer
}

// If newLevel is different from oldLevel, return true
// If newLevel is null, return false
def checkLevelDifferent(type, newLevel, singleDevice, appId = app.id){
    if(!newLevel) return
    oldLevel = getCurrentLevelFromDevice(type, singleDevice, appId)     // This should only happen with temp in color mode, or color in CT mode
    if(!oldLevel) return true
    if(type == 'temp'){
        if(Math.abs(newLevel - oldLevel) < 25) return false
    }
    if((type == 'hue' || type == 'sat') && singleDevice.currentColorMode != 'RGB') return true
    if(type == 'temp' && singleDevice.currentColorMode == 'RGB') return true
    if(newLevel != oldLevel) return true
}

def checkTempWithinVariance(originalTemp, newTemp, colorMode, appId = app.id){
    if(!originalTemp) return
    if(!newTemp) return
    if(colorMode == 'RGB') return
    if(originalTemp == newTemp) return true
    if(roundTemp(originalTemp) == roundTemp(newTemp)) return true

}

// Do reverse, and wrap around
// Shouldn't be needed?
def _computeIncrementalHueRange(singleDevice,appId = app.id){
    if(atomicState.'schedule'."${singleDevice.id}".'hue'?.'hueDirection' == 'reverse') return 360 - Math.abs(atomicState.'schedule'."${singleDevice.id}".'hue'.'stopLevel' + atomicState.'schedule'."${singleDevice.id}".'hue'.'startLevel')
    if(atomicState.'schedule'."${singleDevice.id}".'hue'?.'hueDirection' != 'reverse') return Math.abs(atomicState.'schedule'."${singleDevice.id}".'hue'.'stopLevel' - atomicState.'schedule'."${singleDevice.id}".'hue'.'startLevel')
}

def resumeDeviceScheduleMulti(multiDevice,appId = app.id){
    if(!multiDevice) return
    multiDevice.each{singleDevice->
        clearNonScheduleDeviceSetting(singleDeviceId,appId)
    }
}

def clearNonScheduleDeviceSetting(singleDeviceId,appId = app.id){
    if(!singleDeviceId) return
    if(!atomicState.'nonSchedule'?."${singleDeviceId}") return

    newVar = atomicState.'nonSchedule'
    toRemove = []
    newVar?."${singleDeviceId}".each{key, value -> 
        toRemove.add(key)
        log.error appId + ' deleting with clearTableByDeviceAndTypeAndAppId ' + singleDeviceId
    }
    toRemove.each{newVar."${singleDeviceId}".remove(it)}
    atomicState.'nonSchedule' = newVar
}

def getStateMapSingle(singleDevice,action,appId = app.id){
    if(!action) return
    if(!singleDevice) return
    if(action != 'on' && action != 'off' && action != 'toggle') return
    
    if(action == 'toggle'){
        action = 'on'
        stateValue = atomicState?.'state'?."${singleDevice.id}"
        if(!stateValue) {
            if(singleDevice.currentValue('switch') == 'on') action = 'off'
        }
        if(stateValue == 'on') action = 'off'
    }
    return action    // appId used by sensor
}

def getNonScheduleLevelMap(type,level,appId = app.id){
    if(!type) return
    if(!level) return
    if(!appId) return
    if(type != 'brightness' && type != 'temp' && type != 'hue' && type != 'sat') return
      
    return [(type):level]
}
def getScheduleLevelMap(type,level,appId = app.id){
    if(!type) return
    if(!level) return
    if(!appId) return
    if(type != 'brightness' && type != 'temp' && type != 'hue' && type != 'sat') return
      
    return [(type):['currentLevel':level,'appId':appId]]
}

def updateTableCapturedState(singleDevice,action,appId = app.id){
    stateValue = atomicState?.'state'?."${singleDevice.id}"
    if(stateValue == action) return
    if(action != 'on' && action != 'off') return
    putLog(1181, 'trace', '[' + singleDevice + '] captured state ' + action + ' (table was ' + stateValue + '; actually was ' + singleDevice.currentState + ')',appId)
    stateMap = getStateMapSingle(singleDevice,action,app.id)
    mergeMapToTable('state',singleDevice.id,stateMap,appId)
    if(action == 'on') setDeviceSingle(singleDevice,appId)    // With device on, set levels
}


def combineMaps(map1, map2, map3 = '', map4 = '', map5 = '',appId = app.id){
    returnMap = [:]
    if(map1) returnMap += map1
    if(map2) returnMap += map2
    if(map3) returnMap += map3
    if(map4) returnMap += map4
    if(map5) returnMap += map5
    return returnMap
}

//THIS CAN BE REWRITTEN TO OVERWRITE WITH SUBKEYS FROM NEW MAP

// newMap in format of [[type:'brightness/temp/sat/hus',time:datetime as long,stopTime:datetime as long,addId:nnnn,appType:string],ETC]
def mergeMapToTable(tableName, singleDeviceId, newMap, appId = app.id){
    if(!singleDeviceId) return
    if(!newMap) return
    if(tableName != 'state' && tableName != 'nonSchedule' && tableName != 'schedule') {
        putLog(1205,'error','Invalid table name ' + tableName + ' sent to mergeMapToTable (ignore if new install)',appId)
        return
    }
    tempMap = atomicState?."${tableName}"
    if(!tempMap) tempMap = [:]

    if(tableName == 'state') {
        tempMap[(singleDeviceId)] = newMap
        atomicState."${tableName}" =  tempMap
        return true
    }
    if(!tempMap?."${singleDeviceId}") tempMap[(singleDeviceId)] = [:]
    newMap.each{newKey,newValue->
        tempMap."${singleDeviceId}"."${newKey}" = newValue
        if(tableName == 'schedule') clearTableByDeviceAndType('nonSchedule',singleDeviceId,newKey,appId)    //On schedule start, clear non-schedule
    }
    atomicState."${tableName}" = tempMap
}

def clearTableByDeviceAndTypeAndAppId(tableName,singleDeviceId, type, appId = app.id){
    if(!singleDeviceId) return
    if(!atomicState?."${tableName}"?."${singleDeviceId}"?."${type}") return

    newVar = atomicState."${tableName}"
    if(!newVar) newVar = [:]
    toRemove = []
    newVar?."${singleDeviceId}".each{key, value ->
        if(tableName == 'nonSchedule') toRemove.add(key)
        if(tableName == 'schedule') {
            if(value?.'appId' == appId) {
                log.error appId + ' deleting with clearTableByDeviceAndTypeAndAppId ' + singleDeviceId
                toRemove.add(key)
            }
        }
    }

    toRemove.each{newVar."${singleDeviceId}".remove(it)}
    atomicState."${tableName}" = newVar.findAll{ it.value!=[:] }
}

def clearTableByDeviceAndAppId(tableName, singleDeviceId, appId = app.id){
    if(!singleDeviceId) return
    if(!atomicState?."${tableName}"?."${singleDeviceId}") return
    // if schedule, need to loop through and delete only itself (appId)
    newVar = atomicState?."${tableName}"
    if(!newVar) newVar = [:]
    if(tableName == 'schedule') {
        toRemove = []
        newVar?."${singleDeviceId}".each{key, value -> 
            if(value?.'appId'){
                if(value?.'appId' == appId) {
                    toRemove.add(key)
                }
            }
            
        }
        toRemove.each{newVar."${singleDeviceId}".remove(it)}
        atomicState.'schedule' = newVar.findAll{ it.value!=[:] }
        return true
    }

    newVar.remove(singleDeviceId)
    atomicState."${tableName}" = newVar.findAll{ it.value!=[:] }
    return true
}

def clearTableByDeviceAndType(tableName, singleDeviceId, type, appId = app.id){
    if(!singleDeviceId) return
    if(!atomicState?."${tableName}"?."${singleDeviceId}"?."${type}") return
    newVar = atomicState?."${tableName}"
    if(!newVar) newVar = [:]
    newVar."${singleDeviceId}".remove(type)
    atomicState."${tableName}" = newVar.findAll{ it.value!=[:] }
    return true
}

def clearTableByAppId(tableName, appId = app.id){
    newVar = atomicState?."${tableName}"
    if(!newVar) newVar = [:]
    newVar?.each{key,value ->      // Devices
        toRemove = []
        newVar?."${key}".each{key2, value2 -> // Levels
            if(value2?.'appId'){
                if(value2?.'appId' == appId) {
                    toRemove.add(key2)
                }
            }
        }
        toRemove.each{newVar."${key}".remove(it)}
    }
    atomicState."${tableName}" = newVar.findAll{ it.value!=[:] }
    return true
}

def updateTableCapturedLevel(singleDeviceId,type,newLevel,appId = app.id){
    if(!newLevel) return
    tableLevel = atomicState?.'nonSchedule'?."${singleDeviceId}"?."${type}"
    if(!tableLevel) tableLevel = atomicState?.'schedule'?."${singleDeviceId}"?."${type}"?.'currentLevel'
    if(newLevel == tableLevel) return

    if(!tableLevel){      // Exclude defaults (or maybe capture them as nonSchedule? Or have a separate table for them?)
        if(type == 'brightness') {
            if(newLevel == CONSTDeviceDefaultBrightness()) return
        }
        if(type == 'temp') {
            log.error newLevel + ' - ' + CONSTDeviceDefaultTemp() + ' - ' + Math.abs(newLevel - CONSTDeviceDefaultTemp())
            if(Math.abs(newLevel - CONSTDeviceDefaultTemp()) < 25) return
        }
        if(type == 'sat'){
            if(newLevel == 100) {
                if(atomicState?.'nonSchedule'?."${singleDeviceId}"?.'hue' || atomicState?.'schedule'?."${singleDeviceId}"?.'hue'?.'currentLevel') return
            }
        }
    }

    putLog(1320,'trace','[Device Id: ' + singleDeviceId + '] captured ' + type + ' to ' + newLevel + ' (table was ' + tableLevel + ')',appId)
    levelMap = getNonScheduleLevelMap(type,newLevel,appId)
    mergeMapToTable('nonSchedule',singleDeviceId,levelMap,appId)
}

def _getNextLevelDimmable(singleDevice, action, dimFactor = 1.61,appId = app.id){
    if(!dimFactor) dimFactor = 1.61
    if(checkIsFan(singleDevice,childLabel)) return _getNextLevelFan(singleDevice,action,childLabel)
    if(!checkIsDimmable(singleDevice,childLabel)) return
    if(action != 'dim' && action != 'brighten') return
    oldLevel = getCurrentLevelFromTable('brightness', singleDevice.id, appId)
    if(!oldLevel && checkIsOn(singleDevice,appId)) oldLevel = getCurrentLevelFromDevice('brightness',singleDevice,appId)
    if(!oldLevel){
        if(action == 'brighten') return 1
        if(action == 'dim') return 100
    }
    if (action == 'dim'){
        newLevel = Math.round(convertToInteger(oldLevel) / dimFactor)
        if (newLevel == level) newLevel -= 1
        if(newLevel < 1) newLevel = 1
    }
    if (action == 'brighten'){
        newLevel = Math.round(convertToInteger(oldLevel) * dimFactor)
        if (newLevel == level) newLevel++
        if(newLevel > 100) newLevel = 100
    }

    return newLevel
}

def _getNextLevelFan(singleDevice, action, appId = app.id){
    if(!checkIsFan(singleDevice,appId)) return
    if(action != 'dim' && action != 'brighten') return
    brightnessValue = atomicState.'schedule'."${singleDevice.id}"?.'brightness'?.'currentLevel'
    if(brightnessValue) level = brightnessValue
    if(!brightnessValue) level = getCurrentLevelFromDevice('brightness',singleDevice,appId)

    if(action == 'brighten'){
        if(level == 'low' || level == 'medium-low') return 'medium'
        if(level == 'medium' || level == 'medium-high') return 'high'
        if(!level || level == 'off') return 'low'
    }
    if(!level) return
    if(level == 'high') return 'medium'
    if(level == 'medium-high') return 'medium'
    if(level == 'medium') return 'low'
    if(level == 'medium-low') return 'low'
}

def computeOptiomalGeometricProgressionFactor(numberOfSteps,appId = app.id){   
    if(!numberOfSteps) return                                                                                         
    currentTotal = 1
    factor = 1.5
    factorTestSteps = computeGeometricProgressionSteps(factor)

    if(factorTestSteps < numberOfSteps){
        while (factorTestSteps < numberOfSteps){
            factor -= 0.1
            factorTestSteps = computeGeometricProgressionSteps(factor)
        }
        factorTestSteps = numberOfSteps
    }
    
    if(factorTestSteps > numberOfSteps){
        while (factorTestSteps > numberOfSteps){
            factor += 0.1
            factorTestSteps = computeGeometricProgressionSteps(factor)
        }
        factorTestSteps = numberOfSteps
    }
    
    if(factorTestSteps == numberOfSteps){
        factorTestSteps = 0
        while (factorTestSteps != numberOfSteps){
            factor += 0.01
            factorTestSteps = computeGeometricProgressionSteps(factor)
        }
    }
    return factor
}
                                                                                                       
// computes the number of steps to get from 1 to 100 by x * 1.y
def computeGeometricProgressionSteps(factor,appId = app.id){
    if(factor < 1) return
    if(factor > 2) return
    priorStep = 1
    count = 0
    while (priorStep < 100) {
        thisStep = Math.round(factor * priorStep)
        if(thisStep > priorStep) priorStep = thisStep
        if(thisStep == priorStep) priorStep++
        count++
    }
    return count
}

// Sets cron string based on:
//      timeMillis = time from now in milliseconds, or
//      timeValue = time in timestamp
// Checks child app getDisabled
// If active, passes cronString, functionName, and formated parameters back to child app, to set the schedule
def scheduleChildEvent(timeMillis = '',timeValue = '',functionName,parameters,appId = app.id){
    // noPerformDisableCheck is reversed, so that null does not equal False
    // true = not check, False = check
    if(!appId) return
    if(!timeMillis && !timeValue) return
    if(timeMillis < 0) {
        putLog(1427,'warn','scheduleChildEvent given negative timeMillis from appId ' + appId + ' (' + functionName + ' timeMillis = ' + timeMillis + ')',appId)
        return
    }
    if(timeValue) {
        def currentTimeMillis = now()
        def targetTimeMillis = new Date(timeValue).time
        timeMillis = targetTimeMillis - currentTimeMillis
        if(timeMillis < 0) timeMillis += CONSTDayInMilli()
    }
    if(parameters) parametersMap = ['data':parameters]

    childApps.find {Child->
        if(Child.id == appId) {
                if(!functionName) {
                    putLog(1441,'warn','scheduleChildEvent given null for functionName from appId ' + appId + ' (timeMillis = ' + timeMillis + ', timeValue = ' + TimeValue + ')',appId)
                    return
                }
                Child.setScheduleFromParent(timeMillis,functionName,parametersMap)
                if(parameters) parameters = ' (with parameters: ' + parameters + ')'
                putLog(1446,'debug','Scheduled ' + functionName + parameters + ' for ' + new Date(timeMillis + now()).format('hh:mma MM/dd ') + ' (in ' + Math.round(timeMillis / 1000) + ' seconds)',appId)
        }
    }
}

def convertToInteger(value, appId = app.id){
    if(value instanceof Integer) return value
    if(value instanceof Long) return value
    if(value instanceof String && value.isInteger()) return value.toInteger()
    return
}

// pauseActions is called AFTER a device change is made
// Maybe this should be per device, not per app/action - but tracking that would take too much computing time
def pauseActions(appId = app.id){
    lastAction = atomicState.'lastActionTime'
    if(!lastAction) lastAction = 0
    atomicState.'lastActionTime' = now()
    if((lastAction + CONSTDeviceActionDelayMillis()) < now()) return        // No delay required

    delayTime = CONSTDeviceActionDelayMillis() - (now() - lastAction)
    if(delayTime < 1) return        // Just in case the computing time to compute it makes it less than 0
    putLog(1468,'debug','Pausing execution: ' + delayTime + 'ms',appId)
    
    pauseExecution(delayTime)
}

def displayScheduleTip(){
    if(hideTips) return
    section(){
        scheduleText = 'Schedules allow performing actions on switches and lights (e.g. turn on, off, dim, set color, etc.) at specific times, \
and will run daily. Devices can be set to progressively change by setting both start and stop values for brightness or color. If a \
device is turned off, the schedule continues to run, so <i>when it is</i> turned on, it will do so at the correct level(s). You \
can also select specific weekdays and dates, and other factors like Mode. Schedules also support sunrise and sunset. Use cases:<br>\
        • Set a porch light to turn on at sunset and off at sunrise<br>\
        • Set room lights to turn on and get dimmer as it gets later in the evening<br>\
        • Set room lights to be dimmer as it gets later in the evening, when and if they are turned on<br>\
        • Set lights to turn off at night<br>\
        • Set a bedroom light to turn on in the morning, on weekdays (and progressively brighten for a couple minutes)<br>\
        • Set irrigation (or pool) controls for different times based on the season (by setting a schedule for each season, limited to those dates)<br>\
        • Turn off all devices when everyone leaves, within a certain time (or any time, by setting start time the same as stop time)<br>\
        • Turn on certain devices when someone arrives, within a certain time (or any time, by setting start time the same as stop time)<br>\
        • Set a color of a light based on who is home, based on the time (e.g. if a child is not home an hour after sunset, set a light to red)<br>\
        • Send a notification based on who is home (e.g. a child arriving/departing, during a certain time, or in general).'
        displayInfo(scheduleText)
    }
}

def displayPicoTip(){
    if(hideTips) return
    section(){
        picoText = 'The Pico app is designed for Lutron Caseta switches and Pico remotes. It may or may not work for other 2, 4 or 5 button devices. \
Actions include on, off, toggle, dim, brighten, and "resume schedule" (more on that below). It supports push, and hold (no double tap yet). \
It allows quickly setting up remotes to work (select a Pico, set a device, and save), or more advanced options of defining specific actions for each button, \
and/or defining different devices per button. It allows scheduling, such that a remote can do different things at different times, as well as presence. \
The "resume" option resets back to a schedule. For instance, if the brightness set by a schedule is overridden, selecting resume has the schedule take \
back over. Use cases:<br>\
        • Control a light or group of lights<br>\
        • Control any (smart) switched device (i. e. blinds, irrigation, fans, etc.)<br>\
        • With 5-button Picos, set center button to toggle the ceiling fan (and other buttons control the lights)<br>\
        • Using a 2 button Pico, control two bedside lamps by setting both as "toggle"<br>\
        • When turning on a bedroom lamp, turn off the living room (or porch) light<br>\
        • Set push to control one light, but hold to control lights in an adjacent room<br>\
        • Set push to dim, but hold to progressively dim<br>\
        • Set light switch to send a notification during the day as a honeypot for would be intruders (notifications aren\'t yet a feature, but soon)<br>\
        • Set switches for a pool pump or irrigation for pool cleaner/landscaper for their time and day, but repurpose the switch for outdoor lighting at other times<br>\
        • Schedule a porch light to be dim, but set the "on" button to set it to full brightness, and "off" button has it go back to dim per the schedule.'
        displayInfo(picoText)
    }
}

def displayMagicCubeTip(){
    if(hideTips) return
    section(){
        magicCubeText = 'MagicCubes are sold by Xiaomi under the Aqara brand. They are, aptly, cubes (and perhaps magic). Actions supported are flip 90, flip 180, shake, \
rotate left, and rotate right. (Support for side up number to be added.) Each action can be set to control different devices and/or perform different actions. Actions include on, off, dim, \
brighten, and resum schedule. The "resume" option grants control back to a schedule if the schedule is overridden (see Pico app note for more info).\
Use cases:<br>\
        • Control a light by flipping the Cube.<br>\
        • Control multiple devices, such as having a lamp toggle with flip 90, another lamp toggle with flip 180, and a ceiling fan toggle with shake.<br>\
        • When turning on a bedroom lamp, turn off the living room (or porch) light<br>\
        • Control light brightness with rotate right (brighten) and left (dim) (like a volume knob)<br>\
        • Control a (smart) media player volume... like a volume knob, as well as power, skip track, etc.<br>\
        • Train a pet to flip it to indicate to be let outside (notifications not yet supported).'
        displayInfo(magicCubeText)
    }
}
                                                                                                       
def checkLog(type,logLevel){
    switch(type) {
        case 'error':
        if(logLevel > 0) return true
        break
        case 'warn':
        if(logLevel > 1) return true
        break
        case 'info':
        if(logLevel > 2) return true
        break
        case 'trace':
        if(logLevel > 3) return true
        case 'debug':
        if(logLevel > 4) return true
        break
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber, type = 'trace', message, appId = app.id, isChild = null){
    logLevel = 5
    if(appId == app.id) logLevel = getLogLevel()
    if(appId != app.id){
        appLabel = 'new app'
        if(getChildAppById(appId)) appLabel = (getChildAppById(appId)).label
        if(getChildAppById(appId)) logLevel = (getChildAppById(appId)).getLogLevel()
    }
    
    if(!checkLog(type,logLevel)) return
    if(isChild)  text = '[' + appLabel + ' :: ' + lineNumber + '] -- ' + message
    if(!isChild && appId != app.id) text = '[' + appLabel + '] [Master :: ' + lineNumber + '] -- ' + message        // Within parent, but called from app
    if(!isChild && appId == app.id) text = '[Master :: ' + lineNumber + '] -- ' + message        // Within parent, and not called from app

    if(type == 'error') text = '<font color="red">' + text + '</font>'
    if(type == 'warn') text = '<font color="brown">' + text + '</font>'

    switch(type) {
        case 'error':
        log.error(text)
        return
        case 'warn':
        log.warn(text)
        return
        case 'info':
        log.info(text)
        return
        case 'debug':
        log.debug(text)
        return
        case 'trace':
        log.trace(text)
    }
}
