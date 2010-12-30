#include <TimerOne.h>

#define ArdID 0x03
#define IDLE 0
byte mode;
unsigned int prevTime;
boolean enabled=false;

unsigned int profileStep=0;
volatile boolean trigger = false;
  void setup(){
     mode=IDLE;
     enabled=true;
     Serial.begin(115200);
     Timer1.initialize(50000);  //50ms
     Timer1.attachInterrupt(wakeUp50MS);
     prevTime=millis();
  }

 void wakeUp50MS(){
   static int x = 0;
   x++;
   //once every 100ms
   if (x>=10){
     trigger=true;
     x=0;
   }
 }
 
 static void sendStroke(byte batID){
    int tDif   =  millis()-prevTime;
    byte tDif1 = (tDif & 0xFF00)>>8;
    byte tDif2 =  tDif & 0x00FF;
    prevTime   =  millis();
    
    //init packet
    Serial.write(0xA1);            //1
    Serial.write(ArdID);           //2
    Serial.write(batID);           //3
    Serial.write(mode);            //4
    Serial.write(0x01|(batID<<4)); //5
    Serial.write(0x02|(batID<<4)); //6
    Serial.write(0x03|(batID<<4)); //7
    Serial.write(0x04|(batID<<4)); //8
    Serial.write(0x05|(batID<<4)); //9
    Serial.write(0x06|(batID<<4)); //10
    Serial.write(tDif1);           //11
    Serial.write(tDif2);           //12
   // Serial.write(0xFF);
   sendEndTransmit();
 }
  static void sendEndTransmit(){
    
    Serial.write(0xAA);
    Serial.write(0xBB);
    Serial.write(0xCC);
    Serial.write(0xDD);
  }
 /*
  static int getIncData(){
     if(Serial.available()>=3){
        unsigned byte data1=Serial.read();
        unsigned byte data2=Serial.read();
        unsigned byte data3=Serial.read();
        if (data1==0x01){
          processEvent1(data2, data3);
        }
        
     } 
    
  }
  static void processEvent1(unsigned byte data1, unsigned byte data2){
    mode=data1;
    
  }
  */
  void loop(){
    while(true){ 
      if(trigger){
        trigger=false;
        if(enabled){
          sendStroke(0);
          sendStroke(1);
        }
         if(Serial.available()>=1){
             if( Serial.read() == 0xAC){
                  enabled = !enabled;
             }
         }
    
    
        //blink LED for the hell of it
        digitalWrite(13, !digitalRead(13));
    
      }
    }
}
