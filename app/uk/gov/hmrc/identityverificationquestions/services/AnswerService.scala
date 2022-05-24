/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.identityverificationquestions.services

import javax.inject.Inject
import play.api.Logging
import uk.gov.hmrc.circuitbreaker.UsingCircuitBreaker
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.identityverificationquestions.config.AppConfig
import uk.gov.hmrc.identityverificationquestions.connectors.AnswerConnector
import uk.gov.hmrc.identityverificationquestions.models._

import scala.concurrent.{ExecutionContext, Future}


abstract class AnswerService @Inject()(implicit val appConfig: AppConfig, ec: ExecutionContext) extends UsingCircuitBreaker
  with Logging {
  type Record

  def serviceName: ServiceName

  def connector: AnswerConnector[Record]

  def isAvailable(selection: Selection): Boolean

  def supportedQuestions: Seq[QuestionKey]

  def answerTransformer(records: Seq[Record], filteredAnswers: Seq[AnswerDetails]): Seq[QuestionResult]

  def unknownResult(answers: Seq[AnswerDetails]): Seq[QuestionResult] =
    answers.map(answer => QuestionResult(answer.questionKey, Unknown))

  override def breakOnException(t: Throwable): Boolean = t match {
    case _: NotFoundException | _: BadRequestException => false
    case _ => true
  }

  def checkAnswers(answerCheck: AnswerCheck)(implicit hc: HeaderCarrier): Future[Seq[QuestionResult]] = {
    val filteredAnswers = answerCheck.answers.filter(a => supportedQuestions.contains(a.questionKey))

    if (isAvailable(answerCheck.selection)) {
      withCircuitBreaker {
        for {
          correctAnswers <- Future.sequence(filteredAnswers.map(answer => connector.verifyAnswer(answerCheck.correlationId, answerCheck.selection, answer)))
          result = answerTransformer(correctAnswers, filteredAnswers)
        } yield result
      } recover {
        case e: UpstreamErrorResponse if e.statusCode == 404 => {
          logger.info(s"$serviceName, no answers returned for selection, correlationId: ${answerCheck.correlationId}, selection: ${answerCheck.selection}")
          unknownResult(filteredAnswers)
        }
        case t: Throwable => {
          logger.error(s"$serviceName, threw exception $t, correlationId: ${answerCheck.correlationId}, selection: ${answerCheck.selection}")
          unknownResult(filteredAnswers)
        }
      }
    } else {
      Future.successful(unknownResult(filteredAnswers))
    }
  }
}
