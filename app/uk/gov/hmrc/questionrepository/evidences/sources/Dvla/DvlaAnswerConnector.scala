/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.Dvla

import play.api.Logging
import play.api.http.Status
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{CoreGet, CorePost, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.AnswerConnector
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.models.dvla.UkDrivingLicenceRequest

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DvlaAnswerConnector @Inject()(http: CoreGet with CorePost)(implicit appConfig: AppConfig, ec: ExecutionContext) extends AnswerConnector[QuestionResult] with Logging {

  def serviceName: ServiceName = dvlaService

  override def verifyAnswer(correlationId: CorrelationId, selection: Selection, answer: AnswerDetails)
                           (implicit hc: HeaderCarrier): Future[QuestionResult] = {
    selection.dob.fold {
      logger.error(s"$serviceName, No date of birth identifier for ${answer.questionKey}, selection: $selection")
      Future.successful(QuestionResult(answer.questionKey, Unknown))
    } { dob =>
      val request = UkDrivingLicenceRequest(dob.toString, answer.answer.asInstanceOf[UkDrivingLicenceAnswer])
      val url = s"${appConfig.serviceBaseUrl(serviceName)}/driving-licence/validate"

      http.POST[UkDrivingLicenceRequest, HttpResponse](url, request).map {
        case resp if resp.status == Status.NO_CONTENT => QuestionResult(answer.questionKey, Correct)
        case resp => QuestionResult(answer.questionKey, Error(s"Unexpected response ${resp.status}"))
      }
    }
  }
}
