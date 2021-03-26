/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.Passport

import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.AnswerConnector
import uk.gov.hmrc.questionrepository.models.{AnswerDetails, PassportQuestion, QuestionKey, QuestionResult, ServiceName, passportService}
import uk.gov.hmrc.questionrepository.services.AnswerService
import uk.gov.hmrc.questionrepository.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PassportAnswerService @Inject()(passportAnswerConnector: PassportAnswerConnector)(implicit override val appConfig: AppConfig, ec: ExecutionContext) extends AnswerService
  with CheckAvailability
  with CircuitBreakerConfiguration {

  override type Record = QuestionResult

  override def serviceName: ServiceName = passportService

  override def connector: AnswerConnector[QuestionResult] = passportAnswerConnector

  override def supportedQuestions: Seq[QuestionKey] = Seq(PassportQuestion)

  override def answerTransformer(records: Seq[QuestionResult], filteredAnswers: Seq[AnswerDetails]): Seq[QuestionResult] = records

}
