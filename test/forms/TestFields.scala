package forms

import scala.xml.{Attribute, Null, Text}

import org.scalatest.FunSuite
import fields._
import validators._
import forms.widgets._

class TestFields extends FunSuite {
  test("1. default TextField") {
    val f = new TextField()
    assert(f.clean("1") === Right(Some("1")))
    assert(f.clean("hello") === Right(Some("hello")))
    assert(f.clean("") === Left(ValidationError(List("This field is required."))))
    assert(f.clean(Nil) === Left(ValidationError(List("This field is required."))))
    assert(f.maxLength === None)
    assert(f.minLength === None)
  }
  
  test("2. optional TextField") {
    val f = new TextField(required=false)
    assert(f.clean("1") === Right(Some("1")))
    assert(f.clean("hello") === Right(Some("hello")))
    assert(f.clean("") === Right(None))
    assert(f.clean(Nil) === Right(None))
    assert(f.maxLength === None)
    assert(f.minLength === None)
  }
  
  test("3. optional TextField with max length") {
    val f = new TextField(required=false, maxLength=Some(10))
    assert(f.clean("12345") === Right(Some("12345")))
    assert(f.clean("1234567890") === Right(Some("1234567890")))
    assert(f.clean("") === Right(None))
    assert(f.clean(Nil) === Right(None))
    assert(f.clean("12345678901") === Left(ValidationError(List("This value must have no more than 10 characters. (It has 11.)"))))
    assert(f.maxLength === Some(10))
    assert(f.minLength === None)
  }
  
  test("4. optional TextField with min length") {
    val f = new TextField(required=false, minLength=Some(10))
    assert(f.clean("") === Right(None))
    assert(f.clean("12345") === Left(ValidationError("This value must have at least 10 characters. (It has 5.)")))
    assert(f.clean("1234567890") === Right(Some("1234567890")))
    assert(f.clean("1234567890a") === Right(Some("1234567890a")))
    assert(f.maxLength === None)
    assert(f.minLength === Some(10))
  }
  
  test("5. required TextField with min length") {
    val f = new TextField(minLength=Some(10))
    assert(f.clean("") === Left(ValidationError(List("This field is required."))))
    assert(f.clean("123456") === Left(ValidationError(List("This value must have at least 10 characters. (It has 6.)"))))
    assert(f.clean("1234567890") === Right(Some("1234567890")))
    assert(f.clean("1234567890a") === Right(Some("1234567890a")))
    assert(f.maxLength === None)
    assert(f.minLength === Some(10))
  }
  
  test("TextField widgetAttrs") {
    val f1 = new TextField()
    assert(f1.widgetAttrs(new TextInput()) === Null)
    val f2 = new TextField(maxLength=Some(10))
    assert(f2.widgetAttrs(new HiddenInput()) === Null)
    assert(f2.widgetAttrs(new TextInput()) === Attribute("maxlength", Text("10"), Null))
    assert(f2.widgetAttrs(new PasswordInput()) === Attribute("maxlength", Text("10"), Null))
  }
  
  test("1. NumericField[Int]") {
    val f = new NumericField[Int]()
    assert(f.clean("") === Left(ValidationError("This field is required.")))
    assert(f.clean(Nil) === Left(ValidationError("This field is required.")))
    assert(f.clean("1") === Right(Some(1)))
    assert(f.clean("23") === Right(Some(23)))
    assert(f.clean("a") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.clean("3.14") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.clean("1 ") === Right(Some(1)))
    assert(f.clean(" 2") === Right(Some(2)))
    assert(f.clean(" 3 ") === Right(Some(3)))
    assert(f.clean("4a") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.maxValue === None)
    assert(f.minValue === None)
  }
  
  test("2. optional NumericField[Int]") {
    val f = new NumericField[Int](required=false)
    assert(f.clean("") === Right(None))
    assert(f.clean(Nil) === Right(None))
    assert(f.clean("1") === Right(Some(1)))
    assert(f.clean("23") === Right(Some(23)))
    assert(f.clean("a") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.clean("3.14") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.clean("1 ") === Right(Some(1)))
    assert(f.clean(" 2") === Right(Some(2)))
    assert(f.clean(" 3 ") === Right(Some(3)))
    assert(f.clean("4a") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.maxValue === None)
    assert(f.minValue === None)
  }

