from __future__ import print_function # Python 2/3 compatibility
from yelpApi import Yelp_API
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
        self.table = dynamodb.Table('foodtinder-mobilehub-761050320-food')

    # Upload single item
    def upload(self, item):
        foodId = item["food_id"]
        restaurantId = item['location']['business_id']
        name = item['name']

        try:
            response = self.table.put_item(
            Item={
                'foodId' : foodId,
                'restaurantId' : restaurantId,
                'name' : name
                },
            ConditionExpression='attribute_not_exists(foodId)'
            )
            print("Success: {} {}".format(foodId, restaurantId))
            json.dumps(response, indent=4, cls=DecimalEncoder)
        except Exception as e:
            print("Fail: {}".format(foodId))

    # Upload from yelpApi list (cap 20)
    def uploadList(self, data):
        for item in data:
            self.upload(item)

    # Upload until no more business returns
    def uploadAllFilter(self, category, term, os):
        offset = os;
        while True:
            print("Offset: {}".format(offset))
            params = {
                "term": term,
                "food_per_business": 5,
                "ll": "33.9533, -117.3962",
                "limit": 20,
                "radius_filter": 40000,
                "category_filter": category,
                "sort": 0,
                "offset": offset
            }

            response = Yelp_API(params).call_API()
            print("Number of food: {}".format(len(response)))
            if len(response) == 0:
                break
            offset += 20
            FoodUpload().uploadList(response)

    def scanAndUpload(self, attribute, value):
        response = table.scan(

                )


if __name__ == "__main__":
    FoodUpload().uploadAllFilter("", "restaurant", 0)
