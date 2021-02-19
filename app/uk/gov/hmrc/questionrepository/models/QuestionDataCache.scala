/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, Json}

case class QuestionDataCache(selection: Selection, questions: Seq[Question])

object QuestionDataCache{
  implicit val format: Format[QuestionDataCache] = Json.format[QuestionDataCache]
}
