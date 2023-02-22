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
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}


case class VatReturnSubmission(periodKey: String,
                               vatDueSales: BigDecimal,
                               vatDueAcquisitions: BigDecimal,
                               vatDueTotal: BigDecimal,
                               vatReclaimedCurrPeriod: BigDecimal,
                               vatDueNet: BigDecimal,
                               totalValueSalesExVAT: BigDecimal,
                               totalValuePurchasesExVAT: BigDecimal,
                               totalValueGoodsSuppliedExVAT: BigDecimal,
                               totalAllAcquisitionsExVAT: BigDecimal)

object VatReturnSubmission {
  implicit val writes: OWrites[VatReturnSubmission] = Json.writes[VatReturnSubmission]
  implicit val reads: Reads[VatReturnSubmission] = (
    (JsPath \ "periodKey").read[String] and
      (JsPath \ "vatDueSales").read[BigDecimal] and  //Vat due on sales
      (JsPath \ "vatDueAcquisitions").read[BigDecimal] and //VAT due on acquisitions
      (JsPath \ "vatDueTotal").read[BigDecimal] and  //Total VAT due
      (JsPath \ "vatReclaimedCurrPeriod"). read[BigDecimal] and  //Total VAT reclaimed
      (JsPath \ "vatDueNet").read[BigDecimal] and  //Net VAT due
      (JsPath \ "totalValueSalesExVAT").read[BigDecimal] and  //Total sales box6
      (JsPath \ "totalValuePurchasesExVAT").read[BigDecimal] and  //Total purchases box7
      (JsPath \ "totalValueGoodsSuppliedExVAT").read[BigDecimal] and  //Total supply
      (JsPath \ "totalAllAcquisitionsExVAT").read[BigDecimal]  //Total acquisitions
    )(VatReturnSubmission.apply _)
}
