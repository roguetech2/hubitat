/***********************************************************************************************************************
*
*  Copyright (C) 2018 roguetech
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
*  Name: Master - Presence
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master - Presence.groovy
*  Version: 0.0.02
*
***********************************************************************************************************************/

definition(
    name: "Master - Presence",
    namespace: "master",
    author: "roguetech",
    description: "Presence - Arriving and Leaving",
    parent: "master:Master",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light13-icn@2x.png"
)

preferences {
    page(name: "setup", nextPage: "arriving") {
 
        section() {
            input "presenceAdult", "capability.presenceSensor", title: "Adults", multiple: true, required: true
            input "presenceChild", "capability.presenceSensor", title: "Children", multiple: true, required: false
        }
    }
    page(name: "arriving", nextPage: "leaving"){
        section("• <b><font color=\"#000099\">Arriving</font></b>") {

        }

        section(hideable: true, hidden: true, "Anyone arrives home") {
            input "arriveAnyMode", "mode", title: "Set Mode",  required: false
            input "arriveAnyOn", "capability.switch", title: "Turn on", multiple: true, required: false
            input "arriveAnyOff", "capability.switch", title: "Turn off", multiple: true, required: false
            input "arriveAnyToggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "arriveAnyLock", "capability.lock", title: "Lock", multiple: true, required: false
            input "arriveAnyUnlock", "capability.lock", title: "Unlock", multiple: true, required: false
        }
        section(hideable: true, hidden: true, "Anyone arrives home alone") {
            input "arriveAnyAloneMode", "mode", title: "Set Mode",  required: false
            input "arriveAnyAloneOn", "capability.switch", title: "Turn on", multiple: true, required: false
            input "arriveAnyAloneOff", "capability.switch", title: "Turn off", multiple: true, required: false
            input "arriveAnyAloneToggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "arriveAnyAloneLock", "capability.lock", title: "Lock", multiple: true, required: false
            input "arriveAnyAloneUnlock", "capability.lock", title: "Unlock", multiple: true, required: false
        }
        section(hideable: true, hidden: true, "Any adult arrives home") {
            input "arriveAnyAdultMode", "mode", title: "Set Mode",  required: false
            input "arriveAnyAdultOn", "capability.switch", title: "Turn on", multiple: true, required: false
            input "arriveAnyAdultOff", "capability.switch", title: "Turn off", multiple: true, required: false
            input "arriveAnyAdultToggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "arriveAnyAdultLock", "capability.lock", title: "Lock", multiple: true, required: false
            input "arriveAnyAdultUnlock", "capability.lock", title: "Unlock", multiple: true, required: false
        }
        section(hideable: true, hidden: true, "Any adult arrives home alone") {
            input "arriveAnyAdultAloneMode", "mode", title: "Set Mode",  required: false
            input "arriveAnyAdultAloneOn", "capability.switch", title: "Turn on", multiple: true, required: false
            input "arriveAnyAdultAloneOff", "capability.switch", title: "Turn off", multiple: true, required: false
            input "arriveAnyAdultAloneToggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "arriveAnyAdultAloneLock", "capability.lock", title: "Lock", multiple: true, required: false
            input "arriveAnyAdultAloneUnlock", "capability.lock", title: "Unlock", multiple: true, required: false
        }
        section(hideable: true, hidden: true, "Any child arrives home") {
            input "arriveAnyChildMode", "mode", title: "Set Mode",  required: false
            input "arriveAnyChildOn", "capability.switch", title: "Turn on", multiple: true, required: false
            input "arriveAnyChildOff", "capability.switch", title: "Turn off", multiple: true, required: false
            input "arriveAnyChildToggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "arriveAnyChildLock", "capability.lock", title: "Lock", multiple: true, required: false
            input "arriveAnyChildUnlock", "capability.lock", title: "Unlock", multiple: true, required: false
            input "arriveAnyChildText", "phone", title: "Send Text to Phone #", multiple: true, required: false
        }
        section(hideable: true, hidden: true, "Any child arrives home alone") {
            input "arriveAnyChildAloneMode", "mode", title: "Set Mode",  required: false
            input "arriveAnyChildAloneOn", "capability.switch", title: "Turn on", multiple: true, required: false
            input "arriveAnyChildAloneOff", "capability.switch", title: "Turn off", multiple: true, required: false
            input "arriveAnyChildAloneToggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "arriveAnyChildAloneLock", "capability.lock", title: "Lock", multiple: true, required: false
            input "arriveAnyChildAloneUnlock", "capability.lock", title: "Unlock", multiple: true, required: false
            input "arriveAnyChildAloneText", "phone", title: "Send Text to Phone #", multiple: true, required: false
        }
        section(hideable: true, hidden: true, "All adults are home") {
            input "arriveAllAdultMode", "mode", title: "Set Mode",  required: false
            input "arriveAllAdultOn", "capability.switch", title: "Turn on", multiple: true, required: false
            input "arriveAllAdultOff", "capability.switch", title: "Turn off", multiple: true, required: false
            input "arriveAllAdultToggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "arriveAllAdultLock", "capability.lock", title: "Lock", multiple: true, required: false
            input "arriveAllAdultUnlock", "capability.lock", title: "Unlock", multiple: true, required: false
        }
        section(hideable: true, hidden: true, "All children are home") {
            input "arriveAllChildMode", "mode", title: "Set Mode",  required: false
            input "arriveAllChildOn", "capability.switch", title: "Turn on", multiple: true, required: false
            input "arriveAllChildOff", "capability.switch", title: "Turn off", multiple: true, required: false
            input "arriveAllChildToggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "arriveAllChildLock", "capability.lock", title: "Lock", multiple: true, required: false
            input "arriveAllChildUnlock", "capability.lock", title: "Unlock", multiple: true, required: false
        }
        section(hideable: true, hidden: true, "Everyone is home") {
            input "arriveAllMode", "mode", title: "Set Mode",  required: false
            input "arriveAllOn", "capability.switch", title: "Turn on", multiple: true, required: false
            input "arriveAllOff", "capability.switch", title: "Turn off", multiple: true, required: false
            input "arriveAllToggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "arriveAllLock", "capability.lock", title: "Lock", multiple: true, required: false
            input "arriveAllUnlock", "capability.lock", title: "Unlock", multiple: true, required: false
        }
         }
    page(name: "leaving", nextPage: "colors"){
        section("• <b><font color=\"#000099\">Leaving</font></b>") {

        }
        section(hideable: true, hidden: true, "No one is home") {
            input "departAllMode", "mode", title: "Set Mode",  required: false
            input "departAllOn", "capability.switch", title: "Turn on", multiple: true, required: false
            input "departAllOff", "capability.switch", title: "Turn off", multiple: true, required: false
            input "departAllToggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "departAllLock", "capability.lock", title: "Lock", multiple: true, required: false
            input "departAllUnlock", "capability.lock", title: "Unlock", multiple: true, required: false
        }
        section(hideable: true, hidden: true, "No adults are home") {
            input "departAllAdultMode", "mode", title: "Set Mode",  required: false
            input "departAllAdultOn", "capability.switch", title: "Turn on", multiple: true, required: false
            input "departAllAdultOff", "capability.switch", title: "Turn off", multiple: true, required: false
            input "departAllAdultToggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "departAllAdultLock", "capability.lock", title: "Lock", multiple: true, required: false
            input "departAllAdultUnlock", "capability.lock", title: "Unlock", multiple: true, required: false
        }
        section(hideable: true, hidden: true, "No children are home") {
            input "departAllChildMode", "mode", title: "Set Mode",  required: false
            input "departAllChildOn", "capability.switch", title: "Turn on", multiple: true, required: false
            input "departAllChildOff", "capability.switch", title: "Turn off", multiple: true, required: false
            input "departAllChildToggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "departAllChildLock", "capability.lock", title: "Lock", multiple: true, required: false
            input "departAllChildUnlock", "capability.lock", title: "Unlock", multiple: true, required: false
        }
        section(hideable: true, hidden: true, "Any adults leaves") {
            input "departAnyAdultMode", "mode", title: "Set Mode",  required: false
            input "departAnyAdultOn", "capability.switch", title: "Turn on", multiple: true, required: false
            input "departAnyAdultOff", "capability.switch", title: "Turn off", multiple: true, required: false
            input "departAnyAdultToggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "departAnyAdultLock", "capability.lock", title: "Lock", multiple: true, required: false
            input "departAnyAdultUnlock", "capability.lock", title: "Unlock", multiple: true, required: false
        }
        section(hideable: true, hidden: true, "Any child leaves") {
            input "departAnyChildMode", "mode", title: "Set Mode",  required: false
            input "departAnyChildOn", "capability.switch", title: "Turn on", multiple: true, required: false
            input "departAnyChildOff", "capability.switch", title: "Turn off", multiple: true, required: f
            input "departAnyChildToggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "departAnyChildLock", "capability.lock", title: "Lock", multiple: true, required: false
            input "departAnyChildUnlock", "capability.lock", title: "Unlock", multiple: true, required: false
            input "departAnyChildText", "phone", title: "Send Text to Phone #", multiple: true, required: false
        }
        section(hideable: true, hidden: true, "Anyone leaves") {
            input "departAnyMode", "mode", title: "Set Mode",  required: false
            input "departAnyOn", "capability.switch", title: "Turn on", multiple: true, required: false
            input "departAnyOff", "capability.switch", title: "Turn off", multiple: true, required: false
            input "departAnyToggle", "capability.switch", title: "Toggle (if on, turn off; if off, turn on)", multiple: true, required: false
            input "departAnyLock", "capability.lock", title: "Lock", multiple: true, required: false
            input "departAnyUnlock", "capability.lock", title: "Unlock", multiple: true, required: false
        }
         }
    page(name: "colors", install: true, uninstall: true){
        section("• <b><font color=\"#000099\">Arrivals Flash Colors</font></b>") {

        }
        section(hideable: true, hidden: true, "Person 1"){
			input "flashPerson1", "capability.presenceSensor", title: "Person 1", multiple: true, required: false
            input "flashDevice1", "capability.switch", title: "Flash", required: false
            input "flashColor1", "color", title: "Flash Color", required: false
        }
        section(hideable: true, hidden: true, "Person 2"){
            input "flashPerson2", "capability.presenceSensor", title: "Person 2", multiple: true, required: false
            input "flashDevice2", "capability.switch", title: "Flash", required: false
            input "flashColor2", "color", title: "Flash Color", required: false
        }
        section(hideable: true, hidden: true, "Person 3"){
            input "flashPerson3", "capability.presenceSensor", title: "Person 3", multiple: true, required: false
            input "flashDevice3", "capability.switch", title: "Flash", required: false
            input "flashColor3", "color", title: "Flash Color", required: false
        }
        section(hideable: true, hidden: true, "Person 4"){
            input "flashPerson4", "capability.presenceSensor", title: "Person 4", multiple: true, required: false
            input "flashDevice4", "capability.switch", title: "Flash", required: false
            input "flashColor4", "color", title: "Flash Color", required: false
        }
    }
}

