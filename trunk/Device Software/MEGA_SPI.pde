// This file contains all functions relevant to working with the Hardware SPI feature of the MEGA328 micro

//////////////// Hardware SPI configuration////////////
void initialize_SPI(){
pinMode(DAC1_SELPIN, OUTPUT); //CS pin
digitalWrite(DAC1_SELPIN,HIGH); //disable device to start with

pinMode(DAC2_SELPIN, OUTPUT); //CS pin
digitalWrite(DAC2_SELPIN,HIGH); //disable device to start with

pinMode(ADC_SELPIN, OUTPUT); //CS pin
digitalWrite(ADC_SELPIN,HIGH); //disable device to start with

pinMode(PIN_SCK,OUTPUT);       // set up for "manual" SPI directions
digitalWrite(PIN_SCK,LOW);
pinMode(PIN_MOSI,OUTPUT);
digitalWrite(PIN_MOSI,LOW);
 
pinMode(PIN_MISO,INPUT);       // configure inputs
digitalWrite(PIN_MISO,HIGH);
SPCR = B00010100;              // Auto SPI: no int, disabled, LSB first, master, - edge, leading, f/128
SPSR = B00000000;              // not double data rate
}
//////////////////////////////////////////


void EnableSPI(void) {
 SPCR |= 1 << SPE;
}


void DisableSPI(void) {
 SPCR &= ~(1 << SPE);
}


void WaitSPIF(void) {
 while (! (SPSR & (1 << SPIF))) {
//        TogglePin(PIN_HEARTBEAT);       // use these for debugging!
//        TogglePin(PIN_HEARTBEAT);
 continue;
 }
}


byte SendRecSPI(byte Dbyte) {             // send one byte, get another in exchange
 SPDR = Dbyte;
 WaitSPIF();
 return SPDR;                             // SPIF will be cleared
}

