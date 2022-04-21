/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.monitoring

import com.google.inject.Inject
import play.api.Logging
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.monitoring.analytics.AnalyticsEventHandler
import uk.gov.hmrc.questionrepository.monitoring.auditing.AuditEventHandler

import scala.concurrent.ExecutionContext

trait EventHandler {
  def handleEvent(event: MonitoringEvent)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Unit
}

trait EventDispatcher extends Logging {

  protected def eventHandlers: Seq[EventHandler]

  def applyEventHandler(handler: EventHandler, event: MonitoringEvent)(implicit request: Request[_],hc: HeaderCarrier, ec: ExecutionContext): Unit =
    try {
      handler.handleEvent(event)
    } catch {
      case ex: Exception => logger.warn(s"Exception when invoking event handler:", ex)
    }

  def dispatchEvent(event: MonitoringEvent)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    eventHandlers foreach { applyEventHandler(_, event) }
  }
}

@Singleton
class AuditAndAnalyticsEventDispatcher @Inject() (auditEventHandler: AuditEventHandler, analyticsEventHandler: AnalyticsEventHandler) extends EventDispatcher {
  val eventHandlers = Seq(
    auditEventHandler,
    analyticsEventHandler
  )
}