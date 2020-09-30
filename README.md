# Kafka Clusterstate Tools

## Design goals
* modularity
* freedom to specify your Kafka cluster in any way you like. In love with YAML? Write your 
cluster spec in YAML. Prefer a Java DSL? Just use our builders. Prefer another way to set up
ACLs? Reimplement one of our strategies and roll with it.