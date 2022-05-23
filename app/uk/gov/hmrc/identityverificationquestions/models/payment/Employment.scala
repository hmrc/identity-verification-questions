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

package uk.gov.hmrc.identityverificationquestions.models.payment

import play.api.libs.json.{JsSuccess, Reads}

case class Employment(payments: Seq[Payment]) {
  def paymentsByDateDescending: Seq[Payment] = payments.sortWith((p1, p2) => p1.paymentDate.isAfter(p2.paymentDate))
  def newest: Seq[Payment] = paymentsByDateDescending.filter(_.paymentDate.isEqual(paymentsByDateDescending.head.paymentDate))
}

object Employment{
  implicit val reads: Reads[Employment] = Reads[Employment] { json =>
    val payments = (json \ "payments" \ "inYear").asOpt[Seq[Payment]] match {
      case Some(payments) => payments
      case _ => Seq()
    }
    JsSuccess(Employment(payments))
  }

  implicit val p60ResponseReads = Reads[Seq[Employment]] { json =>
    val employments = (json \ "individual" \ "employments" \ "employment").asOpt[Seq[Employment]] match {
      case Some(employments) => employments
      case _ => Seq()
    }
    JsSuccess(employments)
  }
}
