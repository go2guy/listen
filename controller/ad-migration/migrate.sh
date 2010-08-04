#!/bin/bash
API=http://khaaannnn:4242/api
TMPFILE=/tmp/listen-migration

while read MAPPING
  do
  EMAIL=`echo "$MAPPING" | cut -d',' -f1`
  ADUSER=`echo "$MAPPING" | cut -d',' -f2`
#  REALNAME=`echo $MAPPING | cut -d',' -f3`
  echo "Converting [$EMAIL] to [$ADUSER]"

  CODE=`curl -# -w %{http_code} -o $TMPFILE "$API/subscribers?username=$EMAIL&_uniqueResult=true"`
  echo "CODE: $CODE"

  if [ "$CODE" == "200" ]; then
      curl -# -o $TMPFILE "$API/subscribers?username=$EMAIL&_uniqueResult=true"

      HREF=`cat $TMPFILE | awk -F'<subscriber href="' '{print $2}'|awk -F'">' '{print $1}'`

      sed "s/<username>$EMAIL<\/username>/<username>$ADUSER<\/username>/g" $TMPFILE > "$TMPFILE.1"
      mv -f "$TMPFILE.1" $TMPFILE

      sed 's/<isActiveDirectory>false<\/isActiveDirectory>/<isActiveDirectory>true<\/isActiveDirectory>/g' $TMPFILE > "$TMPFILE.1"
      mv -f "$TMPFILE.1" $TMPFILE

      echo "PUTting to $API$HREF"
      CODE=`curl -# -w %{http_code} --data @$TMPFILE -o $TMPFILE --request PUT "$API$HREF"`
      if [ "$CODE" == "200" ]; then
          echo "SUCCESS"
      else
          echo "FAILURE: $CODE"
          echo "RESPONSE:"
          cat $TMPFILE
          echo ""
      fi
  else
      echo "Subscriber [$EMAIL] not found"
  fi
  echo ""
done < mapping.txt