/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.identityverificationquestions.services.utilities

import scala.math.BigDecimal.RoundingMode


trait PenceAnswerConvertor {

  val pound = BigDecimal("1.00")
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
