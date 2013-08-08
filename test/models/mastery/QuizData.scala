package models.mastery

import scalajdo.DataStore

object QuizData {
  def load(debug: Boolean = false) {
    val pm = DataStore.pm
    if (debug) println("Creating Masteries")
    val exponents1 = new Question("You can only add monomials if they have the same _____", 
        List("bases with the same exponents", "bases with the same exponents."), false)
    val exponents2parta = new Question("When you are adding monomials, ____ coefficients", 
        List("add"), false)
    val exponents2partb = new Question("When you are adding monomials, the bases and exponents _____", 
        List("stay the same", "stay the same."), false)
    val exponents3 = new Question("When you are multiplying monomials, ____ the coefficients, and ____ the exponents whose ____ are the ____. (List answers with commas)", 
        List("multiply, add, bases, same"), false)
    val exponents4 = new Question("When you are raising monomials to a power, ____ coefficients ______ and ____ the exponents inside the parenthesis by _____. (List answers with commas)",
        List("raise, to the power outside, multiply, the exponents outside the ()"), false)
    val exponents5 = new Question("When you are dividing monomials, _____ the coefficients by the ____ and _____ the exponents whose ____ are the ____. (List answers with commas)",
        List("divide, GCF, subtract, bases, same", "divide, gcf, subtract, bases, same", "divide, GCF, cancel, same", "divide, gcf, cancel, bases, same"), false)
    val exponents6a = new Question(M("5a+a="), List("6a", "6*a"), true)
    val exponents6b = new Question(M("3a+a="), List("4a", "4*a"), true)
    val exponents6c = new Question(M("-4a+a="), List("-3a", "-3*a"), true)
    val exponents6d = new Question(M("-5a+a="), List("-4a", "-4*a"), true)
    val exponents7a = new Question(M("(a^5)^4="), List("a^20"), true)
    val exponents7b = new Question(M("(a^3)^4="), List("a^12"), true)
    val exponents7c = new Question(M("(a^2)^4="), List("a^8"), true)
    val exponents7d = new Question(M("(a^4)^3="), List("a^12"), true)
    val exponents7e = new Question(M("(a^5)^3="), List("a^15"), true)
    val exponents8a = new Question(M("a^{-3}="), List("1/a^3"), true)
    val exponents8b = new Question(M("a^{-5}="), List("1/a^5"), true)
    val exponents8c = new Question(M("a^{-4}="), List("1/a^4"), true)
    val exponents8d = new Question(M("a^{-6}="), List("1/a^6"), true)
    val exponents9a = new Question(M("\\frac{a^4}{a^7}="), List("1/a^3"), true)
    val exponents9b = new Question(M("\\frac{a^3}{a^5}="), List("1/a^2"), true)
    val exponents9c = new Question(M("\\frac{a^2}{a^7}="), List("1/a^5"), true)
    val exponents9d = new Question(M("\\frac{a^3}{a^7}="), List("1/a^4"), true)
    val exponents9e = new Question(M("\\frac{a^5}{a^7}="), List("1/a^2"), true)
    val exponents9f = new Question(M("\\frac{a^5}{a^9}="), List("1/a^4"), true)
    val exponents10a = new Question(M("a\\cdot a\\cdot a="), List("a^3"), true)
    val exponents10b = new Question(M("a\\cdot a="), List("a^2"), true)
    val exponents11a = new Question(M("3a^3+a^3+a^2"), List("4a^3+a^2", "a^2+4a^3"), true)
    val exponents11b = new Question(M("a^3+a^3+a^2"), List("2a^3+a^2", "a^2+2a^3"), true)
    val exponents11c = new Question(M("2a^3+a^3+a^2"), List("3a^3+a^2", "a^2+3a^3"), true)
    val exponents11d = new Question(M("2a^3+2a^3+2a^2"), List("4a^3+2a^2", "2a^2+4a^3"), true)
    val exponents12a = new Question(M("7a\\cdot 2a"), List("14a^2"), true)
    val exponents12b = new Question(M("3a\\cdot 4a"), List("12a^2"), true)
    val exponents12c = new Question(M("5a\\cdot 4a"), List("20a^2"), true)
    val exponents12d = new Question(M("7a\\cdot 3a"), List("21a^2"), true)
    val exponents12e = new Question(M("6a\\cdot 3a"), List("18a^2"), true)
    val exponents12f = new Question(M("6a\\cdot 5a"), List("30a^2"), true)
    val exponents13a = new Question(M("-6m^2n+3m^2n"), List("-3m^2n", "-3nm^2"), true)
    val exponents13b = new Question(M("-7m^2n+3m^2n"), List("-4m^2n", "-4nm^2"), true)
    val exponents13c = new Question(M("-5m^2n+3m^2n"), List("-2m^2n", "-2nm^2"), true)
    val exponents13d = new Question(M("-5m^2n+8m^2n"), List("3m^2n", "3nm^2"), true)
    val exponents13e = new Question(M("-6m^2n+8m^2n"), List("2m^2n", "2nm^2"), true)
    val exponents14a = new Question(M("(-3a^5)^3"), List("-27a^15"), true)
    val exponents14b = new Question(M("(-5a^2)^3"), List("-125a^6"), true)
    val exponents14c = new Question(M("(-5a^4)^3"), List("-125a^12"), true)
    val exponents14d = new Question(M("(-4a^5)^3"), List("-64a^15"), true)
    val exponents14e = new Question(M("(-3a^7)^3"), List("-27a^21"), true)
    val exponents15a = new Question(M("-5m^4n\\cdot -4mn^3"), List("20m^5n^4", "20n^4m^5"), true)
    val exponents15b = new Question(M("-5m^2n\\cdot -2mn^3"), List("10m^3n^4", "10n^4m^3"), true)
    val exponents15c = new Question(M("-5m^4n\\cdot -3mn^3"), List("15m^5n^4", "15n^4m^5"), true)
    val exponents15d = new Question(M("-5m^4n\\cdot -2mn^3"), List("10m^5n^4", "10n^4m^5"), true)
    val exponents15e = new Question(M("-5m^4n\\cdot -3mn^4"), List("15m^5n^5", "15n^5m^5"), true)
    val exponents16a = new Question(M("-5m^2n-2mn^3"), List("A/S", "a/s"), true)
    val exponents16b = new Question(M("-5m^2n-2mn"), List("A/S", "a/s"), true)
    val exponents16c = new Question(M("-5m^2n-4mn^3"), List("A/S", "a/s"), true)
    val exponents17a = new Question(M("\\frac{a^{-2}}{a^{-7}}"), List("a^5"), true)
    val exponents17b = new Question(M("\\frac{a^{-2}}{a^{-5}}"), List("a^3"), true)
    val exponents17c = new Question(M("\\frac{a^{-4}}{a^{-6}}"), List("a^2"), true)
    val exponents17d = new Question(M("\\frac{a^{-3}}{a^{-7}}"), List("a^4"), true)
    val exponents17e = new Question(M("\\frac{a^{-3}}{a^{-8}}"), List("a^5"), true)
    val exponents18a = new Question(M("\\frac{3m^{-3}}{21m^3n^{-2}}"), List("n^2/7m^6"), true)
    val exponents18b = new Question(M("\\frac{3m^{-5}}{15m^3n^{-4}}"), List("n^4/5m^8"), true)
    val exponents18c = new Question(M("\\frac{3m^{-3}}{18m^3n^{-5}}"), List("n^5/6m^6"), true)
    val exponents18d = new Question(M("\\frac{6m^{-3}}{18m^4n^{-5}}"), List("n^5/3m^7"), true)
    val exponents18e = new Question(M("\\frac{15m^{-3}}{18m^4n^{-2}}"), List("5n^2/6m^7"), true)
    val exponents19a = new Question(M("(3a^3b^2)^4="), List("81a^12b^8", "81b^8a^12"), true)
    val exponents19b = new Question(M("(2a^3b^2)^4="), List("16a^12b^8", "16b^8a^12"), true)
    val exponents19c = new Question(M("(2a^4b^2)^5="), List("32a^20b^10", "32b^10a^20"), true)
    val exponents19d = new Question(M("(2a^4b^3)^5="), List("32a^20b^15", "32b^15a^20"), true)
    val exponents19e = new Question(M("(2a^3b^7)^5="), List("32a^15b^35", "32b^35a^15"), true)
    val exponents20a = new Question(M("\\frac{-6m^2n^4}{2m^4n}="), List("-3n^3/m^2"), true)
    val exponents20b = new Question(M("\\frac{-6m^2n^4}{3m^5n^{-1}}="), List("-2n^5/m^3"), true)
    val exponents20c = new Question(M("\\frac{-10m^4n^4}{2m^5n^{-2}}="), List("-5n^6/m"), true)
    val exponents20d = new Question(M("\\frac{-10m^4n^4}{20m^4n^{-2}}="), List("-n^6/2", "-1/2n^6", "-0.5n^6", "-.5n^6"), true)
    val exponents20e = new Question(M("\\frac{-14m^4n^4}{20m^4n^{-5}}="), List("-7n^9/10", "-7/10n^9", "(-7/10)n^9"), true)
    val exponents21a = new Question(M("(2a-5b)^2="), List("4a^2-20ab+25b^2", "25b^2-20ab+4a^2", "4a^2+25b^2-20ab", "25b^2+4a^2-20ab", "-20ab+25b^2+4a^2", "-20ab+4a^2+25b^2"), true)
    val exponents21b = new Question(M("(3a-2b)^2="), List("9a^2-12ab+4b^2", "4b^2-20ab+9a^2", "9a^2+4b^2-12ab", "4b^2+9a^2-12ab", "-12ab+4b^2+9a^2", "-12ab+9a^2+4b^2"), true)
    val exponents21c = new Question(M("(a+b)^2="), List("a^2+2ab+b^2", "b^2+2ab+a^2", "a^2+b^2+2ab", "b^2+a^2+2ab", "2ab+b^2+a^2", "2ab+a^2+b^2"), true)
    val exponents21d = new Question(M("(3a+4b)^2="), List("9a^2+24ab+16b^2", "16b^2+24ab+9a^2", "9a^2+16b^2+24ab", "16b^2+9a^2+24ab", "24ab+16b^2+9a^2", "24ab+9a^2+16b^2"), true)
    val exponents21e = new Question(M("(5a+4b)^2="), List("25a^2+40ab+16b^2", "16b^2+40ab+25a^2", "25a^2+16b^2+40ab", "16b^2+25a^2+40ab", "40ab+16b^2+25a^2", "40ab+25a^2+16b^2"), true)
    val exponents22a = new Question(M("\\frac{6a+9b}{21a}="), List("(2a+3b)/7a", "2/7+3b/7a"), true)
    val exponents22b = new Question(M("\\frac{6a+10b}{4a}="), List("(3a+5b)/2a", "3/2+5b/2a"), true)
    val exponents22c = new Question(M("\\frac{6a^2+10ab}{4a}="), List("(3a+5b)/2", "3a/2+5b/2"), true)
    val exponents22d = new Question(M("\\frac{6a+12ab}{4a}="), List("(3+6b)/2", "3/2+3b"), true)
    val exponents22e = new Question(M("\\frac{6a+12ab}{10a}="), List("(3+6b)/5", "3/5+6b/5"), true)
    val exponents22f = new Question(M("\\frac{8a+12ab}{10a}="), List("(4+6b)/5", "4/5+6b/5", "4/5+(6/5)b"), true)
    val exponents23a = new Question(M("\\frac{10a^3+15a^2}{5a^3}="), List("(2a+3)/a", "2+3/a"), true)
    val exponents23b = new Question(M("\\frac{10a^3+15a^2}{5a^4}="), List("(2a+3)/a^2", "2/a+3/a^2"), true)
    val exponents23c = new Question(M("\\frac{20a^3+15a^2}{5a^4}="), List("(4a+3)/a^2", "4/a+3/a^2"), true)
    val exponents23d = new Question(M("\\frac{10a^3+15a^2}{10a^3}="), List("(2a+3)/2a", "1+3/2a"), true)
    val exponents23e = new Question(M("\\frac{10a^3+15a^2}{10a^2}="), List("(2a+3)/2", "a+3/2", "3/2+a"), true)
    val exponents23f = new Question(M("\\frac{10a^3+15a^2}{25a}="), List("(2a^2+3a)/5", "2a^2/5+3a/5", "3a/5+2a^2/5"), true)
    pm.makePersistentAll(List(exponents1, exponents2parta, exponents2partb, exponents3, exponents4, exponents5, exponents6a, exponents6b, exponents6c,
      exponents6d, exponents7a, exponents7b, exponents7c, exponents7d, exponents7e, exponents8a, exponents8b, exponents8c, exponents8d, exponents9a,
      exponents9b, exponents9c, exponents9d, exponents9e, exponents9f, exponents10a, exponents10b, exponents11a, exponents11b, exponents11c, exponents11d,
      exponents12a, exponents12b, exponents12c, exponents12d, exponents12e, exponents12f, exponents13a, exponents13b, exponents13c, exponents13d, exponents13e,
      exponents14a, exponents14b, exponents14c, exponents14d, exponents14e, exponents15a, exponents15b, exponents15c, exponents15d, exponents15e, exponents16a,
      exponents16b, exponents16c, exponents17a, exponents17b, exponents17c, exponents17d, exponents17e, exponents18a, exponents18b, exponents18c, exponents18d,
      exponents18e, exponents19a, exponents19b, exponents19c, exponents19d, exponents19e, exponents20a, exponents20b, exponents20c, exponents20d, exponents20e,
      exponents21a, exponents21b, exponents21c, exponents21d, exponents21e, exponents22a, exponents22b, exponents22c, exponents22d, exponents22e, exponents22f,
      exponents23a, exponents23b, exponents23c, exponents23d, exponents23e, exponents23f))
    val exponentsQset1 = new QuestionSet(List(exponents1))
    val exponentsQset2a = new QuestionSet(List(exponents2parta))
    val exponentsQset2b = new QuestionSet(List(exponents2partb))
    val exponentsQset3 = new QuestionSet(List(exponents3))
    val exponentsQset4 = new QuestionSet(List(exponents4))
    val exponentsQset5 = new QuestionSet(List(exponents5))
    val exponentsQset6 = new QuestionSet(List(exponents6a, exponents6b, exponents6c, exponents6d))
    val exponentsQset7 = new QuestionSet(List(exponents7a, exponents7b, exponents7c, exponents7d, exponents7e))
    val exponentsQset8 = new QuestionSet(List(exponents8a, exponents8b, exponents8c, exponents8d))
    val exponentsQset9 = new QuestionSet(List(exponents9a, exponents9b, exponents9c, exponents9d, exponents9e, exponents9f))
    val exponentsQset10 = new QuestionSet(List(exponents10a, exponents10b))
    val exponentsQset11 = new QuestionSet(List(exponents11a, exponents11b, exponents11c, exponents11d))
    val exponentsQset12 = new QuestionSet(List(exponents12a, exponents12b, exponents12c, exponents12d, exponents12e, exponents12f))
    val exponentsQset13 = new QuestionSet(List(exponents13a, exponents13b, exponents13c, exponents13d, exponents13e))
    val exponentsQset14 = new QuestionSet(List(exponents14a, exponents14b, exponents14c, exponents14d, exponents14e))
    val exponentsQset15 = new QuestionSet(List(exponents15a, exponents15b, exponents15c, exponents15d, exponents15e))
    val exponentsQset16 = new QuestionSet(List(exponents16a, exponents16b, exponents16c))
    val exponentsQset17 = new QuestionSet(List(exponents17a, exponents17b, exponents17c, exponents17d, exponents17e))
    val exponentsQset18 = new QuestionSet(List(exponents18a, exponents18b, exponents18c, exponents18d, exponents18e))
    val exponentsQset19 = new QuestionSet(List(exponents19a, exponents19b, exponents19c, exponents19d, exponents19e))
    val exponentsQset20 = new QuestionSet(List(exponents20a, exponents20b, exponents20c, exponents20d, exponents20e))
    val exponentsQset21 = new QuestionSet(List(exponents21a, exponents21b, exponents21c, exponents21d, exponents21e))
    val exponentsQset22 = new QuestionSet(List(exponents22a, exponents22b, exponents22c, exponents22d, exponents22e, exponents22f))
    val exponentsQset23 = new QuestionSet(List(exponents23a, exponents23b, exponents23c, exponents23d, exponents23e, exponents23f))
    pm.makePersistentAll(List(exponentsQset1, exponentsQset2a, exponentsQset2b, exponentsQset3, exponentsQset4, exponentsQset5, exponentsQset6, exponentsQset7,
      exponentsQset8, exponentsQset9, exponentsQset10, exponentsQset11, exponentsQset12, exponentsQset13, exponentsQset14, exponentsQset15, exponentsQset16, exponentsQset17,
      exponentsQset18, exponentsQset19, exponentsQset20, exponentsQset21, exponentsQset22, exponentsQset23))
    val ExponentSection1 = new QuizSection("Sentences", "Complete the sentences below:", List(exponentsQset1, exponentsQset2a, exponentsQset2b, exponentsQset3, exponentsQset4, exponentsQset5))
    val ExponentSection2 = new QuizSection("Simplifing", "Simplify the following:", List(exponentsQset6, exponentsQset7, exponentsQset8, exponentsQset9, exponentsQset10,
      exponentsQset11, exponentsQset12, exponentsQset13, exponentsQset14, exponentsQset15, exponentsQset16, exponentsQset17, exponentsQset18, exponentsQset19, exponentsQset20,
      exponentsQset21, exponentsQset22, exponentsQset23))
    val ExponentsMastery = new Quiz("Exponents Mastery", List(ExponentSection1, ExponentSection2))
    pm.makePersistentAll(List(ExponentsMastery))
    val solvequations1a = new Question(M("10-\\frac{3}{4}n=14"), List("-16/3", "-5+1/3"), true)
    val solvequations1b = new Question(M("8-\\frac{4}{5}n=12"), List("-5"), true)
    val solvequations1c = new Question(M("2-\\frac{4}{5}n=12"), List("-25/2", "-12+1/2"), true)
    val solvequations1d = new Question(M("8-\\frac{3}{4}n=14"), List("-8"), true)
    val solvequations1e = new Question(M("8-\\frac{2}{3}n=14"), List("-9"), true)
    val solvequations2a = new Question(M("0=5(8n+4)"), List("-1/2"), true)
    val solvequations2b = new Question(M("0=2(5n-9)"), List("9/5", "1+4/5"), true)
    val solvequations2c = new Question(M("3(2n-5)+7=6n-8+n"), List("0"), true)
    val solvequations2d = new Question(M("0=3(4n-1)"), List("1/4"), true)
    val solvequations2e = new Question(M("0=5(9n-1)"), List("1/9"), true)
    val solvequations2f = new Question(M("0=5(9n-4)"), List("4/9"), true)
    val solvequations3a = new Question(M("n^2-12=4n"), List("6, -2", "-2, 6"), true)
    val solvequations3b = new Question(M("n^2+10n=24"), List("-12,2", "2,-12"), true)
    val solvequations3c = new Question(M("n^2+20=9n"), List("4,5", "5,4"), true)
    val solvequations3d = new Question(M("n^2=24-5n"), List("5/2,-4", "-4,5/2"), true)
    val solvequations3e = new Question(M("n^2-12=n"), List("4,-3", "-3,4"), true)
    val solvequations3f = new Question(M("n^2-18=3n"), List("-3,6", "6,-3"), true)
    val solvequations4a = new Question(M("5-(3n+6)+n=3-2n"), List("ns", "NS", "Ns", "no solution"), true)
    val solvequations4b = new Question(M("3(2n-1)+7=5n+4+n"), List("all real numbers", "ARN", "arn"), true)
    val solvequations4c = new Question(M("0=2(4n-9)"), List("9/4", "2+1/4"), true)
    val solvequations4d = new Question(M("(2n-5)(n+4)=0"), List("5/2,-4", "-4,5/2"), true)
    val solvequations4e = new Question(M("3(2n-5)+10=6n-5+n"), List("0"), true)
    val solvequations4f = new Question(M("9-(3n+6)+n=3-2n"), List("all real numbers", "ARN", "arn"), true)
    val solvequations5a = new Question(M("\\frac{2}{5}n+2=\\frac{2}{3}n-3"), List("75/4", "18+3/4"), true)
    val solvequations5b = new Question(M("\\frac{1}{5}n+2=\\frac{3}{4}n-3"), List("100/11", "9+1/11"), true)
    val solvequations5c = new Question(M("\\frac{2}{5}n+4=\\frac{3}{4}n-4"), List("20"), true)
    val solvequations5d = new Question(M("\\frac{4}{5}n+4=\\frac{3}{4}n-3"), List("-140"), true)
    val solvequations5e = new Question(M("\\frac{3}{5}n+4=\\frac{2}{3}n-3"), List("105"), true)
    val solvequations5f = new Question(M("\\frac{2}{5}n+4=\\frac{2}{3}n-3"), List("105/4", "26+1/4"), true)
    val solvequations6a = new Question(M("(3n+5)(2n-6)=0"), List("-5/3,3", "3,-5/3"), true)
    val solvequations6b = new Question(M("(2n-5)(n+4)=0"), List("5/2,-4", "-4,5/2"), true)
    val solvequations6c = new Question(M("(2n-7)(3n+1)=0"), List("7/2,-1/3", "-1/3,7/2"), true)
    val solvequations6d = new Question(M("1-3n=5n+1-8n"), List("all real numbers", "ARN", "arn"), true)
    val solvequations6e = new Question(M("(3n+4)(2n-9)=0"), List("-4/3,9/2", "9/2,-4/3"), true)
    val solvequations6f = new Question(M("(3n+2)(2n-9)=0"), List("-2/3,9/2", "9/2,-2/3"), true)
    val solvequations7a = new Question(M("3(2n-2)+10=6n+4+n"), List("0"), true)
    val solvequations7b = new Question(M("9-(3n+10)=7+n"), List("-2"), true)
    val solvequations7c = new Question(M("9-(3n+6)=7+5n"), List("-1/2"), true)
    val solvequations7d = new Question(M("9-(3n+6)+n=7-2n"), List("no solution", "NS", "Ns", "ns"), true)
    val solvequations7e = new Question(M("3(2n-5)+10=6n-5+n"), List("no solution", "NS", "Ns", "ns"), true)
    val solvequations8a = new Question(M("4(n+2)-10=5n-(6-2n)-2"), List("2"), true)
    val solvequations8b = new Question(M("10-3(n+3)=1"), List("0"), true)
    val solvequations8c = new Question(M("4(n+2)-10=3n-(3-n)+1"), List("all real numbers", "ARN", "arn"), true)
    val solvequations8d = new Question(M("4(n+2)-10=3n-(3+n)+1"), List("0"), true)
    val solvequations8e = new Question(M("4(n+2)-10=5n-(6-2n)+1"), List("1"), true)
    val solvequations8f = new Question(M("4(n+2)-10=5n-(6-2n)-2"), List("2"), true)
    val solvequations9a = new Question(M("b=\\frac{2}{3}a+14"), List("3/2b-21", "3b/2-21"), true)
    val solvequations9b = new Question(M("b=\\frac{2}{3}a+4"), List("3/2b-6", "3b/2-6"), true)
    val solvequations9c = new Question(M("b=\\frac{4}{3}a+4"), List("3/4b-3", "3b/4-3"), true)
    val solvequations9d = new Question(M("b=\\frac{5}{8}a+10"), List("8/5b-16", "8b/5-16"), true)
    val solvequations9e = new Question(M("b=\\frac{4}{3}a+10"), List("3/4b-15/2", "3b/4-15/2", "3/4b-15/2", "3b/4-15/2"), true)
    val solvequations9f = new Question(M("b=\\frac{2}{3}a+10"), List("3/2b-15", "2b/2-15"), true)
    val solvequations10a = new Question(M("\\frac{4(3a+2b)}{5c}=60"), List("25c-2/3b", "25c-2b/3"), true)
    val solvequations10b = new Question(M("\\frac{4(2a+6b)}{3c}=40"), List("15c-3b", "-3b+15c"), true)
    val solvequations10c = new Question(M("\\frac{4(5a+6b)}{3c}=40"), List("6c-6/5b", "6c-6b/5", "-6b/5+6c", "-6/5b+6c"), true)
    val solvequations10d = new Question(M("\\frac{2(5a-b)}{5c}=40"), List("b/5+20c", "1/5b+20c", "20c+1/5b", "20c+b/5"), true)
    val solvequations10e = new Question(M("\\frac{2(5a+b)}{5c}=40"), List("20c-b/5", "-b/5+20c", "-1/5b+20c", "20c-1/5b"), true)
    val solvequations10f = new Question(M("\\frac{4(5a+b)}{5c}=40"), List("10c-b/5", "-b/5+10c", "10c-1/5b", "-1/5b+10c"), true)
    pm.makePersistentAll(List(solvequations1a, solvequations1b, solvequations1c, solvequations1d, solvequations1e, solvequations2a, solvequations2b, solvequations2c, solvequations2d,
      solvequations2e, solvequations2f, solvequations3a, solvequations3b, solvequations3c, solvequations3d, solvequations3d, solvequations3f, solvequations4a, solvequations4b,
      solvequations4c, solvequations4d, solvequations4e, solvequations4f, solvequations5a, solvequations5b, solvequations5c, solvequations5d, solvequations5e, solvequations5f,
      solvequations6a, solvequations6b, solvequations6c, solvequations6d, solvequations6e, solvequations6f, solvequations7a, solvequations7b, solvequations7c, solvequations7d,
      solvequations7e, solvequations8a, solvequations8b, solvequations8c, solvequations8d, solvequations8e, solvequations8f, solvequations9a, solvequations9b, solvequations9c,
      solvequations9d, solvequations9e, solvequations9f, solvequations10a, solvequations10b, solvequations10c, solvequations10d, solvequations10e, solvequations10f))
    val solvequationsQSet1 = new QuestionSet(List(solvequations1a, solvequations1b, solvequations1c, solvequations1d, solvequations1e))
    val solvequationsQSet2 = new QuestionSet(List(solvequations2a, solvequations2b, solvequations2c, solvequations2d, solvequations2e, solvequations2f))
    val solvequationsQSet3 = new QuestionSet(List(solvequations3a, solvequations3b, solvequations3c, solvequations3d, solvequations3e, solvequations3f))
    val solvequationsQSet4 = new QuestionSet(List(solvequations4a, solvequations4b, solvequations4c, solvequations4d, solvequations4e, solvequations4f))
    val solvequationsQSet5 = new QuestionSet(List(solvequations5a, solvequations5b, solvequations5c, solvequations5d, solvequations5e, solvequations5f))
    val solvequationsQSet6 = new QuestionSet(List(solvequations6a, solvequations6b, solvequations6c, solvequations6d, solvequations6e, solvequations6f))
    val solvequationsQSet7 = new QuestionSet(List(solvequations7a, solvequations7b, solvequations7c, solvequations7d, solvequations7e))
    val solvequationsQSet8 = new QuestionSet(List(solvequations8a, solvequations8b, solvequations8c, solvequations8d, solvequations8e, solvequations8f))
    val solvequationsQSet9 = new QuestionSet(List(solvequations9a, solvequations9b, solvequations9c, solvequations9d, solvequations9e, solvequations9f))
    val solvequationsQSet10 = new QuestionSet(List(solvequations10a, solvequations10b, solvequations10c, solvequations10d, solvequations10e, solvequations10f))
    pm.makePersistentAll(List(solvequationsQSet1, solvequationsQSet2, solvequationsQSet3, solvequationsQSet4, solvequationsQSet5, solvequationsQSet6, solvequationsQSet7,
      solvequationsQSet8, solvequationsQSet9, solvequationsQSet10))
    val solvequationsSection1 = new QuizSection("Solve for n", "Solve for n:", List(solvequationsQSet1, solvequationsQSet2, solvequationsQSet3, solvequationsQSet4, solvequationsQSet5,
      solvequationsQSet6, solvequationsQSet7, solvequationsQSet8))
    val solvequationsSection2 = new QuizSection("Solve for a", "Solve for a. Do not use parenthesis or a large division bar:", List(solvequationsQSet9, solvequationsQSet10))
    pm.makePersistentAll(List(solvequationsSection1, solvequationsSection2))
    val SolvequationsMastery = new Quiz("Solving Equations Mastery", List(solvequationsSection1, solvequationsSection2))
    pm.makePersistentAll(List(SolvequationsMastery))
    if (debug) println("Loading Complete!")
  }
}
