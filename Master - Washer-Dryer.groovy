/***********************************************************************************************************************
*
*  Copyright (C) 2018 roguetech
*  Derived from Craig Romei Copyright (C) 2019
*
*  License:
*  This program is free software: you can redistribute it and/or modify it under the terms of the GNU
*  General Public License as published by the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
*  implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
*  for more details.
*
*  You should have received a copy of the GNU General Public License along with this program.
*  If not, see <http://www.gnu.org/licenses/>.
*
*  Name: Master - Washer-Dryer
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master - Washer-Dryer.groovy
*  Version: 0.0.03
*
***********************************************************************************************************************/

definition(
    name: "Master - Washer-Dryer",
    namespace: "master",
    author: "roguetech",
    description: "Washer/Dryer",
    parent: "master:Master",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png"
)

preferences {
	infoIcon = "<img src=\"http://files.softicons.com/download/system-icons/windows-8-metro-invert-icons-by-dakirby309/ico/Folders%20&%20OS/Info.ico\" width=20 height=20>"
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
	logTrace("$app.label: installed")
	if(app.getLabel().length() < 12)  app.updateLabel("Washer-dryer - " + app.getLabel())
	if(app.getLabel().substring(0,12) != "Washer-dryer") app.updateLabel("Washer-dryer - " + app.getLabel())
	state.activity = []
    initialize()
}

def updated() {
	logTrace("$app.label: updated")
    unsubscribe()
    initialize()
}

def initialize() {
	logTrace("$app.label: initialized")
	
	if(washerDisable || state.washerDisable) {
		unsubscribe()
		unschedule()
		stat.firstNotice = false
		state.activity = []
		logTrace("$app.label: function initialize returning (washer-dryer disabled)")
		return
	}

	// Clear prior schedules
	unschedule()

	subscribe(washerDevice, "acceleration.active", washerHandler)
	subscribe(washerDevice, "motion.active", washerHandler)
	if(washerContactDevice) subscribe(washerContactDevice, "contact.open", contactHandler)
}

def washerHandler(evt) {
	logTrace("$app.label: washerHandler starting [evt:  $evt ($evt.value)]")

	// If already started alerting, exit
	if(state.firstNotice){
		logTrace("$app.label: function washerHandler returning (notices already processing)")
		return
	}

	if(washerContactDevice && washerContactDevice.contact == "open") {
		logTrace("$app.label: function washerHandler returning (contact is open)")
		return
	}

	if(washerDisable || state.washerDisable) {
		logTrace("$app.label: function washerHandler returning (washer-dryer disabled)")
		return
	}

	// Clear prior schedules
	unschedule()

	// If neccesary, initialize activity history
	if(!state.activity) {
		log.debug state.activity.size()
		state.activity = [now()]
	} else {
		log.debug state.activity.size()
		state.activity.add(time)	
	}

	target = now() - 30 * 60000
	toRemove = []
	// Remove stale activity
	state.activity.each {
		if(it < target) {
			logTrace("$app.label: function washerHandler (removed value $it)")
			toRemove.add(it);
		}
	}
	state.activity.removeAll(toRemove)

	// get count
	listSize = state.activity.size()
	logTrace("$app.label: function washerHandler ($listSize events in the last half hour)")

	// If 10 or more motion events in half hour, washer/dryer is running
	if(listSize > 10){
		logTrace("$app.label: function washerHandler (schedule inactivity check for 6 minutes)")
		runIn(60 * 6, washerSchedule)
	}
}

def contactHandler(evt) {
	logTrace("$app.label: contactHandler clearing alerts")

	unschedule()
	state.firstNotice = false
	state.activity = []

	logTrace("$app.label: contactHandler exiting")
}

def washerSchedule(){
	// if not between start and stop time (or correct day)
	if(timeStart && timeStop && wait){
		if(!parent.timeBetween(timeStart, timeStop) || (timeDays && !parent.todayInDayList(timeDays))) {
			// Schedule for start time
			if(timeToday(timeStop, location.timeZone).time < timeToday(timeStart, location.timeZone).time) timeStop = parent.getTomorrow(timeStop)
			hours = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('HH').toInteger()
			minutes = Date.parse("yyyy-MM-dd'T'HH:mm:ss", timeStop).format('mm').toInteger()
			logTrace("$app.label: function washerSchedule scheduling for start time (0 $minutes $hours * * ?)")
			schedule("0 " + minutes + " " + hours + " * * ?", runDayOffSchedule, [overwrite: true])
			logTrace("$app.label: function washerSchedule returning (not between start time and stop time)")
			return
		}
	} else if(timeStart && timeStop && !wait){
		if(!parent.timeBetween(timeStart, timeStop) || (timeDays && !parent.todayInDayList(timeDays))) {
			logTrace("$app.label: function washerSchedule returning (not between start time and stop time)")
			state.firstnotice = false
			state.activity = []
			return
		}
	}

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
		if(speakText) parent.speak(speakText)
		if(phone) {
			if(speakText) parent.sendText(phone,speakText)
		}

		// Schedule repeat notices
		if(repeat && repeatMinutes){
			logTrace("$app.label: function washerSchedule scheduling repeat notifition for $repeatMinutes")
			runIn(60 * repeatMinutes, washerSchedule)
		}

	// If not home and wait, reschedule in 10 minutes
	} else if(!present && wait){
		runIn(60 * 10, washerSchedule)
		return

	// If not home and not wait, clear and exit
	} else if(!present && !wait){
		logTrace("$app.label: function washerSchedule returning (not home)")
		state.firstnotice = false
		state.activity = []
		return
	}


}

def logTrace(message) {
	//log.trace message
}
