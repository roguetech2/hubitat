This is an integrated set of apps, with scheduling being at the core. Schedules are daily, with day of the week and dates, supporting progressive changes (dimming, hue, saturation and color temperature).

When devices are automated within this suite, scheduling will always apply. For instance, if a light is scheduled (from within the suite) to dim over time, and it is turned on by using a Pico (or sensor), the schedule will resume and the light will be at the brightness from the schedule. For instance, if you want a light to get progressively dim in the evening, the schedule can control what brightness the light would be at, if /when it is turned on.

Color temperature (aka white color), hue, and saturation can also be scheduled.

Changing brightness (etc.) manually will suspend the schedule, only to resume if the device is turned off and back on, or if commanded to resume. This applies to events from other apps, devices, etc.

Button controllers (etc.) can themselves be scheduled (allowing a button controller to do something different in the morning as at night). Button controllers can be mapped to control multiple devices in different ways (within a single setup), such as turning on one light and toggling another (and the settings of the lights in turn depending on their own scheduling, allowing for complex layering depending on time, day, Mode, and how the device is activated).

Settings are also available for days of the week, individual dates, dates ranges, Mode, people present, and people not present.

All the apps have the same look and feel with intuitive, dynamic "wizard" type UI, using help tips, warnings, and errors, designed to provide flexibility and power with a simple but adaptive interface. They also provide plain-language summaries of settings to more easily maintain and correct automations.

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
Master - Presence.groovy:
	Child app for home presence.

	Production status: Abandoned, possibly to be rebuilt (does not function)
------------------------------------------------
Master - Time.groovy:
	Child app for complex scheduling.

	Production state: Beta.
