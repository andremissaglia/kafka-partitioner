#!/bin/sh

if [ -z "$CONFIG_FILE" ]; then
    CONFIG_FILE="/config.properties"
    [ -z $KAFKA_USER ] && echo "KAFKA_USER is not set!" && exit 1
    [ -z $$KAFKA_PASS ] && echo "KAFKA_PASS is not set!" && exit 1
    [ -z $KAFKA_APPLICATION_ID ] && echo "KAFKA_APPLICATION_ID is not set!" && exit 1
    [ -z $KAFKA_SERVER ] && echo "KAFKA_SERVER is not set!" && exit 1

    cat > $CONFIG_FILE <<EOF
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="$KAFKA_USER" password="$KAFKA_PASS";
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
application.id=$KAFKA_APPLICATION_ID
bootstrap.servers=$KAFKA_SERVER
EOF

    if [ "$ENABLE_EOS" = "y" ]; then
        cat >> $CONFIG_FILE <<EOF
processing.guarantee=exactly_once
EOF
    fi
fi

[ ! -e $CONFIG_FILE ] && echo "$CONFIG_FILE does not exists!" && exit 1
[ -z $INPUT_TOPIC ] && echo "INPUT_TOPIC is not set" && exit 1
[ -z $OUTPUT_TOPIC ] && echo "OUTPUT_TOPIC is not set" && exit 1

[ -z $PARTITION_KEY ] && echo "PARTITION_KEY is not set, order can't be guaranteed"

/usr/bin/java -jar /app.jar