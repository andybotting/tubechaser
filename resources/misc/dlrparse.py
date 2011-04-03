#!/usr/bin/env python

from BeautifulSoup import BeautifulSoup
import urllib2
import re

station = "caw"
page = urllib2.urlopen("http://www.dlrdaisy.co.uk/daisy/%s/%s06pid.htm" % (station, station))
soup = BeautifulSoup(page)

class DepartureBoard(object):
  def __init__(self):
    pass

class Platform(object):
  def __init__(self):
    pass
  def __repr__(self):
    return self.number

class NextDeparture(object):
  def __init__(self):
    pass
  def __repr__(self):
    return "%s: %s" % (self.destination, self.time)

def parse_line(line):
  match = re.search("(\d+)&nbsp;(\w+)&nbsp;.*&nbsp;(\d+)&nbsp;", line)
  if (match):
    next_departure = NextDeparture()
    next_departure.destination = match.group(2)
    next_departure.time = match.group(3)
    return next_departure


boards = soup.findAll("div", id="ttbox")
departureBoard = DepartureBoard()

for board in boards:
  platformString = str(board.find("div", id="platformleft").firstText())
  plat_match = re.search("""<img src="p(\d)l.gif" """, platformString)
  if plat_match:
    platform = Platform()
    platform.number = plat_match.group(1)

    line1str = board.find("div", id="line1").string
    line1 = parse_line(line1str)
    if (line1):
      print line1

    line23str = board.find("div", id="line23").firstText()
    line23str = str(line23str).split("<br />")
    for linestr in line23str:
       line = parse_line(linestr)
       if line:
         print line
    

