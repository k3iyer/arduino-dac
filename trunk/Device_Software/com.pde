// Tyler Grandahl
// 12/11/2010
// Battery cycler, com.pde
// This file contains the functions for monitoring for serial data, processing the incomming data, and producing heartbeat and ACK serial communications.


#define frame_size 12 // 12 byte fixed rx frame size
#define ser_rx_buff_size 16  // 16 byte rx serial buffer

#define ser_tx_buff_size 32 // 32 byte tx buffer

int ser_rx_buff[ser_rx_buff_size] = {0};
int rx_buff_used = 0;  // tracks how full the buffer is and where the next byte should go when filling it

int ser_tx_buff[ser_tx_buff_size] = {0};


void serial_rx()
{   
    // read in serial data from the uart fifo
      while((Serial.available() >= 1)&&(rx_buff_used < ser_rx_buff_size))
      {
        ser_rx_buff[rx_buff_used] = Serial.read();
        rx_buff_used ++;
      }
    
//    // print out the contentse of the serial buffer for my debuggings
//    for (int i=0; i< ser_rx_buff_size; i++)
//    {
//      Serial.println(ser_buff[i]);
//    }    
//    Serial.println("");

    
    if (rx_buff_used >= frame_size) // could have a full message check the checksum
    {  
      // if there is enough data then check the checksum for the first byte, if there is not enough data then wait one loop.
      int temp = 0;
      
      for (int i=0; i < frame_size - 1; i++)
      {
        temp = temp + ser_rx_buff[i];   
      }
      temp=temp%256; // calculate the remainder that should be the checksum
      
      
      // If the checksum is correct process the message
      if (temp == ser_rx_buff[frame_size - 1])
      {     
        //Process incomming serial message
        switch (ser_rx_buff[0] & 0x0F) {
          case 1: // basic request
            if (ser_rx_buff[1] < 128) // the command can be run regardless of system status
            {
              switch (ser_rx_buff[1]) {
                  case 0: // stop
                    system_disable(ser_rx_buff[0] & 0xF0); // disable the system on a specified channel as a 'stop' action
//                    Serial.print("Stop command recieved");
                    break;
                  case 1: // start
                    system_enable(ser_rx_buff[0] & 0xF0); // enable the system on specified channel as a 'start' action
//                    Serial.print("Start command recieved");
                    break;
  //                case 2: // zeros out the cumulative energy counters
  //
  //                  break;
  //                default: 
  //                  // if nothing else matches, do the default
                }
            }
            else if ((system_status[ser_rx_buff[0] & 0xF0]) == 0) // channel must be idle to run commands 128 and above
            {
              switch (ser_rx_buff[1]){
                  case 128: // toggles test mode
                  Serial.print("test mode toggled");
                  if (test_mode_en[ser_rx_buff[0] & 0xF0]==0) // if test mode is currently diabled, then toggle it to enabled
                  {
                    Serial.print("test mode enabled");
                    test_mode_en[ser_rx_buff[0] & 0xF0] = 1;
                    channel_enable[ser_rx_buff[0] & 0xF0]=1; // mark system as active to run full heartbear
                    
                    //Zero out all variables that will be swept
                    
                    current_profile[ser_rx_buff[0] & 0xF0] = 0;
                    step_number[ser_rx_buff[0] & 0xF0] = 0;
                    step_time[ser_rx_buff[0] & 0xF0] = 0;
                    for(int i=0; i<4; i++)
                    {
                      input_data[ser_rx_buff[0] & 0xF0][i] =0;
                    }
                  }
                  
                  else{ // assume that test mode was enabled and toggle it to disabled.
                    Serial.print("test mode disabled");
                    test_mode_en[ser_rx_buff[0] & 0xF0] = 0;
                    channel_enable[ser_rx_buff[0] & 0xF0]=0; // set system back to idle
                  }
  
                    break;
//                  default: 
//                    // if nothing else matches, do the default
                }
            }
            break;
          case 2: // profile request
            current_profile[ser_rx_buff[0] & 0xF0] = ser_rx_buff[1]; // sets the correst profile to be run
            counter_reset(ser_rx_buff[0] & 0xF0); // resets the step clock on that channel
            system_enable(ser_rx_buff[0] & 0xF0); // enables the channel
            break;
//          case 3: // variable update
//            break;
//          case 4: // user profile update
//            break;
//          default: 
            // if nothing else matches, do the default
            // default is optional
        }
        
        // BEGIN ACK TRANSMISSION
        ser_tx_buff[0] = 11; // send out a header byte with 11 to indicate this is an ACK message
        ser_tx_buff[1] = ser_rx_buff[frame_size - 1]; // send back the checksum being acknowledged
        serial_tx(2); // request the first two bytes be sent
        // END OF TRANSMISSION
        
        shift_buff(frame_size); // end of processing for successful message, can shift the buffer up now.
      }
      else
      {
//        Serial.println("Checksum error!");
//        Serial.println(temp);
        shift_buff(1);
      }
    }
}


