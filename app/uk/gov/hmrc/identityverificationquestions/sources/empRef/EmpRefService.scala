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

package uk.gov.hmrc.identityverificationquestions.sources.empRef

import play.api.Logging
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.EventDispatcher
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.monitoring.metric.MetricsService
import uk.gov.hmrc.identityverificationquestions.services.utilities.{CheckAvailability, CircuitBreakerConfiguration, PenceAnswerConvertor, TaxYearBuilder}
import uk.gov.hmrc.identityverificationquestions.sources.QuestionServiceMeoMinimumNumberOfQuestions

import javax.inject.{Inject, Singleton}

@Singleton
class EmpRefService @Inject()(empRefConnector: EmpRefConnector,
                              val eventDispatcher: EventDispatcher,
                              val auditService: AuditService,
                              val appConfig: AppConfig,
                              val metricsService: MetricsService)
  extends QuestionServiceMeoMinimumNumberOfQuestions
    with CheckAvailability with CircuitBreakerConfiguration with TaxYearBuilder with PenceAnswerConvertor with Logging {

  lazy val payslipMonths: Int = appConfig.rtiNumberOfPayslipMonthsToCheck(serviceName)

  override type Record = PayePaymentsDetails

  override def serviceName: ServiceName = desPayeService

  override def deniedUserAgentList: Seq[String] = appConfig.deniedUserAgentListForPaye

  override def connector: QuestionConnector[PayePaymentsDetails] = empRefConnector

  override def evidenceTransformer(records: Seq[PayePaymentsDetails], corrId: CorrelationId): Seq[QuestionWithAnswers] = {
    logger.warn(s"$serviceName, payments details for correlationId: $corrId, $records")
    records match {
      case Nil => Nil
      case answers if answers.exists(_.payments.isEmpty) =>
        //todo remove this logger when VER-3022 has done
        logger.warn(s"$serviceName, debug paye details line 48, for correlationId: $corrId")
        Nil
      case answers if answers.exists(_.payments.get.isEmpty) =>
        //todo remove this logger when VER-3022 has done
        logger.warn(s"$serviceName, debug paye details line 53, for correlationId: $corrId")
        Nil
      case answers =>
        val dateOfPayment: Seq[QuestionWithAnswers] = {
          Seq(
            QuestionWithAnswers(
              PayeRefQuestion.DateOfPayment,
              answers.flatMap { payePaymentsDetails => payePaymentsDetails.payments.get.map(_.paymentDate) }
            )
          )
        }
        val amountOfPayment: Seq[QuestionWithAnswers] = {
          Seq(
            QuestionWithAnswers(
              PayeRefQuestion.AmountOfPayment,
              answers.flatMap { payePaymentsDetails => payePaymentsDetails.payments.get.map(_.paymentAmount.amount.toString()) }
            )
          )
        }

        dateOfPayment ++ amountOfPayment
    }
  }

}
