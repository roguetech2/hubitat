/***********************************************************************************************************************
*
*  Copyright (C) 2020 roguetech
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
*  Name: Master - Washer-Dryer
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master%20-%20Washer-Dryer.groovy
*  Version: 0.0.14
*
***********************************************************************************************************************/

definition(
    name: "Master - Washer-Dryer",
    namespace: "master",
    author: "roguetech",
    description: "Washer/Dryer",
    parent: "master:Master",
    category: "Convenience",
    importUrl: "https://raw.githubusercontent.com/roguetech2/hubitat/master/Master%20-%20Washer-Dryer.groovy",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png"
)

// logLevel sets number of log messages
// 0 for none
// 1 for errors only
// 5 for all
def getLogLevel(){
    return 5
}

/* ************************************************** */
/* TO-DO: Add error messages (and change info icon    */
/* (see humidity).                                    */
/* ************************************************** */ 
preferences {
	infoIcon = "<img src=\"http://emily-john.love/icons/information.png\" width=20 height=20>"
	errorIcon = "<img src=\"http://emily-john.love/icons/error.png\" width=20 height=20>"
    page(name: "setup", install: true, uninstall: true) {
		
        section() {
			// Set disable all
			if(washerDisableAll) {
				state.washerDisable = true
			} else {
				state.washerDisable = false
			}

			// If all disabled, force reenable
			if(state.washerDisable){
				input "washerDisableAll", "bool", title: "<b>All washer-dryers are disabled.</b> Reenable?", defaultValue: false, submitOnChange:true
			// If disabled, show only basic options
			} else if(washerDisable){
				paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this washer-dryer routine:</b></div>"
				label title: "", required: true, submitOnChange:true
				if(!app.label){
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				} else if(app.label){
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select washer-dryer sensor(s):</b></div>"
					input "washerDevice", "capability.accelerationSensor", title: "Vibration Sensor(s)?", multiple: true, required: true, submitOnChange:true
					input "washerDisable", "bool", title: "<b><font color=\"#000099\">Washer-dryer is disabled.</font></b> Reenable it?", submitOnChange:true
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				}
			} else {
				paragraph "<div style=\"background-color:BurlyWood\"><b> Set name for this washer-dryer routine:</b></div>"
				label title: "", required: true, submitOnChange:true
				if(!app.label){
					paragraph "<div style=\"background-color:BurlyWood\"> </div>"
				} else if(app.label){
					paragraph "<div style=\"background-color:BurlyWood\"><b> Select washer-dryer for routine:</b></div>"
					input "washerDevice", "capability.accelerationSensor", title: "Vibration sensor(s)?", multiple: true, required: true, submitOnChange:true
					input "washerDisable", "bool", title: "Disable this washer-dryer routine?", submitOnChange:true
					if(!washerDevice){
						paragraph "<div style=\"background-color:BurlyWood\"> </div>"
					} else if(washerDevice){
						paragraph "<div style=\"background-color:BurlyWood\"><b> Select contact sensor:</b></div>"
						if(noWasherContactDevice){
							input "noWasherContactDevice", "bool", title: "No contact sensor. Click to select a contact sensor.", submitOnChange:true
						} else {
							input "noWasherContactDevice", "bool", title: "Click for no contact sensor.", submitOnChange:true
						}
						if(!noWasherContactDevice)
							input "washerContactDevice", "capability.contactSensor", title: "Contact sensor(s)?", multiple: false, required: true, submitOnChange:true
						if(!washerContactDevice && !noWasherContactDevice){
							paragraph "<div style=\"background-color:BurlyWood\"> </div>"
						} else if(washerContactDevice || noWasherContactDevice){
							paragraph "<div style=\"background-color:BurlyWood\"><b> Set alert: </b></div>"
							input "phone", "phone", title: "Number to text alert? (Optional)", required: false, submitOnChange:true
							if(parent.notificationDevice) input "speak", "bool", title: "Use voice notification (Optional)", submitOnChange:true
							if((speak || phone) && !speakText) text = "The washer-dryer is finished."
							if(speak || phone) input "speakText", "text", title: "Text to send and/or speak?", defaultValue: speakText, required: true, submitOnChange:true

							if(!phone && (!speak || !speakText)){
								paragraph "<div style=\"background-color:BurlyWood\"> </div>"
							} else if(phone || (speak && speakText)){
								paragraph "<div style=\"background-color:BurlyWood\"><b> Set time and presence exceptions:</b></div>"
								if(!washerContactDevice || noWasherContactDevice){
									if(repeat){
										input "repeat", "bool", title: "Repeating notification. Click to only notify once.", submitOnChange:true
										input "repeatMinutes", "number", title: "Repeat every number of minutes", submitOnChange:true
										if(repeatMinutes && repeatMinutes < 30) paragraph "<div style=\"background-color:AliceBlue\">$infoIcon  Repeating every $repeatMinutes could be very annoying.</div>"
									} else {
										input "repeat", "bool", title: "Only notify once. Click to repeat notification.", submitOnChange:true
									}
								}	
								if(timeStop){
									paragraph "Only alert between start time", width: 6
								} else {
									paragraph "Only alert between start time (optional)", width: 6
								}

								if(timeStart){
									paragraph "and stop time", width: 6
								} else {
									paragraph "and stop time (optional)", width: 6
								}
								input "timeStart", "time", title: "", required: false, width: 6, submitOnChange:true
								input "timeStop", "time", title: "", required: false, width: 6, submitOnChange:true
								input "timeDays", "enum", title: "On these days: (Optional):", required: false, multiple: true, width: 12, options: ["Monday": "Monday", "Tuesday": "Tuesday", "Wednesday": "Wednesday", "Thursday": "Thursday", "Friday": "Friday", "Saturday": "Saturday", "Sunday": "Sunday"]
								input "personHome", "capability.presenceSensor", title: "Only alert if any of these people are home (optional)", multiple: true, required: false, submitOnChange:true
								if((timeStart && timeStop) || personHome){
									if(!timeStart || !timeStop){
										input "wait", "bool", title: "If $personHome isn't present, send notice on arrival?", submitOnChange:true
									} else if(timeStart && timeStop && !personHome){
										input "wait", "bool", title: "If after Stop Time, schedule notice at Start Time?", submitOnChange:true
									} else {
										input "wait", "bool", title: "If after Stop Time or $personHome isn't present, schedule notice at Start Time and/or on arrival?", submitOnChange:true
									}
								}
							}
						}
					}
					
					input "washerDisableAll", "bool", title: "Disable <b>ALL</b> washer-dryer routines?", defaultValue: false, submitOnChange:true
				}
			}
		}
	}
}

