/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.AnswerConnector
import uk.gov.hmrc.questionrepository.models.SelfAssessment.{SelfAssessedIncomeFromPensionsQuestion, SelfAssessedPaymentQuestion}
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.services.AnswerService
import uk.gov.hmrc.questionrepository.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SAAnswerService @Inject()(saAnswerConnector: SAAnswerConnector)(implicit override val appConfig: AppConfig, ec: ExecutionContext) extends AnswerService
  with CheckAvailability
  with CircuitBreakerConfiguration {

  override type Record = QuestionResult

  override def serviceName: ServiceName = selfAssessmentService

  override def connector: AnswerConnector[QuestionResult] = saAnswerConnector

  override def supportedQuestions: Seq[QuestionKey] =
    Seq(SelfAssessedIncomeFromPensionsQuestion, SelfAssessedPaymentQuestion)

  override def answerTransformer(records: Seq[QuestionResult], filteredAnswers: Seq[AnswerDetails]): Seq[QuestionResult] = records

}
