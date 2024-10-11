package com.crisszkutnik.financialmarketdatapuller.http

import io.circe.*
import org.http4s.*
import io.circe.syntax.*
import io.circe.generic.auto.*
import com.crisszkutnik.financialmarketdatapuller.priceFetcher.exceptions.TickerNotFoundException
import com.crisszkutnik.financialmarketdatapuller.priceFetcher.{AssetType, Market, PriceResponse}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats.effect.*
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.http4s.circe.*

import scala.util.{Failure, Success, Try}

case class BasicTickerBody(market: String, ticker: String, assetType: String)

object TickerRoutes:
  def tickerRoutes[F[_]: Concurrent](controller: TickerController[F]): HttpRoutes[F] =
    val dsl = new Http4sDsl[F]{}
    import dsl.*

    implicit val basicTickerBodyReader: EntityDecoder[F, BasicTickerBody] = jsonOf[F, BasicTickerBody]

    HttpRoutes.of[F] {
      case GET -> Root / "ticker" / "basic" / market / ticker / assetType =>
        getBasicTickerValue(dsl, controller, market, ticker, assetType)

      case req @ POST -> Root / "ticker" / "basic" =>
        for {
          body <- req.as[BasicTickerBody]
          resp <- getBasicTickerValue(dsl, controller, body)
        } yield resp
    }

  private def getBasicTickerValue[F[_]: Concurrent](dsl: Http4sDsl[F], controller: TickerController[F], market: String, ticker: String, assetType: String): F[Response[F]] =
    import dsl.*

    val parsedMarket = Try(Market.valueOf(market));
    val parsedAssetType = Try(AssetType.valueOf(assetType))

    (parsedMarket, parsedAssetType) match
      case (Success(m), Success(at)) =>
        controller.getBasicTickerValue(m, ticker, at) match
          case Success(res) => Ok(res.asJson)
          case Failure(e: TickerNotFoundException) => BadRequest()
          case Failure(e: Throwable) => InternalServerError()
      case _ => BadRequest()

  private def getBasicTickerValue[F[_]: Concurrent](dsl: Http4sDsl[F], controller: TickerController[F], body: BasicTickerBody): F[Response[F]] =
    getBasicTickerValue(dsl, controller, body.market, body.ticker, body.assetType)