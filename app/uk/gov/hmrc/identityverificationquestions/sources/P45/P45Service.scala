/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.identityverificationquestions.sources.P45

import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.models.P45._
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.models.payment.Payment
import uk.gov.hmrc.identityverificationquestions.monitoring.EventDispatcher
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.monitoring.metric.MetricsService
import uk.gov.hmrc.identityverificationquestions.services.utilities._
import uk.gov.hmrc.identityverificationquestions.sources.QuestionServiceMeoMinimumNumberOfQuestions

import javax.inject.{Inject, Singleton}
import scala.collection.SortedSet

@Singleton
class P45Service @Inject()(p45Connector: P45Connector,
                           val eventDispatcher: EventDispatcher,
                           val auditService: AuditService,
                           val appConfig: AppConfig,
                           val metricsService: MetricsService)
  extends QuestionServiceMeoMinimumNumberOfQuestions
    with CheckAvailability
    with CircuitBreakerConfiguration
    with TaxYearBuilder
    with PenceAnswerConvertor {

  override type Record = Payment

  override def serviceName: ServiceName = p45Service

  override def connector: QuestionConnector[Payment] = p45Connector

  override def deniedUserAgentList: Seq[String] = appConfig.deniedUserAgentListForP45

  override def evidenceTransformer(records: Seq[Payment], corrId: CorrelationId): Seq[QuestionWithAnswers] = {

    def taxYears: SortedSet[TaxYear] = SortedSet(currentTaxYear.previous, currentTaxYearWithBuffer.previous)
    def additionalInfoMap: Map[String, String] = Map("currentTaxYear" -> taxYears.last.display) ++
      (if (taxYears.size > 1) Map("previousTaxYear" -> taxYears.head.display) else Map())

    val p45Questions: Seq[QuestionWithAnswers] = {
      val PaymentToDateAnswers: Seq[QuestionWithAnswers] = records.flatMap(_.taxablePayYTD).filter(_ > 0).map(roundDownWithPence) match {
        case Nil => Nil
        case answers => Seq(QuestionWithAnswers(PaymentToDate, answers.map(_.toString), additionalInfoMap))
      }
      val TaxToDateAnswers: Seq[QuestionWithAnswers] = records.flatMap(_.totalTaxYTD).filter(_ > 0).map(roundDownWithPence) match {
        case Nil => Nil
        case answers => Seq(QuestionWithAnswers(TaxToDate, answers.map(_.toString), additionalInfoMap))
      }
      PaymentToDateAnswers ++ TaxToDateAnswers
    }

    p45Questions
  }
}
