/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models.payment

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
