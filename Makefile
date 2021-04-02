run:
	./gradlew clean build composeUp -x test

down:
	./gradlew composeDown

test:
	./gradlew test
