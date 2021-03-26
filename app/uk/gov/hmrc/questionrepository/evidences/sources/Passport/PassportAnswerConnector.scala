/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.Passport

import play.api.Logging
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{CoreGet, CorePost, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.AnswerConnector
import uk.gov.hmrc.questionrepository.models.identifier.Identifier
import uk.gov.hmrc.questionrepository.models.passport.PassportRequest
import uk.gov.hmrc.questionrepository.models.{AnswerDetails, CorrelationId, Error, Origin, PassportAnswer, QuestionResult, ServiceName, Unknown, passportService}
import uk.gov.hmrc.questionrepository.models.identifier.Search._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import play.api.http.Status._
import uk.gov.hmrc.questionrepository.services.utilities.PassportXmlConfig

class PassportAnswerConnector @Inject()(val http: CoreGet with CorePost)(implicit appConfig: AppConfig, ec: ExecutionContext) extends AnswerConnector[QuestionResult]
  with PassportXmlConfig
  with Logging {

  def serviceName: ServiceName = passportService

  override def verifyAnswer(correlationId: CorrelationId, origin: Origin, identifiers: Seq[Identifier], answer: AnswerDetails)(implicit hc: HeaderCarrier): Future[QuestionResult] = {
    identifiers.dob.fold {
      logger.error(s"$serviceName, No date of birth identifier for ${answer.questionKey}, origin: $origin, identifiers: ${identifiers.mkString(",")}")
      Future.successful(QuestionResult(answer.questionKey, Unknown))
    } { dob =>
      val request = createPassportXml(PassportRequest(dob.toString, answer.answer.asInstanceOf[PassportAnswer]))
      val url = s"${appConfig.serviceBaseUrl(serviceName)}/passport-data-service"

      http.POSTString[HttpResponse](url, request, PassportHeaders) map {
        case resp if resp.status == OK => {
          QuestionResult(answer.questionKey, checkResponse(resp))
        }
        case resp => QuestionResult(answer.questionKey, Error(s"status: ${resp.status} body: ${resp.body}"))
      }
    }
  }
}
