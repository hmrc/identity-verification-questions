package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, Json}

case class ReturningJson(quid:String, questionEn:String, questionCy: Option[String], answerType: Option[AnswerType], regex: Option[String]) {

}

object ReturningJson{
  implicit val format: Format[ReturningJson] = Json.format[ReturningJson]
}