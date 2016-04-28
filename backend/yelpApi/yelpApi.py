import io
import json
from pprint import pprint
from yelp.client import Client
from yelp.oauth1_authenticator import Oauth1Authenticator
from yelp.config import SEARCH_PATH
from yelp.obj.search_response import SearchResponse
from crawl import Crawler

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
        #  return self.client.search('SF', self.data)
        return SearchResponse (
                self.client._make_request(SEARCH_PATH, self.data)
        )
        #  return self.client.search_by_coordinates(33.9533, -117.23, self.data)



params = {
        'term': 'asian', #general search
        'randomTest': 'd', #test if input random key
        'll': '33.7175, -117.8311', #long and latitude
        'limit': 2, #number of businesses
        'radius_filter': 40000, #25 miles maximum
        'category_filter': 'vietnamese', #pre-set categories
        'sort': 1 #distance
        }

response = Yelp_API(params).call_API()

# print full object attribute of first object
print("----------------------------------------------------")
print("FIRST BUSINESS")
pprint(vars(response.businesses[0]))

# print all business id's
print("----------------------------------------------------")
print("FIRST 5 BUS.ID")
for bus in response.businesses:
    print ("{0}".format( bus.id))

# crawl url for pics
print("----------------------------------------------------")
print("CRAWL")
#  Crawler("https://www.yelp.com/biz/pho-vinam-riverside")
for bus in response.businesses:
    Crawler("http://www.yelp.com/biz_photos/" + bus.id +
"?tab=food&start=0")

