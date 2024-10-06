package com.crisszkutnik.financialmarketdatapuller.priceFetcher

import io.circe.{Encoder, Json}

enum Currency:
  case ARS,
  USD

object Currency:
  given Encoder[Currency] = new Encoder[Currency]:
    def apply(a: Currency): Json = Json.fromString(a.toString)