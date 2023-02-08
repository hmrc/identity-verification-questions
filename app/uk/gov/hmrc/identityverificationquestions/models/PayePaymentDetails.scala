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

import play.api.libs.json.{Json, OFormat}

case class PayePaymentAmount(amount: BigDecimal, currency: String)

case class PayePayment(paymentAmount: PayePaymentAmount, paymentDate: String)

case class PayePaymentsDetails(payments: Option[List[PayePayment]])

object PayePaymentAmount {
  implicit val formatPaymentAmount: OFormat[PayePaymentAmount] = Json.format[PayePaymentAmount]
}

object PayePayment {
  implicit val formatPayment: OFormat[PayePayment] = Json.format[PayePayment]
}

object PayePaymentsDetails {
  implicit val formatPaymentsDetails: OFormat[PayePaymentsDetails] = Json.format[PayePaymentsDetails]
}
