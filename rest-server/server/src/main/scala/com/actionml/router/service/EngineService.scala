/*
 * Copyright ActionML, LLC under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * ActionML licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.actionml.router.service

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import com.actionml.admin.{Administrator, SystemInfo}
import com.actionml.core.HIO
import com.actionml.core.model.{Comment, Response}
import com.actionml.core.spark.SparkContextSupport.jsonComment
import com.actionml.core.utils.ZIOUtil.ValidatedImplicits._
import com.actionml.core.validate.{ValidRequestExecutionError, WrongParams}
import com.typesafe.scalalogging.LazyLogging
import zio.{Fiber, IO}

import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.util.control.NonFatal

/**
  *
  *
  * @author The ActionML Team (<a href="http://actionml.com">http://actionml.com</a>)
  * 28.01.17 14:49
  */

trait EngineService {
  def status(engineId: String): HIO[Response]
  def statuses(): HIO[List[Response]]
  def addEngine(engineJson: String): HIO[Response]
  def updateEngine(engineJson: String): HIO[Response]
  def train(engineId: String): HIO[Response]
  def importFromPath(engineId: String, importPath: String): HIO[Response]
  def deleteEngine(engineId: String): HIO[Response]
  def cancelJob(engineId: String, jobId: String): HIO[Response]
  def getUserData(engineId: String, userId: String, num: Int, from: Int): HIO[List[Response]]
  def deleteUserData(engineId: String, userId: String): HIO[Response]
}

class EngineServiceImpl(admin: Administrator) extends EngineService with LazyLogging {
  override def status(engineId: String): HIO[Response] = admin.status(engineId)
  override def statuses(): HIO[List[Response]] =  admin.statuses()
  override def addEngine(engineJson: String): HIO[Response] = admin.addEngine(engineJson)
  override def updateEngine(engineJson: String): HIO[Response] = admin.updateEngine(engineJson)
  override def train(engineId: String): HIO[Response] = admin.updateEngineWithTrain(engineId)
  override def importFromPath(engineId: String, importPath: String): HIO[Response] = admin.updateEngineWithImport(engineId, importPath)
  override def deleteEngine(engineId: String): HIO[Response] = admin.removeEngine(engineId)
  override def cancelJob(engineId: String, jobId: String): HIO[Response] = admin.cancelJob(engineId = engineId, jobId = jobId)
  /*
=======
trait EngineService extends ActorInjectable

class EngineServiceImpl(implicit inj: Injector) extends EngineService{

  private val admin = inject[Administrator]('Administrator)

  override def receive: Receive = {
    case GetSystemInfo() =>
      log.info("Get system info")
      sender() ! admin.systemInfo(ExecutionContext.Implicits.global)

    case GetEngine(engineId) =>
      log.info("Get engine, {}", engineId)
      sender() ! admin.status(engineId)

    case GetEngines =>
      log.info("Get one or all engine status")
      sender() ! admin.statuses()

    case CreateEngine(engineJson) =>
      log.info("Create new engine, {}", engineJson)
      sender() ! admin.addEngine(engineJson)

    case UpdateEngine(engineJson) =>
      log.info(s"Update existing engine, $engineJson")
      sender() ! admin.updateEngine(engineJson)

    case UpdateEngineWithTrain(engineId) =>
      log.info(s"Update existing engine, $engineId")
      sender() ! admin.updateEngineWithTrain(engineId)

    case UpdateEngineWithImport(engineId, inputPath) =>
      log.info(s"Update existing engine by importing, $inputPath")
      sender() ! admin.updateEngineWithImport(engineId, inputPath)

    case DeleteEngine(engineId) =>
      log.info("Delete existing engine, {}", engineId)
      sender() ! admin.removeEngine(engineId)

    case CancelJob(engineId, jobId) =>
      log.info(s"Cancel job $jobId for engine $engineId")
      sender() ! admin.cancelJob(engineId = engineId, jobId = jobId)

    case GetUserData(engineId, userId, num, from) =>
      admin.getEngine(engineId).fold {
        sender() ! Invalid(WrongParams(jsonComment(s"Non-existent engine-id: $engineId")))
      } { engine =>
        sender() ! (try {
          engine.getUserData(userId, num, from)
        } catch {
          case _: NotImplementedError => Invalid(NotImplemented)
          case NonFatal(_) => Invalid(ValidRequestExecutionError)
        })
      }

>>>>>>> feature/add-system-health-to-status
    case DeleteUserData(engineId, userId) =>
      admin.getEngine(engineId).fold {
        sender() ! Invalid(WrongParams(jsonComment(s"Non-existent engine-id: $engineId")))
      } { engine =>
        sender() ! (try {
          engine.deleteUserData(userId)
        } catch {
          case _: NotImplementedError => Invalid(NotImplemented)
          case NonFatal(_) => Invalid(ValidRequestExecutionError)
        })
      }
<<<<<<< HEAD
   */
  override def getUserData(engineId: String, userId: String, num: Int, from: Int): HIO[List[Response]] = {
    admin.getEngine(engineId).fold[HIO[List[Response]]](IO.fail(WrongParams(jsonComment(s"Non-existent engine-id: $engineId")))) { engine =>
      engine.getUserData(userId, num, from) match {
        case Valid(r) => IO.succeed(r)
        case Invalid(e) => IO.fail(e)
      }
    }
  }
  override def deleteUserData(engineId: String, userId: String): HIO[Response] = {
    admin.getEngine(engineId).fold[HIO[Response]](IO.fail(WrongParams(jsonComment(s"Non-existent engine-id: $engineId")))) { engine =>
      engine.deleteUserData(userId)
    }
  }
}


sealed trait EngineAction
case class GetSystemInfo() extends EngineAction
case class GetEngine(engineId: String) extends EngineAction
case object GetEngines extends EngineAction
case class CreateEngine(engineJson: String) extends EngineAction
case class UpdateEngine(engineJson: String) extends EngineAction
case class UpdateEngineWithTrain(engineId: String) extends EngineAction
case class UpdateEngineWithImport(engineId: String, inputPath: String) extends EngineAction
case class CancelJob(engineId: String, jobId: String) extends EngineAction
case class GetUserData(engineId: String, userId: String, num: Int, from: Int) extends EngineAction
case class DeleteUserData(engineId: String, userId: String) extends EngineAction

case class DeleteEngine(engineId: String) extends EngineAction
