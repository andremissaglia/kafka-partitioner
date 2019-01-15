package partitioner

import java.util.Properties

import com.fasterxml.jackson.databind.JsonNode
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider
import com.jayway.jsonpath.{Configuration, JsonPath}
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.apache.kafka.streams.scala.StreamsBuilder
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.Serdes._
import org.slf4j.LoggerFactory

import scala.io.Source
import scala.util.Try

object Main extends App {
  val inputReader = Source.fromFile("config.properties", "UTF-8").reader()
  val config: Properties = {
    val p = new Properties()
    p.load(inputReader)
    p
  }
  val builder = new StreamsBuilder()
  val keyField = sys.env.get("PARTITION_KEY")

  val inputStream = builder
    .stream[String, String]("com.arquivei.dataeng.andre")
  val log = LoggerFactory.getLogger(this.getClass)
  val mapStream = keyField match {
    case None => inputStream
    case Some(keyPath) =>
      val path = JsonPath.compile(keyPath)
      val jsonPathConfig = Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider()).build()

      inputStream
      .map((k,v) => {
        val newKey = Try(path.read[JsonNode](v, jsonPathConfig).toString).getOrElse("")
        newKey -> v
      })
  }

  mapStream.to("com.arquivei.dataeng.andre2")

  val topology = builder.build()

  new KafkaStreams(topology, config).start()

}
