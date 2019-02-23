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
 
 // Version 1.0 Feb 23 2019 mg
 
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
    }

}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
    
    subscribe(thermostat, "thermostatFanMode", delayControlHandler)    
    subscribe(thermostat, "thermostatOperatingState", delayControlHandler)
    subscribe(thermostat, "thermostatMode", delayControlHandler)

  	runIn(offdelaytime*60, circHandler)
  	log.debug "off delay started"	

	log.debug "initialized"
}


// TODO: implement event handlers

def delayControlHandler(evt) {
	log.debug "circControlHandler called: $evt"
    
    def fanmode = thermostat.currentthermostatFanMode
    def tmode = thermostat.currentthermostatMode
    def tstate = thermostat.currentthermostatOperatingState
        
//log.debug "Tfan ${thermostat.currentthermostatFanMode}"
//log.debug "TMode ${thermostat.currentthermostatMode}"
//log.debug "TState ${thermostat.currentthermostatOperatingState}"
 
	if(fanmode == "fanAuto" && (tmode == "heat" || tmode == "cool" || tmode == "auto") && tstate == "idle")
	  {
	  runIn(offdelaytime*60, circHandler)
	  log.debug "off delay started"
	  }
	  else {
	  		unschedule(circHandler)
	        log.debug "off delay cancelled"
	        }
}


def circHandler() {

	thermostat.fanOn()
    runIn(fanontime *60, doneHandler)
    log.debug "fan turned on"
}
        
    
def doneHandler() {

	thermostat.fanAuto()
    log.debug "fan off"
}
	