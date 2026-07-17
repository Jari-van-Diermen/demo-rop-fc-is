package nl.sogyo.demo.shell

import nl.sogyo.demo.core.CSVWorkflow.*

@main def main(args: String*): Unit = {
  // Add required arguments
  val expected = List("name", "email", "age")
  val path = if (args.nonEmpty) args(0) else "/customers.csv"

  // Get database and partially apply the saving function
  val mockRepository = MockRepository
  val mockSaveCustomers = saveCustomers(mockRepository)

  Right(path)
    .flatMap(parse)
    .foreach(lines => lines.foreach(line => println(line.mkString(" "))))
}


