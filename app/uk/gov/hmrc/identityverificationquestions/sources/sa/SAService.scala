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

package uk.gov.hmrc.identityverificationquestions.sources.sa

import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.EventDispatcher
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.monitoring.metric.MetricsService
import uk.gov.hmrc.identityverificationquestions.services.QuestionService
import uk.gov.hmrc.identityverificationquestions.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SAService @Inject() (
    val appConfig: AppConfig,
    val saPensionService: SAPensionService,
    val saPaymentService: SAPaymentService,
    val eventDispatcher: EventDispatcher,
    val auditService: AuditService,
    val metricsService: MetricsService) extends QuestionService
    with CheckAvailability
    with CircuitBreakerConfiguration {

  val serviceName: ServiceName = selfAssessmentService

  override def questions(selection: Selection, corrId: CorrelationId)
                        (implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[QuestionWithAnswers]] = {

    val paymentQuestionsFuture = saPaymentService.questions(selection, corrId)
    val pensionQuestionsFuture = saPensionService.questions(selection, corrId)

    for {
      paymentQuestion <- paymentQuestionsFuture
      pensionQuestion <- pensionQuestionsFuture
    } yield if (paymentQuestion.nonEmpty) paymentQuestion else pensionQuestion
  }

  override type Record = SelfAssessmentReturn

  override def connector: connectors.QuestionConnector[SelfAssessmentReturn] = ???

  override def evidenceTransformer(records: Seq[SelfAssessmentReturn], corrId: CorrelationId): Seq[QuestionWithAnswers] = ???

  override def allowedUserAgentList: Seq[String] = appConfig.allowedUserAgentListForSA
}
