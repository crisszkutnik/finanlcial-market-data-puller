package com.crisszkutnik.financialmarketdatapuller.priceFetcher

import io.circe.{Encoder, Json}

enum Market:
  case BCBA,
  NYSE,
  NASDAQ,
  NYSEARCA

object Market:
  given Encoder[Market] = new Encoder[Market]:
    def apply(a: Market): Json = Json.fromString(a.toString)