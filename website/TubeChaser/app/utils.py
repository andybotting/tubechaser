def elapsed_time(seconds, suffixes=['y','w','d','h','m','s'], add_s=False, separator=' '):
	"""
	Takes an amount of seconds and turns it into a human-readable amount of time.
	"""
	# the formatted time string to be returned
	time = []
 
	# the pieces of time to iterate over (days, hours, minutes, etc)
	# - the first piece in each tuple is the suffix (d, h, w)
	# - the second piece is the length in seconds (a day is 60s * 60m * 24h)
	parts = [(suffixes[0], 60 * 60 * 24 * 7 * 52),
		(suffixes[1], 60 * 60 * 24 * 7),
		(suffixes[2], 60 * 60 * 24),
		(suffixes[3], 60 * 60),
		(suffixes[4], 60),
		(suffixes[5], 1)]
 
	# for each time piece, grab the value and remaining seconds, and add it to
	# the time string
	for suffix, length in parts:
		value = seconds / length
		if value > 0:
			seconds = seconds % length
			time.append('%s%s' % (str(value), (suffix, (suffix, suffix + 's')[value > 1])[add_s]))
		if seconds < 1:
			break
 
	return separator.join(time)
