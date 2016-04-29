import requests
from bs4 import BeautifulSoup
import urllib.parse
from nameParser import NameParser


class Crawler(object):
    def no_limit(url):
        parse = NameParser("unwantedWords.txt")

        # new = input("Enter the url to crawl: ")
        # url = new.rstrip()  # removes any extra spaces
        # edit url to get the pictures and only the foods
        url.replace("/biz", "/biz_photos")
        url += "?tab=food&start=0"
        # parse the url for html code
        source_code = requests.get(url)  # variable = requests.get(url)
        html = source_code.text  # get source code of page
        soup = BeautifulSoup(html, 'html.parser')
        # parse for the number of pages
        for sz in soup.find("div", "page-of-pages arrange_unit arrange_unit--fill"):
            sz = int(sz[sz.find("of") + 2:])

        visited = [url]  # keeps track of visited urls
        pics = []
        pics_id = []
        com = []
        for i in range(sz):
            url.find("&start")
            url = url[:url.find("&start")] + "&start=" + str(30 * i)
            i += 1

            # parse the url for html code
            source_code = requests.get(url)  # variable = requests.get(url)
            html = source_code.text  # get source code of page
            soup = BeautifulSoup(html, 'html.parser')
            #find all the links thats are img urls
            for link in soup.findAll('img', src=True, alt=True):
                link['src'] = urllib.parse.urljoin(url, link['src'])
                if '#' not in link['src']:
                    if link['src'] not in visited:
                        visited.append(link['src'])
                        if "bphoto" in link['src']:
                            fake = link['alt']
                            fake = fake[fake.find("States.") + 7:]
                            # removes majority of the bad comments
                            if " United States" not in fake:
                                if parse.parse_name(fake.rstrip()) and fake.rstrip():
                                    com.append(fake.rstrip()[1:])
                                    pics.append(link['src'])
                                    fake = link['src']
                                    fake = fake[fake.find("bphoto/") + 7:fake.rfind("/258s.jpg")]
                                    pics_id.append(fake)
        # print(len(pics))
        # remove the first thing because it automatically catches it and is in a small size
        com.pop(0)
        pics.pop(0)
        pics_id.pop(0)

        information = []
        # prints the comments, pic_id and the url of the picture
        for pic, coms, pic_id in zip(pics, com, pics_id):
            information.append([pic_id, pic, coms])
            # print(coms)
            # print(pic_id)
            # print(pic)

        return information

    def limit(url,limit):
        parse = NameParser("unwantedWords.txt")

        # new = input("Enter the url to crawl: ")
        # url = new.rstrip()  # removes any extra spaces
        # edit url to get the pictures and only the foods
        url= url
        url.replace("/biz", "/biz_photos")
        url += "?tab=food&start=0"
        # parse the url for html code
        source_code = requests.get(url)  # variable = requests.get(url)
        html = source_code.text  # get source code of page
        soup = BeautifulSoup(html, 'html.parser')
        # parse for the number of pages
        for sz in soup.find("div", "page-of-pages arrange_unit arrange_unit--fill"):
            sz = int(sz[sz.find("of") + 2:])

        visited = [url]  # keeps track of visited urls
        pics = []
        pics_id = []
        com = []
        for i in range(sz):
            url.find("&start")
            url = url[:url.find("&start")] + "&start=" + str(30 * i)
            i += 1

            # parse the url for html code
            source_code = requests.get(url)  # variable = requests.get(url)
            html = source_code.text  # get source code of page
            soup = BeautifulSoup(html, 'html.parser')
            #find all the links thats are img urls
            for link in soup.findAll('img', src=True, alt=True):
                if len(pics_id) ==(limit+1):
                    break
                link['src'] = urllib.parse.urljoin(url, link['src'])
                if '#' not in link['src']:
                    if link['src'] not in visited:
                        visited.append(link['src'])
                        if "bphoto" in link['src']:
                            fake = link['alt']
                            fake = fake[fake.find("States.") + 7:]
                            # removes majority of the bad comments
                            if " United States" not in fake:
                                if parse.parse_name(fake.rstrip()) and fake.rstrip():
                                    com.append(fake.rstrip()[1:])
                                    pics.append(link['src'])
                                    fake = link['src']
                                    fake = fake[fake.find("bphoto/") + 7:fake.rfind("/258s.jpg")]
                                    pics_id.append(fake)

        # print(len(pics))
        # remove the first thing because it automatically catches it and is in a small size
        com.pop(0)
        pics.pop(0)
        pics_id.pop(0)

        information = []
        # prints the comments, pic_id and the url of the picture
        for pic, coms, pic_id in zip(pics, com, pics_id):
            information.append([pic_id, pic, coms])
            # print(coms)
            # print(pic_id)
            # print(pic)

        return information



# nl = Crawler.no_limit("https://www.yelp.com/biz/arcade-coffee-roasters-riverside")
#l = Crawler.limit("https://www.yelp.com/biz/arcade-coffee-roasters-riverside",10)
# print(len(nl))
#for ls in l:
#    print(ls)
