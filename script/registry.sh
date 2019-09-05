#!/bin/bash
cd ../tppod/server/target
if [ ! -f 'tppod-server-1.0-SNAPSHOT' ]; then
    tar -xzf tppod-server-1.0-SNAPSHOT-bin.tar.gz
fi
cd tppod-server-1.0-SNAPSHOT
sh run-registry.sh