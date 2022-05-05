/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

import javax.inject.Inject
import org.joda.time.DateTime
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.evidences.sources.QuestionServiceMeoMinimumNumberOfQuestions
import uk.gov.hmrc.questionrepository.models.SelfAssessment.SelfAssessedIncomeFromPensionsQuestion
import uk.gov.hmrc.questionrepository.models.{QuestionWithAnswers, ServiceName, selfAssessmentService}
import uk.gov.hmrc.questionrepository.monitoring.EventDispatcher
import uk.gov.hmrc.questionrepository.monitoring.auditing.AuditService
import uk.gov.hmrc.questionrepository.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}

class SAPensionService @Inject() (
    val appConfig: AppConfig,
    connector : SAPensionsConnector,
    val eventDispatcher: EventDispatcher,
    override implicit val auditService: AuditService) extends QuestionServiceMeoMinimumNumberOfQuestions
  with CheckAvailability
  with CircuitBreakerConfiguration {

  override def connector: QuestionConnector[SAReturn] = connector

  type Record = SAReturn

  def currentDate: DateTime = DateTime.now()

  private val currentYearKey = "currentTaxYear"
  private val previousYearKey = "previousTaxYear"

  override def serviceName: ServiceName = selfAssessmentService

  override def evidenceTransformer(records: Seq[SAReturn]): Seq[QuestionWithAnswers] =
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
