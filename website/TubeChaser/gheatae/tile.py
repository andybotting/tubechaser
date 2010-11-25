from gheatae import color_scheme
from pngcanvas import PNGCanvas
from random import random, Random
import logging
import gmerc
import math
from datetime import datetime

#import Image
from google.appengine.api import images

from app.models import DepartureStat

log = logging.getLogger('space_level')

# Variables
DOT_MULT = 3		# Radius of dots
LEVEL_MAX = 300		# Max level of something
WEIGHT = 1 		# Weight of each point
COLOR_SCHEME = 0	# Gradient colour scheme

rdm = Random()

class BasicTile(object):
  def __init__(self, lat_north, lng_west, range_lat, range_lng):
    self.level_max = LEVEL_MAX
    self.color_scheme = color_scheme.color_schemes[color_scheme.color_schemes.keys()[COLOR_SCHEME]]

    self.cache_levels = []
    for i in range(self.level_max - 1, -1, -1):
      self.cache_levels.append(int(((-(pow(float(i) - self.level_max, 2))/self.level_max) + self.level_max) / self.level_max * 255))

    lat_south = min(90, max(-90, lat_north + range_lat))
    lng_east = min(180, max(-180, lng_west + range_lng))

    points_query = DepartureStat.objects.all().order_by('-timestamp')[:1000]

    points = []
    for point in points_query:
      if (point.latitude <= lat_north) and (point.latitude >= lat_south) and (point.longitude >= lng_west) and (point.longitude <= lng_east):
        points.append(point)
    self.tile_img = self.plot_image(points)

  def plot_image(self, points):
    space_level = self.__create_empty_space()
    start = datetime.now()
    for i, point in enumerate(points):
      if point.latitude:
        self.__merge_point_in_space(space_level, point)
    return self.convert_image(space_level)

  def __merge_point_in_space(self, space_level, point):
    dot_levels = []
    rad = int(self.zoom * DOT_MULT)
    for i in range(rad * 2):
      dot_levels.append([0.] * (rad * 2))
    y_off = int(math.ceil((-1 * self.northwest_ll[0] + float(point.latitude)) / self.latlng_diff[0] * 256. - rad))
    x_off = int(math.ceil((-1 * self.northwest_ll[1] + float(point.longitude)) / self.latlng_diff[1] * 256. - rad))
    for y in range(y_off, y_off + (rad * 2)):
      if y < 0 or y >= len(space_level):
        continue
      for x in range(x_off, x_off + (rad * 2)):
        if x < 0 or x >= len(space_level[0]):
          continue
        y_adj = math.pow((y - rad - y_off), 2)
        x_adj = math.pow((x - rad - x_off), 2)
        pt_rad = math.sqrt(y_adj + x_adj)
        temp_rad = rad
        if pt_rad > temp_rad:
          continue
        space_level[y][x] += self.calc_point(rad, pt_rad)

  def scale_value(self, value):
    #ret_float = math.log(max((value + 50) / 50, 1), 1.01) + 30
    #ret_float = math.log(max((value + 30) / 40, 1), 1.01) + 30
    #ret_float = math.log(max((value + 40) / 20, 1), 1.01)
    ret_float = math.log(max(value, 1), 1.1) * 4
    return int(ret_float)

  def convert_image(self, space_level):
    tile = PNGCanvas(len(space_level[0]), len(space_level), bgcolor=[0xff,0xff,0xff,0])
    temp_color_scheme = []
    for i in range(self.level_max):
      temp_color_scheme.append(self.color_scheme.canvas[self.cache_levels[i]][0])
    for y in xrange(len(space_level[0])):
      for x in xrange(len(space_level[0])):
        if len(temp_color_scheme) > 0:
          tile.canvas[y][x] = [int(e) for e in temp_color_scheme[max(0, min(len(temp_color_scheme) - 1, self.scale_value(space_level[y][x])))]]
        else:
          tile.canvas[y][x] = [0,0,0,0]
    return tile

  def calc_point(self, rad, pt_rad):
    max_alpha = 100
    fraction = (rad - pt_rad) / rad
    return max_alpha * math.pow(fraction, math.pow(WEIGHT, 0.25)) # Andy: Weight forced to 1 - refactor?
    #return max_alpha * math.pow(fraction, 1) # Andy: Weight forced to 1 - refactor?

  def __create_empty_space(self):
    space = []
    for i in range(256):
      space.append( [0.] * 256 )
    return space

  def image_out(self):
    if self.tile_img:
      self.tile_dump = self.tile_img.dump()

    if self.tile_dump:
      return self.tile_dump
    else:
      raise Exception("Failure in generation of image.")

