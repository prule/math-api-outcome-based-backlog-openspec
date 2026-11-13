# Spec Delta

## Purpose

Lets developers and operators tell, over HTTP, whether a running instance of the service is up and able to serve.

## ADDED Requirements

### Requirement: Service reports its health
The running service SHALL answer `GET /actuator/health` with HTTP 200 and a JSON body whose `status` is `UP` once it
has started and is able to serve requests.

#### Scenario: Started service is healthy
- **WHEN** the service has started and a client sends `GET /actuator/health`
- **THEN** the response status is 200
- **AND** the JSON body has `"status": "UP"`
