This is an integrated set of apps, with scheduling being at the core. When devices are automated within this suite, scheduling will always apply. For instance, if a light is scheduled (from within the suite) to dim over time, and it is turned on by a Pico or contact sensor, the schedule will resume. However, if the light is dimmed by Pico, it is considered a "manual" change, and overrides the schedule. In addition, Picos (etc.) have an option (in beta status) to "resume" a schedule, in addition to the normal "on" and "off".

Each child app (as beta: schedule, Pico, contact, humidity, and MagicCube) have the same look and feel, and with simple intuitive "wizard" type UI, using help tips, warnings, and errors.

Master.groovy:
	Parent app, containing shared functions and basic settings such as presense device selection.

	Production status: Beta but required.
------------------------------------------------
Master - Alert:
	Child app for sending text messages and push alerts.

	Production status: Experimental. Do not install in any production enviroment.
------------------------------------------------
Master - Contact.groovy:
	Child app for door and window sensors.

	Production status: Beta.
------------------------------------------------
Master - Humidity.groovy:
	Child app for bathroom humidity sensors.

	Production status: Beta (untested).
------------------------------------------------
Master - MagicCube.groovy:
	Child app for MagicCubes.

	Production status: Late alpha. Only works with kkossev's T1 driver.
------------------------------------------------
Master - Pico.groovy:
	Child app for Caseta Picos. (For other switches, must be mapped as 5 buttons, such that "off" = button 5.)

	Production status: Late beta.
------------------------------------------------
Master - Presense.groovy:
	Child app for home presense.

	Production status: Experimental. Do not install in any production enviroment.
------------------------------------------------
Master - Time.groovy:
	Child app for complex scheduling.

	Production state: Beta.
------------------------------------------------
Master - Washer-Dryer.groovy:
	Child app for washer/dryer alerts

	Production state: Experimental. Do not install in any production enviroment.
------------------------------------------------
schedule.drawio:
	Flowchart of logic for schedule app using draw.io, available at https://www.diagrams.net/

	Production state: Abandoned.
