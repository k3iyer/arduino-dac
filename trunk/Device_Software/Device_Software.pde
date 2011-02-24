//Tyler Grandahl
//08/25/2010
//Battery cycler R0.0.9, adding support for the BC communication protocol to host software.

//Battery cycler R0.0.8, adding in external watchdog functionality, shift register for extra outputs, charge enable for system faults, bang bang fan control, I2C load/charge bank temp sensor integration. Updated all pin locations to reflect planned system, added scaling of cell and chamber temps, fixed display code errors.

//Battery cycler R0.0.7, adding LCD output, push button input, and menu functionality.

//Battery cycler R0.0.6, completed output processor, as well as output_dac function.

//Battery cycler R0.0.5, dramatically improved the organization of the main loop as changed channel enables to an array, added the profile processor, and step up functions,
//the system now roughly cycles through a profile! Controling output and reacting to feedback is next...

//battery cycler R0.0.4, adds a much cleaner averaging and data sampling functions that that use a single 2D and 3D array for storing adc sample and average data, also allowing 
//for a single function to work with both channels. Added Scaling and calibration math to the new averaging function

//battery cycler R0.0.3, adds functional step_updating function, for pulling profile data from FLASH into SRAM and modifying it as desired.
//battyer cycler R0.0.2, adds FLASH storage, lots of pains experimenting with 3D arrays, eventually discovered it was because they were too big and poor arduino was running out of SRAM
//battery cycler R0.0.1, contains basic code to communicate with ADC and DAC, as well as sample data into an array once every 100mS and broadcast the averaged values every 1S.
//
#include <EEPROM.h>

#include <Wire.h> // the wire library for I2C interface for the power board temp sensors.

#include <Flash.h> // include the flash library so we dont have to deal with all the PROGMEM crap

#include "profiles.h" // contains the definitions for all the profiles that are to be stored in FLASH

#include "TimerOne.h" // timer library contains the functions for initializing a period for timer 1 and configuring an ISR with it.

#include <LiquidCrystal.h>
/* The circuit:
 * LCD RS pin to digital pin 9
 * LCD Enable pin to digital pin 10
 * LCD D4 pin to digital pin 5
 * LCD D5 pin to digital pin 6
 * LCD D6 pin to digital pin 7
 * LCD D7 pin to digital pin 8
 initialize the library with the numbers of the interface pins
*/
LiquidCrystal lcd(9, 10, 5, 6, 7, 8);


//pin definitions
#define device_ID        0001          // the unique device ID that helps identify the unit like a MAC addr.

#define upbtn_pin        2             // Pin number the up pushbutton is attached to
#define selbtn_pin       3             // pin number for the select push button
#define dwnbtn_pin       4             // pin number for the down push button



#define DAC1_SELPIN      6              // DAC1 CS pin
#define DAC2_SELPIN      7//note should be 7              // DAC2 CS pin
#define ADC_SELPIN       5             // ADC CS pin
#define shift_latch      17             // Shift register latch pin, adruino analog pin 3 being used as GPIO
#define PIN_SCK          13             // SPI clock (also Arduino LED!)
#define PIN_MISO         12             // SPI data input
#define PIN_MOSI         11             // SPI data output


#define pin_ch1_voltage      0              // ch# on the tlc3544 0
#define pin_ch1_current      2              // 2
#define pin_ch2_voltage      1              // 1
#define pin_ch2_current      3              // 3

#define pin_ch1_cell_temp    0              // ch# in arduino for the megas internal ADC
#define pin_ch2_cell_temp    1              //
#define pin_chamber_temp     2              //

//pin definitions

#define ch0_load_temp       72              // I2C address of the temp sensor on the ch0 load bank.
#define ch0_charge_temp     72              // I2C address of the temp sensor on the ch0 charge bank.
#define ch1_load_temp       72              // I2C address of the temp sensor on the ch1 load bank.
#define ch1_charge_temp     72              // I2C address of the temp sensor on the ch1 charge bank.


// VARDECS...

//word readvalue;
//double calcvalue;
//word adc_data[500]; // array for storing sampled ADC data at 1kHz

int DAC_SELPIN[2] = {DAC1_SELPIN, DAC2_SELPIN};

int incomingByte = 0;	// for incoming serial data


int adc_sample_count[2] = {0}; // kinda obvious, the count used to keep track of the current sample in the arrays of adc_data, counts from 0 - 9. 
int input_data[2][7] = {0}; // stores the averaged samples of data collected from the ADC before broadcasting, also stores cumulative energy in value 4(in mWHrs).  { mV, mA, cell temp, chamber temp, cum energy, load temp, charge temp}
int adc_samples[2][10][4] ={0}; // array of 10 rows of adc data for holding adc samples for averaging for both channels.


