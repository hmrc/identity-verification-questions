/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.monitoring.auditing

import javax.inject.Inject
import play.api.libs.json.Json.prettyPrint
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions.auditHeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}
import uk.gov.hmrc.questionrepository.models.identifier.Identifier

import scala.concurrent.{ExecutionContext, Future}

class AuditService @Inject()(auditConnector: AuditConnector){

  val AuditSource = "question-repository"


  def sendCircuitBreakerEvent(identifiers: Seq[Identifier], unavailableServiceName: String)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[AuditResult] = {
    val tags = Map("transactionName" -> "CircuitBreakerUnhealthyEvent")

    auditConnector.sendEvent(
      DataEvent(
        auditSource = AuditSource,
        auditType = "CircuitBreakerUnhealthyService",
        detail = Map("unavailableServiceName" -> s"$unavailableServiceName", "identifiers" -> identifiers.mkString(",")),
        tags = tags
      )
    )
  }
}

