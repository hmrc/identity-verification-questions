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

import play.api.libs.json.Format
import uk.gov.hmrc.identityverificationquestions.services.json.{Mapping, Mappings}

abstract class QuestionKey(val name: String, val evidenceOption: String) extends Ordered[QuestionKey] {
  override def compare(that: QuestionKey): Int = this.name.compareTo(that.name)
}

object QuestionKey {
  val keys = Seq(
    P60.PaymentToDate,
    P60.EmployeeNIContributions,
    P60.EarningsAbovePT,
    P60.StatutoryMaternityPay,
    P60.StatutorySharedParentalPay,
    P60.StatutoryAdoptionPay,
    P60.StudentLoanDeductions,
    P60.PostgraduateLoanDeductions,
    Payslip.IncomeTax,
    Payslip.NationalInsurance,
    PassportQuestion,
    SCPEmailQuestion,
    DVLAQuestion,
    SelfAssessment.SelfAssessedIncomeFromPensionsQuestion,
    SelfAssessment.SelfAssessedPaymentQuestion,
    PayeRefQuestion.DateOfPayment,
    PayeRefQuestion.AmountOfPayment,
    TaxCredits.Amount,
    TaxCredits.BankAccount,
    Vat.ValueOfSalesAmount,
    Vat.ValueOfPurchasesAmount
  )

  val mapping: Mapping[String, QuestionKey] = Mappings.mapOption[String, QuestionKey](fromString, _.name)
  implicit val format: Format[QuestionKey] = mapping.jsonFormat

  def fromString(keyName: String): Option[QuestionKey] = {
    keys.find(_.name == keyName)
  }
}

/**
 * All P60 question keys will provide the following info fields:
 * - currentTaxYear (YYYY/YY)
 * - previousTaxYear (YYYY/YY)
 */
case object P60 {
  case object PaymentToDate extends QuestionKey("rti-p60-payment-for-year", "P60")
  case object EmployeeNIContributions extends QuestionKey("rti-p60-employee-ni-contributions", "P60")
  case object EarningsAbovePT extends QuestionKey("rti-p60-earnings-above-pt", "P60")
  case object StatutoryMaternityPay extends QuestionKey("rti-p60-statutory-maternity-pay", "P60")
  case object StatutorySharedParentalPay extends QuestionKey("rti-p60-statutory-shared-parental-pay", "P60")
  case object StatutoryAdoptionPay extends QuestionKey("rti-p60-statutory-adoption-pay", "P60")
  case object StudentLoanDeductions extends QuestionKey("rti-p60-student-loan-deductions", "P60")
  case object PostgraduateLoanDeductions extends QuestionKey("rti-p60-postgraduate-loan-deductions", "P60")
}

case object Payslip {
  case object IncomeTax extends QuestionKey("rti-payslip-income-tax", "Payslip")
  case object NationalInsurance extends QuestionKey("rti-payslip-national-insurance", "Payslip")
}

case object PassportQuestion extends QuestionKey("passport", "passport")
case object SCPEmailQuestion extends QuestionKey("scpEmail", "scpEmail")
case object DVLAQuestion extends QuestionKey("dvla", "dvla")

case object SelfAssessment {
  case object SelfAssessedIncomeFromPensionsQuestion extends QuestionKey("sa-income-from-pensions", "SelfAssessment")
  case object SelfAssessedPaymentQuestion extends QuestionKey("sa-payment-details", "SelfAssessment")
}

case object PayeRefQuestion {
  case object DateOfPayment extends QuestionKey("paye-date-of-payment", "PayeRef")
  case object AmountOfPayment extends QuestionKey("paye-payment-amount", "PayeRef")
}

case object TaxCredits {
  case object BankAccount extends QuestionKey("ita-bankaccount", "national-tax-credit")
  case object Amount extends QuestionKey("tc-amount", "national-tax-credit")
}

/**
 * The ValueOfSalesAmount is also known as Box 6
 *
 * The ValueOfPurchasesAmount is also known as Box 7
 **/
case object Vat {
  case object ValueOfSalesAmount extends QuestionKey("value-of-sales-amount", "vat")
  case object ValueOfPurchasesAmount extends QuestionKey("value-of-purchases-amount", "vat")
}

sealed trait ServiceName
case object p60Service extends ServiceName
case object payslipService extends ServiceName
case object passportService extends ServiceName
case object scpEmailService extends ServiceName
case object dvlaService extends ServiceName
case object selfAssessmentService extends ServiceName
case object desPayeService extends ServiceName
case object taxCreditService extends ServiceName
case object vatService extends ServiceName
