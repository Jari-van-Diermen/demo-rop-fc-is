package nl.sogyo.demo.shell

import nl.sogyo.demo.core.CSVWorkflow.*
import nl.sogyo.demo.core.WorkflowUtils.makePassthrough

import java.nio.file.Path

@main def main(args: String*): Unit = {
  // Add required arguments
  val expected = List("name", "email", "age")
  val path = if (args.nonEmpty) args(0) else "/customers.csv"
  
  // Partially apply the chosen LineReader with the parser using currying
  val csvStreamReader: LineReader = CSVReader.StreamBasedReader
  val streamParse = parse(csvStreamReader)

  // Partially apply validateSchema and make the function Passthrough
  val schemaValidate = validateSchema(_, expected)
  val schemaValidatePassthrough = makePassthrough(schemaValidate)

  // Partially apply loggin function
  val PrintInValidLines = logInvalidLines(println)

  // Get database and partially apply the saveing function
  val mockRepository = MockRepository
  val mockSaveCustomers = saveCustomers(mockRepository)

  Right(path)
    .flatMap(streamParse)
    .flatMap(schemaValidatePassthrough)
    .flatMap(PrintInValidLines)
    .flatMap(filterLines)
    .flatMap(transformRows)
    .flatMap(mockSaveCustomers)
    //.foreach(lines => lines.foreach(line => line.mkString(" ")))
}


