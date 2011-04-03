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

def read_csv(filename):
  output_data = []
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
      output_data.append(data)
    rownum += 1

  ifile.close()
  return output_data

def sql_transaction(conn, sql):
  print "SQL: %s" % sql
  conn.execute(sql);

def do_sql():

  lines = read_csv('lines.csv')
  stations = read_csv('stations.csv')
  line_stations = read_csv('line_stations.csv')


  conn = sqlite3.connect('tubechaser.db')
  c = conn.cursor()

  sql_transaction(c,"BEGIN TRANSACTION;")
  sql_transaction(c,"PRAGMA user_version = 4;")
  sql_transaction(c,"CREATE TABLE 'android_metadata' ('locale' TEXT DEFAULT 'en_US');")
  sql_transaction(c,"INSERT INTO 'android_metadata' VALUES('en_US');")

  # Lines table
  sql_transaction(c,"CREATE TABLE 'lines' (_id INTEGER PRIMARY KEY, name VARCHAR, shortname VARCHAR, code VARCHAR, tfl_id INTEGER, type INTEGER, colour VARCHAR, status TEXT, status_desc TEXT, status_code VARCHAR, status_class VARCHAR);")

  # Stations table
  sql_transaction(c,"CREATE TABLE 'stations' (_id INTEGER PRIMARY KEY, name VARCHAR, lines TEXT, tfl_id INTEGER, latitude REAL, longitude REAL, status TEXT, status_desc TEXT, status_code VARCHAR, stepfree INTEGER, line_ids VARCHAR);")

  # Line-Stations table
  sql_transaction(c,"CREATE TABLE 'lines_stations' (_id INTEGER PRIMARY KEY, line_id INTEGER, station_id INTEGER, station_code VARCHAR);")

  # Lines
  print "Writing lines... %s" % len(lines)
  for line in lines:
    sql_transaction(c,"""INSERT INTO lines(_id, name, shortname, code, tfl_id, type, colour) VALUES ("%s","%s","%s","%s","%s","%s","%s");""" %
      (line['_id'], line['name'], line['shortname'], line['code'], line['tfl_id'], line['type'], line['colour']))


  # Stations
  print "Writing stations... %s" % len(stations)
  for station in stations:
    sql_transaction(c,"""INSERT INTO stations(_id, name, lines, tfl_id, latitude, longitude, stepfree, line_ids) VALUES ("%s","%s","%s","%s","%s","%s","%s","%s");""" %
      (station['_id'], station['name'], station['lines'], station['tfl_id'], station['latitude'], station['longitude'], station['stepfree'], station['line_ids']))



  # Line Stations
  print "Writing line_stations... (%s)" % len(line_stations)
  for line_station in line_stations:
    sql_transaction(c,"""INSERT INTO lines_stations (_id, line_id, station_id, station_code) VALUES ("%s","%s","%s","%s");""" %
      (line_station['_id'], line_station['line_id'], line_station['station_id'], line_station['station_code']))

  # Commit the changes and close everything.
  conn.commit()
  c.close()
  conn.close()


  
if __name__ == "__main__":
  do_sql()
  

