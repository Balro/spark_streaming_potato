package spark.potato.common.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.streaming.{Duration, Milliseconds, StreamingContext}
import spark.potato.common.conf.POTATO_COMMON_STREAMING_BATCH_DURATION_MS_KEY
import spark.potato.common.exception.ConfigNotFoundException
import spark.potato.common.utils.JVMCleanUtil

object StreamingUtil extends Logging {
  /**
   * 获取StreamingContext的批处理时间。
   */
  def getBatchDuration(ssc: StreamingContext): Duration = {
    val sscClazz = classOf[StreamingContext]
    val graphField = sscClazz.getDeclaredField("graph")
    graphField.setAccessible(true)
    val graphClazz = Class.forName("org.apache.spark.streaming.DStreamGraph")
    val durationField = graphClazz.getDeclaredField("batchDuration")
    durationField.setAccessible(true)
    durationField.get(graphField.get(ssc)).asInstanceOf[Duration]
  }

  /**
   * 读取conf中的 POTATO_STREAMING_BATCH_DURATION_MS_KEY 参数作为batchDuration，创建StreamingContext。
   *
   * @param conf     SparkConf
   * @param duration 手动指定duration，-1表示从配置文件读取。其他值表示覆盖配置文件。
   * @return
   */
  def createStreamingContextWithDuration(conf: SparkConf, duration: Long = -1L): StreamingContext = {
    if (duration <= 0) {
      if (conf.contains(POTATO_COMMON_STREAMING_BATCH_DURATION_MS_KEY))
        new StreamingContext(conf, Milliseconds(conf.get(POTATO_COMMON_STREAMING_BATCH_DURATION_MS_KEY).toLong))
      else
        throw ConfigNotFoundException(s"Config: $POTATO_COMMON_STREAMING_BATCH_DURATION_MS_KEY not found.")
    } else {
      new StreamingContext(conf, Milliseconds(duration))
    }
  }

  def stopOnJVMExit(ssc: StreamingContext): Unit = {
    logInfo(s"Register stop when shutdown on ssc $ssc")
    JVMCleanUtil.cleanWhenShutdown("stop ssc", { () =>
      ssc.stop()
    })
  }
}