package spark.streaming.potato.template.template

import org.apache.spark.internal.Logging
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.DStream
import spark.streaming.potato.plugins.kafka.source.KafkaSource
import spark.streaming.potato.common.conf.CommonConfigKeys._
import spark.streaming.potato.plugins.kafka.KafkaConfigKeys._
import spark.streaming.potato.plugins.kafka.source.offsets.OffsetsManager
import spark.streaming.potato.plugins.lock.LockConfigKeys._

object KafkaErrTest extends KafkaSourceTemplate[(String, String)] with Logging {
  override def initKafka(ssc: StreamingContext, kafkaParam: Map[String, String]): (DStream[(String, String)], OffsetsManager) =
    KafkaSource.kvDStream(ssc, kafkaParam)

  override def doWork(args: Array[String]): Unit = {
    stream.foreachRDD { rdd =>
      println(1 / (rdd.count() - 1))
    }
  }

  override def afterConfCreated(args: Array[String]): Unit = {
    super.afterConfCreated(args)
    conf.setMaster("local[10]").setAppName("test")
    conf.set(POTATO_STREAMING_SLIDE_DURATION_SECONDS_KEY, "5")
    conf.set(OFFSETS_STORAGE_KEY, "zookeeper")
    conf.set(CONSUMER_OFFSET_RESET_POLICY, "earliest")
    conf.set(SUBSCRIBE_TOPICS_KEY, "test")
    conf.set(CONSUMER_BOOTSTRAP_SERVERS_KEY, "test01:9092,test02:9092")
    conf.set(CONSUMER_GROUP_ID_KEY, "kafka_print_test")

    conf.set(POTATO_RUNNING_LOCK_ENABLE_KEY, "true")
    conf.set(POTATO_RUNNING_LOCK_ZOOKEEPER_ADDR_KEY, "test02:2181")
    conf.set(POTATO_RUNNING_LOCK_ZOOKEEPER_PATH_KEY, "/potato/lock/test")
  }
}
