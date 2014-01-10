#!/bin/bash

apt-get install wget puppet

puppet apply --modulepath=`pwd` default.pp
