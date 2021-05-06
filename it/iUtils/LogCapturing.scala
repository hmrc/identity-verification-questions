/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package iUtils

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Level, Logger => LogbackLogger}
import ch.qos.logback.core.read.ListAppender
import play.api.{Logger, LoggerLike}

import scala.collection.JavaConverters._
import scala.reflect.ClassTag

trait LogCapturing {

  def withCaptureOfLoggingFrom[T](body: (=> List[ILoggingEvent]) => Unit)(implicit classTag: ClassTag[T]): Unit = {
    withCaptureOfLoggingFrom(List(Logger(classTag.runtimeClass)))(body)
  }

  def withCaptureOfLoggingFrom[T, U](
                                      body: (=> List[ILoggingEvent]) => Unit
                                    )(implicit classTagT: ClassTag[T], classTagU: ClassTag[U]): Unit = {
    withCaptureOfLoggingFrom(List(Logger(classTagT.runtimeClass), Logger(classTagU.runtimeClass)))(body)
  }

  private def withCaptureOfLoggingFrom(loggers: List[LoggerLike])(body: (=> List[ILoggingEvent]) => Unit): Unit = {
    val appender = new ListAppender[ILoggingEvent]()

    loggers.foreach { logger =>
      val underlying = logger.logger.asInstanceOf[LogbackLogger]
      appender.setContext(underlying.getLoggerContext)
      underlying.addAppender(appender)
      underlying.setLevel(Level.ALL)
      underlying.setAdditive(true)
    }

    appender.start()
    body(appender.list.asScala.toList)
  }
}

