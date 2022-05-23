/*
 * Copyright 2022 HM Revenue & Customs
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
import uk.gov.hmrc.domain.{Nino, SaUtr}

import java.time.LocalDate


/**
 * A set of identifiers to retrieve questions for.
 * At least one identifier should be provided
 */
case class Selection(nino: Option[Nino], sautr: Option[SaUtr], dob: Option[LocalDate]) {

  require(nino.isDefined || sautr.isDefined || dob.isDefined, "Must supply at least one identifier")

  /**
   * Will return true if AT LEAST ONE non empty requested identifier is contained within this Selection
   *
   * e.g. this selection: sautr:123456789; query: nino:AA000000D ---- should be false (no matches)
   *      this selection: nino:AA000000D, sautr:123456789; query: nino: AA000002D, sautr:123456789 ---- should be true (one match)
   */
  def contains(input: Selection): Boolean = {
    val containsNino = input.nino.isDefined && input.nino == this.nino
    val containsUtr = input.sautr.isDefined && input.sautr == this.sautr
    val containsDob = input.dob.isDefined && input.dob == this.dob
    containsNino || containsUtr || containsDob
  }

  override def toString: String =
    List(nino.map(_.nino), sautr.map(_.utr), dob.map(_.toString))
      .flatten
      .mkString(",")
}

object Selection {

  implicit val format: Format[Selection] = Json.format[Selection]

  def apply(nino: Nino): Selection = Selection(Some(nino), None, None)

  def apply(saUtr: SaUtr): Selection = Selection(None, Some(saUtr), None)

  def apply(nino: Nino, saUtr: SaUtr): Selection = Selection(Some(nino), Some(saUtr), None)

}
