/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.domain.Nino

case class NinoClStoreEntry(credId: String, nino: Nino, confidenceLevel: Option[ConfidenceLevel])

object NinoClStoreEntry {
  implicit val ninoCLStoreEntryFormat: Format[NinoClStoreEntry] = Json.format[NinoClStoreEntry]
}
