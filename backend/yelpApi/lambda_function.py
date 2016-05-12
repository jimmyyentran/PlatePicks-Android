from __future__ import print_function

import json
from yelpApi import Yelp_API

print('Loading function')

def lambda_handler(event, context):
    print(event)
    response = Yelp_API(event).call_API()
    print(response)
    return response # Echo back the first key value
