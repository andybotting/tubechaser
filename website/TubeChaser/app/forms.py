from django.forms import ModelForm
from app.models import DepartureStat
from app.models import AppStat

class DepartureStatForm(ModelForm):
	class Meta:
		model = DepartureStat


class AppStatForm(ModelForm):
	class Meta:
		model = AppStat

