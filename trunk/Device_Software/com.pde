// Tyler Grandahl
// 12/11/2010
// Battery cycler, com.pde
// This file contains the functions for monitoring for serial data, processing the incomming data, and producing heartbeat serial communications.
int test_mode_en = 0; // enables the test mode when set to 1, will cause the system to output a constant ramp data, of one byte frames each loop, looping from 0 to 256 back to 0.
int test_count = 0;
int test_dir = 0;

int start_byte = 0;

#define max_transmission 2 // the highest number of transmission that is currently supported by the system
const int trans_length[]={3,5,3}; // the length of bytes that should follow the start byte during transmissions from the PC.
int waiting = 0; // a flag that is raised when the serial monitor is waiting a loop for more data to make a full transmission. We need this because we cannot guarantuee all data will be there when we first check.


void serial_monitor()
{
  //Check if there is a waiting flag for serial data.
  //The waiting flag tells us that we had some data last time but it was not enough to make a full transmission for that start byte
  //
  
  if (waiting == 0){
    if (Serial.available() > 0){
      start_byte = Serial.read();
      Serial.println("byte read");
    }
    // check and see if we have enough data for a full trans, if we dont then toggle the flag.
    // We toggle the flag because if it is 1 then a loop has already passed and if enough data isnt here its a bad start_byte
    // if it is 0 then we may just need some more time for the uart to fill the FIFO so lets wait a loop.
    if ((start_byte & 0x0F) <= max_transmission){
      if (Serial.available() < trans_length[(start_byte & 0x0F)-1]){
        Serial.println("waiting is and will change from");
        Serial.println(waiting);
        if(waiting == 1){
          waiting = 0;
        }
        else{
          waiting = 1;
        }
      }
      else{
        Serial.println("complete set of data");
        // process the data, check the CRC and see if its a legit transmission.
        Serial.flush();
      }
    }
    
  
  
  // check for incomming serial data

//   if (Serial.available() > 0) 
//     {
//        // read the incoming byte:
//        int uart = Serial.read(); 
//    
//        switch (uart & 0xF) {
//          case 0:
//          test_mode_en = 0;
//            //Basic Request
////            Serial.println("Basic Request");
////            switch(uart & 0x0F){
////              case 0:
////                // a stop request
////                Serial.println("case 0!");
////                test_mode_en = 0;
////                break;
////              case 5:
////                // a test mode enable request
////                Serial.println("case 5!");
////                test_mode_en = 1;
////                break;
////              default:
////              Serial.println("unsupported basic request");
////            }
//            break;
//          case 1:
//            //Variable update transmission
//            Serial.println("variable update request");
//            break;
//          case 2:
//            //Profile request
//            Serial.println("profile request");
//            break;
//          case 5:
//            Serial.println("send into test mode");
//            test_mode_en = 1;
//            break;
//          default: 
//            // if nothing else matches, it must not be supported for a Host PC -> Arduino command
//            Serial.println("unsupported request");
//        }

             
//        //    //            int channel = 0;
//        //    //            
//        //    //            switch (request) 
//        //    //              {
//        //    //              case 49: //1 in ASCII
//        //    //              
//        //    //              ////NOTE: it may be usefull to compile the following instructoins into a function for starting a new profile, will have to see how the serial monitor develops out....
//        //    //              
//        //    //              // if we recieve an instruction to start a new profile, enable the channel and copy the profile in that channels array in RAM
//        //    //              //profile_updater(channel, profile);
//        //    //              current_profile[channel] = 64;
//        //    //              counter_reset(channel);
//        //    //              step_update(channel,current_profile[channel],step_number[channel]); // request that the current step data be updates with the specificed (channel#, profile#, step#).
//        //    //              copy_check(channel); //print the updated step so I can watch
//        //    //              system_enable(channel);
//        //    //              
//        //    //              //NOTE: also need to set the channel system status to BUSY!!!
//        //    //              
//        //    //              break;
//        //    //                
//        //    //              case 50: //2 in ASCII
//        //    //              channel_enable[0] = 0;
//        //    //              channel_enable[1] = 0;
//        //    //                break;
//        //    //              case 51: //3 in ASCII
//        //    //              channel_enable[0] = 1;
//        //    //                break;
//        //    //              case 52: //4 in ASCII
//        //    //              channel_enable[1] = 1;
//        //    //                break;
//        //    //              }
     }
     
     if(test_mode_en == 1){
       if (test_count >= 255){
         test_dir = 0;
       }
       if (test_count <= 0){
         test_dir = 1;
       }
       if (test_dir == 0){
         test_count --;
       }
       else{
         test_count ++;
       }
            //broadcast a hertbeat
     Serial.write(test_count);
     }

}
