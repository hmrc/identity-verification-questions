/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, Json}

case class Selections(identifiers: List[Identifier])

object Selections{
  implicit val format: Format[Selections] = Json.format[Selections]
}
