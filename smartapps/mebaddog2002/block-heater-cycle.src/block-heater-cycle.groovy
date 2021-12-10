/**
 *  Block Heater Cycle
 *
 *  Copyright 2021 Michael Goldsberry
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
 *
 *
 *  Version 0.0 mg testing and coding
 *
 */
definition(
    name: "Block Heater Cycle",
    namespace: "mebaddog2002",
    author: "Michael Goldsberry",
    description: "Cycle block heater on and off after outside temp drops below set point. ",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

        // TODO: put inputs here
preferences {
	section("Title") {
	    paragraph "This app will cycle a block heater on and off to help maintain a warm coolant temp after the outside temp drops below a set point"
	}
    section("Outside Temp") {
        input "outsidetemp", "capability.temperatureMeasurement", required: true, title: "Select Outside Temp Sensor"
        input "ontemp", "number",required: true, title: "Turn heater on below this temp", defaultValue:40
    }
    section("Timers") {
    	input "ontime", "number", required: true, title: "Block Heater ON Time", defaultValue:30
        input "offtime", "number", required: true, title: "Block Heater OFF Time", defaultValue:30
    }
    section("Block Heater") {
    	input "heater", "capability.switch", required: true, title: "Select Block Heater"
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
    
    atomicState.heateron = false
    atomicState.heaterondone = false
    atomicState.heateroff = false
    atomicState.heateroffdone = false
    
    subscribe(outsidetemp, "temperature", blockheaterControlHandler)
}

// TODO: implement event handlers

def blockheaterControlHandler(evt) {
	log.debug "blockheaterControlHandler called: $evt"
    
    def outsidet = outsidetemp.currentTemperature
    
log.debug "outside temp ${outsidet}"

	if(outsidet <= ontemp)
      {
      log.debug "cycling heater"
      if(atomicState.heateron == false && atomicState.heateroff == false)
        {
        heater.on()
        runIn(ontime * 60, ontimeHandler)
        atomicState.heateron = true
        atomicState.heateroff = false
        log.debug "heater on"
        }
      if(atomicState.heaterondone == true)
        {
        heater.off()
        atomicState.heateroff = true
        atomicState.heateron = false
        atomicState.heaterondone = false
        runIn(offtime * 60, offtimeHandler)
        log.debug "heater off cycle"
        }
      if(atomicState.heateroffdone == true)
      	{
        heater.on()
        atomicState.heateron = true
        atomicState.heateroff = false
        atomicState.heateroffdone = false
        runIn(ontime * 60, ontimeHandler)
        log.debug "heater on cycle"
        }
      } else {
      		 heater.off()
             atomicState.heateron = false
             atomicState.heateroff = false
             atomicState.heaterondone = false
             atomicState.heateroffdone = false
             unschedule(ontimeHandler)
             unschedule(offtimeHandler)
             log.debug "heat not needed"
             }

}

def ontimeHandler(evt){
	atomicState.heaterondone = true
    blockheaterControlHandler()
    log.debug "heater on done"

}

def offtimeHandler(evt){
	atomicState.heateroffdone = true
    blockheaterControlHandler()
    log.debug "heater off done"


}