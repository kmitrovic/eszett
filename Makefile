run:
	./gradlew clean build
	docker-compose up --build

down:
	docker-compose down
