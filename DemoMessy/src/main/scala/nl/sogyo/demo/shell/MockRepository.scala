package nl.sogyo.demo.shell

import nl.sogyo.demo.core.CSVWorkflow.{CustomerRow, DbRepository}

import java.io.IOException

object MockRepository extends DbRepository {

  var simulateNetworkFailure: Boolean = false
  private var store: List[CustomerRow] = Nil
  override def CustomerStore: List[CustomerRow] = store
  override def saveToRepo(customerData: List[CustomerRow]): Unit = {
    if (simulateNetworkFailure) {
      throw new IOException("Could not connect to database")
    }
    store = customerData
    println(s"[MockDB] Successfully saved ${customerData.length} customers to the database!")
  }
}
