/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.P60

import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.payment.Payment
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.services.QuestionService
import uk.gov.hmrc.questionrepository.services.utilities.{CheckAvailability, CircuitBreakerConfiguration, PenceAnswerConvertor, TaxYearBuilder}

import javax.inject.{Inject, Singleton}
import scala.collection.SortedSet
import scala.concurrent.ExecutionContext

@Singleton
class P60Service @Inject()(p60Connector: P60Connector)(implicit override val appConfig: AppConfig, ec: ExecutionContext) extends QuestionService
  with CheckAvailability
  with CircuitBreakerConfiguration
  with TaxYearBuilder
  with PenceAnswerConvertor {

  override type Record = Payment

  override def serviceName: ServiceName = p60Service

  override def connector: QuestionConnector[Payment] = p60Connector

  override def evidenceTransformer(records: Seq[Payment]): Seq[Question] = {

    def taxYears = SortedSet(currentTaxYear.previous, currentTaxYearWithBuffer.previous)
    def additionalInfoMap = Map("currentTaxYear" -> taxYears.last.display) ++
      (if (taxYears.size > 1) Map("previousTaxYear" -> taxYears.head.display) else Map())

    val PaymentToDateAnswers: Seq[Question] = records.flatMap(_.taxablePayYTD).filter(_ > 0).map(convertAnswer) match {
      case Nil => Nil
      case answers => Seq(Question(PaymentToDate, answers.map(_.toString), additionalInfoMap))
    }
    val EmployeeNIContributionsAnswers: Seq[Question] = records.flatMap(_.employeeNIContrib).filter(_ > 0).map(convertAnswer) match {
      case Nil => Nil
      case answers => Seq(Question(EmployeeNIContributions, answers.map(_.toString), additionalInfoMap))
    }
    PaymentToDateAnswers ++ EmployeeNIContributionsAnswers
  }
}
