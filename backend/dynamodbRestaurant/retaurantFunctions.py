from __future__ import print_function # Python 2/3 compatibility
from yelpApiDivy import Yelp_API
from decimal import *
import boto3
import json
import decimal
import boto3

# Helper class to convert a DynamoDB item to JSON.
class DecimalEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, decimal.Decimal):
            if o % 1 > 0:
                return float(o)
            else:
                return int(o)
        return super(DecimalEncoder, self).default(o)

class FoodUpload(object):
    def __init__(self):
        dynamodb = boto3.resource('dynamodb', region_name='us-east-1')
        self.table = dynamodb.Table('foodtinder-mobilehub-761050320-restaurant')

    # Upload single item
    def upload(self, item):
        #  getcontext().prec = 5

        # Convert float -> string -> decimal
        restaurantId    = item['restaurantId']
        restaurant_name = item['restaurant_name']
        address         = item['address']
        categories      = item['category']
        city            = item['city']
        latitude        = Decimal(str(item['latitude']))
        longitude       = Decimal(str(item['longitude']))
        postal_code     = int(item['postal_code'])
        state           = item['state']
        try:
            response = self.table.put_item(
            Item={
                'restaurantId'      :   restaurantId,
                'address'           :   address,
                'categories'        :   categories,
                'city'              :   city,
                'latitude'          :   latitude,
                'longitude'         :   longitude,
                'postal_code'       :   postal_code,
                'restaurant_name'   :   restaurant_name,
                'state'             :   state
                },
            ConditionExpression='attribute_not_exists(restaurantId)'
            )
            print("Success: {}".format(restaurantId))
            json.dumps(response, indent=4, cls=DecimalEncoder)
        except Exception as e:
            try:
                print("Fail: {}".format(restaurantId))
            except Exception as e:
                print("Can't Print")
            print(e)

    # Upload from yelpApi list (cap 20)
    def uploadList(self, data):
        for item in data:
            #  pprint(item)
            self.upload(item)

    # Upload until no more business returns
    def uploadAllFilter(self, category, term):
        offset = 0;
        while True:
            params = {
                "term": term,
                "food_per_business": 1000,
                "ll": "33.9533, -117.3962",
                "limit": 20,
                "radius_filter": 40000,
                "category_filter": category,
                "sort": 0,
                "offset": offset
            }

            response = Yelp_API(params).call_API()
            #  print("Number of food: {}".format(len(response)))
            print("Offset: {}".format(offset))
            if len(response) == 0:
                break
            offset += 20

            FoodUpload().uploadList(response)

if __name__ == "__main__":
    FoodUpload().uploadAllFilter("", "restaurant")
