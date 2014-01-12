#!/usr/bin/env bash

apt-get update
apt-get -y install curl

curl -sSL https://get.rvm.io | bash -s $1
