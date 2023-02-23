/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.identityverificationquestions.connectors.utilities

import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import uk.gov.hmrc.identityverificationquestions.config.{AppConfig, MissingAllConfig, MissingAuthorizationToken, MissingEnvironmentHeader}
import uk.gov.hmrc.identityverificationquestions.models.ServiceName


trait HodConnectorConfig {

  implicit val appConfig: AppConfig
  def serviceName: ServiceName

  protected def headersForDES(implicit hc: HeaderCarrier): HeaderCarrier =
    appConfig.hodConfiguration(serviceName) match {
      case Right(hodConf) =>
        hc.copy(
          authorization = Some(Authorization(s"Bearer ${hodConf.authorizationToken}")),
          extraHeaders = Seq(
            "Originator-Id" -> appConfig.originatorId,
            "Authorization" -> s"Bearer ${hodConf.authorizationToken}",
            "Environment" -> hodConf.environmentHeader
          )
        )
      case Left(MissingAuthorizationToken) =>
        throw new RuntimeException(s"Could not find hod authorizationToken configuration for $serviceName")
      case Left(MissingEnvironmentHeader) =>
        throw new RuntimeException(s"Could not find hod environmentHeader configuration for $serviceName")
      case Left(MissingAllConfig) =>
        throw new RuntimeException(s"Could not find any hod configuration for $serviceName")
    }
}
