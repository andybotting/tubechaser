from django.db import models
from google.appengine.ext import db

class Tile(db.Model):
	url = db.StringProperty()
	img = db.BlobProperty()
	timestamp = db.DateTimeProperty()


class DepartureStat(models.Model):
	device_id = models.CharField(max_length=32)
	station_id = models.IntegerField()
	line_id = models.IntegerField()
	latitude = models.FloatField(null=True, blank=True)
	longitude = models.FloatField(null=True, blank=True)
	accuracy = models.FloatField(null=True, blank=True)
	timestamp = models.DateTimeField(auto_now_add=True)

	def __str__(self):
		return "Device %s at %s" % (self.device_id, self.timestamp)

	def __unicode__(self):
		return "Device %s at %s" % (self.device_id, self.timestamp)

	class Admin:
		ordering = ('device_id','timestamp')


class AppStat(models.Model):
	device_id = models.CharField(max_length=32)
	app_version = models.CharField(max_length=10)
	home_function = models.CharField(max_length=32)
	device_model = models.CharField(max_length=14, null=True, blank=True)
	device_version = models.CharField(max_length=14)
	device_language = models.CharField(max_length=10)
	mobile_country_code = models.CharField(max_length=2, null=True, blank=True)
	mobile_network_number = models.CharField(max_length=10, null=True, blank=True)
	mobile_network_type = models.CharField(max_length=14, null=True, blank=True)
	timestamp = models.DateTimeField(auto_now_add=True)

	def __str__(self):
		return "Device %s at %s" % (self.device_id, self.timestamp)
	def __unicode__(self):
		return "Device %s at %s" % (self.device_id, self.timestamp)

	def get_fields(self):
	# make a list of field/values.
		return [(field, field.value_to_string(self)) for field in self._meta.fields]

	class Admin:
		ordering = ('device_id','timestamp')

class Line(models.Model):
	name = models.CharField(max_length=64)
	shortname = models.CharField(max_length=32)
	code = models.CharField(max_length=10)
	type = models.CharField(max_length=10)
	colour = models.CharField(max_length=6)

	def __str__(self):
		return "Line %s" % (self.name)
	def __unicode__(self):
		return "Line %s" % (self.name)
	class Admin:
		pass
	
class Station(models.Model):
	name = models.CharField(max_length=32)
	code = models.CharField(max_length=32)
	lines = models.CharField(max_length=256)
	latitude = models.FloatField(null=True, blank=True)
	longitude = models.FloatField(null=True, blank=True)
	tflid = models.CharField(max_length=10)

	def __str__(self):
		return "Station %s" % (self.name)
	def __unicode__(self):
		return "Station %s" % (self.name)
	class Admin:
		pass
	

class LineStation(models.Model):
	line = models.ForeignKey(Line)
	station = models.ForeignKey(Station)

	def __str__(self):
		return "Line Station (%s %s)" % (self.line.name, self.station.name)
	def __unicode__(self):
		return "Line Station (%s %s)" % (self.line.name, self.station.name)
	class Admin:
		pass
	

class MobileOperator(models.Model):
	mmc = models.CharField(max_length=4)
	mnc = models.CharField(max_length=4)
	operator = models.CharField(max_length=64)

	def __str__(self):
		return "Operator %s (%s %s)" % (self.operator, self.mmc, self.mnc)
	def __unicode__(self):
		return "Operator %s (%s %s)" % (self.operator, self.mmc, self.mnc)
	class Admin:
		pass
	
	def getCode(self):
		code = "%s%s" % (self.mmc, self.mnc)
		return (code, self.operator)
