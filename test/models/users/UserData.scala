package models.users

import util.ScalaPersistenceManager

object UserData {  
  def load(debug: Boolean = false)(implicit pm: ScalaPersistenceManager) {
    val mary = new User("mary", "Mary", Some("King"), "Claire", None, Gender.FEMALE, "mary@mary.com", "cla123")
    val christina = new User("christina", "Christina", Some("King"), "Teresa", Some("Tina"), Gender.FEMALE, "christina@christina.com", "ter123")
    val jack = new User("jack", "Jack", Some("Oliver"), "Phillips", None, Gender.MALE, "jack@jack.com", "phi123")
    val richard = new User("richard", "Richard", Some("King"), "Will", None, Gender.MALE, "richard@richard.com", "wil123")
    val todd = new User("todd", "Todd", Some("Allen"), "O'Bryan", None, Gender.MALE, "todd@todd.com", "obr123")
    val fitzgerald = new User("fitzgerald", "Fitzgerald", Some("Longfellow"), "Pennyworth", Some("Fitz of Fury"), Gender.MALE, "fitzgerald@fitzgerald.com", "pen123")
    val tyler = new User("tyler", "Tyler", None, "Darnell", None, Gender.MALE, "tyler@tyler.com", "dar123")
    val meriadoc = new User("meriadoc", "Meriadoc", None, "Brandybuck", Some("Merry"), Gender.MALE, "meriadoc@meradoc.com", "bra123")
    val peregrin = new User("peregrin", "Peregrin", None, "Took", Some("Pippin"), Gender.MALE, "peregrin@peregrin.com", "too123")
    val mack = new User("mack", "Mack", None, "House", Some("Brick"), Gender.MALE, "mack@mack.com", "hou123")
    val andrew = new User("andrew", "Andrew", None, "Hamm", None, Gender.MALE, "andrew@andrew.com", "ham123")
    val jordan = new User("jordan", "Jordan", None, "Jorgensen", None, Gender.MALE, "jordan@jordan.com", "jor123")
    val emma = new User("emma", "Emma", Some("Kathryn"), "King", None, Gender.FEMALE, "emma@emma.com", "kin123")
    val laura = new User("laura", "Laura", Some("Ann"), "King", None, Gender.FEMALE, "laura@laura.com", "kin123")
    val john = new User("john", "John", Some("Francis"), "King", None, Gender.MALE, "john@john.com", "kin123")
    val eric = new User("eric", "Eric", None, "McKnight", None, Gender.MALE, "eric@eric.com", "mck123")
    val ericStud = new Student(eric, "4208935702", "384979", 6, "MST")
    val jackStud = new Student(jack, "3757202948", "425636", 0, "MST")
    val fitzgeraldStud = new Student(fitzgerald, "8340522509", "382085", 4, "VA")
    val tylerStud = new Student(tyler, "2558203943", "246666", 8, "MST")
    val meriadocStud = new Student(meriadoc, "6872037839", "495312", 9, "HSU")
    val peregrinStud = new Student(peregrin, "0974781434", "375012", 10, "HSU")
    val mackStud = new Student(mack, "4907532423", "819823", 11, "MST")
    val andrewStud = new Student(andrew, "0572059453", "745105", 12, "MST")
    val jordanStud = new Student(jordan, "2094298408", "037432", 1, "MST")
    val emmaStud = new Student(emma, "4534414554", "245434", 6, "CMA")
    val lauraStud = new Student(laura, "3943334223", "403024", 3, "YPAS")
    val johnStud = new Student(john, "5022165324", "154524", 12, "HSU")
    val maryTeacher = new Teacher(mary, "318508", "4284802048")
    val christinaTeacher = new Teacher(christina, "542358", "8795177958")
    val richardTeacher = new Teacher(richard, "423423", "4478340832")
    val toddTeacher = new Teacher(todd, "323423", "3042093480")
    val toddGuardian = new Guardian(todd, Set(meriadocStud, peregrinStud))
    pm.makePersistentAll(List(eric, ericStud, mary, christina, jack, richard, john, fitzgerald, emma, laura, tyler, jordan, todd, andrew, mack, meriadoc, peregrin, maryTeacher, christinaTeacher, toddTeacher, richardTeacher, johnStud, fitzgeraldStud, emmaStud, lauraStud, tylerStud, jordanStud, jackStud, andrewStud, mackStud, meriadocStud, peregrinStud))
  }
}