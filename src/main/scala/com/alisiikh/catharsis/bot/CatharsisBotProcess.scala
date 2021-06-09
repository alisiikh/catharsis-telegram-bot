package com.alisiikh.catharsis.bot

import cats.effect._
import cats.implicits._
import com.alisiikh.catharsis.bot.api.{ BotResponse, BotUpdate, Http4sBotApi }
import com.alisiikh.catharsis.giphy.GiphyClient
import fs2.Stream
import io.chrisdavenport.log4cats.Logger
import io.circe.generic.auto._
import org.http4s._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe._

import scala.concurrent.ExecutionContext

class CatharsisBotProcess[F[_]: Async: Temporal: Logger](token: String, giphyApiKey: String) {

  def stream: Stream[F, Unit] =
    BlazeClientBuilder[F](ExecutionContext.global).stream
      .flatMap { client =>
        Stream.force {
          for {
            api   <- Sync[F].delay(new Http4sBotApi(token, client))
            giphy <- Sync[F].delay(new GiphyClient(giphyApiKey))
            bot   <- Sync[F].delay(new CatharsisBot(api, giphy))
          } yield bot.stream
        }
      }
}
