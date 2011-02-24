// This file contains all functions for communication and control of the TLC3544 4Channel 14Bit ADC from TI



//////////////// Initialize the  TLC3544 ADC///////////////////////////////////////
/// For initilization we must first write A000h (Write CFR + 000h) into the device
void initalize_ADC(void) {
  digitalWrite(ADC_SELPIN,LOW); //Select adc begin conversation
  byte commandbits = B10100000;   // Write A0h
  shiftOut(PIN_MOSI, PIN_SCK, MSBFIRST, commandbits);  // shift out the first byte
  commandbits = B00000000;   // Write 00h
  shiftOut(PIN_MOSI, PIN_SCK, MSBFIRST, commandbits);   // shift out the second byte
  digitalWrite(ADC_SELPIN,HIGH); //de-select adc ending first 16bit convo
///// Now that we have "zeroed" out the registers we can write our real init data as Axxxh (write CFR + xxxh) into the device
  digitalWrite(ADC_SELPIN,LOW); //Select adc starting the second 16bit convo
  commandbits = B10101000;   // Write Axh
  shiftOut(PIN_MOSI, PIN_SCK, MSBFIRST, commandbits);  // shift out the first byte
  commandbits = B00000000;   // Write xxh
  shiftOut(PIN_MOSI, PIN_SCK, MSBFIRST, commandbits);   // shift out the second byte
  digitalWrite(ADC_SELPIN,HIGH); //de-select adc ending second 16bit convo 
} 



/////////////////////////////////////// SAMPLE /////////////////////////////////
// this function communicates with and samples the TLC3544 ADC
// It expects a channel number between 0 and 3, as well as a sample count between 0 and the size of the array.

int read_adc(int adc_channel){
  
  int sample = 0;
  
  conversation(adc_channel);
  sample = conversation(adc_channel);
  
  return sample;
}


int conversation(int adc_channel){

  byte commandbits;
  
  switch (adc_channel) {
  case 0:
    commandbits |= B0000<<4;
    break;
  case 1:
    commandbits |= B0001<<4;
    break;
  case 2:
    commandbits |= B0010<<4;
    break;
  case 3:
    commandbits |= B0011<<4;
    break;
  }
  
  SPCR |= 1 << CPHA; // set the SPI configuration to set up bits for the falling edge of SCK, the TLC3544 requires this.
  
  EnableSPI();  // turn on the SPI hardware interface, assume control of the pins.
    
    digitalWrite(ADC_SELPIN,LOW); //Select adc begin the conversation
// because the data comes in MSB first as two bytes and the data is 14bits, the two LSB's of the second byte are not used.
// Thus when shifting the data into the array we only shift over the first byte six bits and shift the second byte to the right two bits to chop off the two unused LSB's.
    int sample = SendRecSPI(commandbits)<<6; 
    sample |= SendRecSPI(commandbits)>>2;
// We initiate the hardware SPI again four times for another 32 SCLK ticks to complete the long sampling
// because the TLC3544 ignores all recieved bits after the first four command bits of the first bytes, we do not have to zero out the command bits variable, it does not matter what it is.
    SendRecSPI(commandbits);
    SendRecSPI(commandbits);
    SendRecSPI(commandbits);
    SendRecSPI(commandbits);
  
    digitalWrite(ADC_SELPIN, HIGH); //turn off device end the conversation
  
//  // NOTE: I dont think we will ever need this delay, our software will provide enough delay, enable this if there are issues.
//  //this gives us a small delay for the conversion and overhead time the ADC needs.
//  for (int i=130; i>=0; i--)
//  {
//  __asm__("nop\n\t");
//  }

    DisableSPI();  // return to manual control of SPI pins

 return sample; 
}
/////////////////////////////////////////////////////////////////////////////
