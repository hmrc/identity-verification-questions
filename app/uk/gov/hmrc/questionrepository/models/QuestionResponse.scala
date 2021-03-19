/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, Json}

case class QuestionResponse(correlationId: CorrelationId, questions: Seq[Question], questionTextEn: Map[String,String], questionTextCy: Option[Map[String,String]]) {

}

object QuestionResponse{
  implicit val format: Format[QuestionResponse] = Json.format[QuestionResponse]
}