def installed() {
	if(app.getLabel().length() < 8)  app.updateLabel("Presence - " + app.getLabel())
    if(app.getLabel().substring(0,8) != "Presence") app.updateLabel("Presence - " + app.getLabel())
    initialize()
}

def updated() {
    unsubscribe()
    initialize()
}

def initialize() {
    log.debug "Presence initialized"
    subscribe(presenceAdult, "presence", presenceHandlerAdult) 
    subscribe(presenceChild, "presence", presenceHandlerChild) 
   
}

def presenceHandlerAdult(evt) {
	def appId = app.getId()
    def person = evt.value

    if(evt.value == "present") {
        //anyone arrives
        if(arriveAnyMode != null) parent.changeMode(arriveAnyMode)
        parent.multiOn(arriveAnyOn,appId)
        parent.multiOff(arriveAnyOff,appId)
        parent.toggle(arriveAnyToggle,appId)
        parent.multiLock(arriveAnyLock,appId)
        parent.multiUnlock(arriveAnyUnlock,appId)
        
        //anyone arrives alon
        anyonePresent = false
        presenceAdult.each {
            if(it == "present" && evt.displayName != it) anyonePresent = true
        }
        presenceChild.each{
            if(it == "present" && evt.displayName != it) anyonePresent = true
        }
        if(anyonePresent == false){
            log.debug "Presense: Someone arrived alone."
            if(arriveAnyAloneMode != null) parent.changeMode(arriveAnyAloneMode)
            parent.multiOn(arriveAnyAloneOn,appId)
            parent.multiOff(arriveAnyAlonetOff,appId)
            parent.toggle(arriveAnyAloneToggle,appId)
            parent.multiLock(arriveAnyAloneLock,appId)
            parent.multiUnlock(arriveAnyAloneUnlock,appId)
        }

        //any adult is home
        log.debug "Presense: Adult arrived."
        if(arriveAnyAdultMode != null) parent.changeMode(arriveAnyAdultMode)
        parent.multiOn(arriveAnyAdultOn,appId)
        parent.multiOff(arriveAnyAdultOff,appId)
        parent.toggle(arriveAnyAdultToggle,appId)
        parent.multiLock(arriveAnyAdultLock,appId)
        parent.multiUnlock(arriveAnyAdultUnlock,appId)

                
        //any adult arrives alone
        anyAdultPresent = false
        presenceAdult.each{
            if(it == "present" && evt.displayName != it) anyAdultPresent = true
        }
        if(anyAdultPresent == false){
            log.debug "Presense: Adult arrived home."
            if(arriveAnyAdultAloneMode != null) parent.changeMode(arriveAnyAdultAloneMode)
            parent.multiOn(arriveAnyAdultAloneOn,appId)
            parent.multiOff(arriveAnyAdultAlonetOff,appId)
            parent.toggle(arriveAnyAdultAloneToggle,appId)
            parent.multiLock(arriveAnyAdultAloneLock,appId)
            parent.multiUnlock(arriveAnyAdultAloneUnlock,appId)
        }

        //All adults are home
        allAdultsPresent = true
        presenceAdult.each{
            if(it == "not present") allAdultsPresent = false
        }
        if(allAdultsPresent == true){
            log.debug "Presense: All adults home."
            if(arriveAllAdultMode != null) parent.changeMode(arriveAllAdultMode)
            parent.multiOn(arriveAllAdultOn,appId)
            parent.multiOff(arriveAllAdultOff,appId)
            parent.toggle(arriveAllAdultToggle,appId)
            parent.multiLock(arriveAllAdultLock,appId)
            parent.multiUnlock(arriveAllAdultUnlock,appId)
        }

		//Everyone is home
        allPresent = true
        presenceAdult.each{
            if(it == "not present") allAdultsPresent = false
        }
        presenceChild.each{
            if(it == "not present") allPresent = false
        }
        if(allPresent == true){
            log.debug "Presense: Everyone is home."
            if(arriveAllMode != null) parent.changeMode(arriveAllMode)
            parent.multiOn(arriveAllOn,appId)
            parent.multiOff(arriveAllOff,appId)
            parent.toggle(arriveAllToggle,appId)
            parent.multiLock(arriveAllLock,appId)
            parent.multiUnlock(arriveAllUnlock,appId)
        }
    } else if(evt.value == "not present"){
        //anyone
        if(departAnyMode != null) parent.changeMode(departAnyMode)
        parent.multiOn(departAnyOn,appId)
        parent.multiOff(departAnyOff,appId)
        parent.toggle(departAnyToggle,appId)
        parent.multiLock(departAnyLock,appId)
        parent.multiUnlock(departAnyUnlock,appId)

        //any adult
        log.debug "Presense: Adult left."
        if(departAnyAdultMode != null) parent.changeMode(departAnyAdultMode)
        parent.multiOn(departAnyAdultOn,appId)
        parent.multiOff(departAnyAdultOff,appId)
        parent.toggle(departAnyAdultToggle,appId)
        parent.multiLock(departAnyAdultLock,appId)
        parent.multiUnlock(departAnyAdultUnlock,appId)

        //All adults
        allAdultsPresent = false
        presenceAdult.each{
            if(it == "present") allAdultsPresent = true
        }
        if(allAdultsPresent == false){
        log.debug "Presense: All adults left."
            if(departAllAdultMode != null) parent.changeMode(departAllAdultMode)
            parent.multiOn(departAllAdultOn,appId)
            parent.multiOff(departAllAdultOff,appId)
            parent.toggle(departAllAdultToggle,appId)
            parent.multiLock(departAllAdultLock,appId)
            parent.multiUnlock(departAllAdultUnlock,appId)
        }

		//Everyone
        allPresent = false
        presenceAdult.each{
            if(it == "present") allPresent = true
        }
       presenceChild.each{
            if(it == "present") allPresent = true
        }
        if(allPresent == false){
            
        log.debug "Presense: Everyone left."
            if(departAllMode != null) parent.changeMode(departAllMode)
            parent.multiOn(departAllOn,appId)
            parent.multiOff(departAllOff,appId)
            parent.toggle(departAllToggle,appId)
            parent.multiLock(departAllLock,appId)
            parent.multiUnlock(departAllUnlock,appId)
        }
    }
}

