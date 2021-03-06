/**
 *  HVAC Fan Circulation
 *
 *  Copyright 2019 Michael Goldsberry
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
 //	Using this app will shorten the life of your air filter.  Depending on amount of people and pets in home a 3 month air filter could clog in 1 month.
 //	Check you filter regularly until you have established a life span for them.  When the filter starts getting sucked in (center bowed in) it is pasted clogged.
 // Recommend using a good 3 month filter.
 
 
 // Version 1.0 Feb 23 2019 mg
 // Version 2.0 Updated so it can be used in off mode.  Feb 28 2019 MG
 // Version 2.1 Bug fix for "fan only" mode Aug 30 2020 MG
 // Version 2.2 Bug fix so it will verify fan turned off Aug 30 2020 MG
 
definition(
    name: "HVAC Fan Circulation",
    namespace: "Mebaddog2002",
    author: "Michael Goldsberry",
    description: "This app will turn on the HVAC fan to circulate air when it has not ran in a selected time frame. If any device or you turn on the HVAC fan the timer will reset and start again after it is off. If the thermostat is in the OFF mode it will not circulate the fan. ",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


		// TODO: put inputs here
preferences {
	section("Title") {
		paragraph "This app will ciculate the HVAC fan if it has not been on for the selected time and will stay on for the selected time"
	}
    section("Thermostat") {
       input "thermostat", "capability.thermostat", required: true, title: "Select Thermostat"
       input "offdelaytime", "number", required: true, title: "Set time between circulation cycles (in minutes)", defaultValue:30
       input "fanontime", "number", required: true, title: "Set the length of circulation cycle (in minutes)", defaultValue:10
       input "useinoff", "enum", requirerd: true, title: "Use fan circulation in OFF mode", options: ["True", "False"], defaultValue: "True"
       input "humidifierswitch", "capability.switch", title: "Select Humidifier"
       input "dehumidifierswitch", "capability.switch", title: "Select Dehumidifier"
    }

}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
    unschedule()
	initialize()
}

def initialize() {

	atomicState.fanstarted = false
    atomicState.stopfan = false
    atomicState.offdelaystarted = false

	// TODO: subscribe to attributes, devices, locations, etc.
    
    subscribe(thermostat, "thermostatFanMode", delayControlHandler)    
    subscribe(thermostat, "thermostatOperatingState", delayControlHandler)
    subscribe(thermostat, "thermostatMode", delayControlHandler)

  	delayControlHandler()

}


// TODO: implement event handlers

def delayControlHandler(evt) {
	log.debug "delayControlHandler called: $evt"
    
    def fanmode = thermostat.currentthermostatFanMode
    def tmode = thermostat.currentthermostatMode
    def tstate = thermostat.currentthermostatOperatingState
    def humidifier = humidifierswitch.currentSwitch
    def dehumidifier = dehumidifierswitch.currentSwitch
        
log.debug "Tfan ${thermostat.currentthermostatFanMode}"
log.debug "TMode ${thermostat.currentthermostatMode}"
log.debug "TState ${thermostat.currentthermostatOperatingState}"
log.debug "fanstarted ${atomicState.fanstarted}"
log.debug "stopfan ${atomicState.stopfan}"
 
	if(fanmode == "auto" && (tmode != "off" || useinoff == "True") && tstate == "idle")
	  {
      if(atomicState.offdelaystarted == false)
	  	{
        runIn(offdelaytime*60, circHandler)
        atomicState.offdelaystarted = true
	  	log.debug "off delay started"
        }
	  }
	  else {
	  		unschedule(circHandler)
            atomicState.offdelaystarted = false
	        log.debug "off delay cancelled"
	        }
    if((tstate != "idle" && tstate != "fan only") && atomicState.fanstarted == true)
      {
      thermostat.fanAuto()
      atomicState.fanstarted = false
//      atomicState.stopfan = true
//      runIn(60, delayControlHandler)
      unschedule(doneHandler)
      log.debug "Fan turned to auto because system is running"
      }
/*    if(atomicState.stopfan == true && fanmode == "on" && humidifier != "on" && dehumidifier != "on")
      {
      thermostat.fanAuto()
      runIn(60, delayControlHandler)
      log.debug "fan still on try to turn off again"
      }
    if(atomicState.stopfan == true && fanmode == "on" && (humidifier == "on" || dehumidifier == "on"))
      {
      atomicState.stopfan = false
      log.debug "fan on because of humidifier or dehumidifier"
      }
    if(atomicState.stopfan == true && fanmode == "auto")
      {
      atomicState.stopfan = false
      log.debug "checked and fan is off"
      }
*/      
log.debug "fanstarted ${atomicState.fanstarted}"
log.debug "stopfan ${atomicState.stopfan}"

}


def circHandler() {

	thermostat.fanOn()
    atomicState.fanstarted = true
    runIn(fanontime*60, doneHandler)
    log.debug "fan turned on"
}
        
    
def doneHandler() {
	
    atomicState.fanstarted = false
//    atomicState.stopfan = true
	thermostat.fanAuto()
    log.debug "fan off"
//    runIn(60, delayControlHandler)
    
}
	