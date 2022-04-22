/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.monitoring.analytics

import akka.Done
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.questionrepository.config.AppConfig
import javax.inject.{Inject, Singleton}
import play.api.mvc.Request

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AnalyticsConnector @Inject() (appConfig: AppConfig, http:HttpClient) extends Logging {
  def serviceUrl: String = appConfig.platformAnalyticsUrl

  private implicit val dimensionWrites = Json.writes[DimensionValue]
  private implicit val eventWrites = Json.writes[Event]
  private implicit val analyticsWrites = Json.writes[AnalyticsRequest]

  def sendEvent(request: AnalyticsRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Done] = {
    val url = s"$serviceUrl/platform-analytics/event"
    http.POST[AnalyticsRequest, HttpResponse](url, request).map(_ => Done).recover {
      case e : Throwable =>
        logger.error(s"Couldn't send analytics event $request")
        Done
    }
  }

}