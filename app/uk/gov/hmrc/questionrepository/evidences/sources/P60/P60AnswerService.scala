/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.P60

import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.AnswerConnector
import uk.gov.hmrc.questionrepository.models.P60._
import uk.gov.hmrc.questionrepository.models.{AnswerDetails, QuestionKey, QuestionResult, ServiceName, p60Service}
import uk.gov.hmrc.questionrepository.services.AnswerService
import uk.gov.hmrc.questionrepository.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class P60AnswerService @Inject()(p60AnswerConnector: P60AnswerConnector)(implicit override val appConfig: AppConfig, ec: ExecutionContext) extends AnswerService
  with CheckAvailability
  with CircuitBreakerConfiguration {

  override type Record = QuestionResult

  override def serviceName: ServiceName = p60Service

  override def connector: AnswerConnector[QuestionResult] = p60AnswerConnector

  override def supportedQuestions: Seq[QuestionKey] =
    Seq(PaymentToDate, EmployeeNIContributions, EarningsAbovePT, StatutoryMaternityPay, StatutorySharedParentalPay, StatutoryAdoptionPay, StudentLoanDeductions, PostgraduateLoanDeductions)

  override def answerTransformer(records: Seq[QuestionResult], filteredAnswers: Seq[AnswerDetails]): Seq[QuestionResult] = records

}
