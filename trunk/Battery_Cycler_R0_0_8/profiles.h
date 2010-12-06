/*
  Profiles.h - the declaration of all of the profiles for the battery cycler

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
    



