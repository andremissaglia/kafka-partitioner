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
import org.apache.kafka.streams.scala.kstream.KStream
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
    .stream[String, String](sys.env("INPUT_TOPIC").split(",").toSet)
  val log = LoggerFactory.getLogger(this.getClass)
  val mapStream: KStream[String,String] = keyField match {
    case None => inputStream.map((_, value) => (null, value))
    case Some(keyPath) =>
      val path = JsonPath.compile(keyPath)
      val jsonPathConfig = Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider()).build()

      inputStream
      .map((k,v) => {
        val newKey = Try(path.read[JsonNode](v, jsonPathConfig).toString).getOrElse("")
        newKey -> v
      })
  }

  mapStream.to(sys.env("OUTPUT_TOPIC"))

  val topology = builder.build()

  new KafkaStreams(topology, config).start()

}
