/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.evidences.sources.sa

case class SAPaymentReturn(payments: Vector[SAPayment]) extends SelfAssessmentReturn