def presenceHandlerChild(evt){
	def appId = app.getId()
    def person = evt.value
	
    if(evt.value == "present"){
        //anyone
        if(arriveAnyMode != null) parent.changeMode(arriveAnyMode)
        parent.multiOn(arriveAnyOn,appId)
        parent.multiOff(arriveAnyOff,appId)
        parent.toggle(arriveAnyToggle,appId)
        parent.multiLock(arriveAnyLock,appId)
        parent.multiUnlock(arriveAnyUnlock,appId)

        //any child
        if(arriveAnyChildMode != null) parent.changeMode(arriveAnyAdultMode)
        parent.multiOn(arriveAnyChildOn,appId)
        parent.multiOff(arriveAnyChildOff,appId)
        parent.toggle(arriveAnyChildToggle,appId)
        parent.multiLock(arriveAnyChildLock,appId)
        parent.multiUnlock(arriveAnyChildUnlock,appId)

        //All children
        allChildPresent = "true"
        presenceChild.each{
            if(it == "not present") allChildPresent = "false"
        }
        if(allChildPresent == "true"){
            if(arriveAllChildMode != null) parent.changeMode(arriveAllChildMode)
            parent.multiOn(arriveAllChildOn,appId)
            parent.multiOff(arriveAllChildOff,appId)
            parent.toggle(arriveAllChildToggle,appId)
            parent.multiLock(arriveAllChildLock,appId)
            parent.multiUnlock(arriveAllChildUnlock,appId)
        }

		//Everyone
        allPresent = "true"
        presenceChild.each{
            if(it == "not present") allPresent = "false"
        }
       presenceAdult.each{
            if(it == "not present") allPresent = "false"
        }
        if(allPresent == "true"){
            if(arriveAllMode != null) parent.changeMode(arriveAllMode)
            parent.multiOn(arriveAllOn,appId)
            parent.multiOff(arriveAllOff,appId)
            parent.toggle(arriveAllToggle,appId)
            parent.multiLock(arriveAllLock,appId)
            parent.multiUnlock(arriveAllUnlock,appId)
        }
    } else if(evt.value == "not present"){
        //anyone
        if(departAnyMode != null) parent.changeMode(departAnyMode)
        parent.multiOn(departAnyOn,appId)
        parent.multiOff(departAnyOff,appId)
        parent.toggle(departAnyToggle,appId)
        parent.multiLock(departAnyLock,appId)
        parent.multiUnlock(departAnyUnlock,appId)

        //any child
        if(departAnyChildMode != null) parent.changeMode(departAnyChildMode)
        parent.multiOn(departAnyChildOn,appId)
        parent.multiOff(departAnyChildOff,appId)
        parent.multiToggle(departAnyChildToggle,appId)
        parent.multiLock(departAnyChildLock,appId)
        parent.multiUnlock(departAnyChildUnlock,appId)

        //All children
        allChildPresent = "false"
        presenceChild.each{
            if(it == "present") allChildPresent = "true"
        }
        if(allChildPresent == "false"){
            if(departAllChildMode != null) parent.changeMode(departAllChildMode)
            parent.multiOn(departAllChildOn,appId)
            parent.multiOff(departAllChildOff,appId)
            parent.toggle(departAllChildToggle,appId)
            parent.mutliLock(departAllChildLock,appId)
            parent.multiUnlock(departAllChildUnlock,appId)
        }

		//Everyone
        allPresent = "false"
        presenceChild.each{
            if(it == "present") allChildPresent = "true"
        }
       presenceAdult.each{
            if(it == "present") allPresent = "true"
        }
        if(allPresent == "false"){
            if(departAllMode != null) parent.changeMode(departAllMode)
            parent.multiOn(departAllOn,appId)
            parent.multiOff(departAllOff,appId)
            parent.toggle(departAllToggle,appId)
            parent.multiLock(departAllLock,appId)
            parent.multiUnlock(departAllUnlock,appId)
        }
    }
}

