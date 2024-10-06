package com.crisszkutnik.financialmarketdatapuller

import cats.effect.{IO, IOApp}
import com.crisszkutnik.financialmarketdatapuller.http.Server

object Main extends IOApp.Simple:
  val run: IO[Nothing] = Server.run[IO]
