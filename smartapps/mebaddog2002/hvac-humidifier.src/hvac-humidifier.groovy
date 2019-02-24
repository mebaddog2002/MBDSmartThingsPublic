/**
 *  HVAC Humidifier
 *
 *  Copyright 2017 Michael Goldsberry
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
 
 
 
 
 // This is a testing app for me.  Only use it in evaporator style.  Steam style has bugs.
 
definition(
    name: "HVAC Humidifier",
    namespace: "mebaddog2002",
    author: "Michael Goldsberry",
    description: "This is for a whole house humidifier connected to the HVAC system.  This app is designed to work with a steam style humidifier that only needs the HVAC fan on.  It will turn on the HVAC fan and humidifier when the humidity drops below the set point.  It will still work with the evaporate style humidifiers.  Just note that it probably wont raise the humidity very much. It will not hurt the system if it comes on.  It will only run the humidifier if your thermostat is set to heat. So no need to remove app during summer. You will need some kind of air proving switch so the app can verify that the fan is running.  This app will also take into consideration the outside temp and automatically adjust your house humidity level so condensation does not develop on the inside of the windows. ",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

		// TODO: put inputs here
preferences {
    section("About App"){
       paragraph "This app will control a whole house steam humidifer.  It will only work if the thermostat is in the heat mode.  You need to install a fan air pressure proving switch in the duct work.  This is so the humidifier does not run if the fan is not circulating air.  As the outside temperature gets colder it will allow the humidity in the house to lower so condensation does not develop on the windows.  During heating if the humidity drops it will turn on the humidifier.  If the humidity is to low in the house after the heat turns off it will turn on the HVAC fan and the humidifier to raise the humidity level in the house."
    }
    section("Thermostat") {
       input "thermostat", "capability.thermostat", required: true, title: "Select Thermostat"
	}
    section("Humidity Sensor") {
        input "humiditysensor", "capability.relativeHumidityMeasurement", required: true, title: "Select Humidity Sensor"
    }
    section("HVAC Fan Proving Sensor") {
        input "fanon", "capability.contactSensor", required: true, title: "Select Fan Proving Sensor"
    }
    section("Outside Temprature Sensor") {
        input "outsidetemp", "capability.temperatureMeasurement", required: true, title: "Select Outside Temprature Sensor"
    }
    section("Whole House Humidifier") {
        input "humidifier", "capability.switch", required: true, title: "Select Humidifier"   
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
    subscribe(humiditysensor, "humidity", humidityControlHandler)
    subscribe(fanon, "contact", humidityControlHandler)
    subscribe(thermostat, "thermostatOperatingState", humidityControlHandler)
}

// TODO: implement event handlers.
def humidityControlHandler(evt) {
    log.debug "humidityControlHandler called: $evt"

log.debug "fan on state ${fanon.currentContact}"
log.debug "Outside temp ${outsidetemp.currentTemperature}"    
log.debug "Current humidity is ${humiditysensor.currentHumidity}" 
log.debug "Mode ${thermostat.currentthermostatMode}"
log.debug "State ${thermostat.currentthermostatOperatingState}"

if(fanon.currentContact == "open") 
  {
   humidifier.off()
   log.debug "humidifier off no fan"
   }

if(thermostat.currentthermostatMode == "heat")
   {
   if((outsidetemp.currentTemperature >= 40 && humiditysensor.currentHumidity < humidity40p - 3) || (thermostat.currentthermostatOperatingState == "heating" && humiditysensor.currentHumidity < humidity40p + 3 && outsidetemp.currentTemperature >= 40))
   	  {
      if(thermostat.currentthermostatOperatingState == "idle")
        {
         thermostat.fanOn() 
         }
      log.debug "fan for 40"
      if(fanon.currentContact == "closed")
      	{
         humidifier.on()  
         log.debug "humidifier on 40"
         }	
         else
         	 {
              log.debug "no fan 40"
             }
      } 
      else 
          {
           if((outsidetemp.currentTemperature >= 30 && outsidetemp.currentTemperature < 40 && humiditysensor.currentHumidity < humidity30to39 - 3) || (thermostat.currentthermostatOperatingState == "heating" && humiditysensor.currentHumidity < humidity30to39 + 3 && outsidetemp.currentTemperature >= 30 && outsidetemp.currentTemperature < 40))
             {
              if(thermostat.currentthermostatOperatingState == "idle")
                {
                 thermostat.fanOn()
                 }
              log.debug "fan for 30"
              if(fanon.currentContact == "closed") 
        	    {
                 humidifier.on()
                 log.debug "humidifier on 30"
                 }
                 else
         	         {
                      log.debug "no fan 30"
                     }
              }
              else
                  {
                   if((outsidetemp.currentTemperature >= 20 && outsidetemp.currentTemperature < 30 && humiditysensor.currentHumidity < humidity20to29 - 3) || (thermostat.currentthermostatOperatingState == "heating" && humiditysensor.currentHumidity < humidity20to29 + 3 && outsidetemp.currentTemperature >= 20 && outsidetemp.currentTemperature < 30))
         		     {
                      if(thermostat.currentthermostatOperatingState == "idle")
                        {
                         thermostat.fanOn()
                         }
                      log.debug "fan for 20"
                      if(fanon.currentContact == "closed") 
        	            {
                         humidifier.on()
                         log.debug "humidifier on 20"
                         }
                         else
         					 {
          				      log.debug "no fan 20"
          				     }
                      }
                      else 
                          {
                           if((outsidetemp.currentTemperature >= 10 && outsidetemp.currentTemperature < 20 && humiditysensor.currentHumidity < humidity10to19 - 3) || (thermostat.currentthermostatOperatingState == "heating" && humiditysensor.currentHumidity < humidity10to19 + 3 && outsidetemp.currentTemperature >= 10 && outsidetemp.currentTemperature < 20))
         	  	             {
                              if(thermostat.currentthermostatOperatingState == "idle")
                                {
                                 thermostat.fanOn()
                                 }
                              log.debug "fan for 10"
                              if(fanon.currentContact == "closed") 
        	      	            {
                                 humidifier.on()
                                 log.debug "humidifier on 10"
                                 }
                                 else
         							 {
           						      log.debug "no fan 10"
            						  }
                             }
                             else 
                                 {
                                  if((outsidetemp.currentTemperature >= 0 && outsidetemp.currentTemperature < 10 && humiditysensor.currentHumidity < humidity0to9 - 3) || (thermostat.currentthermostatOperatingState == "heating" && humiditysensor.currentHumidity < humidity0to9 + 3 && outsidetemp.currentTemperature >= 0 && outsidetemp.currentTemperature < 10))
         		                    {
                                     if(thermostat.currentthermostatOperatingState == "idle")
                                       {
                                        thermostat.fanOn()
                                        }
                                     log.debug "fan for 0"
                                     if(fanon.currentContact == "closed") 
        	                           {
                                        humidifier.on()
                                        log.debug "humidifier on 0"
                                        }
                                        else
         								    {
             								 log.debug "no fan 0"
            								 }
                                    }
                                    else 
                                        {
                                         if((outsidetemp.currentTemperature < 0 && humiditysensor.currentHumidity < humiditylow - 3) || (thermostat.currentthermostatOperatingState == "heating" && humiditysensor.currentHumidity < humiditylow + 3 && outsidetemp.currentTemperature < 0))
         		                           {
                                            if(thermostat.currentthermostatOperatingState == "idle")
                                              {
                                               thermostat.fanOn()
                                               }
                                            log.debug "fan for fucking cold"
                                            if(fanon.currentContact == "closed") 
        	                                  {
                                               humidifier.on()
                                               log.debug "humidifier on fucking cold"
                                               }
                                               else
         										   {
             									    log.debug "no fan fucking cold"
           										    }
                                            }     
  										    else 
      									    	{
          									     humidifier.off()
           										 thermostat.fanAuto()
          										 log.debug "humidifier not needed"
         									    }
        							   }
                                 }
                          }
                  }
          }   
   }
   else
   		{
         humidifier.off()
         thermostat.fanAuto()
         log.debug "no heat"
         }
}

 
       

