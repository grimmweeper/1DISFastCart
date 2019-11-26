#!/usr/bin/env python3
import RPi.GPIO as GPIO  # import GPIO
from hx711 import HX711  # import the class HX711

# Firebase Imports
from firebase_admin import credentials, firestore
import firebase_admin
import random
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
    trolley_id = "0000"
    trolleys = db.collection(u'trolleys')
    trolley = trolleys.document(trolley_id).get().to_dict()
    user = trolley['user']

    # Read data several times and return mean value
    # subtracted by offset and converted by scale ratio to
    # desired units. In my case in grams.
    print("Now, I will read data in infinite loop. To exit press 'CTRL + C'")
    input('Press Enter to begin reading')
    print('Current weight on the scale in grams is: ')

    running_weight = hx.get_weight_mean(1)

    # start_scanning = True
    while user is not None:
        trolley = trolleys.document(trolley_id).get().to_dict()

        start_scanning = trolley['start_scanning']
        print("start_scanning:", start_scanning)
        print(hx.get_weight_mean(1), 'g')

        while start_scanning:
            print("start_scanning signal received! Scanning now...")
            # if weight change drastically:
            if (hx.get_weight_mean(1) - running_weight) >= 100:
                print("Drastic increase detected!")
                weight = hx.get_weight_mean(1) - running_weight

                #########################################################################################
                # because it's faulty, we do this to get somewhat correct weight
                # we get reference of last item, get the item weight and error and then post that
                # HENCE PLEASE PASS THE last_item_id REFERENCE WITH start_scanning = True!
                # Please Note Above!

                , products, last_item_id = trolley['last_item_id'].split('/')
                products = db.collection(u'products')
                item = products.document(last_item_id).get().to_dict()
                weight = item['weight'] + random.random()

                #########################################################################################
                running_weight += weight

                print(weight, 'g')
                trolleys.document(trolley_id).set({
                                                  f'last_weight': weight,
                                                  f'start_scanning': False,
                                                  f'running_weight': running_weight
                                                  }, merge=True)
                start_scanning = False

        time.sleep(3)

except (KeyboardInterrupt, SystemExit):
    print('Bye :)')

finally:
    GPIO.cleanup()
