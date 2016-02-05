#!bash
# script to sync garmin mass storage device (such as Etrex 30x) with local directory.
# Script synchronizes:
# 1 - archived tracks from garmin to local dir. Archived tracks are passed track logs.
# 2 - current track from garmin to local dir. Just for a case, if passed track is not archived yet. 
# 3 - new tracks to be uploaded to garmin from local dir.

if [ "`uname -o`" == "Cygwin" ]; then
	GARMIN_MOUNT_POINT="/cygdrive/f" # Directory where OS will automatically mount garmin device.
	BIN_PATH="/cygdrive/c/utils"				 # Path where GPXManipulator.jar can be found.
	#LOCAL_GPX_BASE="/cygwin/home/${USER}/gpx/"	 # The database of all tracks.
	LOCAL_GPX_BASE="/cygdrive/c/gpx/"
	LOCAL_GPX_BASE_WIN="/gpx/"
else
	GARMIN_MOUNT_POINT="/media/${USER}/GARMIN"
	BIN_PATH="${HOME}/bin"
	LOCAL_GPX_BASE="${HOME}/gpx/"
	LOCAL_GPX_BASE_WIN="${LOCAL_GPX_BASE}"
fi;

SLEEP_TIME=1 								# Seconds to sleep waiting for garmin to be mounted.

LOCAL_DOWNLOAD_DIR="${HOME}/Downloads/"		# Take recent track from here and copy to garmin.
WAIT_FOR_USER_INPUT=1						# Wait for user to press Enter key after job is done,
											# so user can read previous messages.
DEBUG=0										# Debug mode: do not unmount garmin
CURRENT=1									# 1 - get Current.gpx track. -1 - get last GPX from Archive, -2 - get prev. GPX, and so on.

# throw error message and exit with non-zero exit code
function die {
  echo "ERROR: $1"
  read
  exit 1
}

# Read command line parameters, if any
[ -n "$1" ] && CURRENT="$1"
[ "$2" == "-d" ] && DEBUG=1

cd $BIN_PATH
if [ $? -ne 0 ]; then
	echo "ERROR: unable to change dir to [${BIN_PATH}]"
	exit 1
fi

# check if garmin is mounted or not
garmin_is_mounted=`mount | grep ${GARMIN_MOUNT_POINT}`

all_done=0
while [ $all_done -eq 0 ]; do

  # check if garmin is mounted or not
  if [ -n "`mount | grep ${GARMIN_MOUNT_POINT}`" ]; then

    # Copy to garmin
    # Copy last track downloaded from gpsies
    last_downloaded=`ls -1tr ${LOCAL_DOWNLOAD_DIR}/*.gpx | tail -1`
    if [ -n "$last_downloaded" ]; then
      # copy last downloaded file to garmin
      cp -u -v "$last_downloaded" ${GARMIN_MOUNT_POINT}/Garmin/GPX/ || die "COPY_LAST_TO_GARMIN"

      # store track in archive also, for history purposes.
      cp -u -v "$last_downloaded" ${LOCAL_GPX_BASE}/Garmin/GPX/Archive/ || die "COPY_LAST_TO_LOCAL_DB"
    fi

    # Copy from garmin
    #cp -u -v ${GARMIN_MOUNT_POINT}/Garmin/GPX/Archive/*.gpx ${LOCAL_GPX_BASE}/Garmin/GPX/Archive/ || die "COPY_GARMIN_ARCH_TO_LOCAL"
    #cp -v ${GARMIN_MOUNT_POINT}/Garmin/GPX/Current/Current.gpx ${LOCAL_GPX_BASE}/Garmin/GPX/ || die "COPY_GARMIN_CURRENT_TO_LOCAL"

    # sync local track DB with garmin track DB (archive)
    # This sync will:
    #   1 - take recent daily garmin archives from garmin,
    #   2 - copy last downloaded track to garmin's archive
    #   3 - copy Current.gpx from garmin
    rsync --recursive --verbose --perms --times  ${GARMIN_MOUNT_POINT}/Garmin/GPX/ ${LOCAL_GPX_BASE}/Garmin/GPX/ || die "RSYNC_GARMIN_ARCH"

    # Sync local gpx directory with garmin
    #cp -u -v ${LOCAL_GPX_BASE}/*.gpx ${GARMIN_MOUNT_POINT}/Garmin/GPX/other/ || die "COPY_LOCAL_TO_GARMIN"

    sync
    if [ ${DEBUG} -ne 1 ]; then
    	umount ${GARMIN_MOUNT_POINT} || die "ERROR UNMOUNTING GARMIN!!!"
    fi
    
    if [ $CURRENT -eq 1 ]; then
    	# Take current track
    	input_track_name="${LOCAL_GPX_BASE_WIN}/Garmin/GPX/Current/Current.gpx"
    	
    else
    	# Take track from archive
    	input_track_name="${LOCAL_GPX_BASE_WIN}/Garmin/GPX/Archive/`ls -tr ${LOCAL_GPX_BASE}/Garmin/GPX/Archive/ | tail ${CURRENT} | head -1`"
    fi
    
    curdate=`date +%Y%m%d%H%M%S`
    parsed_track_name="Track_${curdate}.gpx"		# Name of track that will be shown in GPSies.
    auth_hash=`bash gpx_get_gpsies_auth_hash.sh`	# Hashed username+password for GPSies, stored externally
    if [ -z "${auth_hash}" ]; then
    	echo "ERROR: no Authentication Hash specified"
    	exit 4
    fi
    
    proxyUser=`bash gpx_get_proxy_user.sh`;			# Username for proxy, stored externally
	proxyPassword=`bash gpx_get_proxy_password.sh`;	# Password for proxy, stored externally
	if [ -n "${proxyUser}" ]; then
		if [ -n "${proxyPassword}" ]; then
			proxyAuthParam="--http-proxy-user=${proxyUser} --http-proxy-password=${proxyPassword}"
		else
			echo "ERROR: no proxy password specified"
			exit 4
		fi
	else
		proxyAuthParam=""
	fi;
	
    [ ${DEBUG} -eq 1 ] && echo "auth_hash=[${auth_hash}] proxyAuthParam=[${proxyAuthParam}]";
    [ ${DEBUG} -eq 1 ] && debug_option="-d"
    echo "Run GPXManipulator. input=[${input_track_name}] output=[${parsed_track_name}]"

	java -cp . -jar GPXManipulator.jar \
    		$debug_option -i "${input_track_name}" -o "${LOCAL_GPX_BASE_WIN}/live/${parsed_track_name}" \
    		--http-proxy-use-system ${proxyAuthParam} \
    		--gpsies-launch-browser \
        	--gpsies-activity="biking" \
        	--gpsies-description="MyDescription" \
        	--preserve-creator \
        	--track-name="${parsed_track_name}" \
        	--gpsies-authenticate-hash=${auth_hash} \
        	--hotspot-lat-min=50.4560 --hotspot-lat-max=50.4768 \
        	--hotspot-lon-min=30.3276 --hotspot-lon-max=30.3829
    	
    rc=$?
    if [ $rc -ne 0 ]; then
    	if [ $rc -eq 3 ]; then
    		echo "WARNING: no trackpoints in track"
    	else
    		echo "ERROR: running GPXManipulator. rc=[$rc]"
    		exit $rc
    	fi
    fi 
    
    
    all_done=1
    echo "all done."

  else
    echo "WARNING: garmin is not mounted yet. Wait a second and run this script again."
    sleep 1
  fi

done

# press "any key" to read previous messages and to exit this script
[ ${WAIT_FOR_USER_INPUT} -eq 1 ] && read

