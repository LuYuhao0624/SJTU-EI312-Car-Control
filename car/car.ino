#include <SoftwareSerial.h>

#define STOP 0
#define STRAIGHT 1
#define LEFT 2
#define RIGHT 3

#define MOTOR1A P2_5
#define MOTOR1B P2_4
#define MOTOR2A P2_1
#define MOTOR2B P2_2

SoftwareSerial bluetooth(P1_1, P1_2); // RX, TX
byte cmd;
int targetSpeedL = 0;
int targetSpeedR = 0;
int speedL = 0;
int speedR = 0;

void setup(){
	// Open bluetooth port
  delay(500);
	bluetooth.begin(9600);
	
	// Set up output pins
	pinMode(GREEN_LED, OUTPUT);
	pinMode(RED_LED, OUTPUT);
	digitalWrite(GREEN_LED, LOW);
	digitalWrite(RED_LED, LOW);
	pinMode(MOTOR1A, OUTPUT);
	pinMode(MOTOR1B, OUTPUT);
	pinMode(MOTOR2A, OUTPUT);
	pinMode(MOTOR2B, OUTPUT);
	analogWrite(MOTOR1A, 0);
	analogWrite(MOTOR1B, 0);
	analogWrite(MOTOR2A, 0);
	analogWrite(MOTOR2B, 0);
}

void updateSpeed(){
  if(speedL == targetSpeedL && speedR == targetSpeedR){
    delay(10);
    return;
  }
  
  if(speedL < targetSpeedL){
    speedL++;
    if(speedL > 120 && speedL < 150)
      delay(3);
  }else if(speedL > targetSpeedL){
    speedL = targetSpeedL;
  }

  if(speedR < targetSpeedR){
    speedR++;
    if(speedR > 120 && speedR < 150)
      delay(3);
  }else if(speedR > targetSpeedR){
    speedR = targetSpeedR;
  }
	analogWrite(MOTOR1A, speedL);
	analogWrite(MOTOR2A, speedR);
  delay(1);
}

void loop(){
	if(bluetooth.available()){
		cmd = bluetooth.read();
		
		switch(cmd){
			case STOP:
				targetSpeedL = targetSpeedR = 0;
				break;
			case STRAIGHT:
				targetSpeedL = 255;
				targetSpeedR = 240;
				break;
			case LEFT:
				targetSpeedL = 0;
				targetSpeedR = 220;
				break;
			case RIGHT:
				targetSpeedL = 200;
				targetSpeedR = 0;
		}
	}
  updateSpeed();
}
