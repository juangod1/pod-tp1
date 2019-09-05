#!/bin/bash
cd "${0%/*}"
cd ../tppod/client/target
if [ ! -f 'tppod-client-1.0-SNAPSHOT' ]; then
    tar -xzf tppod-client-1.0-SNAPSHOT-bin.tar.gz
fi
cd tppod-client-1.0-SNAPSHOT
sh run-client.sh