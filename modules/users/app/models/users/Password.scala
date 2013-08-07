package models.users

import javax.jdo.annotations._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import java.security.SecureRandom
import org.apache.commons.codec.binary.Hex
import java.security.MessageDigest
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class Password {
  var value: String = _
    
  def this(nakedPassword: String) {
    this()
    value = PasswordHash.hashes.head.encode(nakedPassword, None, None).toString
  }
  
  def matches(possPass: String): Boolean = {
    value match {
      case PasswordForDataStore(alg, its, salt, hash) => {
        val pw = PasswordForDataStore(alg, its, salt, hash)
        val checks = PasswordHash.hashes.find(_.algorithm == alg).map(_.verify(possPass, pw)).getOrElse(false)
        if (!checks) false
        else {
          val pref = PasswordHash.preferred
          if (alg != pref.algorithm || its != pref.iterations) {
            value = pref.encode(possPass, None, None).toString
          }
          true
        }
      }
      case _ => false
    }
  }
}

object Password {
  def fromEncoding(encoding: String): Password = {
    val pw = new Password()
    pw.value = encoding
    pw
  }
}

case class PasswordForDataStore(
  algorithm: String,
  iterations: Option[Int],
  saltString: String, 
  hash: String
) {
  override def toString: String = iterations match {
    case None => s"$algorithm$$$saltString$$$hash"
    case Some(its) => s"$algorithm$$$its$$$saltString$$$hash"
  }
  
  def salt: Array[Byte] = Hex.decodeHex(saltString.toCharArray)
}

object PasswordForDataStore {
  private def toOptionInt(str: String): Option[Int] = {
    try {
      Some(str.toInt)
    } catch {
      case e: NumberFormatException => None
    }
  }
  
  def unapply(str: String): Option[(String, Option[Int], String, String)] = {
    str.split("\\$").toList match {
      case List(alg, its, salt, hash) if toOptionInt(its).isDefined => {
        Some(alg, toOptionInt(its), salt, hash)
      }
      case List(alg, salt, hash) => {
        Some(alg, None, salt, hash)
      }
      case _ => None
    } 
  }
}

/** A password hash is stored as "algorithm$iterations$salt$hash"
 *  with the number of iterations optional for some algorithms
 */
trait PasswordHash { 
  val algorithm: String
  val iterations: Option[Int] // minimum number of iterations for security
                              // None if algorithm not iterated
  
  /** produces a PasswordForDataStore given a password, an optional salt, and the
   *  number of iterations. If no salt is given, a random 64-bit salt is created.
   */
  def encode(
      nakedPassword: String, 
      salt: Option[String] = None,
      iterations: Option[Int] = None): PasswordForDataStore
  
  def verify(possPass: String, encoding: PasswordForDataStore): Boolean = {
    val testPass = encode(possPass, Some(encoding.saltString), encoding.iterations)
    testPass.hash == encoding.hash
  }
}

object PasswordHash {
  lazy val rand = new SecureRandom()
  
  lazy val hashes: List[PasswordHash] = List(Pbkdf2, Sha1)
  
  def preferred: PasswordHash = hashes.head
  
  def salt(numBytes: Int): Array[Byte] = {
    val result = new Array[Byte](numBytes)
    rand.nextBytes(result)
    result
  }
}

object Pbkdf2 extends PasswordHash {
  val algorithm = "pbkdf2"
  val iterations = Some(10000)
  
  def encode(pw: String, salt: Option[String] = None, 
      iters: Option[Int] = None): PasswordForDataStore = {
    val theSalt = salt.map(s => Hex.decodeHex(s.toCharArray)).getOrElse(PasswordHash.salt(8))
    val its = iters.getOrElse(iterations.get)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
    val spec = new PBEKeySpec(pw.toCharArray(), theSalt, its, 160);
    val hash = Hex.encodeHexString(factory.generateSecret(spec).getEncoded())
    PasswordForDataStore(algorithm, Some(its), Hex.encodeHexString(theSalt), hash)
  }
}

object Sha1 extends PasswordHash {
  val algorithm = "sha1"
  val iterations = None
  
  def encode(nakedPassword: String, 
      salt: Option[String], iterations: Option[Int]): PasswordForDataStore = {
    // ignore the iterations
    val md = MessageDigest.getInstance("SHA1")
    val theSalt = salt.getOrElse(Hex.encodeHexString(PasswordHash.salt(8)))
    val saltAndPassword = theSalt + nakedPassword
    val encoding = Hex.encodeHexString(md.digest(saltAndPassword.getBytes()))
    PasswordForDataStore(algorithm, None, theSalt, encoding)
  }
}


trait QPassword extends PersistableExpression[Password] {
  private[this] lazy val _value: StringExpression = {
    new StringExpressionImpl(this, "_value")
  }
  def value: StringExpression = _value
}

object QPassword {
  def apply(parent: PersistableExpression[Password], name: String, depth: Int): QPassword = {
    new PersistableExpressionImpl[Password](parent, name) with QPassword
  }
  
  def apply(cls: Class[Password], name: String, exprType: ExpressionType): QPassword = {
    new PersistableExpressionImpl[Password](cls, name, exprType) with QPassword
  }
  
  private[this] lazy val jdoCandidate: QPassword = candidate("this")
  
  def candidate(name: String): QPassword = candidate("this")
  
  def candidate(): QPassword = jdoCandidate
  
  def parameter(name: String): QPassword = QPassword(classOf[Password], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QPassword = QPassword(classOf[Password], name, ExpressionType.VARIABLE)
}