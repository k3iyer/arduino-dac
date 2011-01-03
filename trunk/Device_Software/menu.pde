// Tyler Grandahl
// 08/25/2010
// Battery cycler, menu.pde
// This file contains the functions for LCD output, push button input, and menu functionality.


/// NOTES: This menu system can use some improvement, several operations could be done more effeciently / cleanly using pointers.
/// MISSING FEATURES: The menu system is currently missing the implementation of timers to reset the menu_state to 0 when a button time-out occurs.
/// Missing FEATURES: It is also missing a timer to increment the current menu_state on a set interval by raising the up button flag.
/// MIDDING FEATURES: At some level the fan set temp that is created here needs to be processed....
/// MISSING FEATURES: Currently setup variables are being stored in volatile memory, they need to be updated and stored in non-volatile memory!!!

#include <avr/pgmspace.h>


prog_char status_string_0[] PROGMEM = "CH1: ";   // "String 0" etc are strings to store - change to suit.
prog_char status_string_1[] PROGMEM = "CH2: ";
prog_char status_string_2[] PROGMEM = "ACTIVE #";
prog_char status_string_3[] PROGMEM = "   IDLE ";
prog_char status_string_4[] PROGMEM = "FAULT  #";
prog_char status_string_5[] PROGMEM = "  V=";
prog_char status_string_6[] PROGMEM = "I=";
prog_char status_string_7[] PROGMEM = "W=";
prog_char status_string_8[] PROGMEM = "  SOC=";
prog_char status_string_9[] PROGMEM = " Cell Temp=";
prog_char status_string_10[] PROGMEM = " Load=";
prog_char status_string_11[] PROGMEM = "    Charge=";
prog_char status_string_12[] PROGMEM = "Chmbr  Set=";
prog_char status_string_13[] PROGMEM = "      Meas=";
prog_char status_string_14[] PROGMEM = "C";
prog_char status_string_15[] PROGMEM = "%";



prog_char setup_string_0[] PROGMEM = "<<            >>";
prog_char setup_string_1[] PROGMEM = "--";
prog_char setup_string_2[] PROGMEM = "++";
prog_char setup_string_3[] PROGMEM = "SW Rev 0.0.7"; // try to keep me up to date!!!! or find some better way to define me! 
prog_char setup_string_4[] PROGMEM = "EXIT Setup";
prog_char setup_string_5[] PROGMEM = "Disp Time Out";
prog_char setup_string_6[] PROGMEM = "Disp Delay Time";
prog_char setup_string_7[] PROGMEM = "Fan On Temp";
prog_char setup_string_8[] PROGMEM = "Fan Off Temp";
prog_char setup_string_9[] PROGMEM = "PID kP";
prog_char setup_string_10[] PROGMEM = "PID kI";
prog_char setup_string_11[] PROGMEM = "PID kD";



// Then set up a table to refer to your strings.

PROGMEM const char *setup_string_table[] = 	   // change "string_table" name to suit
{   
  setup_string_0,
  setup_string_1,
  setup_string_2,
  setup_string_3,
  setup_string_4,
  setup_string_5,
  setup_string_6,
  setup_string_7,
  setup_string_8,
  setup_string_9,
  setup_string_10,
  setup_string_11};



PROGMEM const char *status_string_table[] = 	   // change "string_table" name to suit
{ 
  status_string_0,
  status_string_1,
  status_string_2,
  status_string_3,
  status_string_4,
  status_string_5,
  status_string_6,
  status_string_7,
  status_string_8,
  status_string_9,
  status_string_10,
  status_string_11,
  status_string_12,
  status_string_13,
  status_string_14,
  status_string_15};


//// Variable decs



word menu_state = 0; // he variable menu_state keeps track of the menus current state from 0-255.
boolean menu_enable;
typedef struct
{
  boolean mem; // marks that a button has been held for more than one loop, do not raise select again
  boolean hold; // marks that a button has been held down before and is probably debounced.
  boolean select; // flags that a button has been pressed and can be processed by other code and lowered.
} button;

button sel; // this variable is used to debounce and flag push button inputs.
button up;
button dn;

word menu_timeout = 30; // the number of seconds the menu system waits at a selected state before returning to state 0; // Note this feature should be disabled if '0' is selected.


// the variable menu_state keeps track of the menus current state from 0-255.
// states 0-127 are used for the status menus, states 128-255 are used for the configuration menus.



void display_updater()
{
  button_monitor();       // monitor and handle inputs
  menu_display();         // handle the states of the display
  display_generator();    // generate the displays output from the current state
}


