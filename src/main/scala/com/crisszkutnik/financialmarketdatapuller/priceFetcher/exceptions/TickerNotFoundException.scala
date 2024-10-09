package com.crisszkutnik.financialmarketdatapuller.priceFetcher.exceptions

import com.crisszkutnik.financialmarketdatapuller.priceFetcher.{Market, Source}

class TickerNotFoundException(message: String) extends Exception(message: String):
  def this(strategy: Source, market: String, ticker: String) =
    this(s"Strategy ${strategy.toString} unable to find ticker ${market}:${ticker}")

  def this(strategy: Source, market: Market, ticker: String) =
    this(strategy, market.toString, ticker)

  def this(strategy: Source, ticker: String) =
    this(s"Strategy ${strategy.toString} unable to find ticker ${ticker}")

  def this(market: Market, ticker: String) =
    this(s"Failed to found ticker $market:$ticker")