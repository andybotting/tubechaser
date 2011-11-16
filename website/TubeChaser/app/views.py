from django.http import HttpResponseRedirect
from django.http import HttpResponse
from django.http import Http404 
from django.shortcuts import render_to_response
from django.template import RequestContext
from django.views.decorators.csrf import csrf_exempt

from google.appengine.runtime import DeadlineExceededError

from datetime import date,timedelta,datetime
import re
import logging
from GChartWrapper import Pie3D,Note
from gheatae import tile

from django.conf import settings

import forms
import models
import tile_utils
import chart_utils
import utils

PERIODS = ['week', 'month']

""" Index page
"""
def index(request):
	
	return render_to_response("index.html",
		{},
		context_instance = RequestContext(request))

""" Heat Map
"""
def heat_map(request):
	return render_to_response("map.html", {
			'key': settings.GMAPS_KEY,
			'bounds_north': settings.BOUNDS_NORTH,
			'bounds_west': settings.BOUNDS_WEST,
			'bounds_south': settings.BOUNDS_SOUTH,
			'bounds_east': settings.BOUNDS_EAST,
			'default_lat': settings.DEFAULT_LAT,
			'default_lng': settings.DEFAULT_LNG,
			'default_zoom': settings.DEFAULT_ZOOM,
			'min_zoom': settings.MIN_ZOOM,
			'max_zoom': settings.MAX_ZOOM,
		}, 
		context_instance = RequestContext(request))

""" Departure
"""
def requested_departure(request):
	points = models.DepartureStat.objects.all()

""" Departure Stats POST handler
"""
@csrf_exempt
def departure_stat_send(request):
	if request.method == 'POST':
		form = forms.DepartureStatForm(request.POST)
		if form.is_valid():

			# Get any previous entries from this device
			previous_stats = models.DepartureStat.objects.filter(device_id=form.data['device_id'])
			now = datetime.now()
			for prev in previous_stats:
				time_diff = now - prev.timestamp
				if time_diff < timedelta(minutes=10):
					# Last update is less than 10 mins ago, we return and not save
					logging.info("Form error: last entry was less 10 mins ago (%s)" % utils.elapsed_time(time_diff.seconds))
					return HttpResponse("ERR\n", status=405)
				
			# No entries less an 1 hour old, so save this one
			stat = form.save()
			logging.info("Form OK: %s" % form.data)
			return HttpResponse("OK\n")
		else:
			logging.info("Form error: %s" % form.errors)
			return HttpResponse("ERR\n", status=405)

	return HttpResponse("")

""" App Stats POST handler
"""
@csrf_exempt
def app_stat_send(request):
	if request.method == 'POST':
		form = forms.AppStatForm(request.POST)
		if form.is_valid():
			stat = form.save()
			logging.info("Form OK: %s" % form.data)
			return HttpResponse("OK\n")
		else:
			logging.info("Form error: %s" % form.errors)
			return HttpResponse("ERR\n")

		def clean_device_id(self):
			device_id = self.cleaned_data['device_id']
			exp = re.findall(r"([a-fA-F\d]{32})", data)
			if not re.search(exp, device_id):
				raise forms.ValidationError("Invalid device_id")
			return device_id
		
	return HttpResponse("")

def generate_tiles_job(request):
	tile_utils.generate_tiles_job()
	return HttpResponse("OK\n")

""" Tile generation handler
"""
def generate_tile(request):
	url = request.path.split('/')[-1] # Get the url of the image
	img = tile_utils.make_tile(url)

	if img:
		tile_utils.save_tile(img, url)
		return HttpResponse("OK\n")
	else:
		return HttpResponse("ERR\n")

""" Tile handler
"""
def heat_tile(request):
	if request.path.endswith('.png'):
		filename = request.path[:-4] # strip extension
		url = filename.split('/')[-1] # Get the url of the image

		# Get the tile
		img_data = tile_utils.get_or_generate_tile(url)

		# Return tile
		response = HttpResponse(img_data, mimetype='image/png')
		#response['Cache-Control'] = 'public; max-age=3600' # Cache for 1 day
		return response
	else:
		# Invalid url
		raise Http404

""" Chart list
"""
def list_charts(request):

	charts = {  'app_version': 'Application Version',
				'home_function': 'Home Function',
				'device_model': 'Device Model',
				'device_version': 'Device Version',
				'device_language': 'Device Language',
				'mobile_country_code': 'Mobile Country Code',
				'mobile_network_number': 'Mobile Network Number',
				'mobile_network_type': 'Mobile Network Type',
			}
	
	return render_to_response('list_charts.html', {
			'charts': charts 
		},
		context_instance=RequestContext(request))


""" Chart handler
"""
def make_chart(request, type, days='week'):

	if days not in PERIODS:
		raise Http404

	charts = {  'app_version': 'Application Version',
				'home_function': 'Home Function',
				'device_model': 'Device Model',
				'device_version': 'Device Version',
				'device_language': 'Device Language',
				'mobile_country_code': 'Mobile Country Code',
				'mobile_network_number': 'Mobile Network Number',
				'mobile_network_type': 'Mobile Network Type',
			}
	
	app_stats = models.AppStat.objects.all()

	# If no results
	if len(app_stats) < 1:
		raise Http404  

	# If invalid type
	if not hasattr(app_stats[0], type):
		raise Http404  

	# If we want last week, or month
	today = date.today()
	if days == "week":
		date_diff = today - timedelta(days=7)
		app_stats = app_stats.filter(timestamp__gt=date_diff)
	elif days == "month":
		date_diff = today - timedelta(days=30)
		app_stats = app_stats.filter(timestamp__gt=date_diff)
	else:
		days = "all"

	# Build chart
	labels = None
	if type == 'mobile_network_number':
		mobile_ops = models.MobileOperator.objects.all()
		labels = []
		for op in mobile_ops:
			labels.append(op.getCode())	

	chart_data = chart_utils.make_gchart_data(app_stats, type, labels)

	if len(chart_data['data']) < 1:
		chart = Note('note_title','pinned_c',1,'000000','l', "No Results") 
	else:
		chart = Pie3D(chart_data['data'])
		chart.color(*chart_data['colours'])
		chart.size(600,300)
		chart.legend(*chart_data['labels'])

	return render_to_response('chart.html', {
			'request': request,
			'chart': chart,
			'time': days,
			'name': charts[type],
			'title': "%s for %s (%s)" % (charts[type], days, chart_data['total']),
			'base_url': "/stats/chart/%s" % type,
		},
		context_instance=RequestContext(request))

