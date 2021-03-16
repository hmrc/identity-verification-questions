/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.questionrepository.models.{QuestionResponse, Selection}
import uk.gov.hmrc.questionrepository.services.EvidenceRetrievalService

import scala.concurrent.ExecutionContext

@Singleton()
class QuestionController @Inject()(evidenceRetrievalService: EvidenceRetrievalService)(implicit cc: ControllerComponents, ec: ExecutionContext)
  extends BackendController(cc) {

  private def toOKResponse[T](result: T)(implicit writes: Writes[T]) = Ok(Json.toJson(result))

  def question(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[Selection] { selection =>
      evidenceRetrievalService.callAllEvidenceSources(selection) map toOKResponse[QuestionResponse]
    }
  }
}
