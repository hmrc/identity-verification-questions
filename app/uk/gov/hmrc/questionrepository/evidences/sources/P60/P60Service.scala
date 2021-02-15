/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.P60

import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.Payment.Payment
import uk.gov.hmrc.questionrepository.models.Question
import uk.gov.hmrc.questionrepository.services.QuestionService
import uk.gov.hmrc.questionrepository.services.utilities.{CheckAvailability, CircuitBreakerConfiguration, PenceAnswerConvertor, TaxYearBuilder}

import javax.inject.Inject
import scala.collection.SortedSet

class P60Service @Inject()(connector: P60Connector)(implicit override val appConfig: AppConfig) extends QuestionService
  with CheckAvailability
  with CircuitBreakerConfiguration
  with TaxYearBuilder
  with PenceAnswerConvertor {

  override type Record = Payment

  override def serviceName: String = "p60Service"

  override def connector: QuestionConnector[Payment] = connector

  override def evidenceTransformer(records: Seq[Payment]): Seq[Question] = {

    def taxYears = SortedSet(currentTaxYear.previous, currentTaxYearWithBuffer.previous)
    def additionalInfoMap = Map("currentTaxYear" -> taxYears.last.display) ++
      (if (taxYears.size > 1) Map("previousTaxYear" -> taxYears.head.display) else Map())

    val PaymentToDateAnswers: Seq[Question] = records.flatMap(_.taxablePayYTD).filter(_ > 0).map(convertAnswer) match {
      case answers if answers.nonEmpty => Seq(Question("P60-PaymentToDate", answers.map(_.toString), additionalInfoMap))
      case _ => Nil
    }
    val EmployeeNIContributionsAnswers: Seq[Question] = records.flatMap(_.employeeNIContrib).filter(_ > 0).map(convertAnswer) match {
      case answers if answers.nonEmpty => Seq(Question("P60-EmployeeNIContributions", answers.map(_.toString), additionalInfoMap))
      case _ => Nil
    }
    PaymentToDateAnswers ++ EmployeeNIContributionsAnswers
  }
}