class CustomTile(BasicTile):
  def __init__(self, zoom, lat_north, lng_west, offset_x_px, offset_y_px):
    self.zoom = zoom
    self.decay = 0.5
    #dot_radius = int(math.ceil(len(dot[self.zoom]) / 2))
    dot_radius = int(math.ceil((self.zoom + 1) * DOT_MULT)) #TODO double check that this is + 1 - because range started from 1 in old dot array?!

    # convert to pixel first so we can factor in the dot radius and get the tile bounds
    northwest_px = gmerc.ll2px(lat_north, lng_west, zoom)

    self.northwest_ll_buffered = gmerc.px2ll(northwest_px[0] + offset_x_px       - dot_radius, northwest_px[1] + offset_y_px       - dot_radius, zoom)
    self.northwest_ll          = gmerc.px2ll(northwest_px[0] + offset_x_px                   , northwest_px[1] + offset_y_px                   , zoom)

    self.southeast_ll_buffered = gmerc.px2ll(northwest_px[0] + offset_x_px + 256 + dot_radius, northwest_px[1] + offset_y_px + 256 + dot_radius, zoom)
    self.southeast_ll          = gmerc.px2ll(northwest_px[0] + offset_x_px + 256             , northwest_px[1] + offset_y_px + 256             , zoom) # THIS IS IMPORTANT TO PROPERLY CALC latlng_diff

    self.latlng_diff_buffered = [ self.southeast_ll_buffered[0] - self.northwest_ll_buffered[0], self.southeast_ll_buffered[1] - self.northwest_ll_buffered[1]]
    self.latlng_diff          = [ self.southeast_ll[0]          - self.northwest_ll[0]         , self.southeast_ll[1]          - self.northwest_ll[1]]

    BasicTile.__init__(self, self.northwest_ll_buffered[0], self.northwest_ll_buffered[1], self.latlng_diff_buffered[0], self.latlng_diff_buffered[1])


class GoogleTile(BasicTile):
  def __init__(self, zoom, x_tile, y_tile):
    self.zoom = zoom
    self.decay = 0.5
    #dot_radius = int(math.ceil(len(dot[self.zoom]) / 2))
    dot_radius = int(math.ceil((self.zoom + 1) * DOT_MULT))

    self.northwest_ll_buffered = gmerc.px2ll((x_tile    ) * 256 - dot_radius, (y_tile    ) * 256 - dot_radius, zoom)
    self.northwest_ll          = gmerc.px2ll((x_tile    ) * 256             , (y_tile    ) * 256             , zoom)

    self.southeast_ll_buffered = gmerc.px2ll((x_tile + 1) * 256 + dot_radius, (y_tile + 1) * 256 + dot_radius, zoom) #TODO fix this in case we're at the edge of the map!
    self.southeast_ll          = gmerc.px2ll((x_tile + 1) * 256             , (y_tile + 1) * 256             , zoom)

    # calculate the real values for these without the offsets, otherwise it messes up the __merge_point_in_space calculations
    self.latlng_diff_buffered = [ self.southeast_ll_buffered[0] - self.northwest_ll_buffered[0], self.southeast_ll_buffered[1] - self.northwest_ll_buffered[1]]
    self.latlng_diff          = [ self.southeast_ll[0]          - self.northwest_ll[0]         , self.southeast_ll[1]          - self.northwest_ll[1]]

    BasicTile.__init__(self, self.northwest_ll_buffered[0], self.northwest_ll_buffered[1], self.latlng_diff_buffered[0], self.latlng_diff_buffered[1])
