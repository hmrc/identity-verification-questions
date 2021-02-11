/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services.utilities

import uk.gov.hmrc.questionrepository.config.AppConfig

import java.time.LocalDate

case class TaxYear(startYear: Int) extends Ordered[TaxYear] {

  def finishYear = startYear + 1

  def display = startYear + "/" + (finishYear % 100)

  def previous = back(1)

  def back(years: Int) = TaxYear(startYear - years)

  def next = forwards(1)

  def forwards(years: Int) = TaxYear(startYear + years)

  override def compare(that: TaxYear): Int = startYear.compare(that.startYear)
}

trait TaxYearBuilder {

  implicit val appConfig: AppConfig

  def serviceName: String

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

  def currentTaxYearWithBuffer =
    currentTaxYear(startOfTheTaxYear.plusMonths(appConfig.bufferInMonthsForService(serviceName)))

  private def currentTaxYear(logicalStartOfYear: LocalDate): TaxYear = TaxYear(
    if (today isBefore logicalStartOfYear) currentYear - 1
    else currentYear
  )

  def currentTaxYear: TaxYear = currentTaxYear(startOfTheTaxYear)
}

