import io
import json
from pprint import pprint
from yelp.client import Client
from yelp.oauth1_authenticator import Oauth1Authenticator

#  class YelpAPI(object):
    #  def __init__(self, )

params = {
    'lang': 'en'
}

with io.open('config_secret.json') as cred:
    creds = json.load(cred)
    auth = Oauth1Authenticator(**creds)
    client = Client(auth)
    response = client.search('San Francisco', **params)

# print full object attribute of first object
pprint(vars(response.businesses[0]))

# print all business id's
for bus in response.businesses:
    print (bus.id)

