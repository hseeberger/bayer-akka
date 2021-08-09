/*
 * Copyright 2021 Heiko Seeberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rocks.heikoseeberger.bayer

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.actor.{ ActorSystem as ClassicSystem, CoordinatedShutdown }
import akka.actor.typed.{ ActorSystem, Behavior }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{ Directives, Route }
import scala.concurrent.duration.DurationInt

@main def bayer(): Unit =
  val httpAddress   = sys.env.getOrElse("HTTP_ADDRESS", "::1")
  val httpPort      = sys.env.getOrElse("HTTP_PORT", "8080").toShort
  val classicSystem = ClassicSystem("bayer")
  classicSystem.spawn(Main(httpAddress, httpPort), "main")

object Main:

  sealed trait Command

  private case object BindFailure extends CoordinatedShutdown.Reason

  def apply(httpAddress: String, httpPort: Short): Behavior[Command] =
    Behaviors.setup { context =>
      given ActorSystem[?] = context.system
      runHttpServer(httpAddress, httpPort)
      Behaviors.empty
    }

  def runHttpServer(httpAddress: String, httpPort: Short)(using system: ActorSystem[?]): Unit =
    import system.executionContext
    Http()
      .newServerAt(httpAddress, httpPort)
      .bind(route)
      .failed
      .foreach(_ => CoordinatedShutdown(system).run(BindFailure))

  def route: Route =
    import Directives.*
    pathSingleSlash {
      get {
        complete {
          "Habe die Ehre!"
        }
      }
    }
