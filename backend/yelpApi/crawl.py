#  import requests
from bs4 import BeautifulSoup
from urlparse import urljoin
from nameParser import NameParser
import grequests
#  import unirest
#  from requests_futures.sessions import FuturesSession


class Crawler(object):
    # data is passed in as url as key and respective info: {url, location info}
    def __init__(self, data):
        self.urls = data;
        self.parse = NameParser("unwantedWords.txt")
        self.information = []
        #  self.session = FuturesSession()
        #  tornado.httpclient.AsyncHTTPClient.configure("tornado.curl_httpclient.CurlAsyncHTTPClient")

    #Take in a list of url
    def limit(self,limit):
        self.limit = limit
        async_list = []
        for key in self.urls:
            action_item = grequests.get(key, hooks = {'response' :
                self.extract_food_names
                })
            async_list.append(action_item)
        grequests.map(async_list, exception_handler=self.exception_handler)
        return self.information

    #  @tornado.web.asynchronous
    #  def limit(self,limit):
        #  self.limit = limit
        #  client = tornado.httpclient.AsyncHTTPClient()
        #  for key in self.urls:
            #  client.fetch(HTTPRequest(url, 'GET',
                #  callback=self.extract_food_names))
            #  client.fetch(key, self.extract_food_names)
        #  tornado.ioloop.IOLoop.instance().start()
        #  client.close()
        #  return self.information

    #  def limit(self,limit):
        #  self.limit = limit
        #  future = []
        #  for key in self.urls:
            #  future.append(self.session.get(key,
                #  background_callback=self.extract_food_names))
            #  #  print(key)
        #  for f in future:
            #  response = f.result()

        #  #  print(self.information)
        #  return self.information

    def exception_handler(self, request, exception):
        print "Failed %s" % (request)
        print(exception)


    def extract_food_names(self, response, **kwargs):
        #  print(vars(response))
        url = response.url
        #  print(url)
        firstUrl = url
        html = response.content
        #  print(html)
        #  url = args['url']
        #  html = args['response']
        soup = BeautifulSoup(html, 'html.parser')
        # parse for the number of pages
        for sz in soup.find("div", "page-of-pages arrange_unit arrange_unit--fill"):
            sz = int(sz[sz.find("of") + 2:])

        visited = [url]  # keeps track of visited urls
        pics = []
        pics_id = []
        com = []
        flag = True # if reached limit

        #  sz = 1 #just query on the first page
        for i in range(sz):
            if not flag: break # if reached limit then break this loop
            url.find("&start")
            url = url[:url.find("&start")] + "&start=" + str(30 * i)
            i += 1

            # parse the url for html code
            #  source_code = requests.get(url)  # variable = requests.get(url)
            #  html = source_code.text  # get source code of page
            soup = BeautifulSoup(html, 'html.parser')
            #find all the links thats are img urls
            for link in soup.findAll('img', src=True, alt=True):
                if len(pics_id) ==(self.limit+1):
                    #  print("BREAK")
                    flag = False
                    break
                #  link['src'] = urllib.parse.urljoin(url, link['src'])
                link['src'] = urljoin(url, link['src'])
                if '#' not in link['src']:
                    if link['src'] not in visited:
                        visited.append(link['src'])
                        if "bphoto" in link['src']:
                            fake = link['alt']
                            fake = fake[fake.find("States.") + 7:]
                            # removes majority of the bad comments
                            if " United States" not in fake:
                                #  if parse.parse_name(fake.rstrip()) and fake.rstrip():
                                prettified = self.parse.parse_name(fake.rstrip().lstrip()) # strip
                                #  print(prettified)
                                #  if parse.parse_name(fake.rstrip()) is not None:
                                if prettified is not None:
                                    com.append(prettified)
                                    pics.append(link['src'])
                                    fake = link['src']
                                    fake = fake[fake.find("bphoto/") + 7:fake.rfind("/258s.jpg")]
                                    pics_id.append(fake)

        # print(len(pics))
        # remove the first thing because it automatically catches it and is in a small size
        com.pop(0)
        pics.pop(0)
        pics_id.pop(0)

        #  information = []
        # prints the comments, pic_id and the url of the picture
        for pic, coms, pic_id in zip(pics, com, pics_id):
            #  self.information.append([pic_id, pic, coms])
            to_be_returned = dict(url=pic, food_id=pic_id, name=coms)
            to_be_returned['location']=self.urls[firstUrl]
            #  to_be_returned.update(self.urls[firstUrl])
            self.information.append(to_be_returned)
            #  self.information.append(dict(url=pic, food_id=pic_id,
                #  name=coms).update(original_info))
            # print(coms)
            # print(pic_id)
            # print(pic)
        #  print(information)
        #  return information



# nl = Crawler.no_limit("https://www.yelp.com/biz/arcade-coffee-roasters-riverside")
#l = Crawler.limit("https://www.yelp.com/biz/arcade-coffee-roasters-riverside",10)
# print(len(nl))
#for ls in l:
#    print(ls)
