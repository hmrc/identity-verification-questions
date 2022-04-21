/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.monitoring.analytics

import javax.inject.Inject
import play.api.Logging
import play.api.mvc.Request
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.monitoring.{EventHandler, MonitoringEvent, ServiceUnavailableEvent}

import scala.concurrent.ExecutionContext


@Singleton
class AnalyticsEventHandler @Inject()(connector: AnalyticsConnector, config: AppConfig) extends EventHandler with Logging {

  private lazy val factory = new AnalyticsRequestFactory(config)

  override def handleEvent(event: MonitoringEvent)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    event match {
      case e: ServiceUnavailableEvent => sendEvent(factory.serviceUnavailableEvent(e.serviceName))
      case _ => ()
    }
  }

  private def clientId(implicit request: Request[_]) = request.cookies.get("_ga").map(_.value)

  private def sendEvent(reqCreator: (Option[String]) => AnalyticsRequest)
                       (implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    val xSessionId: Option[String] = request.headers.get(HeaderNames.xSessionId)
    if (clientId.isDefined || xSessionId.isDefined) {
      val analyticsRequest = reqCreator(clientId)
      connector.sendEvent(analyticsRequest)
    } else {
      logger.info("VER-381 - No sessionId found in request")
    }
  }
}

private class AnalyticsRequestFactory(config: AppConfig) {

  def serviceUnavailableEvent(serviceName: String)(clientId: Option[String]): AnalyticsRequest={
    val gaEvent = Event("sos_iv" , "circuit_breaker","P60service_unavailable_circuit-breaker".toLowerCase, Seq())
    AnalyticsRequest(clientId, Seq(gaEvent))
  }
}