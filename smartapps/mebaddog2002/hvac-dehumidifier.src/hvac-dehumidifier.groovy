/**
 *  HVAC Dehumidifier 
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
 *
 *
 *
 *	Version 0.0 Still developing and testing MG
 *  Version 1.0 Debugging done and running good March 21 2020 MG
 *  Version 1.1 Allow dehumidifier to turn on when A/C starts March 28 2020 MG
 *  Version 1.2 Bug fix July 28 2020 MG
 *	Version 1.3 Bug fix for fan only mode Aug 21 2020 MG
 *
 */
definition(
    name: "HVAC Dehumidifier ",
    namespace: "mebaddog2002",
    author: "Michael Goldsberry",
    description: "This app will control a HVAC dehumidifier and turn on the HVAC fan if needed.  The thermostat mode must be in cool for app to work. ",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

	// TODO: put inputs here
preferences {
	section("About App") {
		paragraph "This app will control a HVAC dehumidifier and turn on the HVAC fan if needed."
	}
    section("Thermostat") {
       input "thermostat", "capability.thermostat", required: true, title: "Select Thermostat"
       input "hvacfanneeded", "enum", required: true, title: "Use HVAC fan while dehumidifier is on", options: ["True", "False"], defaultValue: "True"
       input "acdelayneeded", "enum", required: true, title: "Stop dehumidifier after A/C shuts off", options: ["True", "False"], defaultValue: "True"
       input "acdelay", "number", required: true, title: "Delay after A/C before dehumidifier can turn back on in minutes", defaultValue:15
       input "runwithac", "enum", required: true, title: "Start dehumidifier if A/C turns on", options: ["True", "False"], defaultValue: "True"
	}
    section("Humidity Sensor") {
        input "humiditysensor", "capability.relativeHumidityMeasurement", required: true, title: "Select Humidity Sensor"
    }
    section("Whole House Dehumidifier") {
        input "dehumidifierswitch", "capability.switch", required: true, title: "Select Dehumidifier"  
    }
    section("Target Humidity Level") {
    	input "dehumidifieron", "number", required: true, title: "Dehumidifier ON humidity level", defaultValue:50
        input "dehumidifieroff", "number", required: true, title: "Dehumidifier OFF humidity level", defaultValue:47
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

	// TODO: subscribe to attributes, devices, locations, etc.
def initialize() {

	atomicState.startedfan = false
    atomicState.acrunning = false
    atomicState.waiting = false

	subscribe(humiditysensor, "humidity", humidityControlHandler)
    subscribe(thermostat, "thermostatFanMode", humidityControlHandler)
    subscribe(thermostat, "thermostatOperatingState", humidityControlHandler)
    subscribe(thermostat, "thermostatMode", humidityControlHandler)
    subscribe(dehumidifierswitch, "switch", humidityControlHandler)
}

// TODO: implement event handlers

def humidityControlHandler(evt) {
    log.debug "humidityControlHandler called: $evt"
    
    def humidity = humiditysensor.currentHumidity
    def dehumidifier = dehumidifierswitch.currentSwitch
    def fanmode = thermostat.currentthermostatFanMode
    def tmode = thermostat.currentthermostatMode
    def tstate = thermostat.currentthermostatOperatingState
    
log.debug "Current humidity is ${humidity}" 
log.debug "Dehumidifier ${dehumidifier}"
log.debug "fan mode ${fanmode}"
log.debug "Mode ${tmode}"
log.debug "State ${tstate}"
log.debug "started fan ${atomicState.startedfan}"
log.debug "acrunning ${atomicState.acrunning}"
log.debug "waiting ${atomicState.waiting}"

	if(tstate == "cooling" && dehumidifier == "on" && acdelayneeded == "True")
      {
      atomicState.acrunning = true
      atomicState.waiting = false
      log.debug "ac on"
      if(atomicState.startedfan == true)
        {
        thermostat.fanAuto()
        atomicState.startedfan = false
        log.debug "fan turned to auto ac on"
        }
      }
    
    if(tstate == "cooling" && dehumidifier == "off" && runwithac == "True" && humidity > dehumidifieroff)
      {
      log.debug "dehum on because ac turned on"
      dehumidifierswitch.on()
      atomicState.waiting = false
      }
      
    if(atomicState.acrunning == true && (tstate == "idle" || tstate == "fan only") && atomicState.waiting == false)
      {
      atomicState.waiting = true
      dehumidifierswitch.off()
      atomicState.acrunning = false
      runIn(acdelay*60, dehumwaitHandler)
      log.debug "ac off dehum off waiting to restart"
      }

    if(tmode == "cool")
      {
	  if(humidity >= dehumidifieron && atomicState.waiting == false)
        {
        log.debug "need dehum"
        if(fanmode == "auto" && hvacfanneeded == "True" && tstate == "idle")
       	  {
	      thermostat.fanOn()
          dehumidifierswitch.off()
          atomicState.startedfan = true
          log.debug "turned fan on" 
          }
	    if(dehumidifier == "off" && (fanmode == "on" || hvacfanneeded == "False"))
  	      {
          dehumidifierswitch.on()
          log.debug "turned dehumidifier on"
  	      }
        }
      if(humidity <= dehumidifieroff)
        {
        dehumidifierswitch.off()
        log.debug "turned dehumidifier off"
        if(atomicState.startedfan == true)
          {
          thermostat.fanAuto()
          atomicState.startedfan == false
          log.debug "turned fan off"
          }
        }
      } else {
             log.debug "not in cool mode"
             atomicState.acrunning = false
    		 atomicState.waiting = false
             unschedule(dehumwaitHandler)
             if(dehumidifier == "on")
               {
               dehumidifierswitch.off()
               log.debug "dehumidifier turned off not in cool mode"
               }
             if(atomicState.startedfan == true)
               {
               thermostat.fanAuto()
               atomicState.startedfan = false
               log.debug "fan turned to auto not in cool mode"
               }  
             }  
log.debug "started fan ${atomicState.startedfan}"
log.debug "acrunning ${atomicState.acrunning}"
log.debug "waiting ${atomicState.waiting}"
}
    

def dehumwaitHandler() {

	log.debug "wait over"
    atomicState.waiting = false
    humidityControlHandler()
}