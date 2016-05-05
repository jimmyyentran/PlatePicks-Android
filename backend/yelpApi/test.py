from yelpApi import Yelp_API
import pprint

#Test Parameters
params = {
        'term': 'asian', #general search
        'food_per_business': 10, #test if input random key
        'll': '33.7175, -117.8311', #long and latitude
        'limit': 20, #number of businesses
        'radius_filter': 40000, #25 miles maximum
        'category_filter': 'vietnamese,filipino', #pre-set categories
        'sort': 1 #distance
        }

response = Yelp_API(params).call_API()
pprint.pprint(response)