void button_monitor() // sets the trigger flag true every 100mS
{
  /// select button
  if (digitalRead(selbtn_pin) == 0)
  {
    if (sel.mem == 0)
    {
      if (sel.hold == 1)
        {
         sel.mem = 1;
         sel.select = 1; 
        }
      else
        {
        sel.hold = 1;
        }
    }
  }
  else
  {
    sel.mem = 0;
    sel.hold = 0;
    sel.select = 0;
  }

  ////// up button
  if (digitalRead(upbtn_pin) == 0)
  {
    if (up.mem == 0)
    {
      if (up.hold == 1)
      {
       up.mem = 1;
       up.select = 1; 
      }
      else
      {
        up.hold = 1;
      }
    }
  }
  else
  {
    up.mem = 0;
    up.hold = 0;
    up.select = 0;
  }
  
  ////// down button
  if (digitalRead(dwnbtn_pin) == 0)
  {
    if (dn.mem == 0)
    {
      if (dn.hold == 1)
      {
       dn.mem = 1;
       dn.select = 1; 
      }
      else
      {
        dn.hold = 1;
      }
    }
  }
  else
  {
    dn.mem = 0;
    dn.hold = 0;
    dn.select = 0;
  }
}

/// The function menu_display handles the state changes due to button inputs, it also has allot of conditional statements for determining the displays output.

/// NOTE: this function could be broken up into a state handler and a seperate display generator 

 void menu_display()
 {
  if (menu_state <= 127)
    {
      if (sel.select == 1) // if the menu button is pressed jump to the menu state
        {
          menu_state = 130;
          sel.select = 0;
        }
      else if (up.select == 1)
        {
          if (menu_state >= 7) // if we are alread at the last menu then roll back to 0
          {
            menu_state = 0;
          }
          else
          {
            menu_state ++;
          }
          up.select = 0;
        }  
      else if (dn.select == 1)
        {
          if (menu_state == 0)
          {
            menu_state = 7;
          }
          else
          {
            menu_state --;
          }
          dn.select = 0;
        }
     }
    
    else // assuming the menu_state is at 128 or above we are in a setup menu state...
    {
      if (sel.select == 1) // if the menu button is pressed jump to the menu state
        {
          if (menu_state == 128)  // we dont want to be able to enable a menu when in this state because it is the SW Version information page, a setup vatiable doesnt exist....
            {
            }
          else if (menu_state == 129)  // state 129 is the option to exit the setup menu, thius when selected it resets the state to the first display state.
            {
              write_setup_vars(); // Note: upon exiting the setup menu we also need to save all the setup variables in case they were changed
              menu_state = 0;
            }
          else        // we must be in a state with a setup variable then so enable editing of it.
            {
            menu_enable  = !menu_enable;
            }
          sel.select = 0;
        }
      if (menu_enable == 0)
      {
       if (up.select == 1)
        {
          if (menu_state == 136) // if we are alread at the last menu then roll back to 0
          {
            menu_state = 128;
          }
          else
          {
            menu_state ++;
          }
          up.select = 0;
        }  
      else if (dn.select == 1)
        {
          if (menu_state == 128)
          {
            menu_state = 136;
          }
          else
          {
            menu_state --;
          }
          dn.select = 0;
        }
      }
      else  // the menu is enabled so we will modify the variable and not the menu_state......
        {
          if (menu_state >= 130)
          {
            if (up.select == 1)
            {
            setup_variables[menu_state-130] ++;
            up.select = 0;
            }
            
            else if (dn.select == 1)
            {
            setup_variables[menu_state-130] --;
            dn.select = 0;
            }
          }
        }     
    }  
 }
 

