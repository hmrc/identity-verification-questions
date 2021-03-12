/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.questionrepository.models.AnswerCheck
import uk.gov.hmrc.questionrepository.services.AnswerVerificationService

import scala.concurrent.ExecutionContext

@Singleton()
class AnswerController @Inject()(answersService: AnswerVerificationService)(implicit cc: ControllerComponents, ec: ExecutionContext)
  extends BackendController(cc) {

  def answer(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[AnswerCheck] { answerCheck =>
      answersService.checkAnswers(answerCheck).map { score =>
        Ok(Json.toJson(score))
      }
    }
  }

}
