package com.rockthejvm.bookings.model

import java.sql.Date
import scala.util.Random


case class Reservation(
                      guestId: String,
                      hotelId: String,
                      startDate: Date,
                      endDate: Date,
                      roomNumber: Int,
                      confirmationNumber: String
                      ) {

  def intersect(another: Reservation) =
    this.hotelId == another.hotelId && this.roomNumber == another.roomNumber &&
      (
        startDate.compareTo(another.startDate) >= 0 && startDate.compareTo(another.endDate) <=0 ||
        another.startDate.compareTo(startDate) >= 0 && another.startDate.compareTo(endDate) <= 0
      )

  override def equals(obj: Any) = obj match {
    case Reservation(_, _, _, _, _, `confirmationNumber`) => true
    case _ => false
  }

  override def hashCode() = confirmationNumber.hashCode
}

object Reservation {
  def make(guestId: String, hotelId: String, startDate: Date, endDate: Date, roomNumber: Int): Reservation = {
    val chars = ('A' to 'Z') ++ ('0' to '9')
    val nChars = chars.length
    val confirmationNumber = (1 to 10).map(_ => chars(Random.nextInt(nChars))).mkString
    Reservation(guestId, hotelId, startDate, endDate, roomNumber, confirmationNumber)
  }

}