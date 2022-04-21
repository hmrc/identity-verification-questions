package uk.gov.hmrc.questionrepository.monitoring.auditing

import com.google.inject.Inject
import play.api.i18n.{LangImplicits, MessagesApi}
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.AuditExtensions.AuditHeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.monitoring.{EventHandler, MonitoringEvent, ServiceUnavailableEvent}

import scala.concurrent.ExecutionContext

@Singleton
class AuditEventHandler @Inject()(connector: AuditConnector, appConfig: AppConfig)(implicit val messagesApi: MessagesApi)
  extends EventHandler {

  private lazy val factory = new AuditEventFactory(appConfig)


  override def handleEvent(event: MonitoringEvent)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Unit = {
    event match {
      case e: ServiceUnavailableEvent => sendEvent(e,factory.serviceUnavailableEvent)
    }
  }

  private def sendEvent[E <: MonitoringEvent](event: E, create: E => DataEvent)(implicit hc: HeaderCarrier, ec: ExecutionContext) = {
    val dataEvent = create(event)
    connector.sendEvent(dataEvent)
  }
}

private[auditing] class AuditEventFactory(config: AppConfig)(implicit override val messagesApi: MessagesApi)
  extends LangImplicits {
  private def generateDataEvent(auditType: String,
                                event: JourneyMonitoringEvent,
                                additionalDetail: Map[String, String])
                               (implicit request: Request[_], hc: HeaderCarrier) = {
    val carrier: AuditHeaderCarrier = AuditExtensions.auditHeaderCarrier(hc)

    val commonDetail = carrier.toAuditDetails(
      "deviceFingerprint" -> DeviceFingerprint.deviceFingerprintFrom(request),
      "deviceID" -> deviceIdService.getDeviceId()(request).uuid,
      "nino" -> event.journey.nino.flatMap(_.nino).getOrElse(""),
      "confidenceLevel" -> event.journey.serviceContract.confidenceLevel.level.toString,
      "origin" -> event.journey.serviceContract.origin.value,
      "journeyId" -> event.journey.journeyId.toString,
      "journeyType" -> event.journey.journeyType.toString,
      "authProviderId" -> event.journey.authProviderId.getOrElse(AuthProviderId("Unknown AuthProviderId")).value
    )

    DataEvent(
      auditSource = config.appName,
      auditType = auditType,
      tags = carrier.toAuditTags(auditType, request.path),
      detail = commonDetail ++ additionalDetail
    )
  }
}