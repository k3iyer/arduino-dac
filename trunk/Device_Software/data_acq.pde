// Tyler Grandahl
// 08/03/2010
// Battery cycler, data_acq.pde
// This file contains the functions for acquiring data, processing the data, and broadcasting the results.

// The averaging function calculates the average of the 10 samples of voltage, current, and temps, stored in the adc_samples array.
// It then scales the data and writes it to the input_data array to be read by other functions and broadcasted.


void average(int channel)
{  
  int current_offset[2] = {8192,8192}; // NOTE: // These are calibration values, depending on how we want to do calibration they may need to become global, may also need to add cal coeff.
  int voltage_offset[2] = {0,0};
  
  long accumulator = 0; 
  for (int i = 0; i < 4; i++)
   {
    for (int ii = 0; ii < 10; ii++)
      {
        accumulator = accumulator + adc_samples[channel][ii][i];
      }
    input_data[channel][i] = accumulator / 10;
    accumulator = 0;
   } 

// Prints the averaged raw count for voltage and current, uncomment to use for calibration and debugging.

//Serial.println(input_data[channel][0]);
//Serial.println(input_data[channel][1]);


///// Scaling inputs
///// TLC3544 uses a 4.096V ref and is 14bit (16384 counts) giving 4 counts per mV so it is scaled by 1/4 for an output of 1mV/count.
///// The current shunt used to measure channel current is 0.005ohm, the current shunt IC has a set gain of 20, so 1mA would result in 0.1mV and read 2V full scale (20A) allowing for bidirectional reading within the 4.096V refrence
///// So each raw count of the TLC3544 is 2.5mA so if we scale this data by 2.5 for an output of mA.

//NOTE: to add scaling for the LM35Z with a voltage divider and precision arduino refrence. TBD
// aslo should the output valiables be changed to floats? this would increase our reporting resolution a bit, not sure if we have the extra loop time at the moment....

//Voltage (mV)
input_data[channel][0] = (input_data[channel][0] - voltage_offset[channel]) / 4;


//Current (mA)
input_data[channel][1] = (input_data[channel][1] - current_offset[channel]);// * 2.5; //Has an offset of -8192 because of the 2.048V ref for 0 // to scale first mult by 2.5
//input_data[channel][1] = accumulator;

//                Serial.print("avg CH ");
//    Serial.println(channel);
//    Serial.println(input_data[channel][0]); // read the voltage and request current
//        Serial.println(input_data[channel][1]); // request current again and display the current from last request.


//Temperatures
// the sampled data from the internal ADC is still as counts from the 1.1V internal reference. The LM35 outputs 10mV / degC.
// the atmegas ADC is 10bit so it has a full count of 1024. with a 1.1V ref thats 930.9 counts / volt. 
// So if we want 0.1 degC resolution or mV we must divide 0.93. if we want resolution of 1 degC then we we divide by 9.3.
// NOTE: we are currently using a temp resolution of 0.1 degC however we may want to scale back as this invoves doing additional math in the display routine.
input_data[channel][2] = input_data[channel][2] / 0.93;// cell temp

input_data[channel][3] = input_data[channel][3] / 0.93;// chamber temp
//Calculate the cumulative energy this update and add it to the count. in units of mWHrs. (int can store +/- 32WHrs). This will also be used to calculate the SOC.

}  



// Samples the ADC channels and writes the raw data into the sampling array

void sample(int channel)
{
  
    //note that because the read data will be from the commanded channel of the last conversion, 
    //the pins for current and voltage are flipped.. There may be a more elegant way of doing this... 
    //Or the adc_read could just use two conversations and take twice as long...
  
    adc_samples[channel][adc_sample_count[channel]][1] = read_adc(pin_ch1_current); // read current and request current again...
    adc_samples[channel][adc_sample_count[channel]][0] = read_adc(pin_ch1_voltage); // read voltage and request current


    
//                    Serial.print("avg CH ");
//    Serial.println(channel);
//    Serial.println(adc_samples[channel][adc_sample_count[channel]][0]); // read the voltage and request current
//        Serial.println(adc_samples[channel][adc_sample_count[channel]][1]); // request current again and display the current from last request.
    
    adc_samples[channel][adc_sample_count[channel]][2] = analogRead(pin_ch1_cell_temp);
    adc_samples[channel][adc_sample_count[channel]][3] = analogRead(pin_chamber_temp);

  // if we have acculumated 10 samples (array index 9) then reset the sample count to 0,
  // otherwise increment the sample count for the next time around.
  if (adc_sample_count[channel] >= 9) 
  {
    adc_sample_count[channel] = 0;
  }
  else 
  {
    adc_sample_count[channel] ++;
  }
  
}



// Broadcasts the newest set of calculated data
// NOTE:
// is there a neater way to format these long strings?
void print_array(int channel)
{
// uncomment the following for verbose sample data, to verify the functionality of the averaging function.

//  for (int i = 0; i < 10; i++)
//  { Serial.print("ch");
//    Serial.print(channel);
//    Serial.print("_sample#");
//    Serial.print(i);
//    Serial.print(",");
//    Serial.print(adc_samples[channel][i][0]);
//    Serial.print(",");
//    Serial.print(adc_samples[channel][i][1]);
//    Serial.print(",");
//    Serial.print(adc_samples[channel][i][2]);
//    Serial.print(",");
//    Serial.print(adc_samples[channel][i][3]);
//    Serial.println();
//  }

    Serial.print("ch");
    Serial.print(channel);
    Serial.print("_avg ");
    Serial.print(input_data[channel][0]);
    Serial.print(",");
    Serial.print(input_data[channel][1]);
    Serial.print(",");
    //Serial.print(input_data[channel][2]);
    //Serial.print(",");
    //Serial.print(input_data[channel][3]);
    Serial.println();
}

