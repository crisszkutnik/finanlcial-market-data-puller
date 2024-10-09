package com.crisszkutnik.financialmarketdatapuller.http

import cats.Applicative
import com.crisszkutnik.financialmarketdatapuller.priceFetcher.{AssetPriceService, AssetType, Market, PriceResponse}
import scala.util.Try

trait TickerController[F[_]]:
  def getBasicTickerValue(market: Market, ticker: String, assetType: AssetType): Try[PriceResponse]

object TickerController:
  def impl[F[_]: Applicative]: TickerController[F] = new TickerController[F]:
    def getBasicTickerValue(market: Market, ticker: String, assetType: AssetType): Try[PriceResponse] =
      AssetPriceService().getValue(market, ticker, assetType)