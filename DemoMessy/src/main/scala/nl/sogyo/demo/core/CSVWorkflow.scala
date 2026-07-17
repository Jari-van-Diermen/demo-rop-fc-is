package nl.sogyo.demo.core

import nl.sogyo.demo.core.WorkflowUtils.*
import nl.sogyo.demo.shell.CSVReader.{FileError, FileNotFoundError, FileReadingError}

import java.io.{FileNotFoundException, IOException}
import scala.annotation.tailrec
import scala.io.Source
import scala.util.{Try, Using}
import scala.util.control.NonFatal

object CSVWorkflow {

  // ============================================================
  // DOMAIN Objects
  // ============================================================

  case class CustomerRow(name: String, email: String, age: Int):
    def mkString(sep: String): Unit = println(s"${name}${sep}${email}${sep}${age.toString}")

  trait LineReader {
    /** Reads all lines from the given path, handling resource opening/closing safely. */
    def readLines(path: String): Either[FileError, List[String]]
  }

  trait DbRepository {
    def CustomerStore: List[CustomerRow]

    def saveToRepo(customerData: List[CustomerRow]): Unit
  }

  // ============================================================
  // Parsing
  // ============================================================

  def parse(path: String): Either[FileError, List[Array[String]]] =
    
    def readLines(resourcePath: String): Either[FileError, List[String]] = {
      val resourceStream = this.getClass.getResourceAsStream(resourcePath)

      if (resourceStream == null) {
        Left(FileNotFoundError())
      } else {
        try {
          Right(
            Using.resource(Source.fromInputStream(resourceStream)) { source =>
              source.getLines().toList
            }
          )
        } catch {
          case NonFatal(e) => Left(FileReadingError())
        }
      }
    }
    
    for {
      lines <- readLines(path)
    } yield lines
      .filterNot(_.trim.isEmpty)
      .map(_.split(",").map(_.trim))

  // ============================================================
  // Validation
  // ============================================================

  def validateSchema(lines: List[Array[String]], expected: List[String]): Unit = {
    lines.head match {
      case header if header.length < expected.length =>
        throw new IllegalArgumentException("Too few columns")
      case header =>
        val missing = expected.find(col => !header.map(_.toLowerCase).contains(col))
        missing match {
          case Some(col) => IllegalArgumentException("Missing Column")
          case None => ()
        }
    }
  }

  // ============================================================
  // Filtering out bad rows (and some necessary)
  // ============================================================

  @tailrec
  private def recursiveFilter(
    lines: List[Array[String]],
    acc: List[Array[String]] = Nil,
    currentIndex: Int = 0
  ): List[Array[String]] =
    lines match {
      case Nil => acc.reverse
      case head :: tail if validateLine(head) => recursiveFilter(tail, head :: acc, currentIndex + 1)
      case head :: tail =>
        println(s"Logging: The ${currentIndex}th index is not formatted correctly: ${head.mkString(" ")}")
        recursiveFilter(tail, acc, currentIndex + 1)
    }

  def filterLines(lines: List[Array[String]]): Option[List[Array[String]]] = {
    // Tail removes the header
    lines match
      case head :: Nil => None
      case head :: tail => Some(recursiveFilter(tail))
  }

  // ============================================================
  // Transforming to customer value objects
  // ============================================================

  @tailrec
  private def recursiveTransform(lines: List[Array[String]], acc: List[CustomerRow] = Nil): List[CustomerRow] =
    lines match
      case Nil => acc.reverse
      case Array(name: String, email: String, age: String, _*) :: tail =>
        recursiveTransform(tail, CustomerRow(name, email, age.trim.toInt) :: acc)
      case head :: _ => throw DataTransformationException("Data could not be transformed", head)

  def transformRows(lines: List[Array[String]]): List[CustomerRow] = {
    lines match
      case Nil => throw new IllegalArgumentException("Datatable is empty")
      case lines => recursiveTransform(lines)
  }

  // ============================================================
  // Saving customers
  // ============================================================

  def saveCustomers(dbRepo: DbRepository)(customerData: List[CustomerRow]): Unit = {
    customerData match
      case Nil => throw new IllegalArgumentException("No customers in list")
      case data =>
        try {
          dbRepo.saveToRepo(data)
        } catch {
          case e: Exception => throw new IOException()
        }
  }
}