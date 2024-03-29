/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.identityverificationquestions.controllers

import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.AnswerCheck
import uk.gov.hmrc.identityverificationquestions.services.AnswerVerificationService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class AnswerController @Inject()(answersVerificationService: AnswerVerificationService,
                                 appConfig: AppConfig)
                                (implicit cc: ControllerComponents, ec: ExecutionContext)
  extends BackendController(cc)
    with Logging {

  def answer(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val userAgent: Option[String] = request.headers.get("User-Agent")
    val userAllowed: Boolean = appConfig.allowedUserAgentList.contains(userAgent.getOrElse(""))

    if (userAllowed) {
      withJsonBody[AnswerCheck] { answerCheck =>
        answersVerificationService.checkAnswers(answerCheck) map { score =>
          Ok(Json.toJson(score))
        }
      }
    } else {
      logger.warn(s"Unauthorised client called question repository, User-Agent is: $userAgent")
      Future.successful(Forbidden("You are not authorised to use question repository - please contact team verification"))
    }

  }

}
