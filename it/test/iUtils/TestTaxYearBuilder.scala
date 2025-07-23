/*
 * Copyright 2025 HM Revenue & Customs
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

package iUtils

import uk.gov.hmrc.identityverificationquestions.services.utilities.TaxYear

import java.time.LocalDate

trait TestTaxYearBuilder {

  val startingDayForTaxYear = 6
  val startingMonthForTaxYear = 4

  def today: LocalDate = LocalDate.now

  def currentYear = today.getYear

  def startOfTheTaxYear = LocalDate.of(currentYear, startingMonthForTaxYear, startingDayForTaxYear)

  /**
   *
   * @param bufferInMonths the number of months after the start of current tax year while we still look at the old one
   * @return the current tax year - deferred a bit.
   */
  def currentTaxYearWithBuffer(bufferInMonths: Int) =
    currentTaxYear(startOfTheTaxYear.plusMonths(bufferInMonths))

  private def currentTaxYear(logicalStartOfYear: LocalDate): TaxYear = TaxYear(
    if (today isBefore logicalStartOfYear) currentYear - 1
    else currentYear
  )

  def currentTaxYear: TaxYear = currentTaxYear(startOfTheTaxYear)
}
