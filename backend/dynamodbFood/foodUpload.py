from __future__ import print_function # Python 2/3 compatibility
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

    def upload(self, item):
        foodId = item["food_id"]
        restaurantId = item['location']['name']
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
            print("Success: {}".format(foodId))
            json.dumps(response, indent=4, cls=DecimalEncoder)
        except Exception as e:
            print("Fail: {}".format(foodId))

    def uploadList(self, data):
        for item in data:
            self.upload(item)


