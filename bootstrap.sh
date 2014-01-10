#!/bin/bash

apt-get install wget, puppet

wget https://github.com/sidelab-urjc/scstack/archive/master.zip
unzip scstack-master.zip

cd scstack-master
puppet apply --modulepath=`pwd` default.pp