void display_generator() /////////////////DISPLAY PRINTING /////////////////
{
lcd.clear(); // clear the screen so we can refresh it
if (menu_state <= 127)  // we are in a status display state, display accordingly.
  {
      switch (menu_state) 
    {
    case 0:
      for (int i=0 ; i<=1; i++)
        {
          string_printer(i,0);
          if (system_status[i] == 0)
          {
            string_printer(3,0);
            lcd.setCursor(0,1);
          }
          else if (system_status[i] == 1)
          {
            string_printer(2,0);
            lcd.print(current_profile[i]);
            lcd.setCursor(0,1);
          }
          else
          {
            string_printer(4,0);
            lcd.print(system_status[i]);
            lcd.setCursor(0,1);
          }
        }
      break;

      case 1:
        string_printer(0,0);  // channel 0 Voltage, current, and power
        string_printer(5,0);
        lcd.print(input_data[0][0]);
        lcd.setCursor(0,1);
        string_printer(7,0);
        lcd.print((input_data[0][0]*input_data[0][1])/1000);
        lcd.setCursor(7,1);
        string_printer(6,0);
        lcd.print(input_data[0][1]);
        break;
      
      case 4:
        string_printer(1,0);  // channel 1 Voltage, current, and power
        string_printer(5,0);
        lcd.print(input_data[1][0]);
        lcd.setCursor(0,1);
        string_printer(7,0);
        lcd.print((input_data[1][0]*input_data[0][1])/1000);
        lcd.setCursor(7,1);
        string_printer(6,0);
        lcd.print(input_data[1][1]);
        break;
        
       case 2: // channel 0 SOC and Cell temp
        string_printer(0,0);
        string_printer(8,0);
        lcd.print(input_data[0][4]);
        string_printer(15,0);
        lcd.setCursor(0,1);
        string_printer(9,0);
        lcd.print(input_data[0][2]/10); //, temps in units of 0.1 degC so need to be scaled to print in whole units.
        lcd.print((char)223);
        string_printer(14,0);
        break;
      
      case 5:  // channel 1 SOC and Cell temp
        string_printer(1,0);
        string_printer(8,0);
        lcd.print(input_data[1][4]);
        string_printer(15,0);
        lcd.setCursor(0,1);
        string_printer(9,0);
        lcd.print(input_data[1][2]/10); //, temps in units of 0.1 degC so need to be scaled to print in whole units.
        lcd.print((char)223);
        string_printer(14,0);
        break;
      
      case 3:  // channel 0 load bank temp, and charge bank temp
        string_printer(0,0);  // ch0
        string_printer(10,0);  // Load=
        lcd.print(input_data[0][5]); // load bank temp
        lcd.print((char)223);
        string_printer(14,0);  // degree C
        lcd.setCursor(0,1);  // move to the next line
        string_printer(11,0);  // charge= 
        lcd.print(input_data[0][6]);  // charge bank temp
        lcd.print((char)223);
        string_printer(14,0);  // degree C
        break;
        
      case 6:  // channel 1 load bank temp, and charge bank temp
        string_printer(1,0);  // ch0
        string_printer(10,0);  // Load=
        lcd.print(input_data[1][5]);  // load bank temp
        lcd.print((char)223);
        string_printer(14,0);  // degree C
        lcd.setCursor(0,1);  // move to the next line
        string_printer(11,0);  // charge= 
        lcd.print(input_data[1][6]);  // charge bank temp
        lcd.print((char)223);
        string_printer(14,0);  // degree C
        break;
        
      case 7:                           // chamber set and measured temps.
        string_printer(12,0);            // chamber set=
        lcd.print(set_temp);              // set chamber temp
        lcd.print((char)223);            // degree symbol
        string_printer(14,0);            // degree C
        lcd.setCursor(0,1);              // move to the next line
        string_printer(13,0);            // meas=
        lcd.print(input_data[0][3]/10);     // measured air temp, temps in units of 0.1 degC so need to be scaled to print in whole units.
        lcd.print((char)223);            // degree symbol
        string_printer(14,0);            // degree C
        break;
        
      default:    // something isnt right, print the state to the LCD so we can correct this....
        lcd.print(menu_state);
      }
    }
else  // we are above menu_state 127 and thus in a setup menu, display accordingly....
  {
  lcd.clear(); // reset the display
  if (menu_enable == 0)
    {
    string_printer((menu_state-125),1); // select (string number calculated from the menu_state, and '1' for setup strings)
    lcd.setCursor(0, 1);  // move to the second line

    string_printer((0),1); // select (string number calculated from the menu_state, and '1' for setup strings)
    }
  else
    {
    string_printer((menu_state-125),1); // select (string number calculated from the menu_state, and '1' for setup strings)
    lcd.setCursor(0, 1);  // move to the second line
    string_printer((1),1); // select (string number for the -- variable alt characters, and '1' for setup strings)
    lcd.setCursor(7, 1);  // center for printing the variable
    lcd.print (setup_variables[menu_state-130]);
    lcd.setCursor(14, 1);  // right justify
    string_printer((2),1); // select (string number for the ++ variable alt characters, and '1' for setup strings)
    } 
  }
}



/// This function takes the requested string that was passed to it and buffers it from the program memory, it then prints that buffer to the LCD.

/// NOTES: this function currently uses a if statement to chosse between two arrays storing the strings, it could probably be done better with a pointer.

void string_printer(byte string_number, byte array_number) 
  {
  char buffer[16];      // create a buffer large enough to fit one whole line of LCD text;
  if (array_number == 0)  // are we using the status or setup menus? NOTE: this should be replaced with a pointer!!! I just dont know how to do that right now :-(
    {
    strcpy_P(buffer, (char*)pgm_read_word(&(status_string_table[string_number]))); // Necessary casts and dereferencing, just copy. 
    }
  else
    {
      strcpy_P(buffer, (char*)pgm_read_word(&(setup_string_table[string_number]))); // Necessary casts and dereferencing, just copy. 
    }
  lcd.print(buffer);                           
  }



void write_setup_vars()
{
   for (int i=0; i < 6 ; i++)
  {
   EEPROM.write(i, setup_variables[i]);
  }
}

void read_setup_vars()
{
   for (int i=0; i < 6 ; i++)
  {
   setup_variables[i] = EEPROM.read(i);
  }
}
