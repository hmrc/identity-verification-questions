/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.questionrepository.models.Selection

import scala.concurrent.Future

@Singleton()
class QuestionController @Inject()(implicit cc: ControllerComponents)
  extends BackendController(cc){

  def question(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[Selection] { Selection =>
      Future.successful(NotImplemented("Not yet implemented for request:" + request)) }
  }

  def answer(): Action[AnyContent] =  Action.async{ implicit request =>
    Future.successful(NotImplemented("Not yet implemented for request:" + request))
  }

}
