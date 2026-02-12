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

case class TaxYear(startYear: Int) extends Ordered[TaxYear] {

  def finishYear: Int = startYear + 1

  def display: String = s"$startYear/${finishYear % 100}"

  def previous: TaxYear = back(1)

  def back(years: Int) = TaxYear(startYear - years)

  def next: TaxYear = forwards(1)

  def forwards(years: Int) = TaxYear(startYear + years)

  def yearForUrl: String = {
    def takeYY(value: Int) = value % 100
    s"${takeYY(startYear)}-${takeYY(finishYear)}"
  }

  override def compare(that: TaxYear): Int = startYear.compare(that.startYear)
}
