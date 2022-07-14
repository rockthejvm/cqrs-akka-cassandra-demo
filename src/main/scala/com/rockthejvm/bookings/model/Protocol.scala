package com.rockthejvm.bookings.model

import akka.actor.typed.ActorRef

import java.sql.Date

// commands
sealed trait Command
case class MakeReservation(guestId: String, startDate: Date, endDate: Date, roomNumber: Int, replyTo: ActorRef[HotelProtocol]) extends Command
case class ChangeReservation(confirmationNumber: String, startDate: Date, endDate: Date, roomNumber: Int, replyTo: ActorRef[HotelProtocol]) extends Command
case class CancelReservation(confirmationNumber: String, replyTo: ActorRef[HotelProtocol]) extends Command

// events
sealed trait Event
case class ReservationAccepted(reservation: Reservation) extends Event with HotelProtocol
case class ReservationUpdated(oldReservation: Reservation, newReservation: Reservation) extends Event with HotelProtocol
case class ReservationCanceled(reservation: Reservation) extends Event with HotelProtocol

// communication with the "agent"
case class CommandFailure(reason: String) extends HotelProtocol

trait HotelProtocol
case class ManageHotel(hotel: ActorRef[Command]) extends HotelProtocol
case class Generate(nCommands: Int) extends HotelProtocol