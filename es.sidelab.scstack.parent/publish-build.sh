#!/bin/bash

username=$1
password=$2
lftp -u ${username},${password} sftp://code.sidelab.es <<EOF
put -O /public/sidelabcodestack/artifacts/0.4 ${WORKSPACE}/es.sidelab.scstack.service/target/scstack-service-bin.tar.gz
put -O /public/sidelabcodestack/artifacts/0.4 ${WORKSPACE}/es.sidelab.scstack.puppet-installer/target/puppet-installer-0.4-SNAPSHOT-bin.tar.gz
bye
EOF

