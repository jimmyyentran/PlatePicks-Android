from yelpApi import Yelp_API
import pprint
from foodUpload import FoodUpload

#Test Parameters
params = {
        #  "term": "asian",
       "food_per_business": 1000,
       "ll": "33.9533, -117.3962",
       "limit": 20,
       "radius_filter": 40000,
       "category_filter": "chinese",
       "sort": 0,
       "offset": 0,
        }

response = Yelp_API(params).call_API()
print("Number of food: {}".format(len(response)))
FoodUpload().uploadList(response)
#  pprint.pprint(response)
