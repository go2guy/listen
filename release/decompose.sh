# this script will decompose an existing Invigorate.rpm into component parts ready to be used as components for a master package build.

if [ -f $1 ]; then
	echo "Decomposing package $1"
else
  exit;
fi

cd `dirname $1`
rpm2cpio `basename $1` |cpio -iduc
rm -rf ii_artifacts
mkdir ii_artifacts

for X in `find ./interact -name "*.rpm"`
do
  mv $X ./ii_artifacts
done

for X in `find ./interact -name "*.uia"`
do
  mv $X ./ii_artifacts
done

for X in `find ./interact -name "*.dat"`
do
  mv $X ./ii_artifacts
done

rm -rf interact
cd -

echo "Package decomposed, ii_artifacts populated"

if [ -f locations.mf ]; then
  echo `dirname $1` > locations.mf
fi
