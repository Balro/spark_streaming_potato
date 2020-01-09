package spark.streaming.potato.quickstart

import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.streaming.{Seconds, StreamingContext}
import spark.streaming.potato.core.source.kafka.KafkaSource

object KafkaDemo extends Logging {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    val ssc = new StreamingContext(conf, Seconds(10))

    val (stream, manager) = KafkaSource.valueDStream(ssc)

    stream.foreachRDD(rdd => rdd.foreach(r => println(r)))

    ssc.start()
    ssc.awaitTermination()
  }
}
