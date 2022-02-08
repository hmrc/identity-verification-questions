/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.questionrepository.config

import play.api.{Configuration, Logging}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME
import java.time.format.DateTimeParseException
import javax.inject.Inject

class ConfigHelper @Inject()(config: Configuration)
  extends Logging {
  protected def getIntOrDefault(key: String, default: => Int): Int = config.getOptional[Int](key).getOrElse(default)

  protected def getStringOrDefault(key: String, default: => String): String = config.getOptional[String](key).getOrElse(default)

  protected def getDateTime(key: String) : Either[DateParsingIssue, LocalDateTime] = config.getOptional[String](key) match {
    case Some(possibleDate) => try {
      Right(LocalDateTime.parse(possibleDate, ISO_LOCAL_DATE_TIME))
    } catch {
      case _: DateTimeParseException => Left(InvalidDateFound(key, possibleDate))
    }
    case None => Left(DateMissing)
  }

  protected def getStringList(key: String): Option[Seq[String]] = config.getOptional[Seq[String]](key)

  protected def scheduledOutage(serviceName: String): Option[Outage] = {
    (getDateTime(s"microservice.services.$serviceName.disabled.start"), getDateTime(s"microservice.services.$serviceName.disabled.end")) match {
      case (Right(startDate), Right(endDate)) if startDate.isBefore(endDate) =>
        logger.info(s"Scheduled $serviceName outage between $startDate and $endDate")
        Some(Outage(startDate, endDate))
      case (Right(startDate), Right(endDate)) =>
        logger.info(s"Scheduled $serviceName outage startDate: $startDate must be earlier than endDate: $endDate")
        None
      case (Left(DateMissing), Right(_)) =>
        logger.info(s"Scheduled $serviceName outage $serviceName.disabled.start missing")
        None
      case (Left(InvalidDateFound(key, date)), Right(_)) =>
        logger.info(s"Scheduled $serviceName outage Invalid date in `$key` : `$date`")
        None
      case (Right(_), Left(DateMissing)) =>
        logger.info(s"Scheduled $serviceName outage $serviceName.disabled.end missing")
        None
      case (Right(_), Left(InvalidDateFound(key, date))) =>
        logger.info(s"Scheduled $serviceName outage Invalid date in `$key` : `$date`")
        None
      case _ =>
        logger.info(s"Scheduled $serviceName outage not specified")
        None
    }
  }

  protected def getDisabledOrigins(serviceName: String): List[String] = {
    val key = s"microservice.services.$serviceName.disabled.origin"
    getStringList(key) match {
      case Some(origins) =>
        logger.info(s"Disabled origins for $serviceName are [${origins.mkString(", ")}]")
        origins.toList
      case None =>
        logger.info(s"Disabled origins for $serviceName not specified")
        List.empty[String]
    }
  }

  protected def getEnabledOrigins(serviceName: String): List[String] = {
    val key = s"microservice.services.$serviceName.enabled.origin"
    getStringList(key) match {
      case Some(origins) =>
        logger.info(s"Enabled origins for $serviceName are [${origins.mkString(", ")}]")
        origins.toList
      case None =>
        logger.info(s"Enabled origins for $serviceName not specified")
        List.empty[String]
    }
  }

  protected def getRequiredIdentifiers(serviceName: String): List[String] = {
    val key = s"microservice.services.$serviceName.identifier.required"
    getStringList(key) match {
      case Some(identifiers) =>
        logger.info(s"Required identifiers for $serviceName are [${identifiers.mkString(", ")}]")
        identifiers.toList
      case None =>
        logger.info(s"Required identifiers for $serviceName not specified")
        List.empty[String]
    }
  }

  case class ServiceState(outage: Option[Outage],
                          disabledOrigins: List[String],
                          enabledOrigins: List[String],
                          requiredIdentifiers: List[String])

  object ServiceState {

    def apply(serviceName: String): ServiceState = apply(scheduledOutage(serviceName),
                                                        getDisabledOrigins(serviceName),
                                                        getEnabledOrigins(serviceName),
                                                        getRequiredIdentifiers(serviceName))
  }

  protected def getHodConfItem(serviceName: String): Either[HodConfigMissing, HodConf] = {
    val authToken = config.getOptional[String](s"microservice.services.$serviceName.hod.authorizationToken")
    val envHeader = config.getOptional[String](s"microservice.services.$serviceName.hod.environmentHeader")
    (authToken, envHeader) match {
      case (Some(a), Some(e)) => Right(HodConf(a, e))
      case (None, Some(_)) => Left(MissingAuthorizationToken)
      case (Some(_), None) => Left(MissingEnvironmentHeader)
      case _ => Left(MissingAllConfig)
    }
  }

}

sealed trait DateParsingIssue
case class InvalidDateFound(key: String, badDate : String) extends DateParsingIssue
case object DateMissing extends DateParsingIssue

case class Outage(startDate : LocalDateTime, endDate : LocalDateTime)

case class HodConf(authorizationToken: String, environmentHeader: String)
sealed trait HodConfigMissing
case object MissingAuthorizationToken extends HodConfigMissing
case object MissingEnvironmentHeader extends HodConfigMissing
case object MissingAllConfig extends HodConfigMissing