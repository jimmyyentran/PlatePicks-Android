from yelp.client import Client
from yelp.oauth1_authenticator import Oauth1Authenticator
import io
import json

params = {
    'lang': 'en'
}

with io.open('config_secret.json') as cred:
    creds = json.load(cred)
    auth = Oauth1Authenticator(**creds)
    client = Client(auth)
    response = client.search('San Francisco', **params)

for bus in response.businesses:
    print (bus.id)

