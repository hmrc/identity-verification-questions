/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models.Payment

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