package com.crisszkutnik.financialmarketdatapuller

import cats.effect.{IO, IOApp}
import com.crisszkutnik.financialmarketdatapuller.http.Server
import io.prometheus.metrics.exporter.httpserver.HTTPServer
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics

object Main extends IOApp.Simple:
  JvmMetrics.builder().register()
  HTTPServer.builder().port(9400).buildAndStart()

  val run: IO[Nothing] = Server.run[IO]