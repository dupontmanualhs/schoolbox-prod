package quizzes

class Quiz(val questions: List[Question]) {
	
}

object Quiz {
  val quiz1 = new Quiz(List(new Question("a+5a", "6a"),
    				  new Question("5b * 4a", "20ab"),
    				  new Question("4b^2/2b", "2b"),
    				  new Question("question", "answer")))
}