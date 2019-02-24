/**
 *  HVAC Fan Speed Control
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
 */
 
/**
 *	USE THIS APP AT YOUR OWN RISK.  MAJOR DAMAGE CAN OCCUR TO YOUR HVAC SYSTEM.
 *
 *	Most HVAC systems are oversized.  You should not hear the air coming out of your vents.  This is a sign of to much back pressure.
 *		When installing the HVAC they normally tap high speed to A/C and MedHi to heat and call it good.
 *
 *	If you never had it done it is a good idea to clean your A coil (do not remove) and remove and clean your fan.  You will need the wiring diagram off the side of 
 *		the motor to verify what color wire is what speed.  Make sure the fan is centered.  If the A coil or fan is nasty this will reduce air flow and will damage your system.
 *		If your return system is sealed properly and you use good air filters and change them out properly you will not have to clean your sytem every year.  Cleaning the outside A/C sytem now also
 *		before testing.  You do not want build up on the fins or bushes and flowers close to it.  You should be able to walk around it.
 *
 *	You will have to test your system for the min speed for heating and cooling.  When testing lower/raise the thermostat temp so the heating/cooling stays on for 30 min to a hour.
 *		You need to run the system longer then when it runs on the hotest or coldest day.  But run it for atleast 30 min.
 *	Cooling make sure your A coil does not freeze when running at a slower speed.  The temp on the large line leaving the A coil must stay above
 *		freezing leaving your A coil or you can freeze the A coil and it will become a solid block of ice.
 *	Heating make sure you have enough air flow so the thermal overloads do not trip from getting to hot.  If they do you will notice that it 
 *		will blow hot air then cool then hot again.  This will cause them to fail if used to much.
 *	When you first turn the fan from Auto to On it will go to high speed after 1 minute.  After a time you set the fan will slow down to a speed you set when the fan mode is ON.  
 *		This is so during the cooler months you can run the fan constantly to circulate air through the house.  The fan doesn't need to be on high during this time.  
 *		If the heat/ac turns on during this time the fan will speed back up.
 *
 *	Wire the relays in series for the fan speed.  You will need 3 relays.  You want only 1 speed wire to have power at a time when multi relays are on.  With all the relays OFF make this high speed.
 *		This is so if you loose power when it comes back on it will be on Hi speed until SmartThings starts controlling again.  When all the relays are ON make this MedHi speed.
 *		The next relay will be MedLow.  To go into MedLow speed I turn on the MedLow relay then turn off the MedHigh relay. When the MedHigh relay turns off this is when the fan 
 *		actually slows down to the new speed.  The last relay is for Low speed.  You will have to make a jumper on your HVAC board between the cooling and heating taps.  
 *		Then go from here to the MedHi relay.  From the MedHi relay NC contact go to the MedLow relay.  From the MedLow relay NC contact go to the Low relay.  On the Low relay NC
 *		contact connect the High speed wire.  On each relay connect the correct speed wire to each NO contact.
 *
 *	When the HVAC system is idle the fan speed is set to the min for the mode you are in (heat/cool).  When the heat/ac starts 1 minute later the fan will speed up to high.  This is incase you have smart vents.
 *		It gives your smart vents a chance to open before going into high speed and building to much back pressure.
 *
 *	When installing the pressure switch do not connect the low pressure side of the switch to the return. This will give you differental pressure across the fan and A coil 
 *		and will give you a false back pressure reading.
 *
 *
 *
 *	Version 0.0 Still developing and testing. MG
 */
