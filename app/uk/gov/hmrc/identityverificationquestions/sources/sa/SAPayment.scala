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

package uk.gov.hmrc.identityverificationquestions.sources.sa

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.identityverificationquestions.models.JsonLocalDateFormats.dFormat

// TODO why are we using Joda date here??

case class SAPayment(amount: BigDecimal, paymentDate: Option[LocalDate], transactionCode: Option[String] = None)

object SAPayment {
  implicit val saPaymentReads: Reads[SAPayment] = (
    (__ \ "amount" \ "amount").readNullable[BigDecimal] and
      (__ \ "createdDate").readNullable[LocalDate] and
      (__ \ "transactionCode").readNullable[String]
  )((payment, year, transactionCode) =>
    SAPayment(payment.getOrElse(BigDecimal(0)), year, transactionCode)
  )

  implicit val saPaymentsReads: Reads[Seq[SAPayment]] = (
    (__ \ "paymentsList").read[List[SAPayment]] and
      (__ \ "").readNullable[Int]
    )((payments, _) => payments
  )

  implicit val saPaymentWrites: Writes[SAPayment] = Json.writes[SAPayment]
}
