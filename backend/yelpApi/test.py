from yelpApi import Yelp_API
import pprint

#Test Parameters
params = {
        "term": "asian",
       "food_per_business": 3,
       "ll": "33.9533, -117.3962",
       "limit": 1,
       "radius_filter": 40000,
       "category_filter": "vietnamese,filipino",
       "sort": 1,
       "offset": 0
        }

response = Yelp_API(params).call_API()
pprint.pprint(response)
