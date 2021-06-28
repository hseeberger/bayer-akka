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

import akka.actor.{ CoordinatedShutdown, ActorSystem => ClassicSystem }
import akka.actor.CoordinatedShutdown.PhaseBeforeServiceUnbind
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior }
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.ConfigFactory
import org.apache.logging.log4j.core.async.AsyncLoggerContextSelector
import scala.concurrent.ExecutionContext
import scala.jdk.DurationConverters.given

/**
  * Runner for bayer. Creates the actor system with the [[Main]] actor.
  */
@main def bayer(): Unit =
  val asyncLoggerName = classOf[AsyncLoggerContextSelector].getName
  if !sys.props.get("log4j2.contextSelector").contains(asyncLoggerName) then
    println(s"WARNING: system property log4j2.contextSelector not set to [$asyncLoggerName]!")

  val config        = Main.Config.load()
  val classicSystem = ClassicSystem(Main.Name)
  val mgmt          = AkkaManagement(classicSystem)
  val shutdown      = CoordinatedShutdown(classicSystem)

  given ExecutionContext = classicSystem.dispatcher
  mgmt
    .start()
    .foreach(_ => shutdown.addTask(PhaseBeforeServiceUnbind, "stop-akka-mgmt")(() => mgmt.stop()))
  classicSystem.spawn(Main(config), "main")

/**
  * Main actor, empty behavior. Initializes all components, e.g. the [[HttpServer]].
  */
object Main:

  sealed trait Command

  object Config:
    def load(): Config =
      val bayer      = ConfigFactory.load().getConfig(Name)
      val httpServer = bayer.getConfig("http-server")
      Config(
        HttpServer.Config(
          httpServer.getString("interface"),
          httpServer.getInt("port"),
          httpServer.getDuration("termination-deadline").toScala
        )
      )

  final case class Config(httpServer: HttpServer.Config)

  inline val Name = "bayer"

  def apply(config: Config): Behavior[Command] =
    Behaviors.setup { context =>
      given ActorSystem[_] = context.system
      HttpServer.run(config.httpServer)
      Behaviors.empty
    }
