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

package uk.gov.hmrc.identityverificationquestions.sources

import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.models.{CorrelationId, QuestionWithAnswers, Selection}
import uk.gov.hmrc.identityverificationquestions.services.QuestionService

import scala.concurrent.{ExecutionContext, Future}

trait QuestionServiceMeoMinimumNumberOfQuestions extends QuestionService {

  implicit val appConfig:AppConfig

  lazy val minimumNumber = appConfig.minimumMeoQuestionCount(serviceName.toString)

  override def questions(selection: Selection, corrId: CorrelationId)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[QuestionWithAnswers]] =
    for {
      foundQuestions <- super.questions(selection, corrId)
      questionsToReturn = if (foundQuestions.size >= minimumNumber) foundQuestions else Seq()
    } yield questionsToReturn
}
