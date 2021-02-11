/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models.Payment

import play.api.libs.json.{JsSuccess, Reads}

case class Employment(payments: Seq[Payment]) {
  def paymentsByDateDescending: Seq[Payment] = payments.sortWith((p1, p2) => p1.paymentDate.isAfter(p2.paymentDate))
}

object Employment{
  implicit val reads: Reads[Employment] = Reads[Employment] { json =>
    val payments = (json \ "payments" \ "inYear").asOpt[Seq[Payment]] match {
      case Some(payments) => payments
      case _ => Seq()
    }
    JsSuccess(Employment(payments))
  }
}

case class P60Response(p60Response: Seq[Employment])

object P60Response {
  implicit val p60ResponseReads: Reads[P60Response] = Reads[P60Response] { json =>
    val employments = (json \ "individual" \ "employments" \ "employment").as[Seq[Employment]]
    JsSuccess(P60Response(employments))
  }
}
