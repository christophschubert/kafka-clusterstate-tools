# Kafka Clusterstate Tools

## Design goals
* modularity
* freedom to specify your Kafka cluster in any way you like. In love with YAML? Write your 
cluster spec in YAML. Prefer a Java DSL? Just use our builders. Prefer another way to set up
ACLs? Reimplement one of our strategies and roll with it.


## Domains

*Domains* provide a compact description of a the topics and access rights of a
Apache Kafka or Confluent Server cluster which makes it easy to follow best
practices.

**Note**: The concept of domain was inspired to the *topology* concept of
the Kafka Topology Builder. We chose a different name since the term topology is also used
in a Kafka Streams context where it means something completely different.

Domains are specified in *domain files*. 
Domain files can be serialized as YAML (ending in `.domy`) or as JSON (ending in `domj`).
A group of domain files is called a *context*.
Physically, a context is just a folder and we treat all `.domy` or `.domj` files in this folder as part of the same context.
Each domain has a *name*.
The name of a domain will be a prefix of every topic, consumer group, connector, or Kafka Streams application ID defined in this context.

**Note**: This is due to the fact that ACLs in Apache Kafka can be either given for literal resource names or for prefixed resources.
Hence having the name of the domain as the prefix of the resource names allows for a compact specification.


### Applying a domain

Domains serve as a description of the desired cluster state.
`kst` can be used to automatically provision topics, manage access rights, and register schemas specified in a domain.

This is done as follows:
1. Point `kst` to a context (folder)
1. `kst` will read all `.domy` and `.domj` files in the folder and compile them to a desired cluster state.
1. This cluster state will be grouped by domain names. In pseudo-code we could write `Map<String, ClusterState>` where each
key in the map is a prefix for all resource names mentioned in the corresponding value.
1. A `ClusterState` is generated from the Apache Kafka cluster we want to manage.
1. For each key (resource prefix) in the prefix-clusterstate derived from the context map we extract precisely those resources   
in the actual ClusterState which start with the given prefix.
1. For each domain name, we compare the desired and current states and generate a list of actions which have to be
performed to move the cluster from the current state to the desired state. The actions could be:
  - adding a topic
  - deleting a topic
  - changing a topic config
  - adding ACLs
  - deleting ACLs
  - registering schema
  - adding/modifying/deleting RBAC role-bindings
  
Then the corresponding actions are performed.
When performing the actions, the following config options determine whether an individual action will be performed:
  - allowDeletes: are we allowed to delete topics
  - allowEmptyDeletes: are we allowed to delete topics which currently do not contain messages
  - preserveData: prohibits certain types of config changes, such as shortening the retention time.

#### Remarks
- `kst` will not delete topics whose names do not start with a prefix which appears in one of the domain files of the context which it is running in.
Hence you should not split domain files for one domain over multiple contexts (folders).

#### Config and keeping different environments in sync
`kst` will look for a `kst.properties` file in the current context to read information about
bootstrap servers, schema registry, and its internal config such as the topic naming strategy from there.
Using the `--env-var-prefix` command line option it is possible to specify a 
prefix for environment variables.
All environment variables with this prefix will have the prefix stripped and will be
placed into the in-memory configs used to connect to the server, overwriting the values from the properties file
This can be to keep secrets out of the property files and also to keep different environments in sync.

For example, let us assume we have a PROD and a QA environment which should contain the same topics and 
have the same schemas and access rights configured. This could be achieved by using the same context and starting `kst` as 
follows:

```bash
export KST_PROD_BOOTSTRAP_SERVER=broker0.prod.your.company.internal
export KST_PROD_SASL_JAAS_CONFIG=.....

export KST_QA_BOOTSTRAP_SERVER=broker0.qa.your.company.internal
export KST_QA_SASL_JAAS_CONFIG=.....

# update QA
kst apply --env-var-prefix KST_QA --context .

# update PROD
kst apply --env-var-prefix KST_PROD --context .
```

### Domain file format

```yaml
---
name: store.pos.checkout
projects:
- name: projectA
  topics:
  - name: topicA
    dataModel:
      key:
        type: String
      value:
        type: Avro
        schemaFile: "schemas/test.avsc"
      headers:
        - keyName: correlationId
          description: "A correlation Id set by process addCorrelationId" 
```

User are free to add further meta-data to the domain files.
This will be ignored by the parser.

For example, the following domain has been enriched with descriptions and tags.
This might aid in topic discovery. 

`kst` will try to register schemas for keys or values whose type are any one of `Avro`, `Protobuf`, or `JSONSchema`.
The registered schema will be taken from the file specified under `schemaFile`.
The path of `schemaFile` is relative to the given context.

```yaml
---
todo: add it!
```

### Which ACLs do I need and which ones will be created

```yaml
---
projects:
  - name: sample-project
    consumers:
      - principal: "User:xxx"
        groupId: group
        prefixGroup: true
        topics:      #when the topics are specified, individual ACLs will be created
            - topicA  
            - topicB
      - principal: "User:yyy"
        groupId: groupY
        prefixGroup: false #only access to literal groupId are allowed 
        # no topics field => prefix ACLs on the will be created for this producer
    producers:
      - principal: "User:yyy"
        idempotent: true # false by default, will give principal the idempotent write to the cluster
        transactionId: "transId"   #producer gets access to this transaction ID
        # not topics => prefix ACL will be created
      - principal: "User:zzzz"
        transactionId: "transId"   #producer gets access to this transaction ID
        topics: #only access to these topics will be provided
            - topicA

    streamsApps:
      - principal: "User:xxx"
        applicationId: aggregator
        prefixApplicationId: true # true by default
        inputTopics:    # not provided: access to all topics in the project
            - topicA
            - topicB
        outputTopics:   # not provided: access to all topics in the project
            - topicC
            - topicD   

    topics:
      - name: testTopic
        consumerPrincipals: 
        - [list of users getting read access to this topic, 
            especially useful for wildcard 'User:*']
        producerPrincipals:
        - [list of users getting read access to this topic, 
                      especially useful for wildcard 'User:*']
```
The topic and group names will be prefixed by the domain and project name.

Each consumer and producer defined in the project will get READ/WRITE access to each topic specified in the project.

Each `topic` can specify the following fields:

- `name` (required): will be concatenated with the domain and project name to form the topic name in the 
Kafka cluster.
- `configs`: a map which contains the topic configuration. Possible fields include:
    - `replication.factor`
    - `num.partitions`
    - any topic level config from https://docs.confluent.io/current/installation/configuration/topic-configs.html
- `dataModel`: a data model description as specified below.
- `consumerPrincipals`: a list of additional principals which will be granted READ access to the topic.
- `producerPrincipals`: a list of additional principals which will be granted WRITE access to the topic.

All fields are optional unless they are marked as required.

The `DataModel` of a topic has the following fields:
- `key`: TODO
- `value`: TODO
- `headers`: TODO