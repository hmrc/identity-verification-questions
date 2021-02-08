/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.models

case class Question(questionKey: String, answers: Seq[String], info: Map[String, String] = Map.empty)
