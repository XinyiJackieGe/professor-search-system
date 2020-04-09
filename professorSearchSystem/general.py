import scrapy
from scrapy.spiders import CrawlSpider, Rule
from scrapy.linkextractors import LinkExtractor
import re

class QuotesSpider(CrawlSpider):
    name = "multiple"

    start_urls = ['https://www.khoury.northeastern.edu/role/tenured-and-tenure-track-faculty/',
                  'https://grainger.illinois.edu/about/directory/faculty/',
                  'https://www.eecs.mit.edu/people/faculty-advisors',
                  'https://www2.eecs.berkeley.edu/Faculty/Lists/faculty.html?_ga=2.12965941.321699826.1585814574-916567024.1585814568',
                  'https://www.cs.washington.edu/people/faculty',
                  'https://www.cs.cornell.edu/people/faculty',
                  'https://www.cs.columbia.edu/people/faculty/',
                  'http://www.ece.utexas.edu/people/faculty',
                  'https://www.cs.usc.edu/directory/faculty/',
                  'https://cs.uic.edu/faculty-staff/faculty/',
                  "https://www.seas.upenn.edu/directory/departments.php?departments=Computer%20and%20Information%20Science%20(CIS)",
                  "https://www.cc.gatech.edu/people/faculty"


        ]
    rules = [Rule(LinkExtractor(allow=r'https://www.khoury.northeastern.edu/people/.*/'), callback='parse_items', follow=True)
             Rule(LinkExtractor(allow=r'https://experts.illinois.edu/en/persons/.*-.*',
                 deny=r'.*(similar|network|fingerprints|activities|publications|prizes|datasets|clippings)'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'https://www.csail.mit.edu/person/.*'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'https://www2.eecs.berkeley.edu/Faculty/Homepages/.*'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'https://www.cs.washington.edu/people/faculty/.*',
                 deny=r'.*(students|reading|fun|page)'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'https://homes.cs.washington.edu/~.*',
                 deny=r'.*(students|teaching|index|page)'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'https://people.ece.cornell.edu/.*'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'https://www.cs.cornell.edu/~.*'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'http://www.csl.cornell.edu/~.*'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'https://people.orie.cornell.edu/.*'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'http://pi.math.cornell.edu/~.*'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'http://tsg.ece.cornell.edu/people/.*'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'http://www.cs.columbia.edu/~.*',
                 deny=r'.*(students|admin|index|page|courses)'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'http://www.ece.utexas.edu/people/faculty/.*',
                 deny=r'.*(students|admin|index|page)'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'https://www.cs.usc.edu/directory/faculty/profile/\?lname=.*',
                 deny=r'.*(students|admin|index|page)'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'https://cs.uic.edu/profiles/.*',
                 deny=r'.*(students|admin|index|page)'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'https://www.seas.upenn.edu/directory/profile.php\?ID=.*'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'https://www.cc.gatech.edu/people/.*'), callback='parse_items', follow=True),
             Rule(LinkExtractor(allow=r'http://www.cc.gatech.edu/~.*',
                 deny=r'.*(reading|admin|index|page|courses)'), callback='parse_items', follow=True)

        ]

    crawl_count = 0

    def parse_items(self, response):
        url = response.url
        # title = response.css('h2::text')[0].get()
        isProfessor = False
        # if "Professor" in title:
        #     isProfessor = True

        divs = response.css('div').getall()

        for i in range(len(divs)):
            if ("Professor" in divs[i]) and ("Computer" in divs[i]) :
                isProfessor = True
                print("Professor")
                break

        #if title:
            #print(title)
            #if "Professor" in title:
                #print("Professor")
                #response.xpath('//body//p//text()').extract()


        if isProfessor:
            self.__class__.crawl_count += 1
            print(self.__class__.crawl_count)
            #filename = re.sub('/', '_', url)
            #filename = re.sub('__', '', filename)
            #filename = re.sub('http:', '', filename)
            #filename = filename + '.html'
            #print(filename)
            filename = 'd' + str(self.__class__.crawl_count) + '.html'
            f = open(filename, "wb")
            f.write(response.body)
            f.close()
            filename1 = 'dUrl' + str(self.__class__.crawl_count) + '.txt'
            f1 = open(filename1, "w")
            f1.write(str(self.__class__.crawl_count) + " " + str(url))
            f1.close()
            print("save file done")


            #personal site
            links = response.css('a').getall()
            name = url.split('/')[-1].split('-')[0]
            for i in range(len(links)):
                if ("personal website" in links[i].lower()) \
                    or ("homepage" in links[i].lower())\
                    or ("personal site" in links[i].lower())\
                    or ("personal webpage" in links[i].lower()):
                        #or (name in links[i].lower()):
                    next_url = response.css('a')[i].attrib['href']
                    if next_url:
                            #print(next_url)
                        self.__class__.crawl_count += 1
                        print(self.__class__.crawl_count)
                        yield response.follow(next_url, callback=self.parse_person)

    def parse_person(self, response):
        url = response.url
        print(url)
        #filename = re.sub('/', '_', url)
        #filename = re.sub('__', '', filename)
        #filename = re.sub('http:', '', filename)
        filename = 'd' + str(self.__class__.crawl_count) + '.html'
        print(filename)
        f = open(filename, "wb")
        f.write(response.body)
        f.close()
        filename1 = 'dUrl' + str(self.__class__.crawl_count) + '.txt'
        f1 = open(filename1, "w")
        f1.write(str(self.__class__.crawl_count) + " " + str(url))
        f1.close()
        print("save file done")


