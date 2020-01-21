Skip to content
Search or jump to…

Pull requests
Issues
Marketplace
Explore
 
@roguetech2 
roguetech2
/
hubitat
0
00
 Code Issues 0 Pull requests 0 Actions Projects 0 Wiki Security Insights Settings
hubitat
/
Master - Time.groovy
 

1
/***********************************************************************************************************************
2
*
3
*  Copyright (C) 2020 roguetech
4
*
5
*  License:
6
*  This program is free software: you can redistribute it and/or modify it under the terms of the GNU
7
*  General Public License as published by the Free Software Foundation, either version 3 of the License, or
8
*  (at your option) any later version.
9
*
10
*  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
11
*  implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
12
*  <http://www.gnu.org/licenses/> for more details.
13
*
14
*  Name: Master - Time
15
*  Source: https://github.com/roguetech2/hubitat/edit/master/Master%20-%20Time.groovy
16
*  Version: 0.3.8
17
*
18
***********************************************************************************************************************/
19
​
20
definition(
21
    name: "Master - Time",
22
    namespace: "master",
23
    author: "roguetech",
24
    description: "Schedules, times and default settings",
25
    parent: "master:Master",
26
    category: "Convenience",
27
    iconUrl: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/mi_face_s.png",
28
    iconX2Url: "https://raw.githubusercontent.com/ClassicGOD/SmartThingsPublic/master/devicetypes/classicgod/xiaomi-magic-cube-controller.src/images/mi_face.png"
29
)
30
​
31
preferences {
32
    
33
        infoIcon = "<img src=\"http://files.softicons.com/download/toolbar-icons/fatcow-hosting-icons-by-fatcow/png/16/information.png\" width=20 height=20>"
34
        errorIcon = "<img src=\"http://files.softicons.com/download/toolbar-icons/fatcow-hosting-icons-by-fatcow/png/16/error.png\" width=20 height=20>"
35
​
36
    page(name: "setup", install: true, uninstall: true) {
37
        section() {
38
            // If all disabled, force reenable
39
            if(disableAll){
40
                input "disableAll", "bool", title: "<b>All schedules are disabled.</b> Reenable?", defaultValue: false, submitOnChange:true
41
                state.disable = true
42
            }
43
​
44
            // if app disabled, display Name and Devices
45
            if(!state.disable && disable){
46
                // display Name
47
                displayNameOption()
48
                // if Name entered, display Devices
49
                if(app.label){
@roguetech2
Commit changes
Commit summary
Update Master - Time.groovy
Optional extended description
Add an optional extended description…
 Commit directly to the master branch.
 Create a new branch for this commit and start a pull request. Learn more about pull requests.
 
© 2020 GitHub, Inc.
Terms
Privacy
Security
Status
Help
Contact GitHub
Pricing
API
Training
Blog
About
