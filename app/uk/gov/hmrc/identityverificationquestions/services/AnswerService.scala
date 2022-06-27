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

import play.api.Logging
import play.api.mvc.Request
import uk.gov.hmrc.circuitbreaker.UsingCircuitBreaker
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.identityverificationquestions.connectors.AnswerConnector
import uk.gov.hmrc.identityverificationquestions.models._
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


abstract class AnswerService @Inject()(implicit ec: ExecutionContext) extends UsingCircuitBreaker
  with Logging {
  type Record

  def auditService: AuditService

  def serviceName: ServiceName

  def connector: AnswerConnector[Record]

  def supportedQuestions: Seq[QuestionKey]

  def answerTransformer(records: Seq[Record], filteredAnswers: Seq[AnswerDetails]): Seq[QuestionResult]

  def unknownResult(answers: Seq[AnswerDetails]): Seq[QuestionResult] =
    answers.map(answer => QuestionResult(answer.questionKey, Unknown))

  override def breakOnException(t: Throwable): Boolean = t match {
    case _: NotFoundException | _: BadRequestException => false
    case _ => true
  }

  def checkAnswers(answerCheck: AnswerCheck)(implicit request: Request[_], hc: HeaderCarrier): Future[Seq[QuestionResult]] = {
    val filteredAnswers: Seq[AnswerDetails] = answerCheck.answers.filter(a => supportedQuestions.contains(a.questionKey))
    val selection = answerCheck.selection

    // Removed isAvailable check in VER-2219
    // We don't need to check the availability of services where we cache the answer data up-front
    // For services that we call after answer submission, we will need to check availability, but not the selection
    withCircuitBreaker {
      for {
        correctAnswers <- Future.sequence(filteredAnswers.map(answer =>
          connector.verifyAnswer(
            answerCheck.correlationId,
            answer,
            ivJourney = answerCheck.ivJourney //for iv calls only
          )
        ))
        result = answerTransformer(correctAnswers, filteredAnswers)
      } yield result
    } recover {
      case e: UpstreamErrorResponse if e.statusCode == 404 => {
        logger.warn(s"$serviceName, no answers returned for selection, correlationId: ${answerCheck.correlationId}, " +
          s"selection: ${selection.toList.map(selection.obscureIdentifier).mkString(",")}")
        unknownResult(filteredAnswers)
      }
      case t: Throwable => {
        logger.error(s"$serviceName, threw exception $t, correlationId: ${answerCheck.correlationId}, " +
          s"selection: ${selection.toList.map(selection.obscureIdentifier).mkString(",")}")
        unknownResult(filteredAnswers)
      }
    }
  }
}
