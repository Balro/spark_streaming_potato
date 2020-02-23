package spark.streaming.potato.quickstart

import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import spark.streaming.potato.plugins.kafka.source._
import org.elasticsearch.spark._
import spark.streaming.potato.template.KafkaSourceTemplate

object Kafka2EsDemo extends KafkaSourceTemplate[String] {
  override def initKafka(ssc: StreamingContext): (DStream[String], OffsetsManager) =
    KafkaSourceUtil.valueDStream(ssc)

  override def doWork(args: Array[String]): Unit = {
    getStream.foreachRDD { rdd =>
      rdd.saveToEs("test")
    }
  }
}
