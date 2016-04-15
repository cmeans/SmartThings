/**
 *  ComEd RRTP Current Hour Average Monitor
 *  Author: chris.a.means@gmail.com
 *  Date: 2016-02-07
 *
 *  v1.0 Initial implementation
 *  v1.1 Changed to an Energy Meter, and other code clean up.
 *  v1.1.1 Added additional tile color values to inhibit interpolation.
 *  v1.2 Improved scheduling.
 *  v1.3 Fixed variable/event name/value.
 *
 * INSTALLATION
 * =========================================
 * 1) Create a new device type from code (https://graph.api.smartthings.com/ide/devices)
 *      Copy and paste the below, save, publish "For Me"
 *
 * 2) Create a new device (https://graph.api.smartthings.com/device/list)
 *     Name: Your Choice
 *     Device Network Id: Your Choice
 *     Type: ComEd RRTP Current Average Hourly Rate Monitor (should be the last option)
 *     Location: Choose the correct location
 *     Hub/Group: Leave blank
 *
 * Copyright (C) 2016 Chris Means <chris.a.means@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions: The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 **/
 
/**
  * Static values
  */
private COMED_CURRENTHOURAVERAGE_URL()		{ "https://hourlypricing.comed.com/api?type=currenthouraverage" }

metadata 
{
	definition (name: "ComEd RRTP Current Hour Average Monitor", namespace: "cmeans", author: "Chris Means") 
    {
		capability "Energy Meter"
        capability "Refresh"
        capability "Polling"

		command "refresh"
		command "poll"
		command "setCurrentHourAverage", ["number"]
	}

	// UI tile definitions
    tiles( scale: 2 ) 
	{
		valueTile("currentHourAverage", "device.energy", width: 6, height: 6, canChangeIcon: true, canChangeBackground: true) 
        {
			state("currentHourAverage", label:'${currentValue}¢ / kWh', unit: "¢ / kWh",
				backgroundColors:[
					[value: 0, color: "#ffffff"],
					[value: 1, color: "#0000ff"],
					[value: 2, color: "#0000ff"],
					[value: 3, color: "#0000ff"],
					[value: 4, color: "#0000ff"],
					[value: 5, color: "#00ff00"],
					[value: 6, color: "#00ff00"],
					[value: 7, color: "#00ff00"],
					[value: 8, color: "#ffff00"],
					[value: 9, color: "#ffff00"],
					[value: 10, color: "#ffff00"],
					[value: 11, color: "#ff0000"],
					[value: 1000, color: "#ff0000"]
				]
			)
		}
        
   		standardTile("refresh", "device.refresh", inactiveLabel: false, width: 2, height: 2, decoration: "flat")
		{
			state(
            	"default", 
                label: 'refresh', 
                //action: "polling.poll", 
                action: "refresh",
                icon: "st.secondary.refresh-icon" )
		}

   		standardTile("version", "device.versoin", inactiveLabel: true, width: 2, height: 2, decoration: "flat")
		{
			state(
            	"default", 
                label: textVersion())
		}

		main "currentHourAverage"
		details(["currentHourAverage", "refresh", "version"])
   }
}

// Parse incoming device messages to generate events
def parse(String description) 
{
	def pair = description.split(":")
    
	createEvent(name: pair[0].trim(), value: pair[1].trim(), unit: "¢ / kWh")
}

def setCurrentHourAverage(value) 
{
	sendEvent(name: "energy", value: value, unit: "¢ / kWh")
}

def getRate() 
{
	def params = [
    	uri: COMED_CURRENTHOURAVERAGE_URL(),
        contentType: 'application/json',
        requestContentType: 'application/json'
	]
    
	try 
    {
    	httpGet(params) { resp ->
            log.debug "Rate is ${resp.data[0].price}¢ / kWh"
            
            // Return price.
            resp.data[0].price
	    }
	} 
    catch (e) 
    {
    	log.error "something went wrong: $e"
	}
}

/**
 * handle commands
 */
def poll() 
{
	//log.debug "poll"
    
    do_update()
}

def refresh() 
{
	//log.debug "refresh"
    
	do_update()
}

def reschedule()
{
	runIn(300, do_update)
}

def do_update()
{
	setCurrentHourAverage(getRate())

	reschedule()
}

private def textVersion() 
{
	"v1.3.1"
}

private def textCopyright() 
{
	"Copyright © 2016 Chris Means <chris.a.means@gmail.com>"
}
