package models.books

import org.joda.time.LocalDateTime
import org.joda.time.LocalDate

import models.courses.Student
import models.courses.Teacher

object BookData {
  def load(debug: Boolean = true) {
  }

  val gabb3 = new Title(
    "The Great American Bathroom Book",
    Some("Stevens W. Anderson"),
    Some("Compact Classics, Inc."),
    "9781880184264",
    Some(573),
    Some("8.4 x 5.7 x 1.7"),
    Some(1.9),
    true,
    Some(LocalDateTime.now()),
    Some("http://ecx.images-amazon.com/images/I/41H3AX9Q8ZL._SY300_.jpg"))
  val tmi = new Title(
    "The Mind's I",
    Some("Douglas R. Hofstadter"),
    Some("Bantam Books"),
    "9780553345841",
    Some(512),
    Some("9 x 6 x 1.1"),
    Some(1.1),
    true,
    Some(LocalDateTime.now()),
    Some("http://ecx.images-amazon.com/images/I/511tVNypUXL.jpg"))
  val tg = new Title(
    "Turtle Geometry",
    Some("Harold Abelson and Andrea diSessa"),
    Some("The MIT Press"),
    "0262510375",
    Some(497),
    Some("1.1 x 5.8 x 8.8"),
    Some(1.4),
    true,
    Some(LocalDateTime.now()),
    Some("http://ecx.images-amazon.com/images/I/41ujhRpL0XL.jpg"))
  val semantics = new Title(
    "Semantics",
    Some("John I. Saeed"),
    Some("Blackwell Publishers"),
    "9780631200345",
    Some(360),
    Some("10.2 x 7 x 1.2"),
    Some(1.8),
    true,
    Some(LocalDateTime.now()),
    Some("http://ecx.images-amazon.com/images/I/716W7TS3FKL.gif"))

  val gabb3pg = new PurchaseGroup(
    gabb3,
    LocalDate.now(),
    59.99)
  val tmipg = new PurchaseGroup(
    tmi,
    LocalDate.now(),
    0.99)
  val tgpg = new PurchaseGroup(
    tg,
    LocalDate.now(),
    12.59)
  val semanticspg = new PurchaseGroup(
    semantics,
    LocalDate.now(),
    100.00)

  val gabb3copy = new Copy(
    gabb3pg,
    200,
    false,
    false)
  val tmicopy = new Copy(
    tmipg,
    500,
    true,
    false)
  val tgcopy = new Copy(
    tgpg,
    150,
    false,
    true)
  val semanticscopy = new Copy(
    semanticspg,
    99,
    true,
    true)

  val gabb3check = new Checkout(
    Student.getByUsername("jack").get,
    gabb3copy,
    Some(new LocalDate(2015, 10, 5)),
    None)
  val tmicheck = new Checkout(
    Student.getByUsername("mack").get,
    tmicopy,
    Some(new LocalDate(2013, 8, 9)),
    None)
  val tgcheck = new Checkout(
    Student.getByUsername("laura").get,
    tgcopy,
    Some(new LocalDate(2014, 5, 12)),
    None)
  val semanticscheck = new Checkout(
    Student.getByUsername("jordan").get,
    semanticscopy,
    Some(new LocalDate(2012, 1, 14)),
    None)

  val gabb3queue = new LabelQueueSet(
    Teacher.getByUsername("mary").get,
    gabb3,
    "2,8,3,7,4")
  val tmiqueue = new LabelQueueSet(
    Teacher.getByUsername("todd").get,
    tmi,
    "1-10")
  val tgqueue = new LabelQueueSet(
    Teacher.getByUsername("christina").get,
    tg,
    "3,7")
  val semanticsqueue = new LabelQueueSet(
    Teacher.getByUsername("richard").get,
    semantics,
    "1,3")
} 