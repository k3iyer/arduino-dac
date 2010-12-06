/// This file contains all functions relevant to communication with the MCP4822 2dac_channel 12Bit DAC from Microchip

/////////////////////////////////////// WRITE /////////////////////////////////
// this function communicates with and writes to the MCP4822
// It expects a dac_channel number between 0 and 1, as well as an input value in counts full scale that is 0 to 4096.
// If the value is 0 the output power down control bit is selected latching a 500kohm load across the selected output.
// Output gain is selected as 2*Vref by default (that is 4.096V or 1mv / count)


int write_dac(int channel, int dac_channel, int value){

  byte commandbits;
  
  if (value == 0){
    commandbits |= dac_channel<<7;
  }
    else{
      commandbits |= dac_channel<<7;
      commandbits |= B1<<4;
    }
  
commandbits |= value>>8;
   
//   Uncomment the following for debugging
//   Serial.println(commandbits, BIN);
//   Serial.println(value, DEC);
     
  SPCR &= ~(1 << CPHA); // set the SPI configuration to setup up bits for the rising edge of SCK, the MCP4822 requires this..

  EnableSPI();  // turn on the SPI hardware interface, assume control of the pins.
  
    
    digitalWrite(DAC_SELPIN[channel], LOW); //Select adc begin the conversation

    SendRecSPI(commandbits);
    commandbits = 0;
    commandbits |= value;   
    SendRecSPI(commandbits);
    digitalWrite(DAC_SELPIN[channel], HIGH); //turn off device end the conversation

    DisableSPI();  // return to manual control of SPI pins
   
//   Uncomment the following for debugging
//   Serial.println(commandbits, BIN);
 
 // return adc_data; 
}
/////////////////////////////////////////////////////////////////////////////
