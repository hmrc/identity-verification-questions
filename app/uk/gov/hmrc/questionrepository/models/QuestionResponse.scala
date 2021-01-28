/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, Json}

case class QuestionResponse(questionId:QuestionId, questionEn:String, questionCy: Option[String] = None, answerType: Option[AnswerType] = None, regex: Option[String] = None) {

}

object QuestionResponse{
  implicit val format: Format[QuestionResponse] = Json.format[QuestionResponse]
}
