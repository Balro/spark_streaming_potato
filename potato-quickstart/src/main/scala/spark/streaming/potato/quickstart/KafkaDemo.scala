package spark.streaming.potato.quickstart

import kafka.serializer.StringDecoder
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.KafkaUtils
import spark.streaming.potato.template.GeneralTemplate

object KafkaDemo extends GeneralTemplate {
  override def doWork(args: Array[String]): Unit = {
    val props = Map(
      "bootstrap.servers" -> "test01:9092",
      //      "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
      //      "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
      "auto.offset.reset" -> "largest",
      "enable.auto.commit" -> "true"
    )

    val stream: InputDStream[(String, String)] = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, props, Set("test_topic"))

    stream.flatMap(f => f._2.split("\\s")).map((_, 1)).reduceByKey(_ + _).print()
  }

  override def initConf(args: Array[String]): Unit = conf.setMaster("local[6]").setAppName("hello")
}