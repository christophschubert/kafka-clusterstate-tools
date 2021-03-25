# Example configurations

```yaml
name: store.pos
projects:
- name: checkout
  topics:
  - name: raw
    config:
      "retention.ms": 3600000
    dataModel:
      key:
        type: String
      value:
        type: Avro
        schemaFile: "schemas/test.avsc"
      headers:
        - keyName: correlationId
          description: "A correlation Id set by process addCorrelationId" 
  - name: processed
```