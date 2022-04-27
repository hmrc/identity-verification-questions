/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

import javax.inject.Inject
import org.joda.time.DateTime
import play.api.mvc.Request
import uk.gov.hmrc.circuitbreaker.CircuitBreakerConfig
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.connectors.QuestionConnector
import uk.gov.hmrc.questionrepository.evidences.sources.QuestionServiceMeoMinimumNumberOfQuestions
import uk.gov.hmrc.questionrepository.models.SelfAssessment.SelfAssessedIncomeFromPensionsQuestion
import uk.gov.hmrc.questionrepository.models.{Question, QuestionKey, Selection, selfAssessmentService}
import uk.gov.hmrc.questionrepository.monitoring.EventDispatcher
import uk.gov.hmrc.questionrepository.monitoring.auditing.AuditService

import scala.concurrent.{ExecutionContext, Future}

class SAPensionService @Inject() (
    val appConfig: AppConfig,
    connector : SAPensionsConnector,
    val eventDispatcher: EventDispatcher,
    override implicit val auditService: AuditService) extends QuestionServiceMeoMinimumNumberOfQuestions
//  with CircuitBreakerConfig
{

  type Record = SelfAssessmentReturn

  def currentDate: DateTime = DateTime.now()

  private lazy val switchOverDay : Int = appConfig.saYearSwitchDay
  private lazy val switchOverMonth : Int = appConfig.saYearSwitchMonth
  private val currentYearKey = "currentTaxYear"
  private val previousYearKey = "previousTaxYear"

  override def serviceName = selfAssessmentService

  private[sa] def determinePeriod = {
    val switchDate = DateTime.parse(s"${currentDate.getYear}-$switchOverMonth-$switchOverDay")
    if (currentDate.isBefore(switchDate)) {
      (currentDate.getYear - 3, currentDate.getYear - 2)
    } else {
      (currentDate.getYear - 2, currentDate.getYear - 1)
    }
  }

   def getRecords(selection: Selection)(implicit request: Request[_], hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[SAReturn]] = {
      val (startYear, endYear) = determinePeriod
      connector.getReturns(selection.nino.get, startYear, endYear)
  }

//  val questionHandlers: Seq[QuestionHandler[Record]]  = Seq(
//    new PenceQuestionHandler[SelfAssessmentReturn] {
//      override def key: QuestionKey = SelfAssessedIncomeFromPensionsQuestion
//
//      override def validateAnswer(validAnswers: Seq[String], answer: String, selection: Selection)(implicit ec: ExecutionContext, appConfig: AppConfig): Future[AnswerCorrectness] = {
//        val answers = validAnswers.map(convertAnswer).map(_.toBigInt)
//        val intAnswer = convertAnswer(answer).toBigInt
//        val offset = appConfig.saAnswerOffset
//        val result = if (answers.exists(a => a - offset <= intAnswer && a + offset >= intAnswer)) Match else NoMatch(answers.map(_.toString))
//        Future.successful(result)
//      }
//
//      private def returnsToAdditionalInfo(returns: Seq[SAReturn]): Map[String, String] = {
//        val yearToRecords: Map[Int, Seq[SARecord]] = returns.map(sar => sar.taxYear.startYear -> sar.returns).toMap
//        val yearsWithSomeNotZero: Set[String] = yearToRecords.collect { case (year, records) if records.exists(_.incomeFromPensions > 0) => year.toString }.toSet
//        additionalInfo.filter { case (_, year) => yearsWithSomeNotZero(year) }
//      }
//
//      override def customAdditionalInfo(returns: Seq[SelfAssessmentReturn]): Map[String, String]= {
//        if (returns.isEmpty) additionalInfo
//        else {
//              // it will always be an SAReturn
//            returnsToAdditionalInfo(returns.asInstanceOf[Seq[SAReturn]])
//        }
//      }
//
//      override protected def additionalInfo: Map[String, String] = {
//        val (previousYear, currentYear) = determinePeriod
//        Map(
//          currentYearKey -> currentYear.toString,
//          previousYearKey -> previousYear.toString
//        )
//      }
//
//      override protected def correctAnswers(record: SelfAssessmentReturn): Seq[String] = {
//        record match {
//          case pension: SAReturn =>
//            pension.returns.collect {
//              case value if value.incomeFromPensions > 0 => value.incomeFromPensions.toString()
//            }
//          case _ => Seq()
//        }
//      }
//    }
//  )
  override def connector: QuestionConnector[SelfAssessmentReturn] = ???

  override def isAvailable(selection: Selection): Boolean = ???

  override def evidenceTransformer(records: Seq[SelfAssessmentReturn]): Seq[Question] = ???

  override protected def circuitBreakerConfig: CircuitBreakerConfig = ???
}
