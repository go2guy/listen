function setDestination (jsonObj) {
	var destination = getJsonVal(jsonObj, 'number');
	var extLength = getJsonVal(jsonObj, 'EXT_LENGTH');
	var extPrefix = getJsonVal(jsonObj, 'EXT_PREFIX');
	var extSuffix = getJsonVal(jsonObj, 'EXT_SUFFIX');
	var sipUrl = getJsonVal(jsonObj, 'sipURL');
	
	if (destination.length > extLength) {
		destination = destination + '@' + sipUrl;
	} else {
		destination = extPrefix + destination + extSuffix;
	}
	return destination;
}
