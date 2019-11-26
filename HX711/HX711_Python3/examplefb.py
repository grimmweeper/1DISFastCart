#!/usr/bin/env python3
import RPi.GPIO as GPIO  # import GPIO
from hx711 import HX711  # import the class HX711

# Firebase Imports
from firebase_admin import credentials, firestore
import firebase_admin
import time

#########################################################################################################
## SETUP PHASE (DO NOT TOUCH)
#########################################################################################################

try:
    GPIO.setmode(GPIO.BCM)  # set GPIO pin mode to BCM numbering
    # Create an object hx which represents your real hx711 chip
    # Required input parameters are only 'dout_pin' and 'pd_sck_pin'
    hx = HX711(dout_pin=17, pd_sck_pin=27)
    # measure tare and save the value as offset for current channel
    # and gain selected. That means channel A and gain 128
    err = hx.zero()
    # check if successful
    if err:
        raise ValueError('Tare is unsuccessful.')

    reading = hx.get_raw_data_mean()
    if reading:  # always check if you get correct value or only False
        # now the value is close to 0
        print('Data subtracted by offset but still not converted to units:',
              reading)
    else:
        print('invalid data', reading)

    # In order to calculate the conversion ratio to some units, in my case I want grams,
    # you must have known weight.
    input('Put known weight on the scale and then press Enter')
    reading = hx.get_data_mean()
    if reading:
        print('Mean value from HX711 subtracted by offset:', reading)
        known_weight_grams = input(
            'Write how many grams it was and press Enter: ')
        try:
            value = float(known_weight_grams)
            print(value, 'grams')
        except ValueError:
            print('Expected integer or float and I have got:',
                  known_weight_grams)

        # set scale ratio for particular channel and gain which is
        # used to calculate the conversion to units. Required argument is only
        # scale ratio. Without arguments 'channel' and 'gain_A' it sets
        # the ratio for current channel and gain.
        ratio = reading / value  # calculate the ratio for channel A and gain 128
        hx.set_scale_ratio(ratio)  # set ratio for current channel
        print('Ratio is set.')
    else:
        raise ValueError('Cannot calculate mean value. Try debug mode. Variable reading:', reading)

#########################################################################################################
## READING PHASE (SIMPLY EDIT FROM HERE!)
#########################################################################################################

    # initialize firebase stuffs
    cred = credentials.Certificate('serviceAccountKey.json')
    firebase_admin.initialize_app(cred)
    db = firestore.client()

    # trolley ID - hardcoded for now
    trolley_id = "ZZafaKzVTvmlreT99wBL"
    trolleys = db.collection(u'trolleys')
    trolley = trolleys.document(trolley_id).get().to_dict()
    user = trolley['user']
    removed_item = trolley['removed_item']

    # Read data several times and return mean value
    # subtracted by offset and converted by scale ratio to
    # desired units. In my case in grams.
    print("Now, I will read data in infinite loop. To exit press 'CTRL + C'")
    input('Press Enter to begin reading')
    print('Current weight on the scale in grams is: ')

    running_weight = 0

    while user is not None:
        trolley = trolleys.document(trolley_id).get().to_dict()

        scanning = trolley['scanning']
        print("start_scanning:", scanning)
        print(hx.get_weight_mean(1), 'g')

        while scanning:
            print("start_scanning signal received! Scanning now...")
            print(hx.get_weight_mean(1), 'g')
            # if weight change drastically:
            if (hx.get_weight_mean(1) - running_weight) >= 50:
                print("Drastic increase detected!")
                actual_weight = hx.get_weight_mean(1) - running_weight

                #########################################################################################
                # HENCE PLEASE PASS THE last_item_id REFERENCE WITH start_scanning = True!
                # Please Note Above!

                last_item = trolley['last_item_id']
                last_item = last_item.get().to_dict()
                print(last_item)
                item_weight = last_item['weight']
                print("Actual Weight: ",actual_weight, 'g')
                print("Product Weight: ",item_weight, 'g')

                #########################################################################################
                
                if abs(actual_weight - item_weight) <= 10:
                    running_weight += actual_weight
    
                    print("Actual Weight: ",actual_weight, 'g')
                    print("Product Weight: ",item_weight, 'g')
                    trolleys.document(trolley_id).set({
                                                      f'scanning': False,
                                                      f'running_weight': running_weight,
                                                      f'correct_item': True
                                                      }, merge=True)
                    scanning = False
            time.sleep(3)
            
        #To test it out next time    
        #Update existing weight on the trolley cart when item is being removed and setting it to null
        while removed_item is not None:
            
            print("I'm in this loop")
            if (running_weight- hx.get_weight_mean(1)) >= 50:
                print("Drastic decrease detected!")
                actual_weight = running_weight - hx.get_weight_mean(1)
                
                product_rm = removed_item.get().to_dict()
                item_weight = product_rm['weight']
                
                print("Actual Weight: ",actual_weight, 'g')
                print("Product Weight: ",item_weight, 'g')                
                
                if abs(actual_weight - item_weight) <= 10:
                    running_weight -= actual_weight
                    trolleys.document(trolley_id).set({
                                                      f'removed_item': None,
                                                      f'running_weight': running_weight,
                                                      f'correct_item': True
                                                      }, merge=True)                
                
                



except (KeyboardInterrupt, SystemExit):
    print('Bye :)')

finally:
    GPIO.cleanup()
