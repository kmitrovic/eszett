run:
	./gradlew clean build -x test
	docker-compose up --build

down:
	docker-compose down

test:
	./gradlew test
