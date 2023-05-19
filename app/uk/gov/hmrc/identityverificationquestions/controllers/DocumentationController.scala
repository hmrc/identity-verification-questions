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

import controllers.Assets
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController


@Singleton
class DocumentationController @Inject() (cc: ControllerComponents, assets: Assets) extends BackendController (cc) {

  def definition(): Action[AnyContent] =  Action {Ok(definitionJson)}

  def conf(version: String, file: String): Action[AnyContent] = {assets.at(s"/public/api/conf/$version", file)  }

  val definitionJson: String =
    """
      |{
      |  "scopes": [],
      |  "api": {
      |    "name": "Identity Verification Questions",
      |    "description": "Backend service to provide question data and answer processing by unique user identifier for all verification services in MDTP",
      |    "context": "individuals/identity-verification-questions",
      |    "versions": [
      |      {
      |        "version": "1.0",
      |        "status": "STABLE",
      |        "endpointsEnabled": true,
      |        "access": {
      |              "type": "PRIVATE"
      |             }
      |        }
      |      }
      |    ]
      |  }
      |}
      |""".stripMargin

}


