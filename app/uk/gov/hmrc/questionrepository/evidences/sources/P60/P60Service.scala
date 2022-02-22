/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.P60

import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.P60._
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

    val p60Questions: Seq[Question] = {
      val PaymentToDateAnswers: Seq[Question] = records.flatMap(_.taxablePayYTD).filter(_ > 0).map(roundDownIgnorePence) match {
        case Nil => Nil
        case answers => Seq(Question(PaymentToDate, answers.map(_.toString), additionalInfoMap))
      }
      val EmployeeNIContributionsAnswers: Seq[Question] = records.flatMap(_.employeeNIContrib).filter(_ > 0).map(roundDownIgnorePence) match {
        case Nil => Nil
        case answers => Seq(Question(EmployeeNIContributions, answers.map(_.toString), additionalInfoMap))
      }
      PaymentToDateAnswers ++ EmployeeNIContributionsAnswers
    }

    lazy val p60NewQuestions: Seq[Question] = {
      val EarningsAbovePTAnswers: Seq[Question] = records.flatMap(_.earningsAbovePT).filter(_ > 0).map(roundDownIgnorePence) match {
        case Nil => Nil
        case answers => Seq(Question(EarningsAbovePT, answers.map(_.toString), additionalInfoMap))
      }
      val StatutoryMaternityPayAnswers: Seq[Question] = records.flatMap(_.statutoryMaternityPay).filter(_ > 0).map(roundDownIgnorePence) match {
        case Nil => Nil
        case answers => Seq(Question(StatutoryMaternityPay, answers.map(_.toString), additionalInfoMap))
      }
      val StatutorySharedParentalPayAnswers: Seq[Question] = records.flatMap(_.statutorySharedParentalPay).filter(_ > 0).map(roundDownIgnorePence) match {
        case Nil => Nil
        case answers => Seq(Question(StatutorySharedParentalPay, answers.map(_.toString), additionalInfoMap))
      }
      val StatutoryAdoptionPayAnswers: Seq[Question] = records.flatMap(_.statutoryAdoptionPay).filter(_ > 0).map(roundDownIgnorePence) match {
        case Nil => Nil
        case answers => Seq(Question(StatutoryAdoptionPay, answers.map(_.toString), additionalInfoMap))
      }
      val StudentLoanDeductionsAnswers: Seq[Question] = records.flatMap(_.studentLoanDeductions).filter(_ > 0).map(roundDownIgnorePence) match {
        case Nil => Nil
        case answers => Seq(Question(StudentLoanDeductions, answers.map(_.toString), additionalInfoMap))
      }
      val PostgraduateLoanDeductionsAnswers: Seq[Question] = records.flatMap(_.postgraduateLoanDeductions).filter(_ > 0).map(roundDownIgnorePence) match {
        case Nil => Nil
        case answers => Seq(Question(PostgraduateLoanDeductions, answers.map(_.toString), additionalInfoMap))
      }
      EarningsAbovePTAnswers ++ StatutoryMaternityPayAnswers ++ StatutorySharedParentalPayAnswers ++ StatutoryAdoptionPayAnswers ++ StudentLoanDeductionsAnswers ++ PostgraduateLoanDeductionsAnswers
    }

    if (appConfig.p60NewQuestionEnabled) p60Questions ++ p60NewQuestions
    else p60Questions
  }
}
