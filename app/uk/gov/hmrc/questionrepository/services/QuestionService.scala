/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import uk.gov.hmrc.circuitbreaker.UsingCircuitBreaker
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException}
import uk.gov.hmrc.questionrepository.models.{Origin, Question, Selection}
import play.api.Logging
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.Identifier._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

abstract class QuestionService @Inject()(implicit val appConfig: AppConfig) extends UsingCircuitBreaker
  with Logging {

  type Record

  def serviceName: String

  def connector: QuestionConnector[Record]

  def isAvailable(origin: Origin, identifiers: Seq[Identifier]): Boolean

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

  def questions(selection: Selection)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[Question]] = {
    if (isAvailable(selection.origin, selection.identifiers)) {
      withCircuitBreaker {
        connector.getRecords(selection).map(evidenceTransformer)
      } recover {
        case _: NotFoundException => {
          logger.info(s"$serviceName, no records returned for selection, origin: ${selection.origin}, identifiers: ${selection.identifiers.mkString(",")}")
          Seq()
        }
        case t: Throwable => {
          logger.error(s"$serviceName, threw exception $t, origin: ${selection.origin}, identifiers: ${selection.identifiers.mkString(",")}")
          Seq()
        }
      }
    } else {
      Future.successful(Seq())
    }
  }
}
