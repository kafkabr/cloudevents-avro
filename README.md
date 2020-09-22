# catalog-query APP

Catalog Service responsável pelo Query no contexto de __catalog__.

## Requerimentos

- JDK 1.8
- Apache Maven 3.6+
- Docker 19+
- Acesso ao repositório https://repo.maven.apache.org/maven2/ ou uma 
alternativa com acesso às dependências presentes no `pom.xml`
- Kafka
- Elasticsearch
- Docker Compose 1.24+

## Configurações

Não se preocupe, pois apesar de existirem atalhos pelas variávies
de ambiente, você pode utilizar tranquilamente aquilo que o Spring Boot
oferece. Então veja todos as propriedades no 
[application.properties](./src/main/resources/application.properties)

No caso do Kafka, utilizamos Spring Kafka, então você utilizar 
o modo Spring para configurações.

### Variáveis de Ambiente

- `APP_KAFKA_CONSUMER_TOPICS`: tópicos para consumir, ou expressão.
- `KAFKA_CLIENT_ID`: nome do cliente Kafka, usado pelos brokers para logs e 
métricas. Utilize um [nome clean](https://medium.com/coding-skills/clean-code-101-meaningful-names-and-functions-bf450456d90c), não genérico.
  - `spring.kafka.producer.client-id`, `spring.kafka.consumer.client-id`
- `KAFKA_CONSUMER_GROUP`: nome do grupo de consumo que esta aplicação pertence
  - `spring.kafka.consumer.group-id`
- `KAFKA_BOOTSTRAP_SERVERS`: lista de brokers para o cluster Kafka
  - `spring.kafka.bootstrap-servers`
- `SCHEMA_REGISTRY_URL`: url para o registro de esquemas Avro 
  - `spring.kafka.properties.schema.registry.url`
- `KAFKA_FAIL_WHEN_MISSING_TOPICS` (`true` | `false`): configure `true` em
ambientes restritos, onde a criação automática de tópicos é bloqueada.
Assim a aplicação só iniciará se eles existirem.
  - `spring.kafka.listener.missing-topics-fatal`
- `ELASTICSEARCH_HOST_AND_PORT`: host e port para cluster Elasticsearch

- `APP_LOG_LEVEL` _(opcional)_: nível de registro para logs. Padrão é `INFO`

Tabela com os níveis de registro e sua hierarquia:

| `APP_LOG_LEVEL` | TRACE  | DEBUG  | INFO   | WARN   | ERROR  |
| --------------- | :----: | :----: | :----: | :----: | :----: |
| TRACE           |  sim   |  não   |  não   |  não   |  não   |
| DEBUG           |  sim   |  sim   |  não   |  não   |  não   |
| INFO            |  sim   |  sim   |  sim   |  não   |  não   |
| WARN            |  sim   |  sim   |  sim   |  sim   |  não   |
| ERROR           |  sim   |  sim   |  sim   |  sim   |  sim   |

- `APP_LOG_PATTERN` _(opcional)_: padrão do registro de logs. Esse padrão
segue o formato documentado [aqui](https://logback.qos.ch/manual/layouts.html),
porém, veja um exemplo na configuração
[logback.groovy](src/main/resources/logback.groovy)

- `APP_LOG_LOGGERS` _(opcional)_: para configurar níveis de log de loggers
presentes nas dependências da aplicação.
```bash
# logger_1:LEVEL,logger_2:LEVEL,...
APP_LOG_LOGGERS='io.netty:ERROR,org.springframework:ERROR'
```

- `OPENTRACING_HTTP_SENDER_URL`: url para endpoint que recebe os dados emitidos
pelo rastreamento distribuído no padrão
[OpenTrancing](https://opentracing.io/).

## Build & Run

Depois de utilizar uma das duas formas para build & run, a documentação swagger
estará disponível em:

- http://localhost:8080/swagger-ui.html

### Maven

Para montar o fatjar, execute o comando:

```bash
mvn clean package
```

Para rodar e iniciar a aplicação na porta `38002`:

> Utilize o [docker-compose](./docker-compose.yaml) e inicie todos os serviços
para testes

O exemplo abaixo também está disponível no script [run.sh](./run.sh).

```bash
# Exemplo de execução
APP_KAFKA_CONSUMER_TOPICS='catalog-product-added' \
KAFKA_CLIENT_ID='catalog-query' \
KAFKA_CONSUMER_GROUP='catalog-query' \
KAFKA_BOOTSTRAP_SERVERS='localhost:9092' \
SCHEMA_REGISTRY_URL='http://localhost:8081' \
KAFKA_FAIL_WHEN_MISSING_TOPICS='false' \
ELASTICSEARCH_HOST_AND_PORT='localhost:9200' \
OPENTRACING_HTTP_SENDER_URL='http://localhost:14268/api/traces' \
java -Dserver.port='38002' -jar target/app-spring-boot.jar
```

### Docker

A definição [Dockerfile](./Dockerfile) desta aplicação emprega 
[multi-stage builds](https://docs.docker.com/develop/develop-images/multistage-build/).
Isso significa que nela acontece o build da aplicação e a criação da imagem.

Se for necessário somente a criar a imagem, pode-se utilizar a definição 
[Dockerfile-image](./Dockerfile-image). Mas antes é necessário montar
o fatjar através do maven.

Para build do fatjar e montar a imagem, execute o comando:

```bash
docker build . -t catalog:1.0
```

Para montar apenas a imagem (antes é necessário o build do maven):

> Normalmente em pipelines DevOps corporativos só é possível montar a imagem

```bash
docker build -f Dockerfile-image . -t catalog:1.0
```

Para rodar o container:

> Utilize o [docker-compose](./docker-compose.yaml) e inicie todos os serviços
para testes

```bash
docker run -p 8080:8080 \
       -i --rm \
       catalog:1.0
```

## Deployment

Esta aplicação vem preparada para deployment no Kubernetes.

Para implantar, fazer deployment, dessa aplicação no kubernetes são utilizados
[helm](https://helm.sh/) charts, disponíveis no diretório [k8s-helm](./k8s-helm).

```bash
helm install \
    --namespace='<NOME DO SEU NAMESPACE>' \
    --set image.repository='<URL PARA DOCKER REGISTRY>' \
    --set configmap.APP_KAFKA_CONSUMER_TOPICS='catalog-product-added' \
    --set configmap.KAFKA_CLIENT_ID='catalog-query' \
    --set configmap.KAFKA_CONSUMER_GROUP='catalog-query' \
    --set configmap.KAFKA_BOOTSTRAP_SERVERS='localhost:9092' \
    --set configmap.SCHEMA_REGISTRY_URL='http://localhost:8081' \
    --set configmap.KAFKA_FAIL_WHEN_MISSING_TOPICS='false' \
    --set configmap.ELASTICSEARCH_HOST_AND_PORT='localhost:9200' \
    --set configmap.OPENTRACING_HTTP_SENDER_URL='http://localhost:14268/api/traces' \
    catalog k8s-helm/
```

## Cobertura

- Executar os testes

```bash
mvn clean test
```

- Acessar o relatório: `target/site/jacoco/index.html`

## Dependências Principais

Seguem em destaque algumas das principais dependências do projeto, todas
elas declaradas no [pom.xml](pom.xml).

- lombok [Instalar Eclipse](https://projectlombok.org/setup/eclipse)
- Spring Boot
- JUnit 5
- Testcontainers

## Logging

Todo os registros de log são escrito na saída padrão, `sysout`.

O padrão é a produção de registros amigáveis, como:

```
INFO  [main] y.h.CatalogGetControllerTest: No active profile set, falling back to default profiles: default
WARN  [main] o.a.t.u.m.Registry: The MBean registry cannot be disabled because it has already been initialised
INFO  [main] o.s.b.w.e.t.TomcatWebServer: Tomcat initialized with port(s): 0 (http)
INFO  [main] o.a.c.h.Http11NioProtocol: Initializing ProtocolHandler ["http-nio-auto-2"]
INFO  [main] o.a.c.c.StandardService: Starting service [Tomcat]
INFO  [main] o.a.c.c.StandardEngine: Starting Servlet engine: [Apache Tomcat/9.0.27]
INFO  [main] o.a.c.c.C.[.[.[/]: Initializing Spring embedded WebApplicationContext
```

Mas pode-se optar pela estrutura JSON, menos amigável e mais centrada no
processamento por ferramentas como Elasticsearch e Splunk.

## Métricas

As métricas deste microsserção são expostas através do Spring Boot Actuator,
no seguinte caminho `/actuator`. E as métricas para Prometheus estão 
disponíveis no caminho `/actuator/prometheus`.

Veja mais detalhes na documentação oficial: 

- [Actuator](https://docs.spring.io/spring-boot/docs/current/actuator-api/html/)

## Rastreamento

Para solução do rastreamento distribuído foi empregado a definição 
[OpenTracing](https://opentracing.io/)
em conjunto com uma implementação chamada [Jaeger](https://www.jaegertracing.io/).

## Caminho HTTP p/ Verificações de Saúde

> Health Checks

Essas são configurações comuns quando a implantação do aplicação 
é feita em container, especificamente no Kubernetes.

- [Liveness Probe](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/#define-a-liveness-http-request)
  - `/actuator/info`

- [Readiness Probe](https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-startup-probes/#define-readiness-probes)
  - `/actuator/health`

## Detalhes Internos

- Arquitetura tradicional (MVC)
- `APP_NAME` é uma propriedade de sistema criada por `AppConfiguration.java`
a partir do build-info produzido pelo plugin springboot
  - utilizada pelo opentracing na identificação da aplicação
- logback é configurado através de arquivo .groovy, assim é possível
modificar os níveis e formatação do log apenas definindo variáveis de ambiente
- foi utiizada a implementação OpenTrancing Jaeger, mas pode-se trocar por zipikin 
apenas modificando configurações e dependências

## Dicas

### Docker Compose

Neste repositório existe o arquivo [docker-compose.yaml](./docker-compose.yaml),
que inicia todas as partes móveis que a aplicação depende.

Iniciar a stack:

```bash
docker-compose up
```

Serviços presentes na stack

- Kafka: `localhost:9092`
- Jaeger UI: `http://localhost:16686`

### Eclipse

Esse projeto pode ser preparado para ser utilizado no Eclipse, basta 
executar o seguinte comando:

```sh
mvn eclipse:eclipse
```

Caso você atualize as dependências no `pom.xml`, execute esses comandos 
para atualizar o projeto no Eclipse:

```sh
mvn eclipse:clean eclipse:eclipse
```