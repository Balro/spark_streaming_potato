package potato.kafka08.offsets.cmd

import kafka.consumer.ConsumerConfig
import org.apache.spark.SparkConf
import potato.common.cmd.ActionCMDBase
import potato.kafka08.offsets.manager.{OffsetsManager, OffsetsManagerConf}
import potato.kafka08.utils.OffsetsUtil

object OffsetsCmd extends ActionCMDBase {
  /**
   * 添加action,argument以及其他初始化。
   */
  override def init(): Unit = {
    /*
    显示给定groupId的offsets。
     */
    addAction("list", "show committed offsets.",
      action = () => {
        val conf = new OffsetsManagerConf(new SparkConf(), Map.empty[String, String])
        val manager = new OffsetsManager(conf)
        output(s"list status for groupId: ${conf.groupId}\n")
        output("%-20s  %-10s  offset".format("topic", "partition"))
        manager.committedOffsets(reset = false).foreach { tap =>
          output(f"${tap._1.topic}%-20s  ${tap._1.partition}%-10s  ${tap._2}")
        }
      }
    )

    /*
    显示给定groupId的lag。
     */
    addAction("lag", "show current lag.",
      action = () => {
        val conf = new OffsetsManagerConf(new SparkConf(), Map.empty[String, String])
        val manager = new OffsetsManager(conf)
        output(s"lag for groupId: ${conf.groupId}\n")
        output("%-20s  lag".format("topic"))
        manager.getLag().foreach { tl =>
          output(f"${tl._1}%-20s  ${tl._2}")
        }
      }
    )

    /*
    重置给定groupId的offsets。
     */
    addAction("reset", "reset offsets to earliest or latest.",
      action = () => {
        val conf = new OffsetsManagerConf(new SparkConf(), Map.empty[String, String])
        val manager = new OffsetsManager(conf)

        val policy = props.get("--reset-to") match {
          case Some("earliest") => "smallest"
          case Some("latest") => "largest"
          case Some(other) => other
          case None =>
        }

        implicit val consumerConfig: ConsumerConfig = conf
        val resetOffsets = policy match {
          case "smallest" => OffsetsUtil.getEarliestOffsets(manager.brokers, manager.subscriptions)
          case "largest" => OffsetsUtil.getLatestOffsets(manager.brokers, manager.subscriptions)
          case other => throw new Exception(s"Unknown reset policy $other")
        }

        output("reset offsets:")
        output("%-20s  %-10s  offset".format("topic", "partition"))
        resetOffsets.foreach { tapo =>
          output(f"${tapo._1.topic}%-20s  ${tapo._1.partition}%-10s  ${tapo._2}")
        }

        if (props.contains("--execute")) {
          manager.updateOffsets(resetOffsets)
          output("\nReset offsets confirmed.")
        } else {
          output("\nUse arg '--execute' to confirm.")
        }
      },
      neededArgs = Set("--reset-to")
    )

    addArgument("--reset-to", describe = "earliest or latest.", needValue = true)
    addArgument("--execute", describe = "confirm reset action.")
  }
}
