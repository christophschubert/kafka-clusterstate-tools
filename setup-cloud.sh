# sets up a service account and a ACLs to run the CLI
# Assumes:
# - ccloud CLI and jq are installed
# - user in logged into Confluent Cloud
# - right environment is enabled (e.g., using ccloud environment use <env-....>)
# - right Kafka cluster is set active (e.g., using ccloud kafka cluster use <lkc-...>
# - environment has a schema registry enabled (e.g., using ccloud schema-registry cluster enable --cloud aws --geo eu)

schema_reg_url=`ccloud schema-registry cluster describe -o json | jq '.endpoint_url'
sr_id=`ccloud schema-registry cluster describe -o json | jq -r '.cluster_id'`
service_account_id=`ccloud service-account create kst-test --description 'SA for kst' -o json | jq '.id'`
api_result=`ccloud api-key create --service-account 118857 --resource $sr_id -o json`