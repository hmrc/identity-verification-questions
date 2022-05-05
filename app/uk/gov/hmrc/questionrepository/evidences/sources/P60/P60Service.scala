/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.P60

import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.P60._
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.models.payment.Payment
import uk.gov.hmrc.questionrepository.monitoring.EventDispatcher
import uk.gov.hmrc.questionrepository.monitoring.auditing.AuditService
import uk.gov.hmrc.questionrepository.services.QuestionService
import uk.gov.hmrc.questionrepository.services.utilities.{CheckAvailability, CircuitBreakerConfiguration, PenceAnswerConvertor, TaxYearBuilder}

import javax.inject.{Inject, Singleton}
import scala.collection.SortedSet

@Singleton
class P60Service @Inject()(p60Connector: P60Connector, val eventDispatcher: EventDispatcher, val auditService: AuditService)(implicit override val appConfig: AppConfig) extends QuestionService
  with CheckAvailability
  with CircuitBreakerConfiguration
  with TaxYearBuilder
  with PenceAnswerConvertor {

  override type Record = Payment

  override def serviceName: ServiceName = p60Service

  override def connector: QuestionConnector[Payment] = p60Connector

  override def evidenceTransformer(records: Seq[Payment]): Seq[QuestionWithAnswers] = {

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
