/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.questionrepository.models.{AnswerCheck, IdentifiersMismatch, Selection}
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository
import uk.gov.hmrc.questionrepository.services.AnswerVerificationService
import akka.Done
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class AnswerController @Inject()(answersVerificationService: AnswerVerificationService,
                                 questionMongoRepository: QuestionMongoRepository)
                                (implicit cc: ControllerComponents, ec: ExecutionContext)
  extends BackendController(cc)
    with Logging {

  def answer(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[AnswerCheck] { answerCheck =>
      withValidIdentifiers(answerCheck).flatMap { _ =>
        answersVerificationService.checkAnswers(answerCheck) map { score =>
          Ok(Json.toJson(score))
        }
      }.recoverWith {
        case IdentifiersMismatch =>
          logger.warn(s"supplied origin, correlationId and identifies not found for questionKey(s) ${answerCheck.answers.map{a => a.questionKey}.mkString(",")}")
          Future.successful(NotFound)
        case e =>
          logger.warn(s"An unexpected error has occurred: ${e.getClass}, ${e.getMessage}")
          Future.successful(InternalServerError(e.getMessage))
      }
    }
  }

  def withValidIdentifiers(answerCheck: AnswerCheck): Future[Done] =
    questionMongoRepository.findAnswers(answerCheck.correlationId, Selection(answerCheck.origin, answerCheck.identifiers)) map
      (questionDataCaches => if (questionDataCaches.nonEmpty) Done else throw IdentifiersMismatch)

}
