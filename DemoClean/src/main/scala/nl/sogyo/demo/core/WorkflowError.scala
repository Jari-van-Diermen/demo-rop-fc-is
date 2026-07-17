package nl.sogyo.demo.core

sealed trait WorkflowError:
  def message: String
  def cause: Option[Throwable] = None
case class FileReadingError(override val cause: Option[Throwable] = None) extends WorkflowError:
  def message: String = s"Failed to read file. Reason: ${cause.map(_.getMessage).getOrElse("Unknown")}"
case class FileNotFoundError(override val cause: Option[Throwable] = None) extends WorkflowError:
  def message: String = s"Failed to find file. Reason: ${cause.map(_.getMessage).getOrElse("Unknown")}"
case class DatabaseConnectionError(override val cause: Option[Throwable] = None) extends WorkflowError:
  def message: String = s"Failed to connect to the database. Reason: ${cause.map(_.getMessage).getOrElse("Unknown")}"

sealed trait SchemaError extends WorkflowError
case class MissingColumn(name: String) extends SchemaError:
  def message: String = s"Schema validation failed: Missing expected column '$name'."
case class TooFewColumns(expected: Int, actual: Int) extends SchemaError:
  def message: String = s"Schema validation failed: Expected $expected columns, but found $actual."
case class MissingField(field: String, rowIndex: Int) extends SchemaError:
  def message: String = s"Schema validation failed at row #$rowIndex: Missing value for '$field'."

sealed trait DataError extends WorkflowError
case class EmptyTable() extends DataError:
  def message: String = "Data error: The data contains no data rows."
case class DataTransformError(row: Array[String]) extends DataError:
  def message: String = s"Data error: Error transforming row\nRow: ${row.mkString(" ")}"