/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.controllers

import play.api.Logging
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.models.{QuestionResponse, Selection}
import uk.gov.hmrc.questionrepository.services.EvidenceRetrievalService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class QuestionController @Inject()(evidenceRetrievalService: EvidenceRetrievalService, appConfig: AppConfig)(implicit cc: ControllerComponents, ec: ExecutionContext)
  extends BackendController(cc) with Logging {

  private def toOKResponse[T](result: T)(implicit writes: Writes[T]) = {
    Ok(Json.toJson(result))
  }

  def question(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val userAgent: Option[String] = request.headers.get("User-Agent")
    val userAllowed: Boolean = appConfig.allowedUserAgentList.contains(userAgent.getOrElse(""))
    
    if (userAllowed) {
      withJsonBody[Selection] { selection =>
        evidenceRetrievalService.callAllEvidenceSources(selection) map toOKResponse[QuestionResponse]
      }
    } else {
      logger.warn(s"Unauthorised client called question repository, User-Agent is: $userAgent")
      Future.successful(Forbidden("You are not authorised to use question repository - please contact team verification"))
    }

  }
}
