run:
	./gradlew clean build -x test
	docker-compose up --build

down:
	docker-compose down

test:
	docker pull quay.io/testcontainers/ryuk:0.2.2
	./gradlew test
