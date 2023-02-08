/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.identityverificationquestions.models

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.domain.{EmpRef, Nino, SaUtr}

import java.time.LocalDate


/**
 * A set of identifiers to retrieve questions for.
 * At least one identifier should be provided
 */
case class Selection(nino: Option[Nino], sautr: Option[SaUtr], dob: Option[LocalDate], payeRef: Option[EmpRef]) {

  require(nino.isDefined || sautr.isDefined || dob.isDefined || payeRef.isDefined, "Must supply at least one identifier")

  def obscureIdentifier(identifier: String): String = ("X" * 4) + identifier.drop(4) //eg AA000000D to XXXX0000D

  def toList: List[String] = List(nino.map(_.nino), sautr.map(_.utr), dob.map(_.toString)).flatten

  override def toString: String = toList.mkString(",")
}

object Selection {

  implicit val format: Format[Selection] = Json.format[Selection]

  def apply(nino: Nino): Selection = Selection(Some(nino), None, None, None)

  def apply(saUtr: SaUtr): Selection = Selection(None, Some(saUtr), None, None)

  def apply(nino: Nino, saUtr: SaUtr): Selection = Selection(Some(nino), Some(saUtr), None, None)

  def apply(payeRef: EmpRef): Selection = Selection(None, None, None, Some(payeRef))

}
