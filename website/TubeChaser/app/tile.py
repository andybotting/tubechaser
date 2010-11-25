from google.appengine.api.labs import taskqueue
from google.appengine.ext import db
from google.appengine.runtime import DeadlineExceededError

from django.conf import settings
from django.http import HttpResponse

from datetime import date, datetime, timedelta

from gheatae import tile

import utils
import models
import views
import logging
import math

# Python 2.5 does not have inverse hyporbolic functions
try:
	a = math.atanh(0)
except AttributeError:
	# No such functions? No probs, we can emulate them
	math.asinh = lambda x: math.log(x + math.sqrt(x*x + 1.0))
	math.atanh = lambda x: 0.5 * math.log((1.0 + x) / (1.0 - x))


# Calculates tile coordinates from lat/lon and zoomlevel
def tileXY(latitude, longitude, zoom):
	x = longitude * math.pi / 180.0;
	y = math.asinh(math.tan(latitude * math.pi / 180.0))

	# In Google Maps projected sphere is the square [-pi..pi]x[-pi..pi]
	# This square is then divided on (2^zoom)x(2^zoom) tiles. Tile
	# coordinates are counting from upper left corner of the grid.
	x = (x / math.pi / 2.0 + 0.5) * 2**zoom
	y = (-y/ math.pi / 2.0 + 0.5) * 2**zoom
	return (int(x), int(y))


def tile_url(tileX, tileY, zoom):
	return "%d_%d_%d" % (zoom, tileY, tileX)

# For rectangle in lat/lon coordinates returns corresponding
# tile coordinates in form ((x1, y1), (x2, y2))
# This function will rearrange coordinates, so
#	 x1 <= x2 and y1 <= y2
# Zoom is by TileProvider's counting
def tile_rectangle(lat1, lon1, lat2, lon2, zoom):
	pn1 = tileXY(lat1, lon1, zoom)
	pn2 = tileXY(lat2, lon2, zoom)

	# Rearranging coordinates so p1 is top-left corner and p2 is bottom-right one
	p1 = (min(pn1[0], pn2[0]), min(pn1[1], pn2[1]))
	p2 = (max(pn1[0], pn2[0]), max(pn1[1], pn2[1]))

	return (p1, p2)


def get_or_generate_tile(url):
	# Get tile from cache
	tile = get_tile(url)

	# If we have a tile, check timestamp
	if tile:
		now = datetime.now()
		generated = tile.timestamp
		timediff = utils.elapsed_time((now-generated).seconds)

		if (now - generated) > timedelta(days=2):
			logging.info("Tile %s expired %s ago" % (url, timediff))
		else:
			logging.info("Tile %s found in store that is %s old" % (url, timediff))
			return tile.img

	# Fall back to generate a new tile
	img = make_tile(url)

	save_tile(img, url)
	return img


def get_tile(url):
	tile = db.GqlQuery("SELECT * FROM Tile WHERE url = :1 LIMIT 1", url).fetch(1)
	if tile:
		return tile[0]
	return None


def generate_tiles_job():
	for zoom in range(settings.MIN_ZOOM, settings.MAX_ZOOM+1):
		(p1, p2) = tile_rectangle(
				settings.BOUNDS_NORTH,
				settings.BOUNDS_WEST,
				settings.BOUNDS_SOUTH,
				settings.BOUNDS_EAST,
				zoom)

		for x in range(p1[0], p2[0] + 1):
			for y in range(p1[1], p2[1] + 1):
				url = tile_url(x, y, zoom)
				logging.info("Adding %s to task queue" % url)
				taskqueue.add(url='/generate/%s' % url, method="GET")



def save_tile(img, url):
	tile = get_tile(url)

	if tile:
		logging.info("Updating tile %s in store" % tile.url)
	else:		
		logging.info("Creating new tile %s in store" % url)
		tile = models.Tile()

	tile.img = img
	tile.url = url
	tile.timestamp = datetime.now()
	tile.save()


def make_tile(url):
	# Parse our values
	try:
		zoom, y, x = url.split('_')
		assert zoom.isdigit() and x.isdigit() and y.isdigit(), "not digits"
		zoom = int(zoom)
		x = int(x)
		y = int(y)
		assert settings.MIN_ZOOM <= zoom <= settings.MAX_ZOOM, "bad zoom: %d" % zoom
	except AssertionError, err:
		logging.error(err.args[0])

	# Generate the tile
	try:
		then = datetime.now()
		logging.info("Generating tile %d_%d_%d" % (zoom, y, x))

		# Generate
		new_tile = tile.GoogleTile(zoom, x, y)
		img_data = new_tile.image_out()

		# Timing
		now = datetime.now()
		timediff = utils.elapsed_time((now-then).seconds)
		logging.info("Generating tile %d_%d_%d finished %s" % (zoom, y, x, timediff) )
		return img_data

	except DeadlineExceededError, err:
		logging.warning('%s error - failed at %s' % (str(err), datetime.now()))

	return None

