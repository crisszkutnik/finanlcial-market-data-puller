package com.crisszkutnik.financialmarketdatapuller.http

import io.circe.*
import org.http4s.*
import io.circe.syntax.*
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityEncoder.*
import cats.effect.kernel.Sync
import com.crisszkutnik.financialmarketdatapuller.priceFetcher.PriceResponse
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object TickerRoutes:
  def tickerRoutes[F[_]: Sync](controller: TickerController[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F]{}
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root / "ticker" / "basic" / market / ticker / assetType =>
        val tickerInfo = controller.getBasicTickerValue(market, ticker, assetType)

        tickerInfo match
          case Some(value) => Ok(value.asJson)
          case None => BadRequest()
    }
