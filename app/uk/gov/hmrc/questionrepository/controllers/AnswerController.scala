/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.Future

@Singleton()
class AnswerController  @Inject()(implicit cc: ControllerComponents)
  extends BackendController(cc){

  def answer(): Action[AnyContent] =  Action.async{ implicit request =>
    Future.successful(NotImplemented("Not yet implemented for request:" + request))
  }

}
