package com.crisszkutnik.financialmarketdatapuller.http

import cats.Applicative
import com.crisszkutnik.financialmarketdatapuller.priceFetcher.{AssetPriceService, AssetType, Market, PriceResponse}

import scala.util.{Success, Try}

trait TickerController[F[_]]:
  def getBasicTickerValue(market: String, ticker: String, assetType: String): Option[PriceResponse]

object TickerController:
  def impl[F[_]: Applicative]: TickerController[F] = new TickerController[F]:
    def getBasicTickerValue(market: String, ticker: String, assetType: String): Option[PriceResponse] =
      val parsedMarket = Try(Market.valueOf(market));
      val parsedAssetType = Try(AssetType.valueOf(assetType))

      (parsedMarket, parsedAssetType) match
        case (Success(m), Success(at)) =>
          AssetPriceService().getValue(m, ticker, at)
        case _ => None