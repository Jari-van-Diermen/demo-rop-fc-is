package nl.sogyo.demo.core

sealed trait DataError
case class EmptyTable() extends DataError:
  def message: String = "Data error: The data contains no data rows."

case class DataTransformationException(
  message: String,
  rawData: Array[String],
  cause: Throwable = null
  ) extends RuntimeException(message, cause)