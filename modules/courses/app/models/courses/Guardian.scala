package models.courses

import scala.collection.JavaConverters._
import javax.jdo.annotations._
import org.datanucleus.query.typesafe._
import org.datanucleus.api.jdo.query._
import models.users.QRole
import models.users.Role
import models.users.User
import config.users.UsesDataStore
import models.users.QUser
import org.dupontmanual.forms.fields.ChoiceFieldOptional

@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
class Guardian extends Role {
  @Persistent
  @Unique
  @Column(allowsNull="true")
  private[this] var _contactId: String = _
  def contactId: Option[String] = Option(_contactId)
  def contactId_=(theContactId: Option[String]) = theContactId match {
    case Some(cid) => _contactId = cid
    case None => _contactId = null
  }
  def contactId_=(theContactId: String) { _contactId = theContactId }
  
  @Persistent
  @Join
  @Element(types=Array(classOf[Student]))
  private[this] var _children: java.util.Set[Student] = _
  def children: Set[Student] = _children.asScala.toSet
  def children_=(theChildren: Set[Student]) { _children = theChildren.asJava }
    
  def this(user: User, contactId: Option[String], children: Set[Student]){
    this()
    user_=(user)
    contactId_=(contactId)
    children_=(children)
  }
  
  def role = "Parent/Guardian"
    
  override def canEqual(that: Any): Boolean = that.isInstanceOf[Guardian]
}

object Guardian extends UsesDataStore {
  def getByUsername(username: String): Option[Guardian] = {
    val cand = QGuardian.candidate
    val userVar = QUser.variable("userVar")
    dataStore.pm.query[Guardian].filter(cand.user.eq(userVar).and(userVar.username.eq(username))).executeOption()
  }
  
  def getByContactId(contactId: String): Option[Guardian] = {
    val cand = QGuardian.candidate
    dataStore.pm.query[Guardian].filter(cand.contactId.eq(contactId)).executeOption()
  }
  
  def ChooseGuardianField(students: List[Student]): ChoiceFieldOptional[Guardian] = {
    def studentWithGuardian(student: Student, guardian: Guardian) = s"Student: ${student.formalName} - Guardian: ${guardian.formalName}"
    val guardiansByStudent: List[(String, Guardian)] = students.map((s: Student) => 
        s.guardians().map((g: Guardian) => (studentWithGuardian(s, g), g))
    ).flatten
    new  ChoiceFieldOptional[Guardian]("Guardian", guardiansByStudent)
  }
}


trait QGuardian extends QRole[Guardian] {
  private[this] lazy val _children: CollectionExpression[java.util.Set[Student], Student] = 
      new CollectionExpressionImpl[java.util.Set[Student], Student](this, "_children")
  def children: CollectionExpression[java.util.Set[Student], Student] = _children
  
  private[this] lazy val _contactId: StringExpression = new StringExpressionImpl(this, "_contactId")
  def contactId: StringExpression = _contactId
}

object QGuardian {
  def apply(parent: PersistableExpression[Guardian], name: String, depth: Int): QGuardian = {
    new PersistableExpressionImpl[Guardian](parent, name) with QGuardian
  }
  
  def apply(cls: Class[Guardian], name: String, exprType: ExpressionType): QGuardian = {
    new PersistableExpressionImpl[Guardian](cls, name, exprType) with QGuardian
  }
  
  private[this] lazy val jdoCandidate: QGuardian = candidate("this")
  
  def candidate(name: String): QGuardian = QGuardian(null, name, 5)
  
  def candidate(): QGuardian = jdoCandidate
  
  def parameter(name: String): QGuardian = QGuardian(classOf[Guardian], name, ExpressionType.PARAMETER)
  
  def variable(name: String): QGuardian = QGuardian(classOf[Guardian], name, ExpressionType.VARIABLE)  
}