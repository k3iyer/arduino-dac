/// This file contains all functions relevant to communication with the MCP4822 2dac_channel 12Bit DAC from Microchip

/////////////////////////////////////// WRITE /////////////////////////////////
// this function communicates with and writes to the MCP4822
// It expects channel number and output of channel (A or B), as well as an input value in counts full scale that is 0 to 4096.
// If the value is 0 the output power down control bit is selected latching a 500kohm load across the selected output.
// Output gain is selected as 2*Vref by default (that is 4.096V or 1mv / count)


int write_dac(int channel, int A_value, int B_value){

//  A_value=2500;
//  B_value=1500;
  Serial.println("write");  
  Serial.println(system_output[0]);
      Serial.println(A_value);
        Serial.println(channel);
          Serial.println(B_value);

  
  byte commandbits=0;
  
  if (A_value == 0){
    commandbits |= 0<<7;
  }
    else{
      commandbits |= 0<<7;
      commandbits |= B1<<4;
    }
  
commandbits |= A_value>>8;
   
//   Uncomment the following for debugging
//   Serial.println(commandbits, BIN);
//   Serial.println(A_value, DEC);
     
  SPCR &= ~(1 << CPHA); // set the SPI configuration to setup up bits for the rising edge of SCK, the MCP4822 requires this..

  EnableSPI();  // turn on the SPI hardware interface, assume control of the pins.
  
    
    digitalWrite(DAC_SELPIN[channel], LOW); //Select adc begin the conversation

    SendRecSPI(commandbits);
    commandbits = 0;
    commandbits |= A_value;   
    SendRecSPI(commandbits);
    digitalWrite(DAC_SELPIN[channel], HIGH); //turn off device end the conversation

    DisableSPI();  // return to manual control of SPI pins
   

//////////////////////////////// OUTPUT B /////////////////////////////////////////////////////////////

  commandbits=0;
  
  if (B_value == 0){
    commandbits |= 1<<7;
  }
    else{
      commandbits |= 1<<7;
      commandbits |= B1<<4;
    }
  
commandbits |= B_value>>8;
   
//   Uncomment the following for debugging
//   Serial.println(commandbits, BIN);
//   Serial.println(B_value, DEC);
     
  SPCR &= ~(1 << CPHA); // set the SPI configuration to setup up bits for the rising edge of SCK, the MCP4822 requires this..

  EnableSPI();  // turn on the SPI hardware interface, assume control of the pins.
  
    
    digitalWrite(DAC_SELPIN[channel], LOW); //Select adc begin the conversation

    SendRecSPI(commandbits);
    commandbits = 0;
    commandbits |= B_value;   
    SendRecSPI(commandbits);
    digitalWrite(DAC_SELPIN[channel], HIGH); //turn off device end the conversation

    DisableSPI();  // return to manual control of SPI pins






//   Uncomment the following for debugging
//   Serial.println(commandbits, BIN);
 
 // return adc_data; 
}
/////////////////////////////////////////////////////////////////////////////
