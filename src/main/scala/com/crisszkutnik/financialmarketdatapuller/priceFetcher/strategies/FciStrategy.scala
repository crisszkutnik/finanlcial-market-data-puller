package com.crisszkutnik.financialmarketdatapuller.priceFetcher.strategies

import FciStrategy.BASE_PATH
import com.crisszkutnik.financialmarketdatapuller.priceFetcher.{AssetType, Currency, Market, Source, TickerPriceInfo}
import com.typesafe.scalalogging.Logger
import org.apache.commons.io.FileUtils
import org.apache.poi.ss.usermodel.WorkbookFactory
import sttp.client4.UriContext

import java.io.{File, FileOutputStream}
import java.nio.channels.Channels
import java.nio.file.{FileAlreadyExistsException, Files, Path}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}


class FciStrategy(
 private val logger: Logger = Logger[FciStrategy]
) extends PriceFetcher:
  val source: Source = Source.CAFCI
  
  def canHandle(market: Market, ticker: String, assetType: AssetType): Boolean =
    (market, ticker, assetType) match
      case (Market.BCBA, _, AssetType.MUTUAL_FUND) => true
      case _ => false

  private def getFilePath: Path =
    val currentTime = LocalDateTime.now()
    val yesterdayTime = LocalDateTime.now().minusDays(1)
    val fileNameFormatter = DateTimeFormatter.ofPattern("YYYY-MM-dd")
    val hourFormatter = DateTimeFormatter.ofPattern("HH")

    val formattedYesterdayFileName = yesterdayTime.format(fileNameFormatter)
    val formattedFileName = currentTime.format(fileNameFormatter)
    val formattedHour = currentTime.format(hourFormatter)

    val hour = Integer.parseInt(formattedHour)

    val fileName =
      hour match
        case _ if hour >= 19 => formattedFileName
        case _ => formattedYesterdayFileName

    Path.of(BASE_PATH + s"/${fileName}.xlsx")

  private def downloadFile(path: Path): File =
    logger.info("Downloading updated FCI file")
    val timestamp = Date().getTime
    val uri = uri"https://api.cafci.org.ar/pb_get?d=${timestamp}"
    val readableByteChannel = Channels.newChannel(uri.toJavaUri.toURL.openStream())

    val file = path.toFile

    val fileOutputStream = FileOutputStream(file)
    val fileChannel = fileOutputStream.getChannel

    fileChannel.transferFrom(readableByteChannel, 0, Long.MaxValue)
    fileChannel.close()
    readableByteChannel.close()
    fileOutputStream.close()

    file

  private def createDirectory(): Try[Unit] =
    Try {
      val p = Path.of(BASE_PATH)
      val _ = Files.createDirectory(p)
    }

  private def retrieveFile(): Try[File] =
    Try {
      val filePath = getFilePath
      val exists = Files.exists(filePath)

      if exists then
        filePath.toFile
      else
        FileUtils.cleanDirectory(Path.of(BASE_PATH).toFile)
        downloadFile(filePath)
    }

  private def readFromSpreadsheet(fciName: String, file: File): Option[TickerPriceInfo] =
    val data = Try {
      val wb =  WorkbookFactory.create(file)
      val sheet = wb.getSheetAt(0)
      val it = sheet.iterator().asScala

      val row = it.find(r => {
        val cell = r.getCell(0)
        cell.getStringCellValue.strip() == fciName
      })

      val price = row.get.getCell(5).getNumericCellValue
      val currency = row.get.getCell(1).getStringCellValue

      val enumValue = Currency.valueOf(currency)

      (price, enumValue)
    }

    data match
      case Success((v, currency)) => Some(
        TickerPriceInfo(
          v,
          1000,
          currency
        )
      )
      case Failure(e: Throwable) =>
        logger.error("Failed to read from spreadsheet")
        logger.error(e.toString)
        None

  private def readValue(fciName: String): Option[TickerPriceInfo] =
    retrieveFile() match
      case Success(f) =>
        readFromSpreadsheet(fciName, f)
      case Failure(e: Throwable) =>
        logger.error("Failed to retrieve file")
        logger.error(e.toString)
        None

  def getTickerPriceInfo(market: Market, ticker: String, assetType: AssetType): Option[TickerPriceInfo] =
    createDirectory() match
      case Success(_) =>
        logger.info("Directory created")
        readValue(ticker)
      case Failure(e: FileAlreadyExistsException) =>
        logger.info("Directory already exists. Skipping")
        readValue(ticker)
      case Failure(e: Throwable) =>
        logger.info("Failed to create directory")
        logger.info(e.toString)
        None

object FciStrategy:
  private val BASE_PATH = "./fci_files"