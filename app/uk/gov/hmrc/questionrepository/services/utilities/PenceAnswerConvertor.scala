/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services.utilities

import scala.math.BigDecimal.RoundingMode


trait PenceAnswerConvertor {
  val pound = BigDecimal("1.00")
  protected def convertAnswer(answer: BigDecimal): BigDecimal = roundDownIgnorePence(answer)
  protected def convertAnswer(answer: String): BigDecimal = roundDownWithPence(BigDecimal(answer))

  //the method will add .00 to int, eg 100 convert to 100.00
  //the method also ignore pence, eg, 100.38 convert to 100.00
  def roundDownIgnorePence(value: BigDecimal): BigDecimal = {
    if (value > pound) {
      value.setScale(0, RoundingMode.DOWN).setScale(2)
    } else {
      value
    }
  }

  //the method will add .00 to int, eg 100 convert to 100.00
  //the method will not ignore pence, eg, 100.38 will stay as 100.38
  def roundDownWithPence(value: BigDecimal): BigDecimal = {
    if (value > pound) {
      value.setScale(2, RoundingMode.DOWN).setScale(2)
    } else {
      value
    }
  }
  
}
