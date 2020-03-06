package spark.potato.quickstart.streaming

import java.util.Date

import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import spark.potato.hbase.sink._
import spark.potato.kafka.source._
import spark.potato.template.KafkaSourceTemplate

object Kafka2HBaseDemo extends KafkaSourceTemplate[String] {
  override def initKafka(ssc: StreamingContext): (DStream[String], OffsetsManager) =
    KafkaSourceUtil.valueDStream(ssc)

  override def doWork(args: Array[String]): Unit = {
    getStream.transform { rdd =>
      rdd.flatMap { f =>
        f.split("\\s+")
      }.distinct().filter {
        _.length > 0
      }.map { f =>
        MutationAction(MutationType.PUT, new Put(Bytes.toBytes(f)).addColumn(
          Bytes.toBytes("f1"),
          Bytes.toBytes("date"),
          Bytes.toBytes(new Date().toString)
        ))
      }
    }.saveToHBase(getConf, "test")
  }
}