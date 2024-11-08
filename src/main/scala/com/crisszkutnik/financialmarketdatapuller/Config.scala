package com.crisszkutnik.financialmarketdatapuller

import com.typesafe.scalalogging.Logger

import scala.util.Try

/*
 Fast and easy implementation
*/

object Config {
  private val logger = Logger[Config.type]

  /*
    We call this function in Main.scala so env variables are loaded as soon as the app starts but
    it does nothing really
  */
  def init(): Unit = ()

  logger.info("Loading environment variables")

  val SQLITE_CACHE_EXPIRE_TIME_MS: Long = loadLongEnvVar("SQLITE_CACHE_EXPIRE_TIME_MS", 30000)

  private def loadLongEnvVar(varName: String, defaultVal: Long) =
    val value = Try {
      System.getenv(varName).toLong
    }

    if value.isSuccess then
      value.get
    else
      logger.warn(s"Failed to load environment variable $varName. Using default value $defaultVal")
      defaultVal
}
