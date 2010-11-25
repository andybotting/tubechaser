#!/bin/bash

WORKSPACE="~/usr/android/workspace/TubeChaser"

for ICON in `ls *.svg`; do
	NAME=`basename ${ICON} .svg`

	DIMENS=`identify ${ICON} | awk '{print $3}'`
	WIDTH=${DIMENS%x*}
	HEIGHT=${DIMENS#*x}

	# Generate HDPI icon - using standard dimensions
	inkscape ${ICON} -e ${WORKSPACE}/res/drawable-hdpi-v4/${NAME}.png -w ${WIDTH} -h ${HEIGHT}

	M_WIDTH=`echo "${WIDTH}/1.5" | bc`
	M_HEIGHT=`echo "${HEIGHT}/1.5" | bc`

	# Generate MDPI icons using HDPI size / 1.5
	inkscape ${ICON} -e ${WORKSPACE}/res/drawable/${NAME}.png -w ${M_WIDTH} -h ${M_HEIGHT}

	L_WIDTH=`echo "${WIDTH}/2" | bc`
	L_HEIGHT=`echo "${HEIGHT}/2" | bc`

	# Generate LDPI icons using HDPI size /2 
	inkscape ${ICON} -e ${WORKSPACE}/res/drawable-ldpi-v4/${NAME}.png -w ${L_WIDTH} -h ${L_HEIGHT}

done
