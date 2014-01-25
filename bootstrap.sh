#!/bin/bash

apt-get update

apt-get -y install wget puppet

puppet apply --modulepath=`pwd` default.pp
