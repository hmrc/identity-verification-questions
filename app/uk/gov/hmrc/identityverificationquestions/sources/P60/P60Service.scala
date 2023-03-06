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

package uk.gov.hmrc.identityverificationquestions.sources.P60

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.models.P60._
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.models.payment.Payment
import uk.gov.hmrc.identityverificationquestions.monitoring.EventDispatcher
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.services.utilities.{CheckAvailability, CircuitBreakerConfiguration, PenceAnswerConvertor, TaxYearBuilder}
import uk.gov.hmrc.identityverificationquestions.sources.QuestionServiceMeoMinimumNumberOfQuestions

import scala.collection.SortedSet

@Singleton
class P60Service @Inject()(p60Connector: P60Connector, val eventDispatcher: EventDispatcher, val auditService: AuditService)(implicit override val appConfig: AppConfig) extends QuestionServiceMeoMinimumNumberOfQuestions
  with CheckAvailability
  with CircuitBreakerConfiguration
  with TaxYearBuilder
  with PenceAnswerConvertor {

  override type Record = Payment

  override def serviceName: ServiceName = p60Service

  override def connector: QuestionConnector[Payment] = p60Connector

  override def evidenceTransformer(records: Seq[Payment], corrId: CorrelationId): Seq[QuestionWithAnswers] = {

    def taxYears = SortedSet(currentTaxYear.previous, currentTaxYearWithBuffer.previous)
    def additionalInfoMap = Map("currentTaxYear" -> taxYears.last.display) ++
      (if (taxYears.size > 1) Map("previousTaxYear" -> taxYears.head.display) else Map())

    val p60Questions: Seq[QuestionWithAnswers] = {
      val PaymentToDateAnswers: Seq[QuestionWithAnswers] = records.flatMap(_.taxablePayYTD).filter(_ > 0).map(roundDownWithPence) match {
        case Nil => Nil
        case answers => Seq(QuestionWithAnswers(PaymentToDate, answers.map(_.toString), additionalInfoMap))
      }
      val EmployeeNIContributionsAnswers: Seq[QuestionWithAnswers] = records.flatMap(_.employeeNIContrib).filter(_ > 0).map(roundDownWithPence) match {
        case Nil => Nil
        case answers => Seq(QuestionWithAnswers(EmployeeNIContributions, answers.map(_.toString), additionalInfoMap))
      }
      PaymentToDateAnswers ++ EmployeeNIContributionsAnswers
    }

    lazy val p60NewQuestions: Seq[QuestionWithAnswers] = {
      val EarningsAbovePTAnswers: Seq[QuestionWithAnswers] = records.flatMap(_.earningsAbovePT).filter(_ > 0).map(roundDownIgnorePence) match {
        case Nil => Nil
        case answers => Seq(QuestionWithAnswers(EarningsAbovePT, answers.map(_.toString), additionalInfoMap))
      }
      val StatutoryMaternityPayAnswers: Seq[QuestionWithAnswers] = records.flatMap(_.statutoryMaternityPay).filter(_ > 0).map(roundDownWithPence) match {
        case Nil => Nil
        case answers => Seq(QuestionWithAnswers(StatutoryMaternityPay, answers.map(_.toString), additionalInfoMap))
      }
      val StatutorySharedParentalPayAnswers: Seq[QuestionWithAnswers] = records.flatMap(_.statutorySharedParentalPay).filter(_ > 0).map(roundDownWithPence) match {
        case Nil => Nil
        case answers => Seq(QuestionWithAnswers(StatutorySharedParentalPay, answers.map(_.toString), additionalInfoMap))
      }
      val StatutoryAdoptionPayAnswers: Seq[QuestionWithAnswers] = records.flatMap(_.statutoryAdoptionPay).filter(_ > 0).map(roundDownWithPence) match {
        case Nil => Nil
        case answers => Seq(QuestionWithAnswers(StatutoryAdoptionPay, answers.map(_.toString), additionalInfoMap))
      }
      val StudentLoanDeductionsAnswers: Seq[QuestionWithAnswers] = records.flatMap(_.studentLoanDeductions).filter(_ > 0).map(roundDownIgnorePence) match {
        case Nil => Nil
        case answers => Seq(QuestionWithAnswers(StudentLoanDeductions, answers.map(_.toString), additionalInfoMap))
      }
      val PostgraduateLoanDeductionsAnswers: Seq[QuestionWithAnswers] = records.flatMap(_.postgraduateLoanDeductions).filter(_ > 0).map(roundDownIgnorePence) match {
        case Nil => Nil
        case answers => Seq(QuestionWithAnswers(PostgraduateLoanDeductions, answers.map(_.toString), additionalInfoMap))
      }
      EarningsAbovePTAnswers ++ StatutoryMaternityPayAnswers ++ StatutorySharedParentalPayAnswers ++ StatutoryAdoptionPayAnswers ++ StudentLoanDeductionsAnswers ++ PostgraduateLoanDeductionsAnswers
    }

    if (appConfig.p60NewQuestionEnabled) p60Questions ++ p60NewQuestions
    else p60Questions
  }
}
