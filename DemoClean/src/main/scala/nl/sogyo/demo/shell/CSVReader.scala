package nl.sogyo.demo.shell

import nl.sogyo.demo.core.CSVWorkflow.LineReader
import nl.sogyo.demo.core.{FileNotFoundError, FileReadingError, WorkflowError}

import java.io.{FileNotFoundException, IOException}
import scala.io.Source
import scala.util.Using
import scala.util.control.NonFatal

// ============================================================
// STAGE 1 — PARSE FILE
// Throws built-in exceptions. This is the imperative-shell boundary:
// touching the filesystem is unavoidably impure.
// ============================================================

object CSVReader {

  // Adapter 1: Reads raw files from disk
  object FileBasedReader extends LineReader {
    override def readLines(path: String): Either[WorkflowError, List[String]] =
      try
        Right(
          Using.resource(Source.fromFile(path)) { source =>
          source.getLines().toList
        }
        )
      catch {
        case _: FileNotFoundException => Left(FileNotFoundError(None))
        case NonFatal(e) => Left(FileReadingError(Some(e)))
      }
  }

  // Adapter 2: Reads classpath resources from stream
  object StreamBasedReader extends LineReader {
    override def readLines(resourcePath: String): Either[WorkflowError, List[String]] = {
      val resourceStream = this.getClass.getResourceAsStream(resourcePath)

      if (resourceStream == null) {
        Left(FileNotFoundError(None))
      } else {
        try {
          Right(
            Using.resource(Source.fromInputStream(resourceStream)) { source =>
              source.getLines().toList
            }
          )
        } catch {
          case NonFatal(e) => Left(FileReadingError(Some(e)))
        } 
      }
    }
  }
}