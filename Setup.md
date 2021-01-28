# Setting up clusterstate tools for topic and ACL management

## Prerequisites

The principal the cluster state tools run under needs the following ACLs:

* ALTER on the cluster resource to create and delete ACLs 
* DESCRIBE on the cluster resource
* the following operations be allowed for topic resources prefixed with the current domain:
   * ALTER_CONFIGS, CREATE, and DESCRIBE
   * ALTER when changing the number of partitions should be allowed
   * DELETE when topic deletion should be allowed
    
## Using the CLI

The main entrypoint for using the cluster state tools is the CLI which can be found at `net.christophschubert.kafka.clusterstate.actions.CLI`.
It contains a number of subcommands, the most useful of which is `apply`.
`apply` requires a _context_, that is, a folder which contains _domain files_ describing the resources to be managed as well as file `kst.properties` which is used to pass configuration values.

## Passing config values

The following ways can be used to pass config values to the CLI:

* Include them in the command properties file.
  This property file defaults to `kst.properties` in the domain folder.
  Another value can be specified using the `--command-properties` (or `-c`) command line option of the CLI.
  
* Specify them using environment variables.
  By default, all environment variables with the prefix `KST_` will be read and converted to properties according to the following rules:
  1. drop the prefix (e.g., `KST_`)
  1. lower-case the environment variable name
  1. replace every underscore (`_`) with a dot (`.`)
  
  For example, `KST_BOOTSTRAP_SERVER=localhost:9091` will lead to the property `bootstrap.server=localhost:9091`.
Observe that the env var value is not modified.
  
  The prefix used to select environment variables can be changed using the `--env-var-prefix` (or `-e`) command line option.