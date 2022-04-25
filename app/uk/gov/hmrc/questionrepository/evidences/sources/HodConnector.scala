/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources

import akka.http.javadsl.model.headers.UserAgent
import uk.gov.hmrc.circuitbreaker.CircuitBreakerConfig
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.questionrepository.config.AppConfig

import scala.concurrent.{ExecutionContext, Future}

trait QuestionConnector[T] {
  def getRecords(nino: Nino)(implicit hc: HeaderCarrier, ec: ExecutionContext, userAgent: Option[UserAgent]): Future[Seq[T]]
}

trait HodConnector[T] extends QuestionConnector[T] {

  def servicesConfig: ServicesConfig

  protected[sources] def authorizationToken: String
  protected[sources] def environmentHeader: String

  protected def headersForDes(implicit hc: HeaderCarrier): HeaderCarrier =
    hc.copy(authorization = Some(Authorization(s"Bearer $authorizationToken")), extraHeaders = Seq("Environment" -> environmentHeader))

  protected def getHodConf(hod: String, param: String): String =
    servicesConfig.getConfString(s"$hod.$param", throw new RuntimeException(s"Could not find configuration for $hod.$param"))
}

//trait HodCircuitBreakerConfig {
//
//  def appConfig: AppConfig
//
//  def serviceName: String = this.getClass.getSimpleName
//
//  lazy val circuitBreakerConfig: CircuitBreakerConfig = {
//    CircuitBreakerConfig(
//      serviceName,
//      appConfig.hodsCircuitBreakerNumberOfCallsToTrigger,
//      appConfig.hodsCircuitBreakerUnavailableDurationInSec * 1000,
//      appConfig.hodsCircuitBreakerUnstableDurationInSec * 1000
//    )
//  }
//}
