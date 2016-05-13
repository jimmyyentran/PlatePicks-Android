from __future__ import print_function # Python 2/3 compatibility
import boto3
import json
import decimal

# Helper class to convert a DynamoDB item to JSON.
class DecimalEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, decimal.Decimal):
            if o % 1 > 0:
                return float(o)
            else:
                return int(o)
        return super(DecimalEncoder, self).default(o)

dynamodb = boto3.resource('dynamodb', region_name='us-east-1')

table = dynamodb.Table('foodtinder-mobilehub-761050320-food')

foodId = "McgzqVxfFfYAUutq5xhNlA"
restaurantId = "Po Huynh" 
name = "Cobination Chow Fun"

response = table.put_item(
   Item={
       'foodId' : foodId,
       'restaurantId' : restaurantId,
       'name' : name
    }
)

print("PutItem succeeded:")
print(json.dumps(response, indent=4, cls=DecimalEncoder))
