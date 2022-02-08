/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services.utilities

import scala.math.BigDecimal.RoundingMode

trait PenceAnswerConvertor {
  val pound = BigDecimal("1.00")
  protected def convertAnswer(answer: BigDecimal): BigDecimal = roundDown(answer)
  protected def convertAnswer(answer: String): BigDecimal = roundDown(BigDecimal(answer))

  def roundDown(value: BigDecimal): BigDecimal =
    if (value > pound) {
      value.setScale(0, RoundingMode.DOWN).setScale(2)
    } else {
      value
    }
}
