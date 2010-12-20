#!/usr/bin/env python

import re
import urllib2
import csv
import sqlite3

import urllib2
from xml.dom import minidom


lines = []
stations = []
line_stations = []

class Line(object):
  def __init__(self):
    self._id = len(lines)
    self.name = ''
    self.shortname = ''
    self.code = ''
    self.tfl_id = -1 
    self.type = 'tube'
    self.colour = ''

  def get_name(self):
    return self.name

  def __repr__(self):
    return "%s (%s)" % (self.name, self.code)

class Station(object):
  def __init__(self):
    self._id = len(stations)
    self.name = ''
    self.code = ''
    self.lines = ''
    self.tfl_id = -1
    self.lat = -1
    self.lng = -1
    self.stepfree = 0

  def __repr__(self):
    return "%s (%s)" % (self.name, self.code)

class LineStation(object):
  def __init__(self):
    self._id = len(line_stations)
    self.line = ''
    self.station = ''

  def __repr__(self):
    return "%s: %s" % (self.station, self.line)


def create_lines():

  newline = Line()
  newline.name = 'Bakerloo'
  newline.shortname = 'bakerloo'
  newline.code = 'B'
  newline.tfl_id = 1
  newline.colour = '996633'
  lines.append(newline)

  newline = Line()
  newline.name = 'Central'
  newline.shortname = 'central'
  newline.code = 'C'
  newline.tfl_id = 2
  newline.colour = 'CC3333'
  lines.append(newline)
   
  newline = Line()
  newline.name = 'Circle'
  newline.shortname = 'circle'
  newline.code = 'H'
  newline.tfl_id = 7
  newline.colour = 'FFCC00'
  lines.append(newline)
   
  newline = Line()
  newline.name = 'District'
  newline.shortname = 'district'
  newline.code = 'D'
  newline.tfl_id = 9
  newline.colour = '006633'
  lines.append(newline)
   
  newline = Line()
  newline.name = 'Hammersmith & City'
  newline.shortname = 'hammersmith'
  newline.code = 'H'
  newline.tfl_id = 8
  newline.colour = 'CC9999'
  lines.append(newline)
   
  newline = Line()
  newline.name = 'Jubilee'
  newline.shortname = 'jubilee'
  newline.code = 'J'
  newline.tfl_id = 4
  newline.colour = '868F98'
  lines.append(newline)
   
  newline = Line()
  newline.name = 'Metropolitan'
  newline.shortname = 'metropolitan'
  newline.code = 'M'
  newline.tfl_id = 11
  newline.colour = '660066'
  lines.append(newline)
   
  newline = Line()
  newline.name = 'Northern'
  newline.shortname = 'northern'
  newline.code = 'N'
  newline.tfl_id = 5
  newline.colour = '000000'
  lines.append(newline)
   
  newline = Line()
  newline.name = 'Piccadilly'
  newline.shortname = 'piccadilly'
  newline.code = 'P'
  newline.tfl_id = 6
  newline.colour = '000099'
  lines.append(newline)

  newline = Line()
  newline.name = 'Victoria'
  newline.shortname = 'victoria'
  newline.code = 'V'
  newline.tfl_id = 3
  newline.colour = '0099CC'
  lines.append(newline)

  newline = Line()
  newline.name = 'Waterloo & City'
  newline.shortname = 'waterlooandcity'
  newline.code = 'W'
  newline.tfl_id = 12
  newline.colour = '66CCCC'
  lines.append(newline)

#  newline = Line()
#  newline.name = 'DLR'
#  newline.shortname = 'dlr'
#  newline.code = '25DLR'
#  newline.colour = '009999'
#  newline.type = 'dlr'
#  lines.append(newline)
#
#  newline = Line()
#  newline.name = 'Overground'
#  newline.shortname = 'overground'
#  newline.code = '90OVG'
#  newline.colour = 'FF6600'
#  newline.type = 'overground'
#  lines.append(newline)


def read_csv(filename):
  stations = []
  ifile  = open(filename, "rb")
  reader = csv.reader(ifile)

  rownum = 0
  for row in reader:
    if rownum == 0:
      header = row
    else:
      colnum = 0
      data = {}
      for col in row:
        data[header[colnum]] = col.strip()
        colnum += 1
      stations.append(data)
    rownum += 1

  ifile.close()

  return stations

  
