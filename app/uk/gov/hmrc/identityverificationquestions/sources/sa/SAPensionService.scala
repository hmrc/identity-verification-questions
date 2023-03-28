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

import org.joda.time.DateTime
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.models.SelfAssessment.SelfAssessedIncomeFromPensionsQuestion
import uk.gov.hmrc.identityverificationquestions.models.{CorrelationId, QuestionWithAnswers, ServiceName, selfAssessmentService}
import uk.gov.hmrc.identityverificationquestions.monitoring.EventDispatcher
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}
import uk.gov.hmrc.identityverificationquestions.sources.QuestionServiceMeoMinimumNumberOfQuestions

import javax.inject.Inject

class SAPensionService @Inject() (
    val appConfig: AppConfig,
    connector : SAPensionsConnector,
    val eventDispatcher: EventDispatcher,
    val auditService: AuditService) extends QuestionServiceMeoMinimumNumberOfQuestions
  with CheckAvailability
  with CircuitBreakerConfiguration {

  override def connector: QuestionConnector[SAReturn] = connector

  type Record = SAReturn

  def currentDate: DateTime = DateTime.now()

  private val currentYearKey = "currentTaxYear"
  private val previousYearKey = "previousTaxYear"

  override def serviceName: ServiceName = selfAssessmentService

  override def evidenceTransformer(records: Seq[SAReturn], corrId: CorrelationId): Seq[QuestionWithAnswers] =
    records.flatMap(correctAnswers(_)) match {
      case Nil => Nil
      case answers => Seq(QuestionWithAnswers(SelfAssessedIncomeFromPensionsQuestion, answers, returnsToAdditionalInfo(records)))
    }


  private def returnsToAdditionalInfo(returns: Seq[SAReturn]): Map[String, String] = {
    val yearToRecords: Map[Int, Seq[SARecord]] = returns.map(sar => sar.taxYear.startYear -> sar.returns).toMap
    val yearsWithSomeNotZero: Set[String] = yearToRecords.collect { case (year, records) if records.exists(_.incomeFromPensions > 0) => year.toString }.toSet
    additionalInfo.filter { case (_, year) => yearsWithSomeNotZero(year) }
  }

   protected def additionalInfo: Map[String, String] = {
    val (previousYear, currentYear) = connector.determinePeriod
    Map(
      currentYearKey -> currentYear.toString,
      previousYearKey -> previousYear.toString
    )
  }

  protected def correctAnswers(record: SelfAssessmentReturn): Seq[String] = {
    record match {
      case pension: SAReturn =>
        pension.returns.collect {
          case value if value.incomeFromPensions > 0 => value.incomeFromPensions.toString()
        }
      case _ => Seq()
    }
  }
}
