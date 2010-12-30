/*
  Profiles.h - the declaration of all of the profiles for the battery cycler

Note Storing profiles on the battery cycler:
Each step of a profile requires the storage of intergers, thus taking up 8Bytes of space in the chips flash.
As the goal is to keep the total program size for the system under 15kBytes or only half the flash, 15kBytes is left for profile data.
15kBytes translates into 1875 profile lines. Assuming most profiles store less than 25 lines this will allow for 75 profiles to be stored in flash.

The EEPROM on the Atmega328 is 1024 bytes and can store 128 profile lines or around 5+ user defined profiles. This can allow for the addition of user
profiles without having to reflash the devices.

More flash or eeprom can be added and communicated with via SPI at relitavley low cost, additionally the storage of profiles on SD cards would be relativley easy to add,
would give the user a way to add profiles onto the card outside of the BC system, IE put it in a computer and copy the text files onto the card, or also swap the sd cards between hardware etc.

  */
  
  


FLASH_TABLE(int, charge_1, 4, // profile #54, a relative charging profile, the output is interpreted as C-rate, limits are interprets as percentages of vmin, vmax, and C-rate. Calculated from

// mode, output, limit_1, limit_2

    {1,500,3600,0}, // output mode = constant current charging... // output current in mA .. this needs to be relative // limit high cell voltage in mV .. this needs to be relative // time limit is 0 meaning it is ignored
    {4,4200,200,7200}, 
    {0,0,0,0}); // all zero conditions means the profile is done and is to be terminated
    
    



FLASH_TABLE(int, charge_2, 4, // profile #129, an absolute charging profile with defined voltages (mV) and currents (mA)

// mode, output, limit_1, limit_2

    {1,500,3600,0}, // output mode = constant current charging... // output current in mA .. this needs to be relative // limit high cell voltage in mV .. this needs to be relative // time limit is 0 meaning it is ignored
    {4,4200,200,7200}, 
    {0,0,0,0}); // all zero conditions means the profile is done and is to be terminated
    
    
    
FLASH_TABLE(int, charge_3, 4, // profile #130, an absolute test profile for hardware and firmware development

// mode, output, limit_1, limit_2

    {0,500,3600,10}, // output mode = constant current charging... // output current in mA .. this needs to be relative // limit high cell voltage in mV .. this needs to be relative // time limit is 0 meaning it is ignored
    {0,1,4200,5},
    {0,1000,4200,3},
    {0,200,4200,15},
    {0,0,4200,5},
    {0,1000,4200,5}, 
    {0,0,0,0}); // all zero conditions means the profile is done and is to be terminated
    



