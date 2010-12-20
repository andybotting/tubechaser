import logging

""" Chart generator
"""
def make_gchart_data(obj, key, labels=None):

	colours = ['e10505', '2009c8', '09c824', 'd9c808'] #, '9803ae', '109abd', 'ff7800']

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


	items_data = []
	items_labels = []
	items_total = 0

	# Sort the dict by value
	for key, value in sorted(d.items(), key=lambda x: (-1*x[1], x[0])):
		percent_val = value*100/total

		# If we have 10+ items, then we'll show the top 10, 
		# and bunch the leftovers into an 'Others' category
		if len(d.items()) > 9:
			if len(items_labels) < 11:
				items_labels.append(key)
				items_data.append(percent_val)
				items_total += percent_val
		else:
			items_labels.append(key)
			items_data.append(percent_val)
			items_total = 100

	# If the total of all >5% values is less than 100%, make an 'Others' group for the rest
	if items_total < 98:
		items_labels.append("Others")
		items_data.append(100 - items_total)

	# If labels have been given
	if labels:
		for i, label in enumerate(items_labels):
			for label in labels:
				if items_labels[i] == label[0]:
					items_labels[i] = label[1]


	# Add a percent value to the end of the labels
	for i, label in enumerate(items_labels):
		# Make small values <1%
		if items_data[i] < 1:
			percent = "<1"
		else:
			percent = items_data[i]
		items_labels[i] = "%s %s%%" % (label, percent)


	return {'data': items_data, 'labels': items_labels, 'colours': colours, 'total': total}

