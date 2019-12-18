# SJTU-EI312
## Request
1. Reduce the speed of changing direction.
## To-do
1. Synchronous control
2. Absolute control user programming
3. Follow control
## Update Log
### Dec. 18
1. Change start destination to AUDIO CONTROL and comment parts about STEER CONTROL.
2. Sensor sample frequency reduced.
3. Min API level fixed
4. Abstract `DirectionSensor` in both client and controller. Now the client only send azimuth to controller and leave all process logic in controller. In controller, there is an abstract function which can be implement after being instantialized.
