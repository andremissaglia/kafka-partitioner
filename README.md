# kafka-partitioner

kafka-partitioner is a worker that reads from a topic and writes to another topic. 
It is used when you have a topic with one partition and wish to have multiple consumers, without worrying about changing
the producers to set the partition key.


## Getting started
Run this image locally, or in kubernetes, setting the configs accordingly:

```yaml
version: '2'
services:
  partitioner:
    image: andremissaglia/kafka-partitioner:0.3.0
    environment:

# Set the consumer configs.
      KAFKA_APPLICATION_ID: "applicationid"
      KAFKA_USER: "user"
      KAFKA_PASS: "pass"
      KAFKA_SERVER: "localhost:9092"
      ENABLE_EOS: "y" # Optional: Enable EOS processing guarantee (y/n)

# Or, specify the consumer config file.
      CONFIG_FILE: ""

# Where to read/write.
      INPUT_TOPIC: "input1,input2" (use comma as a delimiter if multiple topics)
      OUTPUT_TOPIC: "output"

# Optional: JSONPath for the field to use as key.
# If not specified, kafka will randomly choose a partition
      PARTITION_KEY: "$.userId"
```

## Partition key
* If not specified, the key in the output topic will be null, and a random partition will be assigned.
* If a [JsonPath](https://github.com/json-path/JsonPath) is specified, the message will be parsed, and the key will be 
the value of the specified field. 

Kafka guarantees that messages with the same key will always be on the same partition, so that order is preserved. 

Example:

```json
{
  "type": "user-click",
  "userId": 15
}
```

When using the `PARTITION_KEY: "$.userId"`, the output topic will have a message with key = `15` and value = `{"type":"user-click","userId":15}`
