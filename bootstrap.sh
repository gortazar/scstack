#!/bin/bash

apt-get update

apt-get -y install wget puppet

# ./install-rvm.sh stable
# ./install-ruby.sh 1.9.3

puppet apply --modulepath=`pwd` default.pp
