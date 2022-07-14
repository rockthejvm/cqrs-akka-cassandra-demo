package com.rockthejvm.bookings.actor

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.rockthejvm.bookings.model._

import java.sql.Date
import java.time.LocalDate
import java.util.UUID
import scala.util.Random

object ReservationAgent {

  val DELETE_PROB = 0.05
  val CHANGE_PROB = 0.2
  val START_DATE = LocalDate.of(2023,1,1)

  def generateAndSend(hotels: Vector[ActorRef[Command]], state: Map[String, Reservation])(implicit replyTo: ActorRef[HotelProtocol]): Unit = {
    val prob = Random.nextDouble()
    if (prob <= DELETE_PROB && state.keys.nonEmpty) {
      // generate cancellation
      val confNumbers = state.keysIterator.toVector
      val confNumberIndex = Random.nextInt(confNumbers.size)
      val confNumber = confNumbers(confNumberIndex)
      val reservation = state(confNumber)
      val hotel = hotels.find(_.path.name == reservation.hotelId)
      hotel.foreach(_ ! CancelReservation(confNumber, replyTo))
    } else if (prob <= CHANGE_PROB && state.keys.nonEmpty) {
      // generate a reservation change
      val confNumbers = state.keysIterator.toVector
      val confNumberIndex = Random.nextInt(confNumbers.size)
      val confNumber = confNumbers(confNumberIndex)
      val Reservation(guestId, hotelId, startDate, endDate, roomNumber, confirmationNumber) = state(confNumber)
      val localS = startDate.toLocalDate
      val localE = endDate.toLocalDate

      // can change duration OR room number
      val isDurationChange = Random.nextBoolean()
      val newReservation =
        if (isDurationChange) {
          val newLocalS = localS.plusDays(Random.nextInt(5) - 2) // between -2 and +3 days
          val tentativeLocalE = localE.plusDays(Random.nextInt(5) - 2) // same, but interval might be degenerate
          // make end at least newStart + 1 day
          val newLocalE =
            if (tentativeLocalE.compareTo(newLocalS) <= 0)
              newLocalS.plusDays(Random.nextInt(5) + 1)
            else
              tentativeLocalE

          Reservation(guestId, hotelId, Date.valueOf(newLocalS), Date.valueOf(newLocalE), roomNumber, confirmationNumber)
        } else {
          val newRoomNumber = Random.nextInt(100) + 1
          Reservation(guestId, hotelId, startDate, endDate, newRoomNumber, confirmationNumber)
        }

      // now change reservation
      val Reservation(_, _, newStartDate, newEndDate, newRoomNumber, _) = newReservation
      val hotel = hotels.find(_.path.name == hotelId)
      // take both changes into account in a single call
      hotel.foreach(_ ! ChangeReservation(confirmationNumber, newStartDate, newEndDate, newRoomNumber, replyTo))
    } else {
      // new reservation
      val hotelIndex = Random.nextInt(hotels.size)
      val startDate = START_DATE.plusDays(Random.nextInt(365))
      val endDate = startDate.plusDays(Random.nextInt(14))
      val roomNumber = 1 + Random.nextInt(100)
      val hotel = hotels(hotelIndex)
      hotel ! MakeReservation(UUID.randomUUID().toString, Date.valueOf(startDate), Date.valueOf(endDate), roomNumber, replyTo)
    }
  }

  // simulate a large influx of commands to hotel actors
  def active(hotels: Vector[ActorRef[Command]], state: Map[String, Reservation]): Behavior[HotelProtocol] =
    Behaviors.receive[HotelProtocol] { (context, message) =>
      implicit val self: ActorRef[HotelProtocol] = context.self

      message match {
        case Generate(nCommands) =>
          (1 to nCommands).foreach(_ => generateAndSend(hotels, state))
          Behaviors.same
        case ManageHotel(hotel) =>
          context.log.info(s"Managing hotel ${hotel.path.name}")
          active(hotels :+ hotel, state)
        case ReservationAccepted(res) =>
          context.log.info(s"Reservation accepted: ${res.confirmationNumber}")
          active(hotels, state + (res.confirmationNumber -> res))
        case ReservationUpdated(_, newRes) =>
          context.log.info(s"Reservation updated: ${newRes.confirmationNumber}")
          active(hotels, state + (newRes.confirmationNumber -> newRes))
        case ReservationCanceled(res) =>
          context.log.info(s"Reservation cancelled: ${res.confirmationNumber}")
          active(hotels, state - res.confirmationNumber)
        case _ =>
          Behaviors.same
      }

    }

  def apply(): Behavior[HotelProtocol] = active(Vector(), Map())
}
