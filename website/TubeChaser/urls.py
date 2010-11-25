from django.conf.urls.defaults import *
from django.views.generic.simple import direct_to_template
from app import views


from django.contrib import admin
admin.autodiscover()

urlpatterns = patterns('',
	# Stat forms
	(r'^stats/depart/send$', views.departure_stat_send),
	(r'^stats/app/send$', views.app_stat_send),

	# Charts
    (r'^stats/$', direct_to_template, {'template': 'stats.html'}),
	(r'^stats/chart/$', views.list_charts),
	(r'^stats/chart/(?P<type>\w+)/(?P<days>\w+)$', views.make_chart),
	(r'^stats/chart/(?P<type>\w+)/$', views.make_chart),

	# Map
	(r'^stats/map/', views.heat_map),
	(r'^map/', views.heat_map),

	# Tiles
	(r'^tile/', views.heat_tile),
	(r'^generate/', views.generate_tile),
	(r'^generate_tiles_job_I2bmxS2RAfVmt7C3DBeNHh75Ug/', views.generate_tiles_job),
	
	# Rest
    (r'^about/$', direct_to_template, {'template': 'about.html'}),
    (r'^news/$', direct_to_template, {'template': 'news.html'}),
    (r'^screenshots/$', direct_to_template, {'template': 'screenshots.html'}),
    (r'^install/$', direct_to_template, {'template': 'install.html'}),
    (r'^source/$', direct_to_template, {'template': 'source.html'}),
    (r'^privacy/$', direct_to_template, {'template': 'privacy.html'}),
	(r'^admin/', include(admin.site.urls)),
	(r'^$', views.index),
)

