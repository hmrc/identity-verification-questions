/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json._

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
}
