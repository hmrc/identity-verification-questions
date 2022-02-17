/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.Format
import uk.gov.hmrc.questionrepository.services.json.{Mapping, Mappings}

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
    PassportQuestion,
    DVLAQuestion
  )

  val mapping: Mapping[String, QuestionKey] = Mappings.mapOption[String, QuestionKey](fromString, _.name)
  implicit val format: Format[QuestionKey] = mapping.jsonFormat

  def fromString(keyName: String): Option[QuestionKey] = {
    keys.find(_.name == keyName)
  }
}
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

case object PassportQuestion extends QuestionKey("passport", "passport")
case object SCPEmailQuestion extends QuestionKey("rti-p60-payment-for-year", "P60")
case object DVLAQuestion extends QuestionKey("dvla", "dvla")


sealed trait ServiceName
case object p60Service extends ServiceName
case object passportService extends ServiceName
case object scpEmailService extends ServiceName
case object dvlaService extends ServiceName
