/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.SCPEmail

import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.AnswerConnector
import uk.gov.hmrc.questionrepository.models.{AnswerDetails, QuestionKey, QuestionResult, SCPEmailQuestion, ServiceName, scpEmailService}
import uk.gov.hmrc.questionrepository.services.AnswerService
import uk.gov.hmrc.questionrepository.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SCPEmailAnswerService @Inject()(scpEmailAnswerConnector: SCPEmailAnswerConnector) (implicit override val appConfig: AppConfig, ec: ExecutionContext) extends AnswerService
  with CheckAvailability
  with CircuitBreakerConfiguration {

  override type Record = QuestionResult

  override def serviceName: ServiceName = scpEmailService

  override def connector: AnswerConnector[QuestionResult] = scpEmailAnswerConnector

  override def supportedQuestions: Seq[QuestionKey] = Seq(SCPEmailQuestion)

  override def answerTransformer(records: Seq[QuestionResult], filteredAnswers: Seq[AnswerDetails]): Seq[QuestionResult] = records

}
