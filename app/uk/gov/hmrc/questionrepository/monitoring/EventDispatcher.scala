/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.monitoring

import javax.inject.{Inject, Singleton}
import play.api.Logging
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.monitoring.analytics.AnalyticsEventHandler

import scala.concurrent.ExecutionContext

trait EventHandler {
  def handleEvent(event: MonitoringEvent)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Unit
}

@Singleton
class EventDispatcher @Inject()(analyticsEventHandler: AnalyticsEventHandler) extends Logging {

  def dispatchEvent(event: MonitoringEvent)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    try {
      analyticsEventHandler.handleEvent(event)
    } catch {
      case ex: Exception => logger.warn(s"Exception when invoking event handler:", ex)
    }

  }
}