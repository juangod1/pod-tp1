#!/bin/bash
if [ $# -lt 1 ]; then
    echo "Especificar el directorio de deploy"
    exit 1
fi

sh ${0%/*}/maven.sh
tar -xzf ${0%/*}/../tppod/server/target/tppod-server-1.0-SNAPSHOT-bin.tar.gz 
tar -xzf ${0%/*}/../tppod/client/target/tppod-client-1.0-SNAPSHOT-bin.tar.gz
mv tppod-server-1.0-SNAPSHOT $1/server
mv tppod-client-1.0-SNAPSHOT $1/client