  test("3. required NumericField[Int] with max value") {
    val f = new NumericField[Int](maxValue=Some(23))
    assert(f.clean("") === Left(ValidationError("This field is required.")))
    assert(f.clean(Nil) === Left(ValidationError("This field is required.")))
    assert(f.clean("1") === Right(Some(1)))
    assert(f.clean("23") === Right(Some(23)))
    assert(f.clean("24") === Left(ValidationError("This value must be at most 23.")))
    assert(f.clean("a") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.clean("3.14") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.clean("1 ") === Right(Some(1)))
    assert(f.clean(" 2") === Right(Some(2)))
    assert(f.clean(" 3 ") === Right(Some(3)))
    assert(f.clean("4a") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.maxValue === Some(23))
    assert(f.minValue === None)
  }
  
  test("4. required NumericField[Int] with min value") {
    val f = new NumericField[Int](minValue=Some(3))
    assert(f.clean("") === Left(ValidationError("This field is required.")))
    assert(f.clean(Nil) === Left(ValidationError("This field is required.")))
    assert(f.clean("2") === Left(ValidationError("This value must be at least 3.")))
    assert(f.clean("23") === Right(Some(23)))
    assert(f.clean("3") === Right(Some(3)))
    assert(f.clean("a") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.clean("3.14") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.clean("5 ") === Right(Some(5)))
    assert(f.clean(" 8") === Right(Some(8)))
    assert(f.clean(" 3 ") === Right(Some(3)))
    assert(f.clean("4a") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.maxValue === None)
    assert(f.minValue === Some(3))
  }
  
  test("5. required NumericField[Int] with both min and max values") {
    val f = new NumericField[Int](minValue=Some(-3), maxValue=Some(3))
    assert(f.clean("") === Left(ValidationError("This field is required.")))
    assert(f.clean(Nil) === Left(ValidationError("This field is required.")))
    assert(f.clean("-4") === Left(ValidationError("This value must be at least -3.")))
    assert(f.clean("-3") === Right(Some(-3)))
    assert(f.clean("0") === Right(Some(0)))
    assert(f.clean("3") === Right(Some(3)))
    assert(f.clean("4") === Left(ValidationError("This value must be at most 3.")))
    assert(f.clean("a") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.clean("3.14") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.clean("-1 ") === Right(Some(-1)))
    assert(f.clean(" 2") === Right(Some(2)))
    assert(f.clean(" -2 ") === Right(Some(-2)))
    assert(f.clean("4a") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.maxValue === Some(3))
    assert(f.minValue === Some(-3))   
  }
  
  test("6. optional NumericField[Int] with both min and max values") {
    val f = new NumericField[Int](required=false, minValue=Some(-3), maxValue=Some(3))
    assert(f.clean("") === Right(None))
    assert(f.clean(Nil) === Right(None))
    assert(f.clean("-4") === Left(ValidationError("This value must be at least -3.")))
    assert(f.clean("-3") === Right(Some(-3)))
    assert(f.clean("0") === Right(Some(0)))
    assert(f.clean("3") === Right(Some(3)))
    assert(f.clean("4") === Left(ValidationError("This value must be at most 3.")))
    assert(f.clean("a") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.clean("3.14") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.clean("-1 ") === Right(Some(-1)))
    assert(f.clean(" 2") === Right(Some(2)))
    assert(f.clean(" -2 ") === Right(Some(-2)))
    assert(f.clean("    ") === Right(None))
    assert(f.clean("4a") === Left(ValidationError("This value must be a positive or negative whole number.")))
    assert(f.maxValue === Some(3))
    assert(f.minValue === Some(-3))   
  }
  
