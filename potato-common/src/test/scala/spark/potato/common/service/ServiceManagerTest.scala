package spark.potato.common.service

import org.apache.spark.{SparkConf, SparkContext}
import org.junit.Test

class ServiceManagerTest {
  @Test
  def serveConfTest(): Unit = {
    val sm = new ServiceManager().conf(Map("a" -> "1", "b" -> "2"))
    val service = sm.serve(classOf[TestGeneralService])
    service.start()
    service.stop()
  }

  @Test
  def serveSCTest(): Unit = {
    val conf = new SparkConf()
    conf.setMaster("local").setAppName("ServeScTest")
    //    val sm = new ServiceManager().conf(conf)
    val sm = new ServiceManager().sc(SparkContext.getOrCreate(conf))
    val service = sm.serve(classOf[TestContextService].getName)
    service.start()
    service.stop()
  }
}

class TestGeneralService extends GeneralService {
  private var conf: Map[String, String] = _

  override def serve(conf: Map[String, String]): GeneralService = {
    this.conf = conf
    this
  }

  override def start(): Unit = {
    println(s"Service startted with sc $conf.")
  }

  override def stop(): Unit = {
    println("Service stopped.")
  }
}

class TestContextService extends ContextService {
  private var sc: SparkContext = _

  override def serve(sc: SparkContext): ContextService = {
    this.sc = sc
    this
  }

  override def start(): Unit = {
    println(s"Service startted with sc $sc.")
  }

  override def stop(): Unit = {
    sc.stop()
    println("Service stopped.")
  }
}
