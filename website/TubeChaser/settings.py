try:
    from djangoappengine.settings_base import *
    has_djangoappengine = True
except ImportError:
    has_djangoappengine = False
    DEBUG = True
    TEMPLATE_DEBUG = DEBUG

import os

SECRET_KEY = ''

INSTALLED_APPS = (
    'app',
    'djangotoolbox',
    'django.contrib.auth',
    'django.contrib.contenttypes',
    'django.contrib.sessions',
    'django.contrib.admin',
    'GChartWrapper.charts'
)

TEMPLATE_CONTEXT_PROCESSORS = (
	'django.core.context_processors.media',
	'django.core.context_processors.request',
	'django.contrib.auth.context_processors.auth',
)

if has_djangoappengine:
    INSTALLED_APPS = ('djangoappengine',) + INSTALLED_APPS

ADMIN_MEDIA_PREFIX = '/media/admin/'
MEDIA_ROOT = os.path.join(os.path.dirname(__file__), 'media')
TEMPLATE_DIRS = (os.path.join(os.path.dirname(__file__), 'templates'),)

# Media URL
MEDIA_URL = "/static/" 

ROOT_URLCONF = 'urls'

# Map settings
MIN_ZOOM = 10
MAX_ZOOM = 14

# Set tile boundaries
BOUNDS_NORTH = 51.59
BOUNDS_WEST = -0.36
BOUNDS_SOUTH = 51.45
BOUNDS_EAST = 0.11

# Default map
DEFAULT_LAT = 51.51
DEFAULT_LNG =  -0.11
DEFAULT_ZOOM = 11

# tubechaser.andybotting.com
GMAPS_KEY = 'ABQIAAAAFwbW-uKyqw5AXMK3-I2bmxQomtdMZDbsI44l4IOQHqCMWcJZ2hQqb_Ky1Ag66pKB-UDVUYp2lnNRSw'

