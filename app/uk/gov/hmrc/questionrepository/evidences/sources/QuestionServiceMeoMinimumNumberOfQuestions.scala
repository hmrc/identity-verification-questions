/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources

import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.models.{Question, Selection}
import uk.gov.hmrc.questionrepository.services.QuestionService

import scala.concurrent.{ExecutionContext, Future}

trait QuestionServiceMeoMinimumNumberOfQuestions extends QuestionService {

  implicit val appConfig:AppConfig

  lazy val minimumNumber = appConfig.minimumMeoQuestionCount(serviceName.toString)

  override def questions(selection: Selection)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Question]] =
    for {
      foundQuestions <- super.questions(selection)
      questionsToReturn = if (foundQuestions.size >= minimumNumber) foundQuestions else Seq()
    } yield questionsToReturn
}
