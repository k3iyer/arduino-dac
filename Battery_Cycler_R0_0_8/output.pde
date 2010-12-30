// Tyler Grandahl
// 08/03/2010
// Battery cycler, data_acq.pde
// This file contains the functions processing profile data, and controling output DA data for different modes.


/// The output_dac function makes the connection between the desired output for a channel as decided by the output processor and the DAC itself.
/// It should run every 100mS loop as the output processor does, and it should run regardless of a channel enable.
/// It checks to see if a channel is enabled and if not it zeros it output. This is important for when a fault trips the channel enables off, we sill need to be able to zero out the DAC,s

/////NOTE: THIS STILL NEEDS SOME SORT OF OUTPUT SCALING (mA to DAC counts...)

void output_dac(int channel)
{
  if (channel_enable[channel] == 1)
  {
      if (system_output[channel] >= 0)
      {
        write_dac(channel, 0, system_output[channel]);/// DAC output ch0 is connected to the load bank...
        write_dac(channel, 1, 0);/// make sure the charge bank is off.
      }
      else
      {
        write_dac(channel, 0, 0);/// make sure the load bank is off. 
        write_dac(channel, 1, system_output[channel]);/// DAC output ch1 is connected to the charge bank...
      }
  }
  else
  {
        write_dac(channel, 0, 0);/// make sure the load bank is off. 
        write_dac(channel, 1, 0);/// make sure the charge bank is off.
  }
  
}



void output_processor(int channel)
{
  
  if (current_step[channel][0] >= 0 && current_step[channel][0]<=3) // check the current mode to see if it is in the constant current range.
    {
      system_output[channel]=current_step[channel][1]; // set the output current to be the output defined in the profile in mA
    }
    
  if (current_step[channel][0] >= 4 && current_step[channel][0]<=5) // check the current mode to see if it is in the constant voltage range.
    {
      if(input_data[channel][1] < current_step[channel][1]) // if the cell voltage is low decrease the current // Note this could put the system into a charging state (negative current value)
      {
        system_output[channel] --;
      }
      if(input_data[channel][1] > current_step[channel][1]) // if the cell voltage is high inclease the load to drop the voltage.
      {
        system_output[channel] ++;
      }
    }
    
  if (current_step[channel][0] >= 6 && current_step[channel][0]<=8) // check the current mode to see if it is in the constant power range.
    {
      system_output[channel] = current_step[channel][1]/input_data[channel][1]; // calculate the needed current as I=P/V
    }
  
}


void profile_processor(int channel)
{
 
  // look at the current channel counter and limit conditions, if a limit is met, update the row counter, check for terminating condition, if none update the new output values and mode.
 switch(current_step[channel][0])
   {
   case 0:
     if((step_time[channel]/10) >= current_step[channel][3]) // check the limit var2 that stores the time limit.
       {
         step_up(channel);
       }
   break;
   case 1:
   /// mode 1 ... 2... 3... 4.... fill in the blanks... yay!
   
   break;
   case 2:
   
   
   break;
   case 3:
   
   
   break;
   case 4:
   
   break;
   case 5:
   
   
   break;
   case 6:
   
   
   break;
   case 7:
   
   
   break;
   case 8:
   
   break;
   }
 
 

  
}


void step_up(int channel)
{
 step_number[channel]++;
 step_time[channel] = 0;

 step_update(channel,current_profile[channel],step_number[channel]); // request that the current step data be updates with the specificed (channel#, profile#, step#).
 
 copy_check(channel); //print the updated step so I can watch
 
 system_output[channel] = 0; // reset the system output for a fresh count if we are going to be in constant vontage mode and need to integrate error., not really worth putting this in a conditional statement.
 
 if(current_step[channel][0] ==0 && current_step[channel][1] ==0 && current_step[channel][2] ==0 && current_step[channel][3] ==0)
 {
   channel_enable[channel]=0;
   step_time[channel]=0;
   step_number[channel]=0;
   
   Serial.print("channel ");
   Serial.print(channel);
   Serial.print(" terminated");
   
   /// NOTE: The channel system status should also be set back from busy to idle...
   
 }

}





// step update provides a index of all the FLASH stored profiles for calling them from a variable
// It copies the current step, from the selected profile, into the local SRAM.
// once in the local ram it can be scaled with BSF or for HPPC current.

//NOTE: If we move to a 3D FLASH array this fucntion can become a lot simpler and nicer and such, however initiliazing the 3D array might also be a bitch and I dont feel like doing that right now.

void step_update(int channel, int profile, int step_number)
{
  
  switch(profile)
  {
    case 64:
      for(int i=0; i<4; i++)
      {
      current_step[channel][i] = charge_3[step_number][i];
      }
    break;
  }
  
  // NOTE:
  // Insert a if condition here to check the range of profile # and determine if the data needs to be scaled with BSF or HPPC values
  
  //Uncomment this to print the data and make sure it is being copied ok.
// copy_check(channel); //print the updated step so I can watch
  
  
}





/// The copy check is a function used for code debugging to make sure that array data is being coped to where its supposed to be.
void copy_check(int channel)
{
  
   for (int i=0; i<4; i++)
    { 
      Serial.print(" copyied  ");
      Serial.print(current_step[channel][i]);
    } 
    Serial.println();
}



