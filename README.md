This is an integrated set of apps, with scheduling being at the core. Schedules are daily, with day of the week and dates, supporting progressive changes (dimming, hue, saturation and color temperature).

When devices are automated within this suite, scheduling will always apply. For instance, if a light is scheduled (from within the suite) to dim over time, and it is turned on by a button controller or by way of a sensor, the schedule will resume and the light turn on at the brightness from the schedule. However, if the light is dimmed by button controller (etc.), it is considered a "manual" change, and overrides the schedule. It will also capture events from other apps, devices, etc., and apply scheduled settings. In addition, bnutton controllers (etc.) have an option (in beta status) to "resume" a schedule, in addition to the normal "on" and "off", which allows scheduled settings to resume after a "manual" change.

In addition, button controllers (etc.) can themselves be scheduled (allowing a button controller to do something different in the morning as at night), as well as (early beta status) what people are present/away, and/or what the Hubitat Mode is. Button controllers can be mapped to control multiple devices in different ways (within a single setup), such as turning one light on and toggling another (and the settings of the lights in turn depending on their own, potentially independant, scheduling).  

Each child app (currently beta: schedule, sesnsrs, Pico, and MagicCube - with Pico and MagicCube to combined into Button Controller) have the same look and feel with intuitive, dynamic "wizard" type UI, using help tips, warnings, and errors, designed to provide many features while maintaining a simple interface. They also provide plain-language summaries of settings to more easily maintain and correct automations.

Basic to do: Bring all apps out of beta; combine Pico and MagicCube into a generalized Button Controller; add motion detection app.

Included files:

Master.groovy:
	Parent app, containing shared functions and basic settings such as presense device selection.

	Production status: Beta but required.
------------------------------------------------
Master - Alert.groovy:
	Child app for sending text messages and push alerts.

	Production status: Abandonded (does not function)
------------------------------------------------
Master - Sensor.groovy:
	Child app for sensors.

	Production status: Early Beta.
------------------------------------------------
Master - MagicCube.groovy:
	Child app for (Xiaomi) MagicCubes.

	Production status: Late alpha. Only works with kkossev's T1 driver.
------------------------------------------------
Master - Pico.groovy:
	Child app for (Lutron) Caseta Picos. (Should work with any 2, 4 or 5 button devices.)

	Production status: Late beta.
------------------------------------------------
Master - Presense.groovy:
	Child app for home presense.

	Production status: Abandoned, possibly to be rebuilt (does not function)
------------------------------------------------
Master - Time.groovy:
	Child app for complex scheduling.

	Production state: Beta.
