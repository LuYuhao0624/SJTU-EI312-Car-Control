# SJTU-EI312
## Update Log
### Dec. 24
1. Finish coding the synchronous control. The car is able to follow the specified trace now.
2. Change the UI into `DrawerLayout` to provide better experience.
3. Add a fragment for basic controls (i.e. forward, stop, left and right).
3. Modify the icons.

### Dec. 23
1. Add user draw control residing in synchronous control. Currently to decoding trace into a control sequence, need to send actual control signal to the car according to the control sequence.
2. Fix bugs in abosulte control.
3. Add a button to turn on/off auto light in CarClient.

### Dec. 20
1. In speech recognition module
	- Fix bugs in speech recognition. **NOTE: before you run the program, please permit microphone usage in your setting page.**
	- Change operation mode: Hold the button to speak, release the button when you finish.
	- Allow user to specify the duration of each action.
2. Fix the inconsistency problem on the caption bar.
3. Support control over flashlight.

### Dec. 19
1. Implement Synchronous Control and test with the car. Good.
2. Implement light sensor.

### Dec. 18
1. Change start destination to AUDIO CONTROL and comment parts about STEER CONTROL.
2. Sensor sample frequency reduced.
3. Min API level fixed.
4. Abstract `DirectionSensor` in both client and controller. Now the client sends only `azimuth` to the controller and leaves all process logic to the latter. In controller, there is an abstract function which can be implemented after instantialization.
