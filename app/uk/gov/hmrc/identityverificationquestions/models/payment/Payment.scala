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

package uk.gov.hmrc.identityverificationquestions.models.payment

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Reads, __}

import java.time.LocalDate

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
                   postgraduateLoanDeductions: Option[BigDecimal] = None,
                   leavingDate: Option[LocalDate] = None,
                   totalTaxYTD: Option[BigDecimal] = None)

object Payment {
  implicit val paymentReads: Reads[Payment] = {

    def findValue(fieldName: String, paymentItems: Seq[PaymentItem]): Option[BigDecimal] = paymentItems.filter(_.`type` == fieldName) match {
      case Seq(paym) => Some(paym.amount)
      case _ => None
    }

    ((__ \ "pmtDate").read[LocalDate] and
      (__ \ "mandatoryMonetaryAmount").readNullable[Seq[PaymentItem]] and
      (__ \ "niLettersAndValues").readNullable[Seq[NiLettersAndValues]] and
      (__ \ "optionalMonetaryAmount").readNullable[Seq[PaymentItem]] and
      (__ \ "leavingDate").readNullable[LocalDate]
      ) { (paymentDate, optMandatoryPaymentItems, optNiLettersAndValues, optionalMonetaryPaymentItems, leavingDate) =>
      val niLettersAndValues = optNiLettersAndValues.getOrElse(Seq.empty)
      val mandatoryPayments = optMandatoryPaymentItems.getOrElse(Seq.empty)
      val optionalMonetaryPayment = optionalMonetaryPaymentItems.getOrElse(Seq.empty)
      val taxablePayYtd = findValue("TaxablePayYTD", mandatoryPayments)
      val totalTaxYtd = findValue("TotalTaxYTD", mandatoryPayments)
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
        statutoryMaternityPay, statutorySharedParentalPay, statutoryAdoptionPay, studentLoanDeductions, postgraduateLoanDeductions, leavingDate, totalTaxYtd)
    }
  }
}
