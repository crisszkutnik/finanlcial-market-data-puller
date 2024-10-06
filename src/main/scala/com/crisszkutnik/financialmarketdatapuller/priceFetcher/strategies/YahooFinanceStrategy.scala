package com.crisszkutnik.financialmarketdatapuller.priceFetcher.strategies

import com.crisszkutnik.financialmarketdatapuller.priceFetcher.{AssetType, Currency, Market, Source, TickerPriceInfo}
import com.typesafe.scalalogging.Logger
import sttp.client4.quick.*
import sttp.client4.UriContext

import scala.util.{Failure, Success, Try}

class YahooFinanceStrategy(
  private val logger: Logger = Logger[YahooFinanceStrategy]
) extends PriceFetcher:
  val source: Source = Source.YAHOOFINANCE

  def canHandle(market: Market, ticker: String, assetType: AssetType): Boolean =
    assetType == AssetType.STOCK

  def getTickerPriceInfo(market: Market, ticker: String, assetType: AssetType): Option[TickerPriceInfo] =
    val actualTicker = transformTicker(market, ticker)
    val data = retrieveData(actualTicker)

    data match
      case Success((price, currency: Currency)) => Some(
        TickerPriceInfo(
          price,
          1,
          currency
        )
      )
      case Failure(e: Throwable) =>
        logger.error(s"Failed to retrieve data from Yahoo finance for combination (market, ticker, assetType) = (${market}, ${ticker}, ${assetType})")
        logger.error(e.toString)
        None

  private def retrieveData(ticker: String): Try[(Double, Currency)] =
    Try {
      val response = quickRequest
        .get(uri"https://query1.finance.yahoo.com/v8/finance/chart/${ticker}")
        .send()

      val json = ujson.read(response.body)

      // TODO: Maybe try to use Circe for this?
      // chart.result[0].meta.regularMarketPrice
      val value = (((json("chart").obj)("result").arr.head)("meta").obj)("regularMarketPrice").num

      // chart.result[0].meta.currency
      val currency = (((json("chart").obj)("result").arr.head)("meta").obj)("currency").str
      val enumVal = Currency.valueOf(currency)

      (value, enumVal)
    }


  private def transformTicker(market: Market, ticker: String) =
    market match
      case Market.BCBA => s"${ticker}.BA"
      case _ => ticker