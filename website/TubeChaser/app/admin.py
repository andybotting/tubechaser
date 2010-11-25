from django.contrib import admin
from app.models import DepartureStat
from app.models import AppStat
from app.models import Line
from app.models import Station
from app.models import LineStation
from app.models import MobileOperator

admin.site.register(DepartureStat)
admin.site.register(AppStat)
admin.site.register(Line)
admin.site.register(Station)
admin.site.register(LineStation)
admin.site.register(MobileOperator)

