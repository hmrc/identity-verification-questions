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
import uk.gov.hmrc.circuitbreaker.{UnhealthyServiceException, UsingCircuitBreaker}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.identityverificationquestions.connectors.QuestionConnector
import uk.gov.hmrc.identityverificationquestions.models.{QuestionWithAnswers, Selection, ServiceName}
import uk.gov.hmrc.identityverificationquestions.monitoring.auditing.AuditService
import uk.gov.hmrc.identityverificationquestions.monitoring.{EventDispatcher, ServiceUnavailableEvent}

import scala.concurrent.{ExecutionContext, Future}

trait QuestionService extends UsingCircuitBreaker with Logging {

  type Record

  implicit val eventDispatcher: EventDispatcher

  implicit val auditService: AuditService

  def serviceName: ServiceName

  def connector: QuestionConnector[Record]

  def isAvailableForRequestedSelection(selection: Selection): Boolean

  def evidenceTransformer(records: Seq[Record]): Seq[QuestionWithAnswers]

  /** All the HODs return 404s for an unknown Nino, so these
   *  should never trigger the circuit breaker. The only exception
   *  is the passport API, which always returns 200 SOAP responses
   *  when the user cannot be identified, which also does not affect
   *  the circuit breaker.
   *
   *  When adding new endpoints, this config must be re-validated,
   *  as it is shared by all connectors.
   */
  override def breakOnException(t: Throwable): Boolean = t match {
    case _: NotFoundException | _: BadRequestException => false
    case _ => true
  }

  def questions(selection: Selection)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[QuestionWithAnswers]] = {
    val origin = request.headers.get("user-agent").getOrElse("unknown origin")
    if (isAvailableForRequestedSelection(selection)) {
      withCircuitBreaker {
        connector.getRecords(selection).map(evidenceTransformer)
      } recover {
        case _: UnhealthyServiceException =>
          auditService.sendCircuitBreakerEvent(selection, serviceName.toString)
          eventDispatcher.dispatchEvent(ServiceUnavailableEvent(serviceName.toString))
          logger.error(s"$serviceName is Unavailable, UnhealthyServiceException raised.")
          Seq()
        case t: Throwable =>
          logger.error(s"$serviceName, threw exception $t, origin: $origin, selection: ${selection.toList.map(selection.obscureIdentifier).mkString(",")}")
          Seq()
      }
    } else {
      Future.successful(Seq())
    }
  }
}
