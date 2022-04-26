/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import play.api.Logging
import uk.gov.hmrc.circuitbreaker.UsingCircuitBreaker
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.models.{Question, Selection, ServiceName}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

abstract class QuestionService @Inject()(implicit val appConfig: AppConfig, ec: ExecutionContext) extends UsingCircuitBreaker
  with Logging {

  type Record

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

  def questions(selection: Selection)(implicit hc: HeaderCarrier): Future[Seq[Question]] = {
    if (isAvailable(selection)) {
      withCircuitBreaker {
        connector.getRecords(selection).map(evidenceTransformer)
      } recover {
        case e: UpstreamErrorResponse if e.statusCode == 404 => {
          logger.info(s"$serviceName, no records returned for selection: $selection")
          Seq()
        }
        case t: Throwable => {
          logger.error(s"$serviceName, threw exception $t, selection: $selection")
          Seq()
        }
      }
    } else {
      Future.successful(Seq())
    }
  }
}
