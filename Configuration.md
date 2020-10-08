
## MDS related configs
MDS (metadata service) is used to extract information about RBAC role bindings 

`mds.use.cluster.registry` uses the cluster registry of the MDS to retrieve RBAC scopes.
Requires CP 6.0 or above.

`mds.scope.file` loads the scopes from a JSON file.