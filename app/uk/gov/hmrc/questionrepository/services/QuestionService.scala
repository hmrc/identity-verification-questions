/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import play.api.Logging
import uk.gov.hmrc.circuitbreaker.{UnhealthyServiceException, UsingCircuitBreaker}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.identifier._
import uk.gov.hmrc.questionrepository.models.{Origin, Question, Selection, ServiceName}
import play.api.mvc.Request
import uk.gov.hmrc.questionrepository.monitoring.auditing.AuditService
import uk.gov.hmrc.questionrepository.monitoring.{EventDispatcher, ServiceUnavailableEvent}


import scala.concurrent.{ExecutionContext, Future}

trait QuestionService extends UsingCircuitBreaker with Logging {

  type Record

  implicit val eventDispatcher: EventDispatcher

  implicit val auditService: AuditService

  def serviceName: ServiceName

  def connector: QuestionConnector[Record]

  def isAvailable(selection: Selection): Boolean

  def evidenceTransformer(records: Seq[Record]): Seq[Question]

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

  def questions(selection: Selection)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Question]] = {
    if (isAvailable(selection.origin, selection.identifiers)) {
      withCircuitBreaker {
        connector.getRecords(selection).map(evidenceTransformer)
      } recover {
        case e: UpstreamErrorResponse if e.statusCode == 404 =>
          logger.info(s"$serviceName, no records returned for selection, origin: ${selection.origin}, identifiers: ${selection.identifiers.mkString(",")}")
          Seq()
        case _: UnhealthyServiceException =>
          auditService.sendCircuitBreakerEvent(selection.identifiers, serviceName.toString)
          eventDispatcher.dispatchEvent(ServiceUnavailableEvent(serviceName.toString))
          Seq()
        case t: Throwable =>
          logger.error(s"$serviceName, threw exception $t, selection: $selection")
          Seq()
      }
    } else {
      Future.successful(Seq())
    }
  }
}
