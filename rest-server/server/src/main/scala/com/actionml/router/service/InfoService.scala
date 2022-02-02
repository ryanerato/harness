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

import cats.data.Validated.{Invalid, Valid}
import com.actionml.admin.Administrator
import com.actionml.core.HIO
import com.actionml.core.engine.EnginesBackend
import com.actionml.core.model.Response
import com.actionml.core.validate.{JsonSupport, ValidRequestExecutionError}
import zio.{IO, ZIO}

class InfoService(admin: Administrator) extends JsonSupport {
  def getSystemInfo: HIO[Response] =
    ZIO.fromFuture(admin.systemInfo(_))
      .flatMap {
        case Valid(a) => IO.succeed(a)
        case Invalid(e) => IO.fail(e)
      }.mapError(_ => ValidRequestExecutionError())

  def getClusterInfo: HIO[List[Response]] = {
    EnginesBackend.listNodes
  }
}