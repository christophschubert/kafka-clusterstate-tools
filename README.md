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
  - preserveData: prohibits certain types of config changes, such as shortening the retention time

#### Remarks
- `kst` will not delete topics whose names do not start with a prefix which appears in one of the domain files of the context which it is running in.
Hence you should not split domain files for one domain over multiple contexts (folders).

### Domain file format

```yaml
---
name: store.pos.checkout
projects:
- name: projectA
  topics:
  - name
```

User are free to add further meta-data to the domain files.
This will be ignored by the parser.

For example, the following domain has been enriched with descriptions and tags.
This might aid in topic discovery. 

```yaml
---
todo: add it!
```