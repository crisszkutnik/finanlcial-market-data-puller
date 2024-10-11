package com.crisszkutnik.financialmarketdatapuller

import cats.effect.{IO, IOApp}
import com.crisszkutnik.financialmarketdatapuller.http.Server
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics

object Main extends IOApp.Simple:
  JvmMetrics.builder().register()
  val run: IO[Nothing] = Server.run[IO]
