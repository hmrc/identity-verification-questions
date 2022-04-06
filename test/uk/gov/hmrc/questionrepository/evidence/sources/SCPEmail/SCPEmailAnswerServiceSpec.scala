/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidence.sources.SCPEmail

import Utils.UnitSpec
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import uk.gov.hmrc.questionrepository.evidences.sources.SCPEmail.SCPEmailAnswerConnector
import uk.gov.hmrc.questionrepository.models._
import uk.gov.hmrc.questionrepository.repository.QuestionMongoRepository

import scala.concurrent.ExecutionContext.Implicits.global

class SCPEmailAnswerServiceSpec extends UnitSpec with BeforeAndAfterEach {

  val questionRepo = new QuestionMongoRepository(reactiveMongoComponent)
  val connector = new SCPEmailAnswerConnector(questionRepo)

  val answerDetails: AnswerDetails = AnswerDetails(SCPEmailQuestion, StringAnswer("email@email.com"))

  override def afterEach(): Unit = {
    super.afterEach()
    await(questionRepo.collection.drop().toFuture())
  }

  "verifyAnswer" should {
    // ver-1281: not in use for now
//    "return score of 'Correct'" when {
//      "answer matches an answer retrieved from repo" in new Setup {
//        val correctQDC: QuestionDataCache = QuestionDataCache(corrId, Selection(origin, Seq(ninoIdentifier, saUtrIdentifier)), Seq(Question(SCPEmailQuestion, Seq("email@email.com"))), dateTime)
//
//        await(questionRepo.insert(correctQDC))
//        connector.verifyAnswer(corrId, origin, Seq(ninoIdentifier, saUtrIdentifier), answerDetails).futureValue shouldBe QuestionResult(SCPEmailQuestion, Correct)
//      }
//    }

    "return score of 'Incorrect'" when {
      "answer does not match an answer retrieved from repo" in new Setup {
        val inCorrectQDC: QuestionDataCache = QuestionDataCache(corrId, Selection(origin, Seq(ninoIdentifier, saUtrIdentifier)), Seq(Question(SCPEmailQuestion, Seq("bad-email@bad-email.com"))), dateTime)

        await(questionRepo.collection.insertOne(inCorrectQDC).toFuture())
        connector.verifyAnswer(corrId, origin, Seq(ninoIdentifier, saUtrIdentifier), answerDetails).futureValue shouldBe QuestionResult(SCPEmailQuestion, Incorrect)
      }
    }

    "return score of 'Unknown'" when {
      "no answers retrieved from repo" in new Setup {
        connector.verifyAnswer(corrId, origin, Seq(ninoIdentifier, saUtrIdentifier), answerDetails).futureValue shouldBe QuestionResult(SCPEmailQuestion, Unknown)
      }
    }
  }
}
