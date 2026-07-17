package nl.sogyo.demo.core

object WorkflowUtils {
  def makePassthrough[A, B](f: A => Either[B, Unit]): A => Either[B, A] = { input =>
    f(input) match {
      case Right(_) => Right(input)
      case Left(err) => Left(err)
    }
  }

  private def isValidName(field: String): Boolean =
    field != null && field.trim.nonEmpty

  // --- Helper 2: Check if the email contains an '@' sign
  private def isValidEmail(field: String): Boolean =
    field != null && field.contains("@")

  private def isValidAge(field: String): Boolean =
    if (field == null) false
    else {
      try {
        field.trim.toInt
        true
      } catch {
        case _: NumberFormatException => false
      }
    }

  // --- Main Validator
  def validateLine(line: Array[String]): Boolean = {
    // Ensure the array has at least the 3 fields we expect before checking them
    line.length >= 3 &&
      isValidName(line(0)) &&
      isValidEmail(line(1)) &&
      isValidAge(line(2))
  }
}
