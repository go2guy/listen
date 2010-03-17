cd `dirname $0`

svnRel=`svnversion`
export RELEASE=`echo $svnRel |awk -F':' '{print $1}'`

# Validate uia files against dtd...
echo
echo "Validating uia files against the dtd:"
retval=0
for uiafile in `find ./ii_artifacts/ -name "*.uia" -o -name "*.cfg"`
do
    echo "Validating [ ${uiafile} ]:"
    /opt/uia-validator/validate.py ${uiafile}
    if [ $? -ne 0 ]
    then
        retval=`expr ${retval} + 1`
    fi
    echo
done

if [ ${retval} -ne 0 ]
then
    echo "Uia validation failed for [ ${retval} ] file(s). Exiting."
    echo
    exit 1
fi

rpmbuild -bb spotapps.spec