definition(
    name: "HVAC Fan Speed Control",
    namespace: "mebaddog2002",
    author: "Michael Goldsberry",
    description: "This app will change the speed of the HVAC fan to reduce back pressure when using smart vents.  It does not control the vents.  It will also lower the fan speed when the fan is in the ON position after a set time.  It is designed to be used with a 4 speed fan.  You will need a pressure switch in the duct work for max back pressure.  You will have to test your system to determine min speed for heating and cooling. Read notes in code before installing.",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


		// TODO: put inputs here
preferences {
	section("Title") {
		paragraph "This app will change the speed of the HVAC fan to reduce back pressure when using smart vents.  It does not control the vents.  It will also lower the fan speed when the fan is in the ON position after a set time. You will have to test your system to determine min speed for heating and cooling. Read notes in code before installing."
	}
    section("Thermostat") {
       input "thermostat", "capability.thermostat", required: true, title: "Select Thermostat"
       input "delayfanon", "number", required: true, title: "Delay before fan slows when fan mode is ON (in minutes)", defaultValue:60
    }
    section("Fan Relays") {
    	input "medhirelay", "capability.switch", required: true, title: "MedHi Relay"
        input "medlowrelay", "capability.switch", required: true, title: "MedLow Relay"
        input "lowrelay", "capability.switch", required: true, title: "Low Relay"
    }
    section("Back Pressure Sensor") {
    	input "backpressure", "capability.contactSensor", required: true, title: "Back Pressure Sensor"
    }
    section("Min Fan Speeds") {
    	input "heatmin", "enum", title: "Heat Min Speed", options: ["Hi", "MedHi", "MedLow"], required: true, defaultValue:"MedHi"
        input "coolmin", "enum", title: "Cooling Min Speed", options: ["Hi", "MedHi", "MedLow"], required: true, defaultValue:"MedHi"
        input "fanmin", "enum", title: "Fan On Slow Speed", options: ["Hi", "MedHi", "MedLow", "Low"], required: true, defaultValue:"Low"
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
    
    subscribe(thermostat, "thermostatFanMode", mainControlHandler)    
    subscribe(thermostat, "thermostatMode", mainControlHandler)    
    subscribe(thermostat, "thermostatOperatingState", mainControlHandler)
    subscribe(backpressure, "contact", mainControlHandler)
    
    log.debug "Initialized"        
}

// TODO: implement event handlers

def mainControlHandler(evt) {
	log.debug "mainControlHandler called: $evt"
    
log.debug "TFan ${thermostat.currentthermostatFanMode}"
log.debug "TMode ${thermostat.currentthermostatMode}"
log.debug "TState ${thermostat.currentthermostatOperatingState}"
log.debug "MedHi Relay ${medhirelay.currentswitchState}"
log.debug "MedLow Relay ${medlowrelay.currentSwitchState}"
log.debug "Low Relay ${lowrelay.currentSwitchState}"
log.debug "Back Pressure ${backpressure.currentContact}"
    
    def fanmode = thermostat.currentthermostatFanMode
    def tmode = thermostat.currentthermostatMode
    def tstate = thermostat.currentthermostatOperatingState
    
    if(tmode == "heat" && tstate == "idle" && fanmode == "fanAuto")
      {
      if(heatmin == "Hi")
        {
        medhirelay.off()
        medlowrelay.off()
        lowrelay.off()
        log.debug "Heat speed set to Hi at idle"
        }
      if(heatmin == "MedHi")
        {
        medhirelay.on()
        medlowrelay.off()
        lowrelay.off()
        log.debug "Heat speed set to MedHi at idle"
        }
      if(heatmin == "MedLow")
        {
        medhirelay.off()
        medlowrelay.on()
        lowrelay.off()
        log.debug "Heat speed set to MedLow at idle"
        }        
      }
    if(tmode == "cool" && tstate == "idle" && fanmode == "fanAuto")
      {
      if(coolmin == "Hi")
        {
        medhirelay.off()
        medlowrelay.off()
        lowrelay.off()
        log.debug "Cool speed set to Hi at idle"
        }
      if(coolmin == "MedHi")
        {
        medhirelay.on()
        medlowrelay.off()
        lowrelay.off()
        log.debug "Cool speed set to MedHi at idle"
        }
      if(coolmin == "MedLow")
        {
        medhirelay.off()
        medlowrelay.on()
        lowrelay.off()
        log.debug "Cool speed set to MedLow at idle"
        }        
      }      
}    