/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services.Utilities

import Utils.UnitSpec
import Utils.testData.AppConfigTestData
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.models.{ServiceName, p60Service}
import uk.gov.hmrc.questionrepository.services.utilities.{TaxYear, TaxYearBuilder}

import java.time.LocalDate

class TaxYearSpec extends UnitSpec {

  "working tax year out" should {

    "give the correct format" in new Setup {
      _14_15.display shouldBe "2014/15"
    }

    "startand end correctly" in new Setup {
      _14_15.startYear shouldBe 2014
      _14_15.finishYear shouldBe 2015
    }

    "traverse functions return correct year after traverse function" in new Setup {
      _14_15.previous.startYear shouldBe 2013
      _14_15.next.startYear shouldBe 2015
      _14_15.back(2).startYear shouldBe 2012
      _14_15.forwards(2).startYear shouldBe 2016
      _14_15.compare(_15_16) shouldBe -1

    }

    "return the correct current year" in new Setup {
      override def testConfig: Map[String, Any] = baseConfig
      new TaxYearTest(2015, 1, 1).currentTaxYear shouldBe _14_15
      new TaxYearTest(2015, 4, 5).currentTaxYear shouldBe _14_15
      new TaxYearTest(2015, 4, 6).currentTaxYear shouldBe _15_16
      new TaxYearTest(2015, 11, 1).currentTaxYear shouldBe _15_16
    }

    "return the correct buffered previous year with taxYearStartingMonthBuffer = 2" in new Setup {
      override def testConfig: Map[String, Any] = baseConfig
      new TaxYearTest(2015, 2, 1).currentTaxYearWithBuffer(2) shouldBe _14_15
      new TaxYearTest(2015, 4, 6).currentTaxYearWithBuffer(2) shouldBe _14_15
      new TaxYearTest(2015, 6, 5).currentTaxYearWithBuffer(2) shouldBe _14_15
      new TaxYearTest(2015, 6, 6).currentTaxYearWithBuffer(2) shouldBe  _15_16
      new TaxYearTest(2015, 9, 1).currentTaxYearWithBuffer(2) shouldBe _15_16
    }

    "return the correct buffered previous year when MonthBuffer of 2 returned from appConfig" in new Setup {
      override def testConfig: Map[String, Any] = baseConfig ++ bufferInMonthsForService
      new TaxYearTest(2015, 2, 1).currentTaxYearWithBuffer shouldBe _14_15
      new TaxYearTest(2015, 4, 6).currentTaxYearWithBuffer shouldBe _14_15
      new TaxYearTest(2015, 6, 5).currentTaxYearWithBuffer shouldBe _14_15
      new TaxYearTest(2015, 6, 6).currentTaxYearWithBuffer shouldBe  _15_16
      new TaxYearTest(2015, 9, 1).currentTaxYearWithBuffer shouldBe _15_16
    }

    "return the correct result with taxYearStartingMonthBuffer= 1" in new Setup {
      override def testConfig: Map[String, Any] = baseConfig
      new TaxYearTest(2015, 5, 10).currentTaxYearWithBuffer(1) shouldBe _15_16
    }

    "return today's date when today is called" in new Setup {
      override def testConfig: Map[String, Any] = baseConfig

      class TaxYearToday extends TaxYearBuilder {
        override implicit val appConfig: AppConfig = mockAppConfig
        override def serviceName: ServiceName = p60Service
      }

      new TaxYearToday().today shouldBe LocalDate.now
    }
  }

  trait Setup extends TestData {
    def testConfig: Map[String, Any] = Map.empty
    val config: Configuration = Configuration.from(testConfig)
    lazy val servicesConfig = new ServicesConfig(config)
    lazy val mockAppConfig = new AppConfig(config, servicesConfig)

    class TaxYearTest(y: Int, m: Int, d: Int) extends TaxYearBuilder {
      override def today = LocalDate.of(y, m, d)
      override implicit val appConfig: AppConfig = mockAppConfig

      override def serviceName: ServiceName = p60Service
    }
  }

  trait TestData extends AppConfigTestData {
    val _13_14 = TaxYear(2013)
    val _14_15 = TaxYear(2014)
    val _15_16 = TaxYear(2015)

    val bufferInMonthsForService: Map[String, Any] = Map("microservice.services.p60Service.bufferInMonths" -> 2)
  }
}
