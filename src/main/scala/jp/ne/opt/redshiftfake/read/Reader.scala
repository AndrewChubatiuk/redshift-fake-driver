package jp.ne.opt.redshiftfake.read

import jp.ne.opt.redshiftfake._
import jp.ne.opt.redshiftfake.s3.{S3Location, S3Service}

/**
 * Provides features to download text from dataSource and read into rows.
 */
class Reader(copyCommand: CopyCommand, columnDefinitions: Seq[ColumnDefinition], s3Service: S3Service) {

  // TODO: Support more dataSources and formats
  def read(): Seq[Row] = {

    val contents = (copyCommand.copyFormat, copyCommand.dataSource) match {
      case (CopyFormat.Manifest(location), _) =>
        val rawManifest = s3Service.downloadAsString(location)
        val manifest = new Manifest(rawManifest)
        manifest.files.map(s3Service.downloadAsString)
      case (_, CopyDataSource.S3(location)) =>
        downloadAllAsStringFromS3(location)
    }

    copyCommand.copyFormat match {
      case CopyFormat.Json(Some(jsonpathsLocation)) =>
        val rawJsonpaths = s3Service.downloadAsString(S3Location(jsonpathsLocation.bucket, jsonpathsLocation.prefix))
        val jsonpaths = new Jsonpaths(rawJsonpaths)

        (for {
          content <- contents
          line <- content.trim.lines
        } yield {
          val jsonReader = jsonpaths.mkReader(line.trim)
          val columns = columnDefinitions.zipWithIndex.map { case (_, n) =>
            Column(jsonReader.valueAt(n))
          }
          Row(columns)
        })(collection.breakOut)
      case CopyFormat.Manifest(manifestLocation) =>
        (for {
          content <- contents
          line <- content.trim.lines
        } yield {
          val csvReader = new CsvReader(line)
          csvReader.toRow
        })(collection.breakOut)
      case _ => Nil
    }
  }

  private[this] def downloadAllAsStringFromS3(location: S3Location): Seq[String] = {
    val summaries = s3Service.lsRecurse(location)
    summaries.map { summary =>
      s3Service.downloadAsString(S3Location(location.bucket, summary.getKey))
    }
  }
}
