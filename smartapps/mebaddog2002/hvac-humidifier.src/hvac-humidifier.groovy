/**
 *  HVAC Humidifier
 *
 *  Copyright 2018 Michael Goldsberry
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
 *
 *	Version 0.0 testing and coding
 *  Version 1.0 done changed name and removed testing versions from github March 21 2020 MG
 */
definition(
    name: "HVAC Humidifier",
    namespace: "mebaddog2002",
    author: "Michael Goldsberry",
    description: "This app is to control a whole house humidifier. It only runs when the thermostat is in heat mode. It takes into account the outside temperature and will adjust the humidity in the house so condensation does not build up on the windows.  If you have a steam humidifier it will turn the HVAC fan if the humidity is below the set point and run the humidifier to bring the humidity back up. If you have an evaporation humidifier it will turn on during heating if the humidity is below the set point.  ",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


		// TODO: put inputs here
preferences {
    section("About App"){
       paragraph "This app will control a whole house steam or evaporation humidifer.  It will only work if the thermostat is in the heat mode.  As the outside temperature gets colder it will allow the humidity in the house to lower so condensation does not develop on the windows.  During heating if the humidity drops it will turn on a evap style humidifier.  If the humidity drops below the set point a steam style will turn on the HVAC fan and a steam humidifier to raise the humidity level in the house."
    }
    section("Thermostat") {
       input "thermostat", "capability.thermostat", required: true, title: "Select Thermostat"
	}
    section("Humidity Sensor") {
        input "humiditysensor", "capability.relativeHumidityMeasurement", required: true, title: "Select Humidity Sensor"
    }
    section("Outside Temprature Sensor") {
        input "outsidetemp", "capability.temperatureMeasurement", required: true, title: "Select Outside Temprature Sensor"
    }
    section("Whole House Humidifier") {
        input "humidifierswitch", "capability.switch", required: true, title: "Select Humidifier"  
        input "humidifiertype", "enum", title: "Humidifier Type", options: ["Steam", "Evaporation"], required: true
    }
    section("Target Humidity Level") {
        input "humidity40p", "number", required: true, title: "Humidity Level Above 40 Degrees", defaultValue:45
        input "humidity30to39", "number", required: true, title: "Humidity Level Between 30 to 39 Degrees", defaultValue:40
        input "humidity20to29", "number", required: true, title: "Humidity Level Between 20 to 29 Degrees", defaultValue:35
        input "humidity10to19", "number", required: true, title: "Humidity Level Between 10 to 19 Degrees", defaultValue:30
        input "humidity0to9", "number", required: true, title: "Humidity Level Between 0 to 9 Degrees", defaultValue:25
        input "humiditylow", "number", required: true, title: "Humidity Level Less Then 0 Degrees", defaultValue:20
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

    subscribe(humiditysensor, "humidity", humidityControlHandler)
    subscribe(thermostat, "thermostatOperatingState", humidityControlHandler)
    subscribe(thermostat, "thermostatFanMode", humidityControlHandler)
}

// TODO: implement event handlers.


def humidityControlHandler(evt) {
    log.debug "humidityControlHandler called: $evt"
    
    def fanmode = thermostat.currentthermostatFanMode
    def outsidetemperature = outsidetemp.currentTemperature
    def tmode = thermostat.currentthermostatMode
    def tstate = thermostat.currentthermostatOperatingState
    def humidity = humiditysensor.currentHumidity
    def humidifier = humidifierswitch.currentSwitch
    
log.debug "fan mode ${fanmode}"
log.debug "Outside temp ${outsidetemperature}"    
log.debug "Current humidity is ${humidity}" 
log.debug "Mode ${tmode}"
log.debug "State ${tstate}"
log.debug "Humidifier ${humidifier}"
log.debug "Humidifier type ${humidifiertype}"
log.debug "Started Fan ${atomicState.startedfan}"

	// Run humidifier if heating and evap style if humidity is less then set point

	if(humidifiertype == "Evaporation")
  	  {
      if(tstate == "heating")
        {
  	    log.debug "evap stytle"    
  		if(outsidetemperature >= 40 && humidity < humidity40p)
    	  {
    	  humidifierswitch.on()
    	  log.debug "humidifier on 40+ evap"
          } 
  		if(outsidetemperature >= 30 && outsidetemperature < 40 && humidity < humidity30to39)
          {
          humidifierswitch.on()
          log.debug "humidifier on 30 to 40 evap"
          } 
 		if(outsidetemperature >= 20 && outsidetemperature < 30 && humidity < humidity20to29)
          {
          humidifierswitch.on()
          log.debug "humidifier on 20 to 30 evap"
          }
  		if(outsidetemperature >= 10 && outsidetemperature < 20 && humidity < humidity10to19)
          {
          humidifierswitch.on()
          log.debug "humidifier on 10 to 20 evap"
          }
  		if(outsidetemperature >= 0 && outsidetemperature < 10 && humidity < humidity0to9)
          {
          humidifierswitch.on()
          log.debug "humidifier on 0 to 10 evap"
          }
  		if(outsidetemperature < 0 && humidity < humiditylow)
          {
          humidifierswitch.on()
          log.debug "humidifier on freaking cold evap"                      									
          }
  		} else {
  		       log.debug "humidifier not needed or heating stopped evap"
               if(humidifier == "on")
                 {
                 humidifierswitch.off()
                 log.debug "humidifier turned off evap"
                 } 
 			   }
	  }
   
	// Run steam humidifier and turn on HVAC fan if humidity is 2% less then set point.
    
    if(humidifiertype == "Steam") 
  	  {
      if(tmode == "heat")
        {
        log.debug "steam style check to turn on"
        if(outsidetemperature >= 40 && humidity < humidity40p - 1)
          {
          startSteamHandler()      
          log.debug "humidifier on 40+ steam"
          }          
        if(outsidetemperature >= 30 && outsidetemperature < 40 && humidity < humidity30to39 - 1)
          {
          startSteamHandler()
          log.debug "humidifier on 30 to 40 steam"
          } 
        if(outsidetemperature >= 20 && outsidetemperature < 30 && humidity < humidity20to29 - 1)
          {
          startSteamHandler()
          log.debug "humidifier on 20 to 30 steam"
          }
        if(outsidetemperature >= 10 && outsidetemperature < 20 && humidity < humidity10to19 - 1)
          {
          startSteamHandler()
          log.debug "humidifier on 10 to 20 steam"
          }
        if(outsidetemperature >= 0 && outsidetemperature < 10 && humidity < humidity0to9 - 1)
          {
          startSteamHandler()
          log.debug "humidifier on 0 to 10 steam"
          }
        if(outsidetemperature < 0 && humidity < humiditylow - 1)
          {
          startSteamHandler()
          log.debug "humidifier on freaking cold steam"                      									
          } 
        }
      }  
        
    // Stop steam humidifier and turn off HVAC fan if humidity is equal or greater than set point.
    
    if(humidifiertype == "Steam") 
  	  {
      if(tmode == "heat")
        {
        log.debug "steam style check to turn off"
        if(outsidetemperature >= 40 && humidity >= humidity40p)
          {
          stopSteamHandler()      
          log.debug "humidifier stopping 40+ steam"
          }          
        if(outsidetemperature >= 30 && outsidetemperature < 40 && humidity >= humidity30to39)
          {
          stopSteamHandler()
          log.debug "humidifier stopping 30 to 40 steam"
          } 
        if(outsidetemperature >= 20 && outsidetemperature < 30 && humidity >= humidity20to29)
          {
          stopSteamHandler()
          log.debug "humidifier stopping 20 to 30 steam"
          }
        if(outsidetemperature >= 10 && outsidetemperature < 20 && humidity >= humidity10to19)
          {
          stopSteamHandler()
          log.debug "humidifier stopping 10 to 20 steam"
          }
        if(outsidetemperature >= 0 && outsidetemperature < 10 && humidity >= humidity0to9)
          {
          stopSteamHandler()
          log.debug "humidifier stopping 0 to 10 steam"
          }
        if(outsidetemperature < 0 && humidity >= humiditylow)
          {
          stopSteamHandler()
          log.debug "humidifier stopping freaking cold steam"                      									
          } 
        } else {
               log.debug "not in heat mode steam"
               if(humidifier == "on")
                 {
                 humidifierswitch.off()
                 log.debug "humidifier turned off not heating steam"
                 }
               if(atomicState.startedfan == true)
                 {
                 runIn(120,  stopfanSteamHandler)
                 log.debug "timer started to stop fan not in heat mode"
                 }  
               }  
      }  
}

// Turn fan on then start steam.

def startSteamHandler(evt) {
	log.debug "startSteamHandler called: $evt"

    def fanmodes = thermostat.currentthermostatFanMode
    def humidifiers = humidifierswitch.currentSwitch    

	if(fanmodes == "auto")
      {
	  thermostat.fanOn()
      atomicState.startedfan = true
      log.debug "turned fan on" 
      }

	if(humidifiers == "off" && fanmodes == "on")
  	  {
      humidifierswitch.on()
      log.debug "turned humidifier on steam"
  	  }
}
    
// Turn off steam and start timer for turning fan off.

def stopSteamHandler(evt) {
    log.debug "stopSteamHandler called: $evt"
    
    def humidifierss = humidifierswitch.currentSwitch    

	log.debug "humidifier not needed steam"
    if(humidifierss == "on")
      {
      humidifierswitch.off()
      log.debug "humidifier turned off done steam"
      }
    if(atomicState.startedfan == true)
      {
      runIn(120,  stopfanSteamHandler)
      log.debug "timer started to stop fan"
	  }
}


// Turn fan back to auto.  The steam should be out of ductwork by now.

def stopfanSteamHandler(evt) {
    log.debug "stopfanSteamHandler called: $evt"
    
    def humidifiersf = humidifierswitch.currentSwitch    

	if(humidifiersf == "off")
      {
	  atomicState.startedfan = false
      thermostat.fanAuto()
	  log.debug " fan turned to auto"
      }
}