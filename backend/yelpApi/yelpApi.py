import io
import json
from pprint import pprint
from yelp.client import Client
from yelp.oauth1_authenticator import Oauth1Authenticator

# This class serves as Yelp API's wrapper
# param: python dictionary with
class Yelp_API(object):
    def __init__(self, data):
        #authorization
        with io.open('config_secret.json') as cred:
            creds = json.load(cred)
            auth = Oauth1Authenticator(**creds)
            self.client = Client(auth)
            self.data = data

    def call_API(self):
        return self.client.search('SF', self.data)

params = {
        'term': 'food',
        'l': 'd'
        }

response = Yelp_API(params).call_API()

# print full object attribute of first object
pprint(vars(response.businesses[0]))

# print all business id's
for bus in response.businesses:
    print (bus.id)