void shift_buff(int num) // function that shifts up the serial working buffer a number of bytes sent as an argument
{
  for (int i=0; i < ser_rx_buff_size - num; i++)
  {
    ser_rx_buff[i] = ser_rx_buff[i+num];
  }
  rx_buff_used = (rx_buff_used - num);
}


void serial_tx(int tx_length) // transmitts the serial tx buffer, calculates and attaches a checksum.
{
  int checksum = 0; // temp var for doing the checksum calc
  for (int i=0; i< tx_length; i++)
  {
    checksum = checksum + ser_tx_buff[i]; // calculate the sum of the transmission
  }
  checksum=checksum%256; // calculate the remainder to get the checksum
  for (int i=0; i<tx_length; i++) // transmit all requested data from the tx buffer
  {
    Serial.write(ser_tx_buff[i]);
  }
  Serial.write(checksum); // transmit the checksum to finish the transmission  
}


// notes for making the heartbeat.
// int input_data[2][7] = {0}; // stores the averaged samples of data collected from the ADC before broadcasting, also stores cumulative energy in value 4(in mWHrs).  { mV, mA, cell temp, chamber temp, cum energy, load temp, charge temp}
// int current_profile[2];  // Stores the current profile number to be running on each channel // Note: convert to unsigned char? (for numbers 0 to 255) 
// int step_number[2]; // keeps track of the step number we are on for a particular profile, gets incremented as we hit limit conditions.
// int system_status[2] = {0}; // stores the system status of each channel, 0 is idle, 1 is active, 2+ is a fault, the vaule of the system status is also the fault code...
// long step_time[2]; // counts 0.1S increments for triggering step time limits, gets reset for every new profile.

void tx_heartbeat(int channel) // will transmit a heartbeat message for the channel specified in the argument 0 or 1.
{
    switch (system_status[channel]) {
    case 0:  // channel is idle
      ser_tx_buff[0]=((channel << 4)+(0x8));       // header byte, channel shifted left 4 and then anded with an 0x8
      // Cell temp, chamber temp, upper and then lower
      ser_tx_buff[1]=(input_data[channel][2] >> 4); // cell temp upper nibble
      ser_tx_buff[2]=(input_data[channel][2] & 0x0F); // cell temp lower nibble
      ser_tx_buff[3]=(input_data[channel][3] >> 4); // cell temp upper nibble
      ser_tx_buff[4]=(input_data[channel][3] & 0x0F); // cell temp lower nibble
      serial_tx(5); // make the transmission
      
      break;
    case 1:  // channel is active
      ser_tx_buff[0]=((channel << 4)+(0x9));       // header byte, channel shifted left 4 and then anded with an 0x8
      ser_tx_buff[1]=(current_profile[channel]); // current profile lowe nibble, should only be an 1 byte var
      ser_tx_buff[2]=(step_number[channel]); // the current step number
      ser_tx_buff[3]=(step_time[channel] >> 24); // the step time counter broken up into 8 bit nibbles
      ser_tx_buff[4]=(step_time[channel] >> 16);
      ser_tx_buff[5]=(step_time[channel] >> 8);
      ser_tx_buff[6]=(step_time[channel]);
      for(int i=0; i<4; i++) // tx all the current input data except for load and charge bank temps.
      {
        ser_tx_buff[7+i]=(input_data[channel][i] >> 4); //upper nibble
        ser_tx_buff[8+i]=(input_data[channel][i] & 0x0F); //lower nibble
      }
      serial_tx(17); // make the transmission
      break;
    default: // the default state is the fault state, the ID of the system fault is also the fault code
      ser_tx_buff[0]=((channel << 4)+(0x8));       // header byte, channel shifted left 4 and then anded with an 0x8
      ser_tx_buff[1]=(system_status[channel]);  // the fault code is also the system status
      serial_tx(2); // make the transmission
  }
}



// NOTE, would it be wise to diable the output processor when test mode is enabled? certainly we need to at least make sure both channels are disabled.
// perhaps Test mode should not be included in the final product as it could cause a mess if accediently enabled and implementing safetys to prevent that is not worth the effort
// when would it be used anyways?

void test_generator(int channel) // creates the ramping signal that can be read in output heartbeats when test mode is enabled.
{
  if (test_mode_en[channel] >1){
    if (current_profile[ser_rx_buff[0] & 0xF0] <= 0)
    {
      test_mode_en[channel] =1;
    }
    current_profile[ser_rx_buff[0] & 0xF0] --;
    step_number[ser_rx_buff[0] & 0xF0] --;
    step_time[ser_rx_buff[0] & 0xF0] --;
    for(int i=0; i<4; i++)
    {
      input_data[ser_rx_buff[0] & 0xF0][i] --;
    }
  }
  else{
    if (current_profile[ser_rx_buff[0] & 0xF0] >=255)
    {
      test_mode_en[channel] =2;
    }
    current_profile[ser_rx_buff[0] & 0xF0] ++;
    step_number[ser_rx_buff[0] & 0xF0] ++;
    step_time[ser_rx_buff[0] & 0xF0] ++;
    for(int i=0; i<4; i++)
    {
      input_data[ser_rx_buff[0] & 0xF0][i] ++;
    }
  }
}
