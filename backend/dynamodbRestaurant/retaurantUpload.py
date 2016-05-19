from __future__ import print_function # Python 2/3 compatibility
import boto3
import json
import decimal
from boto3.dynamodb.conditions import Key, Attr
from botocore.exceptions import ClientError

# Helper class to convert a DynamoDB item to JSON.
class DecimalEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, decimal.Decimal):
            if o % 1 > 0:
                return float(o)
            else:
                return int(o)
        return super(DecimalEncoder, self).default(o)

class FoodInsert(object):
    def __init__(self):
        dynamodb = boto3.resource("dynamodb", region_name='us-east-1')
        table = dynamodb.Table('foodtinder-mobilehub-761050320-restaurant')

restaurantId = "Pho Huynh"

try:
    response = table.get_item(
        Key={
            'restaurantId': restaurantId
        }
    )
except ClientError as e:
    print(e.response['Error']['Message'])
else:
    item = response['Item']
    print("GetItem succeeded:")
    print(item)
    print(json.dumps(item, indent=4, cls=DecimalEncoder))
