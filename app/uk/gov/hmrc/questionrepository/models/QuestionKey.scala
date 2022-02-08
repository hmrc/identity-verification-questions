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
    PaymentToDate,
    EmployeeNIContributions,
    PassportQuestion,
    DVLAQuestion
  )

  val mapping: Mapping[String, QuestionKey] = Mappings.mapOption[String, QuestionKey](fromString, _.name)
  implicit val format: Format[QuestionKey] = mapping.jsonFormat

  def fromString(keyName: String): Option[QuestionKey] = {
    keys.find(_.name == keyName)
  }
}
case object PaymentToDate extends QuestionKey("rti-p60-payment-for-year", "P60")
case object EmployeeNIContributions extends QuestionKey("rti-p60-employee-ni-contributions", "P60")
case object PassportQuestion extends QuestionKey("passport", "passport")
case object SCPEmailQuestion extends QuestionKey("rti-p60-payment-for-year", "P60")
case object DVLAQuestion extends QuestionKey("dvla", "dvla")


sealed trait ServiceName
case object p60Service extends ServiceName
case object passportService extends ServiceName
case object scpEmailService extends ServiceName
case object dvlaService extends ServiceName
