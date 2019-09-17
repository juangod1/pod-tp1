# POD
## Instrucciones de ejecución
- Para deployar, correr el bash script `scripts/deploy.sh`. Como argumento debe pasársele el directorio donde se hara el deploy (en adelante `$DEPLOY_DIR`). Es decir, correr: `$> sh scripts/deploy.sh $DEPLOY_DIR`

- Para correr el servidor
    - Ejecutar el script `$DEPLOY_DIR/server/run-registry.sh`
    - En otra terminal, levantar el servidor ejecutando `$DEPLOY_DIR/server/run-server.sh`

- Para correr los clientes, se deben ejecutar los siguientes scripts:
    - Para el cliente de administración: `$DEPLOY_DIR/client/run-management.sh`
    - Para el cliente de fiscalización: `$DEPLOY_DIR/client/run-fiscal.sh`
    - Para el cliente de votación: `$DEPLOY_DIR/client/run-voting.sh`
    - Para el cliente de consulta: `$DEPLOY_DIR/client/run-consulting.sh`
    
    En todos los casos a estos scripts se le pasan los argumentos a los programas (de la misma forma que se le pasarían a `java`). Por ejemplo `$> sh $DEPLOY_DIR/client/run-management.sh -DserverAddress=10.6.0.1:1099 -Daction=open` es una invocación correcta del cliente de administración.