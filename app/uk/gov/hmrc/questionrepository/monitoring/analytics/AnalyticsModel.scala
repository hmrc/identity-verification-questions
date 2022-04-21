/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.monitoring.analytics

case class DimensionValue(index: Int, value: String)

case class Event(category: String, action: String, label: String, dimensions: Seq[DimensionValue])

case class AnalyticsRequest(gaClientId: Option[String], events: Seq[Event])
