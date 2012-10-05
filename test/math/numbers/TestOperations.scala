package math.numbers

import org.scalatest.FunSuite

class TestOperations extends FunSuite {
  test("number toString") {
    assert(Integer(3).toString === "3")
    assert(Integer(-27).toString === "-27")
    assert(Frac(2, 3).toString === "2/3")
    assert(Frac(-3, 7).toString === "-3/7")
    assert(Frac(3, -7).toString === "3/-7")
    assert(Frac(-3, -7).toString === "-3/-7")
    assert(Dec("1.5").toString === "1.5")
    assert(Dec("1.000000000000000000000000000004").toString === "1.000000000000000000000000000004")
  }
  
  test("number repr") {
    assert(Integer(3).repr === """Integer("3")""")
    assert(Integer(-27).repr === """Integer("-27")""")
    assert(Frac(2, 3).repr === """Frac("2", "3")""")
    assert(Frac(-3, 7).repr === """Frac("-3", "7")""")
    assert(Frac(3, -7).repr === """Frac("3", "-7")""")
    assert(Frac(-3, -7).repr === """Frac("-3", "-7")""")
    assert(Dec("1.5").repr === """Dec("1.5")""")
    assert(Dec("1.000000000000000000000000000004").repr === """Dec("1.000000000000000000000000000004")""")    
  }
  
  test("addition") {
    assert(Integer(2) + Integer(3) === Integer(5))
    assert(Frac(1, 2) + Frac(1, 3) === Frac(5, 6))
    assert(Frac(1, 2) + Frac(1, 2) === Integer(1))
    assert(Dec("1.002") + Dec("2.004") === Dec("3.006"))
    assert(Dec("1.5") + Dec("2.5") === Integer(4))
    assert(Frac(3, 4) + Frac(1, 4) === Integer(1))
    assert(Integer(2) + Dec("3.274") === Dec("5.274"))
    assert(Integer(1) + Frac(-2, 3) === Frac(1, 3))
    assert(Dec("1.5") + Integer(3) === Dec("4.5"))
    assert(Dec("1.25") + Frac(1, 2) === Frac(7, 4))
    assert(Dec("0.001") + Frac(1, 3) === Frac(1003, 3000))
    
  }
}