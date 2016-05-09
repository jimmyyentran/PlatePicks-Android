from yelpApi import Yelp_API
import pprint

#Test Parameters
params = {
        "term": "asian",
       "food_per_business": 1,
       "ll": "40.7128, -74.0059",
       "limit": 5,
       "radius_filter": 40000,
       "category_filter": "vietnamese,filipino",
       "sort": 1,
       "latitude" : "40.987"
        }

response = Yelp_API(params).call_API()
pprint.pprint(response)
