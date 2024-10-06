package com.crisszkutnik.financialmarketdatapuller.priceFetcher

import io.circe.{Encoder, Json}

enum AssetType:
  case STOCK,
  BOND,
  MUTUAL_FUND

object AssetType:
  given Encoder[AssetType] = new Encoder[AssetType]:
    def apply(a: AssetType): Json = Json.fromString(a.toString)