# eszett
Test: employee storage service for companies

# Docker compose
To start the server execute from within this folder:
`make run`

Check the default UI at http://127.0.0.1:8080/ on your machine.

Stop composer by interrupting it with Ctrl+c, and then execute:
`make down`

# Initial data
Check src/main/resources/db/migration/V2021.03.26.22.41.00__Add_initial_dummy_data.sql

After it is loaded, there will be a single superuser kris/kris01. No admin users will be created.
