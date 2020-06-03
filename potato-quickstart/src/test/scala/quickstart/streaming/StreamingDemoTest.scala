package quickstart.streaming

import org.junit.Test
import potato.spark.util.LocalLauncherUtil

class StreamingDemoTest {
  @Test
  def localTest(): Unit = {
    LocalLauncherUtil.launch(StreamingDemo, propFile = "/quickstart/streaming/StreamingDemo.properties")
  }
}
