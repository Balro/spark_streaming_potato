package potato.spark.service

import java.util.concurrent.atomic.AtomicBoolean

import org.apache.spark.SparkContext
import org.apache.spark.internal.Logging
import org.apache.spark.streaming.StreamingContext
import potato.common.utils.JVMCleanUtil

/**
 * service特质，用于创建附加服务。
 */
trait Service extends Logging {
  val serviceName: String

  private val started = new AtomicBoolean(false)

  /**
   * 启动服务方法，禁止外部调用，启动服务请使用[[checkAndStart]]。
   */
  protected def start(): Unit

  /**
   * 检查服务状态，如服务未启动则启动服务，如服务已启动则不做操作。
   */
  def checkAndStart(): Unit = this.synchronized {
    if (started.get()) {
      logWarning(s"Service $this already started.")
      return
    }
    start()
    started.set(true)
  }

  /**
   * 停止服务方法，禁止外部调用，停止服务请使用[[checkAndStop]]。
   */
  protected def stop(): Unit

  def checkAndStop(): Unit = this.synchronized {
    if (started.get()) {
      stop()
      started.set(false)
      return
    }
    logWarning(s"Service $this already stopped.")
  }

  private val registeredStopOnJVMExit = new AtomicBoolean(false)

  /**
   * 当需要在jvm停止时同时停止服务，则调用该方法。当jvm退出时会自动调用stop()。
   */
  def stopOnJVMExit(): Unit = {
    if (registeredStopOnJVMExit.get()) {
      logInfo(s"Service ${this.getClass} already registered stop on jvm exit.")
      return
    }
    logInfo(s"Service ${this.getClass} registered stop on jvm exit.")
    JVMCleanUtil.cleanWhenShutdown(s"${this.getClass}", { () =>
      stop()
    })
  }

  /**
   * 快捷方法，启动服务同时盗用stopOnJVMExit()方法。
   */
  def startAndStopOnJVMExit(): Unit = {
    checkAndStart()
    stopOnJVMExit()
  }

  override def toString: String = serviceName
}

/**
 * 只需要配置文件，不依赖spark运行的服务。
 */
trait GeneralService extends Service {
  /**
   * 初始化服务。
   */
  def serve(conf: Map[String, String]): GeneralService
}

/**
 * 依赖SparkContext的服务。
 */
trait ContextService extends Service {
  /**
   * 初始化服务。
   */
  def serve(sc: SparkContext): ContextService
}

/**
 * 依赖StreamingContext的服务。
 */
trait StreamingService extends Service {
  /**
   * 初始化服务。
   */
  def serve(ssc: StreamingContext): StreamingService
}
