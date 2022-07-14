package com.rockthejvm.bookings.apps

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.alpakka.cassandra.scaladsl.CassandraSessionRegistry
import akka.stream.scaladsl.{Sink, Source}

import java.time.LocalDate
import scala.concurrent.Future

object CassandraTableCleaner {
  implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "CassandraSystem")
  import system.executionContext
  val session = CassandraSessionRegistry(system).sessionFor("akka.projection.cassandra.session-config")

  def clearTables(): Unit = {
    val daysFree = Source (
      for {
        hotelId <- (1 to 100).map(i => s"hotel_$i")
        roomNumber <- 1 to 100 // 100 rooms/hotel
        day <- (0 until 365).map(LocalDate.of(2023,1,1).plusDays(_))
      } yield (hotelId, roomNumber, day)
    )

    val clearRooms = daysFree.mapAsync(8) {
      case (hotelId, roomNumber, day) =>
        // write an entry to Cassandra
        session.executeWrite(
          "INSERT INTO hotel.available_rooms_by_hotel_date (hotel_id, date, room_number, is_available) VALUES" +
          s"('$hotelId', '$day', $roomNumber, true)"
        )
    }
      .runWith(Sink.last) // future
      .recover(e => println(s"clearing rooms failed: ${e}"))

    Future.sequence(
      clearRooms :: List (
        "truncate akka.all_persistence_ids",
        "truncate akka.messages",
        "truncate akka.metadata",
        "truncate akka.tag_scanning",
        "truncate akka.tag_views",
        "truncate akka.tag_write_progress",
        "truncate akka_snapshot.snapshots",
        "truncate reservation.reservations_by_hotel_date",
        "truncate reservation.reservations_by_guest",
      ).map(session.executeWrite(_))
    )
      .recover(e => println(s"failed clearing tables: ${e}"))
      .onComplete { _ =>
        println("all tables clear")
        system.terminate()
      }
  }

  def main(args: Array[String]): Unit = {
    clearTables()
  }
}
