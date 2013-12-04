#!/bin/bash
# set -x

DEFINED_TARGETS='schema_install data_install seed_install release drop install seed schema_upgrade upgrade'

user='root'
pass=''

# . ~/.bash_profile
. build.env
 
fail_on_error()
{
  ERR_CODE=${PIPESTATUS[0]}
  if [ $ERR_CODE -ne 0 ]
  then
    echo "Failure in target: $1" |tee -a $BUILD_LOG
    exit $ERR_CODE
  fi
}

data_install ()
{
  mysql -u $user -p "$pass" < /interact/listen/db/data/load.sql
  fail_on_error data_install
}

seed_install ()
{
  mysql -u $user -p "$pass" < /interact/listen/db/data/seed.sql
  fail_on_error seed_install
}

schema_install () {
  mysql -u $user -p "$pass" < /interact/listen/db/schema/schema.sql
  fail_on_error schema_install
}

schema_upgrade () {
  mysql -u $user -p "$pass" < /interact/listen/db/schema/upgrade.sql
  fail_on_error schema_upgrade
}

data_upgrade () {
  mysql -u $user -p "$pass" < /interact/listen/db/data/upgrade.sql
  fail_on_error seed_upgrade
}

install() {
  drop
  schema_install
  data_install
  seed_install
  fail_on_error install
}

drop() {
  mysql -u $user -p "$pass" < /interact/listen/db/schema/drop.sql
  fail_on_error drop
}

seed ()
{
  seed_install
  fail_on_error seed
}

upgrade () {
  schema_upgrade
  data_upgrade
  fail_on_error upgrade
}

echo "Build invoked with target $1" | tee $BUILD_LOG

# if [ ! -d "$BUILDTMP" ]; then
  # mkdir "$BUILDTMP"
# fi

# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! IMPORTANT !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! #
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!This does very bad shit if left blank!!!!!!!!!!!!!!!!!!!!!!!!!!!!! #
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! IMPORTANT !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! #
# if [ "$BUILDTMP" != "" ]
# then
  # rm -rf "$BUILDTMP"/*
# fi
# fail_on_error build_tmp_setup

#
# Check targets against list of possible inputs
#

for X in $@
do
  VALIDATION=0
  for Y in $DEFINED_TARGETS
  do
    if [ $X = $Y ]
    then
      VALIDATION=1
    fi
  done
  if [ $VALIDATION != 1 ]
  then
    echo "Target $X unknown"
    exit 1
  fi
done

for TARGET in $@
do
  $TARGET
done

echo -e "#\n#\n# Build Successful for targets: $@\n# See $BUILD_LOG for details" |tee -a $BUILD_LOG
exit 0
