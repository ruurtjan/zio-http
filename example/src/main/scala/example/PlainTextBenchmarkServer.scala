package example

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.util.AsciiString
import zhttp.http._
import zhttp.service.server.ServerChannelFactory
import zhttp.service.{EventLoopGroup, Server}
import zio.{App, ExitCode, URIO}

case class Message(message: String)

/**
 * This server is used to run plaintext benchmarks on CI.
 */
object Main extends App {
  val message: String            = "Hello, World!"
  private val STATIC_SERVER_NAME = AsciiString.cached("zio-http")

  // Create HTTP route
  val app: HttpApp[Any, Nothing] =
    Http.response(Response.text(message).withServerTime.memoize.addHeader(HttpHeaderNames.SERVER, STATIC_SERVER_NAME))
  val server                     = Server.app(app) ++
    Server.port(8080) ++
    Server.keepAlive ++
    Server.disableLeakDetection

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    server.make.useForever.provideCustomLayer(ServerChannelFactory.auto ++ EventLoopGroup.auto(8)).exitCode
  }

}