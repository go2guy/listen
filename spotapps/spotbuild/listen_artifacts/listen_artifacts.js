function getArtifact(argList) {
    return getJsonVal(argList, 'artifactsDIR') + getJsonVal(argList, 'artifact').split('artifacts/')[1];
}

function getFileName(artifact, fileType, index) {
    var result = '';
    var type = getNextElement(index, fileType, '|');
    if (type != '-1')
        result = artifact.slice(0,-3) + type;
    return result;
}
