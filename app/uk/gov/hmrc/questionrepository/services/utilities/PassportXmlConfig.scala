/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.services.utilities

import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.questionrepository.config.AppConfig
import uk.gov.hmrc.questionrepository.models.passport.PassportRequest
import uk.gov.hmrc.questionrepository.models.{Correct, Error, Incorrect, Score}

import java.util.UUID
import javax.xml.parsers.SAXParserFactory
import scala.xml.{Elem, Utility, XML}

trait PassportXmlConfig {

  protected val secureSAXParser = {
    val saxParserFactory = SAXParserFactory.newInstance()
    saxParserFactory.setFeature("http://xml.org/sax/features/external-general-entities", false)
    saxParserFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
    saxParserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    saxParserFactory.newSAXParser()
  }

  protected val PassportHeaders = Seq("SOAPAction" -> "http://dva.hmpo.gov.uk/passport-data-service/validateData",
    "Content-Type" -> "application/soap+xml; charset=utf-8")

   protected def createPassportXml(passportRequest: PassportRequest)(implicit appConfig: AppConfig, hc: HeaderCarrier): String = {

    val reqId = hc.requestId.fold(UUID.randomUUID().toString)(_.value.replace("govuk-tax-", "")) // max 40 chars

    s"""<?xml version="1.0" encoding="utf-8"?>
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:pas="http://dva.hmpo.gov.uk/passport-data-service">
      <soapenv:Header>
        <authenticationData>
          <organisationId>${appConfig.passportAuthData.organisationId}</organisationId>
          <organisationApplicationId>${appConfig.passportAuthData.organisationApplicationId}</organisationApplicationId>
          <organisationRequestId>$reqId</organisationRequestId>
          <organisationUserName>${appConfig.passportAuthData.organisationUserName}</organisationUserName>
          <organisationUserPassword>${appConfig.passportAuthData.organisationUserPassword}</organisationUserPassword>
        </authenticationData>
      </soapenv:Header>
      <soapenv:Body>
        <pas:validateData>
          <validateDataRequest>
            <passportNumber>${passportRequest.passport.passportNumber}</passportNumber>
            <surname>${Utility.escape(passportRequest.passport.surname)}</surname>
            <forenames>${Utility.escape(passportRequest.passport.forenames)}</forenames>
            <dateOfBirth>${passportRequest.dateOfBirth}</dateOfBirth>
            <dateOfExpiry>${passportRequest.passport.dateOfExpiry.toString}</dateOfExpiry>
          </validateDataRequest>
        </pas:validateData>
      </soapenv:Body>
    </soapenv:Envelope>""".stripMargin
  }

  protected def checkResponse(response: HttpResponse): Score = {
    def textToXml(text: String) = XML.withSAXParser(secureSAXParser).loadString(text.trim)

    def isOnStopList(validateDataResponseXML: Elem): Boolean = {

      val biomisMatched = validateDataResponseXML \ "bioDataMismatch"
      val passportNotFound = validateDataResponseXML \ "passportNotFound"
      val passportCancelled = validateDataResponseXML \ "passportCancelled"

      val matches = validateDataResponseXML \ "matches"
      val isOnStopList = matches \ "stopMatch"

      if (biomisMatched.nonEmpty && !biomisMatched.text.toBoolean &&
        passportNotFound.nonEmpty && !passportNotFound.text.toBoolean &&
        passportCancelled.nonEmpty && !passportCancelled.text.toBoolean &&
        matches.nonEmpty && isOnStopList.nonEmpty && isOnStopList.text.toBoolean) {
        true
      } else false
    }

    val xml = textToXml(response.body.trim)

    (xml \\ "Fault", xml \\ "validateDataResponse") match {
      case (fault, _) if fault.length > 0 => Error((fault \\ "faultstring").text)
      case (_, result) if result.length == 0 => Error(s"Unexpected response \n:${response.body}")

      case (_, result) =>
        val innerResult = result \ "validateDataResponse"
        val validateDataResponseXML = textToXml(innerResult.text)
        val validationResult = validateDataResponseXML \ "validationResult"
        validationResult.text match {
          case "Success" => Correct
          case "Failure" if isOnStopList(validateDataResponseXML) =>
            //            sendCustomerOnStopListAuditEvent()
            Correct
          case _ => Incorrect
        }
    }
  }
}
