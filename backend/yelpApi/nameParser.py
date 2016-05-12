import re
import string

# Take in a list of words that will be rejected
# List of word should be sorted for faster lookups
# Return some flag if string is parsed or rejected
class NameParser(object):
    def __init__(self, dictionaryFile):
        with open(dictionaryFile) as file:
            self.words = file.read().split(",")

    #  @profile
    def parse_name(self, name):
        trim = re.search("[-!#./():$]", name)
        if trim:
            pretty = (name[0:trim.start()])
        else:
            pretty = (name)

        #Capitalize first letter in word
        pretty = pretty.title()

        pretty_split_words = pretty.split()

        #if only 1 or less word reject
        if(len(pretty_split_words)) <= 1:
            return None

        #Assume that comments are usually shorter and list of rejected words
        #This algorithm breaks comments into word then search the dictionary
        for prettyWord in pretty_split_words:
            if prettyWord.lower() in self.words:
                #These are rejected and should not be used
                return None

        return pretty
