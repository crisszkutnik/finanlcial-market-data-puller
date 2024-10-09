package com.crisszkutnik.financialmarketdatapuller.http

import io.circe.*
import org.http4s.*
import io.circe.syntax.*
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityEncoder.*
import cats.effect.kernel.Sync
import com.crisszkutnik.financialmarketdatapuller.priceFetcher.exceptions.TickerNotFoundException
import com.crisszkutnik.financialmarketdatapuller.priceFetcher.{AssetType, Market, PriceResponse}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

import scala.util.{Failure, Success, Try}

object TickerRoutes:
  def tickerRoutes[F[_]: Sync](controller: TickerController[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F]{}
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root / "ticker" / "basic" / market / ticker / assetType =>
        val parsedMarket = Try(Market.valueOf(market));
        val parsedAssetType = Try(AssetType.valueOf(assetType))

        (parsedMarket, parsedAssetType) match
          case (Success(m), Success(at)) =>
            controller.getBasicTickerValue(m, ticker, at) match
              case Success(res) => Ok(res.asJson)
              case Failure(e: TickerNotFoundException) => BadRequest()
              case Failure(e: Throwable) => InternalServerError()
          case _ => BadRequest()
    }
