/*
 * Copyright 2022 HM Revenue & Customs
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
                   nationalInsurancePaid: Option[BigDecimal] = None,
                   earningsAbovePT: Option[BigDecimal] = None,
                   statutoryMaternityPay: Option[BigDecimal] = None,
                   statutorySharedParentalPay: Option[BigDecimal] = None,
                   statutoryAdoptionPay: Option[BigDecimal] = None,
                   studentLoanDeductions: Option[BigDecimal] = None,
                   postgraduateLoanDeductions: Option[BigDecimal] = None)

object Payment {
  implicit val paymentReads: Reads[Payment] = {

    def findValue(fieldName: String, paymentItems: Seq[PaymentItem]): Option[BigDecimal] = paymentItems.filter(_.`type` == fieldName) match {
      case Seq(paym) => Some(paym.amount)
      case _ => None
    }

    ((__ \ "pmtDate").read[LocalDate] and
      (__ \ "mandatoryMonetaryAmount").tolerantReadNullable[Seq[PaymentItem]] and
      (__ \ "niLettersAndValues").tolerantReadNullable[Seq[NiLettersAndValues]] and
      (__ \ "optionalMonetaryAmount").tolerantReadNullable[Seq[PaymentItem]]
      ) { (paymentDate, optMandatoryPaymentItems, optNiLettersAndValues, optionalMonetaryPaymentItems) =>
      val niLettersAndValues = optNiLettersAndValues.getOrElse(Seq.empty)
      val mandatoryPayments = optMandatoryPaymentItems.getOrElse(Seq.empty)
      val optionalMonetaryPayment = optionalMonetaryPaymentItems.getOrElse(Seq.empty)
      val taxablePayYtd = findValue("TaxablePayYTD", mandatoryPayments)
      val employeeNIContrib = findValue("EmpeeContribnsYTD", niLettersAndValues.flatMap(_.niFigure))
      val incomeTaxPaid = findValue("TaxDeductedOrRefunded", mandatoryPayments)
      val nationalInsPaid = findValue("EmpeeContribnsInPd", niLettersAndValues.flatMap(_.niFigure))
      val earningsAbovePT = findValue("PTtoUELYTD", niLettersAndValues.flatMap(_.niFigure))
      val statutoryMaternityPay = findValue("SMPYTD", optionalMonetaryPayment)
      val statutorySharedParentalPay = findValue("SHPPYTD", optionalMonetaryPayment)
      val statutoryAdoptionPay = findValue("SAPYTD", optionalMonetaryPayment)
      val studentLoanDeductions = findValue("StudentLoansYTD", optionalMonetaryPayment)
      val postgraduateLoanDeductions = findValue("PostGraduateLoansYTD", optionalMonetaryPayment)
      Payment(paymentDate, taxablePayYtd, employeeNIContrib, incomeTaxPaid, nationalInsPaid, earningsAbovePT,
        statutoryMaternityPay, statutorySharedParentalPay, statutoryAdoptionPay, studentLoanDeductions, postgraduateLoanDeductions)
    }
  }
}
