import unittest
from nameParser import NameParser

class NameParserTest(unittest.TestCase):
    def setUp(self):
        self.parse = NameParser("unwantedWords.txt")

    def test_parse_name_punctuation(self):
        self.assertEqual(self.parse.parse_name("lskdjf.lkj"), "Lskdjf")
        self.assertEqual(self.parse.parse_name("abcd ("), "Abcd ")

    def test_parse_name_capitalize(self):
        self.assertEqual(self.parse.parse_name("Testing Test"), "Testing Test")
        self.assertEqual(self.parse.parse_name("testing test"), "Testing Test")
        self.assertEqual(self.parse.parse_name("testing"), "Testing")
        self.assertEqual(self.parse.parse_name("t"), "T")
        self.assertEqual(self.parse.parse_name("te kd kj"), "Te Kd Kj")
        self.assertEqual(self.parse.parse_name("te Kd kj"), "Te Kd Kj")
        self.assertEqual(self.parse.parse_name("te 1kd kj"), "Te 1Kd Kj")


if __name__ == '__main__':
    unittest.main()