/*
Input fields:

washerDisableAll - bool
	disables all washer-dryer routines
washerDevice - device - required - multiple
	primary washer-dryer sensor
washerDisable - bool
	disables this washer-dryer routine
phone - phone
	phone number to text
speak - bool
	enables voice alert
speakText - text - required
	text to speak
timeStart - time
	start time for alerts
timeStop - time
	stop time for alerts
timeDays - enum [Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday
	days on which to alert
personHome - device
	presence sensors required as present

*/

def installed() {
	if(checkLog(a="trace")) putLog(176,"Installed",a)

    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

	state.activity = []
    initialize()
}

def updated() {
	if(checkLog(a="trace")) putLog(185,"Updated",a)
    unsubscribe()
    initialize()
}

def initialize() {
	if(checkLog(a="trace")) putLog(191,"Initialized",a)

    app.updateLabel(parent.appendAppTitle(app.getLabel(),app.getName()))

	if(washerDisable || state.washerDisable) {
		unsubscribe()
		unschedule()
		stat.firstNotice = false
		state.activity = []
		if(checkLog(a="debug")) putLog(200,"Washer-dryer disabled",a)
		return
	}

	// Clear prior schedules
	unschedule()

	subscribe(washerDevice, "acceleration.active", washerHandler)
	subscribe(washerDevice, "motion.active", washerHandler)
	if(washerContactDevice) subscribe(washerContactDevice, "contact.open", contactHandler)
}

def washerHandler(evt) {
	if(checkLog(a="debug")) putLog(213,"washerHandler starting [evt:  $evt ($evt.value)]",a)

	// If already started alerting, exit
	if(state.firstNotice){
		if(checkLog(a="trace")) putLog(217,"Washer/dryer notices already processing",a)
		return
	}

	if(washerContactDevice && washerContactDevice.contact == "open") {
		if(checkLog(a="trace")) putLog(222,"Washer Handler doing nothing, contact is open",a)
		return
	}

	if(washerDisable || state.washerDisable) return

	// Clear prior schedules
	unschedule()

	// If neccesary, initialize activity history
	if(!state.activity) {
		state.activity = [now()]
	} else {
		state.activity.add(time)	
	}

	target = now() - 30 * 60000
	toRemove = []
	// Remove stale activity
	state.activity.each {
		if(it < target) {
			if(checkLog(a="debug")) putLog(243,"function washerHandler removed value $it",a)
			toRemove.add(it);
		}
	}
	state.activity.removeAll(toRemove)

	// get count
	listSize = state.activity.size()
	if(checkLog(a="trace")) putLog(251,"function washerHandler registered $listSize events in the last half hour",a)

	// If 10 or more motion events in half hour, washer/dryer is running
	if(listSize > 10){
		if(checkLog(a="trace")) putLog(255,"Scheduling inactivity check for 6 minutes",a)
		runIn(60 * 6, washerSchedule)
	}
}

