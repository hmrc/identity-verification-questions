/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.connectors.utilities

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.questionrepository.config.{AppConfig, MissingAllConfig, MissingAuthorizationToken, MissingEnvironmentHeader}

trait HodConnectorConfig {

  implicit val appConfig: AppConfig
  def serviceName: String

  protected def headersForDES(implicit hc: HeaderCarrier): HeaderCarrier =
    appConfig.hodConfiguration(serviceName) match {
      case Right(hodConf) =>
        hc.copy(authorization = Some(Authorization(s"Bearer ${hodConf.authorizationToken}")), extraHeaders = Seq("Environment" -> hodConf.environmentHeader))
      case Left(MissingAuthorizationToken) =>
        throw new RuntimeException(s"Could not find hod authorizationToken configuration for $serviceName")
      case Left(MissingEnvironmentHeader) =>
        throw new RuntimeException(s"Could not find hod environmentHeader configuration for $serviceName")
      case Left(MissingAllConfig) =>
        throw new RuntimeException(s"Could not find any hod configuration for $serviceName")
    }
}
