import logging

""" Chart generator
"""
def make_gchart_data(obj, key):

	colours = ['e10505', '2009c8', '09c824', 'd9c808',] #'9803ae', '109abd', 'ff7800']

	# Make a dict from the obj
	d = {}
	total = 0
	for i in obj:
		k = getattr(i, key)
		if k is not None and len(k) > 1:
			if d.has_key(k):
				d[k] = d[k] + 1
				total = total + 1
			else:
				d[k] = 1
				total = total + 1


	data = []
	labels = []

	# Sort the dict by value
	for key, value in sorted(d.items(), key=lambda x: (-1*x[1], x[0])):
		if len(labels) < 11:
			labels.append(key)
			data.append(value*100/total)

	# Make small values <1%
	for i, label in enumerate(labels):
		if data[i] < 1:
			percent = "<1"
		else:
			percent = data[i]
		labels[i] = "%s %s%%" % (label, percent)


	return {'data': data, 'labels': labels, 'colours': colours, 'total': total}