def contactHandler(evt) {
	if(checkLog(a="debug")) putLog(261,"$evt.displayName changed to $evt.value",a)

	unschedule()
	state.firstNotice = false
	state.activity = []
}

def washerSchedule(){
	// if not between start and stop time (or correct day)
	if(timeStart && timeStop && wait){
		if(!parent.timeBetween(timeStart, timeStop,app.label) || (timeDays && !parent.todayInDayList(timeDays,app.label))) {
			// Schedule for start time
			if(timeToday(timeStop, location.timeZone).time < timeToday(timeStart, location.timeZone).time) timeStop = parent.getTomorrow(timeStop,app.label)
			hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('HH').toInteger()
			minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('mm').toInteger()
			if(checkLog(a="trace")) putLog(276,"function washerSchedule scheduling for start time (0 $minutes $hours * * ?)",a)
			schedule("0 " + minutes + " " + hours + " * * ?", runDayOffSchedule, [overwrite: true])
			if(checkLog(a="trace")) putLog(278,"function washerSchedule returning (not between start time and stop time)",a)
			return
		}
	} else if(timeStart && timeStop && !wait){
		if(!parent.timeBetween(timeStart, timeStop,app.label) || (timeDays && !parent.todayInDayList(timeDays,app.label))) {
			if(checkLog(a="trace")) putLog(283,"function washerSchedule returning (not between start time and stop time)",a)
			state.firstnotice = false
			state.activity = []
			return
		}
	}

	//Why are we setting this to true only if no timeStop?!
	// That doesn't seem right. Maybe should figure out wtf this does.
	state.firstNotice = true

	if(personHome){
		present = false
		personHome.each{
			if(it.currentPresence == "present") present = true
		}
	} else {
		present = true
	}

	// If home, announce
	if(present){
		if(speakText) parent.speak(speakText,app.label)
		if(phone) {
			if(speakText) parent.sendText(phone,speakText,app.label)
		}

		// Schedule repeat notices
		if(repeat && repeatMinutes){
			if(checkLog(a="trace")) putLog(312,"Scheduling repeat notifition for $repeatMinutes",a)
			runIn(60 * repeatMinutes, washerSchedule)
		}

	// If not home and wait, reschedule in 10 minutes
	} else if(!present && wait){
		if(checkLog(a="trace")) putLog(318,"Scheduling notifition for $repeatMinutes; not currently home",a)
		runIn(60 * 10, washerSchedule)
		return

	// If not home and not wait, clear and exit
	} else if(!present && !wait){
		if(checkLog(a="trace")) putLog(324,"Washer Schedule doing nothing; not home",a)
		state.firstnotice = false
		state.activity = []
		return
	}
}

// Returns the value of deviceChange
// Used by schedule when a device state changes to on, to check if an app did it
// Function must be in every app
def getStateDeviceChange(singleDeviceId){
    return false
}

def checkLog(type = null){
    if(!state.logLevel) getLogLevel()
    switch(type) {
        case "error":
        if(state.logLevel > 0) return "error"
        break
        case "warn":
        if(state.logLevel > 1) return "warn"
        break
        case "info":
        if(state.logLevel > 2) return "info"
        break
        case "trace":
        if(state.logLevel > 3) return "trace"
        break
        case "debug":
        if(state.logLevel == 5) return "debug"
    }
    return false
}

//lineNumber should be a number, but can be text
//message is the log message, and is not required
//type is the log type: error, warn, info, debug, or trace, not required; defaults to trace
def putLog(lineNumber,message = null,type = "trace"){
    message = (message ? " -- $message" : "")
    if(lineNumber) message = "(line $lineNumber)$message"
    message = "$app.label $message"
    if(type == "error") message = "<font color=\"red\">$message</font>"
    if(type == "warn") message = "<font color=\"yellow\">$message</font>"
    switch(type) {
        case "error":
        log.error(message)
        return true
        case "warn":
        log.warn(message)
        return true
        case "info":
        log.info(message)
        return true
        case "trace":
        log.trace(message)
        return true
        case "debug":
        log.debug(message)
        return true
    }
    return
}
