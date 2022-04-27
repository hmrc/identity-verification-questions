/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.monitoring

sealed trait MonitoringEvent

case class ServiceUnavailableEvent(serviceName: String) extends MonitoringEvent

