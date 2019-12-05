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

    # trolley ID - hardcoded to each trolley
    trolley_id = "ZZafaKzVTvmlreT99wBL"
    trolleys = db.collection(u'trolleys')
    trolley = trolleys.document(trolley_id).get().to_dict()
    user = trolley['user']
    running_weight = 0
    illop_count = 0 # silence this if using user

    # Read data several times and return mean value
    # subtracted by offset and converted by scale ratio to
    # desired units. In my case in grams.
    print("Now, I will read data in infinite loop. To exit press 'CTRL + C'")
    input('Press Enter to begin reading')
    print('Current weight on the scale in grams is: ')

    # unlock the cart when there is user
    while user is None:
        print("This cart is still locked!")
        illop_count = 0
        trolleys.document(trolley_id).set({f'running_weight': 0,
                                           f'scanning': False,
                                           f'correct_item': False,
                                           f'product_id': None,
                                           f'removed_item': None}, merge=True)
        break

    # once unlocked, goes in this while loop
    # start checking when scanning == True
    while user is not None:
        # retrieve these information about this trolley
        print("This cart is unlocked!")
        trolley = trolleys.document(trolley_id).get().to_dict()
        scanning = trolley['scanning']
        added_item = trolley['product_id']
        removed_item = trolley['removed_item']
        running_weight = trolley['running_weight']
        illop = trolley['illop']
        
        print("Scanning:", scanning)
        print(hx.get_weight_mean(3), 'g')

        # why 3?
        # if 1, it's too fast, when item put down, clock rise too fast and catches smaller value
        # if too high, too slow and value drifts
        # not perfect, when putting item must be good
        
        # illegal add/remove cases
        if (hx.get_weight_mean(3) - running_weight >= 50) or (running_weight - hx.get_weight_mean(3) >= 50):
            print("Illegal Operation Detected!")
            illop_count += 1
            if illop_count == 3:
                trolleys.document(trolley_id).set({f'illop': True}, merge=True)
        
        # when illop, trap them here
        while illop:
            print("Please remove item and tap ok in app!")
            trolley = trolleys.document(trolley_id).get().to_dict()
            illop = trolley['illop']
            illop_count = 0

        while scanning and not illop:
            
            print("start_scanning signal received! Scanning now...")
            print(hx.get_weight_mean(3), 'g')
            
            trolley = trolleys.document(trolley_id).get().to_dict()
            scanning = trolley['scanning']
            added_item = trolley['product_id']
            removed_item = trolley['removed_item']
            illop = trolley['illop']

            # if weight change drastically:
            # 1. adding item
            # 2. removing item
                        
            # 1. adding item
            if (hx.get_weight_mean(3) - running_weight) >= 50 and added_item is not None and removed_item is None:
                print("Drastic increase detected!")
                actual_weight = hx.get_weight_mean(3) - running_weight

                # gets the item information from reference
                added_item = added_item.get().to_dict()
                added_item_weight = added_item['weight']
                print("Actual Weight: ", actual_weight, 'g')
                print("Product Weight: ", added_item_weight, 'g')

                # if the weight difference is acceptable, add item
                if abs(actual_weight - added_item_weight) <= 10:
                    running_weight = hx.get_weight_mean(3)
                    
                    # append item ref
                    item_list = trolley['items']
                    if item_list is None:
                        item_list = [trolley['product_id']]
                    else:
                        item_list.append(trolley['product_id'])
                    
                    print("Actual Weight: ", actual_weight, 'g')
                    print("Product Weight: ", added_item_weight, 'g')
                    time.sleep(0.3)
                    trolleys.document(trolley_id).set({f'scanning': False,
                                                      f'product_id': None,
                                                      f'running_weight': running_weight,
                                                      f'correct_item': True,
                                                      f'items': item_list,
                                                      }, merge=True)
                    scanning = False
                    
                # else wrong item!
                else:
                    print("Wrong item!")
                    print("Actual Weight: ", actual_weight, 'g')
                    print("Product Weight: ", added_item_weight, 'g')
                    time.sleep(0.3)
                    trolleys.document(trolley_id).set({f'scanning': False,
                                                      f'product_id': None,
                                                      f'correct_item': False
                                                      }, merge=True)
                    
                    scanning = False
                # reset loop
                break
            
            # 2. remove item
            elif (running_weight - hx.get_weight_mean(3) ) >= 50 and removed_item is not None and added_item is None:
                print("Drastic decrease detected!")
                actual_weight = running_weight - hx.get_weight_mean(3)
                
                # gets the item information from reference
                removed_item = removed_item.get().to_dict()
                removed_item_weight = removed_item['weight']
                print("Actual Weight: ", actual_weight, 'g')
                print("Product Weight: ", removed_item_weight, 'g')
               
                # if the weight difference is acceptable, remove item
                if abs(actual_weight - removed_item_weight) <= 10:
                    running_weight = hx.get_weight_mean(3)
                    
                    # remove item ref
                    item_list = trolley['items']
                    item_list.remove(trolley['removed_item'])
                    
                    print("Actual Weight: ", actual_weight, 'g')
                    print("Product Weight: ", removed_item_weight, 'g')
                    time.sleep(0.3)
                    trolleys.document(trolley_id).set({f'scanning': False,
                                                      f'removed_item': None,
                                                      f'running_weight': running_weight,
                                                      f'correct_item': True,
                                                      f'items': item_list,
                                                      }, merge=True)
                    scanning = False
                
                # else wrong item!
                else:
                    print("Wrong item!")
                    print("Actual Weight: ", actual_weight, 'g')
                    print("Product Weight: ", removed_item_weight, 'g')
                    time.sleep(0.3)
                    trolleys.document(trolley_id).set({f'scanning': False,
                                                      f'removed_item': None,
                                                      f'correct_item': False
                                                      }, merge=True)
                    
                    scanning = False
                # reset loop
                break
            
            else: continue
                        
except (KeyboardInterrupt, SystemExit):
    print('Bye :)')

finally:
    GPIO.cleanup()
