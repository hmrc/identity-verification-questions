/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models.payment

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import java.time.LocalDate
import play.api.libs.json.{Reads, __}
import uk.gov.hmrc.http.controllers.JsPathEnrichment._

case class Payment(paymentDate: LocalDate,
                   taxablePayYTD: Option[BigDecimal] = None,
                   employeeNIContrib: Option[BigDecimal] = None,
                   incomeTax: Option[BigDecimal] = None,
                   nationalInsurancePaid: Option[BigDecimal] = None)

object Payment {
  implicit val paymentReads: Reads[Payment] = {

    def findValue(fieldName: String, paymentItems: Seq[PaymentItem]) = paymentItems.filter(_.`type` == fieldName) match {
      case Seq(paym) => Some(paym.amount)
      case _ => None
    }

    ((__ \ "pmtDate").read[LocalDate] and
      (__ \ "mandatoryMonetaryAmount").tolerantReadNullable[Seq[PaymentItem]] and
      (__ \ "niLettersAndValues").tolerantReadNullable[Seq[NiLettersAndValues]]
      ) { (paymentDate, optMandatoryPaymentItems, optNiLettersAndValues) =>
      val niLettersAndValues = optNiLettersAndValues.getOrElse(Seq.empty)
      val mandatoryPayments = optMandatoryPaymentItems.getOrElse(Seq.empty)
      val taxablePayYtd = findValue("TaxablePayYTD", mandatoryPayments)
      val employeeNIContrib = findValue("EmpeeContribnsYTD", niLettersAndValues.flatMap(_.niFigure))
      val incomeTaxPaid = findValue("TaxDeductedOrRefunded", mandatoryPayments)
      val nationalInsPaid = findValue("EmpeeContribnsInPd", niLettersAndValues.flatMap(_.niFigure))
      Payment(paymentDate, taxablePayYtd, employeeNIContrib, incomeTaxPaid, nationalInsPaid)
    }
  }
}