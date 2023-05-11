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

import akka.stream.Materializer
import controllers.Assets
import play.api.Configuration
import play.api.http.{ContentTypes, MimeTypes}

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, Codec, ControllerComponents}
import play.filters.cors.CORSActionBuilder
import play.libs.Json
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import views.txt

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class DocumentationController @Inject() (assets: Assets, cc: ControllerComponents, configuration: Configuration)(implicit mat: Materializer, ec: ExecutionContext) extends BackendController (cc) {


  def definition(): Action[AnyContent] =  Action.async {
    Future.successful(Ok(
      """
        |{
        |  "scopes": [],
        |  "api": {
        |    "name": "identity-verification-questions",
        |    "description": "Backend service to provide question data and answer processing by unique user identifier for all verification services in MDTP",
        |    "context": "identity-verification-questions",
        |    "versions": [
        |      {
        |        "version": "1.0",
        |        "status": "STABLE",
        |        "endpointsEnabled": true
        |      },
        |      {
        |        "version": "2.0",
        |        "status": "STABLE",
        |        "endpointsEnabled": true
        |      }
        |    ]
        |  }
        |}
        |""".stripMargin)//.as(ContentTypes.withCharset(MimeTypes.JSON)(Codec.utf_8)))
    )
}


  def specification(version: String, file: String): Action[AnyContent] = {
    println(s"\n\nspecification Method")

    CORSActionBuilder(configuration).async { implicit request =>
      println(s"accessing file ${file}")

      assets.at(s"/public/api/conf/$version", file)(request)
    }
  }
}


