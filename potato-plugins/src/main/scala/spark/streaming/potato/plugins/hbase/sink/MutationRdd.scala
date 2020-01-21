package spark.streaming.potato.plugins.hbase.sink

import org.apache.hadoop.hbase.client.Mutation
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import spark.streaming.potato.plugins.hbase.GlobalConnectionCache._
import spark.streaming.potato.plugins.hbase.HBaseImplicits.mapToConfiguration

class MutationRdd(rdd: RDD[MutationAction]) extends Logging with Serializable {
  def saveToHBaseTable(conf: Map[String, String], table: String, batchSize: Int = 100): Unit = {
    rdd.foreachPartition { part =>
      var count = 0
      withMutator(conf, table) { mutator =>
        withBufferedTable(conf, table) { btbl =>
          part.foreach { mutate =>
            mutate match {
              case MutationAction(MutationType.APPEND, mutation) =>
                btbl.add(mutation)
              case MutationAction(MutationType.INCREMENT, mutation) =>
                btbl.add(mutation)
              case MutationAction(MutationType.DELETE, mutation) =>
                mutator.mutate(mutation)
              case MutationAction(MutationType.PUT, mutation) =>
                mutator.mutate(mutation)
              case m: MutationAction =>
                logWarning(s"Uknown mutation $m")
            }
            count += 1
            if (count >= batchSize) {
              mutator.flush()
              btbl.flush()
              count = 0
            }
          }
        }
      }
    }
  }
}

case class MutationAction(action: MutationType.Type, mutation: Mutation)

object MutationType extends Enumeration {
  type Type = Value
  val APPEND, DELETE, PUT, INCREMENT = Value
}