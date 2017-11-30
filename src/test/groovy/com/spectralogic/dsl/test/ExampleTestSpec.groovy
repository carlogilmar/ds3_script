import spock.lang.*

/*
  Spock Framework for implement test
    run: gradle test
 */

class MyFirstSpecification extends Specification {

  def "First Test for test Spock Framework in this project"() {
    given:"A list of numbers"
      ArrayList<Integer> numbers = [1,2,3,4,5,6]
    when:"I want to know the size"
      def arraySize = numbers.size()
    then:"I should get the correct number of elements"
      //This should fail!!
      arraySize == 0
  }

  def "Second Test for test Spock Framework in this project"() {
    given:"A list of numbers"
      ArrayList<Integer> numbers = [1,2,3,4,5,6]
    when:"I want to know the size"
      def arraySize = numbers.size()
    then:"I should get the correct number of elements"
      //This should pass
      arraySize == 6
  }

}
