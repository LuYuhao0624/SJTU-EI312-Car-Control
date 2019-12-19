# SJTU-EI312
## Request
1. Implement turn on/off flash light function in CameraPreview in CarClient. See more in Section Details.
2. Think of more fancy ways of control. Anything related to location is not very favorable.
3. Reduce the speed of changing direction. (not very necessary now)
## Details
The intention is that when the ambient is dim, we make the client phone turn on its torch. However, I cannot do that independently because the camera is occupied by the client cameraPreview all the time (it blocks others to use torch). Therefore, I need u to implement a function in CameraPreview to turn on/off the torch. U can see the how I am going to use it in `CarClient.MainActivity.turnOn/OffLight`. U may refer to [this](https://stackoverflow.com/questions/6068803/how-to-turn-on-front-flash-light-programmatically-in-android) on how to turn on the torch.
## To-do
1. Think of some new controls.
2. Absolute control user programming. (seems infeasible due to bad location accuracy)
3. Follow control. (seems infeasible due to bad accuracy)
## Update Log
### Dec. 19
1. Implement Synchronous Control and test with the car. Good.
2. Implement light sensor.
### Dec. 18
1. Change start destination to AUDIO CONTROL and comment parts about STEER CONTROL.
2. Sensor sample frequency reduced.
3. Min API level fixed
4. Abstract `DirectionSensor` in both client and controller. Now the client only send azimuth to controller and leave all process logic in controller. In controller, there is an abstract function which can be implement after being instantialized.
