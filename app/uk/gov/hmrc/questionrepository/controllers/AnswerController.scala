/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.controllers

import akka.Done
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.models.{AnswerCheck, IdentifiersMismatch}
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository
import uk.gov.hmrc.questionrepository.services.AnswerVerificationService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class AnswerController @Inject()(answersVerificationService: AnswerVerificationService,
                                 questionMongoRepository: QuestionMongoRepository, appConfig: AppConfig)
                                (implicit cc: ControllerComponents, ec: ExecutionContext)
  extends BackendController(cc)
    with Logging {

  def answer(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val userAgent: Option[String] = request.headers.get("User-Agent")
    val userAllowed: Boolean = appConfig.allowedUserAgentList.contains(userAgent.getOrElse(""))

    if (userAllowed) {
      withJsonBody[AnswerCheck] { answerCheck =>
        withValidIdentifiers(answerCheck).flatMap { _ =>
          answersVerificationService.checkAnswers(answerCheck) map { score =>
            Ok(Json.toJson(score))
          }
        }.recoverWith {
          case IdentifiersMismatch =>
            logger.warn(s"supplied correlationId and selection not found for questionKey(s) ${answerCheck.answers.map{a => a.questionKey}.mkString(",")}")
            Future.successful(NotFound)
          case e =>
            logger.warn(s"An unexpected error has occurred: ${e.getClass}, ${e.getMessage}")
            Future.successful(InternalServerError(e.getMessage))
        }
      }
    } else {
      logger.warn(s"Unauthorised client called question repository, User-Agent is: $userAgent")
      Future.successful(Forbidden("You are not authorised to use question repository - please contact team verification"))
    }

  }

  def withValidIdentifiers(answerCheck: AnswerCheck): Future[Done] =
    questionMongoRepository.findAnswers(answerCheck.correlationId, answerCheck.selection) map
      (questionDataCaches => if (questionDataCaches.nonEmpty) Done else throw IdentifiersMismatch)

}
