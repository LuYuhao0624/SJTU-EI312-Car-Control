# SJTU-EI312
## Requests
1. Implement `CarController.TraceDrawer.sendControlSignal` invoked in `TraceDrawer.onTouchEvent`. Remember to deal with the boolean `occupied` used in `SynchronousFragment`. See more in Section Details.
2. Time permitting, add button control back to one of the fragment using toggle like that in `SynchronousFragment`. See toggle implementation idea in Section Details.

## Attention
In case you do not want to read the details, you must be aware of this.
1. Azimuth used in both CarClient and CarController are in range [-180, 180] degrees.
2. **GREAT ATTENTION**. The azimuth-direction map is strange as follows. You will see when you draw it. Therefore, you should treat whether increasing azimuth is turning left or right. 
	- EAST 0
	- WEST (-)180
	- NORTH -90
	- SOUTH 90


## Details

1. Send control sequence in `ArrayList<ControlSignalPair> TraceDrawer.control_sequence`. You may see the decode logic in the debug function `TraceDrawer.printControlSequence`. Controls in the sequence conceptually appears in pair, first a turning control, then a timed forward control.
   	- `ControlSignalPair` is defined in `TraceDrawer.java`. It has three public attributes:
   	   1. `int signal` corresponds to the signal should be send through the bluetooth (0 stop, 1 forward, 2 left, 3 right). Set to 0 when the car do not need to turn in a turning control.
   	   2. `float duration` corresponds to the real forward time in seconds. You can change the the unit time by changing `TraceDrawer.UNIT_DISTANCE`. The real forward time is calculated by the multiplying length in the draw (e.g. 1, 1.414, 2.236) with `UNIT_DISTANCE`. When it is a turning control, `duration` is set -1.
   	   3. `int degree` corresponds to the degree the car should turn. When it is a forward control, `degree` is set -1.
   	- You need to access the client azimuth in your function. You may add some interfaces in `TraceDrawer` to allow `SynchronousFragment` passing client azimuth in.
2. Toggle details
	- Put all views related to two controls into two different `ArrayList<View>` respectively, e.g. I initialize `sync_widgets` and `draw_widgets` in `SynchronousFragment.initializeSyncWidgets(View root)` and `SynchronousFragment.initializeDrawWidgets(View root)` and set items in `draw_widgets` invisible first.
	- When press toggle, just set visible views to be invisible and vice versa.
	- Invisible views are not functional, i.e. buttons cannot be pressed.


## Update Log
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
