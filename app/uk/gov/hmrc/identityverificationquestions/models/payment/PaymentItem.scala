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

package uk.gov.hmrc.identityverificationquestions.models.payment

import play.api.libs.json.{JsSuccess, Json, Reads}

case class PaymentItem(`type`: String, amount: BigDecimal)

object PaymentItem {
  implicit val paymentItemReads: Reads[PaymentItem] = Json.reads[PaymentItem]
}

case class NiLettersAndValues(niFigure: Seq[PaymentItem])

object NiLettersAndValues {
  implicit val niLettersAndValuesReads: Reads[NiLettersAndValues] = Reads[NiLettersAndValues] { json =>
    val niFigure = (json \ "niFigure").asOpt[Seq[PaymentItem]]
    JsSuccess(NiLettersAndValues(niFigure.getOrElse(Seq.empty)))
  }
}