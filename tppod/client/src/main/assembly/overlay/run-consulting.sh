#!/bin/bash
cd ${0%/*}
java $* -cp 'lib/jars/*' "grupo2.client.ConsultingClient"

