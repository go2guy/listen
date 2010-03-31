#!/bin/bash
#
#  Master build shell script

cd `dirname $0`
BUILDDIR=$PWD/stage
ARTIFACTS_DIR=$PWD/ii_artifacts

if [ -d $BUILDDIR ]; then
  rm -rf $BUILDDIR
fi


echo "Master build populating BUILDDIR=$BUILDDIR"
mkdir -p $BUILDDIR/interact/packages/conf

# copy in the stuff.  to the place.  You know what I mean
cp  ../controller/ii_artifacts/*.rpm $BUILDDIR/interact/packages/
cp  ../spotapps/ii_artifacts/*.rpm $BUILDDIR/interact/packages/

cp  ../controller/ii_artifacts/*.uia $BUILDDIR/interact/packages/conf/
cp  ../spotapps/ii_artifacts/*.uia $BUILDDIR/interact/packages/conf/

# Move iiInstall and iiLoader config files into place
if [ -f iiInstall.cfg ]
then
    cp iiInstall.cfg $BUILDDIR/interact/packages/conf/
fi

if [ -f iiLoader.cfg ]
then
    cp iiLoader.cfg $BUILDDIR/interact/packages/conf/
fi

if [ -z "$VERSION" ]; then
    export VERSION="unknown"
fi

if [ -z "$RELEASE" ]; then
    export RELEASE="`date +%Y%m%d%H%M`"
fi


export TOPDIR=$PWD/rpm-build
rpmbuild -bb --buildroot $BUILDDIR listen.spec
RPMFILE=`find $TOPDIR -name "*.rpm"`
echo "RPMFILE=$RPMFILE"
mv $RPMFILE $ARTIFACTS_DIR

rm -rf $TOPDIR



