package nl.sogyo.demo.core

import nl.sogyo.demo.core.WorkflowUtils.*

import java.io.FileNotFoundException
import scala.annotation.tailrec
import scala.util.Try
import scala.util.control.NonFatal

object CSVWorkflow {

  // ============================================================
  // DOMAIN Objects
  // ============================================================

  case class CustomerRow(name: String, email: String, age: Int):
    def mkString(sep: String): Unit = println(s"${name}${sep}${email}${sep}${age.toString}")

  trait LineReader {
    /** Reads all lines from the given path, handling resource opening/closing safely. */
    def readLines(path: String): Either[WorkflowError, List[String]]
  }

  trait DbRepository {
    def CustomerStore: List[CustomerRow]

    def saveToRepo(customerData: List[CustomerRow]): Unit
  }

  // ============================================================
  // Parsing
  // ============================================================

  def parse(reader: LineReader)(path: String): Either[WorkflowError, List[Array[String]]] =
    for {
      lines <- reader.readLines(path)
    } yield lines
      .filterNot(_.trim.isEmpty)
      .map(_.split(",").map(_.trim))

  // ============================================================
  // Validation
  // ============================================================

  def validateSchema(lines: List[Array[String]], expected: List[String]): Either[WorkflowError, Unit] = {
    lines.head match {
      case header if header.length < expected.length =>
        Left(TooFewColumns(expected.length, header.length))
      case header =>
        val missing = expected.find(col => !header.map(_.toLowerCase).contains(col))
        missing match {
          case Some(col) => Left(MissingColumn(col))
          case None => Right(())
        }
    }
  }

  // ============================================================
  // Logging
  // ============================================================

  def logInvalidLines(logging: String => Unit)(lines: List[Array[String]]): Either[WorkflowError, List[Array[String]]] =
    lines match
      case Nil => Left(EmptyTable())
      case head :: Nil => Left(EmptyTable())
      case head :: tail =>
        tail.zipWithIndex.foreach((line, index) =>
          if !validateLine(line) then logging(s"Logging: The ${index + 1}th index is not formatted correctly: ${line.mkString(" ")}"))
        Right(lines)

  // ============================================================
  // Filtering out bad rows
  // ============================================================

  def filterLines(lines: List[Array[String]]): Either[WorkflowError, List[Array[String]]] = {

    @tailrec
    def recursiveFilter(lines: List[Array[String]], acc: List[Array[String]] = Nil): List[Array[String]] =
      lines match {
        case Nil => acc.reverse
        case head :: tail if validateLine(head) => recursiveFilter(tail, head :: acc)
        case _ :: tail => recursiveFilter(tail, acc)
      }

    // Tail removes the header
    lines match
      case Nil => Left(EmptyTable())
      case head :: Nil => Left(EmptyTable())
      case head :: tail => Right(recursiveFilter(tail))
  }

  // ============================================================
  // Transforming to customer value objects
  // ============================================================

  def transformRows(lines: List[Array[String]]): Either[WorkflowError, List[CustomerRow]] = {
    @tailrec
    def recursiveTransform(lines: List[Array[String]], acc: List[CustomerRow] = Nil): Either[WorkflowError, List[CustomerRow]] =
      lines match
        case Nil => Right(acc.reverse)
        case Array(name: String, email: String, age: String, _*) :: tail =>
          recursiveTransform(tail, CustomerRow(name, email, age.trim.toInt) :: acc)
        case head :: _ => Left(DataTransformError(head))
        
    lines match
      case Nil => Left(EmptyTable())
      case lines => recursiveTransform(lines)
  }

  // ============================================================
  // Saving customers
  // ============================================================

  def saveCustomers(dbRepo: DbRepository)(customerData: List[CustomerRow]): Either[WorkflowError, Unit] = {
    customerData match
      case Nil => Left(EmptyTable())
      case data =>
        try {
          dbRepo.saveToRepo(data)
          Right(())
        } catch {
          case e: Exception =>
            Left(DatabaseConnectionError(Some(e)))
        }
  }
}