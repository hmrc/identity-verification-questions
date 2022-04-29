/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.AnswerConnector
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60AnswerConnector
import uk.gov.hmrc.questionrepository.models.P60._
import uk.gov.hmrc.questionrepository.models.SelfAssessment.{SelfAssessedIncomeFromPensionsQuestion, SelfAssessedPaymentQuestion}
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.services.AnswerService
import uk.gov.hmrc.questionrepository.services.utilities.{CheckAvailability, CircuitBreakerConfiguration}

import scala.concurrent.ExecutionContext

@Singleton
class SAAnswerService @Inject()(p60AnswerConnector: P60AnswerConnector)(implicit override val appConfig: AppConfig, ec: ExecutionContext) extends AnswerService
  with CheckAvailability
  with CircuitBreakerConfiguration {

  override type Record = QuestionResult

  override def serviceName: ServiceName = selfAssessmentService

  override def connector: AnswerConnector[QuestionResult] = p60AnswerConnector

  override def supportedQuestions: Seq[QuestionKey] =
    Seq(SelfAssessedIncomeFromPensionsQuestion, SelfAssessedPaymentQuestion)

  override def answerTransformer(records: Seq[QuestionResult], filteredAnswers: Seq[AnswerDetails]): Seq[QuestionResult] = records

//    override def validateAnswer(validAnswers: Seq[String], answer: String, selection: Selection)(implicit ec: ExecutionContext, appConfig: AppConfig): Future[AnswerCorrectness] = {
//      val answers = validAnswers.map(convertAnswer).map(_.toBigInt)
//      val intAnswer = convertAnswer(answer).toBigInt
//      val offset = appConfig.saAnswerOffset
//      val result = if (answers.exists(a => a - offset <= intAnswer && a + offset >= intAnswer)) Match else NoMatch(answers.map(_.toString))
//      Future.successful(result)
//    }
}
