package com.crisszkutnik.financialmarketdatapuller.priceFetcher.strategies

import com.crisszkutnik.financialmarketdatapuller.priceFetcher.{AssetType, Currency, Market, Source, TickerPriceInfo}
import com.typesafe.scalalogging.Logger
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import sttp.client4.quick.*
import sttp.client4.Response
import sttp.model.Uri

class IolStrategy(
 private val logger: Logger = Logger[IolStrategy]
) extends PriceFetcher:
  val source: Source = Source.IOL

  def canHandle(market: Market, ticker: String, assetType: AssetType): Boolean =
    assetType match
      case AssetType.STOCK | AssetType.BOND => true
      case _ => false

  def getTickerPriceInfo(market: Market, ticker: String, assetType: AssetType): Option[TickerPriceInfo] =
    try {
      val marketVal = transformMarket(market)
      val url: Uri = uri"https://iol.invertironline.com/titulo/cotizacion/$marketVal/$ticker"

      val response: Response[String] = quickRequest.get(url).send()
      val doc = Jsoup.parse(response.body)

      val unitsForTicker = getUnitsForGivenPrice(assetType)

      Some(
        TickerPriceInfo(
          getPrice(doc),
          unitsForTicker,
          getCurrency(doc)
        )
      )
    } catch {
      case e: Throwable =>
        logger.error(s"FATAL ERROR. Failed to fetch price for combination (market, ticker, assetType) = ($market, $ticker, $assetType)")
        logger.error(e.toString)
        None
    }

  private def getPrice(doc: Document): Double =
    doc
      .selectFirst("#IdTitulo [data-field=\"UltimoPrecio\"]")
      .ownText()
      .replace(".", "")
      .replace(",", ".")
      .toDouble
  
  private def getCurrency(doc: Document): Currency =
    doc.selectFirst("#IdTitulo span").ownText() match
      case "US$" => Currency.USD
      case _ => Currency.ARS

  private def getUnitsForGivenPrice(assetType: AssetType) =
    assetType match
      case AssetType.BOND => 100
      case _ => 1

  private def transformMarket(market: Market): String =
    market match
      case Market.NYSEARCA => Market.NYSE.toString
      case other => other.toString