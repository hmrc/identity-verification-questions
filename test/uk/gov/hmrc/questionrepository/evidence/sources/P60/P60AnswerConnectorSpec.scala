/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.P60

import Utils.UnitSpec
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.questionrepository.evidences.sources.P60.P60AnswerConnector
import uk.gov.hmrc.questionrepository.models.P60._
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository

import scala.concurrent.ExecutionContext.Implicits.global


class P60AnswerConnectorSpec extends UnitSpec with BeforeAndAfterEach {

  val mongoRepo: QuestionMongoRepository = new QuestionMongoRepository(reactiveMongoComponent)
  val connector = new P60AnswerConnector(mongoRepo)

  val answerDetailsPaymentToDate: AnswerDetails = AnswerDetails(PaymentToDate, SimpleAnswer("100.11"))
  val answerDetailsEarningsAbovePT: AnswerDetails = AnswerDetails(EarningsAbovePT, SimpleAnswer("100.11"))

  override def afterEach(): Unit = {
    super.afterEach()
    await(mongoRepo.collection.drop().toFuture())
  }

  "verifyAnswer" should {
    "return score of 'Correct'" when {
      "answer matches an answer retrieved from repo PaymentToDate" in {
        val correctQDC: QuestionDataCache = QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(PaymentToDate, Seq("200.22", "100.11"))), dateTime)

        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, Selection(ninoIdentifier, saUtrIdentifier), answerDetailsPaymentToDate).futureValue shouldBe QuestionResult(PaymentToDate, Correct)
      }
      "answer matches an answer retrieved from repo EarningsAbovePT" in {
        val correctQDC: QuestionDataCache =
          QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(EarningsAbovePT, Seq("200.22", "100.11"))), dateTime)

        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, Selection(ninoIdentifier, saUtrIdentifier), answerDetailsEarningsAbovePT).futureValue shouldBe QuestionResult(EarningsAbovePT, Correct)
      }
      "answer matches an answer retrieved from repo EarningsAbovePT with tolerance" in {
        val correctQDC: QuestionDataCache =
          QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(EarningsAbovePT, Seq("200.22", "100.11"))), dateTime)

        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, Selection(ninoIdentifier, saUtrIdentifier), answerDetailsEarningsAbovePT.copy(answer = SimpleAnswer("101.11"))).futureValue shouldBe QuestionResult(EarningsAbovePT, Correct)
      }
    }

    "return score of 'Incorrect'" when {
      "answer does not match an answer retrieved from repo" in {
        val inCorrectQDC: QuestionDataCache = QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(PaymentToDate, Seq("200.22", "300.33"))), dateTime)

        await(mongoRepo.store(inCorrectQDC))
        connector.verifyAnswer(corrId, Selection(ninoIdentifier, saUtrIdentifier), answerDetailsPaymentToDate).futureValue shouldBe QuestionResult(PaymentToDate, Incorrect)
      }
      "answer matches an answer retrieved from repo EarningsAbovePT over tolerance" in {
        val correctQDC: QuestionDataCache =
          QuestionDataCache(corrId, Selection(ninoIdentifier, saUtrIdentifier), Seq(QuestionWithAnswers(EarningsAbovePT, Seq("200.22", "100.11"))), dateTime)

        await(mongoRepo.store(correctQDC))
        connector.verifyAnswer(corrId, Selection(ninoIdentifier, saUtrIdentifier), answerDetailsEarningsAbovePT.copy(answer = SimpleAnswer("102.11"))).futureValue shouldBe QuestionResult(EarningsAbovePT, Incorrect)
      }
    }

    "return score of 'Unknown'" when {
      "no answers retrieved from repo" in {
        connector.verifyAnswer(corrId, Selection(ninoIdentifier, saUtrIdentifier), answerDetailsPaymentToDate).futureValue shouldBe QuestionResult(PaymentToDate, Unknown)
      }
    }
  }
}
