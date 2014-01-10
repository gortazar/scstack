#!/bin/bash

HTTPSTATUS=`curl -s -w "%{http_code}" "http://test.scstack.org/redmine/" -o /dev/null`
if [ 200 -ne $HTTPSTATUS ]; then
  echo "Unexpected HTTP status code: $HTTPSTATUS"
  exit 1
fi

HTTPSTATUS=`curl -s -w "%{http_code}" "https://test.scstack.org:5555/" -o /dev/null`
if [ 200 -ne $HTTPSTATUS ]; then
  echo "Unexpected HTTP status code: $HTTPSTATUS"
  exit 1
fi