def process_data():
  create_lines()

  stations_latlng = read_csv('stations_latlng.csv')

  # Match TfL ID from list of stations - done after the name changes
  url = "http://cloud.tfl.gov.uk/TrackerNet/StationStatus"
  index = urllib2.urlopen(urllib2.Request(url, None)).read()
  dom = minidom.parseString(index)


  for line in lines:
    newline = line
    print "Line:    %s" % newline

    if line.code == "waterloo":
      line.code = "waterlooandcity"
  
    if line.type == 'tube':
      url = "http://www.tfl.gov.uk/tfl/livetravelnews/departureboards/tube/default.asp?LineCode=%s" % line.shortname
      index = urllib2.urlopen(urllib2.Request(url, None)).read()
      station_list = re.findall(r'<option value="([a-zA-Z]{3})">(.*)</option>', index)

    #if line.type == 'dlr':
    #  url = "http://dlr-scripts.appius.com/lib/redirect_daisy.asp?daisy=&go=Go"
    #  index = urllib2.urlopen(urllib2.Request(url, None)).read()
    #  # <a href="?daisy=als&desc=All_Saints" title="All Saints">All Saints</a><br>
    #  station_list = re.findall(r'<a href="\?daisy=([a-zA-Z]{3})&desc=.*" title=".*">(.*)</a><br>', index)
  
    for station in station_list:
      exist = None
        
      for exist_station in stations:
        if exist_station.code == station[0]:
          exist = exist_station
  
      if (exist is None):
        newstation = Station()

        newstation.name = station[1]
        newstation.code = station[0]

        # Remove whitespace either side of the name
        newstation.name = newstation.name.rstrip()	

        # Match Lat Lng from CSV file
        for match in stations_latlng:
          if newstation.name == match['name']:
            newstation.lat = match['lat']
            newstation.lng = match['lng']

        # One off fixes for station names - only in the stations_latlng file
        if newstation.name == "Earls Court":
          newstation.name = "Earl's Court"
        if newstation.name == "Totteridge and Whetstone":
          newstation.name = "Totteridge & Whetstone"

		# Get the TfL ID
        for node in dom.getElementsByTagName('StationStatus'):
            stationID = node.getAttribute('ID')
            stationElement = node.getElementsByTagName('Station')[0]
            stationName = stationElement.getAttribute('Name')

            # Name fixes so the list from the TfL Departures pages
            # match the names from the TrackerNet feed
            if stationName == "Edgware Road (Bak)":
                stationName = "Edgware Road (Bakerloo)"

            if stationName == "Harrow-on-the-Hill":
                stationName = "Harrow on the Hill"

            if stationName == "Hammersmith (H&C)":
                stationName = "Hammersmith (Circle and H&C)"

            if stationName == "Hammersmith (Dis)":
                stationName = "Hammersmith (District and Picc)"

            if stationName == "Kensington (Olympia)":
                stationName = "Olympia"

            if stationName == "Shepherd's Bush (Cen)":
                stationName = "Shepherds Bush (Central Line)"

            if stationName == newstation.name:
                newstation.tfl_id = stationID	


        # Remove the (Line) part from the Name
        newstation.name = newstation.name.split(" (")[0]

        # Olympia needs a fix, but only after the split from "("
        if newstation.name == "Olympia":
          newstation.name = "Kensington (Olympia)"

        newstation._id = len(stations)
        stations.append(newstation)
  
        line_station = LineStation()
        line_station.line = newline._id
        line_station.station = newstation._id
        line_station._id = len(line_stations)
        line_stations.append(line_station)
        print "Adding:   %s to %s" % (newstation.name, newline.name)  
      else:
        line_station = LineStation()
        line_station.line = newline._id
        line_station.station = exist._id
        line_station._id = len(line_stations)
        line_stations.append(line_station)
        print "Existing: %s to %s" % (exist.name, newline.name)


  # Build a list of Lines a station has - to save DB transaactions later
  for station in stations:
    lines_list = []
    for line_station in line_stations:
       if station._id == line_station.station:
         lines_list.append(lines[line_station.line])

    # Either use ',' or 'and' depending on list position
    lines_string = ""
    for i,line in enumerate(lines_list):
      lines_string += line.name
      if (i < len(lines_list)-2):
        lines_string += ", "
      elif (i == len(lines_list)-2):
        lines_string += " and "

    # End with 'Lines' or 'Line'
    if (len(lines_list) > 1):
      lines_string += " Lines"
    else:
      lines_string += " Line"

    station.lines = lines_string


def sql_transaction(conn, sql):
  print "SQL: %s" % sql
  conn.execute(sql);


def do_sql():

  conn = sqlite3.connect('tubechaser.db')
  c = conn.cursor()

  sql_transaction(c,"BEGIN TRANSACTION;")
  sql_transaction(c,"CREATE TABLE 'android_metadata' ('locale' TEXT DEFAULT 'en_US');")
  sql_transaction(c,"INSERT INTO 'android_metadata' VALUES('en_US');")

  # Lines table
  sql_transaction(c,"CREATE TABLE 'lines' (_id INTEGER PRIMARY KEY, name VARCHAR, shortname VARCHAR, code VARCHAR, tfl_id INTEGER, type VARCHAR, colour VARCHAR, status TEXT, status_desc TEXT, status_code VARCHAR, status_class VARCHAR);")

  # Stations table
  sql_transaction(c,"CREATE TABLE 'stations' (_id INTEGER PRIMARY KEY, name VARCHAR, code VARCHAR, lines TEXT, tfl_id INTEGER, latitude REAL, longitude REAL, status TEXT, status_desc TEXT, status_code VARCHAR, stepfree INTEGER);")

  # Line-Stations table
  sql_transaction(c,"CREATE TABLE 'lines_stations' (_id INTEGER PRIMARY KEY, line_id INTEGER, station_id INTEGER);")

  # Lines
  print "Writing lines... %s" % len(lines)
  for line in lines:
    sql_transaction(c,"""INSERT INTO lines(_id, name, shortname, code, tfl_id, type, colour) VALUES ("%s","%s","%s","%s","%s","%s","%s");""" %
      (line._id, line.name, line.shortname, line.code, line.tfl_id, line.type, line.colour))


  # Stations
  print "Writing stations... %s" % len(stations)
  for station in stations:
    sql_transaction(c,"""INSERT INTO stations(_id, name, code, lines, tfl_id, latitude, longitude, stepfree) VALUES ("%s","%s","%s","%s","%s","%s","%s","%s");""" %
      (station._id, station.name, station.code, station.lines, station.tfl_id, station.lat, station.lng, station.stepfree))


  # Line Stations
  print "Writing line_stations... (%s)" % len(line_stations)
  for line_station in line_stations:
    sql_transaction(c,"""INSERT INTO lines_stations (_id, line_id, station_id) VALUES ("%s","%s","%s");""" %
      (line_station._id, line_station.line, line_station.station))

  # Commit the changes and close everything.
  conn.commit()
  c.close()
  conn.close()


  
if __name__ == "__main__":
  process_data()  
  do_sql()
  

