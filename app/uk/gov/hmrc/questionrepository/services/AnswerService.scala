/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import javax.inject.Inject
import play.api.Logging
import uk.gov.hmrc.circuitbreaker.UsingCircuitBreaker
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.AnswerConnector
import uk.gov.hmrc.questionrepository.models.identifier.Identifier
import uk.gov.hmrc.questionrepository.models._

import scala.concurrent.{ExecutionContext, Future}


abstract class AnswerService @Inject()(implicit val appConfig: AppConfig, ec: ExecutionContext) extends UsingCircuitBreaker
  with Logging {
  type Record

  def serviceName: ServiceName

  def connector: AnswerConnector[Record]

  def isAvailable(origin: Origin, identifiers: Seq[Identifier]): Boolean

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

    if (isAvailable(answerCheck.origin, answerCheck.identifiers)) {
      withCircuitBreaker {
        for {
          correctAnswers <- Future.sequence(filteredAnswers.map(answer => connector.verifyAnswer(answerCheck.correlationId, answerCheck.origin, answerCheck.identifiers, answer)))
          result = answerTransformer(correctAnswers, filteredAnswers)
        } yield result
      } recover {
        case e: UpstreamErrorResponse if e.statusCode == 404 => {
          logger.info(s"$serviceName, no answers returned for selection, correlationId: ${answerCheck.correlationId}, origin: ${answerCheck.origin}, identifiers: ${answerCheck.identifiers.mkString(",")}")
          unknownResult(filteredAnswers)
        }
        case t: Throwable => {
          logger.error(s"$serviceName, threw exception $t, correlationId: ${answerCheck.correlationId}, origin: ${answerCheck.origin}, identifiers: ${answerCheck.identifiers.mkString(",")}")
          unknownResult(filteredAnswers)
        }
      }
    } else {
      Future.successful(unknownResult(filteredAnswers))
    }
  }
}
