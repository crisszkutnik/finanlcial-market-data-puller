package com.crisszkutnik.financialmarketdatapuller.priceFetcher

import io.circe.{Encoder, Json}

enum Source:
  case YAHOOFINANCE,
  IOL,
  CAFCI

object Source:
  given Encoder[Source] = new Encoder[Source]:
    def apply(a: Source): Json = Json.fromString(a.toString)