long step_time[2]; // counts 0.1S increments for triggering step time limits, gets reset for every new profile.
int step_number[2]; // keeps track of the step number we are on for a particular profile, gets incremented as we hit limit conditions.

int current_profile[2];  // Stores the current profile number to be running on each channel // Note: convert to unsigned char? (for numbers 0 to 255) 

int current_step[2][4] = {0}; //  formatted [channel number][{Mode,Output,Limt1,Limt2}], holding arrays of the profile type for holding scaled or unscalled data in RAM, {current, voltage. cell_temp, chamber_temp, cum energy}

int system_output[2] = {0}; // stores the currently desired output current values, will hold the calculated value from the output_processor for constant voltage and power modes.  POSITIVE VALUES ARE LOAD, NEGATIVE VALUES ARE CHARGE

int system_status[2] = {0}; // stores the system status of each channel, 0 is idle, 1 is active, 2+ is a fault, the vaule of the system status is also the fault code...

int test_mode_en[2] = {0}; // the enable bits for test mode on each channel.
// single bit triggers...
// NOTE: try changing these to boolean to save RAM

int second_timer = 0;
int trigger_flag = 0; // is used to trigger the loop every 100mS
int channel_enable[2] ={0}; // selected when a profile is to be running on a channel, will enable all relevant functions in the loop.

int set_temp = 0; // the set temp for the thermal chamber

byte shift = 0; // the holding register that will get pushed to the 595 at the end of every loop. B7{ WD HEARTBEAT | FAULT | Charge Enable | -unused- | MS CH1 | MS CH0 | FAN | TRIAC}B0

int setup_variables[] = {60, 5, 26, 23, 8, 3, 0}; // an array to hold the variables that are configurable through the seyup menu.. varuables are in the following order, 'disp time out', 'disp delay time', 'fan on temp','fan off temp','PID kP','PID kI','PID kD'..
// NOTE: move into eeprom -> EEPROM.write(address, value) address are same as index for setup_variables
//////////////////////////////////////////////////////////////////////////////

      void setup()
      {
      Timer1.initialize(100000); // set timer 1 for a 100mS period
      
      Timer1.attachInterrupt(trigger); // attach the trigger
        
      Serial.begin(115200); // Setup the serial coms thorugh the USART and FTDI chip
       
      lcd.begin(16, 2); // set up the LCD's number of rows and columns:   
        
      initialize_SPI(); // configure the hardware SPI controller
      
      initalize_ADC(); // Initalize the TLC3544 ADC
      
      Wire.begin();        // join i2c bus for communications with the temp sensors on the power board.
      
      analogReference(INTERNAL); // set the atmgas ADC reference to the internal 1.1V Ref for the LM35 readings. 
      
      pinMode(upbtn_pin, INPUT);
      digitalWrite(upbtn_pin, HIGH);  // set pullup on analog pin 2
      pinMode(selbtn_pin, INPUT);
      digitalWrite(selbtn_pin, HIGH);  // set pullup on analog pin 1
      pinMode(dwnbtn_pin, INPUT);
      digitalWrite(dwnbtn_pin, HIGH);  // set pullup on analog pin 0
      
      pinMode(shift_latch, OUTPUT);    // setup 595 latch on the analog pin 3
      digitalWrite(shift_latch, LOW);  

      read_setup_vars(); //update the working array of setup variables from what was stored in the EEPROM
      }
       
       
       
