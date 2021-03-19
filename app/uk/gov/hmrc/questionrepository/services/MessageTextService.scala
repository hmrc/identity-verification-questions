/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services

import play.api.i18n.Lang
import uk.gov.hmrc.questionrepository.models.QuestionKey
import javax.inject.Singleton
import scala.io.Source

@Singleton
class MessageTextService {

  private def getMessageMap(lang: Lang): Map[String, String] =
    Source.fromResource(s"question-text.${lang.code}").getLines().flatMap(_.split("=") match {
      case Array(s1, s2) => Map(s1.trim -> s2.trim)
      case _ => Map.empty[String, String]
    } ).toMap

  private lazy val questionMessagesEn: Map[String, String] = getMessageMap(Lang("en"))

  private lazy val questionMessagesCy: Map[String, String] = getMessageMap(Lang("cy"))

  def getQuestionMessageEn(questionKey: QuestionKey): Map[String, String] =
    questionMessagesEn.filter(_._1.contains(questionKey.toString))

  def getQuestionMessageCy(questionKey: QuestionKey): Map[String, String] =
    questionMessagesCy.filter(_._1.contains(questionKey.toString))
}
