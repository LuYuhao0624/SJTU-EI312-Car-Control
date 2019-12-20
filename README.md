# SJTU-EI312
## Requests
1. Think of more fancy ways of control. Anything related to location is not very favorable.
2. Reduce the speed of changing direction. (not very necessary now)

## To-do
1. Think of some new controls.
2. Absolute control user programming. (seems infeasible due to bad location accuracy)
3. Follow control. (seems infeasible due to bad accuracy)
4. Search for new ways of measuring distance.

## Update Log
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