void loop() 
{
if (trigger_flag == 1)
  {
  trigger_flag = 0; // reset the trigger flag once we have begun the main loop;

  if (second_timer == 9)
    {
      second_timer = 0;
    }
  else
    {
      second_timer ++;
    }
  
  //// TIMING CRITICAL FUNCTIONS ////
  for(int channel=0; channel<2; channel++)  // always update so our display is accurate
    {
      sample(channel); // pass it the channel number to sample // Samples the ADC channels and writes the raw data into the sampling array.
      average(channel); // average the samples in the array now that all values have been updated // NOTE: could be in the slower 1Sec loop?
    }
  
  //// TIMING CRITICAL FUNCTIONS FOR ENABLED CHANNELS////
  // such as sample the ch1 ADC ch1 channels and update the array.
  for(int channel=0; channel<2; channel++)
    {
      if (channel_enable[channel] == 1) 
        {
          step_time[channel] ++; // Increments the step time for that channel, this is used to trigger time limits for a profile step.
          profile_processor(channel); 
          output_processor(channel);
        }
    }
    
  //// TIMING CRITICAL FUNCTIONS ////    
  output_dac(0);
  output_dac(1);
//        digitalWrite(shift_latch, LOW);  
  bank_temps(); // update the temps of the load and charge banks.
  
  
  //// NON-TIMING CRITICAL FUNCTIONS FOR ENABLED CHANNELS////
  
  for(int channel=0; channel<2; channel++)
    {
      if (channel_enable[channel] == 1)
        {
        }
    }
  

   //// NON-TIMING CRITICAL FUNCTIONS////
    fan_controller();
    serial_rx();
    display_updater();
    
    shift ^=B10000000; // toggle the hertbeat output every loop
//    shift_register_update();  // update the Shift register...
    
    status_monitor(); // update the system status for the next loop, also checks for faults

  
  //// ONE SECOND EXECUTION ////
  if (second_timer == 0) // if ch1 is enabled then when the current sample is 0 (this will happen once a secondafter the tenth sample has been taken) average the array, update wH counter, SOC meter, broadcast the results.
    {
      
//      for (int i=0; i<2; i++) // check if test mode is enabled on any of the channels, if so then run the test generator beofore sending heartbeat
//      {
//        if(test_mode_en[i])
//        {
//          test_generator(i); // update test data
//        }
//      }
      
      tx_heartbeat(0); // make heartbeat transmissions
      tx_heartbeat(1);
      
      
    //// ONE SECOND EXECUTION FOR ENABLED CHANNELS ONLY ////
      for(int channel=0; channel<2; channel++)
        {
        if (channel_enable[channel] == 1) // then sample the ch1 ADC ch1 channels and update the array.
          {
            
            // NOTE: currently disabled as it is not needed, was only being used for debug, functionality replaced by serial heartbeat
            
//           print_array(channel); // broadcast the calculated values             
          }
        }
    }              

  ///////////////////////////////////////////////////////////////////////
  }// end of the main function  

}// end of the loop function


 
void trigger() // sets the trigger flag true every 100mS
{
  trigger_flag = 1;
}


 void system_disable(int channel)
 {
  channel_enable[channel]=0; // diabel the specified chanel 
 }

 void system_enable(int channel)
 {
   channel_enable[channel]=1; // enable the specified channel
 }  
 
 
 
 void counter_reset(int channel)
 {
  step_time[channel] = 0;
 }






void shift_register_update()
{
      shiftOut(PIN_MOSI, PIN_SCK, MSBFIRST, shift);  // shit out the holding register
      digitalWrite(shift_latch, HIGH);
      digitalWrite(shift_latch, LOW);
}


void bank_temps()  // updates the temps of the load and charge bank mosfets from the I2C sensors.
{
  Wire.requestFrom(ch0_load_temp, 1);    // request 1 bytes from the ch0 load bank sensor
  if(Wire.available())    // slave may send less than requested
  { 
    input_data[0][5] = Wire.receive(); // receive a byte as character
  }
  Wire.requestFrom(ch0_charge_temp, 1);    // request 1 bytes from the ch0 load bank sensor
  if(Wire.available())    // slave may send less than requested
  { 
    input_data[0][6] = Wire.receive(); // receive a byte as character
  }
  Wire.requestFrom(ch1_load_temp, 1);    // request 1 bytes from the ch0 load bank sensor
  if(Wire.available())    // slave may send less than requested
  { 
    input_data[1][5] = Wire.receive(); // receive a byte as character
  }
  Wire.requestFrom(ch1_charge_temp, 1);    // request 1 bytes from the ch0 load bank sensor
  if(Wire.available())    // slave may send less than requested
  { 
    input_data[1][6] = Wire.receive(); // receive a byte as character
  }
}

void fan_controller()  // watches the load bank temperature and toggles the fan enable bit as needed.
{
  if (input_data[0][5] >= setup_variables[2] | input_data[0][6] >= setup_variables[2] | input_data[1][5] >= setup_variables[2] | input_data[1][6] >= setup_variables[2])
  {
    shift |= B00000010; // turn the fan ebable bit on //B7{ WD HEARTBEAT | FAULT/KICK SAFETY | SET SAFETY | -unused- | MS CH1 | MS CH0 | FAN | TRIAC}B0
  }
  else if(input_data[0][5] <= setup_variables[3] | input_data[0][6] <= setup_variables[3] | input_data[1][5] <= setup_variables[3] | input_data[1][6] <= setup_variables[3])
  {
    shift &= B11111101; // turn the fan off..
  }
}

void status_monitor() // monitors the system for activity and faults and updates the systems current status accordingly.
{
  for (int i=0; i<2; i++)
  {
    if (channel_enable[i] == 0)
    {
      system_status[i]=0; // if the channel is not enabled it is idle
    }
    else if (channel_enable[i] == 1)
    {
      system_status[i]=1; // the channel is enabled and thus active
    }
  }
}
