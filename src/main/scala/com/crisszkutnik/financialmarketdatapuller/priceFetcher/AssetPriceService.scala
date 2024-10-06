package com.crisszkutnik.financialmarketdatapuller.priceFetcher

import com.crisszkutnik.financialmarketdatapuller.priceFetcher.strategies.{FciStrategy, IolStrategy, PriceFetcher, YahooFinanceStrategy}
import com.typesafe.scalalogging.Logger

class AssetPriceService(
  private val fetchers: List[PriceFetcher] = List(
    YahooFinanceStrategy(),
    IolStrategy(),
    FciStrategy()
  ),
  private val logger: Logger = Logger[AssetPriceService]
):
  def getValue(market: Market, ticker: String, assetType: AssetType): Option[PriceResponse] =
    val validFetchers = fetchers.filter(_.canHandle(market, ticker, assetType))
    val foldFn = (accumulator: Option[(TickerPriceInfo, Source)], fetcher: PriceFetcher) => {
      accumulator match
        case Some(i) => Some(i)
        case None => evalFetcher(fetcher, market, ticker, assetType)
    }

    val info = validFetchers.foldLeft(Option.empty[(TickerPriceInfo, Source)])(foldFn)

    info match
      case Some((tickerInfo, source)) =>
        Some(
          PriceResponse(
            tickerInfo.value,
            ticker,
            market,
            assetType,
            tickerInfo.unitsForTickerPrice,
            tickerInfo.currency,
            source
          )
        )
      case None =>
        logger.error(s"Failed to find handler for combination (market, ticker, assetType) = ($market, $ticker, $assetType)")
        None

  private def evalFetcher(fetcher: PriceFetcher, market: Market, ticker: String, assetType: AssetType) =
    fetcher.getTickerPriceInfo(market, ticker, assetType) match
      case Some(v) => Some(v, fetcher.source)
      case None => None