  test("1. required NumericField[Double]") {
    val f = new NumericField[Double]()
    assert(f.clean("") === Left(ValidationError("This field is required.")))
    assert(f.clean(Nil) === Left(ValidationError("This field is required.")))
    assert(f.clean("-4") === Right(Some(-4.0)))
    assert(f.clean("-3") === Right(Some(-3.0)))
    assert(f.clean("0") === Right(Some(0.0)))
    assert(f.clean("a") === Left(ValidationError("This value must be a number.")))
    assert(f.clean("3.14") === Right(Some(3.14)))
    assert(f.clean("-1.5 ") === Right(Some(-1.5)))
    assert(f.clean(" 2.5") === Right(Some(2.5)))
    assert(f.clean(" -2.5 ") === Right(Some(-2.5)))
    assert(f.clean("    ") === Left(ValidationError("This field is required.")))
    assert(f.clean("4a") === Left(ValidationError("This value must be a number.")))
    assert(f.maxValue === None)
    assert(f.minValue === None)
  }
  
  test("2. optional NumericField[Double]") {
    val f = new NumericField[Double](required=false)
    assert(f.clean("") === Right(None))
    assert(f.clean(Nil) === Right(None))
    assert(f.clean("-4") === Right(Some(-4.0)))
    assert(f.clean("-3") === Right(Some(-3.0)))
    assert(f.clean("0") === Right(Some(0.0)))
    assert(f.clean("a") === Left(ValidationError("This value must be a number.")))
    assert(f.clean("3.14") === Right(Some(3.14)))
    assert(f.clean("-1.5 ") === Right(Some(-1.5)))
    assert(f.clean(" 2.5") === Right(Some(2.5)))
    assert(f.clean(" -2.5 ") === Right(Some(-2.5)))
    assert(f.clean("    ") === Right(None))
    assert(f.clean("4a") === Left(ValidationError("This value must be a number.")))
    assert(f.maxValue === None)
    assert(f.minValue === None)   
  }
  
  test("3. NumericField[Double] with min and max value") {
    val f = new NumericField[Double](minValue=Some(-3.5), maxValue=Some(2.1))
    assert(f.clean("") === Left(ValidationError("This field is required.")))
    assert(f.clean(Nil) === Left(ValidationError("This field is required.")))
    assert(f.clean("-4") === Left(ValidationError("This value must be at least -3.5.")))
    assert(f.clean("-3") === Right(Some(-3.0)))
    assert(f.clean("0") === Right(Some(0.0)))
    assert(f.clean("a") === Left(ValidationError("This value must be a number.")))
    assert(f.clean("2.11") === Left(ValidationError("This value must be at most 2.1.")))
    assert(f.clean("-1.5 ") === Right(Some(-1.5)))
    assert(f.clean(" 2.1") === Right(Some(2.1)))
    assert(f.clean(" -3.5 ") === Right(Some(-3.5)))
    assert(f.clean("    ") === Left(ValidationError("This field is required.")))
    assert(f.clean("4a") === Left(ValidationError("This value must be a number.")))
    assert(f.maxValue === Some(2.1))
    assert(f.minValue === Some(-3.5))   
  }

  test("4. optional NumericField[Double] with min and max value") {
    val f = new NumericField[Double](required=false, minValue=Some(-3.5), maxValue=Some(2.1))
    assert(f.clean("") === Right(None))
    assert(f.clean(Nil) === Right(None))
    assert(f.clean("-4") === Left(ValidationError("This value must be at least -3.5.")))
    assert(f.clean("-3") === Right(Some(-3.0)))
    assert(f.clean("0") === Right(Some(0.0)))
    assert(f.clean("a") === Left(ValidationError("This value must be a number.")))
    assert(f.clean("2.11") === Left(ValidationError("This value must be at most 2.1.")))
    assert(f.clean("-1.5 ") === Right(Some(-1.5)))
    assert(f.clean(" 2.1") === Right(Some(2.1)))
    assert(f.clean(" -3.5 ") === Right(Some(-3.5)))
    assert(f.clean("    ") === Right(None))
    assert(f.clean("4a") === Left(ValidationError("This value must be a number.")))
    assert(f.maxValue === Some(2.1))
    assert(f.minValue === Some(-3.5))   
  }
}