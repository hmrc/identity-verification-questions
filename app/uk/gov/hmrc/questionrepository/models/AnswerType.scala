package uk.gov.hmrc.questionrepository.models

import play.api.libs.json.{Format, Json, Reads, __}


sealed trait AnswerType

case class STR() extends AnswerType
object STR {
  implicit val format: Format[STR] = Json.format[STR]
}

case class INT() extends AnswerType
object INT {
  implicit val format: Format[INT] = Json.format[INT]
}

case class DBL() extends AnswerType
object DBL {
  implicit val format: Format[DBL] = Json.format[DBL]
}

object AnswerType{
  implicit val answerTypeReads: Reads[AnswerType] =
    __.read[STR].map(n => n:AnswerType) orElse
      __.read[INT].map(n => n:AnswerType) orElse
      __.read[DBL].map(n => n:AnswerType)
}
