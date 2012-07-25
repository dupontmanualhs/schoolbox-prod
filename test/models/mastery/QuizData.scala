package models.mastery

import util.ScalaPersistenceManager

object QuizData {
  def load(debug: Boolean = false)(implicit pm: ScalaPersistenceManager) {
    val addLike = new Kind("add like terms")
    val multLike = new Kind("multiply like terms")
    val multUnlike = new Kind("multiply unlike terms")
    pm.makePersistentAll(List(addLike, multLike, multUnlike))
    val questions = List(
        new Question("`5a+a`", "`6a`", addLike),
        new Question("`3b+2b`", "`5b`", addLike),
        new Question("`6c+3c`", "`9c`", addLike),
        new Question("`2d+5d`", "`7d`", addLike),
        new Question("`5a*a`", "`10a^2`", multLike),
        new Question("`3b*2b`", "`6b^2`", multLike),
        new Question("`6c*3c`", "`18c^2`", multLike),
        new Question("`2d*5d`", "`10d^2`", multLike),
        new Question("`5a*2b`", "`10ab`", multUnlike),
        new Question("`2d*a`", "`2ad`", multUnlike),
        new Question("`3b*2c`", "`6bc`", multUnlike),
        new Question("`5c*3d`", "`10cd`", multUnlike))
    pm.makePersistentAll(questions)
    val template = new QuizTemplate("Quiz 1", 
                                    List(new QuestionSet(1, addLike),
                                         new QuestionSet(2, multLike),
                                         new QuestionSet(1, multUnlike)))
    pm.makePersistent(template)
  }
}